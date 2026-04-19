package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Dedicated {@link StructurePiece} for the Phase 13C Royal Trees (Tree of
 * Goodness / Queen Tree).
 *
 * <p><b>What this class fixes (the deadlock-grade freeze):</b> the previous
 * multi-pass implementation ran the full 1.7.10 {@code MakeBigSquareTree} +
 * recursive {@code make_branch} algorithm inside every {@code postProcess}
 * call. Vanilla fires {@code postProcess} once per chunk that intersects the
 * piece's bounding box — for our ±48-block footprint that is up to 36 chunks
 * per tree. The recursive {@code make_branch} algorithm, with its branching
 * factor of ~3 per recursion level and depth of 5, generates several million
 * coordinate computations and {@code place()} calls per pass; multiplied by
 * 36 passes the server thread spent several minutes per tree, pinned at
 * 100% CPU and accumulating gigabytes of {@code BlockPos} allocation
 * pressure. The user reported this as "completely froze (infinite loop /
 * deadlock)" because the world tick thread was unresponsive long enough for
 * watchdogs to flag it.</p>
 *
 * <p><b>How the fix works:</b> two changes.</p>
 * <ol>
 *   <li><b>Anchor-chunk gate</b> at the top of {@link #postProcess}: only
 *       run the geometry algorithm during the chunk pass that owns the
 *       trunk origin ({@code chunkPos == origin >> 4}). The other ~35
 *       intersected chunks return immediately, dropping per-tree work from
 *       36× to 1×. Cross-chunk writes still succeed natively because
 *       structure-step {@link WorldGenLevel} (a {@code WorldGenRegion}) has
 *       a write radius of 8 chunks centred on the generating chunk —
 *       comfortably covering our ±3-chunk tree footprint — and the
 *       structure's declared bounding box is the "permit" that reserves
 *       those neighbour chunks against re-generation.</li>
 *   <li><b>Hot-path optimisation</b>: the per-cell write helper
 *       {@link #place} is rewritten to use cached {@code int} bounds,
 *       a single reusable {@link BlockPos.MutableBlockPos}, and a
 *       structure-bounding-box gate (instead of the per-chunk
 *       {@code chunkBox.isInside}). The previous helper allocated a fresh
 *       {@link BlockPos} on every call (millions per tree) and made two
 *       virtual calls into {@code WorldGenLevel} for {@code minBuildHeight}
 *       / {@code maxBuildHeight} per call. The new helper does pure int
 *       comparisons + a mutable position set + the actual write. This
 *       removes the GC pressure that was compounding the freeze.</li>
 * </ol>
 *
 * <p><b>Audit checklist (per the freeze-fix directive):</b></p>
 * <ul>
 *   <li><b>Loop counters never live inside the bounding-box guard.</b>
 *       Every {@code ++j}, {@code ++i}, {@code ++current_y}, {@code --this_width},
 *       {@code ++spiral}, {@code ++last_branch}, {@code --current_width}
 *       runs unconditionally. The bounds check exclusively wraps the
 *       {@code level.setBlock} call inside {@link #place}.</li>
 *   <li><b>Zero world reads.</b> No {@code getBlockState}, no
 *       {@code isEmptyBlock}, no {@code getHeightmapPos} anywhere in the
 *       generation logic. The only world calls are
 *       {@code getMinBuildHeight} / {@code getMaxBuildHeight}, which are
 *       constant int reads on {@code WorldGenLevel} and are now hoisted
 *       out of the hot loop and cached once per {@link #postProcess}.</li>
 *   <li><b>Recursion is finite by construction.</b> {@link #makeBranch}
 *       recurses with {@code current_width - 1} guarded by
 *       {@code current_width > 0}, so depth is bounded by the initial
 *       {@code this_width} (≤ 5). The intra-call safety counter on the
 *       branch-direction-dedup loop is bounded at 8 iterations.</li>
 * </ul>
 *
 * <p><b>Geometry preservation</b> — the {@code MakeBigSquareTree} +
 * {@code make_branch} algorithm ported from
 * {@code reference_1_7_10_source/sources/danger/orespawn/ItemMagicApple.java}
 * lines 248-469 + 133-246 is preserved verbatim. Same loops, same RNG
 * draws in the same order, same per-cell block selections, same rare
 * gem-leaf substitution chances, same apex emerald-pair + spawner crown.
 * The deltas vs. the deleted {@code RoyalTreeFeature}: terrain reads
 * removed (always-write for chunk safety + determinism), all writes
 * routed through the bounding-box-gated {@link #place} helper, and RNG
 * seeded from the piece origin instead of the feature context.</p>
 */
public class RoyalTreePiece extends StructurePiece {

    /** Legacy {@code ItemMagicApple#tree_radius = 6}. Drives tower size and branch reach. */
    private static final int TREE_RADIUS = 6;

    /** Foundation pillar depth (legacy block6 walked up to 20 blocks; we use a fixed
     *  deterministic 6 since terrain reads are gone). Surface-to-stone in Utopia
     *  plains is 3-5 blocks, so 6 reliably anchors the four mid-side pillars. */
    private static final int BASE_DEPTH = 6;

    /** {@code Block.UPDATE_CLIENTS}: no neighbour cascade, no lighting recompute. */
    private static final int FLAG_CLIENTS_ONLY = 2;

    /** Bounding-box "permit" extents. Horizontal ±48 = ±3 chunks, comfortably
     *  inside the 8-chunk WorldGenRegion radius for the structure step, and
     *  generous enough to cover the recursive {@code make_branch} reach
     *  (sub-branch xaccum can extend ~40 blocks from the trunk centre).
     *  Vertical −12 below origin covers the foundation walks; +80 above
     *  covers a generous tower height (legacy max ~50). */
    private static final int H_EXTENT = 48;
    private static final int DOWN_EXTENT = 12;
    private static final int UP_EXTENT = 80;

    private final BlockPos origin;
    private final boolean queenVariant;

    // ---- Per-postProcess hot-loop cache ---------------------------------
    // These transient fields are set at the top of postProcess and read by
    // the inlined place() helper on every cell. Caching them avoids virtual
    // calls into WorldGenLevel and per-call BoundingBox accessor overhead in
    // the algorithm's millions-of-cells inner loops. Not serialised — they
    // get rebuilt on every postProcess invocation.
    private transient WorldGenLevel pLevel;
    private transient BlockPos.MutableBlockPos pMut;
    private transient int pMinY;
    private transient int pMaxY;
    private transient int pBbMinX;
    private transient int pBbMaxX;
    private transient int pBbMinY;
    private transient int pBbMaxY;
    private transient int pBbMinZ;
    private transient int pBbMaxZ;

    public RoyalTreePiece(BlockPos origin, boolean queenVariant) {
        super(ModStructureTypes.ROYAL_TREE_PIECE.get(), 0,
                new BoundingBox(
                        origin.getX() - H_EXTENT,
                        origin.getY() - DOWN_EXTENT,
                        origin.getZ() - H_EXTENT,
                        origin.getX() + H_EXTENT,
                        origin.getY() + UP_EXTENT,
                        origin.getZ() + H_EXTENT));
        this.origin = origin.immutable();
        this.queenVariant = queenVariant;
    }

    public RoyalTreePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructureTypes.ROYAL_TREE_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
        this.queenVariant = tag.getBoolean("q");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("ox", origin.getX());
        tag.putInt("oy", origin.getY());
        tag.putInt("oz", origin.getZ());
        tag.putBoolean("q", queenVariant);
    }

    @Override
    public void postProcess(WorldGenLevel level,
                            StructureManager structureManager,
                            ChunkGenerator chunkGenerator,
                            RandomSource random,
                            BoundingBox chunkBox,
                            ChunkPos chunkPos,
                            BlockPos pivot) {
        // ---- Anchor-chunk gate (THE freeze fix) -------------------------
        // Vanilla calls postProcess once per chunk that intersects the
        // piece's bounding box — up to 36 chunks for our 96x96 footprint.
        // Without this gate, the recursive 1.7.10 algorithm runs in full
        // 36 times per tree (~tens of millions of place() calls + matching
        // BlockPos allocations), pinning the world tick thread for several
        // minutes. Cross-chunk writes from this single execution still land
        // because WorldGenLevel (a WorldGenRegion under the hood) accepts
        // writes anywhere inside the 8-chunk-radius structure region, and
        // the structure's declared bounding box reserves those neighbour
        // chunks so the chunk generator doesn't bulldoze us afterward.
        if (chunkPos.x != (origin.getX() >> 4) || chunkPos.z != (origin.getZ() >> 4)) {
            return;
        }

        // Deterministic per-piece RNG. Seeded purely from the piece's
        // static bounding-box corners — survives save/reload because the
        // BB is part of StructurePiece's own serialisation.
        RandomSource rng = RandomSource.create(
                (long) this.boundingBox.minX() * 341873128712L
                        + (long) this.boundingBox.minZ() * 132897987541L);

        // Cache hot-loop values + reusable mutable position. Hoisting these
        // out of the per-cell hot path eliminates the GC pressure and
        // virtual-call overhead that compounded the freeze.
        this.pLevel = level;
        this.pMut = new BlockPos.MutableBlockPos();
        this.pMinY = level.getMinBuildHeight();
        this.pMaxY = level.getMaxBuildHeight();
        this.pBbMinX = this.boundingBox.minX();
        this.pBbMaxX = this.boundingBox.maxX();
        this.pBbMinY = this.boundingBox.minY();
        this.pBbMaxY = this.boundingBox.maxY();
        this.pBbMinZ = this.boundingBox.minZ();
        this.pBbMaxZ = this.boundingBox.maxZ();

        BlockState trunk;
        BlockState leaves;
        BlockState steps;
        BlockState spawner;
        if (queenVariant) {
            trunk = Blocks.OBSIDIAN.defaultBlockState();
            leaves = ModBlocks.BLOCK_RUBY.get().defaultBlockState();
            steps = ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
            spawner = ModBlocks.QUEEN_SPAWNER.get().defaultBlockState();
        } else {
            trunk = Blocks.GOLD_BLOCK.defaultBlockState();
            leaves = Blocks.EMERALD_BLOCK.defaultBlockState();
            steps = Blocks.DIAMOND_BLOCK.defaultBlockState();
            spawner = ModBlocks.KING_SPAWNER.get().defaultBlockState();
        }

        try {
            makeBigSquareTree(rng,
                    origin.getX(), origin.getY(), origin.getZ(),
                    trunk, leaves, steps, spawner);
        } finally {
            // Drop the WorldGenLevel reference so this piece can't pin a
            // world generation context across postProcess invocations.
            this.pLevel = null;
            this.pMut = null;
        }
    }

    /**
     * Per-cell write. The bounding-box gate is the structure's declared
     * footprint (the "permit") rather than a per-chunk slice, which is what
     * lets the single anchor-chunk pass safely write into all 36 chunks
     * the structure has reserved. The mathematical algorithm above this
     * helper is fully independent of the gate — counters, RNG draws, and
     * coordinate computations all run unconditionally; only the actual
     * {@code level.setBlock} call is wrapped.
     */
    private void place(int x, int y, int z, BlockState state) {
        if (y < pMinY || y >= pMaxY) return;
        if (x < pBbMinX || x > pBbMaxX) return;
        if (z < pBbMinZ || z > pBbMaxZ) return;
        if (y < pBbMinY || y > pBbMaxY) return;
        pMut.set(x, y, z);
        pLevel.setBlock(pMut, state, FLAG_CLIENTS_ONLY);
    }

    /**
     * Byte-for-byte port of the legacy
     * {@code ItemMagicApple#MakeBigSquareTree} (lines 248-469). Loops, RNG
     * draws, and per-cell block selections are preserved verbatim. The
     * deviations from legacy: terrain reads dropped (always-write for chunk
     * safety + determinism), foundation walk uses a fixed
     * {@link #BASE_DEPTH} instead of "walk down until non-stone", live boss
     * spawn at apex replaced with a placed spawner block, and every
     * {@code setBlock} routed through {@link #place} for the bounding-box
     * gate.
     */
    private void makeBigSquareTree(RandomSource rand,
                                   int x, int y, int z,
                                   BlockState trunkID, BlockState leafID,
                                   BlockState stepID, BlockState spawnerCap) {
        int t_radius = TREE_RADIUS;
        int this_height = t_radius + rand.nextInt(t_radius);
        int this_width = t_radius;
        int base_height = t_radius * 3;
        int spiral;
        int current_y;
        boolean do_floor;
        int platform_looper;
        int last = -1;
        int last_last = -1;

        // ---- Anchor the four perimeter mid-side pillars down to surface ----
        for (int i = -t_radius; i <= t_radius; ++i) {
            for (int j = 0; j < BASE_DEPTH; ++j) {
                if (y - j <= pMinY) break;
                place(x + i, y - j, z - t_radius, trunkID);
                place(x + i, y - j, z + t_radius, trunkID);
                place(x - t_radius, y - j, z + i, trunkID);
                place(x + t_radius, y - j, z + i, trunkID);
            }
        }

        // ---- Layered tower body (legacy line 309-442) ----
        current_y = y;
        spiral = -this_width;
        while (this_width >= 0) {
            if (this_width != t_radius) {
                base_height = 0;
            }
            for (int j = 0; j < this_height + base_height; ++j) {
                do_floor = false;

                // Wall ring at current_y on all four sides
                for (int i = -this_width; i <= this_width; ++i) {
                    place(x + i, current_y, z - this_width, trunkID);
                    place(x + i, current_y, z + this_width, trunkID);
                    place(x - this_width, current_y, z + i, trunkID);
                    place(x + this_width, current_y, z + i, trunkID);
                }

                // Spiral steps + periodic full floors (legacy line 344-401)
                if (this_width != 0 || j < this_height / 2) {
                    platform_looper = 1;
                    if ((spiral == 0 && this_width >= 2)
                            || spiral == this_width
                            || (spiral == this_width - 1 && j == this_height + base_height - 1)) {
                        ++platform_looper;
                        if (spiral != 0 && this_width >= 3) ++platform_looper;
                        if (spiral == 0) do_floor = true;
                    }
                    for (int k = 0; k < platform_looper; ++k) {
                        place(x - spiral, current_y, z - this_width - 1, stepID);
                        place(x + spiral, current_y, z + this_width + 1, stepID);
                        place(x - this_width - 1, current_y, z + spiral, stepID);
                        place(x + this_width + 1, current_y, z - spiral, stepID);
                        if (this_width >= 3) {
                            place(x - spiral, current_y, z - this_width - 2, stepID);
                            place(x + spiral, current_y, z + this_width + 2, stepID);
                            place(x - this_width - 2, current_y, z + spiral, stepID);
                            place(x + this_width + 2, current_y, z - spiral, stepID);
                        }
                        if (platform_looper == 1) continue;
                        ++spiral;
                    }
                    if (do_floor) {
                        for (int m = -this_width; m <= this_width; ++m) {
                            for (int n = -this_width; n <= this_width; ++n) {
                                place(x + m, current_y, z + n, trunkID);
                            }
                        }
                    }
                }

                // Branch dispatch (legacy line 403-430). Only inner shrinking
                // tiers spawn branches — the outermost (this_width == t_radius)
                // is a pure wall, matching the legacy guard.
                if (this_width != t_radius) {
                    int next = rand.nextInt(4 + this_width);
                    int safety = 0;
                    while ((next == last || next == last_last) && safety++ < 8) {
                        next = rand.nextInt(4 + this_width);
                    }
                    if (next < 4) {
                        last_last = last;
                        last = next;
                    }
                    switch (next) {
                        case 0 -> makeBranch(rand, x + this_width, current_y, z,
                                this_width, 1, 0, trunkID, leafID);
                        case 1 -> makeBranch(rand, x - this_width, current_y, z,
                                this_width, -1, 0, trunkID, leafID);
                        case 2 -> makeBranch(rand, x, current_y, z + this_width,
                                this_width, 0, 1, trunkID, leafID);
                        case 3 -> makeBranch(rand, x, current_y, z - this_width,
                                this_width, 0, -1, trunkID, leafID);
                        default -> { /* idle iteration */ }
                    }
                }

                ++current_y;
                if (!do_floor) ++spiral;
                if (spiral > this_width) spiral = -this_width;
            }
            if (Math.abs(spiral) > --this_width) spiral = -this_width;
            this_height += rand.nextInt(t_radius);
        }

        // ---- Apex (legacy line 443-468) ----
        BlockState emeraldCrown = Blocks.EMERALD_BLOCK.defaultBlockState();
        place(x, current_y, z, emeraldCrown);
        place(x, current_y + 1, z, emeraldCrown);
        place(x, current_y + 2, z, spawnerCap);
    }

    /**
     * Byte-for-byte port of the legacy {@code ItemMagicApple#make_branch}
     * (lines 133-246). Same control flow, same RNG draws in the same order.
     * Recursion is bounded by {@code current_width - 1} guarded by
     * {@code current_width > 0}, so depth ≤ initial {@code this_width} ≤ 5.
     */
    private void makeBranch(RandomSource rand,
                            int x, int y, int z,
                            int this_width, int dirx, int dirz,
                            BlockState trunkID, BlockState leafID) {
        int current_width = this_width;
        int last_branch = 0;
        int branch_side = (rand.nextInt(2) == 0) ? -1 : 1;
        int xaccum = dirx;
        int zaccum = dirz;
        while (current_width >= 0) {
            int length = this_width * 3 + rand.nextInt(this_width + 3);
            for (int i = 0; i < length; ++i) {
                // Trunk slice perpendicular to branch direction
                for (int jj = -current_width; jj <= current_width; ++jj) {
                    int realx = x + jj * dirz + xaccum;
                    int realz = z + jj * dirx + zaccum;
                    place(realx, y, realz, trunkID);
                }

                // Leaf cluster at branch tip (legacy line 173-228)
                if (current_width < 3 || this_width <= 1) {
                    int leaf_depth = 2 + rand.nextInt(2);
                    int leaf_width = 2 + rand.nextInt(3);
                    for (int n = 0; n < leaf_depth; ++n) {
                        int lw = current_width + leaf_width - n;
                        if (current_width == 0 && length - i <= 2 && lw >= length - i) {
                            lw = length - i - 1;
                        }
                        if (lw < 0) lw = 0;
                        for (int jj = -lw; jj <= lw; ++jj) {
                            int realx = x + jj * Math.abs(dirz) + xaccum + dirx;
                            int realz = z + jj * Math.abs(dirx) + zaccum + dirz;
                            BlockState localLeaf = leafID;
                            // Rare gem-leaf substitution (legacy line 206-224):
                            //   1/20 chance to "fancify"; on hit, 2/3 -> redstone
                            //   block, 1/3 -> uniform pick from {U, Ti, Ruby, Amethyst}
                            if (rand.nextInt(20) == 1) {
                                if (rand.nextInt(3) != 0) {
                                    localLeaf = Blocks.REDSTONE_BLOCK.defaultBlockState();
                                } else {
                                    int ilt = rand.nextInt(4);
                                    localLeaf = switch (ilt) {
                                        case 0 -> ModBlocks.BLOCK_URANIUM.get().defaultBlockState();
                                        case 1 -> ModBlocks.BLOCK_TITANIUM.get().defaultBlockState();
                                        case 2 -> ModBlocks.BLOCK_RUBY.get().defaultBlockState();
                                        case 3 -> ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
                                        default -> leafID;
                                    };
                                }
                            }
                            place(realx, y + n, realz, localLeaf);
                        }
                    }
                }

                // Recursive perpendicular sub-branches (legacy line 229-239).
                // Termination guarantee: recursion is gated by current_width > 0
                // and passes current_width - 1 as the new this_width. Each level
                // strictly decreases, so the call tree is finite (max depth ≤
                // initial this_width ≤ TREE_RADIUS = 6).
                if (current_width > 0 && last_branch > current_width
                        && current_width != this_width
                        && rand.nextInt(current_width + 1) == 0) {
                    int subdirx = branch_side;
                    int subdirz = 0;
                    if (dirx != 0) {
                        subdirx = 0;
                        subdirz = branch_side;
                    }
                    makeBranch(rand,
                            x + xaccum + current_width * subdirx,
                            y,
                            z + zaccum + current_width * subdirz,
                            current_width - 1, subdirx, subdirz, trunkID, leafID);
                    last_branch = 0;
                    branch_side = (branch_side < 0) ? 1 : -1;
                }

                xaccum += dirx;
                zaccum += dirz;
                ++last_branch;
            }
            --current_width;
        }
    }
}
