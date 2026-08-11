package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;

import java.util.List;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Basilisk extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);

    private static final double KNOCKBACK_HORIZONTAL = 1.5;
    private static final double KNOCKBACK_VERTICAL = 0.15;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;

    private int hurtTimer = 0;
    private final float moveSpeed = 0.4f;
    private final GenericTargetSorter targetSorter;

    public Basilisk(EntityType<? extends Basilisk> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        // TF-035: orig Basilisk.java:43,53 — targets sort with
        // GenericTargetSorter (creeper-halved / big-silhouette-first).
        this.targetSorter = new GenericTargetSorter(this);
    }

    // ENT-A-032: all attacking runs through the customServerAiStep re-scan
    // below, exactly as the orig's func_70619_bc — no vanilla target-selector
    // acquisition, no gaze/aura goal (that was invented in the first pass).
    @Override
    protected void registerGoals() {
        // orig Basilisk.java:54-59 — goal lineup in original priority order.
        this.goalSelector.addGoal(0, new FloatGoal(this)); // orig :54
        // orig :55 — EntityAIMoveThroughVillage(1.0, false). Vanilla
        // MoveThroughVillageGoal is the same goal line; 1.14+ villages are
        // POI clusters (beds/workstations) rather than door lists, so it
        // engages only where village POI exists nearby — the honest modern
        // mapping (documented per the MoveIndoorsGoal pattern). No door AI
        // in the orig, hence canDealWithDoors=false.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        // orig :56 — MyEntityAIWanderALot(20, 1.0).
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f)); // orig :57
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this)); // orig :58
        // orig :59 — EntityAIHurtByTarget(false): revenge memory only; the
        // scan in customServerAiStep does all the attacking, so the stored
        // target is as decorative here as it was in 1.7.10.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6487 — Basilisk 200 HP / 24 ATK / 15 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.BASILISK.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, MobStats.BASILISK.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ARMOR, MobStats.BASILISK.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    public int mygetMaxHealth() {
        // orig Basilisk.java:83-85 — reads Basilisk_stats.health (200,
        // orig OreSpawnMain.java:6487); the 1-in-75 heal gate compares
        // against this value.
        return (int) MobStats.BASILISK.maxHealth();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // orig Basilisk.java:114-119 — basilisk_living 1-in-2, else silent.
        if (this.getRandom().nextInt(2) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "basilisk_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig Basilisk.java:121-123
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        // orig Basilisk.java:125-127
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // Death drops are fully data-driven via loot_table/entities/basilisk.json
    // (orig Basilisk.java:151-310: basilisk scale, painting, 12-17 emerald,
    // 8-12 raw chicken, 3-7 rolls of the d15 Emerald gear table).

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                // orig Basilisk.java:319-327 — Poison duration scales with
                // difficulty: peaceful 8s, easy 10s, normal 12s, hard 14s.
                int poisonSeconds = switch (this.level().getDifficulty()) {
                    case EASY   -> 10;
                    case NORMAL -> 12;
                    case HARD   -> 14;
                    default     -> 8;
                };
                // orig :328-330 — 1-in-3 per landed bite.
                if (this.getRandom().nextInt(3) == 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, poisonSeconds * 20, 0));
                }
                double verticalKnockback = KNOCKBACK_VERTICAL;
                float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
                }
                target.push(
                        Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                        verticalKnockback,
                        Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) {
            return false;
        }
        this.hurtTimer = 30;
        return super.hurt(source, amount);
    }

    // orig Basilisk.java:352-382 (func_70619_bc) — the entire combat AI is
    // this 1-in-5 cadence re-scan; there is no persistent vanilla-style
    // acquisition. Each successful scan re-picks the sorted-nearest suitable
    // target, so the Basilisk switches victims mid-fight just like 1.7.10.
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) {
            --this.hurtTimer;
        }

        if (this.getRandom().nextInt(5) == 0) { // orig :360
            LivingEntity e = findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f); // orig :363
                // orig :364 — reach is 6 plus the TARGET's half-width.
                double reach = 6.0f + e.getBbWidth() / 2.0f;
                if (this.distanceToSqr(e) < reach * reach) {
                    this.setAttacking(1); // orig :365
                    // orig :366 — nested swing dice (1/3, else 1/4).
                    if (this.getRandom().nextInt(3) == 0 || this.getRandom().nextInt(4) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25); // orig :370
                }
                // orig :372-374 — Slowness V 100t lands on the scanned target
                // whether IN or OUT of bite reach; the legacy "gaze" is this
                // unconditional debuff, not a radius aura.
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5));
            } else {
                this.setAttacking(0); // orig :376
            }
        }

        // orig :379-381 — 1-in-75 regen tick while below max health.
        if (this.getRandom().nextInt(75) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    private LivingEntity findSomethingToAttack() {
        // orig Basilisk.java:416-418 — PlayNicely disables aggression entirely.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(24.0, 7.0, 24.0)); // orig :419
        list.sort(this.targetSorter); // orig :420 — GenericTargetSorter
        for (LivingEntity candidate : list) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    /**
     * orig Basilisk.java:384-413 — anything living is fair game except
     * ignoreables, other Basilisks, Leaf Monsters, and creative players;
     * line of sight required.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig :394-396
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :397-399
        if (target instanceof Basilisk) return false; // orig :400-402
        if (target instanceof EntityLeafMonster) return false; // orig :403-405
        if (target instanceof Player p && p.getAbilities().instabuild) return false; // orig :406-411
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isRemoved()) return;
        if (this.getRandom().nextInt(200) == 0) {
            this.heal(1.0f);
        }
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    /** orig Basilisk.java:441-477 — spawner bypass; darkness; night; clear air above; no other Basilisk within 20/6/20. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 4, -1, 1)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, Basilisk.class, 20.0, 6.0, 20.0);
    }
}
