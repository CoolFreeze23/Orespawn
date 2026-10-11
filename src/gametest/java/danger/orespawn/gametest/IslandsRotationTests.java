package danger.orespawn.gametest;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.OreSpawnChunkGenerator;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import danger.orespawn.world.structure.StructurePicks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
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
 * WGEN-078 / WGEN-080: the Islands structure roll. orig OreSpawnWorld.java:134-176 rolls one chunk in a hundred for a
 * big structure, but only while the shared {@code recently_placed} cooldown is clear, and every one of the roll's
 * builders sets that cooldown to 50 (the tower :2224, the ruby dungeon :2181, the generic dungeon :2448, the other D4
 * builders likewise), blocking the roll for the next 49 chunks the populator runs. A build therefore comes
 * {@code p / (1 + 49p)} = one chunk in 149 on average, and {@code nextInt(19)} picks its builder: the towers three
 * slots (King or Queen at even odds, :2219), the generic dungeon four, every other builder, the ruby dungeon among
 * them, one. The whole roll is one set, islands_structures: one spot in every 9 by 9 chunks, its structure picked at
 * those odds or nothing, the pick final.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class IslandsRotationTests {

    /** The roll's builders in the original's slot order (:137-176), with their slots of nineteen. */
    private static final String[] ROLL = {"challenge_tower_king", "challenge_tower_queen", "islands_generic_dungeon",
            "ender_castle_islands", "inca_pyramid", "robot_lab", "mini_dungeon", "islands_ruby_dungeon",
            "cephadrome_altar", "greenhouse", "nightmare_rookery", "stinky_house", "white_house", "pumpkin", "rainbow"};
    private static final double[] SLOTS = {1.5, 1.5, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    /** The share of the roll's builds the weights keep for D4BigSpaceCheck (:2655-2664): all of them since WGEN-120,
     *  which turns a spot away where an earlier structure stands in the check (IslandsSpaceCheck); WGEN-080's weights
     *  had kept 95.5%, the share it simulated, and refused none. */
    private static final double KEPT = 1.0;

    private static final String[] RETIRED = {"challenge_towers", "challenge_tower_king", "challenge_tower_queen",
            "ender_castle_islands", "inca_pyramid", "robot_lab", "mini_dungeon", "cephadrome_altar", "greenhouse",
            "nightmare_rookery", "stinky_house", "white_house", "pumpkin", "rainbow"};

    /**
     * The roll's one set: 9 by 9 chunks a spot, a separation of one, each builder at its slots of nineteen of one
     * build in 149 chunks; the space check's refusals come from IslandsSpaceCheck (w078c), not the weights.
     */
    @GameTest(template = "empty")
    public static void w078a_the_islands_roll_is_one_set(GameTestHelper helper) {
        Registry<StructureSet> sets = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        StructureSet set = sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", "islands_structures"));
        helper.assertTrue(set != null && set.placement() instanceof RandomSpreadStructurePlacement p
                && p.spacing() == 9 && p.separation() == 1, "orespawn:islands_structures must be a 9/1 random_spread set");
        List<String> names = new ArrayList<>();
        for (String n : ROLL) names.add("orespawn:" + n);
        names.add("orespawn:nothing");
        List<String> ours = set.structures().stream()
                .map(e -> e.structure().unwrapKey().map(k -> k.location().toString()).orElse("?")).toList();
        helper.assertTrue(ours.equals(names), "orespawn:islands_structures holds " + ours);
        int total = set.structures().stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();
        double perBuild = 100 * (1 + 49 / 100.0);
        for (int i = 0; i < ROLL.length; i++) {
            double original = perBuild * 19 / SLOTS[i] / KEPT;
            double chunks = 81.0 * total / set.structures().get(i).weight();
            helper.assertTrue(Math.abs(chunks - original) / original < 0.005, ROLL[i] + " comes one chunk in "
                    + Math.round(chunks) + ", the original's one in " + Math.round(original));
        }
        helper.assertTrue(StructurePicks.ONE_PICK_SETS.stream().anyMatch(k -> k.location().getPath().equals("islands_structures")),
                "the Islands' roll must be a one-pick set");
        for (String old : RETIRED) {
            helper.assertTrue(sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", old)) == null,
                    "the old set orespawn:" + old + " is still registered");
        }
        helper.succeed();
    }

    private static String spawnerMob(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawner)) return "<no spawner at " + pos + ">";
        CompoundTag tag = spawner.getSpawner().save(new CompoundTag());
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    /**
     * The Islands' two box dungeons are structures of the roll now. On a detached Islands generator they take the D4
     * scan's site (orig :2438-2452, :2171-2185: chunk + nextInt(8), the grass inside Y5-20), and built there the ruby
     * dungeon is the 10×10×5 box with the Ruby Bird spawner, ruby ore in its walls and ceiling and the ruby chest, the
     * generic one the 12×12×6 box with a spawner from the twelve-mob pool and the generic chest. Built at Y10 above
     * this test's own columns (the test level's ground is far below).
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void w078b_the_islands_dungeons_are_structures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        Holder<Biome> island = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("orespawn:island_biome")));
        ResourceKey<NoiseGeneratorSettings> islands = ResourceKey.create(Registries.NOISE_SETTINGS,
                ResourceLocation.parse("orespawn:islands"));
        OreSpawnChunkGenerator gen = new OreSpawnChunkGenerator(new FixedBiomeSource(island),
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(islands), DimensionStyle.ISLANDS);
        long seed = 20260928L;
        RandomState state = RandomState.create(server.registryAccess().asGetterLookup(), islands, seed);
        String[] types = {"islands_generic_dungeon", "islands_ruby_dungeon"};
        int oldLessLag = OreSpawnConfig.LESS_LAG.get();
        OreSpawnConfig.LESS_LAG.set(0);
        try {
            for (String type : types) {
                Structure dungeon = server.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .get(ResourceLocation.fromNamespaceAndPath("orespawn", type));
                helper.assertTrue(dungeon != null, "orespawn:" + type + " is not registered");
                for (int c = 0; c < 12; c++) {
                    ChunkPos chunk = new ChunkPos(c * 7 - 30, 11 - c * 5);
                    WorldgenRandom r = new WorldgenRandom(new LegacyRandomSource(0L));
                    r.setLargeFeatureSeed(seed, chunk.x, chunk.z);
                    int x = chunk.getMinBlockX() + r.nextInt(8);
                    int z = chunk.getMinBlockZ() + r.nextInt(8);
                    int grass = gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, state) - 1;
                    BlockPos expected = grass >= 5 && grass <= 20 ? new BlockPos(x, grass, z) : null;
                    Optional<BlockPos> site = dungeon.findValidGenerationPoint(new Structure.GenerationContext(
                            server.registryAccess(), gen, gen.getBiomeSource(), state, level.getStructureManager(), seed,
                            chunk, level, b -> true)).map(Structure.GenerationStub::position);
                    helper.assertTrue(site.equals(Optional.ofNullable(expected)), "orespawn:" + type + "'s site at "
                            + chunk + " is " + site + ", the D4 scan's is " + expected);
                }
            }
        } finally {
            OreSpawnConfig.LESS_LAG.set(oldLessLag);
        }
        ChunkPos here = new ChunkPos(helper.absolutePos(BlockPos.ZERO));
        LegacyDungeonPiece.DungeonType[] built = {LegacyDungeonPiece.DungeonType.ISLANDS_GENERIC_DUNGEON,
                LegacyDungeonPiece.DungeonType.ISLANDS_RUBY_DUNGEON};
        int[] half = {6, 5};
        // One seeded stream for both builds: the ruby dungeon's ruby ore is a 1-in-20 roll per wall and ceiling cell,
        // so an unseeded build has none in (19/20)^244 of runs; under this seed it has 15.
        RandomSource random = RandomSource.create(0L);
        for (int k = 0; k < 2; k++) {
            BlockPos corner = new BlockPos(here.getMinBlockX() + 2, 10, here.getMinBlockZ() + 2);
            try {
                LegacyDungeonPiece.buildNow(level, corner, built[k], random);
                BlockPos spawner = corner.offset(half[k], 1, half[k]);
                String mob = spawnerMob(level, spawner);
                if (k == 1) {
                    helper.assertTrue(mob.equals("orespawn:ruby_bird"), "the ruby dungeon's spawner at " + spawner + " is " + mob);
                    int ores = 0;
                    for (BlockPos p : BlockPos.betweenClosed(corner, corner.offset(9, 4, 9))) {
                        if (level.getBlockState(p).is(danger.orespawn.ModBlocks.ORE_RUBY.get())) ores++;
                    }
                    helper.assertTrue(ores > 0, "the ruby dungeon's walls and ceiling carry no ruby ore");
                } else {
                    helper.assertTrue(mob.startsWith("orespawn:") || mob.startsWith("minecraft:"),
                            "the generic dungeon has no spawner at " + spawner + ": " + mob);
                }
                BlockPos chest = corner.offset(half[k], 1, 1);
                helper.assertTrue(level.getBlockEntity(chest) instanceof RandomizableContainerBlockEntity container
                                && container.getLootTable() != null && container.getLootTable().location().getPath()
                                .equals(k == 1 ? "chests/ruby_dungeon" : "chests/generic_dungeon"),
                        "the " + built[k] + " chest at " + chest + " is missing or carries the wrong loot");
                helper.assertTrue(level.getBlockState(corner.offset(3, 0, 3)).is(Blocks.MOSSY_COBBLESTONE), "the "
                        + built[k] + "'s floor is not mossy cobblestone at " + corner.offset(3, 0, 3));
            } finally {
                for (BlockPos p : BlockPos.betweenClosed(corner.offset(-1, -1, -1), corner.offset(13, 8, 13))) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        helper.succeed();
    }

    /**
     * WGEN-120 (#8): the original's space checks between the Islands' structures. On a detached Islands generator, over
     * 32 by 32 cells of islands_structures: no built spot has an earlier built neighbour (IslandsSpaceCheck's stand-in
     * order) whose envelope meets its D4BigSpaceCheck box at Y11 (orig :2655-2664: x -25 to 39, z -25 to 29 from the
     * chunk's corner) or, for a tower, its air check 18 over its grass (orig :2212-2218: x -20 to 32, z -4 to 32 from the
     * site); every pick turned away has one; and the share turned away is near the 4.5% WGEN-080 simulated for the
     * original.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void w078c_an_islands_structure_is_never_built_into_another(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        Holder<Biome> island = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse("orespawn:island_biome")));
        ResourceKey<NoiseGeneratorSettings> islands = ResourceKey.create(Registries.NOISE_SETTINGS,
                ResourceLocation.parse("orespawn:islands"));
        OreSpawnChunkGenerator gen = new OreSpawnChunkGenerator(new FixedBiomeSource(island),
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(islands), DimensionStyle.ISLANDS);
        long seed = 1007L;
        RandomState state = RandomState.create(server.registryAccess().asGetterLookup(), islands, seed);
        StructureSet set = server.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)
                .get(ResourceLocation.fromNamespaceAndPath("orespawn", "islands_structures"));
        RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) set.placement();
        int cells = 16;
        int side = 2 * cells;
        LegacyDungeonPiece.DungeonType[][] type = new LegacyDungeonPiece.DungeonType[side][side];
        BlockPos[][] site = new BlockPos[side][side];
        boolean[][] built = new boolean[side][side];
        ChunkPos[][] spot = new ChunkPos[side][side];
        int oldLessLag = OreSpawnConfig.LESS_LAG.get();
        OreSpawnConfig.LESS_LAG.set(0);
        try {
            for (int i = 0; i < side; i++) {
                for (int j = 0; j < side; j++) {
                    int gx = i - cells, gz = j - cells;
                    ChunkPos chunk = placement.getPotentialStructureChunk(seed, gx * placement.spacing(), gz * placement.spacing());
                    spot[i][j] = chunk;
                    Structure structure = StructurePicks.firstPick(set, seed, chunk).structure().value();
                    if (!(structure instanceof danger.orespawn.world.structure.LegacyDungeonStructure legacy)) continue;
                    type[i][j] = legacy.dungeonType();
                    Optional<BlockPos> stub = structure.findValidGenerationPoint(new Structure.GenerationContext(
                            server.registryAccess(), gen, gen.getBiomeSource(), state, level.getStructureManager(), seed,
                            chunk, level, structure.biomes()::contains)).map(Structure.GenerationStub::position);
                    built[i][j] = stub.isPresent();
                    site[i][j] = stub.orElse(null);
                    if (type[i][j] != LegacyDungeonPiece.DungeonType.RAINBOW) {
                        // the site written out: the D4 scan's chunk + nextInt(8) and the grass under it (the pumpkin's
                        // air above); the rainbow's own lies in the sky and matters to no check of its own
                        WorldgenRandom r = new WorldgenRandom(new LegacyRandomSource(0L));
                        r.setLargeFeatureSeed(seed, chunk.x, chunk.z);
                        int x = chunk.getMinBlockX() + r.nextInt(8);
                        int z = chunk.getMinBlockZ() + r.nextInt(8);
                        int grass = gen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, state) - 1;
                        BlockPos scan = new BlockPos(x, type[i][j] == LegacyDungeonPiece.DungeonType.PUMPKIN ? grass + 1 : grass, z);
                        helper.assertTrue(stub.isEmpty() || stub.get().equals(scan), type[i][j] + " at " + chunk
                                + " stands at " + stub + ", the D4 scan's site " + scan);
                        site[i][j] = scan;
                    }
                }
            }
        } finally {
            OreSpawnConfig.LESS_LAG.set(oldLessLag);
        }
        int picks = 0, refused = 0;
        List<String> wrong = new ArrayList<>();
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                if (type[i][j] == null) continue;
                picks++;
                if (!built[i][j]) refused++;
                boolean blocked = false;
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        int a = i + di, b = j + dj;
                        if ((di == 0 && dj == 0) || a < 0 || b < 0 || a >= side || b >= side || !built[a][b]) continue;
                        if (!danger.orespawn.world.structure.IslandsSpaceCheck.before(seed, a - cells, b - cells, i - cells, j - cells)) continue;
                        blocked |= danger.orespawn.world.structure.IslandsSpaceCheck.blocks(type[a][b], site[a][b], spot[i][j],
                                type[i][j], site[i][j]);
                    }
                }
                // the patch's edge cells have neighbours outside it: only the inner cells' refusals are fully seen
                boolean inner = i > 0 && j > 0 && i < side - 1 && j < side - 1;
                if (built[i][j] && blocked) wrong.add(type[i][j] + " built at " + spot[i][j] + " into an earlier structure");
                if (!built[i][j] && !blocked && inner) wrong.add(type[i][j] + " turned away at " + spot[i][j] + " with nothing earlier in its way");
            }
        }
        helper.assertTrue(wrong.isEmpty(), wrong.size() + " wrong, the first: " + String.join("; ", wrong.subList(0, Math.min(5, wrong.size()))));
        double share = (double) refused / picks;
        helper.assertTrue(share > 0.02 && share < 0.08, refused + " of " + picks + " picks turned away ("
                + Math.round(share * 1000) / 10.0 + "%), the original's about 4.5%");
        helper.succeed();
    }
}
