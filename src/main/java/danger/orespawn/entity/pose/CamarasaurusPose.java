package danger.orespawn.entity.pose;

/**
 * What {@code ModelCamarasaurus.poseFrom} reads from its entity, as an interface the entity already satisfies (the
 * hooks, the Slice 4b form of {@link Robot2Pose}): the health fraction that drives the tail's frequency AND amplitude
 * - orig ModelCamarasaurus.java:184 {@code hf = (float)c.getCamarasaurusHealth() / c.func_110138_aP()} (the port's
 * ModelCamarasaurus.poseFrom {@code hf = (float)entity.getHealth() / entity.getMaxHealth()}; orig :186's sitting check is
 * the port's {@code if (false)}, kept as the port has it). Both getters are {@code LivingEntity}'s own.
 * The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly with
 * a declared state instead of a live entity.
 */
public interface CamarasaurusPose {
    /** orig ModelCamarasaurus.java:184 {@code getCamarasaurusHealth()}; rest = the full health (the fraction 1). */
    float getHealth();

    /** orig ModelCamarasaurus.java:184 {@code func_110138_aP()} (getMaxHealth). */
    float getMaxHealth();
}
