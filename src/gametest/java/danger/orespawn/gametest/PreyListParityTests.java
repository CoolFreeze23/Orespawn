package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.ai.DragonflyHuntGoal;
import danger.orespawn.util.MyUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-128 (targeting ledger batch T6, wave 2 — owner: "Targeting ledger, ruled by wave … Generated pins wherever
 * the pattern allows … Next: T5 and T6"): the exclusion / prey lists. 1.7.10 decided a hunter's prey by name in
 * fixed filter chains; the port had drifted at fifteen of them — the shared {@code MyUtils.isAttackableNonMob}
 * membership (orig MyUtils.java:77-115, reached by the Crab :417, Mantis :391, Molenoid :274, TheKing :981,
 * TheQueen :929 and WaterDragon :679 fallthroughs), the Dragonfly's whitelist (orig Dragonfly.java:213-228), the
 * Lizard's AttackSquid grant (:316), the Purple Power's tamed-pet and royalty steps (:261-264), the Rat's five
 * species steps (:201-224), the Terrible Terror's nine dropped kinds (:229-285), the Triffid's named seven for a
 * blanket {@code !Monster} (:291-311), the Boyfriend / Girlfriend monster task's own rules
 * (MyEntityAITarget.java:88-128), and the three ENT-S-108 residuals (DungeonBeast :216-239, EmperorScorpion
 * :476-493, HerculesBeetle :401-406 — present at HEAD since ENT-S-108, pinned here).
 *
 * <p>The PitchBlackAllyTests shape, generated: one row per (hunter, species) the batch changes — the species
 * frozen 8 blocks off the frozen hunter (18 off the royal pair, whose home is written and which face east so
 * TheKing's block-marching {@code MyCanSee} starts inside the shell — the IgnoreScreenParityTests idiom), the
 * private filter driven by reflection, refused or granted exactly as orig says, and on the same spot a vanilla
 * pig and a vanilla Zombie as controls with orig's own verdict for that hunter (both refused by the Lizard and
 * the Dragonfly; the Zombie taken by the Triffid, the row the old blanket fails; the pig refused by the
 * helper's callers). The helper gets a generator over its thirteen orig members driven directly, the two
 * removed port-only grants refused, and the same members through each HEAD caller wherever the caller's own
 * chain leaves the verdict to the helper (a member the caller's earlier steps decide in both trees — the Crab's
 * own Villager / Girlfriend / Boyfriend grants, the Mantis's Mothra / WaterDragon refusals, the royals' royalty
 * and ignore-screen refusals, the Water Dragon's self-kind refusal, every Monster — is no row: such a row would
 * hold with the helper reverted). The Dragonfly's whitelist is driven through the goal's private
 * {@code findPrey} (PlayNicelyGateParityTests idiom), with the horse toggle flipped and restored in a finally;
 * the Boyfriend / Girlfriend goal is spawned with its goals, read off the target selector by
 * {@code targetType == Mob.class} and asked {@code canUse()} under the ForcedRoll seam, the pick read back
 * (IMobConventionTests idiom). The Purple Power rows set the orb type and tame a vanilla Wolf. Every row is
 * synchronous; spawns are discarded in a finally; no mock players; the batch is this class alone (TEST-003);
 * no documenting-only row — each fails with its port line reverted.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PreyListParityTests {

    private static final String BATCH = "preyListParity";
    private static final String TEST_PREFIX = "preylistparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the templates are named in full. */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final String EMPTY_TALL = OreSpawnMod.MOD_ID + ":empty_tall";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-128";

    /** Hunter and prey 8 blocks apart on the floor, clear line of sight. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** TheKing / TheQueen (22 wide, 24 tall): box 5.5..27.5, prey 18 blocks off, inside the 144 home leash; empty_tall. */
    private static final BlockPos ROYAL_HUNTER_POS = new BlockPos(16, 1, 24);
    private static final BlockPos ROYAL_PREY_POS = new BlockPos(34, 1, 24);
    /** {@code NearestAttackableTargetGoal.canUse} rolls {@code nextInt(reducedTickDelay(10))} = nextInt(5). */
    private static final int GOAL_ROLL_BOUND = 5;

    private static final Consumer<Mob> NO_SETUP = mob -> { };
    /**
     * The royal pair: home = own position (the first-tick init, port TheKing.java:585-588 / TheQueen.java:821-824,
     * unreachable while frozen), and a fixed yaw: TheKing.MyCanSee marches from 22 blocks ahead of the facing at
     * 7/8 of the body height, so facing east keeps that start inside the shell (the IgnoreScreenParityTests idiom).
     */
    private static final Consumer<Mob> ROYAL_SETUP = mob -> {
        writeInt(mob, "homeX", (int) mob.getX());
        writeInt(mob, "homeZ", (int) mob.getZ());
        mob.setYRot(-90.0f);
        mob.yBodyRot = -90.0f;
        mob.yHeadRot = -90.0f;
    };
    private static final Consumer<Mob> TAMED = mob -> ((TamableAnimal) mob).setTame(true, false);

    // ------------------------------------------------------------------
    // The table
    // ------------------------------------------------------------------

    /** How a row reaches the port's filter. */
    private enum Shape { HELPER, FILTER, DRAGONFLY, GOAL }

    /**
     * A hunter: its type, template and spots, its per-spawn setup, whether its sight step is vanilla's eye-to-eye
     * ray (asserted as a precondition; TheKing and the Molenoid march blocks instead — their Zombie control proves
     * the chain runs past that step), and orig's verdict for the two controls.
     */
    private record Hunter(String tag, Supplier<? extends EntityType<? extends Mob>> type, String where, String template,
                          BlockPos hunterPos, BlockPos preyPos, Consumer<Mob> setup, boolean vanillaSight,
                          boolean pigExpected, String pigWhy, boolean zombieExpected, String zombieWhy) {
    }

    /** A candidate: its type, what it is, and a setup (a tamed pet). */
    private record Prey(Supplier<? extends EntityType<? extends Mob>> type, String description, Consumer<Mob> setup) {
    }

    private record Row(int index, Shape shape, Hunter hunter, String speciesTag, Prey prey, boolean expected, String orig,
                       String why, Boolean horseFriendly) {
        String id() {
            return String.format("s128_%02d_%s_%s", this.index, this.hunter.tag(), this.speciesTag);
        }

        String testName() {
            return TEST_PREFIX + this.id();
        }

        String where() {
            return this.hunter.where() + " (orig " + this.orig + ")";
        }
    }

    private static final class Rows {
        private final List<Row> rows = new ArrayList<>();

        void add(Shape shape, Hunter hunter, String speciesTag, Prey prey, boolean expected, String orig, String why) {
            this.add(shape, hunter, speciesTag, prey, expected, orig, why, null);
        }

        void add(Shape shape, Hunter hunter, String speciesTag, Prey prey, boolean expected, String orig, String why,
                 Boolean horseFriendly) {
            this.rows.add(new Row(this.rows.size() + 1, shape, hunter, speciesTag, prey, expected, orig, why, horseFriendly));
        }
    }

    private static Prey prey(Supplier<? extends EntityType<? extends Mob>> type, String description) {
        return new Prey(type, description, NO_SETUP);
    }

    private static Hunter filterHunter(String tag, Supplier<? extends EntityType<? extends Mob>> type, String where,
                                       boolean pigExpected, String pigWhy, boolean zombieExpected, String zombieWhy) {
        return new Hunter(tag, type, where, EMPTY_LARGE, HUNTER_POS, PREY_POS, NO_SETUP, true,
                pigExpected, pigWhy, zombieExpected, zombieWhy);
    }

    // The controls' descriptions and vanilla prey.
    private static final Prey PIG = prey(() -> EntityType.PIG, "a vanilla pig");
    private static final Prey ZOMBIE = prey(() -> EntityType.ZOMBIE, "a vanilla Zombie (a Monster — orig EntityMob)");

    // The helper's thirteen orig members (orig MyUtils.java:78-114) and the two port-only grants.
    private static final Prey SKELETON = prey(() -> EntityType.SKELETON, "a vanilla Skeleton (a Monster — orig EntityMob, MyUtils.java:78)");
    private static final Prey MOTHRA = prey(ModEntities.MOTHRA, "a Mothra (port Mothra, an EntityButterfly — orig Mothra.java:52 implements IMob)");
    private static final Prey LEON = prey(ModEntities.ENTITY_LEON, "a Leonopteryx (port EntityLeon, a TamableAnimal)");
    private static final Prey DRAGON = prey(ModEntities.DRAGON, "a Dragon (port Dragon, a TamableAnimal)");
    private static final Prey SPYRO = prey(ModEntities.ENTITY_SPYRO, "a Spyro (port EntitySpyro, a TamableAnimal)");
    private static final Prey PRINCE = prey(ModEntities.THE_PRINCE, "a Prince (port ThePrince, a TamableAnimal — royalty, orig MyUtils.java:49)");
    private static final Prey GAMMA_METROID = prey(ModEntities.ENTITY_GAMMA_METROID, "a Gamma Metroid (port EntityGammaMetroid, a TamableAnimal)");
    private static final Prey CEPHADROME = prey(ModEntities.CEPHADROME, "a Cephadrome (port Cephadrome, a PathfinderMob)");
    private static final Prey WATER_DRAGON = prey(ModEntities.WATER_DRAGON, "a Water Dragon (port WaterDragon, a TamableAnimal)");
    private static final Prey GIRLFRIEND = prey(ModEntities.GIRLFRIEND, "a Girlfriend (a TamableAnimal)");
    private static final Prey BOYFRIEND = prey(ModEntities.BOYFRIEND, "a Boyfriend (a TamableAnimal)");
    private static final Prey VILLAGER = prey(() -> EntityType.VILLAGER, "a vanilla Villager (orig EntityVillager)");
    private static final Prey STINKY = prey(ModEntities.ENTITY_STINKY, "a Stinky (port EntityStinky, a TamableAnimal)");
    private static final Prey KRAKEN = prey(ModEntities.KRAKEN, "a Kraken (a Monster — orig EntityMob)");
    private static final Prey GODZILLA_HEAD = prey(ModEntities.GODZILLA_HEAD, "a Godzilla Head (a Mob, no Monster — orig an EntityLiving outside the list)");
    private static final Prey ENDER_DRAGON = prey(() -> EntityType.ENDER_DRAGON, "a vanilla Ender Dragon (a Mob + Enemy, no Monster — no orig counterpart in the helper)");

    // The per-species rows' prey.
    private static final Prey ANT = prey(ModEntities.ENTITY_ANT, "an ant (port EntityAnt, 0.1 wide)");
    private static final Prey BUTTERFLY = prey(ModEntities.ENTITY_BUTTERFLY, "a butterfly (port EntityButterfly, 0.4 wide)");
    private static final Prey COCKATEIL = prey(ModEntities.COCKATEIL, "a Cockateil (0.5 wide)");
    private static final Prey MOSQUITO = prey(ModEntities.ENTITY_MOSQUITO, "a mosquito (port EntityMosquito, 0.2 wide)");
    private static final Prey FIREFLY = prey(ModEntities.FIREFLY, "a Firefly (0.4 wide)");
    private static final Prey HORSE = prey(() -> EntityType.HORSE, "a vanilla Horse (an AbstractHorse, 1.4 wide — orig EntityHorse)");
    private static final Prey CHICKEN = prey(() -> EntityType.CHICKEN, "a vanilla Chicken (0.4 wide)");
    private static final Prey BAT = prey(() -> EntityType.BAT, "a vanilla Bat (an AmbientCreature, 0.5 wide)");
    private static final Prey ATTACK_SQUID = prey(ModEntities.ATTACK_SQUID, "an Attack Squid (port AttackSquid, a Monster)");
    private static final Prey SPIDER = prey(() -> EntityType.SPIDER, "a vanilla Spider");
    private static final Prey CAVE_SPIDER = prey(() -> EntityType.CAVE_SPIDER, "a vanilla Cave Spider");
    private static final Prey TAMED_WOLF = new Prey(() -> EntityType.WOLF, "a tamed vanilla Wolf (a tamed TamableAnimal — orig EntityTameable.isTamed)", TAMED);
    private static final Prey WILD_WOLF = prey(() -> EntityType.WOLF, "an untamed vanilla Wolf (a TamableAnimal, not tamed)");
    private static final Prey IRUKANDJI = prey(ModEntities.IRUKANDJI, "an Irukandji (a Monster)");
    private static final Prey SKATE = prey(ModEntities.SKATE, "a Skate (a Monster)");
    private static final Prey WHALE = prey(ModEntities.WHALE, "a Whale (an Animal)");
    private static final Prey FLOUNDER = prey(ModEntities.FLOUNDER, "a Flounder (an Animal)");
    private static final Prey RAT = prey(ModEntities.ENTITY_RAT, "a Rat (port EntityRat, a Monster)");
    private static final Prey DUNGEON_BEAST = prey(ModEntities.DUNGEON_BEAST, "a Dungeon Beast (a Monster)");
    private static final Prey ROCK_BASE = prey(ModEntities.ROCK_BASE, "a RockBase (a Mob)");
    private static final Prey TERRIBLE_TERROR = prey(ModEntities.ENTITY_TERRIBLE_TERROR, "a Terrible Terror (port EntityTerribleTerror, a Monster)");
    private static final Prey ENDER_REAPER = prey(ModEntities.ENDER_REAPER, "an Ender Reaper (a Monster)");
    private static final Prey LURKING_TERROR = prey(ModEntities.ENTITY_LURKING_TERROR, "a Lurking Terror (port EntityLurkingTerror, a Monster)");
    private static final Prey CLOUD_SHARK = prey(ModEntities.CLOUD_SHARK, "a Cloud Shark (a Monster)");
    private static final Prey ROTATOR = prey(ModEntities.ENTITY_ROTATOR, "a Rotator (port EntityRotator, a Monster)");
    private static final Prey BEE = prey(ModEntities.ENTITY_BEE, "a Bee (port EntityBee, a Monster)");
    private static final Prey MANTIS = prey(ModEntities.ENTITY_MANTIS, "a Mantis (port EntityMantis, a Monster)");
    private static final Prey LEAF_MONSTER = prey(ModEntities.ENTITY_LEAF_MONSTER, "a Leaf Monster (port EntityLeafMonster, a Monster)");
    private static final Prey CREEPING_HORROR = prey(ModEntities.CREEPING_HORROR, "a Creeping Horror (a Monster)");
    private static final Prey TRIFFID = prey(ModEntities.ENTITY_TRIFFID, "a Triffid (port EntityTriffid, a Monster)");
    private static final Prey PITCH_BLACK = prey(ModEntities.PITCH_BLACK, "a Nightmare (port PitchBlack, a Monster)");
    private static final Prey ISLAND = prey(ModEntities.ISLAND, "an Island (an Animal)");
    private static final Prey ISLAND_TOO = prey(ModEntities.ISLAND_TOO, "an IslandToo (an Animal)");
    private static final Prey CREEPER = prey(() -> EntityType.CREEPER, "a vanilla Creeper (a Monster)");
    private static final Prey ZOMBIFIED_PIGLIN = prey(() -> EntityType.ZOMBIFIED_PIGLIN, "a vanilla Zombified Piglin (a Monster — orig EntityPigZombie)");
    private static final Prey ENDERMAN = prey(() -> EntityType.ENDERMAN, "a vanilla Enderman (a Monster — orig EntityEnderman)");
    private static final Prey ENDER_KNIGHT = prey(ModEntities.ENDER_KNIGHT, "an Ender Knight (a Monster)");
    private static final Prey SCORPION = prey(ModEntities.ENTITY_SCORPION, "a Scorpion (port EntityScorpion, a Monster)");
    private static final Prey EMPEROR_SCORPION = prey(ModEntities.ENTITY_EMPEROR_SCORPION, "an Emperor Scorpion (port EntityEmperorScorpion, a Monster)");
    private static final Prey HERCULES_BEETLE = prey(ModEntities.ENTITY_HERCULES_BEETLE, "a Hercules Beetle (port EntityHerculesBeetle, a Monster)");
    private static final Prey PEACOCK = prey(ModEntities.PEACOCK, "a Peacock (an Animal)");

    // The hunters.
    private static final Hunter HELPER = new Hunter("helper", null, "MyUtils.isAttackableNonMob", EMPTY_LARGE, HUNTER_POS, PREY_POS,
            NO_SETUP, false, false, "a pig is on neither list (orig MyUtils.java:77-115)", true, "an EntityMob, orig MyUtils.java:78");
    private static final Hunter CRAB = filterHunter("crab", ModEntities.CRAB, "Crab.isSuitableTarget",
            false, "orig Crab.java:417 falls through to the helper, which never granted a pig", true, "orig Crab.java:399 EntityMob");
    private static final Hunter MANTIS_HUNTER = filterHunter("mantis", ModEntities.ENTITY_MANTIS, "EntityMantis.isSuitableTarget",
            false, "orig Mantis.java:391 falls through to the helper, which never granted a pig", true, "orig Mantis.java:373 EntityMob");
    private static final Hunter MOLENOID = new Hunter("molenoid", ModEntities.ENTITY_MOLENOID, "EntityMolenoid.isSuitableTarget", EMPTY_LARGE,
            HUNTER_POS, PREY_POS, NO_SETUP, false,
            false, "orig Molenoid.java:274 falls through to the helper, which never granted a pig", true, "orig Molenoid.java:271 EntityMob");
    private static final Hunter THE_KING = new Hunter("theking", ModEntities.THE_KING, "TheKing.isSuitableTarget", EMPTY_TALL,
            ROYAL_HUNTER_POS, ROYAL_PREY_POS, ROYAL_SETUP, false,
            false, "orig TheKing.java:981 falls through to the helper, which never granted a pig", true, "orig TheKing.java:975 EntityMob");
    private static final Hunter THE_QUEEN = new Hunter("thequeen", ModEntities.THE_QUEEN, "TheQueen.isSuitableTarget", EMPTY_TALL,
            ROYAL_HUNTER_POS, ROYAL_PREY_POS, ROYAL_SETUP, true,
            false, "orig TheQueen.java:929 falls through to the helper, which never granted a pig", true, "orig TheQueen.java:923 EntityMob");
    private static final Hunter WATER_DRAGON_HUNTER = filterHunter("waterdragon", ModEntities.WATER_DRAGON, "WaterDragon.isSuitableTarget",
            false, "orig WaterDragon.java:679 falls through to the helper, which never granted a pig (the dragon untamed)", true, "orig WaterDragon.java:669 EntityMob");
    private static final Hunter DRAGONFLY = new Hunter("dragonfly", ModEntities.ENTITY_DRAGONFLY, "DragonflyHuntGoal.findPrey", EMPTY_LARGE,
            HUNTER_POS, PREY_POS, NO_SETUP, true,
            false, "a pig is on no line of the orig whitelist (Dragonfly.java:213-228)", false, "a Zombie is on no line of the orig whitelist (Dragonfly.java:213-228)");
    private static final Hunter LIZARD = filterHunter("lizard", ModEntities.LIZARD, "Lizard.isSuitableTarget",
            false, "a pig is on no line of the orig prey ladder (Lizard.java:316-327, :331 false)", false, "a Zombie is on no line of the orig prey ladder (Lizard.java:316-327, :331 false)");
    private static final Hunter PURPLE_POWER_TYPE_0 = purplePower(0);
    private static final Hunter PURPLE_POWER_TYPE_1 = purplePower(1);
    private static final Hunter PURPLE_POWER_TYPE_10 = purplePower(10);
    private static final Hunter RAT_HUNTER = filterHunter("rat", ModEntities.ENTITY_RAT, "EntityRat.isSuitableTarget",
            true, "no orig step names a pig (Rat.java:201-224), :248 true", true, "no orig step names a Zombie (Rat.java:201-224), :248 true");
    private static final Hunter TERRIBLE_TERROR_HUNTER = filterHunter("terribleterror", ModEntities.ENTITY_TERRIBLE_TERROR, "EntityTerribleTerror.isSuitableTarget",
            true, "no orig step names a pig (TerribleTerror.java:229-285), :292 true", true, "no orig step names a Zombie (TerribleTerror.java:229-285), :292 true");
    private static final Hunter TRIFFID_HUNTER = filterHunter("triffid", ModEntities.ENTITY_TRIFFID, "EntityTriffid.isSuitableTarget",
            true, "no orig step names a pig (Triffid.java:291-311), :318 true", true,
            "no orig step names a Zombie (Triffid.java:291-311), :318 true — the row the port's blanket !Monster failed");
    private static final Hunter BOYFRIEND_HUNTER = new Hunter("boyfriend", ModEntities.BOYFRIEND, "Boyfriend's NearestAttackableTargetGoal<Mob>", EMPTY_LARGE,
            HUNTER_POS, PREY_POS, NO_SETUP, true,
            false, "a pig is no IMob (orig MyEntityAINearestAttackableTarget.java:56 mobSelector; the port's Enemy test, ENT-S-124)", true,
            "a Zombie is an IMob (orig MyEntityAINearestAttackableTarget.java:56) no rule of MyEntityAITarget.java:88-128 refuses");
    private static final Hunter GIRLFRIEND_HUNTER = new Hunter("girlfriend", ModEntities.GIRLFRIEND, "Girlfriend's NearestAttackableTargetGoal<Mob>", EMPTY_LARGE,
            HUNTER_POS, PREY_POS, NO_SETUP, true,
            false, "a pig is no IMob (orig MyEntityAINearestAttackableTarget.java:56 mobSelector; the port's Enemy test, ENT-S-124)", true,
            "a Zombie is an IMob (orig MyEntityAINearestAttackableTarget.java:56) no rule of MyEntityAITarget.java:88-128 refuses");
    private static final Hunter DUNGEON_BEAST_HUNTER = filterHunter("dungeonbeast", ModEntities.DUNGEON_BEAST, "DungeonBeast.isSuitableTarget",
            true, "no orig step names a pig (DungeonBeast.java:216-239), :246 true", true, "no orig step names a Zombie (DungeonBeast.java:216-239), :246 true");
    private static final Hunter EMPEROR_SCORPION_HUNTER = filterHunter("emperorscorpion", ModEntities.ENTITY_EMPEROR_SCORPION, "EntityEmperorScorpion.isSuitableTarget",
            true, "no orig step names a pig (EmperorScorpion.java:476-493), :500 true", true, "no orig step names a Zombie (EmperorScorpion.java:476-493), :500 true");
    private static final Hunter HERCULES_BEETLE_HUNTER = filterHunter("herculesbeetle", ModEntities.ENTITY_HERCULES_BEETLE, "EntityHerculesBeetle.isSuitableTarget",
            true, "no orig step names a pig (HerculesBeetle.java:401-406), :413 true", true, "no orig step names a Zombie (HerculesBeetle.java:401-406), :413 true");

    /** The Purple Power with its orb type set (orig PurplePower.java:261 reads it thrice; the port once). The controls carry no type rule. */
    private static Hunter purplePower(int type) {
        return new Hunter("purplepower", ModEntities.PURPLE_POWER, "PurplePower.isSuitableTarget (type " + type + ")", EMPTY_LARGE,
                HUNTER_POS, PREY_POS, mob -> ((PurplePower) mob).setPurpleType(type), true,
                true, "no orig step names a pig (PurplePower.java:261-264): not a tamed pet, not royalty", true,
                "no orig step names a Zombie (PurplePower.java:261-264): not a tamed pet, not royalty");
    }

    /** The helper's members through one caller: only the pairs the caller's own chain leaves to the helper, in orig MyUtils.java order. */
    private static void helperThrough(Rows rows, Hunter caller, String callerOrig, boolean mothra, boolean prince, boolean waterDragon,
                                      boolean girlfriendBoyfriendVillager, boolean enderDragon) {
        String via = callerOrig + " -> MyUtils.java:";
        if (mothra) rows.add(Shape.FILTER, caller, "mothra_81", MOTHRA, true, via + "81", "granted through the helper's Mothra term (orig MyUtils.java:81)");
        rows.add(Shape.FILTER, caller, "leon_84", LEON, true, via + "84", "granted through the helper's Leon term (orig MyUtils.java:84)");
        rows.add(Shape.FILTER, caller, "dragon_87", DRAGON, true, via + "87", "granted through the helper's Dragon term (orig MyUtils.java:87)");
        rows.add(Shape.FILTER, caller, "spyro_90", SPYRO, true, via + "90", "granted through the helper's Spyro term (orig MyUtils.java:90)");
        if (prince) rows.add(Shape.FILTER, caller, "prince_royalty_93", PRINCE, true, via + "93", "granted through the helper's isRoyalty term (orig MyUtils.java:93, :49 ThePrince)");
        rows.add(Shape.FILTER, caller, "gamma_metroid_96", GAMMA_METROID, true, via + "96", "granted through the helper's GammaMetroid term (orig MyUtils.java:96)");
        rows.add(Shape.FILTER, caller, "cephadrome_99", CEPHADROME, true, via + "99", "granted through the helper's Cephadrome term (orig MyUtils.java:99)");
        if (waterDragon) rows.add(Shape.FILTER, caller, "water_dragon_102", WATER_DRAGON, true, via + "102", "granted through the helper's WaterDragon term (orig MyUtils.java:102)");
        if (girlfriendBoyfriendVillager) {
            rows.add(Shape.FILTER, caller, "girlfriend_105", GIRLFRIEND, true, via + "105", "granted through the helper's Girlfriend term (orig MyUtils.java:105)");
            rows.add(Shape.FILTER, caller, "boyfriend_108", BOYFRIEND, true, via + "108", "granted through the helper's Boyfriend term (orig MyUtils.java:108)");
            rows.add(Shape.FILTER, caller, "villager_111", VILLAGER, true, via + "111", "granted through the helper's EntityVillager term (orig MyUtils.java:111)");
        }
        rows.add(Shape.FILTER, caller, "stinky_114", STINKY, true, via + "114", "granted through the helper's Stinky term (orig MyUtils.java:114)");
        rows.add(Shape.FILTER, caller, "godzilla_head_port_only", GODZILLA_HEAD, false, via + "77-115",
                "refused: a Godzilla Head was an EntityLiving on no line of orig MyUtils.java:77-115 — the port-only grant is gone");
        if (enderDragon) rows.add(Shape.FILTER, caller, "ender_dragon_port_only", ENDER_DRAGON, false, via + "77-115",
                "refused: the Ender Dragon is no EntityMob and on no line of orig MyUtils.java:77-115 — the port-only grant is gone");
    }

    private static List<Row> rows() {
        Rows rows = new Rows();

        // (i) the shared helper, driven directly — orig MyUtils.java:77-115 in the original's order
        rows.add(Shape.HELPER, HELPER, "skeleton_entity_mob_78", SKELETON, true, "MyUtils.java:78", "granted: EntityMob → Monster");
        rows.add(Shape.HELPER, HELPER, "mothra_81", MOTHRA, true, "MyUtils.java:81", "granted: Mothra");
        rows.add(Shape.HELPER, HELPER, "leon_84", LEON, true, "MyUtils.java:84", "granted: Leon → EntityLeon");
        rows.add(Shape.HELPER, HELPER, "dragon_87", DRAGON, true, "MyUtils.java:87", "granted: Dragon");
        rows.add(Shape.HELPER, HELPER, "spyro_90", SPYRO, true, "MyUtils.java:90", "granted: Spyro → EntitySpyro");
        rows.add(Shape.HELPER, HELPER, "prince_royalty_93", PRINCE, true, "MyUtils.java:93", "granted: isRoyalty (orig :49 ThePrince)");
        rows.add(Shape.HELPER, HELPER, "gamma_metroid_96", GAMMA_METROID, true, "MyUtils.java:96", "granted: GammaMetroid → EntityGammaMetroid");
        rows.add(Shape.HELPER, HELPER, "cephadrome_99", CEPHADROME, true, "MyUtils.java:99", "granted: Cephadrome");
        rows.add(Shape.HELPER, HELPER, "water_dragon_102", WATER_DRAGON, true, "MyUtils.java:102", "granted: WaterDragon");
        rows.add(Shape.HELPER, HELPER, "girlfriend_105", GIRLFRIEND, true, "MyUtils.java:105", "granted: Girlfriend");
        rows.add(Shape.HELPER, HELPER, "boyfriend_108", BOYFRIEND, true, "MyUtils.java:108", "granted: Boyfriend");
        rows.add(Shape.HELPER, HELPER, "villager_111", VILLAGER, true, "MyUtils.java:111", "granted: EntityVillager → Villager");
        rows.add(Shape.HELPER, HELPER, "stinky_114", STINKY, true, "MyUtils.java:114", "granted: Stinky → EntityStinky");
        rows.add(Shape.HELPER, HELPER, "kraken_entity_mob_78", KRAKEN, true, "MyUtils.java:78",
                "granted: a Kraken was an EntityMob (orig Kraken.java:55) — kept through the Monster term, not by name");
        rows.add(Shape.HELPER, HELPER, "godzilla_head_port_only", GODZILLA_HEAD, false, "MyUtils.java:77-115",
                "refused: an EntityLiving on no line of the orig list — the port-only grant is gone");
        rows.add(Shape.HELPER, HELPER, "ender_dragon_port_only", ENDER_DRAGON, false, "MyUtils.java:77-115",
                "refused: no EntityMob and on no line of the orig list — the port-only grant is gone");

        // (i) the helper through each HEAD caller, wherever the caller's own chain leaves the verdict to the helper
        helperThrough(rows, CRAB, "Crab.java:417", true, true, true, false, true);           // Villager / Girlfriend / Boyfriend: Crab.java:408-416's own grants
        helperThrough(rows, MANTIS_HUNTER, "Mantis.java:391", false, true, false, true, true); // Mothra :370, WaterDragon :349: the Mantis's own refusals
        helperThrough(rows, MOLENOID, "Molenoid.java:274", true, true, true, true, true);
        helperThrough(rows, THE_KING, "TheKing.java:981", false, false, true, true, false);   // Mothra: ignore screen :947; royalty :939; EntityDragon :978 pre-granted
        helperThrough(rows, THE_QUEEN, "TheQueen.java:929", false, false, true, true, false); // Mothra: ignore screen :910; royalty :902; EntityDragon :926 pre-granted
        helperThrough(rows, WATER_DRAGON_HUNTER, "WaterDragon.java:679", true, true, false, true, true); // WaterDragon: self-kind :666

        // (ii) Dragonfly — orig Dragonfly.java:213-228, the whitelist for the port's width rule
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "ant_213", ANT, true, "Dragonfly.java:213-215", "granted: EntityAnt");
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "butterfly_216", BUTTERFLY, true, "Dragonfly.java:216-218", "granted: EntityButterfly");
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "cockateil_219", COCKATEIL, true, "Dragonfly.java:219-221", "granted: Cockateil");
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "mosquito_222", MOSQUITO, true, "Dragonfly.java:222-224", "granted: EntityMosquito");
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "firefly_225", FIREFLY, true, "Dragonfly.java:225-227", "granted: Firefly");
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "horse_228_toggle_off", HORSE, true, "Dragonfly.java:228",
                "granted: EntityHorse with DragonflyHorseFriendly == 0 — 1.4 wide, the port's width rule never took it", false);
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "horse_228_toggle_on", HORSE, false, "Dragonfly.java:228",
                "refused: EntityHorse with DragonflyHorseFriendly set (the port's dragonflyHorseFriendly, read live)", true);
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "chicken_width_rule", CHICKEN, false, "Dragonfly.java:213-228",
                "refused: a Chicken is on no line of the whitelist — the port's bbWidth <= 0.6 rule took it");
        rows.add(Shape.DRAGONFLY, DRAGONFLY, "bat_width_rule", BAT, false, "Dragonfly.java:213-228",
                "refused: a Bat is an AmbientCreature on no line of the whitelist — the port's bbWidth <= 0.6 rule took it");

        // (ii) Lizard — orig Lizard.java:316-327
        rows.add(Shape.FILTER, LIZARD, "attack_squid_316", ATTACK_SQUID, true, "Lizard.java:316-318", "granted: AttackSquid heads the prey ladder — the port had dropped it");
        rows.add(Shape.FILTER, LIZARD, "spider_319", SPIDER, true, "Lizard.java:319-321", "granted: EntitySpider");
        rows.add(Shape.FILTER, LIZARD, "cave_spider_322", CAVE_SPIDER, true, "Lizard.java:322-324", "granted: EntityCaveSpider");
        rows.add(Shape.FILTER, LIZARD, "chicken_325", CHICKEN, true, "Lizard.java:325-327", "granted: EntityChicken");

        // (ii) PurplePower — orig PurplePower.java:261-264
        rows.add(Shape.FILTER, PURPLE_POWER_TYPE_1, "tamed_wolf_type_1_261", TAMED_WOLF, false, "PurplePower.java:261-263",
                "refused: type 1 is neither 0 nor 10, so a tamed EntityTameable is spared");
        rows.add(Shape.FILTER, PURPLE_POWER_TYPE_0, "tamed_wolf_type_0_261", TAMED_WOLF, true, "PurplePower.java:261-264",
                "granted: type 0 skips the tamed-pet step, and a Wolf is no royalty");
        rows.add(Shape.FILTER, PURPLE_POWER_TYPE_10, "tamed_wolf_type_10_261", TAMED_WOLF, true, "PurplePower.java:261-264",
                "granted: type 10 skips the tamed-pet step, and a Wolf is no royalty");
        rows.add(Shape.FILTER, PURPLE_POWER_TYPE_1, "wild_wolf_type_1_261", WILD_WOLF, true, "PurplePower.java:261-264",
                "granted: an untamed Wolf fails the isTamed half of :261, and is no royalty");
        rows.add(Shape.FILTER, PURPLE_POWER_TYPE_0, "prince_royalty_264", PRINCE, false, "PurplePower.java:264",
                "refused: return !isRoyalty — a Prince is royalty (orig MyUtils.java:49)");

        // (ii) Rat — orig Rat.java:201-224
        rows.add(Shape.FILTER, RAT_HUNTER, "irukandji_201", IRUKANDJI, false, "Rat.java:201-203", "refused: Irukandji");
        rows.add(Shape.FILTER, RAT_HUNTER, "skate_204", SKATE, false, "Rat.java:204-206", "refused: Skate");
        rows.add(Shape.FILTER, RAT_HUNTER, "whale_207", WHALE, false, "Rat.java:207-209", "refused: Whale");
        rows.add(Shape.FILTER, RAT_HUNTER, "flounder_210", FLOUNDER, false, "Rat.java:210-212", "refused: Flounder");
        rows.add(Shape.FILTER, RAT_HUNTER, "rat_213", RAT, false, "Rat.java:213-215", "refused: Rat");
        rows.add(Shape.FILTER, RAT_HUNTER, "dungeon_beast_222", DUNGEON_BEAST, false, "Rat.java:222-224", "refused: DungeonBeast");

        // (ii) TerribleTerror — orig TerribleTerror.java:229-285
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "rock_base_229", ROCK_BASE, false, "TerribleTerror.java:229-231", "refused: RockBase");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "terrible_terror_232", TERRIBLE_TERROR, false, "TerribleTerror.java:232-234", "refused: TerribleTerror");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "ender_reaper_235", ENDER_REAPER, false, "TerribleTerror.java:235-237", "refused: EnderReaper");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "mothra_238", MOTHRA, false, "TerribleTerror.java:238-240", "refused: Mothra — the port hunted her");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "lurking_terror_241", LURKING_TERROR, false, "TerribleTerror.java:241-243", "refused: LurkingTerror — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "cloud_shark_244", CLOUD_SHARK, false, "TerribleTerror.java:244-246", "refused: CloudShark");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "rotator_247", ROTATOR, false, "TerribleTerror.java:247-249", "refused: Rotator");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "bee_250", BEE, false, "TerribleTerror.java:250-252", "refused: Bee — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "mantis_253", MANTIS, false, "TerribleTerror.java:253-255", "refused: Mantis — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "leaf_monster_256", LEAF_MONSTER, false, "TerribleTerror.java:256-258", "refused: LeafMonster — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "creeping_horror_259", CREEPING_HORROR, false, "TerribleTerror.java:259-261", "refused: CreepingHorror");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "triffid_262", TRIFFID, false, "TerribleTerror.java:262-264", "refused: Triffid — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "pitch_black_265", PITCH_BLACK, false, "TerribleTerror.java:265-267", "refused: PitchBlack");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "dragon_268", DRAGON, false, "TerribleTerror.java:268-270", "refused: Dragon — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "island_271", ISLAND, false, "TerribleTerror.java:271-273", "refused: Island");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "island_too_274", ISLAND_TOO, false, "TerribleTerror.java:274-276", "refused: IslandToo");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "butterfly_277", BUTTERFLY, false, "TerribleTerror.java:277-279", "refused: EntityButterfly — the port hunted it");
        rows.add(Shape.FILTER, TERRIBLE_TERROR_HUNTER, "firefly_280", FIREFLY, false, "TerribleTerror.java:280-282", "refused: Firefly — the port hunted it");

        // (ii) Triffid — orig Triffid.java:291-311; the Zombie control is the row the blanket !Monster failed
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "creeper_291", CREEPER, false, "Triffid.java:291-293", "refused: EntityCreeper");
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "ender_reaper_294", ENDER_REAPER, false, "Triffid.java:294-296", "refused: EnderReaper");
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "triffid_297", TRIFFID, false, "Triffid.java:297-299", "refused: Triffid");
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "terrible_terror_300", TERRIBLE_TERROR, false, "Triffid.java:300-302", "refused: TerribleTerror");
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "lurking_terror_303", LURKING_TERROR, false, "Triffid.java:303-305", "refused: LurkingTerror");
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "pitch_black_306", PITCH_BLACK, false, "Triffid.java:306-308", "refused: PitchBlack");
        rows.add(Shape.FILTER, TRIFFID_HUNTER, "dragon_309", DRAGON, false, "Triffid.java:309-311", "refused: Dragon (a tameable, no Monster) — the port hunted it");

        // (iii) Boyfriend / Girlfriend — orig MyEntityAITarget.java:99-116 behind the :56 IMob pre-filter
        for (Hunter companion : List.of(BOYFRIEND_HUNTER, GIRLFRIEND_HUNTER)) {
            rows.add(Shape.GOAL, companion, "zombified_piglin_99", ZOMBIFIED_PIGLIN, false, "MyEntityAITarget.java:99-101",
                    "refused: EntityPigZombie → false — the port's Enemy test alone took it");
            rows.add(Shape.GOAL, companion, "enderman_102", ENDERMAN, false, "MyEntityAITarget.java:102-104",
                    "refused: EntityEnderman → false — the port's Enemy test alone took it");
            rows.add(Shape.GOAL, companion, "mothra_105", MOTHRA, true, "MyEntityAITarget.java:105-107",
                    "taken: Mothra → true ahead of the sight step — an IMob in 1.7.10 (orig Mothra.java:52), an EntityButterfly the port's Enemy test refused");
            rows.add(Shape.GOAL, companion, "creeper_111", CREEPER, true, "MyEntityAITarget.java:111-113", "taken: EntityCreeper → true");
        }

        // (iv) the ENT-S-108 residuals — present at HEAD, pinned
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "rat_216", RAT, false, "DungeonBeast.java:216-218", "refused: Rat");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "dungeon_beast_219", DUNGEON_BEAST, false, "DungeonBeast.java:219-221", "refused: DungeonBeast");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "rotator_222", ROTATOR, false, "DungeonBeast.java:222-224", "refused: Rotator");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "peacock_225", PEACOCK, false, "DungeonBeast.java:225-227", "refused: Peacock");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "irukandji_228", IRUKANDJI, false, "DungeonBeast.java:228-230", "refused: Irukandji");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "skate_231", SKATE, false, "DungeonBeast.java:231-233", "refused: Skate");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "whale_234", WHALE, false, "DungeonBeast.java:234-236", "refused: Whale");
        rows.add(Shape.FILTER, DUNGEON_BEAST_HUNTER, "flounder_237", FLOUNDER, false, "DungeonBeast.java:237-239", "refused: Flounder");
        rows.add(Shape.FILTER, EMPEROR_SCORPION_HUNTER, "enderman_476", ENDERMAN, false, "EmperorScorpion.java:476-478", "refused: EntityEnderman");
        rows.add(Shape.FILTER, EMPEROR_SCORPION_HUNTER, "ender_knight_479", ENDER_KNIGHT, false, "EmperorScorpion.java:479-481", "refused: EnderKnight");
        rows.add(Shape.FILTER, EMPEROR_SCORPION_HUNTER, "ender_reaper_482", ENDER_REAPER, false, "EmperorScorpion.java:482-484", "refused: EnderReaper");
        rows.add(Shape.FILTER, EMPEROR_SCORPION_HUNTER, "creeper_485", CREEPER, false, "EmperorScorpion.java:485-487", "refused: EntityCreeper");
        rows.add(Shape.FILTER, EMPEROR_SCORPION_HUNTER, "scorpion_488", SCORPION, false, "EmperorScorpion.java:488-490", "refused: Scorpion");
        rows.add(Shape.FILTER, EMPEROR_SCORPION_HUNTER, "emperor_scorpion_491", EMPEROR_SCORPION, false, "EmperorScorpion.java:491-493", "refused: EmperorScorpion");
        rows.add(Shape.FILTER, HERCULES_BEETLE_HUNTER, "creeper_401", CREEPER, false, "HerculesBeetle.java:401-403", "refused: EntityCreeper");
        rows.add(Shape.FILTER, HERCULES_BEETLE_HUNTER, "hercules_beetle_404", HERCULES_BEETLE, false, "HerculesBeetle.java:404-406", "refused: HerculesBeetle");

        return rows.rows;
    }

    /** One test per row, all in the {@code preyListParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> preyListRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), row.hunter().template(), Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, row)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: the species as orig says, then the pig and the Zombie on the same spot as orig says
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Row row) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL — the Dragonfly, Lizard, Purple Power and Water Dragon filters"
                        + " refuse everything on Peaceful (" + FINDING + " test setup)");
        helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                "precondition: playNicely must be off (the Dragonfly scan and the companion goals answer nothing under it,"
                        + " ENT-S-115); a batch-mate left it raised (" + FINDING + " test setup)");
        final Boolean priorHorse = row.horseFriendly() == null ? null : OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.get();
        Driver driver = null;
        Mob species = null;
        Mob control = null;
        try {
            if (row.horseFriendly() != null) {
                OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.set(row.horseFriendly());
                helper.assertTrue(OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.get() == row.horseFriendly(),
                        "precondition: DRAGONFLY_HORSE_FRIENDLY.set(" + row.horseFriendly() + ") must read back (" + FINDING + " test setup)");
            }
            driver = driverFor(helper, row);
            String where = row.where();

            species = spawnFrozen(helper, row.prey().type().get(), row.hunter().preyPos());
            row.prey().setup().accept(species);
            driver.assertSees(helper, species, row.prey().description());
            boolean actual = driver.test(species);
            helper.assertTrue(actual == row.expected(), where + ": " + row.prey().description() + " — " + row.why()
                    + " — expected " + verdict(row.expected()) + ", got " + verdict(actual) + driver.trace() + " (" + FINDING + ")");
            species.discard();
            species = null;

            control = spawnFrozen(helper, PIG.type().get(), row.hunter().preyPos());
            driver.assertSees(helper, control, PIG.description());
            boolean pig = driver.test(control);
            helper.assertTrue(pig == row.hunter().pigExpected(), "control: " + where + " with " + PIG.description() + " on the same spot"
                    + " — " + row.hunter().pigWhy() + " — expected " + verdict(row.hunter().pigExpected()) + ", got " + verdict(pig)
                    + driver.trace() + " (" + FINDING + ")");
            control.discard();
            control = null;

            control = spawnFrozen(helper, ZOMBIE.type().get(), row.hunter().preyPos());
            helper.assertTrue(control instanceof Monster, "precondition: a Zombie is a Monster (" + FINDING + " test setup)");
            driver.assertSees(helper, control, ZOMBIE.description());
            boolean zombie = driver.test(control);
            helper.assertTrue(zombie == row.hunter().zombieExpected(), "control: " + where + " with " + ZOMBIE.description()
                    + " on the same spot — " + row.hunter().zombieWhy() + " — expected " + verdict(row.hunter().zombieExpected())
                    + ", got " + verdict(zombie) + driver.trace() + " (" + FINDING + ")");
        } finally {
            if (priorHorse != null) OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.set(priorHorse);
            discardQuietly(control);
            discardQuietly(species);
            if (driver != null) driver.cleanUp();
        }
        helper.succeed();
    }

    private static String verdict(boolean granted) {
        return granted ? "granted" : "refused";
    }

    // ------------------------------------------------------------------
    // Drivers: how the row reaches the port's filter
    // ------------------------------------------------------------------

    private interface Driver {
        boolean test(LivingEntity candidate);

        /** The geometry precondition where the hunter's own sight step is vanilla's eye-to-eye ray. */
        void assertSees(GameTestHelper helper, LivingEntity candidate, String what);

        /** What the last drive saw, for the message. */
        default String trace() {
            return "";
        }

        void cleanUp();
    }

    private static Driver driverFor(GameTestHelper helper, Row row) {
        return switch (row.shape()) {
            case HELPER -> new HelperDriver();
            case FILTER -> new FilterDriver(helper, row.hunter());
            case DRAGONFLY -> new DragonflyDriver(helper, row.hunter());
            case GOAL -> new GoalDriver(helper, row.hunter());
        };
    }

    /** {@code MyUtils.isAttackableNonMob} itself. */
    private static final class HelperDriver implements Driver {
        @Override
        public boolean test(LivingEntity candidate) {
            return MyUtils.isAttackableNonMob(candidate);
        }

        @Override
        public void assertSees(GameTestHelper helper, LivingEntity candidate, String what) {
        }

        @Override
        public void cleanUp() {
        }
    }

    /** The hunter frozen on its spot; its private one-arg {@code isSuitableTarget} by reflection (the PitchBlackAllyTests idiom). */
    private static final class FilterDriver implements Driver {
        private final Hunter kind;
        private final Mob hunter;

        FilterDriver(GameTestHelper helper, Hunter kind) {
            this.kind = kind;
            this.hunter = spawnFrozen(helper, kind.type().get(), kind.hunterPos());
            kind.setup().accept(this.hunter);
        }

        @Override
        public boolean test(LivingEntity candidate) {
            return invokeFilter(this.hunter, candidate);
        }

        @Override
        public void assertSees(GameTestHelper helper, LivingEntity candidate, String what) {
            if (this.kind.vanillaSight()) assertLineOfSight(helper, this.hunter, candidate, what);
        }

        @Override
        public void cleanUp() {
            discardQuietly(this.hunter);
        }
    }

    /** The dragonfly frozen; the goal's private {@code findPrey} scan (PlayNicelyGateParityTests idiom): the row's species is the pick or nothing is. */
    private static final class DragonflyDriver implements Driver {
        private final Mob hunter;
        private final DragonflyHuntGoal goal;
        private String trace = "";

        DragonflyDriver(GameTestHelper helper, Hunter kind) {
            this.hunter = spawnFrozen(helper, kind.type().get(), kind.hunterPos());
            this.goal = (DragonflyHuntGoal) readField(this.hunter, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(this.goal != null,
                    "precondition: EntityDragonfly.registerGoals must have built the hunt goal (" + FINDING + " test setup)");
        }

        @Override
        public boolean test(LivingEntity candidate) {
            Object found = invoke(this.goal, DragonflyHuntGoal.class, "findPrey");
            this.trace = " [findPrey -> " + describe((Entity) found) + "]";
            if (found != null && found != candidate) {
                throw new IllegalStateException("DragonflyHuntGoal.findPrey picked " + describe((Entity) found) + " — not the row's "
                        + describe(candidate) + " (" + FINDING + " test setup)");
            }
            return found == candidate;
        }

        @Override
        public void assertSees(GameTestHelper helper, LivingEntity candidate, String what) {
            assertLineOfSight(helper, this.hunter, candidate, what);
        }

        @Override
        public String trace() {
            return this.trace;
        }

        @Override
        public void cleanUp() {
            discardQuietly(this.hunter);
        }
    }

    /**
     * The companion spawned with its goals and no AI; its {@code NearestAttackableTargetGoal} of target type {@code Mob}
     * (the ENT-S-124 form) read off the target selector and asked {@code canUse()} under a forced {@code Entity.random}
     * pinning the 1-in-5 acquisition roll (the IMobConventionTests idiom); the pick read back from the goal's {@code target}.
     */
    private static final class GoalDriver implements Driver {
        private final Mob hunter;
        private final NearestAttackableTargetGoal<?> goal;
        private String trace = "";

        GoalDriver(GameTestHelper helper, Hunter kind) {
            this.hunter = spawnWithGoals(helper, kind.type().get(), kind.hunterPos());
            this.hunter.setOnGround(true); // a frozen mob never lands; the companions' nearbyOnly reach cache (ENT-S-135, TargetGoal.canReach) paths through GroundPathNavigation.canUpdatePath, which needs the ground (the T5 refuter B1 precedent)
            replaceRandom(this.hunter, rolls(GOAL_ROLL_BOUND, 0));
            NearestAttackableTargetGoal<?> found = null;
            int matching = 0;
            for (WrappedGoal wrapped : this.hunter.targetSelector.getAvailableGoals()) {
                Goal candidate = wrapped.getGoal();
                if (candidate instanceof NearestAttackableTargetGoal<?> nearest
                        && readField(nearest, NearestAttackableTargetGoal.class, "targetType") == Mob.class) {
                    found = nearest;
                    matching++;
                }
            }
            helper.assertTrue(matching == 1, "precondition: " + this.hunter.getClass().getSimpleName() + " must carry exactly one"
                    + " NearestAttackableTargetGoal<Mob> on its target selector — the ENT-S-124 form of the orig monster task"
                    + " (" + FINDING + " test setup); found " + matching);
            this.goal = found;
        }

        @Override
        public boolean test(LivingEntity candidate) {
            boolean can = this.goal.canUse();
            Object pick = readField(this.goal, NearestAttackableTargetGoal.class, "target");
            this.trace = " [canUse=" + can + ", target=" + describe((Entity) pick) + "]";
            if (pick != null && pick != candidate) {
                throw new IllegalStateException(this.hunter.getClass().getSimpleName() + "'s goal took " + describe((Entity) pick)
                        + " — not the row's " + describe(candidate) + " (" + FINDING + " test setup)");
            }
            return can && pick == candidate;
        }

        @Override
        public void assertSees(GameTestHelper helper, LivingEntity candidate, String what) {
            assertLineOfSight(helper, this.hunter, candidate, what);
        }

        @Override
        public String trace() {
            return this.trace;
        }

        @Override
        public void cleanUp() {
            discardQuietly(this.hunter);
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the PitchBlackAllyTests / PlayNicelyGateParityTests idiom)
    // ------------------------------------------------------------------

    private static void assertLineOfSight(GameTestHelper helper, Mob hunter, LivingEntity target, String what) {
        helper.assertTrue(hunter.hasLineOfSight(target), "precondition: the " + hunter.getClass().getSimpleName() + " (eye "
                + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see " + what
                + " on the prey spot inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** With its registered goals (the target selector is the site under test) but no AI, so nothing runs. */
    private static Mob spawnWithGoals(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam. */
    private static RandomSource rolls(int... boundAnswerPairs) {
        RandomSource source = RandomSource.create(1234L);
        for (int i = 0; i < boundAnswerPairs.length; i += 2) {
            source = new VortexParityTests.ForcedRoll(source, boundAnswerPairs[i], boundAnswerPairs[i + 1]);
        }
        return source;
    }

    /** Same seam as VortexParityTests.forceDiscardRoll: swap {@code Entity.random} for a forced source. */
    private static void replaceRandom(Entity entity, RandomSource forced) {
        try {
            Field field = Entity.class.getDeclaredField("random");
            field.setAccessible(true);
            field.set(entity, forced);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot replace Entity.random", exception);
        }
    }

    /** The hunter's private {@code isSuitableTarget(LivingEntity)} — the port's one-arg shape of the orig two-arg method. */
    private static boolean invokeFilter(Mob hunter, LivingEntity candidate) {
        String name = hunter.getClass().getSimpleName() + ".isSuitableTarget";
        try {
            Method method = hunter.getClass().getDeclaredMethod("isSuitableTarget", LivingEntity.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(hunter, candidate);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(name + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + name, exception);
        }
    }

    private static Object invoke(Object target, Class<?> declaring, String name) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(where + " threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke " + where, exception);
        }
    }

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    private static void writeInt(Mob mob, String name, int value) {
        String owner = mob.getClass().getSimpleName();
        try {
            Field field = mob.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.setInt(mob, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + owner + "." + name, exception);
        }
    }

    private static String describe(Entity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }
}
