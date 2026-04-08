package danger.orespawn;

import danger.orespawn.gui.CrystalFurnaceMenu;
import danger.orespawn.gui.CrystalWorkbenchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<CrystalFurnaceMenu>> CRYSTAL_FURNACE_MENU =
            MENUS.register("crystal_furnace",
                    () -> IMenuTypeExtension.create((windowId, inv, data) -> new CrystalFurnaceMenu(windowId, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<CrystalWorkbenchMenu>> CRYSTAL_WORKBENCH_MENU =
            MENUS.register("crystal_workbench",
                    () -> IMenuTypeExtension.create((windowId, inv, data) -> new CrystalWorkbenchMenu(windowId, inv)));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
