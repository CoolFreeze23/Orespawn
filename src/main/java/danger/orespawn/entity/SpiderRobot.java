package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.entity.gait.ModernSpiderGait;
import de.dertoaster.multihitboxlib.api.ICustomHitboxProfileSupplier;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;

import java.util.Optional;

public class SpiderRobot extends Mob implements ICustomHitboxProfileSupplier, IModernLeggedRobot {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROBOTSPIDER = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider"));
    private static final SoundEvent SND_ROBOTSPIDERMOUNT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspidermount"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SpiderRobot.class, EntityDataSerializers.INT);
    /**
     * 2.0 S2: the SERVER's construction-time spiderMovement snapshot, synced
     * to clients. spiderMovement is a COMMON config (per-side files, never
     * synced), so the client must NEVER consult its own copy — an unsynced
     * mode split leaves client legs frozen at full stretch (independent
     * review, multiplayer BLOCKER). Mode-independent infrastructure: defined
     * in both modes, false for classic.
     */
    private static final EntityDataAccessor<Boolean> DATA_MODERN_GAIT =
            SynchedEntityData.defineId(SpiderRobot.class, EntityDataSerializers.BOOLEAN);

    private static final int LEG_COUNT = 8;
    private final RenderSpiderRobotInfo renderInfo = new RenderSpiderRobotInfo(LEG_COUNT);
    /** Lazy one-shot leg-data (re)initialization flag (orig {@code didonce}, SpiderRobot.java:53). */
    private boolean legDataInitialized = false;

    /**
     * 2.0 spider overhaul (S2): the modern gait controller, or {@code null}
     * in classic mode. SERVER: fixed at construction from the spiderMovement
     * config (BOSS-017 snapshot pattern — a config flip affects newly
     * constructed spiders only). CLIENT: created lazily off the server's
     * {@link #DATA_MODERN_GAIT synced flag}; the client's own config is
     * never consulted. Classic-mode spiders on either side never construct
     * modern state and run the untouched D2 path below.
     */
    private ModernSpiderGait modernGait;

    // ENT-S-022: the port had invented a ServerBossEvent here. The original
    // SpiderRobot has no boss bar of any kind — it is a rideable vehicle whose
    // status renders through the dedicated RenderSpiderRobotInfo HUD overlay
    // (orig SpiderRobot.java:52); nothing in the orig class touches BossStatus.
    // Removed for parity.

    /**
     * TF-035: orig SpiderRobot.java:50 (field), :60 (ctor) — the original
     * declares and constructs a GenericTargetSorter but never sorts with it:
     * findSomethingToAttack (orig :971-986) walks the raw entity list and
     * returns the first suitable hit. The dead field is kept, typed as the
     * real sorter, for exactness — do NOT add a sort call.
     */
    private final GenericTargetSorter targetSorter;
    private final float moveSpeed = 0.35f;
    private int soundCooldown = 0;

    public SpiderRobot(EntityType<? extends SpiderRobot> type, Level level) {
        super(type, level);
        // orig SpiderRobot.java:64 — XP = SpiderRobot_stats.health / 2 = 1500/2.
        this.xpReward = 750;
        this.targetSorter = new GenericTargetSorter(this);
        // orig SpiderRobot.java:508-511 — entityInit primes the leg data once at construction.
        initLegData();
        // 2.0 S2/S4: server-side construction snapshot of the movement mode,
        // published to clients on the synced flag (see DATA_MODERN_GAIT).
        // The mode was already read ONCE at the LivingEntity ctor tail by the
        // profile supplier (ctorTailModernDecision) — consume that same
        // decision here rather than re-reading the config: entity ctors can
        // run on worldgen worker threads, and a config flip between two
        // independent reads could have built parts on a CLASSIC-snapshot
        // spider (review: the ctor tear).
        if (!level.isClientSide()) {
            boolean modern = this.ctorTailModernDecision != null
                    ? this.ctorTailModernDecision
                    : OreSpawnConfig.SPIDER_MOVEMENT.get() == OreSpawnConfig.SpiderMovement.MODERN;
            this.entityData.set(DATA_MODERN_GAIT, modern);
            if (modern) {
                this.modernGait = new ModernSpiderGait(danger.orespawn.entity.gait.SpiderRigProfile.RIG);
            }
        }
        // 2.0 S4: the profile supplier below may already have been consulted
        // from the LivingEntity ctor tail (before this body ran); from here
        // on the snapshot field is the authority.
        this.movementModeDecided = true;
    }

    /**
     * 2.0 S4: true once this ctor's body has run — the MHLib profile
     * supplier is consulted from the LivingEntity ctor TAIL, before the
     * movement-mode snapshot field assigns, and decides from the config
     * exactly once in that window (stored below; the ctor body consumes
     * the SAME decision — single authoritative read, no tear).
     */
    private boolean movementModeDecided;
    /** The one ctor-tail config read (server); null until the supplier ran. */
    private Boolean ctorTailModernDecision;

