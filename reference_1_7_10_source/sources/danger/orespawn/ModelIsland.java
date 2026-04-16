/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelIsland
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;

    public ModelIsland(float f) {
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.Shape1 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Shape1.func_78789_a(-4.0f, -4.0f, -4.0f, 8, 8, 8);
        this.Shape1.func_78793_a(0.0f, 16.0f, 0.0f);
        this.Shape1.func_78787_b(64, 32);
        this.Shape1.field_78809_i = true;
        this.setRotation(this.Shape1, 0.0f, 0.0f, 0.0f);
        this.Shape2 = new ModelRenderer((ModelBase)this, 32, 0);
        this.Shape2.func_78789_a(-4.0f, -4.0f, -4.0f, 8, 8, 8);
        this.Shape2.func_78793_a(0.0f, 16.0f, 0.0f);
        this.Shape2.func_78787_b(64, 32);
        this.Shape2.field_78809_i = true;
        this.setRotation(this.Shape2, 0.7853982f, 0.7853982f, 0.7853982f);
        this.Shape3 = new ModelRenderer((ModelBase)this, 32, 16);
        this.Shape3.func_78789_a(-4.0f, -4.0f, -4.0f, 8, 8, 8);
        this.Shape3.func_78793_a(0.0f, 16.0f, 0.0f);
        this.Shape3.func_78787_b(64, 32);
        this.Shape3.field_78809_i = true;
        this.setRotation(this.Shape3, 0.7853982f, 0.7853982f, 0.7853982f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        this.Shape1.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.05f * this.wingspeed)) * (float)Math.PI;
        this.Shape1.field_78796_g = newangle = MathHelper.func_76134_b((float)(f2 * 0.051f * this.wingspeed)) * (float)Math.PI;
        this.Shape1.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.052f * this.wingspeed)) * (float)Math.PI;
        this.Shape2.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.053f * this.wingspeed)) * (float)Math.PI;
        this.Shape2.field_78796_g = newangle = MathHelper.func_76134_b((float)(f2 * 0.054f * this.wingspeed)) * (float)Math.PI;
        this.Shape2.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.055f * this.wingspeed)) * (float)Math.PI;
        this.Shape3.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.056f * this.wingspeed)) * (float)Math.PI;
        this.Shape3.field_78796_g = newangle = MathHelper.func_76134_b((float)(f2 * 0.057f * this.wingspeed)) * (float)Math.PI;
        this.Shape3.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.058f * this.wingspeed)) * (float)Math.PI;
        this.Shape1.func_78785_a(f5);
        this.Shape2.func_78785_a(f5);
        this.Shape3.func_78785_a(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }

    public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
        super.func_78087_a(par1, par2, par3, par4, par5, par6, par7Entity);
    }
}

