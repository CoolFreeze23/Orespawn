package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModDimensionKeys;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Structure-content GameTests, batch B (checklist items i147-i148, i151-i155,
 * i157-i161 — the WGEN-042 / D6b mechanical-structure pool, batches 1-3).
 *
 * <p>Every asserted offset/block/mob/loot value is cited to the D6b
 * extraction specs ({@code phase_d_reports/d6_extraction/*.md}), the port
 * generators (which carry per-line {@code orig GenericDungeon.java} = GD
 * citations), the loot JSONs under
 * {@code data/orespawn/loot_table/chests/}, or the 1.7.10 source under
 * {@code reference_1_7_10_source/sources/danger/orespawn/}.</p>
 *
 * <p>Small structures build INSIDE the {@code empty_large} 48x16x48
 * template via {@link LegacyDungeonPiece#buildNow} and are asserted with
 * absolute positions derived from {@code helper.absolutePos}. The two
 * structures taller than the template (Leaf Monster Dungeon, 21 blocks;
 * Pumpkin, 18 blocks) build at far deterministic offsets
 * {@code absolutePos(0,0,0).offset(K * 512, +10, 50000)} with
 * <b>K = 704 (leaf monster)</b> and <b>K = 711 (pumpkin)</b> — batch base
 * 700 + test index, so no two tests in the suite share a region.</p>
 *
 * <p>No test mutates global state (config, difficulty, time, clocks);
 * spawned entities (the Damsel's Girlfriend, the bounce-probe pig) are
 * discarded before the test succeeds.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructureTestsB {

    /** {@code Block.UPDATE_CLIENTS} — the same flag the generators use. */
    private static final int FLAG = 2;

    // ---------------------------------------------------------------
    // Shared assert helpers
    // ---------------------------------------------------------------

    private static Block blockAt(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getBlockState(pos).getBlock();
    }

    private static void assertBlockIs(GameTestHelper helper, BlockPos pos, Block expected, String what) {
        BlockState state = helper.getLevel().getBlockState(pos);
        helper.assertTrue(state.is(expected),
                what + ": expected " + BuiltInRegistries.BLOCK.getKey(expected) + " at " + pos
                        + ", found " + BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static void assertBlockIn(GameTestHelper helper, BlockPos pos, String what, Block... allowed) {
        BlockState state = helper.getLevel().getBlockState(pos);
        for (Block b : allowed) {
            if (state.is(b)) return;
        }
        helper.fail(what + ": unexpected block " + BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " at " + pos);
    }

    /**
     * Reads the mob id a vanilla spawner was bound to.
     * {@code BaseSpawner.setEntityId} stores it as SpawnData.entity.id
     * (decompiled BaseSpawner.java:48-52 + SpawnData.java:21 field "entity"),
     * recovered via {@code BaseSpawner.save}.
     */
    private static String spawnerMobId(GameTestHelper helper, BlockPos pos) {
        if (!(helper.getLevel().getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            return null;
        }
        CompoundTag tag = spawner.getSpawner().save(new CompoundTag());
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    private static void assertSpawner(GameTestHelper helper, BlockPos pos, EntityType<?> mob, String what) {
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.SPAWNER),
                what + ": expected a mob spawner block at " + pos);
        String expected = BuiltInRegistries.ENTITY_TYPE.getKey(mob).toString();
        String actual = spawnerMobId(helper, pos);
        helper.assertTrue(expected.equals(actual),
                what + ": spawner at " + pos + " should emit " + expected + ", found " + actual);
    }

    private static ResourceKey<LootTable> lootKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("orespawn", path));
    }

    /** Asserts a chest block bound (via setLootTable) to orespawn:{path}. */
    private static void assertLootChest(GameTestHelper helper, BlockPos pos, String path, String what) {
        helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.CHEST),
                what + ": expected a chest at " + pos);
        if (!(helper.getLevel().getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest)) {
            helper.fail(what + ": no container block entity at " + pos);
            return;
        }
        helper.assertTrue(lootKey(path).equals(chest.getLootTable()),
                what + ": chest at " + pos + " should be bound to orespawn:" + path
                        + ", found " + chest.getLootTable());
    }

    /**
     * Mechanism assert on a chest loot table: rolls the actual registered
     * table 150 times through the CHEST param set and checks that
     * (a) every sample yields minRolls..maxRolls stacks (the JSON's uniform
     * rolls; {@code getRandomItemsRaw} emits exactly one stack per pool
     * roll, decompiled LootTable.java:79-96),
     * (b) both extremes appear — false-failure P = 2*((span-1)/span)^150,
     * worst span 5 gives 5.6e-15,
     * (c) every produced item id is in {@code allowed} (the JSON entry
     * list), and
     * (d) every id in {@code mustSee} appears at least once — for the worst
     * table asserted this way (spit_bug_lair, min weight 15/295 over
     * >= 4 rolls x 150 samples = 600+ draws) the false-failure chance is
     * below (1 - 0.0508)^600 * 15 entries = 3e-13; every other table is
     * astronomically safer. Total false-failure per call is far under 1e-9.
     */
    private static void checkLootRolls(GameTestHelper helper, String path, BlockPos origin,
                                       int minRolls, int maxRolls,
                                       Set<String> allowed, Set<String> mustSee, String what) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(lootKey(path));
        helper.assertTrue(table != LootTable.EMPTY, what + ": loot table orespawn:" + path + " is not registered");
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                .create(LootContextParamSets.CHEST);
        boolean sawMin = false;
        boolean sawMax = false;
        Set<String> seen = new HashSet<>();
        for (int s = 0; s < 150; s++) {
            List<ItemStack> stacks = new ArrayList<>();
            table.getRandomItemsRaw(params, stacks::add);
            int n = stacks.size();
            helper.assertTrue(n >= minRolls && n <= maxRolls,
                    what + ": one fill produced " + n + " stacks, expected " + minRolls + "-" + maxRolls);
            sawMin |= n == minRolls;
            sawMax |= n == maxRolls;
            for (ItemStack stack : stacks) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                seen.add(id);
                if (allowed != null) {
                    helper.assertTrue(allowed.contains(id), what + ": loot produced unexpected item " + id);
                }
            }
        }
        helper.assertTrue(sawMin, what + ": minimum roll count " + minRolls + " never observed in 150 fills");
        helper.assertTrue(sawMax, what + ": maximum roll count " + maxRolls + " never observed in 150 fills");
        if (mustSee != null) {
            for (String id : mustSee) {
                helper.assertTrue(seen.contains(id), what + ": expected loot entry " + id + " never observed in 150 fills");
            }
        }
    }

    /** Two-sided Hoeffding bound check; callers document their T/eps math. */
    private static void assertFraction(GameTestHelper helper, int hits, int trials,
                                       double p, double eps, String what) {
        double observed = (double) hits / trials;
        helper.assertTrue(Math.abs(observed - p) <= eps,
                what + ": observed fraction " + observed + " over " + trials
                        + " trials, expected " + p + " +/- " + eps);
    }

    /** Asserts no block of the given type anywhere in the inclusive box. */
    private static void assertNoneInBox(GameTestHelper helper, BlockPos min, BlockPos max,
                                        Block forbidden, String what) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    helper.assertTrue(!helper.getLevel().getBlockState(pos).is(forbidden),
                            what + ": forbidden block " + BuiltInRegistries.BLOCK.getKey(forbidden)
                                    + " found at " + pos);
                }
            }
        }
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    /** The 9-entry DamselContentsList item ids (orig GenericDungeon.java:39;
     *  loot_table/chests/damsel_in_distress.json = girlfriend_island.json,
     *  total weight 315). */
    private static final Set<String> DAMSEL_LIST_ITEMS = Set.of(
            "minecraft:iron_pickaxe", "minecraft:iron_sword", "minecraft:cooked_porkchop",
            "minecraft:beef", "minecraft:cooked_chicken", "minecraft:cooked_cod",
            "orespawn:blt_sandwich", "orespawn:salad", "orespawn:corn_dog");

    // ---------------------------------------------------------------
    // i147 — Gold Fish Bowl
    // ---------------------------------------------------------------

    /**
     * Checklist item i147-goldfishbowl-overworld-ocean-wgen-042 (WGEN-042).
     * GoldFishBowlGenerator content per
     * phase_d_reports/d6_extraction/gold_fish_bowl_spec.md sections 2/4/S2-S5
     * (orig GenericDungeon.java:2408-2488): 5x5 glass base plate (+1) and lid
     * (+8), 7x7 glass-ring layers +2..+7 with sand bed (+2), 2-deep water pool
     * (+3..+4), glowstone in the four lower pool corners (GD:2439-2451),
     * 3-layer air headspace, one "Gold Fish" spawner at (+2,+6,+2)
     * (GD:2480-2487), NO chest (spec section 3). The two faithful oddities
     * (spec S3/S4) — the never-written base ring at j=+1 and wall-top ring at
     * j=+8 — are proven by pre-placed sentinel blocks surviving the build.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i147_gold_fish_bowl_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(21, 1, 21));

        // Sentinels on the two never-written rings (spec S3/S4). Amethyst is
        // not in the builder's palette, so survival proves "never written".
        for (int i = -1; i < 6; i++) {
            for (int k = -1; k < 6; k++) {
                if (i == -1 || k == -1 || i == 5 || k == 5) {
                    level.setBlock(O.offset(i, 1, k), Blocks.AMETHYST_BLOCK.defaultBlockState(), FLAG);
                    level.setBlock(O.offset(i, 8, k), Blocks.AMETHYST_BLOCK.defaultBlockState(), FLAG);
                }
            }
        }

        LegacyDungeonPiece.buildNow(level, O, DungeonType.GOLD_FISH_BOWL);

        // j=+1 — 5x5 glass base plate (GD:2412-2418); ring cells keep the sentinel.
        for (int i = -1; i < 6; i++) {
            for (int k = -1; k < 6; k++) {
                boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                assertBlockIs(helper, O.offset(i, 1, k),
                        rim ? Blocks.AMETHYST_BLOCK : Blocks.GLASS,
                        rim ? "base ring must stay unwritten (spec S3)" : "base plate");
            }
        }
        // j=+2 — glass ring, sand interior (GD:2419-2428).
        // j=+3 — glass ring, water interior with 4 glowstone corners (GD:2429-2451).
        // j=+4 — glass ring, water interior (GD:2452-2461).
        // j=+5..+7 — glass ring, air interior; spawner at (+2,+6,+2) (GD:2462-2487).
        for (int j = 2; j <= 7; j++) {
            for (int i = -1; i < 6; i++) {
                for (int k = -1; k < 6; k++) {
                    boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                    BlockPos pos = O.offset(i, j, k);
                    if (rim) {
                        assertBlockIs(helper, pos, Blocks.GLASS, "bowl wall ring at j=+" + j);
                        continue;
                    }
                    if (j == 2) {
                        assertBlockIs(helper, pos, Blocks.SAND, "sand bed");
                    } else if (j == 3) {
                        boolean glow = (i == 0 || i == 4) && (k == 0 || k == 4);
                        assertBlockIs(helper, pos, glow ? Blocks.GLOWSTONE : Blocks.WATER,
                                glow ? "glowstone pool corner (GD:2439-2451)" : "lower pool water");
                    } else if (j == 4) {
                        assertBlockIs(helper, pos, Blocks.WATER, "upper pool water");
                    } else if (j == 6 && i == 2 && k == 2) {
                        assertSpawner(helper, pos, ModEntities.GOLD_FISH.get(),
                                "Gold Fish spawner (GD:2480-2487)");
                    } else {
                        assertBlockIs(helper, pos, Blocks.AIR, "headspace air");
                    }
                }
            }
        }
        // j=+8 — 5x5 inset lid; wall-top ring keeps the sentinel (spec S4).
        for (int i = -1; i < 6; i++) {
            for (int k = -1; k < 6; k++) {
                boolean rim = i == -1 || k == -1 || i == 5 || k == 5;
                assertBlockIs(helper, O.offset(i, 8, k),
                        rim ? Blocks.AMETHYST_BLOCK : Blocks.GLASS,
                        rim ? "wall-top ring must stay unwritten (spec S4)" : "lid");
            }
        }
        // NO chest anywhere in the enum box (spec section 3: "places NO chest").
        assertNoneInBox(helper, O.offset(-2, 0, -2), O.offset(6, 9, 6), Blocks.CHEST,
                "gold fish bowl has no chest");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i148 — Spit Bug Lair
    // ---------------------------------------------------------------

    /**
     * Checklist item i148-spitbuglair-swamp-wgen-042 (WGEN-042).
     * SpitBugLairGenerator content per
     * phase_d_reports/d6_extraction/spit_bug_lair_spec.md (orig
     * GenericDungeon.java:2638-2696): emerald-ORE antenna at (0,+10..+12)
     * (GD:2655-2657, spec S2), three stacked "Spit Bug" spawners at
     * (0,+7..+9) (GD:2658-2672), taxicab-diamond lime-terracotta platform
     * with green/chiseled rim wall (GD:2674-2690), center loot chest at
     * (0,+1,0) bound to orespawn:chests/spit_bug_lair (GD:2691-2695; JSON
     * rolls uniform 4-7, 15 entries, total weight 295). Biome half of the
     * item: the structure JSON restricts to
     * #orespawn:has_structure/spit_bug_lair, whose resolved holder set must
     * contain minecraft:swamp and NOT minecraft:mangrove_swamp (orig
     * OreSpawnWorld.java:1236-1256 — exact-name "Swampland" gate).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i148_spit_bug_lair_content_and_biome(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(24, 1, 24));
        LegacyDungeonPiece.buildNow(level, O, DungeonType.SPIT_BUG_LAIR);

        // Emerald-ore antenna (GD:2655-2657; ore not block, spec S2).
        for (int j = 10; j <= 12; j++) {
            assertBlockIs(helper, O.offset(0, j, 0), Blocks.EMERALD_ORE, "antenna course");
        }
        // Three stacked Spit Bug spawners (GD:2658-2672).
        for (int j = 7; j <= 9; j++) {
            assertSpawner(helper, O.offset(0, j, 0), ModEntities.ENTITY_SPIT_BUG.get(), "fin-core spawner");
        }
        // Platform: lime floor over the whole taxicab diamond |dx|+|dz| <= 8
        // (GD:2674-2677); rim ring |dx|+|dz| == 8 walled green terracotta at
        // +1, chiseled stone bricks at +2 (GD:2679-2682); interior +1/+2 air
        // (GD:2685-2688) except the chest cell.
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int taxi = Math.abs(dx) + Math.abs(dz);
                if (taxi > 8) continue;
                assertBlockIs(helper, O.offset(dx, 0, dz), Blocks.LIME_TERRACOTTA, "platform floor");
                if (taxi == 8) {
                    assertBlockIs(helper, O.offset(dx, 1, dz), Blocks.GREEN_TERRACOTTA, "rim wall course 1");
                    assertBlockIs(helper, O.offset(dx, 2, dz), Blocks.CHISELED_STONE_BRICKS, "rim wall course 2");
                } else if (dx == 0 && dz == 0) {
                    assertLootChest(helper, O.offset(0, 1, 0), "chests/spit_bug_lair", "center chest");
                } else {
                    assertBlockIs(helper, O.offset(dx, 1, dz), Blocks.AIR, "platform interior");
                    assertBlockIs(helper, O.offset(dx, 2, dz), Blocks.AIR, "platform interior");
                }
            }
        }
        // Fin survivors above the platform-clear band (GD:2647-2654, spec S3):
        // per side column |dx| = 5, mossy step at +4 under green at +5/+6.
        for (int side = -1; side <= 1; side += 2) {
            assertBlockIs(helper, O.offset(5 * side, 4, 0), Blocks.MOSSY_COBBLESTONE, "fin step");
            assertBlockIs(helper, O.offset(5 * side, 5, 0), Blocks.GREEN_TERRACOTTA, "fin course");
            assertBlockIs(helper, O.offset(5 * side, 6, 0), Blocks.GREEN_TERRACOTTA, "fin course");
        }

        // Loot mechanism: rolls uniform 4-7, 15-entry weight-295 list
        // (loot_table/chests/spit_bug_lair.json; orig GenericDungeon.java:42 + :2694).
        checkLootRolls(helper, "chests/spit_bug_lair", O, 4, 7,
                Set.of("minecraft:rotten_flesh", "minecraft:cod", "minecraft:bone", "minecraft:string",
                        "orespawn:amethyst_pickaxe", "orespawn:amethyst_shovel", "orespawn:amethyst_hoe",
                        "orespawn:amethyst_axe", "orespawn:amethyst_sword", "orespawn:amethyst_chestplate",
                        "orespawn:amethyst_leggings", "orespawn:amethyst_helmet", "orespawn:amethyst_boots",
                        "orespawn:instant_garden", "orespawn:instant_shelter"),
                Set.of("minecraft:rotten_flesh", "minecraft:cod", "minecraft:bone", "minecraft:string",
                        "orespawn:amethyst_pickaxe", "orespawn:amethyst_shovel", "orespawn:amethyst_hoe",
                        "orespawn:amethyst_axe", "orespawn:amethyst_sword", "orespawn:amethyst_chestplate",
                        "orespawn:amethyst_leggings", "orespawn:amethyst_helmet", "orespawn:amethyst_boots",
                        "orespawn:instant_garden", "orespawn:instant_shelter"),
                "spit bug lair loot");

        // Biome data assert: structure biomes resolve the tag
        // #orespawn:has_structure/spit_bug_lair (worldgen/structure/
        // spit_bug_lair.json) -> plain swamp only, no mangrove.
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure lair = structures.get(ResourceLocation.fromNamespaceAndPath("orespawn", "spit_bug_lair"));
        helper.assertTrue(lair != null, "structure orespawn:spit_bug_lair must be registered");
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        helper.assertTrue(lair.biomes().contains(biomes.getHolderOrThrow(Biomes.SWAMP)),
                "spit bug lair must generate in minecraft:swamp (OSW:1236-1256 exact-name Swampland)");
        helper.assertTrue(!lair.biomes().contains(biomes.getHolderOrThrow(Biomes.MANGROVE_SWAMP)),
                "spit bug lair must NOT generate in minecraft:mangrove_swamp");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i151 — Ender Reaper Graveyard
    // ---------------------------------------------------------------

    /**
     * Checklist item i151-enderreapergraveyard-end-wgen-042 (WGEN-042).
     * EnderReaperGraveyardGenerator content per
     * phase_d_reports/d6_extraction/graveyard_spec.md (orig
     * GenericDungeon.java:2490-2563 + makeAGrave :2565-2576):
     * air-only foundation skirt (GD:2497-2504 — the sanctioned same-cell
     * terrain probe, LegacyDungeonPiece "Ender Reaper graveyard air-only
     * skirt, orig GenericDungeon.java:2500"), 11x13 end-stone pad,
     * 4-high ROOFLESS iron-bar cage (GD:2512-2522, spec S3), four
     * "Ender Reaper" spawners one cell inside the corners (GD:2523-2554),
     * eight obsidian graves with chests sunk FLUSH into the floor
     * (GD:2555-2576, spec S4) rolling 3-5 stacks of the 4-entry
     * eye/poppy/dandelion/pearl table (loot_table/chests/graveyard.json,
     * orig GenericDungeon.java:43 weight 140 + :2574 fill 3+nextInt(3)).
     * Skirt behavior is proven on controlled mixed terrain: pre-placed
     * purpur "End terrain" cells must survive, pre-air cells must fill
     * with end stone.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i151_ender_reaper_graveyard_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(18, 6, 17));

        // Controlled mixed pre-build terrain under the pad: purpur (not in
        // the builder's palette) marks "existing End terrain", the rest air.
        // Skirt cells are (0..10, -1..-4, 0..12) (GD:2497-2504).
        for (int j = 1; j < 5; j++) {
            for (int i = 0; i < 11; i++) {
                for (int k = 0; k < 13; k++) {
                    boolean terrain = (i + k + j) % 2 == 0;
                    level.setBlock(O.offset(i, -j, k),
                            (terrain ? Blocks.PURPUR_BLOCK : Blocks.AIR).defaultBlockState(), FLAG);
                }
            }
        }

        LegacyDungeonPiece.buildNow(level, O, DungeonType.ENDER_REAPER_GRAVEYARD);

        // Skirt: air-only fill — prior terrain untouched (GD:2500-2501).
        for (int j = 1; j < 5; j++) {
            for (int i = 0; i < 11; i++) {
                for (int k = 0; k < 13; k++) {
                    boolean terrain = (i + k + j) % 2 == 0;
                    assertBlockIs(helper, O.offset(i, -j, k),
                            terrain ? Blocks.PURPUR_BLOCK : Blocks.END_STONE,
                            terrain ? "skirt must preserve pre-build terrain (GD:2500)"
                                    : "skirt must fill pre-build air with end stone (GD:2501)");
                }
            }
        }

        // Grave anatomy (GD:2565-2576): call list GD:2555-2562.
        int[][] graves = {{1, 6}, {3, 4}, {5, 4}, {7, 4}, {3, 8}, {5, 8}, {7, 8}, {9, 6}};
        Set<Long> graveCells = new HashSet<>();   // chest cells in the pad
        Set<Long> footCells = new HashSet<>();    // foot obsidian in the pad
        Set<Long> headCells = new HashSet<>();    // headstone obsidian at y+1
        for (int[] g : graves) {
            graveCells.add(pack(g[0], g[1]));
            footCells.add(pack(g[0], g[1] + 1));
            headCells.add(pack(g[0], g[1] - 1));
        }

        // Pad at y+0 (GD:2505-2511) with flush chests + foot markers.
        for (int i = 0; i < 11; i++) {
            for (int k = 0; k < 13; k++) {
                BlockPos pos = O.offset(i, 0, k);
                if (graveCells.contains(pack(i, k))) {
                    assertLootChest(helper, pos, "chests/graveyard", "flush grave chest");
                    assertBlockIs(helper, pos.above(), Blocks.AIR, "grave chest lid must sit under open air (spec S4)");
                } else if (footCells.contains(pack(i, k))) {
                    assertBlockIs(helper, pos, Blocks.OBSIDIAN, "grave foot marker (GD:2570)");
                } else {
                    assertBlockIs(helper, pos, Blocks.END_STONE, "floor pad");
                }
            }
        }

        // Cage y+1..+4 (GD:2512-2522): perimeter iron bars, interior air
        // except the four corner spawners (y+1) and headstones (y+1).
        for (int j = 1; j <= 4; j++) {
            for (int i = 0; i < 11; i++) {
                for (int k = 0; k < 13; k++) {
                    BlockPos pos = O.offset(i, j, k);
                    boolean perimeter = i == 0 || k == 0 || i == 10 || k == 12;
                    if (perimeter) {
                        assertBlockIs(helper, pos, Blocks.IRON_BARS, "cage fence");
                    } else if (j == 1 && (i == 1 || i == 9) && (k == 1 || k == 11)) {
                        assertSpawner(helper, pos, ModEntities.ENDER_REAPER.get(),
                                "corner Ender Reaper spawner (GD:2523-2554)");
                    } else if (j == 1 && headCells.contains(pack(i, k))) {
                        assertBlockIs(helper, pos, Blocks.OBSIDIAN, "grave headstone (GD:2569)");
                    } else {
                        assertBlockIs(helper, pos, Blocks.AIR, "cage interior");
                    }
                }
            }
        }
        // ROOFLESS (spec S3): nothing is ever written at y+5.
        for (int i = 0; i < 11; i++) {
            for (int k = 0; k < 13; k++) {
                assertBlockIs(helper, O.offset(i, 5, k), Blocks.AIR, "cage must be open-topped (spec S3)");
            }
        }

        // Loot mechanism: rolls uniform 3-5, 4 entries at weight 35 each
        // (loot_table/chests/graveyard.json; orig GenericDungeon.java:43+:2574).
        Set<String> graveItems = Set.of("minecraft:ender_eye", "minecraft:poppy",
                "minecraft:dandelion", "minecraft:ender_pearl");
        checkLootRolls(helper, "chests/graveyard", O, 3, 5, graveItems, graveItems, "grave loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i152 — Water Dragon Lair
    // ---------------------------------------------------------------

    /**
     * Checklist item i152-waterdragonlair-ocean-wgen-042 (WGEN-042).
     * WaterDragonLairGenerator content per
     * phase_d_reports/d6_extraction/water_dragon_lair_spec.md (orig
     * GenericDungeon.java:1959-2057): the y+7 roof plane is checked against
     * a cell-exact replica of the original float-polar writes — disc +
     * radius-(5,6) iron annulus (GD:1972-1982), iron cross spokes
     * (GD:1983-1988), center air skylight ringed by glowstone
     * (GD:1989-1993) — using the original's (int)(c + off + 0.5f) rounding
     * (spec S2). Rim cylinder courses at radius 10 (GD:1994-2012):
     * glowstone / lapis-or-spawn-egg / lapis-or-spawn-egg / glowstone /
     * bedrock / bedrock, with BOTH band materials required across the 144
     * 50/50 draws (false-failure 2 * 0.5^144 = 9e-44). Raft, canopy tree,
     * four "Water Dragon" spawners (GD:2032-2051), and the chest at
     * (0,+1,-1) rolling 4-8 stacks of the 7-entry weight-145 table
     * (loot_table/chests/water_dragon_lair.json; orig GenericDungeon.java:48
     * + :2055).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i152_water_dragon_lair_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(24, 4, 24));
        int cx = O.getX();
        int cy = O.getY();
        int cz = O.getZ();
        LegacyDungeonPiece.buildNow(level, O, DungeonType.WATER_DRAGON_LAIR);

        // ---- Roof plane y+7: replica of GD:1972-1993 (same float math). ----
        Map<Long, Block> roof = new HashMap<>();
        for (float currad = 0.0f; currad < 10.0f; currad += 0.33f) {           // GD:1972
            for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {       // GD:1973
                float curx = (float) (currad * Math.cos(Math.toRadians(curdeg))); // GD:1974
                float curz = (float) (currad * Math.sin(Math.toRadians(curdeg))); // GD:1975
                Block b = (currad > 5.0f && currad < 6.0f) ? Blocks.IRON_BLOCK : Blocks.BEDROCK; // GD:1976-1979
                roof.put(pack((int) (cx + curx + 0.5f), (int) (cz + curz + 0.5f)), b);           // GD:1980
            }
        }
        for (int i = 1; i < 10; i++) {                                          // GD:1983-1988 spokes
            roof.put(pack((int) ((float) (cx + i) + 0.5f), (int) ((float) cz + 0.5f)), Blocks.IRON_BLOCK);
            roof.put(pack((int) ((float) (cx - i) + 0.5f), (int) ((float) cz + 0.5f)), Blocks.IRON_BLOCK);
            roof.put(pack((int) ((float) cx + 0.5f), (int) ((float) (cz + i) + 0.5f)), Blocks.IRON_BLOCK);
            roof.put(pack((int) ((float) cx + 0.5f), (int) ((float) (cz - i) + 0.5f)), Blocks.IRON_BLOCK);
        }
        int cxr = (int) ((float) cx + 0.5f);
        int czr = (int) ((float) cz + 0.5f);
        roof.put(pack(cxr, czr), Blocks.AIR);                                   // GD:1989 skylight
        roof.put(pack((int) ((float) (cx + 1) + 0.5f), czr), Blocks.GLOWSTONE); // GD:1990-1993
        roof.put(pack((int) ((float) (cx - 1) + 0.5f), czr), Blocks.GLOWSTONE);
        roof.put(pack(cxr, (int) ((float) (cz + 1) + 0.5f)), Blocks.GLOWSTONE);
        roof.put(pack(cxr, (int) ((float) (cz - 1) + 0.5f)), Blocks.GLOWSTONE);
        int ironCells = 0;
        int bedrockCells = 0;
        for (Map.Entry<Long, Block> e : roof.entrySet()) {
            int x = (int) (e.getKey() >> 32);
            int z = e.getKey().intValue();
            assertBlockIs(helper, new BlockPos(x, cy + 7, z), e.getValue(), "roof plane cell");
            if (e.getValue() == Blocks.IRON_BLOCK) ironCells++;
            if (e.getValue() == Blocks.BEDROCK) bedrockCells++;
        }
        helper.assertTrue(ironCells >= 20, "iron annulus + spokes expected >= 20 cells, found " + ironCells);
        helper.assertTrue(bedrockCells >= 150, "bedrock disc expected >= 150 cells, found " + bedrockCells);

        // ---- Rim cylinder at radius 10 (GD:1994-2012). ----
        Set<Long> rim = new HashSet<>();
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {            // GD:1995
            float curx = (float) (10.0f * Math.cos(Math.toRadians(curdeg)));    // GD:1996
            float curz = (float) (10.0f * Math.sin(Math.toRadians(curdeg)));    // GD:1997
            rim.add(pack((int) (cx + curx + 0.5f), (int) (cz + curz + 0.5f)));
        }
        Block eggBlock = ModBlocks.WATER_DRAGON_SPAWN_BLOCK.get();
        int eggCells = 0;
        int lapisCells = 0;
        for (long cell : rim) {
            int x = (int) (cell >> 32);
            int z = (int) cell;
            assertBlockIs(helper, new BlockPos(x, cy + 1, z), Blocks.GLOWSTONE, "rim course y+1 (GD:1998)");
            assertBlockIs(helper, new BlockPos(x, cy + 4, z), Blocks.GLOWSTONE, "rim course y+4 (GD:2009)");
            assertBlockIs(helper, new BlockPos(x, cy + 5, z), Blocks.BEDROCK, "rim parapet y+5 (GD:2010)");
            assertBlockIs(helper, new BlockPos(x, cy + 6, z), Blocks.BEDROCK, "rim parapet y+6 (GD:2011)");
            for (int dy = 2; dy <= 3; dy++) {
                Block b = blockAt(helper, new BlockPos(x, cy + dy, z));
                helper.assertTrue(b == Blocks.LAPIS_BLOCK || b == eggBlock,
                        "rim band y+" + dy + " must be lapis or Water Dragon spawn-egg block (GD:1999-2008), found "
                                + BuiltInRegistries.BLOCK.getKey(b) + " at " + x + "," + z);
                if (b == eggBlock) eggCells++;
                else lapisCells++;
            }
        }
        // 144 unconditional 50/50 draws (GD:2000/:2005): both kinds present,
        // false-failure 2 * 0.5^144 = 9e-44.
        helper.assertTrue(eggCells > 0, "no spawn-egg blocks in the rim bands (expected ~50%)");
        helper.assertTrue(lapisCells > 0, "no lapis blocks in the rim bands (expected ~50%)");

        // ---- Raft (GD:2013-2018): 7x7 sand at y+0 over stone at y-1. ----
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                assertBlockIs(helper, O.offset(i, 0, j), Blocks.SAND, "raft sand");
                assertBlockIs(helper, O.offset(i, -1, j), Blocks.STONE, "raft stone underlayer");
            }
        }
        // ---- Canopy tree (GD:2019-2031): leaves slab at y+3 minus the trunk,
        // branches, and spawner cells; trunk logs; apex leaf at y+4. ----
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                boolean trunkOrBranch = (i == 0 && j == 0) || (Math.abs(i) == 1 && Math.abs(j) == 1);
                boolean spawnerCell = (Math.abs(i) == 1 && j == 0) || (i == 0 && Math.abs(j) == 1);
                BlockPos pos = O.offset(i, 3, j);
                if (trunkOrBranch) {
                    assertBlockIs(helper, pos, Blocks.OAK_LOG, "trunk top / diagonal branch (GD:2025/2028-2031)");
                } else if (!spawnerCell) {
                    BlockState state = helper.getLevel().getBlockState(pos);
                    helper.assertTrue(state.is(Blocks.OAK_LEAVES)
                                    && state.getValue(BlockStateProperties.PERSISTENT),
                            "canopy must be persistent oak leaves (spec S5) at " + pos);
                }
            }
        }
        assertBlockIs(helper, O.offset(0, 4, 0), Blocks.OAK_LEAVES, "apex leaf (GD:2024)");
        assertBlockIs(helper, O.offset(0, 1, 0), Blocks.OAK_LOG, "trunk (GD:2027)");
        assertBlockIs(helper, O.offset(0, 2, 0), Blocks.OAK_LOG, "trunk (GD:2026)");
        // ---- Four Water Dragon spawners (GD:2032-2051). ----
        assertSpawner(helper, O.offset(1, 3, 0), ModEntities.WATER_DRAGON.get(), "canopy spawner");
        assertSpawner(helper, O.offset(-1, 3, 0), ModEntities.WATER_DRAGON.get(), "canopy spawner");
        assertSpawner(helper, O.offset(0, 3, 1), ModEntities.WATER_DRAGON.get(), "canopy spawner");
        assertSpawner(helper, O.offset(0, 3, -1), ModEntities.WATER_DRAGON.get(), "canopy spawner");
        // ---- Chest (GD:2052-2056) + the weight-145 table at 4-8 rolls. ----
        assertLootChest(helper, O.offset(0, 1, -1), "chests/water_dragon_lair", "raft chest");
        Set<String> lairItems = Set.of("minecraft:cod", "orespawn:ultimate_axe", "orespawn:ultimate_pickaxe",
                "orespawn:ultimate_shovel", "orespawn:experience_catcher", "minecraft:iron_block",
                "minecraft:rotten_flesh");
        checkLootRolls(helper, "chests/water_dragon_lair", O, 4, 8, lairItems, lairItems, "water dragon lair loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i153 — Leaf Monster Dungeon (far build, K = 704: 21 blocks tall)
    // ---------------------------------------------------------------

    /**
     * Checklist item i153-leafmonsterdungeon-plains-wgen-042 (WGEN-042).
     * LeafMonsterDungeonGenerator content per
     * phase_d_reports/d6_extraction/leaf_monster_dungeon_spec.md (orig
     * GenericDungeon.java:2093-2226): 4x4 oak-log tower with north doorway
     * and ladder shaft (GD:2119-2142), 10x10 plateau at j=9 with leaf rim
     * (GD:2145-2155), 3-tall leaf crown room (GD:2156-2166) holding four
     * "Leaf Monster" spawners (GD:2201-2220) and the chest pair — ONLY the
     * (+1,+10,+5) half loot-bound at 12-16 rolls, the (+2) half
     * deliberately empty (GD:2221-2226, spec S7) — stepped roof rings with
     * the faithful corner notches (GD:2167-2200, spec S6), and foundation
     * roots (GD:2109-2118) that fill ONLY pre-build air/short-grass cells
     * ("Leaf Monster foundation roots, orig GenericDungeon.java:2113"),
     * leaving the solid grass threshold intact. 21 blocks tall, so it
     * builds at the far offset K=704 over controlled terrain.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void i153_leaf_monster_dungeon_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(0, 0, 0)).offset(704 * 512, 10, 50000);

        // Controlled pre-build terrain in the 4x4 x 4-deep root volume
        // (GD:2109-2118). Threshold cells under the doorway stay solid grass;
        // elsewhere a fixed mix of short grass (fills), grass block
        // (survives), and air (fills).
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = -1; j > -5; j--) {
                    Block pre;
                    if (j == -1 && k == 0 && (i == 1 || i == 2)) {
                        pre = Blocks.GRASS_BLOCK;            // the doorway threshold
                    } else if ((i + k + j) % 3 == 0) {
                        pre = Blocks.SHORT_GRASS;            // tallgrass -> filled (GD:2113-2114)
                    } else if ((i + k) % 2 == 0) {
                        pre = Blocks.GRASS_BLOCK;            // solid terrain -> preserved
                    } else {
                        pre = Blocks.AIR;                    // air -> filled
                    }
                    level.setBlock(O.offset(i, j, k), pre.defaultBlockState(), FLAG);
                }
            }
        }

        LegacyDungeonPiece.buildNow(level, O, DungeonType.LEAF_MONSTER_DUNGEON);

        // Foundation roots: air/short-grass filled with logs, solid terrain
        // (incl. the grass threshold) untouched (GD:2109-2118, spec S2).
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = -1; j > -5; j--) {
                    BlockPos pos = O.offset(i, j, k);
                    if (j == -1 && k == 0 && (i == 1 || i == 2)) {
                        assertBlockIs(helper, pos, Blocks.GRASS_BLOCK,
                                "grass doorway threshold must survive the roots (GD:2113)");
                    } else if ((i + k + j) % 3 == 0 || (i + k) % 2 != 0) {
                        assertBlockIs(helper, pos, Blocks.OAK_LOG,
                                "root must fill pre-build air/short-grass (GD:2112-2115)");
                    } else {
                        assertBlockIs(helper, pos, Blocks.GRASS_BLOCK,
                                "root must not replace solid terrain (GD:2113-2114)");
                    }
                }
            }
        }

        // Tower shell (GD:2119-2133): sampled walls, doorway, shaft, ladders.
        assertBlockIs(helper, O.offset(0, 0, 0), Blocks.OAK_LOG, "shell corner");
        assertBlockIs(helper, O.offset(3, 5, 3), Blocks.OAK_LOG, "shell wall");
        assertBlockIs(helper, O.offset(1, 0, 0), Blocks.AIR, "doorway (GD:2123-2125)");
        assertBlockIs(helper, O.offset(2, 1, 0), Blocks.AIR, "doorway (GD:2123-2125)");
        assertBlockIs(helper, O.offset(1, 5, 1), Blocks.AIR, "interior shaft (GD:2126-2128)");
        assertBlockIs(helper, O.offset(2, 8, 1), Blocks.AIR, "interior shaft (GD:2126-2128)");
        for (int j = 0; j < 10; j++) {                       // ladder wall (GD:2134-2142)
            for (int i = 1; i <= 2; i++) {
                BlockPos pos = O.offset(i, j, 2);
                BlockState state = level.getBlockState(pos);
                helper.assertTrue(state.is(Blocks.LADDER)
                                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
                        "ladder facing north expected at " + pos + " (GD:2138-2139 meta 2)");
            }
        }
        // Plateau at j=9 (GD:2145-2155): leaf rim, log field.
        assertBlockIs(helper, O.offset(-3, 9, -3), Blocks.OAK_LEAVES, "plateau rim");
        assertBlockIs(helper, O.offset(6, 9, 6), Blocks.OAK_LEAVES, "plateau rim");
        assertBlockIs(helper, O.offset(-1, 9, -1), Blocks.OAK_LOG, "plateau field");
        assertBlockIs(helper, O.offset(5, 9, -2), Blocks.OAK_LOG, "plateau field");
        // Crown room j=10..12 (GD:2156-2166): leaf walls, air interior.
        assertBlockIs(helper, O.offset(-3, 11, 0), Blocks.OAK_LEAVES, "crown wall");
        assertBlockIs(helper, O.offset(6, 12, 3), Blocks.OAK_LEAVES, "crown wall");
        assertBlockIs(helper, O.offset(0, 11, 0), Blocks.AIR, "crown interior");
        // Roof rings (GD:2167-2200): outer log square, corner notch where the
        // inner leaf ring wins (spec S6), leaf/log caps, 2x2 leaf tip.
        assertBlockIs(helper, O.offset(-2, 13, -2), Blocks.OAK_LOG, "j=13 outer log square");
        assertBlockIs(helper, O.offset(-2, 13, -1), Blocks.OAK_LEAVES, "j=13 corner notch (spec S6)");
        assertBlockIs(helper, O.offset(1, 13, 1), Blocks.AIR, "j=13 centre");
        assertBlockIs(helper, O.offset(0, 14, 0), Blocks.OAK_LEAVES, "j=14 leaf cap");
        assertBlockIs(helper, O.offset(0, 15, 0), Blocks.OAK_LOG, "j=15 log cap");
        assertBlockIs(helper, O.offset(1, 16, 1), Blocks.OAK_LEAVES, "j=16 leaf tip");
        // Four Leaf Monster spawners (GD:2201-2220).
        assertSpawner(helper, O.offset(-2, 10, -2), ModEntities.ENTITY_LEAF_MONSTER.get(), "crown spawner");
        assertSpawner(helper, O.offset(5, 10, 5), ModEntities.ENTITY_LEAF_MONSTER.get(), "crown spawner");
        assertSpawner(helper, O.offset(-2, 10, 5), ModEntities.ENTITY_LEAF_MONSTER.get(), "crown spawner");
        assertSpawner(helper, O.offset(5, 10, -2), ModEntities.ENTITY_LEAF_MONSTER.get(), "crown spawner");
        // Chest pair: exactly one filled (GD:2221-2226, spec S7).
        assertLootChest(helper, O.offset(1, 10, 5), "chests/leaf_monster_dungeon", "loot-bound chest half");
        assertBlockIs(helper, O.offset(2, 10, 5), Blocks.CHEST, "empty chest half");
        if (level.getBlockEntity(O.offset(2, 10, 5)) instanceof RandomizableContainerBlockEntity emptyHalf) {
            helper.assertTrue(emptyHalf.getLootTable() == null,
                    "the (+2,+10,+5) chest half must stay deliberately empty (spec S7)");
        } else {
            helper.fail("no container block entity for the (+2,+10,+5) chest half");
        }
        // Loot mechanism: rolls uniform 12-16, 9-entry weight-255 list
        // (loot_table/chests/leaf_monster_dungeon.json; orig GenericDungeon.java:46 + :2225).
        Set<String> leafItems = Set.of("minecraft:flower_pot", "minecraft:oak_sapling", "minecraft:oak_leaves",
                "minecraft:dirt", "minecraft:oak_log", "orespawn:poison_sword", "minecraft:rotten_flesh");
        checkLootRolls(helper, "chests/leaf_monster_dungeon", O, 12, 16, leafItems, leafItems,
                "leaf monster dungeon loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i154 — Mini Dungeon
    // ---------------------------------------------------------------

    /**
     * Checklist item i154-minidungeon-islands-wgen-042 (WGEN-042).
     * MiniDungeonGenerator content per
     * phase_d_reports/d6_extraction/mini_dungeon_spec.md (orig
     * GenericDungeon.java:2229-2406): the full 10x10x7 cage map
     * (GD:2238-2266), grass roof rings at j=7/j=8 (GD:2267-2286), the
     * floating 12-spawner Butterfly ring at j=9 (GD:2302-2312, spec S6),
     * four cobble corner towers capped by spawners at j=11 (GD:2313-2352),
     * six floor spawners (GD:2353-2400), and the loot chest at (+3,+1,+3)
     * rolling 4-8 stacks of the 6-entry weight-190 table
     * (loot_table/chests/mini_dungeon.json; orig GenericDungeon.java:45 + :2404).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i154_mini_dungeon_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(20, 1, 20));
        LegacyDungeonPiece.buildNow(level, O, DungeonType.MINI_DUNGEON);

        // Floor spawners/chest occupy loop-1 interior air cells (GD:2353-2405).
        Map<Long, EntityType<?>> floorSpawners = new HashMap<>();
        floorSpawners.put(pack(1, 1), ModEntities.ENTITY_TERRIBLE_TERROR.get());
        floorSpawners.put(pack(8, 8), ModEntities.ENTITY_TERRIBLE_TERROR.get());
        floorSpawners.put(pack(8, 1), ModEntities.ENTITY_BUTTERFLY.get());
        floorSpawners.put(pack(1, 8), ModEntities.ENTITY_BUTTERFLY.get());
        floorSpawners.put(pack(4, 4), ModEntities.ENTITY_LURKING_TERROR.get());
        floorSpawners.put(pack(5, 5), ModEntities.ENTITY_LURKING_TERROR.get());

        // Full cage map (GD:2238-2266): sequential rules, last wins.
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 10; k++) {
                for (int j = 0; j < 7; j++) {
                    BlockPos pos = O.offset(i, j, k);
                    if (j == 1 && floorSpawners.containsKey(pack(i, k))) {
                        assertSpawner(helper, pos, floorSpawners.get(pack(i, k)), "floor spawner");
                        continue;
                    }
                    if (j == 1 && i == 3 && k == 3) {
                        assertLootChest(helper, pos, "chests/mini_dungeon", "floor chest");
                        continue;
                    }
                    boolean perimeter = i == 0 || k == 0 || i == 9 || k == 9;
                    Block expected = Blocks.AIR;
                    if (perimeter) expected = Blocks.IRON_BARS;
                    if ((i == 0 || i == 9) && (k == 0 || k == 9)) expected = Blocks.COBBLESTONE;
                    if (j == 0) expected = Blocks.COBBLESTONE;
                    if (j == 6 && perimeter) expected = Blocks.COBBLESTONE;
                    assertBlockIs(helper, pos, expected, "cage cell");
                }
            }
        }
        // Grass roof rings (GD:2267-2286).
        for (int i = 1; i < 9; i++) {
            for (int k = 1; k < 9; k++) {
                boolean ring = i == 1 || i == 8 || k == 1 || k == 8;
                assertBlockIs(helper, O.offset(i, 7, k), ring ? Blocks.GRASS_BLOCK : Blocks.AIR, "j=7 grass ring");
            }
        }
        for (int i = 2; i < 8; i++) {
            for (int k = 2; k < 8; k++) {
                boolean ring = i == 2 || i == 7 || k == 2 || k == 7;
                assertBlockIs(helper, O.offset(i, 8, k), ring ? Blocks.GRASS_BLOCK : Blocks.AIR, "j=8 grass ring");
            }
        }
        // The 12-spawner Butterfly ring at j=9, floating on air (GD:2302-2312).
        int ringSpawners = 0;
        for (int i = 3; i < 7; i++) {
            for (int k = 3; k < 7; k++) {
                if (i != 3 && i != 6 && k != 3 && k != 6) {
                    assertBlockIs(helper, O.offset(i, 9, k), Blocks.AIR, "ring interior");
                    continue;
                }
                assertSpawner(helper, O.offset(i, 9, k), ModEntities.ENTITY_BUTTERFLY.get(), "j=9 ring spawner");
                ringSpawners++;
                assertBlockIs(helper, O.offset(i, 8, k), Blocks.AIR, "ring must float on air (spec S6)");
            }
        }
        helper.assertTrue(ringSpawners == 12, "expected 12 ring spawners, found " + ringSpawners);
        // Corner towers + caps (GD:2313-2352).
        int[][] corners = {{0, 0}, {9, 9}, {0, 9}, {9, 0}};
        EntityType<?>[] caps = {ModEntities.ENTITY_TERRIBLE_TERROR.get(), ModEntities.ENTITY_BUTTERFLY.get(),
                ModEntities.ENTITY_TERRIBLE_TERROR.get(), ModEntities.ENTITY_BUTTERFLY.get()};
        for (int c = 0; c < 4; c++) {
            for (int j = 7; j < 11; j++) {
                assertBlockIs(helper, O.offset(corners[c][0], j, corners[c][1]), Blocks.COBBLESTONE, "corner tower");
            }
            assertSpawner(helper, O.offset(corners[c][0], 11, corners[c][1]), caps[c], "corner cap spawner");
        }
        // Staircase (GD:2287-2301): 6 steps of 4 planks + fences + torches.
        for (int m = 0; m < 6; m++) {
            int i = -6 + m;
            int j = 1 + m;
            for (int dz = 0; dz < 4; dz++) {
                assertBlockIs(helper, O.offset(i, j, 3 + dz), Blocks.OAK_PLANKS, "stair tread");
            }
            assertBlockIs(helper, O.offset(i, j + 1, 3), Blocks.OAK_FENCE, "stair railing");
            assertBlockIs(helper, O.offset(i, j + 1, 6), Blocks.OAK_FENCE, "stair railing");
            assertBlockIs(helper, O.offset(i, j + 2, 3), Blocks.TORCH, "railing torch");
            assertBlockIs(helper, O.offset(i, j + 2, 6), Blocks.TORCH, "railing torch");
        }
        // Loot mechanism: rolls uniform 4-8, 6-entry weight-190 list.
        Set<String> miniItems = Set.of("minecraft:golden_apple", "orespawn:crystal_apple",
                "orespawn:cooked_bacon", "orespawn:fire_fish", "orespawn:instant_garden",
                "orespawn:instant_shelter");
        checkLootRolls(helper, "chests/mini_dungeon", O, 4, 8, miniItems, miniItems, "mini dungeon loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i155 — Cephadrome Altar
    // ---------------------------------------------------------------

    /**
     * Checklist item i155-cephadromealtar-islands-wgen-042 (WGEN-042).
     * "Altar per spec": CephadromeAltarGenerator content per
     * phase_d_reports/d6_extraction/cephadrome_altar_spec.md (orig
     * GenericDungeon.java:4731-4829): 9x9 cobble base (GD:4736-4744), 7x7
     * and 5x5 tiers with stone-brick cross/corners (GD:4745-4758,
     * :4797-4812), corner pillars stone-brick / stone-brick / end-stone /
     * Extreme Torch (GD:4761-4796), 3x3 cap with end-stone corners and the
     * Eye-of-Ender block dead centre (GD:4813-4828). Per spec section 4 and
     * S2 the altar has NO spawner and NO chest — the "spawning" is the
     * player-triggered Extreme Torch summon (ITEM-017), so the spec-correct
     * assert is the ABSENCE of spawner/chest blocks; the checklist line's
     * "with its spawners" resolves to that spec statement.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i155_cephadrome_altar_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(24, 1, 24));
        LegacyDungeonPiece.buildNow(level, O, DungeonType.CEPHADROME_ALTAR);

        Block torch = ModBlocks.EXTREME_TORCH.get();
        Block eye = ModBlocks.BLOCK_EYE_OF_ENDER.get();
        for (int i = -4; i <= 4; i++) {
            for (int k = -4; k <= 4; k++) {
                boolean in7 = Math.abs(i) <= 3 && Math.abs(k) <= 3;
                boolean corner3 = Math.abs(i) == 3 && Math.abs(k) == 3;
                // j=0 — 9x9 solid cobblestone base plate (GD:4736-4744).
                assertBlockIs(helper, O.offset(i, 0, k), Blocks.COBBLESTONE, "base plate");
                // j=+1 — 7x7 tier: stone-brick cross + corners, else cobble
                // (GD:4745-4758); outside the 7x7 nothing is written.
                Block j1 = Blocks.AIR;
                if (in7) {
                    j1 = Blocks.COBBLESTONE;
                    if (i == 0 || k == 0) j1 = Blocks.STONE_BRICKS;
                    if (corner3) j1 = Blocks.STONE_BRICKS;
                }
                assertBlockIs(helper, O.offset(i, 1, k), j1, "7x7 tier");
                // j=+2 — pillar course 2 at the 7x7 corners; 5x5 tier
                // re-solidified over the air layer (GD:4761-4772 + :4797-4812).
                Block j2 = Blocks.AIR;
                if (corner3) {
                    j2 = Blocks.STONE_BRICKS;
                } else if (Math.abs(i) <= 2 && Math.abs(k) <= 2) {
                    j2 = Blocks.COBBLESTONE;
                    if (i == 0 || k == 0) j2 = Blocks.STONE_BRICKS;
                    if (Math.abs(i) == 2 && Math.abs(k) == 2) j2 = Blocks.STONE_BRICKS;
                }
                assertBlockIs(helper, O.offset(i, 2, k), j2, "5x5 tier / pillar course 2");
                // j=+3 — pillar course 3 (end stone) at the 7x7 corners; 3x3
                // cap with the eye centre and end-stone corners (GD:4773-4784
                // + :4813-4828).
                Block j3 = Blocks.AIR;
                if (corner3) {
                    j3 = Blocks.END_STONE;
                } else if (Math.abs(i) <= 1 && Math.abs(k) <= 1) {
                    j3 = Blocks.COBBLESTONE;
                    if (i == 0 && k == 0) j3 = eye;
                    if (Math.abs(i) == 1 && Math.abs(k) == 1) j3 = Blocks.END_STONE;
                }
                assertBlockIs(helper, O.offset(i, 3, k), j3, "3x3 cap / pillar course 3");
                // j=+4 — Extreme Torch on each pillar top, air elsewhere
                // (GD:4785-4796); the (0,+4,0) air cell is the summoning spot.
                assertBlockIs(helper, O.offset(i, 4, k), corner3 ? torch : Blocks.AIR, "pillar tops");
            }
        }
        // Per spec section 4/S2: no spawner, no chest anywhere in the box.
        assertNoneInBox(helper, O.offset(-5, 0, -5), O.offset(5, 5, 5), Blocks.SPAWNER,
                "cephadrome altar has no spawner (spec section 4)");
        assertNoneInBox(helper, O.offset(-5, 0, -5), O.offset(5, 5, 5), Blocks.CHEST,
                "cephadrome altar has no chest (spec S2)");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i157 — Bouncy Castle
    // ---------------------------------------------------------------

    /**
     * Checklist item i157-bouncycastle-desert (Phase D6b batch 3).
     * BouncyCastleGenerator content per
     * phase_d_reports/d6_extraction/bouncy_castle_spec.md (orig
     * GenericDungeon.java:3106-3205): the full 9x9x5 Lavafoam shell with
     * red-terracotta corner pillars and the north doorway (GD:3129-3154,
     * spec S3/S4), the nine Silverfish/Rat/Scorpion spawners at y+3
     * (GD:3155-3199 — vanilla Silverfish, spec S6), and the north-facing
     * chest at (+3,+3,+3) (GD:3200-3204, meta 2) rolling 6-10 stacks of
     * the 7-entry weight-180 table (loot_table/chests/bouncy_castle.json;
     * orig GenericDungeon.java:41 + :3203). "Bouncy, friction 1.1":
     * Block#getFriction() == 1.1f (ITEM-009, AUDIT_FINDINGS.md — orig
     * Lavafoam.java:23-26; port ModBlocks.java LAVAFOAM registration) and
     * the collision push sets the away-from-centre velocity component to
     * 0.45 (orig Lavafoam.java:92; port block/Lavafoam.java entityInside).
     * "Sunk to the sand surface" is the SAND_SURFACE_MINUS1 anchor — the
     * scan hit's posY-1, the top sand block itself (orig
     * OreSpawnWorld.java:1292; spec section 7) — asserted as placement-mode
     * data.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i157_bouncy_castle_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(24, 1, 24));
        LegacyDungeonPiece.buildNow(level, O, DungeonType.BOUNCY_CASTLE);

        Block lavafoam = ModBlocks.LAVAFOAM.get();
        // Spawner trio per wall (GD:3155-3199): south z=+3, east x=+3, west x=-3.
        Map<Long, EntityType<?>> spawners = new HashMap<>();
        spawners.put(pack(-1, 3), EntityType.SILVERFISH);
        spawners.put(pack(0, 3), ModEntities.ENTITY_RAT.get());
        spawners.put(pack(1, 3), ModEntities.ENTITY_SCORPION.get());
        spawners.put(pack(3, -1), EntityType.SILVERFISH);
        spawners.put(pack(3, 0), ModEntities.ENTITY_RAT.get());
        spawners.put(pack(3, 1), ModEntities.ENTITY_SCORPION.get());
        spawners.put(pack(-3, -1), EntityType.SILVERFISH);
        spawners.put(pack(-3, 0), ModEntities.ENTITY_RAT.get());
        spawners.put(pack(-3, 1), ModEntities.ENTITY_SCORPION.get());

        // Full shell map (GD:3129-3154): rules in source order, later wins.
        for (int i = -4; i <= 4; i++) {          // X
            for (int j = -4; j <= 4; j++) {      // Z
                for (int k = 0; k < 5; k++) {    // Y
                    BlockPos pos = O.offset(i, k, j);
                    if (k == 3 && spawners.containsKey(pack(i, j))) {
                        assertSpawner(helper, pos, spawners.get(pack(i, j)), "castle spawner");
                        continue;
                    }
                    if (k == 3 && i == 3 && j == 3) {
                        assertLootChest(helper, pos, "chests/bouncy_castle", "corner chest");
                        BlockState chest = level.getBlockState(pos);
                        helper.assertTrue(chest.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
                                "chest must face north toward the door wall (GD:3200 meta 2)");
                        continue;
                    }
                    Block expected = Blocks.AIR;
                    if (k == 4 || k == 0) expected = lavafoam;                       // GD:3134-3136
                    if (i == -4 || i == 4) expected = lavafoam;                      // GD:3137-3139
                    if (j == -4 || j == 4) expected = lavafoam;                      // GD:3140-3142
                    if ((i == -4 || i == 4) && (j == -4 || j == 4)) expected = Blocks.RED_TERRACOTTA; // GD:3143-3146
                    if ((k == 1 || k == 2) && i == 0 && j == -4) expected = Blocks.AIR;               // GD:3147-3150 doorway
                    assertBlockIs(helper, pos, expected, "castle shell cell");
                }
            }
        }

        // Friction 1.1 (ITEM-009; orig Lavafoam.java:23-26).
        helper.assertTrue(lavafoam.getFriction() == 1.1f,
                "lavafoam friction must be 1.1 (ITEM-009), found " + lavafoam.getFriction());

        // Bounce: the collision handler sets the away-from-centre velocity to
        // 0.45 (orig Lavafoam.java:92 field_70159_w = 0.45f for an entity
        // east of the block centre; port block/Lavafoam.java). Invoked via
        // the public BlockState.entityInside dispatch on a built wall cell.
        BlockPos wall = O.offset(-4, 2, 0);
        BlockState wallState = level.getBlockState(wall);
        helper.assertTrue(wallState.is(lavafoam), "expected a lavafoam wall cell at " + wall);
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 1));
        pig.moveTo(wall.getX() + 0.9, wall.getY(), wall.getZ() + 0.5, 0.0f, 0.0f);
        pig.setDeltaMovement(Vec3.ZERO);
        wallState.entityInside(level, wall, pig);
        helper.assertTrue(Math.abs(pig.getDeltaMovement().x - 0.45) < 1.0e-6,
                "lavafoam must push an entity east of centre with x velocity 0.45 (orig Lavafoam.java:92), got "
                        + pig.getDeltaMovement().x);
        pig.discard();

        // "Sunk to the sand surface": the worldgen anchor is the sand block
        // itself (posY-1, orig OreSpawnWorld.java:1292 -> spec section 7).
        helper.assertTrue(DungeonType.BOUNCY_CASTLE.placement == DungeonType.PlacementMode.SAND_SURFACE_MINUS1,
                "BOUNCY_CASTLE must anchor with SAND_SURFACE_MINUS1 (OSW:1292, spec section 7)");

        // Loot mechanism: rolls uniform 6-10, 7-entry weight-180 list.
        Set<String> castleItems = Set.of("minecraft:rotten_flesh", "minecraft:cod", "minecraft:bone",
                "minecraft:string", "minecraft:poppy", "minecraft:dandelion", "minecraft:ender_pearl");
        checkLootRolls(helper, "chests/bouncy_castle", O, 6, 10, castleItems, castleItems, "bouncy castle loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i158 — Damsel In Distress
    // ---------------------------------------------------------------

    /**
     * Checklist item i158-damselindistress-village-dim (Phase D6b batch 3).
     * DamselInDistressGenerator content per
     * phase_d_reports/d6_extraction/damsel_in_distress_spec.md (orig
     * GenericDungeon.java:3625-3733): 9x9x5 cobble/mossy shell with the 3x3
     * front doorway (GD:3643-3667, mossy = 1/8 rolls so shell cells assert
     * cobble-OR-mossy), asymmetric roof courses (GD:3669-3688, spec S6 — the
     * unwritten back strips assert air), triangular front gable to y+9
     * (GD:3689-3705), the 7x4 iron-bar jail wall at z=+1 (GD:3706-3711,
     * spec S2), two Scorpion spawners (GD:3712-3721), the north-facing jail
     * chest at (+3,+1,+3) (GD:3722-3726, meta 2) rolling 10-14 stacks of
     * the 9-entry weight-315 iron-tools+foods table
     * (loot_table/chests/damsel_in_distress.json; orig GenericDungeon.java:39
     * + :3725), and the live Girlfriend at the jail corner (-2,+1,+3)
     * (GD:3727-3732, spec S9 — block-corner stand, class-level persistence:
     * orig Girlfriend.java:850-852 canDespawn false -> port
     * removeWhenFarAway false, no PersistenceRequired flag). Village-
     * dimension selection is a data assert: worldgen/structure/
     * damsel_in_distress.json binds exactly the orespawn:village_biome and
     * the Village level exists; the anchor is VILLAGE_GRASS_SURFACE
     * (orig OreSpawnWorld.java:1301-1317, spec S7).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i158_damsel_in_distress_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(24, 1, 24));
        LegacyDungeonPiece.buildNow(level, O, DungeonType.DAMSEL_IN_DISTRESS);

        // Shell (GD:3643-3667): i = X, j = Z, k = Y; doorway carved after the
        // mossy roll; jail bars / spawners / chest overwrite interior air.
        for (int i = -4; i <= 4; i++) {
            for (int j = -4; j <= 4; j++) {
                for (int k = 0; k < 5; k++) {
                    BlockPos pos = O.offset(i, k, j);
                    boolean doorway = (k == 1 || k == 2 || k == 3)
                            && (i == 0 || i == -1 || i == 1) && j == -4;
                    if (doorway) {
                        assertBlockIs(helper, pos, Blocks.AIR, "3x3 doorway (GD:3660-3663)");
                        continue;
                    }
                    boolean shell = k == 0 || i == -4 || i == 4 || j == -4 || j == 4;
                    if (shell) {
                        assertBlockIn(helper, pos, "shell must be cobble or 1/8 mossy (GD:3648-3659)",
                                Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
                        continue;
                    }
                    // Interior overwrites.
                    if (j == 1 && k >= 1 && k <= 4 && Math.abs(i) <= 3) {
                        assertBlockIs(helper, pos, Blocks.IRON_BARS,
                                "7x4 jail wall at z=+1 (GD:3706-3711)");
                    } else if (k == 1 && j == -3 && (i == -3 || i == 3)) {
                        assertSpawner(helper, pos, ModEntities.ENTITY_SCORPION.get(),
                                "front-room Scorpion spawner (GD:3712-3721)");
                    } else if (k == 1 && i == 3 && j == 3) {
                        assertLootChest(helper, pos, "chests/damsel_in_distress", "jail chest");
                        helper.assertTrue(level.getBlockState(pos)
                                        .getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH,
                                "jail chest must face north into the cell (GD:3722 meta 2)");
                    } else {
                        assertBlockIs(helper, pos, Blocks.AIR, "hut interior");
                    }
                }
            }
        }
        // Roof course 1 at y+5: X -3..+3, Z -4..+3 (GD:3669-3678); the z=+4
        // strip is never written (spec S6).
        for (int i = -3; i <= 3; i++) {
            for (int j = -4; j <= 3; j++) {
                assertBlockIn(helper, O.offset(i, 5, j), "roof course 1",
                        Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
            }
            assertBlockIs(helper, O.offset(i, 5, 4), Blocks.AIR,
                    "roof course 1 stops short of the back wall (spec S6)");
        }
        // Roof course 2 at y+6: X -2..+2, Z -4..+2 (GD:3679-3688).
        for (int i = -2; i <= 2; i++) {
            for (int j = -4; j <= 2; j++) {
                assertBlockIn(helper, O.offset(i, 6, j), "roof course 2",
                        Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
            }
            assertBlockIs(helper, O.offset(i, 6, 3), Blocks.AIR,
                    "roof course 2 stops short (spec S6)");
        }
        // Front gable at z=-4 (GD:3689-3705): rows shrink 9/7/5/3/1 over
        // y+5..+9; cells outside each row were never written.
        int[] halfWidth = {4, 3, 2, 1, 0};
        for (int row = 0; row < 5; row++) {
            int y = 5 + row;
            for (int i = -4; i <= 4; i++) {
                if (Math.abs(i) <= halfWidth[row]) {
                    assertBlockIn(helper, O.offset(i, y, -4), "gable row y+" + y,
                            Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
                } else {
                    assertBlockIs(helper, O.offset(i, y, -4), Blocks.AIR, "outside the gable triangle");
                }
            }
        }

        // The live Girlfriend (GD:3727-3732): exactly one, standing at the
        // block corner (-2,+1,+3) in the jail cell.
        AABB hut = new AABB(O.getX() - 5, O.getY(), O.getZ() - 5, O.getX() + 6, O.getY() + 10, O.getZ() + 6);
        var girls = level.getEntities(ModEntities.GIRLFRIEND.get(), hut, e -> true);
        helper.assertTrue(girls.size() == 1, "expected exactly 1 Girlfriend in the hut, found " + girls.size());
        Mob girl = girls.get(0);
        helper.assertTrue(Math.abs(girl.getX() - (O.getX() - 2)) < 1.0e-9
                        && Math.abs(girl.getY() - (O.getY() + 1)) < 1.0e-9
                        && Math.abs(girl.getZ() - (O.getZ() + 3)) < 1.0e-9,
                "Girlfriend must stand at the block corner (-2,+1,+3) (spec S9), found "
                        + girl.getX() + "," + girl.getY() + "," + girl.getZ());
        // Persistence by CLASS, not flag (orig Girlfriend.java:850-852; the
        // generator uses spawnEntity, not spawnPersistent):
        helper.assertTrue(!girl.isPersistenceRequired(),
                "Girlfriend must NOT carry the PersistenceRequired flag (generator spec section 4.1)");
        helper.assertTrue(!girl.removeWhenFarAway(16384.0),
                "Girlfriend.removeWhenFarAway must be false at any distance (orig Girlfriend.java:850-852)");
        girl.checkDespawn();
        helper.assertTrue(!girl.isRemoved(),
                "Girlfriend must survive checkDespawn with no players nearby");
        girl.discard();

        // Loot mechanism: rolls uniform 10-14, 9-entry weight-315 list.
        checkLootRolls(helper, "chests/damsel_in_distress", O, 10, 14,
                DAMSEL_LIST_ITEMS, DAMSEL_LIST_ITEMS, "damsel loot");

        // Village-dimension selection (data asserts).
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure damsel = structures.get(ResourceLocation.fromNamespaceAndPath("orespawn", "damsel_in_distress"));
        helper.assertTrue(damsel != null, "structure orespawn:damsel_in_distress must be registered");
        List<Holder<Biome>> biomeList = damsel.biomes().stream().toList();
        helper.assertTrue(biomeList.size() == 1
                        && biomeList.get(0).unwrapKey().orElseThrow().location()
                        .equals(ResourceLocation.fromNamespaceAndPath("orespawn", "village_biome")),
                "damsel_in_distress must bind exactly orespawn:village_biome (worldgen/structure/damsel_in_distress.json)");
        helper.assertTrue(level.getServer().getLevel(ModDimensionKeys.VILLAGE) != null,
                "the Village dimension must exist on this server");
        helper.assertTrue(DungeonType.DAMSEL_IN_DISTRESS.placement == DungeonType.PlacementMode.VILLAGE_GRASS_SURFACE,
                "DAMSEL_IN_DISTRESS must anchor with VILLAGE_GRASS_SURFACE (OSW:1301-1317, spec S7)");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i159 — Girlfriend Island
    // ---------------------------------------------------------------

    /**
     * Checklist item i159-girlfriendisland-ocean (Phase D6b batch 3).
     * GirlfriendIslandGenerator per
     * phase_d_reports/d6_extraction/girlfriend_island_spec.md (orig
     * GenericDungeon.java:4962-5028): Monster Island's geometry TWIN — the
     * island/tree loops and chest positions are byte-identical to orig
     * :5181-5209/:5230-5239, so the two structures are built side by side
     * and block-diffed over the full shared box (block STATES must match on
     * every cell; only block-entity data — spawner ids, loot bindings —
     * differs). Spawners are the four FIXED ids Girlfriend / Boyfriend /
     * Gold Fish x2 (GD:4998-5017; zero RNG draws per spec S4 — no
     * Monster-Island-style 50/50 pick, which is cross-checked by Monster
     * Island's four spawners being uniformly one of Sea Monster/Sea Viper,
     * orig :5176-5180). TWO chests at (0,+1,-1)/(0,+1,+1) bound to
     * orespawn:chests/girlfriend_island rolling 4-8 stacks of the shared
     * DamselContentsList (orig GenericDungeon.java:39 + :5021/:5026).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void i159_girlfriend_island_twin_and_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos G = helper.absolutePos(new BlockPos(12, 3, 24));
        BlockPos M = helper.absolutePos(new BlockPos(34, 3, 24));
        LegacyDungeonPiece.buildNow(level, G, DungeonType.GIRLFRIEND_ISLAND);
        LegacyDungeonPiece.buildNow(level, M, DungeonType.MONSTER_ISLAND);

        // Geometry-twin block diff over the shared enum box (X -6..+6,
        // Y -2..+5, Z -4..+4): every block STATE must match.
        for (int i = -6; i <= 6; i++) {
            for (int j = -2; j <= 5; j++) {
                for (int k = -4; k <= 4; k++) {
                    BlockState gs = level.getBlockState(G.offset(i, j, k));
                    BlockState ms = level.getBlockState(M.offset(i, j, k));
                    helper.assertTrue(gs == ms,
                            "geometry-twin mismatch at offset (" + i + "," + j + "," + k + "): girlfriend="
                                    + gs + " monster=" + ms);
                }
            }
        }
        // Island body spot checks (GD:4969-4984): sand over stone, lens tips.
        assertBlockIs(helper, G.offset(0, 0, 0), Blocks.SAND, "island sand");
        assertBlockIs(helper, G.offset(0, -1, 0), Blocks.STONE, "island stone underlayer");
        assertBlockIs(helper, G.offset(5, 0, 1), Blocks.SAND, "lens tip half-width 1");
        assertBlockIs(helper, G.offset(5, 0, 2), Blocks.AIR, "beyond the lens tip");

        // The four FIXED spawner ids (GD:4998-5017; spec S4 — no roll).
        assertSpawner(helper, G.offset(1, 3, 0), ModEntities.GIRLFRIEND.get(), "canopy spawner east");
        assertSpawner(helper, G.offset(-1, 3, 0), ModEntities.BOYFRIEND.get(), "canopy spawner west");
        assertSpawner(helper, G.offset(0, 3, 1), ModEntities.GOLD_FISH.get(), "canopy spawner south");
        assertSpawner(helper, G.offset(0, 3, -1), ModEntities.GOLD_FISH.get(), "canopy spawner north");
        // Cross-check: Monster Island's four spawners are uniformly ONE mob
        // out of Sea Monster / Sea Viper (orig :5176-5180) — the roll the
        // Girlfriend Island must NOT have.
        String pick = spawnerMobId(helper, M.offset(1, 3, 0));
        String seaMonster = BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.SEA_MONSTER.get()).toString();
        String seaViper = BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.SEA_VIPER.get()).toString();
        helper.assertTrue(seaMonster.equals(pick) || seaViper.equals(pick),
                "monster island spawner must be Sea Monster or Sea Viper, found " + pick);
        helper.assertTrue(pick.equals(spawnerMobId(helper, M.offset(-1, 3, 0)))
                        && pick.equals(spawnerMobId(helper, M.offset(0, 3, 1)))
                        && pick.equals(spawnerMobId(helper, M.offset(0, 3, -1))),
                "monster island's four spawners must share the single 50/50 pick (orig :5176-5180)");

        // TWO chests flanking the trunk, both bound to the girlfriend_island
        // table (GD:5018-5027).
        assertLootChest(helper, G.offset(0, 1, -1), "chests/girlfriend_island", "north trunk chest");
        assertLootChest(helper, G.offset(0, 1, 1), "chests/girlfriend_island", "south trunk chest");
        // Rolls uniform 4-8 over the shared 9-entry weight-315 DamselContentsList.
        checkLootRolls(helper, "chests/girlfriend_island", G, 4, 8,
                DAMSEL_LIST_ITEMS, DAMSEL_LIST_ITEMS, "girlfriend island loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i160 — Stinky House (statistical, 20 rebuilds)
    // ---------------------------------------------------------------

    /**
     * Checklist item i160-stinkyhouse-islands (Phase D6b batch 3).
     * StinkyHouseGenerator per
     * phase_d_reports/d6_extraction/stinky_house_spec.md (orig
     * GenericDungeon.java:5314-5381). Deterministic content: the 2x2
     * doorway force-cleared AFTER the decay roll (GD:5359-5361, spec S2),
     * 8 corner-adjacent window cells (GD:5350-5352, spec S8), Stink Bug +
     * Stinky spawners (GD:5366-5375), the centre chest at (+6,+1,+4)
     * rolling 8-12 stacks of the 7-entry weight-215 table INCLUDING both
     * stink eggs (loot_table/chests/stinky_house.json; orig
     * GenericDungeon.java:28 + :5379). Probabilistic content over N = 20
     * rebuilds on a sentinel-prefilled canvas:
     * <ul>
     * <li>yard perimeter fence survival p = 2/3 (1/3 knockout,
     *     GD:5330-5335): T = 20*80 = 1600 trials, eps = 0.09 — Hoeffding
     *     false-failure 2*exp(-2*1600*0.0081) = 1.1e-11;</li>
     * <li>interior-yard dead bushes p = 1/10 (GD:5336-5338): T = 20*215 =
     *     4300, eps = 0.06 — 2*exp(-2*4300*0.0036) = 7.2e-13;</li>
     * <li>roof-cell decay survival p = 9/10 (GD:5353-5358): T = 20*130 =
     *     2600, eps = 0.07 — 2*exp(-2*2600*0.0049) = 1.7e-11;</li>
     * <li>each window seen as a pane at least once (p = 9/10 per build,
     *     miss chance 8 * 10^-20) and at least one bush standing in the
     *     fence line (spec S6; p = 1/30 per perimeter cell, miss chance
     *     (29/30)^1600 = 3.5e-24).</li>
     * </ul>
     * Total false-failure is far below 1e-9. The yard's never-writes-air
     * invariant (GD:5339, spec S3) holds on every build: every yard cell
     * outside the house is fence, bush, or the untouched sentinel — never
     * air; every house-volume cell is overwritten (no sentinel survives).
     */
    @GameTest(template = "empty_large", timeoutTicks = 400)
    public void i160_stinky_house_content_and_stats(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(12, 2, 10));
        // Not in the builder's palette (oak fence/planks, dead bush, glass
        // pane, spawner, chest, air) — survival marks "never written".
        Block sentinel = Blocks.POLISHED_ANDESITE;

        int builds = 20;
        int fenceHits = 0;
        int interiorBushHits = 0;
        int roofHits = 0;
        int perimeterBushes = 0;
        // Window cells (house-relative x,z), y+2 (GD:5350-5352).
        int[][] windows = {{1, 0}, {1, 9}, {11, 0}, {11, 9}, {0, 1}, {12, 1}, {0, 8}, {12, 8}};
        boolean[] windowSeen = new boolean[windows.length];

        for (int b = 0; b < builds; b++) {
            // Reset the canvas: sentinel across the whole yard layer (rel X
            // -5..+19, Z -4..+12 at y+1, GD:5327-5342) and the house volume
            // (X 0..12, Z 0..9, Y +1..+3, GD:5343-5365).
            for (int i = 0; i <= 24; i++) {
                for (int k = 0; k <= 16; k++) {
                    level.setBlock(O.offset(i - 5, 1, k - 4), sentinel.defaultBlockState(), FLAG);
                }
            }
            for (int x = 0; x <= 12; x++) {
                for (int z = 0; z <= 9; z++) {
                    for (int y = 1; y <= 3; y++) {
                        level.setBlock(O.offset(x, y, z), sentinel.defaultBlockState(), FLAG);
                    }
                }
            }

            LegacyDungeonPiece.buildNow(level, O, DungeonType.STINKY_HOUSE);

            // Yard sweep (outside the house footprint).
            for (int i = 0; i <= 24; i++) {
                for (int k = 0; k <= 16; k++) {
                    int x = i - 5;
                    int z = k - 4;
                    if (x >= 0 && x <= 12 && z >= 0 && z <= 9) continue; // overwritten by loop B
                    Block blk = blockAt(helper, O.offset(x, 1, z));
                    boolean perimeter = i == 0 || i == 24 || k == 0 || k == 16;
                    if (perimeter) {
                        helper.assertTrue(blk == Blocks.OAK_FENCE || blk == Blocks.DEAD_BUSH || blk == sentinel,
                                "yard perimeter cell must be fence, bush, or untouched (GD:5330-5340), found "
                                        + BuiltInRegistries.BLOCK.getKey(blk) + " at rel " + x + "," + z);
                        if (blk == Blocks.OAK_FENCE) fenceHits++;
                        if (blk == Blocks.DEAD_BUSH) perimeterBushes++;
                    } else {
                        helper.assertTrue(blk == Blocks.DEAD_BUSH || blk == sentinel,
                                "interior yard cell must be bush or untouched — the yard never writes air "
                                        + "(GD:5336-5339, spec S3), found "
                                        + BuiltInRegistries.BLOCK.getKey(blk) + " at rel " + x + "," + z);
                        if (blk == Blocks.DEAD_BUSH) interiorBushHits++;
                    }
                }
            }
            // House volume: loop B writes every cell — no sentinel survives
            // (unconditional write incl. air, GD:5362, spec S3 contrast).
            for (int x = 0; x <= 12; x++) {
                for (int z = 0; z <= 9; z++) {
                    for (int y = 1; y <= 3; y++) {
                        helper.assertTrue(blockAt(helper, O.offset(x, y, z)) != sentinel,
                                "house shell must overwrite every cell (GD:5362) — sentinel survived at rel "
                                        + x + "," + y + "," + z);
                    }
                }
            }
            // Roof at y+3 (j=2): planks unless the 1/10 decay (GD:5353-5358).
            for (int x = 0; x <= 12; x++) {
                for (int z = 0; z <= 9; z++) {
                    Block blk = blockAt(helper, O.offset(x, 3, z));
                    helper.assertTrue(blk == Blocks.OAK_PLANKS || blk == Blocks.AIR,
                            "roof cell must be planks or decayed air, found " + BuiltInRegistries.BLOCK.getKey(blk));
                    if (blk == Blocks.OAK_PLANKS) roofHits++;
                }
            }
            // Windows at y+2: pane unless decayed (GD:5350-5358, spec S8).
            for (int w = 0; w < windows.length; w++) {
                Block blk = blockAt(helper, O.offset(windows[w][0], 2, windows[w][1]));
                helper.assertTrue(blk == Blocks.GLASS_PANE || blk == Blocks.AIR,
                        "window cell must be pane or decayed air, found " + BuiltInRegistries.BLOCK.getKey(blk));
                if (blk == Blocks.GLASS_PANE) windowSeen[w] = true;
            }
            // Doorway force-cleared AFTER the decay roll — always open
            // (GD:5359-5361, spec S2).
            for (int y = 1; y <= 2; y++) {
                assertBlockIs(helper, O.offset(0, y, 4), Blocks.AIR, "doorway must be force-cleared");
                assertBlockIs(helper, O.offset(0, y, 5), Blocks.AIR, "doorway must be force-cleared");
            }
        }

        // Statistics (math in the test Javadoc).
        assertFraction(helper, fenceHits, builds * 80, 2.0 / 3.0, 0.09, "yard fence survival (1/3 knockout)");
        assertFraction(helper, interiorBushHits, builds * 215, 0.10, 0.06, "interior dead-bush rate (1/10)");
        assertFraction(helper, roofHits, builds * 130, 0.90, 0.07, "roof decay survival (1/10 decay)");
        helper.assertTrue(perimeterBushes >= 1,
                "expected at least one dead bush standing in the fence line over 20 builds (spec S6)");
        for (int w = 0; w < windows.length; w++) {
            helper.assertTrue(windowSeen[w],
                    "corner-adjacent window at rel " + windows[w][0] + ",+2," + windows[w][1]
                            + " never appeared as a pane in 20 builds (GD:5350-5352)");
        }

        // Deterministic content on the final build.
        assertSpawner(helper, O.offset(2, 1, 2), ModEntities.ENTITY_STINK_BUG.get(),
                "Stink Bug spawner (GD:5366-5370)");
        assertSpawner(helper, O.offset(10, 1, 7), ModEntities.ENTITY_STINKY.get(),
                "Stinky spawner (GD:5371-5375)");
        assertLootChest(helper, O.offset(6, 1, 4), "chests/stinky_house", "house-centre chest");
        // Rolls uniform 8-12 over the 7-entry weight-215 list incl. BOTH
        // stink eggs (loot_table/chests/stinky_house.json).
        Set<String> stinkyItems = Set.of("orespawn:dead_stink_bug", "orespawn:stinky_spawn_egg",
                "orespawn:stink_bug_spawn_egg", "minecraft:bone", "minecraft:coal",
                "minecraft:string", "minecraft:rotten_flesh");
        checkLootRolls(helper, "chests/stinky_house", O, 8, 12, stinkyItems, stinkyItems, "stinky house loot");
        helper.succeed();
    }

    // ---------------------------------------------------------------
    // i161 — Pumpkin (far build, K = 711: 18 blocks tall)
    // ---------------------------------------------------------------

    /**
     * Checklist item i161-pumpkin-islands (Phase D6b batch 3).
     * PumpkinGenerator per phase_d_reports/d6_extraction/pumpkin_spec.md
     * (orig GenericDungeon.java:6041-6182): hollow 14x14x12
     * orange-terracotta box (GD:6053-6073), EXACTLY 48 carved-face air
     * cells on the -z wall — eyes, split nose, notched mouth band, fangs
     * (GD:6074-6143, spec S6: the face is carved ONLY on the z=0 wall) —
     * the 3-wide green-terracotta stem leaning one block -x per layer on
     * the lid (GD:6144-6149), the 2x2x5 oak-plank candle with netherrack
     * cap (GD:6150-6162), the twin eternal fires (GD:6163-6167, spec S7),
     * two "Ghost Pumpkin Skelly" spawners (GD:6168-6181), and NO chest
     * (spec section 3/S3). "One block above grass" is the
     * ISLANDS_GRASS_AIR anchor — grass + 1 (orig OreSpawnWorld.java:2416;
     * port DungeonType.PUMPKIN) — asserted as placement-mode data.
     * 18 blocks tall, so it builds at the far offset K=711.
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public void i161_pumpkin_content(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos O = helper.absolutePos(new BlockPos(0, 0, 0)).offset(711 * 512, 10, 50000);
        LegacyDungeonPiece.buildNow(level, O, DungeonType.PUMPKIN);

        // The 48 carved-face cells on the front (z = 0) wall (GD:6074-6143),
        // enumerated with the original's right-half base i=6 / left-half
        // base i=7 offsets.
        Set<Long> face = new HashSet<>();   // pack(x, y)
        for (int y = 9; y <= 11; y++) {     // eyes, 3x3 each (GD:6076-6087 / :6111-6122)
            for (int x = 9; x <= 11; x++) face.add(pack(x, y));
            for (int x = 2; x <= 4; x++) face.add(pack(x, y));
        }
        for (int y = 7; y <= 8; y++) {      // split nose, 2x2 halves (GD:6088-6093 / :6123-6128)
            face.add(pack(8, y));
            face.add(pack(9, y));
            face.add(pack(4, y));
            face.add(pack(5, y));
        }
        face.add(pack(7, 4));               // mouth top notches (GD:6094-6096 / :6129-6131)
        face.add(pack(10, 4));
        face.add(pack(6, 4));
        face.add(pack(3, 4));
        for (int y = 2; y <= 3; y++) {      // mouth band (GD:6097-6106 / :6132-6141)
            for (int x = 7; x <= 10; x++) face.add(pack(x, y));
            for (int x = 3; x <= 6; x++) face.add(pack(x, y));
        }
        face.add(pack(8, 1));               // bottom fangs (GD:6107-6108 / :6142-6143)
        face.add(pack(5, 1));
        helper.assertTrue(face.size() == 48, "face enumeration must be 48 cells, got " + face.size());
        for (int x = 0; x < 14; x++) {
            for (int y = 0; y < 14; y++) {
                BlockPos pos = O.offset(x, y, 0);
                if (face.contains(pack(x, y))) {
                    assertBlockIs(helper, pos, Blocks.AIR, "carved face cell (GD:6074-6143)");
                } else {
                    assertBlockIs(helper, pos, Blocks.ORANGE_TERRACOTTA, "front wall (GD:6066-6069)");
                }
            }
        }
        // Shell samples: floor, lid, side and back walls (GD:6053-6073);
        // interior explicitly written air (spec S5).
        assertBlockIs(helper, O.offset(5, 0, 5), Blocks.ORANGE_TERRACOTTA, "floor");
        assertBlockIs(helper, O.offset(5, 13, 8), Blocks.ORANGE_TERRACOTTA, "lid");
        assertBlockIs(helper, O.offset(0, 6, 5), Blocks.ORANGE_TERRACOTTA, "west wall");
        assertBlockIs(helper, O.offset(13, 6, 5), Blocks.ORANGE_TERRACOTTA, "east wall");
        assertBlockIs(helper, O.offset(5, 6, 11), Blocks.ORANGE_TERRACOTTA, "back wall — no carving (spec S6)");
        assertBlockIs(helper, O.offset(3, 5, 3), Blocks.AIR, "carved interior");
        assertBlockIs(helper, O.offset(10, 9, 8), Blocks.AIR, "carved interior");
        // Stem: 3 wide, 4 tall, leaning one block -x per layer at z=+5
        // (GD:6144-6149): x = 7 - i - j, y = 14 + j.
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 3; i++) {
                assertBlockIs(helper, O.offset(7 - i - j, 14 + j, 5), Blocks.GREEN_TERRACOTTA,
                        "stem course j=" + j + " (lean -x per layer)");
            }
        }
        // Candle: 2x2x5 oak planks (GD:6150-6156) + netherrack cap (GD:6157-6162).
        for (int j = 1; j <= 5; j++) {
            for (int i = 6; i <= 7; i++) {
                for (int k = 5; k <= 6; k++) {
                    assertBlockIs(helper, O.offset(i, j, k), Blocks.OAK_PLANKS, "candle pillar");
                }
            }
        }
        for (int i = 6; i <= 7; i++) {
            for (int k = 5; k <= 6; k++) {
                assertBlockIs(helper, O.offset(i, 6, k), Blocks.NETHERRACK, "netherrack cap");
            }
        }
        // Twin fires on the front netherrack pair (GD:6163-6167).
        assertBlockIs(helper, O.offset(6, 7, 5), Blocks.FIRE, "candle flame");
        assertBlockIs(helper, O.offset(7, 7, 5), Blocks.FIRE, "candle flame");
        // Two Ghost Pumpkin Skelly spawners on the back pair (GD:6168-6181).
        assertSpawner(helper, O.offset(6, 7, 6), ModEntities.GHOST_SKELLY.get(), "candle spawner");
        assertSpawner(helper, O.offset(7, 7, 6), ModEntities.GHOST_SKELLY.get(), "candle spawner");
        // NO chest anywhere in the enum box (spec section 3/S3).
        assertNoneInBox(helper, O.offset(-1, -1, -1), O.offset(14, 18, 12), Blocks.CHEST,
                "the pumpkin has no chest");
        // "One block above grass": the ISLANDS_GRASS_AIR anchor (grass + 1,
        // orig OreSpawnWorld.java:2416; LegacyDungeonStructure grass+1 mode).
        helper.assertTrue(DungeonType.PUMPKIN.placement == DungeonType.PlacementMode.ISLANDS_GRASS_AIR,
                "PUMPKIN must anchor with ISLANDS_GRASS_AIR (OSW:2407-2421, spec S2)");
        helper.succeed();
    }
}
