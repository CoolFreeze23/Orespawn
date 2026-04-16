/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.SoundType
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OreGenericEgg
extends Block {
    public OreGenericEgg(String name) {
        super(Material.field_151578_c);
        this.func_149711_c(0.5f);
        this.func_149752_b(1.0f);
        this.func_149663_c(name);
        this.setRegistryName(name);
        this.func_149672_a(SoundType.field_185849_b);
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    public void func_180653_a(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.func_180653_a(worldIn, pos, state, chance, fortune);
        int dropAmount = 5 + worldIn.field_73012_v.nextInt(3) + worldIn.field_73012_v.nextInt(3);
        if (worldIn.field_73012_v.nextInt(2) == 1) {
            this.func_180637_b(worldIn, pos, dropAmount);
        }
    }
}

