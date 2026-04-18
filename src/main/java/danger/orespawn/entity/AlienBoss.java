package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Alien Boss — the leader of the WTF-Alien Dungeon. The 1.7.10 source did not
 * expose a separate "Alien Boss" entity; danger's `Alien.java` was the only
 * registration. We promote a beefier subclass here for the Phase 12 dungeon
 * cap so the WTF chamber has a single high-health summoner gating its loot
 * (matches the design of legacy boss-cap dungeons such as the Mantis Lair
 * or Sea Monster's Dungeon).
 *
 * <p>Stat scaling: roughly 4× HP, 2× attack, 1.5× hitbox of the standard
 * {@link Alien}. Adds an "Alien-spawn aura" that summons up to 3 standard
 * Aliens around itself when its target is alive — gated to once every
 * 10 seconds (200 ticks) so it cannot ever exceed the per-chunk mob cap and
 * never re-summons during transient damage events.</p>
 *
 * <p>Inherits the parent's torch-destruction behaviour via the
 * {@link AlienTorchSeekGoal} attached at registerGoals().</p>
 */
public class AlienBoss extends Alien {
    private static final int MAX_HEALTH = 320;
    private static final int ATTACK_DAMAGE = 24;
    private static final double MOVE_SPEED = 0.55;
    private static final int SUMMON_INTERVAL_TICKS = 200;
    private static final int MAX_NEARBY_ALIENS = 3;
    private static final double SUMMON_RADIUS = 12.0;

    private int summonCooldown = 0;

    public AlienBoss(EntityType<? extends Alien> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living && this.getRandom().nextInt(3) == 0) {
            // Heavier wither bite vs the parent Alien's hunger nibble.
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        }
        return hit;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.summonCooldown > 0) {
            this.summonCooldown--;
            return;
        }
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.getTarget() == null || !this.getTarget().isAlive()) return;

        int nearby = countNearbyAliens();
        if (nearby >= MAX_NEARBY_ALIENS) {
            // Reset cooldown lightly so we re-check sooner once they die.
            this.summonCooldown = 60;
            return;
        }

        BlockPos myPos = this.blockPosition();
        BlockPos spawnPos = myPos.offset(
                this.getRandom().nextInt(5) - 2, 1,
                this.getRandom().nextInt(5) - 2);

        Alien minion = ModEntities.ALIEN.get().create(server);
        if (minion == null) return;
        minion.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                this.getRandom().nextFloat() * 360f, 0f);
        minion.finalizeSpawn(server, server.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.MOB_SUMMONED, null);
        minion.setTarget(this.getTarget());
        server.addFreshEntity(minion);

        this.summonCooldown = SUMMON_INTERVAL_TICKS;
    }

    private int countNearbyAliens() {
        AABB box = this.getBoundingBox().inflate(SUMMON_RADIUS);
        return this.level().getEntitiesOfClass(Alien.class, box,
                a -> a != this && a.isAlive()).size();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Boss is immune to its own minions' poison/wither pings.
        if (source.getEntity() instanceof Alien alien && !(alien instanceof AlienBoss)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        // Boss is persistent — it must guard the dungeon room until killed.
        return false;
    }
}
