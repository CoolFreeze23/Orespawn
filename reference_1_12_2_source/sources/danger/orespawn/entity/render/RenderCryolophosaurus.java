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

import danger.orespawn.entity.Cryolophosaurus;
import danger.orespawn.entity.model.ModelCryolophosaurus;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class RenderCryolophosaurus
extends RenderLiving<Cryolophosaurus> {
    public static final ResourceLocation TEXTURES = new ResourceLocation("orespawn:textures/entity/cryolophosaurus.png");

    public RenderCryolophosaurus(RenderManager manager) {
        super(manager, (ModelBase)new ModelCryolophosaurus(1.5f), 0.3f);
    }

    protected ResourceLocation getEntityTexture(Cryolophosaurus entity) {
        return TEXTURES;
    }

    protected void applyRotations(Cryolophosaurus entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        super.func_77043_a((EntityLivingBase)entityLiving, p_77043_2_, rotationYaw, partialTicks);
    }
}

