package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnMod;

public class Crab extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SCALE =
            SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int hurtTimer = 0;
    private float moveSpeed = 0.55f;
    private static final int BASE_HEALTH = 100;
    private static final int BASE_ATTACK = 10;

    public Crab(EntityType<? extends Crab> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SCALE, 25);
    }

    public float getCrabScale() {
        return this.entityData.get(DATA_SCALE) / 100.0f;
    }

    public void setCrabScale(float val) {
        this.entityData.set(DATA_SCALE, (int) (val * 100.0f));
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.moveSpeed = this.isInWater() ? 0.95f : 0.55f;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed * this.getCrabScale());
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) BASE_ATTACK * this.getCrabScale();
        boolean hit = target.hurt(this.damageSources().mobAttack(this), damage);
        if (hit && target instanceof LivingEntity) {
            double ks = 1.15 * this.getCrabScale();
            double inair = 0.48 * this.getCrabScale();
            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            if (target instanceof Player) inair *= 2.0;
            target.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        Entity e = source.getEntity();
        if (e instanceof Crab) return false;
        if (this.hurtTimer <= 0) {
            this.hurtTimer = 8;
            boolean ret = super.hurt(source, amount);
            if (e instanceof LivingEntity living) {
                this.setTarget(living);
                this.getNavigation().moveTo(living, 1.2);
            }
            return ret;
        }
        return false;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.getRandom().nextInt(5) == 1) {
            LivingEntity e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) e = findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                float attackRange = (6.0f + e.getBbWidth() / 2.0f) * this.getCrabScale();
                if (this.distanceToSqr(e) < attackRange * attackRange) {
                    this.setAttacking(1);
                    if (this.getRandom().nextInt(4) == 0) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.getRandom().nextInt(120) == 1 && this.isInWater() && this.getHealth() < this.getMaxHealth()) {
            this.heal(4.0f * this.getCrabScale());
        }
    }

    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        list.sort(this.targetSorter);
        for (LivingEntity e : list) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Crab) return false;
        if (target instanceof Player p) return !p.getAbilities().instabuild;
        if (target instanceof Monster) return true;
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leaves_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Fscale", this.getCrabScale());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setCrabScale(tag.getFloat("Fscale"));
    }
}
