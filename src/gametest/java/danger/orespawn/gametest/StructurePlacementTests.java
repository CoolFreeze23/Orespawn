package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.OreSpawnChunkGenerator;
import danger.orespawn.world.structure.FeatureStructure;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType.PlacementMode;
import danger.orespawn.world.structure.RoyalTreeStructure;
import danger.orespawn.world.structure.UtopiaTreeStructure;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * WGEN-079: every dimension's structure placement against 1.7.10's OreSpawnWorld (the Islands' are WGEN-078's,
 * {@link IslandsRotationTests}; Crystal keeps its own cooldown counter, CrystalStructures).
 *
 * <p>The original's cooldown (orig OreSpawnWorld.java:30, 37-38): {@code recently_placed} counts down once per
 * populated chunk; a builder that builds sets it to 50 (the King altar 100); the rolls behind it run only while it is
 * 0. In a dimension whose builders build with probability p per chunk, the rolls run in 1 / (1 + 49p) of the chunks.
 * The overworld's builders are biome-bound, so p is taken per biome from the builders that share it; Mining's and the
 * Village's builders' site success was measured on the port's own generators (400 chunks each).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructurePlacementTests {

    private static double factor(double buildsPerChunk, int blocked) {
        return 1 / (1 + blocked * buildsPerChunk);
    }

    // the overworld pass (orig OreSpawnWorld.java:284-321): world.rand.nextInt(6) picks one of six builders, then the
    // first-success chain of seven; every builder that builds sets the cooldown
    private static final double F_OCEAN = factor(3.0 / (6 * 350) + 2.0 / (6 * 300), 49);
    private static final double F_PLAINS = factor(1.0 / (6 * 350) + 1.0 / 285 + 1.0 / 275 + 1.0 / 275, 49);
    private static final double F_TAIGA = factor(1.0 / 285, 49);
    private static final double F_SWAMP = factor(1.0 / 285 + 1.0 / 190, 49);
    private static final double F_FOREST = factor(1.0 / 230, 49);
    private static final double F_SNOWY = factor(1.0 / 220, 49);
    private static final double F_DESERT = factor(1.0 / 230, 49);
    /** The haunted house stands in plains, taiga and swamp; the cooldown weighted by their share of its ground. */
    private static final double F_HAUNTED = 0.5 * F_PLAINS + 0.3 * F_TAIGA + 0.2 * F_SWAMP;
    // Mining (:79-104): nextInt(95) == 1, then nextInt(7); six of the seven builders find their lowest-surface or
    // lowest-grass site in 89% of chunks, the Leonopteryx nest its grass above Y80 in 34%
    private static final double F_MINING = factor((6 * 0.8925 + 0.3375) / (95 * 7), 49);
    // the Village (:120-129): the damsel 1/250 (58% of chunks have its site), the spider hangout 1/350 (49%), the
    // red ant hangout 1/250 (53%)
    private static final double F_VILLAGE = factor(0.5825 / 250 + 0.49 / 350 + 0.525 / 250, 49);
    // the King altar's own cooldown of 100 (:2566): 1/2000, its site in 64% of chunks, a tree-free chunk in about 90%
    private static final double F_ALTAR = factor(0.64 * 0.9 / 2000, 99);

    /** A structure set: its spacing and separation, the original's chunks per structure before the cooldown, the factor. */
    private record Row(String set, int spacing, int separation, double chunks, double cooldown) {}

    private static final List<Row> ROWS = List.of(
            new Row("play_pool", 49, 8, 6 * 350, F_OCEAN),
            new Row("water_dragon_lair", 49, 8, 6 * 350, F_OCEAN),
            new Row("gold_fish_bowl", 49, 8, 6 * 350, F_OCEAN),
            new Row("girlfriend_island", 45, 8, 6 * 300, F_OCEAN),
            new Row("monster_island", 45, 8, 6 * 300, F_OCEAN),
            new Row("frog_pond", 57, 8, 6 * 350, F_PLAINS),
            new Row("nests", 17, 4, 230, F_FOREST),
            new Row("haunted_house", 20, 5, 285, F_HAUNTED),
            new Row("leaf_monster_dungeon", 21, 5, 275, F_PLAINS),
            new Row("spit_bug_lair", 16, 4, 190, F_SWAMP),
            new Row("igloo", 16, 4, 220, F_SNOWY),
            new Row("bouncy_castle", 17, 4, 230, F_DESERT),
            new Row("rubber_ducky_pond", 21, 5, 275, F_PLAINS),
            new Row("basilisk_maze", 31, 8, 95 * 7, F_MINING),
            new Row("kyuubi_dungeon", 31, 8, 95 * 7, F_MINING),
            new Row("beehive", 31, 8, 95 * 7, F_MINING),
            new Row("shadow_dungeon", 31, 8, 95 * 7, F_MINING),
            new Row("wtf_alien_dungeon", 31, 8, 95 * 7, F_MINING),
            new Row("ender_knight_dungeon_mining", 31, 8, 95 * 7, F_MINING),
            new Row("leonopteryx_nest", 31, 8, 95 * 7, F_MINING),
            new Row("damsel_in_distress", 18, 5, 250, F_VILLAGE),
            new Row("spider_hangout", 21, 5, 350, F_VILLAGE),
            new Row("red_ant_hangout", 18, 5, 250, F_VILLAGE),
            new Row("ender_knight_dungeon_end", 10, 3, 4 * 25, 1),
            new Row("graveyard", 10, 3, 4 * 25, 1),
            new Row("hospital", 10, 3, 4 * 25, 1),
            new Row("ender_castle_end", 14, 4, 4 * 50, 1),
            new Row("cloud_shark_dungeon", 17, 4, 300, 1),
            new Row("royal_trees", 71, 8, 50 * 100, 1),
            new Row("royal_altars", 45, 8, 2000, F_ALTAR));

    private static RandomSpreadStructurePlacement placement(GameTestHelper helper, String set) {
        StructureSet s = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceLocation.fromNamespaceAndPath("orespawn", set));
        helper.assertTrue(s != null && s.placement() instanceof RandomSpreadStructurePlacement,
                "orespawn:" + set + " must be a random_spread structure set");
        return (RandomSpreadStructurePlacement) s.placement();
    }

    /** Every set's cell is the original's chunks per structure once the cooldown is counted, within rounding. */
    @GameTest(template = "empty")
    public static void w079a_every_set_at_the_original_rate(GameTestHelper helper) {
        for (Row row : ROWS) {
            RandomSpreadStructurePlacement p = placement(helper, row.set());
            helper.assertTrue(p.spacing() == row.spacing() && p.separation() == row.separation(), "orespawn:" + row.set()
                    + " is " + p.spacing() + "/" + p.separation() + ", expected " + row.spacing() + "/" + row.separation());
            double target = row.chunks() / row.cooldown();
            double cell = (double) row.spacing() * row.spacing();
            helper.assertTrue(Math.abs(cell - target) / target < 0.07, "orespawn:" + row.set() + "'s cell of " + cell
                    + " chunks is off the original's one structure in " + Math.round(target) + " chunks");
        }
        helper.succeed();
    }

    /**
     * addANest (orig OreSpawnWorld.java:999-1021) builds a small bee hive or a mantis hive at even odds (:1010), in
     * exactly "Forest", "ForestHills", "Birch Forest", "Birch Forest Hills", "Jungle" and "JungleHills" (:1004): one
     * set for both, on the 1.21 forest, birch forest and jungle; the two old one-hive sets are gone.
     */
    @GameTest(template = "empty")
    public static void w079b_the_nests_share_one_set_on_the_original_biomes(GameTestHelper helper) {
        Registry<StructureSet> sets = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        StructureSet nests = sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", "nests"));
        helper.assertTrue(nests != null && nests.structures().size() == 2
                        && nests.structures().stream().allMatch(e -> e.weight() == 1),
                "orespawn:nests must hold the small bee hive and the mantis hive at even odds");
        for (String old : new String[] {"small_beehive", "mantis_nest"}) {
            helper.assertTrue(sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", old)) == null,
                    "the old one-hive set orespawn:" + old + " is still registered");
            Structure hive = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .get(ResourceLocation.fromNamespaceAndPath("orespawn", old));
            Set<String> biomes = hive.biomes().stream().map(h -> h.unwrapKey().orElseThrow().location().toString())
                    .collect(Collectors.toSet());
            helper.assertTrue(biomes.equals(Set.of("minecraft:forest", "minecraft:birch_forest", "minecraft:jungle")),
                    "orespawn:" + old + " must stand in the forest, birch forest and jungle only, not " + biomes);
            helper.assertTrue(hive instanceof FeatureStructure f && f.anchor().equals("grass_attempts_5")
                    && f.overworldDungeon(), "orespawn:" + old + " must take addANest's five grass attempts and honour"
                    + " DisableOverworldDungeons");
        }
        helper.succeed();
    }

    /**
     * The Village dimension's villages (MapGenMoreVillages, spacing 9, separation 7, salt 10387312): the port's set
     * picks exactly the chunks 1.7.10 picks for the same seed, compared with the original's own test, over a patch.
     */
    @GameTest(template = "empty")
    public static void w079c_the_villages_stand_where_1_7_10_put_them(GameTestHelper helper) {
        RandomSpreadStructurePlacement p = placement(helper, "dim_villages");
        helper.assertTrue(p.spacing() == 9 && p.separation() == 7, "orespawn:dim_villages must be 9/7 (MapGenMoreVillages:11-12)");
        long seed = helper.getLevel().getSeed();
        int villages = 0;
        for (int cx = -120; cx < 120; cx++) {
            for (int cz = -120; cz < 120; cz++) {
                // orig MapGenMoreVillages.java:14-33, func_75047_a with World.setRandomSeed(x, z, 10387312)
                int x = cx < 0 ? cx - 8 : cx;
                int z = cz < 0 ? cz - 8 : cz;
                int cellX = x / 9;
                int cellZ = z / 9;
                Random r = new Random((long) cellX * 341873128712L + (long) cellZ * 132897987541L + seed + 10387312L);
                boolean original = cx == cellX * 9 + r.nextInt(2) && cz == cellZ * 9 + r.nextInt(2);
                ChunkPos ours = p.getPotentialStructureChunk(seed, cx, cz);
                helper.assertTrue(original == (ours.x == cx && ours.z == cz), "the villages' set and 1.7.10 disagree at "
                        + new ChunkPos(cx, cz));
                if (original) villages++;
            }
        }
        helper.assertTrue(villages > 400, "only " + villages + " village chunks in the patch");
        helper.succeed();
    }

    /**
     * No two sets of one dimension that share a spacing lay their starts out in one pattern: the most common offset
     * between their starts stays rare over 3,600 cells (sets on close salts repeated one offset cell after cell).
     */
    @GameTest(template = "empty")
    public static void w079d_no_shared_patterns_within_a_dimension(GameTestHelper helper) {
        String[][] groups = {
                {"play_pool", "water_dragon_lair", "gold_fish_bowl"}, {"girlfriend_island", "monster_island"},
                {"nests", "bouncy_castle"}, {"leaf_monster_dungeon", "rubber_ducky_pond"}, {"spit_bug_lair", "igloo"},
                {"basilisk_maze", "kyuubi_dungeon", "beehive", "shadow_dungeon", "wtf_alien_dungeon",
                        "ender_knight_dungeon_mining", "leonopteryx_nest"},
                {"damsel_in_distress", "red_ant_hangout"}, {"ender_knight_dungeon_end", "graveyard", "hospital"}};
        long seed = helper.getLevel().getSeed();
        for (String[] group : groups) {
            for (int a = 0; a < group.length; a++) {
                for (int b = a + 1; b < group.length; b++) {
                    RandomSpreadStructurePlacement pa = placement(helper, group[a]);
                    RandomSpreadStructurePlacement pb = placement(helper, group[b]);
                    int spacing = pa.spacing();
                    helper.assertTrue(pb.spacing() == spacing, group[a] + " and " + group[b] + " no longer share a spacing");
                    int range = spacing - pa.separation();
                    Map<Long, Integer> offsets = new HashMap<>();
                    int most = 0;
                    for (int i = -30; i < 30; i++) {
                        for (int j = -30; j < 30; j++) {
                            ChunkPos ca = pa.getPotentialStructureChunk(seed, i * spacing, j * spacing);
                            ChunkPos cb = pb.getPotentialStructureChunk(seed, i * spacing, j * spacing);
                            long key = ((long) (cb.x - ca.x) << 32) ^ (cb.z - ca.z & 0xFFFFFFFFL);
                            most = Math.max(most, offsets.merge(key, 1, Integer::sum));
                        }
                    }
                    double limit = 4.0 / ((double) range * range) + 0.005;
                    helper.assertTrue(most / 3600.0 < limit, "orespawn:" + group[a] + " and orespawn:" + group[b]
                            + " share an offset in " + most + " of 3,600 cells");
                }
            }
        }
        helper.succeed();
    }

    /**
     * The anchors the original's builders used: the Islands' greenhouse and white house on the D4 grass scan, Mining's
     * shadow and WTF alien dungeons (and, through its feature structure, the bee hive) on the lowest grass of the 6×6
     * grid, the Leonopteryx nest on its highest grass, the royal altars on addKingAltar's eight attempts.
     */
    @GameTest(template = "empty")
    public static void w079e_the_structures_take_the_original_sites(GameTestHelper helper) {
        helper.assertTrue(DungeonType.GREENHOUSE.placement == PlacementMode.ISLANDS_GRASS
                && DungeonType.WHITE_HOUSE.placement == PlacementMode.ISLANDS_GRASS, "the Islands' greenhouse and white"
                + " house must take the D4 grass anchor (orig OreSpawnWorld.java:2230-2251, :2299-2320)");
        helper.assertTrue(DungeonType.SHADOW.placement == PlacementMode.LOWEST_GRASS_36
                && DungeonType.ALIEN_WTF.placement == PlacementMode.LOWEST_GRASS_36, "the shadow and WTF alien dungeons"
                + " must take the lowest grass of the 6x6 grid (:2143-2169, :2059-2085)");
        helper.assertTrue(DungeonType.LEONOPTERYX_NEST.placement == PlacementMode.HIGHEST_GRASS_36,
                "the Leonopteryx nest must take its highest grass (:2115-2141)");
        helper.assertTrue(DungeonType.KING_ALTAR.placement == PlacementMode.UTOPIA_ALTAR
                && DungeonType.QUEEN_ALTAR.placement == PlacementMode.UTOPIA_ALTAR, "the royal altars must take"
                + " addKingAltar's eight attempts (:2549-2571)");
        Structure beehive = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath("orespawn", "beehive"));
        helper.assertTrue(beehive instanceof FeatureStructure f && f.anchor().equals("lowest_grass_36"),
                "the bee hive must take addBeeHive's lowest grass (:2031-2057)");
        helper.succeed();
    }

    private static Structure.GenerationContext context(GameTestHelper helper, ChunkGenerator gen, RandomState state,
                                                       long seed, ChunkPos chunk) {
        ServerLevel level = helper.getLevel();
        return new Structure.GenerationContext(level.getServer().registryAccess(), gen, gen.getBiomeSource(), state,
                level.getStructureManager(), seed, chunk, level, biome -> true);
    }

    private static Structure structure(GameTestHelper helper, String id) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath("orespawn", id));
    }

    private static int surface(ChunkGenerator gen, GameTestHelper helper, RandomState state, int x, int z,
                               Heightmap.Types type) {
        return gen.getBaseHeight(x, z, type, helper.getLevel(), state);
    }

    /**
     * On a detached Mining generator (data/orespawn/dimension/mining.json): the Leonopteryx nest's site is exactly
     * addLeonNest's (orig OreSpawnWorld.java:2115-2141: the 6×6 grid, grass inside Y81-128, a column replacing the kept
     * one only when its grass is above the kept anchor, the anchor one above the grass, none without a column), and the
     * bee hive stands on the same lowest grass the ender knight dungeon's LOWEST_GRASS_36 scan finds.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void w079f_mining_sites_replay_the_original_scans(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Holder<Biome> biome = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("orespawn:mining_biome")));
        ResourceKey<NoiseGeneratorSettings> inland = ResourceKey.create(Registries.NOISE_SETTINGS,
                ResourceLocation.parse("orespawn:inland"));
        ChunkGenerator gen = new OreSpawnChunkGenerator(new FixedBiomeSource(biome),
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(inland), DimensionStyle.MINING);
        long seed = 8780444890188216456L;
        RandomState state = RandomState.create(server.registryAccess().asGetterLookup(), inland, seed);
        Structure leon = structure(helper, "leonopteryx_nest");
        Structure hive = structure(helper, "beehive");
        Structure knight = structure(helper, "ender_knight_dungeon_mining");
        int nests = 0;
        int refused = 0;
        for (int c = 0; c < 120; c++) {
            ChunkPos chunk = new ChunkPos(700 + c * 5, -300 + c * 3);
            int highestY = 30;
            BlockPos expected = null;
            for (int i = 0; i < 16; i += 3) {
                for (int j = 0; j < 16; j += 3) {
                    int x = chunk.getMinBlockX() + i;
                    int z = chunk.getMinBlockZ() + j;
                    int grass = surface(gen, helper, state, x, z, Heightmap.Types.WORLD_SURFACE_WG) - 1;
                    if (grass > 128 || grass <= 80 || grass <= highestY) continue;
                    highestY = grass + 1;
                    expected = new BlockPos(x, highestY, z);
                }
            }
            Optional<Structure.GenerationStub> stub = leon.findValidGenerationPoint(context(helper, gen, state, seed, chunk));
            helper.assertTrue(stub.map(Structure.GenerationStub::position).orElse(null) == null ? expected == null
                            : stub.get().position().equals(expected),
                    "the Leonopteryx nest's site at " + chunk + " is " + stub.map(Structure.GenerationStub::position)
                            + ", addLeonNest's is " + expected);
            if (expected != null) nests++; else refused++;
            Optional<Structure.GenerationStub> h = hive.findValidGenerationPoint(context(helper, gen, state, seed, chunk));
            Optional<Structure.GenerationStub> k = knight.findValidGenerationPoint(context(helper, gen, state, seed, chunk));
            helper.assertTrue(h.map(Structure.GenerationStub::position).equals(k.map(Structure.GenerationStub::position)),
                    "the bee hive's site at " + chunk + " is " + h.map(Structure.GenerationStub::position)
                            + ", the lowest grass is " + k.map(Structure.GenerationStub::position));
        }
        helper.assertTrue(nests > 0 && refused > 0, "the patch left a case untested: " + nests + " nests, " + refused + " refusals");
        helper.succeed();
    }

    /**
     * On a detached Utopia generator: the royal tree takes the huge roll's site (orig OreSpawnWorld.java:1841-1846:
     * three attempts at chunk + 4 + nextInt(8), grass inside Y51-127, built on the grass) and the King altar
     * addKingAltar's (:2553-2564: eight attempts at chunk + 3 + nextInt(10), grass inside Y51-100, built on the grass),
     * both drawn from the chunk random the structure pass gives them.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void w079g_utopia_sites_replay_the_original_scans(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Holder<Biome> plains = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("orespawn:utopia_plains")));
        ResourceKey<NoiseGeneratorSettings> inland = ResourceKey.create(Registries.NOISE_SETTINGS,
                ResourceLocation.parse("orespawn:inland"));
        ChunkGenerator gen = new OreSpawnChunkGenerator(new FixedBiomeSource(plains),
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(inland), DimensionStyle.UTOPIA);
        long seed = 8780444890188216456L;
        RandomState state = RandomState.create(server.registryAccess().asGetterLookup(), inland, seed);
        Structure king = structure(helper, "royal_tree_king");
        Structure altar = structure(helper, "king_altar");
        int trees = 0;
        int altars = 0;
        for (int c = 0; c < 150; c++) {
            ChunkPos chunk = new ChunkPos(-900 + c * 7, 400 + c * 2);
            Structure.GenerationContext ctx = context(helper, gen, state, seed, chunk);
            // the royal tree
            WorldgenRandom r = new WorldgenRandom(new LegacyRandomSource(0L));
            r.setLargeFeatureSeed(seed, chunk.x, chunk.z);
            BlockPos tree = null;
            for (int i = 0; i < 3 && tree == null; i++) {
                int x = 4 + chunk.getMinBlockX() + r.nextInt(8);
                int z = 4 + chunk.getMinBlockZ() + r.nextInt(8);
                int top = surface(gen, helper, state, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                int floor = surface(gen, helper, state, x, z, Heightmap.Types.OCEAN_FLOOR_WG);
                if (top == floor && top > 50 && top <= 127) tree = new BlockPos(x, top - 1, z);
            }
            Optional<Structure.GenerationStub> t = king.findValidGenerationPoint(ctx);
            helper.assertTrue(t.map(Structure.GenerationStub::position).equals(Optional.ofNullable(tree)),
                    "the King tree's site at " + chunk + " is " + t.map(Structure.GenerationStub::position)
                            + ", the huge roll's is " + tree);
            if (tree != null) trees++;
            // the altar, where the chunk's own pass grows no tree
            if (UtopiaTreeStructure.chunkPass(seed, chunk, UtopiaTreeStructure.probe(ctx),
                    () -> RoyalTreeStructure.startsIn(ctx)).grewTrees()) continue;
            WorldgenRandom a = new WorldgenRandom(new LegacyRandomSource(0L));
            a.setLargeFeatureSeed(seed, chunk.x, chunk.z);
            BlockPos site = null;
            for (int i = 0; i < 8 && site == null; i++) {
                int x = 3 + chunk.getMinBlockX() + a.nextInt(10);
                int z = 3 + chunk.getMinBlockZ() + a.nextInt(10);
                int top = surface(gen, helper, state, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                int floor = surface(gen, helper, state, x, z, Heightmap.Types.OCEAN_FLOOR_WG);
                if (top == floor && top > 50 && top <= 100) site = new BlockPos(x, top - 1, z);
            }
            Optional<Structure.GenerationStub> s = altar.findValidGenerationPoint(context(helper, gen, state, seed, chunk));
            helper.assertTrue(s.map(Structure.GenerationStub::position).equals(Optional.ofNullable(site)),
                    "the King altar's site at " + chunk + " is " + s.map(Structure.GenerationStub::position)
                            + ", addKingAltar's is " + site);
            if (site != null) altars++;
        }
        helper.assertTrue(trees > 0 && altars > 0, "the patch left a case untested: " + trees + " trees, " + altars + " altars");
        helper.succeed();
    }
}
