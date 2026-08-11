package danger.orespawn.entity;

import danger.orespawn.MobStats;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ai.GenericTargetSorter;

public class Alien extends Monster {
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Alien.class, EntityDataSerializers.INT);

    private final Comparator<Entity> targetSorter;
    private static final double MOVE_SPEED = 0.65;
    private static final double KNOCKBACK_HORIZONTAL = 1.1;
    private static final double KNOCKBACK_VERTICAL = 0.1;
    private static final double PLAYER_KNOCKBACK_VERTICAL_MULTIPLIER = 2.0;
    private static final double JUMP_BOOST = 0.25; // orig Alien.java:102 — motionY += 0.25 after super.jump()

    public Alien(EntityType<? extends Alien> type, Level level) {
        super(type, level);
        this.xpReward = 100;
        // orig Alien.java:41,59 — target priority uses the shared
        // GenericTargetSorter (creepers and large targets outrank nearer
        // small ones), not plain distance (TF-035).
        this.targetSorter = new GenericTargetSorter(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // orig Alien.java:61
        // orig Alien.java:62 — EntityAIMoveThroughVillage(1.0, false) @1.
        // Mapping decision: the 1.7.10 goal walked the door list of the
        // pre-1.14 village object; that framework is gone and vanilla
        // MoveThroughVillageGoal is its direct descendant driven by village
        // POI sections. Same speed (1.0), same day-and-night operation (orig
        // third arg false = not nocturnal-only), vanilla-standard
        // distanceToPoi 4 (the param has no 1.7.10 analog), no door-breaking
        // (the 1.7.10 goal never broke doors).
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        // orig Alien.java:328-343 — torch hunt lived in func_70619_bc,
        // outside the 1.7.10 task-mutex system; the goal is flagless for the
        // same reason and self-gates on the orig dice (see AlienTorchSeekGoal).
        this.goalSelector.addGoal(1, new AlienTorchSeekGoal(this));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 10, 1.0)); // orig Alien.java:63
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f)); // orig Alien.java:64
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this)); // orig Alien.java:65
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this)); // orig Alien.java:66
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig OreSpawnMain.java:6491 — Alien 100 HP / 12 ATK / 8 armor
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MobStats.ALIEN.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, MobStats.ALIEN.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, MobStats.ALIEN.armor());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    // orig Alien.java:100-103 (func_70664_aZ) — the original Alien boosts
    // every jump by +0.25 vertical; this is not a port invention (ENT-A-007).
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        Vec3 delta = this.getDeltaMovement();
        this.setDeltaMovement(delta.x, delta.y + JUMP_BOOST, delta.z);
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
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        super.tick();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien_living"));
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien_hurt"));
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "alien_death"));
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                if (this.getRandom().nextInt(5) == 1) {
                    // orig Alien.java:200-210 — POISON (field_76436_u, not Hunger as
                    // the audit claimed) for var2*5 ticks. var2 is 6 except on Easy
                    // where it is 8; the orig Normal/Hard branches are nested inside
                    // the Easy check and therefore unreachable (quirk kept).
                    int durationBase = this.level().getDifficulty() == Difficulty.EASY ? 8 : 6;
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, durationBase * 5, 0));
                }
                double verticalKnockback = KNOCKBACK_VERTICAL;
                float yawToTarget = (float) Math.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
                if (target instanceof Player) {
                    verticalKnockback *= PLAYER_KNOCKBACK_VERTICAL_MULTIPLIER;
                }
                target.push(
                        Math.cos(yawToTarget) * KNOCKBACK_HORIZONTAL,
                        verticalKnockback,
                        Math.sin(yawToTarget) * KNOCKBACK_HORIZONTAL);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // orig Alien.java:228-230 — cactus damage is ignored outright.
        if (source.getMsgId().equals("cactus")) {
            return false;
        }
        // orig Alien.java:231-233 — hurt_timer is declared 0 (:43) and decremented
        // (:311-313) but never set above zero anywhere, so the gate always passes:
        // damage lands on EVERY hit; there is no retaliation cooldown (TF-036).
        boolean ret = super.hurt(source, amount);
        // orig Alien.java:234-239 — every hurt retaliates: target the attacker,
        // navigate to it at 1.2, and force a true return. The orig instanceof is
        // EntityLiving (the Mob base class), which excludes players; player
        // attackers are handled by HurtByTargetGoal (orig :66) as before.
        // (orig :236 func_70784_b set the legacy EntityCreature "target" field,
        // which has no modern analog.)
        if (source.getEntity() instanceof Mob attacker) {
            this.setTarget(attacker);
            this.getNavigation().moveTo(attacker, 1.2);
            return true;
        }
        return ret;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) return;
        super.customServerAiStep();

        if (this.getRandom().nextInt(8) == 0) {
            LivingEntity target = findSomethingToAttack();
            if (target != null) {
                this.lookAt(target, 10.0f, 10.0f);
                if (this.distanceToSqr(target) < 16.0) {
                    this.setAttacking(1);
                    // orig Alien.java:320 — two swing dice, nextInt(4)==0 OR
                    // nextInt(5)==1 (~40% per attack tick, not 25%).
                    if (this.getRandom().nextInt(4) == 0 || this.getRandom().nextInt(5) == 1) {
                        this.doHurtTarget(target);
                    }
                }
                this.getNavigation().moveTo(target, 1.2);
            } else {
                this.setAttacking(0);
            }
        }

        if (this.getRandom().nextInt(40) == 1 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
        }
    }

    private LivingEntity findSomethingToAttack() {
        // orig Alien.java:367-369 — PlayNicely disables target acquisition
        // entirely, even when a live target is already held.
        if (OreSpawnConfig.PLAY_NICELY.get()) return null;
        LivingEntity current = this.getTarget();
        if (current != null && current.isAlive()) return current;
        this.setTarget(null);

        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0, 4.0, 12.0));
        list.sort(this.targetSorter);
        for (LivingEntity candidate : list) {
            if (isSuitableTarget(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSuitableTarget(LivingEntity target) {
        if (target == null || target == this || !target.isAlive()) return false;
        if (target instanceof Player player) return !player.getAbilities().instabuild;
        return false;
    }

    /** orig Alien.java:397-434 — spawner bypass; darkness; Islands always allowed; y<=50; 3x3x3 clear air above. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        if (OriginalSpawnGates.nearOwnSpawner(this, level)) return true;
        if (!OriginalSpawnGates.isDarkEnough(this, level)) return false;
        if (danger.orespawn.ModDimensionKeys.isIn(level, danger.orespawn.ModDimensionKeys.ISLANDS)) return true;
        if (this.getY() > 50.0) return false;
        return OriginalSpawnGates.airBox(this, level, -1, 1, 1, 3, -1, 1);
    }
}
