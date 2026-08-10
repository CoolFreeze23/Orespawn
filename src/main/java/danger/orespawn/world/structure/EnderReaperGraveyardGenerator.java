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
 * The Ender Reaper Graveyard (The End), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeEnderReaperGraveyard}
 * (orig GenericDungeon.java:2490-2563) plus its only helper
 * {@code makeAGrave} (orig :2565-2576) &mdash; one of the WGEN-042
 * structure pool. An 11&times;13 end-stone pad sits one block above the
 * End surface (the worldgen anchor is the AIR block directly above end
 * stone, orig OreSpawnWorld.java:1535-1536, with no Y offset), standing
 * on a conditional up-to-4-deep end-stone foundation skirt, fenced by a
 * 4-high ROOFLESS iron-bar wall, with four "Ender Reaper" spawners one
 * block inside the fence corners and eight obsidian-marked graves, each
 * hiding a loot chest sunk flush into the floor.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/graveyard_spec.md}):</p>
 * <ul>
 * <li><b>Conditional foundation skirt (the one mid-build world READ,
 *     orig :2500).</b> The first loop probes each cell at
 *     {@code (0..10, -1..-4, 0..12)} and writes end stone only where the
 *     PRE-BUILD terrain has air &mdash; existing End terrain is never
 *     replaced. This is a pre-build terrain probe, not a self-read (loop
 *     A runs first and nothing else ever writes below y+0), so an
 *     in-memory model is impossible by construction; the sanctioned port
 *     is the royal-altar dirt-skirt read-at-write-cell pattern
 *     (LegacyDungeonPiece.generateRoyalAltar, "Phase 2"): gate with
 *     {@code inChunk} FIRST, then read, then {@code place}. Stitching is
 *     safe because the read cell == the write cell &mdash; a read outside
 *     the current write window never happens, and the write it would have
 *     gated is skipped by the same test, so every chunk pass stays
 *     consistent regardless of neighbor-chunk state. The condition is
 *     {@code isAir()} ONLY (the original tests {@code != air} alone, orig
 *     :2500 &mdash; none of the royal skirt's grass/water extras). The
 *     modern {@code isAir()} also matches cave/void air where the 1.7.10
 *     identity test knew a single air block; documented approximation on
 *     the Dungeon Spawner Block path (spec &sect;10.1).</li>
 * <li><b>No roof.</b> The fence loop stops at y+4 and nothing is ever
 *     written at y&ge;5 (orig :2512-2522) &mdash; the cage is open-topped
 *     by design; do not add a lid (spec S3).</li>
 * <li><b>Grave anatomy is asymmetric</b> (orig :2569-2571): headstone
 *     obsidian at y+1 on the &minus;z side, foot obsidian and the chest
 *     sunk FLUSH into the floor at y+0, both replacing floor end stone.
 *     Write order preserved: floor first, graves last (spec S4).</li>
 * <li><b>Zero RNG draws.</b> The only draws in the original builder are
 *     the 8 chest fill counts {@code 3 + nextInt(3)} (orig :2574), which
 *     moved into the loot JSON's rolls (uniform 3-5), so this generator
 *     satisfies the per-chunk-replay stitching contract vacuously; the
 *     skirt conditionals branch on terrain, not RNG, and are pass-safe
 *     per the bullet above (spec &sect;11). The original's tile-entity
 *     fetches at just-written positions (orig :2527/:2535/:2543/:2551,
 *     :2572) are absorbed by {@code placeSpawner}/{@code placeLootChest}.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/graveyard} transcribes the
 * original 4-entry {@code GraveContentsList} (orig GenericDungeon.java:43,
 * total weight 140 &mdash; eye of ender / poppy / dandelion / ender pearl,
 * all 6-16 stacks) with the original 3-5 stack rolls; the random-slot
 * collision quirk is dropped, the same documented approximation as every
 * other chest-list conversion since Phase C.</p>
 */
final class EnderReaperGraveyardGenerator {

    /** Loot shared by all 8 grave chests (orig GenericDungeon.java:43 + :2574). */
    private static final ResourceKey<LootTable> GRAVE_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/graveyard"));

    private EnderReaperGraveyardGenerator() {}

