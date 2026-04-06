/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityAgeable
 *  net.minecraft.entity.passive.EntityAnimal
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.nbt.NBTTagCompound
 *  net.minecraft.util.DamageSource
 *  net.minecraft.world.World
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class IslandToo
extends EntityAnimal {
    private int dir = 0;
    private float speed = 0.1f;
    private int width = 5;
    private int depth = 3;
    private int length = 10;
    private int timer = 42;
    private int just_spawned = 1;
    private int ticker = 0;
    private int once = 1;
    private double myX;
    private double myY;
    private double myZ;
    private int dirchange = 0;
    private int blocktype = 0;

    public IslandToo(World par1World) {
        super(par1World);
        this.func_70105_a(0.5f, 0.5f);
        this.ticker = par1World.field_73012_v.nextInt(50);
        this.dirchange = this.field_70170_p.field_73012_v.nextInt(5000);
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
            this.dir = this.field_70170_p.field_73012_v.nextInt(4);
            if (this.field_70170_p.field_73012_v.nextInt(40) != 1) {
                this.length = this.width = 1 + this.field_70170_p.field_73012_v.nextInt(5 * OreSpawnMain.IslandSizeFactor);
                this.depth = 1 + this.field_70170_p.field_73012_v.nextInt(4);
                this.speed = this.field_70170_p.field_73012_v.nextFloat() / 40.0f * (float)OreSpawnMain.IslandSpeedFactor;
                if (this.length * this.width * this.depth <= 64) {
                    this.speed *= 2.0f;
                }
                if (this.length * this.width * this.depth <= 32) {
                    this.speed *= 2.0f;
                }
            } else {
                this.length = this.width = 5 + this.field_70170_p.field_73012_v.nextInt(8 * OreSpawnMain.IslandSizeFactor);
                this.depth = 3 + this.field_70170_p.field_73012_v.nextInt(6);
                this.speed = this.field_70170_p.field_73012_v.nextFloat() / 150.0f * (float)OreSpawnMain.IslandSpeedFactor;
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
            this.dir = this.field_70170_p.field_73012_v.nextInt(4);
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
        this.width = par1NBTTagCompound.func_74762_e("Iwidth");
        this.depth = par1NBTTagCompound.func_74762_e("Idepth");
        this.length = par1NBTTagCompound.func_74762_e("Ilength");
        this.speed = par1NBTTagCompound.func_74760_g("Ispeed");
        this.dir = par1NBTTagCompound.func_74762_e("Idir");
        this.blocktype = par1NBTTagCompound.func_74762_e("Iblocktype");
    }

    public void func_70014_b(NBTTagCompound par1NBTTagCompound) {
        super.func_70014_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74768_a("JustSpawned", this.just_spawned);
        par1NBTTagCompound.func_74768_a("Iwidth", this.width);
        par1NBTTagCompound.func_74768_a("Idepth", this.depth);
        par1NBTTagCompound.func_74768_a("Ilength", this.length);
        par1NBTTagCompound.func_74776_a("Ispeed", this.speed);
        par1NBTTagCompound.func_74768_a("Idir", this.dir);
        par1NBTTagCompound.func_74768_a("Iblocktype", this.blocktype);
    }

    public EntityAgeable func_90011_a(EntityAgeable entityageable) {
        return null;
    }

    public boolean func_70097_a(DamageSource par1DamageSource, float par2) {
        boolean ret = false;
        boolean xoff = false;
        boolean zoff = false;
        int ix = (int)this.field_70165_t;
        int iz = (int)this.field_70161_v;
        if (ix < 0) {
            xoff = true;
            this.field_70165_t = ix;
            this.field_70165_t -= 0.5;
        } else {
            this.field_70165_t = ix;
            this.field_70165_t += 0.5;
        }
        if (iz < 0) {
            zoff = true;
            this.field_70161_v = iz;
            this.field_70161_v -= 0.5;
        } else {
            this.field_70161_v = iz;
            this.field_70161_v += 0.5;
        }
        super.func_70097_a(par1DamageSource, par2);
        return ret;
    }

    private void create_island() {
        int xoff = 0;
        int zoff = 0;
        if (this.field_70165_t < 0.0) {
            xoff = 1;
        }
        if (this.field_70161_v < 0.0) {
            zoff = 1;
        }
        for (int k = 0; k <= this.depth; ++k) {
            int il = this.length / (this.depth - k + 1);
            if (il < 1) {
                il = 1;
            }
            for (int i = -il; i <= il; ++i) {
                for (int j = -il; j <= il; ++j) {
                    int ix = (int)this.field_70165_t + j - xoff;
                    int iz = (int)this.field_70161_v + i - zoff;
                    if (k == this.depth) {
                        Block bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + k, iz);
                        if (bid == Blocks.field_150350_a) {
                            if (this.field_70170_p.field_73012_v.nextInt(5000) == 1) {
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + k, iz, Blocks.field_150355_j);
                                continue;
                            }
                            this.FastSetBlock(ix, (int)this.field_70163_u + k, iz, (Block)Blocks.field_150349_c);
                            if (this.field_70170_p.field_73012_v.nextInt(30) == 1) {
                                if (this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + k + 1, iz) != Blocks.field_150350_a) continue;
                                if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                                    this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + k + 1, iz, OreSpawnMain.MyFlowerPinkBlock);
                                    continue;
                                }
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + k + 1, iz, OreSpawnMain.MyFlowerBlueBlock);
                                continue;
                            }
                            if (this.field_70170_p.field_73012_v.nextInt(100) != 1 || this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + k + 1, iz) != Blocks.field_150350_a) continue;
                            OreSpawnMain.OreSpawnTrees.SmallTree(this.field_70170_p, ix, (int)this.field_70163_u + k + 1, iz);
                            continue;
                        }
                        if (bid != Blocks.field_150357_h) continue;
                        this.func_70106_y();
                        return;
                    }
                    this.mySetBlock(ix, (int)this.field_70163_u + k, iz);
                }
            }
        }
        this.field_70170_p.func_147449_b((int)this.field_70165_t - xoff, (int)this.field_70163_u, (int)this.field_70161_v - zoff, Blocks.field_150350_a);
    }

    private void mySetBlock(int ix, int iy, int iz) {
        Block bid = Blocks.field_150348_b;
        if (this.blocktype == 0) {
            this.blocktype = 1 + this.field_70170_p.field_73012_v.nextInt(8);
        }
        if (this.blocktype == 1 && this.field_70170_p.field_73012_v.nextInt(5) == 1) {
            bid = Blocks.field_150365_q;
        }
        if (this.blocktype == 2 && this.field_70170_p.field_73012_v.nextInt(10) == 1) {
            bid = Blocks.field_150366_p;
        }
        if (this.blocktype == 3 && this.field_70170_p.field_73012_v.nextInt(20) == 1) {
            bid = Blocks.field_150412_bA;
        }
        if (this.blocktype == 4 && this.field_70170_p.field_73012_v.nextInt(30) == 1) {
            bid = OreSpawnMain.MyOreTitaniumBlock;
        }
        if (this.blocktype == 5 && this.field_70170_p.field_73012_v.nextInt(30) == 1) {
            bid = OreSpawnMain.MyOreUraniumBlock;
        }
        if (this.blocktype == 6 && this.field_70170_p.field_73012_v.nextInt(30) == 1) {
            bid = OreSpawnMain.MyOreRubyBlock;
        }
        if (this.blocktype == 7 && this.field_70170_p.field_73012_v.nextInt(30) == 1) {
            bid = OreSpawnMain.MyOreAmethystBlock;
        }
        if (this.blocktype == 8 && this.field_70170_p.field_73012_v.nextInt(20) == 1) {
            bid = Blocks.field_150352_o;
        }
        if (bid == Blocks.field_150348_b) {
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 1) {
                bid = OreSpawnMain.MyEnderPearlBlock;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 2) {
                bid = OreSpawnMain.MyEyeOfEnderBlock;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 3) {
                bid = OreSpawnMain.MyBlockAmethystBlock;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 4) {
                bid = OreSpawnMain.MyBlockRubyBlock;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 5) {
                bid = OreSpawnMain.MyBlockUraniumBlock;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 6) {
                bid = OreSpawnMain.MyBlockTitaniumBlock;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 7) {
                bid = Blocks.field_150340_R;
            }
            if (this.field_70170_p.field_73012_v.nextInt(3000) == 8) {
                bid = Blocks.field_150484_ah;
            }
        }
        this.FastSetBlock(ix, iy, iz, bid);
    }

    private void update_island() {
        int xoff = 0;
        int zoff = 0;
        if (this.dir == 0) {
            this.myZ -= (double)this.speed;
        } else if (this.dir == 1) {
            this.myZ += (double)this.speed;
        } else {
            this.myX = this.dir == 2 ? (this.myX += (double)this.speed) : (this.myX -= (double)this.speed);
        }
        int ke = 0;
        int ks = 0;
        int je = 0;
        int js = 0;
        int mx = (int)this.myX;
        int mz = (int)this.myZ;
        int px = (int)this.field_70165_t;
        int pz = (int)this.field_70161_v;
        if (mx != px || mz != pz) {
            Block bid;
            int iz;
            int ix;
            int k;
            int j;
            int il;
            int i;
            if (this.dir == 0) {
                js = 1;
                je = 1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 1) {
                js = -1;
                je = -1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 2) {
                js = -1;
                je = 1;
                ks = -1;
                ke = -1;
            } else {
                js = -1;
                je = 1;
                ks = 1;
                ke = 1;
            }
            if (this.field_70165_t < 0.0) {
                xoff = 1;
            }
            if (this.field_70161_v < 0.0) {
                zoff = 1;
            }
            for (i = 0; i <= this.depth; ++i) {
                il = this.length / (this.depth - i + 1);
                if (il < 1) {
                    il = 1;
                }
                for (j = js * il; j <= je * il; ++j) {
                    for (k = ks * il; k <= ke * il; ++k) {
                        ix = (int)this.field_70165_t + k - xoff;
                        iz = (int)this.field_70161_v + j - zoff;
                        if (i == this.depth) {
                            bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i + 1, iz);
                            if (bid == OreSpawnMain.MyFlowerPinkBlock || bid == OreSpawnMain.MyFlowerBlueBlock || bid == OreSpawnMain.MyFlowerBlackBlock || bid == OreSpawnMain.MyFlowerScaryBlock) {
                                this.FastSetBlock(ix, (int)this.field_70163_u + i + 1, iz, Blocks.field_150350_a);
                            }
                            if (bid == Blocks.field_150355_j || bid == Blocks.field_150358_i) {
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i, iz, Blocks.field_150350_a);
                            }
                            if (bid == OreSpawnMain.MySkyTreeLog) {
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i + 1, iz, Blocks.field_150350_a);
                                bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i + 2, iz);
                                if (bid == OreSpawnMain.MySkyTreeLog) {
                                    this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i + 2, iz, Blocks.field_150350_a);
                                    bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i + 3, iz);
                                    if (bid == OreSpawnMain.MySkyTreeLog) {
                                        this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i + 3, iz, Blocks.field_150350_a);
                                    }
                                }
                            }
                            if ((bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i, iz)) == Blocks.field_150355_j || bid == Blocks.field_150358_i) {
                                this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i, iz, Blocks.field_150350_a);
                            }
                        }
                        this.FastSetBlock(ix, (int)this.field_70163_u + i, iz, Blocks.field_150350_a);
                    }
                }
            }
            this.mySetBlock((int)this.field_70165_t - xoff, (int)this.field_70163_u, (int)this.field_70161_v - zoff);
            this.field_70165_t = mx;
            this.field_70165_t = this.myX < 0.0 ? (this.field_70165_t -= 0.5) : (this.field_70165_t += 0.5);
            this.field_70161_v = mz;
            this.field_70161_v = this.myZ < 0.0 ? (this.field_70161_v -= 0.5) : (this.field_70161_v += 0.5);
            if (this.dir == 0) {
                js = -1;
                je = -1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 1) {
                js = 1;
                je = 1;
                ks = -1;
                ke = 1;
            } else if (this.dir == 2) {
                js = -1;
                je = 1;
                ks = 1;
                ke = 1;
            } else {
                js = -1;
                je = 1;
                ks = -1;
                ke = -1;
            }
            zoff = 0;
            xoff = 0;
            if (this.field_70165_t < 0.0) {
                xoff = 1;
            }
            if (this.field_70161_v < 0.0) {
                zoff = 1;
            }
            this.field_70170_p.func_147449_b((int)this.field_70165_t - xoff, (int)this.field_70163_u, (int)this.field_70161_v - zoff, Blocks.field_150350_a);
            for (i = 0; i <= this.depth; ++i) {
                il = this.length / (this.depth - i + 1);
                if (il < 1) {
                    il = 1;
                }
                for (j = js * il; j <= je * il; ++j) {
                    for (k = ks * il; k <= ke * il; ++k) {
                        ix = (int)this.field_70165_t + k - xoff;
                        iz = (int)this.field_70161_v + j - zoff;
                        if (i == this.depth) {
                            bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i, iz);
                            if (bid == Blocks.field_150350_a) {
                                if (this.field_70170_p.field_73012_v.nextInt(5000) == 1) {
                                    this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i, iz, Blocks.field_150355_j);
                                    continue;
                                }
                                this.FastSetBlock(ix, (int)this.field_70163_u + i, iz, (Block)Blocks.field_150349_c);
                                if (this.field_70170_p.field_73012_v.nextInt(30) == 1) {
                                    if (this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i + 1, iz) != Blocks.field_150350_a) continue;
                                    if (this.field_70170_p.field_73012_v.nextInt(2) == 1) {
                                        this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i + 1, iz, OreSpawnMain.MyFlowerPinkBlock);
                                        continue;
                                    }
                                    this.field_70170_p.func_147449_b(ix, (int)this.field_70163_u + i + 1, iz, OreSpawnMain.MyFlowerBlueBlock);
                                    continue;
                                }
                                if (this.field_70170_p.field_73012_v.nextInt(100) != 1 || this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i + 1, iz) != Blocks.field_150350_a) continue;
                                OreSpawnMain.OreSpawnTrees.SmallTree(this.field_70170_p, ix, (int)this.field_70163_u + i + 1, iz);
                                continue;
                            }
                            if (bid != Blocks.field_150357_h) continue;
                            this.func_70106_y();
                            return;
                        }
                        bid = this.field_70170_p.func_147439_a(ix, (int)this.field_70163_u + i, iz);
                        if (bid == Blocks.field_150377_bs) {
                            if (this.field_70170_p.field_72995_K) continue;
                            this.field_70170_p.func_72876_a((Entity)this, (double)ix, this.field_70163_u + (double)i, (double)iz, 5.0f, true);
                            continue;
                        }
                        this.mySetBlock(ix, (int)this.field_70163_u + i, iz);
                    }
                }
            }
            this.field_70170_p.func_147449_b((int)this.field_70165_t - xoff, (int)this.field_70163_u, (int)this.field_70161_v - zoff, Blocks.field_150350_a);
        }
    }

    protected Item func_146068_u() {
        return Item.func_150898_a((Block)OreSpawnMain.MyIslandBlock);
    }

    public void FastSetBlock(int ix, int iy, int iz, Block id) {
        OreSpawnMain.setBlockFast(this.field_70170_p, ix, iy, iz, id, 0, 3);
    }
}

