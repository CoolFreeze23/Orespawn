package danger.orespawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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
     * Unconditional bonus XP (orig OreRuby.java:26-30 / OreAmethyst.java:26-30 —
     * {@code 5 + nextInt(5) + nextInt(5)} on every break; no Y gate, unlike
     * uranium/titanium).
     *
     * <p>Implemented via NeoForge's {@code getExpDrop} because every 1.21.1
     * break path calls {@code spawnAfterBreak(..., dropExperience = false)}
     * unconditionally and sources break XP exclusively from this hook
     * (CommonHooks.handleBlockDrops; TF-022, TESTING_FINDINGS 2026-08-10 — the
     * previous spawnAfterBreak override never fired). Silk Touch drops no XP
     * because {@code EnchantmentHelper.processBlockExperience} applies the
     * enchantment's block_experience=0 effect to this value — the same net
     * semantics as the 1.7.10 silk-harvest path, which skipped
     * dropBlockAsItemWithChance.</p>
     */
    @Override
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                          net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                          Entity breaker, ItemStack tool) {
        RandomSource random = level.getRandom();
        return 5 + random.nextInt(5) + random.nextInt(5);
    }
}
