package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityWormMedium extends Monster {
    public int upcount = 0;
    public int downcount = 0;

    public EntityWormMedium(EntityType<? extends EntityWormMedium> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    public void pointAtEntity(LivingEntity e) {
        double dx = e.getX() - this.getX();
        double dz = e.getZ() - this.getZ();
        float angle = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        this.setYRot(angle);
        this.yBodyRot = angle;
    }

    @Override
    public void tick() {
        if (this.isVehicle()) {
            this.noPhysics = false;
        }
        super.tick();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.65, motion.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        EntityWormSmall nearbySmall = this.level().getNearestEntity(
                EntityWormSmall.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat(),
                this, this.getX(), this.getY(), this.getZ(),
                this.getBoundingBox().inflate(8.0));

        Player target = null;
        if (nearbySmall == null) {
            target = this.level().getNearestPlayer(this, 8.0);
        }

        if (nearbySmall == null && target != null) {
            if (this.upcount > 0) {
                --this.upcount;
                if (this.upcount == 0) {
                    this.downcount = 100 + this.random.nextInt(150);
                }
                this.pointAtEntity(target);

                BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 0.25, this.getZ());
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    Vec3 mot = this.getDeltaMovement();
                    this.setDeltaMovement(mot.x, mot.y + 0.2, mot.z);
                    this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
                }
            } else {
                if (this.downcount > 0) {
                    --this.downcount;
                } else {
                    this.upcount = 25 + this.random.nextInt(75);
                }
                BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 3, this.getZ());
                BlockState state = this.level().getBlockState(checkPos);
                if (!state.isAir()) {
                    Vec3 mot = this.getDeltaMovement();
                    this.setDeltaMovement(mot.x, mot.y + 0.1, mot.z);
                    this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
                }
            }
        } else {
            this.upcount = this.random.nextInt(50);
            this.downcount = 0;
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 3, this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir()) {
                Vec3 mot = this.getDeltaMovement();
                this.setDeltaMovement(mot.x, mot.y + 0.1, mot.z);
                this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
            }
        }

        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(0, mot.y - 0.01, 0);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        EntityWormSmall nearbySmall = this.level().getNearestEntity(
                EntityWormSmall.class, net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat(),
                this, this.getX(), this.getY(), this.getZ(),
                this.getBoundingBox().inflate(8.0));
        if (nearbySmall != null) return;

        Player target = this.level().getNearestPlayer(this, 2.25);
        if (target != null && !target.getAbilities().invulnerable) {
            this.pointAtEntity(target);
            if (this.upcount > 0 && this.random.nextInt(15) == 1) {
                this.doHurtTarget(target);
            }
        }
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (int i = 0; i < 2; i++) {
            this.spawnAtLocation(Items.ROTTEN_FLESH);
        }
        for (int i = 0; i < 2; i++) {
            this.spawnAtLocation(Items.STRING);
        }
    }
}
