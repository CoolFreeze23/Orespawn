/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.CrystalCow;
import danger.orespawn.EnchantedCow;
import danger.orespawn.GoldCow;
import danger.orespawn.RedCow;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderEnchantedCow
extends RenderLiving {
    protected ModelCow model;
    private static final ResourceLocation texture3 = new ResourceLocation("orespawn", "crystal_cow.png");
    private static final ResourceLocation texture1 = new ResourceLocation("orespawn", "red_cow.png");
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn", "gold_cow.png");

    public RenderEnchantedCow(ModelCow par1ModelBase, float par2) {
        super((ModelBase)par1ModelBase, par2);
        this.model = (ModelCow)this.field_77045_g;
    }

    public void renderEnchantedCow(RedCow par1EntityEnchantedCow, double par2, double par4, double par6, float par8, float par9) {
        super.func_76986_a((EntityLiving)par1EntityEnchantedCow, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
        this.renderEnchantedCow((RedCow)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    public void func_76986_a(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
        this.renderEnchantedCow((RedCow)par1Entity, par2, par4, par6, par8, par9);
    }

    public int func_77032_a(EntityLivingBase par1EntityLiving, int par2, float par3) {
        if (par1EntityLiving instanceof EnchantedCow && par2 == 3) {
            this.func_77042_a((ModelBase)this.model);
            GL11.glColor3f((float)1.0f, (float)1.0f, (float)1.0f);
            return 31;
        }
        return -1;
    }

    protected ResourceLocation func_110775_a(Entity entity) {
        if (entity instanceof EnchantedCow) {
            return texture2;
        }
        if (entity instanceof GoldCow) {
            return texture2;
        }
        if (entity instanceof CrystalCow) {
            return texture3;
        }
        return texture1;
    }
}

