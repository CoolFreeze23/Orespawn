package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.item.ItemOreSpawnArmor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ITEM-073: OreSpawn's gear at the enchanting table and the anvil. Each sword, tool and armour piece takes what vanilla
 * 1.21.1's item of its 1.7.10 class takes, through that class's vanilla tags (armour through the enchantable tags
 * directly); the Ultimate and Skate Bows and the Ultimate Fishing Rod, Ray Gun, SquidZooka, Thunder Staff, Wrench,
 * Sifter and NetherLost take the durability set; everything else takes nothing (the spears, with Mounts of Mayhem only,
 * have their own rows in SpearTests).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EnchantingTests {

    /**
     * A Ruby Sword through the game's own menus, an iron sword the control: with three lapis and no bookshelves the
     * table offers each an enchantment in one of its three slots, and the anvil puts a Sharpness book on each.
     */
    @GameTest(template = "empty")
    public static void item073a_a_ruby_sword_at_the_table_and_the_anvil(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ContainerLevelAccess access = ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)));
        Holder<Enchantment> sharpness = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SHARPNESS);
        String iron = offers(player, access, new ItemStack(Items.IRON_SWORD), sharpness);
        String ruby = offers(player, access, new ItemStack(ModItems.RUBY_SWORD.get()), sharpness);
        helper.assertTrue(iron.startsWith("table yes") && iron.endsWith("anvil yes"), "the iron sword (the control): " + iron);
        helper.assertTrue(ruby.startsWith("table yes") && ruby.endsWith("anvil yes"),
                "the Ruby Sword: " + ruby + " (the iron sword: " + iron + ")");
        helper.succeed();
    }

    /**
     * The same menus for an armour piece, a bow and a gadget: a Ruby Helmet (the table offers it something, the anvil
     * puts Protection on it), the Skate Bow (enchantment value 50: the table offers it Unbreaking, the anvil puts
     * Unbreaking on it) and the Ray Gun (enchantment value 0: the table offers it nothing, the anvil puts Unbreaking on
     * it, as 1.7.10's anvil did).
     */
    @GameTest(template = "empty")
    public static void item073c_armour_a_bow_and_a_gadget_at_the_table_and_the_anvil(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ContainerLevelAccess access = ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)));
        Registry<Enchantment> registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        String helmet = offers(player, access, new ItemStack(ModItems.RUBY_HELMET.get()), holder(registry, "protection"));
        String bow = offers(player, access, new ItemStack(ModItems.SKATE_BOW.get()), holder(registry, "unbreaking"));
        String gun = offers(player, access, new ItemStack(ModItems.RAY_GUN.get()), holder(registry, "unbreaking"));
        helper.assertTrue(helmet.startsWith("table yes") && helmet.endsWith("anvil yes"), "the Ruby Helmet: " + helmet);
        helper.assertTrue(bow.startsWith("table yes") && bow.endsWith("anvil yes"), "the Skate Bow: " + bow);
        helper.assertTrue(gun.startsWith("table no") && gun.endsWith("anvil yes"), "the Ray Gun: " + gun);
        helper.succeed();
    }

    /**
     * ITEM-074: the Zoo Keeper, one durability and two a capture, took Unbreaking from a book at 1.7.10's anvil, which
     * could spare it a capture: the anvil puts an Unbreaking book on it here too, not a Mending one, and the table offers
     * it nothing. The robot kits and the Creeper Launcher, used up whole there, take no book.
     */
    @GameTest(template = "empty")
    public static void item074a_the_zoo_keeper_takes_unbreaking_at_the_anvil(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ContainerLevelAccess access = ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)));
        Registry<Enchantment> registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        String unbreaking = offers(player, access, new ItemStack(ModItems.ZOO_KEEPER.get()), holder(registry, "unbreaking"));
        String mending = offers(player, access, new ItemStack(ModItems.ZOO_KEEPER.get()), holder(registry, "mending"));
        String kit = offers(player, access, new ItemStack(ModItems.SPIDER_ROBOT_KIT.get()), holder(registry, "unbreaking"));
        String launcher = offers(player, access, new ItemStack(ModItems.CREEPER_LAUNCHER.get()), holder(registry, "unbreaking"));
        helper.assertTrue(unbreaking.startsWith("table no") && unbreaking.endsWith("anvil yes"),
                "the Zoo Keeper and Unbreaking: " + unbreaking);
        helper.assertTrue(mending.endsWith("anvil no"), "the Zoo Keeper and Mending: " + mending);
        helper.assertTrue(kit.endsWith("anvil no") && launcher.endsWith("anvil no"),
                "the Spider Robot Kit: " + kit + "; the Creeper Launcher: " + launcher);
        // 1.7.10 broke it when its damage passed its one point of durability; here two points, broken on reaching
        // them: a capture of two with a point spared leaves it whole, as there
        ItemStack zoo = new ItemStack(ModItems.ZOO_KEEPER.get());
        zoo.hurtAndBreak(1, helper.getLevel(), null, item -> { });
        helper.assertTrue(zoo.getMaxDamage() == 2 && !zoo.isEmpty() && zoo.getDamageValue() == 1,
                "the Zoo Keeper: durability " + zoo.getMaxDamage() + ", after one point " + zoo.getDamageValue()
                        + (zoo.isEmpty() ? " (broken)" : ""));
        helper.succeed();
    }

    /** Whether the table offers the item anything in its three slots, and whether the anvil puts a level I book of the enchantment on it. */
    private static String offers(Player player, ContainerLevelAccess access, ItemStack item, Holder<Enchantment> book) {
        EnchantmentMenu table = new EnchantmentMenu(1, player.getInventory(), access);
        table.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
        table.getSlot(0).set(item.copy());
        boolean offered = false;
        for (int slot = 0; slot < 3; slot++) {
            offered |= table.costs[slot] > 0 && table.enchantClue[slot] >= 0;
        }
        AnvilMenu anvil = new AnvilMenu(2, player.getInventory(), access);
        anvil.getSlot(0).set(item.copy());
        anvil.getSlot(1).set(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(book, 1)));
        ItemStack result = anvil.getSlot(2).getItem();
        boolean combined = !result.isEmpty() && result.getEnchantments().getLevel(book) == 1;
        return "table " + (offered ? "yes" : "no") + " (costs " + java.util.Arrays.toString(table.costs) + ", clues "
                + java.util.Arrays.toString(table.enchantClue) + "), anvil " + (combined ? "yes" : "no");
    }

    private static final Set<String> DURABILITY = Set.of("unbreaking", "mending", "vanishing_curse");
    /** The bows and the plain items that take the durability set alone, with the enchantment value each has here. */
    private static final Map<String, Integer> DURABILITY_ONLY = new LinkedHashMap<>();
    static {
        DURABILITY_ONLY.put("ultimate_bow", 50);
        DURABILITY_ONLY.put("skate_bow", 50);
        DURABILITY_ONLY.put("ultimate_fishing_rod", 1);
        DURABILITY_ONLY.put("ray_gun", 0);
        DURABILITY_ONLY.put("squid_zooka", 0);
        DURABILITY_ONLY.put("thunder_staff", 0);
        DURABILITY_ONLY.put("wrench", 0);
        DURABILITY_ONLY.put("sifter", 0);
        DURABILITY_ONLY.put("nether_lost", 0);
    }

    /**
     * Every OreSpawn item but the spears (Mounts of Mayhem only; SpearTests has them), over every enchantment in the
     * registry, by the game's own tests (ItemStack.supportsEnchantment, isPrimaryItemFor, getEnchantmentValue): a sword,
     * axe, pickaxe, shovel or hoe exactly as the iron one, and an armour piece exactly as the iron piece of its slot,
     * each with an enchantment value above 0; the items in DURABILITY_ONLY the durability set alone, at their values;
     * the Zoo Keeper Unbreaking alone (ITEM-074), at no value; every other item nothing (the robot kits, the Creeper
     * Launcher, the shoes, the horse and wolf armour and the rest). Each miss is collected, then all are reported
     * together; each group's count is checked.
     */
    @GameTest(template = "empty")
    public static void item073b_every_item_takes_what_its_class_takes(GameTestHelper helper) {
        Registry<Enchantment> registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<Holder.Reference<Enchantment>> all = registry.holders().toList();
        List<String> misses = new ArrayList<>();
        // vanilla's own items as anchors, one for each tag file: a file that failed to load would empty vanilla's
        // members too, and the comparison with the iron items below would still match
        anchor(misses, registry, Items.IRON_SWORD, "sharpness", true);
        anchor(misses, registry, Items.IRON_AXE, "efficiency", false);
        anchor(misses, registry, Items.IRON_PICKAXE, "efficiency", false);
        anchor(misses, registry, Items.IRON_SHOVEL, "efficiency", false);
        anchor(misses, registry, Items.IRON_HOE, "efficiency", false);
        anchor(misses, registry, Items.IRON_HELMET, "respiration", false);
        anchor(misses, registry, Items.IRON_CHESTPLATE, "thorns", true);
        anchor(misses, registry, Items.IRON_LEGGINGS, "swift_sneak", false);
        anchor(misses, registry, Items.IRON_BOOTS, "feather_falling", false);
        anchor(misses, registry, Items.IRON_HELMET, "binding_curse", false);
        anchor(misses, registry, Items.IRON_SWORD, "unbreaking", false);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!OreSpawnMod.MOD_ID.equals(id.getNamespace()) || id.getPath().endsWith("_spear")) {
                continue;
            }
            String path = id.getPath();
            ItemStack stack = new ItemStack(item);
            Item iron = counterpart(item);
            if (iron != null) {
                count(counts, item instanceof ItemOreSpawnArmor armour ? armour.getType().name().toLowerCase(Locale.ROOT)
                        : BuiltInRegistries.ITEM.getKey(iron).getPath().replace("iron_", ""));
                ItemStack control = new ItemStack(iron);
                for (Holder.Reference<Enchantment> e : all) {
                    String name = e.key().location().getPath();
                    if (stack.supportsEnchantment(e) != control.supportsEnchantment(e)) {
                        misses.add(path + (control.supportsEnchantment(e) ? " refuses " : " takes ") + name);
                    }
                    if (stack.isPrimaryItemFor(e) != control.isPrimaryItemFor(e)) {
                        misses.add(path + (control.isPrimaryItemFor(e) ? " is not " : " is ") + "a primary item for " + name);
                    }
                }
                if (stack.getEnchantmentValue() <= 0) {
                    misses.add(path + ": enchantment value " + stack.getEnchantmentValue());
                }
            } else if (DURABILITY_ONLY.containsKey(path)) {
                count(counts, "durability only");
                for (Holder.Reference<Enchantment> e : all) {
                    boolean expected = DURABILITY.contains(e.key().location().getPath());
                    if (stack.supportsEnchantment(e) != expected) {
                        misses.add(path + (expected ? " refuses " : " takes ") + e.key().location().getPath());
                    }
                }
                if (stack.getEnchantmentValue() != DURABILITY_ONLY.get(path)) {
                    misses.add(path + ": enchantment value " + stack.getEnchantmentValue() + ", not " + DURABILITY_ONLY.get(path));
                }
            } else if (path.equals("zoo_keeper")) {
                count(counts, "unbreaking only");
                for (Holder.Reference<Enchantment> e : all) {
                    boolean expected = e.key().location().getPath().equals("unbreaking");
                    if (stack.supportsEnchantment(e) != expected) {
                        misses.add(path + (expected ? " refuses " : " takes ") + e.key().location().getPath());
                    }
                }
                if (stack.getEnchantmentValue() != 0) {
                    misses.add(path + ": enchantment value " + stack.getEnchantmentValue() + ", not 0");
                }
            } else {
                count(counts, "nothing");
                for (Holder.Reference<Enchantment> e : all) {
                    if (stack.supportsEnchantment(e)) {
                        misses.add(path + " takes " + e.key().location().getPath());
                    }
                }
            }
        }
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("sword", 23);
        expected.put("axe", 8);
        expected.put("pickaxe", 8);
        expected.put("shovel", 8);
        expected.put("hoe", 8);
        expected.put("helmet", 14);
        expected.put("chestplate", 14);
        expected.put("leggings", 14);
        expected.put("boots", 14);
        expected.put("durability only", DURABILITY_ONLY.size());
        expected.put("unbreaking only", 1);
        expected.forEach((group, n) -> {
            if (!n.equals(counts.getOrDefault(group, 0))) {
                misses.add(0, "the registry has " + counts.getOrDefault(group, 0) + " " + group + ", the test expects " + n);
            }
        });
        helper.assertTrue(counts.getOrDefault("nothing", 0) > 100, "too few items that take nothing: " + counts);
        helper.assertTrue(misses.isEmpty(), misses.size() + " misses, the first: " + String.join("; ",
                misses.subList(0, Math.min(12, misses.size()))));
        helper.succeed();
    }

    private static void anchor(List<String> misses, Registry<Enchantment> registry, Item item, String name, boolean primary) {
        ItemStack stack = new ItemStack(item);
        Holder<Enchantment> e = holder(registry, name);
        if (!stack.supportsEnchantment(e) || (primary && !stack.isPrimaryItemFor(e))) {
            misses.add(0, "vanilla's " + BuiltInRegistries.ITEM.getKey(item).getPath() + " lost " + name
                    + (primary ? " as a primary item" : "") + ": a tag file did not load");
        }
    }

    /** The vanilla item whose enchanting an OreSpawn sword, tool or armour piece shares, or null. */
    private static Item counterpart(Item item) {
        if (item instanceof SwordItem) return Items.IRON_SWORD;
        if (item instanceof AxeItem) return Items.IRON_AXE;
        if (item instanceof PickaxeItem) return Items.IRON_PICKAXE;
        if (item instanceof ShovelItem) return Items.IRON_SHOVEL;
        if (item instanceof HoeItem) return Items.IRON_HOE;
        if (item instanceof ItemOreSpawnArmor armour) {
            return switch (armour.getType()) {
                case HELMET -> Items.IRON_HELMET;
                case CHESTPLATE -> Items.IRON_CHESTPLATE;
                case LEGGINGS -> Items.IRON_LEGGINGS;
                case BOOTS -> Items.IRON_BOOTS;
                default -> null;
            };
        }
        return null;
    }

    private static void count(Map<String, Integer> counts, String group) {
        counts.merge(group, 1, Integer::sum);
    }

    private static Holder<Enchantment> holder(Registry<Enchantment> registry, String name) {
        return registry.getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(name)));
    }
}
