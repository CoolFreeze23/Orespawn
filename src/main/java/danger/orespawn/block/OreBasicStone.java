package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;

/**
 * Stone blocks that spawn creatures when broken (e.g. rat stone, fairy stone, ant troll stone).
 */
public class OreBasicStone extends Block {

    public enum StoneType { NORMAL, RAT, FAIRY, RED_ANT_TROLL, TERMITE_TROLL }

    private final StoneType stoneType;

    public OreBasicStone(BlockBehaviour.Properties properties, StoneType stoneType) {
        super(properties);
        this.stoneType = stoneType;
    }

    public OreBasicStone(BlockBehaviour.Properties properties) {
        this(properties, StoneType.NORMAL);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel && !serverLevel.isClientSide()) {
            spawnOnBreak(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void spawnOnBreak(ServerLevel level, BlockPos pos) {
        int num;
        switch (stoneType) {
            case RAT:
                num = 1 + level.random.nextInt(10);
                for (int i = 0; i < num; i++) {
                    // TODO: spawnCreature(level, ModEntities.RAT.get(), pos);
                }
                break;
            case FAIRY:
                num = 1 + level.random.nextInt(6);
                for (int i = 0; i < num; i++) {
                    // TODO: spawnCreature(level, ModEntities.FAIRY.get(), pos);
                }
                break;
            case RED_ANT_TROLL:
                num = 15 + level.random.nextInt(6);
                for (int i = 0; i < num; i++) {
                    // TODO: spawnCreature(level, ModEntities.RED_ANT.get(), pos);
                }
                break;
            case TERMITE_TROLL:
                num = 15 + level.random.nextInt(6);
                for (int i = 0; i < num; i++) {
                    // TODO: spawnCreature(level, ModEntities.TERMITE.get(), pos);
                }
                break;
            default:
                break;
        }
    }

    private void spawnCreature(ServerLevel level, EntityType<? extends Mob> type, BlockPos pos) {
        Mob entity = type.create(level);
        if (entity != null) {
            double x = pos.getX() + 0.5 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2;
            double z = pos.getZ() + 0.5 + (level.random.nextFloat() - level.random.nextFloat()) * 0.2;
            entity.moveTo(x, pos.getY() + 0.01, z, level.random.nextFloat() * 360.0f, 0.0f);
            entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(entity);
        }
    }
}
