/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelWormSmall
extends ModelBase {
    ModelRenderer head;
    ModelRenderer body;
    ModelRenderer tail;

    public ModelWormSmall() {
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.head = new ModelRenderer((ModelBase)this, 0, 0);
        this.head.func_78789_a(-0.5f, -5.0f, -0.5f, 1, 5, 1);
        this.head.func_78793_a(0.0f, 14.0f, 0.0f);
        this.head.func_78787_b(64, 32);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.body = new ModelRenderer((ModelBase)this, 6, 0);
        this.body.func_78789_a(-0.5f, -5.0f, -0.5f, 1, 5, 1);
        this.body.func_78793_a(0.0f, 19.0f, 0.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.tail = new ModelRenderer((ModelBase)this, 12, 0);
        this.tail.func_78789_a(-0.5f, -5.0f, -0.5f, 1, 5, 1);
        this.tail.func_78793_a(0.0f, 24.0f, 0.0f);
        this.tail.func_78787_b(64, 32);
        this.tail.field_78809_i = true;
        this.setRotation(this.tail, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        this.tail.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.55f)) * (float)Math.PI * 0.15f;
        float d1 = (float)(Math.sin(newangle) * 5.0);
        float d2 = (float)(Math.cos(newangle) * 5.0);
        this.body.field_78798_e = this.tail.field_78798_e - d1;
        this.tail.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.35f)) * (float)Math.PI * 0.1f;
        float d3 = (float)(Math.cos(newangle) * (double)d2);
        float d4 = (float)(Math.sin(newangle) * (double)d2);
        this.body.field_78800_c = this.tail.field_78800_c + d4;
        this.body.field_78797_d = (float)((double)this.tail.field_78797_d - 5.0 + (5.0 - (double)d3));
        this.body.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.45f)) * (float)Math.PI * 0.15f;
        d1 = (float)(Math.sin(newangle) * 5.0);
        d2 = (float)(Math.cos(newangle) * 5.0);
        this.head.field_78798_e = this.body.field_78798_e - d1;
        this.body.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 0.25f)) * (float)Math.PI * 0.1f;
        d3 = (float)(Math.cos(newangle) * (double)d2);
        d4 = (float)(Math.sin(newangle) * (double)d2);
        this.head.field_78800_c = this.body.field_78800_c + d4;
        this.head.field_78797_d = (float)((double)this.body.field_78797_d - 5.0 + (5.0 - (double)d3));
        this.head.field_78795_f = 0.62f + MathHelper.func_76134_b((float)(f2 * 0.65f)) * (float)Math.PI * 0.15f;
        this.head.field_78808_h = MathHelper.func_76134_b((float)(f2 * 0.3f)) * (float)Math.PI * 0.05f;
        this.head.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.tail.func_78785_a(f5);
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

