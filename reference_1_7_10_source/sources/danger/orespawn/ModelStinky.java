/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Stinky;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelStinky
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer body;
    ModelRenderer neck1;
    ModelRenderer neck;
    ModelRenderer neckbase;
    ModelRenderer head;
    ModelRenderer Rleg1;
    ModelRenderer Lleg1;
    ModelRenderer Lhorn1;
    ModelRenderer Rhorn1;
    ModelRenderer snout;
    ModelRenderer Lhorn2;
    ModelRenderer Rhorn2;
    ModelRenderer tail1;
    ModelRenderer Rleg2;
    ModelRenderer Lleg2;
    ModelRenderer tail2;
    ModelRenderer tail3;
    ModelRenderer tail4;
    ModelRenderer Lwing;
    ModelRenderer Rwing;

    public ModelStinky(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 128;
        this.field_78089_u = 64;
        this.body = new ModelRenderer((ModelBase)this, 0, 12);
        this.body.func_78789_a(-4.5f, -3.0f, -5.0f, 8, 8, 10);
        this.body.func_78793_a(0.5f, 15.0f, 1.0f);
        this.body.func_78787_b(128, 64);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.neck1 = new ModelRenderer((ModelBase)this, 0, 31);
        this.neck1.func_78789_a(-2.0f, -3.0f, -2.0f, 4, 5, 5);
        this.neck1.func_78793_a(0.0f, 16.0f, -5.0f);
        this.neck1.func_78787_b(128, 64);
        this.neck1.field_78809_i = true;
        this.setRotation(this.neck1, 0.715585f, 0.0f, 0.0f);
        this.neck = new ModelRenderer((ModelBase)this, 0, 42);
        this.neck.func_78789_a(-2.0f, -8.0f, -3.0f, 4, 8, 4);
        this.neck.func_78793_a(0.0f, 15.0f, -5.5f);
        this.neck.func_78787_b(128, 64);
        this.neck.field_78809_i = true;
        this.setRotation(this.neck, 0.0f, 0.0f, 0.0f);
        this.neckbase = new ModelRenderer((ModelBase)this, 0, 55);
        this.neckbase.func_78789_a(-3.0f, -4.0f, 0.0f, 6, 6, 3);
        this.neckbase.func_78793_a(0.0f, 17.0f, 5.0f);
        this.neckbase.func_78787_b(128, 64);
        this.neckbase.field_78809_i = true;
        this.setRotation(this.neckbase, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 0, 0);
        this.head.func_78789_a(-2.5f, -10.0f, -3.5f, 5, 5, 5);
        this.head.func_78793_a(0.0f, 15.0f, -5.5f);
        this.head.func_78787_b(128, 64);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.Rleg1 = new ModelRenderer((ModelBase)this, 19, 53);
        this.Rleg1.func_78789_a(-1.5f, 0.0f, -1.0f, 3, 8, 3);
        this.Rleg1.func_78793_a(2.0f, 16.0f, 5.5f);
        this.Rleg1.func_78787_b(128, 64);
        this.Rleg1.field_78809_i = true;
        this.setRotation(this.Rleg1, 0.0f, 0.0f, 0.0f);
        this.Lleg1 = new ModelRenderer((ModelBase)this, 19, 53);
        this.Lleg1.func_78789_a(-1.5f, 0.0f, -0.5f, 3, 8, 3);
        this.Lleg1.func_78793_a(-2.0f, 16.0f, 5.0f);
        this.Lleg1.func_78787_b(128, 64);
        this.Lleg1.field_78809_i = true;
        this.setRotation(this.Lleg1, 0.0f, 0.0f, 0.0f);
        this.Lhorn1 = new ModelRenderer((ModelBase)this, 19, 47);
        this.Lhorn1.func_78789_a(-3.0f, -10.5f, -1.0f, 2, 2, 3);
        this.Lhorn1.func_78793_a(0.0f, 15.0f, -5.5f);
        this.Lhorn1.func_78787_b(128, 64);
        this.Lhorn1.field_78809_i = true;
        this.setRotation(this.Lhorn1, 0.0f, 0.0f, 0.0f);
        this.Rhorn1 = new ModelRenderer((ModelBase)this, 19, 47);
        this.Rhorn1.func_78789_a(1.0f, -10.5f, -1.0f, 2, 2, 3);
        this.Rhorn1.func_78793_a(0.0f, 15.0f, -5.5f);
        this.Rhorn1.func_78787_b(128, 64);
        this.Rhorn1.field_78809_i = true;
        this.setRotation(this.Rhorn1, 0.0f, 0.0f, 0.0f);
        this.snout = new ModelRenderer((ModelBase)this, 32, 57);
        this.snout.func_78789_a(-1.5f, -8.0f, -6.5f, 3, 3, 4);
        this.snout.func_78793_a(0.0f, 15.0f, -5.5f);
        this.snout.func_78787_b(128, 64);
        this.snout.field_78809_i = true;
        this.setRotation(this.snout, 0.0f, 0.0f, 0.0f);
        this.Lhorn2 = new ModelRenderer((ModelBase)this, 19, 42);
        this.Lhorn2.func_78789_a(-2.5f, -10.0f, 1.0f, 1, 1, 3);
        this.Lhorn2.func_78793_a(0.0f, 15.0f, -5.5f);
        this.Lhorn2.func_78787_b(128, 64);
        this.Lhorn2.field_78809_i = true;
        this.setRotation(this.Lhorn2, 0.0f, 0.0f, 0.0f);
        this.Rhorn2 = new ModelRenderer((ModelBase)this, 19, 42);
        this.Rhorn2.func_78789_a(1.5f, -10.0f, 1.0f, 1, 1, 3);
        this.Rhorn2.func_78793_a(0.0f, 15.0f, -5.5f);
        this.Rhorn2.func_78787_b(128, 64);
        this.Rhorn2.field_78809_i = true;
        this.setRotation(this.Rhorn2, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 47, 55);
        this.tail1.func_78789_a(-3.0f, -3.0f, -3.0f, 6, 6, 3);
        this.tail1.func_78793_a(0.0f, 16.5f, -2.0f);
        this.tail1.func_78787_b(128, 64);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.0f, 0.0f, 0.0f);
        this.Rleg2 = new ModelRenderer((ModelBase)this, 19, 53);
        this.Rleg2.func_78789_a(-1.5f, 0.0f, -1.5f, 3, 8, 3);
        this.Rleg2.func_78793_a(2.0f, 16.0f, -3.0f);
        this.Rleg2.func_78787_b(128, 64);
        this.Rleg2.field_78809_i = true;
        this.setRotation(this.Rleg2, 0.0f, 0.0f, 0.0f);
        this.Lleg2 = new ModelRenderer((ModelBase)this, 19, 53);
        this.Lleg2.func_78789_a(-1.5f, 0.0f, -1.5f, 3, 8, 3);
        this.Lleg2.func_78793_a(-2.0f, 16.0f, -3.0f);
        this.Lleg2.func_78787_b(128, 64);
        this.Lleg2.field_78809_i = true;
        this.setRotation(this.Lleg2, 0.0f, 0.0f, 0.0f);
        this.tail2 = new ModelRenderer((ModelBase)this, 19, 31);
        this.tail2.func_78789_a(-2.5f, -2.5f, 0.0f, 5, 5, 5);
        this.tail2.func_78793_a(0.0f, 16.0f, 7.0f);
        this.tail2.func_78787_b(128, 64);
        this.tail2.field_78809_i = true;
        this.setRotation(this.tail2, -0.3839724f, 0.0f, 0.0f);
        this.tail3 = new ModelRenderer((ModelBase)this, 32, 46);
        this.tail3.func_78789_a(-2.0f, -2.0f, 0.0f, 4, 4, 4);
        this.tail3.func_78793_a(0.0f, 17.2f, 11.0f);
        this.tail3.func_78787_b(128, 64);
        this.tail3.field_78809_i = true;
        this.setRotation(this.tail3, -0.2094395f, 0.0f, 0.0f);
        this.tail4 = new ModelRenderer((ModelBase)this, 37, 13);
        this.tail4.func_78789_a(-1.5f, -1.5f, 0.0f, 3, 3, 5);
        this.tail4.func_78793_a(0.0f, 17.5f, 14.0f);
        this.tail4.func_78787_b(128, 64);
        this.tail4.field_78809_i = true;
        this.setRotation(this.tail4, -0.0698132f, 0.0f, 0.0f);
        this.Lwing = new ModelRenderer((ModelBase)this, 59, 0);
        this.Lwing.func_78789_a(-18.0f, 0.0f, -5.0f, 18, 0, 10);
        this.Lwing.func_78793_a(-2.0f, 12.6f, 0.0f);
        this.Lwing.func_78787_b(128, 64);
        this.Lwing.field_78809_i = true;
        this.setRotation(this.Lwing, 0.0f, 0.0f, 0.4014257f);
        this.Rwing = new ModelRenderer((ModelBase)this, 59, 11);
        this.Rwing.func_78789_a(0.0f, 0.0f, -5.0f, 18, 0, 10);
        this.Rwing.func_78793_a(2.0f, 12.6f, 0.0f);
        this.Rwing.func_78787_b(128, 64);
        this.Rwing.field_78809_i = true;
        this.setRotation(this.Rwing, 0.0f, 0.0f, -0.4014257f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Stinky c = (Stinky)entity;
        float hf = 0.0f;
        float newangle = 0.0f;
        int current_activity = c.getActivity();
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 2.3f * this.wingspeed)) * (float)Math.PI * 0.4f * f1 : 0.0f;
        this.Rwing.field_78808_h = newangle - 0.4f;
        this.Lwing.field_78808_h = -newangle + 0.4f;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 2.0f * this.wingspeed)) * (float)Math.PI * 0.25f * f1 : 0.0f;
        if (current_activity != 2) {
            this.Rleg1.field_78795_f = newangle;
            this.Lleg1.field_78795_f = -newangle;
            this.Rleg2.field_78795_f = -newangle;
            this.Lleg2.field_78795_f = newangle;
        } else {
            this.Rleg2.field_78795_f = newangle = -1.0f;
            this.Lleg2.field_78795_f = newangle;
            this.Rleg1.field_78795_f = newangle = 1.0f;
            this.Lleg1.field_78795_f = newangle;
        }
        newangle = MathHelper.func_76134_b((float)(f2 * 1.0f * this.wingspeed)) * (float)Math.PI * 0.2f;
        if (c.func_70906_o()) {
            newangle = 0.0f;
        }
        this.tail2.field_78796_g = newangle;
        this.tail3.field_78798_e = this.tail2.field_78798_e + (float)Math.cos(this.tail2.field_78796_g) * 4.0f;
        this.tail3.field_78800_c = this.tail2.field_78800_c + (float)Math.sin(this.tail2.field_78796_g) * 4.0f - 0.5f;
        this.tail3.field_78796_g = newangle * 1.6f;
        this.tail4.field_78798_e = this.tail3.field_78798_e + (float)Math.cos(this.tail3.field_78796_g) * 3.0f;
        this.tail4.field_78800_c = this.tail3.field_78800_c + (float)Math.sin(this.tail3.field_78796_g) * 3.0f - 0.5f;
        this.tail4.field_78796_g = newangle * 2.6f;
        this.head.field_78796_g = (float)Math.toRadians(f3);
        this.snout.field_78796_g = (float)Math.toRadians(f3);
        this.neck.field_78796_g = (float)Math.toRadians(f3) / 2.0f;
        this.Rhorn1.field_78796_g = (float)Math.toRadians(f3);
        this.Rhorn2.field_78796_g = (float)Math.toRadians(f3);
        this.Lhorn1.field_78796_g = (float)Math.toRadians(f3);
        this.Lhorn2.field_78796_g = (float)Math.toRadians(f3);
        this.head.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.snout.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.neck.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.Rhorn1.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.Rhorn2.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.Lhorn1.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.Lhorn2.field_78795_f = (float)Math.toRadians(f4) / 3.0f;
        this.body.func_78785_a(f5);
        this.neck1.func_78785_a(f5);
        this.neck.func_78785_a(f5);
        this.neckbase.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.Rleg1.func_78785_a(f5);
        this.Lleg1.func_78785_a(f5);
        this.Lhorn1.func_78785_a(f5);
        this.Rhorn1.func_78785_a(f5);
        this.snout.func_78785_a(f5);
        this.Lhorn2.func_78785_a(f5);
        this.Rhorn2.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.Rleg2.func_78785_a(f5);
        this.Lleg2.func_78785_a(f5);
        this.tail2.func_78785_a(f5);
        this.tail3.func_78785_a(f5);
        this.tail4.func_78785_a(f5);
        this.Lwing.func_78785_a(f5);
        this.Rwing.func_78785_a(f5);
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

