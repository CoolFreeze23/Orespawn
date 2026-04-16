/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.GenericTargetSorter;
import danger.orespawn.OreSpawnMain;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class Fairy
extends EntityAmbientCreature {
    private static final ResourceLocation texture0 = new ResourceLocation("orespawn", "fairytexture.png");
    private static final ResourceLocation texture1 = new ResourceLocation("orespawn", "fairytexture2.png");
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn", "fairytexture3.png");
    private static final ResourceLocation texture3 = new ResourceLocation("orespawn", "fairytexture4.png");
    private static final ResourceLocation texture4 = new ResourceLocation("orespawn", "fairytexture5.png");
    private static final ResourceLocation texture5 = new ResourceLocation("orespawn", "fairytexture6.png");
    private static final ResourceLocation texture6 = new ResourceLocation("orespawn", "fairytexture7.png");
    private static final ResourceLocation texture7 = new ResourceLocation("orespawn", "fairytexture8.png");
    private static final ResourceLocation texture8 = new ResourceLocation("orespawn", "fairytexture9.png");
    int my_blink = 20 + this.field_70146_Z.nextInt(20);
    int blinker = 0;
    int myspace = 0;
    public int fairy_type = 0;
    private int force_sync = 10;
    private ChunkCoordinates currentFlightTarget = null;
    private String myowner = null;
    private GenericTargetSorter TargetSorter = null;

    public Fairy(World par1World) {
        super(par1World);
        this.func_70105_a(0.4f, 0.8f);
        if (par1World != null) {
            this.fairy_type = par1World.field_73012_v.nextInt(9);
        }
        this.func_70661_as().func_75491_a(true);
        this.field_70155_l = 3.0;
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityLiving.class, 8.0f));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.TargetSorter = new GenericTargetSorter((Entity)this);
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(3.0);
    }

    public ResourceLocation getTexture(Fairy a) {
        if (this.fairy_type == 8) {
            return texture8;
        }
        if (this.fairy_type == 7) {
            return texture7;
        }
        if (this.fairy_type == 6) {
            return texture6;
        }
        if (this.fairy_type == 5) {
            return texture5;
        }
        if (this.fairy_type == 4) {
            return texture4;
        }
        if (this.fairy_type == 3) {
            return texture3;
        }
        if (this.fairy_type == 2) {
            return texture2;
        }
        if (this.fairy_type == 1) {
            return texture1;
        }
        return texture0;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)this.fairy_type);
    }

    public void setOwner(EntityLivingBase e) {
        String s;
        EntityPlayer p = null;
        if (e != null && e instanceof EntityPlayer && (s = (p = (EntityPlayer)e).getDisplayName()) != null) {
            this.myowner = s;
        }
    }

    public float getBlink() {
        if (this.blinker < this.my_blink / 2) {
            return 240.0f;
        }
        return 0.0f;
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return false;
        }
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 2.0f);
        return var4;
    }

    public int func_70658_aO() {
        return 4;
    }

    protected float func_70599_aP() {
        return 0.25f;
    }

    protected float func_70647_i() {
        return 1.7f;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return "orespawn:rat_hit";
    }

    protected String func_70673_aS() {
        return "orespawn:big_splat";
    }

    public boolean func_70104_M() {
        return true;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    protected void func_85033_bc() {
    }

    public int mygetMaxHealth() {
        return 40;
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)OreSpawnMain.CrystalTorch);
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= 0.600000023841;
        ++this.blinker;
        if (this.blinker > this.my_blink) {
            this.blinker = 0;
        }
        --this.force_sync;
        if (this.force_sync < 0) {
            this.force_sync = 10;
            if (this.field_70170_p.field_72995_K) {
                this.fairy_type = this.field_70180_af.func_75679_c(20);
            } else {
                this.field_70180_af.func_75692_b(20, (Object)this.fairy_type);
            }
        }
        long t = this.field_70170_p.func_72820_D();
        if ((t %= 24000L) < 12000L) {
            return;
        }
        if (this.field_70170_p.field_72995_K && this.field_70170_p.field_73012_v.nextInt(5) == 0 && this.getBlink() > 1.0f) {
            this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t, this.field_70163_u - (double)0.15f, this.field_70161_v, (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 8.0f), (double)(-this.field_70170_p.field_73012_v.nextFloat() / 8.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 8.0f));
        }
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        if (this.myowner == null) {
            this.myowner = "null";
        }
        par1NBTTagCompound.func_74778_a("MyOwner", this.myowner);
        par1NBTTagCompound.func_74768_a("FairyType", this.fairy_type);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.myowner = par1NBTTagCompound.func_74779_i("MyOwner");
        if (this.myowner != null && this.myowner.equals("null")) {
            this.myowner = null;
        }
        this.fairy_type = par1NBTTagCompound.func_74762_e("fairyType");
    }

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
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
        return par1EntityLiving instanceof EntityMob;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(8.0, 8.0, 8.0));
        Collections.sort(var5, this.TargetSorter);
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

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.25), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    protected void func_70619_bc() {
        EntityPlayer p;
        int keep_trying = 25;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 0 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 2.5f) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                int zdir = this.field_70170_p.field_73012_v.nextInt(8);
                int xdir = this.field_70170_p.field_73012_v.nextInt(8);
                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t + xdir, (int)this.field_70163_u + this.field_70170_p.field_73012_v.nextInt(5) - 2, (int)this.field_70161_v + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                if (bid == Blocks.field_150350_a && !this.canSeeTarget((double)this.currentFlightTarget.field_71574_a, (double)this.currentFlightTarget.field_71572_b, (double)this.currentFlightTarget.field_71573_c)) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
        } else if (this.field_70170_p.field_73012_v.nextInt(12) == 0 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL) {
            EntityLivingBase e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + 1.0), (int)e.field_70161_v);
                if (this.func_70068_e((Entity)e) < 6.0) {
                    this.func_70652_k((Entity)e);
                }
            }
        } else if (this.myowner != null && (p = this.field_70170_p.func_72924_a(this.myowner)) != null) {
            if (this.func_70068_e((Entity)p) > 64.0) {
                this.currentFlightTarget.func_71571_b((int)p.field_70165_t + this.field_70170_p.field_73012_v.nextInt(3) - this.field_70170_p.field_73012_v.nextInt(3), (int)(p.field_70163_u + 1.0), (int)p.field_70161_v + this.field_70170_p.field_73012_v.nextInt(3) - this.field_70170_p.field_73012_v.nextInt(3));
            }
            if (this.func_70068_e((Entity)p) > 256.0) {
                this.func_70107_b(p.field_70165_t + (double)this.field_70170_p.field_73012_v.nextFloat() - (double)this.field_70170_p.field_73012_v.nextFloat(), p.field_70163_u, p.field_70161_v + (double)this.field_70170_p.field_73012_v.nextFloat() - (double)this.field_70170_p.field_73012_v.nextFloat());
            }
        }
        if (this.field_70170_p.field_73012_v.nextInt(250) == 1) {
            this.func_70691_i(1.0f);
        }
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.2 - this.field_70159_w) * 0.1;
        this.field_70181_x += (Math.signum(var3) * (double)0.7f - this.field_70181_x) * 0.1;
        this.field_70179_y += (Math.signum(var5) * 0.2 - this.field_70179_y) * 0.1;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.2f;
        this.field_70177_z += var8 / 4.0f;
    }

    protected boolean func_70041_e_() {
        return false;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_145773_az() {
        return true;
    }

    public boolean func_70601_bi() {
        int sc = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int j = -1; j <= 1; ++j) {
                Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u, (int)this.field_70161_v + k);
                if (bid != Blocks.field_150350_a) continue;
                ++sc;
            }
        }
        if (sc < 6) {
            return false;
        }
        return !(this.field_70163_u < 50.0);
    }

    public void initCreature() {
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        return this.myowner == null;
    }
}

