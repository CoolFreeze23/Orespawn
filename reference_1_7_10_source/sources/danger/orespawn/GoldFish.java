/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class GoldFish
extends EntityAnimal {
    private ChunkCoordinates currentFlightTarget = null;

    public GoldFish(World par1World) {
        super(par1World);
        this.func_70105_a(0.75f, 0.5f);
        this.field_70728_aV = 5;
        this.field_70178_ae = false;
        this.field_70174_ab = 5;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)0.22f);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0);
    }

    protected boolean func_70692_ba() {
        if (this.func_104002_bU()) {
            return false;
        }
        return !this.field_70170_p.func_72935_r();
    }

    protected float func_70599_aP() {
        return 0.45f;
    }

    protected float func_70647_i() {
        return 1.0f;
    }

    protected String func_70639_aQ() {
        return "splash";
    }

    protected String func_70621_aR() {
        return "splash";
    }

    protected String func_70673_aS() {
        return "orespawn:little_splat";
    }

    public boolean func_70104_M() {
        return true;
    }

    protected void func_82167_n(Entity par1Entity) {
    }

    public int mygetMaxHealth() {
        return 6;
    }

    protected boolean func_70650_aV() {
        return true;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70181_x *= 0.6;
    }

    public boolean canSeeTarget(double pX, double pY, double pZ) {
        return this.field_70170_p.func_72901_a(Vec3.func_72443_a((double)this.field_70165_t, (double)(this.field_70163_u + 0.75), (double)this.field_70161_v), Vec3.func_72443_a((double)pX, (double)pY, (double)pZ), false) == null;
    }

    protected void func_70619_bc() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        int updown = 0;
        if (this.field_70128_L) {
            return;
        }
        super.func_70619_bc();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new ChunkCoordinates((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
        }
        if ((int)this.field_70163_u < 120) {
            updown = 2;
        }
        if ((int)this.field_70163_u > 140) {
            updown = -2;
        }
        if (this.field_70146_Z.nextInt(300) == 0 || this.currentFlightTarget.func_71569_e((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v) < 2.1f) {
            Block bid = Blocks.field_150348_b;
            while (bid != Blocks.field_150350_a && keep_trying != 0) {
                zdir = this.field_70146_Z.nextInt(5) + 5;
                xdir = this.field_70146_Z.nextInt(5) + 5;
                if (this.field_70146_Z.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.field_70146_Z.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.func_71571_b((int)this.field_70165_t + xdir, (int)this.field_70163_u + this.field_70146_Z.nextInt(11) - 5 + updown, (int)this.field_70161_v + zdir);
                bid = this.field_70170_p.func_147439_a(this.currentFlightTarget.field_71574_a, this.currentFlightTarget.field_71572_b, this.currentFlightTarget.field_71573_c);
                if (bid == Blocks.field_150350_a && !this.canSeeTarget((double)this.currentFlightTarget.field_71574_a, (double)this.currentFlightTarget.field_71572_b, (double)this.currentFlightTarget.field_71573_c)) {
                    bid = Blocks.field_150348_b;
                }
                --keep_trying;
            }
        }
        double var1 = (double)this.currentFlightTarget.field_71574_a + 0.4 - this.field_70165_t;
        double var3 = (double)this.currentFlightTarget.field_71572_b + 0.1 - this.field_70163_u;
        double var5 = (double)this.currentFlightTarget.field_71573_c + 0.4 - this.field_70161_v;
        this.field_70159_w += (Math.signum(var1) * 0.4 - this.field_70159_w) * 0.3;
        this.field_70181_x += (Math.signum(var3) * 0.7 - this.field_70181_x) * 0.2;
        this.field_70179_y += (Math.signum(var5) * 0.4 - this.field_70179_y) * 0.3;
        float var7 = (float)(Math.atan2(this.field_70179_y, this.field_70159_w) * 180.0 / Math.PI) - 90.0f;
        float var8 = MathHelper.func_76142_g((float)(var7 - this.field_70177_z));
        this.field_70701_bs = 0.75f;
        this.field_70177_z += var8 / 6.0f;
    }

    protected boolean func_70041_e_() {
        return true;
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_145773_az() {
        return false;
    }

    public boolean func_70601_bi() {
        return true;
    }

    protected Item func_146068_u() {
        int i = this.field_70170_p.field_73012_v.nextInt(3);
        if (i == 0) {
            return Item.func_150898_a((Block)Blocks.field_150340_R);
        }
        if (i == 1) {
            return OreSpawnMain.UraniumNugget;
        }
        if (i == 2) {
            return OreSpawnMain.TitaniumNugget;
        }
        return null;
    }

    public EntityAgeable func_90011_a(EntityAgeable var1) {
        return null;
    }

    public boolean func_70648_aU() {
        return true;
    }
}

