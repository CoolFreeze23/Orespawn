package danger.orespawn.entity.pose;

/**
 * What {@code ModelThePrincess.setupAnim} reads from its entity, as an interface the entity already satisfies (the
 * remainder slice, 2026-09-15, owner's with the bipeds; the Slice 4b form of {@link ThePrincePose}, whose pose hers is
 * - the activity (2 = flying tucks the legs), the attacking flag (the wings' and tail's fast rhythms, the three jaws'
 * chatter), the sit order that stills the tail ({@code TamableAnimal.isOrderedToSit()}) and the three heads' neck
 * extensions in degrees (the port's {@code ModelThePrincess.poseFrom}; orig ModelThePrincess.java
 * setRotationAngles). The three power orbs read no entity: their rotations accumulate per rendered frame on the model's
 * own parts (ModelThePrincess.java:319-354), which the seam's hook carries from the bake's bones. The classic model and the
 * GeckoLib hook both pose from this, so the parity harness can drive them headlessly with a declared state instead of a
 * live entity.
 */
public interface ThePrincessPose {
    int getActivity();

    int getAttacking();

    boolean isOrderedToSit();

    int getHead1Ext();

    int getHead2Ext();

    int getHead3Ext();
}
