package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Stinky House (Islands dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeStinkyHouse} (orig GenericDungeon.java:5314-5381)
 * &mdash; one of the WGEN-042 structure pool. A ruined 13&times;10 oak-plank
 * shack, walls 2 high under a flat plank roof, with 8 corner-adjacent glass
 * -pane windows and a 2&times;2 doorway centered on the &minus;X wall, sitting
 * directly ON the Islands grass (the untouched terrain is the floor) inside a
 * gap-toothed 25&times;17 oak-fence yard scattered with dead bushes; two
 * spawners (Stink Bug + Stinky) and one loot chest stand on the dirt floor.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/stinky_house_spec.md}):</p>
 * <ul>
 * <li>The house is generated PRE-RUINED: an unconditional 1/10 decay roll
 *     (orig :5356-5358) runs over every one of the 390 shell cells AFTER
 *     walls/windows/roof are decided, so roof holes, missing planks and
 *     shattered windows are original content &mdash; do not "repair", and the
 *     roll stays ahead of the doorway force-clear so the door is always fully
 *     open &mdash; spec S2.</li>
 * <li>The yard loop NEVER writes air (orig :5339 {@code continue}): fence
 *     gaps expose whatever terrain/vegetation was already there. The house
 *     loop, by contrast, writes air unconditionally (orig :5362) and clears
 *     terrain inside its 13&times;10&times;3 volume &mdash; spec S3.</li>
 * <li>The worldgen origin is the Islands grass block itself (orig
 *     OreSpawnWorld.java:2283-2284, no +1): every write lands at
 *     {@code cposy + 1} and above &mdash; no foundation, no floor, nothing at
 *     or below origin level &mdash; spec S4. The Dungeon Spawner Block path
 *     (type 39, orig DungeonSpawnerBlock.java:170-172) builds at the clicked
 *     position in any dimension; floating or embedded there is faithful
 *     behavior &mdash; spec &sect;9.</li>
 * <li>Dead bushes can stand IN the fence line: a perimeter post knocked out
 *     by the 1/3 roll re-rolls the 1/10 bush chance (orig :5333-5338), so
 *     the two rolls' order and conditions are kept exactly &mdash; spec
 *     S6.</li>
 * <li>The 8 windows sit one cell in from each corner at wall height 1 (orig
 *     :5350-5352); the long-wall midsections have none. Faithful &mdash; do
 *     not recenter &mdash; spec S8.</li>
 * <li>Oak fence and glass panes go in as {@code defaultBlockState()}
 *     (unconnected) via the flag-2 {@code piece.place}, the standing
 *     precedent (MiniDungeonGenerator.java:92) for the 1.7.10-render-time vs
 *     modern-blockstate connection difference &mdash; spec S10.</li>
 * <li>RNG: the yard's 1/3 fence-decay and 1/10 bush rolls and the shell's
 *     1/10 decay roll all stay in the generator &mdash; every conditional
 *     draw's condition depends only on loop indices and earlier draws, never
 *     on world state, so all per-chunk replay passes consume the identical
 *     stream (spec &sect;11); the air-skip is a write-skip, not a draw-skip.
 *     The chest fill count {@code 8 + nextInt(5)} (orig :5379) moved into
 *     the loot JSON's rolls (uniform 8-12). The builder performs ZERO world
 *     reads (spec &sect;10); its only tile-entity fetches (orig :5367/:5372
 *     spawners, :5377 chest) were self-reads at just-written positions,
 *     absorbed by the {@code placeSpawner}/{@code placeLootChest}
 *     helpers.</li>
 * <li>Spawn-gate interplay: the port Stink Bug's {@code y >= 50} rule is
 *     bypassed near its own spawner (EntityStinkBug.java:117-123), which is
 *     what lets this spawner work at Islands grass level (Y 6-21); Stinky's
 *     daytime/buddy-cap gate (EntityStinky.java:505-511) throttles the
 *     second spawner naturally. Not a bug &mdash; spec S7.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/stinky_house} transcribes the
 * original 7-entry {@code StinkyHouseContentsList} (orig
 * GenericDungeon.java:28, total weight 215) with the original 8-12 fill
 * rolls; the random-slot collision quirk is dropped, the same documented
 * approximation as every other chest-list conversion since Phase C.</p>
 */
final class StinkyHouseGenerator {

    /** Loot for the house-center chest at (+6, +1, +4) (orig GenericDungeon.java:28 + :5376-5380). */
    private static final ResourceKey<LootTable> STINKY_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/stinky_house"));

    private StinkyHouseGenerator() {}

