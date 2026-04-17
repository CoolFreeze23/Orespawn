package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Leonopteryx — a Mining-Dimension sky tyrant. In 1.7.10 it was a
 * massive flying boss/mount: aggressive while wild, tameable with raw beef,
 * and a free flight platform for its owner once domesticated.
 *
 * <p>Implementation notes:</p>
 * <ul>
 *   <li>Flight is BlockPos-target driven (same pattern as Mothra) so the
 *       creature can hover, dive, and re-target without the vanilla
 *       PathNavigation pulling it into the ground every time it tries to
 *       sit on a non-existent path node.</li>
 *   <li>Tameability uses the standard {@link TamableAnimal} contract with
 *       a {@link ItemTags#MEAT} ingredient gate (any raw or cooked meat —
 *       the wiki specifies raw beef, but the meat tag is the modern
 *       1:1 mapping for "any flesh that a carnivore would accept").</li>
 *   <li>The Battle Axe and Kraken Repellent drops are handled by the
 *       {@code data/orespawn/loot_table/entities/leonopteryx.json} loot
 *       table, not hardcoded here, so server admins can swap drops
 *       without touching code.</li>
 * </ul>
 */
public class Leonopteryx extends TamableAnimal {

    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Leonopteryx.class, EntityDataSerializers.INT);

    private static final double WILD_HP        = 300.0;
    private static final double WILD_ATTACK    = 40.0;
    private static final double FLIGHT_SPEED   = 0.6;
    /** 1-in-3 dice on raw-beef offering — matches the 1.7.10 tame chance. */
    private static final int TAME_DICE = 3;

    private final Comparator<Entity> targetSorter;
    private BlockPos currentFlightTarget = null;
    private int hurtTimer = 0;
    private int wingSound = 0;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Leonopteryx"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS);

    public Leonopteryx(EntityType<? extends Leonopteryx> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
        this.setNoGravity(true);
        // Air-resistant move/look — flying creatures get treated like ambient
        // bats/birds rather than ground pathfinders. canDrownInFluidType is
        // not strictly required (we never enter water willingly) but we still
        // disable fall damage below.
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, WILD_HP)
                .add(Attributes.ATTACK_DAMAGE, WILD_ATTACK)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.FLYING_SPEED, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        // Tamed Leonopteryx politely follows its owner. The y-tolerance is
        // large because the bird hovers well above the player's head.
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.5, 12.0f, 4.0f));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int v) { this.entityData.set(DATA_ATTACKING, v); }

    @Override
    public boolean isFood(ItemStack stack) {
        // Wiki §13: Leonopteryx accepts raw beef. The MEAT item tag is the
        // canonical modern equivalent and includes raw_beef, raw_porkchop,
        // raw_chicken, raw_mutton, raw_rabbit — all carnivore food.
        return stack.is(ItemTags.MEAT);
    }

    @Override
    public boolean canBeLeashed() {
        return this.isTame();
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isTame() && !this.isPersistenceRequired();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            boolean ownedAndFood = this.isTame()
                    && this.isOwnedBy(player)
                    && (this.isFood(stack) || stack.isEmpty());
            return ownedAndFood ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    this.heal((float) Math.ceil(this.getMaxHealth() / 10.0f));
                    return InteractionResult.SUCCESS;
                }
                this.setOrderedToSit(!this.isOrderedToSit());
                this.setInSittingPose(this.isOrderedToSit());
                this.jumping = false;
                this.getNavigation().stop();
                this.setTarget(null);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (this.isFood(stack)) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            // 1-in-N dice mirrors 1.7.10 EntityWolf-style taming randomness;
            // unsuccessful offerings still consume the meat.
            if (this.random.nextInt(TAME_DICE) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.setInSittingPose(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = target.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hit && target instanceof LivingEntity le) {
            float angle = (float) Math.atan2(le.getZ() - this.getZ(), le.getX() - this.getX());
            le.push(Math.cos(angle) * 1.0, 0.4, Math.sin(angle) * 1.0);
            this.setAttacking(1);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        this.hurtTimer = 10;
        Entity attacker = source.getEntity();
        if (attacker instanceof Leonopteryx) return false;
        // Tamed Leonopteryx is invulnerable to its owner's attacks (no friendly
        // fire while flying as a mount). Same idiom as vanilla wolves.
        if (this.isTame() && this.isOwnedBy(attacker instanceof LivingEntity le ? le : null)) {
            return false;
        }
        boolean ret = super.hurt(source, amount);
        if (ret && attacker instanceof LivingEntity le && this.currentFlightTarget != null) {
            this.currentFlightTarget = new BlockPos(
                    (int) le.getX(),
                    (int) (le.getY() + 2),
                    (int) le.getZ());
        }
        return ret;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hurtTimer > 0) --this.hurtTimer;

        ++this.wingSound;
        if (this.wingSound > 24) {
            if (!this.level().isClientSide()) {
                this.playSound(SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings")), 1.0f, 0.85f);
            }
            this.wingSound = 0;
        }

        // Damp the y-velocity so the bird coasts smoothly rather than
        // free-falling between flap intervals.
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!this.isTame()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) return;

        // Only show a boss bar while the Leonopteryx is wild — domesticated
        // birds shouldn't keep haunting their owner with a red bar.
        if (!this.isTame()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (this.isOrderedToSit()) {
            this.getNavigation().stop();
            return;
        }

        if (this.currentFlightTarget == null
                || this.currentFlightTarget.distSqr(this.blockPosition()) < 9
                || this.random.nextInt(200) == 0) {
            int xdir = this.random.nextInt(24) + 6;
            int zdir = this.random.nextInt(24) + 6;
            if (this.random.nextInt(2) == 0) xdir = -xdir;
            if (this.random.nextInt(2) == 0) zdir = -zdir;
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX() + xdir,
                    (int) this.getY() + this.random.nextInt(9) - 3,
                    (int) this.getZ() + zdir);
        } else if (!this.isTame() && this.random.nextInt(8) == 0) {
            LivingEntity quarry = findSomethingToAttack();
            if (quarry != null) {
                this.setTarget(quarry);
                this.currentFlightTarget = new BlockPos(
                        (int) quarry.getX(),
                        (int) (quarry.getY() + 2),
                        (int) quarry.getZ());
                double meleeRadius = 4.0 + quarry.getBbWidth();
                if (this.distanceToSqr(quarry) < meleeRadius * meleeRadius) {
                    this.doHurtTarget(quarry);
                } else {
                    this.setAttacking(0);
                }
            } else {
                this.setAttacking(0);
            }
        }

        double toX = this.currentFlightTarget.getX() + 0.5 - this.getX();
        double toY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double toZ = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(
                motion.x + (Math.signum(toX) * FLIGHT_SPEED - motion.x) * 0.30,
                motion.y + (Math.signum(toY) * 0.6 - motion.y) * 0.20,
                motion.z + (Math.signum(toZ) * FLIGHT_SPEED - motion.z) * 0.30
        );
        float motionYaw = (float) (Math.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(motionYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 5.0f);
    }

    @Nullable
    private LivingEntity findSomethingToAttack() {
        AABB box = this.getBoundingBox().inflate(20.0, 12.0, 20.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, box);
        entities.sort(this.targetSorter);
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Leonopteryx) return false;
        if (this.isTame() && this.isOwnedBy(target)) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // No additional fields to restore — the synced attacking flag is
        // recomputed every tick from the AI state. Override left explicit so
        // future fields land in the right place.
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }
}
