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

    private static final VoxelShape[] SHAPES = new VoxelShape[6];
    static {
        for (int i = 0; i <= 5; i++) {
            float f1 = (1 + i * 2) / 16.0f;
            SHAPES[i] = Block.box(f1 * 16, 0, 1, 16 - 1, 4, 16 - 1);
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

        int repairAmount = maxDamage / 6;
        if (repairAmount < 1) repairAmount = 1;

        int currentDamage = held.getDamageValue();
        if (currentDamage <= 0) return;

        int newDamage = Math.max(0, currentDamage - repairAmount);
        held.setDamageValue(newDamage);

        int uses = state.getValue(USES) + 1;
        if (uses >= 6) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(USES, uses), 2);
        }
    }
}
