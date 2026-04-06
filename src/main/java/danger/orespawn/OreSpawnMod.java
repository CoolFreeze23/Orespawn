package danger.orespawn;

import danger.orespawn.world.ModWorldGen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OreSpawnMod.MOD_ID)
public class OreSpawnMod {
    public static final String MOD_ID = "orespawn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public OreSpawnMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModWorldGen.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, OreSpawnConfig.SPEC);
    }
}
