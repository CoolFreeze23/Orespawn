package danger.orespawn.entity;

import danger.orespawn.MobStats;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.OreSpawnConfig;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Irukandji extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_LITTLE_SPLAT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "little_splat"));
    private static final SoundEvent SND_RATDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "ratdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Irukandji.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.15;
    private static final int NO_WATER_FOUND_SENTINEL = 99999;
    private static final float EMPTY_HAND_RETALIATION_DAMAGE = 200.0f;

    private int closestWaterDistanceSq = NO_WATER_FOUND_SENTINEL;
    private int targetX = 0;
    private int targetY = 0;
    private int targetZ = 0;

    public Irukandji(EntityType<? extends Irukandji> type, Level level) {
        super(type, level);
        this.xpReward = 50;
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
        // orig OreSpawnMain.java:6509 — Irukandji 1 HP / 20 ATK / 0 armor.
        // (The 200.0f is only the empty-hand retaliation, orig Irukandji.java:96.)
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.IRUKANDJI.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.IRUKANDJI.attackDamage())
                .add(Attributes.ARMOR, MobStats.IRUKANDJI.armor());
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
        Entity damager = source.getEntity();
        if (damager instanceof Irukandji) return false;
        if (damager instanceof Player player && player.getMainHandItem().isEmpty()) {
            player.hurt(this.damageSources().mobAttack(this), EMPTY_HAND_RETALIATION_DAMAGE);
            return false;
        }
        if (damager instanceof Mob mob) {
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
            this.closestWaterDistanceSq = NO_WATER_FOUND_SENTINEL;
            this.targetX = 0;
            this.targetY = 0;
            this.targetZ = 0;
            for (int i = 1; i < 12; ++i) {
                int j = Math.min(i, 5);
                if (this.scanForWater((int) this.getX(), (int) this.getY() - 1, (int) this.getZ(), i, j, i)) break;
                if (i >= 5) ++i;
            }
            if (this.closestWaterDistanceSq < NO_WATER_FOUND_SENTINEL) {
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
            // orig Irukandji.java:291-293 — findSomethingToAttack answers null under PlayNicely ahead of its stored-target
            // read (:299-302) and its scan (:304-309); the port's pick is this inline block, gated as a whole (ENT-S-115).
            boolean playNicely = OreSpawnConfig.PLAY_NICELY.get();
            LivingEntity target = playNicely ? null : this.getTarget();
            if (target == null && !playNicely) {
                Player nearest = this.level().getNearestPlayer(this, 6.0);
                // orig Irukandji.java:280-282 — canSee, the eye-to-eye block ray, ahead of the creative check (:283-286) (ENT-S-118)
                if (nearest != null && this.getSensing().hasLineOfSight(nearest) && !nearest.getAbilities().instabuild) {
                    target = nearest;
                    this.setTarget(target);
                }
            }
            if (target != null && target.isAlive()) {
                if (this.distanceToSqr(target) < 3.0) {
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
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWaterAt(x + dx, y + i, z + j, dx * dx + j * j + i * i);
                found += checkWaterAt(x - dx, y + i, z + j, dx * dx + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                found += checkWaterAt(x + i, y + dy, z + j, dy * dy + j * j + i * i);
                found += checkWaterAt(x + i, y - dy, z + j, dy * dy + j * j + i * i);
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                found += checkWaterAt(x + i, y + j, z + dz, dz * dz + j * j + i * i);
                found += checkWaterAt(x + i, y + j, z - dz, dz * dz + j * j + i * i);
            }
        }
        return found != 0;
    }

    private int checkWaterAt(int x, int y, int z, int dist) {
        BlockState state = this.level().getBlockState(new BlockPos(x, y, z));
        if (state.is(Blocks.WATER) && dist < this.closestWaterDistanceSq) {
            this.closestWaterDistanceSq = dist;
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            return 1;
        }
        return 0;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_LITTLE_SPLAT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_RATDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** orig Irukandji.java:326-337 — y>=50; daytime; 1-in-60 dice; at most 2 buddies within 16/8/16. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        if (this.random.nextInt(60) != 1) return false;
        return OriginalSpawnGates.countBuddies(this, level, Irukandji.class, 16.0, 8.0, 16.0) <= 2;
    }
}
