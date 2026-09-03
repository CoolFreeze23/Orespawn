package danger.orespawn.entity;

import danger.orespawn.util.MyUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BetterFireball extends LargeFireball {
    private static final int MAX_LIFETIME_TICKS = 600;
    private static final float LARGE_MOB_BB_AREA_THRESHOLD = 30.0f;
    private static final float DAMAGE_SMALL = 5.0f;
    private static final float DAMAGE_LARGE = 10.0f;
    private static final int FIRE_SECONDS_ON_HIT = 5;
    private static final double SMOKE_OFFSET_Y = 0.5;
    /**
     * orig BetterFireball.java:84 setSmall() {@code func_70105_a(0.3125f, 0.3125f)}
     * — the vanilla small-fireball box (EntityType.SMALL_FIREBALL .sized(0.3125F, 0.3125F)).
     */
    private static final float SMALL_SIZE = 0.3125f;

    private int ticksAlive = 0;
    private int explosionPower = 1;
    private boolean small = false;
    /** orig BetterFireball.java:70-72,152,214 — shooter-set flag sparing players/Dragons. */
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
    /**
     * orig BetterFireball.java:82-85 — flags the 5-damage / no-explosion variant
     * AND shrinks the box from the constructors' 1.0x1.0 (:48/:57) to
     * 0.3125x0.3125 (:84). The port's setSmall only flipped the flag; owner
     * ruling 2026-09-03 (ENT-S-095) restores the shrink the modern way: the
     * projectile chain (LargeFireball/Fireball/AbstractHurtingProjectile/
     * Projectile) has no dims hook of its own and {@code getDefaultDimensions}
     * is a LivingEntity method, so the non-living override point is
     * {@code Entity#getDimensions(Pose)} (the vanilla AreaEffectCloud pattern),
     * keyed on the flag and applied through {@code refreshDimensions()}, which
     * rebuilds the live AABB via reapplyPosition() whether or not the entity
     * is in a level yet (shooters call setSmall before addFreshEntity). As in
     * 1.7.10 the flag is neither synced nor saved: the client keeps the
     * type's 1x1 box exactly as its World-ctor copy did (orig :46-49).
     */
    public void setSmall() {
        this.small = true;
        this.refreshDimensions();
    }

    /** orig BetterFireball.java:84 — 0.3125x0.3125 once setSmall() ran, else the registered 1.0x1.0 (orig :48/:57). */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.small ? EntityDimensions.scalable(SMALL_SIZE, SMALL_SIZE) : super.getDimensions(pose);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticksAlive++;
        if (this.ticksAlive >= MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + SMOKE_OFFSET_Y, this.getZ(), 0, 0, 0);
        }
    }

    /**
     * orig BetterFireball.java:136-155 (tick sweep skips) and :208-217 (impact
     * returns) — the fireball passes through other BetterFireballs, Mothra,
     * GodzillaHeads and Royalty, and, when the shooter set notme, through
     * players and Dragons. The original aborted the whole collision scan when
     * such an entity appeared anywhere in the sweep list; the port maps this
     * to per-entity pass-through, the closest vanilla-hit-detection analogue.
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof BetterFireball || target instanceof Mothra
                || target instanceof GodzillaHead || MyUtils.isRoyalty(target)) {
            return false;
        }
        if (this.notme && (target instanceof Player || target instanceof Dragon)) {
            return false;
        }
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        if (target == this.getOwner()) return;

        if (target instanceof LivingEntity living) {
            float boundingBoxArea = living.getBbHeight() * living.getBbWidth();
            // orig BetterFireball.java:221-223 — big mobs lose half their HP,
            // except Royalty/Godzilla/GodzillaHead/PitchBlack/Kraken
            if (boundingBoxArea > LARGE_MOB_BB_AREA_THRESHOLD
                    && !MyUtils.isRoyalty(living)
                    && !(living instanceof Godzilla)
                    && !(living instanceof GodzillaHead)
                    && !(living instanceof PitchBlack)
                    && !(living instanceof Kraken)) {
                living.setHealth(living.getHealth() / 2.0f);
            }
        }

        float damage = this.small ? DAMAGE_SMALL : DAMAGE_LARGE;
        target.hurt(this.damageSources().fireball(this, this.getOwner()), damage);
        target.igniteForSeconds(FIRE_SECONDS_ON_HIT);
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
