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
 * The Mini Dungeon (Islands dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeMiniDungeon} (orig GenericDungeon.java:2229-2406)
 * &mdash; one of the WGEN-042 structure pool. A 10&times;10 iron-bar cage
 * with a cobblestone floor, solid corner columns and a j=6 cobblestone rim;
 * a two-step grass-block "roof pyramid" (rings at j=7 and j=8); twelve
 * "Butterfly" spawners floating in a ring at j=9 over the open roof bowl;
 * four cobblestone corner towers up to j=10, each capped by a spawner at
 * j=11; a 4-wide oak-plank staircase with fence railings and torches
 * climbing the west side onto the roof; and six spawners plus one loot
 * chest on the floor inside.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/mini_dungeon_spec.md}):</p>
 * <ul>
 * <li>22 spawners total &mdash; 16 Butterfly, 4 Terrible Terror, 2 Lurking
 *     Terror. The Butterfly is AMBIENT category in the port, so 16 of the
 *     22 emit a harmless mob; that mix is the original's joke (butterflies
 *     everywhere, two Lurking Terrors by the chest) and is NOT to be
 *     "fixed" &mdash; spec S2.</li>
 * <li>The worldgen origin is the Islands grass block itself (orig
 *     OreSpawnWorld.java:2398-2400, no Y offset), so the j=0 cobblestone
 *     floor replaces the terrain surface &mdash; spec S3. The staircase
 *     floats 1..6 blocks above the flat plane with nothing under its
 *     treads; faithful, no supports added &mdash; spec S4.</li>
 * <li>Torches stand on fence posts via flag-2 writes (orig :2295-2298);
 *     {@code piece.place}'s UPDATE_CLIENTS write preserves them. Iron bars
 *     go in as {@code defaultBlockState()} (unconnected), the standing
 *     precedent (HospitalGenerator.java:86) &mdash; spec S5.</li>
 * <li>The j=9 spawner ring floats on air (j=8's interior cells at i,k 3..6
 *     are air/never written); faithful &mdash; spec S6.</li>
 * <li>The original's only RNG draw, the chest fill count
 *     {@code 4 + nextInt(5)} (orig :2404), moved into the loot JSON's rolls
 *     (uniform 4-8), so this generator consumes ZERO random draws and
 *     satisfies the per-chunk-replay stitching contract vacuously &mdash;
 *     spec &sect;10. Its only world reads were the 23 tile-entity fetches
 *     across 12 call sites at just-written positions (orig :2308 runs 12
 *     times in the ring loop, plus
 *     :2319/:2329/:2339/:2349 corners, :2357-:2397 floor, :2402 chest),
 *     absorbed by the {@code placeSpawner}/{@code placeLootChest} helpers
 *     &mdash; spec &sect;9.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/mini_dungeon} transcribes the
 * original 6-entry {@code MiniContentsList} (orig GenericDungeon.java:45,
 * total weight 190, all food/utility &mdash; no weapons, spec S9) with the
 * original 4-8 stack rolls; the random-slot collision quirk is dropped, the
 * same documented approximation as every other chest-list conversion since
 * Phase C.</p>
 */
final class MiniDungeonGenerator {

    /** Loot for the floor chest at (+3, +1, +3) (orig GenericDungeon.java:45 + :2401-2405). */
    private static final ResourceKey<LootTable> MINI_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/mini_dungeon"));

    private MiniDungeonGenerator() {}

