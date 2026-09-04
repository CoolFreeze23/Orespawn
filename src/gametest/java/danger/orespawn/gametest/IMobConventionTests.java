package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-124 — the IMob convention (owner's ruling, 2026-09-04: "Convention: {@code Mob} + {@code Enemy}, port-wide,
 * wherever 1.7.10 tested IMob"). 1.7.10's {@code IMob} was an interface — every {@code EntityMob} plus
 * {@code EntitySlime} (and the Magma Cube), {@code EntityGhast}, {@code EntityDragon} and OreSpawn's own Mothra (orig
 * Mothra.java:52) — and the six vanilla target tasks that filtered on {@code IMob.mobSelector} (orig Dragon.java:116,
 * Leon.java:93, ThePrinceAdult.java:113, ThePrinceTeen.java:117, Boyfriend.java:141, Girlfriend.java:167) had been
 * ported as {@code NearestAttackableTargetGoal<Monster>}, which drops every hostile that is no {@code Monster}
 * subclass. The ruled form is a {@code Mob}-typed goal with an {@code instanceof Enemy} selector (vanilla's marker
 * interface for hostile mobs: Slime, MagmaCube, Ghast, EnderDragon, Shulker, Phantom, Hoglin implement it alongside
 * every Monster), and'ed ahead of the site's own predicate where it has one (the Leon's tame rule) and under the
 * ENT-S-115 live PlayNicely {@code canUse} gate, kept as it was. The Ghast is still refused — by vanilla
 * {@code Mob.canAttackType} ({@code type != GHAST}, applied in {@code TargetingConditions.test}), as 1.7.10's
 * {@code EntityAITarget} refused it through {@code canAttackClass}; nothing special-cases it.
 *
 * <p>A {@link GameTestGenerator} over {@link #rows()} in orig file order, one synchronous {@link TestFunction} per
 * row, {@code imobconventiontests.s124_NN_<species>_<prey>}. Per site the goal is read off the hunter's target
 * selector by its target type ({@code targetType == Mob.class}; no {@code Monster}-typed goal may remain) and asked
 * {@code canUse()} directly under a forced {@code Entity.random} (the VortexParityTests.ForcedRoll seam, the goal's
 * 1-in-5 acquisition roll pinned to fire; the Dragon's interval 0 rolls nothing) with, in turn, a Slime 8 blocks off
 * (an Enemy that is no Monster — taken, the goal's pick read back; this row fails on the old Monster form), a Zombie
 * (taken), a pig (refused), and the Slime again under the flipped PlayNicely flag (refused — the ENT-S-115 gate
 * composes over the selector); the Leon's extra row proves its port-only tame rule ({@code !isTame() ||
 * getTarget() == null} — MOD-033's modern branch since 2026-09-05; the game-test config runs modern, and the classic
 * form is pinned per mode in PortOnlyTargetingTests) still composes: tamed and holding a target it refuses the Slime,
 * with the slot emptied it takes her. Geometry as PlayNicelyGateParityTests: the hunter spawned with its goals and no AI at rel (20,1,24) on
 * the floor of the 48x16x48 empty_large, the prey frozen 8 blocks east (inside every follow range of the six: the
 * Dragon's pinned 16, the Leon's 40, the Princes' 64 / 32, the Boyfriend's / Girlfriend's default 16), line of sight
 * asserted. The flag restored in a finally on every path; every spawn discarded there; no mock players. Own batch
 * (TEST-003).</p>
 *
 * <p>ENT-S-127 (the {@code s127_NN_<species>_creeper} rows, T5b): 1.7.10's {@code EntityAITarget.isSuitableTarget} asked
 * {@code EntityLiving.canAttackClass} — {@code cls != EntityCreeper.class && cls != EntityGhast.class}, the only body in the
 * 1.7.10 jar — of every candidate ahead of the task's selector, so the four vanilla tasks (Dragon, Leon, both Princes) never
 * took a Creeper; the port's four goals read {@code OrigTargets.vanillaTaskPrey} (Enemy less the Creeper) and these rows ask
 * each of them {@code canUse()} with a Creeper 8 blocks off — refused — beside the Zombie row above that takes (the control).
 * The Boyfriend / Girlfriend goals carry no such row: orig {@code MyEntityAITarget.isSuitableTarget} never called
 * {@code canAttackClass} and granted the Creeper explicitly (MyEntityAITarget.java:111).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class IMobConventionTests {

    private static final String BATCH = "imobConvention";
    private static final String TEST_PREFIX = "imobconventiontests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (PlayNicelyGateParityTests). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;
    private static final String FINDING = "ENT-S-124";
    private static final String FINDING_CREEPER = "ENT-S-127";

    /** The hunter on the template floor. */
    private static final BlockPos HUNTER_POS = new BlockPos(20, 1, 24);
    /** 8 blocks east: inside every follow range of the six goals. */
    private static final BlockPos PREY_POS = new BlockPos(28, 1, 24);
    /** Prey health, high enough that nothing incidental kills it. */
    private static final float PREY_HEALTH = 1000.0f;
    /** The vanilla goal's acquisition roll: {@code NearestAttackableTargetGoal} reduces its 10-tick interval to {@code nextInt(5) != 0 → skip}. */
    private static final int GOAL_ROLL_BOUND = 5;

    // ------------------------------------------------------------------
    // The row table, in orig file order
    // ------------------------------------------------------------------

    /** One orig IMob site and the port goal that carries it. */
    private record Hunter(String species, Supplier<? extends EntityType<? extends Mob>> type, String orig, String port) {
    }

    /** What a prey is to 1.7.10's IMob selector and to the port's Enemy test. */
    private enum Prey {
        SLIME("slime", () -> EntityType.SLIME, true,
                "a Slime 8 blocks off — IMob in 1.7.10 (EntitySlime implements IMob), an Enemy and no Monster here"),
        ZOMBIE("zombie", () -> EntityType.ZOMBIE, true,
                "a Zombie 8 blocks off — an EntityMob, IMob in 1.7.10, a Monster and an Enemy here"),
        PIG("pig", () -> EntityType.PIG, false,
                "a pig 8 blocks off — never IMob, no Enemy"),
        CREEPER("creeper", () -> EntityType.CREEPER, true,
                "a Creeper 8 blocks off — an EntityMob and IMob in 1.7.10, refused by EntityLiving.canAttackClass for every vanilla"
                        + " target task; a Monster and an Enemy here, refused by OrigTargets.vanillaTaskPrey (ENT-S-127)");

        final String tag;
        final Supplier<EntityType<? extends Mob>> type;
        final boolean enemy;
        final String description;

        Prey(String tag, Supplier<EntityType<? extends Mob>> type, boolean enemy, String description) {
            this.tag = tag;
            this.type = type;
            this.enemy = enemy;
            this.description = description;
        }
    }

    /** What a row drives: the prey taken, refused, taken then refused under PlayNicely, or the Leon's tame rule. */
    private enum Kind { TAKEN, REFUSED, PLAY_NICELY, LEON_TAME_RULE }

    private record Row(String series, String finding, int index, Hunter hunter, Prey prey, Kind kind, String tag) {
        String testName() {
            return TEST_PREFIX + String.format("%s_%02d_%s_%s", this.series, this.index, this.hunter.species(), this.tag);
        }

        String where() {
            return this.hunter.port() + " (orig " + this.hunter.orig() + ")";
        }
    }

    private static List<Hunter> hunters() {
        return List.of(
                new Hunter("boyfriend", ModEntities.BOYFRIEND,
                        "Boyfriend.java:141 — MyEntityAINearestAttackableTarget(this, EntityLiving.class, 15.0f, 0, true, true, IMob.mobSelector)",
                        "Boyfriend's target-priority-3 NearestAttackableTargetGoal<Mob> + Enemy (Boyfriend.registerGoals)"),
                new Hunter("dragon", ModEntities.DRAGON,
                        "Dragon.java:116 — EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)",
                        "Dragon's target-priority-1 NearestAttackableTargetGoal<Mob> + Enemy (Dragon.registerGoals, ENT-S-117's channel)"),
                new Hunter("girlfriend", ModEntities.GIRLFRIEND,
                        "Girlfriend.java:167 — MyEntityAINearestAttackableTarget(this, EntityLiving.class, 15.0f, 0, true, true, IMob.mobSelector)",
                        "Girlfriend's target-priority-5 NearestAttackableTargetGoal<Mob> + Enemy (Girlfriend.registerGoals)"),
                new Hunter("leon", ModEntities.ENTITY_LEON,
                        "Leon.java:93 — EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)",
                        "EntityLeon's target-priority-4 NearestAttackableTargetGoal<Mob> + Enemy, and'ed with its tame rule (EntityLeon.registerGoals)"),
                new Hunter("theprinceadult", ModEntities.THE_PRINCE_ADULT,
                        "ThePrinceAdult.java:113 — EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)",
                        "ThePrinceAdult's target-priority-4 NearestAttackableTargetGoal<Mob> + Enemy (ThePrinceAdult.registerGoals)"),
                new Hunter("theprinceteen", ModEntities.THE_PRINCE_TEEN,
                        "ThePrinceTeen.java:117 — EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)",
                        "ThePrinceTeen's target-priority-4 NearestAttackableTargetGoal<Mob> + Enemy (ThePrinceTeen.registerGoals)"));
    }

    private static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        int n = 0;
        for (Hunter hunter : hunters()) {
            rows.add(new Row("s124", FINDING, ++n, hunter, Prey.SLIME, Kind.TAKEN, Prey.SLIME.tag));
            rows.add(new Row("s124", FINDING, ++n, hunter, Prey.ZOMBIE, Kind.TAKEN, Prey.ZOMBIE.tag));
            rows.add(new Row("s124", FINDING, ++n, hunter, Prey.PIG, Kind.REFUSED, Prey.PIG.tag));
            rows.add(new Row("s124", FINDING, ++n, hunter, Prey.SLIME, Kind.PLAY_NICELY, "slime_playnicely"));
            if (hunter.species().equals("leon")) {
                rows.add(new Row("s124", FINDING, ++n, hunter, Prey.SLIME, Kind.LEON_TAME_RULE, "slime_tame_rule"));
            }
        }
        // ENT-S-127 — the Creeper 1.7.10's EntityLiving.canAttackClass refused for every vanilla target task, refused by
        // OrigTargets.vanillaTaskPrey at the four sites that map one (orig Dragon.java:116, Leon.java:93,
        // ThePrinceAdult.java:113, ThePrinceTeen.java:117), each beside its Zombie control above; not the Boyfriend /
        // Girlfriend, whose orig MyEntityAITarget.isSuitableTarget granted the Creeper explicitly (:111)
        int m = 0;
        for (Hunter hunter : hunters()) {
            if (hunter.species().equals("boyfriend") || hunter.species().equals("girlfriend")) continue;
            rows.add(new Row("s127", FINDING_CREEPER, ++m, hunter, Prey.CREEPER, Kind.REFUSED, Prey.CREEPER.tag));
        }
        return rows;
    }

    /** One test per row: 29 TestFunctions in the {@code imobConvention} batch (25 of ENT-S-124, 4 of ENT-S-127). */
    @GameTestGenerator
    public Collection<TestFunction> imobConventionRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, row)));
        }
        return functions;
    }

    // ------------------------------------------------------------------
    // Runner: the goal read off the selector by type, asked canUse() for the row's prey
    // ------------------------------------------------------------------

    private static void run(GameTestHelper helper, Row row) {
        helper.assertTrue(helper.getLevel().getDifficulty() != Difficulty.PEACEFUL,
                "precondition: the game-test level runs at NORMAL, not Peaceful — the vanilla conditions refuse players on"
                        + " Peaceful and the hunters' own filters refuse everything (" + FINDING + " test setup)");
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Mob hunter = null;
        Mob prey = null;
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            helper.assertTrue(!OreSpawnConfig.PLAY_NICELY.get(),
                    "precondition: PLAY_NICELY.set(false) must read back false (" + FINDING + " test setup)");
            hunter = spawnWithGoals(helper, row.hunter().type().get(), HUNTER_POS);
            replaceRandom(hunter, rolls(GOAL_ROLL_BOUND, 0));
            NearestAttackableTargetGoal<?> goal = imobGoal(helper, hunter, row.hunter());
            prey = spawnPrey(helper, row.prey().type.get(), PREY_POS);
            helper.assertTrue((prey instanceof Enemy) == row.prey().enemy, "precondition: " + row.prey().description
                    + " — its Enemy membership must be as the row states (" + FINDING + " test setup)");
            if (row.prey() == Prey.SLIME) {
                helper.assertTrue(!(prey instanceof Monster), "precondition: a vanilla Slime is no Monster — the prey that tells"
                        + " the Mob + Enemy form from the Monster form (" + FINDING + " test setup)");
            }
            assertSees(helper, hunter, prey);
            switch (row.kind()) {
                case TAKEN -> assertTaken(helper, row, goal, prey, "");
                case REFUSED -> assertRefused(helper, row, goal, prey, "");
                case PLAY_NICELY -> {
                    assertTaken(helper, row, goal, prey, " (control, PlayNicely off)");
                    OreSpawnConfig.PLAY_NICELY.set(true);
                    helper.assertTrue(OreSpawnConfig.PLAY_NICELY.get(),
                            "precondition: PLAY_NICELY.set(true) must read back true (" + FINDING + " test setup)");
                    assertRefused(helper, row, goal, prey, " with PlayNicely on — the ENT-S-115 live canUse gate must compose"
                            + " over the Enemy selector, as orig registered the task only when PlayNicely == 0");
                }
                case LEON_TAME_RULE -> {
                    helper.assertTrue(OreSpawnConfig.petsDefendOwner(), "precondition: the game-test config runs modern"
                            + " (modern.enabled and petsDefendOwner at their defaults) — the tame rule is MOD-033's modern"
                            + " branch since 2026-09-05, pinned per mode in PortOnlyTargetingTests (" + FINDING + " test setup)");
                    TamableAnimal leon = (TamableAnimal) hunter;
                    leon.setTame(true, false);
                    hunter.setTarget(prey);
                    helper.assertTrue(leon.isTame() && hunter.getTarget() == prey,
                            "precondition: the Leon reads tamed and holds a target (" + FINDING + " test setup)");
                    assertRefused(helper, row, goal, prey, " tamed and holding a target — the port's tame rule"
                            + " (!isTame() || getTarget() == null, EntityLeon.registerGoals; MOD-033's modern branch) must compose"
                            + " over the Enemy selector");
                    hunter.setTarget(null);
                    assertTaken(helper, row, goal, prey, " tamed with the slot emptied — the tame rule admits, the Enemy selector takes");
                }
            }
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
            discardQuietly(prey);
            discardQuietly(hunter);
        }
        helper.succeed();
    }

    private static void assertTaken(GameTestHelper helper, Row row, NearestAttackableTargetGoal<?> goal, Mob prey, String when) {
        boolean can = goal.canUse();
        Object pick = readField(goal, NearestAttackableTargetGoal.class, "target");
        helper.assertTrue(can && pick == prey, row.where() + when + ": " + row.prey().description
                + " must be taken — orig's IMob.mobSelector took it, so the port's Enemy selector must (" + row.finding() + "); canUse="
                + can + ", pick " + describe((Entity) pick));
    }

    private static void assertRefused(GameTestHelper helper, Row row, NearestAttackableTargetGoal<?> goal, Mob prey, String when) {
        boolean can = goal.canUse();
        Object pick = readField(goal, NearestAttackableTargetGoal.class, "target");
        helper.assertTrue(!can, row.where() + when + ": " + row.prey().description + " must be refused (" + row.finding()
                + "); canUse=" + can + ", pick " + describe((Entity) pick));
    }

    /** The site's goal off the target selector: the one NearestAttackableTargetGoal typed Mob; no Monster-typed one may remain. */
    private static NearestAttackableTargetGoal<?> imobGoal(GameTestHelper helper, Mob hunter, Hunter site) {
        NearestAttackableTargetGoal<?> found = null;
        for (WrappedGoal wrapped : hunter.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof NearestAttackableTargetGoal<?> nearest) {
                Object targetType = readField(nearest, NearestAttackableTargetGoal.class, "targetType");
                helper.assertTrue(targetType != Monster.class, site.port() + ": no NearestAttackableTargetGoal<Monster> may remain"
                        + " on the target selector — Monster stood in for IMob until the convention (" + FINDING + ")");
                if (targetType == Mob.class) {
                    helper.assertTrue(found == null, site.port() + ": exactly one NearestAttackableTargetGoal<Mob> on the target"
                            + " selector (" + FINDING + ")");
                    found = nearest;
                }
            }
        }
        helper.assertTrue(found != null, "precondition: " + site.port() + " must carry a NearestAttackableTargetGoal<Mob> on its"
                + " target selector — the ruled shape of orig " + site.orig() + " (" + FINDING + " test setup)");
        return found;
    }

    // ------------------------------------------------------------------
    // Helpers (the PlayNicelyGateParityTests idiom)
    // ------------------------------------------------------------------

    private static void assertSees(GameTestHelper helper, Mob hunter, LivingEntity prey) {
        helper.assertTrue(hunter.hasLineOfSight(prey), "precondition: the " + hunter.getClass().getSimpleName()
                + " (eye " + String.format("%.2f", hunter.getEyeHeight()) + " above its feet on the floor) must see the "
                + prey.getClass().getSimpleName() + " inside the barrier shell (" + FINDING + " test geometry)");
    }

    /** Frozen on the floor: goals stripped, noAi, persistence set. */
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

    /** Frozen prey with 1000 HP. */
    private static Mob spawnPrey(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained as PlayNicelyGateParityTests.rolls. */
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

    private static Object readField(Object owner, Class<?> declaring, String name) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name, exception);
        }
    }

    private static String describe(Entity entity) {
        return entity == null ? "null" : entity.getClass().getSimpleName() + "#" + entity.getId();
    }
}
