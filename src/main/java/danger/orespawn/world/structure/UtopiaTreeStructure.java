package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.world.GenerationRange;
import danger.orespawn.world.LegacyTerrainReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
 * draw order) and answers with the pieces or with nothing. The royal branch of the huge roll (one in a hundred) makes
 * no tree here: the King or the Queen it picks is built by {@link RoyalTreeStructure}, which reads the same roll
 * ({@link #hugeRoll}).
 *
 * <p>The original's downward air scan for a grass block under air is answered from the chunk generator's base column
 * (the terrain before decoration): the surface must be inside the original's window and must not be water. A log or a
 * leaf of a tree the same pass grew earlier, over the column below Y100, stops the grove's and the apple trees' scans as
 * it stopped the original's ({@link ScanShadow}, {@link #underAppleTree}; GitHub #6); a tree of a neighbouring chunk,
 * which the original saw only when that chunk had populated first, is not seen, nor a plant on the grass, nor a grass
 * block under air beneath an overhang, which the huge roll's scan (:1844-1846) would have taken. The trees' own shape draws
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
 * two meet, the big tree wins (in 1.7.10 whichever chunk populated last won). GitHub #6: among the big trees the royal
 * ones now come last, so a huge tree rooted next door no longer cuts into a King or Queen tree: the structures of one
 * step generate in the order of their ids' paths, and the royal trees are {@code utopia_royal_tree_king} and
 * {@code utopia_royal_tree_queen}, after {@code utopia_huge_tree} (1.7.10 gave either order, as the chunks populated);
 * {@code royal_tree_king} and {@code royal_tree_queen} stay registered, out of every structure set, for the trees older
 * worlds already started.
 *
 * <p>WGEN-077, one random for the whole pass. The original drew the huge roll, the apple trees and the grove from the
 * populator's one random, in that order, and the apple trees short-circuited the grove: {@code !addAppleTrees(...) &&
 * !addOtherTrees(...)} (:43) never ran addOtherTrees in a chunk that grew an apple tree. {@link #chunkPass} replays
 * that pass on the chunk's worldgen random: the huge roll's draws, then (when it grew nothing, a royal tree included)
 * the apple trees' ({@link #appleTrees}), then (when those grew nothing) the grove's. The grove structure, the magic
 * apple tree feature and the King altars all read their answer from it, so they agree chunk for chunk. Drawing the
 * rolls one after another on one random also keeps them as independent as the original's: two rolls that each started
 * on a fresh copy of the chunk's random would share their first draw, and then the huge gate (one in fifty) would pass
 * in a fifth of the grove chunks, not in a fiftieth.
 *
 * <p>WGEN-080, the royal trees and the altars on the same pass. The King and Queen trees were the huge roll's royal
 * branch and the King and Queen altars the roll that came after the grove's (:43-44, {@link #altarRoll}); both now
 * come from the pass itself, one chunk at a time, rather than from a spacing grid, so they turn up as the original's
 * did: at its odds, as often in one spot as another, and never where the pass grew something else.
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
            case GROVE -> chunkPass(context.seed(), context.chunkPos(), probe).grove();
            case HUGE -> huge(context.random(), context.chunkPos(), probe);
        };
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
        ScanShadow shadow = new ScanShadow(chunk, 3, 12);
        for (int i = 0; i < nc; i++) {                                              // :2529
            int posX = 3 + chunk.getMinBlockX() + random.nextInt(10);               // :2530
            int posZ = 3 + chunk.getMinBlockZ() + random.nextInt(10);               // :2531
            int base = probe.base(posX, posZ, 50, 100);                             // :2532-2533
            if (base == Integer.MIN_VALUE) continue;
            // the scan runs down from Y100 through air only (:2532): an earlier tree of this grove over the column stops it
            if (shadow.blocks(posX, posZ, base)) continue;
            ++count;                                                                // :2534
            if (what == 0) {
                // orig Trees.java:66-67 WindTree: height nextInt(8) + 40; width nextInt(4) + 8, drawn and unused
                int height = random.nextInt(8) + 40;
                random.nextInt(4);
                UtopiaTreePiece wind = UtopiaTreePiece.wind(new BlockPos(posX, base, posZ), height, dir);
                out.add(wind);
                shadow.add(wind);
                if (count >= 4) break;                                              // :2537-2538
            } else {
                // orig Trees.java:101-104 SkyTree: the top nextInt(15) + 190, an absolute Y; no tree under 20 blocks
                // of trunk; the canopy half-width nextInt(10) + 25; the lower ring 5 + nextInt(4) below the top (:115)
                int top = random.nextInt(15) + 190;
                if (top - base >= 20) {
                    int width = random.nextInt(10) + 25;
                    int drop = random.nextInt(4);
                    UtopiaTreePiece sky = UtopiaTreePiece.sky(new BlockPos(posX, base, posZ), top, width, drop);
                    out.add(sky);
                    shadow.add(sky);
                }
                if (count >= 3) break;                                              // :2541-2542
            }
        }
        return out;
    }

    /** The huge roll's trees: the pieces of a square, circular or round tree, empty for no tree or a royal tree. */
    public static List<UtopiaTreePiece> huge(RandomSource random, ChunkPos chunk, ColumnProbe probe) {
        return hugeRoll(random, chunk, probe).trees();
    }

    /**
     * What addHugeTree grew in a chunk: a huge tree's pieces, or the site of the royal tree its one-in-a-hundred branch
     * picked (the King or the Queen), or nothing. At most one of them, as {@code made_one} ended the roll.
     */
    public record HugeRoll(List<UtopiaTreePiece> trees, BlockPos royal, boolean queen) {
        static final HugeRoll NONE = new HugeRoll(List.of(), null, false);

        /** Whether the roll grew a tree of any kind (the original's {@code made_one}). */
        public boolean madeOne() {
            return !trees.isEmpty() || royal != null;
        }
    }

    /**
     * orig OreSpawnWorld.java:1830-1880 {@code addHugeTree}: the 1-in-50 gate, the LessLag gates, three placement
     * attempts, one tree. The type roll (:1855): above 75 the square tree (its leaves the apple's one time in twenty
     * when the wood is not jungle, :1856), 0 the royal tree, the King when the next draw is 0 and the Queen otherwise
     * (:1860-1866: gold, emerald and diamond against obsidian, ruby and amethyst), above 15 the circular tree, else the
     * round tree; the radius 6 - nextInt(2) (:1849) for the square tree, redrawn 6 - nextInt(3) for the circular and
     * round ones (:1869, :1872); three trees in four carry no critters (:1852-1854). WGEN-080: the royal branch is the
     * only source of the King and Queen trees, as in the original, so they come one chunk in 5,000 where the roll finds
     * its grass, and never beside another of the roll's trees.
     */
    public static HugeRoll hugeRoll(RandomSource random, ChunkPos chunk, ColumnProbe probe) {
        if (random.nextInt(50) != 0) return HugeRoll.NONE;                         // :1832
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1 && random.nextInt(2) != 0) return HugeRoll.NONE;          // :1835
        if (lessLag == 2 && random.nextInt(4) != 0) return HugeRoll.NONE;          // :1838
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
                return new HugeRoll(List.of(UtopiaTreePiece.square(origin, random.nextLong(), radius, treeType, apple,
                        noCritters)), null, false);
            } else if (roll == 0) {                                                 // :1860
                return new HugeRoll(List.of(), origin, random.nextInt(2) != 0);     // :1863, King on 0
            } else if (roll > 15) {                                                 // :1868
                radius = 6 - random.nextInt(3);                                     // :1869
                return new HugeRoll(List.of(UtopiaTreePiece.circular(origin, random.nextLong(), radius, treeType,
                        noCritters)), null, false);
            } else {
                radius = 6 - random.nextInt(3);                                     // :1872
                return new HugeRoll(List.of(UtopiaTreePiece.round(origin, random.nextLong(), radius, treeType)),
                        null, false);
            }
        }
        return HugeRoll.NONE;
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
            // the scan runs down from Y100 through air only (:1811): an earlier tree's crown or trunk over the column
            // stops it before it reaches the grass
            if (base == Integer.MIN_VALUE || underAppleTree(out, posX, posZ, base)) continue;
            out.add(new BlockPos(posX, base, posZ));                                // :1813-1824
        }
        return out;
    }

    /** The King or Queen altar the pass's altar roll builds: on the grass at {@code origin}. */
    public record Altar(BlockPos origin, boolean queen) {}

    /**
     * What the Utopia chunk pass grows (orig OreSpawnWorld.java:42-46): one of addHugeTree's trees (a huge tree or a
     * royal tree), else apple trees, else a grove, else, when none of them grew, the altar its roll picked (or none).
     */
    public record ChunkPass(HugeRoll huge, List<BlockPos> appleTrees, List<UtopiaTreePiece> grove, Altar altar) {
        /** Whether addHugeTree grew a tree here (a huge one or a royal one). */
        public boolean bigTree() {
            return huge.madeOne();
        }

        /** Whether the pass grew any tree of its own; the King altar rolled only when it grew none (:43-45). */
        public boolean grewTrees() {
            return bigTree() || !appleTrees.isEmpty() || !grove.isEmpty();
        }
    }

    /**
     * WGEN-075 / WGEN-077 / WGEN-080: the Utopia chunk pass on one random (orig OreSpawnWorld.java:42-46), seeded as
     * the structure pass seeds a structure's context ({@code makeRandom}: a {@code LegacyRandomSource(0)} given
     * {@code setLargeFeatureSeed(seed, x, z)}), so its first draws are exactly the huge and royal structures'. The huge
     * roll first; when it grows a tree (a royal one included), nothing else of the pass grows. Otherwise the apple
     * trees' draws follow on the same random, the grove's only when the apple trees grew none (:43), and the altar's
     * roll only when the grove grew none too (:43-44, {@link #altarRoll}).
     */
    public static ChunkPass chunkPass(long seed, ChunkPos chunk, ColumnProbe probe) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, chunk.x, chunk.z);
        HugeRoll huge = hugeRoll(random, chunk, probe);
        if (huge.madeOne()) return new ChunkPass(huge, List.of(), List.of(), null);
        List<BlockPos> apples = appleTrees(random, chunk, probe);
        if (!apples.isEmpty()) return new ChunkPass(huge, List.copyOf(apples), List.of(), null);
        List<UtopiaTreePiece> grove = grove(random, chunk, probe);
        if (!grove.isEmpty()) return new ChunkPass(huge, List.of(), grove, null);
        return new ChunkPass(huge, List.of(), List.of(), altarRoll(random, chunk, probe));
    }

    /** A probe that finds no grass anywhere. */
    private static final ColumnProbe NO_GRASS = (x, z, low, high) -> Integer.MIN_VALUE;

    /**
     * Whether the altar roll passes in the one pass that reaches it: a pass reaches the roll only when none of the
     * chunk's tree rolls grew a tree, so every attempt of theirs found no grass (a grass found always grows its tree:
     * the huge roll's every type, the apple tree, the grove's Wind tree, and its Sky tree, whose top of 190 or more is
     * always 20 over a base of at most 100) and the draws before the roll are those of a pass that finds no grass
     * anywhere. False: the chunk has no altar whatever its terrain. True: {@link #chunkPass} decides.
     */
    public static boolean altarRollReached(long seed, ChunkPos chunk) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, chunk.x, chunk.z);
        if (hugeRoll(random, chunk, NO_GRASS).madeOne()) return false;
        if (!appleTrees(random, chunk, NO_GRASS).isEmpty()) return false;
        if (!grove(random, chunk, NO_GRASS).isEmpty()) return false;
        return random.nextInt(2000) == 1;                                           // :2550
    }

    /**
     * orig OreSpawnWorld.java:2549-2571 {@code addKingAltar}, drawn on after the pass's tree rolls as the original drew
     * it: one chunk in 2,000 ({@code nextInt(2000) != 1} ends it, :2550), then up to eight attempts at chunk + 3 +
     * nextInt(10) (:2553-2555) for grass under air inside Y51-100 (:2556-2557, the base column as for the trees), and
     * on the first grass found the King when the next draw is 0 and the Queen otherwise (:2562-2565). The altar is built
     * on the grass ({@code posY - 1}). The original's quickReallyBigSpaceCheck (:2558) is {@link #reallyBigSpaceClear},
     * asked by the altar's structure on the site this returns; the cooldown the original checked first (:43) is the
     * royal_altars structure's share ({@code LegacyDungeonStructure}).
     */
    static Altar altarRoll(RandomSource random, ChunkPos chunk, ColumnProbe probe) {
        if (random.nextInt(2000) != 1) return null;                                // :2550
        for (int i = 0; i < 8; i++) {                                               // :2553
            int posX = 3 + chunk.getMinBlockX() + random.nextInt(10);               // :2554
            int posZ = 3 + chunk.getMinBlockZ() + random.nextInt(10);               // :2555
            int base = probe.base(posX, posZ, 50, 100);                             // :2556-2557
            if (base == Integer.MIN_VALUE) continue;
            return new Altar(new BlockPos(posX, base, posZ), random.nextInt(2) != 0); // :2562, King on 0
        }
        return null;
    }


    /**
     * The cells a pass's earlier trees put over the columns its later attempts can pick, as the original's downward scans
     * met them: the scan runs from Y100 down through air only (addOtherTrees :2532, addAppleTrees :1811), so a log or a
     * leaf of a tree grown earlier in the same pass, anywhere from Y100 down to the grass, ends it before it finds the
     * grass. Kept per column as the highest such cell; cells above Y100 never stop a scan.
     */
    public static final class ScanShadow {
        public static final int SCAN_TOP = 100;
        private final int minX, maxX, minZ, maxZ;
        private final Map<Long, Integer> top = new HashMap<>();

        /** The columns of {@code chunk} from {@code from} to {@code to} blocks in, the attempts' own window. */
        public ScanShadow(ChunkPos chunk, int from, int to) {
            this.minX = chunk.getMinBlockX() + from;
            this.maxX = chunk.getMinBlockX() + to;
            this.minZ = chunk.getMinBlockZ() + from;
            this.maxZ = chunk.getMinBlockZ() + to;
        }

        public void add(UtopiaTreePiece tree) {
            tree.forEachCell(Integer.MIN_VALUE / 2, SCAN_TOP + 1, (x, y, z) -> {
                if (x < minX || x > maxX || z < minZ || z > maxZ || y > SCAN_TOP) return;
                top.merge(ChunkPos.asLong(x, z), y, Math::max);
            });
        }

        /** Whether a cell of an earlier tree stands in the column above the grass at {@code base}, up to Y100. */
        public boolean blocks(int x, int z, int base) {
            Integer y = top.get(ChunkPos.asLong(x, z));
            return y != null && y > base;
        }
    }

    /**
     * orig ItemAppleSeed.makeTree (ItemAppleSeed.java:46-123, MagicAppleTreeFeature's geometry): the trunk from the grass
     * up eleven blocks, the arms at +6 (four out) and +9 (two out), the crown's square discs at +6 to +8 (six out), +9 to
     * +10 (five) and +11 to +13 (four). Whether one of {@code trees} has a cell over the column {@code (x, z)} above the
     * grass at {@code base}, up to Y100, where the original's scan (:1811) stops.
     */
    public static boolean underAppleTree(List<BlockPos> trees, int x, int z, int base) {
        for (BlockPos t : trees) {
            int r = Math.max(Math.abs(x - t.getX()), Math.abs(z - t.getZ()));
            int highest;
            if (r <= 4) highest = t.getY() + 13;
            else if (r == 5) highest = t.getY() + 10;
            else if (r == 6) highest = t.getY() + 8;
            else continue;
            int lowest = r == 0 ? t.getY() + 1 : t.getY() + 6;   // the trunk from the grass up; elsewhere the crown
            if (Math.max(lowest, base + 1) <= Math.min(highest, ScanShadow.SCAN_TOP)) return true;
        }
        return false;
    }

    /**
     * orig OreSpawnWorld.java:2645-2653 {@code quickReallyBigSpaceCheck}, which addKingAltar asks on the first grass it
     * finds and which ends the roll when it fails (:2558-2560): every block of the 60 x 60 plane eight above the grass,
     * from five before the site to 54 past it on both axes, must be air. The original read the world as it stood: the
     * terrain, and the trees already grown around the site. Asked here of the terrain the generator gives for those
     * columns (stone or water at the plane fails it, as in the original) and of every Utopia tree with a cell on the
     * plane: the huge and royal trees, the apple trees and the groves of every chunk close enough to reach it, as if
     * they had all grown first (in 1.7.10 a neighbour that populated after the altar could still grow into it).
     */
    public static boolean reallyBigSpaceClear(long seed, ChunkGenerator generator, LevelHeightAccessor heights,
                                              RandomState randomState, BlockPos grass) {
        int plane = grass.getY() + 8;
        int minX = grass.getX() - 5, maxX = grass.getX() + 54, minZ = grass.getZ() - 5, maxZ = grass.getZ() + 54;
        int minY = GenerationRange.bottom(generator, heights), maxY = GenerationRange.top(generator, heights);
        if (!planeTerrainClear(generator, heights, randomState, minX, maxX, minZ, maxZ, plane)) return false;
        ColumnProbe probe = probe(generator, heights, randomState);
        int reach = 144;  // the farthest any Utopia tree's cells reach from its site (RoyalTreePiece's permit)
        int cx0 = (minX - reach) >> 4, cx1 = (maxX + reach) >> 4, cz0 = (minZ - reach) >> 4, cz1 = (maxZ + reach) >> 4;
        boolean[] hit = new boolean[1];
        UtopiaTreePiece.CellSink sink = (x, y, z) -> {
            if (y == plane && x >= minX && x <= maxX && z >= minZ && z <= maxZ) hit[0] = true;
        };
        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                ChunkPass pass = chunkPass(seed, new ChunkPos(cx, cz), probe);
                for (UtopiaTreePiece tree : pass.huge().trees()) {
                    if (reaches(tree.getBoundingBox(), plane, minX, maxX, minZ, maxZ)) tree.forEachCell(minY, maxY, sink);
                }
                if (pass.huge().royal() != null) {
                    RoyalTreePiece royal = new RoyalTreePiece(pass.huge().royal(), pass.huge().queen());
                    if (reaches(royal.getBoundingBox(), plane, minX, maxX, minZ, maxZ)) royal.forEachCell(minY, maxY, sink);
                }
                for (UtopiaTreePiece tree : pass.grove()) {
                    if (reaches(tree.getBoundingBox(), plane, minX, maxX, minZ, maxZ)) tree.forEachCell(minY, maxY, sink);
                }
                for (BlockPos apple : pass.appleTrees()) {
                    if (appleCellOnPlane(apple, plane, minX, maxX, minZ, maxZ)) return false;
                }
                if (hit[0]) return false;
            }
        }
        return true;
    }

    /**
     * The check's terrain half: whether every block of the plane over the rectangle is air as the generator builds it.
     * The 1.7.10 terrain's generators (one interpolated final density) are read by {@link LegacyTerrainReader}: the
     * density from the cell corners, the aquifer only where it can answer other than air, every block as getBaseColumn
     * builds it without building a noise column per column. Any other generator reads every column
     * ({@link #planeTerrainClearByColumns}).
     */
    public static boolean planeTerrainClear(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState,
                                            int minX, int maxX, int minZ, int maxZ, int plane) {
        if (plane < GenerationRange.bottom(generator, heights) || plane >= GenerationRange.top(generator, heights)) {
            return false;
        }
        LegacyTerrainReader reader = LegacyTerrainReader.of(generator, heights, randomState);
        if (reader == null) return planeTerrainClearByColumns(generator, heights, randomState, minX, maxX, minZ, maxZ, plane);
        return reader.planeClear(minX, maxX, minZ, maxZ, plane);
    }

    /** The full read: every column of the rectangle built as the generator builds it, its block at the plane air. */
    public static boolean planeTerrainClearByColumns(ChunkGenerator generator, LevelHeightAccessor heights,
                                                     RandomState randomState, int minX, int maxX, int minZ, int maxZ,
                                                     int plane) {
        if (plane < GenerationRange.bottom(generator, heights) || plane >= GenerationRange.top(generator, heights)) {
            return false;
        }
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!generator.getBaseColumn(x, z, heights, randomState).getBlock(plane).isAir()) return false;
            }
        }
        return true;
    }

    private static boolean reaches(BoundingBox box, int plane, int minX, int maxX, int minZ, int maxZ) {
        return box.minY() <= plane && box.maxY() >= plane && box.minX() <= maxX && box.maxX() >= minX
                && box.minZ() <= maxZ && box.maxZ() >= minZ;
    }

    /** Whether an apple tree on the grass at {@code t} has a cell on the plane inside the rectangle (makeTree's shape). */
    private static boolean appleCellOnPlane(BlockPos t, int plane, int minX, int maxX, int minZ, int maxZ) {
        int h = plane - t.getY();
        int r;
        if (h >= 1 && h <= 5) r = 0;            // the trunk alone
        else if (h >= 6 && h <= 8) r = 6;       // the widest discs (the arms at +6 lie inside them)
        else if (h == 9 || h == 10) r = 5;
        else if (h == 11) r = 4;                // the trunk's top log and the narrow discs
        else if (h == 12 || h == 13) r = 4;
        else return false;
        return t.getX() - r <= maxX && t.getX() + r >= minX && t.getZ() - r <= maxZ && t.getZ() + r >= minZ;
    }

    /** The column probe for a structure's generation context (see {@link #grassBase}). */
    public static ColumnProbe probe(GenerationContext context) {
        return probe(context.chunkGenerator(), context.heightAccessor(), context.randomState());
    }

    /**
     * The column probe on a chunk generator's base column: the same answer the structure pass gets, so a feature that
     * replays the chunk pass agrees with the structures chunk for chunk. The 1.7.10 terrain's generators answer through
     * {@link LegacyTerrainReader} (the same answer as {@link #fullProbe}, without building the two noise columns).
     */
    public static ColumnProbe probe(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState) {
        LegacyTerrainReader reader = LegacyTerrainReader.of(generator, heights, randomState);
        if (reader != null) return reader::grassBase;
        return fullProbe(generator, heights, randomState);
    }

    /** The column probe read from the generator's own noise columns ({@link #grassBase}). */
    public static ColumnProbe fullProbe(ChunkGenerator generator, LevelHeightAccessor heights, RandomState randomState) {
        return (x, z, low, high) -> grassBase(generator, heights, randomState, x, z, low, high);
    }

    /**
     * The structures that are addHugeTree's trees: the Utopia huge tree and the two royal trees, under their ids since
     * GitHub #6 and under the ids older worlds started them with.
     */
    private static final Set<ResourceLocation> BIG_TREES = Set.of(
            ResourceLocation.fromNamespaceAndPath("orespawn", "utopia_huge_tree"),
            ResourceLocation.fromNamespaceAndPath("orespawn", "utopia_royal_tree_king"),
            ResourceLocation.fromNamespaceAndPath("orespawn", "utopia_royal_tree_queen"),
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
