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
 * Crystal dimension crystal ore. Sparkles with firework/flame particles.
 * CrystalCrystal variant has a chance to explode when broken.
 */
public class OreCrystalCrystal extends Block {
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) != 0) return;

        float dx = 0.5f;
        float dy = 0.5f;
        float dz = 0.5f;
        double vx = (random.nextFloat() - random.nextFloat()) / 4.0;
        double vy = (random.nextFloat() - random.nextFloat()) / 4.0;
        double vz = (random.nextFloat() - random.nextFloat()) / 4.0;

        if (useFlameParticle) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, vx, vy, vz);
        } else {
            level.addParticle(ParticleTypes.FIREWORK, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, vx, vy, vz);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (isVolatile && !level.isClientSide() && level.random.nextInt(10) == 1) {
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.0f, Level.ExplosionInteraction.BLOCK);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
