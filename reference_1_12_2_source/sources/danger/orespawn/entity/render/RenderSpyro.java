/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.ResourceLocation
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.model.ModelSpyro;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderSpyro
extends RenderLiving {
    protected ModelSpyro model;
    private float scale = 1.0f;
    private static final ResourceLocation texture = new ResourceLocation("orespawn:textures/entity/spyrotexture.png");

    public RenderSpyro(RenderManager renderManager) {
        super(renderManager, (ModelBase)new ModelSpyro(1.0f), 0.0f);
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        return texture;
    }
}

