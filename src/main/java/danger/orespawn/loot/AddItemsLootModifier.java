package danger.orespawn.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.function.Supplier;

/**
 * Chest-injection GLM replacing the 1.7.10 ChestGenHooks additions
 * (TF-009/TF-010; orig OreSpawnMain.java:5391-5402):
 * <pre>
 *   dungeonChest        : ruby (1, w3), amethyst (1, w3), thunder staff (1, w2)   OSM:5391-5394
 *   pyramidJungleChest  : ruby (1, w3), amethyst (1, w3), ant robot kit (1, w3)   OSM:5395-5398
 *   pyramidDesertyChest : ruby (1, w2), amethyst (1, w2), spider robot kit (1, w2) OSM:5399-5402
 * </pre>
 * 1.7.10 constructor semantics are {@code WeightedRandomChestContent(stack, minStack,
 * maxStack, weight)} (see phase_d_reports/d6_extraction/trees_spec.md:243), so every
 * original entry is {@code min=max=1}: exactly ONE item per weighted pick — the weight
 * (3 or 2) is the pick weight, not a stack size. The JSONs therefore all declare
 * {@code min_count=max_count=1}; the range support below exists so the codec can
 * faithfully express any WeightedRandomChestContent min/max if ever needed.
 *
 * <p><b>Chance derivation</b> (weighted-pool -> per-roll chance). 1.7.10 filled these
 * chests with N weighted picks (with replacement) from the vanilla pool plus OreSpawn's
 * added weights; a modern chest loot table is rolled once per chest, and this modifier
 * fires once per roll adding one stack with probability {@code chance}. Matching the
 * original expected stacks per chest, E = N * w / T':
 * <pre>
 *   dungeonChest: vanilla pool 119 (WorldGenDungeons 15 entries: 9x w10 saddle/iron/
 *     bread/wheat/gunpowder/string/bucket/redstone/name_tag + records 13/cat w10 each
 *     + golden_apple w1 + horse armors w2/w5/w1) + Forge's random-enchanted-book
 *     entry w1 = 120; N = 8 (ChestGenHooks DUNGEON_CHEST count 8-8).
 *     T' = 120 + 3 + 3 + 2 = 128
 *       ruby = amethyst   : 8 * 3 / 128 = 0.1875
 *       thunder staff     : 8 * 2 / 128 = 0.125
 *   pyramidJungleChest: vanilla pool 72 (diamond w3, iron w10, gold w15, emerald w2,
 *     bone w20, rotten_flesh w16, saddle w3, horse armors w1 x3); N = 2-7 avg 4.5
 *     (ChestGenHooks PYRAMID_JUNGLE_CHEST count 2-7). T' = 72 + 3 + 3 + 3 = 81
 *       ruby = amethyst = ant robot kit : 4.5 * 3 / 81 = 1/6 ~ 0.1667
 *   pyramidDesertyChest: same vanilla 72-weight pool; N = 2-7 avg 4.5.
 *     T' = 72 + 2 + 2 + 2 = 78
 *       ruby = amethyst = spider robot kit : 4.5 * 2 / 78 = 3/26 ~ 0.1154
 * </pre>
 * Ruby and amethyst are split into PER-TABLE modifiers
 * ({@code add_ruby_to_simple_dungeon} etc., batch-verify refinement 2026-08-10)
 * so every table carries its exact target chance — 0.1875 / 0.1667 / 0.1154 —
 * rather than a shared mean; the three single-item modifiers likewise use their
 * exact values (0.125 / 0.1667 / 0.1154). Approximation note: expectation is
 * matched per table; variance is not (1.7.10 could pick the same entry twice in
 * one chest; a GLM adds at most one stack per chest).
 *
 * <p><b>Single chance application</b> (TF-009 double-roll fix): the coded {@code chance}
 * field in {@link #doApply} is the ONE roll; the former {@code minecraft:random_chance}
 * JSON conditions were removed, and the JSON conditions now carry only the
 * {@code neoforge:loot_table_id} scoping that restores the original's
 * dungeon/jungle/desert-chest-only injection.
 */
public class AddItemsLootModifier extends LootModifier {
    public static final Supplier<MapCodec<AddItemsLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(
                    instance.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item),
                            Codec.INT.fieldOf("min_count").forGetter(m -> m.minCount),
                            Codec.INT.fieldOf("max_count").forGetter(m -> m.maxCount),
                            Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance)
                    )
            ).apply(instance, AddItemsLootModifier::new))
    );

    private final Item item;
    private final int minCount;
    private final int maxCount;
    private final float chance;

    public AddItemsLootModifier(LootItemCondition[] conditions, Item item, int minCount, int maxCount, float chance) {
        super(conditions);
        this.item = item;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < chance) {
            int count = maxCount > minCount
                    ? minCount + context.getRandom().nextInt(maxCount - minCount + 1)
                    : minCount;
            generatedLoot.add(new ItemStack(item, count));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
