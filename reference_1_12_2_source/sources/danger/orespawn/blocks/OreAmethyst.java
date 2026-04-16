/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.Objects;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class OreAmethyst
extends Block {
    public OreAmethyst() {
        super(Material.field_151576_e);
        this.func_149663_c("amethyst_ore");
        this.setRegistryName("amethyst_ore");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        this.func_149711_c(5.0f);
        this.func_149752_b(5.0f);
        this.setHarvestLevel("pickaxe", 2);
        this.func_149715_a(0.2f);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    public Item func_180660_a(IBlockState state, Random rand, int fortune) {
        return ModItems.AMETHYST;
    }

    public int func_149745_a(Random random) {
        return random.nextInt(2) + 1;
    }
}

