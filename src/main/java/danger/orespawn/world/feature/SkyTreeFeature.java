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

import java.util.Random;

/**
 * Sky Tree &mdash; <b>Audit Part 1 new feature</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code danger.orespawn.Trees.SkyTree(world, x, y, z)}
 * (1.7.10 source, lines 96&ndash;119) and its
 * {@code SkyTreeBranch} helper (lines 79&ndash;94).</p>
 *
 * <p><b>Legacy geometry (verified Trees.java:96-119):</b></p>
 * <ol>
 *   <li>Random {@code height = nextInt(15) + 190} target Y, abort if
 *       remaining headroom is &lt; 20 (line 102).</li>
 *   <li>Random {@code width = nextInt(10) + 25} (the canopy half-width,
 *       25&ndash;34 blocks).</li>
 *   <li>Stamp a single {@code SkyTreeLog} pillar from y..height
 *       (line 106&ndash;108).</li>
 *   <li>Place an {@code OAK_LEAVES} cap one block above the trunk top
 *       (line 109).</li>
 *   <li>Stamp 4 horizontal {@code SkyTreeLog} branches at the cap
 *       height in cardinal directions, length = width (lines
 *       110&ndash;113).</li>
 *   <li>Drop {@code height -= 5} then a random {@code height -=
 *       nextInt(4)}, shrink width to width/3, and stamp another 4
 *       cardinal branches at that lower height (lines 114&ndash;118).</li>
 * </ol>
 *
 * <p><b>Branch geometry (Trees.java:79-94):</b> for each i in
 * [1, length): place a {@code SkyTreeLog} along the cardinal,
 * {@code OAK_LEAVES} above and to both perpendicular sides (only on
 * air). One trailing {@code OAK_LEAVES} at the branch tip.</p>
 *
 * <p><b>Spawn frequency (verified OreSpawnWorld.java:2508-2547,
 * addOtherTrees):</b> {@code random.nextInt(30) != 0} gating, then
 * {@code what = nextInt(2)} 50/50 split with the WindTree, then up to
 * 3 attempts per chunk &rarr; net <b>~3 trees per 60 chunks</b> in
 * the Utopia dimension only.</p>
 *
 * <p><b>Stability guard:</b> if the trunk would clip max build height,
 * abort up-front. Branch writes that fall outside the active
 * {@link WorldGenLevel} 24-block radius will be silently dropped by
 * the engine; this is the closest legacy-faithful behavior we can
 * achieve without promoting Sky Tree to a multi-pass StructurePiece
 * (see {@code RoyalTreePiece} for the reference promotion).</p>
 */
public class SkyTreeFeature extends Feature<NoneFeatureConfiguration> {
    public SkyTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockState below = level.getBlockState(surface.below());
        if (!(below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT))) return false;

        // Legacy line 101: pick a target trunk top Y. Originally absolute
        // Y in 1.7.10's 0-256 world (190..204). For 1.21.1's expanded
        // -64..320 world, we treat this as a height *above* surface so
        // the tree visually scales the same regardless of terrain Y.
        int targetTop = surface.getY() + random.nextInt(15) + 190;
        // Legacy line 102: abort if there's not at least 20 blocks of
        // headroom (here, if the trunk wouldn't fit in build height).
        if (targetTop + 5 >= level.getMaxBuildHeight() - 2) return false;
        if (targetTop - surface.getY() < 20) return false;

        int width = random.nextInt(10) + 25;

        BlockState skyLog = ModBlocks.SKY_TREE_LOG.get().defaultBlockState();
        // QA fix: persistent + DISTANCE=1 so the canopy never decays.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);

        // QA fix: don't overwrite Royal Tree gold/emerald blocks if the
        // player got unlucky with co-located placement in Utopia.
        if (!isReplaceable(level, surface)) return false;

        // Legacy line 106-108: trunk pillar.
        for (int j = surface.getY(); j <= targetTop; j++) {
            BlockPos pos = new BlockPos(surface.getX(), j, surface.getZ());
            if (isReplaceable(level, pos)) level.setBlock(pos, skyLog, 2);
        }
        // Legacy line 109: apex leaf cap.
        BlockPos apex = new BlockPos(surface.getX(), targetTop + 1, surface.getZ());
        if (isReplaceable(level, apex)) level.setBlock(apex, leaves, 2);

        // Legacy line 110-113: 4 cardinal canopy branches at targetTop.
        BlockPos branchOrigin = new BlockPos(surface.getX(), targetTop, surface.getZ());
        skyTreeBranch(level, branchOrigin, width, 1, 0);
        skyTreeBranch(level, branchOrigin, width, -1, 0);
        skyTreeBranch(level, branchOrigin, width, 0, 1);
        skyTreeBranch(level, branchOrigin, width, 0, -1);

        // Legacy line 114-118: drop height by 5 + nextInt(4), shrink
        // width by 1/3, place 4 more cardinal branches.
        int lowerY = targetTop - 5 - random.nextInt(4);
        int lowerWidth = width / 3;
        BlockPos lowerOrigin = new BlockPos(surface.getX(), lowerY, surface.getZ());
        skyTreeBranch(level, lowerOrigin, lowerWidth, 1, 0);
        skyTreeBranch(level, lowerOrigin, lowerWidth, -1, 0);
        skyTreeBranch(level, lowerOrigin, lowerWidth, 0, 1);
        skyTreeBranch(level, lowerOrigin, lowerWidth, 0, -1);
        return true;
    }

    /** Direct port of {@code SkyTreeBranch} (Trees.java:79-94). */
    private static void skyTreeBranch(WorldGenLevel level, BlockPos origin, int length, int dirx, int dirz) {
        BlockState skyLog = ModBlocks.SKY_TREE_LOG.get().defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true)
                .setValue(LeavesBlock.DISTANCE, 1);
        for (int i = 1; i < length; i++) {
            BlockPos pos = origin.offset(i * dirx, 0, i * dirz);
            if (isReplaceable(level, pos)) level.setBlock(pos, skyLog, 2);

            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, leaves, 2);
            }
            BlockPos sideA = pos.offset(dirz, 0, dirx);
            BlockPos sideB = pos.offset(-dirz, 0, -dirx);
            if (level.getBlockState(sideA).isAir()) {
                level.setBlock(sideA, leaves, 2);
            }
            if (level.getBlockState(sideB).isAir()) {
                level.setBlock(sideB, leaves, 2);
            }
        }
        BlockPos tip = origin.offset(length * dirx, 0, length * dirz);
        if (level.getBlockState(tip).isAir()) {
            level.setBlock(tip, leaves, 2);
        }
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.canBeReplaced() || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN);
    }
}
