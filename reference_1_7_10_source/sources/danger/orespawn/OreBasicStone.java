/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/*
 * Exception performing whole class analysis ignored.
 */
public class OreBasicStone
extends Block {
    public OreBasicStone(int par1, float f1, float f2) {
        super(Material.field_151576_e);
        this.func_149711_c(f1);
        this.func_149752_b(f2);
        this.func_149647_a(CreativeTabs.field_78030_b);
        this.func_149675_a(false);
    }

    public void func_149664_b(World par1World, int par2, int par3, int par4, int par5) {
        int i;
        int num;
        if (!par1World.field_72995_K && this == OreSpawnMain.CrystalRat) {
            num = 1 + par1World.field_73012_v.nextInt(10);
            for (i = 0; i < num; ++i) {
                OreBasicStone.spawnCreature((World)par1World, (int)0, (String)"Rat", (double)((double)par2 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2), (double)((double)par3 + 0.01), (double)((double)par4 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2));
            }
        }
        if (!par1World.field_72995_K && this == OreSpawnMain.CrystalFairy) {
            num = 1 + par1World.field_73012_v.nextInt(6);
            for (i = 0; i < num; ++i) {
                OreBasicStone.spawnCreature((World)par1World, (int)0, (String)"Fairy", (double)((double)par2 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2), (double)((double)par3 + 0.01), (double)((double)par4 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2));
            }
        }
        if (!par1World.field_72995_K && this == OreSpawnMain.RedAntTroll) {
            num = 15 + par1World.field_73012_v.nextInt(6);
            for (i = 0; i < num; ++i) {
                OreBasicStone.spawnCreature((World)par1World, (int)0, (String)"Red Ant", (double)((double)par2 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2), (double)((double)par3 + 0.01), (double)((double)par4 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2));
            }
        }
        if (!par1World.field_72995_K && this == OreSpawnMain.TermiteTroll) {
            num = 15 + par1World.field_73012_v.nextInt(6);
            for (i = 0; i < num; ++i) {
                OreBasicStone.spawnCreature((World)par1World, (int)0, (String)"Termite", (double)((double)par2 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2), (double)((double)par3 + 0.01), (double)((double)par4 + 0.5 + (double)(par1World.field_73012_v.nextFloat() - par1World.field_73012_v.nextFloat()) * 0.2));
            }
        }
        super.func_149664_b(par1World, par2, par3, par4, par5);
    }

    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    public boolean func_149662_c() {
        return OreSpawnMain.current_dimension == OreSpawnMain.DimensionID5;
    }

    public boolean func_149686_d() {
        return OreSpawnMain.current_dimension == OreSpawnMain.DimensionID5;
    }

    public static Entity spawnCreature(World par0World, int par1, String name, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = name == null ? EntityList.func_75616_a((int)par1, (World)par0World) : EntityList.func_75620_a((String)name, (World)par0World);
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

