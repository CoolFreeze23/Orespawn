package danger.orespawn.block.entity;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.world.GenericDungeon;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Countdown BlockEntity for {@link danger.orespawn.block.RandomDungeonSpawnerBlock}.
 *
 * <p>Strategy:
 * <ol>
 *   <li>Tick 0 → 400: emit firework-spark particles for visual feedback
 *       (orig DungeonSpawnerBlock.java:35-40 schedules the build 400 ticks
 *       after placement — ITEM-020).</li>
 *   <li>Tick 400: remove self + the block above (orig :50-51), roll
 *       {@code nextInt(50)} against the table-driven structure pool
 *       (orig :52-202 — 50 outcomes, FairyTree → RedAntHangout).</li>
 * </ol>
 *
 * <p>Pool status: ALL 50 outcomes are wired (D6b batch 4 — ITEM-020
 * complete). Each case is keyed by the ORIGINAL index with its orig
 * DungeonSpawnerBlock.java block cite; types 43/44/45 carry the original's
 * {@code clickedY + 1}. The default arm is unreachable defensive fallback
 * only.</p>
 *
 * <p>Persisted across save/load via {@code Delay} NBT, so chunk unload + reload
 * during the countdown doesn't reset the timer.</p>
 */
public class RandomDungeonSpawnerBlockEntity extends BlockEntity {
    // orig DungeonSpawnerBlock.java:39 — world.scheduleBlockUpdate(..., 400)
    private static final int TOTAL_DELAY = 400; // 20 seconds at 20 TPS
    // orig DungeonSpawnerBlock.java:52 — nextInt(50) outcome roll
    private static final int STRUCTURE_POOL_SIZE = 50;
    // orig DungeonSpawnerBlock.java:53-55 — OreSpawnTrees.FairyTree
    private static final int TYPE_FAIRY_TREE = 0;
    // orig DungeonSpawnerBlock.java:62-64 — makeRotatorStation
    private static final int TYPE_ROTATOR_STATION = 3;
    // orig DungeonSpawnerBlock.java:89-91 — makePlayPool
    private static final int TYPE_PLAY_POOL = 12;
    // orig DungeonSpawnerBlock.java:95-97 — makeCloudSharkDungeon
    private static final int TYPE_CLOUD_SHARK_DUNGEON = 14;
    // orig DungeonSpawnerBlock.java:104-106 — makeGoldFishBowl
    private static final int TYPE_GOLD_FISH_BOWL = 17;
    // orig DungeonSpawnerBlock.java:110-112 — makeSpitBugLair
    private static final int TYPE_SPIT_BUG_LAIR = 19;
    // orig DungeonSpawnerBlock.java:92-94 — makeWaterDragonLair
    private static final int TYPE_WATER_DRAGON_LAIR = 13;
    // orig DungeonSpawnerBlock.java:98-100 — makeLeafMonsterDungeon
    private static final int TYPE_LEAF_MONSTER_DUNGEON = 15;
    // orig DungeonSpawnerBlock.java:101-103 — makeMiniDungeon
    private static final int TYPE_MINI_DUNGEON = 16;
    // orig DungeonSpawnerBlock.java:107-109 — makeEnderReaperGraveyard
    private static final int TYPE_ENDER_REAPER_GRAVEYARD = 18;
    // orig DungeonSpawnerBlock.java:113-115 — makeIgloo
    private static final int TYPE_IGLOO = 20;
    // orig DungeonSpawnerBlock.java:155-157 — makeCephadromeAltar
    private static final int TYPE_CEPHADROME_ALTAR = 34;
    // orig DungeonSpawnerBlock.java:56-58 — OreSpawnTrees.FairyCastleTree
    private static final int TYPE_FAIRY_CASTLE_TREE = 1;
    // orig DungeonSpawnerBlock.java:59-61 — makeEnormousCastle (King tower)
    private static final int TYPE_ENORMOUS_CASTLE_KING = 2;
    // orig DungeonSpawnerBlock.java:74-76 — makeKyuubiDungeon
    private static final int TYPE_KYUUBI_DUNGEON = 7;
    // orig DungeonSpawnerBlock.java:125-127 — makeEnderDragonHospital (single call;
    // MonsterIsland is NOT paired here — it is its own type 37, DSB:164-166)
    private static final int TYPE_HOSPITAL = 24;
    // orig DungeonSpawnerBlock.java:134-136 — makeEnderCastle
    private static final int TYPE_ENDER_CASTLE = 27;
    // orig DungeonSpawnerBlock.java:140-142 — makeIncaPyramid
    private static final int TYPE_INCA_PYRAMID = 29;
    // orig DungeonSpawnerBlock.java:143-145 — makeRobotLab
    private static final int TYPE_ROBOT_LAB = 30;
    // orig DungeonSpawnerBlock.java:164-166 — makeMonsterIsland
    private static final int TYPE_MONSTER_ISLAND = 37;
    private static final int TYPE_GENERIC_DUNGEON = 21;
    private static final int TYPE_RUBY_DUNGEON = 22;
    // orig DungeonSpawnerBlock.java:122-124 — BMaze.buildBasiliskMaze at the block pos
    private static final int TYPE_BASILISK_MAZE = 23;
    // orig DungeonSpawnerBlock.java:167-169 — MyDungeon.makeNightmareRookery
    private static final int TYPE_NIGHTMARE_ROOKERY = 38;
    // orig DungeonSpawnerBlock.java:194-196 — makeEnormousCastleQ (Queen tower)
    private static final int TYPE_ENORMOUS_CASTLE_QUEEN = 47;
    // orig DungeonSpawnerBlock.java:131-133 — makeBouncyCastle (single call)
    private static final int TYPE_BOUNCY_CASTLE = 26;
    // orig DungeonSpawnerBlock.java:137-139 — makeDamselInDistress (single call)
    private static final int TYPE_DAMSEL_IN_DISTRESS = 28;
    // orig DungeonSpawnerBlock.java:158-160 — makeGirlfriendIsland (single call)
    private static final int TYPE_GIRLFRIEND_ISLAND = 35;
    // orig DungeonSpawnerBlock.java:170-172 — makeStinkyHouse
    private static final int TYPE_STINKY_HOUSE = 39;
    // orig DungeonSpawnerBlock.java:185-187 — makePumpkin at clickedY + 1
    private static final int TYPE_PUMPKIN = 44;
    // orig DungeonSpawnerBlock.java:191-193 — makeRainbow
    private static final int TYPE_RAINBOW = 46;
    // ---- D6b batch 4: the final 19 outcomes (dsb_sweep_spec.md + the six
    // batch-4 structure specs). All 50 blocks are single-call; 43/44/45 are
    // the only clickedY+1 outliers. ----
    // orig DungeonSpawnerBlock.java:65-67 — makeBeeHive
    private static final int TYPE_BEE_HIVE = 4;
    // orig DungeonSpawnerBlock.java:68-70 — makeHauntedHouse (overworld)
    private static final int TYPE_HAUNTED_HOUSE = 5;
    // orig DungeonSpawnerBlock.java:71-73 — makeMantisHive
    private static final int TYPE_MANTIS_HIVE = 6;
    // orig DungeonSpawnerBlock.java:77-79 — makeSmallBeeHive
    private static final int TYPE_SMALL_BEE_HIVE = 8;
    // orig DungeonSpawnerBlock.java:80-82 — makeShadowDungeon
    private static final int TYPE_SHADOW_DUNGEON = 9;
    // orig DungeonSpawnerBlock.java:83-85 — makeAlienWTFDungeon
    private static final int TYPE_ALIEN_WTF_DUNGEON = 10;
    // orig DungeonSpawnerBlock.java:86-88 — makeEnderKnightDungeon
    private static final int TYPE_ENDER_KNIGHT_DUNGEON = 11;
    // orig DungeonSpawnerBlock.java:128-130 — makeCrystalHauntedHouse
    private static final int TYPE_CRYSTAL_HAUNTED_HOUSE = 25;
    // orig DungeonSpawnerBlock.java:146-148 — makeKingAltar
    private static final int TYPE_KING_ALTAR = 31;
    // orig DungeonSpawnerBlock.java:149-151 — makeLeonNest
    private static final int TYPE_LEON_NEST = 32;
    // orig DungeonSpawnerBlock.java:152-154 — makeCrystalBattleTower
    private static final int TYPE_CRYSTAL_BATTLE_TOWER = 33;
    // orig DungeonSpawnerBlock.java:161-163 — makeGreenhouseDungeon
    private static final int TYPE_GREENHOUSE_DUNGEON = 36;
    // orig DungeonSpawnerBlock.java:173-175 — makeRubberDuckyPond
    private static final int TYPE_RUBBER_DUCKY_POND = 40;
    // orig DungeonSpawnerBlock.java:176-178 — makeWhiteHouse
    private static final int TYPE_WHITE_HOUSE = 41;
    // orig DungeonSpawnerBlock.java:179-181 — makeQueenAltar
    private static final int TYPE_QUEEN_ALTAR = 42;
    // orig DungeonSpawnerBlock.java:182-184 — makeFrogPond at clickedY + 1
    private static final int TYPE_FROG_POND = 43;
    // orig DungeonSpawnerBlock.java:188-190 — makeRoundRotator at clickedY + 1
    private static final int TYPE_ROUND_ROTATOR = 45;
    // orig DungeonSpawnerBlock.java:197-199 — makeSpiderHangout
    private static final int TYPE_SPIDER_HANGOUT = 48;
    // orig DungeonSpawnerBlock.java:200-202 — makeRedAntHangout
    private static final int TYPE_RED_ANT_HANGOUT = 49;

