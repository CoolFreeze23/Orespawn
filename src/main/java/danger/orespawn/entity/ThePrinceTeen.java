package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;

public class ThePrinceTeen extends TamableAnimal
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer {
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

    public ThePrinceTeen(EntityType<? extends ThePrinceTeen> type, Level level) {
        super(type, level);
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
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
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

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.random.nextInt(100) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
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

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 25;
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
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
     * vanilla travel too would integrate the motion twice.
     */
    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
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

        if (this.fireballTicker > 0) {
            --this.fireballTicker;
        }
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

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.isDay == 0) {
            this.isDay = 1;
            if (!this.level().isDay()) this.isDay = -1;
        } else {
            if (this.isDay == -1 && this.level().isDay()) ++this.dayCount;
            this.isDay = this.level().isDay() ? 1 : -1;
        }

        if (this.killCount > 25 && this.dayCount > 10) {
            this.transformToAdult();
            return;
        }

        // While ridden, ground combat is replaced by the original's ridden
        // duties (orig ThePrinceTeen.java:879-1098 ran instead of the AI path).
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player rider) {
            serverRiddenTick(rider);
            return;
        }

        if (this.random.nextInt(7) == 1) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.isAlive()) { this.setTarget(null); target = null; }
            if (target == null) target = findSomethingToAttack();
            if (target != null) {
                this.getNavigation().moveTo(target, 1.5);
                this.lookAt(target, 10.0f, 10.0f);
                this.setAttacking(1);
                double meleeRange = 6.0 + target.getBbWidth() / 2.0;
                if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
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

    private LivingEntity findSomethingToAttack() {
        AABB box = this.getBoundingBox().inflate(20.0, 10.0, 20.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity t : targets) {
            if (t == this || !t.isAlive()) continue;
            if (!this.getSensing().hasLineOfSight(t)) continue;
            if (t instanceof Monster) return t;
        }
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Diamond block: tame + full heal + instant adult-growth credit (orig
        // ThePrinceTeen.java:1133-1150 — kill_count/day_count forced to 1000).
        if (stack.is(net.minecraft.world.level.block.Blocks.DIAMOND_BLOCK.asItem())
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                if (!this.isTame()) {
                    this.tame(player);
                }
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
                this.killCount = 1000;
                this.dayCount = 1000;
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Empty hand: saddle-free mount (orig ThePrinceTeen.java:1151-1162 —
        // tamed owner within 25 sq blocks; mounting wakes the dragon and
        // switches it to flight, activity 1).
        if (this.isTame() && this.isOwnedBy(player)
                && stack.isEmpty() && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setActivity(1);
                this.setOrderedToSit(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && this.distanceToSqr(player) < 25.0) {
            if (stack.is(Items.CAKE)) {
                if (!this.level().isClientSide) {
                    this.killCount = 1000;
                    this.dayCount = 1000;
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // BOSS-029: the gold-ingot teen→baby regression that used to live
            // here was a port invention — orig ThePrinceTeen.java:1127-1230
            // (func_70085_c) has no shrink-back interaction. Removed.
        }

        if (stack.has(net.minecraft.core.component.DataComponents.FOOD) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) this.heal(this.getMaxHealth() - this.getHealth());
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar")); }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt")); }
    @Override protected SoundEvent getDeathSound() { return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death")); }
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
}