    /**
     * Direct port of {@code makeMiniDungeon(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:2229-2406). {@code origin} is the Islands
     * grass block found by the {@code addD4Mini} Y 20&rarr;5 scan (worldgen,
     * orig OreSpawnWorld.java:2391-2405 &mdash; anchor AT the grass, no
     * offset) or the Dungeon Spawner Block position (outcome 16, orig
     * DungeonSpawnerBlock.java:101-103 &mdash; a floating/embedded cage
     * there is faithful behavior).
     *
     * <p>Write order preserved from the original: cage box &rarr; j=7 grass
     * ring &rarr; j=8 grass ring &rarr; staircase &rarr; j=9 spawner ring
     * &rarr; corner pillars + top spawners &rarr; 6 floor spawners &rarr;
     * chest. Later writes only fill previously-written air or virgin cells
     * (no cross-loop erasures, spec S7).</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState oakPlanks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop 1 — 10×10×7 iron-bar cage (orig :2238-2266). Sequential
        // assignments, last matching rule wins: default air; perimeter iron
        // bars; the four corners cobblestone at every j; the whole j=0 floor
        // cobblestone; the j=6 perimeter rim cobblestone. Net: cobble floor,
        // bar walls j=1..5 with solid cobble corner columns, cobble rim ring
        // at j=6, interior carved to air.
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 10; k++) {
                for (int j = 0; j < 7; j++) {
                    boolean perimeter = i == 0 || k == 0 || i == 9 || k == 9;      // orig :2242
                    BlockState blk = air;                                          // orig :2241
                    if (perimeter) {
                        blk = ironBars;                                            // orig :2243
                    }
                    if ((i == 0 || i == 9) && (k == 0 || k == 9)) {
                        blk = cobble;                                              // orig :2245-2256 (four corner rules)
                    }
                    if (j == 0) {
                        blk = cobble;                                              // orig :2257-2259
                    }
                    if (j == 6 && perimeter) {
                        blk = cobble;                                              // orig :2260-2262
                    }
                    piece.place(cx + i, cy + j, cz + k, blk);                      // orig :2263
                }
            }
        }

        // Loop 2 — grass ring at j=7, inset 1 from the cage rim (orig
        // :2267-2276): ring cells grass block, interior air.
        for (int i = 1; i < 9; i++) {
            for (int k = 1; k < 9; k++) {
                BlockState blk = (i == 1 || i == 8 || k == 1 || k == 8) ? grass : air; // orig :2270-2273
                piece.place(cx + i, cy + 7, cz + k, blk);                          // orig :2274
            }
        }

        // Loop 3 — grass ring at j=8, inset 2 (orig :2277-2286); together
        // with loop 2 a stepped grass roof pyramid.
        for (int i = 2; i < 8; i++) {
            for (int k = 2; k < 8; k++) {
                BlockState blk = (i == 2 || i == 7 || k == 2 || k == 7) ? grass : air; // orig :2280-2283
                piece.place(cx + i, cy + 8, cz + k, blk);                          // orig :2284
            }
        }

        // Staircase west of the cage (orig :2287-2301): 6 steps from
        // (-6, +1) up to (-1, +6), 4 planks wide (z +3..+6), fence railings
        // one above the outer treads, torches on the fences one above that.
        // The top step is level with the j=6 rim, walking onto the j=7 ring.
        {
            int i = -6;                                                            // orig :2287
            int j = 1;                                                             // orig :2288
            int k = 3;                                                             // orig :2289
            for (int m = 0; m < 6; m++) {                                          // orig :2290
                piece.place(cx + i, cy + j, cz + k, oakPlanks);                    // orig :2291
                piece.place(cx + i, cy + j, cz + k + 1, oakPlanks);                // orig :2292
                piece.place(cx + i, cy + j, cz + k + 2, oakPlanks);                // orig :2293
                piece.place(cx + i, cy + j, cz + k + 3, oakPlanks);                // orig :2294
                piece.place(cx + i, cy + j + 1, cz + k, oakFence);                 // orig :2295
                piece.place(cx + i, cy + j + 1, cz + k + 3, oakFence);             // orig :2296
                piece.place(cx + i, cy + j + 2, cz + k, torch);                    // orig :2297
                piece.place(cx + i, cy + j + 2, cz + k + 3, torch);                // orig :2298
                ++i;                                                               // orig :2299
                ++j;                                                               // orig :2300
            }
        }

        // Loop 4 — Butterfly spawner ring at j=9 (orig :2302-2312): the 12
        // ring cells of the 4×4 at i,k 3..6 (interior 2×2 skipped) each get
        // a "Butterfly" spawner, floating over the open roof bowl. (The
        // original's `blk = air` at :2305 is a dead assignment — nothing
        // writes it.)
        for (int i = 3; i < 7; i++) {
            for (int k = 3; k < 7; k++) {
                if (i != 3 && i != 6 && k != 3 && k != 6) continue;                // orig :2306
                piece.placeSpawner(cx + i, cy + 9, cz + k, ModEntities.ENTITY_BUTTERFLY.get()); // orig :2307-2310
            }
        }

        // Four corner towers (orig :2313-2352): cobblestone extending the
        // loop-1 corner columns through j=7..10, spawner cap at j=11 (the
        // original's j falls out of its `j < 11` loop at 11).
        placeCornerTower(piece, cx, cy, cz, 0, 0, ModEntities.ENTITY_TERRIBLE_TERROR.get()); // orig :2313-2322
        placeCornerTower(piece, cx, cy, cz, 9, 9, ModEntities.ENTITY_BUTTERFLY.get());       // orig :2323-2332
        placeCornerTower(piece, cx, cy, cz, 0, 9, ModEntities.ENTITY_TERRIBLE_TERROR.get()); // orig :2333-2342
        placeCornerTower(piece, cx, cy, cz, 9, 0, ModEntities.ENTITY_BUTTERFLY.get());       // orig :2343-2352

        // Six interior floor spawners at j=1, overwriting loop-1 air
        // (orig :2353-2400): Terrible Terrors on the (1,1)/(8,8) diagonal,
        // Butterflies on the (8,1)/(1,8) diagonal, Lurking Terrors at the
        // center next to the chest.
        piece.placeSpawner(cx + 1, cy + 1, cz + 1, ModEntities.ENTITY_TERRIBLE_TERROR.get()); // orig :2353-2360
        piece.placeSpawner(cx + 8, cy + 1, cz + 8, ModEntities.ENTITY_TERRIBLE_TERROR.get()); // orig :2361-2368
        piece.placeSpawner(cx + 8, cy + 1, cz + 1, ModEntities.ENTITY_BUTTERFLY.get());       // orig :2369-2376
        piece.placeSpawner(cx + 1, cy + 1, cz + 8, ModEntities.ENTITY_BUTTERFLY.get());       // orig :2377-2384
        piece.placeSpawner(cx + 4, cy + 1, cz + 4, ModEntities.ENTITY_LURKING_TERROR.get());  // orig :2385-2392
        piece.placeSpawner(cx + 5, cy + 1, cz + 5, ModEntities.ENTITY_LURKING_TERROR.get());  // orig :2393-2400

        // Loot chest at (+3, +1, +3) (orig :2401-2405). Fill count
        // 4 + nextInt(5) lives in the loot table's rolls (uniform 4-8).
        piece.placeLootChest(cx + 3, cy + 1, cz + 3, MINI_LOOT);
    }

    /**
     * One corner tower (orig GenericDungeon.java:2313-2322 and its three
     * byte-identical siblings :2323-2352): cobblestone at j=7..10, then the
     * capping spawner at j=11 &mdash; the original reuses {@code j}, which
     * exits its {@code j < 11} loop holding 11.
     */
    private static void placeCornerTower(LegacyDungeonPiece piece, int cx, int cy, int cz,
                                         int i, int k, net.minecraft.world.entity.EntityType<?> mob) {
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        for (int j = 7; j < 11; j++) {
            piece.place(cx + i, cy + j, cz + k, cobble);   // orig :2315-2317
        }
        piece.placeSpawner(cx + i, cy + 11, cz + k, mob);  // orig :2318-2321
    }
}
