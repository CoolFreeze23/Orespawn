package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
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
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Ender Castle, ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeEnderCastle} (orig GenericDungeon.java:3207-3493)
 * and its private tower helper {@code makeAColumn} (orig :3495-3623) &mdash;
 * one of the WGEN-042 structure pool. A 23&times;23 bedrock keep on a
 * 29&times;29 obsidian plate ringed with iron bars, four obsidian corner
 * towers with nether-brick spiral staircases, a rooftop plaza with a 5&times;5
 * lava pool around a dragon-egg pedestal and a trophy ender chest, decorative
 * "ancient dried spawn egg" and eye-of-ender wall bands, hanging ender-pearl
 * stalactites under the outer parapet, 16 mob spawners (Ender Reaper / Ender
 * Knight / CaveFisher) and 3 loot chests in wall alcoves. Generates in TWO
 * dimensions: the End (orig OreSpawnWorld.java:1557-1570) and the Islands D4
 * {@code i == 7} roll (orig :2322-2343), plus Dungeon Spawner Block outcome 27
 * (orig DungeonSpawnerBlock.java:134-136).
 *
 * <p><b>Mapping decisions</b> (full tables in
 * {@code phase_d_reports/d6_extraction/ender_castle_spec.md}):</p>
 * <ul>
 *   <li><b>RNG:</b> the original drew all build randomness from
 *       {@code world.field_73012_v} (the live world RNG, orig :3244, :3273,
 *       :3275, :3461, :3476, :3491); the port threads the deterministic
 *       per-piece {@link RandomSource} through the identical call sequence.
 *       Every draw sits behind conditions of loop variables only (never chunk
 *       or world state), so all chunk passes replay the same stream
 *       (stitching contract). The Dungeon Spawner Block path keeps live-RNG
 *       behaviour via {@code buildNow}'s runtime override.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/ender_castle} transcribes
 *       the 8-entry {@code EnderCastleContentsList} (orig :40, total weight
 *       270) with the original 6-10 stack rolls ({@code 6 + nextInt(5)},
 *       orig :3461/:3476/:3488-3491); the random-slot collision quirk is
 *       dropped &mdash; the same documented approximation as every chest-list
 *       conversion since Phase C.</li>
 *   <li><b>Chest facings:</b> the original stamped raw metadata &mdash; west
 *       chest meta 2 (NORTH &mdash; a likely original bug on a west wall,
 *       kept as-is per spec &sect;13.10), north chest meta 3 (SOUTH), south
 *       chest meta 4 (WEST). The trophy ender chest (orig :3325, meta 2) is a
 *       plain block with NO loot table (spec &sect;13.4).</li>
 *   <li><b>Spawn-egg wall band:</b> the four "Ancient Dried ... Spawn Egg"
 *       blocks are decorative {@code OreGenericEgg}s (50% 5-9 XP on break),
 *       not spawners &mdash; already modelled by the port blocks.</li>
 *   <li><b>Zero terrain reads:</b> the original build never reads world
 *       blocks (spec &sect;12) &mdash; no in-memory state model needed;
 *       every {@code bid == ...} comparison is against the just-assigned
 *       local, reproduced here verbatim.</li>
 * </ul>
 */
final class EnderCastleGenerator {

    /** Loot for the 3 alcove chests (orig GenericDungeon.java:40 + :3461). */
    private static final ResourceKey<LootTable> ENDER_CASTLE_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/ender_castle"));

    /** Keep edge length minus one; walls at 0 and 22 (orig GD:3212). */
    private static final int WIDTH = 22;
    /** Keep wall top Y offset (orig GD:3213). */
    private static final int HEIGHT = 12;

    private EnderCastleGenerator() {}

