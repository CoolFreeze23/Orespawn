package de.dertoaster.multihitboxlib.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 2.0 S4 (vendored addition): NeoForge registers multipart part entities in
 * {@code ClientLevel.partEntities} ONLY from the add-time tracking-start
 * callback — parts built lazily after the entity was added (the modern
 * spider's client parts, which wait for the synced movement-mode flag) are
 * otherwise invisible to {@code Level.getEntities} and therefore to every
 * client pick path. This accessor lets
 * {@code MHLibClientPartRegistration} complete the registration the
 * callback missed. Field name verified against the NeoForge 21.1.223
 * binary ({@code javap}: {@code private final Int2ObjectMap<PartEntity<?>>
 * partEntities}).
 */
@Mixin(ClientLevel.class)
public interface AccessorClientLevel {

    @Accessor("partEntities")
    Int2ObjectMap<PartEntity<?>> mhlib$getPartEntities();
}
