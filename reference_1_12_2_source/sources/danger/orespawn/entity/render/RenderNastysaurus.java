/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.ResourceLocation
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.model.ModelNastysaurus;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderNastysaurus
extends RenderLiving {
    public RenderNastysaurus(RenderManager renderManager) {
        super(renderManager, (ModelBase)new ModelNastysaurus(1.5f), 0.0f);
    }

    @Nullable
    protected ResourceLocation func_110775_a(Entity entity) {
        return new ResourceLocation("orespawn:textures/entity/nastysaurus.png");
    }
}

