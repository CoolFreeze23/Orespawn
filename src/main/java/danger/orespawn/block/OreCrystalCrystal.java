package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal dimension crystal ore. Sparkles with firework/flame particles.
 * Extends {@link TransparentBlock} so light passes through and adjacent
 * faces between identical blocks are culled (no seam artifacts).
 *
 * <p>1.7.10 OreCrystalCrystal served CrystalCrystal (volatile, firework
 * sparkle) and TigersEye ore (non-volatile, flame sparkle) — orig
 * OreCrystalCrystal.java:35-64.</p>
 */
public class OreCrystalCrystal extends TransparentBlock {
    private static final int ANIMATE_TICK_ROLL_BOUND = 20;
    private static final float PARTICLE_CENTER_OFFSET = 0.5f;
    private static final double PARTICLE_VELOCITY_SCALE = 4.0;
    // orig OreCrystalCrystal.java:59-64 — nextInt(10)==1, power 1.0, flaming
    private static final int VOLATILE_EXPLODE_ROLL_BOUND = 10;
    private static final int VOLATILE_EXPLODE_SUCCESS_INDEX = 1;
    private static final float VOLATILE_EXPLOSION_POWER = 1.0f;
    // orig OreCrystalCrystal.java:66-72 — XP only below Y40
    private static final int XP_MAX_Y_EXCLUSIVE = 40;

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
            // orig OreCrystalCrystal.java:61 — func_72885_a(..., 1.0f, true, mobGriefing): flaming explosion
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    VOLATILE_EXPLOSION_POWER, true, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Y-gated bonus XP (orig OreCrystalCrystal.java:66-72 —
     * {@code 5 + nextInt(5) + nextInt(10)} only when {@code y < 40}).
     *
     * <p>Moved from the dead {@code spawnAfterBreak} path to NeoForge's
     * {@code getExpDrop} — the sole 1.21.1 break-XP source — completing the
     * approved ITEM-003/TF-022 migration across all 8 original XP classes
     * (residual-triage finding, 2026-08-10; mirrors OreUranium). Silk Touch
     * drops no XP via the enchantment's block_experience=0 effect, matching
     * the 1.7.10 silk path.</p>
     */
    @Override
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                          net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                          net.minecraft.world.entity.Entity breaker, ItemStack tool) {
        if (pos.getY() < XP_MAX_Y_EXCLUSIVE) {
            var random = level.getRandom();
            return 5 + random.nextInt(5) + random.nextInt(10);
        }
        return 0;
    }
}

