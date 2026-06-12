package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 OreCrystal port — the Crystal Dimension's volatile CrystalCoal ore.
 * Sparkles, has a 1-in-3 chance to explode (with fire) when broken by a
 * player, and drops bonus XP below Y40.
 *
 * <p>In 1.7.10 this class was used ONLY by CrystalCoal
 * (orig OreSpawnMain.java:1865); the overworld ruby/amethyst ores were plain
 * non-volatile blocks (see {@link OreRuby}).</p>
 */
public class OreCrystal extends TransparentBlock {
    private static final int ANIMATE_TICK_ROLL_BOUND = 5;
    private static final int PARTICLE_BURST_COUNT = 5;
    private static final int PARTICLE_TYPE_VARIANT_COUNT = 3;
    private static final double PARTICLE_VELOCITY_SCALE = 4.0;
    private static final float PARTICLE_CENTER_OFFSET = 0.5f;
    // orig OreCrystal.java:64-69 — nextInt(3)==1, power 1.5, flaming
    private static final int BREAK_EXPLODE_ROLL_BOUND = 3;
    private static final int BREAK_EXPLODE_SUCCESS_INDEX = 1;
    private static final float BREAK_EXPLOSION_POWER = 1.5f;
    // orig OreCrystal.java:71-77 — XP only below Y40
    private static final int XP_MAX_Y_EXCLUSIVE = 40;

    public OreCrystal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
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
            // orig OreCrystal.java:66 — func_72885_a(..., 1.5f, true, mobGriefing): flaming explosion
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    BREAK_EXPLOSION_POWER, true, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Bonus XP, Y-gated like the original (orig OreCrystal.java:71-77 —
     * {@code 5 + nextInt(5) + nextInt(10)} only when {@code y < 40}).
     * {@code dropExperience} is false for Silk Touch harvests, matching the
     * 1.7.10 silk-harvest path which skipped dropBlockAsItemWithChance.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience && pos.getY() < XP_MAX_Y_EXCLUSIVE) {
            popExperience(level, pos, 5 + level.random.nextInt(5) + level.random.nextInt(10));
        }
    }
}
