package danger.orespawn.entity.pose;

/**
 * What {@code ModelThePrince.setupAnim} reads from its entity, as an interface the entity already satisfies (the hooks,
 * the Slice 4b form of {@link HerculesBeetlePose}): the activity (2 = flying tucks the legs), the attacking flag (the
 * wings' and tail's fast rhythms, the three jaws' chatter), the sit order that stills the tail ({@code
 * TamableAnimal.isOrderedToSit()}) and the three heads' neck extensions in degrees (the port's {@code
 * ModelThePrince.poseFrom}; orig ModelThePrince.java setRotationAngles). The classic model and the GeckoLib hook both
 * pose from this, so the parity harness can drive them headlessly with a declared state instead of a live entity.
 */
public interface ThePrincePose {
    int getActivity();

    int getAttacking();

    boolean isOrderedToSit();

    int getHead1Ext();

    int getHead2Ext();

    int getHead3Ext();
}
