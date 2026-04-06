package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Temporary dirt block left by moles. Disappears on random tick and slows entities.
 */
public class MoleDirtBlock extends Block {
    /** Horizontal movement multiplier while inside this block (sticky mud). */
    private static final double HORIZONTAL_DRAG_FACTOR = 0.3;

    public MoleDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * HORIZONTAL_DRAG_FACTOR, motion.y, motion.z * HORIZONTAL_DRAG_FACTOR);
    }
}
