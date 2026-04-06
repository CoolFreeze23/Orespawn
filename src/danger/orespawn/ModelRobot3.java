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

import danger.orespawn.RenderInfo;
import danger.orespawn.Robot3;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelRobot3
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer rleg1;
    ModelRenderer lleg1;
    ModelRenderer rleg2;
    ModelRenderer lleg2;
    ModelRenderer hips;
    ModelRenderer waist1;
    ModelRenderer waist2;
    ModelRenderer body3;
    ModelRenderer lazer;
    ModelRenderer body2;
    ModelRenderer body1;
    ModelRenderer body4;
    ModelRenderer waist3;
    ModelRenderer larm3;
    ModelRenderer rarm3;
    ModelRenderer larm2;
    ModelRenderer rarm2;
    ModelRenderer larm1;
    ModelRenderer rarm1;

    public ModelRobot3(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 512;
        this.field_78089_u = 512;
        this.rleg1 = new ModelRenderer((ModelBase)this, 20, 100);
        this.rleg1.func_78789_a(-23.0f, 26.0f, -8.0f, 16, 29, 16);
        this.rleg1.func_78793_a(-9.0f, -31.0f, 0.0f);
        this.rleg1.func_78787_b(512, 512);
        this.rleg1.field_78809_i = true;
        this.setRotation(this.rleg1, 0.0f, 0.0f, 0.0f);
        this.lleg1 = new ModelRenderer((ModelBase)this, 20, 159);
        this.lleg1.func_78789_a(7.0f, 25.0f, -8.0f, 16, 29, 16);
        this.lleg1.func_78793_a(9.0f, -30.0f, 0.0f);
        this.lleg1.func_78787_b(512, 512);
        this.lleg1.field_78809_i = true;
        this.setRotation(this.lleg1, 0.0f, 0.0f, 0.0f);
        this.rleg2 = new ModelRenderer((ModelBase)this, 20, 212);
        this.rleg2.func_78789_a(-14.0f, 0.0f, -7.0f, 14, 29, 14);
        this.rleg2.func_78793_a(-9.0f, -31.0f, 0.0f);
        this.rleg2.func_78787_b(512, 512);
        this.rleg2.field_78809_i = true;
        this.setRotation(this.rleg2, 0.0f, 0.0f, 0.2792527f);
        this.lleg2 = new ModelRenderer((ModelBase)this, 20, 265);
        this.lleg2.func_78789_a(0.0f, 0.0f, -7.0f, 13, 29, 14);
        this.lleg2.func_78793_a(9.0f, -31.0f, 0.0f);
        this.lleg2.func_78787_b(512, 512);
        this.lleg2.field_78809_i = true;
        this.setRotation(this.lleg2, 0.0f, 0.0f, -0.2792527f);
        this.hips = new ModelRenderer((ModelBase)this, 20, 316);
        this.hips.func_78789_a(0.0f, 0.0f, 0.0f, 18, 16, 16);
        this.hips.func_78793_a(-9.0f, -43.0f, -8.0f);
        this.hips.func_78787_b(512, 512);
        this.hips.field_78809_i = true;
        this.setRotation(this.hips, 0.0f, 0.0f, 0.0f);
        this.waist1 = new ModelRenderer((ModelBase)this, 20, 359);
        this.waist1.func_78789_a(0.0f, 0.0f, 0.0f, 12, 12, 12);
        this.waist1.func_78793_a(-6.0f, -55.0f, -4.0f);
        this.waist1.func_78787_b(512, 512);
        this.waist1.field_78809_i = true;
        this.setRotation(this.waist1, -0.1f, 0.0f, 0.0f);
        this.waist2 = new ModelRenderer((ModelBase)this, 20, 391);
        this.waist2.func_78789_a(0.0f, 0.0f, 0.0f, 12, 12, 12);
        this.waist2.func_78793_a(-6.0f, -67.0f, -4.0f);
        this.waist2.func_78787_b(512, 512);
        this.waist2.field_78809_i = true;
        this.setRotation(this.waist2, 0.0f, 0.0f, 0.0f);
        this.body3 = new ModelRenderer((ModelBase)this, 20, 426);
        this.body3.func_78789_a(-23.0f, -25.0f, 10.0f, 47, 47, 25);
        this.body3.func_78793_a(0.0f, -88.0f, -10.0f);
        this.body3.func_78787_b(512, 512);
        this.body3.field_78809_i = true;
        this.setRotation(this.body3, 0.2f, 0.0f, 0.0f);
        this.lazer = new ModelRenderer((ModelBase)this, 20, 50);
        this.lazer.func_78789_a(-8.0f, -8.0f, -22.0f, 17, 16, 22);
        this.lazer.func_78793_a(0.0f, -88.0f, -11.0f);
        this.lazer.func_78787_b(512, 512);
        this.lazer.field_78809_i = true;
        this.setRotation(this.lazer, 0.4f, 0.0f, 0.0f);
        this.body2 = new ModelRenderer((ModelBase)this, 101, 103);
        this.body2.func_78789_a(9.0f, -24.0f, -12.0f, 15, 47, 47);
        this.body2.func_78793_a(0.0f, -88.0f, -11.0f);
        this.body2.func_78787_b(512, 512);
        this.body2.field_78809_i = true;
        this.setRotation(this.body2, 0.2f, 0.0f, 0.0f);
        this.body1 = new ModelRenderer((ModelBase)this, 101, 210);
        this.body1.func_78789_a(-23.0f, -24.0f, -12.0f, 15, 47, 47);
        this.body1.func_78793_a(0.0f, -88.0f, -11.0f);
        this.body1.func_78787_b(512, 512);
        this.body1.field_78809_i = true;
        this.setRotation(this.body1, 0.2f, 0.0f, 0.0f);
        this.body4 = new ModelRenderer((ModelBase)this, 101, 321);
        this.body4.func_78789_a(-8.0f, -24.0f, -12.0f, 18, 16, 22);
        this.body4.func_78793_a(0.0f, -88.0f, -11.0f);
        this.body4.func_78787_b(512, 512);
        this.body4.field_78809_i = true;
        this.setRotation(this.body4, 0.2f, 0.0f, 0.0f);
        this.waist3 = new ModelRenderer((ModelBase)this, 99, 375);
        this.waist3.func_78789_a(0.0f, 0.0f, -1.0f, 12, 17, 12);
        this.waist3.func_78793_a(-6.0f, -83.0f, -6.0f);
        this.waist3.func_78787_b(512, 512);
        this.waist3.field_78809_i = true;
        this.setRotation(this.waist3, 0.2f, 0.0f, 0.0f);
        this.larm3 = new ModelRenderer((ModelBase)this, 121, 54);
        this.larm3.func_78789_a(0.0f, -10.0f, -9.0f, 20, 18, 18);
        this.larm3.func_78793_a(24.0f, -92.0f, 2.0f);
        this.larm3.func_78787_b(512, 512);
        this.larm3.field_78809_i = true;
        this.setRotation(this.larm3, 1.0f, 0.0f, 0.0f);
        this.rarm3 = new ModelRenderer((ModelBase)this, 26, 8);
        this.rarm3.func_78789_a(-20.0f, -9.0f, -9.0f, 20, 18, 18);
        this.rarm3.func_78793_a(-23.0f, -92.0f, 2.0f);
        this.rarm3.func_78787_b(512, 512);
        this.rarm3.field_78809_i = true;
        this.setRotation(this.rarm3, 1.0f, 0.0f, 0.0f);
        this.larm2 = new ModelRenderer((ModelBase)this, 207, 47);
        this.larm2.func_78789_a(3.0f, 8.0f, -7.0f, 14, 29, 14);
        this.larm2.func_78793_a(24.0f, -92.0f, 2.0f);
        this.larm2.func_78787_b(512, 512);
        this.larm2.field_78809_i = true;
        this.setRotation(this.larm2, 1.0f, 0.0f, 0.0f);
        this.rarm2 = new ModelRenderer((ModelBase)this, 161, 372);
        this.rarm2.func_78789_a(-17.0f, 9.0f, -7.0f, 14, 29, 14);
        this.rarm2.func_78793_a(-23.0f, -92.0f, 2.0f);
        this.rarm2.func_78787_b(512, 512);
        this.rarm2.field_78809_i = true;
        this.setRotation(this.rarm2, 1.0f, 0.0f, 0.0f);
        this.larm1 = new ModelRenderer((ModelBase)this, 185, 433);
        this.larm1.func_78789_a(0.0f, -12.0f, 30.0f, 14, 37, 14);
        this.larm1.func_78793_a(27.0f, -92.0f, 2.0f);
        this.larm1.func_78787_b(512, 512);
        this.larm1.field_78809_i = true;
        this.setRotation(this.larm1, -1.0f, 0.0f, 0.0f);
        this.rarm1 = new ModelRenderer((ModelBase)this, 239, 105);
        this.rarm1.func_78789_a(-17.0f, -12.0f, 30.0f, 14, 37, 14);
        this.rarm1.func_78793_a(-23.0f, -92.0f, 2.0f);
        this.rarm1.func_78787_b(512, 512);
        this.rarm1.field_78809_i = true;
        this.setRotation(this.rarm1, -1.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Robot3 e = (Robot3)entity;
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 0.55f * this.wingspeed)) * (float)Math.PI * 0.12f * f1 : 0.0f;
        this.lleg1.field_78795_f = newangle;
        this.lleg2.field_78795_f = newangle;
        this.rleg1.field_78795_f = -newangle;
        this.rleg2.field_78795_f = -newangle;
        this.lazer.field_78796_g = (float)Math.toRadians((double)f3 / 2.0);
        r = e.getRenderInfo();
        newangle = MathHelper.func_76134_b((float)(f2 * 1.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        nextangle = MathHelper.func_76134_b((float)((f2 + 0.3f) * 1.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (e.getAttacking() != 0) {
                r.ri1 = 1;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        this.rarm1.field_78795_f = newangle - 1.0f;
        this.rarm2.field_78795_f = newangle + 1.0f;
        this.rarm3.field_78795_f = newangle + 1.0f;
        this.larm1.field_78795_f = newangle - 1.0f;
        this.larm2.field_78795_f = newangle + 1.0f;
        this.larm3.field_78795_f = newangle + 1.0f;
        e.setRenderInfo(r);
        this.rleg1.func_78785_a(f5);
        this.lleg1.func_78785_a(f5);
        this.rleg2.func_78785_a(f5);
        this.lleg2.func_78785_a(f5);
        this.hips.func_78785_a(f5);
        this.waist1.func_78785_a(f5);
        this.waist2.func_78785_a(f5);
        this.body3.func_78785_a(f5);
        this.lazer.func_78785_a(f5);
        this.body2.func_78785_a(f5);
        this.body1.func_78785_a(f5);
        this.body4.func_78785_a(f5);
        this.waist3.func_78785_a(f5);
        this.larm3.func_78785_a(f5);
        this.rarm3.func_78785_a(f5);
        this.larm2.func_78785_a(f5);
        this.rarm2.func_78785_a(f5);
        this.larm1.func_78785_a(f5);
        this.rarm1.func_78785_a(f5);
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

