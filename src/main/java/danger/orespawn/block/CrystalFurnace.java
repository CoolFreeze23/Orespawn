package danger.orespawn.block;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.gui.CrystalFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

public class CrystalFurnace extends BaseEntityBlock {
    private static final double PARTICLE_Y_SPREAD = 6.0 / 16.0;
    private static final double FURNACE_FACE_OUTSET = 0.52;
    private static final double SIDE_JITTER_RANGE = 0.3;

    @Override
    protected MapCodec<? extends CrystalFurnace> codec() {
        return simpleCodec(CrystalFurnace::new);
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CrystalFurnace(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrystalFurnaceBlockEntity furnace) {
                furnace.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CrystalFurnaceBlockEntity furnace) {
            player.openMenu(furnace);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        Direction direction = state.getValue(FACING);
        double centerX = pos.getX() + 0.5;
        double particleY = pos.getY() + random.nextFloat() * PARTICLE_Y_SPREAD;
        double centerZ = pos.getZ() + 0.5;
        double sideOffset = random.nextFloat() * 0.6 - SIDE_JITTER_RANGE;

        switch (direction) {
            case WEST -> {
                level.addParticle(ParticleTypes.SMOKE, centerX - FURNACE_FACE_OUTSET, particleY, centerZ + sideOffset, 0, 0, 0);
                level.addParticle(ParticleTypes.FLAME, centerX - FURNACE_FACE_OUTSET, particleY, centerZ + sideOffset, 0, 0, 0);
            }
            case EAST -> {
                level.addParticle(ParticleTypes.SMOKE, centerX + FURNACE_FACE_OUTSET, particleY, centerZ + sideOffset, 0, 0, 0);
                level.addParticle(ParticleTypes.FLAME, centerX + FURNACE_FACE_OUTSET, particleY, centerZ + sideOffset, 0, 0, 0);
            }
            case NORTH -> {
                level.addParticle(ParticleTypes.SMOKE, centerX + sideOffset, particleY, centerZ - FURNACE_FACE_OUTSET, 0, 0, 0);
                level.addParticle(ParticleTypes.FLAME, centerX + sideOffset, particleY, centerZ - FURNACE_FACE_OUTSET, 0, 0, 0);
            }
            case SOUTH -> {
                level.addParticle(ParticleTypes.SMOKE, centerX + sideOffset, particleY, centerZ + FURNACE_FACE_OUTSET, 0, 0, 0);
                level.addParticle(ParticleTypes.FLAME, centerX + sideOffset, particleY, centerZ + FURNACE_FACE_OUTSET, 0, 0, 0);
            }
            default -> {}
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrystalFurnaceBlockEntity furnace) {
                Containers.dropContents(level, pos, furnace);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.CRYSTAL_FURNACE_BE.get(), CrystalFurnaceBlockEntity::serverTick);
    }
}
