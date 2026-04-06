package danger.orespawn.entity;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Hammerhead extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Hammerhead.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 200;
    private static final double MOVE_SPEED = 0.35;
    private static final double ATTACK_DAMAGE = 20.0;

    private LivingEntity revengeTarget = null;

    public Hammerhead(EntityType<? extends Hammerhead> type, Level level) {
        super(type, level);
        this.xpReward = 350;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
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
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                double ks = 1.1;
                double inair = 0.85;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target instanceof Player) inair *= 2.0;
                target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        Entity e = source.getEntity();
        if (e instanceof LivingEntity living) {
            this.revengeTarget = living;
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(3) == 1) {
            LivingEntity e = this.revengeTarget;
            if (e != null) {
                if (!e.isAlive() || this.random.nextInt(250) == 1) {
                    e = null;
                    this.revengeTarget = null;
                }
            }
            if (e == null) {
                e = this.getTarget();
            }
            if (e == null) {
                Player nearest = this.level().getNearestPlayer(this, 18.0);
                if (nearest != null && !nearest.getAbilities().instabuild) {
                    e = nearest;
                }
            }
            if (e != null && e.isAlive()) {
                this.lookAt(e, 10.0f, 10.0f);
                double dist = this.distanceToSqr(e);
                double range = (7.0 + e.getBbWidth() / 2.0) * (7.0 + e.getBbWidth() / 2.0);
                if (dist < range) {
                    this.setAttacking(1);
                    if (this.random.nextInt(3) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        for (int i = 0; i < 8; ++i) this.spawnAtLocation(Items.EXPERIENCE_BOTTLE);
        for (int i = 0; i < 6; ++i) this.spawnAtLocation(Items.BONE);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hammerhead_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.2f;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return this.level().getEntitiesOfClass(Hammerhead.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0)).isEmpty();
    }
}
