/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.ResourceLocation
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.WormDoom;
import danger.orespawn.entity.model.ModelWormDoom;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderWormDoom
extends RenderLiving<WormDoom> {
    protected ModelWormDoom model = (ModelWormDoom)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn:textures/entity/wormdoomtexture.png");

    public RenderWormDoom(RenderManager manager) {
        super(manager, (ModelBase)new ModelWormDoom(), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(WormDoom entity) {
        return texture;
    }

    protected void preRenderCallback(WormDoom entitylivingbaseIn, float partialTickTime) {
        super.func_77041_b((EntityLivingBase)entitylivingbaseIn, partialTickTime);
    }

    public void doRender(WormDoom entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.func_76986_a((EntityLiving)entity, x, y, z, entityYaw, partialTicks);
    }
}

