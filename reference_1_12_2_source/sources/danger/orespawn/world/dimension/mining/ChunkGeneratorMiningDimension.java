/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockFalling
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.EnumCreatureType
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$MutableBlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.World
 *  net.minecraft.world.WorldType
 *  net.minecraft.world.biome.Biome
 *  net.minecraft.world.biome.Biome$SpawnListEntry
 *  net.minecraft.world.biome.BiomeProvider
 *  net.minecraft.world.chunk.Chunk
 *  net.minecraft.world.chunk.ChunkPrimer
 *  net.minecraft.world.gen.IChunkGenerator
 *  net.minecraft.world.gen.MapGenBase
 *  net.minecraft.world.gen.MapGenCaves
 *  net.minecraft.world.gen.NoiseGeneratorOctaves
 *  net.minecraft.world.gen.NoiseGeneratorPerlin
 *  net.minecraftforge.event.ForgeEventFactory
 *  net.minecraftforge.event.terraingen.InitMapGenEvent$EventType
 *  net.minecraftforge.event.terraingen.InitNoiseGensEvent$Context
 *  net.minecraftforge.event.terraingen.InitNoiseGensEvent$ContextOverworld
 *  net.minecraftforge.event.terraingen.TerrainGen
 */
package danger.orespawn.world.dimension.mining;

