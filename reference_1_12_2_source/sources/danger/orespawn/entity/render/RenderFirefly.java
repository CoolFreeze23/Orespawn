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

import danger.orespawn.entity.model.ModelFirefly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderFirefly
extends RenderLiving {
    private static final ResourceLocation texture = new ResourceLocation("orespawn:textures/entity/firefly.png");

    public RenderFirefly(RenderManager renderManager) {
        super(renderManager, (ModelBase)new ModelFirefly(1.5f), 0.0f);
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        return texture;
    }
}

