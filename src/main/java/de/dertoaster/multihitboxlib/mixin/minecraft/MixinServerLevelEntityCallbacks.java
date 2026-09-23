package de.dertoaster.multihitboxlib.mixin.minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.dertoaster.multihitboxlib.api.IMHLibPartIndexHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * ENT-S-174: the server level's part index follows its part map ({@code ServerLevel.dragonParts}) call by call.
 * NeoForge's {@code onTrackingStart} puts each part of a multipart entity into the map and {@code
 * onTrackingEnd} removes each; the one {@code put} and the one {@code remove} are wrapped so the index hears of every
 * change the moment the map makes it (the previous occupant of a reused id included). {@code this$0} is the level
 * whose map it is. {@code allow = expect = 1} per method (NeoForge 21.1.223's ServerLevel patch; the recompiled class
 * has one {@code Int2ObjectMap.put} in onTrackingStart and one {@code remove} in onTrackingEnd).
 */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class MixinServerLevelEntityCallbacks {

	@Shadow
	@Final
	ServerLevel this$0;

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
