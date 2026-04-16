/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelCloudShark
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer body;
    ModelRenderer head;
    ModelRenderer jaw;
    ModelRenderer topfin;
    ModelRenderer bbody;
    ModelRenderer fins;
    ModelRenderer leftfin;
    ModelRenderer rightfin;

    public ModelCloudShark(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.body = new ModelRenderer((ModelBase)this, 0, 0);
        this.body.func_78789_a(0.0f, 0.0f, 0.0f, 6, 8, 15);
        this.body.func_78793_a(-4.0f, 11.0f, 0.0f);
        this.body.func_78787_b(64, 64);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 0, 51);
        this.head.func_78789_a(-2.5f, 0.0f, -8.0f, 5, 5, 8);
        this.head.func_78793_a(-1.0f, 11.0f, 0.0f);
        this.head.func_78787_b(64, 64);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.jaw = new ModelRenderer((ModelBase)this, 42, 0);
        this.jaw.func_78789_a(-2.5f, 0.0f, -6.0f, 5, 2, 6);
        this.jaw.func_78793_a(-1.0f, 15.0f, 0.0f);
        this.jaw.func_78787_b(64, 64);
        this.jaw.field_78809_i = true;
        this.setRotation(this.jaw, 0.5056291f, 0.0f, 0.0f);
        this.topfin = new ModelRenderer((ModelBase)this, 0, 0);
        this.topfin.func_78789_a(0.0f, 0.0f, 0.0f, 1, 3, 6);
        this.topfin.func_78793_a(-1.5f, 11.0f, 5.0f);
        this.topfin.func_78787_b(64, 64);
        this.topfin.field_78809_i = true;
        this.setRotation(this.topfin, 0.935765f, 0.0f, 0.0f);
        this.bbody = new ModelRenderer((ModelBase)this, 0, 9);
        this.bbody.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 8, 6);
        this.bbody.func_78793_a(-1.0f, 11.0f, 15.0f);
        this.bbody.func_78787_b(64, 64);
        this.bbody.field_78809_i = true;
        this.setRotation(this.bbody, 0.0f, 0.0f, 0.0f);
        this.fins = new ModelRenderer((ModelBase)this, 0, 24);
        this.fins.func_78789_a(0.0f, 0.0f, 0.0f, 0, 10, 10);
        this.fins.func_78793_a(-1.0f, 16.0f, 16.0f);
        this.fins.func_78787_b(64, 64);
        this.fins.field_78809_i = true;
        this.setRotation(this.fins, 0.9220296f, 0.0f, 0.0f);
        this.leftfin = new ModelRenderer((ModelBase)this, 0, 0);
        this.leftfin.func_78789_a(0.0f, 0.0f, 0.0f, 0, 3, 7);
        this.leftfin.func_78793_a(2.0f, 16.0f, 6.0f);
        this.leftfin.func_78787_b(64, 64);
        this.leftfin.field_78809_i = true;
        this.setRotation(this.leftfin, -0.6108652f, 1.134464f, -0.6108652f);
        this.rightfin = new ModelRenderer((ModelBase)this, 0, 0);
        this.rightfin.func_78789_a(0.0f, 0.0f, 0.0f, 0, 3, 7);
        this.rightfin.func_78793_a(-4.0f, 16.0f, 6.0f);
        this.rightfin.func_78787_b(64, 64);
        this.rightfin.field_78809_i = true;
        this.setRotation(this.rightfin, -0.6283185f, -1.134464f, 0.6108652f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = MathHelper.func_76134_b((float)(f2 * 0.7f * this.wingspeed)) * (float)Math.PI * 0.15f;
        this.leftfin.field_78796_g = 1.15f + newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 1.5f * this.wingspeed)) * (float)Math.PI * 0.15f;
        this.rightfin.field_78796_g = -0.9f + newangle;
        this.fins.field_78796_g = newangle = MathHelper.func_76134_b((float)(f2 * 1.5f * this.wingspeed)) * (float)Math.PI * 0.25f;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.5f * this.wingspeed)) * (float)Math.PI * 0.1f;
        this.jaw.field_78795_f = 0.5f + newangle;
        this.body.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.jaw.func_78785_a(f5);
        this.topfin.func_78785_a(f5);
        this.bbody.func_78785_a(f5);
        this.fins.func_78785_a(f5);
        this.leftfin.func_78785_a(f5);
        this.rightfin.func_78785_a(f5);
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

