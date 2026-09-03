package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.TargetSelection;
import danger.orespawn.util.MyUtils;

public class EntityRat extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_RATLIVE = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratlive"));
    private static final SoundEvent SND_RATHIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rathit"));
    private static final SoundEvent SND_RATDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityRat.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.25f;
    /**
     * Owner assigned via {@link #setOwner}. Stored as a UUID (not a string) so that
     * spawner-spawned or summoned rats, whose NBT has no owner tag, can never reach
     * {@code UUID.fromString("")} — which crashed the server (BUG-003).
     */
    @Nullable
    private java.util.UUID ownerUuid = null;

    public EntityRat(EntityType<? extends EntityRat> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6483 — Rat 5 HP / 3 ATK / 1 armor
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.RAT.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, MobStats.RAT.attackDamage())
                .add(Attributes.ARMOR, MobStats.RAT.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int val) {
        this.entityData.set(DATA_ATTACKING, val);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        if (this.isPersistenceRequired()) return false;
        return this.ownerUuid == null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("inWall")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public void setOwner(LivingEntity entity) {
        if (entity instanceof Player player) {
            this.ownerUuid = player.getUUID();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) {
            tag.putUUID("MyOwner", this.ownerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ownerUuid = null;
        if (tag.hasUUID("MyOwner")) {
            this.ownerUuid = tag.getUUID("MyOwner");
        } else if (tag.contains("MyOwner", CompoundTag.TAG_STRING)) {
            // Legacy format: owner persisted as a string ("null" meant no owner; absent
            // tags read back as ""). Parse defensively so old saves can't crash (BUG-003).
            String legacyOwner = tag.getString("MyOwner");
            if (!legacyOwner.isEmpty() && !legacyOwner.equals("null")) {
                try {
                    this.ownerUuid = java.util.UUID.fromString(legacyOwner);
                } catch (IllegalArgumentException ignored) {
                    // Malformed legacy data: treat as wild rat rather than crash.
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }

        if (this.random.nextInt(5) == 1) {
            LivingEntity target = this.findSomethingToAttack();
            if (target != null) {
                this.setAttacking(1);
                this.getNavigation().moveTo(target, 1.25);
                if (this.distanceToSqr(target) < 4.0 &&
                        (this.random.nextInt(8) == 0 || this.random.nextInt(7) == 1)) {
                    this.doHurtTarget(target);
                }
            } else {
                this.setAttacking(0);
                if (this.ownerUuid != null) {
                    Player owner = this.level().getPlayerByUUID(this.ownerUuid);
                    if (owner != null) {
                        if (this.distanceToSqr(owner) > 64.0) {
                            this.getNavigation().moveTo(owner, 1.75);
                        }
                        if (this.distanceToSqr(owner) > 256.0) {
                            this.teleportTo(
                                    owner.getX() + this.random.nextFloat() - this.random.nextFloat(),
                                    owner.getY(),
                                    owner.getZ() + this.random.nextFloat() - this.random.nextFloat());
                        }
                    }
                }
            }
        }

        if (this.random.nextInt(250) == 1) {
            this.heal(1.0f);
        }
    }

    // orig Rat.java:185-249 — RatPlayerFriendly/RatPetFriendly only gate OWNED rats
    // (myowner != null); wild rats attack players and pets regardless of the configs.
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (MyUtils.isIgnoreable(target)) return false; // orig Rat.java:195-197 — the shared ignore screen, ahead of line of sight (:198) (ENT-S-106)
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof EntityRat) return false;

        if (target instanceof Player player) {
            if (player.getAbilities().invulnerable) return false;
            if (this.ownerUuid != null) {
                if (this.ownerUuid.equals(player.getUUID())) return false; // orig Rat.java:230-233
                if (OreSpawnConfig.RAT_PLAYER_FRIENDLY.get()) return false; // orig Rat.java:234-236
            }
        }
        if (this.ownerUuid != null && target instanceof TamableAnimal tamable) {
            if (OreSpawnConfig.RAT_PET_FRIENDLY.get() && tamable.isTame()) return false; // orig Rat.java:241-243
            if (tamable.getOwnerUUID() != null && this.ownerUuid.equals(tamable.getOwnerUUID())) return false; // orig Rat.java:244-246
        }
        return true;
    }

    private LivingEntity findSomethingToAttack() {
        AABB searchBox = this.getBoundingBox().inflate(9.0, 2.0, 9.0);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(targets, Comparator.comparingDouble(this::distanceToSqr), this::isSuitableTarget);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_RATLIVE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_RATHIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_RATDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.45f;
    }

    /**
     * orig Rat.java:302-339 — spawner bypass (x/z -2..+1); darkness; in the
     * Crystal dimension additionally y&lt;=50 and at least 4 air blocks in the
     * 3x3 ring one block above the feet; at most 8 buddies within 20/10/20.
     */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 1, 0, 4)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CRYSTAL)) {
            if (this.getY() > 50.0) return false;
            int airCount = 0;
            net.minecraft.core.BlockPos feet = this.blockPosition();
            for (int k = -1; k <= 1; k++) {
                for (int j = -1; j <= 1; j++) {
                    if (level.getBlockState(feet.offset(j, 1, k)).isAir()) airCount++;
                }
            }
            if (airCount < 4) return false;
        }
        return OriginalSpawnGates.countBuddies(this, level, EntityRat.class, 20.0, 10.0, 20.0) <= 8;
    }
}
