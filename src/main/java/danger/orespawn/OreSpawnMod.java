package danger.orespawn;

import danger.orespawn.world.ModWorldGen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OreSpawn main mod entry point (NeoForge 1.21.1).
 *
 * <p><b>1.12.2 → 1.21.1 paradigm shift.</b> The legacy
 * {@code danger.orespawn.OreSpawnMain} class used the old FML lifecycle:
 * {@code @EventHandler FMLPreInitializationEvent / FMLInitializationEvent /
 * FMLPostInitializationEvent} with a sided proxy ({@code CommonProxy} /
 * {@code ClientProxy}) and {@code GameRegistry.register(...)} calls scattered
 * through {@code RegistryHandler}.</p>
 *
 * <p>That model is gone. NeoForge 1.21 dispatches a single mod-bus event
 * stream ({@link IEventBus modEventBus}) into the mod constructor, and every
 * registry add must go through a {@link net.neoforged.neoforge.registries.DeferredRegister}
 * that's hooked to that bus *before the constructor returns*. The
 * {@code Mod-bus / Game-bus} split is now explicit
 * (see {@link net.neoforged.fml.common.EventBusSubscriber.Bus}); one-off
 * setup work uses {@link FMLCommonSetupEvent#enqueueWork} for thread safety.</p>
 *
 * <p>The {@link ModContainer} parameter is also new — it's how we attach the
 * mod's config spec without resorting to the 1.12.2
 * {@code Configuration} / {@code MinecraftForge.EVENT_BUS.register(...)}
 * pattern.</p>
 *
 * <p>Order matters in the constructor. {@link ModDataComponents} ships first
 * because items and blocks reference data-component types in their properties.
 * {@link ModBlocks} ships before {@link ModItems} so the block-item factory
 * methods can resolve their {@code DeferredHolder<Block>} during construction.
 * {@link ModWorldGen} (chunk generators) runs last so it can cite already-
 * registered blocks/biomes inside its codec.</p>
 */
@Mod(OreSpawnMod.MOD_ID)
public class OreSpawnMod {
    public static final String MOD_ID = "orespawn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public OreSpawnMod(IEventBus modEventBus, ModContainer modContainer) {
        // Order is intentional — see class JavaDoc.
        ModDataComponents.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModWorldGen.register(modEventBus);
        danger.orespawn.world.feature.ModFeatures.register(modEventBus);
        danger.orespawn.world.structure.ModStructureTypes.register(modEventBus);
        danger.orespawn.recipe.ModRecipes.register(modEventBus);
        danger.orespawn.loot.ModLootModifiers.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        // Config registration replaces 1.12.2's Configuration class + manual
        // load() calls in preInit. NeoForge handles file watching and reload
        // events for us.
        modContainer.registerConfig(ModConfig.Type.COMMON, OreSpawnConfig.SPEC);
    }

    /**
     * Common setup runs after all DeferredRegisters have fired, but before
     * world load. enqueueWork pushes the body onto the main thread because
     * dispenser behaviour registration (and many other vanilla maps) is not
     * thread-safe — the parallel mod-loading dispatcher would otherwise call
     * us off-thread.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModDispenserBehaviors::register);
    }
}
