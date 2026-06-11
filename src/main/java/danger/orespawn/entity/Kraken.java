package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModSounds;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;

public class Kraken extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Kraken"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    private final Comparator<Entity> targetSorter;
    private BlockPos currentFlightTarget = null;
    private LivingEntity caught = null;
    private int newtarget = 0;
    private int release = 0;
    private int weatherSet = 10;
    private int longEnough = 3600;
    private int callReinforcements = 0;
    private boolean hitByPlayer = false;
    private int straightDown = 1;
    private int hurtTimer = 0;

    public Kraken(EntityType<? extends Kraken> type, Level level) {
        super(type, level);
        this.xpReward = 500;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6515 — Kraken 1000 HP / 40 ATK / 10 armor;
        // speed 0.37 hardcoded in orig Kraken.java:90.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.KRAKEN.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.37)
                .add(Attributes.ATTACK_DAMAGE, MobStats.KRAKEN.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, MobStats.KRAKEN.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        if (this.longEnough <= 0) return true;
        if (this.getY() > 150.0 && this.getHealth() < this.mygetMaxHealth() / 2.0f) return true;
        if (this.getY() > 180.0 && this.longEnough <= 0) {
            this.discard();
            return true;
        }
        return false;
    }

    public int mygetMaxHealth() {
        return 3000;
    }

    public int getKrakenHealth() {
        return (int) this.getHealth();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) return;

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), (int) (this.getY() - 10.0), (int) this.getZ());
        } else {
            Vec3 motion = this.getDeltaMovement();
            double dampedY = this.getY() < this.currentFlightTarget.getY()
                    ? motion.y * 0.72 : motion.y * 0.5;
            this.setDeltaMovement(motion.x, dampedY, motion.z);
        }

        if (this.weatherSet > 0) {
            --this.weatherSet;
            if (this.weatherSet == 0 && this.level() instanceof ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(0, 6000, true, true);
                this.weatherSet = 100;
            }
        }
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

        if (this.hurtTimer > 0) --this.hurtTimer;
        if (this.longEnough > 0) --this.longEnough;

        if (this.getRandom().nextInt(400) == 1 && this.level() instanceof ServerLevel serverLevel) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(this.getX(), this.getY() - 16.0, this.getZ());
                serverLevel.addFreshEntity(bolt);
            }
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), (int) this.getY(), (int) this.getZ());
        }

        double dxFlight = this.currentFlightTarget.getX() - this.getX();
        double dyFlight = this.currentFlightTarget.getY() - this.getY();
        double dzFlight = this.currentFlightTarget.getZ() - this.getZ();
        double distSqToTarget = dxFlight * dxFlight + dyFlight * dyFlight + dzFlight * dzFlight;

        if (this.newtarget != 0 || this.getRandom().nextInt(250) == 1 || distSqToTarget < 9.1) {
            pickNewFlightTarget();
        } else if (this.caught == null && this.getRandom().nextInt(8) == 1) {
            searchForPrey();
        }

        handleCaughtEntity();
        applyFlightMovement();
        applyObstructionAvoidance();

        if (this.getY() > 256.0 && !this.isPersistenceRequired()) {
            this.discard();
        }
    }

    private void pickNewFlightTarget() {
        this.newtarget = 0;
        int groundDist;
        for (groundDist = 0; groundDist < 31; ++groundDist) {
            BlockPos checkPos = new BlockPos(
                    (int) this.getX(), (int) this.getY() - groundDist, (int) this.getZ());
            if (!this.level().getBlockState(checkPos).isAir()) {
                this.straightDown = 0;
                break;
            }
        }
        groundDist = 20 - groundDist;

        int keepTrying = 50;
        boolean foundAir = false;
        int targetX = (int) this.getX();
        int targetY = (int) this.getY() + groundDist;
        int targetZ = (int) this.getZ();

        while (!foundAir && keepTrying > 0) {
            int xdir = this.getRandom().nextInt(6) + 12;
            int zdir = this.getRandom().nextInt(6) + 12;
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;
            if (this.straightDown != 0) {
                xdir = 0;
                zdir = 0;
            }
            targetX = (int) this.getX() + xdir;
            targetY = (int) this.getY() + groundDist + this.getRandom().nextInt(9) - 6;
            targetZ = (int) this.getZ() + zdir;
            BlockPos candidatePos = new BlockPos(targetX, targetY, targetZ);
            if (this.level().getBlockState(candidatePos).isAir()
                    && this.canSeeTarget(targetX, targetY, targetZ)) {
                foundAir = true;
            }
            --keepTrying;
        }
        this.currentFlightTarget = new BlockPos(targetX, targetY, targetZ);

        if (this.longEnough <= 0
                || (this.getY() < 200.0 && this.getHealth() < this.mygetMaxHealth() / 4.0f)) {
            this.currentFlightTarget = new BlockPos(
                    this.currentFlightTarget.getX(),
                    this.currentFlightTarget.getY() + 30,
                    this.currentFlightTarget.getZ());

            if (this.hitByPlayer && this.callReinforcements == 0
                    && this.getHealth() < this.mygetMaxHealth() / 8.0f
                    && this.getY() > 130.0
                    && this.level() instanceof ServerLevel serverLevel) {
                this.callReinforcements = 1;
                for (int i = 0; i < 10; i++) {
                    Entity newEntity = this.getType().create(serverLevel);
                    if (newEntity != null) {
                        double sx = this.getX() + this.getRandom().nextInt(10)
                                - this.getRandom().nextInt(10);
                        double sz = this.getZ() + this.getRandom().nextInt(10)
                                - this.getRandom().nextInt(10);
                        newEntity.moveTo(sx, 170.0, sz,
                                this.getRandom().nextFloat() * 360.0f, 0.0f);
                        serverLevel.addFreshEntity(newEntity);
                    }
                }
            }
        }
    }

    private void searchForPrey() {
        Player playerTarget = findNearestValidPlayer();
        if (playerTarget != null && this.getSensing().hasLineOfSight(playerTarget)) {
            this.currentFlightTarget = new BlockPos(
                    (int) playerTarget.getX(),
                    (int) playerTarget.getY() + 15,
                    (int) playerTarget.getZ());
            this.attackWithSomething(playerTarget);
        }
        if (playerTarget == null && this.getRandom().nextInt(2) == 0) {
            LivingEntity entityTarget = findSomethingToAttack();
            if (entityTarget != null) {
                this.currentFlightTarget = new BlockPos(
                        (int) entityTarget.getX(),
                        (int) entityTarget.getY() + 15,
                        (int) entityTarget.getZ());
                this.attackWithSomething(entityTarget);
            }
        }
    }

    private void handleCaughtEntity() {
        if (this.caught == null) return;

        if (!this.caught.isRemoved() && this.caught.isAlive()) {
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX(), 200, (int) this.getZ());
            if (this.getY() > 190.0) {
                this.release = 1;
            }
            Vec3 myMotion = this.getDeltaMovement();
            this.caught.setDeltaMovement(myMotion);
            if (this.getY() - this.caught.getY() > 16.0) {
                this.caught.setDeltaMovement(
                        this.caught.getDeltaMovement().add(0, 0.25, 0));
            }
            // The grab holds the victim 15 blocks below the Kraken (original behavior).
            // Players are client-authoritative: raw setPos/setDeltaMovement on a
            // ServerPlayer desyncs the client and triggers "moved wrongly" kicks
            // (BUG-011), so players are moved via the connection teleport and their
            // forced motion is flagged for sync with hurtMarked.
            double grabX = this.getX();
            double grabY = this.getY() - 15.0;
            double grabZ = this.getZ();
            if (this.caught instanceof ServerPlayer caughtPlayer) {
                caughtPlayer.connection.teleport(grabX, grabY, grabZ, this.getYRot(), caughtPlayer.getXRot());
                caughtPlayer.hurtMarked = true;
            } else {
                this.caught.setPos(grabX, grabY, grabZ);
                this.caught.setYRot(this.getYRot());
            }

            if (this.getRandom().nextInt(50) == 1) {
                this.doHurtTarget(this.caught);
            }
            if (this.release != 0 || this.getRandom().nextInt(250) == 1) {
                releaseCaughtEntity();
            }
        } else {
            releaseCaughtEntity();
        }
    }

    private void releaseCaughtEntity() {
        this.caught = null;
        this.newtarget = 1;
        this.release = 0;
        this.setAttacking(0);
    }

    private void applyFlightMovement() {
        double toTargetX = this.currentFlightTarget.getX() + 0.3 - this.getX();
        double toTargetY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double toTargetZ = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(toTargetX) * 0.45 - motion.x) * 0.15;
        double my = motion.y + (Math.signum(toTargetY) * 0.70999 - motion.y) * 0.202;
        double mz = motion.z + (Math.signum(toTargetZ) * 0.45 - motion.z) * 0.15;
        this.setDeltaMovement(mx, my, mz);

        float targetYaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        float yawDiff = Mth.wrapDegrees(targetYaw - this.getYRot());
        this.zza = 0.4f;
        if (Math.abs(mx) + Math.abs(mz) < 0.15) {
            yawDiff = 0.0f;
        }
        this.setYRot(this.getYRot() + yawDiff / 5.0f);
    }

    private void applyObstructionAvoidance() {
        double obstructionFactor = 0.0;
        for (int k = -20; k < 18; k += 2) {
            for (int i = 1; i < 10; i += 2) {
                double dx = (double) i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                double dz = (double) i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                BlockPos checkPos = new BlockPos(
                        (int) (this.getX() + dx),
                        (int) this.getY() + k,
                        (int) (this.getZ() + dz));
                if (!this.level().getBlockState(checkPos).isAir()) {
                    obstructionFactor += 0.1;
                }
            }
        }
        if (obstructionFactor > 0) {
            this.setDeltaMovement(
                    this.getDeltaMovement().add(0, obstructionFactor * 0.08, 0));
            this.setPos(this.getX(),
                    this.getY() + obstructionFactor * 0.08, this.getZ());
        }
    }

    private Player findNearestValidPlayer() {
        AABB searchBox = this.getBoundingBox().inflate(25.0, 40.0, 25.0);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox);
        Player nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Player player : players) {
            if (player.getAbilities().instabuild) continue;
            double distSq = this.distanceToSqr(player);
            if (distSq < minDist) {
                minDist = distSq;
                nearest = player;
            }
        }
        return nearest;
    }

    private void attackWithSomething(LivingEntity target) {
        if (this.caught != null) return;
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        double dy = this.getY() - target.getY() - 15.0;
        double dist = dx * dx + dz * dz + dy * dy;
        if (dist < 30.0) {
            this.caught = target;
            this.release = 0;
            this.setAttacking(1);
        }
    }

    public boolean canSeeTarget(double targetX, double targetY, double targetZ) {
        Vec3 from = new Vec3(this.getX(), this.getY() + 0.75, this.getZ());
        Vec3 to = new Vec3(targetX, targetY, targetZ);
        HitResult result = this.level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (this.currentFlightTarget != null && attacker instanceof Player
                && this.getHealth() > this.mygetMaxHealth() / 4.0f) {
            this.hitByPlayer = true;
            this.currentFlightTarget = new BlockPos(
                    (int) attacker.getX(), (int) attacker.getY() + 15, (int) attacker.getZ());
        }
        if (this.hurtTimer > 0) return false;
        this.hurtTimer = 30;
        boolean ret = super.hurt(source, amount);
        if (this.getRandom().nextInt(2) == 1) {
            this.release = 1;
        }
        return ret;
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

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(5) == 0) {
            return ModSounds.KRAKEN_LIVING.get(); // orig Kraken.java:199-204 "orespawn:kraken_living" 1-in-5
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get(); // orig Kraken.java:210-212 "orespawn:alo_death"
    }

    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LongEnough", this.longEnough);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.longEnough = tag.getInt("LongEnough");
    }

    // Death drops are fully data-driven via loot_table/entities/kraken.json
    // (orig Kraken.java:236-871: kraken tooth, painting, 120-279 ink sac,
    // 5-14 rolls of the d53 Ultimate/Diamond/Iron/Gold/Experience/Amethyst
    // gear table).

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Kraken) return false;
        if (target instanceof Squid) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().instabuild && !player.getAbilities().invulnerable;
        }
        if (!target.onGround() && !target.isInWater()) return false;
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(20.0, 40.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity entity : entities) {
            if (isSuitableTarget(entity)) return entity;
        }
        return null;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }
}
