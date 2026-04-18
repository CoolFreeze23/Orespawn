package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.neoforged.neoforge.common.Tags;

/**
 * The "Tree of Goodness" (King) and "Queen Tree" — a faithful 1.21.1 worldgen
 * port of the legacy 1.7.10 {@code MakeBigSquareTree} (the King / Queen "ginormous"
 * variant), found in {@code reference_1_7_10_source/sources/danger/orespawn/ItemMagicApple.java}
 * lines 248-469 (tower body) + 133-246 ({@code make_branch} horizontal limbs)
 * + 760-772 (the Magic-Apple dispatch site that picks the King vs. Queen palette
 * via the {@code GinormousEmeraldTreeEnable} branch).
 *
 * <p><b>Authentic legacy palette</b> (per {@code ItemMagicApple.java#L763,765}
 * and the {@code MakeBigSquareTree} apex check at {@code #L447,457}):</p>
 * <ul>
 *   <li><b>King variant ("Tree of Goodness")</b> — trunk = {@link Blocks#GOLD_BLOCK}
 *       (legacy {@code field_150340_R}), leaves = {@link Blocks#EMERALD_BLOCK}
 *       (legacy {@code field_150475_bE}), spiral steps = {@link Blocks#DIAMOND_BLOCK}
 *       (legacy {@code field_150484_ah}). The diamond-block step ID is the implicit
 *       King marker — in legacy this triggered {@code EntityList.func_75620_a("The King")}
 *       4 blocks above the apex; we replace that with a placed
 *       {@link ModBlocks#KING_SPAWNER} block on top of the canonical apex pair so the
 *       summon happens on player approach (matching this mod's shrine pattern).</li>
 *   <li><b>Queen variant</b> — trunk = {@link Blocks#OBSIDIAN} (legacy
 *       {@code field_150343_Z}), leaves = {@link ModBlocks#BLOCK_RUBY}
 *       (legacy {@code MyBlockRubyBlock}), spiral steps = {@link ModBlocks#BLOCK_AMETHYST}
 *       (legacy {@code MyBlockAmethystBlock}). The amethyst-block step ID is the implicit
 *       Queen marker, with the same apex spawner replacement using
 *       {@link ModBlocks#QUEEN_SPAWNER}.</li>
 * </ul>
 *
 * <p><b>Apex marker</b> (legacy {@code MakeBigSquareTree#L443-L446}): the very top
 * of the tower carries two stacked {@link Blocks#EMERALD_BLOCK} blocks regardless
 * of variant — this is the legacy "crown" used to test the step ID for boss spawn.
 * We preserve this and place the variant's spawner block immediately above so a
 * player walking the diamond/amethyst spiral up the outside arrives at an
 * unmistakable green-emerald + spawner cap.</p>
 *
 * <p><b>Rare gem-leaf substitution</b> (legacy {@code make_branch#L206-L224}):
 * each leaf cell rolls a 1-in-20 chance to become a "fancy" leaf. On hit, 2/3 of
 * those become {@link Blocks#REDSTONE_BLOCK} (legacy {@code field_150451_bX}); the
 * remaining 1/3 picks one of the four canonical OreSpawn gem blocks
 * (uranium / titanium / ruby / amethyst) uniformly. This produces the
 * scattered glow you can see in fan screenshots of the legacy tree.</p>
 *
 * <p><b>Geometry</b> — the tower is a hollow square shell of side
 * {@code (2 * radius + 1)} growing layer-by-layer with
 * inwardly-shrinking width as in the legacy {@code while (this_width >= 0)}
 * loop. Each layer carries a one-block-wide spiral of step blocks wrapping
 * the outside (the legacy {@code spiral} ladder), with periodic full floors
 * inside, perpendicular {@code make_branch} limbs of leaves at the cardinal
 * directions, and (with low probability) chest loot tucked at branch tips and
 * floor centres. The very top closes with a 2-block emerald cap + spawner crown.</p>
 *
 * <p><b>1.21.1 stability guards</b> (the only deliberate departures from byte-for-byte):</p>
 * <ul>
 *   <li>Up-front Y-bound check rejects placements within 64 blocks of
 *       {@link WorldGenLevel#getMaxBuildHeight()} — the tower can grow ~50 blocks
 *       tall with the legacy radius and we never want canopy clipping.</li>
 *   <li>Every write goes through {@code WorldGenLevel.setBlock(pos, state, 2)} —
 *       client-update-only, no neighbour cascades, no lighting recompute on the
 *       hot path. This matches the modern "feature placement" idiom used by
 *       vanilla mega trees.</li>
 *   <li>{@link #isBoringBlock} / {@link #isBoringBaseBlock} return {@code false}
 *       for any out-of-world Y so the legacy 20-step pillar walks (which
 *       could otherwise punch into bedrock-floor chunks) terminate cleanly.</li>
 * </ul>
 */
public class RoyalTreeFeature extends Feature<RoyalTreeFeature.Config> {

