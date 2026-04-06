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

import danger.orespawn.EntityCannonFodder;
import danger.orespawn.Ostrich;
import danger.orespawn.RenderInfo;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelOstrich
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Body1;
    ModelRenderer body2;
    ModelRenderer LLeg1;
    ModelRenderer Rleg1;
    ModelRenderer LLeg2;
    ModelRenderer Lfoot1;
    ModelRenderer RLeg2;
    ModelRenderer Lfoot2;
    ModelRenderer Lfoot3;
    ModelRenderer LClaw1;
    ModelRenderer LClaw2;
    ModelRenderer LClaw3;
    ModelRenderer Lfoot4;
    ModelRenderer LClaw4;
    ModelRenderer Rfoot1;
    ModelRenderer Rfoot2;
    ModelRenderer Rclaw1;
    ModelRenderer Rfoot3;
    ModelRenderer Rclaw3;
    ModelRenderer Rfoot4;
    ModelRenderer Rclaw2;
    ModelRenderer Rclaw4;
    ModelRenderer Body3;
    ModelRenderer Tail1;
    ModelRenderer Tail2;
    ModelRenderer Tail3;
    ModelRenderer Body4;
    ModelRenderer head;
    ModelRenderer leftleg;
    ModelRenderer Neck1;
    ModelRenderer Head1;
    ModelRenderer mouth1;
    ModelRenderer neck2;
    ModelRenderer rightleg;
    ModelRenderer Lwing;
    ModelRenderer Rwing;
    ModelRenderer Hat1;
    ModelRenderer Hat2;

    public ModelOstrich(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 256;
        this.field_78089_u = 128;
        this.Body1 = new ModelRenderer((ModelBase)this, 0, 28);
        this.Body1.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 9, 8);
        this.Body1.func_78793_a(0.0f, 0.0f, -6.0f);
        this.Body1.func_78787_b(256, 128);
        this.Body1.field_78809_i = true;
        this.setRotation(this.Body1, -0.2230717f, 0.0f, 0.0f);
        this.body2 = new ModelRenderer((ModelBase)this, 25, 111);
        this.body2.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 8, 8);
        this.body2.func_78793_a(0.0f, 2.0f, -1.0f);
        this.body2.func_78787_b(256, 128);
        this.body2.field_78809_i = true;
        this.setRotation(this.body2, 0.0f, 0.0f, 0.0f);
        this.LLeg1 = new ModelRenderer((ModelBase)this, 25, 70);
        this.LLeg1.func_78789_a(-1.0f, 3.0f, -5.0f, 2, 7, 3);
        this.LLeg1.func_78793_a(3.0f, 8.0f, 1.0f);
        this.LLeg1.func_78787_b(256, 128);
        this.LLeg1.field_78809_i = true;
        this.setRotation(this.LLeg1, 0.4833219f, 0.0f, 0.0f);
        this.Rleg1 = new ModelRenderer((ModelBase)this, 25, 70);
        this.Rleg1.func_78789_a(-2.0f, 3.0f, -5.0f, 2, 7, 3);
        this.Rleg1.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rleg1.func_78787_b(256, 128);
        this.Rleg1.field_78809_i = true;
        this.setRotation(this.Rleg1, 0.4833219f, 0.0f, 0.0f);
        this.LLeg2 = new ModelRenderer((ModelBase)this, 29, 59);
        this.LLeg2.func_78789_a(-1.0f, 7.0f, 4.0f, 2, 7, 3);
        this.LLeg2.func_78793_a(3.0f, 8.0f, 1.0f);
        this.LLeg2.func_78787_b(256, 128);
        this.LLeg2.field_78809_i = true;
        this.setRotation(this.LLeg2, -0.4370552f, 0.0f, 0.0f);
        this.Lfoot1 = new ModelRenderer((ModelBase)this, 29, 50);
        this.Lfoot1.func_78789_a(-1.0f, 14.0f, -5.0f, 2, 2, 6);
        this.Lfoot1.func_78793_a(3.0f, 8.0f, 1.0f);
        this.Lfoot1.func_78787_b(256, 128);
        this.Lfoot1.field_78809_i = true;
        this.setRotation(this.Lfoot1, 0.0f, 0.0f, 0.0f);
        this.RLeg2 = new ModelRenderer((ModelBase)this, 29, 59);
        this.RLeg2.func_78789_a(-2.0f, 7.0f, 4.0f, 2, 7, 3);
        this.RLeg2.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.RLeg2.func_78787_b(256, 128);
        this.RLeg2.field_78809_i = true;
        this.setRotation(this.RLeg2, -0.4370552f, 0.0f, 0.0f);
        this.Lfoot2 = new ModelRenderer((ModelBase)this, 0, 9);
        this.Lfoot2.func_78789_a(-1.0f, 15.0f, -4.0f, 2, 1, 5);
        this.Lfoot2.func_78793_a(3.0f, 8.0f, 1.0f);
        this.Lfoot2.func_78787_b(256, 128);
        this.Lfoot2.field_78809_i = true;
        this.setRotation(this.Lfoot2, 0.0f, 0.2602503f, 0.0f);
        this.Lfoot3 = new ModelRenderer((ModelBase)this, 0, 9);
        this.Lfoot3.func_78789_a(-1.0f, 15.0f, -4.0f, 2, 1, 5);
        this.Lfoot3.func_78793_a(3.0f, 8.0f, 1.0f);
        this.Lfoot3.func_78787_b(256, 128);
        this.Lfoot3.field_78809_i = true;
        this.setRotation(this.Lfoot3, 0.0f, -0.260246f, 0.0f);
        this.LClaw1 = new ModelRenderer((ModelBase)this, 16, 10);
        this.LClaw1.func_78789_a(0.0f, 14.0f, -7.0f, 0, 2, 3);
        this.LClaw1.func_78793_a(3.0f, 8.0f, 1.0f);
        this.LClaw1.func_78787_b(256, 128);
        this.LClaw1.field_78809_i = true;
        this.setRotation(this.LClaw1, 0.0f, 0.0f, 0.0f);
        this.LClaw2 = new ModelRenderer((ModelBase)this, 19, 16);
        this.LClaw2.func_78789_a(-0.5f, 15.0f, -5.0f, 0, 1, 3);
        this.LClaw2.func_78793_a(3.0f, 8.0f, 1.0f);
        this.LClaw2.func_78787_b(256, 128);
        this.LClaw2.field_78809_i = true;
        this.setRotation(this.LClaw2, 0.0f, 0.260246f, 0.0f);
        this.LClaw3 = new ModelRenderer((ModelBase)this, 19, 16);
        this.LClaw3.func_78789_a(0.5f, 15.0f, -5.0f, 0, 1, 3);
        this.LClaw3.func_78793_a(3.0f, 8.0f, 1.0f);
        this.LClaw3.func_78787_b(256, 128);
        this.LClaw3.field_78809_i = true;
        this.setRotation(this.LClaw3, 0.0f, -0.260246f, 0.0f);
        this.Lfoot4 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Lfoot4.func_78789_a(-1.0f, 14.0f, -1.0f, 2, 2, 4);
        this.Lfoot4.func_78793_a(3.0f, 8.0f, 1.0f);
        this.Lfoot4.func_78787_b(256, 128);
        this.Lfoot4.field_78809_i = true;
        this.setRotation(this.Lfoot4, 0.0f, 0.0f, 0.0f);
        this.LClaw4 = new ModelRenderer((ModelBase)this, 16, 10);
        this.LClaw4.func_78789_a(0.0f, 14.0f, 2.0f, 0, 2, 3);
        this.LClaw4.func_78793_a(3.0f, 8.0f, 1.0f);
        this.LClaw4.func_78787_b(256, 128);
        this.LClaw4.field_78809_i = true;
        this.setRotation(this.LClaw4, 0.0f, 0.0f, 0.0f);
        this.Rfoot1 = new ModelRenderer((ModelBase)this, 29, 50);
        this.Rfoot1.func_78789_a(-2.0f, 14.0f, -5.0f, 2, 2, 6);
        this.Rfoot1.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rfoot1.func_78787_b(256, 128);
        this.Rfoot1.field_78809_i = true;
        this.setRotation(this.Rfoot1, 0.0f, 0.0f, 0.0f);
        this.Rfoot2 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Rfoot2.func_78789_a(-2.0f, 14.0f, -1.0f, 2, 2, 4);
        this.Rfoot2.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rfoot2.func_78787_b(256, 128);
        this.Rfoot2.field_78809_i = true;
        this.setRotation(this.Rfoot2, 0.0f, 0.0f, 0.0f);
        this.Rclaw1 = new ModelRenderer((ModelBase)this, 16, 10);
        this.Rclaw1.func_78789_a(-1.0f, 14.0f, -7.0f, 0, 2, 3);
        this.Rclaw1.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rclaw1.func_78787_b(256, 128);
        this.Rclaw1.field_78809_i = true;
        this.setRotation(this.Rclaw1, 0.0f, 0.0f, 0.0f);
        this.Rfoot3 = new ModelRenderer((ModelBase)this, 0, 9);
        this.Rfoot3.func_78789_a(-2.0f, 15.0f, -4.0f, 2, 1, 5);
        this.Rfoot3.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rfoot3.func_78787_b(256, 128);
        this.Rfoot3.field_78809_i = true;
        this.setRotation(this.Rfoot3, 0.0f, -0.260246f, 0.0f);
        this.Rclaw3 = new ModelRenderer((ModelBase)this, 19, 16);
        this.Rclaw3.func_78789_a(-0.5f, 15.0f, -5.0f, 0, 1, 3);
        this.Rclaw3.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rclaw3.func_78787_b(256, 128);
        this.Rclaw3.field_78809_i = true;
        this.setRotation(this.Rclaw3, 0.0f, -0.260246f, 0.0f);
        this.Rfoot4 = new ModelRenderer((ModelBase)this, 0, 9);
        this.Rfoot4.func_78789_a(-2.0f, 15.0f, -4.0f, 2, 1, 5);
        this.Rfoot4.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rfoot4.func_78787_b(256, 128);
        this.Rfoot4.field_78809_i = true;
        this.setRotation(this.Rfoot4, 0.0f, 0.2602503f, 0.0f);
        this.Rclaw2 = new ModelRenderer((ModelBase)this, 19, 16);
        this.Rclaw2.func_78789_a(-1.5f, 15.0f, -5.0f, 0, 1, 3);
        this.Rclaw2.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rclaw2.func_78787_b(256, 128);
        this.Rclaw2.field_78809_i = true;
        this.setRotation(this.Rclaw2, 0.0f, 0.260246f, 0.0f);
        this.Rclaw4 = new ModelRenderer((ModelBase)this, 16, 10);
        this.Rclaw4.func_78789_a(-1.0f, 14.0f, 2.0f, 0, 2, 3);
        this.Rclaw4.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.Rclaw4.func_78787_b(256, 128);
        this.Rclaw4.field_78809_i = true;
        this.setRotation(this.Rclaw4, 0.0f, 0.0f, 0.0f);
        this.Body3 = new ModelRenderer((ModelBase)this, 17, 96);
        this.Body3.func_78789_a(-3.0f, 0.0f, 0.0f, 6, 7, 3);
        this.Body3.func_78793_a(0.0f, 2.0f, 6.0f);
        this.Body3.func_78787_b(256, 128);
        this.Body3.field_78809_i = true;
        this.setRotation(this.Body3, 0.0f, 0.0f, 0.0f);
        this.Tail1 = new ModelRenderer((ModelBase)this, 33, 81);
        this.Tail1.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 0, 14);
        this.Tail1.func_78793_a(0.0f, 3.0f, 9.0f);
        this.Tail1.func_78787_b(256, 128);
        this.Tail1.field_78809_i = true;
        this.setRotation(this.Tail1, -0.5948578f, 0.0f, 0.0f);
        this.Tail2 = new ModelRenderer((ModelBase)this, 36, 97);
        this.Tail2.func_78789_a(-1.0f, 0.0f, 0.0f, 3, 0, 13);
        this.Tail2.func_78793_a(0.0f, 3.0f, 8.0f);
        this.Tail2.func_78787_b(256, 128);
        this.Tail2.field_78809_i = true;
        this.setRotation(this.Tail2, -0.5948578f, 0.3346075f, 0.0f);
        this.Tail3 = new ModelRenderer((ModelBase)this, 36, 97);
        this.Tail3.func_78789_a(-2.0f, 0.0f, 0.0f, 3, 0, 13);
        this.Tail3.func_78793_a(0.0f, 3.0f, 8.0f);
        this.Tail3.func_78787_b(256, 128);
        this.Tail3.field_78809_i = true;
        this.setRotation(this.Tail3, -0.5948578f, -0.3346145f, 0.0f);
        this.Body4 = new ModelRenderer((ModelBase)this, 17, 89);
        this.Body4.func_78789_a(-2.0f, 0.0f, 0.0f, 4, 3, 3);
        this.Body4.func_78793_a(0.0f, 6.0f, 7.0f);
        this.Body4.func_78787_b(256, 128);
        this.Body4.field_78809_i = true;
        this.setRotation(this.Body4, 1.003822f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 74, 48);
        this.head.func_78789_a(-1.0f, -24.0f, -7.0f, 2, 2, 4);
        this.head.func_78793_a(0.0f, 5.0f, -7.0f);
        this.head.func_78787_b(256, 128);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.leftleg = new ModelRenderer((ModelBase)this, 0, 16);
        this.leftleg.func_78789_a(-2.0f, 0.0f, -2.0f, 4, 6, 5);
        this.leftleg.func_78793_a(3.0f, 8.0f, 1.0f);
        this.leftleg.func_78787_b(256, 128);
        this.leftleg.field_78809_i = true;
        this.setRotation(this.leftleg, -0.2974289f, 0.0f, 0.0f);
        this.Neck1 = new ModelRenderer((ModelBase)this, 79, 84);
        this.Neck1.func_78789_a(-1.5f, -21.0f, -2.0f, 3, 21, 3);
        this.Neck1.func_78793_a(0.0f, 5.0f, -7.0f);
        this.Neck1.func_78787_b(256, 128);
        this.Neck1.field_78809_i = true;
        this.setRotation(this.Neck1, 0.0f, -0.0349066f, 0.0f);
        this.Head1 = new ModelRenderer((ModelBase)this, 0, 70);
        this.Head1.func_78789_a(-2.0f, -25.0f, -3.0f, 4, 4, 4);
        this.Head1.func_78793_a(0.0f, 5.0f, -7.0f);
        this.Head1.func_78787_b(256, 128);
        this.Head1.field_78809_i = true;
        this.setRotation(this.Head1, 0.0f, 0.0f, 0.0f);
        this.mouth1 = new ModelRenderer((ModelBase)this, 74, 64);
        this.mouth1.func_78789_a(-1.0f, -22.0f, -6.0f, 2, 1, 3);
        this.mouth1.func_78793_a(0.0f, 5.0f, -7.0f);
        this.mouth1.func_78787_b(256, 128);
        this.mouth1.field_78809_i = true;
        this.setRotation(this.mouth1, 0.0f, 0.0f, 0.0f);
        this.neck2 = new ModelRenderer((ModelBase)this, 0, 99);
        this.neck2.func_78789_a(-1.0f, -2.0f, -2.0f, 2, 4, 3);
        this.neck2.func_78793_a(0.0f, 5.0f, -6.9f);
        this.neck2.func_78787_b(256, 128);
        this.neck2.field_78809_i = true;
        this.setRotation(this.neck2, 0.0f, 0.0f, 0.0f);
        this.rightleg = new ModelRenderer((ModelBase)this, 0, 16);
        this.rightleg.func_78789_a(-3.0f, 0.0f, -2.0f, 4, 6, 5);
        this.rightleg.func_78793_a(-2.0f, 8.0f, 1.0f);
        this.rightleg.func_78787_b(256, 128);
        this.rightleg.field_78809_i = true;
        this.setRotation(this.rightleg, -0.2974216f, 0.0f, 0.0f);
        this.Lwing = new ModelRenderer((ModelBase)this, 0, 107);
        this.Lwing.func_78789_a(0.0f, 0.0f, 0.0f, 1, 7, 11);
        this.Lwing.func_78793_a(4.0f, 1.0f, -5.0f);
        this.Lwing.func_78787_b(256, 128);
        this.Lwing.field_78809_i = true;
        this.setRotation(this.Lwing, 0.0f, 0.0f, 0.0f);
        this.Rwing = new ModelRenderer((ModelBase)this, 0, 107);
        this.Rwing.func_78789_a(0.0f, 0.0f, 0.0f, 1, 7, 11);
        this.Rwing.func_78793_a(-5.0f, 1.0f, -5.0f);
        this.Rwing.func_78787_b(256, 128);
        this.Rwing.field_78809_i = true;
        this.setRotation(this.Rwing, 0.0f, 0.0f, 0.0f);
        this.Hat1 = new ModelRenderer((ModelBase)this, 40, 0);
        this.Hat1.func_78789_a(-2.5f, -26.0f, -4.0f, 5, 1, 5);
        this.Hat1.func_78793_a(0.0f, 5.0f, -7.0f);
        this.Hat1.func_78787_b(256, 128);
        this.Hat1.field_78809_i = true;
        this.setRotation(this.Hat1, 0.0f, 0.0f, 0.0f);
        this.Hat2 = new ModelRenderer((ModelBase)this, 40, 0);
        this.Hat2.func_78789_a(-2.0f, -28.0f, -3.0f, 4, 2, 4);
        this.Hat2.func_78793_a(0.0f, 5.0f, -7.0f);
        this.Hat2.func_78787_b(256, 128);
        this.Hat2.field_78809_i = true;
        this.setRotation(this.Hat2, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Ostrich o = (Ostrich)entity;
        RenderInfo r = null;
        float hf = 0.0f;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        float lspeed = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        lspeed = (float)((o.field_70169_q - o.field_70165_t) * (o.field_70169_q - o.field_70165_t) + (o.field_70166_s - o.field_70161_v) * (o.field_70166_s - o.field_70161_v));
        lspeed = (float)Math.sqrt(lspeed);
        newangle = MathHelper.func_76134_b((float)(f2 * 1.25f * this.wingspeed)) * (float)Math.PI * lspeed * 0.4f;
        if ((double)newangle > 0.5) {
            newangle = 0.75f;
        }
        if ((double)newangle < -0.5) {
            newangle = -0.75f;
        }
        this.leftleg.field_78795_f = -0.297f + newangle;
        this.LLeg1.field_78795_f = 0.483f + newangle;
        this.LLeg2.field_78795_f = -0.437f + newangle;
        this.Lfoot1.field_78795_f = newangle;
        this.Lfoot2.field_78795_f = newangle;
        this.Lfoot3.field_78795_f = newangle;
        this.Lfoot4.field_78795_f = newangle;
        this.LClaw1.field_78795_f = newangle;
        this.LClaw2.field_78795_f = newangle;
        this.LClaw3.field_78795_f = newangle;
        this.LClaw4.field_78795_f = newangle;
        this.rightleg.field_78795_f = -0.297f - newangle;
        this.Rleg1.field_78795_f = 0.483f - newangle;
        this.RLeg2.field_78795_f = -0.437f - newangle;
        this.Rfoot1.field_78795_f = -newangle;
        this.Rfoot2.field_78795_f = -newangle;
        this.Rfoot3.field_78795_f = -newangle;
        this.Rfoot4.field_78795_f = -newangle;
        this.Rclaw1.field_78795_f = -newangle;
        this.Rclaw2.field_78795_f = -newangle;
        this.Rclaw3.field_78795_f = -newangle;
        this.Rclaw4.field_78795_f = -newangle;
        this.Tail2.field_78795_f = this.Tail1.field_78795_f = -0.594f + MathHelper.func_76134_b((float)(f2 * 0.05f)) * (float)Math.PI * 0.06f;
        this.Tail3.field_78795_f = this.Tail1.field_78795_f;
        this.Tail3.field_78796_g = -0.334f + MathHelper.func_76134_b((float)(f2 * 0.061f)) * (float)Math.PI * 0.08f;
        this.Tail2.field_78796_g = 0.334f - MathHelper.func_76134_b((float)(f2 * 0.072f)) * (float)Math.PI * 0.08f;
        r = o.getRenderInfo();
        if (o.field_70153_n != null) {
            f3 = (o.field_70126_B - o.field_70177_z) * 20.0f;
            f3 = -f3;
            r.rf1 += (f3 - r.rf1) / 60.0f;
            if (r.rf1 > 50.0f) {
                r.rf1 = 50.0f;
            }
            if (r.rf1 < -50.0f) {
                r.rf1 = -50.0f;
            }
            f3 = r.rf1;
        } else {
            f3 /= 2.0f;
        }
        if (o.func_70906_o() && o.get_is_activated() == 0) {
            f3 = 0.0f;
            this.head.field_78795_f = this.Head1.field_78795_f = 3.1415f;
            this.mouth1.field_78795_f = this.Head1.field_78795_f;
            this.Neck1.field_78795_f = this.Head1.field_78795_f;
            this.Hat1.field_78795_f = this.Head1.field_78795_f;
            this.Hat2.field_78795_f = this.Head1.field_78795_f;
        } else {
            this.head.field_78795_f = this.Head1.field_78795_f = 0.0f;
            this.mouth1.field_78795_f = this.Head1.field_78795_f;
            this.Neck1.field_78795_f = this.Head1.field_78795_f;
            this.Hat1.field_78795_f = this.Head1.field_78795_f;
            this.Hat2.field_78795_f = this.Head1.field_78795_f;
        }
        this.head.field_78796_g = this.Head1.field_78796_g = (float)Math.toRadians(f3) * 0.65f;
        this.mouth1.field_78796_g = this.Head1.field_78796_g;
        this.Hat1.field_78796_g = this.Head1.field_78796_g;
        this.Hat2.field_78796_g = this.Head1.field_78796_g;
        newangle = MathHelper.func_76134_b((float)(f2 * 1.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        nextangle = MathHelper.func_76134_b((float)((f2 + 0.3f) * 1.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (o.field_70170_p.field_73012_v.nextInt(3) == 1) {
                r.ri1 = 1;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        newangle = Math.abs(newangle);
        this.Lwing.field_78808_h = -newangle;
        this.Lwing.field_78796_g = newangle / 2.0f;
        this.Rwing.field_78808_h = newangle;
        this.Rwing.field_78796_g = -newangle / 2.0f;
        o.setRenderInfo(r);
        this.Body1.func_78785_a(f5);
        this.body2.func_78785_a(f5);
        this.LLeg1.func_78785_a(f5);
        this.Rleg1.func_78785_a(f5);
        this.LLeg2.func_78785_a(f5);
        this.Lfoot1.func_78785_a(f5);
        this.RLeg2.func_78785_a(f5);
        this.Lfoot2.func_78785_a(f5);
        this.Lfoot3.func_78785_a(f5);
        this.LClaw1.func_78785_a(f5);
        this.LClaw2.func_78785_a(f5);
        this.LClaw3.func_78785_a(f5);
        this.Lfoot4.func_78785_a(f5);
        this.LClaw4.func_78785_a(f5);
        this.Rfoot1.func_78785_a(f5);
        this.Rfoot2.func_78785_a(f5);
        this.Rclaw1.func_78785_a(f5);
        this.Rfoot3.func_78785_a(f5);
        this.Rclaw3.func_78785_a(f5);
        this.Rfoot4.func_78785_a(f5);
        this.Rclaw2.func_78785_a(f5);
        this.Rclaw4.func_78785_a(f5);
        this.Body3.func_78785_a(f5);
        this.Tail1.func_78785_a(f5);
        this.Tail2.func_78785_a(f5);
        this.Tail3.func_78785_a(f5);
        this.Body4.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.leftleg.func_78785_a(f5);
        this.Neck1.func_78785_a(f5);
        this.Head1.func_78785_a(f5);
        this.mouth1.func_78785_a(f5);
        this.neck2.func_78785_a(f5);
        this.rightleg.func_78785_a(f5);
        this.Lwing.func_78785_a(f5);
        this.Rwing.func_78785_a(f5);
        if (o instanceof EntityCannonFodder && o.get_is_activated() != 0) {
            this.Hat1.func_78785_a(f5);
            if (o.get_is_activated() > 1) {
                this.Hat2.func_78785_a(f5);
            }
        }
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

