/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.OreSpawnMain;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class ChunkProviderOreSpawn4
implements IChunkProvider {
    private World worldObj;
    private Random random;
    private final Block[] cachedBlockIDs = new Block[256];
    private final byte[] cachedBlockMetadata = new byte[256];

    public ChunkProviderOreSpawn4(World par1World, long par2, boolean par4) {
        this.worldObj = par1World;
        this.random = new Random(par2);
        for (int j = 0; j < 8; ++j) {
            this.cachedBlockIDs[j] = j == 0 ? Blocks.field_150357_h : (j == 7 ? Blocks.field_150349_c : Blocks.field_150346_d);
        }
    }

    public Chunk func_73158_c(int par1, int par2) {
        return this.func_73154_d(par1, par2);
    }

    public Chunk func_73154_d(int par1, int par2) {
        Chunk chunk = new Chunk(this.worldObj, par1, par2);
        for (int k = 0; k < this.cachedBlockIDs.length; ++k) {
            Block block = this.cachedBlockIDs[k];
            if (block == null) continue;
            int l = k >> 4;
            ExtendedBlockStorage extendedblockstorage = chunk.func_76587_i()[l];
            if (extendedblockstorage == null) {
                chunk.func_76587_i()[l] = extendedblockstorage = new ExtendedBlockStorage(k, !this.worldObj.field_73011_w.field_76576_e);
            }
            for (int i1 = 0; i1 < 16; ++i1) {
                for (int j1 = 0; j1 < 16; ++j1) {
                    extendedblockstorage.func_150818_a(i1, k & 0xF, j1, block);
                    extendedblockstorage.func_76654_b(i1, k & 0xF, j1, (int)this.cachedBlockMetadata[k]);
                }
            }
        }
        this.addScragglyTrees(this.worldObj, par1 * 16, par2 * 16, chunk);
        chunk.func_76603_b();
        return chunk;
    }

    public boolean func_73149_a(int par1, int par2) {
        return true;
    }

    public void func_73153_a(IChunkProvider par1IChunkProvider, int par2, int par3) {
        int k = par2 * 16;
        int l = par3 * 16;
        this.random.setSeed(this.worldObj.func_72905_C());
        long i1 = this.random.nextLong() / 2L * 2L + 1L;
        long j1 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long)par2 * i1 + (long)par3 * j1 ^ this.worldObj.func_72905_C());
    }

    public boolean func_73151_a(boolean par1, IProgressUpdate par2IProgressUpdate) {
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
        return "DangerDimension";
    }

    public List func_73155_a(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4) {
        BiomeGenBase biomegenbase = this.worldObj.func_72807_a(par2, par4);
        return biomegenbase.func_76747_a(par1EnumCreatureType);
    }

    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
        return null;
    }

    public int func_73152_e() {
        return 0;
    }

    public void func_82695_e(int par1, int par2) {
    }

    public void addScragglyTrees(World world, int chunkX, int chunkZ, Chunk chunk) {
        int howmany = 1 + this.random.nextInt(10);
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
            for (int posY = 20; posY > 2; --posY) {
                if (OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)posX, (int)(posY - 1), (int)posZ) != Blocks.field_150349_c) continue;
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
            if ((bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)(x += ix), (int)(y += (iy = this.random.nextInt(3) > 0 ? 1 : 0)), (int)(z += iz))) != Blocks.field_150350_a && bid != Blocks.field_150364_r && bid != OreSpawnMain.MyAppleLeaves) {
                return;
            }
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)x, (int)y, (int)z, (Block)Blocks.field_150364_r, (int)0);
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)(x + m), (int)y, (int)(z + n))) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(x + m), (int)y, (int)(z + n), (Block)OreSpawnMain.MyAppleLeaves, (int)0);
                }
            }
            if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)x, (int)(y + 1), (int)z)) != Blocks.field_150350_a) continue;
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)x, (int)(y + 1), (int)z, (Block)OreSpawnMain.MyAppleLeaves, (int)0);
        }
    }

    public void ScragglyTreeWithBranches(World world, int x, int y, int z, Chunk chunk) {
        Block bid;
        int k;
        int i = 1 + this.random.nextInt(3);
        int j = i + this.random.nextInt(12);
        for (k = 0; k < i; ++k) {
            bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)x, (int)(y + k), (int)z);
            if (k >= 1 && bid != Blocks.field_150350_a && bid != Blocks.field_150364_r && bid != OreSpawnMain.MyAppleLeaves) {
                return;
            }
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)x, (int)(y + k), (int)z, (Block)Blocks.field_150364_r, (int)0);
        }
        y += i - 1;
        for (k = i; k < j; ++k) {
            int ix = this.random.nextInt(2) - this.random.nextInt(2);
            int iz = this.random.nextInt(2) - this.random.nextInt(2);
            int iy = this.random.nextInt(4) > 0 ? 1 : 0;
            bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)(x += ix), (int)(y += iy), (int)(z += iz));
            if (bid != Blocks.field_150350_a && bid != Blocks.field_150364_r && bid != OreSpawnMain.MyAppleLeaves) break;
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)x, (int)y, (int)z, (Block)Blocks.field_150364_r, (int)0);
            if (this.random.nextInt(4) == 1) {
                this.makeScragglyBranch(world, x, y, z, this.random.nextInt(1 + j - k), this.random.nextInt(2) - this.random.nextInt(2), this.random.nextInt(2) - this.random.nextInt(2), chunk);
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)(x + m), (int)y, (int)(z + n))) != Blocks.field_150350_a) continue;
                    OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)(x + m), (int)y, (int)(z + n), (Block)OreSpawnMain.MyAppleLeaves, (int)0);
                }
            }
            if (this.random.nextInt(2) != 1 || (bid = OreSpawnMain.getBlockIDInChunk((Chunk)chunk, (int)x, (int)(y + 1), (int)z)) != Blocks.field_150350_a) continue;
            OreSpawnMain.setBlockIDWithMetadataInChunk((Chunk)chunk, (int)x, (int)(y + 1), (int)z, (Block)OreSpawnMain.MyAppleLeaves, (int)0);
        }
    }
}

