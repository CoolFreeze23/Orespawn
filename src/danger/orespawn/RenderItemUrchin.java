/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.EntityThrowable
 */
package danger.orespawn;

import danger.orespawn.Acid;
import danger.orespawn.BerthaHit;
import danger.orespawn.DeadIrukandji;
import danger.orespawn.IceBall;
import danger.orespawn.InkSack;
import danger.orespawn.LaserBall;
import danger.orespawn.RenderSpinner;
import danger.orespawn.SunspotUrchin;
import danger.orespawn.WaterBall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;

public class RenderItemUrchin
extends RenderSpinner {
    @Override
    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        EntityThrowable var2;
        if (par1Entity instanceof BerthaHit) {
            return;
        }
        if (par1Entity instanceof SunspotUrchin) {
            var2 = (SunspotUrchin)par1Entity;
            this.spinItemIconIndex = var2.getUrchinIndex();
        }
        if (par1Entity instanceof WaterBall) {
            var2 = (WaterBall)par1Entity;
            this.spinItemIconIndex = var2.getWaterBallIndex();
        }
        if (par1Entity instanceof InkSack) {
            var2 = (InkSack)par1Entity;
            this.spinItemIconIndex = var2.getInkSackIndex();
        }
        if (par1Entity instanceof LaserBall) {
            var2 = (LaserBall)par1Entity;
            this.spinItemIconIndex = var2.getLaserBallIndex();
        }
        if (par1Entity instanceof IceBall) {
            var2 = (IceBall)par1Entity;
            this.spinItemIconIndex = var2.getIceBallIndex();
        }
        if (par1Entity instanceof Acid) {
            var2 = (Acid)par1Entity;
            this.spinItemIconIndex = var2.getAcidIndex();
        }
        if (par1Entity instanceof DeadIrukandji) {
            var2 = (DeadIrukandji)par1Entity;
            this.spinItemIconIndex = var2.getIrukandjiIndex();
        }
        super.func_76986_a(par1Entity, par2, par4, par6, par8, par9);
    }
}

