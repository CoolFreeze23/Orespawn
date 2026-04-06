package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Lava foam block that pushes entities away from center on collision.
 */
public class Lavafoam extends Block {
    private static final int FACE_COUNT = 6;
    private static final double FACE_PARTICLE_EPSILON = 0.0625;
    private static final int ANIMATE_TICK_ROLL_BOUND = 20;
    private static final int PARTICLE_KIND_ROLL_BOUND = 10;
    private static final int SMOKE_KIND_INDEX = 1;
    private static final int DUST_KIND_INDEX = 2;
    private static final double PUSH_COMPONENT = 0.45;
    private static final double TANGENTIAL_BOOST = 1.35;
    private static final double MAX_DAMAGE_SPEED = 1.0;

    public Lavafoam(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != 0) return;

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
                int particleKind = random.nextInt(PARTICLE_KIND_ROLL_BOUND);
                if (particleKind == SMOKE_KIND_INDEX) level.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, 0, 0, 0);
                if (particleKind == DUST_KIND_INDEX) level.addParticle(ParticleTypes.DUST_PLUME, particleX, particleY, particleZ, 0, 0, 0);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double angleFromNorth = Math.atan2(entity.getX() - centerX, entity.getZ() - centerZ);
        if (angleFromNorth < 0) angleFromNorth += Math.PI * 2;

        double halfPi = Math.PI / 2.0;
        double quarterPi = Math.PI / 4.0;

        Vec3 motion = entity.getDeltaMovement();
        double motionX = motion.x;
        double motionZ = motion.z;

        if (angleFromNorth > halfPi - quarterPi && angleFromNorth < halfPi + quarterPi) {
            motionX = PUSH_COMPONENT;
            motionZ *= TANGENTIAL_BOOST;
        } else if (angleFromNorth > Math.PI - quarterPi && angleFromNorth < Math.PI + quarterPi) {
            motionZ = -PUSH_COMPONENT;
            motionX *= TANGENTIAL_BOOST;
        } else if (angleFromNorth > Math.PI + halfPi - quarterPi && angleFromNorth < Math.PI + halfPi + quarterPi) {
            motionX = -PUSH_COMPONENT;
            motionZ *= TANGENTIAL_BOOST;
        } else {
            motionZ = PUSH_COMPONENT;
            motionX *= TANGENTIAL_BOOST;
        }

        entity.setDeltaMovement(motionX, motion.y, motionZ);

        double horizontalSpeed = Math.sqrt(motionZ * motionZ + motionX * motionX);
        if (horizontalSpeed > MAX_DAMAGE_SPEED) {
            entity.hurt(level.damageSources().generic(), (float) horizontalSpeed);
        }
    }
}
