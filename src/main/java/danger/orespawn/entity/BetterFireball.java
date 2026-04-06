package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BetterFireball extends LargeFireball {
    private int ticksAlive = 0;
    private int explosionPower = 1;
    private boolean small = false;
    private boolean notme = false;

    public BetterFireball(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
    }

    public BetterFireball(Level level, LivingEntity shooter, Vec3 movement) {
        super(level, shooter, movement, 1);
    }

    public void setNotMe() { this.notme = true; }
    public void setBig() { this.explosionPower = 2; }
    public void setReallyBig() { this.explosionPower = 4; }
    public void setSmall() {
        this.small = true;
    }

    @Override
    public void tick() {
        super.tick();
        this.ticksAlive++;
        if (this.ticksAlive >= 600) {
            this.discard();
            return;
        }
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        if (target == this.getOwner()) return;

        if (target instanceof LivingEntity living) {
            float s = living.getBbHeight() * living.getBbWidth();
            if (s > 30.0f) {
                living.setHealth(living.getHealth() / 2.0f);
            }
        }

        float damage = this.small ? 5.0f : 10.0f;
        target.hurt(this.damageSources().fireball(this, this.getOwner()), damage);
        target.igniteForSeconds(5);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (!this.small) {
                boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                this.level().explode(null, this.getX(), this.getY(), this.getZ(),
                        (float) this.explosionPower, canGrief, Level.ExplosionInteraction.MOB);
            }
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ExplosionPower", this.explosionPower);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ExplosionPower")) {
            this.explosionPower = tag.getInt("ExplosionPower");
        }
    }
}
