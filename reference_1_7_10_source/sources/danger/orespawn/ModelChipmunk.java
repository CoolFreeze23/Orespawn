/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Chipmunk;
import danger.orespawn.EntityCannonFodder;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelChipmunk
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Cheek2;
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Tail2;
    ModelRenderer Neck;
    ModelRenderer Head;
    ModelRenderer MouthUnder;
    ModelRenderer Cheek1;
    ModelRenderer Ear2;
    ModelRenderer Nose;
    ModelRenderer Ear1;
    ModelRenderer Body;
    ModelRenderer BodyTail;
    ModelRenderer Tail1;
    ModelRenderer Hat1;
    ModelRenderer Hat2;

    public ModelChipmunk(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.Cheek2 = new ModelRenderer((ModelBase)this, 14, 0);
        this.Cheek2.func_78789_a(0.5f, -1.5f, -3.5f, 2, 2, 2);
        this.Cheek2.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Cheek2.func_78787_b(64, 32);
        this.Cheek2.field_78809_i = true;
        this.setRotation(this.Cheek2, 0.0f, 0.0f, 0.0f);
        this.Leg1 = new ModelRenderer((ModelBase)this, 22, 7);
        this.Leg1.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.Leg1.func_78793_a(-2.0f, 23.0f, -4.0f);
        this.Leg1.func_78787_b(64, 32);
        this.Leg1.field_78809_i = true;
        this.setRotation(this.Leg1, 0.0f, 0.0f, 0.0f);
        this.Leg2 = new ModelRenderer((ModelBase)this, 22, 9);
        this.Leg2.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.Leg2.func_78793_a(1.0f, 23.0f, -4.0f);
        this.Leg2.func_78787_b(64, 32);
        this.Leg2.field_78809_i = true;
        this.setRotation(this.Leg2, 0.0f, 0.0f, 0.0f);
        this.Leg3 = new ModelRenderer((ModelBase)this, 22, 11);
        this.Leg3.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.Leg3.func_78793_a(1.0f, 23.0f, 0.0f);
        this.Leg3.func_78787_b(64, 32);
        this.Leg3.field_78809_i = true;
        this.setRotation(this.Leg3, 0.0f, 0.0f, 0.0f);
        this.Leg4 = new ModelRenderer((ModelBase)this, 22, 13);
        this.Leg4.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.Leg4.func_78793_a(-2.0f, 23.0f, 0.0f);
        this.Leg4.func_78787_b(64, 32);
        this.Leg4.field_78809_i = true;
        this.setRotation(this.Leg4, 0.0f, 0.0f, 0.0f);
        this.Tail2 = new ModelRenderer((ModelBase)this, 28, 15);
        this.Tail2.func_78789_a(-0.5f, 1.0f, 2.5f, 3, 3, 4);
        this.Tail2.func_78793_a(-1.0f, 20.0f, 1.0f);
        this.Tail2.func_78787_b(64, 32);
        this.Tail2.field_78809_i = true;
        this.setRotation(this.Tail2, 0.7662421f, 0.0f, 0.0f);
        this.Neck = new ModelRenderer((ModelBase)this, 26, 9);
        this.Neck.func_78789_a(0.0f, 0.0f, 0.0f, 3, 2, 4);
        this.Neck.func_78793_a(-1.5f, 22.0f, -5.0f);
        this.Neck.func_78787_b(64, 32);
        this.Neck.field_78809_i = true;
        this.setRotation(this.Neck, 1.570796f, 0.0f, 0.0f);
        this.Head = new ModelRenderer((ModelBase)this, 0, 0);
        this.Head.func_78789_a(-2.0f, -3.0f, 0.0f, 4, 4, 3);
        this.Head.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Head.func_78787_b(64, 32);
        this.Head.field_78809_i = true;
        this.setRotation(this.Head, 1.570796f, 0.0f, 0.0f);
        this.MouthUnder = new ModelRenderer((ModelBase)this, 20, 4);
        this.MouthUnder.func_78789_a(-1.0f, -1.9f, -3.8f, 2, 2, 1);
        this.MouthUnder.func_78793_a(0.0f, 20.0f, -3.0f);
        this.MouthUnder.func_78787_b(64, 32);
        this.MouthUnder.field_78809_i = true;
        this.setRotation(this.MouthUnder, 0.0f, 0.0f, 0.0f);
        this.Cheek1 = new ModelRenderer((ModelBase)this, 22, 0);
        this.Cheek1.func_78789_a(-2.5f, -1.5f, -3.5f, 2, 2, 2);
        this.Cheek1.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Cheek1.func_78787_b(64, 32);
        this.Cheek1.field_78809_i = true;
        this.setRotation(this.Cheek1, 0.0f, 0.0f, 0.0f);
        this.Ear2 = new ModelRenderer((ModelBase)this, 18, 11);
        this.Ear2.func_78789_a(1.0f, 0.0f, 3.0f, 1, 1, 1);
        this.Ear2.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Ear2.func_78787_b(64, 32);
        this.Ear2.field_78809_i = true;
        this.setRotation(this.Ear2, 1.570796f, 0.0f, 0.0f);
        this.Nose = new ModelRenderer((ModelBase)this, 18, 7);
        this.Nose.func_78789_a(-0.5f, -2.0f, -4.2f, 1, 1, 1);
        this.Nose.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Nose.func_78787_b(64, 32);
        this.Nose.field_78809_i = true;
        this.setRotation(this.Nose, 0.0f, 0.0f, 0.0f);
        this.Ear1 = new ModelRenderer((ModelBase)this, 18, 9);
        this.Ear1.func_78789_a(-2.0f, 0.0f, 3.0f, 1, 1, 1);
        this.Ear1.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Ear1.func_78787_b(64, 32);
        this.Ear1.field_78809_i = true;
        this.setRotation(this.Ear1, 1.570796f, 0.0f, 0.0f);
        this.Body = new ModelRenderer((ModelBase)this, 0, 7);
        this.Body.func_78789_a(0.0f, 0.0f, 0.0f, 4, 3, 5);
        this.Body.func_78793_a(-2.0f, 20.0f, -4.0f);
        this.Body.func_78787_b(64, 32);
        this.Body.field_78809_i = true;
        this.setRotation(this.Body, 0.0f, 0.0f, 0.0f);
        this.BodyTail = new ModelRenderer((ModelBase)this, 0, 15);
        this.BodyTail.func_78789_a(0.0f, 0.0f, 0.0f, 5, 4, 3);
        this.BodyTail.func_78793_a(-2.5f, 19.0f, -1.0f);
        this.BodyTail.func_78787_b(64, 32);
        this.BodyTail.field_78809_i = true;
        this.setRotation(this.BodyTail, 0.0f, 0.0f, 0.0f);
        this.Tail1 = new ModelRenderer((ModelBase)this, 16, 15);
        this.Tail1.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 4);
        this.Tail1.func_78793_a(-1.0f, 20.0f, 1.0f);
        this.Tail1.func_78787_b(64, 32);
        this.Tail1.field_78809_i = true;
        this.setRotation(this.Tail1, 0.3064968f, 0.0f, 0.0f);
        this.Hat1 = new ModelRenderer((ModelBase)this, 40, 0);
        this.Hat1.func_78789_a(-2.5f, -4.0f, -4.0f, 5, 1, 5);
        this.Hat1.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Hat1.func_78787_b(64, 32);
        this.Hat1.field_78809_i = true;
        this.setRotation(this.Hat1, 0.0f, 0.0f, 0.0f);
        this.Hat2 = new ModelRenderer((ModelBase)this, 40, 0);
        this.Hat2.func_78789_a(-2.0f, -6.0f, -3.0f, 4, 2, 4);
        this.Hat2.func_78793_a(0.0f, 20.0f, -3.0f);
        this.Hat2.func_78787_b(64, 32);
        this.Hat2.field_78809_i = true;
        this.setRotation(this.Hat2, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Chipmunk c = (Chipmunk)entity;
        float hf = 0.0f;
        float newangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 2.3f * this.wingspeed)) * (float)Math.PI * 0.25f * f1 : 0.0f;
        this.Leg1.field_78795_f = newangle;
        this.Leg3.field_78795_f = newangle;
        this.Leg2.field_78795_f = -newangle;
        this.Leg4.field_78795_f = -newangle;
        this.Nose.field_78796_g = this.Head.field_78796_g = (float)Math.toRadians(f3) * 0.45f;
        this.Ear1.field_78796_g = this.Head.field_78796_g;
        this.Ear2.field_78796_g = this.Head.field_78796_g;
        this.MouthUnder.field_78796_g = this.Head.field_78796_g;
        this.Cheek1.field_78796_g = this.Head.field_78796_g;
        this.Cheek2.field_78796_g = this.Head.field_78796_g;
        this.Hat1.field_78796_g = this.Head.field_78796_g;
        this.Hat2.field_78796_g = this.Head.field_78796_g;
        if (!c.func_70906_o()) {
            this.Tail1.field_78795_f = 0.306f + MathHelper.func_76134_b((float)(f2 * 0.25f)) * (float)Math.PI * 0.06f;
            newangle = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.25f * f1;
            this.Tail1.field_78795_f += newangle;
            this.Tail2.field_78795_f = 0.306f + this.Tail1.field_78795_f;
        }
        this.Cheek2.func_78785_a(f5);
        this.Leg1.func_78785_a(f5);
        this.Leg2.func_78785_a(f5);
        this.Leg3.func_78785_a(f5);
        this.Leg4.func_78785_a(f5);
        this.Tail2.func_78785_a(f5);
        this.Neck.func_78785_a(f5);
        this.Head.func_78785_a(f5);
        this.MouthUnder.func_78785_a(f5);
        this.Cheek1.func_78785_a(f5);
        this.Ear2.func_78785_a(f5);
        this.Nose.func_78785_a(f5);
        this.Ear1.func_78785_a(f5);
        this.Body.func_78785_a(f5);
        this.BodyTail.func_78785_a(f5);
        this.Tail1.func_78785_a(f5);
        if (c instanceof EntityCannonFodder && c.get_is_activated() != 0) {
            this.Hat1.func_78785_a(f5);
            if (c.get_is_activated() > 1) {
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

