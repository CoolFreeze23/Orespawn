/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelBase
 *  net.minecraft.client.model.ModelRenderer
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.MathHelper
 *  org.lwjgl.opengl.GL11
 */
package danger.orespawn;

import danger.orespawn.GhostSkelly;
import danger.orespawn.RenderInfo;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelGhostSkelly
extends ModelBase {
    ModelRenderer body;
    ModelRenderer shirt;
    ModelRenderer head;
    ModelRenderer stem;
    ModelRenderer rarm;
    ModelRenderer larm;
    ModelRenderer rsleeve;
    ModelRenderer lsleeve;
    ModelRenderer lchains;
    ModelRenderer rchains;

    public ModelGhostSkelly() {
        this.field_78090_t = 128;
        this.field_78089_u = 64;
        this.body = new ModelRenderer((ModelBase)this, 0, 0);
        this.body.func_78789_a(0.0f, 0.0f, 0.0f, 1, 21, 1);
        this.body.func_78793_a(0.0f, -1.0f, 0.0f);
        this.body.func_78787_b(128, 64);
        this.body.field_78809_i = true;
        this.setRotation(this.body, 0.0f, 0.0f, 0.0f);
        this.shirt = new ModelRenderer((ModelBase)this, 42, 43);
        this.shirt.func_78789_a(-2.0f, 0.0f, -2.0f, 5, 12, 5);
        this.shirt.func_78793_a(0.0f, 0.0f, 0.0f);
        this.shirt.func_78787_b(128, 64);
        this.shirt.field_78809_i = true;
        this.setRotation(this.shirt, 0.0f, 0.0f, 0.0f);
        this.head = new ModelRenderer((ModelBase)this, 40, 29);
        this.head.func_78789_a(-3.0f, 0.0f, -3.0f, 7, 5, 7);
        this.head.func_78793_a(0.0f, -6.0f, 0.0f);
        this.head.func_78787_b(128, 64);
        this.head.field_78809_i = true;
        this.setRotation(this.head, 0.0f, 0.0f, 0.0f);
        this.stem = new ModelRenderer((ModelBase)this, 49, 23);
        this.stem.func_78789_a(0.0f, 0.0f, 0.0f, 1, 2, 1);
        this.stem.func_78793_a(0.0f, -8.0f, 0.0f);
        this.stem.func_78787_b(128, 64);
        this.stem.field_78809_i = true;
        this.setRotation(this.stem, 0.1745329f, 0.0f, 0.1745329f);
        this.rarm = new ModelRenderer((ModelBase)this, 26, 0);
        this.rarm.func_78789_a(-14.0f, 0.0f, 0.0f, 15, 1, 1);
        this.rarm.func_78793_a(0.0f, 0.0f, 0.0f);
        this.rarm.func_78787_b(128, 64);
        this.rarm.field_78809_i = true;
        this.setRotation(this.rarm, 0.0f, 0.0f, 0.0f);
        this.larm = new ModelRenderer((ModelBase)this, 63, 0);
        this.larm.func_78789_a(0.0f, 0.0f, 0.0f, 15, 1, 1);
        this.larm.func_78793_a(0.0f, 0.0f, 0.0f);
        this.larm.func_78787_b(128, 64);
        this.larm.field_78809_i = true;
        this.setRotation(this.larm, 0.0f, 0.0f, 0.0f);
        this.rsleeve = new ModelRenderer((ModelBase)this, 31, 7);
        this.rsleeve.func_78789_a(-11.0f, 0.0f, -1.0f, 9, 8, 3);
        this.rsleeve.func_78793_a(0.0f, 0.0f, 0.0f);
        this.rsleeve.func_78787_b(128, 64);
        this.rsleeve.field_78809_i = true;
        this.setRotation(this.rsleeve, 0.0f, 0.0f, 0.0f);
        this.lsleeve = new ModelRenderer((ModelBase)this, 71, 7);
        this.lsleeve.func_78789_a(3.0f, 0.0f, -1.0f, 9, 8, 3);
        this.lsleeve.func_78793_a(0.0f, 0.0f, 0.0f);
        this.lsleeve.func_78787_b(128, 64);
        this.lsleeve.field_78809_i = true;
        this.setRotation(this.lsleeve, 0.0f, 0.0f, 0.0f);
        this.lchains = new ModelRenderer((ModelBase)this, 98, 0);
        this.lchains.func_78789_a(11.0f, -1.0f, 0.0f, 3, 16, 1);
        this.lchains.func_78793_a(0.0f, 0.0f, 0.0f);
        this.lchains.func_78787_b(128, 64);
        this.lchains.field_78809_i = true;
        this.setRotation(this.lchains, 0.0f, 0.0f, 0.0f);
        this.rchains = new ModelRenderer((ModelBase)this, 12, 0);
        this.rchains.func_78789_a(-13.0f, -1.0f, 0.0f, 3, 10, 1);
        this.rchains.func_78793_a(0.0f, 0.0f, 0.0f);
        this.rchains.func_78787_b(128, 64);
        this.rchains.field_78809_i = true;
        this.setRotation(this.rchains, 0.0f, 0.0f, 0.0f);
    }

    public void func_78088_a(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        GhostSkelly e = (GhostSkelly)entity;
        RenderInfo r = null;
        float newangle = 0.0f;
        float newrf1 = 0.0f;
        super.func_78088_a(entity, f, f1, f2, f3, f4, f5);
        this.func_78087_a(f, f1, f2, f3, f4, f5, entity);
        r = e.getRenderInfo();
        this.lsleeve.field_78808_h = this.lchains.field_78808_h = MathHelper.func_76134_b((float)(f2 * 0.2f)) * (float)Math.PI * 0.05f;
        this.larm.field_78808_h = this.lchains.field_78808_h;
        this.rsleeve.field_78808_h = this.rchains.field_78808_h = MathHelper.func_76134_b((float)(f2 * 0.22f)) * (float)Math.PI * 0.05f;
        this.rarm.field_78808_h = this.rchains.field_78808_h;
        this.lsleeve.field_78796_g = this.lchains.field_78796_g = MathHelper.func_76134_b((float)(f2 * 0.24f)) * (float)Math.PI * 0.05f;
        this.larm.field_78796_g = this.lchains.field_78796_g;
        this.rsleeve.field_78796_g = this.rchains.field_78796_g = MathHelper.func_76134_b((float)(f2 * 0.26f)) * (float)Math.PI * 0.05f;
        this.rarm.field_78796_g = this.rchains.field_78796_g;
        newangle = MathHelper.func_76134_b((float)(f2 * 0.05f)) * (float)Math.PI * 2.0f;
        newrf1 = f2 * 0.05f % ((float)Math.PI * 2);
        newrf1 = Math.abs(newrf1);
        if (newrf1 < r.rf2) {
            r.ri2 = 0;
            if (e.field_70170_p.field_73012_v.nextInt(3) == 1) {
                r.ri2 |= 1;
            }
        }
        r.rf2 = newrf1;
        if ((r.ri2 & 1) == 0) {
            newangle = 0.0f;
        }
        this.head.field_78796_g = newangle;
        e.setRenderInfo(r);
        GL11.glPushMatrix();
        GL11.glEnable((int)2977);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)0.75f, (float)0.75f, (float)0.75f, (float)0.25f);
        this.body.func_78785_a(f5);
        this.shirt.func_78785_a(f5);
        this.head.func_78785_a(f5);
        this.stem.func_78785_a(f5);
        this.rarm.func_78785_a(f5);
        this.larm.func_78785_a(f5);
        this.rsleeve.func_78785_a(f5);
        this.lsleeve.func_78785_a(f5);
        this.lchains.func_78785_a(f5);
        this.rchains.func_78785_a(f5);
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

