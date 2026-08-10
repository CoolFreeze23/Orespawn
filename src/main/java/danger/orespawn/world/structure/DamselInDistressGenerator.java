package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Damsel In Distress hut (Village dimension), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeDamselInDistress}
 * (orig GenericDungeon.java:3625-3733) &mdash; a 9&times;9&times;5
 * cobblestone hut (1/8 mossy) with a 3&times;3 front doorway, a stepped
 * cobble roof (y+5/+6) biased toward the front, a solid triangular front
 * gable rising to y+9, and an iron-bar wall at z+1 sealing off a back JAIL
 * CELL that holds a north-facing loot chest and a live Girlfriend &mdash;
 * the damsel &mdash; while two Scorpion spawners guard the front room.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/damsel_in_distress_spec.md}):</p>
 * <ul>
 * <li><b>The jail cell is the point (spec S2):</b> loop E's 7&times;4 bar
 *     wall fully partitions the hut; there is NO opening into the cell
 *     &mdash; the player must break bars to reach the chest and the
 *     Girlfriend. Do not add a door.</li>
 * <li><b>Girlfriend spawn (spec &sect;4.1/S9) &mdash;
 *     {@code piece.spawnEntity}, NOT {@code spawnPersistent}:</b> the
 *     original (orig :3727-3732) set no persistence flag and ran no spawn
 *     rules ({@code func_72838_d} direct add); the entity never despawns
 *     because the CLASS overrides canDespawn &rarr; false (orig
 *     Girlfriend.java:850-852), carried by the port entity's
 *     {@code removeWhenFarAway} &rarr; false &mdash; a
 *     {@code PersistenceRequired} tag would be data the original save never
 *     had. The doubles in the original are casts of INTS, so the entity
 *     stands on the block CORNER, not the +0.5 centre &mdash; transcribed
 *     exactly, not "fixed". Yaw is drawn unconditionally by this caller
 *     (stitching contract; the {@link HospitalGenerator} end-crystal
 *     precedent).</li>
 * <li><b>Draw-then-carve doorway (spec S4):</b> in loop A the 9 doorway
 *     cells are cobble when the mossy roll runs and are forced back to air
 *     AFTER it (orig :3657-3663), so they consume draws that are then
 *     discarded. The roll condition is positional only &mdash; every chunk
 *     pass consumes the identical sequence &mdash; and the order is
 *     transcribed so the draw count matches the original
 *     distribution.</li>
 * <li><b>Gable overwrites (spec S5):</b> loop D's y+5 row overwrites loop
 *     B's z&nbsp;=&nbsp;&minus;4 row and its y+6 row overwrites loop C's,
 *     each with fresh mossy rolls, and each row's x&nbsp;=&nbsp;0 centre
 *     cell is written TWICE with separate rolls (orig :3691-3703). Later
 *     write wins; write order and both draws are replicated, not
 *     deduped.</li>
 * <li><b>Asymmetric roof (spec S6):</b> course 1 stops at
 *     j&nbsp;&le;&nbsp;+3 and course 2 at j&nbsp;&le;&nbsp;+2 (orig
 *     :3669-3670/:3679-3680) &mdash; the strip above the back wall is never
 *     written, yet every interior column is covered and the room stays
 *     sealed. Faithful; not squared up.</li>
 * <li><b>Anchor (spec S7):</b> the worldgen origin is the GRASS BLOCK
 *     itself &mdash; {@code addDamselInDistress} passes {@code posY - 1}
 *     (orig OreSpawnWorld.java:1311), so the floor replaces the grass and
 *     the hut sits flush with the terrain (contrast the Leaf Monster's
 *     one-block-raised anchor). That &minus;1, plus the 12&times;12
 *     {@code quickSpaceCheck} clearance plane (orig OreSpawnWorld.java:
 *     2625-2633), is {@code PlacementMode.VILLAGE_GRASS_SURFACE}.</li>
 * <li><b>Chest facing (spec S8):</b> the method's only nonzero-meta write
 *     is the chest's meta 2 = facing north, into the cell (orig :3722)
 *     &mdash; ported with the facing-aware {@code placeLootChest}
 *     overload.</li>
 * <li><b>RNG (spec &sect;11):</b> geometry draws (loop A cobble cells,
 *     56 in loop B, 35 in loop C, 30 in loop D) are transcribed verbatim in
 *     source order; the chest fill count {@code 10 + nextInt(5)}
 *     (orig :3725) moved into the loot JSON's 10-14 rolls; the Girlfriend
 *     yaw (orig :3730) is the generator's only post-geometry draw. The
 *     original's tile-entity self-reads (orig :3713/:3718/:3723) are
 *     absorbed by the piece helpers; the client-side guard (orig
 *     :3640-3642) has no port equivalent &mdash; postProcess and
 *     {@code buildNow} are server-only (spec S11). No terrain reads
 *     anywhere &mdash; the builder writes unconditionally (spec
 *     &sect;10).</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/damsel_in_distress} transcribes the
 * 9-entry {@code DamselContentsList} (orig GenericDungeon.java:39, total
 * weight 315) with the original 10-14 rolls. The SAME 1.7.10 list serves
 * {@code makeGirlfriendIsland} at fill 4&nbsp;+&nbsp;nextInt(5) (orig
 * :4968/:5021/:5026), so that structure ships its own
 * {@code chests/girlfriend_island.json} with identical entries and 4-8
 * rolls &mdash; one table per (list, fill formula) pair, the Kyuubi
 * precedent (pattern doc &sect;1 step 5); each file's {@code _shared_list}
 * key names its twin. The random-slot collision quirk is dropped, the same
 * documented approximation as every chest-list conversion since
 * Phase C.</p>
 */
