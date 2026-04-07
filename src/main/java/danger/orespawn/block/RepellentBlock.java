package danger.orespawn.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

/**
 * Torch-like block that repels matching entities within a 20-block radius.
 * Entities are pushed away with a force inversely proportional to distance.
 */
public class RepellentBlock extends Block {
    private static final double REPEL_RADIUS = 20.0;
    private static final double REPEL_VERTICAL_RADIUS = 10.0;
    private static final double REPEL_STRENGTH = 0.4;

    private final Predicate<LivingEntity> repelTest;

    public RepellentBlock(BlockBehaviour.Properties properties, Predicate<LivingEntity> repelTest) {
        super(properties);
        this.repelTest = repelTest;
    }

    @Override
    protected MapCodec<? extends RepellentBlock> codec() {
        return simpleCodec(p -> new RepellentBlock(p, e -> false));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AABB area = new AABB(pos).inflate(REPEL_RADIUS, REPEL_VERTICAL_RADIUS, REPEL_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!repelTest.test(entity)) continue;

            double dx = entity.getX() - pos.getX();
            double dy = entity.getY() - pos.getY();
            double dz = entity.getZ() - pos.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double force = REPEL_RADIUS - dist;
            if (force <= 0) continue;
            force = Math.min(force, REPEL_RADIUS) * REPEL_STRENGTH;

            double angle = Math.atan2(dx, dz);
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    force * Math.sin(angle), 0, force * Math.cos(angle)));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;

        level.addParticle(ParticleTypes.SMOKE, x, y + 0.21, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y + 0.21, z, 0, 0, 0);
        level.addParticle(ParticleTypes.DUST_PLUME, x, y + 0.21, z, 0, 0, 0);
    }
}
