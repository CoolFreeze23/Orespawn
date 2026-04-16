/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.EntityCannonFodder;
import danger.orespawn.ModelVelocityRaptor;
import danger.orespawn.VelocityRaptor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderVelocityRaptor
extends RenderLiving {
    protected ModelVelocityRaptor model = (ModelVelocityRaptor)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn", "velocityraptor.png");
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn", "velocityraptor2.png");
    private static final ResourceLocation texture3 = new ResourceLocation("orespawn", "velocityraptor3.png");

    public RenderVelocityRaptor(ModelVelocityRaptor par1ModelBase, float par2, float par3) {
        super((ModelBase)par1ModelBase, par2 * par3);
        this.scale = par3;
    }

    public void renderVelocityRaptor(VelocityRaptor par1EntityVelocityRaptor, double par2, double par4, double par6, float par8, float par9) {
        super.func_76986_a((EntityLiving)par1EntityVelocityRaptor, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
        this.renderVelocityRaptor((VelocityRaptor)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderVelocityRaptor((VelocityRaptor)par1Entity, par2, par4, par6, par8, par9);
    }

    protected void preRenderScale(VelocityRaptor par1Entity, float par2) {
        if (par1Entity != null && par1Entity.func_70631_g_()) {
            GL11.glScalef((float)(this.scale / 2.0f), (float)(this.scale / 2.0f), (float)(this.scale / 2.0f));
            return;
        }
        GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale);
    }

    protected void func_77041_b(EntityLivingBase par1EntityLiving, float par2) {
        this.preRenderScale((VelocityRaptor)par1EntityLiving, par2);
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        EntityCannonFodder c;
        if (entity instanceof EntityCannonFodder && (c = (EntityCannonFodder)entity).get_is_activated() != 0) {
            if (c.getHatColor() == 2) {
                return texture2;
            }
            if (c.getHatColor() == 3) {
                return texture3;
            }
        }
        return texture;
    }
}

