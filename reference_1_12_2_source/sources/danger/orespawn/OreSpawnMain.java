/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.fml.common.IWorldGenerator
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.common.Mod$EventHandler
 *  net.minecraftforge.fml.common.Mod$Instance
 *  net.minecraftforge.fml.common.SidedProxy
 *  net.minecraftforge.fml.common.event.FMLInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLPostInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLPreInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLServerStartingEvent
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.fml.common.registry.GameRegistry
 */
package danger.orespawn;

import danger.orespawn.proxy.CommonProxy;
import danger.orespawn.tabs.OrespawnTab;
import danger.orespawn.util.handlers.RegistryHandler;
import danger.orespawn.util.premium.PremiumChecker;
import danger.orespawn.world.CornPlantGenerator;
import danger.orespawn.world.LiquidGenerator;
import danger.orespawn.world.PlantGenerator;
import danger.orespawn.world.gen.ores.AntHillGenerator;
import danger.orespawn.world.gen.ores.WorldGenOres;
import java.util.Random;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid="orespawn", name="OreSpawn", version="1.0.0")
public class OreSpawnMain {
    public static Random OreSpawnRand = new Random(151L);
    public static int PlayNicely = 0;
    @Mod.Instance
    public static OreSpawnMain instance;
    @SidedProxy(clientSide="danger.orespawn.proxy.ClientProxy", serverSide="danger.orespawn.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static final CreativeTabs OreSpawnTab;

    @Mod.EventHandler
    public static void PreInit(FMLPreInitializationEvent event) {
        RegistryHandler.preInitRegistries();
        proxy.preInit(event);
        PremiumChecker.Init();
        GameRegistry.registerWorldGenerator((IWorldGenerator)new WorldGenOres(), (int)3);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        RegistryHandler.initRegistries();
        proxy.init(event);
        MinecraftForge.TERRAIN_GEN_BUS.register((Object)new CornPlantGenerator());
        MinecraftForge.TERRAIN_GEN_BUS.register((Object)new PlantGenerator());
        MinecraftForge.TERRAIN_GEN_BUS.register((Object)new AntHillGenerator());
        MinecraftForge.TERRAIN_GEN_BUS.register((Object)new LiquidGenerator());
    }

    @Mod.EventHandler
    public static void PostInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public static void serverInit(FMLServerStartingEvent event) {
        RegistryHandler.serverRegistries(event);
    }

    @SubscribeEvent
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("---------------------------------------------------------------------- PLAYER JOINED");
        PremiumChecker.CheckUser(event.player);
    }

    static {
        OreSpawnTab = new OrespawnTab();
    }
}

