/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.Chunk
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ItemAppleSeed
extends Item {
    public ItemAppleSeed(int i) {
        this.field_77777_bU = 16;
        this.func_77637_a(CreativeTabs.field_78031_c);
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if (!world.field_72995_K) {
            Block bid = world.func_147439_a(x, y, z);
            if (bid != Blocks.field_150349_c && bid != Blocks.field_150346_d && bid != Blocks.field_150458_ak) {
                return false;
            }
            if (this == OreSpawnMain.MyAppleSeed) {
                this.makeTree(world, x, y, z, OreSpawnMain.MyAppleLeaves, null);
            } else if (this == OreSpawnMain.MyCherrySeed) {
                this.makeTree(world, x, y, z, OreSpawnMain.MyCherryLeaves, null);
            } else {
                this.makeTree(world, x, y, z, OreSpawnMain.MyPeachLeaves, null);
            }
        }
        if (!par2EntityPlayer.field_71075_bZ.field_75098_d) {
            --par1ItemStack.field_77994_a;
        }
        return true;
    }

    public void makeTree(World world, int x, int y, int z, Block blkid, Chunk chunk) {
        int j;
        Block bid = world.func_147439_a(x, y, z);
        if (bid != Blocks.field_150349_c && bid != Blocks.field_150346_d && bid != Blocks.field_150458_ak) {
            return;
        }
        int h1 = 12;
        int h2 = 6;
        int h3 = 9;
        int h4 = 6;
        int h5 = 14;
        int w1 = 5;
        int w2 = 3;
        if (blkid == OreSpawnMain.MyPeachLeaves) {
            h1 = 10;
            h2 = 5;
            h3 = 7;
            h4 = 5;
            h5 = 12;
            w1 = 4;
            w2 = 2;
        }
        if (blkid == OreSpawnMain.MyCherryLeaves) {
            h1 = 8;
            h2 = 3;
            h3 = 5;
            h4 = 3;
            h5 = 10;
            w1 = 3;
            w2 = 1;
        }
        for (j = 1; j < h1; ++j) {
            world.func_147465_d(x, y + j, z, Blocks.field_150364_r, 0, 2);
        }
        for (j = 1; j < w1; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x + j, y + h2, z, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w1; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x - j, y + h2, z, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w1; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x, y + h2, z + j, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w1; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x, y + h2, z - j, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w2; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x + j, y + h3, z, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w2; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x - j, y + h3, z, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w2; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x, y + h3, z + j, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (j = 1; j < w2; ++j) {
            OreSpawnMain.setBlockSuperFast(world, x, y + h3, z - j, Blocks.field_150364_r, 0, 2, chunk);
        }
        for (int i = h4; i < h5; ++i) {
            int width = 6;
            if (i > 8) {
                width = 5;
            }
            if (i > 10) {
                width = 4;
            }
            if (blkid != OreSpawnMain.MyAppleLeaves) {
                --width;
            }
            for (j = -width; j <= width; ++j) {
                for (int k = -width; k <= width; ++k) {
                    bid = world.func_147439_a(x + k, y + i, z + j);
                    if (bid != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockSuperFast(world, x + k, y + i, z + j, blkid, 0, 2, chunk);
                }
            }
        }
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

