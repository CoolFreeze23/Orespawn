package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModBlocks;

/**
 * OreSpawn flower that transforms between day and night variants.
 * Pink ↔ Black, Blue ↔ Scary based on time of day.
 */
public class MyBlockFlower extends BushBlock {

    public enum FlowerVariant { PINK, BLACK, BLUE, SCARY, OTHER }

    private final FlowerVariant variant;

    public MyBlockFlower(BlockBehaviour.Properties properties, FlowerVariant variant) {
        super(properties);
        this.variant = variant;
    }

    public MyBlockFlower(BlockBehaviour.Properties properties) {
        this(properties, FlowerVariant.OTHER);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return variant == FlowerVariant.PINK || variant == FlowerVariant.BLACK
                || variant == FlowerVariant.BLUE || variant == FlowerVariant.SCARY;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) return;

        long t = level.getDayTime() % 24000L;

        if (t > 12000L) {
            // Night time
            // TODO: Uncomment when ModBlocks has flower registrations
            // if (variant == FlowerVariant.PINK)
            //     level.setBlock(pos, ModBlocks.FLOWER_BLACK.get().defaultBlockState(), 2);
            // if (variant == FlowerVariant.BLUE)
            //     level.setBlock(pos, ModBlocks.FLOWER_SCARY.get().defaultBlockState(), 2);
        } else {
            // Day time
            // if (variant == FlowerVariant.BLACK)
            //     level.setBlock(pos, ModBlocks.FLOWER_PINK.get().defaultBlockState(), 2);
            // if (variant == FlowerVariant.SCARY)
            //     level.setBlock(pos, ModBlocks.FLOWER_BLUE.get().defaultBlockState(), 2);
        }
    }
}
