/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 */
package danger.orespawn;

import danger.orespawn.GiantRobot;
import danger.orespawn.RenderGiantRobotInfo;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelGiantRobot
extends ModelBase {
    private float wingspeed = 1.0f;
    private float hipy = 0.0f;
    ModelRenderer Hip;
    ModelRenderer Thigh;
    ModelRenderer Shin;
    ModelRenderer Foot1;
    ModelRenderer Foot2;
    ModelRenderer Foot3;
    ModelRenderer Thigh2;
    ModelRenderer Thigh3;
    ModelRenderer Back1;
    ModelRenderer Back2;
    ModelRenderer Back3;
    ModelRenderer Shoulders;
    ModelRenderer Neck;
    ModelRenderer Head;
    ModelRenderer Arm1;
    ModelRenderer Arm2;
    ModelRenderer Arm3;
    ModelRenderer Knuckles;

    public ModelGiantRobot(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 256;
        this.field_78089_u = 512;
        this.Hip = new ModelRenderer((ModelBase)this, 0, 0);
        this.Hip.func_78789_a(-4.0f, -4.0f, -15.0f, 8, 8, 30);
        this.Hip.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Hip.func_78787_b(64, 32);
        this.Hip.field_78809_i = true;
        this.setRotation(this.Hip, 0.0f, 0.0f, 0.0f);
        this.Thigh = new ModelRenderer((ModelBase)this, 0, 115);
        this.Thigh.func_78789_a(-3.0f, -3.0f, -3.0f, 6, 43, 6);
        this.Thigh.func_78793_a(0.0f, -58.0f, 0.0f);
        this.Thigh.func_78787_b(64, 32);
        this.Thigh.field_78809_i = true;
        this.setRotation(this.Thigh, 0.0f, 0.0f, 0.0f);
        this.Shin = new ModelRenderer((ModelBase)this, 0, 167);
        this.Shin.func_78789_a(-3.0f, -3.0f, -3.0f, 6, 43, 6);
        this.Shin.func_78793_a(0.0f, -18.0f, 0.0f);
        this.Shin.func_78787_b(64, 32);
        this.Shin.field_78809_i = true;
        this.setRotation(this.Shin, 0.0f, 0.0f, 0.0f);
        this.Foot1 = new ModelRenderer((ModelBase)this, 0, 282);
        this.Foot1.func_78789_a(-7.0f, 38.0f, -11.0f, 14, 4, 17);
        this.Foot1.func_78793_a(0.0f, -18.0f, 0.0f);
        this.Foot1.func_78787_b(64, 32);
        this.Foot1.field_78809_i = true;
        this.setRotation(this.Foot1, 0.0f, 0.0f, 0.0f);
        this.Foot2 = new ModelRenderer((ModelBase)this, 0, 246);
        this.Foot2.func_78789_a(-6.0f, 19.0f, -8.0f, 12, 19, 13);
        this.Foot2.func_78793_a(0.0f, -18.0f, 0.0f);
        this.Foot2.func_78787_b(64, 32);
        this.Foot2.field_78809_i = true;
        this.setRotation(this.Foot2, 0.0f, 0.0f, 0.0f);
        this.Foot3 = new ModelRenderer((ModelBase)this, 0, 219);
        this.Foot3.func_78789_a(-5.0f, 5.0f, -5.0f, 10, 14, 9);
        this.Foot3.func_78793_a(0.0f, -18.0f, 0.0f);
        this.Foot3.func_78787_b(64, 32);
        this.Foot3.field_78809_i = true;
        this.setRotation(this.Foot3, 0.0f, 0.0f, 0.0f);
        this.Thigh2 = new ModelRenderer((ModelBase)this, 0, 43);
        this.Thigh2.func_78789_a(-7.0f, -8.0f, -7.0f, 14, 24, 14);
        this.Thigh2.func_78793_a(0.0f, -58.0f, 0.0f);
        this.Thigh2.func_78787_b(64, 32);
        this.Thigh2.field_78809_i = true;
        this.setRotation(this.Thigh2, 0.0f, 0.0f, 0.0f);
        this.Thigh3 = new ModelRenderer((ModelBase)this, 0, 84);
        this.Thigh3.func_78789_a(-5.0f, 16.0f, -5.0f, 10, 17, 10);
        this.Thigh3.func_78793_a(0.0f, -58.0f, 0.0f);
        this.Thigh3.func_78787_b(64, 32);
        this.Thigh3.field_78809_i = true;
        this.setRotation(this.Thigh3, 0.0f, 0.0f, 0.0f);
        this.Back1 = new ModelRenderer((ModelBase)this, 125, 138);
        this.Back1.func_78789_a(-4.0f, -20.0f, -4.0f, 8, 24, 8);
        this.Back1.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Back1.func_78787_b(64, 32);
        this.Back1.field_78809_i = true;
        this.setRotation(this.Back1, 0.0f, 0.0f, 0.0f);
        this.Back2 = new ModelRenderer((ModelBase)this, 125, 95);
        this.Back2.func_78789_a(-13.0f, -42.0f, -10.0f, 26, 24, 16);
        this.Back2.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Back2.func_78787_b(64, 32);
        this.Back2.field_78809_i = true;
        this.setRotation(this.Back2, 0.0f, 0.0f, 0.0f);
        this.Back3 = new ModelRenderer((ModelBase)this, 125, 43);
        this.Back3.func_78789_a(-17.0f, -68.0f, -13.0f, 34, 26, 20);
        this.Back3.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Back3.func_78787_b(64, 32);
        this.Back3.field_78809_i = true;
        this.setRotation(this.Back3, 0.0f, 0.0f, 0.0f);
        this.Shoulders = new ModelRenderer((ModelBase)this, 60, 200);
        this.Shoulders.func_78789_a(-22.0f, -64.0f, -4.0f, 44, 8, 8);
        this.Shoulders.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Shoulders.func_78787_b(64, 32);
        this.Shoulders.field_78809_i = true;
        this.setRotation(this.Shoulders, 0.0f, 0.0f, 0.0f);
        this.Neck = new ModelRenderer((ModelBase)this, 125, 29);
        this.Neck.func_78789_a(-4.0f, -70.0f, -4.0f, 8, 2, 8);
        this.Neck.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Neck.func_78787_b(64, 32);
        this.Neck.field_78809_i = true;
        this.setRotation(this.Neck, 0.0f, 0.0f, 0.0f);
        this.Head = new ModelRenderer((ModelBase)this, 127, 0);
        this.Head.func_78789_a(-7.0f, -82.0f, -7.0f, 14, 12, 14);
        this.Head.func_78793_a(0.0f, -60.0f, 0.0f);
        this.Head.func_78787_b(64, 32);
        this.Head.field_78809_i = true;
        this.setRotation(this.Head, 0.0f, 0.0f, 0.0f);
        this.Arm1 = new ModelRenderer((ModelBase)this, 77, 250);
        this.Arm1.func_78789_a(-6.0f, -6.0f, -6.0f, 12, 21, 12);
        this.Arm1.func_78793_a(28.0f, -120.0f, 0.0f);
        this.Arm1.func_78787_b(64, 32);
        this.Arm1.field_78809_i = true;
        this.setRotation(this.Arm1, 0.0f, 0.0f, 0.0f);
        this.Arm2 = new ModelRenderer((ModelBase)this, 73, 300);
        this.Arm2.func_78789_a(-4.0f, 15.0f, -4.0f, 8, 24, 8);
        this.Arm2.func_78793_a(28.0f, -120.0f, 0.0f);
        this.Arm2.func_78787_b(64, 32);
        this.Arm2.field_78809_i = true;
        this.setRotation(this.Arm2, 0.0f, 0.0f, 0.0f);
        this.Arm3 = new ModelRenderer((ModelBase)this, 61, 350);
        this.Arm3.func_78789_a(-3.0f, -3.0f, -3.0f, 6, 33, 6);
        this.Arm3.func_78793_a(28.0f, -81.0f, 0.0f);
        this.Arm3.func_78787_b(64, 32);
        this.Arm3.field_78809_i = true;
        this.setRotation(this.Arm3, 0.0f, 0.0f, 0.0f);
        this.Knuckles = new ModelRenderer((ModelBase)this, 56, 400);
        this.Knuckles.func_78789_a(-7.0f, 30.0f, -5.0f, 14, 12, 10);
        this.Knuckles.func_78793_a(28.0f, -81.0f, 0.0f);
        this.Knuckles.func_78787_b(256, 512);
        this.Knuckles.field_78809_i = true;
        this.setRotation(this.Knuckles, 0.0f, 0.0f, 0.0f);
        this.hipy = this.Hip.field_78797_d;
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float b2angle;
        float a2angle;
        GiantRobot e = (GiantRobot)entity;
        RenderGiantRobotInfo r = null;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        r = e.getRenderGiantRobotInfo();
        float movescale = f1 * 0.65f;
        if (movescale > 1.0f) {
            movescale = 1.0f;
        }
        r.hipxdisplayangle = (float)(Math.cos(-f2 * this.wingspeed) * Math.PI * (double)0.1f * (double)movescale);
        r.hipydisplayangle = (float)(Math.sin(-f2 * this.wingspeed) * Math.PI * (double)0.1f * (double)movescale);
        r.thighdisplayangle[0] = (float)(Math.cos((double)(-f2 * this.wingspeed) + 1.5707963267948966) * Math.PI * (double)0.15f * (double)movescale) - (float)(0.19634954084936207 * (double)movescale);
        r.thighdisplayangle[1] = (float)(Math.cos((double)(-f2 * this.wingspeed) + Math.PI + 1.5707963267948966) * Math.PI * (double)0.15f * (double)movescale) - (float)(0.19634954084936207 * (double)movescale);
        r.shindisplayangle[0] = (float)((double)((float)(Math.cos((double)(-f2 * this.wingspeed) + Math.PI) * Math.PI * (double)0.2f * (double)movescale)) + 0.6283185400806344 * (double)movescale);
        r.shindisplayangle[1] = (float)((double)((float)(Math.cos(-f2 * this.wingspeed) * Math.PI * (double)0.2f * (double)movescale)) + 0.6283185400806344 * (double)movescale);
        float newangle = (float)(Math.cos(-f2 * this.wingspeed * 2.0f) * (double)movescale);
        this.Hip.field_78797_d = this.hipy + newangle * 4.0f;
        this.Hip.field_78795_f = r.hipxdisplayangle;
        this.Hip.field_78796_g = (float)((double)r.hipydisplayangle + 1.5707963267948966);
        this.Hip.func_78785_a(f5);
        this.Thigh2.field_78795_f = this.Thigh3.field_78795_f = r.thighdisplayangle[0];
        this.Thigh.field_78795_f = this.Thigh3.field_78795_f;
        this.Thigh2.field_78797_d = this.Thigh3.field_78797_d = this.Hip.field_78797_d - (float)Math.sin(this.Hip.field_78795_f) * 13.0f;
        this.Thigh.field_78797_d = this.Thigh3.field_78797_d;
        this.Thigh2.field_78798_e = this.Thigh3.field_78798_e = this.Hip.field_78798_e + (float)Math.cos(this.Hip.field_78795_f) * (float)Math.cos(this.Hip.field_78796_g) * 13.0f;
        this.Thigh.field_78798_e = this.Thigh3.field_78798_e;
        this.Thigh2.field_78800_c = this.Thigh3.field_78800_c = this.Hip.field_78800_c + (float)Math.cos(this.Hip.field_78795_f) * (float)Math.sin(this.Hip.field_78796_g) * 13.0f;
        this.Thigh.field_78800_c = this.Thigh3.field_78800_c;
        this.Thigh.func_78785_a(f5);
        this.Thigh2.func_78785_a(f5);
        this.Thigh3.func_78785_a(f5);
        this.Shin.field_78795_f = r.shindisplayangle[0];
        this.Shin.field_78797_d = this.Thigh.field_78797_d + (float)Math.cos(this.Thigh.field_78795_f) * 40.0f;
        this.Shin.field_78798_e = this.Thigh.field_78798_e + (float)Math.sin(this.Thigh.field_78795_f) * 40.0f;
        this.Shin.field_78800_c = this.Thigh.field_78800_c;
        this.Shin.func_78785_a(f5);
        this.Foot2.field_78795_f = this.Foot3.field_78795_f = r.shindisplayangle[0];
        this.Foot1.field_78795_f = this.Foot3.field_78795_f;
        this.Foot2.field_78797_d = this.Foot3.field_78797_d = this.Shin.field_78797_d;
        this.Foot1.field_78797_d = this.Foot3.field_78797_d;
        this.Foot2.field_78798_e = this.Foot3.field_78798_e = this.Shin.field_78798_e;
        this.Foot1.field_78798_e = this.Foot3.field_78798_e;
        this.Foot2.field_78800_c = this.Foot3.field_78800_c = this.Shin.field_78800_c;
        this.Foot1.field_78800_c = this.Foot3.field_78800_c;
        this.Foot1.func_78785_a(f5);
        this.Foot2.func_78785_a(f5);
        this.Foot3.func_78785_a(f5);
        this.Thigh2.field_78795_f = this.Thigh3.field_78795_f = r.thighdisplayangle[1];
        this.Thigh.field_78795_f = this.Thigh3.field_78795_f;
        this.Thigh2.field_78797_d = this.Thigh3.field_78797_d = this.Hip.field_78797_d + (float)Math.sin(this.Hip.field_78795_f) * 13.0f;
        this.Thigh.field_78797_d = this.Thigh3.field_78797_d;
        this.Thigh2.field_78798_e = this.Thigh3.field_78798_e = this.Hip.field_78798_e - (float)Math.cos(this.Hip.field_78795_f) * (float)Math.cos(this.Hip.field_78796_g) * 13.0f;
        this.Thigh.field_78798_e = this.Thigh3.field_78798_e;
        this.Thigh2.field_78800_c = this.Thigh3.field_78800_c = this.Hip.field_78800_c - (float)Math.cos(this.Hip.field_78795_f) * (float)Math.sin(this.Hip.field_78796_g) * 13.0f;
        this.Thigh.field_78800_c = this.Thigh3.field_78800_c;
        this.Thigh.func_78785_a(f5);
        this.Thigh2.func_78785_a(f5);
        this.Thigh3.func_78785_a(f5);
        this.Shin.field_78795_f = r.shindisplayangle[1];
        this.Shin.field_78797_d = this.Thigh.field_78797_d + (float)Math.cos(this.Thigh.field_78795_f) * 40.0f;
        this.Shin.field_78798_e = this.Thigh.field_78798_e + (float)Math.sin(this.Thigh.field_78795_f) * 40.0f;
        this.Shin.field_78800_c = this.Thigh.field_78800_c;
        this.Shin.func_78785_a(f5);
        this.Foot2.field_78795_f = this.Foot3.field_78795_f = r.shindisplayangle[1];
        this.Foot1.field_78795_f = this.Foot3.field_78795_f;
        this.Foot2.field_78797_d = this.Foot3.field_78797_d = this.Shin.field_78797_d;
        this.Foot1.field_78797_d = this.Foot3.field_78797_d;
        this.Foot2.field_78798_e = this.Foot3.field_78798_e = this.Shin.field_78798_e;
        this.Foot1.field_78798_e = this.Foot3.field_78798_e;
        this.Foot2.field_78800_c = this.Foot3.field_78800_c = this.Shin.field_78800_c;
        this.Foot1.field_78800_c = this.Foot3.field_78800_c;
        this.Foot1.func_78785_a(f5);
        this.Foot2.func_78785_a(f5);
        this.Foot3.func_78785_a(f5);
        float shoulderangle = -r.hipydisplayangle;
        float a1angle = a2angle = r.thighdisplayangle[1];
        float b1angle = b2angle = r.thighdisplayangle[0];
        if (e.getAttacking() != 0) {
            shoulderangle = (float)(-(Math.sin(f2 * this.wingspeed * 2.0f) * Math.PI * (double)0.2f));
            a1angle = (float)((double)((float)(Math.sin(f2 * this.wingspeed * 2.0f) * Math.PI / 5.0)) - 0.7853981633974483);
            a2angle = (float)((double)(-a1angle) + Math.PI);
            a1angle = (float)((double)a1angle + 0.6283185307179586);
            a2angle = (float)((double)a2angle + 0.6283185307179586);
            b1angle = (float)((double)((float)(-(Math.sin(f2 * this.wingspeed * 2.0f) * Math.PI / 5.0))) - 0.7853981633974483);
            b2angle = (float)((double)(-b1angle) + Math.PI);
            b1angle = (float)((double)b1angle + 0.6283185307179586);
            b2angle = (float)((double)b2angle + 0.6283185307179586);
        }
        this.Back3.field_78796_g = shoulderangle / 2.0f;
        this.Shoulders.field_78796_g = shoulderangle;
        this.Arm1.field_78797_d = this.Arm2.field_78797_d = this.Hip.field_78797_d - 60.0f;
        this.Arm1.field_78800_c = this.Arm2.field_78800_c = this.Hip.field_78800_c + 26.0f;
        this.Arm1.field_78798_e = this.Arm2.field_78798_e = this.Shoulders.field_78798_e - (float)Math.sin(this.Shoulders.field_78796_g) * 26.0f;
        this.Arm1.field_78795_f = this.Arm2.field_78795_f = a1angle;
        this.Arm1.func_78785_a(f5);
        this.Arm2.func_78785_a(f5);
        this.Arm3.field_78795_f = this.Knuckles.field_78795_f = (float)((double)a2angle - 0.19634954084936207);
        this.Arm3.field_78797_d = this.Knuckles.field_78797_d = this.Arm1.field_78797_d + (float)Math.cos(this.Arm1.field_78795_f) * 41.0f;
        this.Arm3.field_78798_e = this.Knuckles.field_78798_e = this.Arm1.field_78798_e + (float)Math.sin(this.Arm1.field_78795_f) * 41.0f;
        this.Arm3.field_78800_c = this.Knuckles.field_78800_c = this.Arm1.field_78800_c;
        this.Arm3.func_78785_a(f5);
        this.Knuckles.func_78785_a(f5);
        this.Arm1.field_78797_d = this.Arm2.field_78797_d = this.Hip.field_78797_d - 60.0f;
        this.Arm1.field_78800_c = this.Arm2.field_78800_c = this.Hip.field_78800_c - 26.0f;
        this.Arm1.field_78798_e = this.Arm2.field_78798_e = this.Shoulders.field_78798_e + (float)Math.sin(this.Shoulders.field_78796_g) * 26.0f;
        this.Arm1.field_78795_f = this.Arm2.field_78795_f = b1angle;
        this.Arm1.func_78785_a(f5);
        this.Arm2.func_78785_a(f5);
        this.Arm3.field_78795_f = this.Knuckles.field_78795_f = (float)((double)b2angle - 0.19634954084936207);
        this.Arm3.field_78797_d = this.Knuckles.field_78797_d = this.Arm1.field_78797_d + (float)Math.cos(this.Arm1.field_78795_f) * 41.0f;
        this.Arm3.field_78798_e = this.Knuckles.field_78798_e = this.Arm1.field_78798_e + (float)Math.sin(this.Arm1.field_78795_f) * 41.0f;
        this.Arm3.field_78800_c = this.Knuckles.field_78800_c = this.Arm1.field_78800_c;
        this.Arm3.func_78785_a(f5);
        this.Knuckles.func_78785_a(f5);
        this.Back2.field_78797_d = this.Back3.field_78797_d = this.Hip.field_78797_d;
        this.Back1.field_78797_d = this.Back3.field_78797_d;
        this.Neck.field_78797_d = this.Head.field_78797_d = this.Hip.field_78797_d;
        this.Shoulders.field_78797_d = this.Head.field_78797_d;
        this.Head.field_78796_g = (float)Math.toRadians(f3);
        this.Head.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.Back1.func_78785_a(f5);
        this.Back2.func_78785_a(f5);
        this.Back3.func_78785_a(f5);
        this.Shoulders.func_78785_a(f5);
        this.Neck.func_78785_a(f5);
        this.Head.func_78785_a(f5);
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

