package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExperienceLeaves extends LeavesBlock {
    public BlockExperienceLeaves(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        long t = level.getDayTime() % 24000L;
        if (t < 14000L || t > 22000L) return;

        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (random.nextInt(65) == 1 && level.getBlockState(above).is(Blocks.AIR)) {
            Block.popResource(level, above, new ItemStack(Items.EXPERIENCE_BOTTLE));
        }

        if (random.nextInt(75) == 1 && level.getBlockState(below).is(Blocks.AIR)) {
            ThrownExperienceBottle bottle = new ThrownExperienceBottle(level, pos.getX(), pos.getY() - 1, pos.getZ());
            bottle.setDeltaMovement(
                    (random.nextFloat() - random.nextFloat()) / 2.0,
                    -0.1,
                    (random.nextFloat() - random.nextFloat()) / 2.0
            );
            level.addFreshEntity(bottle);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        long t = level.getDayTime() % 24000L;
        if (t < 13000L || t > 23000L) return;

        int rate = 0;
        if (t < 14000L) rate = (14000 - (int) t) / 2;
        if (t > 22000L) rate = (int) (t - 22000L) / 2;

        BlockPos above = pos.above();
        BlockPos below = pos.below();

        if (random.nextInt(200 + rate) == 1 && level.getBlockState(above).is(Blocks.AIR)) {
            for (int i = 0; i < 10; i++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5,
                        random.nextGaussian(), Math.abs(random.nextGaussian()), random.nextGaussian());
            }
        }

        if (random.nextInt(40 + rate) == 1 && level.getBlockState(below).is(Blocks.AIR)) {
            for (int i = 0; i < 4; i++) {
                level.addParticle(ParticleTypes.FIREWORK,
                        pos.getX() + 0.5, pos.getY() - 1.25, pos.getZ() + 0.5,
                        random.nextFloat() - random.nextFloat(),
                        -Math.abs(random.nextFloat()),
                        random.nextFloat() - random.nextFloat());
            }
        }
    }
}
