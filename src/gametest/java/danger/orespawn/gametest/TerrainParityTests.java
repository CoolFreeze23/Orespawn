package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.ModBlocks;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.LegacySurface;
import danger.orespawn.world.LegacyTerrainNoise;
import danger.orespawn.world.LegacyTerrainReader;
import danger.orespawn.world.OreSpawnChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GitHub #6, "mountains get extreme about 4k blocks out": Utopia, the Village, Crystal and Mining stood on 1.18's
 * overworld noise, where the original built all four on its copy of the 1.7.10 overworld generator
 * (ChunkProviderOreSpawn{,2,3,5}.func_147423_a), one biome each. These pin the four dimension files to the 1.7.10 terrain, its
 * seeding through the dimension's own RandomState, and the shape: the heights over a 32,000-block square stay within
 * the original's for each biome height.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TerrainParityTests {

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    private static final Map<String, String> STEMS = Map.of("utopia", "legacy_utopia", "village", "legacy_utopia",
            "crystal", "legacy_crystal", "mining", "legacy_extreme_hills");

    /**
     * The four dimension files point their generators at the 1.7.10 terrain (read from the files: the game-test server
     * creates no dimension of its own).
     */
    @GameTest(template = "empty")
    public static void gh6t_a_the_four_dimensions_stand_on_the_1_7_10_terrain(GameTestHelper helper) {
        var resources = helper.getLevel().getServer().getResourceManager();
        for (var entry : STEMS.entrySet()) {
            ResourceLocation file = rl("dimension/" + entry.getKey() + ".json");
            var resource = resources.getResource(file);
            helper.assertTrue(resource.isPresent(), "no " + file);
            JsonObject json;
            try (Reader reader = resource.get().openAsReader()) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                throw new AssertionError("unreadable " + file + ": " + e);
            }
            String settings = json.getAsJsonObject("generator").get("settings").getAsString();
            helper.assertTrue(settings.equals("orespawn:" + entry.getValue()),
                    entry.getKey() + " uses " + settings + ", not orespawn:" + entry.getValue());
        }
        helper.succeed();
    }

    private static RandomState state(MinecraftServer server, String settings, long seed) {
        return RandomState.create(server.registryAccess().asGetterLookup(),
                ResourceKey.create(Registries.NOISE_SETTINGS, rl(settings)), seed);
    }

    /**
     * The dimension's RandomState hands the terrain the original's Random (legacy_random_source): the router's density
     * at the cell corners is LegacyTerrainNoise seeded with the world seed, so a seed gives the 1.7.10 world's terrain.
     */
    @GameTest(template = "empty")
    public static void gh6t_b_the_terrain_is_seeded_as_the_original(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Object[][] cases = {{"legacy_utopia", 0.1F, 0.2F}, {"legacy_crystal", 0.1F, 0.5F}, {"legacy_extreme_hills", 1.0F, 0.5F}};
        for (long seed : new long[] {12345L, -4172144997902289642L}) {
            for (Object[] c : cases) {
                DensityFunction routed = state(server, (String) c[0], seed).router().finalDensity();
                LegacyTerrainNoise reference = LegacyTerrainNoise.seeded(seed, (Float) c[1], (Float) c[2]);
                for (int i = 0; i < 40; i++) {
                    int x = (i * 1237 - 20000) & ~3;
                    int z = (i * -2903 + 9000) & ~3;
                    int y = ((i * 37) % 33) * 8;
                    DensityFunction.SinglePointContext at = new DensityFunction.SinglePointContext(x, y, z);
                    double got = routed.compute(at);
                    double want = reference.compute(at);
                    helper.assertTrue(got == want, c[0] + " at seed " + seed + " (" + x + ", " + y + ", " + z
                            + "): the dimension's density " + got + " is not the original's " + want);
                }
            }
        }
        helper.succeed();
    }

    /**
     * The shape the report was about: the surface over a 32,000-block square, 441 columns per seed. With one biome all
     * round the 1.7.10 generator keeps Utopia and the Village near sea level (BiomeGenBase's 0.1 height, 0.2 variation),
     * Crystal a little rougher (0.5) and Mining in extreme hills (1.0, 0.5); nothing climbs into the mountains the 1.18
     * noise raised thousands of blocks out.
     */
    @GameTest(template = "empty", timeoutTicks = 24000)
    public static void gh6t_c_no_mountains_far_out(GameTestHelper helper) {
        OffThread.run(helper, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            LevelHeightAccessor heights = LevelHeightAccessor.create(0, 256);
            Object[][] cases = {{"utopia_plains", "legacy_utopia", DimensionStyle.UTOPIA, 50, 100},
                    {"crystal_plains", "legacy_crystal", DimensionStyle.CRYSTAL, 45, 125},
                    {"mining_biome", "legacy_extreme_hills", DimensionStyle.MINING, 60, 145}};
            for (Object[] c : cases) {
                Holder<Biome> biome = server.registryAccess().registryOrThrow(Registries.BIOME)
                        .getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl((String) c[0])));
                Holder<NoiseGeneratorSettings> settings = server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
                        .getHolderOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, rl((String) c[1])));
                // as the dimension file builds it: orespawn:orespawn on the fixed biome and the settings
                ChunkGenerator generator = new OreSpawnChunkGenerator(new FixedBiomeSource(biome), settings, (DimensionStyle) c[2]);
                for (long seed : new long[] {12345L, 987654321L}) {
                    RandomState state = state(server, (String) c[1], seed);
                    int lowest = Integer.MAX_VALUE, highest = Integer.MIN_VALUE;
                    for (int x = -16000; x <= 16000; x += 1600) {
                        for (int z = -16000; z <= 16000; z += 1600) {
                            int top = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, state);
                            lowest = Math.min(lowest, top);
                            highest = Math.max(highest, top);
                        }
                    }
                    helper.assertTrue(lowest >= (Integer) c[3] && highest <= (Integer) c[4], c[1] + " at seed " + seed
                            + ": surface from " + lowest + " to " + highest + ", outside the original's " + c[3] + "-" + c[4]);
                }
            }
        });
    }

    /** {dimension, biome, noise settings, style, surface}. */
    private static final Object[][] LEGACY = {
            {"utopia", "utopia_plains", "legacy_utopia", DimensionStyle.UTOPIA, LegacySurface.Kind.PLAINS},
            {"village", "village_biome", "legacy_utopia", DimensionStyle.VILLAGE, LegacySurface.Kind.PLAINS},
            {"mining", "mining_biome", "legacy_extreme_hills", DimensionStyle.MINING, LegacySurface.Kind.EXTREME_HILLS},
            {"crystal", "crystal_plains", "legacy_crystal", DimensionStyle.CRYSTAL, LegacySurface.Kind.CRYSTAL}};

    /**
     * A block's code in the 1.7.10 columns: 0 air, 1 stone, 2 grass, 3 dirt, 4 bedrock, 5 water, 6 gravel, 7 sand,
     * 8 sandstone, 9 crystal stone, 10 crystal grass, 11 ice, 15 anything else.
     */
    private static int code(BlockState state) {
        if (state.isAir()) return 0;
        if (state.is(Blocks.STONE)) return 1;
        if (state.is(Blocks.GRASS_BLOCK)) return 2;
        if (state.is(Blocks.DIRT)) return 3;
        if (state.is(Blocks.BEDROCK)) return 4;
        if (state.is(Blocks.WATER)) return 5;
        if (state.is(Blocks.GRAVEL)) return 6;
        if (state.is(Blocks.SAND)) return 7;
        if (state.is(Blocks.SANDSTONE)) return 8;
        if (state.is(ModBlocks.CRYSTAL_STONE.get())) return 9;
        if (state.is(ModBlocks.CRYSTAL_GRASS.get())) return 10;
        if (state.is(Blocks.ICE)) return 11;
        return 15;
    }

    /**
     * The original itself at seed 1007: OreSpawn 1.7.10 (20.3, on Forge 10.13.4.1614) asked each dimension's chunk
     * provider for 36 chunks round the origin and 9 some 2,000 blocks east, as provideChunk asks it, before the caves
     * (terrain_1710/NAME.bin: per column, z outer and x inner, the highest stone and water of the terrain, the highest
     * block of the surfaced column that is neither air nor water and its code, the surface noise, and an FNV-1a hash of
     * the surfaced column's codes from Y0 to Y255). Each chunk here filled by the dimension's generator as a level fills
     * it, then surfaced by the dimension's legacy surface: every column's terrain, surface noise and surfaced blocks are
     * the original's.
     */
    @GameTest(template = "empty", timeoutTicks = 24000)
    public static void gh6t_d_terrain_and_surface_are_1_7_10_column_for_column(GameTestHelper helper) {
        OffThread.run(helper, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            LevelHeightAccessor heights = LevelHeightAccessor.create(0, 256);
            Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);
            List<String> differ = new ArrayList<>();
            long columns = 0;
            long water = 0;
            long stoneTops = 0;
            for (Object[] d : LEGACY) {
                Holder<Biome> biome = biomes.getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl((String) d[1])));
                Holder<NoiseGeneratorSettings> settings = server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
                        .getHolderOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, rl((String) d[2])));
                NoiseBasedChunkGenerator generator = new OreSpawnChunkGenerator(new FixedBiomeSource(biome), settings,
                        (DimensionStyle) d[3]);
                RandomState state = state(server, (String) d[2], 1007L);
                LegacyTerrainNoise noise = LegacyTerrainReader.legacyNoise(state);
                helper.assertTrue(noise != null, d[0] + " has no legacy noise");
                NoiseGeneratorSettings value = settings.value();
                Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
                Aquifer.FluidStatus sea = new Aquifer.FluidStatus(value.seaLevel(), value.defaultFluid());
                Aquifer.FluidPicker picker = (x, y, z) -> y < Math.min(-54, value.seaLevel()) ? lava : sea;
                for (String where : new String[] {"near", "far"}) {
                    String file = "/terrain_1710/" + d[0] + "_" + where + ".bin";
                    try (InputStream raw = TerrainParityTests.class.getResourceAsStream(file)) {
                        helper.assertTrue(raw != null, "no " + file);
                        DataInputStream in = new DataInputStream(new BufferedInputStream(raw));
                        helper.assertTrue(in.readInt() == 0x52463138, file + " is not the column format");
                        in.readInt();
                        int x0 = in.readInt(), z0 = in.readInt(), x1 = in.readInt(), z1 = in.readInt();
                        for (int cz = z0; cz <= z1; cz++) {
                            for (int cx = x0; cx <= x1; cx++) {
                                ProtoChunk chunk = new ProtoChunk(new ChunkPos(cx, cz), UpgradeData.EMPTY, heights, biomes, null);
                                chunk.fillBiomesFromNoise(generator.getBiomeSource(), state.sampler());
                                chunk.getOrCreateNoiseChunk(c -> NoiseChunk.forChunk(c, state, LegacyTerrainReaderTests.NO_BEARD,
                                        value, picker, Blender.empty()));
                                generator.fillFromNoise(Blender.empty(), state, helper.getLevel().structureManager(), chunk).join();
                                int[][] terrain = new int[256][2];
                                BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
                                for (int z = 0; z < 16; z++) {
                                    for (int x = 0; x < 16; x++) {
                                        terrain[z * 16 + x][0] = -1;
                                        terrain[z * 16 + x][1] = -1;
                                        for (int y = 255; y >= 0; y--) {
                                            int c = code(chunk.getBlockState(at.set(cx * 16 + x, y, cz * 16 + z)));
                                            if ((c == 1 || c == 9) && terrain[z * 16 + x][0] < 0) terrain[z * 16 + x][0] = y;
                                            if (c == 5 && terrain[z * 16 + x][1] < 0) terrain[z * 16 + x][1] = y;
                                        }
                                    }
                                }
                                LegacySurface.build(chunk, noise, (LegacySurface.Kind) d[4]);
                                for (int z = 0; z < 16; z++) {
                                    for (int x = 0; x < 16; x++) {
                                        int stone = in.readShort(), wet = in.readShort(), top = in.readShort(), topCode = in.readShort();
                                        double surfaceNoise = in.readDouble();
                                        long hash = in.readLong();
                                        int bx = cx * 16 + x, bz = cz * 16 + z;
                                        int gotTop = -1, gotCode = 0;
                                        long gotHash = 0xcbf29ce484222325L;
                                        for (int y = 0; y < 256; y++) {
                                            int c = code(chunk.getBlockState(at.set(bx, y, bz)));
                                            gotHash ^= c;
                                            gotHash *= 0x100000001b3L;
                                            if (c != 0 && c != 5) {
                                                gotTop = y;
                                                gotCode = c;
                                            }
                                        }
                                        columns++;
                                        if (wet > stone) water++;
                                        if (topCode == 1 && top >= 62) stoneTops++;
                                        double gotNoise = noise.surfaceNoise(bx, bz);
                                        String what = null;
                                        if (terrain[z * 16 + x][0] != stone || terrain[z * 16 + x][1] != wet) {
                                            what = "terrain: stone to " + terrain[z * 16 + x][0] + ", water to " + terrain[z * 16 + x][1]
                                                    + "; 1.7.10 " + stone + ", " + wet;
                                        } else if (gotNoise != surfaceNoise) {
                                            what = "surface noise " + gotNoise + "; 1.7.10 " + surfaceNoise;
                                        } else if (gotHash != hash) {
                                            what = "surfaced column (top " + gotTop + " code " + gotCode + "; 1.7.10 " + top + " code "
                                                    + topCode + ")";
                                        }
                                        if (what != null && differ.size() < 8) differ.add(d[0] + " (" + bx + ", " + bz + ") " + what);
                                    }
                                }
                            }
                        }
                    } catch (java.io.IOException e) {
                        throw new AssertionError("unreadable " + file + ": " + e);
                    }
                }
            }
            org.slf4j.LoggerFactory.getLogger("TerrainParityTests").info("gh6t_d: {} columns, {} under water, {} stone tops",
                    columns, water, stoneTops);
            helper.assertTrue(differ.isEmpty(), differ.size() + "+ of " + columns + " columns differ from 1.7.10's: " + differ);
            helper.assertTrue(water > 0 && stoneTops > 0, "the sample holds no water or no stone tops (" + columns + " columns)");
        });
    }
}
