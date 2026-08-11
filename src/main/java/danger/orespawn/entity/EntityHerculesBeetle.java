package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityHerculesBeetle extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_HERCULES_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hercules_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityHerculesBeetle.class, EntityDataSerializers.INT);

    private int hurtTimer = 0;

    public EntityHerculesBeetle(EntityType<? extends EntityHerculesBeetle> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.herculesBeetle()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6468 — HerculesBeetle 250 HP / 30 ATK / 19 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.HERCULES_BEETLE.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, MobStats.HERCULES_BEETLE.attackDamage())
                .add(Attributes.ARMOR, MobStats.HERCULES_BEETLE.armor())
                .add(Attributes.FOLLOW_RANGE, 24.0);
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
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_HERCULES_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y + 0.25, motion.z);
        this.setPos(this.getX(), this.getY() + 0.5, this.getZ());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Signature vertical "gore" knockback — the beetle tosses victims up.
        // Preserved verbatim from the 1.7.10 doHurtTarget math (ks=0.45, vs=1.25).
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 0.45;
                double verticalKnockback = 1.25;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) verticalKnockback *= 2.0;
                target.push(Math.cos(angle) * knockbackStrength,
                        verticalKnockback * Math.abs(this.random.nextFloat()),
                        Math.sin(angle) * knockbackStrength);
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
        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }
    }

    // Death drops are fully data-driven via loot_table/entities/hercules_beetle.json
    // (orig HerculesBeetle.java:141-301: big hammer, painting, 4-11 raw beef,
    // 1-5 rolls of the d20 Diamond gear table).

    /** orig HerculesBeetle.java:442-481 — "Hercules Beetle" spawner bypass; darkness; night; y>=50; clear-air box; no other HerculesBeetle within 16/6/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.airBox(this, level, -2, 1, 2, 4, -2, 1)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityHerculesBeetle.class, 16.0, 6.0, 16.0);
    }
}