    /**
     * Direct port of {@code makeEnderReaperGraveyard(world, cposx, cposy,
     * cposz)} (orig GenericDungeon.java:2490-2563). {@code origin} is the
     * air block directly above end stone found by the
     * {@code addEndReapers} scan (worldgen, orig OreSpawnWorld.java:
     * 1527-1540 &mdash; NO Y offset, so the floor pad sits one block above
     * the terrain surface and the skirt closes the gap, spec S6) or the
     * Dungeon Spawner Block position (outcome 18, orig
     * DungeonSpawnerBlock.java:107-109 &mdash; a single-builder index; the
     * DSB path skips the End scan entirely, so a graveyard built in any
     * dimension at the clicked position is faithful behavior).
     *
     * <p>{@code random} is unused: the ported builder consumes zero draws
     * (see class Javadoc); the parameter stays for the uniform generator
     * dispatch signature.</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        final int width = 11;                                        // orig :2494
        final int length = 13;                                       // orig :2495
        BlockState endStone = Blocks.END_STONE.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop A — conditional foundation skirt, up to 4 deep
        // (orig :2497-2504): end stone into PRE-BUILD air only, terrain
        // preserved. inChunk gate FIRST, then the read, then the gated
        // write — read cell == write cell keeps every chunk pass
        // consistent (class Javadoc bullet 1). Probe via the piece's
        // sanctioned terrainStateIfInChunk helper (D6b batch-2 verification
        // replaced this class's interim reflection access).
        for (int j = 1; j < 5; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    int x = cx + i;
                    int y = cy - j;
                    int z = cz + k;
                    var terrain = piece.terrainStateIfInChunk(x, y, z);
                    if (terrain == null || !terrain.isAir()) continue;  // orig :2500
                    piece.place(x, y, z, endStone);                     // orig :2501
                }
            }
        }

        // Loop B — unconditional 11x13 end-stone floor pad at y+0
        // (orig :2505-2511).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < length; k++) {
                piece.place(cx + i, cy, cz + k, endStone);           // orig :2509
            }
        }

        // Loop C — 4-high iron-bar fence cage, interior air, NO roof
        // (orig :2512-2522): perimeter cells (i == 0 | k == 0 |
        // i == width-1 | k == length-1) get iron bars, the rest air.
        for (int j = 1; j < 5; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    boolean perimeter = i == 0 || k == 0
                            || i == width - 1 || k == length - 1;    // orig :2516
                    piece.place(cx + i, cy + j, cz + k,
                            perimeter ? ironBars : air);             // orig :2515-2519
                }
            }
        }

        // Four "Ender Reaper" spawners at y+1, one block inside each
        // fence corner (orig :2523-2554; mob string orig :2529/:2537/
        // :2545/:2553 -> ModEntities.ENDER_REAPER, registration orig
        // OreSpawnMain.java:4133). All four cells were loop-C interior
        // air — no fence overwrite.
        piece.placeSpawner(cx + 1, cy + 1, cz + 1, ModEntities.ENDER_REAPER.get());                  // orig :2523-2530
        piece.placeSpawner(cx + width - 2, cy + 1, cz + length - 2, ModEntities.ENDER_REAPER.get()); // orig :2531-2538
        piece.placeSpawner(cx + 1, cy + 1, cz + length - 2, ModEntities.ENDER_REAPER.get());         // orig :2539-2546
        piece.placeSpawner(cx + width - 2, cy + 1, cz + 1, ModEntities.ENDER_REAPER.get());          // orig :2547-2554

        // Eight graves, original call order (orig :2555-2562): two rows
        // of three (z = 4 and z = 8, x in {3, 5, 7}) plus two singles at
        // the mid-length edges. All cells strictly interior; no grave
        // collides with a spawner or another grave.
        makeAGrave(piece, cx, cy, cz, 1, 6);                         // orig :2555
        makeAGrave(piece, cx, cy, cz, 3, 4);                         // orig :2556
        makeAGrave(piece, cx, cy, cz, 5, 4);                         // orig :2557
        makeAGrave(piece, cx, cy, cz, 7, 4);                         // orig :2558
        makeAGrave(piece, cx, cy, cz, 3, 8);                         // orig :2559
        makeAGrave(piece, cx, cy, cz, 5, 8);                         // orig :2560
        makeAGrave(piece, cx, cy, cz, 7, 8);                         // orig :2561
        makeAGrave(piece, cx, cy, cz, 9, 6);                         // orig :2562
    }

    /**
     * Direct port of {@code makeAGrave(world, cposx, cposy, cposz, xoff,
     * zoff)} (orig GenericDungeon.java:2565-2576), called only by
     * {@link #generate}. Headstone obsidian one block UP on the &minus;z
     * side; foot obsidian and the chest sunk flush into the floor at y+0,
     * both replacing loop-B end stone — the chest lid sits level with the
     * floor under open air, openable (spec S4). Every grave faces the
     * same way. The fill count {@code 3 + nextInt(3)} (orig :2574) lives
     * in the loot table's rolls (uniform 3-5).
     */
    private static void makeAGrave(LegacyDungeonPiece piece, int cx, int cy, int cz,
                                   int xoff, int zoff) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        piece.place(cx + xoff, cy + 1, cz + zoff - 1, obsidian);     // orig :2569 (headstone)
        piece.place(cx + xoff, cy, cz + zoff + 1, obsidian);         // orig :2570 (foot marker)
        piece.placeLootChest(cx + xoff, cy, cz + zoff, GRAVE_LOOT);  // orig :2571-2575
    }
}
