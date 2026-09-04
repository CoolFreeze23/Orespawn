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

    /** orig Nastysaurus.java:58 — the revenge task, holding {@code rt} by rt's rule (see {@link RevengeGoal}); assigned in registerGoals (the Mob constructor). ENT-S-129. */
    private RevengeGoal revengeGoal;

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
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal); // orig Nastysaurus.java:58 — EntityAIHurtByTarget(this, false): the port's store of rt (:199-202), held by rt's rule (ENT-S-129)
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
     * other path is kept, as the original kept {@code rt}. ENT-S-129: the 1-in-250 drop of :219 is
     * rolled here, inside the pass, on the revenge occupant alone, and is final ({@code RevengeGoal
     * .release}); the out-of-sight skip of :223-225 lets the scan's pick take the slot for the
     * goal while rt is hidden, the revenge goal restoring rt once the slot is empty again (the
     * scan itself is PlayNicely-gated, :279-281). ENT-S-108. ENT-S-115: the PlayNicely blanking of :215-217
     * is transcribed on the pass's copy of a foreign occupant only — orig's {@code rt} was never
     * the scan's pick — so a revenge target's dead-drop is skipped, the (gated) scan runs, the
     * slot is left as orig left {@code rt} and the scan's bookkeeping claims nothing it did not
     * set; the scan's own pick is not blanked, runs on to the gated scan and is cleared, as it
     * was at HEAD and as orig stood down (:240-242). The melee goal's own reading of a stored revenge
     * target under the flag is answered by its per-preset stand-down ({@code Presets.nastysaurus}, ENT-S-129).
     */
    private void selectTarget() {
        LivingEntity current = this.getTarget();                   // orig :214
        if (OreSpawnConfig.PLAY_NICELY.get() && current != this.scanPick) current = null; // orig :215-217 — `e = rt; e = null`: only a foreign occupant (orig rt, never the scan's pick) is blanked for the pass, the slot untouched; the scan's own pick runs on to the gated scan and is cleared as at HEAD (ENT-S-115, refuter B1)
        LivingEntity rt = this.revengeGoal.held();
        if (rt != null && rt != current && !OreSpawnConfig.PLAY_NICELY.get()) { // orig :218 — `e = rt` while the scan's pick occupies the slot: rt is still rolled and re-taken every pass (ENT-S-129 refuter A)
            if (!rt.isAlive() || this.random.nextInt(250) == 1) {        // orig :219-221 — the dead test and the 1-in-250 rolled on rt every pass; memory only, the slot keeps the pick
                this.revengeGoal.release();
            } else if (this.getSensing().hasLineOfSight(rt)) {             // orig :223-227 — rt visible again takes the pass ahead of the scan: the slot changes occupant (the mark clears)
                this.setTarget(rt);
                return;
            }
        }
        boolean skipped = false;
        if (current != null && current != this.scanPick) {         // orig :218 — `if (e != null)`: rt, the revenge occupant, never the scan's pick
            if (!current.isAlive() || this.random.nextInt(250) == 1) { // orig :219 — `e.isDead || nextInt(250) == 1`: the 1-in-250 rolled inside the 1-in-5 pass, on rt alone (ENT-S-129)
                this.setTarget(null);                              // orig :220-221 — e = null; rt = null
                this.revengeGoal.release();                        // rt's memory in the revenge goal goes with the slot, else vanilla's TargetGoal re-asserts it on the next cleanup pass (ENT-S-129)
                current = null;
            } else if (!this.getSensing().hasLineOfSight(current)) { // orig :223-225 — rt out of sight is skipped for the pass and KEPT: the scan runs, its pick (if any) takes the slot for the goal, and the revenge goal restores rt once the slot is empty again (ENT-S-129)
                skipped = true;
                current = null;
            } else {
                return;                                            // orig :227 — `if (e == null)`: with rt in hand the scan does not run
            }
        }
        LivingEntity pick = this.findSomethingToAttack();           // orig :228
        if (pick == null && skipped) return;                       // orig :240-242 — nothing found while rt was out of sight: the pass stood down; rt stays in the slot, which the goal keeps chasing (disclosed, ENT-S-129)
        if (pick != current) super.setTarget(pick);                // super: the scan's own set keeps its ownership
        // Re-read the slot rather than trusting `pick`: a LivingChangeTargetEvent handler may
        // have substituted or cancelled the set, and a stale scanPick would stall the scan
        // (ENT-S-108 refuter hardening, 2026-09-04). A pass that set nothing — both null, the
        // PlayNicely-blanked pass of orig :215-217 — claims nothing: the slot may still hold the
        // revenge target the pass never touched (ENT-S-115).
        if (pick != null || current != null) this.scanPick = this.getTarget();
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
     * orig Nastysaurus.java:44 {@code rt} — stored by :199-202 (any living attacker), consumed by the pass alone
     * (:214-225) and held by rt's rule: until dead or the pass's roll (:219-221), through sight loss (:223-225
     * skips the pass and keeps rt), at any range. The port's revenge store is vanilla's {@code lastHurtByMob} through
     * this goal's start (orig :58's own {@code EntityAIHurtByTarget} set an attack target nothing read; 1.7.10's task
     * ended when that target was nulled — {@code EntityAITarget.continueExecuting}), so its hold replaces
     * {@code TargetGoal.canContinueToUse}'s: no FOLLOW_RANGE, no unseen-ticks memory; vanilla's re-set of the slot
     * (a same-entity re-assert, which keeps the scan's mark) and its fallback to the goal's own memory when the
     * slot is empty (rt restored after a pass that acted on the scan's pick while rt was out of sight) are kept,
     * as is vanilla's {@code canAttack} screen; the pass's {@link #release} is final. ENT-S-129.
     */
    private final class RevengeGoal extends HurtByTargetGoal {
        RevengeGoal() {
            super(Nastysaurus.this);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity held = Nastysaurus.this.getTarget();
            if (held == null) held = this.targetMob;
            if (held == null || !held.isAlive()) return false; // orig :219 — rt dropped dead
            if (!Nastysaurus.this.canAttack(held)) return false; // vanilla's screen (a creative, spectator or Peaceful player), kept
            if (Nastysaurus.this.getTeam() != null && held.getTeam() == Nastysaurus.this.getTeam()) return false; // vanilla's team screen, kept
            Nastysaurus.this.setTarget(held);
            return true;
        }

        /** The goal's own memory of rt, for the pass's rt reads. */
        @Nullable
        LivingEntity held() {
            return this.targetMob;
        }

        /** orig :221 {@code rt = null}: the goal's memory goes with the slot, so nothing re-asserts it. */
        void release() {
            this.targetMob = null;
        }
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
        boolean ret = super.hurt(source, amount);
        // orig Nastysaurus.java:199-202 — the attacker becomes rt, read ahead of the scan from the next pass on (:214):
        // the scan's mark on a pick that turned on it ends here, exactly when this hit stores the attacker in the port —
        // super.hurt recording it as lastHurtByMob on this tick, the revenge goal's start; a hit that stores nothing
        // keeps the pick transient (ENT-S-129, the ownership convention)
        Entity attacker = source.getEntity();
        if (attacker != null && attacker == this.scanPick && this.getLastHurtByMob() == attacker
                && this.getLastHurtByMobTimestamp() == this.tickCount) this.scanPick = null;
        return ret;
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
