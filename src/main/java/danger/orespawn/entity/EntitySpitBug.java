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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntitySpitBug extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntitySpitBug.class, EntityDataSerializers.INT);

    private static final double KNOCKBACK_STRENGTH = 0.5;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double KNOCKBACK_VERTICAL_PLAYER_MULT = 2.0;
    private static final double JUMP_HORIZONTAL_MIN = 0.2f;
    private static final double JUMP_HORIZONTAL_RANDOM = 0.45f;
    private static final double JUMP_VERTICAL_BOOST = 0.75;

    private int hurtTimer = 0;

    public EntitySpitBug(EntityType<? extends EntitySpitBug> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "clatter"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crunch"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Override
    public void jumpFromGround() {
        Vec3 motion = this.getDeltaMovement();
        double yawRad = Math.toRadians(this.getYRot());
        float horizontalJumpStrength = (float) (JUMP_HORIZONTAL_MIN + Math.abs(this.random.nextFloat() * JUMP_HORIZONTAL_RANDOM));
        this.setDeltaMovement(
                motion.x - horizontalJumpStrength * Math.sin(yawRad),
                motion.y + JUMP_VERTICAL_BOOST,
                motion.z + horizontalJumpStrength * Math.cos(yawRad));
        this.setPos(this.getX(), this.getY() + 0.75, this.getZ());
        this.hasImpulse = true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double verticalKnockback = KNOCKBACK_VERTICAL;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    verticalKnockback *= KNOCKBACK_VERTICAL_PLAYER_MULT;
                }
                target.push(
                        Math.cos(angle) * KNOCKBACK_STRENGTH,
                        verticalKnockback,
                        Math.sin(angle) * KNOCKBACK_STRENGTH);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 15;
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.random.nextInt(5) == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.isAlive()) {
                this.setTarget(null);
                target = null;
            }
            if (target == null) target = findSomethingToAttack();

            if (target != null) {
                this.getLookControl().setLookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) < 9.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(6) == 0 || this.random.nextInt(7) == 1) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 0.5);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 7.0, 12.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Creeper) return false;
        if (target instanceof EntitySpitBug) return false;
        if (target instanceof EntityTrooperBug) return false;
        if (target instanceof Player p && p.getAbilities().invulnerable) return false;
        return true;
    }
}
