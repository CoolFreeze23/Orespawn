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

import danger.orespawn.entity.Alien;
import danger.orespawn.entity.model.ModelAlien;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAlien
extends RenderLiving<Alien> {
    protected ModelAlien model = (ModelAlien)this.field_77045_g;
    private float scale = 1.0f;

    public RenderAlien(RenderManager manager) {
        super(manager, (ModelBase)new ModelAlien(0.1f), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(Alien entity) {
        return new ResourceLocation("orespawn:textures/entity/alien.png");
    }
}

