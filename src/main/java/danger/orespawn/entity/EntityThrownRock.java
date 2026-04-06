package danger.orespawn.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityThrownRock extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_ROCK_TYPE =
            SynchedEntityData.defineId(EntityThrownRock.class, EntityDataSerializers.INT);

    private int rockType = 0;
    private int myAge = 0;
    private float myRotation = 0.0f;

    public EntityThrownRock(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityThrownRock(Level level, LivingEntity shooter, int rockType) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
        this.rockType = rockType;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ROCK_TYPE, 0);
    }

    public int getRockType() { return this.entityData.get(DATA_ROCK_TYPE); }
    public void setRockType(int type) {
        if (this.level() == null || this.level().isClientSide) return;
        this.rockType = type;
        this.entityData.set(DATA_ROCK_TYPE, type);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide || this.isRemoved()) return;
        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        if (owner == null || target == owner) return;

        float damage;
        double knockback = 0.2;
        double airKnock = 0.025;

        switch (this.rockType) {
            case 1 -> { damage = 2.0f; knockback = 0.1; }
            case 2, 3, 4, 5 -> damage = 5.0f;
            case 6 -> damage = 20.0f;
            case 7, 8 -> { damage = 40.0f; knockback = 0.5; airKnock = 0.055; }
            case 9, 10, 11 -> damage = 150.0f;
            case 12 -> damage = 250.0f;
            default -> damage = 2.0f;
        }

        target.hurt(this.damageSources().playerAttack((Player) owner), damage);
        float angle = (float) Math.atan2(target.getZ() - owner.getZ(), target.getX() - owner.getX());
        if (target.isRemoved()) airKnock *= 2.0;
        target.push(Math.cos(angle) * knockback, airKnock, Math.sin(angle) * knockback);

        if (this.rockType == 3) target.igniteForSeconds(20);
        if (target instanceof LivingEntity living) {
            if (this.rockType == 4) living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            if (this.rockType == 5) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
            if (this.rockType == 6 || this.rockType == 9 || this.rockType == 11)
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            if (this.rockType == 10)
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
            if (this.rockType == 12)
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
        }

        if (this.rockType == 8 && !this.level().isClientSide) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(null, target.getX(), target.getY() + 0.25, target.getZ(),
                    2.1f, canGrief, Level.ExplosionInteraction.MOB);
        }
        if (this.rockType == 12 && !this.level().isClientSide) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(null, target.getX(), target.getY() + 0.25, target.getZ(),
                    5.1f, canGrief, Level.ExplosionInteraction.MOB);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        // TODO: Drop corresponding rock item on ground impact
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.myRotation += 30.0f;
        this.myRotation %= 360.0f;
        this.setXRot(this.myRotation);
        ++this.myAge;
        if (this.myAge > 1000) this.discard();

        if (this.level().isClientSide) {
            this.rockType = this.getRockType();
        } else {
            this.setRockType(this.rockType);
        }
    }
}
