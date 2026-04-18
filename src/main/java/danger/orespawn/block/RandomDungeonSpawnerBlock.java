package danger.orespawn.block;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.block.entity.RandomDungeonSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Delayed-trigger structure spawner. When placed via {@code random_dungeon}
 * item, the backing {@link RandomDungeonSpawnerBlockEntity} ticks down for
 * 200 ticks (~10 s) — emitting fireworks/spark particles on the way — then
 * removes itself and rolls one of the OreSpawn micro-dungeons at its position.
 *
 * <p>1.7.10 reference: {@code DungeonSpawnerBlock} (extending {@code BlockReed})
 * scheduled a 400-tick world tick, then nulled itself and rolled a 50-way
 * dungeon variant. We tightened the delay to 200 ticks because the legacy 20s
 * felt too long without the visual particle storm modern players expect, and
 * we narrowed the variant pool to what we currently have ported.</p>
 */
public class RandomDungeonSpawnerBlock extends Block implements EntityBlock {
    public RandomDungeonSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RandomDungeonSpawnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.RANDOM_DUNGEON_SPAWNER_BE.get()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<RandomDungeonSpawnerBlockEntity>) RandomDungeonSpawnerBlockEntity::serverTick
                : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
