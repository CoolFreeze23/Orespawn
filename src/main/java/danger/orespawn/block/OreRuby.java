package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.7.10 OreRuby / OreAmethyst port — plain overworld gem ore. Never
 * explodes (only the Crystal Dimension's volatile ores do); always drops
 * bonus XP on break.
 *
 * <p>orig OreRuby.java:26-30 / OreAmethyst.java:26-30 —
 * {@code int j1 = 5 + rand.nextInt(5) + rand.nextInt(5);} (5..13 XP,
 * triangular), dropped unconditionally (no Y gate, unlike uranium/titanium).</p>
 */
public class OreRuby extends Block {

    public OreRuby(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Drops the original bonus XP. {@code dropExperience} is false for
     * Silk Touch harvests, matching 1.7.10 where dropBlockAsItemWithChance
     * (the XP path) was skipped when silk-harvesting.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (dropExperience) {
            // orig OreRuby.java:28 — 5 + nextInt(5) + nextInt(5)
            popExperience(level, pos, 5 + level.random.nextInt(5) + level.random.nextInt(5));
        }
    }
}
