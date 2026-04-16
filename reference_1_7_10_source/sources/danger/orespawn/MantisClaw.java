/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class MantisClaw
extends ItemSword {
    private int weaponDamage;
    private final Item.ToolMaterial toolMaterial;

    public MantisClaw(int par1, Item.ToolMaterial par2EnumToolMaterial) {
        super(par2EnumToolMaterial);
        this.toolMaterial = par2EnumToolMaterial;
        this.weaponDamage = 10;
        this.field_77777_bU = 1;
        this.func_77656_e(1000);
        this.func_77637_a(CreativeTabs.field_78037_j);
    }

    public String getMaterialName() {
        return "AMETHYST";
    }

    public boolean func_77644_a(ItemStack par1ItemStack, EntityLivingBase par2EntityLiving, EntityLivingBase par3EntityLiving) {
        int var2 = 5;
        if (par2EntityLiving != null && par3EntityLiving != null && !par2EntityLiving.field_70170_p.field_72995_K) {
            par2EntityLiving.func_70691_i(-1.0f);
            par3EntityLiving.func_70691_i(1.0f);
        }
        par1ItemStack.func_77972_a(1, par3EntityLiving);
        return true;
    }

    public int func_77626_a(ItemStack par1ItemStack) {
        return 3000;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

