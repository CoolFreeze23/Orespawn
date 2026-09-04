package danger.orespawn.entity;

import danger.orespawn.MobStats;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.DinosaurMeleeAttackGoal;
import danger.orespawn.entity.ai.PointysaurusStareGoal;
import danger.orespawn.util.MyUtils;

public class Pointysaurus extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_living"));
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_ALO_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Pointysaurus.class, EntityDataSerializers.INT);

    private final float moveSpeed = 0.35f;

    public Pointysaurus(EntityType<? extends Pointysaurus> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.xpReward = 40;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DinosaurMeleeAttackGoal(this, this::setAttacking,
                DinosaurMeleeAttackGoal.Presets.pointysaurus()));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Phase 10 — Enderman-style eye-contact aggression. Runs at priority 2
        // so it overrides the regular proximity targeting; if you stare at the
        // Pointysaurus it locks onto you. Wider-radius proximity aggro still
        // exists at priority 3 as a fallback so it isn't completely passive
        // when you mind your business but get too close.
        // orig Pointysaurus.java:250-252 — findSomethingToAttack answers null under PlayNicely (PlayNicely != 0);
        // the port's proactive pick is this pair of goals, so the flag is read live in their canUse: neither
        // starts while PlayNicely is on (ENT-S-115).
        // MOD-034 (T9 A3): modern only, a construction snapshot (OreSpawnConfig.pointysaurusStareAggro(),
        // read once here; goals register in the Mob ctor, the BOSS-017 shape — a config change applies
        // to newly spawned Pointysaurs); classic registers no stare goal (orig Pointysaurus.java:50-55 —
        // aggression only from the proximity scan below and from being hit). The ENT-S-115 canUse gate
        // stays on the goal.
        if (OreSpawnConfig.pointysaurusStareAggro()) {
            this.targetSelector.addGoal(2, new PointysaurusStareGoal(this) {
                @Override
                public boolean canUse() {
                    if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Pointysaurus.java:250-252 (ENT-S-115)
                    return super.canUse();
                }
            });
        }
        // Pointysaurus only targets players — it will not attack other mobs.
        // This preserves the 1.7.10 isSuitableTarget filter (rejects all
        // Monster instances) which would otherwise make it pacifist without
        // an explicit Player-only target goal.
        // orig Pointysaurus.java:227-229 — the shared ignore screen, ahead of the
        // species chain and line of sight (:239), as the target goal's predicate (ENT-S-106).
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !MyUtils.isIgnoreable(target)) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig Pointysaurus.java:250-252 (ENT-S-115)
                return super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6472 — Pointysaurus 80 HP / 10 ATK / 16 armor;
        // speed 0.35 matches orig Pointysaurus.java:40.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.POINTYSAURUS.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.POINTYSAURUS.attackDamage())
                .add(Attributes.ARMOR, MobStats.POINTYSAURUS.armor())
                .add(Attributes.FOLLOW_RANGE, 24.0);
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
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return SND_ALO_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_ALO_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackHorizontal = 0.8;
                double knockbackVertical = 0.1;
                float pushAngle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) {
                    knockbackVertical *= 2.0;
                }
                target.push(Math.cos(pushAngle) * knockbackHorizontal, knockbackVertical, Math.sin(pushAngle) * knockbackHorizontal);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getMsgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, (int) (byte) value);
    }

    // Death drops are fully data-driven via loot_table/entities/pointysaurus.json
    // (orig Pointysaurus.java:127-141: 10 leather, 6 raw beef, 6 rotten flesh, 6 string).

    /** orig Pointysaurus.java:275-312 — "Pointysaurus" spawner bypass; darkness; y>=50; night; clear-air column. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        return OriginalSpawnGates.airBox(this, level, -1, 0, 1, 5, -1, 0);
    }
}
