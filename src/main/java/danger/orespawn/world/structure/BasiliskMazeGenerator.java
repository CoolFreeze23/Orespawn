package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Basilisk Maze (Mining dimension), ported line-by-line from 1.7.10
 * {@code BasiliskMaze.java} (WGEN-037). Layout, west to east: a sandstone
 * step-pyramid surface marker over a bedrock parkour shaft descending
 * 20-29 blocks (depth {@code D = 20 + nextInt(10)}, BasiliskMaze.java:31)
 * into an iron-ore-walled antechamber, a 30&times;30 obsidian maze
 * (randomised Prim's algorithm over a 10&times;10 grid of 3-block cells)
 * with 80 lava traps under its floor, and a 30&times;30 Basilisk chamber
 * with 20 random-teleport floor traps, 2-4 loot chests on the east wall
 * and 3 persistent Basilisks &mdash; no mob spawner blocks anywhere.
 *
 * <p><b>Mapping decisions</b> (full tables in
 * {@code phase_d_reports/d5_extraction/basilisk_maze_spec.md} and
 * {@code phase_d_reports/D5_structures_spawnores.md}):</p>
 * <ul>
 *   <li><b>RNG:</b> the original drew the maze topology from unseeded
 *       {@code Math.random()} (BasiliskMaze.java:232-234) and everything
 *       else from {@code world.rand}. This port threads the single
 *       deterministic per-piece {@link RandomSource} through the same
 *       call sequence (depth &rarr; topology &rarr; castle) &mdash;
 *       required, because the multi-pass chunk stitching replays the
 *       whole build once per intersecting chunk and every pass must see
 *       identical rolls. Per-instance layout randomness is unchanged;
 *       the only behavioural delta is that layouts are now seed-stable.</li>
 *   <li><b>Entrance carving:</b> {@code openMaze}
 *       (BasiliskMaze.java:278-297) probed world blocks it had just
 *       written. Worldgen passes cannot read neighbouring chunks'
 *       pending writes, so the maze is first rasterised into an
 *       in-memory 30&times;30 wall bitmap that serves both block
 *       placement and the entrance probes &mdash; the probe sees exactly
 *       the state the original's read-after-write saw.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/basilisk_maze}
 *       transcribes the original 31-entry {@code chestContentsList}
 *       (BasiliskMaze.java:28) with the original 5-10 stack rolls
 *       ({@code 5 + nextInt(6)}, BasiliskMaze.java:395). The original's
 *       random-slot placement could overwrite earlier stacks; the
 *       data-driven table cannot &mdash; same documented approximation
 *       the Phase C generic/ruby dungeon chest conversion used.</li>
 * </ul>
 */
final class BasiliskMazeGenerator {

    /** Loot for the 2-4 east-wall chests (orig BasiliskMaze.java:28 + :395). */
    private static final ResourceKey<LootTable> BASILISK_MAZE_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/basilisk_maze"));

    /** Maze grid: 10x10 cells (orig BasiliskMaze.java:33). */
    private static final int GRID_WIDTH = 10;
    /** Cell edge in blocks; corridors are cellSize-1 = 2 wide (orig :33, :45-47). */
    private static final int CELL_SIZE = 3;
    /** Rasterised maze edge: 10 cells x 3 blocks = 30 (clamp bound in orig :131, :139). */
    private static final int MAZE_SPAN = GRID_WIDTH * CELL_SIZE;

    // Cell wall bits (orig BasiliskMaze.java:24-27). The border "no
    // neighbour" flags 16/32/64/128 (orig :55-74) block expansion beyond the
    // grid edge for the top/right/bottom/left directions respectively.
    private static final int WALL_TOP = 1;
    private static final int WALL_RIGHT = 2;
    private static final int WALL_BOTTOM = 4;
    private static final int WALL_LEFT = 8;
    private static final int BORDER_TOP = 16;
    private static final int BORDER_RIGHT = 32;
    private static final int BORDER_BOTTOM = 64;
    private static final int BORDER_LEFT = 128;

    /** Maze grid coordinate (replaces the original's {@code java.awt.Point}). */
    private record GridCell(int x, int y) {}

    private BasiliskMazeGenerator() {}

    /**
     * Direct port of {@code buildBasiliskMaze(world, x, y, z)}
     * (orig BasiliskMaze.java:30-37). {@code origin} is the build origin
     * (X0, Y0, Z0): the worldgen path passes the lowest sampled surface
     * minus 2; the Dungeon Spawner Block (outcome 23) passes the block's
     * own position unmodified.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        // orig :31 — dungeon depth below the entrance, D ∈ [20, 29].
        int depth = 20 + random.nextInt(10);

        clearArea(piece, x + 3, y - depth - 4, z - 20);                    // orig :32
        boolean[][] wallColumns = computeMazeWalls(random);                // orig :33 (topology + raster)
        placeMazeWalls(piece, wallColumns, x + 3, y - depth - 3, z - 20);  // orig :94-109
        openMaze(piece, wallColumns, x + 3, y - depth - 3, z - 20);        // orig :34
        buildCastle(piece, random, x + 3, y - depth - 4, z - 20);          // orig :35
        makeEntrance(piece, x, y, z, depth);                               // orig :36
    }

    /**
     * Direct port of {@code clearArea} (orig BasiliskMaze.java:254-276):
     * the 60&times;30 main cavity &mdash; maze half (west, i &lt; 30)
     * cleared 5 high, castle half 7 high &mdash; plus the 5-wide, 6-high
     * antechamber strip extending west of the cavity.
     */
    private static void clearArea(LegacyDungeonPiece piece, int x, int y, int z) {
        BlockState air = Blocks.AIR.defaultBlockState();
        // orig :258-268 — main cavity.
        for (int i = 0; i < 60; i++) {
            int height = i < 30 ? 5 : 7;
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < 30; k++) {
                    piece.place(x + i, y + j, z + k, air);
                }
            }
        }
        // orig :269-275 — antechamber strip west of the cavity (x-0 .. x-4).
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 30; k++) {
                    piece.place(x - i, y + j, z + k, air);
                }
            }
        }
    }

    /**
     * Direct port of the {@code makeMaze} algorithm
     * (orig BasiliskMaze.java:39-110), returning the rasterised 30&times;30
     * wall bitmap instead of writing blocks (see the class Javadoc on why).
     *
     * <p>Randomised Prim's, exactly as the original: all 100 cells start
     * fully walled (+ border flags); a random start cell moves to the "in"
     * set and its neighbours to the frontier; while the frontier is
     * non-empty, a random frontier cell joins the "in" set, promotes its
     * unvisited neighbours, and knocks down the wall to one random already-in
     * neighbour. The original's {@code rnd(n)} was
     * {@code (int)(Math.random() * n + 1.0)} &mdash; uniform in [1, n] &mdash;
     * ported as {@code random.nextInt(n) + 1} on the piece RNG.</p>
     */
    private static boolean[][] computeMazeWalls(RandomSource random) {
        // orig :48-54 — all cells fully walled.
        int[][] cells = new int[GRID_WIDTH][GRID_WIDTH];
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_WIDTH; y++) {
                cells[x][y] = WALL_TOP | WALL_RIGHT | WALL_BOTTOM | WALL_LEFT;
            }
        }
        // orig :55-74 — border flags: left column 128, right column 32,
        // top row 16, bottom row 64.
        for (int y = 0; y < GRID_WIDTH; y++) {
            cells[0][y] |= BORDER_LEFT;
            cells[GRID_WIDTH - 1][y] |= BORDER_RIGHT;
        }
        for (int x = 0; x < GRID_WIDTH; x++) {
            cells[x][0] |= BORDER_TOP;
            cells[x][GRID_WIDTH - 1] |= BORDER_BOTTOM;
        }

        // orig :75-82 — outlist holds all cells, added x-major.
        List<GridCell> outside = new ArrayList<>(GRID_WIDTH * GRID_WIDTH);
        List<GridCell> inside = new ArrayList<>();
        List<GridCell> frontier = new ArrayList<>();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_WIDTH; y++) {
                outside.add(new GridCell(x, y));
            }
        }

        // orig :83-92 — Prim's expansion.
        GridCell current = removeRandomElement(outside, random);
        inside.add(current);
        moveNeighborsToFrontier(current, cells, outside, frontier);
        while (!frontier.isEmpty()) {
            current = removeRandomElement(frontier, random);
            inside.add(current);
            moveNeighborsToFrontier(current, cells, outside, frontier);
            int wallBit = pickInsideNeighborDirection(current, cells, inside, random);
            removeWall(current, wallBit, cells);
        }

        // orig :94-109 — rasterise every remaining wall bit into 3-block-wide
        // line segments (drawSide), collapsed here into a bitmap of 1-block
        // wall columns; placeMazeWalls stamps each true cell 3 blocks high.
        boolean[][] wallColumns = new boolean[MAZE_SPAN][MAZE_SPAN];
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_WIDTH; y++) {
                int walls = cells[x][y];
                if ((walls & WALL_TOP) != 0) {       // orig :97-99
                    drawWallLine(wallColumns, x * CELL_SIZE, y * CELL_SIZE,
                            (x + 1) * CELL_SIZE, y * CELL_SIZE);
                }
                if ((walls & WALL_RIGHT) != 0) {     // orig :100-102
                    drawWallLine(wallColumns, (x + 1) * CELL_SIZE - 1, y * CELL_SIZE,
                            (x + 1) * CELL_SIZE - 1, (y + 1) * CELL_SIZE);
                }
                if ((walls & WALL_BOTTOM) != 0) {    // orig :103-105
                    drawWallLine(wallColumns, x * CELL_SIZE, (y + 1) * CELL_SIZE - 1,
                            (x + 1) * CELL_SIZE, (y + 1) * CELL_SIZE - 1);
                }
                if ((walls & WALL_LEFT) != 0) {      // orig :106-108
                    drawWallLine(wallColumns, x * CELL_SIZE, y * CELL_SIZE,
                            x * CELL_SIZE, (y + 1) * CELL_SIZE);
                }
            }
        }
        return wallColumns;
    }

    /**
     * Direct port of {@code drawSide}'s line walk
     * (orig BasiliskMaze.java:112-145) into the bitmap: endpoints normalised
     * so from &le; to, the walk is INCLUSIVE of both endpoints, and any
     * coordinate {@code >= 30} is skipped (the original's
     * {@code cellsize * grid} clamp, orig :131, :139). Only the varying axis
     * can reach 30; the fixed axis is always &le; 29. The original's
     * bedrock arm ({@code b != 0}, orig :115-117) is unreachable from
     * {@code buildBasiliskMaze} (always called with b = 0) and is not ported.
     */
    private static void drawWallLine(boolean[][] wallColumns, int fromX, int fromZ, int toX, int toZ) {
        if (fromX > toX) {
            int swap = fromX;
            fromX = toX;
            toX = swap;
        }
        if (fromZ > toZ) {
            int swap = fromZ;
            fromZ = toZ;
            toZ = swap;
        }
        if (fromX == toX) {
            for (int z = fromZ; z <= toZ; z++) {
                if (z >= MAZE_SPAN) continue;
                wallColumns[fromX][z] = true;
            }
        } else {
            for (int x = fromX; x <= toX; x++) {
                if (x >= MAZE_SPAN) continue;
                wallColumns[x][fromZ] = true;
            }
        }
    }

    /**
     * Stamps every bitmap wall column as 3 blocks of obsidian at maze level
     * (orig BasiliskMaze.java:132-134/140-142 &mdash; walls occupy
     * {@code My .. My+2}).
     */
    private static void placeMazeWalls(LegacyDungeonPiece piece, boolean[][] wallColumns,
                                       int mazeX, int mazeY, int mazeZ) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        for (int x = 0; x < MAZE_SPAN; x++) {
            for (int z = 0; z < MAZE_SPAN; z++) {
                if (!wallColumns[x][z]) continue;
                piece.place(mazeX + x, mazeY, mazeZ + z, obsidian);
                piece.place(mazeX + x, mazeY + 1, mazeZ + z, obsidian);
                piece.place(mazeX + x, mazeY + 2, mazeZ + z, obsidian);
            }
        }
    }

    /**
     * Direct port of {@code openMaze} (orig BasiliskMaze.java:278-297).
     * West entrance: scan rows ascending, and at the first row whose cell
     * just inside the west wall (local x = 1) is open, carve a 3-high air
     * column through the wall at local x = 0 into the antechamber. East
     * exit: scan rows DESCENDING probing local x = 28, carving x = 29 into
     * the Basilisk chamber. The original probed the freshly written world
     * blocks; the bitmap holds exactly that state (class Javadoc).
     */
    private static void openMaze(LegacyDungeonPiece piece, boolean[][] wallColumns,
                                 int mazeX, int mazeY, int mazeZ) {
        BlockState air = Blocks.AIR.defaultBlockState();
        // orig :281-288 — west entrance.
        for (int row = 0; row < MAZE_SPAN; row++) {
            if (wallColumns[1][row]) continue;
            piece.place(mazeX, mazeY, mazeZ + row, air);
            piece.place(mazeX, mazeY + 1, mazeZ + row, air);
            piece.place(mazeX, mazeY + 2, mazeZ + row, air);
            break;
        }
        // orig :289-296 — east exit.
        for (int row = MAZE_SPAN - 1; row >= 0; row--) {
            if (wallColumns[MAZE_SPAN - 2][row]) continue;
            piece.place(mazeX + MAZE_SPAN - 1, mazeY, mazeZ + row, air);
            piece.place(mazeX + MAZE_SPAN - 1, mazeY + 1, mazeZ + row, air);
            piece.place(mazeX + MAZE_SPAN - 1, mazeY + 2, mazeZ + row, air);
            break;
        }
    }

    /**
     * Direct port of {@code buildCastle} (orig BasiliskMaze.java:299-411):
     * the shared 60&times;30 obsidian floor with 80 lava traps under the
     * maze half and 20 random-teleport traps under the Basilisk chamber,
     * bedrock ceilings (maze at +4, chamber at +6), triple-thick east/north/
     * south chamber shells, the sandstone-floored iron-ore antechamber with
     * 3 pillars and 3 Extreme Torches, 3 redstone torches on the divider
     * lip, 2-4 loot chests (each with a floating torch 3 above &mdash; the
     * original's no-update {@code setBlockFast} writes let unsupported
     * torches stand, as does the piece's UPDATE_CLIENTS flag), and the 3
     * persistent Basilisks in the chamber centre.
     */
    private static void buildCastle(LegacyDungeonPiece piece, RandomSource random,
                                    int x, int y, int z) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
        BlockState ironOre = Blocks.IRON_ORE.defaultBlockState();

        // orig :303-307 — full 60x30 floor.
        for (int i = 0; i < 60; i++) {
            for (int k = 0; k < 30; k++) {
                piece.place(x + i, y, z + k, obsidian);
            }
        }
        // orig :308-310 — 80 lava traps in the maze-half floor (+1..+28).
        BlockState lava = Blocks.LAVA.defaultBlockState();
        for (int i = 0; i < 80; i++) {
            int trapX = x + random.nextInt(28) + 1;
            int trapZ = z + random.nextInt(28) + 1;
            piece.place(trapX, y, trapZ, lava);
        }
        // orig :311-313 — 20 random-teleport traps in the chamber-half floor.
        BlockState teleportTrap = ModBlocks.BLOCK_TELEPORT.get().defaultBlockState();
        for (int i = 0; i < 20; i++) {
            int trapX = x + 30 + random.nextInt(28) + 1;
            int trapZ = z + random.nextInt(28) + 1;
            piece.place(trapX, y, trapZ, teleportTrap);
        }
        // orig :314-318 — maze ceiling (bedrock, +4).
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 30; k++) {
                piece.place(x + i, y + 4, z + k, bedrock);
            }
        }
        // orig :319-323 — chamber ceiling (bedrock, +6; the chamber is taller).
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 30; k++) {
                piece.place(x + i + 30, y + 6, z + k, bedrock);
            }
        }
        // orig :324-330 — east wall: obsidian at +59, double bedrock shell +60/+61.
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 5; k++) {
                piece.place(x + 59, y + k + 1, z + i, obsidian);
                piece.place(x + 60, y + k + 1, z + i, bedrock);
                piece.place(x + 61, y + k + 1, z + i, bedrock);
            }
        }
        // orig :331-337 — chamber north wall + shell (z-1, z-2).
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 5; k++) {
                piece.place(x + 30 + i, y + k + 1, z, obsidian);
                piece.place(x + 30 + i, y + k + 1, z - 1, bedrock);
                piece.place(x + 30 + i, y + k + 1, z - 2, bedrock);
            }
        }
        // orig :338-344 — chamber south wall + shell (z+30, z+31).
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 5; k++) {
                piece.place(x + 30 + i, y + k + 1, z + 29, obsidian);
                piece.place(x + 30 + i, y + k + 1, z + 30, bedrock);
                piece.place(x + 30 + i, y + k + 1, z + 31, bedrock);
            }
        }
        // orig :345-347 — divider lip above the maze/chamber boundary.
        for (int i = 0; i < 30; i++) {
            piece.place(x + 30, y + 5, z + i, obsidian);
        }
        // orig :348-352 — antechamber floor (sandstone, x-4..x-1).
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 4; k++) {
                piece.place(x - 4 + k, y, z + i, sandstone);
            }
        }
        // orig :353-357 — antechamber ceiling (obsidian, +5).
        for (int i = 0; i < 30; i++) {
            for (int k = 0; k < 4; k++) {
                piece.place(x - 4 + k, y + 5, z + i, obsidian);
            }
        }
        // orig :358-362 — antechamber west wall (iron ore, x-5).
        for (int i = 0; i < 30; i++) {
            for (int k = 1; k < 5; k++) {
                piece.place(x - 5, y + k, z + i, ironOre);
            }
        }
        // orig :363-372 — antechamber north (z-1) and south (z+30) walls.
        for (int i = 0; i < 5; i++) {
            for (int k = 1; k < 5; k++) {
                piece.place(x - 4 + i, y + k, z - 1, ironOre);
                piece.place(x - 4 + i, y + k, z + 30, ironOre);
            }
        }
        // orig :373-381 — 3 sandstone pillars at z, z+15, z+29.
        for (int k = 0; k < 4; k++) {
            piece.place(x - 4, y + 1 + k, z, sandstone);
            piece.place(x - 4, y + 1 + k, z + 15, sandstone);
            piece.place(x - 4, y + 1 + k, z + 29, sandstone);
        }
        // orig :382-384 — 3 Extreme Torches beside the pillars.
        BlockState extremeTorch = ModBlocks.EXTREME_TORCH.get().defaultBlockState();
        piece.place(x - 3, y + 3, z, extremeTorch);
        piece.place(x - 3, y + 3, z + 15, extremeTorch);
        piece.place(x - 3, y + 3, z + 29, extremeTorch);
        // orig :385-387 — 3 redstone torches on the divider.
        BlockState redstoneTorch = Blocks.REDSTONE_TORCH.defaultBlockState();
        piece.place(x + 30, y + 4, z + 2, redstoneTorch);
        piece.place(x + 30, y + 4, z + 15, redstoneTorch);
        piece.place(x + 30, y + 4, z + 27, redstoneTorch);
        // orig :388-396 — 2-4 chests along the east wall, torch 3 above each,
        // fill 5-10 weighted stacks (the counts live in chests/basilisk_maze).
        BlockState torch = Blocks.TORCH.defaultBlockState();
        int chestCount = 2 + random.nextInt(3);
        for (int k = 0; k < chestCount; k++) {
            piece.place(x + 58, y + 4, z + 2 + k * 2, torch);
            piece.placeLootChest(x + 58, y + 1, z + 2 + k * 2, BASILISK_MAZE_LOOT);
        }
        // orig :397-410 — 3 persistent Basilisks at the chamber centre. The
        // yaw rolls stay OUTSIDE the gated spawn helper so the RNG stream is
        // identical in every chunk pass (stitching contract).
        for (int i = 0; i < 3; i++) {
            float yaw = random.nextFloat() * 360.0f;
            piece.spawnPersistent(ModEntities.BASILISK.get(),
                    x + 45.0 + i, y + 1.01, z + 15.0, yaw);
        }
    }

    /**
     * Direct port of {@code makeEntrance} (orig BasiliskMaze.java:413-458).
     * Surface marker: 9 concentric sandstone rings shrinking from
     * 20&times;20 at Y0 to 4&times;4 at Y0+8. Below it, a 4&times;4 bedrock
     * shaft with a 2&times;2 air core descends to one layer above the
     * antechamber ceiling, with a single obsidian parkour step per layer
     * spiralling through the four core cells. The final layers' air core
     * punches through the antechamber's obsidian ceiling (makeEntrance runs
     * after buildCastle, orig :35-36), dropping the player onto the
     * sandstone antechamber floor.
     */
    private static void makeEntrance(LegacyDungeonPiece piece, int x, int y, int z, int depth) {
        BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // orig :417-424 — step pyramid: ring j at level y + 8 - j.
        final int pyramidHeight = 8;
        for (int j = pyramidHeight; j >= 0; j--) {
            for (int i = 0; i < j * 2 + 4; i++) {
                int level = y + pyramidHeight - j;
                piece.place(x + i - j, level, z - j, sandstone);
                piece.place(x + i - j, level, z + j + 3, sandstone);
                piece.place(x - j, level, z + i - j, sandstone);
                piece.place(x + j + 3, level, z + i - j, sandstone);
            }
        }
        // orig :425-457 — descending shaft with spiral parkour column.
        int spiralStep = 0;
        for (int j = pyramidHeight; j > -depth; j--) {
            // orig :427-432 — 4x4 bedrock perimeter ring.
            for (int i = 0; i < 4; i++) {
                piece.place(x + i, y + j, z, bedrock);
                piece.place(x + i, y + j, z + 3, bedrock);
                piece.place(x, y + j, z + i, bedrock);
                piece.place(x + 3, y + j, z + i, bedrock);
            }
            // orig :433-437 — 2x2 interior air core.
            for (int coreX = 0; coreX < 2; coreX++) {
                for (int coreZ = 0; coreZ < 2; coreZ++) {
                    piece.place(x + 1 + coreX, y + j, z + 1 + coreZ, air);
                }
            }
            // orig :438-454 — one obsidian step per layer, cycling the four
            // core cells: (1,1) → (2,1) → (2,2) → (1,2).
            switch (spiralStep) {
                case 0 -> piece.place(x + 1, y + j, z + 1, obsidian);
                case 1 -> piece.place(x + 2, y + j, z + 1, obsidian);
                case 2 -> piece.place(x + 2, y + j, z + 2, obsidian);
                default -> piece.place(x + 1, y + j, z + 2, obsidian);
            }
            // orig :455-456 — k wraps 0..3.
            spiralStep++;
            if (spiralStep > 3) {
                spiralStep = 0;
            }
        }
    }

    // ---- Maze algorithm helpers (orig BasiliskMaze.java:147-241) --------

    /**
     * Direct port of {@code rndElement} (orig BasiliskMaze.java:236-241):
     * remove and return a uniformly random element. The original's
     * {@code rnd(size) - 1} index maps to {@code nextInt(size)}.
     */
    private static GridCell removeRandomElement(List<GridCell> list, RandomSource random) {
        return list.remove(random.nextInt(list.size()));
    }

    /**
     * Direct port of {@code moveNbrs} (orig BasiliskMaze.java:173-191): for
     * each direction whose border flag is clear &mdash; in top, right,
     * bottom, left order &mdash; move that neighbour from the outside list
     * to the frontier if it is still outside ({@code movePoint},
     * orig :193-199).
     */
    private static void moveNeighborsToFrontier(GridCell cell, int[][] cells,
                                                List<GridCell> outside, List<GridCell> frontier) {
        if ((cells[cell.x()][cell.y()] & BORDER_TOP) == 0) {
            promoteIfOutside(new GridCell(cell.x(), cell.y() - 1), outside, frontier);
        }
        if ((cells[cell.x()][cell.y()] & BORDER_RIGHT) == 0) {
            promoteIfOutside(new GridCell(cell.x() + 1, cell.y()), outside, frontier);
        }
        if ((cells[cell.x()][cell.y()] & BORDER_BOTTOM) == 0) {
            promoteIfOutside(new GridCell(cell.x(), cell.y() + 1), outside, frontier);
        }
        if ((cells[cell.x()][cell.y()] & BORDER_LEFT) == 0) {
            promoteIfOutside(new GridCell(cell.x() - 1, cell.y()), outside, frontier);
        }
    }

    /** {@code movePoint} (orig BasiliskMaze.java:193-199). */
    private static void promoteIfOutside(GridCell cell, List<GridCell> outside, List<GridCell> frontier) {
        int index = outside.indexOf(cell);
        if (index >= 0) {
            outside.remove(index);
            frontier.add(cell);
        }
    }

    /**
     * Direct port of {@code findInNbr} (orig BasiliskMaze.java:147-171):
     * starting from a random direction d ∈ [0, 3], scan up to four
     * directions cyclically (checking d BEFORE incrementing) and return the
     * wall bit (top 1 / right 2 / bottom 4 / left 8) of the first direction
     * whose border flag is clear and whose neighbour is already in the "in"
     * set; 0 if none qualifies.
     */
    private static int pickInsideNeighborDirection(GridCell cell, int[][] cells,
                                                   List<GridCell> inside, RandomSource random) {
        int direction = random.nextInt(4);
        for (int attempt = 0; attempt < 4; attempt++) {
            switch (direction) {
                case 0 -> {
                    if ((cells[cell.x()][cell.y()] & BORDER_TOP) == 0
                            && inside.contains(new GridCell(cell.x(), cell.y() - 1))) {
                        return WALL_TOP;
                    }
                }
                case 1 -> {
                    if ((cells[cell.x()][cell.y()] & BORDER_RIGHT) == 0
                            && inside.contains(new GridCell(cell.x() + 1, cell.y()))) {
                        return WALL_RIGHT;
                    }
                }
                case 2 -> {
                    if ((cells[cell.x()][cell.y()] & BORDER_BOTTOM) == 0
                            && inside.contains(new GridCell(cell.x(), cell.y() + 1))) {
                        return WALL_BOTTOM;
                    }
                }
                default -> {
                    if ((cells[cell.x()][cell.y()] & BORDER_LEFT) == 0
                            && inside.contains(new GridCell(cell.x() - 1, cell.y()))) {
                        return WALL_LEFT;
                    }
                }
            }
            direction = (direction + 1) % 4;
        }
        return 0;
    }

    /**
     * Direct port of {@code removeWall} (orig BasiliskMaze.java:201-230):
     * XOR the chosen wall bit off the cell and the reciprocal bit off the
     * neighbour (top&harr;bottom with y&plusmn;1, right&harr;left with
     * x&plusmn;1). A direction of 0 (no in-neighbour found) is a no-op on
     * the cell and matches no switch arm, exactly like the original.
     */
    private static void removeWall(GridCell cell, int wallBit, int[][] cells) {
        cells[cell.x()][cell.y()] ^= wallBit;
        switch (wallBit) {
            case WALL_TOP -> cells[cell.x()][cell.y() - 1] ^= WALL_BOTTOM;
            case WALL_RIGHT -> cells[cell.x() + 1][cell.y()] ^= WALL_LEFT;
            case WALL_BOTTOM -> cells[cell.x()][cell.y() + 1] ^= WALL_TOP;
            case WALL_LEFT -> cells[cell.x() - 1][cell.y()] ^= WALL_RIGHT;
        }
    }
}
