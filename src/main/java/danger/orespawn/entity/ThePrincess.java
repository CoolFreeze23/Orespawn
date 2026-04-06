package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import danger.orespawn.OreSpawnMod;

public class ThePrincess extends TamableAnimal {
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FIRE =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.3f;
    private int head1ext = 0, head2ext = 0, head3ext = 0;
    private int head1dir = 1, head2dir = 1, head3dir = 1;
    private int okToGrow = 0;
    private int killCount = 0;
    private int fedCount = 0;
    private int dayCount = 0;

    public ThePrincess(EntityType<? extends ThePrincess> type, Level level) {
        super(type, level);
        this.xpReward = 50;
        this.noPhysics = false;
        this.setOrderedToSit(false);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.15, 12.0f, 2.0f));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_FIRE, 1);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) { return false; }

    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int value) { this.entityData.set(DATA_ACTIVITY, value); }
    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }
    public int getHead1Ext() { return this.head1ext; }
    public int getHead2Ext() { return this.head2ext; }
    public int getHead3Ext() { return this.head3ext; }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = this.getActivity() == 2;

        int i;
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head1dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head2dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        if (this.random.nextInt(10) == 1) { i = this.random.nextInt(3); this.head3dir = i == 0 ? 2 : i == 1 ? -2 : 0; }
        this.head1ext = Math.max(0, Math.min(60, this.head1ext + this.head1dir));
        this.head2ext = Math.max(0, Math.min(60, this.head2ext + this.head2dir));
        this.head3ext = Math.max(0, Math.min(60, this.head3ext + this.head3dir));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")) return false;
        boolean ret = super.hurt(source, amount);
        this.setOrderedToSit(false);
        this.setActivity(2);
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.random.nextInt(200) == 1) this.setTarget(null);
        if (this.getActivity() != 2) super.customServerAiStep();
        if (this.random.nextInt(200) == 1 && this.getHealth() < this.getMaxHealth()) this.heal(1.0f);

        if (!this.isTame()) {
            Player nearestPlayer = this.level().getNearestPlayer(this, 10.0);
            if (nearestPlayer != null) {
                this.tame(nearestPlayer);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.getMaxHealth() - this.getHealth());
            }
        }

        if (!this.isOrderedToSit() && this.random.nextInt(7) == 1) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.setActivity(2);
                this.setAttacking(1);
                double range = (3.0 + target.getBbWidth() / 2.0);
                if (this.distanceToSqr(target) < range * range) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    private LivingEntity findSomethingToAttack() {
        AABB box = this.getBoundingBox().inflate(12.0, 6.0, 12.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box);
        targets.sort(Comparator.comparingDouble(this::distanceToSqr));
        for (LivingEntity t : targets) {
            if (t == this || !t.isAlive()) continue;
            if (!this.getSensing().hasLineOfSight(t)) continue;
            if (t instanceof Monster) return t;
        }
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD) && this.distanceToSqr(player) < 16.0 && this.isTame() && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) { this.heal(20.0f); ++this.fedCount; this.level().broadcastEntityEvent(this, (byte)7); }
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isTame() && this.isOwnedBy(player) && this.distanceToSqr(player) < 16.0) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.setInSittingPose(this.isOrderedToSit());
            if (this.isOrderedToSit()) this.setActivity(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override protected SoundEvent getAmbientSound() {
        if (this.isOrderedToSit() || this.getAttacking() == 0) return null;
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "roar"));
    }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt")); }
    @Override protected SoundEvent getDeathSound() { return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "cryo_death")); }
    @Override protected float getSoundVolume() { return 0.6f; }
    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override public boolean isFood(ItemStack s) { return s.is(Items.BEEF); }
    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel l, AgeableMob o) { return null; }

    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("PrincessActivity", getActivity()); tag.putInt("PrincessFire", entityData.get(DATA_FIRE)); tag.putInt("PrincessGrow", okToGrow); tag.putInt("PrincessKill", killCount); tag.putInt("PrincessFed", fedCount); tag.putInt("PrincessDay", dayCount); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); setActivity(tag.getInt("PrincessActivity")); entityData.set(DATA_FIRE, tag.getInt("PrincessFire")); okToGrow = tag.getInt("PrincessGrow"); killCount = tag.getInt("PrincessKill"); fedCount = tag.getInt("PrincessFed"); dayCount = tag.getInt("PrincessDay"); }
}
