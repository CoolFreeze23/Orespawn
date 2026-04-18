package danger.orespawn.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Phase 10 — Pointysaurus eye-contact aggression. Modelled on vanilla
 * {@code EndermanLookForPlayerGoal}: scans for the nearest player inside the
 * Pointysaurus's follow range, then promotes that player to {@code target}
 * only if they are actively looking at the dino's head box. Once provoked,
 * the dino stays angry until it loses sight (handled by the existing
 * {@link net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal}
 * downstream).
 *
 * The 1.7.10 Pointysaurus had no such mechanic — its aggression was purely
 * proximity / damage-based — so this is a modern enhancement layered on top
 * to give it the "you angered the predator by making eye contact" feel the
 * Phase 10 brief asked for.
 */
public class PointysaurusStareGoal extends Goal {

    private final Mob mob;
    private final TargetingConditions conditions;
    private Player target;
    private int aggroTimer;

    private static final double STARE_RADIUS = 32.0;
    private static final double DOT_THRESHOLD = 0.97;

    public PointysaurusStareGoal(Mob mob) {
        this.mob = mob;
        this.conditions = TargetingConditions.forCombat()
                .range(STARE_RADIUS)
                .selector(this::isAngryAt);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    private boolean isAngryAt(net.minecraft.world.entity.LivingEntity living) {
        return living instanceof Player p && this.isLookingAtMe(p);
    }

    private boolean isLookingAtMe(Player player) {
        if (player.isCreative() || player.isSpectator()) return false;
        Vec3 viewVec = player.getViewVector(1.0F).normalize();
        Vec3 toMob = new Vec3(this.mob.getX() - player.getX(),
                this.mob.getEyeY() - player.getEyeY(),
                this.mob.getZ() - player.getZ());
        double distance = toMob.length();
        if (distance > STARE_RADIUS) return false;
        Vec3 toMobNorm = toMob.normalize();
        double dot = viewVec.dot(toMobNorm);
        if (dot <= DOT_THRESHOLD) return false;
        return this.mob.hasLineOfSight(player);
    }

    @Override
    public boolean canUse() {
        this.target = this.mob.level().getNearestPlayer(this.conditions, this.mob);
        return this.target != null;
    }

    @Override
    public void start() {
        this.aggroTimer = this.adjustedTickDelay(5);
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target == null) return false;
        if (!this.target.isAlive()) return false;
        if (this.target.distanceToSqr(this.mob) > STARE_RADIUS * STARE_RADIUS) return false;
        return this.isLookingAtMe(this.target);
    }

    @Override
    public void tick() {
        if (this.target == null) return;
        this.mob.getLookControl().setLookAt(this.target.getX(),
                this.target.getEyeY(), this.target.getZ());
        if (this.aggroTimer-- <= 0 && this.mob.getTarget() != this.target) {
            // Lock the staring player as the combat target. The downstream
            // NearestAttackableTargetGoal/HurtByTargetGoal will keep us angry
            // at them even after they break eye contact.
            this.mob.setTarget(this.target);
        }
    }
}