    /**
     * 2.0 S4 — the MHLib part gate (design D3: classic constructs ZERO
     * parts). MHLib's first {@code ICustomHitboxProfileSupplier}
     * implementor. IMPORTANT dispatch note (corrects the S4 as-designed
     * sketch): this method has the same signature as
     * {@code IMultipartEntity.getHitboxProfile}'s default, so it SHADOWS
     * MHLib's entire resolution — every internal MHLib call (part build,
     * pickability, damage routing, alignment) lands here. It must therefore
     * return the REAL profile for modern spiders itself (via the
     * EntityType-memoized datapack lookup) and {@code Optional.empty()} for
     * classic — never {@code null}. Mode source: server = the snapshot
     * (config during the ctor-tail window); client = the synced flag, with
     * the client part build in {@link #onSyncedDataUpdated} (id-restore +
     * pick registration).
     */
    @Override
    public Optional<HitboxProfile> getHitboxProfile() {
        final boolean modern;
        if (this.level().isClientSide()) {
            modern = this.entityData.get(DATA_MODERN_GAIT);
        } else if (this.movementModeDecided) {
            modern = this.modernGait != null;
        } else {
            // Single authoritative ctor-tail read (see ctorTailModernDecision).
            if (this.ctorTailModernDecision == null) {
                this.ctorTailModernDecision =
                        OreSpawnConfig.SPIDER_MOVEMENT.get() == OreSpawnConfig.SpiderMovement.MODERN;
            }
            modern = this.ctorTailModernDecision;
        }
        if (!modern) {
            return Optional.empty();
        }
        return MHLibDatapackLoaders.getHitboxProfile(this.getType(), this.level().registryAccess());
    }

