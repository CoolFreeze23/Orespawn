/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Baryonyx;
import danger.orespawn.ModelBaryonyx;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderBaryonyx
extends RenderLiving {
    protected ModelBaryonyx model = (ModelBaryonyx)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn", "Baryonyx.png");

    public RenderBaryonyx(ModelBaryonyx par1ModelBase, float par2, float par3) {
        super((ModelBase)par1ModelBase, par2 * par3);
        this.scale = par3;
    }

    public void renderBaryonyx(Baryonyx par1EntityBaryonyx, double par2, double par4, double par6, float par8, float par9) {
        super.func_76986_a((EntityLiving)par1EntityBaryonyx, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
        this.renderBaryonyx((Baryonyx)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderBaryonyx((Baryonyx)par1Entity, par2, par4, par6, par8, par9);
    }

    protected void preRenderScale(Baryonyx par1Entity, float par2) {
        if (par1Entity != null && par1Entity.func_70631_g_()) {
            GL11.glScalef((float)(this.scale / 2.0f), (float)(this.scale / 2.0f), (float)(this.scale / 2.0f));
            return;
        }
        GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale);
    }

    protected void func_77041_b(EntityLivingBase par1EntityLiving, float par2) {
        this.preRenderScale((Baryonyx)par1EntityLiving, par2);
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        return texture;
    }
}

