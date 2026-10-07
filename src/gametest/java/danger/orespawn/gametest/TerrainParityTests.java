package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.LegacyTerrainNoise;
import danger.orespawn.world.OreSpawnChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.Reader;
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
    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void gh6t_c_no_mountains_far_out(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        LevelHeightAccessor heights = LevelHeightAccessor.create(-64, 384);
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
        helper.succeed();
    }
}
