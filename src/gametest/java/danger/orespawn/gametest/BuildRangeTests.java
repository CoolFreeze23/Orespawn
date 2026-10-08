package danger.orespawn.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.OldLandBiomes;
import danger.orespawn.world.OreSpawnChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * WGEN-096 and WGEN-095: Utopia, the Village, Crystal and Mining keep the build range of earlier versions, Y-64 to 320,
 * so land saved in it keeps every block, and build the original's world inside it: the 1.7.10 terrain and surface from
 * Y0 to 256 as before, solid bedrock below, nothing above; spawning from Y0 in their new land, old land in its own
 * biome where the game filled in plains; their ores 1.7.10's decorator's, at its counts and heights.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BuildRangeTests {

    /** name, biome, noise settings, style. */
    private static final Object[][] ORIGINAL = {
            {"utopia", "utopia_plains", "legacy_utopia", DimensionStyle.UTOPIA},
            {"village", "village_biome", "legacy_utopia", DimensionStyle.VILLAGE},
            {"mining", "mining_biome", "legacy_extreme_hills", DimensionStyle.MINING},
            {"crystal", "crystal_plains", "legacy_crystal", DimensionStyle.CRYSTAL}};

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    private static OreSpawnChunkGenerator generator(MinecraftServer server, String biome, String settings, DimensionStyle style) {
        Holder<Biome> holder = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl(biome)));
        Holder<NoiseGeneratorSettings> noise = server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
                .getHolderOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, rl(settings)));
        return new OreSpawnChunkGenerator(new FixedBiomeSource(holder), noise, style);
    }

    @GameTest(template = "empty")
    public static void wgen096c_the_four_dimensions_keep_the_old_build_range(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Registry<DimensionType> types = server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        LevelHeightAccessor range = LevelHeightAccessor.create(-64, 384);
        for (Object[] d : ORIGINAL) {
            DimensionType type = types.get(rl((String) d[0]));
            helper.assertTrue(type != null && type.minY() == -64 && type.height() == 384 && type.logicalHeight() == 384,
                    d[0] + "'s build range is not Y-64 to 320: " + (type == null ? "none" : type.minY() + ", " + type.height()));
            OreSpawnChunkGenerator generator = generator(server, (String) d[1], (String) d[2], (DimensionStyle) d[3]);
            helper.assertTrue(generator.originalWorld() && GenerationRange.bottom(generator, range) == 0
                            && GenerationRange.top(generator, range) == 256,
                    d[0] + " does not generate in the original's Y0 to 256: " + GenerationRange.bottom(generator, range)
                            + " to " + GenerationRange.top(generator, range));
        }
        // the dimensions this leaves alone generate in their whole build range, whatever their noise covers (Chaos's
        // noise is 128 high in its 256-high range)
        for (Object[] d : new Object[][] {{"chaos_biome", "chaos", DimensionStyle.CHAOS},
                {"island_biome", "islands", DimensionStyle.ISLANDS}}) {
            OreSpawnChunkGenerator generator = generator(server, (String) d[0], (String) d[1], (DimensionStyle) d[2]);
            DimensionType type = types.get(rl((String) d[1]));
            LevelHeightAccessor own = LevelHeightAccessor.create(type.minY(), type.height());
            helper.assertTrue(!generator.originalWorld() && GenerationRange.bottom(generator, own) == type.minY()
                            && GenerationRange.top(generator, own) == type.minY() + type.height(),
                    d[1] + "'s generation range was narrowed: " + GenerationRange.bottom(generator, own) + " to "
                            + GenerationRange.top(generator, own));
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 24000)
    public static void wgen096d_a_new_chunk_is_bedrock_below_y0_and_empty_above_256(GameTestHelper helper) {
        OffThread.run(helper, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
            List<String> wrong = new ArrayList<>();
            int chunks = 0;
            for (Object[] d : ORIGINAL) {
                OreSpawnChunkGenerator generator = generator(server, (String) d[1], (String) d[2], (DimensionStyle) d[3]);
                RandomState state = RandomState.create(server.registryAccess().asGetterLookup(),
                        ResourceKey.create(Registries.NOISE_SETTINGS, rl((String) d[2])), 1007L);
                for (int[] at : new int[][] {{0, 0}, {-3, 5}, {1875, -1250}}) {
                    // the same chunk built in the original's 256-high world and in the build range kept
                    ProtoChunk original = build(helper, generator, state, biomes, at, LevelHeightAccessor.create(0, 256));
                    ProtoChunk kept = build(helper, generator, state, biomes, at, LevelHeightAccessor.create(-64, 384));
                    BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
                    int below = 0, above = 0, differ = 0;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int bx = at[0] * 16 + x, bz = at[1] * 16 + z;
                            for (int y = -64; y < 0; y++) if (!kept.getBlockState(p.set(bx, y, bz)).is(Blocks.BEDROCK)) below++;
                            for (int y = 256; y < 320; y++) if (!kept.getBlockState(p.set(bx, y, bz)).isAir()) above++;
                            for (int y = 0; y < 256; y++) {
                                BlockState a = original.getBlockState(p.set(bx, y, bz));
                                if (a != kept.getBlockState(p)) differ++;
                            }
                        }
                    }
                    chunks++;
                    if (below + above + differ > 0) {
                        wrong.add(d[0] + " chunk " + at[0] + "," + at[1] + ": " + below + " below Y0 not bedrock, " + above
                                + " from Y256 not air, " + differ + " from Y0 to 255 not the original's");
                    }
                }
            }
            helper.assertTrue(wrong.isEmpty() && chunks == 12, "chunks " + chunks + ": " + wrong);
            helper.succeed();
        });
    }

    /** A chunk filled by the dimension's generator as a level fills it, then surfaced by its original surface pass. */
    private static ProtoChunk build(GameTestHelper helper, OreSpawnChunkGenerator generator, RandomState state,
                                    Registry<Biome> biomes, int[] at, LevelHeightAccessor heights) {
        NoiseGeneratorSettings value = generator.generatorSettings().value();
        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        Aquifer.FluidStatus sea = new Aquifer.FluidStatus(value.seaLevel(), value.defaultFluid());
        Aquifer.FluidPicker picker = (x, y, z) -> y < Math.min(-54, value.seaLevel()) ? lava : sea;
        ProtoChunk chunk = new ProtoChunk(new ChunkPos(at[0], at[1]), UpgradeData.EMPTY, heights, biomes, null);
        chunk.fillBiomesFromNoise(generator.getBiomeSource(), state.sampler());
        chunk.getOrCreateNoiseChunk(c -> NoiseChunk.forChunk(c, state, LegacyTerrainReaderTests.NO_BEARD, value, picker,
                Blender.empty()));
        generator.fillFromNoise(Blender.empty(), state, helper.getLevel().structureManager(), chunk).join();
        helper.assertTrue(generator.buildOriginalSurface(chunk, state), "no original surface for " + generator.getStyle());
        return chunk;
    }

    @GameTest(template = "empty")
    public static void wgen095b_the_original_dimensions_place_the_1_7_10_ores(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
        List<String> decorator = List.of("dirt", "gravel", "coal", "iron", "gold", "redstone", "diamond", "lapis");
        for (String[] b : new String[][] {{"utopia_plains", ""}, {"village_biome", ""}, {"mining_biome", "hills"}}) {
            Biome biome = biomes.get(rl(b[0]));
            helper.assertTrue(biome != null, "no biome " + b[0]);
            List<HolderSet<PlacedFeature>> steps = biome.getGenerationSettings().features();
            List<String> ores = new ArrayList<>();
            for (Holder<PlacedFeature> f : steps.get(GenerationStep.Decoration.UNDERGROUND_ORES.ordinal())) {
                String id = f.unwrapKey().orElseThrow().location().toString();
                helper.assertTrue(!id.startsWith("minecraft:ore_"), b[0] + " still places " + id);
                if (id.endsWith("_1710")) ores.add(id.substring("orespawn:ore_".length(), id.length() - "_1710".length()));
            }
            List<String> expected = new ArrayList<>(decorator);
            if (!b[1].isEmpty()) expected.addAll(List.of("emerald", "silverfish"));
            helper.assertTrue(ores.equals(expected), b[0] + " places " + ores + ", not 1.7.10's " + expected);
            for (Holder<PlacedFeature> f : steps.get(GenerationStep.Decoration.FLUID_SPRINGS.ordinal())) {
                String id = f.unwrapKey().orElseThrow().location().toString();
                helper.assertTrue(id.startsWith("orespawn:spring_"), b[0] + " places the spring " + id);
            }
        }
        // the decorator's veins as BiomeDecorator.generateOres has them: the block, its size, veins a chunk, the height
        // provider and its range; then BiomeGenHills's silverfish stone (Mining)
        Object[][] table = {{"dirt", "minecraft:dirt", 32, 20, "uniform", 0, 255},
                {"gravel", "minecraft:gravel", 32, 10, "uniform", 0, 255},
                {"coal", "minecraft:coal_ore", 16, 20, "uniform", 0, 127},
                {"iron", "minecraft:iron_ore", 8, 20, "uniform", 0, 63},
                {"gold", "minecraft:gold_ore", 8, 2, "uniform", 0, 31},
                {"redstone", "minecraft:redstone_ore", 7, 8, "uniform", 0, 15},
                {"diamond", "minecraft:diamond_ore", 7, 1, "uniform", 0, 15},
                {"lapis", "minecraft:lapis_ore", 6, 1, "trapezoid", 0, 30},
                {"silverfish", "minecraft:infested_stone", 8, 7, "uniform", 0, 63}};
        var configured = server.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        for (Object[] row : table) {
            var feature = configured.get(rl("ore_" + row[0] + "_1710"));
            helper.assertTrue(feature != null && feature.config() instanceof OreConfiguration ore && ore.size == (Integer) row[2]
                    && ore.targetStates.size() == 1
                    && ore.targetStates.get(0).target.test(Blocks.STONE.defaultBlockState(), RandomSource.create(1L))
                    && !ore.targetStates.get(0).target.test(Blocks.DEEPSLATE.defaultBlockState(), RandomSource.create(1L))
                    && BuiltInRegistries.BLOCK.getKey(ore.targetStates.get(0).state.getBlock()).toString().equals(row[1]),
                    row[0] + "'s vein is not 1.7.10's: " + row[1] + " in stone, size " + row[2]);
            JsonObject placed = json(helper, "worldgen/placed_feature/ore_" + row[0] + "_1710.json");
            int count = -1, low = -1, high = -1;
            String provider = "";
            for (var step : placed.getAsJsonArray("placement")) {
                JsonObject modifier = step.getAsJsonObject();
                String type = modifier.get("type").getAsString();
                if (type.equals("minecraft:count")) count = modifier.get("count").getAsInt();
                if (type.equals("minecraft:height_range")) {
                    JsonObject height = modifier.getAsJsonObject("height");
                    provider = height.get("type").getAsString();
                    low = height.getAsJsonObject("min_inclusive").get("absolute").getAsInt();
                    high = height.getAsJsonObject("max_inclusive").get("absolute").getAsInt();
                }
            }
            helper.assertTrue(count == (Integer) row[3] && provider.equals("minecraft:" + row[4]) && low == (Integer) row[5]
                            && high == (Integer) row[6],
                    row[0] + ": " + count + " veins, " + provider + " Y" + low + "-" + high + ", not 1.7.10's " + row[3]
                            + ", " + row[4] + " Y" + row[5] + "-" + row[6]);
        }
        // BiomeGenHills's emeralds: 3 to 8 single blocks a chunk in stone at Y4 to 31
        var emerald = configured.get(rl("ore_emerald_1710"));
        helper.assertTrue(emerald != null && emerald.config() instanceof ReplaceBlockConfiguration replace
                        && replace.targetStates.size() == 1
                        && replace.targetStates.get(0).state.is(Blocks.EMERALD_ORE)
                        && replace.targetStates.get(0).target.test(Blocks.STONE.defaultBlockState(), RandomSource.create(1L))
                        && !replace.targetStates.get(0).target.test(Blocks.DEEPSLATE.defaultBlockState(), RandomSource.create(1L)),
                "the emeralds are not single emerald ores in stone");
        JsonObject emeralds = json(helper, "worldgen/placed_feature/ore_emerald_1710.json");
        String counter = "", provider = "";
        int fewest = -1, most = -1, low = -1, high = -1;
        for (var step : emeralds.getAsJsonArray("placement")) {
            JsonObject modifier = step.getAsJsonObject();
            String type = modifier.get("type").getAsString();
            if (type.equals("minecraft:count")) {
                JsonObject count = modifier.getAsJsonObject("count");
                counter = count.get("type").getAsString();
                fewest = count.get("min_inclusive").getAsInt();
                most = count.get("max_inclusive").getAsInt();
            }
            if (type.equals("minecraft:height_range")) {
                JsonObject height = modifier.getAsJsonObject("height");
                provider = height.get("type").getAsString();
                low = height.getAsJsonObject("min_inclusive").get("absolute").getAsInt();
                high = height.getAsJsonObject("max_inclusive").get("absolute").getAsInt();
            }
        }
        helper.assertTrue(counter.equals("minecraft:uniform") && fewest == 3 && most == 8
                        && provider.equals("minecraft:uniform") && low == 4 && high == 31,
                "the emeralds are " + counter + " " + fewest + "-" + most + " a chunk, " + provider + " Y" + low + "-"
                        + high + ", not uniform 3 to 8 a chunk at Y4-31");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wgen096e_spawning_starts_at_the_original_floor_in_new_land_only(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
        LevelHeightAccessor range = LevelHeightAccessor.create(-64, 384);
        OreSpawnChunkGenerator utopia = generator(server, "utopia_plains", "legacy_utopia", DimensionStyle.UTOPIA);
        RandomState state = RandomState.create(server.registryAccess().asGetterLookup(),
                ResourceKey.create(Registries.NOISE_SETTINGS, rl("legacy_utopia")), 1007L);
        // new land, built by the dimension's generator: solid bedrock under Y0
        ProtoChunk fresh = build(helper, utopia, state, biomes, new int[] {2, 3}, range);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        // land an earlier version generated: deepslate and a cave under Y0
        ProtoChunk old = new ProtoChunk(new ChunkPos(2, 3), UpgradeData.EMPTY, range, biomes, null);
        for (int y = -64; y < 0; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    old.setBlockState(p.set(32 + x, y, 48 + z), y == -10 ? Blocks.AIR.defaultBlockState()
                            : Blocks.DEEPSLATE.defaultBlockState(), false);
                }
            }
        }
        int newBottom = GenerationRange.spawnBottom(utopia, fresh, -64);
        int oldBottom = GenerationRange.spawnBottom(utopia, old, -64);
        OreSpawnChunkGenerator chaos = generator(server, "chaos_biome", "chaos", DimensionStyle.CHAOS);
        int chaosBottom = GenerationRange.spawnBottom(chaos, fresh, -64);
        helper.assertTrue(newBottom == 0 && oldBottom == -64 && chaosBottom == -64, "spawning starts at " + newBottom
                + " in new land, " + oldBottom + " in old land, " + chaosBottom + " in Chaos (want 0, -64, -64)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wgen096f_old_land_takes_the_dimensions_own_biome(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
        ResourceKey<Biome> own = ResourceKey.create(Registries.BIOME, rl("mining_biome"));
        Holder<Biome> mining = biomes.getHolderOrThrow(own);
        Holder<Biome> plains = biomes.getHolderOrThrow(Biomes.PLAINS);
        Holder<Biome> desert = biomes.getHolderOrThrow(Biomes.DESERT);
        // an old Mining chunk: its biome saved to Y192, plains over it, as the game fills sections a save lacks; and a
        // desert set by hand down one corner (with /fillbiome, say), which stays
        ProtoChunk chunk = new ProtoChunk(new ChunkPos(-5, 7), UpgradeData.EMPTY, LevelHeightAccessor.create(-64, 384),
                biomes, null);
        var sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            Holder<Biome> b = chunk.getSectionYFromSectionIndex(i) < 12 ? mining : plains;
            sections[i].fillBiomesFromNoise((x, y, z, s) -> x == 0 && z == 0 ? desert : b, null, 0, 0, 0);
        }
        boolean first = OldLandBiomes.refill(chunk, mining);
        int wrong = 0;
        for (var section : sections) {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    for (int z = 0; z < 4; z++) {
                        if (!section.getNoiseBiome(x, y, z).is(x == 0 && z == 0 ? Biomes.DESERT : own)) wrong++;
                    }
                }
            }
        }
        boolean second = OldLandBiomes.refill(chunk, mining);
        helper.assertTrue(first && wrong == 0 && !second, "the old chunk's plains were " + (first ? "" : "not ")
                + "refilled, " + wrong + " cells neither Mining's nor the desert set by hand, a second pass changed it "
                + second);
        helper.succeed();
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
}
