package de.dertoaster.multihitboxlib.client;

import de.dertoaster.multihitboxlib.api.IMHLibPartIndexHolder;
import de.dertoaster.multihitboxlib.mixin.accessor.AccessorClientLevel;
import de.dertoaster.multihitboxlib.util.PartEntityIndex;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * 2.0 S4 (vendored addition): completes the client-side part registration
 * for multipart entities whose parts were built AFTER the entity was added
 * to the client level (NeoForge's {@code ClientLevel} registers parts only
 * in its add-time tracking-start callback). Callers must already be on the
 * client — this class references client-only types and is loaded lazily at
 * its first (client-guarded) call site.
 */
public final class MHLibClientPartRegistration {

    private MHLibClientPartRegistration() {
    }

    /** Registers {@code entity}'s current parts in the client pick registry. */
    public static void registerParts(Entity entity) {
        if (!(entity.level() instanceof ClientLevel clientLevel)) {
            return;
        }
        PartEntity<?>[] parts = entity.getParts();
        if (parts == null || parts.length == 0) {
            return;
        }
        Int2ObjectMap<PartEntity<?>> registry =
                ((AccessorClientLevel) clientLevel).mhlib$getPartEntities();
        // ENT-S-174: this is the one writer of the level's part map outside NeoForge's tracking callbacks, so it tells
        // the level's part index of each part it puts, as the wrapped put in onTrackingStart does; without it the map
        // outgrew the index while a modern robot was tracked and every client query walked the whole map.
        final PartEntityIndex index = ((IMHLibPartIndexHolder) clientLevel)._mhlibAccess_getPartIndex();
        for (PartEntity<?> part : parts) {
            final PartEntity<?> previous = registry.put(part.getId(), part);
            index.onPut(part, previous);
        }
    }
}
