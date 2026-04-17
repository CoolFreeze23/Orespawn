package danger.orespawn;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS_TAB =
            CREATIVE_MODE_TABS.register("orespawn_blocks", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.orespawn.blocks"))
                    .icon(() -> new ItemStack(ModItems.ORE_RUBY_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ORE_RUBY_ITEM.get());
                        output.accept(ModItems.ORE_AMETHYST_ITEM.get());
                        output.accept(ModItems.ORE_URANIUM_ITEM.get());
                        output.accept(ModItems.ORE_TITANIUM_ITEM.get());
                        output.accept(ModItems.ORE_SALT_ITEM.get());
                        output.accept(ModItems.TIGERS_EYE_ORE_ITEM.get());

                        output.accept(ModItems.BLOCK_RUBY_ITEM.get());
                        output.accept(ModItems.BLOCK_AMETHYST_ITEM.get());
                        output.accept(ModItems.BLOCK_URANIUM_ITEM.get());
                        output.accept(ModItems.BLOCK_TITANIUM_ITEM.get());
                        output.accept(ModItems.BLOCK_MOBZILLA_SCALE_ITEM.get());
                        output.accept(ModItems.BLOCK_CRYSTAL_PINK_ITEM.get());
                        output.accept(ModItems.BLOCK_TIGERS_EYE_ITEM.get());
                        output.accept(ModItems.BLOCK_ENDER_PEARL_ITEM.get());
                        output.accept(ModItems.BLOCK_EYE_OF_ENDER_ITEM.get());

                        output.accept(ModItems.CRYSTAL_STONE_ITEM.get());
                        output.accept(ModItems.CRYSTAL_COAL_ITEM.get());
                        output.accept(ModItems.CRYSTAL_GRASS_ITEM.get());
                        output.accept(ModItems.CRYSTAL_CRYSTAL_ITEM.get());
                        output.accept(ModItems.CRYSTAL_PLANKS_ITEM.get());
                        output.accept(ModItems.CRYSTAL_WORKBENCH_ITEM.get());
                        output.accept(ModItems.CRYSTAL_FURNACE_ITEM.get());
                        output.accept(ModItems.SKY_TREE_LOG_ITEM.get());
                        output.accept(ModItems.DUPLICATOR_LOG_ITEM.get());
                        output.accept(ModItems.CRYSTAL_TREE_LOG_ITEM.get());

                        output.accept(ModItems.APPLE_LEAVES_ITEM.get());
                        output.accept(ModItems.EXPERIENCE_LEAVES_ITEM.get());
                        output.accept(ModItems.SCARY_LEAVES_ITEM.get());
                        output.accept(ModItems.CHERRY_LEAVES_ITEM.get());
                        output.accept(ModItems.PEACH_LEAVES_ITEM.get());
                        output.accept(ModItems.CRYSTAL_LEAVES_ITEM.get());
                        output.accept(ModItems.CRYSTAL_LEAVES_2_ITEM.get());
                        output.accept(ModItems.CRYSTAL_LEAVES_3_ITEM.get());

                        output.accept(ModItems.EXPERIENCE_SAPLING_ITEM.get());
                        output.accept(ModItems.CRYSTAL_SAPLING_ITEM.get());
                        output.accept(ModItems.CRYSTAL_SAPLING_2_ITEM.get());
                        output.accept(ModItems.CRYSTAL_SAPLING_3_ITEM.get());

                        output.accept(ModItems.FLOWER_PINK_ITEM.get());
                        output.accept(ModItems.FLOWER_BLUE_ITEM.get());
                        output.accept(ModItems.FLOWER_BLACK_ITEM.get());
                        output.accept(ModItems.FLOWER_SCARY_ITEM.get());
                        output.accept(ModItems.CRYSTAL_FLOWER_RED_ITEM.get());
                        output.accept(ModItems.CRYSTAL_FLOWER_GREEN_ITEM.get());
                        output.accept(ModItems.CRYSTAL_FLOWER_BLUE_ITEM.get());
                        output.accept(ModItems.CRYSTAL_FLOWER_YELLOW_ITEM.get());

                        output.accept(ModItems.ANT_BLOCK_ITEM.get());
                        output.accept(ModItems.RED_ANT_BLOCK_ITEM.get());
                        output.accept(ModItems.TERMITE_BLOCK_ITEM.get());
                        output.accept(ModItems.CRYSTAL_TERMITE_BLOCK_ITEM.get());
                        output.accept(ModItems.RAINBOW_ANT_BLOCK_ITEM.get());
                        output.accept(ModItems.UNSTABLE_ANT_BLOCK_ITEM.get());

                        output.accept(ModItems.LAVAFOAM_ITEM.get());
                        output.accept(ModItems.PIZZA_BLOCK_ITEM.get());
                        output.accept(ModItems.DUCT_TAPE_BLOCK_ITEM.get());
                        output.accept(ModItems.BLOCK_TELEPORT_ITEM.get());
                        output.accept(ModItems.MOLE_DIRT_ITEM.get());
                        output.accept(ModItems.ISLAND_ITEM.get());
                        output.accept(ModItems.CRYSTAL_RAT_ITEM.get());
                        output.accept(ModItems.CRYSTAL_FAIRY_ITEM.get());
                        output.accept(ModItems.RED_ANT_TROLL_ITEM.get());
                        output.accept(ModItems.TERMITE_TROLL_ITEM.get());

                        output.accept(ModItems.EXTREME_TORCH_ITEM.get());
                        output.accept(ModItems.CRYSTAL_TORCH_ITEM.get());
                        output.accept(ModItems.KRAKEN_REPELLENT_ITEM.get());
                        output.accept(ModItems.CREEPER_REPELLENT_ITEM.get());
                        output.accept(ModItems.KRAKEN_SPAWN_BLOCK_ITEM.get());
                        output.accept(ModItems.DRAGON_SPAWN_BLOCK_ITEM.get());
                        output.accept(ModItems.KING_SPAWNER_ITEM.get());
                        output.accept(ModItems.QUEEN_SPAWNER_ITEM.get());
                        output.accept(ModItems.DUNGEON_SPAWNER_ITEM.get());
                        output.accept(ModItems.UTOPIA_PORTAL_ITEM.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT_TAB =
            CREATIVE_MODE_TABS.register("orespawn_equipment", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.orespawn.equipment"))
                    .icon(() -> new ItemStack(ModItems.ULTIMATE_SWORD.get()))
                    .displayItems((parameters, output) -> {
                        // Ultimate
                        output.accept(ModItems.ULTIMATE_SWORD.get());
                        output.accept(ModItems.ULTIMATE_PICKAXE.get());
                        output.accept(ModItems.ULTIMATE_SHOVEL.get());
                        output.accept(ModItems.ULTIMATE_HOE.get());
                        output.accept(ModItems.ULTIMATE_AXE.get());
                        // Ruby
                        output.accept(ModItems.RUBY_SWORD.get());
                        output.accept(ModItems.RUBY_PICKAXE.get());
                        output.accept(ModItems.RUBY_SHOVEL.get());
                        output.accept(ModItems.RUBY_HOE.get());
                        output.accept(ModItems.RUBY_AXE.get());
                        // Amethyst
                        output.accept(ModItems.AMETHYST_SWORD.get());
                        output.accept(ModItems.AMETHYST_PICKAXE.get());
                        output.accept(ModItems.AMETHYST_SHOVEL.get());
                        output.accept(ModItems.AMETHYST_HOE.get());
                        output.accept(ModItems.AMETHYST_AXE.get());
                        // Emerald
                        output.accept(ModItems.EMERALD_SWORD.get());
                        output.accept(ModItems.EMERALD_PICKAXE.get());
                        output.accept(ModItems.EMERALD_SHOVEL.get());
                        output.accept(ModItems.EMERALD_HOE.get());
                        output.accept(ModItems.EMERALD_AXE.get());
                        // Crystal Wood
                        output.accept(ModItems.CRYSTAL_WOOD_SWORD.get());
                        output.accept(ModItems.CRYSTAL_WOOD_PICKAXE.get());
                        output.accept(ModItems.CRYSTAL_WOOD_SHOVEL.get());
                        output.accept(ModItems.CRYSTAL_WOOD_HOE.get());
                        output.accept(ModItems.CRYSTAL_WOOD_AXE.get());
                        // Crystal Stone
                        output.accept(ModItems.CRYSTAL_STONE_SWORD.get());
                        output.accept(ModItems.CRYSTAL_STONE_PICKAXE.get());
                        output.accept(ModItems.CRYSTAL_STONE_SHOVEL.get());
                        output.accept(ModItems.CRYSTAL_STONE_HOE.get());
                        output.accept(ModItems.CRYSTAL_STONE_AXE.get());
                        // Crystal Pink
                        output.accept(ModItems.CRYSTAL_PINK_SWORD.get());
                        output.accept(ModItems.CRYSTAL_PINK_PICKAXE.get());
                        output.accept(ModItems.CRYSTAL_PINK_SHOVEL.get());
                        output.accept(ModItems.CRYSTAL_PINK_HOE.get());
                        output.accept(ModItems.CRYSTAL_PINK_AXE.get());
                        // Tigers Eye
                        output.accept(ModItems.TIGERS_EYE_SWORD.get());
                        output.accept(ModItems.TIGERS_EYE_PICKAXE.get());
                        output.accept(ModItems.TIGERS_EYE_SHOVEL.get());
                        output.accept(ModItems.TIGERS_EYE_HOE.get());
                        output.accept(ModItems.TIGERS_EYE_AXE.get());
                        // Special weapons
                        output.accept(ModItems.BIG_BERTHA.get());
                        output.accept(ModItems.ROYAL_GUARDIAN_SWORD.get());
                        output.accept(ModItems.QUEEN_BATTLE_AXE.get());
                        output.accept(ModItems.NIGHTMARE_SWORD.get());
                        output.accept(ModItems.BATTLE_AXE.get());
                        output.accept(ModItems.CHAINSAW.get());
                        output.accept(ModItems.ATTITUDE_ADJUSTER.get());
                        output.accept(ModItems.SLICE.get());
                        output.accept(ModItems.EXPERIENCE_SWORD.get());
                        output.accept(ModItems.POISON_SWORD.get());
                        output.accept(ModItems.RAT_SWORD.get());
                        output.accept(ModItems.FAIRY_SWORD.get());
                        output.accept(ModItems.MANTIS_CLAW.get());
                        output.accept(ModItems.BIG_HAMMER.get());
                        output.accept(ModItems.ROSE_SWORD.get());
                        // Ranged
                        output.accept(ModItems.ULTIMATE_BOW.get());
                        output.accept(ModItems.SKATE_BOW.get());
                        output.accept(ModItems.ULTIMATE_FISHING_ROD.get());
                        output.accept(ModItems.RAY_GUN.get());
                        output.accept(ModItems.THUNDER_STAFF.get());
                        output.accept(ModItems.SQUID_ZOOKA.get());
                        output.accept(ModItems.CREEPER_LAUNCHER.get());
                        output.accept(ModItems.WRENCH.get());
                        // Throwables
                        output.accept(ModItems.WATER_BALL.get());
                        output.accept(ModItems.LASER_BALL.get());
                        output.accept(ModItems.ICE_BALL.get());
                        output.accept(ModItems.ACID.get());
                        output.accept(ModItems.DEAD_IRUKANDJI.get());
                        output.accept(ModItems.IRUKANDJI_ARROW.get());
                        output.accept(ModItems.ROCK_SMALL.get());
                        output.accept(ModItems.ROCK.get());
                        output.accept(ModItems.ROCK_RED.get());
                        output.accept(ModItems.ROCK_GREEN.get());
                        output.accept(ModItems.ROCK_BLUE.get());
                        output.accept(ModItems.ROCK_PURPLE.get());
                        output.accept(ModItems.ROCK_SPIKEY.get());
                        output.accept(ModItems.ROCK_TNT.get());
                        output.accept(ModItems.ROCK_CRYSTAL_RED.get());
                        output.accept(ModItems.ROCK_CRYSTAL_GREEN.get());
                        output.accept(ModItems.ROCK_CRYSTAL_BLUE.get());
                        output.accept(ModItems.ROCK_CRYSTAL_TNT.get());
                        // Armor - Ultimate
                        output.accept(ModItems.ULTIMATE_HELMET.get());
                        output.accept(ModItems.ULTIMATE_CHESTPLATE.get());
                        output.accept(ModItems.ULTIMATE_LEGGINGS.get());
                        output.accept(ModItems.ULTIMATE_BOOTS_ARMOR.get());
                        // Armor - Royal
                        output.accept(ModItems.ROYAL_HELMET.get());
                        output.accept(ModItems.ROYAL_CHESTPLATE.get());
                        output.accept(ModItems.ROYAL_LEGGINGS.get());
                        output.accept(ModItems.ROYAL_BOOTS.get());
                        // Armor - Queen
                        output.accept(ModItems.QUEEN_HELMET.get());
                        output.accept(ModItems.QUEEN_CHESTPLATE.get());
                        output.accept(ModItems.QUEEN_LEGGINGS.get());
                        output.accept(ModItems.QUEEN_BOOTS.get());
                        // Armor - Mobzilla
                        output.accept(ModItems.MOBZILLA_HELMET.get());
                        output.accept(ModItems.MOBZILLA_CHESTPLATE.get());
                        output.accept(ModItems.MOBZILLA_LEGGINGS.get());
                        output.accept(ModItems.MOBZILLA_BOOTS.get());
                        // Armor - Experience
                        output.accept(ModItems.EXPERIENCE_HELMET.get());
                        output.accept(ModItems.EXPERIENCE_CHESTPLATE.get());
                        output.accept(ModItems.EXPERIENCE_LEGGINGS.get());
                        output.accept(ModItems.EXPERIENCE_BOOTS.get());
                        // Armor - Ruby
                        output.accept(ModItems.RUBY_HELMET.get());
                        output.accept(ModItems.RUBY_CHESTPLATE.get());
                        output.accept(ModItems.RUBY_LEGGINGS.get());
                        output.accept(ModItems.RUBY_BOOTS_ARMOR.get());
                        // Armor - Amethyst
                        output.accept(ModItems.AMETHYST_HELMET.get());
                        output.accept(ModItems.AMETHYST_CHESTPLATE.get());
                        output.accept(ModItems.AMETHYST_LEGGINGS.get());
                        output.accept(ModItems.AMETHYST_BOOTS_ARMOR.get());
                        // Armor - Emerald
                        output.accept(ModItems.EMERALD_HELMET.get());
                        output.accept(ModItems.EMERALD_CHESTPLATE.get());
                        output.accept(ModItems.EMERALD_LEGGINGS.get());
                        output.accept(ModItems.EMERALD_BOOTS_ARMOR.get());
                        // Armor - Lava Eel
                        output.accept(ModItems.LAVAEEL_HELMET.get());
                        output.accept(ModItems.LAVAEEL_CHESTPLATE.get());
                        output.accept(ModItems.LAVAEEL_LEGGINGS.get());
                        output.accept(ModItems.LAVAEEL_BOOTS.get());
                        // Armor - Moth Scale
                        output.accept(ModItems.MOTHSCALE_HELMET.get());
                        output.accept(ModItems.MOTHSCALE_CHESTPLATE.get());
                        output.accept(ModItems.MOTHSCALE_LEGGINGS.get());
                        output.accept(ModItems.MOTHSCALE_BOOTS.get());
                        // Armor - Peacock
                        output.accept(ModItems.PEACOCK_HELMET.get());
                        output.accept(ModItems.PEACOCK_CHESTPLATE.get());
                        output.accept(ModItems.PEACOCK_LEGGINGS.get());
                        output.accept(ModItems.PEACOCK_BOOTS.get());
                        // Armor - Crystal Pink
                        output.accept(ModItems.PINK_HELMET.get());
                        output.accept(ModItems.PINK_CHESTPLATE.get());
                        output.accept(ModItems.PINK_LEGGINGS.get());
                        output.accept(ModItems.PINK_BOOTS.get());
                        // Armor - Tigers Eye
                        output.accept(ModItems.TIGERSEYE_HELMET.get());
                        output.accept(ModItems.TIGERSEYE_CHESTPLATE.get());
                        output.accept(ModItems.TIGERSEYE_LEGGINGS.get());
                        output.accept(ModItems.TIGERSEYE_BOOTS.get());
                        // Armor - Lapis
                        output.accept(ModItems.LAPIS_HELMET.get());
                        output.accept(ModItems.LAPIS_CHESTPLATE.get());
                        output.accept(ModItems.LAPIS_LEGGINGS.get());
                        output.accept(ModItems.LAPIS_BOOTS.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS_TAB =
            CREATIVE_MODE_TABS.register("orespawn_items", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.orespawn.items"))
                    .icon(() -> new ItemStack(ModItems.RUBY.get()))
                    .displayItems((parameters, output) -> {
                        // Gems & Ingots
                        output.accept(ModItems.RUBY.get());
                        output.accept(ModItems.AMETHYST_GEM.get());
                        output.accept(ModItems.INGOT_URANIUM.get());
                        output.accept(ModItems.INGOT_TITANIUM.get());
                        output.accept(ModItems.URANIUM_NUGGET.get());
                        output.accept(ModItems.TITANIUM_NUGGET.get());
                        output.accept(ModItems.CRYSTAL_PINK_INGOT.get());
                        output.accept(ModItems.TIGERS_EYE_INGOT.get());
                        output.accept(ModItems.SALT.get());
                        output.accept(ModItems.CRYSTAL_STICKS.get());
                        output.accept(ModItems.GREEN_GOO.get());
                        // Mob Drops
                        output.accept(ModItems.MOTH_SCALE.get());
                        output.accept(ModItems.QUEEN_SCALE.get());
                        output.accept(ModItems.NIGHTMARE_SCALE.get());
                        output.accept(ModItems.EMPEROR_SCORPION_SCALE.get());
                        output.accept(ModItems.BASILISK_SCALE.get());
                        output.accept(ModItems.WATER_DRAGON_SCALE.get());
                        output.accept(ModItems.PEACOCK_FEATHER.get());
                        output.accept(ModItems.JUMPY_BUG_SCALE.get());
                        output.accept(ModItems.KRAKEN_TOOTH.get());
                        output.accept(ModItems.GODZILLA_SCALE.get());
                        output.accept(ModItems.BERTHA_HANDLE.get());
                        output.accept(ModItems.BERTHA_GUARD.get());
                        output.accept(ModItems.BERTHA_BLADE.get());
                        output.accept(ModItems.MOLENOID_NOSE.get());
                        output.accept(ModItems.SEA_MONSTER_SCALE.get());
                        output.accept(ModItems.WORM_TOOTH.get());
                        output.accept(ModItems.TREX_TOOTH.get());
                        output.accept(ModItems.CATERKILLER_JAW.get());
                        output.accept(ModItems.SEA_VIPER_TONGUE.get());
                        output.accept(ModItems.VORTEX_EYE_ITEM.get());
                        output.accept(ModItems.DEAD_STINK_BUG.get());
                        // Food
                        output.accept(ModItems.PIZZA_ITEM.get());
                        output.accept(ModItems.CORN_DOG.get());
                        output.accept(ModItems.RAW_CORN_DOG.get());
                        output.accept(ModItems.COOKED_BACON.get());
                        output.accept(ModItems.RAW_BACON.get());
                        output.accept(ModItems.COOKED_CRAB_MEAT.get());
                        output.accept(ModItems.RAW_CRAB_MEAT.get());
                        output.accept(ModItems.COOKED_PEACOCK.get());
                        output.accept(ModItems.RAW_PEACOCK.get());
                        output.accept(ModItems.SALAD.get());
                        output.accept(ModItems.BLT_SANDWICH.get());
                        output.accept(ModItems.CRABBY_PATTY.get());
                        output.accept(ModItems.CHEESE.get());
                        output.accept(ModItems.BUTTER.get());
                        output.accept(ModItems.BUTTER_CANDY.get());
                        output.accept(ModItems.POPCORN.get());
                        output.accept(ModItems.BUTTERED_POPCORN.get());
                        output.accept(ModItems.BUTTERED_SALTED_POPCORN.get());
                        output.accept(ModItems.POPCORN_BAG.get());
                        output.accept(ModItems.HEART.get());
                        output.accept(ModItems.CRYSTAL_APPLE.get());
                        output.accept(ModItems.STRAWBERRY.get());
                        output.accept(ModItems.CHERRIES.get());
                        output.accept(ModItems.PEACH.get());
                        output.accept(ModItems.RADISH.get());
                        output.accept(ModItems.RICE.get());
                        output.accept(ModItems.CORN_SEED.get());
                        output.accept(ModItems.QUINOA.get());
                        output.accept(ModItems.TOMATO.get());
                        output.accept(ModItems.LETTUCE.get());
                        // Fish
                        output.accept(ModItems.FIRE_FISH.get());
                        output.accept(ModItems.SUN_FISH.get());
                        output.accept(ModItems.LAVA_EEL.get());
                        output.accept(ModItems.SPARK_FISH.get());
                        output.accept(ModItems.GREEN_FISH.get());
                        output.accept(ModItems.BLUE_FISH.get());
                        output.accept(ModItems.PINK_FISH.get());
                        output.accept(ModItems.ROCK_FISH.get());
                        output.accept(ModItems.WOOD_FISH.get());
                        output.accept(ModItems.GREY_FISH.get());
                        // Seeds & Saplings
                        output.accept(ModItems.STRAWBERRY_SEED.get());
                        output.accept(ModItems.BUTTERFLY_SEED.get());
                        output.accept(ModItems.MOTH_SEED.get());
                        output.accept(ModItems.MOSQUITO_SEED.get());
                        output.accept(ModItems.FIREFLY_SEED.get());
                        output.accept(ModItems.APPLE_TREE_SEED.get());
                        output.accept(ModItems.CHERRY_TREE_SEED.get());
                        output.accept(ModItems.PEACH_TREE_SEED.get());
                        output.accept(ModItems.EXPERIENCE_TREE_SEED.get());
                        output.accept(ModItems.RADISH_SEED.get());
                        output.accept(ModItems.RICE_SEED.get());
                        output.accept(ModItems.QUINOA_SEED.get());
                        output.accept(ModItems.TOMATO_SEED.get());
                        output.accept(ModItems.LETTUCE_SEED.get());
                        // Utility
                        output.accept(ModItems.MAGIC_APPLE.get());
                        output.accept(ModItems.MINERS_DREAM.get());
                        output.accept(ModItems.RANDOM_DUNGEON.get());
                        output.accept(ModItems.EXPERIENCE_CATCHER.get());
                        output.accept(ModItems.SUNSPOT_URCHIN.get());
                        output.accept(ModItems.SIFTER.get());
                        output.accept(ModItems.SPIDER_ROBOT_KIT.get());
                        output.accept(ModItems.ANT_ROBOT_KIT.get());
                        output.accept(ModItems.ZOO_KEEPER.get());
                        output.accept(ModItems.NETHER_LOST.get());
                        output.accept(ModItems.ELEVATOR.get());
                        output.accept(ModItems.INSTANT_SHELTER.get());
                        output.accept(ModItems.INSTANT_GARDEN.get());
                        output.accept(ModItems.DUCT_TAPE_ITEM.get());
                        output.accept(ModItems.STEP_UP.get());
                        output.accept(ModItems.STEP_DOWN.get());
                        output.accept(ModItems.STEP_ACROSS.get());
                        output.accept(ModItems.PRINCE_EGG.get());
                        // Shoes & Accessories
                        output.accept(ModItems.RED_HEELS.get());
                        output.accept(ModItems.BLACK_HEELS.get());
                        output.accept(ModItems.SLIPPERS.get());
                        output.accept(ModItems.BOOTS_SHOES.get());
                        output.accept(ModItems.GAME_CONTROLLER.get());
                        // Zoo Cages
                        output.accept(ModItems.CAGE_EMPTY.get());
                        output.accept(ModItems.CAGED_MOB.get());
                        output.accept(ModItems.ZOO_CAGE_2.get());
                        output.accept(ModItems.ZOO_CAGE_4.get());
                        output.accept(ModItems.ZOO_CAGE_6.get());
                        output.accept(ModItems.ZOO_CAGE_8.get());
                        output.accept(ModItems.ZOO_CAGE_10.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPAWN_EGGS_TAB =
            CREATIVE_MODE_TABS.register("orespawn_spawn_eggs", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.orespawn.spawn_eggs"))
                    .icon(() -> new ItemStack(ModItems.GODZILLA_SPAWN_EGG.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ALIEN_SPAWN_EGG.get());
                        output.accept(ModItems.ALOSAURUS_SPAWN_EGG.get());
                        output.accept(ModItems.ANT_SPAWN_EGG.get());
                        output.accept(ModItems.ANT_ROBOT_SPAWN_EGG.get());
                        output.accept(ModItems.ATTACK_SQUID_SPAWN_EGG.get());
                        output.accept(ModItems.BAND_P_SPAWN_EGG.get());
                        output.accept(ModItems.BARYONYX_SPAWN_EGG.get());
                        output.accept(ModItems.BASILISK_SPAWN_EGG.get());
                        output.accept(ModItems.BEAVER_SPAWN_EGG.get());
                        output.accept(ModItems.BEE_SPAWN_EGG.get());
                        output.accept(ModItems.BOYFRIEND_SPAWN_EGG.get());
                        output.accept(ModItems.BRUTALFLY_SPAWN_EGG.get());
                        output.accept(ModItems.BUTTERFLY_SPAWN_EGG.get());
                        output.accept(ModItems.CAMARASAURUS_SPAWN_EGG.get());
                        output.accept(ModItems.CASSOWARY_SPAWN_EGG.get());
                        output.accept(ModItems.CATER_KILLER_SPAWN_EGG.get());
                        output.accept(ModItems.CAVE_FISHER_SPAWN_EGG.get());
                        output.accept(ModItems.CEPHADROME_SPAWN_EGG.get());
                        output.accept(ModItems.CHIPMUNK_SPAWN_EGG.get());
                        output.accept(ModItems.CLIFF_RACER_SPAWN_EGG.get());
                        output.accept(ModItems.CLOUD_SHARK_SPAWN_EGG.get());
                        output.accept(ModItems.COCKATEIL_SPAWN_EGG.get());
                        output.accept(ModItems.CRAB_SPAWN_EGG.get());
                        output.accept(ModItems.CREEPING_HORROR_SPAWN_EGG.get());
                        output.accept(ModItems.CRICKET_SPAWN_EGG.get());
                        output.accept(ModItems.CRYOLOPHOSAURUS_SPAWN_EGG.get());
                        output.accept(ModItems.DRAGON_SPAWN_EGG.get());
                        output.accept(ModItems.DRAGONFLY_SPAWN_EGG.get());
                        output.accept(ModItems.DUNGEON_BEAST_SPAWN_EGG.get());
                        output.accept(ModItems.EASTER_BUNNY_SPAWN_EGG.get());
                        output.accept(ModItems.EMPEROR_SCORPION_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_KNIGHT_SPAWN_EGG.get());
                        output.accept(ModItems.ENDER_REAPER_SPAWN_EGG.get());
                        output.accept(ModItems.FAIRY_SPAWN_EGG.get());
                        output.accept(ModItems.FIREFLY_SPAWN_EGG.get());
                        output.accept(ModItems.FLOUNDER_SPAWN_EGG.get());
                        output.accept(ModItems.FROG_SPAWN_EGG.get());
                        output.accept(ModItems.GAMMA_METROID_SPAWN_EGG.get());
                        output.accept(ModItems.GAZELLE_SPAWN_EGG.get());
                        output.accept(ModItems.GHOST_SPAWN_EGG.get());
                        output.accept(ModItems.GHOST_SKELLY_SPAWN_EGG.get());
                        output.accept(ModItems.GIANT_ROBOT_SPAWN_EGG.get());
                        output.accept(ModItems.JEFFERY_SPAWN_EGG.get());
                        output.accept(ModItems.GIRLFRIEND_SPAWN_EGG.get());
                        output.accept(ModItems.GODZILLA_SPAWN_EGG.get());
                        output.accept(ModItems.GOLD_FISH_SPAWN_EGG.get());
                        output.accept(ModItems.HAMMERHEAD_SPAWN_EGG.get());
                        output.accept(ModItems.HERCULES_BEETLE_SPAWN_EGG.get());
                        output.accept(ModItems.HYDROLISC_SPAWN_EGG.get());
                        output.accept(ModItems.IRUKANDJI_SPAWN_EGG.get());
                        output.accept(ModItems.KRAKEN_SPAWN_EGG.get());
                        output.accept(ModItems.KYUUBI_SPAWN_EGG.get());
                        output.accept(ModItems.LEAF_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.LEON_SPAWN_EGG.get());
                        output.accept(ModItems.LIZARD_SPAWN_EGG.get());
                        output.accept(ModItems.LUNA_MOTH_SPAWN_EGG.get());
                        output.accept(ModItems.LURKING_TERROR_SPAWN_EGG.get());
                        output.accept(ModItems.MANTIS_SPAWN_EGG.get());
                        output.accept(ModItems.MOLENOID_SPAWN_EGG.get());
                        output.accept(ModItems.MOSQUITO_SPAWN_EGG.get());
                        output.accept(ModItems.MOTHRA_SPAWN_EGG.get());
                        output.accept(ModItems.NASTYSAURUS_SPAWN_EGG.get());
                        output.accept(ModItems.OSTRICH_SPAWN_EGG.get());
                        output.accept(ModItems.PEACOCK_SPAWN_EGG.get());
                        output.accept(ModItems.PITCH_BLACK_SPAWN_EGG.get());
                        output.accept(ModItems.POINTYSAURUS_SPAWN_EGG.get());
                        output.accept(ModItems.RAINBOW_ANT_SPAWN_EGG.get());
                        output.accept(ModItems.RAT_SPAWN_EGG.get());
                        output.accept(ModItems.RED_ANT_SPAWN_EGG.get());
                        output.accept(ModItems.RED_COW_SPAWN_EGG.get());
                        output.accept(ModItems.ROBOT_1_SPAWN_EGG.get());
                        output.accept(ModItems.ROBOT_2_SPAWN_EGG.get());
                        output.accept(ModItems.ROBOT_3_SPAWN_EGG.get());
                        output.accept(ModItems.ROBOT_4_SPAWN_EGG.get());
                        output.accept(ModItems.ROBOT_5_SPAWN_EGG.get());
                        output.accept(ModItems.ROTATOR_SPAWN_EGG.get());
                        output.accept(ModItems.RUBBER_DUCKY_SPAWN_EGG.get());
                        output.accept(ModItems.SCORPION_SPAWN_EGG.get());
                        output.accept(ModItems.SEA_MONSTER_SPAWN_EGG.get());
                        output.accept(ModItems.SEA_VIPER_SPAWN_EGG.get());
                        output.accept(ModItems.SKATE_SPAWN_EGG.get());
                        output.accept(ModItems.SPIDER_DRIVER_SPAWN_EGG.get());
                        output.accept(ModItems.SPIDER_ROBOT_SPAWN_EGG.get());
                        output.accept(ModItems.SPIT_BUG_SPAWN_EGG.get());
                        output.accept(ModItems.SPYRO_SPAWN_EGG.get());
                        output.accept(ModItems.STINK_BUG_SPAWN_EGG.get());
                        output.accept(ModItems.STINKY_SPAWN_EGG.get());
                        output.accept(ModItems.TERMITE_SPAWN_EGG.get());
                        output.accept(ModItems.TERRIBLE_TERROR_SPAWN_EGG.get());
                        output.accept(ModItems.THE_KING_SPAWN_EGG.get());
                        output.accept(ModItems.THE_PRINCE_SPAWN_EGG.get());
                        output.accept(ModItems.THE_PRINCE_ADULT_SPAWN_EGG.get());
                        output.accept(ModItems.THE_PRINCE_TEEN_SPAWN_EGG.get());
                        output.accept(ModItems.THE_PRINCESS_SPAWN_EGG.get());
                        output.accept(ModItems.THE_QUEEN_SPAWN_EGG.get());
                        output.accept(ModItems.TREX_SPAWN_EGG.get());
                        output.accept(ModItems.TRIFFID_SPAWN_EGG.get());
                        output.accept(ModItems.TROOPER_BUG_SPAWN_EGG.get());
                        output.accept(ModItems.UNSTABLE_ANT_SPAWN_EGG.get());
                        output.accept(ModItems.URCHIN_SPAWN_EGG.get());
                        output.accept(ModItems.VELOCITY_RAPTOR_SPAWN_EGG.get());
                        output.accept(ModItems.VORTEX_SPAWN_EGG.get());
                        output.accept(ModItems.WATER_DRAGON_SPAWN_EGG.get());
                        output.accept(ModItems.WHALE_SPAWN_EGG.get());
                        output.accept(ModItems.WORM_SMALL_SPAWN_EGG.get());
                        output.accept(ModItems.WORM_MEDIUM_SPAWN_EGG.get());
                        output.accept(ModItems.WORM_LARGE_SPAWN_EGG.get());
                        output.accept(ModItems.CRYSTAL_COW_SPAWN_EGG.get());
                        output.accept(ModItems.GOLD_COW_SPAWN_EGG.get());
                        output.accept(ModItems.ENCHANTED_COW_SPAWN_EGG.get());
                        output.accept(ModItems.RUBY_BIRD_SPAWN_EGG.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
