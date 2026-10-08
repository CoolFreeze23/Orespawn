package danger.orespawn.world;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

/**
 * The heights world generation builds at. In Utopia, the Village, Mining and Crystal that is the original's world, Y0 to
 * 256: their build range reaches from Y-64 to 320 so that land an earlier version generated keeps every block, while
 * their terrain is the 1.7.10 generator's from Y0 to 256 with bedrock under it. 1.21 resolves every placed feature's and
 * carver's heights in the generator's own range ({@link WorldGenerationContext}: the noise's range inside the build
 * range); OreSpawn's own builds and checks in these dimensions keep to the same range, as the original's never left its
 * 256-high world. Everywhere else it is the build range.
 */
public final class GenerationRange {
    private GenerationRange() {
    }

    /** The lowest height world generation builds at. */
    public static int bottom(ChunkGenerator generator, LevelHeightAccessor level) {
        return original(generator) ? new WorldGenerationContext(generator, level).getMinGenY() : level.getMinBuildHeight();
    }

    /** One over the highest height world generation builds at. */
    public static int top(ChunkGenerator generator, LevelHeightAccessor level) {
        if (!original(generator)) return level.getMaxBuildHeight();
        WorldGenerationContext context = new WorldGenerationContext(generator, level);
        return context.getMinGenY() + context.getGenDepth();
    }

    private static boolean original(ChunkGenerator generator) {
        return generator instanceof OreSpawnChunkGenerator orespawn && orespawn.originalWorld();
    }
}
