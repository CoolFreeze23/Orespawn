package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND;
        // TODO: Also check CrystalGrass
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
        // TODO: Port TallCrystalTree generation logic
        // Places crystal tree log + crystal leaves in a tall columnar pattern
        // Needs ModBlocks.CRYSTAL_TREE_LOG and ModBlocks.CRYSTAL_LEAVES
    }

    private void growScragglyCrystalTree(ServerLevel level, BlockPos pos, RandomSource random) {
        // TODO: Port ScragglyCrystalTreeWithBranches generation logic
        // Places crystal tree log + crystal leaves2 in a branching pattern
    }

    private void growBlueCrystalTree(ServerLevel level, BlockPos pos, RandomSource random) {
        // TODO: Port TallCrystalTreeBlue generation logic
        // Places crystal tree log + crystal leaves3 in a tall columnar pattern
    }
}
