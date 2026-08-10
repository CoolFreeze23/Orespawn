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
 * The Ender Knight Dungeon, ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeEnderKnightDungeon} (orig
 * GenericDungeon.java:1794-1901) and its private shelf helper
 * {@code makeShelves} (orig :1903-1932) &mdash; one of the WGEN-014/WGEN-042
 * structure pool. A 4-long 5&times;5 air approach cut leads to a 1-wide,
 * 3-tall doorway through an obsidian front wall, opening into a 6-tall
 * obsidian room with an END-STONE floor and an octagonal plan (7 wide at the
 * flanking slices x+5/x+11, 9 wide across the middle x+6..x+10), sealed by a
 * solid obsidian back wall. The 28 wall-adjacent interior columns each roll a
 * shelf &mdash; loot chest / bookshelf stack / cobweb stack / nothing &mdash;
 * and two Ender Knight spawners hang stacked at room centre over the doorway
 * lane. Generates in TWO dimensions: the End ({@code addEndKnights}, orig
 * OreSpawnWorld.java:1512-1525, the enum's default {@code END_SURFACE}) and
 * the Mining dimension ({@code addEnderKnight}, orig :2087-2113, via the
 * per-JSON {@code LOWEST_GRASS_36} placement override), plus Dungeon Spawner
 * Block outcome 11 (orig DungeonSpawnerBlock.java:86-88).
 *
 * <p><b>Mapping decisions</b> (full tables in
 * {@code phase_d_reports/d6_extraction/ender_knight_dungeon_spec.md}):</p>
 * <ul>
 *   <li><b>Coordinates:</b> the original MUTATES {@code cposx}/{@code cposz}
 *       as it walks east (orig :1807, :1819-1820, :1839, :1841, :1872-1873,
 *       :1892-1893); the port transcribes every slice origin-relative per the
 *       spec &sect;2.1 table, so all offsets below are against the ORIGINAL
 *       anchor and every chunk pass replays identical absolute positions.</li>
 *   <li><b>RNG:</b> the original drew all build randomness from
 *       {@code world.field_73012_v} (orig :1906, :1915, :1920, :1927); the
 *       port threads the deterministic per-piece {@link RandomSource} through
 *       the identical call sequence &mdash; one unconditional
 *       {@code nextInt(4)} per shelf site (28 sites in fixed loop order:
 *       C k=1,2,4,5 &rarr; D m=0..4 &times; k=1,2,6,7 &rarr; E k=1,2,4,5),
 *       plus a height draw gated ONLY on that first draw's result, never on
 *       chunk or world state, so all chunk passes replay the same stream
 *       (stitching contract). The geometry and the spawners draw nothing. The
 *       Dungeon Spawner Block path keeps live-RNG behaviour via
 *       {@code buildNow}'s runtime override.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/ender_knight_dungeon}
 *       transcribes the 5-entry {@code KnightContentsList} (orig :50, total
 *       weight 95) with the original 3-7 stack rolls ({@code 3 + nextInt(5)},
 *       orig :1915); the random-slot collision quirk is dropped &mdash; the
 *       same documented approximation as every chest-list conversion since
 *       Phase C. Chests keep the original's meta 0 = default state
 *       (orig :1912).</li>
 *   <li><b>Signature content kept as-is:</b> the room floor is END STONE in
 *       both dimensions (spec &sect;12 S4 &mdash; imported into the grass
 *       Mining world on purpose); the two spawners FLOAT at y+2/y+3 with air
 *       beneath them (spec S8 &mdash; the piece's UPDATE_CLIENTS writes are
 *       the original's flag-2 writes, so they hold); the entrance cut is one
 *       block shorter than the room, so players step UP once through the
 *       doorway (spec S7). Do not "fix" any of these.</li>
 *   <li><b>Zero terrain reads:</b> the original build never reads world
 *       blocks (spec &sect;10) &mdash; no in-memory state model needed; the
 *       shelf columns simply overwrite interior air the same pass just
 *       wrote.</li>
 * </ul>
 */
final class EnderKnightDungeonGenerator {

    /** Loot for the shelf chests (orig GenericDungeon.java:50 + :1911-1915). */
    private static final ResourceKey<LootTable> KNIGHT_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/ender_knight_dungeon"));

    /** Room height; floor at +0, ceiling at +5 (orig GD:1798). */
    private static final int HEIGHT = 6;

    private EnderKnightDungeonGenerator() {}

    /**
     * Direct port of {@code makeEnderKnightDungeon(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:1794-1901). {@code origin} is the anchor: on
     * the End path the AIR block sitting directly on end stone (orig
     * OreSpawnWorld.java:1519-1521), on the Mining path the GRASS block itself
     * with NO offset (orig :2097/:2108), on the Dungeon Spawner Block path the
     * spawner block's own position (orig DungeonSpawnerBlock.java:86-88). The
     * room floor replaces that layer in all three cases &mdash; the two
     * placement modes encode the per-dimension anchor difference (spec
     * &sect;12 S5); no Y offset is applied anywhere here.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState endStone = Blocks.END_STONE.defaultBlockState();

        // orig :1801-1808 — section A: the 4x5x5 entrance cut at x+0..+3,
        // z+0..+4, y+0..+4, all air (one block SHORTER than the room — the
        // step-up at the door, spec S7). `++cposx` after each slice.
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 5; k++) {
                for (int j = 0; j < 5; j++) {
                    piece.place(x + i, y + j, z + k, air);              // orig :1804
                }
            }
        }

        // orig :1809-1818 — section B: the obsidian front wall at x+4,
        // z+0..+4, full height y+0..+5, with the 1-wide 3-tall doorway
        // carved at z+2, y+1..+3.
        for (int k = 0; k < 5; k++) {
            for (int j = 0; j < HEIGHT; j++) {
                BlockState blk = obsidian;                              // orig :1812
                if (k == 2 && j >= 1 && j <= 3) {
                    blk = air;                                          // orig :1813-1815 — doorway
                }
                piece.place(x + 4, y + j, z + k, blk);                  // orig :1816
            }
        }

        // orig :1819-1820 — ++cposx / --cposz: every offset below stays
        // origin-relative (spec §2.1 slice map).

        // orig :1821-1838 — section C: 7-wide room slice at x+5, z-1..z+5
        // (k = 0..6): floor/ceiling obsidian, end-stone floor interior,
        // obsidian Z walls, interior air. Shelf sites at k = 1, 2, 4, 5
        // (zwidth-3 = 4, zwidth-2 = 5) -> rel Z 0, +1, +3, +4.
        for (int k = 0; k < 7; k++) {
            for (int j = 0; j < HEIGHT; j++) {
                BlockState blk = air;                                   // orig :1824
                if (j == 0 || j == HEIGHT - 1) {
                    blk = obsidian;                                     // orig :1825-1827 — floor/ceiling
                }
                if (j == 0 && k > 0 && k < 6) {
                    blk = endStone;                                     // orig :1828-1830 — end-stone floor
                }
                if (k == 0 || k == 6) {
                    blk = obsidian;                                     // orig :1831-1833 — Z walls
                }
                piece.place(x + 5, y + j, z - 1 + k, blk);              // orig :1834
            }
            if (k == 1 || k == 2 || k == 4 || k == 5) {                 // orig :1836
                makeShelves(piece, random, x + 5, y + 1, z - 1 + k);    // orig :1837
            }
        }

        // orig :1839-1871 — section D: --cposz, then the 9-wide main room,
        // five slices at x+6..x+10 (`++cposx` FIRST each pass, orig :1841),
        // z-2..z+6 (k = 0..8), same four rules with the wider span. Shelf
        // sites at k = 1, 2, 6, 7 (zwidth-3 = 6, zwidth-2 = 7) -> rel Z -1,
        // 0, +4, +5. The two stacked Ender Knight spawners fire at
        // m == 2 && k == 4 -> (x+8, y+2/y+3, z+2), room centre on the
        // doorway lane, floating over interior air (spec S8).
        for (int m = 0; m < 5; m++) {
            int sx = x + 6 + m;                                         // orig :1841 — ++cposx
            for (int k = 0; k < 9; k++) {
                for (int j = 0; j < HEIGHT; j++) {
                    BlockState blk = air;                               // orig :1845
                    if (j == 0 || j == HEIGHT - 1) {
                        blk = obsidian;                                 // orig :1846-1848
                    }
                    if (j == 0 && k > 0 && k < 8) {
                        blk = endStone;                                 // orig :1849-1851
                    }
                    if (k == 0 || k == 8) {
                        blk = obsidian;                                 // orig :1852-1854
                    }
                    piece.place(sx, y + j, z - 2 + k, blk);             // orig :1855
                }
                if (k == 1 || k == 2 || k == 6 || k == 7) {             // orig :1857
                    makeShelves(piece, random, sx, y + 1, z - 2 + k);   // orig :1858
                }
                if (m == 2 && k == 4) {                                 // orig :1860
                    piece.placeSpawner(sx, y + 2, z - 2 + k, ModEntities.ENDER_KNIGHT.get()); // orig :1861-1865
                    piece.placeSpawner(sx, y + 3, z - 2 + k, ModEntities.ENDER_KNIGHT.get()); // orig :1866-1869
                }
            }
        }

        // orig :1872-1891 — section E: ++cposz / ++cposx, then the second
        // 7-wide room slice at x+11, z-1..z+5 — identical rules to section C,
        // shelf sites again at k = 1, 2, 4, 5.
        for (int k = 0; k < 7; k++) {
            for (int j = 0; j < HEIGHT; j++) {
                BlockState blk = air;                                   // orig :1877
                if (j == 0 || j == HEIGHT - 1) {
                    blk = obsidian;                                     // orig :1878-1880
                }
                if (j == 0 && k > 0 && k < 6) {
                    blk = endStone;                                     // orig :1881-1883
                }
                if (k == 0 || k == 6) {
                    blk = obsidian;                                     // orig :1884-1886
                }
                piece.place(x + 11, y + j, z - 1 + k, blk);             // orig :1887
            }
            if (k == 1 || k == 2 || k == 4 || k == 5) {                 // orig :1889
                makeShelves(piece, random, x + 11, y + 1, z - 1 + k);   // orig :1890
            }
        }

        // orig :1892-1900 — section F: ++cposz / ++cposx, then the solid
        // obsidian back wall at x+12, z+0..+4, full height y+0..+5.
        for (int k = 0; k < 5; k++) {
            for (int j = 0; j < HEIGHT; j++) {
                piece.place(x + 12, y + j, z + k, obsidian);            // orig :1897-1898
            }
        }
    }

    /**
     * Direct port of {@code makeShelves(world, cposx, cposy, cposz)} (orig
     * GenericDungeon.java:1903-1932), called at the 28 wall-adjacent interior
     * columns with base Y = floor + 1. One unconditional {@code nextInt(4)}
     * per site picks: 0 = loot chest (fill {@code 3 + nextInt(5)}, orig
     * :1915, moved to the loot table's 3-7 rolls), 1 = bookshelf column of
     * height {@code 1 + nextInt(4)}, 2 = cobweb column of the same height
     * formula, 3 = nothing (the slice loop's interior air stands). The height
     * draw depends only on the site draw's result — never on chunk or world
     * state — so every chunk pass consumes the identical stream (stitching
     * contract, spec §11). Max column top is y+4, one below the ceiling.
     */
    private static void makeShelves(LegacyDungeonPiece piece, RandomSource random,
                                    int cx, int cy, int cz) {
        int i = random.nextInt(4);                                      // orig :1906 — unconditional site draw
        if (i == 0) {                                                   // orig :1908-1917 — loot chest
            piece.placeLootChest(cx, cy, cz, KNIGHT_LOOT);              // orig :1912 (meta 0 -> default state)
        }
        if (i == 1) {                                                   // orig :1918-1924 — bookshelf column
            int h = 1 + random.nextInt(4);                              // orig :1920
            for (int j = 0; j < h; j++) {
                piece.place(cx, cy + j, cz, Blocks.BOOKSHELF.defaultBlockState()); // orig :1922
            }
        }
        if (i == 2) {                                                   // orig :1925-1931 — cobweb column
            int h = 1 + random.nextInt(4);                              // orig :1927
            for (int j = 0; j < h; j++) {
                piece.place(cx, cy + j, cz, Blocks.COBWEB.defaultBlockState());    // orig :1929
            }
        }
        // i == 3 — nothing written (orig :1906 falls through all three ifs).
    }
}
