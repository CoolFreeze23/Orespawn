/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.RenderInfo;
import danger.orespawn.Scorpion;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelScorpion
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer body;
    ModelRenderer tail1;
    ModelRenderer tail2;
    ModelRenderer tail3;
    ModelRenderer tail4;
    ModelRenderer tail5;
    ModelRenderer tail6;
    ModelRenderer lleg1;
    ModelRenderer rleg1;
    ModelRenderer rleg2;
    ModelRenderer lleg3;
    ModelRenderer rleg4;
    ModelRenderer rleg3;
    ModelRenderer lleg4;
    ModelRenderer lleg2;
    ModelRenderer head;
    ModelRenderer larm2;
    ModelRenderer rarm2;
    ModelRenderer larm1;
    ModelRenderer rarm1;
    ModelRenderer lclaw;
    ModelRenderer rclaw;

    public ModelScorpion(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 88;
        this.field_78089_u = 24;
        this.body = new ModelRenderer((ModelBase)this, 0, 0);
        this.body.func_78789_a(0.0f, 0.0f, 0.0f, 6, 4, 8);
        this.body.func_78793_a(-3.0f, 17.0f, -4.0f);
        this.body.func_78787_b(88, 24);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 28, 0);
        this.tail1.func_78789_a(0.0f, 0.0f, 0.0f, 4, 4, 5);
        this.tail1.func_78793_a(-2.0f, 17.0f, 3.0f);
        this.tail1.func_78787_b(88, 24);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.2617994f, 0.0f, 0.0f);
        this.tail2 = new ModelRenderer((ModelBase)this, 46, 0);
        this.tail2.func_78789_a(0.0f, 0.0f, 0.0f, 3, 3, 5);
        this.tail2.func_78793_a(-1.5f, 16.8f, 6.0f);
        this.tail2.func_78787_b(88, 24);
        this.tail2.field_78809_i = true;
        this.setRotation(this.tail2, 1.029744f, 0.0f, 0.0f);
        this.tail3 = new ModelRenderer((ModelBase)this, 62, 0);
        this.tail3.func_78789_a(0.0f, 0.0f, 0.0f, 3, 3, 4);
        this.tail3.func_78793_a(-1.5f, 14.5f, 8.0f);
        this.tail3.func_78787_b(88, 24);
        this.tail3.field_78809_i = true;
        this.setRotation(this.tail3, 1.727876f, 0.0f, 0.0f);
        this.tail4 = new ModelRenderer((ModelBase)this, 0, 17);
        this.tail4.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 5);
        this.tail4.func_78793_a(-1.0f, 12.0f, 9.0f);
        this.tail4.func_78787_b(88, 24);
        this.tail4.field_78809_i = true;
        this.setRotation(this.tail4, 2.513274f, 0.0f, 0.0f);
        this.tail5 = new ModelRenderer((ModelBase)this, 70, 7);
        this.tail5.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 4);
        this.tail5.func_78793_a(-1.0f, 9.0f, 6.0f);
        this.tail5.func_78787_b(88, 24);
        this.tail5.field_78809_i = true;
        this.setRotation(this.tail5, 3.141593f, 0.0f, 0.0f);
        this.tail6 = new ModelRenderer((ModelBase)this, 62, 7);
        this.tail6.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 3);
        this.tail6.func_78793_a(-0.5f, 8.0f, 2.0f);
        this.tail6.func_78787_b(88, 24);
        this.tail6.field_78809_i = true;
        this.setRotation(this.tail6, 3.141593f, 0.0f, 0.0f);
        this.lleg1 = new ModelRenderer((ModelBase)this, 0, 12);
        this.lleg1.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.lleg1.func_78793_a(2.0f, 18.0f, -3.0f);
        this.lleg1.func_78787_b(88, 24);
        this.lleg1.field_78809_i = true;
        this.setRotation(this.lleg1, 0.0f, 0.4886922f, 0.3665191f);
        this.rleg1 = new ModelRenderer((ModelBase)this, 0, 12);
        this.rleg1.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.rleg1.func_78793_a(-2.0f, 18.0f, -1.0f);
        this.rleg1.func_78787_b(88, 24);
        this.rleg1.field_78809_i = true;
        this.setRotation(this.rleg1, 0.0f, 2.6529f, -0.3665191f);
        this.rleg2 = new ModelRenderer((ModelBase)this, 0, 12);
        this.rleg2.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.rleg2.func_78793_a(-2.0f, 18.0f, 1.0f);
        this.rleg2.func_78787_b(88, 24);
        this.rleg2.field_78809_i = true;
        this.setRotation(this.rleg2, 0.0f, 2.897247f, -0.3665191f);
        this.lleg3 = new ModelRenderer((ModelBase)this, 0, 12);
        this.lleg3.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.lleg3.func_78793_a(2.0f, 18.0f, 1.0f);
        this.lleg3.func_78787_b(88, 24);
        this.lleg3.field_78809_i = true;
        this.setRotation(this.lleg3, 0.0f, -0.2443461f, 0.3665191f);
        this.rleg4 = new ModelRenderer((ModelBase)this, 0, 12);
        this.rleg4.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.rleg4.func_78793_a(-2.0f, 18.0f, 5.0f);
        this.rleg4.func_78787_b(88, 24);
        this.rleg4.field_78809_i = true;
        this.setRotation(this.rleg4, 0.0f, -2.6529f, -0.3665191f);
        this.rleg3 = new ModelRenderer((ModelBase)this, 0, 12);
        this.rleg3.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.rleg3.func_78793_a(-2.0f, 18.0f, 3.0f);
        this.rleg3.func_78787_b(88, 24);
        this.rleg3.field_78809_i = true;
        this.setRotation(this.rleg3, 0.0f, -2.897247f, -0.3665191f);
        this.lleg4 = new ModelRenderer((ModelBase)this, 0, 12);
        this.lleg4.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.lleg4.func_78793_a(2.0f, 18.0f, 3.0f);
        this.lleg4.func_78787_b(88, 24);
        this.lleg4.field_78809_i = true;
        this.setRotation(this.lleg4, 0.0f, -0.4886922f, 0.3665191f);
        this.lleg2 = new ModelRenderer((ModelBase)this, 0, 12);
        this.lleg2.func_78789_a(0.0f, 0.0f, 0.0f, 11, 2, 2);
        this.lleg2.func_78793_a(2.0f, 18.0f, -1.0f);
        this.lleg2.func_78787_b(88, 24);
        this.lleg2.field_78809_i = true;
        this.setRotation(this.lleg2, 0.0f, 0.2443461f, 0.3665191f);
        this.head = new ModelRenderer((ModelBase)this, 28, 9);
        this.head.func_78789_a(0.0f, 0.0f, 0.0f, 5, 3, 4);
        this.head.func_78793_a(-2.5f, 17.5f, -8.0f);
        this.head.func_78787_b(88, 24);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.larm2 = new ModelRenderer((ModelBase)this, 46, 8);
        this.larm2.func_78789_a(0.0f, 0.0f, 0.0f, 6, 2, 2);
        this.larm2.func_78793_a(1.0f, 18.0f, -6.0f);
        this.larm2.func_78787_b(88, 24);
        this.larm2.field_78809_i = true;
        this.setRotation(this.larm2, 0.0f, 0.5235988f, 0.1745329f);
        this.rarm2 = new ModelRenderer((ModelBase)this, 46, 8);
        this.rarm2.func_78789_a(0.0f, 0.0f, -2.0f, 6, 2, 2);
        this.rarm2.func_78793_a(-1.0f, 18.0f, -6.0f);
        this.rarm2.func_78787_b(88, 24);
        this.rarm2.field_78809_i = true;
        this.setRotation(this.rarm2, 0.0f, 2.617994f, -0.1745329f);
        this.larm1 = new ModelRenderer((ModelBase)this, 70, 13);
        this.larm1.func_78789_a(-2.0f, 0.0f, -3.0f, 2, 2, 3);
        this.larm1.func_78793_a(7.0f, 19.0f, -7.2f);
        this.larm1.func_78787_b(88, 24);
        this.larm1.field_78809_i = true;
        this.setRotation(this.larm1, 0.1745329f, 0.1745329f, 0.0f);
        this.rarm1 = new ModelRenderer((ModelBase)this, 70, 13);
        this.rarm1.func_78789_a(0.0f, 0.0f, -3.0f, 2, 2, 3);
        this.rarm1.func_78793_a(-7.0f, 19.0f, -7.2f);
        this.rarm1.func_78787_b(88, 24);
        this.rarm1.field_78809_i = true;
        this.setRotation(this.rarm1, 0.1745329f, -0.1745329f, 0.0f);
        this.lclaw = new ModelRenderer((ModelBase)this, 46, 12);
        this.lclaw.func_78789_a(-3.0f, 0.0f, -4.0f, 3, 2, 4);
        this.lclaw.func_78793_a(7.0f, 19.0f, -10.0f);
        this.lclaw.func_78787_b(88, 24);
        this.lclaw.field_78809_i = true;
        this.setRotation(this.lclaw, 0.0174533f, 0.3839724f, 0.1396263f);
        this.rclaw = new ModelRenderer((ModelBase)this, 46, 12);
        this.rclaw.func_78789_a(0.0f, 0.0f, -4.0f, 3, 2, 4);
        this.rclaw.func_78793_a(-7.0f, 19.0f, -10.0f);
        this.rclaw.func_78787_b(88, 24);
        this.rclaw.field_78809_i = true;
        this.setRotation(this.rclaw, 0.0174533f, -0.3839724f, 0.1396263f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Scorpion e = (Scorpion)entity;
        RenderInfo r = null;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = MathHelper.func_76134_b((float)(f2 * 2.0f * this.wingspeed)) * (float)Math.PI * 0.12f * f1;
        this.lleg1.field_78796_g = newangle + 0.49f;
        this.rleg1.field_78796_g = -newangle + 2.65f;
        newangle = MathHelper.func_76134_b((float)(f2 * 2.0f * this.wingspeed - 1.0f * pi4)) * (float)Math.PI * 0.12f * f1;
        this.lleg2.field_78796_g = newangle + 0.24f;
        this.rleg2.field_78796_g = -newangle + 2.9f;
        newangle = MathHelper.func_76134_b((float)(f2 * 2.0f * this.wingspeed - 2.0f * pi4)) * (float)Math.PI * 0.12f * f1;
        this.lleg3.field_78796_g = newangle - 0.24f;
        this.rleg3.field_78796_g = -newangle - 2.9f;
        newangle = MathHelper.func_76134_b((float)(f2 * 2.0f * this.wingspeed - 3.0f * pi4)) * (float)Math.PI * 0.12f * f1;
        this.lleg4.field_78796_g = newangle - 0.49f;
        this.rleg4.field_78796_g = -newangle - 2.65f;
        r = e.getRenderInfo();
        newangle = MathHelper.func_76134_b((float)(f2 * 3.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        nextangle = MathHelper.func_76134_b((float)((f2 + 0.1f) * 3.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (e.getAttacking() == 0) {
                r.ri1 = e.field_70170_p.field_73012_v.nextInt(20);
                r.ri2 = e.field_70170_p.field_73012_v.nextInt(25);
            } else {
                r.ri1 = e.field_70170_p.field_73012_v.nextInt(4);
                r.ri2 = e.field_70170_p.field_73012_v.nextInt(3);
            }
        }
        if (r.ri1 == 1 || r.ri1 == 3) {
            this.doLeftClaw(newangle);
        } else {
            this.doLeftClaw(0.0f);
        }
        if (r.ri1 == 2 || r.ri1 == 3) {
            this.doRightClaw(newangle);
        } else {
            this.doRightClaw(0.0f);
        }
        if (r.ri2 == 1) {
            this.doTail(newangle);
        } else {
            this.doTail(0.0f);
        }
        e.setRenderInfo(r);
        this.body.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.tail2.func_78785_a(f5);
        this.tail3.func_78785_a(f5);
        this.tail4.func_78785_a(f5);
        this.tail5.func_78785_a(f5);
        this.tail6.func_78785_a(f5);
        this.lleg1.func_78785_a(f5);
        this.rleg1.func_78785_a(f5);
        this.rleg2.func_78785_a(f5);
        this.lleg3.func_78785_a(f5);
        this.rleg4.func_78785_a(f5);
        this.rleg3.func_78785_a(f5);
        this.lleg4.func_78785_a(f5);
        this.lleg2.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.larm2.func_78785_a(f5);
        this.rarm2.func_78785_a(f5);
        this.larm1.func_78785_a(f5);
        this.rarm1.func_78785_a(f5);
        this.lclaw.func_78785_a(f5);
        this.rclaw.func_78785_a(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }

    public void func_78087_a(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity) {
        super.func_78087_a(par1, par2, par3, par4, par5, par6, par7Entity);
    }

    private void doLeftClaw(float angle) {
        this.larm2.field_78796_g = 0.52f + angle;
        this.larm1.field_78798_e = (float)((double)this.larm2.field_78798_e - Math.sin(this.larm2.field_78796_g) * 4.5);
        this.lclaw.field_78798_e = this.larm1.field_78798_e - 3.0f;
        this.lclaw.field_78796_g = 0.381f - angle;
    }

    private void doRightClaw(float angle) {
        this.rarm2.field_78796_g = 2.61f - angle;
        this.rarm1.field_78798_e = (float)((double)this.rarm2.field_78798_e - Math.sin(this.rarm2.field_78796_g) * 4.5);
        this.rclaw.field_78798_e = this.rarm1.field_78798_e - 3.0f;
        this.rclaw.field_78796_g = -0.381f + angle;
    }

    private void doTail(float angle) {
        this.tail1.field_78795_f = 0.26f + angle;
        this.tail2.field_78795_f = this.tail1.field_78795_f + 0.76900005f + angle;
        this.tail2.field_78797_d = (float)((double)this.tail1.field_78797_d - Math.sin(this.tail1.field_78795_f) * 4.0);
        this.tail2.field_78798_e = (float)((double)this.tail1.field_78798_e + Math.cos(this.tail1.field_78795_f) * 4.0);
        this.tail3.field_78795_f = this.tail2.field_78795_f + 0.701f + angle;
        this.tail3.field_78797_d = (float)((double)this.tail2.field_78797_d - Math.sin(this.tail2.field_78795_f) * 4.0);
        this.tail3.field_78798_e = (float)((double)this.tail2.field_78798_e + Math.cos(this.tail2.field_78795_f) * 4.0);
        this.tail4.field_78795_f = this.tail3.field_78795_f + -5.501f - angle * 3.0f / 2.0f - 0.4f;
        this.tail4.field_78797_d = (float)((double)this.tail3.field_78797_d - Math.sin(this.tail3.field_78795_f) * 3.0);
        this.tail4.field_78798_e = (float)((double)this.tail3.field_78798_e + Math.cos(this.tail3.field_78795_f) * 3.0);
        this.tail5.field_78797_d = (float)((double)this.tail4.field_78797_d - Math.sin(this.tail4.field_78795_f) * 4.0);
        this.tail5.field_78798_e = (float)((double)this.tail4.field_78798_e + Math.cos(this.tail4.field_78795_f) * 4.0);
        this.tail6.field_78797_d = (float)((double)this.tail5.field_78797_d - Math.sin(this.tail5.field_78795_f) * 4.0);
        this.tail6.field_78798_e = (float)((double)this.tail5.field_78798_e + Math.cos(this.tail5.field_78795_f) * 4.0);
    }
}

