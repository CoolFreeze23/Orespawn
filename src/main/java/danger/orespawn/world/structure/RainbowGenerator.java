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
 * The Rainbow (Islands dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeRainbow} (orig GenericDungeon.java:6260-6391)
 * &mdash; one of the WGEN-042 structure pool. A hollow lens-shaped white
 * terracotta cloud (y +26..+29: two 26&times;5 perimeter rings around a
 * 28&times;7 bulge ring, capped by a solid 24&times;3 roof slab; the +26
 * layer is itself a ring, so the lens is OPEN underneath &mdash; spec S3,
 * no floor added), an 8-colour one-block-thick rainbow (y +30..+40 at
 * z = 0) arching over six "Cloud Shark" spawners and a loot double chest,
 * and a second flat 24&times;3 cloud slab at +35 that the outer arches
 * visibly pierce (spec S4).
 *
 * <p>Worldgen anchor: the original fired from the Islands "D4" dispatch at
 * 1/100 ({@code recently_placed == 0 && nextInt(100) == 0}) &times; 1/19
 * ({@code i == 18}, the last table slot) = 1/1900 per Islands chunk (orig
 * OreSpawnWorld.java:132-135, :174-176) &mdash; mapped to structure set
 * spacing 44/22 (&radic;1900 &asymp; 43.6). {@code addD4Rainbow} itself
 * (orig OreSpawnWorld.java:2430-2436) is UNCONDITIONAL &mdash; no ground
 * scan, no biome check, no LessLag gate, no failure path; it always builds,
 * always sets the shared {@code recently_placed = 50} cooldown, always
 * returns {@code true} (spec S7). That cooldown and the dispatch's 65&times;55
 * {@code D4BigSpaceCheck} air probe (orig OreSpawnWorld.java:2655-2664) are
 * absorbed into structure-set separation, the C7-approved approximation.
 * Anchoring is {@code PlacementMode.SKY_BAND_70}: {@code y = 70 + nextInt(20)}
 * (orig OreSpawnWorld.java:2433) &mdash; a DIFFERENT band from the Cloud
 * Shark's {@code SKY_BAND_150} (spec S9: losing it would move the build ~60
 * blocks); the original's mixed-RNG quirk (Y from the world rand, X/Z from
 * the chunk random, spec S8) collapses into the single seeded
 * structure-start random, as documented on {@code skyBand70Origin}.
 * Play-time trigger: Random Dungeon Spawner type 46 (orig
 * DungeonSpawnerBlock.java:191-193, unmodified click position &mdash; a
 * cloud-and-rainbow built 26-40 blocks above the block, possibly
 * intersecting terrain with nothing cleared outside the written cells, is
 * faithful).</p>
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/rainbow_spec.md}):</p>
 * <ul>
 * <li><b>The rain never survives (spec S2):</b> loop 2 hangs 8 water-source
 *     blocks in the +35 slab plane and 8 flowing-water blocks under it at
 *     +34 (orig :6279-6283), and the arch loop later overwrites ALL 16
 *     (coverage proof in the spec: at both planes the m &ge; 4 bars plus
 *     legs union to x &minus;11..10, a superset of the 8 rain columns).
 *     Flag-2 writes schedule no liquid spread in the interim, so the
 *     finished structure contains ZERO water. The dead writes are
 *     replicated in the original's order &mdash; write order is behavior
 *     &mdash; and both 1.7.10 water blocks ({@code field_150355_j} /
 *     {@code field_150358_i}) collapse to the one modern water block, a
 *     distinction without a difference on cells that never survive.</li>
 * <li><b>Colour order (spec S5):</b> {@code blkcolors = {14, 1, 4, 5, 3,
 *     11, 10, 6}} (orig :65, used by no other method) runs red INNERMOST to
 *     pink outermost &mdash; a real rainbow has red outermost. Transcribed
 *     verbatim, not "corrected".</li>
 * <li><b>Chest flags + facing (spec S6):</b> the chests are the method's
 *     only non-flag-2 writes ({@code func_147449_b} = flag 3, :6379/:6385)
 *     &mdash; a behavioral no-op delta (nothing flammable/fluid adjacent),
 *     so the piece's flag-2 helper matches. Meta 2 = facing north
 *     ({@code func_72921_c(..., 2, 3)}, :6380/:6386) &rarr; the
 *     facing-aware {@code placeLootChest} overload with
 *     {@code Direction.NORTH} on both halves. 1.7.10 auto-merged the
 *     adjacent pair into a double chest; modern default-ChestType states
 *     stay cosmetic singles (the {@link PlayPoolGenerator}/
 *     {@link LeafMonsterDungeonGenerator} precedent) &mdash; but unlike the
 *     Leaf Monster pair, BOTH halves are loot-filled (10-14 pulls each,
 *     orig :6383/:6389), so both are bound to the same table.</li>
 * <li><b>RNG (spec &sect;11):</b> the original's only draws were the two
 *     chest fill counts ({@code 10 + nextInt(5)}, orig :6383/:6389), moved
 *     into the loot JSON's 10-14 rolls, so this generator consumes ZERO
 *     random draws and satisfies the per-chunk-replay stitching contract
 *     vacuously. The method's only world reads were the 8 tile-entity
 *     fetches at just-written spawner/chest cells (spec &sect;10),
 *     absorbed by the {@code placeSpawner}/{@code placeLootChest} helpers
 *     &mdash; a fully floating, read-free build.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/rainbow} transcribes the original
 * 6-entry {@code RainbowContentsList} (orig GenericDungeon.java:25, total
 * weight 150) with the original 10-14 stack rolls per half; the random-slot
 * collision quirk is dropped, the same documented approximation as every
 * other chest-list conversion since Phase C.</p>
 */
