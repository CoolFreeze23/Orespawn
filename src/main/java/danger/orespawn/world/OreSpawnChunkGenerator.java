package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.StructureManager;

public class OreSpawnChunkGenerator extends NoiseBasedChunkGenerator {

    public static final MapCodec<OreSpawnChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings),
                    Codec.BOOL.optionalFieldOf("crystal_surface", false).forGetter(gen -> gen.crystalSurface)
            ).apply(instance, OreSpawnChunkGenerator::new)
    );

    private final Holder<NoiseGeneratorSettings> settings;
    private final boolean crystalSurface;
    private static int recentlyPlaced = 0;

    public OreSpawnChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, boolean crystalSurface) {
        super(biomeSource, settings);
        this.settings = settings;
        this.crystalSurface = crystalSurface;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
        super.buildSurface(region, structures, randomState, chunk);

        placeDungeons(region, chunk);

        if (!crystalSurface) return;

        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                        continue;
                    }

                    if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SAND) || state.is(Blocks.MYCELIUM)) {
                        chunk.setBlockState(pos, crystalGrass, false);
                    }

                    for (int d = 1; d < 4 && (y - d) >= chunk.getMinBuildHeight(); d++) {
                        BlockPos below = new BlockPos(worldX, y - d, worldZ);
                        BlockState belowState = chunk.getBlockState(below);
                        if (belowState.is(Blocks.DIRT) || belowState.is(Blocks.SAND) || belowState.is(Blocks.SANDSTONE)) {
                            chunk.setBlockState(below, crystalStone, false);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void placeDungeons(WorldGenRegion region, ChunkAccess chunk) {
        if (recentlyPlaced > 0) {
            --recentlyPlaced;
            return;
        }

        RandomSource random = region.getRandom();
        int cx = chunk.getPos().getMinBlockX();
        int cz = chunk.getPos().getMinBlockZ();

        if (crystalSurface) {
            if (random.nextInt(15) == 0) {
                int y = 10 + random.nextInt(10);
                BlockPos pos = new BlockPos(cx + 8, y, cz + 8);
                if (GenericDungeon.tryPlaceRubyDungeon(region, random, pos)) {
                    recentlyPlaced = 50;
                    return;
                }
            }
        }

        if (random.nextInt(16) == 0) {
            int y = 5 + random.nextInt(40);
            BlockPos pos = new BlockPos(cx + 8, y, cz + 8);
            if (GenericDungeon.tryPlaceGenericDungeon(region, random, pos)) {
                recentlyPlaced = 50;
            }
        }
    }
}
