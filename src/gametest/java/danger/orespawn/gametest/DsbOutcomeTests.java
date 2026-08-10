package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.block.entity.RandomDungeonSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Random Dungeon Spawner Block outcome tests (checklist items i129 / i143 /
 * i150 / i156 / i163 / i171 / i175, all ITEM-020 descendants).
 *
 * <p>Strategy: every outcome index 0..49 is driven DIRECTLY through the
 * sanctioned public seam {@code RandomDungeonSpawnerBlockEntity.buildForType}
 * (RandomDungeonSpawnerBlockEntity.java:202, visibility widened for this
 * suite) instead of rolling {@code nextInt(50)} hundreds of times; one test
 * covers the real 400-tick fuse via a placed {@code random_dungeon_block}
 * (orig DungeonSpawnerBlock.java:39 — scheduleBlockUpdate 400).</p>
 *
 * <p>Big-structure builds happen at FAR deterministic offsets from the test
 * origin, per the suite convention: X column {@code K*512}, Z
 * {@code 50000 + row*512}, Y forced to 100 so the deepest writes (Basilisk
 * maze −33, orig BasiliskMaze.java:305) stay inside the world height. This
 * file's K values (batch 9): K=904 (i129), 905 (i143), 906 (i150), 907
 * (i163), 908 (i171 full sweep), 909 (i175), 910 row 1 (i156 loot igloos —
 * int-offset chest asserts only, quadrant/precision-immune). The i156 igloo
 * QUADRANT builds are the exception to the convention: the float-idiom shell
 * asserts need small-magnitude coordinates of known sign (float32 has no
 * half-steps beyond 2^23, and the suite's absolute X is negative), so they
 * use the explicit mirrored columns +(910*512+256), Z +50000 for (+,+) and
 * −(910*512+256), Z −50000 for the negative quadrant — neither touched by
 * any other suite region. Blocks at those absolute positions are asserted
 * via {@code helper.getLevel().getBlockState} (never the template-relative
 * {@code helper.assertBlock*}).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DsbOutcomeTests {

    // ---------------------------------------------------------------
    // Far-region plumbing (suite convention, batch 9 → K base 900)
    // ---------------------------------------------------------------

    /** Far deterministic build origin: X = K*512, Z = 50000 + row*512, Y = 100. */
    private static BlockPos region(GameTestHelper helper, int k, int row) {
        return helper.absolutePos(BlockPos.ZERO).offset(k * 512, 0, 50_000 + row * 512).atY(100);
    }

    private record SpawnerHit(BlockPos pos, String id) {}

    private record Scan(List<SpawnerHit> spawners, Set<Block> found) {}

    /** Reads the bound entity id of a placed mob spawner via its public saved NBT. */
    private static String spawnerId(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawner)) return "";
        // BaseSpawner.save → "SpawnData" → SpawnData.CODEC → "entity" → "id"
        // (net.minecraft.world.level.BaseSpawner / SpawnData, 1.21.1).
        CompoundTag tag = spawner.saveWithoutMetadata(level.registryAccess());
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    /**
     * One pass over the signature box around {@code c} (±40 X/Z, −{@code down}
     * .. +65 Y), collecting all mob spawners (with bound ids) and which of the
     * {@code wanted} blocks occur. ServerLevel block reads force-load the
     * chunks synchronously, so this works far outside the template.
     */
    private static Scan scan(ServerLevel level, BlockPos c, Set<Block> wanted, int down) {
        List<SpawnerHit> spawners = new ArrayList<>();
        Set<Block> found = new HashSet<>();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-40, -down, -40), c.offset(40, 65, 40))) {
            BlockState state = level.getBlockState(p);
            if (state.is(Blocks.SPAWNER)) {
                spawners.add(new SpawnerHit(p.immutable(), spawnerId(level, p)));
            } else if (wanted.contains(state.getBlock())) {
                found.add(state.getBlock());
            }
        }
        return new Scan(spawners, found);
    }

    private static String idOf(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    private static long count(Scan scan, EntityType<?> type) {
        String id = idOf(type);
        return scan.spawners().stream().filter(s -> s.id().equals(id)).count();
    }

    private static int countEntities(ServerLevel level, BlockPos c, EntityType<?> type) {
        AABB box = new AABB(c.getX() - 48, c.getY() - 45, c.getZ() - 48,
                c.getX() + 48, c.getY() + 70, c.getZ() + 48);
        return level.getEntitiesOfClass(Entity.class, box, e -> e.getType() == type).size();
    }

    private static void requireSpawner(GameTestHelper helper, Scan scan, EntityType<?> type,
                                       int min, int outcome) {
        helper.assertTrue(count(scan, type) >= min,
                "DSB outcome " + outcome + ": expected >= " + min + " '" + idOf(type)
                        + "' spawners, found " + count(scan, type));
    }

    private static void requireBlock(GameTestHelper helper, Scan scan, Block block, int outcome) {
        helper.assertTrue(scan.found().contains(block),
                "DSB outcome " + outcome + ": expected block "
                        + BuiltInRegistries.BLOCK.getKey(block) + " in the signature box");
    }

    private static void requireBlockAt(GameTestHelper helper, ServerLevel level, BlockPos pos,
                                       Block block, String what) {
        helper.assertTrue(level.getBlockState(pos).is(block),
                what + ": expected " + BuiltInRegistries.BLOCK.getKey(block) + " at " + pos
                        + ", found " + BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()));
    }

    private static void requireSpawnerAt(GameTestHelper helper, ServerLevel level, BlockPos pos,
                                         EntityType<?> type, String what) {
        helper.assertTrue(level.getBlockState(pos).is(Blocks.SPAWNER)
                        && spawnerId(level, pos).equals(idOf(type)),
                what + ": expected a '" + idOf(type) + "' spawner at " + pos);
    }

    /** Runs {@code buildForType} for one outcome and asserts its documented signature. */
    private static void buildAndAssert(GameTestHelper helper, BlockPos pos, int type) {
        ServerLevel level = helper.getLevel();
        boolean placed = RandomDungeonSpawnerBlockEntity.buildForType(level, pos, type);
        helper.assertTrue(placed, "DSB outcome " + type + ": buildForType returned false");
        assertOutcomeSignature(helper, level, pos, type);
    }

    /**
     * The 50-outcome signature table. Each case asserts server-observable
     * blocks / spawner ids / spawned entities documented for that outcome;
     * citations name the port generator plus the extraction spec or original
     * source whose values are asserted.
     */
    private static void assertOutcomeSignature(GameTestHelper helper, ServerLevel level,
                                               BlockPos pos, int type) {
        switch (type) {
            case 0 -> { // FairyTree — CrystalStructures.buildFairyTree (orig Trees.java:480-489;
                        // trees_spec.md): trunk crystal log (x, y+1), Fairy spawner (x-1, y+1, z).
                requireBlockAt(helper, level, pos.offset(0, 1, 0), ModBlocks.CRYSTAL_TREE_LOG.get(),
                        "outcome 0 fairy-tree trunk");
                requireSpawnerAt(helper, level, pos.offset(-1, 1, 0), ModEntities.FAIRY.get(),
                        "outcome 0 fairy spawner");
            }
            case 1 -> { // FairyCastleTree — CrystalStructures.buildFairyCastleTree (orig
                        // Trees.java:616-660; trees_spec.md): crystal-log platforms always build.
                Scan s = scan(level, pos, Set.of(ModBlocks.CRYSTAL_TREE_LOG.get()), 35);
                requireBlock(helper, s, ModBlocks.CRYSTAL_TREE_LOG.get(), 1);
            }
            case 2 -> { // King tower — LegacyDungeonPiece.generateChallengeTower(king): corner
                        // Terrible Terror spawners (LDP:2277,2333-2336) + RTP trap blocks
                        // (d5_extraction/enormous_castle_spec.md, ITEM-066).
                Scan s = scan(level, pos, Set.of(ModBlocks.BLOCK_TELEPORT.get()), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_TERRIBLE_TERROR.get(), 1, 2);
                requireBlock(helper, s, ModBlocks.BLOCK_TELEPORT.get(), 2);
            }
            case 3 -> { // Rotator station — CrystalStructures.buildRotatorStation (orig
                        // GD:787-810; rotator_station_spec.md): spawners at +5/+6, stone +4.
                requireSpawnerAt(helper, level, pos.offset(0, 5, 0), ModEntities.ENTITY_ROTATOR.get(),
                        "outcome 3 rotator spawner +5");
                requireSpawnerAt(helper, level, pos.offset(0, 6, 0), ModEntities.ENTITY_ROTATOR.get(),
                        "outcome 3 rotator spawner +6");
                requireBlockAt(helper, level, pos.offset(0, 4, 0), ModBlocks.CRYSTAL_STONE.get(),
                        "outcome 3 station column");
            }
            case 4 -> { // Bee hive — BeehiveFeature.buildAt (orig GD:812-858; dsb_sweep_spec.md).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_BEE.get(), 1, 4);
            }
            case 5 -> { // Haunted house — HauntedHouseGenerator (haunted_house_spec.md):
                        // Rat/Ghost/GhostSkelly spawner stack.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_RAT.get(), 1, 5);
                requireSpawner(helper, s, ModEntities.GHOST.get(), 1, 5);
                requireSpawner(helper, s, ModEntities.GHOST_SKELLY.get(), 1, 5);
            }
            case 6 -> { // Mantis nest — MantisNestFeature.buildAt (orig GD:1012-1062).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_MANTIS.get(), 1, 6);
            }
            case 7 -> { // Kyuubi dungeon — KyuubiDungeonGenerator (kyuubi_dungeon_spec.md:
                        // 3 stacked "Kyuubi" spawners, orig GD:1246-1251).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_KYUUBI.get(), 1, 7);
            }
            case 8 -> { // Small bee hive — SmallBeehiveFeature.buildAt (honeycomb trunk,
                        // SmallBeehiveFeature.java:148,227; orig GD:1363-1451).
                Scan s = scan(level, pos, Set.of(Blocks.HONEYCOMB_BLOCK), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_BEE.get(), 1, 8);
                requireBlock(helper, s, Blocks.HONEYCOMB_BLOCK, 8);
            }
            case 9 -> { // Shadow dungeon — LDP.generateShadowDungeon (orig GD:1453-1537):
                        // soul-sand cross + Nightmare(PitchBlack)/EnderReaper spawners
                        // (LDP:718-762, dsb_sweep_spec.md A.1).
                Scan s = scan(level, pos, Set.of(Blocks.SOUL_SAND), 35);
                requireSpawner(helper, s, ModEntities.PITCH_BLACK.get(), 1, 9);
                requireSpawner(helper, s, ModEntities.ENDER_REAPER.get(), 1, 9);
                requireBlock(helper, s, Blocks.SOUL_SAND, 9);
            }
            case 10 -> { // Alien WTF — LDP.generateAlienWtfDungeon (orig GD:1570-1691;
                         // dsb_sweep_spec.md A.2): lapis antenna at (−2, −17, −2), Alien spawners.
                requireBlockAt(helper, level, pos.offset(-2, -17, -2), Blocks.LAPIS_BLOCK,
                        "outcome 10 lapis antenna corner (cposy - (depth-3), LDP:1684)");
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ALIEN.get(), 1, 10);
            }
            case 11 -> { // Ender knight dungeon — EnderKnightDungeonGenerator:97-99
                         // (obsidian octagon, end-stone floor, 2 floating spawners).
                Scan s = scan(level, pos, Set.of(Blocks.OBSIDIAN, Blocks.END_STONE), 35);
                requireSpawner(helper, s, ModEntities.ENDER_KNIGHT.get(), 1, 11);
                requireBlock(helper, s, Blocks.OBSIDIAN, 11);
                requireBlock(helper, s, Blocks.END_STONE, 11);
            }
            case 12 -> { // Play pool — PlayPoolGenerator (play_pool_spec.md: 4 Attack Squid
                         // spawners on the +16 platform, orig GD:1940-1945).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ATTACK_SQUID.get(), 4, 12);
            }
            case 13 -> { // Water dragon lair — WaterDragonLairGenerator (water_dragon_lair_spec.md:
                         // 4 Water Dragon spawners).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.WATER_DRAGON.get(), 4, 13);
            }
            case 14 -> { // Cloud shark dungeon — cloud_shark_dungeon_spec.md: spawner east at
                         // (X0+1, Y0, Z0), cardinal plus (orig GD:2066-2085).
                requireSpawnerAt(helper, level, pos.offset(1, 0, 0), ModEntities.CLOUD_SHARK.get(),
                        "outcome 14 east cloud-shark spawner");
                Scan s = scan(level, pos, Set.of(Blocks.GLOWSTONE), 35);
                requireSpawner(helper, s, ModEntities.CLOUD_SHARK.get(), 4, 14);
                requireBlock(helper, s, Blocks.GLOWSTONE, 14);
            }
            case 15 -> { // Leaf monster dungeon — LeafMonsterDungeonGenerator (4 spawners,
                         // leaf_monster_dungeon_spec.md).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_LEAF_MONSTER.get(), 4, 15);
            }
            case 16 -> { // Mini dungeon — MiniDungeonGenerator (mini_dungeon_spec.md:
                         // Butterfly among the 22 spawners).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_BUTTERFLY.get(), 1, 16);
            }
            case 17 -> { // Gold fish bowl — gold_fish_bowl_spec.md: "Gold Fish" spawner at
                         // (+2, +6, +2) (orig GD:2480-2487), glass bowl.
                requireSpawnerAt(helper, level, pos.offset(2, 6, 2), ModEntities.GOLD_FISH.get(),
                        "outcome 17 gold fish spawner");
                Scan s = scan(level, pos, Set.of(Blocks.GLASS), 35);
                requireBlock(helper, s, Blocks.GLASS, 17);
            }
            case 18 -> { // Ender reaper graveyard — graveyard_spec.md: 4 "Ender Reaper"
                         // spawners (orig GD:2523-2554), obsidian graves.
                Scan s = scan(level, pos, Set.of(Blocks.OBSIDIAN), 35);
                requireSpawner(helper, s, ModEntities.ENDER_REAPER.get(), 4, 18);
                requireBlock(helper, s, Blocks.OBSIDIAN, 18);
            }
            case 19 -> { // Spit bug lair — spit_bug_lair_spec.md: emerald-ore antenna,
                         // 3 Spit Bug spawners (SpitBugLairGenerator:76).
                Scan s = scan(level, pos, Set.of(Blocks.EMERALD_ORE), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_SPIT_BUG.get(), 3, 19);
                requireBlock(helper, s, Blocks.EMERALD_ORE, 19);
            }
            case 20 -> { // Igloo — IglooGenerator:167-173 (orig GD:2746-2760): int-offset
                         // spawners rat/ghost/ghost-skelly + snow dome. Detail test: dsb_igloo.
                requireSpawnerAt(helper, level, pos.offset(2, 1, -4), ModEntities.ENTITY_RAT.get(),
                        "outcome 20 rat spawner");
                requireSpawnerAt(helper, level, pos.offset(-1, 1, 1), ModEntities.GHOST.get(),
                        "outcome 20 ghost spawner");
                requireSpawnerAt(helper, level, pos.offset(3, 1, 4), ModEntities.GHOST_SKELLY.get(),
                        "outcome 20 ghost pumpkin skelly spawner");
                Scan s = scan(level, pos, Set.of(Blocks.SNOW_BLOCK), 35);
                requireBlock(helper, s, Blocks.SNOW_BLOCK, 20);
            }
            case 21 -> { // Generic dungeon — GenericDungeon.makeDungeon (orig GD:97-185): the
                         // mossy floor course (orig GD:110-115) is overwritten AT THE EDGES by
                         // the k=0 / i=0 wall loops, whose j loop starts at 0 and rolls each
                         // cell 50/50 mossy-or-plain cobblestone via setThisBlock (orig
                         // GD:67-73,122-136) — so the corner is RANDOM, and only interior
                         // floor cells (i,k in 1..10) are deterministically mossy.
                BlockState corner21 = level.getBlockState(pos);
                helper.assertTrue(corner21.is(Blocks.MOSSY_COBBLESTONE)
                                || corner21.is(Blocks.COBBLESTONE),
                        "outcome 21 corner: 50/50 mossy/cobble wall roll (orig GD:67-73,122-136)");
                requireBlockAt(helper, level, pos.offset(1, 0, 1), Blocks.MOSSY_COBBLESTONE,
                        "outcome 21 interior floor cell always mossy (orig GD:110-115)");
                helper.assertTrue(level.getBlockState(pos.offset(6, 1, 6)).is(Blocks.SPAWNER),
                        "outcome 21: 12-mob-ladder spawner at (6,1,6)");
            }
            case 22 -> { // Ruby dungeon — orig RubyBirdDungeon.java:30-82: fixed Ruby Bird
                         // spawner at (5,1,5).
                requireSpawnerAt(helper, level, pos.offset(5, 1, 5), ModEntities.RUBY_BIRD.get(),
                        "outcome 22 ruby bird spawner");
            }
            case 23 -> { // Basilisk maze — BasiliskMazeGenerator (basilisk_maze_spec.md,
                         // orig BasiliskMaze.java:305-419): bedrock shaft/shell + 3 persistent
                         // Basilisks, RTP traps; deepest write −33 → scan down 45.
                Scan s = scan(level, pos, Set.of(Blocks.BEDROCK, ModBlocks.BLOCK_TELEPORT.get()), 45);
                requireBlock(helper, s, Blocks.BEDROCK, 23);
                helper.assertTrue(countEntities(level, pos, ModEntities.BASILISK.get()) >= 1,
                        "outcome 23: expected persistent Basilisks in the maze chamber");
            }
            case 24 -> { // Hospital — HospitalGenerator:177-186 (orig GD:2815-2991): iron-bar
                         // cage, Ender Reaper + PitchBlack spawners, 4 End Crystals, NO dragon.
                Scan s = scan(level, pos, Set.of(Blocks.IRON_BARS), 35);
                requireSpawner(helper, s, ModEntities.ENDER_REAPER.get(), 1, 24);
                requireSpawner(helper, s, ModEntities.PITCH_BLACK.get(), 1, 24);
                requireBlock(helper, s, Blocks.IRON_BARS, 24);
                helper.assertTrue(countEntities(level, pos, EntityType.END_CRYSTAL) >= 4,
                        "outcome 24: expected 4 End Crystals (HospitalGenerator:177-186)");
                helper.assertTrue(countEntities(level, pos, EntityType.ENDER_DRAGON) == 0,
                        "outcome 24: hospital must NOT spawn an Ender Dragon (spec A2)");
            }
            case 25 -> { // Crystal haunted house — CrystalStructures:735-737 (orig GD:2993-3104):
                         // Rat/Ghost/GhostSkelly spawner stack at (0, +1..+3, 0).
                requireSpawnerAt(helper, level, pos.offset(0, 1, 0), ModEntities.ENTITY_RAT.get(),
                        "outcome 25 rat spawner");
                requireSpawnerAt(helper, level, pos.offset(0, 2, 0), ModEntities.GHOST.get(),
                        "outcome 25 ghost spawner");
                requireSpawnerAt(helper, level, pos.offset(0, 3, 0), ModEntities.GHOST_SKELLY.get(),
                        "outcome 25 ghost skelly spawner");
            }
            case 26 -> { // Bouncy castle — BouncyCastleGenerator:140-146 (bouncy_castle_spec.md):
                         // lavafoam shell + Silverfish/Rat/Scorpion spawners.
                Scan s = scan(level, pos, Set.of(ModBlocks.LAVAFOAM.get()), 35);
                requireBlock(helper, s, ModBlocks.LAVAFOAM.get(), 26);
                requireSpawner(helper, s, ModEntities.ENTITY_RAT.get(), 1, 26);
                requireSpawner(helper, s, ModEntities.ENTITY_SCORPION.get(), 1, 26);
                requireSpawner(helper, s, EntityType.SILVERFISH, 1, 26);
            }
            case 27 -> { // Ender castle — EnderCastleGenerator (ender_castle_spec.md §16/§21:
                         // Ender Reaper/Knight rooftop + pit spawners, CaveFisher alcoves).
                Scan s = scan(level, pos, Set.of(Blocks.OBSIDIAN), 35);
                requireSpawner(helper, s, ModEntities.ENDER_REAPER.get(), 1, 27);
                requireSpawner(helper, s, ModEntities.ENDER_KNIGHT.get(), 1, 27);
                requireSpawner(helper, s, ModEntities.CAVE_FISHER.get(), 1, 27);
                requireBlock(helper, s, Blocks.OBSIDIAN, 27);
            }
            case 28 -> { // Damsel in distress — DamselInDistressGenerator:263
                         // (damsel_in_distress_spec.md §2.6, orig GD:3712-3732): 2 Scorpion
                         // spawners + a live Girlfriend.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_SCORPION.get(), 2, 28);
                helper.assertTrue(countEntities(level, pos, ModEntities.GIRLFRIEND.get()) >= 1,
                        "outcome 28: expected the caged Girlfriend entity");
            }
            case 29 -> { // Inca pyramid — inca_pyramid_spec.md: Molenoid temple spawner
                         // (orig GD:3938-3942).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_MOLENOID.get(), 1, 29);
            }
            case 30 -> { // Robot lab — recentring cancelled: RDS passes pos.offset(5,0,25) and
                         // generateRobotLab re-anchors ox=x-5, oz=z-25 (LDP:1066-1067,
                         // robot_lab_audit_spec.md §18 item 10; dsb_sweep_spec.md F7 — ITEM-067),
                         // so the original's corner lands EXACTLY at the clicked pos: quartz
                         // entry-hall floor corner at pos, iron center strip at (4..5, 0, 0)
                         // (LDP:1069-1093, orig GD:4053-4075).
                requireBlockAt(helper, level, pos, Blocks.QUARTZ_BLOCK,
                        "outcome 30 entry hall floor corner AT the clicked pos");
                requireBlockAt(helper, level, pos.offset(4, 0, 0), Blocks.IRON_BLOCK,
                        "outcome 30 iron floor strip i=width/2-1");
                requireBlockAt(helper, level, pos.offset(5, 0, 0), Blocks.IRON_BLOCK,
                        "outcome 30 iron floor strip i=width/2");
                Scan s = scan(level, pos, Set.of(), 35);
                helper.assertTrue(
                        count(s, ModEntities.ROBOT_5.get()) + count(s, ModEntities.ROBOT_2.get())
                                + count(s, ModEntities.ROBOT_4.get()) >= 1,
                        "outcome 30: expected Robo spawners (LDP:1167-1340)");
            }
            case 31 -> { // King altar — RDS passes pos.offset(25,0,25); generateRoyalAltar centres
                         // ox=cposx-25 (LDP:1941-1942), so the original's SW pad corner lands at
                         // the clicked pos (orig GD:4353-4405): 51x51 grass pad from pos, quartz
                         // ceiling plate at +48 (king), cleared envelope outside the pad.
                requireBlockAt(helper, level, pos, Blocks.GRASS_BLOCK,
                        "outcome 31 pad SW corner AT the clicked pos");
                requireBlockAt(helper, level, pos.offset(50, 0, 50), Blocks.GRASS_BLOCK,
                        "outcome 31 pad NE corner (51x51, orig GD:4373-4376)");
                helper.assertTrue(level.getBlockState(pos.offset(-1, 0, -1)).isAir(),
                        "outcome 31: cell outside the pad must be cleared air (orig GD:4364-4371)"
                                + " — a shifted (uncorrected) build would put grass here");
                requireBlockAt(helper, level, pos.offset(25, 48, 25), Blocks.QUARTZ_BLOCK,
                        "outcome 31 king ceiling plate (LDP:1988-1993)");
            }
            case 32 -> { // Leonopteryx nest — LDP:1912 (orig GD:4677-4729): spawner at cposy-6.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.LEONOPTERYX.get(), 1, 32);
            }
            case 33 -> { // Crystal battle tower — CrystalStructures:916-931 (orig GD port):
                         // Rat/DungeonBeast/Urchin/Rotator/Vortex floor spawner pairs.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_VORTEX.get(), 1, 33);
                requireSpawner(helper, s, ModEntities.DUNGEON_BEAST.get(), 1, 33);
            }
            case 34 -> { // Cephadrome altar — CephadromeAltarGenerator:24-36,77-82
                         // (cephadrome_altar_spec.md): eye-of-ender blocks + extreme torches,
                         // faithfully NO spawner, NO chest.
                Scan s = scan(level, pos,
                        Set.of(ModBlocks.BLOCK_EYE_OF_ENDER.get(), ModBlocks.EXTREME_TORCH.get()), 35);
                requireBlock(helper, s, ModBlocks.BLOCK_EYE_OF_ENDER.get(), 34);
                requireBlock(helper, s, ModBlocks.EXTREME_TORCH.get(), 34);
                helper.assertTrue(s.spawners().isEmpty(),
                        "outcome 34: the altar is a summoning pad — no spawners (spec S/no-spawner)");
            }
            case 35 -> { // Girlfriend island — GirlfriendIslandGenerator (girlfriend_island_spec.md:
                         // four FIXED spawner names, zero random draws): Girlfriend / Boyfriend /
                         // Gold Fish x2.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.GIRLFRIEND.get(), 1, 35);
                requireSpawner(helper, s, ModEntities.BOYFRIEND.get(), 1, 35);
                requireSpawner(helper, s, ModEntities.GOLD_FISH.get(), 2, 35);
            }
            case 36 -> { // Greenhouse — RDS passes pos.offset(11,0,7); generateGreenhouse centres
                         // ox=cposx-11, oz=cposz-7 (LDP:852-863), so the original's SW corner is
                         // the clicked pos: grass floor corner at pos, glass wall above it
                         // (LDP:874-900); Triffid spawners above the roof (LDP:949-950; WGEN-063).
                requireBlockAt(helper, level, pos, Blocks.GRASS_BLOCK,
                        "outcome 36 greenhouse floor corner AT the clicked pos");
                requireBlockAt(helper, level, pos.offset(0, 1, 0), Blocks.GLASS,
                        "outcome 36 greenhouse glass wall corner");
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_TRIFFID.get(), 2, 36);
            }
            case 37 -> { // Monster island — MonsterIslandGenerator:84,126
                         // (hospital_monster_island_spec.md): 4 canopy spawners sharing ONE
                         // rolled id per build.
                Scan s = scan(level, pos, Set.of(), 35);
                helper.assertTrue(s.spawners().size() >= 4,
                        "outcome 37: expected 4 canopy spawners, found " + s.spawners().size());
                long distinct = s.spawners().stream().map(SpawnerHit::id).distinct().count();
                helper.assertTrue(distinct == 1,
                        "outcome 37: all monster-island spawners share one rolled id, found "
                                + distinct + " ids");
            }
            case 38 -> { // Nightmare rookery — NightmareRookeryGenerator (d5 nightmare_spec.md,
                         // orig GD:5242-5312): chest at spike y19 with a Nightmare(PitchBlack)
                         // spawner directly on top at y20.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.PITCH_BLACK.get(), 1, 38);
                boolean paired = s.spawners().stream().anyMatch(
                        hit -> level.getBlockState(hit.pos().below()).is(Blocks.CHEST));
                helper.assertTrue(paired,
                        "outcome 38: expected the chest-with-spawner-on-top pairing");
            }
            case 39 -> { // Stinky house — StinkyHouseGenerator (stinky_house_spec.md):
                         // Stink Bug + Stinky spawners.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_STINK_BUG.get(), 1, 39);
                requireSpawner(helper, s, ModEntities.ENTITY_STINKY.get(), 1, 39);
            }
            case 40 -> { // Rubber ducky pond — RubberDuckyPondGenerator
                         // (rubber_ducky_pond_spec.md: Rubber Ducky spawner pair at +6).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_RUBBER_DUCKY.get(), 1, 40);
            }
            case 41 -> { // White house — RDS passes pos.offset(12,0,9); generateWhiteHouse centres
                         // ox=cposx-12, oz=cposz-9 (LDP:1423-1424), so the original's anchor is
                         // the clicked pos: fountain quartz corner at (-5, 0, -15) and its
                         // glowstone at (-2, 1, -13) (makeFountain, LDP:1426,1436-1450);
                         // criminal (BandP) spawners (LDP:1612-1615).
                requireBlockAt(helper, level, pos.offset(-5, 0, -15), Blocks.QUARTZ_BLOCK,
                        "outcome 41 fountain corner anchored to the clicked pos");
                requireBlockAt(helper, level, pos.offset(-2, 1, -13), Blocks.GLOWSTONE,
                        "outcome 41 fountain glowstone (j==1, i==3, k==2)");
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.BAND_P.get(), 1, 41);
            }
            case 42 -> { // Queen altar — same +25/+25 correction as the King altar; obsidian
                         // ceiling plate for the queen variant (LDP:1947-1949,1988-1993).
                requireBlockAt(helper, level, pos, Blocks.GRASS_BLOCK,
                        "outcome 42 pad SW corner AT the clicked pos");
                requireBlockAt(helper, level, pos.offset(50, 0, 50), Blocks.GRASS_BLOCK,
                        "outcome 42 pad NE corner");
                requireBlockAt(helper, level, pos.offset(25, 48, 25), Blocks.OBSIDIAN,
                        "outcome 42 queen ceiling plate");
            }
            case 43 -> { // Frog pond — built at clickedY+1 (orig DSB:183); "Frog" spawner at
                         // (0, +2, 0) of the pond origin (frog_pond_spec.md, orig GD:6020-6024)
                         // = pos.offset(0, 3, 0).
                requireSpawnerAt(helper, level, pos.offset(0, 3, 0), ModEntities.FROG.get(),
                        "outcome 43 frog spawner (clickedY+1 then +2)");
            }
            case 44 -> { // Pumpkin — built at clickedY+1 (orig DSB:186); orange terracotta shell,
                         // netherrack candle, Ghost Pumpkin Skelly spawners
                         // (PumpkinGenerator:83-87, pumpkin_spec.md §2.7, orig GD:6168-6181).
                Scan s = scan(level, pos, Set.of(Blocks.ORANGE_TERRACOTTA, Blocks.NETHERRACK), 35);
                requireSpawner(helper, s, ModEntities.GHOST_SKELLY.get(), 1, 44);
                requireBlock(helper, s, Blocks.ORANGE_TERRACOTTA, 44);
                requireBlock(helper, s, Blocks.NETHERRACK, 44);
            }
            case 45 -> { // Round rotator — built at clickedY+1 (orig DSB:189); Rotator x4 +
                         // Dungeon Beast x4 spawner cross (CrystalStructures:805-812,
                         // orig GD:6184-6258).
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_ROTATOR.get(), 4, 45);
                requireSpawner(helper, s, ModEntities.DUNGEON_BEAST.get(), 4, 45);
            }
            case 46 -> { // Rainbow — RainbowGenerator:128-135 (rainbow_spec.md): 8-colour
                         // terracotta arches (pink outermost) + 6 Cloud Shark spawners
                         // (orig GD:6348-6377).
                Scan s = scan(level, pos, Set.of(Blocks.PINK_TERRACOTTA), 35);
                requireSpawner(helper, s, ModEntities.CLOUD_SHARK.get(), 4, 46);
                requireBlock(helper, s, Blocks.PINK_TERRACOTTA, 46);
            }
            case 47 -> { // Queen tower — generateChallengeTower(queen): corner Lurking Terror
                         // spawners (LDP:2277-2278,2333-2336) + RTP traps.
                Scan s = scan(level, pos, Set.of(ModBlocks.BLOCK_TELEPORT.get()), 35);
                requireSpawner(helper, s, ModEntities.ENTITY_LURKING_TERROR.get(), 1, 47);
                requireBlock(helper, s, ModBlocks.BLOCK_TELEPORT.get(), 47);
            }
            case 48 -> { // Spider hangout — SpiderHangoutGenerator:152,197
                         // (spider_hangout_spec.md): 12 Spider Driver spawners + a persistent
                         // Robot Spider entity.
                Scan s = scan(level, pos, Set.of(), 35);
                requireSpawner(helper, s, ModEntities.SPIDER_DRIVER.get(), 4, 48);
                helper.assertTrue(countEntities(level, pos, ModEntities.SPIDER_ROBOT.get()) >= 1,
                        "outcome 48: expected the persistent Robot Spider");
            }
            case 49 -> { // Red ant hangout — RedAntHangoutGenerator:126-160
                         // (red_ant_hangout_spec.md): gravel pad + red-ant nest corner cells +
                         // persistent unowned Robot Red Ant at (+8, +1, +8); NO spawners.
                Scan s = scan(level, pos, Set.of(Blocks.GRAVEL, ModBlocks.RED_ANT_BLOCK.get()), 35);
                requireBlock(helper, s, Blocks.GRAVEL, 49);
                requireBlock(helper, s, ModBlocks.RED_ANT_BLOCK.get(), 49);
                helper.assertTrue(countEntities(level, pos, ModEntities.ANT_ROBOT.get()) >= 1,
                        "outcome 49: expected the persistent Robot Red Ant");
            }
            default -> helper.fail("no signature defined for DSB outcome " + type);
        }
    }

    // ---------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------

    /**
     * ITEM-020 fuse (items i129/i143/i150/i163/i171 share this mechanism):
     * a placed {@code orespawn:random_dungeon_block} detonates after the real
     * 400-tick countdown (RandomDungeonSpawnerBlockEntity.TOTAL_DELAY = 400,
     * orig DungeonSpawnerBlock.java:39 scheduleBlockUpdate 400; block+above →
     * air at DSB:50-51). Asserted via the block itself: still present at tick
     * 380, gone once the fuse fires. Runs in its OWN batch because the rolled
     * outcome builds a random structure into the arena around this test.
     */
    @GameTest(template = "empty_large", batch = "orespawnDsbFuse", timeoutTicks = 520)
    public void dsb_fuse_400_ticks(GameTestHelper helper) {
        BlockPos rel = new BlockPos(24, 2, 24);
        helper.setBlock(rel, ModBlocks.RANDOM_DUNGEON_BLOCK.get());
        // Before the fuse ends the block must still exist (a premature build
        // would clear it): 380 < 400 with margin for the placement tick.
        helper.runAtTickTime(380, () ->
                helper.assertBlockPresent(ModBlocks.RANDOM_DUNGEON_BLOCK.get(), rel));
        // Fires at ~400 ticks; no structure in the 50-outcome pool ever writes
        // the spawner block back, so absence == detonation.
        helper.succeedWhen(() ->
                helper.assertBlockNotPresent(ModBlocks.RANDOM_DUNGEON_BLOCK.get(), rel));
    }

    /**
     * i129 (ITEM-020, D5b): DSB outcomes now include King tower (2), Basilisk
     * maze (23), Nightmare rookery (38), Queen tower (47) at the block
     * position (RandomDungeonSpawnerBlockEntity.java cases; orig
     * DungeonSpawnerBlock.java:59-61/122-124/167-169/194-196). K=904.
     */
    @GameTest(template = "empty")
    public void dsb_item020_towers_maze_rookery(GameTestHelper helper) {
        int[] types = {2, 23, 38, 47};
        for (int i = 0; i < types.length; i++) {
            buildAndAssert(helper, region(helper, 904, i), types[i]);
        }
        helper.succeed();
    }

    /**
     * i143 (D6a): DSB produces fairy tree (0), fairy castle tree (1), kyuubi
     * dungeon (7), hospital (24), ender castle (27), inca pyramid (29), robot
     * lab (30), monster island (37) — orig DungeonSpawnerBlock.java:53-58,
     * 74-76, 125-127, 134-136, 140-145, 164-166. K=905.
     */
    @GameTest(template = "empty")
    public void dsb_d6a_strong_models(GameTestHelper helper) {
        int[] types = {0, 1, 7, 24, 27, 29, 30, 37};
        for (int i = 0; i < types.length; i++) {
            buildAndAssert(helper, region(helper, 905, i), types[i]);
        }
        helper.succeed();
    }

    /**
     * i150 (D6b batches 1-2): DSB produces types 3/12/13/14/15/16/17/18/19/20/34
     * (rotator station, play pool, water-dragon lair, cloud cluster, leaf
     * tower, mini dungeon, gold fish bowl, graveyard, spit-bug lair, igloo,
     * cephadrome altar) — orig DungeonSpawnerBlock.java:62-64, 89-115, 155-157.
     * K=906.
     */
    @GameTest(template = "empty")
    public void dsb_d6b_mechanical(GameTestHelper helper) {
        int[] types = {3, 12, 13, 14, 15, 16, 17, 18, 19, 20, 34};
        for (int i = 0; i < types.length; i++) {
            buildAndAssert(helper, region(helper, 906, i), types[i]);
        }
        helper.succeed();
    }

    /**
     * i163 (D6b batch 3): DSB additionally produces types 26/28/35/39/44/46
     * (bouncy castle, damsel cottage, girlfriend island, stinky house, pumpkin
     * at +1Y, rainbow) — orig DungeonSpawnerBlock.java:131-133, 137-139,
     * 158-160, 170-172, 185-187, 191-193; type 44 carries the original's
     * clickedY+1 (RDS TYPE_PUMPKIN case). K=907.
     */
    @GameTest(template = "empty")
    public void dsb_batch3_outcomes(GameTestHelper helper) {
        int[] types = {26, 28, 35, 39, 44, 46};
        for (int i = 0; i < types.length; i++) {
            buildAndAssert(helper, region(helper, 907, i), types[i]);
        }
        helper.succeed();
    }

    /**
     * i171 (D6b batch 4): the DSB produces ALL 50 original outcomes (orig
     * DungeonSpawnerBlock.java:52-202 — one nextInt(50) roll, one builder per
     * index; dsb_sweep_spec.md §0 baseline + §A/§B wiring). Types 43/44/45 are
     * the only clickedY+1 outliers (DSB:183/186/189); the royal altars (31/42),
     * greenhouse (36), white house (41) and robot lab (30) carry the
     * group-A pre-offsets that cancel the port generators' recentring so each
     * build lands exactly at the clicked position (dsb_sweep_spec.md output
     * table; asserted positionally inside the signature table). K=908,
     * one 512-spaced row per type.
     */
    @GameTest(template = "empty")
    public void dsb_full_sweep_all_50(GameTestHelper helper) {
        for (int type = 0; type < 50; type++) {
            buildAndAssert(helper, region(helper, 908, type), type);
        }
        helper.succeed();
    }

    /**
     * i175 (regression, ITEM-067): a Robot Lab rolled from the DSB builds
     * anchored at the block position, not shifted (−5, −25). The RDS case
     * pre-offsets pos by (+5, 0, +25) (RandomDungeonSpawnerBlockEntity
     * TYPE_ROBOT_LAB case, dsb_sweep_spec.md F7) to cancel generateRobotLab's
     * internal ox=x−5 / oz=z−25 recentring (LegacyDungeonPiece.java:1066-1067;
     * robot_lab_audit_spec.md §18 item 10). With the fix, the original's
     * corner-anchored entry hall (orig GenericDungeon.java:4053-4075) starts
     * exactly at the clicked pos: quartz floor corner at pos and the iron
     * centre strip at (width/2−1..width/2, 0, 0) = (+4..+5, 0, 0). Under the
     * old bug those cells would be 5/25 blocks away. K=909.
     */
    @GameTest(template = "empty")
    public void dsb_recentring_robot_lab(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = region(helper, 909, 0);
        helper.assertTrue(RandomDungeonSpawnerBlockEntity.buildForType(level, pos, 30),
                "buildForType(30) must report placed");
        requireBlockAt(helper, level, pos, Blocks.QUARTZ_BLOCK,
                "ITEM-067: entry hall corner must land AT the clicked pos");
        requireBlockAt(helper, level, pos.offset(4, 0, 0), Blocks.IRON_BLOCK,
                "ITEM-067: iron walkway strip (i=4)");
        requireBlockAt(helper, level, pos.offset(5, 0, 0), Blocks.IRON_BLOCK,
                "ITEM-067: iron walkway strip (i=5)");
        // The pre-fix failure mode: anchor shifted to pos+(-5,0,-25) — that
        // cell must NOT hold the entry-hall corner quartz floor.
        helper.assertFalse(level.getBlockState(pos.offset(-5, 0, -25)).is(Blocks.QUARTZ_BLOCK),
                "ITEM-067 regression: corner must not sit at the old shifted anchor");
        helper.succeed();
    }

    /**
     * i156 (Igloo, WGEN-042 / WGEN-071): DSB type 20 only — no worldgen wiring
     * exists (IglooGenerator class Javadoc: "Worldgen placement is deliberately
     * NOT wired"; WGEN-071 is Phase E). Structure content per igloo_spec.md /
     * orig GenericDungeon.java:2698-2813: snow/ice dome courses, west oak door
     * over a plank sill (orig :2742-2745), int-offset Rat/Ghost/Ghost-Pumpkin-
     * Skelly spawners (orig :2746-2760), north-facing kit chest (meta 2, orig
     * :2761) bound to orespawn:chests/igloo whose 16 slots land independently
     * at 50% each (orig :2764-2810, moved to 16 random_chance-0.5 loot pools);
     * and the faithful float-truncation quirk — the 1x1 apex skylight exists
     * only at (+,+) origins and closes at negative origins (igloo_spec.md
     * §2.7/S5, D6b batch-2 four-quadrant float32 verification). K=910.
     */
    @GameTest(template = "empty")
    public void dsb_igloo_detail(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // -- (+,+) quadrant build --------------------------------------------
        // NOT region(910,0): the far-region convention lands at absolute
        // X ≈ −10.02M — a NEGATIVE x (wrong quadrant for this half) whose
        // magnitude also exceeds 2^23, where float32 has no half-steps, so the
        // original's verbatim (int)((float)c + cur + 0.5f) idiom (orig
        // GD:2705-2741, IglooGenerator.java:127-128,189) rounds half-to-even
        // and the shell drifts off the spec cells entirely. The quadrant
        // behavior verified by igloo_spec.md §2.7/S5 holds at small-magnitude
        // coordinates, so this half builds at an explicit positive origin
        // (+466176, 100, +50000) — the positive mirror of the negative-quadrant
        // column below, which no other suite region touches.
        BlockPos pos = new BlockPos(910 * 512 + 256, 100, 50_000);
        helper.assertTrue(RandomDungeonSpawnerBlockEntity.buildForType(level, pos, 20),
                "buildForType(20) must report placed");
        // Dome wall column at curdeg=0: snow y+1 / ice y+2 / snow y+3 at x+6
        // (orig GD:2705-2711; positive-origin rasterization).
        requireBlockAt(helper, level, pos.offset(6, 1, 0), Blocks.SNOW_BLOCK, "igloo wall snow y+1");
        requireBlockAt(helper, level, pos.offset(6, 2, 0), Blocks.ICE, "igloo wall ice y+2");
        requireBlockAt(helper, level, pos.offset(6, 3, 0), Blocks.SNOW_BLOCK, "igloo wall snow y+3");
        // West oak door over the buried plank sill (orig GD:2742-2745).
        requireBlockAt(helper, level, pos.offset(-6, 0, 0), Blocks.OAK_PLANKS, "igloo door sill");
        BlockState doorLower = level.getBlockState(pos.offset(-6, 1, 0));
        helper.assertTrue(doorLower.is(Blocks.OAK_DOOR)
                        && doorLower.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST,
                "igloo: west-facing oak door lower half (orig GD:2745, direction 2 = west)");
        // Spawners at fixed int offsets (orig GD:2746-2760).
        requireSpawnerAt(helper, level, pos.offset(2, 1, -4), ModEntities.ENTITY_RAT.get(),
                "igloo rat spawner");
        requireSpawnerAt(helper, level, pos.offset(-1, 1, 1), ModEntities.GHOST.get(),
                "igloo ghost spawner");
        requireSpawnerAt(helper, level, pos.offset(3, 1, 4), ModEntities.GHOST_SKELLY.get(),
                "igloo ghost pumpkin skelly spawner");
        // North-facing kit chest bound to the igloo loot table (orig GD:2761).
        BlockPos chestPos = pos.offset(-3, 1, -3);
        BlockState chest = level.getBlockState(chestPos);
        helper.assertTrue(chest.is(Blocks.CHEST)
                        && chest.getValue(ChestBlock.FACING) == Direction.NORTH,
                "igloo: kit chest faces north (orig meta 2, spec S9)");
        helper.assertTrue(level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity c
                        && c.getLootTable() != null
                        && c.getLootTable().location().toString().equals("orespawn:chests/igloo"),
                "igloo: chest bound to orespawn:chests/igloo");
        // Apex skylight OPEN at (+,+): the r=1 ice ring surrounds but never
        // covers (0,0) (igloo_spec.md §2.3; curdeg=0 cell lands at x+1).
        helper.assertTrue(level.getBlockState(pos.offset(0, 5, 0)).isAir(),
                "igloo: 1x1 apex skylight must stay OPEN at a (+,+) origin");
        requireBlockAt(helper, level, pos.offset(1, 5, 0), Blocks.ICE,
                "igloo: r=1 cap ring cell beside the apex");

        // -- negative quadrant: apex closes (faithful float drift) -----------
        // Mirrored column no other suite region uses: X and Z both negative.
        BlockPos neg = new BlockPos(-(910 * 512 + 256), 100, -50_000);
        helper.assertTrue(RandomDungeonSpawnerBlockEntity.buildForType(level, neg, 20),
                "buildForType(20) at negative coords must report placed");
        helper.assertFalse(level.getBlockState(neg.offset(0, 5, 0)).isAir(),
                "igloo: apex must CLOSE at negative-coordinate origins "
                        + "(toward-zero float truncation, igloo_spec.md S5)");
        // Int-offset spawners do NOT drift with the shell (spec §2.5/S5).
        requireSpawnerAt(helper, level, neg.offset(-1, 1, 1), ModEntities.GHOST.get(),
                "igloo (negative origin): ghost spawner keeps its int offset");

        // -- DSB-only: no worldgen structure/structure-set registration ------
        helper.assertTrue(level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                        .keySet().stream().noneMatch(k -> k.getPath().contains("igloo")),
                "igloo must have NO structure registration (WGEN-071 open, Phase E)");
        helper.assertTrue(level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)
                        .keySet().stream().noneMatch(k -> k.getPath().contains("igloo")),
                "igloo must have NO structure-set registration (WGEN-071 open, Phase E)");

        // -- 16 independent 50% chest slots (statistical) --------------------
        // 30 igloos → 480 Bernoulli(0.5) slot rolls; filled-slot total is
        // Binomial(480, 0.5), mean 240. Bound [140, 340] (|dev| = 100):
        // Hoeffding P(|X-240| >= 100) <= 2*exp(-2*100^2/480) = 2*exp(-41.7)
        // ≈ 1.6e-18 < 1e-9. Per-chest max is 16 (16 pools, 1 stack each).
        Player looter = helper.makeMockPlayer(GameType.SURVIVAL);
        int filled = 0;
        for (int i = 0; i < 30; i++) {
            BlockPos p = region(helper, 910, 1).offset(0, 0, i * 64);
            RandomDungeonSpawnerBlockEntity.buildForType(level, p, 20);
            BlockEntity be = level.getBlockEntity(p.offset(-3, 1, -3));
            helper.assertTrue(be instanceof RandomizableContainerBlockEntity,
                    "igloo #" + i + ": kit chest present");
            RandomizableContainerBlockEntity c = (RandomizableContainerBlockEntity) be;
            c.unpackLootTable(looter);
            int slots = 0;
            for (int slot = 0; slot < c.getContainerSize(); slot++) {
                if (!c.getItem(slot).isEmpty()) slots++;
            }
            helper.assertTrue(slots <= 16,
                    "igloo chest rolls at most its 16 fixed kit slots, got " + slots);
            filled += slots;
        }
        helper.assertTrue(filled >= 140 && filled <= 340,
                "igloo chest slots: expected ~240/480 at 50% each, got " + filled);
        helper.succeed();
    }
}
