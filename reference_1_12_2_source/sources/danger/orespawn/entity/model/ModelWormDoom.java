/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 *  org.lwjgl.opengl.GL11
 */
package danger.orespawn.entity.model;

import danger.orespawn.entity.WormDoom;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelWormDoom
extends ModelBase {
    ModelRenderer head1;
    ModelRenderer tooth1;
    ModelRenderer tooth2;
    ModelRenderer tooth3;
    ModelRenderer tooth4;
    ModelRenderer tooth5;
    ModelRenderer tooth6;
    ModelRenderer tooth7;
    ModelRenderer tooth8;
    ModelRenderer head2;
    ModelRenderer body1;
    ModelRenderer body2;
    float worm_scale = 2.0f;

    public ModelWormDoom() {
        this.field_78090_t = 64;
        this.field_78089_u = 256;
        this.head1 = new ModelRenderer((ModelBase)this, 0, 0);
        this.head1.func_78789_a(-8.0f, -8.0f, -6.0f, 16, 16, 12);
        this.head1.func_78793_a(0.0f, 12.0f, 0.0f);
        this.head1.func_78787_b(64, 256);
        this.head1.field_78809_i = true;
        this.setRotation(this.head1, 0.0f, 0.0f, 0.0f);
        this.tooth1 = new ModelRenderer((ModelBase)this, 0, 220);
        this.tooth1.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth1.func_78793_a(0.0f, 21.0f, -6.0f);
        this.tooth1.func_78787_b(64, 256);
        this.tooth1.field_78809_i = true;
        this.setRotation(this.tooth1, 0.0f, 0.0f, 0.0f);
        this.tooth2 = new ModelRenderer((ModelBase)this, 0, 210);
        this.tooth2.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth2.func_78793_a(0.0f, 3.0f, -6.0f);
        this.tooth2.func_78787_b(64, 256);
        this.tooth2.field_78809_i = true;
        this.setRotation(this.tooth2, 0.0f, 0.0f, 0.0f);
        this.tooth3 = new ModelRenderer((ModelBase)this, 0, 200);
        this.tooth3.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth3.func_78793_a(9.0f, 12.0f, -6.0f);
        this.tooth3.func_78787_b(64, 256);
        this.tooth3.field_78809_i = true;
        this.setRotation(this.tooth3, 0.0f, 0.0f, 0.0f);
        this.tooth4 = new ModelRenderer((ModelBase)this, 0, 190);
        this.tooth4.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth4.func_78793_a(-9.0f, 12.0f, -6.0f);
        this.tooth4.func_78787_b(64, 256);
        this.tooth4.field_78809_i = true;
        this.setRotation(this.tooth4, 0.0f, 0.0f, 0.0f);
        this.tooth5 = new ModelRenderer((ModelBase)this, 0, 180);
        this.tooth5.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth5.func_78793_a(-6.0f, 6.0f, -6.0f);
        this.tooth5.func_78787_b(64, 256);
        this.tooth5.field_78809_i = true;
        this.setRotation(this.tooth5, 0.0f, 0.0f, 0.0f);
        this.tooth6 = new ModelRenderer((ModelBase)this, 0, 170);
        this.tooth6.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth6.func_78793_a(6.0f, 18.0f, -6.0f);
        this.tooth6.func_78787_b(64, 256);
        this.tooth6.field_78809_i = true;
        this.setRotation(this.tooth6, 0.0f, 0.0f, 0.0f);
        this.tooth7 = new ModelRenderer((ModelBase)this, 0, 160);
        this.tooth7.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth7.func_78793_a(6.0f, 6.0f, -6.0f);
        this.tooth7.func_78787_b(64, 256);
        this.tooth7.field_78809_i = true;
        this.setRotation(this.tooth7, 0.0f, 0.0f, 0.0f);
        this.tooth8 = new ModelRenderer((ModelBase)this, 0, 150);
        this.tooth8.func_78789_a(-0.5f, -0.5f, -7.0f, 1, 1, 7);
        this.tooth8.func_78793_a(-6.0f, 18.0f, -6.0f);
        this.tooth8.func_78787_b(64, 256);
        this.tooth8.field_78809_i = true;
        this.setRotation(this.tooth8, 0.0f, 0.0f, 0.0f);
        this.head2 = new ModelRenderer((ModelBase)this, 0, 31);
        this.head2.func_78789_a(-8.0f, -8.0f, -6.0f, 16, 16, 12);
        this.head2.func_78793_a(0.0f, 12.0f, 0.0f);
        this.head2.func_78787_b(64, 256);
        this.head2.field_78809_i = true;
        this.setRotation(this.head2, 0.0f, 0.0f, 0.7853982f);
        this.body1 = new ModelRenderer((ModelBase)this, 0, 82);
        this.body1.func_78789_a(-8.0f, -8.0f, -6.0f, 16, 16, 12);
        this.body1.func_78793_a(0.0f, 12.0f, 4.0f);
        this.body1.func_78787_b(64, 256);
        this.body1.field_78809_i = true;
        this.setRotation(this.body1, 0.0f, 0.0f, 0.0f);
        this.body2 = new ModelRenderer((ModelBase)this, 0, 114);
        this.body2.func_78789_a(-8.0f, -8.0f, -6.0f, 16, 16, 12);
        this.body2.func_78793_a(0.0f, 12.0f, 4.0f);
        this.body2.func_78787_b(64, 256);
        this.body2.field_78809_i = true;
        this.setRotation(this.body2, 0.0f, 0.0f, 0.7853982f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float scale) {
        GL11.glPushMatrix();
        if (!entity.field_70170_p.field_72995_K) {
            return;
        }
        if (!(entity instanceof WormDoom)) {
            return;
        }
        WormDoom dw = (WormDoom)entity;
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        double dist = 32.0;
        dist = 6.0;
        double d1 = (float)(Math.cos(newangle) * dist);
        double d2 = (float)(Math.sin(newangle) * dist);
        this.tooth1.field_78798_e = (float)((double)this.head1.field_78798_e - d1);
        double d3 = (float)(Math.sin(newangle2) * d1);
        this.tooth1.field_78800_c = (float)((double)this.head1.field_78800_c - d3);
        this.tooth1.field_78797_d = (float)((double)this.head1.field_78797_d + d2 - 9.0);
        this.tooth2.field_78798_e = this.tooth1.field_78798_e;
        this.tooth2.field_78800_c = this.tooth1.field_78800_c;
        this.tooth2.field_78797_d = this.tooth1.field_78797_d + 18.0f;
        this.tooth3.field_78798_e = this.tooth1.field_78798_e;
        this.tooth3.field_78800_c = this.tooth1.field_78800_c + 9.0f;
        this.tooth3.field_78797_d = this.tooth1.field_78797_d + 9.0f;
        this.tooth4.field_78798_e = this.tooth1.field_78798_e;
        this.tooth4.field_78800_c = this.tooth1.field_78800_c - 9.0f;
        this.tooth4.field_78797_d = this.tooth1.field_78797_d + 9.0f;
        this.tooth5.field_78798_e = this.tooth1.field_78798_e;
        this.tooth5.field_78800_c = this.tooth1.field_78800_c - 6.0f;
        this.tooth5.field_78797_d = this.tooth1.field_78797_d + 9.0f - 6.0f;
        this.tooth6.field_78798_e = this.tooth1.field_78798_e;
        this.tooth6.field_78800_c = this.tooth1.field_78800_c + 6.0f;
        this.tooth6.field_78797_d = this.tooth1.field_78797_d + 9.0f + 6.0f;
        this.tooth7.field_78798_e = this.tooth1.field_78798_e;
        this.tooth7.field_78800_c = this.tooth1.field_78800_c + 6.0f;
        this.tooth7.field_78797_d = this.tooth1.field_78797_d + 9.0f - 6.0f;
        this.tooth8.field_78798_e = this.tooth1.field_78798_e;
        this.tooth8.field_78800_c = this.tooth1.field_78800_c - 6.0f;
        this.tooth8.field_78797_d = this.tooth1.field_78797_d + 9.0f + 6.0f;
        this.tooth1.field_78798_e = (float)((double)this.tooth1.field_78798_e - Math.sin(this.head1.field_78795_f) * 9.0);
        this.tooth2.field_78798_e = (float)((double)this.tooth2.field_78798_e + Math.sin(this.head1.field_78795_f) * 9.0);
        this.tooth3.field_78798_e = (float)((double)this.tooth3.field_78798_e - Math.sin(this.head1.field_78796_g) * 9.0);
        this.tooth4.field_78798_e = (float)((double)this.tooth4.field_78798_e + Math.sin(this.head1.field_78796_g) * 9.0);
        this.tooth7.field_78798_e = (float)((double)this.tooth7.field_78798_e - Math.sin(this.head1.field_78795_f) * 6.0);
        this.tooth7.field_78798_e = (float)((double)this.tooth7.field_78798_e - Math.sin(this.head1.field_78796_g) * 6.0);
        this.tooth6.field_78798_e = (float)((double)this.tooth6.field_78798_e + Math.sin(this.head1.field_78795_f) * 6.0);
        this.tooth6.field_78798_e = (float)((double)this.tooth6.field_78798_e - Math.sin(this.head1.field_78796_g) * 6.0);
        this.tooth5.field_78798_e = (float)((double)this.tooth5.field_78798_e - Math.sin(this.head1.field_78795_f) * 6.0);
        this.tooth5.field_78798_e = (float)((double)this.tooth5.field_78798_e + Math.sin(this.head1.field_78796_g) * 6.0);
        this.tooth8.field_78798_e = (float)((double)this.tooth8.field_78798_e + Math.sin(this.head1.field_78795_f) * 6.0);
        this.tooth8.field_78798_e = (float)((double)this.tooth8.field_78798_e + Math.sin(this.head1.field_78796_g) * 6.0);
        newangle = (float)Math.cos(Math.toRadians(f * 5.7f)) * (float)Math.PI * 0.35f;
        this.tooth1.field_78795_f = this.head1.field_78795_f + newangle;
        this.tooth2.field_78795_f = this.head1.field_78795_f - newangle;
        this.tooth3.field_78796_g = this.head1.field_78796_g + newangle;
        this.tooth4.field_78796_g = this.head1.field_78796_g - newangle;
        this.tooth5.field_78795_f = this.head1.field_78795_f + newangle;
        this.tooth7.field_78795_f = this.head1.field_78795_f + newangle;
        this.tooth6.field_78795_f = this.head1.field_78795_f - newangle;
        this.tooth8.field_78795_f = this.head1.field_78795_f - newangle;
        this.tooth6.field_78796_g = this.head1.field_78796_g + newangle;
        this.tooth7.field_78796_g = this.head1.field_78796_g + newangle;
        this.tooth5.field_78796_g = this.head1.field_78796_g - newangle;
        this.tooth8.field_78796_g = this.head1.field_78796_g - newangle;
        GL11.glPushMatrix();
        GL11.glScalef((float)this.worm_scale, (float)this.worm_scale, (float)this.worm_scale);
        GL11.glRotatef((float)dw.field_70177_z, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)dw.field_70125_A, (float)1.0f, (float)0.0f, (float)0.0f);
        this.head1.func_78785_a(scale);
        this.head2.func_78785_a(scale);
        this.tooth1.func_78785_a(scale);
        this.tooth2.func_78785_a(scale);
        this.tooth3.func_78785_a(scale);
        this.tooth4.func_78785_a(scale);
        this.tooth5.func_78785_a(scale);
        this.tooth6.func_78785_a(scale);
        this.tooth7.func_78785_a(scale);
        this.tooth8.func_78785_a(scale);
        GL11.glPopMatrix();
        for (int i = 0; i < 100; ++i) {
            double dx = dw.lposx[0] - dw.lposx[i];
            double dy = dw.lposy[0] - dw.lposy[i];
            double dz = dw.lposz[0] - dw.lposz[i];
            GL11.glPushMatrix();
            GL11.glTranslated((double)dx, (double)dy, (double)dz);
            if (dw.rotyaw[i] != 0.0) {
                GL11.glRotated((double)dw.rotyaw[i], (double)0.0, (double)1.0, (double)0.0);
            }
            if (dw.rotpitch[i] != 0.0) {
                GL11.glRotated((double)dw.rotpitch[i], (double)1.0, (double)0.0, (double)0.0);
            }
            double scale1 = 1.0;
            if (i > 25) {
                scale1 = (75.0 - (double)(i - 25)) / 75.0;
            }
            if ((scale1 *= 2.0 - Math.cos(Math.toRadians(i * 3))) < (double)0.01f) {
                scale1 = 0.01f;
            }
            GL11.glScaled((double)(scale1 * (double)this.worm_scale), (double)(scale1 * (double)this.worm_scale), (double)(1.5f * this.worm_scale));
            this.body1.func_78785_a(scale);
            this.body2.func_78785_a(scale);
            if (i > 75) {
                GL11.glTranslatef((float)0.0f, (float)(1.0f / this.worm_scale), (float)0.0f);
                this.body1.func_78785_a(scale);
                this.body2.func_78785_a(scale);
            }
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }
}

