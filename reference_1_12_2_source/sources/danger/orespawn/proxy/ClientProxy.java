/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.block.model.ModelResourceLocation
 *  net.minecraft.item.Item
 *  net.minecraft.util.ResourceLocation
 *  net.minecraftforge.client.model.ModelLoader
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.fml.common.Mod$EventHandler
 *  net.minecraftforge.fml.common.event.FMLPostInitializationEvent
 *  net.minecraftforge.fml.common.event.FMLPreInitializationEvent
 */
package danger.orespawn.proxy;

import danger.orespawn.events.ClientEvents;
import danger.orespawn.proxy.CommonProxy;
import danger.orespawn.util.handlers.RenderHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy
extends CommonProxy {
    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ResourceLocation resourceLocation = item.getRegistryName();
        if (resourceLocation != null) {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(resourceLocation, id);
            ModelLoader.setCustomModelResourceLocation((Item)item, (int)meta, (ModelResourceLocation)modelResourceLocation);
        } else {
            System.out.println("Resource Location was null.");
        }
    }

    @Mod.EventHandler
    public static void PreInit(FMLPreInitializationEvent event) {
        RenderHandler.registerEntityRenders();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        MinecraftForge.EVENT_BUS.register((Object)new ClientEvents());
    }

    @Override
    public void render() {
    }
}