    /**
     * Direct port of {@code makeEnderCastle(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:3207-3493). {@code origin} is the anchor:
     * on the End path the air block directly on end stone (orig
     * OreSpawnWorld.java:1564-1566), on the Islands path the grass block
     * itself (orig :2328-2338), on the Dungeon Spawner Block path the
     * spawner block's own position (orig DungeonSpawnerBlock.java:134-136).
     * The obsidian base plate overwrites that layer in all three cases.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();

        // orig :3219-3232 — 29x29 obsidian base plate (-3..+25) at y+0, with
        // an air sweep at y+1 that becomes an iron-bar fence on the plate's
        // outermost ring (i or k == -3 / +25; the decompiler's `|` on the k
        // clause is a plain boolean OR, spec header note).
        for (int i = -3; i <= WIDTH + 3; i++) {
            for (int k = -3; k <= WIDTH + 3; k++) {
                for (int j = 0; j <= 1; j++) {
                    BlockState bid = air;
                    if (j == 0) {
                        bid = obsidian;
                    }
                    if (j == 1 && (i == -3 || i == WIDTH + 3 || k == WIDTH + 3 || k == -3)) {
                        bid = ironBars;
                    }
                    piece.place(x + i, y + j, z + k, bid);
                }
            }
        }

        // orig :3233-3264 — main keep shell: 23x23, y+1..+12. Perimeter cells
        // are bedrock, interior is carved to air. Three decorated wall bands:
        //  - j == 12 (wall top): even (i+k) cells become air -> crenellation
        //    gaps (orig :3240-3242);
        //  - j == 10 (HEIGHT-2): even wall cells roll nextInt(4) for one of
        //    the four decorative "ancient dried spawn egg" blocks
        //    (orig :3243-3257). The draw's condition uses loop variables
        //    only, so every chunk pass rolls identically;
        //  - j == 7: odd wall cells become Eye-of-Ender blocks (orig :3258-3260).
        for (int i = 0; i <= WIDTH; i++) {
            for (int k = 0; k <= WIDTH; k++) {
                for (int j = 1; j <= HEIGHT; j++) {
                    BlockState bid = air;
                    boolean wall = i == 0 || i == WIDTH || k == WIDTH || k == 0;
                    if (wall) {
                        bid = bedrock;
                    }
                    if (j == HEIGHT && wall && ((i + k) & 1) == 0) {
                        bid = air;                                      // orig :3240-3242
                    }
                    if (j == HEIGHT - 2 && wall && ((i + k) & 1) == 0) {
                        int which = random.nextInt(4);                  // orig :3244
                        bid = switch (which) {
                            case 0 -> ModBlocks.ENDER_KNIGHT_SPAWN_BLOCK.get().defaultBlockState();
                            case 1 -> ModBlocks.ENDER_REAPER_SPAWN_BLOCK.get().defaultBlockState();
                            case 2 -> ModBlocks.ENDERMAN_SPAWN_BLOCK.get().defaultBlockState();
                            default -> ModBlocks.ENDER_DRAGON_SPAWN_BLOCK.get().defaultBlockState();
                        };
                    }
                    if (j == 7 && wall && ((i + k) & 1) != 0) {
                        bid = ModBlocks.BLOCK_EYE_OF_ENDER.get().defaultBlockState(); // orig :3258-3260
                    }
                    piece.place(x + i, y + j, z + k, bid);
                }
            }
        }

        // orig :3265-3292 — outer parapet ring (-1..+23): bedrock at j == 6
        // and j == 9..11 on the ring, a checkered walkway at j == 7 (odd
        // (i+k) ring cells only), and hanging ender-pearl stalactites under
        // the j == 6 ring: 1/2 chance of a pearl at j-1 = +5, then a nested
        // 1/3 chance of a second at j-2 = +4 (orig :3273-3278). This loop
        // NEVER writes air (`continue` on air, orig :3288), so terrain and
        // earlier writes survive between the ring and the keep. Both pearl
        // rolls' conditions are loop-variable-only (ring cell at j == 6), so
        // the RNG stream is pass-identical; the nested roll depends only on
        // the outer roll's result, which is itself deterministic per pass.
        BlockState pearlBlock = ModBlocks.BLOCK_ENDER_PEARL.get().defaultBlockState();
        for (int i = -1; i <= WIDTH + 1; i++) {
            for (int k = -1; k <= WIDTH + 1; k++) {
                for (int j = 1; j <= HEIGHT - 1; j++) {
                    BlockState bid = air;
                    boolean ring = i == -1 || i == WIDTH + 1 || k == WIDTH + 1 || k == -1;
                    if (j == 6 || j > 8) {                              // orig :3269
                        if (ring) {
                            bid = bedrock;
                        }
                        if (j == 6 && bid != air && random.nextInt(2) == 1) { // orig :3273
                            piece.place(x + i, y + j - 1, z + k, pearlBlock);
                            if (random.nextInt(3) == 1) {               // orig :3275
                                piece.place(x + i, y + j - 2, z + k, pearlBlock);
                            }
                        }
                    }
                    if (j == 7) {                                       // orig :3280-3287
                        if (ring) {
                            bid = bedrock;
                        }
                        if (bid == bedrock && ((i + k) & 1) == 0) {
                            bid = air;
                        }
                    }
                    if (bid == air) continue;                           // orig :3288
                    piece.place(x + i, y + j, z + k, bid);
                }
            }
        }

        // orig :3293-3296 — the four corner towers, height arg = HEIGHT+1 = 13.
        // Directions 0..3 pick which corner of each tower is opened toward
        // the castle and phase the spiral stair (see makeColumn).
        makeColumn(piece, x - 2, y, z - 2, HEIGHT + 1, 0);
        makeColumn(piece, x + WIDTH - 2, y, z - 2, HEIGHT + 1, 1);
        makeColumn(piece, x - 2, y, z + WIDTH - 2, HEIGHT + 1, 2);
        makeColumn(piece, x + WIDTH - 2, y, z + WIDTH - 2, HEIGHT + 1, 3);

        // orig :3297-3306 — rooftop plaza at y+8 (1..21): obsidian with a
        // bedrock cross (i == 11 or k == 11) plus both diagonals (i == k,
        // i == 22-k) inlaid.
        for (int i = 1; i <= WIDTH - 1; i++) {
            for (int k = 1; k <= WIDTH - 1; k++) {
                BlockState bid = obsidian;
                if (i == WIDTH / 2 || k == WIDTH / 2 || i == k || i == WIDTH - k) {
                    bid = bedrock;
                }
                piece.place(x + i, y + 8, z + k, bid);
            }
        }

        // orig :3307-3313 — 5x5 rooftop lava pool at y+9, centred on (11,11),
        // one block above the plaza. Source states with the piece's
        // UPDATE_CLIENTS flag = the original's flag-2 setBlockFast: no fluid
        // updates are scheduled at build time (spec §13.5), so the pool sits
        // exposed on the diagonal gaps exactly like 1.7.10.
        BlockState lava = Blocks.LAVA.defaultBlockState();
        for (int i = -2; i <= 2; i++) {
            for (int k = -2; k <= 2; k++) {
                piece.place(x + i + WIDTH / 2, y + 9, z + k + WIDTH / 2, lava);
            }
        }

        // orig :3314-3324 — partial bedrock rim around the pool at y+9: four
        // 3-long edge bars at offset ±3 from centre, the four (±2,±2)
        // corners, and the exact centre block (the pedestal footing).
        for (int m = -1; m <= 1; m++) {
            piece.place(x + WIDTH / 2 + m, y + 9, z + WIDTH / 2 + 3, bedrock);
            piece.place(x + WIDTH / 2 + m, y + 9, z + WIDTH / 2 - 3, bedrock);
            piece.place(x + WIDTH / 2 + 3, y + 9, z + WIDTH / 2 + m, bedrock);
            piece.place(x + WIDTH / 2 - 3, y + 9, z + WIDTH / 2 + m, bedrock);
        }
        piece.place(x + WIDTH / 2 - 2, y + 9, z + WIDTH / 2 - 2, bedrock);   // orig :3320
        piece.place(x + WIDTH / 2 + 2, y + 9, z + WIDTH / 2 + 2, bedrock);   // orig :3321
        piece.place(x + WIDTH / 2 - 2, y + 9, z + WIDTH / 2 + 2, bedrock);   // orig :3322
        piece.place(x + WIDTH / 2 + 2, y + 9, z + WIDTH / 2 - 2, bedrock);   // orig :3323
        piece.place(x + WIDTH / 2, y + 9, z + WIDTH / 2, bedrock);           // orig :3324

        // orig :3325 — the central TROPHY ender chest at (11, +10, 11),
        // meta 2 = facing NORTH. A plain vanilla ender chest — deliberately
        // no loot table (spec §13.4: player-backed inventory).
        piece.place(x + WIDTH / 2, y + 10, z + WIDTH / 2,
                Blocks.ENDER_CHEST.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));

        // orig :3326-3338 — centre pedestal spike above the chest: obsidian
        // at +11, a bedrock cross at +12, four standing torches at +13
        // around a bedrock core, a bedrock cap at +14 and the DRAGON EGG
        // trophy at +15. Torches stand on the flag-2 writes exactly like the
        // original (no neighbour updates -> no pop-off).
        BlockState torch = Blocks.TORCH.defaultBlockState();
        piece.place(x + WIDTH / 2, y + 11, z + WIDTH / 2, obsidian);         // orig :3326
        piece.place(x + WIDTH / 2, y + 12, z + WIDTH / 2, bedrock);          // orig :3327
        piece.place(x + WIDTH / 2 - 1, y + 12, z + WIDTH / 2, bedrock);      // orig :3328
        piece.place(x + WIDTH / 2 + 1, y + 12, z + WIDTH / 2, bedrock);      // orig :3329
        piece.place(x + WIDTH / 2, y + 12, z + WIDTH / 2 - 1, bedrock);      // orig :3330
        piece.place(x + WIDTH / 2, y + 12, z + WIDTH / 2 + 1, bedrock);      // orig :3331
        piece.place(x + WIDTH / 2 - 1, y + 13, z + WIDTH / 2, torch);        // orig :3332
        piece.place(x + WIDTH / 2 + 1, y + 13, z + WIDTH / 2, torch);        // orig :3333
        piece.place(x + WIDTH / 2, y + 13, z + WIDTH / 2 - 1, torch);        // orig :3334
        piece.place(x + WIDTH / 2, y + 13, z + WIDTH / 2 + 1, torch);        // orig :3335
        piece.place(x + WIDTH / 2, y + 13, z + WIDTH / 2, bedrock);          // orig :3336
        piece.place(x + WIDTH / 2, y + 14, z + WIDTH / 2, bedrock);          // orig :3337
        piece.place(x + WIDTH / 2, y + 15, z + WIDTH / 2,
                Blocks.DRAGON_EGG.defaultBlockState());                      // orig :3338

        // orig :3339-3378 — 8 rooftop spawner pairs at the four plaza
        // corners (11±5, 11±5): Ender Reaper at y+9 with an Ender Knight
        // stacked directly above at y+10.
        piece.placeSpawner(x + WIDTH / 2 + 5, y + 9, z + WIDTH / 2 + 5, ModEntities.ENDER_REAPER.get());  // orig :3339
        piece.placeSpawner(x + WIDTH / 2 + 5, y + 10, z + WIDTH / 2 + 5, ModEntities.ENDER_KNIGHT.get()); // orig :3344
        piece.placeSpawner(x + WIDTH / 2 - 5, y + 9, z + WIDTH / 2 + 5, ModEntities.ENDER_REAPER.get());  // orig :3349
        piece.placeSpawner(x + WIDTH / 2 - 5, y + 10, z + WIDTH / 2 + 5, ModEntities.ENDER_KNIGHT.get()); // orig :3354
        piece.placeSpawner(x + WIDTH / 2 + 5, y + 9, z + WIDTH / 2 - 5, ModEntities.ENDER_REAPER.get());  // orig :3359
        piece.placeSpawner(x + WIDTH / 2 + 5, y + 10, z + WIDTH / 2 - 5, ModEntities.ENDER_KNIGHT.get()); // orig :3364
        piece.placeSpawner(x + WIDTH / 2 - 5, y + 9, z + WIDTH / 2 - 5, ModEntities.ENDER_REAPER.get());  // orig :3369
        piece.placeSpawner(x + WIDTH / 2 - 5, y + 10, z + WIDTH / 2 - 5, ModEntities.ENDER_KNIGHT.get()); // orig :3374

        // orig :3379-3409 — ground-level ceiling ring at y+4 (bedrock where
        // i <= 5, k <= 5, i >= 17 or k >= 17; the interior 6..16 stays open
        // as the rooftop light/drop shaft — air cells are NOT written,
        // orig :3386), plus the inner iron-bar cage: 3-high bar walls at
        // y+5..+7 along i == 5, i == 17, k == 5 and k == 17 (each spanning
        // 5..17 on the other axis).
        for (int i = 1; i <= WIDTH - 1; i++) {
            for (int k = 1; k <= WIDTH - 1; k++) {
                if (i <= 5 || k <= 5 || i >= WIDTH - 5 || k >= WIDTH - 5) { // orig :3383
                    piece.place(x + i, y + 4, z + k, bedrock);
                }
                if (i == 5 && k >= 5 && k <= WIDTH - 5) {               // orig :3389
                    piece.place(x + i, y + 5, z + k, ironBars);
                    piece.place(x + i, y + 6, z + k, ironBars);
                    piece.place(x + i, y + 7, z + k, ironBars);
                }
                if (i == WIDTH - 5 && k >= 5 && k <= WIDTH - 5) {       // orig :3394
                    piece.place(x + i, y + 5, z + k, ironBars);
                    piece.place(x + i, y + 6, z + k, ironBars);
                    piece.place(x + i, y + 7, z + k, ironBars);
                }
                if (k == 5 && i >= 5 && i <= WIDTH - 5) {               // orig :3399
                    piece.place(x + i, y + 5, z + k, ironBars);
                    piece.place(x + i, y + 6, z + k, ironBars);
                    piece.place(x + i, y + 7, z + k, ironBars);
                }
                if (k == WIDTH - 5 && i >= 5 && i <= WIDTH - 5) {       // orig :3404
                    piece.place(x + i, y + 5, z + k, ironBars);
                    piece.place(x + i, y + 6, z + k, ironBars);
                    piece.place(x + i, y + 7, z + k, ironBars);
                }
            }
        }

        // orig :3410-3428 — 3-wide bedrock entry stairs on the east side,
        // stepping down from the cage: (16, +3), (15, +2), (14, +1), each
        // spanning z 10..12 (= WIDTH/2 ± 1).
        for (int m = -1; m <= 1; m++) {                                 // orig :3414 (j=3, i=16)
            piece.place(x + WIDTH - 6, y + 3, z + WIDTH / 2 + m, bedrock);
        }
        for (int m = -1; m <= 1; m++) {                                 // orig :3420 (j=2, i=15)
            piece.place(x + WIDTH - 7, y + 2, z + WIDTH / 2 + m, bedrock);
        }
        for (int m = -1; m <= 1; m++) {                                 // orig :3426 (j=1, i=14)
            piece.place(x + WIDTH - 8, y + 1, z + WIDTH / 2 + m, bedrock);
        }

        // orig :3429-3435 — cage doorway: a 3x3 air hole carved through the
        // i == 17 bar wall at y+5..+7, z 10..12, above the stair top.
        for (int m = -1; m <= 1; m++) {
            piece.place(x + WIDTH - 5, y + 5, z + WIDTH / 2 + m, air);
            piece.place(x + WIDTH - 5, y + 6, z + WIDTH / 2 + m, air);
            piece.place(x + WIDTH - 5, y + 7, z + WIDTH / 2 + m, air);
        }

        // orig :3436-3446 — pit spawners at the centre of the shaft floor:
        // Ender Reaper at (11, +1, 11), Ender Knight stacked at (11, +2, 11).
        piece.placeSpawner(x + WIDTH / 2, y + 1, z + WIDTH / 2, ModEntities.ENDER_REAPER.get());  // orig :3437
        piece.placeSpawner(x + WIDTH / 2, y + 2, z + WIDTH / 2, ModEntities.ENDER_KNIGHT.get());  // orig :3442

        // orig :3447-3462 — WEST wall alcove at y+5: two CaveFisher spawners
        // flanking a loot chest at (1, +5, 11). The chest keeps the
        // original's meta 2 = NORTH facing (a likely original bug on a west
        // wall — faithful, spec §13.10). Fill counts (6 + nextInt(5)) live
        // in the loot table's rolls.
        piece.placeSpawner(x + 1, y + 5, z + WIDTH / 2 - 1, ModEntities.CAVE_FISHER.get());       // orig :3448
        piece.placeSpawner(x + 1, y + 5, z + WIDTH / 2 + 1, ModEntities.CAVE_FISHER.get());       // orig :3453
        piece.placeLootChest(x + 1, y + 5, z + WIDTH / 2, Direction.NORTH, ENDER_CASTLE_LOOT);    // orig :3458

        // orig :3463-3477 — NORTH wall alcove: CaveFishers at (10/12, +5, 1),
        // chest at (11, +5, 1) meta 3 = SOUTH (faces into the room).
        piece.placeSpawner(x + WIDTH / 2 - 1, y + 5, z + 1, ModEntities.CAVE_FISHER.get());       // orig :3463
        piece.placeSpawner(x + WIDTH / 2 + 1, y + 5, z + 1, ModEntities.CAVE_FISHER.get());       // orig :3468
        piece.placeLootChest(x + WIDTH / 2, y + 5, z + 1, Direction.SOUTH, ENDER_CASTLE_LOOT);    // orig :3473

        // orig :3478-3492 — SOUTH wall alcove: CaveFishers at (10/12, +5, 21),
        // chest at (11, +5, 21) meta 4 = WEST (sideways — faithful raw meta).
        piece.placeSpawner(x + WIDTH / 2 - 1, y + 5, z + WIDTH - 1, ModEntities.CAVE_FISHER.get()); // orig :3478
        piece.placeSpawner(x + WIDTH / 2 + 1, y + 5, z + WIDTH - 1, ModEntities.CAVE_FISHER.get()); // orig :3483
        piece.placeLootChest(x + WIDTH / 2, y + 5, z + WIDTH - 1, Direction.WEST, ENDER_CASTLE_LOOT); // orig :3488
        // No 4th (east) alcove — the east wall carries the stairs/doorway.
    }

    /**
     * Direct port of {@code makeAColumn(world, cposx, cposy, cposz, height,
     * dir)} (orig GenericDungeon.java:3495-3623): one 5&times;5 obsidian
     * corner tower with an obsidian cap plate + checkered crenellation ring,
     * mid-face iron-bar window bands, two 3-block doorway openings (ground
     * y+1..+2 and parapet level y+9..+10) on the corner facing the castle,
     * and a nether-brick spiral staircase whose phase differs per tower.
     * Only ever called with {@code height == 13} (orig :3293-3296), giving
     * cap plate at +15 and crenellation at +16.
     *
     * <p>The spiral phase quirk is transcribed as-is (spec &sect;13.7): dir 0
     * applies no {@code ++step} wrap before the stair loop, dir 1 one, dirs 2
     * and 3 TWO each (orig :3559-3561, :3574-3579, :3592-3597), so the four
     * staircases start at phases 0 / 2 / 0 / 1. No RNG anywhere in this
     * method.</p>
     */
    private static void makeColumn(LegacyDungeonPiece piece, int cx, int cy, int cz,
                                   int height, int dir) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();

