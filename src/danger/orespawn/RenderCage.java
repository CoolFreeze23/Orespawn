/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package danger.orespawn;

import danger.orespawn.EntityCage;
import danger.orespawn.RenderSpinner;
import net.minecraft.entity.Entity;

public class RenderCage
extends RenderSpinner {
    @Override
    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.spinItemIconIndex = 160;
        if (par1Entity instanceof EntityCage) {
            EntityCage var2 = (EntityCage)par1Entity;
            this.spinItemIconIndex = var2.getCageIndex();
        }
        super.func_76986_a(par1Entity, par2, par4, par6, par8, par9);
    }
}

