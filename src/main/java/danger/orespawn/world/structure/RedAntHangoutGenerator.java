package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Red Ant Hangout (Village dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeRedAntHangout} (orig GenericDungeon.java:7045-7069
 * &mdash; the FINAL method in the file) &mdash; a flat 16&times;16 gravel pad
 * flush with the terrain (stone slab one below, 15 blocks of forced open air
 * above), with a 3&times;3 Red Ant Nest patch in each corner and one wild
 * Robot Red Ant standing at pad centre. No walls, no roof, no spawners, no
 * chest &mdash; the family outlier next to the Spider Hangout's
 * spawner-corner 20&times;20 twin (GD:6989-7043).
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/red_ant_hangout_spec.md}):</p>
 * <ul>
 * <li><b>The corner condition is a De Morgan trap (spec S3):</b> orig :7056
 *     reads {@code if (!(i >= 3 && i <= 12 || k >= 3 && k <= 12))} &mdash;
 *     nest blocks ONLY where {@code (i <= 2 or i >= 13) AND (k <= 2 or
 *     k >= 13)} = the four 3&times;3 CORNER pads (36 cells); the other 220
 *     floor cells stay gravel. Transcribed with the original's negation
 *     verbatim, not "simplified" &mdash; a border-ring reading produces a
 *     visibly wrong floor.</li>
 * <li><b>Origin is the pad's minimum corner, not its centre (spec S5):</b>
 *     the loops run 0..15 (orig :7047-7049), so the structure extends only
 *     toward +X/+Z from the anchor and the robot's +8,+8 spawn is the pad
 *     centre. One-sided box kept ({@code RED_ANT_HANGOUT(-1, 16, 2, 16, -1,
 *     16, ...)}); contrast the Damsel's &plusmn;4 symmetric loops.</li>
 * <li><b>Anchor is the grass block itself (spec S4):</b>
 *     {@code addRedAntHangout} passes {@code posY - 1} (orig
 *     OreSpawnWorld.java:1350), so the pad REPLACES the surface &mdash;
 *     gravel at former grass level, stone one below ({@code j = -1..0}).
 *     That scan is line-for-line identical to {@code addDamselInDistress}'s
 *     (orig OreSpawnWorld.java:1301-1317) &mdash; same 1/250 gate, same
 *     4 attempts, same Y&nbsp;100&rarr;41 air-over-grass scan, same
 *     12&times;12 {@code quickSpaceCheck} plane (orig OreSpawnWorld.java:
 *     2625-2633) &mdash; which is exactly
 *     {@code PlacementMode.VILLAGE_GRASS_SURFACE}; the 1/250 odds map to
 *     the structure set's spacing 16 / separation 8 (C7 sqrt
 *     equivalence).</li>
 * <li><b>15 layers of unconditional air (spec S6):</b> {@code j = 1..15}
 *     force-clears a 16&times;16&times;15 volume &mdash; trees, hills,
 *     buildings, everything above the pad vanishes (orig :7050/:7060 write
 *     every cell, air included). Faithful; no terrain-sparing conditions
 *     added.</li>
 * <li><b>No spawners, no chest, no loot (spec S7):</b> orig :7045-7069
 *     places none &mdash; the renewable mob content is the 36 Red Ant Nest
 *     corner blocks (the port {@code CrystalAntBlock} spawns Red Ants,
 *     config-gated &mdash; the anthill-feature mapping, spec &sect;5) plus
 *     the one robot. Not "completed" with spawners or a chest.</li>
 * <li><b>The robot spawns UNOWNED (spec S8/&sect;4.1):</b> the spawn site
 *     runs no init &mdash; no NBT, no {@code setOwned()} ({@code owned = 0},
 *     orig AntRobot.java:48). An unowned AntRobot is hostile-neutral: it
 *     stomps and aggros on its own AI (orig AntRobot.java:105-146) but can
 *     NOT be ridden &mdash; interact no-ops while unowned (orig
 *     AntRobot.java:908-910); riding and iron-ingot healing are owned-only.
 *     The structure is effectively a free CLAIMABLE robot guarded by ant
 *     nests: batter it below half health, wrench it into an AntRobotKit
 *     (orig ItemWrench.java:50-60), place the kit to respawn it owned.
 *     Spawned factory-fresh; not pre-owned.</li>
 * <li><b>{@code spawnPersistent}, NOT {@code spawnEntity} (spec
 *     &sect;4.1):</b> the spawn site set no flag, but the 1.7.10 AntRobot
 *     class self-persists twice over &mdash; {@code canDespawn} &rarr; false
 *     (orig AntRobot.java:80-82) AND entityInit's {@code func_110163_bv()}
 *     (orig AntRobot.java:532-538, the call at :534) &mdash; so every
 *     original robot carried {@code PersistenceRequired} in its saved NBT
 *     from birth. Unlike the Damsel's Girlfriend (class override only, no
 *     NBT flag &rarr; {@code spawnEntity}), here the flagged helper IS the
 *     faithful choice. (The port entity class carries NEITHER original
 *     mechanism &mdash; spec S2 parity gap, an entity-file fix outside this
 *     structure's scope &mdash; so the flag also keeps this robot
 *     despawn-proof in the meantime.)</li>
 * <li><b>Direct-spawn quirks preserved (spec S9):</b> the original's doubles
 *     are casts of INTS (orig :7066), so the robot stands on the block
 *     CORNER at {@code (+8, +1, +8)}, not the +0.5 centre &mdash;
 *     transcribed exactly, not "fixed". Pitch 0, no NBT.
 *     The original spawn is bare (no {@code playLivingSound}, orig
 *     :7064-7068), so this caller uses the silent
 *     {@code spawnPersistent(..., false)} variant &mdash; the live DSB
 *     path stays quiet like the original (batch-4 verify fix).</li>
 * <li><b>RNG (spec &sect;11):</b> the geometry consumes ZERO draws &mdash;
 *     every cell's block is a pure function of loop indices (orig
 *     :7050-7058). The yaw (orig :7066) is the builder's ONLY draw, taken
 *     unconditionally by this caller (stitching contract; the original's
 *     {@code var8 != null} guard cannot fail in the port &mdash; the
 *     EntityType is registered, Damsel &sect;4.1 precedent). Zero mid-build
 *     world reads &mdash; no terrain probes, no tile-entity fetches
 *     anywhere (spec &sect;10).</li>
 * </ul>
 *
 * <p>Gravel-support note (spec &sect;5): gravel is a modern
 * {@code FallingBlock}, but every {@code j = 0} gravel cell sits on the
 * {@code j = -1} stone slab written by the same loop &mdash; always
 * supported, on the worldgen AND the DSB path (a floating DSB build still
 * writes its own stone base). No falling hazard.</p>
 */
