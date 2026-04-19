package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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
 * <p><b>Spawn frequency (verified OreSpawnWorld.java:1792-1828,
 * addAppleTrees):</b> {@code random.nextInt(15 + freq) != 0} gating
 * (where {@code freq = (|cx/16| + |cz/16|) % 15}); {@code which =
 * nextInt(10)} &rarr; {@code which < 8} picks Apple Leaves
 * (80% of triggers); attempts {@code 2 + nextInt(2 + (15-freq)/2)}
 * placements per chunk &rarr; net <b>~1 apple grove per 15 chunks
 * near spawn, scaling to ~1 per 30 chunks far from spawn</b>. We
 * approximate this with {@code spacing=6, separation=2}, which yields
 * one grove per ~36 chunks &mdash; the closest stable lattice that
 * doesn't oversaturate the overworld.</p>
 *
 * <p><b>Stand-in note:</b> the legacy variant tracking ({@code which
 * == 8} cherry, {@code which == 9} peach) is not modeled here; the
 * 1.21.1 mod ships only the Apple Leaves block, so all Magic Apple
 * Tree placements use Apple Leaves to stay loyal to the dominant
 * 80% legacy outcome.</p>
 */
public class MagicAppleTreeFeature extends Feature<NoneFeatureConfiguration> {
    public MagicAppleTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockState below = level.getBlockState(surface.below());
        if (!(below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.FARMLAND))) {
            return false;
        }

        int x = surface.getX();
        int y = surface.getY() - 1; // Legacy uses (posY - 1) as the trunk base.
        int z = surface.getZ();

        // Legacy Apple Leaves dimensions.
        final int h1 = 12, h2 = 6, h3 = 9, h4 = 6, h5 = 14, w1 = 5, w2 = 3;

        if (y + h5 + 2 >= level.getMaxBuildHeight()) return false;

        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        // QA fix: Apple Leaves block extends LeavesBlock so the engine
        // assigns DISTANCE=7/PERSISTENT=false on a bare defaultBlockState(),
        // which decays the entire canopy on the first random tick. Pin
        // PERSISTENT=true and DISTANCE=1 so worldgen-placed leaves never
        // decay regardless of trunk recompute distance.
        BlockState leaves = ModBlocks.APPLE_LEAVES.get().defaultBlockState()
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
            int width = 6;
            if (i > 8) width = 5;
            if (i > 10) width = 4;
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
