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
 * Reed-like multi-block corn stalk, ported from 1.7.10 BlockCorn (a BlockReed
 * subclass). Four block variants map to the original four registered blocks:
 * corn_0 = MyCornPlant1 (growing tip), corn_1 = MyCornPlant2 (immature stalk),
 * corn_2 = MyCornPlant3 (ripening), corn_3 = MyCornPlant4 (mature, drops cobs).
 *
 * <p>Growth (orig BlockCorn.java:35-82): only Plant1/Plant2 act on random
 * tick. While the stalk is shorter than a rolled height cap of
 * {@code 4 + rand.nextInt(4)} (4..7, orig :49), the column grows by placing a
 * new tip above and converting itself to immature stalk; once the cap is
 * reached, the lower stalk ripens (Plant2&rarr;Plant3&rarr;Plant4, orig
 * :65-73). Growth pauses (age counter) until {@code age >= 6 - maxHeight / 3}
 * (orig :60). Any Plant3/Plant4 found below pins the cap to the current
 * height so a ripened stalk never grows again (orig :54-59).</p>
 *
 * <p>Quirk preserved: the original wrote {@code myMaxHeight << 8} into block
 * metadata (orig :62-63, :79), but 1.7.10 metadata is a 4-bit nibble, so the
 * cap was never actually persisted and was re-rolled on every growth tick
 * (orig :48-50). We reproduce that by re-rolling each tick instead of storing
 * a property.</p>
 */
public class BlockCorn extends BushBlock {
    @Override
    protected MapCodec<? extends BlockCorn> codec() {
        return simpleCodec(BlockCorn::new);
    }

    // orig BlockCorn.java:45,47 — growth-delay counter stored in metadata (nibble, 0..15)
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public BlockCorn(BlockBehaviour.Properties properties) {
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
        // orig BlockCorn.java:27-33 — corn stalk blocks, grass, dirt or farmland
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND
                || isCornBlock(block);
    }

    private boolean isCornBlock(Block block) {
        return block instanceof BlockCorn;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /**
     * Original growth algorithm, orig BlockCorn.java:35-82. Acts only on the
     * tip (corn_0/Plant1) and immature stalk (corn_1/Plant2), and only when
     * the block above is air.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;

        Block thisBlock = state.getBlock();
        // orig BlockCorn.java:42-44 — only Plant1 and Plant2 grow
        if (thisBlock != ModBlocks.CORN_0.get() && thisBlock != ModBlocks.CORN_1.get()) return;

        // orig BlockCorn.java:48-50 — height cap 4 + nextInt(4); the <<8 metadata
        // store never survived the 1.7.10 nibble, so it is re-rolled every tick
        int maxHeight = 4 + random.nextInt(4);

        // orig BlockCorn.java:51 — only grow when the block above is air
        if (!level.isEmptyBlock(pos.above())) return;

        // orig BlockCorn.java:52-59 — count stalk height below (scan cap 10);
        // any ripening/mature block below freezes the cap at current height
        int height = 1;
        boolean dontGrow = false;
        for (int i = 1; i < 10; ++i) {
            Block below = level.getBlockState(pos.below(i)).getBlock();
            if (!isCornBlock(below)) break;
            ++height;
            if (below == ModBlocks.CORN_2.get() || below == ModBlocks.CORN_3.get()) {
                dontGrow = true;
            }
        }
        if (dontGrow) {
            maxHeight = height;
        }

        int age = state.getValue(AGE);
        // orig BlockCorn.java:60 — grow once age reaches 6 - maxHeight/3 (4..5)
        if (age >= 6 - maxHeight / 3) {
            if (height < maxHeight) {
                // orig BlockCorn.java:61-63 — new tip above, self becomes immature stalk
                level.setBlock(pos.above(), ModBlocks.CORN_0.get().defaultBlockState(), 2);
                level.setBlock(pos, ModBlocks.CORN_1.get().defaultBlockState(), 2);
            } else {
                // orig BlockCorn.java:64-76 — ripen the stalk below (skips i=0, stops
                // before maxHeight-1): Plant2->Plant3, Plant3->Plant4; reset own age
                for (int i = 1; i < maxHeight - 1; ++i) {
                    BlockPos lower = pos.below(i);
                    Block below = level.getBlockState(lower).getBlock();
                    if (below == ModBlocks.CORN_1.get()) {
                        level.setBlock(lower, ModBlocks.CORN_2.get().defaultBlockState(), 2);
                    } else if (below == ModBlocks.CORN_2.get()) {
                        level.setBlock(lower, ModBlocks.CORN_3.get().defaultBlockState(), 2);
                    }
                }
                level.setBlock(pos, state.setValue(AGE, 0), 2);
            }
        } else {
            // orig BlockCorn.java:77-80 — not ready yet, bump the age counter
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }
}
