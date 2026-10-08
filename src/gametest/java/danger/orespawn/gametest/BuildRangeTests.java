package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.OreSpawnChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * WGEN-096: Utopia, the Village, Crystal and Mining keep the build range of earlier versions, Y-64 to 320,
 * so land saved in it keeps every block, and build the original's world inside it: the 1.7.10 terrain and surface from
 * Y0 to 256 as before, solid bedrock below, nothing above.
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
        // the dimensions this leaves alone generate in their whole build range, whatever their noise covers
        for (Object[] d : new Object[][] {{"chaos_biome", "chaos", DimensionStyle.CHAOS},
                {"island_biome", "islands", DimensionStyle.ISLANDS}}) {
            OreSpawnChunkGenerator generator = generator(server, (String) d[0], (String) d[1], (DimensionStyle) d[2]);
            helper.assertTrue(!generator.originalWorld() && GenerationRange.bottom(generator, range) == -64
                    && GenerationRange.top(generator, range) == 320, d[1] + "'s generation range was narrowed");
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
}
