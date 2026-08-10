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
 * <p>Pool status: only the orig type 21 (MyDungeon.makeDungeon → generic
 * dungeon) and type 22 (RubyDungeon.makeDungeon) builders are ported so far;
 * unported indices currently fall back to the generic dungeon. As WGEN-042
 * ports more of the 50 structures, register them in {@link #buildForType}.</p>
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
    // orig DungeonSpawnerBlock.java:71-73 — makeRotatorStation
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
     * (orig DungeonSpawnerBlock.java:52-202). Ported entries: 21 (generic
     * dungeon), 22 (ruby dungeon). Every still-unported index falls back to
     * the generic dungeon until WGEN-042 lands those structures — register
     * new builders here as they are ported.
     */
    private static boolean buildForType(ServerLevel server, BlockPos pos, int type) {
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
                LegacyDungeonPiece.buildNow(server, pos,
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
            // Interim fallback for the not-yet-ported structures (Phase D / WGEN-042)
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
