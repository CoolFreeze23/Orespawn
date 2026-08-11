package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

public class Urchin extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_KYUUBI_LIVING = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "kyuubi_living"));
    private static final SoundEvent SND_GLASSHIT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glasshit"));
    private static final SoundEvent SND_GLASSDEAD = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "glassdead"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Urchin.class, EntityDataSerializers.INT);

    private static final double MOVE_SPEED = 0.3;

    /**
     * orig Urchin.java:46 {@code was_spawnered} — set when the spawn check passes
     * via the "Crystal Urchin" spawner bypass (orig :312); spawnered Urchins skip
     * the far-away despawn (orig :87-92) and the daytime discard (orig :94-107).
     * Not persisted in the original either.
     */
    private int wasSpawnered = 0;

    public Urchin(EntityType<? extends Urchin> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6484 — Urchin 25 HP / 10 ATK / 4 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.URCHIN.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.URCHIN.attackDamage())
                .add(Attributes.ARMOR, MobStats.URCHIN.armor());
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
    public boolean doHurtTarget(Entity target) {
        target.igniteForSeconds(5);
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.type().msgId().equals("cactus")) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.random.nextInt(3) == 1 && this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.75, this.getZ(),
                    0.0, this.random.nextFloat() / 10.0, 0.0);
            if (this.isInWater() && this.random.nextInt(5) == 1) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 1.75, this.getZ(),
                        0.0, this.random.nextFloat() / 10.0, 0.0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getX(), this.getY() + 2.0, this.getZ(),
                        0.0, this.random.nextFloat() / 10.0, 0.0);
            }
        }
        if (this.isInWater() && this.random.nextInt(5) == 1 && !this.level().isClientSide) {
            this.doHurtTarget(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        // orig Urchin.java:94-107 — daytime discard skipped when spawnered
        long timeOfDay = this.level().getDayTime() % 24000L;
        if (timeOfDay < 12000L && this.random.nextInt(400) == 1 && !this.level().isClientSide
                && this.wasSpawnered == 0) {
            this.discard();
        }
    }

    /** orig Urchin.java:87-92 — spawnered Urchins never despawn from distance. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.wasSpawnered == 0;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.random.nextInt(8) == 0) {
            LivingEntity target = this.getTarget();
            if (target == null) {
                Player nearest = this.level().getNearestPlayer(this, 16.0);
                if (nearest != null && !nearest.getAbilities().instabuild) {
                    target = nearest;
                    this.setTarget(target);
                }
            }
            if (target != null && target.isAlive()) {
                if (this.distanceToSqr(target) < 8.0) {
                    this.setAttacking(1);
                    if (this.random.nextInt(7) == 0) {
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SND_KYUUBI_LIVING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_GLASSHIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_GLASSDEAD;
    }

    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    /**
     * orig Urchin.java:298-332 — "Crystal Urchin" spawner bypass (x/z -2..+2,
     * y +1..+3, sets {@code was_spawnered}); >=6 air blocks in the 3x3 ring one
     * above the feet; darkness; night half of the day only.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level, -2, 2, 1, 3)) {
            this.wasSpawnered = 1;
            return true;
        }
        int sc = 0;
        BlockPos feet = this.blockPosition();
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (level.getBlockState(feet.offset(dx, 1, dz)).isAir()) sc++;
            }
        }
        if (sc < 6) return false;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        return level.dayTime() % 24000L >= 13000L;
    }
}
