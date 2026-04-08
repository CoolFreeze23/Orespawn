package danger.orespawn.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A fully see-through crystal block (like stained glass).
 * Extends {@link TransparentBlock} which:
 *  - Propagates skylight downward (no shadow underneath)
 *  - Culls adjacent faces between identical blocks (no edge seams)
 *  - Returns 0 from getLightBlock (light passes through)
 */
public class TransparentCrystalBlock extends TransparentBlock {

    public TransparentCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }
}
