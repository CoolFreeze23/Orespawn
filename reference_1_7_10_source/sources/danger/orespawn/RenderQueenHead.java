/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.ModelTheQueen;
import danger.orespawn.QueenHead;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderQueenHead
extends RenderLiving {
    public RenderQueenHead(ModelTheQueen par1ModelBase, float par2, float par3) {
        super((ModelBase)par1ModelBase, par2 * par3);
    }

    public void renderQueenHead(QueenHead par1EntityQueenHead, double par2, double par4, double par6, float par8, float par9) {
    }

    public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
    }

    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
    }

    protected void preRenderScale(QueenHead par1Entity, float par2) {
    }

    protected void func_77041_b(EntityLivingBase par1EntityLiving, float par2) {
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        return null;
    }
}

