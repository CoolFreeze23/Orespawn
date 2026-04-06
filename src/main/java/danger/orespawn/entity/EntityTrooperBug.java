package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityTrooperBug extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityTrooperBug.class, EntityDataSerializers.INT);

    private static final double KNOCKBACK_STRENGTH = 1.8;
    private static final double KNOCKBACK_VERTICAL = 0.2;
    private static final double KNOCKBACK_VERTICAL_PLAYER_MULT = 2.0;
    private static final double JUMP_HORIZONTAL_MIN = 0.2f;
    private static final double JUMP_HORIZONTAL_RANDOM = 0.45f;
    private static final double JUMP_VERTICAL_BOOST = 1.15;
    private static final double JUMP_POS_RAISE = 1.5;

    private int hurtTimer = 0;

    public EntityTrooperBug(EntityType<? extends EntityTrooperBug> type, Level level) {
        super(type, level);
        this.xpReward = 150;
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
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 16.0);
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
        return 1.5f;
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
        this.setPos(this.getX(), this.getY() + JUMP_POS_RAISE, this.getZ());
        this.hasImpulse = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasImpulse) {
            this.getNavigation().stop();
        }
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
        this.hurtTimer = 20;
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
                if (this.random.nextInt(10) == 1 && !this.hasImpulse) {
                    this.jumpFromGround();
                } else {
                    double dist = this.distanceToSqr(target);
                    double range = (5.0 + target.getBbWidth() / 2.0) * (5.0 + target.getBbWidth() / 2.0);
                    if (dist < range) {
                        this.setAttacking(1);
                        if (this.random.nextInt(6) == 0 || this.random.nextInt(7) == 1) {
                            this.doHurtTarget(target);
                            if (!this.level().isClientSide) {
                                if (this.random.nextInt(3) == 1) {
                                    this.level().playSound(null, target.blockPosition(),
                                            SoundEvent.createVariableRangeEvent(
                                                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_attack")),
                                            this.getSoundSource(), 1.4f, 1.0f);
                                }
                            }
                        }
                    } else if (!this.hasImpulse) {
                        this.getNavigation().moveTo(target, 1.2);
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(Items.NAME_TAG);
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
        if (target instanceof EntityTrooperBug) return false;
        if (target instanceof EntitySpitBug) return false;
        if (target instanceof Player p && p.getAbilities().invulnerable) return false;
        return true;
    }
}
