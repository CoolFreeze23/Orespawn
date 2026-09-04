package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.EmperorScorpionPoisonGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityEmperorScorpion extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_EMPERORSCORPION_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityEmperorScorpion.class, EntityDataSerializers.INT);

    private int hurtTimer = 0;
    private int healTimer = 0;

    /**
     * Per-entity render scratch (orig EmperorScorpion.java:53 {@code renderdata = new RenderInfo()},
     * re-created :65, zeroed :77-87, accessor :110-112). Mutated client-side by
     * {@code EmperorScorpionModel} for the claw/tail selector latch
     * (orig ModelEmperorScorpion.java:613-641); never datawatcher-synced. ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /**
     * orig EmperorScorpion.java:52 {@code TargetSorter}, :64 {@code new GenericTargetSorter(this)}
     * — the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:508). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick {@link #selectTarget} handed to the target slot. 1.7.10 stored the scan's
     * pick nowhere — only the hurt-set attack target persisted (orig EmperorScorpion.java:409,
     * :391); the pick was acted on for that tick and re-derived on the next (:417-419) — so
     * the port re-runs the scan whenever the slot still holds its own pick, and leaves a
     * target set by any other path alone (see {@link #setTarget}). ENT-S-108.
     */
    @Nullable
    private LivingEntity scanPick;

    /** orig EmperorScorpion.java:71 — the revenge task; the pass's 1-in-100 release ends it (see {@link RevengeGoal}); assigned in registerGoals (the Mob constructor). ENT-S-129. */
    private RevengeGoal revengeGoal;

    public EntityEmperorScorpion(EntityType<? extends EntityEmperorScorpion> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // EmperorScorpionPoisonGoal extends BugMeleeAttackGoal and layers in
        // the 1/3-chance 90-tick poison effect that the 1.7.10 source applied
        // inside doHurtTarget. Moving it into the goal keeps doHurtTarget
        // focused on knockback, and ensures the poison only lands when the
        // attack actually connects (respects invulnerability frames).
        this.goalSelector.addGoal(1, new EmperorScorpionPoisonGoal(this, this::setAttacking));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal); // orig EmperorScorpion.java:71 — EntityAIHurtByTarget(this, false), released by the pass's 1-in-100 (ENT-S-129)
        // orig EmperorScorpion.java:71 registers no target-search task: prey is found by
        // the 1-in-4 EntityLivingBase box scan of :408-419 / :503-519, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // players-only NearestAttackableTargetGoal is gone.
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6488 — EmperorScorpion 350 HP / 35 ATK / 20 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.EMPEROR_SCORPION.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.EMPEROR_SCORPION.attackDamage())
                .add(Attributes.ARMOR, MobStats.EMPEROR_SCORPION.armor())
                .add(Attributes.FOLLOW_RANGE, 40.0);
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

    /** Mirrors orig EmperorScorpion.java:110-112 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
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
        return SND_EMPERORSCORPION_DEATH;
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
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 3.0;
                double verticalKnockback = 0.2;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) verticalKnockback *= 2.0;
                target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 30;
        Entity attacker = source.getEntity();
        // orig EmperorScorpion.java:389-393 — a living attacker becomes the stored target, read ahead of the scan (:409): the scan's
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

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.hurtTimer > 0) --this.hurtTimer;
        // Slow regen — preserved from 1.7.10 (~1-in-100 tick roll).
        if (++this.healTimer >= 100 && this.getHealth() < this.getMaxHealth()) {
            this.healTimer = 0;
            if (this.random.nextInt(100) == 1) this.heal(2.0f);
        }

        // orig EmperorScorpion.java:408-443 — the nextInt(4)==0 AI gate: the target
        // selection (:409-419, selectTarget — ENT-S-108), then the melee half (:420-436:
        // look, reach (6 + w/2)^2, the nextInt(4)==0 || nextInt(6)==1 swing with its
        // sounds, the chase at 1.2, setAttacking) which is EmperorScorpionPoisonGoal,
        // fed through the target slot, and the baby-scorpion summon (:437-438): while a
        // combat target exists, a nextInt(20)==1 roll spawns one Scorpion at the
        // midpoint between Emperor and target (±nextInt(5) horizontal jitter, y
        // midpoint +1.01). No population cap, no cooldown, no ground check in the
        // original.
        if (this.random.nextInt(4) == 0) {                                   // orig :408
            this.selectTarget();                                             // orig :409-419
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.random.nextInt(20) == 1) { // orig :437
                spawnBabyScorpionToward(target);                             // orig :438
            }
        }
    }

    /** orig EmperorScorpion.java:437-438 — midpoint spawn between self and target. */
    private void spawnBabyScorpionToward(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) return;
        EntityScorpion minion = ModEntities.ENTITY_SCORPION.get().create(server);
        if (minion == null) return;
        double x = (this.getX() + target.getX()) / 2.0
                + this.random.nextInt(5) - this.random.nextInt(5);
        double y = (this.getY() + target.getY()) / 2.0 + 1.01;
        double z = (this.getZ() + target.getZ()) / 2.0
                + this.random.nextInt(5) - this.random.nextInt(5);
        minion.moveTo(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
        minion.finalizeSpawn(server, server.getCurrentDifficultyAt(minion.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(minion);
    }

    /**
     * orig EmperorScorpion.java:409-419: the attack target set by being hurt (:391, a mob
     * attacker; port {@link #hurt} and {@code HurtByTargetGoal}, orig :71) is read first
     * (:409), dropped once dead (:410-413) or on a 1-in-100 roll (:414-416 — rolled here, inside
     * the pass, and final: {@code RevengeGoal.release}, ENT-S-129); only without one does the scan run (:417-419), and
     * its pick was acted on for that tick alone — a candidate that had left the 24/6/24 box
     * or line of sight was simply not found next time (:440-442, setAttacking(0)). The
     * port's single target slot feeds the melee goal, so the scan's own pick is re-derived
     * on every cadence tick (replaced, or cleared when the scan comes back empty), while a
     * target set by any other path is kept, as the original kept its attack target.
     * ENT-S-108.
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :409 getAttackTarget
        if (current != null && !current.isAlive()) {               // orig :410-413
            this.setTarget(null);
            current = null;
        }
        if (this.random.nextInt(100) == 0 && current != null && current != this.scanPick) { // orig :414-416 — `nextInt(100) == 0 → setAttackTarget(null)`: the roll spent every pass (RNG parity), after the read, so the cleared target is still this pass's `e`; it acts on the stored attack target alone — the scan's pick was never stored, so with the scan's own pick in the slot orig's field was empty and the clear a no-op (ENT-S-129)
            this.setTarget(null);
            this.revengeGoal.release();                            // 1.7.10's task ended on a nulled attack target; vanilla's TargetGoal would re-assert its memory (ENT-S-129)
            return;                                                // the pass acts no further this pass, as orig's `e` stayed read (the goal stops on its next cleanup pass)
        }
        if (current != null && current != this.scanPick) return;   // orig :417: the attack target stands
        LivingEntity pick = this.findSomethingToAttack();           // orig :418
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
     * orig EmperorScorpion.java:71 {@code EntityAIHurtByTarget(this, false)} — the revenge task whose attack target the pass read
     * ahead of the scan (:409) and released on its roll (:414-416); 1.7.10's task ended when its attack target
     * was nulled ({@code EntityAITarget.continueExecuting}), where vanilla's {@code TargetGoal} re-asserts its own memory
     * into an emptied slot — so the pass's release also drops that memory ({@link #release}). The hold itself stays
     * vanilla's. ENT-S-129.
     */
    private final class RevengeGoal extends HurtByTargetGoal {
        RevengeGoal() {
            super(EntityEmperorScorpion.this);
        }

        /** orig :414-416 {@code setAttackTarget(null)} ended the task: the goal's memory goes with the slot. */
        void release() {
            this.targetMob = null;
        }
    }

    /**
     * orig EmperorScorpion.java:503-519 {@code findSomethingToAttack}: nothing under
     * PlayNicely (:504-506); every {@code EntityLivingBase} whose box meets the hunter's box
     * grown by 24/6/24 (:507, {@code getEntitiesWithinAABB} — players included, itself
     * included); sorted by the {@link GenericTargetSorter} (:508); the first the filter
     * accepts wins (:509-517), else null (:518). {@link TargetSelection#firstMatch} is that
     * sort-and-loop, stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :504-506
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(24.0, 6.0, 24.0));                  // orig :507
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :508-518
    }

    /**
     * orig EmperorScorpion.java:460-501 {@code isSuitableTarget}, in the original's order:
     * null / self / dead (:461-469), line of sight (:470-472), the shared ignore screen
     * (:473-475, ENT-S-106 — here after the sight check), then the species chain —
     * EntityEnderman (:476-478), EnderKnight (:479-481), EnderReaper (:482-484),
     * EntityCreeper (:485-487), Scorpion (:488-490), EmperorScorpion (:491-493) — and the
     * player branch, creative refused (:494-499, {@code isCreativeMode} =
     * {@code Abilities.instabuild}); everything else that lives is prey (:500). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :461-469
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :470-472
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :473-475
        if (target instanceof EnderMan) return false;                               // orig :476-478 EntityEnderman
        if (target instanceof EnderKnight) return false;                            // orig :479-481
        if (target instanceof EnderReaper) return false;                            // orig :482-484
        if (target instanceof Creeper) return false;                                // orig :485-487
        if (target instanceof EntityScorpion) return false;                         // orig :488-490 Scorpion
        if (target instanceof EntityEmperorScorpion) return false;                  // orig :491-493
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :494-499
        return true;                                                                // orig :500
    }

    // Death drops are fully data-driven via loot_table/entities/emperor_scorpion.json
    // (orig EmperorScorpion.java:181-347: scale, painting, 4-8 obsidian,
    // 4-11 raw beef, 1-5 rolls of the d20 Ultimate/Diamond gear table).

    /** orig EmperorScorpion.java:529-559 — combined scan of x/z -2..+1, y +2..+4: own spawner anywhere in the box passes, any non-air block fails; then darkness; night; y>=50; no other EmperorScorpion within 20/6/20. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        net.minecraft.core.BlockPos feet = this.blockPosition();
        for (int dz = -2; dz <= 1; dz++) {
            for (int dx = -2; dx <= 1; dx++) {
                for (int dy = 2; dy <= 4; dy++) {
                    net.minecraft.core.BlockPos p = feet.offset(dx, dy, dz);
                    if (OriginalSpawnGates.isOwnSpawner(this, level, p)) return true;
                    if (!level.getBlockState(p).isAir()) return false;
                }
            }
        }
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 50.0) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityEmperorScorpion.class, 20.0, 6.0, 20.0);
    }
}
