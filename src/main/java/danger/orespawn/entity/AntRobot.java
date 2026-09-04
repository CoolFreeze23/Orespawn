package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.entity.client.RenderSpiderRobotInfo;
import danger.orespawn.entity.gait.AntRigProfile;
import danger.orespawn.entity.gait.ModernSpiderGait;
import danger.orespawn.util.MyUtils;
import de.dertoaster.multihitboxlib.api.ICustomHitboxProfileSupplier;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;

import java.util.Optional;

public class AntRobot extends Mob implements ICustomHitboxProfileSupplier, IModernLeggedRobot {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ROBOTSPIDER = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider"));
    private static final SoundEvent SND_ROBOTSPIDERMOUNT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspidermount"));

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(AntRobot.class, EntityDataSerializers.INT);
    /**
     * 2.0 S5b: the server's construction-time spiderMovement snapshot for
     * the ANT, synced so clients build the replay controller and parts off
     * the server's decision, never their own config — the exact
     * DATA_MODERN_GAIT pattern from SpiderRobot (S2/S4 review lineage).
     * The snapshot reads the EFFECTIVE mode,
     * {@link OreSpawnConfig#spiderMovement()} ([modern] enabled master
     * override, ruling 2026-09-04): CLASSIC while the master is off.
     */
    private static final EntityDataAccessor<Boolean> DATA_MODERN_GAIT =
            SynchedEntityData.defineId(AntRobot.class, EntityDataSerializers.BOOLEAN);

    private static final double CHASE_SPEED = 0.2;
    private static final double KNOCKBACK_HORIZONTAL = 0.7;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;
    private static final double STOMP_KNOCKBACK = 0.6;
    private static final float PARTICLE_OFFSET_BLOCKS = 4.0f;

    private static final int LEG_COUNT = 6;
    private final RenderSpiderRobotInfo renderInfo = new RenderSpiderRobotInfo(LEG_COUNT);
    /** Lazy one-shot leg-data (re)initialization flag (orig {@code didonce}, AntRobot.java:47). */
    private boolean legDataInitialized = false;

    /** orig AntRobot.java:43 — built but never consulted (the orig scan iterates unsorted); kept unused for parity (TF-035). */
    private final GenericTargetSorter targetSorter;
    private final float moveSpeed = 0.3f;
    private int playing = 0;
    private int rideTicker = 0;
    private int owned = 0;

    /**
     * 2.0 S5b: the modern gait controller (ant rig), or {@code null} in
     * classic mode — the SpiderRobot S2 snapshot pattern verbatim: server
     * fixes it at construction, clients materialize lazily off the synced
     * flag. Classic ants never construct modern state; the classic leg
     * solver below is untouched.
     */
    private ModernSpiderGait modernGait;
    /** 2.0 S5b: true once this ctor's body has run (see SpiderRobot's twin field). */
    private boolean movementModeDecided;
    /** The one ctor-tail config read (server); null until the supplier ran. */
    private Boolean ctorTailModernDecision;

    public AntRobot(EntityType<? extends AntRobot> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.targetSorter = new GenericTargetSorter(this); // orig AntRobot.java:54
        // orig AntRobot.java:532-535 — entityInit primes the leg data once at construction.
        initLegData();
        // 2.0 S5b: consume the single ctor-tail mode read (the profile
        // supplier may have run from the LivingEntity ctor tail before this
        // body) — the SpiderRobot ctor-tear rule, applied identically.
        if (!level.isClientSide()) {
            boolean modern = this.ctorTailModernDecision != null
                    ? this.ctorTailModernDecision
                    : OreSpawnConfig.spiderMovement() == OreSpawnConfig.SpiderMovement.MODERN;
            this.entityData.set(DATA_MODERN_GAIT, modern);
            if (modern) {
                this.modernGait = new ModernSpiderGait(AntRigProfile.RIG);
            }
        }
        this.movementModeDecided = true;
    }

    /**
     * 2.0 S5b — the MHLib part gate for the ant (S4 pattern: this SHADOWS
     * IMultipartEntity's entire profile resolution and must return the REAL
     * profile for modern ants, Optional.empty() for classic — never null).
     * The profile's main size is EXACTLY [2.75, 1.25]: MHLib applies it via
     * the EntityEvent.Size hook and i083 pins the ant's classic dims.
     */
    @Override
    public Optional<HitboxProfile> getHitboxProfile() {
        final boolean modern;
        if (this.level().isClientSide()) {
            modern = this.entityData.get(DATA_MODERN_GAIT);
        } else if (this.movementModeDecided) {
            modern = this.modernGait != null;
        } else {
            if (this.ctorTailModernDecision == null) {
                this.ctorTailModernDecision =
                        OreSpawnConfig.spiderMovement() == OreSpawnConfig.SpiderMovement.MODERN;
            }
            modern = this.ctorTailModernDecision;
        }
        if (!modern) {
            return Optional.empty();
        }
        return MHLibDatapackLoaders.getHitboxProfile(this.getType(), this.level().registryAccess());
    }

