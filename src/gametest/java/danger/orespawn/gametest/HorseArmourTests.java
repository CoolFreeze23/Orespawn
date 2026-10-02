package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import danger.orespawn.ModCreativeTabs;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.data.ModernCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MOD-040: horse armour for the 14 armour sets. Each is vanilla's horse armour (an equestrian AnimalArmorItem) on its
 * set's armour material: the chestplate's armour value as its own (the material's body value), the material's toughness
 * and knockback resistance, no durability, one to a stack, drawn from textures/entity/horse/armor/horse_armor_<material>.
 * modern.horseArmour (with modern.enabled) loads the 13 recipes and the King's drop of the Royal Guardian one (the
 * {@code orespawn:modern} load condition) and fills the creative tab; the King's challenge chest is checked in
 * StructureTestsA's King tower row.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HorseArmourTests {

    /** The sets in the armour's order, with each one's armour material name. */
    private static final String[][] SETS = {
            {"ultimate", "ultimate"}, {"royal", "royal"}, {"queen", "queen"}, {"mobzilla", "mobzilla"},
            {"experience", "experience"}, {"ruby", "ruby"}, {"amethyst", "amethyst"}, {"emerald", "emerald"},
            {"lavaeel", "lava_eel"}, {"mothscale", "moth_scale"}, {"peacock", "peacock"}, {"pink", "pink"},
            {"tigerseye", "tigers_eye"}, {"lapis", "lapis"}};

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(id(path));
    }

    /**
     * Each horse armour is an equestrian AnimalArmorItem on its set's material: the chestplate's armour value,
     * toughness and knockback resistance, no durability, one to a stack, its texture where the game's horse armour
     * layer looks for the material; the list holds the 14 in the armour's order.
     */
    @GameTest(template = "empty")
    public static void mod040a_each_horse_armour_is_its_sets_material_on_a_horse(GameTestHelper helper) {
        helper.assertValueEqual(ModItems.HORSE_ARMOR.size(), SETS.length, "horse armours listed");
        for (int i = 0; i < SETS.length; i++) {
            String set = SETS[i][0];
            Item item = item(set + "_horse_armor");
            helper.assertTrue(item == ModItems.HORSE_ARMOR.get(i).get(), set + "_horse_armor is not the list's entry " + i);
            helper.assertTrue(item instanceof AnimalArmorItem animal
                            && animal.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN,
                    set + "_horse_armor is not an equestrian AnimalArmorItem");
            AnimalArmorItem armour = (AnimalArmorItem) item;
            ArmorItem chest = (ArmorItem) item(set + "_chestplate");
            helper.assertTrue(armour.getMaterial().is(chest.getMaterial().unwrapKey().orElseThrow()),
                    set + ": the horse armour's material is not the chestplate's");
            helper.assertValueEqual(armour.getMaterial().unwrapKey().orElseThrow().location(), id(SETS[i][1]),
                    set + "'s armour material");
            helper.assertValueEqual(armour.getDefense(), chest.getDefense(), set + ": armour value against the chestplate's");
            helper.assertTrue(armour.getToughness() == chest.getToughness(),
                    set + ": toughness " + armour.getToughness() + " against the chestplate's " + chest.getToughness());
            helper.assertTrue(armour.getMaterial().value().knockbackResistance() == chest.getMaterial().value().knockbackResistance(),
                    set + ": knockback resistance against the chestplate's");
            ItemStack stack = new ItemStack(item);
            helper.assertValueEqual(stack.getMaxStackSize(), 1, set + ": stack size");
            helper.assertFalse(stack.isDamageableItem(), set + ": horse armour takes no wear, as vanilla's");
            helper.assertValueEqual(armour.getTexture(), id("textures/entity/horse/armor/horse_armor_" + SETS[i][1] + ".png"),
                    set + ": texture");
        }
        helper.succeed();
    }

    /**
     * A horse wears each one: the horse takes it as body armour and, a tick on, carries its armour value, toughness and
     * knockback resistance.
     */
    @GameTest(template = "empty_large", timeoutTicks = 40)
    public static void mod040b_a_horse_wears_each_and_carries_its_armour(GameTestHelper helper) {
        List<Horse> horses = new ArrayList<>();
        for (int i = 0; i < SETS.length; i++) {
            Horse horse = helper.spawnWithNoFreeWill(EntityType.HORSE, new BlockPos(3 + (i % 7) * 6, 2, 12 + (i / 7) * 12));
            horse.setNoAi(true);
            ItemStack stack = new ItemStack(item(SETS[i][0] + "_horse_armor"));
            helper.assertTrue(horse.isBodyArmorItem(stack), SETS[i][0] + "_horse_armor is not taken as a horse's armour");
            horse.setBodyArmorItem(stack);
            horses.add(horse);
        }
        helper.runAfterDelay(3, () -> {
            for (int i = 0; i < SETS.length; i++) {
                Horse horse = horses.get(i);
                AnimalArmorItem armour = (AnimalArmorItem) item(SETS[i][0] + "_horse_armor");
                helper.assertTrue(horse.getBodyArmorItem().is(armour), SETS[i][0] + ": the horse no longer wears it");
                helper.assertTrue(horse.getAttributeValue(Attributes.ARMOR) == armour.getDefense(),
                        SETS[i][0] + ": the horse's armour " + horse.getAttributeValue(Attributes.ARMOR) + ", not " + armour.getDefense());
                helper.assertTrue(Math.abs(horse.getAttributeValue(Attributes.ARMOR_TOUGHNESS) - armour.getToughness()) < 1.0E-6,
                        SETS[i][0] + ": the horse's toughness " + horse.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
                helper.assertTrue(Math.abs(horse.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)
                                - armour.getMaterial().value().knockbackResistance()) < 1.0E-6,
                        SETS[i][0] + ": the horse's knockback resistance " + horse.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            }
            horses.forEach(Horse::discard);
            helper.succeed();
        });
    }

    /**
     * The load condition reads modern.enabled and modern.horseArmour together and refuses an unknown key; with the
     * defaults the 13 recipes are loaded (none for the Royal Guardian, which is loot only), each making its horse
     * armour, and the King's table drops the Royal Guardian horse armour once a kill, beside his chestplate.
     */
    @GameTest(template = "empty")
    public static void mod040c_the_recipes_and_the_kings_drop_load_with_the_feature(GameTestHelper helper) {
        ICondition condition = new ModernCondition("horseArmour");
        final boolean master = OreSpawnConfig.MODERN_ENABLED.get();
        final boolean key = OreSpawnConfig.MODERN_HORSE_ARMOUR.get();
        boolean on, keyOff, masterOff;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(true);
            on = condition.test(ICondition.IContext.EMPTY);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(false);
            keyOff = condition.test(ICondition.IContext.EMPTY);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(true);
            OreSpawnConfig.MODERN_ENABLED.set(false);
            masterOff = condition.test(ICondition.IContext.EMPTY);
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(key);
        }
        helper.assertTrue(on && !keyOff && !masterOff,
                "the condition reads master and key: on " + on + ", key off " + keyOff + ", master off " + masterOff);
        JsonObject unknown = new JsonObject();
        unknown.addProperty("key", "noSuchFeature");
        helper.assertTrue(ModernCondition.CODEC.codec().parse(JsonOps.INSTANCE, unknown).isError(),
                "an unknown [modern] key decodes");

        helper.assertTrue(master && key, "the defaults are expected on for the loaded data: modern.enabled " + master
                + ", modern.horseArmour " + key);
        ServerLevel level = helper.getLevel();
        for (String[] set : SETS) {
            var recipe = level.getRecipeManager().byKey(id(set[0] + "_horse_armor"));
            if (set[0].equals("royal")) {
                helper.assertTrue(recipe.isEmpty(), "the Royal Guardian horse armour has a recipe");
                continue;
            }
            helper.assertTrue(recipe.isPresent(), set[0] + "_horse_armor has no recipe");
            RecipeHolder<?> holder = recipe.get();
            ItemStack result = holder.value().getResultItem(level.registryAccess());
            helper.assertTrue(result.is(item(set[0] + "_horse_armor")) && result.getCount() == 1,
                    set[0] + "_horse_armor's recipe makes " + result);
            helper.assertTrue(holder.value() instanceof ShapedRecipe shaped && shaped.getWidth() == 3 && shaped.getHeight() == 3,
                    set[0] + "_horse_armor's recipe is not a 3 x 3 shaped one");
            List<Ingredient> grid = holder.value().getIngredients();
            Item[] expected = grid(set[0]);
            for (int slot = 0; slot < 9; slot++) {
                Ingredient ingredient = grid.get(slot);
                boolean right = expected[slot] == null ? ingredient.isEmpty()
                        : !ingredient.isEmpty() && ingredient.test(new ItemStack(expected[slot]));
                helper.assertTrue(right, set[0] + "_horse_armor's recipe, slot " + slot + ": expected "
                        + (expected[slot] == null ? "nothing" : BuiltInRegistries.ITEM.getKey(expected[slot])));
            }
        }

        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 2, 1));
        LootTable king = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id("entities/the_king")));
        for (int roll = 0; roll < 8; roll++) {
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, pig)
                    .withParameter(LootContextParams.ORIGIN, pig.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            int horse = 0;
            int chest = 0;
            for (ItemStack stack : king.getRandomItems(params)) {
                if (stack.is(ModItems.ROYAL_HORSE_ARMOR.get())) {
                    horse += stack.getCount();
                }
                if (stack.is(ModItems.ROYAL_CHESTPLATE.get())) {
                    chest += stack.getCount();
                }
            }
            helper.assertTrue(horse == 1 && chest == 1,
                    "the King's drop, roll " + roll + ": " + horse + " horse armour and " + chest + " chestplate, not one each");
        }
        pig.discard();
        helper.succeed();
    }

    /**
     * A recipe's 3 x 3 grid, row by row (null where it is empty): vanilla's horse armour shape with the set's own
     * material, as its chestplate takes; Ultimate's rows as its chestplate's (iron, titanium, uranium); Experience as
     * its armour, eight bottles o' enchanting around the Emerald one.
     */
    private static Item[] grid(String set) {
        if (set.equals("ultimate")) {
            Item iron = Items.IRON_INGOT;
            Item titanium = item("ingot_titanium");
            Item uranium = item("ingot_uranium");
            return new Item[]{iron, null, iron, titanium, titanium, titanium, uranium, null, uranium};
        }
        if (set.equals("experience")) {
            Item bottle = Items.EXPERIENCE_BOTTLE;
            return new Item[]{bottle, bottle, bottle, bottle, item("emerald_horse_armor"), bottle, bottle, bottle, bottle};
        }
        Item m = switch (set) {
            case "queen" -> item("queen_scale");
            case "mobzilla" -> item("godzilla_scale");
            case "ruby" -> item("ruby");
            case "amethyst" -> item("amethyst_gem");
            case "emerald" -> Items.EMERALD;
            case "lavaeel" -> item("lava_eel");
            case "mothscale" -> item("moth_scale");
            case "peacock" -> item("peacock_feather");
            case "pink" -> item("crystal_pink_ingot");
            case "tigerseye" -> item("tigers_eye_ingot");
            case "lapis" -> Items.LAPIS_BLOCK;
            default -> throw new IllegalArgumentException(set);
        };
        return new Item[]{m, null, m, m, m, m, m, null, m};
    }

    /** The equipment tab lists the 14 horse armours right after the armour while the feature is on, none while it is off. */
    @GameTest(template = "empty")
    public static void mod040d_the_creative_tab_follows_the_feature(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CreativeModeTab tab = ModCreativeTabs.EQUIPMENT_TAB.get();
        CreativeModeTab.ItemDisplayParameters parameters =
                new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), false, level.registryAccess());
        final boolean master = OreSpawnConfig.MODERN_ENABLED.get();
        final boolean key = OreSpawnConfig.MODERN_HORSE_ARMOUR.get();
        List<Item> shownOn;
        List<Item> shownOff;
        List<Item> afterBoots = new ArrayList<>();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(true);
            tab.buildContents(parameters);
            shownOn = horseArmourIn(tab);
            List<ItemStack> all = new ArrayList<>(tab.getDisplayItems());
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).is(ModItems.LAPIS_BOOTS.get())) {
                    for (int j = i + 1; j < Math.min(all.size(), i + 1 + SETS.length); j++) {
                        afterBoots.add(all.get(j).getItem());
                    }
                    break;
                }
            }
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(false);
            tab.buildContents(parameters);
            shownOff = horseArmourIn(tab);
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(key);
            tab.buildContents(parameters);
        }
        List<Item> expected = new ArrayList<>();
        ModItems.HORSE_ARMOR.forEach(h -> expected.add(h.get()));
        helper.assertTrue(shownOn.equals(expected), "with the feature on the tab lists " + shownOn.size()
                + " horse armours, not the 14 in the armour's order");
        helper.assertTrue(afterBoots.equals(expected), "the horse armours do not follow the Lapis boots, the armour's last");
        helper.assertTrue(shownOff.isEmpty(), "with the feature off the tab still lists " + shownOff.size() + " horse armours");
        helper.succeed();
    }

    private static List<Item> horseArmourIn(CreativeModeTab tab) {
        List<Item> out = new ArrayList<>();
        for (ItemStack stack : tab.getDisplayItems()) {
            if (stack.getItem() instanceof AnimalArmorItem animal && animal.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN
                    && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(OreSpawnMod.MOD_ID)) {
                out.add(stack.getItem());
            }
        }
        return out;
    }
}
