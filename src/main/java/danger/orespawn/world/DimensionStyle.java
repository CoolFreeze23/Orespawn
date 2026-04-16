package danger.orespawn.world;

import com.mojang.serialization.Codec;

import java.util.Locale;

/**
 * Identifies which OreSpawn dimension a given {@link OreSpawnChunkGenerator}
 * instance is driving. The enum exists to replace the old single-purpose
 * {@code crystal_surface: bool} codec field with an extensible discriminator
 * that lets one generator codec serve multiple dimensions while still allowing
 * each dimension to opt in to its own surface post-processing, structure set,
 * and decoration hooks.
 *
 * <p><b>Dual-reference porting context:</b></p>
 * <ul>
 *   <li>The 1.7.10 mod shipped six dedicated {@code ChunkProviderOreSpawn[1-6]}
 *       classes — one per dimension. Each baked its terrain shape, surface
 *       blocks, ore veins, and populate hooks directly into its
 *       {@code provideChunk}/{@code populate} pair.</li>
 *   <li>The 1.12.2 mod moved most per-dimension work into
 *       {@code IWorldGenerator} + {@code BiomeProviderSingle}, which is closer
 *       to the 1.21 paradigm but still had one {@code IChunkGenerator} per
 *       dimension.</li>
 *   <li>In 1.21.1 the primary extensibility points are
 *       {@link net.minecraft.world.level.chunk.ChunkGenerator} (via MapCodec)
 *       and datapack JSON for {@code dimension}, {@code dimension_type}, and
 *       {@code worldgen/biome}. Rather than register six separate chunk-generator
 *       codecs, we register <em>one</em> ({@code orespawn:orespawn}) and use
 *       this enum, read from the dimension JSON's {@code dimension_style}
 *       field, to select per-dimension behaviour.</li>
 * </ul>
 *
 * <p><b>Style meanings:</b></p>
 * <ul>
 *   <li>{@link #DEFAULT} — pass-through vanilla noise + dungeon placement only.
 *       Used by Utopia, Village, Mining and any dimension whose terrain should
 *       look like overworld plains. The biome JSON controls climate/flora.</li>
 *   <li>{@link #CRYSTAL} — runs the legacy 1.7.10 crystal surface rewrite
 *       (vanilla stone/grass → crystal blocks), shallow-water fill,
 *       {@link CrystalMaze} at Y=25, ore veins, flowers, rice/quinoa, and the
 *       procedural {@link CrystalStructures} pass during decoration.</li>
 *   <li>{@link #ISLANDS} — expects the dimension JSON to select
 *       {@code "settings": "minecraft:floating_islands"}. The generator still
 *       does the usual dungeon pass but skips the crystal-only rewrite.
 *       Scraggly oak-style trees are scattered on top of the islands to echo
 *       the original 1.7.10 {@code ChunkProviderOreSpawn4.addScragglyTrees}.</li>
 *   <li>{@link #CHAOS} — pass-through terrain but with a chaos-specific light
 *       decoration pass (future: hellish flora, nightmare dungeon placement).</li>
 *   <li>{@link #VILLAGE} — identical to {@code DEFAULT} for now; the hook lets
 *       us later wire in 1.7.10's {@code MapGenMoreVillages} frequency bump
 *       without touching the codec surface.</li>
 *   <li>{@link #UTOPIA} — identical to {@code DEFAULT} for now; placeholder
 *       so future time-sync or bonus-generation hooks have a home.</li>
 * </ul>
 *
 * <p><b>Codec:</b> serialised as a lowercase string. Unknown values map to
 * {@link #DEFAULT} rather than throwing so a dimension JSON typo never bricks
 * world loading.</p>
 */
public enum DimensionStyle {
    DEFAULT,
    CRYSTAL,
    ISLANDS,
    CHAOS,
    VILLAGE,
    UTOPIA;

    public static final Codec<DimensionStyle> CODEC = Codec.STRING.xmap(
            DimensionStyle::fromId,
            DimensionStyle::getId
    );

    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static DimensionStyle fromId(String id) {
        if (id == null) return DEFAULT;
        try {
            return valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}
