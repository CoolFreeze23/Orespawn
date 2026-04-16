package danger.orespawn.entity.ai;

import java.util.function.IntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Acid;

/**
 * Spit Bug's 1.7.10 ranged "water-canon" attack — when a target was too far
 * for melee, the bug fired an 8-round burst of {@link Acid} projectiles (one
 * per tick) aimed slightly ahead of the target to compensate for ballistic
 * drop. The 1.21.1 port previously lacked this attack (it only melee'd,
 * which made Spit Bug identical in feel to a regular Trooper Bug).
 *
 * <p>We restore the burst here. {@link #onOutOfMeleeRange(LivingEntity, double)}
 * implements the stream cadence and projectile spawning, matching the 1.7.10
 * {@code watercanon()} math:
 * <pre>
 *   var9 = sqrt(dx*dx + dz*dz) * 0.2f   // ballistic lift
 *   acid.shoot(dx, dy + var9, dz, 1.1f, 6.0f)
 * </pre>
 * The {@code 1.1f} velocity and {@code 6.0f} inaccuracy are preserved.
 *
 * <p>The 1-in-7 roll (after stream exhausts) that starts a new burst is
 * preserved. We gate the whole goal on a maximum engagement distance so
 * the bug doesn't spam acid across the entire view distance.
 */
public class SpitBugAcidAttackGoal extends BugMeleeAttackGoal {

    private static final double MAX_ENGAGEMENT_RANGE_SQ = 20.0 * 20.0;
    private static final double VERTICAL_MOUTH_OFFSET = 1.5;
    private static final double HORIZONTAL_MOUTH_OFFSET = 1.5;
    private static final float PROJECTILE_VELOCITY = 1.1f;
    private static final float PROJECTILE_INACCURACY = 6.0f;
    private static final int STREAM_BURST_COUNT = 8;
    private static final int STREAM_RESET_DICE = 7;

    private int streamCount;

    public SpitBugAcidAttackGoal(Mob mob, IntConsumer setAttacking) {
        super(mob, setAttacking, Params.spitBug());
    }

    @Override
    protected void onOutOfMeleeRange(LivingEntity target, double distSq) {
        if (distSq > MAX_ENGAGEMENT_RANGE_SQ) return;
        if (this.streamCount > 0) {
            this.fireOneAcidRound(target);
            --this.streamCount;
            this.setAttacking.accept(1);
        } else if (this.mob.getRandom().nextInt(STREAM_RESET_DICE) == 1) {
            this.streamCount = STREAM_BURST_COUNT;
        }
    }

    private void fireOneAcidRound(LivingEntity target) {
        if (this.mob.level().isClientSide) return;

        double yawRad = Math.toRadians(this.mob.yHeadRot);
        double pitchRad = Math.toRadians(this.mob.getXRot());

        Acid acid = new Acid(this.mob.level(), this.mob);
        double muzzleX = this.mob.getX() - HORIZONTAL_MOUTH_OFFSET * Mth.sin((float) yawRad);
        double muzzleY = this.mob.getY() + VERTICAL_MOUTH_OFFSET;
        double muzzleZ = this.mob.getZ() + HORIZONTAL_MOUTH_OFFSET * Mth.cos((float) yawRad);
        acid.moveTo(muzzleX, muzzleY, muzzleZ, (float) yawRad, (float) pitchRad);

        double dx = target.getX() - muzzleX;
        double dy = target.getY() + 0.25 - muzzleY;
        double dz = target.getZ() - muzzleZ;
        float ballisticLift = Mth.sqrt((float) (dx * dx + dz * dz)) * 0.2f;

        acid.shoot(dx, dy + ballisticLift, dz, PROJECTILE_VELOCITY, PROJECTILE_INACCURACY);

        this.mob.level().addFreshEntity(acid);

        BlockPos at = this.mob.blockPosition();
        this.mob.level().playSound(null, at,
                SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "clatter")),
                this.mob.getSoundSource(),
                0.75f,
                1.0f / (this.mob.getRandom().nextFloat() * 0.4f + 0.8f));
    }
}
