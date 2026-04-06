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
    private float myRotation = 0.0f;

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
        float damage = 1.0f;
        if (target instanceof Creeper) damage = 4.0f;

        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        if (target instanceof LivingEntity living && this.random.nextInt(2) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
                    100 + 50 * this.random.nextInt(8), 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getZ() + this.random.nextFloat(), 0, 0, 0);
            }
        }
        this.playSound(SoundEvents.GENERIC_SPLASH, 0.5f,
                1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.myRotation += 30.0f;
        if (this.myRotation > 360.0f) this.myRotation -= 360.0f;
        this.setXRot(this.myRotation);
    }
}
