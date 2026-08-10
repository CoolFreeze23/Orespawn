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
 * <p>Provenance (audit 2026-08-11): this block is a Phase-11 port modernization
 * with NO 1.7.10 counterpart. The reference dump (reference_1_7_10_source,
 * sources + assets) contains no Extractor class, block, or asset, and no
 * fossil/DNA-extraction mechanic of any kind. An earlier Javadoc here claimed a
 * 1.7.10 "Extractor" GUI block "pulling DNA from fossils" — that claim was
 * spurious and has been removed so future parity passes do not chase a
 * nonexistent original. Whether the block itself is retained is tracked
 * separately as a MODERNIZATION_NOTES MOD entry (orchestrator-owned).</p>
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
