/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.math.MathHelper
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.Butterfly;
import danger.orespawn.entity.model.ModelButterfly;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderButterfly
extends RenderLiving<Butterfly> {
    protected ModelButterfly model = (ModelButterfly)this.field_77045_g;
    private float scale = 1.0f;
    private static final ResourceLocation TEXTURE1 = new ResourceLocation("orespawn:textures/entity/butterfly.png");
    private static final ResourceLocation TEXTURE2 = new ResourceLocation("orespawn:textures/entity/butterfly2.png");
    private static final ResourceLocation TEXTURE3 = new ResourceLocation("orespawn:textures/entity/butterfly3.png");
    private static final ResourceLocation TEXTURE4 = new ResourceLocation("orespawn:textures/entity/butterfly4.png");

    public RenderButterfly(RenderManager manager) {
        super(manager, (ModelBase)new ModelButterfly(0.6f, 1.0f), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(Butterfly entity) {
        switch (entity.butterflyType) {
            case 1: {
                return TEXTURE1;
            }
            case 2: {
                return TEXTURE2;
            }
            case 3: {
                return TEXTURE3;
            }
        }
        return TEXTURE4;
    }

    protected void applyRotations(Butterfly entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        GlStateManager.func_179109_b((float)0.0f, (float)(MathHelper.func_76134_b((float)(p_77043_2_ * 0.3f)) * 0.1f), (float)0.0f);
        super.func_77043_a((EntityLivingBase)entityLiving, p_77043_2_, rotationYaw, partialTicks);
    }
}

