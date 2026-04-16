/**
 * World generation for all OreSpawn dimensions (Crystal, Chaos, Utopia,
 * Village, Islands, Mining).
 *
 * <h2>Phase 3 architecture overview</h2>
 *
 * <p>Every OreSpawn dimension is data-driven, configured by three JSON
 * trees under {@code data/orespawn/}:</p>
 * <ul>
 *   <li>{@code dimension/*.json} — binds a dimension type to a chunk
 *       generator and biome source. This is the top-level entry loaded
 *       by {@code MinecraftServer.createLevels}.</li>
 *   <li>{@code dimension_type/*.json} — ambient light, skylight, height
 *       bounds, coordinate scale, respawn/bed behaviour.</li>
 *   <li>{@code worldgen/biome/*.json} — climate, effects, spawn lists,
 *       carvers, and the ten feature decoration steps.</li>
 * </ul>
 *
 * <h2>Chunk generator consolidation</h2>
 *
 * <p>1.7.10 OreSpawn shipped six dedicated {@code ChunkProviderOreSpawn[1-6]}
 * classes — one per dimension — each with its own terrain shape, surface
 * blocks, ore tables, and populate hook. 1.21.1 identifies chunk generators
 * by their {@link com.mojang.serialization.MapCodec}, so registering six
 * separate codecs would explode the registry namespace and duplicate a lot
 * of code.</p>
 *
 * <p>Instead we ship <em>one</em> generator
 * ({@link danger.orespawn.world.OreSpawnChunkGenerator}, registered under
 * {@code orespawn:orespawn} via {@link danger.orespawn.world.ModWorldGen})
 * that dispatches on a {@link danger.orespawn.world.DimensionStyle}
 * discriminator read from each dimension JSON's {@code dimension_style}
 * field. This lets Crystal keep its aggressive surface rewrite + maze +
 * structures while Utopia/Village/Mining share a lightweight pass-through
 * path that just runs the vanilla noise pipeline plus dungeon placement.</p>
 *
 * <h2>Thread safety</h2>
 *
 * <p>NeoForge 1.21.1 runs {@code buildSurface} and {@code applyBiomeDecoration}
 * concurrently across a worker pool — the generator instance is shared
 * across all workers for a given dimension. Every piece of cross-chunk
 * state in this package is either:</p>
 * <ul>
 *   <li>Immutable (codec-provided fields like biome source, noise settings,
 *       and the {@link danger.orespawn.world.DimensionStyle} enum).</li>
 *   <li>{@link java.util.concurrent.atomic.AtomicInteger} where a global
 *       cooldown must be enforced — see
 *       {@link danger.orespawn.world.OreSpawnChunkGenerator}'s
 *       {@code recentlyPlaced} and
 *       {@link danger.orespawn.world.CrystalStructures}' {@code
 *       recentlyPlaced}. Both use {@code updateAndGet} / {@code compareAndSet}
 *       so two worker threads can never both observe "cooldown is 0" and
 *       place overlapping mega-structures one chunk apart.</li>
 *   <li>Per-call locals inside {@code static} helper methods — stateless
 *       by construction.</li>
 * </ul>
 *
 * <h2>Why procedural (non-datapack) structures?</h2>
 *
 * <p>{@link danger.orespawn.world.CrystalStructures} and
 * {@link danger.orespawn.world.GenericDungeon} intentionally place blocks
 * directly rather than via {@code StructureFeature} / {@code
 * ConfiguredFeature}. The original 1.7.10 generation is stateful and
 * procedural — the Crystal Battle Tower, Fairy Castle Tree, Haunted House,
 * and Round Rotator all rely on nested random rolls and per-floor loot
 * tables that don't cleanly map to the data-driven structure-set/template
 * pipeline without a full rewrite. Keeping them procedural preserves the
 * original gameplay feel at the cost of giving up {@code /locate}. Simple
 * decorations (ore veins, crystal flowers, crystal trees with fixed shape)
 * are candidates for eventual migration to JSON-defined
 * {@code PlacedFeature}s if player demand warrants it.</p>
 *
 * <h2>Dimension style map</h2>
 *
 * <table border="1" summary="Per-dimension generator configuration">
 *   <tr><th>Dimension</th><th>Settings</th><th>Style</th><th>Biome</th></tr>
 *   <tr><td>Crystal</td><td>minecraft:overworld</td><td>CRYSTAL</td>
 *       <td>orespawn:crystal_plains</td></tr>
 *   <tr><td>Utopia</td><td>minecraft:overworld</td><td>UTOPIA</td>
 *       <td>orespawn:utopia_plains</td></tr>
 *   <tr><td>Village</td><td>minecraft:overworld</td><td>VILLAGE</td>
 *       <td>orespawn:village_biome</td></tr>
 *   <tr><td>Islands</td><td>minecraft:floating_islands</td><td>ISLANDS</td>
 *       <td>orespawn:island_biome</td></tr>
 *   <tr><td>Chaos</td><td>minecraft:overworld</td><td>CHAOS</td>
 *       <td>orespawn:chaos_biome</td></tr>
 *   <tr><td>Mining</td><td>minecraft:overworld</td><td>DEFAULT</td>
 *       <td>orespawn:mining_biome</td></tr>
 * </table>
 *
 * @see danger.orespawn.world.OreSpawnChunkGenerator
 * @see danger.orespawn.world.DimensionStyle
 * @see danger.orespawn.world.ModWorldGen
 * @see danger.orespawn.world.CrystalStructures
 */
package danger.orespawn.world;
