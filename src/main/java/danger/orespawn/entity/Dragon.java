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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import danger.orespawn.ModSounds;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.util.MyUtils;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Dragon extends TamableAnimal implements danger.orespawn.network.RiderInputPayload.RideableFlyer {

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DRAGON_TYPE =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DRAGON_FIRE =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);

    /**
     * Ridden-flight tuning, number-for-number from orig Dragon.java:919-1165
     * (ridden branch of onLivingUpdate): hover probe 1.25 (:935-942 — lift
     * +0.03/+0.1, glide-fall 0.018), terrain scan 3 + v*7 @ 0.05/block ×0.07
     * (:944-956), rise cap 2.0 (:957-959), yaw lag 1.85 above 0.01 (:975-986),
     * fly-up +0.03 + v*0.036 (:1001-1004), throttle 0.025 ramped (max_speed
     * 0.95 :882 — under the >1.0 bonus gate :1018), reverse 0.35 @ -0.02
     * (:1029-1030), friction 0.985/0.94/0.985 (:1163-1165).
     */
    private static final danger.orespawn.entity.ai.RiderFlightController.Config RIDER_FLIGHT_CONFIG =
            new danger.orespawn.entity.ai.RiderFlightController.Config(
                    true, 1.25, 0.03, 0.1, 0.018,
                    3, 7.0, 0.05, 0.07, false,
                    2.0,
                    1.85, 0.01, false,
                    false, 0.03, 0.036,
                    0.025, 0.95, -0.02, 0.35, true,
                    0.0, 0.985, 0.94);

    private final danger.orespawn.entity.ai.RiderFlightController riderFlight =
            new danger.orespawn.entity.ai.RiderFlightController(RIDER_FLIGHT_CONFIG);
    /** Held state of the rider's vertical keys (client-set for prediction, server-set via payload). */
    private boolean riderFlyUp = false;
    private boolean riderFlyDown = false;

    // TF-035: orig Dragon.java:79 declares a GenericTargetSorter field (built at :120,
    // used at :581) — creeper/large-target weighting, not plain distance.
    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.32f;
    private int hurtTimer = 0;
    private int wingSound = 0;
    private boolean targetInSight = false;
    private int ownerFlying = 0;
    private int flyaway = 0;
    private int stuckCount = 0;
    private int lastX = 0;
    private int lastZ = 0;
    private int unstickTimer = 0;
    private int fireballTicker = 0;
    /** Local copy of dragon type (fire vs ice/water); synced with entity data and NBT. */
    private int cachedDragonType = 0;
    @Nullable
    private BlockPos currentFlightTarget = null;

    public Dragon(EntityType<? extends Dragon> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.targetSorter = new danger.orespawn.entity.ai.GenericTargetSorter(this); // orig Dragon.java:120
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.1, 12.0f, 2.0f));
        // orig Dragon.java:1212 — dragons are tempted/tamed/healed with raw beef, not bones
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 9.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig Dragon.java:192 HP 200, :140 ATK 35, :215 armor 14.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 35.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_DRAGON_TYPE, 0);
        builder.define(DATA_DRAGON_FIRE, 1);
    }

    // ==================== Basic Properties ====================

    public int mygetMaxHealth() {
        return 200;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.hasCustomName()) return false;
        if (this.isVehicle()) return false;
        return !this.isTame();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF); // orig Dragon.java:1212 — raw beef (field_151082_bd)
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isTame()) {
            Entity passenger = this.getFirstPassenger();
            if (passenger instanceof Player player && this.isOwnedBy(player)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        Vec3 delta = this.getDeltaMovement();
        this.setDeltaMovement(delta.x, delta.y + 0.25, delta.z);
    }

    // ==================== Sounds ====================

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit()) return null;
        if (this.getAttacking() == 1 && !this.isVehicle()) {
            return ModSounds.ROAR1.get();
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ALO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    @Override
    public float getVoicePitch() {
        return 0.75f;
    }

    // ==================== Loot ====================
    // Death drops live solely in data/orespawn/loot_table/entities/dragon.json
    // (orig Dragon.java:342-347 — raw beef 1-6); the former dropCustomDeathLoot
    // bone override was an invention and double-dropped on top of the table.

    // ==================== Combat ====================

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity) {
            double knockbackStrength = 1.75;
            double verticalKnockback = 0.1;
            float attackDamage = 35.0f;

            if (target instanceof Kraken) attackDamage *= 2.0f;

            target.hurt(this.damageSources().mobAttack(this), attackDamage);

            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            if (target.isRemoved() || target instanceof Player) {
                verticalKnockback *= 2.0;
            }
            target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;

        String msgId = source.getMsgId();
        if (msgId.equals("cactus") || msgId.equals("inFire") || msgId.equals("onFire")
                || msgId.equals("lava") || msgId.equals("inWall")) {
            return false;
        }

        this.setOrderedToSit(false);
        this.setActivity(1);

        Entity attacker = source.getEntity();

        if (this.getDragonType() == 0) {
            if (attacker instanceof BetterFireball) return false;
            if (source.is(net.minecraft.world.damagesource.DamageTypes.FIREBALL)) return false;
            if (source.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)) return false;
        } else {
            if (attacker instanceof IceBall || attacker instanceof WaterBall) return false;
        }

        if (attacker instanceof Dragon) return false;
        if (attacker instanceof EntitySpyro) return false;

        if (this.isTame() && attacker instanceof Player) {
            return false;
        }

        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 20;

        if (attacker instanceof LivingEntity living) {
            this.setTarget(living);
            this.setLastHurtByMob(living);
            this.getNavigation().moveTo(living, 1.2);
        }

        return ret;
    }

    // ==================== AI / Tick ====================

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();

        if (this.hurtTimer > 0) {
            --this.hurtTimer;
        }

        if (this.getActivity() == 1) {
            ++this.wingSound;
            if (this.wingSound > 20) {
                if (!this.level().isClientSide) {
                    // Orig Dragon.java:645 plays the custom "orespawn:MothraWings" event
                    // (volume 0.5f, pitch 1.0f) while flying, NOT the vanilla ender-dragon
                    // flap. sounds.json already defines "mothrawings" (3 random variants),
                    // so reference it by id like Mothra.java does rather than adding a
                    // new registration.
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                                    OreSpawnMod.MOD_ID, "mothrawings")),
                            SoundSource.NEUTRAL, 0.5f, 1.0f);
                }
                this.wingSound = 0;
            }
        }

        if (this.isInWater()) {
            Vec3 delta = this.getDeltaMovement();
            this.setDeltaMovement(delta.x, delta.y + 0.07, delta.z);
        }

        if (this.level().isClientSide) return;

        this.setNoGravity(this.getActivity() == 1);

        if (this.getActivity() == 0 && this.isTame() && this.getOwner() != null
                && !this.isOrderedToSit() && !this.isVehicle()) {
            if (this.distanceToSqr(this.getOwner()) > 144.0) {
                this.setActivity(1);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) return;

        if (!this.isOrderedToSit() && this.getActivity() == 0 && !this.isVehicle()
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.getRandom().nextInt(10) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.setActivity(1);
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isRemoved() || this.level().isClientSide) return;

        if (this.fireballTicker > 0) {
            --this.fireballTicker;
        }

        if (this.getActivity() == 1) {
            if (this.isVehicle() && this.getControllingPassenger() instanceof Player rider) {
                serverRiddenTick(rider);
            } else if (!this.isOrderedToSit()) {
                handleAIFlight();
            }
        }

        handlePassiveBehaviors();
    }

    /**
     * Skips vanilla travel while flying: the wild AI flight (activity 1)
     * moves the dragon itself, and player-ridden movement is fully applied
     * in {@link #tickRidden} via {@code RiderFlightController}, so vanilla
     * travel would integrate the motion twice.
     */
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.getControllingPassenger() instanceof Player) {
            return;
        }
        if (this.getActivity() == 1) {
            return;
        }
        super.travel(travelVector);
    }

    // ==================== Flight with Rider ====================

    /**
     * Client-predicted ridden movement (BUG-020 fix): the riding client runs
     * the original flight physics (orig Dragon.java:919-1165, constants in
     * {@link #RIDER_FLIGHT_CONFIG}) and syncs the vehicle position to the
     * server like a vanilla horse; the server no longer moves the dragon.
     */
    @Override
    protected void tickRidden(Player rider, Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        if (this.isControlledByLocalInstance()) {
            this.riderFlight.tick(this, rider, this.riderFlyUp, this.riderFlyDown);
        }
    }

    /**
     * Server-side portion of the original ridden branch — everything except
     * movement: strafe-key projectile firing (orig Dragon.java:1060-1161),
     * pushing nearby entities (orig :1166-1172, box 2.25/2.0/2.25), the
     * mounted auto-melee {@code fly_with_rider} (orig :486-518), and ejecting
     * a removed rider (orig :1174-1176).
     */
    private void serverRiddenTick(Player rider) {
        if (this.isRemoved() || this.isOrderedToSit()) return;

        if (this.fireballTicker == 0) {
            handleRiderProjectiles(rider);
        }

        AABB pushBox = this.getBoundingBox().inflate(2.25, 2.0, 2.25);
        List<Entity> nearby = this.level().getEntities(this, pushBox);
        for (Entity entity : nearby) {
            if (entity != this.getFirstPassenger() && !entity.isRemoved() && entity.isPushable()) {
                entity.push(this);
            }
        }

        handleRiderCombat();

        if (this.getFirstPassenger() != null && this.getFirstPassenger().isRemoved()) {
            this.ejectPassengers();
        }
    }

    /**
     * Strafe-key projectile firing while ridden (orig Dragon.java:1060-1161):
     * fire dragon — strafe-right small fireball @ 0.15 accel, "random.bow",
     * 10-tick cooldown (orig :1068-1091); strafe-left regular (NOT big)
     * fireball @ 0.1 accel, "random.fuse", 20-tick cooldown (orig :1092-1114).
     * Water dragon — strafe-right WaterBall 1.4f/5.0f, "random.bow", 5 ticks
     * (orig :1121-1137); strafe-left special IceBall 1.4f/5.0f,
     * "fireworks.launch" 0.75f, 15 ticks (orig :1138-1159). Spawn offset
     * xz 4.0 / y -0.25 ahead of the dragon (orig :1063-1064), aimed along the
     * rider's view.
     */
    private void handleRiderProjectiles(Player rider) {
        float strafe = rider.xxa;
        if (Math.abs(strafe) < 0.001f) return;

        double xzoff = 4.0;
        double yoff = -0.25;
        double spawnX = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double spawnY = this.getY() + yoff;
        double spawnZ = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        Vec3 look = rider.getLookAngle();

        if (this.getDragonType() == 0) {
            // Fire dragon
            if (strafe > 0) {
                BetterFireball bf = new BetterFireball(this.level(), this, look);
                bf.setNotMe();
                bf.setSmall();
                bf.setPos(spawnX, spawnY, spawnZ);
                this.level().addFreshEntity(bf);
                // orig :1088 "random.bow"
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 10;
            } else {
                // orig :1093-1094 — plain fireball, no setBig()/setSmall()
                BetterFireball bf = new BetterFireball(this.level(), this, look);
                bf.setNotMe();
                bf.setPos(spawnX, spawnY, spawnZ);
                this.level().addFreshEntity(bf);
                // orig :1111 "random.fuse"
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 1.0f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 20;
            }
        } else {
            // Ice/Water dragon
            if (strafe > 0) {
                WaterBall wb = new WaterBall(this.level(), spawnX, spawnY, spawnZ);
                wb.setOwner(this);
                wb.shoot(look.x, look.y, look.z, 1.4f, 5.0f);
                this.level().addFreshEntity(wb);
                // orig :1134 "random.bow"
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 5;
            } else {
                IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
                ib.setOwner(this);
                ib.setSpecial();
                ib.setPos(spawnX, spawnY, spawnZ);
                ib.shoot(look.x, look.y, look.z, 1.4f, 5.0f);
                this.level().addFreshEntity(ib);
                // orig :1156 "fireworks.launch" 0.75f
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 15;
            }
        }
    }

    private void handleRiderCombat() {
        if (this.isRemoved() || this.isOrderedToSit()) return;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return;
        if (this.getRandom().nextInt(7) != 1) return;

        if (this.getRandom().nextInt(250) == 0) {
            this.setTarget(null);
        }

        LivingEntity target = this.getTarget();
        if (target != null && !target.isAlive()) {
            this.setTarget(null);
            target = null;
        }
        if (target == null) {
            target = findSomethingToAttack();
        }

        if (target != null) {
            this.setAttacking(1);
            float attackRange = 7.0f + target.getBbWidth() / 2.0f;
            if (this.distanceToSqr(target) < attackRange * attackRange) {
                this.doHurtTarget(target);
            }
        } else {
            this.setAttacking(0);
        }
    }

    // ==================== AI Flight (no rider) ====================

    private void handleAIFlight() {
        if (this.isOrderedToSit() || this.isVehicle()) return;

        boolean doNewTarget = false;
        boolean hasOwner = false;
        double ownerX = 0, ownerY = 0, ownerZ = 0;
        boolean tooFar = false;
        LivingEntity owner = null;

        if (this.currentFlightTarget == null) {
            doNewTarget = true;
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.unstickTimer > 0) --this.unstickTimer;

        // Stuck detection
        if (this.lastX == (int) this.getX() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
            if (this.stuckCount > 50) {
                this.stuckCount = 0;
                this.unstickTimer = 100;
                this.targetInSight = false;
                this.setAttacking(0);
                doNewTarget = true;
            }
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastZ = (int) this.getZ();
        }

        // Vertical damping relative to target height
        Vec3 delta = this.getDeltaMovement();
        double motionY = delta.y;
        if (this.getY() < this.currentFlightTarget.getY() + 2.0) {
            motionY *= 0.7;
        } else if (this.getY() > this.currentFlightTarget.getY() - 2.0) {
            motionY *= 0.5;
        } else {
            motionY *= 0.61;
        }

        if (this.getRandom().nextInt(300) == 1) doNewTarget = true;

        // Owner tracking
        if (this.isTame() && this.getOwner() != null) {
            owner = this.getOwner();
            hasOwner = true;
            ownerX = owner.getX();
            ownerY = owner.getY();
            ownerZ = owner.getZ();

            if (this.distanceToSqr(owner) > 144.0) {
                tooFar = true;
                this.targetInSight = false;
                this.setAttacking(0);
                this.flyaway = 0;
                doNewTarget = true;
            }

            if (owner instanceof Player player && player.getAbilities().flying) {
                this.ownerFlying = 1;
            } else {
                this.ownerFlying = 0;
            }
        }

        if (this.flyaway > 0) --this.flyaway;

        // Combat during flight
        if (!tooFar && this.unstickTimer == 0 && this.flyaway == 0
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.getRandom().nextInt(9) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / (float) this.mygetMaxHealth() < 0.25f) {
                    // Low health: flee from target
                    this.setAttacking(0);
                    this.targetInSight = false;
                    doNewTarget = false;
                    int fleeX = (int) (this.getX() + (this.getX() - target.getX()));
                    int fleeZ = (int) (this.getZ() + (this.getZ() - target.getZ()));
                    this.currentFlightTarget = new BlockPos(fleeX, (int) (this.getY() + 1), fleeZ);
                } else {
                    this.setAttacking(1);
                    this.targetInSight = true;
                    this.currentFlightTarget = new BlockPos(
                            (int) target.getX(), (int) (target.getY() + 1), (int) target.getZ());
                    doNewTarget = false;

                    float attackRange = 5.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < attackRange * attackRange) {
                        this.doHurtTarget(target);
                        this.flyaway = 5 + this.getRandom().nextInt(10);
                        doNewTarget = true;
                    } else if (this.distanceToSqr(target) < 256.0
                            && !this.isInWater() && this.getDragonFire() >= 1) {
                        shootProjectileAt(target);
                    }
                }
            } else {
                this.targetInSight = false;
                this.flyaway = 0;
                this.setAttacking(0);
            }
        }

        // Arrived at target
        double distToTarget = this.blockPosition().distSqr(this.currentFlightTarget);
        if (distToTarget < 4.0) doNewTarget = true;

        // Pick new flight target when not pursuing
        if ((doNewTarget && !this.targetInSight) || (doNewTarget && this.flyaway != 0)) {
            pickNewFlightTarget(hasOwner, ownerX, ownerY, ownerZ);
        }

        // Obstacle avoidance
        double velocity = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double obstruction = scanForObstructions(velocity);
        motionY += obstruction * 0.05;
        if (obstruction > 0) {
            this.setPos(this.getX(), this.getY() + obstruction * 0.05, this.getZ());
        }

        // Accelerate toward flight target
        double speedFactor = 0.5;
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (owner != null && this.distanceToSqr(owner) > 49.0) {
                speedFactor = 3.5;
            }
        }

        double targetDx = (this.currentFlightTarget.getX() + 0.5) - this.getX();
        double targetDy = (this.currentFlightTarget.getY() + 0.1) - this.getY();
        double targetDz = (this.currentFlightTarget.getZ() + 0.5) - this.getZ();

        double motionX = delta.x + (Math.signum(targetDx) - delta.x) * 0.15 * speedFactor;
        motionY += (Math.signum(targetDy) - motionY) * 0.21 * speedFactor;
        double motionZ = delta.z + (Math.signum(targetDz) - delta.z) * 0.15 * speedFactor;

        // Face movement direction
        float targetYaw = (float) (Math.atan2(motionZ, motionX) * (180.0 / Math.PI)) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDiff / 4.0f);

        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private void pickNewFlightTarget(boolean hasOwner, double ownerX, double ownerY, double ownerZ) {
        int attempts = 50;
        while (attempts > 0) {
            int gox, goy, goz;
            int rangeX, rangeZ;

            if (hasOwner && this.unstickTimer == 0) {
                gox = (int) ownerX;
                goy = (int) ownerY;
                goz = (int) ownerZ;
                if (this.ownerFlying == 0) {
                    rangeX = this.getRandom().nextInt(10) + 4;
                    rangeZ = this.getRandom().nextInt(10) + 4;
                } else {
                    rangeX = this.getRandom().nextInt(6);
                    rangeZ = this.getRandom().nextInt(6);
                }
            } else {
                gox = (int) this.getX();
                goy = (int) this.getY();
                goz = (int) this.getZ();
                rangeX = this.getRandom().nextInt(10) + 16;
                rangeZ = this.getRandom().nextInt(10) + 16;
            }

            if (this.getRandom().nextBoolean()) rangeX = -rangeX;
            if (this.getRandom().nextBoolean()) rangeZ = -rangeZ;

            int targetY = goy + this.getRandom().nextInt(9 + this.ownerFlying * 2) - 4;
            BlockPos candidate = new BlockPos(gox + rangeX, targetY, goz + rangeZ);

            if (this.level().getBlockState(candidate).isAir()) {
                this.currentFlightTarget = candidate;
                return;
            }
            --attempts;
        }
    }

    private double scanForObstructions(double velocity) {
        double obstruction = 0.0;
        int dist = 2 + (int) (velocity * 4.0);
        float yaw = this.getYRot();

        for (int k = 1; k < dist; k++) {
            for (int i = 1; i < dist * 2; i++) {
                double dx = i * Math.cos(Math.toRadians(yaw + 90.0f));
                double dz = i * Math.sin(Math.toRadians(yaw + 90.0f));
                BlockPos checkPos = BlockPos.containing(this.getX() + dx, this.getY() - k, this.getZ() + dz);
                if (!this.level().getBlockState(checkPos).isAir()) {
                    obstruction += 0.05;
                }
            }
        }
        return obstruction;
    }

    private void shootProjectileAt(LivingEntity target) {
        double yoff = 1.25;
        double xzoff = 2.25;
        double spawnX = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double spawnY = this.getY() + yoff;
        double spawnZ = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        double dirX = target.getX() - spawnX;
        double dirY = target.getY() + (target.getBbHeight() / 2.0f) - spawnY;
        double dirZ = target.getZ() - spawnZ;

        if (this.cachedDragonType == 0) {
            // Fire dragon
            if (this.getDragonFire() == 1) {
                net.minecraft.world.entity.projectile.SmallFireball sf =
                        new net.minecraft.world.entity.projectile.SmallFireball(
                                this.level(), this, new Vec3(dirX, dirY, dirZ));
                sf.moveTo(spawnX, spawnY, spawnZ, this.getYRot(), 0.0f);
                this.level().addFreshEntity(sf);
            } else {
                BetterFireball bf = new BetterFireball(this.level(), this, new Vec3(dirX, dirY, dirZ));
                bf.moveTo(spawnX, spawnY, spawnZ, this.getYRot(), 0.0f);
                bf.setPos(spawnX, spawnY, spawnZ);
                this.level().addFreshEntity(bf);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    this.getDragonFire() == 1 ? SoundEvents.BLAZE_SHOOT : SoundEvents.TNT_PRIMED,
                    SoundSource.NEUTRAL,
                    this.getDragonFire() == 1 ? 0.75f : 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        } else {
            // Ice/Water dragon
            float horizDist = Mth.sqrt((float)(dirX * dirX + dirZ * dirZ)) * 0.2f;
            if (this.getDragonFire() == 1) {
                WaterBall wb = new WaterBall(this.level(), spawnX, spawnY, spawnZ);
                wb.setOwner(this);
                wb.shoot(dirX, dirY + horizDist, dirZ, 1.4f, 5.0f);
                this.level().addFreshEntity(wb);
            } else {
                IceBall ib = new IceBall(ModEntities.ICE_BALL.get(), this.level());
                ib.setOwner(this);
                ib.setSpecial();
                ib.setPos(spawnX, spawnY, spawnZ);
                ib.shoot(dirX, dirY + horizDist, dirZ, 1.4f, 5.0f);
                this.level().addFreshEntity(ib);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    this.getDragonFire() == 1 ? SoundEvents.ARROW_SHOOT : SoundEvents.TNT_PRIMED,
                    SoundSource.NEUTRAL,
                    this.getDragonFire() == 1 ? 0.75f : 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        }
    }

    // ==================== Passive Behaviors ====================

    private void handlePassiveBehaviors() {
        if (this.level().isClientSide) return;

        if (this.getRandom().nextInt(250) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }

        if (this.isOrderedToSit()) return;

        this.ownerFlying = 0;
        if (this.isTame() && this.getOwner() != null && !this.isVehicle()) {
            if (this.getOwner() instanceof Player player && player.getAbilities().flying) {
                this.ownerFlying = 1;
                this.setActivity(1);
            }
        }

        if (this.isTame() && this.getOwner() != null && !this.isVehicle()) {
            if (this.distanceToSqr(this.getOwner()) > 400.0) {
                this.setActivity(1);
            }
        }

        // Random activity switching when idle
        if (this.getRandom().nextInt(50) == 1 && !this.targetInSight && !this.isVehicle()) {
            if (this.getRandom().nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }

        // Water-seeking for healing
        if (this.getRandom().nextInt(25) == 0 && !this.targetInSight && !this.isVehicle()) {
            BlockPos waterPos = findNearbyWater();
            if (waterPos != null) {
                this.setActivity(0);
                this.getNavigation().moveTo(waterPos.getX(), waterPos.getY() - 1, waterPos.getZ(), 1.0);
                if (this.isInWater()) {
                    this.heal(1.0f);
                    this.playSound(SoundEvents.GENERIC_SPLASH, 1.0f,
                            this.getRandom().nextFloat() * 0.2f + 0.9f);
                }
            }
        }
    }

    @Nullable
    private BlockPos findNearbyWater() {
        BlockPos center = this.blockPosition().below();
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int range = 1; range <= 10; range++) {
            int yRange = Math.min(range, 4);
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -yRange; dy <= yRange; dy++) {
                    for (int dz = -range; dz <= range; dz++) {
                        // Only scan the shell faces of the expanding cube
                        if (Math.abs(dx) < range && Math.abs(dy) < yRange && Math.abs(dz) < range) continue;
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (this.level().getBlockState(pos).is(Blocks.WATER)) {
                            double dist = center.distSqr(pos);
                            if (dist < closestDist) {
                                closestDist = dist;
                                closest = pos;
                            }
                        }
                    }
                }
            }
            if (closest != null && range >= 6) break;
        }
        return closest;
    }

    // ==================== Target Finding ====================

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;

        if (target instanceof Dragon) return false;
        if (MyUtils.isAlly(target)) return false;

        if (target instanceof Monster) return true;
        if (target instanceof Mothra) return true;
        if (target instanceof Kraken) return true;

        if (target instanceof Player) return false;

        return false;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(20.0, 20.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity entity : entities) {
            if (isSuitableTarget(entity)) return entity;
        }
        return null;
    }

    // ==================== Interaction ====================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.isTame()) {
            // orig Dragon.java:1212-1219 — raw beef, 1-in-5 tame chance, heal to full on success
            if (stack.is(Items.BEEF) && this.distanceToSqr(player) < 25.0) {
                if (!this.level().isClientSide) {
                    if (this.getRandom().nextInt(5) == 1) {
                        this.setTame(true, false);
                        this.setOwnerUUID(player.getUUID());
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.mygetMaxHealth() - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }

        // === Tamed from here ===
        if (!this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }

        // Empty hand: mount and ride
        if (stack.isEmpty() && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setActivity(1);
                this.setOrderedToSit(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Raw beef: heal to full (orig Dragon.java:1245-1252)
        if (stack.is(Items.BEEF) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (this.getHealth() < this.mygetMaxHealth()) {
                this.heal(this.mygetMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Dead Bush: release (untame) (TF-019; orig Dragon.java:1261-1275 — the
        // original has no TNT branch, so TNT falls through to the any-item sit toggle)
        if (stack.is(Blocks.DEAD_BUSH.asItem()) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Ice: extinguish fireballs (TF-020; orig Dragon.java:1276-1290 —
        // ownership-gated by the isOwnedBy check atop this section, same message)
        if (stack.is(Blocks.ICE.asItem()) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.setDragonFire(0);
                player.displayClientMessage(Component.literal("Dragon fireballs extinguished."), false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Flint and Steel: light fireballs
        if (stack.is(Items.FLINT_AND_STEEL) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.setDragonFire(1);
                player.displayClientMessage(Component.literal("Dragon fireballs lit!"), false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Gunpowder: supercharge (requires fire already lit)
        if (stack.is(Items.GUNPOWDER) && this.distanceToSqr(player) < 25.0
                && this.getDragonFire() > 0) {
            if (!this.level().isClientSide) {
                this.setDragonFire(2);
                player.displayClientMessage(Component.literal("Dragon fireballs supercharged!"), false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Water Bucket: change to ice/water dragon
        if (stack.is(Items.WATER_BUCKET) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.cachedDragonType = 1;
                this.setDragonType(1);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Lava Bucket: change to fire dragon
        if (stack.is(Items.LAVA_BUCKET) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.cachedDragonType = 0;
                this.setDragonType(0);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Diamond: spawn a Spyro (orig Dragon.java:1351-1369 — the "Baby Dragon"
        // registry name is the Spyro class). The adult is discarded; the baby is
        // tamed to the player when the adult was tame. No type/fire transfer in
        // the original — Spyro carries its own state.
        if (stack.is(Items.DIAMOND) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                EntitySpyro baby = ModEntities.ENTITY_SPYRO.get().create(this.level());
                if (baby != null) {
                    baby.moveTo(this.getX(), this.getY(), this.getZ(),
                            this.getRandom().nextFloat() * 360.0f, 0.0f);
                    if (this.isTame()) {
                        baby.setTame(true, false);
                        baby.setOwnerUUID(player.getUUID());
                    }
                    this.level().addFreshEntity(baby);
                    this.discard();
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        // Any other item: toggle sit
        if (!stack.isEmpty() && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                boolean wasSitting = this.isOrderedToSit();
                this.setOrderedToSit(!wasSitting);
                this.setActivity(0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    // ==================== Rider Positioning ====================

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        float mountForwardOffset = 0.65f;
        double rx = this.getX() - mountForwardOffset * Math.sin(Math.toRadians(this.getYRot()));
        double ry = this.getY() + 1.3;
        double rz = this.getZ() + mountForwardOffset * Math.cos(Math.toRadians(this.getYRot()));
        callback.accept(passenger, rx, ry, rz);
    }

    // ==================== Synched Data Accessors ====================

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    public void setActivity(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ACTIVITY, value);
    }

    public int getDragonType() {
        return this.entityData.get(DATA_DRAGON_TYPE);
    }

    public void setDragonType(int value) {
        this.entityData.set(DATA_DRAGON_TYPE, value);
    }

    public int getDragonFire() {
        return this.entityData.get(DATA_DRAGON_FIRE);
    }

    public void setDragonFire(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_DRAGON_FIRE, value);
    }

    public int getDragonHealth() {
        return (int) this.getHealth();
    }

    // ==================== NBT Save/Load ====================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DragonAttacking", this.getAttacking());
        tag.putInt("DragonActivity", this.getActivity());
        tag.putInt("DragonFire", this.getDragonFire());
        tag.putInt("DragonType", this.getDragonType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getInt("DragonAttacking"));
        this.setActivity(tag.getInt("DragonActivity"));
        this.setDragonFire(tag.getInt("DragonFire"));
        this.cachedDragonType = tag.getInt("DragonType");
        this.setDragonType(this.cachedDragonType);
    }

    // ==================== TamableAnimal / AgeableMob ====================

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    // ==================== RideableFlyer Interface ====================

    /**
     * {@inheritDoc}
     *
     * <p>Consumed by the flight physics in {@link #travelRidden}; matches the
     * original's polling of {@code OreSpawnMain.flyup_keystate} (orig
     * Dragon.java:1001-1004). No {@code riderSpecial} — the original Dragon
     * had no special-key action (its ranged attacks are strafe-fired); the
     * previous "ignite fireballs" implementation was invented and removed.</p>
     */
    @Override
    public void setRiderVerticalInput(boolean up, boolean down) {
        this.riderFlyUp = up;
        this.riderFlyDown = down;
    }

    /** orig Dragon.java:598-611 — daytime; no other Dragon within 16/6/16; Islands always allowed; otherwise y>=50. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (OriginalSpawnGates.anyOtherNearby(this, level, Dragon.class, 16.0, 6.0, 16.0)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;
        return this.getY() >= 50.0;
    }
}
