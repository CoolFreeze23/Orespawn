package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.feature.ModFeatures;
import danger.orespawn.world.structure.RoyalTreePiece;
import danger.orespawn.world.structure.UtopiaTreePiece;
import danger.orespawn.world.structure.UtopiaTreeStructure;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.OreSpawnChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * WGEN-072 / WGEN-073 / WGEN-074: the Utopia trees as structure pieces. Each test drives a piece's
 * {@code postProcess} on the gametest level with the template's own bounds as the chunk window, so the tree lands
 * inside the template; the geometry pins read the 1.7.10 sources the pieces transcribe (Trees.java:21-119,
 * ItemMagicApple.java:133-722), and one test builds a tree twice, whole and in two chunk slices, and compares.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class UtopiaTreeTests {

    private static BoundingBox box(GameTestHelper helper, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return BoundingBox.fromCorners(helper.absolutePos(new BlockPos(minX, minY, minZ)),
                helper.absolutePos(new BlockPos(maxX, maxY, maxZ)));
    }

    /** The whole {@code empty_tall} template (48 x 34 x 48). */
    private static BoundingBox templateBox(GameTestHelper helper) {
        return box(helper, 0, 0, 0, 47, 33, 47);
    }

    private static void build(GameTestHelper helper, UtopiaTreePiece piece, BoundingBox window) {
        ServerLevel level = helper.getLevel();
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.random,
                window, new ChunkPos(piece.origin()), piece.origin());
    }

    private static void assertIs(GameTestHelper helper, BlockPos abs, Block block, String what) {
        BlockState s = helper.getLevel().getBlockState(abs);
        helper.assertTrue(s.is(block), what + " at " + abs + ": expected " + block + ", found " + s.getBlock());
    }

    private static int count(GameTestHelper helper, BoundingBox window, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(window.minX(), window.minY(), window.minZ(),
                window.maxX(), window.maxY(), window.maxZ())) {
            if (helper.getLevel().getBlockState(p).is(block)) n++;
        }
        return n;
    }

    /** orig Trees.java:96-119: the trunk to the absolute top, the apex leaf, four canopy branches of the width, four lower branches of a third of it. */
    @GameTest(template = "empty_tall")
    public static void w072a_sky_tree_geometry(GameTestHelper helper) {
        BlockPos o = helper.absolutePos(new BlockPos(24, 1, 24));
        int top = o.getY() + 12;
        int width = 9;
        build(helper, UtopiaTreePiece.sky(o, top, width, 2), templateBox(helper));
        Block skyLog = ModBlocks.SKY_TREE_LOG.get();
        for (int y = o.getY(); y <= top; y++) {
            assertIs(helper, o.atY(y), skyLog, "the trunk (Trees.java:106-108)");
        }
        assertIs(helper, o.atY(top + 1), Blocks.OAK_LEAVES, "the apex leaf (:109)");
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            for (int i = 1; i < width; i++) {
                BlockPos b = new BlockPos(o.getX() + i * d[0], top, o.getZ() + i * d[1]);
                assertIs(helper, b, skyLog, "a canopy branch log (:81)");
                assertIs(helper, b.above(), Blocks.OAK_LEAVES, "the leaf above a branch log (:82-84)");
                assertIs(helper, b.offset(d[1], 0, d[0]), Blocks.OAK_LEAVES, "the leaf beside a branch log (:85-87)");
                assertIs(helper, b.offset(-d[1], 0, -d[0]), Blocks.OAK_LEAVES, "the leaf beside a branch log (:88-89)");
            }
            assertIs(helper, new BlockPos(o.getX() + width * d[0], top, o.getZ() + width * d[1]),
                    Blocks.OAK_LEAVES, "the branch tip leaf (:91-93)");
        }
        int lower = top - 5 - 2;
        assertIs(helper, new BlockPos(o.getX() + 1, lower, o.getZ()), skyLog, "the lower ring's first log (:115)");
        assertIs(helper, new BlockPos(o.getX() + 2, lower, o.getZ()), skyLog, "the lower ring's second log (:115)");
        assertIs(helper, new BlockPos(o.getX() + 3, lower, o.getZ()), Blocks.OAK_LEAVES, "the lower ring's tip (width / 3 = 3)");
        helper.succeed();
    }

    /** orig Trees.java:21-77: the trunk, the lean leaves above a fifth of the height, a branch every fourth row above a quarter of it, the apex leaf; every worldgen Wind tree leans +x. */
    @GameTest(template = "empty_tall")
    public static void w072b_wind_tree_geometry(GameTestHelper helper) {
        BlockPos o = helper.absolutePos(new BlockPos(4, 1, 24));
        int height = 20;
        build(helper, UtopiaTreePiece.wind(o, height, 0), templateBox(helper));
        for (int j = 0; j < height; j++) {
            assertIs(helper, o.offset(0, j, 0), Blocks.OAK_LOG, "the trunk (Trees.java:69)");
        }
        assertIs(helper, o.offset(0, height, 0), Blocks.OAK_LEAVES, "the apex leaf (:76)");
        for (int j = 0; j <= height / 5; j++) {
            helper.assertTrue(helper.getLevel().getBlockState(o.offset(1, j, 0)).isAir(),
                    "no lean leaf on the lowest fifth (:70), row " + j);
        }
        for (int j = height / 5 + 1; j < height; j++) {
            boolean branchRow = j > height / 4 && j % 4 == 0;
            assertIs(helper, o.offset(1, j, 0), branchRow ? Blocks.OAK_LOG : Blocks.OAK_LEAVES,
                    branchRow ? "a branch's first log over the lean leaf (:23)" : "the lean leaf (:71)");
        }
        // The row-16 branch: length 4 (height - j), logs 1..4, leaves above, side leaves past the inner third, two tip leaves.
        for (int i = 1; i <= 4; i++) {
            assertIs(helper, o.offset(i, 16, 0), Blocks.OAK_LOG, "a branch log (:23)");
            assertIs(helper, o.offset(i, 17, 0), Blocks.OAK_LEAVES, "the leaf above a branch log (:24-26)");
        }
        for (int i = 2; i <= 4; i++) {
            assertIs(helper, o.offset(i, 16, 1), Blocks.OAK_LEAVES, "a side leaf past the inner third (:31-33)");
            assertIs(helper, o.offset(i, 16, -1), Blocks.OAK_LEAVES, "a side leaf past the inner third (:34-35)");
        }
        assertIs(helper, o.offset(5, 16, 0), Blocks.OAK_LEAVES, "the first tip leaf (:37-39)");
        assertIs(helper, o.offset(6, 16, 0), Blocks.OAK_LEAVES, "the second tip leaf (:40-42)");
        // The row-8 branch: length 12, a second leaf layer on the inner third (i < 4), no side leaves there.
        for (int i = 1; i <= 3; i++) {
            assertIs(helper, o.offset(i, 10, 0), Blocks.OAK_LEAVES, "the second leaf layer on the inner third (:27-29)");
        }
        helper.assertTrue(helper.getLevel().getBlockState(o.offset(2, 8, 1)).isAir(),
                "no side leaf on the inner third (:30)");
        assertIs(helper, o.offset(13, 8, 0), Blocks.OAK_LEAVES, "the long branch's first tip leaf");
        assertIs(helper, o.offset(14, 8, 0), Blocks.OAK_LEAVES, "the long branch's second tip leaf");
        helper.succeed();
    }

    /** The stitching contract: a tree built in one pass equals the same tree built in two chunk slices, cell for cell. */
    @GameTest(template = "empty_tall")
    public static void w072c_two_chunk_passes_stitch_into_one_tree(GameTestHelper helper) {
        BlockPos a = helper.absolutePos(new BlockPos(24, 1, 12));
        BlockPos b = helper.absolutePos(new BlockPos(24, 1, 34));
        int width = 9;
        build(helper, UtopiaTreePiece.sky(a, a.getY() + 12, width, 2), templateBox(helper));
        build(helper, UtopiaTreePiece.sky(b, b.getY() + 12, width, 2), box(helper, 0, 0, 0, 27, 33, 47));
        build(helper, UtopiaTreePiece.sky(b, b.getY() + 12, width, 2), box(helper, 28, 0, 0, 47, 33, 47));
        int shift = b.getZ() - a.getZ();
        int cells = 0;
        for (int x = a.getX() - width - 1; x <= a.getX() + width + 1; x++) {
            for (int y = a.getY(); y <= a.getY() + 14; y++) {
                for (int z = a.getZ() - width - 1; z <= a.getZ() + width + 1; z++) {
                    BlockState one = helper.getLevel().getBlockState(new BlockPos(x, y, z));
                    BlockState two = helper.getLevel().getBlockState(new BlockPos(x, y, z + shift));
                    helper.assertTrue(one.getBlock() == two.getBlock(),
                            "the two-slice tree differs at " + new BlockPos(x, y, z + shift) + ": " + two.getBlock()
                                    + " against " + one.getBlock());
                    if (!one.isAir()) cells++;
                }
            }
        }
        helper.assertTrue(cells > 100, "the sky tree placed only " + cells + " cells");
        helper.succeed();
    }

    /** The data side: both structures and both sets are registered (a slip in the JSON would drop every Utopia tree silently), and the sets ask every chunk (spacing 1, separation 0). */
    @GameTest(template = "empty")
    public static void w072d_structures_and_sets_registered(GameTestHelper helper) {
        RegistryAccess access = helper.getLevel().registryAccess();
        Registry<Structure> structures = access.registryOrThrow(Registries.STRUCTURE);
        for (String id : new String[] {"orespawn:utopia_tree_grove", "orespawn:utopia_huge_tree"}) {
            Structure s = structures.get(ResourceLocation.parse(id));
            helper.assertTrue(s instanceof UtopiaTreeStructure, id + " is not a registered UtopiaTreeStructure");
        }
        Registry<StructureSet> sets = access.registryOrThrow(Registries.STRUCTURE_SET);
        for (String id : new String[] {"orespawn:utopia_tree_groves", "orespawn:utopia_huge_trees"}) {
            StructureSet set = sets.get(ResourceLocation.parse(id));
            helper.assertTrue(set != null, id + " is not a registered structure set");
            boolean everyChunk = set != null && set.placement() instanceof RandomSpreadStructurePlacement p
                    && p.spacing() == 1 && p.separation() == 0;
            helper.assertTrue(everyChunk, id + " does not ask every chunk (spacing 1, separation 0)");
        }
        helper.succeed();
    }

    /** orig ItemMagicApple.java:624-722: the first ring of the round tree at the original's float rounding, its disc branches' leaf rims. */
    @GameTest(template = "empty_tall")
    public static void w073a_round_tree_geometry(GameTestHelper helper) {
        BlockPos o = helper.absolutePos(new BlockPos(24, 2, 24));
        build(helper, UtopiaTreePiece.round(o, 11L, 2, 2), templateBox(helper));
        float fx = o.getX();
        fx += 0.5f;
        float fz = o.getZ();
        fz += 0.5f;
        for (int i : new int[] {0, 45, 90, 135, 180, 225, 270, 315}) {
            float fcurx = (float) (2.0 * Math.sin(Math.toRadians(i)));
            float fcurz = (float) (2.0 * Math.cos(Math.toRadians(i)));
            assertIs(helper, new BlockPos((int) (fx + fcurx), o.getY() + 1, (int) (fz + fcurz)), Blocks.BIRCH_LOG,
                    "the first ring (ItemMagicApple.java:654-663) at " + i + " degrees");
        }
        BoundingBox window = templateBox(helper);
        helper.assertTrue(count(helper, window, Blocks.BIRCH_LOG) > 100, "too few ring logs");
        helper.assertTrue(count(helper, window, Blocks.BIRCH_LEAVES) > 0, "no disc-branch leaf rim (:716-718)");
        helper.succeed();
    }

    /** orig ItemMagicApple.java:248-469: the square tree's outer wall ring, its step blocks, its branch leaves, the two emerald blocks at the apex. */
    @GameTest(template = "empty_tall")
    public static void w074a_square_tree_geometry(GameTestHelper helper) {
        BlockPos o = helper.absolutePos(new BlockPos(24, 2, 24));
        build(helper, UtopiaTreePiece.square(o, 5L, 2, 0, false, true), templateBox(helper));
        for (int y = o.getY(); y <= o.getY() + 3; y++) {
            for (int i = -2; i <= 2; i++) {
                assertIs(helper, new BlockPos(o.getX() + i, y, o.getZ() - 2), Blocks.OAK_LOG, "the outer wall (:316-322)");
                assertIs(helper, new BlockPos(o.getX() + i, y, o.getZ() + 2), Blocks.OAK_LOG, "the outer wall (:323-329)");
                assertIs(helper, new BlockPos(o.getX() - 2, y, o.getZ() + i), Blocks.OAK_LOG, "the outer wall (:330-336)");
                assertIs(helper, new BlockPos(o.getX() + 2, y, o.getZ() + i), Blocks.OAK_LOG, "the outer wall (:337-342)");
            }
        }
        BoundingBox window = templateBox(helper);
        helper.assertTrue(count(helper, window, Blocks.MOSSY_COBBLESTONE) > 0, "no step blocks (:356-380)");
        helper.assertTrue(count(helper, window, Blocks.OAK_LEAVES) > 0, "no branch leaves (:173-224)");
        boolean apex = false;
        for (int y = o.getY(); y < o.getY() + 30; y++) {
            if (helper.getLevel().getBlockState(o.atY(y)).is(Blocks.EMERALD_BLOCK)
                    && helper.getLevel().getBlockState(o.atY(y + 1)).is(Blocks.EMERALD_BLOCK)) apex = true;
        }
        helper.assertTrue(apex, "no two-emerald apex on the trunk column (:443-446)");
        helper.succeed();
    }

    /** orig ItemMagicApple.java:533-623: the circular tree's first ring at the original's (int)(v + 0.5) rounding, which lands the -x and -z cells one block nearer the centre; its step platforms. */
    @GameTest(template = "empty_tall")
    public static void w074b_circular_tree_geometry(GameTestHelper helper) {
        BlockPos o = helper.absolutePos(new BlockPos(24, 2, 24));
        build(helper, UtopiaTreePiece.circular(o, 3L, 2, 1, true), templateBox(helper));
        assertIs(helper, new BlockPos(o.getX(), o.getY() + 1, o.getZ() + 2), Blocks.SPRUCE_LOG, "the ring at 0 degrees (:561-570)");
        assertIs(helper, new BlockPos(o.getX() + 2, o.getY() + 1, o.getZ()), Blocks.SPRUCE_LOG, "the ring at 90 degrees");
        assertIs(helper, new BlockPos(o.getX(), o.getY() + 1, o.getZ() - 1), Blocks.SPRUCE_LOG, "the ring at 180 degrees, (int)(-1.5) = -1");
        assertIs(helper, new BlockPos(o.getX() - 1, o.getY() + 1, o.getZ()), Blocks.SPRUCE_LOG, "the ring at 270 degrees, (int)(-1.5) = -1");
        helper.assertTrue(count(helper, templateBox(helper), Blocks.MOSSY_COBBLESTONE) > 0, "no step platforms (:571-580)");
        helper.succeed();
    }

    /**
     * orig ItemMagicApple.java:160-171 and :394-398: a tree that carries critters puts chests with the magic apple
     * list on its branches and floors and Iron Golems on its branches. The seeds are fixed, the tree's reach is
     * clipped to the template; over the first seeds at least one golem and one chest must land inside it.
     */
    @GameTest(template = "empty_tall")
    public static void w074c_critter_trees_carry_chests_and_golems(GameTestHelper helper) {
        BlockPos o = helper.absolutePos(new BlockPos(24, 2, 24));
        BoundingBox window = templateBox(helper);
        boolean golem = false, chest = false;
        for (long seed = 1; seed <= 40 && !(golem && chest); seed++) {
            build(helper, UtopiaTreePiece.square(o, seed, 5, 0, false, false), window);
            if (!helper.getLevel().getEntitiesOfClass(IronGolem.class, AABB.of(window)).isEmpty()) golem = true;
            for (BlockPos p : BlockPos.betweenClosed(window.minX(), window.minY(), window.minZ(),
                    window.maxX(), window.maxY(), window.maxZ())) {
                if (helper.getLevel().getBlockEntity(p) instanceof RandomizableContainerBlockEntity container
                        && container.getLootTable() != null
                        && container.getLootTable().location().getPath().startsWith("chests/utopia_huge_tree")) {
                    chest = true;
                    break;
                }
            }
        }
        helper.assertTrue(golem, "no Iron Golem on the branches of forty critter trees (:169-171)");
        helper.assertTrue(chest, "no magic-apple chest in forty critter trees (:161-167, :394-398)");
        helper.succeed();
    }

    /** A random seeded the way the structure pass seeds a structure's context ({@code GenerationContext.makeRandom}). */
    private static WorldgenRandom chunkRandom(long seed, ChunkPos chunk) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, chunk.x, chunk.z);
        return random;
    }

    /**
     * WGEN-075 (GitHub issue #4): the huge and royal trees generate in top_layer_modification, after the vegetation step's
     * trees and plants and after the groves, so they overwrite them where they build (1.7.10 ran OreSpawnWorld after the
     * chunk's own decoration); the grove stays in the vegetation step.
     */
    @GameTest(template = "empty")
    public static void w075a_big_trees_generate_after_the_vegetation(GameTestHelper helper) {
        Registry<Structure> structures = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (String id : new String[] {"orespawn:royal_tree_king", "orespawn:royal_tree_queen", "orespawn:utopia_huge_tree"}) {
            Structure s = structures.get(ResourceLocation.parse(id));
            helper.assertTrue(s != null && s.step() == GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                    id + " must generate in top_layer_modification, after the vegetation and the groves");
        }
        Structure grove = structures.get(ResourceLocation.parse("orespawn:utopia_tree_grove"));
        helper.assertTrue(grove != null && grove.step() == GenerationStep.Decoration.VEGETAL_DECORATION,
                "the grove must stay in vegetal_decoration, before the big trees");
        helper.succeed();
    }

    /**
     * WGEN-075 / WGEN-077: the chunk pass over 60,000 chunks with every column grass at Y 70 (orig OreSpawnWorld.java:
     * 42-46). A chunk grows a huge tree, apple trees or a grove, never two of them; the huge pass keeps the huge
     * structure's own draws; and the three rolls are as independent as the original's consecutive draws: the grove
     * count sits where one in thirty of the chunks with neither a huge tree nor apple trees puts it. (Two rolls that
     * each began on a fresh copy of the chunk's random would share their first draw: the huge gate would then take a
     * fifth of the grove chunks, not a fiftieth, and the count would fall about a sixth short.)
     */
    @GameTest(template = "empty")
    public static void w075b_the_chunk_pass_grows_one_kind_of_tree(GameTestHelper helper) {
        UtopiaTreeStructure.ColumnProbe flat = (x, z, low, high) -> 70;
        long seed = 20260926L;
        int hugeChunks = 0;
        int appleChunks = 0;
        int groveChunks = 0;
        double groveExpected = 0;
        double appleExpected = 0;
        for (int cx = -300; cx < 300; cx++) {
            for (int cz = -50; cz < 50; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                UtopiaTreeStructure.ChunkPass pass = UtopiaTreeStructure.chunkPass(seed, chunk, flat);
                boolean huge = UtopiaTreeStructure.hugeRoll(chunkRandom(seed, chunk), chunk, flat).madeOne();
                helper.assertTrue(pass.bigTree() == huge, "the pass and the huge structure disagree at " + chunk);
                int kinds = (pass.bigTree() ? 1 : 0) + (pass.appleTrees().isEmpty() ? 0 : 1) + (pass.grove().isEmpty() ? 0 : 1);
                helper.assertTrue(kinds <= 1, "the pass grew " + kinds + " kinds of tree in " + chunk + ": " + pass);
                if (huge) hugeChunks++;
                if (!pass.appleTrees().isEmpty()) appleChunks++;
                if (!pass.grove().isEmpty()) groveChunks++;
                if (!huge) {
                    double apple = 1.0 / (15 + (Math.abs(cx) + Math.abs(cz)) % 15);
                    appleExpected += apple;
                    groveExpected += (1 - apple) / 30.0;
                }
            }
        }
        helper.assertTrue(Math.abs(hugeChunks - 1188) < 170, hugeChunks + " huge-tree chunks in 60,000 (about 1188 expected)");
        helper.assertTrue(Math.abs(appleChunks - appleExpected) < 5 * Math.sqrt(appleExpected),
                appleChunks + " apple-tree chunks in 60,000 (" + Math.round(appleExpected) + " expected)");
        helper.assertTrue(Math.abs(groveChunks - groveExpected) < 5 * Math.sqrt(groveExpected),
                groveChunks + " grove chunks in 60,000 (" + Math.round(groveExpected) + " expected)");
        helper.succeed();
    }

    /**
     * WGEN-080: the King and Queen trees are the huge roll's royal branch (orig OreSpawnWorld.java:1855-1866): with
     * grass in every column, one chunk in 5,000 (the one-in-fifty gate, then the type roll's 0), King or Queen at even
     * odds, the tree on the grass 4 to 11 blocks into the chunk; the roll grows no other tree there, and the rest of
     * the chunk's pass grows nothing.
     */
    @GameTest(template = "empty")
    public static void w075c_the_royal_trees_are_the_huge_roll_s_branch(GameTestHelper helper) {
        UtopiaTreeStructure.ColumnProbe flat = (x, z, low, high) -> 70;
        long seed = 20260926L;
        int royals = 0;
        int queens = 0;
        int chunks = 0;
        for (int cx = -200; cx < 200; cx++) {
            for (int cz = -200; cz < 200; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                chunks++;
                UtopiaTreeStructure.HugeRoll roll = UtopiaTreeStructure.hugeRoll(chunkRandom(seed, chunk), chunk, flat);
                if (roll.royal() == null) continue;
                royals++;
                if (roll.queen()) queens++;
                int dx = roll.royal().getX() - chunk.getMinBlockX();
                int dz = roll.royal().getZ() - chunk.getMinBlockZ();
                helper.assertTrue(roll.trees().isEmpty() && dx >= 4 && dx <= 11 && dz >= 4 && dz <= 11
                        && roll.royal().getY() == 70, "the royal branch at " + chunk + " grew " + roll);
                UtopiaTreeStructure.ChunkPass pass = UtopiaTreeStructure.chunkPass(seed, chunk, flat);
                helper.assertTrue(pass.bigTree() && pass.appleTrees().isEmpty() && pass.grove().isEmpty()
                        && pass.altar() == null, "the pass grew " + pass + " in " + chunk + ", where a royal tree grows");
            }
        }
        double expected = chunks / 5000.0;
        helper.assertTrue(Math.abs(royals - expected) < 5 * Math.sqrt(expected), royals + " royal trees in " + chunks
                + " chunks (" + Math.round(expected) + " expected)");
        helper.assertTrue(queens > 0 && queens < royals, queens + " of the " + royals + " royal trees are Queens");
        helper.succeed();
    }

    /**
     * WGEN-075: the magic apple tree and the veggie patch stay out of a chunk where one of addHugeTree's trees is rooted
     * (orig OreSpawnWorld.java:42-47), read from the chunk's structure starts: a royal tree's start is put in the test
     * chunk, then taken out again.
     */
    @GameTest(template = "empty")
    public static void w075d_no_apple_tree_or_veggies_under_a_big_tree(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        LevelChunk chunk = level.getChunkAt(pos);
        Structure king = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.parse("orespawn:royal_tree_king"));
        helper.assertTrue(king != null, "orespawn:royal_tree_king is not registered");
        Map<Structure, StructureStart> saved = new HashMap<>(chunk.getAllStarts());
        try {
            helper.assertTrue(!UtopiaTreeStructure.bigTreeRootedAt(level, pos), "a big tree is already rooted in the test chunk");
            chunk.setStartForStructure(king, new StructureStart(king, chunk.getPos(), 0,
                    new PiecesContainer(List.of(new RoyalTreePiece(pos, false)))));
            helper.assertTrue(UtopiaTreeStructure.bigTreeRootedAt(level, pos), "the royal tree's start went unseen");
            FeaturePlaceContext<NoneFeatureConfiguration> ctx = new FeaturePlaceContext<>(Optional.empty(), level,
                    level.getChunkSource().getGenerator(), RandomSource.create(1L), pos, NoneFeatureConfiguration.INSTANCE);
            helper.assertTrue(!ModFeatures.MAGIC_APPLE_TREE.get().place(ctx), "a magic apple tree grew under a big tree");
            helper.assertTrue(!ModFeatures.VEGGIE_PATCH.get().place(ctx), "a veggie patch grew under a big tree");
        } finally {
            chunk.setAllStarts(saved);
        }
        helper.assertTrue(!UtopiaTreeStructure.bigTreeRootedAt(level, pos), "the test chunk kept the royal tree's start");
        helper.succeed();
    }

    /**
     * WGEN-077: the apple trees' roll (orig OreSpawnWorld.java:1792-1828): a chunk that grows them grows
     * {@code 2 + nextInt(2 + (15 - freq) / 2)} of them, each {@code 2 + nextInt(12)} into the chunk on the grass the
     * scan finds, and a column the scan refuses grows none; the gate's odds follow {@code freq}, one in 15 where it is
     * 0 and one in 29 where it is 14.
     */
    @GameTest(template = "empty")
    public static void w077a_the_apple_trees_roll(GameTestHelper helper) {
        UtopiaTreeStructure.ColumnProbe flat = (x, z, low, high) -> 70;
        UtopiaTreeStructure.ColumnProbe none = (x, z, low, high) -> Integer.MIN_VALUE;
        long seed = 20260927L;
        int[] chunks = new int[15];
        int[] grew = new int[15];
        for (int cx = 0; cx < 400; cx++) {
            for (int cz = 0; cz < 150; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                int freq = (cx + cz) % 15;
                List<BlockPos> trees = UtopiaTreeStructure.appleTrees(chunkRandom(seed, chunk), chunk, flat);
                chunks[freq]++;
                if (trees.isEmpty()) continue;
                grew[freq]++;
                int most = 3 + (15 - freq) / 2;
                helper.assertTrue(trees.size() >= 2 && trees.size() <= most, trees.size() + " apple trees in " + chunk
                        + " (2 to " + most + " expected at freq " + freq + ")");
                for (BlockPos tree : trees) {
                    int dx = tree.getX() - chunk.getMinBlockX();
                    int dz = tree.getZ() - chunk.getMinBlockZ();
                    helper.assertTrue(dx >= 2 && dx <= 13 && dz >= 2 && dz <= 13 && tree.getY() == 70,
                            "an apple tree at " + tree + " is outside 2..13 of " + chunk + " or off the grass");
                }
                helper.assertTrue(UtopiaTreeStructure.appleTrees(chunkRandom(seed, chunk), chunk, none).isEmpty(),
                        "apple trees grew in " + chunk + " where no column has grass in the window");
            }
        }
        double near = (double) grew[0] / chunks[0];
        double far = (double) grew[14] / chunks[14];
        helper.assertTrue(Math.abs(near - 1 / 15.0) < 0.012, "freq 0 grew apple trees in " + near + " of its chunks (1/15 expected)");
        helper.assertTrue(Math.abs(far - 1 / 29.0) < 0.008, "freq 14 grew apple trees in " + far + " of its chunks (1/29 expected)");
        helper.succeed();
    }

    /**
     * WGEN-075 / WGEN-077 on the Utopia dimension's generator: the grove structure grows exactly the chunk pass's
     * grove, and the King altar refuses every chunk where the pass grows a tree (orig OreSpawnWorld.java:42-45). Where
     * an altar does build, and the royal trees, are StructurePlacementTests' w080e.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void w077b_the_utopia_structures_read_one_chunk_pass(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Holder<Biome> plains = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("orespawn:utopia_plains")));
        ResourceKey<NoiseGeneratorSettings> inland = ResourceKey.create(Registries.NOISE_SETTINGS,
                ResourceLocation.parse("orespawn:inland"));
        ChunkGenerator generator = new OreSpawnChunkGenerator(new FixedBiomeSource(plains),
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(inland), DimensionStyle.UTOPIA);
        long seed = 8780444890188216456L;
        RandomState randomState = RandomState.create(server.registryAccess().asGetterLookup(), inland, seed);
        Registry<Structure> structures = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure grove = structures.get(ResourceLocation.parse("orespawn:utopia_tree_grove"));
        Structure altar = structures.get(ResourceLocation.parse("orespawn:king_altar"));
        helper.assertTrue(grove != null && altar != null, "a Utopia structure is not registered");
        int groves = 0;
        int refused = 0;
        for (int cx = 0; cx < 40; cx++) {
            for (int cz = 0; cz < 40; cz++) {
                Structure.GenerationContext ctx = utopiaContext(helper, generator, randomState, seed, new ChunkPos(cx + 3000, cz - 1700));
                UtopiaTreeStructure.ChunkPass pass = UtopiaTreeStructure.chunkPass(ctx.seed(), ctx.chunkPos(),
                        UtopiaTreeStructure.probe(ctx));
                Optional<Structure.GenerationStub> g = grove.findValidGenerationPoint(ctx);
                helper.assertTrue(g.isPresent() == !pass.grove().isEmpty(), "the grove structure and the pass disagree at "
                        + ctx.chunkPos());
                if (g.isPresent()) {
                    groves++;
                    helper.assertTrue(g.get().position().equals(pass.grove().get(0).origin()),
                            "the grove structure's first tree is not the pass's at " + ctx.chunkPos());
                }
                if (pass.grewTrees()) {
                    helper.assertTrue(altar.findValidGenerationPoint(ctx).isEmpty(), "a King altar stands in "
                            + ctx.chunkPos() + ", where the pass grew " + pass);
                    refused++;
                }
            }
        }
        helper.assertTrue(groves > 0 && refused > 0, "the patch left a case untested: " + groves + " groves, "
                + refused + " refused altars");
        helper.succeed();
    }

    /**
     * A structure context on a detached Utopia generator (data/orespawn/dimension/utopia.json: the fixed Utopia plains
     * biome, the inland noise settings, the Utopia style), as the structure pass builds one for {@code chunk}. The test
     * level serves as the height accessor, which only clamps {@code getBaseHeight} to the generator's own range.
     */
    private static Structure.GenerationContext utopiaContext(GameTestHelper helper, ChunkGenerator generator,
                                                             RandomState randomState, long seed, ChunkPos chunk) {
        ServerLevel level = helper.getLevel();
        return new Structure.GenerationContext(level.getServer().registryAccess(), generator, generator.getBiomeSource(),
                randomState, level.getStructureManager(), seed, chunk, level, biome -> true);
    }
}
