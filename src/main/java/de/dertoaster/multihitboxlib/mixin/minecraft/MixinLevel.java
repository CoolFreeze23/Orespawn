package de.dertoaster.multihitboxlib.mixin.minecraft;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import de.dertoaster.multihitboxlib.api.IMHLibPartIndexHolder;
import de.dertoaster.multihitboxlib.util.PartEntityIndex;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * ENT-S-174: NeoForge's two box queries walk the level's part map after the section lookup ({@code for
 * (PartEntity<?> p : this.getPartEntities())}, one loop in each, patched in by NeoForge). Each loop's {@code
 * getPartEntities()} call is wrapped to hand over only the parts of the entities whose envelope meets the query box
 * ({@link PartEntityIndex#near}); the loop's own tests then run on them unchanged, and the index falls back to the map
 * itself whenever it cannot vouch for the answer. The index lives on the level (built with it, on the thread that
 * builds it); the tracking callbacks keep it in step with the map. {@code allow = expect = 1} per method: one part loop
 * each, verified against NeoForge 21.1.223's Level patch and the recompiled class.
 */
@Mixin(Level.class)
public abstract class MixinLevel implements IMHLibPartIndexHolder {

	@Unique
	private final PartEntityIndex mhlibPartIndex = new PartEntityIndex();

	@Override
	public PartEntityIndex _mhlibAccess_getPartIndex() {
		return this.mhlibPartIndex;
	}

	@WrapOperation(
			method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getPartEntities()Ljava/util/Collection;"),
			allow = 1,
			expect = 1
	)
	private Collection<PartEntity<?>> mhlibPartsNearTheBox(final Level level, final Operation<Collection<PartEntity<?>>> original,
			@Nullable final Entity except, final AABB box, final Predicate<? super Entity> predicate) {
		return this.mhlibPartIndex.near(original.call(level), box);
	}

	@WrapOperation(
			method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getPartEntities()Ljava/util/Collection;"),
			allow = 1,
			expect = 1
	)
	private Collection<PartEntity<?>> mhlibPartsNearTheTypedBox(final Level level, final Operation<Collection<PartEntity<?>>> original,
			final EntityTypeTest<Entity, ?> test, final AABB box, final Predicate<?> predicate, final List<?> out, final int limit) {
		return this.mhlibPartIndex.near(original.call(level), box);
	}
}
