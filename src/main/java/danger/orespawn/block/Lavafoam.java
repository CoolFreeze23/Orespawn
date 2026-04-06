package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
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
    public Lavafoam(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) != 0) return;

        double offset = 0.0625;
        for (int face = 0; face < 6; face++) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();

            if (face == 0 && !level.getBlockState(pos.above()).canOcclude()) y = pos.getY() + 1 + offset;
            if (face == 1 && !level.getBlockState(pos.below()).canOcclude()) y = pos.getY() - offset;
            if (face == 2 && !level.getBlockState(pos.south()).canOcclude()) z = pos.getZ() + 1 + offset;
            if (face == 3 && !level.getBlockState(pos.north()).canOcclude()) z = pos.getZ() - offset;
            if (face == 4 && !level.getBlockState(pos.east()).canOcclude()) x = pos.getX() + 1 + offset;
            if (face == 5 && !level.getBlockState(pos.west()).canOcclude()) x = pos.getX() - offset;

            if (x < pos.getX() || x > pos.getX() + 1 || y < 0 || y > pos.getY() + 1 || z < pos.getZ() || z > pos.getZ() + 1) {
                int which = random.nextInt(10);
                if (which == 1) level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                if (which == 2) level.addParticle(ParticleTypes.DUST_PLUME, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        double centerX = pos.getX() + 0.5;
        double centerZ = pos.getZ() + 0.5;
        double angle = Math.atan2(entity.getX() - centerX, entity.getZ() - centerZ);
        if (angle < 0) angle += Math.PI * 2;

        double pi = Math.PI;
        double pi2 = pi / 2.0;
        double pi4 = pi / 4.0;

        Vec3 motion = entity.getDeltaMovement();
        double mx = motion.x;
        double mz = motion.z;

        if (angle > pi2 - pi4 && angle < pi2 + pi4) {
            mx = 0.45;
            mz *= 1.35;
        } else if (angle > pi - pi4 && angle < pi + pi4) {
            mz = -0.45;
            mx *= 1.35;
        } else if (angle > pi + pi2 - pi4 && angle < pi + pi2 + pi4) {
            mx = -0.45;
            mz *= 1.35;
        } else {
            mz = 0.45;
            mx *= 1.35;
        }

        entity.setDeltaMovement(mx, motion.y, mz);

        double speed = Math.sqrt(mz * mz + mx * mx);
        if (speed > 1.0) {
            entity.hurt(level.damageSources().generic(), (float) speed);
        }
    }
}
