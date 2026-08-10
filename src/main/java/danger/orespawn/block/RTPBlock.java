package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Random Teleport Block - teleports players to a random nearby location on contact.
 */
public class RTPBlock extends Block {
    private static final int MAX_TELEPORT_SEARCH_ATTEMPTS = 1000;
    /** Horizontal offset from block: base distance along one axis. */
    private static final int TELEPORT_BASE_OFFSET = 16;
    /** Random jitter applied on top of base offset (nextInt(n) - nextInt(n)). */
    private static final int TELEPORT_JITTER_SPAN = 8;
    /** Vertical search range above/below starting Y. */
    private static final int VERTICAL_SEARCH_RADIUS = 4;
    private static final int TELEPORT_EFFECT_BURST_COUNT = 6;
    private static final double TELEPORT_PARTICLE_Y = 2.25;
    private static final float EXPLODE_SOUND_PITCH = 1.5f;

    public RTPBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * orig RTPBlock.java:25 — teleport fires from {@code func_149724_b}
     * (onEntityWalking, i.e. standing on top of the block). The old port used
     * {@code entityInside}, which never fires for a full cube (ITEM-013).
     */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide()) return;

        int startX = pos.getX();
        int startY = pos.getY();
        int startZ = pos.getZ();

        boolean foundValidSpot = false;
        int targetX = startX;
        int targetY = startY;
        int targetZ = startZ;

        for (int attempt = 0; attempt < MAX_TELEPORT_SEARCH_ATTEMPTS && !foundValidSpot; attempt++) {
            targetX = level.random.nextInt(2) == 0
                    ? startX + TELEPORT_BASE_OFFSET + level.random.nextInt(TELEPORT_JITTER_SPAN) - level.random.nextInt(TELEPORT_JITTER_SPAN)
                    : startX - TELEPORT_BASE_OFFSET + level.random.nextInt(TELEPORT_JITTER_SPAN) - level.random.nextInt(TELEPORT_JITTER_SPAN);
            targetZ = level.random.nextInt(2) == 0
                    ? startZ + TELEPORT_BASE_OFFSET + level.random.nextInt(TELEPORT_JITTER_SPAN) - level.random.nextInt(TELEPORT_JITTER_SPAN)
                    : startZ - TELEPORT_BASE_OFFSET + level.random.nextInt(TELEPORT_JITTER_SPAN) - level.random.nextInt(TELEPORT_JITTER_SPAN);

            for (targetY = startY - VERTICAL_SEARCH_RADIUS; targetY <= startY + VERTICAL_SEARCH_RADIUS; targetY++) {
                BlockPos candidateFeet = new BlockPos(targetX, targetY, targetZ);
                BlockPos groundBelow = candidateFeet.below();
                if (level.getBlockState(groundBelow).isSolid()
                        && level.getBlockState(candidateFeet).is(Blocks.AIR)
                        && level.getBlockState(candidateFeet.above()).is(Blocks.AIR)) {
                    foundValidSpot = true;
                    break;
                }
            }
        }

        if (foundValidSpot) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(targetX + 0.5, targetY, targetZ + 0.5, player.getYRot(), 0.0f);
            } else {
                player.teleportTo(targetX + 0.5, targetY, targetZ + 0.5);
            }

            // BUG-028: Level.addParticle is an empty no-op on the server (this
            // method is server-gated above), so the burst never rendered. In
            // 1.7.10 only the teleported player's OWN client drew it (orig
            // RTPBlock.java:51-56 ran in the client's movement replay), so the
            // per-player sendParticles overload is the faithful delivery.
            // Particle mapping: orig "smoke"=SMOKE, "explode"=POOF (the small
            // white puff — modern EXPLOSION is the large blast flash),
            // "reddust"=red DUST (DUST_PLUME did not exist in 1.7.10).
            if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                double px = targetX + 0.5, py = targetY + TELEPORT_PARTICLE_Y, pz = targetZ + 0.5;
                var redDust = new net.minecraft.core.particles.DustParticleOptions(
                        new org.joml.Vector3f(1.0f, 0.0f, 0.0f), 1.0f);
                for (int i = 0; i < TELEPORT_EFFECT_BURST_COUNT; i++) {
                    serverLevel.sendParticles(serverPlayer, ParticleTypes.SMOKE, false, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                    serverLevel.sendParticles(serverPlayer, ParticleTypes.POOF, false, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                    serverLevel.sendParticles(serverPlayer, redDust, false, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            level.playSound(null, new BlockPos(targetX, targetY, targetZ), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, EXPLODE_SOUND_PITCH);
        }
    }
}
