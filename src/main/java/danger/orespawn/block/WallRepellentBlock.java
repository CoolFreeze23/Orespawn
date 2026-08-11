package danger.orespawn.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Wall-mounted form of {@link RepellentBlock} — the modern half of the 1.7.10
 * torch-metadata wall placement.
 *
 * <p>Orig KrakenRepellent.java:21-22 / CreeperRepellent.java:22-23 extend
 * {@code BlockTorch}, whose metadata 1-4 attached the repellent to a wall
 * exactly like a vanilla torch (the orig particle code at
 * KrakenRepellent.java:35-51 / CreeperRepellent.java:36-52 branches on those
 * four wall metas). The modern split mirrors vanilla TORCH/WALL_TORCH: this
 * block is never in the creative menu or loot as itself — the shared
 * {@code StandingAndWallBlockItem} in ModItems places it, and its loot table
 * drops the standing repellent's item. All repel behavior (radius, cadence,
 * push, target sets) is inherited untouched from {@link RepellentBlock}.</p>
 */
public class WallRepellentBlock extends RepellentBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Vanilla WallTorchBlock shapes — the direction the torch leans away from the wall.
    private static final VoxelShape NORTH_SHAPE = Block.box(5.5, 3.0, 11.0, 10.5, 13.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.box(5.5, 3.0, 0.0, 10.5, 13.0, 5.0);
    private static final VoxelShape WEST_SHAPE = Block.box(11.0, 3.0, 5.5, 16.0, 13.0, 10.5);
    private static final VoxelShape EAST_SHAPE = Block.box(0.0, 3.0, 5.5, 5.0, 13.0, 10.5);

    // orig KrakenRepellent.java:33-34 / CreeperRepellent.java:34-35 — wall-meta
    // particles sit 0.271 toward the wall and 0.413 above the torch-top baseline.
    private static final double WALL_PARTICLE_HORIZONTAL = 0.271;
    private static final double WALL_PARTICLE_UP = 0.413;

    public WallRepellentBlock(BlockBehaviour.Properties properties, Variant variant) {
        super(properties, variant);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends WallRepellentBlock> codec() {
        return simpleCodec(p -> new WallRepellentBlock(p, Variant.KRAKEN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Vanilla WallTorchBlock shape for the attached direction. */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    /** Vanilla WallTorchBlock — needs a sturdy face on the wall behind it. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos wallPos = pos.relative(facing.getOpposite());
        BlockState wallState = level.getBlockState(wallPos);
        return wallState.isFaceSturdy(level, wallPos, facing);
    }

    /** Vanilla WallTorchBlock — first survivable horizontal facing along the look vector, else null. */
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                state = state.setValue(FACING, direction.getOpposite());
                if (state.canSurvive(level, pos)) {
                    return state;
                }
            }
        }
        return null;
    }

    /** Pop off (dropping via the wall loot table) when the supporting wall goes away. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * orig KrakenRepellent.java:35-51 / CreeperRepellent.java:36-52 — wall metas
     * 1-4 emit the smoke/flame/reddust triple offset 0.271 toward the supporting
     * wall and 0.413 above the y+0.7 baseline.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction toWall = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5 + WALL_PARTICLE_HORIZONTAL * toWall.getStepX();
        double y = pos.getY() + 0.7 + WALL_PARTICLE_UP;
        double z = pos.getZ() + 0.5 + WALL_PARTICLE_HORIZONTAL * toWall.getStepZ();
        addRepellentParticles(level, x, y, z);
    }
}
