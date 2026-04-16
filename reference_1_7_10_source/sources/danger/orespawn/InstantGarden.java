/*
 * Decompiled with CFR 0.152.
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

public class InstantGarden
extends Item {
    public InstantGarden(int i) {
        this.field_77777_bU = 16;
        this.func_77637_a(CreativeTabs.field_78028_d);
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer Player, World world, int cposx, int cposy, int cposz, int par7, float par8, float par9, float par10) {
        int deltax = 0;
        int deltaz = 0;
        boolean bid = false;
        int dirx = 0;
        int dirz = 0;
        int height = 10;
        int width = 7;
        int length = 18;
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
            int j;
            int k;
            int i;
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
            for (i = 0; i < height; ++i) {
                for (k = 0; k < length; ++k) {
                    for (j = -width; j <= width; ++j) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y + i, z + k * deltaz + j * deltax, Blocks.field_150350_a, 0, 2);
                        if (i != 0) continue;
                        world.func_147465_d(x + k * deltax + j * deltaz, y + i - 1, z + k * deltaz + j * deltax, (Block)Blocks.field_150349_c, 0, 2);
                    }
                }
            }
            for (k = 1; k < length - 1; ++k) {
                i = 0;
                for (j = -width; j <= width; ++j) {
                    if (i == 1) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, OreSpawnMain.MyRadishPlant, 0, 2);
                    }
                    if (i == 2) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, OreSpawnMain.MyLettucePlant1, 0, 2);
                    }
                    if (i == 3) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, Blocks.field_150459_bM, 0, 2);
                    }
                    if (i == 4) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150355_j, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 2, z + k * deltaz + j * deltax, Blocks.field_150347_e, 0, 2);
                    }
                    if (i == 5) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, Blocks.field_150469_bN, 0, 2);
                    }
                    if (i == 6) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, Blocks.field_150464_aj, 0, 2);
                    }
                    if (i == 7) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, OreSpawnMain.MyTomatoPlant1, 0, 2);
                    }
                    if (i == 8) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150355_j, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 2, z + k * deltaz + j * deltax, Blocks.field_150347_e, 0, 2);
                    }
                    if (i == 9) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, OreSpawnMain.MyCornPlant1, 0, 2);
                    }
                    if (i == 10) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, OreSpawnMain.MyStrawberryPlant, 0, 2);
                    }
                    if (i == 11) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 2, z + k * deltaz + j * deltax, Blocks.field_150347_e, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, (Block)Blocks.field_150354_m, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, Blocks.field_150436_aH, 0, 2);
                    }
                    if (i == 12) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150355_j, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 2, z + k * deltaz + j * deltax, Blocks.field_150347_e, 0, 2);
                    }
                    if (i == 13) {
                        world.func_147465_d(x + k * deltax + j * deltaz, y - 1, z + k * deltaz + j * deltax, Blocks.field_150458_ak, 0, 2);
                        world.func_147465_d(x + k * deltax + j * deltaz, y, z + k * deltaz + j * deltax, Blocks.field_150394_bc, 0, 2);
                    }
                    ++i;
                }
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

