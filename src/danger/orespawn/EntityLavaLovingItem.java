/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityLavaLovingItem
extends EntityItem {
    public EntityLavaLovingItem(World par1World, double par2, double par4, double par6, ItemStack par8ItemStack) {
        super(par1World, par2, par4, par6, par8ItemStack);
        this.field_70174_ab = 300;
        this.field_70178_ae = true;
        this.field_70172_ad = 300;
    }

    public void noFire() {
        this.field_70174_ab = 300;
        this.field_70178_ae = true;
        this.field_70172_ad = 300;
    }

    public void yesFire() {
        this.field_70174_ab = 0;
        this.field_70178_ae = false;
        this.field_70172_ad = 0;
    }

    protected void dealFireDamage(float par1) {
        if (!this.field_70178_ae) {
            this.func_70097_a(DamageSource.field_76372_a, par1);
        }
    }
}

