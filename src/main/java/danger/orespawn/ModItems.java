package danger.orespawn;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import danger.orespawn.item.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnMod.MOD_ID);

    // Block items
    public static final DeferredItem<BlockItem> ORE_RUBY_ITEM = ITEMS.registerSimpleBlockItem("ore_ruby", ModBlocks.ORE_RUBY);
    public static final DeferredItem<BlockItem> ORE_AMETHYST_ITEM = ITEMS.registerSimpleBlockItem("ore_amethyst", ModBlocks.ORE_AMETHYST);
    public static final DeferredItem<BlockItem> ORE_URANIUM_ITEM = ITEMS.registerSimpleBlockItem("ore_uranium", ModBlocks.ORE_URANIUM);
    public static final DeferredItem<BlockItem> ORE_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem("ore_titanium", ModBlocks.ORE_TITANIUM);
    public static final DeferredItem<BlockItem> ORE_SALT_ITEM = ITEMS.registerSimpleBlockItem("ore_salt", ModBlocks.ORE_SALT);
    public static final DeferredItem<BlockItem> BLOCK_RUBY_ITEM = ITEMS.registerSimpleBlockItem("block_ruby", ModBlocks.BLOCK_RUBY);
    public static final DeferredItem<BlockItem> BLOCK_AMETHYST_ITEM = ITEMS.registerSimpleBlockItem("block_amethyst", ModBlocks.BLOCK_AMETHYST);
    public static final DeferredItem<BlockItem> BLOCK_URANIUM_ITEM = ITEMS.registerSimpleBlockItem("block_uranium", ModBlocks.BLOCK_URANIUM);
    public static final DeferredItem<BlockItem> BLOCK_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem("block_titanium", ModBlocks.BLOCK_TITANIUM);
    public static final DeferredItem<BlockItem> BLOCK_MOBZILLA_SCALE_ITEM = ITEMS.registerSimpleBlockItem("block_mobzilla_scale", ModBlocks.BLOCK_MOBZILLA_SCALE);
    public static final DeferredItem<BlockItem> BLOCK_CRYSTAL_PINK_ITEM = ITEMS.registerSimpleBlockItem("block_crystal_pink", ModBlocks.BLOCK_CRYSTAL_PINK);
    public static final DeferredItem<BlockItem> BLOCK_TIGERS_EYE_ITEM = ITEMS.registerSimpleBlockItem("block_tigers_eye", ModBlocks.BLOCK_TIGERS_EYE);
    public static final DeferredItem<BlockItem> LAVAFOAM_ITEM = ITEMS.registerSimpleBlockItem("lavafoam", ModBlocks.LAVAFOAM);
    public static final DeferredItem<BlockItem> CRYSTAL_STONE_ITEM = ITEMS.registerSimpleBlockItem("crystal_stone", ModBlocks.CRYSTAL_STONE);
    public static final DeferredItem<BlockItem> CRYSTAL_PLANKS_ITEM = ITEMS.registerSimpleBlockItem("crystal_planks", ModBlocks.CRYSTAL_PLANKS);
    public static final DeferredItem<BlockItem> CRYSTAL_WORKBENCH_ITEM = ITEMS.registerSimpleBlockItem("crystal_workbench", ModBlocks.CRYSTAL_WORKBENCH);
    public static final DeferredItem<BlockItem> CRYSTAL_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("crystal_furnace", ModBlocks.CRYSTAL_FURNACE);
    public static final DeferredItem<BlockItem> EXTREME_TORCH_ITEM = ITEMS.registerSimpleBlockItem("extreme_torch", ModBlocks.EXTREME_TORCH);
    public static final DeferredItem<BlockItem> CRYSTAL_TORCH_ITEM = ITEMS.registerSimpleBlockItem("crystal_torch", ModBlocks.CRYSTAL_TORCH);
    public static final DeferredItem<BlockItem> KRAKEN_REPELLENT_ITEM = ITEMS.registerSimpleBlockItem("kraken_repellent", ModBlocks.KRAKEN_REPELLENT);
    public static final DeferredItem<BlockItem> CREEPER_REPELLENT_ITEM = ITEMS.registerSimpleBlockItem("creeper_repellent", ModBlocks.CREEPER_REPELLENT);
    public static final DeferredItem<BlockItem> ISLAND_ITEM = ITEMS.registerSimpleBlockItem("island", ModBlocks.ISLAND);
    public static final DeferredItem<BlockItem> BLOCK_ENDER_PEARL_ITEM = ITEMS.registerSimpleBlockItem("block_ender_pearl", ModBlocks.BLOCK_ENDER_PEARL);
    public static final DeferredItem<BlockItem> BLOCK_EYE_OF_ENDER_ITEM = ITEMS.registerSimpleBlockItem("block_eye_of_ender", ModBlocks.BLOCK_EYE_OF_ENDER);
    public static final DeferredItem<BlockItem> PIZZA_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pizza", ModBlocks.PIZZA);
    // TF-027: the ONE functional duct tape item (crafted via recipe/duct_tape_item.json);
    // stack size 1 per orig OreSpawnMain.java:1620 func_77625_d(1).
    public static final DeferredItem<BlockItem> DUCT_TAPE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("duct_tape", ModBlocks.DUCT_TAPE, new Item.Properties().stacksTo(1));
    public static final DeferredItem<BlockItem> BLOCK_TELEPORT_ITEM = ITEMS.registerSimpleBlockItem("block_teleport", ModBlocks.BLOCK_TELEPORT);
    public static final DeferredItem<BlockItem> MOLE_DIRT_ITEM = ITEMS.registerSimpleBlockItem("mole_dirt", ModBlocks.MOLE_DIRT);
    public static final DeferredItem<BlockItem> CRYSTAL_COAL_ITEM = ITEMS.registerSimpleBlockItem("crystal_coal", ModBlocks.CRYSTAL_COAL);
    public static final DeferredItem<BlockItem> CRYSTAL_GRASS_ITEM = ITEMS.registerSimpleBlockItem("crystal_grass", ModBlocks.CRYSTAL_GRASS);
    public static final DeferredItem<BlockItem> CRYSTAL_CRYSTAL_ITEM = ITEMS.registerSimpleBlockItem("crystal_crystal", ModBlocks.CRYSTAL_CRYSTAL);
    public static final DeferredItem<BlockItem> TIGERS_EYE_ORE_ITEM = ITEMS.registerSimpleBlockItem("tigers_eye_ore", ModBlocks.TIGERS_EYE_ORE);
    public static final DeferredItem<BlockItem> CRYSTAL_RAT_ITEM = ITEMS.registerSimpleBlockItem("crystal_rat", ModBlocks.CRYSTAL_RAT);
    public static final DeferredItem<BlockItem> CRYSTAL_FAIRY_ITEM = ITEMS.registerSimpleBlockItem("crystal_fairy", ModBlocks.CRYSTAL_FAIRY);
    public static final DeferredItem<BlockItem> RED_ANT_TROLL_ITEM = ITEMS.registerSimpleBlockItem("red_ant_troll", ModBlocks.RED_ANT_TROLL);
    public static final DeferredItem<BlockItem> TERMITE_TROLL_ITEM = ITEMS.registerSimpleBlockItem("termite_troll", ModBlocks.TERMITE_TROLL);
    public static final DeferredItem<BlockItem> KRAKEN_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("kraken_spawn_block", ModBlocks.KRAKEN_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> DRAGON_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("dragon_spawn_block", ModBlocks.DRAGON_SPAWN_BLOCK);
    // WGEN-023 — crystal spawn-block egg ores (orig OreSpawnMain.java:6326-6336)
    public static final DeferredItem<BlockItem> ORE_URCHIN_ITEM = ITEMS.registerSimpleBlockItem("ore_urchin", ModBlocks.ORE_URCHIN);
    public static final DeferredItem<BlockItem> ORE_FLOUNDER_ITEM = ITEMS.registerSimpleBlockItem("ore_flounder", ModBlocks.ORE_FLOUNDER);
    public static final DeferredItem<BlockItem> ORE_SKATE_ITEM = ITEMS.registerSimpleBlockItem("ore_skate", ModBlocks.ORE_SKATE);
    public static final DeferredItem<BlockItem> ORE_ROTATOR_ITEM = ITEMS.registerSimpleBlockItem("ore_rotator", ModBlocks.ORE_ROTATOR);
    public static final DeferredItem<BlockItem> ORE_PEACOCK_ITEM = ITEMS.registerSimpleBlockItem("ore_peacock", ModBlocks.ORE_PEACOCK);
    public static final DeferredItem<BlockItem> ORE_FAIRY_ITEM = ITEMS.registerSimpleBlockItem("ore_fairy", ModBlocks.ORE_FAIRY);
    public static final DeferredItem<BlockItem> ORE_DUNGEON_BEAST_ITEM = ITEMS.registerSimpleBlockItem("ore_dungeon_beast", ModBlocks.ORE_DUNGEON_BEAST);
    public static final DeferredItem<BlockItem> ORE_VORTEX_ITEM = ITEMS.registerSimpleBlockItem("ore_vortex", ModBlocks.ORE_VORTEX);
    public static final DeferredItem<BlockItem> ORE_RAT_ITEM = ITEMS.registerSimpleBlockItem("ore_rat", ModBlocks.ORE_RAT);
    public static final DeferredItem<BlockItem> ORE_WHALE_ITEM = ITEMS.registerSimpleBlockItem("ore_whale", ModBlocks.ORE_WHALE);
    public static final DeferredItem<BlockItem> ORE_IRUKANDJI_ITEM = ITEMS.registerSimpleBlockItem("ore_irukandji", ModBlocks.ORE_IRUKANDJI);
    // Phase D5 (WGEN-005) — block items for the restored SpawnOres pool.
    public static final DeferredItem<BlockItem> SPIDER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("spider_spawn_block", ModBlocks.SPIDER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BAT_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("bat_spawn_block", ModBlocks.BAT_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> COW_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cow_spawn_block", ModBlocks.COW_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> PIG_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pig_spawn_block", ModBlocks.PIG_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SQUID_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("squid_spawn_block", ModBlocks.SQUID_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CHICKEN_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("chicken_spawn_block", ModBlocks.CHICKEN_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CREEPER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("creeper_spawn_block", ModBlocks.CREEPER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SKELETON_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("skeleton_spawn_block", ModBlocks.SKELETON_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ZOMBIE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("zombie_spawn_block", ModBlocks.ZOMBIE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SLIME_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("slime_spawn_block", ModBlocks.SLIME_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GHAST_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ghast_spawn_block", ModBlocks.GHAST_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ZOMBIFIED_PIGLIN_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("zombified_piglin_spawn_block", ModBlocks.ZOMBIFIED_PIGLIN_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ENDERMAN_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("enderman_spawn_block", ModBlocks.ENDERMAN_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CAVE_SPIDER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cave_spider_spawn_block", ModBlocks.CAVE_SPIDER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SILVERFISH_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("silverfish_spawn_block", ModBlocks.SILVERFISH_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> MAGMA_CUBE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("magma_cube_spawn_block", ModBlocks.MAGMA_CUBE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WITCH_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("witch_spawn_block", ModBlocks.WITCH_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SHEEP_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("sheep_spawn_block", ModBlocks.SHEEP_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WOLF_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("wolf_spawn_block", ModBlocks.WOLF_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> MOOSHROOM_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("mooshroom_spawn_block", ModBlocks.MOOSHROOM_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> OCELOT_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ocelot_spawn_block", ModBlocks.OCELOT_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BLAZE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("blaze_spawn_block", ModBlocks.BLAZE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WITHER_SKELETON_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("wither_skeleton_spawn_block", ModBlocks.WITHER_SKELETON_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ENDER_DRAGON_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ender_dragon_spawn_block", ModBlocks.ENDER_DRAGON_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SNOW_GOLEM_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("snow_golem_spawn_block", ModBlocks.SNOW_GOLEM_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> IRON_GOLEM_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("iron_golem_spawn_block", ModBlocks.IRON_GOLEM_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WITHER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("wither_spawn_block", ModBlocks.WITHER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GIRLFRIEND_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("girlfriend_spawn_block", ModBlocks.GIRLFRIEND_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BOYFRIEND_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("boyfriend_spawn_block", ModBlocks.BOYFRIEND_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> RED_COW_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("red_cow_spawn_block", ModBlocks.RED_COW_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CRYSTAL_COW_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("crystal_cow_spawn_block", ModBlocks.CRYSTAL_COW_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> VILLAGER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("villager_spawn_block", ModBlocks.VILLAGER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GOLD_COW_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("gold_cow_spawn_block", ModBlocks.GOLD_COW_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ENCHANTED_APPLE_COW_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("enchanted_apple_cow_spawn_block", ModBlocks.ENCHANTED_APPLE_COW_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> MOTHRA_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("mothra_spawn_block", ModBlocks.MOTHRA_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ALOSAURUS_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("alosaurus_spawn_block", ModBlocks.ALOSAURUS_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CRYOLOPHOSAURUS_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cryolophosaurus_spawn_block", ModBlocks.CRYOLOPHOSAURUS_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CAMARASAURUS_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("camarasaurus_spawn_block", ModBlocks.CAMARASAURUS_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> VELOCITY_RAPTOR_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("velocity_raptor_spawn_block", ModBlocks.VELOCITY_RAPTOR_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> HYDROLISC_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("hydrolisc_spawn_block", ModBlocks.HYDROLISC_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BASILISK_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("basilisk_spawn_block", ModBlocks.BASILISK_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> DRAGONFLY_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("dragonfly_spawn_block", ModBlocks.DRAGONFLY_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> EMPEROR_SCORPION_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("emperor_scorpion_spawn_block", ModBlocks.EMPEROR_SCORPION_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SCORPION_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("scorpion_spawn_block", ModBlocks.SCORPION_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CAVE_FISHER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cave_fisher_spawn_block", ModBlocks.CAVE_FISHER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SPYRO_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("spyro_spawn_block", ModBlocks.SPYRO_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BARYONYX_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("baryonyx_spawn_block", ModBlocks.BARYONYX_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GAMMA_METROID_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("gamma_metroid_spawn_block", ModBlocks.GAMMA_METROID_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> COCKATEIL_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cockateil_spawn_block", ModBlocks.COCKATEIL_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> KYUUBI_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("kyuubi_spawn_block", ModBlocks.KYUUBI_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ALIEN_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("alien_spawn_block", ModBlocks.ALIEN_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ATTACK_SQUID_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("attack_squid_spawn_block", ModBlocks.ATTACK_SQUID_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WATER_DRAGON_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("water_dragon_spawn_block", ModBlocks.WATER_DRAGON_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> LIZARD_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("lizard_spawn_block", ModBlocks.LIZARD_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CEPHADROME_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cephadrome_spawn_block", ModBlocks.CEPHADROME_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BEE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("bee_spawn_block", ModBlocks.BEE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> HORSE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("horse_spawn_block", ModBlocks.HORSE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> TROOPER_BUG_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("trooper_bug_spawn_block", ModBlocks.TROOPER_BUG_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SPIT_BUG_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("spit_bug_spawn_block", ModBlocks.SPIT_BUG_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> STINK_BUG_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("stink_bug_spawn_block", ModBlocks.STINK_BUG_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> OSTRICH_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ostrich_spawn_block", ModBlocks.OSTRICH_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GAZELLE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("gazelle_spawn_block", ModBlocks.GAZELLE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CHIPMUNK_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("chipmunk_spawn_block", ModBlocks.CHIPMUNK_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CREEPING_HORROR_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("creeping_horror_spawn_block", ModBlocks.CREEPING_HORROR_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> TERRIBLE_TERROR_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("terrible_terror_spawn_block", ModBlocks.TERRIBLE_TERROR_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CLIFF_RACER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cliff_racer_spawn_block", ModBlocks.CLIFF_RACER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> TRIFFID_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("triffid_spawn_block", ModBlocks.TRIFFID_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> PITCH_BLACK_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pitch_black_spawn_block", ModBlocks.PITCH_BLACK_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> LURKING_TERROR_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("lurking_terror_spawn_block", ModBlocks.LURKING_TERROR_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GODZILLA_PART_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("godzilla_part_spawn_block", ModBlocks.GODZILLA_PART_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GODZILLA_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("godzilla_spawn_block", ModBlocks.GODZILLA_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> THE_KING_PART_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("the_king_part_spawn_block", ModBlocks.THE_KING_PART_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> THE_QUEEN_PART_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("the_queen_part_spawn_block", ModBlocks.THE_QUEEN_PART_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> THE_KING_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("the_king_spawn_block", ModBlocks.THE_KING_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> THE_QUEEN_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("the_queen_spawn_block", ModBlocks.THE_QUEEN_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WORM_SMALL_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("worm_small_spawn_block", ModBlocks.WORM_SMALL_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WORM_MEDIUM_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("worm_medium_spawn_block", ModBlocks.WORM_MEDIUM_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> WORM_LARGE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("worm_large_spawn_block", ModBlocks.WORM_LARGE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CASSOWARY_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cassowary_spawn_block", ModBlocks.CASSOWARY_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CLOUD_SHARK_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cloud_shark_spawn_block", ModBlocks.CLOUD_SHARK_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> GOLD_FISH_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("gold_fish_spawn_block", ModBlocks.GOLD_FISH_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> LEAF_MONSTER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("leaf_monster_spawn_block", ModBlocks.LEAF_MONSTER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> TSHIRT_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("tshirt_spawn_block", ModBlocks.TSHIRT_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ENDER_KNIGHT_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ender_knight_spawn_block", ModBlocks.ENDER_KNIGHT_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> ENDER_REAPER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ender_reaper_spawn_block", ModBlocks.ENDER_REAPER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BEAVER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("beaver_spawn_block", ModBlocks.BEAVER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> TREX_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("trex_spawn_block", ModBlocks.TREX_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> HERCULES_BEETLE_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("hercules_beetle_spawn_block", ModBlocks.HERCULES_BEETLE_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> MANTIS_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("mantis_spawn_block", ModBlocks.MANTIS_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> STINKY_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("stinky_spawn_block", ModBlocks.STINKY_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> EASTER_BUNNY_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("easter_bunny_spawn_block", ModBlocks.EASTER_BUNNY_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CATER_KILLER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cater_killer_spawn_block", ModBlocks.CATER_KILLER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> MOLENOID_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("molenoid_spawn_block", ModBlocks.MOLENOID_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SEA_MONSTER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("sea_monster_spawn_block", ModBlocks.SEA_MONSTER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SEA_VIPER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("sea_viper_spawn_block", ModBlocks.SEA_VIPER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> LEON_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("leon_spawn_block", ModBlocks.LEON_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> HAMMERHEAD_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("hammerhead_spawn_block", ModBlocks.HAMMERHEAD_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> RUBBER_DUCKY_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("rubber_ducky_spawn_block", ModBlocks.RUBBER_DUCKY_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BAND_P_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("band_p_spawn_block", ModBlocks.BAND_P_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> BRUTALFLY_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("brutalfly_spawn_block", ModBlocks.BRUTALFLY_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> NASTYSAURUS_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("nastysaurus_spawn_block", ModBlocks.NASTYSAURUS_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> POINTYSAURUS_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("pointysaurus_spawn_block", ModBlocks.POINTYSAURUS_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CRICKET_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("cricket_spawn_block", ModBlocks.CRICKET_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> FROG_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("frog_spawn_block", ModBlocks.FROG_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> SPIDER_DRIVER_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("spider_driver_spawn_block", ModBlocks.SPIDER_DRIVER_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> CRAB_SPAWN_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("crab_spawn_block", ModBlocks.CRAB_SPAWN_BLOCK);
    public static final DeferredItem<BlockItem> KING_SPAWNER_ITEM = ITEMS.registerSimpleBlockItem("king_spawner", ModBlocks.KING_SPAWNER);
    public static final DeferredItem<BlockItem> QUEEN_SPAWNER_ITEM = ITEMS.registerSimpleBlockItem("queen_spawner", ModBlocks.QUEEN_SPAWNER);
    public static final DeferredItem<BlockItem> DUNGEON_SPAWNER_ITEM = ITEMS.registerSimpleBlockItem("dungeon_spawner", ModBlocks.DUNGEON_SPAWNER);
    public static final DeferredItem<BlockItem> RANDOM_DUNGEON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("random_dungeon_block", ModBlocks.RANDOM_DUNGEON_BLOCK);
    public static final DeferredItem<BlockItem> UTOPIA_PORTAL_ITEM = ITEMS.registerSimpleBlockItem("utopia_portal", ModBlocks.UTOPIA_PORTAL);
    public static final DeferredItem<BlockItem> APPLE_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("apple_leaves", ModBlocks.APPLE_LEAVES);
    public static final DeferredItem<BlockItem> EXPERIENCE_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("experience_leaves", ModBlocks.EXPERIENCE_LEAVES);
    public static final DeferredItem<BlockItem> SCARY_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("scary_leaves", ModBlocks.SCARY_LEAVES);
    public static final DeferredItem<BlockItem> CHERRY_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("cherry_leaves", ModBlocks.CHERRY_LEAVES);
    public static final DeferredItem<BlockItem> PEACH_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("peach_leaves", ModBlocks.PEACH_LEAVES);
    public static final DeferredItem<BlockItem> CRYSTAL_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("crystal_leaves", ModBlocks.CRYSTAL_LEAVES);
    public static final DeferredItem<BlockItem> CRYSTAL_LEAVES_2_ITEM = ITEMS.registerSimpleBlockItem("crystal_leaves_2", ModBlocks.CRYSTAL_LEAVES_2);
    public static final DeferredItem<BlockItem> CRYSTAL_LEAVES_3_ITEM = ITEMS.registerSimpleBlockItem("crystal_leaves_3", ModBlocks.CRYSTAL_LEAVES_3);
    public static final DeferredItem<BlockItem> SKY_TREE_LOG_ITEM = ITEMS.registerSimpleBlockItem("sky_tree_log", ModBlocks.SKY_TREE_LOG);
    public static final DeferredItem<BlockItem> DUPLICATOR_LOG_ITEM = ITEMS.registerSimpleBlockItem("duplicator_log", ModBlocks.DUPLICATOR_LOG);
    public static final DeferredItem<BlockItem> CRYSTAL_TREE_LOG_ITEM = ITEMS.registerSimpleBlockItem("crystal_tree_log", ModBlocks.CRYSTAL_TREE_LOG);
    public static final DeferredItem<BlockItem> FLOWER_PINK_ITEM = ITEMS.registerSimpleBlockItem("flower_pink", ModBlocks.FLOWER_PINK);
    public static final DeferredItem<BlockItem> FLOWER_BLUE_ITEM = ITEMS.registerSimpleBlockItem("flower_blue", ModBlocks.FLOWER_BLUE);
    public static final DeferredItem<BlockItem> FLOWER_BLACK_ITEM = ITEMS.registerSimpleBlockItem("flower_black", ModBlocks.FLOWER_BLACK);
    public static final DeferredItem<BlockItem> FLOWER_SCARY_ITEM = ITEMS.registerSimpleBlockItem("flower_scary", ModBlocks.FLOWER_SCARY);
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_RED_ITEM = ITEMS.registerSimpleBlockItem("crystal_flower_red", ModBlocks.CRYSTAL_FLOWER_RED);
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_GREEN_ITEM = ITEMS.registerSimpleBlockItem("crystal_flower_green", ModBlocks.CRYSTAL_FLOWER_GREEN);
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_BLUE_ITEM = ITEMS.registerSimpleBlockItem("crystal_flower_blue", ModBlocks.CRYSTAL_FLOWER_BLUE);
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_YELLOW_ITEM = ITEMS.registerSimpleBlockItem("crystal_flower_yellow", ModBlocks.CRYSTAL_FLOWER_YELLOW);
    public static final DeferredItem<BlockItem> EXPERIENCE_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("experience_sapling", ModBlocks.EXPERIENCE_SAPLING);
    public static final DeferredItem<BlockItem> CRYSTAL_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("crystal_sapling", ModBlocks.CRYSTAL_SAPLING);
    public static final DeferredItem<BlockItem> CRYSTAL_SAPLING_2_ITEM = ITEMS.registerSimpleBlockItem("crystal_sapling_2", ModBlocks.CRYSTAL_SAPLING_2);
    public static final DeferredItem<BlockItem> CRYSTAL_SAPLING_3_ITEM = ITEMS.registerSimpleBlockItem("crystal_sapling_3", ModBlocks.CRYSTAL_SAPLING_3);
    public static final DeferredItem<BlockItem> ANT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("ant_block", ModBlocks.ANT_BLOCK);
    public static final DeferredItem<BlockItem> RED_ANT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("red_ant_block", ModBlocks.RED_ANT_BLOCK);
    public static final DeferredItem<BlockItem> TERMITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("termite_block", ModBlocks.TERMITE_BLOCK);
    public static final DeferredItem<BlockItem> CRYSTAL_TERMITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("crystal_termite_block", ModBlocks.CRYSTAL_TERMITE_BLOCK);
    public static final DeferredItem<BlockItem> RAINBOW_ANT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("rainbow_ant_block", ModBlocks.RAINBOW_ANT_BLOCK);
    public static final DeferredItem<BlockItem> UNSTABLE_ANT_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("unstable_ant_block", ModBlocks.UNSTABLE_ANT_BLOCK);

    // Raw materials / Ingots / Gems
    public static final DeferredItem<Item> RUBY = ITEMS.registerSimpleItem("ruby");
    public static final DeferredItem<Item> AMETHYST_GEM = ITEMS.registerSimpleItem("amethyst_gem");
    // Deals radiation damage when held in inventory
    public static final DeferredItem<Item> INGOT_URANIUM = ITEMS.register("ingot_uranium",
            () -> new IngotUranium(new Item.Properties()));
    // Titanium ingot for high-tier crafting recipes
    public static final DeferredItem<Item> INGOT_TITANIUM = ITEMS.register("ingot_titanium",
            () -> new IngotTitanium(new Item.Properties()));
    public static final DeferredItem<Item> URANIUM_NUGGET = ITEMS.registerSimpleItem("uranium_nugget");
    public static final DeferredItem<Item> TITANIUM_NUGGET = ITEMS.registerSimpleItem("titanium_nugget");
    public static final DeferredItem<Item> CRYSTAL_PINK_INGOT = ITEMS.registerSimpleItem("crystal_pink_ingot");
    public static final DeferredItem<Item> TIGERS_EYE_INGOT = ITEMS.registerSimpleItem("tigers_eye_ingot");

    // The Phase-10 "kyanite"/"pink_tourmaline" gem items were port inventions,
    // removed for parity per PN-009 / MODERNIZATION_NOTES MOD-009.
    // Parity removal (asset audit 2026-08-11, ITEM_TEXTURE_MISSING/crystal_wood_lumber):
    // the Phase-10 "crystal_wood_lumber" and "crystal_wood_stick" items were also port
    // inventions with no 1.7.10 counterpart. The original crafts the crystal planks
    // BLOCK directly from logs (CrystalWood; orig OreSpawnMain.java:1869) with no
    // intermediate lumber item, and its only stick item is CrystalSticks
    // (orig OreSpawnMain.java:1728, registered :2280, crafted 2x planks -> 6 at :3375),
    // which this port already registers faithfully below as CRYSTAL_STICKS.

    // Phase 10 â€” Block item for ancient_dried_egg fossil block.
    // Phase D5 (WGEN-005): the invented "ancient_dried_egg" block/item was
    // retired with the full SpawnOres pool restoration (PN-010 closed; the
    // right-click rehydration idea is archived as MOD-013).
    // Salt for cooking recipes (popcorn, salad, etc.)
    public static final DeferredItem<Item> SALT = ITEMS.register("salt",
            () -> new ItemSalt(new Item.Properties()));
    // Crystal dimension crafting sticks
    public static final DeferredItem<Item> CRYSTAL_STICKS = ITEMS.register("crystal_sticks",
            () -> new ItemCrystalSticks(new Item.Properties()));
    public static final DeferredItem<Item> GREEN_GOO = ITEMS.registerSimpleItem("green_goo");

    // Mob drop materials
    public static final DeferredItem<Item> MOTH_SCALE = ITEMS.registerSimpleItem("moth_scale");
    public static final DeferredItem<Item> QUEEN_SCALE = ITEMS.registerSimpleItem("queen_scale");
    public static final DeferredItem<Item> NIGHTMARE_SCALE = ITEMS.registerSimpleItem("nightmare_scale");
    public static final DeferredItem<Item> EMPEROR_SCORPION_SCALE = ITEMS.registerSimpleItem("emperor_scorpion_scale");
    public static final DeferredItem<Item> BASILISK_SCALE = ITEMS.registerSimpleItem("basilisk_scale");
    public static final DeferredItem<Item> WATER_DRAGON_SCALE = ITEMS.registerSimpleItem("water_dragon_scale");
    public static final DeferredItem<Item> PEACOCK_FEATHER = ITEMS.registerSimpleItem("peacock_feather");
    public static final DeferredItem<Item> JUMPY_BUG_SCALE = ITEMS.registerSimpleItem("jumpy_bug_scale");
    public static final DeferredItem<Item> KRAKEN_TOOTH = ITEMS.registerSimpleItem("kraken_tooth");
    public static final DeferredItem<Item> GODZILLA_SCALE = ITEMS.registerSimpleItem("godzilla_scale");
    public static final DeferredItem<Item> BERTHA_HANDLE = ITEMS.registerSimpleItem("bertha_handle");
    public static final DeferredItem<Item> BERTHA_GUARD = ITEMS.registerSimpleItem("bertha_guard");
    public static final DeferredItem<Item> BERTHA_BLADE = ITEMS.registerSimpleItem("bertha_blade");
    public static final DeferredItem<Item> MOLENOID_NOSE = ITEMS.registerSimpleItem("molenoid_nose");
    public static final DeferredItem<Item> SEA_MONSTER_SCALE = ITEMS.registerSimpleItem("sea_monster_scale");
    public static final DeferredItem<Item> WORM_TOOTH = ITEMS.registerSimpleItem("worm_tooth");
    public static final DeferredItem<Item> TREX_TOOTH = ITEMS.registerSimpleItem("trex_tooth");
    public static final DeferredItem<Item> CATERKILLER_JAW = ITEMS.registerSimpleItem("caterkiller_jaw");
    public static final DeferredItem<Item> SEA_VIPER_TONGUE = ITEMS.registerSimpleItem("sea_viper_tongue");
    public static final DeferredItem<Item> VORTEX_EYE_ITEM = ITEMS.registerSimpleItem("vortex_eye");
    public static final DeferredItem<Item> DEAD_STINK_BUG = ITEMS.registerSimpleItem("dead_stink_bug");

    // Ultimate tier tools
    // orig OreSpawnMain.java:1636, UltimateSword.java:49-55 â€” full config-driven enchant set (ITEM-030)
    public static final DeferredItem<Item> ULTIMATE_SWORD = ITEMS.register("ultimate_sword",
            () -> new UltimateSword(ModToolTiers.ULTIMATE, UltimateSword.Variant.FULL,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.ULTIMATE, 3, -2.4f))));
    public static final DeferredItem<Item> ULTIMATE_PICKAXE = ITEMS.register("ultimate_pickaxe",
            () -> new UltimatePickaxe(new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.ULTIMATE, 1.0f, -2.8f))));
    public static final DeferredItem<Item> ULTIMATE_SHOVEL = ITEMS.register("ultimate_shovel",
            () -> new UltimateShovel(new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.ULTIMATE, 1.5f, -3.0f))));
    public static final DeferredItem<Item> ULTIMATE_HOE = ITEMS.register("ultimate_hoe",
            () -> new UltimateHoe(new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.ULTIMATE, -3.0f, 0.0f))));
    public static final DeferredItem<Item> ULTIMATE_AXE = ITEMS.register("ultimate_axe",
            () -> new UltimateAxe(new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.ULTIMATE, 5.0f, -3.0f))));

    // Ruby tier tools
    public static final DeferredItem<Item> RUBY_SWORD = ITEMS.register("ruby_sword",
            () -> new SwordItem(ModToolTiers.RUBY, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.RUBY, 3, -2.4f))));
    public static final DeferredItem<Item> RUBY_PICKAXE = ITEMS.register("ruby_pickaxe",
            () -> new PickaxeItem(ModToolTiers.RUBY, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.RUBY, 1.0f, -2.8f))));
    public static final DeferredItem<Item> RUBY_SHOVEL = ITEMS.register("ruby_shovel",
            () -> new ShovelItem(ModToolTiers.RUBY, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.RUBY, 1.5f, -3.0f))));
    public static final DeferredItem<Item> RUBY_HOE = ITEMS.register("ruby_hoe",
            () -> new HoeItem(ModToolTiers.RUBY, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.RUBY, -3.0f, 0.0f))));
    public static final DeferredItem<Item> RUBY_AXE = ITEMS.register("ruby_axe",
            () -> new AxeItem(ModToolTiers.RUBY, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.RUBY, 5.0f, -3.0f))));

    // Amethyst tier tools
    public static final DeferredItem<Item> AMETHYST_SWORD = ITEMS.register("amethyst_sword",
            () -> new SwordItem(ModToolTiers.AMETHYST, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.AMETHYST, 3, -2.4f))));
    public static final DeferredItem<Item> AMETHYST_PICKAXE = ITEMS.register("amethyst_pickaxe",
            () -> new PickaxeItem(ModToolTiers.AMETHYST, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.AMETHYST, 1.0f, -2.8f))));
    public static final DeferredItem<Item> AMETHYST_SHOVEL = ITEMS.register("amethyst_shovel",
            () -> new ShovelItem(ModToolTiers.AMETHYST, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.AMETHYST, 1.5f, -3.0f))));
    public static final DeferredItem<Item> AMETHYST_HOE = ITEMS.register("amethyst_hoe",
            () -> new HoeItem(ModToolTiers.AMETHYST, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.AMETHYST, -3.0f, 0.0f))));
    public static final DeferredItem<Item> AMETHYST_AXE = ITEMS.register("amethyst_axe",
            () -> new AxeItem(ModToolTiers.AMETHYST, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.AMETHYST, 5.0f, -3.0f))));

    // Emerald tier tools
    public static final DeferredItem<Item> EMERALD_SWORD = ITEMS.register("emerald_sword",
            () -> new SwordItem(ModToolTiers.EMERALD, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));
    public static final DeferredItem<Item> EMERALD_PICKAXE = ITEMS.register("emerald_pickaxe",
            () -> new EmeraldPickaxe(new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.EMERALD, 1.0f, -2.8f))));
    public static final DeferredItem<Item> EMERALD_SHOVEL = ITEMS.register("emerald_shovel",
            () -> new ShovelItem(ModToolTiers.EMERALD, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.EMERALD, 1.5f, -3.0f))));
    public static final DeferredItem<Item> EMERALD_HOE = ITEMS.register("emerald_hoe",
            () -> new HoeItem(ModToolTiers.EMERALD, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.EMERALD, -3.0f, 0.0f))));
    public static final DeferredItem<Item> EMERALD_AXE = ITEMS.register("emerald_axe",
            () -> new AxeItem(ModToolTiers.EMERALD, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.EMERALD, 5.0f, -3.0f))));

    // Crystal Wood tier tools
    public static final DeferredItem<Item> CRYSTAL_WOOD_SWORD = ITEMS.register("crystal_wood_sword",
            () -> new SwordItem(ModToolTiers.CRYSTAL_WOOD, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.CRYSTAL_WOOD, 3, -2.4f))));
    public static final DeferredItem<Item> CRYSTAL_WOOD_PICKAXE = ITEMS.register("crystal_wood_pickaxe",
            () -> new PickaxeItem(ModToolTiers.CRYSTAL_WOOD, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.CRYSTAL_WOOD, 1.0f, -2.8f))));
    public static final DeferredItem<Item> CRYSTAL_WOOD_SHOVEL = ITEMS.register("crystal_wood_shovel",
            () -> new ShovelItem(ModToolTiers.CRYSTAL_WOOD, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.CRYSTAL_WOOD, 1.5f, -3.0f))));
    public static final DeferredItem<Item> CRYSTAL_WOOD_HOE = ITEMS.register("crystal_wood_hoe",
            () -> new HoeItem(ModToolTiers.CRYSTAL_WOOD, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.CRYSTAL_WOOD, -3.0f, 0.0f))));
    public static final DeferredItem<Item> CRYSTAL_WOOD_AXE = ITEMS.register("crystal_wood_axe",
            () -> new AxeItem(ModToolTiers.CRYSTAL_WOOD, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.CRYSTAL_WOOD, 5.0f, -3.0f))));

    // Crystal Stone tier tools
    public static final DeferredItem<Item> CRYSTAL_STONE_SWORD = ITEMS.register("crystal_stone_sword",
            () -> new SwordItem(ModToolTiers.CRYSTAL_STONE, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.CRYSTAL_STONE, 3, -2.4f))));
    public static final DeferredItem<Item> CRYSTAL_STONE_PICKAXE = ITEMS.register("crystal_stone_pickaxe",
            () -> new PickaxeItem(ModToolTiers.CRYSTAL_STONE, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.CRYSTAL_STONE, 1.0f, -2.8f))));
    public static final DeferredItem<Item> CRYSTAL_STONE_SHOVEL = ITEMS.register("crystal_stone_shovel",
            () -> new ShovelItem(ModToolTiers.CRYSTAL_STONE, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.CRYSTAL_STONE, 1.5f, -3.0f))));
    public static final DeferredItem<Item> CRYSTAL_STONE_HOE = ITEMS.register("crystal_stone_hoe",
            () -> new HoeItem(ModToolTiers.CRYSTAL_STONE, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.CRYSTAL_STONE, -3.0f, 0.0f))));
    public static final DeferredItem<Item> CRYSTAL_STONE_AXE = ITEMS.register("crystal_stone_axe",
            () -> new AxeItem(ModToolTiers.CRYSTAL_STONE, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.CRYSTAL_STONE, 5.0f, -3.0f))));

    // Crystal Pink tier tools
    public static final DeferredItem<Item> CRYSTAL_PINK_SWORD = ITEMS.register("crystal_pink_sword",
            () -> new SwordItem(ModToolTiers.CRYSTAL_PINK, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.CRYSTAL_PINK, 3, -2.4f))));
    public static final DeferredItem<Item> CRYSTAL_PINK_PICKAXE = ITEMS.register("crystal_pink_pickaxe",
            () -> new PickaxeItem(ModToolTiers.CRYSTAL_PINK, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.CRYSTAL_PINK, 1.0f, -2.8f))));
    public static final DeferredItem<Item> CRYSTAL_PINK_SHOVEL = ITEMS.register("crystal_pink_shovel",
            () -> new ShovelItem(ModToolTiers.CRYSTAL_PINK, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.CRYSTAL_PINK, 1.5f, -3.0f))));
    public static final DeferredItem<Item> CRYSTAL_PINK_HOE = ITEMS.register("crystal_pink_hoe",
            () -> new HoeItem(ModToolTiers.CRYSTAL_PINK, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.CRYSTAL_PINK, -3.0f, 0.0f))));
    public static final DeferredItem<Item> CRYSTAL_PINK_AXE = ITEMS.register("crystal_pink_axe",
            () -> new AxeItem(ModToolTiers.CRYSTAL_PINK, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.CRYSTAL_PINK, 5.0f, -3.0f))));

    // Tigers Eye tier tools
    public static final DeferredItem<Item> TIGERS_EYE_SWORD = ITEMS.register("tigers_eye_sword",
            () -> new SwordItem(ModToolTiers.TIGERS_EYE, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.TIGERS_EYE, 3, -2.4f))));
    public static final DeferredItem<Item> TIGERS_EYE_PICKAXE = ITEMS.register("tigers_eye_pickaxe",
            () -> new PickaxeItem(ModToolTiers.TIGERS_EYE, new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.TIGERS_EYE, 1.0f, -2.8f))));
    public static final DeferredItem<Item> TIGERS_EYE_SHOVEL = ITEMS.register("tigers_eye_shovel",
            () -> new ShovelItem(ModToolTiers.TIGERS_EYE, new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.TIGERS_EYE, 1.5f, -3.0f))));
    public static final DeferredItem<Item> TIGERS_EYE_HOE = ITEMS.register("tigers_eye_hoe",
            () -> new HoeItem(ModToolTiers.TIGERS_EYE, new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.TIGERS_EYE, -3.0f, 0.0f))));
    public static final DeferredItem<Item> TIGERS_EYE_AXE = ITEMS.register("tigers_eye_axe",
            () -> new AxeItem(ModToolTiers.TIGERS_EYE, new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.TIGERS_EYE, 5.0f, -3.0f))));

    // Special weapons
    public static final DeferredItem<Item> NIGHTMARE_SWORD = ITEMS.register("nightmare_sword",
            () -> new NightmareSword(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.NIGHTMARE, 3, -2.4f))));
    // orig Bertha.java:39-41 â€” Big Bertha bakes Knockback 5 / Bane 1 / Fire Aspect 1 (ITEM-031)
    public static final DeferredItem<Item> BIG_BERTHA = ITEMS.register("big_bertha",
            () -> new Bertha(ModToolTiers.BERTHA, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.BERTHA, 3, -2.4f)),
                    0, new int[]{5, 1, 1}, net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK, net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS, net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT));
    // orig OreSpawnMain.java:1646 â€” Slice is a plain Bertha-class clone: same baked
    // KB5/Bane1/FireAspect1 set and shockwave (ITEM-032)
    public static final DeferredItem<Item> SLICE = ITEMS.register("slice",
            () -> new Bertha(ModToolTiers.BERTHA, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.BERTHA, 3, -2.4f)),
                    0, new int[]{5, 1, 1}, net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK, net.minecraft.world.item.enchantment.Enchantments.BANE_OF_ARTHROPODS, net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT));
    // orig Bertha.java:36-37 â€” Royal Guardian Sword bakes Unbreaking 5 (not Sharpness); hitType 2 (ITEM-033)
    public static final DeferredItem<Item> ROYAL_GUARDIAN_SWORD = ITEMS.register("royal_guardian_sword",
            () -> new Bertha(ModToolTiers.ROYAL, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.ROYAL, 3, -2.4f)),
                    2, new int[]{5}, net.minecraft.world.item.enchantment.Enchantments.UNBREAKING));
    // orig OreSpawnMain.java:1649 â€” Battle Axe is UltimateSword-class (no shockwave),
    // Looting 3 + Unbreaking 3 branch, dur 3000 class override (ITEM-036)
    public static final DeferredItem<Item> BATTLE_AXE = ITEMS.register("battle_axe",
            () -> new UltimateSword(ModToolTiers.BATTLE, UltimateSword.Variant.BATTLE_AXE,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.BATTLE, 3, -2.4f))));
    // orig OreSpawnMain.java:1651 â€” Queen Battle Axe is UltimateSword-class (no shockwave)
    // and takes the FULL enchant branch (orig UltimateSword.java:48-55), dur 3000 (ITEM-035)
    public static final DeferredItem<Item> QUEEN_BATTLE_AXE = ITEMS.register("queen_battle_axe",
            () -> new UltimateSword(ModToolTiers.QUEEN_BATTLE, UltimateSword.Variant.FULL,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.QUEEN_BATTLE, 3, -2.4f))));
    // orig OreSpawnMain.java:1650 â€” Chainsaw is UltimateSword-class: no baked enchants,
    // AoE left-click, tree crush, saw sound (ITEM-037)
    public static final DeferredItem<Item> CHAINSAW = ITEMS.register("chainsaw",
            () -> new Chainsaw(ModToolTiers.CHAINSAW,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.CHAINSAW, 3, -2.4f))));
    // orig Bertha.java:38,91-93 â€” Attitude Adjuster: no baked enchants, hitType 3 (ITEM-034)
    public static final DeferredItem<Item> ATTITUDE_ADJUSTER = ITEMS.register("attitude_adjuster",
            () -> new Bertha(ModToolTiers.HAMMY, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModToolTiers.HAMMY, 3, -2.4f)),
                    3, new int[]{}, new net.minecraft.resources.ResourceKey[0]));
    public static final DeferredItem<Item> EXPERIENCE_SWORD = ITEMS.register("experience_sword",
            () -> new ExperienceSword(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));
    public static final DeferredItem<Item> POISON_SWORD = ITEMS.register("poison_sword",
            () -> new PoisonSword(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));
    public static final DeferredItem<Item> RAT_SWORD = ITEMS.register("rat_sword",
            () -> new RatSword(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));
    public static final DeferredItem<Item> FAIRY_SWORD = ITEMS.register("fairy_sword",
            () -> new FairySword(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));
    public static final DeferredItem<Item> MANTIS_CLAW = ITEMS.register("mantis_claw",
            () -> new MantisClaw(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));
    public static final DeferredItem<Item> BIG_HAMMER = ITEMS.register("big_hammer",
            () -> new BigHammer(new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.AMETHYST, 3, -2.4f))));
    public static final DeferredItem<Item> ROSE_SWORD = ITEMS.register("rose_sword",
            () -> new SwordItem(ModToolTiers.EMERALD, new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.EMERALD, 3, -2.4f))));

    // Ranged weapons
    public static final DeferredItem<Item> ULTIMATE_BOW = ITEMS.register("ultimate_bow",
            () -> new UltimateBow(new Item.Properties().durability(1000)));
    public static final DeferredItem<Item> SKATE_BOW = ITEMS.register("skate_bow",
            () -> new SkateBow(new Item.Properties().durability(300)));
    public static final DeferredItem<Item> ULTIMATE_FISHING_ROD = ITEMS.register("ultimate_fishing_rod",
            () -> new UltimateFishingRod(new Item.Properties().durability(3000)));
    public static final DeferredItem<Item> RAY_GUN = ITEMS.register("ray_gun",
            () -> new ItemRayGun(new Item.Properties().durability(50).stacksTo(1)));
    public static final DeferredItem<Item> THUNDER_STAFF = ITEMS.register("thunder_staff",
            () -> new ItemThunderStaff(new Item.Properties().durability(50).stacksTo(1)));
    public static final DeferredItem<Item> SQUID_ZOOKA = ITEMS.register("squid_zooka",
            () -> new ItemSquidZooka(new Item.Properties().durability(100).stacksTo(1)));
    public static final DeferredItem<Item> CREEPER_LAUNCHER = ITEMS.register("creeper_launcher",
            () -> new ItemCreeperLauncher(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> WRENCH = ITEMS.register("wrench",
            // orig ItemWrench.java:26 â€” setMaxDamage(100)
            () -> new ItemWrench(new Item.Properties().durability(100)));

    // Throwables
    public static final DeferredItem<Item> WATER_BALL = ITEMS.register("water_ball",
            () -> new ItemWaterBall(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> LASER_BALL = ITEMS.register("laser_ball",
            () -> new ItemLaserBall(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ICE_BALL = ITEMS.register("ice_ball",
            () -> new ItemIceBall(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> ACID = ITEMS.register("acid",
            () -> new ItemAcid(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> DEAD_IRUKANDJI = ITEMS.register("dead_irukandji",
            // orig ItemIrukandji.java:20-23 â€” stack 64, throws a DeadIrukandji on use
            () -> new ItemDeadIrukandji(new Item.Properties()));
    public static final DeferredItem<Item> IRUKANDJI_ARROW = ITEMS.registerSimpleItem("irukandji_arrow");
    // Rock types are 1-based to match orig MyDispenserBehaviorRock.java:36-71 and
    // ORIG EntityThrownRock.java:74-227 / RockBase.java:213-251 (type 0 means "unset"
    // and re-randomizes in RockBase.tick). D4: was 0-11, which shifted every rock's
    // damage/recovery/placement down one tier and broke rock_small entirely.
    public static final DeferredItem<Item> ROCK_SMALL = ITEMS.register("rock_small",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 1));
    public static final DeferredItem<Item> ROCK = ITEMS.register("rock",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 2));
    public static final DeferredItem<Item> ROCK_RED = ITEMS.register("rock_red",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 3));
    public static final DeferredItem<Item> ROCK_GREEN = ITEMS.register("rock_green",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 4));
    public static final DeferredItem<Item> ROCK_BLUE = ITEMS.register("rock_blue",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 5));
    public static final DeferredItem<Item> ROCK_PURPLE = ITEMS.register("rock_purple",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 6));
    public static final DeferredItem<Item> ROCK_SPIKEY = ITEMS.register("rock_spikey",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 7));
    public static final DeferredItem<Item> ROCK_TNT = ITEMS.register("rock_tnt",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 8));
    public static final DeferredItem<Item> ROCK_CRYSTAL_RED = ITEMS.register("rock_crystal_red",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 9));
    public static final DeferredItem<Item> ROCK_CRYSTAL_GREEN = ITEMS.register("rock_crystal_green",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 10));
    public static final DeferredItem<Item> ROCK_CRYSTAL_BLUE = ITEMS.register("rock_crystal_blue",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 11));
    public static final DeferredItem<Item> ROCK_CRYSTAL_TNT = ITEMS.register("rock_crystal_tnt",
            () -> new ItemRock(new Item.Properties().stacksTo(16), 12));

    // Food items â€” special fish grant potion effects on consumption
    // Grants 30s fire resistance when eaten
    public static final DeferredItem<Item> FIRE_FISH = ITEMS.register("fire_fish",
            () -> new ItemFireFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build())));
    // Grants 30s fire resistance when eaten
    public static final DeferredItem<Item> SUN_FISH = ITEMS.register("sun_fish",
            () -> new ItemSunFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6f).build())));
    // Grants 60s fire resistance when eaten (double duration)
    public static final DeferredItem<Item> LAVA_EEL = ITEMS.register("lava_eel",
            () -> new ItemLavaEel(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.6f).build())));
    // Grants 30s fire resistance when eaten
    public static final DeferredItem<Item> SPARK_FISH = ITEMS.register("spark_fish",
            () -> new ItemSparkFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.2f).build())));
    // Generic fish â€” 25% chance to inflict hunger debuff
    public static final DeferredItem<Item> GREEN_FISH = ITEMS.register("green_fish",
            () -> new ItemGenericFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5f).build())));
    public static final DeferredItem<Item> BLUE_FISH = ITEMS.register("blue_fish",
            () -> new ItemGenericFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4f).build())));
    public static final DeferredItem<Item> PINK_FISH = ITEMS.register("pink_fish",
            () -> new ItemGenericFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build())));
    public static final DeferredItem<Item> ROCK_FISH = ITEMS.register("rock_fish",
            () -> new ItemGenericFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.7f).build())));
    public static final DeferredItem<Item> WOOD_FISH = ITEMS.register("wood_fish",
            () -> new ItemGenericFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.7f).build())));
    public static final DeferredItem<Item> GREY_FISH = ITEMS.register("grey_fish",
            () -> new ItemGenericFish(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.5f).build())));
    public static final DeferredItem<Item> POPCORN = ITEMS.register("popcorn",
            () -> new ItemPopcorn(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.5f).build())));
    public static final DeferredItem<Item> BUTTERED_POPCORN = ITEMS.register("buttered_popcorn",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.6f).build())));
    public static final DeferredItem<Item> BUTTERED_SALTED_POPCORN = ITEMS.register("buttered_salted_popcorn",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.75f).build())));
    public static final DeferredItem<Item> POPCORN_BAG = ITEMS.register("popcorn_bag",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationModifier(1.25f).build())));
    public static final DeferredItem<Item> BUTTER = ITEMS.register("butter",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.5f).build())));
    public static final DeferredItem<Item> CORN_DOG = ITEMS.register("corn_dog",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(16).saturationModifier(2.5f).build())));
    public static final DeferredItem<Item> RAW_CORN_DOG = ITEMS.register("raw_corn_dog",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build())));
    // orig ItemSunFish.java:29-32 — Speed + Jump Boost, 2000 ticks
    public static final DeferredItem<Item> BUTTER_CANDY = ITEMS.register("butter_candy",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2000, 0), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, 2000, 0), 1.0f)
                    .build())));
    // orig ItemSunFish.java:33-36 — Regeneration + Strength, 2000 ticks
    public static final DeferredItem<Item> COOKED_BACON = ITEMS.register("cooked_bacon",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(14).saturationModifier(1.5f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 2000, 0), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2000, 0), 1.0f)
                    .build())));
    public static final DeferredItem<Item> RAW_BACON = ITEMS.register("raw_bacon",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.0f).build())));
    public static final DeferredItem<Item> COOKED_CRAB_MEAT = ITEMS.register("cooked_crab_meat",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.75f).build())));
    public static final DeferredItem<Item> RAW_CRAB_MEAT = ITEMS.register("raw_crab_meat",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.25f).build())));
    public static final DeferredItem<Item> CHEESE = ITEMS.register("cheese",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.5f).build())));
    public static final DeferredItem<Item> SALAD = ITEMS.register("salad",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(10).saturationModifier(0.95f).build())));
    public static final DeferredItem<Item> BLT_SANDWICH = ITEMS.register("blt_sandwich",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(12).saturationModifier(0.95f).build())));
    public static final DeferredItem<Item> CRABBY_PATTY = ITEMS.register("crabby_patty",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(16).saturationModifier(2.35f).build())));
    public static final DeferredItem<Item> COOKED_PEACOCK = ITEMS.register("cooked_peacock",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(12).saturationModifier(1.4f).build())));
    public static final DeferredItem<Item> RAW_PEACOCK = ITEMS.register("raw_peacock",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.7f).build())));
    public static final DeferredItem<Item> STRAWBERRY = ITEMS.register("strawberry",
            () -> new ItemStrawberry(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.65f).build())));
    public static final DeferredItem<Item> CHERRIES = ITEMS.register("cherries",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.45f).build())));
    public static final DeferredItem<Item> PEACH = ITEMS.register("peach",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.55f).build())));
    // orig ItemSunFish.java:37-40 — Regeneration + Strength, 3000 ticks
    public static final DeferredItem<Item> CRYSTAL_APPLE = ITEMS.register("crystal_apple",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.85f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 3000, 0), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3000, 0), 1.0f)
                    .build())));
    // orig ItemSunFish.java:41-48 (MyLove) — Regen IV + Strength III + Fire Res III
    // + Resistance II for 6000t; Speed + Jump Boost for 5000t
    public static final DeferredItem<Item> HEART = ITEMS.register("heart",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.95f)
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 6000, 3), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 2), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5000, 0), 1.0f)
                    .effect(() -> new MobEffectInstance(MobEffects.JUMP, 5000, 0), 1.0f)
                    .build())));
    // Right-click to place as a pizza block in the world
    public static final DeferredItem<Item> PIZZA_ITEM = ITEMS.register("pizza_item",
            () -> new ItemPizza(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.0f).build()).stacksTo(1),
                    ModBlocks.PIZZA.get()));

    // Seeds & saplings
    public static final DeferredItem<Item> STRAWBERRY_SEED = ITEMS.registerSimpleItem("strawberry_seed");
    public static final DeferredItem<Item> BUTTERFLY_SEED = ITEMS.registerSimpleItem("butterfly_seed");
    public static final DeferredItem<Item> MOTH_SEED = ITEMS.registerSimpleItem("moth_seed");
    public static final DeferredItem<Item> MOSQUITO_SEED = ITEMS.registerSimpleItem("mosquito_seed");
    public static final DeferredItem<Item> FIREFLY_SEED = ITEMS.registerSimpleItem("firefly_seed");
    public static final DeferredItem<Item> RADISH = ITEMS.register("radish",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.45f).build())));
    public static final DeferredItem<Item> RICE = ITEMS.register("rice",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.65f).build())));
    public static final DeferredItem<Item> CORN_SEED = ITEMS.registerSimpleItem("corn_seed");
    public static final DeferredItem<Item> CORN_COB = ITEMS.register("corn_cob",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.75f).build())));
    public static final DeferredItem<Item> QUINOA = ITEMS.register("quinoa",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.85f).build())));
    public static final DeferredItem<Item> TOMATO = ITEMS.register("tomato",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.55f).build())));
    public static final DeferredItem<Item> LETTUCE = ITEMS.register("lettuce",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.45f).build())));
    public static final DeferredItem<Item> RADISH_SEED = ITEMS.registerSimpleItem("radish_seed");
    public static final DeferredItem<Item> RICE_SEED = ITEMS.registerSimpleItem("rice_seed");
    public static final DeferredItem<Item> QUINOA_SEED = ITEMS.registerSimpleItem("quinoa_seed");
    public static final DeferredItem<Item> TOMATO_SEED = ITEMS.registerSimpleItem("tomato_seed");
    public static final DeferredItem<Item> LETTUCE_SEED = ITEMS.registerSimpleItem("lettuce_seed");
    public static final DeferredItem<Item> APPLE_TREE_SEED = ITEMS.registerSimpleItem("apple_tree_seed");
    public static final DeferredItem<Item> CHERRY_TREE_SEED = ITEMS.registerSimpleItem("cherry_tree_seed");
    public static final DeferredItem<Item> PEACH_TREE_SEED = ITEMS.registerSimpleItem("peach_tree_seed");
    // Plants an experience tree sapling when used on grass/dirt
    public static final DeferredItem<Item> EXPERIENCE_TREE_SEED = ITEMS.register("experience_tree_seed",
            () -> new ItemExperienceTreeSeed(new Item.Properties()));

    // Utility items
    public static final DeferredItem<Item> MAGIC_APPLE = ITEMS.register("magic_apple",
            () -> new ItemMagicApple(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> MINERS_DREAM = ITEMS.register("miners_dream",
            () -> new ItemMinersDream(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> RANDOM_DUNGEON = ITEMS.register("random_dungeon",
            () -> new ItemRandomDungeon(new Item.Properties()));

    // Phase 11 â€” Coin: right-click consumes & rolls orespawn:gameplay/coin_reward
    public static final DeferredItem<Item> EXPERIENCE_CATCHER = ITEMS.register("experience_catcher",
            () -> new ExperienceCatcher(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> SUNSPOT_URCHIN = ITEMS.register("sunspot_urchin",
            () -> new ItemSunspotUrchin(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> SIFTER = ITEMS.register("sifter",
            // orig ItemSifter.java:25-27 â€” stack size 1, setMaxDamage(600)
            () -> new ItemSifter(new Item.Properties().stacksTo(1).durability(600)));
    public static final DeferredItem<Item> SPIDER_ROBOT_KIT = ITEMS.register("spider_robot_kit",
            // orig ItemSpiderRobotKit.java:26-29 â€” stack 1, durability = SpiderRobot health (1500)
            () -> new ItemSpiderRobotKit(new Item.Properties().stacksTo(1).durability((int) MobStats.SPIDER_ROBOT.maxHealth()),
                    () -> ModEntities.SPIDER_ROBOT.get()));
    public static final DeferredItem<Item> ANT_ROBOT_KIT = ITEMS.register("ant_robot_kit",
            // orig ItemSpiderRobotKit.java:26,31 â€” stack 1, durability = AntRobot health (300)
            () -> new ItemSpiderRobotKit(new Item.Properties().stacksTo(1).durability((int) MobStats.ANT_ROBOT.maxHealth()),
                    () -> ModEntities.ANT_ROBOT.get()));
    public static final DeferredItem<Item> ZOO_KEEPER = ITEMS.register("zoo_keeper",
            // orig ItemZooKeeper.java:21 â€” setMaxDamage(1)
            () -> new ItemZooKeeper(new Item.Properties().durability(1)));
    public static final DeferredItem<Item> NETHER_LOST = ITEMS.register("nether_lost",
            // orig ItemNetherLost.java:24-25 â€” stack 1, setMaxDamage(3000)
            () -> new ItemNetherLost(new Item.Properties().stacksTo(1).durability(3000)));
    // orig OreSpawnMain.java:1904/5174: "elevator" internally, displayed "Hoverboard".
    public static final DeferredItem<Item> ELEVATOR = ITEMS.register("elevator",
            () -> new ItemElevator(new Item.Properties()));
    public static final DeferredItem<Item> INSTANT_SHELTER = ITEMS.register("instant_shelter",
            () -> new InstantShelter(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> INSTANT_GARDEN = ITEMS.register("instant_garden",
            () -> new InstantGarden(new Item.Properties().stacksTo(16)));
    // TF-027: inert "duct_tape_item" registration removed — orig 1.7.10 has a single
    // duct tape item (OreSpawnMain.java:1619-1620) that places the block; that role is
    // filled by DUCT_TAPE_BLOCK_ITEM ("orespawn:duct_tape") above.
    public static final DeferredItem<Item> STEP_UP = ITEMS.register("step_up",
            () -> new StepUp(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> STEP_DOWN = ITEMS.register("step_down",
            () -> new StepDown(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> STEP_ACROSS = ITEMS.register("step_across",
            () -> new StepAccross(new Item.Properties().stacksTo(16)));

    // Shoes / accessories — throwables, orig OreSpawnMain.java:1700-1704
    // (ItemShoes ids: redheels 2, blackheels 3, slippers 4, boots 5, gamecontroller 6)
    public static final DeferredItem<Item> RED_HEELS = ITEMS.register("red_heels",
            () -> new danger.orespawn.item.ItemShoes(new Item.Properties(), 2));
    public static final DeferredItem<Item> BLACK_HEELS = ITEMS.register("black_heels",
            () -> new danger.orespawn.item.ItemShoes(new Item.Properties(), 3));
    public static final DeferredItem<Item> SLIPPERS = ITEMS.register("slippers",
            () -> new danger.orespawn.item.ItemShoes(new Item.Properties(), 4));
    public static final DeferredItem<Item> BOOTS_SHOES = ITEMS.register("boots_shoes",
            () -> new danger.orespawn.item.ItemShoes(new Item.Properties(), 5));
    public static final DeferredItem<Item> GAME_CONTROLLER = ITEMS.register("game_controller",
            () -> new danger.orespawn.item.ItemShoes(new Item.Properties(), 6));


    // ==================== ARMOR ====================
    // Durability multipliers below are the original per-set durability values from
    // orig OreSpawnMain.java:1489-1502 (get_armorstats arg 3). 1.21.1
    // ArmorItem.Type.getDurability uses the same per-slot bases as the 1.7.10
    // ItemArmor (helmet x11, chest x16, legs x15, boots x13), so passing the
    // original value reproduces the original per-piece durability exactly
    // (e.g. Ultimate helmet = 200 x 11 = 2200).
    // Ultimate armor — orig OreSpawnMain.java:1494 (durability 200)
    public static final DeferredItem<Item> ULTIMATE_HELMET = ITEMS.register("ultimate_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ULTIMATE, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(200)), "ultimate"));
    public static final DeferredItem<Item> ULTIMATE_CHESTPLATE = ITEMS.register("ultimate_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ULTIMATE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(200)), "ultimate"));
    public static final DeferredItem<Item> ULTIMATE_LEGGINGS = ITEMS.register("ultimate_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ULTIMATE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(200)), "ultimate"));
    public static final DeferredItem<Item> ULTIMATE_BOOTS_ARMOR = ITEMS.register("ultimate_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ULTIMATE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(200)), "ultimate"));

    // Mobzilla armor — orig OreSpawnMain.java:1498 (durability 1000)
    public static final DeferredItem<Item> MOBZILLA_HELMET = ITEMS.register("mobzilla_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOBZILLA, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(1000)), "mobzilla"));
    public static final DeferredItem<Item> MOBZILLA_CHESTPLATE = ITEMS.register("mobzilla_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOBZILLA, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(1000)), "mobzilla"));
    public static final DeferredItem<Item> MOBZILLA_LEGGINGS = ITEMS.register("mobzilla_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOBZILLA, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(1000)), "mobzilla"));
    public static final DeferredItem<Item> MOBZILLA_BOOTS = ITEMS.register("mobzilla_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOBZILLA, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(1000)), "mobzilla"));

    // Lava Eel armor — orig OreSpawnMain.java:1493 (durability 40)
    public static final DeferredItem<Item> LAVAEEL_HELMET = ITEMS.register("lavaeel_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAVA_EEL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(40)), "lavaeel"));
    public static final DeferredItem<Item> LAVAEEL_CHESTPLATE = ITEMS.register("lavaeel_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAVA_EEL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(40)), "lavaeel"));
    public static final DeferredItem<Item> LAVAEEL_LEGGINGS = ITEMS.register("lavaeel_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAVA_EEL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(40)), "lavaeel"));
    public static final DeferredItem<Item> LAVAEEL_BOOTS = ITEMS.register("lavaeel_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAVA_EEL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(40)), "lavaeel"));

    // Moth Scale armor — orig OreSpawnMain.java:1492 (durability 50)
    public static final DeferredItem<Item> MOTHSCALE_HELMET = ITEMS.register("mothscale_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOTH_SCALE, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(50)), "mothscale"));
    public static final DeferredItem<Item> MOTHSCALE_CHESTPLATE = ITEMS.register("mothscale_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOTH_SCALE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(50)), "mothscale"));
    public static final DeferredItem<Item> MOTHSCALE_LEGGINGS = ITEMS.register("mothscale_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOTH_SCALE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(50)), "mothscale"));
    public static final DeferredItem<Item> MOTHSCALE_BOOTS = ITEMS.register("mothscale_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.MOTH_SCALE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(50)), "mothscale"));

    // Emerald armor — orig OreSpawnMain.java:1490 (durability 60)
    public static final DeferredItem<Item> EMERALD_HELMET = ITEMS.register("emerald_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EMERALD, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(60)), "emerald"));
    public static final DeferredItem<Item> EMERALD_CHESTPLATE = ITEMS.register("emerald_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EMERALD, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(60)), "emerald"));
    public static final DeferredItem<Item> EMERALD_LEGGINGS = ITEMS.register("emerald_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EMERALD, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(60)), "emerald"));
    public static final DeferredItem<Item> EMERALD_BOOTS_ARMOR = ITEMS.register("emerald_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EMERALD, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(60)), "emerald"));

    // Experience armor — orig OreSpawnMain.java:1491 (durability 70)
    public static final DeferredItem<Item> EXPERIENCE_HELMET = ITEMS.register("experience_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EXPERIENCE, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(70)), "experience"));
    public static final DeferredItem<Item> EXPERIENCE_CHESTPLATE = ITEMS.register("experience_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EXPERIENCE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(70)), "experience"));
    public static final DeferredItem<Item> EXPERIENCE_LEGGINGS = ITEMS.register("experience_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EXPERIENCE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(70)), "experience"));
    public static final DeferredItem<Item> EXPERIENCE_BOOTS = ITEMS.register("experience_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.EXPERIENCE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(70)), "experience"));

    // Ruby armor — orig OreSpawnMain.java:1499 (durability 90)
    public static final DeferredItem<Item> RUBY_HELMET = ITEMS.register("ruby_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.RUBY, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(90)), "ruby"));
    public static final DeferredItem<Item> RUBY_CHESTPLATE = ITEMS.register("ruby_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.RUBY, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(90)), "ruby"));
    public static final DeferredItem<Item> RUBY_LEGGINGS = ITEMS.register("ruby_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.RUBY, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(90)), "ruby"));
    public static final DeferredItem<Item> RUBY_BOOTS_ARMOR = ITEMS.register("ruby_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.RUBY, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(90)), "ruby"));

    // Amethyst armor — orig OreSpawnMain.java:1489 (durability 100)
    public static final DeferredItem<Item> AMETHYST_HELMET = ITEMS.register("amethyst_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.AMETHYST, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(100)), "amethyst"));
    public static final DeferredItem<Item> AMETHYST_CHESTPLATE = ITEMS.register("amethyst_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.AMETHYST, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(100)), "amethyst"));
    public static final DeferredItem<Item> AMETHYST_LEGGINGS = ITEMS.register("amethyst_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.AMETHYST, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(100)), "amethyst"));
    public static final DeferredItem<Item> AMETHYST_BOOTS_ARMOR = ITEMS.register("amethyst_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.AMETHYST, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(100)), "amethyst"));

    // Crystal Pink armor — orig OreSpawnMain.java:1495 (durability 50)
    public static final DeferredItem<Item> PINK_HELMET = ITEMS.register("pink_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PINK, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(50)), "pink"));
    public static final DeferredItem<Item> PINK_CHESTPLATE = ITEMS.register("pink_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PINK, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(50)), "pink"));
    public static final DeferredItem<Item> PINK_LEGGINGS = ITEMS.register("pink_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PINK, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(50)), "pink"));
    public static final DeferredItem<Item> PINK_BOOTS = ITEMS.register("pink_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PINK, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(50)), "pink"));

    // Tigers Eye armor — orig OreSpawnMain.java:1496 (durability 80)
    public static final DeferredItem<Item> TIGERSEYE_HELMET = ITEMS.register("tigerseye_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.TIGERS_EYE, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(80)), "tigerseye"));
    public static final DeferredItem<Item> TIGERSEYE_CHESTPLATE = ITEMS.register("tigerseye_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.TIGERS_EYE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(80)), "tigerseye"));
    public static final DeferredItem<Item> TIGERSEYE_LEGGINGS = ITEMS.register("tigerseye_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.TIGERS_EYE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(80)), "tigerseye"));
    public static final DeferredItem<Item> TIGERSEYE_BOOTS = ITEMS.register("tigerseye_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.TIGERS_EYE, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(80)), "tigerseye"));

    // Peacock armor — orig OreSpawnMain.java:1497 (durability 40)
    public static final DeferredItem<Item> PEACOCK_HELMET = ITEMS.register("peacock_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PEACOCK, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(40)), "peacock"));
    public static final DeferredItem<Item> PEACOCK_CHESTPLATE = ITEMS.register("peacock_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PEACOCK, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(40)), "peacock"));
    public static final DeferredItem<Item> PEACOCK_LEGGINGS = ITEMS.register("peacock_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PEACOCK, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(40)), "peacock"));
    public static final DeferredItem<Item> PEACOCK_BOOTS = ITEMS.register("peacock_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.PEACOCK, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(40)), "peacock"));

    // Royal Guardian armor — orig OreSpawnMain.java:1500 (durability 2000)
    public static final DeferredItem<Item> ROYAL_HELMET = ITEMS.register("royal_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ROYAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(2000)), "royal"));
    public static final DeferredItem<Item> ROYAL_CHESTPLATE = ITEMS.register("royal_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ROYAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(2000)), "royal"));
    public static final DeferredItem<Item> ROYAL_LEGGINGS = ITEMS.register("royal_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ROYAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(2000)), "royal"));
    public static final DeferredItem<Item> ROYAL_BOOTS = ITEMS.register("royal_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.ROYAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(2000)), "royal"));

    // Lapis armor — orig OreSpawnMain.java:1501 (durability 60)
    public static final DeferredItem<Item> LAPIS_HELMET = ITEMS.register("lapis_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAPIS, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(60)), "lapis"));
    public static final DeferredItem<Item> LAPIS_CHESTPLATE = ITEMS.register("lapis_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAPIS, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(60)), "lapis"));
    public static final DeferredItem<Item> LAPIS_LEGGINGS = ITEMS.register("lapis_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAPIS, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(60)), "lapis"));
    public static final DeferredItem<Item> LAPIS_BOOTS = ITEMS.register("lapis_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.LAPIS, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(60)), "lapis"));

    // Queen armor — orig OreSpawnMain.java:1502 (durability 1500)
    public static final DeferredItem<Item> QUEEN_HELMET = ITEMS.register("queen_helmet",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.QUEEN, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(1500)), "queen"));
    public static final DeferredItem<Item> QUEEN_CHESTPLATE = ITEMS.register("queen_chestplate",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.QUEEN, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(1500)), "queen"));
    public static final DeferredItem<Item> QUEEN_LEGGINGS = ITEMS.register("queen_leggings",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.QUEEN, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(1500)), "queen"));
    public static final DeferredItem<Item> QUEEN_BOOTS = ITEMS.register("queen_boots",
            () -> new ItemOreSpawnArmor(ModArmorMaterials.QUEEN, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(1500)), "queen"));

    // Zoo cages (legacy, kept for backwards compat)
    public static final DeferredItem<Item> ZOO_CAGE_2 = ITEMS.register("zoo_cage_2",
            () -> new ZooCageItem(new Item.Properties(), 2));
    public static final DeferredItem<Item> ZOO_CAGE_4 = ITEMS.register("zoo_cage_4",
            () -> new ZooCageItem(new Item.Properties(), 4));
    public static final DeferredItem<Item> ZOO_CAGE_6 = ITEMS.register("zoo_cage_6",
            () -> new ZooCageItem(new Item.Properties(), 6));
    public static final DeferredItem<Item> ZOO_CAGE_8 = ITEMS.register("zoo_cage_8",
            () -> new ZooCageItem(new Item.Properties(), 8));
    public static final DeferredItem<Item> ZOO_CAGE_10 = ITEMS.register("zoo_cage_10",
            () -> new ZooCageItem(new Item.Properties(), 10));
    public static final DeferredItem<Item> CAGE_EMPTY = ITEMS.register("cage_empty",
            () -> new EmptyCageItem(new Item.Properties()));

    // Modern caged mob item (stores entity type via DataComponent)
    public static final DeferredItem<Item> CAGED_MOB = ITEMS.register("caged_mob",
            () -> new CagedMobItem(new Item.Properties().stacksTo(1)));

    // NOTE (ITEM-066, Phase D5): the invented "prince_egg"/"princess_egg"
    // trophy items were removed. The original ThePrinceEgg/ThePrincessEgg
    // (orig OreSpawnMain.java:5616/5630) are functional ItemSpawnEggs; every
    // consumer (Queen/Teen/Adult drops, Challenge Tower prize chests) now
    // uses THE_PRINCE_SPAWN_EGG / THE_PRINCESS_SPAWN_EGG below.

    // ---- Spawn Eggs ----
    public static final DeferredItem<SpawnEggItem> ALIEN_SPAWN_EGG = ITEMS.register("alien_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ALIEN.get(), 0x333333, 0x00FF00, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ALIEN_BOSS_SPAWN_EGG = ITEMS.register("alien_boss_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ALIEN_BOSS.get(), 0x101010, 0xFF00FF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ALOSAURUS_SPAWN_EGG = ITEMS.register("alosaurus_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ALOSAURUS.get(), 0x8B4513, 0xCD853F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ATTACK_SQUID_SPAWN_EGG = ITEMS.register("attack_squid_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ATTACK_SQUID.get(), 0x2F4F4F, 0xFF4500, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BAND_P_SPAWN_EGG = ITEMS.register("band_p_spawn_egg",
            () -> new SpawnEggItem(ModEntities.BAND_P.get(), 0x8B0000, 0x000000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BASILISK_SPAWN_EGG = ITEMS.register("basilisk_spawn_egg",
            () -> new SpawnEggItem(ModEntities.BASILISK.get(), 0x556B2F, 0x8FBC8F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CAVE_FISHER_SPAWN_EGG = ITEMS.register("cave_fisher_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CAVE_FISHER.get(), 0x696969, 0xA9A9A9, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CLOUD_SHARK_SPAWN_EGG = ITEMS.register("cloud_shark_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CLOUD_SHARK.get(), 0xB0C4DE, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CRAB_SPAWN_EGG = ITEMS.register("crab_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CRAB.get(), 0xFF6347, 0xCD5C5C, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CREEPING_HORROR_SPAWN_EGG = ITEMS.register("creeping_horror_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CREEPING_HORROR.get(), 0x2E8B57, 0x006400, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CRYOLOPHOSAURUS_SPAWN_EGG = ITEMS.register("cryolophosaurus_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CRYOLOPHOSAURUS.get(), 0x4682B4, 0x87CEEB, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> DUNGEON_BEAST_SPAWN_EGG = ITEMS.register("dungeon_beast_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DUNGEON_BEAST.get(), 0x8B4513, 0x654321, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ENDER_KNIGHT_SPAWN_EGG = ITEMS.register("ender_knight_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENDER_KNIGHT.get(), 0x301934, 0xDA70D6, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ENDER_REAPER_SPAWN_EGG = ITEMS.register("ender_reaper_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENDER_REAPER.get(), 0x1C1C1C, 0x9400D3, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GIANT_ROBOT_SPAWN_EGG = ITEMS.register("giant_robot_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GIANT_ROBOT.get(), 0x808080, 0xC0C0C0, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> JEFFERY_SPAWN_EGG = ITEMS.register("jeffery_spawn_egg",
            () -> new SpawnEggItem(ModEntities.JEFFERY.get(), 0x808080, 0xA0A0A0, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> HAMMERHEAD_SPAWN_EGG = ITEMS.register("hammerhead_spawn_egg",
            () -> new SpawnEggItem(ModEntities.HAMMERHEAD.get(), 0x4682B4, 0x708090, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> IRUKANDJI_SPAWN_EGG = ITEMS.register("irukandji_spawn_egg",
            () -> new SpawnEggItem(ModEntities.IRUKANDJI.get(), 0x00CED1, 0xE0FFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> NASTYSAURUS_SPAWN_EGG = ITEMS.register("nastysaurus_spawn_egg",
            () -> new SpawnEggItem(ModEntities.NASTYSAURUS.get(), 0x8B4513, 0xA0522D, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> PITCH_BLACK_SPAWN_EGG = ITEMS.register("pitch_black_spawn_egg",
            () -> new SpawnEggItem(ModEntities.PITCH_BLACK.get(), 0x0A0A0A, 0x1A1A1A, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> LEONOPTERYX_SPAWN_EGG = ITEMS.register("leonopteryx_spawn_egg",
            () -> new SpawnEggItem(ModEntities.LEONOPTERYX.get(), 0xCC2222, 0xFFEE55, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> POINTYSAURUS_SPAWN_EGG = ITEMS.register("pointysaurus_spawn_egg",
            () -> new SpawnEggItem(ModEntities.POINTYSAURUS.get(), 0x8B4513, 0xDEB887, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ROBOT_1_SPAWN_EGG = ITEMS.register("robot_1_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ROBOT_1.get(), 0x708090, 0xB0C4DE, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ROBOT_2_SPAWN_EGG = ITEMS.register("robot_2_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ROBOT_2.get(), 0x708090, 0x87CEEB, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ROBOT_3_SPAWN_EGG = ITEMS.register("robot_3_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ROBOT_3.get(), 0x708090, 0xADD8E6, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ROBOT_4_SPAWN_EGG = ITEMS.register("robot_4_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ROBOT_4.get(), 0x708090, 0x778899, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ROBOT_5_SPAWN_EGG = ITEMS.register("robot_5_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ROBOT_5.get(), 0x708090, 0xD3D3D3, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SEA_MONSTER_SPAWN_EGG = ITEMS.register("sea_monster_spawn_egg",
            () -> new SpawnEggItem(ModEntities.SEA_MONSTER.get(), 0x006400, 0x228B22, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SEA_VIPER_SPAWN_EGG = ITEMS.register("sea_viper_spawn_egg",
            () -> new SpawnEggItem(ModEntities.SEA_VIPER.get(), 0x2E8B57, 0x3CB371, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SKATE_SPAWN_EGG = ITEMS.register("skate_spawn_egg",
            () -> new SpawnEggItem(ModEntities.SKATE.get(), 0x708090, 0xA9A9A9, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> TREX_SPAWN_EGG = ITEMS.register("trex_spawn_egg",
            () -> new SpawnEggItem(ModEntities.TREX.get(), 0x556B2F, 0x6B8E23, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> URCHIN_SPAWN_EGG = ITEMS.register("urchin_spawn_egg",
            () -> new SpawnEggItem(ModEntities.URCHIN.get(), 0x800080, 0xDA70D6, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GODZILLA_SPAWN_EGG = ITEMS.register("godzilla_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GODZILLA.get(), 0x2F4F4F, 0x556B2F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> KRAKEN_SPAWN_EGG = ITEMS.register("kraken_spawn_egg",
            () -> new SpawnEggItem(ModEntities.KRAKEN.get(), 0x191970, 0x4169E1, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> THE_KING_SPAWN_EGG = ITEMS.register("the_king_spawn_egg",
            () -> new SpawnEggItem(ModEntities.THE_KING.get(), 0x8B0000, 0xFFD700, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> THE_QUEEN_SPAWN_EGG = ITEMS.register("the_queen_spawn_egg",
            () -> new SpawnEggItem(ModEntities.THE_QUEEN.get(), 0x4B0082, 0xFF69B4, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BEE_SPAWN_EGG = ITEMS.register("bee_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_BEE.get(), 0xFFD700, 0x000000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BRUTALFLY_SPAWN_EGG = ITEMS.register("brutalfly_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_BRUTALFLY.get(), 0x8B4513, 0x654321, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CATER_KILLER_SPAWN_EGG = ITEMS.register("cater_killer_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_CATER_KILLER.get(), 0x228B22, 0x32CD32, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> EMPEROR_SCORPION_SPAWN_EGG = ITEMS.register("emperor_scorpion_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_EMPEROR_SCORPION.get(), 0x000000, 0x8B0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> HERCULES_BEETLE_SPAWN_EGG = ITEMS.register("hercules_beetle_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_HERCULES_BEETLE.get(), 0x654321, 0x8B4513, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> KYUUBI_SPAWN_EGG = ITEMS.register("kyuubi_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_KYUUBI.get(), 0xFF8C00, 0xFFD700, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> LEAF_MONSTER_SPAWN_EGG = ITEMS.register("leaf_monster_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_LEAF_MONSTER.get(), 0x228B22, 0x006400, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> LURKING_TERROR_SPAWN_EGG = ITEMS.register("lurking_terror_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_LURKING_TERROR.get(), 0x2F4F4F, 0x696969, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> MANTIS_SPAWN_EGG = ITEMS.register("mantis_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_MANTIS.get(), 0x00FF00, 0x006400, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> MOLENOID_SPAWN_EGG = ITEMS.register("molenoid_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_MOLENOID.get(), 0x8B4513, 0xD2B48C, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> RAT_SPAWN_EGG = ITEMS.register("rat_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_RAT.get(), 0x808080, 0xA9A9A9, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ROTATOR_SPAWN_EGG = ITEMS.register("rotator_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_ROTATOR.get(), 0x808080, 0xFF0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SCORPION_SPAWN_EGG = ITEMS.register("scorpion_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_SCORPION.get(), 0xDAA520, 0x8B4513, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SPIT_BUG_SPAWN_EGG = ITEMS.register("spit_bug_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_SPIT_BUG.get(), 0x9ACD32, 0x006400, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> TERRIBLE_TERROR_SPAWN_EGG = ITEMS.register("terrible_terror_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_TERRIBLE_TERROR.get(), 0x8B0000, 0xFF4500, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> TRIFFID_SPAWN_EGG = ITEMS.register("triffid_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_TRIFFID.get(), 0x006400, 0x8B4513, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> TROOPER_BUG_SPAWN_EGG = ITEMS.register("trooper_bug_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_TROOPER_BUG.get(), 0x556B2F, 0x8FBC8F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> VORTEX_SPAWN_EGG = ITEMS.register("vortex_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_VORTEX.get(), 0x4B0082, 0x9400D3, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> WORM_SMALL_SPAWN_EGG = ITEMS.register("worm_small_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_WORM_SMALL.get(), 0xD2B48C, 0xDEB887, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> WORM_MEDIUM_SPAWN_EGG = ITEMS.register("worm_medium_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_WORM_MEDIUM.get(), 0xCD853F, 0xDEB887, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> WORM_LARGE_SPAWN_EGG = ITEMS.register("worm_large_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_WORM_LARGE.get(), 0x8B4513, 0xCD853F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BARYONYX_SPAWN_EGG = ITEMS.register("baryonyx_spawn_egg",
            () -> new SpawnEggItem(ModEntities.BARYONYX.get(), 0x556B2F, 0x8FBC8F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BEAVER_SPAWN_EGG = ITEMS.register("beaver_spawn_egg",
            () -> new SpawnEggItem(ModEntities.BEAVER.get(), 0x8B4513, 0xA0522D, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CASSOWARY_SPAWN_EGG = ITEMS.register("cassowary_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CASSOWARY.get(), 0x000000, 0x1E90FF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CHIPMUNK_SPAWN_EGG = ITEMS.register("chipmunk_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CHIPMUNK.get(), 0xD2691E, 0xFFDEAD, new Item.Properties()));
    // orig OreSpawnMain.java:5612,5330 — CoinEgg "Spawn Coin" (D4: needed by the
    // Coin jackpot loot slot, ENT-A-098, and the EasterBunny egg table, ENT-D-010)
    public static final DeferredItem<SpawnEggItem> COIN_SPAWN_EGG = ITEMS.register("coin_spawn_egg",
            () -> new SpawnEggItem(ModEntities.COIN.get(), 0xFFD700, 0xB8860B, new Item.Properties()));
    // orig OreSpawnMain.java:5577,5295 — TshirtEgg "Spawn T-Shirt!" (D4: needed by
    // the EasterBunny egg table, ENT-D-010)
    public static final DeferredItem<SpawnEggItem> TSHIRT_SPAWN_EGG = ITEMS.register("tshirt_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_TSHIRT.get(), 0xFFFFFF, 0xFF4040, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> COCKATEIL_SPAWN_EGG = ITEMS.register("cockateil_spawn_egg",
            () -> new SpawnEggItem(ModEntities.COCKATEIL.get(), 0xFFFF00, 0xFF8C00, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> EASTER_BUNNY_SPAWN_EGG = ITEMS.register("easter_bunny_spawn_egg",
            () -> new SpawnEggItem(ModEntities.EASTER_BUNNY.get(), 0xFFB6C1, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> FLOUNDER_SPAWN_EGG = ITEMS.register("flounder_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FLOUNDER.get(), 0xDAA520, 0x8B4513, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> FROG_SPAWN_EGG = ITEMS.register("frog_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FROG.get(), 0x228B22, 0x32CD32, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GAZELLE_SPAWN_EGG = ITEMS.register("gazelle_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GAZELLE.get(), 0xD2B48C, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GOLD_FISH_SPAWN_EGG = ITEMS.register("gold_fish_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GOLD_FISH.get(), 0xFFD700, 0xFF8C00, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> PEACOCK_SPAWN_EGG = ITEMS.register("peacock_spawn_egg",
            () -> new SpawnEggItem(ModEntities.PEACOCK.get(), 0x006400, 0x00CED1, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> WHALE_SPAWN_EGG = ITEMS.register("whale_spawn_egg",
            () -> new SpawnEggItem(ModEntities.WHALE.get(), 0x4682B4, 0x87CEEB, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ANT_SPAWN_EGG = ITEMS.register("ant_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_ANT.get(), 0x000000, 0x8B0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CLIFF_RACER_SPAWN_EGG = ITEMS.register("cliff_racer_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_CLIFF_RACER.get(), 0x8B4513, 0xDAA520, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CRICKET_SPAWN_EGG = ITEMS.register("cricket_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_CRICKET.get(), 0x006400, 0x228B22, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> DRAGONFLY_SPAWN_EGG = ITEMS.register("dragonfly_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_DRAGONFLY.get(), 0x00CED1, 0x00FFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> RED_ANT_SPAWN_EGG = ITEMS.register("red_ant_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_RED_ANT.get(), 0x8B0000, 0xFF0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> RAINBOW_ANT_SPAWN_EGG = ITEMS.register("rainbow_ant_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_RAINBOW_ANT.get(), 0xFF0000, 0x00FF00, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> STINK_BUG_SPAWN_EGG = ITEMS.register("stink_bug_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_STINK_BUG.get(), 0x006400, 0x556B2F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> TERMITE_SPAWN_EGG = ITEMS.register("termite_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_TERMITE.get(), 0xD2B48C, 0xF5DEB3, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> UNSTABLE_ANT_SPAWN_EGG = ITEMS.register("unstable_ant_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_UNSTABLE_ANT.get(), 0x9400D3, 0xFF00FF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BOYFRIEND_SPAWN_EGG = ITEMS.register("boyfriend_spawn_egg",
            () -> new SpawnEggItem(ModEntities.BOYFRIEND.get(), 0x4169E1, 0xFFDEAD, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CAMARASAURUS_SPAWN_EGG = ITEMS.register("camarasaurus_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CAMARASAURUS.get(), 0x808080, 0x696969, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> DRAGON_SPAWN_EGG = ITEMS.register("dragon_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DRAGON.get(), 0x8B0000, 0xFF4500, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BABY_DRAGON_SPAWN_EGG = ITEMS.register("baby_dragon_spawn_egg",
            () -> new SpawnEggItem(ModEntities.BABY_DRAGON.get(), 0xFF6347, 0xFFD700, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GAMMA_METROID_SPAWN_EGG = ITEMS.register("gamma_metroid_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_GAMMA_METROID.get(), 0x00FF00, 0xADFF2F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GIRLFRIEND_SPAWN_EGG = ITEMS.register("girlfriend_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GIRLFRIEND.get(), 0xFF69B4, 0xFFDEAD, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> HYDROLISC_SPAWN_EGG = ITEMS.register("hydrolisc_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_HYDROLISC.get(), 0x4682B4, 0x00CED1, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> LEON_SPAWN_EGG = ITEMS.register("leon_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_LEON.get(), 0xDAA520, 0x8B4513, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> LIZARD_SPAWN_EGG = ITEMS.register("lizard_spawn_egg",
            () -> new SpawnEggItem(ModEntities.LIZARD.get(), 0x228B22, 0x006400, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> OSTRICH_SPAWN_EGG = ITEMS.register("ostrich_spawn_egg",
            () -> new SpawnEggItem(ModEntities.OSTRICH.get(), 0x000000, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> RUBBER_DUCKY_SPAWN_EGG = ITEMS.register("rubber_ducky_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_RUBBER_DUCKY.get(), 0xFFFF00, 0xFF8C00, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SPYRO_SPAWN_EGG = ITEMS.register("spyro_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_SPYRO.get(), 0x800080, 0xFFD700, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> STINKY_SPAWN_EGG = ITEMS.register("stinky_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_STINKY.get(), 0x654321, 0x8B4513, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> THE_PRINCE_SPAWN_EGG = ITEMS.register("the_prince_spawn_egg",
            () -> new SpawnEggItem(ModEntities.THE_PRINCE.get(), 0xFFD700, 0x8B0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> THE_PRINCE_ADULT_SPAWN_EGG = ITEMS.register("the_prince_adult_spawn_egg",
            () -> new SpawnEggItem(ModEntities.THE_PRINCE_ADULT.get(), 0xFFD700, 0x8B0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> THE_PRINCESS_SPAWN_EGG = ITEMS.register("the_princess_spawn_egg",
            () -> new SpawnEggItem(ModEntities.THE_PRINCESS.get(), 0xFF69B4, 0xFFD700, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> THE_PRINCE_TEEN_SPAWN_EGG = ITEMS.register("the_prince_teen_spawn_egg",
            () -> new SpawnEggItem(ModEntities.THE_PRINCE_TEEN.get(), 0xFFD700, 0xCD853F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> VELOCITY_RAPTOR_SPAWN_EGG = ITEMS.register("velocity_raptor_spawn_egg",
            () -> new SpawnEggItem(ModEntities.VELOCITY_RAPTOR.get(), 0x556B2F, 0x8FBC8F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> WATER_DRAGON_SPAWN_EGG = ITEMS.register("water_dragon_spawn_egg",
            () -> new SpawnEggItem(ModEntities.WATER_DRAGON.get(), 0x4682B4, 0x00CED1, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_BUTTERFLY.get(), 0xFF69B4, 0xFFFF00, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> LUNA_MOTH_SPAWN_EGG = ITEMS.register("luna_moth_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_LUNA_MOTH.get(), 0x90EE90, 0x00FF7F, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> MOSQUITO_SPAWN_EGG = ITEMS.register("mosquito_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENTITY_MOSQUITO.get(), 0x696969, 0xA9A9A9, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> FAIRY_SPAWN_EGG = ITEMS.register("fairy_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FAIRY.get(), 0xFF69B4, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> FIREFLY_SPAWN_EGG = ITEMS.register("firefly_spawn_egg",
            () -> new SpawnEggItem(ModEntities.FIREFLY.get(), 0xFFFF00, 0x9ACD32, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GHOST_SPAWN_EGG = ITEMS.register("ghost_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GHOST.get(), 0xC0C0C0, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GHOST_SKELLY_SPAWN_EGG = ITEMS.register("ghost_skelly_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GHOST_SKELLY.get(), 0xC0C0C0, 0xD3D3D3, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> MOTHRA_SPAWN_EGG = ITEMS.register("mothra_spawn_egg",
            () -> new SpawnEggItem(ModEntities.MOTHRA.get(), 0x8B4513, 0xDAA520, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ANT_ROBOT_SPAWN_EGG = ITEMS.register("ant_robot_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ANT_ROBOT.get(), 0x808080, 0xFF0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SPIDER_ROBOT_SPAWN_EGG = ITEMS.register("spider_robot_spawn_egg",
            () -> new SpawnEggItem(ModEntities.SPIDER_ROBOT.get(), 0x696969, 0xFF0000, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CEPHADROME_SPAWN_EGG = ITEMS.register("cephadrome_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CEPHADROME.get(), 0x4682B4, 0xB0C4DE, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> RED_COW_SPAWN_EGG = ITEMS.register("red_cow_spawn_egg",
            () -> new SpawnEggItem(ModEntities.RED_COW.get(), 0x8B0000, 0xFFFFFF, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> SPIDER_DRIVER_SPAWN_EGG = ITEMS.register("spider_driver_spawn_egg",
            () -> new SpawnEggItem(ModEntities.SPIDER_DRIVER.get(), 0x2F4F4F, 0x696969, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> CRYSTAL_COW_SPAWN_EGG = ITEMS.register("crystal_cow_spawn_egg",
            () -> new SpawnEggItem(ModEntities.CRYSTAL_COW.get(), 0xFF69B4, 0xFFB6C1, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> GOLD_COW_SPAWN_EGG = ITEMS.register("gold_cow_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GOLD_COW.get(), 0xFFD700, 0xFFA500, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> RUBY_BIRD_SPAWN_EGG = ITEMS.register("ruby_bird_spawn_egg",
            () -> new SpawnEggItem(ModEntities.RUBY_BIRD.get(), 0xDC143C, 0xFF6347, new Item.Properties()));

    // Phase 14 â€” wiki-canon mob completion eggs.
    // Apple Cow uses red+white (apple red on snow white) so it reads as
    // an "apple variant" of the standard cow at a glance in the egg row.
    public static final DeferredItem<SpawnEggItem> APPLE_COW_SPAWN_EGG = ITEMS.register("apple_cow_spawn_egg",
            () -> new SpawnEggItem(ModEntities.APPLE_COW.get(), 0xC83232, 0xFFFFFF, new Item.Properties()));
    // Golden Apple Cow gets the gold-on-cream palette to mirror the
    // golden apple sprite. Foil flag matches the in-world golden_apple
    // shimmer so players intuit the rarity from the inventory hover.
    public static final DeferredItem<SpawnEggItem> GOLDEN_APPLE_COW_SPAWN_EGG = ITEMS.register("golden_apple_cow_spawn_egg",
            () -> new SpawnEggItem(ModEntities.GOLDEN_APPLE_COW.get(), 0xFFD700, 0xFFF8DC, new Item.Properties()) {
                @Override
                public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
                    return true;
                }
            });
    // Vampire Butterfly uses the deep-red / bat-purple palette so it
    // reads as the hostile cousin of the rainbow ambient butterfly egg.
    public static final DeferredItem<SpawnEggItem> VAMPIRE_BUTTERFLY_SPAWN_EGG = ITEMS.register("vampire_butterfly_spawn_egg",
            () -> new SpawnEggItem(ModEntities.VAMPIRE_BUTTERFLY.get(), 0x8B0000, 0x4B0082, new Item.Properties()));
    // Enchanted Apple Cow caps the apple-cow ladder; foil flag matches
    // the entity's in-world glint layer so the spawn egg in inventory
    // signals "rare/enchanted tier" the same way the mob does in-world.
    public static final DeferredItem<SpawnEggItem> ENCHANTED_APPLE_COW_SPAWN_EGG = ITEMS.register("enchanted_apple_cow_spawn_egg",
            () -> new SpawnEggItem(ModEntities.ENCHANTED_APPLE_COW.get(), 0xFFD700, 0xC83232, new Item.Properties()) {
                @Override
                public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
                    return true;
                }
            });

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
