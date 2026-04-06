/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.Item$ToolMaterial
 *  net.minecraft.item.ItemPickaxe
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.Boyfriend;
import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class UltimatePickaxe
extends ItemPickaxe {
    private int weaponDamage = 15;

    public UltimatePickaxe(int par1, Item.ToolMaterial par2) {
        super(par2);
        this.field_77777_bU = 1;
        this.func_77656_e(3000);
        this.func_77637_a(CreativeTabs.field_78040_i);
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par1ItemStack.func_77966_a(Enchantment.field_77349_p, 5);
        par1ItemStack.func_77966_a(Enchantment.field_77346_s, 5);
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77349_p.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            stack.func_77966_a(Enchantment.field_77349_p, 5);
            stack.func_77966_a(Enchantment.field_77346_s, 5);
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        this.onUsingTick(stack, null, 0);
    }

    public boolean canHarvestBlock(Block par1Block) {
        return true;
    }

    public int getDamageVsEntity(Entity par1Entity) {
        if (par1Entity instanceof Girlfriend) {
            return 1;
        }
        if (par1Entity instanceof Boyfriend) {
            return 1;
        }
        if (par1Entity instanceof EntityPlayer) {
            return 1;
        }
        return this.weaponDamage;
    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity != null && OreSpawnMain.ultimate_sword_pvp == 0) {
            EntityTameable t;
            if (entity instanceof EntityPlayer || entity instanceof Girlfriend || entity instanceof Boyfriend) {
                return true;
            }
            if (entity instanceof EntityTameable && (t = (EntityTameable)entity).func_70909_n()) {
                return true;
            }
        }
        return false;
    }

    private ItemStack dropItemAnItem(World world, int x, int y, int z, Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(world, (double)x, (double)y, (double)z, is);
        if (var3 != null) {
            world.func_72838_d((Entity)var3);
        }
        return is;
    }

    public boolean func_150894_a(ItemStack par1ItemStack, World par2World, Block par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase) {
        if ((double)par3.func_149712_f(par2World, par4, par5, par6) != 0.0) {
            par1ItemStack.func_77972_a(1, par7EntityLivingBase);
        }
        if (!par2World.field_72995_K) {
            if (par3 == Blocks.field_150366_p && par2World.field_73012_v.nextInt(2) != 0) {
                this.dropItemAnItem(par2World, par4, par5, par6, Items.field_151042_j, 1 + par2World.field_73012_v.nextInt(2));
            }
            if (par3 == Blocks.field_150352_o && par2World.field_73012_v.nextInt(2) != 0) {
                this.dropItemAnItem(par2World, par4, par5, par6, Items.field_151043_k, 1 + par2World.field_73012_v.nextInt(2));
            }
            if (par3 == Blocks.field_150348_b && par2World.field_73012_v.nextInt(100) == 2) {
                int i = par2World.field_73012_v.nextInt(10);
                if (i == 0) {
                    this.dropItemAnItem(par2World, par4, par5, par6, Items.field_151045_i, 1);
                }
                if (i == 1) {
                    this.dropItemAnItem(par2World, par4, par5, par6, Items.field_151166_bC, 1);
                }
                if (i == 2) {
                    this.dropItemAnItem(par2World, par4, par5, par6, OreSpawnMain.MyAmethyst, 1);
                }
                if (i == 3) {
                    this.dropItemAnItem(par2World, par4, par5, par6, OreSpawnMain.MyRuby, 1);
                }
                if (i == 4) {
                    this.dropItemAnItem(par2World, par4, par5, par6, OreSpawnMain.UraniumNugget, 1);
                }
                if (i == 5) {
                    this.dropItemAnItem(par2World, par4, par5, par6, OreSpawnMain.TitaniumNugget, 1);
                }
            }
        }
        return true;
    }

    public String getMaterialName() {
        return "Uranium/Titanium";
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