final class RedAntHangoutGenerator {

    private RedAntHangoutGenerator() {}

    /**
     * Direct port of {@code makeRedAntHangout(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:7045-7069). {@code origin} is the grass block
     * a Village-dimension column scan anchored on (worldgen, orig
     * OreSpawnWorld.java:1350 &mdash; {@code posY - 1}, so the pad REPLACES
     * the grass, spec S4) or the Dungeon Spawner Block position (type 49
     * &mdash; the LAST outcome of the 50-entry table &mdash; orig
     * DungeonSpawnerBlock.java:200-202 &mdash; no offset and no validation:
     * the gravel floor sits AT the clicked position, stone one below, in any
     * dimension, faithful behavior). Write order is behavior and is preserved
     * verbatim: one triple loop, then the robot.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();       // field_150350_a
        BlockState stone = Blocks.STONE.defaultBlockState();   // field_150348_b
        BlockState gravel = Blocks.GRAVEL.defaultBlockState(); // field_150351_n
        // OreSpawnMain.MyRedAntBlock (orig OreSpawnMain.java:6272, "Red Ant
        // Nest") — the established anthill-feature mapping (spec §5).
        BlockState nest = ModBlocks.RED_ANT_BLOCK.get().defaultBlockState();

        // The whole builder is one triple loop (orig :7047-7063) writing the
        // full 16×17×16 volume unconditionally, air included: stone slab at
        // j = -1, floor at j = 0 (gravel, with the four 3×3 nest corner
        // pads), forced air for j = 1..15. Zero RNG, zero world reads.
        for (int i = 0; i < 16; ++i) {                                 // orig :7047
            for (int j = -1; j < 16; ++j) {                            // orig :7048
                for (int k = 0; k < 16; ++k) {                         // orig :7049
                    BlockState blk = air;                              // orig :7050
                    if (j == -1) {
                        blk = stone;                                   // orig :7051-7053 — 16×16 base slab
                    }
                    if (j == 0) {
                        blk = gravel;                                  // orig :7054-7055 — floor
                        if (!(i >= 3 && i <= 12 || k >= 3 && k <= 12)) {
                            blk = nest;                                // orig :7056-7058 — 3×3 corner pads (spec S3)
                        }
                    }
                    piece.place(cx + i, cy + j, cz + k, blk);          // orig :7060 — flag-2, meta 0
                }
            }
        }

        // orig :7064-7068 — the wild Robot Red Ant at pad centre, standing
        // on the gravel at the block CORNER (int casts, spec S9), unowned
        // (spec S8). Yaw drawn unconditionally by this caller; only the
        // spawn is chunk-gated (stitching contract). spawnPersistent writes
        // the same PersistenceRequired NBT the original entity always
        // carried from birth (orig AntRobot.java:80-82/:534, spec §4.1).
        float yaw = random.nextFloat() * 360.0f;                       // orig :7066
        piece.spawnPersistent(ModEntities.ANT_ROBOT.get(), cx + 8, cy + 1, cz + 8, yaw, false); // orig :7064-7068 (bare spawn — silent)
    }
}
