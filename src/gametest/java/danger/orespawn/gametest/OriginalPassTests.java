package danger.orespawn.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.BelowOrChancePlacement;
import danger.orespawn.world.LegacyChunkVein;
import danger.orespawn.world.feature.LegacyHillsDecorationFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

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
        for (String biome : new String[] {"utopia_plains", "village_biome"}) {
            List<String> ores = step(helper, biome, 6);
            for (Object[] row : SIZES) {
                helper.assertTrue(ores.contains("orespawn:" + row[0] + "_dim") && !ores.contains("orespawn:" + row[0]),
                        biome + " does not run " + row[0] + " as the original's pass: " + ores);
            }
        }
        JsonObject spawnDim = json(helper, "worldgen/configured_feature/spawn_ores_dim.json").getAsJsonObject("config");
        JsonObject spawnMining = json(helper, "worldgen/configured_feature/spawn_ores_mining.json").getAsJsonObject("config");
        JsonObject spawnChaos = json(helper, "worldgen/configured_feature/spawn_ores_chaos.json").getAsJsonObject("config");
        JsonArray dims = json(helper, "neoforge/biome_modifier/add_spawn_ores_dims.json").getAsJsonArray("biomes");
        helper.assertTrue(spawnDim.get("chunk_veins").getAsBoolean() && spawnMining.get("chunk_veins").getAsBoolean()
                        && !spawnChaos.has("chunk_veins") && dims.size() == 2,
                "the spawn ores: " + spawnDim + ", " + spawnMining + ", Chaos " + spawnChaos + ", " + dims);
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
    public static void wgen101a_utopia_and_crystal_take_the_originals_colours(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().getServer().registryAccess().registryOrThrow(Registries.BIOME);
        for (String name : new String[] {"utopia_plains", "crystal_plains"}) {
            Biome biome = biomes.get(ResourceKey.create(Registries.BIOME, rl(name)));
            helper.assertTrue(biome != null, "no " + name);
            BiomeSpecialEffects e = biome.getSpecialEffects();
            // 1.7.10: getSkyColorByTemp(0.7), the overworld's fog, vanilla's water; the colormaps at 0.7 and 0.5
            boolean grass = name.equals("utopia_plains")
                    ? e.getGrassColorOverride().isEmpty() && e.getFoliageColorOverride().isEmpty()
                    && Math.abs(biome.getBaseTemperature() - 0.7F) < 1e-6 && Math.abs(biome.getModifiedClimateSettings().downfall() - 0.5F) < 1e-6
                    : e.getGrassColorOverride().orElse(0) == 0x8CBD5F && e.getFoliageColorOverride().orElse(0) == 0x70AB38;
            helper.assertTrue(e.getSkyColor() == 0x7AA6FF && e.getFogColor() == 0xC0D8FF && e.getWaterColor() == 0x3F76E4
                            && e.getWaterFogColor() == 0x050533 && grass,
                    name + "'s colours: sky " + Integer.toHexString(e.getSkyColor()) + ", fog " + Integer.toHexString(e.getFogColor())
                            + ", water " + Integer.toHexString(e.getWaterColor()) + ", grass " + e.getGrassColorOverride()
                            + ", foliage " + e.getFoliageColorOverride());
        }
        helper.succeed();
    }
}
