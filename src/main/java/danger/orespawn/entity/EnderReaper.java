package danger.orespawn.entity;

import danger.orespawn.MobStats;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EnderReaper extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_SCREAMING =
            SynchedEntityData.defineId(EnderReaper.class, EntityDataSerializers.BOOLEAN);


    public EnderReaper(EntityType<? extends EnderReaper> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6508 — EnderReaper 90 HP / 18 ATK / 8 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ENDER_REAPER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.37)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ENDER_REAPER.attackDamage())
                .add(Attributes.ARMOR, MobStats.ENDER_REAPER.armor())
                .add(Attributes.FOLLOW_RANGE, 81.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCREAMING, false);
    }

    public boolean isScreaming() {
        return this.entityData.get(DATA_SCREAMING);
    }

    public void setScreaming(boolean val) {
        this.entityData.set(DATA_SCREAMING, val);
    }

    @Override
    public void aiStep() {
        if (this.isInWaterRainOrBubble()) {
            this.hurt(this.damageSources().drown(), 1.0f);
        }

        if (!this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.PORTAL,
                        this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        this.getY() + this.random.nextDouble() * this.getBbHeight() - 0.25,
                        this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                        (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(),
                        (this.random.nextDouble() - 0.5) * 2.0);
            }
        }

        if (this.isOnFire()) {
            this.setScreaming(false);
            teleportRandomly();
        }
        super.aiStep();
    }

    protected boolean teleportRandomly() {
        double x = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        double y = this.getY() + (this.random.nextInt(64) - 32);
        double z = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.randomTeleport(x, y, z, true);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        this.setScreaming(true);
        if (source.is(DamageTypes.INDIRECT_MAGIC) || source.getDirectEntity() != source.getEntity()) {
            for (int i = 0; i < 16; ++i) {
                if (this.teleportRandomly()) return true;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isScreaming() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    /** orig EnderReaper.java:253-279 — "Ender Reaper" spawner bypass; darkness; night; y>=30; no other EnderReaper within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 30.0) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EnderReaper.class, 16.0, 8.0, 16.0);
    }
}
