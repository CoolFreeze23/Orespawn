package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.ai.SeaViperBiteGoal;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

public class SeaViper extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SEAVIPER_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_living"));
    private static final SoundEvent SND_SEAVIPER_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_hit"));
    private static final SoundEvent SND_SEAVIPER_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seaviper_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SeaViper.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED_IN_WATER = 0.75;
    private static final double MOVE_SPEED_OUT_OF_WATER = 0.25;

    private int hurtCooldown = 0;
    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    /**
     * OPT-009: the speed genuinely varies (water/land), so the per-tick write
     * stays, but the AttributeInstance is resolved once instead of via a map
     * lookup every tick. Attribute instances live exactly as long as the entity
     * (a dimension change constructs a fresh entity), so this cannot go stale.
     */
    private final net.minecraft.world.entity.ai.attributes.AttributeInstance movementSpeedAttribute;

    public SeaViper(EntityType<? extends SeaViper> type, Level level) {
        super(type, level);
        this.movementSpeedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
        this.xpReward = 120;
        // Smooth amphibious move/look — modern 1.21.1 equivalent of the
        // 1.7.10 EntityAISwimming + func_70648_aU (breathes underwater) combo.
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    // AI: FloatGoal pushes the viper up if it ends up in deep air; the
    // SeaViperBiteGoal carries the 1.7.10 swing dice + hunger-on-hit
    // effect; RandomSwimmingGoal gives idle amphibious wander inside water
    // bodies. Target acquisition relies on HurtByTargetGoal +
    // NearestAttackableTargetGoal to replace the legacy 18×4×18 scan.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // orig SeaViper.java:539-543 — the stored target was read inside the PlayNicely-gated scan method (:531-533): under
        // the flag the pass acted on nothing (attacking 0), the stored target kept; the bite goal stands down under the
        // flag through Presets.seaViper (ENT-S-129)
        this.goalSelector.addGoal(1, new SeaViperBiteGoal(this, this::setAttacking));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0, 40));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // orig SeaViper.java:531-533 — findSomethingToAttack answers null under PlayNicely (PlayNicely != 0). The
        // port's scan is this goal, so the flag is read live in its canUse: the goal never starts while PlayNicely
        // is on (ENT-S-115).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                if (OreSpawnConfig.PLAY_NICELY.get()) return false; // orig SeaViper.java:531-533 (ENT-S-115)
                return super.canUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6494 — SeaViper 160 HP / 22 ATK / 12 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SEA_VIPER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED_IN_WATER)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SEA_VIPER.attackDamage())
                .add(Attributes.ARMOR, MobStats.SEA_VIPER.armor())
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    // Pure water navigation when submerged — without this the viper would
    // path like a land mob and drown itself on reach-for-shore attempts.
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    // 1.21.1's LivingEntity#canBreatheUnderwater is final. NeoForge's
    // IEntityExtension#canDrownInFluidType lets us opt out of drowning in
    // any fluid type (water included), which is the modern equivalent.
    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 vec) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), vec);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(vec);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    public void setAttacking(int value) {
        this.entityData.set(DATA_ATTACKING, value);
    }

    // Dynamic speed mirror: 0.75 in water, 0.25 on land. We nudge the
    // attribute every aiStep because some modern goals cache the base value.
    @Override
    public void aiStep() {
        super.aiStep();
        // OPT-009: cached instance; setBaseValue itself no-ops when unchanged.
        this.movementSpeedAttribute.setBaseValue(
                this.isInWater() ? MOVE_SPEED_IN_WATER : MOVE_SPEED_OUT_OF_WATER);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Vanilla Monster#doHurtTarget applies attack damage + standard
        // knockback. We stack the legacy 0.8 h / 0.14 v knockback on top so
        // bites feel like the original push. Hunger-on-hit is applied by
        // SeaViperBiteGoal#onSuccessfulAttack.
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 0.8;
                double upwardKnockback = 0.14;
                float angleToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target instanceof Player) upwardKnockback *= 2.0;
                target.push(Math.cos(angleToTarget) * knockbackStrength, upwardKnockback, Math.sin(angleToTarget) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        Entity attacker = source.getEntity();
        // Sea Vipers do not friendly-fire each other (1.7.10 behaviour).
        if (attacker instanceof SeaViper) return false;
        boolean ret = false;
        if (this.hurtCooldown <= 0) {
            ret = super.hurt(source, amount);
            this.hurtCooldown = 5;
        }
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    // Keep the legacy dry-out + water-seek behaviour outside the new goal:
    // when out of water, scan outward in a 12-block cube, path to the
    // nearest water column, and gradually take damage if none is found.
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.hurtCooldown > 0) --this.hurtCooldown;

        if (!this.isInWater() && this.random.nextInt(25) == 0) {
            this.closestWaterDistance = 99999;
            this.targetX = 0; this.targetY = 0; this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 10);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistance < 99999) {
                this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
            } else {
                if (this.random.nextInt(150) == 1) {
                    this.hurt(this.damageSources().dryOut(), 1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.random.nextInt(100) == 1 && this.isInWater() && this.getHealth() < this.getMaxHealth()) {
            this.playSound(SoundEvents.GENERIC_SPLASH, 1.5f, this.random.nextFloat() * 0.2f + 0.9f);
            this.heal(1.0f);
        }
    }

    private boolean scanForWater(int x, int y, int z, int dx, int dy, int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) for (int j = -dz; j <= dz; ++j) {
            found += checkWaterAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
            found += checkWaterAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
        }
        for (int i = -dx; i <= dx; ++i) for (int j = -dz; j <= dz; ++j) {
            found += checkWaterAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
            found += checkWaterAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
        }
        for (int i = -dx; i <= dx; ++i) for (int j = -dy; j <= dy; ++j) {
            found += checkWaterAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
            found += checkWaterAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
        }
        return found != 0;
    }

    private int checkWaterAt(int x, int y, int z, int dist) {
        BlockState state = this.level().getBlockState(new BlockPos(x, y, z));
        if (state.is(Blocks.WATER) && dist < this.closestWaterDistance) {
            this.closestWaterDistance = dist; this.targetX = x; this.targetY = y; this.targetZ = z;
            return 1;
        }
        return 0;
    }

    // Death drops are fully data-driven via loot_table/entities/sea_viper.json
    // (orig SeaViper.java:174-327: tongue, painting, 9-14x paired raw fish +
    // raw chicken, one d20 roll of the Iron gear table).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(2) == 0) {
            return SND_SEAVIPER_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_SEAVIPER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_SEAVIPER_DEATH;
    }

    // Keep legacy "near-surface water" spawn rule: Y >= 50 (vanilla sea level
    // in 1.21.1 is ~63), mob must be in water or have a water body nearby,
    // and no other Sea Viper within 16 blocks to avoid cluster overspawn.
    /** orig SeaViper.java:561-584 — "Sea Viper" spawner bypass; y>=50; daytime; no other SeaViper within 16/5/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, SeaViper.class, 16.0, 5.0, 16.0);
    }
}
