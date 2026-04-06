/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockReed
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockReed;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class BlockLettuce
extends BlockReed {
    protected BlockLettuce(int par1) {
        float var3 = 0.375f;
        this.func_149676_a(0.5f - var3, 0.0f, 0.5f - var3, 0.5f + var3, 1.0f, 0.5f + var3);
        this.func_149675_a(true);
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        Block bid = par1World.func_147439_a(par2, par3 - 1, par4);
        if (bid == Blocks.field_150350_a) {
            return false;
        }
        return bid == OreSpawnMain.MyLettucePlant1 || bid == OreSpawnMain.MyLettucePlant2 || bid == OreSpawnMain.MyLettucePlant3 || bid == OreSpawnMain.MyLettucePlant4 || bid == Blocks.field_150349_c || bid == Blocks.field_150346_d || bid == Blocks.field_150458_ak;
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        boolean dontGrow = false;
        if (par1World.field_72995_K) {
            return;
        }
        int var7 = par1World.func_72805_g(par2, par3, par4);
        if ((var7 &= 0xFF) >= 4) {
            Block bid = par1World.func_147439_a(par2, par3, par4);
            if (bid == OreSpawnMain.MyLettucePlant1) {
                par1World.func_147465_d(par2, par3, par4, OreSpawnMain.MyLettucePlant2, 0, 2);
            } else if (bid == OreSpawnMain.MyLettucePlant2) {
                par1World.func_147465_d(par2, par3, par4, OreSpawnMain.MyLettucePlant3, 0, 2);
            } else if (bid == OreSpawnMain.MyLettucePlant3) {
                par1World.func_147465_d(par2, par3, par4, OreSpawnMain.MyLettucePlant4, 0, 2);
            }
        } else {
            Block bid = par1World.func_147439_a(par2, par3, par4);
            par1World.func_147465_d(par2, par3, par4, bid, var7 + 1, 2);
        }
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return OreSpawnMain.MyLettuce;
    }

    public int func_149745_a(Random par1Random) {
        if (this == OreSpawnMain.MyLettucePlant4) {
            return 2 + par1Random.nextInt(3);
        }
        return 0;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

