package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModSounds;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

public class TRex extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TRex.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.38f;

    /**
     * orig TRex.java:40 {@code TargetSorter}, :50 {@code new GenericTargetSorter(this)} — the
     * shared weighted-distance order (creepers halved, big silhouettes first) the scan sorts
     * its candidates by (:255). ENT-S-108.
     */
    private final GenericTargetSorter targetSorter = new GenericTargetSorter(this);

    /**
     * The last pick {@link #selectTarget} handed to the target slot. 1.7.10 stored the scan's
     * pick nowhere — only the revenge target {@code rt} persisted (orig TRex.java:42, set at
     * :171 by any living attacker); the pick was acted on for that tick and re-derived on the
     * next (:197-199) — so the port re-runs the scan whenever the slot still holds its own
     * pick, and leaves a target set by any other path alone (see {@link #setTarget}).
     * ENT-S-108.
     */
    @Nullable
    private LivingEntity scanPick;

    public TRex(EntityType<? extends TRex> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 150;
    }

    // AI mirrors 1.7.10 TRex#func_70619_bc: random-cadence swings with the
    // same outer/inner nextInt dice. Revenge target is now handled by the
    // standard HurtByTargetGoal; proactive target acquisition is the original's
    // 1-in-5 20x6x20 EntityLivingBase box scan (orig :182,:254), restored in
    // customServerAiStep / findSomethingToAttack (ENT-S-108) — the port's
    // players-only NearestAttackableTargetGoal is gone.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DinosaurMeleeAttackGoal(this, this::setAttacking,
                DinosaurMeleeAttackGoal.Presets.trex()));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /**
     * orig TRex.java:177-214 {@code updateAITasks}: nothing while dead (:178-180), super
     * (:181), then on the nextInt(5)==1 tick (:182) the target selection (:183-199,
     * {@link #selectTarget}); the rest of that block (:200-212: look, reach (4 + w/2)^2, the
     * nextInt(4)==0 || nextInt(5)==1 swing, the chase at 1.25, setAttacking) is
     * {@code DinosaurMeleeAttackGoal.Presets.trex()}, fed through the target slot. ENT-S-108.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;            // orig TRex.java:178-180
        super.customServerAiStep();              // orig :181
        if (this.random.nextInt(5) == 1) {       // orig :182
            this.selectTarget();                 // orig :183-199
        }
    }

    /**
     * orig TRex.java:183-199: the revenge target {@code rt} (set at :171 by any living
     * attacker; port {@code HurtByTargetGoal}, orig :56) is read first (:184), blanked for
     * the tick under PlayNicely (:185-187), dropped once dead or on a 1-in-200 roll
     * (:189-192) and skipped for the tick while out of sight (:193-195); only without one
     * does the scan run (:197-199), and its pick was acted on for that tick alone — a
     * candidate that had left the 20/6/20 box or line of sight was simply not found next
     * time (:210-212, setAttacking(0)). The port's single target slot feeds the melee goal,
     * so the scan's own pick is re-derived on every cadence tick (replaced, or cleared when
     * the scan comes back empty), a dead target is dropped (:189), and a target set by any
     * other path is kept, as the original kept {@code rt}. The 1-in-200 drop and the
     * out-of-sight skip of the revenge target have no place in a single slot and are the
     * revenge path's own concern (the scan itself is PlayNicely-gated, :251-253). ENT-S-108.
     * ENT-S-115: the PlayNicely blanking of :185-187 is transcribed on the pass's copy of a
     * foreign occupant only — orig's {@code rt} was never the scan's pick — so a revenge
     * target's dead-drop is skipped, the (gated) scan runs, the slot is left as orig left
     * {@code rt} and the scan's bookkeeping claims nothing it did not set; the scan's own pick
     * is not blanked, runs on to the gated scan and is cleared, as it was at HEAD and as orig
     * stood down (:210-212). The melee goal still reads a stored revenge target every tick (the
     * ledger's release-rule row), which a pass-local blanking cannot reach.
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :184
        if (OreSpawnConfig.PLAY_NICELY.get() && current != this.scanPick) current = null; // orig :185-187 — `e = rt; e = null`: only a foreign occupant (orig rt, never the scan's pick) is blanked for the pass, the slot untouched; the scan's own pick runs on to the gated scan and is cleared as at HEAD (ENT-S-115, refuter B1)
        if (current != null && !current.isAlive()) {               // orig :189 isDead
            this.setTarget(null);                                  // orig :190-191
            current = null;
        }
        if (current != null && current != this.scanPick) return;   // orig :197: the revenge target stands
        LivingEntity pick = this.findSomethingToAttack();           // orig :198
        if (pick != current) super.setTarget(pick);                // super: the scan's own set keeps its ownership
        // Re-read the slot rather than trusting `pick`: a LivingChangeTargetEvent handler may
        // have substituted or cancelled the set, and a stale scanPick would stall the scan
        // (ENT-S-108 refuter hardening, 2026-09-04). A pass that set nothing — both null, the
        // PlayNicely-blanked pass of orig :185-187 — claims nothing: the slot may still hold the
        // revenge target the pass never touched (ENT-S-115).
        if (pick != null || current != null) this.scanPick = this.getTarget();
    }

    /** A target set by any other path ends the scan's ownership of the slot; see {@link #selectTarget}. ENT-S-108. */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        this.scanPick = null;
    }

    /**
     * orig TRex.java:250-266 {@code findSomethingToAttack}: nothing under PlayNicely
     * (:251-253); every {@code EntityLivingBase} whose box meets the hunter's box grown by
     * 20/6/20 (:254, {@code getEntitiesWithinAABB} — players included, itself included);
     * sorted by the {@link GenericTargetSorter} (:255); the first the filter accepts wins
     * (:256-264), else null (:265). {@link TargetSelection#firstMatch} is that sort-and-loop,
     * stable ties included (OPT-021). ENT-S-108.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;                        // orig :251-253
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0, 6.0, 20.0));                  // orig :254
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget); // orig :255-265
    }

    /**
     * orig TRex.java:216-248 {@code isSuitableTarget}, in the original's order: null / self /
     * dead (:217-225), the shared ignore screen (:226-228, ENT-S-106), line of sight
     * (:229-231), then the species chain — TRex (:232-234), Cryolophosaurus (:235-237),
     * VelocityRaptor (:238-240) — and the player branch, creative refused (:241-246,
     * {@code isCreativeMode} = {@code Abilities.instabuild}); everything else that lives is
     * prey (:247). ENT-S-108.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;    // orig :217-225
        if (MyUtils.isIgnoreable(target)) return false;                             // orig :226-228
        if (!this.getSensing().hasLineOfSight(target)) return false;                // orig :229-231
        if (target instanceof TRex) return false;                                   // orig :232-234
        if (target instanceof Cryolophosaurus) return false;                        // orig :235-237
        if (target instanceof VelocityRaptor) return false;                         // orig :238-240
        if (target instanceof Player player && player.getAbilities().instabuild) return false; // orig :241-246
        return true;                                                                // orig :247
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6479 — TRex 160 HP / 22 ATK / 14 armor;
        // speed 0.38 matches orig TRex.java:41.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.TREX.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, MobStats.TREX.attackDamage())
                .add(Attributes.ARMOR, MobStats.TREX.armor())
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
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

    public int mygetMaxHealth() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // orig TRex.java:96-101 — "orespawn:trex_living" 1-in-4, else silent.
        if (this.getRandom().nextInt(4) == 0) {
            return ModSounds.TREX_LIVING.get();
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig TRex.java:103-105 — "orespawn:alo_hurt".
        return ModSounds.ALO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        // orig TRex.java:107-109 — "orespawn:trex_death".
        return ModSounds.TREX_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // Death drops are fully data-driven via loot_table/entities/trex.json
    // (orig TRex.java:128-140: trex tooth, painting, 7 raw beef,
    // 2-5x paired uranium+titanium nuggets).

    // 1.7.10 knockback: horizontal push of 1.2 + vertical bump of 0.1
    // (doubled if hitting a player or removed entity).
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 1.2;
                double upwardKnockback = 0.1;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    upwardKnockback *= 2.0;
                }
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Cactus immunity preserved from 1.7.10 (T-Rex leather is too thick).
        if (source.getMsgId().equals("cactus")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    /** orig TRex.java:276-315 — "T. Rex" spawner bypass; darkness; y>=50; night; clear-air column; no other TRex within 24/12/24. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 1, 1, 5, -1, 1)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, TRex.class, 24.0, 12.0, 24.0);
    }
}
