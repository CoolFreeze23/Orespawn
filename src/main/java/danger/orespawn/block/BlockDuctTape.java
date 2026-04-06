package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class BlockDuctTape extends Block {
    public static final IntegerProperty USES = IntegerProperty.create("uses", 0, 5);
    private static final int MAX_USES_BEFORE_GONE = 6;
    private static final int REPAIR_FRACTION_DIVISOR = 6;
    private static final int MIN_REPAIR_AMOUNT = 1;
    private static final float TAPE_HEIGHT = 4;
    private static final double EDGE_INSET = 1;

    private static final VoxelShape[] SHAPES = new VoxelShape[6];
    static {
        for (int useIndex = 0; useIndex <= 5; useIndex++) {
            float minXInSixteenths = (1 + useIndex * 2) / 16.0f;
            SHAPES[useIndex] = Block.box(minXInSixteenths * 16, 0, EDGE_INSET, 16 - EDGE_INSET, TAPE_HEIGHT, 16 - EDGE_INSET);
        }
    }

    public BlockDuctTape(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(USES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(USES);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(USES)];
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            repairItem(level, pos, state, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void repairItem(Level level, BlockPos pos, BlockState state, Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || held.getCount() != 1) return;

        int maxDamage = held.getMaxDamage();
        if (maxDamage <= 0) return;

        int repairAmount = maxDamage / REPAIR_FRACTION_DIVISOR;
        if (repairAmount < MIN_REPAIR_AMOUNT) repairAmount = MIN_REPAIR_AMOUNT;

        int currentDamage = held.getDamageValue();
        if (currentDamage <= 0) return;

        int newDamage = Math.max(0, currentDamage - repairAmount);
        held.setDamageValue(newDamage);

        int uses = state.getValue(USES) + 1;
        if (uses >= MAX_USES_BEFORE_GONE) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(USES, uses), 2);
        }
    }
}
