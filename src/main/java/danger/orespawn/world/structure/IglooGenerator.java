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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Igloo (vanilla-overworld snow structure), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeIgloo} (orig GenericDungeon.java:2698-2813)
 * &mdash; a hollow stepped snow dome: a 3-course wall (snow block / ice /
 * snow block, y+1..y+3) rasterized on a radius-6 float circle, an ice
 * setback ring (radius 5) at y+4, and a flat roof cap at y+5 of four
 * concentric rings (snow r=4, ice r=3, snow r=2, ice r=1). Whether the
 * apex cell {@code (0,0)} stays open depends on the ORIGIN QUADRANT: the
 * original's toward-zero float truncation lands ring cells on the apex
 * whenever the origin x or z is negative, so the 1&times;1 skylight exists
 * only at (+,+) origins &mdash; bit-faithfully reproduced here (the spec's
 * unconditional-skylight claim S3 was corrected by the D6b batch-2
 * verification's four-quadrant float32 simulation). An oak door faces west
 * in the wall over a buried plank sill;
 * three spawners (Rat / Ghost / Ghost Pumpkin Skelly) and one north-facing
 * kit chest sit flush in the untouched interior terrain (no floor, no
 * interior clearing &mdash; spec S4).
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/igloo_spec.md}):</p>
 * <ul>
 * <li><b>Float-circle idiom transcribed VERBATIM</b> (spec shared plumbing +
 *     &sect;2.7/S5): every shell cell is
 *     {@code (int)((float) origin + (float)(radius * cos/sin(deg)) + 0.5f)}
 *     &mdash; the drift-preserving origin-added form
 *     ({@code CrystalStructures.buildCrystalBattleTower} precedent,
 *     CrystalStructures.java:868). {@code (int)} truncates toward zero,
 *     so the whole shell + door column shifts +1 per NEGATIVE world axis
 *     while the int-math spawners/chest stay fixed &mdash; faithful 1.7.10
 *     quadrant drift, reproduced bit-identically. Deliberately NOT a
 *     zero-centered stamp form (the since-deleted
 *     CrystalBattleTowerFeature's approach &mdash; removed in D6b batch 4,
 *     dsb_sweep_spec.md F4), which the spec proves is not bit-faithful at
 *     any origin.</li>
 * <li>The original door helper {@code ItemDoor.func_150924_a} (orig :2745,
 *     direction 2 = facing west) picks the hinge by READING the two flanking
 *     wall columns &mdash; all four probed cells are this builder's own
 *     writes (snow y+1 counts solid, ice y+2 does not), so both sides tie
 *     and the result is a constant: <b>hinge = LEFT</b>, modeled in memory
 *     (BasiliskMaze-style self-read rule; spec &sect;2.4/&sect;10.2). The
 *     helper's two trailing neighbor notifications are dropped &mdash; safe,
 *     the door stands on the solid plank sill (spec S8).</li>
 * <li>Chest meta 2 = facing north (orig :2761, spec S9) &rarr;
 *     {@code placeLootChest(..., Direction.NORTH, ...)}.</li>
 * <li>Write order preserved: wall &rarr; setback ring &rarr; cap rings
 *     (later ice rings overwrite 4 snow cells each at r3/r1 boundaries)
 *     &rarr; doorway punch + door &rarr; spawners &rarr; chest
 *     (spec &sect;2.3/&sect;2.8).</li>
 * <li>The original's only RNG, 16 independent {@code nextInt(2)} chest-slot
 *     gates (orig :2764-2810), moved into the loot JSON as 16 pools with
 *     {@code random_chance 0.5} each, so this generator consumes ZERO random
 *     draws and satisfies the per-chunk-replay stitching contract vacuously
 *     (spec &sect;11). Its only world reads were tile-entity fetches at
 *     just-written positions (orig :2747/:2752/:2757/:2762), absorbed by the
 *     {@code placeSpawner}/{@code placeLootChest} helpers, plus the door
 *     helper's modeled hinge probe above; no pre-build terrain reads exist
 *     (spec &sect;10.3).</li>
 * <li><b>Worldgen placement (WGEN-071, spec &sect;7.3 ruling):</b> the
 *     original's "Ice Plains" exact-name gate (orig OreSpawnWorld.java:
 *     1263-1264) and its snow-BLOCK-below column scan (OSW:1270) are
 *     mutually inconsistent on plain Ice Plains terrain, making natural
 *     igloos a rare biome-border artifact (spec &sect;1.3/S7). The wired
 *     placement reproduces BOTH gates mechanically &mdash; a
 *     snowy_plains-only biome tag plus
 *     {@code LegacyDungeonPiece.resolveIglooWorldgenSite}'s generation-time
 *     TRUE-surface re-verification (which hands this generator its build
 *     origin, possibly relocated within the anchor chunk, or suppresses the
 *     build entirely) &mdash; with no invented frequency. The Random
 *     Dungeon Spawner path, type 20 (orig DungeonSpawnerBlock.java:
 *     113-115), still bypasses biome, scan, and the &minus;2 sink &mdash;
 *     a shell perched on or embedded in the clicked terrain is faithful
 *     behavior (spec &sect;9).</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/igloo} &mdash; the InstantShelter
 * kit with slot 12 (Ore Salt) omitted, slot 2 swapped raw&rarr;cooked
 * porkchop, and three gold-nugget stacks appended (spec S6); NOT a weighted
 * list. Only the fixed slot positions are lost to the loot system's
 * sequential fill &mdash; documented approximation (spec &sect;3).</p>
 */
final class IglooGenerator {

    /** Loot for the kit chest (orig GenericDungeon.java:2761-2812). */
    private static final ResourceKey<LootTable> IGLOO_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/igloo"));

