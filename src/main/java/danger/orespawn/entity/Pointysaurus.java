package danger.orespawn.entity;

import danger.orespawn.MobStats;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import javax.annotation.Nullable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.PointysaurusStareGoal;
import danger.orespawn.util.MyUtils;

public class Pointysaurus extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_living"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Pointysaurus.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.35f;

    /** orig Pointysaurus.java:55 — the revenge task, holding {@code rt} by rt's rule (see {@link RevengeGoal}); assigned in registerGoals (the Mob constructor). ENT-S-129. */
    private RevengeGoal revengeGoal;

    public Pointysaurus(EntityType<? extends Pointysaurus> type, Level level) {
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
                DinosaurMeleeAttackGoal.Presets.pointysaurus()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal); // orig Pointysaurus.java:55 — EntityAIHurtByTarget(this, false): the port's store of rt (:168-176), held by rt's rule (ENT-S-129)
        // Phase 10 — Enderman-style eye-contact aggression. Runs at priority 2
        // so it overrides the regular proximity targeting; if you stare at the
        // Pointysaurus it locks onto you. Wider-radius proximity aggro still
        // exists at priority 3 as a fallback so it isn't completely passive
        // when you mind your business but get too close.
        // orig Pointysaurus.java:250-252 — findSomethingToAttack answers null under PlayNicely (PlayNicely != 0);
        // the port's proactive pick is this pair of goals, so the flag is read live in their canUse: neither
        // starts while PlayNicely is on (ENT-S-115).
        // MOD-034 (T9 A3): modern only, a construction snapshot (OreSpawnConfig.pointysaurusStareAggro(),
        // read once here; goals register in the Mob ctor, the BOSS-017 shape — a config change applies
        // to newly spawned Pointysaurs); classic registers no stare goal (orig Pointysaurus.java:50-55 —
        // aggression only from the proximity scan below and from being hit). The ENT-S-115 canUse gate
        // stays on the goal.
        if (OreSpawnConfig.pointysaurusStareAggro()) {
            this.targetSelector.addGoal(2, new PointysaurusStareGoal(this) {
                @Override
                public boolean canUse() {
                    if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Pointysaurus.java:250-252 (ENT-S-115)
                    return super.canUse();
                }
            });
        }
        // Pointysaurus only targets players — it will not attack other mobs.
        // This preserves the 1.7.10 isSuitableTarget filter (rejects all
        // Monster instances) which would otherwise make it pacifist without
        // an explicit Player-only target goal.
        // orig Pointysaurus.java:227-229 — the shared ignore screen, ahead of the
        // species chain and line of sight (:239), as the target goal's predicate (ENT-S-106).
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !MyUtils.isIgnoreable(target)) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Pointysaurus.java:250-252 (ENT-S-115)
                return super.canUse();
            }
        });
    }

    /**
     * orig Pointysaurus.java:177-214 {@code updateAITasks}: nothing while dead (:178-180), super (:181), then on the
     * 1-in-6 tick (:182) the rt half of the target selection (:183-195): rt read (:184), blanked under PlayNicely
     * (:185-187 — the pass reads nothing; the melee goal's own stand-down under the flag is
     * {@code DinosaurMeleeAttackGoal.Presets.pointysaurus}'s), dropped dead or on the 1-in-250 (:189-192), skipped
     * for the pass out of sight (:193-195 — with the port's scan a vanilla goal (T3c) nothing runs in its place:
     * recorded, not transcribed). The scan half (:197-199) is the port's NearestAttackableTargetGoal (T3c) and the
     * melee half (:200-212) the melee goal, fed through the slot. ENT-S-129.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;                              // orig :178-180
        super.customServerAiStep();                                // orig :181
        if (this.random.nextInt(6) == 0) {                         // orig :182
            LivingEntity current = this.getTarget();               // orig :184 — e = rt: the slot's occupant while the revenge goal holds it
            if (OreSpawnConfig.PLAY_NICELY.get()) current = null;  // orig :185-187 — the pass's copy blanked, rt kept (ENT-S-129; the ENT-S-115 deferral)
            if (current != null && current == this.revengeGoal.held()) { // orig :188 — `if (e != null)`: rt, never a scan goal's pick
                if (!current.isAlive() || this.random.nextInt(250) == 1) { // orig :189 — `e.isDead || nextInt(250) == 1`: the 1-in-250 rolled inside the 1-in-6 pass, on rt alone (ENT-S-129)
                    this.setTarget(null);                          // orig :190-191 — e = null; rt = null
                    this.revengeGoal.release();                    // rt's memory in the revenge goal goes with the slot, else vanilla's TargetGoal re-asserts it on the next cleanup pass (ENT-S-129)
                }
            }
        }
    }

    /**
     * orig Pointysaurus.java:42 {@code rt} — stored by :168-176 (any living attacker), consumed by the pass alone
     * (:184-195) and held by rt's rule: until dead or the pass's roll (:189-191), through sight loss (:193-195
     * skips the pass and keeps rt), at any range. The port's revenge store is vanilla's {@code lastHurtByMob} through
     * this goal's start (orig :55's own {@code EntityAIHurtByTarget} set an attack target nothing read; 1.7.10's task
     * ended when that target was nulled — {@code EntityAITarget.continueExecuting}), so its hold replaces
     * {@code TargetGoal.canContinueToUse}'s: no FOLLOW_RANGE, no unseen-ticks memory; vanilla's re-set of the slot
     * (a same-entity re-assert, which keeps the scan's mark) and its fallback to the goal's own memory when the
     * slot is empty (rt restored after a pass that acted on the scan's pick while rt was out of sight) are kept,
     * as is vanilla's {@code canAttack} screen; the pass's {@link #release} is final. ENT-S-129.
     */
    private final class RevengeGoal extends HurtByTargetGoal {
        RevengeGoal() {
            super(Pointysaurus.this);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity held = Pointysaurus.this.getTarget();
            if (held == null) held = this.targetMob;
            if (held == null || !held.isAlive()) return false; // orig :189 — rt dropped dead
            if (!Pointysaurus.this.canAttack(held)) return false; // vanilla's screen (a creative, spectator or Peaceful player), kept
            if (Pointysaurus.this.getTeam() != null && held.getTeam() == Pointysaurus.this.getTeam()) return false; // vanilla's team screen, kept
            Pointysaurus.this.setTarget(held);
            return true;
        }

        /** The goal's own memory of rt, for the pass's rt reads. */
        @Nullable
        LivingEntity held() {
            return this.targetMob;
        }

        /** orig :191 {@code rt = null}: the goal's memory goes with the slot, so nothing re-asserts it. */
        void release() {
            this.targetMob = null;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6472 — Pointysaurus 80 HP / 10 ATK / 16 armor;
        // speed 0.35 matches orig Pointysaurus.java:40.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.POINTYSAURUS.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.POINTYSAURUS.attackDamage())
                .add(Attributes.ARMOR, MobStats.POINTYSAURUS.armor())
                .add(Attributes.FOLLOW_RANGE, 24.0);
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
        return 0.9f;
    }

    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackHorizontal = 0.8;
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

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, (int) (byte) value);
    }

    // Death drops are fully data-driven via loot_table/entities/pointysaurus.json
    // (orig Pointysaurus.java:127-141: 10 leather, 6 raw beef, 6 rotten flesh, 6 string).

    /** orig Pointysaurus.java:275-312 — "Pointysaurus" spawner bypass; darkness; y>=50; night; clear-air column. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        return OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0);
    }
}
