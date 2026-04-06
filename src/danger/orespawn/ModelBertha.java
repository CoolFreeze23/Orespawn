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

public class ModelBertha
extends ModelBase {
    ModelRenderer Grip;
    ModelRenderer Blade;
    ModelRenderer Handguard2;
    ModelRenderer Handguard1;
    ModelRenderer hg2;
    ModelRenderer hg4;
    ModelRenderer hg3;
    ModelRenderer hg1;
    ModelRenderer BaseGrip;
    ModelRenderer Tip1;
    ModelRenderer Tip2;
    ModelRenderer Bottom;

    public ModelBertha() {
        this.field_78090_t = 64;
        this.field_78089_u = 128;
        this.Grip = new ModelRenderer((ModelBase)this, 0, 0);
        this.Grip.func_78789_a(0.0f, -6.0f, 0.0f, 1, 12, 1);
        this.Grip.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Grip.func_78787_b(64, 128);
        this.Grip.field_78809_i = true;
        this.setRotation(this.Grip, 0.0f, 0.0f, 0.0f);
        this.Blade = new ModelRenderer((ModelBase)this, 6, 0);
        this.Blade.func_78789_a(0.0f, -41.0f, -1.0f, 1, 34, 3);
        this.Blade.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Blade.func_78787_b(64, 128);
        this.Blade.field_78809_i = true;
        this.setRotation(this.Blade, 0.0f, 0.0f, 0.0f);
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
        this.hg2.func_78789_a(0.0f, -8.0f, -5.0f, 1, 1, 1);
        this.hg2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.hg2.func_78787_b(64, 128);
        this.hg2.field_78809_i = true;
        this.setRotation(this.hg2, 0.0f, 0.0f, 0.0f);
        this.hg4 = new ModelRenderer((ModelBase)this, 0, 18);
        this.hg4.func_78789_a(0.0f, -8.0f, 5.0f, 1, 1, 1);
        this.hg4.func_78793_a(0.0f, 0.0f, 0.0f);
        this.hg4.func_78787_b(64, 128);
        this.hg4.field_78809_i = true;
        this.setRotation(this.hg4, 0.0f, 0.0f, 0.0f);
        this.hg3 = new ModelRenderer((ModelBase)this, 0, 21);
        this.hg3.func_78789_a(-4.0f, -8.0f, 0.0f, 1, 1, 1);
        this.hg3.func_78793_a(0.0f, 0.0f, 0.0f);
        this.hg3.func_78787_b(64, 128);
        this.hg3.field_78809_i = true;
        this.setRotation(this.hg3, 0.0f, 0.0f, 0.0f);
        this.hg1 = new ModelRenderer((ModelBase)this, 0, 24);
        this.hg1.func_78789_a(4.0f, -8.0f, 0.0f, 1, 1, 1);
        this.hg1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.hg1.func_78787_b(64, 128);
        this.hg1.field_78809_i = true;
        this.setRotation(this.hg1, 0.0f, 0.0f, 0.0f);
        this.BaseGrip = new ModelRenderer((ModelBase)this, 0, 39);
        this.BaseGrip.func_78789_a(-1.0f, 5.0f, -1.0f, 3, 1, 3);
        this.BaseGrip.func_78793_a(0.0f, 0.0f, 0.0f);
        this.BaseGrip.func_78787_b(64, 128);
        this.BaseGrip.field_78809_i = true;
        this.setRotation(this.BaseGrip, 0.0f, 0.0f, 0.0f);
        this.Tip1 = new ModelRenderer((ModelBase)this, 21, 16);
        this.Tip1.func_78789_a(0.0f, -42.0f, -0.5f, 1, 1, 2);
        this.Tip1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Tip1.func_78787_b(64, 128);
        this.Tip1.field_78809_i = true;
        this.setRotation(this.Tip1, 0.0f, 0.0f, 0.0f);
        this.Tip2 = new ModelRenderer((ModelBase)this, 22, 20);
        this.Tip2.func_78789_a(0.0f, -43.0f, 0.0f, 1, 1, 1);
        this.Tip2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Tip2.func_78787_b(64, 128);
        this.Tip2.field_78809_i = true;
        this.setRotation(this.Tip2, 0.0f, 0.0f, 0.0f);
        this.Bottom = new ModelRenderer((ModelBase)this, 0, 45);
        this.Bottom.func_78789_a(0.0f, 6.0f, 0.0f, 1, 1, 1);
        this.Bottom.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Bottom.func_78787_b(64, 128);
        this.Bottom.field_78809_i = true;
        this.setRotation(this.Bottom, 0.0f, 0.0f, 0.0f);
    }

    public void render() {
        float f5 = 1.0f;
        this.Grip.func_78785_a(f5);
        this.Blade.func_78785_a(f5);
        this.Handguard2.func_78785_a(f5);
        this.Handguard1.func_78785_a(f5);
        this.hg2.func_78785_a(f5);
        this.hg4.func_78785_a(f5);
        this.hg3.func_78785_a(f5);
        this.hg1.func_78785_a(f5);
        this.BaseGrip.func_78785_a(f5);
        this.Tip1.func_78785_a(f5);
        this.Tip2.func_78785_a(f5);
        this.Bottom.func_78785_a(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }
}

