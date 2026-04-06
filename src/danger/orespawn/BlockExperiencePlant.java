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

public class BlockExperiencePlant
extends BlockReed {
    protected BlockExperiencePlant(int par1) {
        float var3 = 0.375f;
        this.func_149676_a(0.5f - var3, 0.0f, 0.5f - var3, 0.5f + var3, 1.0f, 0.5f + var3);
        this.func_149675_a(true);
    }

    public boolean func_149742_c(World par1World, int par2, int par3, int par4) {
        Block bid = par1World.func_147439_a(par2, par3 - 1, par4);
        if (bid == Blocks.field_150350_a) {
            return false;
        }
        return bid == Blocks.field_150349_c || bid == Blocks.field_150346_d || bid == Blocks.field_150458_ak;
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
        if (par1World.field_72995_K) {
            return;
        }
        if (par1World.field_73012_v.nextInt(10) != 1) {
            return;
        }
        par1World.func_147465_d(par2, par3, par4, Blocks.field_150350_a, 0, 2);
        OreSpawnMain.OreSpawnTrees.ExperienceTree(par1World, par2, par3 - 1, par4);
    }

    public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
        return Item.func_150898_a((Block)OreSpawnMain.MyExperiencePlant);
    }

    public int func_149745_a(Random par1Random) {
        return 1;
    }

    public int idPicked(World par1World, int par2, int par3, int par4) {
        return 0;
    }

    protected Item getSeedItem() {
        return OreSpawnMain.MyExperienceTreeSeed;
    }

    protected int getCropItem() {
        return 0;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister iconRegister) {
        this.field_149761_L = iconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5));
    }
}

