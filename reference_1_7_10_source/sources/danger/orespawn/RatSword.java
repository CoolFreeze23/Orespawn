/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.Rat;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class RatSword
extends ItemSword {
    private int weaponDamage;
    private final Item.ToolMaterial toolMaterial;

    public RatSword(int par1, Item.ToolMaterial par2EnumToolMaterial) {
        super(par2EnumToolMaterial);
        this.toolMaterial = par2EnumToolMaterial;
        this.weaponDamage = 15;
        this.field_77777_bU = 1;
        this.func_77656_e(1300);
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public String getMaterialName() {
        return "Rat";
    }

    public boolean func_77644_a(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving) {
        int var2 = 5;
        if (par2EntityLiving != null && !par2EntityLiving.field_70170_p.field_72995_K) {
            int num = 1 + par2EntityLiving.field_70170_p.field_73012_v.nextInt(6);
            for (int i = 0; i < num; ++i) {
                Rat r = null;
                r = (Rat)RatSword.spawnCreature((World)par2EntityLiving.field_70170_p, (int)0, (String)"Rat", (double)(par2EntityLiving.field_70165_t + (double)(par2EntityLiving.field_70170_p.field_73012_v.nextFloat() - par2EntityLiving.field_70170_p.field_73012_v.nextFloat()) * 0.5), (double)(par2EntityLiving.field_70163_u + (double)par2EntityLiving.field_70170_p.field_73012_v.nextFloat() + 0.01), (double)(par2EntityLiving.field_70161_v + (double)(par2EntityLiving.field_70170_p.field_73012_v.nextFloat() - par2EntityLiving.field_70170_p.field_73012_v.nextFloat()) * 0.5));
                if (r == null) continue;
                r.setOwner(par3EntityLiving);
            }
        }
        par1ItemStack.func_77972_a(1, par3EntityLiving);
        return true;
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

    public int func_77626_a(ItemStack par1ItemStack) {
        return 3000;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

