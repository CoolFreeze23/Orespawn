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

import danger.orespawn.RenderInfo;
import danger.orespawn.RubberDucky;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelRubberDucky
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer bottom;
    ModelRenderer body;
    ModelRenderer back;
    ModelRenderer neck;
    ModelRenderer head;
    ModelRenderer beak;
    ModelRenderer Lwing;
    ModelRenderer Rwing;

    public ModelRubberDucky(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.bottom = new ModelRenderer((ModelBase)this, 0, 56);
        this.bottom.func_78789_a(-2.0f, 0.0f, -2.0f, 4, 1, 4);
        this.bottom.func_78793_a(0.0f, 23.0f, 0.0f);
        this.bottom.func_78787_b(64, 64);
        this.bottom.field_78809_i = true;
        this.setRotation(this.bottom, 0.0f, 0.0f, 0.0f);
        this.body = new ModelRenderer((ModelBase)this, 0, 45);
        this.body.func_78789_a(-3.0f, 0.0f, -3.0f, 6, 2, 8);
        this.body.func_78793_a(0.0f, 21.0f, 0.0f);
        this.body.func_78787_b(64, 64);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.back = new ModelRenderer((ModelBase)this, 0, 33);
        this.back.func_78789_a(-3.0f, 0.0f, -3.0f, 6, 1, 10);
        this.back.func_78793_a(0.0f, 20.0f, 0.0f);
        this.back.func_78787_b(64, 64);
        this.back.field_78809_i = true;
        this.setRotation(this.back, 0.0f, 0.0f, 0.0f);
        this.neck = new ModelRenderer((ModelBase)this, 17, 27);
        this.neck.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 1, 2);
        this.neck.func_78793_a(0.0f, 19.0f, -1.0f);
        this.neck.func_78787_b(64, 64);
        this.neck.field_78809_i = true;
        this.setRotation(this.neck, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 13, 18);
        this.head.func_78789_a(-2.0f, -4.0f, -2.0f, 4, 4, 4);
        this.head.func_78793_a(0.0f, 19.0f, -1.0f);
        this.head.func_78787_b(64, 64);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.beak = new ModelRenderer((ModelBase)this, 0, 21);
        this.beak.func_78789_a(-1.5f, -1.0f, -5.0f, 3, 1, 3);
        this.beak.func_78793_a(0.0f, 19.0f, -1.0f);
        this.beak.func_78787_b(64, 64);
        this.beak.field_78809_i = true;
        this.setRotation(this.beak, 0.0f, 0.0f, 0.0f);
        this.Lwing = new ModelRenderer((ModelBase)this, 0, 0);
        this.Lwing.func_78789_a(0.0f, -0.5f, 0.0f, 2, 1, 5);
        this.Lwing.func_78793_a(3.0f, 21.0f, -2.0f);
        this.Lwing.func_78787_b(64, 64);
        this.Lwing.field_78809_i = true;
        this.setRotation(this.Lwing, 0.0f, 0.0f, 0.0f);
        this.Rwing = new ModelRenderer((ModelBase)this, 17, 0);
        this.Rwing.func_78789_a(-2.0f, -0.5f, 0.0f, 2, 1, 5);
        this.Rwing.func_78793_a(-3.0f, 21.0f, -2.0f);
        this.Rwing.func_78787_b(64, 64);
        this.Rwing.field_78809_i = true;
        this.setRotation(this.Rwing, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        RubberDucky c = (RubberDucky)entity;
        RenderInfo r = null;
        float hf = 0.0f;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 2.3f * this.wingspeed)) * (float)Math.PI * 0.25f * f1 : 0.0f;
        this.beak.field_78796_g = this.head.field_78796_g = (float)Math.toRadians(f3) * 0.45f;
        this.beak.field_78795_f = this.head.field_78795_f = (float)Math.toRadians(f4) * 0.65f;
        r = c.getRenderInfo();
        newangle = MathHelper.func_76134_b((float)(f2 * 1.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        nextangle = MathHelper.func_76134_b((float)((f2 + 0.3f) * 1.0f * this.wingspeed)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (c.field_70170_p.field_73012_v.nextInt(3) == 1) {
                r.ri1 = 1;
            }
            if (c.getKillCount() >= 5) {
                if (c.field_70170_p.field_73012_v.nextInt(2) == 1) {
                    r.ri1 = 1;
                }
                newangle *= 4.0f;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        if (c.func_70906_o()) {
            newangle = 0.0f;
        }
        newangle = Math.abs(newangle);
        this.Lwing.field_78808_h = -newangle;
        this.Lwing.field_78796_g = newangle / 2.0f;
        this.Rwing.field_78808_h = newangle;
        this.Rwing.field_78796_g = -newangle / 2.0f;
        c.setRenderInfo(r);
        this.bottom.func_78785_a(f5);
        this.body.func_78785_a(f5);
        this.back.func_78785_a(f5);
        this.neck.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.beak.func_78785_a(f5);
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

