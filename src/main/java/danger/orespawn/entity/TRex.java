package danger.orespawn.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;

public class TRex extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TRex.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.38f;

    public TRex(EntityType<? extends TRex> type, Level level) {
        super(type, level);
        this.xpReward = 150;
    }

    // AI mirrors 1.7.10 TRex#func_70619_bc: random-cadence swings with the
    // same outer/inner nextInt dice. Revenge target is now handled by the
    // standard HurtByTargetGoal; proactive target acquisition is pushed onto
    // NearestAttackableTargetGoal with a wide follow range (40) to match the
    // legacy 20×6×20 AABB scan.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DinosaurMeleeAttackGoal(this, this::setAttacking,
                DinosaurMeleeAttackGoal.Presets.trex()));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    public int mygetMaxHealth() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0) {
            return SoundEvents.RAVAGER_ROAR;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // Bone drops are kept here (independent of the loot-table tooth/beef
    // pools) because 1.7.10 dropped a bone on every death regardless of
    // recently-hit state. The loot table handles the trex_tooth weighted
    // pull (60% × 1, 30% × 2, 10% × 3 — see trex.json) plus the name tag,
    // beef, gold/iron nuggets, XP bottle, and rare diamond pity drop. The
    // weighted tooth pool exists so a Big Bertha craft (which needs three
    // bertha components, each gated behind a tooth) is achievable in roughly
    // 4-5 T-Rex kills on average.
    private void dropItemRand(ItemStack stack) {
        double ox = this.getX() + this.getRandom().nextInt(4) - this.getRandom().nextInt(4);
        double oy = this.getY() + 1.0;
        double oz = this.getZ() + this.getRandom().nextInt(4) - this.getRandom().nextInt(4);
        ItemEntity itemEntity = new ItemEntity(this.level(), ox, oy, oz, stack);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        dropItemRand(new ItemStack(Items.BONE, 1));
    }

    // 1.7.10 knockback: horizontal push of 1.2 + vertical bump of 0.1
    // (doubled if hitting a player or removed entity).
    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 1.2;
                double upwardKnockback = 0.1;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    upwardKnockback *= 2.0;
                }
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Cactus immunity preserved from 1.7.10 (T-Rex leather is too thick).
        if (source.getMsgId().equals("cactus")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }
}
