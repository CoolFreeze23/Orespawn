package danger.orespawn.mixin;

import danger.orespawn.util.OreSpawnSight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ENT-S-121 — the 1.7.10 line-of-sight convention, port-wide (owner's ruling 2026-09-04). A HEAD injection on
 * {@code LivingEntity.hasLineOfSight(Entity)} (NeoForm LivingEntity.java:3033-3043) that answers
 * {@link OreSpawnSight#canSee} — the same eye-to-eye clip with {@code ClipContext.Block.OUTLINE} / {@code Fluid.NONE}
 * (1.7.10's {@code rayTraceBlocks(eyes, eyes)}: selection bounds, no liquid stop) and the same 128-block cap — whenever
 * the receiver is an OreSpawn entity ({@link OreSpawnSight#isOreSpawn}: the registry namespace, no per-class marker).
 * Vanilla receivers fall through to vanilla's {@code COLLIDER} ray untouched; {@code Sensing}'s per-tick cache, every
 * call site and the vanilla goals are untouched — they reach this method through the same virtual call. Registered in
 * {@code orespawn.mixins.json} (common side).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySightMixin {

    @Inject(method = "hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void orespawn$selectionBoundsLineOfSight(Entity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (OreSpawnSight.isOreSpawn(self)) {
            cir.setReturnValue(OreSpawnSight.canSee(self, target));
        }
    }
}
