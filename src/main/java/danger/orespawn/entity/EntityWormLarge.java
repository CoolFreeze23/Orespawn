package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityWormLarge extends Monster {
    private int wormsSpawned = 0;

    public EntityWormLarge(EntityType<? extends EntityWormLarge> type, Level level) {
        super(type, level);
        this.xpReward = 2050;
        this.noPhysics = true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 15.0);
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
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
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
        this.setDeltaMovement(motion.x, motion.y * 0.85, motion.z);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        Player target = this.level().getNearestPlayer(this, 8.0);

        if (target != null) {
            this.pointAtEntity(target);
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir()) {
                Vec3 mot = this.getDeltaMovement();
                this.setDeltaMovement(mot.x, mot.y + 0.25, mot.z);
                this.setPos(this.getX(), this.getY() + 0.1, this.getZ());
            } else {
                this.noPhysics = false;
            }
        } else {
            this.noPhysics = true;
            BlockPos checkPos = BlockPos.containing(this.getX(), this.getY() + 3.5, this.getZ());
            BlockState state = this.level().getBlockState(checkPos);
            if (!state.isAir()) {
                Vec3 mot = this.getDeltaMovement();
                this.setDeltaMovement(mot.x, mot.y + 0.1, mot.z);
                this.setPos(this.getX(), this.getY() + 0.05, this.getZ());
            }
        }

        if (this.noPhysics) {
            Vec3 mot = this.getDeltaMovement();
            this.setDeltaMovement(0, mot.y - 0.01, 0);
        }

        if (this.wormsSpawned == 0) {
            this.wormsSpawned = 1;
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 20; ++i) {
                    spawnWorm(serverLevel, ModEntities.ENTITY_WORM_SMALL.get(),
                            this.getX() + this.random.nextInt(6) - this.random.nextInt(6),
                            this.getY(),
                            this.getZ() + this.random.nextInt(6) - this.random.nextInt(6));
                    spawnWorm(serverLevel, ModEntities.ENTITY_WORM_MEDIUM.get(),
                            this.getX() + this.random.nextInt(5) - this.random.nextInt(5),
                            this.getY(),
                            this.getZ() + this.random.nextInt(5) - this.random.nextInt(5));
                }
            }
        }
    }

    private void spawnWorm(ServerLevel level, EntityType<? extends Monster> type, double x, double y, double z) {
        Monster worm = type.create(level);
        if (worm != null) {
            worm.moveTo(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
            level.addFreshEntity(worm);
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (!this.noPhysics) {
            super.customServerAiStep();
        }

        Player target = this.level().getNearestPlayer(this, 8.0);
        if (target != null && !target.getAbilities().invulnerable) {
            this.pointAtEntity(target);
            this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
            if (this.random.nextInt(10) == 1 && this.distanceTo(target) < 3.0f) {
                this.doHurtTarget(target);
            }
        }
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        if (!this.noPhysics) {
            return super.causeFallDamage(dist, mult, source);
        }
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!this.noPhysics) {
            super.checkFallDamage(y, onGround, state, pos);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("wormsSpawned", this.wormsSpawned);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.wormsSpawned = tag.getInt("wormsSpawned");
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(Items.NETHER_STAR);
        for (int i = 0; i < 6; i++) {
            this.spawnAtLocation(Items.ROTTEN_FLESH);
        }
        for (int i = 0; i < 6; i++) {
            this.spawnAtLocation(Items.STRING);
        }
        for (int i = 0; i < 16; i++) {
            this.spawnAtLocation(Items.SPIDER_EYE);
        }
        for (int i = 0; i < 5; i++) {
            this.spawnAtLocation(Items.DIAMOND);
        }
    }
}
