package danger.orespawn.entity.ai;

import danger.orespawn.entity.CaveFisher;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy 1.7.10 Cave Fisher trip-wire / pulley AI.
 *
 * <p>Behaviour breakdown (matches 1.7.10):</p>
 * <ol>
 *   <li><b>Anchor.</b> When idle and underground, the Cave Fisher seeks
 *       the ceiling above its current position. If a solid block is found
 *       within {@link #MAX_CEILING_HEIGHT}, the entity teleports up to
 *       hang from it — disabling gravity until the ambush triggers.</li>
 *   <li><b>Ambush watch.</b> While anchored, the goal scans for a player
 *       directly below in a 4-block radius cylinder (mimicking the
 *       1.7.10 detection cone). If a player enters the cone, the goal
 *       drops the entity by re-enabling gravity and gives the
 *       {@code CaveFisher} an immediate target so {@code BugMeleeAttackGoal}
 *       can take over the moment the spider lands.</li>
 *   <li><b>Cooldown.</b> After a successful drop (or a failed ambush
 *       where the player walked away), the goal sits on a 200-tick
 *       cool-down before trying to anchor again — preventing oscillation
 *       between "climb up / fall down / climb up" loops on the same tick.</li>
 * </ol>
 *
 * <p>This goal does not interfere with the existing
 * {@link BugMeleeAttackGoal} — both can be in the goal selector at once.
 * The melee goal automatically takes priority once the Cave Fisher
 * acquires a target via {@link CaveFisher#setTarget(LivingEntity)}.</p>
 */
public class CaveFisherAmbushGoal extends Goal {

    /** Max distance above the spider that we'll look for a ceiling block. */
    private static final int MAX_CEILING_HEIGHT = 6;
    /** Horizontal radius (blocks) of the trip-wire detection cone. */
    private static final double TRIP_RADIUS = 4.0;
    /** Vertical depth (blocks) of the trip-wire detection cone below us. */
    private static final double TRIP_DEPTH = 8.0;
    /** Hard cool-down after each ambush, in server ticks. */
    private static final int RECOVERY_TICKS = 200;
    /**
     * Sky brightness gate: only ambush in genuine darkness so we don't
     * have Cave Fishers raining out of trees in broad daylight. 7 mirrors
     * the vanilla monster spawn-light threshold.
     */
    private static final int MAX_LIGHT_LEVEL = 7;

    private final CaveFisher fisher;
    @Nullable
    private BlockPos anchorPos = null;
    private int recoveryTicks = 0;
    private boolean anchored = false;

    public CaveFisherAmbushGoal(CaveFisher fisher) {
        this.fisher = fisher;
        // MOVE flag — once anchored, no other navigation goal should grab
        // the spider. JUMP is included so vanilla "jump out of water" can't
        // dislodge us mid-anchor.
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.fisher.getTarget() != null) return false;
        if (this.recoveryTicks > 0) {
            --this.recoveryTicks;
            return false;
        }
        // Daylight / on-surface Cave Fishers don't ambush — they stick to
        // the BugMeleeAttackGoal flow. Faithful to the 1.7.10 spawn gate
        // that already restricts them to Y <= 50.
        if (this.fisher.level().getMaxLocalRawBrightness(this.fisher.blockPosition()) > MAX_LIGHT_LEVEL) {
            return false;
        }
        return findCeiling() != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.fisher.getTarget() != null) {
            // Target acquired during ambush — drop and let melee take over.
            return false;
        }
        return this.anchored && this.anchorPos != null;
    }

    @Override
    public void start() {
        BlockPos ceiling = findCeiling();
        if (ceiling == null) return;
        // Hang one block below the solid ceiling — the entity AABB is 0.8 tall
        // so this leaves the spider visually pressed up against the underside.
        BlockPos hangPos = ceiling.below();
        this.fisher.moveTo(hangPos.getX() + 0.5, hangPos.getY(),
                hangPos.getZ() + 0.5, this.fisher.getYRot(), 0.0f);
        this.fisher.setNoGravity(true);
        this.fisher.setDeltaMovement(Vec3.ZERO);
        this.anchorPos = hangPos;
        this.anchored = true;
    }

    @Override
    public void stop() {
        this.fisher.setNoGravity(false);
        this.anchorPos = null;
        this.anchored = false;
        this.recoveryTicks = RECOVERY_TICKS;
    }

    @Override
    public void tick() {
        if (this.anchorPos == null || !this.anchored) return;

        // Stay glued to the anchor — without this, server-side physics or
        // a chunk reload can desync the spider into freefall while still
        // "anchored". Re-snapping each tick is cheap and keeps the visual
        // honest. We don't snap the X/Z so push/AI nudge isn't fought.
        this.fisher.setDeltaMovement(0.0, 0.0, 0.0);
        this.fisher.setPos(this.anchorPos.getX() + 0.5,
                this.anchorPos.getY(),
                this.anchorPos.getZ() + 0.5);

        // Trip-wire scan — look for any player below us in a tight cone.
        AABB cone = new AABB(
                this.fisher.getX() - TRIP_RADIUS,
                this.fisher.getY() - TRIP_DEPTH,
                this.fisher.getZ() - TRIP_RADIUS,
                this.fisher.getX() + TRIP_RADIUS,
                this.fisher.getY(),
                this.fisher.getZ() + TRIP_RADIUS);

        // getNearestPlayer's predicate is typed against Entity, so we
        // re-narrow to Player here to safely peek at creative-mode abilities.
        Player victim = this.fisher.level().getNearestPlayer(
                this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(),
                TRIP_RADIUS + 1.0,
                e -> e instanceof Player pl
                        && cone.contains(pl.getX(), pl.getY(), pl.getZ())
                        && !pl.getAbilities().instabuild);
        if (victim != null) {
            // Drop! Clear gravity-disable, mark target, give a tiny down-kick
            // so the spider visibly leaps off the ceiling instead of just
            // floating. The melee goal takes over from here.
            this.fisher.setTarget(victim);
            this.fisher.setNoGravity(false);
            this.fisher.setDeltaMovement(0.0, -0.4, 0.0);
            this.anchored = false;
            this.recoveryTicks = RECOVERY_TICKS;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Walks straight up from the spider's current position looking for the
     * first solid block. Returns the position of the solid block, or null
     * if none was found within {@link #MAX_CEILING_HEIGHT}.
     */
    @Nullable
    private BlockPos findCeiling() {
        BlockPos here = this.fisher.blockPosition();
        for (int up = 1; up <= MAX_CEILING_HEIGHT; up++) {
            BlockPos probe = here.above(up);
            if (this.fisher.level().getBlockState(probe).isFaceSturdy(
                    this.fisher.level(), probe, Direction.DOWN)) {
                // Need at least one air block beneath the ceiling so the
                // hanging spider doesn't get stuck inside the block itself.
                BlockPos hang = probe.below();
                if (this.fisher.level().getBlockState(hang).isAir()) {
                    return probe;
                }
            }
        }
        return null;
    }

    /**
     * Convenience for entities that want to abort an ambush early (e.g.
     * if hit by a projectile while anchored). Cleanly resets the gravity
     * override even when the goal isn't currently the active one.
     *
     * @param entity the spider whose anchor should be released
     */
    public static void abortAnchor(Entity entity) {
        if (entity instanceof CaveFisher cf) {
            cf.setNoGravity(false);
        }
    }
}
