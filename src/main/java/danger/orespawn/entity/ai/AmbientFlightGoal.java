package danger.orespawn.entity.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Generic "aimless wandering flyer" Goal for the small ambient insects in
 * OreSpawn — butterflies, moths, mosquitoes, fireflies, and their relatives.
 *
 * <p><b>What this replaces:</b> In 1.7.10 the flyers encoded their entire
 * flight loop inline inside {@code customServerAiStep}
 * (see {@code reference_1_7_10_source/sources/danger/orespawn/EntityButterfly.java:175-220}
 * and the matching blocks in {@code Firefly.java}, {@code EntityLunaMoth.java},
 * {@code EntityMosquito.java}, {@code Dragonfly.java}). The pattern was
 * always the same:
 * <ol>
 *   <li>Pick (or re-pick) a target {@link BlockPos} around the entity.</li>
 *   <li>Lerp delta-movement toward the target using mob-specific steer
 *       strengths and a shared blend factor.</li>
 *   <li>Update yaw to face motion.</li>
 * </ol>
 * That block of math did not belong in {@code customServerAiStep} — it
 * could not be disabled by sub-goals and made priority-based interruption
 * (panic, attack, breed) impossible. Extracting it into this Goal lets the
 * {@link net.minecraft.world.entity.ai.goal.GoalSelector} interleave it
 * correctly with vanilla goals and subclass extensions ({@link MosquitoFlightGoal},
 * {@link LunaMothFlightGoal}, {@link DragonflyHuntGoal}).
 *
 * <p><b>Flag ownership:</b> this goal takes {@code MOVE|LOOK} so lower-
 * priority goals that would also move or rotate the mob are suspended while
 * it runs. Panic goals with a lower priority number will still preempt it.
 *
 * <p><b>Cost:</b> one air {@code isAir} lookup per retarget attempt (up to
 * {@link Params#wanderAttempts}), rate-gated by {@link Params#retargetChance}.
 * At the default once-every-100-ticks cadence the amortised cost per flyer
 * is a fraction of a microsecond — safe on populated servers.
 *
 * <p><b>Thread safety:</b> runs only on the server tick thread via
 * {@code GoalSelector.tick()}; the {@code level().getBlockState} calls are
 * on already-loaded chunks (we never wander beyond {@code xzRange}).
 */
public class AmbientFlightGoal extends Goal {

    /**
     * Tunable steering + retarget parameters for an {@link AmbientFlightGoal}.
     * Values are a 1:1 match to the 1.7.10 per-mob magic numbers so flight
     * feels identical to the legacy mod.
     */
    public record Params(
            int xzRange, int yRange, int yBias,
            double steerXY, double steerY, double blend,
            float forwardSpeed, float yawDivisor,
            int retargetChance, double nearTargetDistSq,
            int wanderAttempts
    ) {
        public static Params butterfly() {
            return new Params(7, 6, 2, 0.5, 0.7, 0.1, 0.5f, 1.0f, 100, 4.0, 25);
        }
        public static Params firefly() {
            return new Params(4, 4, 2, 0.2, 0.7, 0.1, 0.2f, 4.0f, 40, 2.0, 25);
        }
        public static Params mosquito() {
            return new Params(7, 6, 2, 0.4, 0.7, 0.1, 0.5f, 1.0f, 100, 4.0, 25);
        }
        public static Params lunaMoth() {
            return new Params(7, 6, 2, 0.5, 0.7, 0.1, 0.5f, 1.0f, 100, 4.0, 25);
        }
        public static Params dragonfly() {
            return new Params(10, 5, 2, 0.5, 0.7, 0.3, 1.0f, 4.0f, 300, 4.5, 50);
        }
    }

    protected final Mob mob;
    protected final Params params;
    protected BlockPos flightTarget;

    public AmbientFlightGoal(Mob mob, Params params) {
        this.mob = mob;
        this.params = params;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return !this.mob.isPassenger() && !this.mob.isInWater();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.mob.isRemoved()) return;
        if (this.flightTarget == null) this.flightTarget = this.mob.blockPosition();

        // Retarget branch — mob-specific behaviour hooks in here.
        double distSq = this.flightTarget.distSqr(this.mob.blockPosition());
        boolean needNewTarget = this.mob.getRandom().nextInt(this.params.retargetChance()) == 0
                || distSq < this.params.nearTargetDistSq();
        if (needNewTarget) {
            BlockPos chosen = pickRetarget();
            if (chosen != null) this.flightTarget = chosen;
        }

        // Steering lerp — identical math to 1.7.10 but clamped into doubles.
        double dx = this.flightTarget.getX() + 0.5 - this.mob.getX();
        double dy = this.flightTarget.getY() + 0.1 - this.mob.getY();
        double dz = this.flightTarget.getZ() + 0.5 - this.mob.getZ();
        Vec3 motion = this.mob.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * this.params.steerXY() - motion.x) * this.params.blend();
        double my = motion.y + (Math.signum(dy) * this.params.steerY()  - motion.y) * this.params.blend();
        double mz = motion.z + (Math.signum(dz) * this.params.steerXY() - motion.z) * this.params.blend();
        this.mob.setDeltaMovement(mx, my, mz);

        // Yaw: atan2 of horizontal motion, divided by yawDivisor for slow
        // turn visuals on small flyers (Firefly / Dragonfly use 4.0, others 1.0).
        float targetYaw = (float) (Math.atan2(mz, mx) * Mth.RAD_TO_DEG) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.mob.getYRot());
        this.mob.zza = this.params.forwardSpeed();
        this.mob.setYRot(this.mob.getYRot() + yawDiff / this.params.yawDivisor());
    }

    /**
     * Default retarget: pick a random air block around the mob.
     * Subclasses override to inject player-seeking, torch-seeking, etc.
     */
    protected BlockPos pickRetarget() {
        for (int tries = this.params.wanderAttempts(); tries > 0; tries--) {
            BlockPos candidate = new BlockPos(
                    (int) this.mob.getX()
                            + this.mob.getRandom().nextInt(this.params.xzRange())
                            - this.mob.getRandom().nextInt(this.params.xzRange()),
                    (int) this.mob.getY()
                            + this.mob.getRandom().nextInt(this.params.yRange())
                            - this.params.yBias(),
                    (int) this.mob.getZ()
                            + this.mob.getRandom().nextInt(this.params.xzRange())
                            - this.mob.getRandom().nextInt(this.params.xzRange()));
            if (this.mob.level().getBlockState(candidate).isAir()) return candidate;
        }
        return null;
    }

    /** Exposed so the host mob can seed the target after hurt() etc. */
    public void setFlightTarget(BlockPos pos) {
        this.flightTarget = pos;
    }
}
