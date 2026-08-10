package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.RockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

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
 *   <li>The original's {@code WeightedRandomChestContent} arrays are transcribed
 *       to data-driven loot tables ({@code data/orespawn/loot_table/chests/
 *       crystal_chest*.json}, {@code battle_tower_*.json}) referenced via
 *       {@link RandomizableContainerBlockEntity#setLootTable}; chests the
 *       original filled with FIXED slot contents (Rotator Station, Urchin
 *       Spawner, Haunted House) keep in-code {@link ItemStack} fills matching
 *       the original slot-by-slot.</li>
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

    /** Fairy tree / castle / maze loot (orig Trees.CrystalChestContentsList, Trees.java:19). */
    private static final ResourceKey<LootTable> CRYSTAL_CHEST_LOOT = chestLoot("crystal_chest");
    /** Same pool with the maze chest's smaller 1-3 roll count (orig OreSpawnWorld.java:1762). */
    private static final ResourceKey<LootTable> CRYSTAL_CHEST_MAZE_LOOT = chestLoot("crystal_chest_maze");
    private static final ResourceKey<LootTable> BATTLE_TOWER_RAT_LOOT = chestLoot("battle_tower_rat");
    private static final ResourceKey<LootTable> BATTLE_TOWER_DUNGEON_BEAST_LOOT = chestLoot("battle_tower_dungeon_beast");
    private static final ResourceKey<LootTable> BATTLE_TOWER_URCHIN_LOOT = chestLoot("battle_tower_urchin");
    private static final ResourceKey<LootTable> BATTLE_TOWER_ROTATOR_LOOT = chestLoot("battle_tower_rotator");
    private static final ResourceKey<LootTable> BATTLE_TOWER_VORTEX_LOOT = chestLoot("battle_tower_vortex");

    private static ResourceKey<LootTable> chestLoot(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("orespawn", "chests/" + name));
    }

    /**
     * Decorates one crystal-dimension chunk with structures, mirroring the D5
     * dispatch in {@code OreSpawnWorld.generateSurface}
     * (orig OreSpawnWorld.java:187-202): a successful fairy-tree roll skips the
     * termites and the big-structure block, but maze chests and rocks ALWAYS run;
     * the Irukandji spawner rolls whenever the cooldown gate was open, regardless
     * of whether a big structure landed.
     *
     * <p>{@code placementCooldown} is the anti-clustering counter ({@code
     * recently_placed}, orig OreSpawnWorld.java:30/37-38). It is owned by the
     * calling {@link OreSpawnChunkGenerator} instance (one per dimension) rather
     * than being a static field here, so structure placement in one dimension can
     * never suppress placement in another (BUG-013). It is an
     * {@link java.util.concurrent.atomic.AtomicInteger} because the decoration
     * phase runs on a worker pool that processes several chunks in parallel.</p>
     */
    public static void generate(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ,
                                java.util.concurrent.atomic.AtomicInteger placementCooldown) {
        // orig OreSpawnWorld.java:37-38 — decrement once per chunk before any attempt.
        placementCooldown.updateAndGet(v -> v > 0 ? v - 1 : 0);

        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();

        // Each successful large-structure placement arms the 50-chunk cooldown
        // (recently_placed = 50 on the helpers' success paths, e.g. orig
        // OreSpawnWorld.java:1621/1641/1661/1678/1695/1992); hoisted here so the
        // counter can be per-generator instead of static (BUG-013).
        FairyTreeResult fairyResult = tryPlaceFairyTree(level, random, chunkX, chunkZ, crystalGrass);
        if (fairyResult == FairyTreeResult.BUILT) {
            // orig OSW:1992 — recently_placed = 50 ONLY on the build path.
            placementCooldown.set(50);
        } else if (fairyResult == FairyTreeResult.SUPPRESS) {
            // orig OSW:1994-1995 — scan exhaustion suppresses this chunk's
            // follow-ups without arming the cooldown (WGEN-062).
        } else {
            // orig OreSpawnWorld.java:189 — termites only when the fairy roll failed
            placeCrystalTermiteBlocks(level, random, chunkX, chunkZ, crystalGrass);

            if (placementCooldown.get() == 0) {
                // orig OreSpawnWorld.java:191-193 — battle tower only if none of the
                // first four landed; Irukandji rolls afterwards either way (:194).
                boolean placed = tryPlaceRotatorStation(level, random, chunkX, chunkZ, crystalGrass)
                        || tryPlaceUrchinSpawner(level, random, chunkX, chunkZ, crystalGrass)
                        || tryPlaceCrystalHauntedHouse(level, random, chunkX, chunkZ, crystalGrass)
                        || tryPlaceRoundRotator(level, random, chunkX, chunkZ, crystalGrass);
                if (!placed) {
                    placed = tryPlaceCrystalBattleTower(level, random, chunkX, chunkZ, crystalGrass);
                }
                if (placed) {
                    placementCooldown.set(50);
                }

                placeIrukandjiSpawner(level, random, chunkX, chunkZ);
            }
        }

        // orig OreSpawnWorld.java:197-200 — these run even when a fairy tree placed
        placeMazeChestsAndSpawners(level, random, chunkX, chunkZ);

        if (random.nextInt(4) == 1) {
            addRocks(level, random, chunkX, chunkZ, crystalGrass);
        }
    }

    // =====================================================================
    // FAIRY TREE - faithful port from Trees.FairyTree / Trees.FairyCastleTree
    //
    // Both builders serve TWO trigger paths, exactly like the original shared
    // one OreSpawnMain.OreSpawnTrees instance between OreSpawnWorld (crystal
    // worldgen, orig OreSpawnWorld.java:1987-1991) and DungeonSpawnerBlock
    // (player-placed Random Dungeon Spawner, orig DungeonSpawnerBlock.java:53-58):
    //   1. worldgen  — tryPlaceFairyTree below, decoration phase, reads legal;
    //   2. play time — buildFairyTreeAt / buildFairyCastleTreeAt below, called
    //      from RandomDungeonSpawnerBlockEntity.buildForType on a live
    //      ServerLevel tick (types 0/1 of the nextInt(50) roll), reads legal.
    // The shared private builders are typed on WorldGenLevel, which a
    // ServerLevel satisfies.
    // =====================================================================

    /**
     * Random Dungeon Spawner outcome type 0 (orig DungeonSpawnerBlock.java:53-55):
     * {@code OreSpawnTrees.FairyTree(world, x, y, z)} at the (already-cleared)
     * spawner-block position — no {@code posY-1} adjustment; that asymmetry
     * belongs to the worldgen trigger only (orig OreSpawnWorld.java:1988).
     * The original FairyTree has NO ground gate (callers pre-validate, the DSB
     * doesn't), so this can legally conjure a floating/embedded tree — faithful.
     *
     * @return always {@code true} — the original builds unconditionally
     */
    public static boolean buildFairyTreeAt(ServerLevel level, RandomSource random, BlockPos pos) {
        buildFairyTree(level, random, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    /**
     * Random Dungeon Spawner outcome type 1 (orig DungeonSpawnerBlock.java:56-58):
     * {@code OreSpawnTrees.FairyCastleTree(world, x, y, z)} at the spawner-block
     * position. See {@link #buildFairyTreeAt} for the shared-builder rationale
     * (no ground gate, live-tick reads legal).
     *
     * @return always {@code true} — the original builds unconditionally
     */
    public static boolean buildFairyCastleTreeAt(ServerLevel level, RandomSource random, BlockPos pos) {
        buildFairyCastleTree(level, random, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    /**
     * The 1.7.10 LessLag shrink (orig {@code OreSpawnMain.LessLag} config,
     * OreSpawnMain.java:471; ported as {@link OreSpawnConfig#LESS_LAG}, 0-2):
     * {@code LessLag == 1} subtracts 1, {@code LessLag == 2} subtracts 2.
     * The original applies it to every crystal-branch segment length AFTER the
     * random draw (orig Trees.java:529-534, :543-548, :556-561, :570-575,
     * :584-589) and to the fairy-castle tier count (orig Trees.java:618-624) —
     * the draw itself is never gated, only the resulting length shrinks.
     */
    private static int lessLagShrink(int value) {
        int lessLag = OreSpawnConfig.LESS_LAG.get();
        if (lessLag == 1) --value;
        if (lessLag == 2) value -= 2;
        return value;
    }

    /**
     * Tri-state result of the fairy-tree roll, mirroring the original's TWO
     * distinct true-paths (orig OreSpawnWorld.addFairyTree:1962-1996):
     * {@code BUILT} = a tree was placed (arms recently_placed, OSW:1992);
     * {@code SUPPRESS} = the 1/5 roll passed but the Y-scan found no site —
     * the original still returns true (OSW:1994-1995), suppressing the
     * chunk's termite/big-structure follow-ups WITHOUT arming the cooldown
     * (WGEN-062, cooldown side-effect corrected in D6b batch-1 verification);
     * {@code NONE} = roll failed or an explicit clearance check failed
     * (:1977/:1984) — follow-ups proceed.
     */
    private enum FairyTreeResult { BUILT, SUPPRESS, NONE }

    private static FairyTreeResult tryPlaceFairyTree(WorldGenLevel level, RandomSource random,
                                              int chunkX, int chunkZ, BlockState grassState) {
        int posX = chunkX + 8;
        int posZ = chunkZ + 8;
        if (random.nextInt(5) != 0) return FairyTreeResult.NONE;

        for (int posY = 128; posY > 40; posY--) {
            BlockPos pos = new BlockPos(posX, posY, posZ);
            BlockPos below = new BlockPos(posX, posY - 1, posZ);
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(below).equals(grassState))
                continue;

            for (int i = -8; i <= 8; i++) {
                for (int j = -8; j <= 8; j++) {
                    if (!level.getBlockState(new BlockPos(posX + i, posY, posZ + j)).isAir())
                        return FairyTreeResult.NONE;
                }
            }

            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    if (!level.getBlockState(new BlockPos(posX + i, posY - 1, posZ + j)).equals(grassState))
                        return FairyTreeResult.NONE;
                }
            }

            if (random.nextInt(5) != 1) {
                buildFairyTree(level, random, posX, posY - 1, posZ);
            } else {
                buildFairyCastleTree(level, random, posX, posY, posZ);
            }
            return FairyTreeResult.BUILT;
        }
        // orig OreSpawnWorld.addFairyTree:1968-1995 — when the Y 128→41 scan
        // finds no air-over-CrystalGrass candidate, the original falls
        // through to `return true` (OSW:1994-1995), which per the dispatch
        // (OSW:188-196) suppresses this chunk's termite/big-structure
        // follow-ups — but does NOT arm recently_placed (that happens only on
        // the build path, OSW:1992). Restored as WGEN-062; the cooldown
        // distinction is why this is SUPPRESS, not BUILT.
        return FairyTreeResult.SUPPRESS;
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

        // orig Trees.java:485-489 — crystal chest, 1+nextInt(5) weighted picks
        placeLootChest(level, new BlockPos(x + 2, y + 1, z), CRYSTAL_CHEST_LOOT);
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

        // orig Trees.java:528-534 — seg 1 (rising): 4+nextInt(4), LessLag shrink
        int grow = lessLagShrink(4 + random.nextInt(4));
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i, j, k, log);
            makeCrystalLeaves(level, i, j, k);
            j++;
            i += xdir;
            k += zdir;
        }
        i2 = i;
        k2 = k;

        // orig Trees.java:542-548 — seg 2 (level, main arm): 5+nextInt(5), LessLag shrink
        grow = lessLagShrink(5 + random.nextInt(5));
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i, j, k, log);
            makeCrystalLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
        }

        // orig Trees.java:555-561 — seg 3 (level, fork): 5+nextInt(5), LessLag shrink
        grow = lessLagShrink(5 + random.nextInt(5));
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i2, j, k2, log);
            makeCrystalLeaves(level, i2, j, k2);
            i2 += xxdir;
            k2 += zzdir;
        }

        j--;
        j2 = j;
        // orig Trees.java:569-575 — seg 4 (drooping, main): 4+nextInt(4), LessLag shrink
        grow = lessLagShrink(4 + random.nextInt(4));
        for (int n = 0; n < grow; n++) {
            safeSetBlock(level, i, j, k, log);
            makeCrystalLeaves(level, i, j, k);
            i += xdir;
            k += zdir;
            j += ydir;
        }

        // orig Trees.java:583-589 — seg 5 (drooping, fork): 4+nextInt(4), LessLag shrink
        grow = lessLagShrink(4 + random.nextInt(4));
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
        // orig Trees.java:618-624 — 6 tiers; LessLag==1 → 5, LessLag==2 → 4
        int nc = lessLagShrink(6);
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
            // orig Trees.java:608-614 — crystal chest, 1+nextInt(5) weighted picks
            placeLootChest(level, new BlockPos(x, y + 1, z), CRYSTAL_CHEST_LOOT);
        }
    }

    // =====================================================================
    // ROTATOR STATION - 1/150 chance
    //
    // Like the fairy trees above, the builder serves TWO trigger paths, the
    // same shape the original had (one GenericDungeon.makeRotatorStation
    // reached from OreSpawnWorld.addRotatorStation, orig OreSpawnWorld.java:
    // 1608-1626, and from DungeonSpawnerBlock type 3, orig
    // DungeonSpawnerBlock.java:62-64):
    //   1. worldgen  — tryPlaceRotatorStation below, decoration phase;
    //   2. play time — buildRotatorStationAt below, called from
    //      RandomDungeonSpawnerBlockEntity.buildForType on a live ServerLevel.
    // =====================================================================

    /**
     * Random Dungeon Spawner outcome type 3 (orig DungeonSpawnerBlock.java:62-64):
     * {@code MyDungeon.makeRotatorStation(world, x, y, z)} at the
     * (already-cleared) spawner-block position, unmodified — no ground scan, no
     * config gate, no Y adjustment (orig :46-52 validates nothing before the
     * builder). The column writes start at {@code pos.above(4)} exactly as the
     * original's cposy+4, so a floating or terrain-embedded station is faithful
     * behavior. Same shared-builder pattern as {@link #buildFairyTreeAt}.
     *
     * @return always {@code true} — the original builds unconditionally
     */
    public static boolean buildRotatorStationAt(ServerLevel level, RandomSource random, BlockPos pos) {
        buildRotatorStation(level, random, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    private static boolean tryPlaceRotatorStation(WorldGenLevel level, RandomSource random,
                                                   int chunkX, int chunkZ, BlockState grassState) {
        // orig OreSpawnWorld.java:1612 — 1/150 gate, then 3 attempts (:1615).
        // (RotatorEnable, orig :1609-1611, intentionally not ported — see the
        // class Javadoc; original default is enabled, orig OreSpawnMain.java:6423.)
        if (random.nextInt(150) != 0) return false;

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            // orig OreSpawnWorld.java:1618-1619 — Y 100→51 descending,
            // air above CrystalGrass
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                        || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                    continue;

                // orig OreSpawnWorld.java:1620 — build at the found air block,
                // no offset (makeRotatorStation itself floats the column +4..+8)
                buildRotatorStation(level, random, posX, posY, posZ);
                return true;
            }
        }
        return false;
    }

    /**
     * Port of {@code GenericDungeon.makeRotatorStation} (orig
     * GenericDungeon.java:787-810): a floating 1-wide column starting 4 blocks
     * above the origin — CrystalStone (+4), two "Rotator" spawners (+5, +6),
     * CrystalStone (+7), chest (+8) facing north (orig meta 2 via
     * {@code func_72921_c(..., 2, 3)} ≡ default {@code FACING=NORTH}).
     * Typed on {@link WorldGenLevel} so one transcription serves both the
     * decoration phase and the live-tick DSB path ({@link ServerLevel}
     * satisfies it).
     */
    private static void buildRotatorStation(WorldGenLevel level, RandomSource random,
                                             int posX, int posY, int posZ) {
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();

        // orig GenericDungeon.java:790-801
        level.setBlock(new BlockPos(posX, posY + 4, posZ), crystalStone, 2);
        placeSpawner(level, new BlockPos(posX, posY + 5, posZ), ModEntities.ENTITY_ROTATOR.get());
        placeSpawner(level, new BlockPos(posX, posY + 6, posZ), ModEntities.ENTITY_ROTATOR.get());
        level.setBlock(new BlockPos(posX, posY + 7, posZ), crystalStone, 2);

        // orig GenericDungeon.java:802-809 — FIXED slot fill, not the
        // weighted crystal list: slot 1 Rotator egg 1+nextInt(5), slots
        // 2-3 Crystal Coal 4+nextInt(16) each; slot 0 left empty.
        BlockPos chestPos = new BlockPos(posX, posY + 8, posZ);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
            container.setItem(1, new ItemStack(ModItems.ROTATOR_SPAWN_EGG.get(), 1 + random.nextInt(5)));
            container.setItem(2, new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 4 + random.nextInt(16)));
            container.setItem(3, new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 4 + random.nextInt(16)));
        }
    }

    // =====================================================================
    // URCHIN SPAWNER - 1/180 chance
    // =====================================================================

    private static boolean tryPlaceUrchinSpawner(WorldGenLevel level, RandomSource random,
                                                  int chunkX, int chunkZ, BlockState grassState) {
        // orig OreSpawnWorld.java:1649-1651 — UrchinEnable kill-switch (config
        // default 1/enabled, orig OreSpawnMain.java:6391) returns false BEFORE
        // the 1/180 roll; with urchins disabled no spawner structure may
        // generate. Kept FIRST to match the original contract shape (D6b,
        // urchin_spawner_spec.md §11.1) — the skipped draw only matters for the
        // original's shared Random stream, not the port's decoration RandomSource.
        if (!OreSpawnConfig.URCHIN_ENABLE.get()) return false;
        // orig OreSpawnWorld.java:1652 — 1/180 gate, then 3 attempts (:1655)
        if (random.nextInt(180) != 0) return false;

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                        || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                    continue;

                buildUrchinSpawner(level, random, posX, posY, posZ);
                return true;
            }
        }
        return false;
    }

    /** Port of {@code GenericDungeon.makeUrchinSpawner} (orig GenericDungeon.java:2578-2636). */
    private static void buildUrchinSpawner(WorldGenLevel level, RandomSource random,
                                            int posX, int posY, int posZ) {
        BlockState[] columnBlocks = {
                ModBlocks.CRYSTAL_STONE.get().defaultBlockState(),
                ModBlocks.CRYSTAL_CRYSTAL.get().defaultBlockState(),
                ModBlocks.TIGERS_EYE_ORE.get().defaultBlockState()
        };

        for (int col = 0; col < 3; col++) {
            float dx = random.nextFloat() - random.nextFloat();
            float dz = random.nextFloat() - random.nextFloat();
            float dy = 0.5f + random.nextFloat() / 2.0f;
            int w = random.nextInt(2);
            // orig GenericDungeon.java:2594-2597 — same length formula for all
            // three spikes, halved (integer division) for crystal and tigers-eye
            int length = 10 + w * 3 + random.nextInt(5);
            if (col != 0) length /= 2;
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

        // orig GenericDungeon.java:2628-2635 — FIXED slot fill: slot 1 Urchin egg
        // 1+nextInt(5), slots 2-3 Crystal Coal 4+nextInt(16) each.
        BlockPos chestPos = new BlockPos(posX, posY - 1, posZ);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity container) {
            container.setItem(1, new ItemStack(ModItems.URCHIN_SPAWN_EGG.get(), 1 + random.nextInt(5)));
            container.setItem(2, new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 4 + random.nextInt(16)));
            container.setItem(3, new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 4 + random.nextInt(16)));
        }
    }

    // =====================================================================
    // CRYSTAL HAUNTED HOUSE - 1/230 chance
    // Original: width=3, length=3, height=3 (7x7x4 building)
    // =====================================================================

    /**
     * Random Dungeon Spawner outcome type 25 (orig DungeonSpawnerBlock.java:128-130):
     * {@code MyDungeon.makeCrystalHauntedHouse(world, x, y, z)} at the
     * (already-cleared) spawner-block position, unmodified — no scan, no
     * Y adjustment (orig GenericDungeon.java:2993-3104 anchors on the passed
     * centre and validates nothing). Skips the worldgen
     * {@link #tryPlaceCrystalHauntedHouse} gate/jitter/scan entirely; the
     * slot loot and spawners live inside the shared private builder. Same
     * shared-builder pattern as {@link #buildRotatorStationAt}.
     *
     * @return always {@code true} — the original builds unconditionally
     */
    public static boolean buildCrystalHauntedHouseAt(ServerLevel level, RandomSource random, BlockPos pos) {
        buildCrystalHauntedHouse(level, random, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    private static boolean tryPlaceCrystalHauntedHouse(WorldGenLevel level, RandomSource random,
                                                        int chunkX, int chunkZ, BlockState grassState) {
        // orig OreSpawnWorld.java:1669 — 1/230 gate, then 3 attempts (:1672)
        if (random.nextInt(230) != 0) return false;

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                        || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                    continue;

                buildCrystalHauntedHouse(level, random, posX, posY, posZ);
                return true;
            }
        }
        return false;
    }

    private static void buildCrystalHauntedHouse(WorldGenLevel level, RandomSource random,
                                                   int x, int y, int z) {
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        BlockState crystalPlanks = ModBlocks.CRYSTAL_PLANKS.get().defaultBlockState();
        // orig GenericDungeon.java:3028 — the k==height wall band is GLASS windows
        BlockState glass = Blocks.GLASS.defaultBlockState();
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
                            safeSetBlock(level, x + i, y + k, z + j, glass);
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
    // Original: BEDROCK outer ring, crystal pink inner ring (vertical, X/Y plane)
    // =====================================================================

    /**
     * Random Dungeon Spawner outcome type 45 (orig DungeonSpawnerBlock.java:188-190):
     * {@code MyDungeon.makeRoundRotator(world, x, y + 1, z)} — one of only
     * three {@code clickedY + 1} outliers in the whole DSB table (types
     * 43/44/45). The caller (RandomDungeonSpawnerBlockEntity) passes
     * {@code pos.above()}, mirroring the shipped TYPE_PUMPKIN precedent, so
     * this adapter adds NO offset of its own. No scan, no other adjustment
     * (orig GenericDungeon.java:6184-6258 floats itself +6 internally); the
     * Vortex-list chest lives inside the shared private builder. Same
     * shared-builder pattern as {@link #buildRotatorStationAt}.
     *
     * @return always {@code true} — the original builds unconditionally
     */
    public static boolean buildRoundRotatorAt(ServerLevel level, RandomSource random, BlockPos pos) {
        buildRoundRotator(level, random, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    private static boolean tryPlaceRoundRotator(WorldGenLevel level, RandomSource random,
                                                 int chunkX, int chunkZ, BlockState grassState) {
        // orig OreSpawnWorld.java:1632 — 1/150 gate, then 3 attempts (:1635)
        if (random.nextInt(150) != 0) return false;

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                        || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                    continue;

                buildRoundRotator(level, random, posX, posY, posZ);
                return true;
            }
        }
        return false;
    }

    /** Port of {@code GenericDungeon.makeRoundRotator} (orig GenericDungeon.java:6184-6258). */
    private static void buildRoundRotator(WorldGenLevel level, RandomSource random,
                                           int posX, int posY, int posZ) {
        int centerY = posY + 6;
        // orig GenericDungeon.java:6196 — outer radius-6 ring is BEDROCK
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState pink = ModBlocks.BLOCK_CRYSTAL_PINK.get().defaultBlockState();
        BlockState coal = ModBlocks.CRYSTAL_COAL.get().defaultBlockState();

        for (float deg = 0; deg < 360; deg += 5) {
            double rad = Math.toRadians(deg);
            int bx = (int)(posX + 6 * Math.cos(rad) + 0.5);
            int by = (int)(centerY + 6 * Math.sin(rad) + 0.5);
            safeSetBlock(level, bx, by, posZ, bedrock);
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

        // orig GenericDungeon.java:6253-6257 — Vortex list, 6+nextInt(6) picks
        placeLootChest(level, new BlockPos(posX, centerY, posZ), BATTLE_TOWER_VORTEX_LOOT);
    }

    // =====================================================================
    // CRYSTAL BATTLE TOWER - 1/280 chance
    // Original: radius=10, 22 blocks + 2 crystal rim, 5 floors
    // =====================================================================

    /**
     * Random Dungeon Spawner outcome type 33 (orig DungeonSpawnerBlock.java:152-154):
     * {@code MyDungeon.makeCrystalBattleTower(world, x, y, z)} at the
     * (already-cleared) spawner-block position, unmodified — centre-anchored,
     * base disc at {@code pos.getY()} (orig GenericDungeon.java:4831-4959),
     * no scan, no Y adjustment. NOT the deleted {@code CrystalBattleTowerFeature}
     * — that class was the datapack-dead Phase 13A modernization with a
     * heightmap re-anchor, a solid-footing veto the original never had, and
     * invented per-floor loot (dsb_sweep_spec.md &sect;C.4, flag F4). The five
     * {@code battle_tower_*} loot tables and mob ladder live inside the shared
     * private builder. Same shared-builder pattern as
     * {@link #buildRotatorStationAt}.
     *
     * @return always {@code true} — the original builds unconditionally
     */
    public static boolean buildCrystalBattleTowerAt(ServerLevel level, RandomSource random, BlockPos pos) {
        buildCrystalBattleTower(level, random, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    private static boolean tryPlaceCrystalBattleTower(WorldGenLevel level, RandomSource random,
                                                       int chunkX, int chunkZ, BlockState grassState) {
        // orig OreSpawnWorld.java:1686 — 1/280 gate, then 3 attempts (:1689)
        if (random.nextInt(280) != 0) return false;

        for (int i = 0; i < 3; i++) {
            int posX = chunkX + random.nextInt(16);
            int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; posY--) {
                if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()
                        || !level.getBlockState(new BlockPos(posX, posY - 1, posZ)).equals(grassState))
                    continue;

                buildCrystalBattleTower(level, random, posX, posY, posZ);
                return true;
            }
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

        // Floors per orig GenericDungeon.java:4875-4959 — chest at cy+j, two
        // spawners above; pick counts (5+nextInt(5), Vortex 6+nextInt(6)) are
        // encoded in the loot tables' rolls.
        placeBattleTowerFloor(level, cx, cy, cz, 1,
                ModEntities.ENTITY_RAT.get(), BATTLE_TOWER_RAT_LOOT);
        placeBattleTowerFloor(level, cx, cy, cz, 6,
                ModEntities.DUNGEON_BEAST.get(), BATTLE_TOWER_DUNGEON_BEAST_LOOT);
        placeBattleTowerFloor(level, cx, cy, cz, 11,
                ModEntities.URCHIN.get(), BATTLE_TOWER_URCHIN_LOOT);
        placeBattleTowerFloor(level, cx, cy, cz, 16,
                ModEntities.ENTITY_ROTATOR.get(), BATTLE_TOWER_ROTATOR_LOOT);
        placeBattleTowerFloor(level, cx, cy, cz, 21,
                ModEntities.ENTITY_VORTEX.get(), BATTLE_TOWER_VORTEX_LOOT);
    }

    private static void placeBattleTowerFloor(WorldGenLevel level,
                                               int cx, int cy, int cz, int floorY,
                                               EntityType<?> mob, ResourceKey<LootTable> loot) {
        placeSpawner(level, new BlockPos(cx, cy + floorY + 1, cz), mob);
        placeSpawner(level, new BlockPos(cx, cy + floorY + 2, cz), mob);
        placeLootChest(level, new BlockPos(cx, cy + floorY, cz), loot);
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

    /**
     * Maze-level chests/spawners at Y=25, port of
     * {@code OreSpawnWorld.addCrystalChestsAndSpawners} (orig OreSpawnWorld.java:1724-1753)
     * and {@code addCrystalChest} (:1755-1777). The original requires the picked
     * position to be air and looks for an adjacent AIR block in the fixed order
     * +X, -X, +Z, -Z (the chest's facing followed that direction); if even -Z is
     * solid, it gives up for the whole chunk rather than retrying. Whatever spot
     * passes gets a 1-in-3 chest (maze crystal loot, 1+nextInt(3) picks,
     * :1762) or a spawner, 50/50 Dungeon Beast / Rat (:1768-1774).
     */
    private static void placeMazeChestsAndSpawners(WorldGenLevel level, RandomSource random,
                                                     int chunkX, int chunkZ) {
        for (int i = 0; i < 3; i++) {
            int posX = 1 + chunkX + random.nextInt(14);
            int posY = 25;
            int posZ = 1 + chunkZ + random.nextInt(14);

            // orig :1730 — position itself must be air
            if (!level.getBlockState(new BlockPos(posX, posY, posZ)).isAir()) continue;

            // orig :1733-1750 — first AIR neighbour in +X, -X, +Z, -Z order wins;
            // all-solid means stop entirely (the original's final `break`)
            boolean foundOpenSide = false;
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] dir : dirs) {
                if (level.getBlockState(new BlockPos(posX + dir[0], posY, posZ + dir[1])).isAir()) {
                    foundOpenSide = true;
                    break;
                }
            }
            if (!foundOpenSide) break;

            // orig :1756 — 1/3 chest, else spawner
            if (random.nextInt(3) == 0) {
                placeLootChest(level, new BlockPos(posX, posY, posZ), CRYSTAL_CHEST_MAZE_LOOT);
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

    /**
     * Places a chest whose contents are generated lazily from the given loot
     * table when first opened. The tables under {@code orespawn:chests/}
     * transcribe the original {@code WeightedRandomChestContent} arrays
     * entry-for-entry (weights, counts and pick counts); generation is in
     * tools/gen_loot_tables.py with per-entry citations.
     */
    private static void placeLootChest(WorldGenLevel level, BlockPos pos, ResourceKey<LootTable> loot) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(loot);
        }
    }

    /**
     * Haunted House chest, slot-by-slot per orig GenericDungeon.java:3052-3088:
     * compass (1/2), cooked peacock x8 (2/3), crystal torch x32 (2/3), crystal
     * coal x16 (1/2), two beds (1/2 each), wooden door (1/2), crystal pink
     * pickaxe/sword/axe (1/2 each), guaranteed Kraken Repellent, chest (1/2).
     */
    private static void fillHauntedHouseChest(RandomizableContainerBlockEntity container, RandomSource random) {
        if (random.nextInt(2) == 0)
            container.setItem(0, new ItemStack(Items.COMPASS));
        if (random.nextInt(3) != 0)
            container.setItem(2, new ItemStack(ModItems.COOKED_PEACOCK.get(), 8));
        if (random.nextInt(3) != 0)
            container.setItem(3, new ItemStack(ModBlocks.CRYSTAL_TORCH.get(), 32));
        if (random.nextInt(2) == 0)
            container.setItem(4, new ItemStack(ModBlocks.CRYSTAL_COAL.get(), 16));
        if (random.nextInt(2) == 0)
            container.setItem(5, new ItemStack(Items.RED_BED));
        if (random.nextInt(2) == 0)
            container.setItem(6, new ItemStack(Items.RED_BED));
        if (random.nextInt(2) == 0)
            container.setItem(7, new ItemStack(Items.OAK_DOOR));
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

}
