package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;

public class VelocityRaptor extends TamableAnimal {
    private int closestPlantDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    public VelocityRaptor(EntityType<? extends VelocityRaptor> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.5, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, Ingredient.of(Items.APPLE), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, Math.min(amount, 10.0f));
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        float dmg = (float) Math.ceil(dist - 3.0f);
        if (dmg > 0.0f) {
            dmg = Math.min(dmg, 2.0f);
            this.hurt(this.damageSources().fall(), dmg);
            return true;
        }
        return false;
    }

    private boolean isPlant(BlockState state) {
        return state.is(Blocks.DANDELION) || state.is(Blocks.POPPY) ||
                state.is(Blocks.RED_TULIP) || state.is(Blocks.OAK_LEAVES) ||
                state.is(Blocks.HAY_BLOCK);
    }

    private boolean scanForPlants(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; i++) {
            for (int j = -dz; j <= dz; j++) {
                BlockState state = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (isPlant(state)) {
                    int distSq = dx * dx + j * j + i * i;
                    if (distSq < this.closestPlantDistance) { this.closestPlantDistance = distSq; this.targetX = x + dx; this.targetY = y + i; this.targetZ = z + j; found++; }
                }
                state = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (isPlant(state)) {
                    int distSq = dx * dx + j * j + i * i;
                    if (distSq < this.closestPlantDistance) { this.closestPlantDistance = distSq; this.targetX = x - dx; this.targetY = y + i; this.targetZ = z + j; found++; }
                }
            }
        }
        return found != 0;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) return;
        if (this.random.nextInt(200) == 1) this.setTarget(null);

        if (!this.isOrderedToSit() && (this.random.nextInt(20) == 0 && this.getHealth() < this.getMaxHealth() || this.random.nextInt(250) == 0)) {
            this.closestPlantDistance = 99999; this.targetX = 0; this.targetY = 0; this.targetZ = 0;
            for (int i = 1; i < 10; i++) {
                int j = Math.min(i, 2);
                if (this.scanForPlants((int) this.getX(), (int) this.getY() + 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) i++;
            }
            if (this.closestPlantDistance < 99999) {
                this.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, 1.0);
                if (this.closestPlantDistance < 12) {
                    BlockPos targetPos = new BlockPos(this.targetX, this.targetY, this.targetZ);
                    if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        this.level().setBlock(targetPos, Blocks.AIR.defaultBlockState(), 2);
                    }
                    this.heal(2.0f);
                }
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.APPLE) && this.distanceToSqr(player) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 0) {
                        this.setTame(true, true);
                        this.setOwnerUUID(player.getUUID());
                        this.heal(this.getMaxHealth() - this.getHealth());
                    }
                }
            } else if (this.isOwnedBy(player)) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (this.isTame() && this.isOwnedBy(player) && stack.isEmpty()
                && this.distanceToSqr(player) < 16.0 && !player.isShiftKeyDown()
                && this.getPassengers().isEmpty()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player) && this.distanceToSqr(player) < 16.0 && player.isShiftKeyDown()) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty()) {
            Entity first = this.getPassengers().get(0);
            if (first instanceof Player player && this.isOwnedBy(player)) {
                return player;
            }
        }
        return super.getControllingPassenger();
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (!this.hasPassenger(passenger)) return;
        callback.accept(passenger, this.getX(), this.getY() + this.getBbHeight() * 0.95, this.getZ());
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        this.setYRot(player.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(player.getXRot() * 0.5F);
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.yBodyRot;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float strafe = player.xxa * 0.5F;
        float forward = player.zza;
        if (forward <= 0.0F) forward *= 0.25F;
        return new Vec3(strafe, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.6F;
    }

    @Override
    public boolean isPushable() {
        return this.getPassengers().isEmpty();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    }

    @Override
    protected float getSoundVolume() { return 0.4f; }
    @Override
    public boolean isFood(ItemStack stack) { return stack.is(Items.APPLE); }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isBaby()) return false;
        if (this.isTame()) return false;
        return !this.isPersistenceRequired();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.canSeeSky(this.blockPosition());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new VelocityRaptor(ModEntities.VELOCITY_RAPTOR.get(), this.level());
    }
}
