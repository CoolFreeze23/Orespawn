/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.EntityLunaMoth;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.Mothra;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.OreSpawnTeleporter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;

public class EntityButterfly
extends EntityAmbientCreature {
    private static final ResourceLocation texture1 = new ResourceLocation("orespawn", "butterfly.png");
    private static final ResourceLocation texture2 = new ResourceLocation("orespawn", "butterfly2.png");
    private static final ResourceLocation texture3 = new ResourceLocation("orespawn", "butterfly3.png");
    private static final ResourceLocation texture4 = new ResourceLocation("orespawn", "butterfly4.png");
    private static final ResourceLocation texture5 = new ResourceLocation("orespawn", "eyemoth.png");
    private static final ResourceLocation texture6 = new ResourceLocation("orespawn", "lunamoth.png");
    private static final ResourceLocation texture7 = new ResourceLocation("orespawn", "darkmoth.png");
    private static final ResourceLocation texture8 = new ResourceLocation("orespawn", "firemoth.png");
    private static final ResourceLocation texture9 = new ResourceLocation("orespawn", "vbutterfly1.png");
    public int butterfly_type = OreSpawnMain.OreSpawnRand.nextInt(4);
    private int attack_delay = 0;
    private GenericTargetSorter TargetSorter = null;
    private int force_sync = 25;
    private ChunkCoordinates currentFlightTarget = null;

    public EntityButterfly(World par1World) {
        super(par1World);
        this.func_70105_a(0.4f, 0.4f);
        this.func_70661_as().func_75491_a(true);
        this.TargetSorter = new GenericTargetSorter((Entity)this);
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.1f);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(0.0);
    }

    public ResourceLocation getTexture(EntityButterfly a) {
        if (a instanceof Mothra) {
            return texture5;
        }
        if (a instanceof EntityLunaMoth) {
            if (((EntityLunaMoth)a).moth_type == 1) {
                return texture5;
            }
            if (((EntityLunaMoth)a).moth_type == 2) {
                return texture7;
            }
            if (((EntityLunaMoth)a).moth_type == 3) {
                return texture8;
            }
            return texture6;
        }
        if (this.butterfly_type == 1) {
            if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
                return texture9;
            }
            return texture2;
        }
        if (this.butterfly_type == 2) {
            return texture3;
        }
        if (this.butterfly_type == 3) {
            return texture4;
        }
        return texture1;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.field_70180_af.func_75682_a(20, (Object)this.butterfly_type);
    }

    protected boolean func_70692_ba() {
        return !this.func_104002_bU();
    }

    protected float func_70599_aP() {
        return 0.0f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected String func_70639_aQ() {
        return null;
    }

    protected String func_70621_aR() {
        return null;
    }

    protected String func_70673_aS() {
        return null;
    }

    public boolean func_70104_M() {
        return true;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    protected void func_85033_bc() {
    }

    public int mygetMaxHealth() {
        return 2;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    protected void func_70619_bc() {
        int keep_trying = 25;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if (this.field_70146_Z.nextInt(100) == 0 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 4.0f) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t + this.field_70146_Z.nextInt(7) - this.field_70146_Z.nextInt(7), (int)this.field_70163_u + this.field_70146_Z.nextInt(6) - 2, (int)this.field_70161_v + this.field_70146_Z.nextInt(7) - this.field_70146_Z.nextInt(7));
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                --keep_trying;
            }
        } else if (this.field_70146_Z.nextInt(10) == 0 && this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4 && this.butterfly_type == 1 && this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL) {
            EntityLivingBase e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.func_71571_b((int)e.field_70165_t, (int)(e.field_70163_u + 1.0), (int)e.field_70161_v);
                if (this.func_70068_e((Entity)e) < 6.0) {
                    this.func_70652_k((Entity)e);
                }
            }
        }
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.5 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.5 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.5 - this.field_70159_w) * (double)0.1f;
        this.field_70181_x += (Math.signum(var3) * (double)0.7f - this.field_70181_x) * (double)0.1f;
        this.field_70179_y += (Math.signum(var5) * 0.5 - this.field_70179_y) * (double)0.1f;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.5f;
        this.field_70177_z += var8;
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (OreSpawnMain.OreSpawnRand.nextInt(2) != 0) {
            return false;
        }
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return false;
        }
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 1.0f);
        return var4;
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
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        return par1EntityLiving instanceof EntityHorse;
    }

    private EntityLivingBase findSomethingToAttack() {
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(8.0, 5.0, 8.0));
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

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= (double)0.6f;
        --this.force_sync;
        if (this.force_sync < 0) {
            this.force_sync = 25;
            if (this.field_70170_p.field_72995_K) {
                this.butterfly_type = this.field_70180_af.func_75679_c(20);
            } else {
                this.field_70180_af.func_75692_b(20, (Object)this.butterfly_type);
            }
        }
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

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        if (par1EntityPlayer == null) {
            return false;
        }
        if (!(par1EntityPlayer instanceof EntityPlayerMP)) {
            return false;
        }
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null) {
            return false;
        }
        if (par1EntityPlayer.field_71093_bK != OreSpawnMain.DimensionID6) {
            MinecraftServer.func_71276_C().func_71203_ab().transferPlayerToDimension((EntityPlayerMP)par1EntityPlayer, OreSpawnMain.DimensionID6, (Teleporter)new OreSpawnTeleporter(MinecraftServer.func_71276_C().func_71218_a(OreSpawnMain.DimensionID6), OreSpawnMain.DimensionID6, this.field_70170_p));
        } else {
            MinecraftServer.func_71276_C().func_71203_ab().transferPlayerToDimension((EntityPlayerMP)par1EntityPlayer, 0, (Teleporter)new OreSpawnTeleporter(MinecraftServer.func_71276_C().func_71218_a(0), 0, this.field_70170_p));
        }
        return true;
    }

    public boolean func_70601_bi() {
        Block bid;
        for (int k = -3; k < 3; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 0; i < 5; ++i) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    if (bid != Blocks.field_150474_ac) continue;
                    TileEntityMobSpawner tileentitymobspawner = null;
                    tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
                    String s = tileentitymobspawner.func_145881_a().func_98276_e();
                    if (s == null || !s.equals("Butterfly")) continue;
                    this.butterfly_type = 1;
                    return true;
                }
            }
        }
        bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        if (bid != Blocks.field_150350_a) {
            return false;
        }
        if (!this.field_70170_p.func_72935_r()) {
            return false;
        }
        if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
            return true;
        }
        return !(this.field_70163_u < 50.0);
    }

    public void initCreature() {
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("ButterflyType", this.butterfly_type);
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.butterfly_type = par1NBTTagCompound.func_74762_e("ButterflyType");
    }
}

