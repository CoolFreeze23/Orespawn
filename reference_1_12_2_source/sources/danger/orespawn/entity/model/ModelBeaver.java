/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.MathHelper
 */
package danger.orespawn.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBeaver
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer head;
    ModelRenderer nose;
    ModelRenderer teeth;
    ModelRenderer body;
    ModelRenderer tail;
    ModelRenderer rff;
    ModelRenderer lff;
    ModelRenderer rrf;
    ModelRenderer lrf;

    public ModelBeaver(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.head = new ModelRenderer((ModelBase)this, 0, 3);
        this.head.func_78789_a(0.0f, 0.0f, 0.0f, 6, 5, 5);
        this.head.func_78793_a(0.0f, 15.0f, -8.0f);
        this.head.func_78787_b(64, 32);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.nose = new ModelRenderer((ModelBase)this, 6, 0);
        this.nose.func_78789_a(0.0f, 0.0f, 0.0f, 2, 1, 1);
        this.nose.func_78793_a(2.0f, 18.0f, -8.5f);
        this.nose.func_78787_b(64, 32);
        this.nose.field_78809_i = true;
        this.setRotation(this.nose, 0.0f, 0.0f, 0.0f);
        this.teeth = new ModelRenderer((ModelBase)this, 0, 0);
        this.teeth.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 1);
        this.teeth.func_78793_a(2.0f, 19.0f, -8.2f);
        this.teeth.func_78787_b(64, 32);
        this.teeth.field_78809_i = true;
        this.setRotation(this.teeth, 0.0f, 0.0f, 0.0f);
        this.body = new ModelRenderer((ModelBase)this, 0, 13);
        this.body.func_78789_a(0.0f, 0.0f, 0.0f, 8, 8, 10);
        this.body.func_78793_a(-1.0f, 14.0f, -3.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.tail = new ModelRenderer((ModelBase)this, 22, 0);
        this.tail.func_78789_a(0.0f, -1.0f, 0.0f, 5, 1, 8);
        this.tail.func_78793_a(0.5f, 21.0f, 7.0f);
        this.tail.func_78787_b(64, 32);
        this.tail.field_78809_i = true;
        this.setRotation(this.tail, 0.0f, 0.0f, 0.0f);
        this.rff = new ModelRenderer((ModelBase)this, 22, 9);
        this.rff.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 2);
        this.rff.func_78793_a(-0.5f, 22.0f, -2.5f);
        this.rff.func_78787_b(64, 32);
        this.rff.field_78809_i = true;
        this.setRotation(this.rff, 0.0f, 0.0f, 0.0f);
        this.lff = new ModelRenderer((ModelBase)this, 22, 9);
        this.lff.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 2);
        this.lff.func_78793_a(4.5f, 22.0f, -2.5f);
        this.lff.func_78787_b(64, 32);
        this.lff.field_78809_i = true;
        this.setRotation(this.lff, 0.0f, 0.0f, 0.0f);
        this.rrf = new ModelRenderer((ModelBase)this, 22, 9);
        this.rrf.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 2);
        this.rrf.func_78793_a(-0.5f, 22.0f, 4.5f);
        this.rrf.func_78787_b(64, 32);
        this.rrf.field_78809_i = true;
        this.setRotation(this.rrf, 0.0f, 0.0f, 0.0f);
        this.lrf = new ModelRenderer((ModelBase)this, 22, 9);
        this.lrf.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 2);
        this.lrf.func_78793_a(4.5f, 22.0f, 4.5f);
        this.lrf.func_78787_b(64, 32);
        this.lrf.field_78809_i = true;
        this.setRotation(this.lrf, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        this.rff.field_78795_f = this.lrf.field_78795_f = (newangle = MathHelper.func_76134_b((float)(f2 * 3.7f * this.wingspeed)) * (float)Math.PI * 0.45f * f1);
        this.lff.field_78795_f = this.rrf.field_78795_f = -newangle;
        this.teeth.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 2.7f * this.wingspeed)) * (float)Math.PI * 0.25f;
        this.tail.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.5f * this.wingspeed)) * (float)Math.PI * 0.05f;
        this.head.func_78785_a(f5);
        this.nose.func_78785_a(f5);
        this.teeth.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.tail.func_78785_a(f5);
        this.rff.func_78785_a(f5);
        this.lff.func_78785_a(f5);
        this.rrf.func_78785_a(f5);
        this.lrf.func_78785_a(f5);
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

