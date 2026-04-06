package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;

public class EntityGammaMetroid extends TamableAnimal {
    private static final int NO_STONE_FOUND = 99999;

    private int closestStoneDistSq = NO_STONE_FOUND;
    private int targetStoneX = 0;
    private int targetStoneY = 0;
    private int targetStoneZ = 0;

    public EntityGammaMetroid(EntityType<? extends EntityGammaMetroid> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2, Ingredient.of(Items.COD), false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        if (this.isTame()) return false;
        return !this.isPersistenceRequired();
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(5) == 0) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) <= 9.0) {
                    if (this.random.nextInt(4) == 0 || this.random.nextInt(5) == 1) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.25);
                }
            }
        }

        if ((this.random.nextInt(20) == 0 && this.getHealth() < this.getMaxHealth()
                || this.random.nextInt(100) == 0)
                && !this.isOrderedToSit()) {
            scanForStone();
        }
    }

    private void scanForStone() {
        this.closestStoneDistSq = NO_STONE_FOUND;
        this.targetStoneX = 0;
        this.targetStoneY = 0;
        this.targetStoneZ = 0;

        for (int i = 1; i < 6; i++) {
            int j = Math.min(i, 2);
            if (scanVolumeForStone((int) this.getX(), (int) this.getY() + 1, (int) this.getZ(), i, j, i)) break;
            if (i >= 4) i++;
        }

        if (this.closestStoneDistSq < NO_STONE_FOUND) {
            this.getNavigation().moveTo(this.targetStoneX, this.targetStoneY, this.targetStoneZ, 1.0);
            if (this.closestStoneDistSq < 12) {
                if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    this.level().setBlock(new BlockPos(this.targetStoneX, this.targetStoneY, this.targetStoneZ),
                            Blocks.AIR.defaultBlockState(), 2);
                }
                this.heal(1.0f);
                this.playSound(SoundEvents.PLAYER_BURP, 0.5f,
                        this.random.nextFloat() * 0.2f + 1.5f);
            }
        }
    }

    private boolean scanVolumeForStone(int originX, int originY, int originZ, int radiusX, int radiusY, int radiusZ) {
        int found = 0;
        for (int i = -radiusY; i <= radiusY; i++) {
            for (int j = -radiusZ; j <= radiusZ; j++) {
                int distSq = radiusX * radiusX + j * j + i * i;
                found += considerStoneAt(originX + radiusX, originY + i, originZ + j, distSq);
                found += considerStoneAt(originX - radiusX, originY + i, originZ + j, distSq);
            }
        }
        for (int i = -radiusX; i <= radiusX; i++) {
            for (int j = -radiusZ; j <= radiusZ; j++) {
                int distSq = radiusY * radiusY + j * j + i * i;
                found += considerStoneAt(originX + i, originY + radiusY, originZ + j, distSq);
                found += considerStoneAt(originX + i, originY - radiusY, originZ + j, distSq);
            }
        }
        for (int i = -radiusX; i <= radiusX; i++) {
            for (int j = -radiusY; j <= radiusY; j++) {
                int distSq = radiusZ * radiusZ + j * j + i * i;
                found += considerStoneAt(originX + i, originY + j, originZ + radiusZ, distSq);
                found += considerStoneAt(originX + i, originY + j, originZ - radiusZ, distSq);
            }
        }
        return found != 0;
    }

    private int considerStoneAt(int blockX, int blockY, int blockZ, int distSq) {
        BlockState state = this.level().getBlockState(new BlockPos(blockX, blockY, blockZ));
        if (state.is(Blocks.STONE) && distSq < this.closestStoneDistSq) {
            this.closestStoneDistSq = distSq;
            this.targetStoneX = blockX;
            this.targetStoneY = blockY;
            this.targetStoneZ = blockZ;
            return 1;
        }
        return 0;
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (this.isBaby()) return null;
        if (this.isTame()) return null;
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(10.0, 3.0, 10.0));
        entities.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityGammaMetroid) return false;
        if (target instanceof Monster) return false;
        if (this.isTame()) return false;
        if (target instanceof Player player) {
            return !player.getAbilities().invulnerable;
        }
        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (super.mobInteract(player, hand).consumesAction()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.COD) && this.distanceToSqr(player) < 25.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.getMaxHealth() - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.getMaxHealth() > this.getHealth()) {
                    this.heal(this.getMaxHealth() - this.getHealth());
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.is(Blocks.GLASS.asItem())
                && this.distanceToSqr(player) < 25.0
                && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.setOrderedToSit(false);
                this.setInSittingPose(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.is(Items.NAME_TAG)
                && this.distanceToSqr(player) < 16.0
                && this.isOwnedBy(player)) {
            this.setCustomName(stack.getHoverName());
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && this.isOwnedBy(player)
                && this.distanceToSqr(player) < 25.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(5) == 1) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "wtf_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        EntityGammaMetroid baby = new EntityGammaMetroid(ModEntities.ENTITY_GAMMA_METROID.get(), level);
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null) {
            baby.setOwnerUUID(ownerUUID);
            baby.setTame(true, true);
        }
        return baby;
    }
}
