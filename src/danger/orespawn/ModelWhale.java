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

public class ModelWhale
extends ModelBase {
    ModelRenderer belly;
    ModelRenderer body;
    ModelRenderer back;
    ModelRenderer tail1;
    ModelRenderer tail2;
    ModelRenderer tailfin1;
    ModelRenderer tailfin2;
    ModelRenderer backfin;
    ModelRenderer head;
    ModelRenderer jaw;
    ModelRenderer lfin1;
    ModelRenderer lfin2;
    ModelRenderer rfin1;
    ModelRenderer rfin2;

    public ModelWhale() {
        this.field_78090_t = 256;
        this.field_78089_u = 256;
        this.belly = new ModelRenderer((ModelBase)this, 0, 92);
        this.belly.func_78789_a(-6.0f, 0.0f, 0.0f, 12, 2, 32);
        this.belly.func_78793_a(0.0f, 22.0f, 6.0f);
        this.belly.func_78787_b(256, 256);
        this.belly.field_78809_i = true;
        this.setRotation(this.belly, 0.0f, 0.0f, 0.0f);
        this.body = new ModelRenderer((ModelBase)this, 0, 188);
        this.body.func_78789_a(-10.0f, 0.0f, 0.0f, 20, 12, 52);
        this.body.func_78793_a(0.0f, 10.0f, 0.0f);
        this.body.func_78787_b(256, 256);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.back = new ModelRenderer((ModelBase)this, 0, 45);
        this.back.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 2, 40);
        this.back.func_78793_a(0.0f, 8.0f, 3.0f);
        this.back.func_78787_b(256, 256);
        this.back.field_78809_i = true;
        this.setRotation(this.back, 0.0f, 0.0f, 0.0f);
        this.tail1 = new ModelRenderer((ModelBase)this, 186, 0);
        this.tail1.func_78789_a(-6.0f, 0.0f, 0.0f, 12, 7, 14);
        this.tail1.func_78793_a(0.0f, 11.0f, 52.0f);
        this.tail1.func_78787_b(256, 256);
        this.tail1.field_78809_i = true;
        this.setRotation(this.tail1, 0.0f, 0.0f, 0.0f);
        this.tail2 = new ModelRenderer((ModelBase)this, 186, 24);
        this.tail2.func_78789_a(-4.0f, 0.0f, 0.0f, 8, 5, 10);
        this.tail2.func_78793_a(0.0f, 12.0f, 66.0f);
        this.tail2.func_78787_b(256, 256);
        this.tail2.field_78809_i = true;
        this.setRotation(this.tail2, 0.0f, 0.0f, 0.0f);
        this.tailfin1 = new ModelRenderer((ModelBase)this, 186, 43);
        this.tailfin1.func_78789_a(0.0f, 0.0f, 0.0f, 17, 2, 11);
        this.tailfin1.func_78793_a(2.0f, 13.0f, 74.0f);
        this.tailfin1.func_78787_b(256, 256);
        this.tailfin1.field_78809_i = true;
        this.setRotation(this.tailfin1, 0.0872665f, -0.0872665f, 0.0f);
        this.tailfin2 = new ModelRenderer((ModelBase)this, 186, 59);
        this.tailfin2.func_78789_a(-17.0f, 0.0f, 0.0f, 17, 2, 11);
        this.tailfin2.func_78793_a(-2.0f, 13.0f, 74.0f);
        this.tailfin2.func_78787_b(256, 256);
        this.tailfin2.field_78809_i = true;
        this.setRotation(this.tailfin2, 0.0872665f, 0.0872665f, 0.0f);
        this.backfin = new ModelRenderer((ModelBase)this, 0, 15);
        this.backfin.func_78789_a(-0.5f, 0.0f, 0.0f, 1, 4, 8);
        this.backfin.func_78793_a(0.0f, 8.0f, 11.0f);
        this.backfin.func_78787_b(256, 256);
        this.backfin.field_78809_i = true;
        this.setRotation(this.backfin, 0.3665191f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 0, 155);
        this.head.func_78789_a(-8.0f, 0.0f, -16.0f, 16, 8, 22);
        this.head.func_78793_a(0.0f, 11.0f, -6.0f);
        this.head.func_78787_b(256, 256);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.jaw = new ModelRenderer((ModelBase)this, 0, 130);
        this.jaw.func_78789_a(-7.0f, -1.0f, -20.0f, 14, 2, 20);
        this.jaw.func_78793_a(0.0f, 20.0f, 0.0f);
        this.jaw.func_78787_b(256, 256);
        this.jaw.field_78809_i = true;
        this.setRotation(this.jaw, 0.0698132f, 0.0f, 0.0f);
        this.lfin1 = new ModelRenderer((ModelBase)this, 96, 0);
        this.lfin1.func_78789_a(0.0f, -1.0f, -3.0f, 4, 3, 6);
        this.lfin1.func_78793_a(10.0f, 18.0f, 8.0f);
        this.lfin1.func_78787_b(256, 256);
        this.lfin1.field_78809_i = true;
        this.setRotation(this.lfin1, 0.0f, -0.0872665f, 0.0f);
        this.lfin2 = new ModelRenderer((ModelBase)this, 120, 0);
        this.lfin2.func_78789_a(2.0f, -0.5f, -3.0f, 22, 2, 8);
        this.lfin2.func_78793_a(10.0f, 18.0f, 8.0f);
        this.lfin2.func_78787_b(256, 256);
        this.lfin2.field_78809_i = true;
        this.setRotation(this.lfin2, 0.0f, -0.0872665f, 0.0f);
        this.rfin1 = new ModelRenderer((ModelBase)this, 96, 12);
        this.rfin1.func_78789_a(-4.0f, -1.0f, -3.0f, 4, 3, 6);
        this.rfin1.func_78793_a(-10.0f, 18.0f, 8.0f);
        this.rfin1.func_78787_b(256, 256);
        this.rfin1.field_78809_i = true;
        this.setRotation(this.rfin1, 0.0f, 0.0872665f, 0.0f);
        this.rfin2 = new ModelRenderer((ModelBase)this, 120, 13);
        this.rfin2.func_78789_a(-24.0f, -0.5f, -3.0f, 22, 2, 8);
        this.rfin2.func_78793_a(-10.0f, 18.0f, 8.0f);
        this.rfin2.func_78787_b(256, 256);
        this.rfin2.field_78809_i = true;
        this.setRotation(this.rfin2, 0.0f, 0.0872665f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        float newangle = MathHelper.func_76134_b((float)(f2 * 0.55f)) * (float)Math.PI * 0.15f;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 0.3f)) * (float)Math.PI * 0.2f * f1 : MathHelper.func_76134_b((float)(f2 * 0.08f)) * (float)Math.PI * 0.05f;
        this.lfin2.field_78808_h = 0.436f + newangle;
        this.lfin1.field_78808_h = this.lfin2.field_78808_h / 2.0f;
        this.rfin2.field_78808_h = -0.436f - newangle;
        this.rfin1.field_78808_h = this.rfin2.field_78808_h / 2.0f;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.03f)) * (float)Math.PI * 0.02f;
        this.jaw.field_78795_f = 0.087f + newangle;
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 0.4f)) * (float)Math.PI * 0.16f * f1 : MathHelper.func_76134_b((float)(f2 * 0.05f)) * (float)Math.PI * 0.03f;
        this.tail1.field_78795_f = newangle * 0.5f;
        this.tail2.field_78795_f = newangle * 1.25f;
        this.tailfin1.field_78795_f = this.tailfin2.field_78795_f = newangle * 2.25f;
        this.tail2.field_78798_e = this.tail1.field_78798_e + (float)Math.cos(this.tail1.field_78795_f) * 14.0f;
        this.tail2.field_78797_d = this.tail1.field_78797_d - (float)Math.sin(this.tail1.field_78795_f) * 14.0f;
        this.tailfin1.field_78798_e = this.tailfin2.field_78798_e = this.tail2.field_78798_e + (float)Math.cos(this.tail2.field_78795_f) * 8.0f;
        this.tailfin1.field_78797_d = this.tailfin2.field_78797_d = this.tail2.field_78797_d - (float)Math.sin(this.tail2.field_78795_f) * 8.0f;
        this.belly.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.back.func_78785_a(f5);
        this.tail1.func_78785_a(f5);
        this.tail2.func_78785_a(f5);
        this.tailfin1.func_78785_a(f5);
        this.tailfin2.func_78785_a(f5);
        this.backfin.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.jaw.func_78785_a(f5);
        this.lfin1.func_78785_a(f5);
        this.lfin2.func_78785_a(f5);
        this.rfin1.func_78785_a(f5);
        this.rfin2.func_78785_a(f5);
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

