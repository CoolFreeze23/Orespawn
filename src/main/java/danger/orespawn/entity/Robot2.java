package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

/**
 * Robot2 — RoboWarrior role.
 *
 * Heavy front-line melee chassis (500 HP, 30 ATK, 8 ARM). Uses pack-alert
 * HurtByTargetGoal so all RoboWarriors in earshot of a hit aggro the
 * attacker simultaneously, mirroring the 1.7.10 robot-army "swarm a
 * threat" reaction. Closes to ~5-block reach and swings on a stochastic
 * cadence (nextInt(5)==0 || nextInt(6)==1) — the same tuning as the
 * 1.7.10 reference's func_70619_bc swing logic — and otherwise idles
 * with brief windup animations to telegraph attack readiness. Registry
 * ID kept as "robot_2" for save compat.
 */
public class Robot2 extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot2.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int idleAnimationTimer = 0;
    private final float moveSpeed = 0.3f;

    public Robot2(EntityType<? extends Robot2> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Robot2.class).setAlertOthers());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.ARMOR, 8.0);
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
    public void jumpFromGround() {
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, velocity.y + 0.25, velocity.z);
        super.jumpFromGround();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.getRandom().nextInt(6) == 1) {
            LivingEntity target = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) this.setTarget(null);
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                double dist = this.distanceToSqr(target);
                double meleeRange = (5.0f + target.getBbWidth() / 2.0f);
                if (dist < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    if (this.getRandom().nextInt(5) == 0 || this.getRandom().nextInt(6) == 1) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.setAttacking(0);
                }
                this.getNavigation().moveTo(target, 1.0);
            } else {
                this.setAttacking(0);
            }
        }
        if (this.getAttacking() == 0) {
            if (this.getRandom().nextInt(450) == 1) this.idleAnimationTimer = 50;
            if (this.idleAnimationTimer > 0) --this.idleAnimationTimer;
            if (this.idleAnimationTimer > 0) {
                this.setAttacking(1);
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(14.0, 3.0, 14.0);
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
        if (this.getRandom().nextInt(4) == 0)
            return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    }
}
