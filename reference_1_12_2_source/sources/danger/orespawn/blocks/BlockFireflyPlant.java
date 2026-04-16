/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockCrops
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.util.math.AxisAlignedBB
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.Chunk
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.Firefly;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BlockFireflyPlant
extends BlockCrops {
    private boolean shouldSpawn = true;

    public BlockFireflyPlant() {
        this.func_149663_c("firefly_plant");
        this.setRegistryName("firefly_plant");
        this.func_180632_j(this.func_176223_P().func_177226_a((IProperty)field_176488_a, (Comparable)Integer.valueOf(0)));
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        ModBlocks.BLOCKS.add((Block)this);
    }

    public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.func_180650_b(worldIn, pos, state, rand);
        if (worldIn.func_72896_J()) {
            return;
        }
        Chunk chunk = worldIn.func_175726_f(pos);
        float radius = 50.0f;
        AxisAlignedBB aabb = new AxisAlignedBB((double)((float)pos.func_177958_n() - radius), 0.0, (double)((float)pos.func_177952_p() - radius), (double)((float)pos.func_177958_n() + radius), 200.0, (double)((float)pos.func_177952_p() + radius));
        ArrayList fireflyList = new ArrayList();
        chunk.func_177430_a(Firefly.class, aabb, fireflyList, e -> true);
        if (fireflyList.size() > 15) {
            return;
        }
        IBlockState st = worldIn.func_180495_p(pos);
        int rate = st.func_177230_c().func_176201_c(st);
        rate &= 7;
        if ((rate = 6 - rate) > 1 && OreSpawnMain.OreSpawnRand.nextInt(rate) != 0) {
            return;
        }
        if (worldIn.func_180495_p(pos.func_177981_b(1)).func_177230_c() == Blocks.field_150350_a && !worldIn.func_72935_r()) {
            Entity firefly = EntityList.func_191304_a(Firefly.class, (World)worldIn);
            firefly.func_70107_b((double)pos.func_177958_n(), (double)(pos.func_177956_o() + 1), (double)pos.func_177952_p());
            worldIn.func_72838_d(firefly);
        }
    }

    public Item func_180660_a(IBlockState state, Random rand, int fortune) {
        return ModItems.FIREFLY_SEED;
    }

    protected Item func_149866_i() {
        return ModItems.FIREFLY_SEED;
    }

    protected Item func_149865_P() {
        return ModItems.FIREFLY_SEED;
    }
}

