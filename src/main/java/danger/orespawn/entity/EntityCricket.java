package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityCricket extends Animal {
    private static final EntityDataAccessor<Integer> DATA_SINGING =
            SynchedEntityData.defineId(EntityCricket.class, EntityDataSerializers.INT);

    private int singing = 0;
    private int jumpCount = 0;

    public EntityCricket(EntityType<? extends EntityCricket> type, Level level) {
        super(type, level);
        this.xpReward = 1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 8, 1.0));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SINGING, 0);
    }

    public int getSinging() {
        return this.entityData.get(DATA_SINGING);
    }

    public void setSinging(int value) {
        this.entityData.set(DATA_SINGING, value);
    }

    private void jumpAround() {
        Vec3 motion = this.getDeltaMovement();
        double newY = motion.y + (0.55 + Math.abs(this.random.nextFloat() * 0.35f));
        this.setPos(this.getX(), this.getY() + 0.25, this.getZ());

        float f = 0.3f + Math.abs(this.random.nextFloat() * 0.25f);
        float d = (float) (this.random.nextFloat() * Math.PI * 2.0);
        double newX = motion.x + f * Math.sin(d);
        double newZ = motion.z + f * Math.cos(d);

        this.setDeltaMovement(newX, newY, newZ);
        this.hasImpulse = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.singing != 0) {
                --this.singing;
                if (this.singing <= 0) {
                    this.setSinging(0);
                }
            }
            if (this.jumpCount > 0) {
                --this.jumpCount;
            }
            if (this.jumpCount == 0 && this.random.nextInt(50) == 1) {
                this.jumpAround();
                this.jumpCount = 50;
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (!this.level().isClientSide) {
            if (this.random.nextInt(2) == 0) return null;
            this.singing = 40;
            this.setSinging(this.singing);
        }
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cricket"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.7f;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
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
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 30.0) return false;
        return findBuddies() <= 5;
    }

    private int findBuddies() {
        return this.level().getEntitiesOfClass(EntityCricket.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }
}
