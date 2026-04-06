package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Volatile crystal ore that sparkles, explodes when broken, and drops XP.
 */
public class OreCrystal extends Block {
    private static final int ANIMATE_TICK_ROLL_BOUND = 5;
    private static final int PARTICLE_BURST_COUNT = 5;
    private static final int PARTICLE_TYPE_VARIANT_COUNT = 3;
    private static final double PARTICLE_VELOCITY_SCALE = 4.0;
    private static final float PARTICLE_CENTER_OFFSET = 0.5f;
    private static final int BREAK_EXPLODE_ROLL_BOUND = 3;
    private static final int BREAK_EXPLODE_SUCCESS_INDEX = 1;
    private static final float BREAK_EXPLOSION_POWER = 1.5f;

    public OreCrystal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != 0) return;

        for (int burst = 0; burst < PARTICLE_BURST_COUNT; burst++) {
            int particleKind = random.nextInt(PARTICLE_TYPE_VARIANT_COUNT);
            double vx = (random.nextFloat() - random.nextFloat()) / PARTICLE_VELOCITY_SCALE;
            double vy = (random.nextFloat() - random.nextFloat()) / PARTICLE_VELOCITY_SCALE;
            double vz = (random.nextFloat() - random.nextFloat()) / PARTICLE_VELOCITY_SCALE;

            if (particleKind == 0)
                level.addParticle(ParticleTypes.FLAME, pos.getX() + PARTICLE_CENTER_OFFSET, pos.getY() + PARTICLE_CENTER_OFFSET, pos.getZ() + PARTICLE_CENTER_OFFSET, vx, vy, vz);
            if (particleKind == 1)
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + PARTICLE_CENTER_OFFSET, pos.getY() + PARTICLE_CENTER_OFFSET, pos.getZ() + PARTICLE_CENTER_OFFSET, vx, vy, vz);
            if (particleKind == 2)
                level.addParticle(ParticleTypes.DUST_PLUME, pos.getX() + PARTICLE_CENTER_OFFSET, pos.getY() + PARTICLE_CENTER_OFFSET, pos.getZ() + PARTICLE_CENTER_OFFSET, vx, vy, vz);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.random.nextInt(BREAK_EXPLODE_ROLL_BOUND) == BREAK_EXPLODE_SUCCESS_INDEX) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    BREAK_EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
