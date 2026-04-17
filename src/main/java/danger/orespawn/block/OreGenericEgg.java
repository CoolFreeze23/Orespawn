package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 OreGenericEgg port. The Kraken/Dragon "ancient dried spawn egg" blocks
 * use this class so a single broken block has a 50% chance to drop an extra
 * 5–11 copies of itself, which is how 1.7.10 made the rare deep-cave veins
 * yield enough eggs to actually craft the boss-summon altars.
 *
 * <p>1.7.10 logic from {@code OreGenericEgg.func_149690_a}:</p>
 * <pre>
 *   int j1 = 5 + rand.nextInt(3) + rand.nextInt(3);  // 5..11 inclusive
 *   if (rand.nextInt(2) == 1) dropMore(j1);
 * </pre>
 *
 * <p>1.21.1 paradigm note: vanilla loot tables can express weighted bonus rolls
 * but not "self-multiplying drop based on a 50% gate", so we override
 * {@link #playerWillDestroy} and inject the bonus drops via
 * {@link Block#popResource}. The block's own loot table still controls the
 * guaranteed single self-drop, so silk-touch and fortune behave as expected on
 * the base item.</p>
 */
public class OreGenericEgg extends Block {

    public OreGenericEgg(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative() && level instanceof ServerLevel sl) {
            if (sl.random.nextInt(2) == 1) {
                int extra = 5 + sl.random.nextInt(3) + sl.random.nextInt(3);
                Block.popResource(sl, pos, new ItemStack(this.asItem(), extra));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
