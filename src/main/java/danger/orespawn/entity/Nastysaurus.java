package danger.orespawn.entity;

import danger.orespawn.MobStats;

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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

public class Nastysaurus extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_living"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Nastysaurus.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.35f;

    /**
     * Per-entity render scratch (orig Nastysaurus.java:43 {@code renderdata = new RenderInfo()},
     * accessor orig Nastysaurus.java:84-86). Mutated client-side by {@code ModelNastysaurus}
     * for the idle jaw-twitch latch (orig ModelNastysaurus.java:479-486: rf1 = last idle
     * phase, ri1 = 1-in-20 reroll); never datawatcher-synced. The original zeroed all eight
     * fields in entityInit (orig :71-82); a fresh instance's defaults are already zero.
     * ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    /**
     * orig Nastysaurus.java:41 {@code TargetSorter}, :52 {@code new GenericTargetSorter(this)}
     * — the shared weighted-distance order (creepers halved, big silhouettes first) the scan
     * sorts its candidates by (:283). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick {@link #selectTarget} handed to the target slot. 1.7.10 stored the scan's
     * pick nowhere — only the revenge target {@code rt} persisted (orig Nastysaurus.java:44,
     * set at :201 by any living attacker); the pick was acted on for that tick and re-derived
     * on the next (:227-229) — so the port re-runs the scan whenever the slot still holds its
     * own pick, and leaves a target set by any other path alone (see {@link #setTarget}).
     * ENT-S-108.
     */
    @Nullable
    private LivingEntity scanPick;

    public Nastysaurus(EntityType<? extends Nastysaurus> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 40;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DinosaurMeleeAttackGoal(this, this::setAttacking,
                DinosaurMeleeAttackGoal.Presets.nastysaurus()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // orig Nastysaurus.java:58 registers no target-search task: prey is found by the
        // 1-in-5 EntityLivingBase box scan of :212-229 / :278-294, restored in
        // customServerAiStep / findSomethingToAttack (ENT-S-108). The port's
        // players-only NearestAttackableTargetGoal is gone.
    }

    /**
     * orig Nastysaurus.java:207-244 {@code updateAITasks}: nothing while dead (:208-210),
     * super (:211), then on the 1-in-5 tick (:212) the target selection (:213-229,
     * {@link #selectTarget}); the rest of that block (:230-242: look, reach (4.5 + w/2)^2,
     * the nextInt(4)==0 || nextInt(5)==1 swing, the chase at 1.25, setAttacking) is
     * {@code DinosaurMeleeAttackGoal.Presets.nastysaurus()}, fed through the target slot.
     * ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;            // orig Nastysaurus.java:208-210
        super.customServerAiStep();              // orig :211
        if (this.random.nextInt(5) == 0) {       // orig :212
            this.selectTarget();                 // orig :213-229
        }
    }

    /**
     * orig Nastysaurus.java:213-229: the revenge target {@code rt} (set at :201 by any living
     * attacker; port {@code HurtByTargetGoal}, orig :58) is read first (:214), blanked for
     * the tick under PlayNicely (:215-217), dropped once dead or on a 1-in-250 roll
     * (:219-222) and skipped for the tick while out of sight (:223-225); only without one
     * does the scan run (:227-229), and its pick was acted on for that tick alone — a
     * candidate that had left the 32/8/32 box or line of sight was simply not found next
     * time (:240-242, setAttacking(0)). The port's single target slot feeds the melee goal,
     * so the scan's own pick is re-derived on every cadence tick (replaced, or cleared when
     * the scan comes back empty), a dead target is dropped (:219), and a target set by any
     * other path is kept, as the original kept {@code rt}. The 1-in-250 drop is the melee
     * goal's {@code forgetTargetRoll}; the PlayNicely blanking and the out-of-sight skip of
     * the revenge target have no place in a single slot and are the revenge path's own
     * concern (the scan itself is PlayNicely-gated, :279-281). ENT-S-108.
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :214
        if (current != null && !current.isAlive()) {               // orig :219 isDead
            this.setTarget(null);                                  // orig :220-221
            current = null;
        }
        if (current != null && current != this.scanPick) return;   // orig :227: the revenge target stands
        LivingEntity pick = this.findSomethingToAttack();           // orig :228
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
     * orig Nastysaurus.java:278-294 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:279-281); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 32/8/32 (:282, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:283); the first the filter accepts wins
     * (:284-292), else null (:293). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :279-281
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(32.0, 8.0, 32.0));                  // orig :282
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :283-293
    }

    /**
     * orig Nastysaurus.java:246-276 {@code isSuitableTarget}, in the original's order: null /
     * self / dead (:247-255), the shared ignore screen (:256-258, ENT-S-106), the species
     * chain — Nastysaurus (:259-261), Cryolophosaurus (:262-264), VelocityRaptor (:265-267) —
     * then line of sight (:268-270), and the player branch, which answers
     * {@code !isCreativeMode} (:271-274, {@code Abilities.instabuild}); everything else that
     * lives is prey (:275). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :247-255
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :256-258
        if (target instanceof Nastysaurus) return false;                            // orig :259-261
        if (target instanceof Cryolophosaurus) return false;                        // orig :262-264
        if (target instanceof VelocityRaptor) return false;                         // orig :265-267
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :268-270
        if (target instanceof Player player) return !player.getAbilities().instabuild; // orig :271-274
        return true;                                                                // orig :275
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6471 — Nastysaurus 200 HP / 32 ATK / 17 armor;
        // speed 0.35 matches orig Nastysaurus.java:42.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.NASTYSAURUS.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.NASTYSAURUS.attackDamage())
                .add(Attributes.ARMOR, MobStats.NASTYSAURUS.armor())
                .add(Attributes.FOLLOW_RANGE, 40.0);
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
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return SND_ALO_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ALO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackHorizontal = 1.2;
                double knockbackVertical = 0.1;
                float pushAngle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    knockbackVertical *= 2.0;
                }
                target.push(Math.cos(pushAngle) * knockbackHorizontal, knockbackVertical, Math.sin(pushAngle) * knockbackHorizontal);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** Mirrors orig Nastysaurus.java:84-86 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, (int) (byte) value);
    }

    // Death drops are fully data-driven via loot_table/entities/nastysaurus.json
    // (orig Nastysaurus.java:156-170: 10 iron ingot, 10 rotten flesh, 10 leather, 10 string).

    /** orig Nastysaurus.java:304-343 — spawner bypass; darkness; y>=50; night; clear air above; no other Nastysaurus within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, Nastysaurus.class, 16.0, 8.0, 16.0);
    }
}
