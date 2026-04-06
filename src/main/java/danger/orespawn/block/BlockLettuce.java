package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import danger.orespawn.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

/**
 * Reed-like lettuce plant with 4 growth stages (separate block variants).
 * AGE property tracks growth delay within a stage.
 */
public class BlockLettuce extends BushBlock {
    /** AGE at or above this transitions to the next block variant (when ported). */
    private static final int NEXT_STAGE_AGE_THRESHOLD = 4;

    @Override
    protected MapCodec<? extends BlockLettuce> codec() {
        return simpleCodec(BlockLettuce::new);
    }

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockLettuce(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND
                || block instanceof BlockLettuce;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;

        int age = state.getValue(AGE);
        if (age >= NEXT_STAGE_AGE_THRESHOLD) {
            Block thisBlock = state.getBlock();
            if (thisBlock == ModBlocks.LETTUCE_0.get()) {
                level.setBlock(pos, ModBlocks.LETTUCE_1.get().defaultBlockState(), 3);
            } else if (thisBlock == ModBlocks.LETTUCE_1.get()) {
                level.setBlock(pos, ModBlocks.LETTUCE_2.get().defaultBlockState(), 3);
            } else if (thisBlock == ModBlocks.LETTUCE_2.get()) {
                level.setBlock(pos, ModBlocks.LETTUCE_3.get().defaultBlockState(), 3);
            }
        } else {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }
}
