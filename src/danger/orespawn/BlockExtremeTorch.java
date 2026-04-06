/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.BlockTorch
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockExtremeTorch
extends BlockTorch {
    public BlockExtremeTorch(int par1) {
        this.func_149647_a(CreativeTabs.field_78028_d);
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        int var6 = par1World.func_72805_g(par2, par3, par4);
        double var7 = (float)par2 + 0.5f;
        double var9 = (float)par3 + 0.7f;
        double var11 = (float)par4 + 0.5f;
        double var13 = 0.213;
        double var15 = 0.271;
        if (var6 == 1) {
            par1World.func_72869_a("smoke", var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7 - var15, var9 + var13, var11, 0.0, 0.0, 0.0);
        } else if (var6 == 2) {
            par1World.func_72869_a("smoke", var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7 + var15, var9 + var13, var11, 0.0, 0.0, 0.0);
        } else if (var6 == 3) {
            par1World.func_72869_a("smoke", var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7, var9 + var13, var11 - var15, 0.0, 0.0, 0.0);
        } else if (var6 == 4) {
            par1World.func_72869_a("smoke", var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7, var9 + var13, var11 + var15, 0.0, 0.0, 0.0);
        } else {
            par1World.func_72869_a("smoke", var7, var9, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("flame", var7, var9, var11, 0.0, 0.0, 0.0);
            par1World.func_72869_a("reddust", var7, var9, var11, 0.0, 0.0, 0.0);
        }
        this.func_149689_a(par1World, par2, par3, par4, null, null);
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        return super.func_149742_c(par1World, par2, par3, par4);
    }

    public void func_149689_a(World world, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {
        int x = par2;
        int y = par3;
        int z = par4;
        boolean found = false;
        if (world.func_147439_a(x, y - 1, z) == OreSpawnMain.MyEyeOfEnderBlock) {
            block0: for (int tries = 0; tries < 100 && !found; ++tries) {
                x = world.field_73012_v.nextInt(2) == 0 ? par2 + 4 + world.field_73012_v.nextInt(3) - world.field_73012_v.nextInt(3) : par2 - 4 + world.field_73012_v.nextInt(3) - world.field_73012_v.nextInt(3);
                z = world.field_73012_v.nextInt(2) == 0 ? par4 + 4 + world.field_73012_v.nextInt(3) - world.field_73012_v.nextInt(3) : par4 - 4 + world.field_73012_v.nextInt(3) - world.field_73012_v.nextInt(3);
                for (y = par3 - 2; y <= par3 + 2; ++y) {
                    if (!world.func_147439_a(x, y - 1, z).func_149688_o().func_76220_a() || world.func_147439_a(x, y, z) != Blocks.field_150350_a || world.func_147439_a(x, y + 1, z) != Blocks.field_150350_a) continue;
                    found = true;
                    continue block0;
                }
            }
            if (found) {
                if (!world.field_72995_K) {
                    Entity ent = null;
                    ent = BlockExtremeTorch.spawnCreature(world, "Cephadrome", (double)x + 0.5, (double)y + 0.01, (double)z + 0.5);
                } else {
                    for (int var3 = 0; var3 < 16; ++var3) {
                        world.func_72869_a("smoke", (double)((float)par2 + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), (double)((float)par3 + world.field_73012_v.nextFloat()), (double)((float)par4 + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
                        world.func_72869_a("explode", (double)((float)par2 + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), (double)((float)par3 + world.field_73012_v.nextFloat()), (double)((float)par4 + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
                        world.func_72869_a("reddust", (double)((float)par2 + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), (double)((float)par3 + world.field_73012_v.nextFloat()), (double)((float)par4 + world.field_73012_v.nextFloat() - world.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
                    }
                }
                if (par5EntityLiving != null) {
                    par5EntityLiving.field_70170_p.func_72956_a((Entity)par5EntityLiving, "random.explode", 1.0f, world.field_73012_v.nextFloat() * 0.2f + 0.9f);
                } else {
                    world.func_72980_b((double)par2, (double)par3, (double)par4, "random.explode", 1.0f, world.field_73012_v.nextFloat() * 0.2f + 0.9f, false);
                }
                world.func_147449_b(par2, par3, par4, Blocks.field_150350_a);
            }
        }
        super.func_149689_a(world, par2, par3, par4, par5EntityLiving, par6ItemStack);
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

