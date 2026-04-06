/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockReed
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.TheQueen;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockReed;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class QueenSpawnerBlock
extends BlockReed {
    protected QueenSpawnerBlock(int par1) {
        float var3 = 0.375f;
        this.func_149676_a(0.5f - var3, 0.0f, 0.5f - var3, 0.5f + var3, 1.0f, 0.5f + var3);
        this.func_149675_a(true);
        this.func_149647_a(CreativeTabs.field_78031_c);
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        return par1World.func_147439_a(par2, par3 - 1, par4).func_149688_o().func_76220_a();
    }

    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (!par1World.field_72995_K) {
            this.func_149674_a(par1World, par2, par3, par4, par5Random);
            return;
        }
        if (par1World.field_73012_v.nextInt(20) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 20; ++j1) {
            par1World.func_72869_a("fireworksSpark", (double)((float)par2 + par1World.field_73012_v.nextFloat()), (double)par3 + (double)par1World.field_73012_v.nextFloat(), (double)((float)par4 + par1World.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
        }
    }

    public void func_149726_b(World world, int x, int y, int z) {
        if (world.field_72995_K) {
            return;
        }
        world.func_147464_a(x, y, z, (Block)this, 100);
    }

    public void func_149664_b(World par1World, int par2, int par3, int par4, int par5) {
        this.func_149674_a(par1World, par2, par3, par4, null);
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_72995_K) {
            return;
        }
        if (OreSpawnMain.TheQueenEnable != 0) {
            QueenSpawnerBlock.spawnTheQueen(par1World, par2, par3 + 8, par4);
        }
        par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
        par1World.func_147465_d(par2, par3 + 1, par4, Blocks.field_150350_a, 0, 2);
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return Item.func_150898_a((Block)OreSpawnMain.MyQueenSpawnerBlock);
    }

    public int func_149745_a(Random par1Random) {
        return 1;
    }

    public static Entity spawnTheQueen(World par0World, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)"The Queen", (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
            ((TheQueen)var8).setGuardMode(1);
        }
        return var8;
    }

    public boolean func_149718_j(World par1World, int par2, int par3, int par4) {
        this.func_149674_a(par1World, par2, par3, par4, null);
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

