package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class CloudShark extends Monster {
    private static final int MAX_HEALTH = 20;
    private static final double ATTACK_DAMAGE = 6.0;
    private static final int MIN_CRUISE_ALTITUDE = 120;
    private static final int MAX_CRUISE_ALTITUDE = 140;
    private static final int ALTITUDE_BIAS_UP = 2;
    private static final int ALTITUDE_BIAS_DOWN = -2;

    private BlockPos currentFlightTarget = null;

    public CloudShark(EntityType<? extends CloudShark> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurtApplied = super.hurt(source, amount);
        Entity damageSourceEntity = source.getEntity();
        if (damageSourceEntity != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = damageSourceEntity.blockPosition();
        }
        return hurtApplied;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        int verticalAltitudeBias = 0;
        if ((int) this.getY() < MIN_CRUISE_ALTITUDE) verticalAltitudeBias = ALTITUDE_BIAS_UP;
        if ((int) this.getY() > MAX_CRUISE_ALTITUDE) verticalAltitudeBias = ALTITUDE_BIAS_DOWN;

        if (this.random.nextInt(300) == 0 || this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            int pickNewTargetAttempts = 50;
            boolean found = false;
            while (!found && pickNewTargetAttempts > 0) {
                int xdir = this.random.nextInt(10) + 8;
                int zdir = this.random.nextInt(10) + 8;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;
                BlockPos newTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(5) - 2 + verticalAltitudeBias,
                        (int) this.getZ() + zdir);
                if (this.level().getBlockState(newTarget).is(Blocks.AIR)) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --pickNewTargetAttempts;
            }
        }

        if (this.random.nextInt(9) == 2) {
            Player target = this.level().getNearestPlayer(this, 12.0);
            if (target != null && !target.getAbilities().instabuild) {
                this.currentFlightTarget = target.blockPosition();
                if (this.distanceToSqr(target) < 9.0) {
                    this.doHurtTarget(target);
                }
            }
        }

        double deltaX = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double deltaY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double deltaZ = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(deltaX) * 0.5 - motion.x) * 0.3;
        double my = motion.y + (Math.signum(deltaY) * 0.7 - motion.y) * 0.2;
        double mz = motion.z + (Math.signum(deltaZ) * 0.5 - motion.z) * 0.3;
        this.setDeltaMovement(mx, my, mz);

        float targetYawDegrees = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYawDegrees - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 4.0f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GENERIC_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return true;
    }
}
