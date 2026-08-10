package danger.orespawn.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DimensionStyle;
import danger.orespawn.world.OreSpawnChunkGenerator;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import danger.orespawn.world.structure.LegacyDungeonStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Structure-parity GameTests, batch A (testing_session category "structures",
 * first 12 items, K base 600).
 *
 * <p><b>Far-build regions:</b> big structures are built OUTSIDE the test
 * template at {@code helper.absolutePos(0,0,0).offset(K*512, 0, 50000)} with a
 * per-test K so no two tests share chunks. K map: i122=601, i126=602, i127=603,
 * i137=604, i138=605, i139=606, i140=607, i141=608, i142=609, i145=610,
 * i146=611 (i007 runs entirely inside its template). Far asserts use
 * {@code helper.getLevel().getBlockState(...)} on absolute positions, never the
 * template-relative helper asserts.</p>
 *
 * <p><b>Build seams used (main code, already wired):</b>
 * {@link LegacyDungeonPiece#buildNow} (live Dungeon-Spawner-Block path,
 * level-RNG layouts — used for statistical rebuild sampling) and the public
 * {@code LegacyDungeonPiece} constructor + {@code postProcess} (the worldgen
 * path whose per-piece RNG is deterministically seeded from the bounding box,
 * LegacyDungeonPiece.java:465-470 — used where a test must FIND a specific
 * random outcome, e.g. a level-6 Challenge Tower, by predicting the roll from
 * the seed before building).</p>
 *
 * <p><b>Statistical asserts:</b> every sampled bound carries its false-failure
 * math in a comment; all are &lt; 1e-9. Loot pools are exercised through
 * {@code LootTable.getRandomItems(LootParams)} against the server's reloadable
 * registries — the same data path a player-opened chest rolls.</p>
 *
 * <p><b>Citation shorthand:</b> orig = reference_1_7_10_source/sources/danger/
 * orespawn/...; OSW = orig OreSpawnWorld.java; GD = orig GenericDungeon.java;
 * BM = orig BasiliskMaze.java. Port files are src/main/java paths.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructureTestsA {

    private static final int FAR_Z = 50000;

    // ---------------------------------------------------------------- helpers

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    /** Base position of this test's private far-build region (see class doc). */
    private static BlockPos farOrigin(GameTestHelper helper, int k, int y) {
        BlockPos corner = helper.absolutePos(BlockPos.ZERO);
        return new BlockPos(corner.getX() + k * 512, y, corner.getZ() + FAR_Z);
    }

    private static String entityId(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /** Reads the mob id a placed mob-spawner is bound to, via its saved SpawnData NBT. */
    private static String spawnerMob(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawner)) {
            return "<no spawner block entity at " + pos + ">";
        }
        CompoundTag tag = spawner.getSpawner().save(new CompoundTag());
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    private static void assertSpawner(GameTestHelper helper, ServerLevel level, BlockPos pos,
                                      EntityType<?> mob) {
        helper.assertTrue(level.getBlockState(pos).is(Blocks.SPAWNER),
                "expected spawner at " + pos + ", got " + level.getBlockState(pos));
        String actual = spawnerMob(level, pos);
        helper.assertTrue(actual.equals(entityId(mob)),
                "spawner at " + pos + " should spawn " + entityId(mob) + ", got " + actual);
    }

    /** Asserts a chest block bound to the given orespawn loot table, optionally with a facing. */
    private static void assertLootChest(GameTestHelper helper, ServerLevel level, BlockPos pos,
                                        String tablePath, Direction facing) {
        BlockState state = level.getBlockState(pos);
        helper.assertTrue(state.is(Blocks.CHEST), "expected chest at " + pos + ", got " + state);
        if (facing != null) {
            helper.assertTrue(state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing,
                    "chest at " + pos + " should face " + facing
                            + ", got " + state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        BlockEntity be = level.getBlockEntity(pos);
        helper.assertTrue(be instanceof RandomizableContainerBlockEntity,
                "chest block entity missing at " + pos);
        ResourceKey<LootTable> bound = ((RandomizableContainerBlockEntity) be).getLootTable();
        helper.assertTrue(bound != null && bound.location().equals(rl(tablePath)),
                "chest at " + pos + " should bind orespawn:" + tablePath + ", got "
                        + (bound == null ? "null" : bound.location().toString()));
    }

    /**
     * Rolls an orespawn chest loot table n times through the CHEST param set.
     *
     * <p>Triage fix (2026-08-10): rolls via {@code getRandomItemsRaw}, which
     * emits exactly ONE stack per pool roll, so a sample's stack count equals
     * its pick count. The previous {@code getRandomItems} path post-processes
     * through {@code createStackSplitter} (LootTable.java, 1.21.1), which
     * splits any pick whose count reaches the item's max stack size into
     * multiple stacks — e.g. one caged_mob pick of 2-4 (unstackable, BM:28)
     * or one zoo_keeper pick of 10-16 (durability 1 ⇒ unstackable, GD:60)
     * became 2-16 stacks and tripped the roll-count bounds. The originals
     * behaved the same in-chest (1.7.10 ChestGenHooks.generateStacks splits
     * over-limit picks into singles), so the documented 5-10/5-11 ranges are
     * PICK counts, which the raw path measures directly (same idiom as
     * StructureTestsB.checkLootRolls).
     */
    private static List<List<ItemStack>> rollLoot(GameTestHelper helper, String tablePath, int n) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, rl(tablePath)));
        helper.assertTrue(table != LootTable.EMPTY, "loot table orespawn:" + tablePath + " is missing");
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        List<List<ItemStack>> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<ItemStack> roll = new ArrayList<>();
            table.getRandomItemsRaw(params, roll::add);
            out.add(roll);
        }
        return out;
    }

    /**
     * Shared loot-distribution assert: every roll's stack count must sit in
     * [minRolls, maxRolls] with BOTH endpoints observed, every rolled item must
     * be in {@code allowedIds}, and with {@code fullCoverage} every allowed id
     * must be observed at least once. Callers state their n / false-failure
     * math at the call site.
     */
    private static Set<String> assertLootStats(GameTestHelper helper, List<List<ItemStack>> rolls,
                                               int minRolls, int maxRolls, Set<String> allowedIds,
                                               boolean fullCoverage, String label) {
        Set<String> observed = new HashSet<>();
        int seenMin = Integer.MAX_VALUE;
        int seenMax = Integer.MIN_VALUE;
        for (List<ItemStack> roll : rolls) {
            int count = roll.size();
            helper.assertTrue(count >= minRolls && count <= maxRolls,
                    label + ": roll produced " + count + " stacks, expected "
                            + minRolls + "-" + maxRolls);
            seenMin = Math.min(seenMin, count);
            seenMax = Math.max(seenMax, count);
            for (ItemStack stack : roll) {
                String id = itemId(stack);
                helper.assertTrue(allowedIds.contains(id),
                        label + ": rolled undocumented item " + id);
                observed.add(id);
            }
        }
        helper.assertTrue(seenMin == minRolls && seenMax == maxRolls,
                label + ": observed roll-count range " + seenMin + "-" + seenMax
                        + ", documented " + minRolls + "-" + maxRolls);
        if (fullCoverage) {
            helper.assertTrue(observed.size() == allowedIds.size(),
                    label + ": expected all " + allowedIds.size()
                            + " table entries observed, saw " + observed.size());
        }
        return observed;
    }

    /**
     * Worldgen-path build: identical to {@link LegacyDungeonPiece#buildNow}
     * (LegacyDungeonPiece.java:548-553) except the runtime RNG override is not
     * installed, so the piece uses its deterministic box-seeded RandomSource
     * (LegacyDungeonPiece.java:465-470) — the layout is a pure function of the
     * origin, which lets a test PREDICT random outcomes before building.
     */
    private static void buildWorldgenPath(ServerLevel level, BlockPos origin,
                                          LegacyDungeonPiece.DungeonType type) {
        LegacyDungeonPiece piece = new LegacyDungeonPiece(origin, type);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
                level.random, piece.getBoundingBox(), new ChunkPos(origin), origin);
    }

    /**
     * Replica of the worldgen-path level roll for a Challenge Tower at
     * {@code origin}: the piece RNG seeds from the bounding-box corners
     * (LegacyDungeonPiece.java:467-470) and the level roll is the FIRST two
     * draws of generateChallengeTower (LegacyDungeonPiece.java:2255-2266, orig
     * GD:202-205): uniform 1-6, then 1-3 rerolled up by 3 two thirds of the
     * time — P(4)=P(5)=P(6)=5/18.
     */
    private static int predictTowerLevel(BlockPos origin, LegacyDungeonPiece.DungeonType type) {
        LegacyDungeonPiece piece = new LegacyDungeonPiece(origin, type);
        long seed = (long) piece.getBoundingBox().minX() * 341873128712L
                + (long) piece.getBoundingBox().minZ() * 132897987541L;
        RandomSource rng = RandomSource.create(seed);
        int level = 1 + rng.nextInt(6);
        if (level <= 3 && rng.nextInt(3) != 1) {
            level += 3;
        }
        return level;
    }

    /** Structure registry lookup (worldgen/structure/&lt;path&gt;.json). */
    private static Structure registeredStructure(GameTestHelper helper, String path) {
        Registry<Structure> registry = helper.getLevel().getServer().registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        Structure structure = registry.get(rl(path));
        helper.assertTrue(structure != null, "structure orespawn:" + path + " not registered");
        return structure;
    }

    /** Encodes a registered LegacyDungeonStructure back through its own codec. */
    private static JsonObject encodeStructure(GameTestHelper helper, Structure structure) {
        helper.assertTrue(structure instanceof LegacyDungeonStructure,
                "expected a LegacyDungeonStructure, got " + structure.getClass().getName());
        RegistryOps<com.google.gson.JsonElement> ops =
                RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().getServer().registryAccess());
        return LegacyDungeonStructure.CODEC.codec()
                .encodeStart(ops, (LegacyDungeonStructure) structure)
                .getOrThrow().getAsJsonObject();
    }

    /**
     * Builds a {@link Structure.GenerationContext} around an arbitrary chunk
     * generator + random state, mirroring what the structure-placement
     * machinery hands {@code findGenerationPoint}. The height accessor only
     * clamps {@code getBaseHeight} to the generator's own noise settings range,
     * so the (always-loaded) test overworld serves for detached generators.
     */
    private static Structure.GenerationContext contextFor(GameTestHelper helper, ChunkGenerator gen,
                                                          RandomState randomState, long seed,
                                                          ChunkPos chunkPos) {
        ServerLevel level = helper.getLevel();
        return new Structure.GenerationContext(
                level.getServer().registryAccess(), gen, gen.getBiomeSource(), randomState,
                level.getStructureManager(), seed, chunkPos, level, biome -> true);
    }

    /** The detached Islands-dimension chunk generator (data/orespawn/dimension/islands.json). */
    private static OreSpawnChunkGenerator islandsGenerator(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Holder<Biome> islandBiome = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl("island_biome")));
        Holder<NoiseGeneratorSettings> settings =
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
                        .getHolderOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, rl("islands")));
        return new OreSpawnChunkGenerator(new FixedBiomeSource(islandBiome), settings, DimensionStyle.ISLANDS);
    }

    /** RandomState matching {@link #islandsGenerator} at seed 0. */
    private static RandomState islandsRandomState(GameTestHelper helper) {
        return RandomState.create(
                helper.getLevel().getServer().registryAccess().asGetterLookup(),
                ResourceKey.create(Registries.NOISE_SETTINGS, rl("islands")), 0L);
    }

    // =====================================================================
    // i007-item-027 — Duplicator Log: progressive tree growth + 5x5 block copy
    // =====================================================================

    /**
     * Checklist i007-item-027 (ITEM-027, FIX_LOG.md:115): "place a Duplicator
     * Log on dirt/grass with blocks nearby: tree grows block-by-block, then
     * copies ~20 nearby blocks into the 5x5 area".
     *
     * <p>Documented mechanism (port BlockDuplicatorLog.java:57-116, orig
     * Trees.java:121-182 via BlockDuplicatorLog.java:32-39): each random tick
     * performs exactly ONE build step — trunk to 3 logs (orig :140-154), then
     * the apple-leaves cap (orig :155-159), then the 3x3 leaf ring one at a
     * time (orig :160-166), then per tick one copy of a random non-air,
     * non-log block in the 5x5 area at trunk-base level to a random air cell
     * in the same area (orig :167-181). The test drives the steps by invoking
     * {@code BlockState.randomTick} directly (deterministic step count; the
     * config gate at BlockDuplicatorLog.java:49 is asserted enabled and
     * restored). "~20 copied blocks": the 5x5 ring holds 25 cells − 1 log − 3
     * seeded markers = 21 fillable cells; 400 copy ticks fill essentially all
     * of them (each tick's 20+20 source/destination tries succeed with
     * probability &gt; 0.5 even at one remaining air cell, so P(&lt;15 copies
     * after 400 ticks) is astronomically small — a binomial tail beyond 1e-40).</p>
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void i007_duplicator_log_progressive_growth_and_copy(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        boolean oldGate = OreSpawnConfig.DUPLICATOR_TREE_ENABLE.get();
        OreSpawnConfig.DUPLICATOR_TREE_ENABLE.set(true); // orig gate enableduplicatortree, BlockDuplicatorLog.java:49
        try {
            // Soil + log + three gold markers inside the 5x5 copy area at log level.
            helper.setBlock(3, 1, 3, Blocks.GRASS_BLOCK);
            helper.setBlock(3, 2, 3, ModBlocks.DUPLICATOR_LOG.get());
            helper.setBlock(1, 2, 1, Blocks.GOLD_BLOCK);
            helper.setBlock(5, 2, 5, Blocks.GOLD_BLOCK);
            helper.setBlock(1, 2, 5, Blocks.GOLD_BLOCK);

            BlockPos log = helper.absolutePos(new BlockPos(3, 2, 3));
            Runnable step = () -> level.getBlockState(log).randomTick(level, log, level.random);

            // Step 1: second trunk log only (orig Trees.java:140-154 — one block per tick).
            step.run();
            helper.assertBlockPresent(ModBlocks.DUPLICATOR_LOG.get(), new BlockPos(3, 3, 3));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 4, 3));
            // Step 2: third trunk log.
            step.run();
            helper.assertBlockPresent(ModBlocks.DUPLICATOR_LOG.get(), new BlockPos(3, 4, 3));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 5, 3));
            // Step 3: apple-leaves cap (orig Trees.java:155-159).
            step.run();
            helper.assertBlockPresent(ModBlocks.APPLE_LEAVES.get(), new BlockPos(3, 5, 3));

            // Steps 4-11: the 8 ring leaves, one per tick (orig Trees.java:160-166).
            for (int placed = 1; placed <= 8; placed++) {
                step.run();
                int ringLeaves = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue;
                        BlockPos ring = helper.absolutePos(new BlockPos(3 + i, 4, 3 + j));
                        if (level.getBlockState(ring).is(ModBlocks.APPLE_LEAVES.get())) ringLeaves++;
                    }
                }
                helper.assertTrue(ringLeaves == placed,
                        "block-by-block growth: expected " + placed + " ring leaves, got " + ringLeaves);
            }

            // Copy phase (orig Trees.java:167-181): 400 ticks, then count copies.
            for (int i = 0; i < 400; i++) {
                step.run();
            }
            int gold = 0;
            for (int i = 1; i <= 5; i++) {
                for (int j = 1; j <= 5; j++) {
                    BlockPos cell = helper.absolutePos(new BlockPos(i, 2, j));
                    BlockState state = level.getBlockState(cell);
                    if (state.is(Blocks.GOLD_BLOCK)) {
                        gold++;
                    } else {
                        // Only the log itself and (unfilled) air may remain — copies
                        // reproduce the source block, never invent others.
                        helper.assertTrue(state.isAir() || state.is(ModBlocks.DUPLICATOR_LOG.get()),
                                "unexpected block in copy area at " + cell + ": " + state);
                    }
                }
            }
            // 3 markers + >=15 copies (~20 documented, steady state 21 copies).
            helper.assertTrue(gold >= 18,
                    "expected >=15 copied blocks (~20 documented, ITEM-027) in the 5x5 area, got "
                            + (gold - 3));
        } finally {
            OreSpawnConfig.DUPLICATOR_TREE_ENABLE.set(oldGate);
        }
        helper.succeed();
    }

    // =====================================================================
    // i122-wgen-037 — Basilisk Maze content + Mining-dimension sink anchor
    // =====================================================================

    /**
     * Checklist i122-wgen-037 (WGEN-037, FIX_LOG.md:777). Far region K=601.
     *
     * <p>Layout source: port BasiliskMazeGenerator.java (line-by-line from orig
     * BasiliskMaze.java:30-458); spec phase_d_reports/d5_extraction/
     * basilisk_maze_spec.md. Build origin O; depth D = 20 + nextInt(10)
     * (BM:31); castle base CB = (O.x+3, O.y−D−4, O.z−20) (generator lines
     * 102-107). Asserts:</p>
     * <ul>
     *   <li>sandstone step pyramid (BM:417-424) + 4x4 bedrock shaft with 2x2
     *       core and one spiral obsidian step per layer (BM:425-457), shaft
     *       depth D in 20-29;</li>
     *   <li>iron-ore-walled antechamber with 3 Extreme Torches
     *       (BM:358-372, :382-384);</li>
     *   <li>30x30 obsidian maze, walls 3 high, 2-wide corridors, solvable
     *       west-to-east via BFS over the placed blocks (BM:39-145, openMaze
     *       BM:278-297);</li>
     *   <li>80-draw lava trap field under the maze half and 20-draw RTP trap
     *       field under the chamber half (BM:308-313): distinct-cell counts of
     *       uniform draws — E[lava]≈76 of 784 cells, P(&lt;40) and E[rtp]≈19.8
     *       of 784, P(&lt;10) are collision-tail events far below 1e-9;</li>
     *   <li>3 persistent Basilisks in the chamber (BM:397-410 +
     *       setPersistenceRequired via spawnPersistent,
     *       LegacyDungeonPiece.java:620-655);</li>
     *   <li>2-4 east-wall chests, torch 3 above each, bound to the 31-entry
     *       orespawn:chests/basilisk_maze table (BM:388-396, :28, :395), and
     *       the table itself rolls 5-10 stacks with diamonds at 15-25
     *       (chests/basilisk_maze.json transcribing BM:28; n=600 rolls: both
     *       roll-count endpoints P(miss) ≤ 2·(5/6)^600 ≈ 3e-48; full 31-entry
     *       coverage: worst weight 5/495 over ≥3000 stacks → miss ≤
     *       31·e^(−3000·5/495) ≈ 2e-12);</li>
     *   <li>the Mining-dimension anchor sinks the origin 2 blocks into the
     *       lowest-of-36-columns surface (PlacementMode.LOWEST_SURFACE_36,
     *       LegacyDungeonStructure.java:180-210, OSW:2573-2597): the scan is
     *       RNG-free, so the test replicates it against the real Mining
     *       generator (dimension/mining.json: orespawn:inland noise, MINING
     *       style) and asserts findGenerationPoint returns exactly
     *       lowest−2.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i122_basilisk_maze_content_and_sink(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = farOrigin(helper, 601, 30);

        // TF-023 harness race (proven fix pattern: StructureTestsC red-ant
        // i165, FIX_LOG TF-023 CLOSED): freshly force-loaded chunks keep their
        // entity sections HIDDEN until the queued FullChunkStatus promotion
        // pumps on the main thread, so an entity query against a just-built
        // far region can miss mobs that ARE in section storage. Pin the maze
        // footprint under a FORCED ticket and sync-load it a few ticks BEFORE
        // the build; buildNow then writes into already-promoted chunks and the
        // Basilisk spawns land in TRACKED (queryable) sections. Footprint XZ
        // (D only affects Y): pyramid o±8; castle base cb = o+(3,-D-4,-20)
        // spans local x 0..60, z 0..29 → x o-8..o+63, z o-20..o+10. Ticket
        // centred on the Basilisk chamber (cb x+30..+60).
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkPos ticketPos = new ChunkPos(o.offset(48, 0, -5));
        chunkSource.addRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
        ChunkPos minC = new ChunkPos(o.offset(-8, 0, -20));
        ChunkPos maxC = new ChunkPos(o.offset(63, 0, 10));
        for (int ccx = minC.x; ccx <= maxC.x; ccx++) {
            for (int ccz = minC.z; ccz <= maxC.z; ccz++) {
                level.getChunk(ccx, ccz); // blocking FULL load now; the queued
                                          // visibility promotion drains between
                                          // ticks, before the delayed build
            }
        }
        helper.runAfterDelay(5, () -> {
            try {
                basiliskMazeBuildAndAsserts(helper, level, o);
            } finally {
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
            }
            helper.succeed();
        });
    }

    /** Build + asserts of {@link #i122_basilisk_maze_content_and_sink} (body
     *  unchanged by the TF-023 infra fix — only moved behind the chunk
     *  pre-ticketing; the previously-delayed Basilisk query now runs same-tick,
     *  which the pre-promoted sections make reliable). */
    private static void basiliskMazeBuildAndAsserts(GameTestHelper helper, ServerLevel level,
                                                    BlockPos o) {
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.BASILISK_MAZE);

        // --- Shaft depth D from the lowest bedrock ring layer at the shaft corner
        // (ring loop BM:427-432 writes (x, y+j, z) for j = 8 down to -D+1).
        int lowestRingJ = Integer.MIN_VALUE;
        for (int j = -40; j <= 8; j++) {
            if (level.getBlockState(o.offset(0, j, 0)).is(Blocks.BEDROCK)) {
                lowestRingJ = j;
                break;
            }
        }
        helper.assertTrue(lowestRingJ != Integer.MIN_VALUE, "bedrock shaft ring not found");
        int depth = 1 - lowestRingJ; // ring bottom at y-D+1
        helper.assertTrue(depth >= 20 && depth <= 29,
                "shaft depth D=" + depth + " outside documented 20-29 (BM:31)");

        // --- Pyramid marker (BM:417-424): base ring corner + a mid ring; the
        // apex 4x4 is overwritten by the shaft's bedrock ring (BM:425-432,
        // makeEntrance runs after the pyramid loop).
        helper.assertTrue(level.getBlockState(o.offset(-8, 0, -8)).is(Blocks.SANDSTONE),
                "pyramid base ring corner should be sandstone");
        helper.assertTrue(level.getBlockState(o.offset(-1, 7, -1)).is(Blocks.SANDSTONE),
                "pyramid j=1 ring corner should be sandstone");
        helper.assertTrue(level.getBlockState(o.offset(0, 8, 0)).is(Blocks.BEDROCK),
                "shaft ring corner at the apex should be bedrock");

        // --- Spiral parkour steps: one obsidian core cell per layer, cycling
        // (1,1)->(2,1)->(2,2)->(1,2) (BM:438-456).
        int[][] spiralCells = {{1, 1}, {2, 1}, {2, 2}, {1, 2}};
        for (int j = 8; j >= -depth + 1; j--) {
            int stepIndex = (8 - j) % 4;
            for (int c = 0; c < 4; c++) {
                BlockPos cell = o.offset(spiralCells[c][0], j, spiralCells[c][1]);
                BlockState state = level.getBlockState(cell);
                if (c == stepIndex) {
                    helper.assertTrue(state.is(Blocks.OBSIDIAN),
                            "spiral step missing at layer j=" + j + " cell " + cell);
                } else {
                    helper.assertTrue(state.isAir(),
                            "shaft core cell should be air at layer j=" + j + " cell " + cell);
                }
            }
        }

        // --- Antechamber (castle base CB = O + (3, -D-4, -20)).
        BlockPos cb = o.offset(3, -depth - 4, -20);
        helper.assertTrue(level.getBlockState(cb.offset(-5, 2, 10)).is(Blocks.IRON_ORE),
                "antechamber west wall should be iron ore (BM:358-362)");
        for (int zOff : new int[]{0, 15, 29}) {
            helper.assertTrue(level.getBlockState(cb.offset(-3, 3, zOff))
                            .is(ModBlocks.EXTREME_TORCH.get()),
                    "antechamber Extreme Torch missing at z+" + zOff + " (BM:382-384)");
        }

        // --- Maze: passable = air at walking level CB.y+1, local 30x30.
        boolean[][] open = new boolean[30][30];
        for (int x = 0; x < 30; x++) {
            for (int z = 0; z < 30; z++) {
                open[x][z] = level.getBlockState(cb.offset(x, 1, z)).isAir();
            }
        }
        int entranceRow = -1;
        int exitRow = -1;
        int westOpenings = 0;
        int eastOpenings = 0;
        for (int z = 0; z < 30; z++) {
            if (open[0][z]) {
                westOpenings++;
                entranceRow = z;
            }
            if (open[29][z]) {
                eastOpenings++;
                exitRow = z;
            }
        }
        helper.assertTrue(westOpenings == 1, "maze west wall should have exactly the one carved "
                + "entrance (openMaze BM:281-288), found " + westOpenings);
        helper.assertTrue(eastOpenings == 1, "maze east wall should have exactly the one carved "
                + "exit (openMaze BM:289-296), found " + eastOpenings);
        // BFS west entrance -> east exit (4-neighbour) over placed blocks.
        boolean[][] visited = new boolean[30][30];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{0, entranceRow});
        visited[0][entranceRow] = true;
        boolean solvable = false;
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            if (cell[0] == 29 && cell[1] == exitRow) {
                solvable = true;
                break;
            }
            int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : steps) {
                int nx = cell[0] + d[0];
                int nz = cell[1] + d[1];
                if (nx < 0 || nx >= 30 || nz < 0 || nz >= 30) continue;
                if (visited[nx][nz] || !open[nx][nz]) continue;
                visited[nx][nz] = true;
                queue.add(new int[]{nx, nz});
            }
        }
        helper.assertTrue(solvable, "maze must be solvable west-to-east (Prim's algorithm "
                + "guarantees a spanning tree, BM:39-110)");

        // --- Trap fields in the floor (counts documented above).
        int lava = 0;
        for (int x = 1; x <= 28; x++) {
            for (int z = 1; z <= 28; z++) {
                if (level.getBlockState(cb.offset(x, 0, z)).is(Blocks.LAVA)) lava++;
            }
        }
        helper.assertTrue(lava >= 40 && lava <= 80,
                "maze-half lava trap count " + lava + " outside [40, 80] (80 draws, BM:308-310)");
        int rtp = 0;
        for (int x = 31; x <= 58; x++) {
            for (int z = 1; z <= 28; z++) {
                if (level.getBlockState(cb.offset(x, 0, z)).is(ModBlocks.BLOCK_TELEPORT.get())) rtp++;
            }
        }
        helper.assertTrue(rtp >= 10 && rtp <= 20,
                "chamber RTP trap count " + rtp + " outside [10, 20] (20 draws, BM:311-313)");

        // --- East-wall chests: 2-4 contiguous from z+2 step 2, torch 3 above,
        // all bound to chests/basilisk_maze (BM:388-396).
        int chests = 0;
        for (int k = 0; k < 5; k++) {
            BlockPos chestPos = cb.offset(58, 1, 2 + k * 2);
            if (!level.getBlockState(chestPos).is(Blocks.CHEST)) break;
            assertLootChest(helper, level, chestPos, "chests/basilisk_maze", null);
            helper.assertTrue(level.getBlockState(chestPos.above(3)).is(Blocks.TORCH),
                    "torch 3 above chest missing at " + chestPos.above(3));
            chests++;
        }
        helper.assertTrue(chests >= 2 && chests <= 4,
                "east-wall chest count " + chests + " outside documented 2-4 (BM:394)");

        // --- Loot distribution (n and tails documented in the method javadoc).
        Set<String> basiliskTable = Set.of(
                "minecraft:ender_pearl", "minecraft:diamond", "minecraft:blaze_rod",
                "orespawn:cage_empty", "orespawn:caged_mob", "minecraft:iron_ingot",
                "minecraft:gold_ingot", "orespawn:ingot_uranium", "orespawn:ingot_titanium",
                "orespawn:sun_fish", "orespawn:fire_fish", "orespawn:lava_eel",
                "orespawn:corn_dog", "minecraft:diamond_pickaxe", "minecraft:diamond_sword",
                "orespawn:ultimate_pickaxe", "orespawn:ultimate_sword",
                "orespawn:ultimate_fishing_rod", "orespawn:ultimate_bow",
                "minecraft:diamond_chestplate", "minecraft:diamond_helmet",
                "minecraft:diamond_leggings", "minecraft:diamond_boots",
                "orespawn:ultimate_chestplate", "orespawn:ultimate_leggings",
                "orespawn:ultimate_helmet", "orespawn:ultimate_boots", "orespawn:ruby",
                "orespawn:thunder_staff", "orespawn:magic_apple", "minecraft:golden_apple");
        List<List<ItemStack>> rolls = rollLoot(helper, "chests/basilisk_maze", 600);
        assertLootStats(helper, rolls, 5, 10, basiliskTable, true, "basilisk_maze loot");
        for (List<ItemStack> roll : rolls) {
            for (ItemStack stack : roll) {
                if (itemId(stack).equals("minecraft:diamond")) {
                    helper.assertTrue(stack.getCount() >= 15 && stack.getCount() <= 25,
                            "diamond stack of " + stack.getCount()
                                    + " outside documented 15-25 (BM:28 entry, WGEN-037)");
                }
            }
        }

        // --- Mining-dimension -2 sink (RNG-free scan replicated 1:1 against the
        // real Mining generator; LegacyDungeonStructure.java:180-210).
        helper.assertTrue(LegacyDungeonPiece.DungeonType.BASILISK_MAZE.placement
                        == LegacyDungeonPiece.DungeonType.PlacementMode.LOWEST_SURFACE_36,
                "BASILISK_MAZE must anchor via LOWEST_SURFACE_36 (LegacyDungeonPiece.java:139)");
        MinecraftServer server = level.getServer();
        Holder<Biome> miningBiome = server.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl("mining_biome")));
        Holder<NoiseGeneratorSettings> inland =
                server.registryAccess().registryOrThrow(Registries.NOISE_SETTINGS)
                        .getHolderOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, rl("inland")));
        OreSpawnChunkGenerator miningGen = new OreSpawnChunkGenerator(
                new FixedBiomeSource(miningBiome), inland, DimensionStyle.MINING);
        RandomState miningRandom = RandomState.create(
                server.registryAccess().asGetterLookup(),
                ResourceKey.create(Registries.NOISE_SETTINGS, rl("inland")), 0L);
        Structure maze = registeredStructure(helper, "basilisk_maze");
        int comparisons = 0;
        for (int c = 0; c < 300 && comparisons < 3; c++) {
            ChunkPos chunkPos = new ChunkPos(c * 7, c * 3);
            // Replica of lowestSurfaceOrigin (LegacyDungeonStructure.java:180-210,
            // OSW:2573-2597): 6x6 columns at offsets {0,3,...,15}, strictly-lowest
            // first-seen-wins inside Y 31..128, gate lowestY > 40, anchor lowest-2.
            int lowest = 128;
            int lx = chunkPos.getMinBlockX();
            int lz = chunkPos.getMinBlockZ();
            boolean found = false;
            for (int xOff = 0; xOff <= 15; xOff += 3) {
                for (int zOff = 0; zOff <= 15; zOff += 3) {
                    int x = chunkPos.getMinBlockX() + xOff;
                    int z = chunkPos.getMinBlockZ() + zOff;
                    int surface = miningGen.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                            level, miningRandom) - 1;
                    if (surface > 128 || surface < 31) continue;
                    if (surface >= lowest) continue;
                    lowest = surface;
                    lx = x;
                    lz = z;
                    found = true;
                }
            }
            BlockPos expected = (!found || lowest <= 40) ? null : new BlockPos(lx, lowest - 2, lz);
            // findValidGenerationPoint = findGenerationPoint filtered by the
            // context's biome predicate, which this test pins to always-true
            // (the base declaration is protected; the valid- wrapper is public).
            Optional<Structure.GenerationStub> stub = maze.findValidGenerationPoint(
                    contextFor(helper, miningGen, miningRandom, 0L, chunkPos));
            if (expected == null) {
                helper.assertTrue(stub.isEmpty(),
                        "scan rejected chunk " + chunkPos + " but findGenerationPoint placed at "
                                + stub.map(s -> s.position().toString()).orElse("?"));
            } else {
                helper.assertTrue(stub.isPresent() && stub.get().position().equals(expected),
                        "Mining anchor at chunk " + chunkPos + " should be lowest-2 = " + expected
                                + ", got " + stub.map(s -> s.position().toString()).orElse("empty"));
                comparisons++;
            }
        }
        helper.assertTrue(comparisons >= 1,
                "no Mining chunk in the sample produced a valid maze anchor to compare");

        // --- Basilisks: same-tick query, reliable now that the chamber chunks
        // were promoted (entity sections TRACKED) before the build (TF-023 —
        // the earlier delay-only attempt still raced the promotion queue).
        AABB chamber = new AABB(cb.getX() + 30, cb.getY(), cb.getZ(),
                cb.getX() + 60, cb.getY() + 7, cb.getZ() + 30);
        List<? extends Mob> basilisks =
                level.getEntities(ModEntities.BASILISK.get(), chamber, e -> true);
        helper.assertTrue(basilisks.size() == 3,
                "expected exactly 3 chamber Basilisks (BM:397-410), got " + basilisks.size());
        for (Mob basilisk : basilisks) {
            helper.assertTrue(basilisk.isPersistenceRequired(),
                    "chamber Basilisks must be persistent (orig func_110163_bv, BM:243-252)");
        }
    }

    // =====================================================================
    // i126-wgen-052-053-056-item-066 — level-6 King Challenge Tower prizes
    // =====================================================================

    /**
     * Checklist i126-wgen-052-053-056-item-066 (FIX_LOG.md:794 "Challenge
     * Towers reconciled, WGEN-051..056 + ITEM-066"). Far region K=602.
     *
     * <p>The tower level roll is deterministic on the worldgen path (piece RNG
     * seeded from the bounding box, LegacyDungeonPiece.java:465-470), so the
     * test predicts the roll per candidate origin ({@link #predictTowerLevel})
     * and builds the first level-6 candidate. 200 candidates: under seed
     * uniformity P(no level-6) = (13/18)^200 ≈ 5e-29.</p>
     *
     * <p>Asserts, all against generateChallengeTower
     * (LegacyDungeonPiece.java:2247-2446) + buildChallengeFloor (:2458-2551) +
     * addChallengeFloorDecor (:2565-2681) + fillChallengeChests (:2798-2831),
     * ported from orig GD:191-786:</p>
     * <ul>
     *   <li>WGEN-052: NO scaffolding and NO ladders anywhere in the piece
     *       bounding box (the legacy places no climbable blocks,
     *       LegacyDungeonPiece.java:2664-2669);</li>
     *   <li>WGEN-056: all 4 chests of a floor face the room centre
     *       (E/W/S/N, GD:744/754/765/776 metas 5/4/3/2);</li>
     *   <li>WGEN-053: non-prize floors of the level-6 tower bind the faithful
     *       tier tables — floor2→level5 (the 83-spawn-egg jackpot),
     *       floor3→level4, floor4→level3, floor5→level2, floor6 (Nightmare
     *       cap)→level1 (pickDecorReward, LegacyDungeonPiece.java:2768-2777) —
     *       and the tables roll 5-11 stacks of the documented lists
     *       (chests/challenge_tower_level*.json transcribing GD:57-61);</li>
     *   <li>ITEM-066 + WGEN-051: the level-6 BOTTOM floor holds the fixed
     *       Royal prize layout — west chest slot 1 = FUNCTIONAL The Prince
     *       spawn egg (King variant; used by a fake player it spawns
     *       orespawn:the_prince), east = Royal helmet+chestplate, north =
     *       leggings+boots, south = Royal Guardian Sword (GD:743-784) — plus
     *       the 4 RTP blocks ringing the central spawner column
     *       (LegacyDungeonPiece.java:2671-2679, GD:718-721).</li>
     * </ul>
     *
     * <p>Loot sampling: level5 n=1500 (avg 8 stacks/roll ⇒ ≥7500 samples; the
     * rarest entry weighs 5/1285, so P(any of the 87 entries unobserved) ≤
     * 87·e^(−7500·5/1285) ≈ 2e-11; roll-count endpoints of 5-11: 2·(6/7)^1500
     * ≈ 5e-101). Levels 1-4 n=300 each (≥1500 samples; worst weight 10/235 ⇒
     * coverage miss ≤ 17·e^(−1500·10/235) ≈ 5e-27).</p>
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void i126_challenge_tower_level6_prizes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = farOrigin(helper, 602, 10);

        BlockPos origin = null;
        for (int i = 0; i < 200; i++) {
            BlockPos candidate = base.offset(0, 0, i);
            if (predictTowerLevel(candidate, LegacyDungeonPiece.DungeonType.KING_TOWER) == 6) {
                origin = candidate;
                break;
            }
        }
        helper.assertTrue(origin != null, "no level-6 King tower seed among 200 candidates "
                + "(P ≈ 5e-29 under seed uniformity)");
        final BlockPos o = origin;

        // TF-023 harness race (proven fix pattern: StructureTestsC red-ant
        // i165, FIX_LOG TF-023 CLOSED): the prize-egg use spawns The Prince
        // into freshly force-loaded chunks whose entity sections stay HIDDEN
        // until the queued FullChunkStatus promotion pumps, so the query after
        // the spawn could miss it. Pin the tower footprint (piece box
        // ~o..o+28 in XZ) under a FORCED ticket and sync-load it a few ticks
        // BEFORE the build so the spawn lands in TRACKED (queryable) sections.
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkPos ticketPos = new ChunkPos(o.offset(14, 0, 14));
        chunkSource.addRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
        ChunkPos minC = new ChunkPos(o.offset(-2, 0, -2));
        ChunkPos maxC = new ChunkPos(o.offset(30, 0, 30));
        for (int ccx = minC.x; ccx <= maxC.x; ccx++) {
            for (int ccz = minC.z; ccz <= maxC.z; ccz++) {
                level.getChunk(ccx, ccz); // blocking FULL load now; the queued
                                          // visibility promotion drains between
                                          // ticks, before the delayed build
            }
        }
        helper.runAfterDelay(5, () -> {
            try {
                challengeTowerBuildAndAsserts(helper, level, o);
            } finally {
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
            }
            helper.succeed();
        });
    }

    /** Build + asserts of {@link #i126_challenge_tower_level6_prizes} (body
     *  unchanged by the TF-023 infra fix — only moved behind the chunk
     *  pre-ticketing; the previously-delayed Prince query now runs same-tick,
     *  which the pre-promoted sections make reliable). */
    private static void challengeTowerBuildAndAsserts(GameTestHelper helper, ServerLevel level,
                                                      BlockPos o) {
        buildWorldgenPath(level, o, LegacyDungeonPiece.DungeonType.KING_TOWER);

        // Sanity: the predicted level-6 tower really built all six floors —
        // floor 6's bedrock CEILING exists (floor 6 corner O+(3,62,3), h=16 ->
        // ceiling plane y+78; probe a non-corner cell because the decor-6
        // Nightmare cap stamps netherrack/fire on the ceiling corners and an
        // air hole at the centre, LegacyDungeonPiece.java:2571-2599).
        helper.assertTrue(level.getBlockState(o.offset(10, 78, 10)).is(Blocks.BEDROCK),
                "predicted level-6 tower is missing floor 6 — seed replica out of sync");

        // WGEN-052 — no scaffolding, no ladders anywhere in the piece box.
        LegacyDungeonPiece piece = new LegacyDungeonPiece(o, LegacyDungeonPiece.DungeonType.KING_TOWER);
        var box = piece.getBoundingBox();
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.SCAFFOLDING) || state.is(Blocks.LADDER)) {
                helper.assertTrue(false,
                        "invented climb aid at " + pos.immutable() + " (WGEN-052: the legacy tower "
                                + "has no climbable blocks, LegacyDungeonPiece.java:2664-2669)");
            }
        }

        // Bottom floor (floor 1: corner O+(1,16,1), width 26, decor 1, reward 6).
        BlockPos f1 = o.offset(1, 16, 1);
        // ITEM-066 — west chest, faces EAST, slot 1 = functional Prince egg (GD:743-752).
        BlockPos westChestPos = f1.offset(1, 1, 13);
        BlockState westState = level.getBlockState(westChestPos);
        helper.assertTrue(westState.is(Blocks.CHEST)
                        && westState.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST,
                "west prize chest missing or not facing the room centre (WGEN-056, GD:744)");
        BlockEntity westBe = level.getBlockEntity(westChestPos);
        helper.assertTrue(westBe instanceof RandomizableContainerBlockEntity,
                "west prize chest has no container");
        ItemStack prizeEgg = ((RandomizableContainerBlockEntity) westBe).getItem(1);
        helper.assertTrue(prizeEgg.is(ModItems.THE_PRINCE_SPAWN_EGG.get()),
                "King tower prize must be the functional The Prince SPAWN EGG (ITEM-066, "
                        + "orig ThePrinceEgg OSM:5616), got " + itemId(prizeEgg));

        // East chest: Royal helmet + chestplate (GD:753-763), faces WEST.
        BlockPos eastChestPos = f1.offset(24, 1, 13);
        helper.assertTrue(level.getBlockState(eastChestPos).is(Blocks.CHEST)
                        && level.getBlockState(eastChestPos)
                        .getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST,
                "east prize chest missing or wrong facing");
        RandomizableContainerBlockEntity eastBe =
                (RandomizableContainerBlockEntity) level.getBlockEntity(eastChestPos);
        helper.assertTrue(eastBe.getItem(1).is(ModItems.ROYAL_HELMET.get())
                        && eastBe.getItem(2).is(ModItems.ROYAL_CHESTPLATE.get()),
                "east prize chest should hold Royal helmet (slot 1) + chestplate (slot 2)");

        // North chest: leggings + boots (GD:764-774), faces SOUTH.
        BlockPos northChestPos = f1.offset(13, 1, 1);
        helper.assertTrue(level.getBlockState(northChestPos).is(Blocks.CHEST)
                        && level.getBlockState(northChestPos)
                        .getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH,
                "north prize chest missing or wrong facing");
        RandomizableContainerBlockEntity northBe =
                (RandomizableContainerBlockEntity) level.getBlockEntity(northChestPos);
        helper.assertTrue(northBe.getItem(1).is(ModItems.ROYAL_LEGGINGS.get())
                        && northBe.getItem(2).is(ModItems.ROYAL_BOOTS.get()),
                "north prize chest should hold Royal leggings (slot 1) + boots (slot 2)");

        // South chest: Royal Guardian Sword (GD:775-784), faces NORTH.
        BlockPos southChestPos = f1.offset(13, 1, 24);
        helper.assertTrue(level.getBlockState(southChestPos).is(Blocks.CHEST)
                        && level.getBlockState(southChestPos)
                        .getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
                "south prize chest missing or wrong facing");
        RandomizableContainerBlockEntity southBe =
                (RandomizableContainerBlockEntity) level.getBlockEntity(southChestPos);
        helper.assertTrue(southBe.getItem(1).is(ModItems.ROYAL_GUARDIAN_SWORD.get()),
                "south prize chest should hold the Royal Guardian Sword (slot 1)");

        // 4 RTP blocks around the central spawner column (GD:718-721).
        for (int[] d : new int[][]{{12, 12}, {14, 14}, {14, 12}, {12, 14}}) {
            helper.assertTrue(level.getBlockState(f1.offset(d[0], 1, d[1]))
                            .is(ModBlocks.BLOCK_TELEPORT.get()),
                    "RTP block missing at floor offset (" + d[0] + ",1," + d[1] + ")");
        }
        // Central spawner column, bottom floor: decor=1 difficulty=6 -> Hammerhead
        // (pickKingDecorMob, LegacyDungeonPiece.java:2694-2700).
        assertSpawner(helper, level, f1.offset(13, 2, 13), ModEntities.HAMMERHEAD.get());
        assertSpawner(helper, level, f1.offset(13, 3, 13), ModEntities.HAMMERHEAD.get());
        // WGEN-054 cross-check (see i127): the SAME level-6 tower's floor-2
        // centre column is the "Jumpy Bug" — pickKingDecorMob(decor=2,
        // difficulty=6) = TrooperBug (LegacyDungeonPiece.java:2701-2702).
        BlockPos f2 = o.offset(1, 26, 1);
        assertSpawner(helper, level, f2.offset(13, 2, 13), ModEntities.ENTITY_TROOPER_BUG.get());
        assertSpawner(helper, level, f2.offset(13, 3, 13), ModEntities.ENTITY_TROOPER_BUG.get());

        // WGEN-053 — per-floor tier tables of the level-6 tower (reward = 7-decor).
        // Floor corners/widths per generateChallengeTower phase 10
        // (LegacyDungeonPiece.java:2344-2375): f2 O+(1,26,1) w26; f3 O+(2,36,2) w24;
        // f4 O+(2,45,2) w24; f5 O+(3,54,3) w22; f6 O+(3,62,3) w22 with chests at
        // floor y+4+1 (decor-6 Nightmare cap, fillChallengeChests call :2614).
        assertTierChests(helper, level, o.offset(1, 26, 1), 26, 0, "chests/challenge_tower_level5");
        assertTierChests(helper, level, o.offset(2, 36, 2), 24, 0, "chests/challenge_tower_level4");
        assertTierChests(helper, level, o.offset(2, 45, 2), 24, 0, "chests/challenge_tower_level3");
        assertTierChests(helper, level, o.offset(3, 54, 3), 22, 0, "chests/challenge_tower_level2");
        assertTierChests(helper, level, o.offset(3, 62, 3), 22, 4, "chests/challenge_tower_level1");

        // Tier list contents (chests/challenge_tower_level1..5.json <- GD:57-61).
        assertLootStats(helper, rollLoot(helper, "chests/challenge_tower_level1", 300), 5, 11,
                Set.of("minecraft:emerald", "orespawn:miners_dream", "orespawn:emerald_pickaxe",
                        "orespawn:emerald_shovel", "orespawn:emerald_hoe", "orespawn:emerald_axe",
                        "orespawn:emerald_sword", "orespawn:emerald_chestplate",
                        "orespawn:emerald_leggings", "orespawn:emerald_helmet",
                        "orespawn:emerald_boots"),
                true, "challenge level1 (emerald kit)");
        assertLootStats(helper, rollLoot(helper, "chests/challenge_tower_level2", 300), 5, 11,
                Set.of("minecraft:experience_bottle", "orespawn:creeper_launcher",
                        "orespawn:pink_helmet", "orespawn:pink_chestplate", "orespawn:pink_leggings",
                        "orespawn:pink_boots", "orespawn:fairy_sword", "orespawn:emerald_pickaxe",
                        "orespawn:emerald_shovel", "orespawn:emerald_hoe", "orespawn:emerald_axe",
                        "orespawn:emerald_sword", "orespawn:experience_chestplate",
                        "orespawn:experience_leggings", "orespawn:experience_helmet",
                        "orespawn:experience_boots"),
                true, "challenge level2 (experience+pink)");
        assertLootStats(helper, rollLoot(helper, "chests/challenge_tower_level3", 300), 5, 11,
                Set.of("orespawn:squid_zooka", "orespawn:rat_sword", "orespawn:amethyst_gem",
                        "minecraft:ink_sac", "orespawn:tigerseye_helmet",
                        "orespawn:tigerseye_chestplate", "orespawn:tigerseye_leggings",
                        "orespawn:tigerseye_boots", "orespawn:amethyst_pickaxe",
                        "orespawn:amethyst_shovel", "orespawn:amethyst_hoe", "orespawn:amethyst_axe",
                        "orespawn:amethyst_sword", "orespawn:amethyst_chestplate",
                        "orespawn:amethyst_leggings", "orespawn:amethyst_helmet",
                        "orespawn:amethyst_boots"),
                true, "challenge level3 (amethyst+tigerseye)");
        assertLootStats(helper, rollLoot(helper, "chests/challenge_tower_level4", 300), 5, 11,
                Set.of("orespawn:ruby", "orespawn:magic_apple", "orespawn:ray_gun",
                        "orespawn:creeper_repellent", "orespawn:kraken_repellent",
                        "orespawn:experience_catcher", "orespawn:zoo_keeper",
                        "orespawn:ruby_pickaxe", "orespawn:ruby_shovel", "orespawn:ruby_hoe",
                        "orespawn:ruby_axe", "orespawn:ruby_sword", "orespawn:thunder_staff",
                        "orespawn:ruby_chestplate", "orespawn:ruby_leggings", "orespawn:ruby_helmet",
                        "orespawn:ruby_boots"),
                true, "challenge level4 (ruby+utility)");
        // Level 5 — the 83-spawn-egg jackpot: every item is either a spawn egg
        // or one of the 4 documented non-egg entries, and exactly 83 distinct
        // eggs appear (chests/challenge_tower_level5.json, 87 entries).
        Set<String> level5NonEggs = Set.of("orespawn:nightmare_sword", "orespawn:poison_sword",
                "orespawn:ant_robot_kit", "orespawn:spider_robot_kit");
        List<List<ItemStack>> level5Rolls = rollLoot(helper, "chests/challenge_tower_level5", 1500);
        Set<String> eggIds = new HashSet<>();
        Set<String> nonEggIds = new HashSet<>();
        int level5Min = Integer.MAX_VALUE;
        int level5Max = Integer.MIN_VALUE;
        for (List<ItemStack> roll : level5Rolls) {
            helper.assertTrue(roll.size() >= 5 && roll.size() <= 11,
                    "level5 roll produced " + roll.size() + " stacks, expected 5-11");
            level5Min = Math.min(level5Min, roll.size());
            level5Max = Math.max(level5Max, roll.size());
            for (ItemStack stack : roll) {
                if (stack.getItem() instanceof SpawnEggItem) {
                    eggIds.add(itemId(stack));
                } else {
                    helper.assertTrue(level5NonEggs.contains(itemId(stack)),
                            "level5 rolled an undocumented non-egg item: " + itemId(stack));
                    nonEggIds.add(itemId(stack));
                }
            }
        }
        helper.assertTrue(level5Min == 5 && level5Max == 11,
                "level5 roll-count range " + level5Min + "-" + level5Max + ", documented 5-11");
        helper.assertTrue(eggIds.size() == 83,
                "level5 jackpot should surface exactly 83 distinct spawn eggs, saw " + eggIds.size());
        helper.assertTrue(nonEggIds.equals(level5NonEggs),
                "level5 non-egg entries mismatch: " + nonEggIds);

        // ITEM-066 behavioural half — the prize egg actually spawns The Prince.
        Player user = helper.makeMockPlayer(GameType.SURVIVAL);
        user.setItemInHand(InteractionHand.MAIN_HAND, prizeEgg.copy());
        BlockPos spawnFloor = f1.offset(6, 0, 6); // bedrock floor cell of the bottom room
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(spawnFloor).add(0, 0.5, 0), Direction.UP, spawnFloor, false);
        InteractionResult used = user.getItemInHand(InteractionHand.MAIN_HAND)
                .useOn(new UseOnContext(user, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(used.consumesAction(),
                "using the prize spawn egg on the floor should succeed, got " + used);
        AABB spawnBox = new AABB(spawnFloor).inflate(4.0);
        // Same-tick query, reliable now that the tower chunks were promoted
        // (entity sections TRACKED) before the build (TF-023 — the earlier
        // delay-only attempt still raced the promotion queue).
        List<? extends Mob> princes =
                level.getEntities(ModEntities.THE_PRINCE.get(), spawnBox, e -> true);
        helper.assertTrue(!princes.isEmpty(),
                "prize spawn egg did not spawn orespawn:the_prince (ITEM-066)");
        princes.forEach(Mob::discard);
    }

    /**
     * Asserts the 4 centre-facing chests of one tower floor all bind
     * {@code tablePath}: west faces EAST, east faces WEST, north faces SOUTH,
     * south faces NORTH (fillChallengeChests, LegacyDungeonPiece.java:2825-2830,
     * facings restored per WGEN-056 from GD:744/754/765/776). {@code yBump} is
     * +4 on the decor-6 Nightmare-cap floor (chest fill call at cposy+4,
     * LegacyDungeonPiece.java:2614).
     */
    private static void assertTierChests(GameTestHelper helper, ServerLevel level, BlockPos floor,
                                         int width, int yBump, String tablePath) {
        int mid = width / 2;
        assertLootChest(helper, level, floor.offset(1, yBump + 1, mid), tablePath, Direction.EAST);
        assertLootChest(helper, level, floor.offset(width - 2, yBump + 1, mid), tablePath, Direction.WEST);
        assertLootChest(helper, level, floor.offset(mid, yBump + 1, 1), tablePath, Direction.SOUTH);
        assertLootChest(helper, level, floor.offset(mid, yBump + 1, width - 2), tablePath, Direction.NORTH);
    }

    // =====================================================================
    // i127-wgen-054 — "Jumpy Bug" (TrooperBug) in tower centre rooms
    // =====================================================================

    /**
     * Checklist i127-wgen-054 (WGEN-054, FIX_LOG.md:794): "level-2+ tower
     * centre rooms at the right difficulty spawn Jumpy Bugs (TrooperBug —
     * jumps, no spit attack)". Far region K=603.
     *
     * <p>"Right difficulty" is the TOWER's level roll, not server difficulty:
     * the centre-room mob ladder pickKingDecorMob maps "Jumpy Bug" =
     * TrooperBug (orig OreSpawnMain.java:3943, WGEN-054) to decor=1 @
     * difficulty 5 and decor=2 @ difficulty 6 (LegacyDungeonPiece.java:
     * 2694-2706, orig GD:484-700). The test builds a deterministically
     * predicted LEVEL-5 King tower ({@link #predictTowerLevel}; 200 candidates,
     * P(none) = (13/18)^200 ≈ 5e-29) and asserts the bottom-floor centre
     * spawner column is TrooperBug — the very cell the pre-fix port filled
     * with the wrong bug.</p>
     *
     * <p>Behaviour half, in-template: TrooperBug's jump seam is the public
     * {@code jumpFromGround()} override (EntityTrooperBug.java:117-131) —
     * +1.15 vertical impulse and +1.5 position raise
     * (EntityTrooperBug.java:40-41) — asserted exactly. "No spit attack": the
     * TrooperBug's goal table has no ranged goal at all (EntityTrooperBug.java:
     * 51-62 — leap/wander/look only; the SpitBug's acid burst lives in
     * SpitBugAcidAttackGoal, EntitySpitBug.java:51-55), asserted as a bounded
     * 200-tick negative: with a live target set every tick, no orespawn:acid
     * projectile may ever appear in the test bounds.</p>
     */
    @GameTest(template = "empty_large", timeoutTicks = 400)
    public void i127_tower_centre_rooms_spawn_jumpy_bug(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // --- Far half: deterministic level-5 King tower, bottom-centre spawners.
        BlockPos base = farOrigin(helper, 603, 10);
        BlockPos origin = null;
        for (int i = 0; i < 200; i++) {
            BlockPos candidate = base.offset(0, 0, i);
            if (predictTowerLevel(candidate, LegacyDungeonPiece.DungeonType.KING_TOWER) == 5) {
                origin = candidate;
                break;
            }
        }
        helper.assertTrue(origin != null, "no level-5 King tower seed among 200 candidates "
                + "(P ≈ 5e-29 under seed uniformity)");
        buildWorldgenPath(level, origin, LegacyDungeonPiece.DungeonType.KING_TOWER);
        // Sanity of the prediction: floor 5's bedrock ceiling plane (y+62 =
        // 16+10+10+9+9+8, non-corner cell to dodge the decor ceiling holes)
        // exists, and floor 6's ceiling plane (y+78) does NOT.
        helper.assertTrue(level.getBlockState(origin.offset(10, 62, 10)).is(Blocks.BEDROCK),
                "predicted level-5 tower is missing floor 5 — seed replica out of sync");
        helper.assertTrue(!level.getBlockState(origin.offset(10, 78, 10)).is(Blocks.BEDROCK),
                "predicted level-5 tower built a floor 6 — seed replica out of sync");
        // Bottom floor (corner O+(1,16,1), width 26): centre column at +13, y+2/+3
        // (addChallengeFloorDecor :2627-2629) = TrooperBug for decor=1 difficulty=5
        // (pickKingDecorMob case 1, LegacyDungeonPiece.java:2696).
        BlockPos f1 = origin.offset(1, 16, 1);
        assertSpawner(helper, level, f1.offset(13, 2, 13), ModEntities.ENTITY_TROOPER_BUG.get());
        assertSpawner(helper, level, f1.offset(13, 3, 13), ModEntities.ENTITY_TROOPER_BUG.get());

        // --- In-template behaviour half.
        // Platform so the bug and its target have ground to stand on.
        for (int x = 16; x <= 31; x++) {
            for (int z = 16; z <= 31; z++) {
                helper.setBlock(x, 1, z, Blocks.STONE);
            }
        }
        var bug = helper.spawn(ModEntities.ENTITY_TROOPER_BUG.get(), new BlockPos(23, 2, 23));
        var cow = helper.spawn(EntityType.COW, new BlockPos(27, 2, 23));

        // Jump mechanism: exact impulse/raise per EntityTrooperBug.java:40-41,117-131.
        double y0 = bug.getY();
        double motionY0 = bug.getDeltaMovement().y;
        bug.jumpFromGround();
        helper.assertTrue(Math.abs(bug.getDeltaMovement().y - (motionY0 + 1.15)) < 1.0e-6,
                "jumpFromGround should add the 1.15 vertical impulse (EntityTrooperBug.java:40), got dY="
                        + (bug.getDeltaMovement().y - motionY0));
        helper.assertTrue(Math.abs(bug.getY() - (y0 + 1.5)) < 1.0e-6,
                "jumpFromGround should raise the bug 1.5 blocks (EntityTrooperBug.java:41), got "
                        + (bug.getY() - y0));

        // Negative: 200 ticks with a live target and never an acid projectile
        // (TrooperBug registers no ranged goal, EntityTrooperBug.java:51-62).
        helper.onEachTick(() -> {
            if (bug.isAlive() && cow.isAlive()) {
                bug.setTarget(cow);
            }
            helper.assertEntityNotPresent(ModEntities.ACID.get());
        });
        helper.runAfterDelay(200, helper::succeed);
    }

    // =====================================================================
    // i137-endercastle-islands — Ender Castle Islands variant anchors at grass
    // =====================================================================

    /**
     * Checklist i137-endercastle-islands (D6a, ender_castle_spec.md; FIX_LOG.md
     * :758 notes PN-017): "/locate orespawn:ender_castle_islands in
     * orespawn:islands: same castle at grass level". Far region K=604.
     *
     * <p>Mechanism (LegacyDungeonStructure.CODEC placement_mode override,
     * LegacyDungeonStructure.java:36-48; DungeonType.ENDER_CASTLE,
     * LegacyDungeonPiece.java:146-153 — orig OSW:1557-1570 End anchor vs
     * OSW:2322-2343 Islands D4 i==7):</p>
     * <ul>
     *   <li>"same castle": both registered structures deserialize to
     *       LegacyDungeonStructure with dungeon_type ENDER_CASTLE — the
     *       generate dispatch keys on the DungeonType alone
     *       (LegacyDungeonPiece.java:495), so both variants run the identical
     *       EnderCastleGenerator; asserted by codec re-encoding both registry
     *       entries;</li>
     *   <li>"at grass level": the islands JSON carries placement_mode
     *       ISLANDS_GRASS while the End JSON omits it (default END_SURFACE);
     *       against the real Islands generator every anchor sits AT the grass
     *       block (heightmap−1, the flat Y-7 plane, window 5..20 per
     *       OSW:2259), while against the real End generator anchors sit ON the
     *       surface (heightmap, air-on-end-stone, window 11..90 per
     *       OSW:1564);</li>
     *   <li>biome wiring: ender_castle_islands.json binds
     *       #orespawn:has_structure/ender_castle_islands = orespawn:island_biome;
     *       ender_castle_end.json binds #minecraft:is_end;</li>
     *   <li>content sanity via buildNow: the 29x29 obsidian plate (−3..+25,
     *       EnderCastleGenerator.java:94-110, orig GD:3219-3232) and the
     *       rooftop Ender Reaper / Ender Knight spawner pairs
     *       (EnderCastleGenerator.java:267-277, orig GD:3339-3378).</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i137_ender_castle_islands_grass_anchor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();

        // --- Same castle: both variants encode to dungeon_type ENDER_CASTLE.
        Structure islandsVariant = registeredStructure(helper, "ender_castle_islands");
        Structure endVariant = registeredStructure(helper, "ender_castle_end");
        JsonObject islandsJson = encodeStructure(helper, islandsVariant);
        JsonObject endJson = encodeStructure(helper, endVariant);
        helper.assertTrue("ENDER_CASTLE".equals(islandsJson.get("dungeon_type").getAsString())
                        && "ENDER_CASTLE".equals(endJson.get("dungeon_type").getAsString()),
                "both Ender Castle structures must share dungeon_type ENDER_CASTLE (same castle)");
        helper.assertTrue(islandsJson.has("placement_mode")
                        && "ISLANDS_GRASS".equals(islandsJson.get("placement_mode").getAsString()),
                "islands variant must override placement_mode to ISLANDS_GRASS (OSW:2322-2343)");
        helper.assertTrue(!endJson.has("placement_mode"),
                "End variant must use the DungeonType default placement (no override)");
        helper.assertTrue(LegacyDungeonPiece.DungeonType.ENDER_CASTLE.placement
                        == LegacyDungeonPiece.DungeonType.PlacementMode.END_SURFACE,
                "ENDER_CASTLE default anchor must be END_SURFACE (LegacyDungeonPiece.java:153)");

        // --- Biome wiring.
        helper.assertTrue(islandsVariant.biomes().stream()
                        .anyMatch(h -> h.is(ResourceKey.create(Registries.BIOME, rl("island_biome")))),
                "islands variant must be bound to orespawn:island_biome");
        helper.assertTrue(endVariant.biomes().stream().anyMatch(h -> h.is(Biomes.THE_END)),
                "End variant must be bound to the End biomes (#minecraft:is_end)");

        // --- Islands anchor: grass level (heightmap-1) on the flat Islands plane.
        int oldLessLag = OreSpawnConfig.LESS_LAG.get();
        OreSpawnConfig.LESS_LAG.set(0); // islandsGrassOrigin 50% skip gate, LegacyDungeonStructure.java:459
        try {
            OreSpawnChunkGenerator islandsGen = islandsGenerator(helper);
            RandomState islandsRandom = islandsRandomState(helper);
            for (int c = 0; c < 20; c++) {
                ChunkPos chunkPos = new ChunkPos(c * 5, -c * 3);
                Optional<Structure.GenerationStub> stub = islandsVariant.findValidGenerationPoint(
                        contextFor(helper, islandsGen, islandsRandom, 0L, chunkPos));
                helper.assertTrue(stub.isPresent(),
                        "islands variant produced no anchor at chunk " + chunkPos
                                + " (flat grass plane should always qualify with lessLag=0)");
                BlockPos pos = stub.get().position();
                int grassY = islandsGen.getBaseHeight(pos.getX(), pos.getZ(),
                        Heightmap.Types.WORLD_SURFACE_WG, level, islandsRandom) - 1;
                helper.assertTrue(pos.getY() == grassY,
                        "islands anchor must sit AT the grass block (heightmap-1): got Y="
                                + pos.getY() + ", grass Y=" + grassY);
                helper.assertTrue(pos.getY() >= 5 && pos.getY() <= 20,
                        "islands grass anchor outside the original's Y 5..20 scan window (OSW:2259)");
            }
        } finally {
            OreSpawnConfig.LESS_LAG.set(oldLessLag);
        }

        // --- End anchor contrast: ON the surface (heightmap, no -1), window 11..90.
        ServerLevel end = server.getLevel(Level.END);
        helper.assertTrue(end != null, "the End dimension is not loaded on the gametest server");
        ChunkGenerator endGen = end.getChunkSource().getGenerator();
        RandomState endRandom = end.getChunkSource().randomState();
        int endStubs = 0;
        for (int c = 0; c < 80 && endStubs < 3; c++) {
            ChunkPos chunkPos = new ChunkPos(c % 9 - 4, c / 9 - 4);
            Optional<Structure.GenerationStub> stub = endVariant.findValidGenerationPoint(
                    contextFor(helper, endGen, endRandom, end.getSeed(), chunkPos));
            if (stub.isEmpty()) continue;
            BlockPos pos = stub.get().position();
            // Same height accessor the context carried (both clamp to the end
            // noise settings' own 0..256 range, so the probe is identical).
            int surfaceY = endGen.getBaseHeight(pos.getX(), pos.getZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, helper.getLevel(), endRandom);
            helper.assertTrue(pos.getY() == surfaceY,
                    "End anchor must sit ON the surface (heightmap): got Y=" + pos.getY()
                            + ", surface " + surfaceY);
            helper.assertTrue(pos.getY() >= 11 && pos.getY() <= 90,
                    "End anchor outside the original's Y 11..90 scan window (OSW:1564)");
            endStubs++;
        }
        helper.assertTrue(endStubs >= 1,
                "no End chunk in the sample produced an Ender Castle anchor");

        // --- Content sanity (both variants run this same generator — dispatch
        // keys on DungeonType, LegacyDungeonPiece.java:495).
        BlockPos o = farOrigin(helper, 604, 10);
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.ENDER_CASTLE);
        helper.assertTrue(level.getBlockState(o.offset(-3, 0, -3)).is(Blocks.OBSIDIAN)
                        && level.getBlockState(o.offset(25, 0, 25)).is(Blocks.OBSIDIAN),
                "29x29 obsidian base plate corners missing (GD:3219-3232)");
        helper.assertTrue(level.getBlockState(o.offset(-3, 1, 11)).is(Blocks.IRON_BARS),
                "plate rim iron-bar fence missing (GD:3219-3232)");
        // Rooftop spawner pairs at WIDTH/2 +-5 = x+16/x+6 (GD:3339-3378).
        assertSpawner(helper, level, o.offset(16, 9, 16), ModEntities.ENDER_REAPER.get());
        assertSpawner(helper, level, o.offset(16, 10, 16), ModEntities.ENDER_KNIGHT.get());
        assertSpawner(helper, level, o.offset(6, 9, 6), ModEntities.ENDER_REAPER.get());
        assertSpawner(helper, level, o.offset(6, 10, 6), ModEntities.ENDER_KNIGHT.get());
        helper.succeed();
    }

    // =====================================================================
    // i138-incapyramid-islands — Inca Pyramid content
    // =====================================================================

    /**
     * Checklist i138-incapyramid-islands (WGEN-042 pool, PN-018; spec
     * phase_d_reports/d6_extraction/inca_pyramid_spec.md). Far region K=605:
     * build 1 at the region base carries the content asserts; builds 2-15 (z
     * step 160) only sample the per-grave Ghost-spawner rolls.
     *
     * <p>All positions from IncaPyramidGenerator.java (orig GD:3735-4042),
     * base origin B, temple origin T = B + (10,10,10) (orig :3879-3881):</p>
     * <ul>
     *   <li>41x31 stepped base: stone-brick floor plate corners (orig
     *       :3769-3771) + ring walls in the stone/cobble/mossy palette (orig
     *       :3760-3768);</li>
     *   <li>4 torch-ended ramps with support pillars reaching the ground —
     *       PN-018: on the buildNow path unrecorded cells read as air, so
     *       every outer ramp column fills stone down to relative y0
     *       (buildRamp, IncaPyramidGenerator.java:274-311; end torches orig
     *       :3790-3797);</li>
     *   <li>lit redstone-lamp checkerboard punched into the temple walls at
     *       T.y+6 — every perimeter cell with (i+k) odd is a LIT lamp (orig
     *       :3907-3915, field_150374_bv = the lit block);</li>
     *   <li>5 water altars (orig :3929-3933 via makepoolalter :3954-3961);</li>
     *   <li>4 Creeper Repellents (orig :3934-3937) + Molenoid spawner (orig
     *       :3938-3942);</li>
     *   <li>trapdoor + ladder shaft (orig :3943-3950; trapdoor meta 3 = facing
     *       EAST, ladder meta 2 = facing NORTH);</li>
     *   <li>24 graves — poppy/dandelion/poppy beds, headstone, slabs, chest
     *       facing NORTH bound to chests/inca_pyramid (orig :3963-4042);</li>
     *   <li>~1/3 Ghost spawners: 15 builds x 24 graves = 360 Bernoulli(1/3)
     *       draws (orig :4000-4006 nextInt(3)==1); bound [60,180] vs mean 120,
     *       sigma ≈ 8.94 — a 6.7-sigma two-sided tail, P ≈ 2e-11;</li>
     *   <li>chests roll 10-14 stacks of the 480-weight 14-entry table
     *       (chests/inca_pyramid.json <- GD:38 + 10+nextInt(5) at :4010/:4039;
     *       n=300: endpoints 2·(4/5)^300 ≈ 3e-29, full coverage min weight
     *       25/480 over ≥3000 samples → miss ≤ 14·e^(−156) ≈ 0).</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void i138_inca_pyramid_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos b = farOrigin(helper, 605, 10);
        LegacyDungeonPiece.buildNow(level, b, LegacyDungeonPiece.DungeonType.INCA_PYRAMID);

        // 41x31 floor plate corners (stone bricks, orig :3769-3771).
        helper.assertTrue(level.getBlockState(b).is(Blocks.STONE_BRICKS)
                        && level.getBlockState(b.offset(40, 0, 30)).is(Blocks.STONE_BRICKS),
                "41x31 stone-brick floor plate corners missing");
        // Stepped ring sample: layer j=9 corner cell in the wall palette.
        BlockState ring9 = level.getBlockState(b.offset(9, 9, 9));
        helper.assertTrue(ring9.is(Blocks.STONE) || ring9.is(Blocks.COBBLESTONE)
                        || ring9.is(Blocks.MOSSY_COBBLESTONE),
                "step-ring layer 9 corner not in the stone/cobble/mossy palette, got " + ring9);

        // 4 ramps: outer-end torches on both rails (orig :3790-3797; walk
        // formulae West -10, East +50, North/South lanes at x = B.x+18/+22).
        for (BlockPos torch : new BlockPos[]{
                b.offset(-10, 2, 13), b.offset(-10, 2, 17),   // west ramp ends
                b.offset(50, 2, 13), b.offset(50, 2, 17),     // east
                b.offset(18, 2, -10), b.offset(22, 2, -10),   // north
                b.offset(18, 2, 40), b.offset(22, 2, 40)}) {  // south
            helper.assertTrue(level.getBlockState(torch).is(Blocks.TORCH),
                    "ramp end torch missing at " + torch);
        }
        // PN-018 support pillars: outer end = one stone at ground level; the
        // m=10 column (west ramp, x=B.x) fills by+1..by+5 down to the plate.
        for (int lane = 13; lane <= 17; lane++) {
            helper.assertTrue(level.getBlockState(b.offset(-10, 0, lane)).is(Blocks.STONE),
                    "west ramp outer support pillar missing at lane z+" + lane);
        }
        for (int j = 1; j <= 5; j++) {
            helper.assertTrue(level.getBlockState(b.offset(0, j, 15)).is(Blocks.STONE),
                    "west ramp mid support pillar should reach the ground (PN-018), gap at y+" + j);
        }

        // Temple T = B + (10,10,10).
        BlockPos t = b.offset(10, 10, 10);
        // Lit-lamp checkerboard on the full wall perimeter at T.y+6.
        for (int i = 0; i < 21; i++) {
            for (int k = 0; k < 11; k++) {
                boolean perimeter = i == 0 || k == 0 || i == 20 || k == 10;
                if (!perimeter) continue;
                BlockState state = level.getBlockState(t.offset(i, 6, k));
                if ((i + k) % 2 == 1) {
                    helper.assertTrue(state.is(Blocks.REDSTONE_LAMP)
                                    && state.getValue(BlockStateProperties.LIT),
                            "checkerboard cell (" + i + "," + k + ") should be a LIT redstone lamp"
                                    + " (orig :3907-3915), got " + state);
                } else {
                    helper.assertTrue(!state.is(Blocks.REDSTONE_LAMP),
                            "checkerboard cell (" + i + "," + k + ") should not be a lamp");
                }
            }
        }
        // 5 water altars: cobble pad + water centre at T.y+1 (orig :3929-3933).
        for (BlockPos altarCentre : new BlockPos[]{
                t.offset(1, 1, 1), t.offset(19, 1, 9), t.offset(1, 1, 9),
                t.offset(19, 1, 1), t.offset(10, 1, 5)}) {
            helper.assertTrue(level.getBlockState(altarCentre).is(Blocks.WATER),
                    "water altar centre missing at " + altarCentre);
        }
        helper.assertTrue(level.getBlockState(t.offset(10 + 1, 1, 5 + 1)).is(Blocks.COBBLESTONE),
                "centre altar cobble pad missing (makepoolalter, orig :3954-3961)");
        // 4 Creeper Repellents (orig :3934-3937).
        for (int[] d : new int[][]{{9, 4}, {11, 6}, {9, 6}, {11, 4}}) {
            helper.assertTrue(level.getBlockState(t.offset(d[0], 2, d[1]))
                            .is(ModBlocks.CREEPER_REPELLENT.get()),
                    "Creeper Repellent missing at temple offset (" + d[0] + ",2," + d[1] + ")");
        }
        // Molenoid spawner (orig :3938-3942).
        assertSpawner(helper, level, t.offset(8, 1, 5), ModEntities.ENTITY_MOLENOID.get());
        // Trapdoor + ladder shaft (orig :3943-3950).
        BlockState trapdoor = level.getBlockState(t.offset(12, 1, 5));
        helper.assertTrue(trapdoor.is(Blocks.OAK_TRAPDOOR)
                        && trapdoor.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST,
                "oak trapdoor facing EAST missing at the shaft head, got " + trapdoor);
        helper.assertTrue(level.getBlockState(t.offset(12, 0, 5)).isAir(),
                "shaft floor hole missing under the trapdoor");
        for (int j = 1; j <= 9; j++) {
            BlockState ladder = level.getBlockState(t.offset(12, -j, 5));
            helper.assertTrue(ladder.is(Blocks.LADDER)
                            && ladder.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
                    "ladder facing NORTH missing at shaft depth " + j);
            helper.assertTrue(level.getBlockState(t.offset(12, -j, 6)).is(Blocks.COBBLESTONE),
                    "ladder backing cobble missing at shaft depth " + j);
        }

        // 24 graves: beds, headstone, slabs, north-facing chest per grave.
        int[] graveRows = {5, 10, 20, 25};
        int ghostSpawners = 0;
        for (int gz : graveRows) {
            int s = gz <= 10 ? 1 : -1; // dir 1 rows extend +Z, dir 3 rows -Z (orig :3979-4042)
            for (int gx = 5; gx < 36; gx += 6) {
                BlockPos plot = b.offset(gx, 0, gz);
                helper.assertTrue(level.getBlockState(plot.offset(0, 1, 0)).is(Blocks.STONE),
                        "grave headstone missing at " + plot);
                helper.assertTrue(level.getBlockState(plot.offset(-1, 1, 0)).is(Blocks.POPPY)
                                && level.getBlockState(plot.offset(1, 1, 0)).is(Blocks.POPPY),
                        "grave poppy bed missing at " + plot);
                helper.assertTrue(level.getBlockState(plot.offset(0, 1, s)).is(Blocks.SMOOTH_STONE_SLAB)
                                && level.getBlockState(plot.offset(0, 1, 2 * s)).is(Blocks.SMOOTH_STONE_SLAB),
                        "grave slabs missing at " + plot);
                assertLootChest(helper, level, plot.offset(0, 1, -s),
                        "chests/inca_pyramid", Direction.NORTH);
                BlockPos spawnerPos = plot.offset(0, 2, 0);
                if (level.getBlockState(spawnerPos).is(Blocks.SPAWNER)) {
                    helper.assertTrue(spawnerMob(level, spawnerPos)
                                    .equals(entityId(ModEntities.GHOST.get())),
                            "grave spawner at " + spawnerPos + " should spawn the Ghost");
                    ghostSpawners++;
                }
            }
        }

        // Builds 2-15: Ghost-roll sampling only (statistics in the javadoc).
        for (int buildIdx = 1; buildIdx < 15; buildIdx++) {
            BlockPos bn = b.offset(0, 0, buildIdx * 160);
            LegacyDungeonPiece.buildNow(level, bn, LegacyDungeonPiece.DungeonType.INCA_PYRAMID);
            for (int gz : graveRows) {
                for (int gx = 5; gx < 36; gx += 6) {
                    if (level.getBlockState(bn.offset(gx, 2, gz)).is(Blocks.SPAWNER)) {
                        ghostSpawners++;
                    }
                }
            }
        }
        helper.assertTrue(ghostSpawners >= 60 && ghostSpawners <= 180,
                "Ghost spawner total " + ghostSpawners + "/360 outside [60,180] for the documented "
                        + "1/3 per-grave roll (orig :4000-4006)");

        // Chest loot distribution.
        assertLootStats(helper, rollLoot(helper, "chests/inca_pyramid", 300), 10, 14,
                Set.of("minecraft:golden_sword", "minecraft:golden_boots", "minecraft:golden_leggings",
                        "minecraft:golden_helmet", "minecraft:golden_chestplate", "minecraft:dandelion",
                        "minecraft:poppy", "minecraft:gold_nugget", "minecraft:gold_ingot",
                        "minecraft:experience_bottle", "orespawn:corn_cob",
                        "orespawn:experience_catcher", "minecraft:bone", "minecraft:gold_block"),
                true, "inca_pyramid loot (480-weight table)");
        helper.succeed();
    }

    // =====================================================================
    // i139-kyuubidungeon-mining — Kyuubi Dungeon content
    // =====================================================================

    /**
     * Checklist i139-kyuubidungeon-mining (WGEN-042 pool; spec
     * phase_d_reports/d6_extraction/kyuubi_dungeon_spec.md). Far region K=606.
     * All geometry is deterministic (the generator draws no RNG,
     * KyuubiDungeonGenerator.java:36-43), so one buildNow suffices; the five
     * chest fill formulae live in the loot tables and are asserted by rolling
     * the tables directly. Positions from KyuubiDungeonGenerator.java (orig
     * GD:1095-1361), origin O, boss room R = O + (15, −20, −15) (orig
     * :1160-1174 — depth is FIXED at 20, :1099-1105):</p>
     * <ul>
     *   <li>sealed hut, entry only via the 1x1 roof hole (orig :1116-1134);</li>
     *   <li>22-deep shaft with the 2-layer water brake at −20/−21 and stone
     *       basin at −22 (orig :1135-1159);</li>
     *   <li>lava-walled corridor (side walls j=1..3 standing lava, orig
     *       :1175-1192);</li>
     *   <li>boss room with the two-tier lava altar + 3-Kyuubi spawner stack +
     *       chest (addkyuubi, orig :1219-1258) and the 4-tier ziggurat with
     *       the 8-Blaze spawner ring (addblaze, orig :1260-1336);</li>
     *   <li>kyuubi chest rolls 7-13 of the 110-weight list (GD:53 + :1256)
     *       and the four wall chests roll DIFFERENT counts 4-8/3-7/5-9/6-10 of
     *       the 130-weight blaze list incl. minecraft:blaze_spawn_egg (GD:54,
     *       fills :1341/:1347/:1353/:1359; meta-61 egg = Blaze). n=300 per
     *       table: endpoint miss ≤ 2·(6/7)^300 ≈ 2e-20 (worst width 7), so the
     *       four observed [min,max] ranges are exactly the four documented
     *       ranges; blaze-egg coverage worst case e^(−900·10/130) ≈ 1e-30.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i139_kyuubi_dungeon_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = farOrigin(helper, 606, 30);
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.KYUUBI_DUNGEON);

        // Sealed hut: roof slab with only the centre hole; all wall cells sandstone.
        helper.assertTrue(level.getBlockState(o.offset(2, 5, 2)).isAir(),
                "1x1 roof entry hole missing (orig :1122)");
        for (int i = 0; i < 5; i++) {
            for (int k = 0; k < 5; k++) {
                if (i == 2 && k == 2) continue;
                helper.assertTrue(level.getBlockState(o.offset(i, 5, k)).is(Blocks.SANDSTONE),
                        "hut roof cell (" + i + "," + k + ") should be sandstone");
                boolean perimeter = i == 0 || k == 0 || i == 4 || k == 4;
                if (perimeter) {
                    // No door anywhere: every perimeter wall cell at j=1 and j=2 solid.
                    helper.assertTrue(level.getBlockState(o.offset(i, 1, k)).is(Blocks.SANDSTONE)
                                    && level.getBlockState(o.offset(i, 2, k)).is(Blocks.SANDSTONE),
                            "hut wall must be sealed at (" + i + "," + k + ") (orig :1123-1134)");
                }
            }
        }
        // Shaft + water brake + basin (orig :1135-1159).
        helper.assertTrue(level.getBlockState(o.offset(0, -10, 0)).is(Blocks.STONE),
                "shaft perimeter should be stone");
        helper.assertTrue(level.getBlockState(o.offset(2, -10, 2)).isAir(),
                "shaft bore should be air");
        helper.assertTrue(level.getBlockState(o.offset(2, -20, 2)).is(Blocks.WATER)
                        && level.getBlockState(o.offset(2, -21, 2)).is(Blocks.WATER),
                "2-layer water brake missing at depth 20-21");
        helper.assertTrue(level.getBlockState(o.offset(2, -22, 2)).is(Blocks.STONE),
                "stone basin floor missing at depth 22");

        // Lava-walled corridor (orig :1175-1192): sides lava, bore air.
        for (int i = 5; i <= 15; i++) {
            helper.assertTrue(level.getBlockState(o.offset(i, -18, 0)).is(Blocks.LAVA)
                            && level.getBlockState(o.offset(i, -18, 4)).is(Blocks.LAVA),
                    "corridor side walls should be standing lava at x+" + i);
            helper.assertTrue(level.getBlockState(o.offset(i, -18, 2)).isAir(),
                    "corridor bore should be air at x+" + i);
        }

        // Boss room shell (orig :1160-1174): netherrack floor + ceiling samples.
        helper.assertTrue(level.getBlockState(o.offset(25, -20, 0)).is(Blocks.NETHERRACK),
                "boss room floor should be netherrack");
        helper.assertTrue(level.getBlockState(o.offset(25, -3, 0)).is(Blocks.NETHERRACK),
                "boss room ceiling should be netherrack");

        // Kyuubi altar A = R + (5, +1, +19) = O + (20, -19, +4) (orig :1201, :1219-1258).
        BlockPos altar = o.offset(20, -19, 4);
        helper.assertTrue(level.getBlockState(altar).is(Blocks.NETHER_BRICKS),
                "altar base rim should be nether bricks");
        helper.assertTrue(level.getBlockState(altar.offset(4, 0, 4)).is(Blocks.LAVA),
                "altar tier interior should be lava");
        for (int j = 0; j < 3; j++) {
            assertSpawner(helper, level, altar.offset(4, 2 + j, 4), ModEntities.ENTITY_KYUUBI.get());
        }
        assertLootChest(helper, level, altar.offset(4, 5, 4), "chests/kyuubi_dungeon", Direction.NORTH);

        // Blaze ziggurat Z = R + (10, +1, +5) = O + (25, -19, -10) (orig :1202, :1260-1361).
        BlockPos zig = o.offset(25, -19, -10);
        helper.assertTrue(level.getBlockState(zig).is(Blocks.OBSIDIAN),
                "ziggurat tier-1 should be solid obsidian");
        for (int j = 0; j < 2; j++) {
            BlockPos ringCentre = zig.offset(3, 13 + j, 3);
            assertSpawner(helper, level, ringCentre.west(), EntityType.BLAZE);
            assertSpawner(helper, level, ringCentre.east(), EntityType.BLAZE);
            assertSpawner(helper, level, ringCentre.north(), EntityType.BLAZE);
            assertSpawner(helper, level, ringCentre.south(), EntityType.BLAZE);
        }
        assertLootChest(helper, level, zig.offset(0, 4, 3), "chests/kyuubi_dungeon_blaze_west", Direction.WEST);
        assertLootChest(helper, level, zig.offset(3, 4, 0), "chests/kyuubi_dungeon_blaze_north", Direction.NORTH);
        assertLootChest(helper, level, zig.offset(3, 4, 6), "chests/kyuubi_dungeon_blaze_south", Direction.SOUTH);
        assertLootChest(helper, level, zig.offset(6, 4, 3), "chests/kyuubi_dungeon_blaze_east", Direction.EAST);

        // Loot distributions: kyuubi 7-13 of the 110 list; blaze chests get
        // FOUR tables that differ only in rolls (4-8 / 3-7 / 5-9 / 6-10).
        Set<String> kyuubiList = Set.of("minecraft:redstone", "minecraft:redstone_block",
                "minecraft:quartz", "minecraft:coal", "orespawn:nightmare_sword",
                "orespawn:poison_sword", "orespawn:kyuubi_spawn_egg");
        assertLootStats(helper, rollLoot(helper, "chests/kyuubi_dungeon", 300), 7, 13,
                kyuubiList, true, "kyuubi altar chest");
        Set<String> blazeList = Set.of("minecraft:blaze_rod", "minecraft:blaze_powder",
                "minecraft:fire_charge", "minecraft:flint_and_steel", "orespawn:lavaeel_helmet",
                "orespawn:lavaeel_chestplate", "orespawn:lavaeel_leggings", "orespawn:lavaeel_boots",
                "minecraft:blaze_spawn_egg");
        String[][] blazeTables = {
                {"chests/kyuubi_dungeon_blaze_west", "4", "8"},
                {"chests/kyuubi_dungeon_blaze_north", "3", "7"},
                {"chests/kyuubi_dungeon_blaze_south", "5", "9"},
                {"chests/kyuubi_dungeon_blaze_east", "6", "10"}};
        for (String[] spec : blazeTables) {
            Set<String> observed = assertLootStats(helper, rollLoot(helper, spec[0], 300),
                    Integer.parseInt(spec[1]), Integer.parseInt(spec[2]), blazeList, true, spec[0]);
            helper.assertTrue(observed.contains("minecraft:blaze_spawn_egg"),
                    spec[0] + " must include the blaze-egg entry (orig meta-61 egg, GD:54)");
        }
        helper.succeed();
    }

    // =====================================================================
    // i140-robot-lab-islands — Robot Lab content + working redstone tableau
    // =====================================================================

    /**
     * Checklist i140-robot-lab-islands (robot_lab_audit_spec.md §18 fixes;
     * FIX_LOG.md:724). Far region K=607. Positions from generateRobotLab
     * (LegacyDungeonPiece.java:1057-1392, orig GD:4044-4351): entry origin
     * E = O + (−5, 0, −25) (the documented −5/−25 recentring, :1061-1066),
     * hangar H = E + (−10, 0, +19) (makeRoboMain x-shift, :1131-1133, :1187).
     * <ul>
     *   <li>BOTH rear Robo-Sniper spawners behind the hangar wall — the two
     *       z = length−1 pillars are rebuilt AFTER the hangar so the shared-
     *       wall spawners survive the door carve (:1135-1144, audit §19.3);
     *       Robo-Sniper = Robot5 (OSM:3719);</li>
     *   <li>altar spawns Robo-Pounder = Robot2 (OSM:3695, audit §18 item 1 —
     *       the ROBOT_4 swap was the port bug), treasure room Robo-Warrior =
     *       Robot4 (OSM:3711), pillars Robo-Sniper = Robot5;</li>
     *   <li>railway: golden (powered) rails on rows z+2/6/10 with UNPOWERED
     *       floor levers between the tracks; plain rails elsewhere
     *       (makeRoboRailway :1262-1279, orig GD:4260-4293, audit §18 item 6);</li>
     *   <li>assembly line: sticky pistons FACING SOUTH under white carpet with
     *       floor levers generated PRE-POWERED (meta 13, orig GD:4302-4304,
     *       audit §18 item 7) — and the crusher actually extends once its
     *       already-ON lever's signal is delivered (the generator's flag-2
     *       writes send no updates; the test kicks the neighbour update and
     *       lets a FORCED-ticket-ticking chunk process the piston block event,
     *       mirroring the first player-caused update in game);</li>
     *   <li>entry doors face NORTH (1.7.10 door dir=3, :1098-1121) and open
     *       from the wall buttons (:1122-1129) — asserted by powering each
     *       button and replaying ButtonBlock.press's two neighbour updates;</li>
     *   <li>chests roll 10-14 stacks of the 755-weight 23-entry list
     *       (chests/robot_lab.json <- orig RobotContentsList + 10+nextInt(5)
     *       fills GD:4344/4349) with NO droppers/dispensers/clocks (audit §18
     *       item 8). n=300 (avg 12 stacks ⇒ ≈3600 samples): roll endpoints
     *       2·(4/5)^300 ≈ 3e-29; full 21-distinct-id coverage, worst weight
     *       10/755 → miss ≤ 21·e^(−3600·10/755) ≈ 4e-20.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i140_robot_lab_content_and_redstone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = farOrigin(helper, 607, 10);
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.ROBOT_LAB);
        BlockPos e = o.offset(-5, 0, -25);
        BlockPos h = e.offset(-10, 0, 19);

        // Rear sniper pair on the shared entry/hangar wall (z = oz+19).
        assertSpawner(helper, level, e.offset(1, 1, 19), ModEntities.ROBOT_5.get());
        assertSpawner(helper, level, e.offset(8, 1, 19), ModEntities.ROBOT_5.get());
        // Altar: two Robo-Pounder (Robot2) spawners (LegacyDungeonPiece.java:1242-1247).
        assertSpawner(helper, level, h.offset(14, 2, 9), ModEntities.ROBOT_2.get());
        assertSpawner(helper, level, h.offset(15, 2, 10), ModEntities.ROBOT_2.get());
        // Treasure room: Robo-Warrior (Robot4) spawner (:1336-1340).
        assertSpawner(helper, level, h.offset(19, 1, 19), ModEntities.ROBOT_4.get());
        // A front entry pillar sniper for the pillar rooms (:1139-1144).
        assertSpawner(helper, level, e.offset(1, 1, 6), ModEntities.ROBOT_5.get());

        // Railway (:1262-1279): boost rows powered rails + unpowered floor levers.
        for (int dz : new int[]{2, 6, 10}) {
            helper.assertTrue(level.getBlockState(h.offset(3, 1, 10 + dz)).is(Blocks.POWERED_RAIL)
                            && level.getBlockState(h.offset(6, 1, 10 + dz)).is(Blocks.POWERED_RAIL),
                    "golden (powered) rail row missing at railway dz=" + dz);
            for (int dx : new int[]{4, 5}) {
                BlockState lever = level.getBlockState(h.offset(dx, 1, 10 + dz));
                helper.assertTrue(lever.is(Blocks.LEVER)
                                && lever.getValue(BlockStateProperties.ATTACH_FACE)
                                == net.minecraft.world.level.block.state.properties.AttachFace.FLOOR
                                && !lever.getValue(BlockStateProperties.POWERED),
                        "railway floor lever (unpowered) missing at dx=" + dx + " dz=" + dz);
            }
        }
        helper.assertTrue(level.getBlockState(h.offset(3, 1, 10)).is(Blocks.RAIL),
                "plain rail row missing at railway dz=0");

        // Assembly line (:1294-1317): piston row k=1, lever row k=0.
        BlockPos piston = h.offset(26, 2, 5);
        BlockState pistonState = level.getBlockState(piston);
        helper.assertTrue(pistonState.is(Blocks.STICKY_PISTON)
                        && pistonState.getValue(BlockStateProperties.FACING) == Direction.SOUTH,
                "assembly sticky piston should face SOUTH (orig meta 3, GD:4299), got " + pistonState);
        helper.assertTrue(level.getBlockState(piston.above()).is(Blocks.WHITE_CARPET),
                "white carpet missing above the assembly piston (orig meta 0, GD:4300)");
        BlockPos assemblyLever = h.offset(26, 2, 4);
        BlockState leverOn = level.getBlockState(assemblyLever);
        helper.assertTrue(leverOn.is(Blocks.LEVER) && leverOn.getValue(BlockStateProperties.POWERED),
                "assembly lever should generate PRE-POWERED (orig meta 13, GD:4302-4304)");
        helper.assertTrue(level.getBlockState(h.offset(24, 1, 5)).is(Blocks.QUARTZ_STAIRS),
                "assembly quartz stair step missing (orig meta 1, GD:4298)");

        // Entry doors + buttons (:1098-1129).
        BlockState eastDoor = level.getBlockState(e.offset(5, 1, 0));
        BlockState westDoor = level.getBlockState(e.offset(4, 1, 0));
        helper.assertTrue(eastDoor.is(Blocks.IRON_DOOR)
                        && eastDoor.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH
                        && eastDoor.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.LEFT,
                "east entry door should face NORTH hinge LEFT, got " + eastDoor);
        helper.assertTrue(westDoor.is(Blocks.IRON_DOOR)
                        && westDoor.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH
                        && westDoor.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.RIGHT,
                "west entry door should face NORTH hinge RIGHT, got " + westDoor);
        BlockPos westButton = e.offset(3, 2, -1);
        BlockPos eastButton = e.offset(6, 2, -1);
        for (BlockPos button : new BlockPos[]{westButton, eastButton}) {
            BlockState state = level.getBlockState(button);
            helper.assertTrue(state.is(Blocks.STONE_BUTTON)
                            && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
                    "entry wall button missing/misfacing at " + button + ", got " + state);
        }
        // Press both buttons: power the state, then replay ButtonBlock.press's
        // updates (button pos + its supporting wall block) so the doors hear it.
        for (BlockPos button : new BlockPos[]{westButton, eastButton}) {
            level.setBlock(button, level.getBlockState(button)
                    .setValue(BlockStateProperties.POWERED, true), 3);
            level.updateNeighborsAt(button, Blocks.STONE_BUTTON);
            level.updateNeighborsAt(button.south(), Blocks.STONE_BUTTON); // support block behind the button
        }
        helper.assertTrue(level.getBlockState(e.offset(5, 1, 0)).getValue(BlockStateProperties.OPEN)
                        && level.getBlockState(e.offset(4, 1, 0)).getValue(BlockStateProperties.OPEN),
                "wall buttons should open both NORTH-facing entry doors");

        // Chest bindings + loot distribution.
        assertLootChest(helper, level, h.offset(17, 1, 19), "chests/robot_lab", Direction.NORTH);
        assertLootChest(helper, level, h.offset(15, 1, 19), "chests/robot_lab", Direction.NORTH);
        Set<String> robotList = Set.of("minecraft:redstone", "minecraft:repeater",
                "minecraft:minecart", "minecraft:fire_charge", "minecraft:hopper_minecart",
                "minecraft:redstone_block", "minecraft:rail", "minecraft:detector_rail",
                "minecraft:sticky_piston", "minecraft:piston", "minecraft:redstone_torch",
                "minecraft:tnt", "minecraft:lever", "orespawn:ant_robot_kit",
                "orespawn:spider_robot_kit", "minecraft:iron_door", "minecraft:oak_button",
                "minecraft:iron_bars", "minecraft:comparator", "minecraft:activator_rail",
                "orespawn:ray_gun");
        Set<String> observed = assertLootStats(helper,
                rollLoot(helper, "chests/robot_lab", 300), 10, 14, robotList, true, "robot_lab loot");
        for (String banned : new String[]{"minecraft:dropper", "minecraft:dispenser", "minecraft:clock"}) {
            helper.assertTrue(!observed.contains(banned),
                    "robot_lab loot must not contain " + banned + " (audit §18 item 8)");
        }

        // Crusher piston extends once its pre-powered lever's signal is
        // delivered (block events need a block-ticking chunk: FORCED ticket).
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkPos ticketPos = new ChunkPos(piston);
        chunkSource.addRegionTicket(TicketType.FORCED, ticketPos, 3, ticketPos);
        // Kick twice: a piston block event queued before the FORCED chunks
        // reach block-ticking is dropped by runBlockEvents, so the second kick
        // guarantees one lands after the ticking futures resolve.
        helper.runAfterDelay(20, () -> level.updateNeighborsAt(assemblyLever, Blocks.LEVER));
        helper.runAfterDelay(45, () -> level.updateNeighborsAt(assemblyLever, Blocks.LEVER));
        helper.runAfterDelay(70, () -> {
            try {
                BlockState extended = level.getBlockState(piston);
                helper.assertTrue(extended.is(Blocks.STICKY_PISTON)
                                && extended.getValue(BlockStateProperties.EXTENDED),
                        "assembly crusher should extend from its pre-powered lever, got " + extended);
                helper.assertTrue(level.getBlockState(piston.south()).is(Blocks.PISTON_HEAD),
                        "extended crusher head missing south of the piston");
            } finally {
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 3, ticketPos);
            }
            helper.succeed();
        });
    }

    // =====================================================================
    // i141-hospital-end — Ender Dragon Hospital content
    // =====================================================================

    /**
     * Checklist i141-hospital-end (WGEN-042 pool; spec d6_extraction/
     * hospital_monster_island_spec.md §A). Far region K=608. Positions from
     * HospitalGenerator.java (orig GD:2815-2991), origin O:
     * <ul>
     *   <li>10x10 cage: iron-bar perimeter, obsidian corner posts, end-stone
     *       floor and j=6 rim (orig :2824-2852);</li>
     *   <li>exactly 4 End Crystals embedded in the four bedrock corner caps at
     *       y+9 (entity-then-block order kept, orig :2906-2921, spec S3) and
     *       NO Ender Dragon anywhere (spec §A2 — the method spawns only
     *       crystals);</li>
     *   <li>8 spawners: 4 Ender Reaper on the j=9 dome-ring corners (orig
     *       :2922-2953) + 4 Nightmare (= PitchBlack, OSM:4023-4027) in the
     *       cage corners (orig :2954-2985);</li>
     *   <li>the single chest — the generator places it at the cage CENTRE
     *       (x+4, y+1, z+4), orig :2986-2990; the checklist's "corner" is the
     *       manual phrasing for the same one chest — bound to
     *       chests/hospital rolling 6-10 stacks of the 210-weight 6-entry
     *       list (GD:44 + 6+nextInt(5) :2989). n=300: roll endpoints
     *       2·(4/5)^300 ≈ 3e-29; full coverage min weight 35/210 trivial.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i141_hospital_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = farOrigin(helper, 608, 10);

        // TF-023 harness race (proven fix pattern: StructureTestsC red-ant
        // i165, FIX_LOG TF-023 CLOSED): the End Crystals are spawnEntity'd
        // during buildNow into freshly force-loaded chunks whose entity
        // sections stay HIDDEN until the queued FullChunkStatus promotion
        // pumps, so the crystal census could read 0 while the crystals WERE in
        // section storage. Pin the cage + query area (the census AABB spans
        // o-8..o+18) under a FORCED ticket and sync-load it a few ticks BEFORE
        // the build so the spawns land in TRACKED (queryable) sections.
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkPos ticketPos = new ChunkPos(o.offset(4, 0, 4));
        chunkSource.addRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
        ChunkPos minC = new ChunkPos(o.offset(-8, 0, -8));
        ChunkPos maxC = new ChunkPos(o.offset(18, 0, 18));
        for (int ccx = minC.x; ccx <= maxC.x; ccx++) {
            for (int ccz = minC.z; ccz <= maxC.z; ccz++) {
                level.getChunk(ccx, ccz); // blocking FULL load now; the queued
                                          // visibility promotion drains between
                                          // ticks, before the delayed build
            }
        }
        helper.runAfterDelay(5, () -> {
            try {
                hospitalBuildAndAsserts(helper, level, o);
            } catch (Throwable t) {
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
                throw t;
            }
            // TF-023 addendum (i141 triage, 2026-08-10, diagnostic-proven):
            // the fixed preload + 5-tick delay is NOT always enough — in this
            // batch the perch chunks still reported entitiesLoaded=false at
            // t=5 and even a manual addFreshEntity in the assert context was
            // added=true yet queryable same-tick=0, i.e. the queued
            // FullChunkStatus promotion that flips entity-SECTION visibility
            // (ChunkMap.onFullChunkStatusChange -> PersistentEntitySection
            // Manager.updateChunkStatus) had still not pumped for these far
            // chunks under batch load. The crystals sit in section storage
            // either way, so POLL the census until the promotion lands (the
            // FORCED ticket stays up during the poll and is dropped on the
            // passing tick; on a timeout the server teardown reclaims it).
            helper.succeedWhen(() -> {
                hospitalEntityAsserts(helper, level, o);
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
            });
        });
    }

    /** Build + block/loot asserts of {@link #i141_hospital_content} (body
     *  unchanged by the TF-023 infra fix — only moved behind the chunk
     *  pre-ticketing; the entity census lives in
     *  {@link #hospitalEntityAsserts} and is POLLED, see the addendum note
     *  in the test method). */
    private static void hospitalBuildAndAsserts(GameTestHelper helper, ServerLevel level,
                                                BlockPos o) {
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.HOSPITAL);

        // Cage shell samples (orig :2824-2852).
        helper.assertTrue(level.getBlockState(o.offset(0, 3, 4)).is(Blocks.IRON_BARS),
                "cage perimeter should be iron bars");
        helper.assertTrue(level.getBlockState(o.offset(0, 3, 0)).is(Blocks.OBSIDIAN),
                "cage corner posts should be obsidian");
        helper.assertTrue(level.getBlockState(o.offset(4, 0, 4)).is(Blocks.END_STONE),
                "cage floor should be end stone");
        helper.assertTrue(level.getBlockState(o.offset(0, 6, 5)).is(Blocks.END_STONE),
                "cage j=6 rim should be end stone (rim rule beats the corner obsidian)");
        // Bedrock crystal caps (orig :2909/:2913/:2917/:2921).
        for (int[] d : new int[][]{{0, 0}, {0, 9}, {9, 0}, {9, 9}}) {
            helper.assertTrue(level.getBlockState(o.offset(d[0], 9, d[1])).is(Blocks.BEDROCK),
                    "crystal perch bedrock cap missing at corner (" + d[0] + "," + d[1] + ")");
        }
        // 8 spawners.
        assertSpawner(helper, level, o.offset(3, 9, 3), ModEntities.ENDER_REAPER.get());
        assertSpawner(helper, level, o.offset(3, 9, 6), ModEntities.ENDER_REAPER.get());
        assertSpawner(helper, level, o.offset(6, 9, 3), ModEntities.ENDER_REAPER.get());
        assertSpawner(helper, level, o.offset(6, 9, 6), ModEntities.ENDER_REAPER.get());
        assertSpawner(helper, level, o.offset(1, 1, 1), ModEntities.PITCH_BLACK.get());
        assertSpawner(helper, level, o.offset(1, 1, 8), ModEntities.PITCH_BLACK.get());
        assertSpawner(helper, level, o.offset(8, 1, 1), ModEntities.PITCH_BLACK.get());
        assertSpawner(helper, level, o.offset(8, 1, 8), ModEntities.PITCH_BLACK.get());
        // The one chest + its table.
        assertLootChest(helper, level, o.offset(4, 1, 4), "chests/hospital", null);
        assertLootStats(helper, rollLoot(helper, "chests/hospital", 300), 6, 10,
                Set.of("minecraft:ender_chest", "minecraft:diamond_block", "minecraft:dragon_egg",
                        "orespawn:block_ender_pearl", "minecraft:ender_pearl", "minecraft:ender_eye"),
                true, "hospital loot (210-weight table)");
    }

    /** Entity census of {@link #i141_hospital_content} — polled via
     *  {@code succeedWhen} until the entity-section visibility promotion
     *  lands (TF-023 addendum; see the note in the test method). Once the
     *  crystals are visible the sections are accessible, so the dragon
     *  absence assert on the same box carries its original strength. */
    private static void hospitalEntityAsserts(GameTestHelper helper, ServerLevel level,
                                              BlockPos o) {
        AABB area = new AABB(o.getX() - 8, o.getY() - 2, o.getZ() - 8,
                o.getX() + 18, o.getY() + 14, o.getZ() + 18);
        int crystals = level.getEntities(EntityType.END_CRYSTAL, area, c -> true).size();
        helper.assertTrue(crystals == 4,
                "expected exactly 4 End Crystals on the perches (orig :2906-2921), got " + crystals);
        int dragons = level.getEntities(EntityType.ENDER_DRAGON, area, d -> true).size();
        helper.assertTrue(dragons == 0,
                "the Hospital must NOT spawn an Ender Dragon (spec §A2), found " + dragons);
    }

    // =====================================================================
    // i142-monsterisland-overworld — Monster Island content + biome gate
    // =====================================================================

    /**
     * Checklist i142-monsterisland-overworld (WGEN-042 pool, PN-019; spec
     * d6_extraction/hospital_monster_island_spec.md §B). Far region K=609:
     * build 1 sits in a test-built water sheet for the waterline asserts;
     * builds 2-40 (z step 32) sample the 50/50 spawner-mob pick.
     * <ul>
     *   <li>PN-019 biome half (PARITY_NOTES.md:263): monster_island.json's
     *       biomes field is literally "minecraft:ocean" — plain ocean only,
     *       never deep/frozen/warm. Asserted on the registered structure's
     *       resolved HolderSet: exactly one biome, minecraft:ocean;</li>
     *   <li>anchor mode OCEAN_SURFACE — the water-surface block itself
     *       (posY−1, OSW:1410; LegacyDungeonPiece.java:174);</li>
     *   <li>lens island embedded at the waterline: sand at origin level
     *       replaces surface water with stone beneath (orig :5181-5196), the
     *       surrounding sheet stays water; canopy palm with persistent leaves
     *       (orig :5197-5209 + the meta-0 decay mapping);</li>
     *   <li>4 spawners all of ONE 50/50 pick between Sea Monster and Sea
     *       Viper — the FIRST RNG draw (orig :5176-5180, spec S7); per build
     *       all four ids equal, across 40 level-RNG builds both picks appear
     *       (P(one-sided) = 2·(1/2)^40 ≈ 1.8e-12);</li>
     *   <li>2 trunk chests rolling 4-8 stacks of the 450-weight 14-entry
     *       list (chests/monster_island.json <- GD:30 + 4+nextInt(5) :5233).
     *       n=300: endpoints 2·(4/5)^300 ≈ 3e-29; coverage min weight 25/450
     *       over ≥1800 samples → miss ≤ 14·e^(−100) ≈ 0.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i142_monster_island_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = farOrigin(helper, 609, 10);

        // PN-019 — plain-ocean-only biome binding.
        Structure island = registeredStructure(helper, "monster_island");
        helper.assertTrue(island.biomes().size() == 1
                        && island.biomes().stream().anyMatch(h -> h.is(Biomes.OCEAN)),
                "monster_island must bind exactly minecraft:ocean (PN-019 — no deep/frozen/warm)");
        helper.assertTrue(LegacyDungeonPiece.DungeonType.MONSTER_ISLAND.placement
                        == LegacyDungeonPiece.DungeonType.PlacementMode.OCEAN_SURFACE,
                "MONSTER_ISLAND must anchor on the water-surface block (OSW:1410)");

        // Test-built sea: surface sheet at origin level (the OCEAN_SURFACE
        // anchor IS the water-surface block) plus one layer below.
        for (int x = -7; x <= 7; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(o.offset(x, 0, z), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(o.offset(x, -1, z), Blocks.WATER.defaultBlockState(), 2);
            }
        }
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.MONSTER_ISLAND);

        // Lens island replaces the surface water; the sheet around it stays.
        helper.assertTrue(level.getBlockState(o).is(Blocks.SAND)
                        && level.getBlockState(o.offset(-5, 0, 0)).is(Blocks.SAND)
                        && level.getBlockState(o.offset(5, 0, 0)).is(Blocks.SAND)
                        && level.getBlockState(o.offset(0, 0, 3)).is(Blocks.SAND),
                "lens island sand layer missing at the waterline (orig :5181-5196)");
        helper.assertTrue(level.getBlockState(o.below()).is(Blocks.STONE),
                "island stone underlayer missing");
        helper.assertTrue(level.getBlockState(o.offset(0, 0, 4)).is(Blocks.WATER)
                        && level.getBlockState(o.offset(6, 0, 0)).is(Blocks.WATER),
                "the island must sit EMBEDDED in the water sheet, not clear it");
        // Canopy palm (orig :5197-5209): persistent leaves + trunk + tip.
        BlockState canopy = level.getBlockState(o.offset(2, 3, 2));
        helper.assertTrue(canopy.is(Blocks.OAK_LEAVES)
                        && canopy.getValue(BlockStateProperties.PERSISTENT),
                "canopy leaves should be persistent oak leaves (meta-0 decay mapping), got " + canopy);
        helper.assertTrue(level.getBlockState(o.offset(0, 1, 0)).is(Blocks.OAK_LOG)
                        && level.getBlockState(o.offset(0, 4, 0)).is(Blocks.OAK_LEAVES),
                "palm trunk/tip missing");

        // 4 spawners of ONE pick + 2 chests (orig :5210-5239).
        String[] picks = {entityId(ModEntities.SEA_VIPER.get()), entityId(ModEntities.SEA_MONSTER.get())};
        Set<String> observedPicks = new HashSet<>();
        observedPicks.add(assertIslandSpawners(helper, level, o, picks));
        assertLootChest(helper, level, o.offset(0, 1, -1), "chests/monster_island", null);
        assertLootChest(helper, level, o.offset(0, 1, 1), "chests/monster_island", null);

        // Builds 2-40: the 50/50 pick varies across level-RNG builds.
        for (int build = 1; build < 40; build++) {
            BlockPos bn = o.offset(0, 0, build * 32);
            LegacyDungeonPiece.buildNow(level, bn, LegacyDungeonPiece.DungeonType.MONSTER_ISLAND);
            observedPicks.add(assertIslandSpawners(helper, level, bn, picks));
        }
        helper.assertTrue(observedPicks.size() == 2,
                "both Sea Monster and Sea Viper picks should appear across 40 builds "
                        + "(orig :5176-5180 50/50), saw only " + observedPicks);

        assertLootStats(helper, rollLoot(helper, "chests/monster_island", 300), 4, 8,
                Set.of("orespawn:creeper_repellent", "orespawn:kraken_repellent", "minecraft:ink_sac",
                        "minecraft:bone", "minecraft:string", "minecraft:porkchop", "minecraft:beef",
                        "minecraft:chicken", "minecraft:cod", "minecraft:rotten_flesh",
                        "minecraft:experience_bottle", "orespawn:raw_bacon", "orespawn:raw_peacock",
                        "minecraft:oak_log"),
                true, "monster_island loot (450-weight table)");
        helper.succeed();
    }

    /** Asserts the 4 canopy spawners share one pick from {@code picks}; returns it. */
    private static String assertIslandSpawners(GameTestHelper helper, ServerLevel level,
                                               BlockPos origin, String[] picks) {
        String pick = spawnerMob(level, origin.offset(1, 3, 0));
        helper.assertTrue(pick.equals(picks[0]) || pick.equals(picks[1]),
                "island spawner pick must be Sea Viper or Sea Monster, got " + pick);
        for (BlockPos pos : new BlockPos[]{origin.offset(-1, 3, 0),
                origin.offset(0, 3, 1), origin.offset(0, 3, -1)}) {
            helper.assertTrue(level.getBlockState(pos).is(Blocks.SPAWNER)
                            && spawnerMob(level, pos).equals(pick),
                    "all four island spawners must share the single mob pick (spec S7), "
                            + pos + " diverged");
        }
        return pick;
    }

    // =====================================================================
    // i145-playpool-overworld-ocean — Play Pool content
    // =====================================================================

    /**
     * Checklist i145-playpool-overworld-ocean (WGEN-042 pool; spec
     * d6_extraction/play_pool_spec.md). Far region K=610. The generator's
     * documented anchor contract: origin = the AIR block directly above the
     * ocean surface with every offset internal (PlayPoolGenerator.java:25-31,
     * orig OSW:1146-1148 — no −1; DSB type 12 unmodified), carried by
     * PlacementMode.OCEAN_SURFACE_AIR (LegacyDungeonPiece.java:179). The test
     * builds its own sea one block below the origin and asserts, per
     * PlayPoolGenerator.java:76-101 (orig GD:1934-1957):
     * <ul>
     *   <li>the platform floats 16 above the sea: 4 Attack Squid spawners in a
     *       row at y+16 (17 above the water-surface block = 16 above the
     *       origin air block, orig :1940-1945);</li>
     *   <li>twin chests at y+17 with loot bound to the x+1 chest ONLY (orig
     *       :1946-1951 — the x+2 chest is deliberately empty, spec quirk kept);</li>
     *   <li>the 6-block water trough at y+18: all six cells are SOURCE blocks
     *       (the meta-0 flowing caps flatten to the source state, spec §6);</li>
     *   <li>the x+1 chest's table rolls 3-7 stacks of the 80-weight 4-entry
     *       SquidContentsList (chests/play_pool.json <- GD:49 + 3+nextInt(5)
     *       :1950): n=300 → endpoints 2·(4/5)^300 ≈ 3e-29, full coverage min
     *       weight 15/80 trivial.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 200)
    public void i145_play_pool_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = farOrigin(helper, 610, 40);
        helper.assertTrue(LegacyDungeonPiece.DungeonType.PLAY_POOL.placement
                        == LegacyDungeonPiece.DungeonType.PlacementMode.OCEAN_SURFACE_AIR,
                "PLAY_POOL must anchor on the AIR block above the ocean (OSW:1146-1148)");

        // Test-built sea: surface at o.below(), one filler layer beneath.
        for (int x = -3; x <= 8; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(o.offset(x, -1, z), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(o.offset(x, -2, z), Blocks.WATER.defaultBlockState(), 2);
            }
        }
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.PLAY_POOL);

        helper.assertTrue(level.getBlockState(o.below()).is(Blocks.WATER),
                "the sea surface below the origin must survive the build");
        // Spawner row 16 above the origin air block (orig :1940-1945).
        for (int i = 0; i < 4; i++) {
            assertSpawner(helper, level, o.offset(i, 16, 0), ModEntities.ATTACK_SQUID.get());
        }
        // Loot on the x+1 chest only (orig :1946-1951).
        assertLootChest(helper, level, o.offset(1, 17, 0), "chests/play_pool", null);
        BlockPos emptyChestPos = o.offset(2, 17, 0);
        helper.assertTrue(level.getBlockState(emptyChestPos).is(Blocks.CHEST),
                "the x+2 twin chest must exist");
        BlockEntity emptyBe = level.getBlockEntity(emptyChestPos);
        helper.assertTrue(emptyBe instanceof RandomizableContainerBlockEntity container
                        && container.getLootTable() == null && container.isEmpty(),
                "the x+2 twin chest must be DELIBERATELY empty with no loot table "
                        + "(orig :1948 fetches only the x+1 tile entity)");
        // Water trough: 6 source blocks at y+18 (orig :1952-1956, spec §6).
        for (int x = -1; x <= 4; x++) {
            BlockState water = level.getBlockState(o.offset(x, 18, 0));
            helper.assertTrue(water.is(Blocks.WATER) && water.getFluidState().isSource(),
                    "trough cell x+" + x + " should be a water SOURCE block, got " + water);
        }
        assertLootStats(helper, rollLoot(helper, "chests/play_pool", 300), 3, 7,
                Set.of("minecraft:ink_sac", "orespawn:squid_zooka", "minecraft:gold_nugget",
                        "minecraft:rotten_flesh"),
                true, "play_pool loot (80-weight SquidContentsList)");
        helper.succeed();
    }

    // =====================================================================
    // i146-cloudsharkdungeon-islands-sky — Cloud Shark Dungeon + Y150-159 band
    // =====================================================================

    /**
     * Checklist i146-cloudsharkdungeon-islands-sky (WGEN-042 pool; spec
     * d6_extraction/cloud_shark_dungeon_spec.md). Far region K=611.
     * <ul>
     *   <li>Sky band: PlacementMode.SKY_BAND_150 (LegacyDungeonPiece.java:182)
     *       — no scan at all, X/Z = chunk + 4 + nextInt(8), Y = 150 +
     *       nextInt(10) (skyBand150Origin, LegacyDungeonStructure.java:289-295,
     *       orig OSW:2423-2428). Sampled through findGenerationPoint against
     *       the real Islands generator over 300 chunks: every anchor lands in
     *       X/Z jitter 4..11 and Y 150..159, and all ten band Y values appear
     *       (P(any band value unobserved) ≤ 10·(9/10)^300 ≈ 2.7e-13);</li>
     *   <li>biome wiring: #orespawn:has_structure/cloud_shark_dungeon =
     *       orespawn:island_biome only;</li>
     *   <li>content via buildNow (CloudSharkDungeonGenerator.java:64-80, orig
     *       GD:2059-2091): 2-block glowstone core, 4 Cloud Shark spawners in a
     *       cardinal plus, cap chest bound to chests/cloud_shark_dungeon;</li>
     *   <li>the chest table rolls 4-8 stacks of the 140-weight 6-entry
     *       CloudSharkContentsList incl. experience tree seeds
     *       (chests/cloud_shark_dungeon.json <- GD:47 + 4+nextInt(5) :2089):
     *       n=300 → endpoints 2·(4/5)^300 ≈ 3e-29, full coverage min weight
     *       15/140 over ≥1200 samples → miss ≤ 6·e^(−128) ≈ 0.</li>
     * </ul>
     */
    @GameTest(template = "empty", timeoutTicks = 300)
    public void i146_cloud_shark_dungeon_sky_band(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        Structure dungeon = registeredStructure(helper, "cloud_shark_dungeon");
        helper.assertTrue(LegacyDungeonPiece.DungeonType.CLOUD_SHARK_DUNGEON.placement
                        == LegacyDungeonPiece.DungeonType.PlacementMode.SKY_BAND_150,
                "CLOUD_SHARK_DUNGEON must anchor via SKY_BAND_150 (OSW:2423-2428)");
        helper.assertTrue(dungeon.biomes().size() == 1 && dungeon.biomes().stream()
                        .anyMatch(h -> h.is(ResourceKey.create(Registries.BIOME, rl("island_biome")))),
                "cloud_shark_dungeon must be bound to orespawn:island_biome only");

        // Y150-159 band + in-chunk jitter over the real Islands generator.
        OreSpawnChunkGenerator islandsGen = islandsGenerator(helper);
        RandomState islandsRandom = islandsRandomState(helper);
        Set<Integer> bandYs = new HashSet<>();
        for (int c = 0; c < 300; c++) {
            ChunkPos chunkPos = new ChunkPos(c * 3 + 1, -c * 5 + 2);
            Optional<Structure.GenerationStub> stub = dungeon.findValidGenerationPoint(
                    contextFor(helper, islandsGen, islandsRandom, 0L, chunkPos));
            helper.assertTrue(stub.isPresent(),
                    "SKY_BAND_150 placement is unconditional but chunk " + chunkPos + " was empty");
            BlockPos pos = stub.get().position();
            helper.assertTrue(pos.getY() >= 150 && pos.getY() <= 159,
                    "sky anchor Y=" + pos.getY() + " outside the documented 150-159 band");
            int xJitter = pos.getX() - chunkPos.getMinBlockX();
            int zJitter = pos.getZ() - chunkPos.getMinBlockZ();
            helper.assertTrue(xJitter >= 4 && xJitter <= 11 && zJitter >= 4 && zJitter <= 11,
                    "sky anchor jitter (" + xJitter + "," + zJitter + ") outside chunk+4+nextInt(8)");
            bandYs.add(pos.getY());
        }
        helper.assertTrue(bandYs.size() == 10,
                "all ten band heights 150..159 should appear over 300 chunks, saw " + bandYs.size());

        // Content build (deterministic 7-block cluster + chest).
        BlockPos o = farOrigin(helper, 611, 100);
        LegacyDungeonPiece.buildNow(level, o, LegacyDungeonPiece.DungeonType.CLOUD_SHARK_DUNGEON);
        helper.assertTrue(level.getBlockState(o).is(Blocks.GLOWSTONE)
                        && level.getBlockState(o.below()).is(Blocks.GLOWSTONE),
                "2-block glowstone core missing (orig :2064-2065)");
        assertSpawner(helper, level, o.east(), ModEntities.CLOUD_SHARK.get());
        assertSpawner(helper, level, o.west(), ModEntities.CLOUD_SHARK.get());
        assertSpawner(helper, level, o.south(), ModEntities.CLOUD_SHARK.get());
        assertSpawner(helper, level, o.north(), ModEntities.CLOUD_SHARK.get());
        assertLootChest(helper, level, o.above(), "chests/cloud_shark_dungeon", null);

        Set<String> observed = assertLootStats(helper,
                rollLoot(helper, "chests/cloud_shark_dungeon", 300), 4, 8,
                Set.of("minecraft:cod", "minecraft:bone", "minecraft:string", "minecraft:paper",
                        "orespawn:experience_tree_seed", "minecraft:rotten_flesh"),
                true, "cloud_shark_dungeon loot (140-weight table)");
        helper.assertTrue(observed.contains("orespawn:experience_tree_seed"),
                "cloud shark chest must roll experience tree seeds");
        helper.succeed();
    }
}
