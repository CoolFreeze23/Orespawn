package danger.orespawn.entity;

import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BerthaHit extends ThrowableProjectile {
    private static final int HIT_TYPE_DEFAULT = 0;
    private static final int HIT_TYPE_MEDIUM = 2;
    private static final int HIT_TYPE_EXPLOSIVE = 3;
    // orig BerthaHit.java:107 — smaller blast when the shockwave connects with an entity
    private static final float EXPLOSION_POWER_ENTITY_HIT = 1.5f;
    // orig BerthaHit.java:111 — bigger blast when it hits anything else (block impact)
    private static final float EXPLOSION_POWER_MISS = 2.1f;
    // orig BerthaHit.java:106,110 — both blasts only fire within distSq 64 of the shooter
    private static final double EXPLOSION_RANGE_SQ = 64.0;
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

        // orig BerthaHit.java:68-75 — Girlfriend/Boyfriend always spared (the
        // orig &&/|| precedence makes their check unconditional); players and
        // tamed pets only spared while big_bertha_pvp == 0.
        if (entity instanceof Girlfriend || entity instanceof Boyfriend) {
            this.discard();
            return;
        }
        if (!OreSpawnConfig.BIG_BERTHA_PVP.get()
                && (entity instanceof Player || entity instanceof TamableAnimal tame && tame.isTame())) {
            this.discard();
            return;
        }
        if (entity == owner) { this.discard(); return; }

        float damage;
        double knockback;
        double verticalKnock;
        double maxRangeSq;
        switch (this.hitType) {
            // orig BerthaHit.java:87-95 — Royal: 746 dmg (royal_stats), distSq < 101.
            case HIT_TYPE_MEDIUM -> { damage = 746.0f; knockback = 1.5; verticalKnock = 0.25; maxRangeSq = 101.0; }
            // orig BerthaHit.java:97-105 — Hammy: 82 dmg (hammy_stats), distSq < 64.
            case HIT_TYPE_EXPLOSIVE -> { damage = 82.0f; knockback = 1.25; verticalKnock = 0.65; maxRangeSq = 64.0; }
            // orig BerthaHit.java:76-85 — Bertha: 496 dmg (bertha_stats), distSq < 81.
            default -> { damage = 496.0f; knockback = 2.25; verticalKnock = 0.35; maxRangeSq = 81.0; }
        }

        if (this.distanceToSqr(owner) < maxRangeSq) {
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
            // orig BerthaHit.java:106-108 — a Hammy shockwave that connects with an
            // entity detonates at the smaller 1.5 radius, inside the same
            // hit_type==3 / distSq<64 guard as the damage (EXPLOSION_RANGE_SQ ==
            // the type-3 maxRangeSq). isFlaming is unconditionally true; block
            // destruction follows mobGriefing (orig isSmoking flag →
            // ExplosionInteraction.MOB, which reads the same gamerule).
            if (this.hitType == HIT_TYPE_EXPLOSIVE) {
                this.level().explode(null, this.getX(), this.getY(), this.getZ(),
                        EXPLOSION_POWER_ENTITY_HIT, true, Level.ExplosionInteraction.MOB);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        // orig BerthaHit.java:110-112 — the 2.1-radius blast is the else-branch of
        // the entity-hit path: it fires only when NO entity was struck (block
        // impact), and only within distSq 64 of the shooter. Entity hits use the
        // 1.5-radius branch in onHitEntity; the orig's pvp early-returns
        // (func_70106_y + return) likewise never reached this blast. A null owner
        // is skipped where the orig would NPE on func_70068_e(null) — a server
        // crash is not reproducible behavior.
        if (!this.level().isClientSide && this.hitType == HIT_TYPE_EXPLOSIVE
                && result.getType() != HitResult.Type.ENTITY) {
            Entity owner = this.getOwner();
            if (owner != null && this.distanceToSqr(owner) < EXPLOSION_RANGE_SQ) {
                this.level().explode(null, this.getX(), this.getY(), this.getZ(),
                        EXPLOSION_POWER_MISS, true, Level.ExplosionInteraction.MOB);
            }
        }
        this.discard();
    }
}
