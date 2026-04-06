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
    public RTPBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;
        int startX = pos.getX();
        int startY = pos.getY();
        int startZ = pos.getZ();

        boolean found = false;
        int x = startX, y = startY, z = startZ;

        for (int tries = 0; tries < 1000 && !found; tries++) {
            x = level.random.nextInt(2) == 0
                    ? startX + 16 + level.random.nextInt(8) - level.random.nextInt(8)
                    : startX - 16 + level.random.nextInt(8) - level.random.nextInt(8);
            z = level.random.nextInt(2) == 0
                    ? startZ + 16 + level.random.nextInt(8) - level.random.nextInt(8)
                    : startZ - 16 + level.random.nextInt(8) - level.random.nextInt(8);

            for (y = startY - 4; y <= startY + 4; y++) {
                BlockPos target = new BlockPos(x, y, z);
                BlockPos belowTarget = target.below();
                if (level.getBlockState(belowTarget).isSolid()
                        && level.getBlockState(target).is(Blocks.AIR)
                        && level.getBlockState(target.above()).is(Blocks.AIR)) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(x + 0.5, y, z + 0.5, player.getYRot(), 0.0f);
            } else {
                player.teleportTo(x + 0.5, y, z + 0.5);
            }

            for (int i = 0; i < 6; i++) {
                level.addParticle(ParticleTypes.SMOKE, x + 0.5, y + 2.25, z + 0.5, 0, 0, 0);
                level.addParticle(ParticleTypes.EXPLOSION, x + 0.5, y + 2.25, z + 0.5, 0, 0, 0);
                level.addParticle(ParticleTypes.DUST_PLUME, x + 0.5, y + 2.25, z + 0.5, 0, 0, 0);
            }
            level.playSound(null, new BlockPos(x, y, z), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, 1.5f);
        }
    }
}
