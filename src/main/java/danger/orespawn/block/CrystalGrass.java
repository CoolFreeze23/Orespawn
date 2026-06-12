package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

/**
 * Crystal dimension grass block. Supports planting on top.
 * Extends {@link TransparentBlock} so light passes through and adjacent
 * faces between identical blocks are culled (no seam artifacts).
 */
public class CrystalGrass extends TransparentBlock {
    public CrystalGrass(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    /**
     * orig CrystalGrass.java:64-66 — {@code canSustainPlant} returns true
     * unconditionally, so every plant (crystal flowers, rice, quinoa,
     * vanilla crops, saplings...) may be planted on crystal grass (ITEM-006).
     */
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return TriState.TRUE;
    }
}
