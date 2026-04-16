/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
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

/*
 * Exception performing whole class analysis ignored.
 */
public class IslandBlock
extends BlockReed {
    protected IslandBlock(int par1) {
        float var3 = 0.375f;
        this.func_149676_a(0.5f - var3, 0.0f, 0.5f - var3, 0.5f + var3, 1.0f, 0.5f + var3);
        this.func_149675_a(true);
        this.func_149647_a(CreativeTabs.field_78031_c);
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        return par1World.func_147439_a(par2, par3 - 1, par4).func_149688_o().func_76220_a();
    }

    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (par1World.field_73012_v.nextInt(20) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 20; ++j1) {
            par1World.func_72869_a("happyVillager", (double)((float)par2 + par1World.field_73012_v.nextFloat()), (double)par3 + (double)par1World.field_73012_v.nextFloat(), (double)((float)par4 + par1World.field_73012_v.nextFloat()), 0.0, 0.0, 0.0);
        }
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        boolean isok = true;
        if (par1World.field_72995_K) {
            return;
        }
        int n = 1 + par1World.field_73012_v.nextInt(3);
        int m = 64;
        if (OreSpawnMain.IslandSizeFactor == 2) {
            m = 55;
        }
        if (OreSpawnMain.IslandSizeFactor == 1) {
            m = 45;
        }
        for (int i = 0; i < n; ++i) {
            int height = 12 + par1World.field_73012_v.nextInt(m);
            isok = true;
            block1: for (int k = -10; k <= 10; ++k) {
                for (int j = -10; j <= 10; ++j) {
                    Block bid = par1World.func_147439_a(par2 + j, par3 + height, par4 + k);
                    if (bid == Blocks.field_150350_a) continue;
                    isok = false;
                    continue block1;
                }
            }
            if (!isok) continue;
            if (par1World.field_73012_v.nextInt(25) == 1) {
                IslandBlock.spawnCreature((World)par1World, (String)"Island", (double)par2, (double)(par3 + height), (double)par4);
                continue;
            }
            IslandBlock.spawnCreature((World)par1World, (String)"IslandToo", (double)par2, (double)(par3 + height), (double)par4);
        }
        par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
        par1World.func_147465_d(par2, par3 + 1, par4, Blocks.field_150350_a, 0, 2);
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return Item.func_150898_a((Block)OreSpawnMain.MyIslandBlock);
    }

    public int func_149745_a(Random par1Random) {
        return 1;
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

