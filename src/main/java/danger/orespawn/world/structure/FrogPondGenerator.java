package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Frog Pond (vanilla-overworld Plains), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeFrogPond} (orig GenericDungeon.java:6018-6039)
 * &mdash; one of the WGEN-042 structure pool, and one of the smallest: a
 * 7&times;7 still-water sheet flush with the terrain, a plus-shaped water
 * mound one block high at the center, and four lily pads floating on the
 * mound around a "Frog" mob spawner hovering at +2 on the center water
 * column. The +1 sources spill over the sheet and off the rim once fluid
 * ticks run &mdash; generated behavior in both versions, do not rim the
 * pond (spec S9).
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/frog_pond_spec.md}):</p>
 * <ul>
 * <li><b>Anchor duality &mdash; the builder itself is offset-free</b>
 *     (spec S5): worldgen hands in {@code posY - 1}, the GRASS block
 *     itself (orig OreSpawnWorld.java:1168), so the water sheet REPLACES
 *     the grass layer and the pond sits flush; the Dungeon Spawner Block
 *     hands in {@code clickedY + 1} (type 43, orig
 *     DungeonSpawnerBlock.java:182-184), so the sheet floats one above
 *     the cleared block. Both quirks live entirely at the call sites
 *     ({@code SAND_SURFACE_MINUS1}'s {@code .below()} and the RDS case's
 *     {@code pos.above()}); this generator adds NO offsets of its own.</li>
 * <li><b>Zero RNG draws</b> (spec S7/&sect;11): orig :6018-6039 contains
 *     no {@code nextInt}/{@code rand} of any kind &mdash; every coordinate
 *     and block is a constant. The per-chunk-replay stitching contract is
 *     satisfied vacuously; the {@code random} parameter is unused by
 *     design, and worldgen ponds are block-identical to {@code buildNow}
 *     ponds.</li>
 * <li><b>Flag semantics</b> (spec S3): the spawner is the method's only
 *     flag-2 write (orig :6020); <b>every water and lily-pad write is
 *     flag 3</b> (update + notify, orig :6027-6038) &mdash; the original
 *     deliberately notified neighbours to start the water flowing.
 *     {@code piece.place} writes flag 2 only, but modern
 *     {@code LiquidBlock.onPlace} self-schedules a fluid tick, so the
 *     cascade still starts &mdash; PARITY note, non-behavioral
 *     (play_pool_spec.md S3 precedent).</li>
 * <li><b>Flowing-water meta 0 = source</b> (spec S4/&sect;5): the four +1
 *     cross cells use {@code Blocks.field_150358_i} (flowing_water,
 *     meta 0), which the 1.13 flattening maps &mdash; like the meta-0
 *     still water &mdash; to the {@code minecraft:water} source state,
 *     which is also what they settle into in 1.7.10.</li>
 * <li><b>No chest, no loot, no direct entity spawns</b> (spec S2/&sect;3/
 *     &sect;4.2): the spawner is the method's only tile entity. Do not
 *     invent a loot table.</li>
 * <li><b>Sub-Y50 ponds have dormant spawners in BOTH versions</b> (spec
 *     S8/&sect;4.1): the placement window is Y 41..100 (orig
 *     OreSpawnWorld.java:1166) but Frog's spawn rules require
 *     {@code y >= 50} with no near-spawner bypass &mdash; faithful, do
 *     not "fix".</li>
 * <li><b>Lily-pad survival</b> (spec &sect;5): each pad sits on a +1
 *     water source, so modern {@code WaterlilyBlock.canSurvive} holds
 *     through the post-placement fluid updates; the initial flag-2
 *     structure write bypasses the check, same as the original's flag-3
 *     {@code func_147465_d} did.</li>
 * </ul>
 */
final class FrogPondGenerator {

    private FrogPondGenerator() {}

    /**
     * Direct port of {@code makeFrogPond(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:6018-6039). {@code origin} is the scanned
     * grass block itself (worldgen, {@code posY - 1} at orig
     * OreSpawnWorld.java:1168 &mdash; the sheet replaces the grass) or one
     * ABOVE the cleared Dungeon Spawner Block (type 43, {@code clickedY + 1}
     * at orig DungeonSpawnerBlock.java:183, no ground gate &mdash; a
     * hovering or wall-embedded pond is faithful behavior). Writes run in
     * source order: spawner first, then the water under and around it
     * (spec S10 &mdash; order has no behavioral consequence, kept for
     * diff-ability). 59 writes total.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        BlockState water = Blocks.WATER.defaultBlockState();       // field_150355_j / field_150358_i (both meta 0 -> source)
        BlockState lilyPad = Blocks.LILY_PAD.defaultBlockState();  // field_150392_bi

        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        // orig :6020-6024 — "Frog" spawner at (0, +2, 0), written FIRST,
        // before the water below it. The original's only flag-2 write; the
        // TE fetch + func_98272_a("Frog") (and its silent null-guard) are
        // absorbed by placeSpawner (spec §10 item 2).
        piece.placeSpawner(x, y + 2, z, ModEntities.FROG.get());   // orig :6020

        // orig :6025-6029 — 7x7 still-water pond sheet at cposy, replacing
        // whatever terrain layer is there (the grass, at the worldgen
        // anchor). Loop var j is the Z offset here, not Y as elsewhere in
        // the original file (spec S11 — symmetric square, no transposition
        // risk). Flag 3 in the original, like every write below.
        for (int i = -3; i <= 3; i++) {                            // orig :6025
            for (int j = -3; j <= 3; j++) {                        // orig :6026
                piece.place(x + i, y, z + j, water);               // orig :6027
            }
        }

        // orig :6030 — center riser: a 1-block fountain bump above the
        // sheet, directly beneath the spawner.
        piece.place(x, y + 1, z, water);                           // orig :6030

        // orig :6031-6034 — flow cross at +1: four flowing-water (meta 0)
        // cells around the riser; all flatten to the water source state
        // (spec S4). These five +1 sources cascade over the sheet and off
        // the rim once fluid ticks run (spec S9).
        piece.place(x - 1, y + 1, z, water);                       // orig :6031
        piece.place(x + 1, y + 1, z, water);                       // orig :6032
        piece.place(x, y + 1, z - 1, water);                       // orig :6033
        piece.place(x, y + 1, z + 1, water);                       // orig :6034

        // orig :6035-6038 — lily-pad cross at +2: each pad sits on a flow-
        // cross water cell, ringing the spawner.
        piece.place(x - 1, y + 2, z, lilyPad);                     // orig :6035
        piece.place(x + 1, y + 2, z, lilyPad);                     // orig :6036
        piece.place(x, y + 2, z - 1, lilyPad);                     // orig :6037
        piece.place(x, y + 2, z + 1, lilyPad);                     // orig :6038
    }
}
