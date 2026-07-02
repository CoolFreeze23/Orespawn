package danger.orespawn.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModItems;
import danger.orespawn.ModSounds;
import danger.orespawn.network.RiderInputPayload;

/**
 * The Hoverboard vehicle (internally "Elevator", orig Elevator.java — the
 * original registered the entity as "Hoverboard" and named the spawning item
 * "Hoverboard", orig OreSpawnMain.java:3879-3883/5174). A player-ridden hovercraft:
 * it floats over terrain, auto-climbs obstructions, accelerates with W/S plus
 * the fly-up key as a speed boost, randomly enters a 45-tick "exploding"
 * malfunction at high speed, shatters into sticks and diamonds when slammed
 * into a wall, and cycles through 10 paint colors when clicked with the
 * Ultimate Sword.
 *
 * <p><b>Architecture mapping (ANIM-012).</b> The original ran the whole
 * physics block server-side inside a full {@code onUpdate} replacement
 * (orig Elevator.java:232-515) with boat-style client interpolation plus
 * explicit input packets (orig :337-341). The port uses the same
 * client-predicted riding architecture as the six B3 mounts (vanilla
 * horse-style): the controlling client runs the movement math in
 * {@link #tickRidden} and syncs position, while the server applies the
 * world-mutating side effects in {@link #serverRiddenTick} (grass trample,
 * exploding state machine, crash destruction, entity pushing) from its synced
 * view of the entity. Split points are cited on each method. The fly-up key
 * arrives via {@link RiderInputPayload} (modern per-player equivalent of the
 * original's global {@code OreSpawnMain.flyup_keystate}, orig :441).</p>
 *
 * <p><b>Known mapping deltas</b> (documented, gameplay-equivalent):
 * crash detection runs server-side against a wall probe and the last two
 * ticks' position-delta speed instead of the mover's collision flag (see
 * {@link #serverRiddenTick}), so destruction can lag the impact by one tick;
 * the riderless client hover nudge (orig :328-336) is superseded by vanilla
 * position lerping; the fly-down key (a port-wide addition) is ignored —
 * the original Elevator only consumed the single UP/FAST key.</p>
 */
