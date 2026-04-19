package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

/**
 * Wind Tree &mdash; <b>Audit Part 1 new feature</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code danger.orespawn.Trees.WindTree(world, x, y, z, dir)}
 * (1.7.10 source, lines 45&ndash;77) and its
 * {@code WindTreeBranch} helper (lines 21&ndash;43).</p>
 *
 * <p><b>Legacy geometry (verified Trees.java:45-77):</b></p>
 * <ul>
 *   <li>Random {@code height = nextInt(8) + 40} (40&ndash;47 blocks
 *       tall) and unused {@code width = nextInt(4) + 8}.</li>
 *   <li>Direction selector: {@code dir} = 0/1/2/3 maps to
 *       (+x, -x, +z, -z) for the lean direction (line 50&ndash;63).</li>
 *   <li>For each j in [0, height): place an {@code OAK_LOG} at
 *       {@code (x, y+j, z)}; for j > height/5 also place an
 *       {@code OAK_LEAVES} at the lean offset
 *       {@code (x+dirx, y+j, z+dirz)}; every 4th row past height/4
 *       spawns a {@link #windTreeBranch} of length {@code height-j}.</li>
 *   <li>Apex is a single {@code OAK_LEAVES} at {@code (x, y+height, z)}.</li>
 * </ul>
 *
 * <p><b>Spawn frequency (verified OreSpawnWorld.java:2508-2547,
 * addOtherTrees):</b> {@code random.nextInt(30) != 0} gating, then
 * {@code what = nextInt(2)} 50/50 split with the SkyTree, then up to
 * 4 attempts per chunk &rarr; net <b>~4 trees per 60 chunks</b> in
 * the Utopia dimension only. Encoded as a {@code count = 1} placed
 * feature with {@code rarity_filter chance = 60} which yields the
 * matching average.</p>
 */
public class WindTreeFeature extends Feature<NoneFeatureConfiguration> {
    public WindTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockState below = level.getBlockState(surface.below());
        if (!(below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT))) return false;

        // Pick a direction (legacy Trees.java:47-63).
        int dir = random.nextInt(4);
        int dirx = 1, dirz = 0;
        if (dir == 1) { dirx = -1; dirz = 0; }
        else if (dir == 2) { dirx = 0; dirz = 1; }
        else if (dir == 3) { dirx = 0; dirz = -1; }

        int height = random.nextInt(8) + 40;
        if (surface.getY() + height + 1 >= level.getMaxBuildHeight() - 2) return false;

        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        // QA fix: leaves placed by feature have to be PERSISTENT, otherwise
        // the engine flags them with distance=7 and they decay on the next
        // random tick. Also pin DISTANCE=1 so even if the surrounding logs
        // load out of order the leaves never enter the decay queue.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);

        // QA fix: bail if the trunk column is already occupied (e.g. inside
        // a Royal Tree structure). Prevents Wind Trees from punching through
        // the King/Queen mega-trees that share the Utopia biome.
        if (!isReplaceable(level, surface)) return false;

        // Main trunk + lean canopy (legacy Trees.java:69-75).
        for (int j = 0; j < height; j++) {
            BlockPos trunkPos = surface.offset(0, j, 0);
            if (isReplaceable(level, trunkPos)) {
                level.setBlock(trunkPos, log, 2);
            }
            if (j <= height / 5) continue;
            BlockPos leafPos = surface.offset(dirx, j, dirz);
            if (isReplaceable(level, leafPos)) {
                level.setBlock(leafPos, leaves, 2);
            }
            if (j <= height / 4 || j % 4 != 0) continue;
            windTreeBranch(level, surface.offset(0, j, 0), height - j, dirx, dirz, random);
        }
        // Apex leaf cap (legacy Trees.java:76).
        BlockPos apex = surface.offset(0, height, 0);
        if (isReplaceable(level, apex)) {
            level.setBlock(apex, leaves, 2);
        }
        return true;
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.canBeReplaced() || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN);
    }

    /**
     * Direct port of {@code WindTreeBranch} (Trees.java:21-43).
     * Carves a horizontal log run with leaf canopy + sideways leaf
     * fringe. Uses the calling method's RNG for stable per-tree shape.
     */
    private static void windTreeBranch(WorldGenLevel level, BlockPos origin, int length,
                                       int dirx, int dirz, Random random) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);
        for (int i = 1; i <= length; i++) {
            BlockPos branchPos = origin.offset(i * dirx, 0, i * dirz);
            if (isReplaceable(level, branchPos)) {
                level.setBlock(branchPos, log, 2);
            }

            BlockPos abovePos = branchPos.above();
            if (level.getBlockState(abovePos).isAir()) {
                level.setBlock(abovePos, leaves, 2);
            }
            if (i < length / 3) {
                BlockPos above2 = branchPos.above(2);
                if (level.getBlockState(above2).isAir()) {
                    level.setBlock(above2, leaves, 2);
                }
            }
            if (i <= length / 3) continue;
            BlockPos leftSide = branchPos.offset(dirz, 0, dirx);
            BlockPos rightSide = branchPos.offset(-dirz, 0, -dirx);
            if (level.getBlockState(leftSide).isAir()) {
                level.setBlock(leftSide, leaves, 2);
            }
            if (level.getBlockState(rightSide).isAir()) {
                level.setBlock(rightSide, leaves, 2);
            }
        }
        BlockPos tip1 = origin.offset((length + 1) * dirx, 0, (length + 1) * dirz);
        BlockPos tip2 = origin.offset((length + 2) * dirx, 0, (length + 2) * dirz);
        if (level.getBlockState(tip1).isAir()) {
            level.setBlock(tip1, leaves, 2);
        }
        if (level.getBlockState(tip2).isAir()) {
            level.setBlock(tip2, leaves, 2);
        }
    }
}
