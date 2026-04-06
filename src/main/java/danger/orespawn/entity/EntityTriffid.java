package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityTriffid extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityTriffid.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OPEN_CLOSED =
            SynchedEntityData.defineId(EntityTriffid.class, EntityDataSerializers.INT);

    private int hurt_timer = 0;

    public EntityTriffid(EntityType<? extends EntityTriffid> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.13)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_OPEN_CLOSED, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getOpenClosed() {
        return this.entityData.get(DATA_OPEN_CLOSED);
    }

    public void setOpenClosed(int value) {
        this.entityData.set(DATA_OPEN_CLOSED, value);
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "triffid_dead"));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.hurt_timer > 0) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(0, motion.y, 0);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.random.nextInt(100) == 1) {
                this.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 1.0);
            }
            if (this.hurt_timer <= 0) {
                LivingEntity e = findSomethingToAttack();
                if (e != null) {
                    float yaw = (float) Math.toDegrees(
                            Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX())) - 90.0f;
                    while (yaw < 0.0f) yaw += 360.0f;
                    this.setYRot(yaw);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurt_timer > 0 || this.getOpenClosed() == 0) {
            this.hurt_timer = 300;
            this.setAttacking(0);
            return false;
        }
        boolean ret = super.hurt(source, amount);
        this.hurt_timer = 300;
        this.setOpenClosed(0);
        this.setAttacking(0);
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurt_timer > 0) {
            --this.hurt_timer;
            this.clearFire();
            this.setOpenClosed(0);
        }

        if (this.random.nextInt(250) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }

        if (this.random.nextInt(80) == 2 && this.hurt_timer <= 0) {
            if (this.random.nextInt(8) == 1) {
                this.setOpenClosed(1);
            } else {
                this.setOpenClosed(0);
            }
        }

        if (this.random.nextInt(10) == 1 && this.hurt_timer <= 0) {
            LivingEntity e = findSomethingToAttack();
            if (e != null) {
                this.setOpenClosed(1);
                if (this.distanceToSqr(e) < 25.0) {
                    float yaw = (float) Math.toDegrees(
                            Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX())) - 90.0f;
                    while (yaw < 0.0f) yaw += 360.0f;
                    this.setYRot(yaw);
                    this.setAttacking(1);
                    this.doHurtTarget(e);
                } else {
                    this.setAttacking(0);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (this.random.nextInt(3) == 0) {
            this.spawnAtLocation(Items.POISONOUS_POTATO);
        }
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 8.0, 10.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof EntityTriffid) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Player p) return !p.getAbilities().invulnerable;
        return !(target instanceof Monster);
    }
}
