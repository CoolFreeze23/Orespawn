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

import danger.orespawn.BandP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBandP
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer belly;
    ModelRenderer chest;
    ModelRenderer head;
    ModelRenderer lleg;
    ModelRenderer rleg;
    ModelRenderer larm;
    ModelRenderer rarm;

    public ModelBandP(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 128;
        this.belly = new ModelRenderer((ModelBase)this, 0, 61);
        this.belly.func_78789_a(-8.0f, -5.0f, -7.0f, 16, 10, 16);
        this.belly.func_78793_a(0.0f, 12.0f, 0.0f);
        this.belly.func_78787_b(64, 32);
        this.belly.field_78809_i = true;
        this.setRotation(this.belly, 0.0698132f, 0.0f, 0.0f);
        this.chest = new ModelRenderer((ModelBase)this, 0, 42);
        this.chest.func_78789_a(-5.0f, -3.0f, -5.0f, 10, 6, 10);
        this.chest.func_78793_a(0.0f, 5.0f, 2.0f);
        this.chest.func_78787_b(64, 32);
        this.chest.field_78809_i = true;
        this.setRotation(this.chest, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 0, 11);
        this.head.func_78789_a(-3.0f, -5.0f, -3.0f, 6, 6, 6);
        this.head.func_78793_a(0.0f, 1.0f, 3.0f);
        this.head.func_78787_b(64, 32);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.lleg = new ModelRenderer((ModelBase)this, 25, 90);
        this.lleg.func_78789_a(-2.0f, 0.0f, -3.0f, 6, 8, 6);
        this.lleg.func_78793_a(2.0f, 16.0f, 2.0f);
        this.lleg.func_78787_b(64, 32);
        this.lleg.field_78809_i = true;
        this.setRotation(this.lleg, 0.0f, 0.0f, 0.0f);
        this.rleg = new ModelRenderer((ModelBase)this, 0, 90);
        this.rleg.func_78789_a(-4.0f, 0.0f, -3.0f, 6, 8, 6);
        this.rleg.func_78793_a(-2.0f, 16.0f, 2.0f);
        this.rleg.func_78787_b(64, 32);
        this.rleg.field_78809_i = true;
        this.setRotation(this.rleg, 0.0f, 0.0f, 0.0f);
        this.larm = new ModelRenderer((ModelBase)this, 0, 25);
        this.larm.func_78789_a(-1.0f, -1.0f, -2.0f, 4, 10, 4);
        this.larm.func_78793_a(6.0f, 4.0f, 3.0f);
        this.larm.func_78787_b(64, 32);
        this.larm.field_78809_i = true;
        this.setRotation(this.larm, 0.0f, 0.0f, -0.4886922f);
        this.rarm = new ModelRenderer((ModelBase)this, 18, 25);
        this.rarm.func_78789_a(-3.0f, -1.0f, -2.0f, 4, 10, 4);
        this.rarm.func_78793_a(-6.0f, 4.0f, 3.0f);
        this.rarm.func_78787_b(64, 32);
        this.rarm.field_78809_i = true;
        this.setRotation(this.rarm, 0.0f, 0.0f, 0.4886922f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        BandP e = (BandP)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        if ((double)f1 > 0.1) {
            newangle = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.25f * f1;
            newangle2 = MathHelper.func_76134_b((float)(f2 * 2.6f * this.wingspeed)) * (float)Math.PI * 0.025f * f1;
            newangle3 = newangle;
        } else {
            newangle = 0.0f;
            newangle2 = MathHelper.func_76134_b((float)(f2 * 0.6f * this.wingspeed)) * (float)Math.PI * 0.005f;
            newangle3 = MathHelper.func_76134_b((float)(f2 * 0.3f * this.wingspeed)) * (float)Math.PI * 0.02f;
        }
        this.lleg.field_78795_f = newangle;
        this.rleg.field_78795_f = -newangle;
        this.belly.field_78795_f = 0.07f + newangle2;
        this.larm.field_78795_f = -newangle3;
        this.rarm.field_78795_f = newangle3;
        this.belly.field_78796_g = -newangle / 2.0f;
        this.head.field_78796_g = (float)Math.toRadians(f3);
        this.head.field_78795_f = (float)Math.toRadians(f4);
        this.belly.func_78785_a(f5);
        this.chest.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.lleg.func_78785_a(f5);
        this.rleg.func_78785_a(f5);
        this.larm.func_78785_a(f5);
        this.rarm.func_78785_a(f5);
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

