/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.util.math.AxisAlignedBB
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.Chunk
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.blocks.AntType;
import danger.orespawn.entity.RedAnt;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BlockAnt
extends Block {
    private AntType ANT_TYPE = AntType.RED;

    public BlockAnt() {
        super(Material.field_151577_b);
        this.func_149663_c("ant_block");
        this.setRegistryName("ant_block");
        this.func_149675_a(true);
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add((Item)new ItemBlock((Block)this).setRegistryName(Objects.requireNonNull(this.getRegistryName())));
    }

    public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.field_72995_K) {
            return;
        }
        super.func_180650_b(worldIn, pos, state, rand);
        if (worldIn.func_180495_p(pos.func_177981_b(1)).func_177230_c() == Blocks.field_150350_a && worldIn.func_72935_r()) {
            Chunk chunk = worldIn.func_175726_f(pos);
            int radius = 16;
            AxisAlignedBB aabb = new AxisAlignedBB((double)(pos.func_177958_n() - radius), 0.0, (double)(pos.func_177952_p() - radius), (double)(pos.func_177958_n() + radius), 200.0, (double)(pos.func_177952_p() + radius));
            ArrayList antList = new ArrayList();
            chunk.func_177430_a(RedAnt.class, aabb, antList, e -> true);
            if (antList.size() > 20) {
                return;
            }
            int howmany = OreSpawnMain.OreSpawnRand.nextInt(6) + 2;
            for (int i = 0; i < howmany; ++i) {
                Entity ant = EntityList.func_191304_a(this.getClassFromAntType(this.ANT_TYPE), (World)worldIn);
                ant.func_70080_a((double)pos.func_177958_n(), (double)(pos.func_177956_o() + 1), (double)pos.func_177952_p(), rand.nextFloat() * 360.0f, 0.0f);
                worldIn.func_72838_d(ant);
                ((EntityLiving)ant).func_70642_aH();
            }
        }
    }

    private Class<? extends Entity> getClassFromAntType(AntType type) {
        switch (type) {
            case RED: {
                return RedAnt.class;
            }
        }
        return RedAnt.class;
    }
}

