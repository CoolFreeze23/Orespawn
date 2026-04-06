package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import danger.orespawn.ModItems;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import danger.orespawn.OreSpawnMod;

public class Cephadrome extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.25f;
    private int hurtTimer = 0;
    private int wasfed = 0;
    private int shouldattack = 0;
    private int hitByPlayer = 0;
    private int badmood = 0;

    private static final float MELEE_DAMAGE = 70.0f;
    private static final double KNOCKBACK_HORIZONTAL = 2.5;
    private static final double KNOCKBACK_VERTICAL = 0.35;
    private static final double PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER = 2.0;
    private static final float LOW_HEALTH_FRACTION_FOR_PLAYER_AGGRO = 9.0f / 10.0f;

    public Cephadrome(EntityType<? extends Cephadrome> type, Level level) {
        super(type, level);
        this.xpReward = 200;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 9.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 70.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getActivity() != 1 && this.random.nextInt(6) == 1) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
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
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        boolean hurtApplied = target.hurt(this.damageSources().mobAttack(this), MELEE_DAMAGE);
        double verticalKnockback = KNOCKBACK_VERTICAL;
        float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
        if (target.isRemoved() || target instanceof Player) {
            verticalKnockback *= PLAYER_OR_REMOVED_VERTICAL_MULTIPLIER;
        }
        target.push(
                Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                verticalKnockback,
                Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
        return hurtApplied;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        if (source.getMsgId().equals("cactus")) return false;

        boolean hurtApplied = super.hurt(source, amount);
        this.hurtTimer = 25;
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            this.setTarget(livingAttacker);
            this.getNavigation().moveTo(livingAttacker, 1.2);
        }
        if (attacker instanceof Player && this.getHealth() < this.getMaxHealth() * LOW_HEALTH_FRACTION_FOR_PLAYER_AGGRO) {
            this.hitByPlayer = 1;
        }
        return hurtApplied;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.hurtTimer > 0) --this.hurtTimer;

        if (this.random.nextInt(100) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0f);
        }

        super.customServerAiStep();

        if (this.random.nextInt(7) == 1) {
            LivingEntity target = this.getTarget();
            if (target != null && !target.isAlive()) {
                this.setTarget(null);
                target = null;
            }
            if (target == null) {
                target = this.findSomethingToAttack();
            }
            if (target != null) {
                this.getNavigation().moveTo(target, 1.7);
                this.lookAt(target, 10.0f, 10.0f);
                this.setAttacking(1);
                double meleeRange = 6.0 + target.getBbWidth() / 2.0;
                if (this.distanceToSqr(target) < meleeRange * meleeRange) {
                    this.doHurtTarget(target);
                }
            } else if (this.getAttacking() != 0) {
                this.setAttacking(0);
            }
        }
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Cephadrome) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Mothra || target instanceof EntityLeon || target instanceof EntityGammaMetroid || target instanceof WaterDragon) return false;
        if (target instanceof Player player) {
            if (player.getAbilities().invulnerable) return false;
            return this.hitByPlayer != 0 || this.badmood != 0 || this.shouldattack > 0;
        }
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(16.0, 20.0, 16.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        targets.sort(this.targetSorter::compare);
        for (LivingEntity target : targets) {
            if (this.isSuitableTarget(target)) return target;
        }
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int uraniumCount = 4 + this.random.nextInt(6);
        for (int i = 0; i < uraniumCount; i++) {
            this.spawnAtLocation(new ItemStack(ModItems.URANIUM_NUGGET.get(), 1));
        }
        int titaniumCount = 4 + this.random.nextInt(6);
        for (int i = 0; i < titaniumCount; i++) {
            this.spawnAtLocation(new ItemStack(ModItems.TITANIUM_NUGGET.get(), 1));
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if ((stack.is(Items.BEEF) || stack.is(Items.FEATHER) || stack.is(Items.COOKED_BEEF))
                && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide) {
                this.heal(this.getMaxHealth() - this.getHealth());
            }
            this.wasfed = 1;
            this.shouldattack = 0;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ATTACKING, (int) (byte) value);
    }

    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    public void setActivity(int value) {
        if (this.level() != null && this.level().isClientSide) return;
        this.entityData.set(DATA_ACTIVITY, (int) (byte) value);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CephaWasFed", this.wasfed);
        tag.putInt("CephaAttacking", this.getAttacking());
        tag.putInt("CephaActivity", this.getActivity());
        tag.putInt("CephaHitByPlayer", this.hitByPlayer);
        tag.putInt("CephaBadMood", this.badmood);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.wasfed = tag.getInt("CephaWasFed");
        this.hitByPlayer = tag.getInt("CephaHitByPlayer");
        this.badmood = tag.getInt("CephaBadMood");
        this.setAttacking(tag.getInt("CephaAttacking"));
        this.setActivity(tag.getInt("CephaActivity"));
    }
}
