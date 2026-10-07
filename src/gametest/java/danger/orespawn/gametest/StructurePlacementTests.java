package danger.orespawn.gametest;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.OreSpawnChunkGenerator;
import danger.orespawn.world.structure.FeatureStructure;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType.PlacementMode;
import danger.orespawn.world.structure.LegacyDungeonStructure;
import danger.orespawn.world.structure.NothingStructure;
import danger.orespawn.world.structure.StructurePicks;
import danger.orespawn.world.structure.UtopiaTreeStructure;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * WGEN-079 / WGEN-080: every dimension's structure placement against 1.7.10's OreSpawnWorld (the Islands' roll is
 * {@link IslandsRotationTests}'; Crystal keeps its own cooldown counter, CrystalStructures).
 *
 * <p>The original rolled each chunk as it populated: one roll picked one builder, a builder that found no site built
 * nothing, and the {@code recently_placed} cooldown (orig OreSpawnWorld.java:30, 37-38) held every roll behind it for
 * the next 49 chunks after any build (the King altar's, 99). In a dimension whose builders build with probability p per
 * chunk, the rolls run in 1 / (1 + 49p) of the chunks. The overworld's builders are biome-bound, so p is taken per
 * biome from the builders that share it; Mining's and the Village's builders' site success was measured on the port's
 * own generators (400 chunks each). Each dimension's roll is one set now ({@link StructurePicks}): one spot for every
 * few chunks, each spot's structure picked at the original's odds with the cooldown's share counted, the rest of the
 * spots picking {@code orespawn:nothing}, and the pick final.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructurePlacementTests {

    private static double factor(double buildsPerChunk) {
        return 1 / (1 + 49 * buildsPerChunk);
    }

    // the overworld pass (orig OreSpawnWorld.java:284-321): world.rand.nextInt(6) picks one of six builders, then the
    // first-success chain of seven; every builder that builds sets the cooldown
    private static final double F_OCEAN = factor(3.0 / 2100 + 2.0 / 1800);
    private static final double F_PLAINS = factor(1.0 / 2100 + 1.0 / 285 + 2.0 / 275);
    private static final double F_TAIGA = factor(1.0 / 285);
    private static final double F_SWAMP = factor(1.0 / 285 + 1.0 / 190);
    private static final double F_FOREST = factor(1.0 / 230);
    private static final double F_SNOWY = factor(1.0 / 220);
    private static final double F_DESERT = factor(1.0 / 230);
    /** The haunted house stands in plains, taiga and swamp; the cooldown weighted by their share of its ground. */
    private static final double F_HAUNTED = 0.5 * F_PLAINS + 0.3 * F_TAIGA + 0.2 * F_SWAMP;
    // Mining (:79-104): nextInt(95) == 1, then nextInt(7); six of the seven builders find their lowest-surface or
    // lowest-grass site in 89% of chunks, the Leonopteryx nest its grass above Y80 in 34%
    private static final double F_MINING = factor((6 * 0.8925 + 0.3375) / 665);
    // the Village (:120-129): the damsel 1/250 (58% of chunks have its site), the spider hangout 1/350 (49%), the
    // red ant hangout 1/250 (53%)
    private static final double F_VILLAGE = factor(0.5825 / 250 + 0.49 / 350 + 0.525 / 250);
    // the End (:215-228) has no cooldown, but quickSpaceCheck (:2625-2633) turned a build away where an earlier
    // structure stood in its plane: 2.0% of them, simulated with the structures' footprints
    private static final double KEPT_END = 0.980;

    /** A one-pick set: its spacing and separation, and each structure's chunks per structure in the original. */
    private record OnePick(String set, int spacing, int separation, Map<String, Double> chunks) {}

    private static Map<String, Double> ordered(Object... pairs) {
        Map<String, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return map;
    }

    private static final List<OnePick> SETS = List.of(
            new OnePick("overworld_pool", 16, 1, ordered(
                    "play_pool", 2100 / F_OCEAN, "water_dragon_lair", 2100 / F_OCEAN, "gold_fish_bowl", 2100 / F_OCEAN,
                    "girlfriend_island", 1800 / F_OCEAN, "monster_island", 1800 / F_OCEAN, "frog_pond", 2100 / F_PLAINS)),
            new OnePick("overworld_chain", 6, 1, ordered(
                    "small_beehive", 460 / F_FOREST, "mantis_nest", 460 / F_FOREST, "haunted_house", 285 / F_HAUNTED,
                    "leaf_monster_dungeon", 275 / F_PLAINS, "spit_bug_lair", 190 / F_SWAMP, "igloo", 220 / F_SNOWY,
                    "bouncy_castle", 230 / F_DESERT, "rubber_ducky_pond", 275 / F_PLAINS)),
            new OnePick("mining_structures", 9, 0, ordered(
                    "basilisk_maze", 665 / F_MINING, "kyuubi_dungeon", 665 / F_MINING, "beehive", 665 / F_MINING,
                    "shadow_dungeon", 665 / F_MINING, "wtf_alien_dungeon", 665 / F_MINING,
                    "ender_knight_dungeon_mining", 665 / F_MINING, "leonopteryx_nest", 665 / F_MINING)),
            new OnePick("village_structures", 9, 0, ordered(
                    "damsel_in_distress", 250 / F_VILLAGE, "spider_hangout", 350 / F_VILLAGE,
                    "red_ant_hangout", 250 / F_VILLAGE)),
            new OnePick("end_structures", 2, 0, ordered(
                    "ender_knight_dungeon_end", 4 * 25 / KEPT_END, "graveyard", 4 * 25 / KEPT_END,
                    "hospital", 4 * 25 / KEPT_END, "ender_castle_end", 4 * 50 / KEPT_END)),
            new OnePick("cloud_shark_dungeon", 1, 0, ordered("cloud_shark_dungeon", 300)));

    /** The one-structure sets the one-pick sets replaced. */
    private static final String[] RETIRED = {"play_pool", "water_dragon_lair", "gold_fish_bowl", "girlfriend_island",
            "monster_island", "frog_pond", "nests", "small_beehive", "mantis_nest", "haunted_house",
            "leaf_monster_dungeon", "spit_bug_lair", "igloo", "bouncy_castle", "rubber_ducky_pond", "basilisk_maze",
            "kyuubi_dungeon", "beehive", "shadow_dungeon", "wtf_alien_dungeon", "ender_knight_dungeon_mining",
            "leonopteryx_nest", "damsel_in_distress", "spider_hangout", "red_ant_hangout", "ender_knight_dungeon_end",
            "graveyard", "hospital", "ender_castle_end"};

    private static StructureSet set(GameTestHelper helper, String name) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceLocation.fromNamespaceAndPath("orespawn", name));
    }

    private static String id(StructureSet.StructureSelectionEntry entry) {
        return entry.structure().unwrapKey().map(k -> k.location().toString()).orElse("?");
    }

    /**
     * Every one-pick set's spacing and separation, its structures in the original's order, and each one's odds: the
     * spacing cell over its weight's share is the original's chunks per structure with the cooldown counted, within
     * half a percent; the last entry is {@code orespawn:nothing}. The royal trees and altars ask every Utopia chunk and
     * leave the pick to the chunk's own pass; the one-structure sets these replaced are gone.
     */
    @GameTest(template = "empty")
    public static void w080a_every_roll_at_the_original_odds(GameTestHelper helper) {
        for (OnePick row : SETS) {
            StructureSet set = set(helper, row.set());
            helper.assertTrue(set != null && set.placement() instanceof RandomSpreadStructurePlacement,
                    "orespawn:" + row.set() + " must be a random_spread structure set");
            RandomSpreadStructurePlacement p = (RandomSpreadStructurePlacement) set.placement();
            helper.assertTrue(p.spacing() == row.spacing() && p.separation() == row.separation(), "orespawn:" + row.set()
                    + " is " + p.spacing() + "/" + p.separation() + ", expected " + row.spacing() + "/" + row.separation());
            List<StructureSet.StructureSelectionEntry> entries = set.structures();
            List<String> names = new ArrayList<>(row.chunks().keySet().stream().map(n -> "orespawn:" + n).toList());
            names.add("orespawn:nothing");
            helper.assertTrue(entries.stream().map(StructurePlacementTests::id).toList().equals(names),
                    "orespawn:" + row.set() + " holds " + entries.stream().map(StructurePlacementTests::id).toList());
            int total = entries.stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();
            for (int i = 0; i < row.chunks().size(); i++) {
                double original = row.chunks().get(names.get(i).substring("orespawn:".length()));
                double ours = (double) row.spacing() * row.spacing() * total / entries.get(i).weight();
                helper.assertTrue(Math.abs(ours - original) / original < 0.005, names.get(i) + " comes one chunk in "
                        + Math.round(ours) + " in orespawn:" + row.set() + ", the original's one in " + Math.round(original));
            }
            helper.assertTrue(StructurePicks.ONE_PICK_SETS.stream().anyMatch(k -> k.location().getPath().equals(row.set())),
                    "orespawn:" + row.set() + " is not one of the one-pick sets");
        }
        for (String name : new String[] {"royal_trees", "royal_altars"}) {
            StructureSet set = set(helper, name);
            helper.assertTrue(set != null && set.placement() instanceof RandomSpreadStructurePlacement p
                            && p.spacing() == 1 && p.separation() == 0 && set.structures().size() == 2,
                    "orespawn:" + name + " must ask every Utopia chunk for its two structures");
            helper.assertTrue(StructurePicks.ONE_PICK_SETS.stream().noneMatch(k -> k.location().getPath().equals(name)),
                    "orespawn:" + name + " leaves its pick to the chunk pass and must not be a one-pick set");
        }
        for (String old : RETIRED) {
            helper.assertTrue(set(helper, old) == null, "the old one-structure set orespawn:" + old + " is still registered");
        }
        helper.succeed();
    }

    /**
     * addANest (orig OreSpawnWorld.java:999-1021) builds a small bee hive or a mantis hive at even odds (:1010), in
     * exactly "Forest", "ForestHills", "Birch Forest", "Birch Forest Hills", "Jungle" and "JungleHills" (:1004): both in
     * the overworld's chain roll at one weight, on the 1.21 forest, birch forest and jungle, each taking addANest's five
     * grass attempts and honouring DisableOverworldDungeons.
     */
    @GameTest(template = "empty")
    public static void w080b_the_nests_on_the_original_biomes(GameTestHelper helper) {
        StructureSet chain = set(helper, "overworld_chain");
        int[] weights = new int[2];
        String[] hives = {"small_beehive", "mantis_nest"};
        for (int h = 0; h < 2; h++) {
            String name = "orespawn:" + hives[h];
            weights[h] = chain.structures().stream().filter(e -> id(e).equals(name)).mapToInt(e -> e.weight()).sum();
            Structure hive = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .get(ResourceLocation.fromNamespaceAndPath("orespawn", hives[h]));
            Set<String> biomes = hive.biomes().stream().map(b -> b.unwrapKey().orElseThrow().location().toString())
                    .collect(Collectors.toSet());
            helper.assertTrue(biomes.equals(Set.of("minecraft:forest", "minecraft:birch_forest", "minecraft:jungle")),
                    name + " must stand in the forest, birch forest and jungle only, not " + biomes);
            helper.assertTrue(hive instanceof FeatureStructure f && f.anchor().equals("grass_attempts_5")
                    && f.overworldDungeon(), name + " must take addANest's five grass attempts and honour"
                    + " DisableOverworldDungeons");
        }
        helper.assertTrue(weights[0] > 0 && weights[0] == weights[1], "the small bee hive and the mantis hive must share"
                + " the overworld chain at even odds: " + weights[0] + " and " + weights[1]);
        helper.succeed();
    }

    /**
     * The Village dimension's villages (MapGenMoreVillages, spacing 9, separation 7, salt 10387312): the port's set
     * picks exactly the chunks 1.7.10 picks for the same seed, compared with the original's own test, over a patch.
     */
    @GameTest(template = "empty")
    public static void w079c_the_villages_stand_where_1_7_10_put_them(GameTestHelper helper) {
        StructureSet villagesSet = set(helper, "dim_villages");
        helper.assertTrue(villagesSet != null && villagesSet.placement() instanceof RandomSpreadStructurePlacement,
                "orespawn:dim_villages must be a random_spread structure set");
        RandomSpreadStructurePlacement p = (RandomSpreadStructurePlacement) villagesSet.placement();
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
     * The pick is final: in every one-pick set the structure pass asks only the structure vanilla draws first
     * ({@code ChunkGenerator.createStructures}: {@code setLargeFeatureSeed(seed, x, z)}, then {@code nextInt} of the
     * total weight, written out here), and passes every other one over; the picks follow the weights. A set outside
     * the list (the royal trees, whose chunk pass picks) has none passed over.
     */
    @GameTest(template = "empty")
    public static void w080c_a_pick_is_final(GameTestHelper helper) {
        RegistryAccess access = helper.getLevel().registryAccess();
        Registry<StructureSet> sets = access.registryOrThrow(Registries.STRUCTURE_SET);
        long seed = helper.getLevel().getSeed();
        for (ResourceKey<StructureSet> key : StructurePicks.ONE_PICK_SETS) {
            StructureSet set = sets.get(key);
            helper.assertTrue(set != null, key.location() + " is not registered");
            List<StructureSet.StructureSelectionEntry> entries = set.structures();
            int total = entries.stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();
            int[] picked = new int[entries.size()];
            int chunks = 0;
            for (int cx = -60; cx < 60; cx++) {
                for (int cz = -60; cz < 60; cz += 2) {
                    ChunkPos chunk = new ChunkPos(cx, cz);
                    WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
                    random.setLargeFeatureSeed(seed, cx, cz);
                    int draw = random.nextInt(total);
                    int first = 0;
                    while (draw >= entries.get(first).weight()) {
                        draw -= entries.get(first).weight();
                        first++;
                    }
                    for (int i = 0; i < entries.size(); i++) {
                        boolean over = StructurePicks.passedOver(access, entries.get(i), seed, chunk);
                        helper.assertTrue(over == (i != first), key.location() + " at " + chunk + ": "
                                + id(entries.get(i)) + (over ? " passed over, though vanilla draws it first"
                                : " asked, though vanilla draws " + id(entries.get(first)) + " first"));
                    }
                    picked[first]++;
                    chunks++;
                }
            }
            for (int i = 0; i < entries.size(); i++) {
                double expected = (double) chunks * entries.get(i).weight() / total;
                helper.assertTrue(Math.abs(picked[i] - expected) < 5 * Math.sqrt(expected) + 2, key.location() + " picked "
                        + id(entries.get(i)) + " " + picked[i] + " times in " + chunks + " chunks, " + Math.round(expected)
                        + " expected");
            }
        }
        for (StructureSet.StructureSelectionEntry entry : set(helper, "royal_trees").structures()) {
            helper.assertTrue(!StructurePicks.passedOver(access, entry, seed, new ChunkPos(3, 4)),
                    "orespawn:royal_trees had " + id(entry) + " passed over");
        }
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

    /** A detached generator for one of the port's dimensions (data/orespawn/dimension/*.json). */
    private static ChunkGenerator generator(GameTestHelper helper, String biome, String noise, DimensionStyle style) {
        MinecraftServer server = helper.getLevel().getServer();
        Holder<Biome> b = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(biome)));
        ResourceKey<NoiseGeneratorSettings> settings = ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.parse(noise));
        return new OreSpawnChunkGenerator(new FixedBiomeSource(b),
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(settings), style);
    }

    private static RandomState randomState(GameTestHelper helper, String noise, long seed) {
        return RandomState.create(helper.getLevel().getServer().registryAccess().asGetterLookup(),
                ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.parse(noise)), seed);
    }

    /**
     * The structure pass itself, on a detached Mining generator: vanilla's {@code createStructures} run on fresh
     * chunks at mining_structures' spots. A spot builds its first pick where that structure finds its site and nothing
     * otherwise, never one of the others: where the pick is the Leonopteryx nest and its highest grass above Y80 is
     * missing, vanilla alone would have gone on to the next structure, whose lowest grass is nearly always there.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void w080d_the_structure_pass_keeps_to_the_pick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RegistryAccess access = level.getServer().registryAccess();
        ChunkGenerator gen = generator(helper, "orespawn:mining_biome", "orespawn:legacy_extreme_hills", DimensionStyle.MINING);
        long seed = 8780444890188216456L;
        RandomState state = randomState(helper, "orespawn:legacy_extreme_hills", seed);
        ChunkGeneratorStructureState structureState = gen.createState(access.lookupOrThrow(Registries.STRUCTURE_SET), state, seed);
        StructureSet mining = set(helper, "mining_structures");
        RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) mining.placement();
        Set<Structure> miningStructures = mining.structures().stream().map(e -> e.structure().value())
                .filter(s -> !(s instanceof NothingStructure)).collect(Collectors.toSet());
        int built = 0;
        int emptyPick = 0;
        int siteless = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                ChunkPos chunk = placement.getPotentialStructureChunk(seed, (40 + i) * 9, (-20 + j) * 9);
                ProtoChunk proto = new ProtoChunk(chunk, UpgradeData.EMPTY, level, access.registryOrThrow(Registries.BIOME), null);
                gen.createStructures(access, structureState, level.structureManager(), proto, level.getStructureManager());
                List<Structure> starts = proto.getAllStarts().entrySet().stream()
                        .filter(e -> e.getValue().isValid() && miningStructures.contains(e.getKey()))
                        .map(Map.Entry::getKey).toList();
                Structure pick = StructurePicks.firstPick(mining, seed, chunk).structure().value();
                Optional<Structure.GenerationStub> site = pick.findValidGenerationPoint(context(helper, gen, state, seed, chunk));
                if (site.isPresent()) {
                    helper.assertTrue(starts.equals(List.of(pick)), "the spot at " + chunk + " built " + starts
                            + ", its pick was " + pick);
                    built++;
                } else {
                    helper.assertTrue(starts.isEmpty(), "the spot at " + chunk + " built " + starts + ", though its pick "
                            + pick + " found no site");
                    if (miningStructures.contains(pick)) siteless++; else emptyPick++;
                }
            }
        }
        helper.assertTrue(built > 0 && emptyPick > 0 && siteless > 0, "the patch left a case untested: " + built
                + " built, " + emptyPick + " nothing picked, " + siteless + " picks without a site");
        helper.succeed();
    }

    /**
     * The anchors the original's builders used: the Islands' greenhouse, white house and two box dungeons on the D4
     * grass scan, Mining's shadow and WTF alien dungeons (and, through its feature structure, the bee hive) on the
     * lowest grass of the 6×6 grid, the Leonopteryx nest on its highest grass, the royal altars on addKingAltar's
     * eight attempts.
     */
    @GameTest(template = "empty")
    public static void w079e_the_structures_take_the_original_sites(GameTestHelper helper) {
        helper.assertTrue(DungeonType.GREENHOUSE.placement == PlacementMode.ISLANDS_GRASS
                && DungeonType.WHITE_HOUSE.placement == PlacementMode.ISLANDS_GRASS, "the Islands' greenhouse and white"
                + " house must take the D4 grass anchor (orig OreSpawnWorld.java:2230-2251, :2299-2320)");
        helper.assertTrue(DungeonType.ISLANDS_GENERIC_DUNGEON.placement == PlacementMode.ISLANDS_GRASS
                && DungeonType.ISLANDS_RUBY_DUNGEON.placement == PlacementMode.ISLANDS_GRASS, "the Islands' box dungeons"
                + " must take the D4 grass anchor (:2438-2452, :2171-2185)");
        helper.assertTrue(DungeonType.SHADOW.placement == PlacementMode.LOWEST_GRASS_36
                && DungeonType.ALIEN_WTF.placement == PlacementMode.LOWEST_GRASS_36, "the shadow and WTF alien dungeons"
                + " must take the lowest grass of the 6x6 grid (:2143-2169, :2059-2085)");
        helper.assertTrue(DungeonType.LEONOPTERYX_NEST.placement == PlacementMode.HIGHEST_GRASS_36,
                "the Leonopteryx nest must take its highest grass (:2115-2141)");
        helper.assertTrue(DungeonType.KING_ALTAR.placement == PlacementMode.UTOPIA_ALTAR
                && DungeonType.QUEEN_ALTAR.placement == PlacementMode.UTOPIA_ALTAR, "the royal altars must take"
                + " addKingAltar's eight attempts (:2549-2571)");
        Structure beehive = structure(helper, "beehive");
        helper.assertTrue(beehive instanceof FeatureStructure f && f.anchor().equals("lowest_grass_36"),
                "the bee hive must take addBeeHive's lowest grass (:2031-2057)");
        helper.succeed();
    }

    /**
     * On a detached Mining generator (data/orespawn/dimension/mining.json): the Leonopteryx nest's site is exactly
     * addLeonNest's (orig OreSpawnWorld.java:2115-2141: the 6×6 grid, grass inside Y81-128, a column replacing the kept
     * one only when its grass is above the kept anchor, the anchor one above the grass, none without a column), and the
     * bee hive stands on the same lowest grass the ender knight dungeon's LOWEST_GRASS_36 scan finds.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void w079f_mining_sites_replay_the_original_scans(GameTestHelper helper) {
        ChunkGenerator gen = generator(helper, "orespawn:mining_biome", "orespawn:legacy_extreme_hills", DimensionStyle.MINING);
        long seed = 8780444890188216456L;
        RandomState state = randomState(helper, "orespawn:legacy_extreme_hills", seed);
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
     * On a detached Utopia generator: the King and Queen trees grow exactly where the chunk's huge roll takes its royal
     * branch, written out here from the original (orig OreSpawnWorld.java:1830-1880: {@code nextInt(50) == 0}, up to
     * three attempts at chunk + 4 + nextInt(8) for grass inside Y51-127, the tree type, radius and critter draws, the
     * type roll's 0, then the King on a 0), on the grass, and nothing else of the pass grows there; the King and Queen
     * altars build where the chunk's pass picks them (addKingAltar's roll after the grove's, :2549-2571) and the
     * cooldown's share lets the roll through.
     */
    @GameTest(template = "empty", timeoutTicks = 600)
    public static void w080e_utopia_royal_trees_and_altars_follow_the_pass(GameTestHelper helper) {
        ChunkGenerator gen = generator(helper, "orespawn:utopia_plains", "orespawn:legacy_utopia", DimensionStyle.UTOPIA);
        long seed = 8780444890188216456L;
        RandomState state = randomState(helper, "orespawn:legacy_utopia", seed);
        Structure king = structure(helper, "utopia_royal_tree_king");
        Structure queen = structure(helper, "utopia_royal_tree_queen");
        Structure huge = structure(helper, "utopia_huge_tree");
        Structure kingAltar = structure(helper, "utopia_temple_king_altar");
        Structure queenAltar = structure(helper, "utopia_temple_queen_altar");
        int oldLessLag = OreSpawnConfig.LESS_LAG.get();
        OreSpawnConfig.LESS_LAG.set(0);
        int royals = 0;
        int altars = 0;
        int checked = 0;
        try {
            for (int c = 0; c < 40000 && (royals < 2 || altars < 2); c++) {
                ChunkPos chunk = new ChunkPos(-9000 + c % 200, 4000 + c / 200);
                WorldgenRandom r = new WorldgenRandom(new LegacyRandomSource(0L));
                r.setLargeFeatureSeed(seed, chunk.x, chunk.z);
                BlockPos royal = null;
                boolean queenPick = false;
                boolean treeHere = false;
                if (r.nextInt(50) == 0) {
                    for (int i = 0; i < 3 && !treeHere; i++) {
                        int x = 4 + chunk.getMinBlockX() + r.nextInt(8);
                        int z = 4 + chunk.getMinBlockZ() + r.nextInt(8);
                        int top = surface(gen, helper, state, x, z, Heightmap.Types.WORLD_SURFACE_WG);
                        int floor = surface(gen, helper, state, x, z, Heightmap.Types.OCEAN_FLOOR_WG);
                        if (top != floor || top <= 50 || top > 127) continue;
                        treeHere = true;
                        r.nextInt(4);
                        r.nextInt(2);
                        r.nextInt(100);
                        if (r.nextInt(100) == 0) {
                            royal = new BlockPos(x, top - 1, z);
                            queenPick = r.nextInt(2) != 0;
                        }
                    }
                }
                if (treeHere || c % 97 == 0) {
                    Optional<BlockPos> k = king.findValidGenerationPoint(context(helper, gen, state, seed, chunk))
                            .map(Structure.GenerationStub::position);
                    Optional<BlockPos> q = queen.findValidGenerationPoint(context(helper, gen, state, seed, chunk))
                            .map(Structure.GenerationStub::position);
                    helper.assertTrue(k.equals(Optional.ofNullable(royal != null && !queenPick ? royal : null))
                                    && q.equals(Optional.ofNullable(royal != null && queenPick ? royal : null)),
                            "at " + chunk + " the King tree stands at " + k + " and the Queen at " + q
                                    + "; the huge roll's royal branch is " + royal + (queenPick ? " (Queen)" : " (King)"));
                    if (royal != null) {
                        royals++;
                        helper.assertTrue(huge.findValidGenerationPoint(context(helper, gen, state, seed, chunk)).isEmpty(),
                                "a huge tree grew beside the"
                                + " royal tree at " + chunk);
                    }
                    checked++;
                }
                UtopiaTreeStructure.ChunkPass pass = UtopiaTreeStructure.chunkPass(seed, chunk,
                        UtopiaTreeStructure.probe(context(helper, gen, state, seed, chunk)));
                helper.assertTrue(!treeHere || (pass.bigTree() && pass.altar() == null), "the pass at " + chunk
                        + " grew " + pass + " beside the huge roll's tree");
                UtopiaTreeStructure.Altar altar = pass.altar();
                if (altar != null || c % 97 == 0) {
                    boolean clear = LegacyDungeonStructure.altarRollClear(seed, chunk)
                            && (altar == null || UtopiaTreeStructure.reallyBigSpaceClear(seed, gen, helper.getLevel(), state,
                            altar.origin()));
                    Optional<BlockPos> ka = kingAltar.findValidGenerationPoint(context(helper, gen, state, seed, chunk))
                            .map(Structure.GenerationStub::position);
                    Optional<BlockPos> qa = queenAltar.findValidGenerationPoint(context(helper, gen, state, seed, chunk))
                            .map(Structure.GenerationStub::position);
                    // the piece's origin is its pad's centre, over the grass corner the pass found
                    BlockPos pad = altar == null ? null : altar.origin().offset(LegacyDungeonStructure.ALTAR_HALF_PAD, 0,
                            LegacyDungeonStructure.ALTAR_HALF_PAD);
                    helper.assertTrue(ka.equals(Optional.ofNullable(altar != null && clear && !altar.queen() ? pad : null))
                                    && qa.equals(Optional.ofNullable(altar != null && clear && altar.queen() ? pad : null)),
                            "at " + chunk + " the King altar stands at " + ka + " and the Queen's at " + qa
                                    + "; the pass picked " + altar + (clear ? "" : " behind the cooldown or the space check"));
                    if (altar != null) altars++;
                }
            }
        } finally {
            OreSpawnConfig.LESS_LAG.set(oldLessLag);
        }
        helper.assertTrue(royals > 0 && altars > 0 && checked > 0, "the patch left a case untested: " + royals
                + " royal trees, " + altars + " altars");
        helper.succeed();
    }
}
