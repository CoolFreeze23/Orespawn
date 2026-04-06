package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCrystalTorch extends TorchBlock {
    private static final int ANIMATE_TICK_ROLL_BOUND = 4;
    private static final int ANIMATE_SUCCESS_INDEX = 1;
    private static final double FLAME_CENTER_XZ = 0.5;
    private static final double FLAME_CENTER_Y = 0.7;
    private static final double FIREWORK_VELOCITY_DIVISOR = 8.0;
    private static final double FLAME_HORIZONTAL_VELOCITY_DIVISOR = 60.0;
    private static final double FLAME_VERTICAL_VELOCITY_DIVISOR = 10.0;

    public BlockCrystalTorch(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FIREWORK, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != ANIMATE_SUCCESS_INDEX) return;

        double x = pos.getX() + FLAME_CENTER_XZ;
        double y = pos.getY() + FLAME_CENTER_Y;
        double z = pos.getZ() + FLAME_CENTER_XZ;

        level.addParticle(ParticleTypes.FIREWORK, x, y, z,
                (random.nextFloat() - random.nextFloat()) / FIREWORK_VELOCITY_DIVISOR,
                random.nextFloat() / FIREWORK_VELOCITY_DIVISOR,
                (random.nextFloat() - random.nextFloat()) / FIREWORK_VELOCITY_DIVISOR);
        level.addParticle(ParticleTypes.FLAME, x, y, z,
                (random.nextFloat() - random.nextFloat()) / FLAME_HORIZONTAL_VELOCITY_DIVISOR,
                random.nextFloat() / FLAME_VERTICAL_VELOCITY_DIVISOR,
                (random.nextFloat() - random.nextFloat()) / FLAME_HORIZONTAL_VELOCITY_DIVISOR);
    }
}
