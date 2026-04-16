/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.RedAnt;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTermiteTroll
extends Block {
    public BlockTermiteTroll() {
        super(Material.field_151576_e);
        this.func_149663_c("termite_troll_block");
        this.setRegistryName("termite_troll_block");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    public void func_180663_b(World worldIn, BlockPos pos, IBlockState state) {
        super.func_180663_b(worldIn, pos, state);
        for (int i = 0; i < 20; ++i) {
            Entity termite = EntityList.func_191304_a(RedAnt.class, (World)worldIn);
            assert (termite != null);
            termite.func_70080_a((double)pos.func_177958_n(), (double)pos.func_177956_o(), (double)pos.func_177952_p(), worldIn.field_73012_v.nextFloat() * 360.0f, 0.0f);
            worldIn.func_72838_d(termite);
            ((EntityLiving)termite).func_70642_aH();
        }
    }
}

