package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.ModSounds;
import danger.orespawn.util.MyUtils;

public class TheKing extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_IS_END =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH_VALUE = 6000;
    private static final double ATTACK_DAMAGE_VALUE = 250.0;
    private static final int DEFENSE_VALUE = 12;
    private static final double MOVE_SPEED_VALUE = 0.62;

    private BlockPos currentFlightTarget = null;
    private final Comparator<Entity> targetSorter;
    private LivingEntity revengeTarget = null;
    private double attackDamage = ATTACK_DAMAGE_VALUE;
    private int hurtCooldown = 0;
    private int homeX = 0;
    private int homeZ = 0;
    private int fireballStreamCount = 0;
    private int lightningStreamCount = 0;
    private int iceStreamCount = 0;
    private int ticker = 0;
    private int playerHitCount = 0;
    private int backoffTimer = 0;
    private int guardModeTimer = 0;
    private volatile int headEntityFound = 0;
    private int wingSoundTimer = 0;
    private int largeEntityDetected = 0;
    private int isEnd = 0;
    private int endCounter = 0;

    public TheKing(EntityType<? extends TheKing> type, Level level) {
        super(type, level);
        // Entity dimensions (22x24) should be set in EntityType.Builder.sized(22.0f, 24.0f)
        this.xpReward = 25000;
        this.noCulling = true;
        this.noPhysics = true;
        this.setNoGravity(true);
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
                .add(Attributes.FOLLOW_RANGE, 128.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, DEFENSE_VALUE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_IS_END, 0);
    }

    public int mygetMaxHealth() {
        return MAX_HEALTH_VALUE;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    public int getIsEnd() {
        return this.entityData.get(DATA_IS_END);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distSq) {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
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
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
    }

    // ---- Tick / AI ----

    @Override
    public void tick() {
        super.tick();
        this.wingSoundTimer++;
        if (this.wingSoundTimer > 30) {
            if (!this.level().isClientSide) {
                this.playSound(ModSounds.MOTHRAWINGS1.get(), 1.75f, 0.75f);
            }
            this.wingSoundTimer = 0;
        }

        this.noPhysics = true;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() * 2 / 3)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 2;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 2)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 4;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 4)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 8;
        }
        if (this.playerHitCount < 10 && this.getHealth() < (float)(this.mygetMaxHealth() / 8)) {
            this.attackDamage = ATTACK_DAMAGE_VALUE * 16;
        }

        if (this.level().isClientSide) {
            this.isEnd = this.entityData.get(DATA_IS_END);
            if (this.isEnd != 0 && this.getRandom().nextInt(3) == 1) {
                float particleOffset = 7.0f;
                Vec3 mot = this.getDeltaMovement();
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            this.getX() - (double) particleOffset * Math.sin(Math.toRadians(this.getYRot())),
                            this.getY() + 14.0,
                            this.getZ() + (double) particleOffset * Math.cos(Math.toRadians(this.getYRot())),
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0 + mot.x * 6.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0,
                            (this.getRandom().nextGaussian() - this.getRandom().nextGaussian()) / 4.0 + mot.z * 6.0);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        int randomXOffset;
        int randomZOffset;
        int attackChance = 5;
        int attackChoice;
        LivingEntity currentTarget;
        LivingEntity nearbyTarget;

        if (this.isRemoved()) return;
        super.customServerAiStep();

        this.entityData.set(DATA_IS_END, this.isEnd);
        this.entityData.set(DATA_PLAY_NICELY, 0);

        // ---- End-game phase 1: dialogue ----
        if (this.isEnd == 1) {
            this.endCounter++;
            this.noPhysics = true;
            this.setDeltaMovement(Vec3.ZERO);
            this.hurtCooldown = 10;
            if (this.isRemoved()) return;

            Player nearestPlayer = this.findNearestPlayer();
            if (nearestPlayer != null) {
                this.lookAt(nearestPlayer, 10.0f, 10.0f);
                nearestPlayer.setDeltaMovement(Vec3.ZERO);
                double deltaX = this.getX() - nearestPlayer.getX();
                double deltaZ = this.getZ() - nearestPlayer.getZ();
                float facingAngle = (float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
                nearestPlayer.setYRot(facingAngle);
                nearestPlayer.setHealth(1.0f);
            }

            if (this.endCounter == 10) {
                this.msgToPlayers("The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
                return;
            }
            if (this.endCounter == 80) {
                this.msgToPlayers("The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
                return;
            }
            if (this.endCounter == 160) {
                this.msgToPlayers("The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
                return;
            }
            if (this.endCounter == 240) {
                this.msgToPlayers("The King: I used you to learn your ways, and I have reached my conclusion on your species.");
                return;
            }
            if (this.endCounter == 300) {
                this.msgToPlayers("The King: You have 10 seconds to run...");
                return;
            }
            if (this.endCounter == 320) { this.msgToPlayers("9."); return; }
            if (this.endCounter == 340) { this.msgToPlayers("8."); return; }
            if (this.endCounter == 360) { this.msgToPlayers("7."); return; }
            if (this.endCounter == 380) { this.msgToPlayers("6."); return; }
            if (this.endCounter == 400) { this.msgToPlayers("5."); return; }
            if (this.endCounter == 420) { this.msgToPlayers("4."); return; }
            if (this.endCounter == 440) { this.msgToPlayers("3."); return; }
            if (this.endCounter == 460) { this.msgToPlayers("2."); return; }
            if (this.endCounter == 480) { this.msgToPlayers("1."); return; }
            if (this.endCounter == 500) {
                this.msgToPlayers("The King: Prepare to die!");
                this.isEnd = 2;
                return;
            }
            return;
        }

        // ---- End-game phase 2: enraged ----
        if (this.isEnd == 2) {
            this.hurtCooldown = 10;
            this.playerHitCount = 0;
            this.fireballStreamCount = 10;
            this.lightningStreamCount = 10;
            this.iceStreamCount = 10;
            attackChance = 3;
            this.guardModeTimer = 0;
            this.largeEntityDetected = 1;
            if (this.backoffTimer > 0) this.backoffTimer--;
        }

        if (this.hurtCooldown > 0) this.hurtCooldown--;

        if ((this.homeX == 0 && this.homeZ == 0) || this.guardModeTimer == 0) {
            this.homeX = (int) this.getX();
            this.homeZ = (int) this.getZ();
        }

        this.ticker++;
        if (this.ticker > 30000) this.ticker = 0;
        if (this.ticker % 80 == 0) this.fireballStreamCount = 10;
        if (this.ticker % 90 == 0) this.lightningStreamCount = 5;
        if (this.ticker % 70 == 0) this.iceStreamCount = 8;
        if (this.backoffTimer > 0) this.backoffTimer--;

        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2)) {
            attackChance = 3;
        }

        this.noPhysics = true;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        }

        if (this.tooFarFromHome() || this.getRandom().nextInt(200) == 0 || this.flightTargetDistSqr() < 9.1) {
            randomZOffset = this.getRandom().nextInt(120);
            randomXOffset = this.getRandom().nextInt(120);
            if (this.getRandom().nextInt(2) == 0) randomZOffset = -randomZOffset;
            if (this.getRandom().nextInt(2) == 0) randomXOffset = -randomXOffset;

            int altAdj = computeAltitudeAdjustment(this.homeX, this.homeZ);
            int flightY = Math.min((int) (this.getY() + altAdj), 230);
            this.currentFlightTarget = new BlockPos(this.homeX + randomXOffset, flightY, this.homeZ + randomZOffset);

        } else if (this.getRandom().nextInt(attackChance) == 0) {
            // ---- Target acquisition ----
            currentTarget = this.revengeTarget;

            if (currentTarget instanceof TheKing || currentTarget instanceof KingHead) {
                this.revengeTarget = null;
                currentTarget = null;
            }
            if (currentTarget != null) {
                float deltaX = (float) (currentTarget.getX() - this.homeX);
                float deltaZ = (float) (currentTarget.getZ() - this.homeZ);
                float distFromHome = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                if (!currentTarget.isAlive() || this.getRandom().nextInt(250) == 1
                        || (distFromHome > 128.0f && this.guardModeTimer == 1)) {
                    currentTarget = null;
                    this.revengeTarget = null;
                }
                if (currentTarget != null && !this.MyCanSee(currentTarget)) {
                    currentTarget = null;
                }
            }

            nearbyTarget = this.findSomethingToAttack();

            if (this.headEntityFound == 0) {
                KingHead head = ModEntities.KING_HEAD.get().create(this.level());
                if (head != null) {
                    head.moveTo(this.getX(), this.getY() + 20, this.getZ(), 0.0F, 0.0F);
                    this.level().addFreshEntity(head);
                    this.headEntityFound = 1;
                }
            }

            if (currentTarget == null) currentTarget = nearbyTarget;

            if (currentTarget != null) {
                this.setAttacking(1);

                if (this.backoffTimer == 0) {
                    int flightY = Math.min((int) (currentTarget.getY() + currentTarget.getBbHeight() / 2.0f + 1.0), 230);
                    this.currentFlightTarget = new BlockPos((int) currentTarget.getX(), flightY, (int) currentTarget.getZ());
                    if (this.getRandom().nextInt(70) == 1) {
                        this.backoffTimer = 80 + this.getRandom().nextInt(80);
                    }
                } else if (this.flightTargetDistSqr() < 9.1) {
                    randomZOffset = this.getRandom().nextInt(20) + 30;
                    randomXOffset = this.getRandom().nextInt(20) + 30;
                    if (this.getRandom().nextInt(2) == 0) randomZOffset = -randomZOffset;
                    if (this.getRandom().nextInt(2) == 0) randomXOffset = -randomXOffset;

                    int altAdj = computeAltitudeAdjustment((int) currentTarget.getX(), (int) currentTarget.getZ());
                    int flightY = Math.min((int) (this.getY() + altAdj), 230);
                    this.currentFlightTarget = new BlockPos((int) currentTarget.getX() + randomXOffset, flightY, (int) currentTarget.getZ() + randomZOffset);
                }

                // Melee range area + direct attack
                if (this.distanceToSqr(currentTarget) < 900.0) {
                    if (this.getRandom().nextInt(2) == 1) {
                        this.doJumpDamage(this.getX(), this.getY(), this.getZ(),
                                15.0, ATTACK_DAMAGE_VALUE / 4, 0);
                    }
                    this.doHurtTarget(currentTarget);
                }

                // Forward area damage
                double forwardX = this.getX() + 20.0 * Math.sin(Math.toRadians(this.yHeadRot));
                double forwardZ = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.yHeadRot));
                if (this.getRandom().nextInt(3) == 1) {
                    this.doJumpDamage(forwardX, this.getY() + 10.0, forwardZ,
                            15.0, ATTACK_DAMAGE_VALUE / 2, 1);
                }

                // Ranged attacks when target is far
                if (this.getHorizontalDistanceSqToEntity(currentTarget) > 900.0) {
                    attackChoice = this.getRandom().nextInt(3);
                    if (attackChoice == 0 && this.fireballStreamCount > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(currentTarget)) this.firecanon(currentTarget);
                    } else if (attackChoice == 1 && this.lightningStreamCount > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(currentTarget)) this.firecanonl(currentTarget);
                    } else if (this.iceStreamCount > 0) {
                        this.setAttacking(1);
                        if (isAimedAt(currentTarget)) this.firecanoni(currentTarget);
                    }
                }
            } else {
                this.setAttacking(0);
                this.fireballStreamCount = 10;
                this.lightningStreamCount = 5;
                this.iceStreamCount = 8;
            }
        }

        // Purple power in enraged mode
        if (this.getAttacking() != 0 && this.isEnd == 2) {
            double xzoff = 10.0;
            double yoff = 14.0;
            PurplePower pwr = ModEntities.PURPLE_POWER.get().create(this.level());
            if (pwr != null) {
                pwr.moveTo(
                    this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())),
                    this.getY() + yoff,
                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())),
                    0, 0);
                Vec3 mot = this.getDeltaMovement();
                pwr.setDeltaMovement(mot.x * 3.0, 0, mot.z * 3.0);
                pwr.setPurpleType(10);
                this.level().addFreshEntity(pwr);
            }
        }

        // ---- Flight movement ----
        double goalX = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double goalY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double goalZ = this.currentFlightTarget.getZ() + 0.5 - this.getZ();

        Vec3 mot = this.getDeltaMovement();
        double newMx = mot.x + (Math.signum(goalX) * 0.7 - mot.x) * 0.35;
        double newMy = mot.y + (Math.signum(goalY) * 0.69999 - mot.y) * 0.3;
        double newMz = mot.z + (Math.signum(goalZ) * 0.7 - mot.z) * 0.35;
        this.setDeltaMovement(newMx, newMy, newMz);

        float targetYaw = (float) (Math.atan2(newMz, newMx) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + yawDelta / 8.0f);

        // ---- Passive healing ----
        if (this.getRandom().nextInt(30) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(5.0f);
            if (this.largeEntityDetected != 0) {
                this.heal(200.0f);
            }
        }
        if (this.playerHitCount < 10 && this.getHealth() < 2000.0f) {
            this.heal(2000.0f - this.getHealth());
        }
    }

    // ---- Combat ----

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target == null) return false;

        if (target instanceof LivingEntity living) {
            float entitySize = living.getBbHeight() * living.getBbWidth();
            if (entitySize > 30.0f
                    && !MyUtils.isRoyalty(living)
                    && !MyUtils.isBigBoss(living)
                    && !(living instanceof EnderDragon)) {
                living.setHealth(living.getHealth() / 2.0f);
                living.hurt(this.damageSources().mobAttack(this), (float) this.attackDamage * 10.0f);
                this.largeEntityDetected = 1;
            }
        }

        if (target instanceof EnderDragon dragon) {
            DamageSource genDmg = this.damageSources().generic();
            dragon.head.hurt(genDmg, (float) this.attackDamage);
        }

        boolean hit = target.hurt(this.damageSources().mobAttack(this), (float) this.attackDamage);
        if (hit) {
            double knockbackStrength = 3.3;
            double upwardKnockback = 0.25;
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
        boolean ret = false;
        if (this.hurtCooldown > 0) return false;

        float clampedDamage = Math.min(amount, 750.0f);

        if (source.getMsgId().equals("inWall")) return false;

        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living) {
            float entitySize = living.getBbHeight() * living.getBbWidth();
            if (entitySize > 30.0f
                    && !MyUtils.isRoyalty(living)
                    && !MyUtils.isBigBoss(living)
                    ) {
                clampedDamage /= 10.0f;
                this.hurtCooldown = 50;
                this.largeEntityDetected = 1;
            }
            if (living instanceof Monster && entitySize < 3.0f) {
                living.discard();
                return false;
            }
        }

        if (!source.getMsgId().equals("cactus")) {
            this.hurtCooldown = 20;
            ret = super.hurt(source, clampedDamage);
            if (attacker instanceof Player) {
                this.playerHitCount++;
            }
            if (attacker instanceof LivingEntity living && this.currentFlightTarget != null
                    && !MyUtils.isRoyalty(attacker)
                    ) {
                this.revengeTarget = living;
                int flightY = Math.min((int) attacker.getY(), 230);
                this.currentFlightTarget = new BlockPos((int) attacker.getX(), flightY, (int) attacker.getZ());
            }
        }
        return ret;
    }

    @Override
    public int getArmorValue() {
        if (this.largeEntityDetected != 0) return 25;
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() * 2 / 3))
            return DEFENSE_VALUE + 1;
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 2))
            return DEFENSE_VALUE + 2;
        if (this.playerHitCount < 10 && this.getHealth() < (float) (this.mygetMaxHealth() / 4))
            return DEFENSE_VALUE + 3;
        return DEFENSE_VALUE;
    }

    // ---- Projectile attacks ----

    private void firecanon(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.fireballStreamCount > 0) {
            this.playSound(SoundEvents.TNT_PRIMED, 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            BetterFireball bf = new BetterFireball(this.level(), this,
                    new Vec3(e.getX() - cx,
                             e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff),
                             e.getZ() - cz));
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            this.level().addFreshEntity(bf);

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

            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.fireballStreamCount--;
        }
    }

    private void firecanonl(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.lightningStreamCount > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

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

            this.lightningStreamCount--;
        }
    }

    private void firecanoni(LivingEntity e) {
        double yoff = 14.0;
        double xzoff = 32.0;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));

        if (this.iceStreamCount > 0) {
            this.level().playSound(null, cx, this.getY() + yoff, cz,
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE,
                    1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

            for (int i = 0; i < 5; i++) {
                IceBall lb = new IceBall(ModEntities.ICE_BALL.get(), this.level());
                lb.setOwner(this);
                lb.enableIceCreation();
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

            this.iceStreamCount--;
        }
    }

    private LivingEntity doJumpDamage(double x, double y, double z, double dist, double damage, int knock) {
        AABB bb = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, bb);
        entities.sort(this.targetSorter);

        for (LivingEntity target : entities) {
            if (target == this || !target.isAlive()) continue;
            if (MyUtils.isRoyalty(target)) continue;
            if (target instanceof Ghost || target instanceof GhostSkelly) continue;

            target.hurt(this.damageSources().magic(), (float) damage / 2.0f);
            target.hurt(this.damageSources().generic(), (float) damage / 2.0f);

            target.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);

            if (knock != 0) {
                double knockbackStrength = 2.75;
                double upwardKnockback = 0.65;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
        }
        return null;
    }

    // ---- Targeting ----

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;

        if (target instanceof KingHead) { headEntityFound = 1; return false; }
        if (MyUtils.isRoyalty(target)) return false;

        float deltaX = (float) (target.getX() - this.homeX);
        float deltaZ = (float) (target.getZ() - this.homeZ);
        if (Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) > 144.0f) return false;

        if (MyUtils.isIgnoreable(target)) return false;

        if (this.isEnd == 2) {
            if (target instanceof Player player) {
                return !player.getAbilities().instabuild;
            }
            if (target instanceof Girlfriend || target instanceof Boyfriend) return true;
            if (target instanceof Villager) return true;
        }

        if (!this.MyCanSee(target)) return false;

        if (target instanceof Player player) {
            return !player.getAbilities().instabuild;
        }
        if (target instanceof Horse) return true;
        if (target instanceof Monster) return true;
        if (target instanceof EnderDragon) return true;
        return MyUtils.isAttackableNonMob(target);
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(80.0, 64.0, 80.0);

        if (this.isEnd == 2) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);
            players.sort(this.targetSorter);
            this.headEntityFound = 1;
            for (Player player : players) {
                if (this.isSuitableTarget(player)) return player;
            }
        }

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        LivingEntity ret = null;
        this.headEntityFound = 0;
        for (LivingEntity entity : entities) {
            if (entity instanceof KingHead) { this.headEntityFound = 1; }
            if (ret == null && this.isSuitableTarget(entity)) {
                ret = entity;
            }
            if (ret != null && this.headEntityFound != 0) break;
        }
        return ret;
    }

    // ---- Helpers ----

    private boolean isAimedAt(LivingEntity e) {
        double rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
        double rhdir = Math.toRadians((this.yHeadRot + 90.0f) % 360.0f);
        double rdd = Math.abs(rr - rhdir) % (Math.PI * 2.0);
        if (rdd > Math.PI) rdd -= Math.PI * 2.0;
        return Math.abs(rdd) < 0.5;
    }

    public boolean MyCanSee(LivingEntity e) {
        double xzoff = 22.0;
        int nblks = 20;
        double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + this.getBbHeight() * 7.0f / 8.0f);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 20.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 20.0);
        float dz = (float) ((e.getZ() - startz) / 20.0);

        if (Math.abs(dx) > 1.0f) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int) (nblks * Math.abs(dx));
            dx = Mth.clamp(dx, -1.0f, 1.0f);
        }
        if (Math.abs(dy) > 1.0f) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int) (nblks * Math.abs(dy));
            dy = Mth.clamp(dy, -1.0f, 1.0f);
        }
        if (Math.abs(dz) > 1.0f) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int) (nblks * Math.abs(dz));
            dz = Mth.clamp(dz, -1.0f, 1.0f);
        }

        for (int i = 0; i < nblks; i++) {
            startx += dx;
            starty += dy;
            startz += dz;
            BlockState state = this.level().getBlockState(BlockPos.containing(startx, starty, startz));
            if (state.isAir() || !state.getFluidState().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private boolean tooFarFromHome() {
        float deltaX = (float) (this.getX() - this.homeX);
        float deltaZ = (float) (this.getZ() - this.homeZ);
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) > 120.0f;
    }

    private double flightTargetDistSqr() {
        if (currentFlightTarget == null) return Double.MAX_VALUE;
        double dx = currentFlightTarget.getX() - this.getX();
        double dy = currentFlightTarget.getY() - this.getY();
        double dz = currentFlightTarget.getZ() - this.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private double getHorizontalDistanceSqToEntity(Entity entity) {
        double deltaZ = entity.getZ() - this.getZ();
        double deltaX = entity.getX() - this.getX();
        return deltaZ * deltaZ + deltaX * deltaX;
    }

    private int computeAltitudeAdjustment(int centerX, int centerZ) {
        int dist = 0;
        for (int i = -5; i <= 5; i += 5) {
            scan:
            for (int j = -5; j <= 5; j += 5) {
                BlockPos checkPos = new BlockPos(centerX + j, (int) this.getY(), centerZ + i);
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.above(k));
                        dist++;
                        if (state.isAir()) continue scan;
                    }
                } else {
                    for (int k = 1; k < 20; k++) {
                        state = this.level().getBlockState(checkPos.below(k));
                        dist--;
                        if (!state.isAir()) continue scan;
                    }
                }
            }
        }
        return dist / 9 + 2;
    }

    private void msgToPlayers(String s) {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        for (Player p : players) {
            p.sendSystemMessage(Component.literal(s));
        }
    }

    private Player findNearestPlayer() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        players.sort(this.targetSorter);
        return players.isEmpty() ? null : players.get(0);
    }

    public void setGuardMode(int i) {
        this.guardModeTimer = i;
    }

    public void setFree() {
        this.isEnd = 1;
    }

    // ---- Drops ----

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

        ThePrince prince = ModEntities.THE_PRINCE.get().create(this.level());
        if (prince != null) {
            prince.moveTo(this.getX(), this.getY() + 10, this.getZ(), 0.0F, 0.0F);
            this.level().addFreshEntity(prince);
        }

        dropItemRand(new ItemStack(ModItems.ROYAL_GUARDIAN_SWORD.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_HELMET.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_CHESTPLATE.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_LEGGINGS.get(), 1));
        dropItemRand(new ItemStack(ModItems.ROYAL_BOOTS.get(), 1));

        int icount = BuiltInRegistries.ITEM.size();
        int j = 0;
        while (j < 150) {
            Item item = BuiltInRegistries.ITEM.byId(this.getRandom().nextInt(icount));
            if (item == null || item == Items.AIR) continue;
            j++;
            dropItemRand(new ItemStack(item, 1));
        }

        int bcount = BuiltInRegistries.BLOCK.size();
        j = 0;
        while (j < 150) {
            Block block = BuiltInRegistries.BLOCK.byId(this.getRandom().nextInt(bcount));
            if (block == null || block == Blocks.AIR) continue;
            Item blockItem = block.asItem();
            if (blockItem == Items.AIR) continue;
            j++;
            dropItemRand(new ItemStack(blockItem, 1));
        }
    }

    // ---- NBT ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KingHomeX", this.homeX);
        tag.putInt("KingHomeZ", this.homeZ);
        tag.putInt("GuardMode", this.guardModeTimer);
        tag.putInt("PlayerHits", this.playerHitCount);
        tag.putInt("IsEnd", this.isEnd);
        tag.putInt("EndCounter", this.endCounter);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.homeX = tag.getInt("KingHomeX");
        this.homeZ = tag.getInt("KingHomeZ");
        this.guardModeTimer = tag.getInt("GuardMode");
        this.playerHitCount = tag.getInt("PlayerHits");
        this.isEnd = tag.getInt("IsEnd");
        this.endCounter = tag.getInt("EndCounter");
    }
}
