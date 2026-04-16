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
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.SoundEvent
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.util.ai.MyEntityAIWanderALot;
import java.util.List;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class Ant
extends EntityAnimal {
    public double moveSpeed = 0.15f;
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn:textures/entity/red_ant.png");

    public Ant(World par1World) {
        super(par1World);
        this.func_70105_a(0.1f, 0.1f);
        this.field_70728_aV = 0;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIPanic((EntityCreature)this, 1.4));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 9, 1.0));
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    public ResourceLocation getTexture(Ant a) {
        return texture2;
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    public void func_70071_h_() {
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        super.func_70071_h_();
        this.updateAITick();
    }

    public boolean interact(EntityPlayer par1EntityPlayer) {
        System.out.println("ANT INTERACTED");
        if (par1EntityPlayer == null) {
            return false;
        }
        if (!(par1EntityPlayer instanceof EntityPlayerMP)) {
            return false;
        }
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.func_190916_E() <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        return var2 == null;
    }

    public boolean isAIEnabled() {
        return true;
    }

    public int mygetMaxHealth() {
        return 1;
    }

    protected SoundEvent func_184639_G() {
        return null;
    }

    protected SoundEvent func_184601_bQ(DamageSource source) {
        return null;
    }

    protected SoundEvent func_184615_bR() {
        return null;
    }

    protected float func_70599_aP() {
        return 0.0f;
    }

    protected void playStepSound(int par1, int par2, int par3, int par4) {
    }

    protected void func_70628_a(boolean par1, int par2) {
    }

    protected boolean func_70041_e_() {
        return true;
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    public boolean func_70601_bi() {
        if (this.field_70163_u < 50.0) {
            return false;
        }
        return this.findBuddies() <= 4;
    }

    private int findBuddies() {
        List var5 = this.field_70170_p.func_72872_a(Ant.class, this.func_174813_aQ().func_72321_a(20.0, 10.0, 20.0));
        return var5.size();
    }

    public void updateAITick() {
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
    }
}

