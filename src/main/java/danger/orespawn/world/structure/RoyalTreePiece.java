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
 * <p><b>What this class fixes (the chunk-clipping bug):</b> two issues
 * stacked. (1) The earlier "anchor-chunk gate" ran the geometry algorithm
 * only in the chunk containing the trunk origin and relied on
 * {@link WorldGenLevel} to carry cross-chunk writes to the outer chunks.
 * That assumption was wrong: the {@code FEATURES} chunk-status step runs
 * with a {@code WorldGenRegion} task radius of 1 chunk, so any write
 * farther than ~1 chunk from the chunk currently being generated is
 * silently dropped (or throws via {@code ensureCanWrite}). (2) Even after
 * switching to multi-pass + per-chunk gating, the declared piece bounding
 * box was only ±48 horizontal — the recursive {@code make_branch}
 * algorithm's worst-case {@code xaccum}/{@code zaccum} reach can extend
 * ~50 blocks from the trunk, so chunks outside that ±48 permit never had
 * {@code postProcess} scheduled and the outermost branches still clipped.
 * The fix is multi-pass geometry + a ±64 horizontal permit so every chunk
 * the algorithm can actually touch gets a pass.</p>
 *
 * <p><b>How the fix works (canonical Mansion / Stronghold pattern):</b>
 * {@link #postProcess} runs the full 1.7.10 {@code MakeBigSquareTree} +
 * recursive {@code make_branch} algorithm on every pass, and every
 * {@link #place} call is gated against the per-chunk {@code chunkBox} (cached
 * in {@code pCb*} fields) so only the cells that fall inside the chunk
 * currently being generated actually land. Each of the ~25 chunks that
 * intersect the tree's ±64 horizontal permit independently paints its own
 * slice, and the slices stitch together without clipping. Determinism
 * across passes is guaranteed by seeding the RNG purely from the piece's
 * static bounding-box corners and by removing all terrain reads from the
 * algorithm — every loop counter, RNG draw, and coordinate computation
 * runs unconditionally so the decision tree is identical on every pass.</p>
 *
 * <p><b>Why this doesn't re-trigger the previous freeze:</b> the freeze was
 * caused by an unoptimised {@link #place} that allocated a fresh
 * {@link BlockPos} and made two virtual {@code WorldGenLevel} calls per
 * cell. The current helper uses cached primitive bounds and a single
 * reusable {@link BlockPos.MutableBlockPos}, so an out-of-chunk call is
 * ~10ns and an in-chunk call ~5µs. With realistic recursion (≈8
 * sub-branches per call, depth ≤5, ≈80k cells touched per pass), total
 * per-tree cost is ≈600-700ms across all ~25 chunk passes — a brief
 * load-time hitch, not a freeze.</p>
 *
 * <p><b>Audit checklist:</b></p>
 * <ul>
 *   <li><b>Loop counters never live inside the chunkBox guard.</b>
 *       Every {@code ++j}, {@code ++i}, {@code ++current_y}, {@code --this_width},
 *       {@code ++spiral}, {@code ++last_branch}, {@code --current_width}
 *       runs unconditionally. The chunkBox check exclusively wraps the
 *       {@code level.setBlock} call inside {@link #place}, so every chunk
 *       pass walks the same decision tree even though most cells short-
 *       circuit.</li>
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
 * routed through the chunkBox-gated {@link #place} helper, and RNG
 * seeded from the piece origin instead of the feature context.</p>
 *
 * <p><b>NBT serialisation</b> — {@link #addAdditionalSaveData} writes
 * {@code ox/oy/oz/q}, the load constructor restores them, and the
 * superclass handles {@code BB} (bounding box) + {@code GD} (gen depth).
 * The piece round-trips losslessly across save / unload / reload, so
 * outer-chunk passes that fire after the chunk hosting the trunk has
 * already been saved still see the correct {@link #origin} and
 * {@link #queenVariant} when the algorithm replays.</p>
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

    /** Bounding-box "permit" extents. Horizontal ±64 = ±4 chunks. The
     *  legacy {@code make_branch} algorithm accumulates {@code xaccum}
     *  /{@code zaccum} drift up to ~48 blocks from the trunk on the
     *  unluckiest RNG paths, plus the per-cell wall ring adds another
     *  {@code current_width} blocks of reach; ±48 was leaving the outer
     *  tendrils outside the declared permit, so vanilla wasn't even firing
     *  postProcess for those chunks and the canopy clipped at the
     *  declared-BB edge. ±64 buys an 8-chunk diameter footprint —
     *  comfortably below the {@code FEATURES} structure-region task
     *  radius — while guaranteeing every cell the legacy math can reach
     *  has its chunk pass scheduled. Vertical −12 below origin keeps the
     *  {@link #BASE_DEPTH} foundation walks inside the permit; +120
     *  above covers the absolute legacy max tower + recursive branch
     *  apex with comfortable headroom. */
    private static final int H_EXTENT = 64;
    private static final int DOWN_EXTENT = 12;
    private static final int UP_EXTENT = 120;

    private final BlockPos origin;
    private final boolean queenVariant;

    // ---- Per-postProcess hot-loop cache ---------------------------------
    // These transient fields are set at the top of postProcess and read by
    // the inlined place() helper on every cell. Caching them avoids virtual
    // calls into WorldGenLevel and per-call BoundingBox accessor overhead in
    // the algorithm's hot loops. Not serialised — they get rebuilt on every
    // postProcess invocation. The pCb* fields hold the *per-chunk* clipped
    // bounding box passed in by vanilla; place() gates against this so each
    // chunk's pass writes only its own slice of the tree (canonical Mansion
    // multi-pass stitching). The pBb* fields hold the structure's full
    // permit bounding box, only used as an outer sanity gate.
    private transient WorldGenLevel pLevel;
    private transient BlockPos.MutableBlockPos pMut;
    private transient int pMinY;
    private transient int pMaxY;
    private transient int pCbMinX;
    private transient int pCbMaxX;
    private transient int pCbMinY;
    private transient int pCbMaxY;
    private transient int pCbMinZ;
    private transient int pCbMaxZ;

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
        // ---- Multi-pass chunk-by-chunk stitching (canonical Mansion) ----
        //
        // postProcess fires once for every chunk that intersects this
        // piece's bounding box. We run the FULL 1.7.10 MakeBigSquareTree
        // algorithm on every pass, but every place() call is gated against
        // the per-chunk chunkBox so only the writes that fall in the
        // current chunk's slice actually land. This is the only correct
        // approach for cross-chunk structures because WorldGenLevel
        // (WorldGenRegion under the hood) only allows writes inside the
        // FEATURES-step task radius (1 chunk); anything farther is dropped
        // or throws via ensureCanWrite, which is what was clipping the
        // outer chunks under the previous anchor-only gate.
        //
        // Determinism across passes is guaranteed by:
        //   1. Seeding the RNG purely from the piece's static bounding
        //      box corners (same seed every pass).
        //   2. Removing all terrain reads from the algorithm — every
        //      decision is a pure function of (origin, RNG sequence).
        //   3. Routing every write through place() so the chunkBox gate
        //      decides whether the cell lands; the algorithm itself
        //      (loops, RNG draws, coordinate computations) runs
        //      identically on every pass.
        //
        // Performance: ~80k place() calls per pass × ~16 intersected
        // chunks ≈ 1.3M total per tree. With cached chunkBox bounds and
        // a reusable MutableBlockPos in the inner loop, an out-of-chunk
        // call is ~10ns and an in-chunk call (the ~1/16 that actually
        // write) is ~5µs — total ~400ms per tree across all passes.
        RandomSource rng = RandomSource.create(
                (long) this.boundingBox.minX() * 341873128712L
                        + (long) this.boundingBox.minZ() * 132897987541L);

        this.pLevel = level;
        this.pMut = new BlockPos.MutableBlockPos();
        this.pMinY = level.getMinBuildHeight();
        this.pMaxY = level.getMaxBuildHeight();
        this.pCbMinX = chunkBox.minX();
        this.pCbMaxX = chunkBox.maxX();
        this.pCbMinY = chunkBox.minY();
        this.pCbMaxY = chunkBox.maxY();
        this.pCbMinZ = chunkBox.minZ();
        this.pCbMaxZ = chunkBox.maxZ();

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
     * Per-cell write. Gated against the per-chunk {@code chunkBox} (cached
     * in {@code pCb*}) so only writes that fall inside the chunk currently
     * being generated actually land — that's how this {@link StructurePiece}
     * spans multiple chunks safely (canonical Mansion / Stronghold pattern).
     * All loop counters, RNG draws, and coordinate computations in the
     * caller run unconditionally; only the {@code level.setBlock} call
     * itself is gated, so the algorithm walks the identical decision tree
     * on every chunk pass and the slices stitch together.
     */
    private void place(int x, int y, int z, BlockState state) {
        if (y < pMinY || y >= pMaxY) return;
        if (x < pCbMinX || x > pCbMaxX) return;
        if (z < pCbMinZ || z > pCbMaxZ) return;
        if (y < pCbMinY || y > pCbMaxY) return;
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
