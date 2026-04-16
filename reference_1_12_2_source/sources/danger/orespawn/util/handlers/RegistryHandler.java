/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.command.ICommand
 *  net.minecraft.item.Item
 *  net.minecraftforge.client.event.ModelRegistryEvent
 *  net.minecraftforge.event.RegistryEvent$Register
 *  net.minecraftforge.fml.common.IWorldGenerator
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.event.FMLServerStartingEvent
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.registry.GameRegistry
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 *  net.minecraftforge.registries.IForgeRegistryEntry
 */
package danger.orespawn.util.handlers;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.blocks.TileEntityPlant;
import danger.orespawn.commands.CommandDimensionTeleport;
import danger.orespawn.init.EntitySpawns;
import danger.orespawn.init.ModBiomes;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModDimensions;
import danger.orespawn.init.ModEntities;
import danger.orespawn.init.ModItems;
import danger.orespawn.recipes.CraftingRecipes;
import danger.orespawn.recipes.SmeltingRecipes;
import danger.orespawn.util.handlers.RenderHandler;
import danger.orespawn.util.handlers.SoundsHandler;
import danger.orespawn.world.gen.StructureGenerator;
import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber
public class RegistryHandler {
    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll((IForgeRegistryEntry[])ModItems.ITEMS.toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll((IForgeRegistryEntry[])ModBlocks.BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent
    @SideOnly(value=Side.CLIENT)
    public static void onModelRegister(ModelRegistryEvent event) {
        RenderHandler.registerEntityRenders();
        for (Item item : ModItems.ITEMS) {
            OreSpawnMain.proxy.registerItemRenderer(item, 0, "inventory");
        }
        for (Block block : ModBlocks.BLOCKS) {
            OreSpawnMain.proxy.registerItemRenderer(Item.func_150898_a((Block)block), 0, "inventory");
        }
    }

    public static void preInitRegistries() {
        ModEntities.registerEntities();
        ModDimensions.registerDimensions();
        ModBiomes.registerBiomes();
        EntitySpawns.addSpawns();
        GameRegistry.registerWorldGenerator((IWorldGenerator)new StructureGenerator(), (int)3);
    }

    public static void initRegistries() {
        SoundsHandler.registerSounds();
        SmeltingRecipes.init();
        CraftingRecipes.init();
        GameRegistry.registerTileEntity(TileEntityPlant.class, (String)TileEntityPlant.class.getName());
    }

    public static void serverRegistries(FMLServerStartingEvent event) {
        event.registerServerCommand((ICommand)new CommandDimensionTeleport());
    }
}

