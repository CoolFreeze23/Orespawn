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

import danger.orespawn.Cricket;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelCricket
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer body;
    ModelRenderer head;
    ModelRenderer abdomen;
    ModelRenderer lfleg;
    ModelRenderer lrleg;
    ModelRenderer rfleg;
    ModelRenderer rrleg;
    ModelRenderer lleg1;
    ModelRenderer rleg1;
    ModelRenderer lleg2;
    ModelRenderer rleg2;

    public ModelCricket(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.body = new ModelRenderer((ModelBase)this, 0, 25);
        this.body.func_78789_a(-1.0f, -1.0f, -3.0f, 3, 3, 6);
        this.body.func_78793_a(0.0f, 21.0f, 0.0f);
        this.body.func_78787_b(64, 64);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 0, 17);
        this.head.func_78789_a(-1.0f, -2.0f, -1.0f, 3, 4, 3);
        this.head.func_78793_a(0.0f, 21.0f, -5.0f);
        this.head.func_78787_b(64, 64);
        this.head.field_78809_i = true;
        this.setRotation(this.head, -0.1745329f, 0.0f, 0.0f);
        this.abdomen = new ModelRenderer((ModelBase)this, 0, 36);
        this.abdomen.func_78789_a(-0.5f, -1.0f, 3.0f, 2, 2, 3);
        this.abdomen.func_78793_a(0.0f, 21.0f, 0.0f);
        this.abdomen.func_78787_b(64, 64);
        this.abdomen.field_78809_i = true;
        this.setRotation(this.abdomen, 0.0f, 0.0f, 0.0f);
        this.lfleg = new ModelRenderer((ModelBase)this, 25, 0);
        this.lfleg.func_78789_a(2.0f, 0.0f, 0.0f, 5, 1, 1);
        this.lfleg.func_78793_a(0.0f, 21.0f, -2.0f);
        this.lfleg.func_78787_b(64, 64);
        this.lfleg.field_78809_i = true;
        this.setRotation(this.lfleg, 0.0f, 0.4712389f, 0.418879f);
        this.lrleg = new ModelRenderer((ModelBase)this, 23, 4);
        this.lrleg.func_78789_a(1.0f, 0.0f, -2.0f, 6, 1, 1);
        this.lrleg.func_78793_a(0.0f, 21.0f, 0.0f);
        this.lrleg.func_78787_b(64, 64);
        this.lrleg.field_78809_i = true;
        this.setRotation(this.lrleg, 0.0f, -0.296706f, 0.418879f);
        this.rfleg = new ModelRenderer((ModelBase)this, 25, 8);
        this.rfleg.func_78789_a(-7.0f, 0.0f, 0.0f, 5, 1, 1);
        this.rfleg.func_78793_a(1.0f, 21.0f, -2.0f);
        this.rfleg.func_78787_b(64, 64);
        this.rfleg.field_78809_i = true;
        this.setRotation(this.rfleg, 0.0f, -0.5410521f, -0.4363323f);
        this.rrleg = new ModelRenderer((ModelBase)this, 25, 12);
        this.rrleg.func_78789_a(-7.0f, -1.0f, 0.0f, 5, 1, 1);
        this.rrleg.func_78793_a(1.0f, 22.0f, -2.0f);
        this.rrleg.func_78787_b(64, 64);
        this.rrleg.field_78809_i = true;
        this.setRotation(this.rrleg, 0.0f, 0.3839724f, -0.418879f);
        this.lleg1 = new ModelRenderer((ModelBase)this, 40, 0);
        this.lleg1.func_78789_a(-1.0f, -1.0f, 0.0f, 1, 2, 8);
        this.lleg1.func_78793_a(2.0f, 22.0f, 0.0f);
        this.lleg1.func_78787_b(64, 64);
        this.lleg1.field_78809_i = true;
        this.setRotation(this.lleg1, 0.5585054f, 0.4363323f, 0.0f);
        this.rleg1 = new ModelRenderer((ModelBase)this, 40, 11);
        this.rleg1.func_78789_a(0.0f, -1.0f, 0.0f, 1, 2, 8);
        this.rleg1.func_78793_a(-1.0f, 22.0f, 0.0f);
        this.rleg1.func_78787_b(64, 64);
        this.rleg1.field_78809_i = true;
        this.setRotation(this.rleg1, 0.5585054f, -0.4363323f, 0.0f);
        this.lleg2 = new ModelRenderer((ModelBase)this, 21, 23);
        this.lleg2.func_78789_a(-0.5f, -6.5f, 4.5f, 1, 1, 8);
        this.lleg2.func_78793_a(2.0f, 22.0f, 0.0f);
        this.lleg2.func_78787_b(64, 64);
        this.lleg2.field_78809_i = true;
        this.setRotation(this.lleg2, -0.3665191f, 0.3490659f, 0.0f);
        this.rleg2 = new ModelRenderer((ModelBase)this, 21, 34);
        this.rleg2.func_78789_a(-0.5f, -6.5f, 4.0f, 1, 1, 8);
        this.rleg2.func_78793_a(-1.0f, 22.0f, 0.0f);
        this.rleg2.func_78787_b(64, 64);
        this.rleg2.field_78809_i = true;
        this.setRotation(this.rleg2, -0.3665191f, -0.3490659f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Cricket c = (Cricket)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * this.wingspeed)) * (float)Math.PI * 0.25f * f1 : 0.0f;
        this.lfleg.field_78796_g = 0.47f + newangle;
        this.rfleg.field_78796_g = -0.54f + newangle;
        this.lrleg.field_78796_g = -0.296f - newangle;
        this.rrleg.field_78796_g = 0.384f - newangle;
        if (c.getSinging() != 0) {
            newangle = MathHelper.func_76134_b((float)(f2 * 3.0f * this.wingspeed)) * (float)Math.PI * 0.25f;
            this.lleg1.field_78796_g = -0.035f;
            this.lleg2.field_78796_g = -0.105f;
            this.rleg1.field_78796_g = 0.035f;
            this.rleg2.field_78796_g = 0.105f;
        } else {
            newangle = 0.0f;
            this.lleg1.field_78796_g = 0.436f;
            this.lleg2.field_78796_g = 0.349f;
            this.rleg1.field_78796_g = -0.436f;
            this.rleg2.field_78796_g = -0.349f;
        }
        this.lleg1.field_78795_f = newangle + 0.558f;
        this.lleg2.field_78795_f = newangle - 0.366f;
        this.rleg1.field_78795_f = -newangle + 0.558f;
        this.rleg2.field_78795_f = -newangle - 0.366f;
        this.body.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.abdomen.func_78785_a(f5);
        this.lfleg.func_78785_a(f5);
        this.lrleg.func_78785_a(f5);
        this.rfleg.func_78785_a(f5);
        this.rrleg.func_78785_a(f5);
        this.lleg1.func_78785_a(f5);
        this.rleg1.func_78785_a(f5);
        this.lleg2.func_78785_a(f5);
        this.rleg2.func_78785_a(f5);
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