        final int width = 4;                                            // orig :3500
        final int halfwidth = 2;                                        // orig :3501
        int step = dir;                                                 // orig :3502

        // orig :3503-3508 — 9x9 obsidian cap plate at height+2 (= +15).
        for (int i = -2; i <= width + 2; i++) {
            for (int k = -2; k <= width + 2; k++) {
                piece.place(cx + i, cy + height + 2, cz + k, obsidian);
            }
        }

        // orig :3509-3521 — crenellation ring at height+3 (= +16): ring
        // cells obsidian with even-(i+k) checker gaps; interior cells are
        // WRITTEN as air (this loop, unlike the castle parapet, always
        // writes).
        for (int i = -2; i <= width + 2; i++) {
            for (int k = -2; k <= width + 2; k++) {
                BlockState bid = air;
                if (i == -2 || i == width + 2 || k == width + 2 || k == -2) {
                    bid = obsidian;
                }
                if (bid != air && ((i + k) & 1) == 0) {
                    bid = air;                                          // orig :3516-3518
                }
                piece.place(cx + i, cy + height + 3, cz + k, bid);
            }
        }

        // orig :3522-3535 — 5x5 shaft, y+1..height+2: perimeter obsidian,
        // interior carved to air. Window bars replace obsidian on the
        // mid-face columns (i == 2 or k == 2) at layers where j % 3 is 0 or
        // 1, except the top layer (j == height+2) — a 2-on/1-off band
        // pattern up each face (orig :3529-3531, de Morgan of the
        // decompiled negated condition).
        for (int i = 0; i <= width; i++) {
            for (int k = 0; k <= width; k++) {
                for (int j = 1; j <= height + 2; j++) {
                    BlockState bid = air;
                    if (i == 0 || i == width || k == width || k == 0) {
                        bid = obsidian;
                    }
                    if ((j % 3 == 0 || j % 3 == 1) && j != height + 2
                            && bid == obsidian && (i == halfwidth || k == halfwidth)) {
                        bid = ironBars;
                    }
                    piece.place(cx + i, cy + j, cz + k, bid);
                }
            }
        }