final class RainbowGenerator {

    /** Loot for BOTH double-chest halves (orig GenericDungeon.java:25 + :6383/:6389). */
    private static final ResourceKey<LootTable> RAINBOW_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/rainbow"));

    private RainbowGenerator() {}

    /**
     * Direct port of {@code makeRainbow(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:6260-6391). {@code origin} is the sky-band
     * anchor (worldgen, Y 70-89 &mdash; every write lands 26..40 blocks ABOVE
     * it, nothing at or below the origin, spec S10) or the Dungeon Spawner
     * Block position (type 46, orig DungeonSpawnerBlock.java:191-193). Write
     * order is behavior and is preserved verbatim: upper slab &rarr; rain
     * &rarr; lower cloud rings &rarr; roof slab &rarr; arches (which
     * overwrite the rain) &rarr; spawners &rarr; chests.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState white = Blocks.WHITE_TERRACOTTA.defaultBlockState(); // field_150406_ce meta 0
        // Source (field_150355_j, orig :6281) and flowing (field_150358_i,
        // orig :6282) both collapse to the one modern water block — every one
        // of these cells is a dead write (class Javadoc / spec S2).
        BlockState water = Blocks.WATER.defaultBlockState();
        // blkcolors = {14, 1, 4, 5, 3, 11, 10, 6} (orig :65), indexed
        // [m - 3]: red innermost -> pink outermost (spec S5 — faithful, a
        // real rainbow's red is outermost; do not "correct").
        BlockState[] archColors = {
                Blocks.RED_TERRACOTTA.defaultBlockState(),        // meta 14
                Blocks.ORANGE_TERRACOTTA.defaultBlockState(),     // meta 1
                Blocks.YELLOW_TERRACOTTA.defaultBlockState(),     // meta 4
                Blocks.LIME_TERRACOTTA.defaultBlockState(),       // meta 5
                Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(), // meta 3
                Blocks.BLUE_TERRACOTTA.defaultBlockState(),       // meta 11
                Blocks.PURPLE_TERRACOTTA.defaultBlockState(),     // meta 10
                Blocks.PINK_TERRACOTTA.defaultBlockState(),       // meta 6
        };

        // Loop 1 — upper cloud slab (orig :6270-6278): width 12, depth 1,
        // j = +35 — a solid 24×1×3 white slab.
        for (int i = -12; i < 12; i++) {
            for (int k = -1; k <= 1; k++) {
                piece.place(cx + i, cy + 35, cz + k, white);       // orig :6276
            }
        }

        // Loop 2 — the "rain" (orig :6279-6283): k = 0, i from -11 step 3
        // while < 12 -> 8 columns {-11, -8, -5, -2, 1, 4, 7, 10}: a source
        // replacing a loop-1 slab cell at +35 and a flowing block hanging
        // under it at +34. ALL 16 cells are later overwritten by the arch
        // loop (spec S2) — dead writes, replicated because write order is
        // behavior; no liquid spreads in between (flag-2 semantics).
        for (int i = -11; i < 12; i += 3) {
            piece.place(cx + i, cy + 35, cz, water);               // orig :6281 — source
            piece.place(cx + i, cy + 34, cz, water);               // orig :6282 — flowing
        }

        // Loop 3 — lower cloud bottom ring (orig :6284-6298): width 13,
        // depth 2, j = +26 — 26×5 white perimeter ring, interior AIR (the
        // cloud has no floor, spec S3).
        for (int i = -13; i < 13; i++) {
            for (int k = -2; k <= 2; k++) {
                BlockState blk = air;                              // orig :6289
                if (i == -13 || i == 12 || k == -2 || k == 2) {
                    blk = white;                                   // orig :6290-6295
                }
                piece.place(cx + i, cy + 26, cz + k, blk);         // orig :6296
            }
        }

        // Loop 4 — lower cloud bulge ring (orig :6299-6313): width 14,
        // depth 3, j = +27 — 28×7 ring, the widest layer, interior air.
        for (int i = -14; i < 14; i++) {
            for (int k = -3; k <= 3; k++) {
                BlockState blk = air;                              // orig :6304
                if (i == -14 || i == 13 || k == -3 || k == 3) {
                    blk = white;                                   // orig :6305-6310
                }
                piece.place(cx + i, cy + 27, cz + k, blk);         // orig :6311
            }
        }

        // Loop 5 — lower cloud top ring (orig :6314-6328): width 13, depth 2,
        // j = +28 — identical shape to loop 3, one block higher.
        for (int i = -13; i < 13; i++) {
            for (int k = -2; k <= 2; k++) {
                BlockState blk = air;                              // orig :6319
                if (i == -13 || i == 12 || k == -2 || k == 2) {
                    blk = white;                                   // orig :6320-6325
                }
                piece.place(cx + i, cy + 28, cz + k, blk);         // orig :6326
            }
        }

        // Loop 6 — cloud roof slab (orig :6329-6336): width 12, depth 1,
        // j = +29 — solid 24×1×3 white slab capping the ring interior; the
        // rainbow, spawners and chests stand on it.
        for (int i = -12; i < 12; i++) {
            for (int k = -1; k <= 1; k++) {
                piece.place(cx + i, cy + 29, cz + k, white);       // orig :6334
            }
        }

        // Loop 7 — the rainbow arches (orig :6337-6347): base j = 30, all at
        // z = 0, m = 3..10 innermost -> outermost: two m-tall legs at
        // x = +m and x = -(m+1), then a top bar spanning x = -(m+1)..+m at
        // y = +30+m. Arches m >= 4 pass THROUGH the +34/+35 slab plane:
        // the m=4 top bar (y +34, x -5..4) repaints the four +34 rain
        // cells at x {-5,-2,1,4} by itself, and the m >= 5 legs/bars cover
        // the remaining rain cells at x {-11,-8,7,10} and the +35 row
        // (spec S2/S4) — so the rainbow threads the raining cloud. Keep
        // the order: slabs and rain first, arches after.
        for (int m = 3; m < 11; m++) {
            BlockState arch = archColors[m - 3];                   // orig :6339
            for (int i = 0; i < m; i++) {
                piece.place(cx + m, cy + 30 + i, cz, arch);        // orig :6341 — east leg
                piece.place(cx - (m + 1), cy + 30 + i, cz, arch);  // orig :6342 — west leg
            }
            for (int i = -(m + 1); i <= m; i++) {
                piece.place(cx + i, cy + 30 + m, cz, arch);        // orig :6345 — top bar
            }
        }

        // Six "Cloud Shark" spawners (orig :6348-6377): two 3-high pillars
        // flanking the double chest, one block inside the red (m = 3) legs,
        // in the original's order — +2 then -3 at each of +30/+31/+32. All
        // six cells sit in the arch opening's untouched air. Mob mapping:
        // "Cloud Shark" (orig OreSpawnMain.java:4095) -> orespawn:cloud_shark
        // (spec §4).
        piece.placeSpawner(cx + 2, cy + 30, cz, ModEntities.CLOUD_SHARK.get()); // orig :6348-6352
        piece.placeSpawner(cx - 3, cy + 30, cz, ModEntities.CLOUD_SHARK.get()); // orig :6353-6357
        piece.placeSpawner(cx + 2, cy + 31, cz, ModEntities.CLOUD_SHARK.get()); // orig :6358-6362
        piece.placeSpawner(cx - 3, cy + 31, cz, ModEntities.CLOUD_SHARK.get()); // orig :6363-6367
        piece.placeSpawner(cx + 2, cy + 32, cz, ModEntities.CLOUD_SHARK.get()); // orig :6368-6372
        piece.placeSpawner(cx - 3, cy + 32, cz, ModEntities.CLOUD_SHARK.get()); // orig :6373-6377

        // Double chest on the roof slab, centred under the arches (orig
        // :6378-6390): meta 2 = facing north on BOTH halves, and BOTH halves
        // filled (10 + nextInt(5) pulls each, orig :6383/:6389 — the counts
        // live in the loot table's 10-14 rolls). See the class Javadoc on
        // the flag-3 no-op and the double-chest cosmetic delta.
        piece.placeLootChest(cx, cy + 30, cz, Direction.NORTH, RAINBOW_LOOT);     // orig :6379-6384
        piece.placeLootChest(cx - 1, cy + 30, cz, Direction.NORTH, RAINBOW_LOOT); // orig :6385-6390
    }
}
