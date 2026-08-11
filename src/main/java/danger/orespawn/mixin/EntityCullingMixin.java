package danger.orespawn.mixin;

import java.util.Set;

import danger.orespawn.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityCullingMixin {

    /**
     * OPT-012: the 8 oversized-weapon deferred holders, resolved once into a
     * Set on first use instead of 8 {@code ModItems.X.get()} + {@code is()}
     * checks per entity per frame. Lazily built because this runs client-side
     * after item registration; the item registry is frozen at startup and its
     * entries are never replaced afterwards (datapack/resource reloads do not
     * touch registered Item instances), so the resolved set can never go stale
     * for the lifetime of the JVM.
     */
    @Unique
    private static Set<Item> orespawn$oversizedWeapons;

    @Inject(method = "getBoundingBoxForCulling", at = @At("RETURN"), cancellable = true)
    private void orespawn$expandCullingForOversizedWeapons(CallbackInfoReturnable<AABB> cir) {
        if (!((Object) this instanceof LivingEntity living)) return;

        ItemStack mainHand = living.getMainHandItem();
        if (mainHand.isEmpty()) return;

        Set<Item> oversized = orespawn$oversizedWeapons;
        if (oversized == null) {
            oversized = Set.of(
                    ModItems.BIG_BERTHA.get(),
                    ModItems.SLICE.get(),
                    ModItems.ROYAL_GUARDIAN_SWORD.get(),
                    ModItems.BATTLE_AXE.get(),
                    ModItems.QUEEN_BATTLE_AXE.get(),
                    ModItems.CHAINSAW.get(),
                    ModItems.ATTITUDE_ADJUSTER.get(),
                    ModItems.SQUID_ZOOKA.get());
            orespawn$oversizedWeapons = oversized;
        }

        // Same membership test the old is() chain performed (is(Item) compares
        // stack.getItem() identity), so the culled set is unchanged.
        if (oversized.contains(mainHand.getItem())) {
            cir.setReturnValue(cir.getReturnValue().inflate(5.0));
        }
    }
}
