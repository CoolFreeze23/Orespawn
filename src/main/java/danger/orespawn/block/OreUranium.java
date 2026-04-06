package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Uranium ore that sparkles with redstone particles when interacted with.
 */
public class OreUranium extends Block {
    private boolean glowing = false;
    private int glowCount = 0;

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
        this.glowCount = 10;
        sparkle(level, pos, level.random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!glowing) return;
        sparkle(level, pos, random);
        if (glowCount > 0) {
            glowCount--;
        } else {
            glowing = false;
        }
    }

    private void sparkle(Level level, BlockPos pos, RandomSource random) {
        double offset = 0.0625;
        for (int face = 0; face < 6; face++) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + random.nextFloat();
            double z = pos.getZ() + random.nextFloat();

            if (face == 0 && !level.getBlockState(pos.above()).canOcclude()) y = pos.getY() + 1 + offset;
            if (face == 1 && !level.getBlockState(pos.below()).canOcclude()) y = pos.getY() - offset;
            if (face == 2 && !level.getBlockState(pos.south()).canOcclude()) z = pos.getZ() + 1 + offset;
            if (face == 3 && !level.getBlockState(pos.north()).canOcclude()) z = pos.getZ() - offset;
            if (face == 4 && !level.getBlockState(pos.east()).canOcclude()) x = pos.getX() + 1 + offset;
            if (face == 5 && !level.getBlockState(pos.west()).canOcclude()) x = pos.getX() - offset;

            if (x < pos.getX() || x > pos.getX() + 1 || y < 0 || y > pos.getY() + 1 || z < pos.getZ() || z > pos.getZ() + 1) {
                level.addParticle(ParticleTypes.DUST_PLUME, x, y, z, 0, 0, 0);
            }
        }
    }
}
