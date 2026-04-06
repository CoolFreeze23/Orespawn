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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Dragon extends TamableAnimal {

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DRAGON_TYPE =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DRAGON_FIRE =
            SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);

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
    private float deltaSmooth = 0.0f;
    /** Local copy of dragon type (fire vs ice/water); synced with entity data and NBT. */
    private int cachedDragonType = 0;
    @Nullable
    private BlockPos currentFlightTarget = null;

    public Dragon(EntityType<? extends Dragon> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.1, 12.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.BONE), false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 9.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 35.0)
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
        return stack.is(Items.BONE);
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
            return SoundEvents.ENDER_DRAGON_GROWL; // TODO: replace with custom "orespawn:roar"
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT; // TODO: replace with custom "orespawn:alo_hurt"
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH; // TODO: replace with custom "orespawn:alo_death"
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

    private void dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(5) - this.getRandom().nextInt(5);
        double oy = this.getY() + 1.0;
        double oz = this.getZ() + this.getRandom().nextInt(5) - this.getRandom().nextInt(5);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = 1 + this.getRandom().nextInt(6);
        for (int i = 0; i < count; i++) {
            dropItemRand(new ItemStack(Items.BONE, 1));
        }
    }

    // ==================== Combat ====================

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity) {
            double knockbackStrength = 1.75;
            double verticalKnockback = 0.1;
            float attackDamage = 35.0f;

            // TODO: if (target instanceof Kraken) attackDamage *= 2.0f;

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

        // TODO: Ignore BetterFireball damage if fire dragon (dragontype == 0)
        // TODO: Ignore IceBall/WaterBall damage if non-fire dragon (dragontype != 0)
        // TODO: Ignore SmallFireball damage if fire dragon

        if (attacker instanceof Dragon) return false;
        // TODO: if (attacker instanceof Spyro) return false;

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
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENDER_DRAGON_FLAP, SoundSource.NEUTRAL, 0.5f, 1.0f);
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
                handleRiderFlight(rider);
            } else if (!this.isOrderedToSit()) {
                handleAIFlight();
            }
        }

        handlePassiveBehaviors();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.getActivity() == 1) {
            return;
        }
        super.travel(travelVector);
    }

    // ==================== Flight with Rider ====================

    private void handleRiderFlight(Player rider) {
        if (this.isRemoved() || this.isOrderedToSit()) return;

        Vec3 delta = this.getDeltaMovement();
        double motionX = Mth.clamp(delta.x, -2.0, 2.0);
        double motionY = delta.y;
        double motionZ = Mth.clamp(delta.z, -2.0, 2.0);
        double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);

        // Ground proximity: push up near blocks, apply gravity when clear
        BlockPos belowPos = BlockPos.containing(this.getX(), this.getY() - 1.25, this.getZ());
        if (!this.level().getBlockState(belowPos).isAir()) {
            motionY += 0.03;
            this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
        } else {
            motionY -= 0.018;
        }

        // Obstacle avoidance
        double obstruction = scanForObstructions(velocity);
        motionY += obstruction * 0.07;
        if (obstruction > 0) {
            this.setPos(this.getX(), this.getY() + obstruction * 0.07, this.getZ());
        }
        motionY = Math.min(motionY, 2.0);

        // Smooth yaw interpolation from rider, with turning radius based on velocity
        double yawDiff = Mth.wrapDegrees(rider.getYRot() - this.getYRot());
        if (yawDiff > 90) yawDiff -= 180;
        if (yawDiff < -90) yawDiff += 180;

        if (velocity > 0.01) {
            double turnRate = Mth.clamp(Math.abs(1.85 - velocity), 0.01, 0.9);
            this.setYRot(rider.getYRot() + (float) (yawDiff * turnRate));
        } else {
            this.setYRot(rider.getYRot());
        }
        this.setXRot(2.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        this.yHeadRot = this.getYRot();

        // Resolve current velocity direction vs. rider direction
        double riderDirAngle = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        double currentAngle = Math.atan2(motionZ, motionX);
        double angleDiff = Math.abs(currentAngle - riderDirAngle) % (Math.PI * 2);
        if (angleDiff > Math.PI) angleDiff -= Math.PI * 2;
        angleDiff = Math.abs(angleDiff);
        double newVelocity = velocity;
        if (Math.abs(newVelocity) < 0.01) angleDiff = 0;
        if (angleDiff > 1.5) newVelocity = -newVelocity;

        // Forward/backward from rider input
        float forwardInput = rider.zza;
        double maxSpeed = 0.95;
        if (Math.abs(forwardInput) > 0.001f) {
            double deltav;
            if (forwardInput > 0) {
                deltav = maxSpeed > 1.0 ? 0.075 : 0.025;
                if (this.deltaSmooth < 0) this.deltaSmooth = 0;
                this.deltaSmooth = (float) Math.min(this.deltaSmooth + deltav / 10.0, deltav);
            } else {
                maxSpeed = 0.35;
                deltav = -0.02;
                if (this.deltaSmooth > 0) this.deltaSmooth = 0;
                this.deltaSmooth = (float) Math.max(this.deltaSmooth + deltav / 10.0, deltav);
            }

            newVelocity += this.deltaSmooth;
            double moveAngle;
            if (newVelocity >= 0) {
                newVelocity = Math.min(newVelocity, maxSpeed);
                moveAngle = Math.toRadians(this.getYRot() + 90.0f);
            } else {
                newVelocity = Math.max(newVelocity, -maxSpeed);
                newVelocity = -newVelocity;
                moveAngle = Math.toRadians(this.getYRot() + 270.0f);
            }
            motionX = Math.cos(moveAngle) * newVelocity;
            motionZ = Math.sin(moveAngle) * newVelocity;
        } else {
            double moveAngle;
            if (newVelocity >= 0) {
                moveAngle = Math.toRadians(this.getYRot() + 90.0f);
            } else {
                newVelocity = -newVelocity;
                moveAngle = Math.toRadians(this.getYRot() + 270.0f);
            }
            motionX = Math.cos(moveAngle) * newVelocity;
            motionZ = Math.sin(moveAngle) * newVelocity;
        }

        // TODO: Handle flyup keybinding (needs custom packet from client)
        // When flyup key is pressed: motionY += 0.03 + velocity * 0.036;

        // Rider projectile firing
        if (this.fireballTicker == 0) {
            handleRiderProjectiles(rider);
        }

        // Apply movement
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Velocity damping
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(dm.x * 0.985, dm.y * 0.94, dm.z * 0.985);

        // Push nearby entities
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

    private void handleRiderProjectiles(Player rider) {
        float strafe = rider.xxa;
        if (Math.abs(strafe) < 0.001f) return;

        double xzoff = 4.0;
        double yoff = -0.25;
        double spawnX = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double spawnY = this.getY() + yoff;
        double spawnZ = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.getDragonType() == 0) {
            // Fire dragon
            if (strafe > 0) {
                // TODO: Spawn BetterFireball aimed in rider's look direction
                // BetterFireball bf = new BetterFireball(level(), this, ...);
                // bf.setNotMe(); bf.setSmall();
                // bf.setPos(spawnX, spawnY, spawnZ);
                // level().addFreshEntity(bf);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 10;
            } else {
                // TODO: Spawn large BetterFireball aimed in rider's look direction
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 1.0f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 20;
            }
        } else {
            // Ice/Water dragon
            if (strafe > 0) {
                // TODO: Spawn WaterBall aimed in rider's look direction
                // WaterBall wb = new WaterBall(level(), spawnX, spawnY, spawnZ);
                // wb.moveTo(spawnX, spawnY, spawnZ, rider.getYRot() + 90.0f, rider.getXRot());
                // wb.shoot(dirX, dirY, dirZ, 1.4f, 5.0f);
                // level().addFreshEntity(wb);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.fireballTicker = 5;
            } else {
                // TODO: Spawn IceBall aimed in rider's look direction
                // IceBall ib = new IceBall(level(), spawnX, spawnY, spawnZ);
                // ib.setSpecial(); ib.setIceBall();
                // ib.shoot(dirX, dirY, dirZ, 1.4f, 5.0f);
                // level().addFreshEntity(ib);
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
                // TODO: Spawn SmallFireball at (spawnX, spawnY, spawnZ) aimed at target
                // SmallFireball sf = new SmallFireball(level(), this, dirX, dirY, dirZ);
                // sf.moveTo(spawnX, spawnY, spawnZ, this.getYRot(), 0.0f);
                // level().addFreshEntity(sf);
            } else {
                // TODO: Spawn BetterFireball at (spawnX, spawnY, spawnZ) aimed at target
                // BetterFireball bf = new BetterFireball(level(), this, dirX, dirY, dirZ);
                // bf.moveTo(spawnX, spawnY, spawnZ, this.getYRot(), 0.0f);
                // level().addFreshEntity(bf);
            }
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    this.getDragonFire() == 1 ? SoundEvents.BLAZE_SHOOT : SoundEvents.TNT_PRIMED,
                    SoundSource.NEUTRAL,
                    this.getDragonFire() == 1 ? 0.75f : 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        } else {
            // Ice/Water dragon
            if (this.getDragonFire() == 1) {
                // TODO: Spawn WaterBall at (spawnX, spawnY, spawnZ) aimed at target
                // WaterBall wb = new WaterBall(level(), dirX, dirY, dirZ);
                // wb.moveTo(spawnX, spawnY, spawnZ);
                // wb.shoot(dirX, dirY + horizDist*0.2, dirZ, 1.4f, 5.0f);
                // level().addFreshEntity(wb);
            } else {
                // TODO: Spawn IceBall at (spawnX, spawnY, spawnZ) aimed at target
                // IceBall ib = new IceBall(level(), dirX, dirY, dirZ);
                // ib.setSpecial(); ib.setIceBall();
                // ib.moveTo(spawnX, spawnY, spawnZ);
                // ib.shoot(dirX, dirY + horizDist*0.2, dirZ, 1.4f, 5.0f);
                // level().addFreshEntity(ib);
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
        // TODO: Exclude OreSpawn allies: LurkingTerror, EnderReaper, TerribleTerror,
        //       LeafMonster, CreepingHorror, Triffid, Spyro

        if (target instanceof Monster) return true;
        // TODO: if (target instanceof Mothra) return true;
        // TODO: if (target instanceof Kraken) return true;

        if (target instanceof Player) return false;

        return false;
    }

    private LivingEntity findSomethingToAttack() {
        // TODO: Check OreSpawnMain.PlayNicely config; if != 0 return null
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
            if (stack.is(Items.BONE) && this.distanceToSqr(player) < 25.0) {
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

        // Bone: heal to full
        if (stack.is(Items.BONE) && this.distanceToSqr(player) < 25.0) {
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

        // TNT: release (untame)
        if (stack.is(Items.TNT) && this.distanceToSqr(player) < 25.0) {
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

        // Soul Sand: extinguish fireballs
        if (stack.is(Blocks.SOUL_SAND.asItem()) && this.distanceToSqr(player) < 25.0) {
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

        // Apple: spawn baby dragon
        if (stack.is(Items.APPLE) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                // TODO: Spawn Baby Dragon (Spyro) entity when Spyro class is ported
                // Spyro baby = new Spyro(ModEntityTypes.SPYRO.get(), this.level());
                // baby.moveTo(this.getX(), this.getY(), this.getZ(),
                //         this.getRandom().nextFloat() * 360.0f, 0.0f);
                // if (this.isTame()) {
                //     baby.setTame(true, false);
                //     baby.setOwnerUUID(player.getUUID());
                // }
                // this.level().addFreshEntity(baby);
                // this.discard();
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
}
