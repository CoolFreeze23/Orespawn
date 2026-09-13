package danger.orespawn.entity.pose;

/**
 * What {@code ModelThePrinceAdult.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * hooks, owner 2026-09-14, addendum item 10; the Slice 4b form of {@link HerculesBeetlePose}): the attacking flag (the
 * fast wing / claw / leg / tail rhythms, the heads' sweeps and jaw snaps), the activity (0 walking: the wings and legs
 * scaled by the walking speed and the heads on the head look; otherwise flying: the idle sweeps), the sit order that
 * stills the wings, the legs and the tail ({@code TamableAnimal.isOrderedToSit()}) and the three heads' neck
 * extensions in degrees, pitched from 30 (the port's {@code ModelThePrinceAdult.poseFrom}; orig ModelThePrinceAdult.java
 * setRotationAngles). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive
 * them headlessly with a declared state instead of a live entity.
 */
public interface ThePrinceAdultPose {
    int getAttacking();

    int getActivity();

    boolean isOrderedToSit();

    int getHead1Ext();

    int getHead2Ext();

    int getHead3Ext();
}
