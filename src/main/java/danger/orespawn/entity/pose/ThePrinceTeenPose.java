package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;

/**
 * What {@code ModelThePrinceTeen.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hooks, the Slice 4b form of {@link OstrichPose} / {@link RotatorPose}): the activity (0 walking, 1 flying - the
 * wings' beat, the legs tucked, the flight yaw latch), the attacking flag (the fast wing / leg / tail rhythms, the
 * three jaws' chatter), the per-entity render scratch whose {@code rf1} accumulates the ridden-flight body-yaw delta
 * clamped to +-50 (orig ModelThePrinceTeen.java:669-678; ENT-S-093), the sit order that stills the tail ({@code
 * TamableAnimal.isOrderedToSit()}), the three heads' neck extensions in degrees, and the yaw pair the flight latch
 * differences ({@code Entity.getYRot()} and the {@code yRotO} field through the one-line {@code yRotO()}
 * delegate, the Ostrich form). The classic model and the GeckoLib hook both pose from this, so the parity harness
 * can drive them headlessly with a declared state instead of a live entity.
 */
public interface ThePrinceTeenPose {
    int getActivity();

    int getAttacking();

    RenderInfo getRenderInfo();

    boolean isOrderedToSit();

    int getHead1Ext();

    int getHead2Ext();

    int getHead3Ext();

    /** orig ModelThePrinceTeen.java:669 {@code field_70177_z} / the port's {@code Entity.getYRot()}: 0 at rest. */
    float getYRot();

    /** orig ModelThePrinceTeen.java:669 {@code field_70126_B} / the port's {@code Entity.yRotO} through a one-line delegate: 0 at rest. */
    float yRotO();
}
