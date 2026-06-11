package danger.orespawn.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Shared rider-controlled flight/steering physics for the original OreSpawn
 * mounts (Dragon, Leon, Cephadrome, Ostrich, ThePrinceTeen, ThePrinceAdult).
 *
 * <p>The 1.7.10 originals each carried a near-identical hand-rolled physics
 * block inside {@code onLivingUpdate} (executed server-side); only the tuning
 * constants differed (see the per-entity {@link Config} instances, each cited
 * against the original source). This port runs the exact same math but on the
 * <em>controlling client</em> (vanilla horse-style client prediction): each
 * mount overrides {@code tickRidden} and calls {@link #tick} only when
 * {@code isControlledByLocalInstance()} is true (and short-circuits
 * {@code travel} while ridden so movement is not integrated twice), so server
 * and client never fight over the vehicle position (fixes BUG-020
 * rubber-banding).</p>
 *
 * <p>The physics block, in original order (orig Leon.java:741-889, orig
 * Dragon.java:919-1165, orig Cephadrome.java:703-835, orig Ostrich.java:401-535,
 * orig ThePrinceTeen.java:879-1087, orig ThePrinceAdult.java:859-1069):</p>
 * <ol>
 *   <li>clamp horizontal motion to ±2.0</li>
 *   <li>ground-hover: lift when a block sits {@code hoverProbeDepth} below,
 *       otherwise apply a gentle fall (the mount "glides")</li>
 *   <li>terrain-follow: scan blocks ahead/below and convert obstructions into
 *       upward motion so the mount climbs hills automatically</li>
 *   <li>clamp upward motion</li>
 *   <li>yaw follows the rider with a speed-dependent turn lag; pitch follows
 *       horizontal speed</li>
 *   <li>fly-up key: continuous lift (fliers) or a single boosted jump whose
 *       strength scales with run speed (Ostrich "FAST jump")</li>
 *   <li>W/S accelerate/decelerate along the facing, with per-mount caps and
 *       (for most mounts) a smoothed throttle ramp</li>
 *   <li>move, then apply per-mount friction (and the Ostrich's gravity)</li>
 * </ol>
 */
public final class RiderFlightController {

    /**
     * Per-mount tuning constants. All values must be copied number-for-number
     * from the original entity; see each mount's static config for citations.
     *
     * @param hover               whether the mount hovers (false for Ostrich, which is a runner)
     * @param hoverProbeDepth     depth below the mount probed for ground (orig {@code gh})
     * @param hoverLift           ΔmotionY added while ground is near
     * @param hoverPosLift        direct ΔposY applied while ground is near (orig 0.1)
     * @param hoverGravity        ΔmotionY subtracted while airborne (orig 0.018)
     * @param obstructionBaseDist base length of the forward terrain scan
     * @param obstructionVelScale scan length grows by {@code velocity * scale}
     * @param obstructionPerBlock obstruction factor added per solid block found
     * @param obstructionLift     multiplier converting the factor into ΔmotionY/ΔposY
     * @param obstructionScansUp  true = Ostrich variant (scans rows from feet level upward)
     * @param maxRiseSpeed        upper clamp on motionY
     * @param yawLagBase          base of the speed-dependent turn-lag term (orig 1.85 / Cephadrome 1.5)
     * @param yawVelThreshold     min speed before turn lag applies (orig 0.01 / Cephadrome 0.1)
     * @param invertPitchWhenRising Cephadrome-only: pitch flips negative while ascending (orig 360-2v)
     * @param jumpMode            true = fly-up key performs a 20-tick-cooldown jump (Ostrich)
     * @param flyUpBase           continuous-lift base (or jump strength in jump mode)
     * @param flyUpVelScale       continuous-lift speed bonus (or jump speed bonus in jump mode)
     * @param forwardAccel        throttle step while W held (orig {@code deltav}, bonuses pre-added)
     * @param maxForwardSpeed     forward speed cap (orig {@code max_speed})
     * @param backwardAccel       throttle step while S held (negative)
     * @param maxBackwardSpeed    reverse speed cap (orig 0.35 / Ostrich 0.25)
     * @param smoothAccel         true = throttle ramps in tenths (orig {@code deltasmooth}); Cephadrome applies it instantly
     * @param postMoveGravity     ΔmotionY subtracted after the move (Ostrich 0.25, fliers 0)
     * @param frictionXZ          horizontal friction applied after the move
     * @param frictionY           vertical friction applied after the move
     */
    public record Config(
            boolean hover, double hoverProbeDepth, double hoverLift, double hoverPosLift, double hoverGravity,
            int obstructionBaseDist, double obstructionVelScale, double obstructionPerBlock,
            double obstructionLift, boolean obstructionScansUp,
            double maxRiseSpeed,
            double yawLagBase, double yawVelThreshold, boolean invertPitchWhenRising,
            boolean jumpMode, double flyUpBase, double flyUpVelScale,
            double forwardAccel, double maxForwardSpeed, double backwardAccel, double maxBackwardSpeed,
            boolean smoothAccel,
            double postMoveGravity, double frictionXZ, double frictionY) {
    }

    private final Config config;
    /** Smoothed throttle (orig {@code deltasmooth} field, persists across ticks). */
    private float deltaSmooth = 0.0f;
    /** Ostrich jump cooldown (orig {@code didjump}; only counts down while the key is released). */
    private int jumpCooldown = 0;

    public RiderFlightController(Config config) {
        this.config = config;
    }

    /**
     * Runs one tick of rider-controlled movement. Must only be called on the
     * side that owns the vehicle's movement ({@code isControlledByLocalInstance()}).
     *
     * @param mount    the ridden mount
     * @param rider    the controlling player (steers with look + W/S)
     * @param flyUp    whether the rider is holding the fly-up key
     * @param flyDown  whether the rider is holding the fly-down key (port addition;
     *                 the 1.7.10 original only had the single UP/FAST key)
     */
    public void tick(Mob mount, Player rider, boolean flyUp, boolean flyDown) {
        Vec3 delta = mount.getDeltaMovement();
        // orig: motionX/motionZ clamped to ±2.0
        double motionX = Mth.clamp(delta.x, -2.0, 2.0);
        double motionY = delta.y;
        double motionZ = Mth.clamp(delta.z, -2.0, 2.0);
        double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);

        // Ground hover: lift near ground, glide-fall otherwise.
        if (this.config.hover()) {
            BlockPos below = BlockPos.containing(
                    mount.getX(), mount.getY() - this.config.hoverProbeDepth(), mount.getZ());
            if (!mount.level().getBlockState(below).isAir()) {
                motionY += this.config.hoverLift();
                mount.setPos(mount.getX(), mount.getY() + this.config.hoverPosLift(), mount.getZ());
            } else {
                motionY -= this.config.hoverGravity();
            }
        }

        // Terrain-follow scan ahead of the mount.
        double obstruction = scanObstructions(mount, velocity);
        if (obstruction > 0.0) {
            motionY += obstruction * this.config.obstructionLift();
            mount.setPos(mount.getX(),
                    mount.getY() + obstruction * this.config.obstructionLift(), mount.getZ());
        }
        if (motionY > this.config.maxRiseSpeed()) {
            motionY = this.config.maxRiseSpeed();
        }

        // Yaw follows the rider with a velocity-dependent lag (wide turns at cruise speed).
        double riderYaw = positiveMod(rider.getYRot(), 360.0);
        double mountYaw = positiveMod(mount.getYRot(), 360.0);
        double relativeYaw = positiveMod(riderYaw - mountYaw, 180.0);
        if (relativeYaw > 90.0) {
            relativeYaw -= 180.0;
        }
        if (velocity > this.config.yawVelThreshold()) {
            double turnLag = Mth.clamp(Math.abs(this.config.yawLagBase() - velocity), 0.01, 0.9);
            mount.setYRot(rider.getYRot() + (float) (relativeYaw * turnLag));
        } else {
            mount.setYRot(rider.getYRot());
        }
        float pitch = 2.0f * (float) velocity;
        if (this.config.invertPitchWhenRising() && motionY > 0.0) {
            // orig Cephadrome.java:776 — pitch = 360 - 2*velocity while rising (== negative pitch)
            pitch = -pitch;
        }
        mount.setXRot(pitch);
        mount.yBodyRot = mount.getYRot();
        mount.yHeadRot = mount.getYRot();

        // Fly-up key.
        if (this.config.jumpMode()) {
            // orig Ostrich.java:470-478 — jump 1.0 + velocity*6.0 with a 20-tick latch
            // that only counts down once the key is released (the "FAST" running jump).
            if (flyUp) {
                if (this.jumpCooldown == 0) {
                    motionY += this.config.flyUpBase() + velocity * this.config.flyUpVelScale();
                    this.jumpCooldown = 20;
                }
            } else if (this.jumpCooldown > 0) {
                --this.jumpCooldown;
            }
        } else {
            if (flyUp) {
                motionY += this.config.flyUpBase() + velocity * this.config.flyUpVelScale();
            }
            // Port addition: the descend key mirrors the original's single UP key.
            if (flyDown) {
                motionY -= this.config.flyUpBase() + velocity * this.config.flyUpVelScale();
            }
        }

        // Determine whether current motion runs against the rider's facing
        // (orig rhm/rhdir/rdv block) — if so the scalar speed is negative.
        double newVelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double motionHeading = Math.atan2(motionZ, motionX);
        double riderHeading = Math.toRadians(positiveMod(rider.getYRot() + 90.0f, 360.0));
        double headingDiff = Math.abs(motionHeading - riderHeading) % (Math.PI * 2.0);
        if (headingDiff > Math.PI) {
            headingDiff -= Math.PI * 2.0;
        }
        headingDiff = Math.abs(headingDiff);
        if (Math.abs(newVelocity) < 0.01) {
            headingDiff = 0.0;
        }
        if (headingDiff > 1.5) {
            newVelocity = -newVelocity;
        }

        // Throttle from the rider's forward input.
        double forwardInput = rider.zza;
        double maxSpeed = this.config.maxForwardSpeed();
        if (Math.abs(forwardInput) > 0.001) {
            double accel;
            if (forwardInput > 0.0) {
                accel = this.config.forwardAccel();
                if (this.config.smoothAccel()) {
                    if (this.deltaSmooth < 0.0f) {
                        this.deltaSmooth = 0.0f;
                    }
                    this.deltaSmooth = (float) Math.min(this.deltaSmooth + accel / 10.0, accel);
                } else {
                    this.deltaSmooth = (float) accel;
                }
            } else {
                maxSpeed = this.config.maxBackwardSpeed();
                accel = this.config.backwardAccel();
                if (this.config.smoothAccel()) {
                    if (this.deltaSmooth > 0.0f) {
                        this.deltaSmooth = 0.0f;
                    }
                    this.deltaSmooth = (float) Math.max(this.deltaSmooth + accel / 10.0, accel);
                } else {
                    this.deltaSmooth = (float) accel;
                }
            }
            newVelocity += this.deltaSmooth;
            newVelocity = Mth.clamp(newVelocity, -maxSpeed, maxSpeed);
        }

        // Project the scalar speed back onto the mount's facing.
        double moveAngle;
        double speed;
        if (newVelocity >= 0.0) {
            moveAngle = Math.toRadians(mount.getYRot() + 90.0f);
            speed = newVelocity;
        } else {
            moveAngle = Math.toRadians(mount.getYRot() + 270.0f);
            speed = -newVelocity;
        }
        motionX = Math.cos(moveAngle) * speed;
        motionZ = Math.sin(moveAngle) * speed;

        mount.setDeltaMovement(motionX, motionY, motionZ);
        mount.move(MoverType.SELF, mount.getDeltaMovement());

        // Friction (and the runner's gravity) after the move.
        Vec3 afterMove = mount.getDeltaMovement();
        mount.setDeltaMovement(
                afterMove.x * this.config.frictionXZ(),
                (afterMove.y - this.config.postMoveGravity()) * this.config.frictionY(),
                afterMove.z * this.config.frictionXZ());
        mount.calculateEntityAnimation(false);
    }

    /**
     * Forward terrain scan; returns the accumulated obstruction factor.
     * Standard variant (orig e.g. Leon.java:766-777) scans rows below the mount;
     * the Ostrich variant (orig Ostrich.java:417-427) scans rows from one block
     * below the feet upward so the bird hops onto rising terrain.
     */
    private double scanObstructions(Mob mount, double velocity) {
        double obstruction = 0.0;
        int dist = this.config.obstructionBaseDist() + (int) (velocity * this.config.obstructionVelScale());
        double yawRad = Math.toRadians(mount.getYRot() + 90.0f);
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        if (this.config.obstructionScansUp()) {
            for (int k = 0; k < dist; ++k) {
                for (int i = 1; i < dist * 2; ++i) {
                    BlockPos pos = BlockPos.containing(
                            mount.getX() + i * cos, mount.getY() - 1 + k, mount.getZ() + i * sin);
                    if (!mount.level().getBlockState(pos).isAir()) {
                        obstruction += this.config.obstructionPerBlock();
                    }
                }
            }
        } else {
            for (int k = 1; k < dist; ++k) {
                for (int i = 1; i < dist * 2; ++i) {
                    BlockPos pos = BlockPos.containing(
                            mount.getX() + i * cos, mount.getY() - k, mount.getZ() + i * sin);
                    if (!mount.level().getBlockState(pos).isAir()) {
                        obstruction += this.config.obstructionPerBlock();
                    }
                }
            }
        }
        return obstruction;
    }

    /** Floored modulo that always returns a value in {@code [0, modulus)}. */
    private static double positiveMod(double value, double modulus) {
        double result = value % modulus;
        while (result < 0.0) {
            result += modulus;
        }
        return result;
    }
}
