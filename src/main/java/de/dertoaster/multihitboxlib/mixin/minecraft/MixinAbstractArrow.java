package de.dertoaster.multihitboxlib.mixin.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;

/**
 * ENT-S-173 (the port's extension of the vendored library, the {@link MixinPlayer} pattern): an arrow that strikes an
 * {@link MHLibPartEntity} lands on the part's PARENT for everything but the damage itself.
 *
 * <p>{@code AbstractArrow.onHitEntity} takes the struck entity from the hit result and, around the one
 * {@code Entity.hurt} it makes, treats it as the creature: the flame (the {@code igniteForSeconds} an arrow on fire
 * applies), the piercing list, the owner's {@code setLastHurtMob}, and inside the {@code instanceof LivingEntity}
 * branch the arrow count, the knockback (Punch), the enchantment post-attack effects and {@code doPostHurtEffects}
 * (a tipped arrow's potion effect), the hurt sound and the kill bookkeeping. A part is not a {@code LivingEntity},
 * so with the profiles on every species a burning arrow would set a wing on fire and a tipped arrow would poison
 * nothing. The hit result's entity is swapped to the parent at HEAD (same location), and the receiver of the inner
 * {@code Entity.hurt} is swapped back to the part, so the damage still enters {@code MHLibPartEntity.hurt} and its
 * modifier (the Queen's, the King's and Godzilla's schemes), then {@code IMultipartEntity.hurt}. Gated on
 * {@code MHLibPartEntity}: vanilla's dragon parts keep vanilla's behaviour. Both injectors carry
 * {@code allow = expect = 1}: a second {@code Entity.hurt} site in the method would fail the load instead of applying
 * silently.</p>
 *
 * <p>Piercing: with the swap, {@code piercingIgnoreEntityIds} records the CREATURE's id, but {@code canHitEntity}
 * compares the candidate's own id - the part's - so the tick's hit loop found the same part again on the same
 * segment, the second {@code hurt} fell inside the creature's invulnerability window, and the arrow deflected and was
 * destroyed on the first profiled creature it met. {@link #mhlibIgnoreTheStruckCreaturesParts} wraps that
 * lookup: a part whose creature is in the list is ignored too, so a Piercing arrow passes through a creature
 * once, as through any single-box mob.</p>
 */
@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow {

	@ModifyVariable(
			method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
			at = @At("HEAD"),
			argsOnly = true,
			allow = 1,
			expect = 1
	)
	private EntityHitResult mhlibUnwrapStruckPart(final EntityHitResult result, @Share("mhlibStruckPart") final LocalRef<MHLibPartEntity<?>> struckPart) {
		if (result != null && result.getEntity() instanceof MHLibPartEntity<?> part) {
			final Entity parent = part.getParent();
			if (parent != null) {
				struckPart.set(part);
				return new EntityHitResult(parent, result.getLocation());
			}
		}
		return result;
	}

	@ModifyReceiver(
			method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
			allow = 1,
			expect = 1
	)
	private Entity mhlibHurtTheStruckPart(final Entity receiver, final DamageSource source, final float amount, @Share("mhlibStruckPart") final LocalRef<MHLibPartEntity<?>> struckPart) {
		final MHLibPartEntity<?> part = struckPart.get();
		return part != null && receiver == part.getParent() ? part : receiver;
	}

	@WrapOperation(
			method = "canHitEntity(Lnet/minecraft/world/entity/Entity;)Z",
			at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/IntOpenHashSet;contains(I)Z"),
			allow = 1,
			expect = 1
	)
	private boolean mhlibIgnoreTheStruckCreaturesParts(final IntOpenHashSet ignored, final int id, final Operation<Boolean> original, final Entity target) {
		if (original.call(ignored, id)) {
			return true;
		}
		if (target instanceof MHLibPartEntity<?> part) {
			final Entity parent = part.getParent();
			return parent != null && original.call(ignored, parent.getId());
		}
		return false;
	}
}
