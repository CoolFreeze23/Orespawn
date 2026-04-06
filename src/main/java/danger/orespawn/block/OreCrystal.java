package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
    public OreCrystal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) return;

        float dx = 0.5f;
        float dy = 0.5f;
        float dz = 0.5f;

        for (int i = 0; i < 5; i++) {
            int which = random.nextInt(3);
            double vx = (random.nextFloat() - random.nextFloat()) / 4.0;
            double vy = (random.nextFloat() - random.nextFloat()) / 4.0;
            double vz = (random.nextFloat() - random.nextFloat()) / 4.0;

            if (which == 0)
                level.addParticle(ParticleTypes.FLAME, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, vx, vy, vz);
            if (which == 1)
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, vx, vy, vz);
            if (which == 2)
                level.addParticle(ParticleTypes.DUST_PLUME, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, vx, vy, vz);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.random.nextInt(3) == 1) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.5f, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
