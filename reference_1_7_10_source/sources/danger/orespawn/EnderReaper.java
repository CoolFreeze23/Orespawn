/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EnderReaper
extends EntityMob {
    private static final UUID attackingSpeedBoostModifierUUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier attackingSpeedBoostModifier = new AttributeModifier(attackingSpeedBoostModifierUUID, "Attacking speed boost", (double)6.2f, 0).func_111168_a(false);
    private int teleportDelay;
    private int stareTimer;
    private Entity lastEntityToAttack;

    public EnderReaper(World par1World) {
        super(par1World);
        this.func_70105_a(0.7f, 2.9f);
        this.field_70138_W = 1.0f;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)OreSpawnMain.EnderReaper_stats.health);
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.37);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.EnderReaper_stats.attack);
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(18, (Object)new Byte(0));
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
    }

    protected Entity func_70782_k() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        EntityPlayer entityplayer = this.field_70170_p.func_72856_b((Entity)this, 81.0);
        if (entityplayer != null) {
            if (this.shouldAttackPlayer(entityplayer)) {
                if (this.stareTimer == 0) {
                    this.field_70170_p.func_72956_a((Entity)entityplayer, "mob.endermen.stare", 1.0f, 1.0f);
                }
                if (this.stareTimer++ == 5) {
                    this.stareTimer = 0;
                }
                this.setScreaming(true);
                return entityplayer;
            }
            this.stareTimer = 0;
            this.setScreaming(false);
        }
        return null;
    }

    private boolean shouldAttackPlayer(EntityPlayer par1EntityPlayer) {
        ItemStack itemstack = par1EntityPlayer.field_71071_by.field_70460_b[3];
        if (itemstack != null && itemstack.func_77973_b() == Item.func_150898_a((Block)Blocks.field_150423_aK)) {
            return false;
        }
        Vec3 vec3 = par1EntityPlayer.func_70676_i(1.0f).func_72432_b();
        Vec3 vec31 = Vec3.func_72443_a((double)(this.field_70165_t - par1EntityPlayer.field_70165_t), (double)(this.field_70121_D.field_72338_b + (double)(this.field_70131_O / 2.0f) - (par1EntityPlayer.field_70163_u + (double)par1EntityPlayer.func_70047_e())), (double)(this.field_70161_v - par1EntityPlayer.field_70161_v));
        double d0 = vec31.func_72433_c();
        double d1 = vec3.func_72430_b(vec31 = vec31.func_72432_b());
        return d1 > 1.0 - 0.025 / d0 ? par1EntityPlayer.func_70685_l((Entity)this) : false;
    }

    public void func_70636_d() {
        float f;
        if (this.func_70026_G()) {
            this.func_70097_a(DamageSource.field_76369_e, 1.0f);
        }
        if (this.lastEntityToAttack != this.field_70789_a) {
            IAttributeInstance attributeinstance = this.func_110148_a(SharedMonsterAttributes.field_111263_d);
            attributeinstance.func_111124_b(attackingSpeedBoostModifier);
            if (this.field_70789_a != null) {
                attributeinstance.func_111121_a(attackingSpeedBoostModifier);
            }
        }
        this.lastEntityToAttack = this.field_70789_a;
        for (int i = 0; i < 2; ++i) {
            this.field_70170_p.func_72869_a("portal", this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5) * (double)this.field_70130_N, this.field_70163_u + this.field_70146_Z.nextDouble() * (double)this.field_70131_O - 0.25, this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5) * (double)this.field_70130_N, (this.field_70146_Z.nextDouble() - 0.5) * 2.0, -this.field_70146_Z.nextDouble(), (this.field_70146_Z.nextDouble() - 0.5) * 2.0);
        }
        if (this.field_70170_p.func_72935_r() && !this.field_70170_p.field_72995_K && (f = this.func_70013_c(1.0f)) > 0.5f && this.field_70170_p.func_72937_j(MathHelper.func_76128_c((double)this.field_70165_t), MathHelper.func_76128_c((double)this.field_70163_u), MathHelper.func_76128_c((double)this.field_70161_v)) && this.field_70146_Z.nextFloat() * 30.0f < (f - 0.4f) * 2.0f) {
            this.field_70789_a = null;
            this.setScreaming(false);
            this.teleportRandomly();
        }
        if (this.func_70026_G() || this.func_70027_ad()) {
            this.setScreaming(false);
            this.teleportRandomly();
        }
        this.field_70703_bu = false;
        if (this.field_70789_a != null) {
            this.func_70625_a(this.field_70789_a, 100.0f, 100.0f);
        }
        if (!this.field_70170_p.field_72995_K && this.func_70089_S()) {
            if (this.field_70789_a != null) {
                if (this.field_70789_a instanceof EntityPlayer && this.shouldAttackPlayer((EntityPlayer)this.field_70789_a)) {
                    if (this.field_70789_a.func_70068_e((Entity)this) < 16.0) {
                        this.teleportRandomly();
                    }
                    this.teleportDelay = 0;
                } else if (this.field_70789_a.func_70068_e((Entity)this) > 256.0 && this.teleportDelay++ >= 30 && this.teleportToEntity(this.field_70789_a)) {
                    this.teleportDelay = 0;
                }
            } else {
                this.setScreaming(false);
                this.teleportDelay = 0;
            }
        }
        super.func_70636_d();
    }

    protected boolean teleportRandomly() {
        double d0 = this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5) * 64.0;
        double d1 = this.field_70163_u + (double)(this.field_70146_Z.nextInt(64) - 32);
        double d2 = this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5) * 64.0;
        return this.teleportTo(d0, d1, d2);
    }

    protected boolean teleportToEntity(Entity par1Entity) {
        Vec3 vec3 = Vec3.func_72443_a((double)(this.field_70165_t - par1Entity.field_70165_t), (double)(this.field_70121_D.field_72338_b + (double)(this.field_70131_O / 2.0f) - par1Entity.field_70163_u + (double)par1Entity.func_70047_e()), (double)(this.field_70161_v - par1Entity.field_70161_v));
        vec3 = vec3.func_72432_b();
        double d0 = 16.0;
        double d1 = this.field_70165_t + (this.field_70146_Z.nextDouble() - 0.5) * 8.0 - vec3.field_72450_a * d0;
        double d2 = this.field_70163_u + (double)(this.field_70146_Z.nextInt(16) - 8) - vec3.field_72448_b * d0;
        double d3 = this.field_70161_v + (this.field_70146_Z.nextDouble() - 0.5) * 8.0 - vec3.field_72449_c * d0;
        return this.teleportTo(d1, d2, d3);
    }

    protected boolean teleportTo(double par1, double par3, double par5) {
        int k;
        int j;
        double d3 = this.field_70165_t;
        double d4 = this.field_70163_u;
        double d5 = this.field_70161_v;
        this.field_70165_t = par1;
        this.field_70163_u = par3;
        this.field_70161_v = par5;
        boolean flag = false;
        int i = MathHelper.func_76128_c((double)this.field_70165_t);
        if (this.field_70170_p.func_72899_e(i, j = MathHelper.func_76128_c((double)this.field_70163_u), k = MathHelper.func_76128_c((double)this.field_70161_v))) {
            boolean flag1 = false;
            while (!flag1 && j > 0) {
                Block l = this.field_70170_p.func_147439_a(i, j - 1, k);
                if (l != Blocks.field_150350_a && l.func_149688_o().func_76230_c()) {
                    flag1 = true;
                    continue;
                }
                this.field_70163_u -= 1.0;
                --j;
            }
            if (flag1) {
                this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
                if (this.field_70170_p.func_72945_a((Entity)this, this.field_70121_D).isEmpty() && !this.field_70170_p.func_72953_d(this.field_70121_D)) {
                    flag = true;
                }
            }
        }
        if (!flag) {
            this.func_70107_b(d3, d4, d5);
            return false;
        }
        int short1 = 128;
        for (int lx = 0; lx < short1; ++lx) {
            double d6 = (double)lx / ((double)short1 - 1.0);
            float f = (this.field_70146_Z.nextFloat() - 0.5f) * 0.2f;
            float f1 = (this.field_70146_Z.nextFloat() - 0.5f) * 0.2f;
            float f2 = (this.field_70146_Z.nextFloat() - 0.5f) * 0.2f;
            double d7 = d3 + (this.field_70165_t - d3) * d6 + (this.field_70146_Z.nextDouble() - 0.5) * (double)this.field_70130_N * 2.0;
            double d8 = d4 + (this.field_70163_u - d4) * d6 + this.field_70146_Z.nextDouble() * (double)this.field_70131_O;
            double d9 = d5 + (this.field_70161_v - d5) * d6 + (this.field_70146_Z.nextDouble() - 0.5) * (double)this.field_70130_N * 2.0;
            this.field_70170_p.func_72869_a("portal", d7, d8, d9, (double)f, (double)f1, (double)f2);
        }
        this.field_70170_p.func_72908_a(d3, d4, d5, "mob.endermen.portal", 1.0f, 1.0f);
        this.func_85030_a("mob.endermen.portal", 1.0f, 1.0f);
        return true;
    }

    protected String func_70639_aQ() {
        return this.isScreaming() ? "mob.endermen.scream" : "mob.endermen.idle";
    }

    protected String func_70621_aR() {
        return "mob.endermen.hit";
    }

    protected String func_70673_aS() {
        return "mob.endermen.death";
    }

    protected Item func_146068_u() {
        return Items.field_151061_bv;
    }

    public int func_70658_aO() {
        return OreSpawnMain.EnderReaper_stats.defense;
    }

    protected void func_70628_a(boolean par1, int par2) {
        Item j = this.func_146068_u();
        if (j != null) {
            int k = this.field_70146_Z.nextInt(2 + par2);
            for (int l = 0; l < k; ++l) {
                this.func_145779_a(j, 1);
            }
        }
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        if (this.func_85032_ar()) {
            return false;
        }
        this.setScreaming(true);
        if (par1DamageSource instanceof EntityDamageSourceIndirect) {
            for (int i = 0; i < 16; ++i) {
                if (!this.teleportRandomly()) continue;
                return true;
            }
            return super.func_70097_a(par1DamageSource, par2);
        }
        return super.func_70097_a(par1DamageSource, par2);
    }

    public boolean func_70601_bi() {
        for (int k = -3; k < 3; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 0; i < 5; ++i) {
                    Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Ender Reaper")) continue;
                    return true;
                }
            }
        }
        if (!this.func_70814_o()) {
            return false;
        }
        if (this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70163_u < 30.0) {
            return false;
        }
        EnderReaper target = null;
        target = (EnderReaper)this.field_70170_p.func_72857_a(EnderReaper.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
        return target == null;
    }

    public boolean isScreaming() {
        return this.field_70180_af.func_75683_a(18) > 0;
    }

    public void setScreaming(boolean par1) {
        this.field_70180_af.func_75692_b(18, (Object)((byte)(par1 ? 1 : 0)));
    }
}

