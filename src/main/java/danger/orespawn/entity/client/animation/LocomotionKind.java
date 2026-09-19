package danger.orespawn.entity.client.animation;

/**
 * The SPEC's {@code locomotion} word ({@code tools/artist_specs/<registry>.json}), mirrored on the species'
 * replacement ({@code OreSpawnGeoReplacement.locomotion()}) so the contract's {@code flying} input can be what
 * contract section 3 defines: the SPEC's {@code locomotion: flyer} AND {@code !entity.onGround()}. Only
 * {@link #FLYER} changes an input; the other kinds are recorded for the sheet's sake and read by nothing.
 */
public enum LocomotionKind {
    /** Ground locomotion: {@code walk} scaled by {@code limbSwingAmount}; never {@code flying}. */
    WALKER,
    /** The SPEC's flyer: {@code flying} whenever the entity is off the ground; {@code fly} falls back to {@code walk} (section 2.4). */
    FLYER,
    /** A water species: {@code swim} is its locomotion in water; never {@code flying}. */
    SWIMMER,
    /** A species that never walks ({@code MOVEMENT_SPEED 0}): {@code idle} is its only locomotion loop. */
    STATIONARY;

    public boolean flyer() {
        return this == FLYER;
    }
}
