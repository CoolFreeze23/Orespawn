package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Crystal Maze &mdash; modernized port of the legacy
 * {@code danger.orespawn.CrystalMaze.buildCrystalMaze} (1.7.10 chunk
 * provider). The original was wired into
 * {@code ChunkProviderOreSpawn5#provideChunk} so every chunk of the
 * Crystal Dimension got a 16&times;16&times;3 maze stamped at {@code Y=25}.
 *
 * <p>The maze itself is a chunk-bounded 4&times;4 grid of 4&times;4 cells
 * carved by a Prim-style frontier algorithm:</p>
 * <ul>
 *   <li>Every cell starts with all 4 walls present (bitmask 1=top,
 *       2=right, 4=bottom, 8=left, identical to the legacy WTOP/WRGT/
 *       WBOT/WLFT constants).</li>
 *   <li>Edge cells get an "outer-wall" bit set (16/32/64/128 for
 *       top/right/bottom/left of the grid) to seed the boundary.</li>
 *   <li>A random cell is added to the in-list and its non-outer-wall
 *       neighbours are moved to the frontier list. The frontier is then
 *       drained one cell at a time, removing one wall to a random
 *       in-list neighbour each time.</li>
 *   <li>Walls are stamped 3 blocks tall as bedrock (legacy
 *       {@code drawSide} with {@code bb != 0}). Floor and ceiling are
 *       bedrock to fully enclose the maze, with 4 random ceiling holes
 *       and 1 random floor hole exposing {@code crystal_stone} so the
 *       player can find / drop into the maze from the surface.</li>
 * </ul>
 *
 * <p><b>Stability guard:</b> in 1.7.10 this generated to a raw
 * {@link net.minecraft.world.chunk.LevelChunk}, which sidestepped the
 * "writes to the wrong chunk" trap by definition. In 1.21.1 the
 * {@link Feature} pipeline gives us a {@link WorldGenLevel} that exposes
 * the active chunk's bounding box; we anchor the entire maze to the
 * chunk that owns {@code ctx.origin()}, never write outside the local
 * 16&times;16 column, and reject the placement entirely if the maze Y
 * window would clip world build limits. This satisfies the user's
 * "zero cross-chunk runaway generation" requirement.</p>
 *
 * <p>One {@link danger.orespawn.entity.EntityRotator Rotator} spawner is
 * placed in the centre cell of every successfully generated maze
 * (mirrors the legacy crystal-dim spawner pool that pumped Rotators into
 * dim-5 underground voids).</p>
 */
public class CrystalMazeFeature extends Feature<NoneFeatureConfiguration> {

    /** Side length in cells. Legacy: {@code xw = zw = 4}. */
    private static final int GRID = 4;
    /** Side length of one maze cell in blocks. Legacy: {@code csz = 4}. */
    private static final int CELL = 4;
    /** Total side length in blocks (16 = exactly one chunk wide). */
    private static final int FOOTPRINT = GRID * CELL;
    /** Maze interior height in blocks. Legacy uses 3 air rows + bedrock cap. */
    private static final int INTERIOR_HEIGHT = 3;
    /** Anchor Y for the maze floor. Legacy: 25 (just above sea level). */
    private static final int MAZE_FLOOR_Y = 25;

    /** Wall bit constants — direct mirrors of the legacy WTOP/WRGT/WBOT/WLFT. */
    private static final int WTOP = 1;
    private static final int WRGT = 2;
    private static final int WBOT = 4;
    private static final int WLFT = 8;
    /** Outer-edge sentinel bits the legacy code uses to fence the boundary. */
    private static final int OUTER_TOP = 16;
    private static final int OUTER_RIGHT = 32;
    private static final int OUTER_BOTTOM = 64;
    private static final int OUTER_LEFT = 128;

    public CrystalMazeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();

        // Anchor the maze to the OWNING chunk's NW corner. ctx.origin() is
        // already inside the chunk currently being generated; rounding down
        // to the 16-block grid keeps us strictly inside that chunk's
        // bounding box even if a placement modifier nudged the X/Z coords.
        int chunkX = (origin.getX() >> 4) << 4;
        int chunkZ = (origin.getZ() >> 4) << 4;
        BlockPos mazeOrigin = new BlockPos(chunkX, MAZE_FLOOR_Y, chunkZ);

        // Up-front bound-check: never write below worldgen floor or above
        // build height. Bail without partial writes on failure.
        if (mazeOrigin.getY() < level.getMinBuildHeight() + 1) return false;
        if (mazeOrigin.getY() + INTERIOR_HEIGHT + 1 >= level.getMaxBuildHeight()) return false;

        // Use a deterministic per-chunk RNG so re-generation produces the
        // same maze layout for the same seed — matches modern world-gen
        // determinism and lets shaders/datapacks reason about shape.
        Random random = new Random(ctx.random().nextLong()
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L));

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();

        // ---- 1. Hollow the 16x16x3 interior (legacy lines 21-27) ----
        for (int dx = 0; dx < FOOTPRINT; dx++) {
            for (int dz = 0; dz < FOOTPRINT; dz++) {
                for (int dy = 0; dy < INTERIOR_HEIGHT; dy++) {
                    level.setBlock(mazeOrigin.offset(dx, dy, dz), air, 2);
                }
            }
        }

        // ---- 2. Bedrock floor + ceiling (legacy lines 49-54) ----
        for (int dx = 0; dx < FOOTPRINT; dx++) {
            for (int dz = 0; dz < FOOTPRINT; dz++) {
                level.setBlock(mazeOrigin.offset(dx, -1, dz), bedrock, 2);
                level.setBlock(mazeOrigin.offset(dx, INTERIOR_HEIGHT, dz), bedrock, 2);
            }
        }

        // ---- 3. Build the maze cell grid ----
        int[][] cells = new int[GRID][GRID];
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                cells[x][z] = WTOP | WRGT | WBOT | WLFT;
            }
        }
        // Outer-edge sentinels (legacy lines 81-100).
        for (int z = 0; z < GRID; z++) {
            cells[0][z] |= OUTER_LEFT;
            cells[GRID - 1][z] |= OUTER_RIGHT;
        }
        for (int x = 0; x < GRID; x++) {
            cells[x][0] |= OUTER_TOP;
            cells[x][GRID - 1] |= OUTER_BOTTOM;
        }

        // Frontier walk
        List<int[]> outlist = new ArrayList<>(GRID * GRID);
        List<int[]> inlist = new ArrayList<>(GRID * GRID);
        List<int[]> frontlist = new ArrayList<>(GRID * GRID);
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                outlist.add(new int[]{x, z});
            }
        }
        int[] current = popRandom(outlist, random);
        inlist.add(current);
        moveNeighbours(current, cells, outlist, frontlist);
        while (!frontlist.isEmpty()) {
            current = popRandom(frontlist, random);
            inlist.add(current);
            moveNeighbours(current, cells, outlist, frontlist);
            int dir = findInNeighbour(current, cells, inlist, random);
            if (dir != 0) removeWall(current, dir, cells);
        }

        // ---- 4. Stamp walls (legacy lines 120-135 + drawSide) ----
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                int v = cells[x][z];
                if ((v & WTOP) != 0) {
                    drawWall(level, mazeOrigin, x * CELL, z * CELL, (x + 1) * CELL, z * CELL, bedrock);
                }
                if ((v & WRGT) != 0) {
                    drawWall(level, mazeOrigin, (x + 1) * CELL - 1, z * CELL, (x + 1) * CELL - 1, (z + 1) * CELL, bedrock);
                }
                if ((v & WBOT) != 0) {
                    drawWall(level, mazeOrigin, x * CELL, (z + 1) * CELL - 1, (x + 1) * CELL, (z + 1) * CELL - 1, bedrock);
                }
                if ((v & WLFT) != 0) {
                    drawWall(level, mazeOrigin, x * CELL, z * CELL, x * CELL, (z + 1) * CELL, bedrock);
                }
            }
        }

        // ---- 5. Random ceiling holes + floor hole (legacy lines 55-62) ----
        for (int hole = 0; hole < 4; hole++) {
            int hx = random.nextInt(FOOTPRINT);
            int hz = random.nextInt(FOOTPRINT);
            level.setBlock(mazeOrigin.offset(hx, INTERIOR_HEIGHT, hz), crystalStone, 2);
        }
        int floorHoleX = random.nextInt(FOOTPRINT);
        int floorHoleZ = random.nextInt(FOOTPRINT);
        level.setBlock(mazeOrigin.offset(floorHoleX, -1, floorHoleZ), crystalStone, 2);

        // ---- 6. One Rotator spawner in the centre cell ----
        // Anchored to the cell that owns (GRID/2, GRID/2). Drop one block to
        // sit on the maze floor, never at the wall position.
        int centreX = (GRID / 2) * CELL + 1;
        int centreZ = (GRID / 2) * CELL + 1;
        BlockPos spawnerPos = mazeOrigin.offset(centreX, 0, centreZ);
        // Make sure we didn't roll a wall into that exact cell — if so, jog
        // by 1 block to keep the spawner reachable.
        if (!level.getBlockState(spawnerPos).isAir()) {
            spawnerPos = spawnerPos.offset(1, 0, 0);
        }
        placeSpawner(level, spawnerPos, ModEntities.ENTITY_ROTATOR.get());
        return true;
    }

    /** Stamps a single 3-tall bedrock wall segment from (fromX,fromZ) to (toX,toZ). */
    private static void drawWall(WorldGenLevel level, BlockPos mazeOrigin,
                                 int fromX, int fromZ, int toX, int toZ, BlockState wall) {
        if (fromX > toX) { int t = fromX; fromX = toX; toX = t; }
        if (fromZ > toZ) { int t = fromZ; fromZ = toZ; toZ = t; }
        if (fromX == toX) {
            for (int z = fromZ; z <= toZ; z++) {
                if (z >= FOOTPRINT) continue;
                for (int dy = 0; dy < INTERIOR_HEIGHT; dy++) {
                    level.setBlock(mazeOrigin.offset(fromX, dy, z), wall, 2);
                }
            }
        } else {
            for (int x = fromX; x <= toX; x++) {
                if (x >= FOOTPRINT) continue;
                for (int dy = 0; dy < INTERIOR_HEIGHT; dy++) {
                    level.setBlock(mazeOrigin.offset(x, dy, fromZ), wall, 2);
                }
            }
        }
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }

    /**
     * Direct port of legacy {@code moveNbrs} — for each non-outer-wall
     * neighbour of {@code p}, transfers it from the {@code outlist} to the
     * {@code frontlist}. Skips neighbours already promoted.
     */
    private static void moveNeighbours(int[] p, int[][] cells,
                                        List<int[]> outlist, List<int[]> frontlist) {
        int v = cells[p[0]][p[1]];
        if ((v & OUTER_TOP) == 0) movePoint(new int[]{p[0], p[1] - 1}, outlist, frontlist);
        if ((v & OUTER_RIGHT) == 0) movePoint(new int[]{p[0] + 1, p[1]}, outlist, frontlist);
        if ((v & OUTER_BOTTOM) == 0) movePoint(new int[]{p[0], p[1] + 1}, outlist, frontlist);
        if ((v & OUTER_LEFT) == 0) movePoint(new int[]{p[0] - 1, p[1]}, outlist, frontlist);
    }

    private static void movePoint(int[] p, List<int[]> outlist, List<int[]> frontlist) {
        int idx = indexOf(outlist, p);
        if (idx >= 0) {
            outlist.remove(idx);
            frontlist.add(p);
        }
    }

    /**
     * Direct port of legacy {@code findInNbr} — picks a random in-list
     * neighbour of {@code p} and returns the wall bit (1/2/4/8) that
     * separates them. Returns 0 if no eligible neighbour exists (in which
     * case the legacy code skipped the wall removal too).
     */
    private static int findInNeighbour(int[] p, int[][] cells, List<int[]> inlist, Random random) {
        int v = cells[p[0]][p[1]];
        int d = random.nextInt(4);
        for (int k = 0; k < 4; k++) {
            switch (d) {
                case 0:
                    if ((v & OUTER_TOP) == 0 && indexOf(inlist, new int[]{p[0], p[1] - 1}) >= 0) return WTOP;
                    break;
                case 1:
                    if ((v & OUTER_RIGHT) == 0 && indexOf(inlist, new int[]{p[0] + 1, p[1]}) >= 0) return WRGT;
                    break;
                case 2:
                    if ((v & OUTER_BOTTOM) == 0 && indexOf(inlist, new int[]{p[0], p[1] + 1}) >= 0) return WBOT;
                    break;
                case 3:
                    if ((v & OUTER_LEFT) == 0 && indexOf(inlist, new int[]{p[0] - 1, p[1]}) >= 0) return WLFT;
                    break;
            }
            d = (d + 1) % 4;
        }
        return 0;
    }

    /**
     * Direct port of legacy {@code removeWall} — XORs the wall bit out of
     * cell {@code p} AND its mirror bit out of the neighbour, so the maze
     * stays consistent from either side.
     */
    private static void removeWall(int[] p, int dir, int[][] cells) {
        cells[p[0]][p[1]] ^= dir;
        switch (dir) {
            case WTOP:  cells[p[0]][p[1] - 1] ^= WBOT; break;
            case WRGT:  cells[p[0] + 1][p[1]] ^= WLFT; break;
            case WBOT:  cells[p[0]][p[1] + 1] ^= WTOP; break;
            case WLFT:  cells[p[0] - 1][p[1]] ^= WRGT; break;
        }
    }

    private static int[] popRandom(List<int[]> list, Random random) {
        int idx = random.nextInt(list.size());
        return list.remove(idx);
    }

    private static int indexOf(List<int[]> list, int[] target) {
        for (int i = 0; i < list.size(); i++) {
            int[] p = list.get(i);
            if (p[0] == target[0] && p[1] == target[1]) return i;
        }
        return -1;
    }
}
