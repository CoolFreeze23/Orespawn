/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Peacock;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelPeacock
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer lleg;
    ModelRenderer rleg;
    ModelRenderer body;
    ModelRenderer neck;
    ModelRenderer head1;
    ModelRenderer head2;
    ModelRenderer hf1;
    ModelRenderer hf2;
    ModelRenderer hf3;
    ModelRenderer tailf1;
    ModelRenderer tailf2;
    ModelRenderer tailf3;
    ModelRenderer tailf4;
    ModelRenderer tailf5;
    ModelRenderer tailf6;
    ModelRenderer tailf7;

    public ModelPeacock(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 128;
        this.field_78089_u = 128;
        this.lleg = new ModelRenderer((ModelBase)this, 0, 20);
        this.lleg.func_78789_a(0.0f, 0.0f, 0.0f, 1, 7, 1);
        this.lleg.func_78793_a(1.0f, 17.0f, 0.0f);
        this.lleg.func_78787_b(128, 128);
        this.lleg.field_78809_i = true;
        this.setRotation(this.lleg, 0.0f, 0.0f, 0.0f);
        this.rleg = new ModelRenderer((ModelBase)this, 5, 20);
        this.rleg.func_78789_a(0.0f, 0.0f, 0.0f, 1, 7, 1);
        this.rleg.func_78793_a(-1.0f, 17.0f, 0.0f);
        this.rleg.func_78787_b(128, 128);
        this.rleg.field_78809_i = true;
        this.setRotation(this.rleg, 0.0f, 0.0f, 0.0f);
        this.body = new ModelRenderer((ModelBase)this, 88, 0);
        this.body.func_78789_a(-2.0f, -2.0f, -5.0f, 5, 4, 11);
        this.body.func_78793_a(0.0f, 15.0f, 1.0f);
        this.body.func_78787_b(128, 128);
        this.body.field_78809_i = true;
        this.setRotation(this.body, -0.1396263f, 0.0f, 0.0f);
        this.neck = new ModelRenderer((ModelBase)this, 70, 0);
        this.neck.func_78789_a(-0.5f, -1.0f, -6.0f, 2, 2, 6);
        this.neck.func_78793_a(0.0f, 14.0f, -3.0f);
        this.neck.func_78787_b(128, 128);
        this.neck.field_78809_i = true;
        this.setRotation(this.neck, -0.5585054f, 0.0f, 0.0f);
        this.head1 = new ModelRenderer((ModelBase)this, 56, 0);
        this.head1.func_78789_a(-0.5f, -2.0f, -2.0f, 2, 2, 4);
        this.head1.func_78793_a(0.0f, 12.0f, -8.0f);
        this.head1.func_78787_b(128, 128);
        this.head1.field_78809_i = true;
        this.setRotation(this.head1, 0.0f, 0.0f, 0.0f);
        this.head2 = new ModelRenderer((ModelBase)this, 48, 0);
        this.head2.func_78789_a(0.0f, -1.0f, -4.0f, 1, 1, 2);
        this.head2.func_78793_a(0.0f, 12.0f, -8.0f);
        this.head2.func_78787_b(128, 128);
        this.head2.field_78809_i = true;
        this.setRotation(this.head2, 0.0f, 0.0f, 0.0f);
        this.hf1 = new ModelRenderer((ModelBase)this, 8, 0);
        this.hf1.func_78789_a(0.5f, -9.0f, -1.5f, 0, 7, 3);
        this.hf1.func_78793_a(0.0f, 12.0f, -8.0f);
        this.hf1.func_78787_b(128, 128);
        this.hf1.field_78809_i = true;
        this.setRotation(this.hf1, 0.4014257f, 0.0f, 0.0f);
        this.hf2 = new ModelRenderer((ModelBase)this, 8, 0);
        this.hf2.func_78789_a(0.5f, -9.0f, -1.5f, 0, 7, 3);
        this.hf2.func_78793_a(0.0f, 12.0f, -8.0f);
        this.hf2.func_78787_b(128, 128);
        this.hf2.field_78809_i = true;
        this.setRotation(this.hf2, -0.1745329f, 0.0f, 0.0f);
        this.hf3 = new ModelRenderer((ModelBase)this, 8, 0);
        this.hf3.func_78789_a(0.5f, -9.0f, -1.5f, 0, 7, 3);
        this.hf3.func_78793_a(0.0f, 12.0f, -8.0f);
        this.hf3.func_78787_b(128, 128);
        this.hf3.field_78809_i = true;
        this.setRotation(this.hf3, -0.6981317f, 0.0f, 0.0f);
        this.tailf1 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf1.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf1.func_78793_a(0.5f, 14.0f, 7.0f);
        this.tailf1.func_78787_b(128, 128);
        this.tailf1.field_78809_i = true;
        this.setRotation(this.tailf1, 0.0f, 0.0f, 0.0f);
        this.tailf2 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf2.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf2.func_78793_a(0.5f, 14.0f, 7.0f);
        this.tailf2.func_78787_b(128, 128);
        this.tailf2.field_78809_i = true;
        this.setRotation(this.tailf2, 0.0f, 0.0f, 0.0f);
        this.tailf3 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf3.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf3.func_78793_a(0.5f, 14.0f, 7.0f);
        this.tailf3.func_78787_b(128, 128);
        this.tailf3.field_78809_i = true;
        this.setRotation(this.tailf3, 0.0f, 0.0f, 0.0f);
        this.tailf4 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf4.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf4.func_78793_a(0.5f, 14.0f, 7.0f);
        this.tailf4.func_78787_b(128, 128);
        this.tailf4.field_78809_i = true;
        this.setRotation(this.tailf4, 0.0f, 0.0f, 0.0f);
        this.tailf5 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf5.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf5.func_78793_a(0.5f, 14.0f, 7.0f);
        this.tailf5.func_78787_b(128, 128);
        this.tailf5.field_78809_i = true;
        this.setRotation(this.tailf5, 0.0f, 0.0f, 0.0f);
        this.tailf6 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf6.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf6.func_78793_a(0.5f, 14.0f, 7.0f);
        this.tailf6.func_78787_b(128, 128);
        this.tailf6.field_78809_i = true;
        this.setRotation(this.tailf6, 0.0f, 0.0f, 0.0f);
        this.tailf7 = new ModelRenderer((ModelBase)this, 0, 50);
        this.tailf7.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 0, 30);
        this.tailf7.func_78793_a(0.514f, 14.0f, 7.0f);
        this.tailf7.func_78787_b(128, 128);
        this.tailf7.field_78809_i = true;
        this.setRotation(this.tailf7, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Peacock p = (Peacock)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.15f * f1 : 0.0f;
        this.lleg.field_78795_f = newangle;
        this.rleg.field_78795_f = -newangle;
        if (p.getBlink() > 0) {
            this.hf1.field_78795_f = 0.401f;
            this.hf2.field_78795_f = -0.174f;
            this.hf3.field_78795_f = -0.698f;
            this.tailf1.field_78795_f = 1.047f;
            this.tailf2.field_78795_f = 1.047f;
            this.tailf3.field_78795_f = 1.047f;
            this.tailf4.field_78795_f = 1.047f;
            this.tailf5.field_78795_f = 1.047f;
            this.tailf6.field_78795_f = 1.047f;
            this.tailf7.field_78795_f = 1.047f;
            this.tailf1.field_78808_h = -0.4f;
            this.tailf2.field_78808_h = -0.8f;
            this.tailf3.field_78808_h = -1.2f;
            this.tailf4.field_78808_h = 0.4f;
            this.tailf5.field_78808_h = 0.8f;
            this.tailf6.field_78808_h = 1.2f;
        } else {
            this.hf1.field_78795_f = -1.06f;
            this.hf2.field_78795_f = -1.06f;
            this.hf3.field_78795_f = -1.06f;
            this.tailf1.field_78795_f = 0.0f;
            this.tailf2.field_78795_f = 0.0f;
            this.tailf3.field_78795_f = 0.0f;
            this.tailf4.field_78795_f = 0.0f;
            this.tailf5.field_78795_f = 0.0f;
            this.tailf6.field_78795_f = 0.0f;
            this.tailf7.field_78795_f = 0.0f;
            this.tailf1.field_78808_h = 0.0f;
            this.tailf2.field_78808_h = 0.0f;
            this.tailf3.field_78808_h = 0.0f;
            this.tailf4.field_78808_h = 0.0f;
            this.tailf5.field_78808_h = 0.0f;
            this.tailf6.field_78808_h = 0.0f;
        }
        this.lleg.func_78785_a(f5);
        this.rleg.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.neck.func_78785_a(f5);
        this.head1.func_78785_a(f5);
        this.head2.func_78785_a(f5);
        this.hf1.func_78785_a(f5);
        this.hf2.func_78785_a(f5);
        this.hf3.func_78785_a(f5);
        this.tailf1.func_78785_a(f5);
        this.tailf2.func_78785_a(f5);
        this.tailf3.func_78785_a(f5);
        this.tailf4.func_78785_a(f5);
        this.tailf5.func_78785_a(f5);
        this.tailf6.func_78785_a(f5);
        this.tailf7.func_78785_a(f5);
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