    /**
     * 2.0 S5b: the client part build on the synced flag's arrival — S4's
     * id-capture/restore so the setId cascade gives the parts
     * syncedId+1..+6 (the server's part ids), then client pick
     * registration. See SpiderRobot.onSyncedDataUpdated for the review
     * lineage (building lazily instead cost the network id).
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

    /** True when this ant runs the modern gait (server snapshot; synced). */
    @Override
    public boolean isModernMovement() {
        return this.level().isClientSide() ? this.entityData.get(DATA_MODERN_GAIT) : this.modernGait != null;
    }

    /** The modern gait controller, or {@code null} for classic (client-lazy, as on the spider). */
    @Override
    public ModernSpiderGait getModernGait() {
        if (this.modernGait == null && this.level().isClientSide() && this.entityData.get(DATA_MODERN_GAIT)) {
            this.modernGait = new ModernSpiderGait(AntRigProfile.RIG);
        }
        return this.modernGait;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6475 — AntRobot 300 HP / 30 ATK / 16 armor;
        // speed 0.3 matches orig AntRobot.java:44.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ANT_ROBOT.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ANT_ROBOT.attackDamage())
                .add(Attributes.ARMOR, MobStats.ANT_ROBOT.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_MODERN_GAIT, false);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    public void setOwned() { this.owned = 1; }
    public int getOwned() { return this.owned; }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.getFirstPassenger() != null) return;
        super.customServerAiStep();

        // orig AntRobot.java:105 — `owned == 0 && difficulty != PEACEFUL` gates the whole unridden
        // block: the stomp roll, the target release, the hunt and the melee all sit inside (ENT-S-114).
        if (this.owned == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            if (this.getRandom().nextInt(20) == 0) {
                feetFindSomethingToHit();
            }
            LivingEntity currentTarget = this.getTarget();
            if (this.getRandom().nextInt(150) == 0) this.setTarget(null);
            if (currentTarget != null && !currentTarget.isAlive()) {
                this.setTarget(null);
                currentTarget = null;
            }
            if (currentTarget == null) currentTarget = findSomethingToAttack();
            if (currentTarget != null) {
                this.lookAt(currentTarget, 10.0f, 10.0f);
                if (this.distanceToSqr(currentTarget) > 16.0) {
                    double deltaZ = currentTarget.getZ() - this.getZ();
                    double deltaX = currentTarget.getX() - this.getX();
                    double yawToTarget = Math.atan2(deltaZ, deltaX);
                    this.setDeltaMovement(
                            CHASE_SPEED * Math.cos(yawToTarget),
                            this.getDeltaMovement().y,
                            CHASE_SPEED * Math.sin(yawToTarget));
                }
                // orig AntRobot.java:130-145 — melee only attempted on a
                // 1-in-15 roll per tick, not every tick.
                if (this.getRandom().nextInt(15) == 0) {
                    double meleeRange = (6.0f + currentTarget.getBbWidth() / 2.0f);
                    if (this.distanceToSqr(currentTarget) < meleeRange * meleeRange) {
                        this.setAttacking(1);
                        this.doHurtTarget(currentTarget);
                    } else {
                        this.setAttacking(0);
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")
                || source.getMsgId().equals("inFire") || source.getMsgId().equals("onFire")
                || source.getMsgId().equals("magic") || source.getMsgId().equals("starve")) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            this.setTarget(livingAttacker);
            this.lookAt(attacker, 20.0f, 20.0f);
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

        // orig AntRobot.java:617-619 — while ridden, 1-in-50 stomp around the feet; `difficulty
        // != PEACEFUL` leads the condition (:617), ahead of the client and rider tests (ENT-S-114).
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && !this.level().isClientSide()
                && this.getFirstPassenger() != null && this.getRandom().nextInt(50) == 0) {
            feetFindSomethingToHit();
        }
        // orig AntRobot.java:620-631 — while ridden, 1-in-9 hunt and melee; `difficulty != PEACEFUL`
        // leads the condition (:620) (ENT-S-114).
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && !this.level().isClientSide()
                && this.getFirstPassenger() != null && this.getRandom().nextInt(9) == 0) {
            LivingEntity riderTarget = findSomethingToAttack();
            if (riderTarget != null) {
                double meleeRange = (6.0f + riderTarget.getBbWidth() / 2.0f);
                if (this.distanceToSqr(riderTarget) < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    this.doHurtTarget(riderTarget);
                }
            } else {
                this.setAttacking(0);
            }
        }

        float particleOffsetX = (float) (PARTICLE_OFFSET_BLOCKS * Math.cos(Math.toRadians(this.getYRot() - 80.0f)));
        float particleOffsetZ = (float) (PARTICLE_OFFSET_BLOCKS * Math.sin(Math.toRadians(this.getYRot() - 80.0f)));
        if (this.level().isClientSide()) {
            // orig AntRobot.java:740 — the leg solver steps once per client
            // tick. 2.0 S5b: modern-mode ants replay the server gait instead
            // (mode from the synced flag); the classic branch is untouched.
            if (isModernMovement()) {
                getModernGait().clientTick(this);
            } else {
                updateLegs();
            }
            if (this.getRandom().nextInt(18) == 0) {
                this.level().addParticle(ParticleTypes.FLAME,
                        getX() + particleOffsetX, getY() + 0.5, getZ() + particleOffsetZ, 0, 0, 0);
            }
            if (this.getRandom().nextInt(7) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        getX() + particleOffsetX, getY() + 0.5, getZ() + particleOffsetZ, 0, 0, 0);
            }
        }

