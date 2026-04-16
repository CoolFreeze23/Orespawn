/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.IChunkProvider
 *  net.minecraft.world.gen.IChunkGenerator
 *  net.minecraft.world.gen.feature.WorldGenMinable
 *  net.minecraftforge.fml.common.IWorldGenerator
 */
package danger.orespawn.world.gen.ores;

import danger.orespawn.init.ModBlocks;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenOres
implements IWorldGenerator {
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }

    private void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        this.generateOre(ModBlocks.URANIUM_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 3, 20, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.TITANIUM_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 3, 20, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.ALOSAURUS_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.BARYONYX_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.CAMARASAURUS_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.CRYOLOPHOSAURUS_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.POINTYSAURUS_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.TREX_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.BIRD_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.COW_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.CREEPER_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.GHAST_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.HORSE_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.PIG_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.ZOMBIE_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.ALIEN_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.CAVEFISHER_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.NASTYSAURUS_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.VELOCITYRAPTOR_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.WTF_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.SPYRO_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.DRAGONFLY_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.BEAVER_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.BRUTALFLY_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.MOTHRA_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.MANTIS_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.KYUUBI_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.CASSOWARY_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.REDCOW_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.STINKBUG_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 40, 80, random.nextInt(5) + 5, 3);
        this.generateOre(ModBlocks.AMETHYST_ORE.func_176223_P(), world, random, chunkX * 16, chunkZ * 16, 3, 15, random.nextInt(4) + 4, 1);
    }

    private void generateOre(IBlockState blockState, World world, Random random, int x, int z, int minY, int maxY, int size, int chances) {
        int deltaY = maxY - minY;
        for (int i = 0; i < chances; ++i) {
            BlockPos pos = new BlockPos(x + random.nextInt(16), minY + random.nextInt(deltaY), z + random.nextInt(16));
            WorldGenMinable generator = new WorldGenMinable(blockState, size);
            generator.func_180709_b(world, random, pos);
        }
    }
}

