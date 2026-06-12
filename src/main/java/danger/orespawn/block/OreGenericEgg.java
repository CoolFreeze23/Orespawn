package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 OreGenericEgg port — used by the Kraken/Dragon spawn-egg blocks and
 * the Ender-Pearl / Eye-of-Ender storage blocks (orig OreSpawnMain.java:1972-1973).
 *
 * <p>orig OreGenericEgg.java:24-30 ({@code func_149690_a}) — on break there is a
 * 50% chance to drop bonus <b>experience</b> (not extra items):</p>
 * <pre>
 *   int j1 = 5 + rand.nextInt(3) + rand.nextInt(3);  // 5..9 XP
 *   if (rand.nextInt(2) == 1) dropXpOnBlockBreak(j1);
 * </pre>
 *
 * <p>The previous port dropped 5..9 extra copies of the egg block instead —
 * an infinite-duplication exploit removed by ITEM-021. {@code dropExperience}
 * is false for Silk Touch harvests, matching the 1.7.10 silk-harvest path
 * which skipped dropBlockAsItemWithChance.</p>
 */
public class OreGenericEgg extends Block {

    public OreGenericEgg(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** 50% chance to pop 5..9 XP (orig OreGenericEgg.java:26-29). */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience && level.random.nextInt(2) == 1) {
            popExperience(level, pos, 5 + level.random.nextInt(3) + level.random.nextInt(3));
        }
    }
}
