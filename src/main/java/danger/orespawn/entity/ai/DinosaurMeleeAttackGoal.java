package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.world.entity.Mob;

/**
 * Dinosaur/reptile flavour of the legacy 1.7.10 "random-cadence melee" pattern
 * (see {@link BugMeleeAttackGoal} for the base tick loop). Exists as a
 * subclass purely so the dinosaur family gets its own tuning presets and a
 * name that matches its role in the registry — the runtime behaviour is
 * inherited unchanged.
 *
 * <p>Each {@link Params} preset was extracted verbatim from the matching
 * 1.7.10 {@code customServerAiStep} ({@code func_70619_bc}) in
 * {@code reference_1_7_10_source}. The fields map as:
 * <ul>
 *   <li>{@code navigateSpeed}    → {@code func_75497_a} speed arg</li>
 *   <li>{@code attackReachBonus} → base constant added to {@code bbWidth/2}</li>
 *   <li>{@code cadence}          → outer {@code nextInt(N) == 0/1} gate</li>
 *   <li>{@code outer/innerAttackRoll} → nested swing dice</li>
 *   <li>{@code forgetTargetRoll} → optional target-drop chance (0 = never)</li>
 * </ul>
 */
public class DinosaurMeleeAttackGoal extends BugMeleeAttackGoal {

    public DinosaurMeleeAttackGoal(Mob mob, IntConsumer setAttacking, Params params) {
        super(mob, setAttacking, params);
    }

    public static final class Presets {
        private Presets() {}

        public static Params alosaurus()        { return new Params(1.25, 4.0, 5, 4, 5,   0, 12.0, 5.0); }
        public static Params trex()             { return new Params(1.25, 4.0, 5, 4, 5,   0, 20.0, 6.0, true); } // ENT-S-129: stands down under PlayNicely (orig TRex.java:185-187); the 1-in-200 of :189 is the pass's (TRex.selectTarget)
        public static Params nastysaurus()      { return new Params(1.25, 4.5, 5, 4, 5,   0, 32.0, 8.0, true); } // ENT-S-129: the 1-in-250 of orig Nastysaurus.java:219 is rolled inside the 1-in-5 pass on rt alone (Nastysaurus.selectTarget), not here every tick on any target; stands down under PlayNicely (:215-217)
        public static Params pointysaurus()     { return new Params(1.25, 4.0, 6, 5, 6,   0, 12.0, 5.0, true); } // ENT-S-129: the 1-in-250 of orig Pointysaurus.java:189 is rolled inside the 1-in-6 pass on rt alone (Pointysaurus.customServerAiStep); stands down under PlayNicely (:185-187)
        public static Params cryolophosaurus()  { return new Params(1.25, Math.sqrt(5.0), 5, 12, 14, 200, 9.0, 2.0); }
        public static Params basilisk()         { return new Params(1.25, 6.0, 5, 3, 4,   0, 24.0, 7.0); }
        // TF-026 (2026-08-11): innerAttackRoll was transcribed 0, crashing
        // nextInt(0) on the first in-reach attack once TF-001 made the
        // entity spawnable — orig WaterDragon.java:597/603 is cadence
        // nextInt(5)==1, then nextInt(4)==0 || nextInt(5)==1.
        public static Params waterDragon()      { return new Params(1.0,  4.0, 5, 4, 5, 200, 14.0, 5.0); }
        public static Params seaViper()         { return new Params(1.5,  4.5, 5, 2, 4,   0, 18.0, 4.0, true); } // ENT-S-129: stands down under PlayNicely (orig SeaViper.java:531-543 — the stored-target read sat inside the gated scan)
    }
}