public class Elevator extends Mob implements RiderInputPayload.RideableFlyer {
    private static final EntityDataAccessor<Integer> DATA_TIME_SINCE_HIT =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FORWARD_DIRECTION =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE_TAKEN =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_EXPLODING =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);

    private int color = 1;
    private int playing = 0;
    /** Server-local exploding countdown, mirrored to {@link #DATA_EXPLODING} (orig field, Elevator.java:42). */
    private int exploding = 0;
    /** Held state of the rider's fly-up key (client-set for prediction, server-set via payload). */
    private boolean riderFlyUp = false;
    /** Previous tick's server-side position-delta speed, for the crash check (see class Javadoc). */
    private double lastServerSpeed = 0.0;

    public Elevator(EntityType<? extends Elevator> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        // orig Elevator.java:109-115 — 60 HP, speed 1.33, attack 0.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 1.33)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // orig Elevator.java:147-155 — watcher slots 22/23/24/20/21.
        builder.define(DATA_TIME_SINCE_HIT, 0);
        builder.define(DATA_FORWARD_DIRECTION, 1);
        builder.define(DATA_DAMAGE_TAKEN, 0.0f);
        builder.define(DATA_EXPLODING, 0);
        builder.define(DATA_COLOR, 1);
    }

    public void setDamageTaken(float damageAccumulated) { this.entityData.set(DATA_DAMAGE_TAKEN, damageAccumulated); }
    public float getDamageTaken() { return this.entityData.get(DATA_DAMAGE_TAKEN); }
    public void setTimeSinceHit(int ticks) { this.entityData.set(DATA_TIME_SINCE_HIT, ticks); }
    public int getTimeSinceHit() { return this.entityData.get(DATA_TIME_SINCE_HIT); }
    public void setForwardDirection(int directionSign) { this.entityData.set(DATA_FORWARD_DIRECTION, directionSign); }
    public int getForwardDirection() { return this.entityData.get(DATA_FORWARD_DIRECTION); }
    public void setExploding(int value) { this.entityData.set(DATA_EXPLODING, value); }
    public int getExploding() { return this.entityData.get(DATA_EXPLODING); }
    public void setColor(int colorIndex) { this.entityData.set(DATA_COLOR, colorIndex); }
    public int getColor() { return this.entityData.get(DATA_COLOR); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // orig Elevator.java:165-192 — a passenger blocks non-player damage;
        // inWall ignored; accumulated damage ×10 past 40 (or any creative
        // player hit) destroys the board, dropping the item unless creative.
        boolean isPlayer = source.getEntity() instanceof Player;
        if (this.getFirstPassenger() != null && !isPlayer) return false;
        if (source.getMsgId().equals("inWall")) return false;
        if (!this.level().isClientSide() && !this.isRemoved()) {
            this.setForwardDirection(-this.getForwardDirection());
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0f);
            this.markHurt();
            boolean creative = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
            if (creative || this.getDamageTaken() > 40.0f) {
                if (this.getFirstPassenger() != null) this.getFirstPassenger().stopRiding();
                // orig Elevator.java:184-186 — drops the Hoverboard item on non-creative destruction.
                if (!creative) this.spawnAtLocation(new ItemStack(ModItems.ELEVATOR.get()));
                this.discard();
            }
        }
        return true;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        // orig Elevator.java:137-138 — fall() is a no-op.
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        // orig Elevator.java:121-123 — the rider stands on the board.
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    /** Any mounted player steers (orig had no gate; the board is a pure vehicle). */
    @javax.annotation.Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player) {
            return player;
        }
        return super.getControllingPassenger();
    }

    /** Rider stands 0.5 above the board plane (orig Elevator.java:161-163, 517-521). */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) return;
        move.accept(passenger, this.getX(), this.getY() + 0.5, this.getZ());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Consumed by {@link #tickRidden}; modern per-player equivalent of the
     * original's global {@code OreSpawnMain.flyup_keystate} poll (orig
     * Elevator.java:441-443). The fly-down key is ignored — the original
     * Elevator only consumed the single UP/FAST key.</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
    }

    @Override
    public void tick() {
        super.tick();
        this.clearFire();
        if (this.isRemoved()) return;
        // orig Elevator.java:251-256 — hit-wobble timers decay every tick.
        if (this.getTimeSinceHit() > 0) this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        if (this.getDamageTaken() > 0.0f) this.setDamageTaken(this.getDamageTaken() - 1.0f);

        // orig Elevator.java:297-303 — ridden hover hum ("orespawn:hover",
        // ENT-D-012), 1-in-80, 55-tick cooldown.
        if (this.playing > 0) --this.playing;
        if (this.getFirstPassenger() != null && this.playing == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(ModSounds.HOVER.get(), 0.45f, 1.0f);
            this.playing = 55;
        }

        if (this.level().isClientSide()) {
            clientEffectsTick();
        } else {
            serverRiddenTick();
        }
    }

    // ==================== Client cosmetic effects ====================

    /**
     * Speed trail, water spray and malfunction effects (orig
     * Elevator.java:260-296, 316-326 — the original ran these in the shared
     * update path, where particle spawns only take effect client-side).
     */
    private void clientEffectsTick() {
        Vec3 motion = this.getDeltaMovement();
        double velocity = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        if (velocity > 0.15 && this.getFirstPassenger() != null) {
            double cosBack = Math.cos(Math.toRadians(this.getYRot() + 270.0f));
            double sinBack = Math.sin(Math.toRadians(this.getYRot() + 270.0f));
            // orig :263-265 — scan up to 9 blocks down for the first non-air block.
            BlockState surface = Blocks.AIR.defaultBlockState();
            int depth = 1;
            for (; depth < 10; ++depth) {
                surface = this.level().getBlockState(new BlockPos((int) this.getX(), (int) this.getY() - depth, (int) this.getZ()));
                if (!surface.isAir()) break;
            }
            // orig :266-295 — 1 + velocity*10 puffs per tick, half trailing
            // behind the board, half thrown ahead; smoke or redstone dust.
            for (int j = 0; (double) j < 1.0 + velocity * 10.0; ++j) {
                double spread = this.getRandom().nextFloat() * 2.0f - 1.0f;
                double side = (double) (this.getRandom().nextInt(2) * 2 - 1) * 0.7;
                double px;
                double pz;
                double py;
                if (this.getRandom().nextBoolean()) {
                    px = this.getX() - cosBack * spread * 0.8 + sinBack * side;
                    pz = this.getZ() - sinBack * spread * 0.8 - cosBack * side;
                    py = this.getY() - 0.25;
                } else {
                    px = this.getX() + cosBack + sinBack * spread * 0.7;
                    pz = this.getZ() + sinBack - cosBack * spread * 0.7;
                    py = this.getY() - 0.225;
                }
                if (this.getRandom().nextBoolean()) {
                    this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, motion.x, motion.y, motion.z);
                } else {
                    this.level().addParticle(DustParticleOptions.REDSTONE, px, py, pz, motion.x, motion.y, motion.z);
                }
                // orig :289-293 — spray when skimming water.
                if (surface.is(Blocks.WATER)) {
                    for (int k = 0; k < 5; ++k) {
                        this.level().addParticle(ParticleTypes.SPLASH,
                                this.getX() + this.getRandom().nextFloat(),
                                this.getY() - depth + 1.25,
                                this.getZ() + this.getRandom().nextFloat(),
                                motion.x / 2.0, motion.y + velocity, motion.z / 2.0);
                    }
                }
            }
        }

        // orig :316-326 — malfunction show while ridden: explosion pops and a
        // cloud of explosion/smoke particles at int-truncated offsets.
        if (this.getExploding() > 0 && this.getFirstPassenger() != null) {
            if (this.getRandom().nextInt(10) == 1) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GENERIC_EXPLODE.value(), this.getSoundSource(),
                        0.55f, 0.75f + this.getRandom().nextFloat(), false);
            }
            for (int i = 0; i < 15; ++i) {
                this.level().addParticle(ParticleTypes.POOF,
                        (int) (this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 4.0f),
                        (int) (this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 4.0f),
                        (int) (this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 4.0f),
                        motion.x, 0.0, motion.z);
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        (int) (this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 2.0f),
                        (int) (this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 2.0f),
                        (int) (this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 2.0f),
                        motion.x, 0.0, motion.z);
                this.level().addParticle(ParticleTypes.SMOKE,
                        (int) (this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 5.0f),
                        (int) (this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 5.0f),
                        (int) (this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 5.0f),
                        motion.x, 0.0, motion.z);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        (int) (this.getX() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 3.0f),
                        (int) (this.getY() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 3.0f),
                        (int) (this.getZ() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 3.0f),
                        motion.x, 0.0, motion.z);
            }
        }
    }

    // ==================== Server-side world effects ====================

    /**
     * World-mutating half of the original's ridden update, run on the server
     * against its synced view of the client-controlled board:
     * <ul>
     *   <li>grass trample under the board (orig Elevator.java:368-376)</li>
     *   <li>the random exploding malfunction (orig :304-315)</li>
     *   <li>crash destruction with stick/diamond drops (orig :490-498) —
     *       detected via last-two-ticks speed &gt; 0.75 plus a wall probe in
     *       the travel direction, because the mover's collision flag is only
     *       set on the side that integrates movement (see class Javadoc)</li>
     *   <li>pushing entities aside (orig :504-510)</li>
     * </ul>
     */
    private void serverRiddenTick() {
        double deltaX = this.getX() - this.xo;
        double deltaZ = this.getZ() - this.zo;
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // orig :304-315 — exploding countdown; 1-in-20000 chance per tick to
        // start a 45-tick malfunction while moving fast.
        if (this.exploding > 0) --this.exploding;
        if (this.exploding == 0 && speed > 0.65 && this.getRandom().nextInt(20000) == 1) {
            this.exploding = 45;
            this.playing = 50;
        }
        this.setExploding(this.exploding);

        if (this.getFirstPassenger() != null) {
            // orig :368-376 — while ridden, the hover column occasionally
            // clears grass (1-in-200, mobGriefing): tall grass to air, grass
            // blocks to dirt. Int-truncated coordinates preserved.
            double hoverDepth = 1.25;
            BlockPos under = new BlockPos((int) this.getX(), (int) (this.getY() - hoverDepth), (int) this.getZ());
            Block underBlock = this.level().getBlockState(under).getBlock();
            if (underBlock == Blocks.SHORT_GRASS && this.getRandom().nextInt(200) == 1
                    && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.level().setBlockAndUpdate(under, Blocks.AIR.defaultBlockState());
            }
            if (underBlock == Blocks.GRASS_BLOCK && this.getRandom().nextInt(200) == 1
                    && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.level().setBlockAndUpdate(under, Blocks.DIRT.defaultBlockState());
            }

            // orig :490-498 — slam into a wall above 0.75 speed and the board
            // shatters into 6-15 sticks and 2 diamonds (no board item back).
            double recentSpeed = Math.max(speed, this.lastServerSpeed);
            if (recentSpeed > 0.75) {
                double dirX;
                double dirZ;
                if (speed > 1.0E-4) {
                    dirX = deltaX / speed;
                    dirZ = deltaZ / speed;
                } else {
                    dirX = Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                    dirZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                }
                boolean wallAhead = !this.level().noCollision(this,
                        this.getBoundingBox().move(dirX * 0.1, 0.0, dirZ * 0.1));
                if (wallAhead) {
                    int bonus = this.getRandom().nextInt(10);
                    for (int k = 0; k < 6 + bonus; ++k) {
                        this.spawnAtLocation(new ItemStack(Items.STICK));
                    }
                    for (int k = 0; k < 2; ++k) {
                        this.spawnAtLocation(new ItemStack(Items.DIAMOND));
                    }
                    this.ejectPassengers();
                    this.discard();
                    return;
                }
            }
        }
        this.lastServerSpeed = speed;

        // orig :504-510 — shove entities aside, except the rider and the
        // Girlfriend/Boyfriend companions.
        List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(0.25, 0.0, 0.25));
        for (Entity entity : nearby) {
            if (entity == this.getFirstPassenger() || !entity.isPushable()
                    || entity instanceof Girlfriend || entity instanceof Boyfriend) continue;
            entity.push(this);
        }
    }

    // ==================== Rider-controlled movement (client-predicted) ====================

    /**
     * The original's ridden physics (orig Elevator.java:364-503), run on the
     * controlling client like the six B3 mounts. Order preserved: ground
     * hover, obstruction climb, yaw/pitch, exploding drag, heading sign,
     * throttle with fly-up boost, projection, move, friction.
     */
    @Override
    protected void tickRidden(Player rider, Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        if (!this.isControlledByLocalInstance()) return;

        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        // orig :239 — tick-start speed drives the pitch, particle count and crash check.
        double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);

        // orig :365-379 — ground hover: probe 1.25 below while ridden; lift
        // 0.06 motion + 0.1 position over ground, sink 0.01 in air. (The
        // grass-trample side effect lives server-side in serverRiddenTick.)
        double hoverDepth = 1.25;
        BlockPos under = new BlockPos((int) this.getX(), (int) ((float) this.getY() - (float) hoverDepth), (int) this.getZ());
        if (!this.level().getBlockState(under).isAir()) {
            motionY += 0.06;
            this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
        } else {
            motionY -= 0.01;
        }

        // orig :383-396 — obstruction scan: a wedge ahead of the board, depth
        // 3 + velocity*8, each solid block adds 0.05; the total lifts both
        // motion and position by factor*0.11 so the board climbs terrain.
        double obstructionFactor = 0.0;
        int dist = 3 + (int) (velocity * 8.0);
        for (int k = 1; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                double dx = (double) i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                double dz = (double) i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                BlockPos probe = new BlockPos((int) (this.getX() + dx), (int) this.getY() - k, (int) (this.getZ() + dz));
                if (this.level().getBlockState(probe).isAir()) continue;
                obstructionFactor += 0.05;
            }
        }
        motionY += obstructionFactor * 0.11;
        this.setPos(this.getX(), this.getY() + obstructionFactor * 0.11, this.getZ());

        // orig :397-429 — yaw chases the rider's yaw with a speed-dependent
        // lag (|1.85 - v| clamped 0.01..0.9); pitch = 10 * speed.
        double riderYaw = rider.getYRot() % 360.0;
        while (riderYaw < 0.0) riderYaw += 360.0;
        double boardYaw = this.getYRot() % 360.0;
        while (boardYaw < 0.0) boardYaw += 360.0;
        double relativeYaw = (riderYaw - boardYaw) % 180.0;
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
        this.setXRot(10.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        // orig :430-433 — the exploding malfunction bleeds 0.05 speed per tick.
        double newVelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (this.getExploding() != 0 && (newVelocity -= 0.05) < 0.0) {
            newVelocity = 0.0;
        }

        // orig :434-453 — sign the scalar speed against the RIDER's facing:
        // motion running opposite the rider means we're reversing.
        double max_speed = 0.85;
        double motionHeading = Math.atan2(motionZ, motionX);
        double riderHeading = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        // orig :438 — the original's slightly-off hand-typed PI, kept for exactness.
        double pi = 3.1415926545;
        double headingDiff = Math.abs(motionHeading - riderHeading) % (pi * 2.0);
        if (headingDiff > pi) headingDiff -= pi * 2.0;
        headingDiff = Math.abs(headingDiff);
        if (Math.abs(newVelocity) < 0.01) headingDiff = 0.0;
        if (headingDiff > 1.5) newVelocity = -newVelocity;

        // orig :441-443 — fly-up key raises the cap 0.85 → 1.85 ("FAST").
        if (this.riderFlyUp) {
            max_speed += 1.0;
        }

        // orig :454-484 — throttle: forward +0.025 (+0.15 more while boosted),
        // reverse -0.02 capped at 0.35; project the scalar onto the board's
        // facing (+90 forward, +270 reverse).
        float forwardInput = rider.zza;
        if (Math.abs(forwardInput) > 0.001f) {
            double deltav;
            if (forwardInput > 0.0f) {
                deltav = 0.025;
                if (max_speed > 1.0) {
                    deltav += 0.15;
                }
            } else {
                max_speed = 0.35;
                deltav = -0.02;
            }
            if ((newVelocity += deltav) >= 0.0) {
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

        // orig :489, 499-503 — move, then friction (crash destruction is
        // server-side; see serverRiddenTick).
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 afterMove = this.getDeltaMovement();
        this.setDeltaMovement(afterMove.x * 0.98, afterMove.y * 0.94, afterMove.z * 0.98);
        this.fallDistance = 0.0f;
        this.calculateEntityAnimation(false);
    }

    /**
     * Riderless drift (orig Elevator.java:368-379, 485-488 riderless path):
     * hover 0.75 above ground (lift 0.06/0.1, sink 0.01), no horizontal
     * motion, friction 0.98/0.94/0.98. While player-ridden the controlling
     * client already integrated the move in {@link #tickRidden}, so vanilla
     * travel is skipped (Cephadrome/B3 pattern).
     */
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
            return;
        }
        if (this.level().isClientSide()) {
            // Remote copies interpolate the synced position (replaces the
            // original's boat-lerp client branch, orig :342-363).
            return;
        }
        Vec3 motion = this.getDeltaMovement();
        double motionY = motion.y;
        double hoverDepth = 0.75;
        BlockPos under = new BlockPos((int) this.getX(), (int) ((float) this.getY() - (float) hoverDepth), (int) this.getZ());
        if (!this.level().getBlockState(under).isAir()) {
            motionY += 0.06;
            this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
        } else {
            motionY -= 0.01;
        }
        // orig :485-488 — a riderless board never drifts horizontally.
        this.setDeltaMovement(0.0, motionY, 0.0);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 afterMove = this.getDeltaMovement();
        this.setDeltaMovement(afterMove.x * 0.98, afterMove.y * 0.94, afterMove.z * 0.98);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // orig Elevator.java:542-557 — Ultimate Sword within 4 blocks cycles
        // the paint color 1..10 instead of mounting.
        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && held.getItem() == ModItems.ULTIMATE_SWORD.get()
                && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide()) {
                ++this.color;
                if (this.color > 10) this.color = 1;
                this.setColor(this.color);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        // orig :558-564 — a second player cannot take an occupied board.
        if (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Player
                && this.getFirstPassenger() != player) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide()) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        // orig Elevator.java:523-525 — only the color persists.
        tag.putInt("HoverColor", this.getColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        // orig Elevator.java:527-536 — clamp 1..10.
        this.color = tag.getInt("HoverColor");
        if (this.color < 1) this.color = 1;
        if (this.color > 10) this.color = 10;
        this.setColor(this.color);
    }
}
