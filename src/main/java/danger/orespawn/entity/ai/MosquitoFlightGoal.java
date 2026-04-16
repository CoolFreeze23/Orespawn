package danger.orespawn.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Mosquito flight behaviour.
 *
 * <p>Extends the generic {@link AmbientFlightGoal} with an occasional
 * "seek a player to harass" branch, matching the 1.7.10 inline logic in
 * {@code EntityMosquito.customServerAiStep()} — ~1 in 10 retargets pulls
 * the mosquito toward the nearest player within 16 blocks, with the rest
 * being normal random wandering. Keeps the main-thread cost down because
 * {@code getNearestPlayer} is only called during retarget events (once
 * every ~100 ticks on average).
 */
public class MosquitoFlightGoal extends AmbientFlightGoal {
    private static final double PLAYER_SEEK_RANGE = 16.0;
    private static final int SEEK_PLAYER_RARE_CHANCE = 10;

    public MosquitoFlightGoal(Mob mob) {
        super(mob, Params.mosquito());
    }

    @Override
    protected BlockPos pickRetarget() {
        if (this.mob.getRandom().nextInt(SEEK_PLAYER_RARE_CHANCE) == 0) {
            Player nearest = this.mob.level().getNearestPlayer(
                    this.mob, PLAYER_SEEK_RANGE);
            if (nearest != null) {
                return new BlockPos(
                        (int) nearest.getX(),
                        (int) nearest.getY() + 2,
                        (int) nearest.getZ());
            }
        }
        return super.pickRetarget();
    }
}
