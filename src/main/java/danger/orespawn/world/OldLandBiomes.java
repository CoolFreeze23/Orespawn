package danger.orespawn.world;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Set;

/**
 * Utopia, the Village, Crystal and Mining are each one biome. Land an earlier version saved in Mining reached Y192, and
 * the game gives the sections over it that the save lacks the plains biome (rain and snow, lightning, the plains' mobs
 * over old peaks cut at Y191); as such a chunk loads, the plains there take the dimension's own biome, and the chunk is
 * saved so. Plains is what the game fills in where a save holds no biome, and none of these dimensions' own, so nothing
 * else is touched: a biome set there on purpose (with /fillbiome, say) stays.
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

    /** Every cell of the chunk holding the plains biome given {@code biome}; whether any was. */
    public static boolean refill(ChunkAccess chunk, Holder<Biome> biome) {
        if (biome.is(Biomes.PLAINS)) return false;
        boolean changed = false;
        for (LevelChunkSection section : chunk.getSections()) {
            if (!holds(section, held -> held.is(Biomes.PLAINS))) continue;
            PalettedContainerRO<Holder<Biome>> old = section.getBiomes();
            section.fillBiomesFromNoise((x, y, z, sampler) -> {
                Holder<Biome> held = old.get(x, y, z);
                return held.is(Biomes.PLAINS) ? biome : held;
            }, null, 0, 0, 0);
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
