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
    private static final int MAX_CROP_AGE = 7;
    /** Inclusive minimum mosquitoes spawned per successful tick. */
    private static final int MIN_MOSQUITO_SPAWN = 2;
    /** Extra mosquitoes: random value in [0, MAX_EXTRA) added to minimum. */
    private static final int MAX_EXTRA_MOSQUITOES = 5;

    public BlockMosquitoPlant(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxAge() {
        return MAX_CROP_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.isClientSide()) return;

        // TODO: Check MosquitoEnable config
        int age = getAge(state);
        int spawnRollBound = MAX_CROP_AGE - age;
        if (spawnRollBound > 1 && random.nextInt(spawnRollBound) != 0) return;

        BlockPos above = pos.above();
        if (level.getBlockState(above).is(Blocks.AIR)) {
            int mosquitoCount = MIN_MOSQUITO_SPAWN + random.nextInt(MAX_EXTRA_MOSQUITOES);
            for (int i = 0; i < mosquitoCount; i++) {
                BlockButterflyPlant.spawnCreature(level, ModEntities.ENTITY_MOSQUITO.get(), above);
            }
        }
    }
}
