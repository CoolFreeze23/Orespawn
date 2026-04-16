/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcp.MethodsReturnNonnullByDefault
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemSeedFood
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.IBlockAccess
 */
package danger.orespawn.items.food;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@MethodsReturnNonnullByDefault
public class ItemCorn
extends ItemSeedFood {
    public ItemCorn() {
        super(1, 1.0f, ModBlocks.CORN_PLANT, Blocks.field_150458_ak);
        this.func_77655_b("corn");
        this.setRegistryName("corn");
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
    }

    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return ModBlocks.CORN_PLANT.func_176223_P();
    }
}

