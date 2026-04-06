/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.MathHelper
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelMosquito
extends ModelBase {
    ModelRenderer body;
    ModelRenderer leftwing1;
    ModelRenderer rightwing1;
    ModelRenderer leftwing2;
    ModelRenderer rightwing2;

    public ModelMosquito() {
        this.field_78090_t = 32;
        this.field_78089_u = 32;
        this.body = new ModelRenderer((ModelBase)this, 8, 18);
        this.body.func_78789_a(0.0f, 0.0f, -2.0f, 1, 1, 8);
        this.body.func_78793_a(0.0f, 17.0f, 0.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.leftwing1 = new ModelRenderer((ModelBase)this, 16, 13);
        this.leftwing1.func_78789_a(1.0f, 0.0f, -1.0f, 3, 1, 3);
        this.leftwing1.func_78793_a(1.0f, 17.0f, 0.0f);
        this.leftwing1.func_78787_b(64, 32);
        this.leftwing1.field_78809_i = true;
        this.setRotation(this.leftwing1, 0.0f, 0.0f, 0.0f);
        this.rightwing1 = new ModelRenderer((ModelBase)this, 2, 13);
        this.rightwing1.func_78789_a(-4.0f, 0.0f, -1.0f, 3, 1, 3);
        this.rightwing1.func_78793_a(0.0f, 17.0f, 0.0f);
        this.rightwing1.func_78787_b(64, 32);
        this.rightwing1.field_78809_i = true;
        this.setRotation(this.rightwing1, 0.0f, 0.0f, 0.0f);
        this.leftwing2 = new ModelRenderer((ModelBase)this, 15, 8);
        this.leftwing2.func_78789_a(0.0f, 0.0f, 0.0f, 5, 1, 1);
        this.leftwing2.func_78793_a(1.0f, 17.0f, 0.0f);
        this.leftwing2.func_78787_b(64, 32);
        this.leftwing2.field_78809_i = true;
        this.setRotation(this.leftwing2, 0.0f, 0.0f, 0.0f);
        this.rightwing2 = new ModelRenderer((ModelBase)this, 2, 8);
        this.rightwing2.func_78789_a(-5.0f, 0.0f, 0.0f, 5, 1, 1);
        this.rightwing2.func_78793_a(0.0f, 17.0f, 0.0f);
        this.rightwing2.func_78787_b(64, 32);
        this.rightwing2.field_78809_i = true;
        this.setRotation(this.rightwing2, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        this.body.func_78785_a(f5);
        this.rightwing2.field_78808_h = this.rightwing1.field_78808_h = MathHelper.func_76134_b((float)(f2 * 3.0f)) * (float)Math.PI * 0.25f;
        this.leftwing1.field_78808_h = -this.rightwing1.field_78808_h;
        this.leftwing2.field_78808_h = -this.rightwing1.field_78808_h;
        this.leftwing1.func_78785_a(f5);
        this.rightwing1.func_78785_a(f5);
        this.leftwing2.func_78785_a(f5);
        this.rightwing2.func_78785_a(f5);
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

