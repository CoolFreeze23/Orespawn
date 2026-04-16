/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.SpiderDriver;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;

public class RenderSpiderDriver
extends RenderSpider {
    private static final ResourceLocation texture = new ResourceLocation("orespawn", "spiderdriver.png");

    public RenderSpiderDriver(ModelSpider modelSpider, float par2) {
    }

    public void renderSpiderDriver(SpiderDriver par1EntitySpiderDriver, double par2, double par4, double par6, float par8, float par9) {
        super.func_76986_a((EntityLiving)par1EntitySpiderDriver, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
        this.renderSpiderDriver((SpiderDriver)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderSpiderDriver((SpiderDriver)par1Entity, par2, par4, par6, par8, par9);
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        return texture;
    }

    protected ResourceLocation getSpiderTextures(EntitySpider par1EntitySpider) {
        return texture;
    }
}

