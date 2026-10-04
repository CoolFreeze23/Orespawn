package danger.orespawn.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.item.ItemOreSpawnArmor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The six bosses' OreSpawn gear drops (Godzilla, Kraken, Basilisk, Cater Killer, Cephadrome, Trooper Bug). Each piece
 * rolls the original's enchantment dice, one chance and one level range per enchantment, as the 1.7.10 drop code has
 * them (orespawn_gametest/loot/boss_drop_dice.json); the gear that enchants itself takes its own set as the original
 * keyed it: while its key enchantment reads 0 (an armour piece: while none of its eight does).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BossDropTests {

    private static final String DICE = "/orespawn_gametest/loot/boss_drop_dice.json";
    private static final List<String> BOSSES = List.of("godzilla", "kraken", "basilisk", "cater_killer", "cephadrome",
            "trooper_bug");
    private static final int KILLS = 600;
    /** The key each self-enchanting piece reads (the original's onUsingTick); the armour reads ARMOUR_KIND instead. */
    private static final Map<String, String> KEYS = Map.of(
            "orespawn:experience_sword", "minecraft:sharpness",
            "orespawn:ultimate_axe", "minecraft:efficiency",
            "orespawn:ultimate_shovel", "minecraft:efficiency",
            "orespawn:ultimate_hoe", "minecraft:efficiency",
            "orespawn:ultimate_pickaxe", "minecraft:efficiency",
            "orespawn:emerald_pickaxe", "minecraft:silk_touch",
            "orespawn:ultimate_sword", "minecraft:looting",
            "orespawn:ultimate_bow", "minecraft:infinity");
    private static final Set<String> ARMOUR_KIND = Set.of("minecraft:protection", "minecraft:fire_protection",
            "minecraft:blast_protection", "minecraft:projectile_protection", "minecraft:respiration",
            "minecraft:aqua_affinity", "minecraft:unbreaking", "minecraft:feather_falling");

    /**
     * Over 600 seeded kills of each boss: every enchantment on an OreSpawn piece is one its dice roll, at a level within
     * their range (so no curse, and nothing the original did not roll), and every enchantment the dice can roll turns up
     * wherever enough of the piece dropped to expect it forty times.
     */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void loot065a_boss_gear_rolls_the_original_dice(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        JsonObject dice = dice();
        List<String> misses = new ArrayList<>();
        for (String boss : BOSSES) {
            LivingEntity mob = boss(helper, boss);
            JsonObject items = dice.getAsJsonObject(boss);
            Map<String, Integer> draws = new HashMap<>();
            Map<String, Integer> seen = new HashMap<>();
            for (long seed = 1; seed <= KILLS; seed++) {
                for (ItemStack stack : roll(helper, mob, player, seed)) {
                    String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    if (!items.has(id)) {
                        continue;
                    }
                    draws.merge(id, 1, Integer::sum);
                    Map<String, int[]> allowed = allowed(items.getAsJsonArray(id));
                    for (Object2IntMap.Entry<Holder<Enchantment>> e : stack.getEnchantments().entrySet()) {
                        String name = name(e.getKey());
                        int level = e.getIntValue();
                        int[] range = allowed.get(name);
                        if (range == null || level < range[0] || level > range[1]) {
                            misses.add(boss + ": " + id + " with " + name + " " + level
                                    + (range == null ? ", not among its dice" : ", outside " + range[0] + "-" + range[1]));
                        } else {
                            seen.merge(id + " " + name, 1, Integer::sum);
                        }
                    }
                }
            }
            int total = draws.values().stream().mapToInt(Integer::intValue).sum();
            if (total < 100) {
                misses.add(boss + ": only " + total + " OreSpawn gear drops in " + KILLS + " kills");
            }
            for (Map.Entry<String, JsonElement> item : items.entrySet()) {
                Map<String, Double> expected = expected(item.getValue().getAsJsonArray(), draws.getOrDefault(item.getKey(), 0));
                expected.forEach((name, n) -> {
                    if (n >= 40 && seen.getOrDefault(item.getKey() + " " + name, 0) == 0) {
                        misses.add(boss + ": " + item.getKey() + " never rolled " + name + " (about " + Math.round(n) + " expected)");
                    }
                });
            }
            mob.discard();
        }
        helper.assertTrue(misses.isEmpty(), misses.size() + " misses, the first: "
                + String.join("; ", misses.subList(0, Math.min(10, misses.size()))));
        helper.succeed();
    }

    /**
     * The self-enchanting drops of the same kills, each ticked once in an inventory: one whose dice missed its key (an
     * armour piece: rolled none of its eight) gains its own set, the levels its dice rolled standing; one whose dice hit
     * its key keeps its dice alone, as the original's check left it. Both happen.
     */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void loot065b_self_enchanting_drops_take_their_own_set_as_the_original_keyed_it(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        List<String> misses = new ArrayList<>();
        int gained = 0;
        int gainedWithDice = 0;
        int kept = 0;
        Map<Item, ItemEnchantments> owns = new HashMap<>();
        for (String boss : BOSSES) {
            LivingEntity mob = boss(helper, boss);
            for (long seed = 1; seed <= KILLS; seed++) {
                for (ItemStack drop : roll(helper, mob, player, seed)) {
                    Item item = drop.getItem();
                    String id = BuiltInRegistries.ITEM.getKey(item).toString();
                    ItemEnchantments own = owns.computeIfAbsent(item, i -> ownSet(level, player, i));
                    if (!id.startsWith(OreSpawnMod.MOD_ID + ":") || own.isEmpty()) {
                        continue;
                    }
                    ItemEnchantments before = drop.getEnchantments();
                    boolean hit;
                    if (item instanceof ItemOreSpawnArmor) {
                        hit = before.keySet().stream().anyMatch(h -> ARMOUR_KIND.contains(name(h)));
                    } else if (KEYS.containsKey(id)) {
                        hit = before.keySet().stream().anyMatch(h -> name(h).equals(KEYS.get(id)));
                    } else {
                        misses.add(boss + ": " + id + " enchants itself but has no key here");
                        continue;
                    }
                    ItemStack ticked = drop.copy();
                    item.inventoryTick(ticked, level, player, 0, false);
                    ItemEnchantments after = ticked.getEnchantments();
                    if (hit) {
                        kept++;
                        if (!after.equals(before)) {
                            misses.add(boss + ": " + id + " " + before + " took " + after + " though its dice hit its key");
                        }
                        continue;
                    }
                    gained++;
                    gainedWithDice += before.isEmpty() ? 0 : 1;
                    for (Object2IntMap.Entry<Holder<Enchantment>> e : own.entrySet()) {
                        int want = before.getLevel(e.getKey()) > 0 ? before.getLevel(e.getKey()) : e.getIntValue();
                        if (after.getLevel(e.getKey()) != want) {
                            misses.add(boss + ": " + id + " " + before + " gave " + after + ", not its own " + name(e.getKey()) + " " + want);
                        }
                    }
                    for (Object2IntMap.Entry<Holder<Enchantment>> e : before.entrySet()) {
                        if (after.getLevel(e.getKey()) != e.getIntValue()) {
                            misses.add(boss + ": " + id + " lost its rolled " + name(e.getKey()) + " " + e.getIntValue());
                        }
                    }
                }
            }
            mob.discard();
        }
        helper.assertTrue(gainedWithDice > 0 && kept > 0, "both outcomes must happen: " + gainedWithDice
                + " dice drops gained their own set (" + gained + " in all), " + kept + " kept their dice");
        helper.assertTrue(misses.isEmpty(), misses.size() + " misses, the first: "
                + String.join("; ", misses.subList(0, Math.min(10, misses.size()))));
        helper.succeed();
    }

    /**
     * The first tick of a self-enchanting piece that a drop's dice enchanted: an Ultimate Pickaxe with Unbreaking III
     * gains Efficiency V and Fortune V, its Unbreaking III standing; an Emerald Pickaxe with Efficiency II gains Silk
     * Touch I; an Experience Sword with Looting II gains Sharpness II and Unbreaking III. As the original keyed it: an
     * Ultimate Pickaxe with Efficiency II, and an Ultimate Helmet with Protection II, keep their dice alone; a bare
     * Ultimate Helmet takes its whole set, the four protections V, Respiration II and Aqua Affinity III.
     */
    @GameTest(template = "empty")
    public static void loot065c_a_dice_drop_gains_its_own_set_on_its_first_tick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack pickaxe = enchanted(registry, ModItems.ULTIMATE_PICKAXE.get(), "unbreaking", 3);
        tick(level, player, pickaxe);
        expect(helper, registry, "the Ultimate Pickaxe with Unbreaking III", pickaxe,
                Map.of("unbreaking", 3, "efficiency", 5, "fortune", 5));
        ItemStack emerald = enchanted(registry, ModItems.EMERALD_PICKAXE.get(), "efficiency", 2);
        tick(level, player, emerald);
        expect(helper, registry, "the Emerald Pickaxe with Efficiency II", emerald, Map.of("efficiency", 2, "silk_touch", 1));
        ItemStack sword = enchanted(registry, ModItems.EXPERIENCE_SWORD.get(), "looting", 2);
        tick(level, player, sword);
        expect(helper, registry, "the Experience Sword with Looting II", sword,
                Map.of("looting", 2, "sharpness", 2, "unbreaking", 3));
        ItemStack keyed = enchanted(registry, ModItems.ULTIMATE_PICKAXE.get(), "efficiency", 2);
        tick(level, player, keyed);
        expect(helper, registry, "the Ultimate Pickaxe with Efficiency II", keyed, Map.of("efficiency", 2));
        ItemStack helmet = enchanted(registry, ModItems.ULTIMATE_HELMET.get(), "protection", 2);
        tick(level, player, helmet);
        expect(helper, registry, "the Ultimate Helmet with Protection II", helmet, Map.of("protection", 2));
        ItemStack bare = new ItemStack(ModItems.ULTIMATE_HELMET.get());
        tick(level, player, bare);
        expect(helper, registry, "a bare Ultimate Helmet", bare, Map.of("protection", 5, "fire_protection", 5,
                "blast_protection", 5, "projectile_protection", 5, "respiration", 2, "aqua_affinity", 3));
        helper.succeed();
    }

    /**
     * The tables themselves, entry by entry against the original's dice: each OreSpawn gear entry of the six, in its
     * table's order, has the original's weight and exactly its dice as set_enchantments functions, last die first, each
     * one enchantment at its level (a constant, or uniform from min to max) under a random_chance of 1 in one_in; an
     * entry the original dropped bare sets none. No OreSpawn entry enchants at random, 77 roll dice, and the vanilla gear
     * beside them keeps its one random enchantment.
     */
    @GameTest(template = "empty")
    public static void loot065d_the_tables_carry_the_dice_not_a_random_enchantment(GameTestHelper helper) {
        JsonObject dice = dice();
        int rolled = 0;
        int vanillaRandom = 0;
        List<String> misses = new ArrayList<>();
        for (String boss : BOSSES) {
            JsonObject items = dice.getAsJsonObject(boss);
            Map<String, Integer> taken = new HashMap<>();
            for (JsonElement pool : GsonHelper.getAsJsonArray(table(helper, boss), "pools")) {
                for (JsonElement entry : GsonHelper.getAsJsonArray(pool.getAsJsonObject(), "entries")) {
                    JsonObject e = entry.getAsJsonObject();
                    String name = GsonHelper.getAsString(e, "name", "");
                    List<JsonObject> sets = new ArrayList<>();
                    boolean random = false;
                    for (JsonElement f : GsonHelper.getAsJsonArray(e, "functions", new JsonArray())) {
                        String function = GsonHelper.getAsString(f.getAsJsonObject(), "function", "");
                        random |= function.equals("minecraft:enchant_randomly") || function.equals("minecraft:enchant_with_levels");
                        if (function.equals("minecraft:set_enchantments")) {
                            sets.add(f.getAsJsonObject());
                        }
                    }
                    if (!name.startsWith(OreSpawnMod.MOD_ID + ":")) {
                        vanillaRandom += random ? 1 : 0;
                        continue;
                    }
                    if (random) {
                        misses.add(boss + " enchants " + name + " at random");
                    }
                    if (!items.has(name)) {
                        if (!sets.isEmpty()) {
                            misses.add(boss + " sets enchantments on " + name + ", which is not gear the original rolled");
                        }
                        continue;
                    }
                    JsonArray entries = items.getAsJsonArray(name);
                    int index = taken.merge(name, 1, Integer::sum) - 1;
                    String where = boss + " " + name + " entry " + (index + 1);
                    if (index >= entries.size()) {
                        misses.add(where + ": the original has " + entries.size());
                        continue;
                    }
                    JsonObject want = entries.get(index).getAsJsonObject();
                    if (GsonHelper.getAsInt(e, "weight", 1) != want.get("weight").getAsInt()) {
                        misses.add(where + ": weight " + GsonHelper.getAsInt(e, "weight", 1) + ", not " + want.get("weight"));
                    }
                    JsonArray wantDice = want.getAsJsonArray("dice");
                    rolled += wantDice.isEmpty() ? 0 : 1;
                    if (sets.size() != wantDice.size()) {
                        misses.add(where + ": " + sets.size() + " dice, not " + wantDice.size());
                        continue;
                    }
                    for (int i = 0; i < sets.size(); i++) {
                        int die = wantDice.size() - 1 - i;
                        String miss = die(sets.get(i), wantDice.get(die).getAsJsonObject());
                        if (miss != null) {
                            misses.add(where + ", die " + (die + 1) + ": " + miss);
                        }
                    }
                }
            }
            for (Map.Entry<String, JsonElement> item : items.entrySet()) {
                int found = taken.getOrDefault(item.getKey(), 0);
                if (found != item.getValue().getAsJsonArray().size()) {
                    misses.add(boss + ": " + found + " " + item.getKey() + " entries, the original has "
                            + item.getValue().getAsJsonArray().size());
                }
            }
        }
        helper.assertTrue(misses.isEmpty(), misses.size() + " misses, the first: "
                + String.join("; ", misses.subList(0, Math.min(10, misses.size()))));
        helper.assertTrue(rolled == 77, rolled + " OreSpawn entries roll dice, not 77");
        helper.assertTrue(vanillaRandom > 0, "no vanilla gear entry kept its random enchantment");
        helper.succeed();
    }

    /** One set_enchantments function against one die of the original: null when they agree, else what differs. */
    private static String die(JsonObject function, JsonObject die) {
        String enchantment = die.get("enchantment").getAsString();
        JsonObject enchantments = GsonHelper.getAsJsonObject(function, "enchantments", new JsonObject());
        if (enchantments.size() != 1 || !enchantments.has(enchantment)) {
            return "sets " + enchantments.keySet() + ", not " + enchantment;
        }
        if (GsonHelper.getAsBoolean(function, "add", false)) {
            return "adds to the level it finds";
        }
        int min = die.get("min").getAsInt();
        int max = die.get("max").getAsInt();
        JsonElement level = enchantments.get(enchantment);
        boolean levelHolds = level.isJsonPrimitive()
                ? min == max && level.getAsInt() == min
                : GsonHelper.getAsString(level.getAsJsonObject(), "type", "").equals("minecraft:uniform")
                        && GsonHelper.getAsInt(level.getAsJsonObject(), "min", -1) == min
                        && GsonHelper.getAsInt(level.getAsJsonObject(), "max", -1) == max;
        if (!levelHolds) {
            return "level " + level + ", not " + min + "-" + max;
        }
        int oneIn = die.get("one_in").getAsInt();
        JsonArray conditions = GsonHelper.getAsJsonArray(function, "conditions", new JsonArray());
        if (oneIn == 1) {
            return conditions.isEmpty() ? null : "conditions " + conditions + " on a sure die";
        }
        JsonObject condition = conditions.size() == 1 ? conditions.get(0).getAsJsonObject() : new JsonObject();
        JsonElement chance = condition.get("chance");
        if (!GsonHelper.getAsString(condition, "condition", "").equals("minecraft:random_chance")
                || chance == null || !chance.isJsonPrimitive() || Math.abs(chance.getAsDouble() - 1.0 / oneIn) > 1e-6) {
            return "conditions " + conditions + ", not one random_chance of 1 in " + oneIn;
        }
        return null;
    }

    private static JsonObject table(GameTestHelper helper, String boss) {
        ResourceLocation file = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "loot_table/entities/" + boss + ".json");
        Resource resource = helper.getLevel().getServer().getResourceManager().getResource(file)
                .orElseThrow(() -> new IllegalStateException("missing " + file));
        try (Reader reader = resource.openAsReader()) {
            return GsonHelper.parse(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JsonObject dice() {
        try (InputStream in = BossDropTests.class.getResourceAsStream(DICE)) {
            if (in == null) {
                throw new IllegalStateException("missing " + DICE);
            }
            return GsonHelper.parse(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Each enchantment an item's entries can roll, with the lowest and highest level any of them gives it. */
    private static Map<String, int[]> allowed(JsonArray entries) {
        Map<String, int[]> out = new HashMap<>();
        for (JsonElement entry : entries) {
            for (JsonElement d : entry.getAsJsonObject().getAsJsonArray("dice")) {
                JsonObject o = d.getAsJsonObject();
                int lo = o.get("min").getAsInt();
                int hi = o.get("max").getAsInt();
                out.merge(o.get("enchantment").getAsString(), new int[]{lo, hi},
                        (a, b) -> new int[]{Math.min(a[0], b[0]), Math.max(a[1], b[1])});
            }
        }
        return out;
    }

    /** How often each enchantment should turn up among an item's drops: its entries' shares of the item, times their chances. */
    private static Map<String, Double> expected(JsonArray entries, int draws) {
        double weight = 0;
        for (JsonElement entry : entries) {
            weight += entry.getAsJsonObject().get("weight").getAsInt();
        }
        Map<String, Double> out = new HashMap<>();
        for (JsonElement entry : entries) {
            double share = entry.getAsJsonObject().get("weight").getAsInt() / weight;
            for (JsonElement d : entry.getAsJsonObject().getAsJsonArray("dice")) {
                JsonObject o = d.getAsJsonObject();
                out.merge(o.get("enchantment").getAsString(), draws * share / o.get("one_in").getAsInt(), Double::sum);
            }
        }
        return out;
    }

    private static LivingEntity boss(GameTestHelper helper, String id) {
        return (LivingEntity) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, id))
                .create(helper.getLevel());
    }

    /**
     * One kill's drops for a player's kill, as the death path rolls the boss's table, from a fixed seed (never 0: the
     * loot context takes 0 for no seed and rolls on the level's own random).
     */
    private static List<ItemStack> roll(GameTestHelper helper, LivingEntity mob, Player player, long seed) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(mob.getLootTable());
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1)))
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> out = new ArrayList<>(table.getRandomItems(params, seed));
        out.removeIf(ItemStack::isEmpty);
        return out;
    }

    /** What a bare stack of the item puts on itself on one tick in an inventory: empty for an item that does not. */
    private static ItemEnchantments ownSet(ServerLevel level, Player player, Item item) {
        ItemStack bare = new ItemStack(item);
        tick(level, player, bare);
        return bare.getEnchantments();
    }

    private static void tick(ServerLevel level, Player player, ItemStack stack) {
        stack.getItem().inventoryTick(stack, level, player, 0, false);
    }

    private static String name(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(k -> k.location().toString()).orElse("?");
    }

    private static Holder<Enchantment> holder(Registry<Enchantment> registry, String name) {
        return registry.getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace(name)));
    }

    private static ItemStack enchanted(Registry<Enchantment> registry, Item item, String name, int level) {
        ItemStack stack = new ItemStack(item);
        stack.enchant(holder(registry, name), level);
        return stack;
    }

    private static void expect(GameTestHelper helper, Registry<Enchantment> registry, String what, ItemStack stack,
                               Map<String, Integer> levels) {
        ItemEnchantments got = stack.getEnchantments();
        boolean ok = got.size() == levels.size();
        for (Map.Entry<String, Integer> e : levels.entrySet()) {
            ok &= got.getLevel(holder(registry, e.getKey())) == e.getValue();
        }
        helper.assertTrue(ok, what + " has " + got + ", not " + levels);
    }
}