        // 2.0 S5b: the server-authoritative modern gait. Never moves the
        // body and never touches the hover-ride physics — a hovering body
        // whose scan window misses the ground STRANDS its legs, and the
        // stranded dangle IS the designed hover look (S5 as-designed).
        if (this.modernGait != null && !this.level().isClientSide()) {
            this.modernGait.serverTick(this);
        }

        if (this.playing > 0) --this.playing;
        if (this.getFirstPassenger() != null && this.playing == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(SND_ROBOTSPIDER, 0.35f, 1.0f);
            this.playing = 125;
        }
        this.rideTicker += this.getRandom().nextInt(3);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            float yawToTarget = (float) Math.atan2(livingTarget.getZ() - this.getZ(), livingTarget.getX() - this.getX());
            // orig AntRobot.java:1079 — melee damage = the attack attribute, not a literal.
            boolean hurtApplied = livingTarget.hurt(this.damageSources().mobAttack(this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
            double verticalKnockback = KNOCKBACK_VERTICAL;
            if (livingTarget.isRemoved() || livingTarget instanceof Player) {
                verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
            }
            if (hurtApplied) {
                livingTarget.push(
                        Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                        verticalKnockback,
                        Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
            }
            return hurtApplied;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.owned == 0) return InteractionResult.PASS;
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
            this.playSound(SND_ROBOTSPIDERMOUNT, 0.45f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    // ==================== Riding + hover physics (ENT-A-016) ====================

    /** orig AntRobot.java:544-546 — the chassis cannot be shoved; vanilla pushing would fight the hover model. */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** Any mounted player steers (orig AntRobot.java:929-935 — mounting is gated only by owned != 0 in mobInteract). */
    @javax.annotation.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player) {
            return player;
        }
        return super.getControllingPassenger();
    }

    /**
     * Seat: 1.25 blocks behind center with a ±0.05 fore-aft bob (orig
     * AntRobot.java:552-558), height 0.55 with a ±0.02 vertical bob (orig
     * :548-550), both driven by {@code rideTicker}. TF-029 player-offset
     * convention (see Elevator): the original added rider.getYOffset() and
     * setPosition subtracted yOffset again, so a 1.7.10 player netted
     * mountedYOffset - 0.5 while non-players rode the full 0.55.
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) return;
        float f = -1.25f;
        f = (float) ((double) f + Math.cos((float) this.rideTicker * 0.33f) * 0.05);
        double seatY = 0.55 + Math.cos((float) this.rideTicker * 0.19f) * 0.02;
        if (passenger instanceof Player) seatY -= 0.5;
        double seatX = this.getX() - (double) f * Math.sin(Math.toRadians(this.getYRot()));
        double seatZ = this.getZ() + (double) f * Math.cos(Math.toRadians(this.getYRot()));
        // S7a (sitting OBS-1 → modern-only fix, same mechanism as the
        // spider's): MODERN raises the seat onto the visual back (+0.9,
        // MOD-027) and composes it through the body dynamics; CLASSIC
        // keeps the faithful 1.0 seat (rider inside the shell) untouched.
        danger.orespawn.entity.gait.ModernSpiderGait gait =
                this.isModernMovement() ? this.getModernGait() : null;
        if (gait != null) {
            double[] seat = {seatX - this.getX(), seatY + 0.9, seatZ - this.getZ()};
            danger.orespawn.entity.gait.ModernSpiderGait.bodyTransform(this.getYRot(),
                    gait.bodyPitch(), gait.bodyRoll(), gait.bodyLift(), seat);
            move.accept(passenger,
                    this.getX() + seat[0], this.getY() + seat[1], this.getZ() + seat[2]);
            return;
        }
        move.accept(passenger, seatX, this.getY() + seatY, seatZ);
    }

    /** orig AntRobot.java:710,746,757,776 — air and liquids (water/lava, both flow states) give no hover support. */
    private boolean givesHoverSupport(BlockState state) {
        return !state.isAir() && !state.is(Blocks.WATER) && !state.is(Blocks.LAVA);
    }

    /**
     * The original's ridden hover/walk physics (orig AntRobot.java:659-877,
     * server ridden branch :743-872), run on the controlling client like the
     * Elevator/B3 mounts: the riding client integrates the move and syncs
     * position while the server keeps the stomp/melee side effects in
     * {@link #tick}. Order preserved: motion clamps, ground hover,
     * obstruction climb, yaw chase, heading sign, throttle, first move with
     * 0.98 friction, second move with 0.8/0.98/0.8 friction — the original
     * really did integrate the ridden motion twice per tick (:864 and :869),
     * which is why the ride outruns the nominal 0.3 cap; kept as ride feel.
     */
    @Override
    protected void tickRidden(Player rider, Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        if (!this.isControlledByLocalInstance()) return;

        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        // orig :661 — tick-start speed drives the obstruction depth and yaw
        // lag; read before the clamps below.
        double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);

        // orig :675-692 — vertical ±0.85, horizontal ±1.25 hard clamps.
        if (motionY > 0.85) motionY = 0.85;
        if (motionY < -0.85) motionY = -0.85;
        if (motionX < -1.25) motionX = -1.25;
        if (motionX > 1.25) motionX = 1.25;
        if (motionZ < -1.25) motionZ = -1.25;
        if (motionZ > 1.25) motionZ = 1.25;

        // orig :743-751 — ridden ground hover: solid ground 2.25 below lifts
        // motion 0.06 and position 0.03; air or liquid sinks 0.02.
        BlockPos under = new BlockPos((int) this.getX(), (int) ((float) this.getY() - 2.25f), (int) this.getZ());
        if (givesHoverSupport(this.level().getBlockState(under))) {
            motionY += 0.06;
            this.setPos(this.getX(), this.getY() + 0.03, this.getZ());
        } else {
            motionY -= 0.02;
        }

        // orig :766-782 — obstruction climb: a wedge of probes ahead (depth
        // 3 + velocity*6, radius up to dist*2, arc ±90° in 30° steps), each
        // solid block adds 0.02; the total lifts motion AND position ×0.05 so
        // the ant walks up terrain instead of stalling against it.
        double obstructionFactor = 0.0;
        int dist = 3 + (int) (velocity * 6.0);
        for (int k = 1; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                for (int j = -90; j <= 90; j += 30) {
                    double dx = (double) i * Math.cos(Math.toRadians(this.getYRot() + 90.0f + (float) j));
                    double dz = (double) i * Math.sin(Math.toRadians(this.getYRot() + 90.0f + (float) j));
                    BlockPos probe = new BlockPos((int) (this.getX() + dx), (int) this.getY() - k, (int) (this.getZ() + dz));
                    if (!givesHoverSupport(this.level().getBlockState(probe))) continue;
                    obstructionFactor += 0.02;
                }
            }
        }
        motionY += obstructionFactor * 0.05;
        this.setPos(this.getX(), this.getY() + obstructionFactor * 0.05, this.getZ());

