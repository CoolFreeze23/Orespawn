package danger.orespawn.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Steps-on portal that teleports players between the Overworld and Utopia.
 */
public class UtopiaPortalBlock extends Block {
    public static final ResourceKey<Level> UTOPIA_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "utopia"));
    private static final int PARTICLE_COUNT = 4;
    private static final int COOLDOWN_TICKS = 80;

    public UtopiaPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends UtopiaPortalBlock> codec() {
        return simpleCodec(UtopiaPortalBlock::new);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (player.isPassenger() || player.isVehicle()) return;
        if (player.isOnPortalCooldown()) return;

        player.setPortalCooldown(COOLDOWN_TICKS);

        ServerLevel currentLevel = (ServerLevel) level;
        ResourceKey<Level> destination = level.dimension() == UTOPIA_DIMENSION
                ? Level.OVERWORLD
                : UTOPIA_DIMENSION;

        ServerLevel destLevel = currentLevel.getServer().getLevel(destination);
        if (destLevel == null) return;

        double y = Math.max(destLevel.getMinBuildHeight() + 1, 64);
        DimensionTransition transition = new DimensionTransition(
                destLevel,
                new Vec3(player.getX(), y, player.getZ()),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                DimensionTransition.DO_NOTHING
        );
        player.changeDimension(transition);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();
            level.addParticle(ParticleTypes.PORTAL, x, y, z,
                    (random.nextFloat() - 0.5) * 0.5,
                    (random.nextFloat() - 0.5) * 0.5,
                    (random.nextFloat() - 0.5) * 0.5);
        }
    }
}
