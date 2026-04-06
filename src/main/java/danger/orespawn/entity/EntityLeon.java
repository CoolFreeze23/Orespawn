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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnMod;

public class EntityLeon extends TamableAnimal {

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityLeon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(EntityLeon.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BEING_RIDDEN =
            SynchedEntityData.defineId(EntityLeon.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.25f;
    private int hurtTimer = 0;
    private int wingSound = 0;
    private boolean targetInSight = false;
    private int ownerFlying = 0;
    private int flyaway = 0;
    private int stuckCount = 0;
    private int lastX = 0;
    private int lastZ = 0;
    private int unstickTimer = 0;
    @Nullable
    private BlockPos currentFlightTarget = null;

    public EntityLeon(EntityType<? extends EntityLeon> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.1, 16.0f, 2.0f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.75));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 9.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 55.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_BEING_RIDDEN, 0);
    }

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

    public int getBeingRidden() {
        return this.entityData.get(DATA_BEING_RIDDEN);
    }

    public void setBeingRidden(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_BEING_RIDDEN, value);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LeonAttacking", this.getAttacking());
        tag.putInt("LeonActivity", this.getActivity());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAttacking(tag.getInt("LeonAttacking"));
        this.setActivity(tag.getInt("LeonActivity"));
    }

    // ==================== Properties ====================

    public int mygetMaxHealth() {
        return 250;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.getPassengers().size() > 0) return false;
        return !this.isTame();
    }

    // ==================== Sounds ====================

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit()) return null;
        if (this.getActivity() == 1 && this.getPassengers().isEmpty()) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leon_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.75f;
    }

    @Override
    public float getVoicePitch() {
        return 0.85f;
    }

    // ==================== Combat ====================

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            living.hurt(this.damageSources().mobAttack(this), 55.0f);
            float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            double knockbackStrength = 1.25;
            double verticalKnockback = target.isRemoved() || target instanceof Player ? 0.3 : 0.15;
            target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.getMsgId().equals("inWall")) return false;
        if (!this.level().isClientSide) {
            this.setOrderedToSit(false);
            this.setActivity(1);
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof EntityLeon) return false;
        if (this.isTame() && attacker instanceof Player) return false;

        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 15;

        if (attacker instanceof LivingEntity living && !this.level().isClientSide) {
            this.setTarget(living);
            this.getNavigation().moveTo(living, 1.2);
        }
        return ret;
    }

    // ==================== Tick & AI ====================

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();

        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.getActivity() == 1) {
            ++this.wingSound;
            if (this.wingSound > 20) {
                if (!this.level().isClientSide) {
                    this.level().playSound(null, this.blockPosition(),
                            SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings")),
                            this.getSoundSource(), 0.5f, 1.0f);
                }
                this.wingSound = 0;
            }
        }

        if (this.isInWater()) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y + 0.07, motion.z);
        }

        if (this.level().isClientSide) return;

        alwaysDo();

        if (this.getActivity() == 0) return;

        if (!this.getPassengers().isEmpty()) {
            this.setBeingRidden(1);
        } else {
            this.setBeingRidden(0);
            flyWithoutRider();
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.level().isClientSide) return;
        super.customServerAiStep();

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }
    }

    private void alwaysDo() {
        if (this.level().isClientSide) return;

        if (!this.isOrderedToSit() && this.getActivity() == 0
                && this.getPassengers().isEmpty()
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(10) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.setActivity(1);
            }
        }

        if (this.random.nextInt(250) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }

        if (this.isOrderedToSit()) return;

        this.ownerFlying = 0;
        if (this.isTame() && this.getOwner() != null
                && this.getPassengers().isEmpty() && !this.isOrderedToSit()) {
            Player owner = (Player) this.getOwner();
            if (owner.getAbilities().flying) {
                this.ownerFlying = 1;
                this.setActivity(1);
            }
            if (this.distanceToSqr(owner) > 400.0) {
                this.setActivity(1);
            }
        }

        if (this.random.nextInt(50) == 1 && !this.isOrderedToSit()
                && !this.targetInSight && this.getPassengers().isEmpty()) {
            if (this.random.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
    }

    private void flyWithoutRider() {
        if (this.level().isClientSide) return;
        if (this.isOrderedToSit()) return;
        if (!this.getPassengers().isEmpty()) return;

        boolean doNew = false;

        if (this.currentFlightTarget == null) {
            doNew = true;
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.unstickTimer > 0) --this.unstickTimer;

        if (this.lastX == (int) this.getX() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
            if (this.stuckCount > 50) {
                this.stuckCount = 0;
                this.unstickTimer = 100;
                this.targetInSight = false;
                this.setAttacking(0);
                this.setActivity(1);
                doNew = true;
            }
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastZ = (int) this.getZ();
        }

        Vec3 motion = this.getDeltaMovement();
        double dampedY;
        if (this.getY() < this.currentFlightTarget.getY() + 2.0) {
            dampedY = motion.y * 0.7;
        } else if (this.getY() > this.currentFlightTarget.getY() - 2.0) {
            dampedY = motion.y * 0.5;
        } else {
            dampedY = motion.y * 0.61;
        }
        this.setDeltaMovement(motion.x, dampedY, motion.z);

        if (this.random.nextInt(300) == 1) doNew = true;

        boolean tooFarFromOwner = false;
        double ownerX = this.getX();
        double ownerY = this.getY();
        double ownerZ = this.getZ();
        boolean hasOwner = false;

        if (this.isTame() && this.getOwner() != null) {
            LivingEntity owner = this.getOwner();
            hasOwner = true;
            ownerX = owner.getX();
            ownerY = owner.getY();
            ownerZ = owner.getZ();
            if (this.distanceToSqr(owner) > 144.0) {
                tooFarFromOwner = true;
                this.targetInSight = false;
                this.setAttacking(0);
                this.flyaway = 0;
                doNew = true;
            }
        }

        if (this.flyaway > 0) --this.flyaway;

        if (!tooFarFromOwner && this.unstickTimer == 0 && this.flyaway == 0
                && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.random.nextInt(8) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / this.getMaxHealth() < 0.25f) {
                    this.setActivity(1);
                    this.setAttacking(0);
                    this.targetInSight = false;
                    doNew = false;
                    this.currentFlightTarget = new BlockPos(
                            (int) (this.getX() + (this.getX() - target.getX())),
                            (int) (this.getY() + 1),
                            (int) (this.getZ() + (this.getZ() - target.getZ())));
                } else {
                    this.setActivity(1);
                    this.setAttacking(1);
                    this.targetInSight = true;
                    this.currentFlightTarget = target.blockPosition().above();
                    doNew = false;
                    float attackRange = 7.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < attackRange * attackRange) {
                        this.doHurtTarget(target);
                    }
                }
            } else {
                this.targetInSight = false;
                this.flyaway = 0;
                this.setAttacking(0);
            }
        }

        double distSq = this.currentFlightTarget.distSqr(this.blockPosition());
        if (distSq < 17.0) doNew = true;

        if (doNew && (!this.targetInSight || this.flyaway != 0)) {
            int keepTrying = 50;
            while (keepTrying > 0) {
                int gox = (int) this.getX();
                int goy = (int) this.getY();
                int goz = (int) this.getZ();
                int xdir, zdir;

                if (hasOwner && this.unstickTimer == 0) {
                    gox = (int) ownerX;
                    goy = (int) ownerY;
                    goz = (int) ownerZ;
                    if (this.ownerFlying == 0) {
                        xdir = this.random.nextInt(12) + 6;
                        zdir = this.random.nextInt(12) + 6;
                    } else {
                        xdir = this.random.nextInt(8);
                        zdir = this.random.nextInt(8);
                    }
                } else {
                    xdir = this.random.nextInt(20) + 6;
                    zdir = this.random.nextInt(20) + 6;
                }

                if (this.random.nextInt(2) == 1) xdir = -xdir;
                if (this.random.nextInt(2) == 1) zdir = -zdir;

                BlockPos newTarget = new BlockPos(
                        gox + xdir,
                        goy + this.random.nextInt(9 + this.ownerFlying * 2) - 4,
                        goz + zdir);

                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    break;
                }
                --keepTrying;
            }
        }

        double speedFactor = 0.5;
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (this.isTame() && this.getOwner() != null
                    && this.distanceToSqr(this.getOwner()) > 49.0) {
                speedFactor = 3.5;
            }
        }

        double dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(dx) - motion.x) * 0.15 * speedFactor;
        double newMy = motion.y + (Math.signum(dy) - motion.y) * 0.21 * speedFactor;
        double newMz = motion.z + (Math.signum(dz) - motion.z) * 0.15 * speedFactor;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = (float) (0.75 * speedFactor);
        this.setYRot(this.getYRot() + yawDiff / 5.0f);
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0, 20.0, 20.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityLeon) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Player player) {
            if (player.getAbilities().invulnerable) return false;
            return !this.isTame();
        }
        if (!this.isTame()) return true;
        return false;
    }

    // ==================== Interaction ====================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.BEEF) && this.distanceToSqr(player) < 49.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 1) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.getMaxHealth() - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.getMaxHealth() > this.getHealth()) {
                    this.heal(this.getMaxHealth() - this.getHealth());
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        if (this.isTame() && this.isOwnedBy(player)) {
            if (stack.is(Blocks.GLASS.asItem()) && this.distanceToSqr(player) < 49.0) {
                if (!this.level().isClientSide) {
                    this.setTame(false, false);
                    this.setOwnerUUID(null);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.is(Items.NAME_TAG) && this.distanceToSqr(player) < 49.0) {
                this.setCustomName(stack.getHoverName());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (stack.isEmpty() && this.distanceToSqr(player) < 49.0) {
                if (!this.level().isClientSide) {
                    player.startRiding(this);
                    this.setActivity(1);
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (!stack.isEmpty() && this.distanceToSqr(player) < 49.0
                    && this.getPassengers().isEmpty()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.setInSittingPose(this.isOrderedToSit());
                if (this.isOrderedToSit()) {
                    this.setActivity(0);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = 4 + this.random.nextInt(6);
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(Items.DIAMOND);
        }
        count = 16 + this.random.nextInt(6);
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(Items.GOLD_INGOT);
        }
    }
}
