package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.feature.SpawnOresPoolFeature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Recipe-parity GameTests (testing_session batch "recipes", K base 200 — this
 * class performs no far-offset world builds, so no K regions are consumed;
 * every assertion is RecipeManager / registry data resolved inside the
 * "empty" template).
 *
 * <p>Checklist items covered:</p>
 * <ul>
 *   <li><b>i012-item-059</b> — ITEM-059: uranium/titanium ore smelts to a
 *       NUGGET (not an ingot); 9 nuggets craft the ingot.</li>
 *   <li><b>i016-item-060-061-062-d4</b> — ITEM-060/061/062 (D4 slice): skate
 *       bow, chest + red bed from crystal planks, raw corn dog, bucket from
 *       3 pink tourmaline ingots, cobweb from string, plus the oak door from
 *       crystal planks staying intact.</li>
 *   <li><b>i133-item-062</b> — ITEM-062 (D5 closure): ALL 116 water-bucket
 *       spawn-block → spawn-egg conversions (each output + returned empty
 *       bucket), the 3 nine-part combines, full block + water → boss egg,
 *       and the worldgen pool asymmetry (Mobzilla part+full pool-placed;
 *       King/Queen full blocks craft-only).</li>
 * </ul>
 *
 * <p>Documented sources (citation convention: OSM:NN =
 * reference_1_7_10_source/sources/danger/orespawn/OreSpawnMain.java):
 * FIX_LOG.md "ITEM-059" (:123) + Phase C6 recipe pass (:329,
 * phase_c_reports/C6_recipe_diff.md); FIX_LOG.md "ITEM-060/061 — FIXED" /
 * "ITEM-062 — PARTIAL" (:904-911, D4) and "ITEM-062 — FIXED" (:823-827, D5);
 * phase_d_reports/D4_items_small_entities.md §5;
 * phase_d_reports/D5_structures_spawnores.md §5-§6;
 * phase_d_reports/d5_extraction/spawn_ores_spec.md §2 (119-row master table),
 * §3 (pool weights), §6 (recipe shape).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RecipeTests {

    private static final String NS = "orespawn";

    private static String[] row(String blockName, String eggId) {
        return new String[]{blockName, eggId};
    }

    /**
     * ITEM-062 master conversion table: port spawn-block registry name →
     * expected spawn-egg item id, one row per original
     * {@code shapeless{ water bucket + spawn block } → 1 egg} registration
     * (OSM:2665-3021). Transcribed from
     * phase_d_reports/d5_extraction/spawn_ores_spec.md §2 via
     * tools/d5_gen_spawn_ores.py ROWS (the water=True rows; the three
     * water=False rows are the Mobzilla/King/Queen PART blocks, which have no
     * water recipe — they are the nine-part combine INPUTS, see
     * {@link #item062_nine_part_combines_and_pool_asymmetry}). Mapping
     * decisions per FIX_LOG.md:823-827: vanilla mobs → modern vanilla eggs
     * (ender_dragon/iron_golem/snow_golem/wither all exist since 1.20.5);
     * CriminalEgg → band_p_spawn_egg (WGEN-017); EnchantedCowEgg →
     * enchanted_apple_cow_spawn_egg (Phase-14 consolidation target).
     */
    private static final List<String[]> WATER_CONVERSIONS = List.of(
            row("spider_spawn_block", "minecraft:spider_spawn_egg"), // orig MySpiderSpawnBlock, recipe OSM:2667
            row("bat_spawn_block", "minecraft:bat_spawn_egg"), // orig MyBatSpawnBlock, recipe OSM:2670
            row("cow_spawn_block", "minecraft:cow_spawn_egg"), // orig MyCowSpawnBlock, recipe OSM:2673
            row("pig_spawn_block", "minecraft:pig_spawn_egg"), // orig MyPigSpawnBlock, recipe OSM:2676
            row("squid_spawn_block", "minecraft:squid_spawn_egg"), // orig MySquidSpawnBlock, recipe OSM:2679
            row("chicken_spawn_block", "minecraft:chicken_spawn_egg"), // orig MyChickenSpawnBlock, recipe OSM:2682
            row("creeper_spawn_block", "minecraft:creeper_spawn_egg"), // orig MyCreeperSpawnBlock, recipe OSM:2685
            row("skeleton_spawn_block", "minecraft:skeleton_spawn_egg"), // orig MySkeletonSpawnBlock, recipe OSM:2688
            row("zombie_spawn_block", "minecraft:zombie_spawn_egg"), // orig MyZombieSpawnBlock, recipe OSM:2691
            row("slime_spawn_block", "minecraft:slime_spawn_egg"), // orig MySlimeSpawnBlock, recipe OSM:2694
            row("ghast_spawn_block", "minecraft:ghast_spawn_egg"), // orig MyGhastSpawnBlock, recipe OSM:2697
            row("zombified_piglin_spawn_block", "minecraft:zombified_piglin_spawn_egg"), // orig MyZombiePigmanSpawnBlock, recipe OSM:2700
            row("enderman_spawn_block", "minecraft:enderman_spawn_egg"), // orig MyEndermanSpawnBlock, recipe OSM:2703
            row("cave_spider_spawn_block", "minecraft:cave_spider_spawn_egg"), // orig MyCaveSpiderSpawnBlock, recipe OSM:2706
            row("silverfish_spawn_block", "minecraft:silverfish_spawn_egg"), // orig MySilverfishSpawnBlock, recipe OSM:2709
            row("magma_cube_spawn_block", "minecraft:magma_cube_spawn_egg"), // orig MyMagmaCubeSpawnBlock, recipe OSM:2712
            row("witch_spawn_block", "minecraft:witch_spawn_egg"), // orig MyWitchSpawnBlock, recipe OSM:2715
            row("sheep_spawn_block", "minecraft:sheep_spawn_egg"), // orig MySheepSpawnBlock, recipe OSM:2718
            row("wolf_spawn_block", "minecraft:wolf_spawn_egg"), // orig MyWolfSpawnBlock, recipe OSM:2721
            row("mooshroom_spawn_block", "minecraft:mooshroom_spawn_egg"), // orig MyMooshroomSpawnBlock, recipe OSM:2724
            row("ocelot_spawn_block", "minecraft:ocelot_spawn_egg"), // orig MyOcelotSpawnBlock, recipe OSM:2727
            row("blaze_spawn_block", "minecraft:blaze_spawn_egg"), // orig MyBlazeSpawnBlock, recipe OSM:2730
            row("wither_skeleton_spawn_block", "minecraft:wither_skeleton_spawn_egg"), // orig MyWitherSkeletonSpawnBlock, recipe OSM:2733
            row("ender_dragon_spawn_block", "minecraft:ender_dragon_spawn_egg"), // orig MyEnderDragonSpawnBlock, recipe OSM:2736
            row("snow_golem_spawn_block", "minecraft:snow_golem_spawn_egg"), // orig MySnowGolemSpawnBlock, recipe OSM:2739
            row("iron_golem_spawn_block", "minecraft:iron_golem_spawn_egg"), // orig MyIronGolemSpawnBlock, recipe OSM:2742
            row("wither_spawn_block", "minecraft:wither_spawn_egg"), // orig MyWitherBossSpawnBlock, recipe OSM:2745
            row("girlfriend_spawn_block", "orespawn:girlfriend_spawn_egg"), // orig MyGirlfriendSpawnBlock, recipe OSM:2748
            row("boyfriend_spawn_block", "orespawn:boyfriend_spawn_egg"), // orig MyBoyfriendSpawnBlock, recipe OSM:2751
            row("red_cow_spawn_block", "orespawn:red_cow_spawn_egg"), // orig MyRedCowSpawnBlock, recipe OSM:2754
            row("crystal_cow_spawn_block", "orespawn:crystal_cow_spawn_egg"), // orig MyCrystalCowSpawnBlock, recipe OSM:2757
            row("villager_spawn_block", "minecraft:villager_spawn_egg"), // orig MyVillagerSpawnBlock, recipe OSM:2760
            row("gold_cow_spawn_block", "orespawn:gold_cow_spawn_egg"), // orig MyGoldCowSpawnBlock, recipe OSM:2763
            row("enchanted_apple_cow_spawn_block", "orespawn:enchanted_apple_cow_spawn_egg"), // orig MyEnchantedCowSpawnBlock, recipe OSM:2766
            row("mothra_spawn_block", "orespawn:mothra_spawn_egg"), // orig MyMOTHRASpawnBlock, recipe OSM:2769
            row("alosaurus_spawn_block", "orespawn:alosaurus_spawn_egg"), // orig MyAloSpawnBlock, recipe OSM:2772
            row("cryolophosaurus_spawn_block", "orespawn:cryolophosaurus_spawn_egg"), // orig MyCryoSpawnBlock, recipe OSM:2775
            row("camarasaurus_spawn_block", "orespawn:camarasaurus_spawn_egg"), // orig MyCamaSpawnBlock, recipe OSM:2778
            row("velocity_raptor_spawn_block", "orespawn:velocity_raptor_spawn_egg"), // orig MyVeloSpawnBlock, recipe OSM:2781
            row("hydrolisc_spawn_block", "orespawn:hydrolisc_spawn_egg"), // orig MyHydroSpawnBlock, recipe OSM:2784
            row("basilisk_spawn_block", "orespawn:basilisk_spawn_egg"), // orig MyBasilSpawnBlock, recipe OSM:2787
            row("dragonfly_spawn_block", "orespawn:dragonfly_spawn_egg"), // orig MyDragonflySpawnBlock, recipe OSM:2790
            row("emperor_scorpion_spawn_block", "orespawn:emperor_scorpion_spawn_egg"), // orig MyEmperorScorpionSpawnBlock, recipe OSM:2793
            row("scorpion_spawn_block", "orespawn:scorpion_spawn_egg"), // orig MyScorpionSpawnBlock, recipe OSM:2796
            row("cave_fisher_spawn_block", "orespawn:cave_fisher_spawn_egg"), // orig MyCaveFisherSpawnBlock, recipe OSM:2799
            row("spyro_spawn_block", "orespawn:spyro_spawn_egg"), // orig MySpyroSpawnBlock, recipe OSM:2802
            row("baryonyx_spawn_block", "orespawn:baryonyx_spawn_egg"), // orig MyBaryonyxSpawnBlock, recipe OSM:2805
            row("gamma_metroid_spawn_block", "orespawn:gamma_metroid_spawn_egg"), // orig MyGammaMetroidSpawnBlock, recipe OSM:2808
            row("cockateil_spawn_block", "orespawn:cockateil_spawn_egg"), // orig MyCockateilSpawnBlock, recipe OSM:2811
            row("kyuubi_spawn_block", "orespawn:kyuubi_spawn_egg"), // orig MyKyuubiSpawnBlock, recipe OSM:2814
            row("alien_spawn_block", "orespawn:alien_spawn_egg"), // orig MyAlienSpawnBlock, recipe OSM:2817
            row("attack_squid_spawn_block", "orespawn:attack_squid_spawn_egg"), // orig MyAttackSquidSpawnBlock, recipe OSM:2820
            row("water_dragon_spawn_block", "orespawn:water_dragon_spawn_egg"), // orig MyWaterDragonSpawnBlock, recipe OSM:2823
            row("kraken_spawn_block", "orespawn:kraken_spawn_egg"), // orig MyKrakenSpawnBlock, recipe OSM:2826
            row("lizard_spawn_block", "orespawn:lizard_spawn_egg"), // orig MyLizardSpawnBlock, recipe OSM:2829
            row("cephadrome_spawn_block", "orespawn:cephadrome_spawn_egg"), // orig MyCephadromeSpawnBlock, recipe OSM:2832
            row("dragon_spawn_block", "orespawn:dragon_spawn_egg"), // orig MyDragonSpawnBlock, recipe OSM:2835
            row("bee_spawn_block", "orespawn:bee_spawn_egg"), // orig MyBeeSpawnBlock, recipe OSM:2838
            row("horse_spawn_block", "minecraft:horse_spawn_egg"), // orig MyHorseSpawnBlock, recipe OSM:2841
            row("trooper_bug_spawn_block", "orespawn:trooper_bug_spawn_egg"), // orig MyTrooperBugSpawnBlock, recipe OSM:2844
            row("spit_bug_spawn_block", "orespawn:spit_bug_spawn_egg"), // orig MySpitBugSpawnBlock, recipe OSM:2847
            row("stink_bug_spawn_block", "orespawn:stink_bug_spawn_egg"), // orig MyStinkBugSpawnBlock, recipe OSM:2850
            row("ostrich_spawn_block", "orespawn:ostrich_spawn_egg"), // orig MyOstrichSpawnBlock, recipe OSM:2853
            row("gazelle_spawn_block", "orespawn:gazelle_spawn_egg"), // orig MyGazelleSpawnBlock, recipe OSM:2856
            row("chipmunk_spawn_block", "orespawn:chipmunk_spawn_egg"), // orig MyChipmunkSpawnBlock, recipe OSM:2859
            row("creeping_horror_spawn_block", "orespawn:creeping_horror_spawn_egg"), // orig MyCreepingHorrorSpawnBlock, recipe OSM:2862
            row("terrible_terror_spawn_block", "orespawn:terrible_terror_spawn_egg"), // orig MyTerribleTerrorSpawnBlock, recipe OSM:2865
            row("cliff_racer_spawn_block", "orespawn:cliff_racer_spawn_egg"), // orig MyCliffRacerSpawnBlock, recipe OSM:2868
            row("triffid_spawn_block", "orespawn:triffid_spawn_egg"), // orig MyTriffidSpawnBlock, recipe OSM:2871
            row("pitch_black_spawn_block", "orespawn:pitch_black_spawn_egg"), // orig MyPitchBlackSpawnBlock, recipe OSM:2874
            row("lurking_terror_spawn_block", "orespawn:lurking_terror_spawn_egg"), // orig MyLurkingTerrorSpawnBlock, recipe OSM:2877
            row("godzilla_spawn_block", "orespawn:godzilla_spawn_egg"), // orig MyGodzillaSpawnBlock, recipe OSM:2889
            row("the_king_spawn_block", "orespawn:the_king_spawn_egg"), // orig MyTheKingSpawnBlock, recipe OSM:2895
            row("the_queen_spawn_block", "orespawn:the_queen_spawn_egg"), // orig MyTheQueenSpawnBlock, recipe OSM:2901
            row("worm_small_spawn_block", "orespawn:worm_small_spawn_egg"), // orig MySmallWormSpawnBlock, recipe OSM:2904
            row("worm_medium_spawn_block", "orespawn:worm_medium_spawn_egg"), // orig MyMediumWormSpawnBlock, recipe OSM:2907
            row("worm_large_spawn_block", "orespawn:worm_large_spawn_egg"), // orig MyLargeWormSpawnBlock, recipe OSM:2910
            row("cassowary_spawn_block", "orespawn:cassowary_spawn_egg"), // orig MyCassowarySpawnBlock, recipe OSM:2913
            row("cloud_shark_spawn_block", "orespawn:cloud_shark_spawn_egg"), // orig MyCloudSharkSpawnBlock, recipe OSM:2916
            row("gold_fish_spawn_block", "orespawn:gold_fish_spawn_egg"), // orig MyGoldFishSpawnBlock, recipe OSM:2919
            row("leaf_monster_spawn_block", "orespawn:leaf_monster_spawn_egg"), // orig MyLeafMonsterSpawnBlock, recipe OSM:2922
            row("tshirt_spawn_block", "orespawn:tshirt_spawn_egg"), // orig MyTshirtSpawnBlock, recipe OSM:2925
            row("ender_knight_spawn_block", "orespawn:ender_knight_spawn_egg"), // orig MyEnderKnightSpawnBlock, recipe OSM:2880
            row("ender_reaper_spawn_block", "orespawn:ender_reaper_spawn_egg"), // orig MyEnderReaperSpawnBlock, recipe OSM:2883
            row("beaver_spawn_block", "orespawn:beaver_spawn_egg"), // orig MyBeaverSpawnBlock, recipe OSM:2928
            row("ore_urchin", "orespawn:urchin_spawn_egg"), // orig MyUrchinSpawnBlock, recipe OSM:2931
            row("ore_flounder", "orespawn:flounder_spawn_egg"), // orig MyFlounderSpawnBlock, recipe OSM:2934
            row("ore_skate", "orespawn:skate_spawn_egg"), // orig MySkateSpawnBlock, recipe OSM:2937
            row("ore_rotator", "orespawn:rotator_spawn_egg"), // orig MyRotatorSpawnBlock, recipe OSM:2940
            row("ore_peacock", "orespawn:peacock_spawn_egg"), // orig MyPeacockSpawnBlock, recipe OSM:2943
            row("ore_fairy", "orespawn:fairy_spawn_egg"), // orig MyFairySpawnBlock, recipe OSM:2946
            row("ore_dungeon_beast", "orespawn:dungeon_beast_spawn_egg"), // orig MyDungeonBeastSpawnBlock, recipe OSM:2949
            row("ore_vortex", "orespawn:vortex_spawn_egg"), // orig MyVortexSpawnBlock, recipe OSM:2952
            row("ore_rat", "orespawn:rat_spawn_egg"), // orig MyRatSpawnBlock, recipe OSM:2955
            row("ore_whale", "orespawn:whale_spawn_egg"), // orig MyWhaleSpawnBlock, recipe OSM:2958
            row("ore_irukandji", "orespawn:irukandji_spawn_egg"), // orig MyIrukandjiSpawnBlock, recipe OSM:2961
            row("trex_spawn_block", "orespawn:trex_spawn_egg"), // orig MyTRexSpawnBlock, recipe OSM:2964
            row("hercules_beetle_spawn_block", "orespawn:hercules_beetle_spawn_egg"), // orig MyHerculesSpawnBlock, recipe OSM:2967
            row("mantis_spawn_block", "orespawn:mantis_spawn_egg"), // orig MyMantisSpawnBlock, recipe OSM:2970
            row("stinky_spawn_block", "orespawn:stinky_spawn_egg"), // orig MyStinkySpawnBlock, recipe OSM:2973
            row("easter_bunny_spawn_block", "orespawn:easter_bunny_spawn_egg"), // orig MyEasterBunnySpawnBlock, recipe OSM:2976
            row("cater_killer_spawn_block", "orespawn:cater_killer_spawn_egg"), // orig MyCaterKillerSpawnBlock, recipe OSM:3003
            row("molenoid_spawn_block", "orespawn:molenoid_spawn_egg"), // orig MyMolenoidSpawnBlock, recipe OSM:3006
            row("sea_monster_spawn_block", "orespawn:sea_monster_spawn_egg"), // orig MySeaMonsterSpawnBlock, recipe OSM:3009
            row("sea_viper_spawn_block", "orespawn:sea_viper_spawn_egg"), // orig MySeaViperSpawnBlock, recipe OSM:3012
            row("leon_spawn_block", "orespawn:leon_spawn_egg"), // orig MyLeonSpawnBlock, recipe OSM:3021
            row("hammerhead_spawn_block", "orespawn:hammerhead_spawn_egg"), // orig MyHammerheadSpawnBlock, recipe OSM:3018
            row("rubber_ducky_spawn_block", "orespawn:rubber_ducky_spawn_egg"), // orig MyRubberDuckySpawnBlock, recipe OSM:3015
            row("band_p_spawn_block", "orespawn:band_p_spawn_egg"), // orig MyCriminalSpawnBlock, recipe OSM:2979
            row("brutalfly_spawn_block", "orespawn:brutalfly_spawn_egg"), // orig MyBrutalflySpawnBlock, recipe OSM:2982
            row("nastysaurus_spawn_block", "orespawn:nastysaurus_spawn_egg"), // orig MyNastysaurusSpawnBlock, recipe OSM:2985
            row("pointysaurus_spawn_block", "orespawn:pointysaurus_spawn_egg"), // orig MyPointysaurusSpawnBlock, recipe OSM:2988
            row("cricket_spawn_block", "orespawn:cricket_spawn_egg"), // orig MyCricketSpawnBlock, recipe OSM:2991
            row("frog_spawn_block", "orespawn:frog_spawn_egg"), // orig MyFrogSpawnBlock, recipe OSM:2994
            row("spider_driver_spawn_block", "orespawn:spider_driver_spawn_egg"), // orig MySpiderDriverSpawnBlock, recipe OSM:2997
            row("crab_spawn_block", "orespawn:crab_spawn_egg" ) // orig MyCrabSpawnBlock, recipe OSM:3000
    );

    // ------------------------------------------------------------------
    // Shared plumbing
    // ------------------------------------------------------------------

    /** Registry lookup that fails the test loudly instead of yielding AIR. */
    private static Item item(String id) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id))
                .orElseThrow(() -> new IllegalStateException("item not registered: " + id));
    }

    private static ItemStack stack(String id) {
        return new ItemStack(item(id));
    }

    private static ItemStack air() {
        return ItemStack.EMPTY;
    }

    private static CraftingInput grid(int width, int height, ItemStack... stacks) {
        return CraftingInput.of(width, height, List.of(stacks));
    }

    /**
     * Resolves {@code input} against the live CRAFTING recipe registry (the
     * same lookup a crafting table performs), then asserts the resolved
     * recipe id and the assembled result item + count.
     */
    private static void assertCrafts(GameTestHelper helper, CraftingInput input,
            String expectedRecipeId, String expectedResultId, int expectedCount) {
        ServerLevel level = helper.getLevel();
        Optional<RecipeHolder<CraftingRecipe>> match =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(match.isPresent(),
                "no crafting recipe matches the grid for " + expectedRecipeId);
        RecipeHolder<CraftingRecipe> holder = match.get();
        helper.assertTrue(holder.id().equals(ResourceLocation.parse(expectedRecipeId)),
                "grid for " + expectedRecipeId + " resolved to " + holder.id());
        ItemStack result = holder.value().assemble(input, level.registryAccess());
        helper.assertTrue(result.is(item(expectedResultId)) && result.getCount() == expectedCount,
                expectedRecipeId + ": expected " + expectedCount + "x " + expectedResultId + ", got "
                        + result.getCount() + "x " + BuiltInRegistries.ITEM.getKey(result.getItem()));
    }

    /** Reads a private static pool list off SpawnOresPoolFeature reflectively. */
    @SuppressWarnings("unchecked")
    private static List<Supplier<Block>> spawnOresPool(String fieldName) {
        try {
            Field field = SpawnOresPoolFeature.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (List<Supplier<Block>>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "cannot read SpawnOresPoolFeature." + fieldName + " (renamed?)", e);
        }
    }

    // ------------------------------------------------------------------
    // i012-item-059 — ITEM-059
    // ------------------------------------------------------------------

    /**
     * Checklist i012-item-059 / finding ITEM-059 — "Smelt orespawn:ore_uranium
     * / ore_titanium. Expect: a NUGGET each (9 nuggets → ingot), not an ingot."
     *
     * <p>Documented values: orig OSM:3092/3094 —
     * {@code addSmelting(MyOreUraniumBlock, new ItemStack(UraniumNugget), 0.3f)}
     * (same shape for titanium) → result is ONE nugget, smelting XP 0.3, at the
     * 1.7.10 fixed furnace cook time of 200 ticks; orig OSM:3293/3295 — shaped
     * "UUU"/"UUU"/"UUU" of nuggets → 1 ingot. Fix records: FIX_LOG.md:123
     * (ITEM-059) and the Phase C6 recipe pass ("uranium/titanium smelting →
     * nuggets XP 0.3", FIX_LOG.md:329; phase_c_reports/C6_recipe_diff.md).</p>
     */
    @GameTest(template = "empty")
    public void item059_ore_smelts_to_nugget_not_ingot(GameTestHelper helper) {
        checkNuggetChain(helper, NS + ":ore_uranium", NS + ":uranium_nugget",
                NS + ":ingot_uranium", NS + ":uranium_ingot_from_nuggets");
        checkNuggetChain(helper, NS + ":ore_titanium", NS + ":titanium_nugget",
                NS + ":ingot_titanium", NS + ":titanium_ingot_from_nuggets");
        helper.succeed();
    }

    private static void checkNuggetChain(GameTestHelper helper, String oreId,
            String nuggetId, String ingotId, String nineToIngotRecipeId) {
        ServerLevel level = helper.getLevel();
        SingleRecipeInput oreInput = new SingleRecipeInput(stack(oreId));
        Optional<RecipeHolder<SmeltingRecipe>> smelt =
                level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, oreInput, level);
        helper.assertTrue(smelt.isPresent(), oreId + ": no smelting recipe matches");
        SmeltingRecipe recipe = smelt.get().value();
        ItemStack smelted = recipe.assemble(oreInput, level.registryAccess());
        helper.assertTrue(smelted.is(item(nuggetId)) && smelted.getCount() == 1,
                oreId + ": expected 1x " + nuggetId + ", got " + smelted.getCount() + "x "
                        + BuiltInRegistries.ITEM.getKey(smelted.getItem()));
        // The regression ITEM-059 guards against: an INGOT coming out of the furnace.
        helper.assertFalse(smelted.is(item(ingotId)),
                oreId + " must smelt to the nugget, not " + ingotId);
        helper.assertTrue(Math.abs(recipe.getExperience() - 0.3f) < 1.0e-4f,
                oreId + ": smelting XP " + recipe.getExperience() + " != 0.3 (OSM:3092/3094)");
        helper.assertTrue(recipe.getCookingTime() == 200,
                oreId + ": cook time " + recipe.getCookingTime() + " != 200 (1.7.10 fixed furnace time)");

        // 9 nuggets -> 1 ingot (OSM:3293/3295).
        assertCrafts(helper, grid(3, 3,
                        stack(nuggetId), stack(nuggetId), stack(nuggetId),
                        stack(nuggetId), stack(nuggetId), stack(nuggetId),
                        stack(nuggetId), stack(nuggetId), stack(nuggetId)),
                nineToIngotRecipeId, ingotId, 1);
    }

    // ------------------------------------------------------------------
    // i016-item-060-061-062-d4 — ITEM-060/061/062 (D4 crafting set)
    // ------------------------------------------------------------------

    /**
     * Checklist i016-item-060-061-062-d4 / findings ITEM-060, ITEM-061,
     * ITEM-062 (D4 slice) — "Craft: skate bow (crystal sticks+string), chest +
     * red bed from crystal planks, raw corn dog, bucket from 3 pink tourmaline
     * ingots, cobweb from string. Expect: all five craft; oak door from
     * crystal planks also still works."
     *
     * <p>Documented values (fix records FIX_LOG.md:904-911 and
     * phase_d_reports/D4_items_small_entities.md §5):</p>
     * <ul>
     *   <li>ITEM-060 skate bow — orig OSM:3160, shaped " TS"/"T S"/" TS",
     *       T = crystal sticks, S = string.</li>
     *   <li>ITEM-061 chest — orig OSM:3083 (duplicate :3209), shaped
     *       "FFF"/"F F"/"FFF" of crystal planks → vanilla chest.</li>
     *   <li>ITEM-062 red bed — orig OSM:3291, shapeless 3x peacock feather +
     *       3x crystal planks → 1.7.10 bed (red bed post-flattening).</li>
     *   <li>ITEM-062 raw corn dog — orig OSM:3325, shapeless corn cob + raw
     *       chicken + raw porkchop + stick → FOUR raw corn dogs.</li>
     *   <li>ITEM-062 bucket — orig OSM:3224, shaped "I I"/" I " of pink
     *       tourmaline (crystal pink) ingots → vanilla bucket.</li>
     *   <li>ITEM-062 cobweb — orig OSM:5115, shaped "SSS"/"S S"/"SSS" of
     *       string → cobweb.</li>
     *   <li>ITEM-061 audit correction — orig OSM:3084-3085 crafts the 1.7.10
     *       WOODEN DOOR (field_151135_aq) from a 2x3 of crystal planks, not a
     *       piston; the port's oak_door_from_crystal_planks.json was faithful
     *       all along and must keep working (FIX_LOG.md:905-907).</li>
     * </ul>
     */
    @GameTest(template = "empty")
    public void item060_061_062_d4_crafting_set(GameTestHelper helper) {
        String sticks = NS + ":crystal_sticks";
        String planks = NS + ":crystal_planks";
        String feather = NS + ":peacock_feather";
        String string = "minecraft:string";

        // ITEM-060 — skate bow, " TS"/"T S"/" TS" (OSM:3160).
        assertCrafts(helper, grid(3, 3,
                        air(), stack(sticks), stack(string),
                        stack(sticks), air(), stack(string),
                        air(), stack(sticks), stack(string)),
                NS + ":skate_bow", NS + ":skate_bow", 1);

        // ITEM-061 — chest from a crystal-planks ring (OSM:3083/:3209).
        assertCrafts(helper, grid(3, 3,
                        stack(planks), stack(planks), stack(planks),
                        stack(planks), air(), stack(planks),
                        stack(planks), stack(planks), stack(planks)),
                NS + ":chest_from_crystal_planks", "minecraft:chest", 1);

        // ITEM-062 — red bed, shapeless 3 feathers + 3 planks (OSM:3291).
        assertCrafts(helper, grid(3, 2,
                        stack(feather), stack(planks), stack(feather),
                        stack(planks), stack(feather), stack(planks)),
                NS + ":red_bed_from_crystal_planks", "minecraft:red_bed", 1);

        // ITEM-062 — raw corn dog x4, shapeless cob+chicken+porkchop+stick (OSM:3325).
        assertCrafts(helper, grid(2, 2,
                        stack(NS + ":corn_cob"), stack("minecraft:chicken"),
                        stack("minecraft:porkchop"), stack("minecraft:stick")),
                NS + ":raw_corn_dog", NS + ":raw_corn_dog", 4);

        // ITEM-062 — bucket from 3 pink tourmaline ingots, "I I"/" I " (OSM:3224).
        assertCrafts(helper, grid(3, 2,
                        stack(NS + ":crystal_pink_ingot"), air(), stack(NS + ":crystal_pink_ingot"),
                        air(), stack(NS + ":crystal_pink_ingot"), air()),
                NS + ":bucket_from_crystal_pink_ingot", "minecraft:bucket", 1);

        // ITEM-062 — cobweb from a string ring (OSM:5115).
        assertCrafts(helper, grid(3, 3,
                        stack(string), stack(string), stack(string),
                        stack(string), air(), stack(string),
                        stack(string), stack(string), stack(string)),
                NS + ":cobweb_from_string", "minecraft:cobweb", 1);

        // ITEM-061 audit correction — oak door from 2x3 crystal planks still
        // works (OSM:3084-3085; FIX_LOG.md:905-907).
        assertCrafts(helper, grid(2, 3,
                        stack(planks), stack(planks),
                        stack(planks), stack(planks),
                        stack(planks), stack(planks)),
                NS + ":oak_door_from_crystal_planks", "minecraft:oak_door", 1);

        helper.succeed();
    }

    // ------------------------------------------------------------------
    // i133-item-062 — ITEM-062 (D5 closure): the 116-conversion loop
    // ------------------------------------------------------------------

    /**
     * Checklist i133-item-062 / finding ITEM-062 (D5 closure, FIX_LOG.md:823-827)
     * — data-driven loop over ALL 116 water-bucket conversions: for every row of
     * the documented master table (spawn_ores_spec.md §2, orig OSM:2665-3021)
     * the grid {@code water bucket + spawn block} must resolve, assemble exactly
     * one of the expected spawn egg, and hand back exactly one EMPTY BUCKET via
     * the crafting remainder (the modern equivalent of the 1.7.10
     * container-item behavior — D5_structures_spawnores.md §6). This is a
     * superset of the item's named samples (spider → vanilla spider egg, wither
     * boss → wither egg, criminal → BandP egg, enchanted cow → Enchanted Apple
     * Cow egg, kraken → Kraken egg, urchin ore → Urchin egg) and of the
     * "full block + water → boss egg" clause (the godzilla/the_king/the_queen
     * full-block rows). A closure check then proves there are EXACTLY these 116
     * water-bucket crafting recipes in the orespawn namespace — no stale
     * duplicates, none missing.
     */
    @GameTest(template = "empty")
    public void item062_all_116_water_bucket_egg_conversions(GameTestHelper helper) {
        helper.assertTrue(WATER_CONVERSIONS.size() == 116,
                "master table must carry 116 rows (FIX_LOG.md:823), has " + WATER_CONVERSIONS.size());

        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getRecipeManager();
        List<String> problems = new ArrayList<>();
        Set<ResourceLocation> resolvedIds = new HashSet<>();

        for (String[] conversion : WATER_CONVERSIONS) {
            String blockId = NS + ":" + conversion[0];
            String eggId = conversion[1];
            Optional<Item> blockItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(blockId));
            Optional<Item> eggItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(eggId));
            if (blockItem.isEmpty() || eggItem.isEmpty()) {
                problems.add(blockId + ": missing registration ("
                        + (blockItem.isEmpty() ? blockId : eggId) + ")");
                continue;
            }

            CraftingInput input = grid(2, 1,
                    new ItemStack(Items.WATER_BUCKET), new ItemStack(blockItem.get()));
            Optional<RecipeHolder<CraftingRecipe>> match =
                    recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
            if (match.isEmpty()) {
                problems.add(blockId + ": water bucket + block matches no recipe");
                continue;
            }
            RecipeHolder<CraftingRecipe> holder = match.get();
            resolvedIds.add(holder.id());

            ItemStack result = holder.value().assemble(input, level.registryAccess());
            if (!(result.is(eggItem.get()) && result.getCount() == 1)) {
                problems.add(blockId + ": expected 1x " + eggId + ", got " + result.getCount()
                        + "x " + BuiltInRegistries.ITEM.getKey(result.getItem()));
            }

            // The returned EMPTY BUCKET (1.7.10 container item -> modern
            // crafting remainder; ResultSlot hands these back on craft).
            List<ItemStack> remaining = holder.value().getRemainingItems(input);
            long emptyBuckets = remaining.stream()
                    .filter(s -> s.is(Items.BUCKET) && s.getCount() == 1).count();
            long unexpected = remaining.stream()
                    .filter(s -> !s.isEmpty() && !s.is(Items.BUCKET)).count();
            if (emptyBuckets != 1 || unexpected != 0) {
                problems.add(blockId + ": remainder must be exactly one empty bucket (got "
                        + emptyBuckets + " buckets, " + unexpected + " other stacks)");
            }
        }

        // Each of the 116 rows must have resolved to its own distinct recipe.
        if (problems.isEmpty() && resolvedIds.size() != WATER_CONVERSIONS.size()) {
            problems.add("rows resolved to only " + resolvedIds.size()
                    + " distinct recipes (expected " + WATER_CONVERSIONS.size() + ")");
        }

        // Closure: the orespawn namespace must contain EXACTLY these
        // water-bucket crafting recipes (the original registered exactly one
        // per block, OSM:2665-3021).
        ItemStack waterProbe = new ItemStack(Items.WATER_BUCKET);
        Set<ResourceLocation> allWaterRecipeIds = new HashSet<>();
        for (RecipeHolder<CraftingRecipe> holder : recipes.getAllRecipesFor(RecipeType.CRAFTING)) {
            if (holder.id().getNamespace().equals(NS)
                    && holder.value().getIngredients().stream().anyMatch(i -> i.test(waterProbe))) {
                allWaterRecipeIds.add(holder.id());
            }
        }
        if (!allWaterRecipeIds.equals(resolvedIds)) {
            Set<ResourceLocation> extra = new HashSet<>(allWaterRecipeIds);
            extra.removeAll(resolvedIds);
            Set<ResourceLocation> missing = new HashSet<>(resolvedIds);
            missing.removeAll(allWaterRecipeIds);
            problems.add("water-recipe closure mismatch — unexpected: " + extra
                    + ", unmatched: " + missing);
        }

        helper.assertTrue(problems.isEmpty(), summarize(problems));
        helper.succeed();
    }

    private static String summarize(List<String> problems) {
        if (problems.isEmpty()) {
            return "ok";
        }
        List<String> shown = problems.subList(0, Math.min(problems.size(), 12));
        String tail = problems.size() > shown.size()
                ? " (+" + (problems.size() - shown.size()) + " more)" : "";
        return problems.size() + " conversion failures: " + String.join("; ", shown) + tail;
    }

    // ------------------------------------------------------------------
    // i133-item-062 — nine-part combines + worldgen pool asymmetry
    // ------------------------------------------------------------------

    /**
     * Checklist i133-item-062 / finding ITEM-062 + WGEN-005 — "9x
     * Mobzilla/King/Queen part blocks → full egg block → + water → boss egg.
     * Mobzilla FULL egg blocks also mine from worldgen; King/Queen full blocks
     * never do (parts only)."
     *
     * <p>Documented values: orig OSM:2886/2892/2898 — shapeless 9x part block
     * → 1 full spawn block, then full block + water → boss egg (rows of the
     * §2 table, also swept by the 116-loop test; re-asserted here because the
     * checklist names them). Pool asymmetry per
     * phase_d_reports/D5_structures_spawnores.md §6 ("The original's asymmetry
     * is preserved: Mobzilla part AND full blocks worldgen (c68/c69);
     * King/Queen full blocks are craft-only (parts c86/c97); CrystalCow's
     * block generates nowhere (no pool slot) yet keeps its recipe") and
     * spawn_ores_spec.md §2/§3. Mechanism assert: SpawnOresPoolFeature's
     * COMMON_POOL/RARE_POOL are the SOLE block sources of the pool feature
     * (selection at SpawnOresPoolFeature.java:236-238 draws exclusively from
     * the two lists), so pool membership fully determines what this worldgen
     * can ever place — no probabilistic place() sampling required.</p>
     */
    @GameTest(template = "empty")
    public void item062_nine_part_combines_and_pool_asymmetry(GameTestHelper helper) {
        // 9x part -> full egg block (OSM:2886/2892/2898).
        ninePartCombine(helper, "godzilla");
        ninePartCombine(helper, "the_king");
        ninePartCombine(helper, "the_queen");

        // full block + water -> functional boss spawn egg (table rows, named
        // explicitly by the checklist item).
        waterConversion(helper, "godzilla");
        waterConversion(helper, "the_king");
        waterConversion(helper, "the_queen");

        // Worldgen pool asymmetry (spec §2/§3; D5 report §6).
        List<Supplier<Block>> common = spawnOresPool("COMMON_POOL");
        List<Supplier<Block>> rare = spawnOresPool("RARE_POOL");
        helper.assertTrue(common.size() == 98,
                "common pool must mirror the original nextInt(98) switch (OSW:406-801), size=" + common.size());
        helper.assertTrue(rare.size() == 7,
                "rare pool must mirror the original nextInt(7) switch (OSW:371-402), size=" + rare.size());

        Set<Block> poolBlocks = new HashSet<>();
        common.forEach(s -> poolBlocks.add(s.get()));
        rare.forEach(s -> poolBlocks.add(s.get()));

        helper.assertTrue(poolBlocks.contains(ModBlocks.GODZILLA_PART_SPAWN_BLOCK.get()),
                "Mobzilla PART block must be pool-placed (c68)");
        helper.assertTrue(poolBlocks.contains(ModBlocks.GODZILLA_SPAWN_BLOCK.get()),
                "Mobzilla FULL egg block must be pool-placed (c69)");
        helper.assertTrue(poolBlocks.contains(ModBlocks.THE_KING_PART_SPAWN_BLOCK.get()),
                "King PART block must be pool-placed (c86)");
        helper.assertTrue(poolBlocks.contains(ModBlocks.THE_QUEEN_PART_SPAWN_BLOCK.get()),
                "Queen PART block must be pool-placed (c97)");
        helper.assertFalse(poolBlocks.contains(ModBlocks.THE_KING_SPAWN_BLOCK.get()),
                "full King egg block is craft-only — must NOT be in any worldgen pool");
        helper.assertFalse(poolBlocks.contains(ModBlocks.THE_QUEEN_SPAWN_BLOCK.get()),
                "full Queen egg block is craft-only — must NOT be in any worldgen pool");
        helper.assertFalse(poolBlocks.contains(ModBlocks.CRYSTAL_COW_SPAWN_BLOCK.get()),
                "CrystalCow spawn block generates nowhere (no pool slot) yet keeps its recipe");

        helper.succeed();
    }

    private static void ninePartCombine(GameTestHelper helper, String boss) {
        ItemStack[] parts = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            parts[i] = stack(NS + ":" + boss + "_part_spawn_block");
        }
        assertCrafts(helper, grid(3, 3, parts),
                NS + ":" + boss + "_spawn_block_from_parts",
                NS + ":" + boss + "_spawn_block", 1);
    }

    private static void waterConversion(GameTestHelper helper, String boss) {
        assertCrafts(helper,
                grid(2, 1, new ItemStack(Items.WATER_BUCKET), stack(NS + ":" + boss + "_spawn_block")),
                NS + ":" + boss + "_spawn_egg_from_" + boss + "_spawn_block",
                NS + ":" + boss + "_spawn_egg", 1);
    }
}
