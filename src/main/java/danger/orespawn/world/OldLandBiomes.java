package danger.orespawn.world;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Set;

/**
 * Utopia, the Village, Crystal and Mining are each one biome. Land an earlier version saved in Mining reached Y192, and
 * the game gives the sections over it that the save lacks the plains biome (rain and snow, lightning, the plains' mobs
 * over old peaks cut at Y191); as such a chunk loads, every section holding a biome other than the dimension's own takes
 * the dimension's, and the chunk is saved so. Land saved since holds only the dimension's biome and is left alone.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public final class OldLandBiomes {
    private OldLandBiomes() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(level.getChunkSource().getGenerator() instanceof OreSpawnChunkGenerator generator)
                || !generator.originalWorld()) {
            return;
        }
        Set<Holder<Biome>> own = generator.getBiomeSource().possibleBiomes();
        if (own.size() == 1 && refill(event.getChunk(), own.iterator().next())) event.getChunk().setUnsaved(true);
    }

    /** Every section of the chunk holding a biome other than {@code biome} filled with it; whether any was. */
    public static boolean refill(ChunkAccess chunk, Holder<Biome> biome) {
        boolean changed = false;
        for (LevelChunkSection section : chunk.getSections()) {
            if (!holds(section, held -> !held.equals(biome))) continue;
            section.fillBiomesFromNoise((x, y, z, sampler) -> biome, null, 0, 0, 0);
            changed = true;
        }
        return changed;
    }

    /** Whether a biome the section's cells hold (not merely its palette) matches. */
    public static boolean holds(LevelChunkSection section, java.util.function.Predicate<Holder<Biome>> test) {
        boolean[] found = {false};
        section.getBiomes().getAll(held -> found[0] |= test.test(held));
        return found[0];
    }
}
