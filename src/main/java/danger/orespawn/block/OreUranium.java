package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Uranium ore that sparkles with redstone particles when interacted with.
 * Drops bonus XP only when broken below Y40 (orig OreUranium.java:101-107).
 */
public class OreUranium extends Block {
    private static final int FACE_COUNT = 6;
    /** Particle offset from block face (1/16 block). */
    private static final double FACE_PARTICLE_EPSILON = 0.0625;
    private static final int GLOW_ANIMATION_TICKS = 10;
    // orig OreUranium.java:104 — XP only below Y40 (ITEM-003)
    private static final int XP_MAX_Y_EXCLUSIVE = 40;

    private boolean glowing = false;
    private int glowTicksRemaining = 0;

    public OreUranium(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        glow(level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        glow(level, pos);
        super.stepOn(level, pos, state, entity);
    }

    private void glow(Level level, BlockPos pos) {
        this.glowing = true;
        this.glowTicksRemaining = GLOW_ANIMATION_TICKS;
        sparkle(level, pos, level.random);
    }

    /**
     * Y-gated bonus XP (orig OreUranium.java:101-107 —
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

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!glowing) return;
        sparkle(level, pos, random);
        if (glowTicksRemaining > 0) {
            glowTicksRemaining--;
        } else {
            glowing = false;
        }
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
                level.addParticle(ParticleTypes.DUST_PLUME, particleX, particleY, particleZ, 0, 0, 0);
            }
        }
    }
}
