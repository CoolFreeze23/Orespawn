/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.util.IIcon
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CrystalAntBlock
extends Block {
    @SideOnly(value=Side.CLIENT)
    private IIcon[] field_94364_a;

    protected CrystalAntBlock(int par1) {
        super(Material.field_151577_b);
        this.func_149675_a(true);
        this.func_149647_a(CreativeTabs.field_78030_b);
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        if (this.field_94364_a == null) {
            return null;
        }
        return par1 == 1 ? this.field_94364_a[0] : (par1 == 0 ? this.field_94364_a[1] : this.field_94364_a[2]);
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon getBlockTexture(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        if (this.field_94364_a == null) {
            return null;
        }
        if (par5 == 1) {
            return this.field_94364_a[0];
        }
        if (par5 == 0) {
            return this.field_94364_a[1];
        }
        return this.field_94364_a[2];
    }

    public void func_149674_a(World par1World, int par2, int par3, int par4, Random par5Random) {
        int howmany = 0;
        if (!par1World.field_72995_K) {
            if (par1World.func_72896_J()) {
                return;
            }
            Block bid = par1World.func_147439_a(par2, par3 + 1, par4);
            if (bid == Blocks.field_150350_a) {
                howmany = OreSpawnMain.OreSpawnRand.nextInt(6) + 2;
                for (int i = 0; i < howmany; ++i) {
                    if (this == OreSpawnMain.MyAntBlock) {
                        if (OreSpawnMain.BlackAntEnable == 0) continue;
                        CrystalAntBlock.spawnCreature(par1World, "Ant", (double)par2 + 0.5, (double)par3 + 1.01, (double)par4 + 0.5);
                        continue;
                    }
                    if (this == OreSpawnMain.MyRedAntBlock) {
                        if (OreSpawnMain.RedAntEnable == 0) continue;
                        CrystalAntBlock.spawnCreature(par1World, "Red Ant", (double)par2 + 0.5, (double)par3 + 1.01, (double)par4 + 0.5);
                        continue;
                    }
                    if (this == OreSpawnMain.MyUnstableAntBlock) {
                        if (OreSpawnMain.UnstableAntEnable == 0) continue;
                        CrystalAntBlock.spawnCreature(par1World, "Unstable Ant", (double)par2 + 0.5, (double)par3 + 1.01, (double)par4 + 0.5);
                        continue;
                    }
                    if (this == OreSpawnMain.TermiteBlock) {
                        if (OreSpawnMain.TermiteEnable == 0) continue;
                        CrystalAntBlock.spawnCreature(par1World, "Termite", (double)par2 + 0.5, (double)par3 + 1.01, (double)par4 + 0.5);
                        continue;
                    }
                    if (this == OreSpawnMain.CrystalTermiteBlock) {
                        if (OreSpawnMain.TermiteEnable == 0) continue;
                        CrystalAntBlock.spawnCreature(par1World, "Termite", (double)par2 + 0.5, (double)par3 + 1.01, (double)par4 + 0.5);
                        continue;
                    }
                    if (OreSpawnMain.RainbowAntEnable == 0) continue;
                    CrystalAntBlock.spawnCreature(par1World, "Rainbow Ant", (double)par2 + 0.5, (double)par3 + 1.01, (double)par4 + 0.5);
                }
            }
        }
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return Item.func_150898_a((Block)this);
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

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return false;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister par1IIconRegister) {
        this.field_94364_a = new IIcon[3];
        this.field_94364_a[0] = par1IIconRegister.func_94245_a("OreSpawn:crystaltermite_top");
        this.field_94364_a[1] = par1IIconRegister.func_94245_a("OreSpawn:crystaltermite_bottom");
        this.field_94364_a[2] = par1IIconRegister.func_94245_a("OreSpawn:crystaltermite_side");
    }
}

