package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal dimension crystal ore. Sparkles with firework/flame particles.
 * Extends {@link TransparentBlock} so light passes through and adjacent
 * faces between identical blocks are culled (no seam artifacts).
 */
public class OreCrystalCrystal extends TransparentBlock {
    private static final int ANIMATE_TICK_ROLL_BOUND = 20;
    private static final float PARTICLE_CENTER_OFFSET = 0.5f;
    private static final double PARTICLE_VELOCITY_SCALE = 4.0;
    private static final int VOLATILE_EXPLODE_ROLL_BOUND = 10;
    private static final int VOLATILE_EXPLODE_SUCCESS_INDEX = 1;
    private static final float VOLATILE_EXPLOSION_POWER = 1.0f;

    private final boolean isVolatile;
    private final boolean useFlameParticle;

    public OreCrystalCrystal(BlockBehaviour.Properties properties, boolean isVolatile, boolean useFlameParticle) {
        super(properties);
        this.isVolatile = isVolatile;
        this.useFlameParticle = useFlameParticle;
    }

    public OreCrystalCrystal(BlockBehaviour.Properties properties) {
        this(properties, false, false);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != 0) return;

        double vx = (random.nextFloat() - random.nextFloat()) / PARTICLE_VELOCITY_SCALE;
        double vy = (random.nextFloat() - random.nextFloat()) / PARTICLE_VELOCITY_SCALE;
        double vz = (random.nextFloat() - random.nextFloat()) / PARTICLE_VELOCITY_SCALE;

        if (useFlameParticle) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + PARTICLE_CENTER_OFFSET, pos.getY() + PARTICLE_CENTER_OFFSET, pos.getZ() + PARTICLE_CENTER_OFFSET, vx, vy, vz);
        } else {
            level.addParticle(ParticleTypes.FIREWORK, pos.getX() + PARTICLE_CENTER_OFFSET, pos.getY() + PARTICLE_CENTER_OFFSET, pos.getZ() + PARTICLE_CENTER_OFFSET, vx, vy, vz);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (isVolatile && !level.isClientSide() && level.random.nextInt(VOLATILE_EXPLODE_ROLL_BOUND) == VOLATILE_EXPLODE_SUCCESS_INDEX) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    VOLATILE_EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
