/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.Triffid;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class Island
extends EntityAnimal {
    private float dir = 0.0f;
    private float speed = 0.1f;
    private int radius = 5;
    private int depth = 3;
    private int timer = 73;
    private int just_spawned = 1;
    private int ticker = 0;
    private int once = 1;
    private double myX;
    private double myY;
    private double myZ;
    private int dirchange;

    public Island(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 0.5f);
        this.ticker = par1World.field_73012_v.nextInt(50);
        this.dirchange = this.field_70170_p.field_73012_v.nextInt(2500);
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.field_70179_y = 0.0;
        this.field_70181_x = 0.0;
        this.field_70159_w = 0.0;
        if (this.field_70170_p.field_72995_K) {
            return;
        }
        if (this.once != 0) {
            this.myX = this.field_70165_t;
            this.myY = this.field_70163_u;
            this.myZ = this.field_70161_v;
            this.once = 0;
        }
        if (this.just_spawned != 0) {
            this.dir = this.field_70170_p.field_73012_v.nextFloat() * (float)Math.PI;
            if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                this.dir *= -1.0f;
            }
            if (this.field_70170_p.field_73012_v.nextInt(40) != 1) {
                this.radius = 3 + this.field_70170_p.field_73012_v.nextInt(4);
                this.depth = 2 + this.field_70170_p.field_73012_v.nextInt(3);
                this.speed = this.field_70170_p.field_73012_v.nextFloat() / 50.0f * (float)OreSpawnMain.IslandSpeedFactor;
            } else {
                this.radius = 6 + this.field_70170_p.field_73012_v.nextInt(5);
                this.depth = 3 + this.field_70170_p.field_73012_v.nextInt(4);
                this.speed = this.field_70170_p.field_73012_v.nextFloat() / 200.0f * (float)OreSpawnMain.IslandSpeedFactor;
            }
            this.create_island();
            this.ticker = this.field_70170_p.field_73012_v.nextInt(50);
            this.dirchange = this.field_70170_p.field_73012_v.nextInt(10000);
        }
        ++this.ticker;
        if (this.ticker >= this.timer) {
            this.update_island();
            this.ticker = 0;
        }
        --this.dirchange;
        if (this.dirchange <= 0) {
            this.dirchange = this.field_70170_p.field_73012_v.nextInt(5000);
            this.dir = this.field_70170_p.field_73012_v.nextFloat() * (float)Math.PI;
            if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                this.dir *= -1.0f;
            }
        }
        this.just_spawned = 0;
    }

    public void func_70636_d() {
        if (this.field_70170_p.field_72995_K) {
            super.func_70636_d();
        }
    }

    protected void func_70629_bd() {
    }

    protected void func_70619_bc() {
    }

    protected void func_70069_a(float par1) {
    }

    protected boolean func_70692_ba() {
        return false;
    }

    public void func_70037_a(NBTTagCompound par1NBTTagCompound) {
        super.func_70037_a(par1NBTTagCompound);
        this.just_spawned = par1NBTTagCompound.func_74762_e("JustSpawned");
        this.depth = par1NBTTagCompound.func_74762_e("Idepth");
        this.radius = par1NBTTagCompound.func_74762_e("Iradius");
        this.speed = par1NBTTagCompound.func_74760_g("Ispeed");
        this.dir = par1NBTTagCompound.func_74760_g("Idir");
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("JustSpawned", this.just_spawned);
        par1NBTTagCompound.func_74768_a("Idepth", this.depth);
        par1NBTTagCompound.func_74768_a("Iradius", this.radius);
        par1NBTTagCompound.func_74776_a("Ispeed", this.speed);
        par1NBTTagCompound.func_74776_a("Idir", this.dir);
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return null;
    }

    private void create_island() {
        double deltadir = 0.10471975333333333;
        double deltamag = 0.35f;
        int ixlast = 0;
        int izlast = 0;
        int xoff = 0;
        int zoff = 0;
        for (int i = 0; i < this.depth; ++i) {
            izlast = 0;
            ixlast = 0;
            for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
                double tradius = this.radius;
                tradius /= (double)(i + 1);
                for (double h = 0.75; h < tradius; h += deltamag) {
                    int ix = (int)(this.field_70165_t + Math.cos(curdir + (double)this.dir) * h);
                    int iz = (int)(this.field_70161_v + Math.sin(curdir + (double)this.dir) * h);
                    if (ix == ixlast && iz == izlast) continue;
                    ixlast = ix;
                    izlast = iz;
                    if (i == 0) {
                        Block bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u - i + 1, iz);
                        if (bid == Blocks.field_150350_a) {
                            if (this.field_70170_p.field_73012_v.nextInt(5000) == 1) {
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150353_l);
                                continue;
                            }
                            this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, (Block)Blocks.field_150391_bh);
                            if (this.field_70170_p.field_73012_v.nextInt(20) != 1 || this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u - i + 2, iz) != Blocks.field_150350_a) continue;
                            if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u - i + 2, iz, (Block)Blocks.field_150338_P);
                                continue;
                            }
                            this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u - i + 2, iz, (Block)Blocks.field_150337_Q);
                            continue;
                        }
                        if (bid != Blocks.field_150357_h) continue;
                        this.func_70106_y();
                        return;
                    }
                    if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
                        this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150482_ag);
                        continue;
                    }
                    this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150377_bs);
                }
            }
        }
        if (this.field_70165_t < 0.0) {
            xoff = -1;
        }
        if (this.field_70161_v < 0.0) {
            zoff = -1;
        }
        this.field_70170_p.func_147449_b((int)this.field_70165_t + xoff, (int)this.field_70163_u, (int)this.field_70161_v + zoff, Blocks.field_150350_a);
        this.FastSetBlock((int)this.field_70165_t + xoff, (int)this.field_70163_u, (int)this.field_70161_v + zoff, Blocks.field_150350_a);
    }

    private void update_island() {
        AxisAlignedBB bb;
        List var5;
        Iterator var2;
        double deltadir = 0.10471975333333333;
        double deltamag = 0.35f;
        double pi2 = 1.57079632675;
        int ixlast = 0;
        int izlast = 0;
        int xoff = 0;
        int zoff = 0;
        this.myX += (double)this.speed * Math.cos(this.dir);
        this.myZ += (double)this.speed * Math.sin(this.dir);
        int mx = (int)this.myX;
        int mz = (int)this.myZ;
        int px = (int)this.field_70165_t;
        int pz = (int)this.field_70161_v;
        if (mx != px || mz != pz) {
            Block bid;
            int iz;
            int ix;
            double h;
            double tradius;
            double curdir;
            int i;
            for (i = 0; i < this.depth; ++i) {
                izlast = 0;
                ixlast = 0;
                for (curdir = -3.3; curdir < 3.3; curdir += deltadir / 2.0) {
                    tradius = this.radius;
                    tradius /= (double)(i + 1);
                    for (h = 0.75; h < tradius; h += deltamag) {
                    }
                    if ((h -= deltamag) < 0.75) {
                        h = 0.75;
                    }
                    while (h < tradius + deltamag) {
                        ix = (int)(this.field_70165_t + Math.cos(curdir + (double)this.dir) * h);
                        iz = (int)(this.field_70161_v + Math.sin(curdir + (double)this.dir) * h);
                        if (ix != ixlast || iz != izlast) {
                            ixlast = ix;
                            izlast = iz;
                            if (i == 0 && ((bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + 1 + 1, iz)) == Blocks.field_150337_Q || bid == Blocks.field_150338_P)) {
                                this.FastSetBlock(ix, (int)this.field_70163_u + 1 + 1, iz, Blocks.field_150350_a);
                            }
                            this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150350_a);
                        }
                        h += deltamag / 2.0;
                    }
                }
            }
            if (this.field_70165_t < 0.0) {
                xoff = -1;
            }
            if (this.field_70161_v < 0.0) {
                zoff = -1;
            }
            this.field_70170_p.func_147449_b((int)this.field_70165_t + xoff, (int)this.field_70163_u, (int)this.field_70161_v + zoff, Blocks.field_150377_bs);
            this.field_70165_t = (int)this.myX;
            this.field_70165_t = this.myX < 0.0 ? (this.field_70165_t -= 0.5) : (this.field_70165_t += 0.5);
            this.field_70161_v = (int)this.myZ;
            this.field_70161_v = this.myZ < 0.0 ? (this.field_70161_v -= 0.5) : (this.field_70161_v += 0.5);
            for (i = 0; i < this.depth; ++i) {
                izlast = 0;
                ixlast = 0;
                for (curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
                    tradius = this.radius;
                    tradius /= (double)(i + 1);
                    for (h = 0.75; h < tradius; h += deltamag) {
                    }
                    if ((h -= deltamag * 3.0) < 0.75) {
                        h = 0.75;
                    }
                    while (h < tradius) {
                        ix = (int)(this.field_70165_t + Math.cos(curdir + (double)this.dir) * h);
                        iz = (int)(this.field_70161_v + Math.sin(curdir + (double)this.dir) * h);
                        if (ix != ixlast || iz != izlast) {
                            ixlast = ix;
                            izlast = iz;
                            if (i == 0) {
                                bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u - i + 1, iz);
                                if (bid == Blocks.field_150350_a) {
                                    if (this.field_70170_p.field_73012_v.nextInt(5000) == 1) {
                                        this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150353_l);
                                    } else {
                                        this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, (Block)Blocks.field_150391_bh);
                                        if (this.field_70170_p.field_73012_v.nextInt(20) == 1 && this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u - i + 2, iz) == Blocks.field_150350_a) {
                                            if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u - i + 2, iz, (Block)Blocks.field_150338_P);
                                            } else {
                                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u - i + 2, iz, (Block)Blocks.field_150337_Q);
                                            }
                                        }
                                    }
                                } else if (bid == Blocks.field_150357_h) {
                                    this.func_70106_y();
                                    return;
                                }
                            } else {
                                bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u - i + 1, iz);
                                if (bid == Blocks.field_150348_b) {
                                    if (!this.field_70170_p.field_72995_K) {
                                        this.field_70170_p.func_72876_a((Entity)this, (double)ix, this.field_70163_u - (double)i + 1.0, (double)iz, 5.0f, true);
                                    }
                                } else if (this.field_70170_p.field_73012_v.nextInt(10) == 1) {
                                    this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150482_ag);
                                } else {
                                    this.FastSetBlock(ix, (int)this.field_70163_u - i + 1, iz, Blocks.field_150377_bs);
                                }
                            }
                        }
                        h += deltamag;
                    }
                }
            }
            xoff = 0;
            if (this.field_70165_t < 0.0) {
                xoff = -1;
            }
            zoff = 0;
            if (this.field_70161_v < 0.0) {
                zoff = -1;
            }
            this.field_70170_p.func_147449_b((int)this.field_70165_t + xoff, (int)this.field_70163_u, (int)this.field_70161_v + zoff, Blocks.field_150350_a);
            this.FastSetBlock((int)this.field_70165_t + xoff, (int)this.field_70163_u, (int)this.field_70161_v + zoff, Blocks.field_150350_a);
        }
        if (this.field_70170_p.field_73012_v.nextInt(2 + 2000 / this.timer) == 1 && !(var2 = (var5 = this.field_70170_p.func_72872_a(Triffid.class, bb = AxisAlignedBB.func_72330_a((double)(this.field_70165_t - 10.0), (double)(this.field_70163_u - 5.0), (double)(this.field_70161_v - 10.0), (double)(this.field_70165_t + 10.0), (double)(this.field_70163_u + 5.0), (double)(this.field_70161_v + 10.0)))).iterator()).hasNext()) {
            EntityCreature newent = (EntityCreature)Island.spawnCreature(this.field_70170_p, "Triffid", this.field_70165_t, this.field_70163_u + 2.01, this.field_70161_v);
        }
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

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)OreSpawnMain.MyIslandBlock);
    }

    public void FastSetBlock(int ix, int iy, int iz, Block id) {
        OreSpawnMain.setBlockFast(this.field_70170_p, ix, iy, iz, id, 0, 3);
    }
}

