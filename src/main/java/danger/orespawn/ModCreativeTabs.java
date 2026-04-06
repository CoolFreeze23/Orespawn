package danger.orespawn;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ORESPAWN_TAB =
            CREATIVE_MODE_TABS.register("orespawn_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.orespawn"))
                    .icon(() -> new ItemStack(ModItems.ULTIMATE_SWORD.get()))
                    .displayItems((parameters, output) -> {
                        // Ores
                        output.accept(ModItems.ORE_RUBY_ITEM.get());
                        output.accept(ModItems.ORE_AMETHYST_ITEM.get());
                        output.accept(ModItems.ORE_URANIUM_ITEM.get());
                        output.accept(ModItems.ORE_TITANIUM_ITEM.get());
                        output.accept(ModItems.ORE_SALT_ITEM.get());

                        // Storage blocks
                        output.accept(ModItems.BLOCK_RUBY_ITEM.get());
                        output.accept(ModItems.BLOCK_AMETHYST_ITEM.get());
                        output.accept(ModItems.BLOCK_URANIUM_ITEM.get());
                        output.accept(ModItems.BLOCK_TITANIUM_ITEM.get());

                        // Materials
                        output.accept(ModItems.RUBY.get());
                        output.accept(ModItems.AMETHYST_GEM.get());
                        output.accept(ModItems.INGOT_URANIUM.get());
                        output.accept(ModItems.INGOT_TITANIUM.get());
                        output.accept(ModItems.SALT.get());
                        output.accept(ModItems.GREEN_GOO.get());
                        output.accept(ModItems.MOTH_SCALE.get());
                        output.accept(ModItems.QUEEN_SCALE.get());
                        output.accept(ModItems.GODZILLA_SCALE.get());

                        // Ultimate tier
                        output.accept(ModItems.ULTIMATE_SWORD.get());
                        output.accept(ModItems.ULTIMATE_PICKAXE.get());
                        output.accept(ModItems.ULTIMATE_SHOVEL.get());
                        output.accept(ModItems.ULTIMATE_HOE.get());
                        output.accept(ModItems.ULTIMATE_AXE.get());

                        // Special weapons
                        output.accept(ModItems.BIG_BERTHA.get());
                        output.accept(ModItems.ROYAL_GUARDIAN_SWORD.get());
                        output.accept(ModItems.QUEEN_BATTLE_AXE.get());
                        output.accept(ModItems.NIGHTMARE_SWORD.get());
                        output.accept(ModItems.BATTLE_AXE.get());
                        output.accept(ModItems.CHAINSAW.get());
                        output.accept(ModItems.ATTITUDE_ADJUSTER.get());
                        output.accept(ModItems.SLICE.get());

                        // Ruby tier
                        output.accept(ModItems.RUBY_SWORD.get());
                        output.accept(ModItems.RUBY_PICKAXE.get());
                        output.accept(ModItems.RUBY_SHOVEL.get());
                        output.accept(ModItems.RUBY_HOE.get());
                        output.accept(ModItems.RUBY_AXE.get());

                        // Amethyst tier
                        output.accept(ModItems.AMETHYST_SWORD.get());
                        output.accept(ModItems.AMETHYST_PICKAXE.get());
                        output.accept(ModItems.AMETHYST_SHOVEL.get());
                        output.accept(ModItems.AMETHYST_HOE.get());
                        output.accept(ModItems.AMETHYST_AXE.get());

                        // Emerald tier
                        output.accept(ModItems.EMERALD_SWORD.get());
                        output.accept(ModItems.EMERALD_PICKAXE.get());
                        output.accept(ModItems.EMERALD_SHOVEL.get());
                        output.accept(ModItems.EMERALD_HOE.get());
                        output.accept(ModItems.EMERALD_AXE.get());

                        // Ranged
                        output.accept(ModItems.ULTIMATE_BOW.get());
                        output.accept(ModItems.RAY_GUN.get());
                        output.accept(ModItems.THUNDER_STAFF.get());
                        output.accept(ModItems.SQUID_ZOOKA.get());

                        // Utility
                        output.accept(ModItems.MINERS_DREAM.get());
                        output.accept(ModItems.MAGIC_APPLE.get());
                        output.accept(ModItems.INSTANT_SHELTER.get());
                        output.accept(ModItems.INSTANT_GARDEN.get());
                        output.accept(ModItems.ELEVATOR.get());

                        // Food
                        output.accept(ModItems.PIZZA_ITEM.get());
                        output.accept(ModItems.CORN_DOG.get());
                        output.accept(ModItems.COOKED_BACON.get());
                        output.accept(ModItems.SALAD.get());
                        output.accept(ModItems.BLT_SANDWICH.get());
                        output.accept(ModItems.CRABBY_PATTY.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
