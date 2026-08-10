package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.entity.SpiderRobot;
import danger.orespawn.world.feature.BeehiveFeature;
import danger.orespawn.world.feature.MantisNestFeature;
import danger.orespawn.world.feature.SmallBeehiveFeature;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType;
import danger.orespawn.world.structure.LegacyDungeonStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Structure batch C (checklist items i162..i176, the 11 remaining AUTOMATABLE
 * structures-category items): Rainbow, Spider Hangout, Red Ant Hangout,
 * Frog Pond, Rubber Ducky Pond, Haunted House, Ender Knight Dungeon, and the
 * greenhouse-plants / door / royal-altar-box / hive-chest regressions.
 *
 * <p><b>Far-build region plan</b> (batch K base 800, one K per far-building
 * test so no two suite tests share a region; sub-builds inside one test are
 * spaced 120-300 blocks, far below the 512-block K stride):</p>
 * <ul>
 *   <li>K=800 rainbow (i162), K=801 spider hangout (i164), K=802 red ant
 *       hangout (i165), K=803 greenhouse plants (i172), K=804 doors (i173),
 *       K=805 royal altars + Alien WTF (i174), K=806 hive chests (i176).</li>
 *   <li>Frog Pond (i166), Rubber Ducky Pond (i168), Haunted House (i169) and
 *       Ender Knight Dungeon (i170) fit the 48x16x48 template and build
 *       in-template (i166 needs ticking chunks for its fluid cascade).</li>
 * </ul>
 *
 * <p>All far builds happen at absolute Y {@link #BUILD_Y}; far chunks load
 * synchronously through {@code ServerLevel.setBlock}/{@code getBlockState}.
 * Statistical asserts use a 6.5-sigma binomial window (two-sided false-fail
 * about 8e-11 per assert) or an explicit worst-case product bound shown in
 * the comment; every asserted number cites its documented source.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructureTestsC {

    /** Absolute build Y for far builds: leaves 58 below (deepest need: beehive -31,
     *  royal altar skirt -10) and 99 above (tallest need: rainbow +41, altar +58)
     *  inside the -64..320 world range regardless of the flat test-world surface. */
    private static final int BUILD_Y = 64;

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath("orespawn", path);
    }

    /** Far deterministic build origin for this batch: test-grid X + K*512, Z + 50000
     *  (inside the default +-29999984 world border), at the fixed {@link #BUILD_Y}. */
    private static BlockPos farPos(GameTestHelper helper, int uniqueK) {
        BlockPos abs = helper.absolutePos(BlockPos.ZERO);
        return new BlockPos(abs.getX() + uniqueK * 512, BUILD_Y, abs.getZ() + 50000);
    }

    /** Entity id string a spawner block entity is bound to ("" if unbound). */
    private static String spawnerId(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawner)) {
            return "<no spawner block entity at " + pos + ">";
        }
        return spawner.getSpawner().save(new CompoundTag())
                .getCompound("SpawnData").getCompound("entity").getString("id");
    }

    private static String idOf(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
    }

    private static Item item(GameTestHelper helper, String id) {
        Item it = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        helper.assertTrue(it != Items.AIR, "unknown item id in test expectation: " + id);
        return it;
    }

    /** One roll of a data-driven chest loot table (mechanism-level loot assert). */
    private static List<ItemStack> rollLoot(ServerLevel level, String tablePath, BlockPos origin) {
        LootTable table = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, rl(tablePath)));
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                .create(LootContextParamSets.CHEST);
        return table.getRandomItems(params);
    }

    /** Binomial(trials, p) window at 6.5 sigma: two-sided false-fail ~8e-11. */
    private static void assertBinomial(GameTestHelper helper, String what,
                                       long observed, long trials, double p) {
        double mean = trials * p;
        double sd = Math.sqrt(trials * p * (1.0 - p));
        double lo = mean - 6.5 * sd;
        double hi = mean + 6.5 * sd;
        helper.assertTrue(observed >= lo && observed <= hi,
                what + ": observed " + observed + " outside 6.5-sigma window ["
                        + (long) lo + ", " + (long) hi + "] for Binomial(" + trials + ", " + p + ")");
    }

    /**
     * Structure.GenerationContext against a loaded dimension's real generator.
     *
     * <p>NOTE (triage reclassification): this helper and
     * {@link #structure}/{@link #dimension} below are currently UNUSED —
     * every anchor/gate probe needing an orespawn dimension was reclassified
     * HARNESS_LIMIT/MANUAL_ONLY because the vanilla GameTestServer bakes
     * WorldPresets.FLAT against an empty datapack LevelStem registry and
     * never creates datapack dimensions. Kept as the documented procedure
     * for the manual checks (they work on a real dedicated/client server).</p>
     */
    private static Structure.GenerationContext contextFor(ServerLevel dim, ChunkPos chunkPos) {
        return new Structure.GenerationContext(
                dim.registryAccess(),
                dim.getChunkSource().getGenerator(),
                dim.getChunkSource().getGenerator().getBiomeSource(),
                dim.getChunkSource().randomState(),
                dim.getStructureManager(),
                dim.getSeed(),
                chunkPos,
                dim,
                biome -> true);
    }

    private static LegacyDungeonStructure structure(GameTestHelper helper, String path) {
        Structure s = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.STRUCTURE).get(rl(path));
        helper.assertTrue(s instanceof LegacyDungeonStructure,
                "orespawn:" + path + " is not a LegacyDungeonStructure (got " + s + ")");
        return (LegacyDungeonStructure) s;
    }

    private static ServerLevel dimension(GameTestHelper helper, String path) {
        ServerLevel dim = helper.getLevel().getServer()
                .getLevel(ResourceKey.create(Registries.DIMENSION, rl(path)));
        helper.assertTrue(dim != null, "dimension orespawn:" + path + " not loaded on the test server");
        return dim;
    }

    // ------------------------------------------------------------------
    // i162 — Rainbow (Islands sky)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i162-rainbow-islands-sky} (WGEN-042 pool).
     * Documented values: RainbowGenerator.java (port of orig
     * GenericDungeon.java:6260-6391) and
     * phase_d_reports/d6_extraction/rainbow_spec.md —
     * arch colour order red INNERMOST to pink outermost (blkcolors orig
     * GD:65, spec S5), cloud slabs +26..+29 and +35 with the m>=4 arches
     * piercing the +35 slab (spec S4), the 16 rain water writes all
     * overwritten so the finished build holds ZERO water (spec S2),
     * six Cloud Shark spawners (orig GD:6348-6377), and a north-facing
     * double chest with BOTH halves bound to orespawn:chests/rainbow
     * rolling 10-14 stacks of the weight-150 6-entry table incl. magic
     * apple (orig GD:25 + :6379-6390). Y70-89 anchor = SKY_BAND_70
     * (LegacyDungeonStructure.skyBand70Origin, orig OreSpawnWorld.java:
     * 2430-2436; enum wiring LegacyDungeonPiece.java:227) — the anchor
     * probe is reclassified HARNESS_LIMIT/MANUAL_ONLY (no orespawn
     * dimensions on the GameTestServer; see the stub in the method body).
     * Far-build K=800.
     */
    @GameTest(template = "empty")
    public void rainbow_islands_sky_i162(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos p = farPos(helper, 800);
        LegacyDungeonPiece.buildNow(level, p, DungeonType.RAINBOW);

        // Arch order at z=0, sampled on each arch's own top bar
        // (y = +30+m covers x=0 for every m) — red innermost (m=3) to pink
        // outermost (m=10), transcribed NOT corrected (spec S5).
        var archColors = new net.minecraft.world.level.block.Block[]{
                Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
                Blocks.LIME_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.BLUE_TERRACOTTA,
                Blocks.PURPLE_TERRACOTTA, Blocks.PINK_TERRACOTTA};
        for (int m = 3; m <= 10; m++) {
            BlockPos bar = p.offset(0, 30 + m, 0);
            helper.assertTrue(level.getBlockState(bar).is(archColors[m - 3]),
                    "i162: arch m=" + m + " top bar at " + bar + " should be "
                            + archColors[m - 3] + ", got " + level.getBlockState(bar));
        }
        // Innermost red legs at x+3 / x-4, outermost pink legs at x+10 / x-11
        // (orig GD:6341-6342).
        helper.assertTrue(level.getBlockState(p.offset(3, 30, 0)).is(Blocks.RED_TERRACOTTA),
                "i162: red (innermost) east leg missing at +3/+30");
        helper.assertTrue(level.getBlockState(p.offset(10, 30, 0)).is(Blocks.PINK_TERRACOTTA),
                "i162: pink (outermost) east leg missing at +10/+30");
        helper.assertTrue(level.getBlockState(p.offset(-11, 30, 0)).is(Blocks.PINK_TERRACOTTA),
                "i162: pink (outermost) west leg missing at -11/+30");

        // Upper cloud slab at +35 (orig GD:6270-6278) threaded by the arches:
        // off the z=0 line it stays white; on the line the m=5 top bar
        // (y=+35, yellow) pierces it (spec S4).
        helper.assertTrue(level.getBlockState(p.offset(11, 35, 1)).is(Blocks.WHITE_TERRACOTTA),
                "i162: +35 cloud slab edge (z=+1) should be white terracotta");
        helper.assertTrue(level.getBlockState(p.offset(-12, 35, -1)).is(Blocks.WHITE_TERRACOTTA),
                "i162: +35 cloud slab edge (z=-1) should be white terracotta");
        helper.assertTrue(level.getBlockState(p.offset(0, 35, 0)).is(Blocks.YELLOW_TERRACOTTA),
                "i162: m=5 arch should pierce the +35 slab at x=0 (spec S4)");

        // The 16 rain writes (x = -11 step 3 .. +10 at +34/+35, orig
        // GD:6279-6283) are ALL overwritten by the arch loop — the finished
        // structure contains zero water (spec S2 coverage proof).
        for (int x = -11; x < 12; x += 3) {
            for (int dy : new int[]{34, 35}) {
                BlockState s = level.getBlockState(p.offset(x, dy, 0));
                helper.assertFalse(s.is(Blocks.WATER) || !s.getFluidState().isEmpty(),
                        "i162: rain cell at x=" + x + ", +" + dy + " survived as water (spec S2)");
            }
        }

        // Lower cloud: +26/+28 rings (26x5) + +27 bulge (28x7), interior open
        // (no floor, spec S3); +29 roof slab solid (orig GD:6284-6336).
        helper.assertTrue(level.getBlockState(p.offset(-13, 26, 0)).is(Blocks.WHITE_TERRACOTTA),
                "i162: +26 ring west edge should be white");
        helper.assertTrue(level.getBlockState(p.offset(-14, 27, 0)).is(Blocks.WHITE_TERRACOTTA),
                "i162: +27 bulge west edge should be white");
        helper.assertTrue(level.getBlockState(p.offset(0, 26, 0)).isAir(),
                "i162: cloud interior at +26 must stay open air (spec S3 — no floor)");
        helper.assertTrue(level.getBlockState(p.offset(0, 29, 0)).is(Blocks.WHITE_TERRACOTTA),
                "i162: +29 roof slab should be white terracotta");

        // Six Cloud Shark spawners in two 3-high pillars at x+2 / x-3,
        // y +30..+32 (orig GD:6348-6377).
        String cloudShark = idOf(ModEntities.CLOUD_SHARK.get());
        for (int dy = 30; dy <= 32; dy++) {
            for (int dx : new int[]{2, -3}) {
                BlockPos sp = p.offset(dx, dy, 0);
                helper.assertTrue(level.getBlockState(sp).is(Blocks.SPAWNER),
                        "i162: missing spawner at " + sp);
                helper.assertValueEqual(spawnerId(level, sp), cloudShark,
                        "i162: spawner entity id at " + sp);
            }
        }

        // Double chest on the roof slab: both halves north-facing and BOTH
        // bound to orespawn:chests/rainbow (orig GD:6379-6390 — 10+nextInt(5)
        // pulls EACH; unlike the Leaf Monster pair both halves fill).
        for (int dx : new int[]{0, -1}) {
            BlockPos cp = p.offset(dx, 30, 0);
            BlockState cs = level.getBlockState(cp);
            helper.assertTrue(cs.is(Blocks.CHEST), "i162: missing chest half at " + cp);
            helper.assertValueEqual(cs.getValue(BlockStateProperties.HORIZONTAL_FACING),
                    Direction.NORTH, "i162: chest facing (meta 2 = north, orig GD:6380/:6386)");
            BlockEntity be = level.getBlockEntity(cp);
            helper.assertTrue(be instanceof RandomizableContainerBlockEntity container
                            && container.getLootTable() != null
                            && container.getLootTable().location().equals(rl("chests/rainbow")),
                    "i162: chest half at " + cp + " not bound to orespawn:chests/rainbow");
        }

        // Loot mechanism: 120 rolls of chests/rainbow — every roll 10-14
        // stacks (orig 10+nextInt(5), GD:6383/:6389), every item from the
        // 6-entry weight-150 list (orig GD:25), magic apple present across
        // rolls. P(no magic apple) <= (5/6)^(120*10) ~ 1e-95 < 1e-9.
        Set<Item> rainbowPool = Set.of(
                ModItems.MAGIC_APPLE.get(), ModItems.CLOUD_SHARK_SPAWN_EGG.get(),
                Items.BONE, Items.STRING, Items.ROTTEN_FLESH, Items.EXPERIENCE_BOTTLE);
        boolean sawMagicApple = false;
        for (int i = 0; i < 120; i++) {
            List<ItemStack> roll = rollLoot(level, "chests/rainbow", p);
            helper.assertTrue(roll.size() >= 10 && roll.size() <= 14,
                    "i162: rainbow chest roll size " + roll.size() + " outside 10-14");
            for (ItemStack stack : roll) {
                helper.assertTrue(rainbowPool.contains(stack.getItem()),
                        "i162: unexpected item in rainbow loot: " + stack);
                if (stack.is(ModItems.MAGIC_APPLE.get())) sawMagicApple = true;
            }
        }
        helper.assertTrue(sawMagicApple, "i162: magic apple never rolled in 120 x 10-14 stacks");

        // HARNESS_LIMIT (reclassified, this triage pass): the SKY_BAND_70
        // anchor probe (orig OSW:2430-2436 — Y = 70 + nextInt(20), x/z =
        // chunk + 4 + nextInt(8), against the REAL Islands generator) needs
        // the orespawn:islands ServerLevel. The vanilla GameTestServer bakes
        // WorldPresets.FLAT against an EMPTY datapack LevelStem registry
        // (GameTestServer.create), so no orespawn dimension is ever created
        // — the run log prepares only minecraft:overworld, and every suite
        // test asking for an orespawn dimension failed the same way (this
        // batch's i164; other groups' i158/i114). The anchor sub-check
        // returns to MANUAL_ONLY; the build/loot asserts above remain.
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i164 — Spider Hangout (Village dimension)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i164-spiderhangout-village-dim}.
     * Documented values: SpiderHangoutGenerator.java (port of orig
     * GenericDungeon.java:6989-7043) and
     * phase_d_reports/d6_extraction/spider_hangout_spec.md — 20x20 gravel
     * pad on a stone slab (orig :6995-7008), 12 Spider Driver spawners in
     * 3-high corner columns (orig :7009-7037, spec S3), one persistent
     * Robot Spider on the block corner at (+10,+1,+10) spawned via the
     * SILENT bare-spawn path (orig :7038-7042, spec S5/S9 — no
     * playLivingSound, batch-4 verify fix), NO chest (spec S2), and the
     * SpiderDriverEnable worldgen gate in findGenerationPoint (orig
     * OreSpawnWorld.java:1323-1325; port LegacyDungeonStructure.java:93-96)
     * while the DSB/buildNow path stays ungated — the worldgen-gate
     * sub-check is reclassified HARNESS_LIMIT/MANUAL_ONLY (no orespawn
     * dimensions on the GameTestServer; see the stub in the method body).
     * Far-build K=801.
     */
    @GameTest(template = "empty")
    public void spider_hangout_village_i164(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos p = farPos(helper, 801);

        // TF-023 harness race — identical mechanism and fix as the red-ant
        // i165 test below (see the full analysis there): pin the pad chunks
        // with a FORCED region ticket and sync-load them a few ticks BEFORE
        // the build, so buildNow writes into already-promoted chunks and the
        // Robot Spider spawn lands in TRACKED (queryable) entity sections.
        // All asserts keep their original same-tick-after-build semantics.
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkPos ticketPos = new ChunkPos(p.offset(10, 0, 10));
        chunkSource.addRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
        ChunkPos minC = new ChunkPos(p);
        ChunkPos maxC = new ChunkPos(p.offset(19, 0, 19));
        for (int ccx = minC.x; ccx <= maxC.x; ccx++) {
            for (int ccz = minC.z; ccz <= maxC.z; ccz++) {
                level.getChunk(ccx, ccz); // blocking FULL load now; the queued
                                          // visibility promotion drains between
                                          // ticks, before the delayed build
            }
        }
        helper.runAfterDelay(5, () -> {
            try {
                spiderHangoutBuildAndAsserts(helper, level, p);
            } finally {
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
            }
            helper.succeed();
        });
    }

    /** Build + asserts of {@link #spider_hangout_village_i164} (body unchanged
     *  by the TF-023 infra fix — only moved behind the chunk pre-ticketing). */
    private static void spiderHangoutBuildAndAsserts(GameTestHelper helper, ServerLevel level,
                                                     BlockPos p) {
        // Capture sound dispatches during the build: zero PlayLevelSoundEvent
        // near the build = nothing audible (the classification-blessed
        // absence assert for "no spawn sound"; the original spawn is bare
        // createEntity+spawnEntityInWorld with no playLivingSound).
        List<PlayLevelSoundEvent> nearbySounds = new ArrayList<>();
        Vec3 centre = Vec3.atCenterOf(p.offset(10, 1, 10));
        Consumer<PlayLevelSoundEvent> listener = event -> {
            Vec3 at = null;
            if (event instanceof PlayLevelSoundEvent.AtEntity atEntity) {
                at = atEntity.getEntity().position();
            } else if (event instanceof PlayLevelSoundEvent.AtPosition atPosition) {
                at = atPosition.getPosition();
            }
            if (at != null && at.distanceTo(centre) < 128.0) {
                nearbySounds.add(event);
            }
        };
        NeoForge.EVENT_BUS.addListener(PlayLevelSoundEvent.class, listener);
        try {
            LegacyDungeonPiece.buildNow(level, p, DungeonType.SPIDER_HANGOUT);
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(nearbySounds.isEmpty(),
                "i164: build dispatched " + nearbySounds.size()
                        + " level sound(s) — the Robot Spider spawn must be silent (spec S5)");

        // Pad: stone foundation at -1, gravel floor at 0, across all 20x20.
        for (int i = 0; i < 20; i++) {
            for (int k = 0; k < 20; k++) {
                helper.assertTrue(level.getBlockState(p.offset(i, -1, k)).is(Blocks.STONE),
                        "i164: stone slab missing at (" + i + ",-1," + k + ")");
                helper.assertTrue(level.getBlockState(p.offset(i, 0, k)).is(Blocks.GRAVEL),
                        "i164: gravel floor missing at (" + i + ",0," + k + ")");
            }
        }
        // 12 Spider Driver spawners: 3-high columns on all four corners
        // (orig :7009-7037).
        String spiderDriver = idOf(ModEntities.SPIDER_DRIVER.get());
        int[][] corners = {{0, 0}, {19, 19}, {19, 0}, {0, 19}};
        for (int[] c : corners) {
            for (int j = 1; j <= 3; j++) {
                BlockPos sp = p.offset(c[0], j, c[1]);
                helper.assertTrue(level.getBlockState(sp).is(Blocks.SPAWNER),
                        "i164: missing corner spawner at " + sp);
                helper.assertValueEqual(spawnerId(level, sp), spiderDriver,
                        "i164: spawner entity id at " + sp);
            }
        }
        // No chest anywhere in the carved 20x21x20 volume (spec S2).
        for (int i = 0; i < 20; i++) {
            for (int j = -1; j < 20; j++) {
                for (int k = 0; k < 20; k++) {
                    helper.assertFalse(level.getBlockState(p.offset(i, j, k)).is(Blocks.CHEST),
                            "i164: unexpected chest at (" + i + "," + j + "," + k + ") — the hangout has no loot");
                }
            }
        }
        // One persistent Robot Spider parked on the block CORNER at
        // (+10,+1,+10) — int casts, not +0.5 centred (spec S9).
        List<SpiderRobot> robots = level.getEntities(ModEntities.SPIDER_ROBOT.get(),
                new AABB(p.getX() - 2, p.getY() - 2, p.getZ() - 2,
                        p.getX() + 22, p.getY() + 22, p.getZ() + 22), e -> true);
        helper.assertValueEqual(robots.size(), 1, "i164: Robot Spider count on the pad");
        SpiderRobot robot = robots.get(0);
        helper.assertTrue(robot.isPersistenceRequired(),
                "i164: Robot Spider must be persistence-flagged (spec S5 — spawnPersistent, post-BUG-007)");
        helper.assertTrue(Math.abs(robot.getX() - (p.getX() + 10)) < 1.0e-6
                        && Math.abs(robot.getY() - (p.getY() + 1)) < 1.0e-6
                        && Math.abs(robot.getZ() - (p.getZ() + 10)) < 1.0e-6,
                "i164: Robot Spider not on the block corner (+10,+1,+10): " + robot.position());

        // HARNESS_LIMIT (reclassified, this triage pass): the
        // SpiderDriverEnable worldgen-gate check (LegacyDungeonStructure
        // .java:93-96, orig OSW:1323-1325) needs findGenerationPoint against
        // the REAL orespawn:village generator — the positive control demands
        // a Village grass surface inside the Y 41..100 scan window, and the
        // GameTestServer never creates datapack dimensions (vanilla
        // GameTestServer.create bakes WorldPresets.FLAT against an empty
        // LevelStem registry; the run prepared only minecraft:overworld).
        // The flat overworld surface at Y -60 fails the scan, so a gate-off
        // empty result would be unattributable. The gate sub-check returns
        // to MANUAL_ONLY; the pad/spawner/robot/silent-spawn asserts above
        // remain automated (and passed in the triaged run).
    }

    // ------------------------------------------------------------------
    // i165 — Red Ant Hangout (Village dimension)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i165-redanthangout-village-dim}.
     * Documented values: RedAntHangoutGenerator.java (port of orig
     * GenericDungeon.java:7045-7069) and
     * phase_d_reports/d6_extraction/red_ant_hangout_spec.md — 16x16 gravel
     * pad over stone with EXACTLY four 3x3 Red Ant Nest corner pads (36
     * nest cells via the De Morgan condition orig :7056, spec S3; the
     * other 220 floor cells gravel), NO spawners and NO chest (spec S7),
     * and one persistent UNOWNED Robot Red Ant on the block corner at
     * (+8,+1,+8) (orig :7064-7068, spec S8/S9; owned=0 default, orig
     * AntRobot.java:48 — port AntRobot getOwned()). The wrench-claim item
     * flow itself is ITEM-scope (covered by the i011 wrench bundle); the
     * structure-side contract asserted here is that the robot spawns
     * factory-fresh unowned. Far-build K=802.
     */
    @GameTest(template = "empty")
    public void red_ant_hangout_village_i165(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos p = farPos(helper, 802);

        // TF-023 root cause (test infra, NOT the port): the far-K chunks are
        // loaded by ServerLevel.setBlock's blocking path under self-expiring
        // UNKNOWN tickets (TicketType.java:19 — timeout 1 tick), and the flip
        // that makes a chunk's ENTITY SECTIONS queryable is a QUEUED
        // main-thread task (ChunkHolder.promoteFullChunk → thenRunAsync →
        // ChunkMap.onFullChunkStatusChange → PersistentEntitySectionManager
        // .updateChunkStatus). AABB entity queries visit only sections whose
        // visibility isAccessible() (EntitySectionStorage
        // .forEachAccessibleNonEmptySection), so a same-tick query after
        // buildNow races that queued flip — and loses exactly when the
        // robot's chunk was the LAST chunk the build block-loaded (no later
        // blocking load drains the queue), i.e. whenever the pad's chunk
        // alignment puts the +8,+8 spawn in the final loaded chunk. That is
        // the observed spawn-X-on-chunk-border flake; the robot was present
        // in the section storage every time. Fix: pin the pad chunks with a
        // FORCED region ticket and sync-load them a few ticks BEFORE the
        // build, so buildNow writes into already-promoted chunks and the
        // spawn lands in TRACKED (queryable) sections. All asserts keep
        // their original same-tick-after-build semantics.
        ServerChunkCache chunkSource = level.getChunkSource();
        ChunkPos ticketPos = new ChunkPos(p.offset(8, 0, 8));
        chunkSource.addRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
        ChunkPos minC = new ChunkPos(p);
        ChunkPos maxC = new ChunkPos(p.offset(15, 0, 15));
        for (int ccx = minC.x; ccx <= maxC.x; ccx++) {
            for (int ccz = minC.z; ccz <= maxC.z; ccz++) {
                level.getChunk(ccx, ccz); // blocking FULL load now; the queued
                                          // visibility promotion drains between
                                          // ticks, before the delayed build
            }
        }
        helper.runAfterDelay(5, () -> {
            try {
                redAntHangoutBuildAndAsserts(helper, level, p);
            } finally {
                chunkSource.removeRegionTicket(TicketType.FORCED, ticketPos, 2, ticketPos);
            }
            helper.succeed();
        });
    }

    /** Build + asserts of {@link #red_ant_hangout_village_i165} (body unchanged
     *  by the TF-023 infra fix — only moved behind the chunk pre-ticketing). */
    private static void redAntHangoutBuildAndAsserts(GameTestHelper helper, ServerLevel level,
                                                     BlockPos p) {
        LegacyDungeonPiece.buildNow(level, p, DungeonType.RED_ANT_HANGOUT);

        // Floor census: exactly 36 nest cells, exactly on the 3x3 corner
        // pads (the De Morgan trap, spec S3), 220 gravel cells; stone below.
        int nest = 0;
        int gravel = 0;
        for (int i = 0; i < 16; i++) {
            for (int k = 0; k < 16; k++) {
                helper.assertTrue(level.getBlockState(p.offset(i, -1, k)).is(Blocks.STONE),
                        "i165: stone base missing at (" + i + ",-1," + k + ")");
                BlockState floor = level.getBlockState(p.offset(i, 0, k));
                boolean cornerCell = !((i >= 3 && i <= 12) || (k >= 3 && k <= 12)); // orig :7056
                if (floor.is(ModBlocks.RED_ANT_BLOCK.get())) {
                    nest++;
                    helper.assertTrue(cornerCell,
                            "i165: nest block outside the 3x3 corner pads at (" + i + ",0," + k + ")");
                } else if (floor.is(Blocks.GRAVEL)) {
                    gravel++;
                    helper.assertFalse(cornerCell,
                            "i165: corner-pad cell (" + i + ",0," + k + ") should be a nest block");
                } else {
                    helper.fail("i165: unexpected floor block " + floor + " at (" + i + ",0," + k + ")");
                }
            }
        }
        helper.assertValueEqual(nest, 36, "i165: Red Ant Nest corner-cell count (spec S3)");
        helper.assertValueEqual(gravel, 220, "i165: gravel floor-cell count (spec S3)");

        // No spawners, no chest, forced air above (orig :7050/:7060, spec S6/S7).
        for (int i = 0; i < 16; i++) {
            for (int j = 1; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    BlockState s = level.getBlockState(p.offset(i, j, k));
                    helper.assertFalse(s.is(Blocks.SPAWNER) || s.is(Blocks.CHEST),
                            "i165: unexpected " + s + " at (" + i + "," + j + "," + k
                                    + ") — the hangout has no spawners and no chest (spec S7)");
                    helper.assertTrue(s.isAir(),
                            "i165: clearing volume not air at (" + i + "," + j + "," + k + ")");
                }
            }
        }

        // The wild robot: exactly one, persistent, UNOWNED, on the block
        // corner at (+8,+1,+8) (orig :7064-7068, spec S8/S9; persistence per
        // orig AntRobot.java:80-82/:534 — spawnPersistent carries it).
        List<AntRobot> robots = level.getEntities(ModEntities.ANT_ROBOT.get(),
                new AABB(p.getX() - 2, p.getY() - 2, p.getZ() - 2,
                        p.getX() + 18, p.getY() + 18, p.getZ() + 18), e -> true);
        helper.assertValueEqual(robots.size(), 1, "i165: Robot Red Ant count on the pad");
        AntRobot robot = robots.get(0);
        helper.assertTrue(robot.isPersistenceRequired(),
                "i165: Robot Red Ant must be persistence-flagged (spec section 4.1)");
        helper.assertValueEqual(robot.getOwned(), 0,
                "i165: the hangout robot must spawn UNOWNED (owned=0, orig AntRobot.java:48; spec S8)");
        helper.assertTrue(Math.abs(robot.getX() - (p.getX() + 8)) < 1.0e-6
                        && Math.abs(robot.getY() - (p.getY() + 1)) < 1.0e-6
                        && Math.abs(robot.getZ() - (p.getZ() + 8)) < 1.0e-6,
                "i165: Robot Red Ant not on the block corner (+8,+1,+8): " + robot.position());
    }

    // ------------------------------------------------------------------
    // i166 — Frog Pond (plains, WGEN-042)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i166-frogpond-plains-wgen-042}.
     * Documented values: FrogPondGenerator.java (port of orig
     * GenericDungeon.java:6018-6039) and
     * phase_d_reports/d6_extraction/frog_pond_spec.md — "Frog" spawner at
     * (0,+2,0) (orig :6020), 7x7 still-water sheet flush at the grass line
     * (orig :6025-6029; anchor = the grass block itself, posY-1 at
     * OSW:1168), centre riser + flow cross at +1 (orig :6030-6034, all
     * sources after the 1.13 flattening, spec S4), lily-pad cross at +2
     * (orig :6035-6038), and the +1 sources cascading over the sheet and
     * off the rim once fluid ticks run (generated behavior in BOTH
     * versions, spec S9 — do not rim the pond). Builds in-template on a
     * 19x19 dirt plane standing in for the plains grass so the cascade
     * runs in ticking chunks.
     */
    @GameTest(template = "empty_large", timeoutTicks = 300)
    public void frog_pond_plains_i166(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Terrain stand-in: a 19x19, 2-deep dirt plane; the pond origin is
        // the plane's top-layer centre cell (the "grass block" the worldgen
        // scan anchors on, OSW:1168) so the sheet replaces the surface layer
        // and sits flush, contained by the surrounding grade.
        for (int x = 15; x <= 33; x++) {
            for (int z = 15; z <= 33; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT.defaultBlockState());
                helper.setBlock(new BlockPos(x, 2, z), Blocks.DIRT.defaultBlockState());
            }
        }
        // Test-infra containment (cross-cell hygiene, NOT a structure
        // expectation): a 2-high obsidian wall on the 19x19 plane perimeter.
        // The asserted cascade (spec S9) turns the whole +1 layer over the
        // plane into new sources via the two-adjacent-sources rule and would
        // otherwise walk off the plane edge and across the ground toward
        // neighbouring test cells within the 100-tick window. The wall sits
        // 6+ blocks outside the pond's 7x7 rim, so the "do not rim the pond"
        // rule (spec S9) is untouched and the rim spill assert still runs.
        for (int x = 15; x <= 33; x++) {
            for (int z : new int[]{15, 33}) {
                helper.setBlock(new BlockPos(x, 3, z), Blocks.OBSIDIAN.defaultBlockState());
                helper.setBlock(new BlockPos(x, 4, z), Blocks.OBSIDIAN.defaultBlockState());
            }
        }
        for (int z = 16; z <= 32; z++) {
            for (int x : new int[]{15, 33}) {
                helper.setBlock(new BlockPos(x, 3, z), Blocks.OBSIDIAN.defaultBlockState());
                helper.setBlock(new BlockPos(x, 4, z), Blocks.OBSIDIAN.defaultBlockState());
            }
        }
        BlockPos origin = helper.absolutePos(new BlockPos(24, 2, 24));
        LegacyDungeonPiece.buildNow(level, origin, DungeonType.FROG_POND);

        // Immediate structure asserts (59 writes, zero RNG — spec section 11).
        helper.assertTrue(level.getBlockState(origin.offset(0, 2, 0)).is(Blocks.SPAWNER),
                "i166: Frog spawner missing at (0,+2,0) (orig GD:6020)");
        helper.assertValueEqual(spawnerId(level, origin.offset(0, 2, 0)),
                idOf(ModEntities.FROG.get()), "i166: pond spawner entity id");
        for (int i = -3; i <= 3; i++) {
            for (int k = -3; k <= 3; k++) {
                BlockState s = level.getBlockState(origin.offset(i, 0, k));
                helper.assertTrue(s.is(Blocks.WATER) && s.getFluidState().isSource(),
                        "i166: 7x7 sheet cell (" + i + ",0," + k + ") is not still water (orig GD:6027)");
            }
        }
        helper.assertTrue(level.getBlockState(origin.offset(0, 1, 0)).is(Blocks.WATER),
                "i166: centre riser missing at (0,+1,0) (orig GD:6030)");
        for (int[] d : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            helper.assertTrue(level.getBlockState(origin.offset(d[0], 1, d[1])).is(Blocks.WATER),
                    "i166: flow-cross cell missing at (" + d[0] + ",+1," + d[1] + ") (orig GD:6031-6034)");
            helper.assertTrue(level.getBlockState(origin.offset(d[0], 2, d[1])).is(Blocks.LILY_PAD),
                    "i166: lily pad missing at (" + d[0] + ",+2," + d[1] + ") (orig GD:6035-6038)");
        }

        // After fluid ticks the pond settles STABLE (spec S9 as AMENDED at
        // the TF-025 close-out, 2026-08-11): flow over the source sheet
        // becomes falling water and never propagates horizontally, so the
        // cascade dies one block off the cross; the two-source rule
        // promotes the convex 3x3 around the riser to sources. Asserted
        // end-state = the TF-025 diagnostic's t=250 dump (FIX_LOG).
        helper.runAfterDelay(100, () -> {
            BlockState diag = level.getBlockState(origin.offset(1, 1, 1));
            helper.assertTrue(diag.getFluidState().is(FluidTags.WATER),
                    "i166: flow cross did not spread over the sheet (expected water at (+1,+1,+1), spec S9)");
            // One-block lip flow, then the cascade dies (falling-over-water).
            helper.assertTrue(level.getBlockState(origin.offset(2, 1, 0)).getFluidState().is(FluidTags.WATER),
                    "i166: expected the one-block lip flow at (+2,+1,0) (spec S9 amended)");
            helper.assertTrue(level.getBlockState(origin.offset(3, 1, 0)).getFluidState().isEmpty(),
                    "i166: cascade must DIE at (+3,+1,0) — stable pond, no rim spill (spec S9 amended, TF-025)");
            helper.assertTrue(level.getBlockState(origin.offset(4, 1, 0)).getFluidState().isEmpty(),
                    "i166: no water past the rim at (+4,+1,0) (spec S9 amended, TF-025)");
            helper.assertTrue(level.getBlockState(origin.offset(1, 2, 0)).is(Blocks.LILY_PAD),
                    "i166: lily pad at (+1,+2,0) should survive the fluid updates (spec section 5)");
            helper.assertTrue(level.getBlockState(origin.offset(0, 0, 0)).getFluidState().isSource(),
                    "i166: sheet centre must remain a source");
            helper.succeed();
        });
    }


    /**
     * One TF-025 snapshot (support for {@link #tf025_diag_frog_pond_cascade}
     * only): samples the documented cells, appends to {@code dump}, and logs
     * the block immediately so a timeout still leaves partial data.
     */
    private static void tf025Snapshot(ServerLevel level, BlockPos origin, StringBuilder dump, int tick) {
        StringBuilder out = new StringBuilder();
        out.append("\n--- t=").append(tick).append(" ---");
        // Riser + flow-cross arms — the five +1 cells the cascade starts
        // from (orig GD:6030-6034, spec S9).
        tf025Cell(level, origin, out, "riser    ", 0, 1, 0);
        tf025Cell(level, origin, out, "arm-west ", -1, 1, 0);
        tf025Cell(level, origin, out, "arm-east ", 1, 1, 0);
        tf025Cell(level, origin, out, "arm-north", 0, 1, -1);
        tf025Cell(level, origin, out, "arm-south", 0, 1, 1);
        // Sheet edge cells: the +-3 ring's axis + corner cells at y=0 (the
        // sheet itself, orig GD:6025-6029) and y=+1 (the spill frontier one
        // above them — the cells that must hop outward for the S9 spill).
        for (int[] d : new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {3, 3}, {3, -3}, {-3, 3}, {-3, -3}}) {
            tf025Cell(level, origin, out, "edge y0  ", d[0], 0, d[1]);
            tf025Cell(level, origin, out, "edge y1  ", d[0], 1, d[1]);
        }
        // Rim overflow line — the i166 red assert's neighbourhood.
        for (int z = -1; z <= 1; z++) {
            tf025Cell(level, origin, out, "rim      ", 4, 1, z);
        }
        // +x ray to +6: y=+1 (the layer the spill travels in) and y=0 (the
        // sheet out to +3, then the dirt plane the overflow would cross).
        for (int x = 1; x <= 6; x++) {
            tf025Cell(level, origin, out, "ray y1   ", x, 1, 0);
        }
        for (int x = 1; x <= 6; x++) {
            tf025Cell(level, origin, out, "ray y0   ", x, 0, 0);
        }
        // Pending-fluid-tick census over the pond neighbourhood plus a
        // source/flowing census of the 49-cell +1 layer over the sheet:
        // zero pending ticks with a dry rim means a stable fixed point, not
        // scheduler starvation.
        int pending = 0;
        int srcAbove = 0;
        int flowAbove = 0;
        int emptyAbove = 0;
        StringBuilder pendingList = new StringBuilder();
        for (int x = -7; x <= 7; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -7; z <= 7; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (level.getFluidTicks().hasScheduledTick(pos, Fluids.WATER)
                            || level.getFluidTicks().hasScheduledTick(pos, Fluids.FLOWING_WATER)) {
                        if (pending < 12) {
                            pendingList.append(String.format(" (%+d,%+d,%+d)", x, y, z));
                        }
                        pending++;
                    }
                    if (y == 1 && x >= -3 && x <= 3 && z >= -3 && z <= 3) {
                        FluidState f = level.getBlockState(pos).getFluidState();
                        if (f.isEmpty()) {
                            emptyAbove++;
                        } else if (f.isSource()) {
                            srcAbove++;
                        } else {
                            flowAbove++;
                        }
                    }
                }
            }
        }
        out.append("\n  pending fluid ticks (x,z=-7..+7, y=0..+2): ").append(pending);
        if (pending > 0) {
            out.append(" first:").append(pendingList).append(pending > 12 ? " ..." : "");
        }
        out.append("\n  +1 layer over the 7x7 sheet: ").append(srcAbove).append(" source / ")
                .append(flowAbove).append(" flowing / ").append(emptyAbove).append(" empty");
        OreSpawnMod.LOGGER.info("[TF-025 diag]{}", out);
        dump.append(out);
    }

    /**
     * One TF-025 sampled cell (support for {@link #tf025Snapshot} only):
     * block state, fluid state (source/flowing + amount), and whether a
     * fluid tick is pending for either registered water fluid.
     */
    private static void tf025Cell(ServerLevel level, BlockPos origin, StringBuilder out,
                                  String label, int dx, int dy, int dz) {
        BlockPos pos = origin.offset(dx, dy, dz);
        BlockState state = level.getBlockState(pos);
        FluidState fluid = state.getFluidState();
        out.append("\n  ").append(label).append(String.format(" (%+d,%+d,%+d): ", dx, dy, dz))
                .append(state);
        if (fluid.isEmpty()) {
            out.append(" fluid=EMPTY");
        } else {
            out.append(" fluid=").append(BuiltInRegistries.FLUID.getKey(fluid.getType()).getPath())
                    .append(fluid.isSource() ? " SOURCE" : " flowing")
                    .append(" amt=").append(fluid.getAmount());
        }
        out.append(" tick[W=").append(level.getFluidTicks().hasScheduledTick(pos, Fluids.WATER))
                .append(",FW=").append(level.getFluidTicks().hasScheduledTick(pos, Fluids.FLOWING_WATER))
                .append(']');
    }

    // ------------------------------------------------------------------
    // i168 — Rubber Ducky Pond (plains)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i168-rubberduckypond-plains}.
     * Documented values: RubberDuckyPondGenerator.java (port of orig
     * GenericDungeon.java:5383-5421) and
     * phase_d_reports/d6_extraction/rubber_ducky_pond_spec.md — 12x11 pond
     * pad perched ON the grass (sand perimeter ring / 10x9 water interior,
     * orig :5409-5420; anchor = the AIR block above grass with no offset,
     * OSW:1228-1229, spec S6), floating totem: 4-wide water line +3
     * (orig :5404-5408, all sources, spec S4), glass pair +4 (:5402-5403,
     * full blocks, spec S10), chest pair +5 where ONLY the second-placed
     * (+1,+5,0) half is loot-bound and the (0,+5,0) half stays EMPTY
     * (orig :5396-5401 — the faithful oddity, spec S3), and two Rubber
     * Ducky spawners at +6 (orig :5390-5395). Loot: 13-entry all-weight-35
     * table (orig GenericDungeon.java:27) at 8-12 rolls (orig 8+nextInt(5),
     * :5400) via orespawn:chests/rubber_ducky_pond.
     */
    @GameTest(template = "empty_large")
    public void rubber_ducky_pond_plains_i168(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // Terrain stand-in: dirt plane whose TOP surface is one below the
        // origin — the origin is the air block above the "grass" (spec S6),
        // and the pre-existing terrain is the pond bottom (do not excavate).
        for (int x = 14; x <= 34; x++) {
            for (int z = 14; z <= 34; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT.defaultBlockState());
                helper.setBlock(new BlockPos(x, 2, z), Blocks.DIRT.defaultBlockState());
            }
        }
        BlockPos origin = helper.absolutePos(new BlockPos(24, 3, 24));
        LegacyDungeonPiece.buildNow(level, origin, DungeonType.RUBBER_DUCKY_POND);

        // Pond pad at rel Y 0: 12x11 (rel X -5..+6, rel Z -5..+5), sand on
        // the perimeter ring, still water inside (orig :5409-5420).
        for (int i = 0; i < 12; i++) {
            for (int k = 0; k < 11; k++) {
                BlockPos cell = origin.offset(i - 5, 0, k - 5);
                BlockState s = level.getBlockState(cell);
                if (i == 0 || k == 0 || i == 11 || k == 10) {
                    helper.assertTrue(s.is(Blocks.SAND),
                            "i168: sand rim missing at pad cell (" + i + "," + k + ")");
                } else {
                    helper.assertTrue(s.getFluidState().is(FluidTags.WATER),
                            "i168: pond interior not water at pad cell (" + i + "," + k + ")");
                }
            }
        }
        // Totem: water line +3 (4 sources), glass +4, chest pair +5,
        // spawner pair +6.
        for (int dx = -1; dx <= 2; dx++) {
            BlockState s = level.getBlockState(origin.offset(dx, 3, 0));
            helper.assertTrue(s.is(Blocks.WATER) && s.getFluidState().isSource(),
                    "i168: +3 water-line cell x=" + dx + " must be a source (spec S4, orig :5404-5408)");
        }
        helper.assertTrue(level.getBlockState(origin.offset(0, 4, 0)).is(Blocks.GLASS)
                        && level.getBlockState(origin.offset(1, 4, 0)).is(Blocks.GLASS),
                "i168: full glass blocks missing at +4 (orig :5402-5403, spec S10)");

        // The faithful chest oddity (spec S3): (0,+5,0) placed but NEVER
        // fetched — empty, no loot table; only (+1,+5,0) is loot-bound.
        BlockPos emptyChest = origin.offset(0, 5, 0);
        helper.assertTrue(level.getBlockState(emptyChest).is(Blocks.CHEST),
                "i168: chest half missing at (0,+5,0)");
        BlockEntity emptyBe = level.getBlockEntity(emptyChest);
        helper.assertTrue(emptyBe instanceof ChestBlockEntity chest
                        && chest.getLootTable() == null && chest.isEmpty(),
                "i168: the (0,+5,0) chest half must stay EMPTY with no loot table (orig :5396-5401)");
        BlockPos lootChest = origin.offset(1, 5, 0);
        BlockEntity lootBe = level.getBlockEntity(lootChest);
        helper.assertTrue(lootBe instanceof RandomizableContainerBlockEntity container
                        && container.getLootTable() != null
                        && container.getLootTable().location().equals(rl("chests/rubber_ducky_pond")),
                "i168: the (+1,+5,0) chest half must be bound to orespawn:chests/rubber_ducky_pond");

        String ducky = idOf(ModEntities.ENTITY_RUBBER_DUCKY.get());
        for (int dx = 0; dx <= 1; dx++) {
            BlockPos sp = origin.offset(dx, 6, 0);
            helper.assertTrue(level.getBlockState(sp).is(Blocks.SPAWNER),
                    "i168: Rubber Ducky spawner missing at (" + dx + ",+6,0) (orig :5390-5395)");
            helper.assertValueEqual(spawnerId(level, sp), ducky, "i168: spawner entity id at " + sp);
        }

        // Loot mechanism: 120 rolls — 8-12 stacks each, all 13 entries legal,
        // the Rubber Ducky spawn egg appears. P(no egg) <= (12/13)^(120*8)
        // = (12/13)^960 ~ 4e-34 < 1e-9. Table transcribes orig GD:27.
        Set<Item> pool = new HashSet<>();
        for (String id : new String[]{
                "orespawn:dead_stink_bug", "orespawn:fire_fish", "orespawn:sun_fish",
                "orespawn:spark_fish", "orespawn:green_fish", "orespawn:blue_fish",
                "orespawn:pink_fish", "orespawn:rock_fish", "orespawn:wood_fish",
                "orespawn:grey_fish", "orespawn:rubber_ducky_spawn_egg",
                "orespawn:peacock_feather", "minecraft:feather"}) {
            pool.add(item(helper, id));
        }
        boolean sawEgg = false;
        for (int i = 0; i < 120; i++) {
            List<ItemStack> roll = rollLoot(level, "chests/rubber_ducky_pond", origin);
            helper.assertTrue(roll.size() >= 8 && roll.size() <= 12,
                    "i168: loot roll size " + roll.size() + " outside 8-12 (orig 8+nextInt(5), GD:5400)");
            for (ItemStack stack : roll) {
                helper.assertTrue(pool.contains(stack.getItem()),
                        "i168: unexpected item in rubber ducky loot: " + stack);
                if (stack.is(item(helper, "orespawn:rubber_ducky_spawn_egg"))) sawEgg = true;
            }
        }
        helper.assertTrue(sawEgg, "i168: rubber ducky spawn egg never rolled in 120 x 8-12 stacks");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i169 — Haunted House (overworld)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i169-hauntedhouse-overworld}.
     * Documented values: HauntedHouseGenerator.java (port of orig
     * GenericDungeon.java:891-1010) and
     * phase_d_reports/d6_extraction/haunted_house_spec.md — 7x7x5 plank
     * box with cobble floor, FULL perimeter glass-block clerestory band at
     * +3 corners included (spec S8, orig :925-927), hardcoded east doorway
     * (deltax=1, orig :905/:929-931), north-facing furnace / crafting
     * table / kit-chest furniture row (stuffdir=2 -> meta 2 north, orig
     * :940-949, spec S6), centre spawner STACK Rat/Ghost/Ghost Pumpkin
     * Skelly at +1/+2/+3 (orig :995-1009, spec S4), and the kit chest's 14
     * fixed slots each present at 50% (orig :950-993 -> 14 random_chance
     * 0.5 pools in orespawn:chests/haunted_house).
     */
    @GameTest(template = "empty_large")
    public void haunted_house_overworld_i169(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(24, 2, 24));
        LegacyDungeonPiece.buildNow(level, origin, DungeonType.HAUNTED_HOUSE);

        // Shell: full 7x7 cobble floor at +0 and plank roof at +4
        // (orig :916-922 — walls included in both).
        for (int[] c : new int[][]{{0, 0}, {3, 3}, {-3, -3}, {3, -3}, {-3, 3}}) {
            helper.assertTrue(level.getBlockState(origin.offset(c[0], 0, c[1])).is(Blocks.COBBLESTONE),
                    "i169: cobble floor missing at (" + c[0] + ",0," + c[1] + ")");
            helper.assertTrue(level.getBlockState(origin.offset(c[0], 4, c[1])).is(Blocks.OAK_PLANKS),
                    "i169: plank roof missing at (" + c[0] + ",+4," + c[1] + ")");
        }
        // Clerestory: ALL 24 perimeter cells at +3 are glass BLOCKS,
        // corners included (spec S8 — not punched windows, not panes).
        for (int i = -3; i <= 3; i++) {
            for (int k = -3; k <= 3; k++) {
                if (i != 3 && i != -3 && k != 3 && k != -3) continue;
                helper.assertTrue(level.getBlockState(origin.offset(i, 3, k)).is(Blocks.GLASS),
                        "i169: clerestory cell (" + i + ",+3," + k + ") must be a glass block (spec S8)");
            }
        }
        // East doorway: 1x2 at (+3, +1..+2, 0), wall planks beside it
        // (orig :929-934).
        helper.assertTrue(level.getBlockState(origin.offset(3, 1, 0)).isAir()
                        && level.getBlockState(origin.offset(3, 2, 0)).isAir(),
                "i169: east doorway not carved at (+3,+1..+2,0)");
        helper.assertTrue(level.getBlockState(origin.offset(3, 1, 1)).is(Blocks.OAK_PLANKS),
                "i169: east wall plank missing beside the doorway");

        // Furniture row on the interior south side, all fronts north
        // (orig :940-949, spec S6).
        BlockState furnace = level.getBlockState(origin.offset(2, 1, 2));
        helper.assertTrue(furnace.is(Blocks.FURNACE),
                "i169: furnace missing at (+2,+1,+2)");
        helper.assertValueEqual(furnace.getValue(BlockStateProperties.HORIZONTAL_FACING),
                Direction.NORTH, "i169: furnace facing (meta 2 = north, orig :944)");
        helper.assertValueEqual(furnace.getValue(BlockStateProperties.LIT), Boolean.FALSE,
                "i169: furnace must be unlit (spec S6)");
        helper.assertTrue(level.getBlockState(origin.offset(1, 1, 2)).is(Blocks.CRAFTING_TABLE),
                "i169: crafting table missing at (+1,+1,+2)");
        BlockPos chestPos = origin.offset(0, 1, 2);
        BlockState chestState = level.getBlockState(chestPos);
        helper.assertTrue(chestState.is(Blocks.CHEST), "i169: kit chest missing at (0,+1,+2)");
        helper.assertValueEqual(chestState.getValue(BlockStateProperties.HORIZONTAL_FACING),
                Direction.NORTH, "i169: kit chest facing (meta 2 = north, orig :949; WGEN-056)");
        BlockEntity chestBe = level.getBlockEntity(chestPos);
        helper.assertTrue(chestBe instanceof RandomizableContainerBlockEntity container
                        && container.getLootTable() != null
                        && container.getLootTable().location().equals(rl("chests/haunted_house")),
                "i169: kit chest not bound to orespawn:chests/haunted_house");

        // Centre spawner stack: ONE vertical column, floor to glass band
        // (orig :995-1009, spec S4 — do not redistribute).
        String[] stack = {idOf(ModEntities.ENTITY_RAT.get()),
                idOf(ModEntities.GHOST.get()), idOf(ModEntities.GHOST_SKELLY.get())};
        for (int j = 1; j <= 3; j++) {
            BlockPos sp = origin.offset(0, j, 0);
            helper.assertTrue(level.getBlockState(sp).is(Blocks.SPAWNER),
                    "i169: spawner stack missing at (0,+" + j + ",0)");
            helper.assertValueEqual(spawnerId(level, sp), stack[j - 1],
                    "i169: spawner stack entity id at +" + j);
        }

        // Loot mechanism: 200 rolls of the 14 x random_chance-0.5 pool table
        // (InstantShelter kit incl. slot-12 Ore Salt and COOKED porkchop,
        // spec S3). 13 distinct items (red bed appears in two pools).
        // Presence: P(item never rolled in 200) <= 0.5^200 ~ 6e-61, x13
        // union still << 1e-9. Total-stack mean: sum over 200 rolls ~
        // Binomial(2800, 0.5) checked at 6.5 sigma.
        Set<Item> pool = new HashSet<>();
        String[] ids = {"minecraft:compass", "minecraft:map", "minecraft:cooked_porkchop",
                "minecraft:torch", "minecraft:coal", "minecraft:red_bed", "minecraft:oak_door",
                "minecraft:iron_pickaxe", "minecraft:iron_sword", "minecraft:iron_axe",
                "minecraft:bucket", "orespawn:ore_salt", "minecraft:chest"};
        for (String id : ids) pool.add(item(helper, id));
        Set<Item> seen = new HashSet<>();
        long totalStacks = 0;
        for (int i = 0; i < 200; i++) {
            List<ItemStack> roll = rollLoot(level, "chests/haunted_house", origin);
            helper.assertTrue(roll.size() <= 14,
                    "i169: kit roll produced " + roll.size() + " stacks (> 14 slots)");
            totalStacks += roll.size();
            for (ItemStack stackItem : roll) {
                helper.assertTrue(pool.contains(stackItem.getItem()),
                        "i169: unexpected item in kit loot: " + stackItem);
                seen.add(stackItem.getItem());
            }
        }
        helper.assertValueEqual(seen.size(), pool.size(),
                "i169: distinct kit items seen across 200 rolls (each slot present at 50%)");
        assertBinomial(helper, "i169: total kit stacks over 200 rolls (14 slots x 50%)",
                totalStacks, 200L * 14L, 0.5);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i170 — Ender Knight Dungeon (End + Mining)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i170-enderknightdungeon-end-mining}.
     * Documented values: EnderKnightDungeonGenerator.java (port of orig
     * GenericDungeon.java:1794-1932) and
     * phase_d_reports/d6_extraction/ender_knight_dungeon_spec.md —
     * obsidian dungeon (orig GD:1812 field_150343_Z; the checklist line's
     * "cobble" phrasing is imprecise against the extracted spec section 2.1
     * palette table, which this test follows) with END-STONE floor in both
     * dimensions (spec S4), octagonal plan 7-wide at x+5/x+11 and 9-wide
     * at x+6..+10, 1x3 doorway at z+2 (orig :1813-1815), 28 shelf sites
     * each rolling nextInt(4): chest / bookshelf / cobweb / nothing (orig
     * :1906-1931), TWO Ender Knight spawners FLOATING at (+8,+2/+3,+2)
     * over interior air (orig :1861-1869, spec S8), chest loot 3-7 stacks
     * of the 5-entry weight-95 KnightContentsList (orig GD:50 + :1915),
     * and the Mining anchor sitting ON the lowest grass surface with NO
     * -2 sink (LOWEST_GRASS_36, LegacyDungeonStructure.java:379-404, orig
     * OSW:2087-2113 — contrast addBasiliskMaze's lowestY-2, OSW:2594) —
     * the Mining-anchor sub-check is reclassified HARNESS_LIMIT/MANUAL_ONLY
     * (no orespawn dimensions on the GameTestServer; see the stub in the
     * method body).
     */
    @GameTest(template = "empty_large")
    public void ender_knight_dungeon_i170(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(17, 2, 24));

        // Shelf-site columns (28 total, spec section 2.2): C (x+5) at rel Z
        // {0,1,3,4}; D (x+6..10) at rel Z {-1,0,4,5}; E (x+11) at {0,1,3,4}.
        List<BlockPos> shelfSites = new ArrayList<>();
        for (int z : new int[]{0, 1, 3, 4}) shelfSites.add(origin.offset(5, 1, z));
        for (int x = 6; x <= 10; x++) {
            for (int z : new int[]{-1, 0, 4, 5}) shelfSites.add(origin.offset(x, 1, z));
        }
        for (int z : new int[]{0, 1, 3, 4}) shelfSites.add(origin.offset(11, 1, z));
        helper.assertValueEqual(shelfSites.size(), 28, "i170: shelf site enumeration");

        // 40 rebuilds -> 1120 shelf rolls at p=1/4 per outcome; each
        // category checked at 6.5 sigma (mean 280, sd 14.5, window ~[186,374];
        // two-sided false-fail ~8e-11 per category, x4 ~ 3e-10 < 1e-9).
        long chests = 0;
        long shelves = 0;
        long webs = 0;
        long nothing = 0;
        for (int build = 0; build < 40; build++) {
            LegacyDungeonPiece.buildNow(level, origin, DungeonType.ENDER_KNIGHT_DUNGEON);
            for (BlockPos site : shelfSites) {
                BlockState s = level.getBlockState(site);
                if (s.is(Blocks.CHEST)) chests++;
                else if (s.is(Blocks.BOOKSHELF)) shelves++;
                else if (s.is(Blocks.COBWEB)) webs++;
                else if (s.isAir()) nothing++;
                else helper.fail("i170: unexpected shelf-site block " + s + " at " + site);
            }
        }
        assertBinomial(helper, "i170: shelf chest sites (nextInt(4)==0, orig GD:1908)", chests, 1120, 0.25);
        assertBinomial(helper, "i170: bookshelf sites (nextInt(4)==1, orig GD:1918)", shelves, 1120, 0.25);
        assertBinomial(helper, "i170: cobweb sites (nextInt(4)==2, orig GD:1925)", webs, 1120, 0.25);
        assertBinomial(helper, "i170: empty sites (nextInt(4)==3, orig GD:1906)", nothing, 1120, 0.25);

        // Content asserts on the final build (cells no shelf roll can touch).
        helper.assertTrue(level.getBlockState(origin.offset(0, 2, 2)).isAir(),
                "i170: entrance cut not carved (orig :1801-1808)");
        helper.assertTrue(level.getBlockState(origin.offset(4, 0, 0)).is(Blocks.OBSIDIAN),
                "i170: obsidian front wall missing (orig :1812)");
        for (int j = 1; j <= 3; j++) {
            helper.assertTrue(level.getBlockState(origin.offset(4, j, 2)).isAir(),
                    "i170: 1x3 doorway not carved at (+4,+" + j + ",+2) (orig :1813-1815)");
        }
        helper.assertTrue(level.getBlockState(origin.offset(4, 4, 2)).is(Blocks.OBSIDIAN),
                "i170: wall above the doorway missing");
        helper.assertTrue(level.getBlockState(origin.offset(5, 0, 0)).is(Blocks.END_STONE),
                "i170: end-stone floor missing in the 7-wide slice (orig :1828-1830, spec S4)");
        helper.assertTrue(level.getBlockState(origin.offset(8, 0, 2)).is(Blocks.END_STONE),
                "i170: end-stone floor missing in the 9-wide room (orig :1849-1851)");
        helper.assertTrue(level.getBlockState(origin.offset(5, 1, -1)).is(Blocks.OBSIDIAN),
                "i170: 7-wide slice Z wall missing at z-1 (octagonal plan, orig :1831-1833)");
        helper.assertTrue(level.getBlockState(origin.offset(8, 1, -2)).is(Blocks.OBSIDIAN),
                "i170: 9-wide room Z wall missing at z-2 (octagonal plan, orig :1852-1854)");
        helper.assertTrue(level.getBlockState(origin.offset(8, 5, 2)).is(Blocks.OBSIDIAN),
                "i170: obsidian ceiling missing (orig :1846-1848)");
        helper.assertTrue(level.getBlockState(origin.offset(12, 3, 2)).is(Blocks.OBSIDIAN),
                "i170: solid obsidian back wall missing (orig :1895-1900)");

        // Two Ender Knight spawners FLOATING at room centre (+8,+2/+3,+2)
        // with air beneath (orig :1861-1869, spec S8).
        String enderKnight = idOf(ModEntities.ENDER_KNIGHT.get());
        for (int j = 2; j <= 3; j++) {
            BlockPos sp = origin.offset(8, j, 2);
            helper.assertTrue(level.getBlockState(sp).is(Blocks.SPAWNER),
                    "i170: Ender Knight spawner missing at (+8,+" + j + ",+2)");
            helper.assertValueEqual(spawnerId(level, sp), enderKnight,
                    "i170: spawner entity id at " + sp);
        }
        helper.assertTrue(level.getBlockState(origin.offset(8, 1, 2)).isAir(),
                "i170: the spawner pair must FLOAT over interior air (spec S8)");

        // Loot mechanism: 100 rolls of chests/ender_knight_dungeon — 3-7
        // stacks (orig 3+nextInt(5), GD:1915), 5-entry weight-95 list (GD:50),
        // every entry appears. Min entry weight 15/95=0.158/stack, >=3
        // stacks/roll: P(entry absent) <= (1-0.158)^300 ~ 4e-23, x5 << 1e-9.
        Set<Item> pool = Set.of(Items.PAPER, Items.OAK_PLANKS, Items.ENDER_EYE,
                Items.ENDER_PEARL, Items.ROTTEN_FLESH);
        Set<Item> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            List<ItemStack> roll = rollLoot(level, "chests/ender_knight_dungeon", origin);
            helper.assertTrue(roll.size() >= 3 && roll.size() <= 7,
                    "i170: knight chest roll size " + roll.size() + " outside 3-7");
            for (ItemStack stack : roll) {
                helper.assertTrue(pool.contains(stack.getItem()),
                        "i170: unexpected item in knight loot: " + stack);
                seen.add(stack.getItem());
            }
        }
        helper.assertValueEqual(seen.size(), pool.size(),
                "i170: all 5 KnightContentsList entries seen across 100 rolls");

        // HARNESS_LIMIT (reclassified, this triage pass): the LOWEST_GRASS_36
        // Mining-anchor no-sink probe (LegacyDungeonStructure.java:379-404,
        // orig OSW:2087-2113 — anchor ON lowestY, NO -2 sink, vs
        // addBasiliskMaze OSW:2594) needs the orespawn:mining ServerLevel
        // and its real generator. The GameTestServer never creates datapack
        // dimensions (vanilla GameTestServer.create bakes WorldPresets.FLAT
        // against an empty LevelStem registry; the run prepared only
        // minecraft:overworld), and the flat overworld surface (Y -60) lies
        // outside the scan's 31..128 window, so no stub can even form as a
        // stand-in. The Mining-anchor sub-check returns to MANUAL_ONLY; the
        // octagon/shelf/spawner/loot asserts above remain automated.
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i172 — Regression: greenhouse plants (WGEN-063)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i172-regression-greenhouse-plants-wgen-063};
     * finding WGEN-063 (FIX_LOG.md Phase D close-out: "case 7 is
     * REEDS/sugar cane (orig GD:5090-5092) and case 19 is MyRicePlant
     * (GD:5123-5125) — both had silently drifted"; fix landed in
     * LegacyDungeonPiece.pickGreenhousePlant, D6b batch-4 verify).
     * Documented mechanism (LegacyDungeonPiece.generateGreenhouse, port of
     * orig GD:5030-5168): interior j==1 cells with i%3!=2 plant at 2/3
     * (random.nextInt(3)!=1) — farmland below + pickGreenhousePlant
     * (nextInt(20)) above, where index 8 is the ONE intentional empty roll
     * (1-in-20) and indexes 7/19 are sugar cane / rice. Pumpkins are never
     * in the table. The DSB type-36 path runs this same generator, so one
     * loop covers both entry points. Far-build K=803.
     */
    @GameTest(template = "empty")
    public void greenhouse_plants_regression_i172(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos p = farPos(helper, 803);
        // Greenhouse box: ox=px-11, oz=pz-7; floor at p.y, plants at p.y+1.
        int builds = 40;
        long farmland = 0;
        long empty = 0;
        long cane = 0;
        long rice = 0;
        for (int build = 0; build < builds; build++) {
            LegacyDungeonPiece.buildNow(level, p, DungeonType.GREENHOUSE);
            for (int i = 1; i <= 21; i++) {
                for (int k = 1; k <= 13; k++) {
                    BlockPos floor = new BlockPos(p.getX() - 11 + i, p.getY(), p.getZ() - 7 + k);
                    if (!level.getBlockState(floor).is(Blocks.FARMLAND)) continue;
                    farmland++;
                    BlockState plant = level.getBlockState(floor.above());
                    if (plant.isAir()) empty++;
                    else if (plant.is(Blocks.SUGAR_CANE)) cane++;
                    else if (plant.is(ModBlocks.RICE_PLANT.get())) rice++;
                    helper.assertFalse(plant.is(Blocks.PUMPKIN) || plant.is(Blocks.CARVED_PUMPKIN)
                                    || plant.is(Blocks.PUMPKIN_STEM) || plant.is(Blocks.ATTACHED_PUMPKIN_STEM),
                            "i172: pumpkin rolled in a greenhouse plot at " + floor.above()
                                    + " — the WGEN-063 drift is back");
                }
            }
        }
        // Plot census: 14 plantable columns x 13 rows = 182 candidates per
        // build at p=2/3 -> Binomial(7280, 2/3), 6.5 sigma.
        assertBinomial(helper, "i172: farmland plots over " + builds + " builds (2/3 gate, orig GD:5064)",
                farmland, 182L * builds, 2.0 / 3.0);
        // Empty plots: exactly the nextInt(20)==8 gap -> Binomial(farmland, 1/20).
        assertBinomial(helper, "i172: empty plots (the one legacy table gap, index 8 of nextInt(20))",
                empty, farmland, 1.0 / 20.0);
        // Presence: each specific crop is 1/20 per plot; P(zero sugar cane in
        // ~4850 plots) = (19/20)^4850 ~ e^-248 << 1e-9; same for rice.
        helper.assertTrue(cane > 0,
                "i172: sugar cane (case 7, orig GD:5090-5092) never appeared in " + farmland + " plots");
        helper.assertTrue(rice > 0,
                "i172: rice (case 19, orig GD:5123-5125) never appeared in " + farmland + " plots");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i173 — Regression: greenhouse + white house doors (WGEN-067/068)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i173-regression-greenhouse-white-house-doors-wgen-067-068};
     * findings WGEN-067/WGEN-068 (FIX_LOG.md Phase D close-out: "WGEN-067
     * greenhouse double-door entry rebuilt (two doors at width/2 and
     * width/2-1, lintels, meta-4 buttons — the D6a robot-lab door trace;
     * the port had a single door at the wrong x); WGEN-068 white-house
     * door upper half restored + button re-hung NORTH"; pre-fix state
     * recorded as dsb_sweep_spec.md flags F1/F2).
     * Documented positions (LegacyDungeonPiece.generateGreenhouse, orig
     * GD:5138-5147; makeWhWalls, orig GD:5548-5551): greenhouse ox=px-11,
     * oz=pz-7 — door columns ox+7 (hinge LEFT) and ox+6 (hinge RIGHT) on
     * the z=oz wall, both FACING NORTH, full lower+upper halves; stone
     * lintels at ox+5/ox+8, y+2; wall-attached north-facing stone buttons
     * one block outside at oz-1. White house: wall base (px-15, py+2,
     * pz-14) — full 2-tall door at wall x+11, wall-mounted button at wall
     * (+12, +1, -1) whose supporting block is the quartz wall ("sits ON
     * the wall, not floating"). Far-build K=804.
     */
    @GameTest(template = "empty")
    public void greenhouse_white_house_doors_i173(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // --- Greenhouse (WGEN-067) ---
        BlockPos g = farPos(helper, 804);
        LegacyDungeonPiece.buildNow(level, g, DungeonType.GREENHOUSE);
        int gx = g.getX() - 11;                 // ox
        int gz = g.getZ() - 7;                  // oz
        int gy = g.getY();
        // East leaf at ox+7: FACING NORTH, HINGE LEFT; west leaf at ox+6:
        // HINGE RIGHT (the D6a-verified robot-lab dir-3 trace).
        record DoorSpec(int x, DoorHingeSide hinge) {}
        for (DoorSpec d : new DoorSpec[]{
                new DoorSpec(gx + 7, DoorHingeSide.LEFT), new DoorSpec(gx + 6, DoorHingeSide.RIGHT)}) {
            BlockState lower = level.getBlockState(new BlockPos(d.x(), gy + 1, gz));
            BlockState upper = level.getBlockState(new BlockPos(d.x(), gy + 2, gz));
            helper.assertTrue(lower.is(Blocks.IRON_DOOR) && upper.is(Blocks.IRON_DOOR),
                    "i173/WGEN-067: greenhouse door column at x=" + d.x() + " incomplete (need BOTH halves)");
            helper.assertValueEqual(lower.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF),
                    DoubleBlockHalf.LOWER, "i173: greenhouse door lower half at x=" + d.x());
            helper.assertValueEqual(upper.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF),
                    DoubleBlockHalf.UPPER, "i173: greenhouse door upper half at x=" + d.x());
            for (BlockState half : new BlockState[]{lower, upper}) {
                helper.assertValueEqual(half.getValue(BlockStateProperties.HORIZONTAL_FACING),
                        Direction.NORTH, "i173: greenhouse door facing (1.7.10 dir 3 = north) at x=" + d.x());
                helper.assertValueEqual(half.getValue(BlockStateProperties.DOOR_HINGE), d.hinge(),
                        "i173: greenhouse door hinge at x=" + d.x());
            }
        }
        // Stone lintels flanking the pair (orig GD:5144-5145) and the two
        // wall-attached north-facing stone buttons outside (orig GD:5146-5147,
        // meta 4 = north). Each button's supporting block IS its lintel.
        for (int bx : new int[]{gx + 5, gx + 8}) {
            helper.assertTrue(level.getBlockState(new BlockPos(bx, gy + 2, gz)).is(Blocks.STONE),
                    "i173/WGEN-067: greenhouse stone lintel missing at x=" + bx);
            BlockState button = level.getBlockState(new BlockPos(bx, gy + 2, gz - 1));
            helper.assertTrue(button.is(Blocks.STONE_BUTTON),
                    "i173/WGEN-067: greenhouse stone button missing outside x=" + bx);
            helper.assertValueEqual(button.getValue(BlockStateProperties.ATTACH_FACE), AttachFace.WALL,
                    "i173: greenhouse button attach face at x=" + bx);
            helper.assertValueEqual(button.getValue(BlockStateProperties.HORIZONTAL_FACING),
                    Direction.NORTH, "i173: greenhouse button facing (meta 4 = north) at x=" + bx);
        }

        // --- White House (WGEN-068) ---
        BlockPos w = g.offset(200, 0, 0);
        LegacyDungeonPiece.buildNow(level, w, DungeonType.WHITE_HOUSE);
        // makeWhWalls base = (wx, wy, wz) = (px-15, py+2, pz-14); door at
        // wall x+11, button at wall (+12, +1, -1) — generateWhiteHouse
        // ox=px-12/oz=pz-9, walls call at (ox-3, py+2, oz-5).
        int wx = w.getX() - 15;
        int wy = w.getY() + 2;
        int wz = w.getZ() - 14;
        BlockState lower = level.getBlockState(new BlockPos(wx + 11, wy, wz));
        BlockState upper = level.getBlockState(new BlockPos(wx + 11, wy + 1, wz));
        helper.assertTrue(lower.is(Blocks.IRON_DOOR),
                "i173/WGEN-068: white house door lower half missing");
        helper.assertTrue(upper.is(Blocks.IRON_DOOR),
                "i173/WGEN-068: white house door UPPER half missing (the F2 regression)");
        helper.assertValueEqual(lower.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF),
                DoubleBlockHalf.LOWER, "i173: white house door lower half property");
        helper.assertValueEqual(upper.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF),
                DoubleBlockHalf.UPPER, "i173: white house door upper half property");
        for (BlockState half : new BlockState[]{lower, upper}) {
            helper.assertValueEqual(half.getValue(BlockStateProperties.HORIZONTAL_FACING),
                    Direction.NORTH, "i173: white house door facing (dir 3 = north, orig GD:5550)");
        }
        BlockPos buttonPos = new BlockPos(wx + 12, wy + 1, wz - 1);
        BlockState button = level.getBlockState(buttonPos);
        helper.assertTrue(button.is(Blocks.STONE_BUTTON),
                "i173/WGEN-068: white house entry button missing");
        helper.assertValueEqual(button.getValue(BlockStateProperties.ATTACH_FACE), AttachFace.WALL,
                "i173: white house button attach face");
        helper.assertValueEqual(button.getValue(BlockStateProperties.HORIZONTAL_FACING),
                Direction.NORTH, "i173: white house button facing (re-hung NORTH per FIX_LOG)");
        // "Sits ON the wall (not floating)": the wall-attach support block —
        // pos.relative(facing.getOpposite()) = one south — is the quartz wall.
        helper.assertTrue(level.getBlockState(buttonPos.south()).is(Blocks.QUARTZ_BLOCK),
                "i173/WGEN-068: white house button support block is not the quartz wall — button floats");
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i174 — Regression: royal altar boxes + Alien WTF south wall
    //        (WGEN-065/066)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i174-regression-royal-altars-alien-wtf-boxes-wgen-065-066};
     * findings WGEN-065/WGEN-066, recorded pre-fix as dsb_sweep_spec.md
     * flags F3 and F8 and fixed by widening the DungeonType envelopes
     * (LegacyDungeonPiece.java: KING_ALTAR/QUEEN_ALTAR(32, 10, 59) — F3
     * clipped the v=1..9 dirt skirt below origin-4 and the top rows of the
     * j&lt;=height+10 air clear, orig GD:4377-4382/5721-5726 + :4364/5708;
     * ALIEN_WTF(-20, 20, 25, 6, -22, 20) — F8 clipped the south Part
     * room's far Z wall plane at origin-21, orig GD:1674 makeAlienPart
     * width 15). Asserted here on the buildNow path, where the piece box
     * IS the write window (LegacyDungeonPiece.buildNow), so a clipped box
     * reproduces the regression deterministically:
     * dirt skirt reaches -9, air-clear reaches +58 (and stops there), and
     * the south room's far wall plane is complete obsidian. Far-build
     * K=805 (king at P, queen at P+150x, alien at P+300x).
     */
    @GameTest(template = "empty")
    public void royal_altars_alien_wtf_boxes_i174(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = farPos(helper, 805);

        for (int variant = 0; variant < 2; variant++) {
            boolean king = variant == 0;
            BlockPos p = king ? base : base.offset(150, 0, 0);
            String tag = king ? "KING_ALTAR" : "QUEEN_ALTAR";
            // Pad sample column (-13,-13) from centre: on the 51x51 pad but
            // clear of the centre-altar cross arms (half-extents 10/6/20),
            // corner columns (ox+1..7 etc) and the portrait plane (ox+4).
            BlockPos padTop = p.offset(-13, 0, -13);
            // Deterministic pre-state: the skirt only fills cells that were
            // air/short-grass/water (generateRoyalAltar phase 2), so force
            // the sampled column to air down to -10 before building.
            for (int v = 1; v <= 10; v++) {
                level.setBlock(padTop.below(v), Blocks.AIR.defaultBlockState(), 2);
            }
            // Air-clear reach markers: +58 is the last cleared row
            // (j <= height+10 = 58); +59 is outside every write.
            BlockPos marker58 = p.offset(20, 58, 20);
            BlockPos marker59 = p.offset(20, 59, 20);
            level.setBlock(marker58, Blocks.GLASS.defaultBlockState(), 2);
            level.setBlock(marker59, Blocks.GLASS.defaultBlockState(), 2);

            LegacyDungeonPiece.buildNow(level, p, king ? DungeonType.KING_ALTAR : DungeonType.QUEEN_ALTAR);

            helper.assertTrue(level.getBlockState(padTop).is(Blocks.GRASS_BLOCK),
                    "i174/" + tag + ": 51x51 grass pad missing at the sample column");
            for (int v = 1; v <= 9; v++) {
                helper.assertTrue(level.getBlockState(padTop.below(v)).is(Blocks.DIRT),
                        "i174/WGEN-065 " + tag + ": dirt skirt missing at depth -" + v
                                + " (orig GD:4377-4382/5721-5726 — the F3 clip)");
            }
            helper.assertTrue(level.getBlockState(padTop.below(10)).isAir(),
                    "i174/" + tag + ": skirt must stop at -9 (v < 10)");
            helper.assertTrue(level.getBlockState(marker58).isAir(),
                    "i174/WGEN-065 " + tag + ": air-clear must reach +58 (orig GD:4364/5708 — the F3 clip)");
            helper.assertTrue(level.getBlockState(marker59).is(Blocks.GLASS),
                    "i174/" + tag + ": +59 lies outside every write and must survive");
        }

        // --- Alien WTF south Part room far wall (WGEN-066 / F8) ---
        BlockPos a = base.offset(300, 0, 0);
        // South room: makeAlienPart(origin.x, antennaY, origin.z-7, width 15,
        // height 8, dx=-1, dz=-1) with antennaY = origin.y - 17
        // (generateAlienWtfDungeon; orig GD:1674). Far Z wall plane:
        // z = origin.z - 21, x = origin.x-14..origin.x, y rows 0..7 + the
        // ceiling row at +8. Pre-fill the plane with glass so every
        // asserted obsidian cell proves a write actually landed there.
        int antennaY = a.getY() - 17;
        for (int i = 0; i <= 14; i++) {
            for (int j = 0; j <= 8; j++) {
                level.setBlock(new BlockPos(a.getX() - i, antennaY + j, a.getZ() - 21),
                        Blocks.GLASS.defaultBlockState(), 2);
            }
        }
        LegacyDungeonPiece.buildNow(level, a, DungeonType.ALIEN_WTF);
        helper.assertTrue(level.getBlockState(new BlockPos(a.getX() - 2, antennaY, a.getZ() - 2))
                        .is(Blocks.LAPIS_BLOCK),
                "i174: Alien WTF lapis antenna missing — build sanity check failed");
        for (int i = 0; i <= 14; i++) {
            for (int j = 0; j <= 8; j++) {
                BlockPos cell = new BlockPos(a.getX() - i, antennaY + j, a.getZ() - 21);
                helper.assertTrue(level.getBlockState(cell).is(Blocks.OBSIDIAN),
                        "i174/WGEN-066: south Part room far wall incomplete at " + cell
                                + " (the F8 box clip — orig GD:1674, wall plane at origin-21)");
            }
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i176 — Regression: bee/mantis chests (ITEM-068/069)
    // ------------------------------------------------------------------

    /**
     * Checklist item {@code i176-regression-bee-mantis-chests-item-068-069};
     * findings ITEM-068/ITEM-069 (FIX_LOG.md Phase D close-out: "ITEM-068
     * bee/mantis/small-beehive chest facings restored (inward E/W/S/N ring
     * per orig metas); ITEM-069 bee/mantis egg loot restored
     * (BEE_SPAWN_EGG 2-8 w15, MANTIS_SPAWN_EGG 2-4 w20 — the invented
     * golden-carrot/spider-eye stand-ins removed)").
     * Documented mechanisms: BeehiveFeature.fillBeehiveChests (orig
     * GD:860-889 — inward metas 5/4/3/2 = E/W/S/N; beeContentsList GD:55,
     * egg entry 2-8 weight 15, fills 1+nextInt(5));
     * MantisNestFeature.fillMantisChests (orig GD:1064-1093 — same inward
     * ring; mantisContentsList GD:56, egg 2-4 weight 20, fills
     * 3+nextInt(7)); SmallBeehiveFeature step 7 (orig GD:1444-1450 —
     * single chest meta 5 = EAST, fills 7+nextInt(5) from the bee list).
     * Worst-case egg-presence math per structure is in the inline
     * comments; each bound is &lt; 1e-9. Far-build K=806 (bee at P,
     * mantis at P+120x, skep at P+240x).
     */
    @GameTest(template = "empty")
    public void bee_mantis_chests_i176(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = farPos(helper, 806);

        // --- Deep beehive: 14 layers x 4 inward-facing chests (ITEM-068) ---
        // Egg presence: p(egg)=15/150=0.1 per draw, >=1 draw per chest, 56
        // chests per build, 4 builds: P(no egg) <= 0.9^(56*4) ~ 5.6e-11.
        boolean sawBeeEgg = false;
        for (int build = 0; build < 4; build++) {
            BeehiveFeature.buildAt(level, new java.util.Random(level.random.nextLong()), base);
            for (int j = 2; j < 29; j += 2) {
                sawBeeEgg |= checkHiveChest(helper, level, base.offset(1, -j, 5), Direction.EAST,
                        ModItems.BEE_SPAWN_EGG.get(), 2, 8, "i176 beehive west-wall (meta 5, GD:865-866)");
                sawBeeEgg |= checkHiveChest(helper, level, base.offset(8, -j, 5), Direction.WEST,
                        ModItems.BEE_SPAWN_EGG.get(), 2, 8, "i176 beehive east-wall (meta 4, GD:871-872)");
                sawBeeEgg |= checkHiveChest(helper, level, base.offset(5, -j, 1), Direction.SOUTH,
                        ModItems.BEE_SPAWN_EGG.get(), 2, 8, "i176 beehive north-wall (meta 3, GD:877-878)");
                sawBeeEgg |= checkHiveChest(helper, level, base.offset(5, -j, 8), Direction.NORTH,
                        ModItems.BEE_SPAWN_EGG.get(), 2, 8, "i176 beehive south-wall (meta 2, GD:883-884)");
            }
        }
        helper.assertTrue(sawBeeEgg,
                "i176/ITEM-069: no Bee spawn egg in 4 beehive builds (weight 15/150, GD:55)");

        // --- Mantis nest: 3 pyramid steps x 4 inward-facing chests ---
        // Egg presence: p=20/135~0.148 per draw, >=3 draws per chest, 12
        // chests per build, 4 builds: P(no egg) <= (1-0.148)^(36*4) ~ 9e-11.
        BlockPos mantisBase = base.offset(120, 0, 0);
        boolean sawMantisEgg = false;
        for (int build = 0; build < 4; build++) {
            MantisNestFeature.buildAt(level, new java.util.Random(level.random.nextLong()), mantisBase);
            for (int step = 1; step <= 3; step++) {
                int width = 13 - 2 * step;                       // 11, 9, 7
                BlockPos corner = mantisBase.offset(step, -step, step);
                sawMantisEgg |= checkHiveChest(helper, level, corner.offset(1, 0, width / 2),
                        Direction.EAST, ModItems.MANTIS_SPAWN_EGG.get(), 2, 4,
                        "i176 mantis west-wall (meta 5, GD:1069-1070)");
                sawMantisEgg |= checkHiveChest(helper, level, corner.offset(width - 2, 0, width / 2),
                        Direction.WEST, ModItems.MANTIS_SPAWN_EGG.get(), 2, 4,
                        "i176 mantis east-wall (meta 4, GD:1075-1076)");
                sawMantisEgg |= checkHiveChest(helper, level, corner.offset(width / 2, 0, 1),
                        Direction.SOUTH, ModItems.MANTIS_SPAWN_EGG.get(), 2, 4,
                        "i176 mantis north-wall (meta 3, GD:1081-1082)");
                sawMantisEgg |= checkHiveChest(helper, level, corner.offset(width / 2, 0, width - 2),
                        Direction.NORTH, ModItems.MANTIS_SPAWN_EGG.get(), 2, 4,
                        "i176 mantis south-wall (meta 2, GD:1087-1088)");
            }
        }
        helper.assertTrue(sawMantisEgg,
                "i176/ITEM-069: no Mantis spawn egg in 4 mantis nest builds (weight 20/135, GD:56)");

        // --- Small beehive skep: single EAST-facing chest (meta 5, GD:1446),
        // 7+nextInt(5) bee-list draws (GD:1449). Egg presence over 40
        // builds: P(no egg) <= (0.9^7)^40 = 0.478^40 ~ 1.5e-13.
        BlockPos skepBase = base.offset(240, 0, 0);
        boolean sawSkepEgg = false;
        for (int build = 0; build < 40; build++) {
            SmallBeehiveFeature.buildAt(level, new java.util.Random(level.random.nextLong()), skepBase);
            // Chest at (WIDTH/2=3, TRUNK_Y+1=15, 3) — the legacy j-reassign
            // quirk (SmallBeehiveFeature step 7).
            sawSkepEgg |= checkHiveChest(helper, level, skepBase.offset(3, 15, 3), Direction.EAST,
                    ModItems.BEE_SPAWN_EGG.get(), 2, 8, "i176 skep chest (meta 5 = EAST, GD:1446)");
        }
        helper.assertTrue(sawSkepEgg,
                "i176/ITEM-069: no Bee spawn egg in 40 small-beehive builds (p >= 1-0.9^7 per build)");
        helper.succeed();
    }

    /**
     * Asserts one hive chest: present, facing as the original's metadata
     * write demands (ITEM-068 — inward, NOT uniformly north), no
     * golden-carrot/spider-eye stand-ins (ITEM-069 hard invariant), and
     * egg stacks inside the documented 2-8 / 2-4 count band. Returns
     * whether this chest held at least one egg.
     */
    private static boolean checkHiveChest(GameTestHelper helper, ServerLevel level, BlockPos pos,
                                          Direction expectedFacing, Item egg,
                                          int minEgg, int maxEgg, String what) {
        BlockState state = level.getBlockState(pos);
        helper.assertTrue(state.is(Blocks.CHEST), what + ": chest missing at " + pos);
        helper.assertValueEqual(state.getValue(BlockStateProperties.HORIZONTAL_FACING), expectedFacing,
                what + ": chest facing at " + pos + " (ITEM-068 inward ring)");
        BlockEntity be = level.getBlockEntity(pos);
        helper.assertTrue(be instanceof ChestBlockEntity, what + ": no chest block entity at " + pos);
        ChestBlockEntity chest = (ChestBlockEntity) be;
        boolean sawEgg = false;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (stack.isEmpty()) continue;
            helper.assertFalse(stack.is(Items.GOLDEN_CARROT) || stack.is(Items.SPIDER_EYE),
                    what + ": ITEM-069 stand-in (" + stack + ") found at " + pos
                            + " — golden carrots / spider eyes must never appear");
            if (stack.is(egg)) {
                sawEgg = true;
                helper.assertTrue(stack.getCount() >= minEgg && stack.getCount() <= maxEgg,
                        what + ": egg stack of " + stack.getCount() + " outside " + minEgg + "-" + maxEgg
                                + " (orig list entry, GD:55/GD:56)");
            }
        }
        return sawEgg;
    }
}
