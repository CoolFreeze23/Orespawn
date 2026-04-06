package danger.orespawn.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

    public int getBirdType() {
        return this.entityData.get(DATA_BIRD_TYPE);
    }

    public void setBirdType(int val) {
        this.entityData.set(DATA_BIRD_TYPE, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.currentFlightTarget != null) {
            Vec3 delta = this.getDeltaMovement();
            if (this.getY() < this.currentFlightTarget.getY()) {
                this.setDeltaMovement(delta.x, delta.y * 0.7, delta.z);
            } else {
                this.setDeltaMovement(delta.x, delta.y * 0.5, delta.z);
            }
        }
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity e = source.getEntity();
        if (e instanceof Player) {
            this.killedByPlayer = true;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.lastX == (int) this.getX() && this.lastZ == (int) this.getZ()) {
            ++this.stuckCount;
        } else {
            this.stuckCount = 0;
            this.lastX = (int) this.getX();
            this.lastZ = (int) this.getZ();
        }

        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }

        if (this.stuckCount > 40 || this.random.nextInt(250) == 0
                || this.currentFlightTarget.distSqr(this.blockPosition()) < 4.1) {
            this.stuckCount = 0;
            int xdir = this.random.nextInt(8) + 5;
            int zdir = this.random.nextInt(8) + 5;
            if (this.random.nextInt(2) == 0) zdir = -zdir;
            if (this.random.nextInt(2) == 0) xdir = -xdir;
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX() + xdir,
                    (int) this.getY() + this.random.nextInt(9) - 5,
                    (int) this.getZ() + zdir);
        }

        double dx = this.currentFlightTarget.getX() + 0.3 - this.getX();
        double dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double dz = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        Vec3 delta = this.getDeltaMovement();
        this.setDeltaMovement(
                delta.x + (Math.signum(dx) * 0.3 - delta.x) * 0.25,
                delta.y + (Math.signum(dy) * 0.7 - delta.y) * 0.2,
                delta.z + (Math.signum(dz) * 0.3 - delta.z) * 0.25);
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
}
