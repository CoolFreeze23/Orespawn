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
import danger.orespawn.OreSpawnMod;

public class EntityStinky extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_SPYRO_FIRE =
            SynchedEntityData.defineId(EntityStinky.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(EntityStinky.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SKIN =
            SynchedEntityData.defineId(EntityStinky.class, EntityDataSerializers.INT);

    private static final double OWNER_FAR_DIST_SQ = 256.0;
    private static final float LOW_HEALTH_FRACTION = 0.25f;
    private static final int SKIN_VARIANT_COUNT = 19;

    private BlockPos currentFlightTarget = null;
    public int activity = 1;
    private int ownerFlying = 0;
    private final float moveSpeed = 0.3f;
    private int skinColor = -1;

    public EntityStinky(EntityType<? extends EntityStinky> type, Level level) {
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
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SPYRO_FIRE, 1);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_SKIN, 0);
    }

    public int getActivity() {
        this.activity = this.entityData.get(DATA_ACTIVITY);
        return this.activity;
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

    public int getSkin() {
        this.skinColor = this.entityData.get(DATA_SKIN);
        return this.skinColor;
    }

    public void setSkin(int val) {
        this.skinColor = val;
        this.entityData.set(DATA_SKIN, val);
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

        if (!this.level().isClientSide && this.random.nextInt(1750) == 1) {
            dropItemFront(Items.BONE.getDefaultInstance());
        }

        if (!this.level().isClientSide && this.random.nextInt(2000) == 2) {
            dropRandomItemRear();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        if (this.skinColor < 0) {
            this.skinColor = this.random.nextInt(SKIN_VARIANT_COUNT);
            this.setSkin(this.skinColor);
        }

        if (this.activity == 2) {
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
        }

        if (!this.level().isClientSide && this.random.nextInt(2000) == 1) {
            this.setSkin(this.random.nextInt(SKIN_VARIANT_COUNT));
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        this.setOrderedToSit(false);
        this.setActivity(2);
        return ret;
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
                        this.heal(100.0f - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                this.heal(100.0f - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && player.distanceToSqr(this) < 16.0) {
            if (!this.isOrderedToSit()) {
                this.setOrderedToSit(true);
                this.setActivity(1);
            } else {
                this.setOrderedToSit(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.activity != 2) {
            super.customServerAiStep();
        }

        if (this.random.nextInt(100) == 1 && this.getHealth() < 100.0f) {
            this.heal(1.0f);
        }

        if (!this.isOrderedToSit()) {
            if (this.activity == 0) setActivity(1);
            if (this.random.nextInt(100) == 1) {
                setActivity(this.random.nextInt(20) == 1 ? 2 : 1);
            }

            this.ownerFlying = 0;
            if (this.isTame() && this.getOwner() instanceof Player ownerPlayer) {
                if (ownerPlayer.getAbilities().flying) {
                    this.ownerFlying = 1;
                    setActivity(2);
                }
            }

            if (this.activity == 1 && this.isTame() && this.getOwner() != null &&
                    this.distanceToSqr(this.getOwner()) > OWNER_FAR_DIST_SQ) {
                setActivity(2);
            }

            doMovement();
        }

        if (this.activity == 1 && this.random.nextInt(50) == 0) {
            eatFlowers();
        }
    }

    private void eatFlowers() {
        for (int i = 1; i < 9; i++) {
            int j = Math.min(i, 2);
            for (int di = -j; di <= j; di++) {
                for (int dj = -j; dj <= j; dj++) {
                    BlockPos pos = BlockPos.containing(this.getX() + i, this.getY() + 1 + di, this.getZ() + dj);
                    if (this.level().getBlockState(pos).is(Blocks.DANDELION) ||
                            this.level().getBlockState(pos).is(Blocks.POPPY)) {
                        this.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.25);
                        if (this.blockPosition().distSqr(pos) < 12) {
                            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                            this.heal(1.0f);
                        }
                        return;
                    }
                }
            }
        }
    }

    private void doMovement() {
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        if (this.activity != 2) return;

        boolean doNew = false;
        boolean hasOwner = false;
        double ox = 0, oy = 0, oz = 0;
        LivingEntity owner = null;

        if (this.random.nextInt(300) == 0) doNew = true;

        if (this.isTame() && this.getOwner() != null) {
            owner = this.getOwner();
            hasOwner = true;
            ox = owner.getX(); oy = owner.getY(); oz = owner.getZ();
            if (this.distanceToSqr(owner) > 100.0) doNew = true;
            if (this.ownerFlying != 0 && this.distanceToSqr(owner) > 36.0) doNew = true;
        }

        if (this.random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                if (this.isTame() && this.getHealth() / 100.0f < LOW_HEALTH_FRACTION) {
                    setActivity(2);
                    doNew = false;
                    this.currentFlightTarget = BlockPos.containing(
                            this.getX() + (this.getX() - target.getX()),
                            this.getY() + 1, this.getZ() + (this.getZ() - target.getZ()));
                } else {
                    setActivity(2);
                    this.currentFlightTarget = BlockPos.containing(target.getX(), target.getY() + 1, target.getZ());
                    doNew = false;
                    float reach = 3.0f + target.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(target) < (double)(reach * reach)) {
                        this.doHurtTarget(target);
                    }
                }
            }
        }

        if (this.currentFlightTarget.closerToCenterThan(this.position(), 2.1)) {
            doNew = true;
        }

        if (doNew) {
            int keepTrying = 50;
            boolean found = false;
            while (!found && keepTrying > 0) {
                int gox = (int)(hasOwner ? ox : this.getX());
                int goy = (int)(hasOwner ? oy : this.getY());
                int goz = (int)(hasOwner ? oz : this.getZ());
                int xdir = this.random.nextInt(5) + 6;
                int zdir = this.random.nextInt(5) + 6;
                if (this.random.nextInt(2) == 0) zdir = -zdir;
                if (this.random.nextInt(2) == 0) xdir = -xdir;

                BlockPos newTarget = BlockPos.containing(gox + xdir,
                        goy + this.random.nextInt(6 + this.ownerFlying * 2) - 2,
                        goz + zdir);
                if (this.level().getBlockState(newTarget).isAir()) {
                    this.currentFlightTarget = newTarget;
                    found = true;
                }
                --keepTrying;
            }
        }

        double speedFactor = 1.0;
        double vx = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double vy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double vz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.ownerFlying != 0) {
            speedFactor = 1.75;
            if (owner != null && this.distanceToSqr(owner) > 49.0) speedFactor = 3.5;
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

    private void dropItemFront(ItemStack stack) {
        float dropOffset = 0.75f + Math.abs(this.random.nextFloat() * 0.75f);
        double dx = this.getX() - (double) dropOffset * Math.sin(Math.toRadians(this.yBodyRot));
        double dz = this.getZ() + (double) dropOffset * Math.cos(Math.toRadians(this.yBodyRot));
        ItemEntity item = new ItemEntity(this.level(), dx, this.getY() + 0.9, dz, stack);
        this.level().addFreshEntity(item);
    }

    private void dropItemRear(ItemStack stack) {
        float dropOffset = 0.55f + Math.abs(this.random.nextFloat() * 0.55f);
        double dx = this.getX() + (double) dropOffset * Math.sin(Math.toRadians(this.yBodyRot));
        double dz = this.getZ() - (double) dropOffset * Math.cos(Math.toRadians(this.yBodyRot));
        ItemEntity item = new ItemEntity(this.level(), dx, this.getY() + 0.25, dz, stack);
        this.level().addFreshEntity(item);
    }

    private void dropRandomItemRear() {
        ItemStack drop = switch (this.skinColor) {
            case 0 -> new ItemStack(Items.DIAMOND);
            case 1 -> new ItemStack(Items.CHICKEN);
            case 2 -> new ItemStack(Items.IRON_INGOT);
            case 3 -> new ItemStack(Items.GOLD_NUGGET);
            case 4 -> new ItemStack(Items.COOKIE);
            case 5 -> new ItemStack(Items.CAKE);
            case 6 -> new ItemStack(Items.FLOWER_POT);
            case 7 -> new ItemStack(Items.POISONOUS_POTATO);
            case 8 -> new ItemStack(Items.GOLD_INGOT);
            case 9 -> new ItemStack(Items.SAND.asItem());
            case 10 -> new ItemStack(Items.COPPER_INGOT);
            case 11 -> new ItemStack(Items.APPLE);
            case 12 -> new ItemStack(Items.EMERALD);
            case 13 -> new ItemStack(Items.GRAVEL.asItem());
            case 14 -> new ItemStack(Items.COBBLESTONE.asItem());
            case 15 -> new ItemStack(Items.NAME_TAG);
            case 16 -> new ItemStack(Items.IRON_PICKAXE);
            case 17 -> new ItemStack(Items.SWEET_BERRIES);
            case 18 -> new ItemStack(Items.MELON_SLICE);
            default -> new ItemStack(Items.BONE);
        };
        dropItemRear(drop);
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
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
        tag.putInt("StinkySkin", this.getSkin());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.activity = tag.getInt("SpyroActivity");
        this.setActivity(this.activity);
        this.setSpyroFire(tag.getInt("SpyroFire"));
        this.skinColor = tag.getInt("StinkySkin");
        this.setSkin(this.skinColor);
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
        return null;
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
        return 0.6f;
    }
}
