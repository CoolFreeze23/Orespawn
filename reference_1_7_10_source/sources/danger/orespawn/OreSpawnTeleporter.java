/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class OreSpawnTeleporter
extends Teleporter {
    private WorldServer world;
    private World oldWorld;
    private Random random;
    private int newdim;

    public OreSpawnTeleporter(WorldServer par1WorldServer, int dim, World par2World) {
        super(par1WorldServer);
        this.world = par1WorldServer;
        this.oldWorld = par2World;
        this.random = new Random(par1WorldServer.func_72905_C());
        this.newdim = dim;
    }

    public void func_77185_a(Entity par1Entity, double par2, double par4, double par6, float par8) {
        this.justPutMe(par1Entity);
    }

    public boolean func_77184_b(Entity par1Entity, double par2, double par4, double par6, float par8) {
        this.justPutMe(par1Entity);
        return true;
    }

    public boolean func_85188_a(Entity par1Entity) {
        return true;
    }

    private boolean isGroundBlock(Block bid) {
        if (bid == Blocks.field_150350_a) {
            return false;
        }
        if (bid == Blocks.field_150346_d) {
            return true;
        }
        if (bid == Blocks.field_150349_c) {
            return true;
        }
        if (bid == Blocks.field_150348_b) {
            return true;
        }
        if (bid == Blocks.field_150377_bs) {
            return true;
        }
        if (bid == Blocks.field_150424_aL) {
            return true;
        }
        if (bid == Blocks.field_150347_e) {
            return true;
        }
        if (bid == Blocks.field_150354_m) {
            return true;
        }
        if (bid == Blocks.field_150322_A) {
            return true;
        }
        return bid == Blocks.field_150458_ak;
    }

    public boolean justPutMe(Entity par1Entity) {
        int posX = (int)par1Entity.field_70165_t;
        int posZ = (int)par1Entity.field_70161_v;
        int posY = 120;
        boolean found = false;
        int inarow = 0;
        boolean airfound = false;
        for (int i = 0; i < 1000 && !found; ++i) {
            for (posY = 180; posY > 1; --posY) {
                Block bid = this.world.func_147439_a(posX, posY + 1, posZ);
                if (bid == Blocks.field_150350_a || bid == null) {
                    inarow = 0;
                    bid = this.world.func_147439_a(posX, posY, posZ);
                    if (bid != Blocks.field_150350_a && bid != null) continue;
                    airfound = true;
                    bid = this.world.func_147439_a(posX, posY - 1, posZ);
                    if (bid == Blocks.field_150350_a || bid == null) continue;
                    if (this.world.func_147439_a(posX, posY - 1, posZ).func_149688_o().func_76220_a()) {
                        found = true;
                        break;
                    }
                    if (bid != Blocks.field_150329_H || !this.world.func_147439_a(posX, posY - 2, posZ).func_149688_o().func_76220_a()) break;
                    found = true;
                    --posY;
                    break;
                }
                if (this.isGroundBlock(bid)) {
                    ++inarow;
                }
                if (airfound && inarow >= 3) break;
            }
            if (found) continue;
            posX = (int)par1Entity.field_70165_t + this.world.field_73012_v.nextInt(3 + i / 5) - this.world.field_73012_v.nextInt(3 + i / 5);
            if (i > 100) {
                posX = posX + OreSpawnMain.OreSpawnRand.nextInt(2 + i / 5) - OreSpawnMain.OreSpawnRand.nextInt(2 + i / 5);
            }
            if (i > 500) {
                posX = posX + this.random.nextInt(2 + i / 5) - this.random.nextInt(2 + i / 5);
            }
            posZ = (int)par1Entity.field_70161_v + this.world.field_73012_v.nextInt(3 + i / 5) - this.world.field_73012_v.nextInt(3 + i / 5);
            if (i > 100) {
                posZ = posZ + OreSpawnMain.OreSpawnRand.nextInt(2 + i / 5) - OreSpawnMain.OreSpawnRand.nextInt(2 + i / 5);
            }
            if (i > 500) {
                posZ = posZ + this.random.nextInt(2 + i / 5) - this.random.nextInt(2 + i / 5);
            }
            airfound = false;
            inarow = 0;
        }
        if (!found) {
            posX = (int)par1Entity.field_70165_t;
            posZ = (int)par1Entity.field_70161_v;
            for (posY = 180; posY > 1 && (Blocks.field_150350_a != this.world.func_147439_a(posX, posY + 1, posZ) || Blocks.field_150350_a != this.world.func_147439_a(posX, posY, posZ) || Blocks.field_150350_a == this.world.func_147439_a(posX, posY - 1, posZ)); --posY) {
            }
        }
        double oldX = par1Entity.field_70165_t;
        double oldY = par1Entity.field_70163_u;
        double oldZ = par1Entity.field_70161_v;
        double newX = posX;
        double newZ = posZ;
        double newY = posY;
        newX = newX < 0.0 ? (newX -= 0.5) : (newX += 0.5);
        newZ = newZ < 0.0 ? (newZ -= 0.5) : (newZ += 0.5);
        par1Entity.func_70012_b(newX, newY, newZ, par1Entity.field_70177_z, 0.0f);
        par1Entity.field_70179_y = 0.0;
        par1Entity.field_70181_x = 0.0;
        par1Entity.field_70159_w = 0.0;
        MinecraftServer minecraftserver = MinecraftServer.func_71276_C();
        WorldServer worldserver = minecraftserver.func_71218_a(this.oldWorld.field_73011_w.field_76574_g);
        WorldServer worldserver1 = minecraftserver.func_71218_a(this.newdim);
        if (par1Entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer)par1Entity;
            AxisAlignedBB bb = AxisAlignedBB.func_72330_a((double)(oldX - 24.0), (double)(oldY - 12.0), (double)(oldZ - 24.0), (double)(oldX + 24.0), (double)(oldY + 12.0), (double)(oldZ + 24.0));
            List var5 = this.oldWorld.func_72872_a(EntityTameable.class, bb);
            for (Entity var3 : var5) {
                EntityTameable et = (EntityTameable)var3;
                if (et.func_70906_o()) continue;
                String p1 = ep.func_110124_au().toString();
                String p2 = et.func_152113_b();
                if ((p1 == null || p2 == null || !p1.equals(p2)) && !et.func_152114_e((EntityLivingBase)ep)) continue;
                this.sendToThisDimension(var3, newX, newY, newZ, (int)ep.field_70177_z);
            }
        }
        worldserver.func_82742_i();
        worldserver1.func_82742_i();
        return true;
    }

    public void sendToThisDimension(Entity e, double newX, double newY, double newZ, int ro) {
        if (this.oldWorld.field_72995_K) {
            return;
        }
        e.field_70170_p.func_72900_e(e);
        e.field_70128_L = false;
        e.func_70012_b(newX, newY, newZ, (float)ro, 0.0f);
        e.field_70179_y = 0.0;
        e.field_70181_x = 0.0;
        e.field_70159_w = 0.0;
        e.func_70029_a((World)this.world);
        Entity var6 = EntityList.func_75620_a((String)EntityList.func_75621_b((Entity)e), (World)this.world);
        if (var6 != null) {
            var6.func_82141_a(e, true);
            var6.func_70012_b(newX, newY, newZ, (float)ro, 0.0f);
            var6.field_70179_y = 0.0;
            var6.field_70181_x = 0.0;
            var6.field_70159_w = 0.0;
            var6.func_70029_a((World)this.world);
            this.world.func_72838_d(var6);
        }
        e.field_70128_L = true;
    }

    public void func_85189_a(long par1) {
    }
}

