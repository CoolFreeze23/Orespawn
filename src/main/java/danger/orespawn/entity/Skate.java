package danger.orespawn.entity;

import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Skate extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Skate.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 15;
    private static final double MOVE_SPEED = 0.25;
    private static final double ATTACK_DAMAGE = 4.0;

    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    public Skate(EntityType<? extends Skate> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
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
    public boolean hurt(DamageSource source, float amount) {
        if (this.isRemoved()) return false;
        Entity attacker = source.getEntity();
        if (attacker instanceof Skate) return false;
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (!this.isInWater() && this.random.nextInt(10) == 0) {
            this.closestWaterDistance = 99999;
            this.targetX = 0; this.targetY = 0; this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 5);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistance < 99999) {
                this.getNavigation().moveTo(this.targetX, this.targetY - 1, this.targetZ, 1.33);
            } else {
                if (this.random.nextInt(25) == 1) {
                    this.hurt(this.damageSources().dryOut(), 1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }

        if (this.random.nextInt(8) == 1) {
            LivingEntity target = this.getTarget();
            if (target == null) {
                Player nearest = this.level().getNearestPlayer(this, 10.0);
                if (nearest != null && !nearest.getAbilities().instabuild) {
                    target = nearest;
                    this.setTarget(target);
                }
            }
            if (target != null && target.isAlive()) {
                if (this.distanceToSqr(target) < 4.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(4) == 0) {
                        this.doHurtTarget(target);
                    }
                } else {
                    this.getNavigation().moveTo(target, 1.2);
                }
            } else {
                this.setAttacking(0);
            }
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return null; }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead"));
    }

    @Override
    protected float getSoundVolume() { return 0.33f; }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.random.nextInt(30) != 1) return false;
        return level.getEntitiesOfClass(Skate.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0)).size() <= 6;
    }
}
