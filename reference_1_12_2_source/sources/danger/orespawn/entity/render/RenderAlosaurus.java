/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.util.ResourceLocation
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.model.ModelAlosaurus;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderAlosaurus
extends RenderLiving<Alosaurus> {
    public static final ResourceLocation TEXTURES = new ResourceLocation("orespawn:textures/entity/alosaurus.png");

    public RenderAlosaurus(RenderManager manager) {
        super(manager, (ModelBase)new ModelAlosaurus(1.5f), 0.0f);
    }

    protected ResourceLocation getEntityTexture(Alosaurus entity) {
        return TEXTURES;
    }

    protected void applyRotations(Alosaurus entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        super.func_77043_a((EntityLivingBase)entityLiving, p_77043_2_, rotationYaw, partialTicks);
    }
}

