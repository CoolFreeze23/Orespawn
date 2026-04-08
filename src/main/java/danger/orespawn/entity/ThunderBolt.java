package danger.orespawn.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThunderBolt extends ThrowableProjectile {
    private static final float TOTAL_DAMAGE = 40.0f;
    private static final float THROWN_DAMAGE_FRACTION = 0.5f;
    private static final int IGNITE_DURATION_SECONDS = 1;
    private static final float EXPLOSION_POWER = 3.0f;
    private static final double LIGHTNING_OFFSET_Y = 1.0;
    private static final float EXPLODE_SOUND_VOLUME = 0.5f;
    private static final int CLIENT_FIREWORK_PARTICLE_COUNT = 4;
    private static final double PARTICLE_GAUSSIAN_SCALE = 10.0;

    public ThunderBolt(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public ThunderBolt(Level level, LivingEntity shooter) {
        super(ModEntities.THUNDER_BOLT.get(), shooter, level);
    }

    public ThunderBolt(Level level, double x, double y, double z) {
        super(ModEntities.THUNDER_BOLT.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        float halfDamage = TOTAL_DAMAGE * THROWN_DAMAGE_FRACTION;
        // Owner could be null or a non-living entity; guard the cast to avoid ClassCastException
        Entity thunderOwner = this.getOwner();
        target.hurt(this.damageSources().thrown(this, thunderOwner), halfDamage);
        if (thunderOwner instanceof LivingEntity livingOwner) {
            target.hurt(this.damageSources().mobAttack(livingOwner), halfDamage);
        } else {
            target.hurt(this.damageSources().thrown(this, thunderOwner), halfDamage);
        }
        target.igniteForSeconds(IGNITE_DURATION_SECONDS);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            boolean canGrief = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER,
                    canGrief, Level.ExplosionInteraction.MOB);
            if (this.level() instanceof ServerLevel serverLevel) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(this.getX(), this.getY() + LIGHTNING_OFFSET_Y, this.getZ());
                    serverLevel.addFreshEntity(bolt);
                }
            }
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), EXPLODE_SOUND_VOLUME,
                1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        // LESS_LAG level 2 skips trail particles entirely to reduce client overhead
        if (this.level().isClientSide && OreSpawnConfig.LESS_LAG.get() < 2) {
            for (int particleIndex = 0; particleIndex < CLIENT_FIREWORK_PARTICLE_COUNT; ++particleIndex) {
                this.level().addParticle(ParticleTypes.FIREWORK,
                        this.getX(), this.getY(), this.getZ(),
                        this.random.nextGaussian() / PARTICLE_GAUSSIAN_SCALE,
                        this.random.nextGaussian() / PARTICLE_GAUSSIAN_SCALE,
                        this.random.nextGaussian() / PARTICLE_GAUSSIAN_SCALE);
            }
        }
    }
}
