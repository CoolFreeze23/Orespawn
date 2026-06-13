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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;

public class ThePrinceAdult extends TamableAnimal
        implements danger.orespawn.network.RiderInputPayload.RideableFlyer {
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

    public ThePrinceAdult(EntityType<? extends ThePrinceAdult> type, Level level) {
        super(type, level);
        this.xpReward = 3000;
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
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 20.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
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
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.random.nextInt(100) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(5.0f);
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

        if (this.fireballTicker > 0) {
            --this.fireballTicker;
        }
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

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig ThePrinceAdult.java:400-412 — the grow counter only ticks (and
        // the King transform only fires) while: idle (activity 0), riderless,
        // not Peaceful, tamed, AND the FullPowerKingEnable config is on.
        if (this.getActivity() == 0 && !this.isVehicle()
                && this.level().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && this.isTame()
                && danger.orespawn.OreSpawnConfig.FULL_POWER_KING_ENABLE.get()) {
            ++this.growCounter;
            if (this.growCounter > 288000) { // orig ThePrinceAdult.java:402
                this.transformToKing();
                return;
            }
        }

        // While ridden, ground combat is replaced by the original's ridden
        // duties (orig ThePrinceAdult.java:859-1080 ran instead of the AI path).
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player rider) {
            serverRiddenTick(rider);
            return;
        }

        if (this.random.nextInt(7) == 1) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.isAlive()) {
                this.setTarget(null);
                target = null;
            }
            if (target == null) {
                target = findSomethingToAttack();
            }
            if (target != null) {
                this.getNavigation().moveTo(target, 1.5);
                this.lookAt(target, 10.0f, 10.0f);
                this.setAttacking(1);
                double meleeRange = 8.0 + target.getBbWidth() / 2.0;
                if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(32.0, 16.0, 32.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity target : targets) {
            if (target == this || !target.isAlive()) continue;
            if (!this.getSensing().hasLineOfSight(target)) continue;
            if (MyUtils.isRoyalty(target) || MyUtils.isAlly(target)) continue;
            if (target instanceof Monster) return target;
        }
        return null;
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

        // Empty hand: saddle-free mount (orig ThePrinceAdult.java:1128-1139 —
        // tamed owner within 36 sq blocks; mounting wakes the dragon and
        // switches it to flight, activity 1).
        if (this.isTame() && this.isOwnedBy(player)
                && stack.isEmpty() && this.distanceToSqr(player) < 36.0) {
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
                    this.growCounter = 288000;
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.is(Items.GOLD_INGOT)) {
                if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
                    ThePrinceTeen teen = ModEntities.THE_PRINCE_TEEN.get().create(serverLevel);
                    if (teen != null) {
                        teen.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                        if (this.getOwnerUUID() != null) {
                            Player downgradeOwner = this.level().getPlayerByUUID(this.getOwnerUUID());
                            if (downgradeOwner != null) {
                                teen.tame(downgradeOwner);
                            } else {
                                // Owner offline: tame(null) would NPE (BUG-004).
                                teen.setOwnerUUID(this.getOwnerUUID());
                                teen.setTame(true, true);
                            }
                        }
                        serverLevel.addFreshEntity(teen);
                        this.discard();
                    }
                }
                if (!player.getAbilities().instabuild) stack.shrink(1);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // orig ThePrinceAdult.java:265-273 — "orespawn:king_living", but only
        // while aggro (activity 1), riderless and not sitting; silent otherwise.
        if (this.isOrderedToSit()) return null;
        if (this.getActivity() == 1 && !this.isVehicle()) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "king_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // orig ThePrinceAdult.java:275-277 — "orespawn:king_hit".
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "king_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        // orig ThePrinceAdult.java:279-281 — "orespawn:trex_death".
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "trex_death"));
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
        this.growCounter = tag.getInt("PrinceGrow");
    }

    /** orig ThePrinceAdult.java:541-543 — never spawns naturally (growth stage, spawned by promotion only). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return false;
    }
}
