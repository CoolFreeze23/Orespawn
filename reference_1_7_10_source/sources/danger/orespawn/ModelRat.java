/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Rat;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelRat
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer body;
    ModelRenderer tail1;
    ModelRenderer tail2;
    ModelRenderer lfleg;
    ModelRenderer rfleg;
    ModelRenderer lrleg;
    ModelRenderer rrleg;
    ModelRenderer body2;
    ModelRenderer head;
    ModelRenderer nose;
    ModelRenderer lear;
    ModelRenderer rear;

    public ModelRat(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.body = new ModelRenderer((ModelBase)this, 27, 0);
        this.body.func_78789_a(-2.0f, -1.0f, 0.0f, 5, 3, 10);
        this.body.func_78793_a(0.0f, 20.0f, -3.0f);
        this.body.func_78787_b(64, 64);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 0, 30);
        this.tail1.func_78789_a(-0.5f, -1.0f, 0.0f, 2, 2, 9);
        this.tail1.func_78793_a(0.0f, 21.0f, 7.0f);
        this.tail1.func_78787_b(64, 64);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.0f, 0.0f, 0.0f);
        this.tail2 = new ModelRenderer((ModelBase)this, 0, 43);
        this.tail2.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 12);
        this.tail2.func_78793_a(0.0f, 21.0f, 16.0f);
        this.tail2.func_78787_b(64, 64);
        this.tail2.field_78809_i = true;
        this.setRotation(this.tail2, 0.0f, 0.0f, 0.0f);
        this.lfleg = new ModelRenderer((ModelBase)this, 0, 14);
        this.lfleg.func_78789_a(0.0f, 0.0f, 0.0f, 1, 2, 1);
        this.lfleg.func_78793_a(2.0f, 22.0f, -2.0f);
        this.lfleg.func_78787_b(64, 64);
        this.lfleg.field_78809_i = true;
        this.setRotation(this.lfleg, 0.0f, 0.0f, 0.0f);
        this.rfleg = new ModelRenderer((ModelBase)this, 10, 14);
        this.rfleg.func_78789_a(0.0f, 0.0f, 0.0f, 1, 2, 1);
        this.rfleg.func_78793_a(-2.0f, 22.0f, -2.0f);
        this.rfleg.func_78787_b(64, 64);
        this.rfleg.field_78809_i = true;
        this.setRotation(this.rfleg, 0.0f, 0.0f, 0.0f);
        this.lrleg = new ModelRenderer((ModelBase)this, 0, 18);
        this.lrleg.func_78789_a(0.0f, 0.0f, 0.0f, 2, 4, 2);
        this.lrleg.func_78793_a(2.0f, 20.0f, 4.0f);
        this.lrleg.func_78787_b(64, 64);
        this.lrleg.field_78809_i = true;
        this.setRotation(this.lrleg, 0.0f, 0.0f, 0.0f);
        this.rrleg = new ModelRenderer((ModelBase)this, 9, 18);
        this.rrleg.func_78789_a(0.0f, 0.0f, 0.0f, 2, 4, 2);
        this.rrleg.func_78793_a(-3.0f, 20.0f, 4.0f);
        this.rrleg.func_78787_b(64, 64);
        this.rrleg.field_78809_i = true;
        this.setRotation(this.rrleg, 0.0f, 0.0f, 0.0f);
        this.body2 = new ModelRenderer((ModelBase)this, 0, 0);
        this.body2.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 6);
        this.body2.func_78793_a(0.0f, 18.0f, 0.0f);
        this.body2.func_78787_b(64, 64);
        this.body2.field_78809_i = true;
        this.setRotation(this.body2, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 27, 17);
        this.head.func_78789_a(-1.0f, -2.0f, -3.0f, 3, 2, 4);
        this.head.func_78793_a(0.0f, 22.0f, -4.0f);
        this.head.func_78787_b(64, 64);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.nose = new ModelRenderer((ModelBase)this, 27, 25);
        this.nose.func_78789_a(0.0f, -1.0f, -5.0f, 1, 1, 2);
        this.nose.func_78793_a(0.0f, 22.0f, -4.0f);
        this.nose.func_78787_b(64, 64);
        this.nose.field_78809_i = true;
        this.setRotation(this.nose, 0.0f, 0.0f, 0.0f);
        this.lear = new ModelRenderer((ModelBase)this, 0, 9);
        this.lear.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.lear.func_78793_a(1.5f, 19.5f, -4.0f);
        this.lear.func_78787_b(64, 64);
        this.lear.field_78809_i = true;
        this.setRotation(this.lear, 0.0f, 0.0f, 0.0f);
        this.rear = new ModelRenderer((ModelBase)this, 5, 9);
        this.rear.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.rear.func_78793_a(-1.5f, 19.5f, -4.0f);
        this.rear.func_78787_b(64, 64);
        this.rear.field_78809_i = true;
        this.setRotation(this.rear, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Rat r = (Rat)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 1.7f * this.wingspeed)) * (float)Math.PI * 0.25f * f1 : 0.0f;
        this.rfleg.field_78795_f = newangle;
        this.lfleg.field_78795_f = -newangle;
        this.rrleg.field_78795_f = -newangle;
        this.lrleg.field_78795_f = newangle;
        newangle = r.getAttacking() != 0 ? MathHelper.func_76134_b((float)(f2 * 1.5f * this.wingspeed)) * (float)Math.PI * 0.25f : MathHelper.func_76134_b((float)(f2 * 0.4f * this.wingspeed)) * (float)Math.PI * 0.05f;
        this.tail1.field_78796_g = newangle * 0.5f;
        this.tail2.field_78796_g = newangle * 1.25f;
        this.tail2.field_78798_e = this.tail1.field_78798_e + (float)Math.cos(this.tail1.field_78796_g) * 9.0f;
        this.tail2.field_78800_c = this.tail1.field_78800_c + (float)Math.sin(this.tail1.field_78796_g) * 9.0f;
        this.body.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.tail2.func_78785_a(f5);
        this.lfleg.func_78785_a(f5);
        this.rfleg.func_78785_a(f5);
        this.lrleg.func_78785_a(f5);
        this.rrleg.func_78785_a(f5);
        this.body2.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.nose.func_78785_a(f5);
        this.lear.func_78785_a(f5);
        this.rear.func_78785_a(f5);
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

