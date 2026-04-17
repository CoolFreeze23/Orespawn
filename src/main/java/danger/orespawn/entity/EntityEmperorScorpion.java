package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.EmperorScorpionPoisonGoal;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityEmperorScorpion extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityEmperorScorpion.class, EntityDataSerializers.INT);

    private int hurtTimer = 0;
    private int healTimer = 0;
    /**
     * Throttles the summoner-aura check so it only fires every
     * {@link #SUMMONER_PERIOD_TICKS} ticks instead of every server tick. The
     * 1.7.10 source ran the check inside {@code customServerAiStep} on a
     * {@code rand.nextInt(80) == 1} dice — averaging once per ~4 seconds.
     * We use a hard cooldown counter for predictability and so we never
     * thrash the entity-cramming logic in heavily-populated badlands.
     */
    private int summonerCooldown = 0;

    /** Aura cadence (server ticks). 30 ticks = 1.5s avg, with jitter below. */
    private static final int SUMMONER_PERIOD_TICKS = 30;
    /** Random jitter added on top of {@link #SUMMONER_PERIOD_TICKS}. */
    private static final int SUMMONER_JITTER_TICKS = 10;
    /** Horizontal aura radius — Scorpions outside this don't count. */
    private static final double SUMMONER_RADIUS = 16.0;
    /** Below this many minions the aura tries to spawn a replacement. */
    private static final int SUMMONER_MIN_MINIONS = 3;
    /** Hard cap on the aura's local population — avoids cramming runaway. */
    private static final int SUMMONER_MAX_MINIONS = 6;

    public EntityEmperorScorpion(EntityType<? extends EntityEmperorScorpion> type, Level level) {
        super(type, level);
        this.xpReward = 200;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // EmperorScorpionPoisonGoal extends BugMeleeAttackGoal and layers in
        // the 1/3-chance 90-tick poison effect that the 1.7.10 source applied
        // inside doHurtTarget. Moving it into the goal keeps doHurtTarget
        // focused on knockback, and ensures the poison only lands when the
        // attack actually connects (respects invulnerability frames).
        this.goalSelector.addGoal(1, new EmperorScorpionPoisonGoal(this, this::setAttacking));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y + 0.25, motion.z);
        this.setPos(this.getX(), this.getY() + 0.5, this.getZ());
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity) {
                double knockbackStrength = 3.0;
                double verticalKnockback = 0.2;
                float angle = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target.isRemoved() || target instanceof Player) verticalKnockback *= 2.0;
                target.push(Math.cos(angle) * knockbackStrength, verticalKnockback, Math.sin(angle) * knockbackStrength);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.hurtTimer > 0) return false;
        boolean ret = super.hurt(source, amount);
        this.hurtTimer = 30;
        Entity attacker = source.getEntity();
        if (attacker instanceof Mob mob) {
            this.setTarget(mob);
            this.getNavigation().moveTo(mob, 1.2);
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();
        if (this.hurtTimer > 0) --this.hurtTimer;
        // Slow regen — preserved from 1.7.10 (~1-in-100 tick roll).
        if (++this.healTimer >= 100 && this.getHealth() < this.getMaxHealth()) {
            this.healTimer = 0;
            if (this.random.nextInt(100) == 1) this.heal(2.0f);
        }

        // Summoner aura — see field-level docs on summonerCooldown.
        if (this.summonerCooldown > 0) {
            --this.summonerCooldown;
        } else {
            this.summonerCooldown = SUMMONER_PERIOD_TICKS
                    + this.random.nextInt(SUMMONER_JITTER_TICKS);
            tryReplenishMinions();
        }
    }

    /**
     * Counts nearby standard Scorpions and, if the population has dropped
     * below {@link #SUMMONER_MIN_MINIONS}, spawns one replacement at a
     * random reachable air block adjacent to this Emperor. Hard-capped at
     * {@link #SUMMONER_MAX_MINIONS} so a player who refuses to engage the
     * aura cannot create an infinite Scorpion swarm.
     */
    private void tryReplenishMinions() {
        if (!(this.level() instanceof ServerLevel server)) return;

        AABB box = this.getBoundingBox().inflate(SUMMONER_RADIUS, 8.0, SUMMONER_RADIUS);
        List<EntityScorpion> nearby = server.getEntitiesOfClass(EntityScorpion.class, box);
        if (nearby.size() >= SUMMONER_MIN_MINIONS) return;
        if (nearby.size() >= SUMMONER_MAX_MINIONS) return;

        BlockPos spawnAt = pickSpawnPos();
        if (spawnAt == null) return;

        EntityScorpion minion = ModEntities.ENTITY_SCORPION.get().create(server);
        if (minion == null) return;
        minion.moveTo(spawnAt.getX() + 0.5, spawnAt.getY(), spawnAt.getZ() + 0.5,
                this.random.nextFloat() * 360.0f, 0.0f);
        minion.finalizeSpawn(server, server.getCurrentDifficultyAt(spawnAt),
                MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(minion);
    }

    /**
     * Finds an air block within ±3 of the Emperor whose underside is solid.
     * Returns null if no such position exists in 6 attempts (we don't want
     * a tight loop that spirals out indefinitely each aura tick).
     */
    @Nullable
    private BlockPos pickSpawnPos() {
        for (int attempt = 0; attempt < 6; attempt++) {
            int dx = this.random.nextInt(7) - 3;
            int dz = this.random.nextInt(7) - 3;
            int dy = this.random.nextInt(3) - 1;
            BlockPos candidate = this.blockPosition().offset(dx, dy, dz);
            if (this.level().getBlockState(candidate).isAir()
                    && this.level().getBlockState(candidate.above()).isAir()
                    && !this.level().getBlockState(candidate.below()).isAir()) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(Items.NAME_TAG);
        int count = 4 + this.random.nextInt(8);
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(Items.BONE);
        }
    }
}
