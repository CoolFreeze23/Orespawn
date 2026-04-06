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

import danger.orespawn.AttackSquid;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelAttackSquid
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer tent1;
    ModelRenderer tent2;
    ModelRenderer tent3;
    ModelRenderer tent4;
    ModelRenderer tent5;
    ModelRenderer tent6;
    ModelRenderer tent7;
    ModelRenderer body;
    ModelRenderer tent8;

    public ModelAttackSquid(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.tent1 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent1.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 9, 2);
        this.tent1.func_78793_a(5.0f, 15.0f, -1.0f);
        this.tent1.func_78787_b(64, 32);
        this.tent1.field_78809_i = true;
        this.setRotation(this.tent1, -0.9250245f, -1.745329f, 0.0f);
        this.tent2 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent2.func_78789_a(-8.0f, -1.0f, -1.0f, 8, 2, 2);
        this.tent2.func_78793_a(-2.0f, 15.0f, -3.0f);
        this.tent2.func_78787_b(64, 32);
        this.tent2.field_78809_i = true;
        this.setRotation(this.tent2, -0.1745329f, -0.6632251f, -0.2443461f);
        this.tent3 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent3.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 10, 2);
        this.tent3.func_78793_a(1.0f, 15.0f, -4.0f);
        this.tent3.func_78787_b(64, 32);
        this.tent3.field_78809_i = true;
        this.setRotation(this.tent3, -1.134464f, 0.3316126f, 0.0f);
        this.tent4 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent4.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 10, 2);
        this.tent4.func_78793_a(-3.0f, 15.0f, -1.0f);
        this.tent4.func_78787_b(64, 32);
        this.tent4.field_78809_i = true;
        this.setRotation(this.tent4, 0.5585054f, -1.692969f, 0.0f);
        this.tent5 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent5.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 10, 2);
        this.tent5.func_78793_a(1.0f, 15.0f, 3.0f);
        this.tent5.func_78787_b(64, 32);
        this.tent5.field_78809_i = true;
        this.setRotation(this.tent5, 0.5410521f, 0.2268928f, 0.0f);
        this.tent6 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent6.func_78789_a(-1.0f, -1.0f, 0.0f, 2, 2, 8);
        this.tent6.func_78793_a(-2.0f, 15.0f, 2.0f);
        this.tent6.func_78787_b(64, 32);
        this.tent6.field_78809_i = true;
        this.setRotation(this.tent6, -0.418879f, -0.6806784f, 0.0f);
        this.tent7 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent7.func_78789_a(0.0f, -1.0f, -1.0f, 8, 2, 2);
        this.tent7.func_78793_a(3.0f, 15.0f, 1.0f);
        this.tent7.func_78787_b(64, 32);
        this.tent7.field_78809_i = true;
        this.setRotation(this.tent7, -0.1919862f, -0.6632251f, 0.418879f);
        this.body = new ModelRenderer((ModelBase)this, 0, 0);
        this.body.func_78789_a(-4.0f, -10.0f, -4.0f, 8, 10, 8);
        this.body.func_78793_a(1.0f, 16.0f, -1.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, -0.1919862f, -0.6806784f, 0.0f);
        this.tent8 = new ModelRenderer((ModelBase)this, 0, 18);
        this.tent8.func_78789_a(-1.0f, -1.0f, -8.0f, 2, 2, 8);
        this.tent8.func_78793_a(3.0f, 15.0f, -4.0f);
        this.tent8.func_78787_b(64, 32);
        this.tent8.field_78809_i = true;
        this.setRotation(this.tent8, 0.1919862f, -0.6806784f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        AttackSquid e = (AttackSquid)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangleA = 0.0f;
        float newangleB = 0.0f;
        float newangle8 = 0.0f;
        float newangle1 = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        float newangle4 = 0.0f;
        float newangle5 = 0.0f;
        float newangle6 = 0.0f;
        float newangle7 = 0.0f;
        float pi4 = 0.7853982f;
        if ((double)f1 > 0.1) {
            newangleA = MathHelper.func_76134_b((float)(f2 * 0.25f * this.wingspeed)) * (float)Math.PI * 0.04f * f1;
            newangleB = MathHelper.func_76134_b((float)(f2 * 0.39f * this.wingspeed)) * (float)Math.PI * 0.04f * f1;
            newangle1 = MathHelper.func_76134_b((float)(f2 * 1.2f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle2 = MathHelper.func_76134_b((float)(f2 * 1.1f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle3 = MathHelper.func_76134_b((float)(f2 * 1.0f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle4 = MathHelper.func_76134_b((float)(f2 * 1.9f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle5 = MathHelper.func_76134_b((float)(f2 * 1.8f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle6 = MathHelper.func_76134_b((float)(f2 * 1.7f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle7 = MathHelper.func_76134_b((float)(f2 * 1.6f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
            newangle8 = MathHelper.func_76134_b((float)(f2 * 1.5f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
        } else {
            newangleA = MathHelper.func_76134_b((float)(f2 * 0.25f * this.wingspeed)) * (float)Math.PI * 0.01f;
            newangleB = MathHelper.func_76134_b((float)(f2 * 0.39f * this.wingspeed)) * (float)Math.PI * 0.01f;
            newangle1 = MathHelper.func_76134_b((float)(f2 * 1.2f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle2 = MathHelper.func_76134_b((float)(f2 * 1.1f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle3 = MathHelper.func_76134_b((float)(f2 * 1.0f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle4 = MathHelper.func_76134_b((float)(f2 * 1.9f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle5 = MathHelper.func_76134_b((float)(f2 * 1.8f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle6 = MathHelper.func_76134_b((float)(f2 * 1.7f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle7 = MathHelper.func_76134_b((float)(f2 * 1.6f * this.wingspeed)) * (float)Math.PI * 0.1f;
            newangle8 = MathHelper.func_76134_b((float)(f2 * 1.5f * this.wingspeed)) * (float)Math.PI * 0.1f;
        }
        this.tent1.field_78795_f = newangle1 - 1.03f;
        this.tent7.field_78808_h = newangle2 + 0.37f;
        this.tent5.field_78795_f = newangle3 + 0.6f;
        this.tent6.field_78795_f = newangle4 - 0.48f;
        this.tent4.field_78795_f = newangle5 + 0.63f;
        this.tent2.field_78808_h = newangle6 - 0.26f;
        this.tent3.field_78795_f = newangle7 - 1.03f;
        this.tent8.field_78795_f = newangle8 + 0.43f;
        this.body.field_78795_f = newangleA;
        this.body.field_78808_h = newangleB;
        this.body.field_78796_g = newangleA = (float)Math.toRadians(f3) * 0.75f;
        this.tent1.func_78785_a(f5);
        this.tent2.func_78785_a(f5);
        this.tent3.func_78785_a(f5);
        this.tent4.func_78785_a(f5);
        this.tent5.func_78785_a(f5);
        this.tent6.func_78785_a(f5);
        this.tent7.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.tent8.func_78785_a(f5);
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

