package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SunspotUrchin extends ThrowableProjectile {
    private float myRotation = 0.0f;

    public SunspotUrchin(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SunspotUrchin(Level level, LivingEntity shooter) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target instanceof Player) return;
        float damage = 3.0f;
        if (target instanceof Creeper) damage = 6.0f;
        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        if (!target.fireImmune()) target.igniteForSeconds(5);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int i = 0; i < 5; ++i) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(),
                        this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat());
            }
        }
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.igniteForSeconds(1);
        this.myRotation += 30.0f;
        if (this.myRotation > 360.0f) this.myRotation -= 360.0f;
        this.setXRot(this.myRotation);
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }
}