        // orig :783-815 — yaw chases the rider's yaw with a speed-dependent
        // lag (|1.85 - v| clamped 0.01..0.9); pitch pinned to 0.
        double riderYaw = rider.getYRot() % 360.0;
        while (riderYaw < 0.0) riderYaw += 360.0;
        double bodyYaw = this.getYRot() % 360.0;
        while (bodyYaw < 0.0) bodyYaw += 360.0;
        double relativeYaw = (riderYaw - bodyYaw) % 180.0;
        while (relativeYaw < 0.0) relativeYaw += 180.0;
        if (relativeYaw > 90.0) relativeYaw -= 180.0;
        if (velocity > 0.01) {
            double turnLag = Math.abs(1.85 - velocity);
            if (turnLag < 0.01) turnLag = 0.01;
            if (turnLag > 0.9) turnLag = 0.9;
            this.setYRot(rider.getYRot() + (float) (relativeYaw * turnLag));
        } else {
            this.setYRot(rider.getYRot());
        }
        this.setXRot(0.0f);
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        // orig :816-834 — sign the scalar speed against the RIDER's facing:
        // motion running opposite the rider means the ant is backing up.
        double max_speed = 0.3; // orig :666
        double newVelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double motionHeading = Math.atan2(motionZ, motionX);
        double riderHeading = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        // orig :821 — the original's slightly-off hand-typed PI, kept for exactness.
        double pi = 3.1415926545;
        double headingDiff = Math.abs(motionHeading - riderHeading) % (pi * 2.0);
        if (headingDiff > pi) headingDiff -= pi * 2.0;
        headingDiff = Math.abs(headingDiff);
        if (Math.abs(newVelocity) < 0.01) headingDiff = 0.0;
        if (headingDiff > 1.5) newVelocity = -newVelocity;

