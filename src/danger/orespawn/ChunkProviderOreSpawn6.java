/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockFalling
 *  net.minecraft.block.BlockGrass
 *  net.minecraft.block.material.Material
 *  net.minecraft.entity.EnumCreatureType
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.IProgressUpdate
 *  net.minecraft.world.ChunkPosition
 *  net.minecraft.world.SpawnerAnimals
 *  net.minecraft.world.World
 *  net.minecraft.world.biome.BiomeGenBase
 *  net.minecraft.world.chunk.Chunk
 *  net.minecraft.world.chunk.IChunkProvider
 *  net.minecraft.world.gen.NoiseGenerator
 *  net.minecraft.world.gen.NoiseGeneratorOctaves
 *  net.minecraftforge.event.terraingen.TerrainGen
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkProviderOreSpawn6
implements IChunkProvider {
    private Random hellRNG;
    private Random random;
    private NoiseGeneratorOctaves netherNoiseGen1;
    private NoiseGeneratorOctaves netherNoiseGen2;
    private NoiseGeneratorOctaves netherNoiseGen3;
    private NoiseGeneratorOctaves slowsandGravelNoiseGen;
    private NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
    public NoiseGeneratorOctaves netherNoiseGen6;
    public NoiseGeneratorOctaves netherNoiseGen7;
    private World worldObj;
    private double[] noiseField;
    private double[] slowsandNoise = new double[256];
    private double[] gravelNoise = new double[256];
    private double[] netherrackExclusivityNoise = new double[256];
    double[] noiseData1;
    double[] noiseData2;
    double[] noiseData3;
    double[] noiseData4;
    double[] noiseData5;
    private static final String __OBFID = "CL_00000392";

    public ChunkProviderOreSpawn6(World p_i2005_1_, long p_i2005_2_) {
        this.worldObj = p_i2005_1_;
        this.hellRNG = new Random(p_i2005_2_);
        this.random = new Random(p_i2005_2_);
        this.netherNoiseGen1 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        this.netherNoiseGen2 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        this.netherNoiseGen3 = new NoiseGeneratorOctaves(this.hellRNG, 8);
        this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(this.hellRNG, 4);
        this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(this.hellRNG, 4);
        this.netherNoiseGen6 = new NoiseGeneratorOctaves(this.hellRNG, 10);
        this.netherNoiseGen7 = new NoiseGeneratorOctaves(this.hellRNG, 16);
        NoiseGenerator[] noiseGens = new NoiseGenerator[]{this.netherNoiseGen1, this.netherNoiseGen2, this.netherNoiseGen3, this.slowsandGravelNoiseGen, this.netherrackExculsivityNoiseGen, this.netherNoiseGen6, this.netherNoiseGen7};
        noiseGens = TerrainGen.getModdedNoiseGenerators((World)p_i2005_1_, (Random)this.hellRNG, (NoiseGenerator[])noiseGens);
        this.netherNoiseGen1 = (NoiseGeneratorOctaves)noiseGens[0];
        this.netherNoiseGen2 = (NoiseGeneratorOctaves)noiseGens[1];
        this.netherNoiseGen3 = (NoiseGeneratorOctaves)noiseGens[2];
        this.slowsandGravelNoiseGen = (NoiseGeneratorOctaves)noiseGens[3];
        this.netherrackExculsivityNoiseGen = (NoiseGeneratorOctaves)noiseGens[4];
        this.netherNoiseGen6 = (NoiseGeneratorOctaves)noiseGens[5];
        this.netherNoiseGen7 = (NoiseGeneratorOctaves)noiseGens[6];
    }

    public void func_147419_a(int p_147419_1_, int p_147419_2_, Block[] p_147419_3_) {
        int b0 = 4;
        int b1 = 32;
        int k = b0 + 1;
        int b2 = 17;
        int l = b0 + 1;
        Block block = Blocks.field_150348_b;
        this.noiseField = this.initializeNoiseField(this.noiseField, p_147419_1_ * b0, 0, p_147419_2_ * b0, k, b2, l);
        for (int i1 = 0; i1 < b0; ++i1) {
            for (int j1 = 0; j1 < b0; ++j1) {
                for (int k1 = 0; k1 < 16; ++k1) {
                    double d0 = 0.125;
                    double d1 = this.noiseField[((i1 + 0) * l + j1 + 0) * b2 + k1 + 0];
                    double d2 = this.noiseField[((i1 + 0) * l + j1 + 1) * b2 + k1 + 0];
                    double d3 = this.noiseField[((i1 + 1) * l + j1 + 0) * b2 + k1 + 0];
                    double d4 = this.noiseField[((i1 + 1) * l + j1 + 1) * b2 + k1 + 0];
                    double d5 = (this.noiseField[((i1 + 0) * l + j1 + 0) * b2 + k1 + 1] - d1) * d0;
                    double d6 = (this.noiseField[((i1 + 0) * l + j1 + 1) * b2 + k1 + 1] - d2) * d0;
                    double d7 = (this.noiseField[((i1 + 1) * l + j1 + 0) * b2 + k1 + 1] - d3) * d0;
                    double d8 = (this.noiseField[((i1 + 1) * l + j1 + 1) * b2 + k1 + 1] - d4) * d0;
                    for (int l1 = 0; l1 < 8; ++l1) {
                        double d9 = 0.25;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;
                        for (int i2 = 0; i2 < 4; ++i2) {
                            int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k1 * 8 + l1;
                            int short1 = 128;
                            double d14 = 0.25;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;
                            for (int k2 = 0; k2 < 4; ++k2) {
                                block = Blocks.field_150348_b;
                                if (d15 > 0.0) {
                                    block = null;
                                }
                                p_147419_3_[j2] = block;
                                j2 += short1;
                                d15 += d16;
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

    public void replaceBiomeBlocks(int p_147418_1_, int p_147418_2_, Block[] p_147418_3_, byte[] meta, BiomeGenBase[] biomes) {
        int b0 = 64;
        double d0 = 0.03125;
        this.slowsandNoise = this.slowsandGravelNoiseGen.func_76304_a(this.slowsandNoise, p_147418_1_ * 16, p_147418_2_ * 16, 0, 16, 16, 1, d0, d0, 1.0);
        this.gravelNoise = this.slowsandGravelNoiseGen.func_76304_a(this.gravelNoise, p_147418_1_ * 16, 109, p_147418_2_ * 16, 16, 1, 16, d0, 1.0, d0);
        this.netherrackExclusivityNoise = this.netherrackExculsivityNoiseGen.func_76304_a(this.netherrackExclusivityNoise, p_147418_1_ * 16, p_147418_2_ * 16, 0, 16, 16, 1, d0 * 2.0, d0 * 2.0, d0 * 2.0);
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                boolean flag = this.slowsandNoise[k + l * 16] + this.hellRNG.nextDouble() * 0.2 > 0.0;
                boolean flag1 = this.gravelNoise[k + l * 16] + this.hellRNG.nextDouble() * 0.2 > 0.0;
                int i1 = (int)(this.netherrackExclusivityNoise[k + l * 16] / 3.0 + 3.0 + this.hellRNG.nextDouble() * 0.25);
                int j1 = -1;
                BlockGrass block = Blocks.field_150349_c;
                Block block1 = Blocks.field_150346_d;
                for (int k1 = 127; k1 >= 0; --k1) {
                    int l1 = (l * 16 + k) * 128 + k1;
                    if (k1 < 127 - this.hellRNG.nextInt(5) && k1 > 0 + this.hellRNG.nextInt(5)) {
                        Block block2 = p_147418_3_[l1];
                        if (block2 != null && block2.func_149688_o() != Material.field_151579_a) {
                            if (block2 != Blocks.field_150348_b) continue;
                            if (j1 == -1) {
                                if (i1 <= 0) {
                                    block = null;
                                    block1 = Blocks.field_150348_b;
                                } else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
                                    block = Blocks.field_150348_b;
                                    block1 = Blocks.field_150348_b;
                                    if (flag1) {
                                        block = Blocks.field_150349_c;
                                        block1 = Blocks.field_150346_d;
                                    }
                                    if (flag) {
                                        block = Blocks.field_150349_c;
                                        block1 = Blocks.field_150346_d;
                                    }
                                }
                                if (k1 < b0 && block == Blocks.field_150350_a) {
                                    block = Blocks.field_150355_j;
                                }
                                j1 = i1;
                                if (k1 >= b0 - 1) {
                                    p_147418_3_[l1] = block;
                                    continue;
                                }
                                p_147418_3_[l1] = block1;
                                continue;
                            }
                            if (j1 <= 0) continue;
                            --j1;
                            p_147418_3_[l1] = block1;
                            continue;
                        }
                        j1 = -1;
                        continue;
                    }
                    p_147418_3_[l1] = null;
                }
            }
        }
    }

    public Chunk func_73158_c(int p_73158_1_, int p_73158_2_) {
        return this.func_73154_d(p_73158_1_, p_73158_2_);
    }

    public Chunk func_73154_d(int p_73154_1_, int p_73154_2_) {
        this.hellRNG.setSeed((long)p_73154_1_ * 341873128712L + (long)p_73154_2_ * 132897987541L);
        Block[] ablock = new Block[32768];
        byte[] meta = new byte[ablock.length];
        this.func_147419_a(p_73154_1_, p_73154_2_, ablock);
        this.replaceBiomeBlocks(p_73154_1_, p_73154_2_, ablock, meta, null);
        Chunk chunk = new Chunk(this.worldObj, ablock, meta, p_73154_1_, p_73154_2_);
        OreSpawnMain.Chunker.generateOresInChunk(this.worldObj, this.worldObj.field_73012_v, p_73154_1_ * 16, p_73154_2_ * 16, chunk);
        this.addScragglyTrees(this.worldObj, p_73154_1_ * 16, p_73154_2_ * 16, chunk);
        chunk.func_76603_b();
        return chunk;
    }

    private double[] initializeNoiseField(double[] p_73164_1_, int p_73164_2_, int p_73164_3_, int p_73164_4_, int p_73164_5_, int p_73164_6_, int p_73164_7_) {
        int i2;
        int k1 = 0;
        int l1 = 0;
        double[] adouble1 = new double[p_73164_6_];
        if (p_73164_1_ == null) {
            p_73164_1_ = new double[p_73164_5_ * p_73164_6_ * p_73164_7_];
        }
        double d0 = 684.412;
        double d1 = 2053.236;
        this.noiseData4 = this.netherNoiseGen6.func_76304_a(this.noiseData4, p_73164_2_, p_73164_3_, p_73164_4_, p_73164_5_, 1, p_73164_7_, 1.0, 0.0, 1.0);
        this.noiseData5 = this.netherNoiseGen7.func_76304_a(this.noiseData5, p_73164_2_, p_73164_3_, p_73164_4_, p_73164_5_, 1, p_73164_7_, 100.0, 0.0, 100.0);
        this.noiseData1 = this.netherNoiseGen3.func_76304_a(this.noiseData1, p_73164_2_, p_73164_3_, p_73164_4_, p_73164_5_, p_73164_6_, p_73164_7_, d0 / 80.0, d1 / 60.0, d0 / 80.0);
        this.noiseData2 = this.netherNoiseGen1.func_76304_a(this.noiseData2, p_73164_2_, p_73164_3_, p_73164_4_, p_73164_5_, p_73164_6_, p_73164_7_, d0, d1, d0);
        this.noiseData3 = this.netherNoiseGen2.func_76304_a(this.noiseData3, p_73164_2_, p_73164_3_, p_73164_4_, p_73164_5_, p_73164_6_, p_73164_7_, d0, d1, d0);
        for (i2 = 0; i2 < p_73164_6_; ++i2) {
            adouble1[i2] = Math.cos((double)i2 * Math.PI * 6.0 / (double)p_73164_6_) * 2.0;
            double d2 = i2;
            if (i2 > p_73164_6_ / 2) {
                d2 = p_73164_6_ - 1 - i2;
            }
            if (!(d2 < 4.0)) continue;
            d2 = 4.0 - d2;
            int n = i2;
            adouble1[n] = adouble1[n] - d2 * d2 * d2 * 10.0;
        }
        for (i2 = 0; i2 < p_73164_5_; ++i2) {
            for (int k2 = 0; k2 < p_73164_7_; ++k2) {
                double d3 = (this.noiseData4[l1] + 256.0) / 512.0;
                if (d3 > 1.0) {
                    d3 = 1.0;
                }
                double d4 = 0.0;
                double d5 = this.noiseData5[l1] / 8000.0;
                if (d5 < 0.0) {
                    d5 = -d5;
                }
                if ((d5 = d5 * 3.0 - 3.0) < 0.0) {
                    if ((d5 /= 2.0) < -1.0) {
                        d5 = -1.0;
                    }
                    d5 /= 1.4;
                    d5 /= 2.0;
                    d3 = 0.0;
                } else {
                    if (d5 > 1.0) {
                        d5 = 1.0;
                    }
                    d5 /= 6.0;
                }
                d3 += 0.5;
                d5 = d5 * (double)p_73164_6_ / 16.0;
                ++l1;
                for (int j2 = 0; j2 < p_73164_6_; ++j2) {
                    double d11;
                    double d6 = 0.0;
                    double d7 = adouble1[j2];
                    double d8 = this.noiseData2[k1] / 512.0;
                    double d9 = this.noiseData3[k1] / 512.0;
                    double d10 = (this.noiseData1[k1] / 10.0 + 1.0) / 2.0;
                    d6 = d10 < 0.0 ? d8 : (d10 > 1.0 ? d9 : d8 + (d9 - d8) * d10);
                    d6 -= d7;
                    if (j2 > p_73164_6_ - 4) {
                        d11 = (float)(j2 - (p_73164_6_ - 4)) / 3.0f;
                        d6 = d6 * (1.0 - d11) + -10.0 * d11;
                    }
                    if ((double)j2 < d4) {
                        d11 = (d4 - (double)j2) / 4.0;
                        if (d11 < 0.0) {
                            d11 = 0.0;
                        }
                        if (d11 > 1.0) {
                            d11 = 1.0;
                        }
                        d6 = d6 * (1.0 - d11) + -10.0 * d11;
                    }
                    p_73164_1_[k1] = d6;
                    ++k1;
                }
            }
        }
        return p_73164_1_;
    }

    public boolean func_73149_a(int p_73149_1_, int p_73149_2_) {
        return true;
    }

    public void func_73153_a(IChunkProvider p_73153_1_, int par2, int par3) {
        BlockFalling.field_149832_M = false;
        int var4 = par2 * 16;
        int var5 = par3 * 16;
        BiomeGenBase var6 = this.worldObj.func_72807_a(var4 + 16, var5 + 16);
        var6.func_76728_a(this.worldObj, this.worldObj.field_73012_v, var4, var5);
        SpawnerAnimals.func_77191_a((World)this.worldObj, (BiomeGenBase)var6, (int)(var4 + 8), (int)(var5 + 8), (int)16, (int)16, (Random)this.worldObj.field_73012_v);
    }

    public boolean func_73151_a(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return true;
    }

    public void func_104112_b() {
    }

    public boolean func_73156_b() {
        return false;
    }

    public boolean func_73157_c() {
        return true;
    }

    public String func_73148_d() {
        return "ChaosDimension";
    }

    public List func_73155_a(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_) {
        BiomeGenBase biomegenbase = this.worldObj.func_72807_a(p_73155_2_, p_73155_4_);
        return biomegenbase.func_76747_a(p_73155_1_);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
        return null;
    }

    public int func_73152_e() {
        return 0;
    }

    public void func_82695_e(int p_82695_1_, int p_82695_2_) {
    }

    public void addScragglyTrees(World world, int chunkX, int chunkZ, Chunk chunk) {
        int howmany = 1 + world.field_73012_v.nextInt(5);
        if (world.field_73012_v.nextInt(4) != 0) {
            return;
        }
        if (OreSpawnMain.LessLag == 1) {
            howmany /= 2;
        }
        if (OreSpawnMain.LessLag == 2) {
            howmany /= 4;
        }
        if (howmany == 0) {
            return;
        }
        block0: for (int i = 0; i < howmany; ++i) {
            int posX = 2 + chunkX + this.random.nextInt(12);
            int posZ = 2 + chunkZ + this.random.nextInt(12);
            for (int posY = 120; posY > 50; --posY) {
                if (OreSpawnMain.getBlockIDInChunk(chunk, posX, posY - 1, posZ) != Blocks.field_150349_c) continue;
                this.ScragglyTreeWithBranches(world, posX, posY, posZ, chunk);
                continue block0;
            }
        }
    }

    public void makeScragglyBranch(World world, int x, int y, int z, int len, int biasx, int biasz, Chunk chunk) {
        for (int k = 0; k < len; ++k) {
            int iy;
            Block bid;
            int ix = this.random.nextInt(2) - this.random.nextInt(2) + biasx;
            int iz = this.random.nextInt(2) - this.random.nextInt(2) + biasz;
            if (ix > 1) {
                ix = 1;
            }
            if (ix < -1) {
                ix = -1;
            }
            if (iz > 1) {
                iz = 1;
            }
            if (iz < -1) {
                iz = -1;
            }
            if ((bid = OreSpawnMain.getBlockIDInChunk(chunk, x += ix, y += (iy = this.random.nextInt(3) > 0 ? 1 : 0), z += iz)) != Blocks.field_150350_a && bid != Blocks.field_150364_r && bid != OreSpawnMain.MyAppleLeaves) {
                return;
            }
            OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x, y, z, Blocks.field_150364_r, 0);
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk(chunk, x + m, y, z + n)) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x + m, y, z + n, OreSpawnMain.MyAppleLeaves, 0);
                }
            }
            if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk(chunk, x, y + 1, z)) != Blocks.field_150350_a) continue;
            OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x, y + 1, z, OreSpawnMain.MyAppleLeaves, 0);
        }
    }

    public void ScragglyTreeWithBranches(World world, int x, int y, int z, Chunk chunk) {
        Block bid;
        int k;
        int i = 1 + this.random.nextInt(3);
        int j = i + this.random.nextInt(12);
        for (k = 0; k < i; ++k) {
            bid = OreSpawnMain.getBlockIDInChunk(chunk, x, y + k, z);
            if (k >= 1 && bid != Blocks.field_150350_a && bid != Blocks.field_150364_r && bid != OreSpawnMain.MyAppleLeaves) {
                return;
            }
            OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x, y + k, z, Blocks.field_150364_r, 0);
        }
        y += i - 1;
        for (k = i; k < j; ++k) {
            int ix = this.random.nextInt(2) - this.random.nextInt(2);
            int iz = this.random.nextInt(2) - this.random.nextInt(2);
            int iy = this.random.nextInt(4) > 0 ? 1 : 0;
            bid = OreSpawnMain.getBlockIDInChunk(chunk, x += ix, y += iy, z += iz);
            if (bid != Blocks.field_150350_a && bid != Blocks.field_150364_r && bid != OreSpawnMain.MyAppleLeaves) break;
            OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x, y, z, Blocks.field_150364_r, 0);
            if (this.random.nextInt(4) == 1) {
                this.makeScragglyBranch(world, x, y, z, this.random.nextInt(1 + j - k), this.random.nextInt(2) - this.random.nextInt(2), this.random.nextInt(2) - this.random.nextInt(2), chunk);
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk(chunk, x + m, y, z + n)) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x + m, y, z + n, OreSpawnMain.MyAppleLeaves, 0);
                }
            }
            if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk(chunk, x, y + 1, z)) != Blocks.field_150350_a) continue;
            OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x, y + 1, z, OreSpawnMain.MyAppleLeaves, 0);
        }
    }
}

