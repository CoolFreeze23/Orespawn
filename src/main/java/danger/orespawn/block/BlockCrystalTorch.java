package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCrystalTorch extends TorchBlock {
    public BlockCrystalTorch(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FIREWORK, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 1) return;

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;

        level.addParticle(ParticleTypes.FIREWORK, x, y, z,
                (random.nextFloat() - random.nextFloat()) / 8.0,
                random.nextFloat() / 8.0,
                (random.nextFloat() - random.nextFloat()) / 8.0);
        level.addParticle(ParticleTypes.FLAME, x, y, z,
                (random.nextFloat() - random.nextFloat()) / 60.0,
                random.nextFloat() / 10.0,
                (random.nextFloat() - random.nextFloat()) / 60.0);
    }
}
