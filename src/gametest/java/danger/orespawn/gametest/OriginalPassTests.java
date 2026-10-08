package danger.orespawn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.BelowOrChancePlacement;
import danger.orespawn.world.LegacyChunkVein;
import danger.orespawn.world.feature.LegacyHillsDecorationFeature;
import danger.orespawn.world.feature.LegacyPlantsFeature;
import danger.orespawn.world.feature.SafeLakeFeature;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WGEN-099, WGEN-102, WGEN-097 and WGEN-101: the original's springs and lava-lake gate, Mining's extreme hills
 * decoration, OreSpawn's own ore pass as ChunkOreGenerator lays it, and Utopia's and Crystal's colours, against the
 * 1.7.10 code (the server jar's BiomeDecorator, BiomeGenHills, BiomeGenBase and WorldGenLiquids; OreSpawn's
 * ChunkOreGenerator and ChunkProviderOreSpawn2/3) and the reference server's OreSpawn.cfg. Their counts in a generated
 * world are measured against the reference world by tools/ref1710.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class OriginalPassTests {

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    private static JsonObject json(GameTestHelper helper, String path) {
        var resource = helper.getLevel().getServer().getResourceManager().getResource(rl(path));
        helper.assertTrue(resource.isPresent(), "no " + path);
        try (Reader reader = resource.get().openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(path, e);
        }
    }

    private static List<String> step(GameTestHelper helper, String biome, int step) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : json(helper, "worldgen/biome/" + biome + ".json").getAsJsonArray("features").get(step).getAsJsonArray()) {
            out.add(e.getAsString());
        }
        return out;
    }

    /** A biome's features in one step as the server runs them, after the biome modifiers. */
    private static List<String> liveStep(GameTestHelper helper, String biome, int step) {
        Biome b = helper.getLevel().getServer().registryAccess().registryOrThrow(Registries.BIOME)
                .get(ResourceKey.create(Registries.BIOME, rl(biome)));
        helper.assertTrue(b != null, "no " + biome);
        List<String> out = new ArrayList<>();
        for (Holder<PlacedFeature> f : b.getGenerationSettings().features().get(step)) {
            out.add(f.unwrapKey().map(k -> k.location().toString()).orElse("(direct)"));
        }
        return out;
    }

    private static JsonObject modifier(JsonObject placed, String type) {
        for (JsonElement e : placed.getAsJsonArray("placement")) {
            if (e.getAsJsonObject().get("type").getAsString().equals(type)) return e.getAsJsonObject();
        }
        return null;
    }

    @GameTest(template = "empty")
    public static void wgen099a_the_original_springs_and_the_lava_lake_gate(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Registry<ConfiguredFeature<?, ?>> configured = server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        // BiomeDecorator.genDecorations: 50 water springs at nextInt(nextInt(248) + 8), 20 lava springs; WorldGenLiquids
        // takes stone over and under and three stone sides and one of air
        for (String[] s : new String[][] {{"spring_water_1710", "water"}, {"spring_lava_1710", "lava"}}) {
            ConfiguredFeature<?, ?> f = configured.get(rl(s[0]));
            helper.assertTrue(f != null && f.config() instanceof SpringConfiguration spring && spring.requiresBlockBelow
                            && spring.rockCount == 4 && spring.holeCount == 1 && spring.validBlocks.size() == 1
                            && spring.validBlocks.get(0).is(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                            .getResourceKey(Blocks.STONE).orElseThrow())
                            && spring.state.getType().isSame(s[1].equals("water") ? Fluids.WATER : Fluids.LAVA),
                    s[0] + " is not 1.7.10's spring: " + (f == null ? "none" : f.config()));
        }
        JsonObject water = json(helper, "worldgen/placed_feature/spring_water_dim.json");
        JsonObject height = modifier(water, "minecraft:height_range").getAsJsonObject("height");
        helper.assertTrue(water.get("feature").getAsString().equals("orespawn:spring_water_1710")
                        && modifier(water, "minecraft:count").get("count").getAsInt() == 50
                        && height.get("type").getAsString().equals("minecraft:biased_to_bottom")
                        && height.getAsJsonObject("min_inclusive").get("absolute").getAsInt() == 0
                        && height.getAsJsonObject("max_inclusive").get("absolute").getAsInt() == 255
                        && height.get("inner").getAsInt() == 8,
                "the water springs are not 50 a chunk at nextInt(nextInt(248) + 8): " + water);
        JsonObject lava = json(helper, "worldgen/placed_feature/spring_lava_dim.json");
        helper.assertTrue(lava.get("feature").getAsString().equals("orespawn:spring_lava_1710")
                        && modifier(lava, "minecraft:count").get("count").getAsInt() == 20,
                "the lava springs are not 20 a chunk of 1.7.10's: " + lava);
        for (String biome : new String[] {"utopia_plains", "village_biome", "mining_biome"}) {
            List<String> springs = step(helper, biome, 8);
            helper.assertTrue(springs.equals(List.of("orespawn:spring_water_dim", "orespawn:spring_lava_dim")),
                    biome + "'s springs are " + springs);
        }
        // ChunkProviderOreSpawn2.java:317 and 3.java:302: a lava lake at y < 63, or one in ten above
        JsonObject lake = json(helper, "worldgen/placed_feature/lake_lava_dim.json");
        JsonObject gate = modifier(lake, "orespawn:below_or_chance");
        helper.assertTrue(gate != null && gate.get("y").getAsInt() == 63 && gate.get("chance").getAsInt() == 10,
                "the lava lakes have no 1.7.10 gate: " + lake);
        BelowOrChancePlacement placement = new BelowOrChancePlacement(63, 10);
        RandomSource random = RandomSource.create(72L);
        int under = 0, over = 0;
        for (int i = 0; i < 20_000; i++) {
            under += (int) placement.getPositions(null, random, new BlockPos(0, i % 63, 0)).count();
            over += (int) placement.getPositions(null, random, new BlockPos(0, 63 + i % 193, 0)).count();
        }
        helper.assertTrue(under == 20_000 && over > 1_800 && over < 2_200,
                "the gate kept " + under + " of 20,000 lakes under Y63 and " + over + " of 20,000 over it (want all, ~2,000)");
        // WorldGenLakes shells its lava lakes in stone and its water lakes in nothing (line 102, Material.lava)
        for (String[] l : new String[][] {{"lake_water", "air"}, {"lake_lava", "stone"}}) {
            ConfiguredFeature<?, ?> f = configured.get(rl(l[0]));
            helper.assertTrue(f != null && f.config() instanceof LakeFeature.Configuration c
                            && c.barrier().getState(random, BlockPos.ZERO).is(l[1].equals("air") ? Blocks.AIR : Blocks.STONE),
                    l[0] + "'s shell is not " + l[1] + ": " + (f == null ? "none" : f.config()));
        }
        // ChunkProviderOreSpawn3.java:292 and 298: no lake where the Village's villages reach the chunk's population
        // window, which meets the chunk and its east, south and south-east neighbours
        Structure village = server.registryAccess().registryOrThrow(Registries.STRUCTURE).get(rl("dim_village"));
        helper.assertTrue(village != null, "no orespawn:dim_village");
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(BlockPos.ZERO);
        int cx = (at.getX() >> 4) + 1, cz = (at.getZ() >> 4) + 1;
        helper.assertFalse(SafeLakeFeature.inVillageLand(level, cx, cz), "village land with no village");
        String wrong = null;
        for (int[] d : new int[][] {{0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1}, {-1, 0, 0}, {0, -1, 0}, {2, 0, 0}}) {
            ChunkAccess chunk = level.getChunk(cx + d[0], cz + d[1]);
            Map<Structure, LongSet> saved = new HashMap<>();
            chunk.getAllReferences().forEach((k, v) -> saved.put(k, new LongOpenHashSet(v)));
            try {
                chunk.addReferenceForStructure(village, ChunkPos.asLong(cx + 4, cz + 4));
                if (SafeLakeFeature.inVillageLand(level, cx, cz) != (d[2] == 1)) {
                    wrong = "a village referenced at " + d[0] + ", " + d[1] + (d[2] == 1 ? " lets a lake in" : " keeps a lake out");
                }
            } finally {
                chunk.setAllReferences(saved);
            }
        }
        helper.assertTrue(wrong == null, String.valueOf(wrong));
        helper.assertFalse(SafeLakeFeature.inVillageLand(level, cx, cz), "the references were not restored");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wgen102a_mining_takes_the_extreme_hills_decoration(GameTestHelper helper) {
        List<String> vegetal = step(helper, "mining_biome", 9);
        helper.assertTrue(vegetal.size() >= 2 && vegetal.get(0).equals("orespawn:hills_decoration_1710")
                        && vegetal.get(1).equals("orespawn:lava_and_water"),
                "Mining's vegetation step is " + vegetal);
        // BiomeGenHills.func_150567_a: a spruce two in three, else BiomeGenBase's (a big oak one in ten, else an oak);
        // BiomeGenBase.func_150572_a: a dandelion two in three, else a poppy
        RandomSource random = RandomSource.create(1007L);
        int n = 60_000, spruce = 0, fancy = 0, oak = 0, dandelion = 0;
        for (int i = 0; i < n; i++) {
            switch (LegacyHillsDecorationFeature.tree(random)) {
                case SPRUCE -> spruce++;
                case FANCY_OAK -> fancy++;
                case OAK -> oak++;
            }
            if (LegacyHillsDecorationFeature.flower(random).is(Blocks.DANDELION)) dandelion++;
        }
        helper.assertTrue(Math.abs(spruce / (double) n - 2 / 3.0) < 0.01 && Math.abs(fancy / (double) n - 1 / 30.0) < 0.004
                        && Math.abs(oak / (double) n - 0.3) < 0.01 && Math.abs(dandelion / (double) n - 2 / 3.0) < 0.01,
                "trees spruce " + spruce + ", big oak " + fancy + ", oak " + oak + ", dandelions " + dandelion + " of " + n);
        helper.succeed();
    }

    /** The reference server's OreSpawn.cfg clumpsizes for the ores ChunkOreGenerator lays. */
    private static final Object[][] SIZES = {{"ore_amethyst", 6}, {"ore_salt", 12}, {"ore_titanium", 4},
            {"ore_uranium", 4}, {"red_ant_troll", 4}, {"termite_troll", 4}, {"ore_boost_diamond", 6},
            {"ore_boost_diamond_block", 4}, {"ore_boost_emerald", 6}, {"ore_boost_emerald_block", 4},
            {"ore_boost_gold", 8}, {"ore_boost_gold_block", 4}, {"ore_block_ruby", 2}};

    /** ChunkOreGenerator.generateOresInChunk's order: the spawn ores, the four ores, the trolls, then the boosts. */
    private static final String[] PASS = {"spawn_ores", "ore_uranium", "ore_titanium", "ore_amethyst", "ore_salt",
            "red_ant_troll", "termite_troll", "ore_boost_diamond", "ore_boost_diamond_block", "ore_boost_emerald",
            "ore_boost_emerald_block", "ore_boost_gold", "ore_boost_gold_block", "ore_block_ruby"};

    @GameTest(template = "empty")
    public static void wgen097a_the_orespawn_ore_pass_as_chunkoregenerator_lays_it(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Registry<ConfiguredFeature<?, ?>> configured = server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        List<String> wrong = new ArrayList<>();
        for (Object[] row : SIZES) {
            String name = (String) row[0];
            ConfiguredFeature<?, ?> f = configured.get(rl("chunk_" + name));
            if (f == null || !(f.config() instanceof OreConfiguration ore) || ore.size != (Integer) row[1]
                    || !net.minecraft.core.registries.BuiltInRegistries.FEATURE.getKey(f.feature()).equals(rl("chunk_ore"))) {
                wrong.add("chunk_" + name);
            }
            // Utopia's and the Village's, and Mining's, through the chunk's window without in_square
            for (String placed : new String[] {name + "_dim", name + "_mining"}) {
                JsonObject p = json(helper, "worldgen/placed_feature/" + placed + ".json");
                JsonObject count = modifier(p, "orespawn:vein_count");
                if (!p.get("feature").getAsString().equals("orespawn:chunk_" + name) || count == null
                        || !count.has("chunk_window") || !count.get("chunk_window").getAsBoolean()
                        || modifier(p, "minecraft:in_square") != null) {
                    wrong.add(placed);
                }
            }
            // the overworld's keeps its own (OreSpawnWorld.generateOres, laid whole)
            JsonObject overworld = json(helper, "worldgen/placed_feature/" + name + ".json");
            if (!overworld.get("feature").getAsString().equals("orespawn:" + name) || modifier(overworld, "minecraft:in_square") == null) {
                wrong.add(name + " (the overworld's)");
            }
        }
        helper.assertTrue(wrong.isEmpty(), "not as ChunkOreGenerator lays them: " + wrong);
        // the pass runs as the chunk is built, before the biome's decorator lays its ores, in ChunkOreGenerator's order
        // Chaos runs the same pass once a chunk as Utopia (ChunkProviderOreSpawn6.java:197)
        for (String[] b : new String[][] {{"utopia_plains", "_dim"}, {"village_biome", "_dim"}, {"mining_biome", "_mining"},
                {"chaos_biome", "_dim"}}) {
            List<String> ores = liveStep(helper, b[0], 6);
            List<String> pass = new ArrayList<>();
            for (String name : PASS) pass.add("orespawn:" + name + b[1]);
            boolean overworlds = false;
            for (Object[] row : SIZES) overworlds |= ores.contains("orespawn:" + row[0]);
            helper.assertTrue(ores.size() > pass.size() && ores.subList(0, pass.size()).equals(pass)
                            && ores.stream().filter(pass::contains).count() == pass.size() && !overworlds,
                    b[0] + " does not run the original's pass first and once: " + ores);
        }
        // and its decorator lays 1.7.10's ore set after it, as in the other three: no 1.21 ore left
        List<String> chaos = liveStep(helper, "chaos_biome", 6);
        List<String> set1710 = List.of("orespawn:ore_dirt_1710", "orespawn:ore_gravel_1710", "orespawn:ore_coal_1710",
                "orespawn:ore_iron_1710", "orespawn:ore_gold_1710", "orespawn:ore_redstone_1710", "orespawn:ore_diamond_1710",
                "orespawn:ore_lapis_1710");
        helper.assertTrue(chaos.subList(PASS.length, PASS.length + set1710.size()).equals(set1710)
                        && chaos.stream().noneMatch(f -> f.startsWith("minecraft:ore_")),
                "Chaos's ores: " + chaos);
        JsonObject spawnDim = json(helper, "worldgen/configured_feature/spawn_ores_dim.json").getAsJsonObject("config");
        JsonObject spawnMining = json(helper, "worldgen/configured_feature/spawn_ores_mining.json").getAsJsonObject("config");
        helper.assertTrue(spawnDim.get("chunk_veins").getAsBoolean() && spawnMining.get("chunk_veins").getAsBoolean(),
                "the spawn ores: " + spawnDim + ", " + spawnMining);
        // the vein: centred 8 in from its origin, and of what lies outside the origin's chunk nothing laid
        RandomSource random = RandomSource.create(97L);
        long cells = 0, kept = 0;
        double sx = 0, sz = 0;
        for (int i = 0; i < 4_000; i++) {
            BlockPos origin = new BlockPos(3 + random.nextInt(10), 40, 3 + random.nextInt(10));
            long[] tally = new long[2];
            double[] sum = new double[2];
            LegacyChunkVein.cells(random, origin, 4, (x, y, z) -> {
                tally[0]++;
                sum[0] += x - origin.getX();
                sum[1] += z - origin.getZ();
                if (LegacyChunkVein.keeps(0, 0, x, y, z, 0, 256)) tally[1]++;
            });
            cells += tally[0];
            kept += tally[1];
            sx += sum[0];
            sz += sum[1];
        }
        double cx = sx / cells, cz = sz / cells, share = kept / (double) cells;
        helper.assertTrue(Math.abs(cx - 7.5) < 0.3 && Math.abs(cz - 7.5) < 0.3 && share > 0.2 && share < 0.5,
                "the veins' cells sit " + cx + ", " + cz + " in from their origins (want about 7.5: a block 8 in, its cell index half a block under the centre) and " + share
                        + " of them inside the chunk (want a quarter to a half)");
        // the placement's window: 3 + nextInt(10) into the chunk on x and z, Y in the ore's band
        var window = new danger.orespawn.world.OreSpawnVeinPlacement(40, 0, 10, 20, 1, java.util.Optional.empty(), 1, true);
        List<BlockPos> at = window.getPositions(null, random, new BlockPos(32, -64, -48)).toList();
        boolean inside = !at.isEmpty();
        for (BlockPos p : at) {
            inside &= p.getX() >= 35 && p.getX() <= 44 && p.getZ() >= -45 && p.getZ() <= -36 && p.getY() >= 10 && p.getY() <= 20;
        }
        helper.assertTrue(inside, "the window's positions: " + at);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wgen101a_the_dimensions_take_the_originals_climate_and_colours(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().getServer().registryAccess().registryOrThrow(Registries.BIOME);
        // 1.7.10: each provider sets its one biome's climate as the world loads (Utopia's and the Village's 0.7 and 0.5,
        // WorldProviderOreSpawn.java:54 and 3.java:34; Mining's, the Islands', Crystal's and Chaos's 0.8 and 0.01,
        // 2.java:29, 4.java:33, 5.java:34 and 6.java:33) and none sets a colour: the overworld's fog, vanilla's water,
        // getSkyColorByTemp through AWT's HSB rounding and the colormaps at the climate. Chaos's red is modern.chaosRed's,
        // off by default.
        Object[][] want = {{"utopia_plains", 0.7F, 0.5F, 0x7AA6FF}, {"village_biome", 0.7F, 0.5F, 0x7AA6FF},
                {"mining_biome", 0.8F, 0.01F, 0x79A7FF}, {"island_biome", 0.8F, 0.01F, 0x79A7FF},
                {"crystal_plains", 0.8F, 0.01F, 0x79A7FF}, {"chaos_biome", 0.8F, 0.01F, 0x79A7FF}};
        helper.assertFalse(danger.orespawn.OreSpawnConfig.chaosRed(), "modern.chaosRed is on by default");
        for (Object[] w : want) {
            String name = (String) w[0];
            Biome biome = biomes.get(ResourceKey.create(Registries.BIOME, rl(name)));
            helper.assertTrue(biome != null, "no " + name);
            BiomeSpecialEffects e = biome.getSpecialEffects();
            helper.assertTrue(Math.abs(biome.getBaseTemperature() - (Float) w[1]) < 1e-6
                            && Math.abs(biome.getModifiedClimateSettings().downfall() - (Float) w[2]) < 1e-6
                            && e.getGrassColorOverride().isEmpty() && e.getFoliageColorOverride().isEmpty()
                            && e.getSkyColor() == (Integer) w[3] && e.getFogColor() == 0xC0D8FF
                            && e.getWaterColor() == 0x3F76E4 && e.getWaterFogColor() == 0x050533,
                    name + ": " + biome.getBaseTemperature() + " and " + biome.getModifiedClimateSettings().downfall()
                            + ", sky " + Integer.toHexString(e.getSkyColor()) + ", fog " + Integer.toHexString(e.getFogColor())
                            + ", water " + Integer.toHexString(e.getWaterColor()) + ", grass " + e.getGrassColorOverride()
                            + ", foliage " + e.getFoliageColorOverride());
        }
        // the red the option brings back, on the Chaos biome only
        JsonObject red = json(helper, "neoforge/biome_modifier/chaos_red.json");
        helper.assertTrue(red.get("type").getAsString().equals("orespawn:chaos_red_colours")
                        && red.get("biomes").getAsString().equals("orespawn:chaos_biome")
                        && red.get("sky_color").getAsInt() == 0x660000 && red.get("water_color").getAsInt() == 0x330000,
                "the red Chaos: " + red);
        helper.succeed();
    }

    /** WGEN-103, WGEN-105, WGEN-109: the decorator's plants in each of the original's dimensions, at its counts. */
    @GameTest(template = "empty")
    public static void wgen103a_the_decorators_plants_in_the_originals_dimensions(GameTestHelper helper) {
        Registry<ConfiguredFeature<?, ?>> configured = helper.getLevel().getServer().registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE);
        // BiomeGenUtopianPlains: the constructor's 4 flowers and 6 grass (Utopia, the Village), setCrystalCreatures' and
        // setChaosCreatures' -999s and 2 and 4; Mining's flowers and grass are hills_decoration_1710's
        Object[][] want = {{"legacy_plants_utopian", 4, 6, 0, 0}, {"legacy_plants_chaos", 2, 4, -999, -999},
                {"legacy_plants_crystal", -999, -999, -999, -999}, {"legacy_plants_hills", 0, 0, 0, 0}};
        for (Object[] w : want) {
            ConfiguredFeature<?, ?> f = configured.get(rl((String) w[0]));
            helper.assertTrue(f != null && f.config() instanceof LegacyPlantsFeature.Config c
                            && c.flowersPerChunk() == (Integer) w[1] && c.grassPerChunk() == (Integer) w[2]
                            && c.mushroomsPerChunk() == (Integer) w[3] && c.reedsPerChunk() == (Integer) w[4],
                    w[0] + ": " + (f == null ? "none" : f.config()));
        }
        String[][] wiring = {{"utopia_plains", "legacy_plants_utopian"}, {"village_biome", "legacy_plants_utopian"},
                {"chaos_biome", "legacy_plants_chaos"}, {"crystal_plains", "legacy_plants_crystal"},
                {"mining_biome", "legacy_plants_hills"}};
        for (String[] w : wiring) {
            List<String> vegetal = liveStep(helper, w[0], 9);
            boolean vanilla = vegetal.stream().anyMatch(f -> f.startsWith("minecraft:flower_") || f.startsWith("minecraft:patch_")
                    || f.endsWith("_mushroom_normal"));
            helper.assertTrue(vegetal.contains("orespawn:" + w[1]) && !vanilla, w[0] + "'s plants: " + vegetal);
        }
        helper.succeed();
    }

    /** The light 1.7.10 had while decorating, which the plants read: the column's, less each block's opacity above. */
    @GameTest(template = "empty")
    public static void wgen103b_the_plants_read_the_originals_light(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(0, 1, 0));
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        // clear the column far above, then three layers of leaves and a layer of water over the cell
        for (int y = 1; y < 40; y++) level.setBlock(at.set(base.getX(), base.getY() + y, base.getZ()), Blocks.AIR.defaultBlockState(), 2);
        int open = danger.orespawn.world.LegacyLight.sky(level, base.getX(), base.getY(), base.getZ());
        for (int y = 2; y <= 4; y++) level.setBlock(at.set(base.getX(), base.getY() + y, base.getZ()), Blocks.OAK_LEAVES.defaultBlockState(), 2);
        int leaves = danger.orespawn.world.LegacyLight.sky(level, base.getX(), base.getY(), base.getZ());
        level.setBlock(at.set(base.getX(), base.getY() + 6, base.getZ()), Blocks.WATER.defaultBlockState(), 2);
        int water = danger.orespawn.world.LegacyLight.sky(level, base.getX(), base.getY(), base.getZ());
        level.setBlock(at.set(base.getX(), base.getY() + 8, base.getZ()), Blocks.STONE.defaultBlockState(), 2);
        int roofed = danger.orespawn.world.LegacyLight.sky(level, base.getX(), base.getY(), base.getZ());
        for (int y = 1; y < 40; y++) level.setBlock(at.set(base.getX(), base.getY() + y, base.getZ()), Blocks.AIR.defaultBlockState(), 2);
        helper.assertTrue(open == 15 && leaves == 12 && water == 9 && roofed == 0,
                "the light under nothing, three leaves, water, stone: " + open + ", " + leaves + ", " + water + ", " + roofed);
        helper.succeed();
    }

    /** WGEN-105: the caves' lava to Y10, as 1.7.10's carvers leave it; Mining's ruby after the springs. */
    @GameTest(template = "empty")
    public static void wgen105a_the_caves_lava_and_the_rubys_order(GameTestHelper helper) {
        // MapGenCaves and MapGenRavine test y < 10 one block under the cell they carve, so the lava stands to Y10
        for (String carver : new String[] {"legacy_cave", "legacy_canyon"}) {
            JsonObject c = json(helper, "worldgen/configured_carver/" + carver + ".json").getAsJsonObject("config");
            int lava = c.getAsJsonObject("lava_level").get("absolute").getAsInt();
            helper.assertTrue(lava == 10, carver + "'s lava stands to Y" + lava);
        }
        // OreSpawnWorld.generateRuby runs after the provider's population, its springs' lava there to find
        List<String> ores = liveStep(helper, "mining_biome", 6);
        List<String> springs = liveStep(helper, "mining_biome", 8);
        helper.assertTrue(!ores.contains("orespawn:ore_ruby_mining")
                        && springs.indexOf("orespawn:ore_ruby_mining") > springs.indexOf("orespawn:spring_lava_dim"),
                "Mining's ruby: ores " + ores + ", springs " + springs);
        helper.succeed();
    }
}
