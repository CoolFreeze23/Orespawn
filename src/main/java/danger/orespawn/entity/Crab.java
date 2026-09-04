package danger.orespawn.entity;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import javax.annotation.Nullable;
import danger.orespawn.MobStats;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;
import danger.orespawn.util.MyUtils;
import net.minecraft.world.entity.npc.Villager;
import danger.orespawn.entity.ai.TargetSelection;

public class Crab extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SCORPION_ATTACK = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_attack"));
    private static final SoundEvent SND_SCORPION_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "scorpion_living"));
    private static final SoundEvent SND_LEAVES_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "leaves_hit"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SCALE =
            SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private int hurtTimer = 0;
    private float moveSpeed = 0.55f;
    private static final double KNOCKBACK_HORIZONTAL_SCALE = 1.15;
    private static final double KNOCKBACK_VERTICAL_SCALE = 0.48;
    private static final double PLAYER_VERTICAL_KNOCKBACK_MULTIPLIER = 2.0;
    /** Scale whose stats were last pushed into the live attributes; -1 = never. */
    private float lastAppliedStatScale = -1.0f;
    /** orig Crab.java:64 — the revenge task; the pass's 1-in-100 release ends it (see {@link RevengeGoal}); assigned in registerGoals (the Mob constructor). ENT-S-129. */
    private RevengeGoal revengeGoal;
    /**
     * OPT-009: the crab's speed genuinely varies (water/land × growth scale), so
     * the per-tick write stays, but the AttributeInstance is resolved once here
     * instead of via a map lookup every tick. Attribute instances are created
     * with the entity and live exactly as long as it does (a dimension change
     * constructs a fresh entity), so this reference can never go stale.
     */
    private final net.minecraft.world.entity.ai.attributes.AttributeInstance movementSpeedAttribute;

    public Crab(EntityType<? extends Crab> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        // TF-035: orig Crab.java:43,58 — scans sort with GenericTargetSorter
        // (creeper-halved / big-silhouette-prioritized), not plain distance.
        this.targetSorter = new GenericTargetSorter(this);
        this.movementSpeedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
    }

    @Override
    protected void registerGoals() {
        // orig Crab.java:59-64 — plain wander (MyEntityAIWanderALot), no water
        // avoidance; the crab actively SEEKS water via the tick scan below.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.revengeGoal = new RevengeGoal();
        this.targetSelector.addGoal(1, this.revengeGoal); // orig Crab.java:64 — EntityAIHurtByTarget(this, false), released by the pass's 1-in-100 (ENT-S-129)
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6524 — Crab table entry 180 HP / 24 ATK / 16 armor.
        // These are the scale=1.0 baselines; applyScaleStats() pushes the real
        // per-scale values (orig Crab.java:69-71,137,141), including the original
        // bug where crab health reads PitchBlack's 250 instead of 180.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.PITCH_BLACK.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, MobStats.CRAB.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ARMOR, MobStats.CRAB.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SCALE, 25);
    }

    public float getCrabScale() {
        return this.entityData.get(DATA_SCALE) / 100.0f;
    }

    public void setCrabScale(float val) {
        this.entityData.set(DATA_SCALE, (int) (val * 100.0f));
        if (!this.level().isClientSide) {
            applyScaleStats();
        }
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        // orig Crab.java:133 — live hitbox is 2.5×scale wide, 3.5×scale tall
        // (the per-tick setSize overrides the spawn-time 3.75 width of :97).
        float scale = this.entityData.get(DATA_SCALE) / 100.0f;
        if (scale <= 0.0f) scale = 0.25f;
        return EntityDimensions.scalable(2.5f * scale, 3.5f * scale);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_SCALE.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        // orig Crab.java:74-98 — scale 0.25 base, 1-in-4 → 0.5, 1-in-8 → 1.0;
        // spawner-spawned crabs are pinned to 0.35 (orig func_70601_bi:466).
        float scale = 0.25f;
        if (this.random.nextInt(4) == 1) scale = 0.5f;
        if (this.random.nextInt(8) == 2) scale = 1.0f;
        if (reason == MobSpawnType.SPAWNER) scale = 0.35f;
        this.setCrabScale(scale);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    /**
     * Pushes the size-scaled stats into the live attributes, mirroring the
     * original formulas:
     * <ul>
     *   <li>HP = {@code PitchBlack_stats.health (250) × scale} — orig
     *       Crab.java:137 reads PitchBlack's health instead of Crab's 180;
     *       original bug kept for parity.</li>
     *   <li>ATK = {@code Crab_stats.attack (24) × scale} — orig Crab.java:71.</li>
     *   <li>Armor = {@code Crab_stats.defense (16) + 2 × scale} — orig Crab.java:141.</li>
     *   <li>XP = {@code 400 × scale} — orig Crab.java:95,116.</li>
     * </ul>
     */
    private void applyScaleStats() {
        float scale = this.getCrabScale();
        var hp = this.getAttribute(Attributes.MAX_HEALTH);
        var atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        var arm = this.getAttribute(Attributes.ARMOR);
        if (hp != null) hp.setBaseValue((int) (MobStats.PITCH_BLACK.maxHealth() * scale));
        if (atk != null) atk.setBaseValue(MobStats.CRAB.attackDamage() * scale);
        if (arm != null) arm.setBaseValue((int) (MobStats.CRAB.armor() + 2.0f * scale));
        // Clamp (covers the fresh-spawn case where health still reflects the
        // pre-scale builder baseline); never heals a damaged crab.
        this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
        this.xpReward = (int) (400.0f * scale);
        this.lastAppliedStatScale = scale;
    }

    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public final void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Override
    public void tick() {
        this.moveSpeed = this.isInWater() ? 0.95f : 0.55f;
        // OPT-009: cached instance; setBaseValue itself no-ops when unchanged.
        this.movementSpeedAttribute.setBaseValue(this.moveSpeed * this.getCrabScale());
        if (!this.level().isClientSide && this.getCrabScale() != this.lastAppliedStatScale) {
            applyScaleStats();
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // The attribute already carries Crab_stats.attack × scale (orig Crab.java:71).
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean hit = target.hurt(this.damageSources().mobAttack(this), damage);
        if (hit && target instanceof LivingEntity) {
            float scale = this.getCrabScale();
            double horizontalKnockback = KNOCKBACK_HORIZONTAL_SCALE * scale;
            double verticalKnockback = KNOCKBACK_VERTICAL_SCALE * scale;
            float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
            if (target instanceof Player) verticalKnockback *= PLAYER_VERTICAL_KNOCKBACK_MULTIPLIER;
            target.push(
                    Math.cos(yawToTarget) * horizontalKnockback,
                    verticalKnockback,
                    Math.sin(yawToTarget) * horizontalKnockback);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof Crab) return false;
        boolean hurtApplied = false;
        if (this.hurtTimer <= 0) {
            this.hurtTimer = 8;
            hurtApplied = super.hurt(source, amount);
        }
        // orig Crab.java:232-238 — an EntityLiving attacker (the port's Mob) becomes the stored target, hurt timer or no
        // hurt timer; a player who hits the crab is stored by the revenge goal alone, under its screen (ENT-S-129)
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return hurtApplied;
    }

    /**
     * orig Crab.java:64 {@code EntityAIHurtByTarget(this, false)} — the revenge task whose attack target the pass read
     * ahead of the scan (:345) and released on its roll (:342-344); 1.7.10's task ended when its attack target
     * was nulled ({@code EntityAITarget.continueExecuting}), where vanilla's {@code TargetGoal} re-asserts its own memory
     * into an emptied slot — so the pass's release also drops that memory ({@link #release}). The hold itself stays
     * vanilla's. ENT-S-129.
     */
    private final class RevengeGoal extends HurtByTargetGoal {
        RevengeGoal() {
            super(Crab.this);
        }

        /** orig :342-344 {@code setAttackTarget(null)} ended the task: the goal's memory goes with the slot. */
        void release() {
            this.targetMob = null;
        }
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.hurtTimer > 0) --this.hurtTimer;

        // orig Crab.java:314-338 — 1-in-25 while dry: path to the nearest
        // water within ~12 blocks at speed 1.33; if none, 1-in-100 lose
        // 1×scale HP (silent dry-out) and vanish (no drops) at 0 HP.
        if (!this.isInWater() && this.getRandom().nextInt(25) == 0) {
            BlockPos water = findNearestWater();
            if (water != null) {
                this.getNavigation().moveTo(water.getX(), water.getY() - 1, water.getZ(), 1.33);
            } else {
                if (this.getRandom().nextInt(100) == 1) {
                    this.heal(-1.0f * this.getCrabScale());
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.getRandom().nextInt(5) == 1) {
            if (this.getRandom().nextInt(100) == 1) {                // orig Crab.java:342-344 — the 1-in-100 `setAttackTarget(null)` inside the 1-in-5 pass, ahead of the read (:345) (ENT-S-129)
                this.setTarget(null);
                this.revengeGoal.release();                          // 1.7.10's task ended on a nulled attack target; vanilla's TargetGoal would re-assert its memory (ENT-S-129)
            }
            LivingEntity currentTarget = this.getTarget();           // orig :345
            if (currentTarget != null && !currentTarget.isAlive()) {
                this.setTarget(null);
                currentTarget = null;
            }
            if (currentTarget == null) currentTarget = findSomethingToAttack();
            if (currentTarget != null) {
                this.lookAt(currentTarget, 10.0f, 10.0f);
                // orig Crab.java:354 — distSq < (6 + w/2)² × scale (scale is NOT
                // squared with the range term).
                float reach = 6.0f + currentTarget.getBbWidth() / 2.0f;
                if (this.distanceToSqr(currentTarget) < reach * reach * this.getCrabScale()) {
                    this.setAttacking(1);
                    // orig Crab.java:356 — swing when nextInt(4)==0 OR nextInt(5)==1 (~40%).
                    if (this.getRandom().nextInt(4) == 0 || this.getRandom().nextInt(5) == 1) {
                        this.doHurtTarget(currentTarget);
                        // orig Crab.java:358-364 — scorpion_attack 1-in-3, else
                        // scorpion_living; vol 0.75, pitch 1.5, at the target.
                        // OPT-011: same 1-in-3 pick, cached events instead of a
                        // fresh SoundEvent + ResourceLocation per swing.
                        SoundEvent swingSound = this.getRandom().nextInt(3) == 1
                                ? SND_SCORPION_ATTACK : SND_SCORPION_LIVING;
                        this.level().playSound(null, currentTarget.getX(), currentTarget.getY(),
                                currentTarget.getZ(), swingSound, this.getSoundSource(), 0.75f, 1.5f);
                    }
                } else {
                    this.getNavigation().moveTo(currentTarget, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.getRandom().nextInt(120) == 1 && this.isInWater() && this.getHealth() < this.getMaxHealth()) {
            // orig Crab.java:373-376 — splash sound accompanies the water heal.
            this.playSound(SoundEvents.GENERIC_SPLASH, 1.5f,
                    this.getRandom().nextFloat() * 0.2f + 0.9f);
            this.heal(4.0f * this.getCrabScale());
        }
    }

    /**
     * orig Crab.java:243-327 ({@code scan_it} shells) — nearest water or
     * flowing-water block within an expanding box capped at 12/10/12. A flat
     * nearest-block scan over the same volume yields the same target.
     */
    @Nullable
    private BlockPos findNearestWater() {
        BlockPos origin = this.blockPosition().below();
        BlockPos best = null;
        int bestDistSq = Integer.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -12; dx <= 12; ++dx) {
            for (int dy = -10; dy <= 10; ++dy) {
                for (int dz = -12; dz <= 12; ++dz) {
                    int distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq >= bestDistSq) continue;
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (this.level().getBlockState(cursor).is(Blocks.WATER)) {
                        bestDistSq = distSq;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    private LivingEntity findSomethingToAttack() {
        // orig Crab.java:421-423 — PlayNicely disables crab aggression entirely.
        if (danger.orespawn.OreSpawnConfig.PLAY_NICELY.get()) return null;
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        // OPT-021: nearest-first pick without the full list sort; TargetSelection
        // preserves the removed sort's order and stable-tie semantics exactly.
        return TargetSelection.firstMatch(list, this.targetSorter, this::isSuitableTarget);
    }

    /**
     * orig Crab.java:379-418 — the crab is an equal-opportunity predator:
     * beyond non-creative players (:392-395) and all monsters (:399-401) it
     * hunts Lizards (:402), RubberDuckies (:405), Villagers (:408),
     * Girlfriends (:411) and Boyfriends (:414), then falls through to the
     * shared attackable-non-mob list (:417). Line of sight is required up
     * front (:389); fellow crabs are never targets (:396-398).
     */
    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (!this.getSensing().hasLineOfSight(target)) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        if (target instanceof Crab) return false;
        if (target instanceof Monster) return true;
        if (target instanceof Lizard) return true;
        if (target instanceof EntityRubberDucky) return true;
        if (target instanceof Villager) return true;
        if (target instanceof Girlfriend) return true;
        if (target instanceof Boyfriend) return true;
        return MyUtils.isAttackableNonMob(target);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_LEAVES_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Override
    public float getVoicePitch() {
        // orig Crab.java:172-174 — deterministic pitch by size, 2.0 − 0.3/scale
        // (mini 0.25 → 0.8, mid 0.5 → 1.4, giant 1.0 → 1.7); replaces the
        // vanilla random jitter entirely.
        return 2.0f - 0.3f * (1.0f / this.getCrabScale());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Fscale", this.getCrabScale());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setCrabScale(tag.getFloat("Fscale"));
    }

    /**
     * orig Crab.java:456-486 — spawner bypass (forces the mini 0.35 scale);
     * y&gt;=50; daytime; in the Crystal dimension additionally a 1-in-40 dice and
     * at most 3 buddies within 24/8/24 (orig findBuddies, Crab.java:451-454).
     */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) {
            this.setCrabScale(0.35f);
            return true;
        }
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.CRYSTAL)) {
            if (this.getRandom().nextInt(40) != 1) return false;
            if (OriginalSpawnGates.countBuddies(this, level, Crab.class, 24.0, 8.0, 24.0) > 3) return false;
        }
        return true;
    }
}
