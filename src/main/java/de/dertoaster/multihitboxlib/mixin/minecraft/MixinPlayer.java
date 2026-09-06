package de.dertoaster.multihitboxlib.mixin.minecraft;

// Portions derived from MoreHitboxes by DarkPred (https://github.com/DarkPred/MoreHitboxes, commit 88899b3), MIT License — see LICENSE-MoreHitboxes.txt

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * MHLib harvest 2 (2026-09-06, wave 5; the MoreHitboxes evaluation section 3.5 (1) / section 4 row 2): a player's
 * melee blow on an {@link MHLibPartEntity} lands on the part's PARENT for everything but the damage itself.
 *
 * <p>NeoForge 21.1.223's own {@code Player.attack} unwraps a {@code PartEntity} target only for the weapon's
 * {@code ItemStack.hurtEnemy} / {@code postHurtEnemy} (javap offsets 1109-1126, the parent used at 1173-1177 and
 * 1220-1227). The knockback ({@code LivingEntity.knockback} at 611 / {@code Entity.push} at 660 -- a push on a part
 * is lost: nothing integrates a part's deltaMovement), {@code setLastHurtMob} (1106), the crit gate (the
 * {@code instanceof LivingEntity} at 320: a part never crits), the sweep box and its exclusion (714 / 767), the
 * enchantment damage bonus (67), {@code EnchantmentHelper.doPostAttackEffects} (1187 -- fire aspect ignites the
 * part, whose fire the spider profile's environmental rule then drops) and the damage-dealt stat / indicator
 * (1291-1393) all take the raw argument. Verbatim mechanism from MoreHitboxes' {@code PlayerMixin}: the argument is
 * swapped to the parent at HEAD, so all of that lands on the parent, and the receiver of the inner
 * {@code Entity.hurt} (offset 527 -- the one invocation with that owner; the sweep hits at 864 are
 * {@code LivingEntity.hurt} on other entities) is swapped back to the part, so the damage still enters
 * {@code MHLibPartEntity.hurt} and its damage modifier, then {@code IMultipartEntity.hurt}. NeoForge's own unwrap
 * at 1113 then sees the parent and is a no-op.
 *
 * <p>Gated on {@link MHLibPartEntity}, not {@code PartEntity}: OreSpawn's {@code OreSpawnPartEntity} (the King's
 * and Godzilla's parts, the 1.7.10 {@code EntityDragonPart} pattern) and vanilla's dragon parts keep vanilla's part
 * behaviour, which is 1.7.10's ({@code attackTargetEntityWithCurrentItem} unwrapped a dragon part only for
 * {@code hitEntity}, exactly as NeoForge does today). The Queen's parts are the port's own design over a single
 * 1.7.10 box: a blow on one of them now knocks her back (absorbed by her KNOCKBACK_RESISTANCE 1.0), sets fire to
 * her and records her as the last hurt mob, as a blow on the body did in 1.7.10; the modern robots take a push
 * through a leg as any single-box mob does. Both sides: the client's {@code LocalPlayer.attack} takes the same path
 * (a client-side {@code hurt} returns false, so the crit / magic-crit branches behind it are never reached there);
 * the crit particles a player sees come from the SERVER's {@code crit(target)} / {@code magicCrit(target)} -- an
 * animate packet broadcast on the target's entity id, now the parent's -- so they track the parent (refuter B,
 * 2026-09-06). Both injectors carry {@code allow = expect = 1}: {@code require} (the config's defaultRequire 1) is
 * a MINIMUM -- a second match would apply silently -- so a future second {@code Entity.hurt} site in {@code attack}
 * fails the load instead (refuter A, 2026-09-06).
 */
@Mixin(Player.class)
public abstract class MixinPlayer {

	@ModifyVariable(
			method = "attack(Lnet/minecraft/world/entity/Entity;)V",
			at = @At("HEAD"),
			argsOnly = true,
			allow = 1,
			expect = 1
	)
	private Entity mhlibUnwrapAttackedPart(final Entity target, @Share("mhlibAttackedPart") final LocalRef<MHLibPartEntity<?>> attackedPart) {
		if (target instanceof MHLibPartEntity<?> part) {
			final Entity parent = part.getParent();
			if (parent != null) {
				attackedPart.set(part);
				return parent;
			}
		}
		return target;
	}

	@ModifyReceiver(
			method = "attack(Lnet/minecraft/world/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
			allow = 1,
			expect = 1
	)
	private Entity mhlibHurtTheAttackedPart(final Entity receiver, final DamageSource source, final float amount, @Share("mhlibAttackedPart") final LocalRef<MHLibPartEntity<?>> attackedPart) {
		final MHLibPartEntity<?> part = attackedPart.get();
		return part != null && receiver == part.getParent() ? part : receiver;
	}

}
