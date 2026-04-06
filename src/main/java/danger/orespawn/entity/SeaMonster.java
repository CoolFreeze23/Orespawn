package danger.orespawn.entity;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SeaMonster extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SeaMonster.class, EntityDataSerializers.INT);

    private static final int MAX_HEALTH = 150;
    private static final double MOVE_SPEED = 0.25;
    private static final double ATTACK_DAMAGE = 15.0;

    private int hurtCooldown = 0;
    private float dynamicMoveSpeed = 0.25f;
    private int closestWaterDistance = 99999;
    private int targetX = 0, targetY = 0, targetZ = 0;

    public SeaMonster(EntityType<? extends SeaMonster> type, Level level) {
        super(type, level);
        this.xpReward = 150;
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
    public void aiStep() {
        super.aiStep();
        this.dynamicMoveSpeed = this.isInWater() ? 0.55f : 0.25f;
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

        if (this.random.nextInt(120) == 1 && this.isInWater() && this.getHealth() < MAX_HEALTH) {
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

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        this.spawnAtLocation(Items.HEART_OF_THE_SEA);
        int fishCount = 9 + this.random.nextInt(6);
        for (int i = 0; i < fishCount; ++i) this.spawnAtLocation(Items.COD);
        if (this.random.nextInt(3) == 0) this.spawnAtLocation(Items.DIAMOND);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_hit"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "seamonster_death"));
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return this.level().getEntitiesOfClass(SeaMonster.class,
                this.getBoundingBox().inflate(16.0, 5.0, 16.0)).size() <= 1;
    }
}
