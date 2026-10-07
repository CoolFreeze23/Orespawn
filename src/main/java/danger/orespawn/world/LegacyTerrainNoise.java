package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

/**
 * The terrain of the dimensions the original built on its copy of the 1.7.10 overworld generator: Utopia, the Village,
 * Crystal and Mining (orig ChunkProviderOreSpawn.java:181-262, identical in ChunkProviderOreSpawn2, 3 and 5). The density
 * at each 4 x 8 x 4 cell corner is the 1.7.10 one, which the chunk interpolates between corners as the original did
 * (the noise router's {@code interpolated}); the port had built these dimensions on 1.18's overworld noise, whose
 * continents, erosion and ridges raise mountains thousands of blocks out (GitHub #6).
 *
 * <p>Seeded as the original: {@code new Random(seed)} draws, in order, the two 16-octave limit noises and the 8-octave
 * main noise (vanilla's {@link BlendedNoise}, which reproduces 1.7.10's blend of the three and which the noise settings'
 * {@code legacy_random_source} hands a {@code LegacyRandomSource(seed)}), the 4-octave surface noise, the 10-octave
 * scale noise and the 16-octave depth noise; this class draws the last three from the same source, after the first
 * three, so the surface and depth noises are the original's. Each dimension is one biome (WorldChunkManagerHell), so the 5 x 5
 * parabolic biome weighting reduces to that biome's height and variation, worked out once in the same float steps.</p>
 */
