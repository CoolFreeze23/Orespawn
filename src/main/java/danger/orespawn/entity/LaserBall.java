package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LaserBall extends ThrowableProjectile {
    private float myRotation = 0.0f;
    private boolean isSpecial = false;
    private boolean isIceball = false;
    private boolean isAcid = false;
    private boolean isIrukandji = false;
    private int ticksAlive = 0;

    public LaserBall(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public LaserBall(Level level, LivingEntity shooter) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
    }

    public LaserBall(Level level, double x, double y, double z) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setPos(x, y, z);
    }

    public void setSpecial() { this.isSpecial = true; }
    public void setIceBall() { this.isIceball = true; }
    public void setAcid() { this.isAcid = true; }
    public void setIrukandji() { this.isIrukandji = true; this.isAcid = true; }
    public void setIceMaker(int i) { this.isIceball = true; }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();

        if (this.isIrukandji) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 100.0f);
            this.discard();
            return;
        }

        float damage = 16.0f;
        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
        if (!this.isIceball) {
            target.igniteForSeconds(1);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && !this.isAcid) {
            if (this.isSpecial || this.isIceball) {
                boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 3.0f,
                        canGrief, Level.ExplosionInteraction.MOB);
            }
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.5f,
                    1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        }
        this.discard();
    }

    @Override
    public void tick() {
        ++this.ticksAlive;
        if (this.ticksAlive > 200) {
            this.discard();
            return;
        }
        super.tick();
        this.myRotation += 50.0f;
        if (this.myRotation > 360.0f) this.myRotation -= 360.0f;
        this.setXRot(this.myRotation);

        if (this.level().isClientSide && !this.isAcid) {
            int mx = this.isSpecial ? 10 : (this.isIceball ? 2 : 4);
            for (int i = 0; i < mx; ++i) {
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY(), this.getZ(),
                        this.random.nextGaussian() / 2.0,
                        this.random.nextGaussian() / 2.0,
                        this.random.nextGaussian() / 2.0);
            }
        }
    }
}
