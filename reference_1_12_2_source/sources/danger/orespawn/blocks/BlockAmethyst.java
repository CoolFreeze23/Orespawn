/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class BlockAmethyst
extends Block {
    public BlockAmethyst() {
        super(Material.field_151573_f);
        this.func_149663_c("amethyst_block");
        this.setRegistryName("amethyst_block");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        this.func_149711_c(5.0f);
        this.func_149752_b(5.0f);
        this.setHarvestLevel("pickaxe", 2);
        this.func_149715_a(0.2f);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }
}

