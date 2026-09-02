package danger.orespawn.entity;

import danger.orespawn.entity.pose.RockBasePose;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class RockBase extends Mob implements RockBasePose {
    private static final EntityDataAccessor<Integer> DATA_ROCK_TYPE =
            SynchedEntityData.defineId(RockBase.class, EntityDataSerializers.INT);

    public int rockType = 0;

    public RockBase(EntityType<? extends RockBase> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROCK_TYPE, 0);
    }

    public int getRockType() { return this.entityData.get(DATA_ROCK_TYPE); }

    public void setRockType(int type) {
        if (this.level() == null || this.level().isClientSide()) return;
        this.entityData.set(DATA_ROCK_TYPE, type);
    }

    public void placeRock(int type) {
        this.rockType = type;
        this.setRockType(type);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1 + type / 4.0);
        this.setHealth(1 + type / 4.0f);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity) {
            this.playSound(SoundEvents.ITEM_PICKUP, 0.75f, 2.25f);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.setXRot(0.0f);
        this.yBodyRot = 0.0f;
        this.setYRot(0.0f);

        if (this.level().isClientSide()) {
            this.rockType = this.getRockType();
        }

        if (!this.level().isClientSide() && this.rockType == 0) {
            // orig RockBase.java:93-143 — the one-shot type lottery branches on the
            // dimension: outside DimensionID5 (Crystal) all twelve types roll with
            // rising rarity; inside it the mob is forced to the crystal types 9-12
            // so Crystal-dimension rocks never come up mundane.
            if (!danger.orespawn.ModDimensionKeys.isIn(this.level(), danger.orespawn.ModDimensionKeys.CRYSTAL)) {
                this.rockType = 1;
                if (this.getRandom().nextInt(10) == 0) this.rockType = 2;
                if (this.getRandom().nextInt(20) == 0) this.rockType = 3;
                if (this.getRandom().nextInt(30) == 0) this.rockType = 4;
                if (this.getRandom().nextInt(40) == 0) this.rockType = 5;
                if (this.getRandom().nextInt(50) == 0) this.rockType = 6;
                if (this.getRandom().nextInt(100) == 0) this.rockType = 7;
                if (this.getRandom().nextInt(200) == 0) this.rockType = 8;
                if (this.getRandom().nextInt(500) == 0) this.rockType = 9;
                if (this.getRandom().nextInt(500) == 0) this.rockType = 10;
                if (this.getRandom().nextInt(500) == 0) this.rockType = 11;
                if (this.getRandom().nextInt(1000) == 0) this.rockType = 12;
            } else {
                // orig RockBase.java:129-140 — Crystal-dimension lottery:
                // base 9, 1-in-3 -> 10, 1-in-5 -> 11, 1-in-10 -> 12.
                this.rockType = 9;
                if (this.getRandom().nextInt(3) == 0) this.rockType = 10;
                if (this.getRandom().nextInt(5) == 0) this.rockType = 11;
                if (this.getRandom().nextInt(10) == 0) this.rockType = 12;
            }
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1 + this.rockType / 4.0);
            this.setHealth(1 + this.rockType / 4.0f);
        }

        if (!this.level().isClientSide()) {
            this.setRockType(this.rockType);
        }

        if (this.level().isClientSide() && this.getRandom().nextInt(20) == 0) {
            if (this.rockType == 9)
                this.level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0.05, 0);
            if (this.rockType == 10)
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.25, getZ(), 0, 0.1, 0);
            if (this.rockType == 11)
                this.level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0.05, 0);
            if (this.rockType == 12)
                this.level().addParticle(ParticleTypes.FIREWORK, getX(), getY() + 0.25, getZ(), 0, 0.1, 0);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ButterflyType", this.rockType);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.rockType = tag.getInt("ButterflyType");
    }

    /**
     * orig RockBase.java:213-251 — func_70645_a: on death the rock vanishes and
     * drops exactly one rock item matching its type (types 1-12).
     */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level().isClientSide()) return;
        net.minecraft.world.item.Item drop = switch (this.rockType) {
            case 1 -> danger.orespawn.ModItems.ROCK_SMALL.get();
            case 2 -> danger.orespawn.ModItems.ROCK.get();
            case 3 -> danger.orespawn.ModItems.ROCK_RED.get();
            case 4 -> danger.orespawn.ModItems.ROCK_GREEN.get();
            case 5 -> danger.orespawn.ModItems.ROCK_BLUE.get();
            case 6 -> danger.orespawn.ModItems.ROCK_PURPLE.get();
            case 7 -> danger.orespawn.ModItems.ROCK_SPIKEY.get();
            case 8 -> danger.orespawn.ModItems.ROCK_TNT.get();
            case 9 -> danger.orespawn.ModItems.ROCK_CRYSTAL_RED.get();
            case 10 -> danger.orespawn.ModItems.ROCK_CRYSTAL_GREEN.get();
            case 11 -> danger.orespawn.ModItems.ROCK_CRYSTAL_BLUE.get();
            case 12 -> danger.orespawn.ModItems.ROCK_CRYSTAL_TNT.get();
            default -> null;
        };
        if (drop != null) {
            this.spawnAtLocation(new net.minecraft.world.item.ItemStack(drop));
        }
        this.discard(); // orig :214 — func_70106_y, no death animation
    }

    /** orig RockBase.java:191-193 — y>=50. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return this.getY() >= 50.0;
    }
}
