package danger.orespawn.entity;

import danger.orespawn.ModItems;
import danger.orespawn.item.CagedMobItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityCage extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_CAGE_INDEX =
            SynchedEntityData.defineId(EntityCage.class, EntityDataSerializers.INT);

    private static final int DEFAULT_CAGE_INDEX = 160;
    private static final int HIT_PARTICLE_BURST_COUNT = 4;
    private static final double PARTICLE_OFFSET_Y = 0.25;
    private static final float EXPLODE_SOUND_PITCH = 1.5f;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private int cageIndex = DEFAULT_CAGE_INDEX;
    private float visualRotationDegrees = 0.0f;

    public EntityCage(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityCage(Level level, Player thrower, int index) {
        super(ModEntities.ENTITY_CAGE.get(), thrower, level);
        this.cageIndex = index;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CAGE_INDEX, DEFAULT_CAGE_INDEX);
    }

    public int getCageIndex() { return this.entityData.get(DATA_CAGE_INDEX); }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();

        if (target instanceof Player) {
            this.discard();
            return;
        }

        if (target instanceof Mob mob) {
            for (int i = 0; i < HIT_PARTICLE_BURST_COUNT; ++i) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        target.getX(), target.getY() + PARTICLE_OFFSET_Y, target.getZ(), 0, 0, 0);
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY() + PARTICLE_OFFSET_Y, target.getZ(), 0, 0, 0);
            }
            if (this.getOwner() != null) {
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, EXPLODE_SOUND_PITCH);
            }

            if (this.random.nextInt(10) >= 2) {
                ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
                net.minecraft.nbt.CompoundTag mobData = new net.minecraft.nbt.CompoundTag();
                mob.saveWithoutId(mobData);
                mob.discard();
                ItemStack cageItem = CagedMobItem.createForEntity(entityId, mobData);
                if (mob.hasCustomName()) {
                    cageItem.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, mob.getCustomName());
                }
                Block.popResource(this.level(), this.blockPosition(), cageItem);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            Block.popResource(this.level(), this.blockPosition(), new ItemStack(ModItems.CAGE_EMPTY.get()));
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        if (this.visualRotationDegrees > FULL_ROTATION_DEGREES) this.visualRotationDegrees -= FULL_ROTATION_DEGREES;
        this.setXRot(this.visualRotationDegrees);

        if (this.level().isClientSide) {
            this.entityData.set(DATA_CAGE_INDEX, this.cageIndex);
        }
    }
}
