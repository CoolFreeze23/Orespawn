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

import danger.orespawn.entity.Cassowary;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelCassowary
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer tail;
    ModelRenderer body;
    ModelRenderer neck1;
    ModelRenderer neck;
    ModelRenderer head;
    ModelRenderer beak;
    ModelRenderer leg1;
    ModelRenderer leg2;
    ModelRenderer crest;
    ModelRenderer foot1;
    ModelRenderer foot2;
    ModelRenderer gobbler;

    public ModelCassowary(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.tail = new ModelRenderer((ModelBase)this, 38, 16);
        this.tail.func_78789_a(-3.0f, 0.0f, 0.0f, 6, 9, 7);
        this.tail.func_78793_a(0.0f, 8.0f, 1.0f);
        this.tail.func_78787_b(64, 32);
        this.tail.field_78809_i = true;
        this.setRotation(this.tail, 0.8922867f, 0.0f, 0.0f);
        this.body = new ModelRenderer((ModelBase)this, 0, 13);
        this.body.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 10, 9);
        this.body.func_78793_a(0.0f, 5.0f, -3.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.3346075f, 0.0f, 0.0f);
        this.neck1 = new ModelRenderer((ModelBase)this, 48, 0);
        this.neck1.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 5, 4);
        this.neck1.func_78793_a(0.0f, 4.0f, -1.0f);
        this.neck1.func_78787_b(64, 32);
        this.neck1.field_78809_i = true;
        this.setRotation(this.neck1, -1.189716f, 0.0f, 0.0f);
        this.neck = new ModelRenderer((ModelBase)this, 38, 0);
        this.neck.func_78789_a(-1.0f, 0.0f, 0.0f, 2, 7, 2);
        this.neck.func_78793_a(0.0f, 8.0f, -3.0f);
        this.neck.func_78787_b(64, 32);
        this.neck.field_78809_i = true;
        this.setRotation(this.neck, -2.806985f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 24, 0);
        this.head.func_78789_a(-1.0f, -2.0f, -3.0f, 2, 2, 4);
        this.head.func_78793_a(0.0f, 2.0f, -6.0f);
        this.head.func_78787_b(64, 32);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0371786f, 0.0f, 0.0f);
        this.beak = new ModelRenderer((ModelBase)this, 28, 7);
        this.beak.func_78789_a(-0.5f, 0.0f, 3.0f, 1, 1, 3);
        this.beak.func_78793_a(0.0f, 2.0f, -6.0f);
        this.beak.func_78787_b(64, 32);
        this.beak.field_78809_i = true;
        this.setRotation(this.beak, -3.104414f, 0.0f, 0.0f);
        this.leg1 = new ModelRenderer((ModelBase)this, 0, 0);
        this.leg1.func_78789_a(-0.5f, 0.0f, -1.0f, 1, 11, 2);
        this.leg1.func_78793_a(3.0f, 12.0f, 3.0f);
        this.leg1.func_78787_b(64, 32);
        this.leg1.field_78809_i = true;
        this.setRotation(this.leg1, 0.0f, 0.0f, 0.0f);
        this.leg2 = new ModelRenderer((ModelBase)this, 0, 0);
        this.leg2.func_78789_a(-0.5f, 0.0f, -1.0f, 1, 11, 2);
        this.leg2.func_78793_a(-3.0f, 12.0f, 3.0f);
        this.leg2.func_78787_b(64, 32);
        this.leg2.field_78809_i = true;
        this.setRotation(this.leg2, 0.0f, 0.0f, 0.0f);
        this.crest = new ModelRenderer((ModelBase)this, 10, 0);
        this.crest.func_78789_a(-0.5f, -4.0f, 1.0f, 1, 4, 5);
        this.crest.func_78793_a(0.0f, 2.0f, -6.0f);
        this.crest.func_78787_b(64, 32);
        this.crest.field_78809_i = true;
        this.setRotation(this.crest, 1.710216f, 0.0f, 0.0f);
        this.foot1 = new ModelRenderer((ModelBase)this, 47, 10);
        this.foot1.func_78789_a(-1.033333f, 11.0f, -2.0f, 2, 1, 3);
        this.foot1.func_78793_a(-3.0f, 12.0f, 3.0f);
        this.foot1.func_78787_b(64, 32);
        this.foot1.field_78809_i = true;
        this.setRotation(this.foot1, 0.0f, 0.0f, 0.0f);
        this.foot2 = new ModelRenderer((ModelBase)this, 47, 10);
        this.foot2.func_78789_a(-1.0f, 11.0f, -2.0f, 2, 1, 3);
        this.foot2.func_78793_a(3.0f, 12.0f, 3.0f);
        this.foot2.func_78787_b(64, 32);
        this.foot2.field_78809_i = true;
        this.setRotation(this.foot2, 0.0f, 0.0f, 0.0f);
        this.gobbler = new ModelRenderer((ModelBase)this, 38, 10);
        this.gobbler.func_78789_a(-0.5f, -1.0f, -2.5f, 1, 5, 1);
        this.gobbler.func_78793_a(0.0f, 8.0f, -3.0f);
        this.gobbler.func_78787_b(64, 32);
        this.gobbler.field_78809_i = true;
        this.setRotation(this.gobbler, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Cassowary e = (Cassowary)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if ((double)f1 > 0.1) {
            newangle = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.15f * f1;
            newangle2 = MathHelper.func_76134_b((float)(f2 * 2.6f * this.wingspeed)) * (float)Math.PI * 0.1f * f1;
        } else {
            newangle2 = 0.0f;
            newangle = 0.0f;
        }
        this.leg1.field_78795_f = this.foot2.field_78795_f = newangle;
        this.leg2.field_78795_f = this.foot1.field_78795_f = -newangle;
        this.neck.field_78795_f = -2.827f + newangle2;
        this.gobbler.field_78795_f = newangle2;
        this.crest.field_78798_e = this.beak.field_78798_e = this.neck.field_78798_e + MathHelper.func_76126_a((float)this.neck.field_78795_f) * 7.0f;
        this.head.field_78798_e = this.beak.field_78798_e;
        this.crest.field_78797_d = this.beak.field_78797_d = this.neck.field_78797_d + MathHelper.func_76134_b((float)this.neck.field_78795_f) * 7.0f;
        this.head.field_78797_d = this.beak.field_78797_d;
        this.tail.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.neck1.func_78785_a(f5);
        this.neck.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.beak.func_78785_a(f5);
        this.leg1.func_78785_a(f5);
        this.leg2.func_78785_a(f5);
        this.crest.func_78785_a(f5);
        this.foot1.func_78785_a(f5);
        this.foot2.func_78785_a(f5);
        this.gobbler.func_78785_a(f5);
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

