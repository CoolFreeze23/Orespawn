package danger.orespawn.entity;

import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
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
    private static final double CLOSE_RANGE_DAMAGE_SQ = 100.0;
    private static final int HIT_TYPE_DEFAULT = 0;
    private static final int HIT_TYPE_MEDIUM = 2;
    private static final int HIT_TYPE_EXPLOSIVE = 3;
    private static final float EXPLOSION_POWER = 2.1f;
    private static final int IGNITE_SECONDS_DEFAULT = 10;

    private int hitType = 0;

    public BerthaHit(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public BerthaHit(Level level, LivingEntity shooter) {
        super(ModEntities.BERTHA_HIT.get(), shooter, level);
    }

    public void setHitType(int hitType) { this.hitType = hitType; }

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
        double verticalKnock;
        switch (this.hitType) {
            case HIT_TYPE_MEDIUM -> { damage = 150.0f; knockback = 1.5; verticalKnock = 0.25; }
            case HIT_TYPE_EXPLOSIVE -> { damage = 100.0f; knockback = 1.25; verticalKnock = 0.65; }
            default -> { damage = 250.0f; knockback = 2.25; verticalKnock = 0.35; }
        }

        if (this.distanceToSqr(owner) < CLOSE_RANGE_DAMAGE_SQ) {
            // Owner may not be a Player, so pick the right damage source to avoid ClassCastException
            if (owner instanceof Player player) {
                entity.hurt(this.damageSources().playerAttack(player), damage);
            } else if (owner instanceof LivingEntity livingOwner) {
                entity.hurt(this.damageSources().mobAttack(livingOwner), damage);
            } else {
                entity.hurt(this.damageSources().thrown(this, owner), damage);
            }
            if (this.hitType == HIT_TYPE_DEFAULT) entity.igniteForSeconds(IGNITE_SECONDS_DEFAULT);
            float angle = (float) Math.atan2(entity.getZ() - owner.getZ(), entity.getX() - owner.getX());
            if (entity.isRemoved()) verticalKnock *= 2.0;
            entity.push(Math.cos(angle) * knockback, verticalKnock, Math.sin(angle) * knockback);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && this.hitType == HIT_TYPE_EXPLOSIVE) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER,
                    canGrief, Level.ExplosionInteraction.MOB);
        }
        this.discard();
    }
}
