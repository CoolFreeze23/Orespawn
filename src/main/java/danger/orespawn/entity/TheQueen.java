package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.ModSounds;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.util.MyUtils;

public class TheQueen extends Monster {

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MOOD =
            SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_POWER =
            SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH_VALUE = 6000;
    private static final double MOVE_SPEED_VALUE = 0.62;
    private static final double ATTACK_DAMAGE_VALUE = 200.0;
    private static final int DEFENSE_VALUE = 10;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("The Queen"), BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);

    private final Comparator<Entity> targetSorter;
    private BlockPos currentFlightTarget = null;
    private LivingEntity revengeTarget = null;
    private double attackDamage = ATTACK_DAMAGE_VALUE;
    private int hurtTimer = 0;
    private int homeX = 0;
    private int homeZ = 0;
    private int streamCount = 0;
    private int streamCountL = 0;
    private int ticker = 0;
    private int playerHitCount = 0;
    private int backoffTimer = 0;
    private int guardMode = 0;
    private volatile int headFound = 0;
    private int wingSound = 0;
    private int attackLevel = 1;
    private LivingEntity healthTrackedEntity = null;
    private float healthTrackedEntityHP = 0.0f;
    private int mood = 0;
    private int alwaysMad = 0;

    public TheQueen(EntityType<? extends TheQueen> type, Level level) {
        super(type, level);
        this.xpReward = 25000;
        this.noCulling = true;
        this.noPhysics = true;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH_VALUE)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED_VALUE)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_VALUE)
                .add(Attributes.FOLLOW_RANGE, 80.0)
                .add(Attributes.ARMOR, DEFENSE_VALUE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_MOOD, 0);
        builder.define(DATA_POWER, 1);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    public int getIsHappy() {
        return this.entityData.get(DATA_MOOD);
    }

    public int getPower() {
        return this.entityData.get(DATA_POWER);
    }

    public void setPower(int value) {
        this.entityData.set(DATA_POWER, value);
    }

    public boolean isHappy() {
        return getIsHappy() == 0;
    }

    public int mygetMaxHealth() {
        return MAX_HEALTH_VALUE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KING_LIVING.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.KING_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TREX_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.35f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Override
    public void thunderHit(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LightningBolt bolt) {
    }

    private void dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(20) - this.getRandom().nextInt(20);
        double oy = this.getY() + 12.0;
        double oz = this.getZ() + this.getRandom().nextInt(20) - this.getRandom().nextInt(20);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        dropItemRand(new ItemStack(ModItems.QUEEN_SCALE.get(), 1));
        dropItemRand(new ItemStack(ModItems.PRINCE_EGG.get(), 1));
        ThePrincess princess = ModEntities.THE_PRINCESS.get().create(this.level());
        if (princess != null) {
            princess.moveTo(this.getX(), this.getY() + 10, this.getZ(), 0.0F, 0.0F);
            this.level().addFreshEntity(princess);
        }
        for (int i = 0; i < 56; i++) {
            dropItemRand(new ItemStack(ModItems.QUEEN_SCALE.get(), 1));
            dropItemRand(new ItemStack(Items.EXPERIENCE_BOTTLE, 1));
            dropItemRand(new ItemStack(Items.GOLDEN_APPLE, 1));
            dropItemRand(new ItemStack(Items.NETHER_STAR, 1));
        }
    }

    @Override
    public void tick() {
        super.tick();

        this.wingSound++;
        if (this.wingSound > 30) {
            if (!this.level().isClientSide) {
                this.playSound(ModSounds.MOTHRAWINGS1.get(), 1.75f, 0.75f);
            }
            this.wingSound = 0;
        }

        this.noPhysics = true;
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.x, velocity.y * 0.6, velocity.z);

        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() * 3 / 4)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 20;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 2)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 100;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 3)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 500;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 4)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 1000;
        }

        if (this.level().isClientSide && this.getPower() > 800) {
            float particleOffset = 7.0f;
            if (this.getRandom().nextInt(4) == 1) {
                for (int i = 0; i < 10; i++) {
                    double px = this.getX() - (double) particleOffset * Math.sin(Math.toRadians(this.getYRot()));
                    double py = this.getY() + 14.0;
                    double pz = this.getZ() + (double) particleOffset * Math.cos(Math.toRadians(this.getYRot()));
                    Vec3 delta = this.getDeltaMovement();
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.FIREWORK,
                            px, py, pz,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 5.0 + delta.x * 3.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 5.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 5.0 + delta.z * 3.0
                    );
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target != null && target instanceof LivingEntity living && !this.level().isClientSide) {
            if (!living.isRemoved()) {
                if (this.healthTrackedEntity == living) {
                    if (this.healthTrackedEntityHP < living.getHealth()) {
                        living.setHealth(this.healthTrackedEntityHP);
                    }
                } else {
                    this.healthTrackedEntity = living;
                }
                if (living.getBbWidth() * living.getBbHeight() > 30.0f) {
                    living.setHealth(living.getHealth() * 3.0f / 4.0f);
                    living.hurt(this.damageSources().mobAttack(this), (float) this.attackDamage);
                }
                this.healthTrackedEntityHP = living.getHealth();
                if (this.healthTrackedEntityHP <= 0.0f) {
                    this.healthTrackedEntity.discard();
                }
            } else {
                this.healthTrackedEntity = null;
                this.healthTrackedEntityHP = 0.0f;
            }
        }

        if (target != null && target instanceof EnderDragon dragon) {
            DamageSource explosionSource = this.damageSources().explosion(null, null);
            dragon.hurt(explosionSource, (float) this.attackDamage);
        }

        boolean hit = target.hurt(this.damageSources().mobAttack(this), (float) this.attackDamage);
        if (hit) {
            double knockbackStrength = 2.75;
            double upwardKnockback = 0.2;
            float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            upwardKnockback += this.getRandom().nextFloat() * 0.25;
            if (target.isRemoved() || target instanceof Player) {
                upwardKnockback *= 1.5;
            }
            target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) {
            return false;
        }
        float clampedDamage = Math.min(amount, 750.0f);

        if (source.getMsgId().equals("inWall")) {
            return false;
        }

        this.mood = 1;

        if (source.is(DamageTypes.EXPLOSION)) {
            float healedHealth = this.getHealth();
            healedHealth += amount / 2.0f;
            if (healedHealth > this.getMaxHealth()) {
                healedHealth = this.getMaxHealth();
            }
            this.setHealth(healedHealth);
            return false;
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            if (attacker instanceof PurplePower) return false;
            float entitySize = living.getBbHeight() * living.getBbWidth();
            if (living instanceof Monster && entitySize < 3.0f) {
                living.discard();
                return false;
            }
        }

        if (!source.getMsgId().equals("cactus")) {
            this.hurtTimer = 20;
            boolean ret = super.hurt(source, clampedDamage);
            if (attacker instanceof Player) {
                this.playerHitCount++;
            }
            if (attacker instanceof LivingEntity living && this.currentFlightTarget != null
                    && !MyUtils.isRoyalty(attacker)) {
                this.revengeTarget = living;
                int flightY = Math.min((int) attacker.getY(), 230);
                this.currentFlightTarget = new BlockPos((int) attacker.getX(), flightY, (int) attacker.getZ());
            }
            return ret;
        }
        return false;
    }

    private boolean tooFarFromHome() {
        float deltaX = (float) (this.getX() - (double) this.homeX);
        float deltaZ = (float) (this.getZ() - (double) this.homeZ);
        float distFromHome = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return distFromHome > 120.0f;
    }

    private double getHorizontalDistanceSqToEntity(Entity entity) {
        double deltaZ = entity.getZ() - this.getZ();
        double deltaX = entity.getX() - this.getX();
        return deltaZ * deltaZ + deltaX * deltaX;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.isRemoved()) return;
        super.customServerAiStep();

        int randomXOffset = 1;
        int randomZOffset = 1;
        int attackChance = 5;
        LivingEntity currentTarget = null;
        LivingEntity nearbyTarget = null;
        double angleToTarget, headingAngle, angleDiff;

        if (this.healthTrackedEntity != null) {
            if (this.distanceToSqr(this.healthTrackedEntity) < 2000.0 && !this.healthTrackedEntity.isRemoved()) {
                if (this.healthTrackedEntityHP < this.healthTrackedEntity.getHealth()) {
                    this.healthTrackedEntity.setHealth(this.healthTrackedEntityHP);
                } else {
                    this.healthTrackedEntityHP = this.healthTrackedEntity.getHealth();
                }
                if (this.healthTrackedEntityHP <= 0.0f) {
                    this.healthTrackedEntity.discard();
                }
            } else {
                this.healthTrackedEntity = null;
                this.healthTrackedEntityHP = 0.0f;
            }
        }

        if (this.attackLevel > 1000) {
            if (this.mood == 1) {
                int j = 15;
                if (this.playerHitCount < 10) {
                    j = 45;
                }
                for (int i = 0; i < j; i++) {
                    PurplePower pwr = ModEntities.PURPLE_POWER.get().create(this.level());
                    if (pwr != null) {
                        double xzoff = 10.0;
                        double yoff = 14.0;
                        pwr.moveTo(
                            this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())) + this.getRandom().nextInt(10) - 5,
                            this.getY() + yoff + this.getRandom().nextInt(6) - 3,
                            this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())) + this.getRandom().nextInt(10) - 5,
                            0, 0);
                        Vec3 mot = this.getDeltaMovement();
                        pwr.setDeltaMovement(mot.x * 3.0, 0, mot.z * 3.0);
                        pwr.setPurpleType(10);
                        this.level().addFreshEntity(pwr);
                    }
                }
            } else {
                if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    for (int m = 0; m < 25; m++) {
                        int ix = this.getRandom().nextInt(25) - this.getRandom().nextInt(25);
                        int kx = this.getRandom().nextInt(25) - this.getRandom().nextInt(25);
                        for (int j = -20; j < 20; j++) {
                            BlockPos checkPos = new BlockPos((int) this.getX() + ix, (int) this.getY() + j, (int) this.getZ() + kx);
                            BlockState blockState = this.level().getBlockState(checkPos);
                            BlockPos abovePos = checkPos.above();
                            BlockState aboveState = this.level().getBlockState(abovePos);

                            if (blockState.is(Blocks.GRASS_BLOCK) && aboveState.isAir()) {
                                int which = this.getRandom().nextInt(8);
                                if (which == 0) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.RED_TULIP.defaultBlockState());
                                } else if (which == 1) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.DANDELION.defaultBlockState());
                                } else if (which == 2) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.BLUE_ORCHID.defaultBlockState());
                                } else if (which == 3) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.PINK_TULIP.defaultBlockState());
                                } else if (which == 4) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.POPPY.defaultBlockState());
                                } else if (which == 5) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.ALLIUM.defaultBlockState());
                                } else if (which == 6) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.CORNFLOWER.defaultBlockState());
                                } else if (which == 7) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.SUNFLOWER.defaultBlockState());
                                }
                                break;
                            } else if (blockState.is(Blocks.DIRT) && aboveState.isAir()) {
                                this.level().setBlockAndUpdate(checkPos, Blocks.GRASS_BLOCK.defaultBlockState());
                                break;
                            } else if (blockState.is(Blocks.STONE) && aboveState.isAir()) {
                                this.level().setBlockAndUpdate(abovePos, Blocks.DIRT.defaultBlockState());
                                break;
                            } else if (blockState.is(Blocks.GRAVEL) && aboveState.isAir()) {
                                if (this.getRandom().nextInt(2) == 0) {
                                    this.level().setBlockAndUpdate(abovePos, Blocks.DEAD_BUSH.defaultBlockState());
                                } else {
                                    this.level().setBlockAndUpdate(checkPos, Blocks.DIRT.defaultBlockState());
                                }
                                break;
                            } else if (blockState.is(Blocks.SAND) && aboveState.isAir()) {
                                this.level().setBlockAndUpdate(checkPos, Blocks.WATER.defaultBlockState());
                                break;
                            } else if (blockState.is(Blocks.COBBLESTONE) && aboveState.isAir()) {
                                this.level().setBlockAndUpdate(checkPos, Blocks.STONE.defaultBlockState());
                                break;
                            } else if (blockState.isAir() && j > 0) {
                                break;
                            }
                        }
                    }
                }
                for (int m = 0; m < 10; m++) {
                    EntityButterfly butterfly = ModEntities.ENTITY_BUTTERFLY.get().create(this.level());
                    if (butterfly != null) {
                        butterfly.moveTo(
                                this.getX() + this.getRandom().nextInt(20) - 10,
                                this.getY() + 5 + this.getRandom().nextInt(10),
                                this.getZ() + this.getRandom().nextInt(20) - 10,
                                this.getRandom().nextFloat() * 360.0F, 0.0F);
                        this.level().addFreshEntity(butterfly);
                    }
                }
            }
            this.attackLevel = 1;
        }

        if (this.attackLevel > 1) {
            this.attackLevel--;
        }
        if (this.hurtTimer > 0) {
            this.hurtTimer--;
        }

        if ((this.homeX == 0 && this.homeZ == 0) || this.guardMode == 0) {
            this.homeX = (int) this.getX();
            this.homeZ = (int) this.getZ();
        }

        if (this.getHealth() > (float) (this.mygetMaxHealth() - 2) && this.getRandom().nextInt(500) == 1) {
            this.mood = 0;
        }
        if (this.alwaysMad != 0) {
            this.mood = 1;
        }
        if (this.mood == 0) {
            this.attackLevel += 10;
        }

        this.ticker++;
        if (this.ticker > 30000) {
            this.ticker = 0;
        }
        if (this.ticker % 60 == 0) {
            this.streamCount = 10;
        }
        if (this.ticker % 70 == 0) {
            this.streamCountL = 6;
        }
        if (this.ticker % 10 == 0) {
            // Wire PLAY_NICELY config: when true, The Queen starts in peaceful disposition
            this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.PLAY_NICELY.get() ? 1 : 0);
            this.entityData.set(DATA_MOOD, this.mood);
            this.setPower(this.attackLevel);
        }

        if (this.backoffTimer > 0) {
            this.backoffTimer--;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
            attackChance = 3;
        }

        this.noPhysics = true;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
        }

        double ftDistSq = this.currentFlightTarget.distSqr(new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ()));

        if (this.tooFarFromHome() || this.getRandom().nextInt(200) == 0 || ftDistSq < 9.1) {
            randomZOffset = this.getRandom().nextInt(120);
            randomXOffset = this.getRandom().nextInt(120);
            if (this.getRandom().nextInt(2) == 0) randomZOffset = -randomZOffset;
            if (this.getRandom().nextInt(2) == 0) randomXOffset = -randomXOffset;

            int altOffset = computeAltitudeOffset(this.homeX, (int) this.getY(), this.homeZ);
            if ((int) (this.getY() + altOffset) > 230) {
                altOffset = 230 - (int) this.getY();
            }
            this.currentFlightTarget = new BlockPos(this.homeX + randomXOffset, (int) (this.getY() + altOffset), this.homeZ + randomZOffset);

            if (this.mood == 0) {
                List<TheKing> kingList = this.level().getEntitiesOfClass(TheKing.class, this.getBoundingBox().inflate(64, 32, 64));
                if (kingList != null && !kingList.isEmpty()) {
                    TheKing king = kingList.get(0);
                    int followX = (int) king.getX() + this.getRandom().nextInt(20) - 10;
                    int followZ = (int) king.getZ() + this.getRandom().nextInt(20) - 10;
                    int followY = Math.min((int) king.getY(), 230);
                    this.currentFlightTarget = new BlockPos(followX, followY, followZ);
                }
            }
        } else if (this.getRandom().nextInt(attackChance) == 0) {
            currentTarget = this.revengeTarget;
            if (this.isHappy()) {
                currentTarget = null;
            }

            if (currentTarget instanceof TheQueen || currentTarget instanceof QueenHead) {
                this.revengeTarget = null;
                currentTarget = null;
            }
            if (currentTarget != null) {
                float deltaX = (float) (currentTarget.getX() - (double) this.homeX);
                float deltaZ = (float) (currentTarget.getZ() - (double) this.homeZ);
                float distFromHome = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                if (!currentTarget.isAlive() || this.getRandom().nextInt(450) == 1 || (distFromHome > 128.0f && this.guardMode == 1)) {
                    currentTarget = null;
                    this.revengeTarget = null;
                }
                if (currentTarget != null && !myCanSee(currentTarget)) {
                    currentTarget = null;
                }
            }

            nearbyTarget = findSomethingToAttack();

            if (this.headFound == 0 && this.mood == 1) {
                QueenHead head = ModEntities.QUEEN_HEAD.get().create(this.level());
                if (head != null) {
                    head.moveTo(this.getX(), this.getY() + 20, this.getZ(), 0.0F, 0.0F);
                    this.level().addFreshEntity(head);
                    this.headFound = 1;
                }
            }

            if (currentTarget == null) {
                currentTarget = nearbyTarget;
            }

            if (currentTarget != null) {
                float targetSize = currentTarget.getBbWidth() * currentTarget.getBbHeight();
                if (this.attackLevel < 1000) {
                    this.attackLevel += 15;
                    if (this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
                        this.attackLevel += 15;
                    }
                    if (targetSize > 50.0f) this.attackLevel += 15;
                    if (targetSize > 100.0f) this.attackLevel += 15;
                    if (targetSize > 200.0f) this.attackLevel += 25;
                }

                this.setAttacking(1);

                if (this.backoffTimer == 0) {
                    int flightY = Math.min((int) (currentTarget.getY() + currentTarget.getBbHeight() / 2.0f + 1.0), 230);
                    this.currentFlightTarget = new BlockPos((int) currentTarget.getX(), flightY, (int) currentTarget.getZ());
                    if (this.getRandom().nextInt(50) == 1) {
                        this.backoffTimer = 90 + this.getRandom().nextInt(90);
                    }
                } else {
                    double bftDistSq = this.currentFlightTarget.distSqr(new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ()));
                    if (bftDistSq < 9.1) {
                        randomZOffset = this.getRandom().nextInt(20) + 30;
                        randomXOffset = this.getRandom().nextInt(20) + 30;
                        if (this.getRandom().nextInt(2) == 0) randomZOffset = -randomZOffset;
                        if (this.getRandom().nextInt(2) == 0) randomXOffset = -randomXOffset;

                        int altOffset = computeAltitudeOffset((int) currentTarget.getX(), (int) this.getY(), (int) currentTarget.getZ());
                        if ((int) (this.getY() + altOffset) > 230) {
                            altOffset = 230 - (int) this.getY();
                        }
                        this.currentFlightTarget = new BlockPos((int) currentTarget.getX() + randomXOffset, (int) (this.getY() + altOffset), (int) currentTarget.getZ() + randomZOffset);
                    }
                }

                if (this.distanceToSqr(currentTarget) < 900.0) {
                    if (this.getRandom().nextInt(2) == 1) {
                        doAreaDamage(this.getX(), this.getY(), this.getZ(), 15.0, ATTACK_DAMAGE_VALUE / 4.0, 0);
                    }
                    this.doHurtTarget(currentTarget);
                }

                double forwardX = this.getX() + 20.0 * Math.sin(Math.toRadians(this.getYRot()));
                double forwardZ = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.getYRot()));
                if (this.getRandom().nextInt(3) == 1) {
                    doAreaDamage(forwardX, this.getY() + 10.0, forwardZ, 15.0, ATTACK_DAMAGE_VALUE / 2.0, 1);
                }

                if (this.getHorizontalDistanceSqToEntity(currentTarget) > 900.0) {
                    int attackChoice = this.getRandom().nextInt(2);
                    if (attackChoice == 0) {
                        if (this.streamCount > 0) {
                            this.setAttacking(1);
                            angleToTarget = Math.atan2(currentTarget.getZ() - this.getZ(), currentTarget.getX() - this.getX());
                            headingAngle = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            angleDiff = Math.abs(angleToTarget - headingAngle) % (Math.PI * 2.0);
                            if (angleDiff > Math.PI) angleDiff -= Math.PI * 2.0;
                            angleDiff = Math.abs(angleDiff);
                            if (angleDiff < 0.5) {
                                fireFireballs(currentTarget);
                            }
                        }
                    } else {
                        if (this.streamCountL > 0) {
                            this.setAttacking(1);
                            angleToTarget = Math.atan2(currentTarget.getZ() - this.getZ(), currentTarget.getX() - this.getX());
                            headingAngle = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            angleDiff = Math.abs(angleToTarget - headingAngle) % (Math.PI * 2.0);
                            if (angleDiff > Math.PI) angleDiff -= Math.PI * 2.0;
                            angleDiff = Math.abs(angleDiff);
                            if (angleDiff < 0.5) {
                                fireLightning(currentTarget);
                            }
                        }
                    }
                }
            } else {
                this.setAttacking(0);
                this.streamCount = 10;
                this.streamCountL = 6;
            }
        }

        double goalX = (double) this.currentFlightTarget.getX() + 0.5 - this.getX();
        double goalY = (double) this.currentFlightTarget.getY() + 0.1 - this.getY();
        double goalZ = (double) this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double newMx = motion.x + (Math.signum(goalX) * 0.65 - motion.x) * 0.35;
        double newMy = motion.y + (Math.signum(goalY) * 0.69999 - motion.y) * 0.3;
        double newMz = motion.z + (Math.signum(goalZ) * 0.65 - motion.z) * 0.35;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + yawDelta / 8.0f);

        if (this.getRandom().nextInt(32) == 1 && this.getHealth() < (float) this.mygetMaxHealth()) {
            this.heal(5.0f);
            if (this.playerHitCount < 10) {
                this.heal(50.0f);
            }
        }
        if (this.playerHitCount < 10 && this.getHealth() < 2000.0f) {
            this.heal(2000.0f - this.getHealth());
        }
    }

    private int computeAltitudeOffset(int centerX, int currentY, int centerZ) {
        int dist = 0;
        for (int i = -5; i <= 5; i += 5) {
            for (int j = -5; j <= 5; j += 5) {
                BlockPos checkPos = new BlockPos(centerX + j, currentY, centerZ + i);
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.above(k));
                        dist++;
                        if (state.isAir()) break;
                    }
                } else {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.below(k));
                        dist--;
                        if (!state.isAir()) break;
                    }
                }
            }
        }
        return dist / 9 + 2;
    }

    private void fireFireballs(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.streamCount > 0) {
            BetterFireball bf = new BetterFireball(this.level(), this,
                    new Vec3(e.getX() - cx,
                             e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff),
                             e.getZ() - cz));
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            this.level().addFreshEntity(bf);

            this.playSound(SoundEvents.TNT_PRIMED, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            for (int i = 0; i < 6; i++) {
                float r1 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r2 = 3.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                float r3 = 5.0f * (this.getRandom().nextFloat() - this.getRandom().nextFloat());
                bf = new BetterFireball(this.level(), this,
                        new Vec3(e.getX() - cx + r1,
                                 e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2,
                                 e.getZ() - cz + r3));
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                bf.setBig();
                if (this.getRandom().nextInt(2) == 1) bf.setSmall();
                this.level().addFreshEntity(bf);
            }
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.streamCount--;
        }
    }

    private void fireLightning(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.streamCountL > 0) {
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            for (int i = 0; i < 3; i++) {
                ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
                lb.setOwner(this);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                double dx = e.getX() - lb.getX();
                double dy = e.getY() + 0.25 - lb.getY();
                double dz = e.getZ() - lb.getZ();
                float horizDist = Mth.sqrt((float)(dx * dx + dz * dz)) * 0.2f;
                lb.shoot(dx, dy + horizDist, dz, 1.4f, 4.0f);
                Vec3 lbMot = lb.getDeltaMovement();
                lb.setDeltaMovement(lbMot.x * 3.0, lbMot.y * 3.0, lbMot.z * 3.0);
                this.level().addFreshEntity(lb);
            }
            this.streamCountL--;
        }
    }

    public boolean myCanSee(LivingEntity e) {
        double xzoff = 10.0;
        int nblks = 20;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + 14.0);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 20.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 20.0);
        float dz = (float) ((e.getZ() - startz) / 20.0);

        if (Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) (nblks * Math.abs(dx));
            dx = Mth.clamp(dx, -1.0f, 1.0f);
        }
        if (Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) (nblks * Math.abs(dy));
            dy = Mth.clamp(dy, -1.0f, 1.0f);
        }
        if (Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) (nblks * Math.abs(dz));
            dz = Mth.clamp(dz, -1.0f, 1.0f);
        }

        for (int i = 0; i < nblks; i++) {
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = this.level().getBlockState(new BlockPos((int) startx, (int) starty, (int) startz));
            if (!state.isAir()) {
                return false;
            }
        }
        return true;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof QueenHead) { headFound = 1; return false; }
        if (MyUtils.isRoyalty(target)) return false;

        float deltaX = (float) (target.getX() - (double) this.homeX);
        float deltaZ = (float) (target.getZ() - (double) this.homeZ);
        float distFromHome = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (distFromHome > 144.0f) return false;

        if (MyUtils.isIgnoreable(target)) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;

        if (target instanceof Player player) {
            return !player.getAbilities().instabuild;
        }
        if (target instanceof Horse) return true;
        if (target instanceof Monster) return true;
        if (target instanceof EnderDragon) return true;
        return MyUtils.isAttackableNonMob(target);
    }

    private LivingEntity findSomethingToAttack() {
        if (this.isHappy()) {
            this.headFound = 1;
            return null;
        }
        AABB searchBox = this.getBoundingBox().inflate(80.0, 60.0, 80.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        this.headFound = 0;
        LivingEntity ret = null;
        for (LivingEntity entity : entities) {
            if (entity instanceof QueenHead) { this.headFound = 1; }
            if (isSuitableTarget(entity) && ret == null) {
                ret = entity;
            }
            if (ret != null && this.headFound != 0) break;
        }
        return ret;
    }

    private void doAreaDamage(double x, double y, double z, double dist, double damage, int knock) {
        AABB bb = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, bb);
        entities.sort(this.targetSorter);
        for (LivingEntity target : entities) {
            if (target == this || !target.isAlive()) continue;
            if (MyUtils.isRoyalty(target)) continue;
            if (target instanceof Ghost || target instanceof GhostSkelly) continue;

            DamageSource explosionSource = this.damageSources().explosion(null, null);
            target.hurt(explosionSource, (float) damage / 2.0f);
            target.hurt(this.damageSources().generic(), (float) damage / 2.0f);

            this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    net.minecraft.sounds.SoundSource.HOSTILE, 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);

            if (knock != 0) {
                double knockbackStrength = 2.75;
                double upwardKnockback = 0.65;
                float knockbackAngle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                target.push(Math.cos(knockbackAngle) * knockbackStrength, upwardKnockback, Math.sin(knockbackAngle) * knockbackStrength);
            }
        }
    }

    public void setGuardMode(int i) {
        this.guardMode = i;
    }

    public void setBadMood(int i) {
        this.alwaysMad = i;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KingHomeX", this.homeX);
        tag.putInt("KingHomeZ", this.homeZ);
        tag.putInt("GuardMode", this.guardMode);
        tag.putInt("PlayerHits", this.playerHitCount);
        tag.putInt("MeanMode", this.alwaysMad);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.homeX = tag.getInt("KingHomeX");
        this.homeZ = tag.getInt("KingHomeZ");
        this.guardMode = tag.getInt("GuardMode");
        this.playerHitCount = tag.getInt("PlayerHits");
        this.alwaysMad = tag.getInt("MeanMode");
    }
}
