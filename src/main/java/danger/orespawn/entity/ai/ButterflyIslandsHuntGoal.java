package danger.orespawn.entity.ai;

import danger.orespawn.ModDimensionKeys;
import danger.orespawn.entity.EntityButterfly;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The butterfly's flight (orig EntityButterfly.java:145-181 {@code updateAITasks}) with the Islands-dimension
 * vampire hunt 1.7.10 ran in the else-branch of its retarget test: {@link AmbientFlightGoal}'s butterfly preset
 * for the wander (:151-160 the retarget, :171-180 the steering) plus orig :161-169 — on a tick whose 1-in-100 /
 * near-target retarget did not fire, a 1-in-10 roll, then the Islands dimension ({@code DimensionID4},
 * {@link ModDimensionKeys#ISLANDS}), the type-1 skin ({@code butterfly_type == 1}, the {@code vbutterfly1.png}
 * of :83-86) and {@code difficulty != PEACEFUL}, in that order (the roll is spent everywhere, the rest short-
 * circuits); then the scan (:217-230 — no PlayNicely gate in orig) over the butterfly's box grown by 8/5/8, sorted
 * by the shared {@link GenericTargetSorter} (:48, :56, :219), the first candidate accepted by the filter
 * (:194-215: Peaceful, null / self / dead, line of sight, a player when not creative, else a horse); with prey the
 * flight target is set onto it (:165, {@code (int)} casts as orig) and, inside distSq 6, the bite (:166-168,
 * {@code EntityButterfly.doHurtTarget}).
 *
 * <p>{@link #tick} re-states the base flow rather than calling {@code super.tick()}: the base has no hook between
 * its retarget test and its steering, and orig's hunt sits exactly there — the retarget and steering lines are the
 * base's :104-113 and :116-131 (orig :147-160, :171-180) unchanged. Registered by
 * {@code EntityButterfly.registerGoals} in the base flight goal's slot; the Mothra inherits that registration as it
 * inherited orig's {@code updateAITasks} (Mothra.java:169). The hunt runs inside the flight goal's {@code canUse}
 * ({@code !isPassenger() && !isInWater()}, AmbientFlightGoal :88-90) — a gate orig's {@code updateAITasks} did not
 * have, disclosed (ENT-S-117 refuter A, N4). ENT-S-117.</p>
 */
public class ButterflyIslandsHuntGoal extends AmbientFlightGoal {
    /** orig EntityButterfly.java:161 — the hunt roll, {@code nextInt(10) == 0}. */
    private static final int HUNT_ROLL_BOUND = 10;
    /** orig EntityButterfly.java:161 — the skin type that hunts ({@code butterfly_type == 1}). */
    private static final int VAMPIRE_TYPE = 1;
    /** orig EntityButterfly.java:166 — the bite reach, {@code distSq < 6.0}. */
    private static final double BITE_DIST_SQ = 6.0;

    private final EntityButterfly butterfly;
    /** orig EntityButterfly.java:48 {@code TargetSorter}, :56 {@code new GenericTargetSorter(this)}. */
    private final GenericTargetSorter targetSorter;

    public ButterflyIslandsHuntGoal(EntityButterfly butterfly) {
        super(butterfly, Params.butterfly());
        this.butterfly = butterfly;
        this.targetSorter = new GenericTargetSorter(butterfly);
    }

    @Override
    public void tick() {
        if (this.mob.isRemoved()) return;                                                        // orig :147-149; base :104
        if (this.flightTarget == null) this.flightTarget = this.mob.blockPosition();               // orig :151-153; base :105

        double distSq = this.flightTarget.distSqr(this.mob.blockPosition());                       // base :108
        if (this.mob.getRandom().nextInt(this.params.retargetChance()) == 0                        // orig :154; base :109-110
                || distSq < this.params.nearTargetDistSq()) {
            BlockPos chosen = this.pickRetarget();                                                 // orig :155-160; base :112-113
            if (chosen != null) this.flightTarget = chosen;
        } else if (this.mob.getRandom().nextInt(HUNT_ROLL_BOUND) == 0                              // orig :161 — the roll first,
                && this.mob.level().dimension() == ModDimensionKeys.ISLANDS                        //   then DimensionID4,
                && this.butterfly.getButterflyType() == VAMPIRE_TYPE                               //   the vampire skin,
                && this.mob.level().getDifficulty() != Difficulty.PEACEFUL) {                      //   and not Peaceful
            LivingEntity prey = this.findSomethingToAttack();                                      // orig :162-163
            if (prey != null) {                                                                    // orig :164
                this.flightTarget = new BlockPos((int) prey.getX(), (int) (prey.getY() + 1.0), (int) prey.getZ()); // orig :165
                if (this.mob.distanceToSqr(prey) < BITE_DIST_SQ) {                                 // orig :166
                    this.mob.doHurtTarget(prey);                                                   // orig :167
                }
            }
        }

        // orig :171-180 — the steering; the base's :116-131 unchanged.
        double dx = this.flightTarget.getX() + 0.5 - this.mob.getX();
        double dy = this.flightTarget.getY() + 0.1 - this.mob.getY();
        double dz = this.flightTarget.getZ() + 0.5 - this.mob.getZ();
        Vec3 motion = this.mob.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * this.params.steerXY() - motion.x) * this.params.blend();
        double my = motion.y + (Math.signum(dy) * this.params.steerY()  - motion.y) * this.params.blend();
        double mz = motion.z + (Math.signum(dz) * this.params.steerXY() - motion.z) * this.params.blend();
        this.mob.setDeltaMovement(mx, my, mz);
        float targetYaw = (float) (Math.atan2(mz, mx) * Mth.RAD_TO_DEG) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.mob.getYRot());
        this.mob.zza = this.params.forwardSpeed();
        this.mob.setYRot(this.mob.getYRot() + yawDiff / this.params.yawDivisor());
    }

    /**
     * orig EntityButterfly.java:217-230 {@code findSomethingToAttack} — no PlayNicely gate (orig had none here):
     * every {@code EntityLivingBase} whose box meets the butterfly's box grown by 8/5/8 (:218 — players and the
     * butterfly itself included), sorted by the {@link GenericTargetSorter} (:219), the first the filter accepts
     * (:223-228), else null (:229); the pick is never stored (:163-168 act on it for the tick).
     * {@link TargetSelection#firstMatch} is the sort-and-loop, stable ties preserved (OPT-021). ENT-S-117.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> candidates = this.mob.level().getEntitiesOfClass(LivingEntity.class,
                this.mob.getBoundingBox().inflate(8.0, 5.0, 8.0));                                // orig :218
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget);  // orig :219-229
    }

    /**
     * orig EntityButterfly.java:194-215 {@code isSuitableTarget}, in the original's order: Peaceful → false
     * (:195-197), null / self / dead (:198-206), line of sight (:207-209), a player when not creative (:210-213 —
     * {@code isCreativeMode}, the port's {@code instabuild}, ENT-S-107), else a horse (:214 — {@code EntityHorse};
     * {@link AbstractHorse} is the 1.21.1 base of the horse, donkey, mule, skeleton and zombie horse it covered, and
     * of the later llama, trader llama and camel, the {@code DragonflyHuntGoal} mapping). ENT-S-117.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.mob.level().getDifficulty() == Difficulty.PEACEFUL) return false;               // orig :195-197
        if (target == null || target == this.mob || !target.isAlive()) return false;             // orig :198-206
        if (!this.mob.getSensing().hasLineOfSight(target)) return false;                         // orig :207-209
        if (target instanceof Player player) return !player.getAbilities().instabuild;           // orig :210-213
        return target instanceof AbstractHorse;                                                  // orig :214
    }
}
