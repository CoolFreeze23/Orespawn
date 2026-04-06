/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.client.entity.EntityClientPlayerMP
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAILookIdle
 *  net.minecraft.entity.ai.EntityAIWatchClosest
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.monster.EntityCaveSpider
 *  net.minecraft.entity.monster.EntitySpider
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C03PacketPlayer$C05PacketPlayerLook
 *  net.minecraft.network.play.client.C0CPacketInput
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.MathHelper
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.GenericTargetSorter;
import danger.orespawn.MyUtils;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.RenderSpiderRobotInfo;
import danger.orespawn.SpiderDriver;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class SpiderRobot
extends EntityLiving {
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private int playing = 0;
    private GenericTargetSorter TargetSorter = null;
    private float moveSpeed = 0.35f;
    private RenderSpiderRobotInfo renderdata = new RenderSpiderRobotInfo();
    private int didonce = 0;
    private int rideTicker = 0;

    public SpiderRobot(World par1World) {
        super(par1World);
        this.func_70105_a(3.25f, 2.25f);
        this.field_70153_n = null;
        this.TargetSorter = new GenericTargetSorter((Entity)this);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIWatchClosest((EntityLiving)this, EntityPlayer.class, 12.0f));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAILookIdle((EntityLiving)this));
        this.field_70178_ae = true;
        this.field_70728_aV = OreSpawnMain.SpiderRobot_stats.health / 2;
    }

    public SpiderRobot(World par1World, double par2, double par4, double par6) {
        this(par1World);
        this.func_70107_b(par2, par4 + (double)this.field_70129_M, par6);
        this.field_70159_w = 0.0;
        this.field_70181_x = 0.0;
        this.field_70179_y = 0.0;
        this.field_70169_q = par2;
        this.field_70167_r = par4;
        this.field_70166_s = par6;
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)OreSpawnMain.SpiderRobot_stats.health);
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a((double)this.moveSpeed);
        this.func_110140_aT().func_111150_b(SharedMonsterAttributes.field_111264_e);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a((double)OreSpawnMain.SpiderRobot_stats.attack);
    }

    protected boolean func_70692_ba() {
        return false;
    }

    public int func_70658_aO() {
        return OreSpawnMain.SpiderRobot_stats.defense;
    }

    protected void func_70619_bc() {
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70153_n != null) {
            return;
        }
        super.func_70619_bc();
    }

    protected void func_70629_bd() {
        if (this.field_70153_n != null) {
            return;
        }
        super.func_70629_bd();
    }

    private void initLegData() {
        if (this.renderdata == null) {
            this.renderdata = new RenderSpiderRobotInfo();
        }
        for (int i = 0; i < 8; ++i) {
            this.renderdata.ycurrentangle[i] = 0.0f;
            this.renderdata.ywantedangle[i] = 0.0f;
            this.renderdata.ydisplayangle[i] = 0.0f;
            this.renderdata.yvelocity[i] = 0.0f;
            this.renderdata.ymid[i] = 0.0f;
            this.renderdata.yoff[i] = 0.0f;
            this.renderdata.yrange[i] = 0.0f;
            this.renderdata.udcurrentangle[i] = 0.0f;
            this.renderdata.udwantedangle[i] = 0.0f;
            this.renderdata.uddisplayangle[i] = 0.0f;
            this.renderdata.udvelocity[i] = 0.0f;
            this.renderdata.p1xangle[i] = 0.7853981633974483;
            this.renderdata.p2xangle[i] = 0.0;
            this.renderdata.p3xangle[i] = -0.7853981633974483;
            this.renderdata.pxvelocity[i] = 0.0f;
            this.renderdata.foot_xpos[i] = (float)this.field_70165_t;
            this.renderdata.foot_ypos[i] = (float)this.field_70163_u;
            this.renderdata.foot_zpos[i] = (float)this.field_70161_v;
            this.renderdata.realposx[i] = 0.0f;
            this.renderdata.realposy[i] = 0.0f;
            this.renderdata.realposz[i] = 0.0f;
            this.renderdata.legoff[i] = 0.0f;
            this.renderdata.footup[i] = 1;
            this.renderdata.uppoint[i] = 0.0f;
            this.renderdata.footingticker[i] = 0;
            this.renderdata.gpcounter = 0;
            if (i == 0) {
                this.renderdata.legoff[i] = 1.25f;
                this.renderdata.ymid[i] = -0.32f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 1;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 1) {
                this.renderdata.legoff[i] = 1.25f;
                this.renderdata.ymid[i] = 3.4615927f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 0;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 2) {
                this.renderdata.legoff[i] = 2.0f;
                this.renderdata.ymid[i] = -1.0f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 3;
                this.renderdata.yoff[i] = -0.1f;
            }
            if (i == 3) {
                this.renderdata.legoff[i] = 2.0f;
                this.renderdata.ymid[i] = 4.1415925f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 2;
                this.renderdata.yoff[i] = -0.1f;
            }
            if (i == 4) {
                this.renderdata.legoff[i] = 1.75f;
                this.renderdata.ymid[i] = 0.62831855f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 5;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 5) {
                this.renderdata.legoff[i] = 1.75f;
                this.renderdata.ymid[i] = 2.5132742f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 4;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 6) {
                this.renderdata.legoff[i] = 3.4f;
                this.renderdata.ymid[i] = 1.05f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 7;
                this.renderdata.yoff[i] = -0.1f;
            }
            if (i != 7) continue;
            this.renderdata.legoff[i] = 3.4f;
            this.renderdata.ymid[i] = 2.0915928f;
            this.renderdata.yrange[i] = -0.2617994f;
            this.renderdata.pairedwith[i] = 6;
            this.renderdata.yoff[i] = -0.1f;
        }
    }

    private float getNewVelocity(float v, float diff, float curval) {
        float tv = v;
        if ((tv *= 8.0f) < 1.0f) {
            tv = 1.0f;
        }
        if (tv > 4.0f) {
            tv = 4.0f;
        }
        if (diff > 0.0f) {
            if ((double)diff < Math.PI / 360 * (double)tv) {
                curval = 0.0f;
            } else {
                curval = (float)((double)curval + 0.004363323129985824 * (double)tv);
                if ((double)diff < 0.06981317007977318 * (double)tv) {
                    curval = (float)(Math.PI / 180 * (double)tv);
                }
                if ((double)diff < Math.PI / 90 * (double)tv) {
                    curval = (float)(Math.PI / 360 * (double)tv);
                }
                if ((double)curval > 0.06981317007977318 * (double)tv) {
                    curval = (float)(0.06981317007977318 * (double)tv);
                }
            }
        } else if ((double)diff > -Math.PI / 360 * (double)tv) {
            curval = 0.0f;
        } else {
            curval = (float)((double)curval - 0.004363323129985824 * (double)tv);
            if ((double)diff > -0.06981317007977318 * (double)tv) {
                curval = -((float)(Math.PI / 180 * (double)tv));
            }
            if ((double)diff > -Math.PI / 90 * (double)tv) {
                curval = -((float)(Math.PI / 360 * (double)tv));
            }
            if ((double)curval < -0.06981317007977318 * (double)tv) {
                curval = -((float)(0.06981317007977318 * (double)tv));
            }
        }
        return curval;
    }

    public void updateLegs() {
        if (!this.field_70170_p.field_72995_K) {
            return;
        }
        this.field_70177_z %= 360.0f;
        while (this.field_70177_z < 0.0f) {
            this.field_70177_z += 360.0f;
        }
        ++this.renderdata.gpcounter;
        if (this.didonce == 0) {
            this.didonce = 1;
            this.initLegData();
        }
        float d1 = (float)(this.field_70169_q - this.field_70165_t);
        float d2 = (float)(this.field_70167_r - this.field_70163_u);
        float d3 = (float)(this.field_70166_s - this.field_70161_v);
        float realv = (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
        int i = 0;
        for (i = 0; i < 8; ++i) {
            double rdv;
            int fcount = 0;
            int n = i;
            this.renderdata.footingticker[n] = this.renderdata.footingticker[n] + 1;
            this.renderdata.realposx[i] = (float)(this.field_70165_t - (double)this.renderdata.legoff[i] * Math.sin(Math.toRadians(MathHelper.func_76138_g((double)(this.field_70177_z + 90.0f))) + (double)this.renderdata.ymid[i]));
            this.renderdata.realposz[i] = (float)(this.field_70161_v + (double)this.renderdata.legoff[i] * Math.cos(Math.toRadians(MathHelper.func_76138_g((double)(this.field_70177_z + 90.0f))) + (double)this.renderdata.ymid[i]));
            this.renderdata.realposy[i] = (float)this.field_70163_u + this.renderdata.yoff[i];
            int it = this.renderdata.footingticker[i] + this.renderdata.footingticker[this.renderdata.pairedwith[i]];
            if (it > 50 && this.renderdata.footingticker[i] > this.renderdata.footingticker[this.renderdata.pairedwith[i]]) {
                this.renderdata.footingticker[i] = 0;
            }
            d1 = this.renderdata.realposx[i] - this.renderdata.foot_xpos[i];
            d2 = this.renderdata.realposy[i] - this.renderdata.foot_ypos[i];
            d3 = this.renderdata.realposz[i] - this.renderdata.foot_zpos[i];
            float dd = (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            dd *= 16.0f;
            float da = (float)(Math.abs((double)this.renderdata.ycurrentangle[i] - (Math.toRadians(MathHelper.func_76138_g((double)this.field_70177_z)) + (double)this.renderdata.ymid[i])) % (Math.PI * 2));
            if ((double)da > Math.PI) {
                da = (float)((double)da - Math.PI * 2);
            }
            if ((double)da < -Math.PI) {
                da = (float)((double)da + Math.PI * 2);
            }
            da = Math.abs(da);
            if (dd > 294.0f || dd < 32.0f || da > Math.abs(this.renderdata.yrange[i]) * 8.0f / 7.0f || (double)Math.abs(this.renderdata.udcurrentangle[i]) > 1.25 || this.renderdata.footingticker[i] == 0) {
                this.findNewFooting(i);
                d1 = this.renderdata.realposx[i] - this.renderdata.foot_xpos[i];
                d2 = this.renderdata.realposy[i] - this.renderdata.foot_ypos[i];
                d3 = this.renderdata.realposz[i] - this.renderdata.foot_zpos[i];
                dd = (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                dd *= 16.0f;
            }
            float c1 = (float)(99.0 * Math.cos(this.renderdata.p2xangle[i] - this.renderdata.p1xangle[i]));
            float c2 = 99.0f;
            float c3 = (float)(99.0 * Math.cos(this.renderdata.p2xangle[i] - this.renderdata.p3xangle[i]));
            float cc = c1 + c2 + c3;
            float diff = cc - dd;
            this.renderdata.pxvelocity[i] = this.getNewVelocity(realv, (float)((double)diff * Math.PI / 360.0), this.renderdata.pxvelocity[i]);
            if (this.renderdata.pxvelocity[i] == 0.0f || Math.abs(diff) < 8.0f) {
                ++fcount;
            }
            int n2 = i;
            this.renderdata.p1xangle[n2] = this.renderdata.p1xangle[n2] + (double)this.renderdata.pxvelocity[i];
            this.renderdata.p2xangle[i] = 0.0;
            this.renderdata.p3xangle[i] = -this.renderdata.p1xangle[i];
            dd = this.renderdata.uppoint[i] != 0.0f ? (float)Math.atan2(dd, (double)(this.renderdata.realposy[i] - this.renderdata.uppoint[i]) * 16.0) : (float)Math.atan2(dd, (double)(this.renderdata.realposy[i] - this.renderdata.foot_ypos[i]) * 16.0);
            this.renderdata.udwantedangle[i] = (float)((double)dd - 1.5707963267948966);
            while ((double)this.renderdata.udwantedangle[i] > Math.PI) {
                int n3 = i;
                this.renderdata.udwantedangle[n3] = (float)((double)this.renderdata.udwantedangle[n3] - Math.PI * 2);
            }
            while ((double)this.renderdata.udwantedangle[i] < -Math.PI) {
                int n4 = i;
                this.renderdata.udwantedangle[n4] = (float)((double)this.renderdata.udwantedangle[n4] + Math.PI * 2);
            }
            double rhm = this.renderdata.udwantedangle[i];
            double rhdir = this.renderdata.udcurrentangle[i];
            for (rdv = (rhm - rhdir) % (Math.PI * 2); rdv > Math.PI; rdv -= Math.PI * 2) {
            }
            while (rdv < -Math.PI) {
                rdv += Math.PI * 2;
            }
            diff = (float)rdv;
            this.renderdata.udvelocity[i] = this.getNewVelocity(realv * 2.0f, diff, this.renderdata.udvelocity[i]);
            if (this.renderdata.udvelocity[i] == 0.0f || (double)Math.abs(diff) < Math.PI / 90) {
                this.renderdata.uppoint[i] = 0.0f;
                ++fcount;
            }
            rhdir += (double)this.renderdata.udvelocity[i];
            while (rhdir > Math.PI) {
                rhdir -= Math.PI * 2;
            }
            while (rhdir < -Math.PI) {
                rhdir += Math.PI * 2;
            }
            this.renderdata.uddisplayangle[i] = dd = (this.renderdata.udcurrentangle[i] = (float)rhdir);
            d3 = this.renderdata.realposz[i] - this.renderdata.foot_zpos[i];
            d1 = this.renderdata.realposx[i] - this.renderdata.foot_xpos[i];
            dd = (float)Math.atan2(d3, d1);
            this.renderdata.ywantedangle[i] = dd;
            rhm = this.renderdata.ywantedangle[i];
            rdv = (rhm - (rhdir = (double)this.renderdata.ycurrentangle[i])) % (Math.PI * 2);
            if (rdv > Math.PI) {
                rdv -= Math.PI * 2;
            }
            if (rdv < -Math.PI) {
                rdv += Math.PI * 2;
            }
            diff = (float)rdv;
            this.renderdata.yvelocity[i] = this.getNewVelocity(realv, diff, this.renderdata.yvelocity[i]);
            if (this.renderdata.yvelocity[i] == 0.0f || (double)Math.abs(diff) < Math.PI / 90) {
                ++fcount;
            }
            int n5 = i;
            this.renderdata.ycurrentangle[n5] = this.renderdata.ycurrentangle[n5] + this.renderdata.yvelocity[i];
            while ((double)this.renderdata.ycurrentangle[i] > Math.PI) {
                int n6 = i;
                this.renderdata.ycurrentangle[n6] = (float)((double)this.renderdata.ycurrentangle[n6] - Math.PI * 2);
            }
            while ((double)this.renderdata.ycurrentangle[i] < -Math.PI) {
                int n7 = i;
                this.renderdata.ycurrentangle[n7] = (float)((double)this.renderdata.ycurrentangle[n7] + Math.PI * 2);
            }
            dd = (float)((double)this.renderdata.ycurrentangle[i] - Math.toRadians(MathHelper.func_76138_g((double)this.field_70177_z)) - 1.5707963267948966);
            while ((double)dd > Math.PI) {
                dd = (float)((double)dd - Math.PI * 2);
            }
            while ((double)dd < -Math.PI) {
                dd = (float)((double)dd + Math.PI * 2);
            }
            this.renderdata.ydisplayangle[i] = dd;
            if (fcount != 3) continue;
            this.renderdata.footup[i] = 0;
            Block bid = this.field_70170_p.func_147439_a((int)this.renderdata.foot_xpos[i], (int)this.renderdata.foot_ypos[i], (int)this.renderdata.foot_zpos[i]);
            if (bid == Blocks.field_150329_H && this.field_70153_n != null && this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                this.field_70170_p.func_147449_b((int)this.renderdata.foot_xpos[i], (int)this.renderdata.foot_ypos[i], (int)this.renderdata.foot_zpos[i], Blocks.field_150350_a);
            }
            if ((bid = this.field_70170_p.func_147439_a((int)this.renderdata.foot_xpos[i], (int)this.renderdata.foot_ypos[i] - 1, (int)this.renderdata.foot_zpos[i])) != Blocks.field_150349_c || this.field_70153_n == null || !this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) continue;
            this.field_70170_p.func_147449_b((int)this.renderdata.foot_xpos[i], (int)this.renderdata.foot_ypos[i] - 1, (int)this.renderdata.foot_zpos[i], Blocks.field_150346_d);
        }
    }

    private void findNewFooting(int i) {
        float dd;
        float d2;
        float fy;
        float fz;
        float fx;
        float f = 16.0f;
        boolean found = false;
        float range = 0.0f;
        double rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f);
        double pi = 3.1415926545;
        this.renderdata.footingticker[i] = 0;
        float d1 = (float)(this.field_70165_t - this.field_70169_q);
        float d3 = (float)(this.field_70161_v - this.field_70166_s);
        double rhm = Math.atan2(d3, d1);
        double velocity = Math.sqrt(d1 * d1 + d3 * d3);
        double rdv = Math.abs(rhm - rhdir) % (pi * 2.0);
        if (rdv > pi) {
            rdv -= pi * 2.0;
        }
        rdv = Math.abs(rdv);
        if (Math.abs(velocity) < 0.01) {
            rdv = 0.0;
        }
        range = this.renderdata.yrange[i];
        range *= 0.875f;
        if (Math.abs((this.field_70126_B - this.field_70177_z) % 360.0f) > 0.75f) {
            range = 0.0f;
        }
        if (i >= 4) {
            f = 10.0f;
        }
        if (rdv > 1.5) {
            range = -range;
            f = 10.0f;
            if (i >= 4) {
                f = 16.0f;
            }
        }
        float deffx = fx = (float)((double)this.renderdata.realposx[i] - (double)(f / 2.0f) * Math.sin(Math.toRadians(MathHelper.func_76138_g((double)(this.field_70177_z + 90.0f))) + (double)this.renderdata.ymid[i]));
        float deffz = fz = (float)((double)this.renderdata.realposz[i] + (double)(f / 2.0f) * Math.cos(Math.toRadians(MathHelper.func_76138_g((double)(this.field_70177_z + 90.0f))) + (double)this.renderdata.ymid[i]));
        float deffy = fy = this.renderdata.realposy[i] - 1.0f;
        float oldf = f;
        int span = 1;
        while (!found && f > 3.5f) {
            fx = (float)((double)this.renderdata.realposx[i] - (double)f * Math.sin(Math.toRadians(MathHelper.func_76138_g((double)(this.field_70177_z + 90.0f))) + (double)this.renderdata.ymid[i] - (double)range));
            fz = (float)((double)this.renderdata.realposz[i] + (double)f * Math.cos(Math.toRadians(MathHelper.func_76138_g((double)(this.field_70177_z + 90.0f))) + (double)this.renderdata.ymid[i] - (double)range));
            fy = this.renderdata.realposy[i];
            for (int j = 11; !found && j > -14; --j) {
                block2: for (int m = -span; !found && m <= span; ++m) {
                    for (int n = -span; !found && n <= span; ++n) {
                        Block blk = this.field_70170_p.func_147439_a((int)fx + m, (int)fy + j, (int)fz + n);
                        if (blk == Blocks.field_150350_a || !this.field_70170_p.func_147439_a((int)fx + m, (int)fy + j, (int)fz + n).func_149688_o().func_76220_a()) continue;
                        fy += (float)(j + 1);
                        fx += (float)m;
                        fz += (float)n;
                        found = true;
                        continue block2;
                    }
                }
            }
            if (found) {
                d1 = this.renderdata.realposx[i] - fx;
                d2 = this.renderdata.realposy[i] - fy;
                d3 = this.renderdata.realposz[i] - fz;
                dd = (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                if ((dd *= 16.0f) > 294.0f) {
                    found = false;
                }
            }
            if (!((f -= 1.0f) < 3.5f) || range == 0.0f) continue;
            range = 0.0f;
            span = 3;
            f = oldf;
        }
        if (!found) {
            fx = deffx;
            fy = deffy;
            fz = deffz;
        }
        float sfx = this.renderdata.foot_xpos[i];
        float sfy = this.renderdata.foot_ypos[i];
        float sfz = this.renderdata.foot_zpos[i];
        this.renderdata.foot_xpos[i] = fx;
        this.renderdata.foot_ypos[i] = fy;
        this.renderdata.foot_zpos[i] = fz;
        if (this.renderdata.footup[i] == 0) {
            this.renderdata.footup[i] = 1;
            d1 = sfx - fx;
            d2 = sfy - fy;
            d3 = sfz - fz;
            dd = (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            dd *= 16.0f;
            d1 = (sfy + fy) / 2.0f;
            if (dd > 3.0f) {
                d1 += 1.0f;
            }
            if (dd > 48.0f) {
                d1 += 1.5f;
            }
            if (dd > 100.0f) {
                d1 += 1.5f;
            }
            this.renderdata.uppoint[i] = d1;
        }
    }

    public boolean shouldRiderSit() {
        return false;
    }

    public int getTrackingRange() {
        return 128;
    }

    public int getUpdateFrequency() {
        return 10;
    }

    public boolean sendsVelocityUpdates() {
        return true;
    }

    protected boolean func_70041_e_() {
        return true;
    }

    protected void func_70088_a() {
        super.func_70088_a();
        this.func_110163_bv();
        this.initLegData();
        this.field_70180_af.func_75682_a(20, (Object)0);
    }

    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        return this.renderdata;
    }

    public boolean func_70104_M() {
        return false;
    }

    public double func_70042_X() {
        if (this.field_70153_n != null && this.field_70153_n instanceof SpiderDriver) {
            return 2.0;
        }
        return 2.625 + Math.cos((float)this.rideTicker * 0.19f) * 0.02;
    }

    public void func_70043_V() {
        if (this.field_70153_n != null) {
            float f = -3.0f;
            f = (float)((double)f + Math.cos((float)this.rideTicker * 0.33f) * 0.05);
            this.field_70153_n.func_70107_b(this.field_70165_t - (double)f * Math.sin(Math.toRadians(this.field_70177_z)), this.field_70163_u + this.func_70042_X() + this.field_70153_n.func_70033_W(), this.field_70161_v + (double)f * Math.cos(Math.toRadians(this.field_70177_z)));
        }
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        if (par1DamageSource.func_76355_l().equals("inWall")) {
            return false;
        }
        if (par1DamageSource.func_76355_l().equals("cactus")) {
            return false;
        }
        if (par1DamageSource.func_76355_l().equals("inFire")) {
            return false;
        }
        if (par1DamageSource.func_76355_l().equals("onFire")) {
            return false;
        }
        if (par1DamageSource.func_76355_l().equals("magic")) {
            return false;
        }
        if (par1DamageSource.func_76355_l().equals("starve")) {
            return false;
        }
        return super.func_70097_a(par1DamageSource, par2);
    }

    protected void func_70069_a(float par1) {
    }

    protected void func_70064_a(double par1, boolean par3) {
    }

    public boolean func_70067_L() {
        return !this.field_70128_L;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70056_a(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.boatPosRotationIncrements = this.field_70153_n != null ? par9 + 8 : par9 + 6;
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_70016_h(double par1, double par3, double par5) {
        if (this.field_70153_n == null) {
            super.func_70016_h(par1, par3, par5);
        }
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.func_70015_d(0);
        if (this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && !this.field_70170_p.field_72995_K && this.field_70153_n != null && this.field_70170_p.field_73012_v.nextInt(40) == 0) {
            this.feetFindSomethingToHit();
        }
        if (this.field_70170_p.field_73013_u != EnumDifficulty.PEACEFUL && !this.field_70170_p.field_72995_K && this.field_70153_n != null && this.field_70170_p.field_73012_v.nextInt(15) == 0) {
            EntityLivingBase e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.func_70068_e((Entity)e) < (double)((12.0f + e.field_70130_N / 2.0f) * (12.0f + e.field_70130_N / 2.0f))) {
                    this.setAttacking(1);
                    this.func_70652_k((Entity)e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        float f = 8.0f;
        float dx = (float)((double)f * Math.cos(Math.toRadians(this.field_70177_z - 90.0f)));
        float dz = (float)((double)f * Math.sin(Math.toRadians(this.field_70177_z - 90.0f)));
        if (this.field_70170_p.field_73012_v.nextInt(8) == 0) {
            this.field_70170_p.func_72869_a("flame", this.field_70165_t + (double)dx, this.field_70163_u + 2.0, this.field_70161_v + (double)dz, (double)(dx / f + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 20.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 10.0f), (double)(dz / f + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 20.0f));
        }
        if (this.field_70170_p.field_73012_v.nextInt(2) == 0) {
            this.field_70170_p.func_72869_a("smoke", this.field_70165_t + (double)dx, this.field_70163_u + 2.0, this.field_70161_v + (double)dz, (double)(dx / f + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 20.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 10.0f), (double)(dz / f + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 20.0f));
        }
        if (this.field_70170_p.field_73012_v.nextInt(10) == 0) {
            this.field_70170_p.func_72869_a("fireworksSpark", this.field_70165_t + (double)dx, this.field_70163_u + 2.0, this.field_70161_v + (double)dz, (double)(dx / f + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 20.0f), (double)((this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 5.0f), (double)(dz / f + (this.field_70170_p.field_73012_v.nextFloat() - this.field_70170_p.field_73012_v.nextFloat()) / 20.0f));
        }
    }

    public void func_70636_d() {
        Object list = null;
        double velocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
        double d6 = this.field_70146_Z.nextFloat() * 2.0f - 1.0f;
        double d7 = (double)(this.field_70146_Z.nextInt(2) * 2 - 1) * 0.7;
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.45;
        double gh = 1.55;
        int dist = 2;
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70153_n == null) {
            super.func_70636_d();
        }
        if (this.field_70181_x > (double)0.85f) {
            this.field_70181_x = 0.85f;
        }
        if (this.field_70181_x < (double)-0.85f) {
            this.field_70181_x = -0.85f;
        }
        if (this.field_70159_w < -1.25) {
            this.field_70159_w = -1.25;
        }
        if (this.field_70159_w > 1.25) {
            this.field_70159_w = 1.25;
        }
        if (this.field_70179_y < -1.25) {
            this.field_70179_y = -1.25;
        }
        if (this.field_70179_y > 1.25) {
            this.field_70179_y = 1.25;
        }
        this.field_70169_q = this.field_70165_t;
        this.field_70167_r = this.field_70163_u;
        this.field_70166_s = this.field_70161_v;
        this.rideTicker += this.field_70170_p.field_73012_v.nextInt(3);
        if (this.playing > 0) {
            --this.playing;
        }
        if (this.field_70153_n != null && this.playing == 0 && this.field_70170_p.field_73012_v.nextInt(80) == 1) {
            this.field_70170_p.func_72956_a((Entity)this, "orespawn:robotspider", 0.45f, 1.0f);
            this.playing = 125;
        }
        if (this.field_70170_p.field_72995_K) {
            if (this.field_70153_n == null) {
                Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)((float)this.field_70163_u - (float)gh + 1.0f), (int)this.field_70161_v);
                if (bid == Blocks.field_150350_a) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)((float)this.field_70163_u - (float)gh), (int)this.field_70161_v);
                }
                if (bid != Blocks.field_150350_a && bid != Blocks.field_150355_j && bid != Blocks.field_150358_i && bid != Blocks.field_150353_l && bid != Blocks.field_150356_k) {
                    this.field_70181_x += 0.12;
                    this.field_70163_u += 0.12;
                    this.boatY += 0.12;
                } else {
                    this.field_70181_x -= 0.002;
                }
            } else if (this.field_70153_n instanceof EntityPlayer) {
                EntityClientPlayerMP pp = (EntityClientPlayerMP)this.field_70153_n;
                pp.field_71174_a.func_147297_a((Packet)new C03PacketPlayer.C05PacketPlayerLook(pp.field_70177_z, pp.field_70125_A, pp.field_70122_E));
                pp.field_71174_a.func_147297_a((Packet)new C0CPacketInput(pp.field_70702_br, pp.field_70701_bs, pp.field_71158_b.field_78901_c, pp.field_71158_b.field_78899_d));
            }
            if (this.boatPosRotationIncrements > 0) {
                double d4 = this.field_70165_t + (this.boatX - this.field_70165_t) / (double)this.boatPosRotationIncrements;
                double d5 = this.field_70163_u + (this.boatY - this.field_70163_u) / (double)this.boatPosRotationIncrements;
                double d11 = this.field_70161_v + (this.boatZ - this.field_70161_v) / (double)this.boatPosRotationIncrements;
                this.func_70107_b(d4, d5, d11);
                this.field_70125_A = (float)((double)this.field_70125_A + (this.boatPitch - (double)this.field_70125_A) / (double)this.boatPosRotationIncrements);
                double d10 = MathHelper.func_76138_g((double)(this.boatYaw - (double)this.field_70177_z));
                if (this.field_70153_n != null) {
                    d10 = MathHelper.func_76138_g((double)((double)this.field_70153_n.field_70177_z - (double)this.field_70177_z));
                }
                this.field_70177_z = (float)((double)this.field_70177_z + d10 / (double)this.boatPosRotationIncrements);
                this.func_70101_b(this.field_70177_z, this.field_70125_A);
                --this.boatPosRotationIncrements;
            } else {
                double d4 = this.field_70165_t + this.field_70159_w;
                double d5 = this.field_70163_u + this.field_70181_x;
                double d11 = this.field_70161_v + this.field_70179_y;
                this.func_70107_b(d4, d5, d11);
                this.field_70159_w *= 0.99;
                this.field_70181_x *= 0.95;
                this.field_70179_y *= 0.99;
            }
            this.updateLegs();
        } else {
            Block bid;
            if (this.field_70153_n != null) {
                gh = 4.25;
                bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)((float)this.field_70163_u - (float)gh), (int)this.field_70161_v);
                if (bid != Blocks.field_150350_a && bid != Blocks.field_150355_j && bid != Blocks.field_150358_i && bid != Blocks.field_150353_l && bid != Blocks.field_150356_k) {
                    this.field_70181_x += 0.06;
                    this.field_70163_u += 0.03;
                } else {
                    this.field_70181_x -= 0.02;
                }
            } else {
                bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)((float)this.field_70163_u - (float)gh + 1.0f), (int)this.field_70161_v);
                if (bid == Blocks.field_150350_a) {
                    bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)((float)this.field_70163_u - (float)gh), (int)this.field_70161_v);
                }
                if (bid != Blocks.field_150350_a && bid != Blocks.field_150355_j && bid != Blocks.field_150358_i && bid != Blocks.field_150353_l && bid != Blocks.field_150356_k) {
                    this.field_70181_x += 0.15;
                    this.field_70163_u += 0.15;
                    this.boatY += 0.15;
                } else {
                    this.field_70181_x -= 0.002;
                }
            }
            if (this.field_70153_n != null && this.field_70153_n instanceof EntityPlayer) {
                EntityPlayer pp = (EntityPlayer)this.field_70153_n;
                obstruction_factor = 0.0;
                dist = 3;
                dist += (int)(velocity * 6.0);
                for (int k = 1; k < dist; ++k) {
                    for (int i = 1; i < dist * 3; ++i) {
                        for (int j = -90; j <= 90; j += 30) {
                            double dz;
                            double dx = (double)i * Math.cos(Math.toRadians(this.field_70177_z + 90.0f + (float)j));
                            bid = this.field_70170_p.func_147439_a((int)(this.field_70165_t + dx), (int)this.field_70163_u - k, (int)(this.field_70161_v + (dz = (double)i * Math.sin(Math.toRadians(this.field_70177_z + 90.0f + (float)j)))));
                            if (bid == Blocks.field_150350_a || bid == Blocks.field_150355_j || bid == Blocks.field_150358_i || bid == Blocks.field_150353_l || bid == Blocks.field_150356_k) continue;
                            obstruction_factor += 0.03;
                        }
                    }
                }
                this.field_70181_x += obstruction_factor * 0.05;
                this.field_70163_u += obstruction_factor * 0.05;
                double d4 = this.field_70153_n.field_70177_z;
                d4 %= 360.0;
                while (d4 < 0.0) {
                    d4 += 360.0;
                }
                double d5 = this.field_70177_z;
                d5 %= 360.0;
                while (d5 < 0.0) {
                    d5 += 360.0;
                }
                for (relative_g = (d4 - d5) % 180.0; relative_g < 0.0; relative_g += 180.0) {
                }
                if (relative_g > 90.0) {
                    relative_g -= 180.0;
                }
                if (velocity > 0.01) {
                    d4 = 1.85 - velocity;
                    if ((d4 = Math.abs(d4)) < 0.01) {
                        d4 = 0.01;
                    }
                    if (d4 > 0.9) {
                        d4 = 0.9;
                    }
                    this.field_70177_z = this.field_70153_n.field_70177_z + (float)(relative_g * d4);
                } else {
                    this.field_70177_z = this.field_70153_n.field_70177_z;
                }
                relative_g = Math.abs(relative_g) * velocity;
                if (relative_g > 50.0) {
                    relative_g = 0.0;
                }
                this.field_70125_A = 0.0f;
                this.func_70101_b(this.field_70177_z, this.field_70125_A);
                double newvelocity = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
                double rr = Math.atan2(this.field_70153_n.field_70179_y, this.field_70153_n.field_70159_w);
                double rhm = Math.atan2(this.field_70179_y, this.field_70159_w);
                double rhdir = Math.toRadians((this.field_70153_n.field_70177_z + 90.0f) % 360.0f);
                double rt = 0.0;
                double pi = 3.1415926545;
                double deltav = 0.0;
                float im = pp.field_70701_bs;
                double rdv = Math.abs(rhm - rhdir) % (pi * 2.0);
                if (rdv > pi) {
                    rdv -= pi * 2.0;
                }
                rdv = Math.abs(rdv);
                if (Math.abs(newvelocity) < 0.01) {
                    rdv = 0.0;
                }
                if (rdv > 1.5) {
                    newvelocity = -newvelocity;
                }
                if (Math.abs(im) > 0.001f) {
                    if (im > 0.0f) {
                        deltav = 0.05;
                    } else {
                        max_speed = 0.25;
                        deltav = -0.05;
                    }
                    newvelocity += deltav;
                    if (newvelocity >= 0.0) {
                        if (newvelocity > max_speed) {
                            newvelocity = max_speed;
                        }
                        this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                        this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                    } else {
                        if (newvelocity < -max_speed) {
                            newvelocity = -max_speed;
                        }
                        newvelocity = -newvelocity;
                        this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 270.0f)) * newvelocity;
                        this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 270.0f)) * newvelocity;
                    }
                } else if (newvelocity >= 0.0) {
                    this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                    this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 90.0f)) * newvelocity;
                } else {
                    this.field_70159_w = Math.cos(Math.toRadians(this.field_70177_z + 270.0f)) * (newvelocity * -1.0);
                    this.field_70179_y = Math.sin(Math.toRadians(this.field_70177_z + 270.0f)) * (newvelocity * -1.0);
                }
                this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
                this.field_70159_w *= 0.98;
                this.field_70181_x *= 0.98;
                this.field_70179_y *= 0.98;
            }
            this.func_70091_d(this.field_70159_w, this.field_70181_x, this.field_70179_y);
            this.field_70159_w *= 0.8;
            this.field_70181_x *= 0.98;
            this.field_70179_y *= 0.8;
            if (this.field_70153_n != null && this.field_70153_n.field_70128_L) {
                this.field_70153_n = null;
            }
        }
    }

    public void goThisWay(double mx, double mz) {
        this.field_70159_w = mx;
        this.field_70179_y = mz;
    }

    public boolean func_70650_aV() {
        return this.field_70153_n == null;
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
    }

    public float func_70053_R() {
        return 0.95f;
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null && var2.func_77973_b() == Items.field_151042_j && par1EntityPlayer.func_70068_e((Entity)this) < 25.0) {
            if (!this.field_70170_p.field_72995_K) {
                float f = this.func_110138_aP() - this.func_110143_aJ();
                if (f > 100.0f) {
                    f = 100.0f;
                }
                if (f > 0.0f) {
                    this.func_70691_i(f);
                }
            }
            if (!par1EntityPlayer.field_71075_bZ.field_75098_d) {
                --var2.field_77994_a;
                if (var2.field_77994_a <= 0) {
                    par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }
            }
            return true;
        }
        if (this.field_70153_n != null && this.field_70153_n instanceof EntityPlayer && this.field_70153_n != par1EntityPlayer) {
            return true;
        }
        if (!this.field_70170_p.field_72995_K && this.field_70153_n == null && par1EntityPlayer.func_70068_e((Entity)this) < 16.0) {
            par1EntityPlayer.func_70078_a((Entity)this);
            this.field_70170_p.func_72956_a((Entity)this, "orespawn:robotspidermount", 0.65f, 1.0f);
        }
        return true;
    }

    private void feetFindSomethingToHit() {
        if (OreSpawnMain.PlayNicely != 0) {
            return;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(20.0, 8.0, 20.0));
        Iterator var2 = var5.iterator();
        Entity var3 = null;
        EntityLivingBase var4 = null;
        while (var2.hasNext()) {
            var3 = (Entity)var2.next();
            var4 = (EntityLivingBase)var3;
            if (!this.feetisSuitableTarget(var4, false)) continue;
            this.feetattackEntityAsMob((Entity)var4);
        }
    }

    private boolean feetisSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderRobot) {
            return false;
        }
        if (par1EntityLiving instanceof EntitySpider) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderDriver) {
            return false;
        }
        if (par1EntityLiving instanceof EntityCaveSpider) {
            return false;
        }
        if (par1EntityLiving == this.field_70153_n) {
            return false;
        }
        float d1 = (float)(par1EntityLiving.field_70165_t - this.field_70165_t);
        float d2 = (float)(par1EntityLiving.field_70163_u - this.field_70163_u);
        float d3 = (float)(par1EntityLiving.field_70161_v - this.field_70161_v);
        float dd = (float)Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
        if (dd > 18.0f) {
            return false;
        }
        if (dd < 12.0f) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        return true;
    }

    public boolean feetattackEntityAsMob(Entity par1Entity) {
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
            double ks = 0.6;
            double inair = 0.1;
            float f3 = (float)Math.atan2(par1Entity.field_70161_v - this.field_70161_v, par1Entity.field_70165_t - this.field_70165_t);
            ret = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), (float)OreSpawnMain.SpiderRobot_stats.attack / 10.0f);
            if (par1Entity.field_70128_L || par1Entity instanceof EntityPlayer) {
                inair *= 2.0;
            }
            if (ret) {
                par1Entity.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
        }
        return ret;
    }

    private EntityLivingBase findSomethingToAttack() {
        if (OreSpawnMain.PlayNicely != 0) {
            return null;
        }
        List var5 = this.field_70170_p.func_72872_a(EntityLivingBase.class, this.field_70121_D.func_72314_b(20.0, 12.0, 20.0));
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

    private boolean isSuitableTarget(EntityLivingBase par1EntityLiving, boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.func_70089_S()) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderRobot) {
            return false;
        }
        if (par1EntityLiving instanceof EntitySpider) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderDriver) {
            return false;
        }
        if (par1EntityLiving instanceof EntityCaveSpider) {
            return false;
        }
        if (par1EntityLiving == this.field_70153_n) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.func_70635_at().func_75522_a((Entity)par1EntityLiving)) {
            return false;
        }
        double rr = Math.atan2(par1EntityLiving.field_70161_v - this.field_70161_v, par1EntityLiving.field_70165_t - this.field_70165_t);
        double rhdir = Math.toRadians((this.field_70177_z + 90.0f) % 360.0f);
        double pi = 3.1415926545;
        double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
        if (rdd > pi) {
            rdd -= pi * 2.0;
        }
        rdd = Math.abs(rdd);
        if (this.func_70068_e((Entity)par1EntityLiving) < 36.0) {
            return true;
        }
        if (rdd > 0.75) {
            return false;
        }
        if (par1EntityLiving instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer)par1EntityLiving;
            return !p.field_71075_bZ.field_75098_d;
        }
        return true;
    }

    public boolean func_70652_k(Entity par1Entity) {
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof EntityLivingBase) {
            double ks = 1.2;
            double inair = 0.15;
            float f3 = (float)Math.atan2(par1Entity.field_70161_v - this.field_70161_v, par1Entity.field_70165_t - this.field_70165_t);
            ret = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), (float)OreSpawnMain.SpiderRobot_stats.attack);
            if (par1Entity.field_70128_L || par1Entity instanceof EntityPlayer) {
                inair *= 2.0;
            }
            if (ret) {
                par1Entity.func_70024_g(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
        }
        return ret;
    }

    public int getAttacking() {
        return this.field_70180_af.func_75683_a(20);
    }

    public void setAttacking(int par1) {
        this.field_70180_af.func_75692_b(20, (Object)((byte)par1));
    }

    protected Item func_146068_u() {
        return null;
    }

    private ItemStack dropItemRand(Item index, int par1) {
        EntityItem var3 = null;
        ItemStack is = new ItemStack(index, par1, 0);
        var3 = new EntityItem(this.field_70170_p, this.field_70165_t + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), this.field_70163_u + 1.0, this.field_70161_v + (double)OreSpawnMain.OreSpawnRand.nextInt(2) - (double)OreSpawnMain.OreSpawnRand.nextInt(2), is);
        if (var3 != null) {
            this.field_70170_p.func_72838_d((Entity)var3);
        }
        return is;
    }

    protected void func_70628_a(boolean par1, int par2) {
        ItemStack is = null;
        int i = 14 + this.field_70170_p.field_73012_v.nextInt(14);
        block12: for (int var4 = 0; var4 < i; ++var4) {
            int var3 = this.field_70170_p.field_73012_v.nextInt(15);
            switch (var3) {
                case 0: {
                    is = this.dropItemRand(Items.field_151137_ax, 1);
                    continue block12;
                }
                case 1: {
                    is = this.dropItemRand(Items.field_151107_aW, 1);
                    continue block12;
                }
                case 2: {
                    is = this.dropItemRand(Items.field_151132_bS, 1);
                    continue block12;
                }
                case 3: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150451_bX), 1);
                    continue block12;
                }
                case 4: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150367_z), 1);
                    continue block12;
                }
                case 5: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150320_F), 1);
                    continue block12;
                }
                case 6: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150331_J), 1);
                    continue block12;
                }
                case 7: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150442_at), 1);
                    continue block12;
                }
                case 8: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150451_bX), 1);
                    continue block12;
                }
                case 9: {
                    is = this.dropItemRand(Item.func_150898_a((Block)Blocks.field_150445_bS), 1);
                    continue block12;
                }
            }
        }
    }
}

