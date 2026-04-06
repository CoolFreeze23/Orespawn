/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelSlice
extends ModelBase {
    ModelRenderer Grip;
    ModelRenderer Blade1;
    ModelRenderer Handguard2;
    ModelRenderer Handguard1;
    ModelRenderer hg2;
    ModelRenderer hg4;
    ModelRenderer hg3;
    ModelRenderer hg1;
    ModelRenderer BaseGrip;
    ModelRenderer Bottom;
    ModelRenderer Blade2;
    ModelRenderer Blade3;
    ModelRenderer Blade4;
    ModelRenderer Shape1;

    public ModelSlice() {
        this.field_78090_t = 64;
        this.field_78089_u = 128;
        this.Grip = new ModelRenderer((ModelBase)this, 0, 0);
        this.Grip.func_78789_a(0.0f, -6.0f, 0.0f, 1, 12, 1);
        this.Grip.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Grip.func_78787_b(64, 128);
        this.Grip.field_78809_i = true;
        this.setRotation(this.Grip, 0.0f, 0.0f, 0.0f);
        this.Blade1 = new ModelRenderer((ModelBase)this, 6, 49);
        this.Blade1.func_78789_a(0.0f, -41.0f, 0.0f, 1, 34, 3);
        this.Blade1.func_78793_a(0.5f, 0.0f, -2.3f);
        this.Blade1.func_78787_b(64, 128);
        this.Blade1.field_78809_i = true;
        this.setRotation(this.Blade1, 0.0f, 0.3490659f, 0.0f);
        this.Handguard2 = new ModelRenderer((ModelBase)this, 16, 0);
        this.Handguard2.func_78789_a(0.0f, -7.0f, -4.0f, 1, 1, 9);
        this.Handguard2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Handguard2.func_78787_b(64, 128);
        this.Handguard2.field_78809_i = true;
        this.setRotation(this.Handguard2, 0.0f, 0.0f, 0.0f);
        this.Handguard1 = new ModelRenderer((ModelBase)this, 18, 12);
        this.Handguard1.func_78789_a(-3.0f, -7.0f, 0.0f, 7, 1, 1);
        this.Handguard1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Handguard1.func_78787_b(64, 128);
        this.Handguard1.field_78809_i = true;
        this.setRotation(this.Handguard1, 0.0f, 0.0f, 0.0f);
        this.hg2 = new ModelRenderer((ModelBase)this, 0, 15);
        this.hg2.func_78789_a(0.0f, -9.0f, -7.0f, 1, 3, 3);
        this.hg2.func_78793_a(0.5f, 0.0f, 0.0f);
        this.hg2.func_78787_b(64, 128);
        this.hg2.field_78809_i = true;
        this.setRotation(this.hg2, 0.0f, 0.0f, 0.0f);
        this.hg4 = new ModelRenderer((ModelBase)this, 0, 22);
        this.hg4.func_78789_a(0.0f, -9.0f, 5.0f, 1, 3, 3);
        this.hg4.func_78793_a(0.5f, 0.0f, 0.0f);
        this.hg4.func_78787_b(64, 128);
        this.hg4.field_78809_i = true;
        this.setRotation(this.hg4, 0.0f, 0.0f, 0.0f);
        this.hg3 = new ModelRenderer((ModelBase)this, 0, 29);
        this.hg3.func_78789_a(-4.0f, -9.0f, 0.0f, 3, 3, 1);
        this.hg3.func_78793_a(-2.0f, 0.0f, 0.5f);
        this.hg3.func_78787_b(64, 128);
        this.hg3.field_78809_i = true;
        this.setRotation(this.hg3, 0.0f, 0.0f, 0.0f);
        this.hg1 = new ModelRenderer((ModelBase)this, 0, 34);
        this.hg1.func_78789_a(4.0f, -9.0f, 0.0f, 3, 3, 1);
        this.hg1.func_78793_a(0.0f, 0.0f, 0.5f);
        this.hg1.func_78787_b(64, 128);
        this.hg1.field_78809_i = true;
        this.setRotation(this.hg1, 0.0f, 0.0f, 0.0f);
        this.BaseGrip = new ModelRenderer((ModelBase)this, 0, 39);
        this.BaseGrip.func_78789_a(-1.0f, 5.0f, -1.0f, 3, 1, 3);
        this.BaseGrip.func_78793_a(0.0f, 0.0f, 0.0f);
        this.BaseGrip.func_78787_b(64, 128);
        this.BaseGrip.field_78809_i = true;
        this.setRotation(this.BaseGrip, 0.0f, 0.0f, 0.0f);
        this.Bottom = new ModelRenderer((ModelBase)this, 0, 45);
        this.Bottom.func_78789_a(0.0f, 6.0f, 0.0f, 1, 1, 1);
        this.Bottom.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Bottom.func_78787_b(64, 128);
        this.Bottom.field_78809_i = true;
        this.setRotation(this.Bottom, 0.0f, 0.0f, 0.0f);
        this.Blade2 = new ModelRenderer((ModelBase)this, 24, 49);
        this.Blade2.func_78789_a(-1.0f, -41.0f, 0.0f, 1, 34, 3);
        this.Blade2.func_78793_a(0.5f, 0.0f, -2.3f);
        this.Blade2.func_78787_b(64, 128);
        this.Blade2.field_78809_i = true;
        this.setRotation(this.Blade2, 0.0f, -0.3490659f, 0.0f);
        this.Blade3 = new ModelRenderer((ModelBase)this, 15, 49);
        this.Blade3.func_78789_a(0.0f, -41.0f, 0.0f, 1, 34, 3);
        this.Blade3.func_78793_a(1.5f, 0.0f, 0.4f);
        this.Blade3.func_78787_b(64, 128);
        this.Blade3.field_78809_i = true;
        this.setRotation(this.Blade3, 0.0f, -0.3490659f, 0.0f);
        this.Blade4 = new ModelRenderer((ModelBase)this, 33, 49);
        this.Blade4.func_78789_a(0.0f, -41.0f, 0.0f, 1, 34, 3);
        this.Blade4.func_78793_a(-1.5f, 0.0f, 0.7f);
        this.Blade4.func_78787_b(64, 128);
        this.Blade4.field_78809_i = true;
        this.setRotation(this.Blade4, 0.0f, 0.3490659f, 0.0f);
        this.Shape1 = new ModelRenderer((ModelBase)this, 6, 0);
        this.Shape1.func_78789_a(0.0f, -6.0f, 0.0f, 1, 6, 3);
        this.Shape1.func_78793_a(0.5f, -40.0f, -1.0f);
        this.Shape1.func_78787_b(64, 128);
        this.Shape1.field_78809_i = true;
        this.setRotation(this.Shape1, 0.0f, 0.0f, 0.0f);
    }

    public void render() {
        float f5 = 1.0f;
        this.Grip.func_78785_a(f5);
        this.Blade1.func_78785_a(f5);
        this.Handguard2.func_78785_a(f5);
        this.Handguard1.func_78785_a(f5);
        this.hg2.func_78785_a(f5);
        this.hg4.func_78785_a(f5);
        this.hg3.func_78785_a(f5);
        this.hg1.func_78785_a(f5);
        this.BaseGrip.func_78785_a(f5);
        this.Bottom.func_78785_a(f5);
        this.Blade2.func_78785_a(f5);
        this.Blade3.func_78785_a(f5);
        this.Blade4.func_78785_a(f5);
        this.Shape1.func_78785_a(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }
}

