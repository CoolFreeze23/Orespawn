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
 * Reed-like quinoa plant with 4 growth stages (separate block variants).
 * AGE property tracks growth delay within a stage.
 */
public class BlockQuinoa extends BushBlock {
    @Override
    protected MapCodec<? extends BlockQuinoa> codec() {
        return simpleCodec(BlockQuinoa::new);
    }

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockQuinoa(BlockBehaviour.Properties properties) {
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
                || block instanceof BlockQuinoa
                || block == ModBlocks.CRYSTAL_GRASS.get();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;

        int age = state.getValue(AGE);
        if (age < 15) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            return;
        }

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) return;

        Block thisBlock = state.getBlock();
        if (thisBlock == ModBlocks.QUINOA_0.get()) {
            level.setBlock(above, ModBlocks.QUINOA_1.get().defaultBlockState(), 3);
        } else if (thisBlock == ModBlocks.QUINOA_1.get()) {
            level.setBlock(above, ModBlocks.QUINOA_2.get().defaultBlockState(), 3);
        } else if (thisBlock == ModBlocks.QUINOA_2.get()) {
            level.setBlock(above, ModBlocks.QUINOA_3.get().defaultBlockState(), 3);
        }
    }
}
