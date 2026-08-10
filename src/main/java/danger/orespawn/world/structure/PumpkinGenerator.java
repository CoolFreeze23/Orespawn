package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Giant Jack-o'-Lantern ("Pumpkin", Islands dimension), ported
 * line-by-line from 1.7.10 {@code GenericDungeon.makePumpkin} (orig
 * GenericDungeon.java:6041-6182) &mdash; one of the WGEN-042 structure pool.
 * A hollow 14&times;14&times;12 orange-terracotta box with a jack-o'-lantern
 * face carved through the &minus;z wall (two 3&times;3 eyes, a split
 * 2&times;2+2&times;2 nose, a jagged 8-wide grin), a 3-wide green-terracotta
 * stem leaning one block &minus;x per layer on the lid, and inside a
 * 2&times;2&times;5 oak-plank candle capped with netherrack carrying two
 * eternal fires and two "Ghost Pumpkin Skelly" spawners. No chest, no loot.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/pumpkin_spec.md}):</p>
 * <ul>
 * <li>The worldgen origin is the AIR block ONE ABOVE the Islands grass
 *     anchor (orig OreSpawnWorld.java:2416 passes {@code posY + 1}) &mdash;
 *     the {@code ISLANDS_GRASS_AIR} placement mode already applies the
 *     {@code .above()}, so this generator adds NO offset of its own; the
 *     DSB type-44 path likewise carries the original's {@code clickedY + 1}
 *     via {@code pos.above()} (orig DungeonSpawnerBlock.java:185-187)
 *     &mdash; spec S2, &sect;9.</li>
 * <li>1.7.10 stained-hardened-clay meta 1 = {@code orange_terracotta}
 *     (local {@code orange}, orig :6049), meta 13 = {@code green_terracotta}
 *     (local {@code dark_green} &mdash; NOT lime, orig :6048) &mdash; spec
 *     &sect;5, S8.</li>
 * <li>Loop A explicitly writes AIR through the whole 12&times;12&times;10
 *     interior (orig :6056-6057, :6070) &mdash; terrain inside the box is
 *     carved out, not skipped; the writes are kept, not optimized away
 *     &mdash; spec S5.</li>
 * <li>The face is carved ONLY on the z = 0 wall &mdash; 48 air holes; the
 *     other three walls and the lid stay solid, so rain/mobs/players can
 *     pass through the face; faithful &mdash; spec S6.</li>
 * <li>Fire goes in as {@code Blocks.FIRE.defaultBlockState()} via the
 *     flag-2 {@code piece.place} (no update, it will not re-shape or vanish
 *     on placement). The flames sit on netherrack (eternal in both
 *     versions) two blocks above the flammable planks &mdash; outside
 *     modern fire's ignition reach, so the candle never burns down, the
 *     same equilibrium as 1.7.10 &mdash; spec S7.</li>
 * <li>Both spawners emit "Ghost Pumpkin Skelly" &rarr;
 *     {@code ModEntities.GHOST_SKELLY} (orig OreSpawnMain.java:4055/4059
 *     registration); the ported GhostSkelly's spawner-proximity gate
 *     already whitelists exactly this spawner &mdash; spec &sect;4, S9.</li>
 * <li>The original draws ZERO randomness and performs NO world reads beyond
 *     the two spawner tile-entity self-fetches (orig :6172, :6178, absorbed
 *     by {@code placeSpawner}) &mdash; this generator is a pure constant
 *     block stamp and satisfies the per-chunk-replay stitching contract
 *     vacuously &mdash; spec &sect;10, &sect;11. No loot table exists and
 *     none is invented &mdash; spec &sect;3, S3.</li>
 * </ul>
 */
final class PumpkinGenerator {

    private PumpkinGenerator() {}

