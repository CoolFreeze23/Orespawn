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
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * Dragonfly flight + hunt behaviour.
 *
 * <p>Extends {@link AmbientFlightGoal} with the 1.7.10 prey-hunting branch
 * from {@code Dragonfly.customServerAiStep()}: ~1 in 12 retarget rolls
 * the dragonfly scans its 10x6x10 AABB for the closest living entity in
 * sight that is on the orig prey whitelist — ants, butterflies, cockateils,
 * mosquitoes, fireflies and, unless {@link OreSpawnConfig#DRAGONFLY_HORSE_FRIENDLY}
 * is on, horses (orig Dragonfly.java:213-228, {@link #isPrey}; ENT-S-128 —
 * the port's {@code bbWidth <= 0.6} rule is gone) — paths to it, and calls
 * {@code doHurtTarget} whenever it comes within 6 blocks. Nothing else is
 * prey, players and other dragonflies included, as in 1.7.10.
 *
 * <p>Main-thread budget: the {@code getEntitiesOfClass} scan is gated
 * behind the AmbientFlightGoal retarget cadence ({@link Params#dragonfly})
 * AND a 1-in-12 roll on top, so on average a dragonfly does one
 * neighbour scan roughly every 3600 ticks (~3 minutes). Well within
 * the per-tick budget even with hundreds of flyers.
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
        // Melee when in bite range of the current target, matching 1.7.10.
        // We only hurt a genuine LivingEntity target, not the synthetic
        // wander BlockPos targets from the base class.
        LivingEntity target = (this.dragonfly.getTarget() != null)
                ? this.dragonfly.getTarget() : null;
        // orig Dragonfly.java:147-148 sits inside the :142 `!= PEACEFUL` branch: no bite on Peaceful,
        // whatever target the port still holds (ENT-S-114).
        if (this.mob.level().getDifficulty() != Difficulty.PEACEFUL
                && target != null && this.dragonfly.distanceToSqr(target) < HUNT_HURT_DIST_SQ) {
            this.dragonfly.doHurtTarget(target);
        }
    }

    @Override
    protected BlockPos pickRetarget() {
        // orig Dragonfly.java:142 — the 1-in-12 roll, then `difficulty != PEACEFUL` (ENT-S-114).
        if (this.mob.getRandom().nextInt(HUNT_ROLL_CHANCE) == 0
                && this.mob.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity prey = findPrey();
            if (prey != null) {
                this.dragonfly.setTarget(prey);
                return prey.blockPosition().above();
            }
        }
        return super.pickRetarget();
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
