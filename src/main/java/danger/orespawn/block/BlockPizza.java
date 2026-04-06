package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPizza extends Block {
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 5);

    private static final VoxelShape[] SHAPES = new VoxelShape[6];
    static {
        for (int i = 0; i <= 5; i++) {
            float f1 = (1 + i * 2) / 16.0f;
            SHAPES[i] = Block.box(f1 * 16, 0, 1, 16 - 1, 4, 16 - 1);
        }
    }

    public BlockPizza(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(BITES)];
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            eatPizzaSlice(level, pos, state, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void eatPizzaSlice(Level level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) return;

        player.getFoodData().eat(4, 0.2f);

        int bites = state.getValue(BITES) + 1;
        if (bites >= 6) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(BITES, bites), 2);
        }
    }
}
