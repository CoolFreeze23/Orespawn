package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class EntityRotator extends Monster {
    private BlockPos currentFlightTarget = null;
    private int busyFighting = 0;

    public EntityRotator(EntityType<? extends EntityRotator> type, Level level) {
        super(type, level);
        this.xpReward = 35;
        this.noPhysics = true;
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.busyFighting != 0) return false;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (this.level().isClientSide && this.random.nextInt(10) == 1) {
            this.level().addParticle(ParticleTypes.FIREWORK,
                    this.getX(), this.getY() + 1.4, this.getZ(),
                    (this.random.nextFloat() - this.random.nextFloat()) / 4.0,
                    (this.random.nextFloat() - this.random.nextFloat()) / 4.0,
                    (this.random.nextFloat() - this.random.nextFloat()) / 4.0);
        }

        this.busyFighting = 0;
        LivingEntity target = this.findSomethingToAttack();
        if (target != null) {
            if (this.level().isClientSide) {
                double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY() + 1.4, this.getZ(),
                        Math.cos(angle), (target.getY() - this.getY()) / 10.0, Math.sin(angle));
            }
            this.busyFighting = 1;
        }

        if (!this.level().isClientSide && !this.isPersistenceRequired() && this.busyFighting == 0) {
            long t = this.level().getDayTime() % 24000L;
            if (t < 12000L && this.random.nextInt(400) == 1) {
                this.discard();
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof AbstractArrow) {
            return false;
        }
        boolean ret = super.hurt(source, amount);
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = attacker.blockPosition();
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.random.nextInt(300) == 0 || this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            int keepTrying = 50;
            boolean found = false;
            while (!found && keepTrying > 0) {
                int xdir = this.random.nextInt(10) + 8;
                int zdir = this.random.nextInt(10) + 8;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;
                BlockPos newTarget = BlockPos.containing(
                        this.getX() + xdir,
                        this.getY() + this.random.nextInt(6) - 3,
                        this.getZ() + zdir);
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --keepTrying;
            }
        } else if (this.random.nextInt(9) == 2) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                double angle = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                this.currentFlightTarget = BlockPos.containing(
                        target.getX() + 2.5 * Math.cos(angle + Math.PI / 2),
                        target.getY(),
                        target.getZ() + 2.5 * Math.sin(angle + Math.PI / 2));
                if (this.distanceToSqr(target) < 9.0) {
                    this.doHurtTarget(target);
                }
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.4 - motion.x) * 0.2;
        double my = motion.y + (Math.signum(dy) * 0.7 - motion.y) * 0.2;
        double mz = motion.z + (Math.signum(dz) * 0.4 - motion.z) * 0.2;
        this.setDeltaMovement(mx, my, mz);

        float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(yaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 4.0f);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityRotator) return false;
        if (target instanceof Peacock) return false;
        if (target instanceof CloudShark) return false;
        if (target instanceof EntityTerribleTerror) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().invulnerable;
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(12.0, 10.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity target : targets) {
            if (this.isSuitableTarget(target)) return target;
        }
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "vortexlive"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }
}
