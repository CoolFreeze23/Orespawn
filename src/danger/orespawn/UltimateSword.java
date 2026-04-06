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
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.Item$ToolMaterial
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ItemSword
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.Boyfriend;
import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class UltimateSword
extends ItemSword {
    private int swingtimer = 0;
    private boolean leaf = false;

    public UltimateSword(int par1, Item.ToolMaterial par2EnumToolMaterial) {
        super(par2EnumToolMaterial);
        this.field_77777_bU = 1;
        this.func_77656_e(3000);
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (this == OreSpawnMain.MyChainsaw) {
            return;
        }
        if (this != OreSpawnMain.MyBattleAxe) {
            par1ItemStack.func_77966_a(Enchantment.field_77338_j, OreSpawnMain.UltimateSwordMagic);
            par1ItemStack.func_77966_a(Enchantment.field_77339_k, OreSpawnMain.UltimateSwordMagic);
            par1ItemStack.func_77966_a(Enchantment.field_77336_l, OreSpawnMain.UltimateSwordMagic);
            par1ItemStack.func_77966_a(Enchantment.field_77337_m, 1 + OreSpawnMain.UltimateSwordMagic / 2);
            par1ItemStack.func_77966_a(Enchantment.field_77335_o, 1 + OreSpawnMain.UltimateSwordMagic / 2);
            par1ItemStack.func_77966_a(Enchantment.field_77347_r, 1 + OreSpawnMain.UltimateSwordMagic / 2);
            par1ItemStack.func_77966_a(Enchantment.field_77334_n, 1 + OreSpawnMain.UltimateSwordMagic / 3);
        } else {
            par1ItemStack.func_77966_a(Enchantment.field_77335_o, 1 + OreSpawnMain.UltimateSwordMagic / 2);
            par1ItemStack.func_77966_a(Enchantment.field_77347_r, 1 + OreSpawnMain.UltimateSwordMagic / 2);
        }
    }

    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (this == OreSpawnMain.MyChainsaw && entityLiving != null && this.swingtimer == 0) {
            entityLiving.func_85030_a("orespawn:chainsawshort", 1.0f, entityLiving.field_70170_p.field_73012_v.nextFloat() * 0.2f + 0.9f);
            this.swingtimer = 50;
        }
        return false;
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (this == OreSpawnMain.MyChainsaw) {
            return;
        }
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77335_o.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            if (this != OreSpawnMain.MyBattleAxe) {
                stack.func_77966_a(Enchantment.field_77338_j, OreSpawnMain.UltimateSwordMagic);
                stack.func_77966_a(Enchantment.field_77339_k, OreSpawnMain.UltimateSwordMagic);
                stack.func_77966_a(Enchantment.field_77336_l, OreSpawnMain.UltimateSwordMagic);
                stack.func_77966_a(Enchantment.field_77337_m, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77335_o, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77347_r, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77334_n, 1 + OreSpawnMain.UltimateSwordMagic / 3);
            } else {
                stack.func_77966_a(Enchantment.field_77335_o, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77347_r, 1 + OreSpawnMain.UltimateSwordMagic / 2);
            }
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        if (this == OreSpawnMain.MyChainsaw) {
            if (this.swingtimer > 0) {
                --this.swingtimer;
            }
            if (par2World.field_72995_K && this.swingtimer > 0) {
                float f = 1.0f;
                float dx = (float)((double)f * Math.cos(Math.toRadians(par3Entity.field_70177_z + 90.0f + 45.0f)));
                float dz = (float)((double)f * Math.sin(Math.toRadians(par3Entity.field_70177_z + 90.0f + 45.0f)));
                if (par2World.field_73012_v.nextInt(8) == 0) {
                    par2World.func_72869_a("flame", par3Entity.field_70165_t + (double)dx, par3Entity.field_70163_u, par3Entity.field_70161_v + (double)dz, (double)((par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) / 20.0f), (double)(par2World.field_73012_v.nextFloat() / 10.0f), (double)((par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) / 20.0f));
                }
                if (par2World.field_73012_v.nextInt(2) == 0) {
                    par2World.func_72869_a("smoke", par3Entity.field_70165_t + (double)dx, par3Entity.field_70163_u, par3Entity.field_70161_v + (double)dz, (double)((par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) / 20.0f), (double)(par2World.field_73012_v.nextFloat() / 10.0f), (double)((par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) / 20.0f));
                }
                if (par2World.field_73012_v.nextInt(10) == 0) {
                    par2World.func_72869_a("fireworksSpark", par3Entity.field_70165_t + (double)dx, par3Entity.field_70163_u, par3Entity.field_70161_v + (double)dz, (double)((par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) / 20.0f), (double)(par2World.field_73012_v.nextFloat() / 5.0f), (double)((par2World.field_73012_v.nextFloat() - par2World.field_73012_v.nextFloat()) / 20.0f));
                }
            }
            return;
        }
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77335_o.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            if (this != OreSpawnMain.MyBattleAxe) {
                stack.func_77966_a(Enchantment.field_77338_j, OreSpawnMain.UltimateSwordMagic);
                stack.func_77966_a(Enchantment.field_77339_k, OreSpawnMain.UltimateSwordMagic);
                stack.func_77966_a(Enchantment.field_77336_l, OreSpawnMain.UltimateSwordMagic);
                stack.func_77966_a(Enchantment.field_77337_m, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77335_o, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77347_r, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77334_n, 1 + OreSpawnMain.UltimateSwordMagic / 3);
            } else {
                stack.func_77966_a(Enchantment.field_77335_o, 1 + OreSpawnMain.UltimateSwordMagic / 2);
                stack.func_77966_a(Enchantment.field_77347_r, 1 + OreSpawnMain.UltimateSwordMagic / 2);
            }
        }
    }

    public String getMaterialName() {
        return "Uranium/Titanium";
    }

    public boolean hitEntity(ItemStack par1ItemStack, EntityLiving par2EntityLiving, EntityLiving par3EntityLiving) {
        par1ItemStack.func_77972_a(1, (EntityLivingBase)par3EntityLiving);
        return true;
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
        if (this == OreSpawnMain.MyChainsaw && player != null) {
            this.findSomethingToHit(player);
        }
        return false;
    }

    public int func_77626_a(ItemStack par1ItemStack) {
        return 9000;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }

    private void findSomethingToHit(EntityPlayer player) {
        List var5 = player.field_70170_p.func_72872_a(EntityLivingBase.class, player.field_70121_D.func_72314_b(5.0, 5.0, 5.0));
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (!this.isSuitableTarget(var4, false, player)) continue;
            var4.func_70097_a(DamageSource.func_76365_a((EntityPlayer)player), (float)OreSpawnMain.chainsaw_stats.damage);
        }
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2, EntityPlayer player) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == player) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (OreSpawnMain.ultimate_sword_pvp == 0) {
            EntityTameable t;
            if (par1EntityLiving instanceof EntityPlayer || par1EntityLiving instanceof Girlfriend || par1EntityLiving instanceof Boyfriend) {
                return false;
            }
            if (par1EntityLiving instanceof EntityTameable && (t = (EntityTameable)par1EntityLiving).func_70909_n()) {
                return false;
            }
        }
        return this.MyCanSee(par1EntityLiving, player);
    }

    public boolean MyCanSee(EntityLivingBase e, EntityPlayer player) {
        int nblks = 10;
        double cx = player.field_70165_t;
        double cz = player.field_70161_v;
        float startx = (float)cx;
        float starty = (float)(player.field_70163_u + (double)1.4f);
        float startz = (float)cz;
        float dx = (float)((e.field_70165_t - (double)startx) / 10.0);
        float dy = (float)((e.field_70163_u + (double)(e.field_70131_O / 2.0f) - (double)starty) / 10.0);
        float dz = (float)((e.field_70161_v - (double)startz) / 10.0);
        if ((double)Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks = (int)((float)nblks * Math.abs(dx));
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if ((double)Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks = (int)((float)nblks * Math.abs(dy));
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if ((double)Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks = (int)((float)nblks * Math.abs(dz));
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            Block bid = player.field_70170_p.func_147439_a((int)(startx += dx), (int)(starty += dy), (int)(startz += dz));
            if (bid == Blocks.field_150350_a) continue;
            return false;
        }
        return true;
    }

    public boolean canHarvestBlock(Block par1Block) {
        return this.canCrush(par1Block);
    }

    private boolean canCrush(Block blockID) {
        if (this == OreSpawnMain.MyChainsaw) {
            if (blockID == Blocks.field_150321_G) {
                return true;
            }
            if (blockID == Blocks.field_150364_r) {
                return true;
            }
            if (blockID == Blocks.field_150362_t) {
                return true;
            }
            if (blockID == Blocks.field_150344_f) {
                return true;
            }
            if (blockID == Blocks.field_150345_g) {
                return true;
            }
            if (blockID == Blocks.field_150329_H) {
                return true;
            }
            if (blockID == Blocks.field_150434_aF) {
                return true;
            }
            if (blockID == OreSpawnMain.CrystalPlanksBlock) {
                return true;
            }
            if (blockID == OreSpawnMain.MyAppleLeaves) {
                return true;
            }
            if (blockID == OreSpawnMain.MySkyTreeLog) {
                return true;
            }
            if (blockID == OreSpawnMain.MyDT) {
                return true;
            }
            if (blockID == OreSpawnMain.MyExperienceLeaves) {
                return true;
            }
            if (blockID == OreSpawnMain.MyScaryLeaves) {
                return true;
            }
            if (blockID == OreSpawnMain.MyCherryLeaves) {
                return true;
            }
            if (blockID == OreSpawnMain.MyPeachLeaves) {
                return true;
            }
            if (blockID == OreSpawnMain.MyCrystalLeaves) {
                return true;
            }
            if (blockID == OreSpawnMain.MyCrystalLeaves2) {
                return true;
            }
            if (blockID == OreSpawnMain.MyCrystalLeaves3) {
                return true;
            }
            return blockID == OreSpawnMain.MyCrystalTreeLog;
        }
        return blockID == Blocks.field_150321_G;
    }

    private boolean isLeaves(Block blockID) {
        if (blockID == Blocks.field_150321_G) {
            return true;
        }
        if (blockID == Blocks.field_150362_t) {
            return true;
        }
        if (blockID == Blocks.field_150345_g) {
            return true;
        }
        if (blockID == Blocks.field_150329_H) {
            return true;
        }
        if (blockID == OreSpawnMain.MyAppleLeaves) {
            return true;
        }
        if (blockID == OreSpawnMain.MyExperienceLeaves) {
            return true;
        }
        if (blockID == OreSpawnMain.MyScaryLeaves) {
            return true;
        }
        if (blockID == OreSpawnMain.MyCherryLeaves) {
            return true;
        }
        if (blockID == OreSpawnMain.MyPeachLeaves) {
            return true;
        }
        if (blockID == OreSpawnMain.MyCrystalLeaves) {
            return true;
        }
        if (blockID == OreSpawnMain.MyCrystalLeaves2) {
            return true;
        }
        return blockID == OreSpawnMain.MyCrystalLeaves3;
    }

    public boolean func_150894_a(ItemStack par1ItemStack, World par2World, Block par3, int par4, int par5, int par6, EntityLivingBase par7EntityLivingBase) {
        if (this == OreSpawnMain.MyChainsaw && !par2World.field_72995_K) {
            for (int i = -5; i <= 5; ++i) {
                for (int j = -5; j <= 10; ++j) {
                    for (int k = -5; k <= 5; ++k) {
                        Block bid = par2World.func_147439_a(par4 + i, par5 + j, par6 + k);
                        if (this.leaf) {
                            if (!this.isLeaves(bid)) continue;
                            this.dropItemRand(par2World, Item.func_150898_a((Block)bid), 1, par4 + i, par5 + j, par6 + k);
                            par2World.func_147449_b(par4 + i, par5 + j, par6 + k, Blocks.field_150350_a);
                            continue;
                        }
                        if (!this.canCrush(bid)) continue;
                        this.dropItemRand(par2World, Item.func_150898_a((Block)bid), 1, par4 + i, par5 + j, par6 + k);
                        par2World.func_147449_b(par4 + i, par5 + j, par6 + k, Blocks.field_150350_a);
                    }
                }
            }
        }
        return super.func_150894_a(par1ItemStack, par2World, par3, par4, par5, par6, par7EntityLivingBase);
    }

    private ItemStack dropItemRand(World world, Item index, int par1, int x, int y, int z) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(world, (double)(x + OreSpawnMain.OreSpawnRand.nextInt(5) - OreSpawnMain.OreSpawnRand.nextInt(5)), (double)y + 1.0 + (double)world.field_73012_v.nextInt(5), (double)(z + OreSpawnMain.OreSpawnRand.nextInt(5) - OreSpawnMain.OreSpawnRand.nextInt(5)), is);
        if (var3 != null) {
            world.func_72838_d((Entity)var3);
        }
        return is;
    }

    public float getStrVsBlock(ItemStack par1ItemStack, Block par2Block) {
        if (par2Block != null && this == OreSpawnMain.MyChainsaw) {
            this.leaf = this.isLeaves(par2Block);
            if (par2Block.func_149688_o() == Material.field_151575_d || par2Block.func_149688_o() == Material.field_151585_k || par2Block.func_149688_o() == Material.field_151582_l) {
                return OreSpawnMain.chainsaw_stats.efficiency;
            }
            if (this.canCrush(par2Block)) {
                return OreSpawnMain.chainsaw_stats.efficiency;
            }
        }
        return 2.0f;
    }
}

