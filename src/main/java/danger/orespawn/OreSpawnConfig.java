package danger.orespawn;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OreSpawnConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Mob toggles
    public static final ModConfigSpec.BooleanValue ALL_MOBS_DISABLE;
    public static final ModConfigSpec.BooleanValue MOSQUITO_ENABLE;
    public static final ModConfigSpec.BooleanValue ROCK_ENABLE;
    public static final ModConfigSpec.BooleanValue GHOST_ENABLE;
    public static final ModConfigSpec.BooleanValue GHOST_SKELLY_ENABLE;
    public static final ModConfigSpec.BooleanValue SPIDER_DRIVER_ENABLE;
    public static final ModConfigSpec.BooleanValue JEFFERY_ENABLE;
    public static final ModConfigSpec.BooleanValue MOTHRA_ENABLE;
    public static final ModConfigSpec.BooleanValue BRUTALFLY_ENABLE;
    public static final ModConfigSpec.BooleanValue NASTYSAURUS_ENABLE;
    public static final ModConfigSpec.BooleanValue POINTYSAURUS_ENABLE;
    public static final ModConfigSpec.BooleanValue CRICKET_ENABLE;
    public static final ModConfigSpec.BooleanValue FROG_ENABLE;
    public static final ModConfigSpec.BooleanValue BLACK_ANT_ENABLE;
    public static final ModConfigSpec.BooleanValue RED_ANT_ENABLE;
    public static final ModConfigSpec.BooleanValue TERMITE_ENABLE;
    public static final ModConfigSpec.BooleanValue UNSTABLE_ANT_ENABLE;
    public static final ModConfigSpec.BooleanValue RAINBOW_ANT_ENABLE;
    public static final ModConfigSpec.BooleanValue ALOSAURUS_ENABLE;
    public static final ModConfigSpec.BooleanValue HAMMERHEAD_ENABLE;
    public static final ModConfigSpec.BooleanValue LEON_ENABLE;
    public static final ModConfigSpec.BooleanValue CATERKILLER_ENABLE;
    public static final ModConfigSpec.BooleanValue MOLENOID_ENABLE;
    public static final ModConfigSpec.BooleanValue TREX_ENABLE;
    public static final ModConfigSpec.BooleanValue CRYOLOPHOSAURUS_ENABLE;
    public static final ModConfigSpec.BooleanValue URCHIN_ENABLE;
    public static final ModConfigSpec.BooleanValue CAMARASAURUS_ENABLE;
    public static final ModConfigSpec.BooleanValue CHIPMUNK_ENABLE;
    public static final ModConfigSpec.BooleanValue OSTRICH_ENABLE;
    public static final ModConfigSpec.BooleanValue GAZELLE_ENABLE;
    public static final ModConfigSpec.BooleanValue VELOCITY_RAPTOR_ENABLE;
    public static final ModConfigSpec.BooleanValue HYDROLISC_ENABLE;
    public static final ModConfigSpec.BooleanValue SPYRO_ENABLE;
    public static final ModConfigSpec.BooleanValue BARYONYX_ENABLE;
    public static final ModConfigSpec.BooleanValue COCKATEIL_ENABLE;
    public static final ModConfigSpec.BooleanValue CASSOWARY_ENABLE;
    public static final ModConfigSpec.BooleanValue EASTER_BUNNY_ENABLE;
    public static final ModConfigSpec.BooleanValue PEACOCK_ENABLE;
    public static final ModConfigSpec.BooleanValue KYUUBI_ENABLE;
    public static final ModConfigSpec.BooleanValue CEPHADROME_ENABLE;
    public static final ModConfigSpec.BooleanValue DRAGON_ENABLE;
    public static final ModConfigSpec.BooleanValue GAMMA_METROID_ENABLE;
    public static final ModConfigSpec.BooleanValue BASILISK_ENABLE;
    public static final ModConfigSpec.BooleanValue DRAGONFLY_ENABLE;

    // Tweaks
    public static final ModConfigSpec.BooleanValue LESS_ORE;
    public static final ModConfigSpec.IntValue LESS_LAG;
    public static final ModConfigSpec.BooleanValue RAT_PLAYER_FRIENDLY;
    public static final ModConfigSpec.BooleanValue RAT_PET_FRIENDLY;
    public static final ModConfigSpec.IntValue NIGHTMARE_SIZE;
    public static final ModConfigSpec.IntValue ISLAND_SPEED_FACTOR;
    public static final ModConfigSpec.IntValue ISLAND_SIZE_FACTOR;
    public static final ModConfigSpec.BooleanValue GINORMOUS_EMERALD_TREE_ENABLE;
    public static final ModConfigSpec.BooleanValue GUI_OVERLAY_ENABLE;
    public static final ModConfigSpec.BooleanValue ULTIMATE_SWORD_PVP;
    public static final ModConfigSpec.BooleanValue BIG_BERTHA_PVP;
    public static final ModConfigSpec.BooleanValue BOYFRIEND_BRO_MODE;
    public static final ModConfigSpec.BooleanValue DUPLICATOR_TREE_ENABLE;
    public static final ModConfigSpec.BooleanValue ROYAL_GLIDE_ENABLE;
    public static final ModConfigSpec.BooleanValue DRAGONFLY_HORSE_FRIENDLY;
    public static final ModConfigSpec.BooleanValue PLAY_NICELY;
    public static final ModConfigSpec.BooleanValue MINERS_DREAM_EXPENSIVE;
    public static final ModConfigSpec.BooleanValue DISABLE_OVERWORLD_DUNGEONS;
    public static final ModConfigSpec.BooleanValue FULL_POWER_KING_ENABLE;
    public static final ModConfigSpec.BooleanValue MOTHRA_PEACEFUL;
    public static final ModConfigSpec.IntValue ULTIMATE_SWORD_MAGIC;
    public static final ModConfigSpec.IntValue ULTIMATE_BOW_DAMAGE;

    static {
        BUILDER.push("mobs");
        ALL_MOBS_DISABLE = BUILDER.comment("Disable all OreSpawn mobs from spawning").define("allMobsDisable", false);
        MOSQUITO_ENABLE = BUILDER.define("mosquitoEnable", true);
        ROCK_ENABLE = BUILDER.define("rockEnable", true);
        GHOST_ENABLE = BUILDER.define("ghostEnable", true);
        GHOST_SKELLY_ENABLE = BUILDER.define("ghostSkellyEnable", true);
        SPIDER_DRIVER_ENABLE = BUILDER.define("spiderDriverEnable", true);
        JEFFERY_ENABLE = BUILDER.define("jefferyEnable", true);
        MOTHRA_ENABLE = BUILDER.define("mothraEnable", true);
        BRUTALFLY_ENABLE = BUILDER.define("brutalflyEnable", true);
        NASTYSAURUS_ENABLE = BUILDER.define("nastysaurusEnable", true);
        POINTYSAURUS_ENABLE = BUILDER.define("pointysaurusEnable", true);
        CRICKET_ENABLE = BUILDER.define("cricketEnable", true);
        FROG_ENABLE = BUILDER.define("frogEnable", true);
        BLACK_ANT_ENABLE = BUILDER.define("blackAntEnable", true);
        RED_ANT_ENABLE = BUILDER.define("redAntEnable", true);
        TERMITE_ENABLE = BUILDER.define("termiteEnable", true);
        UNSTABLE_ANT_ENABLE = BUILDER.define("unstableAntEnable", true);
        RAINBOW_ANT_ENABLE = BUILDER.define("rainbowAntEnable", true);
        ALOSAURUS_ENABLE = BUILDER.define("alosaurusEnable", true);
        HAMMERHEAD_ENABLE = BUILDER.define("hammerheadEnable", true);
        LEON_ENABLE = BUILDER.define("leonEnable", true);
        CATERKILLER_ENABLE = BUILDER.define("caterKillerEnable", true);
        MOLENOID_ENABLE = BUILDER.define("molenoidEnable", true);
        TREX_ENABLE = BUILDER.define("trexEnable", true);
        CRYOLOPHOSAURUS_ENABLE = BUILDER.define("cryolophosaurusEnable", true);
        URCHIN_ENABLE = BUILDER.define("urchinEnable", true);
        CAMARASAURUS_ENABLE = BUILDER.define("camarasaurusEnable", true);
        CHIPMUNK_ENABLE = BUILDER.define("chipmunkEnable", true);
        OSTRICH_ENABLE = BUILDER.define("ostrichEnable", true);
        GAZELLE_ENABLE = BUILDER.define("gazelleEnable", true);
        VELOCITY_RAPTOR_ENABLE = BUILDER.define("velocityRaptorEnable", true);
        HYDROLISC_ENABLE = BUILDER.define("hydroliscEnable", true);
        SPYRO_ENABLE = BUILDER.define("spyroEnable", true);
        BARYONYX_ENABLE = BUILDER.define("baryonyxEnable", true);
        COCKATEIL_ENABLE = BUILDER.define("cockateilEnable", true);
        CASSOWARY_ENABLE = BUILDER.define("cassowaryEnable", true);
        EASTER_BUNNY_ENABLE = BUILDER.define("easterBunnyEnable", true);
        PEACOCK_ENABLE = BUILDER.define("peacockEnable", true);
        KYUUBI_ENABLE = BUILDER.define("kyuubiEnable", true);
        CEPHADROME_ENABLE = BUILDER.define("cephadromeEnable", true);
        DRAGON_ENABLE = BUILDER.define("dragonEnable", true);
        GAMMA_METROID_ENABLE = BUILDER.define("gammaMetroidEnable", true);
        BASILISK_ENABLE = BUILDER.define("basiliskEnable", true);
        DRAGONFLY_ENABLE = BUILDER.define("dragonflyEnable", true);
        MOTHRA_PEACEFUL = BUILDER.define("mothraPeaceful", false);
        BUILDER.pop();

        BUILDER.push("tweaks");
        // TODO: LESS_ORE requires datapack-level changes to biome modifier JSONs;
        // wiring this at runtime would need modification of placed feature configs.
        LESS_ORE = BUILDER.define("lessOre", false);
        LESS_LAG = BUILDER.defineInRange("lessLag", 0, 0, 2);
        RAT_PLAYER_FRIENDLY = BUILDER.define("ratPlayerFriendly", true);
        RAT_PET_FRIENDLY = BUILDER.define("ratPetFriendly", true);
        NIGHTMARE_SIZE = BUILDER.defineInRange("nightmareSize", 0, 0, 5);
        ISLAND_SPEED_FACTOR = BUILDER.defineInRange("islandSpeedFactor", 2, 1, 5);
        ISLAND_SIZE_FACTOR = BUILDER.defineInRange("islandSizeFactor", 2, 1, 5);
        GINORMOUS_EMERALD_TREE_ENABLE = BUILDER.define("ginormousEmeraldTreeEnable", true);
        GUI_OVERLAY_ENABLE = BUILDER.define("guiOverlayEnable", true);
        ULTIMATE_SWORD_PVP = BUILDER.define("ultimateSwordPvp", false);
        BIG_BERTHA_PVP = BUILDER.define("bigBerthaPvp", false);
        BOYFRIEND_BRO_MODE = BUILDER.define("boyfriendBroMode", false);
        DUPLICATOR_TREE_ENABLE = BUILDER.define("duplicatorTreeEnable", true);
        ROYAL_GLIDE_ENABLE = BUILDER.define("royalGlideEnable", true);
        DRAGONFLY_HORSE_FRIENDLY = BUILDER.define("dragonflyHorseFriendly", false);
        PLAY_NICELY = BUILDER.define("playNicely", false);
        MINERS_DREAM_EXPENSIVE = BUILDER.define("minersDreamExpensive", false);
        DISABLE_OVERWORLD_DUNGEONS = BUILDER.define("disableOverworldDungeons", false);
        FULL_POWER_KING_ENABLE = BUILDER.define("fullPowerKingEnable", false);
        BUILDER.pop();

        BUILDER.push("weapons");
        ULTIMATE_SWORD_MAGIC = BUILDER.defineInRange("ultimateSwordEnchantmentLevel", 5, 1, 10);
        ULTIMATE_BOW_DAMAGE = BUILDER.defineInRange("ultimateBowDamage", 10, 2, 20);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
