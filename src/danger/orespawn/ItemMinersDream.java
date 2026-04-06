/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMinersDream
extends Item {
    public ItemMinersDream(int i) {
        this.field_77777_bU = 16;
        this.func_77637_a(CreativeTabs.field_78028_d);
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer Player, World world, int cposx, int cposy, int cposz, int par7, float par8, float par9, float par10) {
        int deltax = 0;
        int deltaz = 0;
        int dirx = 0;
        int dirz = 0;
        int height = 5;
        int width = 5;
        int length = 64;
        int torches = 5;
        int solid_count = 0;
        if (cposx < 0) {
            dirx = -1;
        }
        if (cposz < 0) {
            dirz = -1;
        }
        int pposx = (int)(Player.field_70165_t + 0.99 * (double)dirx);
        int pposy = (int)Player.field_70163_u;
        int pposz = (int)(Player.field_70161_v + 0.99 * (double)dirz);
        if (cposx - pposx == 0 || cposz - pposz == 0) {
            Block bid;
            int k;
            int x = cposx;
            int y = pposy;
            int z = cposz;
            if (x - pposx < 0) {
                deltax = -1;
            }
            if (x - pposx > 0) {
                deltax = 1;
            }
            if (z - pposz < 0) {
                deltaz = -1;
            }
            if (z - pposz > 0) {
                deltaz = 1;
            }
            if (deltax == 0 && deltaz == 0) {
                return false;
            }
            if (deltax != 0 && deltaz != 0) {
                return false;
            }
            Player.field_70170_p.func_72956_a((Entity)Player, "random.explode", 1.0f, 1.5f);
            if (world.field_72995_K) {
                return true;
            }
            for (int i = 0; i < height; ++i) {
                for (k = 0; k < length; ++k) {
                    int j;
                    solid_count = 0;
                    for (j = -width; j <= width; ++j) {
                        bid = world.func_147439_a(x + k * deltax + j * deltaz, y + i, z + k * deltaz + j * deltax);
                        if (bid == Blocks.field_150348_b || bid == Blocks.field_150346_d || bid == Blocks.field_150351_n || bid == Blocks.field_150358_i || bid == Blocks.field_150355_j || bid == Blocks.field_150356_k || bid == Blocks.field_150353_l || bid == Blocks.field_150424_aL || bid == Blocks.field_150377_bs || bid == OreSpawnMain.CrystalStone) {
                            world.func_147465_d(x + k * deltax + j * deltaz, y + i, z + k * deltaz + j * deltax, Blocks.field_150350_a, 0, 2);
                        }
                        if (i != height - 1) continue;
                        bid = world.func_147439_a(x + k * deltax + j * deltaz, y + i + 1, z + k * deltaz + j * deltax);
                        if (bid != Blocks.field_150350_a) {
                            ++solid_count;
                        }
                        if (bid != Blocks.field_150350_a && bid != Blocks.field_150351_n && bid != Blocks.field_150354_m && bid != Blocks.field_150358_i && bid != Blocks.field_150355_j && bid != Blocks.field_150356_k && bid != Blocks.field_150353_l) continue;
                        if (world.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5) {
                            world.func_147465_d(x + k * deltax + j * deltaz, y + i + 1, z + k * deltaz + j * deltax, OreSpawnMain.CrystalStone, 0, 2);
                            continue;
                        }
                        world.func_147465_d(x + k * deltax + j * deltaz, y + i + 1, z + k * deltaz + j * deltax, Blocks.field_150347_e, 0, 2);
                    }
                    if (i != height - 1 || solid_count != 0) continue;
                    for (j = -width; j <= width; ++j) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y + i + 1, z + k * deltaz + j * deltax, Blocks.field_150350_a, 0, 2);
                    }
                }
            }
            for (k = 0; k < length; k += torches) {
                bid = world.func_147439_a(x + k * deltax, y - 1, z + k * deltaz);
                if ((bid == Blocks.field_150348_b || bid == Blocks.field_150346_d || bid == Blocks.field_150351_n || bid == Blocks.field_150424_aL || bid == Blocks.field_150377_bs || bid == Blocks.field_150357_h) && world.func_147437_c(x + k * deltax, y, z + k * deltaz)) {
                    world.func_147465_d(x + k * deltax, y, z + k * deltaz, OreSpawnMain.ExtremeTorch, 0, 2);
                }
                if (bid != OreSpawnMain.CrystalStone || !world.func_147437_c(x + k * deltax, y, z + k * deltaz)) continue;
                world.func_147465_d(x + k * deltax, y, z + k * deltaz, OreSpawnMain.CrystalTorch, 0, 2);
            }
            if (!Player.field_71075_bZ.field_75098_d) {
                --par1ItemStack.field_77994_a;
            }
            return true;
        }
        return false;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

