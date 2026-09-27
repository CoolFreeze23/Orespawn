package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The Utopia trees on the structure pipeline (WGEN-072, WGEN-073, WGEN-074).
 *
 * <p>1.7.10 planted its Utopia trees from the chunk populator: {@code OreSpawnWorld.addOtherTrees} (:2508-2547) rolled
 * one chunk in thirty for a grove of up to four Wind trees or three Sky trees, and {@code addHugeTree} (:1830-1880)
 * rolled one chunk in fifty for one big tree: square, circular, round or (one in a hundred) royal. Every one of them
 * reaches far past its chunk (a Sky tree's canopy is 25-34 blocks each way, a Wind tree's branches up to 37, the big
 * trees' up to about 130), which a 1.21.1 feature cannot do: {@code WorldGenRegion.ensureCanWrite} drops every write
 * beyond one chunk from the chunk being decorated, so the feature ports of these trees generated with their branches
 * sheared off at that boundary (GitHub issue #1; the failure BUG-021 records for the Crystal castle trees). A
 * structure piece is written once per chunk it spans, so the trees now live in {@link UtopiaTreePiece}, on the pattern
 * of the royal trees ({@link RoyalTreePiece}).
 *
 * <p>Two structures share this type, told apart by the {@code roll} field: {@code grove} carries the addOtherTrees
 * roll, {@code huge} the addHugeTree roll. Each is placed with {@code random_spread} spacing 1 / separation 0, so every
 * Utopia chunk asks {@link #findGenerationPoint}, which makes the original's per-chunk roll with the chunk's own
 * worldgen random (the gate, the LessLag gates, the placement attempts and the per-tree parameters, in the original's
 * draw order) and answers with the pieces or with nothing. The royal branch of the huge roll (one in a hundred) keeps
 * its own structure set (royal_trees.json) and makes no tree here.
 *
 * <p>The original's downward air scan for a grass block under air is answered from the chunk generator's base column
 * (the terrain before decoration): the surface must be inside the original's window and must not be water. A canopy
 * that an earlier tree left over the column, which stopped the original's scan, is not seen here; nor is a grass block
 * under air beneath an overhang, which the huge roll's scan (:1844-1846) would have taken. The trees' own shape draws
 * come from a per-piece seed drawn after the type roll: the original drew its shapes from OreSpawnRand and the world's
 * random, never from the chunk's, so only the roll's draw order is the original's.
 *
 * <p>WGEN-075, the order the original kept (GitHub issue #4). OreSpawnWorld is a Forge world generator, which 1.7.10
 * ran after the chunk's own decoration, and in Utopia it rolled the huge tree first and grew nothing else of its own
 * in a chunk that got one: no apple trees, no grove, no veggies (OreSpawnWorld.java:42-47). The huge trees and the
 * royal trees therefore generate in {@code top_layer_modification}, after every tree and plant of the vegetation step
 * and after the groves, and overwrite them where they build, as the original's did (it spared only stone and bedrock,
 * ItemMagicApple.isBoringBaseBlock); the grove, the magic apple trees and the Utopia veggies stay out of a chunk
 * where one of those trees is rooted ({@link #chunkPass}, {@link #bigTreeRootedAt}). A grove rooted in a
 * neighbouring chunk can still reach under a big tree's branches, as the reporter remembered from 1.7.10; where the
 * two meet, the big tree wins (in 1.7.10 whichever chunk populated last won).
 *
 * <p>WGEN-077, one random for the whole pass. The original drew the huge roll, the apple trees and the grove from the
 * populator's one random, in that order, and the apple trees short-circuited the grove: {@code !addAppleTrees(...) &&
 * !addOtherTrees(...)} (:43) never ran addOtherTrees in a chunk that grew an apple tree. {@link #chunkPass} replays
 * that pass on the chunk's worldgen random: the huge roll's draws, then (when it grew nothing and no royal tree starts
 * here) the apple trees' ({@link #appleTrees}), then (when those grew nothing) the grove's. The grove structure, the
 * magic apple tree feature and the King altars all read their answer from it, so they agree chunk for chunk. Drawing
 * the rolls one after another on one random also keeps them as independent as the original's: two rolls that each
 * started on a fresh copy of the chunk's random would share their first draw, and then the huge gate (one in fifty)
 * would pass in a fifth of the grove chunks, not in a fiftieth.
 */
public class UtopiaTreeStructure extends Structure {

    public enum Roll implements StringRepresentable {
        GROVE("grove"), HUGE("huge");

        public static final Codec<Roll> CODEC = StringRepresentable.fromEnum(Roll::values);
        private final String name;

        Roll(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final MapCodec<UtopiaTreeStructure> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            settingsCodec(inst),
            Roll.CODEC.fieldOf("roll").forGetter(s -> s.roll)
    ).apply(inst, UtopiaTreeStructure::new));

    private final Roll roll;

    public UtopiaTreeStructure(StructureSettings settings, Roll roll) {
        super(settings);
        this.roll = roll;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ColumnProbe probe = probe(context);
        List<UtopiaTreePiece> pieces = switch (roll) {
            case GROVE -> chunkPass(context.seed(), context.chunkPos(), probe,
                    () -> RoyalTreeStructure.startsIn(context)).grove();
            case HUGE -> huge(context.random(), context.chunkPos(), probe);
        };
        // WGEN-077: the royal tree was the huge roll's own branch (:1860), so a chunk grew one big tree at most; where
        // the royal_trees set starts a King or a Queen, the huge roll's tree gives way to it.
        if (roll == Roll.HUGE && !pieces.isEmpty() && RoyalTreeStructure.startsIn(context)) return Optional.empty();
        if (pieces.isEmpty()) return Optional.empty();
        BlockPos origin = pieces.get(0).origin();
        return Optional.of(new GenerationStub(origin, builder -> pieces.forEach(builder::addPiece)));
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.UTOPIA_TREE.get();
    }

    /**
     * orig OreSpawnWorld.java:2508-2547 {@code addOtherTrees}: the 1-in-30 gate, the LessLag gates, {@code what}
     * choosing Wind or Sky for the whole chunk, up to five placement attempts, the fourth Wind tree or the third Sky
     * tree ending the grove. {@code dir} is assigned 0 once and never changed (:2527), so every Wind tree leans +x.
     */
    public static List<UtopiaTreePiece> grove(RandomSource random, ChunkPos chunk, ColumnProbe probe) {
        List<UtopiaTreePiece> out = new ArrayList<>();
        if (random.nextInt(30) != 0) return out;                                   // :2511
        int nc = 5;                                                                 // :2509
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1) {                                                         // :2514-2519
            if (random.nextInt(2) != 0) return out;
            nc = 4;
        }
        if (lessLag == 2) {                                                         // :2520-2525
            if (random.nextInt(4) != 0) return out;
            nc = 3;
        }
        int dir = 0;                                                                // :2527
        int what = random.nextInt(2);                                               // :2528
        int count = 0;
        for (int i = 0; i < nc; i++) {                                              // :2529
            int posX = 3 + chunk.getMinBlockX() + random.nextInt(10);               // :2530
            int posZ = 3 + chunk.getMinBlockZ() + random.nextInt(10);               // :2531
            int base = probe.base(posX, posZ, 50, 100);                             // :2532-2533
            if (base == Integer.MIN_VALUE) continue;
            ++count;                                                                // :2534
            if (what == 0) {
                // orig Trees.java:66-67 WindTree: height nextInt(8) + 40; width nextInt(4) + 8, drawn and unused
                int height = random.nextInt(8) + 40;
                random.nextInt(4);
                out.add(UtopiaTreePiece.wind(new BlockPos(posX, base, posZ), height, dir));
                if (count >= 4) break;                                              // :2537-2538
            } else {
                // orig Trees.java:101-104 SkyTree: the top nextInt(15) + 190, an absolute Y; no tree under 20 blocks
                // of trunk; the canopy half-width nextInt(10) + 25; the lower ring 5 + nextInt(4) below the top (:115)
                int top = random.nextInt(15) + 190;
                if (top - base >= 20) {
                    int width = random.nextInt(10) + 25;
                    int drop = random.nextInt(4);
                    out.add(UtopiaTreePiece.sky(new BlockPos(posX, base, posZ), top, width, drop));
                }
                if (count >= 3) break;                                              // :2541-2542
            }
        }
        return out;
    }

    /**
     * orig OreSpawnWorld.java:1830-1880 {@code addHugeTree}: the 1-in-50 gate, the LessLag gates, three placement
     * attempts, one tree. The type roll (:1855): above 75 the square tree (its leaves the apple's one time in twenty
     * when the wood is not jungle, :1856), 0 the royal tree (kept by the royal_trees structure set; the King-or-Queen
     * draw is consumed and nothing is placed here), above 15 the circular tree, else the round tree; the radius
     * 6 - nextInt(2) (:1849) for the square tree, redrawn 6 - nextInt(3) for the circular and round ones (:1869,
     * :1872); three trees in four carry no critters (:1852-1854).
     */
    public static List<UtopiaTreePiece> huge(RandomSource random, ChunkPos chunk, ColumnProbe probe) {
        List<UtopiaTreePiece> out = new ArrayList<>();
        if (random.nextInt(50) != 0) return out;                                   // :1832
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1 && random.nextInt(2) != 0) return out;                    // :1835
        if (lessLag == 2 && random.nextInt(4) != 0) return out;                    // :1838
        for (int i = 0; i < 3; i++) {                                               // :1841
            int posX = 4 + chunk.getMinBlockX() + random.nextInt(8);                // :1842
            int posZ = 4 + chunk.getMinBlockZ() + random.nextInt(8);                // :1843
            int base = probe.base(posX, posZ, 50, 127);                             // :1844-1845
            if (base == Integer.MIN_VALUE) continue;
            BlockPos origin = new BlockPos(posX, base, posZ);
            int treeType = random.nextInt(4);                                       // :1848
            int radius = 6 - random.nextInt(2);                                     // :1849
            boolean noCritters = random.nextInt(100) > 25;                          // :1852-1854
            int roll = random.nextInt(100);                                         // :1855
            if (roll > 75) {                                                        // :1855
                boolean apple = treeType != 3 && random.nextInt(20) == 0;           // :1856-1858
                out.add(UtopiaTreePiece.square(origin, random.nextLong(), radius, treeType, apple, noCritters));
            } else if (roll == 0) {                                                 // :1860
                random.nextInt(2);                                                  // :1863, the King-or-Queen pick
            } else if (roll > 15) {                                                 // :1868
                radius = 6 - random.nextInt(3);                                     // :1869
                out.add(UtopiaTreePiece.circular(origin, random.nextLong(), radius, treeType, noCritters));
            } else {
                radius = 6 - random.nextInt(3);                                     // :1872
                out.add(UtopiaTreePiece.round(origin, random.nextLong(), radius, treeType));
            }
            break;                                                                  // :1875 made_one
        }
        return out;
    }

    /** Answers a roll's column scan: the grass block's Y for a tree base inside the window, or {@link Integer#MIN_VALUE}. */
    @FunctionalInterface
    public interface ColumnProbe {
        int base(int x, int z, int windowLow, int windowHigh);
    }

    /**
     * orig OreSpawnWorld.java:1792-1828 {@code addAppleTrees}: {@code freq} from the chunk's distance in chunks,
     * folded to 0-14 (:1793, :1797); the count {@code 2 + nextInt(2 + (15 - freq) / 2)} (:1797) and the leaves draw
     * (:1798, apple eight times in ten, cherry and peach once each; the port grows apple leaves for all three, see
     * {@code MagicAppleTreeFeature}) come before the gate {@code nextInt(15 + freq)} (:1799-1801); the LessLag cuts
     * (:1802-1807); then each tree at {@code 2 + nextInt(12)} into the chunk on the grass the scan finds (:1808-1825).
     * Returns the grass blocks the trees stand on; empty when the chunk grows none (the original's {@code false}).
     */
    public static List<BlockPos> appleTrees(RandomSource random, ChunkPos chunk, ColumnProbe probe) {
        List<BlockPos> out = new ArrayList<>();
        int freq = (Math.abs(chunk.x) + Math.abs(chunk.z)) % 15;                   // :1793, :1797
        int howmany = 2 + random.nextInt(2 + (15 - freq) / 2);                      // :1794, :1797
        random.nextInt(10);                                                         // :1798 which
        if (random.nextInt(15 + freq) != 0) return out;                            // :1799-1801
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1) howmany /= 2;                                             // :1802-1804
        if (lessLag == 2 && (howmany /= 4) < 1) return out;                         // :1805-1807
        for (int i = 0; i < howmany; i++) {                                         // :1808
            int posX = 2 + chunk.getMinBlockX() + random.nextInt(12);               // :1809
            int posZ = 2 + chunk.getMinBlockZ() + random.nextInt(12);               // :1810
            int base = probe.base(posX, posZ, 50, 100);                             // :1811-1812
            if (base != Integer.MIN_VALUE) out.add(new BlockPos(posX, base, posZ)); // :1813-1824
        }
        return out;
    }

    /**
     * What the Utopia chunk pass grows (orig OreSpawnWorld.java:42-46): one of addHugeTree's trees (a huge tree or a
     * royal tree), else apple trees, else a grove, or nothing.
     */
    public record ChunkPass(boolean bigTree, List<BlockPos> appleTrees, List<UtopiaTreePiece> grove) {
        /** Whether the pass grew any tree of its own; the King altar rolled only when it grew none (:43-45). */
        public boolean grewTrees() {
            return bigTree || !appleTrees.isEmpty() || !grove.isEmpty();
        }
    }

    private static final ChunkPass BIG_TREE = new ChunkPass(true, List.of(), List.of());

    /**
     * WGEN-075 / WGEN-077: the Utopia chunk pass on one random (orig OreSpawnWorld.java:42-46), seeded as the structure
     * pass seeds a structure's context ({@code makeRandom}: a {@code LegacyRandomSource(0)} given
     * {@code setLargeFeatureSeed(seed, x, z)}), so its first draws are exactly the huge structure's. The huge roll
     * first; when it grows a tree, or {@code royalTreeHere} (the royal_trees set, keeper of the huge roll's royal
     * branch) answers yes, nothing else of the pass grows. Otherwise the apple trees' draws follow on the same random,
     * and the grove's only when the apple trees grew none (:43).
     */
    public static ChunkPass chunkPass(long seed, ChunkPos chunk, ColumnProbe probe, BooleanSupplier royalTreeHere) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, chunk.x, chunk.z);
        if (!huge(random, chunk, probe).isEmpty() || royalTreeHere.getAsBoolean()) return BIG_TREE;
        List<BlockPos> apples = appleTrees(random, chunk, probe);
        if (!apples.isEmpty()) return new ChunkPass(false, List.copyOf(apples), List.of());
        return new ChunkPass(false, List.of(), grove(random, chunk, probe));
    }

    /** The column probe for a structure's generation context (see {@link #grassBase}). */
    public static ColumnProbe probe(GenerationContext context) {
        return probe(context.chunkGenerator(), context.heightAccessor(), context.randomState());
    }

    /**
     * The column probe on a chunk generator's base column: the same answer the structure pass gets, so a feature that
     * replays the chunk pass agrees with the structures chunk for chunk.
     */
    public static ColumnProbe probe(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState) {
        return (x, z, low, high) -> grassBase(generator, heights, randomState, x, z, low, high);
    }

    /** The structures that are addHugeTree's trees: the Utopia huge tree and the two royal trees. */
    private static final Set<ResourceLocation> BIG_TREES = Set.of(
            ResourceLocation.fromNamespaceAndPath("orespawn", "utopia_huge_tree"),
            ResourceLocation.fromNamespaceAndPath("orespawn", "royal_tree_king"),
            ResourceLocation.fromNamespaceAndPath("orespawn", "royal_tree_queen"));

    /**
     * WGEN-075: whether one of addHugeTree's trees is rooted in the chunk holding {@code pos}, read from that chunk's
     * structure starts (settled before any feature runs). The magic apple tree and the veggie features ask it before
     * placing, since 1.7.10 grew neither in such a chunk (OreSpawnWorld.java:42-47).
     */
    public static boolean bigTreeRootedAt(WorldGenLevel level, BlockPos pos) {
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> start : level.getChunk(pos).getAllStarts().entrySet()) {
            if (start.getValue().isValid() && BIG_TREES.contains(structures.getKey(start.getKey()))) return true;
        }
        return false;
    }

    /**
     * The original's scan (:2532-2533 / :1844-1845): {@code posY} runs down from the window's top through air; the
     * tree goes on a grass block at {@code posY - 1}. Answered from the base column: its surface (the first air above
     * the terrain) must be inside the window and must be solid ground, not water. Returns the base Y, or
     * {@link Integer#MIN_VALUE} when the column refuses.
     */
    private static int grassBase(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState,
                                 int x, int z, int windowLow, int windowHigh) {
        int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heights, randomState);
        int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, randomState);
        if (surface != floor) return Integer.MIN_VALUE;
        if (surface <= windowLow || surface > windowHigh) return Integer.MIN_VALUE;
        return surface - 1;
    }
}