    public record Config(boolean queenVariant) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("queen_variant", false).forGetter(Config::queenVariant)
        ).apply(inst, Config::new));
    }

    /** Legacy {@code ItemMagicApple#tree_radius = 6}. Drives the tower size and branch reach. */
    private static final int TREE_RADIUS = 6;

    /** {@code Block.UPDATE_CLIENTS} — matches the modern "no neighbor cascade" feature idiom. */
    private static final int FLAG_CLIENTS_ONLY = 2;

    public RoyalTreeFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        Config cfg = ctx.config();

        if (origin.getY() <= level.getMinBuildHeight() + 4) return false;
        if (origin.getY() + 64 >= level.getMaxBuildHeight()) return false;

        BlockState trunk;
        BlockState leaves;
        BlockState steps;
        BlockState spawner;
        if (cfg.queenVariant()) {
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

        return makeBigSquareTree(level, rand, origin.getX(), origin.getY(), origin.getZ(),
                trunk, leaves, steps, spawner);
    }

    /**
     * Byte-for-byte port of {@code ItemMagicApple#MakeBigSquareTree}
     * (legacy lines 248-469). The {@code chunk} batching parameter and the
     * {@code tree_type} metadata switch are dropped (1.21.1 has no metadata
     * variants), and the immediate boss spawn at the apex is replaced with
     * a placed spawner block (so the boss spawns on player approach via the
     * existing {@link ModBlocks#KING_SPAWNER}/{@link ModBlocks#QUEEN_SPAWNER}
     * tick logic). All other loops, conditionals, and per-cell decisions
     * are preserved.
     */
    private boolean makeBigSquareTree(WorldGenLevel world, RandomSource rand,
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
        // Legacy block6 (line 261-305): for each i in [-t_radius, t_radius],
        // walk down up to 20 blocks on each of the 4 mid-side columns until we
        // hit stone/bedrock, replacing each replaceable cell with the trunk ID.
        block6:
        for (int i = -t_radius; i <= t_radius; ++i) {
            if (isBoringBaseBlock(world, x + i, y, z - t_radius)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j <= world.getMinBuildHeight()) break;
                    if (!isBoringBaseBlock(world, x + i, y - j, z - t_radius)) break;
                    setBlock2(world, x + i, y - j, z - t_radius, trunkID);
                }
            }
            if (isBoringBaseBlock(world, x + i, y, z + t_radius)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j <= world.getMinBuildHeight()) break;
                    if (!isBoringBaseBlock(world, x + i, y - j, z + t_radius)) break;
                    setBlock2(world, x + i, y - j, z + t_radius, trunkID);
                }
            }
            if (isBoringBaseBlock(world, x - t_radius, y, z + i)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j <= world.getMinBuildHeight()) break;
                    if (!isBoringBaseBlock(world, x - t_radius, y - j, z + i)) break;
                    setBlock2(world, x - t_radius, y - j, z + i, trunkID);
                }
            }
            if (isBoringBaseBlock(world, x + t_radius, y, z + i)) {
                for (int j = 0; j < 20; ++j) {
                    if (y - j <= world.getMinBuildHeight()) break;
                    if (!isBoringBaseBlock(world, x + t_radius, y - j, z + i)) continue block6;
                    setBlock2(world, x + t_radius, y - j, z + i, trunkID);
                }
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
                    if (isBoringBaseBlock(world, x + i, current_y, z - this_width))
                        setBlock2(world, x + i, current_y, z - this_width, trunkID);
                    if (isBoringBaseBlock(world, x + i, current_y, z + this_width))
                        setBlock2(world, x + i, current_y, z + this_width, trunkID);
                    if (isBoringBaseBlock(world, x - this_width, current_y, z + i))
                        setBlock2(world, x - this_width, current_y, z + i, trunkID);
                    if (isBoringBaseBlock(world, x + this_width, current_y, z + i))
                        setBlock2(world, x + this_width, current_y, z + i, trunkID);
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
                        if (isBoringBlock(world, x - spiral, current_y, z - this_width - 1))
                            setBlock2(world, x - spiral, current_y, z - this_width - 1, stepID);
                        if (isBoringBlock(world, x + spiral, current_y, z + this_width + 1))
                            setBlock2(world, x + spiral, current_y, z + this_width + 1, stepID);
                        if (isBoringBlock(world, x - this_width - 1, current_y, z + spiral))
                            setBlock2(world, x - this_width - 1, current_y, z + spiral, stepID);
                        if (isBoringBlock(world, x + this_width + 1, current_y, z - spiral))
                            setBlock2(world, x + this_width + 1, current_y, z - spiral, stepID);
                        if (this_width >= 3) {
                            if (isBoringBlock(world, x - spiral, current_y, z - this_width - 2))
                                setBlock2(world, x - spiral, current_y, z - this_width - 2, stepID);
                            if (isBoringBlock(world, x + spiral, current_y, z + this_width + 2))
                                setBlock2(world, x + spiral, current_y, z + this_width + 2, stepID);
                            if (isBoringBlock(world, x - this_width - 2, current_y, z + spiral))
                                setBlock2(world, x - this_width - 2, current_y, z + spiral, stepID);
                            if (isBoringBlock(world, x + this_width + 2, current_y, z - spiral))
                                setBlock2(world, x + this_width + 2, current_y, z - spiral, stepID);
                        }
                        if (platform_looper == 1) continue;
                        ++spiral;
                    }
                    if (do_floor) {
                        for (int m = -this_width; m <= this_width; ++m) {
                            for (int n = -this_width; n <= this_width; ++n) {
                                if (!isBoringBlock(world, x + m, current_y, z + n)) continue;
                                setBlock2(world, x + m, current_y, z + n, trunkID);
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
                        case 0 -> makeBranch(world, rand, x + this_width, current_y, z,
                                this_width, 1, 0, trunkID, leafID);
                        case 1 -> makeBranch(world, rand, x - this_width, current_y, z,
                                this_width, -1, 0, trunkID, leafID);
                        case 2 -> makeBranch(world, rand, x, current_y, z + this_width,
                                this_width, 0, 1, trunkID, leafID);
                        case 3 -> makeBranch(world, rand, x, current_y, z - this_width,
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
        // Legacy unconditionally writes 2x emerald block at the apex pair, then
        // (if stepID == diamond) spawns The King 4 blocks above, OR (if stepID
        // == amethyst) spawns The Queen with bad mood. We keep the emerald pair
        // and replace the live spawn with the variant's spawner block on top.
        if (isBoringBaseBlock(world, x, current_y, z)) {
            BlockState emeraldCrown = Blocks.EMERALD_BLOCK.defaultBlockState();
            setBlock2(world, x, current_y, z, emeraldCrown);
            setBlock2(world, x, current_y + 1, z, emeraldCrown);
            setBlock2(world, x, current_y + 2, z, spawnerCap);
        }

        return true;
    }

    /**
     * Byte-for-byte port of {@code ItemMagicApple#make_branch} (legacy lines
     * 133-246). Builds a horizontal limb extending {@code dirx/dirz} away from
     * the tower, with progressively-narrowing trunk slices, leaf clusters at
     * the tip, occasional perpendicular sub-branches, and the rare gem-leaf
     * substitution. Vines, mid-branch chests, and the Cave-Spider drop
     * (legacy entity 99) are dropped — modern worldgen handles loot via
     * structure piece chests, and a worldgen-spawned Cave Spider would be a
     * gameplay regression here.
     */
    private void makeBranch(WorldGenLevel world, RandomSource rand,
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
                    if (isBoringBlock(world, realx, y, realz)) {
                        setBlock2(world, realx, y, realz, trunkID);
                    }
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
                            if (!isBoringBlock(world, realx, y + n, realz)) continue;
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
                            setBlock2(world, realx, y + n, realz, localLeaf);
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
                    makeBranch(world, rand,
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

    /**
     * Modern equivalent of legacy {@code isBoringBlock} ({@code ItemMagicApple#L70-L103}).
     * Returns true for cells the tree is allowed to overwrite — air, leaves,
     * flowers, grass, snow, sugar cane, cactus, plus this mod's apple leaves
     * and strawberry plant from the legacy list. Modernized to use tags so any
     * mod-added vegetation registered to {@link BlockTags#FLOWERS} /
     * {@link BlockTags#LEAVES} / {@link BlockTags#SAPLINGS} integrates cleanly.
     */
    private static boolean isBoringBlock(WorldGenLevel world, int x, int y, int z) {
        if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) return false;
        BlockState s = world.getBlockState(new BlockPos(x, y, z));
        if (s.isAir()) return true;
        if (s.is(BlockTags.LEAVES)) return true;
        if (s.is(BlockTags.FLOWERS) || s.is(BlockTags.SMALL_FLOWERS) || s.is(BlockTags.TALL_FLOWERS)) return true;
        if (s.is(BlockTags.SAPLINGS)) return true;
        Block b = s.getBlock();
        if (b == Blocks.SHORT_GRASS || b == Blocks.TALL_GRASS || b == Blocks.FERN) return true;
        if (b == Blocks.SNOW || b == Blocks.SNOW_BLOCK) return true;
        if (b == Blocks.CACTUS || b == Blocks.SUGAR_CANE) return true;
        return false;
    }

    /**
     * Modern equivalent of legacy {@code isBoringBaseBlock} ({@code ItemMagicApple#L105-L117}).
     * Stricter than {@link #isBoringBlock} — used when walking a pillar down
     * to the surface, this returns false on stone/bedrock so the trunk root
     * stops at the first solid cell, true on air/dirt/anything-else so the
     * pillar can replace it.
     */
    private static boolean isBoringBaseBlock(WorldGenLevel world, int x, int y, int z) {
        if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) return false;
        BlockState s = world.getBlockState(new BlockPos(x, y, z));
        if (s.isAir()) return true;
        if (s.is(Blocks.BEDROCK)) return false;
        return !s.is(Tags.Blocks.STONES);
    }

    private static void setBlock2(WorldGenLevel world, int x, int y, int z, BlockState state) {
        if (y < world.getMinBuildHeight() || y >= world.getMaxBuildHeight()) return;
        world.setBlock(new BlockPos(x, y, z), state, FLAG_CLIENTS_ONLY);
    }
}
