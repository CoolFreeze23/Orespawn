package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.BugMeleeAttackGoal;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.entity.ai.TargetSelection;

public class EntityScorpion extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SCORPION_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    private static final SoundEvent SND_SCORPION_ATTACK = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_attack"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityScorpion.class, EntityDataSerializers.INT);

    /**
     * Per-entity render scratch (orig Scorpion.java:45 {@code renderdata = new RenderInfo()},
     * re-newed in the ctor at :56, zeroed in entityInit at :75-85, accessor at :101-103).
     * Mutated client-side by {@code ScorpionModel} for the claw/tail selector latch
     * (orig ModelScorpion.java:198-226); never datawatcher-synced. ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    public EntityScorpion(EntityType<? extends EntityScorpion> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Priority 1: execute the melee combat loop when a target exists.
        // This replaces the legacy 1.7.10 inline customServerAiStep attack
        // block while preserving its cadence (nextInt(6) == 0 outer + nested
        // nextInt(5)/nextInt(6)==1 inner swing dice).
        this.goalSelector.addGoal(1, new BugMeleeAttackGoal(
                this, this::setAttacking, BugMeleeAttackGoal.Params.scorpion()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        // orig Scorpion.java:62 — EntityAIHurtByTarget is the only registered target task;
        // everything else the scorpion attacks comes from the findSomethingToAttack() scan
        // in customServerAiStep (ENT-S-002). The port's extra
        // NearestAttackableTargetGoal(Player) was removed: it acquired players out to
        // follow-range 24 with plain-distance priority, where the orig only ever engages
        // what the 8/3/8 sorted scan returns.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6518 — Scorpion 15 HP / 4 ATK / 10 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SCORPION.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SCORPION.attackDamage())
                .add(Attributes.ARMOR, MobStats.SCORPION.armor())
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

    /** Mirrors orig Scorpion.java:101-103 {@code getRenderInfo()}. ENT-S-093. */
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
        return SND_SCORPION_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return; // orig Scorpion.java:171-173 — bail before super when dead
        super.customServerAiStep();
        // orig Scorpion.java:175-192 — one nextInt(6)==0 gate drives the whole combat
        // brain: rescan for prey and engage it. The swing/pursuit half lives in
        // BugMeleeAttackGoal (Params.scorpion(), same nested dice); this half restores
        // the ACQUISITION the port dropped (ENT-S-002): creepers, spiders,
        // VelocityRaptors — and, per the orig fallthrough (:252), any non-Monster
        // living — not just players.
        if (this.random.nextInt(6) == 0) {
            LivingEntity prey = findSomethingToAttack();
            if (prey != null) this.setTarget(prey);
        }
    }

    /**
     * orig Scorpion.java:180-184 — every swing (hit or miss: the func_70652_k return is
     * ignored) rolls nextInt(3)==1 server-side and plays orespawn:scorpion_attack AT THE
     * TARGET's position, vol 0.75 / pitch 1.5 (ENT-S-003).
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (this.random.nextInt(3) == 1) {
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SND_SCORPION_ATTACK,
                    this.getSoundSource(), 0.75f, 1.5f);
        }
        return hit;
    }

    /**
     * orig Scorpion.java:195-201 — "cactus" damage is discarded before super is ever
     * consulted (ret stays false), so scorpions roam desert cacti unharmed (ENT-S-003).
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.CACTUS)) return false;
        return super.hurt(source, amount);
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig Scorpion.java:256-258
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(8.0, 3.0, 8.0)); // orig :259 — func_72314_b(8,3,8)
        // TF-035: orig Scorpion.java:44 (TargetSorter field), :55 (ctor), :260
        // (Collections.sort) — candidates rank by GenericTargetSorter
        // (creeper-halved / big-silhouette-first), not plain distance.
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, new GenericTargetSorter(this), this::isSuitableTarget);
    }

    /**
     * orig Scorpion.java:203-253 — the prey ladder, in original order: after the
     * null/self/dead/ignoreable/line-of-sight screens, Ghost and GhostSkelly are excluded
     * (redundantly — MyUtils.isIgnoreable already covers both; kept for structural
     * parity), then VelocityRaptor / spiders / creepers are prey, fellow scorpions and
     * all other Monsters are not, creative players are excluded, and EVERYTHING else
     * that lives — animals, villagers, survival players — falls through to true (:252).
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false; // :204-212
        if (MyUtils.isIgnoreable(target)) return false;                          // :213-215
        if (!this.getSensing().hasLineOfSight(target)) return false;             // :216-218
        if (target instanceof Ghost) return false;                               // :219-221
        if (target instanceof GhostSkelly) return false;                         // :222-224
        if (target instanceof VelocityRaptor) return true;                       // :225-227
        // :228-233 — EntitySpider true, then an unreachable EntityCaveSpider branch
        // (CaveSpider extends Spider in 1.7.10 and 1.21.1 alike); one check covers both.
        if (target instanceof Spider) return true;
        if (target instanceof EntityScorpion) return false;                      // :234-236
        if (target instanceof EntityEmperorScorpion) return false;               // :237-239
        if (target instanceof Creeper) return true;                              // :240-242
        if (target instanceof Monster) return false;                             // :243-245 (EntityMob)
        if (target instanceof Player p && p.getAbilities().instabuild) return false; // :246-251
        return true;                                                             // :252
    }

    /** orig Scorpion.java:281-299 — spawner bypass; darkness; then night OR y<=50 (daytime cave spawns allowed). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return !OriginalSpawnGates.isDaytime(level) || this.getY() <= 50.0;
    }
}
