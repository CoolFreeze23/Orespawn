package de.dertoaster.multihitboxlib.mixin.minecraft.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.dertoaster.multihitboxlib.api.IMHLibPartIndexHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * ENT-S-174: the client level's twin of {@code MixinServerLevelEntityCallbacks} - its part index follows {@code
 * ClientLevel.partEntities} through the one {@code put} in NeoForge's {@code onTrackingStart} and the one {@code remove}
 * in {@code onTrackingEnd} (which on the client runs after the leave-level event, so the index and the map agree
 * inside that event too). {@code allow = expect = 1} per method (NeoForge 21.1.223's ClientLevel patch; the
 * recompiled class).
 */
@Mixin(targets = "net.minecraft.client.multiplayer.ClientLevel$EntityCallbacks")
public abstract class MixinClientLevelEntityCallbacks {

	@Shadow
	@Final
	ClientLevel this$0;

	@WrapOperation(
			method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;put(ILjava/lang/Object;)Ljava/lang/Object;"),
			allow = 1,
			expect = 1
	)
	private Object mhlibIndexThePart(final Int2ObjectMap<Object> map, final int id, final Object part, final Operation<Object> original) {
		final Object previous = original.call(map, id, part);
		if (part instanceof PartEntity<?> partEntity) {
			((IMHLibPartIndexHolder) this.this$0)._mhlibAccess_getPartIndex().onPut(partEntity, previous);
		}
		return previous;
	}

	@WrapOperation(
			method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;remove(I)Ljava/lang/Object;"),
			allow = 1,
			expect = 1
	)
	private Object mhlibUnindexThePart(final Int2ObjectMap<Object> map, final int id, final Operation<Object> original) {
		final Object removed = original.call(map, id);
		((IMHLibPartIndexHolder) this.this$0)._mhlibAccess_getPartIndex().onRemoved(removed);
		return removed;
	}
}
