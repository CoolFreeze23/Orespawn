package danger.orespawn.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import danger.orespawn.ModItems;

/**
 * Phase 10 — Hoverboard vehicle, ported from the 1.7.10 {@code Elevator.java}
 * (the original mod called the entity "Elevator" internally even though the
 * spawning item was the Hoverboard).
 *
 * Legacy physics constants pulled verbatim from
 * {@code reference_1_7_10_source/sources/danger/orespawn/Elevator.java}:
 * <pre>
 *   max_speed (forward)  = 0.85       (Elevator.java line 244)
 *   max_speed (reverse)  = 0.35       (Elevator.java line 461)
 *   accel forward        = +0.025     (line 456)
 *   accel forward + jump = +0.175     (line 458, when flyup key held)
 *   accel reverse        = -0.02      (line 462)
 *   friction X / Z       = 0.98       (lines 500, 502)
 *   friction Y           = 0.94       (line 501)
 *   hover height (rider) = 1.25       (line 366)
 *   hover lift           = +0.06 y    (line 369)
 *   air drift            = -0.01 y    (line 378)
 *   crash speed          > 0.75       (line 490)
 *   yaw blend factor     = 1.85 - v   (line 413, clamped 0.01..0.9)
 * </pre>
 *
 * Modernization notes:
 *  - Sync uses {@link SynchedEntityData} for the colour byte instead of legacy
 *    field_70180_af accessors.
 *  - Vehicle controls flow through {@link #travel(Vec3)} so the server is the
 *    single source of truth for position; we read Player.zza / xxa / jumping
 *    inputs (which the client streams via the standard
 *    ServerboundPlayerInputPacket) instead of injecting C0CPacketInput.
 *  - Fall damage is suppressed for the rider via causeFallDamage override.
 *  - We are a {@link Mob} (not a Boat) because Mob already implements the
 *    rideable-server-tick / passenger-positioning machinery we need; the
 *    no-AI flag makes us behave inertly when riderless.
 */
public class HoverboardEntity extends Mob {

    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE =
            SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TIME_SINCE_HIT =
            SynchedEntityData.defineId(HoverboardEntity.class, EntityDataSerializers.INT);

    private static final double MAX_FORWARD_SPEED = 0.85;
    private static final double MAX_FORWARD_SPEED_BOOSTED = 1.85; // +1.0 with jump key
    private static final double MAX_REVERSE_SPEED = 0.35;
    private static final double ACCEL_FORWARD = 0.025;
    private static final double ACCEL_FORWARD_BOOSTED = 0.175;
    private static final double ACCEL_REVERSE = 0.02;
    private static final double FRICTION_HORIZ = 0.98;
    private static final double FRICTION_VERT = 0.94;
    private static final double HOVER_HEIGHT_RIDER = 1.25;
    private static final double HOVER_LIFT = 0.06;
    private static final double AIR_GRAVITY_DRIFT = 0.01;
    private static final double CRASH_SPEED = 0.75;
    private static final float HEALTH_DESTROY_THRESHOLD = 40.0f;

    private double currentVelocity = 0.0;
    private int playingSoundCooldown = 0;

    public HoverboardEntity(EntityType<? extends HoverboardEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setNoGravity(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR, 1);
        builder.define(DATA_DAMAGE, 0.0f);
        builder.define(DATA_TIME_SINCE_HIT, 0);
    }

    public int getColor() { return this.entityData.get(DATA_COLOR); }
    public void setColor(int c) {
        if (c < 1) c = 1;
        if (c > 10) c = 10;
        this.entityData.set(DATA_COLOR, c);
    }

    public int getTimeSinceHit() { return this.entityData.get(DATA_TIME_SINCE_HIT); }
    public void setTimeSinceHit(int t) { this.entityData.set(DATA_TIME_SINCE_HIT, t); }
    public float getDamageTaken() { return this.entityData.get(DATA_DAMAGE); }
    public void setDamageTaken(float d) { this.entityData.set(DATA_DAMAGE, d); }

