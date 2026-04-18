package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

/**
 * Robot1 — RoboSpinner role.
 *
 * Lightweight, low-HP kamikaze drone. Scans for targets every few ticks,
 * sprints toward the closest valid target, and self-detonates on contact
 * (level.explode @ 2.5 power). The "spinner" identity comes from the
 * accelerating yaw spin once it locks a target — the body model rotates
 * faster the closer it gets, telegraphing the imminent explosion. Smoke
 * and lava particles trail it during the charge so players see the
 * detonation coming. Registry ID kept as "robot_1" for save compat.
 */
public class Robot1 extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot1.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.2f;
    private float spinYawAccumulator = 0.0f;

    public Robot1(EntityType<? extends Robot1> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getRandom().nextInt(8) == 0) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.setAttacking(1);
                double distSq = this.distanceToSqr(target);
                if (distSq < 5.0 && !this.level().isClientSide()
                        && this.getRandom().nextInt(18) == 1) {
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.5f,
                            Level.ExplosionInteraction.MOB);
                    this.discard();
                }
                for (int i = 0; i < 2; i++) {
                    if (this.level().isClientSide()) {
                        this.level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 0, 0, 0);
                        this.level().addParticle(ParticleTypes.LAVA, getX(), getY() + 1.0, getZ(), 0, 0, 0);
                    }
                }
                // Spinner identity: yaw-spin accelerates as the kamikaze
                // closes on its target. Spin rate ramps from 12°/tick at
                // 30+ blocks to 36°/tick under 5 blocks (hard-capped so we
                // never NaN the yaw value when the target is co-located).
                float spinRate = 12.0f + (float) Math.max(0.0, 30.0 - Math.sqrt(Math.max(distSq, 0.0001))) * 0.8f;
                spinYawAccumulator = (spinYawAccumulator + spinRate) % 360.0f;
                this.setYRot(spinYawAccumulator);
                this.yBodyRot = spinYawAccumulator;
                this.getNavigation().moveTo(target, 1.2);
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(8.0, 3.0, 8.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot1_death"));
    }
}
