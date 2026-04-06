/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.MyEntityAIWanderALot;
import java.util.List;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.world.World;

public class Cricket
extends EntityAnimal {
    public double moveSpeed = 0.15f;
    private int singing = 0;
    private int jumpcount = 0;

    public Cricket(World par1World) {
        super(par1World);
        this.func_70105_a(0.1f, 0.1f);
        this.field_70728_aV = 1;
        this.func_70661_as().func_75491_a(true);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.4));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 8, 1.0));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public int getSinging() {
        return this.field_70180_af.func_75683_a(20);
    }

    public void setSinging(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    private void jumpAround() {
        this.field_70181_x += (double)(0.55f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.35f));
        this.field_70163_u += 0.25;
        float f = 0.3f + Math.abs(this.field_70170_p.field_73012_v.nextFloat() * 0.25f);
        float d = (float)((double)this.field_70170_p.field_73012_v.nextFloat() * Math.PI * 2.0);
        this.field_70159_w += (double)f * Math.sin(d);
        this.field_70179_y += (double)f * Math.cos(d);
        this.field_70160_al = true;
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        super.func_70071_h_();
        if (!this.field_70170_p.field_72995_K) {
            if (this.singing != 0) {
                --this.singing;
                if (this.singing <= 0) {
                    this.setSinging(0);
                }
            }
            if (this.jumpcount > 0) {
                --this.jumpcount;
            }
            if (this.jumpcount == 0 && this.field_70170_p.field_73012_v.nextInt(50) == 1) {
                this.jumpAround();
                this.jumpcount = 50;
            }
        }
    }

    public boolean func_70650_aV() {
        return true;
    }

    public int mygetMaxHealth() {
        return 3;
    }

    protected String func_70639_aQ() {
        if (!this.field_70170_p.field_72995_K) {
            if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                return null;
            }
            this.singing = 40;
            this.setSinging(this.singing);
        }
        return "orespawn:cricket";
    }

    protected String func_70621_aR() {
        return null;
    }

    protected String func_70673_aS() {
        return null;
    }

    protected float func_70599_aP() {
        return 0.7f;
    }

    protected void playStepSound(int par1, int par2, int par3, int par4) {
    }

    protected void func_70628_a(boolean par1, int par2) {
    }

    protected boolean func_70041_e_() {
        return true;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 30.0) {
            return false;
        }
        return this.findBuddies() <= 5;
    }

    private int findBuddies() {
        List var5 = this.field_70170_p.func_72872_a(Cricket.class, this.field_70121_D.func_72314_b(20.0, 10.0, 20.0));
        return var5.size();
    }
}

