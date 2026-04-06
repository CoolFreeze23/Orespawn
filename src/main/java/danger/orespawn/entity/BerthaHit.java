package danger.orespawn.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BerthaHit extends ThrowableProjectile {
    private int hitType = 0;

    public BerthaHit(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public BerthaHit(Level level, LivingEntity shooter) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
    }

    public void setHitType(int i) { this.hitType = i; }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity entity = result.getEntity();
        Entity owner = this.getOwner();
        if (owner == null) return;

        if (entity instanceof Player || entity instanceof TamableAnimal tame && tame.isTame()) {
            this.discard();
            return;
        }
        if (entity == owner) { this.discard(); return; }

        float damage;
        double knockback;
        double airKnock;
        switch (this.hitType) {
            case 2 -> { damage = 150.0f; knockback = 1.5; airKnock = 0.25; }
            case 3 -> { damage = 100.0f; knockback = 1.25; airKnock = 0.65; }
            default -> { damage = 250.0f; knockback = 2.25; airKnock = 0.35; }
        }

        if (this.distanceToSqr(owner) < 100.0) {
            entity.hurt(this.damageSources().playerAttack((Player) owner), damage);
            if (this.hitType == 0) entity.igniteForSeconds(10);
            float angle = (float) Math.atan2(entity.getZ() - owner.getZ(), entity.getX() - owner.getX());
            if (entity.isRemoved()) airKnock *= 2.0;
            entity.push(Math.cos(angle) * knockback, airKnock, Math.sin(angle) * knockback);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && this.hitType == 3) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), 2.1f,
                    canGrief, Level.ExplosionInteraction.MOB);
        }
        this.discard();
    }
}