    /**
     * Direct port of {@code makePumpkin(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:6041-6182). {@code origin} is the air block
     * above the Islands grass anchor found by the {@code addPumpkin}
     * Y 20&rarr;5 scan (worldgen, orig OreSpawnWorld.java:2407-2421 &mdash;
     * anchor at grass + 1) or the Dungeon Spawner Block position + 1
     * (outcome 44, orig DungeonSpawnerBlock.java:185-187 &mdash; a lantern
     * embedded in a hillside there is faithful behavior; the interior air
     * writes carve the terrain out of the inside regardless).
     *
     * <p>Write order preserved from the original: hollow shell &rarr; face
     * carvings (right half, then left half) &rarr; stem &rarr; candle
     * pillar &rarr; netherrack cap &rarr; fire pair &rarr; 2 spawners.
     * Later writes overwrite earlier ones (the face holes punch through
     * Loop-A shell cells).</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        // Method-local constants: width = 14 (X), depth = 12 (Z),
        // height = 14 (Y), dark_green = 13, orange = 1 (orig :6045-6049).
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState orange = Blocks.ORANGE_TERRACOTTA.defaultBlockState();  // clay meta 1 (orig :6049)
        BlockState green = Blocks.GREEN_TERRACOTTA.defaultBlockState();    // clay meta 13 (orig :6048)
        BlockState oakPlanks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        BlockState fire = Blocks.FIRE.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop A — hollow orange shell, interior cleared (orig :6053-6073).
        // Per cell: default air; then THREE independent ifs, any of which
        // switches to orange terracotta. Net: a closed 14×14×12 orange box —
        // floor at j=0, lid at j=13, four walls — with every interior cell
        // (1..12, 1..12, 1..10) explicitly written air.
        for (int i = 0; i < 14; i++) {                                     // width, orig :6053
            for (int j = 0; j < 14; j++) {                                 // height, orig :6054
                for (int k = 0; k < 12; k++) {                             // depth, orig :6055
                    BlockState blk = air;                                  // orig :6056-6057
                    if (j == 0 || j == 13) {
                        blk = orange;                                      // orig :6058-6061 (floor/lid)
                    }
                    if (i == 0 || i == 13) {
                        blk = orange;                                      // orig :6062-6065 (x walls)
                    }
                    if (k == 0 || k == 11) {
                        blk = orange;                                      // orig :6066-6069 (z walls)
                    }
                    piece.place(cx + i, cy + j, cz + k, blk);              // orig :6070
                }
            }
        }

        // Face carvings — 48 air cells punched through the z = 0 (front)
        // wall (orig :6074-6143), all at k = 0. Two mirrored halves.

        // Right half, base i = width/2 - 1 = 6 (orig :6074-6075).
        {
            int i = 6;                                                     // orig :6074 (k = 0, orig :6075)
            // Right eye, 3×3 at x 9-11, y 9-11 (orig :6076-6087).
            piece.place(cx + i + 3, cy + 11, cz, air);                     // orig :6077 (j = 11, orig :6076)
            piece.place(cx + i + 4, cy + 11, cz, air);                     // orig :6078
            piece.place(cx + i + 5, cy + 11, cz, air);                     // orig :6079
            piece.place(cx + i + 3, cy + 10, cz, air);                     // orig :6081 (j = 10, orig :6080)
            piece.place(cx + i + 4, cy + 10, cz, air);                     // orig :6082
            piece.place(cx + i + 5, cy + 10, cz, air);                     // orig :6083
            piece.place(cx + i + 3, cy + 9, cz, air);                      // orig :6085 (j = 9, orig :6084)
            piece.place(cx + i + 4, cy + 9, cz, air);                      // orig :6086
            piece.place(cx + i + 5, cy + 9, cz, air);                      // orig :6087
            // Nose, right half, 2×2 at x 8-9, y 7-8 (orig :6088-6093).
            piece.place(cx + i + 2, cy + 8, cz, air);                      // orig :6089 (j = 8, orig :6088)
            piece.place(cx + i + 3, cy + 8, cz, air);                      // orig :6090
            piece.place(cx + i + 2, cy + 7, cz, air);                      // orig :6092 (j = 7, orig :6091)
            piece.place(cx + i + 3, cy + 7, cz, air);                      // orig :6093
            // Mouth top notches at y 4, x 7 and 10 (orig :6094-6096).
            piece.place(cx + i + 1, cy + 4, cz, air);                      // orig :6095 (j = 4, orig :6094)
            piece.place(cx + i + 4, cy + 4, cz, air);                      // orig :6096
            // Mouth band right half, x 7-10 at y 3 and y 2 (orig :6097-6106).
            piece.place(cx + i + 1, cy + 3, cz, air);                      // orig :6098 (j = 3, orig :6097)
            piece.place(cx + i + 2, cy + 3, cz, air);                      // orig :6099
            piece.place(cx + i + 3, cy + 3, cz, air);                      // orig :6100
            piece.place(cx + i + 4, cy + 3, cz, air);                      // orig :6101
            piece.place(cx + i + 1, cy + 2, cz, air);                      // orig :6103 (j = 2, orig :6102)
            piece.place(cx + i + 2, cy + 2, cz, air);                      // orig :6104
            piece.place(cx + i + 3, cy + 2, cz, air);                      // orig :6105
            piece.place(cx + i + 4, cy + 2, cz, air);                      // orig :6106
            // Right bottom fang at x 8, y 1 (orig :6107-6108).
            piece.place(cx + i + 2, cy + 1, cz, air);                      // orig :6108 (j = 1, orig :6107)
        }

        // Left half, mirrored, base i = width/2 = 7 (orig :6109-6110).
        {
            int i = 7;                                                     // orig :6109 (k = 0, orig :6110)
            // Left eye, 3×3 at x 2-4, y 9-11 (orig :6111-6122).
            piece.place(cx + i - 3, cy + 11, cz, air);                     // orig :6112 (j = 11, orig :6111)
            piece.place(cx + i - 4, cy + 11, cz, air);                     // orig :6113
            piece.place(cx + i - 5, cy + 11, cz, air);                     // orig :6114
            piece.place(cx + i - 3, cy + 10, cz, air);                     // orig :6116 (j = 10, orig :6115)
            piece.place(cx + i - 4, cy + 10, cz, air);                     // orig :6117
            piece.place(cx + i - 5, cy + 10, cz, air);                     // orig :6118
            piece.place(cx + i - 3, cy + 9, cz, air);                      // orig :6120 (j = 9, orig :6119)
            piece.place(cx + i - 4, cy + 9, cz, air);                      // orig :6121
            piece.place(cx + i - 5, cy + 9, cz, air);                      // orig :6122
            // Nose, left half, 2×2 at x 4-5, y 7-8 (orig :6123-6128).
            piece.place(cx + i - 2, cy + 8, cz, air);                      // orig :6124 (j = 8, orig :6123)
            piece.place(cx + i - 3, cy + 8, cz, air);                      // orig :6125
            piece.place(cx + i - 2, cy + 7, cz, air);                      // orig :6127 (j = 7, orig :6126)
            piece.place(cx + i - 3, cy + 7, cz, air);                      // orig :6128
            // Mouth top notches at y 4, x 6 and 3 (orig :6129-6131).
            piece.place(cx + i - 1, cy + 4, cz, air);                      // orig :6130 (j = 4, orig :6129)
            piece.place(cx + i - 4, cy + 4, cz, air);                      // orig :6131
            // Mouth band left half, x 3-6 at y 3 and y 2 (orig :6132-6141).
            piece.place(cx + i - 1, cy + 3, cz, air);                      // orig :6133 (j = 3, orig :6132)
            piece.place(cx + i - 2, cy + 3, cz, air);                      // orig :6134
            piece.place(cx + i - 3, cy + 3, cz, air);                      // orig :6135
            piece.place(cx + i - 4, cy + 3, cz, air);                      // orig :6136
            piece.place(cx + i - 1, cy + 2, cz, air);                      // orig :6138 (j = 2, orig :6137)
            piece.place(cx + i - 2, cy + 2, cz, air);                      // orig :6139
            piece.place(cx + i - 3, cy + 2, cz, air);                      // orig :6140
            piece.place(cx + i - 4, cy + 2, cz, air);                      // orig :6141
            // Left bottom fang at x 5, y 1 (orig :6142-6143).
            piece.place(cx + i - 2, cy + 1, cz, air);                      // orig :6143 (j = 1, orig :6142)
        }

        // Stem — diagonal green slab on the lid, one block thick at
        // z = depth/2 - 1 = 5 (orig :6144-6149): 3 wide, 4 tall, leaning
        // one block -x per layer. x = width/2 - i - j, y = height + j.
        for (int j = 0; j < 4; j++) {                                      // orig :6145
            for (int i = 0; i < 3; i++) {                                  // orig :6146
                piece.place(cx + 7 - i - j, cy + 14 + j, cz + 5, green);   // orig :6147
            }
        }

        // Candle pillar — oak planks 2×2×5 standing on the Loop-A floor,
        // centered in the interior: x 6-7, y 1-5, z 5-6 (orig :6150-6156).
        // x = width/2 + i - 1, y = j + 1, z = depth/2 + k - 1.
        for (int j = 0; j < 5; j++) {                                      // orig :6150
            for (int i = 0; i < 2; i++) {                                  // orig :6151
                for (int k = 0; k < 2; k++) {                              // orig :6152
                    piece.place(cx + 6 + i, cy + 1 + j, cz + 5 + k, oakPlanks); // orig :6153
                }
            }
        }

        // Netherrack cap — 2×2 at y 6 over the planks (orig :6157-6162).
        for (int i = 0; i < 2; i++) {                                      // orig :6158 (j = 5, orig :6157)
            for (int k = 0; k < 2; k++) {                                  // orig :6159
                piece.place(cx + 6 + i, cy + 6, cz + 5 + k, netherrack);   // orig :6160 (y = j + 1 = 6)
            }
        }

        // Fire — the eternal candle flame: the front pair of the netherrack
        // top at (6, 7, 5) and (7, 7, 5) (orig :6163-6167).
        for (int i = 0; i < 2; i++) {                                      // orig :6165 (j = 6, k = 0, orig :6163-6164)
            piece.place(cx + 6 + i, cy + 7, cz + 5, fire);                 // orig :6166 (z = depth/2 + 0 - 1 = 5)
        }

        // Spawners — 2 × "Ghost Pumpkin Skelly" on the back pair of the
        // netherrack top, shoulder-to-shoulder with the fire
        // (j = 6, k = 1 → y 7, z 6, orig :6168-6181).
        piece.placeSpawner(cx + 6, cy + 7, cz + 6, ModEntities.GHOST_SKELLY.get()); // orig :6171-6175 (i = 0, orig :6170)
        piece.placeSpawner(cx + 7, cy + 7, cz + 6, ModEntities.GHOST_SKELLY.get()); // orig :6177-6181 (i = 1, orig :6176)
    }
}
