/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelChainsaw
extends ModelBase {
    ModelRenderer engine;
    ModelRenderer handle1;
    ModelRenderer handle2;
    ModelRenderer handle3;
    ModelRenderer muffler;
    ModelRenderer blade1;
    ModelRenderer blade2;
    ModelRenderer tooth;
    float toothpos = 0.0f;
    int toothdir = 0;
    float toothpos1 = 7.0f;
    int toothdir1 = 0;
    float toothpos2 = 14.0f;
    int toothdir2 = 0;
    float toothpos3 = 20.0f;
    int toothdir3 = 1;
    float toothpos4 = 13.0f;
    int toothdir4 = 1;
    float toothpos5 = 6.0f;
    int toothdir5 = 1;

    public ModelChainsaw() {
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.engine = new ModelRenderer((ModelBase)this, 0, 19);
        this.engine.func_78789_a(-2.0f, -4.0f, -4.0f, 4, 7, 8);
        this.engine.func_78793_a(0.0f, 0.0f, 0.0f);
        this.engine.func_78787_b(64, 64);
        this.engine.field_78809_i = true;
        this.setRotation(this.engine, 0.0f, 0.0f, 0.0f);
        this.handle1 = new ModelRenderer((ModelBase)this, 49, 0);
        this.handle1.func_78789_a(0.0f, -3.0f, 3.0f, 1, 1, 5);
        this.handle1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.handle1.func_78787_b(64, 64);
        this.handle1.field_78809_i = true;
        this.setRotation(this.handle1, -0.1919862f, 0.0f, 0.0f);
        this.handle2 = new ModelRenderer((ModelBase)this, 50, 13);
        this.handle2.func_78789_a(0.0f, 2.0f, 4.0f, 1, 1, 4);
        this.handle2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.handle2.func_78787_b(64, 64);
        this.handle2.field_78809_i = true;
        this.setRotation(this.handle2, 0.0f, 0.0f, 0.0f);
        this.handle3 = new ModelRenderer((ModelBase)this, 52, 7);
        this.handle3.func_78789_a(0.0f, -2.0f, 7.0f, 1, 4, 1);
        this.handle3.func_78793_a(0.0f, 0.0f, 0.0f);
        this.handle3.func_78787_b(64, 64);
        this.handle3.field_78809_i = true;
        this.setRotation(this.handle3, -0.0872665f, 0.0f, 0.0f);
        this.muffler = new ModelRenderer((ModelBase)this, 14, 0);
        this.muffler.func_78789_a(-3.0f, 0.0f, 1.0f, 1, 3, 3);
        this.muffler.func_78793_a(0.0f, 0.0f, 0.0f);
        this.muffler.func_78787_b(64, 64);
        this.muffler.field_78809_i = true;
        this.setRotation(this.muffler, 0.0f, 0.0f, 0.0f);
        this.blade1 = new ModelRenderer((ModelBase)this, 0, 35);
        this.blade1.func_78789_a(0.0f, -2.0f, -28.0f, 1, 4, 24);
        this.blade1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.blade1.func_78787_b(64, 64);
        this.blade1.field_78809_i = true;
        this.setRotation(this.blade1, 0.0f, 0.0f, 0.0f);
        this.blade2 = new ModelRenderer((ModelBase)this, 0, 8);
        this.blade2.func_78789_a(0.0f, -2.5f, -2.5f, 1, 5, 5);
        this.blade2.func_78793_a(0.0f, 0.0f, -28.0f);
        this.blade2.func_78787_b(64, 64);
        this.blade2.field_78809_i = true;
        this.setRotation(this.blade2, 0.0f, 0.0f, 0.0f);
        this.tooth = new ModelRenderer((ModelBase)this, 0, 0);
        this.tooth.func_78789_a(0.0f, -1.0f, -0.5f, 1, 1, 1);
        this.tooth.func_78793_a(0.0f, -2.0f, -5.0f);
        this.tooth.func_78787_b(64, 64);
        this.tooth.field_78809_i = true;
        this.setRotation(this.tooth, 0.0f, 0.0f, 0.0f);
    }

    public void render() {
        float f5 = 1.0f;
        this.renderTooth(f5);
        this.renderTooth1(f5);
        this.renderTooth2(f5);
        this.renderTooth3(f5);
        this.renderTooth4(f5);
        this.renderTooth5(f5);
        this.blade2.field_78795_f = (float)((double)this.blade2.field_78795_f + 0.10471975511965977);
        if ((double)this.blade2.field_78795_f > Math.PI * 2) {
            this.blade2.field_78795_f = 0.0f;
        }
        this.engine.func_78785_a(f5);
        this.handle1.func_78785_a(f5);
        this.handle2.func_78785_a(f5);
        this.handle3.func_78785_a(f5);
        this.muffler.func_78785_a(f5);
        this.blade1.func_78785_a(f5);
        this.blade2.func_78785_a(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }

    private void renderTooth(float f5) {
        if (this.toothdir == 0) {
            this.tooth.field_78797_d = -2.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos;
            this.toothpos += 0.5f;
            if (this.toothpos > 21.0f) {
                this.toothpos = 21.0f;
                this.toothdir = 1;
            }
        } else {
            this.tooth.field_78797_d = 3.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos;
            this.toothpos -= 0.5f;
            if (this.toothpos < 0.0f) {
                this.toothpos = 0.0f;
                this.toothdir = 0;
            }
        }
        this.tooth.func_78785_a(f5);
    }

    private void renderTooth1(float f5) {
        if (this.toothdir1 == 0) {
            this.tooth.field_78797_d = -2.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos1;
            this.toothpos1 += 0.5f;
            if (this.toothpos1 > 21.0f) {
                this.toothpos1 = 21.0f;
                this.toothdir1 = 1;
            }
        } else {
            this.tooth.field_78797_d = 3.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos1;
            this.toothpos1 -= 0.5f;
            if (this.toothpos1 < 0.0f) {
                this.toothpos1 = 0.0f;
                this.toothdir1 = 0;
            }
        }
        this.tooth.func_78785_a(f5);
    }

    private void renderTooth2(float f5) {
        if (this.toothdir2 == 0) {
            this.tooth.field_78797_d = -2.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos2;
            this.toothpos2 += 0.5f;
            if (this.toothpos2 > 21.0f) {
                this.toothpos2 = 21.0f;
                this.toothdir2 = 1;
            }
        } else {
            this.tooth.field_78797_d = 3.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos2;
            this.toothpos2 -= 0.5f;
            if (this.toothpos2 < 0.0f) {
                this.toothpos2 = 0.0f;
                this.toothdir2 = 0;
            }
        }
        this.tooth.func_78785_a(f5);
    }

    private void renderTooth3(float f5) {
        if (this.toothdir3 == 0) {
            this.tooth.field_78797_d = -2.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos3;
            this.toothpos3 += 0.5f;
            if (this.toothpos3 > 21.0f) {
                this.toothpos3 = 21.0f;
                this.toothdir3 = 1;
            }
        } else {
            this.tooth.field_78797_d = 3.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos3;
            this.toothpos3 -= 0.5f;
            if (this.toothpos3 < 0.0f) {
                this.toothpos3 = 0.0f;
                this.toothdir3 = 0;
            }
        }
        this.tooth.func_78785_a(f5);
    }

    private void renderTooth4(float f5) {
        if (this.toothdir4 == 0) {
            this.tooth.field_78797_d = -2.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos4;
            this.toothpos4 += 0.5f;
            if (this.toothpos4 > 21.0f) {
                this.toothpos4 = 21.0f;
                this.toothdir4 = 1;
            }
        } else {
            this.tooth.field_78797_d = 3.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos4;
            this.toothpos4 -= 0.5f;
            if (this.toothpos4 < 0.0f) {
                this.toothpos4 = 0.0f;
                this.toothdir4 = 0;
            }
        }
        this.tooth.func_78785_a(f5);
    }

    private void renderTooth5(float f5) {
        if (this.toothdir5 == 0) {
            this.tooth.field_78797_d = -2.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos5;
            this.toothpos5 += 0.5f;
            if (this.toothpos5 > 21.0f) {
                this.toothpos5 = 21.0f;
                this.toothdir5 = 1;
            }
        } else {
            this.tooth.field_78797_d = 3.0f;
            this.tooth.field_78798_e = -5.0f - this.toothpos5;
            this.toothpos5 -= 0.5f;
            if (this.toothpos5 < 0.0f) {
                this.toothpos5 = 0.0f;
                this.toothdir5 = 0;
            }
        }
        this.tooth.func_78785_a(f5);
    }
}

