package danger.orespawn.world;

import danger.orespawn.block.BlockCrystalLeaves;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Predicate;

/**
 * The sky light a cell has while 1.7.10 decorates a chunk, for the plant rules that read it there: a new chunk's light
 * is its columns' (Chunk.generateSkylightMap), 15 at the top and less on the way down by each block's opacity (leaves 1,
 * water and ice 3, a full block all of it), and once it is under 15 by one for each clear block too, the cell's own
 * included; a block the decoration sets lights its own column again. A 1.21 chunk has no light yet when its features
 * run, so the plant rules that read it use this.
 */
public final class LegacyLight {
    private LegacyLight() {
    }

    /**
     * Whether 1.7.10 had the block for leaves: vanilla's, and OreSpawn's own (the apple, cherry, peach, scary,
     * experience and crystal leaves were all BlockLeaves there, and are outside {@code #minecraft:leaves} here).
     */
    public static boolean isLeaves(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.getBlock() instanceof LeavesBlock
                || state.getBlock() instanceof BlockCrystalLeaves;
    }

    /** 1.7.10's light opacity of a block: leaves 1, water and ice 3, an occluding block 15, the rest 0. */
    public static int opacity(BlockState state) {
        if (state.isAir()) return 0;
        if (isLeaves(state)) return 1;
        if (state.getFluidState().is(FluidTags.WATER) || state.is(Blocks.ICE)) return 3;
        return state.canOcclude() ? 15 : 0;
    }

    /** The column's sky light at x, y, z as Chunk.generateSkylightMap leaves it, at least 0. */
    public static int sky(WorldGenLevel level, int x, int y, int z) {
        return sky(level, x, y, z, state -> false);
    }

    /** {@link #sky} with the blocks {@code unseen} taken for air, as blocks set after the light was made. */
    public static int sky(WorldGenLevel level, int x, int y, int z, Predicate<BlockState> unseen) {
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int light = 15;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, 0, z);
        for (int cell = top - 1; cell >= y; cell--) {
            BlockState state = level.getBlockState(at.setY(cell));
            int opacity = unseen.test(state) ? 0 : opacity(state);
            if (opacity == 0 && light != 15) opacity = 1;
            light -= opacity;
            if (light <= 0) return 0;
        }
        return light;
    }

    /**
     * The column's height as 1.7.10's height map has it (Chunk.generateSkylightMap): the first cell over the highest
     * block of non-zero opacity, the blocks {@code unseen} passed over; the level's bottom when there is none.
     */
    public static int height(WorldGenLevel level, int x, int z, Predicate<BlockState> unseen) {
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, 0, z);
        for (int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1; y >= level.getMinBuildHeight(); y--) {
            BlockState state = level.getBlockState(at.setY(y));
            if (opacity(state) > 0 && !unseen.test(state)) return y + 1;
        }
        return level.getMinBuildHeight();
    }
}
