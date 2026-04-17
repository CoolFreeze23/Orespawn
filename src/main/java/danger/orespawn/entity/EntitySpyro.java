package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class EntitySpyro extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(EntitySpyro.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SPYRO_FIRE =
            SynchedEntityData.defineId(EntitySpyro.class, EntityDataSerializers.INT);

    private static final double OWNER_FAR_DIST_SQ = 256.0;
    private static final float LOW_HEALTH_FRACTION = 0.25f;

    private BlockPos currentFlightTarget = null;
    public int activity = 1;
    private int ownerFlying = 0;
    private boolean targetInSight = false;
    private final float moveSpeed = 0.3f;

    public EntitySpyro(EntityType<? extends EntitySpyro> type, Level level) {
        super(type, level);
        this.xpReward = 35;
        this.noPhysics = false;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 0.3, 0.4));
        this.goalSelector.addGoal(3, new MyEntityAIFollowOwner(this, 1.15, 12.0f, 2.0f));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, Ingredient.of(Items.COOKED_BEEF), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, e -> this.isTame()));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_SPYRO_FIRE, 1);
    }

    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    public void setActivity(int val) {
        this.activity = val;
        this.entityData.set(DATA_ACTIVITY, val);
    }

    public int getSpyroFire() {
        return this.entityData.get(DATA_SPYRO_FIRE);
    }

    public void setSpyroFire(int val) {
        this.entityData.set(DATA_SPYRO_FIRE, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return !this.isTame();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();

        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y + 0.07, motion.z);
        }

        if (this.level().isClientSide) return;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.activity == 2) {
            Vec3 motion = this.getDeltaMovement();
            double newY;
            if (this.getY() < this.currentFlightTarget.getY() + 2) {
                newY = motion.y * 0.7;
            } else if (this.getY() > this.currentFlightTarget.getY() - 2) {
                newY = motion.y * 0.5;
            } else {
                newY = motion.y * 0.61;
            }
            this.setDeltaMovement(motion.x, newY, motion.z);
        }

        if (this.activity == 1 && this.isTame() && this.getOwner() != null &&
                this.distanceToSqr(this.getOwner()) > OWNER_FAR_DIST_SQ) {
            this.setActivity(2);
        }

        doMovement();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.COOKED_BEEF) && player.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 1) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(200.0f - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                this.heal(200.0f - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && stack.is(Items.FLINT_AND_STEEL)) {
            if (!this.level().isClientSide) {
                this.setSpyroFire(1);
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && stack.is(Items.WATER_BUCKET)) {
            if (!this.level().isClientSide) {
                this.setSpyroFire(0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && player.distanceToSqr(this) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.activity == 1) {
            super.customServerAiStep();
        }

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.random.nextInt(100) == 1 && this.getHealth() < 200.0f) {
            this.heal(1.0f);
        }

        if (!this.isOrderedToSit()) {
            if (this.activity == 0) setActivity(1);
            if (this.random.nextInt(100) == 1 && !this.targetInSight) {
                this.activity = this.random.nextInt(8) == 1 ? 2 : 1;
                setActivity(this.activity);
            }

            this.ownerFlying = 0;
            if (this.isTame() && this.getOwner() instanceof Player ownerPlayer) {
                if (ownerPlayer.getAbilities().flying) {
                    this.ownerFlying = 1;
                    setActivity(2);
                }
            }
        }
    }

    private void doMovement() {
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        if (this.isOrderedToSit()) return;
        if (this.activity == 1) return;

        boolean doNew = false;
        boolean hasOwner = false;
        double ox = 0, oy = 0, oz = 0;
        LivingEntity owner = null;

        if (this.getActivity() == 2 && this.random.nextInt(300) == 0) doNew = true;

        if (this.isTame() && this.getOwner() != null) {
            owner = this.getOwner();
            hasOwner = true;
            ox = owner.getX(); oy = owner.getY(); oz = owner.getZ();
            if (this.distanceToSqr(owner) > 100.0) doNew = true;
            if (this.ownerFlying != 0 && this.distanceToSqr(owner) > 36.0) doNew = true;
        }

        if (this.random.nextInt(6) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / 200.0f < LOW_HEALTH_FRACTION) {
                    setActivity(2);
                    this.targetInSight = false;
                    doNew = false;
                    this.currentFlightTarget = BlockPos.containing(
                            this.getX() + (this.getX() - target.getX()),
                            this.getY() + 1, this.getZ() + (this.getZ() - target.getZ()));
                } else {
                    setActivity(2);
                    this.targetInSight = true;
                    this.currentFlightTarget = BlockPos.containing(target.getX(), target.getY() + 1, target.getZ());
                    this.getNavigation().moveTo(target, 1.25);
                    doNew = false;
                    double distSq = this.distanceToSqr(target);
                    float reach = 3.0f + target.getBbWidth() / 2.0f;
                    if (distSq < (double)(reach * reach)) {
                        this.doHurtTarget(target);
                    } else if (distSq < 64.0 && !this.isInWater() && this.getSpyroFire() == 1 &&
                            (this.random.nextInt(10) == 0 || this.random.nextInt(15) == 1)) {
                        shootFireball(target);
                    }
                }
            } else {
                this.targetInSight = false;
            }
        }

        if (this.currentFlightTarget.closerToCenterThan(this.position(), 2.1) && this.getActivity() != 3) {
            doNew = true;
        }

        if (doNew && !this.targetInSight) {
            int keepTrying = 50;
            boolean found = false;
            while (!found && keepTrying > 0) {
                int gox = (int)(hasOwner ? ox : this.getX());
                int goy = (int)(hasOwner ? oy : this.getY());
                int goz = (int)(hasOwner ? oz : this.getZ());
                int xdir, zdir;
                if (hasOwner && this.ownerFlying == 0) {
                    xdir = this.random.nextInt(4) + 6;
                    zdir = this.random.nextInt(4) + 6;
                } else if (hasOwner) {
                    xdir = this.random.nextInt(6);
                    zdir = this.random.nextInt(6);
                } else {
                    xdir = this.random.nextInt(5) + 6;
                    zdir = this.random.nextInt(5) + 6;
                }
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;

                BlockPos newTarget = BlockPos.containing(gox + xdir,
                        goy + this.random.nextInt(9 + this.ownerFlying * 2) - 4,
                        goz + zdir);
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --keepTrying;
            }
        }

        double speedFactor = 0.5;
        double vx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double vy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double vz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (this.isTame() && owner != null && this.distanceToSqr(owner) > 49.0) speedFactor = 3.5;
        }

        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(vx) * 0.5 - motion.x) * 0.15 * speedFactor;
        double my = motion.y + (Math.signum(vy) * 0.7 - motion.y) * 0.21 * speedFactor;
        double mz = motion.z + (Math.signum(vz) * 0.5 - motion.z) * 0.15 * speedFactor;
        this.setDeltaMovement(mx, my, mz);

        float yaw = (float)(Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(yaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 3.0f);
    }

    private void shootFireball(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dy = target.getY() + 0.25 - (this.getY() + 1.25);
        double dz = target.getZ() - this.getZ();
        SmallFireball fireball = new SmallFireball(this.level(), this, new Vec3(dx, dy, dz));
        fireball.setPos(this.getX(), this.getY() + 1.25, this.getZ());
        this.level().addFreshEntity(fireball);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntitySpyro) return false;
        return target instanceof Monster;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity target : targets) {
            if (this.isSuitableTarget(target)) return target;
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpyroActivity", this.getActivity());
        tag.putInt("SpyroFire", this.getSpyroFire());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.activity = tag.getInt("SpyroActivity");
        this.setActivity(this.activity);
        this.setSpyroFire(tag.getInt("SpyroFire"));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.COOKED_BEEF);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit()) return null;
        if (this.getActivity() != 2) return null;
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }
}
