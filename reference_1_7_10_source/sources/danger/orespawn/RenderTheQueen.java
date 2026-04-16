/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.ModelTheQueen;
import danger.orespawn.TheQueen;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderTheQueen
extends RenderLiving {
    protected ModelTheQueen model = (ModelTheQueen)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn", "TheQueentexture.png");
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn", "TheQueentexture2.png");

    public RenderTheQueen(ModelTheQueen par1ModelBase, float par2, float par3) {
        super((ModelBase)par1ModelBase, par2 * par3);
        this.scale = par3;
    }

    public void renderTheQueen(TheQueen par1EntityTheQueen, double par2, double par4, double par6, float par8, float par9) {
        super.func_76986_a((EntityLiving)par1EntityTheQueen, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
        this.renderTheQueen((TheQueen)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderTheQueen((TheQueen)par1Entity, par2, par4, par6, par8, par9);
    }

    protected void preRenderScale(TheQueen par1Entity, float par2) {
        if (par1Entity != null && par1Entity.getPlayNicely() != 0) {
            GL11.glScalef((float)(this.scale / 4.0f), (float)(this.scale / 4.0f), (float)(this.scale / 4.0f));
            return;
        }
        GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale);
    }

    protected void func_77041_b(EntityLivingBase par1EntityLiving, float par2) {
        this.preRenderScale((TheQueen)par1EntityLiving, par2);
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        TheQueen q = (TheQueen)entity;
        if (q.isHappy()) {
            return texture2;
        }
        return texture;
    }
}

