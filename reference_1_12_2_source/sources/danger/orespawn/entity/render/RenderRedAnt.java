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

import danger.orespawn.entity.RedAnt;
import danger.orespawn.entity.model.ModelAnt;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderRedAnt
extends RenderLiving<RedAnt> {
    public static final ResourceLocation TEXTURES = new ResourceLocation("orespawn:textures/entity/red_ant.png");

    public RenderRedAnt(RenderManager manager) {
        super(manager, (ModelBase)new ModelAnt(), 0.0f);
    }

    protected ResourceLocation getEntityTexture(RedAnt entity) {
        return TEXTURES;
    }

    protected void applyRotations(RedAnt entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        super.func_77043_a((EntityLivingBase)entityLiving, p_77043_2_, rotationYaw, partialTicks);
    }
}

