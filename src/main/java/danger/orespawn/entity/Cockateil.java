package danger.orespawn.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModDimensionKeys;
import danger.orespawn.OreSpawnMod;

public class Cockateil extends Animal {
    private static final EntityDataAccessor<Integer> DATA_BIRD_TYPE =
            SynchedEntityData.defineId(Cockateil.class, EntityDataSerializers.INT);

    private BlockPos currentFlightTarget = null;
    public int birdtype;
    private boolean killedByPlayer = false;
    private int stuckCount = 0;
    private int lastX = 0;
    private int lastZ = 0;
    // orig Cockateil.java:38 — starts 0; setFlyUp() latches it to 2 forever
    // (never reset), shrinking horizontal retarget reach and raising the
    // vertical offset. Only RubyBird's entityInit calls it (orig RubyBird.java:19).
    private int flyup = 0;

    public Cockateil(EntityType<? extends Cockateil> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    @Override
    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BIRD_TYPE, 0);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        // orig Cockateil.java:82-86 — birdtype = nextInt(6) at spawn (loaded
        // entities get theirs back from the "BirdType" NBT read).
        this.birdtype = this.random.nextInt(6);
        this.setBirdType(this.birdtype);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    public int getBirdType() {
        return this.entityData.get(DATA_BIRD_TYPE);
    }

    public void setBirdType(int birdTypeIndex) {
        this.entityData.set(DATA_BIRD_TYPE, birdTypeIndex);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    /** orig Cockateil.java:156-158 — latches the permanent upward flight bias (RubyBird.java:19). */
    public void setFlyUp() {
        this.flyup = 2;
    }

    @Override
    public void tick() {
        super.tick();
        // orig Cockateil.java:143-150 — init the target when absent, else damp
        // vertical motion (0.7 below the target, 0.5 at/above it). (int) casts
        // kept over blockPosition(): the orig truncates toward zero, which
        // differs from floor at negative coordinates.
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget =
                    new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
        } else {
            Vec3 currentMotion = this.getDeltaMovement();
            if (this.getY() < this.currentFlightTarget.getY()) {
                this.setDeltaMovement(currentMotion.x, currentMotion.y * 0.7, currentMotion.z);
            } else {
                this.setDeltaMovement(currentMotion.x, currentMotion.y * 0.5, currentMotion.z);
            }
        }
    }

    /** orig Cockateil.java:166-168 — block-only LOS from eye height 0.75 to the target's corner. */
    public boolean canSeeTarget(double px, double py, double pz) {
        return this.level().clip(new ClipContext(
                new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(px, py, pz),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType()
                == HitResult.Type.MISS;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            this.killedByPlayer = true;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        // orig Cockateil.java:170-222 (func_70619_bc), ported line-for-line.
        int keepTrying = 35;
        int stayup = 0;
        if (this.isRemoved()) return;
        super.customServerAiStep();

        // orig :179-181 — DimensionID4 (Islands, ModDimensionKeys javadoc) biases
        // retargeting upward: taller nextInt(9 + stayup) vertical range.
        if (ModDimensionKeys.isIn(this.level(), ModDimensionKeys.ISLANDS)) {
            stayup = 2;
        }

        if (this.lastX == (int) this.getX() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget =
                    new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
        }

        if (this.stuckCount > 40 || this.random.nextInt(250) == 0
                || this.currentFlightTarget.distSqr(
                        new Vec3i((int) this.getX(), (int) this.getY(), (int) this.getZ())) < 4.1) {
            // orig :193-210 — up to 35 tries for an AIR target the bird can SEE;
            // a failed last try still leaves currentFlightTarget at that spot
            // (the orig keeps it too). zdir before xdir preserves the orig RNG
            // draw order; flyup*2 shortens reach, +flyup raises the offset.
            this.stuckCount = 0;
            boolean accepted = false;
            while (!accepted && keepTrying != 0) {
                int zdir = this.random.nextInt(8) + 5 - this.flyup * 2;
                int xdir = this.random.nextInt(8) + 5 - this.flyup * 2;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget = new BlockPos(
                        (int) this.getX() + xdir,
                        (int) this.getY() + this.random.nextInt(9 + stayup) - 5 + this.flyup,
                        (int) this.getZ() + zdir);
                accepted = this.level().getBlockState(this.currentFlightTarget).isAir()
                        && this.canSeeTarget(this.currentFlightTarget.getX(),
                                this.currentFlightTarget.getY(), this.currentFlightTarget.getZ());
                --keepTrying;
            }
        }

        // orig :212-217 — decompiler literals 0.699999 / 0.200000001 kept verbatim.
        double deltaX = this.currentFlightTarget.getX() + 0.3 - this.getX();
        double deltaY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double deltaZ = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        Vec3 currentMotion = this.getDeltaMovement();
        double newMotionX = currentMotion.x + (Math.signum(deltaX) * 0.3 - currentMotion.x) * 0.25;
        double newMotionY = currentMotion.y + (Math.signum(deltaY) * 0.699999 - currentMotion.y) * 0.200000001;
        double newMotionZ = currentMotion.z + (Math.signum(deltaZ) * 0.3 - currentMotion.z) * 0.25;
        this.setDeltaMovement(newMotionX, newMotionY, newMotionZ);

        // orig :218-221 — heading from the fresh motion, yaw blended by a third
        // of the wrapped difference each tick; forward impulse 0.8 (kept even
        // though the move control zeroes zza right after this hook, exactly as
        // the orig's moveHelper zeroed moveForward right after updateAITasks).
        float heading = (float) (Math.atan2(newMotionZ, newMotionX) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(heading - this.getYRot());
        this.zza = 0.8f;
        this.setYRot(this.getYRot() + yawDelta / 3.0f);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().isDay() && !this.level().isRaining()) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "birds"));
        }
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
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.55f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BirdType", this.getBirdType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.birdtype = tag.getInt("BirdType");
        this.setBirdType(this.birdtype);
    }

    /** orig Cockateil.java:232-240 — daytime; Islands always allowed; otherwise y>=50. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;
        return this.getY() >= 50.0;
    }
}
