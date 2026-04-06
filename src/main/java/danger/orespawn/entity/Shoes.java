package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
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

    private float myRotation = 0.0f;

    public Shoes(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public Shoes(Level level, LivingEntity shooter, int shoeId) {
        super(EntityType.SNOWBALL, level); // TODO: use registered type
        this.setOwner(shooter);
        this.entityData.set(DATA_SHOE_ID, shoeId);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SHOE_ID, 2);
    }

    public int getShoeId() { return this.entityData.get(DATA_SHOE_ID); }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        float damage = 2.0f;
        if (this.getShoeId() == 6) damage = 6.0f;
        if (target instanceof Creeper) damage += 4.0f;
        if (target instanceof Player) damage = 0.0f;

        target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level().isClientSide) {
            for (int i = 0; i < 4; ++i) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL,
                        this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
        if (!this.level().isClientSide) this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        this.myRotation += 20.0f;
        if (this.myRotation > 360.0f) this.myRotation -= 360.0f;
        this.setXRot(this.myRotation);
    }
}
