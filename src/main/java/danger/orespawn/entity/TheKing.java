package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.ModSounds;
import danger.orespawn.util.MyUtils;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * The King — a flying, multi-region boss.
 *
 * <h2>Multi-part framework (1.7.10 → 1.21.1 port)</h2>
 *
 * <p>In the 1.7.10 original ({@code reference_1_7_10_source/sources/danger/orespawn/TheKing.java}),
 * The King was a single {@code EntityMob} with a giant 22×24 AABB and a
 * <em>sidecar</em> {@code KingHead} entity spawned 20 blocks above. The
 * head ran its own per-tick {@code func_70071_h_} loop, did an AABB search
 * to locate the parent, and forwarded damage by invoking the parent's
 * {@code attackEntityFrom}. This made the whole boss behave like a
 * 2-region hitbox pasted together at runtime.</p>
 *
 * <p>NeoForge 1.21.1 replaces that dual-entity hack with a proper
 * {@link PartEntity} array ({@link OreSpawnPartEntity}): five named
 * regions — body, head, left wing, right wing, tail — are owned by this
 * single {@code TheKing} and positioned every tick with offsets rotated by
 * {@link #yBodyRot}. Damage flows back through
 * {@link #hurtFromPart(OreSpawnPartEntity, DamageSource, float)} with
 * per-region multipliers (head = full, body = ½, everything else = ¼ +1).</p>
 *
 * <p>The legacy {@code KingHead} entity type is <i>still registered</i> for
 * save-file backward compatibility and is still spawned by the AI path
 * below — see the comment near {@link ModEntities#KING_HEAD}. Future work
 * should delete the sidecar spawn once multi-part hitboxes are proven in
 * playtesting.</p>
 *
 * <h2>Key lifecycle hooks</h2>
 * <ul>
 *   <li>{@link #TheKing(EntityType, Level)} constructs all five parts. Each
 *       part construction increments {@code Entity.ENTITY_COUNTER}.</li>
 *   <li>{@link #setId(int)} reassigns the parts to a contiguous ID block
 *       starting at {@code id + 1}. This mirrors vanilla
 *       {@code EnderDragon.setId} so the client can correlate part-hit
 *       packets with the owning boss.</li>
 *   <li>{@link #getParts()} returns the stable {@link PartEntity} array
 *       ({@code allParts}) — it must be the same reference every call
 *       because the world stores it for hit-testing.</li>
 *   <li>{@link #tick()} snapshots each part's previous position, advances
 *       the parent via {@code super.tick()}, repositions the parts, then
 *       writes the snapshots back to the parts' {@code xo/yo/zo} /
 *       {@code xOld/yOld/zOld} fields so the client renderer interpolates
 *       instead of teleporting.</li>
 * </ul>
 *
 * @see OreSpawnPartEntity for the part implementation and the full 1.7.10
 *   paradigm-shift commentary.
 */
public class TheKing extends Monster implements OreSpawnPartEntity.MultipartBoss {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_IS_END =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH_VALUE = 6000;
    private static final double ATTACK_DAMAGE_VALUE = 250.0;
    private static final int DEFENSE_VALUE = 12;
    private static final double MOVE_SPEED_VALUE = 0.62;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("The King"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private BlockPos currentFlightTarget = null;
    private final Comparator<Entity> targetSorter;
    private LivingEntity revengeTarget = null;
    private double attackDamage = ATTACK_DAMAGE_VALUE;
    private int hurtCooldown = 0;
    private int homeX = 0;
    private int homeZ = 0;
    private int fireballStreamCount = 0;
    private int lightningStreamCount = 0;
    private int iceStreamCount = 0;
    private int ticker = 0;
    private int playerHitCount = 0;
    private int backoffTimer = 0;
    private int guardModeTimer = 0;
    private volatile int headEntityFound = 0;
    private int wingSoundTimer = 0;
    private int largeEntityDetected = 0;
    private int isEnd = 0;
    private int endCounter = 0;

    private final OreSpawnPartEntity<TheKing> bodyPart;
    private final OreSpawnPartEntity<TheKing> headPart;
    private final OreSpawnPartEntity<TheKing> wingLeft;
    private final OreSpawnPartEntity<TheKing> wingRight;
    private final OreSpawnPartEntity<TheKing> tail;
    private final PartEntity<?>[] allParts;

    public TheKing(EntityType<? extends TheKing> type, Level level) {
        super(type, level);
        this.xpReward = 25000;
        this.noCulling = true;
        this.noPhysics = true;
        this.setNoGravity(true);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);

        this.bodyPart  = new OreSpawnPartEntity<>(this, "body",  5.0f, 5.0f);
        this.headPart  = new OreSpawnPartEntity<>(this, "head",  3.0f, 3.0f);
        this.wingLeft  = new OreSpawnPartEntity<>(this, "wingL", 5.0f, 2.0f);
        this.wingRight = new OreSpawnPartEntity<>(this, "wingR", 5.0f, 2.0f);
        this.tail      = new OreSpawnPartEntity<>(this, "tail",  3.0f, 3.0f);
        this.allParts = new PartEntity<?>[]{ bodyPart, headPart, wingLeft, wingRight, tail };
    }

    @Override
    protected void registerGoals() {
        // Goal-priority ordering: lower number = higher priority.
        //
        // The 1.7.10 original packed flight pathing, target acquisition,
        // elemental ranged streams, melee, and the "Prepare to die!"
        // dialogue into a single ~250-line func_70030_z_ body. We preserve
        // those mechanics byte-for-byte in the helper methods
        // aiStepEndGameDialogue() and aiStepPrimary() on this class, but
        // wrap them in proper Goal subclasses so the engine's mutex system
        // can arbitrate them cleanly:
        //
        //   KingEndGameGoal (priority 0, MOVE|LOOK|JUMP): owns the full
        //     mutex during the end-phase-1 dialogue cutscene. While it is
        //     active, KingPrimaryGoal cannot tick — the boss is frozen in
        //     place and the player is pinned to face it. Without this
        //     mutex lock the flight-motion lerp would still run every
        //     tick and the cutscene wouldn't read as a cutscene.
        //
        //   KingPrimaryGoal (priority 1, MOVE|LOOK): the "live" behaviour
        //     goal. Handles flight wandering, guard-mode leashing, target
        //     acquisition (throttled to the attack-chance RNG gate),
        //     melee + jump damage, the three elemental ranged streams
        //     (fireball / thunder / ice), and the enraged-phase purple-
        //     power bomb trail. Effectively a single fat goal because its
        //     sub-behaviours are tightly coupled to a shared flight-target
        //     state — splitting them further would require more
        //     synchronisation code than just keeping them together.
        //
        //   FloatGoal (priority 2): prevents The King from sinking in
        //     water mid-flight (rare but can happen at spawn).
        //
        //   RandomLookAroundGoal (priority 3): idle head-turning when no
        //     target is engaged. Does not conflict with the LOOK flag on
        //     KingPrimaryGoal because GoalSelector's mutex check runs at
        //     goal-activation time, not every tick.
        //
        //   HurtByTargetGoal (target selector, priority 1): standard
        //     vanilla revenge behaviour. Feeds into the revengeTarget
        //     field which KingPrimaryGoal reads.
        this.goalSelector.addGoal(0, new danger.orespawn.entity.ai.KingEndGameGoal(this));
        this.goalSelector.addGoal(1, new danger.orespawn.entity.ai.KingPrimaryGoal(this));
        this.goalSelector.addGoal(2, new FloatGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /**
     * Accessor for goals / external callers — returns the current end-phase
     * state (0=normal, 1=dialogue cutscene, 2=enraged). Exposed because
     * {@link danger.orespawn.entity.ai.KingEndGameGoal#canUse()} is in a
     * different package and needs to read this gating flag.
     */
    public int getEndPhase() {
        return this.isEnd;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH_VALUE)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED_VALUE)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_VALUE)
                .add(Attributes.FOLLOW_RANGE, 128.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, DEFENSE_VALUE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_IS_END, 0);
    }

    public int mygetMaxHealth() {
        return MAX_HEALTH_VALUE;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    public int getIsEnd() {
        return this.entityData.get(DATA_IS_END);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 1.35f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KING_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KING_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TREX_DEATH.get();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
    }

    // ---- Tick / AI ----

    /**
     * Advertises to NeoForge that this entity owns one or more
     * {@link PartEntity} children. Without this the parts returned by
     * {@link #getParts()} are ignored by hit-detection.
     */
    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    /**
     * Returns the stable array of child parts. The world caches this
     * reference for hit-testing, so we must return the SAME array object
     * every call — never rebuild it on the fly.
     */
    @Override
    public PartEntity<?>[] getParts() {
        return this.allParts;
    }

    /**
     * Reserves a contiguous block of entity IDs for the parts so the client
     * can correlate part-hit packets with the owning boss. Mirrors vanilla
     * {@code EnderDragon.setId} — if parts had non-contiguous IDs, the
     * client-side part lookup in {@code MultiPlayerLevel} would fail and
     * hits would register as "the parent's root AABB was struck", losing
     * the per-part damage multipliers.
     */
    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < allParts.length; i++) {
            allParts[i].setId(id + i + 1);
        }
    }

    /**
     * The parent's own bounding box is invisible to ray-tracing — players
     * must hit a {@link OreSpawnPartEntity} to damage The King. This is
     * the 1.21.1 analogue of 1.7.10's {@code setSize(22, 24)} trick where
     * the giant root AABB doubled as both visual bounds and hit area.
     */
    @Override
    public boolean isPickable() {
        return false;
    }

    /**
     * Per-part damage routing. Head = full damage (weak point), body = ½,
     * everything else = ¼ + 1 flat.
     *
     * <p>1.7.10 parallel: {@code TheKing.func_70097_a} couldn't distinguish
     * which region was hit — every hit applied the same damage because the
     * sidecar {@code KingHead} just called {@code attackEntityFrom} with
     * the raw amount. This multiplier table is pure gain from the port.</p>
     */
    @Override
    public boolean hurtFromPart(OreSpawnPartEntity<?> part, DamageSource source, float amount) {
        String partName = part.getPartName();
        float multiplied = switch (partName) {
            case "head" -> amount;
            case "body" -> amount * 0.5f;
            default -> amount * 0.25f + 1.0f;
        };
        return this.hurt(source, multiplied);
    }

    /**
     * Places a sub-part at {@code (offsetX, offsetY, offsetZ)} relative to
     * this entity, with the horizontal components rotated by this entity's
     * body yaw. Matches the projection used by the client-side renderer so
     * the hitbox stays aligned with what the player sees.
     *
     * <p>Intentionally uses {@code yBodyRot} (not {@code getYRot()}) so a
     * yaw-locked head-tracking animation doesn't slosh the hitboxes around
     * independently of the body.</p>
     */
    private void positionPart(OreSpawnPartEntity<TheKing> part, double offsetX, double offsetY, double offsetZ) {
        float yawRad = this.yBodyRot * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yawRad);
        double cos = Mth.cos(yawRad);
        double rx = offsetX * cos - offsetZ * sin;
        double rz = offsetX * sin + offsetZ * cos;
        part.setPos(this.getX() + rx, this.getY() + offsetY, this.getZ() + rz);
    }

    @Override
    public void tick() {
        // ── Step 1: snapshot previous-tick positions ──
        // We capture before super.tick() so the values are still the
        // positions that were computed last tick (which themselves became
        // the "old" positions at the end of last tick's repositioning).
        Vec3[] oldPos = new Vec3[allParts.length];
        for (int i = 0; i < allParts.length; i++) {
            oldPos[i] = new Vec3(allParts[i].getX(), allParts[i].getY(), allParts[i].getZ());
        }

        // ── Step 2: advance the parent ──
        // super.tick() updates yBodyRot, getX/Y/Z, hurt timers, etc. The
        // repositioning below depends on yBodyRot being current, so do this
        // FIRST and then derive part positions.
        super.tick();

        // ── Step 3: position each part relative to the parent's new pose ──
        // Offsets are in world units (blocks) and are rotated by yBodyRot
        // inside positionPart(). Numbers chosen to roughly match the visual
        // silhouette of the 1.7.10 render model — body at +6 Y, head +11 Y
        // and 5 blocks forward (negative Z), wings 8 blocks left/right at
        // +7 Y, tail 6 blocks behind at +4 Y.
        positionPart(bodyPart,    0.0,  6.0,   0.0);
        positionPart(headPart,    0.0, 11.0,  -5.0);
        positionPart(wingLeft,  -8.0,   7.0,   0.0);
        positionPart(wingRight,  8.0,   7.0,   0.0);
        positionPart(tail,       0.0,   4.0,   6.0);

        // ── Step 4: write the snapshots back as the parts' "old" pose ──
        // The client renderer interpolates between oldPos and pos using the
        // partial-tick timer. If we skipped this, parts would teleport on
        // every tick because their "old" and "new" would be identical.
        // xOld/yOld/zOld are the NeoForge-exposed fields; xo/yo/zo are the
        // legacy mappings — set both to avoid any discrepancy.
        for (int i = 0; i < allParts.length; i++) {
            allParts[i].xo = oldPos[i].x;
            allParts[i].yo = oldPos[i].y;
            allParts[i].zo = oldPos[i].z;
            allParts[i].xOld = oldPos[i].x;
            allParts[i].yOld = oldPos[i].y;
            allParts[i].zOld = oldPos[i].z;
        }

        this.wingSoundTimer++;
        if (this.wingSoundTimer > 30) {
            if (!this.level().isClientSide) {
                this.playSound(ModSounds.MOTHRAWINGS1.get(), 1.75f, 0.75f);
            }
            this.wingSoundTimer = 0;
        }

        this.noPhysics = true;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() * 2 / 3)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 2;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 2)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 4;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 4)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 8;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 8)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 16;
        }

        if (this.level().isClientSide) {
            this.isEnd = this.entityData.get(DATA_IS_END);
            if (this.isEnd != 0 && this.getRandom().nextInt(3) == 1) {
                float particleOffset = 7.0f;
                Vec3 mot = this.getDeltaMovement();
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            this.getX() - (double) particleOffset * Math.sin(Math.toRadians(this.getYRot())),
                            this.getY() + 14.0,
                            this.getZ() + (double) particleOffset * Math.cos(Math.toRadians(this.getYRot())),
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0 + mot.x * 6.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0 + mot.z * 6.0);
                }
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        // Post-goal bookkeeping core. The three behavioural blocks that used
        // to live here — dialogue cutscene, flight pathing, and combat —
        // have been extracted into public helpers (aiStepEndGameDialogue,
        // aiStepFlight, aiStepCombat) invoked by the Goal classes registered
        // in registerGoals(). GoalSelector ticks them in priority order
        // BEFORE this method runs (see Mob.serverAiStep()), so by the time
        // we get here the behavioural work for this tick is already done.
        //
        // What remains here is pure state hygiene that always runs:
        //   - Boss-bar progress sync
        //   - entityData sync (PLAY_NICELY + IS_END) for client rendering
        //   - Enraged-phase config overrides (when isEnd == 2)
        //   - Hurt-cooldown decrement
        //   - Home-point init on first tick
        //   - Tick counter + ranged-attack ammunition refills
        //   - Passive healing
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isRemoved()) return;
        super.customServerAiStep();

        this.entityData.set(DATA_IS_END, this.isEnd);
        // Wire PLAY_NICELY config: when true, The King starts in peaceful disposition
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.PLAY_NICELY.get() ? 1 : 0);

        // ---- End-game phase 2: enraged config overrides ----
        // Phase 1 (dialogue) is fully owned by KingEndGameGoal which freezes
        // all movement via its MOVE|LOOK|JUMP mutex; we do not touch it here.
        // Phase 2 is a permanent config override — maxed ammunition, shorter
        // cooldowns, healing boost — that enhances the flight+attack goals.
        if (this.isEnd == 2) {
            this.hurtCooldown = 10;
            this.playerHitCount = 0;
            this.fireballStreamCount = 10;
            this.lightningStreamCount = 10;
            this.iceStreamCount = 10;
            this.guardModeTimer = 0;
            this.largeEntityDetected = 1;
            if (this.backoffTimer > 0) this.backoffTimer--;
        }

        if (this.hurtCooldown > 0) this.hurtCooldown--;

        // First-tick home-point init — the point The King defends with the
        // guard-mode leash in KingFlightGoal / KingAttackGoal.
        if ((this.homeX == 0 && this.homeZ == 0) || this.guardModeTimer == 0) {
            this.homeX = (int) this.getX();
            this.homeZ = (int) this.getZ();
        }

        this.ticker++;
        if (this.ticker > 30000) this.ticker = 0;
        if (this.ticker % 80 == 0) this.fireballStreamCount = 10;
        if (this.ticker % 90 == 0) this.lightningStreamCount = 5;
        if (this.ticker % 70 == 0) this.iceStreamCount = 8;
        if (this.backoffTimer > 0) this.backoffTimer--;

        this.noPhysics = true;

        // Passive healing — a slow regen tick plus a massive top-up when a
        // large entity (Mobzilla, dragons) is detected. The 2000-HP floor
        // prevents cheese strategies from nibbling away the opening health
        // pool without triggering the real phase multipliers.
        if (this.getRandom().nextInt(30) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(5.0f);
            if (this.largeEntityDetected != 0) {
                this.heal(200.0f);
            }
        }
        if (this.playerHitCount < 10 && this.getHealth() < 2000.0f) {
            this.heal(2000.0f - this.getHealth());
        }
    }

    // ─── AI STEP HELPERS ───────────────────────────────────────────────────
    // These three methods used to form one monolithic ~250-line block inside
    // customServerAiStep. They are now invoked by dedicated Goal classes in
    // danger.orespawn.entity.ai so that:
    //   (a) the GoalSelector's priority/mutex system can arbitrate between
    //       dialogue, combat, and flight cleanly (instead of the old
    //       if/else-if chain that coupled them);
    //   (b) each concern is individually testable and replaceable;
    //   (c) future additions (e.g. a summon-minion goal) slot in as new
    //       Goal classes without touching this class.
    // They are public because the goals live in a sibling package; they are
    // not intended to be called from anywhere else.

    /**
     * Handles the "Prepare to die!" end-phase 1 dialogue cutscene. Freezes
     * The King and the nearest player in place, orients the player to face
     * The King, and ticks out the 500-tick scripted dialogue sequence before
     * flipping to the enraged phase (isEnd = 2).
     *
     * <p>Invoked once per tick by {@link danger.orespawn.entity.ai.KingEndGameGoal#tick()}
     * for as long as {@link #isEnd} == 1. The goal owns {@code MOVE|LOOK|JUMP}
     * so {@link #aiStepFlight()} and {@link #aiStepCombat()} are suppressed
     * for the duration of the cutscene.</p>
     */
    public void aiStepEndGameDialogue() {
        if (this.isEnd == 1) {
            this.endCounter++;
            this.noPhysics = true;
            this.setDeltaMovement(Vec3.ZERO);
            this.hurtCooldown = 10;
            if (this.isRemoved()) return;

            Player nearestPlayer = this.findNearestPlayer();
            if (nearestPlayer != null) {
                this.lookAt(nearestPlayer, 10.0f, 10.0f);
                nearestPlayer.setDeltaMovement(Vec3.ZERO);
                double deltaX = this.getX() - nearestPlayer.getX();
                double deltaZ = this.getZ() - nearestPlayer.getZ();
                float facingAngle = (float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
                nearestPlayer.setYRot(facingAngle);
                nearestPlayer.setHealth(1.0f);
            }

            if (this.endCounter == 10) {
                this.msgToPlayers("The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
                return;
            }
            if (this.endCounter == 80) {
                this.msgToPlayers("The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
                return;
            }
            if (this.endCounter == 160) {
                this.msgToPlayers("The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
                return;
            }
            if (this.endCounter == 240) {
                this.msgToPlayers("The King: I used you to learn your ways, and I have reached my conclusion on your species.");
                return;
            }
            if (this.endCounter == 300) {
                this.msgToPlayers("The King: You have 10 seconds to run...");
                return;
            }
            if (this.endCounter == 320) { this.msgToPlayers("9."); return; }
            if (this.endCounter == 340) { this.msgToPlayers("8."); return; }
            if (this.endCounter == 360) { this.msgToPlayers("7."); return; }
            if (this.endCounter == 380) { this.msgToPlayers("6."); return; }
            if (this.endCounter == 400) { this.msgToPlayers("5."); return; }
            if (this.endCounter == 420) { this.msgToPlayers("4."); return; }
            if (this.endCounter == 440) { this.msgToPlayers("3."); return; }
            if (this.endCounter == 460) { this.msgToPlayers("2."); return; }
            if (this.endCounter == 480) { this.msgToPlayers("1."); return; }
            if (this.endCounter == 500) {
                this.msgToPlayers("The King: Prepare to die!");
                this.isEnd = 2;
                return;
            }
            return;
        }
    }

    /**
     * Primary flight + target + attack behaviour. This is the monolithic
     * "live" phase of The King — mirrors the 1.7.10 original's
     * {@code func_70030_z_} body minus dialogue and bookkeeping.
     *
     * <p>Flow (preserved 1:1 from 1.7.10 to guarantee combat feel):
     * <ol>
     *   <li>If we're too far from home, or a 1-in-200 random wander-roll hits,
     *       or we've reached the current flight target (dist² &lt; 9.1),
     *       <b>pick a new random wander target</b> within ±120 blocks of
     *       home, at an altitude adjusted by
     *       {@link #computeAltitudeAdjustment(int, int)}.</li>
     *   <li>Otherwise, on a 1-in-{@code attackChance} roll, acquire a target
     *       (revenge target first, then {@link #findSomethingToAttack()}
     *       80×64×80 AABB scan), spawn the legacy sidecar {@link KingHead}
     *       if not present, and execute one of:
     *       <ul>
     *         <li>Area + melee damage if within 30 blocks;</li>
     *         <li>Forward area damage in front of the head;</li>
     *         <li>One of three ranged attack streams if &gt;30 blocks
     *             (fireball / thunder / ice) gated by aim check.</li>
     *       </ul>
     *   </li>
     *   <li>If in the enraged phase and currently attacking, spawn a
     *       {@link PurplePower} projectile in the direction of travel.</li>
     *   <li>Always: motion-lerp toward {@link #currentFlightTarget} at 0.35
     *       per tick on the horizontal, 0.3 on vertical (soft smoothing),
     *       and update yaw toward the motion vector at 1/8 per tick.</li>
     * </ol>
     *
     * <p>Invoked by
     * {@link danger.orespawn.entity.ai.KingPrimaryGoal#tick()} every server
     * tick (the goal's {@code canUse()} gates on {@code isEnd != 1} so the
     * dialogue cutscene preempts this completely).</p>
     *
     * <p><b>Main-thread cost</b>: the {@link #findSomethingToAttack()}
     * AABB scan is naturally throttled by the
     * {@code getRandom().nextInt(attackChance)==0} gate — ~1 scan every 3-5
     * ticks per boss in the worst case. No asynchronous work is needed.</p>
     */
    public void aiStepPrimary() {
        int randomXOffset;
        int randomZOffset;
        int attackChance = 5;
        int attackChoice;
        LivingEntity currentTarget;
        LivingEntity nearbyTarget;

        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
            attackChance = 3;
        }
        // In enraged phase, the bookkeeping core already forces stream ammo
        // to max each tick; here we just match the faster attack cadence.
        if (this.isEnd == 2) {
            attackChance = 3;
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        }

        // Branch A: need a new flight target (too far, random wander, or reached current).
        //   → pick a wander destination and return — no combat this tick.
        // Branch B: 1-in-attackChance chance — acquire target and fight.
        //   → if a target is locked, override flight target to pursue it.
        // Branch C (implicit): neither — keep current target, motion-lerp runs below.
        if (this.tooFarFromHome() || this.getRandom().nextInt(200) == 0 || this.flightTargetDistSqr() < 9.1) {
            randomZOffset = this.getRandom().nextInt(120);
            randomXOffset = this.getRandom().nextInt(120);
            if (this.getRandom().nextInt(2) == 0) randomZOffset = -randomZOffset;
            if (this.getRandom().nextInt(2) == 0) randomXOffset = -randomXOffset;

            int altAdj = computeAltitudeAdjustment(this.homeX, this.homeZ);
            int flightY = Math.min((int) (this.getY() + altAdj), 230);
            this.currentFlightTarget = new BlockPos(this.homeX + randomXOffset, flightY, this.homeZ + randomZOffset);

        } else if (this.getRandom().nextInt(attackChance) == 0) {
            // ---- Target acquisition ----
            // Revenge target takes priority; clear it if the target died,
            // is out of range (guard mode), is a boss part (anti-friendly-
            // fire), or is no longer visible (raytrace fails).
            currentTarget = this.revengeTarget;

            if (currentTarget instanceof TheKing || currentTarget instanceof KingHead) {
                this.revengeTarget = null;
                currentTarget = null;
            }
            if (currentTarget != null) {
                float deltaX = (float) (currentTarget.getX() - this.homeX);
                float deltaZ = (float) (currentTarget.getZ() - this.homeZ);
                float distFromHome = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                if (!currentTarget.isAlive() || this.getRandom().nextInt(250) == 1
                        || (distFromHome > 128.0f && this.guardModeTimer == 1)) {
                    currentTarget = null;
                    this.revengeTarget = null;
                }
                if (currentTarget != null && !this.MyCanSee(currentTarget)) {
                    currentTarget = null;
                }
            }

            // ~4 per second under attackChance=5, ~6 per second under
            // attackChance=3. Acceptable for single-boss arenas.
            nearbyTarget = this.findSomethingToAttack();

            // Legacy 1.7.10 sidecar head spawn — see KingHead.java JavaDoc.
            // Retained for NBT save-compat and flight-pattern hook; to be
            // removed once the PartEntity-based hit detection is proven.
            if (this.headEntityFound == 0) {
                KingHead head = ModEntities.KING_HEAD.get().create(this.level());
                if (head != null) {
                    head.moveTo(this.getX(), this.getY() + 20, this.getZ(), 0.0F, 0.0F);
                    this.level().addFreshEntity(head);
                    this.headEntityFound = 1;
                }
            }

            if (currentTarget == null) currentTarget = nearbyTarget;

            if (currentTarget != null) {
                this.setAttacking(1);

                // Chase behaviour: fly directly at the target when not
                // backing off; strafe 30-50 blocks to the side when backing
                // off (makes the fight dynamic instead of a ramming match).
                if (this.backoffTimer == 0) {
                    int flightY = Math.min((int) (currentTarget.getY() + currentTarget.getBbHeight() / 2.0f + 1.0), 230);
                    this.currentFlightTarget = new BlockPos((int) currentTarget.getX(), flightY, (int) currentTarget.getZ());
                    if (this.getRandom().nextInt(70) == 1) {
                        this.backoffTimer = 80 + this.getRandom().nextInt(80);
                    }
                } else if (this.flightTargetDistSqr() < 9.1) {
                    randomZOffset = this.getRandom().nextInt(20) + 30;
                    randomXOffset = this.getRandom().nextInt(20) + 30;
                    if (this.getRandom().nextInt(2) == 0) randomZOffset = -randomZOffset;
                    if (this.getRandom().nextInt(2) == 0) randomXOffset = -randomXOffset;

                    int altAdj = computeAltitudeAdjustment((int) currentTarget.getX(), (int) currentTarget.getZ());
                    int flightY = Math.min((int) (this.getY() + altAdj), 230);
                    this.currentFlightTarget = new BlockPos((int) currentTarget.getX() + randomXOffset, flightY, (int) currentTarget.getZ() + randomZOffset);
                }

                // Melee range (< 30 blocks²) — area damage + direct hit.
                if (this.distanceToSqr(currentTarget) < 900.0) {
                    if (this.getRandom().nextInt(2) == 1) {
                        this.doJumpDamage(this.getX(), this.getY(), this.getZ(),
                                15.0, ATTACK_DAMAGE_VALUE / 4, 0);
                    }
                    this.doHurtTarget(currentTarget);
                }

                // Forward area damage — 20 blocks ahead of the head, 10
                // above. Covers the case where the target ducks under.
                double forwardX = this.getX() + 20.0 * Math.sin(Math.toRadians(this.yHeadRot));
                double forwardZ = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.yHeadRot));
                if (this.getRandom().nextInt(3) == 1) {
                    this.doJumpDamage(forwardX, this.getY() + 10.0, forwardZ,
                            15.0, ATTACK_DAMAGE_VALUE / 2, 1);
                }

                // Long range (> 30 blocks²) — pick one of three elemental
                // projectile streams. Aim check prevents wasting ammo when
                // the target is behind us or blocked by terrain.
                if (this.getHorizontalDistanceSqToEntity(currentTarget) > 900.0) {
                    attackChoice = this.getRandom().nextInt(3);
                    if (attackChoice == 0 && this.fireballStreamCount > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(currentTarget)) this.firecanon(currentTarget);
                    } else if (attackChoice == 1 && this.lightningStreamCount > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(currentTarget)) this.firecanonl(currentTarget);
                    } else if (this.iceStreamCount > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(currentTarget)) this.firecanoni(currentTarget);
                    }
                }
            } else {
                // No target — drop out of attacking pose, refill ammo.
                this.setAttacking(0);
                this.fireballStreamCount = 10;
                this.lightningStreamCount = 5;
                this.iceStreamCount = 8;
            }
        }

        // Enraged-phase signature: a cloud of PurplePower bombs streams
        // behind The King whenever it's mid-attack. Only spawns when
        // isEnd==2 so normal-phase fights don't get spammed.
        if (this.getAttacking() != 0 && this.isEnd == 2) {
            double xzoff = 10.0;
            double yoff = 14.0;
            PurplePower pwr = ModEntities.PURPLE_POWER.get().create(this.level());
            if (pwr != null) {
                pwr.moveTo(
                    this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())),
                    this.getY() + yoff,
                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())),
                    0, 0);
                Vec3 mot = this.getDeltaMovement();
                pwr.setDeltaMovement(mot.x * 3.0, 0, mot.z * 3.0);
                pwr.setPurpleType(10);
                this.level().addFreshEntity(pwr);
            }
        }

        // ---- Flight motion lerp ----
        // Soft smoothing toward the current flight target: Math.signum()
        // gives a unit direction, the 0.7 target speed is blended in over
        // ~3 ticks (35% per tick on x/z, 30% on y). This is what gives The
        // King his characteristic lazy-but-menacing arcs — never snapping,
        // always drifting.
        double goalX = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double goalY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double goalZ = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 mot = this.getDeltaMovement();
        double newMx = mot.x + (Math.signum(goalX) * 0.7 - mot.x) * 0.35;
        double newMy = mot.y + (Math.signum(goalY) * 0.69999 - mot.y) * 0.3;
        double newMz = mot.z + (Math.signum(goalZ) * 0.7 - mot.z) * 0.35;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + yawDelta / 8.0f);
    }

    // ---- Combat ----

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target == null) return false;

        // FULL_POWER_KING_ENABLE: when true, The King deals 2x damage on every
        // attack — melee and ranged alike — making the fight drastically harder.
        double effectiveDamage = this.attackDamage;
        if (OreSpawnConfig.FULL_POWER_KING_ENABLE.get()) {
            effectiveDamage *= 2.0;
        }

        if (target instanceof LivingEntity living) {
            float entitySize = living.getBbHeight() * living.getBbWidth();
            if (entitySize > 30.0f
                    && !MyUtils.isRoyalty(living)
                    && !MyUtils.isBigBoss(living)
                    && !(living instanceof EnderDragon)) {
                living.setHealth(living.getHealth() / 2.0f);
                living.hurt(this.damageSources().mobAttack(this), (float) effectiveDamage * 10.0f);
                this.largeEntityDetected = 1;
            }
        }

        if (target instanceof EnderDragon dragon) {
            DamageSource genDmg = this.damageSources().generic();
            dragon.head.hurt(genDmg, (float) effectiveDamage);
        }

        boolean hit = target.hurt(this.damageSources().mobAttack(this), (float) effectiveDamage);
        if (hit) {
            double knockbackStrength = 3.3;
            double upwardKnockback = 0.25;
            float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            upwardKnockback += this.getRandom().nextFloat() * 0.25;
            if (target.isRemoved() || target instanceof Player) {
                upwardKnockback *= 1.5;
            }
            target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean ret = false;
        if (this.hurtCooldown > 0) return false;

        float clampedDamage = Math.min(amount, 750.0f);

        if (source.getMsgId().equals("inWall")) return false;

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            float entitySize = living.getBbHeight() * living.getBbWidth();
            if (entitySize > 30.0f
                    && !MyUtils.isRoyalty(living)
                    && !MyUtils.isBigBoss(living)
                    ) {
                clampedDamage /= 10.0f;
                this.hurtCooldown = 50;
                this.largeEntityDetected = 1;
            }
            if (living instanceof Monster && entitySize < 3.0f) {
                living.discard();
                return false;
            }
        }

        if (!source.getMsgId().equals("cactus")) {
            this.hurtCooldown = 20;
            ret = super.hurt(source, clampedDamage);
            if (attacker instanceof Player) {
                this.playerHitCount++;
            }
            if (attacker instanceof LivingEntity living && this.currentFlightTarget != null
                    && !MyUtils.isRoyalty(attacker)
                    ) {
                this.revengeTarget = living;
                int flightY = Math.min((int) attacker.getY(), 230);
                this.currentFlightTarget = new BlockPos((int) attacker.getX(), flightY, (int) attacker.getZ());
            }
        }
        return ret;
    }

    @Override
    public int getArmorValue() {
        if (this.largeEntityDetected != 0) return 25;
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() * 2 / 3))
            return DEFENSE_VALUE + 1;
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2))
            return DEFENSE_VALUE + 2;
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 4))
            return DEFENSE_VALUE + 3;
        return DEFENSE_VALUE;
    }

    // ---- Projectile attacks ----

    private void firecanon(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.fireballStreamCount > 0) {
            this.playSound(SoundEvents.TNT_PRIMED, 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            BetterFireball bf = new BetterFireball(this.level(), this,
                    new Vec3(e.getX() - cx,
                             e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff),
                             e.getZ() - cz));
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            this.level().addFreshEntity(bf);

            for (int i = 0; i < 6; i++) {
                float r1 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r2 = 3.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r3 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                bf = new BetterFireball(this.level(), this,
                        new Vec3(e.getX() - cx + r1,
                                 e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2,
                                 e.getZ() - cz + r3));
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                bf.setBig();
                if (this.getRandom().nextInt(2) == 1) bf.setSmall();
                this.level().addFreshEntity(bf);
            }

            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.fireballStreamCount--;
        }
    }

    private void firecanonl(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.lightningStreamCount > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            for (int i = 0; i < 3; i++) {
                ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
                lb.setOwner(this);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                double dx = e.getX() - lb.getX();
                double dy = e.getY() + 0.25 - lb.getY();
                double dz = e.getZ() - lb.getZ();
                float horizDist = Mth.sqrt((float)(dx * dx + dz * dz)) * 0.2f;
                lb.shoot(dx, dy + horizDist, dz, 1.4f, 4.0f);
                Vec3 lbMot = lb.getDeltaMovement();
                lb.setDeltaMovement(lbMot.x * 3.0, lbMot.y * 3.0, lbMot.z * 3.0);
                this.level().addFreshEntity(lb);
            }

            this.lightningStreamCount--;
        }
    }

    private void firecanoni(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.iceStreamCount > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            for (int i = 0; i < 5; i++) {
                IceBall lb = new IceBall(ModEntities.ICE_BALL.get(), this.level());
                lb.setOwner(this);
                lb.enableIceCreation();
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                double dx = e.getX() - lb.getX();
                double dy = e.getY() + 0.25 - lb.getY();
                double dz = e.getZ() - lb.getZ();
                float horizDist = Mth.sqrt((float)(dx * dx + dz * dz)) * 0.2f;
                lb.shoot(dx, dy + horizDist, dz, 1.4f, 4.0f);
                Vec3 lbMot = lb.getDeltaMovement();
                lb.setDeltaMovement(lbMot.x * 3.0, lbMot.y * 3.0, lbMot.z * 3.0);
                this.level().addFreshEntity(lb);
            }

            this.iceStreamCount--;
        }
    }

    private LivingEntity doJumpDamage(double x, double y, double z, double dist, double baseDamage, int knock) {
        // Full Power King mode also amplifies area-of-effect jump damage
        double damage = OreSpawnConfig.FULL_POWER_KING_ENABLE.get() ? baseDamage * 2.0 : baseDamage;
        AABB bb = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, bb);
        entities.sort(this.targetSorter);

        for (LivingEntity target : entities) {
            if (target == this || !target.isAlive()) continue;
            if (MyUtils.isRoyalty(target)) continue;
            if (target instanceof Ghost || target instanceof GhostSkelly) continue;

            target.hurt(this.damageSources().magic(), (float) damage / 2.0f);
            target.hurt(this.damageSources().generic(), (float) damage / 2.0f);

            target.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);

            if (knock != 0) {
                double knockbackStrength = 2.75;
                double upwardKnockback = 0.65;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
        }
        return null;
    }

    // ---- Targeting ----

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;

        if (target instanceof KingHead) { headEntityFound = 1; return false; }
        if (MyUtils.isRoyalty(target)) return false;

        float deltaX = (float) (target.getX() - this.homeX);
        float deltaZ = (float) (target.getZ() - this.homeZ);
        if (Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) > 144.0f) return false;

        if (MyUtils.isIgnoreable(target)) return false;

        if (this.isEnd == 2) {
            if (target instanceof Player player) {
                return !player.getAbilities().instabuild;
            }
            if (target instanceof Girlfriend || target instanceof Boyfriend) return true;
            if (target instanceof Villager) return true;
        }

        if (!this.MyCanSee(target)) return false;

        if (target instanceof Player player) {
            return !player.getAbilities().instabuild;
        }
        if (target instanceof Horse) return true;
        if (target instanceof Monster) return true;
        if (target instanceof EnderDragon) return true;
        return MyUtils.isAttackableNonMob(target);
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(80.0, 64.0, 80.0);

        if (this.isEnd == 2) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);
            players.sort(this.targetSorter);
            this.headEntityFound = 1;
            for (Player player : players) {
                if (this.isSuitableTarget(player)) return player;
            }
        }

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        LivingEntity ret = null;
        this.headEntityFound = 0;
        for (LivingEntity entity : entities) {
            if (entity instanceof KingHead) { this.headEntityFound = 1; }
            if (ret == null && this.isSuitableTarget(entity)) {
                ret = entity;
            }
            if (ret != null && this.headEntityFound != 0) break;
        }
        return ret;
    }

    // ---- Helpers ----

    private boolean isAimedAt(LivingEntity e) {
        double rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
        double rhdir = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
        double rdd = Math.abs(rr - rhdir) % (Math.PI * 2.0);
        if (rdd > Math.PI) rdd -= Math.PI * 2.0;
        return Math.abs(rdd) < 0.5;
    }

    public boolean MyCanSee(LivingEntity e) {
        double xzoff = 22.0;
        int nblks = 20;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + this.getBbHeight() * 7.0f / 8.0f);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 20.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 20.0);
        float dz = (float) ((e.getZ() - startz) / 20.0);

        if (Math.abs(dx) > 1.0f) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) (nblks * Math.abs(dx));
            dx = Mth.clamp(dx, -1.0f, 1.0f);
        }
        if (Math.abs(dy) > 1.0f) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) (nblks * Math.abs(dy));
            dy = Mth.clamp(dy, -1.0f, 1.0f);
        }
        if (Math.abs(dz) > 1.0f) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) (nblks * Math.abs(dz));
            dz = Mth.clamp(dz, -1.0f, 1.0f);
        }

        for (int i = 0; i < nblks; i++) {
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = this.level().getBlockState(BlockPos.containing(startx, starty, startz));
            if (state.isAir() || !state.getFluidState().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean tooFarFromHome() {
        float deltaX = (float) (this.getX() - this.homeX);
        float deltaZ = (float) (this.getZ() - this.homeZ);
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) > 120.0f;
    }

    private double flightTargetDistSqr() {
        if (currentFlightTarget == null) return Double.MAX_VALUE;
        double dx = currentFlightTarget.getX() - this.getX();
        double dy = currentFlightTarget.getY() - this.getY();
        double dz = currentFlightTarget.getZ() - this.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private double getHorizontalDistanceSqToEntity(Entity entity) {
        double deltaZ = entity.getZ() - this.getZ();
        double deltaX = entity.getX() - this.getX();
        return deltaZ * deltaZ + deltaX * deltaX;
    }

    private int computeAltitudeAdjustment(int centerX, int centerZ) {
        int dist = 0;
        for (int i = -5; i <= 5; i += 5) {
            scan:
            for (int j = -5; j <= 5; j += 5) {
                BlockPos checkPos = new BlockPos(centerX + j, (int) this.getY(), centerZ + i);
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.above(k));
                        dist++;
                        if (state.isAir()) continue scan;
                    }
                } else {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.below(k));
                        dist--;
                        if (!state.isAir()) continue scan;
                    }
                }
            }
        }
        return dist / 9 + 2;
    }

    private void msgToPlayers(String s) {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        for (Player p : players) {
            p.sendSystemMessage(Component.literal(s));
        }
    }

    private Player findNearestPlayer() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        players.sort(this.targetSorter);
        return players.isEmpty() ? null : players.get(0);
    }

    public void setGuardMode(int i) {
        this.guardModeTimer = i;
    }

    public void setFree() {
        this.isEnd = 1;
    }

    // ---- Drops ----

    private void dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(20) - this.getRandom().nextInt(20);
        double oy = this.getY() + 12.0;
        double oz = this.getZ() + this.getRandom().nextInt(20) - this.getRandom().nextInt(20);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);

        ThePrince prince = ModEntities.THE_PRINCE.get().create(this.level());
        if (prince != null) {
            prince.moveTo(this.getX(), this.getY() + 10, this.getZ(), 0.0F, 0.0F);
            this.level().addFreshEntity(prince);
        }

        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_SWORD.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_HELMET.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_CHESTPLATE.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_LEGGINGS.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_BOOTS.get(), 1));

        int icount = BuiltInRegistries.ITEM.size();
        int j = 0;
        while (j < 150) {
            Item item = BuiltInRegistries.ITEM.byId(this.getRandom().nextInt(icount));
            if (item == null || item == Items.AIR) continue;
            j++;
            dropItemRand(new ItemStack(item, 1));
        }

        int bcount = BuiltInRegistries.BLOCK.size();
        j = 0;
        while (j < 150) {
            Block block = BuiltInRegistries.BLOCK.byId(this.getRandom().nextInt(bcount));
            if (block == null || block == Blocks.AIR) continue;
            Item blockItem = block.asItem();
            if (blockItem == Items.AIR) continue;
            j++;
            dropItemRand(new ItemStack(blockItem, 1));
        }
    }

    // ---- NBT ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KingHomeX", this.homeX);
        tag.putInt("KingHomeZ", this.homeZ);
        tag.putInt("GuardMode", this.guardModeTimer);
        tag.putInt("PlayerHits", this.playerHitCount);
        tag.putInt("IsEnd", this.isEnd);
        tag.putInt("EndCounter", this.endCounter);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.homeX = tag.getInt("KingHomeX");
        this.homeZ = tag.getInt("KingHomeZ");
        this.guardModeTimer = tag.getInt("GuardMode");
        this.playerHitCount = tag.getInt("PlayerHits");
        this.isEnd = tag.getInt("IsEnd");
        this.endCounter = tag.getInt("EndCounter");
    }
}
