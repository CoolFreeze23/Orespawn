package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Spider Hangout (Village dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeSpiderHangout} (orig GenericDungeon.java:6989-7043)
 * &mdash; a flat 20&times;20 gravel arena on a stone foundation slab, carved
 * 19 blocks high into the terrain, with a 3-high column of Spider Driver
 * spawners at each of the four pad corners (12 spawners total) and one live
 * rideable Robot Spider parked at the pad centre. No chest, no walls, no
 * roof, no lighting.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/spider_hangout_spec.md}):</p>
 * <ul>
 * <li><b>No loot is the design (spec S2):</b> the original places no chest
 *     and fills no container anywhere in the method &mdash; the only
 *     "reward" is the rideable Robot Spider itself. Do not add a chest,
 *     lighting, or walls.</li>
 * <li><b>Robot Spider spawn (spec &sect;4.1/S5) &mdash;
 *     {@code piece.spawnPersistent}, NOT {@code spawnEntity} &mdash; the
 *     OPPOSITE of the {@link DamselInDistressGenerator} Girlfriend ruling:</b>
 *     the original class pinned every instance at construction
 *     ({@code func_110163_bv()} in entityInit, orig SpiderRobot.java:510,
 *     plus canDespawn &rarr; false, orig :86-88), but the PORT
 *     {@code SpiderRobot} carries neither (the BUG-007 rework removed the
 *     empty NBT overrides and added no {@code removeWhenFarAway} override),
 *     so persistence must come from the spawn call &mdash; a naked
 *     {@code spawnEntity} here would produce a robot that despawns (pattern
 *     doc trap #9). The doubles in the original are casts of INTS, so the
 *     robot stands on the block CORNER at (+10, +1, +10), not the +0.5
 *     centre &mdash; transcribed exactly, not "fixed" (spec S9, the damsel
 *     precedent). Yaw is drawn unconditionally by this caller (stitching
 *     contract).</li>
 * <li><b>The robot is load-bearing for the spawners (spec S4/&sect;4.2):</b>
 *     {@code SpiderDriver}'s spawn rule passes whenever a {@code SpiderRobot}
 *     is within its bounding box inflated by (24, 12, 24) (orig
 *     SpiderDriver.java:177-184; port SpiderDriver.java:176-181), which is
 *     why an unlit daylight gravel arena spawns drivers at all. Do not
 *     relight the arena, and keep the robot spawn even though it looks
 *     decorative.</li>
 * <li><b>Spawner columns (spec S3):</b> 3-high stacks at y +1..+3 on all
 *     four pad corners, per-level placement order (0,0) &rarr; (19,19)
 *     &rarr; (19,0) &rarr; (0,19) preserved verbatim (orig :7009-7037); the
 *     fourth placement's {@code continue}-on-null (orig :7035) is cosmetic
 *     decompiler output with identical semantics. The tile-entity
 *     self-reads are absorbed by {@code piece.placeSpawner} (spec
 *     &sect;10).</li>
 * <li><b>Anchor is the grass block; origin is the MINIMUM corner (spec
 *     S8):</b> {@code addSpiderHangout} passes {@code posY - 1} (orig
 *     OreSpawnWorld.java:1332) and loop A writes gravel AT j&nbsp;=&nbsp;0,
 *     so the found grass block becomes the pad's corner gravel cell and the
 *     arena sits flush with grade at the anchor column. The footprint
 *     extends only into +X/+Z (contrast most centred GD builders). That
 *     scan, plus the 12&times;12 {@code quickSpaceCheck} clearance plane
 *     (orig OreSpawnWorld.java:2625-2633), is
 *     {@code PlacementMode.VILLAGE_GRASS_SURFACE} (spec &sect;7); the
 *     {@code SpiderDriverEnable} worldgen gate (orig OreSpawnWorld.java:
 *     1323-1325) lives in {@code LegacyDungeonStructure.findGenerationPoint}
 *     (spec S6).</li>
 * <li><b>RNG (spec &sect;11):</b> the geometry is fully deterministic
 *     &mdash; loop A writes unconditionally, loop B has no rolls, there is
 *     no chest. The Robot Spider yaw (orig :7040) is the builder's ONLY
 *     draw, taken unconditionally by this caller. Zero mid-build world
 *     reads beyond the spawner self-reads (spec &sect;10).</li>
 * </ul>
 */
final class SpiderHangoutGenerator {

    private SpiderHangoutGenerator() {}

    /**
     * Direct port of {@code makeSpiderHangout(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:6989-7043). {@code origin} is the grass block
     * a Village-dimension column scan anchored on (worldgen,
     * orig OreSpawnWorld.java:1332 &mdash; {@code posY - 1}, so the pad's
     * corner gravel cell REPLACES the grass, spec S8) or the Dungeon Spawner
     * Block position (type 48, orig DungeonSpawnerBlock.java:197-199 &mdash;
     * no offset, no validation, no dimension check, and no config gate: a
     * 20&times;20&times;21 volume is carved and paved at the clicked position
     * in any dimension &mdash; a floating pad or an arena punched into a
     * mountainside are both faithful). Write order is behavior and is
     * preserved verbatim: pad + clearing &rarr; 12 spawners &rarr; Robot
     * Spider.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();       // field_150350_a
        BlockState stone = Blocks.STONE.defaultBlockState();   // field_150348_b
        BlockState gravel = Blocks.GRAVEL.defaultBlockState(); // field_150351_n

        // Loop A — 20×21×20 pad + air box (orig :6995-7008). i = X, j = Y,
        // k = Z; per cell: air default, stone at j = −1 (foundation slab),
        // gravel at j = 0 (floor, replacing the anchor grass level), air
        // above carves the clearing 19 high. 8400 unconditional writes, no
        // draws, no terrain reads (spec §2.1/§10). Flag-2 writes schedule no
        // updates and every gravel cell rests on the stone slab below it, so
        // nothing falls even when later neighbor updates arrive (spec §5).
        for (int i = 0; i < 20; i++) {                         // orig :6995
            for (int j = -1; j < 20; j++) {                    // orig :6996
                for (int k = 0; k < 20; k++) {                 // orig :6997
                    BlockState blk = air;                      // orig :6998
                    if (j == -1) {
                        blk = stone;                           // orig :6999-7001
                    }
                    if (j == 0) {
                        blk = gravel;                          // orig :7002-7004
                    }
                    piece.place(cx + i, cy + j, cz + k, blk);  // orig :7005
                }
            }
        }

        // Loop B — 12 "Spider Driver" spawners: 3-high columns (y +1..+3) at
        // the four pad corners, overwriting loop-A air, standing on the
        // gravel floor (orig :7009-7037). Per-level source order preserved:
        // (0,0) → (19,19) → (19,0) → (0,19) (spec S3). Mob mapping:
        // "Spider Driver" (orig OreSpawnMain.java:4489) →
        // orespawn:spider_driver (spec §4). No RNG.
        for (int j = 1; j < 4; j++) {                          // orig :7009
            piece.placeSpawner(cx, cy + j, cz, ModEntities.SPIDER_DRIVER.get());           // orig :7010-7016
            piece.placeSpawner(cx + 19, cy + j, cz + 19, ModEntities.SPIDER_DRIVER.get()); // orig :7017-7023
            piece.placeSpawner(cx + 19, cy + j, cz, ModEntities.SPIDER_DRIVER.get());      // orig :7024-7030
            piece.placeSpawner(cx, cy + j, cz + 19, ModEntities.SPIDER_DRIVER.get());      // orig :7031-7036
        }

        // orig :7038-7042 — the parked Robot Spider at the pad's
        // centre-biased column, standing on the gravel floor. Yaw drawn
        // unconditionally by this caller; only the spawn is chunk-gated
        // (stitching contract — the builder's ONLY draw, spec §11). The
        // original's doubles are casts of INTS — it stands on the block
        // CORNER, not the +0.5 centre (spec S9, transcribed exactly).
        // spawnPersistent, NOT spawnEntity: the original class pinned itself
        // at entityInit (orig SpiderRobot.java:510) + canDespawn → false
        // (orig :86-88); the port class carries neither post-BUG-007, so the
        // flag is load-bearing here (spec S5 — the opposite of the damsel
        // Girlfriend ruling). Mob mapping: "Robot Spider" (orig
        // OreSpawnMain.java:4481) → orespawn:spider_robot (spec §4).
        // Silent variant: the original spawn is bare createEntity +
        // setLocationAndAngles + spawnEntityInWorld with NO playLivingSound
        // (orig :7038-7042 — unlike the spawnCreature pattern), so the live
        // DSB path must not play the ambient one-shot (batch-4 verify fix).
        float yaw = random.nextFloat() * 360.0f;               // orig :7040
        piece.spawnPersistent(ModEntities.SPIDER_ROBOT.get(), cx + 10, cy + 1, cz + 10, yaw, false); // orig :7038-7042
    }
}
