package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
import danger.orespawn.entity.ai.TargetSelection;

public class ThePrinceTeen extends TamableAnimal
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_MOTHRAWINGS = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings"));
    private static final SoundEvent SND_ROAR = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE =
            SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD1 =
            SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD2 =
            SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD3 =
            SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);

    /**
     * Ridden-flight tuning, number-for-number from orig ThePrinceTeen.java:879-1087
     * (ridden branch of onLivingUpdate): hover probe 1.25 (orig :895-902 — lift
     * +0.03/+0.1, glide-fall 0.018), terrain scan 3 + v*7 @ 0.05/block ×0.07
     * (orig :903-916), rise cap 2.0 (orig :917-919), yaw lag 1.85 above 0.01
     * (orig :935-946), fly-up +0.035 + v*0.046 (orig :961-964), throttle 0.025
     * ramped (max_speed 0.95 :843 — under the >1.0 bonus gate :978), reverse
     * 0.35 @ -0.02 (orig :989-990), friction 0.985/0.94/0.985 (orig :1085-1087).
     */
    private static final danger.orespawn.entity.ai.RiderFlightController.Config RIDER_FLIGHT_CONFIG =
            new danger.orespawn.entity.ai.RiderFlightController.Config(
                    true, 1.25, 0.03, 0.1, 0.018,
                    3, 7.0, 0.05, 0.07, false,
                    2.0,
                    1.85, 0.01, false,
                    false, 0.035, 0.046,
                    0.025, 0.95, -0.02, 0.35, true,
                    0.0, 0.985, 0.94);

    private final danger.orespawn.entity.ai.RiderFlightController riderFlight =
            new danger.orespawn.entity.ai.RiderFlightController(RIDER_FLIGHT_CONFIG);
    /** Held state of the rider's vertical keys (client-set for prediction, server-set via payload). */
    private boolean riderFlyUp = false;
    private boolean riderFlyDown = false;
    /** Cooldown between rider-triggered canon volleys (orig {@code fireballticker}). */
    private int fireballTicker = 0;
    /** Cycles the three-headed canon: fireball → iceball → thunderbolt (orig {@code which_attack}). */
    private int whichAttack = 0;

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.35f;
    private int hurtTimer = 0;
    private int head1dir = 1, head2dir = 1, head3dir = 1;
    private int growCounter = 0;
    private int killCount = 0;
    private int dayCount = 0;
    private int isDay = 0;
    /** orig ThePrinceTeen.java:73 — owner creative-flight fast-follow flag. */
    private int ownerFlying = 0;
    /** orig ThePrinceTeen.java:76 — ticks the dragon flees after a bite before re-engaging. */
    private int flyAway = 0;
    /** orig ThePrinceTeen.java:77 — sticky combat flag; keeps flight locked on the victim. */
    private boolean targetInSight = false;
    /** orig ThePrinceTeen.java:80 — wing-flap sound ticker while flying. */
    private int wingSound = 0;
    /** orig ThePrinceTeen.java:64 — flight steering target (transient). */
    private BlockPos.MutableBlockPos currentFlightTarget = null;
    /**
     * Per-entity render scratch (orig ThePrinceTeen.java:80 {@code renderdata = new RenderInfo()},
     * re-created orig :122, zeroed in entityInit orig :216-226, accessor orig :237-239).
     * Mutated client-side by {@code ModelThePrinceTeen} for the flight head-yaw low-pass
     * latch (orig ModelThePrinceTeen.java:671-678, field rf1 only); never datawatcher-synced.
     * ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

    public ThePrinceTeen(EntityType<? extends ThePrinceTeen> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        // orig ThePrinceTeen.java:105 — experienceValue = 300.
        this.xpReward = 300;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 12.0f, 2.0f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 12.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // MOD-033 (T9 A2, petsDefendOwner): the owner-defence pair is modern only, a construction snapshot
        // (the helper read once here; goals register in the Mob ctor, the BOSS-017 shape — a config change
        // applies to newly spawned teens); orig ThePrinceTeen.java:116-119 registered the IMob task and EntityAIHurtByTarget
        // only. Live here: the combat roll reads the target slot first, so a tamed modern teen avenges
        // and defends its owner.
        if (OreSpawnConfig.petsDefendOwner()) {
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        }
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this)); // orig ThePrinceTeen.java:119 — both modes
        // orig ThePrinceTeen.java:116-118 — the EntityAINearestAttackableTarget task (EntityLiving.class, IMob
        // selector) is registered only when PlayNicely == 0 at construction; the port registers the goal always and
        // reads the flag live in its canUse, so it never starts while PlayNicely is on (ENT-S-115; the custom scan's
        // own gate, orig :540-542, is at findSomethingToAttack).
        // orig ThePrinceTeen.java:117 IMob.mobSelector → Mob.class + instanceof Enemy; 10 / false are the 3-arg constructor's own randomInterval / mustReach (ENT-S-124, IMob convention)
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, e -> e instanceof Enemy) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig ThePrinceTeen.java:116-118 (ENT-S-115)
                return super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig ThePrinceTeen.java:230 HP 1500, :141 ATK 50, :253 armor 18, :87 speed 0.32.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.ARMOR, 18.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_FIRE, 1);
        builder.define(DATA_HEAD1, 0);
        builder.define(DATA_HEAD2, 0);
        builder.define(DATA_HEAD3, 0);
    }

    @Override public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int value) { this.entityData.set(DATA_ACTIVITY, value); }
    public int getHead1Ext() { return this.entityData.get(DATA_HEAD1); }
    public int getHead2Ext() { return this.entityData.get(DATA_HEAD2); }
    public int getHead3Ext() { return this.entityData.get(DATA_HEAD3); }

    /** Mirrors orig ThePrinceTeen.java:237-239 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    @Override
    public void tick() {
        super.tick();
        // orig ThePrinceTeen.java:590 — any flight activity ghosts through terrain.
        this.noPhysics = this.getActivity() != 0;
        if (this.hurtTimer > 0) --this.hurtTimer;
        // (The 1-in-100 self-heal that used to sit here was a port invention;
        // the original heals 2.0 at 1-in-250 inside always_do — see alwaysDo().)

        // orig ThePrinceTeen.java:657-665 — wing flaps every 20 ticks while flying wild.
        if (this.getActivity() == 1) {
            ++this.wingSound;
            if (this.wingSound > 20) {
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SND_MOTHRAWINGS,
                            this.getSoundSource(), 0.5f, 1.0f);
                }
                this.wingSound = 0;
            }
        }
        // orig ThePrinceTeen.java:666-668 — buoyancy.
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.07, 0.0));
        }
        // orig ThePrinceTeen.java:672-674 — grounded pet takes flight when the
        // owner is more than 20 blocks away.
        if (!this.level().isClientSide && this.getActivity() == 0 && this.isTame()
                && this.getOwner() != null && !this.isOrderedToSit()
                && this.distanceToSqr(this.getOwner()) > 400.0) {
            this.setActivity(1);
        }

        int i;
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head1dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head2dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head3dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        int h1 = Math.max(0, Math.min(60, this.getHead1Ext() + this.head1dir));
        int h2 = Math.max(0, Math.min(60, this.getHead2Ext() + this.head2dir));
        int h3 = Math.max(0, Math.min(60, this.getHead3Ext() + this.head3dir));
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_HEAD1, h1);
            this.entityData.set(DATA_HEAD2, h2);
            this.entityData.set(DATA_HEAD3, h3);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = target.hurt(this.damageSources().mobAttack(this), 50.0f);
        if (target instanceof LivingEntity living && living.getHealth() <= 0.0f) {
            ++this.killCount;
        }
        return result;
    }

    /** orig ThePrinceTeen.java:343-393 ({@code func_70097_a}). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        // orig :349-363 — immune to cactus, fire, lava, and suffocation.
        String msg = source.getMsgId();
        if (msg.equals("cactus") || msg.equals("inFire") || msg.equals("onFire")
                || msg.equals("lava") || msg.equals("inWall")) {
            return false;
        }
        // orig :364-365 — any real hit breaks the sit and launches into flight.
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.setActivity(1);
        Entity attacker = source.getEntity();
        // orig :367-374 — fireballs pop harmlessly.
        if (attacker instanceof BetterFireball
                || attacker instanceof net.minecraft.world.entity.projectile.SmallFireball) {
            attacker.discard();
            return false;
        }
        // orig :375-380 — immune to other teen princes and Spyros.
        if (attacker instanceof ThePrinceTeen || attacker instanceof EntitySpyro) {
            return false;
        }
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 20; // orig :382
        if (attacker instanceof LivingEntity living) {
            // orig :383-391 — a tamed teen never retaliates against players.
            if (this.isTame() && living instanceof Player) {
                return false;
            }
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
            ret = true;
        }
        return ret;
    }

    // ==================== Riding (BOSS-027) ====================

    /**
     * Only the taming owner steers (orig ThePrinceTeen.java:1151-1162 — riding
     * is gated on tame + ownership).
     */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isTame() && !this.getPassengers().isEmpty()
                && this.getPassengers().get(0) instanceof Player player
                && this.isOwnedBy(player)) {
            return player;
        }
        return null;
    }

    /**
     * Seats the rider 0.65 blocks ahead of center, 2.75 up (orig
     * ThePrinceTeen.java:1107-1112 with mounted y-offset 2.75 at :299-301).
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        double rx = this.getX() - 0.65 * Math.sin(Math.toRadians(this.getYRot()));
        double ry = this.getY() + 2.75;
        double rz = this.getZ() + 0.65 * Math.cos(Math.toRadians(this.getYRot()));
        callback.accept(passenger, rx, ry, rz);
    }

    /**
     * Client-predicted ridden flight (BOSS-027): the riding client runs the
     * original three-headed-dragon flight physics (orig
     * ThePrinceTeen.java:879-1087, constants in {@link #RIDER_FLIGHT_CONFIG})
     * and syncs position like a vanilla horse. The wild autonomous flight
     * (activity 2 / fly_without_rider) remains out of scope (Phase D); only
     * RIDDEN movement runs here, so the BUG-010 interim noPhysics fix for the
     * baby ThePrince/ThePrincess is untouched.
     */
    @Override
    protected void tickRidden(Player rider, net.minecraft.world.phys.Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        if (this.isControlledByLocalInstance()) {
            this.riderFlight.tick(this, rider, this.riderFlyUp, this.riderFlyDown);
        }
    }

    /**
     * Skips vanilla travel while player-ridden: {@link #tickRidden} already
     * applied the full move via {@code RiderFlightController}, so running
     * vanilla travel too would integrate the motion twice. Also skipped while
     * flying wild (activity != 0): the original bypassed all vanilla movement
     * then (orig ThePrinceTeen.java:849-857 — super.onLivingUpdate only when
     * activity == 0) and {@code flyWithoutRider} moves the entity directly.
     */
    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
            return;
        }
        if (!this.level().isClientSide && this.getActivity() != 0) {
            return;
        }
        super.travel(travelVector);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Consumed by {@link #tickRidden}; modern per-player equivalent of
     * the original's global {@code OreSpawnMain.flyup_keystate} poll (orig
     * ThePrinceTeen.java:961-964).</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
        this.riderFlyDown = down;
    }

    /**
     * Server-side portion of the original ridden branch — everything except
     * movement: the strafe-key canon trio (orig ThePrinceTeen.java:1020-1083),
     * pushing nearby entities (orig :1088-1094, box 3.25/4.0/3.25), the
     * mounted auto-attack {@code fly_with_rider} (orig :463-487), and ejecting
     * a removed rider (orig :1096-1098).
     */
    private void serverRiddenTick(Player rider) {
        if (this.isRemoved() || this.isOrderedToSit()) return;

        if (this.fireballTicker == 0 && Math.abs(rider.xxa) > 0.001f) {
            fireCanonTrio(rider);
            // orig ThePrinceTeen.java:1082 — 10-tick volley cooldown.
            this.fireballTicker = 10;
        }

        List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(3.25, 4.0, 3.25));
        for (Entity entity : nearby) {
            if (entity != this.getFirstPassenger() && !entity.isRemoved() && entity.isPushable()) {
                entity.push(this);
            }
        }

        flyWithRider();

        if (this.getFirstPassenger() != null && this.getFirstPassenger().isRemoved()) {
            this.ejectPassengers();
        }
    }

    /**
     * Strafe-key canon volley while ridden (orig ThePrinceTeen.java:1020-1083):
     * each press cycles head 1 → 3 → 2, firing a plain BetterFireball
     * ("random.fuse", head offset -10°), an ice-making IceBall
     * ("fireworks.launch" 0.75f, head offset +10°), and a ×3-velocity
     * ThunderBolt ("random.bow" 0.75f, center head). Muzzle xz 7.5 / y 1.5
     * plus the animated head extension × 0.04 (orig :1023-1024).
     */
    private void fireCanonTrio(Player rider) {
        double xzoff = 7.5;
        double yoff = 1.5;
        ++this.whichAttack;
        if (this.whichAttack > 2) {
            this.whichAttack = 0;
        }
        net.minecraft.world.phys.Vec3 look = rider.getLookAngle();
        if (this.whichAttack == 0) {
            double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() - 10.0f));
            double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() - 10.0f));
            double cy = this.getY() + yoff + this.getHead1Ext() * 0.04;
            BetterFireball bf = new BetterFireball(this.level(), this, look);
            bf.setNotMe();
            bf.setPos(cx, cy, cz);
            this.level().addFreshEntity(bf);
            // orig :1048 "random.fuse"
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.TNT_PRIMED, this.getSoundSource(), 1.0f,
                    1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        } else if (this.whichAttack == 1) {
            double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() + 10.0f));
            double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() + 10.0f));
            double cy = this.getY() + yoff + this.getHead3Ext() * 0.04;
            IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
            ib.setOwner(this);
            ib.enableIceCreation();
            ib.setPos(cx, cy, cz);
            ib.shoot(look.x, look.y, look.z, 1.4f, 5.0f);
            // orig :1065-1067 — ice ball flies at double speed.
            ib.setDeltaMovement(ib.getDeltaMovement().scale(2.0));
            this.level().addFreshEntity(ib);
            // orig :1068 "fireworks.launch" 0.75f
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH, this.getSoundSource(), 0.75f,
                    1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        } else {
            double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
            double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
            double cy = this.getY() + yoff + this.getHead2Ext() * 0.04;
            ThunderBolt tb = new ThunderBolt(this.level(), rider);
            tb.setPos(cx, cy, cz);
            tb.shoot(look.x, look.y, look.z, 1.5f, 1.0f);
            // orig :1076-1078 — thunderbolt flies at triple speed.
            tb.setDeltaMovement(tb.getDeltaMovement().scale(3.0));
            this.level().addFreshEntity(tb);
            // orig :1079 "random.bow" 0.75f
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.ARROW_SHOOT, this.getSoundSource(), 0.75f,
                    1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        }
    }

    /**
     * Mounted auto-attack (orig ThePrinceTeen.java:463-487 {@code fly_with_rider}):
     * 1-in-5 chance per tick outside Peaceful; bites anything within
     * {@code 8.0 + width/2} blocks, or canons targets between 10 and 25 blocks
     * away when out of water and fireballs are lit.
     */
    private void flyWithRider() {
        if (this.isRemoved() || this.isOrderedToSit() || this.level().isClientSide) return;
        if (this.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return;
        if (this.random.nextInt(5) != 1) return;

        LivingEntity target = findSomethingToAttack();
        if (target != null) {
            this.setAttacking(1);
            float attackRange = 8.0f + target.getBbWidth() / 2.0f;
            double distSq = this.distanceToSqr(target);
            if (distSq < attackRange * attackRange) {
                this.doHurtTarget(target);
            } else if (distSq > 100.0 && distSq < 625.0 && !this.isInWater()
                    && this.entityData.get(DATA_FIRE) != 0) {
                shootSomethingAt(target.getX(), target.getY(), target.getZ());
            }
        } else {
            this.setAttacking(0);
        }
    }

    /**
     * Random-head canon shot at a coordinate (orig ThePrinceTeen.java:1355-1389
     * {@code shoot_something} + the firecanon/firecanonl/firecanoni trio at
     * :1391-1459): picks one of the three heads at random and fires only when
     * the target sits within ~0.5 rad of the facing. Muzzle xz 6.0 / y 3.5,
     * fireball aim is jittered ±5/±3/±5 blocks, thunder/ice bolts launch at
     * 1.4f/4.0f and are tripled.
     */
    private void shootSomethingAt(double x, double y, double z) {
        double heading = Math.atan2(z - this.getZ(), x - this.getX());
        double facing = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
        double diff = Math.abs(heading - facing) % (Math.PI * 2.0);
        if (diff > Math.PI) {
            diff -= Math.PI * 2.0;
        }
        if (Math.abs(diff) >= 0.5) {
            return;
        }

        double xzoff = 6.0;
        double yoff = 3.5;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cy = this.getY() + yoff;
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        int which = this.random.nextInt(3);
        if (which == 0) {
            // orig :1391-1406 — big fireball with ±5/±3/±5 aim jitter.
            double r1 = 5.0 * (this.random.nextFloat() - this.random.nextFloat());
            double r2 = 3.0 * (this.random.nextFloat() - this.random.nextFloat());
            double r3 = 5.0 * (this.random.nextFloat() - this.random.nextFloat());
            net.minecraft.world.phys.Vec3 dir =
                    new net.minecraft.world.phys.Vec3(x - cx + r1, y + 0.25 - cy + r2, z - cz + r3);
            BetterFireball bf = new BetterFireball(this.level(), this, dir);
            bf.setBig();
            bf.setPos(cx, cy, cz);
            this.level().addFreshEntity(bf);
        } else {
            double dx = x - cx;
            double dy = y + 0.25 - cy;
            double dz = z - cz;
            double arc = Math.sqrt(dx * dx + dz * dz) * 0.2;
            if (which == 1) {
                // orig :1408-1432 — thunderbolt at 1.4f/4.0f, tripled.
                ThunderBolt tb = new ThunderBolt(this.level(), cx, cy, cz);
                tb.shoot(dx, dy + arc, dz, 1.4f, 4.0f);
                tb.setDeltaMovement(tb.getDeltaMovement().scale(3.0));
                this.level().addFreshEntity(tb);
            } else {
                // orig :1434-1459 — ice-making ice ball at 1.4f/4.0f, tripled.
                IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
                ib.setOwner(this);
                ib.enableIceCreation();
                ib.setPos(cx, cy, cz);
                ib.shoot(dx, dy + arc, dz, 1.4f, 4.0f);
                ib.setDeltaMovement(ib.getDeltaMovement().scale(3.0));
                this.level().addFreshEntity(ib);
            }
        }
        // orig :1404/:1417/:1443 "random.bow"
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.ARROW_SHOOT, this.getSoundSource(), 1.0f,
                1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
    }

    /**
     * orig ThePrinceTeen.java:849-857 — while flying (activity != 0) the
     * original never called {@code super.onLivingUpdate()}, so goals,
     * navigation, and vanilla physics were all paused; only the flight brain
     * ran. Clients still run super for position interpolation (the original
     * hand-rolled the same lerp at :859-873).
     */
    @Override
    public void aiStep() {
        if (this.level().isClientSide || this.getActivity() == 0 || this.isRemoved()) {
            super.aiStep();
            return;
        }
        // orig ThePrinceTeen.java:875-1101 — the server flight branch.
        if (this.fireballTicker > 0) {
            --this.fireballTicker;
        }
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player rider) {
            serverRiddenTick(rider);
        } else {
            flyWithoutRider();
        }
        alwaysDo(); // orig ThePrinceTeen.java:1103 — every server tick.
    }

    /** Grounded server AI (activity 0) — orig func_70619_bc, ThePrinceTeen.java:395-433. */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig ThePrinceTeen.java:398-405 — ground spotting: a 1-in-10 roll
        // outside Peaceful; seeing prey launches the teen into flight.
        if (!this.isOrderedToSit() && !this.isVehicle()
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(10) == 1) {
            LivingEntity e = findSomethingToAttack();
            if (e != null) {
                this.setActivity(1);
            } else {
                this.setAttacking(0);
            }
        }

        // orig ThePrinceTeen.java:406-418 — natural growth to adult.
        if (this.killCount > 25 && this.dayCount > 10) {
            this.transformToAdult();
            return;
        }

        // orig ThePrinceTeen.java:419-432 — day counting.
        if (this.isDay == 0) {
            this.isDay = 1;
            if (!this.level().isDay()) this.isDay = -1;
        } else {
            if (this.isDay == -1 && this.level().isDay()) ++this.dayCount;
            this.isDay = this.level().isDay() ? 1 : -1;
        }

        alwaysDo(); // orig ThePrinceTeen.java:1103 — every server tick.
    }

    /**
     * orig ThePrinceTeen.java:435-461 ({@code always_do}) — slow regen, target
     * forgiveness, owner creative-flight detection, and the random settle roll.
     */
    private void alwaysDo() {
        if (this.random.nextInt(250) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }
        if (this.random.nextInt(250) == 0) {
            this.setTarget(null);
        }
        if (this.isOrderedToSit()) {
            return;
        }
        this.ownerFlying = 0;
        if (this.isTame() && !this.isVehicle()
                && this.getOwner() instanceof Player ownerPlayer
                && ownerPlayer.getAbilities().flying) {
            this.ownerFlying = 1;
            this.setActivity(1);
        }
        // orig :454-460 — 1-in-50 settle roll while uncommitted: 1-in-15 keeps
        // flying, otherwise land (activity 0).
        if (this.random.nextInt(50) == 1 && !this.targetInSight && !this.isVehicle()) {
            if (this.random.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
    }

    /** orig ThePrinceTeen.java:565-567 — block-only line of sight from eye height 0.75. */
    private boolean canSeeSpot(double px, double py, double pz) {
        return this.level().clip(new net.minecraft.world.level.ClipContext(
                new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(px, py, pz),
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType()
                == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    /**
     * orig ThePrinceTeen.java:677-834 ({@code fly_without_rider}) — wild flight:
     * vertical damping, the 1-in-7 combat roll (bite + fly-away, or the canon
     * volley within 20 blocks), flight-target selection (owner-anchored when
     * tame), terrain-following lift, and signum steering with a direct move.
     */
    private void flyWithoutRider() {
        boolean doNew = false;
        boolean hasOwner = false;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        boolean tooFar = false;
        Vec3 motion = this.getDeltaMovement();
        double velocity = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (this.currentFlightTarget == null) {
            doNew = true;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(
                    (int) this.getX(), (int) this.getY(), (int) this.getZ());
        }
        if (this.isVehicle()) {
            return;
        }
        LivingEntity owner = this.getOwner();
        if (this.isTame() && owner != null) {
            hasOwner = true;
            ox = owner.getX();
            oy = owner.getY();
            oz = owner.getZ();
            // orig :709-716 — >20 blocks from the owner: drop everything and return.
            if (this.distanceToSqr(owner) > 400.0) {
                tooFar = true;
                this.targetInSight = false;
                this.setAttacking(0);
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                this.flyAway = 0;
                doNew = true;
            }
        }
        if (this.isOrderedToSit()) {
            return;
        }
        // orig :721 — vertical damping (the 0.61 arm is unreachable in the
        // original's ternary too; kept verbatim).
        double dampY = this.getY() < this.currentFlightTarget.getY() + 2.0 ? 0.7
                : (this.getY() > this.currentFlightTarget.getY() - 2.0 ? 0.5 : 0.61);
        this.setDeltaMovement(motion.x, motion.y * dampY, motion.z);
        if (this.random.nextInt(300) == 1) {
            doNew = true;
        }
        if (this.flyAway > 0) {
            --this.flyAway;
        }
        // orig :728-763 — the 1-in-7 combat roll.
        if (!tooFar && this.flyAway == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(7) == 1) {
            LivingEntity e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                if (this.isTame() && this.getHealth() / this.getMaxHealth() < 0.25f) {
                    // orig :738-743 — badly hurt pet flees away from the threat.
                    this.setActivity(1);
                    this.setAttacking(0);
                    this.targetInSight = false;
                    doNew = false;
                    this.currentFlightTarget.set(
                            (int) (this.getX() + (this.getX() - e.getX())),
                            (int) (this.getY() + 1.0),
                            (int) (this.getZ() + (this.getZ() - e.getZ())));
                } else {
                    this.setActivity(1);
                    this.setAttacking(1);
                    this.targetInSight = true;
                    this.currentFlightTarget.set((int) e.getX(), (int) (e.getY() + 1.0), (int) e.getZ());
                    doNew = false;
                    float meleeRange = 8.0f + e.getBbWidth() / 2.0f;
                    double distSq = this.distanceToSqr(e);
                    if (distSq < (double) (meleeRange * meleeRange)) {
                        // orig :750-753 — bite, then break off for 5-19 ticks.
                        this.doHurtTarget(e);
                        this.flyAway = 5 + this.random.nextInt(15);
                        doNew = true;
                    } else if (distSq < 400.0 && !this.isInWater()
                            && this.entityData.get(DATA_FIRE) != 0 && this.random.nextInt(2) == 1) {
                        this.shootSomethingAt(e.getX(), e.getY(), e.getZ());
                    }
                }
            } else {
                this.targetInSight = false;
                this.flyAway = 0;
                this.setAttacking(0);
            }
        }
        if (this.currentFlightTarget.distSqr(new net.minecraft.core.Vec3i(
                (int) this.getX(), (int) this.getY(), (int) this.getZ())) < 2.1) {
            doNew = true;
        }
        // orig :767-801 — pick a new air target the teen can see: near the owner
        // (spread 5-18, tighter 0-5 when the owner is creative-flying) or a wild
        // 16-25 wander; up to 10 tries.
        if ((doNew && !this.targetInSight) || (doNew && this.flyAway != 0)) {
            int keepTrying = 10;
            boolean found = false;
            while (!found && keepTrying != 0) {
                int gox = (int) this.getX();
                int goy = (int) this.getY();
                int goz = (int) this.getZ();
                int xdir;
                int zdir;
                if (hasOwner) {
                    gox = (int) ox;
                    goy = (int) oy;
                    goz = (int) oz;
                    if (this.ownerFlying == 0) {
                        zdir = this.random.nextInt(14) + 5;
                        xdir = this.random.nextInt(14) + 5;
                    } else {
                        zdir = this.random.nextInt(6);
                        xdir = this.random.nextInt(6);
                    }
                } else {
                    zdir = this.random.nextInt(10) + 16;
                    xdir = this.random.nextInt(10) + 16;
                }
                if (this.random.nextInt(2) == 1) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 1) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir,
                        goy + this.random.nextInt(9 + this.ownerFlying * 2) - 4, goz + zdir);
                found = this.level().isEmptyBlock(this.currentFlightTarget)
                        && this.canSeeSpot(this.currentFlightTarget.getX(),
                                this.currentFlightTarget.getY(), this.currentFlightTarget.getZ());
                --keepTrying;
            }
        }
        // orig :802-815 — terrain-following lift: scan the blocks below and
        // ahead (radius grows with speed) and nudge both motion and position up.
        double obstruction = 0.0;
        int dist = 2 + (int) (velocity * 4.0);
        for (int k = 1; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                double sx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                double sz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                BlockPos probe = new BlockPos((int) (this.getX() + sx),
                        (int) this.getY() - k, (int) (this.getZ() + sz));
                if (!this.level().isEmptyBlock(probe)) {
                    obstruction += 0.05;
                }
            }
        }
        motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.add(0.0, obstruction * 0.05, 0.0));
        this.setPos(this.getX(), this.getY() + obstruction * 0.05, this.getZ());
        // orig :816-833 — signum steering: base 0.5, 1.75 chasing a flying
        // owner, 3.5 when also >8 blocks behind; then a direct move.
        double speedFactor = 0.5;
        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (this.isTame() && owner != null && this.distanceToSqr(owner) > 64.0) {
                speedFactor = 3.5;
            }
        }
        motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) - motion.x) * 0.15 * speedFactor;
        double my = motion.y + (Math.signum(dy) - motion.y) * 0.21 * speedFactor;
        double mz = motion.z + (Math.signum(dz) - motion.z) * 0.15 * speedFactor;
        this.setDeltaMovement(mx, my, mz);
        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = (float) (0.75 * speedFactor);
        this.setYRot(this.getYRot() + yawDelta / 4.0f);
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
    }

    private void transformToAdult() {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) return;
        ThePrinceAdult adult = ModEntities.THE_PRINCE_ADULT.get().create(serverLevel);
        if (adult == null) return;
        adult.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        if (this.isTame() && this.getOwnerUUID() != null) {
            Player owner = this.level().getPlayerByUUID(this.getOwnerUUID());
            if (owner != null) {
                adult.tame(owner);
            } else {
                // Owner offline: tame(null) would NPE (BUG-004); transfer UUID directly.
                adult.setOwnerUUID(this.getOwnerUUID());
                adult.setTame(true, true);
            }
        }
        serverLevel.addFreshEntity(adult);
        this.discard();
    }

    /** orig ThePrinceTeen.java:496-537 ({@code isSuitableTarget}). */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (MyUtils.isRoyalty(target)) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Mothra) return true;
        if (target instanceof Kraken) return true;
        // orig :524-535 — Leon, WaterDragon, and GammaMetroid are prey while wild.
        if (target instanceof EntityLeon leon) return !leon.isTame();
        if (target instanceof WaterDragon wd) return !wd.isTame();
        if (target instanceof EntityGammaMetroid gm) return !gm.isTame();
        return false;
    }

    /** orig ThePrinceTeen.java:539-555 — nearest suitable prey in a 25/20/25 box. */
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB box = this.getBoundingBox().inflate(25.0, 20.0, 25.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, this.targetSorter, this::isSuitableTarget);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Diamond block: tame + full heal + instant adult-growth credit (orig
        // ThePrinceTeen.java:1133-1150 — kill_count/day_count forced to 1000;
        // the original tames unconditionally, so it also transfers ownership).
        if (stack.is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.asItem())
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
                this.killCount = 1000;
                this.dayCount = 1000;
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1151-1154 — everything below is owner-only;
        // a tamed teen ignores strangers entirely.
        if (!this.isTame() || !this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }
        // (The CAKE growth shortcut that used to live here was a port
        // invention — orig func_70085_c :1127-1273 has no cake branch. Removed;
        // see AUDIT_FINDINGS teen_cake_dup.)

        // Empty hand: saddle-free mount (orig ThePrinceTeen.java:1155-1162 —
        // mounting wakes the dragon and switches it to flight, activity 1).
        if (stack.isEmpty() && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setActivity(1);
                this.setOrderedToSit(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1163-1178 — beef heals to full.
        if (stack.is(Items.BEEF) && this.distanceToSqr(player) < 25.0) {
            if (this.getMaxHealth() > this.getHealth()) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1179-1195 — any other food heals nutrition × 10.
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                if (this.getMaxHealth() > this.getHealth()) {
                    net.minecraft.world.food.FoodProperties food =
                            stack.get(net.minecraft.core.component.DataComponents.FOOD);
                    this.heal(food.nutrition() * 10.0f);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1196-1212 — an ice block extinguishes the fireballs.
        if (stack.is(net.minecraft.world.level.block.Blocks.ICE.asItem())
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.entityData.set(DATA_FIRE, 0);
                player.displayClientMessage(Component.literal("Fireballs extinguished."), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1213-1229 — flint & steel relights them.
        if (stack.is(Items.FLINT_AND_STEEL) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.entityData.set(DATA_FIRE, 1);
                player.displayClientMessage(Component.literal("Fireballs lit!"), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1230-1250 — a diamond reverts the teen back to
        // a baby Prince with ok_to_grow set (instant re-grow eligibility).
        if (stack.is(Items.DIAMOND) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.revertToBaby(player);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceTeen.java:1261-1270 — any other held item toggles the sit
        // and grounds the dragon (activity 0).
        if (!stack.isEmpty() && this.distanceToSqr(player) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            this.setActivity(0);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    /** orig ThePrinceTeen.java:1230-1250 — diamond regression to "The Prince". */
    private void revertToBaby(Player player) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        ThePrince baby = ModEntities.THE_PRINCE.get().create(serverLevel);
        if (baby == null) return;
        baby.moveTo(this.getX(), this.getY(), this.getZ(),
                this.random.nextFloat() * 360.0f, 0.0f);
        if (this.isTame()) {
            baby.tame(player);
            baby.setOkToGrow();
        }
        serverLevel.addFreshEntity(baby);
        this.discard();
    }

    @Override protected SoundEvent getAmbientSound() { return SND_ROAR; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SND_ALO_HURT; }
    @Override protected SoundEvent getDeathSound() { return SND_ALO_DEATH; }
    @Override protected float getSoundVolume() { return 1.0f; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean isFood(ItemStack s) { return s.is(Items.BEEF); }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob o) { return null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TeenActivity", getActivity());
        tag.putInt("TeenAttacking", getAttacking());
        tag.putInt("TeenFire", entityData.get(DATA_FIRE));
        tag.putInt("TeenGrow", growCounter);
        tag.putInt("TeenKill", killCount);
        tag.putInt("TeenDay", dayCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setActivity(tag.getInt("TeenActivity"));
        setAttacking(tag.getInt("TeenAttacking"));
        entityData.set(DATA_FIRE, tag.getInt("TeenFire"));
        growCounter = tag.getInt("TeenGrow");
        killCount = tag.getInt("TeenKill");
        dayCount = tag.getInt("TeenDay");
    }

    /** orig ThePrinceTeen.java:561-563 — never spawns naturally (growth stage, spawned by promotion only). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return false;
    }
}
