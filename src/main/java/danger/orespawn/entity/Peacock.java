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
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;

public class Peacock extends Animal {
    private static final float MOVE_SPEED = 0.38f;
    private static final int MAX_HEALTH = 15;
    private int myBlink = 20;
    private int blinkcount = 0;
    private int blinker = 0;

    public Peacock(EntityType<? extends Peacock> type, Level level) {
        super(type, level);
        this.xpReward = 8;
        this.myBlink = 20 + this.random.nextInt(50);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 12.0f, 1.2, 1.6));
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    public int getBlink() {
        return this.blinker;
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        super.tick();
        ++this.blinkcount;
        if (this.blinkcount > this.myBlink) {
            this.blinkcount = 0;
            if (this.blinker > 0) {
                this.blinker = 0;
                this.myBlink = 50 + this.getRandom().nextInt(300);
            } else {
                this.blinker = 1;
                this.myBlink = 25 + this.getRandom().nextInt(100);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(8) != 1) return null;
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacocklive"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacockhit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "peacockdead"));
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    private int findBuddies() {
        return this.level().getEntitiesOfClass(Peacock.class,
                this.getBoundingBox().inflate(16.0, 10.0, 16.0)).size();
    }

    /**
     * orig Peacock.java:181-200 — 1-in-200 revenge clear; nothing on peaceful;
     * 1-in-10 hunt the nearest visible Termite within 10/2/10 (melee 6.0 within
     * 2 blocks, else path at 1.2); 1-in-5000 lay 1-3 Spawn Peacock eggs.
     */
    @Override
    protected void customServerAiStep() {
        if (this.random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
        if (this.level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return;
        }
        if (this.random.nextInt(10) == 1) {
            net.minecraft.world.entity.LivingEntity prey = findSomethingToAttack();
            if (prey != null) {
                if (this.distanceToSqr(prey) < 4.0) {
                    // orig :166-169 — flat 6.0 mob-attack damage
                    prey.hurt(this.damageSources().mobAttack(this), 6.0f);
                } else {
                    this.getNavigation().moveTo(prey, 1.2);
                }
            }
        }
        if (this.random.nextInt(5000) == 1) {
            // orig :171-179,197-199 — 1-3 eggs at ±0-1 x/z, y+1
            ItemStack eggs = new ItemStack(ModItems.PEACOCK_SPAWN_EGG.get(), 1 + this.random.nextInt(3));
            net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                    this.level(),
                    this.getX() + this.random.nextInt(2) - this.random.nextInt(2),
                    this.getY() + 1.0,
                    this.getZ() + this.random.nextInt(2) - this.random.nextInt(2),
                    eggs);
            this.level().addFreshEntity(drop);
        }
    }

    /**
     * orig Peacock.java:202-237 — nearest living, visible Termite within
     * 10/2/10; nothing when PlayNicely is enabled.
     */
    @Nullable
    private net.minecraft.world.entity.LivingEntity findSomethingToAttack() {
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) {
            return null;
        }
        return this.level().getEntitiesOfClass(EntityTermite.class,
                        this.getBoundingBox().inflate(10.0, 2.0, 10.0),
                        t -> t.isAlive() && this.getSensing().hasLineOfSight(t))
                .stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get()); // orig Peacock.java:259-261 breeding item is MyCrystalApple
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new Peacock(ModEntities.PEACOCK.get(), level);
    }

    /** orig Peacock.java:101-119 — clear air above; first half of the day only; 50<=y<=100; at most 2 buddies within 16/10/16 (restores the never-called findBuddies()). */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (!OriginalSpawnGates.airBox(this, level, -1, 0, 1, 2, -1, 0)) return false;
        if (level.dayTime() % 24000L > 12000L) return false;
        if (this.getY() < 50.0 || this.getY() > 100.0) return false;
        return OriginalSpawnGates.countBuddies(this, level, Peacock.class, 16.0, 10.0, 16.0) <= 2;
    }
}
