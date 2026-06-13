package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModSounds;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;

public class TRex extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TRex.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.38f;

    public TRex(EntityType<? extends TRex> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    // AI mirrors 1.7.10 TRex#func_70619_bc: random-cadence swings with the
    // same outer/inner nextInt dice. Revenge target is now handled by the
    // standard HurtByTargetGoal; proactive target acquisition is pushed onto
    // NearestAttackableTargetGoal with a wide follow range (40) to match the
    // legacy 20×6×20 AABB scan.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DinosaurMeleeAttackGoal(this, this::setAttacking,
                DinosaurMeleeAttackGoal.Presets.trex()));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6479 — TRex 160 HP / 22 ATK / 14 armor;
        // speed 0.38 matches orig TRex.java:41.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.TREX.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, MobStats.TREX.attackDamage())
                .add(Attributes.ARMOR, MobStats.TREX.armor())
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
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
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // orig TRex.java:96-101 — "orespawn:trex_living" 1-in-4, else silent.
        if (this.getRandom().nextInt(4) == 0) {
            return ModSounds.TREX_LIVING.get();
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig TRex.java:103-105 — "orespawn:alo_hurt".
        return ModSounds.ALO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        // orig TRex.java:107-109 — "orespawn:trex_death".
        return ModSounds.TREX_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // Death drops are fully data-driven via loot_table/entities/trex.json
    // (orig TRex.java:128-140: trex tooth, painting, 7 raw beef,
    // 2-5x paired uranium+titanium nuggets).

    // 1.7.10 knockback: horizontal push of 1.2 + vertical bump of 0.1
    // (doubled if hitting a player or removed entity).
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 1.2;
                double upwardKnockback = 0.1;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    upwardKnockback *= 2.0;
                }
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Cactus immunity preserved from 1.7.10 (T-Rex leather is too thick).
        if (source.getMsgId().equals("cactus")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    /** orig TRex.java:276-315 — "T. Rex" spawner bypass; darkness; y>=50; night; clear-air column; no other TRex within 24/12/24. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 5, -1, 1)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, TRex.class, 24.0, 12.0, 24.0);
    }
}
