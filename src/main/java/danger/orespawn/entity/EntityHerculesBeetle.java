package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
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

    /**
     * orig HerculesBeetle.java:40 {@code TargetSorter}, :51 {@code new GenericTargetSorter(this)}
     * — the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:421). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick {@link #selectTarget} handed to the target slot. 1.7.10 stored the scan's
     * pick nowhere — only the hurt-set attack target persisted (orig HerculesBeetle.java:351,
     * :333); the pick was acted on for that tick and re-derived on the next (:356-358) — so
     * the port re-runs the scan whenever the slot still holds its own pick, and leaves a
     * target set by any other path alone (see {@link #setTarget}). ENT-S-108.
     */
    @Nullable
    private LivingEntity scanPick;

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
        // orig HerculesBeetle.java:57 registers no target-search task: prey is found by
        // the 1-in-4 EntityLivingBase box scan of :350-358 / :416-432, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // players-only NearestAttackableTargetGoal is gone.
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

    /**
     * orig HerculesBeetle.java:341-383 {@code updateAITasks}: nothing while dead (:343-345),
     * super (:346), the hurt timer (:347-349), then on the 1-in-4 tick (:350) the target
     * selection (:351-358, {@link #selectTarget}); the rest of that block (:359-378: look,
     * reach (5 + w/2)^2, the nextInt(3)==0 || nextInt(4)==1 swing with its sounds, the chase
     * at 1.2, setAttacking) is {@code BugMeleeAttackGoal.Params.herculesBeetle()}, fed
     * through the target slot; the 1-in-150 regen (:380-382) follows. ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;                 // orig HerculesBeetle.java:343-345
        super.customServerAiStep();                   // orig :346
        if (this.hurtTimer > 0) --this.hurtTimer;     // orig :347-349
        if (this.random.nextInt(4) == 0) {            // orig :350
            this.selectTarget();                      // orig :351-358
        }
        if (this.random.nextInt(150) == 1 && this.getHealth() < this.getMaxHealth()) { // orig :380
            this.heal(2.0f);                          // orig :381
        }
    }

    /**
     * orig HerculesBeetle.java:351-358: the attack target set by being hurt (:333, a mob
     * attacker; port {@link #hurt} and {@code HurtByTargetGoal}, orig :57) is read first
     * (:351) and dropped once dead (:352-355); only without one does the scan run (:356-358),
     * and its pick was acted on for that tick alone — a candidate that had left the 16/6/16
     * box or line of sight was simply not found next time (:376-378, setAttacking(0)). The
     * port's single target slot feeds the melee goal, so the scan's own pick is re-derived
     * on every cadence tick (replaced, or cleared when the scan comes back empty), while a
     * target set by any other path is kept, as the original kept its attack target.
     * ENT-S-108.
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :351 getAttackTarget
        if (current != null && !current.isAlive()) {               // orig :352-355
            this.setTarget(null);
            current = null;
        }
        if (current != null && current != this.scanPick) return;   // orig :356: the attack target stands
        LivingEntity pick = this.findSomethingToAttack();           // orig :357
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
     * orig HerculesBeetle.java:416-432 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:417-419); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 16/6/16 (:420, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:421); the first the filter accepts wins
     * (:422-430), else null (:431). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :417-419
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));                  // orig :420
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :421-431
    }

    /**
     * orig HerculesBeetle.java:385-414 {@code isSuitableTarget}, in the original's order: null
     * / self / dead (:386-394), the shared ignore screen (:395-397, ENT-S-106), line of sight
     * (:398-400), then the species chain — EntityCreeper (:401-403), HerculesBeetle
     * (:404-406) — and the player branch, creative refused (:407-412, {@code isCreativeMode}
     * = {@code Abilities.instabuild}); everything else that lives is prey (:413). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :386-394
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :395-397
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :398-400
        if (target instanceof Creeper) return false;                                // orig :401-403
        if (target instanceof EntityHerculesBeetle) return false;                   // orig :404-406
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :407-412
        return true;                                                                // orig :413
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
