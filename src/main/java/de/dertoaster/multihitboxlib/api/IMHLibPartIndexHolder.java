package de.dertoaster.multihitboxlib.api;

import de.dertoaster.multihitboxlib.util.PartEntityIndex;

/**
 * ENT-S-174: implemented on every {@code Level} by {@code MixinLevel}; the level's {@link PartEntityIndex}, which its
 * entity callbacks keep in step with the level's part map and its two box queries read. DO NOT IMPLEMENT THIS INTERFACE.
 */
public interface IMHLibPartIndexHolder {

	PartEntityIndex _mhlibAccess_getPartIndex();
}
