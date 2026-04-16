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

import danger.orespawn.entity.Moth;
import danger.orespawn.entity.model.ModelButterfly;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMoth
extends RenderLiving<Moth> {
    ResourceLocation TEXTURE1 = new ResourceLocation("orespawn:textures/entity/lunamoth.png");
    ResourceLocation TEXTURE2 = new ResourceLocation("orespawn:textures/entity/eyemoth.png");
    ResourceLocation TEXTURE3 = new ResourceLocation("orespawn:textures/entity/firemoth.png");
    ResourceLocation TEXTURE4 = new ResourceLocation("orespawn:textures/entity/darkmoth.png");
    protected ModelButterfly model = (ModelButterfly)this.field_77045_g;
    private float scale = 1.0f;

    public RenderMoth(RenderManager manager) {
        super(manager, (ModelBase)new ModelButterfly(0.6f, 1.5f), 0.0f);
    }

    @Nullable
    protected ResourceLocation getEntityTexture(Moth entity) {
        switch (entity.moth_type) {
            case 0: {
                return this.TEXTURE1;
            }
            case 1: {
                return this.TEXTURE2;
            }
            case 2: {
                return this.TEXTURE3;
            }
        }
        return this.TEXTURE4;
    }
}

