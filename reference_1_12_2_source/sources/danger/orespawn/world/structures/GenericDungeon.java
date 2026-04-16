/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockChest
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.init.Blocks
 *  net.minecraft.tileentity.TileEntity
 *  net.minecraft.tileentity.TileEntityChest
 *  net.minecraft.tileentity.TileEntityMobSpawner
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.world.structures;

import net.minecraft.block.BlockChest;
import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GenericDungeon {
    private ResourceLocation LOOT_TABLE = new ResourceLocation("orespawn", "generic_dungeon");

    private void setThisBlock(World world, int cposx, int cposy, int cposz) {
        if (world.field_73012_v.nextInt(2) == 1) {
            world.func_175656_a(new BlockPos(cposx, cposy, cposz), Blocks.field_150341_Y.func_176223_P());
        } else {
            world.func_175656_a(new BlockPos(cposx, cposy, cposz), Blocks.field_150347_e.func_176223_P());
        }
    }

    private TileEntityChest getChestTileEntity(World world, int cposx, int cposy, int cposz) {
        TileEntityChest chest = null;
        TileEntity t = null;
        t = world.func_175625_s(new BlockPos(cposx, cposy, cposz));
        if (t instanceof TileEntityChest) {
            chest = (TileEntityChest)t;
        }
        return chest;
    }

    private TileEntityMobSpawner getSpawnerTileEntity(World world, int cposx, int cposy, int cposz) {
        TileEntityMobSpawner chest = null;
        TileEntity t = null;
        t = world.func_175625_s(new BlockPos(cposx, cposy, cposz));
        if (t instanceof TileEntityMobSpawner) {
            chest = (TileEntityMobSpawner)t;
            return chest;
        }
        return null;
    }

    public void makeDungeon(World world, int cposx, int cposy, int cposz) {
        int k;
        int j;
        int i;
        int width = 12;
        int height = 6;
        for (i = 0; i < width; ++i) {
            for (j = 0; j < height; ++j) {
                for (k = 0; k < width; ++k) {
                    world.func_175656_a(new BlockPos(cposx + i, cposy + j, cposz + k), Blocks.field_150350_a.func_176223_P());
                }
            }
        }
        for (i = 0; i < width; ++i) {
            j = 0;
            for (k = 0; k < width; ++k) {
                world.func_175656_a(new BlockPos(cposx + i, cposy + j, cposz + k), Blocks.field_150341_Y.func_176223_P());
            }
        }
        for (i = 0; i < width; ++i) {
            j = height - 1;
            for (k = 0; k < width; ++k) {
                this.setThisBlock(world, cposx + i, cposy + j, cposz + k);
            }
        }
        for (i = 0; i < width; ++i) {
            for (j = 0; j < height; ++j) {
                k = 0;
                this.setThisBlock(world, cposx + i, cposy + j, cposz + k);
                k = width - 1;
                this.setThisBlock(world, cposx + i, cposy + j, cposz + k);
            }
        }
        for (k = 0; k < width; ++k) {
            for (j = 0; j < height; ++j) {
                i = 0;
                this.setThisBlock(world, cposx + i, cposy + j, cposz + k);
                i = width - 1;
                this.setThisBlock(world, cposx + i, cposy + j, cposz + k);
            }
        }
        world.func_175656_a(new BlockPos(cposx + width / 2, cposy + 1, cposz + width / 2), Blocks.field_150474_ac.func_176223_P());
        TileEntityMobSpawner tileentitymobspawner = this.getSpawnerTileEntity(world, cposx + width / 2, cposy + 1, cposz + width / 2);
        if (tileentitymobspawner != null) {
            int t = world.field_73012_v.nextInt(3);
            if (t == 0) {
                tileentitymobspawner.func_145881_a().func_190894_a(new ResourceLocation("orespawn:alien"));
            }
            if (t == 1) {
                tileentitymobspawner.func_145881_a().func_190894_a(new ResourceLocation("orespawn:gammametroid"));
            }
            if (t == 2) {
                tileentitymobspawner.func_145881_a().func_190894_a(new ResourceLocation("orespawn:cryolophosaurus"));
            }
        }
        TileEntityChest chest = null;
        world.func_175656_a(new BlockPos(cposx + width / 2, cposy + 1, cposz + 1), Blocks.field_150486_ae.func_176223_P().func_177226_a((IProperty)BlockChest.field_176459_a, (Comparable)EnumFacing.SOUTH));
        chest = this.getChestTileEntity(world, cposx + width / 2, cposy + 1, cposz + 1);
        if (chest != null) {
            chest.func_189404_a(this.LOOT_TABLE, (long)(world.field_73012_v.nextInt() * cposx * cposy * cposz));
        }
    }
}

