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

import danger.orespawn.LeafMonster;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelLeafMonster
extends ModelBase {
    ModelRenderer body;
    ModelRenderer larm;
    ModelRenderer rarm;
    ModelRenderer lleg;
    ModelRenderer rleg;

    public ModelLeafMonster() {
        this.field_78090_t = 128;
        this.field_78089_u = 128;
        this.body = new ModelRenderer((ModelBase)this, 32, 32);
        this.body.func_78789_a(-8.0f, -8.0f, -8.0f, 16, 16, 16);
        this.body.func_78793_a(0.0f, 0.0f, 0.0f);
        this.body.func_78787_b(64, 32);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.larm = new ModelRenderer((ModelBase)this, 64, 0);
        this.larm.func_78789_a(0.0f, -16.0f, -8.0f, 16, 16, 16);
        this.larm.func_78793_a(8.0f, -8.0f, 0.0f);
        this.larm.func_78787_b(64, 32);
        this.larm.field_78809_i = true;
        this.setRotation(this.larm, 0.0f, 0.0f, 0.0f);
        this.rarm = new ModelRenderer((ModelBase)this, 0, 0);
        this.rarm.func_78789_a(-16.0f, -16.0f, -8.0f, 16, 16, 16);
        this.rarm.func_78793_a(-8.0f, -8.0f, 0.0f);
        this.rarm.func_78787_b(64, 32);
        this.rarm.field_78809_i = true;
        this.setRotation(this.rarm, 0.0f, 0.0f, 0.0f);
        this.lleg = new ModelRenderer((ModelBase)this, 64, 64);
        this.lleg.func_78789_a(0.0f, 0.0f, -8.0f, 16, 16, 16);
        this.lleg.func_78793_a(8.0f, 8.0f, 0.0f);
        this.lleg.func_78787_b(64, 32);
        this.lleg.field_78809_i = true;
        this.setRotation(this.lleg, 0.0f, 0.0f, 0.0f);
        this.rleg = new ModelRenderer((ModelBase)this, 0, 64);
        this.rleg.func_78789_a(-16.0f, 0.0f, -8.0f, 16, 16, 16);
        this.rleg.func_78793_a(-8.0f, 8.0f, 0.0f);
        this.rleg.func_78787_b(64, 32);
        this.rleg.field_78809_i = true;
        this.setRotation(this.rleg, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        LeafMonster lm = (LeafMonster)entity;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        if (lm.getAttacking() == 0) {
            this.body.field_78797_d = 16.0f;
            this.rarm.field_78797_d = 8.0f;
            this.larm.field_78797_d = 8.0f;
            this.rarm.field_78796_g = 0.0f;
            this.larm.field_78796_g = 0.0f;
            this.rarm.field_78795_f = 0.0f;
            this.larm.field_78795_f = 0.0f;
            this.lleg.field_78795_f = 0.0f;
            this.rleg.field_78795_f = 0.0f;
        } else {
            this.body.field_78797_d = 0.0f;
            this.rarm.field_78797_d = -8.0f;
            this.larm.field_78797_d = -8.0f;
            float newangle = (double)f1 > 0.1 ? MathHelper.func_76134_b((float)(f2 * 0.95f)) * (float)Math.PI * 0.25f * f1 : 0.0f;
            this.lleg.field_78795_f = newangle;
            this.rleg.field_78795_f = -newangle;
            newangle = MathHelper.func_76134_b((float)(f2 * 0.7f)) * (float)Math.PI * 0.55f;
            this.rarm.field_78796_g = -Math.abs(newangle);
            this.larm.field_78796_g = Math.abs(newangle);
            this.rarm.field_78795_f = -Math.abs(newangle);
            this.larm.field_78795_f = -Math.abs(newangle);
        }
        this.body.func_78785_a(f5);
        this.larm.func_78785_a(f5);
        this.rarm.func_78785_a(f5);
        this.lleg.func_78785_a(f5);
        this.rleg.func_78785_a(f5);
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

