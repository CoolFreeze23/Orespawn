/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package danger.orespawn;

import danger.orespawn.RenderSpinner;
import danger.orespawn.Shoes;
import net.minecraft.entity.Entity;

public class RenderShoe
extends RenderSpinner {
    @Override
    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        if (par1Entity instanceof Shoes) {
            Shoes var2 = (Shoes)par1Entity;
            this.spinItemIconIndex = var2.getShoeId();
        }
        super.func_76986_a(par1Entity, par2, par4, par6, par8, par9);
    }
}

