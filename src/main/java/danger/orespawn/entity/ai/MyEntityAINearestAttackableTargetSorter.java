package danger.orespawn.entity.ai;

import java.util.Comparator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Port of the original {@code MyEntityAINearestAttackableTargetSorter} (orig MyEntityAINearestAttackableTargetSorter.java) —
 * the order the Boyfriend's and Girlfriend's own target task walked its box scan in (orig
 * MyEntityAINearestAttackableTarget.java:38 the construction, :57 the {@code Collections.sort}): the candidate's distance² from
 * the task owner (orig :22 / :26, {@code func_70068_e}), halved for an {@code EntityCreeper} (orig :23-25 / :27-29), compared
 * (:30). It has NO silhouette term — {@link GenericTargetSorter}'s division by a {@code height * width} over 1 (orig
 * GenericTargetSorter.java:24-26) is absent here — so among non-creepers the task took the plain nearest, and a creeper
 * outranked a non-creeper up to √2 times nearer. Ties compare 0 (orig :30), so a stable sort kept the list's order —
 * {@code TargetSelection.firstMatch} keeps it the same way.
 *
 * <p>{@code MyValentineTarget} (orig :41 / :61) sorted its copy of the scan with {@code MyValentineTargetSorter} — plain
 * distance², no creeper term (MyValentineTargetSorter.java:20-24) — which the Girlfriend's Valentine subclass of
 * {@link MyEntityAINearestAttackableTargetGoal} supplies through {@code targetOrder()}. ENT-S-139 (targeting ledger batch T4,
 * §(v)).</p>
 */
public class MyEntityAINearestAttackableTargetSorter implements Comparator<Entity> {

    /** orig :13 {@code theEntity} — the task owner the distances are measured from (:17). */
    private final Entity theEntity;

    public MyEntityAINearestAttackableTargetSorter(Entity theEntity) {
        this.theEntity = theEntity;
    }

    /** orig :21-31 {@code compareDistanceSq}, applied symmetrically to both operands (as the port's GenericTargetSorter does). */
    @Override
    public int compare(Entity first, Entity second) {
        return Double.compare(weightedDistanceSq(first), weightedDistanceSq(second));
    }

    /** orig :22-25 (and :26-29 for the second operand): distance² from the owner, halved for a creeper; no silhouette term. */
    private double weightedDistanceSq(Entity target) {
        double distanceSq = this.theEntity.distanceToSqr(target);
        if (target instanceof Creeper) {
            distanceSq /= 2.0;
        }
        return distanceSq;
    }
}
