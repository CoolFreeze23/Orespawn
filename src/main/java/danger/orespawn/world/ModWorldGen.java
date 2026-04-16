package danger.orespawn.world;

import com.mojang.serialization.MapCodec;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * DeferredRegister wiring for OreSpawn's chunk generator codec.
 *
 * <p><b>Why only the chunk generator is registered here:</b> In 1.21.1 (unlike
 * 1.7.10 / 1.12.2) the {@code DimensionType} and {@code LevelStem} registries
 * are <em>datapack-backed</em>, not code-registered. NeoForge expects dimension
 * types to live in {@code data/<namespace>/dimension_type/*.json} and dimension
 * definitions in {@code data/<namespace>/dimension/*.json}. See the JSON files
 * under {@code src/main/resources/data/orespawn/} for the actual definitions.</p>
 *
 * <p>The one registry that still needs a code-side entry is the
 * {@link ChunkGenerator}'s {@link MapCodec}, because the dimension JSON's
 * {@code "generator": { "type": "orespawn:orespawn", ... }} string is resolved
 * through {@link Registries#CHUNK_GENERATOR}. Without this registration the
 * dimension JSON would fail to load with an "unknown codec" error at world
 * creation time.</p>
 *
 * <p>One codec, many dimensions: {@link OreSpawnChunkGenerator} uses the
 * {@link DimensionStyle} discriminator in its codec to select per-dimension
 * behaviour, so all six OreSpawn dimensions (Crystal, Chaos, Utopia, Village,
 * Islands, Mining) can share this single registered codec. See the
 * {@code danger.orespawn.world} package docs for the dispatch map.</p>
 */
public class ModWorldGen {

    /** DeferredRegister for our chunk-generator codec. Registered on the mod event bus. */
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, OreSpawnMod.MOD_ID);

    /**
     * The sole OreSpawn chunk generator codec, registered under
     * {@code orespawn:orespawn}. Referenced by every
     * {@code data/orespawn/dimension/*.json} as
     * {@code "generator": { "type": "orespawn:orespawn", ... }}.
     */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<OreSpawnChunkGenerator>> ORESPAWN_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("orespawn", () -> OreSpawnChunkGenerator.CODEC);

    /**
     * Wire the chunk-generator register onto the mod event bus. Called from
     * {@link danger.orespawn.OreSpawnMod#OreSpawnMod}.
     */
    public static void register(IEventBus eventBus) {
        CHUNK_GENERATORS.register(eventBus);
    }
}
