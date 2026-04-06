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

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelCliffRacer
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Body;
    ModelRenderer Fins;
    ModelRenderer LWing;
    ModelRenderer RWing;
    ModelRenderer Tail;
    ModelRenderer TailEnd;
    ModelRenderer Head;
    ModelRenderer Beak;

    public ModelCliffRacer(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.Body = new ModelRenderer((ModelBase)this, 0, 52);
        this.Body.func_78789_a(0.0f, 0.0f, 0.0f, 3, 1, 10);
        this.Body.func_78793_a(-1.0f, 15.0f, -4.0f);
        this.Body.func_78787_b(64, 64);
        this.Body.field_78809_i = true;
        this.setRotation(this.Body, 0.0f, 0.0f, 0.0f);
        this.Fins = new ModelRenderer((ModelBase)this, 0, 40);
        this.Fins.func_78789_a(0.0f, -4.0f, 0.0f, 1, 6, 3);
        this.Fins.func_78793_a(0.0f, 15.0f, -1.0f);
        this.Fins.func_78787_b(64, 64);
        this.Fins.field_78809_i = true;
        this.setRotation(this.Fins, 0.0f, 0.0f, 0.0f);
        this.LWing = new ModelRenderer((ModelBase)this, 0, 31);
        this.LWing.func_78789_a(0.0f, 0.0f, 0.0f, 7, 1, 6);
        this.LWing.func_78793_a(2.0f, 15.0f, -2.0f);
        this.LWing.func_78787_b(64, 64);
        this.LWing.field_78809_i = true;
        this.setRotation(this.LWing, 0.0f, 0.0f, 0.0f);
        this.RWing = new ModelRenderer((ModelBase)this, 39, 0);
        this.RWing.func_78789_a(-7.0f, 0.0f, 0.0f, 7, 1, 6);
        this.RWing.func_78793_a(-1.0f, 15.0f, -2.0f);
        this.RWing.func_78787_b(64, 64);
        this.RWing.field_78809_i = true;
        this.setRotation(this.RWing, 0.0f, 0.0f, 0.0f);
        this.Tail = new ModelRenderer((ModelBase)this, 0, 16);
        this.Tail.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 9);
        this.Tail.func_78793_a(0.0f, 15.0f, 6.0f);
        this.Tail.func_78787_b(64, 64);
        this.Tail.field_78809_i = true;
        this.setRotation(this.Tail, 0.0f, 0.0f, 0.0f);
        this.TailEnd = new ModelRenderer((ModelBase)this, 0, 10);
        this.TailEnd.func_78789_a(0.0f, -1.0f, 9.0f, 2, 2, 2);
        this.TailEnd.func_78793_a(-0.5f, 15.0f, 6.0f);
        this.TailEnd.func_78787_b(64, 64);
        this.TailEnd.field_78809_i = true;
        this.setRotation(this.TailEnd, 0.0f, 0.0f, 0.0f);
        this.Head = new ModelRenderer((ModelBase)this, 28, 21);
        this.Head.func_78789_a(0.0f, 0.0f, 0.0f, 2, 2, 2);
        this.Head.func_78793_a(-0.5f, 14.0f, -6.0f);
        this.Head.func_78787_b(64, 64);
        this.Head.field_78809_i = true;
        this.setRotation(this.Head, 0.0f, 0.0f, 0.0f);
        this.Beak = new ModelRenderer((ModelBase)this, 0, 0);
        this.Beak.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 2);
        this.Beak.func_78793_a(0.0f, 14.5f, -8.0f);
        this.Beak.func_78787_b(64, 64);
        this.Beak.field_78809_i = true;
        this.setRotation(this.Beak, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        this.LWing.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.25f;
        this.RWing.field_78808_h = -newangle;
        this.Body.func_78785_a(f5);
        this.Fins.func_78785_a(f5);
        this.LWing.func_78785_a(f5);
        this.RWing.func_78785_a(f5);
        this.Tail.func_78785_a(f5);
        this.TailEnd.func_78785_a(f5);
        this.Head.func_78785_a(f5);
        this.Beak.func_78785_a(f5);
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