    /**
     * Direct port of {@code makeStinkyHouse(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:5314-5381). {@code origin} is the Islands
     * grass block found by the {@code addD4StinkyHouse} Y 20&rarr;5 scan
     * (worldgen, orig OreSpawnWorld.java:2276-2297 &mdash; anchor AT the
     * grass, no offset) or the Dungeon Spawner Block position (outcome 39,
     * orig DungeonSpawnerBlock.java:170-172).
     *
     * <p>Write order preserved from the original: yard fence/bushes first
     * (loop A), then the house shell (loop B, overwriting any loop-A debris
     * inside its footprint), then the 2 spawners, then the chest. Method
     * -local constants folded in: {@code height = 2} (orig :5322),
     * {@code width = 9} (:5323), {@code length = 12} (:5324),
     * {@code yardwidth = 16} (:5325), {@code yardlength = 24} (:5326).</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState deadBush = Blocks.DEAD_BUSH.defaultBlockState();
        BlockState oakPlanks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState glassPane = Blocks.GLASS_PANE.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop A — decayed yard fence + dead bushes (orig :5327-5342), one
        // layer at y+1 spanning rel X −5..+19, rel Z −4..+12. Sequential
        // assignments, source order: default air; perimeter ring oak fence;
        // 1/3 of fence posts knocked back to air; every air-at-this-point
        // cell (interior AND knocked-out posts) has a 1/10 dead-bush chance;
        // air is then SKIPPED, never written — the yard leaves terrain and
        // vegetation untouched (spec S3/S6).
        for (int i = 0; i <= 24; i++) {                                            // orig :5327 (yardlength, :5326)
            for (int k = 0; k <= 16; k++) {                                        // orig :5328 (yardwidth, :5325)
                BlockState blk = air;                                              // orig :5329
                if (i == 0 || i == 24 || k == 0 || k == 16) {
                    blk = oakFence;                                                // orig :5330-5332
                }
                if (blk == oakFence && random.nextInt(3) == 1) {
                    blk = air;                                                     // orig :5333-5335 (fence decay)
                }
                if (blk == air && random.nextInt(10) == 1) {
                    blk = deadBush;                                                // orig :5336-5338 (bush scatter)
                }
                if (blk == air) {
                    continue;                                                      // orig :5339 — write-skip, not a draw-skip
                }
                piece.place(cx + i - 5, cy + 1, cz + k - 4, blk);                  // orig :5340
            }
        }

        // Loop B — the house shell, 13×10×3 at rel Y +1..+3 (orig
        // :5343-5365). Sequential assignments, last matching rule wins:
        // default air; perimeter oak planks; glass panes at wall height 1
        // one cell in from each corner (8 windows, spec S8); the whole j=2
        // layer planks (flat roof); an UNCONDITIONAL 1/10 decay roll for
        // every one of the 390 cells (spec S2); the 2×2 doorway in the i=0
        // wall forced open AFTER the decay roll; then the write happens
        // unconditionally, air included — terrain inside the volume is
        // cleared (contrast loop A).
        for (int i = 0; i <= 12; i++) {                                            // orig :5343 (length, :5324)
            for (int k = 0; k <= 9; k++) {                                         // orig :5344 (width, :5323)
                for (int j = 0; j <= 2; j++) {                                     // orig :5345 (height, :5322)
                    BlockState blk = air;                                          // orig :5346
                    if (i == 0 || i == 12 || k == 0 || k == 9) {
                        blk = oakPlanks;                                           // orig :5347-5349 (walls)
                    }
                    if (blk == oakPlanks && j == 1 && (i == 1 || i == 11 || k == 1 || k == 8)) {
                        blk = glassPane;                                           // orig :5350-5352 (windows; length−1=11, width−1=8)
                    }
                    if (j == 2) {
                        blk = oakPlanks;                                           // orig :5353-5355 (roof slab)
                    }
                    if (random.nextInt(10) == 1) {
                        blk = air;                                                 // orig :5356-5358 (decay, unconditional draw)
                    }
                    if ((j == 0 || j == 1) && i == 0 && (k == 4 || k == 5)) {
                        blk = air;                                                 // orig :5359-5361 (doorway; De-Morganed, width/2=4)
                    }
                    piece.place(cx + i, cy + j + 1, cz + k, blk);                  // orig :5362 — unconditional, air included
                }
            }
        }

        // Two floor-level spawners inside the house at y+1 (the j=0 layer,
        // written air by loop B): Stink Bug near the door-side corner,
        // Stinky in the far corner (orig :5366-5375).
        piece.placeSpawner(cx + 2, cy + 1, cz + 2, ModEntities.ENTITY_STINK_BUG.get());  // orig :5366-5370 ("Stink Bug")
        piece.placeSpawner(cx + 10, cy + 1, cz + 7, ModEntities.ENTITY_STINKY.get());    // orig :5371-5375 ("Stinky", length−2/width−2)

        // Loot chest at the house center (+6, +1, +4) = (length/2, width/2)
        // (orig :5376-5380). Fill count 8 + nextInt(5) lives in the loot
        // table's rolls (uniform 8-12). Meta-0 chest — no facing.
        piece.placeLootChest(cx + 6, cy + 1, cz + 4, STINKY_LOOT);
    }
}
