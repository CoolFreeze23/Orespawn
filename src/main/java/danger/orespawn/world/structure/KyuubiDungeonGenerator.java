package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Kyuubi Dungeon (Mining dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeKyuubiDungeon} (orig GenericDungeon.java:1095-1209)
 * plus its private helpers {@code addlavasquare} (orig :1211-1217),
 * {@code addkyuubi} (orig :1219-1258) and {@code addblaze} (orig :1260-1361)
 * &mdash; one of the WGEN-042 structure pool
 * (spec: {@code phase_d_reports/d6_extraction/kyuubi_dungeon_spec.md}).
 *
 * <p>Layout: a sealed sandstone hut (entry only through a 1x1 hole in the
 * roof centre &mdash; the drop-in gimmick, NOT a missing door), a 5x5 shaft
 * 20 deep onto a 2-layer water brake, and a 12-long corridor whose side
 * walls at head height are standing lava source blocks (they hold only
 * because flag-2 writes skip neighbour updates; the first player-caused
 * update floods the corridor &mdash; faithful trap, orig :1183-1185). The
 * corridor opens into a 20x18x30 netherrack boss room containing five
 * lava-square floor hazards, six everburning fires on netherrack, the
 * Kyuubi altar (two lava-moated nether-brick tiers, a 3-spawner "Kyuubi"
 * stack and a floating loot chest on top, orig :1219-1258) and a 4-tier
 * obsidian ziggurat ringed by 8 "Blaze" spawners with 4 outward-facing
 * loot chests on its first tier (orig :1260-1361).</p>
 *
 * <p>Mapping decisions:</p>
 * <ul>
 * <li>Geometry is fully deterministic &mdash; the original's ONLY RNG use
 *     was the five chest fill counts (orig :1256, :1341, :1347, :1353,
 *     :1359), which move into the loot-table rolls, leaving this generator
 *     draw-free; the chunk-replay RNG stream is trivially stable and the
 *     {@code random} parameter is unused (kept for the dispatch
 *     signature).</li>
 * <li>The altar chest ({@code orespawn:chests/kyuubi_dungeon}, rolls 7-13
 *     = orig {@code 7 + nextInt(7)}) consumes {@code kyuubiContentsList}
 *     (orig :53, total weight 110). The four ziggurat chests each had a
 *     DIFFERENT fill formula on the shared {@code blazeContentsList}
 *     (orig :54, total weight 130), so they get four tables that differ
 *     only in rolls: {@code kyuubi_dungeon_blaze_west/north/south/east}
 *     (4-8 / 3-7 / 5-9 / 6-10). The orig meta-61 spawn-egg entry is 1.7.10
 *     damage-encoded entity ID 61 = Blaze &rarr;
 *     {@code minecraft:blaze_spawn_egg}.</li>
 * <li>Chest facing metas map 2&rarr;NORTH, 3&rarr;SOUTH, 4&rarr;WEST,
 *     5&rarr;EAST via the facing-aware
 *     {@link LegacyDungeonPiece#placeLootChest(int, int, int, Direction,
 *     ResourceKey)} overload.</li>
 * <li>The water brake and basin (below Y0-19) are deliberately unwalled
 *     &mdash; bordered by raw terrain, a crossing cave drains the pool.
 *     Faithful quirk; do not wall it (spec section 13.4).</li>
 * <li>Statement order preserved: room shell before corridor and shaft
 *     before corridor, because the corridor's interior-air writes carve
 *     the doorways out of walls written earlier (spec section 13.10).</li>
 * </ul>
 */
final class KyuubiDungeonGenerator {

    /** Altar-top chest loot, orig {@code kyuubiContentsList} (GenericDungeon.java:53 + :1256). */
    private static final ResourceKey<LootTable> KYUUBI_LOOT = chestLoot("kyuubi_dungeon");
    /** Ziggurat west chest, orig {@code blazeContentsList} @ 4-8 rolls (GenericDungeon.java:54 + :1341). */
    private static final ResourceKey<LootTable> BLAZE_LOOT_WEST = chestLoot("kyuubi_dungeon_blaze_west");
    /** Ziggurat north chest, orig {@code blazeContentsList} @ 3-7 rolls (GenericDungeon.java:54 + :1347). */
    private static final ResourceKey<LootTable> BLAZE_LOOT_NORTH = chestLoot("kyuubi_dungeon_blaze_north");
    /** Ziggurat south chest, orig {@code blazeContentsList} @ 5-9 rolls (GenericDungeon.java:54 + :1353). */
    private static final ResourceKey<LootTable> BLAZE_LOOT_SOUTH = chestLoot("kyuubi_dungeon_blaze_south");
    /** Ziggurat east chest, orig {@code blazeContentsList} @ 6-10 rolls (GenericDungeon.java:54 + :1359). */
    private static final ResourceKey<LootTable> BLAZE_LOOT_EAST = chestLoot("kyuubi_dungeon_blaze_east");

