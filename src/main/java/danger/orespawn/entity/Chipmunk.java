package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.entity.animal.Animal;
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
import danger.orespawn.entity.ai.MoveIndoorsGoal;

// orig Chipmunk.java:40-41 — extends EntityCannonFodder, which carries the shared
// hat-taming / activated-guard behavior a chipmunk shares with the other fodder pets.
public class Chipmunk extends EntityCannonFodder {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SCORPION_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_hit"));
    private static final SoundEvent SND_CRYO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death"));
    private static final int MAX_HEALTH = 5;
    private static final double MOVE_SPEED = 0.38;

    public Chipmunk(EntityType<? extends Chipmunk> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.6));
        // orig Chipmunk.java:56 — the tempt item is an APPLE (field_151034_e), matching
        // the apple taming food; wheat is only its untamed death drop.
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.APPLE), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Mob.class, 5.0f));
        this.goalSelector.addGoal(9, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        // orig Chipmunk.java:63 — EntityAIMoveIndoors in the same lowest-priority slot 11.
        this.goalSelector.addGoal(11, new MoveIndoorsGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
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

        if (this.random.nextInt(250) == 0) {
            this.heal(1.0f);
        }

        if (!this.level().isClientSide && this.random.nextInt(600) == 1) {
            BlockPos below = this.blockPosition().below();
            BlockState belowState = this.level().getBlockState(below);
            if ((belowState.is(Blocks.DIRT) || belowState.is(Blocks.FARMLAND))
                    && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                this.level().setBlock(below, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        super.customServerAiStep();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (super.mobInteract(player, hand).consumesAction()) {
            return InteractionResult.SUCCESS;
        }

        // orig Chipmunk.java:141 — taming food is an apple (1-in-2 chance).
        if (stack.is(Items.APPLE) && this.distanceToSqr(player) < 16.0) {
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

        // orig Chipmunk.java:172 — a dead bush releases (untames) the chipmunk.
        if (this.isTame() && stack.is(Blocks.DEAD_BUSH.asItem())
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_SCORPION_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_CRYO_DEATH;
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

    private int findBuddies() {
        return this.level().getEntitiesOfClass(Chipmunk.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(Chipmunk.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= 2;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        // Exposes the tame flag to the loot-table NBT predicate for the
        // tamed-only poppy drop (orig Chipmunk.java:231-242) — same convention
        // as Gazelle/Camarasaurus.
        tag.putBoolean("OreSpawnTamed", this.isTame());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        Chipmunk baby = new Chipmunk(ModEntities.CHIPMUNK.get(), level);
        UUID ownerUUID = this.getOwnerUUID();
        if (ownerUUID != null) {
            baby.setOwnerUUID(ownerUUID);
            baby.setTame(true, true);
        }
        return baby;
    }
}
