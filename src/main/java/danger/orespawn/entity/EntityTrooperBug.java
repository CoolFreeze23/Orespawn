package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.entity.ai.TrooperBugLeapAttackGoal;
import danger.orespawn.util.MyUtils;
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
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityTrooperBug extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CLATTER = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "clatter"));
    private static final SoundEvent SND_CRUNCH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crunch"));
    private static final SoundEvent SND_EMPERORSCORPION_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
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

    /**
     * orig TrooperBug.java:50 {@code TargetSorter}, :63 {@code new GenericTargetSorter(this)}
     * — the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:515). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick {@link #selectTarget} handed to the target slot. 1.7.10 stored the scan's
     * pick nowhere — only the hurt-set attack target persisted (orig TrooperBug.java:414,
     * :395); the pick was acted on for that tick and re-derived on the next (:419-421) — so
     * the port re-runs the scan whenever the slot still holds its own pick, and leaves a
     * target set by any other path alone (see {@link #setTarget}). ENT-S-108.
     */
    @Nullable
    private LivingEntity scanPick;

    public EntityTrooperBug(EntityType<? extends EntityTrooperBug> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // TrooperBugLeapAttackGoal is a melee goal with an added "pounce"
        // behavior: when 4-8 blocks away and on the ground, 1-in-10 rolls
        // trigger jumpFromGround() which launches the bug forward and up.
        // This is the mob's signature move (its name evokes paratroopers).
        this.goalSelector.addGoal(1, new TrooperBugLeapAttackGoal(this, this::setAttacking));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // orig TrooperBug.java:70 registers no target-search task: prey is found by the
        // 1-in-5 EntityLivingBase box scan of :413-421 / :510-526, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // players-only NearestAttackableTargetGoal is gone.
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6489 — TrooperBug 200 HP / 20 ATK / 15 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.TROOPER_BUG.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, MobStats.TROOPER_BUG.attackDamage())
                .add(Attributes.ARMOR, MobStats.TROOPER_BUG.armor())
                .add(Attributes.FOLLOW_RANGE, 32.0);
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
            return SND_CLATTER;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_CRUNCH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_EMPERORSCORPION_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public void jumpFromGround() {
        // Overrides vanilla jump with a larger forward+upward impulse plus a
        // manual Y-raise so the bug clears the block it was standing on. The
        // navigation is stopped by tick() via the hasImpulse flag so the goal
        // doesn't immediately re-pathfind mid-leap.
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
        // orig TrooperBug.java:390 — after the hurt-timer lockout, "cactus" and
        // "fall" damage are filtered out entirely: no damage, no 20-tick timer
        // arm, no retarget-on-hurt (same idiom as sibling EntitySpitBug.java:151-156,
        // orig SpitBug.java:244).
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)) {
            return false;
        }
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 20;
        Entity attacker = source.getEntity();
        // orig TrooperBug.java:393-397 — a living attacker becomes the stored target, read ahead of the scan (:414): the scan's
        // mark on a pick that turned on it ends here, exactly when this hit stores it in the port — the Mob store below,
        // or super.hurt's lastHurtByMob record of this tick (the revenge goal's start); a hit that stores nothing keeps
        // the pick transient (ENT-S-129, the ownership convention)
        if (attacker != null && attacker == this.scanPick && (attacker instanceof Mob
                || (this.getLastHurtByMob() == attacker && this.getLastHurtByMobTimestamp() == this.tickCount))) this.scanPick = null;
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    /**
     * orig TrooperBug.java:404-451 {@code updateAITasks}: nothing while dead (:406-408),
     * super (:409), the hurt timer (:410-412), then the 1-in-5 block (:413): the target
     * selection (:414-421, {@link #selectTarget}), the look/leap/attack half (:422-440,
     * {@code TrooperBugLeapAttackGoal}, fed through the target slot) and the Spit Bug summon
     * (:441-443); the 1-in-150 regen (:448-450) follows. ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;                 // orig TrooperBug.java:406-408
        super.customServerAiStep();                   // orig :409
        if (this.hurtTimer > 0) --this.hurtTimer;     // orig :410-412
        // orig TrooperBug.java:413,441-443 — inside the 1-in-5 AI-tick block,
        // while a live target exists, a further 1-in-30 roll summons ONE Spit
        // Bug at the bug<->target midpoint (+- 0-4 block x/z scatter, +1.01 y).
        // spawnCreature (orig :453-462) gives it a random yaw and plays its
        // ambient sound. The look/leap/attack halves of that original block
        // live in TrooperBugLeapAttackGoal; the target selection (:414-421) and
        // the summon cadence (~1/150 per server tick while engaged) run here.
        if (this.random.nextInt(5) == 0) {            // orig :413
            this.selectTarget();                      // orig :414-421
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.random.nextInt(30) == 1) { // orig :441
                EntitySpitBug minion = ModEntities.ENTITY_SPIT_BUG.get().create(this.level());
                if (minion != null) {
                    minion.moveTo(
                            (this.getX() + target.getX()) / 2.0 + this.random.nextInt(5) - this.random.nextInt(5),
                            (this.getY() + target.getY()) / 2.0 + 1.01,
                            (this.getZ() + target.getZ()) / 2.0 + this.random.nextInt(5) - this.random.nextInt(5),
                            this.random.nextFloat() * 360.0f, 0.0f);
                    this.level().addFreshEntity(minion);
                    minion.playAmbientSound();
                }
            }
        }
        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) { // orig :448
            this.heal(1.0f);                          // orig :449
        }
    }

    /**
     * orig TrooperBug.java:414-421: the attack target set by being hurt (:395, a mob
     * attacker; port {@link #hurt} and {@code HurtByTargetGoal}, orig :70) is read first
     * (:414) and dropped once dead (:415-418); only without one does the scan run
     * (:419-421), and its pick was acted on for that tick alone — a candidate that had left
     * the 12/7/12 box or line of sight was simply not found next time (:444-446,
     * setAttacking(0)). The port's single target slot feeds the melee goal, so the scan's
     * own pick is re-derived on every cadence tick (replaced, or cleared when the scan comes
     * back empty), while a target set by any other path is kept, as the original kept its
     * attack target. ENT-S-108.
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :414 getAttackTarget
        if (current != null && !current.isAlive()) {               // orig :415-418
            this.setTarget(null);
            current = null;
        }
        if (current != null && current != this.scanPick) return;   // orig :419: the attack target stands
        LivingEntity pick = this.findSomethingToAttack();           // orig :420
        if (pick != current) super.setTarget(pick);                // super: the scan's own set keeps its ownership
        // Re-read the slot rather than trusting `pick`: a LivingChangeTargetEvent handler may
        // have substituted or cancelled the set, and a stale scanPick would stall the scan
        // (ENT-S-108 refuter hardening, 2026-09-04).
        this.scanPick = this.getTarget();
    }

    /**
     * A change of occupant by any other path — the revenge goal's start or stop, a hurt store, the melee goal's
     * forget roll, an event handler — ends the scan's ownership of the slot; a re-assert of the occupant already
     * there keeps it: {@code TargetGoal.canContinueToUse} re-sets the mob's CURRENT target on every cleanup pass
     * while the revenge goal runs, and an every-set clear turned the scan's own pick — placed on the pass that
     * dropped a dead revenge target — into a sticky one (ENT-S-117 refuter B's window). The port-wide convention
     * ruled in ENT-S-129 (the Water Dragon's ENT-S-117 form); the hurt hand-off is in {@link #hurt}. ENT-S-108.
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity before = this.getTarget();
        super.setTarget(target);
        if (this.getTarget() != before) this.scanPick = null; // ENT-S-129: the mark ends on a change of occupant only
    }

    /**
     * orig TrooperBug.java:510-526 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:511-513); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 12/7/12 (:514, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:515); the first the filter accepts wins
     * (:516-524), else null (:525). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :511-513
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 7.0, 12.0));                  // orig :514
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :515-525
    }

    /**
     * orig TrooperBug.java:464-508 {@code isSuitableTarget}, in the original's order: null /
     * self / dead (:465-473), the shared ignore screen (:474-476, ENT-S-106), line of sight
     * (:477-479), then the species chain — Hydrolisc (:480-482), EnderReaper (:483-485),
     * EnderKnight (:486-488), EntityEnderman (:489-491), EntityCreeper (:492-494), TrooperBug
     * (:495-497), SpitBug (:498-500) — and the player branch, creative refused (:501-506,
     * {@code isCreativeMode} = {@code Abilities.instabuild}); everything else that lives is
     * prey (:507). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :465-473
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :474-476
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :477-479
        if (target instanceof EntityHydrolisc) return false;                        // orig :480-482 Hydrolisc
        if (target instanceof EnderReaper) return false;                            // orig :483-485
        if (target instanceof EnderKnight) return false;                            // orig :486-488
        if (target instanceof EnderMan) return false;                               // orig :489-491 EntityEnderman
        if (target instanceof Creeper) return false;                                // orig :492-494
        if (target instanceof EntityTrooperBug) return false;                       // orig :495-497
        if (target instanceof EntitySpitBug) return false;                          // orig :498-500
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :501-506
        return true;                                                                // orig :507
    }

    // Death drops are fully data-driven via loot_table/entities/trooper_bug.json
    // (orig TrooperBug.java:200-359: jumpy bug scale, painting, 2-6 amethyst,
    // 1-5 rolls of the d14 Amethyst gear table).

    /** orig TrooperBug.java:536-570 — "Jumpy Bug" spawner bypass; darkness; daytime only on a 2-in-20 dice; clear-air box. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level) && this.getRandom().nextInt(20) > 1) return false;
        return OriginalSpawnGates.airBox(this, level, -2, 1, 1, 4, -2, 1);
    }
}
