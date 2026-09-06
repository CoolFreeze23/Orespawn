package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntitySpyro;
import danger.orespawn.entity.EntityStinky;
import danger.orespawn.entity.ai.AmbientFlightGoal;
import danger.orespawn.entity.ai.DragonflyHuntGoal;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The XS parity batch (owner ruling 2026-09-06, item 22: ENT-S-148 / 149 / 151 — parity, classic, one refuter): three flight-target
 * transcriptions pinned at the orig positions. One generated {@link TestFunction} per row
 * ({@code flighttargetparitytests.s<finding>_NN_<species>_<what>}), all in the {@code flightTargetParity} batch (TEST-003).
 *
 * <p>Per row the discriminating case of the entry. ENT-S-148: a hurt Dragonfly re-aims its flight target on the attacker's
 * {@code (int)}-cast cell (orig Dragonfly.java:182) — the attacker at a fractional NEGATIVE y on the integer x / z lattice (the s138_04
 * shape: an exact integer casts and floors alike on any grid origin, so y alone discriminates, and the cast reads one cell above the
 * floor's there). ENT-S-149: one steering tick of a Dragonfly at rest toward a target straight above adds {@code 0.7 × 0.20000000149011612}
 * on y (orig :156's own y blend — the class file's double constant) and {@code 0.5 × 0.3} on x and on z (orig :155 / :157 — the half-block
 * centre offset from the lattice; the x / z blend unchanged). ENT-S-151: the Stinky's and the Spyro's flight-target reach is the
 * integer-lattice distSq of the cast cell against {@code 2.1f} (orig Stinky.java:608 / Spyro.java:615): the cast cell's (-1, -1, -1)
 * diagonal — lattice 3 from the cast cell, 2 from the floor cell, 0.5 from the exact position to its centre (the mob stands on that
 * cell's corner) — stands, where HEAD's {@code closerToCenterThan(position(), 2.1)} read 0.5 &lt; 4.41 and re-picked; the (1, 0, 0)
 * neighbour — lattice 1 — re-picks, the pick pinned to the cast cell's (6, 0, 6).
 *
 * <p>Frozen mobs: goals stripped, noAi, persistence set. The Dragonflies with their feet ON the floor (rel y 0, noGravity, onGround —
 * F0.7) on the INTEGER x / z lattice (F0.8: the harness grid origin is random per run, ±15 million on x / z, y −60 — beyond 2^23 a
 * float's ulp is 1); the airborne flyers at rel 1.5 (a fractional negative y) on the same lattice, noGravity. Dice through the
 * ForcedRoll chain; every spawn discarded in a finally; no config flip, no day-time flip, nothing global touched. Assertion messages
 * carry the finding id, the expected and the actual values.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class FlightTargetParityTests {

    private static final String BATCH = "flightTargetParity";
    private static final String TEST_PREFIX = "flighttargetparitytests.";
    /** Generated TestFunctions bypass the holder's template prefixing, so the template is named in full (the kf17b gate). */
    private static final String EMPTY_LARGE = OreSpawnMod.MOD_ID + ":empty_large";
    private static final int TIMEOUT_TICKS = 100;

    /** A mob with its feet ON the floor (rel y 0 — F0.7) on the INTEGER x / z lattice (F0.8): the Dragonfly rows' spot. */
    private static final Vec3 FLOOR_LATTICE_POS = new Vec3(20.0, 0.0, 24.0);
    /**
     * A mob at a fractional NEGATIVE y — rel 1.5 over the floor (the harness grid sits below y 0) on the INTEGER x / z lattice, so the
     * (int) cast and the floor agree on x / z on any grid origin and differ on y alone: the cast cell one above the floor cell (the
     * TargetingWave4ParityTests FRACTIONAL_POS / s138_04 shape). The Stinky's and the Spyro's spot.
     */
    private static final Vec3 FRACTIONAL_POS = new Vec3(20.0, 1.5, 24.0);
    /** The s148 attacker: 2 east of the Dragonfly on the same lattice, at the fractional negative y. */
    private static final Vec3 ATTACKER_POS = new Vec3(22.0, 1.5, 24.0);
    private static final float PREY_HEALTH = 1000.0f;
    /** orig Dragonfly.java:156's y blend — the class file's own double constant (ldc2_w 0.20000000149011612d), not (double) 0.2f (0.20000000298023224). */
    private static final double DRAGONFLY_BLEND_Y = 0.20000000149011612;
    /** orig :155 / :157 — the x / z blend: the port's 0.3 for the class file's 0.30000000149011613 (the ENT-S-149 observation, unchanged). */
    private static final double DRAGONFLY_BLEND_XZ = 0.3;
    private static final double DRAGONFLY_STEER_Y = 0.7;
    private static final double DRAGONFLY_STEER_XZ = 0.5;
    /** From rest, one tick toward a target straight above: 0.0 + (1.0 × 0.7 − 0.0) × 0.20000000149011612 in doubles (HEAD's one blend 0.3 gave 0.21). */
    private static final double Y_DELTA_ONE_TICK = 0.14000000104308127;
    /** From rest, the half-block centre offset (+0.5 on x and on z from the lattice): 0.0 + (1.0 × 0.5 − 0.0) × 0.3 = 0.15 exactly in doubles. */
    private static final double XZ_DELTA_ONE_TICK = 0.15;
    /** HEAD's near read: closerToCenterThan(pos, 2.1) is distToCenterSqr &lt; 2.1 × 2.1. */
    private static final double HEAD_NEAR_SQ = 2.1 * 2.1;
    /** The pinned wild pick: xdir = zdir = nextInt(5) + 6 with 5 → 0, the two sign rolls 2 → 1 (kept), the y roll at its bias (+0). */
    private static final int PICK_XZ = 6;

    // ------------------------------------------------------------------
    // The row table
    // ------------------------------------------------------------------

    private record Row(String name, Consumer<GameTestHelper> body) {
        String testName() {
            return TEST_PREFIX + this.name;
        }
    }

    private static List<Row> rows() {
        List<Row> r = new ArrayList<>();
        r.add(new Row("s148_01_dragonfly_182_hurt_reaim_on_attacker_cast_cell", FlightTargetParityTests::dragonflyHurtReaimCastCell));
        r.add(new Row("s149_01_dragonfly_156_y_blend_one_tick_delta", FlightTargetParityTests::dragonflyYBlendOneTick));
        r.add(new Row("s151_01_stinky_608_diagonal_lattice_3_stands", h -> flightReach(h, false, true)));
        r.add(new Row("s151_02_stinky_608_neighbour_lattice_1_repicks", h -> flightReach(h, false, false)));
        r.add(new Row("s151_03_spyro_615_diagonal_lattice_3_stands", h -> flightReach(h, true, true)));
        r.add(new Row("s151_04_spyro_615_neighbour_lattice_1_repicks", h -> flightReach(h, true, false)));
        return r;
    }

    /** One test per row: 6 TestFunctions in the {@code flightTargetParity} batch. */
    @GameTestGenerator
    public Collection<TestFunction> flightTargetRows() {
        List<TestFunction> functions = new ArrayList<>();
        for (Row row : rows()) {
            functions.add(new TestFunction(BATCH, row.testName(), EMPTY_LARGE, Rotation.NONE, TIMEOUT_TICKS, 0L, true,
                    helper -> run(helper, row)));
        }
        return functions;
    }

    private static void run(GameTestHelper helper, Row row) {
        row.body().accept(helper);
        helper.succeed();
    }

    // ------------------------------------------------------------------
    // ENT-S-148 — orig Dragonfly.java:178-185: attackEntityFrom → currentFlightTarget.set((int) e.posX, (int) e.posY, (int) e.posZ)
    // ------------------------------------------------------------------

    /**
     * EntityDragonfly.hurt (orig Dragonfly.java:182): a butterfly at a fractional negative y (rel 1.5) on the integer lattice, 2 east, is
     * the damage source's entity; after the hit the hunt goal's flight target is the butterfly's CAST cell — one above the cell
     * {@code blockPosition()} (Mth.floor per axis) gave at HEAD. The Dragonfly's flight target is parked far first, so the row reads the
     * hit's own write.
     */
    private static void dragonflyHurtReaimCastCell(GameTestHelper helper) {
        final String finding = "ENT-S-148";
        Mob fly = null;
        Mob butterfly = null;
        try {
            fly = spawnFrozenOnFloor(helper, ModEntities.ENTITY_DRAGONFLY.get(), FLOOR_LATTICE_POS);
            butterfly = spawnPreyAt(helper, ModEntities.ENTITY_BUTTERFLY.get(), ATTACKER_POS);
            assertFractionalNegativeY(helper, butterfly, finding);
            DragonflyHuntGoal goal = (DragonflyHuntGoal) readField(fly, EntityDragonfly.class, "huntGoal");
            helper.assertTrue(goal != null, "precondition: the Dragonfly carries its DragonflyHuntGoal (registerGoals) (" + finding + " test setup)");
            BlockPos parked = AmbientFlightGoal.castCell(fly).above(10);
            goal.setFlightTarget(parked);
            BlockPos cast = new BlockPos((int) butterfly.getX(), (int) butterfly.getY(), (int) butterfly.getZ());
            BlockPos floor = butterfly.blockPosition();
            helper.assertTrue(cast.getY() == floor.getY() + 1 && cast.getX() == floor.getX() && cast.getZ() == floor.getZ(),
                    "precondition: the attacker's cast cell " + cast + " sits one above its floor cell " + floor + " and agrees with it on x / z (" + finding
                            + " test geometry)");
            DamageSource source = helper.getLevel().damageSources().mobAttack(butterfly);
            helper.assertTrue(source.getEntity() == butterfly, "precondition: the damage source's entity is the butterfly (orig :181 getEntity()) (" + finding
                    + " test setup)");
            fly.hurt(source, 1.0f);
            BlockPos after = (BlockPos) readField(goal, AmbientFlightGoal.class, "flightTarget");
            helper.assertTrue(cast.equals(after), "EntityDragonfly.hurt (orig Dragonfly.java:182 — currentFlightTarget.set((int) e.posX, (int) e.posY, (int) e.posZ), the"
                    + " (int) casts, BUG-027): the flight target is the attacker's CAST cell " + cast + ", one above its floor cell at a fractional negative y; HEAD's"
                    + " attacker.blockPosition() gave " + floor + " (" + finding + "); got " + after);
        } finally {
            discardQuietly(butterfly);
            discardQuietly(fly);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-149 — orig Dragonfly.java:155-157: the y blend 0.20000000149011612, x / z 0.30000000149011613
    // ------------------------------------------------------------------

    /**
     * AmbientFlightGoal.tick for the Dragonfly (orig Dragonfly.java:156 — {@code motionY += (signum(dy) · 0.7f − motionY) · 0.20000000149011612};
     * :155 / :157 x and z by 0.30000000149011613): a Dragonfly at rest on the floor (noGravity), its flight target 10 above its cast cell,
     * the retarget (300) and the hunt (12) dice quiet, ticked once through the goal: deltaMovement.y is exactly 0.7 × 0.20000000149011612
     * = 0.14000000104308127 (HEAD's one blend 0.3 on every axis gave 0.21); x and z, the half-block centre offset from the lattice,
     * 0.5 × 0.3 = 0.15 each (the x / z blend unchanged — the control).
     */
    private static void dragonflyYBlendOneTick(GameTestHelper helper) {
        final String finding = "ENT-S-149";
        Mob fly = null;
        try {
            fly = spawnFrozenOnFloor(helper, ModEntities.ENTITY_DRAGONFLY.get(), FLOOR_LATTICE_POS);
            DragonflyHuntGoal goal = (DragonflyHuntGoal) readField(fly, EntityDragonfly.class, "huntGoal");
            AmbientFlightGoal.Params p = (AmbientFlightGoal.Params) readField(goal, AmbientFlightGoal.class, "params");
            helper.assertTrue(p.blendY() == DRAGONFLY_BLEND_Y && p.blend() == DRAGONFLY_BLEND_XZ && p.steerY() == DRAGONFLY_STEER_Y && p.steerXY() == DRAGONFLY_STEER_XZ,
                    "Params.dragonfly() (orig Dragonfly.java:155-157): blendY " + DRAGONFLY_BLEND_Y + " (the class file's double constant), blend " + DRAGONFLY_BLEND_XZ
                            + ", steerY " + DRAGONFLY_STEER_Y + ", steerXY " + DRAGONFLY_STEER_XZ + " (" + finding + "); got " + p);
            helper.assertTrue(fly.getX() == Math.floor(fly.getX()) && fly.getZ() == Math.floor(fly.getZ()), "precondition: the Dragonfly stands on the integer x / z"
                    + " lattice, so its target cell's centre sits +0.5 on x and on z (" + finding + " test geometry); at " + fly.position());
            fly.setDeltaMovement(Vec3.ZERO);
            BlockPos cast = AmbientFlightGoal.castCell(fly);
            BlockPos above = cast.above(10);
            helper.assertTrue(above.distSqr(cast) == 100.0, "precondition: the flight target 10 above the cast cell is not near (100 against 2.1) (" + finding
                    + " test geometry)");
            goal.setFlightTarget(above);
            replaceRandom(fly, rolls(300, 1, 12, 1));
            goal.tick();
            Vec3 after = fly.getDeltaMovement();
            helper.assertTrue(after.y == Y_DELTA_ONE_TICK, "AmbientFlightGoal.tick (orig Dragonfly.java:156 — motionY += (signum · 0.7f − motionY) · 0.20000000149011612):"
                    + " from rest, one tick toward a target straight above adds 0.7 × 0.20000000149011612 = " + Y_DELTA_ONE_TICK + " on y; HEAD's one blend 0.3 for"
                    + " every axis gave 0.21 (" + finding + "); got " + after.y);
            helper.assertTrue(after.x == XZ_DELTA_ONE_TICK && after.z == XZ_DELTA_ONE_TICK, "control: orig Dragonfly.java:155 / :157 — the x / z blend stays 0.3 (the"
                    + " port's short literal for the class file's 0.30000000149011613, the entry's observation): from rest the half-block centre offset adds 0.5 × 0.3 = "
                    + XZ_DELTA_ONE_TICK + " on x and on z (" + finding + "); got " + after);
        } finally {
            discardQuietly(fly);
        }
    }

    // ------------------------------------------------------------------
    // ENT-S-151 — orig Stinky.java:608 / Spyro.java:615: getDistanceSquared((int) posX, (int) posY, (int) posZ) < 2.1f
    // ------------------------------------------------------------------

    /**
     * EntityStinky.doMovement / EntitySpyro.doMovement (orig Stinky.java:608 / Spyro.java:615 — {@code getDistanceSquared((int) posX, (int) posY,
     * (int) posZ) < 2.1f}, the integer lattice of the cast cell): a wild flyer at activity 2 at a fractional negative y on the integer
     * lattice, the retarget clock and the attack pass quiet, the flight target parked and the private drive run once. The cast cell's
     * (-1, -1, -1) diagonal reads 3 on the lattice — not near, the target stands (HEAD's closerToCenterThan read the exact position 0.5
     * from that cell's centre — the mob stands on its corner — and re-picked; from the floor cell the lattice reads 2); the (1, 0, 0)
     * neighbour reads 1 — near, the pick runs and lands on the pinned (6, 0, 6) of the cast cell.
     */
    private static void flightReach(GameTestHelper helper, boolean spyro, boolean diagonal) {
        final String finding = "ENT-S-151";
        final String site = spyro ? "EntitySpyro.doMovement (orig Spyro.java:615" : "EntityStinky.doMovement (orig Stinky.java:608";
        Mob flyer = null;
        try {
            flyer = spawnFrozen(helper, (spyro ? ModEntities.ENTITY_SPYRO : ModEntities.ENTITY_STINKY).get(), FRACTIONAL_POS);
            flyer.setNoGravity(true);
            String name = flyer.getClass().getSimpleName();
            int activity;
            if (spyro) {
                ((EntitySpyro) flyer).setActivity(2);
                activity = ((EntitySpyro) flyer).getActivity();
            } else {
                ((EntityStinky) flyer).setActivity(2);
                activity = ((EntityStinky) flyer).getActivity();
            }
            helper.assertTrue(activity == 2 && flyer instanceof TamableAnimal pet && !pet.isTame() && !pet.isOrderedToSit(), "precondition: a wild " + name
                    + " at activity 2, not sitting — the flight runs and the ownerless pick rolls nextInt(5) + 6 (" + finding + " test setup); activity " + activity);
            assertFractionalNegativeY(helper, flyer, finding);
            BlockPos cast = AmbientFlightGoal.castCell(flyer);
            BlockPos floor = flyer.blockPosition();
            BlockPos parked = diagonal ? cast.offset(-1, -1, -1) : cast.offset(1, 0, 0);
            double lattice = parked.distSqr(cast);
            double headRead = parked.distToCenterSqr(flyer.position());
            if (diagonal) {
                helper.assertTrue(lattice == 3.0 && parked.distSqr(floor) == 2.0 && headRead < HEAD_NEAR_SQ, "precondition: the parked diagonal " + parked
                        + " reads 3 on the lattice from the cast cell " + cast + " (orig: not near), 2 from the floor cell " + floor + ", and " + headRead
                        + " from the exact position " + flyer.position() + " to its centre (HEAD: near, < " + HEAD_NEAR_SQ + ") (" + finding + " test geometry)");
            } else {
                helper.assertTrue(lattice == 1.0, "precondition: the parked neighbour " + parked + " reads 1 on the lattice from the cast cell " + cast + " ("
                        + finding + " test geometry)");
            }
            BlockPos pick = cast.offset(PICK_XZ, 0, PICK_XZ);
            helper.assertTrue(helper.getBounds().contains(Vec3.atCenterOf(pick)) && helper.getLevel().getBlockState(pick).isAir(), "precondition: the pinned pick "
                    + pick + " lies inside the structure and is air (" + finding + " test geometry)");
            writeField(flyer, flyer.getClass(), "currentFlightTarget", parked);
            // The Stinky: 300 → 1 (the retarget clock quiet), 7 → 0 (its 1-in-7 attack pass quiet), 5 → 0, 2 → 1 (the signs kept), 6 → 2 (nextInt(6) - 2 = 0);
            // the Spyro: 300 → 1, 6 → 0 (its 1-in-6 pass quiet), 5 → 0, 2 → 1, 9 → 4 (nextInt(9) - 4 = 0) — the StinkyIdleParityTests pick idiom.
            replaceRandom(flyer, rolls(spyro ? new int[] {300, 1, 6, 0, 5, 0, 2, 1, 9, 4} : new int[] {300, 1, 7, 0, 5, 0, 2, 1, 6, 2}));
            invoke(flyer, flyer.getClass(), "doMovement", new Class<?>[0]);
            BlockPos after = (BlockPos) readField(flyer, flyer.getClass(), "currentFlightTarget");
            if (diagonal) {
                helper.assertTrue(parked.equals(after), site + " — getDistanceSquared((int) posX, (int) posY, (int) posZ) < 2.1f on the integer lattice): a flight"
                        + " target on the cast cell's (-1, -1, -1) diagonal, lattice distSq 3, is NOT near (3 < 2.1f is false): no pick, the target stands at " + parked
                        + "; HEAD's closerToCenterThan(position(), 2.1) read the exact position " + headRead + " from that cell's centre (< 4.41) and re-picked ("
                        + finding + "); got " + after);
            } else {
                helper.assertTrue(pick.equals(after), site + " — the same read): a flight target on the cast cell's (1, 0, 0) neighbour, lattice distSq 1, is near"
                        + " (1 < 2.1f): the pick runs and, pinned (nextInt(5) + 6 → 6 on x and on z, the signs kept, the y roll at its bias), lands on " + pick + " ("
                        + finding + "); target " + parked + " -> " + after);
            }
        } finally {
            discardQuietly(flyer);
        }
    }

    // ------------------------------------------------------------------
    // Helpers (the TargetingWave4ParityTests idioms)
    // ------------------------------------------------------------------

    private static void assertFractionalNegativeY(GameTestHelper helper, Entity mob, String finding) {
        double y = mob.getY();
        helper.assertTrue(y < 0.0 && y != Math.floor(y), "precondition: the mob stands at a fractional NEGATIVE y (the harness grid sits below y 0; rel 1.5 over the"
                + " floor) so the (int) cast and the floor read different cells (" + finding + " test geometry); y " + y);
        BlockPos cast = AmbientFlightGoal.castCell((Mob) mob);
        BlockPos floor = mob.blockPosition();
        helper.assertTrue(cast.getX() == floor.getX() && cast.getZ() == floor.getZ(), "precondition: the cast cell " + cast + " and blockPosition() " + floor
                + " agree on x and z — an integer-valued x / z casts and floors alike on any origin, so y alone discriminates (" + finding + " test geometry); at x "
                + mob.getX() + ", z " + mob.getZ());
        helper.assertTrue(cast.getY() == floor.getY() + 1, "precondition: the cast cell " + cast + " sits one above blockPosition() " + floor
                + " (" + finding + " test geometry)");
    }

    /** Frozen: goals stripped, noAi, persistence set (the IgnoreListParityTests idiom). */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    /** Frozen with its feet ON the floor: noGravity and onGround (F0.7), so nothing moves it between the spawn and the read. */
    private static Mob spawnFrozenOnFloor(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob mob = spawnFrozen(helper, type, pos);
        mob.setNoGravity(true);
        mob.setOnGround(true);
        return mob;
    }

    /** Frozen prey with 1000 HP and no gravity, so it stays where the row put it. */
    private static Mob spawnPreyAt(GameTestHelper helper, EntityType<? extends Mob> type, Vec3 pos) {
        Mob prey = spawnFrozen(helper, type, pos);
        prey.setNoGravity(true);
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(PREY_HEALTH);
        prey.setHealth(PREY_HEALTH);
        return prey;
    }

    private static void discardQuietly(Entity entity) {
        if (entity != null && !entity.isRemoved()) {
            entity.discard();
        }
    }

    /** A seeded random with each (bound, answer) pair pinned — the VortexParityTests.ForcedRoll seam, chained. */
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

    private static Object invoke(Object target, Class<?> declaring, String name, Class<?>[] types, Object... args) {
        String where = declaring.getSimpleName() + "." + name;
        try {
            Method method = declaring.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(target, args);
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
            throw new IllegalStateException("cannot read " + declaring.getSimpleName() + "." + name + " (1.21.1: official names at runtime)", exception);
        }
    }

    private static void writeField(Object owner, Class<?> declaring, String name, Object value) {
        try {
            Field field = declaring.getDeclaredField(name);
            field.setAccessible(true);
            field.set(owner, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot write " + declaring.getSimpleName() + "." + name, exception);
        }
    }
}
