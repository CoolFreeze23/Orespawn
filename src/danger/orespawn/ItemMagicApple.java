/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  cpw.mods.fml.relauncher.Side
 *  cpw.mods.fml.relauncher.SideOnly
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockLeaves
 *  net.minecraft.client.renderer.texture.IIconRegister
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.tileentity.TileEntityChest
 *  net.minecraft.util.WeightedRandomChestContent
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.Chunk
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.TheKing;
import danger.orespawn.TheQueen;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ItemMagicApple
extends Item {
    public int tree_radius = 6;
    public boolean no_critters = false;
    Random rand = OreSpawnMain.OreSpawnRand;
    private final WeightedRandomChestContent[] chestContentsList = new WeightedRandomChestContent[]{new WeightedRandomChestContent(Items.field_151079_bi, 0, 1, 2, 3), new WeightedRandomChestContent(Items.field_151045_i, 0, 1, 5, 15), new WeightedRandomChestContent(Items.field_151072_bj, 0, 1, 3, 10), new WeightedRandomChestContent(OreSpawnMain.CageEmpty, 0, 1, 10, 7), new WeightedRandomChestContent(OreSpawnMain.CagedGirlfriend, 0, 1, 2, 6), new WeightedRandomChestContent(Items.field_151042_j, 0, 1, 10, 16), new WeightedRandomChestContent(Items.field_151043_k, 0, 1, 6, 16), new WeightedRandomChestContent(OreSpawnMain.UraniumNugget, 0, 1, 6, 6), new WeightedRandomChestContent(OreSpawnMain.TitaniumNugget, 0, 1, 4, 6), new WeightedRandomChestContent(Items.field_151025_P, 0, 1, 8, 20), new WeightedRandomChestContent(Items.field_151034_e, 0, 1, 8, 20), new WeightedRandomChestContent(Items.field_151106_aX, 0, 1, 16, 20), new WeightedRandomChestContent(Items.field_151083_be, 0, 1, 8, 20), new WeightedRandomChestContent(Items.field_151077_bg, 0, 1, 8, 20), new WeightedRandomChestContent(Items.field_151101_aQ, 0, 1, 8, 20), new WeightedRandomChestContent(Items.field_151157_am, 0, 1, 8, 20), new WeightedRandomChestContent(Items.field_151158_bO, 0, 1, 4, 20), new WeightedRandomChestContent(Items.field_151172_bF, 0, 1, 16, 20), new WeightedRandomChestContent(Items.field_151174_bG, 0, 1, 16, 20), new WeightedRandomChestContent(OreSpawnMain.MySunFish, 0, 1, 4, 6), new WeightedRandomChestContent(OreSpawnMain.MyFireFish, 0, 1, 8, 6), new WeightedRandomChestContent(OreSpawnMain.MyPopcornBag, 0, 1, 4, 16), new WeightedRandomChestContent(Items.field_151035_b, 0, 1, 1, 20), new WeightedRandomChestContent(Items.field_151040_l, 0, 1, 1, 20), new WeightedRandomChestContent(Items.field_151046_w, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151048_u, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.field_151031_f, 0, 1, 1, 20), new WeightedRandomChestContent(Items.field_151032_g, 0, 1, 64, 20), new WeightedRandomChestContent(OreSpawnMain.MyUltimatePickaxe, 0, 1, 1, 2), new WeightedRandomChestContent(OreSpawnMain.MyUltimateSword, 0, 1, 1, 1), new WeightedRandomChestContent(OreSpawnMain.MyUltimateFishingRod, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.field_151030_Z, 0, 1, 1, 20), new WeightedRandomChestContent((Item)Items.field_151028_Y, 0, 1, 1, 20), new WeightedRandomChestContent((Item)Items.field_151165_aa, 0, 1, 1, 20), new WeightedRandomChestContent((Item)Items.field_151167_ab, 0, 1, 1, 20), new WeightedRandomChestContent((Item)Items.field_151163_ad, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.field_151161_ac, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.field_151173_ae, 0, 1, 1, 5), new WeightedRandomChestContent((Item)Items.field_151175_af, 0, 1, 1, 5), new WeightedRandomChestContent(Items.field_151153_ao, 0, 1, 1, 5)};

    public ItemMagicApple(int i) {
        this.field_77777_bU = 1;
        this.func_77637_a(CreativeTabs.field_78031_c);
    }

    public void func_77622_d(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par1ItemStack.func_77966_a(Enchantment.field_77346_s, 2);
    }

    private Entity spawnCreature(World par0World, int par1, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = EntityList.func_75616_a((int)par1, (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int lvl = EnchantmentHelper.func_77506_a((int)Enchantment.field_77346_s.field_77352_x, (ItemStack)stack);
        if (lvl <= 0) {
            stack.func_77966_a(Enchantment.field_77346_s, 2);
        }
    }

    public void func_77663_a(ItemStack stack, World par2World, Entity par3Entity, int par4, boolean par5) {
        this.onUsingTick(stack, null, 0);
    }

    private Boolean isBoringBlock(World world, int x, int y, int z) {
        Block var1 = world.func_147439_a(x, y, z);
        if (var1 == Blocks.field_150329_H) {
            return true;
        }
        if (var1 == Blocks.field_150434_aF) {
            return true;
        }
        if (var1 == Blocks.field_150328_O) {
            return true;
        }
        if (var1 == Blocks.field_150327_N) {
            return true;
        }
        if (var1 == Blocks.field_150362_t) {
            return true;
        }
        if (var1 == Blocks.field_150433_aE) {
            return true;
        }
        if (var1 == OreSpawnMain.MyStrawberryPlant) {
            return true;
        }
        if (var1 == OreSpawnMain.MyAppleLeaves) {
            return true;
        }
        if (world.func_147437_c(x, y, z)) {
            return true;
        }
        if (var1 == null) {
            return true;
        }
        return false;
    }

    private Boolean isBoringBaseBlock(World world, int x, int y, int z) {
        if (world.func_147437_c(x, y, z)) {
            return true;
        }
        Block var1 = world.func_147439_a(x, y, z);
        if (var1 == Blocks.field_150348_b) {
            return false;
        }
        if (var1 == Blocks.field_150357_h) {
            return false;
        }
        return true;
    }

    private void growVines(World world, int par2, int par3, int par4, int par5, int par6, Chunk chunk) {
        if (world.func_147439_a(par2, par3, par4) != Blocks.field_150350_a) {
            return;
        }
        this.FastSetBlock(world, par2, par3, par4, Blocks.field_150395_bd, par5, 2, chunk);
        while (par6 > 0) {
            if (world.func_147439_a(par2, --par3, par4) != Blocks.field_150350_a) {
                return;
            }
            this.FastSetBlock(world, par2, par3, par4, Blocks.field_150395_bd, par5, 2, chunk);
            --par6;
        }
    }

    private void make_branch(World world, int x, int y, int z, int this_width, int dirx, int dirz, Block ID, Block leafID, int tree_type, int t_radius, boolean bad_critters, Chunk chunk) {
        int current_width = this_width;
        int last_branch = 0;
        int branch_side = 1;
        int leaf_depth = 0;
        int leaf_width = 0;
        int xaccum = dirx;
        int zaccum = dirz;
        if (this.rand.nextInt(2) == 0) {
            branch_side = -1;
        }
        while (current_width >= 0) {
            int length = this_width * 3 + this.rand.nextInt(this_width + 3);
            for (int i = 0; i < length; ++i) {
                int realz;
                int realx;
                int j;
                for (j = -current_width; j <= current_width; ++j) {
                    realx = x + j * dirz + xaccum;
                    realz = z + j * dirx + zaccum;
                    if (this.isBoringBlock(world, realx, y, realz).booleanValue()) {
                        if (tree_type >= 0) {
                            this.FastSetBlock(world, realx, y, realz, ID, tree_type, 2, chunk);
                        } else {
                            this.FastSetBlock(world, realx, y, realz, ID, 0, 2, chunk);
                        }
                    }
                    if (i <= 0 || j != 0 || current_width < 3) continue;
                    if (tree_type >= 0 && this.rand.nextInt(75) == 0 || tree_type < 0 && this.rand.nextInt(50) == 0) {
                        if (bad_critters || !world.func_147437_c(realx, y + 1, realz)) continue;
                        this.FastSetBlock(world, realx, y + 1, realz, (Block)Blocks.field_150486_ae, 0, 2, chunk);
                        TileEntityChest chest = (TileEntityChest)world.func_147438_o(realx, y + 1, realz);
                        if (chest == null) continue;
                        WeightedRandomChestContent.func_76293_a((Random)this.rand, (WeightedRandomChestContent[])this.chestContentsList, (IInventory)chest, (int)(1 + this.rand.nextInt(8)));
                        continue;
                    }
                    if (this.rand.nextInt(50) != 0 || bad_critters || !world.func_147437_c(realx, y + 1, realz) || !world.func_147437_c(realx, y + 2, realz) || !world.func_147437_c(realx, y + 3, realz)) continue;
                    Entity ent = null;
                    ent = this.spawnCreature(world, 99, (double)realx + 0.5, (double)y + 1.01, (double)realz + 0.5);
                }
                if (current_width < 3 || this_width <= 1) {
                    leaf_depth = 2 + this.rand.nextInt(2);
                    leaf_width = 2 + this.rand.nextInt(3);
                    for (int n = 0; n < leaf_depth; ++n) {
                        int lw = current_width + leaf_width - n;
                        if (current_width == 0 && length - i <= 2 && lw >= length - i) {
                            lw = length - i - 1;
                        }
                        if (lw < 0) {
                            lw = 0;
                        }
                        for (j = -lw; j <= lw; ++j) {
                            realx = x + j * Math.abs(dirz) + xaccum + dirx;
                            if (!this.isBoringBlock(world, realx, y + n, realz = z + j * Math.abs(dirx) + zaccum + dirz).booleanValue()) continue;
                            if (tree_type >= 0) {
                                this.FastSetBlock(world, realx, y + n, realz, leafID, tree_type, 2, chunk);
                                if (n != 0 || tree_type != 3 || lw == 0 || j != lw && j != -lw || this.rand.nextInt(5) != 0) continue;
                                if (dirx == 0) {
                                    if (j == lw) {
                                        this.growVines(world, realx + 1, y, realz, 2, this.rand.nextInt(10), chunk);
                                        continue;
                                    }
                                    this.growVines(world, realx - 1, y, realz, 8, this.rand.nextInt(10), chunk);
                                    continue;
                                }
                                if (j == lw) {
                                    this.growVines(world, realx, y, realz + 1, 4, this.rand.nextInt(10), chunk);
                                    continue;
                                }
                                this.growVines(world, realx, y, realz - 1, 1, this.rand.nextInt(10), chunk);
                                continue;
                            }
                            Block local_leaf_type = leafID;
                            if (this.rand.nextInt(20) == 1) {
                                if (this.rand.nextInt(3) != 0) {
                                    local_leaf_type = Blocks.field_150451_bX;
                                } else {
                                    int ilt = this.rand.nextInt(4);
                                    if (ilt == 0) {
                                        local_leaf_type = OreSpawnMain.MyBlockUraniumBlock;
                                    }
                                    if (ilt == 1) {
                                        local_leaf_type = OreSpawnMain.MyBlockTitaniumBlock;
                                    }
                                    if (ilt == 2) {
                                        local_leaf_type = OreSpawnMain.MyBlockRubyBlock;
                                    }
                                    if (ilt == 3) {
                                        local_leaf_type = OreSpawnMain.MyBlockAmethystBlock;
                                    }
                                }
                            }
                            this.FastSetBlock(world, realx, y + n, realz, local_leaf_type, 0, 2, chunk);
                        }
                    }
                }
                if (current_width > 0 && last_branch > current_width && current_width != this_width && this.rand.nextInt(current_width + 1) == 0) {
                    int subdirx = branch_side;
                    int subdirz = 0;
                    if (dirx != 0) {
                        subdirx = 0;
                        subdirz = branch_side;
                    }
                    this.make_branch(world, x + xaccum + current_width * subdirx, y, z + zaccum + current_width * subdirz, current_width - 1, subdirx, subdirz, ID, leafID, tree_type, t_radius, bad_critters, chunk);
                    last_branch = 0;
                    branch_side = branch_side < 0 ? 1 : -1;
                }
                xaccum += dirx;
                zaccum += dirz;
                ++last_branch;
            }
            --current_width;
        }
    }

    public void MakeBigSquareTree(World world, int x, int y, int z, Block ID, Block leafID, Block stepID, int tree_type, int t_radius, boolean bad_critters, Chunk chunk) {
        int j;
        int i;
        int this_height = t_radius + this.rand.nextInt(t_radius);
        int this_width = t_radius;
        int base_height = t_radius * 3;
        int spiral = 0;
        int current_y = 0;
        boolean branch = false;
        boolean do_floor = false;
        int platform_looper = 1;
        int last = -1;
        int last_last = -1;
        block6: for (i = -t_radius; i <= t_radius; ++i) {
            if (this.isBoringBaseBlock(world, x + i, y, z - t_radius).booleanValue()) {
                for (j = 0; j < 20; ++j) {
                    if (y - j <= 0) continue;
                    if (!this.isBoringBaseBlock(world, x + i, y - j, z - t_radius).booleanValue()) break;
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, x + i, y - j, z - t_radius, ID, tree_type, 2, chunk);
                        continue;
                    }
                    this.FastSetBlock(world, x + i, y - j, z - t_radius, ID, 0, 2, chunk);
                }
            }
            if (this.isBoringBaseBlock(world, x + i, y, z + t_radius).booleanValue()) {
                for (j = 0; j < 20; ++j) {
                    if (y - j <= 0) continue;
                    if (!this.isBoringBaseBlock(world, x + i, y - j, z + t_radius).booleanValue()) break;
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, x + i, y - j, z + t_radius, ID, tree_type, 2, chunk);
                        continue;
                    }
                    this.FastSetBlock(world, x + i, y - j, z + t_radius, ID, 0, 2, chunk);
                }
            }
            if (this.isBoringBaseBlock(world, x - t_radius, y, z + i).booleanValue()) {
                for (j = 0; j < 20; ++j) {
                    if (y - j <= 0) continue;
                    if (!this.isBoringBaseBlock(world, x - t_radius, y - j, z + i).booleanValue()) break;
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, x - t_radius, y - j, z + i, ID, tree_type, 2, chunk);
                        continue;
                    }
                    this.FastSetBlock(world, x - t_radius, y - j, z + i, ID, 0, 2, chunk);
                }
            }
            if (!this.isBoringBaseBlock(world, x + t_radius, y, z + i).booleanValue()) continue;
            for (j = 0; j < 20; ++j) {
                if (y - j <= 0) continue;
                if (!this.isBoringBaseBlock(world, x + t_radius, y - j, z + i).booleanValue()) continue block6;
                if (tree_type >= 0) {
                    this.FastSetBlock(world, x + t_radius, y - j, z + i, ID, tree_type, 2, chunk);
                    continue;
                }
                this.FastSetBlock(world, x + t_radius, y - j, z + i, ID, 0, 2, chunk);
            }
        }
        current_y = y;
        do_floor = false;
        spiral = -this_width;
        while (this_width >= 0) {
            if (this_width != t_radius) {
                base_height = 0;
            }
            for (j = 0; j < this_height + base_height; ++j) {
                do_floor = false;
                for (i = -this_width; i <= this_width; ++i) {
                    if (this.isBoringBaseBlock(world, x + i, current_y, z - this_width).booleanValue()) {
                        if (tree_type >= 0) {
                            this.FastSetBlock(world, x + i, current_y, z - this_width, ID, tree_type, 2, chunk);
                        } else {
                            this.FastSetBlock(world, x + i, current_y, z - this_width, ID, 0, 2, chunk);
                        }
                    }
                    if (this.isBoringBaseBlock(world, x + i, current_y, z + this_width).booleanValue()) {
                        if (tree_type >= 0) {
                            this.FastSetBlock(world, x + i, current_y, z + this_width, ID, tree_type, 2, chunk);
                        } else {
                            this.FastSetBlock(world, x + i, current_y, z + this_width, ID, 0, 2, chunk);
                        }
                    }
                    if (this.isBoringBaseBlock(world, x - this_width, current_y, z + i).booleanValue()) {
                        if (tree_type >= 0) {
                            this.FastSetBlock(world, x - this_width, current_y, z + i, ID, tree_type, 2, chunk);
                        } else {
                            this.FastSetBlock(world, x - this_width, current_y, z + i, ID, 0, 2, chunk);
                        }
                    }
                    if (!this.isBoringBaseBlock(world, x + this_width, current_y, z + i).booleanValue()) continue;
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, x + this_width, current_y, z + i, ID, tree_type, 2, chunk);
                        continue;
                    }
                    this.FastSetBlock(world, x + this_width, current_y, z + i, ID, 0, 2, chunk);
                }
                if (this_width != 0 || j < this_height / 2) {
                    platform_looper = 1;
                    if (spiral == 0 && this_width >= 2 || spiral == this_width || spiral == this_width - 1 && j == this_height + base_height - 1) {
                        ++platform_looper;
                        if (spiral != 0 && this_width >= 3) {
                            ++platform_looper;
                        }
                        if (spiral == 0) {
                            do_floor = true;
                        }
                    }
                    for (int k = 0; k < platform_looper; ++k) {
                        if (this.isBoringBlock(world, x - spiral, current_y, z - this_width - 1).booleanValue()) {
                            this.FastSetBlock(world, x - spiral, current_y, z - this_width - 1, stepID, 0, 2, chunk);
                        }
                        if (this.isBoringBlock(world, x + spiral, current_y, z + this_width + 1).booleanValue()) {
                            this.FastSetBlock(world, x + spiral, current_y, z + this_width + 1, stepID, 0, 2, chunk);
                        }
                        if (this.isBoringBlock(world, x - this_width - 1, current_y, z + spiral).booleanValue()) {
                            this.FastSetBlock(world, x - this_width - 1, current_y, z + spiral, stepID, 0, 2, chunk);
                        }
                        if (this.isBoringBlock(world, x + this_width + 1, current_y, z - spiral).booleanValue()) {
                            this.FastSetBlock(world, x + this_width + 1, current_y, z - spiral, stepID, 0, 2, chunk);
                        }
                        if (this_width >= 3) {
                            if (this.isBoringBlock(world, x - spiral, current_y, z - this_width - 2).booleanValue()) {
                                this.FastSetBlock(world, x - spiral, current_y, z - this_width - 2, stepID, 0, 2, chunk);
                            }
                            if (this.isBoringBlock(world, x + spiral, current_y, z + this_width + 2).booleanValue()) {
                                this.FastSetBlock(world, x + spiral, current_y, z + this_width + 2, stepID, 0, 2, chunk);
                            }
                            if (this.isBoringBlock(world, x - this_width - 2, current_y, z + spiral).booleanValue()) {
                                this.FastSetBlock(world, x - this_width - 2, current_y, z + spiral, stepID, 0, 2, chunk);
                            }
                            if (this.isBoringBlock(world, x + this_width + 2, current_y, z - spiral).booleanValue()) {
                                this.FastSetBlock(world, x + this_width + 2, current_y, z - spiral, stepID, 0, 2, chunk);
                            }
                        }
                        if (platform_looper == 1) continue;
                        ++spiral;
                    }
                    if (do_floor) {
                        for (int m = -this_width; m <= this_width; ++m) {
                            for (int n = -this_width; n <= this_width; ++n) {
                                if (!this.isBoringBlock(world, x + m, current_y, z + n).booleanValue()) continue;
                                if (tree_type >= 0) {
                                    this.FastSetBlock(world, x + m, current_y, z + n, ID, tree_type, 2, chunk);
                                } else {
                                    this.FastSetBlock(world, x + m, current_y, z + n, ID, 0, 2, chunk);
                                }
                                if (m != 0 || n != 0 || this.rand.nextInt(2) != 0 || bad_critters || !world.func_147437_c(x, current_y + 1, z)) continue;
                                this.FastSetBlock(world, x, current_y + 1, z, (Block)Blocks.field_150486_ae, 0, 2, chunk);
                                TileEntityChest chest = (TileEntityChest)world.func_147438_o(x, current_y + 1, z);
                                if (chest == null) continue;
                                WeightedRandomChestContent.func_76293_a((Random)this.rand, (WeightedRandomChestContent[])this.chestContentsList, (IInventory)chest, (int)(t_radius - this_width + this.rand.nextInt(10)));
                            }
                        }
                    }
                }
                if (this_width != t_radius) {
                    int next = this.rand.nextInt(4 + this_width);
                    while (next == last || next == last_last) {
                        next = this.rand.nextInt(4 + this_width);
                    }
                    if (next < 4) {
                        last_last = last;
                        last = next;
                    }
                    switch (next) {
                        case 0: {
                            this.make_branch(world, x + this_width, current_y, z, this_width, 1, 0, ID, leafID, tree_type, t_radius, bad_critters, chunk);
                            break;
                        }
                        case 1: {
                            this.make_branch(world, x - this_width, current_y, z, this_width, -1, 0, ID, leafID, tree_type, t_radius, bad_critters, chunk);
                            break;
                        }
                        case 2: {
                            this.make_branch(world, x, current_y, z + this_width, this_width, 0, 1, ID, leafID, tree_type, t_radius, bad_critters, chunk);
                            break;
                        }
                        case 3: {
                            this.make_branch(world, x, current_y, z - this_width, this_width, 0, -1, ID, leafID, tree_type, t_radius, bad_critters, chunk);
                            break;
                        }
                    }
                }
                ++current_y;
                if (!do_floor) {
                    ++spiral;
                }
                if (spiral <= this_width) continue;
                spiral = -this_width;
            }
            if (Math.abs(spiral) > --this_width) {
                spiral = -this_width;
            }
            this_height += this.rand.nextInt(t_radius);
        }
        if (this.isBoringBaseBlock(world, x, current_y, z).booleanValue()) {
            Entity var8;
            this.FastSetBlock(world, x, current_y, z, Blocks.field_150475_bE, 0, 2, chunk);
            this.FastSetBlock(world, x, current_y + 1, z, Blocks.field_150475_bE, 0, 2, chunk);
            if (stepID == Blocks.field_150484_ah) {
                var8 = null;
                var8 = EntityList.func_75620_a((String)"The King", (World)world);
                if (var8 != null) {
                    var8.func_70012_b((double)x, (double)(current_y + 4), (double)z, world.field_73012_v.nextFloat() * 360.0f, 0.0f);
                    world.func_72838_d(var8);
                    ((EntityLiving)var8).func_70642_aH();
                    ((TheKing)var8).setGuardMode(1);
                }
            }
            if (stepID == OreSpawnMain.MyBlockAmethystBlock) {
                var8 = null;
                var8 = EntityList.func_75620_a((String)"The Queen", (World)world);
                if (var8 != null) {
                    var8.func_70012_b((double)x, (double)(current_y + 4), (double)z, world.field_73012_v.nextFloat() * 360.0f, 0.0f);
                    world.func_72838_d(var8);
                    ((EntityLiving)var8).func_70642_aH();
                    ((TheQueen)var8).setGuardMode(1);
                    ((TheQueen)var8).setBadMood(1);
                }
            }
        }
    }

    private void MakeCirclularBranch(World world, int iangle, int branchlen, int width, int startx, int starty, int startz, int twist, Block ID, Block leafID, int tree_type, Chunk chunk) {
        double curlen = 0.0;
        int curangle = iangle;
        double curx = startx;
        double curz = startz;
        for (curlen = 0.0; curlen < (double)branchlen; curlen += 0.5) {
            curx += 0.5 * Math.sin(Math.toRadians(curangle));
            curz += 0.5 * Math.cos(Math.toRadians(curangle));
            double tw = (double)width - (double)width * curlen / (double)branchlen;
            for (double wd = 0.0; wd <= tw; wd += 0.5) {
                double wz;
                double wx;
                int ta;
                Block id = leafID;
                if (wd < tw / 2.0) {
                    id = ID;
                }
                if (tw < 0.9) {
                    id = leafID;
                }
                if ((ta = curangle + 90) > 360) {
                    ta -= 360;
                }
                if (this.isBoringBlock(world, (int)(wx = curx + wd * Math.sin(Math.toRadians(ta))), starty, (int)(wz = curz + wd * Math.cos(Math.toRadians(ta)))).booleanValue()) {
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, (int)wx, starty, (int)wz, id, tree_type, 2, chunk);
                    } else {
                        this.FastSetBlock(world, (int)wx, starty, (int)wz, id, 0, 2, chunk);
                    }
                }
                if (id == ID && this.isBoringBlock(world, (int)wx, starty + 1, (int)wz).booleanValue()) {
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, (int)wx, starty + 1, (int)wz, leafID, tree_type, 2, chunk);
                    } else {
                        this.FastSetBlock(world, (int)wx, starty + 1, (int)wz, leafID, 0, 2, chunk);
                    }
                }
                if ((ta = curangle - 90) < 0) {
                    ta += 360;
                }
                if (this.isBoringBlock(world, (int)(wx = curx + wd * Math.sin(Math.toRadians(ta))), starty, (int)(wz = curz + wd * Math.cos(Math.toRadians(ta)))).booleanValue()) {
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, (int)wx, starty, (int)wz, id, tree_type, 2, chunk);
                    } else {
                        this.FastSetBlock(world, (int)wx, starty, (int)wz, id, 0, 2, chunk);
                    }
                }
                if (id != ID || !this.isBoringBlock(world, (int)wx, starty + 1, (int)wz).booleanValue()) continue;
                if (tree_type >= 0) {
                    this.FastSetBlock(world, (int)wx, starty + 1, (int)wz, leafID, tree_type, 2, chunk);
                    continue;
                }
                this.FastSetBlock(world, (int)wx, starty + 1, (int)wz, leafID, 0, 2, chunk);
            }
            if ((curangle += twist) < 0) {
                curangle += 360;
            }
            if (curangle < 360) continue;
            curangle -= 360;
        }
    }

    public void MakeBigCircularTree(World world, int x, int y, int z, Block ID, Block leafID, Block stepID, int tree_type, int t_radius, boolean bad_critters, Chunk chunk) {
        double dt;
        int i;
        double rad = t_radius;
        int curx = 0;
        int cury = 0;
        int curz = 0;
        int stepindex = this.rand.nextInt(360);
        int ibranch = 0;
        cury = y;
        block0: for (i = 0; i < 360; ++i) {
            dt = rad * Math.sin(Math.toRadians(i)) + 0.5;
            curx = (int)dt;
            curz = (int)(dt = rad * Math.cos(Math.toRadians(i)) + 0.5);
            if (!this.isBoringBaseBlock(world, x + curx, cury, z + curz).booleanValue()) continue;
            for (int j = 0; j < 20; ++j) {
                if (cury - j <= 0) continue;
                if (!this.isBoringBaseBlock(world, x + curx, cury - j, z + curz).booleanValue()) continue block0;
                if (tree_type >= 0) {
                    this.FastSetBlock(world, x + curx, cury - j, z + curz, ID, tree_type, 2, chunk);
                    continue;
                }
                this.FastSetBlock(world, x + curx, cury - j, z + curz, ID, 0, 2, chunk);
            }
        }
        cury = 1;
        while (rad > 0.0) {
            for (i = 0; i < 360; ++i) {
                dt = rad * Math.sin(Math.toRadians(i)) + 0.5;
                curx = (int)dt;
                curz = (int)(dt = rad * Math.cos(Math.toRadians(i)) + 0.5);
                if (this.isBoringBaseBlock(world, x + curx, y + cury, z + curz).booleanValue()) {
                    if (tree_type >= 0) {
                        this.FastSetBlock(world, x + curx, y + cury, z + curz, ID, tree_type, 2, chunk);
                    } else {
                        this.FastSetBlock(world, x + curx, y + cury, z + curz, ID, 0, 2, chunk);
                    }
                }
                if (i < stepindex - 1 || i > stepindex + 1 || !(rad > 1.0)) continue;
                dt = (rad + 1.9) * Math.sin(Math.toRadians(i)) + 0.5;
                curx = (int)dt;
                dt = (rad + 1.9) * Math.cos(Math.toRadians(i)) + 0.5;
                curz = (int)dt;
                for (int m = -1; m <= 1; ++m) {
                    for (int n = -1; n <= 1; ++n) {
                        if (!this.isBoringBaseBlock(world, x + curx + m, y + cury, z + curz + n).booleanValue()) continue;
                        this.FastSetBlock(world, x + curx + m, y + cury, z + curz + n, stepID, 0, 2, chunk);
                    }
                }
            }
            if (cury > (int)rad) {
                if ((ibranch += 80 + this.rand.nextInt(80)) > 360) {
                    ibranch -= 360;
                }
                int ibranchlen = (int)(rad * 5.0) + this.rand.nextInt((int)rad + 2);
                dt = rad * Math.sin(Math.toRadians(ibranch)) + 0.5;
                curx = (int)dt;
                dt = rad * Math.cos(Math.toRadians(ibranch)) + 0.5;
                curz = (int)dt;
                this.MakeCirclularBranch(world, ibranch, ibranchlen, (int)rad + 1, x + curx, y + cury, z + curz, this.rand.nextInt(2) * (this.rand.nextInt(2) == 0 ? -1 : 1), ID, leafID, tree_type, chunk);
            }
            if (cury % 6 == 0 && rad > 3.0) {
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
                    for (i = 0; i < 360; ++i) {
                        dt = dr * Math.sin(Math.toRadians(i)) + 0.5;
                        curx = (int)dt;
                        curz = (int)(dt = dr * Math.cos(Math.toRadians(i)) + 0.5);
                        if (!this.isBoringBaseBlock(world, x + curx, y + cury, z + curz).booleanValue()) continue;
                        if (tree_type >= 0) {
                            this.FastSetBlock(world, x + curx, y + cury, z + curz, ID, tree_type, 2, chunk);
                            continue;
                        }
                        this.FastSetBlock(world, x + curx, y + cury, z + curz, ID, 0, 2, chunk);
                    }
                }
                if (this.rand.nextInt(2) == 0 && !bad_critters && world.func_147437_c(x, y + cury + 1, z)) {
                    this.FastSetBlock(world, x, y + cury + 1, z, (Block)Blocks.field_150486_ae, 0, 2, chunk);
                    TileEntityChest chest = (TileEntityChest)world.func_147438_o(x, y + cury + 1, z);
                    if (chest != null) {
                        WeightedRandomChestContent.func_76293_a((Random)this.rand, (WeightedRandomChestContent[])this.chestContentsList, (IInventory)chest, (int)(t_radius - (int)rad + this.rand.nextInt(10)));
                    }
                }
            }
            if ((stepindex += 15 + (int)(((double)t_radius - rad) * 3.0)) > 360) {
                stepindex -= 360;
            }
            if (!((rad -= 0.01 * (double)this.rand.nextInt(15)) <= 0.0) || !this.isBoringBaseBlock(world, x, y + ++cury, z).booleanValue()) continue;
            this.FastSetBlock(world, x, y + cury, z, Blocks.field_150484_ah, 0, 2, chunk);
        }
    }

    public void MakeBigRoundTree(World world, int inx, int y, int inz, Block ID, Block leafID, Block stepID, int tree_type, int t_radius, Chunk chunk) {
        float fcurz;
        float fcurx;
        double dt;
        int i;
        double rad = t_radius;
        int cury = 0;
        int ibranch = 0;
        float fx = inx;
        fx += 0.5f;
        float fz = inz;
        fz += 0.5f;
        cury = y;
        block0: for (i = 0; i < 360; ++i) {
            dt = rad * Math.sin(Math.toRadians(i));
            fcurx = (float)dt;
            fcurz = (float)(dt = rad * Math.cos(Math.toRadians(i)));
            if (!this.isBoringBaseBlock(world, (int)(fx + fcurx), cury, (int)(fz + fcurz)).booleanValue()) continue;
            for (int j = 0; j < 20; ++j) {
                if (cury - j <= 0) continue;
                if (!this.isBoringBaseBlock(world, (int)(fx + fcurx), cury - j, (int)(fz + fcurz)).booleanValue()) continue block0;
                if (tree_type >= 0) {
                    this.FastSetBlock(world, (int)(fx + fcurx), cury - j, (int)(fz + fcurz), ID, tree_type, 2, chunk);
                    continue;
                }
                this.FastSetBlock(world, (int)(fx + fcurx), cury - j, (int)(fz + fcurz), ID, 0, 2, chunk);
            }
        }
        cury = 1;
        while (rad > 0.0) {
            for (i = 0; i < 360; ++i) {
                dt = rad * Math.sin(Math.toRadians(i));
                fcurx = (float)dt;
                fcurz = (float)(dt = rad * Math.cos(Math.toRadians(i)));
                if (!this.isBoringBaseBlock(world, (int)(fx + fcurx), y + cury, (int)(fz + fcurz)).booleanValue()) continue;
                if (tree_type >= 0) {
                    this.FastSetBlock(world, (int)(fx + fcurx), y + cury, (int)(fz + fcurz), ID, tree_type, 2, chunk);
                    continue;
                }
                this.FastSetBlock(world, (int)(fx + fcurx), y + cury, (int)(fz + fcurz), ID, 0, 2, chunk);
            }
            if (cury > (int)rad) {
                if ((ibranch += 80 + world.field_73012_v.nextInt(80)) > 360) {
                    ibranch -= 360;
                }
                int ibranchlen = (int)(rad * 5.0) + world.field_73012_v.nextInt((int)rad + 2);
                dt = rad * Math.sin(Math.toRadians(ibranch));
                fcurx = (float)dt;
                dt = rad * Math.cos(Math.toRadians(ibranch));
                fcurz = (float)dt;
                this.MakeRoundBranch(world, ibranch, ibranchlen, (int)rad + 1, fx + fcurx, y + cury, fz + fcurz, ID, leafID, tree_type, chunk);
            }
            if (cury % 6 == 0 && rad > 3.0) {
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
                    for (i = 0; i < 360; ++i) {
                        dt = dr * Math.sin(Math.toRadians(i));
                        fcurx = (float)dt;
                        fcurz = (float)(dt = dr * Math.cos(Math.toRadians(i)));
                        if (!this.isBoringBaseBlock(world, (int)(fx + fcurx), y + cury, (int)(fz + fcurz)).booleanValue()) continue;
                        if (tree_type >= 0) {
                            this.FastSetBlock(world, (int)(fx + fcurx), y + cury, (int)(fz + fcurz), ID, tree_type, 2, chunk);
                            continue;
                        }
                        this.FastSetBlock(world, (int)(fx + fcurx), y + cury, (int)(fz + fcurz), ID, 0, 2, chunk);
                    }
                }
            }
            if (!((rad -= 0.01 * (double)world.field_73012_v.nextInt(15)) <= 0.0) || !this.isBoringBaseBlock(world, (int)fx, y + ++cury, (int)fz).booleanValue()) continue;
            this.FastSetBlock(world, (int)fx, y + cury, (int)fz, Blocks.field_150484_ah, 0, 2, chunk);
        }
    }

    private void MakeRoundBranch(World world, int iangle, int branchlen, int width, float startx, int starty, float startz, Block ID, Block leafID, int tree_type, Chunk chunk) {
        double deltadir = 0.06283185200000001;
        double deltamag = 0.35f;
        int ixlast = 0;
        int izlast = 0;
        boolean xoff = false;
        boolean zoff = false;
        int radius = branchlen / 2;
        float centerx = (float)((double)startx + (double)radius * Math.sin(Math.toRadians(iangle)));
        float centerz = (float)((double)startz + (double)radius * Math.cos(Math.toRadians(iangle)));
        izlast = 0;
        ixlast = 0;
        for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
            for (double h = 0.75; h < (double)radius; h += deltamag) {
                int ix = (int)((double)centerx + Math.cos(curdir) * h);
                int iz = (int)((double)centerz + Math.sin(curdir) * h);
                if (ix == ixlast && iz == izlast) continue;
                ixlast = ix;
                izlast = iz;
                Block id = ID;
                if ((double)radius - h < 2.0) {
                    id = leafID;
                }
                if (!this.isBoringBlock(world, ix, starty, iz).booleanValue()) continue;
                this.FastSetBlock(world, ix, starty, iz, id, tree_type, 2, chunk);
            }
        }
    }

    public void FastSetBlock(World world, int ix, int iy, int iz, Block id, int im, int iflg, Chunk chunk) {
        OreSpawnMain.setBlockSuperFast(world, ix, iy, iz, id, im, 2, chunk);
    }

    public boolean func_77648_a(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int clickedX, int clickedY, int clickedZ, int par7, float par8, float par9, float par10) {
        Block var1 = world.func_147439_a(clickedX, clickedY, clickedZ);
        if (var1 != Blocks.field_150349_c && var1 != Blocks.field_150458_ak && var1 != Blocks.field_150346_d) {
            return false;
        }
        int tree_type = this.rand.nextInt(4);
        BlockLeaves leaf_type = Blocks.field_150362_t;
        this.no_critters = true;
        if (this.rand.nextInt(2) == 1) {
            this.no_critters = false;
        }
        if (!world.field_72995_K) {
            world.func_147465_d(clickedX, clickedY, clickedZ, Blocks.field_150340_R, 0, 2);
        }
        for (int var3 = 0; var3 < 6; ++var3) {
            par2EntityPlayer.field_70170_p.func_72869_a("largesmoke", (double)((float)clickedX + 0.5f), (double)((float)(clickedY + 1) + 0.25f), (double)((float)clickedZ + 0.5f), 0.0, 0.0, 0.0);
            par2EntityPlayer.field_70170_p.func_72869_a("largeexplode", (double)((float)clickedX + 0.5f), (double)((float)(clickedY + 1) + 0.25f), (double)((float)clickedZ + 0.5f), 0.0, 0.0, 0.0);
            par2EntityPlayer.field_70170_p.func_72869_a("reddust", (double)((float)clickedX + 0.5f), (double)((float)(clickedY + 1) + 0.25f), (double)((float)clickedZ + 0.5f), 0.0, 0.0, 0.0);
        }
        par2EntityPlayer.field_70170_p.func_72956_a((Entity)par2EntityPlayer, "random.explode", 2.8f, 1.5f);
        if (!world.field_72995_K) {
            int rand_treetype = this.rand.nextInt(100);
            if (rand_treetype >= 20) {
                if (rand_treetype >= 40) {
                    if (tree_type != 3 && this.rand.nextInt(10) == 1) {
                        leaf_type = OreSpawnMain.MyAppleLeaves;
                    }
                    this.MakeBigSquareTree(world, clickedX, clickedY, clickedZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150341_Y, tree_type, this.tree_radius, this.no_critters, null);
                } else {
                    this.MakeBigRoundTree(world, clickedX, clickedY, clickedZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150341_Y, tree_type, this.tree_radius, null);
                }
            } else if (rand_treetype == 1) {
                if (OreSpawnMain.GinormousEmeraldTreeEnable != 0) {
                    if (this.rand.nextInt(2) == 0) {
                        this.MakeBigSquareTree(world, clickedX, clickedY, clickedZ, Blocks.field_150340_R, Blocks.field_150475_bE, Blocks.field_150484_ah, -1, this.tree_radius, true, null);
                    } else {
                        this.MakeBigSquareTree(world, clickedX, clickedY, clickedZ, Blocks.field_150343_Z, OreSpawnMain.MyBlockRubyBlock, OreSpawnMain.MyBlockAmethystBlock, -1, this.tree_radius, true, null);
                    }
                } else {
                    this.MakeBigSquareTree(world, clickedX, clickedY, clickedZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150366_p, tree_type, this.tree_radius, this.no_critters, null);
                }
            } else {
                this.MakeBigCircularTree(world, clickedX, clickedY, clickedZ, Blocks.field_150364_r, (Block)leaf_type, Blocks.field_150341_Y, tree_type, this.tree_radius, this.no_critters, null);
            }
        }
        if (!par2EntityPlayer.field_71075_bZ.field_75098_d) {
            --par1ItemStack.field_77994_a;
        }
        return true;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_94581_a(IIconRegister iconRegister) {
        this.field_77791_bV = iconRegister.func_94245_a("OreSpawn:" + this.func_77658_a().substring(5));
    }
}