        // orig :835-863 — throttle ±0.05 per tick, caps 0.3 forward / 0.25
        // reverse; project the scalar onto the ant's facing (+90 forward,
        // +270 reverse).
        float forwardInput = rider.zza;
        if (Math.abs(forwardInput) > 0.001f) {
            double deltav;
            if (forwardInput > 0.0f) {
                deltav = 0.05;
            } else {
                max_speed = 0.25;
                deltav = -0.05;
            }
            newVelocity += deltav;
            if (newVelocity >= 0.0) {
                if (newVelocity > max_speed) newVelocity = max_speed;
                motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newVelocity;
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newVelocity;
            } else {
                if (newVelocity < -max_speed) newVelocity = -max_speed;
                newVelocity = -newVelocity;
                motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * newVelocity;
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * newVelocity;
            }
        } else if (newVelocity >= 0.0) {
            motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newVelocity;
            motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newVelocity;
        } else {
            motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * (newVelocity * -1.0);
            motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * (newVelocity * -1.0);
        }

        // orig :864-867 — first integration, then 0.98 friction on all axes.
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 afterFirst = this.getDeltaMovement();
        this.setDeltaMovement(afterFirst.x * 0.98, afterFirst.y * 0.98, afterFirst.z * 0.98);

        // orig :869-872 — the shared tail ran again while ridden: a second
        // integration with 0.8/0.98/0.8 friction. Original quirk, kept.
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 afterSecond = this.getDeltaMovement();
        this.setDeltaMovement(afterSecond.x * 0.8, afterSecond.y * 0.98, afterSecond.z * 0.8);
        this.calculateEntityAnimation(false);
    }

    /**
     * Riderless server physics: vanilla living movement first (orig
     * AntRobot.java:672-674 ran super.onLivingUpdate), then the motion clamps
     * (:675-692), the riderless hover (:752-763) and the extra integration
     * with 0.8/0.98/0.8 friction (:869-872) — the original moved the ant
     * once in moveEntityWithHeading and once more here, which is what makes
     * the 0.2 chase impulse from goThisWay effective. While player-ridden the
     * controlling client already integrated the move in {@link #tickRidden},
     * so vanilla travel is skipped (Elevator/B3 pattern).
     */
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
            return;
        }
        super.travel(travelVector);
        if (this.level().isClientSide()) {
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        // orig :675-692 — the clamps apply riderless too.
        if (motionY > 0.85) motionY = 0.85;
        if (motionY < -0.85) motionY = -0.85;
        if (motionX < -1.25) motionX = -1.25;
        if (motionX > 1.25) motionX = 1.25;
        if (motionZ < -1.25) motionZ = -1.25;
        if (motionZ > 1.25) motionZ = 1.25;
        // orig :752-763 — riderless hover: probe 0.75 down, then 1.75 down if
        // that was air; solid ground lifts motion AND position 0.15, anything
        // else sinks 0.002 — the ant floats just under a block over terrain.
        BlockState ground = this.level().getBlockState(
                new BlockPos((int) this.getX(), (int) ((float) this.getY() - 1.75f + 1.0f), (int) this.getZ()));
        if (ground.isAir()) {
            ground = this.level().getBlockState(
                    new BlockPos((int) this.getX(), (int) ((float) this.getY() - 1.75f), (int) this.getZ()));
        }
        if (givesHoverSupport(ground)) {
            motionY += 0.15;
            this.setPos(this.getX(), this.getY() + 0.15, this.getZ());
        } else {
            motionY -= 0.002;
        }
        // orig :869-872 — second integration + 0.8/0.98/0.8 friction.
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 after = this.getDeltaMovement();
        this.setDeltaMovement(after.x * 0.8, after.y * 0.98, after.z * 0.8);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AntRobotOwned", this.owned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.owned = tag.getInt("AntRobotOwned");
    }

    private void feetFindSomethingToHit() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return; // orig AntRobot.java:940-942 — PlayNicely != 0 returns ahead of the stomp scan (ENT-S-115)
        AABB searchBox = this.getBoundingBox().inflate(10.0, 8.0, 10.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity stompTarget : entities) {
            if (feetIsSuitableTarget(stompTarget)) {
                float yawToTarget = (float) Math.atan2(stompTarget.getZ() - this.getZ(), stompTarget.getX() - this.getX());
                // orig AntRobot.java:1000 — stomp damage = attack attribute / 10 (30/10 = 3.0).
                boolean hurtApplied = stompTarget.hurt(this.damageSources().mobAttack(this),
                        (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 10.0f);
                if (hurtApplied) {
                    stompTarget.push(
                            Math.cos(yawToTarget) * STOMP_KNOCKBACK,
                            0.1,
                            Math.sin(yawToTarget) * STOMP_KNOCKBACK);
                }
            }
        }
    }

    private boolean feetIsSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof AntRobot) return false;
        if (target == this.getFirstPassenger()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig AntRobot.java:971-973 — the shared ignore screen (ENT-S-106)
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig AntRobot.java:974-976 — canSee, after the ignore screen and ahead of the 6..9 ring (:977-986) (ENT-S-118)
        double dist = this.distanceTo(target);
        if (dist > 9.0f || dist < 6.0f) return false;
        if (target instanceof Player player && player.getAbilities().instabuild) return false;
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null; // orig AntRobot.java:1012-1014 — PlayNicely != 0 returns null ahead of the hunt scan (ENT-S-115)
        AABB searchBox = this.getBoundingBox().inflate(12.0, 12.0, 12.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof AntRobot) return false;
        if (target == this.getFirstPassenger()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig AntRobot.java:1044-1046 — the shared ignore screen (ENT-S-106)
        if (!this.getSensing().hasLineOfSight(target)) return false; // orig AntRobot.java:1047-1049 — canSee, after the ignore screen and ahead of the dircheck branch (:1050-1065, T8) and the creative check (:1066-1069) (ENT-S-118)
        if (target instanceof Player player && player.getAbilities().instabuild) return false;
        return true;
    }

    /** Live gait-solver state consumed by the model (orig AntRobot.java:540-542). */
    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        return renderInfo;
    }

    // ==================== Procedural leg-gait solver ====================
    // The AntRobot shares the SpiderRobot's inverse-kinematics walk (planted
    // feet, per-leg relocation, speed-scaled joint convergence) with its own
    // constants: 6 legs of 49px segments, tighter reach windows, a ×18 [2,8]
    // velocity scale and shallower mid-step lifts. See SpiderRobot for the
    // algorithm commentary; citations here point at the AntRobot original.

    /**
     * Per-leg constants and initial state (orig AntRobot.java:156-229).
     * Pairs (S5b review corrected this note against the table itself):
     * all three are MIRRORED −X/+X pairs by z-band — (0,1) the z≈0 mids,
     * (2,3) the front pair, (4,5) the rear pair.
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
            // Rest pose: upper segment 45° up, middle level, lower 45° down (orig :172-174).
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
                // orig :187-193
                case 0 -> { renderInfo.legoff[leg] = 0.75f; renderInfo.ymid[leg] = 0.0f;            renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 1; renderInfo.yoff[leg] = -0.75f; }
                // orig :194-200
                case 1 -> { renderInfo.legoff[leg] = 0.75f; renderInfo.ymid[leg] = (float) Math.PI; renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 0; renderInfo.yoff[leg] = -0.75f; }
                // orig :201-207
                case 2 -> { renderInfo.legoff[leg] = 1.0f;  renderInfo.ymid[leg] = -0.7853982f;     renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 3; renderInfo.yoff[leg] = -0.75f; }
                // orig :208-214
                case 3 -> { renderInfo.legoff[leg] = 1.0f;  renderInfo.ymid[leg] = 3.9269907f;      renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 2; renderInfo.yoff[leg] = -0.75f; }
                // orig :215-221
                case 4 -> { renderInfo.legoff[leg] = 1.15f; renderInfo.ymid[leg] = 0.7853982f;      renderInfo.yrange[leg] = 0.2617994f;  renderInfo.pairedwith[leg] = 5; renderInfo.yoff[leg] = -0.75f; }
                // orig :222-227
                case 5 -> { renderInfo.legoff[leg] = 1.15f; renderInfo.ymid[leg] = 2.3561945f;      renderInfo.yrange[leg] = -0.2617994f; renderInfo.pairedwith[leg] = 4; renderInfo.yoff[leg] = -0.75f; }
                default -> { }
            }
        }
    }

    /**
     * Speed-scaled angular-velocity controller (orig AntRobot.java:231-269).
     * Identical structure to the SpiderRobot's, but the body speed scales
     * ×18 clamped to [2,8] — the small ant's joints snap faster.
     *
     * @param bodySpeed       blocks moved last tick
     * @param angleError      remaining angle to the target, radians (sign = direction)
     * @param currentVelocity the joint's angular velocity from last tick
     * @return the new angular velocity, radians/tick
     */
    private float getNewVelocity(float bodySpeed, float angleError, float currentVelocity) {
        float scale = bodySpeed * 18.0f;
        if (scale < 2.0f) {
            scale = 2.0f;
        }
        if (scale > 8.0f) {
            scale = 8.0f;
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
     * One solver step (orig AntRobot.java:271-404), client-only like the
     * original's {@code isRemote} early-out. The ant's relocation windows are
     * 144 &gt; reach×16 &gt; 22 with yaw beyond range×8/6; leg segments are
     * 49px; foot landing has no block side effects (the SpiderRobot's grass
     * trample does not exist here).
     */
    public void updateLegs() {
        if (!this.level().isClientSide()) {
            return;
        }
        // orig :275-278 — yaw normalized into [0,360) before the trig below.
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
            // Hip socket world position (orig :294-296).
            renderInfo.realposx[leg] = (float) (this.getX() - (double) renderInfo.legoff[leg]
                    * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
            renderInfo.realposz[leg] = (float) (this.getZ() + (double) renderInfo.legoff[leg]
                    * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg]));
            renderInfo.realposy[leg] = (float) this.getY() + renderInfo.yoff[leg];
            // Alternating-pair step scheduler (orig :297-300).
            int pairTickerSum = renderInfo.footingticker[leg] + renderInfo.footingticker[renderInfo.pairedwith[leg]];
            if (pairTickerSum > 50 && renderInfo.footingticker[leg] > renderInfo.footingticker[renderInfo.pairedwith[leg]]) {
                renderInfo.footingticker[leg] = 0;
            }
            deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
            deltaY = renderInfo.realposy[leg] - renderInfo.foot_ypos[leg];
            deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
            float footDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            footDistance *= 16.0f;
            // Yaw deviation from the leg's neutral direction (orig :306-313).
            float yawDeviation = (float) (Math.abs((double) renderInfo.ycurrentangle[leg]
                    - (Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) + (double) renderInfo.ymid[leg])) % (Math.PI * 2));
            if ((double) yawDeviation > Math.PI) {
                yawDeviation = (float) ((double) yawDeviation - Math.PI * 2);
            }
            if ((double) yawDeviation < -Math.PI) {
                yawDeviation = (float) ((double) yawDeviation + Math.PI * 2);
            }
            yawDeviation = Math.abs(yawDeviation);
            // Foot relocation triggers (orig :314-321).
            if (footDistance > 144.0f || footDistance < 22.0f
                    || yawDeviation > Math.abs(renderInfo.yrange[leg]) * 8.0f / 6.0f
                    || (double) Math.abs(renderInfo.udcurrentangle[leg]) > 1.25
                    || renderInfo.footingticker[leg] == 0) {
                this.findNewFooting(leg);
                deltaX = renderInfo.realposx[leg] - renderInfo.foot_xpos[leg];
                deltaY = renderInfo.realposy[leg] - renderInfo.foot_ypos[leg];
                deltaZ = renderInfo.realposz[leg] - renderInfo.foot_zpos[leg];
                footDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                footDistance *= 16.0f;
            }
            // Segment fold: 3 × 49px segments converge on the foot distance (orig :322-334).
            float segment1Reach = (float) (49.0 * Math.cos(renderInfo.p2xangle[leg] - renderInfo.p1xangle[leg]));
            float segment2Reach = 49.0f;
            float segment3Reach = (float) (49.0 * Math.cos(renderInfo.p2xangle[leg] - renderInfo.p3xangle[leg]));
            float totalReach = segment1Reach + segment2Reach + segment3Reach;
            float reachError = totalReach - footDistance;
            renderInfo.pxvelocity[leg] = this.getNewVelocity(bodySpeed, (float) ((double) reachError * Math.PI / 360.0), renderInfo.pxvelocity[leg]);
            if (renderInfo.pxvelocity[leg] == 0.0f || Math.abs(reachError) < 8.0f) {
                ++settledAxes;
            }
            renderInfo.p1xangle[leg] = renderInfo.p1xangle[leg] + (double) renderInfo.pxvelocity[leg];
            renderInfo.p2xangle[leg] = 0.0;
            renderInfo.p3xangle[leg] = -renderInfo.p1xangle[leg];
            // Elevation toward the planted foot or the mid-step lift point (orig :335-365).
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
            // Yaw: swing toward the planted foot's world bearing (orig :366-400).
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
            // All three axes settled → foot lands (orig :401-403; no side effects).
            if (settledAxes == 3) {
                renderInfo.footup[leg] = 0;
            }
        }
    }

    /**
     * Plants the foot of leg {@code leg} on fresh ground (orig
     * AntRobot.java:406-510). Same sweep as the SpiderRobot's with ant
     * proportions: reach 9 (4 for the side-rear pair, 6 for the front/back
     * center pair — orig :445-447 applies that unconditionally last), scan
     * column 8 up / 9 down, minimum reach 2.5, candidate rejected while the
     * probe loop runs if it lies beyond 144 (reach×16); mid-step lift bumps
     * +0.3 above 3px, +0.6 above 24px, +0.6 more above 50px.
     */
    private void findNewFooting(int leg) {
        float reach = 9.0f;
        boolean found = false;
        double headingRad = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
        // orig :416 — the original's slightly-off hand-typed PI, kept for exactness.
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
        swingBias *= 0.8f;
        if (Math.abs((this.yRotO - this.getYRot()) % 360.0f) > 0.75f) {
            swingBias = 0.0f;
        }
        if (leg >= 4) {
            reach = 4.0f;
        }
        // Moving backward relative to facing: step behind instead (orig :438-444).
        if (headingDiff > 1.5) {
            swingBias = -swingBias;
            reach = 4.0f;
            if (leg >= 4) {
                reach = 9.0f;
            }
        }
        if (leg == 0 || leg == 1) {
            reach = 6.0f;
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
        while (!found && reach > 2.5f) {
            footX = (float) ((double) renderInfo.realposx[leg] - (double) reach
                    * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg] - (double) swingBias));
            footZ = (float) ((double) renderInfo.realposz[leg] + (double) reach
                    * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + (double) renderInfo.ymid[leg] - (double) swingBias));
            footY = renderInfo.realposy[leg];
            for (int yScan = 8; !found && yScan > -9; --yScan) {
                for (int xSpread = -spread; !found && xSpread <= spread; ++xSpread) {
                    for (int zSpread = -spread; !found && zSpread <= spread; ++zSpread) {
                        BlockPos probe = BlockPos.containing((int) footX + xSpread, (int) footY + yScan, (int) footZ + zSpread);
                        // orig :460-461 — non-air with a solid material.
                        if (this.level().getBlockState(probe).isAir() || !this.level().getBlockState(probe).isSolid()) continue;
                        // orig :462-466 — reject candidates beyond 144 (reach×16) inside the sweep.
                        deltaX = renderInfo.realposx[leg] - (footX + (float) xSpread);
                        float deltaY = renderInfo.realposy[leg] - (footY + (float) yScan + 1.0f);
                        deltaZ = renderInfo.realposz[leg] - (footZ + (float) zSpread);
                        float footingDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
                        if ((footingDistance *= 16.0f) > 144.0f) continue;
                        footY += (float) (yScan + 1);
                        footX += (float) xSpread;
                        footZ += (float) zSpread;
                        found = true;
                        break;
                    }
                }
            }
            // Second pass: bias removed, spread widened to ±3 (orig :475-479).
            if (!((reach -= 1.0f) < 2.5f) || swingBias == 0.0f) continue;
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
        // Mid-step lift point for a foot that was down (orig :491-509).
        if (renderInfo.footup[leg] == 0) {
            renderInfo.footup[leg] = 1;
            deltaX = previousFootX - footX;
            float deltaY = previousFootY - footY;
            deltaZ = previousFootZ - footZ;
            float stepLength = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            stepLength *= 16.0f;
            float liftPoint = (previousFootY + footY) / 2.0f;
            if (stepLength > 3.0f) {
                liftPoint += 0.3f;
            }
            if (stepLength > 24.0f) {
                liftPoint += 0.6f;
            }
            if (stepLength > 50.0f) {
                liftPoint += 0.6f;
            }
            renderInfo.uppoint[leg] = liftPoint;
        }
    }
}
