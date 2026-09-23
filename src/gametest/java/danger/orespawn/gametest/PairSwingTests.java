package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.Shoes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-175: the Girlfriend's and the Boyfriend's swing timer. 1.21 ticks {@code updateSwingTime} only in
 * {@code Monster.aiStep}, {@code Player.serverAiStep} and the client's {@code RemotePlayer.aiStep}; the pair are
 * {@code TamableAnimal}s and the original ticks it itself at the head of {@code onLivingUpdate} (orig Girlfriend.java:577,
 * Boyfriend.java:496 — {@code func_82168_bl}, before super). Without it a swing never ended: {@code swinging} stayed true
 * and {@code swingTime} at -1, so the arm swing was never drawn and {@code performRangedAttack}'s {@code swinging} guard
 * (orig Girlfriend.java:977, Boyfriend.java:876) refused every throw after the first melee swing. Pins: the swing advances
 * one step a tick and ends on the seventh update, vanilla's six-tick swing (a second tick per {@code aiStep} would end it by
 * the sixth); a throw fires once the swing has ended; the guard still refuses a throw while a swing is in progress.
 *
 * <p>The test runs after the level's tick, so at {@code runAfterDelay(n)} the pair have run exactly n {@code aiStep}s since
 * the swing. An empty structure holds no target and no dance block, so nothing but the test swings the pair. On 14 February
 * a valentine-angry Girlfriend hunts Boyfriends in a 16/4/16 box: the two species' rows run in separate batches, and a
 * Boyfriend an earlier batch left nearby is harmless in a row's ten ticks (her RangedAttackGoal's first throw comes 20
 * ticks after she takes a target, and her melee needs a held item); that day she is also the 2.5x8 giant, so the shoes
 * are counted around the thrower ({@link #shoesAround}).</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PairSwingTests {

    /** Past the vanilla swing duration (6 ticks, no haste or mining fatigue). */
    private static final int AFTER_SWING = 10;

    private static void swingAdvancesAndEnds(GameTestHelper helper, LivingEntity pair, String name, String orig) {
        pair.swing(InteractionHand.MAIN_HAND);
        helper.assertTrue(pair.swinging, name + ": swing() did not start a swing");
        helper.runAfterDelay(3, () -> helper.assertTrue(pair.swinging && pair.swingTime == 2,
                name + ": the swing timer does not advance one step a tick (swinging " + pair.swinging + ", swingTime "
                        + pair.swingTime + " after three; " + orig + ", ENT-S-175)"));
        helper.runAfterDelay(6, () -> helper.assertTrue(pair.swinging && pair.swingTime == 5,
                name + ": not one update a tick (swinging " + pair.swinging + ", swingTime " + pair.swingTime
                        + " after six; ENT-S-175)"));
        helper.runAfterDelay(7, () -> helper.assertTrue(!pair.swinging && pair.swingTime == 0,
                name + ": the swing does not end on the seventh update (swinging " + pair.swinging + ", swingTime "
                        + pair.swingTime + "; ENT-S-175)"));
        helper.runAfterDelay(AFTER_SWING, () -> {
            helper.assertTrue(!pair.swinging && pair.swingTime == 0,
                    name + ": the swing never ends (swinging " + pair.swinging + ", swingTime " + pair.swingTime + "; " + orig
                            + ", ENT-S-175)");
            helper.succeed();
        });
    }

    private static Pig target(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, 5, 2, 1);
        pig.setNoAi(true);
        return pig;
    }

    /**
     * The shoes around the thrower, counted in the tick of the throw: on 14 February the Girlfriend is the 2.5x8 giant
     * (orig Girlfriend.java:142-144) and throws from above the 8x8x8 template's bounds, which {@code helper.getEntities}
     * counts within.
     */
    private static int shoesAround(GameTestHelper helper, LivingEntity thrower) {
        return helper.getLevel().getEntitiesOfClass(Shoes.class, thrower.getBoundingBox().inflate(4.0)).size();
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "pairSwingGirlfriend")
    public static void s175a_girlfriend_swing_advances_and_ends(GameTestHelper helper) {
        Girlfriend girlfriend = helper.spawn(ModEntities.GIRLFRIEND.get(), 1, 2, 1);
        swingAdvancesAndEnds(helper, girlfriend, "Girlfriend", "orig Girlfriend.java:577");
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "pairSwingGirlfriend")
    public static void s175b_girlfriend_throws_again_after_a_swing(GameTestHelper helper) {
        Girlfriend girlfriend = helper.spawn(ModEntities.GIRLFRIEND.get(), 1, 2, 1);
        Pig pig = target(helper);
        girlfriend.swing(InteractionHand.MAIN_HAND);
        helper.runAfterDelay(AFTER_SWING, () -> {
            int before = shoesAround(helper, girlfriend);
            girlfriend.performRangedAttack(pig, 1.0f);
            int after = shoesAround(helper, girlfriend);
            helper.assertTrue(after == before + 1, "Girlfriend: no shoe thrown once the melee swing has ended (" + before
                    + " -> " + after + "; the swinging guard, orig Girlfriend.java:977, ENT-S-175)");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20, batch = "pairSwingGirlfriend")
    public static void s175c_girlfriend_guard_refuses_a_throw_mid_swing(GameTestHelper helper) {
        Girlfriend girlfriend = helper.spawn(ModEntities.GIRLFRIEND.get(), 1, 2, 1);
        Pig pig = target(helper);
        girlfriend.swing(InteractionHand.MAIN_HAND);
        int before = shoesAround(helper, girlfriend);
        girlfriend.performRangedAttack(pig, 1.0f);
        helper.assertTrue(shoesAround(helper, girlfriend) == before,
                "Girlfriend: a shoe thrown while a swing is in progress (orig Girlfriend.java:977 refuses it)");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "pairSwingBoyfriend")
    public static void s175d_boyfriend_swing_advances_and_ends(GameTestHelper helper) {
        Boyfriend boyfriend = helper.spawn(ModEntities.BOYFRIEND.get(), 1, 2, 1);
        swingAdvancesAndEnds(helper, boyfriend, "Boyfriend", "orig Boyfriend.java:496");
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "pairSwingBoyfriend")
    public static void s175e_boyfriend_throws_again_after_a_swing(GameTestHelper helper) {
        Boyfriend boyfriend = helper.spawn(ModEntities.BOYFRIEND.get(), 1, 2, 1);
        Pig pig = target(helper);
        boyfriend.swing(InteractionHand.MAIN_HAND);
        helper.runAfterDelay(AFTER_SWING, () -> {
            int before = shoesAround(helper, boyfriend);
            boyfriend.performRangedAttack(pig, 1.0f);
            int after = shoesAround(helper, boyfriend);
            helper.assertTrue(after == before + 1, "Boyfriend: no shoe thrown once the melee swing has ended (" + before
                    + " -> " + after + "; the swinging guard, orig Boyfriend.java:876, ENT-S-175)");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 20, batch = "pairSwingBoyfriend")
    public static void s175f_boyfriend_guard_refuses_a_throw_mid_swing(GameTestHelper helper) {
        Boyfriend boyfriend = helper.spawn(ModEntities.BOYFRIEND.get(), 1, 2, 1);
        Pig pig = target(helper);
        boyfriend.swing(InteractionHand.MAIN_HAND);
        int before = shoesAround(helper, boyfriend);
        boyfriend.performRangedAttack(pig, 1.0f);
        helper.assertTrue(shoesAround(helper, boyfriend) == before,
                "Boyfriend: a shoe thrown while a swing is in progress (orig Boyfriend.java:876 refuses it)");
        helper.succeed();
    }
}
