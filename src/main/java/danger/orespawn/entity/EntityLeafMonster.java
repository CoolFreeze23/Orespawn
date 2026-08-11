package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.entity.ai.GenericTargetSorter;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;

public class EntityLeafMonster extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityLeafMonster.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.25f;

    public EntityLeafMonster(EntityType<? extends EntityLeafMonster> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6516 — LeafMonster 6 HP / 2 ATK / 1 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.LEAF_MONSTER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, MobStats.LEAF_MONSTER.attackDamage())
                .add(Attributes.ARMOR, MobStats.LEAF_MONSTER.armor());
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
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();

        if (this.getAttacking() == 0) {
            int blockX = (int) this.getX();
            int blockY = (int) this.getY();
            int blockZ = (int) this.getZ();
            double alignedX = blockX;
            double alignedZ = blockZ;
            if (this.getX() > 0.0) alignedX += 0.5;
            if (this.getZ() > 0.0) alignedZ += 0.5;
            if (this.getX() < 0.0) alignedX -= 0.5;
            if (this.getZ() < 0.0) alignedZ -= 0.5;
            this.setPos(alignedX, blockY, alignedZ);
            this.setXRot(0.0f);
            int snappedYaw = (int) this.yBodyRot / 90 * 90;
            this.setYRot(snappedYaw);
            this.yBodyRot = snappedYaw;
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        float damage = fallDistance - 3.0f;
        if (damage > 0.0f) {
            if (damage > 2.0f) damage = 2.0f;
            this.hurt(this.damageSources().fall(), damage);
        }
        return false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) return;

        if (this.random.nextInt(100) == 1) {
            this.setTarget(null);
        }

        if (this.random.nextInt(4) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                this.setAttacking(1);
                this.getNavigation().moveTo(target, 1.25);
                if (this.distanceToSqr(target) < 5.0
                        && (this.random.nextInt(8) == 0 || this.random.nextInt(10) == 1)) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig LeafMonster.java:210-212
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(4.0, 6.0, 4.0));
        // TF-035: orig sorts candidates with GenericTargetSorter (LeafMonster.java:36 field,
        // :48 ctor, :214 Collections.sort), not plain distance — creepers/large targets rank closer.
        entities.sort(new GenericTargetSorter(this));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    // orig LeafMonster.java:178-207 — explicit prey list: Ant, Butterfly, LunaMoth, non-creative players
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityAnt) return true;
        if (target instanceof EntityButterfly) return true;
        if (target instanceof EntityLunaMoth) return true;
        if (target instanceof Player player) {
            return !player.getAbilities().invulnerable;
        }
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
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leaves_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.65f;
    }

    /** orig LeafMonster.java:227-251 — "Leaf Monster" spawner bypass; darkness; night; Islands y<=20 / elsewhere y>=50; at most 4 buddies within 20/10/20. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) {
            if (this.getY() > 20.0) return false;
        } else if (this.getY() < 50.0) {
            return false;
        }
        return OriginalSpawnGates.countBuddies(this, level, EntityLeafMonster.class, 20.0, 10.0, 20.0) <= 4;
    }
}
