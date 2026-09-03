package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.SpitBugAcidAttackGoal;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;
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
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntitySpitBug extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_CLATTER = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "clatter"));
    private static final SoundEvent SND_CRUNCH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "crunch"));
    private static final SoundEvent SND_EMPERORSCORPION_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntitySpitBug.class, EntityDataSerializers.INT);

    private static final double KNOCKBACK_STRENGTH = 0.5;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double KNOCKBACK_VERTICAL_PLAYER_MULT = 2.0;
    private static final double JUMP_HORIZONTAL_MIN = 0.2f;
    private static final double JUMP_HORIZONTAL_RANDOM = 0.45f;
    private static final double JUMP_VERTICAL_BOOST = 0.75;

    private int hurtTimer = 0;

    /**
     * orig SpitBug.java:47 {@code TargetSorter}, :61 {@code new GenericTargetSorter(this)} —
     * the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:375). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick {@link #selectTarget} handed to the target slot. 1.7.10 stored the scan's
     * pick nowhere — only the hurt-set attack target persisted (orig SpitBug.java:268, :249);
     * the pick was acted on for that tick and re-derived on the next (:273-275) — so the port
     * re-runs the scan whenever the slot still holds its own pick, and leaves a target set by
     * any other path alone (see {@link #setTarget}). ENT-S-108.
     */
    @Nullable
    private LivingEntity scanPick;

    public EntitySpitBug(EntityType<? extends EntitySpitBug> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // SpitBugAcidAttackGoal handles the full combat loop: in-range swing
        // (burrow pincer) + out-of-range 8-round Acid projectile burst. This
        // restores the 1.7.10 "water-canon" behavior that was missing in the
        // initial 1.21.1 port (the bug previously only had a melee swing).
        this.goalSelector.addGoal(1, new SpitBugAcidAttackGoal(this, this::setAttacking));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // orig SpitBug.java:68 registers no target-search task: prey is found by the
        // 1-in-5 EntityLivingBase box scan of :267-275 / :370-386, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // players-only NearestAttackableTargetGoal is gone.
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6490 — SpitBug 100 HP / 10 ATK / 12 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SPIT_BUG.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SPIT_BUG.attackDamage())
                .add(Attributes.ARMOR, MobStats.SPIT_BUG.armor())
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

    /**
     * orig SpitBug.java:239-256 — hurt-timer lockout, then cactus and fall
     * damage are ignored entirely (no hurt, no timer reset, no retaliation).
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)) {
            return false; // orig :244 — "cactus" / "fall" filtered
        }
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 15;
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    /**
     * orig SpitBug.java:258-299 {@code updateAITasks}: nothing while dead (:260-262), super
     * (:263), the hurt timer (:264-266), then on the 1-in-5 tick (:267) the target selection
     * (:268-275, {@link #selectTarget}); the rest of that block (:276-294: look, the
     * nextInt(15)==1 leap, reach distSq &lt; 9, the nextInt(6)==0 || nextInt(7)==1 swing with
     * its clatter, the chase at 0.5 with the acid stream) is {@code SpitBugAcidAttackGoal},
     * fed through the target slot; the 1-in-150 regen (:296-298) follows. ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;                 // orig SpitBug.java:260-262
        super.customServerAiStep();                   // orig :263
        if (this.hurtTimer > 0) --this.hurtTimer;     // orig :264-266
        if (this.random.nextInt(5) == 0) {            // orig :267
            this.selectTarget();                      // orig :268-275
        }
        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) { // orig :296
            this.heal(1.0f);                          // orig :297
        }
    }

    /**
     * orig SpitBug.java:268-275: the attack target set by being hurt (:249, a mob attacker;
     * port {@link #hurt} and {@code HurtByTargetGoal}, orig :68) is read first (:268) and
     * dropped once dead (:269-272); only without one does the scan run (:273-275), and its
     * pick was acted on for that tick alone — a candidate that had left the 12/7/12 box or
     * line of sight was simply not found next time (:292-294, setAttacking(0)). The port's
     * single target slot feeds the melee goal, so the scan's own pick is re-derived on every
     * cadence tick (replaced, or cleared when the scan comes back empty), while a target set
     * by any other path is kept, as the original kept its attack target. ENT-S-108.
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :268 getAttackTarget
        if (current != null && !current.isAlive()) {               // orig :269-272
            this.setTarget(null);
            current = null;
        }
        if (current != null && current != this.scanPick) return;   // orig :273: the attack target stands
        LivingEntity pick = this.findSomethingToAttack();           // orig :274
        if (pick != current) super.setTarget(pick);                // super: the scan's own set keeps its ownership
        // Re-read the slot rather than trusting `pick`: a LivingChangeTargetEvent handler may
        // have substituted or cancelled the set, and a stale scanPick would stall the scan
        // (ENT-S-108 refuter hardening, 2026-09-04).
        this.scanPick = this.getTarget();
    }

    /** A target set by any other path ends the scan's ownership of the slot; see {@link #selectTarget}. ENT-S-108. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        this.scanPick = null;
    }

    /**
     * orig SpitBug.java:370-386 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:371-373); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 12/7/12 (:374, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:375); the first the filter accepts wins
     * (:376-384), else null (:385). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :371-373
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 7.0, 12.0));                  // orig :374
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :375-385
    }

    /**
     * orig SpitBug.java:324-368 {@code isSuitableTarget}, in the original's order: null / self
     * / dead (:325-333), the shared ignore screen (:334-336, ENT-S-106), line of sight
     * (:337-339), then the species chain — EnderReaper (:340-342), EnderKnight (:343-345),
     * EntityEnderman (:346-348), Hydrolisc (:349-351), EntityCreeper (:352-354), SpitBug
     * (:355-357), TrooperBug (:358-360) — and the player branch, creative refused (:361-366,
     * {@code isCreativeMode} = {@code Abilities.instabuild}); everything else that lives is
     * prey (:367). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :325-333
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :334-336
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :337-339
        if (target instanceof EnderReaper) return false;                            // orig :340-342
        if (target instanceof EnderKnight) return false;                            // orig :343-345
        if (target instanceof EnderMan) return false;                               // orig :346-348 EntityEnderman
        if (target instanceof EntityHydrolisc) return false;                        // orig :349-351 Hydrolisc
        if (target instanceof Creeper) return false;                                // orig :352-354
        if (target instanceof EntitySpitBug) return false;                          // orig :355-357
        if (target instanceof EntityTrooperBug) return false;                       // orig :358-360
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :361-366
        return true;                                                                // orig :367
    }

    /** orig SpitBug.java:396-430 — "Spit Bug" spawner bypass; daytime only on a 2-in-20 dice; darkness; clear-air box. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (OriginalSpawnGates.isDaytime(level) && this.getRandom().nextInt(20) > 1) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return OriginalSpawnGates.airBox(this, level, -2, 1, 1, 3, -2, 1);
    }
}
