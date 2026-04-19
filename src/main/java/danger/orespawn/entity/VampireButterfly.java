package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.AmbientFlightGoal;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Vampire Butterfly — hostile butterfly variant that haunts the Chaos
 * (a.k.a. "Danger") Dimension.
 *
 * <p>This is a wiki-canon mob that the 1.7.10 OreSpawn source never
 * shipped (see Phase 14 audit: zero references to {@code VampireButterfly}
 * in {@code reference_1_7_10_source/}). The Wiki's "Added Mobs" page
 * lists it as a separate entity with hostile attack AI and a
 * blood-drain debuff, so we register it as its own entity ID rather than
 * reskinning {@link EntityButterfly} (which would pollute the ambient
 * flier's spawn pool and break its right-click → Chaos teleport contract).
 *
 * <p><b>Combat profile.</b> Soft target (8 HP, like a vanilla Bat) but it
 * applies {@link MobEffects#HUNGER} + {@link MobEffects#WEAKNESS} on
 * every melee hit — the in-fiction "blood drain" — so a swarm in a Chaos
 * boss arena can drain a player faster than any single hit could. Touch
 * damage stays low (2.0) to keep them in the harassment role rather than
 * one-shotting unarmored newcomers.
 *
 * <p><b>Movement.</b> Reuses {@link AmbientFlightGoal} (butterfly preset)
 * for wandering so the silhouette matches the ambient butterfly, with
 * {@link NearestAttackableTargetGoal} layered on top so it abandons the
 * wander and homes in on the nearest player once one enters its 16-block
 * sensing range.
 */
public class VampireButterfly extends Monster {
    private static final EntityDataAccessor<Integer> ATTACKING_TICKS =
            SynchedEntityData.defineId(VampireButterfly.class, EntityDataSerializers.INT);

    private static final float MELEE_DAMAGE = 2.0f;
    private static final int BLOOD_DRAIN_HUNGER_DURATION = 200;
    private static final int BLOOD_DRAIN_WEAKNESS_DURATION = 120;
    private static final double KNOCKBACK_HORIZONTAL = 0.3;

    public VampireButterfly(EntityType<? extends VampireButterfly> type, Level level) {
        super(type, level);
        this.xpReward = 4;
    }

    @Override
    protected void registerGoals() {
        // Wandering identical to the ambient butterfly so the silhouette
        // reads the same; the attack target overrides this goal slot
        // automatically because it has a lower priority number.
        this.goalSelector.addGoal(8, new AmbientFlightGoal(this, AmbientFlightGoal.Params.butterfly()));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ATTACK_DAMAGE, MELEE_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        // Match the ambient butterfly's vertical drag so flight visually
        // matches and we don't fall like a stone when the AI yields.
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        if (this.entityData.get(ATTACKING_TICKS) > 0) {
            this.entityData.set(ATTACKING_TICKS, this.entityData.get(ATTACKING_TICKS) - 1);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            // Steer toward target each tick — the Goal-based pathfinder
            // would also work, but butterflies don't ground-pathfind so
            // we drive the delta-movement directly like CloudShark does.
            Vec3 toTarget = new Vec3(
                    target.getX() - this.getX(),
                    target.getY() + target.getEyeHeight() - this.getY(),
                    target.getZ() - this.getZ());
            double dist = toTarget.length();
            if (dist > 0.001) {
                Vec3 step = toTarget.scale(0.04 / dist);
                Vec3 newMot = this.getDeltaMovement().add(step);
                this.setDeltaMovement(newMot.x * 0.92, newMot.y * 0.92, newMot.z * 0.92);
                this.lookAt(target, 30.0f, 30.0f);
            }
            double touchRange = 1.4 + target.getBbWidth() / 2.0;
            if (this.distanceToSqr(target) < touchRange * touchRange) {
                this.doHurtTarget(target);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living)) return false;
        boolean hurtApplied = target.hurt(this.damageSources().mobAttack(this), MELEE_DAMAGE);
        if (hurtApplied) {
            // The "blood drain": Hunger drains the player's food meter
            // (mirrors the Wiki's lore that it leeches life-sustenance)
            // and Weakness reduces their counter-attack window so swarms
            // can sustain damage on heavily-armored late-game players.
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, BLOOD_DRAIN_HUNGER_DURATION, 0));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BLOOD_DRAIN_WEAKNESS_DURATION, 0));
            float yaw = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            target.push(Math.cos(yaw) * KNOCKBACK_HORIZONTAL, 0.1, Math.sin(yaw) * KNOCKBACK_HORIZONTAL);
            this.entityData.set(ATTACKING_TICKS, 10);
        }
        return hurtApplied;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource src) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mosquito"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    public static boolean checkSpawnRules(EntityType<VampireButterfly> type, LevelAccessor level,
                                          MobSpawnType reason, BlockPos pos,
                                          net.minecraft.util.RandomSource random) {
        // No light gating — the Chaos dimension is permanently dim and
        // the Wiki describes Vampire Butterflies as active at all times.
        // Standard "valid mob spawn block" + air-above check.
        return Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("VbAttackingTicks", this.entityData.get(ATTACKING_TICKS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(ATTACKING_TICKS, tag.getInt("VbAttackingTicks"));
    }
}
