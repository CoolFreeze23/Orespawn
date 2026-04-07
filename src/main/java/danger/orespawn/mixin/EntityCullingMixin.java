package danger.orespawn.mixin;

import danger.orespawn.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityCullingMixin {

    @Inject(method = "getBoundingBoxForCulling", at = @At("RETURN"), cancellable = true)
    private void orespawn$expandCullingForOversizedWeapons(CallbackInfoReturnable<AABB> cir) {
        if (!((Object) this instanceof LivingEntity living)) return;

        ItemStack mainHand = living.getMainHandItem();
        if (mainHand.isEmpty()) return;

        if (mainHand.is(ModItems.BIG_BERTHA.get())
                || mainHand.is(ModItems.SLICE.get())
                || mainHand.is(ModItems.ROYAL_GUARDIAN_SWORD.get())
                || mainHand.is(ModItems.BATTLE_AXE.get())
                || mainHand.is(ModItems.QUEEN_BATTLE_AXE.get())
                || mainHand.is(ModItems.CHAINSAW.get())
                || mainHand.is(ModItems.ATTITUDE_ADJUSTER.get())
                || mainHand.is(ModItems.SQUID_ZOOKA.get())) {
            cir.setReturnValue(cir.getReturnValue().inflate(5.0));
        }
    }
}
