package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SeaMonster extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_SEAMONSTER_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_living"));
    private static final SoundEvent SND_SEAMONSTER_HIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_hit"));
    private static final SoundEvent SND_SEAMONSTER_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SeaMonster.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.25;

    private int hurtCooldown = 0;
    private float dynamicMoveSpeed = 0.25f;
    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    /**
     * OPT-009: the speed genuinely varies (water/land), so the per-tick write
     * stays, but the AttributeInstance is resolved once instead of via a map
     * lookup every tick. Attribute instances live exactly as long as the entity
     * (a dimension change constructs a fresh entity), so this cannot go stale.
     */
    private final net.minecraft.world.entity.ai.attributes.AttributeInstance movementSpeedAttribute;

    public SeaMonster(EntityType<? extends SeaMonster> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.movementSpeedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6493 — SeaMonster 110 HP / 14 ATK / 8 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.SEA_MONSTER.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.SEA_MONSTER.attackDamage())
                .add(Attributes.ARMOR, MobStats.SEA_MONSTER.armor());
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

    @Override
    public void aiStep() {
        super.aiStep();
        // orig SeaMonster.java:126 — onLivingUpdate sets moveSpeed to 0.55 in water / 0.25 on land,
        // and orig SeaMonster.java:93 — onUpdate writes it into the MOVEMENT_SPEED attribute every tick.
        this.dynamicMoveSpeed = this.isInWater() ? 0.55f : 0.25f;
        // OPT-009: cached instance; setBaseValue itself no-ops when unchanged.
        this.movementSpeedAttribute.setBaseValue(this.dynamicMoveSpeed);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 0.6;
                double upwardKnockback = 0.1;
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
        boolean ret = false;
        if (this.hurtCooldown <= 0) {
            ret = super.hurt(source, amount);
            this.hurtCooldown = 8;
        }
        if (attacker instanceof Mob mob) {
            if (attacker instanceof SeaMonster) return false;
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

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
                if (this.random.nextInt(40) == 1) {
                    this.hurt(this.damageSources().dryOut(), 1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.random.nextInt(5) == 1) {
            LivingEntity target = this.getTarget();
            if (target == null) {
                Player nearest = this.level().getNearestPlayer(this, 16.0);
                if (nearest != null && !nearest.getAbilities().instabuild) {
                    target = nearest;
                    this.setTarget(target);
                }
            }
            if (target != null && target.isAlive()) {
                this.lookAt(target, 10.0f, 10.0f);
                double range = (4.0 + target.getBbWidth() / 2.0) * (4.0 + target.getBbWidth() / 2.0);
                if (this.distanceToSqr(target) < range) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }

        if (this.random.nextInt(120) == 1 && this.isInWater() && this.getHealth() < this.getMaxHealth()) {
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

    // Death drops are fully data-driven via loot_table/entities/sea_monster.json
    // (orig SeaMonster.java:170-322: scale, painting, 9-14 raw fish,
    // one d20 roll of the Iron gear table).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SND_SEAMONSTER_LIVING;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_SEAMONSTER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_SEAMONSTER_DEATH;
    }

    /** orig SeaMonster.java:544-570 — "Sea Monster" spawner bypass; y>=50; night; darkness; no other SeaMonster within 16/5/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (this.getY() < 50.0) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, SeaMonster.class, 16.0, 5.0, 16.0);
    }
}
