package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Frog extends Animal {
    private static final EntityDataAccessor<Integer> DATA_SINGING =
            SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 8;
    private static final double MOVE_SPEED = 0.1;

    private int singing = 0;
    private int jumpCount = 0;

    public Frog(EntityType<? extends Frog> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new MyEntityAIWander(this, 1.0f));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
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

    @Override
    public boolean isPushedByFluid() {
        return true;
    }

    private void jumpAround() {
        Vec3 motion = this.getDeltaMovement();
        double newMotionY = motion.y + (0.75 + Math.abs(this.random.nextFloat() * 0.55f));
        this.setPos(this.getX(), this.getY() + 0.35, this.getZ());

        float f = 0.7f + Math.abs(this.random.nextFloat() * 0.75f);
        float d = (float) Math.toRadians(this.getYRot());
        double newMotionX = motion.x - (double) f * Math.sin(d);
        double newMotionZ = motion.z + (double) f * Math.cos(d);

        this.setDeltaMovement(newMotionX, newMotionY, newMotionZ);
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
            if (this.jumpCount == 0 && this.random.nextInt(70) == 1) {
                this.jumpAround();
                this.jumpCount = 50;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            Level world = player.level();
            this.discard();
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.GENERIC_EXPLODE.value(), player.getSoundSource(),
                    1.0f, world.random.nextFloat() * 0.2f + 0.9f);

            if (!world.isClientSide) {
                // TODO: When Boyfriend/Girlfriend entities are ported, spawn prince/princess here
                // Original logic: 50% chance Boyfriend (prince), 50% chance Girlfriend (princess)
            }

            if (world.isClientSide) {
                for (int i = 0; i < 16; ++i) {
                    world.addParticle(ParticleTypes.SMOKE,
                            this.getX() + world.random.nextFloat() - world.random.nextFloat(),
                            this.getY() + world.random.nextFloat(),
                            this.getZ() + world.random.nextFloat() - world.random.nextFloat(),
                            0.0, 0.0, 0.0);
                    world.addParticle(ParticleTypes.EXPLOSION,
                            this.getX() + world.random.nextFloat() - world.random.nextFloat(),
                            this.getY() + world.random.nextFloat(),
                            this.getZ() + world.random.nextFloat() - world.random.nextFloat(),
                            0.0, 0.0, 0.0);
                    world.addParticle(ParticleTypes.POOF,
                            this.getX() + world.random.nextFloat() - world.random.nextFloat(),
                            this.getY() + world.random.nextFloat(),
                            this.getZ() + world.random.nextFloat() - world.random.nextFloat(),
                            0.0, 0.0, 0.0);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (!this.level().isClientSide) {
            if (this.random.nextInt(2) == 0) {
                return null;
            }
            this.singing = 35;
            this.setSinging(this.singing);
        }
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "frog"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "big_splat"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.7f;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Silent footsteps
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = target.hurt(this.damageSources().mobAttack(this), 3.0f);
        if (!target.isAlive()) {
            this.heal(1.0f);
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!this.level().isClientSide && this.jumpCount <= 0) {
            this.jumpAround();
            this.jumpCount = 25;
        }
        return result;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(12) == 0
                && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity target = findInsectTarget();
            if (target != null) {
                this.getNavigation().moveTo(target, 1.25);
                if (this.distanceToSqr(target) < 6.0) {
                    this.doHurtTarget(target);
                }
            }
        }
    }

    @Nullable
    private LivingEntity findInsectTarget() {
        // TODO: When insect entities (Ant, Butterfly, Cricket, Mosquito, Firefly, WormSmall) 
        // are ported, add target filtering here
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(8.0, 3.0, 8.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        return entities.stream()
                .filter(this::isInsectTarget)
                .findFirst()
                .orElse(null);
    }

    private boolean isInsectTarget(LivingEntity entity) {
        if (entity == this) return false;
        if (!entity.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(entity)) return false;
        // TODO: Check for specific insect entity types when ported
        // Original targets: EntityAnt, EntityButterfly, Cricket, EntityMosquito, Firefly, WormSmall
        return false;
    }

    private int findBuddies() {
        return this.level().getEntitiesOfClass(Frog.class,
                this.getBoundingBox().inflate(20.0, 8.0, 20.0)).size();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        long dayTime = this.level().getDayTime() % 24000L;
        if (dayTime >= 13000L) return false;
        return this.findBuddies() <= 5;
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
}
