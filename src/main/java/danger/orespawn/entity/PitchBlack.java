package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Component;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;

/**
 * The Nightmare — Danger Dimension apex predator.
 *
 * <p>Wiki §14 (1.7.10) documents five discrete size tiers, each with its own
 * stat block. We reproduce them exactly:</p>
 *
 * <pre>
 *   tier | HP   | atk | armor | scale | xp
 *   -----+------+-----+-------+-------+-----
 *    1   |  125 |  15 |  10.0 |  0.50 | 200
 *    2   |  250 |  30 |  12.0 |  0.65 | 250
 *    3   |  500 |  60 |  14.0 |  0.80 | 350
 *    4   |  750 |  90 |  16.0 |  1.00 | 450
 *    5   | 1000 | 120 |  18.0 |  1.25 | 600
 * </pre>
 *
 * <p>The legacy {@code pitch_black} registry id and class name are preserved
 * for save compatibility — every existing world that contains a Nightmare
 * still resolves to this class. The {@code DATA_SIZE_TIER} byte is the new
 * canonical size source; the legacy {@code DATA_SCALE} (10×scale int) and
 * the older {@code Fscale} NBT field are still read so that betas saved with
 * the continuous-scale system migrate cleanly to the discrete tier system.</p>
 */
public class PitchBlack extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    /**
     * @deprecated Use {@link #DATA_SIZE_TIER}. Retained so legacy clients
     * connecting to a 1.0.0-beta.1 server still receive the synced value
     * (we mirror the tier into this field for read-only compatibility).
     */
    @Deprecated
    private static final EntityDataAccessor<Integer> DATA_SCALE =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SIZE_TIER =
            SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);

    /** Number of size tiers. Wiki §14: tiers 1..5 inclusive. */
    public static final int MIN_SIZE_TIER = 1;
    public static final int MAX_SIZE_TIER = 5;

    /** HP per size tier (1.7.10 fidelity). Index 0 unused; tier N → SIZE_HP[N]. */
    private static final double[] SIZE_HP   = { 0.0, 125.0, 250.0,  500.0, 750.0, 1000.0 };
    private static final double[] SIZE_ATK  = { 0.0,  15.0,  30.0,   60.0,  90.0,  120.0 };
    private static final double[] SIZE_ARM  = { 0.0,  10.0,  12.0,   14.0,  16.0,   18.0 };
    private static final float[]  SIZE_SCALE= { 0.0f, 0.50f, 0.65f, 0.80f, 1.00f,  1.25f };
    private static final int[]    SIZE_XP   = { 0,    200,   250,   350,  450,    600   };
    /** Base hitbox at scale 1.0 — the visual scale is applied on top of this. */
    private static final float BASE_WIDTH  = 2.0f;
    private static final float BASE_HEIGHT = 3.0f;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.literal("Nightmare"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private BlockPos currentFlightTarget = null;
    private final Comparator<Entity> targetSorter;
    private int damageTicker = 0;
    private int wingSound = 0;
    /**
     * Set to true after attributes / dimensions have been seeded for the
     * current size tier. Prevents {@link #applySizeTierAttributes()} from
     * stomping a saved-NBT health value during load.
     */
    private boolean attributesSeeded = false;

    public PitchBlack(EntityType<? extends PitchBlack> type, Level level) {
        super(type, level);
        this.targetSorter = Comparator.comparingDouble(this::distanceToSqr);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    /**
     * Base attribute envelope — high enough to register the entity in the
     * vanilla attribute system. The real per-size values are pushed into
     * the live {@link AttributeInstance}s by {@link #applySizeTierAttributes()}
     * the moment a size tier is assigned.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_SCALE, 5);
        builder.define(DATA_SIZE_TIER, MAX_SIZE_TIER);
    }

    public int getAttacking() { return this.entityData.get(DATA_ATTACKING); }
    public void setAttacking(int v) { this.entityData.set(DATA_ATTACKING, v); }
    public int getActivity() { return this.entityData.get(DATA_ACTIVITY); }
    public void setActivity(int v) { this.entityData.set(DATA_ACTIVITY, v); }

    /** @return the current size tier, clamped to {@code [MIN_SIZE_TIER, MAX_SIZE_TIER]}. */
    public int getSizeTier() {
        return Mth.clamp(this.entityData.get(DATA_SIZE_TIER), MIN_SIZE_TIER, MAX_SIZE_TIER);
    }

    /**
     * Assigns a discrete size tier (1..5) and re-derives this entity's
     * MAX_HEALTH, ATTACK_DAMAGE, ARMOR base values, hitbox dimensions, and
     * xp drop.  Safe to call at any time — health is rescaled proportionally
     * so a Nightmare healed-and-buffed mid-fight doesn't snap to full HP.
     *
     * @param tier 1..5 inclusive (clamped if out of range).
     */
    public void setSizeTier(int tier) {
        int clamped = Mth.clamp(tier, MIN_SIZE_TIER, MAX_SIZE_TIER);
        this.entityData.set(DATA_SIZE_TIER, clamped);
        // Mirror into the legacy DATA_SCALE field so old clients still see the
        // intended visual size (pre-tier the scale was a 0..10 int = 0..1.0).
        this.entityData.set(DATA_SCALE, Math.round(SIZE_SCALE[clamped] * 10.0f));
        applySizeTierAttributes();
    }

    /**
     * @return the visual-scale multiplier for the current size tier.
     * Used by both the renderer (model scale) and the AI (flight speed,
     * reach, knockback radius).
     */
    public float getPitchBlackScale() {
        return SIZE_SCALE[getSizeTier()];
    }

    /**
     * @deprecated The continuous-scale setter is preserved for save migration
     * (the v1.0 NBT field {@code Fscale} stored a float in [0.5, 1.0]).
     * Internally it now snaps to the nearest discrete size tier.
     */
    @Deprecated
    public void setPitchBlackScale(float v) {
        setSizeTier(scaleToTier(v));
    }

    /** Maps a legacy continuous scale value to its closest discrete tier. */
    private static int scaleToTier(float scale) {
        int best = MIN_SIZE_TIER;
        float bestDist = Math.abs(scale - SIZE_SCALE[MIN_SIZE_TIER]);
        for (int t = MIN_SIZE_TIER + 1; t <= MAX_SIZE_TIER; t++) {
            float d = Math.abs(scale - SIZE_SCALE[t]);
            if (d < bestDist) { bestDist = d; best = t; }
        }
        return best;
    }

    private void applySizeTierAttributes() {
        int tier = getSizeTier();

        AttributeInstance hp  = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance arm = this.getAttribute(Attributes.ARMOR);

        boolean firstApply = !attributesSeeded;
        float prevRatio = (hp != null && hp.getValue() > 0)
                ? this.getHealth() / (float) hp.getValue() : 1.0f;

        if (hp != null)  hp.setBaseValue(SIZE_HP[tier]);
        if (atk != null) atk.setBaseValue(SIZE_ATK[tier]);
        if (arm != null) arm.setBaseValue(SIZE_ARM[tier]);

        // First apply (fresh spawn) → fill HP. Otherwise preserve the % HP the
        // entity had before the resize, so a tier-up effect doesn't free-heal.
        if (firstApply) {
            this.setHealth(this.getMaxHealth());
        } else {
            this.setHealth(Mth.clamp(prevRatio * this.getMaxHealth(),
                    1.0f, this.getMaxHealth()));
        }

        this.xpReward = SIZE_XP[tier];
        this.bossEvent.setName(Component.literal("Nightmare (Size " + tier + ")"));
        this.refreshDimensions();

        attributesSeeded = true;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        // Hitbox scales with the visual model so the player's crosshair lands
        // on the silhouette correctly. We read DATA_SIZE_TIER directly rather
        // than via getSizeTier() because this can be invoked before the
        // SynchedEntityData has fully attached (during EntityType.create()).
        float scale = SIZE_SCALE[Mth.clamp(this.entityData.get(DATA_SIZE_TIER),
                MIN_SIZE_TIER, MAX_SIZE_TIER)];
        return EntityDimensions.scalable(BASE_WIDTH * scale, BASE_HEIGHT * scale);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SIZE_TIER.equals(key)) {
            // Client picks up server-side tier changes here so the model
            // re-scales without waiting for a full entity re-spawn packet.
            this.refreshDimensions();
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        // NIGHTMARE_SIZE config (0..5) controls the spawn tier:
        //   0 = random 1..5 (the 1.7.10 default — every Nightmare lottery)
        //   1..5 = force that exact tier (debug / arena / boss-rush servers)
        int forced = OreSpawnConfig.NIGHTMARE_SIZE.get();
        int tier = (forced >= MIN_SIZE_TIER && forced <= MAX_SIZE_TIER)
                ? forced
                : MIN_SIZE_TIER + this.random.nextInt(MAX_SIZE_TIER);
        setSizeTier(tier);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Save migration: prefer the v1.1 SizeTier field; fall back to the
        // v1.0-beta.1 Fscale float so existing worlds keep their Nightmares
        // at the right power tier. Default to mid (3) if neither is present.
        if (tag.contains("SizeTier")) {
            setSizeTier(tag.getInt("SizeTier"));
        } else if (tag.contains("Fscale")) {
            setSizeTier(scaleToTier(tag.getFloat("Fscale")));
        } else {
            setSizeTier(3);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SizeTier", getSizeTier());
        // Keep the legacy field in sync so a save written by 1.1+ can still
        // be opened by an older 1.0.0-beta.1 client without losing the size.
        tag.putFloat("Fscale", getPitchBlackScale());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof LivingEntity le) {
            float scale = this.getPitchBlackScale();
            // Use the per-tier attack base (already on Attributes.ATTACK_DAMAGE)
            // — multiplying the legacy 50 by scale double-applied the curve.
            float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            boolean ret = le.hurt(this.damageSources().mobAttack(this), damage);
            if (ret) {
                double knockbackHorizontal = 1.15 * scale;
                double knockbackVertical = 0.08 * scale;
                float pushAngle = (float) Math.atan2(le.getZ() - this.getZ(), le.getX() - this.getX());
                if (le.isRemoved() || le instanceof Player) knockbackVertical *= 2.0;
                le.push(Math.cos(pushAngle) * knockbackHorizontal, knockbackVertical, Math.sin(pushAngle) * knockbackHorizontal);
            }
            return ret;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.wingSound;
        if (this.wingSound > 20) {
            if (!this.level().isClientSide()) {
                this.playSound(SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "mothrawings")), 1.0f, 1.0f);
            }
            this.wingSound = 0;
        }
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.damageTicker > 0) --this.damageTicker;
        if (this.getActivity() == 0) {
            super.customServerAiStep();
            if (this.getRandom().nextInt(10) == 1) {
                LivingEntity spottedTarget = findSomethingToAttack();
                if (spottedTarget != null) {
                    this.setActivity(1);
                    this.getNavigation().stop();
                }
            }
            return;
        }
        if (this.isRemoved()) return;
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = this.blockPosition();
        }
        float scale = this.getPitchBlackScale();
        if (this.getRandom().nextInt(150) == 0
                || this.currentFlightTarget.distSqr(this.blockPosition()) < 4) {
            int xdir = this.getRandom().nextInt(20) + (int) (5 * scale);
            int zdir = this.getRandom().nextInt(20) + (int) (5 * scale);
            if (this.getRandom().nextInt(2) == 0) xdir = -xdir;
            if (this.getRandom().nextInt(2) == 0) zdir = -zdir;
            this.currentFlightTarget = new BlockPos(
                    (int) this.getX() + xdir,
                    (int) this.getY() + this.getRandom().nextInt(11) - 5,
                    (int) this.getZ() + zdir);
        } else if (this.getRandom().nextInt(8) == 0) {
            LivingEntity chaseTarget = findSomethingToAttack();
            if (chaseTarget != null) {
                double meleeRadius = 5.0 + chaseTarget.getBbWidth() / 2.0f + scale;
                this.setAttacking(1);
                this.currentFlightTarget = new BlockPos((int) chaseTarget.getX(), (int) (chaseTarget.getY() + 2), (int) chaseTarget.getZ());
                if (this.distanceToSqr(chaseTarget) < meleeRadius * meleeRadius) {
                    this.doHurtTarget(chaseTarget);
                }
            } else {
                this.setAttacking(0);
            }
        }
        double toTargetX = this.currentFlightTarget.getX() + 0.4 - this.getX();
        double toTargetY = this.currentFlightTarget.getY() + 0.1 - this.getY();
        double toTargetZ = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        double flightSpeed = 0.5f + scale / 10.0f;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(
                motion.x + (Math.signum(toTargetX) * flightSpeed - motion.x) * 0.33,
                motion.y + (Math.signum(toTargetY) * 0.7 - motion.y) * 0.2,
                motion.z + (Math.signum(toTargetZ) * flightSpeed - motion.z) * 0.33
        );
        float motionYaw = (float) (Math.atan2(this.getDeltaMovement().z, this.getDeltaMovement().x) * 180.0 / Math.PI) - 90.0f;
        float yawDelta = Mth.wrapDegrees(motionYaw - this.getYRot());
        this.setYRot(this.getYRot() + yawDelta / 5.0f);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.damageTicker > 0) return false;
        this.damageTicker = 20;
        boolean ret = super.hurt(source, amount);
        Entity attacker = source.getEntity();
        if (attacker != null && this.currentFlightTarget != null) {
            this.currentFlightTarget = new BlockPos((int) attacker.getX(), (int) (attacker.getY() + 2), (int) attacker.getZ());
        }
        this.setActivity(1);
        this.getNavigation().stop();
        return ret;
    }

    @Override
    public boolean causeFallDamage(float dist, float mult, DamageSource source) {
        return false;
    }

    private LivingEntity findSomethingToAttack() {
        float scale = this.getPitchBlackScale();
        double horizontalReach = 16.0 + scale * 6.0;
        double verticalReach = 10.0 + scale * 4.0;
        AABB searchBox = this.getBoundingBox().inflate(horizontalReach, verticalReach, horizontalReach);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        entities.sort(this.targetSorter);
        for (LivingEntity candidate : entities) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof PitchBlack) return false;
        if (target instanceof Player p && p.getAbilities().instabuild) return false;
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(5) != 2) return null;
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_living"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "pitchblack_dead"));
    }
}
