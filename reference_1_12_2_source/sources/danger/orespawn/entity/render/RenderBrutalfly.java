/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.util.ResourceLocation
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.Brutalfly;
import danger.orespawn.entity.model.ModelBrutalfly;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBrutalfly
extends RenderLiving<Brutalfly> {
    public RenderBrutalfly(RenderManager rendermanagerIn) {
        super(rendermanagerIn, (ModelBase)new ModelBrutalfly(0.1f), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(Brutalfly entity) {
        return new ResourceLocation("orespawn:textures/entity/brutalfly.png");
    }
}

