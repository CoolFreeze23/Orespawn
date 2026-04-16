/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.util.ResourceLocation
 *  org.lwjgl.opengl.GL11
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.GammaMetroid;
import danger.orespawn.entity.model.ModelGammaMetroid;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderGammaMetroid
extends RenderLiving<GammaMetroid> {
    protected ModelGammaMetroid model;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn:textures/entity/gammametroid.png");

    public RenderGammaMetroid(RenderManager renderManager) {
        super(renderManager, (ModelBase)new ModelGammaMetroid(1.0f), 0.0f);
    }

    protected void preRenderScale(GammaMetroid par1Entity, float par2) {
        if (par1Entity != null && par1Entity.func_70631_g_()) {
            GL11.glScalef((float)(this.scale / 2.0f), (float)(this.scale / 2.0f), (float)(this.scale / 2.0f));
            return;
        }
        GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(GammaMetroid entity) {
        return texture;
    }
}

