package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import danger.orespawn.ModEntities;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stone blocks that spawn creatures when broken (e.g. rat stone, fairy stone, ant troll stone).
 * Extends {@link TransparentBlock} so light passes through and adjacent
 * faces between identical blocks are culled (no seam artifacts).
 */
public class OreBasicStone extends TransparentBlock {

    public enum StoneType { NORMAL, RAT, FAIRY, RED_ANT_TROLL, TERMITE_TROLL }

    private static final int RAT_SPAWN_MAX_EXTRA = 10;
    private static final int FAIRY_SPAWN_MAX_EXTRA = 6;
    private static final int ANT_TROLL_SPAWN_BASE = 15;
    private static final int ANT_TROLL_SPAWN_MAX_EXTRA = 6;

    private final StoneType stoneType;

    public OreBasicStone(BlockBehaviour.Properties properties, StoneType stoneType) {
        super(properties);
        this.stoneType = stoneType;
    }

    public OreBasicStone(BlockBehaviour.Properties properties) {
        this(properties, StoneType.NORMAL);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel && !serverLevel.isClientSide()) {
            spawnOnBreak(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void spawnOnBreak(ServerLevel level, BlockPos pos) {
        int spawnCount;
        switch (stoneType) {
            case RAT:
                spawnCount = 1 + level.random.nextInt(RAT_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.ENTITY_RAT.get(), pos);
                }
                break;
            case FAIRY:
                spawnCount = 1 + level.random.nextInt(FAIRY_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.FAIRY.get(), pos);
                }
                break;
            case RED_ANT_TROLL:
                spawnCount = ANT_TROLL_SPAWN_BASE + level.random.nextInt(ANT_TROLL_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.ENTITY_RED_ANT.get(), pos);
                }
                break;
            case TERMITE_TROLL:
                spawnCount = ANT_TROLL_SPAWN_BASE + level.random.nextInt(ANT_TROLL_SPAWN_MAX_EXTRA);
                for (int i = 0; i < spawnCount; i++) {
                    spawnCreature(level, ModEntities.ENTITY_TERMITE.get(), pos);
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
