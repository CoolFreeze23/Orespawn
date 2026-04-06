package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SunspotUrchin extends ThrowableProjectile {
    private static final float DAMAGE_DEFAULT = 3.0f;
    private static final float DAMAGE_VS_CREEPER = 6.0f;
    private static final int IGNITE_DURATION_SECONDS = 5;
    private static final int SELF_IGNITE_DURATION_SECONDS = 1;
    private static final int CLIENT_SMOKE_BURST_COUNT = 5;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private float visualRotationDegrees = 0.0f;

    public SunspotUrchin(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SunspotUrchin(Level level, LivingEntity shooter) {
        super(ModEntities.SUNSPOT_URCHIN.get(), level);
        this.setOwner(shooter);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target instanceof Player) return;
        float damage = DAMAGE_DEFAULT;
        if (target instanceof Creeper) damage = DAMAGE_VS_CREEPER;
        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        if (!target.fireImmune()) target.igniteForSeconds(IGNITE_DURATION_SECONDS);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int particleIndex = 0; particleIndex < CLIENT_SMOKE_BURST_COUNT; ++particleIndex) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(),
                        this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat());
            }
        }
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.igniteForSeconds(SELF_IGNITE_DURATION_SECONDS);
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        if (this.visualRotationDegrees > FULL_ROTATION_DEGREES) this.visualRotationDegrees -= FULL_ROTATION_DEGREES;
        this.setXRot(this.visualRotationDegrees);
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }
}
