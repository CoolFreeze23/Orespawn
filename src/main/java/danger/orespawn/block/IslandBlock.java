package danger.orespawn.block;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * orig IslandBlock.java (a BlockReed): the block the Islands dimension leaves on its grass, which makes a flying Island
 * or Island Too as it ticks. WGEN-118.
 *
 * <p>The reed's box (0.125 to 0.875 across, a block high) and no collision; it stays on a solid block
 * ({@code canBlockStay}: the block below's material solid), else it breaks and drops itself, as the reed's neighbour
 * check did. A random tick on the server ({@code updateTick}, the reed's growth replaced): 1 + nextInt(3) tries, each a
 * height 12 + nextInt(m) above the block (m 64, 55 at IslandSizeFactor 2, 45 at 1), and where the 21 x 21 layer at that
 * height is all air, one in 25 the Island and else the Island Too at the block's x and z and that height, facing
 * nextFloat() * 360; then the block and the one above it set to air. One display tick in 20, 20 happy-villager
 * particles in its cell.
 */
public class IslandBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public IslandBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        growIslands(level, pos);
    }

    /** orig IslandBlock.updateTick: the islands made, then the block gone. Public for the tests. */
    public static void growIslands(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int n = 1 + random.nextInt(3);
        int sizeFactor = OreSpawnConfig.ISLAND_SIZE_FACTOR.get();
        int m = sizeFactor == 2 ? 55 : sizeFactor == 1 ? 45 : 64;
        for (int i = 0; i < n; i++) {
            int height = 12 + random.nextInt(m);
            if (!airLayer(level, pos.getX(), pos.getY() + height, pos.getZ())) continue;
            EntityType<? extends Mob> type = random.nextInt(25) == 1 ? ModEntities.ISLAND.get() : ModEntities.ISLAND_TOO.get();
            Mob island = type.create(level);
            if (island == null) continue;
            island.moveTo(pos.getX(), pos.getY() + height, pos.getZ(), random.nextFloat() * 360.0F, 0.0F);
            island.finalizeSpawn(level, level.getCurrentDifficultyAt(island.blockPosition()), MobSpawnType.TRIGGERED, null);
            level.addFreshEntity(island);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
    }

    /** The original's check: the 21 x 21 layer at {@code y} about (x, z) all air. */
    private static boolean airLayer(ServerLevel level, int x, int y, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int k = -10; k <= 10; k++) {
            for (int j = -10; j <= 10; j++) {
                if (!level.getBlockState(cursor.set(x + j, y, z + k)).isAir()) return false;
            }
        }
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.random.nextInt(20) != 1) return;
        for (int j = 0; j < 20; j++) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + level.random.nextFloat(),
                    pos.getY() + level.random.nextFloat(), pos.getZ() + level.random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }
}
