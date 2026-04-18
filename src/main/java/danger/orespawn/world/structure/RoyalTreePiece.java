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
 * Goodness / Queen Tree). Replaces the generic {@code FeatureStructurePiece}
 * wrapper for these two structures specifically — the wrapper's "run a
 * configured feature once" model can't span more than one chunk because
 * {@link WorldGenLevel} (a {@code WorldGenRegion} under the hood) silently
 * bulldozes any {@code setBlock} that lands more than ~24 blocks from the
 * currently-generating chunk. The royal trees are 60+ blocks tall and have
 * branches that can reach ~30 blocks from the trunk, so the wrapper sheared
 * them off at chunk borders.
 *
 * <p><b>How this class fixes the clip:</b> the canonical Woodland-Mansion /
 * Stronghold pattern. We declare a massive static bounding box (the
 * "blueprint permit") covering the full possible footprint of the tree.
 * The vanilla chunk generator then calls {@link #postProcess} once for
 * <em>every</em> chunk that intersects the box — but inside {@code postProcess}
 * we receive a {@code chunkBox} parameter clipped to just the slice of the
 * blueprint that belongs to the chunk currently being generated. Every
 * single {@code level.setBlock} call is gated by
 * {@code chunkBox.isInside(targetPos)} so only the writes that fall in the
 * current chunk's slice actually land. Multiple passes stitch together
 * the full structure chunk-by-chunk, identical to how the vanilla mansion's
 * piece system works.</p>
 *
 * <p><b>Why this works without races:</b> the {@link RandomSource} we
 * compute inside {@code postProcess} is seeded purely from the piece's
 * static bounding-box coordinates (the user-supplied
 * {@code minX * 341873128712L + minZ * 132897987541L} hash), so every pass
 * walks the same algorithmic path with the same per-cell decisions. The
 * algorithm itself is also fully deterministic — terrain reads from the
 * legacy {@code isBoringBlock} / {@code isBoringBaseBlock} were intentionally
 * dropped and replaced with always-true. Reads on neighbouring chunks
 * during {@code structure_starts} can return inconsistent state across
 * passes (some chunks at vanilla-terrain step, others mid-feature) so a
 * read-conditional algorithm would tear at chunk borders. The bulldoze
 * trade-off is fine: trees on Utopia plains overwrite grass / dirt only,
 * and Mansions / Strongholds use the same "always write" rule.</p>
 *
 * <p><b>Geometry preservation:</b> the {@code MakeBigSquareTree} +
 * {@code make_branch} algorithm ported from
 * {@code reference_1_7_10_source/sources/danger/orespawn/ItemMagicApple.java}
 * lines 248-469 + 133-246 is preserved verbatim — same loops, same RNG draws
 * in the same order, same per-cell block selections, same rare gem-leaf
 * substitution chances, same apex emerald-pair + spawner crown. The only
 * deltas vs. the pre-fix {@code RoyalTreeFeature} (deleted in this commit)
 * are: terrain reads removed (always-write for chunk safety + determinism),
 * all writes routed through the chunkBox-gated {@link #place} helper, and
 * RNG seeded from the piece origin instead of the feature context.</p>
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

    /** Bounding-box "permit" extents. Horizontal ±32 covers the 21-block trunk
     *  radius plus the bulk of branch + leaf reach (the rare longest branch tip
     *  beyond ±32 is naturally clipped — acceptable, the canopy silhouette is
     *  what reads at distance). Vertical −12 below origin covers the foundation
     *  walks; +80 above covers a generous tower height (legacy max ~50). */
    private static final int H_EXTENT = 32;
    private static final int DOWN_EXTENT = 12;
    private static final int UP_EXTENT = 80;

    private final BlockPos origin;
    private final boolean queenVariant;

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
        // Deterministic per-piece RNG. Seeded from the piece's static bounding
        // box corners using the canonical large-prime hash (the user-supplied
        // formula). Every postProcess pass — for every intersected chunk —
        // produces the IDENTICAL random sequence, so the algorithm walks the
        // identical decision tree on every pass. Without this we'd get a
        // different tree shape per chunk-pass and the structure would tear.
        RandomSource rng = RandomSource.create(
                (long) this.boundingBox.minX() * 341873128712L
                        + (long) this.boundingBox.minZ() * 132897987541L);

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

        makeBigSquareTree(level, chunkBox, rng,
                origin.getX(), origin.getY(), origin.getZ(),
                trunk, leaves, steps, spawner);
    }

    /**
     * Chunk-safe wrapper around {@code level.setBlock}. The
     * {@code chunkBox.isInside(pos)} check is the single most important line
     * in this class — it's the chunk-by-chunk write gate that makes the
     * multi-chunk tree possible. Writes outside the current generating
     * chunk are silently dropped (they'll be picked up on a later
     * {@code postProcess} pass when their owning chunk becomes the
     * "current" one). World-bound check first to avoid out-of-world writes.
     */
    private void place(WorldGenLevel level, BoundingBox chunkBox,
                       int x, int y, int z, BlockState state) {
        if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) return;
        BlockPos pos = new BlockPos(x, y, z);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, FLAG_CLIENTS_ONLY);
        }
    }

    /**
     * Byte-for-byte port of the legacy
     * {@code ItemMagicApple#MakeBigSquareTree} (lines 248-469). Loops, RNG
     * draws, and per-cell block selections are preserved verbatim. The only
     * deviations from the legacy:
     * <ul>
     *   <li>{@code isBoringBlock} / {@code isBoringBaseBlock} terrain checks
     *       removed — algorithm always writes (chunk-safe + deterministic).</li>
     *   <li>Foundation pillar walk uses a fixed {@link #BASE_DEPTH} instead
     *       of the legacy "walk down until non-stone".</li>
     *   <li>Live boss spawn at apex replaced with a placed spawner block on
     *       top of the canonical 2x emerald crown (matches our shrine pattern).</li>
     *   <li>Every {@code setBlock} routed through {@link #place} for the
     *       chunkBox gate.</li>
     * </ul>
     */
    private void makeBigSquareTree(WorldGenLevel world, BoundingBox chunkBox, RandomSource rand,
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
        // Legacy block6 (line 261-305): walk down up to 20 blocks on each
        // mid-side column, replacing each replaceable cell with trunk. We
        // walk a fixed BASE_DEPTH (6) since terrain reads are gone.
        for (int i = -t_radius; i <= t_radius; ++i) {
            for (int j = 0; j < BASE_DEPTH; ++j) {
                if (y - j <= world.getMinBuildHeight()) break;
                place(world, chunkBox, x + i, y - j, z - t_radius, trunkID);
                place(world, chunkBox, x + i, y - j, z + t_radius, trunkID);
                place(world, chunkBox, x - t_radius, y - j, z + i, trunkID);
                place(world, chunkBox, x + t_radius, y - j, z + i, trunkID);
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
                    place(world, chunkBox, x + i, current_y, z - this_width, trunkID);
                    place(world, chunkBox, x + i, current_y, z + this_width, trunkID);
                    place(world, chunkBox, x - this_width, current_y, z + i, trunkID);
                    place(world, chunkBox, x + this_width, current_y, z + i, trunkID);
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
                        place(world, chunkBox, x - spiral, current_y, z - this_width - 1, stepID);
                        place(world, chunkBox, x + spiral, current_y, z + this_width + 1, stepID);
                        place(world, chunkBox, x - this_width - 1, current_y, z + spiral, stepID);
                        place(world, chunkBox, x + this_width + 1, current_y, z - spiral, stepID);
                        if (this_width >= 3) {
                            place(world, chunkBox, x - spiral, current_y, z - this_width - 2, stepID);
                            place(world, chunkBox, x + spiral, current_y, z + this_width + 2, stepID);
                            place(world, chunkBox, x - this_width - 2, current_y, z + spiral, stepID);
                            place(world, chunkBox, x + this_width + 2, current_y, z - spiral, stepID);
                        }
                        if (platform_looper == 1) continue;
                        ++spiral;
                    }
                    if (do_floor) {
                        for (int m = -this_width; m <= this_width; ++m) {
                            for (int n = -this_width; n <= this_width; ++n) {
                                place(world, chunkBox, x + m, current_y, z + n, trunkID);
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
                        case 0 -> makeBranch(world, chunkBox, rand, x + this_width, current_y, z,
                                this_width, 1, 0, trunkID, leafID);
                        case 1 -> makeBranch(world, chunkBox, rand, x - this_width, current_y, z,
                                this_width, -1, 0, trunkID, leafID);
                        case 2 -> makeBranch(world, chunkBox, rand, x, current_y, z + this_width,
                                this_width, 0, 1, trunkID, leafID);
                        case 3 -> makeBranch(world, chunkBox, rand, x, current_y, z - this_width,
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
        // 2x emerald block crown + spawner cap on top.
        BlockState emeraldCrown = Blocks.EMERALD_BLOCK.defaultBlockState();
        place(world, chunkBox, x, current_y, z, emeraldCrown);
        place(world, chunkBox, x, current_y + 1, z, emeraldCrown);
        place(world, chunkBox, x, current_y + 2, z, spawnerCap);
    }

    /**
     * Byte-for-byte port of the legacy {@code ItemMagicApple#make_branch}
     * (lines 133-246). Same control flow, same RNG draws in the same order.
     * Terrain checks dropped, all writes routed through {@link #place} for
     * the chunkBox gate. Vines, mid-branch chests, and the legacy Cave-Spider
     * drop are also dropped (modern worldgen handles loot via piece chests
     * and a worldgen-spawned Cave Spider would be a gameplay regression).
     */
    private void makeBranch(WorldGenLevel world, BoundingBox chunkBox, RandomSource rand,
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
                    place(world, chunkBox, realx, y, realz, trunkID);
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
                            place(world, chunkBox, realx, y + n, realz, localLeaf);
                        }
                    }
                }

                // Recursive perpendicular sub-branches (legacy line 229-239)
                if (current_width > 0 && last_branch > current_width
                        && current_width != this_width
                        && rand.nextInt(current_width + 1) == 0) {
                    int subdirx = branch_side;
                    int subdirz = 0;
                    if (dirx != 0) {
                        subdirx = 0;
                        subdirz = branch_side;
                    }
                    makeBranch(world, chunkBox, rand,
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
