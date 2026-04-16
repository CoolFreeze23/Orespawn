/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockTorch
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.EnumParticleTypes
 *  net.minecraft.util.ResourceLocation
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
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockExtremeTorch
extends BlockTorch {
    public BlockExtremeTorch() {
        this.func_149663_c("extreme_torch");
        this.setRegistryName("extreme_torch");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        ModBlocks.BLOCKS.add((Block)this);
        this.func_149715_a(1.0f);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    @SideOnly(value=Side.CLIENT)
    public void func_180655_c(IBlockState stateIn, World par1World, BlockPos pos, Random par5Random) {
        int var6 = this.func_176201_c(par1World.func_180495_p(new BlockPos(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p())));
        double var7 = (float)pos.func_177958_n() + 0.5f;
        double var9 = (float)pos.func_177956_o() + 0.7f;
        double var11 = (float)pos.func_177952_p() + 0.5f;
        double var13 = 0.213;
        double var15 = 0.271;
        if (var6 == 1) {
            par1World.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.FLAME, var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.REDSTONE, var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0, new int[0]);
        } else if (var6 == 2) {
            par1World.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.FLAME, var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.REDSTONE, var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0, new int[0]);
        } else if (var6 == 3) {
            par1World.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.FLAME, var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.REDSTONE, var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0, new int[0]);
        } else if (var6 == 4) {
            par1World.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.FLAME, var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.REDSTONE, var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0, new int[0]);
        } else {
            par1World.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var7, var9, var11, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.FLAME, var7, var9, var11, 0.0, 0.0, 0.0, new int[0]);
            par1World.func_175688_a(EnumParticleTypes.REDSTONE, var7, var9, var11, 0.0, 0.0, 0.0, new int[0]);
        }
        this.onBlockPlacedBy(par1World, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), null, null);
    }

    public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
        return super.func_176196_c(par1World, new BlockPos(par2, par3, par4));
    }

    public void onBlockPlacedBy(World world, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {
        int x = par2;
        int y = par3;
        int z = par4;
        boolean found = false;
        super.func_180633_a(world, new BlockPos(par2, par3, par4), Blocks.field_150350_a.func_176223_P(), par5EntityLiving, par6ItemStack);
    }

    private static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_188429_b((ResourceLocation)new ResourceLocation(par1), (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }
}

