/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.ResourceLocation
 *  org.lwjgl.opengl.GL11
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.VelocityRaptor;
import danger.orespawn.entity.model.ModelVelocityRaptor;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderVelocityRaptor
extends RenderLiving<VelocityRaptor> {
    public RenderVelocityRaptor(RenderManager rendermanagerIn) {
        super(rendermanagerIn, (ModelBase)new ModelVelocityRaptor(1.0f), 0.0f);
    }

    protected void preRenderCallback(VelocityRaptor entitylivingbaseIn, float partialTickTime) {
        GL11.glScalef((float)0.8f, (float)0.8f, (float)0.8f);
        super.func_77041_b((EntityLivingBase)entitylivingbaseIn, partialTickTime);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(VelocityRaptor entity) {
        return new ResourceLocation("orespawn:textures/entity/velocityraptor.png");
    }
}

