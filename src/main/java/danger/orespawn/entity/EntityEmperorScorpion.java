package danger.orespawn.entity;

import danger.orespawn.MobStats;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.EmperorScorpionPoisonGoal;
import danger.orespawn.util.MyUtils;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityEmperorScorpion extends Monster {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_ALO_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alo_hurt"));
    private static final SoundEvent SND_EMPERORSCORPION_DEATH = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "emperorscorpion_death"));
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(EntityEmperorScorpion.class, EntityDataSerializers.INT);

    private int hurtTimer = 0;
    private int healTimer = 0;

    /**
     * Per-entity render scratch (orig EmperorScorpion.java:53 {@code renderdata = new RenderInfo()},
     * re-created :65, zeroed :77-87, accessor :110-112). Mutated client-side by
     * {@code EmperorScorpionModel} for the claw/tail selector latch
     * (orig ModelEmperorScorpion.java:613-641); never datawatcher-synced. ENT-S-093.
     */
    private final danger.orespawn.entity.client.RenderInfo renderInfo =
            new danger.orespawn.entity.client.RenderInfo();

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
        // orig EmperorScorpion.java:473-475 — the shared ignore screen (there it follows
        // the line-of-sight check, :470; both are side-effect-free, so the goal's
        // predicate-before-sight order yields the same answer) (ENT-S-106).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                target -> !MyUtils.isIgnoreable(target)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6488 — EmperorScorpion 350 HP / 35 ATK / 20 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.EMPEROR_SCORPION.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, MobStats.EMPEROR_SCORPION.attackDamage())
                .add(Attributes.ARMOR, MobStats.EMPEROR_SCORPION.armor())
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

    /** Mirrors orig EmperorScorpion.java:110-112 {@code getRenderInfo()}. ENT-S-093. */
    public danger.orespawn.entity.client.RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_ALO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_EMPERORSCORPION_DEATH;
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

        // Baby-scorpion summon — orig EmperorScorpion.java:408,437-438: inside a
        // nextInt(4)==0 AI gate, while a combat target exists, a nextInt(20)==1
        // roll spawns one Scorpion at the midpoint between Emperor and target
        // (±nextInt(5) horizontal jitter, y midpoint +1.01). No population cap,
        // no cooldown, no ground check in the original.
        if (this.random.nextInt(4) == 0) {
            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.random.nextInt(20) == 1) {
                spawnBabyScorpionToward(target);
            }
        }
    }

    /** orig EmperorScorpion.java:437-438 — midpoint spawn between self and target. */
    private void spawnBabyScorpionToward(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server)) return;
        EntityScorpion minion = ModEntities.ENTITY_SCORPION.get().create(server);
        if (minion == null) return;
        double x = (this.getX() + target.getX()) / 2.0
                + this.random.nextInt(5) - this.random.nextInt(5);
        double y = (this.getY() + target.getY()) / 2.0 + 1.01;
        double z = (this.getZ() + target.getZ()) / 2.0
                + this.random.nextInt(5) - this.random.nextInt(5);
        minion.moveTo(x, y, z, this.random.nextFloat() * 360.0f, 0.0f);
        minion.finalizeSpawn(server, server.getCurrentDifficultyAt(minion.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(minion);
    }

    // Death drops are fully data-driven via loot_table/entities/emperor_scorpion.json
    // (orig EmperorScorpion.java:181-347: scale, painting, 4-8 obsidian,
    // 4-11 raw beef, 1-5 rolls of the d20 Ultimate/Diamond gear table).

    /** orig EmperorScorpion.java:529-559 — combined scan of x/z -2..+1, y +2..+4: own spawner anywhere in the box passes, any non-air block fails; then darkness; night; y>=50; no other EmperorScorpion within 20/6/20. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        net.minecraft.core.BlockPos feet = this.blockPosition();
        for (int dz = -2; dz <= 1; dz++) {
            for (int dx = -2; dx <= 1; dx++) {
                for (int dy = 2; dy <= 4; dy++) {
                    net.minecraft.core.BlockPos p = feet.offset(dx, dy, dz);
                    if (OriginalSpawnGates.isOwnSpawner(this, level, p)) return true;
                    if (!level.getBlockState(p).isAir()) return false;
                }
            }
        }
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (OriginalSpawnGates.isDaytime(level)) return false;
        if (this.getY() < 50.0) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EntityEmperorScorpion.class, 20.0, 6.0, 20.0);
    }
}
