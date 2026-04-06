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

import danger.orespawn.WaterDragon;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelWaterDragon
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Head;
    ModelRenderer neck1;
    ModelRenderer body1;
    ModelRenderer Leg8;
    ModelRenderer Leg2;
    ModelRenderer Leg7;
    ModelRenderer Leg1;
    ModelRenderer neck2;
    ModelRenderer neck3;
    ModelRenderer neck4;
    ModelRenderer body2;
    ModelRenderer body3;
    ModelRenderer body4;
    ModelRenderer tail1;
    ModelRenderer tailmiddle;
    ModelRenderer tailtop;
    ModelRenderer tailbottom;
    ModelRenderer nose;
    ModelRenderer headfin;
    ModelRenderer rightear;
    ModelRenderer leftear;
    ModelRenderer neackfin;
    ModelRenderer Bodyfin;
    ModelRenderer jaw;

    public ModelWaterDragon(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 128;
        this.field_78089_u = 128;
        this.Head = new ModelRenderer((ModelBase)this, 79, 64);
        this.Head.func_78789_a(-4.0f, -4.0f, -8.0f, 7, 8, 8);
        this.Head.func_78793_a(0.0f, 0.0f, -3.0f);
        this.Head.func_78787_b(128, 128);
        this.Head.field_78809_i = true;
        this.setRotation(this.Head, 0.0f, 0.0f, 0.0f);
        this.neck1 = new ModelRenderer((ModelBase)this, 29, 70);
        this.neck1.func_78789_a(-2.0f, 0.0f, -3.0f, 5, 5, 5);
        this.neck1.func_78793_a(-1.0f, 4.0f, -5.0f);
        this.neck1.func_78787_b(128, 128);
        this.neck1.field_78809_i = true;
        this.setRotation(this.neck1, -0.1858931f, 0.0f, 0.0f);
        this.body1 = new ModelRenderer((ModelBase)this, 0, 33);
        this.body1.func_78789_a(-5.0f, -4.0f, -6.0f, 9, 9, 9);
        this.body1.func_78793_a(0.0f, 19.0f, 2.0f);
        this.body1.func_78787_b(128, 128);
        this.body1.field_78809_i = true;
        this.setRotation(this.body1, 0.0f, 0.0f, 0.0f);
        this.Leg8 = new ModelRenderer((ModelBase)this, 23, 25);
        this.Leg8.func_78789_a(0.0f, -1.0f, -1.0f, 9, 2, 3);
        this.Leg8.func_78793_a(3.0f, 22.0f, -2.0f);
        this.Leg8.func_78787_b(128, 128);
        this.Leg8.field_78809_i = true;
        this.setRotation(this.Leg8, 0.0f, 0.5759587f, 0.1919862f);
        this.Leg2 = new ModelRenderer((ModelBase)this, 80, 18);
        this.Leg2.func_78789_a(0.0f, -1.0f, -1.0f, 9, 2, 3);
        this.Leg2.func_78793_a(2.0f, 22.0f, 13.0f);
        this.Leg2.func_78787_b(128, 128);
        this.Leg2.field_78809_i = true;
        this.setRotation(this.Leg2, 0.0f, -0.5759587f, 0.1919862f);
        this.Leg7 = new ModelRenderer((ModelBase)this, 23, 18);
        this.Leg7.func_78789_a(-9.0f, -1.0f, -1.0f, 9, 2, 3);
        this.Leg7.func_78793_a(-4.0f, 22.0f, -1.0f);
        this.Leg7.func_78787_b(128, 128);
        this.Leg7.field_78809_i = true;
        this.setRotation(this.Leg7, 0.0f, -0.5759587f, -0.1919862f);
        this.Leg1 = new ModelRenderer((ModelBase)this, 80, 25);
        this.Leg1.func_78789_a(-9.0f, -1.0f, -2.0f, 9, 2, 3);
        this.Leg1.func_78793_a(-3.0f, 22.0f, 14.0f);
        this.Leg1.func_78787_b(128, 128);
        this.Leg1.field_78809_i = true;
        this.setRotation(this.Leg1, 0.0f, 0.5759587f, -0.1919862f);
        this.neck2 = new ModelRenderer((ModelBase)this, 0, 11);
        this.neck2.func_78789_a(-2.0f, 0.0f, -2.0f, 5, 5, 5);
        this.neck2.func_78793_a(-1.0f, 9.0f, -7.0f);
        this.neck2.func_78787_b(128, 128);
        this.neck2.field_78809_i = true;
        this.setRotation(this.neck2, 0.1115358f, 0.0f, 0.0f);
        this.neck3 = new ModelRenderer((ModelBase)this, 0, 22);
        this.neck3.func_78789_a(-2.0f, 0.0f, -2.0f, 5, 5, 5);
        this.neck3.func_78793_a(-1.0f, 14.0f, -6.0f);
        this.neck3.func_78787_b(128, 128);
        this.neck3.field_78809_i = true;
        this.setRotation(this.neck3, 0.4461433f, 0.0f, 0.0f);
        this.neck4 = new ModelRenderer((ModelBase)this, 26, 12);
        this.neck4.func_78789_a(-3.0f, 0.0f, -2.0f, 5, 3, 3);
        this.neck4.func_78793_a(0.0f, 18.0f, -4.0f);
        this.neck4.func_78787_b(128, 128);
        this.neck4.field_78809_i = true;
        this.setRotation(this.neck4, 1.226894f, 0.0f, 0.0f);
        this.body2 = new ModelRenderer((ModelBase)this, 0, 52);
        this.body2.func_78789_a(-5.0f, -5.0f, 0.0f, 7, 7, 9);
        this.body2.func_78793_a(1.0f, 21.0f, 5.0f);
        this.body2.func_78787_b(128, 128);
        this.body2.field_78809_i = true;
        this.setRotation(this.body2, 0.0f, 0.0f, 0.0f);
        this.body3 = new ModelRenderer((ModelBase)this, 0, 69);
        this.body3.func_78789_a(-3.0f, -3.0f, 0.0f, 5, 5, 7);
        this.body3.func_78793_a(0.0f, 20.0f, 14.0f);
        this.body3.func_78787_b(128, 128);
        this.body3.field_78809_i = true;
        this.setRotation(this.body3, 0.0f, 0.0f, 0.0f);
        this.body4 = new ModelRenderer((ModelBase)this, 0, 89);
        this.body4.func_78789_a(-1.0f, -1.0f, 0.0f, 3, 3, 5);
        this.body4.func_78793_a(-1.0f, 19.0f, 21.0f);
        this.body4.func_78787_b(128, 128);
        this.body4.field_78809_i = true;
        this.setRotation(this.body4, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 0, 82);
        this.tail1.func_78789_a(0.0f, 0.0f, 0.0f, 1, 2, 3);
        this.tail1.func_78793_a(-1.0f, 19.0f, 25.0f);
        this.tail1.func_78787_b(128, 128);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.0f, 0.0f, 0.0f);
        this.tailmiddle = new ModelRenderer((ModelBase)this, 55, 37);
        this.tailmiddle.func_78789_a(-1.0f, -6.0f, 0.0f, 2, 11, 9);
        this.tailmiddle.func_78793_a(0.0f, 19.0f, 28.0f);
        this.tailmiddle.func_78787_b(128, 128);
        this.tailmiddle.field_78809_i = true;
        this.setRotation(this.tailmiddle, 0.0f, 0.0f, 0.0f);
        this.tailtop = new ModelRenderer((ModelBase)this, 82, 36);
        this.tailtop.func_78789_a(-1.0f, -11.0f, 0.0f, 2, 11, 9);
        this.tailtop.func_78793_a(0.0f, 14.0f, 28.0f);
        this.tailtop.func_78787_b(128, 128);
        this.tailtop.field_78809_i = true;
        this.setRotation(this.tailtop, -0.6320364f, 0.0f, 0.0f);
        this.tailbottom = new ModelRenderer((ModelBase)this, 56, 60);
        this.tailbottom.func_78789_a(0.0f, 0.0f, 0.0f, 2, 11, 9);
        this.tailbottom.func_78793_a(-1.0f, 23.0f, 28.0f);
        this.tailbottom.func_78787_b(128, 128);
        this.tailbottom.field_78809_i = true;
        this.setRotation(this.tailbottom, 0.6320361f, 0.0f, -0.0174533f);
        this.nose = new ModelRenderer((ModelBase)this, 54, 19);
        this.nose.func_78789_a(-3.0f, -2.0f, -5.0f, 5, 5, 5);
        this.nose.func_78793_a(0.0f, -2.0f, -11.0f);
        this.nose.func_78787_b(128, 128);
        this.nose.field_78809_i = true;
        this.setRotation(this.nose, 0.0f, 0.0f, 0.0f);
        this.headfin = new ModelRenderer((ModelBase)this, 0, 99);
        this.headfin.func_78789_a(0.0f, -5.0f, 0.0f, 0, 10, 9);
        this.headfin.func_78793_a(0.0f, -4.0f, -6.0f);
        this.headfin.func_78787_b(128, 128);
        this.headfin.field_78809_i = true;
        this.setRotation(this.headfin, 0.1396263f, 0.0f, 0.0f);
        this.rightear = new ModelRenderer((ModelBase)this, 38, 32);
        this.rightear.func_78789_a(0.0f, 0.0f, 0.0f, 0, 5, 5);
        this.rightear.func_78793_a(-4.0f, -2.0f, -5.0f);
        this.rightear.func_78787_b(128, 128);
        this.rightear.field_78809_i = true;
        this.setRotation(this.rightear, 0.0698132f, -0.418879f, 0.0f);
        this.leftear = new ModelRenderer((ModelBase)this, 38, 32);
        this.leftear.func_78789_a(0.0f, 0.0f, 0.0f, 0, 5, 5);
        this.leftear.func_78793_a(3.0f, -2.0f, -5.0f);
        this.leftear.func_78787_b(128, 128);
        this.leftear.field_78809_i = true;
        this.setRotation(this.leftear, 0.0698132f, 0.418879f, 0.0f);
        this.neackfin = new ModelRenderer((ModelBase)this, 42, 47);
        this.neackfin.func_78789_a(0.0f, -1.0f, 0.0f, 0, 5, 5);
        this.neackfin.func_78793_a(0.0f, 3.0f, -3.0f);
        this.neackfin.func_78787_b(128, 128);
        this.neackfin.field_78809_i = true;
        this.setRotation(this.neackfin, -0.185895f, 0.0f, 0.0f);
        this.Bodyfin = new ModelRenderer((ModelBase)this, 21, 91);
        this.Bodyfin.func_78789_a(0.0f, -6.0f, -3.0f, 0, 10, 9);
        this.Bodyfin.func_78793_a(0.0f, 15.0f, 2.0f);
        this.Bodyfin.func_78787_b(128, 128);
        this.Bodyfin.field_78809_i = true;
        this.setRotation(this.Bodyfin, -0.0698132f, 0.0f, 0.0f);
        this.jaw = new ModelRenderer((ModelBase)this, 76, 8);
        this.jaw.func_78789_a(-2.0f, 0.0f, -5.0f, 5, 1, 5);
        this.jaw.func_78793_a(-1.0f, 3.0f, -10.0f);
        this.jaw.func_78787_b(128, 128);
        this.jaw.field_78809_i = true;
        this.setRotation(this.jaw, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        WaterDragon e = (WaterDragon)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        float pi4 = 0.7853982f;
        float root13 = (float)Math.sqrt(13.0);
        float root20 = (float)Math.sqrt(20.0);
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.2f * f1 : 0.0f;
        this.body3.field_78796_g = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.4f * f1;
        this.body4.field_78798_e = this.body3.field_78798_e + (float)Math.cos(this.body3.field_78796_g) * 7.0f;
        this.body4.field_78800_c = this.body3.field_78800_c - 1.0f + (float)Math.sin(this.body3.field_78796_g) * 7.0f;
        this.body4.field_78796_g = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed - pi4)) * (float)Math.PI * 0.4f * f1;
        this.tail1.field_78798_e = this.body4.field_78798_e + (float)Math.cos(this.body4.field_78796_g) * 5.0f;
        this.tail1.field_78800_c = this.body4.field_78800_c + (float)Math.sin(this.body4.field_78796_g) * 5.0f;
        this.tail1.field_78796_g = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed - 2.0f * pi4)) * (float)Math.PI * 0.4f * f1;
        this.tailmiddle.field_78798_e = this.tail1.field_78798_e + (float)Math.cos(this.tail1.field_78796_g) * 3.0f;
        this.tailmiddle.field_78800_c = this.tail1.field_78800_c + (float)Math.sin(this.tail1.field_78796_g) * 3.0f;
        this.tailtop.field_78796_g = this.tailmiddle.field_78796_g = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed - 3.0f * pi4)) * (float)Math.PI * 0.4f * f1;
        this.tailtop.field_78798_e = this.tailmiddle.field_78798_e;
        this.tailtop.field_78800_c = this.tailmiddle.field_78800_c;
        this.tailbottom.field_78796_g = this.tailmiddle.field_78796_g;
        this.tailbottom.field_78798_e = this.tailmiddle.field_78798_e;
        this.tailbottom.field_78800_c = this.tailmiddle.field_78800_c;
        this.Leg8.field_78796_g = 0.58f + newangle;
        this.Leg2.field_78796_g = -0.58f + newangle;
        this.Leg7.field_78796_g = -0.58f - newangle;
        this.Leg1.field_78796_g = 0.58f - newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.8f * this.wingspeed)) * (float)Math.PI * 0.1f;
        this.leftear.field_78796_g = 0.62f + newangle;
        this.rightear.field_78796_g = -0.62f - newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.7f * this.wingspeed)) * (float)Math.PI * 0.02f;
        if (e.func_70906_o()) {
            newangle = 0.0f;
        }
        this.Bodyfin.field_78808_h = newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.6f * this.wingspeed)) * (float)Math.PI * 0.1f;
        if (e.func_70906_o()) {
            newangle = 0.0f;
        }
        this.neackfin.field_78796_g = newangle;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.5f * this.wingspeed)) * (float)Math.PI * 0.05f;
        if (e.func_70906_o()) {
            newangle = 0.0f;
        }
        this.headfin.field_78796_g = newangle;
        this.jaw.field_78795_f = e.getAttacking() == 1 ? (newangle = MathHelper.func_76134_b((float)(f2 * 1.2f * this.wingspeed)) * (float)Math.PI * 0.25f) : (e.getAttacking() == 2 ? 0.45f : -0.25f);
        this.Head.field_78796_g = newangle = (float)Math.toRadians(f3) * 0.75f;
        this.nose.field_78796_g = newangle;
        this.nose.field_78798_e = this.Head.field_78798_e - (float)Math.cos(this.Head.field_78796_g) * 8.0f;
        this.nose.field_78800_c = this.Head.field_78800_c - (float)Math.sin(this.Head.field_78796_g) * 8.0f;
        this.jaw.field_78796_g = newangle;
        this.jaw.field_78798_e = this.Head.field_78798_e - (float)Math.cos(this.Head.field_78796_g) * 7.0f;
        this.jaw.field_78800_c = this.Head.field_78800_c - (float)Math.sin(this.Head.field_78796_g) * 7.0f - 1.0f;
        this.headfin.field_78796_g = newangle;
        this.headfin.field_78798_e = this.Head.field_78798_e - (float)Math.cos(this.Head.field_78796_g) * 3.0f;
        this.headfin.field_78800_c = this.Head.field_78800_c - (float)Math.sin(this.Head.field_78796_g) * 3.0f;
        this.leftear.field_78796_g += newangle;
        this.leftear.field_78798_e = this.Head.field_78798_e - (float)Math.cos(this.Head.field_78796_g - pi4) * root13;
        this.leftear.field_78800_c = this.Head.field_78800_c - (float)Math.sin(this.Head.field_78796_g - pi4) * root13;
        this.rightear.field_78796_g += newangle;
        this.rightear.field_78798_e = this.Head.field_78798_e - (float)Math.cos(this.Head.field_78796_g + pi4) * root20;
        this.rightear.field_78800_c = this.Head.field_78800_c - (float)Math.sin(this.Head.field_78796_g + pi4) * root20;
        this.Head.func_78785_a(f5);
        this.neck1.func_78785_a(f5);
        this.body1.func_78785_a(f5);
        this.Leg8.func_78785_a(f5);
        this.Leg2.func_78785_a(f5);
        this.Leg7.func_78785_a(f5);
        this.Leg1.func_78785_a(f5);
        this.neck2.func_78785_a(f5);
        this.neck3.func_78785_a(f5);
        this.neck4.func_78785_a(f5);
        this.body2.func_78785_a(f5);
        this.body3.func_78785_a(f5);
        this.body4.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.tailmiddle.func_78785_a(f5);
        this.tailtop.func_78785_a(f5);
        this.tailbottom.func_78785_a(f5);
        this.nose.func_78785_a(f5);
        this.headfin.func_78785_a(f5);
        this.rightear.func_78785_a(f5);
        this.leftear.func_78785_a(f5);
        this.neackfin.func_78785_a(f5);
        this.Bodyfin.func_78785_a(f5);
        this.jaw.func_78785_a(f5);
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

