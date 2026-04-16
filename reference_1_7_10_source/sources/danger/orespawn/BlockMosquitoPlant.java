/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class BlockMosquitoPlant
extends BlockCrops {
    @SideOnly(value=Side.CLIENT)
    private IIcon[] field_94364_a;

    public BlockMosquitoPlant(int par1) {
        this.func_149675_a(true);
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        super.func_149674_a(par1World, par2, par3, par4, par5Random);
        if (par1World.field_72995_K) {
            return;
        }
        if (OreSpawnMain.MosquitoEnable == 0) {
            return;
        }
        int rate = par1World.func_72805_g(par2, par3, par4);
        rate &= 7;
        if ((rate = 7 - rate) > 1 && OreSpawnMain.OreSpawnRand.nextInt(rate) != 0) {
            return;
        }
        Block bid = par1World.func_147439_a(par2, par3 + 1, par4);
        if (bid == Blocks.field_150350_a) {
            int howmany = 2 + OreSpawnMain.OreSpawnRand.nextInt(5);
            for (int i = 0; i < howmany; ++i) {
                BlockMosquitoPlant.spawnCreature((World)par1World, (String)"Mosquito", (double)((double)par2 + 0.5), (double)((double)par3 + 1.01), (double)((double)par4 + 0.5));
            }
        }
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

    public IIcon func_149691_a(int par1, int par2) {
        if (par2 < 7) {
            if (par2 >= 6) {
                par2 = 4;
            }
            return this.field_94364_a[par2 >> 1];
        }
        return this.field_94364_a[3];
    }

    public int func_149745_a(Random par1Random) {
        return 1 + par1Random.nextInt(5);
    }

    protected Item func_149866_i() {
        return OreSpawnMain.MyMosquitoSeed;
    }

    protected Item func_149865_P() {
        return null;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister par1IIconRegister) {
        this.field_94364_a = new IIcon[4];
        for (int i = 0; i < this.field_94364_a.length; ++i) {
            this.field_94364_a[i] = par1IIconRegister.func_94245_a("OreSpawn:mosquito_" + i);
        }
    }
}

