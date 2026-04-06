package danger.orespawn.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

public class Elevator extends Mob {
    private static final EntityDataAccessor<Integer> DATA_TIME_SINCE_HIT =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FORWARD_DIRECTION =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE_TAKEN =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_EXPLODING =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);

    private int exploding = 0;
    private int color = 1;
    private int playing = 0;

    public Elevator(EntityType<? extends Elevator> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 1.33)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIME_SINCE_HIT, 0);
        builder.define(DATA_FORWARD_DIRECTION, 1);
        builder.define(DATA_DAMAGE_TAKEN, 0.0f);
        builder.define(DATA_EXPLODING, 0);
        builder.define(DATA_COLOR, 1);
    }

    public void setDamageTaken(float f) { this.entityData.set(DATA_DAMAGE_TAKEN, f); }
    public float getDamageTaken() { return this.entityData.get(DATA_DAMAGE_TAKEN); }
    public void setTimeSinceHit(int v) { this.entityData.set(DATA_TIME_SINCE_HIT, v); }
    public int getTimeSinceHit() { return this.entityData.get(DATA_TIME_SINCE_HIT); }
    public void setForwardDirection(int v) { this.entityData.set(DATA_FORWARD_DIRECTION, v); }
    public int getForwardDirection() { return this.entityData.get(DATA_FORWARD_DIRECTION); }
    public void setExploding(int v) { this.entityData.set(DATA_EXPLODING, v); }
    public int getExploding() { return this.entityData.get(DATA_EXPLODING); }
    public void setColor(int v) { this.entityData.set(DATA_COLOR, v); }
    public int getColor() { return this.entityData.get(DATA_COLOR); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean isPlayer = source.getEntity() instanceof Player;
        if (this.getFirstPassenger() != null && !isPlayer) return false;
        if (source.getMsgId().equals("inWall")) return false;
        if (!this.level().isClientSide() && !this.isRemoved()) {
            this.setForwardDirection(-this.getForwardDirection());
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0f);
            this.markHurt();
            boolean creative = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
            if (creative || this.getDamageTaken() > 40.0f) {
                if (this.getFirstPassenger() != null) this.getFirstPassenger().stopRiding();
                this.discard();
            }
        }
        return true;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.clearFire();
        if (this.isRemoved()) return;
        if (this.getTimeSinceHit() > 0) this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        if (this.getDamageTaken() > 0.0f) this.setDamageTaken(this.getDamageTaken() - 1.0f);

        if (this.playing > 0) --this.playing;
        if (this.getFirstPassenger() != null && this.playing == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "hover")), 0.45f, 1.0f);
            this.playing = 55;
        }

        Vec3 dm = this.getDeltaMovement();
        this.setDeltaMovement(dm.x * 0.98, dm.y * 0.94, dm.z * 0.98);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Player
                && this.getFirstPassenger() != player) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide()) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("HoverColor", this.getColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.color = tag.getInt("HoverColor");
        if (this.color < 1) this.color = 1;
        if (this.color > 10) this.color = 10;
        this.setColor(this.color);
    }
}
