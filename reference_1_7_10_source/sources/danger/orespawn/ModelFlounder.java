/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelFlounder
extends ModelBase {
    ModelRenderer body;
    ModelRenderer head;
    ModelRenderer tail1;
    ModelRenderer tail2;
    ModelRenderer rfin;
    ModelRenderer lfin;

    public ModelFlounder() {
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.body = new ModelRenderer((ModelBase)this, 0, 16);
        this.body.func_78789_a(-4.0f, 0.0f, -5.0f, 8, 1, 12);
        this.body.func_78793_a(0.0f, 22.0f, 0.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 0, 5);
        this.head.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 1, 2);
        this.head.func_78793_a(0.0f, 22.0f, -7.0f);
        this.head.func_78787_b(64, 32);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 30, 0);
        this.tail1.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 1, 2);
        this.tail1.func_78793_a(0.0f, 22.0f, 7.0f);
        this.tail1.func_78787_b(64, 32);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.0f, 0.0f, 0.0f);
        this.tail2 = new ModelRenderer((ModelBase)this, 30, 4);
        this.tail2.func_78789_a(-3.0f, 0.0f, 2.0f, 6, 1, 3);
        this.tail2.func_78793_a(0.0f, 22.0f, 7.0f);
        this.tail2.func_78787_b(64, 32);
        this.tail2.field_78809_i = true;
        this.setRotation(this.tail2, 0.0f, 0.0f, 0.0f);
        this.rfin = new ModelRenderer((ModelBase)this, 12, 0);
        this.rfin.func_78789_a(-3.0f, 0.0f, 0.0f, 3, 1, 2);
        this.rfin.func_78793_a(-4.0f, 22.0f, -2.0f);
        this.rfin.func_78787_b(64, 32);
        this.rfin.field_78809_i = true;
        this.setRotation(this.rfin, 0.0f, 0.0f, 0.0f);
        this.lfin = new ModelRenderer((ModelBase)this, 0, 0);
        this.lfin.func_78789_a(0.0f, 0.0f, 0.0f, 3, 1, 2);
        this.lfin.func_78793_a(4.0f, 22.0f, -2.0f);
        this.lfin.func_78787_b(64, 32);
        this.lfin.field_78809_i = true;
        this.setRotation(this.lfin, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle2;
        float newangle;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        if ((double)f1 > 0.1) {
            newangle = MathHelper.func_76134_b((float)(f2 * 1.3f)) * (float)Math.PI * 0.25f * f1;
            newangle2 = MathHelper.func_76134_b((float)(f2 * 1.7f)) * (float)Math.PI * 0.25f * f1;
        } else {
            newangle = 0.0f;
            newangle2 = 0.0f;
        }
        this.lfin.field_78808_h = newangle;
        this.rfin.field_78808_h = newangle2;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 1.2f)) * (float)Math.PI * 0.25f * f1 : MathHelper.func_76134_b((float)(f2 * 0.7f)) * (float)Math.PI * 0.05f;
        this.tail1.field_78795_f = this.tail2.field_78795_f = newangle;
        this.body.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.tail2.func_78785_a(f5);
        this.rfin.func_78785_a(f5);
        this.lfin.func_78785_a(f5);
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