public final class LegacyTerrainNoise extends BlendedNoise {
    private static final MapCodec<LegacyTerrainNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("root_height").forGetter(noise -> noise.rootHeight),
            Codec.FLOAT.fieldOf("height_variation").forGetter(noise -> noise.heightVariation)
    ).apply(inst, (root, variation) -> new LegacyTerrainNoise(new XoroshiroRandomSource(0L), root, variation)));
    public static final KeyDispatchDataCodec<LegacyTerrainNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);

    /** The bounds the router may assume; the density's real range is far inside them. */
    private static final double BOUND = 1.0E6;
    /** The first block at cell 32, where the slide has reached its end value. */
    private static final int SLID_OUT_Y = 256;
    /**
     * At least the limit noises' blend in 1.7.10's units: sixteen octaves weighted 1, 2, 4 ... 32,768 (65,535 in all),
     * each improved-noise sample at most 2 in magnitude (two unit components of a gradient against offsets inside the
     * unit cell, mixed by weights between 0 and 1), divided by 512.
     */
    private static final double BLEND_BOUND = 65535.0 * 2.001 / 512.0;

    private final float rootHeight;
    private final float heightVariation;
    /** noiseGen6, the original's 16-octave depth noise. */
    private final ImprovedNoise[] depthOctaves = new ImprovedNoise[16];
    /** field_147430_m, the original's 4-octave surface noise (NoiseGeneratorPerlin): the surface's depth and Mining's stone. */
    private final LegacySimplex[] surfaceOctaves = new LegacySimplex[4];
    /** d14 and d12 before the depth noise: the biome's variation and height after the parabolic weighting. */
    private final double scale;
    private final double offset;
    /** The last column's depth-noise centre on each thread (every corner of a column shares it). */
    private final ThreadLocal<double[]> lastColumn = ThreadLocal.withInitial(() -> new double[] {Double.NaN, Double.NaN, 0.0});

    private LegacyTerrainNoise(RandomSource random, float rootHeight, float heightVariation) {
        // field_147431_j, field_147432_k, field_147429_l: the limit and main noises
        super(random, 0.25, 0.125, 80.0, 160.0, 8.0);
        // field_147430_m, NoiseGeneratorPerlin(rand, 4): the surface noise's four simplex generators
        for (int i = 0; i < 4; i++) this.surfaceOctaves[i] = new LegacySimplex(random);
        // noiseGen5, NoiseGeneratorOctaves(rand, 10): drawn and not used by the terrain
        for (int i = 0; i < 10; i++) new ImprovedNoise(random);
        // noiseGen6, NoiseGeneratorOctaves(rand, 16): the depth noise
        for (int i = 0; i < 16; i++) this.depthOctaves[i] = new ImprovedNoise(random);
        this.rootHeight = rootHeight;
        this.heightVariation = heightVariation;

        // orig :79-84 and :206-231, one biome all round, in the original's float steps
        float[] parabolicField = new float[25];
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                parabolicField[j + 2 + (k + 2) * 5] = 10.0f / (float) Math.sqrt((float) (j * j + k * k) + 0.2f);
            }
        }
        float f = 0.0f;
        float f1 = 0.0f;
        float f2 = 0.0f;
        for (int l1 = -2; l1 <= 2; ++l1) {
            for (int i2 = -2; i2 <= 2; ++i2) {
                float f5 = parabolicField[l1 + 2 + (i2 + 2) * 5] / (rootHeight + 2.0f);
                f += heightVariation * f5;
                f1 += rootHeight * f5;
                f2 += f5;
            }
        }
        f /= f2;
        f1 /= f2;
        f = f * 0.9f + 0.1f;
        f1 = (f1 * 4.0f - 1.0f) / 8.0f;
        this.scale = f;
        this.offset = f1;
    }

    /** The terrain of a world seed as the original's generator built it (the tests' entry; the game seeds through the router). */
    public static LegacyTerrainNoise seeded(long seed, float rootHeight, float heightVariation) {
        return new LegacyTerrainNoise(new LegacyRandomSource(seed), rootHeight, heightVariation);
    }

    /** The noise settings' {@code legacy_random_source} gives a {@code LegacyRandomSource(seed)}: the original's Random. */
    @Override
    public BlendedNoise withNewRandom(RandomSource random) {
        return new LegacyTerrainNoise(random, this.rootHeight, this.heightVariation);
    }

    /**
     * orig :232-258 at the block's cell coordinates (x / 4, y / 8, z / 4): the limit noises blended by the main noise,
     * less the depth term around the column's centre, slid to -10 over the top three cells; scaled by 1/128 as vanilla's
     * {@link BlendedNoise}, so the sign (solid above zero) is the original's.
     */
    @Override
    public double compute(DensityFunction.FunctionContext context) {
        if (context.blockY() >= SLID_OUT_Y) {
            // the original's 33 cells end at y 256: from there the slide's end value is the whole density (d10 * 0 - 10)
            return -10.0 / 128.0;
        }
        // MathHelper.denormalizeClamp(e / 512, f / 512, (d / 10 + 1) / 2): vanilla's BlendedNoise returns it / 128
        double blend = super.compute(context) * 128.0;
        double centre = this.columnCentre(context.blockX(), context.blockZ());
        double cellY = context.blockY() / 8.0;
        double d6 = (cellY - centre) * 12.0 * 128.0 / 256.0 / this.scale;
        if (d6 < 0.0) {
            d6 *= 4.0;
        }
        double d10 = blend - d6;
        if (cellY > 29.0) {
            // the original's 33 cells end at y 256; above them the slide's end value holds
            double d11 = cellY >= 32.0 ? 1.0 : (double) ((float) (cellY - 29.0) / 3.0f);
            d10 = d10 * (1.0 - d11) + -10.0 * d11;
        }
        return d10 / 128.0;
    }

    /**
     * A cell height (block y / 8) at and above which the density in the column is at most {@code threshold}, worked out
     * from the column's depth noise alone: the blend is at most {@link #BLEND_BOUND}, above the centre the depth term
     * grows by 6 / scale a cell, and the slide only mixes in -10. For a threshold of at least -10 / 128; the reader of
     * the terrain starts its scans there.
     */
    public double cellYAtMost(int blockX, int blockZ, double threshold) {
        // the depth term grows upward only with a positive scale, and the slide's -10 must be under the threshold
        if (!(this.scale > 0.0) || threshold < -10.0 / 128.0 || threshold > BLEND_BOUND / 256.0) return Double.POSITIVE_INFINITY;
        return this.columnCentre(blockX, blockZ) + (BLEND_BOUND - 128.0 * threshold) * this.scale / 6.0 + 0.01;
    }

    /**
     * The original's surface noise at a block column (orig replaceBlocksForBiome: {@code stoneNoise} from
     * {@code field_147430_m.func_151599_a(arr, chunkX * 16, chunkZ * 16, 16, 16, 0.0625, 0.0625, 1.0)}): four simplex
     * octaves, each at half the frequency and twice the amplitude of the one before, 0.55 the first, summed in the
     * original's order. The array's index and the surface pass's swap of x and z cancel, so a column's value is the
     * noise at its own x and z.
     */
    public double surfaceNoise(int blockX, int blockZ) {
        double sum = 0.0;
        double step = 1.0;
        for (LegacySimplex octave : this.surfaceOctaves) {
            double frequency = 0.0625 * step;
            sum += octave.value((double) blockX * frequency + octave.xo, (double) blockZ * frequency + octave.yo) * (0.55 / step);
            step *= 0.5;
        }
        return sum;
    }

    /** d5: the column's centre cell, from the depth noise (orig :232-255). */
    private double columnCentre(int blockX, int blockZ) {
        double[] last = this.lastColumn.get();
        if (last[0] == blockX && last[1] == blockZ) {
            return last[2];
        }
        double d13 = this.depthNoise(blockX / 4.0, blockZ / 4.0) / 8000.0;
        if (d13 < 0.0) {
            d13 = -d13 * 0.3;
        }
        if ((d13 = d13 * 3.0 - 2.0) < 0.0) {
            if ((d13 /= 2.0) < -1.0) {
                d13 = -1.0;
            }
            d13 /= 1.4;
            d13 /= 2.0;
        } else {
            if (d13 > 1.0) {
                d13 = 1.0;
            }
            d13 /= 8.0;
        }
        double d12 = this.offset;
        d12 += d13 * 0.2;
        d12 = d12 * 8.5 / 8.0;
        double centre = 8.5 + d12 * 4.0;
        last[0] = blockX;
        last[1] = blockZ;
        last[2] = centre;
        return centre;
    }

    /**
     * noiseGen6.generateNoiseOctaves(arr, x, z, 5, 5, 200, 200, 0.5) at one column: the 2-D path of 1.7.10's improved
     * noise (a y size of 1), which samples the lattice's zero plane and leaves the generator's y offset out, so each
     * octave is vanilla's 3-D noise at y = -yo. The base coordinate's integer part wraps at 2^24 as the original's.
     */
    private double depthNoise(double cellX, double cellZ) {
        double sum = 0.0;
        double d3 = 1.0;
        for (ImprovedNoise octave : this.depthOctaves) {
            double x = wrap(cellX * d3 * 200.0);
            double z = wrap(cellZ * d3 * 200.0);
            sum += octave.noise(x, -octave.yo, z) / d3;
            d3 /= 2.0;
        }
        return sum;
    }

    /**
     * 1.7.10's simplex generator (NoiseGeneratorSimplex), its two-dimensional value as the surface noise sums it: the
     * same draws as vanilla's {@code SimplexNoise} (three offsets, then the permutation), and the original's floor,
     * which takes one off an exact non-positive whole number where {@code Mth.floor} does not.
     */
    static final class LegacySimplex {
        private static final int[][] GRADIENT = {{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1},
                {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
        private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
        final double xo;
        final double yo;
        private final int[] p = new int[512];

        LegacySimplex(RandomSource random) {
            this.xo = random.nextDouble() * 256.0;
            this.yo = random.nextDouble() * 256.0;
            random.nextDouble(); // zo, unused in two dimensions
            for (int i = 0; i < 256; i++) this.p[i] = i;
            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256 - i) + i;
                int k = this.p[i];
                this.p[i] = this.p[j];
                this.p[j] = k;
                this.p[i + 256] = this.p[i];
            }
        }

        private static int floor(double value) {
            return value > 0.0 ? (int) value : (int) value - 1;
        }

        private static double corner(int gradient, double x, double y) {
            double t = 0.5 - x * x - y * y;
            if (t < 0.0) return 0.0;
            t *= t;
            return t * t * (GRADIENT[gradient][0] * x + GRADIENT[gradient][1] * y);
        }

        double value(double x, double y) {
            double s = (x + y) * F2;
            int i = floor(x + s);
            int j = floor(y + s);
            double t = (double) (i + j) * G2;
            double x0 = x - ((double) i - t);
            double y0 = y - ((double) j - t);
            int i1;
            int j1;
            if (x0 > y0) {
                i1 = 1;
                j1 = 0;
            } else {
                i1 = 0;
                j1 = 1;
            }
            double x1 = x0 - (double) i1 + G2;
            double y1 = y0 - (double) j1 + G2;
            double x2 = x0 - 1.0 + 2.0 * G2;
            double y2 = y0 - 1.0 + 2.0 * G2;
            int ii = i & 255;
            int jj = j & 255;
            int g0 = this.p[ii + this.p[jj]] % 12;
            int g1 = this.p[ii + i1 + this.p[jj + j1]] % 12;
            int g2 = this.p[ii + 1 + this.p[jj + 1]] % 12;
            return 70.0 * (corner(g0, x0, y0) + corner(g1, x1, y1) + corner(g2, x2, y2));
        }
    }

    private static double wrap(double value) {
        long whole = Mth.lfloor(value);
        value -= (double) whole;
        whole %= 16777216L;
        return value + (double) whole;
    }

    @Override
    public double minValue() {
        return -BOUND;
    }

    @Override
    public double maxValue() {
        return BOUND;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
