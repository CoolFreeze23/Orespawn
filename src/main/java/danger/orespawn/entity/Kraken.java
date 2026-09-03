package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModSounds;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import danger.orespawn.entity.ai.TargetSelection;

public class Kraken extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.INT);
    /**
     * ENT-S-096: orig Kraken.java:97/:914 — DataWatcher slot 21 mirrors the
     * live PlayNicely flag every AI step so the client renderer can apply
     * the /3 visual shrink dynamically (orig RenderKraken.java:39-45); the
     * BOSS-017 King/Godzilla pattern.
     */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.INT);
    /** ENT-S-096: constructor-time PlayNicely snapshot (orig Kraken.java:70-76). */
    private boolean playNicelyShrunk = false;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Kraken"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    private final Comparator<Entity> targetSorter;
    private BlockPos currentFlightTarget = null;
    private LivingEntity caught = null;
    private int newtarget = 0;
    private int release = 0;
    private int weatherSet = 10;
    private int longEnough = 3600;
    private int callReinforcements = 0;
    private boolean hitByPlayer = false;
    private int straightDown = 1;
    private int hurtTimer = 0;
    // OPT-006 (ruled apply 2026-08-11): obstruction probe cadence; see
    // applyObstructionAvoidance for the throttle/impulse-scaling story.
    private static final int OBSTRUCTION_PROBE_INTERVAL_TICKS = 5;
    private int obstructionProbeCooldown = 0;

    /**
     * Per-entity render scratch (orig Kraken.java:58 {@code renderdata = new RenderInfo()},
     * accessor orig Kraken.java:123-125). Mutated client-side by
     * {@code ModelKraken} for the random mouth-twitch driver
     * (orig ModelKraken.java:1045-1057); never datawatcher-synced.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    public Kraken(EntityType<? extends Kraken> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        // orig Kraken.java:57,81 — target priority uses the shared
        // GenericTargetSorter (creepers and large targets outrank nearer
        // small ones), not plain distance (TF-035).
        this.targetSorter = new GenericTargetSorter(this);
        // ENT-S-096: orig Kraken.java:70-76 — constructor-time PlayNicely
        // snapshot picks 1.3333334x5 instead of 4x15; the hitbox never
        // resizes afterwards even if the config flips (the King's BOSS-017
        // pattern, orig TheKing.java:85-89). The render shrink tracks the
        // live flag through DATA_PLAY_NICELY instead.
        this.playNicelyShrunk = danger.orespawn.OreSpawnConfig.PLAY_NICELY.get();
        this.refreshDimensions();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6515 — Kraken 1000 HP / 40 ATK / 10 armor;
        // speed 0.37 hardcoded in orig Kraken.java:90.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.KRAKEN.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.37)
                .add(Attributes.ATTACK_DAMAGE, MobStats.KRAKEN.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, MobStats.KRAKEN.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        // ENT-S-096: orig Kraken.java:97 seeds slot 21 with the live flag; the
        // port defines 0 like TheKing/Godzilla (BOSS-017) and the first AI
        // step syncs it (orig :914), so a true value is always non-default
        // and ships to the client on spawn and on change.
        builder.define(DATA_PLAY_NICELY, 0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.longEnough <= 0) return true;
        if (this.getY() > 150.0 && this.getHealth() < this.mygetMaxHealth() / 2.0f) return true;
        if (this.getY() > 180.0 && this.longEnough <= 0) {
            this.discard();
            return true;
        }
        return false;
    }

    /**
     * orig Kraken.java:115-117 — reads {@code Kraken_stats.health} (1000, orig
     * OreSpawnMain.java:6515), the same table entry the attributes use. The
     * hurt-retarget (orig :1154, max/4), flee (:952, max/4), reinforcement
     * (:954, max/8) and far-away-despawn (:884, max/2) thresholds all divide
     * this value. ENT-S-100 KT-E: was hardcoded 3000 while the attribute was
     * MobStats' 1000, so every threshold ran against the wrong base (any
     * non-persistent Kraken above y 150 was far-away-despawnable at once);
     * the Basilisk precedent ({@code (int) MobStats.X.maxHealth()}).
     */
    public int mygetMaxHealth() {
        return (int) MobStats.KRAKEN.maxHealth();
    }

    public int getKrakenHealth() {
        return (int) this.getHealth();
    }

    /** ENT-S-096: orig Kraken.java:111-113 — client-side accessor for the /3 render shrink (watcher 21). */
    public final int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    /**
     * ENT-S-096: orig Kraken.java:70-76 — 4x15 normally (orig :73, the
     * ModEntities registration), 1.3333334x5 (orig :75, the exact float the
     * original used) when PlayNicely was set at construction time.
     */
    @Override
    public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
        return this.playNicelyShrunk
                ? net.minecraft.world.entity.EntityDimensions.fixed(1.3333334f, 5.0f)
                : net.minecraft.world.entity.EntityDimensions.fixed(4.0f, 15.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) return;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), (int) (this.getY() - 10.0), (int) this.getZ());
        } else {
            Vec3 motion = this.getDeltaMovement();
            double dampedY = this.getY() < this.currentFlightTarget.getY()
                    ? motion.y * 0.72 : motion.y * 0.5;
            this.setDeltaMovement(motion.x, dampedY, motion.z);
        }

        // ENT-S-097: orig Kraken.java:171 `weather_set > 0 && OreSpawnMain.PlayNicely == 0`
        // — the whole timer block, decrement included, is skipped while
        // PlayNicely is set, so the countdown freezes where it stands and
        // resumes (rather than firing or re-arming) when the flag clears.
        // Live per-site config read, TheKing's BOSS-017 convention: the
        // original read the static at each site; the synced datum
        // (DATA_PLAY_NICELY) is the renderer's copy, never a gate input.
        if (this.weatherSet > 0 && !danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            --this.weatherSet;
            if (this.weatherSet == 0 && this.level() instanceof ServerLevel serverLevel) {
                // BUG-018: orig Kraken.java:171-185 refreshes rain+thunder TIME
                // to 300 ticks each pass (func_76080_g/func_76090_f(300)) and
                // forces the raining/thundering FLAGS only when NOT already
                // raining — an existing plain rain is never upgraded to a
                // thunderstorm. The 100-tick re-arm loop, per-Kraken timers,
                // and non-persistence of weatherSet are all original behavior.
                if (!serverLevel.isRaining()) {
                    serverLevel.setWeatherParameters(0, 300, true, true);
                } else {
                    serverLevel.setWeatherParameters(0, 300, true, serverLevel.isThundering());
                }
                this.weatherSet = 100;
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
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtTimer > 0) --this.hurtTimer;
        if (this.longEnough > 0) --this.longEnough;

        // ENT-S-096: orig Kraken.java:914 — per-AI-step client sync of the
        // live flag (watcher slot 21) for the renderer's /3 branch; the
        // hitbox itself stays the constructor snapshot (getDefaultDimensions).
        this.entityData.set(DATA_PLAY_NICELY, danger.orespawn.OreSpawnConfig.PLAY_NICELY.get() ? 1 : 0);

        // ENT-S-097: orig Kraken.java:915 `nextInt(400) == 1 && OreSpawnMain.PlayNicely == 0`
        // — the roll is consumed first, then the live flag vetoes the bolt.
        if (this.getRandom().nextInt(400) == 1
                && !danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()
                && this.level() instanceof ServerLevel serverLevel) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(this.getX(), this.getY() - 16.0, this.getZ());
                serverLevel.addFreshEntity(bolt);
            }
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), (int) this.getY(), (int) this.getZ());
        }

        double dxFlight = this.currentFlightTarget.getX() - this.getX();
        double dyFlight = this.currentFlightTarget.getY() - this.getY();
        double dzFlight = this.currentFlightTarget.getZ() - this.getZ();
        double distSqToTarget = dxFlight * dxFlight + dyFlight * dyFlight + dzFlight * dzFlight;

        if (this.newtarget != 0 || this.getRandom().nextInt(250) == 1 || distSqToTarget < 9.1) {
            pickNewFlightTarget();
        } else if (this.caught == null && this.getRandom().nextInt(8) == 1
                && !danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            // ENT-S-097: orig Kraken.java:961 `caught == null && nextInt(8) == 1
            // && OreSpawnMain.PlayNicely == 0` — the player grab (:962-973) and
            // the :974-981 findSomethingToAttack fallback both live inside
            // this gated branch (port searchForPrey), so one condition at the
            // call site covers both, exactly as the original's did.
            searchForPrey();
        }

        handleCaughtEntity();
        applyFlightMovement();
        applyObstructionAvoidance();

        if (this.getY() > 256.0 && !this.isPersistenceRequired()) {
            this.discard();
        }
    }

    private void pickNewFlightTarget() {
        this.newtarget = 0;
        int groundDist;
        for (groundDist = 0; groundDist < 31; ++groundDist) {
            BlockPos checkPos = new BlockPos(
                    (int) this.getX(), (int) this.getY() - groundDist, (int) this.getZ());
            if (!this.level().getBlockState(checkPos).isAir()) {
                this.straightDown = 0;
                break;
            }
        }
        groundDist = 20 - groundDist;

        int keepTrying = 50;
        boolean foundAir = false;
        int targetX = (int) this.getX();
        int targetY = (int) this.getY() + groundDist;
        int targetZ = (int) this.getZ();

        while (!foundAir && keepTrying > 0) {
            int xdir = this.getRandom().nextInt(6) + 12;
            int zdir = this.getRandom().nextInt(6) + 12;
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;
            if (this.straightDown != 0) {
                xdir = 0;
                zdir = 0;
            }
            targetX = (int) this.getX() + xdir;
            targetY = (int) this.getY() + groundDist + this.getRandom().nextInt(9) - 6;
            targetZ = (int) this.getZ() + zdir;
            BlockPos candidatePos = new BlockPos(targetX, targetY, targetZ);
            if (this.level().getBlockState(candidatePos).isAir()
                    && this.canSeeTarget(targetX, targetY, targetZ)) {
                foundAir = true;
            }
            --keepTrying;
        }
        this.currentFlightTarget = new BlockPos(targetX, targetY, targetZ);

        if (this.longEnough <= 0
                || (this.getY() < 200.0 && this.getHealth() < this.mygetMaxHealth() / 4.0f)) {
            this.currentFlightTarget = new BlockPos(
                    this.currentFlightTarget.getX(),
                    this.currentFlightTarget.getY() + 30,
                    this.currentFlightTarget.getZ());

            if (this.hitByPlayer && this.callReinforcements == 0
                    && this.getHealth() < this.mygetMaxHealth() / 8.0f
                    && this.getY() > 130.0
                    && this.level() instanceof ServerLevel serverLevel) {
                this.callReinforcements = 1;
                for (int i = 0; i < 10; i++) {
                    Entity newEntity = this.getType().create(serverLevel);
                    if (newEntity != null) {
                        double sx = this.getX() + this.getRandom().nextInt(10)
                                - this.getRandom().nextInt(10);
                        double sz = this.getZ() + this.getRandom().nextInt(10)
                                - this.getRandom().nextInt(10);
                        newEntity.moveTo(sx, 170.0, sz,
                                this.getRandom().nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(newEntity);
                    }
                }
            }
        }
    }

    /**
     * orig Kraken.java:962-981. The player grab takes the NEAREST player of
     * any game mode (:963 {@code func_72857_a}) and only then nulls a
     * creative one (:965/:970-972) — the WormSmall idiom (orig
     * WormSmall.java:179-182) — so a creative player standing nearer than a
     * survival one shadows that survival player from this branch; the
     * {@code findSomethingToAttack} fallback (:974-981, 1-in-2) runs only
     * when the player target ended up null. ENT-S-100 KT-A: the port had
     * skipped creative players inside the scan and hunted the survival one
     * directly.
     */
    private void searchForPrey() {
        Player target = findNearestPlayer();
        if (target != null) {
            if (!target.getAbilities().instabuild) {                    // orig :965
                if (this.getSensing().hasLineOfSight(target)) {        // orig :966
                    this.currentFlightTarget = new BlockPos(
                            (int) target.getX(),
                            (int) target.getY() + 15,
                            (int) target.getZ());                      // orig :967
                    this.attackWithSomething(target);                  // orig :968
                }
            } else {
                target = null;                                         // orig :970-972
            }
        }
        if (target == null && this.getRandom().nextInt(2) == 0) {      // orig :974
            LivingEntity entityTarget = findSomethingToAttack();
            if (entityTarget != null) {
                this.currentFlightTarget = new BlockPos(
                        (int) entityTarget.getX(),
                        (int) entityTarget.getY() + 15,
                        (int) entityTarget.getZ());
                this.attackWithSomething(entityTarget);
            }
        }
    }

    private void handleCaughtEntity() {
        if (this.caught == null) return;

        // orig Kraken.java:984 `if (!this.caught.isDead)` — the hold lasts until
        // the victim is REMOVED from the world, not until its health reaches
        // zero: a victim killed in the grip is dragged through its death
        // animation and let go (the :1006-1012 else branch) once vanilla
        // removes it. ENT-S-100 KT-D, ruled faithful (the port had released at
        // isAlive() == false).
        if (!this.caught.isRemoved()) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), 200, (int) this.getZ());
            if (this.getY() > 190.0) {
                this.release = 1;
            }
            Vec3 myMotion = this.getDeltaMovement();
            this.caught.setDeltaMovement(myMotion);
            if (this.getY() - this.caught.getY() > 16.0) {
                this.caught.setDeltaMovement(
                        this.caught.getDeltaMovement().add(0, 0.25, 0));
            }
            // The grab holds the victim 15 blocks below the Kraken (original behavior).
            // Players are client-authoritative: raw setPos/setDeltaMovement on a
            // ServerPlayer desyncs the client and triggers "moved wrongly" kicks
            // (BUG-011), so players are moved via the connection teleport and their
            // forced motion is flagged for sync with hurtMarked.
            double grabX = this.getX();
            double grabY = this.getY() - 15.0;
            double grabZ = this.getZ();
            if (this.caught instanceof ServerPlayer caughtPlayer) {
                caughtPlayer.connection.teleport(grabX, grabY, grabZ, this.getYRot(), caughtPlayer.getXRot());
                caughtPlayer.hurtMarked = true;
            } else {
                this.caught.setPos(grabX, grabY, grabZ);
                this.caught.setYRot(this.getYRot());
            }

            if (this.getRandom().nextInt(50) == 1) {
                this.doHurtTarget(this.caught);
            }
            if (this.release != 0 || this.getRandom().nextInt(250) == 1) {
                releaseCaughtEntity();
            }
        } else {
            releaseCaughtEntity();
        }
    }

    private void releaseCaughtEntity() {
        this.caught = null;
        this.newtarget = 1;
        this.release = 0;
        this.setAttacking(0);
    }

    private void applyFlightMovement() {
        double toTargetX = this.currentFlightTarget.getX() + 0.3 - this.getX();
        double toTargetY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double toTargetZ = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(toTargetX) * 0.45 - motion.x) * 0.15;
        double my = motion.y + (Math.signum(toTargetY) * 0.70999 - motion.y) * 0.202;
        double mz = motion.z + (Math.signum(toTargetZ) * 0.45 - motion.z) * 0.15;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.4f;
        if (Math.abs(mx) + Math.abs(mz) < 0.15) {
            yawDiff = 0.0f;
        }
        this.setYRot(this.getYRot() + yawDiff / 5.0f);
    }

    /**
     * OPT-006 (ruled apply 2026-08-11): the 19x5 = 95-block obstruction probe
     * now runs once every {@value #OBSTRUCTION_PROBE_INTERVAL_TICKS} server
     * ticks with a single reused {@link BlockPos.MutableBlockPos} instead of
     * 95 fresh {@code new BlockPos} allocations every tick. The lift impulse
     * (both the deltaMovement add and the direct position shift) is scaled by
     * the interval so net buoyancy over any 5-tick window is identical — the
     * finding's own math, accepted by the ruling.
     * <p>Throttle story: obstruction response can lag by up to 4 ticks (the
     * Kraken keeps its damped drift for the skipped ticks, then receives the
     * whole interval's lift at once); the cooldown is deliberately not
     * persisted (same convention as weatherSet), so a reloaded Kraken simply
     * probes on its first AI step and the cadence re-arms from there.
     */
    private void applyObstructionAvoidance() {
        if (this.obstructionProbeCooldown > 0) {
            --this.obstructionProbeCooldown;
            return;
        }
        this.obstructionProbeCooldown = OBSTRUCTION_PROBE_INTERVAL_TICKS - 1;
        double obstructionFactor = 0.0;
        BlockPos.MutableBlockPos probePos = new BlockPos.MutableBlockPos();
        for (int k = -20; k < 18; k += 2) {
            for (int i = 1; i < 10; i += 2) {
                double dx = (double) i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                double dz = (double) i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                probePos.set(
                        (int) (this.getX() + dx),
                        (int) this.getY() + k,
                        (int) (this.getZ() + dz));
                if (!this.level().getBlockState(probePos).isAir()) {
                    obstructionFactor += 0.1;
                }
            }
        }
        if (obstructionFactor > 0) {
            double lift = obstructionFactor * 0.08 * OBSTRUCTION_PROBE_INTERVAL_TICKS;
            this.setDeltaMovement(
                    this.getDeltaMovement().add(0, lift, 0));
            this.setPos(this.getX(),
                    this.getY() + lift, this.getZ());
        }
    }

    /**
     * orig Kraken.java:963 {@code World.findNearestEntityWithinAABB(EntityPlayer.class,
     * boundingBox.expand(25, 40, 25), this)}: the nearest player of ANY game
     * mode. The creative check is the caller's (orig :965/:970-972,
     * {@link #searchForPrey}), so this scan must not skip anyone — ENT-S-100
     * KT-A. Ties go to the LAST player scanned: orig {@code World.func_72857_a}
     * (1.7.10 {@code ahb.a(Class, AxisAlignedBB, Entity)}) replaces its candidate
     * on {@code d1 <= d0} — bytecode {@code dcmpl; ifle}, the update body runs
     * when {@code d1 <= d0} — hence the {@code <=} below, ENT-S-105.
     */
    private Player findNearestPlayer() {
        AABB searchBox = this.getBoundingBox().inflate(25.0, 40.0, 25.0);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player player : players) {
            double distSq = this.distanceToSqr(player);
            if (distSq <= minDist) {                                   // orig d1 <= d0 (dcmpl; ifle) — ENT-S-105
                minDist = distSq;
                nearest = player;
            }
        }
        return nearest;
    }

    private void attackWithSomething(LivingEntity target) {
        if (this.caught != null) return;
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        double dy = this.getY() - target.getY() - 15.0;
        double dist = dx * dx + dz * dz + dy * dy;
        if (dist < 30.0) {
            this.caught = target;
            this.release = 0;
            this.setAttacking(1);
        }
    }

    public boolean canSeeTarget(double targetX, double targetY, double targetZ) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.75, this.getZ());
        Vec3 to = new Vec3(targetX, targetY, targetZ);
        HitResult result = this.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (this.currentFlightTarget != null && attacker instanceof Player
                && this.getHealth() > this.mygetMaxHealth() / 4.0f) {
            this.hitByPlayer = true;
            this.currentFlightTarget = new BlockPos(
                    (int) attacker.getX(), (int) attacker.getY() + 15, (int) attacker.getZ());
        }
        // ENT-K-002: the effective re-hit window is this custom 30-tick timer
        // (orig Kraken.java:1158-1161), decremented once per AI step (orig
        // Kraken.java:908-910; port customServerAiStep). The oft-cited
        // "field_70174_ab = 120" sits ONCE in the constructor (orig
        // Kraken.java:79), not in the hurt path, and is 1.7.10
        // Entity.fireResistance — a vestigial field vanilla never reads — so
        // no 120-tick invulnerability ever existed in the original; it is
        // deliberately not ported.
        if (this.hurtTimer > 0) return false;
        this.hurtTimer = 30;
        boolean ret = super.hurt(source, amount);
        if (this.getRandom().nextInt(2) == 1) {
            this.release = 1;
        }
        return ret;
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

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(5) == 0) {
            return ModSounds.KRAKEN_LIVING.get(); // orig Kraken.java:199-204 "orespawn:kraken_living" 1-in-5
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get(); // orig Kraken.java:210-212 "orespawn:alo_death"
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LongEnough", this.longEnough);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.longEnough = tag.getInt("LongEnough");
    }

    // Death drops are fully data-driven via loot_table/entities/kraken.json
    // (orig Kraken.java:236-871: kraken tooth, painting, 120-279 ink sac,
    // 5-14 rolls of the d53 Ultimate/Diamond/Iron/Gold/Experience/Amethyst
    // gear table).

    /**
     * orig Kraken.java:1060-1128, in the original's order: null / self / dead
     * (:1061-1069), the shared {@code MyUtils.isIgnoreable} screen (:1070-1072,
     * the ENT-S-101 list), line of sight (:1073-1075), the player branch
     * (:1076-1082: creative → false, then {@code !isFlying}), the
     * ground-or-water rule (:1083-1085), then the species chain — EntitySquid
     * (:1086), AttackSquid (:1089), Kraken (:1092), Spyro (:1095), Dragon /
     * Cephadrome / Leon / ThePrinceTeen / ThePrinceAdult prey only while
     * unridden (:1098-1117, {@code riddenByEntity == null} → {@code !isVehicle()}),
     * EntityChicken (:1118), Chipmunk (:1121), StinkBug (:1124), Mothra
     * (:1127). The Rotator-shape instanceof chain. ENT-S-100 KT-B1 (the port
     * had kept only Kraken and Squid, so it grabbed mounts with their riders
     * and spared species) and KT-B2 (orig :1081 {@code capabilities.isFlying}
     * is {@code Abilities.flying}, the port's own Dragon and Leon mapping —
     * not {@code invulnerable}). Mothra is a butterfly in both trees, so the
     * shared list spares it before :1127 does; the check is kept as written.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false; // orig :1061-1069
        if (MyUtils.isIgnoreable(target)) return false;                          // orig :1070-1072
        if (!this.getSensing().hasLineOfSight(target)) return false;             // orig :1073-1075
        if (target instanceof Player player) {                                   // orig :1076-1082
            if (player.getAbilities().instabuild) return false;                  // orig :1078-1080 creative
            return !player.getAbilities().flying;                                // orig :1081 isFlying (KT-B2)
        }
        if (!target.onGround() && !target.isInWater()) return false;             // orig :1083-1085
        if (target instanceof Squid) return false;                               // orig :1086-1088 EntitySquid
        if (target instanceof AttackSquid) return false;                         // orig :1089-1091
        if (target instanceof Kraken) return false;                              // orig :1092-1094
        if (target instanceof EntitySpyro) return false;                         // orig :1095-1097 Spyro
        if (target instanceof Dragon c) return !c.isVehicle();                   // orig :1098-1101 riddenByEntity == null
        if (target instanceof Cephadrome c) return !c.isVehicle();               // orig :1102-1105
        if (target instanceof EntityLeon c) return !c.isVehicle();               // orig :1106-1109 Leon
        if (target instanceof ThePrinceTeen c) return !c.isVehicle();            // orig :1110-1113
        if (target instanceof ThePrinceAdult c) return !c.isVehicle();           // orig :1114-1117
        if (target instanceof Chicken) return false;                             // orig :1118-1120 EntityChicken
        if (target instanceof Chipmunk) return false;                            // orig :1121-1123
        if (target instanceof EntityStinkBug) return false;                      // orig :1124-1126 StinkBug
        return !(target instanceof Mothra);                                      // orig :1127
    }

    private LivingEntity findSomethingToAttack() {
        // ENT-S-097: orig Kraken.java:1131-1133 `if (OreSpawnMain.PlayNicely != 0)
        // return null;` ahead of the search — the gate TheKing (orig :985-988)
        // and Godzilla (orig :524-527) carry too, minus their head_found side
        // effect (the Kraken has no head sidecar). Live per-call read (BOSS-017).
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            return null;
        }
        AABB searchBox = this.getBoundingBox().inflate(20.0, 40.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(entities, this.targetSorter, this::isSuitableTarget);
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** Mirrors orig Kraken.java:123-125 {@code getRenderInfo()}. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    /** orig Kraken.java:1183-1197 — y>=50; air/short-grass clearance above the spawn column. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return OriginalSpawnGates.boxMatches(this, level, -1, 0, 1, 5, -1, 1,
                s -> s.isAir() || s.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS));
    }
}
