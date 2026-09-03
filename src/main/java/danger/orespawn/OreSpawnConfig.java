package danger.orespawn;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OreSpawnConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * 2.0 spider overhaul: which gait solver the robot spiders use.
     * CLASSIC is the untouched 1.7.10-parity client-side gait; MODERN is the
     * server-authoritative procedural IK gait. Snapshotted at entity
     * construction ON THE SERVER (BOSS-017 pattern) and synced to clients on
     * the entity — flipping the value affects newly constructed/loaded
     * spiders, not live ones, and the client's own copy of this file is
     * never consulted for the mode. The EFFECTIVE mode is
     * {@link OreSpawnConfig#spiderMovement()} (master-override ruling
     * 2026-09-04): CLASSIC whenever {@code [modern] enabled} is off, this
     * key's value while it is on.
     */
    public enum SpiderMovement { CLASSIC, MODERN }

    // Mob toggles
    public static final ModConfigSpec.BooleanValue ALL_MOBS_DISABLE;
    /**
     * Phase E wiki-only content (port-only feature: no 1.7.10 source class).
     * Effective only when [modern] enabled is true -- read through
     * {@link #phase14ContentEnable()}, never directly.
     */
    public static final ModConfigSpec.BooleanValue PHASE14_CONTENT_ENABLE;
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
    // Remaining per-mob flags from the original's ~100-entry table
    // (orig OreSpawnMain.java:6364-6465), added for ANIM-018.
    public static final ModConfigSpec.BooleanValue CRIMINAL_ENABLE;
    public static final ModConfigSpec.BooleanValue RAT_ENABLE;
    public static final ModConfigSpec.BooleanValue EMPEROR_SCORPION_ENABLE;
    public static final ModConfigSpec.BooleanValue TROOPER_BUG_ENABLE;
    public static final ModConfigSpec.BooleanValue SPIT_BUG_ENABLE;
    public static final ModConfigSpec.BooleanValue STINK_BUG_ENABLE;
    public static final ModConfigSpec.BooleanValue SCORPION_ENABLE;
    public static final ModConfigSpec.BooleanValue CAVE_FISHER_ENABLE;
    public static final ModConfigSpec.BooleanValue ALIEN_ENABLE;
    public static final ModConfigSpec.BooleanValue WATER_DRAGON_ENABLE;
    public static final ModConfigSpec.BooleanValue SEA_MONSTER_ENABLE;
    public static final ModConfigSpec.BooleanValue SEA_VIPER_ENABLE;
    public static final ModConfigSpec.BooleanValue ATTACK_SQUID_ENABLE;
    public static final ModConfigSpec.BooleanValue ROBOT1_ENABLE;
    public static final ModConfigSpec.BooleanValue ROBOT2_ENABLE;
    public static final ModConfigSpec.BooleanValue ROBOT3_ENABLE;
    public static final ModConfigSpec.BooleanValue ROBOT4_ENABLE;
    public static final ModConfigSpec.BooleanValue ROBOT5_ENABLE;
    public static final ModConfigSpec.BooleanValue ROTATOR_ENABLE;
    public static final ModConfigSpec.BooleanValue VORTEX_ENABLE;
    public static final ModConfigSpec.BooleanValue DUNGEON_BEAST_ENABLE;
    public static final ModConfigSpec.BooleanValue KRAKEN_ENABLE;
    public static final ModConfigSpec.BooleanValue LIZARD_ENABLE;
    public static final ModConfigSpec.BooleanValue RUBBER_DUCKY_ENABLE;
    public static final ModConfigSpec.BooleanValue GIRLFRIEND_ENABLE;
    public static final ModConfigSpec.BooleanValue BOYFRIEND_ENABLE;
    public static final ModConfigSpec.BooleanValue FIREFLY_ENABLE;
    public static final ModConfigSpec.BooleanValue FAIRY_ENABLE;
    public static final ModConfigSpec.BooleanValue BEE_ENABLE;
    public static final ModConfigSpec.BooleanValue MANTIS_ENABLE;
    public static final ModConfigSpec.BooleanValue STINKY_ENABLE;
    public static final ModConfigSpec.BooleanValue HERCULES_BEETLE_ENABLE;
    public static final ModConfigSpec.BooleanValue COW_ENABLE;
    public static final ModConfigSpec.BooleanValue BUTTERFLY_ENABLE;
    public static final ModConfigSpec.BooleanValue MOTH_ENABLE;
    public static final ModConfigSpec.BooleanValue TSHIRT_ENABLE;
    public static final ModConfigSpec.BooleanValue COIN_ENABLE;
    public static final ModConfigSpec.BooleanValue CREEPING_HORROR_ENABLE;
    public static final ModConfigSpec.BooleanValue TERRIBLE_TERROR_ENABLE;
    public static final ModConfigSpec.BooleanValue CLIFF_RACER_ENABLE;
    public static final ModConfigSpec.BooleanValue TRIFFID_ENABLE;
    public static final ModConfigSpec.BooleanValue WORM_ENABLE;
    public static final ModConfigSpec.BooleanValue CLOUD_SHARK_ENABLE;
    public static final ModConfigSpec.BooleanValue GOLD_FISH_ENABLE;
    public static final ModConfigSpec.BooleanValue LEAF_MONSTER_ENABLE;
    public static final ModConfigSpec.BooleanValue ENDER_KNIGHT_ENABLE;
    public static final ModConfigSpec.BooleanValue ENDER_REAPER_ENABLE;
    public static final ModConfigSpec.BooleanValue BEAVER_ENABLE;
    public static final ModConfigSpec.BooleanValue IRUKANDJI_ENABLE;
    public static final ModConfigSpec.BooleanValue SKATE_ENABLE;
    public static final ModConfigSpec.BooleanValue WHALE_ENABLE;
    public static final ModConfigSpec.BooleanValue FLOUNDER_ENABLE;
    public static final ModConfigSpec.BooleanValue PITCH_BLACK_ENABLE;
    public static final ModConfigSpec.BooleanValue LURKING_TERROR_ENABLE;
    public static final ModConfigSpec.BooleanValue GODZILLA_ENABLE;
    public static final ModConfigSpec.BooleanValue CRAB_ENABLE;

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
    /**
     * 2.0 spider overhaul key ({@code [tweaks] spiderMovement}). Effective
     * only when [modern] enabled is true -- read through
     * {@link #spiderMovement()}, never directly.
     */
    public static final ModConfigSpec.EnumValue<SpiderMovement> SPIDER_MOVEMENT;
    /**
     * S7b: the modern-mount riding camera (client-visual only). Effective
     * only when [modern] enabled is true -- read through
     * {@link #mountCamera()}, never directly.
     */
    public static final ModConfigSpec.BooleanValue MOUNT_CAMERA;
    public static final ModConfigSpec.BooleanValue MINERS_DREAM_EXPENSIVE;
    public static final ModConfigSpec.BooleanValue DISABLE_OVERWORLD_DUNGEONS;
    public static final ModConfigSpec.BooleanValue FULL_POWER_KING_ENABLE;
    public static final ModConfigSpec.BooleanValue THE_KING_ENABLE;
    public static final ModConfigSpec.BooleanValue THE_QUEEN_ENABLE;
    public static final ModConfigSpec.BooleanValue MOTHRA_PEACEFUL;
    public static final ModConfigSpec.BooleanValue MOBZILLA_SINGLE_SPAWN;
    public static final ModConfigSpec.BooleanValue MOTHRA_REQUIRES_SPAWNER;
    public static final ModConfigSpec.IntValue ULTIMATE_SWORD_MAGIC;
    public static final ModConfigSpec.IntValue ULTIMATE_BOW_DAMAGE;

    // 2.0 modern mode (owner rulings 2026-09-03; master-override ruling
    // 2026-09-04, verbatim: "modern.enabled: master override only. Off forces
    // all modern features off; on defers to existing per-feature keys, which
    // keep their names. New features register under [modern]."; default
    // ruling of the same day, verbatim: "modern.enabled defaults to true in
    // code — the master defers to per-feature keys by default and only forces
    // classic when set false."). [modern].enabled is the ONE master switch:
    // master override; defaults to true and defers to the per-feature keys;
    // set false to force every modern feature to its classic/off value; new
    // modern features register under [modern]. The earlier per-feature 2.0
    // keys (spiderMovement, mountCamera, phase14ContentEnable) keep their
    // names and their [tweaks] section and are effective only while the
    // master is on -- every read goes through the effective-value helpers at
    // the bottom of this class (spiderMovement(), mountCamera(),
    // phase14ContentEnable(), mothraWideRootHitbox()), never through the keys
    // directly. The MOD-024 items are unimplemented proposals, not keys.
    /**
     * Master modern-mode switch -- master override; defaults to true and
     * defers to the per-feature keys; set false to force every modern feature
     * to its classic/off value; new modern features register under [modern].
     * (Introduced 2026-09-03 with default false; flipped to true by the
     * owner's ruling of 2026-09-04.) A default config therefore runs each
     * modern feature at its own key's default -- the modern robots with the
     * riding camera.
     * Effective values: {@link #spiderMovement()}, {@link #mountCamera()},
     * {@link #phase14ContentEnable()}, {@link #mothraWideRootHitbox()} -- a
     * feature's key is never consulted without this master. Phase G artist
     * animations will hang off this same master ("artist animations are a
     * 2.0 feature behind the modern config; classic stays code-driven
     * parity", ruling 2026-09-03). Construction-snapshotted features (the
     * robot gait mode, the hitbox sub-keys; BOSS-017 pattern) see a flip on
     * newly constructed/loaded entities only, not live ones.
     */
    public static final ModConfigSpec.BooleanValue MODERN_ENABLED;
    /**
     * MOD-029 (ACCEPTED 2026-09-03: "modern-mode default; classic keeps 5x2"):
     * Mothra's root hitbox is the port's original 6 x 3 instead of the 1.7.10
     * 5.0 x 2.0 (orig Mothra.java:65). Takes effect only while
     * {@link #MODERN_ENABLED} is on -- read through
     * {@link #mothraWideRootHitbox()}, never directly.
     */
    public static final ModConfigSpec.BooleanValue MODERN_MOTHRA_WIDE_ROOT_HITBOX;

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
        // orig OreSpawnMain.java:6364-6465 — every flag defaults to 1 except
        // BoyfriendEnable (orig :6430, default 0). Original key spellings kept
        // where they differed from the class name ("NightmareEnable" for
        // PitchBlack, orig :6462; "CriminalEnable" for BandP, orig :6388).
        CRIMINAL_ENABLE = BUILDER.define("criminalEnable", true);
        RAT_ENABLE = BUILDER.define("ratEnable", true);
        EMPEROR_SCORPION_ENABLE = BUILDER.define("emperorScorpionEnable", true);
        TROOPER_BUG_ENABLE = BUILDER.define("trooperBugEnable", true);
        SPIT_BUG_ENABLE = BUILDER.define("spitBugEnable", true);
        STINK_BUG_ENABLE = BUILDER.define("stinkBugEnable", true);
        SCORPION_ENABLE = BUILDER.define("scorpionEnable", true);
        CAVE_FISHER_ENABLE = BUILDER.define("caveFisherEnable", true);
        ALIEN_ENABLE = BUILDER.define("alienEnable", true);
        WATER_DRAGON_ENABLE = BUILDER.define("waterDragonEnable", true);
        SEA_MONSTER_ENABLE = BUILDER.define("seaMonsterEnable", true);
        SEA_VIPER_ENABLE = BUILDER.define("seaViperEnable", true);
        ATTACK_SQUID_ENABLE = BUILDER.define("attackSquidEnable", true);
        ROBOT1_ENABLE = BUILDER.define("robot1Enable", true);
        ROBOT2_ENABLE = BUILDER.define("robot2Enable", true);
        ROBOT3_ENABLE = BUILDER.define("robot3Enable", true);
        ROBOT4_ENABLE = BUILDER.define("robot4Enable", true);
        ROBOT5_ENABLE = BUILDER.define("robot5Enable", true);
        ROTATOR_ENABLE = BUILDER.define("rotatorEnable", true);
        VORTEX_ENABLE = BUILDER.define("vortexEnable", true);
        DUNGEON_BEAST_ENABLE = BUILDER.define("dungeonBeastEnable", true);
        KRAKEN_ENABLE = BUILDER.define("krakenEnable", true);
        LIZARD_ENABLE = BUILDER.define("lizardEnable", true);
        RUBBER_DUCKY_ENABLE = BUILDER.define("rubberDuckyEnable", true);
        GIRLFRIEND_ENABLE = BUILDER.define("girlfriendEnable", true);
        BOYFRIEND_ENABLE = BUILDER.define("boyfriendEnable", false);
        FIREFLY_ENABLE = BUILDER.define("fireflyEnable", true);
        FAIRY_ENABLE = BUILDER.define("fairyEnable", true);
        BEE_ENABLE = BUILDER.define("beeEnable", true);
        MANTIS_ENABLE = BUILDER.define("mantisEnable", true);
        STINKY_ENABLE = BUILDER.define("stinkyEnable", true);
        HERCULES_BEETLE_ENABLE = BUILDER.define("herculesBeetleEnable", true);
        COW_ENABLE = BUILDER.define("cowEnable", true);
        BUTTERFLY_ENABLE = BUILDER.define("butterflyEnable", true);
        MOTH_ENABLE = BUILDER.define("mothEnable", true);
        TSHIRT_ENABLE = BUILDER.define("tshirtEnable", true);
        COIN_ENABLE = BUILDER.define("coinEnable", true);
        CREEPING_HORROR_ENABLE = BUILDER.define("creepingHorrorEnable", true);
        TERRIBLE_TERROR_ENABLE = BUILDER.define("terribleTerrorEnable", true);
        CLIFF_RACER_ENABLE = BUILDER.define("cliffRacerEnable", true);
        TRIFFID_ENABLE = BUILDER.define("triffidEnable", true);
        WORM_ENABLE = BUILDER.define("wormEnable", true);
        CLOUD_SHARK_ENABLE = BUILDER.define("cloudSharkEnable", true);
        GOLD_FISH_ENABLE = BUILDER.define("goldFishEnable", true);
        LEAF_MONSTER_ENABLE = BUILDER.define("leafMonsterEnable", true);
        ENDER_KNIGHT_ENABLE = BUILDER.define("enderKnightEnable", true);
        ENDER_REAPER_ENABLE = BUILDER.define("enderReaperEnable", true);
        BEAVER_ENABLE = BUILDER.define("beaverEnable", true);
        IRUKANDJI_ENABLE = BUILDER.define("irukandjiEnable", true);
        SKATE_ENABLE = BUILDER.define("skateEnable", true);
        WHALE_ENABLE = BUILDER.define("whaleEnable", true);
        FLOUNDER_ENABLE = BUILDER.define("flounderEnable", true);
        PITCH_BLACK_ENABLE = BUILDER.define("nightmareEnable", true);
        LURKING_TERROR_ENABLE = BUILDER.define("lurkingTerrorEnable", true);
        GODZILLA_ENABLE = BUILDER.define("godzillaEnable", true);
        CRAB_ENABLE = BUILDER.define("crabEnable", true);
        MOTHRA_PEACEFUL = BUILDER.define("mothraPeaceful", false);
        MOBZILLA_SINGLE_SPAWN = BUILDER.comment(
                "If true, only one Mobzilla can ever spawn naturally per world (1.7.10 godzilla_has_spawned behavior). " +
                        "Players can still summon additional Mobzillas via the 9 Ancient Dried Mobzilla Egg Parts."
        ).define("mobzillaSingleSpawn", true);
        MOTHRA_REQUIRES_SPAWNER = BUILDER.comment(
                "If true, Mothra can only spawn naturally when a vanilla mob spawner block exists nearby (1.7.10 gating). " +
                        "Disable to allow Mothra to spawn freely from biome modifiers."
        ).define("mothraRequiresSpawner", true);
        BUILDER.pop();

        BUILDER.push("tweaks");
        // orig OreSpawnMain.java:1470 — "LessOre", default 0. Wired through the
        // orespawn:less_ore_count placement modifier (LessOreCountPlacement);
        // Mining-dim density gating remains WGEN-011 scope.
        LESS_ORE = BUILDER.comment(
                "If true, OreSpawn overworld ore/troll-block vein counts are reduced (1.7.10 LessOre)."
        ).define("lessOre", false);
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
        // 2.0 spider overhaul (design ruling 2026-08-11): key default MODERN.
        // Master-override ruling 2026-09-04: both keys below are effective
        // only while [modern] enabled is true -- its default, so a default
        // config runs each key's own default (MODERN, camera on); the master
        // forces CLASSIC / camera off when set false. Server-
        // authoritative: the server's EFFECTIVE value at entity construction
        // wins and is synced.
        MOUNT_CAMERA = BUILDER.comment(
                "Riding camera for the modern robot mounts (spider/ant): pulls the third-person " +
                        "camera back and up, smoothed, with terrain collision, so the mount does not " +
                        "fill the screen. Client-visual only; no effect unridden, in CLASSIC mode, " +
                        "or when disabled — the vanilla camera is untouched then. " +
                        "Effective only when [modern] enabled is true (its default)."
        ).define("mountCamera", true);
        SPIDER_MOVEMENT = BUILDER.comment(
                "Robot leg movement (Giant Robot Spider AND Robot Ant). MODERN = procedural IK gait " +
                        "(server-authoritative, 2.0): legs plant and climb terrain, the body tilts and " +
                        "sags, legs are real hittable hitboxes, and a mounted player can STEER the " +
                        "spider (the ant keeps its own classic hover ride, with legs that dangle " +
                        "in flight and re-plant on landing). " +
                        "CLASSIC = the exact 1.7.10 behavior, preserved bit-identically (visual-only legs, " +
                        "body-only hitbox, unsteerable spider saddle). " +
                        "Takes effect for newly spawned/loaded robots; the server's setting wins in multiplayer. " +
                        "Effective only when [modern] enabled is true (its default); the master forces CLASSIC when set false."
        ).defineEnum("spiderMovement", SpiderMovement.MODERN);
        MINERS_DREAM_EXPENSIVE = BUILDER.define("minersDreamExpensive", false);
        DISABLE_OVERWORLD_DUNGEONS = BUILDER.define("disableOverworldDungeons", false);
        FULL_POWER_KING_ENABLE = BUILDER.define("fullPowerKingEnable", false);
        // orig OreSpawnMain.java:6434-6435 — "TheKingEnable"/"TheQueenEnable", default 1.
        THE_KING_ENABLE = BUILDER.comment(
                "If false, the King Spawner block fizzles without summoning The King (1.7.10 TheKingEnable)."
        ).define("theKingEnable", true);
        THE_QUEEN_ENABLE = BUILDER.comment(
                "If false, the Queen Spawner block fizzles without summoning The Queen (1.7.10 TheQueenEnable)."
        ).define("theQueenEnable", true);
        // Phase E ruling (2026-08-11): wiki-documented mobs with no 1.7.10
        // source counterpart (Vampire Butterfly, Apple Cow, Golden Apple
        // Cow) stay registered but ship disabled — no natural spawns and
        // no creative spawn eggs unless this is flipped on.
        PHASE14_CONTENT_ENABLE = BUILDER.comment(
                "Enables the optional wiki-documented mobs that never existed in the 1.7.10 source: " +
                        "Vampire Butterfly, Apple Cow, Golden Apple Cow. Off = no natural spawns, " +
                        "no creative spawn eggs. Existing placed/saved mobs are unaffected. " +
                        "Effective only when [modern] enabled is true (its default)."
        ).define("phase14ContentEnable", false);
        BUILDER.pop();

        BUILDER.push("weapons");
        ULTIMATE_SWORD_MAGIC = BUILDER.defineInRange("ultimateSwordEnchantmentLevel", 5, 1, 10);
        ULTIMATE_BOW_DAMAGE = BUILDER.defineInRange("ultimateBowDamage", 10, 2, 20);
        BUILDER.pop();

        // Owner rulings 2026-09-03: "MOD-029: accepted as the modern-mode
        // default; classic keeps 5x2" and "Artist animations are a 2.0 feature
        // behind the modern config; classic stays code-driven parity". Ruling
        // 2026-09-04: "modern.enabled: master override only. Off forces all
        // modern features off; on defers to existing per-feature keys, which
        // keep their names. New features register under [modern]." Default
        // ruling 2026-09-04: "modern.enabled defaults to true in code — the
        // master defers to per-feature keys by default and only forces
        // classic when set false." (introduced 2026-09-03 with default false).
        BUILDER.push("modern");
        MODERN_ENABLED = BUILDER.comment(
                "Master switch for OreSpawn 2.0 modern mode -- master override; defaults to true and defers " +
                        "to the per-feature keys; set false to force every modern feature to its classic/off " +
                        "value; new modern features register under [modern]. true (default) = each modern " +
                        "feature follows its own key (tweaks.spiderMovement, tweaks.mountCamera, " +
                        "tweaks.phase14ContentEnable, modern.mothraWideRootHitbox); false = classic 1.7.10 " +
                        "parity everywhere, whatever the per-feature keys say. Phase G artist animations will " +
                        "hang off this same switch (classic stays code-driven parity). Snapshotted features " +
                        "(the robot gait mode, hitbox sub-keys) pick up a flip on newly spawned/loaded " +
                        "entities, not live ones."
        ).define("enabled", true);
        MODERN_MOTHRA_WIDE_ROOT_HITBOX = BUILDER.comment(
                "MOD-029: Mothra's root hitbox is 6 x 3 (the port's original size) instead of the 1.7.10 " +
                        "5 x 2 (orig Mothra.java:65). Only takes effect while modern.enabled is true; classic " +
                        "mode always uses 5 x 2. Her wing/head/body parts are placed from her position and " +
                        "are unaffected; only the root box and the target/spawn sweeps that inflate it change."
        ).define("mothraWideRootHitbox", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    /**
     * MOD-029: the single {@code master && sub-key} evaluation for Mothra's
     * root box -- true only while {@link #MODERN_ENABLED} AND
     * {@link #MODERN_MOTHRA_WIDE_ROOT_HITBOX} are both on. Callers snapshot
     * the result at construction ({@code Mothra} constructor, BOSS-017
     * pattern); it is never re-read on a live entity.
     */
    public static boolean mothraWideRootHitbox() {
        return MODERN_ENABLED.get() && MODERN_MOTHRA_WIDE_ROOT_HITBOX.get();
    }

    /**
     * Effective robot gait mode (master-override ruling 2026-09-04): CLASSIC
     * whenever {@link #MODERN_ENABLED} is off, else {@link #SPIDER_MOVEMENT}.
     * The single read behind the SpiderRobot/AntRobot construction snapshot
     * (their one ctor-tail read consumes this exactly once -- BOSS-017
     * pattern, the S4 ctor-tear rule); never re-read on a live entity.
     */
    public static SpiderMovement spiderMovement() {
        return MODERN_ENABLED.get() ? SPIDER_MOVEMENT.get() : SpiderMovement.CLASSIC;
    }

    /**
     * Effective modern-mount riding camera: {@code master && key}.
     * Client-visual only; read per frame by {@code MountCameraState}.
     */
    public static boolean mountCamera() {
        return MODERN_ENABLED.get() && MOUNT_CAMERA.get();
    }

    /**
     * Effective Phase E wiki-only content gate: {@code master && key}. Read
     * live by the creative tabs and by the natural-spawn control.
     */
    public static boolean phase14ContentEnable() {
        return MODERN_ENABLED.get() && PHASE14_CONTENT_ENABLE.get();
    }
}
