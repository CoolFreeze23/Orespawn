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

import danger.orespawn.entity.WormLarge;
import danger.orespawn.entity.model.ModelWormLarge;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderWormLarge
extends RenderLiving<WormLarge> {
    protected ModelWormLarge model = (ModelWormLarge)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn:textures/entity/wormlargetexture.png");

    public RenderWormLarge(RenderManager manager) {
        super(manager, (ModelBase)new ModelWormLarge(), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(WormLarge entity) {
        return texture;
    }

    protected void preRenderScale(WormLarge par1Entity, float par2) {
        GL11.glScalef((float)this.scale, (float)this.scale, (float)this.scale);
    }

    protected void preRenderCallback(WormLarge entitylivingbaseIn, float partialTickTime) {
        this.preRenderScale(entitylivingbaseIn, partialTickTime);
    }
}

