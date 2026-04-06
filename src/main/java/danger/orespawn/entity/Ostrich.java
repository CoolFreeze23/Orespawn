package danger.orespawn.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;

public class Ostrich extends TamableAnimal {
    public Ostrich(EntityType<? extends Ostrich> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setOrderedToSit(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.9));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.WHEAT), false));
        this.goalSelector.addGoal(5, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Mob.class, 5.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.WHEAT) && this.distanceToSqr(player) < 16.0) {
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

        if (stack.isEmpty() && this.distanceToSqr(player) < 16.0) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
                this.setOrderedToSit(false);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.random.nextInt(200) == 1) this.setTarget(null);
        if (this.random.nextInt(250) == 0) this.heal(1.0f);
        if (this.getFirstPassenger() != null) return;
        super.customServerAiStep();
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
    public boolean isFood(ItemStack stack) { return stack.is(Items.WHEAT); }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isBaby()) return false;
        if (this.getFirstPassenger() != null) return false;
        if (this.isPersistenceRequired()) return false;
        return !this.isTame();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!level.canSeeSky(this.blockPosition())) return false;
        if (this.random.nextInt(4) != 1) return false;
        List<Ostrich> nearby = level.getEntitiesOfClass(Ostrich.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        return nearby.isEmpty();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Ostrich(ModEntities.OSTRICH.get(), this.level());
    }
}
