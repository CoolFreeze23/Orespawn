package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class EntityVortex extends Monster {
    private static final long DAY_LENGTH_TICKS = 24000L;
    private static final long DAYTIME_DESPAWN_BEFORE = 12000L;
    private static final double PULL_RANGE_DIST_SQ = 81.0;
    private static final double PULL_DISTANCE_SCALE = 10.0;
    private static final double PLAYER_VERTICAL_PULL_MULT = 2.0;
    private static final int WINDED_COOLDOWN_TICKS = 20;

    /**
     * Maximum upward velocity the Vortex can apply with its grab launch.
     * Tuned so a launched player tops out around y=+90 above the cast site,
     * matching the 1.7.10 "into the stratosphere" feel without exceeding the
     * 10.0 vanilla velocity sanity cap (anything above ~10 starts triggering
     * MovedTooQuickly disconnects on stricter servers). The horizontal
     * components are clamped separately to keep the trajectory mostly
     * vertical so the launch reads as a tornado uplift rather than a punt.
     */
    private static final double LAUNCH_UPWARD_VELOCITY = 4.0;
    private static final double LAUNCH_HORIZONTAL_VELOCITY = 0.6;
    /** Cooldown ticks between successive launches per Vortex (1.5s). */
    private static final int LAUNCH_COOLDOWN_TICKS = 30;
    private int launchCooldownTicks = 0;

    private BlockPos currentFlightTarget = null;
    private int lastX = 0;
    private int lastY = 0;
    private int lastZ = 0;
    private int stuckCount = 0;
    private int windedCooldownTicks = 0;

    public EntityVortex(EntityType<? extends EntityVortex> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        LivingEntity pullTarget = findSomethingToAttack();
        if (pullTarget != null && this.level().isClientSide) {
            for (int i = 0; i < 20; ++i) {
                double smokeRadius = this.random.nextDouble() * 3.5;
                double smokeHeightOffset = smokeRadius * smokeRadius;
                double dir = this.random.nextDouble() * 2.0 * Math.PI;
                double dx = Math.cos(dir - Math.PI) * smokeHeightOffset / 2.0;
                double dz = Math.sin(dir - Math.PI) * smokeHeightOffset / 2.0;
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + dx, this.getY() + 0.75 + smokeHeightOffset, this.getZ() + dz,
                        Math.cos(dir + Math.PI / 2.0) * this.random.nextFloat() / 4.0,
                        this.random.nextFloat() / 2.0,
                        Math.sin(dir + Math.PI / 2.0) * this.random.nextFloat() / 4.0);
            }
        }

        if (this.random.nextInt(200) == 1) {
            this.heal(1.0f);
        }

        if (!this.level().isClientSide) {
            long dayTimeInCycle = this.level().getDayTime() % DAY_LENGTH_TICKS;
            if (dayTimeInCycle < DAYTIME_DESPAWN_BEFORE && this.random.nextInt(500) == 1) {
                this.discard();
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.lastX == (int) this.getX() && this.lastY == (int) this.getY() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastY = (int) this.getY();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.windedCooldownTicks > 0) {
            --this.windedCooldownTicks;
        }
        if (this.launchCooldownTicks > 0) {
            --this.launchCooldownTicks;
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());

        if (this.stuckCount > 30 || this.random.nextInt(300) == 0 || distSq < 4.0) {
            this.stuckCount = 0;
            int keepTrying = 50;
            while (keepTrying > 0) {
                int xdir = (this.random.nextInt(14) + 10) * (this.random.nextInt(2) == 0 ? -1 : 1);
                int zdir = (this.random.nextInt(14) + 10) * (this.random.nextInt(2) == 0 ? -1 : 1);

                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(6) - 3,
                        (int) this.getZ() + zdir);

                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
                --keepTrying;
            }
        }

        LivingEntity currentTarget = findSomethingToAttack();
        if (currentTarget != null) {
            this.currentFlightTarget = currentTarget.blockPosition();
            double distSqToTarget = this.distanceToSqr(currentTarget);
            if (distSqToTarget < PULL_RANGE_DIST_SQ && this.windedCooldownTicks == 0) {
                double angleAwayFromTarget = Math.atan2(this.getZ() - currentTarget.getZ(), this.getX() - currentTarget.getX());
                double verticalPullMultiplier = (currentTarget instanceof Player) ? PLAYER_VERTICAL_PULL_MULT : 1.0;
                double pullStrength = (PULL_DISTANCE_SCALE - Math.sqrt(distSqToTarget)) * 0.1;
                currentTarget.push(
                        Math.cos(angleAwayFromTarget) * pullStrength,
                        (PULL_DISTANCE_SCALE - Math.sqrt(distSqToTarget)) * 0.05 * verticalPullMultiplier,
                        Math.sin(angleAwayFromTarget) * pullStrength);
            }
            double attackRange = (4.0 + currentTarget.getBbWidth() / 2.0);
            if (distSqToTarget < attackRange * attackRange && this.random.nextInt(8) == 2) {
                this.doHurtTarget(currentTarget);
                if (this.launchCooldownTicks == 0) {
                    skywardLaunch(currentTarget);
                    this.launchCooldownTicks = LAUNCH_COOLDOWN_TICKS;
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.2;
        double newMy = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double newMz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.2;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDiff / 4.0f);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition();
        }
        this.windedCooldownTicks = WINDED_COOLDOWN_TICKS;
        return ret;
    }

    /**
     * The signature 1.7.10 attack: physically grab the victim and hurl
     * them straight up (with a small horizontal nudge inherited from the
     * existing pull vector). Velocity components are clamped, NaN-guarded,
     * and validated against the vanilla 10.0 movement cap so neither a
     * teleport-glitched target nor a co-located target can produce
     * Infinity/NaN deltaMovement values that would crash the physics
     * tick. Players also get hasImpulse set so the server doesn't reject
     * the upward velocity as illegal client motion.
     */
    private void skywardLaunch(LivingEntity victim) {
        if (victim == null || !victim.isAlive()) return;

        Vec3 existing = victim.getDeltaMovement();
        double ex = existing.x;
        double ey = existing.y;
        double ez = existing.z;
        if (Double.isNaN(ex) || Double.isInfinite(ex)) ex = 0.0;
        if (Double.isNaN(ey) || Double.isInfinite(ey)) ey = 0.0;
        if (Double.isNaN(ez) || Double.isInfinite(ez)) ez = 0.0;

        double dx = victim.getX() - this.getX();
        double dz = victim.getZ() - this.getZ();
        double horizontalMagnitude = Math.sqrt(dx * dx + dz * dz);
        double pushX;
        double pushZ;
        if (horizontalMagnitude < 1.0e-4) {
            pushX = 0.0;
            pushZ = 0.0;
        } else {
            pushX = (dx / horizontalMagnitude) * LAUNCH_HORIZONTAL_VELOCITY;
            pushZ = (dz / horizontalMagnitude) * LAUNCH_HORIZONTAL_VELOCITY;
        }

        double newX = Mth.clamp(ex + pushX, -9.5, 9.5);
        double newY = Mth.clamp(ey + LAUNCH_UPWARD_VELOCITY, -9.5, 9.5);
        double newZ = Mth.clamp(ez + pushZ, -9.5, 9.5);
        victim.setDeltaMovement(newX, newY, newZ);
        victim.hasImpulse = true;
        victim.fallDistance = 0.0f;
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 10.0, 16.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof EntityVortex) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Player p) return !p.getAbilities().invulnerable;
        return true;
    }
}
