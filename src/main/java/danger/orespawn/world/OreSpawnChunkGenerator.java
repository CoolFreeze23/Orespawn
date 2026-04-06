package danger.orespawn.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Placeholder chunk generator for OreSpawn custom dimensions.
 * Extends NoiseBasedChunkGenerator so the dimensions get standard terrain
 * with the same noise settings as the overworld. Custom terrain features
 * (huge trees, dungeons, altars, etc.) will be layered on via placed features
 * and structure sets once those are ported.
 */
public class OreSpawnChunkGenerator extends NoiseBasedChunkGenerator {

    public static final MapCodec<OreSpawnChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings)
            ).apply(instance, OreSpawnChunkGenerator::new)
    );

    private final Holder<NoiseGeneratorSettings> settings;

    public OreSpawnChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        this.settings = settings;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
