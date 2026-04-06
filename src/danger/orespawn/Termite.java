/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.SharedMonsterAttributes
 *  net.minecraft.entity.ai.EntityAIAttackOnCollide
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.EntityAINearestAttackableTarget
 *  net.minecraft.entity.ai.EntityAIPanic
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.ItemStack
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.DamageSource
 *  net.minecraft.util.IChatComponent
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.Teleporter
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.EntityAnt;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.OreSpawnTeleporter;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;

public class Termite
extends EntityAnt {
    int attack_delay = 20;
    private int closest = 99999;
    private int tx = 0;
    private int ty = 0;
    private int tz = 0;

    public Termite(World par1World) {
        super(par1World);
        this.func_70105_a(0.2f, 0.2f);
        this.moveSpeed = 0.2f;
        this.field_70728_aV = 1;
        this.func_70661_as().func_75491_a(true);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIPanic((EntityCreature)this, (double)1.4f));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIAttackOnCollide((EntityCreature)this, EntityPlayer.class, 1.0, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 8, 1.0));
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, 6, true));
        }
    }

    @Override
    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(2.0);
    }

    @Override
    public int mygetMaxHealth() {
        return 5;
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (OreSpawnMain.OreSpawnRand.nextInt(15) != 0) {
            return false;
        }
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return false;
        }
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 1.0f);
        return var4;
    }

    @Override
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
            par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText("Empty your hand!"));
            return false;
        }
        if (par1EntityPlayer.field_71093_bK != OreSpawnMain.DimensionID5) {
            int i;
            for (i = 0; i < par1EntityPlayer.field_71071_by.field_70462_a.length; ++i) {
                if (par1EntityPlayer.field_71071_by.field_70462_a[i] == null) continue;
                par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText("Empty your inventory!"));
                return false;
            }
            for (i = 0; i < par1EntityPlayer.field_71071_by.field_70460_b.length; ++i) {
                if (par1EntityPlayer.field_71071_by.field_70460_b[i] == null) continue;
                par1EntityPlayer.func_146105_b((IChatComponent)new ChatComponentText("Take off your armor!"));
                return false;
            }
            MinecraftServer.func_71276_C().func_71203_ab().transferPlayerToDimension((EntityPlayerMP)par1EntityPlayer, OreSpawnMain.DimensionID5, (Teleporter)new OreSpawnTeleporter(MinecraftServer.func_71276_C().func_71218_a(OreSpawnMain.DimensionID5), OreSpawnMain.DimensionID5, this.field_70170_p));
        } else {
            MinecraftServer.func_71276_C().func_71203_ab().transferPlayerToDimension((EntityPlayerMP)par1EntityPlayer, 0, (Teleporter)new OreSpawnTeleporter(MinecraftServer.func_71276_C().func_71218_a(0), 0, this.field_70170_p));
        }
        return true;
    }

    @Override
    public void func_70071_h_() {
        super.func_70071_h_();
        if (this.field_70128_L) {
            return;
        }
        if (this.attack_delay > 0) {
            --this.attack_delay;
        }
        if (this.attack_delay > 0) {
            return;
        }
        this.attack_delay = 20;
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return;
        }
        EntityPlayer e = this.field_70170_p.func_72856_b((Entity)this, 1.5);
        if (e != null) {
            this.func_70652_k((Entity)e);
        }
    }

    public boolean isWood(Block bid) {
        if (bid == Blocks.field_150422_aJ || bid == Blocks.field_150396_be || bid == Blocks.field_150344_f || bid == Blocks.field_150376_bx) {
            return true;
        }
        if (bid == Blocks.field_150373_bw || bid == Blocks.field_150324_C || bid == Blocks.field_150462_ai) {
            return true;
        }
        if (bid == Blocks.field_150472_an || bid == Blocks.field_150342_X || bid == Blocks.field_150466_ao || bid == Blocks.field_150452_aw) {
            return true;
        }
        if (bid == Blocks.field_150487_bG || bid == Blocks.field_150476_ad || bid == Blocks.field_150481_bH || bid == Blocks.field_150485_bF) {
            return true;
        }
        return bid == OreSpawnMain.CrystalPlanksBlock;
    }

    private boolean scan_it(int x, int y, int z, int dx, int dy, int dz) {
        int d;
        Block bid;
        int j;
        int i;
        int found = 0;
        for (i = -dy; i <= dy; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_147439_a(x + dx, y + i, z + j);
                if (this.isWood(bid) && (d = dx * dx + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + dx;
                    this.ty = y + i;
                    this.tz = z + j;
                    ++found;
                }
                if (!this.isWood(bid = this.field_70170_p.func_147439_a(x - dx, y + i, z + j)) || (d = dx * dx + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x - dx;
                this.ty = y + i;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dz; j <= dz; ++j) {
                bid = this.field_70170_p.func_147439_a(x + i, y + dy, z + j);
                if (this.isWood(bid) && (d = dy * dy + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + dy;
                    this.tz = z + j;
                    ++found;
                }
                if (!this.isWood(bid = this.field_70170_p.func_147439_a(x + i, y - dy, z + j)) || (d = dy * dy + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y - dy;
                this.tz = z + j;
                ++found;
            }
        }
        for (i = -dx; i <= dx; ++i) {
            for (j = -dy; j <= dy; ++j) {
                bid = this.field_70170_p.func_147439_a(x + i, y + j, z + dz);
                if (this.isWood(bid) && (d = dz * dz + j * j + i * i) < this.closest) {
                    this.closest = d;
                    this.tx = x + i;
                    this.ty = y + j;
                    this.tz = z + dz;
                    ++found;
                }
                if (!this.isWood(bid = this.field_70170_p.func_147439_a(x + i, y + j, z - dz)) || (d = dz * dz + j * j + i * i) >= this.closest) continue;
                this.closest = d;
                this.tx = x + i;
                this.ty = y + j;
                this.tz = z - dz;
                ++found;
            }
        }
        return found != 0;
    }

    @Override
    public void func_70629_bd() {
        if (this.field_70128_L) {
            return;
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1) {
            this.func_70604_c(null);
        }
        if (this.field_70170_p.field_73012_v.nextInt(200) == 1 && OreSpawnMain.PlayNicely == 0) {
            int i;
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (i = 1; i < 8; ++i) {
                int j = i;
                if (j > 4) {
                    j = 4;
                }
                if (this.scan_it((int)this.field_70165_t, (int)this.field_70163_u + 1, (int)this.field_70161_v, i, j, i)) break;
                if (i < 5) continue;
                ++i;
            }
            i = 0;
            if (this.closest < 99999) {
                this.func_70661_as().func_75492_a((double)this.tx, (double)this.ty, (double)this.tz, 1.0);
                if (this.closest < 6) {
                    if (this.field_70170_p.field_73012_v.nextInt(3) != 0) {
                        if (this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                            this.field_70170_p.func_147465_d(this.tx, this.ty, this.tz, Blocks.field_150346_d, 0, 2);
                        }
                        if (this.findBuddies() < 10) {
                            Termite.spawnCreature(this.field_70170_p, "Termite", this.field_70165_t + (double)0.1f, this.field_70163_u + (double)0.1f, this.field_70161_v + (double)0.1f);
                        }
                    } else {
                        if (this.field_70170_p.func_82736_K().func_82766_b("mobGriefing")) {
                            this.field_70170_p.func_147465_d(this.tx, this.ty, this.tz, Blocks.field_150350_a, 0, 2);
                        }
                        if (this.findBuddies() < 10) {
                            Termite.spawnCreature(this.field_70170_p, "Termite", (float)this.tx + 0.1f, (float)this.ty + 0.1f, (float)this.tz + 0.1f);
                        }
                    }
                    this.func_70691_i(1.0f);
                }
            }
        }
        super.func_70629_bd();
    }

    private int findBuddies() {
        List var5 = this.field_70170_p.func_72872_a(Termite.class, this.field_70121_D.func_72314_b(3.0, 3.0, 3.0));
        return var5.size();
    }

    public static Entity spawnCreature(World par0World, String par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75620_a((String)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
        }
        return var8;
    }
}

