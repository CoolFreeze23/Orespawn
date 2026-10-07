package danger.orespawn.world;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The terrain of a noise generator whose final density is one interpolated function (the 1.7.10 terrain of Utopia, the
 * Village, Crystal and Mining), read as {@code NoiseBasedChunkGenerator.getBaseColumn} and {@code getBaseHeight} read it,
 * block for block, without building a noise column for every question.
 *
 * <p>The density is the cell corners' (the interpolated function at the 4 x 8 x 4 corners), interpolated with
 * {@code Mth.lerp3} as the noise chunk fills a cell for the blocks it places. Where it is above zero the block is the
 * default block. Where it says air the block is the aquifer's answer, worked out line for line as
 * {@code Aquifer.NoiseBasedAquifer} works it out for the one-cell noise chunk {@code getBaseColumn} builds at that
 * column: the aquifer cells' places from the aquifer random; their fluids from the preliminary surface (the first level,
 * eight blocks apart from the top, where the initial density passes 0.390625, at quart columns), the floodedness, spread
 * and lava noises and the deep-dark test; the barrier between two cells. Every one of those is a function of position,
 * except that the deep-dark test reads erosion and depth through the asking noise chunk's flat caches (quantized to the
 * quart inside its two quarts, exact outside), so a cell's fluid is kept twice: as seen from inside those quarts and from
 * outside. The reader keeps all of it for the world in bounded caches, where vanilla rebuilds it for every column.</p>
 *
 * <p>The aquifer is not asked where it can only answer air: a block of density at most zero is air when it lies at
 * least five above the sea level and above every candidate aquifer cell's highest possible fluid level by more than the
 * barrier's four-block reach. A cell's fluid level is the sea level, none, or at most the lower of the preliminary
 * surface at the cell's own column (one of the thirteen it takes the least of) and its spread level, so that bound needs
 * one preliminary surface per cell; for the legacy terrain the preliminary surface itself has a bound from the column's
 * depth noise alone ({@link LegacyTerrainNoise#cellYAtMost}), which also starts every scan from the top.</p>
 *
 * <p>Ore veins never turn air into anything else and only change which solid block stands in the ground, so the reader
 * answers air and solid ground exactly with or without them.</p>
 */
public final class LegacyTerrainReader {
    /** NoiseChunk.computePreliminarySurfaceLevel's threshold on the initial density. */
    private static final double PRELIMINARY_THRESHOLD = 0.390625;
    /** Aquifer.NoiseBasedAquifer's sampling offsets for the preliminary surface, in chunks, in its order. */
    private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};
    /** Columns hold two arrays of the levels each (about half a kilobyte); the other caches hold a number each. */
    private static final int COLUMN_LIMIT = 1 << 15;
    private static final int CACHE_LIMIT = 1 << 16;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
    private static final ResourceLocation INTERPOLATED = ResourceLocation.withDefaultNamespace("interpolated");
    private static final Map<RandomState, Map<Heights, Optional<LegacyTerrainReader>>> READERS =
            Collections.synchronizedMap(new WeakHashMap<>());

    /** The noise range a column is read over (the generator's, clamped to the heights asked) and the build bottom. */
    private record Heights(int minY, int height, int buildMinY) {
    }

    /** Aquifer.FluidStatus. */
    private record Fluid(int level, BlockState type) {
        BlockState at(int y) {
            return y < this.level ? this.type : AIR;
        }
    }

    private final BlockState defaultBlock;
    private final int seaLevel;
    private final boolean aquifers;
    private final int buildMinY;
    private final int minY;
    private final int maxY;
    private final int cellWidth;
    private final int cellHeight;
    private final int cellNoiseMinY;
    private final int cellCountY;
    /** getBaseColumn's noise chunk is one cell wide: its flat caches hold noiseSizeXZ + 1 quarts on each axis. */
    private final int flatCacheQuarts;
    private final DensityFunction terrain;
    private final DensityFunction initial;
    /** The terrain function itself when it is the legacy noise and the initial density is exactly eight times it. */
    @Nullable
    private final LegacyTerrainNoise legacy;
    private final DensityFunction barrier;
    private final DensityFunction floodedness;
    private final DensityFunction spread;
    private final DensityFunction lava;
    private final DensityFunction erosion;
    private final DensityFunction depth;
    private final PositionalRandomFactory aquiferRandom;
    private final Fluid lavaFluid;
    private final Fluid seaFluid;

    private final ConcurrentHashMap<Long, Column> columns = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> locations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Fluid> fluids = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Fluid> fluidsInFlatCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> levelBounds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Integer> roughLevelBounds = new ConcurrentHashMap<>();

    private LegacyTerrainReader(NoiseGeneratorSettings settings, NoiseSettings noise, int buildMinY, RandomState randomState,
                                DensityFunction terrain) {
        NoiseRouter router = randomState.router();
        this.defaultBlock = settings.defaultBlock();
        this.seaLevel = settings.seaLevel();
        this.aquifers = settings.isAquifersEnabled();
        this.buildMinY = buildMinY;
        this.minY = noise.minY();
        this.maxY = noise.minY() + noise.height();
        this.cellWidth = noise.getCellWidth();
        this.cellHeight = noise.getCellHeight();
        this.cellNoiseMinY = Mth.floorDiv(noise.minY(), this.cellHeight);
        this.cellCountY = Mth.floorDiv(noise.height(), this.cellHeight);
        this.flatCacheQuarts = QuartPos.fromBlock(this.cellWidth) + 1;
        this.terrain = terrain;
        this.initial = router.initialDensityWithoutJaggedness();
        this.legacy = unwrap(terrain) instanceof LegacyTerrainNoise noiseFunction && eightTimes(this.initial, terrain)
                ? noiseFunction : null;
        this.barrier = router.barrierNoise();
        this.floodedness = router.fluidLevelFloodednessNoise();
        this.spread = router.fluidLevelSpreadNoise();
        this.lava = router.lavaNoise();
        this.erosion = router.erosion();
        this.depth = router.depth();
        this.aquiferRandom = randomState.aquiferRandom();
        // NoiseBasedChunkGenerator.createFluidPicker
        this.lavaFluid = new Fluid(-54, LAVA);
        this.seaFluid = new Fluid(this.seaLevel, settings.defaultFluid());
    }

    /**
     * The reader for a generator and the heights it is asked at, or null when the generator is not a noise generator
     * whose final density is one interpolated function.
     */
    @Nullable
    public static LegacyTerrainReader of(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState) {
        if (!(generator instanceof NoiseBasedChunkGenerator noiseGenerator)) return null;
        NoiseGeneratorSettings settings = noiseGenerator.generatorSettings().value();
        NoiseSettings noise = settings.noiseSettings().clampToHeightAccessor(heights);
        if (noise.height() <= 0 || Math.floorMod(noise.minY(), noise.getCellHeight()) != 0
                || Math.floorMod(noise.height(), noise.getCellHeight()) != 0) {
            return null;
        }
        Map<Heights, Optional<LegacyTerrainReader>> byHeights;
        synchronized (READERS) {
            byHeights = READERS.computeIfAbsent(randomState, state -> new ConcurrentHashMap<>());
        }
        Heights key = new Heights(noise.minY(), noise.height(), heights.getMinBuildHeight());
        return byHeights.computeIfAbsent(key, k -> {
            DensityFunction terrain = interpolatedArgument(randomState.router().finalDensity());
            return terrain == null ? Optional.empty()
                    : Optional.of(new LegacyTerrainReader(settings, noise, k.buildMinY(), randomState, terrain));
        }).orElse(null);
    }

    /**
     * The argument of the final density when it is one interpolated function; otherwise null. The router's functions come
     * wrapped in holders (the noise settings' codec keeps every router field as a holder, inline ones as direct holders),
     * and the noise chunk unwraps them before it wraps the markers, so they are unwrapped here too.
     */
    @Nullable
    private static DensityFunction interpolatedArgument(DensityFunction density) {
        density = unwrap(density);
        if (density instanceof DensityFunctions.MarkerOrMarked marker
                && INTERPOLATED.equals(BuiltInRegistries.DENSITY_FUNCTION_TYPE.getKey(marker.codec().codec()))) {
            return marker.wrapped();
        }
        return null;
    }

    /** The function inside any holders around it. */
    private static DensityFunction unwrap(DensityFunction function) {
        while (function instanceof DensityFunctions.HolderHolder holder) function = holder.function().value();
        return function;
    }

    /** Whether {@code initial} is {@code mul(8, terrain)}: the same encoding and the same values at a few points. */
    private static boolean eightTimes(DensityFunction initial, DensityFunction terrain) {
        try {
            DensityFunction expected = DensityFunctions.mul(DensityFunctions.constant(8.0), terrain);
            JsonElement a = DensityFunction.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, unwrap(initial)).getOrThrow();
            JsonElement b = DensityFunction.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, expected).getOrThrow();
            if (!a.equals(b)) return false;
            for (int i = 0; i < 16; i++) {
                DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(i * 977 - 7000, i * 23 - 60,
                        i * 1531 - 9000);
                if (initial.compute(point) != terrain.compute(point) * 8.0) return false;
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------- the answers

    /** Whether the scans start at the legacy noise's own bound (the terrain is the legacy noise, the initial density 8 x it). */
    public boolean bounded() {
        return this.legacy != null;
    }

    /** getBaseColumn(x, z).getBlock(y), as the noise leaves it (the default block for solid ground). */
    public BlockState block(int x, int y, int z) {
        if (y < this.minY || y >= this.maxY) return AIR;
        BlockState state = this.substance(x, y, z, this.density(x, y, z), this.quart(x), this.quart(z));
        return state == null ? this.defaultBlock : state;
    }

    /**
     * The column probe of the Utopia chunk pass: the grass block's Y when the column's top block (getBaseHeight's
     * WORLD_SURFACE_WG less one) is solid ground (the same as its OCEAN_FLOOR_WG) and its surface is inside
     * ({@code windowLow}, {@code windowHigh}]; otherwise {@link Integer#MIN_VALUE}.
     */
    public int grassBase(int x, int z, int windowLow, int windowHigh) {
        int solid = this.topSolid(x, z);
        // the top block is at least the solid top: a solid top over the window ends it whatever lies above
        if (solid != Integer.MIN_VALUE && solid + 1 > windowHigh) return Integer.MIN_VALUE;
        int qx = this.quart(x), qz = this.quart(z);
        int gx = Math.floorDiv(x - 5, 16), gz = Math.floorDiv(z - 5, 16);
        for (int y = this.maxY - 1; y > solid && y >= this.minY; y--) {
            if (this.provenAir(gx, gz, y)) continue;
            BlockState state = this.substance(x, y, z, this.density(x, y, z), qx, qz);
            if (state == null) {
                // the aquifer's barrier: stone, the top block, and solid
                return y + 1 > windowLow && y + 1 <= windowHigh ? y : Integer.MIN_VALUE;
            }
            // water or lava on top: the surface is not the floor
            if (!state.isAir()) return Integer.MIN_VALUE;
        }
        // no block at all: getBaseHeight answers the build height's bottom for both heightmaps
        int surface = solid == Integer.MIN_VALUE ? this.buildMinY : solid + 1;
        return surface > windowLow && surface <= windowHigh ? surface - 1 : Integer.MIN_VALUE;
    }

    /** Whether every block of the plane over the rectangle is air, as getBaseColumn builds each column. */
    public boolean planeClear(int minX, int maxX, int minZ, int maxZ, int plane) {
        // outside the noise range getBaseColumn's column answers air
        if (plane < this.minY || plane >= this.maxY) return true;
        int width = maxX - minX + 1;
        double[] densities = new double[width * (maxZ - minZ + 1)];
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                double d = this.density(x, plane, z);
                if (d > 0.0) return false;
                densities[(z - minZ) * width + (x - minX)] = d;
            }
        }
        for (int x = minX; x <= maxX; x++) {
            int gx = Math.floorDiv(x - 5, 16), qx = this.quart(x);
            for (int z = minZ; z <= maxZ; z++) {
                if (this.provenAir(gx, Math.floorDiv(z - 5, 16), plane)) continue;
                BlockState state = this.substance(x, plane, z, densities[(z - minZ) * width + (x - minX)], qx, this.quart(z));
                if (state == null || !state.isAir()) return false;
            }
        }
        return true;
    }

    /** The preliminary surface at a column (NoiseChunk.preliminarySurfaceLevel). */
    public int preliminarySurfaceLevel(int x, int z) {
        return this.column(QuartPos.toBlock(QuartPos.fromBlock(x)), QuartPos.toBlock(QuartPos.fromBlock(z))).preliminary();
    }

    /** The density at a block: the cell's corners interpolated as the noise chunk fills the cell (with the beardifier's 0). */
    public double density(int x, int y, int z) {
        int x0 = Math.floorDiv(x, this.cellWidth) * this.cellWidth;
        int z0 = Math.floorDiv(z, this.cellWidth) * this.cellWidth;
        int index = Math.floorDiv(y, this.cellHeight) - this.cellNoiseMinY;
        double fx = (double) Math.floorMod(x, this.cellWidth) / (double) this.cellWidth;
        double fy = (double) Math.floorMod(y, this.cellHeight) / (double) this.cellHeight;
        double fz = (double) Math.floorMod(z, this.cellWidth) / (double) this.cellWidth;
        Column c00 = this.column(x0, z0), c10 = this.column(x0 + this.cellWidth, z0);
        Column c01 = this.column(x0, z0 + this.cellWidth), c11 = this.column(x0 + this.cellWidth, z0 + this.cellWidth);
        return Mth.lerp3(fx, fy, fz, c00.value(index), c10.value(index), c00.value(index + 1), c10.value(index + 1),
                c01.value(index), c11.value(index), c01.value(index + 1), c11.value(index + 1)) + 0.0;
    }

    // --------------------------------------------------------------------------------------------- the solid top

    /** The highest block of the column whose density is above zero, or {@link Integer#MIN_VALUE}. */
    private int topSolid(int x, int z) {
        int x0 = Math.floorDiv(x, this.cellWidth) * this.cellWidth;
        int z0 = Math.floorDiv(z, this.cellWidth) * this.cellWidth;
        Column c00 = this.column(x0, z0), c10 = this.column(x0 + this.cellWidth, z0);
        Column c01 = this.column(x0, z0 + this.cellWidth), c11 = this.column(x0 + this.cellWidth, z0 + this.cellWidth);
        // the highest corner level any of the four corners can be solid at: no cell above it has a corner over zero, and
        // corners at most zero interpolate to at most zero
        int top = Math.max(Math.max(c00.solidLevelAtMost(), c10.solidLevelAtMost()),
                Math.max(c01.solidLevelAtMost(), c11.solidLevelAtMost()));
        for (int index = Math.min(top, this.cellCountY - 1); index >= 0; index--) {
            if (c00.value(index) <= 0.0 && c10.value(index) <= 0.0 && c01.value(index) <= 0.0 && c11.value(index) <= 0.0
                    && c00.value(index + 1) <= 0.0 && c10.value(index + 1) <= 0.0 && c01.value(index + 1) <= 0.0
                    && c11.value(index + 1) <= 0.0) {
                continue;
            }
            int y0 = (this.cellNoiseMinY + index) * this.cellHeight;
            for (int y = y0 + this.cellHeight - 1; y >= y0; y--) {
                if (this.density(x, y, z) > 0.0) return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    // ---------------------------------------------------------------------------------------- where air is sure

    /**
     * Whether the aquifer can only answer air at height {@code y} in a column whose candidate cells lie in the grid
     * columns {@code gx, gx + 1} by {@code gz, gz + 1}: water needs y under the nearest cell's level, a barrier y at most
     * four over the higher of two cells' levels, and lava the sea level's lava band, so five above every candidate's level
     * bound (and above the sea level's) is air.
     */
    private boolean provenAir(int gx, int gz, int y) {
        return this.provenAir(gx, gz, y, true) || this.provenAir(gx, gz, y, false);
    }

    /** {@link #provenAir} by one of its two passes: the rough bounds alone, or the cells' own preliminary surfaces. */
    private boolean provenAir(int gx, int gz, int y, boolean rough) {
        if (!this.aquifers || y < this.globalLevel() + 5) return false;
        int gy = Math.floorDiv(y + 1, 12);
        for (int i = 0; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = 0; k <= 1; k++) {
                    int bound = rough ? this.roughLevelBound(gx + i, gy + j, gz + k) : this.levelBound(gx + i, gy + j, gz + k);
                    if (y < bound + 5) return false;
                }
            }
        }
        return true;
    }

    /**
     * Whether the reader takes the block at (x, y, z) for air without asking the aquifer (by the rough bounds alone, or
     * by the cells' own preliminary surfaces): for the tests, which check that such a block is air in the noise column.
     */
    public boolean airProven(int x, int y, int z, boolean rough) {
        return this.density(x, y, z) <= 0.0 && this.provenAir(Math.floorDiv(x - 5, 16), Math.floorDiv(z - 5, 16), y, rough);
    }

    /**
     * The highest fluid level a cell can have: the sea level (the fluid picker's water, the global status and the
     * floodedness's sea branch), none, or the spread level capped by the least preliminary surface of its thirteen
     * samples, which is at most the one at its own column.
     */
    private int levelBound(int gx, int gy, int gz) {
        long key = BlockPos.asLong(gx, gy, gz);
        Integer known = this.levelBounds.get(key);
        if (known != null) return known;
        long location = this.location(gx, gy, gz);
        int x = BlockPos.getX(location), y = BlockPos.getY(location), z = BlockPos.getZ(location);
        int bound = Math.max(this.globalLevel(), Math.min(this.preliminarySurfaceLevel(x, z), this.spreadLevel(x, y, z)));
        bounded(this.levelBounds).put(key, bound);
        return bound;
    }

    /** {@link #levelBound} with the preliminary surface's own bound in place of the preliminary surface. */
    private int roughLevelBound(int gx, int gy, int gz) {
        long key = BlockPos.asLong(gx, gy, gz);
        Integer known = this.roughLevelBounds.get(key);
        if (known != null) return known;
        long location = this.location(gx, gy, gz);
        int x = BlockPos.getX(location), y = BlockPos.getY(location), z = BlockPos.getZ(location);
        Column column = this.column(QuartPos.toBlock(QuartPos.fromBlock(x)), QuartPos.toBlock(QuartPos.fromBlock(z)));
        int bound = Math.max(this.globalLevel(), Math.min(column.preliminaryAtMost(), this.spreadLevel(x, y, z)));
        bounded(this.roughLevelBounds).put(key, bound);
        return bound;
    }

    // ------------------------------------------------------------------------- Aquifer.NoiseBasedAquifer, line for line

    /** computeSubstance for getBaseColumn's noise chunk at the column whose cell starts at quarts (qx, qz). */
    @Nullable
    private BlockState substance(int x, int y, int z, double density, int qx, int qz) {
        if (!this.aquifers) {
            // Aquifer.createDisabled
            return density > 0.0 ? null : this.globalFluid(y).at(y);
        }
        if (density > 0.0) return null;
        Fluid global = this.globalFluid(y);
        if (global.at(y).is(Blocks.LAVA)) return LAVA;
        int l = Math.floorDiv(x - 5, 16);
        int i1 = Math.floorDiv(y + 1, 12);
        int j1 = Math.floorDiv(z - 5, 16);
        int k1 = Integer.MAX_VALUE;
        int l1 = Integer.MAX_VALUE;
        int i2 = Integer.MAX_VALUE;
        long j2 = 0L;
        long k2 = 0L;
        long l2 = 0L;
        for (int i3 = 0; i3 <= 1; i3++) {
            for (int j3 = -1; j3 <= 1; j3++) {
                for (int k3 = 0; k3 <= 1; k3++) {
                    long l4 = this.location(l + i3, i1 + j3, j1 + k3);
                    int i6 = BlockPos.getX(l4) - x;
                    int j5 = BlockPos.getY(l4) - y;
                    int k5 = BlockPos.getZ(l4) - z;
                    int l5 = i6 * i6 + j5 * j5 + k5 * k5;
                    if (k1 >= l5) {
                        l2 = k2;
                        k2 = j2;
                        j2 = l4;
                        i2 = l1;
                        l1 = k1;
                        k1 = l5;
                    } else if (l1 >= l5) {
                        l2 = k2;
                        k2 = l4;
                        i2 = l1;
                        l1 = l5;
                    } else if (i2 >= l5) {
                        l2 = l4;
                        i2 = l5;
                    }
                }
            }
        }
        Fluid first = this.fluid(j2, qx, qz);
        double d1 = similarity(k1, l1);
        BlockState state = first.at(y);
        if (d1 <= 0.0) return state;
        if (state.is(Blocks.WATER) && this.globalFluid(y - 1).at(y - 1).is(Blocks.LAVA)) return state;
        double[] barrierNoise = {Double.NaN};
        Fluid second = this.fluid(k2, qx, qz);
        double d2 = d1 * this.pressure(x, y, z, barrierNoise, first, second);
        if (density + d2 > 0.0) return null;
        Fluid third = this.fluid(l2, qx, qz);
        double d0 = similarity(k1, i2);
        if (d0 > 0.0) {
            double d3 = d1 * d0 * this.pressure(x, y, z, barrierNoise, first, third);
            if (density + d3 > 0.0) return null;
        }
        double d4 = similarity(l1, i2);
        if (d4 > 0.0) {
            double d5 = d1 * d4 * this.pressure(x, y, z, barrierNoise, second, third);
            if (density + d5 > 0.0) return null;
        }
        return state;
    }

    private static double similarity(int a, int b) {
        return 1.0 - (double) Math.abs(b - a) / 25.0;
    }

    /** calculatePressure. */
    private double pressure(int x, int y, int z, double[] barrierNoise, Fluid a, Fluid b) {
        BlockState sa = a.at(y);
        BlockState sb = b.at(y);
        if ((!sa.is(Blocks.LAVA) || !sb.is(Blocks.WATER)) && (!sa.is(Blocks.WATER) || !sb.is(Blocks.LAVA))) {
            int j = Math.abs(a.level() - b.level());
            if (j == 0) return 0.0;
            double d0 = 0.5 * (double) (a.level() + b.level());
            double d1 = (double) y + 0.5 - d0;
            double d2 = (double) j / 2.0;
            double d9 = d2 - Math.abs(d1);
            double d10;
            if (d1 > 0.0) {
                double d11 = 0.0 + d9;
                d10 = d11 > 0.0 ? d11 / 1.5 : d11 / 2.5;
            } else {
                double d15 = 3.0 + d9;
                d10 = d15 > 0.0 ? d15 / 3.0 : d15 / 10.0;
            }
            double d12;
            if (!(d10 < -2.0) && !(d10 > 2.0)) {
                if (Double.isNaN(barrierNoise[0])) {
                    barrierNoise[0] = this.barrier.compute(new DensityFunction.SinglePointContext(x, y, z));
                }
                d12 = barrierNoise[0];
            } else {
                d12 = 0.0;
            }
            return 2.0 * (d12 + d10);
        }
        return 2.0;
    }

    /** The higher of the fluid picker's two levels: a status the picker hands a cell is at most this high. */
    private int globalLevel() {
        return Math.max(this.seaLevel, this.lavaFluid.level());
    }

    /** The fluid picker (NoiseBasedChunkGenerator.createFluidPicker). */
    private Fluid globalFluid(int y) {
        return y < Math.min(-54, this.seaLevel) ? this.lavaFluid : this.seaFluid;
    }

    /** The aquifer cell's place (aquiferLocationCache), from the aquifer random at its grid cell. */
    private long location(int gx, int gy, int gz) {
        long key = BlockPos.asLong(gx, gy, gz);
        Long known = this.locations.get(key);
        if (known != null) return known;
        var random = this.aquiferRandom.at(gx, gy, gz);
        int x = gx * 16 + random.nextInt(10);
        int y = gy * 12 + random.nextInt(9);
        int z = gz * 16 + random.nextInt(10);
        long location = BlockPos.asLong(x, y, z);
        bounded(this.locations).put(key, location);
        return location;
    }

    /** getAquiferStatus for the noise chunk whose flat caches start at quarts (qx, qz). */
    private Fluid fluid(long location, int qx, int qz) {
        int x = BlockPos.getX(location), y = BlockPos.getY(location), z = BlockPos.getZ(location);
        int dx = QuartPos.fromBlock(x) - qx, dz = QuartPos.fromBlock(z) - qz;
        boolean inFlatCache = dx >= 0 && dz >= 0 && dx < this.flatCacheQuarts && dz < this.flatCacheQuarts;
        ConcurrentHashMap<Long, Fluid> cache = inFlatCache ? this.fluidsInFlatCache : this.fluids;
        long key = BlockPos.asLong(Math.floorDiv(x, 16), Math.floorDiv(y, 12), Math.floorDiv(z, 16));
        Fluid known = cache.get(key);
        if (known != null) return known;
        Fluid fluid = this.computeFluid(x, y, z, inFlatCache);
        bounded(cache).put(key, fluid);
        return fluid;
    }

    /** computeFluid. */
    private Fluid computeFluid(int x, int y, int z, boolean inFlatCache) {
        Fluid global = this.globalFluid(y);
        int i = Integer.MAX_VALUE;
        int j = y + 12;
        int k = y - 12;
        boolean flag = false;
        for (int[] offset : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
            int l = x + SectionPos.sectionToBlockCoord(offset[0]);
            int i1 = z + SectionPos.sectionToBlockCoord(offset[1]);
            int j1 = this.preliminarySurfaceLevel(l, i1);
            int k1 = j1 + 8;
            boolean centre = offset[0] == 0 && offset[1] == 0;
            if (centre && k > k1) return global;
            boolean above = j > k1;
            if (above || centre) {
                Fluid sample = this.globalFluid(k1);
                if (!sample.at(k1).isAir()) {
                    if (centre) flag = true;
                    if (above) return sample;
                }
            }
            i = Math.min(i, j1);
        }
        int level = this.surfaceLevel(x, y, z, global, i, flag, inFlatCache);
        return new Fluid(level, this.fluidType(x, y, z, global, level));
    }

    /**
     * computeSurfaceLevel. The deep-dark test reads erosion and depth through the noise chunk's wrapped router: their
     * flat caches answer inside the chunk's quarts with the value at the quart corner and y 0, so there the test reads
     * them at the quart corner (both are flat in y: their noises have no y scale; depth's y gradient is not cached).
     */
    private int surfaceLevel(int x, int y, int z, Fluid global, int minSurface, boolean flag, boolean inFlatCache) {
        DensityFunction.SinglePointContext context = new DensityFunction.SinglePointContext(x, y, z);
        DensityFunction.SinglePointContext region = inFlatCache
                ? new DensityFunction.SinglePointContext(QuartPos.toBlock(QuartPos.fromBlock(x)), y,
                QuartPos.toBlock(QuartPos.fromBlock(z)))
                : context;
        double d0;
        double d1;
        if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, region)) {
            d0 = -1.0;
            d1 = -1.0;
        } else {
            int i = minSurface + 8 - y;
            double d2 = flag ? Mth.clampedMap((double) i, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double d3 = Mth.clamp(this.floodedness.compute(context), -1.0, 1.0);
            double d4 = Mth.map(d2, 1.0, 0.0, -0.3, 0.8);
            double d5 = Mth.map(d2, 1.0, 0.0, -0.8, 0.4);
            d0 = d3 - d5;
            d1 = d3 - d4;
        }
        if (d1 > 0.0) return global.level();
        if (d0 > 0.0) return Math.min(minSurface, this.spreadLevel(x, y, z));
        return DimensionType.WAY_BELOW_MIN_Y;
    }

    /** computeRandomizedFluidSurfaceLevel's level before its cap by the least preliminary surface. */
    private int spreadLevel(int x, int y, int z) {
        int k = Math.floorDiv(x, 16);
        int l = Math.floorDiv(y, 40);
        int i1 = Math.floorDiv(z, 16);
        int j1 = l * 40 + 20;
        double d0 = this.spread.compute(new DensityFunction.SinglePointContext(k, l, i1)) * 10.0;
        int l1 = Mth.quantize(d0, 3);
        return j1 + l1;
    }

    /** computeFluidType. */
    private BlockState fluidType(int x, int y, int z, Fluid global, int level) {
        BlockState state = global.type();
        if (level <= -10 && level != DimensionType.WAY_BELOW_MIN_Y && global.type() != LAVA) {
            int k = Math.floorDiv(x, 64);
            int l = Math.floorDiv(y, 40);
            int i1 = Math.floorDiv(z, 64);
            double d0 = this.lava.compute(new DensityFunction.SinglePointContext(k, l, i1));
            if (Math.abs(d0) > 0.3) state = LAVA;
        }
        return state;
    }

    // ------------------------------------------------------------------------------------------------- the columns

    private int quart(int blockCoordinate) {
        return QuartPos.fromBlock(Math.floorDiv(blockCoordinate, this.cellWidth) * this.cellWidth);
    }

    private Column column(int x, int z) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        Column known = this.columns.get(key);
        if (known != null) return known;
        if (this.columns.size() > COLUMN_LIMIT) this.columns.clear();
        return this.columns.computeIfAbsent(key, k -> new Column(x, z));
    }

    private static <V> ConcurrentHashMap<Long, V> bounded(ConcurrentHashMap<Long, V> cache) {
        if (cache.size() > CACHE_LIMIT) cache.clear();
        return cache;
    }

    /** The interpolated function at one corner column's levels (8 blocks apart from the bottom), and its preliminary surface. */
    private final class Column {
        private final int x;
        private final int z;
        private final double[] values = new double[LegacyTerrainReader.this.cellCountY + 1];
        private final boolean[] known = new boolean[LegacyTerrainReader.this.cellCountY + 1];
        private int preliminary;
        private boolean preliminaryKnown;

        private Column(int x, int z) {
            this.x = x;
            this.z = z;
        }

        synchronized double value(int index) {
            if (!this.known[index]) {
                this.values[index] = LegacyTerrainReader.this.terrain.compute(new DensityFunction.SinglePointContext(this.x,
                        LegacyTerrainReader.this.levelY(index), this.z));
                this.known[index] = true;
            }
            return this.values[index];
        }

        /** NoiseChunk.computePreliminarySurfaceLevel: the levels from the top down, the first initial density over 0.390625. */
        synchronized int preliminary() {
            if (this.preliminaryKnown) return this.preliminary;
            int result = Integer.MAX_VALUE;
            for (int index = this.preliminaryStart(); index >= 0; index--) {
                if (this.initial(index) > PRELIMINARY_THRESHOLD) {
                    result = LegacyTerrainReader.this.levelY(index);
                    break;
                }
            }
            this.preliminary = result;
            this.preliminaryKnown = true;
            return result;
        }

        /** The highest level the preliminary surface can stand at (the legacy noise's bound), or the top. */
        int preliminaryAtMost() {
            int start = this.preliminaryStart();
            return start < 0 ? Integer.MIN_VALUE / 2 : LegacyTerrainReader.this.levelY(start);
        }

        /** The highest level index whose value can be above zero. */
        int solidLevelAtMost() {
            return this.levelBelow(0.0);
        }

        private int preliminaryStart() {
            return this.levelBelow(PRELIMINARY_THRESHOLD / 8.0);
        }

        /** The highest level index below the legacy noise's bound for {@code threshold} (every level above it is at most that). */
        private int levelBelow(double threshold) {
            LegacyTerrainNoise noise = LegacyTerrainReader.this.legacy;
            int index = LegacyTerrainReader.this.cellCountY;
            if (noise == null) return index;
            double cellY = noise.cellYAtMost(this.x, this.z, threshold);
            while (index >= 0 && LegacyTerrainReader.this.levelY(index) / 8.0 >= cellY) index--;
            return index;
        }

        private double initial(int index) {
            if (LegacyTerrainReader.this.legacy != null) return this.value(index) * 8.0;
            return LegacyTerrainReader.this.initial.compute(new DensityFunction.SinglePointContext(this.x,
                    LegacyTerrainReader.this.levelY(index), this.z));
        }
    }

    private int levelY(int index) {
        return (this.cellNoiseMinY + index) * this.cellHeight;
    }
}
