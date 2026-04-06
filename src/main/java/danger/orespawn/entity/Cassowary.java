package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.ModItems;

public class Cassowary extends Animal {
    private final float moveSpeed = 0.25f;

    public Cassowary(EntityType<? extends Cassowary> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Mob.class, 12.0f));
        this.goalSelector.addGoal(6, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    @Override
    protected void customServerAiStep() {
        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }
        super.customServerAiStep();
    }

    @Override
    protected SoundEvent getAmbientSound() {
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
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = this.random.nextInt(3) + 2;
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(new ItemStack(Items.FEATHER, 1));
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Cassowary((EntityType<? extends Cassowary>) this.getType(), level);
    }
}
