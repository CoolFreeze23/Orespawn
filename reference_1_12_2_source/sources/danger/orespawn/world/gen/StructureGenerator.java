/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.IChunkProvider
 *  net.minecraft.world.gen.IChunkGenerator
 *  net.minecraftforge.fml.common.IWorldGenerator
 */
package danger.orespawn.world.gen;

import danger.orespawn.init.ModDimensions;
import danger.orespawn.world.structures.GenericDungeon;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class StructureGenerator
implements IWorldGenerator {
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        GenericDungeon dungeon = new GenericDungeon();
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = 5 + random.nextInt(40);
        if (world.field_73011_w.func_186058_p() == ModDimensions.MINING && random.nextInt(16) == 0) {
            dungeon.makeDungeon(world, x, y, z);
        }
    }

    private static int calculateGenerationHeight(World world, int x, int z, Block topBlock) {
        int y = world.func_72800_K();
        boolean foundGround = false;
        while (!foundGround && y-- >= 0) {
            Block block = world.func_180495_p(new BlockPos(x, y, z)).func_177230_c();
            foundGround = block == topBlock;
        }
        return y;
    }
}