final class DamselInDistressGenerator {

    /** The jail-cell chest (orig GenericDungeon.java:39 {@code DamselContentsList} + :3722-3726). */
    private static final ResourceKey<LootTable> DUNGEON_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/damsel_in_distress"));

    private DamselInDistressGenerator() {}

    /**
     * Direct port of {@code makeDamselInDistress(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:3625-3733). {@code origin} is the grass block
     * a Village-dimension column scan anchored on (worldgen,
     * orig OreSpawnWorld.java:1311 &mdash; {@code posY - 1}, so the floor
     * REPLACES the grass, spec S7) or the Dungeon Spawner Block position
     * (type 28, orig DungeonSpawnerBlock.java:137-139 &mdash; no offset and
     * no validation: the hut floor sits AT the clicked position, in any
     * dimension, faithful behavior). Write order is behavior and is
     * preserved verbatim: shell &rarr; roof course 1 &rarr; roof course 2
     * &rarr; front gable &rarr; jail bars &rarr; spawners &rarr; chest
     * &rarr; Girlfriend.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Method-local constants (orig :3636-3638). Dead locals ignored
        // (spec S3): stuffdir (orig :3631/:3639) and the meta = 0 reset
        // (orig :3668) are never consumed — every write is meta 0 except
        // the chest's facing meta 2 (spec S8).
        final int length = 4;                                          // orig :3636
        final int width = 4;                                           // orig :3637
        final int height = 5;                                          // orig :3638

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();    // field_150347_e
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState(); // field_150341_Y
        // field_150411_aY. In 1.7.10 BlockPane derived connections dynamically
        // at collision/render time (no metadata), so the original jail wall was
        // sealed from the moment it generated; the port's defaultBlockState
        // bars stay visually/collision-wise unconnected until a neighbor
        // update. Repo-wide accepted approximation (same as Hospital,
        // EnderCastle, MiniDungeon, Graveyard bars) — spec §5.
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();

        // Loop A — 9×9×5 shell with 3×3 doorway (orig :3643-3667). Per
        // cell, in source order: air default, floor, x walls, z walls,
        // mossy roll on cobble, THEN the doorway carve (orig :3660-3663,
        // the decompiled negation de-Morgans to exactly the condition
        // below) — the 9 door cells consume mossy draws before being
        // forced back to air (spec S4; the roll condition is positional
        // only, so every chunk pass draws the identical sequence).
        for (int i = -width; i <= width; i++) {
            for (int j = -length; j <= length; j++) {
                for (int k = 0; k < height; k++) {
                    BlockState bid = air;                              // orig :3646
                    if (k == 0) {
                        bid = cobble;                                  // orig :3648-3650 — floor
                    }
                    if (i == -width || i == width) {
                        bid = cobble;                                  // orig :3651-3653 — x walls
                    }
                    if (j == -length || j == length) {
                        bid = cobble;                                  // orig :3654-3656 — z walls
                    }
                    if (bid == cobble && random.nextInt(8) == 1) {
                        bid = mossy;                                   // orig :3657-3659
                    }
                    if ((k == 1 || k == 2 || k == 3)
                            && (i == 0 || i == -1 || i == 1) && j == -length) {
                        bid = air;                                     // orig :3660-3663 — doorway, carved AFTER the roll
                    }
                    piece.place(cx + i, cy + k, cz + j, bid);          // orig :3664
                }
            }
        }

        // Loop B — roof course 1, a 7×8 slab at y+5 (orig :3669-3678):
        // i = −3..+3, j = −4..+3 — asymmetric, stopping one short on +z
        // (spec S6): the strip above the back wall is never written, but
        // every interior column (z ≤ +3) is covered, so the room stays
        // sealed. 56 unconditional draws.
        for (int i = -width + 1; i <= width - 1; i++) {
            for (int j = -length; j <= length - 1; j++) {
                BlockState bid = cobble;                               // orig :3671-3672
                if (random.nextInt(8) == 1) {
                    bid = mossy;                                       // orig :3673-3675
                }
                piece.place(cx + i, cy + height, cz + j, bid);         // orig :3676
            }
        }

        // Loop C — roof course 2, a 5×7 slab at y+6 (orig :3679-3688):
        // i = −2..+2, j = −4..+2, again biased toward −z. 35 draws.
        for (int i = -width + 2; i <= width - 2; i++) {
            for (int j = -length; j <= length - 2; j++) {
                BlockState bid = cobble;                               // orig :3681-3682
                if (random.nextInt(8) == 1) {
                    bid = mossy;                                       // orig :3683-3685
                }
                piece.place(cx + i, cy + height + 1, cz + j, bid);     // orig :3686
            }
        }

        // Loop D — solid triangular front gable at z = −4, y +5..+9
        // (orig :3689-3705). Outer m = 4..0 descending, inner i = m..0
        // descending, TWO draw+write pairs per inner step — (+i) then (−i)
        // — so each row's x = 0 centre cell is written twice with separate
        // rolls (spec S5, both draws kept), and the y+5 / y+6 rows
        // overwrite loop B's / C's z = −4 rows with fresh rolls — later
        // write wins, not deduped. 2×(5+4+3+2+1) = 30 draws.
        int gy = height;                                               // orig :3689
        for (int m = width; m >= 0; m--) {
            for (int i = m; i >= 0; i--) {
                BlockState bid = cobble;                               // orig :3693
                if (random.nextInt(8) == 1) {
                    bid = mossy;                                       // orig :3694-3696
                }
                piece.place(cx + i, cy + gy, cz - length, bid);        // orig :3690/:3697
                bid = cobble;                                          // orig :3698
                if (random.nextInt(8) == 1) {
                    bid = mossy;                                       // orig :3699-3701
                }
                piece.place(cx - i, cy + gy, cz - length, bid);        // orig :3702
            }
            gy++;                                                      // orig :3704
        }

        // Loop E — 7×4 iron-bar jail wall at z = +1 (orig :3706-3711).
        // Source index swap: here j is the Y offset and k the Z offset
        // (= length − 3 = 1, orig :3708); the write's cposx − i spans the
        // same −3..+3 set. No RNG. Fully partitions the hut into the front
        // room (z −3..0) and the sealed back jail cell (z +2..+3) — there
        // is NO opening; breaking bars is the only way in (spec S2, do not
        // add a door). All 28 cells were loop-A interior air.
        for (int i = -width + 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                piece.place(cx - i, cy + j, cz + length - 3, bars);    // orig :3708-3709
            }
        }

        // Two "Scorpion" spawners on the front-room floor, flanking the
        // doorway (loop-A interior air cells — nothing overwritten). Mob
        // mapping: "Scorpion" (orig OreSpawnMain.java:3791) →
        // orespawn:scorpion (spec §4).
        piece.placeSpawner(cx - width + 1, cy + 1, cz - length + 1, ModEntities.ENTITY_SCORPION.get()); // orig :3712-3716
        piece.placeSpawner(cx + width - 1, cy + 1, cz - length + 1, ModEntities.ENTITY_SCORPION.get()); // orig :3717-3721

        // Loot chest in the jail cell's back-east corner — meta 2 = facing
        // NORTH into the cell, the method's only nonzero-meta write (orig
        // :3722, spec S8). Single chest, no double-chest pairing. Fill
        // count 10 + nextInt(5) (orig :3725) lives in the loot table's
        // 10-14 rolls.
        piece.placeLootChest(cx + width - 1, cy + 1, cz + length - 1, Direction.NORTH, DUNGEON_LOOT); // orig :3722-3726

        // orig :3727-3732 — the imprisoned Girlfriend, spawned live in the
        // jail cell two west of the origin column (five west of the chest).
        // Yaw drawn unconditionally by this caller; only the spawn is
        // chunk-gated (stitching contract). The original's doubles are
        // casts of INTS — she stands on the block CORNER, not the +0.5
        // centre (spec S9, transcribed exactly). spawnEntity, NOT
        // spawnPersistent: no persistence flag was ever set — the class
        // itself never despawns (orig Girlfriend.java:850-852 canDespawn →
        // false; port removeWhenFarAway → false), in both versions.
        float yaw = random.nextFloat() * 360.0f;                       // orig :3730
        piece.spawnEntity(ModEntities.GIRLFRIEND.get(), cx - width + 2, cy + 1, cz + length - 1, yaw); // orig :3727-3732
    }
}
