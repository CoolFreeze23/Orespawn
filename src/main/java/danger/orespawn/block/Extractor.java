package danger.orespawn.block;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.block.entity.ExtractorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
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
 * Extractor block — a passive, non-GUI processor. Items inserted into the top
 * face (via hopper) are matched against {@code orespawn:extracting} recipes
 * and the result is pushed into the bottom face (typically into another hopper).
 *
 * <p>1.7.10 reference: TheyCallMeDanger's "Extractor" was a manual GUI block
 * pulling DNA from fossils. The 1.21.1 modernization is hopper-driven so it
 * composes with vanilla automation without bringing in a custom screen.</p>
 */
public class Extractor extends Block implements EntityBlock {
    public Extractor(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return type == ModBlockEntities.EXTRACTOR_BE.get()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<ExtractorBlockEntity>) ExtractorBlockEntity::serverTick
                : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ExtractorBlockEntity extractor) {
                Containers.dropContents(level, pos, extractor.snapshotInventory());
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