    /**
     * 2.0 S4: the client part build, moved OFF the lazy getModernGait path
     * (review BLOCKER: building there ran after the network id was applied,
     * and MHLib's ctor-tail re-id clobbered it — modern spiders became
     * unattackable from the client). Building here, when the server's mode
     * flag arrives: (1) the synced id is captured and RESTORED after the
     * build, so the setId cascade gives the parts syncedId+1..+8 — exactly
     * the server's part ids; (2) the parts are then registered in the
     * client pick registry, which NeoForge only populates at add time
     * (vendored MHLibClientPartRegistration).
     */
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (DATA_MODERN_GAIT.equals(dataAccessor)
                && this.level().isClientSide()
                && this.entityData.get(DATA_MODERN_GAIT)
                && (this.getParts() == null || this.getParts().length == 0)) {
            Object self = this;
            if (self instanceof IMultipartEntity<?> multipart) {
                final int syncedId = this.getId();
                multipart.mhlibOnConstructor();
                this.setId(syncedId);
                de.dertoaster.multihitboxlib.client.MHLibClientPartRegistration.registerParts(this);
            }
        }
    }

    /** True when this spider runs the modern gait (server snapshot; synced). */
    public boolean isModernMovement() {
        return this.level().isClientSide() ? this.entityData.get(DATA_MODERN_GAIT) : this.modernGait != null;
    }

    /**
     * The modern gait controller, or {@code null} for classic. On the client
     * the controller materializes lazily once the synced flag says modern —
     * this also lets gait payloads arriving before the first client tick
     * find a controller to apply to.
     */
    public ModernSpiderGait getModernGait() {
        if (this.modernGait == null && this.level().isClientSide() && this.entityData.get(DATA_MODERN_GAIT)) {
            // Client replay controller only — the client PART build lives in
            // onSyncedDataUpdated (id-restore + pick registration; see there).
            this.modernGait = new ModernSpiderGait(danger.orespawn.entity.gait.SpiderRigProfile.RIG);
        }
        return this.modernGait;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6474 — SpiderRobot 1500 HP / 100 ATK / 16 armor;
        // speed 0.35 matches orig SpiderRobot.java:51.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SPIDER_ROBOT.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SPIDER_ROBOT.attackDamage())
                .add(Attributes.ARMOR, MobStats.SPIDER_ROBOT.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_MODERN_GAIT, false);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    protected void customServerAiStep() {
        // orig SpiderRobot.java:94-102 — AI is fully suspended while dead-flagged or ridden.
        if (this.isRemoved()) return;
        if (this.getFirstPassenger() != null) return;
        super.customServerAiStep();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")
                || source.getMsgId().equals("inFire") || source.getMsgId().equals("onFire")
                || source.getMsgId().equals("magic") || source.getMsgId().equals("starve")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.clearFire();
        // ENT-S-021: orig SpiderRobot.java:590-592 — while ridden, the feet
        // stomp 1-in-40 ticks, hitting EVERY suitable target in the 12-18
        // block ring at once (that ring is where the feet actually plant:
        // legoff up to 3.4 plus the 16-block footing probe). PEACEFUL-gated.
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && !this.level().isClientSide()
                && this.getFirstPassenger() != null && this.getRandom().nextInt(40) == 0) {
            this.feetFindSomethingToHit();
        }
        // orig SpiderRobot.java:593-604 — 1-in-15 frontal melee while ridden.
        // ENT-S-021 restores the PEACEFUL gate the port had dropped (orig :593).
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && !this.level().isClientSide()
                && this.getFirstPassenger() != null && this.getRandom().nextInt(15) == 0) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                double meleeRange = (12.0f + target.getBbWidth() / 2.0f); // orig :597
                if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
        // ENT-S-021: orig SpiderRobot.java:605-616 — the flame-jet exhaust.
        // Each particle is launched with outward velocity dx/f, dz/f (a full
        // block/tick horizontally, so the jet streams away from the body) plus
        // per-axis jitter; the port had spawned them motionless and dropped the
        // fireworksSpark roll entirely. Client-only exactly as in 1.7.10, where
        // World.spawnParticle was a WorldClient-only effect.
        float exhaustOffset = 8.0f;
        float exhaustX = (float) (exhaustOffset * Math.cos(Math.toRadians(this.getYRot() - 90.0f)));
        float exhaustZ = (float) (exhaustOffset * Math.sin(Math.toRadians(this.getYRot() - 90.0f)));
        if (this.level().isClientSide()) {
            // orig :608-609 — flame, 1-in-8, vertical jitter /10.
            if (this.getRandom().nextInt(8) == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        getX() + exhaustX, getY() + 2.0, getZ() + exhaustZ,
                        exhaustX / exhaustOffset + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 20.0f,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 10.0f,
                        exhaustZ / exhaustOffset + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 20.0f);
            }
            // orig :611-612 — smoke, 1-in-2, vertical jitter /10.
            if (this.getRandom().nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        getX() + exhaustX, getY() + 2.0, getZ() + exhaustZ,
                        exhaustX / exhaustOffset + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 20.0f,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 10.0f,
                        exhaustZ / exhaustOffset + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 20.0f);
            }
            // orig :614-615 — fireworksSpark, 1-in-10, the hotter /5 vertical jitter.
            if (this.getRandom().nextInt(10) == 0) {
                this.level().addParticle(ParticleTypes.FIREWORK,
                        getX() + exhaustX, getY() + 2.0, getZ() + exhaustZ,
                        exhaustX / exhaustOffset + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 20.0f,
                        (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 5.0f,
                        exhaustZ / exhaustOffset + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) / 20.0f);
            }
            // orig SpiderRobot.java:704 — the leg solver steps once per client
            // tick. 2.0 S2: modern-mode spiders replay the server gait instead
            // (mode from the synced flag); the classic branch is the untouched
            // D2 call.
            if (isModernMovement()) {
                getModernGait().clientTick(this);
            } else {
                updateLegs();
            }
        }
        // 2.0 S2/S3: the server-authoritative modern gait. Never moves the
        // body — its server-visible effects are the gait packets plus the
        // mobGriefing-gated, ridden-only trample block changes (S3).
        if (this.modernGait != null && !this.level().isClientSide()) {
            this.modernGait.serverTick(this);
        }
        if (this.soundCooldown > 0) --this.soundCooldown;
        if (this.getFirstPassenger() != null && this.soundCooldown == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(SND_ROBOTSPIDER, 0.45f, 1.0f);
            this.soundCooldown = 125;
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            double knockbackStrength = 1.2;
            double upwardKnockback = 0.15;
            float angleToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
            // Attribute is authoritative (orig melee used the attack attribute).
            boolean ret = livingTarget.hurt(this.damageSources().mobAttack(this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            if (livingTarget.isRemoved() || livingTarget instanceof Player) upwardKnockback *= 2.0;
            if (ret) livingTarget.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            return ret;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.IRON_INGOT) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide()) {
                float heal = this.getMaxHealth() - this.getHealth();
                if (heal > 100.0f) heal = 100.0f;
                if (heal > 0) this.heal(heal);
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        if (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Player
                && this.getFirstPassenger() != player) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide() && this.getFirstPassenger() == null && this.distanceToSqr(player) < 16.0) {
            player.startRiding(this);
            this.playSound(SND_ROBOTSPIDERMOUNT, 0.65f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    // ==================== 2.0 S5 — the Q1 ridden path (modern-only) ====================

    /**
     * Q1 ruling: modern-mode spiders are STEERABLE by a mounted player;
     * classic keeps the faithful 1.0 gap (a mounted player cannot steer —
     * this returns the vanilla null). The SpiderDriver is NEVER controlling
     * in either mode (it is not a Player); its classic velocity-set shoving
     * coexists untouched with this path.
     */
    @javax.annotation.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isModernMovement()) {
            // Modern owns the decision outright: a mounted player steers,
            // and any non-player rider (the SpiderDriver) is NEVER
            // controlling. Vanilla Mob's default hands control to a MOB
            // first-passenger (the jockey branch) — which, reviewed against
            // decompiled 1.21.1 dispatch, does NOT intercept the driver's
            // velocity-set shoving (only a Player controller diverts travel
            // into travelRidden); its real consumers are updateControlFlags
            // (goal suppression — reasserted for any rider by our override
            // below) and the rider nav/moveControl aliasing, which must
            // never alias a driver onto the modern gait.
            if (!this.getPassengers().isEmpty()
                    && this.getPassengers().get(0) instanceof Player player) {
                return player;
            }
            return null;
        }
        // Classic: the exact pre-S5 vanilla dispatch, untouched (players
        // were never controlling there — the faithful no-steer gap).
        return super.getControllingPassenger();
    }

    /**
     * S5 (independent review): while carrying ANY rider, a MODERN spider's
     * look goals are suppressed. This preserves the pre-S5 driver-ridden
     * behavior — vanilla's jockey branch (a controlling MOB) cleared the
     * LOOK flag through this very method, and the null-for-driver arm above
     * would silently re-enable it — and it keeps the server's head-look
     * from fighting a steering rider's yaw for observers (a Player
     * controller never clears goal flags in vanilla, and vanilla steerables
     * carry no look goals; this spider does). Classic takes pure super:
     * the jockey suppression while driver-ridden AND the look-around while
     * player-ridden both stay bit-identical to pre-S5. Two scope notes:
     * vanilla calls this every 5 server ticks, so suppression lags a mount
     * by up to 5 ticks (the identical cadence the jockey branch always
     * had), and only LOOK is re-cleared — the pre-S5-preservation claim
     * holds because this spider registers LOOK-flag goals exclusively; a
     * future MOVE/JUMP goal would need re-clearing here too.
     */
    @Override
    protected void updateControlFlags() {
        super.updateControlFlags();
        if (this.isModernMovement() && this.isVehicle()) {
            this.goalSelector.setControlFlag(Goal.Flag.LOOK, false);
        }
    }

    /**
     * B3-family ground-walker steering (the ant's tickRidden is the hover
     * member of this family; the spider is the ground member): body yaw
     * follows the rider directly, pitch stays flat. Travel itself flows
     * through the vanilla ridden pipeline via {@link #getRiddenInput} /
     * {@link #getRiddenSpeed} — collisions, step height and the modern gait
     * all see ordinary body movement. The steering-specific guard lives in
     * the gait, not here: rest bearings follow a DEAD-BANDED, rate-limited
     * heading (ModernSpiderGait REST_YAW_*) so a rider's look-jitter cannot
     * dance the legs. Two honest notes from the S5 review: actual ground
     * displacement at speed-attribute 0.35 is ~0.65-0.7 blocks/tick under
     * vanilla physics (~2x the gait's FULL_SPEED constant — speedFrac just
     * saturates at 1.0 and STEP_SPEED still outruns it), and vanilla grants
     * a player-CONTROLLED living entity two ride buffs we keep deliberately
     * (horse-family consistency, stair feel): maxUpStep becomes
     * max(attr, 1.0) while steered (0.6 otherwise) and airborne
     * acceleration rises to speed*0.1. Both recorded in KNOWN_ISSUES.
     */
    @Override
    protected void tickRidden(Player rider, Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        this.setRot(rider.getYRot(), 0.0f);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    /** WASD → travel vector: full forward, half strafe, quarter reverse, no jump. */
    @Override
    protected Vec3 getRiddenInput(Player rider, Vec3 travelVector) {
        float sideways = rider.xxa * 0.5f;
        float forward = rider.zza;
        if (forward <= 0.0f) {
            forward *= 0.25f;
        }
        return new Vec3(sideways, 0.0, forward);
    }

    /** Ridden speed = the rig's own movement-speed attribute (0.35). */
    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    // The previous empty overrides dropped the super call, so Health, effects,
    // PersistenceRequired and equipment were never saved — robots reloaded at full HP
    // and name-tagged ones could despawn (BUG-007). The overrides are removed entirely:
    // SpiderRobot has no extra fields to persist, so the inherited behavior is correct.

    private LivingEntity findSomethingToAttack() {
        // ENT-S-021: orig SpiderRobot.java:972-974 — PlayNicely disables the
        // ridden auto-attack entirely. The list is deliberately NOT sorted:
        // despite owning a GenericTargetSorter (orig :50,:60) the orig never
        // sorts here (:975-985), so the first suitable entity in raw scan
        // order wins — quirk kept.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(20.0, 12.0, 20.0); // orig :975
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    /**
     * ENT-S-021: orig SpiderRobot.java:988-1038 — the ridden melee is a
     * FRONTAL attack, not the port's old omnidirectional one. Beyond the
     * spider-family/passenger exclusions, ignoreables and line of sight, the
     * target's bearing must sit within 0.75 rad (~43°) of the robot's facing
     * — unless it is closer than 6 blocks, where the cone (and, orig quirk,
     * even the creative-player exemption) is bypassed.
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false; // orig :989-996
        if (target instanceof SpiderRobot) return false;      // orig :998
        if (target instanceof Spider) return false;           // orig :1001 (EntitySpider)
        if (target instanceof SpiderDriver) return false;     // orig :1004
        if (target instanceof CaveSpider) return false;       // orig :1007 (redundant under Spider, kept as written)
        if (target == this.getFirstPassenger()) return false; // orig :1010
        if (MyUtils.isIgnoreable(target)) return false;       // orig :1013
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig :1016
        // Bearing error to the target (orig :1019-1026; hand-typed pi kept).
        double bearingToTarget = Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
        double facing = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
        double pi = 3.1415926545;
        double bearingError = Math.abs(bearingToTarget - facing) % (pi * 2.0);
        if (bearingError > pi) bearingError -= pi * 2.0;
        bearingError = Math.abs(bearingError);
        // orig :1027-1029 — point-blank targets (< 6 blocks) are always valid;
        // note this runs BEFORE the creative check, so creative players in
        // that range are attacked too (orig bug, kept).
        if (this.distanceToSqr(target) < 36.0) return true;
        if (bearingError > 0.75) return false; // orig :1030-1032
        if (target instanceof Player p) return !p.getAbilities().instabuild; // orig :1033-1036
        return true; // orig :1037
    }

    /**
     * ENT-S-021: orig SpiderRobot.java:896-910 — the stomp. Every suitable
     * entity in the 20x8x20-inflated box is hit in the SAME tick (the loop
     * has no early return); {@link #feetIsSuitableTarget} then keeps only
     * the 12-18 block ring where the feet actually plant.
     */
    private void feetFindSomethingToHit() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return; // orig :897-899
        AABB stompBox = this.getBoundingBox().inflate(20.0, 8.0, 20.0); // orig :900
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, stompBox);
        for (LivingEntity e : entities) {
            if (feetIsSuitableTarget(e)) this.feetAttackEntityAsMob(e);
        }
    }

    /**
     * orig SpiderRobot.java:912-952 — stomp filter: same spider-family and
     * passenger exclusions as the frontal attack but NO ignoreable, line-of-
     * sight or cone checks; instead the center-to-center distance must fall
     * inside the (12, 18) ring (orig :937-946) — under the feet, not the body.
     */
    private boolean feetIsSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false; // orig :913-920
        if (target instanceof SpiderRobot) return false;      // orig :922
        if (target instanceof Spider) return false;           // orig :925
        if (target instanceof SpiderDriver) return false;     // orig :928
        if (target instanceof CaveSpider) return false;       // orig :931
        if (target == this.getFirstPassenger()) return false; // orig :934
        float dx = (float) (target.getX() - this.getX());     // orig :937
        float dy = (float) (target.getY() - this.getY());     // orig :938
        float dz = (float) (target.getZ() - this.getZ());     // orig :939
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 18.0f) return false; // orig :941-943
        if (dist < 12.0f) return false; // orig :944-946
        if (target instanceof Player p) return !p.getAbilities().instabuild; // orig :947-950
        return true; // orig :951
    }

    /**
     * orig SpiderRobot.java:954-969 — the stomp hit: one TENTH of the attack
     * stat (100 / 10 = 10, orig :960) with gentler knockback than the frontal
     * melee (0.6/0.1 vs 1.2/0.15), upward component doubled on kills and
     * against players.
     */
    public boolean feetAttackEntityAsMob(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            double knockbackStrength = 0.6; // orig :957
            double upwardKnockback = 0.1;   // orig :958
            float angleToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX()); // orig :959
            // orig :960 — SpiderRobot_stats.attack / 10.0f; the attribute holds that stat (OreSpawnMain.java:6474).
            boolean ret = livingTarget.hurt(this.damageSources().mobAttack(this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 10.0f);
            if (livingTarget.isRemoved() || livingTarget instanceof Player) upwardKnockback *= 2.0; // orig :961-963
            if (ret) livingTarget.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength); // orig :964-966
            return ret;
        }
        return false;
    }

    /** Live gait-solver state consumed by the model (orig SpiderRobot.java:515-517). */
    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        return renderInfo;
    }

    // ==================== Procedural leg-gait solver ====================
    // Line-by-line port of the original's client-side inverse-kinematics walk:
    // each foot stays planted in the world while the body moves; when a leg's
    // reach, swing angle or elevation leaves its comfort window (or its step
    // scheduler fires), the foot is relocated to fresh ground ahead and the
    // three joint angles converge on the new pose at speed-scaled rates.

    /**
     * Per-leg constants and initial state (orig SpiderRobot.java:111-198).
     * Legs form mirrored pairs (0-1 front, 2-3 mid-front, 4-5 mid-rear,
     * 6-7 rear) with alternating swing-range signs so paired feet step out
     * of phase.
     */
    private void initLegData() {
        for (int leg = 0; leg < LEG_COUNT; ++leg) {
            renderInfo.ycurrentangle[leg] = 0.0f;
            renderInfo.ywantedangle[leg] = 0.0f;
            renderInfo.ydisplayangle[leg] = 0.0f;
            renderInfo.yvelocity[leg] = 0.0f;
            renderInfo.ymid[leg] = 0.0f;
            renderInfo.yoff[leg] = 0.0f;
            renderInfo.yrange[leg] = 0.0f;
            renderInfo.udcurrentangle[leg] = 0.0f;
            renderInfo.udwantedangle[leg] = 0.0f;
            renderInfo.uddisplayangle[leg] = 0.0f;
            renderInfo.udvelocity[leg] = 0.0f;
            // Rest pose: upper segment 45° up, middle level, lower 45° down (orig :127-129).
            renderInfo.p1xangle[leg] = 0.7853981633974483;
            renderInfo.p2xangle[leg] = 0.0;
            renderInfo.p3xangle[leg] = -0.7853981633974483;
            renderInfo.pxvelocity[leg] = 0.0f;
            renderInfo.foot_xpos[leg] = (float) this.getX();
            renderInfo.foot_ypos[leg] = (float) this.getY();
            renderInfo.foot_zpos[leg] = (float) this.getZ();
            renderInfo.realposx[leg] = 0.0f;
            renderInfo.realposy[leg] = 0.0f;
            renderInfo.realposz[leg] = 0.0f;
            renderInfo.legoff[leg] = 0.0f;
            renderInfo.footup[leg] = 1;
            renderInfo.uppoint[leg] = 0.0f;
            renderInfo.footingticker[leg] = 0;
            renderInfo.gpcounter = 0;
            switch (leg) {
                // orig :142-148
                case 0 -> { renderInfo.legoff[leg] = 1.25f; renderInfo.ymid[leg] = -0.32f;      renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 1; renderInfo.yoff[leg] = -0.3f; }
                // orig :149-155
                case 1 -> { renderInfo.legoff[leg] = 1.25f; renderInfo.ymid[leg] = 3.4615927f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 0; renderInfo.yoff[leg] = -0.3f; }
                // orig :156-162
                case 2 -> { renderInfo.legoff[leg] = 2.0f;  renderInfo.ymid[leg] = -1.0f;       renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 3; renderInfo.yoff[leg] = -0.1f; }
                // orig :163-169
                case 3 -> { renderInfo.legoff[leg] = 2.0f;  renderInfo.ymid[leg] = 4.1415925f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 2; renderInfo.yoff[leg] = -0.1f; }
                // orig :170-176
                case 4 -> { renderInfo.legoff[leg] = 1.75f; renderInfo.ymid[leg] = 0.62831855f; renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 5; renderInfo.yoff[leg] = -0.3f; }
                // orig :177-183
                case 5 -> { renderInfo.legoff[leg] = 1.75f; renderInfo.ymid[leg] = 2.5132742f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 4; renderInfo.yoff[leg] = -0.3f; }
                // orig :184-190
                case 6 -> { renderInfo.legoff[leg] = 3.4f;  renderInfo.ymid[leg] = 1.05f;       renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 7; renderInfo.yoff[leg] = -0.1f; }
                // orig :191-196
                case 7 -> { renderInfo.legoff[leg] = 3.4f;  renderInfo.ymid[leg] = 2.0915928f;  renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 6; renderInfo.yoff[leg] = -0.1f; }
                default -> { }
            }
        }
    }

    /**
     * Speed-scaled angular-velocity controller (orig SpiderRobot.java:200-238).
     * Body speed is scaled ×8 and clamped to [1,4]; the joint accelerates by
     * 0.25°·scale per tick toward the target, snaps to fixed 0.5°/1°·scale
     * rates inside the 2°/4°·scale approach windows, caps at 4°·scale, and
     * stops dead inside the 0.5°·scale deadband. Mirrored for negative error.
     *
     * @param bodySpeed       blocks moved last tick (drives how fast joints may move)
     * @param angleError      remaining angle to the target, radians (sign = direction)
     * @param currentVelocity the joint's angular velocity from last tick
     * @return the new angular velocity, radians/tick
     */
    private float getNewVelocity(float bodySpeed, float angleError, float currentVelocity) {
        float scale = bodySpeed * 8.0f;
        if (scale < 1.0f) {
            scale = 1.0f;
        }
        if (scale > 4.0f) {
            scale = 4.0f;
        }
        if (angleError > 0.0f) {
            if ((double) angleError < Math.PI / 360 * (double) scale) {
                currentVelocity = 0.0f;
            } else {
                currentVelocity = (float) ((double) currentVelocity + 0.004363323129985824 * (double) scale);
                if ((double) angleError < 0.06981317007977318 * (double) scale) {
                    currentVelocity = (float) (Math.PI / 180 * (double) scale);
                }
                if ((double) angleError < Math.PI / 90 * (double) scale) {
                    currentVelocity = (float) (Math.PI / 360 * (double) scale);
                }
                if ((double) currentVelocity > 0.06981317007977318 * (double) scale) {
                    currentVelocity = (float) (0.06981317007977318 * (double) scale);
                }
            }
        } else if ((double) angleError > -Math.PI / 360 * (double) scale) {
            currentVelocity = 0.0f;
        } else {
            currentVelocity = (float) ((double) currentVelocity - 0.004363323129985824 * (double) scale);
            if ((double) angleError > -0.06981317007977318 * (double) scale) {
                currentVelocity = -((float) (Math.PI / 180 * (double) scale));
            }
            if ((double) angleError > -Math.PI / 90 * (double) scale) {
                currentVelocity = -((float) (Math.PI / 360 * (double) scale));
            }
            if ((double) currentVelocity < -0.06981317007977318 * (double) scale) {
                currentVelocity = -((float) (0.06981317007977318 * (double) scale));
            }
        }
        return currentVelocity;
    }

    /**
     * One solver step (orig SpiderRobot.java:240-379). Client-only, exactly
     * like the original's {@code isRemote} early-out: the gait is a visual
     * that reads the entity's already-synced position each frame.
     *
     * <p>Per leg: advance the step scheduler, recompute the hip socket's
     * world position, relocate the foot if out of range/angle/elevation
     * (294 &gt; reach×16 &gt; 32, yaw beyond range×8/7, |elevation| &gt; 1.25,
     * or scheduler fired), then converge segment fold, elevation and yaw on
     * the planted foot with {@link #getNewVelocity}. When all three axes
     * settle the foot is "down" and — a ridden robot with mobGriefing on —
     * tramples tall grass to air and grass blocks to dirt at the foot
     * (orig :370-377; client-local in the original too, since this code
     * never ran server-side).</p>
     */
    public void updateLegs() {
        if (!this.level().isClientSide()) {
            return;
        }
        // orig :244-247 — yaw normalized into [0,360) before the trig below.
        float normalizedYaw = this.getYRot() % 360.0f;
        while (normalizedYaw < 0.0f) {
            normalizedYaw += 360.0f;
        }
        this.setYRot(normalizedYaw);
        ++renderInfo.gpcounter;
        if (!this.legDataInitialized) {
            this.legDataInitialized = true;
            this.initLegData();
        }
        float deltaX = (float) (this.xo - this.getX());
        float deltaY = (float) (this.yo - this.getY());
        float deltaZ = (float) (this.zo - this.getZ());
        float bodySpeed = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        for (int leg = 0; leg < LEG_COUNT; ++leg) {
            int settledAxes = 0;
            renderInfo.footingticker[leg] = renderInfo.footingticker[leg] + 1;
            // Hip socket world position (orig :263-265).
            renderInfo.realposx[leg] = (float) (this.getX() - (double) renderInfo.legoff[leg]
                    * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
            renderInfo.realposz[leg] = (float) (this.getZ() + (double) renderInfo.legoff[leg]
                    * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
            renderInfo.realposy[leg] = (float) this.getY() + renderInfo.yoff[leg];
            // Alternating-pair step scheduler (orig :266-269): whichever paired
            // foot has been planted longer steps once their combined age passes 50.
            int pairTickerSum = renderInfo.footingticker[leg] + renderInfo.footingticker[renderInfo.pairedwith[leg]];
            if (pairTickerSum > 50 && renderInfo.footingticker[leg] > renderInfo.footingticker[renderInfo.pairedwith[leg]]) {
                renderInfo.footingticker[leg] = 0;
            }
            deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
            deltaY = renderInfo.realposy[leg] - renderInfo.foot_ypos[leg];
            deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
            float footDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            footDistance *= 16.0f;
            // Yaw deviation from the leg's neutral direction (orig :275-282).
            float yawDeviation = (float) (Math.abs((double) renderInfo.ycurrentangle[leg]
                    - (Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) + (double) renderInfo.ymid[leg])) % (Math.PI * 2));
            if ((double) yawDeviation > Math.PI) {
                yawDeviation = (float) ((double) yawDeviation - Math.PI * 2);
            }
            if ((double) yawDeviation < -Math.PI) {
                yawDeviation = (float) ((double) yawDeviation + Math.PI * 2);
            }
            yawDeviation = Math.abs(yawDeviation);
            // Foot relocation triggers (orig :283-290).
            if (footDistance > 294.0f || footDistance < 32.0f
                    || yawDeviation > Math.abs(renderInfo.yrange[leg]) * 8.0f / 7.0f
                    || (double) Math.abs(renderInfo.udcurrentangle[leg]) > 1.25
                    || renderInfo.footingticker[leg] == 0) {
                this.findNewFooting(leg);
                deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
                deltaY = renderInfo.realposy[leg] - renderInfo.foot_ypos[leg];
                deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
                footDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                footDistance *= 16.0f;
            }
            // Segment fold: leg reach (3 × 99px segments projected onto the
            // fold angle) converges on the foot distance (orig :291-303).
            float segment1Reach = (float) (99.0 * Math.cos(renderInfo.p2xangle[leg] - renderInfo.p1xangle[leg]));
            float segment2Reach = 99.0f;
            float segment3Reach = (float) (99.0 * Math.cos(renderInfo.p2xangle[leg] - renderInfo.p3xangle[leg]));
            float totalReach = segment1Reach + segment2Reach + segment3Reach;
            float reachError = totalReach - footDistance;
            renderInfo.pxvelocity[leg] = this.getNewVelocity(bodySpeed, (float) ((double) reachError * Math.PI / 360.0), renderInfo.pxvelocity[leg]);
            if (renderInfo.pxvelocity[leg] == 0.0f || Math.abs(reachError) < 8.0f) {
                ++settledAxes;
            }
            renderInfo.p1xangle[leg] = renderInfo.p1xangle[leg] + (double) renderInfo.pxvelocity[leg];
            renderInfo.p2xangle[leg] = 0.0;
            renderInfo.p3xangle[leg] = -renderInfo.p1xangle[leg];
            // Elevation: tilt toward the planted foot, or toward the mid-step
            // lift point while the foot travels (orig :304-334).
            float elevationTarget = renderInfo.uppoint[leg] != 0.0f
                    ? (float) Math.atan2(footDistance, (double) (renderInfo.realposy[leg] - renderInfo.uppoint[leg]) * 16.0)
                    : (float) Math.atan2(footDistance, (double) (renderInfo.realposy[leg] - renderInfo.foot_ypos[leg]) * 16.0);
            renderInfo.udwantedangle[leg] = (float) ((double) elevationTarget - 1.5707963267948966);
            while ((double) renderInfo.udwantedangle[leg] > Math.PI) {
                renderInfo.udwantedangle[leg] = (float) ((double) renderInfo.udwantedangle[leg] - Math.PI * 2);
            }
            while ((double) renderInfo.udwantedangle[leg] < -Math.PI) {
                renderInfo.udwantedangle[leg] = (float) ((double) renderInfo.udwantedangle[leg] + Math.PI * 2);
            }
            double targetHeading = renderInfo.udwantedangle[leg];
            double currentHeading = renderInfo.udcurrentangle[leg];
            double headingError = (targetHeading - currentHeading) % (Math.PI * 2);
            while (headingError > Math.PI) {
                headingError -= Math.PI * 2;
            }
            while (headingError < -Math.PI) {
                headingError += Math.PI * 2;
            }
            renderInfo.udvelocity[leg] = this.getNewVelocity(bodySpeed * 2.0f, (float) headingError, renderInfo.udvelocity[leg]);
            if (renderInfo.udvelocity[leg] == 0.0f || Math.abs(headingError) < Math.PI / 90) {
                renderInfo.uppoint[leg] = 0.0f;
                ++settledAxes;
            }
            currentHeading += (double) renderInfo.udvelocity[leg];
            while (currentHeading > Math.PI) {
                currentHeading -= Math.PI * 2;
            }
            while (currentHeading < -Math.PI) {
                currentHeading += Math.PI * 2;
            }
            renderInfo.uddisplayangle[leg] = renderInfo.udcurrentangle[leg] = (float) currentHeading;
            // Yaw: swing toward the planted foot's world bearing (orig :335-369).
            deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
            deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
            renderInfo.ywantedangle[leg] = (float) Math.atan2(deltaZ, deltaX);
            targetHeading = renderInfo.ywantedangle[leg];
            currentHeading = renderInfo.ycurrentangle[leg];
            headingError = (targetHeading - currentHeading) % (Math.PI * 2);
            if (headingError > Math.PI) {
                headingError -= Math.PI * 2;
            }
            if (headingError < -Math.PI) {
                headingError += Math.PI * 2;
            }
            renderInfo.yvelocity[leg] = this.getNewVelocity(bodySpeed, (float) headingError, renderInfo.yvelocity[leg]);
            if (renderInfo.yvelocity[leg] == 0.0f || Math.abs(headingError) < Math.PI / 90) {
                ++settledAxes;
            }
            renderInfo.ycurrentangle[leg] = renderInfo.ycurrentangle[leg] + renderInfo.yvelocity[leg];
            while ((double) renderInfo.ycurrentangle[leg] > Math.PI) {
                renderInfo.ycurrentangle[leg] = (float) ((double) renderInfo.ycurrentangle[leg] - Math.PI * 2);
            }
            while ((double) renderInfo.ycurrentangle[leg] < -Math.PI) {
                renderInfo.ycurrentangle[leg] = (float) ((double) renderInfo.ycurrentangle[leg] + Math.PI * 2);
            }
            float displayYaw = (float) ((double) renderInfo.ycurrentangle[leg]
                    - Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) - 1.5707963267948966);
            while ((double) displayYaw > Math.PI) {
                displayYaw = (float) ((double) displayYaw - Math.PI * 2);
            }
            while ((double) displayYaw < -Math.PI) {
                displayYaw = (float) ((double) displayYaw + Math.PI * 2);
            }
            renderInfo.ydisplayangle[leg] = displayYaw;
            if (settledAxes != 3) continue;
            // All three axes settled → foot lands (orig :370-377). The grass
            // trample below ran in this client-only path in 1.7.10 as well,
            // so the change is client-local there and here alike.
            renderInfo.footup[leg] = 0;
            // orig :372 int-truncates the foot coordinates (differs from flooring at negatives).
            BlockPos footPos = new BlockPos((int) renderInfo.foot_xpos[leg], (int) renderInfo.foot_ypos[leg], (int) renderInfo.foot_zpos[leg]);
            boolean griefing = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            if (this.level().getBlockState(footPos).is(Blocks.SHORT_GRASS) && this.getFirstPassenger() != null && griefing) {
                this.level().setBlock(footPos, Blocks.AIR.defaultBlockState(), 3);
            }
            BlockPos belowFoot = footPos.below();
            if (this.level().getBlockState(belowFoot).is(Blocks.GRASS_BLOCK) && this.getFirstPassenger() != null && griefing) {
                this.level().setBlock(belowFoot, Blocks.DIRT.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Plants the foot of leg {@code leg} on fresh ground (orig
     * SpiderRobot.java:381-486). Aims a probe ahead of the hip along the
     * leg's neutral direction, biased by the swing range (flipped when the
     * body moves against its facing, zeroed while turning); sweeps the reach
     * from 16 (10 for rear legs) down to 3.5 blocks, scanning an 11-up/14-down
     * column with a ±1 spread at each stop for solid ground; if nothing is
     * found the sweep repeats un-biased with a ±3 spread; the final fallback
     * hangs the foot half-reach ahead at hip height minus one. A relocated
     * airborne foot gets a mid-step lift point above the travel midpoint that
     * grows with step length (+1 above 3px, +1.5 above 48px, +1.5 more
     * above 100px).
     */
    private void findNewFooting(int leg) {
        float reach = 16.0f;
        boolean found = false;
        double headingRad = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
        // orig :391 — the original's slightly-off hand-typed PI, kept for exactness.
        double pi = 3.1415926545;
        renderInfo.footingticker[leg] = 0;
        float deltaX = (float) (this.getX() - this.xo);
        float deltaZ = (float) (this.getZ() - this.zo);
        double travelHeading = Math.atan2(deltaZ, deltaX);
        double travelSpeed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double headingDiff = Math.abs(travelHeading - headingRad) % (pi * 2.0);
        if (headingDiff > pi) {
            headingDiff -= pi * 2.0;
        }
        headingDiff = Math.abs(headingDiff);
        if (Math.abs(travelSpeed) < 0.01) {
            headingDiff = 0.0;
        }
        float swingBias = renderInfo.yrange[leg];
        swingBias *= 0.875f;
        if (Math.abs((this.yRotO - this.getYRot()) % 360.0f) > 0.75f) {
            swingBias = 0.0f;
        }
        if (leg >= 4) {
            reach = 10.0f;
        }
        // Moving backward relative to facing: step behind instead (orig :413-419).
        if (headingDiff > 1.5) {
            swingBias = -swingBias;
            reach = 10.0f;
            if (leg >= 4) {
                reach = 16.0f;
            }
        }
        float fallbackX = (float) ((double) renderInfo.realposx[leg] - (double) (reach / 2.0f)
                * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
        float fallbackZ = (float) ((double) renderInfo.realposz[leg] + (double) (reach / 2.0f)
                * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
        float fallbackY = renderInfo.realposy[leg] - 1.0f;
        float footX = fallbackX;
        float footY = fallbackY;
        float footZ = fallbackZ;
        float initialReach = reach;
        int spread = 1;
        while (!found && reach > 3.5f) {
            footX = (float) ((double) renderInfo.realposx[leg] - (double) reach
                    * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg] - (double) swingBias));
            footZ = (float) ((double) renderInfo.realposz[leg] + (double) reach
                    * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg] - (double) swingBias));
            footY = renderInfo.realposy[leg];
            for (int yScan = 11; !found && yScan > -14; --yScan) {
                for (int xSpread = -spread; !found && xSpread <= spread; ++xSpread) {
                    for (int zSpread = -spread; !found && zSpread <= spread; ++zSpread) {
                        BlockPos probe = BlockPos.containing((int) footX + xSpread, (int) footY + yScan, (int) footZ + zSpread);
                        // orig :432-433 — non-air with a solid material.
                        if (this.level().getBlockState(probe).isAir() || !this.level().getBlockState(probe).isSolid()) continue;
                        footY += (float) (yScan + 1);
                        footX += (float) xSpread;
                        footZ += (float) zSpread;
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                deltaX = renderInfo.realposx[leg] - footX;
                float deltaY = renderInfo.realposy[leg] - footY;
                deltaZ = renderInfo.realposz[leg] - footZ;
                float footingDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                if ((footingDistance *= 16.0f) > 294.0f) {
                    found = false;
                }
            }
            // Second pass: bias removed, spread widened to ±3 (orig :451-455).
            if (!((reach -= 1.0f) < 3.5f) || swingBias == 0.0f) continue;
            swingBias = 0.0f;
            spread = 3;
            reach = initialReach;
        }
        if (!found) {
            footX = fallbackX;
            footY = fallbackY;
            footZ = fallbackZ;
        }
        float previousFootX = renderInfo.foot_xpos[leg];
        float previousFootY = renderInfo.foot_ypos[leg];
        float previousFootZ = renderInfo.foot_zpos[leg];
        renderInfo.foot_xpos[leg] = footX;
        renderInfo.foot_ypos[leg] = footY;
        renderInfo.foot_zpos[leg] = footZ;
        // Mid-step lift point for a foot that was down (orig :467-485).
        if (renderInfo.footup[leg] == 0) {
            renderInfo.footup[leg] = 1;
            deltaX = previousFootX - footX;
            float deltaY = previousFootY - footY;
            deltaZ = previousFootZ - footZ;
            float stepLength = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            stepLength *= 16.0f;
            float liftPoint = (previousFootY + footY) / 2.0f;
            if (stepLength > 3.0f) {
                liftPoint += 1.0f;
            }
            if (stepLength > 48.0f) {
                liftPoint += 1.5f;
            }
            if (stepLength > 100.0f) {
                liftPoint += 1.5f;
            }
            renderInfo.uppoint[leg] = liftPoint;
        }
    }
}
