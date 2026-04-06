package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Capture cage projectile. When it hits a mob, it captures it (stores mob data),
 * and when the resulting cage item is used, it re-spawns the mob.
 * This is a simplified port -- the original had a massive type-mapping table.
 */
public class EntityCage extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_CAGE_INDEX =
            SynchedEntityData.defineId(EntityCage.class, EntityDataSerializers.INT);

    private static final int DEFAULT_CAGE_INDEX = 160;
    private static final int CAPTURE_SUCCESS_ROLL_RANGE = 10;
    private static final int CAPTURE_SUCCESS_THRESHOLD = 2;
    private static final int HIT_PARTICLE_BURST_COUNT = 4;
    private static final double PARTICLE_OFFSET_Y = 0.25;
    private static final float EXPLODE_SOUND_PITCH = 1.5f;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private int cageIndex = DEFAULT_CAGE_INDEX;

    public EntityCage(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityCage(Level level, Player thrower, int index) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(thrower);
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
            for (int particleIndex = 0; particleIndex < HIT_PARTICLE_BURST_COUNT; ++particleIndex) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        target.getX(), target.getY() + PARTICLE_OFFSET_Y, target.getZ(), 0, 0, 0);
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY() + PARTICLE_OFFSET_Y, target.getZ(), 0, 0, 0);
            }
            if (this.getOwner() != null) {
                this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, EXPLODE_SOUND_PITCH);
            }

            if (this.random.nextInt(CAPTURE_SUCCESS_ROLL_RANGE) >= CAPTURE_SUCCESS_THRESHOLD) {
                // TODO: Store the captured mob's type + data into a CritterCage item
                // For now, just remove the entity
                mob.discard();

                // TODO: Create and drop a CritterCage item containing the mob data
                // ItemStack cage = new ItemStack(ModItems.CRITTER_CAGE.get());
                // cage.getOrCreateTag().put("CapturedMob", mobData);
                // Block.popResource(this.level(), this.blockPosition(), cage);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            // TODO: If hit block, drop empty critter cage
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

    private float visualRotationDegrees = 0.0f;
}
