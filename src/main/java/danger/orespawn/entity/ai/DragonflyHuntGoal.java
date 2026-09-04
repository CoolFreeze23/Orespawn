package danger.orespawn.entity.ai;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.EntityButterfly;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityMosquito;
import danger.orespawn.entity.Firefly;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * Dragonfly flight + hunt behaviour.
 *
 * <p>Extends {@link AmbientFlightGoal} with the 1.7.10 prey-hunting branch
 * from {@code Dragonfly.customServerAiStep()}: on every tick the flight
 * retarget did NOT fire, a 1-in-12 roll (orig Dragonfly.java:142, the retarget's
 * else branch — {@link #onRetargetSkipped}) has
 * the dragonfly scan its 10x6x10 AABB for the closest living entity in
 * sight that is on the orig prey whitelist — ants, butterflies, cockateils,
 * mosquitoes, fireflies and, unless {@link OreSpawnConfig#DRAGONFLY_HORSE_FRIENDLY}
 * is on, horses (orig Dragonfly.java:213-228, {@link #isPrey}; ENT-S-128 —
 * the port's {@code bbWidth <= 0.6} rule is gone) — paths to it, and calls
 * {@code doHurtTarget} once per hunt pass when it stands within 6 blocks (orig :146-148; ENT-S-129 —
 * the prey is never retained). Nothing else is
 * prey, players and other dragonflies included, as in 1.7.10.
 *
 * <p>Main-thread budget: the {@code getEntitiesOfClass} scan runs on the
 * 1-in-12 roll of every non-retarget tick — one neighbour scan roughly every
 * 12 ticks per dragonfly, as in 1.7.10 (orig :142). HEAD had nested the roll
 * INSIDE the retarget (one scan roughly every 3600 ticks, the inverted cadence
 * the targeting ledger's Dragonfly scan-set row named; ENT-S-135).
 *
 * <p>Peaceful: orig Dragonfly.java:142 gates the whole hunt branch — the scan, the flight retarget
 * onto the prey and the bite at :147-148 — on {@code difficulty != PEACEFUL}, and :198 answers
 * false at the head of the filter; both are transcribed below. An Animal does not despawn on
 * Peaceful, so without them the port hunted there (ENT-S-114).
 *
 * <p>PlayNicely: orig Dragonfly.java:232-234 answers null at the head of {@code findSomethingToAttack};
 * read live at the head of {@link #findPrey}, the port's shape of that scan (ENT-S-115).</p>
 */
public class DragonflyHuntGoal extends AmbientFlightGoal {
    private static final double HUNT_HURT_DIST_SQ = 6.0;
    private static final int HUNT_ROLL_CHANCE = 12;
    private static final double SCAN_X = 10.0, SCAN_Y = 6.0, SCAN_Z = 10.0;

    private final EntityDragonfly dragonfly;

    public DragonflyHuntGoal(EntityDragonfly dragonfly) {
        super(dragonfly, Params.dragonfly());
        this.dragonfly = dragonfly;
    }

    @Override
    public void tick() {
        super.tick();
        // orig Dragonfly.java:145-148 — the pass's pick is acted on for THIS tick alone: the flight target moved onto it
        // (:145, onRetargetSkipped) and one bite if it stands within distSq 6 (:146-148); it was never stored, and the next
        // pass re-derives it. The pick passes through the slot for this tick only (onRetargetSkipped's hand-off, read back
        // here) and is dropped once acted on — HEAD kept it and bit every tick (ENT-S-129).
        LivingEntity target = this.dragonfly.getTarget();
        if (target == null) return;
        // orig Dragonfly.java:147-148 sits inside the :142 `!= PEACEFUL` branch: no bite on Peaceful,
        // whatever target the port still holds (ENT-S-114).
        if (this.mob.level().getDifficulty() != Difficulty.PEACEFUL
                && this.dragonfly.distanceToSqr(target) < HUNT_HURT_DIST_SQ) {
            this.dragonfly.doHurtTarget(target);
        }
        this.dragonfly.setTarget(null); // orig :146-148 — one bite per pass, nothing retained (ENT-S-129)
    }

    /**
     * orig Dragonfly.java:142-149 — the hunt is the ELSE branch of the :124 flight retarget ({@code else if
     * (rand.nextInt(12) == 0 && difficulty != PEACEFUL)}): rolled on every tick the 1-in-300 / near retarget did NOT fire,
     * so a dragonfly hunts about every 12 ticks. HEAD had put this inside {@code pickRetarget}, i.e. INSIDE the retarget —
     * the roll was only reached when the retarget fired (≈ every 3600 ticks) and the wander pick was skipped for the prey's
     * position on the same tick; the flight retarget itself (:124-141) is now super's alone. ENT-S-135 (the targeting
     * ledger's Dragonfly scan-set row). The roll first, then the difficulty (ENT-S-114).
     */
    @Override
    protected void onRetargetSkipped() {
        if (this.mob.getRandom().nextInt(HUNT_ROLL_CHANCE) == 0
                && this.mob.level().getDifficulty() != Difficulty.PEACEFUL) {   // orig :142
            LivingEntity prey = findPrey();                                   // orig :144
            if (prey != null) {
                this.dragonfly.setTarget(prey);                                // the pass's hand-off for this tick's bite (tick reads it back and drops it) — never retained (ENT-S-129)
                this.flightTarget = prey.blockPosition().above();             // orig :145 — the flight target moved onto the prey; the steering below follows it this tick
            }
        }
    }

    private LivingEntity findPrey() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig Dragonfly.java:232-234 — PlayNicely != 0 returns null ahead of the scan (ENT-S-115)
        List<LivingEntity> candidates = this.mob.level().getEntitiesOfClass(LivingEntity.class,
                this.mob.getBoundingBox().inflate(SCAN_X, SCAN_Y, SCAN_Z));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly,
        // and the predicate keeps the original filter chain's order/short-circuit.
        return TargetSelection.firstMatch(candidates,
                Comparator.comparingDouble(this.mob::distanceToSqr),
                candidate -> this.mob.level().getDifficulty() != Difficulty.PEACEFUL // orig Dragonfly.java:198-200 — PEACEFUL → false ahead of every other check (ENT-S-114)
                        && candidate != this.mob && candidate.isAlive()
                        && this.dragonfly.getSensing().hasLineOfSight(candidate) // orig Dragonfly.java:210-212 — canSee, ahead of the whitelist; HEAD's step, at the orig position again (ENT-S-128)
                        && isPrey(candidate)); // orig Dragonfly.java:213-228 — the prey whitelist, the last step (ENT-S-128)
    }

    /**
     * orig Dragonfly.java:213-228 — the prey whitelist, in the original's order: EntityAnt (:213), EntityButterfly
     * (:216), Cockateil (:219), EntityMosquito (:222), Firefly (:225) — the same hierarchies in both trees, so the
     * Red / Rainbow / Unstable ants and the Termite ride along under EntityAnt, the Luna Moth and Mothra under
     * EntityButterfly — and a horse (:228, {@code EntityHorse} → {@link AbstractHorse}, the port's mapping at
     * ButterflyIslandsHuntGoal :118 and EntityCage :123) unless {@code DragonflyHorseFriendly} — the port's
     * {@link OreSpawnConfig#DRAGONFLY_HORSE_FRIENDLY}, read live — is set. Nothing else is prey: HEAD's
     * {@code bbWidth <= 0.6} rule took chickens, bats, rabbits, cats, silverfish, endermites, baby animals and
     * the Cricket / Chipmunk-sized OreSpawn mobs, and never a horse (1.4 wide). ENT-S-128.
     */
    private static boolean isPrey(LivingEntity candidate) {
        if (candidate instanceof EntityAnt) return true;       // orig Dragonfly.java:213-215
        if (candidate instanceof EntityButterfly) return true; // orig :216-218
        if (candidate instanceof Cockateil) return true;       // orig :219-221
        if (candidate instanceof EntityMosquito) return true;  // orig :222-224
        if (candidate instanceof Firefly) return true;         // orig :225-227
        return candidate instanceof AbstractHorse && !OreSpawnConfig.DRAGONFLY_HORSE_FRIENDLY.get(); // orig :228 — EntityHorse && DragonflyHorseFriendly == 0
    }
}
