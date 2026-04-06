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

import danger.orespawn.Bee;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBee
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Sting;
    ModelRenderer Abdomnem1;
    ModelRenderer Abdomnem2;
    ModelRenderer Abdomnem3;
    ModelRenderer Abdomnem4;
    ModelRenderer Abdomnem5;
    ModelRenderer MainBody;
    ModelRenderer Neck;
    ModelRenderer Head;
    ModelRenderer WingRight;
    ModelRenderer WingLeft;
    ModelRenderer RA1;
    ModelRenderer LA1;
    ModelRenderer LA2;
    ModelRenderer RA2;
    ModelRenderer RA3;
    ModelRenderer LA3;
    ModelRenderer LeftPom;
    ModelRenderer RightPom;
    ModelRenderer LeftPincerExtra;
    ModelRenderer LeftPincerMain;
    ModelRenderer RightPincerMain;
    ModelRenderer RightPincerExtra;

    public ModelBee(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 256;
        this.field_78089_u = 256;
        this.Sting = new ModelRenderer((ModelBase)this, 68, 0);
        this.Sting.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 10, 2);
        this.Sting.func_78793_a(0.0f, 16.0f, 1.0f);
        this.Sting.func_78787_b(256, 256);
        this.Sting.field_78809_i = true;
        this.setRotation(this.Sting, -0.7853982f, 0.0f, 0.0f);
        this.Abdomnem1 = new ModelRenderer((ModelBase)this, 64, 12);
        this.Abdomnem1.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 8, 4);
        this.Abdomnem1.func_78793_a(0.0f, 9.0f, 2.0f);
        this.Abdomnem1.func_78787_b(256, 256);
        this.Abdomnem1.field_78809_i = true;
        this.setRotation(this.Abdomnem1, -0.5235988f, 0.0f, 0.0f);
        this.Abdomnem2 = new ModelRenderer((ModelBase)this, 60, 24);
        this.Abdomnem2.func_78789_a(-3.0f, 0.0f, 0.0f, 6, 6, 6);
        this.Abdomnem2.func_78793_a(0.0f, 5.0f, 0.0f);
        this.Abdomnem2.func_78787_b(256, 256);
        this.Abdomnem2.field_78809_i = true;
        this.setRotation(this.Abdomnem2, 0.0f, 0.0f, 0.0f);
        this.Abdomnem3 = new ModelRenderer((ModelBase)this, 56, 36);
        this.Abdomnem3.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 7, 8);
        this.Abdomnem3.func_78793_a(0.0f, 1.0f, -2.0f);
        this.Abdomnem3.func_78787_b(256, 256);
        this.Abdomnem3.field_78809_i = true;
        this.setRotation(this.Abdomnem3, 0.2617994f, 0.0f, 0.0f);
        this.Abdomnem4 = new ModelRenderer((ModelBase)this, 53, 51);
        this.Abdomnem4.func_78789_a(-5.0f, 0.0f, 0.0f, 10, 12, 10);
        this.Abdomnem4.func_78793_a(0.0f, -6.0f, -8.0f);
        this.Abdomnem4.func_78787_b(256, 256);
        this.Abdomnem4.field_78809_i = true;
        this.setRotation(this.Abdomnem4, 0.5934119f, 0.0f, 0.0f);
        this.Abdomnem5 = new ModelRenderer((ModelBase)this, 48, 73);
        this.Abdomnem5.func_78789_a(-6.0f, 0.0f, 0.0f, 12, 12, 12);
        this.Abdomnem5.func_78793_a(0.0f, -6.0f, -15.0f);
        this.Abdomnem5.func_78787_b(256, 256);
        this.Abdomnem5.field_78809_i = true;
        this.setRotation(this.Abdomnem5, 1.099557f, 0.0f, 0.0f);
        this.MainBody = new ModelRenderer((ModelBase)this, 48, 97);
        this.MainBody.func_78789_a(-6.0f, 0.0f, -6.0f, 12, 14, 12);
        this.MainBody.func_78793_a(0.0f, -12.0f, -24.0f);
        this.MainBody.func_78787_b(256, 256);
        this.MainBody.field_78809_i = true;
        this.setRotation(this.MainBody, 1.48353f, 0.0f, 0.0f);
        this.Neck = new ModelRenderer((ModelBase)this, 55, 123);
        this.Neck.func_78789_a(-4.0f, -4.0f, -8.0f, 8, 8, 8);
        this.Neck.func_78793_a(0.0f, -12.0f, -23.0f);
        this.Neck.func_78787_b(256, 256);
        this.Neck.field_78809_i = true;
        this.setRotation(this.Neck, 0.0f, 0.0f, 0.0f);
        this.Head = new ModelRenderer((ModelBase)this, 51, 139);
        this.Head.func_78789_a(-5.0f, -5.0f, -10.0f, 10, 10, 10);
        this.Head.func_78793_a(0.0f, -13.0f, -28.0f);
        this.Head.func_78787_b(256, 256);
        this.Head.field_78809_i = true;
        this.setRotation(this.Head, 0.2617994f, 0.0f, 0.0f);
        this.WingRight = new ModelRenderer((ModelBase)this, 0, 91);
        this.WingRight.func_78789_a(0.0f, 0.0f, 0.0f, 0, 8, 24);
        this.WingRight.func_78793_a(-4.0f, -14.0f, -15.0f);
        this.WingRight.func_78787_b(256, 256);
        this.WingRight.field_78809_i = true;
        this.setRotation(this.WingRight, -0.7853982f, -0.5235988f, 2.617994f);
        this.WingLeft = new ModelRenderer((ModelBase)this, 96, 91);
        this.WingLeft.func_78789_a(0.0f, 0.0f, 0.0f, 0, 8, 24);
        this.WingLeft.func_78793_a(3.0f, -14.0f, -15.0f);
        this.WingLeft.func_78787_b(256, 256);
        this.WingLeft.field_78809_i = true;
        this.setRotation(this.WingLeft, -0.7853982f, 0.5235988f, -2.617994f);
        this.RA1 = new ModelRenderer((ModelBase)this, 47, 152);
        this.RA1.func_78789_a(0.0f, -6.0f, -1.0f, 1, 6, 1);
        this.RA1.func_78793_a(-3.0f, -17.0f, -31.0f);
        this.RA1.func_78787_b(256, 256);
        this.RA1.field_78809_i = true;
        this.setRotation(this.RA1, 0.2617994f, 0.5235988f, 0.0f);
        this.LA1 = new ModelRenderer((ModelBase)this, 91, 152);
        this.LA1.func_78789_a(0.0f, -6.0f, -1.0f, 1, 6, 1);
        this.LA1.func_78793_a(2.0f, -17.0f, -32.0f);
        this.LA1.func_78787_b(256, 256);
        this.LA1.field_78809_i = true;
        this.setRotation(this.LA1, 0.2617994f, -0.5235988f, 0.0f);
        this.LA2 = new ModelRenderer((ModelBase)this, 91, 145);
        this.LA2.func_78789_a(0.0f, -11.0f, 0.0f, 1, 6, 1);
        this.LA2.func_78793_a(2.0f, -17.0f, -32.0f);
        this.LA2.func_78787_b(256, 256);
        this.LA2.field_78809_i = true;
        this.setRotation(this.LA2, 0.4363323f, -0.6108652f, 0.0f);
        this.RA2 = new ModelRenderer((ModelBase)this, 47, 145);
        this.RA2.func_78789_a(0.0f, -11.0f, 0.0f, 1, 6, 1);
        this.RA2.func_78793_a(-3.0f, -17.0f, -31.0f);
        this.RA2.func_78787_b(256, 256);
        this.RA2.field_78809_i = true;
        this.setRotation(this.RA2, 0.4363323f, 0.6108652f, 0.0f);
        this.RA3 = new ModelRenderer((ModelBase)this, 47, 138);
        this.RA3.func_78789_a(0.0f, -16.0f, 2.0f, 1, 6, 1);
        this.RA3.func_78793_a(-3.0f, -17.0f, -31.0f);
        this.RA3.func_78787_b(256, 256);
        this.RA3.field_78809_i = true;
        this.setRotation(this.RA3, 0.6108652f, 0.6981317f, 0.0f);
        this.LA3 = new ModelRenderer((ModelBase)this, 91, 138);
        this.LA3.func_78789_a(0.0f, -16.0f, 2.0f, 1, 6, 1);
        this.LA3.func_78793_a(2.0f, -17.0f, -32.0f);
        this.LA3.func_78787_b(256, 256);
        this.LA3.field_78809_i = true;
        this.setRotation(this.LA3, 0.6108652f, -0.6981317f, 0.0f);
        this.LeftPom = new ModelRenderer((ModelBase)this, 89, 134);
        this.LeftPom.func_78789_a(4.0f, -16.0f, -6.0f, 2, 2, 2);
        this.LeftPom.func_78793_a(2.0f, -17.0f, -32.0f);
        this.LeftPom.func_78787_b(256, 256);
        this.LeftPom.field_78809_i = true;
        this.setRotation(this.LeftPom, 0.0f, 0.0f, 0.0f);
        this.RightPom = new ModelRenderer((ModelBase)this, 45, 134);
        this.RightPom.func_78789_a(-5.0f, -16.0f, -7.0f, 2, 2, 2);
        this.RightPom.func_78793_a(-3.0f, -17.0f, -31.0f);
        this.RightPom.func_78787_b(256, 256);
        this.RightPom.field_78809_i = true;
        this.setRotation(this.RightPom, 0.0f, 0.0f, 0.0f);
        this.LeftPincerExtra = new ModelRenderer((ModelBase)this, 71, 166);
        this.LeftPincerExtra.func_78789_a(-2.0f, 0.0f, -6.0f, 2, 1, 2);
        this.LeftPincerExtra.func_78793_a(2.0f, -8.0f, -36.0f);
        this.LeftPincerExtra.func_78787_b(256, 256);
        this.LeftPincerExtra.field_78809_i = true;
        this.setRotation(this.LeftPincerExtra, 0.1745329f, -0.1745329f, 0.0f);
        this.LeftPincerMain = new ModelRenderer((ModelBase)this, 71, 159);
        this.LeftPincerMain.func_78789_a(0.0f, 0.0f, -6.0f, 2, 1, 6);
        this.LeftPincerMain.func_78793_a(2.0f, -8.0f, -36.0f);
        this.LeftPincerMain.func_78787_b(256, 256);
        this.LeftPincerMain.field_78809_i = true;
        this.setRotation(this.LeftPincerMain, 0.1745329f, -0.1745329f, 0.0f);
        this.RightPincerMain = new ModelRenderer((ModelBase)this, 55, 159);
        this.RightPincerMain.func_78789_a(0.0f, 0.0f, -6.0f, 2, 1, 6);
        this.RightPincerMain.func_78793_a(-4.0f, -8.0f, -36.0f);
        this.RightPincerMain.func_78787_b(256, 256);
        this.RightPincerMain.field_78809_i = true;
        this.setRotation(this.RightPincerMain, 0.1745329f, 0.1745329f, 0.0f);
        this.RightPincerExtra = new ModelRenderer((ModelBase)this, 63, 166);
        this.RightPincerExtra.func_78789_a(2.0f, 0.0f, -6.0f, 2, 1, 2);
        this.RightPincerExtra.func_78793_a(-4.0f, -8.0f, -36.0f);
        this.RightPincerExtra.func_78787_b(256, 256);
        this.RightPincerExtra.field_78809_i = true;
        this.setRotation(this.RightPincerExtra, 0.1745329f, 0.1745329f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle = 0.0f;
        Bee b = (Bee)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = MathHelper.func_76134_b((float)(f2 * 1.1f * this.wingspeed)) * (float)Math.PI * 0.3f;
        this.WingLeft.field_78808_h = -1.745f - newangle;
        this.WingRight.field_78808_h = 1.754f + newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.3f * this.wingspeed)) * (float)Math.PI * 0.1f;
        this.LeftPincerMain.field_78796_g = -0.274f + newangle;
        this.LeftPincerExtra.field_78796_g = -0.274f + newangle;
        this.RightPincerMain.field_78796_g = 0.274f - newangle;
        this.RightPincerExtra.field_78796_g = 0.274f - newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.21f * this.wingspeed)) * (float)Math.PI * 0.06f;
        this.LA1.field_78795_f = 0.261f + newangle;
        this.LA2.field_78795_f = 0.436f + newangle;
        this.LA3.field_78795_f = 0.611f + newangle;
        this.LeftPom.field_78795_f = newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.27f * this.wingspeed)) * (float)Math.PI * 0.06f;
        this.RA1.field_78795_f = 0.261f + newangle;
        this.RA2.field_78795_f = 0.436f + newangle;
        this.RA3.field_78795_f = 0.611f + newangle;
        this.RightPom.field_78795_f = newangle;
        this.LA1.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.31f * this.wingspeed)) * (float)Math.PI * 0.06f;
        this.LA2.field_78808_h = newangle;
        this.LA3.field_78808_h = newangle;
        this.LeftPom.field_78808_h = newangle;
        this.RA1.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.37f * this.wingspeed)) * (float)Math.PI * 0.06f;
        this.RA2.field_78808_h = newangle;
        this.RA3.field_78808_h = newangle;
        this.RightPom.field_78808_h = newangle;
        newangle = b.getAttacking() == 0 ? MathHelper.func_76134_b((float)(f2 * 0.021f * this.wingspeed)) * (float)Math.PI * 0.023f : MathHelper.func_76134_b((float)(f2 * 0.11f * this.wingspeed)) * (float)Math.PI * 0.055f;
        this.Abdomnem5.field_78795_f = 1.099f + newangle;
        this.Abdomnem4.field_78795_f = this.Abdomnem5.field_78795_f + newangle - 0.35f;
        this.Abdomnem4.field_78797_d = (float)((double)this.Abdomnem5.field_78797_d + Math.cos(this.Abdomnem5.field_78795_f) * 10.0);
        this.Abdomnem4.field_78798_e = (float)((double)this.Abdomnem5.field_78798_e + Math.sin(this.Abdomnem5.field_78795_f) * 10.0);
        this.Abdomnem3.field_78795_f = this.Abdomnem4.field_78795_f + newangle - 0.35f;
        this.Abdomnem3.field_78797_d = (float)((double)this.Abdomnem4.field_78797_d + Math.cos(this.Abdomnem4.field_78795_f) * 10.0);
        this.Abdomnem3.field_78798_e = (float)((double)this.Abdomnem4.field_78798_e + Math.sin(this.Abdomnem4.field_78795_f) * 10.0);
        this.Abdomnem2.field_78795_f = this.Abdomnem3.field_78795_f + newangle - 0.35f;
        this.Abdomnem2.field_78797_d = (float)((double)this.Abdomnem3.field_78797_d + Math.cos(this.Abdomnem3.field_78795_f) * 6.0);
        this.Abdomnem2.field_78798_e = (float)((double)this.Abdomnem3.field_78798_e + Math.sin(this.Abdomnem3.field_78795_f) * 6.0);
        this.Abdomnem1.field_78795_f = this.Abdomnem2.field_78795_f + newangle - 0.35f;
        this.Abdomnem1.field_78797_d = (float)((double)this.Abdomnem2.field_78797_d + Math.cos(this.Abdomnem2.field_78795_f) * 5.0);
        this.Abdomnem1.field_78798_e = (float)((double)this.Abdomnem2.field_78798_e + Math.sin(this.Abdomnem2.field_78795_f) * 5.0);
        this.Sting.field_78795_f = this.Abdomnem1.field_78795_f + newangle - 0.35f;
        this.Sting.field_78797_d = (float)((double)this.Abdomnem1.field_78797_d + Math.cos(this.Abdomnem1.field_78795_f) * 7.0);
        this.Sting.field_78798_e = 1.0f + (float)((double)this.Abdomnem1.field_78798_e + Math.sin(this.Abdomnem1.field_78795_f) * 7.0);
        this.Sting.func_78785_a(f5);
        this.Abdomnem1.func_78785_a(f5);
        this.Abdomnem2.func_78785_a(f5);
        this.Abdomnem3.func_78785_a(f5);
        this.Abdomnem4.func_78785_a(f5);
        this.Abdomnem5.func_78785_a(f5);
        this.MainBody.func_78785_a(f5);
        this.Neck.func_78785_a(f5);
        this.Head.func_78785_a(f5);
        this.WingRight.func_78785_a(f5);
        this.WingLeft.func_78785_a(f5);
        this.RA1.func_78785_a(f5);
        this.LA1.func_78785_a(f5);
        this.LA2.func_78785_a(f5);
        this.RA2.func_78785_a(f5);
        this.RA3.func_78785_a(f5);
        this.LA3.func_78785_a(f5);
        this.LeftPom.func_78785_a(f5);
        this.RightPom.func_78785_a(f5);
        this.LeftPincerExtra.func_78785_a(f5);
        this.LeftPincerMain.func_78785_a(f5);
        this.RightPincerMain.func_78785_a(f5);
        this.RightPincerExtra.func_78785_a(f5);
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

