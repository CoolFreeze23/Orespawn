package de.dertoaster.multihitboxlib.mixin.minecraft;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.dertoaster.multihitboxlib.api.IMHLibPartIndexMember;
import de.dertoaster.multihitboxlib.util.PartEntityIndex;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * ENT-S-174: a part entity's box never changes behind its level's {@link PartEntityIndex}. {@code
 * Entity.setBoundingBox} is the only writer of {@code Entity.bb} besides the field's initializer ({@code setPos}, {@code
 * move}, {@code refreshDimensions} and MHLib's part placement all end in it), so marking the part's group moved there
 * keeps every group envelope exact. The group is marked only when the new box differs from the current one
 * ({@code AABB.equals}, exact on every double): every MHLib part re-sets its box every tick ({@code updateLastPos}), and a
 * creature standing still must not rebuild its envelope for boxes that did not move; a changed box, a sign of zero or a
 * NaN turning into a number included, marks it. A {@code ModifyVariable} at HEAD that returns the box unchanged rather
 * than an {@code Inject}: no callback object on a call every moving entity makes each tick; entities that are not a part
 * in a level's part map hold no group and pay one field read. {@code allow = expect = 1}.
 */
@Mixin(Entity.class)
public abstract class MixinEntity implements IMHLibPartIndexMember {

	@Unique
	@Nullable
	private PartEntityIndex.Group mhlibPartIndexGroup;

	@Override
	@Nullable
	public PartEntityIndex.Group _mhlibAccess_getPartIndexGroup() {
		return this.mhlibPartIndexGroup;
	}

	@Override
	public void _mhlibAccess_setPartIndexGroup(@Nullable final PartEntityIndex.Group group) {
		this.mhlibPartIndexGroup = group;
	}

	@ModifyVariable(
			method = "setBoundingBox(Lnet/minecraft/world/phys/AABB;)V",
			at = @At("HEAD"),
			argsOnly = true,
			allow = 1,
			expect = 1
	)
	private AABB mhlibMarkPartMoved(final AABB box) {
		final PartEntityIndex.Group group = this.mhlibPartIndexGroup;
		if (group != null && !box.equals(((Entity) (Object) this).getBoundingBox())) {
			group.markMoved();
		}
		return box;
	}
}
