/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelCow
 *  net.minecraft.client.renderer.entity.RenderLiving
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.entity.passive.EntityCow
 *  net.minecraft.util.ResourceLocation
 */
package danger.orespawn.entity.render;

import danger.orespawn.entity.RedCow;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.ResourceLocation;

public class RenderEnchantedCow
extends RenderLiving<EntityCow> {
    protected ModelCow model;
    private static final ResourceLocation texture1 = new ResourceLocation("orespawn:textures/entity/red_cow.png");
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn", "gold_cow.png");
    private static final ResourceLocation texture3 = new ResourceLocation("orespawn", "crystal_cow.png");

    public RenderEnchantedCow(RenderManager manager) {
        super(manager, (ModelBase)new ModelCow(), 0.0f);
        this.model = (ModelCow)this.field_77045_g;
    }

    @Nullable
    protected ResourceLocation getEntityTexture(EntityCow entity) {
        if (entity instanceof RedCow) {
            return texture1;
        }
        return texture1;
    }
}

