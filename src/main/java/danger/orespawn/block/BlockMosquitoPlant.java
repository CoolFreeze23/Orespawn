package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;

public class BlockMosquitoPlant extends CropBlock {
    public BlockMosquitoPlant(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxAge() {
        return 7;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        // TODO: Check MosquitoEnable config
        int age = getAge(state);
        int rate = 7 - age;
        if (rate > 1 && random.nextInt(rate) != 0) return;

        BlockPos above = pos.above();
        if (level.getBlockState(above).is(Blocks.AIR)) {
            int howmany = 2 + random.nextInt(5);
            for (int i = 0; i < howmany; i++) {
                BlockButterflyPlant.spawnCreature(level, ModEntities.MOSQUITO.get(), above);
            }
        }
    }
}
