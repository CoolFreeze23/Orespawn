package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WaterBall extends ThrowableProjectile {
    private static final float DAMAGE_DEFAULT = 2.0f;
    private static final float DAMAGE_VS_CREEPER = 5.0f;
    private static final int CLIENT_IMPACT_PARTICLE_LOOPS = 8;
    private static final float SPLASH_VOLUME = 0.5f;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private float visualRotationDegrees = 0.0f;

    public WaterBall(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public WaterBall(Level level, LivingEntity shooter) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
    }

    public WaterBall(Level level, double x, double y, double z) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = DAMAGE_DEFAULT;
        if (target instanceof Creeper) damage = DAMAGE_VS_CREEPER;
        if (target instanceof Player player && player.getVehicle() != null) return;

        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        target.clearFire();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int particleIndex = 0; particleIndex < CLIENT_IMPACT_PARTICLE_LOOPS; ++particleIndex) {
                this.level().addParticle(ParticleTypes.BUBBLE,
                        this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getZ() + this.random.nextFloat(), 0, 0, 0);
                this.level().addParticle(ParticleTypes.SPLASH,
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
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }
}
