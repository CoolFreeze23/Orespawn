/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.util.EnumParticleTypes
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTitanium
extends Block {
    static final int tickRate = 100;

    public BlockTitanium() {
        super(Material.field_151573_f);
        this.func_149663_c("titanium_block");
        this.setRegistryName("titanium_block");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        this.func_149711_c(5.0f);
        this.func_149752_b(5.0f);
        this.setHarvestLevel("pickaxe", 2);
        this.func_149715_a(0.5f);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    @SideOnly(value=Side.CLIENT)
    public void func_180655_c(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(20) == 0) {
            this.sparkle(worldIn, pos);
        }
    }

    private void sparkle(World worldIn, BlockPos pos) {
        int par2 = pos.func_177958_n();
        int par3 = pos.func_177956_o();
        int par4 = pos.func_177952_p();
        Random var5 = worldIn.field_73012_v;
        double var6 = 0.0625;
        for (int var8 = 0; var8 < 6; ++var8) {
            double var9 = (float)par2 + var5.nextFloat();
            double var11 = (float)par3 + var5.nextFloat();
            double var13 = (float)par4 + var5.nextFloat();
            if (var8 == 0 && !worldIn.func_180495_p(new BlockPos(par2, par3 + 1, par4)).func_185914_p()) {
                var11 = (double)(par3 + 1) + var6;
            }
            if (var8 == 1 && !worldIn.func_180495_p(new BlockPos(par2, par3 - 1, par4)).func_185914_p()) {
                var11 = (double)(par3 + 0) - var6;
            }
            if (var8 == 2 && !worldIn.func_180495_p(new BlockPos(par2, par3, par4 - 1)).func_185914_p()) {
                var13 = (double)(par4 + 1) + var6;
            }
            if (var8 == 3 && !worldIn.func_180495_p(new BlockPos(par2, par3, par4 - 1)).func_185914_p()) {
                var13 = (double)(par4 + 0) - var6;
            }
            if (var8 == 4 && !worldIn.func_180495_p(new BlockPos(par2 + 1, par3, par4)).func_185914_p()) {
                var9 = (double)(par2 + 1) + var6;
            }
            if (var8 == 5 && !worldIn.func_180495_p(new BlockPos(par2 - 1, par3, par4)).func_185914_p()) {
                var9 = (double)(par2 + 0) - var6;
            }
            if (!(var9 < (double)par2 || var9 > (double)(par2 + 1) || var11 < 0.0 || var11 > (double)(par3 + 1) || var13 < (double)par4) && !(var13 > (double)(par4 + 1))) continue;
            int which = var5.nextInt(3);
            if (which == 0) {
                worldIn.func_175688_a(EnumParticleTypes.FLAME, var9, var11, var13, 0.0, 0.0, 0.0, new int[0]);
            }
            if (which == 1) {
                worldIn.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var9, var11, var13, 0.0, 0.0, 0.0, new int[0]);
            }
            if (which != 2) continue;
            worldIn.func_175688_a(EnumParticleTypes.REDSTONE, var9, var11, var13, 0.0, 0.0, 0.0, new int[0]);
        }
    }
}