    private static ResourceKey<LootTable> chestLoot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("orespawn", "chests/" + name));
    }

    // Local constants of makeKyuubiDungeon (orig GenericDungeon.java:1099-1105).
    // depth is FIXED at 20 — no random component, unlike BasiliskMaze.
    private static final int WIDTH = 5;    // hut / shaft / corridor cross-section
    private static final int HEIGHT = 5;   // hut wall height
    private static final int DEPTH = 20;   // shaft depth
    private static final int LENGTH = 12;  // corridor length
    private static final int RWIDTH = 30;  // boss room Z extent
    private static final int RHEIGHT = 18; // boss room Y extent
    private static final int RLENGTH = 20; // boss room X extent

    private KyuubiDungeonGenerator() {}

    /**
     * Direct port of {@code makeKyuubiDungeon(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:1095-1209). {@code origin} is the
     * lowest-of-36-columns scan result sunk 2 blocks (worldgen,
     * orig OreSpawnWorld.java:2599-2623) or the Dungeon Spawner Block
     * position (type 7, orig DungeonSpawnerBlock.java:74-76, no sink).
     *
     * @param random unused &mdash; the original drew randomness only for the
     *               five chest fill counts, now expressed as loot-table rolls
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();

        // orig :1109-1115 — pre-clear a 5x5 cavity from Y0 down 5 layers
        // (loop j = 0..4 writes at cposy - j).
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < WIDTH; k++) {
                    piece.place(cposx + i, cposy - j, cposz + k, air);
                }
            }
        }

        // orig :1116-1121 — hut roof slab at Y0+5 (j = height).
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                piece.place(cposx + i, cposy + HEIGHT, cposz + k, sandstone);
            }
        }
        // orig :1122 — the 1x1 roof entry hole at the centre (width/2 = 2):
        // the ONLY way into the sealed hut.
        piece.place(cposx + WIDTH / 2, cposy + HEIGHT, cposz + WIDTH / 2, air);

        // orig :1123-1134 — hut walls, all four sides solid sandstone (no
        // door!), 3x3 air interior.
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                for (int j = 0; j < HEIGHT; j++) {
                    boolean perimeter = k == 0 || k == WIDTH - 1 || i == 0 || i == WIDTH - 1;
                    piece.place(cposx + i, cposy + j, cposz + k, perimeter ? sandstone : air);
                }
            }
        }

        // orig :1135-1146 — drop shaft: stone perimeter, 3x3 air bore, from
        // Y0-1 down to Y0-19 (loop j = -1; j > -depth).
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                for (int j = -1; j > -DEPTH; j--) {
                    boolean perimeter = k == 0 || k == WIDTH - 1 || i == 0 || i == WIDTH - 1;
                    piece.place(cposx + i, cposy + j, cposz + k, perimeter ? stone : air);
                }
            }
        }

        // orig :1147-1153 — 2-layer water brake at Y0-20..Y0-21, interior 3x3
        // only (loop j = -depth; j > -(depth+2)). NO perimeter walls of its
        // own below the shaft — bordered by raw terrain; a crossing cave
        // drains the pool. Faithful (spec section 13.4).
        for (int i = 1; i < WIDTH - 1; i++) {
            for (int k = 1; k < WIDTH - 1; k++) {
                for (int j = -DEPTH; j > -(DEPTH + 2); j--) {
                    piece.place(cposx + i, cposy + j, cposz + k, Blocks.WATER.defaultBlockState());
                }
            }
        }

        // orig :1154-1159 — 3x3 stone basin floor at Y0-22.
        for (int i = 1; i < WIDTH - 1; i++) {
            for (int k = 1; k < WIDTH - 1; k++) {
                piece.place(cposx + i, cposy - (DEPTH + 2), cposz + k, stone);
            }
        }

        // orig :1160-1174 — boss room hollow shell, 20x18x30 netherrack, at
        // R = (X0 + width + length - 2, Y0 - depth, Z0 - rwidth/2)
        //   = (X0+15, Y0-20, Z0-15). Written BEFORE the corridor so the
        // corridor's air bore carves the room doorway (spec section 13.10).
        int x = cposx + WIDTH + LENGTH - 2;
        int z = cposz - RWIDTH / 2;
        int y = cposy - DEPTH;
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        for (int i = 0; i < RLENGTH; i++) {
            for (int k = 0; k < RWIDTH; k++) {
                for (int j = 0; j < RHEIGHT; j++) {
                    boolean shell = k == 0 || k == RWIDTH - 1 || j == 0 || j == RHEIGHT - 1
                            || i == 0 || i == RLENGTH - 1;
                    piece.place(x + i, y + j, z + k, shell ? netherrack : air);
                }
            }
        }

        // orig :1175-1192 — connecting corridor, 12 long, 5x5 section, from
        // the shaft's east wall plane (X0+4) to the room's west wall (X0+15).
        // Perimeter test is k==0 || k==width-1 || j==0 || j==width-1: the
        // corridor "height" deliberately reuses width (orig :1180 j < width;
        // works only because both are 5 — transcribed as-is, spec 13.9).
        // Floor (j=0) and ceiling (j=4) stay stone; side-wall cells at
        // j=1..3 become standing LAVA SOURCE blocks (orig :1183-1185) that
        // hold only because flag-2 writes skip neighbour updates. Its
        // interior-air writes carve the shaft exit (i=0) and room entrance
        // (i=11) out of the walls written above.
        x = cposx + WIDTH - 1;
        z = cposz;
        y = cposy - DEPTH;
        BlockState lava = Blocks.LAVA.defaultBlockState();
        for (int i = 0; i < LENGTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                for (int j = 0; j < WIDTH; j++) {
                    if (k == 0 || k == WIDTH - 1 || j == 0 || j == WIDTH - 1) {
                        BlockState blk = (j > 0 && j < WIDTH - 1) ? lava : stone;
                        piece.place(x + i, y + j, z + k, blk);
                    } else {
                        piece.place(x + i, y + j, z + k, air);
                    }
                }
            }
        }

        // orig :1193-1196 — cursor back to the room origin, then ++y puts
        // every floor decoration at Ry+1 = Y0-19, the first air layer.
        x = cposx + WIDTH + LENGTH - 2;
        z = cposz - RWIDTH / 2;
        y = cposy - DEPTH + 1;

        // orig :1196-1200 — five lava-square floor hazards.
        addLavaSquare(piece, x + 2, y, z + 2);
        addLavaSquare(piece, x + 4, y, z + 6);
        addLavaSquare(piece, x + 12, y, z + 10);
        addLavaSquare(piece, x + 6, y, z + 15);
        addLavaSquare(piece, x + 3, y, z + 22);

        // orig :1201 — Kyuubi altar at (x + rlength/4, y, z + rwidth*3/4 - 3)
        // = (Rx+5, Ry+1, Rz+19). Integer division: 20/4=5, 30*3/4=22, 22-3=19
        // (keep integer math — spec section 13.7).
        addKyuubiAltar(piece, x + RLENGTH / 4, y, z + RWIDTH * 3 / 4 - 3);

        // orig :1202 — blaze ziggurat at (x + rlength*2/3 - 3, y, z + rwidth/4 - 2)
        // = (Rx+10, Ry+1, Rz+5). Integer division: 40/3=13, 13-3=10; 30/4=7, 7-2=5.
        addBlazeZiggurat(piece, x + RLENGTH * 2 / 3 - 3, y, z + RWIDTH / 4 - 2);

        // orig :1203-1208 — six fire blocks on the netherrack floor layer.
        // Netherrack keeps fire alive forever; flag-2 placement means they
        // never get an initial update tick either (spec section 13.5).
        BlockState fire = Blocks.FIRE.defaultBlockState();
        piece.place(x + 7, y, z + 1, fire);
        piece.place(x + 5, y, z + 9, fire);
        piece.place(x + 2, y, z + 12, fire);
        piece.place(x + 16, y, z + 18, fire);
        piece.place(x + 2, y, z + 27, fire);
        piece.place(x + 18, y, z + 28, fire);
    }

    /**
     * Port of {@code addlavasquare(world, x, y, z)} (orig
     * GenericDungeon.java:1211-1217): a plus-shape of netherrack around a
     * single standing lava source at the centre.
     */
    private static void addLavaSquare(LegacyDungeonPiece piece, int x, int y, int z) {
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        piece.place(x - 1, y, z, netherrack);      // orig :1212
        piece.place(x + 1, y, z, netherrack);      // orig :1213
        piece.place(x, y, z + 1, netherrack);      // orig :1214
        piece.place(x, y, z - 1, netherrack);      // orig :1215
        piece.place(x, y, z, Blocks.LAVA.defaultBlockState()); // orig :1216
    }

    /**
     * Port of {@code addkyuubi(world, x, y, z)} (orig
     * GenericDungeon.java:1219-1258): a 9x9 + 7x7 two-tier nether-brick
     * altar whose tier interiors are LAVA, topped by a column of three
     * stacked "Kyuubi" spawners with the loot chest floating directly on
     * top &mdash; three simultaneously active spawners within 8 blocks of
     * the loot (spec section 13.6).
     */
    private static void addKyuubiAltar(LegacyDungeonPiece piece, int x, int y, int z) {
        BlockState netherBricks = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState lava = Blocks.LAVA.defaultBlockState();

        // orig :1222, :1227-1235 — base tier 9x9: nether-brick rim, 7x7 lava fill.
        int width = 9;
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                boolean rim = k == 0 || k == width - 1 || i == 0 || i == width - 1;
                piece.place(x + i, y, z + k, rim ? netherBricks : lava);
            }
        }

        // orig :1236-1245 — second tier 7x7 at y+1, inset by 1: rim + 5x5 lava.
        width = 7;
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                boolean rim = k == 0 || k == width - 1 || i == 0 || i == width - 1;
                piece.place(x + i + 1, y + 1, z + k + 1, rim ? netherBricks : lava);
            }
        }

        // orig :1246-1251 — three vertically stacked Kyuubi spawners at the centre.
        for (int j = 0; j < 3; j++) {
            piece.placeSpawner(x + 4, y + j + 2, z + 4, ModEntities.ENTITY_KYUUBI.get());
        }

        // orig :1252-1257 — loot chest capping the spawner column, metadata 2
        // = facing NORTH; fill 7 + nextInt(7) = 7-13 pulls of
        // kyuubiContentsList (rolls live in the loot table).
        piece.placeLootChest(x + 4, y + 5, z + 4, Direction.NORTH, KYUUBI_LOOT);
    }

    /**
     * Port of {@code addblaze(world, x, y, z)} (orig
     * GenericDungeon.java:1260-1361): a 4-tier solid obsidian ziggurat
     * (7x7x4, 5x5x1, 3x3x6, 1x1x5 &mdash; the cursor steps +1,+tierHeight,+1
     * between tiers, orig :1280-1282/:1292-1294/:1304-1306), a ring of 8
     * "Blaze" spawners in a cardinal plus around the top column on two
     * layers, and 4 outward-facing loot chests on the tier-1 rim.
     */
    private static void addBlazeZiggurat(LegacyDungeonPiece piece, int x, int y, int z) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        int xx = x;
        int yy = y;
        int zz = z;

        // orig :1264-1279 — tier 1: 7x7, 4 tall, solid.
        int width = 7;
        int height = 4;
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = 0; j < height; j++) {
                    piece.place(xx + i, yy + j, zz + k, obsidian);
                }
            }
        }

        // orig :1280-1291 — tier 2: 5x5, 1 tall.
        xx++;
        yy += height;
        zz++;
        width = 5;
        height = 1;
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = 0; j < height; j++) {
                    piece.place(xx + i, yy + j, zz + k, obsidian);
                }
            }
        }

        // orig :1292-1303 — tier 3: 3x3, 6 tall.
        xx++;
        yy += height;
        zz++;
        width = 3;
        height = 6;
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = 0; j < height; j++) {
                    piece.place(xx + i, yy + j, zz + k, obsidian);
                }
            }
        }

        // orig :1304-1315 — tier 4: 1x1 column, 5 tall. Column top = y+15 =
        // the boss room's highest interior air layer.
        xx++;
        yy += height;
        zz++;
        height = 5;
        for (int j = 0; j < height; j++) {
            piece.place(xx, yy + j, zz, obsidian);
        }

        // orig :1316-1336 — blaze spawner ring: two layers at
        // yy + height + j - 3 = y+13+j (j = 0, 1), four spawners per layer
        // in a cardinal plus around the column (-X, +X, -Z, +Z order kept).
        for (int j = 0; j < 2; j++) {
            int sy = yy + height + j - 3;
            piece.placeSpawner(xx - 1, sy, zz, EntityType.BLAZE); // orig :1317
            piece.placeSpawner(xx + 1, sy, zz, EntityType.BLAZE); // orig :1322
            piece.placeSpawner(xx, sy, zz - 1, EntityType.BLAZE); // orig :1327
            piece.placeSpawner(xx, sy, zz + 1, EntityType.BLAZE); // orig :1332
        }

        // orig :1337-1360 — four loot chests on the tier-1 rim at tier-2
        // level (y+4), each facing outward, each drawing blazeContentsList
        // with its OWN fill formula (hence four loot tables differing only
        // in rolls). Facing metas: 4=WEST, 2=NORTH, 3=SOUTH, 5=EAST.
        piece.placeLootChest(x, y + 4, z + 3, Direction.WEST, BLAZE_LOOT_WEST);      // orig :1337-1342, 4-8
        piece.placeLootChest(x + 3, y + 4, z, Direction.NORTH, BLAZE_LOOT_NORTH);    // orig :1343-1348, 3-7
        piece.placeLootChest(x + 3, y + 4, z + 6, Direction.SOUTH, BLAZE_LOOT_SOUTH); // orig :1349-1354, 5-9
        piece.placeLootChest(x + 6, y + 4, z + 3, Direction.EAST, BLAZE_LOOT_EAST);  // orig :1355-1360, 6-10
    }
}