    private int delay = TOTAL_DELAY;

    public RandomDungeonSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RANDOM_DUNGEON_SPAWNER_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RandomDungeonSpawnerBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;

        // Particle/sound feedback every 20 ticks
        if (be.delay % 20 == 0) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0.05);
            server.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.BLOCKS, 0.4f, 1.5f);
        }

        be.delay--;
        if (be.delay <= 0) {
            be.detonate(server, pos);
        }
        be.setChanged();
    }

    private void detonate(ServerLevel server, BlockPos pos) {
        // orig DungeonSpawnerBlock.java:50-51 — spawner AND the block above → air
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        server.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);

        // orig DungeonSpawnerBlock.java:52 — one roll over the 50-entry pool
        int type = server.random.nextInt(STRUCTURE_POOL_SIZE);
        boolean placed = buildForType(server, pos, type);

        if (placed) {
            server.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.BLOCKS, 1.0f, 0.7f);
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                    1, 0, 0, 0, 0);
        }
    }

    /**
     * Table-driven outcome pool keyed by the ORIGINAL type index
     * (orig DungeonSpawnerBlock.java:52-202). ALL 50 outcomes are wired
     * (D6b batch 4 — ITEM-020): each case cites its original block; the
     * group-A {@code pos.offset} calls cancel the ported generators'
     * internal recentring so the build lands exactly where the original's
     * clicked-pos build did (dsb_sweep_spec.md output table).
     */
    // Visibility widened private -> public (2026-08-10, GameTest seam): the
    // suite drives every outcome index directly instead of rolling
    // nextInt(50) four hundred times. No behavioral change; production
    // callers are unchanged (detonate above).
    public static boolean buildForType(ServerLevel server, BlockPos pos, int type) {
        return switch (type) {
            case TYPE_RUBY_DUNGEON -> GenericDungeon.placeRubyDungeonAt(server, server.random, pos);
            case TYPE_GENERIC_DUNGEON -> GenericDungeon.placeGenericDungeonAt(server, server.random, pos);
            case TYPE_BASILISK_MAZE -> {
                // orig DungeonSpawnerBlock.java:122-124 — same public builder as
                // worldgen, at the block position with no ground scan or -2 offset.
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.BASILISK_MAZE);
                yield true;
            }
            case TYPE_NIGHTMARE_ROOKERY -> {
                // orig DungeonSpawnerBlock.java:167-169.
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.NIGHTMARE_ROOKERY);
                yield true;
            }
            case TYPE_ENORMOUS_CASTLE_KING -> {
                // orig DungeonSpawnerBlock.java:59-61.
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.KING_TOWER);
                yield true;
            }
            case TYPE_ENORMOUS_CASTLE_QUEEN -> {
                // orig DungeonSpawnerBlock.java:194-196.
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.QUEEN_TOWER);
                yield true;
            }
            // Phase D6a — the six strong-model structures + the two tree outcomes.
            case TYPE_FAIRY_TREE ->
                    danger.orespawn.world.CrystalStructures.buildFairyTreeAt(server, server.random, pos);
            case TYPE_FAIRY_CASTLE_TREE ->
                    danger.orespawn.world.CrystalStructures.buildFairyCastleTreeAt(server, server.random, pos);
            case TYPE_KYUUBI_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.KYUUBI_DUNGEON);
                yield true;
            }
            case TYPE_HOSPITAL -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.HOSPITAL);
                yield true;
            }
            case TYPE_ENDER_CASTLE -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.ENDER_CASTLE);
                yield true;
            }
            case TYPE_INCA_PYRAMID -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.INCA_PYRAMID);
                yield true;
            }
            case TYPE_ROBOT_LAB -> {
                // D6b batch-4 F7 fix (dsb_sweep_spec.md): the original
                // makeRobotLab is corner-anchored at the passed pos
                // (GD:4053-4059), but generateRobotLab recentres
                // ox = x - 5, oz = z - 25 (the documented recentring,
                // robot_lab_audit_spec.md §18 item 10). Passing pos raw
                // shifted the DSB build (-5, 0, -25) from the original's
                // clicked-pos build; the +5/+25 pre-offset cancels it.
                LegacyDungeonPiece.buildNow(server, pos.offset(5, 0, 25),
                        LegacyDungeonPiece.DungeonType.ROBOT_LAB);
                yield true;
            }
            case TYPE_MONSTER_ISLAND -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.MONSTER_ISLAND);
                yield true;
            }
            // Phase D6b batch 1.
            case TYPE_ROTATOR_STATION ->
                    danger.orespawn.world.CrystalStructures.buildRotatorStationAt(server, server.random, pos);
            case TYPE_PLAY_POOL -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.PLAY_POOL);
                yield true;
            }
            case TYPE_CLOUD_SHARK_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.CLOUD_SHARK_DUNGEON);
                yield true;
            }
            case TYPE_GOLD_FISH_BOWL -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.GOLD_FISH_BOWL);
                yield true;
            }
            case TYPE_SPIT_BUG_LAIR -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.SPIT_BUG_LAIR);
                yield true;
            }
            // Phase D6b batch 2.
            case TYPE_WATER_DRAGON_LAIR -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.WATER_DRAGON_LAIR);
                yield true;
            }
            case TYPE_LEAF_MONSTER_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.LEAF_MONSTER_DUNGEON);
                yield true;
            }
            case TYPE_MINI_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.MINI_DUNGEON);
                yield true;
            }
            case TYPE_ENDER_REAPER_GRAVEYARD -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.ENDER_REAPER_GRAVEYARD);
                yield true;
            }
            case TYPE_IGLOO -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.IGLOO);
                yield true;
            }
            case TYPE_CEPHADROME_ALTAR -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.CEPHADROME_ALTAR);
                yield true;
            }
            case TYPE_BOUNCY_CASTLE -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.BOUNCY_CASTLE);
                yield true;
            }
            case TYPE_DAMSEL_IN_DISTRESS -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.DAMSEL_IN_DISTRESS);
                yield true;
            }
            case TYPE_GIRLFRIEND_ISLAND -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.GIRLFRIEND_ISLAND);
                yield true;
            }
            case TYPE_STINKY_HOUSE -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.STINKY_HOUSE);
                yield true;
            }
            case TYPE_PUMPKIN -> {
                // orig DSB:186 — makePumpkin receives clickedY + 1; the DSB
                // already cleared that cell (DSB:50-51 → the tick handler).
                LegacyDungeonPiece.buildNow(server, pos.above(),
                        LegacyDungeonPiece.DungeonType.PUMPKIN);
                yield true;
            }
            case TYPE_RAINBOW -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.RAINBOW);
                yield true;
            }
            // ---- D6b batch 4: final 19 outcomes. Group-A offsets cancel the
            // ported generators' internal recentring so the DSB build lands
            // exactly where the original's clicked-pos build did
            // (dsb_sweep_spec.md §A + output table). ----
            case TYPE_BEE_HIVE -> {
                // orig DSB:66 — makeBeeHive at the clicked pos; extracted
                // build core (BeehiveFeature.buildAt, orig GD:812-858).
                danger.orespawn.world.feature.BeehiveFeature.buildAt(
                        server, new java.util.Random(server.random.nextLong()), pos);
                yield true;
            }
            case TYPE_HAUNTED_HOUSE -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.HAUNTED_HOUSE);
                yield true;
            }
            case TYPE_MANTIS_HIVE -> {
                // orig DSB:72 — makeMantisHive; extracted core
                // (MantisNestFeature.buildAt, orig GD:1012-1062).
                danger.orespawn.world.feature.MantisNestFeature.buildAt(
                        server, new java.util.Random(server.random.nextLong()), pos);
                yield true;
            }
            case TYPE_SMALL_BEE_HIVE -> {
                // orig DSB:78 — makeSmallBeeHive; extracted core
                // (SmallBeehiveFeature.buildAt, orig GD:1363-1451).
                danger.orespawn.world.feature.SmallBeehiveFeature.buildAt(
                        server, new java.util.Random(server.random.nextLong()), pos);
                yield true;
            }
            case TYPE_SHADOW_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.SHADOW);
                yield true;
            }
            case TYPE_ALIEN_WTF_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.ALIEN_WTF);
                yield true;
            }
            case TYPE_ENDER_KNIGHT_DUNGEON -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.ENDER_KNIGHT_DUNGEON);
                yield true;
            }
            case TYPE_CRYSTAL_HAUNTED_HOUSE -> {
                // orig DSB:129 — new adapter over the private Crystal builder
                // (CrystalStructures GD:2993-3104 port).
                danger.orespawn.world.CrystalStructures.buildCrystalHauntedHouseAt(
                        server, server.random, pos);
                yield true;
            }
            case TYPE_KING_ALTAR -> {
                // orig DSB:147 — original is corner-anchored; the port
                // centres (ox = x - 25, oz = z - 25), so pre-offset +25/+25.
                LegacyDungeonPiece.buildNow(server, pos.offset(25, 0, 25),
                        LegacyDungeonPiece.DungeonType.KING_ALTAR);
                yield true;
            }
            case TYPE_LEON_NEST -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.LEONOPTERYX_NEST);
                yield true;
            }
            case TYPE_CRYSTAL_BATTLE_TOWER -> {
                // orig DSB:153 — new adapter over the faithful Crystal
                // builder (NOT the dead CrystalBattleTowerFeature, removed
                // this batch under the no-fabrication rule — F4).
                danger.orespawn.world.CrystalStructures.buildCrystalBattleTowerAt(
                        server, server.random, pos);
                yield true;
            }
            case TYPE_GREENHOUSE_DUNGEON -> {
                // orig DSB:162 — port centres (ox = x - 11, oz = z - 7).
                LegacyDungeonPiece.buildNow(server, pos.offset(11, 0, 7),
                        LegacyDungeonPiece.DungeonType.GREENHOUSE);
                yield true;
            }
            case TYPE_RUBBER_DUCKY_POND -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.RUBBER_DUCKY_POND);
                yield true;
            }
            case TYPE_WHITE_HOUSE -> {
                // orig DSB:177 — port centres (ox = x - 12, oz = z - 9).
                LegacyDungeonPiece.buildNow(server, pos.offset(12, 0, 9),
                        LegacyDungeonPiece.DungeonType.WHITE_HOUSE);
                yield true;
            }
            case TYPE_QUEEN_ALTAR -> {
                // orig DSB:180 — same centring as the King altar.
                LegacyDungeonPiece.buildNow(server, pos.offset(25, 0, 25),
                        LegacyDungeonPiece.DungeonType.QUEEN_ALTAR);
                yield true;
            }
            case TYPE_FROG_POND -> {
                // orig DSB:183 — makeFrogPond receives clickedY + 1.
                LegacyDungeonPiece.buildNow(server, pos.above(),
                        LegacyDungeonPiece.DungeonType.FROG_POND);
                yield true;
            }
            case TYPE_ROUND_ROTATOR -> {
                // orig DSB:189 — makeRoundRotator receives clickedY + 1;
                // new adapter over the private Crystal builder (GD:6184-6258).
                danger.orespawn.world.CrystalStructures.buildRoundRotatorAt(
                        server, server.random, pos.above());
                yield true;
            }
            case TYPE_SPIDER_HANGOUT -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.SPIDER_HANGOUT);
                yield true;
            }
            case TYPE_RED_ANT_HANGOUT -> {
                LegacyDungeonPiece.buildNow(server, pos,
                        LegacyDungeonPiece.DungeonType.RED_ANT_HANGOUT);
                yield true;
            }
            // All 50 outcomes are wired (D6b batch 4); this arm is
            // unreachable for nextInt(50) rolls and exists only because an
            // int-typed switch requires it. Defensive fallback unchanged.
            default -> GenericDungeon.placeGenericDungeonAt(server, server.random, pos);
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Delay", this.delay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.delay = tag.contains("Delay") ? tag.getInt("Delay") : TOTAL_DELAY;
    }
}
