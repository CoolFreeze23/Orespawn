package danger.orespawn.entity;

import danger.orespawn.util.SeasonalDates;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Shoes extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_SHOE_ID =
            SynchedEntityData.defineId(Shoes.class, EntityDataSerializers.INT);

    private static final int DEFAULT_SHOE_ID = 2;
    private static final int HEAVY_SHOE_ID = 6;
    private static final float DAMAGE_DEFAULT = 2.0f;
    private static final float DAMAGE_HEAVY_SHOE = 6.0f;
    private static final float DAMAGE_CREEPER_BONUS = 4.0f;
    private static final int CLIENT_PARTICLE_COUNT = 4;
    private static final float ROTATION_STEP_DEGREES = 20.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private float visualRotationDegrees = 0.0f;

    public Shoes(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public Shoes(Level level, LivingEntity shooter, int shoeId) {
        super(ModEntities.SHOES.get(), shooter, level);
        this.entityData.set(DATA_SHOE_ID, shoeId);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SHOE_ID, DEFAULT_SHOE_ID);
    }

    public int getShoeId() { return this.entityData.get(DATA_SHOE_ID); }

    /** orig Shoes.java:57-79 — damage table in original order (later rules win). */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = DAMAGE_DEFAULT;
        if (this.getShoeId() == HEAVY_SHOE_ID) damage = DAMAGE_HEAVY_SHOE;
        if (target instanceof Creeper) damage += DAMAGE_CREEPER_BONUS;
        // orig :66-71 — companions only take a token 1.0
        if (target instanceof Girlfriend) damage = 1.0f;
        if (target instanceof Boyfriend) damage = 1.0f;
        if (target instanceof Player) damage = 0.0f;
        // orig :75-77 — on Feb 14 every shoe hits for 10 (giant girlfriends
        // throw them back hard)
        if (SeasonalDates.isValentines()) damage = 10.0f;

        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            // orig Shoes.java:80-83 — 4x snowballpoof + 4x reddust
            for (int particleIndex = 0; particleIndex < CLIENT_PARTICLE_COUNT; ++particleIndex) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                this.level().addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0f),
                        this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        if (this.visualRotationDegrees > FULL_ROTATION_DEGREES) this.visualRotationDegrees -= FULL_ROTATION_DEGREES;
        this.setXRot(this.visualRotationDegrees);
    }
}
