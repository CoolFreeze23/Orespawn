/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 */
package danger.orespawn;

import danger.orespawn.Robot5;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelRobot5
extends ModelBase {
    private float wingspeed = 1.0f;
    ModelRenderer lwheel1;
    ModelRenderer lwheel2;
    ModelRenderer rwheel1;
    ModelRenderer rwheel2;
    ModelRenderer axle;
    ModelRenderer drivebox;
    ModelRenderer stand;
    ModelRenderer swivel;
    ModelRenderer barrel1;
    ModelRenderer barrel2;
    ModelRenderer ammobox;

    public ModelRobot5(float f1) {
        this.wingspeed = f1;
        this.field_78090_t = 128;
        this.field_78089_u = 128;
        this.lwheel1 = new ModelRenderer((ModelBase)this, 0, 23);
        this.lwheel1.func_78789_a(0.0f, -4.0f, -4.0f, 2, 8, 8);
        this.lwheel1.func_78793_a(6.0f, 19.0f, 0.0f);
        this.lwheel1.func_78787_b(128, 128);
        this.lwheel1.field_78809_i = true;
        this.setRotation(this.lwheel1, 0.0f, 0.0f, 0.0f);
        this.lwheel2 = new ModelRenderer((ModelBase)this, 0, 43);
        this.lwheel2.func_78789_a(0.0f, -4.0f, -4.0f, 2, 8, 8);
        this.lwheel2.func_78793_a(6.0f, 19.0f, 0.0f);
        this.lwheel2.func_78787_b(128, 128);
        this.lwheel2.field_78809_i = true;
        this.setRotation(this.lwheel2, 0.7853982f, 0.0f, 0.0f);
        this.rwheel1 = new ModelRenderer((ModelBase)this, 0, 23);
        this.rwheel1.func_78789_a(0.0f, -4.0f, -4.0f, 2, 8, 8);
        this.rwheel1.func_78793_a(-8.0f, 19.0f, 0.0f);
        this.rwheel1.func_78787_b(128, 128);
        this.rwheel1.field_78809_i = true;
        this.setRotation(this.rwheel1, 0.0f, 0.0f, 0.0f);
        this.rwheel2 = new ModelRenderer((ModelBase)this, 0, 43);
        this.rwheel2.func_78789_a(0.0f, -4.0f, -4.0f, 2, 8, 8);
        this.rwheel2.func_78793_a(-8.0f, 19.0f, 0.0f);
        this.rwheel2.func_78787_b(128, 128);
        this.rwheel2.field_78809_i = true;
        this.setRotation(this.rwheel2, 0.7853982f, 0.0f, 0.0f);
        this.axle = new ModelRenderer((ModelBase)this, 42, 0);
        this.axle.func_78789_a(-6.0f, -0.5f, -0.5f, 12, 1, 1);
        this.axle.func_78793_a(0.0f, 19.0f, 0.0f);
        this.axle.func_78787_b(128, 128);
        this.axle.field_78809_i = true;
        this.setRotation(this.axle, 0.0f, 0.0f, 0.0f);
        this.drivebox = new ModelRenderer((ModelBase)this, 47, 4);
        this.drivebox.func_78789_a(-2.0f, -1.5f, -1.5f, 4, 3, 3);
        this.drivebox.func_78793_a(0.0f, 19.0f, 0.0f);
        this.drivebox.func_78787_b(128, 128);
        this.drivebox.field_78809_i = true;
        this.setRotation(this.drivebox, 0.0f, 0.0f, 0.0f);
        this.stand = new ModelRenderer((ModelBase)this, 35, 0);
        this.stand.func_78789_a(-0.5f, 0.0f, -0.5f, 1, 18, 1);
        this.stand.func_78793_a(0.0f, 0.0f, 0.0f);
        this.stand.func_78787_b(128, 128);
        this.stand.field_78809_i = true;
        this.setRotation(this.stand, 0.0f, 0.0f, 0.0f);
        this.swivel = new ModelRenderer((ModelBase)this, 22, 0);
        this.swivel.func_78789_a(-1.0f, 0.0f, -1.0f, 2, 1, 2);
        this.swivel.func_78793_a(0.0f, 0.0f, 0.0f);
        this.swivel.func_78787_b(128, 128);
        this.swivel.field_78809_i = true;
        this.setRotation(this.swivel, 0.0f, 0.0f, 0.0f);
        this.barrel1 = new ModelRenderer((ModelBase)this, 24, 25);
        this.barrel1.func_78789_a(-1.0f, -2.0f, -10.0f, 2, 2, 13);
        this.barrel1.func_78793_a(0.0f, 0.0f, 0.0f);
        this.barrel1.func_78787_b(128, 128);
        this.barrel1.field_78809_i = true;
        this.setRotation(this.barrel1, 0.0f, 0.0f, 0.0f);
        this.barrel2 = new ModelRenderer((ModelBase)this, 27, 43);
        this.barrel2.func_78789_a(-0.5f, -1.5f, -19.0f, 1, 1, 9);
        this.barrel2.func_78793_a(0.0f, 0.0f, 0.0f);
        this.barrel2.func_78787_b(128, 128);
        this.barrel2.field_78809_i = true;
        this.setRotation(this.barrel2, 0.0f, 0.0f, 0.0f);
        this.ammobox = new ModelRenderer((ModelBase)this, 0, 0);
        this.ammobox.func_78789_a(-2.0f, -2.0f, 3.0f, 4, 3, 5);
        this.ammobox.func_78793_a(0.0f, 0.0f, 0.0f);
        this.ammobox.func_78787_b(128, 128);
        this.ammobox.field_78809_i = true;
        this.setRotation(this.ammobox, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Robot5 e = (Robot5)entity;
        float newangle = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        if ((double)f1 > 0.1) {
            newangle = f2 * 0.15f % ((float)Math.PI * 2);
            newangle = Math.abs(newangle);
        } else {
            newangle = 0.0f;
        }
        this.lwheel1.field_78795_f = newangle;
        this.lwheel2.field_78795_f = (float)((double)newangle + 0.7853981633974483);
        this.rwheel1.field_78795_f = newangle;
        this.rwheel2.field_78795_f = (float)((double)newangle + 0.7853981633974483);
        this.barrel2.field_78796_g = this.ammobox.field_78796_g = (float)Math.toRadians((double)f3 / 2.0);
        this.barrel1.field_78796_g = this.ammobox.field_78796_g;
        this.lwheel1.func_78785_a(f5);
        this.lwheel2.func_78785_a(f5);
        this.rwheel1.func_78785_a(f5);
        this.rwheel2.func_78785_a(f5);
        this.axle.func_78785_a(f5);
        this.drivebox.func_78785_a(f5);
        this.stand.func_78785_a(f5);
        this.swivel.func_78785_a(f5);
        this.barrel1.func_78785_a(f5);
        this.barrel2.func_78785_a(f5);
        this.ammobox.func_78785_a(f5);
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