        // orig :3536-3598 — the two doorway openings (ground y+1..+2 and
        // parapet y+9..+10) on the castle-facing corner, per direction, plus
        // the per-direction ++step wraps that phase the spiral (spec §4/§13.7).
        if (dir == 0) {                                                 // orig :3536-3547 — corner (+4,+4)
            for (int j = 1; j <= 2; j++) {
                piece.place(cx + width, cy + j, cz + width, air);
                piece.place(cx + width - 1, cy + j, cz + width, air);
                piece.place(cx + width, cy + j, cz + width - 1, air);
            }
            for (int j = 9; j <= 10; j++) {
                piece.place(cx + width, cy + j, cz + width, air);
                piece.place(cx + width - 1, cy + j, cz + width, air);
                piece.place(cx + width, cy + j, cz + width - 1, air);
            }
        }
        if (dir == 1) {                                                 // orig :3548-3562 — corner (0,+4)
            for (int j = 1; j <= 2; j++) {
                piece.place(cx, cy + j, cz + width, air);
                piece.place(cx + 1, cy + j, cz + width, air);
                piece.place(cx, cy + j, cz + width - 1, air);
            }
            for (int j = 9; j <= 10; j++) {
                piece.place(cx, cy + j, cz + width, air);
                piece.place(cx + 1, cy + j, cz + width, air);
                piece.place(cx, cy + j, cz + width - 1, air);
            }
            if (++step > 3) {                                           // orig :3559-3561
                step = 0;
            }
        }
        if (dir == 2) {                                                 // orig :3563-3580 — corner (+4,0)
            for (int j = 1; j <= 2; j++) {
                piece.place(cx + width, cy + j, cz, air);
                piece.place(cx + width - 1, cy + j, cz, air);
                piece.place(cx + width, cy + j, cz + 1, air);
            }
            for (int j = 9; j <= 10; j++) {
                piece.place(cx + width, cy + j, cz, air);
                piece.place(cx + width - 1, cy + j, cz, air);
                piece.place(cx + width, cy + j, cz + 1, air);
            }
            if (++step > 3) {                                           // orig :3574-3576
                step = 0;
            }
            if (++step > 3) {                                           // orig :3577-3579 (double wrap — real, not a decompiler artefact)
                step = 0;
            }
        }
        if (dir == 3) {                                                 // orig :3581-3598 — corner (0,0)
            for (int j = 1; j <= 2; j++) {
                piece.place(cx, cy + j, cz, air);
                piece.place(cx + 1, cy + j, cz, air);
                piece.place(cx, cy + j, cz + 1, air);
            }
            for (int j = 9; j <= 10; j++) {
                piece.place(cx, cy + j, cz, air);
                piece.place(cx + 1, cy + j, cz, air);
                piece.place(cx, cy + j, cz + 1, air);
            }
            if (++step > 3) {                                           // orig :3592-3594
                step = 0;
            }
            if (++step > 3) {                                           // orig :3595-3597 (double wrap)
                step = 0;
            }
        }

        // orig :3599-3622 — nether-brick spiral staircase: one block per
        // layer y+1..height+2, cycling the four interior cells (1,1) ->
        // (1,3) -> (3,3) -> (3,1) as step wraps 0..3 upward.
        BlockState netherBricks = Blocks.NETHER_BRICKS.defaultBlockState();
        for (int j = 1; j <= height + 2; j++) {
            int i = 1;
            int k = 1;
            if (step == 1) {
                i = 1;
                k = 3;
            }
            if (step == 2) {
                i = 3;
                k = 3;
            }
            if (step == 3) {
                i = 3;
                k = 1;
            }
            if (++step > 3) {                                           // orig :3618-3620
                step = 0;
            }
            piece.place(cx + i, cy + j, cz + k, netherBricks);
        }
    }
}
