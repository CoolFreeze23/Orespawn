package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUranium extends Block {
    private static final int FACE_COUNT = 6;
    private static final double FACE_PARTICLE_EPSILON = 0.0625;
    private static final int ANIMATE_TICK_ROLL_BOUND = 20;
    private static final int PARTICLE_TYPE_COUNT = 3;

    public BlockUranium(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != 0) return;
        sparkle(level, pos, random);
    }

    private void sparkle(Level level, BlockPos pos, RandomSource random) {
        for (int faceIndex = 0; faceIndex < FACE_COUNT; faceIndex++) {
            double particleX = pos.getX() + random.nextFloat();
            double particleY = pos.getY() + random.nextFloat();
            double particleZ = pos.getZ() + random.nextFloat();

            if (faceIndex == 0 && !level.getBlockState(pos.above()).canOcclude()) particleY = pos.getY() + 1 + FACE_PARTICLE_EPSILON;
            if (faceIndex == 1 && !level.getBlockState(pos.below()).canOcclude()) particleY = pos.getY() - FACE_PARTICLE_EPSILON;
            if (faceIndex == 2 && !level.getBlockState(pos.south()).canOcclude()) particleZ = pos.getZ() + 1 + FACE_PARTICLE_EPSILON;
            if (faceIndex == 3 && !level.getBlockState(pos.north()).canOcclude()) particleZ = pos.getZ() - FACE_PARTICLE_EPSILON;
            if (faceIndex == 4 && !level.getBlockState(pos.east()).canOcclude()) particleX = pos.getX() + 1 + FACE_PARTICLE_EPSILON;
            if (faceIndex == 5 && !level.getBlockState(pos.west()).canOcclude()) particleX = pos.getX() - FACE_PARTICLE_EPSILON;

            if (particleX < pos.getX() || particleX > pos.getX() + 1 || particleY < 0 || particleY > pos.getY() + 1 || particleZ < pos.getZ() || particleZ > pos.getZ() + 1) {
                int particleKind = random.nextInt(PARTICLE_TYPE_COUNT);
                if (particleKind == 0) level.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, 0, 0, 0);
                if (particleKind == 1) level.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, 0, 0, 0);
                if (particleKind == 2) level.addParticle(ParticleTypes.DUST_PLUME, particleX, particleY, particleZ, 0, 0, 0);
            }
        }
    }
}
