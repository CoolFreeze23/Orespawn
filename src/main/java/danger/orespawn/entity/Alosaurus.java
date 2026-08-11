package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;

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
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;
import danger.orespawn.entity.ai.TargetSelection;

public class Alosaurus extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_living"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Alosaurus.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.35f;
    private final Comparator<Entity> targetSorter;
    private static final double KNOCKBACK_HORIZONTAL = 1.2;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;

    public Alosaurus(EntityType<? extends Alosaurus> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 40;
        // orig Alosaurus.java:39,48 — target priority uses the shared
        // GenericTargetSorter (creepers and large targets outrank nearer
        // small ones), not plain distance (TF-035).
        this.targetSorter = new GenericTargetSorter(this);
    }

    // orig Alosaurus.java:49-54 — Swim @0, MoveThroughVillage @1,
    // WanderALot(16, 1.0) @2, WatchClosest(Player, 8) @3, LookIdle @4,
    // HurtByTarget(false) @1. Combat lived in func_70619_bc, above the whole
    // task list; its accepted mapping (DinosaurMeleeAttackGoal, tuning
    // verbatim from :164-179) takes slot 1 and pushes the rest down one, so
    // the original relative order is preserved. Target acquisition is the
    // original 1-in-5 tick scan in customServerAiStep — the port's always-on
    // NearestAttackableTargetGoal<Player> and pack-alert HurtByTargetGoal
    // were inventions (ENT-A-009): the orig ignores no attacker classes and
    // never calls for help (EntityAIHurtByTarget(this, false), :54).
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DinosaurMeleeAttackGoal(this, this::setAttacking,
                DinosaurMeleeAttackGoal.Presets.alosaurus()));
        // orig Alosaurus.java:50 — EntityAIMoveThroughVillage(1.0, false).
        // Mapping decision as in Alien (ENT-A-003): vanilla
        // MoveThroughVillageGoal is the POI-driven descendant of the removed
        // door-list goal — same speed, day-and-night (orig third arg false),
        // vanilla-standard distanceToPoi 4, no door-breaking.
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 16, 1.0)); // orig :51
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f)); // orig :52
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this)); // orig :53
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); // orig :54
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6473 — Alosaurus 110 HP / 18 ATK / 8 armor;
        // speed 0.35 matches orig Alosaurus.java:40.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ALOSAURUS.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ALOSAURUS.attackDamage())
                .add(Attributes.ARMOR, MobStats.ALOSAURUS.armor())
                .add(Attributes.FOLLOW_RANGE, 32.0);
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

    // Preserves 1.7.10 knockback: angle-based horizontal push + small vertical
    // bump (doubled when hitting a player or a target already flagged removed
    // — the latter matches the original "if (target.isRemoved() ...)" guard).
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double verticalKnockback = KNOCKBACK_VERTICAL;
                float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
                }
                target.push(
                        Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                        verticalKnockback,
                        Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
            }
            return true;
        }
        return false;
    }

    // orig Alosaurus.java:159-179 — combat ran on a 1-in-5 tick roll that
    // re-picked the nearest suitable prey every time (findSomethingToAttack
    // never persisted a target). The pick is handed to
    // DinosaurMeleeAttackGoal (the accepted mapping of the same lines'
    // chase-at-1.25 / reach (4+w/2)^2 / nextInt(4)==0||nextInt(5)==1 swing
    // dice) via setTarget; an empty scan clears the target so prey leaving
    // the 12/5/12 box stops the chase exactly as the orig did (:176-178).
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return; // orig Alosaurus.java:160-162
        super.customServerAiStep();
        if (this.random.nextInt(5) == 0) { // orig Alosaurus.java:164
            LivingEntity prey = this.findSomethingToAttack();
            if (prey != null) {
                this.setTarget(prey);
            } else {
                this.setAttacking(0); // orig Alosaurus.java:177
                this.setTarget(null);
            }
        }
    }

    /** orig Alosaurus.java:182-212 — everything living is prey except the exclusion list. */
    private boolean isSuitableTarget(LivingEntity candidate) {
        if (candidate == null || candidate == this || !candidate.isAlive()) return false;
        if (MyUtils.isIgnoreable(candidate)) return false;               // orig :192
        if (candidate instanceof Alosaurus) return false;                // orig :195
        if (candidate instanceof Cryolophosaurus) return false;          // orig :198
        if (candidate instanceof VelocityRaptor) return false;           // orig :201
        if (!this.getSensing().hasLineOfSight(candidate)) return false;  // orig :204
        if (candidate instanceof Player player) {
            return !player.getAbilities().instabuild;                    // orig :207-210
        }
        return true;                                                     // orig :211
    }

    /** orig Alosaurus.java:214-230 — PlayNicely-gated 12/5/12 box scan, GenericTargetSorter order. */
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig :215-217
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(12.0, 5.0, 12.0)); // orig :218
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        // (was: .sort orig :219)
        return TargetSelection.firstMatch(candidates, this.targetSorter, this::isSuitableTarget);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, (int) (byte) value);
    }

    // Death drops are fully data-driven via loot_table/entities/alosaurus.json
    // (orig Alosaurus.java:126-134: 10 gold nugget, 6 raw beef).

    /** orig Alosaurus.java:240-279 — spawner bypass; darkness; y>=50; night; clear-air column; no other Alosaurus within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, Alosaurus.class, 16.0, 8.0, 16.0);
    }
}
