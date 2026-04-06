package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
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
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Gazelle extends TamableAnimal {
    private static final int MAX_HEALTH = 15;
    private static final double MOVE_SPEED = 0.3;
    private static final int NO_FOOD_FOUND_SENTINEL = 99999;
    private static final int GRAZE_DISTANCE_THRESHOLD_SQ = 12;

    private int closestFoodDistanceSq = NO_FOOD_FOUND_SENTINEL;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public Gazelle(EntityType<? extends Gazelle> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.7));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 12.0f, 1.0, 2.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    private boolean isGrazeBlock(Block block) {
        return block == Blocks.CARROTS
                || block == Blocks.POTATOES
                || block == Blocks.SHORT_GRASS
                || block == Blocks.TALL_GRASS;
    }

    private boolean scanForFood(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkFoodAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
                found += checkFoodAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkFoodAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
                found += checkFoodAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                found += checkFoodAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
                found += checkFoodAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
            }
        }
        return found != 0;
    }

    private int checkFoodAt(int x, int y, int z, int dist) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = this.level().getBlockState(pos).getBlock();
        if (isGrazeBlock(block) && dist < this.closestFoodDistanceSq) {
            this.closestFoodDistanceSq = dist;
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            return 1;
        }
        return 0;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        float damage = fallDistance - 3.0f;
        if (damage > 0.0f) {
            if (damage > 2.0f) damage = 2.0f;
            this.hurt(this.damageSources().fall(), damage);
        }
        return false;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (!this.isOrderedToSit()) {
            boolean needsFood = this.random.nextInt(30) == 0
                    && (int) this.getHealth() < MAX_HEALTH;
            boolean randomGraze = this.random.nextInt(750) == 1;

            if (needsFood || randomGraze) {
                this.closestFoodDistanceSq = NO_FOOD_FOUND_SENTINEL;
                this.targetX = 0;
                this.targetY = 0;
                this.targetZ = 0;
                for (int i = 1; i < 11; ++i) {
                    int j = Math.min(i, 2);
                    if (this.scanForFood(
                            (int) this.getX(), (int) this.getY() + 1, (int) this.getZ(),
                            i, j, i)) {
                        break;
                    }
                    if (i >= 6) ++i;
                }
                if (this.closestFoodDistanceSq < NO_FOOD_FOUND_SENTINEL) {
                    this.getNavigation().moveTo(this.targetX, this.targetY, this.targetZ, 1.0);
                    if (this.closestFoodDistanceSq < GRAZE_DISTANCE_THRESHOLD_SQ) {
                        if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                            this.level().setBlock(
                                    new BlockPos(this.targetX, this.targetY, this.targetZ),
                                    Blocks.AIR.defaultBlockState(), 2);
                        }
                        this.heal(1.0f);
                        this.playSound(SoundEvents.PLAYER_BURP, 1.0f,
                                this.random.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }

            if (this.random.nextInt(250) == 1) {
                Gazelle buddy = this.findBuddy();
                if (buddy != null) {
                    this.getNavigation().moveTo(buddy.getX(), buddy.getY(), buddy.getZ(), 0.5);
                }
            }
        }

        if (this.random.nextInt(250) == 0) {
            this.heal(1.0f);
        }

        super.customServerAiStep();
    }

    @Nullable
    private Gazelle findBuddy() {
        List<Gazelle> buddies = this.level().getEntitiesOfClass(Gazelle.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        buddies.sort(Comparator.comparingDouble(this::distanceToSqr));
        return buddies.stream()
                .filter(otherGazelle -> otherGazelle != this)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float adjusted = amount;
        if (this.isTame() && adjusted > 10.0f) {
            adjusted = 10.0f;
        }
        return super.hurt(source, adjusted);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (super.mobInteract(player, hand).consumesAction()) {
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.WHEAT) && this.distanceToSqr(player) < 16.0) {
            if (!this.isTame()) {
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(2) == 0) {
                        this.tame(player);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(MAX_HEALTH - this.getHealth());
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (MAX_HEALTH > this.getHealth()) {
                    this.heal(MAX_HEALTH - this.getHealth());
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame() && stack.is(Blocks.GLASS.asItem())
                && this.distanceToSqr(player) < 16.0
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
                && this.distanceToSqr(player) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.getY() > 100.0) return false;
        BlockState below = this.level().getBlockState(this.blockPosition().below());
        return below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SHORT_GRASS);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby()
                ? (this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f
                : (this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        Gazelle baby = new Gazelle(ModEntities.GAZELLE.get(), level);
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null) {
            baby.setOwnerUUID(ownerUUID);
            baby.setTame(true, true);
        }
        return baby;
    }
}
