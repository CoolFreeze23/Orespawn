package danger.orespawn.entity.ai;

import java.util.Comparator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Port of the original GenericTargetSorter (orig GenericTargetSorter.java) —
 * the shared "who do I attack first" comparator of the custom-AI mobs.
 * Effective distance-squared is halved for Creepers (orig :21-23) and divided
 * by the target's silhouette area ({@code bbHeight * bbWidth}) whenever that
 * exceeds 1 (orig :24-26), so creepers and physically large targets outrank
 * closer small ones.
 *
 * <p>TF-035: the port had replaced this with plain distance sorting at every
 * call site. The class is restored here with the Vortex as the reference
 * migration; the remaining call sites migrate as their Phase E4 category
 * batches verify each entity (swapping a plain
 * {@code Comparator.comparingDouble(this::distanceToSqr)} for
 * {@code new GenericTargetSorter(this)}).</p>
 */
public class GenericTargetSorter implements Comparator<Entity> {

    private final Entity perspective;

    public GenericTargetSorter(Entity perspective) {
        this.perspective = perspective;
    }

    @Override
    public int compare(Entity first, Entity second) {
        return Double.compare(weightedDistanceSq(first), weightedDistanceSq(second));
    }

    /** orig GenericTargetSorter.java:19-27, applied symmetrically to both operands. */
    private double weightedDistanceSq(Entity target) {
        double distanceSq = this.perspective.distanceToSqr(target);
        if (target instanceof Creeper) {
            distanceSq /= 2.0;
        }
        double silhouette = target.getBbHeight() * target.getBbWidth();
        if (silhouette > 1.0) {
            distanceSq /= silhouette;
        }
        return distanceSq;
    }
}
