package danger.orespawn.entity;

import danger.orespawn.entity.ai.AmbientFlightGoal;
import danger.orespawn.entity.ai.ButterflyIslandsHuntGoal;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Ambient butterfly mob with random flight AI and 4 visual variants.
 * Right-clicking teleports the player to the Chaos dimension (DimensionID6
 * in the original 1.7.10 mod), or back to the Overworld if already there.
 * Unlike ants, Butterfly extends AmbientCreature so it has its own
 * mobInteract implementation rather than inheriting from EntityAnt.
 */
public class EntityButterfly extends AmbientCreature {
    private static final ResourceKey<Level> CHAOS = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("orespawn", "chaos"));
    private static final int TELEPORT_COOLDOWN = 80;

    private static final EntityDataAccessor<Integer> BUTTERFLY_TYPE =
            SynchedEntityData.defineId(EntityButterfly.class, EntityDataSerializers.INT);

    public EntityButterfly(EntityType<? extends EntityButterfly> type, Level level) {
        super(type, level);
        this.setRandomButterflyType();
    }

    /**
     * Registers the post-1.7.10 Goal-based AI. In 1.7.10 the flight was
     * inlined into {@code customServerAiStep}; we now use
     * {@link AmbientFlightGoal} (butterfly preset) so flight can be
     * interrupted by higher-priority goals in the future (e.g. panic,
     * breed, or the dimension-teleport interaction handler) without
     * the main-thread overhead of a hand-rolled lerp per tick.
     *
     * <p>Subclasses ({@code EntityLunaMoth}) override this method and
     * replace the base flight goal with their own specialisation.
     */
    @Override
    protected void registerGoals() {
        // orig EntityButterfly.java:145-181 — the butterfly preset of AmbientFlightGoal plus the Islands-dimension
        // vampire hunt 1.7.10 ran in the else-branch of its retarget test (:161-169): ButterflyIslandsHuntGoal is
        // that goal with the hunt in the same place. Subclasses inherit this registration as they inherited orig's
        // updateAITasks through super (Mothra.java:169); the Luna Moth registers its own flight goal, a subclass of this one
        // since ENT-S-141 (orig EntityLunaMoth.java:122's super.updateAITasks()). ENT-S-117.
        this.goalSelector.addGoal(8, new ButterflyIslandsHuntGoal(this));
    }

    private void setRandomButterflyType() {
        this.entityData.set(BUTTERFLY_TYPE, this.random.nextInt(4));
    }

    public int getButterflyType() {
        return this.entityData.get(BUTTERFLY_TYPE);
    }

    public void setButterflyType(int type) {
        this.entityData.set(BUTTERFLY_TYPE, type);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BUTTERFLY_TYPE, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 mot = this.getDeltaMovement();
        this.setDeltaMovement(mot.x, mot.y * 0.6, mot.z);
    }

    // Flight logic has moved out of customServerAiStep() and into
    // AmbientFlightGoal (registered above). The legacy inlined version
    // lived here in 1.7.10 but could not coexist with vanilla Goals.

    /**
     * Teleport to Chaos on empty-hand right-click. Uses the vanilla
     * {@link DimensionTransition} pipeline for async chunk loading.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (!player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if (player.isOnPortalCooldown()) return InteractionResult.PASS;

        ResourceKey<Level> destination = this.level().dimension().equals(CHAOS)
                ? Level.OVERWORLD
                : CHAOS;

        ServerLevel destLevel = serverPlayer.server.getLevel(destination);
        if (destLevel == null) return InteractionResult.FAIL;

        serverPlayer.setPortalCooldown(TELEPORT_COOLDOWN);

        double x = serverPlayer.getX();
        double z = serverPlayer.getZ();
        // Chaos floats over open void, so the landing hunt may wander off the
        // departure column exactly like the original teleporter's retry loop.
        BlockPos landing = EntityAnt.findSafeSpot(destLevel, BlockPos.containing(x, 0, z));

        DimensionTransition transition = new DimensionTransition(
                destLevel,
                new Vec3(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5),
                Vec3.ZERO,
                serverPlayer.getYRot(),
                serverPlayer.getXRot(),
                e -> e.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f)
        );
        serverPlayer.changeDimension(transition);
        return InteractionResult.SUCCESS;
    }

    /**
     * orig EntityButterfly.java:183-192 {@code attackEntityAsMob} — the Islands vampire bite: nothing on a 1-in-2
     * roll (:184-186; orig rolled the global OreSpawnRand, the port the entity's — the ENT-S-093 stream
     * convention), nothing on Peaceful (:187-189), else 1.0 of mob damage from this butterfly (:190-191).
     * Reached from {@link ButterflyIslandsHuntGoal} inside distSq 6 of the prey (orig :166-168). Inherited by the
     * subclasses in both trees (orig Mothra and EntityLunaMoth override neither this nor the hunt they inherit
     * through {@code super.updateAITasks()}, Mothra.java:169 / EntityLunaMoth.java:122); the port's Mothra and
     * Luna Moth call {@code doHurtTarget} nowhere, so only the hunt reaches it. ENT-S-117.
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.random.nextInt(2) != 0) return false;                                       // orig :184-186
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;                // orig :187-189
        return target.hurt(this.damageSources().mobAttack(this), 1.0f);                       // orig :190-191
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.0f;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    /** orig EntityButterfly.java:283-310 — "Butterfly" spawner bypass (forces type 1); feet-block air; daytime; Islands always allowed; otherwise y>=50. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) {
            this.setButterflyType(1);
            return true;
        }
        if (!level.getBlockState(this.blockPosition()).isAir()) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;
        return this.getY() >= 50.0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ButterflyType", this.getButterflyType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setButterflyType(tag.getInt("ButterflyType"));
    }

    /** orig EntityButterfly.java:256 — func_145773_az -> true: never presses plates or tripwires (ENT-S-090). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