    @Override
    public boolean isPickable() { return !this.isRemoved(); }
    @Override public boolean isPushable() { return true; }
    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isNoGravity() { return this.getControllingPassenger() != null; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof Player p ? p : null;
    }

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        // Cycle paint colour with Ultimate Sword (1.7.10 fidelity — Elevator.func_70085_c).
        if (!held.isEmpty() && held.getItem() == ModItems.ULTIMATE_SWORD.get()
                && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide()) {
                int next = this.getColor() + 1;
                this.setColor(next > 10 ? 1 : next);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        // Don't let a second player kick out the current rider.
        if (this.getControllingPassenger() != null && this.getControllingPassenger() != player) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        if (!this.level().isClientSide()) {
            player.startRiding(this);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide() || this.isRemoved()) return false;
        if (source.is(DamageTypes.IN_WALL)) return false;

        boolean creativeAttacker = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
        boolean ridden = this.getControllingPassenger() != null;

        // 1.7.10 fidelity: a passenger blocks damage from non-players (so monsters
        // can't punch you off your ride). Players (esp. creative) bypass.
        if (ridden && !(source.getEntity() instanceof Player)) return false;

        this.setTimeSinceHit(10);
        this.setDamageTaken(this.getDamageTaken() + amount * 10.0f);
        this.markHurt();

        if (creativeAttacker || this.getDamageTaken() > HEALTH_DESTROY_THRESHOLD) {
            if (!creativeAttacker) {
                this.spawnAtLocation(new ItemStack(ModItems.HOVERBOARD.get()));
            }
            this.ejectPassengers();
            this.discard();
        }
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDist, float multiplier, DamageSource source) {
        // Riders never take fall damage from their hoverboard (1.7.10 fidelity —
        // Elevator overrode func_70069_a as a no-op). The board itself also
        // ignores fall damage to avoid silly self-deaths.
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }
        if (this.getDamageTaken() > 0.0f) {
            this.setDamageTaken(this.getDamageTaken() - 1.0f);
        }
        if (this.playingSoundCooldown > 0) --this.playingSoundCooldown;

