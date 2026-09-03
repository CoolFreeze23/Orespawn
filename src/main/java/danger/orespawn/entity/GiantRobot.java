package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

public class GiantRobot extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROBOT_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
    private static final SoundEvent SND_ROBOT_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    private static final SoundEvent SND_ROBOT_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    private static final double MELEE_KNOCKBACK_HORIZONTAL = 2.2;
    private static final double MELEE_KNOCKBACK_VERTICAL = 0.25;
    private static final double LONG_RANGE_TARGET_DISTANCE_SQR = 256.0;

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(GiantRobot.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int reloadTicker = 0;
    private final float moveSpeed = 0.55f;

    public GiantRobot(EntityType<? extends GiantRobot> type, Level level) {
        super(type, level);
        // orig GiantRobot.java:48 — XP = Jeffery_stats.health / 2 = 550/2.
        this.xpReward = 275;
        // TF-035: orig GiantRobot.java:39,51 — targets sort with the shared
        // GenericTargetSorter (creeper distance halved, large silhouettes
        // prioritized), not plain distance; consumed by the
        // findSomethingToAttack scan (orig :347).
        this.targetSorter = new GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // orig GiantRobot.java:53
        // orig GiantRobot.java:54 — MyEntityAIWanderALot(14, 1.0) @1, not a
        // vanilla stroll: 1-in-30 roll per tick, 14-block xz range, and it
        // keeps wandering even when that would path through water.
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        // orig GiantRobot.java:55 — EntityAIMoveThroughVillage(0.9f, false) @2.
        // Mapping decision (same as the Alien/Alosaurus batches): the 1.7.10
        // goal walked the door list of the pre-1.14 village object; that
        // framework is gone and vanilla MoveThroughVillageGoal is its direct
        // POI-driven descendant. Same speed (0.9f, widened exactly as the
        // orig's (double)0.9f cast), same day-and-night operation (orig third
        // arg false = not nocturnal-only), vanilla-standard distanceToPoi 4
        // (the param has no 1.7.10 analog), no door-breaking (the 1.7.10 goal
        // never broke doors).
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.9f, false, 4, () -> false));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f)); // orig GiantRobot.java:56
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this)); // orig GiantRobot.java:57
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); // orig GiantRobot.java:58
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                // orig OreSpawnMain.java:6476 — "Jeffery" 550 HP / 40 ATK / 18 armor
                // (the original GiantRobot consumes Jeffery_stats);
                // speed 0.55 matches orig GiantRobot.java:42.
                .add(Attributes.MAX_HEALTH, MobStats.JEFFERY.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, MobStats.JEFFERY.attackDamage())
                .add(Attributes.ARMOR, MobStats.JEFFERY.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    public void jumpFromGround() {
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y + 0.25, motion.z);
        super.jumpFromGround();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        double knockbackHorizontal = MELEE_KNOCKBACK_HORIZONTAL;
        double knockbackVertical = MELEE_KNOCKBACK_VERTICAL;
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity livingTarget) {
                float pushAngle = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
                if (livingTarget.isRemoved() || livingTarget instanceof Player) knockbackVertical *= 2.0;
                livingTarget.push(Math.cos(pushAngle) * knockbackHorizontal, knockbackVertical, Math.sin(pushAngle) * knockbackHorizontal);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.reloadTicker > 0) --this.reloadTicker;
        if (this.getRandom().nextInt(5) == 0) {
            LivingEntity currentTarget = this.getTarget();
            if (this.getRandom().nextInt(100) == 1) this.setTarget(null);
            if (currentTarget != null && !currentTarget.isAlive()) {
                this.setTarget(null);
                currentTarget = null;
            }
            if (currentTarget == null) currentTarget = findSomethingToAttack();
            if (currentTarget != null) {
                this.lookAt(currentTarget, 10.0f, 10.0f);
                if (this.distanceToSqr(currentTarget) < LONG_RANGE_TARGET_DISTANCE_SQR) {
                    // orig GiantRobot.java:256-263 — only engage once the head
                    // has swung to within 0.5 rad of the target bearing.
                    double targetBearing = Math.atan2(currentTarget.getZ() - this.getZ(),
                            currentTarget.getX() - this.getX());
                    double headBearing = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
                    double bearingError = Math.abs(targetBearing - headBearing) % (Math.PI * 2.0);
                    if (bearingError > Math.PI) {
                        bearingError -= Math.PI * 2.0;
                    }
                    if (Math.abs(bearingError) < 0.5) {
                        if (this.reloadTicker == 0) {
                            this.fireLaserBall(currentTarget);
                        }
                        double meleeRange = (8.0f + currentTarget.getBbWidth() / 2.0f);
                        if (this.distanceToSqr(currentTarget) < meleeRange * meleeRange) {
                            this.setAttacking(1);
                            this.doHurtTarget(currentTarget);
                        } else {
                            this.setAttacking(0);
                        }
                    }
                    this.getNavigation().moveTo(currentTarget, 0.5);
                } else {
                    this.setAttacking(0);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * orig GiantRobot.java:264-283 — chest canon: LaserBall from a muzzle
     * 3.75 blocks ahead of the head bearing at y+10, aimed with a
     * 0.2-per-horizontal-block arc, velocity 2.0, inaccuracy 4.0. Beyond
     * distSq 100 the ball is {@code setSpecial()} (explosive) with a 25-tick
     * reload and a deeper launch sound (3.5 vol / 0.5 pitch); closer shots
     * reload in 10 ticks (2.5 vol / 1.0 pitch).
     */
    private void fireLaserBall(LivingEntity target) {
        double yoff = 10.0;
        double xzoff = 3.75;
        double muzzleX = this.getX() - xzoff * Math.sin(Math.toRadians(this.yHeadRot));
        double muzzleY = this.getY() + yoff;
        double muzzleZ = this.getZ() + xzoff * Math.cos(Math.toRadians(this.yHeadRot));
        LaserBall laserBall = new LaserBall(this.level(), this);
        laserBall.setPos(muzzleX, muzzleY, muzzleZ);

        double dx = target.getX() - muzzleX;
        double dy = target.getY() - muzzleY;
        double dz = target.getZ() - muzzleZ;
        double arc = Math.sqrt(dx * dx + dz * dz) * 0.2;
        laserBall.shoot(dx, dy + arc, dz, 2.0f, 4.0f);
        if (this.distanceToSqr(target) > 100.0) {
            laserBall.setSpecial();
            this.reloadTicker = 25;
            this.level().playSound(null, this, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    this.getSoundSource(), 3.5f, 0.5f);
        } else {
            this.reloadTicker = 10;
            this.level().playSound(null, this, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    this.getSoundSource(), 2.5f, 1.0f);
        }
        this.level().addFreshEntity(laserBall);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
        }
        return ret;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(16.0, 12.0, 16.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        // (was: .sort orig GiantRobot.java:347 — GenericTargetSorter order)
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig GiantRobot.java:324-326 — the shared ignore screen (ENT-S-106)
        if (target instanceof Monster) return false;
        if (target instanceof Player player && player.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0)
            return SND_ROBOT_LIVING;
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SND_ROBOT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ROBOT_DEATH;
    }

    /** orig GiantRobot.java:364-381 — y>=50; night; air/short-grass clearance above; darkness. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,
                s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;
        return OriginalSpawnGates.isDarkEnough(this, level);
    }
}
