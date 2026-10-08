package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModDimensionKeys;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.structure.UtopiaTreeStructure;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Magic Apple Tree &mdash; <b>Audit Part 1 new feature</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code danger.orespawn.ItemAppleSeed.makeTree(world, x, y, z,
 * MyAppleLeaves, chunk)} (1.7.10 source, lines 46&ndash;123).</p>
 *
 * <p><b>Legacy geometry (verified ItemAppleSeed.java:46-123):</b></p>
 * <ol>
 *   <li>{@code h1=12, h2=6, h3=9, h4=6, h5=14, w1=5, w2=3} for the
 *       Apple variant (line 52&ndash;58).</li>
 *   <li>Vertical {@code OAK_LOG} trunk from y+1 to y+h1 (line 77).</li>
 *   <li>4 cardinal {@code OAK_LOG} arms at y+h2, length w1 (line
 *       80&ndash;91).</li>
 *   <li>4 shorter cardinal {@code OAK_LOG} arms at y+h3, length w2
 *       (line 92&ndash;103).</li>
 *   <li>Stacked square leaf disks at y+h4..y+h5; width starts at 6,
 *       drops to 5 above i=8, and to 4 above i=10 (line 104&ndash;121).
 *       Only fills air positions.</li>
 * </ol>
 *
 * <p><b>Where the trees go (WGEN-077; OreSpawnWorld.java:1792-1828,
 * addAppleTrees):</b> the feature runs once per chunk and makes the
 * original's roll itself ({@link UtopiaTreeStructure#appleTrees}): the
 * gate {@code nextInt(15 + freq)} with {@code freq = (|cx| + |cz|) % 15},
 * {@code 2 + nextInt(2 + (15 - freq) / 2)} trees at {@code 2 + nextInt(12)}
 * into the chunk, each on the grass the scan finds. In Utopia that roll is
 * the second of the chunk pass ({@link UtopiaTreeStructure#chunkPass}):
 * after the huge roll, on the same random, and only when the huge roll grew
 * nothing (:42-43); the grove then stays out of a chunk that grew an apple
 * tree, as {@code !addAppleTrees(...) && !addOtherTrees(...)} (:43) kept
 * it. The Village runs addAppleTrees on its own (:119).</p>
 *
 * <p>The chunk's trees bear the fruit its roll drew ({@code which}: under 8 apple, 8 cherry, 9 peach), each with
 * its own leaves and size as ItemAppleSeed.makeTree grows them (UtopiaTreeStructure.Fruit).</p>
 */
public class MagicAppleTreeFeature extends Feature<NoneFeatureConfiguration> {
    public MagicAppleTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        // WGEN-075: addAppleTrees never ran in a chunk that grew a huge tree (orig OreSpawnWorld.java:42-43).
        if (UtopiaTreeStructure.bigTreeRootedAt(level, ctx.origin())) return false;
        ChunkPos chunk = new ChunkPos(ctx.origin());
        UtopiaTreeStructure.ColumnProbe probe = UtopiaTreeStructure.probe(ctx.chunkGenerator(), level,
                level.getLevel().getChunkSource().randomState());
        List<BlockPos> trees;
        UtopiaTreeStructure.Fruit fruit;
        if (level.getLevel().dimension() == ModDimensionKeys.UTOPIA) {
            UtopiaTreeStructure.ChunkPass pass = UtopiaTreeStructure.chunkPass(level.getSeed(), chunk, probe);
            trees = pass.appleTrees();
            fruit = pass.fruit();
        } else {
            // WGEN-108: the Village's orchard on the random the original drew it from, after its mosquitos and ants
            UtopiaTreeStructure.Orchard orchard = UtopiaTreeStructure.orchard(
                    UtopiaTreeStructure.villageRandom(level.getSeed(), chunk, probe), chunk, probe);
            trees = orchard.bases();
            fruit = orchard.fruit();
        }
        boolean grew = false;
        for (BlockPos base : trees) {
            BlockPos grass = scan(level, base.getX(), base.getZ());
            if (grass != null) grew |= growTree(level, grass, GenerationRange.top(ctx.chunkGenerator(), level), fruit);
        }
        return grew;
    }

    /**
     * orig OreSpawnWorld.java:1811-1812, the scan in the world as it stands: from Y100 down through air only, the tree on
     * the first block below the air when that block is grass; anything else first (a plant on the grass, a crop, an
     * earlier tree's crown or trunk) ends the scan and the tree does not grow. GitHub #6: the trees of an orchard no
     * longer grow inside each other's crowns.
     */
    private static BlockPos scan(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 100; y > 50; --y) {
            if (!level.getBlockState(pos.set(x, y, z)).isAir()) return null;
            if (level.getBlockState(pos.set(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) return new BlockPos(x, y - 1, z);
        }
        return null;
    }

    /** ItemAppleSeed.makeTree (orig :46-123) on the grass block at {@code base}; false when the ground or trunk refuses. */
    private static boolean growTree(WorldGenLevel level, BlockPos base, int top, UtopiaTreeStructure.Fruit fruit) {
        BlockState ground = level.getBlockState(base);
        if (!(ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.FARMLAND))) {
            return false;
        }

        int x = base.getX();
        int y = base.getY(); // Legacy uses (posY - 1), the grass block, as the trunk base.
        int z = base.getZ();

        // orig :52-75: the fruit's dimensions
        final int h1 = fruit.h1, h2 = fruit.h2, h3 = fruit.h3, h4 = fruit.h4, h5 = fruit.h5, w1 = fruit.w1, w2 = fruit.w2;

        if (y + h5 + 2 >= top) return false;

        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        // QA fix: Apple Leaves block extends LeavesBlock so the engine
        // assigns DISTANCE=7/PERSISTENT=false on a bare defaultBlockState(),
        // which decays the entire canopy on the first random tick. Pin
        // PERSISTENT=true and DISTANCE=1 so worldgen-placed leaves never
        // decay regardless of trunk recompute distance.
        BlockState leaves = (fruit == UtopiaTreeStructure.Fruit.CHERRY ? ModBlocks.CHERRY_LEAVES
                : fruit == UtopiaTreeStructure.Fruit.PEACH ? ModBlocks.PEACH_LEAVES : ModBlocks.APPLE_LEAVES).get()
                .defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);

        // QA fix: don't punch through Royal Tree structures or other
        // already-placed worldgen content sharing the Utopia biome.
        BlockPos trunkBase = new BlockPos(x, y + 1, z);
        if (!isReplaceable(level, trunkBase)) return false;

        // Trunk pillar (legacy line 77-79).
        for (int j = 1; j < h1; j++) {
            BlockPos pos = new BlockPos(x, y + j, z);
            if (isReplaceable(level, pos)) level.setBlock(pos, log, 2);
        }
        // 4 cardinal arms at y+h2 (legacy lines 80-91).
        for (int j = 1; j < w1; j++) {
            placeIfReplaceable(level, new BlockPos(x + j, y + h2, z), log);
            placeIfReplaceable(level, new BlockPos(x - j, y + h2, z), log);
            placeIfReplaceable(level, new BlockPos(x, y + h2, z + j), log);
            placeIfReplaceable(level, new BlockPos(x, y + h2, z - j), log);
        }
        // 4 cardinal arms at y+h3 (legacy lines 92-103).
        for (int j = 1; j < w2; j++) {
            placeIfReplaceable(level, new BlockPos(x + j, y + h3, z), log);
            placeIfReplaceable(level, new BlockPos(x - j, y + h3, z), log);
            placeIfReplaceable(level, new BlockPos(x, y + h3, z + j), log);
            placeIfReplaceable(level, new BlockPos(x, y + h3, z - j), log);
        }
        // Stacked leaf disks (legacy lines 104-121).
        for (int i = h4; i < h5; i++) {
            int width = fruit.discWidth(i); // a cherry's and a peach's one block narrower (orig :112-114)
            for (int j = -width; j <= width; j++) {
                for (int k = -width; k <= width; k++) {
                    BlockPos pos = new BlockPos(x + k, y + i, z + j);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, leaves, 2);
                    }
                }
            }
        }
        return true;
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.canBeReplaced() || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN);
    }

    private static void placeIfReplaceable(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (isReplaceable(level, pos)) level.setBlock(pos, state, 2);
    }
}
