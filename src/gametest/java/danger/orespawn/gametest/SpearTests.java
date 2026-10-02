package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import danger.orespawn.ModCreativeTabs;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.data.ModernCondition;
import danger.orespawn.item.SpearTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MOD-043: the spears of the eight tool tiers, on Mounts of Mayhem's spear. The suite runs without that mod, so it
 * checks the rule's table and the spears' absence (no item, no recipe, none in the creative tab); run with
 * -PgametestMayhem (the mod on the run's classpath, these rows alone), the same rows check the spears themselves: their
 * durability and attack values, their hold, their repair, their enchantments, their recipes, tags and tab, and one more
 * row, generated only while the mod is loaded, their wear. Nothing here names the mod's classes: the items are read
 * through vanilla's item API. Their templates are the port's empty ones, under this namespace.
 */
@GameTestHolder(SpearTests.NAMESPACE)
@PrefixGameTestTemplate(false)
public class SpearTests {
    /** The spears' rows run in a test namespace of their own, so -PgametestMayhem can run them alone. */
    static final String NAMESPACE = "orespawn_spears";


    private static boolean mayhem() {
        return ModList.get().isLoaded("mounts_of_mayhem");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    /**
     * The table follows the rule: durability the tier's uses, melee the attack bonus + 1 (also the charge's base), and
     * the mod's row for the vanilla tier at the tier's mining level (multiplier, vehicle bonus, hold, reload).
     */
    @GameTest(template = "empty")
    public static void mod043a_each_spear_tier_follows_the_rule(GameTestHelper helper) {
        Object[][] expected = {
                {SpearTier.RUBY, "ruby_spear", 1500, 17.0, SpearTier.Row.DIAMOND},
                {SpearTier.AMETHYST, "amethyst_spear", 2000, 12.0, SpearTier.Row.DIAMOND},
                {SpearTier.EMERALD, "emerald_spear", 1300, 7.0, SpearTier.Row.DIAMOND},
                {SpearTier.ULTIMATE, "ultimate_spear", 3000, 37.0, SpearTier.Row.NETHERITE},
                {SpearTier.CRYSTAL_PINK, "crystal_pink_spear", 1100, 8.0, SpearTier.Row.IRON},
                {SpearTier.TIGERS_EYE, "tigers_eye_spear", 1600, 9.0, SpearTier.Row.IRON},
                {SpearTier.CRYSTAL_WOOD, "crystal_wood_spear", 300, 3.0, SpearTier.Row.WOODEN},
                {SpearTier.CRYSTAL_STONE, "crystal_stone_spear", 800, 6.0, SpearTier.Row.STONE}};
        helper.assertValueEqual(SpearTier.values().length, expected.length, "spear tiers");
        for (Object[] row : expected) {
            SpearTier tier = (SpearTier) row[0];
            helper.assertValueEqual(tier.id(), row[1], tier + "'s id");
            helper.assertValueEqual(tier.durability(), row[2], tier + "'s durability");
            helper.assertTrue(tier.melee() == (double) row[3], tier + "'s melee " + tier.melee() + ", not " + row[3]);
            helper.assertTrue(tier.row() == row[4], tier + "'s row " + tier.row() + ", not " + row[4]);
        }
        double[][] rows = {{1.0, 1.0, 150, 40}, {2.0, 1.1, 130, 40}, {3.0, 1.25, 85, 40}, {4.0, 1.4, 50, 45},
                {5.0, 1.5, 40, 50}};
        for (SpearTier.Row row : SpearTier.Row.values()) {
            double[] want = rows[row.ordinal()];
            helper.assertTrue(row.multiplier() == want[0] && row.vehicleBonus() == want[1]
                            && row.holdTicks() == (int) want[2] && row.reloadTicks() == (int) want[3],
                    row + "'s charge values against the mod's row");
            helper.assertValueEqual(row.model(), "mounts_of_mayhem:custom/" + row.name().toLowerCase(Locale.ROOT) + "_spear",
                    row + "'s model");
        }
        helper.assertTrue(SpearTier.ATTACK_SPEED == -2.8, "the spears' attack speed");
        helper.succeed();
    }

    /**
     * The spears exist exactly while Mounts of Mayhem is loaded. Without it: no item, no recipe, none in the creative
     * tab. With it: each item at its tier's durability and attack values, holding a charge for its row's ticks,
     * repaired with its tier's material, in both spear tags, taking Unbreaking and Mending but not the mod's Lunge (which
     * the mod's own spear takes), made by its recipe.
     */
    @GameTest(template = "empty")
    public static void mod043b_the_spears_exist_exactly_with_mounts_of_mayhem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean loaded = mayhem();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var enchantments = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> unbreaking = enchantments.getHolderOrThrow(Enchantments.UNBREAKING);
        Holder<Enchantment> mending = enchantments.getHolderOrThrow(Enchantments.MENDING);
        Holder<Enchantment> lunge = loaded ? enchantments.getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "lunge"))) : null;
        if (loaded) {
            ItemStack theirs = new ItemStack(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("mounts_of_mayhem", "diamond_spear")));
            helper.assertTrue(theirs.supportsEnchantment(lunge) && theirs.supportsEnchantment(unbreaking),
                    "the mod's diamond spear does not take Lunge and Unbreaking, so the rows below prove nothing");
        }
        for (SpearTier tier : SpearTier.values()) {
            var item = BuiltInRegistries.ITEM.getOptional(id(tier.id()));
            var recipe = level.getRecipeManager().byKey(id(tier.id()));
            if (!loaded) {
                helper.assertTrue(item.isEmpty(), tier.id() + " is registered without Mounts of Mayhem");
                helper.assertTrue(recipe.isEmpty(), tier.id() + "'s recipe loaded without Mounts of Mayhem");
                continue;
            }
            helper.assertTrue(item.isPresent(), tier.id() + " is not registered with Mounts of Mayhem loaded");
            ItemStack stack = new ItemStack(item.get());
            helper.assertValueEqual(stack.getMaxDamage(), tier.durability(), tier.id() + "'s durability");
            ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            helper.assertTrue(amount(modifiers, Attributes.ATTACK_DAMAGE.value()) == tier.melee()
                            && amount(modifiers, Attributes.ATTACK_SPEED.value()) == SpearTier.ATTACK_SPEED,
                    tier.id() + "'s attack modifiers " + modifiers);
            helper.assertValueEqual(item.get().getUseDuration(stack, player), tier.row().holdTicks(),
                    tier.id() + "'s hold");
            Ingredient repair = tier.tier().getRepairIngredient();
            helper.assertTrue(repair.getItems().length > 0 && item.get().isValidRepairItem(stack, repair.getItems()[0])
                            && !item.get().isValidRepairItem(stack, new ItemStack(Items.STICK)),
                    tier.id() + "'s repair material");
            helper.assertTrue(stack.is(ItemTags.create(ResourceLocation.withDefaultNamespace("spears")))
                            && stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("bettercombat", "spear")))
                            && stack.is(ItemTags.DURABILITY_ENCHANTABLE),
                    tier.id() + " is not in both spear tags and enchantable/durability");
            helper.assertTrue(stack.supportsEnchantment(unbreaking) && stack.supportsEnchantment(mending)
                            && !stack.supportsEnchantment(lunge),
                    tier.id() + " does not take Unbreaking and Mending, or takes Lunge");
            helper.assertTrue(recipe.isPresent() && recipe.get().value().getResultItem(level.registryAccess()).is(item.get()),
                    tier.id() + " has no recipe making it");
        }
        helper.succeed();
    }

    private static double amount(ItemAttributeModifiers modifiers, Object attribute) {
        double sum = 0.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().value() == attribute && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                sum += entry.modifier().amount();
            }
        }
        return sum;
    }

    /**
     * With Mounts of Mayhem, a melee hit wears a spear a point. The row is generated only while the mod is loaded:
     * without it there is no spear to hit with, and mod043b already checks that absence.
     */
    @GameTestGenerator
    public static Collection<TestFunction> mod043cRows() {
        if (!mayhem()) {
            return List.of();
        }
        return List.of(new TestFunction("defaultBatch", "mod043c_a_melee_hit_wears_a_spear_a_point",
                NAMESPACE + ":empty_large", Rotation.NONE, 40, 0L, true, SpearTests::meleeHitWearsASpear));
    }

    private static void meleeHitWearsASpear(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Pig target = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(3, 2, 3));
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id(SpearTier.RUBY.id())));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        boolean hit = stack.hurtEnemy(target, player);
        if (hit) {
            stack.postHurtEnemy(target, player);
        }
        helper.assertTrue(hit && stack.getDamageValue() == 1,
                "a melee hit left the ruby spear at damage " + stack.getDamageValue() + " (hit " + hit + ")");
        target.discard();
        helper.succeed();
    }

    /**
     * The load condition reads modern.enabled and modern.spears together; the equipment tab lists the registered
     * spears right after the wolf armour (the wolf armour held on around it) while the feature is on, none while it is
     * off (and none at all without Mounts of Mayhem).
     */
    @GameTest(template = "empty")
    public static void mod043d_the_condition_and_the_creative_tab_follow_the_feature(GameTestHelper helper) {
        ICondition condition = new ModernCondition("spears");
        ServerLevel level = helper.getLevel();
        CreativeModeTab tab = ModCreativeTabs.EQUIPMENT_TAB.get();
        CreativeModeTab.ItemDisplayParameters parameters =
                new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), false, level.registryAccess());
        final boolean master = OreSpawnConfig.MODERN_ENABLED.get();
        final boolean key = OreSpawnConfig.MODERN_SPEARS.get();
        final boolean wolves = OreSpawnConfig.MODERN_WOLF_ARMOUR.get();
        Item lastWolfArmour = ModItems.WOLF_ARMOR.get(ModItems.WOLF_ARMOR.size() - 1).get();
        List<Item> registered = SpearTier.registeredItems();
        boolean on, keyOff, masterOff;
        List<Item> shownOn;
        List<Item> shownOff;
        List<Item> afterWolves = new ArrayList<>();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_SPEARS.set(true);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(true);
            on = condition.test(ICondition.IContext.EMPTY);
            tab.buildContents(parameters);
            shownOn = spearsIn(tab);
            List<ItemStack> all = new ArrayList<>(tab.getDisplayItems());
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).is(lastWolfArmour)) {
                    for (int j = i + 1; j < Math.min(all.size(), i + 1 + registered.size()); j++) {
                        afterWolves.add(all.get(j).getItem());
                    }
                    break;
                }
            }
            OreSpawnConfig.MODERN_SPEARS.set(false);
            keyOff = condition.test(ICondition.IContext.EMPTY);
            tab.buildContents(parameters);
            shownOff = spearsIn(tab);
            OreSpawnConfig.MODERN_SPEARS.set(true);
            OreSpawnConfig.MODERN_ENABLED.set(false);
            masterOff = condition.test(ICondition.IContext.EMPTY);
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.MODERN_SPEARS.set(key);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(wolves);
            tab.buildContents(parameters);
        }
        helper.assertTrue(on && !keyOff && !masterOff,
                "the condition reads master and key: on " + on + ", key off " + keyOff + ", master off " + masterOff);
        helper.assertTrue(shownOn.equals(registered),
                "with the feature on the tab lists " + shownOn.size() + " spears, not the " + registered.size()
                        + " registered (" + (mayhem() ? "8 with" : "none without") + " Mounts of Mayhem)");
        helper.assertTrue(afterWolves.equals(registered),
                "the spears do not follow the Lapis wolf armour, the wolf armour's last: " + afterWolves);
        helper.assertTrue(shownOff.isEmpty(), "with the feature off the tab still lists " + shownOff.size() + " spears");
        helper.succeed();
    }

    private static List<Item> spearsIn(CreativeModeTab tab) {
        List<Item> out = new ArrayList<>();
        for (ItemStack stack : tab.getDisplayItems()) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key.getNamespace().equals(OreSpawnMod.MOD_ID) && key.getPath().endsWith("_spear")) {
                out.add(stack.getItem());
            }
        }
        return out;
    }
}
