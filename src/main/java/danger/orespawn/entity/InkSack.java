package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class InkSack extends ThrowableProjectile {
    private static final float DAMAGE_DEFAULT = 1.0f;
    private static final float DAMAGE_VS_CREEPER_BONUS = 4.0f;
    private static final int BLINDNESS_BASE_DURATION = 100;
    private static final int BLINDNESS_DURATION_STEP = 50;
    private static final int BLINDNESS_DURATION_VARIANCE = 8;
    private static final int EFFECT_APPLY_CHANCE = 2;
    private static final int CLIENT_SMOKE_PARTICLE_COUNT = 4;
    private static final float SPLASH_VOLUME = 0.5f;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private float visualRotationDegrees = 0.0f;

    public InkSack(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public InkSack(Level level, LivingEntity shooter) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = DAMAGE_DEFAULT;
        if (target instanceof Creeper) damage = DAMAGE_VS_CREEPER_BONUS;

        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        if (target instanceof LivingEntity living && this.random.nextInt(EFFECT_APPLY_CHANCE) == 0) {
            int duration = BLINDNESS_BASE_DURATION + BLINDNESS_DURATION_STEP * this.random.nextInt(BLINDNESS_DURATION_VARIANCE);
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int particleIndex = 0; particleIndex < CLIENT_SMOKE_PARTICLE_COUNT; ++particleIndex) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getZ() + this.random.nextFloat(), 0, 0, 0);
            }
        }
        this.playSound(SoundEvents.GENERIC_SPLASH, SPLASH_VOLUME,
                1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        if (this.visualRotationDegrees > FULL_ROTATION_DEGREES) this.visualRotationDegrees -= FULL_ROTATION_DEGREES;
        this.setXRot(this.visualRotationDegrees);
    }
}
