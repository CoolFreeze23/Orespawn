package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.GenericDungeon;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * WGEN-078: the Islands structure roll at the original's rate. orig OreSpawnWorld.java:134-176 rolls one chunk in a
 * hundred for a big structure, but only while the shared {@code recently_placed} cooldown is clear, and every one of
 * the roll's builders sets that cooldown to 50 (the tower :2224, the ruby dungeon :2181, the generic dungeon :2448, the
 * other D4 builders likewise), blocking the roll for the next 49 chunks the populator runs. A build therefore comes
 * {@code p / (1 + 49p)} = one chunk in 149 on average: a tower (three slots of 19) one chunk in 944, King or Queen at
 * even odds (:2219), a one-slot structure one in 2831, the generic dungeon (four slots) one in 708.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class IslandsRotationTests {

    private static final String[] ONE_SLOT = {"ender_castle_islands", "inca_pyramid", "robot_lab", "mini_dungeon",
            "cephadrome_altar", "greenhouse", "nightmare_rookery", "stinky_house", "white_house", "pumpkin", "rainbow"};

    private static RandomSpreadStructurePlacement assertSet(GameTestHelper helper, Registry<StructureSet> sets, String name,
                                                            int spacing, int separation, double chunksPerBuild) {
        StructureSet set = sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", name));
        helper.assertTrue(set != null && set.placement() instanceof RandomSpreadStructurePlacement,
                "orespawn:" + name + " must be a random_spread structure set");
        RandomSpreadStructurePlacement placement = (RandomSpreadStructurePlacement) set.placement();
        helper.assertTrue(placement.spacing() == spacing && placement.separation() == separation,
                "orespawn:" + name + " is " + placement.spacing() + "/" + placement.separation() + ", expected "
                        + spacing + "/" + separation);
        double cell = (double) spacing * spacing;
        helper.assertTrue(Math.abs(cell - chunksPerBuild) / chunksPerBuild < 0.03,
                "orespawn:" + name + "'s cell of " + cell + " chunks is off the original's one in " + chunksPerBuild);
        return placement;
    }

    /**
     * The towers' one set (King and Queen at even odds, one tower a cell) and the eleven one-slot structures' sets
     * carry the cooldown's rate, and no two one-slot sets lay their structures out in a shared pattern: sets of equal
     * spacing whose salts sit close together put their structures at nearly the same offset from each other in cell
     * after cell (the King's and Queen's towers, on salts 84320 and 84321, stood about a hundred blocks apart in a third
     * of their cells), so the most common offset between any two of them must stay rare over 3,600 cells.
     */
    @GameTest(template = "empty")
    public static void w078a_the_islands_sets_carry_the_cooldown(GameTestHelper helper) {
        Registry<StructureSet> sets = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        double perBuild = 100 * (1 + 49 / 100.0);
        assertSet(helper, sets, "challenge_towers", 31, 8, perBuild * 19 / 3);
        StructureSet towers = sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", "challenge_towers"));
        helper.assertTrue(towers.structures().size() == 2 && towers.structures().stream().allMatch(e -> e.weight() == 1),
                "the towers' set must hold the King's and the Queen's tower at even odds: " + towers.structures());
        for (String old : new String[] {"challenge_tower_king", "challenge_tower_queen"}) {
            helper.assertTrue(sets.get(ResourceLocation.fromNamespaceAndPath("orespawn", old)) == null,
                    "the old one-tower set orespawn:" + old + " is still registered");
        }
        long seed = helper.getLevel().getSeed();
        java.util.List<RandomSpreadStructurePlacement> placements = new java.util.ArrayList<>();
        for (String one : ONE_SLOT) {
            placements.add(assertSet(helper, sets, one, 53, 8, perBuild * 19));
        }
        for (int a = 0; a < placements.size(); a++) {
            for (int b = a + 1; b < placements.size(); b++) {
                java.util.Map<Long, Integer> offsets = new java.util.HashMap<>();
                int most = 0;
                for (int i = -30; i < 30; i++) {
                    for (int j = -30; j < 30; j++) {
                        ChunkPos pa = placements.get(a).getPotentialStructureChunk(seed, i * 53, j * 53);
                        ChunkPos pb = placements.get(b).getPotentialStructureChunk(seed, i * 53, j * 53);
                        long key = ((long) (pb.x - pa.x) << 32) ^ (pb.z - pa.z & 0xFFFFFFFFL);
                        most = Math.max(most, offsets.merge(key, 1, Integer::sum));
                    }
                }
                helper.assertTrue(most < 72, "orespawn:" + ONE_SLOT[a] + " and orespawn:" + ONE_SLOT[b] + " share an offset in "
                        + most + " of 3,600 cells");
            }
        }
        helper.succeed();
    }

    private static long firstSeed(Predicate<RandomSource> first) {
        for (long seed = 0; ; seed++) {
            if (first.test(RandomSource.create(seed))) return seed;
        }
    }

    private static String spawnerMob(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawner)) return "<no spawner at " + pos + ">";
        CompoundTag tag = spawner.getSpawner().save(new CompoundTag());
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    /**
     * The Islands dungeon roll (orig :136-176): its one draw builds the ruby dungeon (one slot, the Ruby Bird
     * spawner in a ruby-ore box, addD4RubyDungeon :2171-2185) or the generic dungeon (four slots, :2438-2452) on the
     * grass a Y20-to-Y5 scan finds at the chunk corner plus {@code nextInt(8)}, and nothing on any other draw. Built
     * at Y10 above this test's own columns (the test level's ground is far below).
     */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void w078b_the_islands_dungeon_roll(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos chunk = new ChunkPos(helper.absolutePos(BlockPos.ZERO));
        long ruby = firstSeed(r -> r.nextInt(2831) == 4);
        long generic = firstSeed(r -> r.nextInt(2831) < 4);
        long neither = firstSeed(r -> r.nextInt(2831) > 4);
        helper.assertTrue(!GenericDungeon.tryPlaceIslandsDungeon(level, RandomSource.create(neither),
                chunk.getMinBlockX(), chunk.getMinBlockZ()), "the roll built a dungeon on a draw above 4");
        String[] expected = {"orespawn:ruby_bird", null};
        long[] seeds = {ruby, generic};
        int[] half = {5, 6};
        for (int k = 0; k < 2; k++) {
            RandomSource replay = RandomSource.create(seeds[k]);
            replay.nextInt(2831);
            BlockPos grass = new BlockPos(chunk.getMinBlockX() + replay.nextInt(8), 10, chunk.getMinBlockZ() + replay.nextInt(8));
            level.setBlock(grass, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            try {
                helper.assertTrue(GenericDungeon.tryPlaceIslandsDungeon(level, RandomSource.create(seeds[k]),
                        chunk.getMinBlockX(), chunk.getMinBlockZ()), "the roll built nothing on the grass at " + grass);
                BlockPos spawner = grass.offset(half[k], 1, half[k]);
                String mob = spawnerMob(level, spawner);
                if (expected[k] != null) {
                    helper.assertTrue(mob.equals(expected[k]), "the ruby dungeon's spawner at " + spawner + " is " + mob);
                    int ores = 0;
                    for (BlockPos p : BlockPos.betweenClosed(grass, grass.offset(9, 4, 9))) {
                        if (level.getBlockState(p).is(danger.orespawn.ModBlocks.ORE_RUBY.get())) ores++;
                    }
                    helper.assertTrue(ores > 0, "the ruby dungeon's walls and ceiling carry no ruby ore");
                } else {
                    helper.assertTrue(mob.startsWith("orespawn:") || mob.startsWith("minecraft:"),
                            "the generic dungeon has no spawner at " + spawner + ": " + mob);
                }
            } finally {
                for (BlockPos p : BlockPos.betweenClosed(grass.offset(-1, -1, -1), grass.offset(13, 8, 13))) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        helper.succeed();
    }
}
