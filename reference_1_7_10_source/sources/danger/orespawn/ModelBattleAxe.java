/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelBattleAxe
extends ModelBase {
    ModelRenderer Handle1;
    ModelRenderer Head1;
    ModelRenderer Grip;
    ModelRenderer Pin;
    ModelRenderer Top;
    ModelRenderer Blade1;
    ModelRenderer Blade2;
    ModelRenderer Blade3;
    ModelRenderer Blade4;
    ModelRenderer Blade5;
    ModelRenderer Blade6;
    ModelRenderer Blade7;
    ModelRenderer Blade8;
    ModelRenderer Blade9;
    ModelRenderer Blade10;

    public ModelBattleAxe() {
        this.field_78090_t = 128;
        this.field_78089_u = 64;
        this.Handle1 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Handle1.func_78789_a(-7.0f, -0.5f, 0.0f, 31, 2, 1);
        this.Handle1.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Handle1.func_78787_b(128, 64);
        this.Handle1.field_78809_i = true;
        this.setRotation(this.Handle1, 0.0f, 0.0f, 1.570796f);
        this.Head1 = new ModelRenderer((ModelBase)this, 29, 18);
        this.Head1.func_78789_a(-2.0f, -4.5f, -0.5f, 3, 4, 2);
        this.Head1.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Head1.func_78787_b(128, 64);
        this.Head1.field_78809_i = true;
        this.setRotation(this.Head1, 0.0f, 0.0f, 0.0f);
        this.Grip = new ModelRenderer((ModelBase)this, 0, 7);
        this.Grip.func_78789_a(-1.92f, 13.0f, -0.5f, 3, 11, 2);
        this.Grip.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Grip.func_78787_b(128, 64);
        this.Grip.field_78809_i = true;
        this.setRotation(this.Grip, 0.0f, 0.0f, 0.0f);
        this.Pin = new ModelRenderer((ModelBase)this, 38, 11);
        this.Pin.func_78789_a(-1.0f, -3.0f, -1.0f, 1, 1, 3);
        this.Pin.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Pin.func_78787_b(128, 64);
        this.Pin.field_78809_i = true;
        this.setRotation(this.Pin, 0.0f, 0.0f, 0.0f);
        this.Top = new ModelRenderer((ModelBase)this, 24, 11);
        this.Top.func_78789_a(-2.0f, -8.0f, -0.5f, 3, 2, 2);
        this.Top.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Top.func_78787_b(128, 64);
        this.Top.field_78809_i = true;
        this.setRotation(this.Top, 0.0f, 0.0f, 0.0f);
        this.Blade1 = new ModelRenderer((ModelBase)this, 70, 0);
        this.Blade1.func_78789_a(6.0f, -8.0f, 0.0f, 3, 10, 1);
        this.Blade1.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade1.func_78787_b(128, 64);
        this.Blade1.field_78809_i = true;
        this.setRotation(this.Blade1, 0.0f, 0.0f, 0.5061455f);
        this.Blade2 = new ModelRenderer((ModelBase)this, 70, 0);
        this.Blade2.func_78789_a(8.5f, -6.9f, 0.0f, 3, 10, 1);
        this.Blade2.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade2.func_78787_b(128, 64);
        this.Blade2.field_78809_i = true;
        this.setRotation(this.Blade2, 0.0f, 0.0f, -0.5061455f);
        this.Blade3 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Blade3.func_78789_a(-1.5f, -3.0f, 0.0f, 10, 1, 1);
        this.Blade3.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade3.func_78787_b(128, 64);
        this.Blade3.field_78809_i = true;
        this.setRotation(this.Blade3, 0.0f, 0.0f, 0.0f);
        this.Blade4 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Blade4.func_78789_a(-1.0f, -2.0f, 0.0f, 7, 1, 1);
        this.Blade4.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade4.func_78787_b(128, 64);
        this.Blade4.field_78809_i = true;
        this.setRotation(this.Blade4, 0.0f, 0.0f, 0.5061455f);
        this.Blade5 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Blade5.func_78789_a(0.5f, -3.5f, 0.0f, 8, 1, 1);
        this.Blade5.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade5.func_78787_b(128, 64);
        this.Blade5.field_78809_i = true;
        this.setRotation(this.Blade5, 0.0f, 0.0f, -0.5061455f);
        this.Blade6 = new ModelRenderer((ModelBase)this, 70, 0);
        this.Blade6.func_78789_a(-12.2f, -5.2f, 0.0f, 3, 10, 1);
        this.Blade6.func_78793_a(0.0f, -13.0f, 0.0f);
        this.Blade6.func_78787_b(128, 64);
        this.Blade6.field_78809_i = true;
        this.setRotation(this.Blade6, 0.0f, 0.0f, 0.5061455f);
        this.Blade7 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Blade7.func_78789_a(-9.9f, -3.0f, 0.0f, 8, 1, 1);
        this.Blade7.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade7.func_78787_b(128, 64);
        this.Blade7.field_78809_i = true;
        this.setRotation(this.Blade7, 0.0f, 0.0f, 0.5061455f);
        this.Blade8 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Blade8.func_78789_a(-10.0f, -3.0f, 0.0f, 10, 1, 1);
        this.Blade8.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade8.func_78787_b(128, 64);
        this.Blade8.field_78809_i = true;
        this.setRotation(this.Blade8, 0.0f, 0.0f, 0.0f);
        this.Blade9 = new ModelRenderer((ModelBase)this, 70, 0);
        this.Blade9.func_78789_a(-10.0f, -8.5f, 0.0f, 3, 10, 1);
        this.Blade9.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade9.func_78787_b(128, 64);
        this.Blade9.field_78809_i = true;
        this.setRotation(this.Blade9, 0.0f, 0.0f, -0.5061455f);
        this.Blade10 = new ModelRenderer((ModelBase)this, 0, 0);
        this.Blade10.func_78789_a(-7.0f, -2.5f, 0.0f, 7, 1, 1);
        this.Blade10.func_78793_a(0.0f, -12.0f, 0.0f);
        this.Blade10.func_78787_b(128, 64);
        this.Blade10.field_78809_i = true;
        this.setRotation(this.Blade10, 0.0f, 0.0f, -0.5061455f);
    }

    public void render() {
        float f5 = 1.0f;
        this.Handle1.func_78785_a(f5);
        this.Head1.func_78785_a(f5);
        this.Grip.func_78785_a(f5);
        this.Pin.func_78785_a(f5);
        this.Top.func_78785_a(f5);
        this.Blade1.func_78785_a(f5);
        this.Blade2.func_78785_a(f5);
        this.Blade3.func_78785_a(f5);
        this.Blade4.func_78785_a(f5);
        this.Blade5.func_78785_a(f5);
        this.Blade6.func_78785_a(f5);
        this.Blade7.func_78785_a(f5);
        this.Blade8.func_78785_a(f5);
        this.Blade9.func_78785_a(f5);
        this.Blade10.func_78785_a(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }
}

