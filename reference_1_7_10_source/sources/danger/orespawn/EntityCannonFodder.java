/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Chipmunk;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Lizard;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.VelocityRaptor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class EntityCannonFodder
extends EntityTameable {
    String name_one = null;
    String name_two = null;
    private int is_activated = 0;
    private int hat_color = 0;
    private int syncer = 0;
    private int px = 0;
    private int pz = 0;
    private int py = 0;
    private GenericTargetSorter LocalTargetSorter = new GenericTargetSorter((Entity)this);

    public EntityCannonFodder(World par1World) {
        super(par1World);
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)0);
        this.field_70180_af.func_75682_a(21, (Object)0);
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        ++this.syncer;
        if (this.syncer > 5) {
            if (this.field_70170_p.field_72995_K) {
                this.is_activated = this.field_70180_af.func_75679_c(20);
                this.hat_color = this.field_70180_af.func_75679_c(21);
            } else {
                this.field_70180_af.func_75692_b(20, (Object)this.is_activated);
                this.field_70180_af.func_75692_b(21, (Object)this.hat_color);
            }
            this.syncer = 0;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (super.func_70085_c(par1EntityPlayer)) {
            return true;
        }
        if (this.name_one != null && this.func_70909_n()) {
            if (this.name_one.equals(par1EntityPlayer.func_110124_au().toString())) {
                if (this.name_two == null) {
                    this.name_two = this.name_one;
                    this.name_one = par1EntityPlayer.func_110124_au().toString();
                    this.func_152115_b(this.name_one);
                    this.is_activated = 2;
                }
            } else if (this.name_two != null) {
                if (!this.name_two.equals(par1EntityPlayer.func_110124_au().toString())) return true;
                this.name_two = this.name_one;
                this.name_one = par1EntityPlayer.func_110124_au().toString();
                this.func_152115_b(this.name_one);
                this.is_activated = 2;
            } else {
                this.name_two = this.name_one;
                this.name_one = par1EntityPlayer.func_110124_au().toString();
                this.func_152115_b(this.name_one);
                this.is_activated = 2;
            }
        }
        if (var2 != null && var2.func_77973_b() == Items.field_151172_bF && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            this.hat_color = 1;
            if (this.name_one == null) {
                this.name_one = par1EntityPlayer.func_110124_au().toString();
            }
            if (this.is_activated == 0) {
                this.is_activated = 1;
            }
            this.func_70903_f(true);
            this.func_152115_b(this.name_one);
            this.func_70908_e(true);
            this.func_70691_i(this.func_110138_aP() - this.func_110143_aJ());
            this.func_110163_bv();
            if (par1EntityPlayer.field_71075_bZ.field_75098_d) return true;
            --var2.field_77994_a;
            if (var2.field_77994_a > 0) return true;
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            return true;
        }
        if (var2 != null && var2.func_77973_b() == Items.field_151174_bG && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            this.hat_color = 3;
            if (this.name_one == null) {
                this.name_one = par1EntityPlayer.func_110124_au().toString();
            }
            if (this.is_activated == 0) {
                this.is_activated = 1;
            }
            this.func_70903_f(true);
            this.func_152115_b(this.name_one);
            this.func_70908_e(true);
            this.func_70691_i(this.func_110138_aP() - this.func_110143_aJ());
            this.func_110163_bv();
            if (par1EntityPlayer.field_71075_bZ.field_75098_d) return true;
            --var2.field_77994_a;
            if (var2.field_77994_a > 0) return true;
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            return true;
        }
        if (var2 != null && var2.func_77973_b() == OreSpawnMain.MyQuinoa && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            this.hat_color = 2;
            if (this.name_one == null) {
                this.name_one = par1EntityPlayer.func_110124_au().toString();
            }
            if (this.is_activated == 0) {
                this.is_activated = 1;
            }
            this.func_70903_f(true);
            this.func_152115_b(this.name_one);
            this.func_70908_e(true);
            this.func_70691_i(this.func_110138_aP() - this.func_110143_aJ());
            this.func_110163_bv();
            if (par1EntityPlayer.field_71075_bZ.field_75098_d) return true;
            --var2.field_77994_a;
            if (var2.field_77994_a > 0) return true;
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            return true;
        }
        if (var2 != null && this.is_activated == 2 && var2.func_77973_b() == OreSpawnMain.MyCornCob && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            Entity newent;
            String myname = "Ostrich";
            if (this instanceof Lizard) {
                myname = "Lizard";
            }
            if (this instanceof Chipmunk) {
                myname = "Chipmunk";
            }
            if (this instanceof VelocityRaptor) {
                myname = "Velocity Raptor";
            }
            if (!this.field_70170_p.field_72995_K && (newent = EntityCannonFodder.spawnCreature((World)this.field_70170_p, (String)myname, (double)(this.field_70165_t + (double)this.field_70170_p.field_73012_v.nextFloat()), (double)(this.field_70163_u + 0.01), (double)(this.field_70161_v + (double)this.field_70170_p.field_73012_v.nextFloat()))) != null) {
                EntityCannonFodder cf = (EntityCannonFodder)newent;
                cf.func_152115_b(this.func_152113_b());
                cf.func_70903_f(true);
                cf.setStuff(this.hat_color, this.is_activated, this.name_one, this.name_two);
            }
            this.func_70908_e(true);
            this.field_70170_p.func_72956_a((Entity)par1EntityPlayer, "random.explode", 0.75f, 2.0f);
            if (par1EntityPlayer.field_71075_bZ.field_75098_d) return true;
            --var2.field_77994_a;
            if (var2.field_77994_a > 0) return true;
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            return true;
        }
        if (this.is_activated != 2 || !(par1EntityPlayer.func_70068_e((Entity)this) < 16.0)) return false;
        if (this.func_70906_o()) {
            this.func_70904_g(false);
            this.func_70908_e(true);
            return true;
        } else {
            this.func_70904_g(true);
            this.func_70908_e(false);
            this.px = (int)this.field_70165_t;
            this.py = (int)this.field_70163_u;
            this.pz = (int)this.field_70161_v;
        }
        return true;
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }

    public void setStuff(int hc, int ia, String s1, String s2) {
        this.hat_color = hc;
        this.is_activated = ia;
        this.name_one = s1;
        this.name_two = s2;
        this.func_110163_bv();
    }

    public int getHatColor() {
        return this.hat_color;
    }

    public int get_is_activated() {
        return this.is_activated;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        if (this.name_one == null) {
            par1NBTTagCompound.func_74778_a("NameOne", "");
        } else {
            par1NBTTagCompound.func_74778_a("NameOne", this.name_one);
        }
        if (this.name_two == null) {
            par1NBTTagCompound.func_74778_a("NameTwo", "");
        } else {
            par1NBTTagCompound.func_74778_a("NameTwo", this.name_two);
        }
        par1NBTTagCompound.func_74768_a("IsActivated", this.is_activated);
        par1NBTTagCompound.func_74768_a("HatColor", this.hat_color);
        par1NBTTagCompound.func_74768_a("PatrolX", this.px);
        par1NBTTagCompound.func_74768_a("PatrolY", this.py);
        par1NBTTagCompound.func_74768_a("PatrolZ", this.pz);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.name_one = par1NBTTagCompound.func_74779_i("NameOne");
        if (this.name_one != null && this.name_one.equals("")) {
            this.name_one = null;
        }
        this.name_two = par1NBTTagCompound.func_74779_i("NameTwo");
        if (this.name_two != null && this.name_two.equals("")) {
            this.name_two = null;
        }
        this.is_activated = par1NBTTagCompound.func_74762_e("IsActivated");
        this.hat_color = par1NBTTagCompound.func_74762_e("HatColor");
        this.px = par1NBTTagCompound.func_74762_e("PatrolX");
        this.py = par1NBTTagCompound.func_74762_e("PatrolY");
        this.pz = par1NBTTagCompound.func_74762_e("PatrolZ");
        if (this.name_one != null) {
            this.func_70903_f(true);
            this.func_152115_b(this.name_one);
        }
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        double dz;
        double dy;
        double dx;
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return false;
        }
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        if (this.func_70906_o() && (dx = (double)this.px - par1EntityLiving.field_70165_t) * dx + (dy = (double)this.py - par1EntityLiving.field_70163_u) * dy + (dz = (double)this.pz - par1EntityLiving.field_70161_v) * dz > 144.0) {
            return false;
        }
        if (par1EntityLiving instanceof EntityMob) {
            return true;
        }
        if (par1EntityLiving instanceof EntityCannonFodder) {
            EntityCannonFodder cf = (EntityCannonFodder)par1EntityLiving;
            int i = cf.getHatColor();
            return i != 0 && i != this.hat_color;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            if (p.field_71075_bZ.field_75098_d) {
                return false;
            }
            if (this.name_one != null && this.name_one.equals(p.func_110124_au().toString())) {
                return false;
            }
            return this.name_two == null || !this.name_two.equals(p.func_110124_au().toString());
        }
        return false;
    }

    private EntityLivingBase findSomethingToAttack() {
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(10.0, 4.0, 10.0));
        Collections.sort(var5, this.LocalTargetSorter);
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (!this.isSuitableTarget(var4, false)) continue;
            return var4;
        }
        return null;
    }

    public int func_70658_aO() {
        if (this.is_activated == 2) {
            return 3;
        }
        return 0;
    }

    public void attackEntityAsFodder(Entity par1Entity, float f) {
        par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), f);
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        if (this.is_activated != 2) {
            return;
        }
        int pfreq = 5;
        int sfreq = 7;
        float dm = 4.0f;
        if (this instanceof Chipmunk) {
            dm = 3.0f;
            sfreq = 6;
        }
        if (this instanceof Lizard) {
            dm = 6.0f;
            sfreq = 8;
        }
        if (this instanceof VelocityRaptor) {
            sfreq = 6;
            pfreq = 4;
        }
        if (this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && this.field_70170_p.field_73012_v.nextInt(pfreq) == 1) {
            EntityLivingBase e = this.findSomethingToAttack();
            if (e != null) {
                this.func_70661_as().func_75497_a((Entity)e, 1.25);
                if (this.func_70068_e((Entity)e) < 9.0 && (this.field_70146_Z.nextInt(sfreq + 1) == 0 || this.field_70146_Z.nextInt(sfreq) == 1)) {
                    this.attackEntityAsFodder((Entity)e, dm);
                }
            } else if (this.func_70906_o()) {
                this.func_70661_as().func_75492_a((double)this.px, (double)this.py, (double)this.pz, (double)0.65f);
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(250) == 1) {
            this.func_70691_i(1.0f);
        }
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return null;
    }
}

