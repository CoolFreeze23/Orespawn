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
import danger.orespawn.util.MyUtils;
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
import danger.orespawn.entity.ai.TargetSelection;

public class ThePrinceAdult extends TamableAnimal
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_MOTHRAWINGS = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings"));
    private static final SoundEvent SND_KING_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "king_living"));
    private static final SoundEvent SND_KING_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "king_hit"));
    private static final SoundEvent SND_TREX_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trex_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD1 =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD2 =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HEAD3 =
            SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);

    /**
     * Ridden-flight tuning, number-for-number from orig ThePrinceAdult.java:859-1069
     * (ridden branch of onLivingUpdate): hover probe 1.25 (orig :875-882 — lift
     * +0.03/+0.1, glide-fall 0.018), terrain scan 3 + v*7 @ 0.05/block ×0.07
     * (orig :883-896), rise cap 2.0 (orig :897-899), yaw lag 1.85 above 0.01
     * (orig :915-926), fly-up +0.045 + v*0.066 (orig :941-944), throttle
     * 0.035+0.07 ramped (max_speed 1.05 :823 — over the >1.0 bonus gate
     * :958-959), reverse 0.35 @ -0.02 (orig :969-970), friction
     * 0.985/0.94/0.985 (orig :1067-1069).
     */
    private static final danger.orespawn.entity.ai.RiderFlightController.Config RIDER_FLIGHT_CONFIG =
            new danger.orespawn.entity.ai.RiderFlightController.Config(
                    true, 1.25, 0.03, 0.1, 0.018,
                    3, 7.0, 0.05, 0.07, false,
                    2.0,
                    1.85, 0.01, false,
                    false, 0.045, 0.066,
                    0.105, 1.05, -0.02, 0.35, true,
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
    private final float moveSpeed = 0.36f;
    private int hurtTimer = 0;
    private int head1dir = 1, head2dir = 1, head3dir = 1;
    private int growCounter = 0;
    /** orig ThePrinceAdult.java:74 — owner creative-flight fast-follow flag. */
    private int ownerFlying = 0;
    /** orig ThePrinceAdult.java:77 — ticks the dragon flees after a bite before re-engaging. */
    private int flyAway = 0;
    /** orig ThePrinceAdult.java:78 — sticky combat flag; keeps flight locked on the victim. */
    private boolean targetInSight = false;
    /** orig ThePrinceAdult.java:81 — wing-flap sound ticker while flying. */
    private int wingSound = 0;
    /** orig ThePrinceAdult.java:65 — flight steering target (transient). */
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    public ThePrinceAdult(EntityType<? extends ThePrinceAdult> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 3000;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        // TF-035: orig ThePrinceAdult.java:78/:117 — GenericTargetSorter.
        this.targetSorter = new danger.orespawn.entity.ai.GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 12.0f, 2.0f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 20.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // MOD-033 (T9 A2, petsDefendOwner): the owner-defence pair is modern only, a construction snapshot
        // (the helper read once here; goals register in the Mob ctor, the BOSS-017 shape — a config change
        // applies to newly spawned adults); orig ThePrinceAdult.java:112-115 registered the IMob task and EntityAIHurtByTarget
        // only. Live here: the combat roll reads the target slot first, so a tamed modern adult avenges
        // and defends its owner.
        if (OreSpawnConfig.petsDefendOwner()) {
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        }
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this)); // orig ThePrinceAdult.java:115 — both modes
        // orig ThePrinceAdult.java:112-114 — the EntityAINearestAttackableTarget task (EntityLiving.class, IMob
        // selector) is registered only when PlayNicely == 0 at construction; the port registers the goal always and
        // reads the flag live in its canUse, so it never starts while PlayNicely is on (ENT-S-115; the custom scan's
        // own gate, orig :520-522, is at findSomethingToAttack).
        // orig ThePrinceAdult.java:113 IMob.mobSelector → Mob.class + instanceof Enemy; 10 / false are the 3-arg constructor's own randomInterval / mustReach (ENT-S-124, IMob convention)
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, e -> e instanceof Enemy) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig ThePrinceAdult.java:112-114 (ENT-S-115)
                return super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig ThePrinceAdult.java:226 HP 3000, :137 ATK 100, :249 armor 20, :86 speed 0.36.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
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

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int value) { this.entityData.set(DATA_ACTIVITY, value); }
    public int getHead1Ext() { return this.entityData.get(DATA_HEAD1); }
    public int getHead2Ext() { return this.entityData.get(DATA_HEAD2); }
    public int getHead3Ext() { return this.entityData.get(DATA_HEAD3); }

    @Override
    public void tick() {
        super.tick();
        // orig ThePrinceAdult.java:570 — any flight activity ghosts through terrain.
        this.noPhysics = this.getActivity() != 0;
        if (this.hurtTimer > 0) --this.hurtTimer;
        // (The 1-in-100 self-heal that used to sit here was a port invention;
        // the original heals 5.0 at 1-in-250 inside always_do — see alwaysDo().)

        // orig ThePrinceAdult.java:637-645 — wing flaps every 30 ticks while flying wild.
        if (this.getActivity() == 1) {
            ++this.wingSound;
            if (this.wingSound > 30) {
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SND_MOTHRAWINGS,
                            this.getSoundSource(), 0.5f, 1.0f);
                }
                this.wingSound = 0;
            }
        }
        // orig ThePrinceAdult.java:646-648 — buoyancy.
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.07, 0.0));
        }
        // orig ThePrinceAdult.java:652-654 — grounded pet takes flight when the
        // owner is more than 30 blocks away.
        if (!this.level().isClientSide && this.getActivity() == 0 && this.isTame()
                && this.getOwner() != null && !this.isOrderedToSit()
                && this.distanceToSqr(this.getOwner()) > 900.0) {
            this.setActivity(1);
        }

        int i;
        if (this.random.nextInt(10) == 1) {
            i = this.random.nextInt(3);
            this.head1dir = i == 0 ? 2 : i == 1 ? -2 : 0;
        }
        if (this.random.nextInt(10) == 1) {
            i = this.random.nextInt(3);
            this.head2dir = i == 0 ? 2 : i == 1 ? -2 : 0;
        }
        if (this.random.nextInt(10) == 1) {
            i = this.random.nextInt(3);
            this.head3dir = i == 0 ? 2 : i == 1 ? -2 : 0;
        }
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
        return target.hurt(this.damageSources().mobAttack(this), 100.0f);
    }

    /** orig ThePrinceAdult.java:335-387 ({@code func_70097_a}). */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        // orig :341-352 — immune to cactus, fire, and lava.
        String msg = source.getMsgId();
        if (msg.equals("cactus") || msg.equals("inFire") || msg.equals("onFire")
                || msg.equals("lava")) {
            return false;
        }
        // orig :353-357 — suffocation deals no damage but launches into flight.
        if (msg.equals("inWall")) {
            this.setOrderedToSit(false);
            this.setInSittingPose(false);
            this.setActivity(1);
            return false;
        }
        // orig :358-359 — any real hit breaks the sit and launches into flight.
        this.setOrderedToSit(false);
        this.setInSittingPose(false);
        this.setActivity(1);
        Entity attacker = source.getEntity();
        // orig :361-368 — fireballs pop harmlessly.
        if (attacker instanceof BetterFireball
                || attacker instanceof net.minecraft.world.entity.projectile.SmallFireball) {
            attacker.discard();
            return false;
        }
        // orig :369-374 — immune to other adult princes and Spyros.
        if (attacker instanceof ThePrinceAdult || attacker instanceof EntitySpyro) {
            return false;
        }
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 20; // orig :376
        if (attacker instanceof LivingEntity living) {
            // orig :377-385 — a tamed adult never retaliates against players.
            if (this.isTame() && living instanceof Player) {
                return false;
            }
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
            ret = true;
        }
        return ret;
    }

    // ==================== Riding (BOSS-033) ====================

    /**
     * Only the taming owner steers (orig ThePrinceAdult.java:1128-1139 —
     * riding is gated on tame + ownership).
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
     * Seats the rider 4.65 blocks ahead of center, 9.25 up — the adult is
     * colossal (orig ThePrinceAdult.java:1089-1094 with mounted y-offset 9.25
     * at :295-297).
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        double rx = this.getX() - 4.65 * Math.sin(Math.toRadians(this.getYRot()));
        double ry = this.getY() + 9.25;
        double rz = this.getZ() + 4.65 * Math.cos(Math.toRadians(this.getYRot()));
        callback.accept(passenger, rx, ry, rz);
    }

    /**
     * Client-predicted ridden flight (BOSS-033): the riding client runs the
     * original flight physics (orig ThePrinceAdult.java:859-1069, constants in
     * {@link #RIDER_FLIGHT_CONFIG}) and syncs position like a vanilla horse.
     * Wild autonomous flight remains out of scope (Phase D).
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
     * then (orig ThePrinceAdult.java:829-837 — super.onLivingUpdate only when
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
     * ThePrinceAdult.java:941-944).</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
        this.riderFlyDown = down;
    }

    /**
     * Server-side portion of the original ridden branch — everything except
     * movement: the strafe-key canon trio (orig ThePrinceAdult.java:1000-1065),
     * pushing nearby entities (orig :1070-1076, box 6.25/10.0/6.25), the
     * mounted auto-attack {@code fly_with_rider} (orig :443-467), and ejecting
     * a removed rider (orig :1078-1080).
     */
    private void serverRiddenTick(Player rider) {
        if (this.isRemoved() || this.isOrderedToSit()) return;

        if (this.fireballTicker == 0 && Math.abs(rider.xxa) > 0.001f) {
            fireCanonTrio(rider);
            // orig ThePrinceAdult.java:1064 — 8-tick volley cooldown.
            this.fireballTicker = 8;
        }

        List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(6.25, 10.0, 6.25));
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
     * Strafe-key canon volley while ridden (orig ThePrinceAdult.java:1000-1065):
     * each press cycles head 1 → 3 → 2, firing a BIG BetterFireball
     * ("random.fuse", head offset -10°), an ice-making IceBall
     * ("fireworks.launch" 0.75f, head offset +10°), and a ×3-velocity
     * ThunderBolt ("random.bow" 0.75f, center head). Muzzle xz 14.5 / y 9.5
     * minus the animated head extension × 0.08 (orig :1003-1004).
     */
    private void fireCanonTrio(Player rider) {
        double xzoff = 14.5;
        double yoff = 9.5;
        ++this.whichAttack;
        if (this.whichAttack > 2) {
            this.whichAttack = 0;
        }
        net.minecraft.world.phys.Vec3 look = rider.getLookAngle();
        if (this.whichAttack == 0) {
            double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() - 10.0f));
            double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() - 10.0f));
            double cy = this.getY() + yoff - this.getHead1Ext() * 0.08;
            BetterFireball bf = new BetterFireball(this.level(), this, look);
            // orig :1014 — the adult's fireball is the big variant.
            bf.setBig();
            bf.setNotMe();
            bf.setPos(cx, cy, cz);
            this.level().addFreshEntity(bf);
            // orig :1030 "random.fuse"
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.TNT_PRIMED, this.getSoundSource(), 1.0f,
                    1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        } else if (this.whichAttack == 1) {
            double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() + 10.0f));
            double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() + 10.0f));
            double cy = this.getY() + yoff - this.getHead3Ext() * 0.08;
            IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
            ib.setOwner(this);
            ib.enableIceCreation();
            ib.setPos(cx, cy, cz);
            ib.shoot(look.x, look.y, look.z, 1.4f, 5.0f);
            // orig :1047-1049 — ice ball flies at double speed.
            ib.setDeltaMovement(ib.getDeltaMovement().scale(2.0));
            this.level().addFreshEntity(ib);
            // orig :1050 "fireworks.launch" 0.75f
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_LAUNCH, this.getSoundSource(), 0.75f,
                    1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        } else {
            double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
            double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
            double cy = this.getY() + yoff - this.getHead2Ext() * 0.08;
            ThunderBolt tb = new ThunderBolt(this.level(), rider);
            tb.setPos(cx, cy, cz);
            tb.shoot(look.x, look.y, look.z, 1.5f, 1.0f);
            // orig :1058-1060 — thunderbolt flies at triple speed.
            tb.setDeltaMovement(tb.getDeltaMovement().scale(3.0));
            this.level().addFreshEntity(tb);
            // orig :1061 "random.bow" 0.75f
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.ARROW_SHOOT, this.getSoundSource(), 0.75f,
                    1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
        }
    }

    /**
     * Mounted auto-attack (orig ThePrinceAdult.java:443-467 {@code fly_with_rider}):
     * 1-in-5 chance per tick outside Peaceful; bites anything within
     * {@code 10.0 + width/2} blocks, or canons targets up to 25 blocks away
     * when out of water and fireballs are lit.
     */
    private void flyWithRider() {
        if (this.isRemoved() || this.isOrderedToSit() || this.level().isClientSide) return;
        if (this.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) return;
        if (this.random.nextInt(5) != 1) return;

        LivingEntity target = findSomethingToAttack();
        if (target != null) {
            this.setAttacking(1);
            float attackRange = 10.0f + target.getBbWidth() / 2.0f;
            double distSq = this.distanceToSqr(target);
            if (distSq < attackRange * attackRange) {
                this.doHurtTarget(target);
            } else if (distSq < 625.0 && !this.isInWater()
                    && this.entityData.get(DATA_FIRE) != 0) {
                shootSomethingAt(target.getX(), target.getY(), target.getZ());
            }
        } else {
            this.setAttacking(0);
        }
    }

    /**
     * Random-head canon shot at a coordinate (orig ThePrinceAdult.java:1329-1363
     * {@code shoot_something} + firecanon/firecanonl/firecanoni at :1365-1433):
     * picks one of the three heads at random and fires only when the target
     * sits within ~0.5 rad of the facing. Muzzle xz 6.0 / y 3.5; the fireball
     * aim is jittered ±5/±3/±5 blocks, thunder/ice bolts launch at 1.4f/4.0f
     * and are tripled.
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
                ThunderBolt tb = new ThunderBolt(this.level(), cx, cy, cz);
                tb.shoot(dx, dy + arc, dz, 1.4f, 4.0f);
                tb.setDeltaMovement(tb.getDeltaMovement().scale(3.0));
                this.level().addFreshEntity(tb);
            } else {
                IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
                ib.setOwner(this);
                ib.enableIceCreation();
                ib.setPos(cx, cy, cz);
                ib.shoot(dx, dy + arc, dz, 1.4f, 4.0f);
                ib.setDeltaMovement(ib.getDeltaMovement().scale(3.0));
                this.level().addFreshEntity(ib);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.ARROW_SHOOT, this.getSoundSource(), 1.0f,
                1.0f / (this.random.nextFloat() * 0.4f + 0.8f));
    }

    /**
     * orig ThePrinceAdult.java:829-837 — while flying (activity != 0) the
     * original never called {@code super.onLivingUpdate()}, so goals,
     * navigation, and vanilla physics were all paused; only the flight brain
     * ran. Clients still run super for position interpolation (the original
     * hand-rolled the same lerp at :839-853).
     */
    @Override
    public void aiStep() {
        if (this.level().isClientSide || this.getActivity() == 0 || this.isRemoved()) {
            super.aiStep();
            return;
        }
        // orig ThePrinceAdult.java:855-1083 — the server flight branch.
        if (this.fireballTicker > 0) {
            --this.fireballTicker;
        }
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player rider) {
            serverRiddenTick(rider);
        } else {
            flyWithoutRider();
        }
        alwaysDo(); // orig ThePrinceAdult.java:1085 — every server tick.
    }

    /** Grounded server AI (activity 0) — orig func_70619_bc, ThePrinceAdult.java:389-413. */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig ThePrinceAdult.java:392-399 — ground spotting: a 1-in-10 roll
        // outside Peaceful; seeing prey launches the adult into flight.
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

        // orig ThePrinceAdult.java:400-412 — the grow counter only ticks (and
        // the King transform only fires) while: idle (activity 0), riderless,
        // not Peaceful, tamed, AND the FullPowerKingEnable config is on.
        if (!this.isVehicle()
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.isTame()
                && OreSpawnConfig.FULL_POWER_KING_ENABLE.get()) {
            ++this.growCounter;
            if (this.growCounter > 288000) { // orig ThePrinceAdult.java:402
                this.transformToKing();
                return;
            }
        }

        alwaysDo(); // orig ThePrinceAdult.java:1085 — every server tick.
    }

    /**
     * orig ThePrinceAdult.java:415-441 ({@code always_do}) — slow regen, target
     * forgiveness, owner creative-flight detection, and the random settle roll.
     */
    private void alwaysDo() {
        if (this.random.nextInt(250) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(5.0f);
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
        // orig :434-440 — 1-in-50 settle roll while uncommitted: 1-in-15 keeps
        // flying, otherwise land (activity 0).
        if (this.random.nextInt(50) == 1 && !this.targetInSight && !this.isVehicle()) {
            if (this.random.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
    }

    /** orig ThePrinceAdult.java:545-547 — block-only line of sight from eye height 0.75. */
    private boolean canSeeSpot(double px, double py, double pz) {
        return this.level().clip(new net.minecraft.world.level.ClipContext(
                new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(px, py, pz),
                net.minecraft.world.level.ClipContext.Block.OUTLINE, // selection bounds, no liquid stop, as orig rayTraceBlocks(start, end, false) — the ENT-S-089 mapping (ENT-S-121)
                net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType()
                == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    /**
     * orig ThePrinceAdult.java:657-814 ({@code fly_without_rider}) — wild
     * flight: vertical damping, the 1-in-6 combat roll (bite + fly-away, or
     * the canon volley within ~24 blocks), flight-target selection
     * (owner-anchored when tame), terrain-following lift, and signum steering
     * with a direct move.
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
            // orig :689-696 — >20 blocks from the owner: drop everything and return.
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
        // orig :701 — vertical damping (the 0.61 arm is unreachable in the
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
        // orig :708-743 — the 1-in-6 combat roll.
        if (!tooFar && this.flyAway == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(6) == 1) {
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
                    // orig :718-723 — badly hurt pet flees away from the threat.
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
                    float meleeRange = 10.0f + e.getBbWidth() / 2.0f;
                    double distSq = this.distanceToSqr(e);
                    if (distSq < (double) (meleeRange * meleeRange)) {
                        // orig :730-733 — bite, then break off for 5-19 ticks.
                        this.doHurtTarget(e);
                        this.flyAway = 5 + this.random.nextInt(15);
                        doNew = true;
                    } else if (distSq < 600.0 && !this.isInWater()
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
        // orig :747-781 — pick a new air target the adult can see: near the
        // owner (spread 8-23, tighter 0-11 when the owner is creative-flying)
        // or a wild 20-34 wander; up to 10 tries.
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
                        zdir = this.random.nextInt(16) + 8;
                        xdir = this.random.nextInt(16) + 8;
                    } else {
                        zdir = this.random.nextInt(12);
                        xdir = this.random.nextInt(12);
                    }
                } else {
                    zdir = this.random.nextInt(15) + 20;
                    xdir = this.random.nextInt(15) + 20;
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
        // orig :782-795 — terrain-following lift: scan the blocks below and
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
        // orig :796-813 — signum steering: base 0.5, 1.75 chasing a flying
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

    /** orig ThePrinceAdult.java:476-517 ({@code isSuitableTarget}). */
    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (MyUtils.isRoyalty(target)) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Mothra) return true;
        if (target instanceof Kraken) return true;
        // orig :504-515 — Leon, WaterDragon, and GammaMetroid are prey while wild.
        if (target instanceof EntityLeon leon) return !leon.isTame();
        if (target instanceof WaterDragon wd) return !wd.isTame();
        if (target instanceof EntityGammaMetroid gm) return !gm.isTame();
        return false;
    }

    /** orig ThePrinceAdult.java:519-535 — nearest suitable prey in a 32/20/32 box. */
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        AABB searchBox = this.getBoundingBox().inflate(32.0, 20.0, 32.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, this.targetSorter, this::isSuitableTarget);
    }

    private void transformToKing() {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) return;
        TheKing king = ModEntities.THE_KING.get().create(serverLevel);
        if (king == null) return;
        king.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        // orig ThePrinceAdult.java:408 — the transformed King starts the
        // "free" end-game sequence (isEnd=1 → enraged full-power phase).
        king.setFree();
        serverLevel.addFreshEntity(king);
        this.discard();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Diamond block: full heal + instant king-growth credit. Unlike the
        // teen, the adult's diamond block does NOT tame (orig
        // ThePrinceAdult.java:1115-1127 — heal + growcounter = 288000 only).
        if (stack.is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.asItem())
                && this.distanceToSqr(player) < 36.0) {
            if (!this.level().isClientSide) {
                this.heal(this.getMaxHealth() - this.getHealth());
                this.growCounter = 288000;
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceAdult.java:1128-1131 — everything below is owner-only;
        // a tamed adult ignores strangers entirely.
        if (!this.isTame() || !this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }
        // (The CAKE growth shortcut and GOLD-INGOT regression that used to
        // live here were port inventions — orig func_70085_c :1109-1249 has
        // neither; the real regression item is a DIAMOND (:1207-1226).
        // Removed; see AUDIT_FINDINGS adult_cake_gold_dup.)

        // Empty hand: saddle-free mount (orig ThePrinceAdult.java:1132-1139 —
        // mounting wakes the dragon and switches it to flight, activity 1).
        if (stack.isEmpty() && this.distanceToSqr(player) < 36.0) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setActivity(1);
                this.setOrderedToSit(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceAdult.java:1140-1155 — beef heals to full.
        if (stack.is(Items.BEEF) && this.distanceToSqr(player) < 36.0) {
            if (this.getMaxHealth() > this.getHealth()) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceAdult.java:1156-1172 — any other food heals nutrition × 10.
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)
                && this.distanceToSqr(player) < 36.0) {
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

        // orig ThePrinceAdult.java:1173-1189 — an ice block extinguishes the fireballs.
        if (stack.is(net.minecraft.world.level.block.Blocks.ICE.asItem())
                && this.distanceToSqr(player) < 36.0) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.entityData.set(DATA_FIRE, 0);
                player.displayClientMessage(Component.literal("Fireballs extinguished."), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceAdult.java:1190-1206 — flint & steel relights them.
        if (stack.is(Items.FLINT_AND_STEEL) && this.distanceToSqr(player) < 36.0) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.entityData.set(DATA_FIRE, 1);
                player.displayClientMessage(Component.literal("Fireballs lit!"), false);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceAdult.java:1207-1226 — a diamond reverts the adult back
        // to a teen Prince.
        if (stack.is(Items.DIAMOND) && this.distanceToSqr(player) < 36.0) {
            if (!this.level().isClientSide) {
                this.revertToTeen(player);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // orig ThePrinceAdult.java:1237-1246 — any other held item toggles the
        // sit and grounds the dragon (activity 0).
        if (!stack.isEmpty() && this.distanceToSqr(player) < 36.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            this.setActivity(0);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    /** orig ThePrinceAdult.java:1207-1226 — diamond regression to "The Young Prince". */
    private void revertToTeen(Player player) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        ThePrinceTeen teen = ModEntities.THE_PRINCE_TEEN.get().create(serverLevel);
        if (teen == null) return;
        teen.moveTo(this.getX(), this.getY(), this.getZ(),
                this.random.nextFloat() * 360.0f, 0.0f);
        if (this.isTame()) {
            teen.tame(player);
        }
        serverLevel.addFreshEntity(teen);
        this.discard();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // orig ThePrinceAdult.java:265-273 — "orespawn:king_living", but only
        // while aggro (activity 1), riderless and not sitting; silent otherwise.
        if (this.isOrderedToSit()) return null;
        if (this.getActivity() == 1 && !this.isVehicle()) {
            return SND_KING_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig ThePrinceAdult.java:275-277 — "orespawn:king_hit".
        return SND_KING_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        // orig ThePrinceAdult.java:279-281 — "orespawn:trex_death".
        return SND_TREX_DEATH;
    }

    @Override protected float getSoundVolume() { return 1.5f; }
    @Override public boolean removeWhenFarAway(double dist) { return false; }
    @Override public boolean isFood(ItemStack stack) { return stack.is(Items.BEEF); }

    @Nullable @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PrinceActivity", this.getActivity());
        tag.putInt("PrinceAttacking", this.getAttacking());
        tag.putInt("PrinceFire", this.entityData.get(DATA_FIRE));
        tag.putInt("PrinceGrow", this.growCounter);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setActivity(tag.getInt("PrinceActivity"));
        this.setAttacking(tag.getInt("PrinceAttacking"));
        this.entityData.set(DATA_FIRE, tag.getInt("PrinceFire"));
        // BOSS-037: legacy migration — orig ThePrinceAdult.java:1318 saved the
        // grow counter under "ThePrinceAdultGrow"; old saves keep progress.
        this.growCounter = tag.contains("PrinceGrow")
                ? tag.getInt("PrinceGrow")
                : tag.getInt("ThePrinceAdultGrow");
    }

    /** orig ThePrinceAdult.java:541-543 — never spawns naturally (growth stage, spawned by promotion only). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return false;
    }
}