        // Periodic hover hum (matches 1.7.10 Elevator: 1-in-80 chance per tick
        // when ridden; cooldown keeps the SFX from stacking).
        Player rider = this.getControllingPassenger() instanceof Player p ? p : null;
        if (rider != null && this.playingSoundCooldown == 0
                && this.random.nextInt(80) == 1) {
            // Use vanilla beacon hum as a gentle hover tone; legacy used a custom
            // orespawn:hover sound (the .ogg files exist in the 1.7.10 assets but
            // we don't ship a SoundEvent registration for them yet).
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.BEACON_AMBIENT,
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.45f, 1.0f);
            this.playingSoundCooldown = 55;
        }

        // Smoke trail for the boost — purely cosmetic, server-side spawn so all
        // observers see it. Throttled to 1-in-3 ticks while moving.
        if (rider != null && this.level() instanceof net.minecraft.server.level.ServerLevel sl
                && Math.abs(this.currentVelocity) > 0.2 && this.random.nextInt(3) == 0) {
            sl.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.1, this.getZ(),
                    2, 0.2, 0.0, 0.2, 0.0);
        }
    }

    /**
     * Vehicle physics — invoked on the server (and predicted on the client) for
     * the rider-controlled board. We bypass the default Mob travel because we
     * want the legacy hover semantics: ground-attraction lift, friction-only
     * deceleration, and yaw blending toward the rider's view direction.
     */
    @Override
    public void travel(Vec3 input) {
        if (!this.isVehicle() || !(this.getControllingPassenger() instanceof Player rider)) {
            // Riderless: fall slowly with friction so abandoned boards drift to
            // the ground instead of vanishing or rocketing skyward.
            Vec3 v = this.getDeltaMovement();
            this.setDeltaMovement(v.x * FRICTION_HORIZ, (v.y - 0.04) * FRICTION_VERT, v.z * FRICTION_HORIZ);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            return;
        }

        // --- Yaw blend toward rider view (line 413 in legacy, modernized) ---
        float riderYaw = rider.getYRot();
        double horizSpeed = Math.sqrt(this.getDeltaMovement().x * this.getDeltaMovement().x
                + this.getDeltaMovement().z * this.getDeltaMovement().z);
        if (horizSpeed > 0.01) {
            double blend = Math.abs(1.85 - horizSpeed);
            blend = Mth.clamp(blend, 0.01, 0.9);
            float diff = Mth.degreesDifference(this.getYRot(), riderYaw);
            this.setYRot((float) (this.getYRot() + diff * blend));
        } else {
            this.setYRot(riderYaw);
        }
        this.yBodyRot = this.getYRot();
        this.setXRot(10.0f * (float) horizSpeed);

        // --- Hover lift: lift if a block is below us within hover range ---
        BlockPos under = BlockPos.containing(this.getX(), this.getY() - HOVER_HEIGHT_RIDER, this.getZ());
        Vec3 v = this.getDeltaMovement();
        double vy = v.y;
        if (!this.level().getBlockState(under).isAir()) {
            vy += HOVER_LIFT;
        } else {
            vy -= AIR_GRAVITY_DRIFT;
        }

                // --- Forward / reverse acceleration based on rider input ---
                // Player.zza is the WASD-forward axis (+1 forward, -1 back). Modern
                // mapping for the legacy flyup_keystate boost: Player.isSprinting()
                // toggles boosted forward (Ctrl/double-W). The protected jumping field
                // on LivingEntity isn't reachable from another instance, and we don't
                // want to ship an Access Transformer just for a boost flag.
                float forwardInput = rider.zza;
                boolean boosted = rider.isSprinting();
        double maxSpeed = boosted ? MAX_FORWARD_SPEED_BOOSTED : MAX_FORWARD_SPEED;

        if (Math.abs(forwardInput) > 0.001f) {
            double delta;
            if (forwardInput > 0.0f) {
                delta = boosted ? ACCEL_FORWARD_BOOSTED : ACCEL_FORWARD;
            } else {
                maxSpeed = MAX_REVERSE_SPEED;
                delta = -ACCEL_REVERSE;
            }
            this.currentVelocity += delta;
        }
        this.currentVelocity = Mth.clamp(this.currentVelocity, -MAX_REVERSE_SPEED, maxSpeed);

        // Project current scalar velocity onto the (now-updated) yaw direction.
        double yawRad = Math.toRadians(this.getYRot() + 90.0);
        double newVx;
        double newVz;
        if (this.currentVelocity >= 0.0) {
            newVx = Math.cos(yawRad) * this.currentVelocity;
            newVz = Math.sin(yawRad) * this.currentVelocity;
        } else {
            // Reversing: flip 180 and use absolute speed
            double back = Math.toRadians(this.getYRot() + 270.0);
            double mag = -this.currentVelocity;
            newVx = Math.cos(back) * mag;
            newVz = Math.sin(back) * mag;
        }

        // NaN/Infinity guards — same defensive pattern we use on the Vortex
        // launch math so a bad client packet can't crash the server.
        if (Double.isNaN(newVx) || Double.isInfinite(newVx)) newVx = 0.0;
        if (Double.isNaN(vy)    || Double.isInfinite(vy))    vy    = 0.0;
        if (Double.isNaN(newVz) || Double.isInfinite(newVz)) newVz = 0.0;

        // Clamp to a sane range so we never tickle the MovedTooQuicklyPacket.
        newVx = Mth.clamp(newVx, -3.0, 3.0);
        vy    = Mth.clamp(vy, -1.0, 1.5);
        newVz = Mth.clamp(newVz, -3.0, 3.0);

        this.setDeltaMovement(newVx, vy, newVz);
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        // Friction tail
        Vec3 post = this.getDeltaMovement();
        this.setDeltaMovement(post.x * FRICTION_HORIZ, post.y * FRICTION_VERT, post.z * FRICTION_HORIZ);
        this.currentVelocity *= FRICTION_HORIZ;
        this.fallDistance = 0.0f;

        // Crash on horizontal collision while moving fast (1.7.10 self-destruct).
        if (this.horizontalCollision && Math.sqrt(post.x * post.x + post.z * post.z) > CRASH_SPEED) {
            if (!this.level().isClientSide()) {
                int sticks = 6 + this.random.nextInt(10);
                for (int i = 0; i < sticks; i++) this.spawnAtLocation(new ItemStack(Items.STICK));
                for (int i = 0; i < 2; i++)      this.spawnAtLocation(new ItemStack(Items.DIAMOND));
            }
            this.ejectPassengers();
            this.discard();
        }
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) return;
        // Rider sits 0.5 above the board plane so the legs hang naturally.
        move.accept(passenger, this.getX(), this.getY() + this.getBbHeight() + 0.1, this.getZ());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HoverColor", this.getColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HoverColor")) this.setColor(tag.getInt("HoverColor"));
    }
}