import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkGeneratorMiningDimension
implements IChunkGenerator {
    private Random rand;
    private World world;
    private final BiomeProvider biomeProvider;
    private final WorldType terrainType;
    private final double[] heightMap;
    private final float[] parabolicField;
    public NoiseGeneratorOctaves scale;
    public NoiseGeneratorOctaves depth;
    public NoiseGeneratorOctaves forest;
    private NoiseGeneratorOctaves field_185991_j;
    private NoiseGeneratorOctaves field_185992_k;
    private NoiseGeneratorOctaves field_185993_l;
    private NoiseGeneratorPerlin height;
    private MapGenBase caveGenerator;
    private double[] stoneNoise;
    private double[] field_186002_u = new double[256];
    private Biome[] biomesForGeneration;
    double[] field_185986_e;
    double[] field_185987_f;
    double[] field_185988_g;
    double[] field_185989_h;

    public ChunkGeneratorMiningDimension(World worldIn, long seed, BiomeProvider bp) {
        this.world = worldIn;
        this.terrainType = worldIn.func_72912_H().func_76067_t();
        this.rand = new Random(seed);
        this.stoneNoise = new double[256];
        this.biomeProvider = bp;
        this.scale = new NoiseGeneratorOctaves(this.rand, 10);
        this.depth = new NoiseGeneratorOctaves(this.rand, 16);
        this.forest = new NoiseGeneratorOctaves(this.rand, 8);
        this.field_185991_j = new NoiseGeneratorOctaves(this.rand, 16);
        this.field_185992_k = new NoiseGeneratorOctaves(this.rand, 16);
        this.field_185993_l = new NoiseGeneratorOctaves(this.rand, 8);
        this.height = new NoiseGeneratorPerlin(this.rand, 4);
        this.heightMap = new double[825];
        this.parabolicField = new float[25];
        this.caveGenerator = TerrainGen.getModdedMapGen((MapGenBase)new MapGenCaves(), (InitMapGenEvent.EventType)InitMapGenEvent.EventType.CAVE);
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                float f;
                this.parabolicField[i + 2 + (j + 2) * 5] = f = 10.0f / MathHelper.func_76129_c((float)((float)(i * i + j * j) + 0.2f));
            }
        }
        InitNoiseGensEvent.ContextOverworld ctx = new InitNoiseGensEvent.ContextOverworld(this.field_185991_j, this.field_185992_k, this.field_185993_l, this.height, this.scale, this.depth, this.forest);
        ctx = (InitNoiseGensEvent.ContextOverworld)TerrainGen.getModdedNoiseGenerators((World)worldIn, (Random)this.rand, (InitNoiseGensEvent.Context)ctx);
        this.field_185991_j = ctx.getLPerlin1();
        this.field_185992_k = ctx.getLPerlin2();
        this.field_185993_l = ctx.getPerlin();
        this.height = ctx.getHeight();
        this.scale = ctx.getScale();
        this.depth = ctx.getDepth();
        this.forest = ctx.getForest();
    }

    public Chunk func_185932_a(int x, int z) {
        this.rand.setSeed((long)x * 341873128712L + (long)z * 132897987541L);
        ChunkPrimer chunkPrimer = new ChunkPrimer();
        this.setBlocksInChunk(x, z, chunkPrimer);
        this.biomesForGeneration = this.biomeProvider.func_76933_b(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.replaceBiomeBlocks(x, z, chunkPrimer, this.biomesForGeneration);
        this.caveGenerator.func_186125_a(this.world, x, z, chunkPrimer);
        Chunk chunk = new Chunk(this.world, chunkPrimer, x, z);
        byte[] abyte = chunk.func_76605_m();
        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte)Biome.func_185362_a((Biome)this.biomesForGeneration[i]);
        }
        chunk.func_76603_b();
        return chunk;
    }

    private void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomesIn) {
        if (!ForgeEventFactory.onReplaceBiomeBlocks((IChunkGenerator)this, (int)x, (int)z, (ChunkPrimer)primer, (World)this.world)) {
            return;
        }
        double d0 = 0.03125;
        this.field_186002_u = this.height.func_151599_a(this.field_186002_u, (double)(x * 16), (double)(z * 16), 16, 16, d0 * 2.0, d0 * 2.0, 1.0);
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                Biome Biome2 = biomesIn[j + i * 16];
                this.generateBiomeTerrain(this.world, this.rand, primer, x * 16 + i, z * 16 + j, this.field_186002_u[j + i * 16], Biome2);
            }
        }
    }

    private void generateBiomeTerrain(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal, Biome biome) {
        int seaLevel = 63;
        IBlockState topBlock = biome.field_76752_A;
        IBlockState fillerBlock = biome.field_76753_B;
        int j = -1;
        int k = (int)(noiseVal / 3.0 + 3.0 + rand.nextDouble() * 0.25);
        int l = x & 0xF;
        int i1 = z & 0xF;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int y = 255; y >= 0; --y) {
            if (y == 0) {
                chunkPrimerIn.func_177855_a(i1, y, l, Blocks.field_150357_h.func_176223_P());
                continue;
            }
            IBlockState iblockstate2 = chunkPrimerIn.func_177856_a(i1, y, l);
            if (iblockstate2.func_185904_a() == Material.field_151579_a) {
                j = -1;
                continue;
            }
            if (iblockstate2.func_177230_c() != Blocks.field_150348_b) continue;
            if (j == -1) {
                if (k <= 0) {
                    topBlock = Blocks.field_150350_a.func_176223_P();
                    fillerBlock = Blocks.field_150348_b.func_176223_P();
                } else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
                    topBlock = biome.field_76752_A;
                    fillerBlock = biome.field_76753_B;
                }
                if (y < seaLevel && (topBlock == null || topBlock.func_185904_a() == Material.field_151579_a)) {
                    fillerBlock = Blocks.field_150355_j.func_176223_P();
                }
                j = k;
                if (y >= seaLevel - 1) {
                    chunkPrimerIn.func_177855_a(i1, y, l, topBlock);
                    continue;
                }
                if (y < seaLevel - 7 - k) {
                    topBlock = Blocks.field_150350_a.func_176223_P();
                    fillerBlock = Blocks.field_150348_b.func_176223_P();
                    chunkPrimerIn.func_177855_a(i1, y, l, Blocks.field_150351_n.func_176223_P());
                    continue;
                }
                chunkPrimerIn.func_177855_a(i1, y, l, fillerBlock);
                continue;
            }
            if (j <= 0) continue;
            chunkPrimerIn.func_177855_a(i1, y, l, fillerBlock);
            if (--j != 0 || fillerBlock.func_177230_c() != Blocks.field_150354_m) continue;
            j = rand.nextInt(4);
            fillerBlock = Blocks.field_150348_b.func_176223_P();
        }
    }

    private void setBlocksInChunk(int x, int z, ChunkPrimer chunkPrimer) {
        this.biomesForGeneration = this.biomeProvider.func_76937_a(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        this.generateHeightmap(x * 4, 0, z * 4);
        for (int i = 0; i < 4; ++i) {
            int j = i * 5;
            int k = (i + 1) * 5;
            for (int l = 0; l < 4; ++l) {
                int i1 = (j + l) * 33;
                int j1 = (j + l + 1) * 33;
                int k1 = (k + l) * 33;
                int l1 = (k + l + 1) * 33;
                for (int i2 = 0; i2 < 32; ++i2) {
                    double d0 = 0.125;
                    double d1 = this.heightMap[i1 + i2];
                    double d2 = this.heightMap[j1 + i2];
                    double d3 = this.heightMap[k1 + i2];
                    double d4 = this.heightMap[l1 + i2];
                    double d5 = (this.heightMap[i1 + i2 + 1] - d1) * 0.125;
                    double d6 = (this.heightMap[j1 + i2 + 1] - d2) * 0.125;
                    double d7 = (this.heightMap[k1 + i2 + 1] - d3) * 0.125;
                    double d8 = (this.heightMap[l1 + i2 + 1] - d4) * 0.125;
                    for (int j2 = 0; j2 < 8; ++j2) {
                        double d9 = 0.25;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25;
                        double d13 = (d4 - d2) * 0.25;
                        for (int k2 = 0; k2 < 4; ++k2) {
                            double d14 = 0.25;
                            double d16 = (d11 - d10) * 0.25;
                            double lvt_45_1_ = d10 - d16;
                            for (int l2 = 0; l2 < 4; ++l2) {
                                double d;
                                lvt_45_1_ += d16;
                                if (d > 0.0) {
                                    chunkPrimer.func_177855_a(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, Blocks.field_150348_b.func_176223_P());
                                    continue;
                                }
                                if (i2 * 8 + j2 >= 63) continue;
                                chunkPrimer.func_177855_a(i * 4 + k2, i2 * 8 + j2, l * 4 + l2, Blocks.field_150355_j.func_176223_P());
                            }
                            d10 += d12;
                            d11 += d13;
                        }
                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    private void generateHeightmap(int x, int y, int z) {
        this.field_185989_h = this.depth.func_76305_a(this.field_185989_h, x, z, 5, 5, 200.0, 200.0, 0.5);
        float coordScale = 684.412f;
        float heightScale = 684.412f;
        this.field_185986_e = this.field_185993_l.func_76304_a(this.field_185986_e, x, y, z, 5, 33, 5, 8.55515, 4.277575, 8.55515);
        this.field_185987_f = this.field_185991_j.func_76304_a(this.field_185987_f, x, y, z, 5, 33, 5, (double)coordScale, (double)heightScale, (double)coordScale);
        this.field_185988_g = this.field_185992_k.func_76304_a(this.field_185988_g, x, y, z, 5, 33, 5, (double)coordScale, (double)heightScale, (double)coordScale);
        int i = 0;
        int j = 0;
        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float f2 = 0.0f;
                float f3 = 0.0f;
                float f4 = 0.0f;
                int i1 = 2;
                Biome surroundingBiome = this.biomesForGeneration[k + 2 + (l + 2) * 10];
                for (int j1 = -i1; j1 < i1; ++j1) {
                    for (int k1 = -i1; k1 <= i1; ++k1) {
                        Biome biome = this.biomesForGeneration[k + j1 + 2 + (l + k1 + 2) * 10];
                        float baseHeight = biome.func_185355_j();
                        float heightVariation = biome.func_185360_m();
                        float f7 = this.parabolicField[j1 + 2 + (k1 + 2) * 5] / (baseHeight + 2.0f);
                        if (biome.func_185355_j() > surroundingBiome.func_185355_j()) {
                            f7 /= 2.0f;
                        }
                        f2 += heightVariation * f7;
                        f3 += baseHeight * f7;
                        f4 += f7;
                    }
                }
                f2 /= f4;
                f3 /= f4;
                f2 = f2 * 0.9f + 0.1f;
                f3 = (f3 * 4.0f - 1.0f) / 8.0f;
                double d7 = this.field_185989_h[j] / 8000.0;
                if (d7 < 0.0) {
                    d7 = -d7 * 0.3;
                }
                if (d7 < 0.0) {
                    if ((d7 /= 2.0) < -1.0) {
                        d7 = -1.0;
                    }
                    d7 /= 1.4;
                    d7 /= 2.0;
                } else {
                    if (d7 > 1.0) {
                        d7 = 1.0;
                    }
                    d7 /= 8.0;
                }
                ++j;
                double d8 = f3;
                double d9 = f2;
                d8 += d7 * 0.2;
                d8 = d8 * 8.5 / 8.0;
                double d0 = 8.5 + d8 * 4.0;
                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = ((double)l1 - d0) * 12.0 * 128.0 / 256.0 / d9;
                    if (d1 < 0.0) {
                        d1 *= 4.0;
                    }
                    double d2 = this.field_185987_f[i] / 512.0;
                    double d3 = this.field_185988_g[i] / 512.0;
                    double d4 = (this.field_185986_e[i] / 10.0 + 1.0) / 2.0;
                    double d5 = MathHelper.func_151238_b((double)d2, (double)d3, (double)d4) - d1;
                    if (l1 > 29) {
                        double d6 = (float)(l1 - 29) / 3.0f;
                        d5 = d5 * (1.0 - d6) + -10.0 * d6;
                    }
                    this.heightMap[i] = d5;
                    ++i;
                }
            }
        }
    }

    public boolean func_185933_a(Chunk arg0, int arg1, int arg2) {
        return false;
    }

    public BlockPos func_180513_a(World arg0, String arg1, BlockPos arg2, boolean arg3) {
        return null;
    }

    public List<Biome.SpawnListEntry> func_177458_a(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = this.world.func_180494_b(pos);
        return biome.func_76747_a(creatureType);
    }

    public boolean func_193414_a(World arg0, String arg1, BlockPos arg2) {
        return false;
    }

    public void func_185931_b(int x, int z) {
        BlockFalling.field_149832_M = true;
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.func_180494_b(blockpos.func_177982_a(16, 0, 16));
        ForgeEventFactory.onChunkPopulate((boolean)true, (IChunkGenerator)this, (World)this.world, (Random)this.rand, (int)x, (int)z, (boolean)false);
        biome.func_180624_a(this.world, this.rand, blockpos);
        ForgeEventFactory.onChunkPopulate((boolean)false, (IChunkGenerator)this, (World)this.world, (Random)this.rand, (int)x, (int)z, (boolean)false);
        BlockFalling.field_149832_M = false;
    }

    public void func_180514_a(Chunk arg0, int arg1, int arg2) {
    }
}

