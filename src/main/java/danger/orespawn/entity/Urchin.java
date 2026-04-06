package danger.orespawn.entity;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Urchin extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Urchin.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 30;
    private static final double MOVE_SPEED = 0.3;
    private static final double ATTACK_DAMAGE = 8.0;

    public Urchin(EntityType<? extends Urchin> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
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
        target.igniteForSeconds(5);
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.random.nextInt(3) == 1 && this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.75, this.getZ(),
                    0.0, this.random.nextFloat() / 10.0, 0.0);
            if (this.isInWater() && this.random.nextInt(5) == 1) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 1.75, this.getZ(),
                        0.0, this.random.nextFloat() / 10.0, 0.0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY() + 2.0, this.getZ(),
                        0.0, this.random.nextFloat() / 10.0, 0.0);
            }
        }
        if (this.isInWater() && this.random.nextInt(5) == 1 && !this.level().isClientSide) {
            this.doHurtTarget(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        long t = this.level().getDayTime() % 24000L;
        if (t < 12000L && this.random.nextInt(400) == 1 && !this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(8) == 0) {
            LivingEntity target = this.getTarget();
            if (target == null) {
                Player nearest = this.level().getNearestPlayer(this, 16.0);
                if (nearest != null && !nearest.getAbilities().instabuild) {
                    target = nearest;
                    this.setTarget(target);
                }
            }
            if (target != null && target.isAlive()) {
                if (this.distanceToSqr(target) < 8.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(7) == 0) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.2);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        long t = this.level().getDayTime() % 24000L;
        return t >= 13000L;
    }
}
