package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Random;

/**
 * The surface the original laid on the terrain of Utopia, the Village, Mining and Crystal, block for block: its chunk
 * providers' replaceBlocksForBiome (orig ChunkProviderOreSpawn.java, ChunkProviderOreSpawn2, 3 and 5) hands each column
 * to the biome's surface pass, vanilla 1.7.10's BiomeGenBase.genBiomeTerrain with Utopia's and the Village's grass and
 * dirt, BiomeGenHills.genTerrainBlocks for Mining's Extreme Hills (stone over stone where the surface noise is above 1),
 * and Crystal's own copy (orig ChunkProviderOreSpawn5.MygenBiomeTerrain: crystal grass over crystal stone, no gravel).
 *
 * <p>As the original: the chunk's random seeded {@code chunkX * 341873128712 + chunkZ * 132897987541} (provideChunk), the
 * columns z outer and x inner (the provider passes x and z swapped and the pass writes them swapped back), and in each
 * column one draw for its depth ({@code surfaceNoise / 3 + 3 + nextDouble() * 0.25}, truncated) and one bedrock roll at
 * every height from 255 down ({@code y <= nextInt(5)}). The first stone under air or water starts a layer: at Y62 and up
 * the top block, below it the filler (vanilla's pass: gravel instead when the layer starts more than its depth below Y56),
 * then the filler for the depth; a depth of zero or less leaves no top block (air, or water below Y63) and stone under
 * it. The water and air come from the terrain (still water in every empty cell below Y63); ice needs a temperature
 * under 0.15, which no dimension here has.</p>
 */
public final class LegacySurface {
    /** The surface pass of a dimension's biome. */
    public enum Kind {
        /** Utopia and the Village: BiomeGenUtopianPlains keeps vanilla's grass and dirt. */
        PLAINS,
        /** Mining: Extreme Hills, stone for top and filler where the surface noise is above 1. */
        EXTREME_HILLS,
        /** Crystal: crystal grass over crystal stone, and no gravel. */
        CRYSTAL
    }

    /** A chunk's blocks by column-local x and z (0 to 15) and absolute y. */
    public interface Columns {
        BlockState get(int x, int y, int z);

        void set(int x, int y, int z, BlockState state);
    }

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.defaultBlockState();

    private LegacySurface() {
    }

    /** The surface laid on a chunk the noise has filled. */
    public static void build(ChunkAccess chunk, LegacyTerrainNoise noise, Kind kind) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        build(chunk.getPos().x, chunk.getPos().z, noise, kind, new Columns() {
            @Override
            public BlockState get(int x, int y, int z) {
                return y < minY || y >= maxY ? AIR : chunk.getBlockState(cursor.set(minX + x, y, minZ + z));
            }

            @Override
            public void set(int x, int y, int z, BlockState state) {
                if (y >= minY && y < maxY) chunk.setBlockState(cursor.set(minX + x, y, minZ + z), state, false);
            }
        });
    }

    /** The surface laid on a chunk's columns (the tests', and the chunk's above). */
    public static void build(int chunkX, int chunkZ, LegacyTerrainNoise noise, Kind kind, Columns columns) {
        Random random = new Random((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        BlockState stone = kind == Kind.CRYSTAL ? ModBlocks.CRYSTAL_STONE.get().defaultBlockState() : STONE;
        BlockState grass = kind == Kind.CRYSTAL ? ModBlocks.CRYSTAL_GRASS.get().defaultBlockState() : GRASS;
        BlockState dirt = kind == Kind.CRYSTAL ? stone : DIRT;
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                double surfaceNoise = noise.surfaceNoise(chunkX * 16 + x, chunkZ * 16 + z);
                BlockState top = grass;
                BlockState filler = dirt;
                if (kind == Kind.EXTREME_HILLS && surfaceNoise > 1.0) {
                    top = STONE;
                    filler = STONE;
                }
                column(columns, x, z, random, surfaceNoise, kind, stone, top, filler);
            }
        }
    }

    /** genBiomeTerrain (and Crystal's copy) for one column; a null block is the original's empty cell. */
    private static void column(Columns columns, int x, int z, Random random, double surfaceNoise, Kind kind,
                               BlockState stone, BlockState top, BlockState filler) {
        BlockState block = top;
        BlockState block1 = filler;
        int k = -1;
        int depth = (int) (surfaceNoise / 3.0 + 3.0 + random.nextDouble() * 0.25);
        for (int y = 255; y >= 0; y--) {
            if (y <= random.nextInt(5)) {
                columns.set(x, y, z, BEDROCK);
                continue;
            }
            BlockState here = columns.get(x, y, z);
            if (here.isAir()) {
                k = -1;
                continue;
            }
            if (here.getBlock() != stone.getBlock()) continue;
            if (k == -1) {
                if (depth <= 0) {
                    block = null;
                    block1 = stone;
                } else if (y >= 59 && y <= 64) {
                    block = top;
                    block1 = filler;
                }
                if (y < 63 && (block == null || block.isAir())) {
                    block = WATER;
                }
                k = depth;
                if (y >= 62) {
                    columns.set(x, y, z, block == null ? AIR : block);
                } else if (kind != Kind.CRYSTAL && y < 56 - depth) {
                    block = null;
                    block1 = stone;
                    columns.set(x, y, z, GRAVEL);
                } else {
                    columns.set(x, y, z, block1);
                }
            } else if (k > 0) {
                --k;
                columns.set(x, y, z, block1);
                if (kind != Kind.CRYSTAL && k == 0 && block1 == SAND) {
                    k = random.nextInt(4) + Math.max(0, y - 63);
                    block1 = SANDSTONE;
                }
            }
        }
    }
}
