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

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
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

            for (int i = 0; i < TELEPORT_EFFECT_BURST_COUNT; i++) {
                level.addParticle(ParticleTypes.SMOKE, targetX + 0.5, targetY + TELEPORT_PARTICLE_Y, targetZ + 0.5, 0, 0, 0);
                level.addParticle(ParticleTypes.EXPLOSION, targetX + 0.5, targetY + TELEPORT_PARTICLE_Y, targetZ + 0.5, 0, 0, 0);
                level.addParticle(ParticleTypes.DUST_PLUME, targetX + 0.5, targetY + TELEPORT_PARTICLE_Y, targetZ + 0.5, 0, 0, 0);
            }
            level.playSound(null, new BlockPos(targetX, targetY, targetZ), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, EXPLODE_SOUND_PITCH);
        }
    }
}
