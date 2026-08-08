package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Gold Fish Bowl (vanilla-overworld Ocean surface), ported
 * line-by-line from 1.7.10 {@code GenericDungeon.makeGoldFishBowl}
 * (orig GenericDungeon.java:2408-2488) &mdash; one of the WGEN-042
 * structure pool. A glass fishbowl sitting on the ocean surface: 5&times;5
 * glass base plate, sand bed, a two-deep water pool with glowstone in the
 * four lower corners, three layers of air headspace, a 5&times;5 glass
 * lid, and a single "Gold Fish" spawner floating centered in the
 * headspace. No chest, no loot &mdash; the spawner IS the entire reward
 * (spec S2; do not invent loot).
 *
 * <p>Mapping decisions ({@code phase_d_reports/d6_extraction/
 * gold_fish_bowl_spec.md}):</p>
 * <ul>
 * <li>The builder is 100% deterministic &mdash; zero random draws in
 *     GD:2408-2488 (spec &sect;10). The {@code random} parameter is
 *     accepted for the shared dispatch signature and never consumed, so
 *     every per-chunk replay pass is trivially identical.</li>
 * <li>Worldgen anchor: {@code PlacementMode.OCEAN_SURFACE} reproduces the
 *     original {@code addGoldFishBowl} scan (orig OreSpawnWorld.java:
 *     1176-1194 &mdash; air directly above still water, Y 100&rarr;41,
 *     anchor at the water-surface block {@code posY - 1}), which is
 *     line-for-line identical to Monster Island's (spec &sect;6/S9).
 *     Origin Y is the water block, so the {@code j=+1} base plate lands
 *     on the first air block above the sea.</li>
 * <li>Two geometry oddities are faithful, not bugs, and are transcribed
 *     as-is (spec S3/S4): the 7&times;7 glass walls span only
 *     {@code j = +2..+7}, so the base plate's perimeter ring at
 *     {@code j=+1} is never written; and the 5&times;5 lid at
 *     {@code j=+8} is inset one block from the walls, leaving the
 *     wall-top ring at {@code j=+8} open. Do not "complete" either
 *     ring.</li>
 * <li>Write order preserved (spec S5): the glowstone corners overwrite
 *     four interior water cells of the {@code j=+3} layer, and the
 *     spawner overwrites a headspace air cell.</li>
 * </ul>
 */
final class GoldFishBowlGenerator {

    private GoldFishBowlGenerator() {}

    /**
     * Direct port of {@code makeGoldFishBowl(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:2408-2488). {@code origin} is the
     * water-surface block found by the ocean scan (worldgen, orig
     * OreSpawnWorld.java:1188 passes {@code posY - 1}) or the Dungeon
     * Spawner Block position (type 17, orig DungeonSpawnerBlock.java:
     * 104-106 &mdash; no offset, no ground gate; a terrain-embedded or
     * floating bowl is faithful behavior).
     *
     * @param random unused &mdash; the original draws zero randomness
     *               (spec &sect;10)
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState sand = Blocks.SAND.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // orig :2412-2418 — 5×5 glass base plate at j=+1.
        for (int i = 0; i < 5; i++) {
            for (int k = 0; k < 5; k++) {
                piece.place(ox + i, oy + 1, oz + k, glass);
            }
        }
        // orig :2419-2428 — 7×7 at j=+2: perimeter glass ring, sand interior.
        for (int i = -1; i < 6; i++) {
            for (int k = -1; k < 6; k++) {
                boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                piece.place(ox + i, oy + 2, oz + k, rim ? glass : sand);
            }
        }
        // orig :2429-2438 — 7×7 at j=+3: perimeter glass ring, water interior.
        for (int i = -1; i < 6; i++) {
            for (int k = -1; k < 6; k++) {
                boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                piece.place(ox + i, oy + 3, oz + k, rim ? glass : water);
            }
        }
        // orig :2439-2451 — glowstone in the four lower pool corners,
        // overwriting the j=+3 interior water just placed (spec S5).
        piece.place(ox, oy + 3, oz, glowstone);
        piece.place(ox + 4, oy + 3, oz + 4, glowstone);
        piece.place(ox, oy + 3, oz + 4, glowstone);
        piece.place(ox + 4, oy + 3, oz, glowstone);
        // orig :2452-2461 — 7×7 at j=+4: perimeter glass ring, water interior.
        for (int i = -1; i < 6; i++) {
            for (int k = -1; k < 6; k++) {
                boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                piece.place(ox + i, oy + 4, oz + k, rim ? glass : water);
            }
        }
        // orig :2462-2472 — 7×7 at j=+5..+7: perimeter glass ring, air
        // headspace interior.
        for (int j = 5; j < 8; j++) {
            for (int i = -1; i < 6; i++) {
                for (int k = -1; k < 6; k++) {
                    boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                    piece.place(ox + i, oy + j, oz + k, rim ? glass : air);
                }
            }
        }
        // orig :2473-2479 — 5×5 glass lid at j=+8, inset one block from the
        // walls (spec S4 — the wall-top ring stays open; faithful).
        for (int i = 0; i < 5; i++) {
            for (int k = 0; k < 5; k++) {
                piece.place(ox + i, oy + 8, oz + k, glass);
            }
        }
        // orig :2480-2487 — "Gold Fish" spawner at (+2,+6,+2), overwriting a
        // headspace air cell (spec S5). Mob mapping: "Gold Fish" (orig
        // OreSpawnMain.java:4101-4107) → ModEntities.GOLD_FISH.
        piece.placeSpawner(ox + 2, oy + 6, oz + 2, ModEntities.GOLD_FISH.get());
    }
}
