package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import danger.orespawn.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

/**
 * Crystal plant sapling that grows into a crystal tree on random tick.
 * Three variants exist for different crystal tree types.
 */
public class BlockCrystalPlant extends BushBlock {
    /** Growth attempt: nextInt(bound) == this index triggers growth. */
    private static final int GROWTH_ROLL_BOUND = 5;
    private static final int GROWTH_SUCCESS_INDEX = 1;
    private static final int ANIMATE_TICK_ROLL_BOUND = 30;
    private static final int ANIMATE_SUCCESS_INDEX = 1;
    private static final int HAPPY_VILLAGER_BURST_COUNT = 10;

    @Override
    protected MapCodec<? extends BlockCrystalPlant> codec() {
        return simpleCodec(BlockCrystalPlant::new);
    }

    public BlockCrystalPlant(BlockBehaviour.Properties properties) {
        this(properties, TreeType.TALL);
    }

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public enum TreeType { TALL, SCRAGGLY, BLUE }

    private final TreeType treeType;

    public BlockCrystalPlant(BlockBehaviour.Properties properties, TreeType treeType) {
        super(properties);
        this.treeType = treeType;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND
                || block == ModBlocks.CRYSTAL_GRASS.get();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;
        if (random.nextInt(GROWTH_ROLL_BOUND) != GROWTH_SUCCESS_INDEX) return;

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

        switch (treeType) {
            case TALL:
                growTallCrystalTree(level, pos, random);
                break;
            case SCRAGGLY:
                growScragglyCrystalTree(level, pos, random);
                break;
            case BLUE:
                growBlueCrystalTree(level, pos, random);
                break;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(ANIMATE_TICK_ROLL_BOUND) != ANIMATE_SUCCESS_INDEX) return;
        for (int p = 0; p < HAPPY_VILLAGER_BURST_COUNT; p++) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + random.nextFloat(),
                    pos.getZ() + random.nextFloat(),
                    0, 0, 0);
        }
    }

    private void growTallCrystalTree(ServerLevel level, BlockPos pos, RandomSource random) {
        generateCrystalTree(level, pos, random, ModBlocks.CRYSTAL_LEAVES.get(), 7 + random.nextInt(4));
    }

    private void growScragglyCrystalTree(ServerLevel level, BlockPos pos, RandomSource random) {
        generateCrystalTree(level, pos, random, ModBlocks.CRYSTAL_LEAVES_2.get(), 5 + random.nextInt(3));
    }

    private void growBlueCrystalTree(ServerLevel level, BlockPos pos, RandomSource random) {
        generateCrystalTree(level, pos, random, ModBlocks.CRYSTAL_LEAVES_3.get(), 7 + random.nextInt(4));
    }

    private void generateCrystalTree(ServerLevel level, BlockPos pos, RandomSource random, Block leavesBlock, int height) {
        BlockState logState = ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
        BlockState leafState = leavesBlock.defaultBlockState();

        for (int y = 0; y < height; y++) {
            BlockPos logPos = pos.above(y);
            if (level.isEmptyBlock(logPos) || level.getBlockState(logPos).canBeReplaced()) {
                level.setBlock(logPos, logState, 3);
            }
        }

        int leafStart = height - 3;
        for (int dy = leafStart; dy <= height + 1; dy++) {
            int radius = (dy <= height - 1) ? 2 : 1;
            if (dy == height + 1) radius = 0;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dz == 0 && dy < height) continue;
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextBoolean()) continue;
                    BlockPos leafPos = pos.above(dy).offset(dx, 0, dz);
                    if (level.isEmptyBlock(leafPos) || level.getBlockState(leafPos).canBeReplaced()) {
                        level.setBlock(leafPos, leafState, 3);
                    }
                }
            }
        }
    }
}
