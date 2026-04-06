package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;

public class BlockMothPlant extends CropBlock {
    private static final int MAX_CROP_AGE = 7;

    public BlockMothPlant(BlockBehaviour.Properties properties) {
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
        if (level.isRaining()) return;

        int age = getAge(state);
        int spawnRollBound = MAX_CROP_AGE - age;
        if (spawnRollBound > 1 && random.nextInt(spawnRollBound) != 0) return;

        BlockPos above = pos.above();
        if (level.getBlockState(above).is(Blocks.AIR) && !level.isDay()) {
            // TODO: Check MothEnable config
            BlockButterflyPlant.spawnCreature(level, ModEntities.ENTITY_LUNA_MOTH.get(), above);
        }
    }
}
