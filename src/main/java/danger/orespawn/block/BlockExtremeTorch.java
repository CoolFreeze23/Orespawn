package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.entity.Cephadrome;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExtremeTorch extends TorchBlock {
    private static final double FLAME_CENTER_XZ = 0.5;
    private static final double FLAME_CENTER_Y = 0.7;

    public BlockExtremeTorch(BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + FLAME_CENTER_XZ;
        double y = pos.getY() + FLAME_CENTER_Y;
        double z = pos.getZ() + FLAME_CENTER_XZ;

        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.DUST_PLUME, x, y, z, 0, 0, 0);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.is(ModBlocks.BLOCK_EYE_OF_ENDER.get())) {
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f,
                    level.random.nextFloat() * 0.2f + 0.9f);
            if (!level.isClientSide) {
                Cephadrome ceph = ModEntities.CEPHADROME.get().create(level);
                if (ceph != null) {
                    ceph.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(ceph);
                }
            }
        }
    }
}
