package danger.orespawn.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import danger.orespawn.ModItems;

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

    private final Comparator<Entity> targetSorter;
    private BlockPos currentFlightTarget = null;
    private LivingEntity revengeTarget = null;
    private double attdam = ATTACK_DAMAGE_VALUE;
    private int hurtTimer = 0;
    private int homex = 0;
    private int homez = 0;
    private int streamCount = 0;
    private int streamCountL = 0;
    private int ticker = 0;
    private int playerHitCount = 0;
    private int backoffTimer = 0;
    private int guardMode = 0;
    private volatile int headFound = 0;
    private int wingSound = 0;
    private int attackLevel = 1;
    private LivingEntity ev = null;
    private float evh = 0.0f;
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
        // TODO: Replace with custom "orespawn:king_living" sound
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // TODO: Replace with custom "orespawn:king_hit" sound
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        // TODO: Replace with custom "orespawn:trex_death" sound
        return SoundEvents.ENDER_DRAGON_DEATH;
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
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        dropItemRand(new ItemStack(ModItems.QUEEN_SCALE.get(), 1));
        // TODO: dropItemRand(new ItemStack(ModItems.PRINCE_EGG.get(), 1));
        // TODO: Spawn "The Princess" entity at getX(), getY()+10, getZ()
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
                // TODO: Play custom "orespawn:MothraWings" sound
                this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.75f, 0.75f);
            }
            this.wingSound = 0;
        }

        this.noPhysics = true;
        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(dm.x, dm.y * 0.6, dm.z);

        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() * 3 / 4)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 20;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 2)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 100;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 3)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 500;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 4)) {
            this.attdam = ATTACK_DAMAGE_VALUE * 1000;
        }

        if (this.level().isClientSide && this.getPower() > 800) {
            float f = 7.0f;
            if (this.getRandom().nextInt(4) == 1) {
                for (int i = 0; i < 10; i++) {
                    double px = this.getX() - (double) f * Math.sin(Math.toRadians(this.getYRot()));
                    double py = this.getY() + 14.0;
                    double pz = this.getZ() + (double) f * Math.cos(Math.toRadians(this.getYRot()));
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
                if (this.ev == living) {
                    if (this.evh < living.getHealth()) {
                        living.setHealth(this.evh);
                    }
                } else {
                    this.ev = living;
                }
                if (living.getBbWidth() * living.getBbHeight() > 30.0f) {
                    living.setHealth(living.getHealth() * 3.0f / 4.0f);
                    living.hurt(this.damageSources().mobAttack(this), (float) this.attdam);
                }
                this.evh = living.getHealth();
                if (this.evh <= 0.0f) {
                    this.ev.discard();
                }
            } else {
                this.ev = null;
                this.evh = 0.0f;
            }
        }

        if (target != null && target instanceof EnderDragon dragon) {
            DamageSource explosionSource = this.damageSources().explosion(null, null);
            dragon.hurt(explosionSource, (float) this.attdam);
        }

        boolean var4 = target.hurt(this.damageSources().mobAttack(this), (float) this.attdam);
        if (var4) {
            double ks = 2.75;
            double inair = 0.2;
            float f3 = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            inair += this.getRandom().nextFloat() * 0.25;
            if (target.isRemoved() || target instanceof Player) {
                inair *= 1.5;
            }
            target.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return var4;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) {
            return false;
        }
        float dm = Math.min(amount, 750.0f);

        if (source.getMsgId().equals("inWall")) {
            return false;
        }

        this.mood = 1;

        if (source.is(DamageTypes.EXPLOSION)) {
            float s = this.getHealth();
            s += amount / 2.0f;
            if (s > this.getMaxHealth()) {
                s = this.getMaxHealth();
            }
            this.setHealth(s);
            return false;
        }

        Entity e = source.getEntity();
        if (e instanceof LivingEntity living) {
            // TODO: if (e instanceof PurplePower) return false;
            float s = living.getBbHeight() * living.getBbWidth();
            if (living instanceof Monster && s < 3.0f) {
                living.discard();
                return false;
            }
        }

        if (!source.getMsgId().equals("cactus")) {
            this.hurtTimer = 20;
            boolean ret = super.hurt(source, dm);
            if (e instanceof Player) {
                this.playerHitCount++;
            }
            if (e instanceof LivingEntity living && this.currentFlightTarget != null) {
                // TODO: && !MyUtils.isRoyalty(e)
                this.revengeTarget = living;
                int dist = Math.min((int) e.getY(), 230);
                this.currentFlightTarget = new BlockPos((int) e.getX(), dist, (int) e.getZ());
            }
            return ret;
        }
        return false;
    }

    private boolean tooFarFromHome() {
        float d1 = (float) (this.getX() - (double) this.homex);
        float d2 = (float) (this.getZ() - (double) this.homez);
        float dist = (float) Math.sqrt(d1 * d1 + d2 * d2);
        return dist > 120.0f;
    }

    private double getHorizontalDistanceSqToEntity(Entity e) {
        double d1 = e.getZ() - this.getZ();
        double d2 = e.getX() - this.getX();
        return d1 * d1 + d2 * d2;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        int xdir = 1;
        int zdir = 1;
        int attrand = 5;
        LivingEntity e = null;
        LivingEntity f = null;
        double rr, rhdir, rdd;
        double pi = Math.PI;

        double xzoff = 8.0;
        double yoff = 14.0;

        if (this.ev != null) {
            if (this.distanceToSqr(this.ev) < 2000.0 && !this.ev.isRemoved()) {
                if (this.evh < this.ev.getHealth()) {
                    this.ev.setHealth(this.evh);
                } else {
                    this.evh = this.ev.getHealth();
                }
                if (this.evh <= 0.0f) {
                    this.ev.discard();
                }
            } else {
                this.ev = null;
                this.evh = 0.0f;
            }
        }

        if (this.attackLevel > 1000) {
            if (this.mood == 1) {
                int j = 15;
                if (this.playerHitCount < 10) {
                    j = 45;
                }
                for (int i = 0; i < j; i++) {
                    // TODO: Spawn PurplePower projectile at queen's front
                    // Entity ppwr = spawnCreature("PurplePower", ...)
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
                    // TODO: Spawn Butterfly or Bird passive mobs
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

        if ((this.homex == 0 && this.homez == 0) || this.guardMode == 0) {
            this.homex = (int) this.getX();
            this.homez = (int) this.getZ();
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
            this.entityData.set(DATA_PLAY_NICELY, 0);
            this.entityData.set(DATA_MOOD, this.mood);
            this.setPower(this.attackLevel);
        }

        if (this.backoffTimer > 0) {
            this.backoffTimer--;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
            attrand = 3;
        }

        this.noPhysics = true;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
        }

        double ftDistSq = this.currentFlightTarget.distSqr(new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ()));

        if (this.tooFarFromHome() || this.getRandom().nextInt(200) == 0 || ftDistSq < 9.1) {
            zdir = this.getRandom().nextInt(120);
            xdir = this.getRandom().nextInt(120);
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;

            int dist = computeAltitudeOffset(this.homex, (int) this.getY(), this.homez);
            if ((int) (this.getY() + dist) > 230) {
                dist = 230 - (int) this.getY();
            }
            this.currentFlightTarget = new BlockPos(this.homex + xdir, (int) (this.getY() + dist), this.homez + zdir);

            if (this.mood == 0) {
                // TODO: Search for TheKing entities nearby and follow them
                // List<TheKing> kinglist = level().getEntitiesOfClass(TheKing.class, getBoundingBox().inflate(64, 32, 64));
                // if (kinglist != null && !kinglist.isEmpty()) { ... fly near king ... }
            }
        } else if (this.getRandom().nextInt(attrand) == 0) {
            e = this.revengeTarget;
            if (this.isHappy()) {
                e = null;
            }

            // TODO: if (e instanceof TheQueen || e instanceof QueenHead) { revengeTarget = null; e = null; }
            if (e != null) {
                float d1 = (float) (e.getX() - (double) this.homex);
                float d2 = (float) (e.getZ() - (double) this.homez);
                d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
                if (!e.isAlive() || this.getRandom().nextInt(450) == 1 || (d1 > 128.0f && this.guardMode == 1)) {
                    e = null;
                    this.revengeTarget = null;
                }
                if (e != null && !myCanSee(e)) {
                    e = null;
                }
            }

            f = findSomethingToAttack();

            if (this.headFound == 0 && this.mood == 1) {
                // TODO: Spawn QueenHead entity at getX(), getY()+20, getZ()
            }

            if (e == null) {
                e = f;
            }

            if (e != null) {
                float eSize = e.getBbWidth() * e.getBbHeight();
                if (this.attackLevel < 1000) {
                    this.attackLevel += 15;
                    if (this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
                        this.attackLevel += 15;
                    }
                    if (eSize > 50.0f) this.attackLevel += 15;
                    if (eSize > 100.0f) this.attackLevel += 15;
                    if (eSize > 200.0f) this.attackLevel += 25;
                }

                this.setAttacking(1);

                if (this.backoffTimer == 0) {
                    int dist = Math.min((int) (e.getY() + e.getBbHeight() / 2.0f + 1.0), 230);
                    this.currentFlightTarget = new BlockPos((int) e.getX(), dist, (int) e.getZ());
                    if (this.getRandom().nextInt(50) == 1) {
                        this.backoffTimer = 90 + this.getRandom().nextInt(90);
                    }
                } else {
                    double bftDistSq = this.currentFlightTarget.distSqr(new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ()));
                    if (bftDistSq < 9.1) {
                        zdir = this.getRandom().nextInt(20) + 30;
                        xdir = this.getRandom().nextInt(20) + 30;
                        if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
                        if (this.getRandom().nextInt(2) == 0) xdir = -xdir;

                        int dist = computeAltitudeOffset((int) e.getX(), (int) this.getY(), (int) e.getZ());
                        if ((int) (this.getY() + dist) > 230) {
                            dist = 230 - (int) this.getY();
                        }
                        this.currentFlightTarget = new BlockPos((int) e.getX() + xdir, (int) (this.getY() + dist), (int) e.getZ() + zdir);
                    }
                }

                if (this.distanceToSqr(e) < 900.0) {
                    if (this.getRandom().nextInt(2) == 1) {
                        doAreaDamage(this.getX(), this.getY(), this.getZ(), 15.0, ATTACK_DAMAGE_VALUE / 4.0, 0);
                    }
                    this.doHurtTarget(e);
                }

                double dx = this.getX() + 20.0 * Math.sin(Math.toRadians(this.getYRot()));
                double dz = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.getYRot()));
                if (this.getRandom().nextInt(3) == 1) {
                    doAreaDamage(dx, this.getY() + 10.0, dz, 15.0, ATTACK_DAMAGE_VALUE / 2.0, 1);
                }

                if (this.getHorizontalDistanceSqToEntity(e) > 900.0) {
                    int which = this.getRandom().nextInt(2);
                    if (which == 0) {
                        if (this.streamCount > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) rdd -= pi * 2.0;
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                fireFireballs(e);
                            }
                        }
                    } else {
                        if (this.streamCountL > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) rdd -= pi * 2.0;
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                fireLightning(e);
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

        double var1 = (double) this.currentFlightTarget.getX() + 0.5 - this.getX();
        double var3 = (double) this.currentFlightTarget.getY() + 0.1 - this.getY();
        double var5 = (double) this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(var1) * 0.65 - motion.x) * 0.35;
        double my = motion.y + (Math.signum(var3) * 0.69999 - motion.y) * 0.3;
        double mz = motion.z + (Math.signum(var5) * 0.65 - motion.z) * 0.35;
        this.setDeltaMovement(mx, my, mz);

        float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float var8 = Mth.wrapDegrees(var7 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var8 / 8.0f);

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
            // TODO: Fire BetterFireball (really big) at target
            // BetterFireball bf = new BetterFireball(level(), this, e.getX()-cx, e.getY()+e.getBbHeight()/2-(getY()+yoff), e.getZ()-cz);
            // bf.setReallyBig();
            // level().addFreshEntity(bf);

            this.playSound(SoundEvents.TNT_PRIMED, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            for (int i = 0; i < 6; i++) {
                // TODO: Fire BetterFireball (big/small) scatter at target
                // float r1 = 5.0f * (getRandom().nextFloat() - getRandom().nextFloat());
                // float r2 = 3.0f * (getRandom().nextFloat() - getRandom().nextFloat());
                // float r3 = 5.0f * (getRandom().nextFloat() - getRandom().nextFloat());
                // BetterFireball bf = new BetterFireball(level(), this, ...);
                // level().addFreshEntity(bf);
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
                // TODO: Fire ThunderBolt projectile at target
                // ThunderBolt lb = new ThunderBolt(level(), cx, getY()+yoff, cz);
                // lb.shootFromRotation(this, ...);
                // level().addFreshEntity(lb);
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
        // TODO: if (target instanceof QueenHead) { headFound = 1; return false; }
        // TODO: if (MyUtils.isRoyalty(target)) return false;

        float d1 = (float) (target.getX() - (double) this.homex);
        float d2 = (float) (target.getZ() - (double) this.homez);
        float dist = (float) Math.sqrt(d1 * d1 + d2 * d2);
        if (dist > 144.0f) return false;

        // TODO: if (MyUtils.isIgnoreable(target)) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;

        if (target instanceof Player player) {
            return !player.getAbilities().instabuild;
        }
        if (target instanceof Horse) return true;
        if (target instanceof Monster) return true;
        if (target instanceof EnderDragon) return true;
        // TODO: return MyUtils.isAttackableNonMob(target);
        return false;
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
            // TODO: if (MyUtils.isRoyalty(target)) continue;
            // TODO: if (target instanceof Ghost || target instanceof GhostSkelly) continue;

            DamageSource explosionSource = this.damageSources().explosion(null, null);
            target.hurt(explosionSource, (float) damage / 2.0f);
            target.hurt(this.damageSources().generic(), (float) damage / 2.0f);

            this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    net.minecraft.sounds.SoundSource.HOSTILE, 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);

            if (knock != 0) {
                double ks = 2.75;
                double inair = 0.65;
                float f3 = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                target.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
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
        tag.putInt("KingHomeX", this.homex);
        tag.putInt("KingHomeZ", this.homez);
        tag.putInt("GuardMode", this.guardMode);
        tag.putInt("PlayerHits", this.playerHitCount);
        tag.putInt("MeanMode", this.alwaysMad);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.homex = tag.getInt("KingHomeX");
        this.homez = tag.getInt("KingHomeZ");
        this.guardMode = tag.getInt("GuardMode");
        this.playerHitCount = tag.getInt("PlayerHits");
        this.alwaysMad = tag.getInt("MeanMode");
    }
}
