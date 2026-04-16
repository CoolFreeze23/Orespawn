package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.entity.RockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal dimension structures faithfully ported from the original 1.7.10
 * OreSpawnWorld, GenericDungeon, and Trees classes.
 *
 * <p>Invocation: called from {@link OreSpawnChunkGenerator#applyBiomeDecoration}
 * (NOT {@code buildSurface}) so cross-chunk block writes are reliable — by the
 * decoration phase neighboring chunks have already finished terrain generation,
 * preventing the "structures cut off at chunk borders" issue.</p>
 *
 * <p>1.21.1 paradigm notes:</p>
 * <ul>
 *   <li>Structures are placed directly via {@link WorldGenLevel#setBlock} rather
 *       than through {@code StructureFeature}/{@code ConfiguredFeature} because
 *       the original 1.7.10 generation logic is stateful and procedural — porting
 *       it to the data-driven registry would require a full rewrite.</li>
 *   <li>Spawners use {@code SpawnerBlockEntity.getSpawner().setEntityId(type, null,
 *       random, pos)} — passing {@code null} for {@code Level} is intentional; a
 *       non-null Level triggers synchronous chunk loads during worldgen and
 *       deadlocks the server thread.</li>
 *   <li>{@link WeightedRandomChestContent} (1.7.10) has no direct analog, so loot
 *       tables are inlined as {@link ItemStack} picker methods that emulate the
 *       original weights.</li>
 * </ul>
 *
 * <p>Frequencies preserved from 1.7.10:</p>
 * <ul>
 *   <li>FairyTree: 1/5 per chunk (then 1/5 chance of FairyCastleTree variant)</li>
 *   <li>RotatorStation: 1/150</li>
 *   <li>UrchinSpawner: 1/180</li>
 *   <li>CrystalHauntedHouse: 1/230</li>
 *   <li>RoundRotator: 1/150</li>
 *   <li>CrystalBattleTower: 1/280</li>
 *   <li>IrukandjiSpawner: 1/80 (water)</li>
 *   <li>Rocks: 1/4 gate * 1/5 pass = 1/20 effective</li>
 * </ul>
 */
public class CrystalStructures {

    /**
     * Simple cooldown to stop multiple large structures from piling up in adjacent
     * chunks. Decremented each invocation. Static is acceptable because worldgen
     * for a single dimension is effectively serialized at this phase.
     */
    private static int recentlyPlaced = 0;

    public static void generate(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ) {
        if (recentlyPlaced > 0) {
            recentlyPlaced--;
        }

        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();

        if (tryPlaceFairyTree(level, random, chunkX, chunkZ, crystalGrass)) return;

        placeCrystalTermiteBlocks(level, random, chunkX, chunkZ, crystalGrass);

        if (recentlyPlaced == 0) {
            if (tryPlaceRotatorStation(level, random, chunkX, chunkZ, crystalGrass)) return;
            if (tryPlaceUrchinSpawner(level, random, chunkX, chunkZ, crystalGrass)) return;
            if (tryPlaceCrystalHauntedHouse(level, random, chunkX, chunkZ, crystalGrass)) return;
            if (tryPlaceRoundRotator(level, random, chunkX, chunkZ, crystalGrass)) return;
            tryPlaceCrystalBattleTower(level, random, chunkX, chunkZ, crystalGrass);

            placeIrukandjiSpawner(level, random, chunkX, chunkZ);
        }

        placeMazeChestsAndSpawners(level, random, chunkX, chunkZ);

        if (random.nextInt(4) == 1) {
            addRocks(level, random, chunkX, chunkZ, crystalGrass);
        }
    }

    // =====================================================================
    // FAIRY TREE - faithful port from Trees.FairyTree / Trees.FairyCastleTree
    // =====================================================================

    private static boolean tryPlaceFairyTree(WorldGenLevel level, RandomSource random,
                                              int chunkX, int chunkZ, BlockState grassState) {
        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        if (random.nextInt(5) != 0) return false;

        for (int posY = 128; posY > 40; posY--) {
            BlockPos pos = new BlockPos(posX, posY, posZ);
            BlockPos below = new BlockPos(posX, posY - 1, posZ);
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(below).equals(grassState))
                continue;

            for (int i = -8; i <= 8; i++) {
                for (int j = -8; j <= 8; j++) {
                    if (!level.getBlockState(new BlockPos(posX + i, posY, posZ + j)).isAir())
                        return false;
                }
            }

            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (!level.getBlockState(new BlockPos(posX + i, posY - 1, posZ + j)).equals(grassState))
                        return false;
                }
            }

            if (random.nextInt(5) != 1) {
                buildFairyTree(level, random, posX, posY - 1, posZ);
            } else {
                buildFairyCastleTree(level, random, posX, posY, posZ);
            }
            recentlyPlaced = 50;
            return true;
        }
        return false;
    }

    /**
     * Original FairyTree: 2x2 trunk, 8 branching arms with leaf clusters,
     * a Fairy spawner and a crystal-loot chest at the base.
     */
    private static void buildFairyTree(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        BlockState log = ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();

        for (int j = 1; j < 6; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    safeSetBlock(level, x + i, y + j, z + k, log);
                }
            }
        }

        growCrystalBranch(level, random, x, y + 5, z,       0,  1,  1,  1, -1);
        growCrystalBranch(level, random, x + 1, y + 5, z,   1,  0,  1, -1, -1);
        growCrystalBranch(level, random, x, y + 5, z + 1,  -1,  0, -1,  1, -1);
        growCrystalBranch(level, random, x + 1, y + 5, z + 1, 0, -1, -1, -1, -1);
        growCrystalBranch(level, random, x, y + 6, z,       0,  1, -1,  1, -1);
        growCrystalBranch(level, random, x + 1, y + 6, z,   1,  0,  1,  1, -1);
        growCrystalBranch(level, random, x, y + 6, z + 1,  -1,  0, -1, -1, -1);
        growCrystalBranch(level, random, x + 1, y + 6, z + 1, 0, -1,  1, -1, -1);

        int grow = 5 + random.nextInt(5);
        for (int j = 6; j < 6 + grow; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    safeSetBlock(level, x + i, y + j, z + k, log);
                    makeCrystalLeaves(level, x + i, y + j, z + k);
                }
            }
        }

        placeSpawner(level, new BlockPos(x - 1, y + 1, z), ModEntities.FAIRY.get());

        BlockPos chestPos = new BlockPos(x + 2, y + 1, z);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
            fillCrystalChest(container, random);
        }
    }

    private static void makeCrystalLeaves(WorldGenLevel level, int x, int y, int z) {
        BlockState leaves3 = ModBlocks.CRYSTAL_LEAVES_3.get().defaultBlockState();
        for (int l1 = -2; l1 <= 2; l1++) {
            for (int l2 = -2; l2 <= 2; l2++) {
                for (int l3 = 0; l3 <= 1; l3++) {
                    BlockPos pos = new BlockPos(x + l1, y + l3, z + l2);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, leaves3, 2);
                    }
                }
            }
        }
    }

    private static void makeCrystalCastleLeaves(WorldGenLevel level, int x, int y, int z) {
        BlockState leaves2 = ModBlocks.CRYSTAL_LEAVES_2.get().defaultBlockState();
        BlockState leaves3 = ModBlocks.CRYSTAL_LEAVES_3.get().defaultBlockState();
        for (int l1 = -1; l1 <= 1; l1++) {
            for (int l2 = -1; l2 <= 1; l2++) {
                for (int l3 = 0; l3 <= 1; l3++) {
                    BlockPos pos = new BlockPos(x + l1, y + l3, z + l2);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, l3 == 0 ? leaves2 : leaves3, 2);
                    }
                }
            }
        }
    }

    /**
     * Faithful port of Trees.grow_crystal_branch: five growth segments
     * that create the iconic FairyTree branching pattern.
     */
    private static void growCrystalBranch(WorldGenLevel level, RandomSource random,
                                           int x, int y, int z,
                                           int xdir, int zdir, int xxdir, int zzdir, int ydir) {
        BlockState log = ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
        int i = x, j = y, k = z;
        int i2, k2, j2;

        int grow = 4 + random.nextInt(4);
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i, j, k, log);
            makeCrystalLeaves(level, i, j, k);
            j++;
            i += xdir;
            k += zdir;
        }
        i2 = i;
        k2 = k;

        grow = 5 + random.nextInt(5);
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i, j, k, log);
            makeCrystalLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
        }

        grow = 5 + random.nextInt(5);
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i2, j, k2, log);
            makeCrystalLeaves(level, i2, j, k2);
            i2 += xxdir;
            k2 += zzdir;
        }

        j--;
        j2 = j;
        grow = 4 + random.nextInt(4);
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i, j, k, log);
            makeCrystalLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
            j += ydir;
        }

        grow = 4 + random.nextInt(4);
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i2, j2, k2, log);
            makeCrystalLeaves(level, i2, j2, k2);
            i2 += xxdir;
            k2 += zzdir;
            j2 += ydir;
        }
    }

    /**
     * Original FairyCastleTree: expanding tiers of log platforms with leaves,
     * crystal torches on corners, and random spawners/chests via addSomething.
     */
    private static void buildFairyCastleTree(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        BlockState log = ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
        BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();
        int nc = 6;
        int j = 3 + random.nextInt(3);
        int spread = 0;

        for (int iter = 0; iter < nc; iter++) {
            int grow = 4 + random.nextInt(3);
            int width = 1 + random.nextInt(3);
            int randy = random.nextInt(3) - 1;

            buildCastlePlatform(level, random, x + spread, y + j + randy, z, width, log, torch, iter > 0);

            if (iter > 0) {
                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x - spread, y + j + randy, z, width, log, torch, true);

                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x, y + j + randy, z + spread, width, log, torch, true);

                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x, y + j + randy, z - spread, width, log, torch, true);
            }

            if (iter >= 2) {
                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x + spread, y + j + randy, z + spread, width, log, torch, true);

                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x - spread, y + j + randy, z - spread, width, log, torch, true);

                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x - spread, y + j + randy, z + spread, width, log, torch, true);

                width = 1 + random.nextInt(3 + iter);
                randy = random.nextInt(3) - 1;
                buildCastlePlatform(level, random, x + spread, y + j + randy, z - spread, width, log, torch, true);
            }

            j += grow;
            if (iter == 0) spread = 3;
            spread += grow;
        }
    }

    private static void buildCastlePlatform(WorldGenLevel level, RandomSource random,
                                             int cx, int cy, int cz, int width,
                                             BlockState log, BlockState torch, boolean addContent) {
        for (int i = -width; i <= width; i++) {
            for (int k = -width; k <= width; k++) {
                safeSetBlock(level, cx + i, cy, cz + k, log);
                if (i == -width || i == width || k == -width || k == width) {
                    makeCrystalCastleLeaves(level, cx + i, cy, cz + k);
                }
                if (addContent && i == 0 && k == 0) {
                    addSomething(level, random, cx, cy, cz);
                }
                if (i == -width && (k == -width || k == width)) {
                    safeSetBlock(level, cx + i, cy + 1, cz + k, torch);
                }
                if (i == width && (k == -width || k == width)) {
                    safeSetBlock(level, cx + i, cy + 1, cz + k, torch);
                }
            }
        }
    }

    private static void addSomething(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int choice = random.nextInt(3);
        if (choice == 1) {
            placeSpawner(level, new BlockPos(x, y + 1, z), ModEntities.FAIRY.get());
        } else if (choice == 2) {
            BlockPos chestPos = new BlockPos(x, y + 1, z);
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
                fillCrystalChest(container, random);
            }
        }
    }

    // =====================================================================
    // ROTATOR STATION - 1/150 chance
    // =====================================================================

    private static boolean tryPlaceRotatorStation(WorldGenLevel level, RandomSource random,
                                                   int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(150) != 0) return false;

        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                        || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                    continue;

                level.setBlock(new BlockPos(posX, posY + 4, posZ), crystalStone, 2);
                placeSpawner(level, new BlockPos(posX, posY + 5, posZ), ModEntities.ENTITY_ROTATOR.get());
                placeSpawner(level, new BlockPos(posX, posY + 6, posZ), ModEntities.ENTITY_ROTATOR.get());
                level.setBlock(new BlockPos(posX, posY + 7, posZ), crystalStone, 2);

                BlockPos chestPos = new BlockPos(posX, posY + 8, posZ);
                level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
                if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
                    fillCrystalChest(container, random);
                }

                recentlyPlaced = 50;
                return true;
            }
        }
        return false;
    }

    // =====================================================================
    // URCHIN SPAWNER - 1/180 chance
    // =====================================================================

    private static boolean tryPlaceUrchinSpawner(WorldGenLevel level, RandomSource random,
                                                  int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(180) != 0) return false;

        BlockState[] columnBlocks = {
                ModBlocks.CRYSTAL_STONE.get().defaultBlockState(),
                ModBlocks.CRYSTAL_CRYSTAL.get().defaultBlockState(),
                ModBlocks.TIGERS_EYE_ORE.get().defaultBlockState()
        };

        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        for (int posY = 100; posY > 50; posY--) {
            if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                    || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                continue;

            for (int col = 0; col < 3; col++) {
                float dx = random.nextFloat() - random.nextFloat();
                float dz = random.nextFloat() - random.nextFloat();
                float dy = 0.5f + random.nextFloat() / 2.0f;
                int w = random.nextInt(2);
                int length = col == 0 ? (10 + w * 3 + random.nextInt(5)) : (5 + w + random.nextInt(3));
                float rx = posX, ry = posY, rz = posZ;

                for (int iy = 0; iy <= length; iy++) {
                    for (int ix = 0; ix <= w; ix++) {
                        for (int iz = 0; iz <= w; iz++) {
                            safeSetBlock(level, (int)(rx + ix), (int)ry, (int)(rz + iz), columnBlocks[col]);
                        }
                    }
                    ry += dy;
                    rx += dx;
                    rz += dz;
                }
            }

            placeSpawner(level, new BlockPos(posX, posY + 1, posZ), ModEntities.URCHIN.get());
            placeSpawner(level, new BlockPos(posX, posY + 2, posZ), ModEntities.URCHIN.get());
            placeSpawner(level, new BlockPos(posX, posY + 3, posZ), ModEntities.URCHIN.get());
            level.setBlock(new BlockPos(posX, posY, posZ), Blocks.AIR.defaultBlockState(), 2);

            BlockPos chestPos = new BlockPos(posX, posY - 1, posZ);
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
                fillCrystalChest(container, random);
            }

            recentlyPlaced = 50;
            return true;
        }
        return false;
    }

    // =====================================================================
    // CRYSTAL HAUNTED HOUSE - 1/230 chance
    // Original: width=3, length=3, height=3 (7x7x4 building)
    // =====================================================================

    private static boolean tryPlaceCrystalHauntedHouse(WorldGenLevel level, RandomSource random,
                                                        int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(230) != 0) return false;

        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        for (int posY = 100; posY > 50; posY--) {
            if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                    || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                continue;

            buildCrystalHauntedHouse(level, random, posX, posY, posZ);
            recentlyPlaced = 50;
            return true;
        }
        return false;
    }

    private static void buildCrystalHauntedHouse(WorldGenLevel level, RandomSource random,
                                                   int x, int y, int z) {
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        BlockState crystalPlanks = ModBlocks.CRYSTAL_PLANKS.get().defaultBlockState();
        BlockState slabs = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int width = 3;
        int length = 3;
        int height = 3;
        int deltax = 1;

        for (int i = -width; i <= width; i++) {
            for (int j = -length; j <= length; j++) {
                for (int k = 0; k <= height + 1; k++) {
                    if (k == height + 1) {
                        safeSetBlock(level, x + i, y + k, z + j, crystalPlanks);
                    } else if (k == 0) {
                        safeSetBlock(level, x + i, y + k, z + j, crystalStone);
                    } else if (i == width || j == length || i == -width || j == -length) {
                        if (k == height) {
                            safeSetBlock(level, x + i, y + k, z + j, slabs);
                        } else if ((k == 1 || k == 2) && i == deltax * width && j == 0) {
                            safeSetBlock(level, x + i, y + k, z + j, air);
                        } else {
                            safeSetBlock(level, x + i, y + k, z + j, crystalPlanks);
                        }
                    } else {
                        safeSetBlock(level, x + i, y + k, z + j, air);
                    }
                }
            }
        }

        BlockState furnace = ModBlocks.CRYSTAL_FURNACE.get().defaultBlockState();
        BlockState workbench = ModBlocks.CRYSTAL_WORKBENCH.get().defaultBlockState();
        safeSetBlock(level, x + 2, y + 1, z + 2, furnace);
        safeSetBlock(level, x + 1, y + 1, z + 2, workbench);

        BlockPos chestPos = new BlockPos(x, y + 1, z + 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
            fillHauntedHouseChest(container, random);
        }

        placeSpawner(level, new BlockPos(x, y + 1, z), ModEntities.ENTITY_RAT.get());
        placeSpawner(level, new BlockPos(x, y + 2, z), ModEntities.GHOST.get());
        placeSpawner(level, new BlockPos(x, y + 3, z), ModEntities.GHOST_SKELLY.get());
    }

    // =====================================================================
    // ROUND ROTATOR - 1/150 chance
    // Original uses obsidian outer ring, crystal pink inner ring
    // =====================================================================

    private static boolean tryPlaceRoundRotator(WorldGenLevel level, RandomSource random,
                                                 int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(150) != 0) return false;

        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        for (int posY = 100; posY > 50; posY--) {
            if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                    || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                continue;

            int centerY = posY + 6;
            BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
            BlockState pink = ModBlocks.BLOCK_CRYSTAL_PINK.get().defaultBlockState();
            BlockState coal = ModBlocks.CRYSTAL_COAL.get().defaultBlockState();

            for (float deg = 0; deg < 360; deg += 5) {
                double rad = Math.toRadians(deg);
                int bx = (int)(posX + 6 * Math.cos(rad) + 0.5);
                int by = (int)(centerY + 6 * Math.sin(rad) + 0.5);
                safeSetBlock(level, bx, by, posZ, obsidian);
            }
            for (float deg = 0; deg < 360; deg += 5) {
                double rad = Math.toRadians(deg);
                int bx = (int)(posX + 2 * Math.cos(rad) + 0.5);
                int by = (int)(centerY + 2 * Math.sin(rad) + 0.5);
                safeSetBlock(level, bx, by, posZ, pink);
            }

            placeSpawner(level, new BlockPos(posX + 1, centerY + 1, posZ), ModEntities.ENTITY_ROTATOR.get());
            placeSpawner(level, new BlockPos(posX - 1, centerY - 1, posZ), ModEntities.ENTITY_ROTATOR.get());
            placeSpawner(level, new BlockPos(posX + 1, centerY - 1, posZ), ModEntities.ENTITY_ROTATOR.get());
            placeSpawner(level, new BlockPos(posX - 1, centerY + 1, posZ), ModEntities.ENTITY_ROTATOR.get());
            placeSpawner(level, new BlockPos(posX + 5, centerY, posZ), ModEntities.DUNGEON_BEAST.get());
            placeSpawner(level, new BlockPos(posX - 5, centerY, posZ), ModEntities.DUNGEON_BEAST.get());
            placeSpawner(level, new BlockPos(posX, centerY - 5, posZ), ModEntities.DUNGEON_BEAST.get());
            placeSpawner(level, new BlockPos(posX, centerY + 5, posZ), ModEntities.DUNGEON_BEAST.get());

            safeSetBlock(level, posX + 1, centerY, posZ, coal);
            safeSetBlock(level, posX - 1, centerY, posZ, coal);
            safeSetBlock(level, posX, centerY + 1, posZ, coal);
            safeSetBlock(level, posX, centerY - 1, posZ, coal);

            BlockPos chestPos = new BlockPos(posX, centerY, posZ);
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
                fillRoundRotatorChest(container, random);
            }

            recentlyPlaced = 50;
            return true;
        }
        return false;
    }

    // =====================================================================
    // CRYSTAL BATTLE TOWER - 1/280 chance
    // Original: radius=10, 22 blocks + 2 crystal rim, 5 floors
    // =====================================================================

    private static boolean tryPlaceCrystalBattleTower(WorldGenLevel level, RandomSource random,
                                                       int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(280) != 0) return false;

        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        for (int posY = 100; posY > 50; posY--) {
            if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                    || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                continue;

            buildCrystalBattleTower(level, random, posX, posY, posZ);
            recentlyPlaced = 50;
            return true;
        }
        return false;
    }

    private static void buildCrystalBattleTower(WorldGenLevel level, RandomSource random,
                                                  int cx, int cy, int cz) {
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        BlockState crystal = ModBlocks.CRYSTAL_CRYSTAL.get().defaultBlockState();
        float radius = 10.0f;

        for (int j = 0; j <= 20; j++) {
            if (j % 5 == 0) {
                for (float currad = 0.0f; currad < radius; currad += 0.33f) {
                    for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                        float curx = (float)(currad * Math.cos(Math.toRadians(curdeg)));
                        float curz = (float)(currad * Math.sin(Math.toRadians(curdeg)));
                        safeSetBlock(level,
                                (int)(cx + curx + 0.5f), cy + j,
                                (int)(cz + curz + 0.5f), crystalStone);
                    }
                }
            } else {
                float currad2 = 10.0f;
                for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                    float curx = (float)(currad2 * Math.cos(Math.toRadians(curdeg)));
                    float curz = (float)(currad2 * Math.sin(Math.toRadians(curdeg)));
                    BlockState blk = crystalStone;
                    if (j % 5 >= 1 && j % 5 <= 3 && (curdeg < 10.0f || curdeg > 350.0f)) {
                        blk = Blocks.AIR.defaultBlockState();
                    }
                    safeSetBlock(level,
                            (int)(cx + curx + 0.5f), cy + j,
                            (int)(cz + curz + 0.5f), blk);
                }
            }
        }

        for (int j = 21; j <= 22; j++) {
            float currad2 = 10.0f;
            for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                float curx = (float)(currad2 * Math.cos(Math.toRadians(curdeg)));
                float curz = (float)(currad2 * Math.sin(Math.toRadians(curdeg)));
                safeSetBlock(level,
                        (int)(cx + curx + 0.5f), cy + j,
                        (int)(cz + curz + 0.5f), crystal);
            }
        }

        placeBattleTowerFloor(level, random, cx, cy, cz, 1,
                ModEntities.ENTITY_RAT.get(), CrystalLoot.BATTLE_TOWER_RAT);
        placeBattleTowerFloor(level, random, cx, cy, cz, 6,
                ModEntities.DUNGEON_BEAST.get(), CrystalLoot.BATTLE_TOWER_DUNGEON_BEAST);
        placeBattleTowerFloor(level, random, cx, cy, cz, 11,
                ModEntities.URCHIN.get(), CrystalLoot.BATTLE_TOWER_URCHIN);
        placeBattleTowerFloor(level, random, cx, cy, cz, 16,
                ModEntities.ENTITY_ROTATOR.get(), CrystalLoot.BATTLE_TOWER_ROTATOR);
        placeBattleTowerFloor(level, random, cx, cy, cz, 21,
                ModEntities.ENTITY_VORTEX.get(), CrystalLoot.BATTLE_TOWER_VORTEX);
    }

    private static void placeBattleTowerFloor(WorldGenLevel level, RandomSource random,
                                               int cx, int cy, int cz, int floorY,
                                               EntityType<?> mob, CrystalLoot lootType) {
        placeSpawner(level, new BlockPos(cx, cy + floorY + 1, cz), mob);
        placeSpawner(level, new BlockPos(cx, cy + floorY + 2, cz), mob);

        BlockPos chestPos = new BlockPos(cx, cy + floorY, cz);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
            fillBattleTowerChest(container, random, lootType);
        }
    }

    // =====================================================================
    // IRUKANDJI SPAWNER - 1/80 chance, placed on water
    // =====================================================================

    private static void placeIrukandjiSpawner(WorldGenLevel level, RandomSource random,
                                               int chunkX, int chunkZ) {
        if (random.nextInt(80) != 0) return;

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()) continue;
                if (!level.getBlockState(new BlockPos(posX, posY - 1, posZ)).is(Blocks.WATER)) continue;

                placeSpawner(level, new BlockPos(posX, posY, posZ), ModEntities.IRUKANDJI.get());
                return;
            }
        }
    }

    // =====================================================================
    // CRYSTAL TERMITE BLOCKS - 1/40 chance
    // =====================================================================

    private static void placeCrystalTermiteBlocks(WorldGenLevel level, RandomSource random,
                                                    int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(40) != 0) return;

        BlockState termiteBlock = ModBlocks.CRYSTAL_TERMITE_BLOCK.get().defaultBlockState();
        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                BlockPos pos = new BlockPos(posX, posY, posZ);
                BlockPos below = new BlockPos(posX, posY - 1, posZ);
                if (level.getBlockState(pos).isAir() && level.getBlockState(below).equals(grassState)) {
                    level.setBlock(below, termiteBlock, 2);
                    break;
                }
            }
        }
    }

    // =====================================================================
    // MAZE CHESTS AND SPAWNERS at Y=25
    // =====================================================================

    private static void placeMazeChestsAndSpawners(WorldGenLevel level, RandomSource random,
                                                     int chunkX, int chunkZ) {
        for (int i = 0; i < 3; i++) {
            int posX = 1 + chunkX + random.nextInt(14);
            int posY = 25;
            int posZ = 1 + chunkZ + random.nextInt(14);

            if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()) continue;

            boolean hasAdjWall = false;
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dir : dirs) {
                if (!level.getBlockState(new BlockPos(posX + dir[0], posY, posZ + dir[1])).isAir()) {
                    hasAdjWall = true;
                    break;
                }
            }
            if (!hasAdjWall) continue;

            int choice = random.nextInt(3);
            if (choice == 0) {
                BlockPos chestPos = new BlockPos(posX, posY, posZ);
                level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
                if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
                    fillCrystalChest(container, random);
                }
            } else {
                EntityType<?> mob = random.nextInt(2) == 0
                        ? ModEntities.DUNGEON_BEAST.get()
                        : ModEntities.ENTITY_RAT.get();
                placeSpawner(level, new BlockPos(posX, posY, posZ), mob);
            }
            break;
        }
    }

    // =====================================================================
    // ROCK ENTITY SPAWNING
    // Original: 25% chance to call, then 20% pass inside = 5% effective rate
    // =====================================================================

    private static void addRocks(WorldGenLevel level, RandomSource random,
                                  int chunkX, int chunkZ, BlockState grassState) {
        if (random.nextInt(5) != 0) return;

        int howmany = 3 + random.nextInt(10);
        for (int i = 0; i < howmany; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 110; posY > 40; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()) continue;
                BlockState below = level.getBlockState(new BlockPos(posX, posY - 1, posZ));
                if (!below.equals(grassState) && !below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.SAND))
                    continue;
                try {
                    RockBase rock = new RockBase(ModEntities.ROCK_BASE.get(), level.getLevel());
                    rock.setPos(posX + 0.5, posY, posZ + 0.5);
                    level.addFreshEntityWithPassengers(rock);
                } catch (Exception ignored) {
                }
                break;
            }
        }
    }

    // =====================================================================
    // UTILITIES
    // =====================================================================

    /**
     * Places a vanilla spawner at {@code pos} and configures it to spawn {@code mobType}.
     * <p>We pass {@code null} as the Level argument to {@code setEntityId} because a
     * real ServerLevel here triggers synchronous chunk loads during worldgen and
     * freezes the server thread. Passing null skips that lookup and simply stamps
     * the spawn entity id into the block entity NBT.</p>
     */
    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mobType) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mobType, null, level.getRandom(), pos);
        }
    }

    /**
     * Bounds-checked {@link WorldGenLevel#setBlock} wrapper. Silently drops writes
     * outside the world's build height so large circular structures (Battle Tower,
     * Round Rotator) never throw when their perimeter extends past Y=320/Y=-64.
     */
    private static void safeSetBlock(WorldGenLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (y >= level.getMinBuildHeight() && y < level.getMaxBuildHeight()) {
            level.setBlock(pos, state, 2);
        }
    }

    // =====================================================================
    // CRYSTAL-SPECIFIC CHEST LOOT
    // =====================================================================

    enum CrystalLoot {
        BATTLE_TOWER_RAT,
        BATTLE_TOWER_DUNGEON_BEAST,
        BATTLE_TOWER_URCHIN,
        BATTLE_TOWER_ROTATOR,
        BATTLE_TOWER_VORTEX
    }

    /**
     * General crystal dimension chest loot - based on Trees.CrystalChestContentsList.
     * Contains crystal blocks, tools, armor, eggs, food, and special items.
     */
    private static void fillCrystalChest(Container container, RandomSource random) {
        int count = 1 + random.nextInt(5);
        for (int i = 0; i < count && i < container.getContainerSize(); i++) {
            ItemStack stack = pickCrystalItem(random);
            if (!stack.isEmpty()) {
                container.setItem(i, stack);
            }
        }
    }

    private static ItemStack pickCrystalItem(RandomSource random) {
        int roll = random.nextInt(30);
        return switch (roll) {
            case 0 -> new ItemStack(ModBlocks.CRYSTAL_TERMITE_BLOCK.get(), 1 + random.nextInt(5));
            case 1 -> new ItemStack(ModBlocks.CRYSTAL_FLOWER_RED.get(), 1 + random.nextInt(10));
            case 2 -> new ItemStack(ModBlocks.CRYSTAL_FLOWER_BLUE.get(), 1 + random.nextInt(10));
            case 3 -> new ItemStack(ModBlocks.CRYSTAL_FLOWER_GREEN.get(), 1 + random.nextInt(10));
            case 4 -> new ItemStack(ModBlocks.CRYSTAL_FLOWER_YELLOW.get(), 1 + random.nextInt(10));
            case 5 -> new ItemStack(ModBlocks.CRYSTAL_PLANKS.get(), 1 + random.nextInt(10));
            case 6 -> new ItemStack(ModBlocks.CRYSTAL_WORKBENCH.get(), 1);
            case 7 -> new ItemStack(ModBlocks.CRYSTAL_FURNACE.get(), 1);
            case 8 -> new ItemStack(ModBlocks.CRYSTAL_STONE.get(), 1 + random.nextInt(10));
            case 9 -> new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 1 + random.nextInt(10));
            case 10 -> new ItemStack(ModBlocks.CRYSTAL_TORCH.get(), 1 + random.nextInt(10));
            case 11 -> new ItemStack(ModItems.CRYSTAL_WOOD_SWORD.get());
            case 12 -> new ItemStack(ModItems.CRYSTAL_WOOD_PICKAXE.get());
            case 13 -> new ItemStack(ModItems.CRYSTAL_WOOD_AXE.get());
            case 14 -> new ItemStack(ModItems.CRYSTAL_STONE_SWORD.get());
            case 15 -> new ItemStack(ModItems.CRYSTAL_STONE_PICKAXE.get());
            case 16 -> new ItemStack(ModItems.CRYSTAL_STONE_AXE.get());
            case 17 -> new ItemStack(ModItems.CRYSTAL_PINK_SWORD.get());
            case 18 -> new ItemStack(ModItems.CRYSTAL_PINK_PICKAXE.get());
            case 19 -> new ItemStack(ModItems.CRYSTAL_PINK_AXE.get());
            case 20 -> new ItemStack(ModItems.CRYSTAL_PINK_INGOT.get(), 1 + random.nextInt(5));
            case 21 -> new ItemStack(ModItems.CRYSTAL_APPLE.get(), 1 + random.nextInt(5));
            case 22 -> new ItemStack(ModItems.RAW_PEACOCK.get(), 1 + random.nextInt(10));
            case 23 -> new ItemStack(ModItems.RICE.get(), 1 + random.nextInt(10));
            case 24 -> new ItemStack(ModItems.QUINOA.get(), 1 + random.nextInt(10));
            case 25 -> new ItemStack(ModItems.PEACOCK_FEATHER.get(), 1 + random.nextInt(5));
            case 26 -> new ItemStack(ModItems.TIGERS_EYE_INGOT.get(), 1 + random.nextInt(5));
            case 27 -> new ItemStack(ModItems.TIGERS_EYE_SWORD.get());
            case 28 -> new ItemStack(ModItems.TIGERS_EYE_PICKAXE.get());
            case 29 -> new ItemStack(ModItems.IRUKANDJI_ARROW.get(), 5 + random.nextInt(6));
            default -> ItemStack.EMPTY;
        };
    }

    /**
     * Haunted House chest: iron ingot, raw peacock, crystal torches,
     * crystal coal, golden apples, saddle, crystal pink tools, kraken repellent.
     */
    private static void fillHauntedHouseChest(Container container, RandomSource random) {
        if (random.nextInt(2) == 0)
            container.setItem(0, new ItemStack(Items.IRON_INGOT));
        if (random.nextInt(3) != 0)
            container.setItem(2, new ItemStack(ModItems.RAW_PEACOCK.get(), 8));
        if (random.nextInt(3) != 0)
            container.setItem(3, new ItemStack(ModBlocks.CRYSTAL_TORCH.get(), 32));
        if (random.nextInt(2) == 0)
            container.setItem(4, new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 16));
        if (random.nextInt(2) == 0)
            container.setItem(5, new ItemStack(Items.GOLDEN_APPLE));
        if (random.nextInt(2) == 0)
            container.setItem(6, new ItemStack(Items.GOLDEN_APPLE));
        if (random.nextInt(2) == 0)
            container.setItem(7, new ItemStack(Items.SADDLE));
        if (random.nextInt(2) == 0)
            container.setItem(8, new ItemStack(ModItems.CRYSTAL_PINK_PICKAXE.get()));
        if (random.nextInt(2) == 0)
            container.setItem(9, new ItemStack(ModItems.CRYSTAL_PINK_SWORD.get()));
        if (random.nextInt(2) == 0)
            container.setItem(10, new ItemStack(ModItems.CRYSTAL_PINK_AXE.get()));
        container.setItem(11, new ItemStack(ModItems.KRAKEN_REPELLENT_ITEM.get()));
        if (random.nextInt(2) == 0)
            container.setItem(13, new ItemStack(Items.CHEST));
    }

    /**
     * Round Rotator chest: crystal coal, tiger's eye sword, tiger's eye block,
     * poison sword (same loot as BattleTowerVortex in original).
     */
    private static void fillRoundRotatorChest(Container container, RandomSource random) {
        fillBattleTowerChest(container, random, CrystalLoot.BATTLE_TOWER_VORTEX);
    }

    /**
     * Battle Tower chests - per-floor loot based on original GenericDungeon.
     */
    private static void fillBattleTowerChest(Container container, RandomSource random,
                                              CrystalLoot tier) {
        switch (tier) {
            case BATTLE_TOWER_RAT -> {
                int count = 5 + random.nextInt(5);
                for (int i = 0; i < count && i < container.getContainerSize(); i++) {
                    container.setItem(i, pickRatLoot(random));
                }
            }
            case BATTLE_TOWER_DUNGEON_BEAST -> {
                int count = 5 + random.nextInt(5);
                for (int i = 0; i < count && i < container.getContainerSize(); i++) {
                    container.setItem(i, pickDungeonBeastLoot(random));
                }
            }
            case BATTLE_TOWER_URCHIN -> {
                int count = 3 + random.nextInt(3);
                for (int i = 0; i < count && i < container.getContainerSize(); i++) {
                    container.setItem(i, pickUrchinLoot(random));
                }
            }
            case BATTLE_TOWER_ROTATOR -> {
                int count = 3 + random.nextInt(3);
                for (int i = 0; i < count && i < container.getContainerSize(); i++) {
                    container.setItem(i, pickRotatorLoot(random));
                }
            }
            case BATTLE_TOWER_VORTEX -> {
                int count = 6 + random.nextInt(6);
                for (int i = 0; i < count && i < container.getContainerSize(); i++) {
                    container.setItem(i, pickVortexLoot(random));
                }
            }
        }
    }

    private static ItemStack pickRatLoot(RandomSource random) {
        return switch (random.nextInt(7)) {
            case 0 -> new ItemStack(Items.IRON_SWORD, 1);
            case 1 -> new ItemStack(Items.IRON_PICKAXE, 1);
            case 2 -> new ItemStack(Items.IRON_AXE, 1);
            case 3 -> new ItemStack(Items.IRON_HELMET, 1);
            case 4 -> new ItemStack(ModItems.BLT_SANDWICH.get(), 4 + random.nextInt(7));
            case 5 -> new ItemStack(ModItems.SALAD.get(), 4 + random.nextInt(7));
            case 6 -> new ItemStack(ModItems.CORN_DOG.get(), 4 + random.nextInt(7));
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack pickDungeonBeastLoot(RandomSource random) {
        return switch (random.nextInt(4)) {
            case 0 -> new ItemStack(Items.DIAMOND, 6 + random.nextInt(11));
            case 1 -> new ItemStack(ModItems.SQUID_ZOOKA.get());
            case 2 -> new ItemStack(Items.EMERALD, 5 + random.nextInt(11));
            case 3 -> new ItemStack(Items.DIAMOND_PICKAXE);
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack pickUrchinLoot(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0 -> new ItemStack(ModItems.PINK_HELMET.get());
            case 1 -> new ItemStack(ModItems.PINK_CHESTPLATE.get());
            case 2 -> new ItemStack(ModItems.PINK_LEGGINGS.get());
            case 3 -> new ItemStack(ModItems.PINK_BOOTS.get());
            case 4 -> new ItemStack(ModItems.FAIRY_SWORD.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack pickRotatorLoot(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0 -> new ItemStack(ModItems.TIGERSEYE_HELMET.get());
            case 1 -> new ItemStack(ModItems.TIGERSEYE_CHESTPLATE.get());
            case 2 -> new ItemStack(ModItems.TIGERSEYE_LEGGINGS.get());
            case 3 -> new ItemStack(ModItems.TIGERSEYE_BOOTS.get());
            case 4 -> new ItemStack(ModItems.RAT_SWORD.get());
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack pickVortexLoot(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0, 1 -> new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 6 + random.nextInt(5));
            case 2 -> new ItemStack(ModItems.TIGERS_EYE_SWORD.get());
            case 3 -> new ItemStack(ModBlocks.BLOCK_TIGERS_EYE.get(), 4 + random.nextInt(5));
            case 4 -> new ItemStack(ModItems.POISON_SWORD.get());
            default -> ItemStack.EMPTY;
        };
    }
}
