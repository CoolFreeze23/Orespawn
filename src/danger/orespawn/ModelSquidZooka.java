/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  org.lwjgl.opengl.GL11
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.opengl.GL11;

public class ModelSquidZooka
extends ModelBase {
    ModelRenderer Barrel;
    ModelRenderer tail1;
    ModelRenderer tail2;
    ModelRenderer tail3;
    ModelRenderer tail4;
    ModelRenderer tail5;
    ModelRenderer tail6;
    ModelRenderer tail7;
    ModelRenderer sight3;
    ModelRenderer sight2;
    ModelRenderer sight1;
    ModelRenderer handle1;

    public ModelSquidZooka() {
        this.field_78090_t = 128;
        this.field_78089_u = 128;
        this.Barrel = new ModelRenderer((ModelBase)this, 29, 19);
        this.Barrel.func_78789_a(-1.0f, -1.0f, -19.0f, 2, 2, 34);
        this.Barrel.func_78793_a(0.0f, 0.0f, 0.0f);
        this.Barrel.func_78787_b(128, 128);
        this.Barrel.field_78809_i = true;
        this.setRotation(this.Barrel, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 0, 53);
        this.tail1.func_78789_a(-1.5f, -1.5f, 15.0f, 3, 3, 1);
        this.tail1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail1.func_78787_b(128, 128);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.0f, 0.0f, 0.0f);
        this.tail2 = new ModelRenderer((ModelBase)this, 0, 58);
        this.tail2.func_78789_a(-2.0f, -2.0f, 16.0f, 4, 4, 1);
        this.tail2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail2.func_78787_b(128, 128);
        this.tail2.field_78809_i = true;
        this.setRotation(this.tail2, 0.0f, 0.0f, 0.0f);
        this.tail3 = new ModelRenderer((ModelBase)this, 0, 64);
        this.tail3.func_78789_a(-2.5f, -2.5f, 17.0f, 5, 5, 1);
        this.tail3.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail3.func_78787_b(128, 128);
        this.tail3.field_78809_i = true;
        this.setRotation(this.tail3, 0.0f, 0.0f, 0.0f);
        this.tail4 = new ModelRenderer((ModelBase)this, 0, 71);
        this.tail4.func_78789_a(-3.0f, -3.0f, 18.0f, 6, 6, 6);
        this.tail4.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail4.func_78787_b(128, 128);
        this.tail4.field_78809_i = true;
        this.setRotation(this.tail4, 0.0f, 0.0f, 0.0f);
        this.tail5 = new ModelRenderer((ModelBase)this, 0, 84);
        this.tail5.func_78789_a(-2.5f, -2.5f, 24.0f, 5, 5, 1);
        this.tail5.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail5.func_78787_b(128, 128);
        this.tail5.field_78809_i = true;
        this.setRotation(this.tail5, 0.0f, 0.0f, 0.0f);
        this.tail6 = new ModelRenderer((ModelBase)this, 0, 91);
        this.tail6.func_78789_a(-2.0f, -2.0f, 25.0f, 4, 4, 1);
        this.tail6.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail6.func_78787_b(128, 128);
        this.tail6.field_78809_i = true;
        this.setRotation(this.tail6, 0.0f, 0.0f, 0.0f);
        this.tail7 = new ModelRenderer((ModelBase)this, 0, 97);
        this.tail7.func_78789_a(-1.5f, -1.5f, 26.0f, 3, 3, 1);
        this.tail7.func_78793_a(0.0f, 0.0f, 0.0f);
        this.tail7.func_78787_b(128, 128);
        this.tail7.field_78809_i = true;
        this.setRotation(this.tail7, 0.0f, 0.0f, 0.0f);
        this.sight3 = new ModelRenderer((ModelBase)this, 25, 0);
        this.sight3.func_78789_a(1.0f, -2.0f, -10.0f, 1, 1, 2);
        this.sight3.func_78793_a(0.0f, 0.0f, 0.0f);
        this.sight3.func_78787_b(128, 128);
        this.sight3.field_78809_i = true;
        this.setRotation(this.sight3, 0.0f, 0.0f, 0.0f);
        this.sight2 = new ModelRenderer((ModelBase)this, 32, 0);
        this.sight2.func_78789_a(0.5f, -4.0f, -12.0f, 2, 2, 6);
        this.sight2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.sight2.func_78787_b(128, 128);
        this.sight2.field_78809_i = true;
        this.setRotation(this.sight2, 0.0f, 0.0f, 0.0f);
        this.sight1 = new ModelRenderer((ModelBase)this, 18, 0);
        this.sight1.func_78789_a(1.0f, -1.0f, -10.0f, 1, 1, 2);
        this.sight1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.sight1.func_78787_b(128, 128);
        this.sight1.field_78809_i = true;
        this.setRotation(this.sight1, 0.0f, 0.0f, 0.0f);
        this.handle1 = new ModelRenderer((ModelBase)this, 0, 0);
        this.handle1.func_78789_a(0.0f, 1.0f, 0.0f, 1, 7, 1);
        this.handle1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.handle1.func_78787_b(128, 128);
        this.handle1.field_78809_i = true;
        this.setRotation(this.handle1, 0.0f, 0.0f, 0.0f);
    }

    public void render() {
        float f5 = 1.0f;
        GL11.glPushMatrix();
        GL11.glRotatef((float)180.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        this.Barrel.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.tail2.func_78785_a(f5);
        this.tail3.func_78785_a(f5);
        this.tail4.func_78785_a(f5);
        this.tail5.func_78785_a(f5);
        this.tail6.func_78785_a(f5);
        this.tail7.func_78785_a(f5);
        this.sight3.func_78785_a(f5);
        this.sight2.func_78785_a(f5);
        this.sight1.func_78785_a(f5);
        this.handle1.func_78785_a(f5);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.field_78795_f = x;
        model.field_78796_g = y;
        model.field_78808_h = z;
    }
}

