package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.feature.ModFeatures;
import danger.orespawn.world.structure.RoyalTreePiece;
import danger.orespawn.world.structure.RoyalTreeStructure;
import danger.orespawn.world.structure.UtopiaTreePiece;
import danger.orespawn.world.structure.UtopiaTreeStructure;
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
     * WGEN-075: no grove in a chunk where the huge roll grows a tree (orig OreSpawnWorld.java:42-43, addOtherTrees only
     * when addHugeTree grew nothing), and no other grove changed by the check. Both rolls are made on the random the
     * structure pass gives each of them (the same seed per chunk), with every column grass at Y 70.
     */
    @GameTest(template = "empty")
    public static void w075b_no_grove_where_a_huge_tree_grows(GameTestHelper helper) {
        UtopiaTreeStructure.ColumnProbe flat = (x, z, low, high) -> 70;
        long seed = 20260926L;
        int hugeChunks = 0;
        int groveChunks = 0;
        int refused = 0;
        for (int cx = -150; cx < 150; cx++) {
            for (int cz = -20; cz < 20; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                boolean huge = !UtopiaTreeStructure.huge(chunkRandom(seed, chunk), chunk, flat).isEmpty();
                boolean groveAlone = !UtopiaTreeStructure.grove(chunkRandom(seed, chunk), chunk, flat, () -> false).isEmpty();
                boolean grove = !UtopiaTreeStructure.grove(chunkRandom(seed, chunk), chunk, flat,
                        () -> !UtopiaTreeStructure.huge(chunkRandom(seed, chunk), chunk, flat).isEmpty()).isEmpty();
                if (huge) {
                    hugeChunks++;
                    helper.assertTrue(!grove, "a grove grew in " + chunk + ", where the huge roll grew a tree");
                    if (groveAlone) refused++;
                } else {
                    helper.assertTrue(grove == groveAlone, "the check changed the grove in " + chunk + ", which has no huge tree");
                }
                if (grove) groveChunks++;
            }
        }
        helper.assertTrue(hugeChunks > 100, "only " + hugeChunks + " huge-tree chunks in 12000 (one in fifty expected)");
        helper.assertTrue(groveChunks > 200, "only " + groveChunks + " grove chunks in 12000 (about one in thirty expected)");
        helper.assertTrue(refused > 0, "no huge-tree chunk also rolled a grove, so the refusal went untested");
        helper.succeed();
    }

    /**
     * WGEN-075: the royal_trees chunks answered as the structure pass answers them (the level's own structure state,
     * over a patch of chunks), and a grove refused wherever a big tree is rooted.
     */
    @GameTest(template = "empty")
    public static void w075c_royal_tree_chunks_match_the_structure_pass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        StructureSet set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(RoyalTreeStructure.ROYAL_TREES);
        helper.assertTrue(set != null && set.placement() instanceof RandomSpreadStructurePlacement,
                "orespawn:royal_trees must be a random_spread structure set");
        RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) set.placement();
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        int picked = 0;
        for (int cx = -400; cx < 400; cx++) {
            for (int cz = -400; cz < 400; cz += 3) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                boolean ours = RoyalTreeStructure.placementPicks(placement, state.getLevelSeed(), chunk);
                helper.assertTrue(ours == placement.isStructureChunk(state, cx, cz),
                        "the royal_trees placement disagrees with the structure pass at " + chunk);
                if (ours) picked++;
            }
        }
        helper.assertTrue(picked > 0, "no royal_trees chunk in the patch, so the comparison went untested");
        UtopiaTreeStructure.ColumnProbe flat = (x, z, low, high) -> 70;
        long seed = 20260926L;
        for (int cx = 0; cx < 200; cx++) {
            ChunkPos chunk = new ChunkPos(cx, 7);
            if (UtopiaTreeStructure.grove(chunkRandom(seed, chunk), chunk, flat, () -> false).isEmpty()) continue;
            helper.assertTrue(UtopiaTreeStructure.grove(chunkRandom(seed, chunk), chunk, flat, () -> true).isEmpty(),
                    "a grove grew in " + chunk + ", where a big tree is rooted");
            helper.succeed();
            return;
        }
        helper.fail("no grove chunk found to refuse");
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
}
