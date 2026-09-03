package danger.orespawn.entity;

import danger.orespawn.entity.pose.Robot4Pose;
import danger.orespawn.MobStats;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;

import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

/**
 * Robot4 — the Robo-Warrior (orig Robot4.java:428 spawner tag).
 *
 * Hybrid melee/ranged platform (170 HP / 12 ATK / 18 armor, orig
 * OreSpawnMain.java:6497). Point-blank it hammers with a doubled-lift
 * knockback swing (orig :256-267); past melee reach it shoulder-fires
 * {@link LaserBall}s whenever the target is within 0.5 rad of its head
 * facing, and beyond distSq 65 the ball is upgraded to the "special"
 * (explosive) variant with a longer 30-tick reload (orig :294-326,
 * ENT-K-069). It destroys no terrain — the block-griefing this port
 * once carried belongs to Robot2, the Robo-Pounder (ENT-K-063). The
 * shielding flag ({@link #setShielding}) is written only by the client
 * model's arm-pump animation, exactly like the original's
 * ModelRobot4:447-451 (see {@link #hurt} for why that leaves the
 * server-side shield gate permanently cold, ENT-K-070). Registry ID
 * kept as "robot_4" for save compat.
 */
public class Robot4 extends Monster implements Robot4Pose {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROBOT_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_living"));
    private static final SoundEvent SND_ROBOT_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_hurt"));
    private static final SoundEvent SND_ROBOT_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robot_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SHIELDING =
            SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);

    // TF-035: orig Robot4.java:41,54 — targets sort with GenericTargetSorter
    // (creeper-halved / big-silhouette-first), not plain distance.
    private final GenericTargetSorter targetSorter;
    private int reloadTicker = 0;
    private int wasAttackedTicker = 0;
    private final float moveSpeed = 0.34f;

    public Robot4(EntityType<? extends Robot4> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.targetSorter = new GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6497 — Robot4 170 HP / 12 ATK / 18 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ROBOT4.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ROBOT4.attackDamage())
                .add(Attributes.ARMOR, MobStats.ROBOT4.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SHIELDING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getShielding() { return this.entityData.get(DATA_SHIELDING); }
    public void setShielding(int value) { this.entityData.set(DATA_SHIELDING, value); }

    @Override
    public void jumpFromGround() {
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, velocity.y + 0.25, velocity.z);
        super.jumpFromGround();
    }

    /**
     * ENT-K-069: orig Robot4.java:133-143 — client-side ambience only. The
     * invented block-griefing that used to live here was a relocation of
     * Robot2's terrain destruction; orig Robot4 never breaks a block
     * (removed, see ENT-K-063 for the restored Robot2 version).
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            // orig :136-138 — exhaust smoke behind the body (yRot + 180) on a
            // 1-in-3 roll, y jittered +3.0..+4.0.
            if (this.getRandom().nextInt(3) == 1) {
                double angle = Math.toRadians(this.getYRot() + 180.0f);
                this.level().addParticle(ParticleTypes.SMOKE,
                        getX() - 1.25 * Math.sin(angle),
                        getY() + 3.0 + this.getRandom().nextFloat(),
                        getZ() + 1.25 * Math.cos(angle),
                        0, this.getRandom().nextFloat() / 2.0, 0);
            }
            // orig :139-141 — "reddust" stream off the cannon side (yRot + 35)
            // every client tick while the attack pose is up.
            if (this.getAttacking() != 0) {
                double angle = Math.toRadians(this.getYRot() + 35.0f);
                this.level().addParticle(DustParticleOptions.REDSTONE,
                        getX() - 1.55 * Math.sin(angle),
                        getY() + 2.25 + this.getRandom().nextFloat(),
                        getZ() + 1.55 * Math.cos(angle),
                        0, this.getRandom().nextFloat(), 0);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            double knockbackStrength = 2.0;
            double upwardKnockback = 0.12;
            float angleToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
            if (livingTarget.isRemoved() || livingTarget instanceof Player) upwardKnockback *= 2.0;
            livingTarget.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
        }
        return super.doHurtTarget(target);
    }

    /** ENT-K-069: orig Robot4.java:269-334 — hybrid melee/ranged think loop. */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.reloadTicker > 0) --this.reloadTicker;
        if (this.wasAttackedTicker > 0) --this.wasAttackedTicker;
        // orig :280 — think-tick gated on reload done + a 1-in-8 roll.
        if (this.reloadTicker == 0 && this.getRandom().nextInt(8) == 1) {
            LivingEntity target = this.getTarget();
            if (this.getRandom().nextInt(50) == 1) this.setTarget(null);
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) < 256.0) {
                    double meleeRange = 3.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                        // orig :295-296 — point-blank hammer swing; no reload
                        // cost and no attack-pose latch on the melee path.
                        this.doHurtTarget(target);
                    } else {
                        // orig :298-305 — the cannon fires only when the
                        // target is within 0.5 rad of the HEAD facing
                        // (yHeadRot + 90), a tighter cone than Robot2's 1.25.
                        double rr = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                        double rhdir = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
                        double pi = 3.1415926545; // orig :300 — truncated pi constant
                        double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                        if (rdd > pi) rdd -= pi * 2.0;
                        rdd = Math.abs(rdd);
                        if (rdd < 0.5) {
                            fireLaserAt(target);
                        }
                        this.setAttacking(1); // orig :325 — raised on any ranged think-tick, aimed or not
                    }
                    this.getNavigation().moveTo(target, 0.75); // orig :327
                }
            }
        }
        // orig :331-333 — the attack pose drops only once both the reload and
        // the 65-tick post-hit window have run out.
        if (this.reloadTicker <= 0 && this.wasAttackedTicker <= 0) {
            this.setAttacking(0);
        }
    }

    /**
     * ENT-K-069: orig Robot4.java:305-324 — shoulder-cannon shot. The ball
     * spawns 1.75 blocks out at (yRot + 45°) and 2.0 up, is lobbed with a
     * 0.2 × horizontal-distance arc boost at speed 2.0 / inaccuracy 4.0.
     * Beyond distSq 65 it becomes the "special" (explosive) variant with a
     * 30-tick reload and a deep launch report (3.5 vol / 0.5 pitch); inside
     * that it stays normal on a fast 10-tick reload (2.5 vol / 1.0 pitch).
     * Plumbing mirrors Robot3#fireLaserAt (shooter-ctor for kill credit).
     */
    private void fireLaserAt(LivingEntity target) {
        if (this.level().isClientSide) return;
        LaserBall ball = new LaserBall(this.level(), this);
        double yoff = 2.0;   // orig :306
        double xzoff = 1.75; // orig :307
        ball.setPos(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() + 45.0f)),
                this.getY() + yoff,
                this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() + 45.0f))); // orig :309
        double dx = target.getX() - ball.getX();
        double dy = target.getY() - ball.getY();
        double dz = target.getZ() - ball.getZ();
        float arc = (float) Math.sqrt(dx * dx + dz * dz) * 0.2f; // orig :313
        ball.shoot(dx, dy + arc, dz, 2.0f, 4.0f); // orig :314
        if (this.distanceToSqr(target) > 65.0) {
            ball.setSpecial();      // orig :316
            this.reloadTicker = 30; // orig :317
            // orig :318 — "fireworks.launch" 3.5f / 0.5f for the special shot.
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 3.5f, 0.5f);
        } else {
            this.reloadTicker = 10; // orig :320
            // orig :321 — "fireworks.launch" 2.5f / 1.0f for the normal shot.
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 2.5f, 1.0f);
        }
        this.level().addFreshEntity(ball); // orig :323
    }

    /**
     * orig Robot4.java:336-355. ENT-K-070 note: the shielding half of the
     * :339 gate is permanently cold on the server — the only setShielding(1)
     * caller in the ORIGINAL is the client model's arm-pump animation
     * (ModelRobot4.java:447-451), whose datawatcher write never reaches the
     * server. The port's ModelRobot4 reproduces that client-side write, so
     * the effective post-hit immunity here is the 65-tick wasAttackedTicker
     * window (orig :342), bug-for-bug.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        if (this.getShielding() != 0 || this.wasAttackedTicker != 0) return false;
        this.wasAttackedTicker = 65;
        this.setAttacking(1);
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
            ret = true; // orig :350 — forced true for mob attackers
        }
        return ret;
    }

    private LivingEntity findSomethingToAttack() {
        // orig Robot4.java:386-388 — PlayNicely disables acquisition entirely.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(16.0, 4.0, 16.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        // (was: .sort orig :390 — GenericTargetSorter)
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig Robot4.java:367-369 — the shared ignore screen (ENT-S-106)
        if (target instanceof Monster) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
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

    /** orig Robot4.java:415-449 — "Robo-Warrior" spawner bypass; y>=50; night; air/short-grass clearance above; darkness. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.boxMatches(this, level, -1, 1, 1, 5, -1, 0,
                s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS))) return false;
        return OriginalSpawnGates.isDarkEnough(this, level);
    }
}
