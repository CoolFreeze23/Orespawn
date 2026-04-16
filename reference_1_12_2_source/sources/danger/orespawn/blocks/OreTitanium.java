/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.EnumHand
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OreTitanium
extends Block {
    static final int tickRate = 100;
    private boolean glowing = false;
    private int glowcount = 0;

    public OreTitanium() {
        super(Material.field_151576_e);
        this.func_149663_c("titanium_ore");
        this.setRegistryName("titanium_ore");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        this.func_149711_c(5.0f);
        this.func_149752_b(5.0f);
        this.setHarvestLevel("pickaxe", 2);
        this.func_149715_a(0.2f);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    public void func_176199_a(World worldIn, BlockPos pos, Entity entityIn) {
        this.glow(worldIn, pos);
        super.func_176199_a(worldIn, pos, entityIn);
    }

    public boolean func_180639_a(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        this.glow(worldIn, pos);
        return super.func_180639_a(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    public void func_180649_a(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        this.glow(worldIn, pos);
        super.func_180649_a(worldIn, pos, playerIn);
    }

    @SideOnly(value=Side.CLIENT)
    public void func_180655_c(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (this.glowing) {
            this.sparkle(worldIn, pos);
            if (this.glowcount > 0) {
                --this.glowcount;
            } else {
                this.glowing = false;
            }
        }
    }

    private void glow(World par1World, BlockPos pos) {
        this.glowing = true;
        this.glowcount = 10;
        this.sparkle(par1World, pos);
    }

    private void sparkle(World worldIn, BlockPos pos) {
        int par2 = pos.func_177958_n();
        int par3 = pos.func_177956_o();
        int par4 = pos.func_177952_p();
        Random var5 = worldIn.field_73012_v;
        double var6 = 0.0625;
        for (int var8 = 0; var8 < 6; ++var8) {
            int which;
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
            if (!(var9 < (double)par2 || var9 > (double)(par2 + 1) || var11 < 0.0 || var11 > (double)(par3 + 1) || var13 < (double)par4) && !(var13 > (double)(par4 + 1)) || (which = var5.nextInt(3)) != 2) continue;
            worldIn.func_175688_a(EnumParticleTypes.REDSTONE, var9, var11, var13, 0.0, 0.0, 0.0, new int[0]);
        }
    }
}

