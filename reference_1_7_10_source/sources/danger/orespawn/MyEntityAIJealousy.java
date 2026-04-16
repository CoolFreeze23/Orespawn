/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Boyfriend;
import danger.orespawn.Girlfriend;
import danger.orespawn.MyEntityAINearestAttackableTarget;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class MyEntityAIJealousy
extends MyEntityAINearestAttackableTarget {
    private EntityTameable theTameable;

    public MyEntityAIJealousy(EntityTameable par1EntityTameable, Class par2Class, float par3, int par4, boolean par5) {
        super((EntityLiving)par1EntityTameable, par2Class, par3, par4, par5);
        this.theTameable = par1EntityTameable;
    }

    public boolean func_75250_a() {
        EntityTameable te = (EntityTameable)this.taskOwner;
        Girlfriend gf = null;
        Boyfriend bf = null;
        EntityLivingBase ep = null;
        if (te == null) {
            return false;
        }
        if (!te.func_70909_n()) {
            return false;
        }
        if (te.func_70906_o()) {
            return false;
        }
        if (!super.func_75250_a()) {
            return false;
        }
        EntityLiving victim = this.targetEntity;
        if (victim == null) {
            return false;
        }
        if (te instanceof Girlfriend ? victim instanceof Girlfriend && (gf = (Girlfriend)victim).func_70909_n() : victim instanceof Boyfriend && (bf = (Boyfriend)victim).func_70909_n()) {
            return false;
        }
        ep = te.func_70902_q();
        return ep != null;
    }
}

