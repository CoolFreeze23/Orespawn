/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelGhost
extends ModelBase {
    ModelRenderer HeadAndBody;
    ModelRenderer LArm;
    ModelRenderer RArm;

    public ModelGhost() {
        this.field_78090_t = 64;
        this.field_78089_u = 64;
        this.HeadAndBody = new ModelRenderer((ModelBase)this, 0, 0);
        this.HeadAndBody.func_78789_a(-3.0f, 0.0f, -3.0f, 6, 21, 6);
        this.HeadAndBody.func_78793_a(0.0f, 0.0f, 0.0f);
        this.HeadAndBody.func_78787_b(64, 64);
        this.HeadAndBody.field_78809_i = true;
        this.setRotation(this.HeadAndBody, 0.0f, 0.0f, 0.0f);
        this.LArm = new ModelRenderer((ModelBase)this, 34, 0);
        this.LArm.func_78789_a(-1.0f, -1.0f, -1.0f, 2, 11, 2);
        this.LArm.func_78793_a(3.0f, 6.0f, 0.0f);
        this.LArm.func_78787_b(64, 64);
        this.LArm.field_78809_i = true;
        this.setRotation(this.LArm, 0.0f, 0.0f, -0.3316126f);
        this.RArm = new ModelRenderer((ModelBase)this, 25, 0);
        this.RArm.func_78789_a(-1.0f, -1.0f, -1.0f, 2, 11, 2);
        this.RArm.func_78793_a(-3.0f, 6.0f, 0.0f);
        this.RArm.func_78787_b(64, 64);
        this.RArm.field_78809_i = true;
        this.setRotation(this.RArm, 0.0f, 0.0f, 0.3316126f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        this.LArm.field_78808_h = -0.33f + MathHelper.func_76134_b((float)(f2 * 0.3f)) * (float)Math.PI * 0.05f;
        this.RArm.field_78808_h = 0.33f + MathHelper.func_76134_b((float)(f2 * 0.32f)) * (float)Math.PI * 0.05f;
        this.LArm.field_78795_f = -0.33f + MathHelper.func_76134_b((float)(f2 * 0.34f)) * (float)Math.PI * 0.05f;
        this.RArm.field_78795_f = 0.33f + MathHelper.func_76134_b((float)(f2 * 0.36f)) * (float)Math.PI * 0.05f;
        GL11.glPushMatrix();
        GL11.glEnable((int)2977);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)0.75f, (float)0.75f, (float)0.75f, (float)0.25f);
        this.HeadAndBody.func_78785_a(f5);
        this.LArm.func_78785_a(f5);
        this.RArm.func_78785_a(f5);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
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

