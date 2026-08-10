package danger.orespawn.block;

import net.minecraft.core.BlockPos;
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

    /**
     * 50% chance to pop 5..9 XP (orig OreGenericEgg.java:26-29).
     *
     * <p>Implemented via NeoForge's {@code getExpDrop} because every 1.21.1
     * break path calls {@code spawnAfterBreak(..., dropExperience = false)}
     * unconditionally and sources break XP exclusively from this hook
     * (CommonHooks.handleBlockDrops; TESTING_FINDINGS 2026-08-10 — the
     * previous spawnAfterBreak override never fired). Silk Touch drops no XP
     * because {@code EnchantmentHelper.processBlockExperience} applies the
     * enchantment's block_experience=0 effect to this value — the same net
     * semantics as the 1.7.10 silk-harvest path.</p>
     */
    @Override
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelAccessor level, BlockPos pos,
                          net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                          net.minecraft.world.entity.Entity breaker, ItemStack tool) {
        var random = level.getRandom();
        if (random.nextInt(2) == 1) {
            return 5 + random.nextInt(3) + random.nextInt(3);
        }
        return 0;
    }
}
