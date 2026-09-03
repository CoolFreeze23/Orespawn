package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Kraken;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-100 KT-D: how long the Kraken holds a victim. orig Kraken.java:983-1013
 * keeps dragging {@code caught} while {@code !caught.isDead} (:984) and only
 * the else branch (:1006-1012) lets go — so a victim killed in the grip is
 * carried through its whole death animation and released only once the
 * entity is removed from the world. The port had released at health zero
 * ({@code isAlive()}); the owner ruled the faithful shape, {@code !isRemoved()}.
 *
 * <p>Tick-driven, hence its own batch (TEST-003) and its own class: the
 * Kraken's AI must run for the hold to be exercised, and the victim must be
 * ticked through vanilla's 20-tick death animation ({@code LivingEntity
 * .tickDeath}: removed at deathTime 20). A pig is grabbed through the real
 * {@code attackWithSomething} (orig :1047-1056) and killed in the same tick;
 * {@value #DYING_CHECK_TICKS} ticks later it is dead, not yet removed, and
 * must still be held with the attack flag up; {@value #REMOVED_CHECK_TICKS}
 * ticks in it is removed and must have been released (orig :1006-1012:
 * caught null, newtarget 1, attacking 0). Every roll that could drop, bite
 * or re-target during the window is forced to miss ({@link
 * VortexParityTests.ForcedRoll} on {@code Entity.random}, the ENT-S-097
 * seam): :1002 random release and :921 re-target on 250, :999 bite on 50,
 * :915 lightning on 400, :961 prey search on 8. The y &gt; 190 release (:987)
 * cannot fire: the Kraken sits under the shell's ceiling.</p>
 *
 * <p>Geometry (KrakenPlayNicelyGateTests' prey rule): the grab needs the
 * victim within sqrt(30) of the point 15 blocks under the Kraken's feet and
 * the eye 12.75 above them, all inside the barrier shell, so this runs in the
 * 34-tall empty_tall — feet at rel 18 (eye 30.75, ceiling at rel 35), victim
 * at rel 3 on the same column; line of sight is asserted at setup. The Kraken
 * ticks past its 10-tick thunderstorm arm (orig :63/:171), and doWeatherCycle
 * is OFF in the game-test server, so the weather is snapshotted, forced clear
 * and restored on every path.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KrakenHoldReleaseTests {

    private static final BlockPos KRAKEN_POS = new BlockPos(24, 18, 24);
    private static final BlockPos VICTIM_POS = new BlockPos(24, 3, 24);
    /** Inside vanilla's 20-tick death animation: dead, not removed. */
    private static final int DYING_CHECK_TICKS = 8;
    /** Well past it: removed (LivingEntity.tickDeath removes at deathTime 20). */
    private static final int REMOVED_CHECK_TICKS = 40;

    @GameTest(template = "empty_tall", timeoutTicks = 200, batch = "krakenHoldRelease")
    public void s100_kt_d_victim_killed_in_grip_is_held_until_removed(GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final WeatherSnapshot weather = WeatherSnapshot.take(level);
        Kraken spawned = null;
        Pig victim = null;
        try {
            WeatherSnapshot.forceClear(level);
            spawned = helper.spawn(ModEntities.KRAKEN.get(), KRAKEN_POS);
            spawned.setPersistenceRequired();
            replaceRandom(spawned, holdRolls());
            victim = helper.spawn(EntityType.PIG, VICTIM_POS);
            victim.setNoAi(true);
            victim.setPersistenceRequired();
            helper.assertTrue(spawned.hasLineOfSight(victim),
                    "precondition: the Kraken (eye rel 30.75) must see the victim 15 blocks below inside the"
                            + " barrier shell (ceiling at rel 35 in empty_tall) (ENT-S-100 test geometry)");
            attackWithSomething(spawned, victim);
            helper.assertTrue(caughtOf(spawned) == victim && spawned.getAttacking() == 1,
                    "precondition: attackWithSomething must grab a victim 15 blocks straight below"
                            + " (orig Kraken.java:1047-1056), caught=" + caughtOf(spawned) + " (ENT-S-100)");
            victim.kill();
            helper.assertTrue(victim.isDeadOrDying() && !victim.isRemoved(),
                    "precondition: kill() must leave the victim dead but not yet removed (vanilla death animation)"
                            + " health=" + victim.getHealth() + " removed=" + victim.isRemoved() + " (ENT-S-100)");
        } catch (Throwable e) {
            weather.restore(level);
            if (victim != null) victim.discard();
            if (spawned != null) spawned.discard();
            if (e instanceof RuntimeException re) throw re;
            if (e instanceof Error err) throw err;
            throw new IllegalStateException("setup failed after restoring the weather", e);
        }
        final Kraken kraken = spawned;
        final Pig prey = victim;
        final Runnable cleanup = () -> {
            weather.restore(level);
            prey.discard();
            kraken.discard();
        };
        helper.runAfterDelay(DYING_CHECK_TICKS, () -> {
            boolean held = false;
            try {
                helper.assertTrue(prey.isDeadOrDying() && !prey.isRemoved(),
                        "precondition: " + DYING_CHECK_TICKS + " ticks after the kill the victim must still be in its"
                                + " death animation (dead, not removed): health=" + prey.getHealth()
                                + " removed=" + prey.isRemoved() + " (ENT-S-100)");
                LivingEntity caught = caughtOf(kraken);
                helper.assertTrue(caught == prey && kraken.getAttacking() == 1,
                        "a victim killed in the grip must stay held until it is REMOVED (orig Kraken.java:984"
                                + " `!caught.isDead`), not dropped at health zero: caught=" + caught
                                + " attacking=" + kraken.getAttacking() + " (ENT-S-100 KT-D)");
                held = true;
            } finally {
                if (!held) cleanup.run();
            }
            helper.runAfterDelay(REMOVED_CHECK_TICKS - DYING_CHECK_TICKS, () -> {
                try {
                    helper.assertTrue(prey.isRemoved(),
                            "precondition: " + REMOVED_CHECK_TICKS + " ticks after the kill the victim must have been"
                                    + " removed by its death animation (ENT-S-100)");
                    LivingEntity caught = caughtOf(kraken);
                    helper.assertTrue(caught == null && kraken.getAttacking() == 0,
                            "once the held victim is removed the Kraken must let go (orig Kraken.java:1006-1012:"
                                    + " caught null, attacking 0): caught=" + caught + " attacking="
                                    + kraken.getAttacking() + " (ENT-S-100 KT-D)");
                } finally {
                    cleanup.run();
                }
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Entity random for the hold window: orig Kraken.java:1002 random release and :921
     * re-target ({@code nextInt(250) == 1}), :999 in-grip bite ({@code nextInt(50) == 1}),
     * :915 lightning ({@code nextInt(400) == 1}) and :961 prey search
     * ({@code nextInt(8) == 1}) never hit. ForcedRoll delegates unmatched bounds, so
     * the wrappers chain.
     */
    private static RandomSource holdRolls() {
        RandomSource rolls = RandomSource.create(1234L);
        rolls = new VortexParityTests.ForcedRoll(rolls, 400, 0);
        rolls = new VortexParityTests.ForcedRoll(rolls, 250, 0);
        rolls = new VortexParityTests.ForcedRoll(rolls, 50, 0);
        return new VortexParityTests.ForcedRoll(rolls, 8, 0);
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

    /** orig Kraken.java:60 {@code caught}, port private field of the same name. */
    private static LivingEntity caughtOf(Kraken kraken) {
        try {
            Field field = Kraken.class.getDeclaredField("caught");
            field.setAccessible(true);
            return (LivingEntity) field.get(kraken);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot read Kraken.caught", exception);
        }
    }

    /** orig Kraken.java:1047 {@code attackWithSomething(EntityLivingBase)}, port private method of the same name. */
    private static void attackWithSomething(Kraken kraken, LivingEntity target) {
        try {
            Method method = Kraken.class.getDeclaredMethod("attackWithSomething", LivingEntity.class);
            method.setAccessible(true);
            method.invoke(kraken, target);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("Kraken.attackWithSomething threw", exception.getCause());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("cannot invoke Kraken.attackWithSomething", exception);
        }
    }

    /**
     * doWeatherCycle is OFF in the game-test server (GameTestServer.TEST_GAME_RULES),
     * so weather flags only ever change through explicit sets: whatever a test does
     * to them it must undo itself, exactly (KrakenPlayNicelyGateTests' record).
     */
    private record WeatherSnapshot(int clearTime, int rainTime, int thunderTime, boolean raining, boolean thundering) {
        static WeatherSnapshot take(ServerLevel level) {
            ServerLevelData data = (ServerLevelData) level.getLevelData();
            return new WeatherSnapshot(data.getClearWeatherTime(), data.getRainTime(), data.getThunderTime(),
                    data.isRaining(), data.isThundering());
        }

        /** The server's own start-up weather (GameTestServer.java:160). */
        static void forceClear(ServerLevel level) {
            level.setWeatherParameters(20000000, 20000000, false, false);
        }

        void restore(ServerLevel level) {
            ServerLevelData data = (ServerLevelData) level.getLevelData();
            data.setClearWeatherTime(this.clearTime);
            data.setRainTime(this.rainTime);
            data.setThunderTime(this.thunderTime);
            data.setRaining(this.raining);
            data.setThundering(this.thundering);
        }
    }
}