    private IglooGenerator() {}

    /**
     * Direct port of {@code makeIgloo(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:2698-2813). {@code origin} is the Dungeon
     * Spawner Block position (type 20, orig DungeonSpawnerBlock.java:113-115,
     * no Y offset) or, on the worldgen path, the site resolved by
     * {@code LegacyDungeonPiece.resolveIglooWorldgenSite} &mdash; the
     * original anchor {@code posY - 2}, one below the snow surface block
     * (orig OreSpawnWorld.java:1271), re-verified against the real chunk
     * per the spec &sect;7.3 ruling (WGEN-071; see the class Javadoc).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState(); // field_150433_aE — the FULL block
        BlockState ice = Blocks.ICE.defaultBlockState();              // field_150432_aD
        BlockState air = Blocks.AIR.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop 1 — dome wall, radius 6, 5° steps: per rasterized cell three
        // stacked writes, snow y+1 / ice y+2 / snow y+3 (orig :2705-2711).
        // 72 iterations → 40 unique cells (spec §2.1). The float idiom below
        // is the original's, verbatim — see the class Javadoc drift note.
        float currad = 6.0f;                                          // orig :2704
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            float curx = (float) ((double) currad * Math.cos(Math.toRadians(curdeg))); // orig :2706
            float curz = (float) ((double) currad * Math.sin(Math.toRadians(curdeg))); // orig :2707
            int x = (int) ((float) cx + curx + 0.5f);
            int z = (int) ((float) cz + curz + 0.5f);
            piece.place(x, cy + 1, z, snowBlock);                     // orig :2708
            piece.place(x, cy + 2, z, ice);                           // orig :2709
            piece.place(x, cy + 3, z, snowBlock);                     // orig :2710
        }
        // Loop 2 — ice setback ring, radius 5, 5° steps, y+4
        // (orig :2712-2717; 36 unique cells, spec §2.2).
        ring(piece, cx, cy + 4, cz, 5.0f, 5.0f, ice);
        // Loops 3-6 — flat roof cap at y+5, four concentric rings
        // (orig :2718-2741; spec §2.3). Later ice rings overwrite 4 snow
        // cells each; the apex (0,0) stays open only at (+,+) origins —
        // negative-origin truncation lands r=1/r=2 ring cells on it
        // (bit-faithful; see class Javadoc).
        ring(piece, cx, cy + 5, cz, 4.0f, 5.0f, snowBlock);           // orig :2718-2723
        ring(piece, cx, cy + 5, cz, 3.0f, 10.0f, ice);                // orig :2724-2729
        ring(piece, cx, cy + 5, cz, 2.0f, 15.0f, snowBlock);          // orig :2730-2735
        ring(piece, cx, cy + 5, cz, 1.0f, 15.0f, ice);                // orig :2736-2741

        // Doorway + door, west wall (orig :2742-2745; spec §2.4). The column
        // uses the same float idiom as the shell, so it shifts WITH the shell
        // on negative axes. Plank sill buried at y+0; wall snow/ice punched
        // to air at y+1..y+2; the lintel snow at y+3 stays (crouch-height
        // doorway, spec S2 — do not "fix").
        int doorX = (int) ((float) cx - 6.0f + 0.5f);                 // orig :2742
        int doorZ = (int) ((float) cz + 0.5f);
        piece.place(doorX, cy, doorZ, Blocks.OAK_PLANKS.defaultBlockState()); // orig :2742
        piece.place(doorX, cy + 1, doorZ, air);                       // orig :2743
        piece.place(doorX, cy + 2, doorZ, air);                       // orig :2744
        // orig :2745 — ItemDoor.func_150924_a, direction 2 = facing west;
        // hinge probe modeled as constant LEFT (class Javadoc).
        BlockState doorLower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT);
        piece.place(doorX, cy + 1, doorZ, doorLower);
        piece.place(doorX, cy + 2, doorZ,
                doorLower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));

        // Three interior spawners at y+1, plain int offsets — these do NOT
        // drift with the shell in negative quadrants (spec §2.5/S5).
        piece.placeSpawner(cx + 2, cy + 1, cz - 4, ModEntities.ENTITY_RAT.get());   // "Rat", orig :2746-2750
        piece.placeSpawner(cx - 1, cy + 1, cz + 1, ModEntities.GHOST.get());        // "Ghost", orig :2751-2755
        piece.placeSpawner(cx + 3, cy + 1, cz + 4, ModEntities.GHOST_SKELLY.get()); // "Ghost Pumpkin Skelly", orig :2756-2760

        // Kit chest, meta 2 = facing north (orig :2761-2812; spec §2.6/S9).
        // The 16 independent 50% slot rolls live in the loot JSON.
        piece.placeLootChest(cx - 3, cy + 1, cz - 3, Direction.NORTH, IGLOO_LOOT);
    }

    /**
     * One single-course float-circle ring (the shared loop body of orig
     * GenericDungeon.java:2712-2741): {@code curdeg = 0 → <360} in
     * {@code stepDeg} float steps (5/10/15 — all exactly representable, no
     * accumulation drift), each iteration rasterizing one cell via the
     * verbatim origin-added float idiom. Bit-identical to the original's
     * inline loops for every argument this class passes.
     */
    private static void ring(LegacyDungeonPiece piece, int cx, int y, int cz,
                             float currad, float stepDeg, BlockState state) {
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += stepDeg) {
            float curx = (float) ((double) currad * Math.cos(Math.toRadians(curdeg)));
            float curz = (float) ((double) currad * Math.sin(Math.toRadians(curdeg)));
            piece.place((int) ((float) cx + curx + 0.5f), y, (int) ((float) cz + curz + 0.5f), state);
        }
    }
}
