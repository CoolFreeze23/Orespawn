package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import danger.orespawn.OreSpawnMod;

public class SpiderRobot extends Mob {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SpiderRobot.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private final float moveSpeed = 0.35f;
    private int playing = 0;
    private int rideTicker = 0;

    public SpiderRobot(EntityType<? extends SpiderRobot> type, Level level) {
        super(type, level);
        this.xpReward = 200;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int value) { this.entityData.set(DATA_ATTACKING, value); }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        if (this.getFirstPassenger() != null) return;
        super.customServerAiStep();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall") || source.getMsgId().equals("cactus")
                || source.getMsgId().equals("inFire") || source.getMsgId().equals("onFire")
                || source.getMsgId().equals("magic") || source.getMsgId().equals("starve")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.clearFire();
        if (!this.level().isClientSide() && this.getFirstPassenger() != null && this.getRandom().nextInt(15) == 0) {
            LivingEntity e = findSomethingToAttack();
            if (e != null) {
                double meleeRange = (12.0f + e.getBbWidth() / 2.0f);
                if (this.distanceToSqr(e) < meleeRange * meleeRange) {
                    this.setAttacking(1);
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        float f = 8.0f;
        float dx = (float) (f * Math.cos(Math.toRadians(this.getYRot() - 90.0f)));
        float dz = (float) (f * Math.sin(Math.toRadians(this.getYRot() - 90.0f)));
        if (this.level().isClientSide()) {
            if (this.getRandom().nextInt(8) == 0) {
                this.level().addParticle(ParticleTypes.FLAME, getX() + dx, getY() + 2.0, getZ() + dz, 0, 0, 0);
            }
            if (this.getRandom().nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, getX() + dx, getY() + 2.0, getZ() + dz, 0, 0, 0);
            }
        }
        if (this.playing > 0) --this.playing;
        if (this.getFirstPassenger() != null && this.playing == 0 && this.getRandom().nextInt(80) == 1) {
            this.playSound(SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspider")), 0.45f, 1.0f);
            this.playing = 125;
        }
        this.rideTicker += this.getRandom().nextInt(3);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity le) {
            double ks = 1.2;
            double inair = 0.15;
            float f3 = (float) Math.atan2(le.getZ() - this.getZ(), le.getX() - this.getX());
            boolean ret = le.hurt(this.damageSources().mobAttack(this), 50.0f);
            if (le.isRemoved() || le instanceof Player) inair *= 2.0;
            if (ret) le.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            return ret;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.IRON_INGOT) && this.distanceToSqr(player) < 25.0) {
            if (!this.level().isClientSide()) {
                float heal = this.getMaxHealth() - this.getHealth();
                if (heal > 100.0f) heal = 100.0f;
                if (heal > 0) this.heal(heal);
            }
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        if (this.getFirstPassenger() != null && this.getFirstPassenger() instanceof Player
                && this.getFirstPassenger() != player) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide() && this.getFirstPassenger() == null && this.distanceToSqr(player) < 16.0) {
            player.startRiding(this);
            this.playSound(SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "robotspidermount")), 0.65f, 1.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2.625;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {}

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(20.0, 12.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity e : entities) {
            if (isSuitableTarget(e)) return e;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof SpiderRobot) return false;
        if (target == this.getFirstPassenger()) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }
}
