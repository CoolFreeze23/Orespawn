/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.MathHelper
 */
package danger.orespawn.entity.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelBird
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer Body;
    ModelRenderer Head;
    ModelRenderer Beak;
    ModelRenderer LowerBeak;
    ModelRenderer feather2;
    ModelRenderer feather1;
    ModelRenderer feather3;
    ModelRenderer tailfeather1;
    ModelRenderer rwing1;
    ModelRenderer lwing1;
    ModelRenderer leg;
    ModelRenderer otherleg;
    ModelRenderer lwing2;
    ModelRenderer rwing2;
    ModelRenderer tailfeather2;
    ModelRenderer tailfeather3;

    public ModelBird(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 64;
        this.field_78089_u = 32;
        this.Body = new ModelRenderer((ModelBase)this, 0, 0);
        this.Body.func_78789_a(0.0f, 0.0f, 0.0f, 5, 3, 6);
        this.Body.func_78793_a(-1.0f, 18.0f, 0.0f);
        this.Body.func_78787_b(64, 32);
        this.Body.field_78809_i = true;
        this.setRotation(this.Body, 0.0f, 0.0f, 0.0f);
        this.Head = new ModelRenderer((ModelBase)this, 22, 0);
        this.Head.func_78789_a(0.0f, 0.0f, 0.0f, 3, 3, 4);
        this.Head.func_78793_a(0.0f, 16.0f, -3.0f);
        this.Head.func_78787_b(64, 32);
        this.Head.field_78809_i = true;
        this.setRotation(this.Head, 0.0f, 0.0f, 0.0f);
        this.Beak = new ModelRenderer((ModelBase)this, 0, 21);
        this.Beak.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 3);
        this.Beak.func_78793_a(1.0f, 17.0f, -6.0f);
        this.Beak.func_78787_b(64, 32);
        this.Beak.field_78809_i = true;
        this.setRotation(this.Beak, 0.0f, 0.0f, 0.0f);
        this.LowerBeak = new ModelRenderer((ModelBase)this, 1, 17);
        this.LowerBeak.func_78789_a(0.0f, 0.0f, 0.0f, 1, 1, 1);
        this.LowerBeak.func_78793_a(1.0f, 18.0f, -4.0f);
        this.LowerBeak.func_78787_b(64, 32);
        this.LowerBeak.field_78809_i = true;
        this.setRotation(this.LowerBeak, 0.0f, 0.0f, 0.0f);
        this.feather2 = new ModelRenderer((ModelBase)this, 15, 9);
        this.feather2.func_78789_a(0.0f, -2.5f, -0.75f, 1, 3, 1);
        this.feather2.func_78793_a(1.0f, 16.0f, 0.0f);
        this.feather2.func_78787_b(64, 32);
        this.feather2.field_78809_i = true;
        this.setRotation(this.feather2, -0.6426736f, 0.0f, 0.0f);
        this.feather1 = new ModelRenderer((ModelBase)this, 11, 9);
        this.feather1.func_78789_a(0.0f, -2.5f, -0.5f, 1, 3, 1);
        this.feather1.func_78793_a(1.0f, 16.0f, -2.0f);
        this.feather1.func_78787_b(64, 32);
        this.feather1.field_78809_i = true;
        this.setRotation(this.feather1, -0.2230717f, 0.0f, 0.0f);
        this.feather3 = new ModelRenderer((ModelBase)this, 19, 9);
        this.feather3.func_78789_a(0.0f, -3.0f, 0.5f, 1, 4, 1);
        this.feather3.func_78793_a(1.0f, 16.0f, 1.0f);
        this.feather3.func_78787_b(64, 32);
        this.feather3.field_78809_i = true;
        this.setRotation(this.feather3, -1.276259f, 0.0f, 0.0f);
        this.tailfeather1 = new ModelRenderer((ModelBase)this, 46, 15);
        this.tailfeather1.func_78789_a(0.0f, 0.0f, 0.0f, 3, 2, 3);
        this.tailfeather1.func_78793_a(0.0f, 18.0f, 6.0f);
        this.tailfeather1.func_78787_b(64, 32);
        this.tailfeather1.field_78809_i = true;
        this.setRotation(this.tailfeather1, 0.0f, 0.0f, 0.0f);
        this.rwing1 = new ModelRenderer((ModelBase)this, 23, 9);
        this.rwing1.func_78789_a(0.0f, 0.0f, 0.0f, 1, 4, 4);
        this.rwing1.func_78793_a(-1.0f, 18.0f, 1.0f);
        this.rwing1.func_78787_b(64, 32);
        this.rwing1.field_78809_i = true;
        this.setRotation(this.rwing1, 0.0f, 0.0f, 1.595066f);
        this.lwing1 = new ModelRenderer((ModelBase)this, 33, 9);
        this.lwing1.func_78789_a(-1.0f, 0.0f, 0.0f, 1, 4, 4);
        this.lwing1.func_78793_a(4.0f, 18.0f, 1.0f);
        this.lwing1.func_78787_b(64, 32);
        this.lwing1.field_78809_i = true;
        this.setRotation(this.lwing1, 0.0f, 0.0f, -1.561488f);
        this.leg = new ModelRenderer((ModelBase)this, 4, 12);
        this.leg.func_78789_a(0.0f, 0.0f, 0.0f, 1, 3, 1);
        this.leg.func_78793_a(2.0f, 21.0f, 3.0f);
        this.leg.func_78787_b(64, 32);
        this.leg.field_78809_i = true;
        this.setRotation(this.leg, 0.8726646f, 0.0f, 0.0f);
        this.otherleg = new ModelRenderer((ModelBase)this, 0, 12);
        this.otherleg.func_78789_a(0.0f, 0.0f, 0.0f, 1, 3, 1);
        this.otherleg.func_78793_a(0.0f, 21.0f, 3.0f);
        this.otherleg.func_78787_b(64, 32);
        this.otherleg.field_78809_i = true;
        this.setRotation(this.otherleg, 0.6108652f, 0.0f, 0.0f);
        this.lwing2 = new ModelRenderer((ModelBase)this, 10, 14);
        this.lwing2.func_78789_a(4.0f, 0.0f, 0.0f, 3, 1, 3);
        this.lwing2.func_78793_a(4.0f, 18.0f, 1.0f);
        this.lwing2.func_78787_b(64, 32);
        this.lwing2.field_78809_i = true;
        this.setRotation(this.lwing2, 0.0f, 0.0f, 0.0f);
        this.rwing2 = new ModelRenderer((ModelBase)this, 10, 19);
        this.rwing2.func_78789_a(-7.0f, 0.0f, 0.0f, 3, 1, 3);
        this.rwing2.func_78793_a(-1.0f, 18.0f, 1.0f);
        this.rwing2.func_78787_b(64, 32);
        this.rwing2.field_78809_i = true;
        this.setRotation(this.rwing2, 0.0f, 0.0f, 0.0f);
        this.tailfeather2 = new ModelRenderer((ModelBase)this, 44, 20);
        this.tailfeather2.func_78789_a(-0.5f, 0.0f, 3.0f, 4, 1, 4);
        this.tailfeather2.func_78793_a(0.0f, 18.0f, 6.0f);
        this.tailfeather2.func_78787_b(64, 32);
        this.tailfeather2.field_78809_i = true;
        this.setRotation(this.tailfeather2, 0.0f, 0.0f, 0.0f);
        this.tailfeather3 = new ModelRenderer((ModelBase)this, 36, 26);
        this.tailfeather3.func_78789_a(-1.0f, 0.0f, 7.0f, 5, 1, 4);
        this.tailfeather3.func_78793_a(0.0f, 18.0f, 6.0f);
        this.tailfeather3.func_78787_b(64, 32);
        this.tailfeather3.field_78809_i = true;
        this.setRotation(this.tailfeather3, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        float newangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        newangle = MathHelper.func_76134_b((float)(f2 * 1.5f * this.wingspeed)) * (float)Math.PI * 0.35f;
        this.lwing1.field_78808_h = -1.5f + newangle;
        this.lwing2.field_78808_h = newangle;
        this.rwing1.field_78808_h = 1.5f - newangle;
        this.rwing2.field_78808_h = -newangle;
        this.tailfeather1.field_78795_f = newangle = MathHelper.func_76134_b((float)(f2 * 0.3f * this.wingspeed)) * (float)Math.PI * 0.1f;
        this.tailfeather2.field_78795_f = newangle;
        this.tailfeather3.field_78795_f = newangle;
        this.feather1.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 1.1f * this.wingspeed)) * (float)Math.PI * 0.08f;
        this.feather2.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 1.2f * this.wingspeed)) * (float)Math.PI * 0.08f;
        this.feather3.field_78808_h = newangle = MathHelper.func_76134_b((float)(f2 * 1.3f * this.wingspeed)) * (float)Math.PI * 0.08f;
        this.Body.func_78785_a(f5);
        this.Head.func_78785_a(f5);
        this.Beak.func_78785_a(f5);
        this.LowerBeak.func_78785_a(f5);
        this.feather2.func_78785_a(f5);
        this.feather1.func_78785_a(f5);
        this.feather3.func_78785_a(f5);
        this.tailfeather1.func_78785_a(f5);
        this.rwing1.func_78785_a(f5);
        this.lwing1.func_78785_a(f5);
        this.leg.func_78785_a(f5);
        this.otherleg.func_78785_a(f5);
        this.lwing2.func_78785_a(f5);
        this.rwing2.func_78785_a(f5);
        this.tailfeather2.func_78785_a(f5);
        this.tailfeather3.func_78785_a(f5);
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

