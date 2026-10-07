package danger.orespawn.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.ModEntities;
import danger.orespawn.ModSpawnControl;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GitHub #6, "apple cows feel rarer": the 1.7.10 Apple Cow is the Red Cow (orig OreSpawnMain.java:3587), registered
 * here as MobCategory.MISC, and vanilla's SpawnerData turns a MISC entry into a pig, so it never spawned; the Utopia,
 * Village and Chaos lists had their ambient animals moved into the creature list (a smaller cow share, and those
 * animals spawning with the chunk), and a disabled mob kept its entry. These pin the categories, the cows' spawn rule,
 * the three lists against BiomeGenUtopianPlains, and the disabled mobs' absence.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpawnListParityTests {

    private record Row(String id, int weight, int min, int max) {
        static Row of(MobSpawnSettings.SpawnerData data) {
            return new Row(BuiltInRegistries.ENTITY_TYPE.getKey(data.type).toString(), data.getWeight().asInt(),
                    data.minCount, data.maxCount);
        }

        EntityType<?> type() {
            return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
        }
    }

    private static Row r(String id, int weight, int min, int max) {
        return new Row(id.contains(":") ? id : "orespawn:" + id, weight, min, max);
    }

    /** Every spawn entry in the mod's biome and biome-modifier files names a mob a spawn list can hold. */
    @GameTest(template = "empty")
    public static void gh6a_no_spawn_entry_names_a_misc_mob(GameTestHelper helper) {
        var resources = helper.getLevel().getServer().getResourceManager();
        Map<ResourceLocation, Resource> files = new java.util.HashMap<>();
        files.putAll(resources.listResources("worldgen/biome", id -> id.getNamespace().equals(OreSpawnMod.MOD_ID)));
        files.putAll(resources.listResources("neoforge/biome_modifier", id -> id.getNamespace().equals(OreSpawnMod.MOD_ID)));
        helper.assertTrue(files.size() > 10, "found too few spawn files: " + files.size());
        List<String> bad = new ArrayList<>();
        int entries = 0;
        for (var file : files.entrySet()) {
            JsonElement json;
            try (Reader reader = file.getValue().openAsReader()) {
                json = JsonParser.parseReader(reader);
            } catch (Exception e) {
                throw new AssertionError("unreadable " + file.getKey() + ": " + e);
            }
            List<String> ids = new ArrayList<>();
            collectSpawnTypes(json, ids);
            for (String id : ids) {
                entries++;
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(id)).orElse(null);
                if (type == null) bad.add(file.getKey() + ": unknown " + id);
                else if (type.getCategory() == MobCategory.MISC) bad.add(file.getKey() + ": " + id + " is MISC (spawns as a pig)");
            }
        }
        helper.assertTrue(entries > 100, "found too few spawn entries: " + entries);
        helper.assertTrue(bad.isEmpty(), "spawn entries that cannot spawn as themselves: " + bad);
        helper.succeed();
    }

    /** A spawn entry is an object with a "type" and a "weight"; anything else is walked through. */
    private static void collectSpawnTypes(JsonElement json, List<String> out) {
        if (json.isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray()) collectSpawnTypes(e, out);
        } else if (json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            if (o.has("type") && o.has("weight") && o.get("type").isJsonPrimitive()) {
                out.add(o.get("type").getAsString());
            }
            for (var e : o.entrySet()) collectSpawnTypes(e.getValue(), out);
        }
    }

    /**
     * The cow line's categories and rules: the Red Cow, Gold Cow and Enchanted Cow count as creatures and spawn under
     * EntityAnimal's rule (grass below, bright) while the world runs and on any ground with the chunk; the Spider Driver
     * is a monster, the T-Shirt a creature.
     */
    @GameTest(template = "empty")
    public static void gh6b_cow_line_categories_and_spawn_rule(GameTestHelper helper) {
        helper.assertTrue(ModEntities.RED_COW.get().getCategory() == MobCategory.CREATURE, "red_cow is not CREATURE");
        helper.assertTrue(ModEntities.GOLD_COW.get().getCategory() == MobCategory.CREATURE, "gold_cow is not CREATURE");
        helper.assertTrue(ModEntities.ENCHANTED_APPLE_COW.get().getCategory() == MobCategory.CREATURE,
                "enchanted_apple_cow is not CREATURE");
        helper.assertTrue(ModEntities.SPIDER_DRIVER.get().getCategory() == MobCategory.MONSTER, "spider_driver is not MONSTER");
        helper.assertTrue(ModEntities.ENTITY_TSHIRT.get().getCategory() == MobCategory.CREATURE, "tshirt is not CREATURE");
        for (EntityType<?> type : List.of(ModEntities.RED_COW.get(), ModEntities.GOLD_COW.get(),
                ModEntities.SPIDER_DRIVER.get(), ModEntities.ENTITY_TSHIRT.get())) {
            helper.assertTrue(SpawnPlacements.getPlacementType(type) == SpawnPlacementTypes.ON_GROUND,
                    BuiltInRegistries.ENTITY_TYPE.getKey(type) + " has no ON_GROUND placement");
        }
        ServerLevel level = helper.getLevel();
        BlockPos grass = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos stone = helper.absolutePos(new BlockPos(3, 1, 1));
        level.setBlock(grass, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        level.setBlock(stone, Blocks.STONE.defaultBlockState(), 3);
        for (EntityType<? extends Animal> type : List.of(ModEntities.RED_COW.get(), ModEntities.GOLD_COW.get())) {
            for (BlockPos below : List.of(grass, stone)) {
                BlockPos at = below.above();
                boolean registered = SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, at, level.random);
                boolean animal = Animal.checkAnimalSpawnRules(type, level, MobSpawnType.NATURAL, at, level.random);
                helper.assertTrue(registered == animal, BuiltInRegistries.ENTITY_TYPE.getKey(type)
                        + "'s spawn rule is not EntityAnimal's at " + at);
            }
            helper.assertTrue(!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, stone.above(), level.random),
                    BuiltInRegistries.ENTITY_TYPE.getKey(type) + " may spawn on stone");
        }
        // with the chunk, any ground: 1.7.10's chunk-generation spawner never asked getCanSpawnHere, so the Enchanted
        // Cow's mushroom-island herds stood on mycelium (orig OreSpawnMain.java:4623); while the world runs, grass only
        BlockPos mycelium = helper.absolutePos(new BlockPos(5, 1, 1));
        level.setBlock(mycelium, Blocks.MYCELIUM.defaultBlockState(), 3);
        for (EntityType<? extends Animal> type : List.of(ModEntities.RED_COW.get(), ModEntities.GOLD_COW.get(),
                ModEntities.ENCHANTED_APPLE_COW.get())) {
            helper.assertTrue(SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.CHUNK_GENERATION, mycelium.above(), level.random),
                    BuiltInRegistries.ENTITY_TYPE.getKey(type) + " may not spawn on mycelium with the chunk");
            helper.assertTrue(!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, mycelium.above(), level.random),
                    BuiltInRegistries.ENTITY_TYPE.getKey(type) + " may spawn on mycelium while the world runs");
        }
        helper.succeed();
    }

    private static List<Row> rows(Biome biome, MobCategory category) {
        return biome.getMobSettings().getMobs(category).unwrap().stream().map(Row::of).toList();
    }

    /** The original's rows, less the ones the config disables (the original added a disabled mob to no list). */
    private static List<Row> enabled(Row... rows) {
        return java.util.Arrays.stream(rows).filter(row -> ModSpawnControl.naturalSpawnEnabled(row.type())).toList();
    }

    private static void assertList(GameTestHelper helper, Biome biome, String name, MobCategory category, List<Row> want) {
        List<Row> got = rows(biome, category);
        helper.assertTrue(got.equals(want), name + " " + category.getName() + " list differs from the original:\n got  "
                + got + "\n want " + want);
    }

    /**
     * The Utopia, Village and Chaos lists as BiomeGenUtopianPlains built them on BiomeGenBase's defaults (orig
     * BiomeGenUtopianPlains.java:88-133, setVillageCreatures :283-332, setChaosCreatures :336-405); Utopia has no
     * monster list (ChunkProviderOreSpawn.java:318-320).
     */
    @GameTest(template = "empty")
    public static void gh6c_dimension_lists_match_the_original(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        Biome utopia = biomes.get(ResourceLocation.parse("orespawn:utopia_plains"));
        Biome village = biomes.get(ResourceLocation.parse("orespawn:village_biome"));
        Biome chaos = biomes.get(ResourceLocation.parse("orespawn:chaos_biome"));
        helper.assertTrue(utopia != null && village != null && chaos != null, "a dimension biome is missing");

        Row[] vanillaMonsters = {r("minecraft:spider", 100, 4, 4), r("minecraft:zombie", 100, 4, 4),
                r("minecraft:skeleton", 100, 4, 4), r("minecraft:creeper", 100, 4, 4), r("minecraft:slime", 100, 4, 4),
                r("minecraft:enderman", 10, 1, 4), r("minecraft:witch", 5, 1, 1)};
        Row[] utopiaCreatures = {r("minecraft:sheep", 12, 4, 4), r("minecraft:pig", 10, 4, 4),
                r("minecraft:chicken", 10, 4, 4), r("minecraft:cow", 8, 4, 4), r("gazelle", 10, 2, 4),
                r("girlfriend", 5, 2, 3), r("boyfriend", 5, 2, 3), r("red_cow", 10, 4, 8), r("gold_cow", 8, 2, 6),
                r("enchanted_apple_cow", 5, 2, 4)};
        Row[] utopiaAmbient = {r("minecraft:bat", 10, 8, 8), r("firefly", 15, 3, 6), r("butterfly", 20, 3, 6),
                r("luna_moth", 10, 1, 5), r("chipmunk", 3, 1, 2), r("cockateil", 10, 2, 4), r("gold_fish", 1, 1, 1),
                r("coin", 2, 1, 1), r("cricket", 5, 4, 6)};

        assertList(helper, utopia, "utopia", MobCategory.MONSTER, List.of());
        assertList(helper, utopia, "utopia", MobCategory.CREATURE, enabled(utopiaCreatures));
        assertList(helper, utopia, "utopia", MobCategory.AMBIENT, enabled(utopiaAmbient));

        List<Row> villageMonsters = new ArrayList<>(enabled(vanillaMonsters));
        villageMonsters.addAll(enabled(r("robot_1", 25, 4, 8), r("robot_2", 16, 2, 8), r("robot_3", 12, 2, 4),
                r("robot_4", 8, 1, 2), r("robot_5", 20, 4, 8), r("giant_robot", 8, 1, 2), r("spider_driver", 20, 3, 5),
                r("godzilla", 2, 1, 1)));
        List<Row> villageCreatures = new ArrayList<>(enabled(utopiaCreatures));
        villageCreatures.addAll(enabled(r("girlfriend", 1, 2, 3), r("boyfriend", 1, 2, 3), r("red_cow", 8, 4, 8),
                r("gold_cow", 6, 2, 6), r("enchanted_apple_cow", 4, 2, 4)));
        List<Row> villageAmbient = new ArrayList<>(enabled(utopiaAmbient));
        villageAmbient.addAll(enabled(r("firefly", 10, 3, 6), r("butterfly", 25, 3, 6), r("luna_moth", 20, 1, 5),
                r("chipmunk", 5, 1, 2), r("cockateil", 15, 2, 4), r("tshirt", 2, 1, 1), r("coin", 2, 1, 1),
                r("band_p", 15, 1, 2)));
        assertList(helper, village, "village", MobCategory.MONSTER, villageMonsters);
        assertList(helper, village, "village", MobCategory.CREATURE, villageCreatures);
        assertList(helper, village, "village", MobCategory.AMBIENT, villageAmbient);

        assertList(helper, chaos, "chaos", MobCategory.CREATURE, enabled(r("beaver", 1, 1, 2), r("red_cow", 3, 2, 4),
                r("gold_cow", 2, 2, 4), r("enchanted_apple_cow", 1, 2, 4)));
        assertList(helper, chaos, "chaos", MobCategory.AMBIENT, enabled(r("butterfly", 20, 3, 6), r("luna_moth", 10, 1, 5),
                r("cockateil", 10, 2, 4), r("firefly", 15, 3, 6), r("cliff_racer", 30, 3, 6), r("cloud_shark", 2, 1, 1),
                r("gold_fish", 10, 2, 4), r("fairy", 5, 2, 4), r("baryonyx", 2, 2, 4), r("bee", 2, 2, 4),
                r("cassowary", 2, 2, 4), r("dragonfly", 2, 2, 4), r("peacock", 2, 2, 4), r("stink_bug", 3, 2, 4),
                r("ostrich", 1, 1, 2), r("chipmunk", 1, 1, 2)));
        helper.succeed();
    }

    /** No spawn list of any biome holds a mob the config disables (the Boyfriend is off by default, as in 1.7.10). */
    @GameTest(template = "empty")
    public static void gh6d_disabled_mobs_are_in_no_list(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        List<String> bad = new ArrayList<>();
        for (var biome : biomes.entrySet()) {
            for (MobCategory category : MobCategory.values()) {
                for (MobSpawnSettings.SpawnerData data : biome.getValue().getMobSettings().getMobs(category).unwrap()) {
                    if (!ModSpawnControl.naturalSpawnEnabled(data.type)) {
                        bad.add(biome.getKey().location() + " " + category.getName() + " "
                                + BuiltInRegistries.ENTITY_TYPE.getKey(data.type));
                    }
                }
            }
        }
        helper.assertTrue(bad.isEmpty(), "disabled mobs still listed: " + bad);
        helper.assertTrue(!ModSpawnControl.naturalSpawnEnabled(ModEntities.BOYFRIEND.get()),
                "the Boyfriend is enabled by default (1.7.10's BoyfriendEnable defaulted to 0)");
        helper.succeed();
    }
}
