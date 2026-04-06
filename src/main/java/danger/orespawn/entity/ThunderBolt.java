package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThunderBolt extends ThrowableProjectile {

    public ThunderBolt(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public ThunderBolt(Level level, LivingEntity shooter) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
    }

    public ThunderBolt(Level level, double x, double y, double z) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        float damage = 40.0f;
        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage / 2.0f);
        target.hurt(this.damageSources().mobAttack((LivingEntity) this.getOwner()), damage / 2.0f);
        target.igniteForSeconds(1);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 3.0f,
                    canGrief, Level.ExplosionInteraction.MOB);
            if (this.level() instanceof ServerLevel serverLevel) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(this.getX(), this.getY() + 1.0, this.getZ());
                    serverLevel.addFreshEntity(bolt);
                }
            }
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.5f,
                1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY(), this.getZ(),
                        this.random.nextGaussian() / 10.0,
                        this.random.nextGaussian() / 10.0,
                        this.random.nextGaussian() / 10.0);
            }
        }
    }
}
