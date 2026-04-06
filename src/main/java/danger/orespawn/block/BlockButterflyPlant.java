package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;

public class BlockButterflyPlant extends CropBlock {
    private static final int MAX_CROP_AGE = 7;

    public BlockButterflyPlant(BlockBehaviour.Properties properties) {
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
        if (level.getBlockState(above).is(Blocks.AIR) && level.isDay()) {
            // TODO: Check ButterflyEnable config
            spawnCreature(level, ModEntities.ENTITY_BUTTERFLY.get(), above);
        }
    }

    public static void spawnCreature(ServerLevel level, EntityType<? extends Mob> type, BlockPos pos) {
        Mob entity = type.create(level);
        if (entity != null) {
            entity.moveTo(pos.getX() + 0.5, pos.getY() + 0.01, pos.getZ() + 0.5,
                    level.random.nextFloat() * 360.0f, 0.0f);
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(entity);
        }
    }
}
