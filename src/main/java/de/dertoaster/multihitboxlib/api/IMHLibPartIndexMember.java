package de.dertoaster.multihitboxlib.api;

import javax.annotation.Nullable;

import de.dertoaster.multihitboxlib.util.PartEntityIndex;

/**
 * ENT-S-174: implemented on every {@code Entity} by {@code MixinEntity}. A part entity in a level's part map points at
 * its {@link PartEntityIndex.Group}, which {@code Entity.setBoundingBox} marks moved; every other entity holds null.
 * DO NOT IMPLEMENT THIS INTERFACE.
 */
public interface IMHLibPartIndexMember {

	@Nullable
	PartEntityIndex.Group _mhlibAccess_getPartIndexGroup();

	void _mhlibAccess_setPartIndexGroup(@Nullable PartEntityIndex.Group group);
}
