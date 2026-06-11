package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.entity.ai.BasiliskGazeAttackGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Basilisk extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);

    private static final double KNOCKBACK_HORIZONTAL = 1.5;
    private static final double KNOCKBACK_VERTICAL = 0.15;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;

    private int hurtTimer = 0;
    private final float moveSpeed = 0.4f;

    public Basilisk(EntityType<? extends Basilisk> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    // AI: the Basilisk is a melee-only boss in 1.7.10 (no projectile). Its
    // "ranged" flavour is a debilitating aura — Slowness V on any target
    // within its 6-block reach — plus Poison on a successful bite. Both are
    // encapsulated in BasiliskGazeAttackGoal. HurtByTargetGoal handles
    // retaliation; NearestAttackableTargetGoal replaces the legacy 24×7×24
    // private AABB scan with modern target sensing.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BasiliskGazeAttackGoal(this, this::setAttacking));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6487 — Basilisk 200 HP / 24 ATK / 15 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.BASILISK.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, MobStats.BASILISK.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ARMOR, MobStats.BASILISK.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    public int mygetMaxHealth() {
        return 500;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(2) == 0) {
            return SoundEvents.RAVAGER_ROAR;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // Death drops are fully data-driven via loot_table/entities/basilisk.json
    // (orig Basilisk.java:151-310: basilisk scale, painting, 12-17 emerald,
    // 8-12 raw chicken, 3-7 rolls of the d15 Emerald gear table).

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                if (this.getRandom().nextInt(3) == 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                }
                double verticalKnockback = KNOCKBACK_VERTICAL;
                float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
                }
                target.push(
                        Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                        verticalKnockback,
                        Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) {
            return false;
        }
        this.hurtTimer = 30;
        return super.hurt(source, amount);
    }

    // Slow regen: 1 HP every ~75 cadence ticks while damaged, plus 1 HP on
    // a 1-in-200 aiStep roll. Kept on the customServerAiStep path so we tick
    // alongside the legacy hurtTimer i-frame counter without fighting the
    // goal selector's navigation control.
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) {
            --this.hurtTimer;
        }

        if (this.getRandom().nextInt(75) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isRemoved()) return;
        if (this.getRandom().nextInt(200) == 0) {
            this.heal(1.0f);
        }
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }
}
