package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.OreSpawnConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
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
        List<UtopiaTreePiece> pieces = switch (roll) {
            case GROVE -> grove(context);
            case HUGE -> huge(context);
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
    private static List<UtopiaTreePiece> grove(GenerationContext context) {
        WorldgenRandom random = context.random();
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
        ChunkPos chunk = context.chunkPos();
        int dir = 0;                                                                // :2527
        int what = random.nextInt(2);                                               // :2528
        int count = 0;
        for (int i = 0; i < nc; i++) {                                              // :2529
            int posX = 3 + chunk.getMinBlockX() + random.nextInt(10);               // :2530
            int posZ = 3 + chunk.getMinBlockZ() + random.nextInt(10);               // :2531
            int base = grassBase(context, posX, posZ, 50, 100);                     // :2532-2533
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
    private static List<UtopiaTreePiece> huge(GenerationContext context) {
        WorldgenRandom random = context.random();
        List<UtopiaTreePiece> out = new ArrayList<>();
        if (random.nextInt(50) != 0) return out;                                   // :1832
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1 && random.nextInt(2) != 0) return out;                    // :1835
        if (lessLag == 2 && random.nextInt(4) != 0) return out;                    // :1838
        ChunkPos chunk = context.chunkPos();
        for (int i = 0; i < 3; i++) {                                               // :1841
            int posX = 4 + chunk.getMinBlockX() + random.nextInt(8);                // :1842
            int posZ = 4 + chunk.getMinBlockZ() + random.nextInt(8);                // :1843
            int base = grassBase(context, posX, posZ, 50, 127);                     // :1844-1845
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

    /**
     * The original's scan (:2532-2533 / :1844-1845): {@code posY} runs down from the window's top through air; the
     * tree goes on a grass block at {@code posY - 1}. Answered from the base column: its surface (the first air above
     * the terrain) must be inside the window and must be solid ground, not water. Returns the base Y, or
     * {@link Integer#MIN_VALUE} when the column refuses.
     */
    private static int grassBase(GenerationContext context, int x, int z, int windowLow, int windowHigh) {
        int surface = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        int floor = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                context.heightAccessor(), context.randomState());
        if (surface != floor) return Integer.MIN_VALUE;
        if (surface <= windowLow || surface > windowHigh) return Integer.MIN_VALUE;
        return surface - 1;
    }
}
