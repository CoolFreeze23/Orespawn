/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.Girlfriend;
import danger.orespawn.OreSpawnMain;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class MyEntityAIDance
extends EntityAIBase {
    private Girlfriend thePet;
    World theWorld;
    public int ticker = 0;
    public int dance_move = 0;
    public int is_dancing = 0;

    public MyEntityAIDance(Girlfriend par1EntityTameable) {
        this.thePet = par1EntityTameable;
        this.theWorld = par1EntityTameable.field_70170_p;
    }

    public boolean is_dance_block(Block bid) {
        return bid == Blocks.field_150340_R || bid == Blocks.field_150484_ah || bid == Blocks.field_150475_bE || bid == OreSpawnMain.MyBlockRubyBlock || bid == OreSpawnMain.MyBlockAmethystBlock || bid == OreSpawnMain.MyBlockTitaniumBlock || bid == OreSpawnMain.MyBlockUraniumBlock;
    }

    public boolean func_75250_a() {
        if (this.thePet.func_70906_o()) {
            return false;
        }
        long t = this.theWorld.func_72820_D();
        if ((t %= 24000L) < 14000L || t > 22000L) {
            return false;
        }
        int ic = 0;
        int iz = 0;
        int ix = 0;
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                Block bid = this.theWorld.func_147439_a((int)this.thePet.field_70165_t + i, (int)this.thePet.field_70163_u - 1, (int)this.thePet.field_70161_v + j);
                if (!this.is_dance_block(bid)) continue;
                ++ic;
                ix += i;
                iz += j;
            }
        }
        return ic != 0;
    }

    public boolean func_75253_b() {
        if (this.thePet.func_70906_o()) {
            return false;
        }
        long t = this.theWorld.func_72820_D();
        if ((t %= 24000L) < 14000L || t > 22000L) {
            return false;
        }
        int ic = 0;
        int iz = 0;
        int ix = 0;
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                Block bid = this.theWorld.func_147439_a((int)this.thePet.field_70165_t + i, (int)this.thePet.field_70163_u - 1, (int)this.thePet.field_70161_v + j);
                if (!this.is_dance_block(bid)) continue;
                ++ic;
                ix += i;
                iz += j;
            }
        }
        if (ic == 0) {
            return false;
        }
        ix /= ic;
        iz /= ic;
        if (ic < 40) {
            this.thePet.func_70661_as().func_75492_a((double)((int)this.thePet.field_70165_t + ix), (double)((int)this.thePet.field_70163_u), (double)((int)this.thePet.field_70161_v + iz), 1.0);
        } else if (this.theWorld.field_73012_v.nextInt(3) == 1) {
            this.thePet.func_70661_as().func_75492_a((double)((int)this.thePet.field_70165_t), (double)((int)this.thePet.field_70163_u), (double)((int)this.thePet.field_70161_v), 1.0);
        }
        this.is_dancing = 1;
        return true;
    }

    public void func_75249_e() {
        this.thePet.func_70095_a(false);
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 1;
        int ic = 0;
        int iz = 0;
        int ix = 0;
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                Block bid = this.theWorld.func_147439_a((int)this.thePet.field_70165_t + i, (int)this.thePet.field_70163_u - 1, (int)this.thePet.field_70161_v + j);
                if (!this.is_dance_block(bid)) continue;
                ++ic;
                ix += i;
                iz += j;
            }
        }
        if (ic > 0) {
            ix /= ic;
            iz /= ic;
            if (ic < 40) {
                this.thePet.func_70661_as().func_75492_a((double)((int)this.thePet.field_70165_t + ix), (double)((int)this.thePet.field_70163_u), (double)((int)this.thePet.field_70161_v + iz), 1.0);
            }
        }
    }

    public void func_75251_c() {
        this.thePet.func_70095_a(false);
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 0;
    }

    public void func_75246_d() {
        int cycle = 20;
        int halfc = cycle / 2;
        int mover = cycle * 8;
        int tempid = this.thePet.func_145782_y();
        AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)(this.thePet.field_70165_t - 4.0), (double)(this.thePet.field_70163_u - 3.0), (double)(this.thePet.field_70161_v - 4.0), (double)(this.thePet.field_70165_t + 4.0), (double)(this.thePet.field_70163_u + 3.0), (double)(this.thePet.field_70161_v + 4.0));
        List var5 = this.theWorld.func_72872_a(Girlfriend.class, bb);
        for (Girlfriend var3 : var5) {
            if (var3.func_145782_y() >= tempid) continue;
            if (var3.Dance.is_dancing == 1) {
                this.ticker = var3.Dance.ticker;
                this.dance_move = var3.Dance.dance_move;
            }
            tempid = var3.func_145782_y();
        }
        ++this.ticker;
        if (this.dance_move == 0) {
            this.dance_move = 1 + this.theWorld.field_73012_v.nextInt(10);
            this.thePet.field_70159_w = 0.0;
            this.thePet.field_70179_y = 0.0;
            this.ticker = 0;
            this.thePet.func_70095_a(false);
        }
        switch (this.dance_move) {
            case 1: {
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 0);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 2: {
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 1);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 3: {
                if (this.ticker % cycle < halfc) {
                    this.thePet.func_70095_a(false);
                } else {
                    this.thePet.func_70095_a(true);
                }
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 4: {
                if (this.ticker % halfc == 1) {
                    this.thePet.func_71038_i();
                    this.thePet.field_70181_x = 0.25;
                }
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 5: {
                if (this.ticker % halfc == 1) {
                    this.thePet.func_71038_i();
                }
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 0);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 6: {
                if (this.ticker % halfc == 1) {
                    this.thePet.func_71038_i();
                }
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 1);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 7: {
                if (this.ticker % cycle < halfc) {
                    this.thePet.func_70095_a(false);
                } else {
                    this.thePet.func_70095_a(true);
                }
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 0);
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 2);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 8: {
                if (this.ticker % cycle < halfc) {
                    this.thePet.func_70095_a(false);
                } else {
                    this.thePet.func_70095_a(true);
                }
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 1);
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 2);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 9: {
                if (this.ticker % cycle < halfc) {
                    this.thePet.func_70095_a(false);
                } else {
                    this.thePet.func_70095_a(true);
                }
                if (this.ticker % halfc == 1) {
                    this.thePet.func_71038_i();
                }
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 0);
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 3);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            case 10: {
                if (this.ticker % cycle < halfc) {
                    this.thePet.func_70095_a(false);
                    this.thePet.field_70181_x = 0.25;
                } else {
                    this.thePet.func_70095_a(true);
                }
                if (this.ticker % halfc == 1) {
                    this.thePet.func_71038_i();
                }
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 1);
                this.move_it((EntityTameable)this.thePet, this.ticker, cycle, 3);
                if (this.ticker <= mover) break;
                this.dance_move = 0;
                break;
            }
            default: {
                this.dance_move = 0;
            }
        }
    }

    private void move_it(EntityTameable et, int t, int cycle, int dir) {
        float dirx = 0.0f;
        float dirz = 0.0f;
        float dirYaw = 0.0f;
        float dirYawH = 0.0f;
        switch (dir) {
            case 0: {
                dirx = 0.02f;
                dirz = 0.0f;
                dirYaw = 0.0f;
                dirYawH = 0.0f;
                break;
            }
            case 1: {
                dirx = 0.0f;
                dirz = 0.02f;
                dirYaw = 0.0f;
                dirYawH = 0.0f;
                break;
            }
            case 2: {
                dirx = 0.0f;
                dirz = 0.0f;
                dirYaw = 10.0f;
                dirYawH = 0.0f;
                break;
            }
            case 3: {
                dirx = 0.0f;
                dirz = 0.0f;
                dirYaw = 0.0f;
                dirYawH = 10.0f;
                break;
            }
        }
        if ((t %= cycle) >= cycle / 2) {
            dirx = -dirx;
            dirz = -dirz;
            dirYaw = -dirYaw;
            dirYawH = -dirYawH;
        }
        if ((t %= cycle / 2) >= cycle / 4) {
            dirYaw = -dirYaw;
            dirYawH = -dirYawH;
        }
        et.field_70159_w += (double)dirx;
        et.field_70179_y += (double)dirz;
        et.field_70177_z += dirYaw;
        et.field_70759_as += dirYawH;
    }
}

