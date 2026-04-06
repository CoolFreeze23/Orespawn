/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockContainer
 *  net.minecraft.block.material.Material
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.Container
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.tileentity.TileEntity
 *  net.minecraft.util.IIcon
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.TileEntityCrystalFurnace;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class CrystalFurnace
extends BlockContainer {
    private final Random furnaceRand = new Random();
    private final boolean isActive;
    private static boolean keepFurnaceInventory;
    @SideOnly(value=Side.CLIENT)
    private IIcon furnaceIconTop;
    @SideOnly(value=Side.CLIENT)
    private IIcon furnaceIconFront;

    protected CrystalFurnace(int par1, boolean par2, float f1, float f2) {
        super(Material.field_151576_e);
        this.isActive = par2;
        if (!par2) {
            this.func_149647_a(CreativeTabs.field_78031_c);
        } else {
            this.func_149715_a(0.6f);
        }
        this.func_149711_c(f1);
        this.func_149752_b(f2);
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return false;
    }

    public Item func_149650_a(int par1, Random par2Random, int par3) {
        return Item.func_150898_a((Block)OreSpawnMain.CrystalFurnaceBlock);
    }

    @SideOnly(value=Side.CLIENT)
    public Item idPicked(World par1World, int par2, int par3, int par4) {
        return Item.func_150898_a((Block)OreSpawnMain.CrystalFurnaceBlock);
    }

    @SideOnly(value=Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        return par1 == 1 ? this.furnaceIconTop : (par1 == 0 ? this.furnaceIconTop : (par1 != par2 ? this.field_149761_L : this.furnaceIconFront));
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149651_a(IIconRegister par1IIconRegister) {
        this.field_149761_L = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_side");
        this.furnaceIconFront = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + (this.isActive ? "_front_off" : "_front_off"));
        this.furnaceIconTop = par1IIconRegister.func_94245_a("OreSpawn:" + this.func_149739_a().substring(5) + "_top");
    }

    public static void updateFurnaceBlockState(boolean par0, World par1World, int par2, int par3, int par4) {
        int l = par1World.func_72805_g(par2, par3, par4);
        TileEntity tileentity = par1World.func_147438_o(par2, par3, par4);
        keepFurnaceInventory = true;
        if (par0) {
            par1World.func_147449_b(par2, par3, par4, OreSpawnMain.CrystalFurnaceOnBlock);
        } else {
            par1World.func_147449_b(par2, par3, par4, (Block)OreSpawnMain.CrystalFurnaceBlock);
        }
        keepFurnaceInventory = false;
        par1World.func_72921_c(par2, par3, par4, l, 2);
        if (tileentity != null) {
            tileentity.func_145829_t();
            par1World.func_147455_a(par2, par3, par4, tileentity);
        }
    }

    public void func_149726_b(World par1World, int par2, int par3, int par4) {
        super.func_149726_b(par1World, par2, par3, par4);
        this.setDefaultDirection(par1World, par2, par3, par4);
    }

    private void setDefaultDirection(World par1World, int par2, int par3, int par4) {
        if (!par1World.field_72995_K) {
            Block l = par1World.func_147439_a(par2, par3, par4 - 1);
            Block i1 = par1World.func_147439_a(par2, par3, par4 + 1);
            Block j1 = par1World.func_147439_a(par2 - 1, par3, par4);
            Block k1 = par1World.func_147439_a(par2 + 1, par3, par4);
            int b0 = 3;
            if (l.func_149730_j() && !i1.func_149730_j()) {
                b0 = 3;
            }
            if (i1.func_149730_j() && !l.func_149730_j()) {
                b0 = 2;
            }
            if (j1.func_149730_j() && !k1.func_149730_j()) {
                b0 = 5;
            }
            if (k1.func_149730_j() && !j1.func_149730_j()) {
                b0 = 4;
            }
            par1World.func_72921_c(par2, par3, par4, b0, 2);
        }
    }

    public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        if (par1World.field_72995_K) {
            return true;
        }
        TileEntityCrystalFurnace tileentitycrystalfurnace = (TileEntityCrystalFurnace)par1World.func_147438_o(par2, par3, par4);
        if (tileentitycrystalfurnace != null) {
            par5EntityPlayer.openGui((Object)OreSpawnMain.instance, 0, par1World, par2, par3, par4);
        }
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_149734_b(World par1World, int par2, int par3, int par4, Random par5Random) {
        if (this.isActive) {
            int l = par1World.func_72805_g(par2, par3, par4);
            float f = (float)par2 + 0.5f;
            float f1 = (float)par3 + 0.0f + par5Random.nextFloat() * 6.0f / 16.0f;
            float f2 = (float)par4 + 0.5f;
            float f3 = par5Random.nextFloat() * 0.6f - 0.3f;
            float f4 = par5Random.nextFloat() * 0.6f - 0.3f;
            if (l == 4) {
                par1World.func_72869_a("smoke", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0, 0.0, 0.0);
                par1World.func_72869_a("flame", (double)(f - f3), (double)f1, (double)(f2 + f4), 0.0, 0.0, 0.0);
            } else if (l == 5) {
                par1World.func_72869_a("smoke", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0, 0.0, 0.0);
                par1World.func_72869_a("flame", (double)(f + f3), (double)f1, (double)(f2 + f4), 0.0, 0.0, 0.0);
            } else if (l == 2) {
                par1World.func_72869_a("smoke", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0, 0.0, 0.0);
                par1World.func_72869_a("flame", (double)(f + f4), (double)f1, (double)(f2 - f3), 0.0, 0.0, 0.0);
            } else if (l == 3) {
                par1World.func_72869_a("smoke", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0, 0.0, 0.0);
                par1World.func_72869_a("flame", (double)(f + f4), (double)f1, (double)(f2 + f3), 0.0, 0.0, 0.0);
            }
        }
    }

    public void func_149689_a(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
        int l = MathHelper.func_76128_c((double)((double)(par5EntityLivingBase.field_70177_z * 4.0f / 360.0f) + 0.5)) & 3;
        if (l == 0) {
            par1World.func_72921_c(par2, par3, par4, 2, 2);
        }
        if (l == 1) {
            par1World.func_72921_c(par2, par3, par4, 5, 2);
        }
        if (l == 2) {
            par1World.func_72921_c(par2, par3, par4, 3, 2);
        }
        if (l == 3) {
            par1World.func_72921_c(par2, par3, par4, 4, 2);
        }
        if (par6ItemStack.func_82837_s()) {
            ((TileEntityCrystalFurnace)par1World.func_147438_o(par2, par3, par4)).func_145951_a(par6ItemStack.func_82833_r());
        }
    }

    public void func_149749_a(World par1World, int par2, int par3, int par4, Block par5, int par6) {
        TileEntityCrystalFurnace tileentitycrystalfurnace;
        if (!keepFurnaceInventory && (tileentitycrystalfurnace = (TileEntityCrystalFurnace)par1World.func_147438_o(par2, par3, par4)) != null) {
            for (int j1 = 0; j1 < tileentitycrystalfurnace.func_70302_i_(); ++j1) {
                ItemStack itemstack = tileentitycrystalfurnace.func_70301_a(j1);
                if (itemstack == null) continue;
                float f = this.furnaceRand.nextFloat() * 0.8f + 0.1f;
                float f1 = this.furnaceRand.nextFloat() * 0.8f + 0.1f;
                float f2 = this.furnaceRand.nextFloat() * 0.8f + 0.1f;
                while (itemstack.field_77994_a > 0) {
                    int k1 = this.furnaceRand.nextInt(21) + 10;
                    if (k1 > itemstack.field_77994_a) {
                        k1 = itemstack.field_77994_a;
                    }
                    itemstack.field_77994_a -= k1;
                    EntityItem entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.func_77973_b(), k1, itemstack.func_77960_j()));
                    if (itemstack.func_77942_o()) {
                        entityitem.func_92059_d().func_77982_d((NBTTagCompound)itemstack.func_77978_p().func_74737_b());
                    }
                    float f3 = 0.05f;
                    entityitem.field_70159_w = (float)this.furnaceRand.nextGaussian() * f3;
                    entityitem.field_70181_x = (float)this.furnaceRand.nextGaussian() * f3 + 0.2f;
                    entityitem.field_70179_y = (float)this.furnaceRand.nextGaussian() * f3;
                    par1World.func_72838_d((Entity)entityitem);
                }
            }
            par1World.func_147453_f(par2, par3, par4, par5);
        }
        super.func_149749_a(par1World, par2, par3, par4, par5, par6);
    }

    public boolean func_149740_M() {
        return true;
    }

    public int func_149736_g(World par1World, int par2, int par3, int par4, int par5) {
        return Container.func_94526_b((IInventory)((IInventory)par1World.func_147438_o(par2, par3, par4)));
    }

    public TileEntity func_149915_a(World var1, int var2) {
        return new TileEntityCrystalFurnace();
    }
}

