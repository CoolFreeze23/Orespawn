package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;

public class BlockFireflyPlant extends CropBlock {
    private static final int MAX_CROP_AGE = 7;
    /** Firefly spawn roll uses (MAX_SPAWN_ROLL_BASE - age) like other night crops, capped at max age 6 in original formula. */
    private static final int SPAWN_ROLL_BASE = 6;
    private static final int MIN_FIREFLIES = 2;
    private static final int MAX_EXTRA_FIREFLIES = 5;

    public BlockFireflyPlant(BlockBehaviour.Properties properties) {
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
        int spawnRollBound = SPAWN_ROLL_BASE - age;
        if (spawnRollBound > 1 && random.nextInt(spawnRollBound) != 0) return;

        BlockPos above = pos.above();
        if (level.getBlockState(above).is(Blocks.AIR) && !level.isDay()) {
            // TODO: Check FireflyEnable config
            int fireflyCount = MIN_FIREFLIES + random.nextInt(MAX_EXTRA_FIREFLIES);
            for (int i = 0; i < fireflyCount; i++) {
                BlockButterflyPlant.spawnCreature(level, ModEntities.FIREFLY.get(), above);
            }
        }
    }
}
