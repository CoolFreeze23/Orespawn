package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.LegacyTerrainReader;
import danger.orespawn.world.OreSpawnChunkGenerator;
import danger.orespawn.world.structure.UtopiaTreeStructure;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

/**
 * The 1.7.10 terrain's reader (LegacyTerrainReader) against the generators' own noise columns, built in full with their
 * fluids (no aquifers: still water in every empty cell below Y63, as 1.7.10 filled it), in Utopia, the Village, Crystal
 * and Mining at two seeds: every block of each sampled column from the world's bottom to its top, its preliminary
 * surface against a noise chunk's, and the Utopia chunk pass's column probe in both its windows; and the altar's early
 * roll against the chunk pass over a patch with every chunk whose roll passes. The generators are built as the
 * dimension files build them (the game-test server creates no dimension of its own), their random states from the noise
 * settings as a level makes them: the router's functions in holders, which the reader has to see through.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LegacyTerrainReaderTests {

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    /** {dimension, biome, noise settings, style, build height}. */
    private static final Object[][] DIMENSIONS = {
            {"utopia", "utopia_plains", "legacy_utopia", DimensionStyle.UTOPIA, 256},
            {"village", "village_biome", "legacy_utopia", DimensionStyle.VILLAGE, 256},
            {"crystal", "crystal_plains", "legacy_crystal", DimensionStyle.CRYSTAL, 256},
            {"mining", "mining_biome", "legacy_extreme_hills", DimensionStyle.MINING, 256}};
    private static final long[] SEEDS = {1007L, 8780444890188216456L};

    private static ChunkGenerator generator(MinecraftServer server, String biome, String settings, DimensionStyle style) {
        Holder<Biome> b = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl(biome)));
        Holder<NoiseGeneratorSettings> s = server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
                .getHolderOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, rl(settings)));
        return new OreSpawnChunkGenerator(new FixedBiomeSource(b), s, style);
    }

    private static RandomState state(MinecraftServer server, String settings, long seed) {
        return RandomState.create(server.registryAccess().asGetterLookup(),
                ResourceKey.create(Registries.NOISE_SETTINGS, rl(settings)), seed);
    }

    /** The columns read: a spread over 60,000 blocks each way, and a run of neighbours on both sides of a chunk edge. */
    private static List<int[]> columns() {
        List<int[]> out = new ArrayList<>();
        for (int i = 0; i < 120; i++) {
            out.add(new int[] {-30000 + (i * 7919) % 60000, -30000 + (i * 104729) % 60000});
        }
        for (int i = -6; i < 6; i++) out.add(new int[] {i, 5 - 2 * i});
        return out;
    }

    /**
     * Every block, the preliminary surface and the column probe of each column, the reader against the noise column
     * getBaseColumn and getBaseHeight build; the reader is there for all four dimensions and starts its scans at the
     * legacy noise's bound.
     */
    @GameTest(template = "empty", timeoutTicks = 24000)
    public static void r68a_the_reader_reads_every_block_as_the_noise_column(GameTestHelper helper) {
        OffThread.run(helper, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            List<String> differ = Collections.synchronizedList(new ArrayList<>());
            AtomicLong blocks = new AtomicLong();
            AtomicLong fluids = new AtomicLong();
            AtomicLong barriers = new AtomicLong();
            AtomicLong lava = new AtomicLong();
            AtomicLong proven = new AtomicLong();
            for (Object[] d : DIMENSIONS) {
                ChunkGenerator generator = generator(server, (String) d[1], (String) d[2], (DimensionStyle) d[3]);
                LevelHeightAccessor heights = LevelHeightAccessor.create(0, (Integer) d[4]);
                for (long seed : SEEDS) {
                    RandomState state = state(server, (String) d[2], seed);
                    LegacyTerrainReader reader = LegacyTerrainReader.of(generator, heights, state);
                    helper.assertTrue(reader != null, d[0] + " at seed " + seed + " has no reader");
                    helper.assertTrue(reader.bounded(), d[0] + " at seed " + seed + "'s reader scans from the top");
                    UtopiaTreeStructure.ColumnProbe full = UtopiaTreeStructure.fullProbe(generator, heights, state);
                    columns().parallelStream().forEach(at -> {
                        int x = at[0], z = at[1];
                        NoiseColumn column = generator.getBaseColumn(x, z, heights, state);
                        for (int y = heights.getMinBuildHeight(); y < heights.getMaxBuildHeight(); y++) {
                            BlockState want = column.getBlock(y);
                            BlockState got = reader.block(x, y, z);
                            blocks.incrementAndGet();
                            if (!want.isAir() && want.getFluidState().isSource()) fluids.incrementAndGet();
                            if (want.is(net.minecraft.world.level.block.Blocks.LAVA)) lava.incrementAndGet();
                            if (want != got && differ.size() < 8) {
                                differ.add(d[0] + "/" + seed + " block (" + x + ", " + y + ", " + z + "): the reader " + got
                                        + ", the column " + want);
                            }
                            if (reader.density(x, y, z) <= 0.0) {
                                // the aquifer's barrier: stone where the density says air
                                if (!want.isAir() && !want.getFluidState().isSource()) barriers.incrementAndGet();
                                // the bound that spares the aquifer may only claim blocks the column holds as air
                                for (boolean rough : new boolean[] {true, false}) {
                                    if (reader.airProven(x, y, z, rough)) {
                                        proven.incrementAndGet();
                                        if (!want.isAir() && differ.size() < 8) {
                                            differ.add(d[0] + "/" + seed + " (" + x + ", " + y + ", " + z + ") proven air by the "
                                                    + (rough ? "rough" : "precise") + " pass, the column " + want);
                                        }
                                    }
                                }
                            }
                        }
                        int a = reader.preliminarySurfaceLevel(x, z);
                        int b = noiseChunk(generator, heights, state, x, z).preliminarySurfaceLevel(x, z);
                        if (a != b && differ.size() < 8) {
                            differ.add(d[0] + "/" + seed + " preliminary surface (" + x + ", " + z + "): the reader " + a
                                    + ", the noise chunk " + b);
                        }
                        for (int[] window : new int[][] {{50, 127}, {50, 100}, {40, 140}}) {
                            int p = reader.grassBase(x, z, window[0], window[1]);
                            int q = full.base(x, z, window[0], window[1]);
                            if (p != q && differ.size() < 8) {
                                differ.add(d[0] + "/" + seed + " probe (" + x + ", " + z + ") in (" + window[0] + ", "
                                        + window[1] + "]: the reader " + p + ", the columns " + q);
                            }
                        }
                    });
                }
            }
            org.slf4j.LoggerFactory.getLogger("LegacyTerrainReaderTests").info("r68a: {} blocks, {} fluid, {} lava, {} barrier, "
                    + "{} proven air by a bound", blocks.get(), fluids.get(), lava.get(), barriers.get(), proven.get());
            helper.assertTrue(differ.isEmpty(), differ.size() + "+ of " + blocks.get() + " blocks, preliminary surfaces and "
                    + "probes differ: " + differ);
            helper.assertTrue(fluids.get() > 0, "no column of the sample held water or lava (" + blocks.get() + " blocks)");
        });
    }

    /**
     * The altar's early roll (UtopiaTreeStructure.altarRollReached) against the chunk pass: over a 200 x 200 patch the
     * pass's altar is in no chunk whose early roll fails (a sample of 2,000 of them asked in full), and the chunks whose
     * roll passes are the only ones the altar's structure goes on to ask.
     */
    @GameTest(template = "empty", timeoutTicks = 24000)
    public static void r68b_the_altar_roll_is_read_before_the_pass(GameTestHelper helper) {
        OffThread.run(helper, () -> {
            MinecraftServer server = helper.getLevel().getServer();
            ChunkGenerator generator = generator(server, "utopia_plains", "legacy_utopia", DimensionStyle.UTOPIA);
            LevelHeightAccessor heights = LevelHeightAccessor.create(0, 256);
            long seed = 8780444890188216456L;
            RandomState state = state(server, "legacy_utopia", seed);
            UtopiaTreeStructure.ColumnProbe probe = UtopiaTreeStructure.probe(generator, heights, state);
            List<ChunkPos> passing = Collections.synchronizedList(new ArrayList<>());
            List<ChunkPos> failing = Collections.synchronizedList(new ArrayList<>());
            IntStream.range(0, 200).parallel().forEach(i -> {
                for (int j = 0; j < 200; j++) {
                    ChunkPos chunk = new ChunkPos(-100 + i, 400 + j);
                    if (UtopiaTreeStructure.altarRollReached(seed, chunk, probe)) {
                        passing.add(chunk);
                    } else if ((i * 200 + j) % 20 == 7) {
                        failing.add(chunk);
                    }
                }
            });
            helper.assertTrue(passing.size() >= 8 && passing.size() <= 40, passing.size()
                    + " chunks of 40,000 pass the altar roll, not about one in 2,000");
            List<String> wrong = Collections.synchronizedList(new ArrayList<>());
            failing.parallelStream().forEach(chunk -> {
                UtopiaTreeStructure.Altar altar = UtopiaTreeStructure.chunkPass(seed, chunk, probe).altar();
                if (altar != null && wrong.size() < 5) wrong.add(chunk + " has " + altar + " though its early roll fails");
            });
            helper.assertTrue(wrong.isEmpty(), "of " + failing.size() + " chunks whose early roll fails: " + wrong);
            long altars = passing.stream().filter(c -> UtopiaTreeStructure.chunkPass(seed, c, probe).altar() != null).count();
            helper.assertTrue(altars > 0, "none of the " + passing.size() + " passing chunks has its altar");
        });
    }

    /** A beardifier that adds nothing (the preliminary surface never reads it). */
    static final DensityFunctions.BeardifierOrMarker NO_BEARD = new DensityFunctions.BeardifierOrMarker() {
        @Override
        public double compute(DensityFunction.FunctionContext context) {
            return 0.0;
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 0.0;
        }
    };

    /** The one-cell noise chunk getBaseColumn builds at a column, for its preliminary surface. */
    private static NoiseChunk noiseChunk(ChunkGenerator generator, LevelHeightAccessor heights, RandomState state, int x, int z) {
        NoiseGeneratorSettings settings = ((NoiseBasedChunkGenerator) generator).generatorSettings().value();
        NoiseSettings noise = settings.noiseSettings().clampToHeightAccessor(heights);
        Aquifer.FluidStatus sea = new Aquifer.FluidStatus(settings.seaLevel(), settings.defaultFluid());
        return new NoiseChunk(1, state, Math.floorDiv(x, 4) * 4, Math.floorDiv(z, 4) * 4, noise, NO_BEARD, settings,
                (px, py, pz) -> sea, Blender.empty());
    }
}
