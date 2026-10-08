package danger.orespawn.world;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The sky light a cell has while 1.7.10 decorates a chunk, for the plant rules that read it there: a new chunk's light
 * is its columns' (Chunk.generateSkylightMap), 15 at the top, less by each block's opacity on the way down (leaves 1,
 * water and ice 3, a full block all of it), and a block the decoration sets lights its own column again. A 1.21 chunk has
 * no light yet when its features run, so the plant rules that read it use this.
 */
public final class LegacyLight {
    private LegacyLight() {
    }

    /** 1.7.10's light opacity of a block: leaves 1, water and ice 3, an occluding block 15, the rest 0. */
    public static int opacity(BlockState state) {
        if (state.isAir()) return 0;
        if (state.is(BlockTags.LEAVES)) return 1;
        if (state.getFluidState().is(FluidTags.WATER) || state.is(Blocks.ICE)) return 3;
        return state.canOcclude() ? 15 : 0;
    }

    /** The column's sky light at x, y, z: 15 less the opacities of the blocks above it, at least 0. */
    public static int sky(WorldGenLevel level, int x, int y, int z) {
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        int light = 15;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos(x, 0, z);
        for (int above = top - 1; above > y && light > 0; above--) {
            light -= opacity(level.getBlockState(at.setY(above)));
        }
        return Math.max(light, 0);
    }
}
