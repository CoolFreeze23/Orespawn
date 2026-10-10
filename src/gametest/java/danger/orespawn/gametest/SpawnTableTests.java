package danger.orespawn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import danger.orespawn.ModEntities;
import danger.orespawn.ModSpawnControl;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OriginalSpawnRules;
import danger.orespawn.entity.ListedSpawnPlacement;
import danger.orespawn.world.ModBiomeModifiers;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * WGEN-086: the overworld's and the Nether's spawn lists as the original's (original_spawns.json, written by
 * tools/ref1710/gen_spawn_tables.py from the original's addSpawn calls and checked there against the 1.7.10 reference
 * server's lists), the port's own overworld spawns behind {@code [modern] extraSpawns} (extra_spawns.json), every
 * listed species placed by its list and judged by its own 1.7.10 rule alone, and Mining's and the Islands' lists.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpawnTableTests {
    private static final String TABLES = "/data/orespawn/neoforge/biome_modifier/";

    static ModBiomeModifiers.SpawnTable table(GameTestHelper helper, String name) {
        try (InputStream in = SpawnTableTests.class.getResourceAsStream(TABLES + name)) {
            JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
            return ModBiomeModifiers.SPAWN_TABLE.get().codec().parse(ops, json).getOrThrow();
        } catch (Exception e) {
            throw new IllegalStateException("cannot read " + name, e);
        }
    }

    static String row(MobSpawnSettings.SpawnerData d) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(d.type) + " " + d.getWeight().asInt() + "x" + d.minCount + "-" + d.maxCount;
    }

    /**
     * The species a table file lists, read from its JSON (extra_spawns.json keeps the tags its entries had, one of which
     * no pack defines: the server's load leaves such a set empty, a decode against the loaded registries refuses it).
     */
    private static Set<EntityType<?>> listedTypes(String name) {
        Set<EntityType<?>> out = new HashSet<>();
        try (InputStream in = SpawnTableTests.class.getResourceAsStream(TABLES + name)) {
            JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            for (JsonElement entry : json.getAsJsonObject().getAsJsonArray("entries")) {
                for (JsonElement spawner : entry.getAsJsonObject().getAsJsonArray("spawners")) {
                    out.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(
                            spawner.getAsJsonObject().get("type").getAsString())));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("cannot read " + name, e);
        }
        return out;
    }

    private static Set<EntityType<?>> types(ModBiomeModifiers.SpawnTable table) {
        Set<EntityType<?>> out = new HashSet<>();
        for (ModBiomeModifiers.SpawnTable.Entry e : table.entries()) {
            for (MobSpawnSettings.SpawnerData d : e.spawners()) out.add(d.type);
        }
        return out;
    }

    /**
     * Every overworld and Nether biome lists exactly the OreSpawn entries original_spawns.json gives it for the day the
     * server started (in their named lists, a repeated entry twice), less the mobs the config disables; with
     * {@code [modern] extraSpawns} off by default, none of extra_spawns.json's beyond them. The table holds the original's 55
     * species, the dates' entries apart, and no 1.21 biome without an ancestor (cherry grove, the caves) lists any.
     */
    @GameTest(template = "empty")
    public static void wgen086a_the_overworld_lists_are_the_original_s(GameTestHelper helper) {
        ModBiomeModifiers.SpawnTable original = table(helper, "original_spawns.json");
        helper.assertTrue(!OreSpawnConfig.extraSpawns(), "[modern] extraSpawns is on by default");
        Set<EntityType<?>> watched = types(original);
        watched.addAll(listedTypes("extra_spawns.json"));
        helper.assertTrue(types(original).size() == 55, types(original).size() + " species in original_spawns.json, not 55");
        long dated = original.entries().stream().filter(e -> e.when() != ModBiomeModifiers.SpawnTable.When.ALWAYS).count();
        helper.assertTrue(dated > 0, "original_spawns.json holds no dated entry");
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        List<String> wrong = new ArrayList<>();
        int checked = 0;
        for (Map.Entry<net.minecraft.resources.ResourceKey<Biome>, Biome> b : biomes.entrySet()) {
            if (!b.getKey().location().getNamespace().equals("minecraft")) continue;
            Holder<Biome> holder = biomes.wrapAsHolder(b.getValue());
            for (MobCategory category : MobCategory.values()) {
                Map<String, Integer> want = new TreeMap<>();
                for (ModBiomeModifiers.SpawnTable.Entry e : original.entries()) {
                    if (e.category() != category || !e.biomes().contains(holder) || !e.when().holds()) continue;
                    for (MobSpawnSettings.SpawnerData d : e.spawners()) {
                        if (ModSpawnControl.naturalSpawnEnabled(d.type)) want.merge(row(d), 1, Integer::sum);
                    }
                }
                Map<String, Integer> got = new TreeMap<>();
                for (MobSpawnSettings.SpawnerData d : b.getValue().getMobSettings().getMobs(category).unwrap()) {
                    if (watched.contains(d.type)) got.merge(row(d), 1, Integer::sum);
                }
                checked += want.size();
                if (!got.equals(want) && wrong.size() < 6) {
                    wrong.add(b.getKey().location() + " " + category.getName() + ": got " + got + ", want " + want);
                }
            }
            if (b.getKey().location().getPath().matches("cherry_grove|lush_caves|dripstone_caves|deep_dark")) {
                for (MobCategory category : MobCategory.values()) {
                    for (MobSpawnSettings.SpawnerData d : b.getValue().getMobSettings().getMobs(category).unwrap()) {
                        if (watched.contains(d.type) && !OreSpawnConfig.extraSpawns()) {
                            wrong.add(b.getKey().location() + " lists " + row(d) + " though no 1.7.10 biome led to it");
                        }
                    }
                }
            }
        }
        helper.assertTrue(wrong.isEmpty(), "lists differ from the tables: " + wrong);
        helper.assertTrue(checked > 200, "only " + checked + " entries checked");
        helper.succeed();
    }

    /**
     * Every species the tables list is placed by the list the biome carries it in (ListedSpawnPlacement) and its
     * predicate adds nothing to its own rule: a species whose original overrode getCanSpawnHere passes the predicate on
     * bare stone, where the animal rule the cows and the Hydrolisc inherited refuses.
     */
    @GameTest(template = "empty")
    public static void wgen086b_listed_species_keep_their_own_rules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(floor.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(floor.above(2), Blocks.AIR.defaultBlockState());
        List<String> wrong = new ArrayList<>();
        Set<EntityType<?>> all = types(table(helper, "original_spawns.json"));
        all.addAll(listedTypes("extra_spawns.json"));
        for (EntityType<?> type : all) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            if (SpawnPlacements.getPlacementType(type) != ListedSpawnPlacement.INSTANCE) wrong.add(id + " placement");
            boolean passes = SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, floor.above(), level.getRandom());
            boolean own = OriginalSpawnRules.ownRule(type);
            if (own && !passes) wrong.add(id + " predicate adds a rule on stone");
        }
        for (EntityType<?> type : List.of(ModEntities.RED_COW.get(), ModEntities.GOLD_COW.get(),
                ModEntities.ENCHANTED_APPLE_COW.get(), ModEntities.ENTITY_HYDROLISC.get())) {
            if (OriginalSpawnRules.ownRule(type)) wrong.add(BuiltInRegistries.ENTITY_TYPE.getKey(type) + " own rule");
            if (SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, floor.above(), level.getRandom())) {
                wrong.add(BuiltInRegistries.ENTITY_TYPE.getKey(type) + " passes the animal rule on stone");
            }
        }
        helper.assertTrue(wrong.isEmpty(), "spawn rules: " + wrong);
        helper.succeed();
    }

    /**
     * Natural spawning reads a species' own transcribed rule alone (the Kyuubi's {@code return true}, orig Kyuubi.java,
     * in water, where the game's obstruction test refuses any liquid in the box), the cows' the game's checks (refused
     * in water), and a chunk's spawning reads no rule at all (a Mantis in water, which its own air-box rule refuses).
     */
    @GameTest(template = "empty")
    public static void wgen086c_natural_spawns_read_the_original_s_rule_alone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pool = helper.absolutePos(new BlockPos(2, 1, 2));
        for (int dy = 0; dy < 3; dy++) level.setBlockAndUpdate(pool.above(dy), Blocks.WATER.defaultBlockState());
        Mob kyuubi = ModEntities.ENTITY_KYUUBI.get().create(level);
        Mob cow = ModEntities.RED_COW.get().create(level);
        Mob mantis = ModEntities.ENTITY_MANTIS.get().create(level);
        for (Mob mob : new Mob[] {kyuubi, cow, mantis}) {
            mob.moveTo(pool.getX() + 0.5, pool.getY() + 0.1, pool.getZ() + 0.5, 0.0F, 0.0F);
        }
        helper.assertTrue(!kyuubi.checkSpawnObstruction(level), "the Kyuubi's box in the pool is not refused by the game");
        helper.assertTrue(EventHooks.checkSpawnPosition(kyuubi, level, MobSpawnType.NATURAL),
                "the Kyuubi's own rule (true) is refused in water");
        helper.assertTrue(!EventHooks.checkSpawnPosition(cow, level, MobSpawnType.NATURAL),
                "the Apple Cow in water passed the game's checks");
        helper.assertTrue(!mantis.checkSpawnRules(level, MobSpawnType.NATURAL)
                        && !EventHooks.checkSpawnPosition(mantis, level, MobSpawnType.NATURAL),
                "the Mantis in the pool, its air box full of water, passed its own rule");
        helper.assertTrue(EventHooks.checkSpawnPosition(mantis, level, MobSpawnType.CHUNK_GENERATION)
                        && SpawnPlacements.checkSpawnRules(ModEntities.RED_COW.get(), level, MobSpawnType.CHUNK_GENERATION,
                        pool, level.getRandom()),
                "a chunk's spawning read a rule");
        helper.succeed();
    }

    private static List<String> list(Biome biome, MobCategory category) {
        List<String> out = new ArrayList<>();
        for (MobSpawnSettings.SpawnerData d : biome.getMobSettings().getMobs(category).unwrap()) {
            if (ModSpawnControl.naturalSpawnEnabled(d.type)) out.add(row(d));
        }
        return out;
    }

    private static List<String> enabled(String... rows) {
        List<String> out = new ArrayList<>();
        for (String r : rows) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(r.split(" ")[0]));
            if (ModSpawnControl.naturalSpawnEnabled(type)) out.add(r);
        }
        return out;
    }

    /**
     * Mining's ambient list with the seven entries the original's Extreme Hills biome lent its provider's
     * (ChunkProviderOreSpawn2), and the Islands' flyers in the ambient list with no island entity listed (the original
     * made those from its island block): the original's lists as its server held them.
     */
    @GameTest(template = "empty")
    public static void wgen086d_mining_and_islands_as_the_original_s(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        Biome mining = biomes.get(ResourceLocation.parse("orespawn:mining_biome"));
        Biome islands = biomes.get(ResourceLocation.parse("orespawn:island_biome"));
        List<String> miningAmbient = enabled("minecraft:bat 10x8-8", "orespawn:velocity_raptor 1x2-4",
                "orespawn:dragonfly 2x1-3", "orespawn:camarasaurus 1x2-4", "orespawn:baryonyx 2x4-8",
                "orespawn:butterfly 5x1-2", "orespawn:luna_moth 8x1-2", "orespawn:cassowary 5x1-2",
                "orespawn:cockateil 10x1-2", "orespawn:mothra 2x1-1", "orespawn:ender_knight 4x2-4",
                "orespawn:ender_reaper 2x1-2");
        List<String> islandsAmbient = enabled("orespawn:butterfly 5x2-6", "orespawn:cockateil 4x1-2",
                "orespawn:luna_moth 5x2-4", "orespawn:firefly 10x4-8", "orespawn:dragon 1x1-2", "orespawn:stinky 2x1-2",
                "orespawn:cliff_racer 20x3-6", "orespawn:cloud_shark 1x1-1", "orespawn:gold_fish 5x2-4");
        helper.assertTrue(list(mining, MobCategory.AMBIENT).equals(miningAmbient),
                "Mining's ambient list " + list(mining, MobCategory.AMBIENT));
        helper.assertTrue(list(islands, MobCategory.AMBIENT).equals(islandsAmbient)
                        && list(islands, MobCategory.CREATURE).isEmpty(),
                "the Islands' lists " + list(islands, MobCategory.AMBIENT) + " / " + list(islands, MobCategory.CREATURE));
        helper.succeed();
    }
}
