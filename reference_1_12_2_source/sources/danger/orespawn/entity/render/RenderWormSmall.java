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

import danger.orespawn.entity.WormSmall;
import danger.orespawn.entity.model.ModelWormSmall;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderWormSmall
extends RenderLiving<WormSmall> {
    protected ModelWormSmall model = (ModelWormSmall)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn:textures/entity/wormsmalltexture.png");

    public RenderWormSmall(RenderManager manager) {
        super(manager, (ModelBase)new ModelWormSmall(), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(WormSmall entity) {
        return texture;
    }

    protected void preRenderScale(WormSmall par1Entity, float par2) {
        GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale);
    }

    protected void preRenderCallback(WormSmall entitylivingbaseIn, float partialTickTime) {
        this.preRenderScale(entitylivingbaseIn, partialTickTime);
    }
}

