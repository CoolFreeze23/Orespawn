package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.StructureManager;

/**
 * Custom chunk generator for every OreSpawn dimension. Extends vanilla's
 * noise-based generator so terrain shape (caves, height variation, biome
 * blending) still benefits from 1.21.1's noise router pipeline, but adds:
 *
 * <ul>
 *   <li>Post-terrain block replacement: vanilla stone/grass/dirt become
 *       CrystalStone and CrystalGrass to match the original 1.7.10 palette
 *       (CRYSTAL style only).</li>
 *   <li>Shallow-water fill: converts shallow seas to land so the Crystal
 *       dimension is mostly walkable, like the original (CRYSTAL style only).</li>
 *   <li>Custom per-chunk features: mazes, crystal trees, flora, ore veins.</li>
 *   <li>Cross-chunk structures (battle towers, haunted houses) placed in
 *       {@link #applyBiomeDecoration} so neighboring chunks are stable.</li>
 *   <li>Scraggly tree scatter on floating islands (ISLANDS style) ported from
 *       1.7.10 {@code ChunkProviderOreSpawn4.addScragglyTrees}.</li>
 * </ul>
 *
 * <p><b>Dual-reference context.</b> 1.7.10 shipped six dedicated
 * {@code ChunkProviderOreSpawn[1-6]} classes. 1.12.2 consolidated common work
 * behind {@code BiomeProviderSingle} + per-dimension chunk generators. In
 * 1.21.1 chunk generators are identified by their MapCodec, so registering six
 * separate codecs is wasteful. Instead we register <em>one</em>
 * ({@code orespawn:orespawn}) that dispatches on {@link #style}, read from the
 * dimension JSON's {@code dimension_style} field â€” see {@link DimensionStyle}
 * for the rationale.</p>
 *
 * <p><b>Thread safety.</b> NeoForge 1.21.1 runs {@link #buildSurface} and
 * {@link #applyBiomeDecoration} on a worker pool â€” the generator instance is
 * shared across all workers for a given dimension. Every field on this class
 * that is read during generation is either immutable (codec-provided) or
 * wrapped in {@link java.util.concurrent.atomic.AtomicInteger}. Callees that
 * need ordering guarantees (dungeon cooldowns, large-structure cooldowns) use
 * atomic compare/update so two workers can never both pass a "cooldown is 0"
 * check at the same instant.</p>
 *
 * <p><b>Codec fields:</b></p>
 * <ul>
 *   <li>{@code biome_source} â€” forwarded to {@link NoiseBasedChunkGenerator}.</li>
 *   <li>{@code settings} â€” {@link NoiseGeneratorSettings} holder (overworld,
 *       floating_islands, etc). Dimensions pick their noise preset here.</li>
 *   <li>{@code dimension_style} â€” optional {@link DimensionStyle} discriminator,
 *       defaults to {@link DimensionStyle#DEFAULT}.</li>
 *   <li>{@code crystal_surface} â€” legacy boolean, still accepted for
 *       backwards compatibility with pre-Phase-3 dimension JSONs. When
 *       present and {@code true} it overrides {@code dimension_style} with
 *       {@link DimensionStyle#CRYSTAL}. New JSONs should use
 *       {@code dimension_style} instead.</li>
 * </ul>
 */
public class OreSpawnChunkGenerator extends NoiseBasedChunkGenerator {

    /**
     * MapCodec exposing all four fields. We keep {@code crystal_surface} as an
     * optional boolean for backwards compatibility with dimension JSONs that
     * predate the {@link DimensionStyle} enum â€” the constructor folds it into
     * the effective style when decoding, and always emits {@code false} on
     * re-encode so new JSONs can rely solely on {@code dimension_style}.
     */
    public static final MapCodec<OreSpawnChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings),
                    DimensionStyle.CODEC.optionalFieldOf("dimension_style", DimensionStyle.DEFAULT).forGetter(gen -> gen.style),
                    Codec.BOOL.optionalFieldOf("crystal_surface", false).forGetter(gen -> false)
            ).apply(instance, OreSpawnChunkGenerator::new)
    );

    private final Holder<NoiseGeneratorSettings> settings;
    private final DimensionStyle style;
    private final boolean crystalSurface;

    /**
     * Same per-dimension/thread-safe cooldown pattern as {@link #dungeonPlacementCooldown},
     * but owned here and handed to {@link CrystalStructures#generate} so the large crystal
     * structures (Battle Tower, Haunted House, ...) don't share state across dimensions
     * or generator instances either (BUG-013).
     */
    private final java.util.concurrent.atomic.AtomicInteger crystalStructureCooldown =
            new java.util.concurrent.atomic.AtomicInteger(0);

    public OreSpawnChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings,
                                  DimensionStyle style, boolean legacyCrystalSurface) {
        super(biomeSource, settings);
        this.settings = settings;
        // Backwards compatibility: older dimension JSONs used the boolean
        // `crystal_surface: true` to mean "apply crystal surface rewrite". If
        // that field is present and truthy and the new `dimension_style` was
        // left at default, promote the style to CRYSTAL so existing worlds
        // don't regress.
        this.style = (style == DimensionStyle.DEFAULT && legacyCrystalSurface)
                ? DimensionStyle.CRYSTAL : style;
        this.crystalSurface = this.style == DimensionStyle.CRYSTAL;
    }

    /**
     * Convenience overload used by tests and any programmatic instantiation
     * that doesn't care about the legacy boolean.
     */
    public OreSpawnChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings,
                                  DimensionStyle style) {
        this(biomeSource, settings, style, false);
    }

    public DimensionStyle getStyle() {
        return style;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
        super.buildSurface(region, structures, randomState, chunk);

        try {
            placeDungeons(region, chunk);
        } catch (Exception e) {
            // Dungeon placement can fail during early dimension generation (e.g.
            // when neighboring chunks referenced for solidity checks haven't been
            // generated yet). Failing silently here is correct: dungeons are
            // decorative and a missed chunk just means no dungeon in that chunk.
        }

        // Dispatch per-dimension surface post-processing. A switch over the
        // enum keeps every style's hook in one place and compiles to a clean
        // tableswitch at runtime.
        switch (style) {
            case CRYSTAL -> applyCrystalSurface(chunk, region.getRandom());
            case ISLANDS -> applyIslandsSurface(chunk, region.getRandom());
            case CHAOS -> applyChaosSurface(chunk, region.getRandom());
            case VILLAGE, UTOPIA, MINING, DEFAULT -> {
                // Pass-through â€” vanilla noise + the dungeon pass above is
                // sufficient. The "no oceans" continental shape for
                // UTOPIA/VILLAGE/MINING is now data-driven via the
                // orespawn:inland noise_settings (constant continentalness),
                // and Chaos terrain via orespawn:chaos (nether-style noise,
                // orig ChunkProviderOreSpawn6), so the chunk generator stays
                // clean: surface rules apply properly to the new landmass
                // without any post-fill hack patching raw stone over
                // generated terrain.
            }
        }
    }

    /**
     * Full 1.7.10 Crystal surface rewrite + maze + flora + ore veins. Extracted
     * from the old inline body so the style dispatch in {@link #buildSurface}
     * reads top-down without nested branches.
     */
    private void applyCrystalSurface(ChunkAccess chunk, RandomSource random) {
        replaceTerrain(chunk);
        CrystalMaze.generate(chunk, random, chunk.getPos().getMinBlockX(), 25, chunk.getPos().getMinBlockZ());
        CrystalTreeGenerator.generate(chunk, random);
        generatePinkTourmaline(chunk, random);
        generateTigersEye(chunk, random);
        generateCrystalOres(chunk, random);
        placeCrystalFlowers(chunk, random);
        placeRice(chunk, random);
        placeQuinoa(chunk, random);
        placeCrystalTermites(chunk, random);
    }

    /**
     * Islands surface post-process — scraggly apple-leaf trees on the flat
     * dirt plane, faithful to 1.7.10 {@code ChunkProviderOreSpawn4.addScragglyTrees}
     * (orig ChunkProviderOreSpawn4.java:109-129): {@code 1 + nextInt(10)}
     * attempts per chunk (LessLag=0 default), each picking
     * {@code x/z = 2 + nextInt(12)} and scanning Y20 down to Y3 for a grass
     * block to root a {@link ScragglyTrees#scragglyTreeWithBranches} on.
     * The flat plane itself (bedrock Y0, dirt Y1-6, grass Y7,
     * orig ChunkProviderOreSpawn4.java:30-32) is data-driven via the
     * {@code orespawn:islands} noise settings.
     */
    private void applyIslandsSurface(ChunkAccess chunk, RandomSource random) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // orig ChunkProviderOreSpawn4.java:110 — howmany = 1 + nextInt(10)
        int attempts = 1 + random.nextInt(10);
        for (int i = 0; i < attempts; i++) {
            int x = 2 + minX + random.nextInt(12);
            int z = 2 + minZ + random.nextInt(12);
            // orig ChunkProviderOreSpawn4.java:123 — scan Y20 down to Y3 for grass below
            for (int y = 20; y > 2; y--) {
                if (chunk.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
                    ScragglyTrees.scragglyTreeWithBranches(chunk, random, x, y, z);
                    break;
                }
            }
        }
    }

    /**
     * Chaos surface post-process — scraggly tree scatter, faithful to 1.7.10
     * {@code ChunkProviderOreSpawn6.addScragglyTrees}
     * (orig ChunkProviderOreSpawn6.java:335-358): roll {@code 1 + nextInt(5)}
     * trees but only proceed on a 1-in-4 chunk gate; positions
     * {@code 2 + nextInt(12)}, scanning Y120 down to Y51 for grass.
     */
    private void applyChaosSurface(ChunkAccess chunk, RandomSource random) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // orig ChunkProviderOreSpawn6.java:336-339 — count rolled BEFORE the 1/4 gate
        int attempts = 1 + random.nextInt(5);
        if (random.nextInt(4) != 0) return;
        for (int i = 0; i < attempts; i++) {
            int x = 2 + minX + random.nextInt(12);
            int z = 2 + minZ + random.nextInt(12);
            // orig ChunkProviderOreSpawn6.java:352 — scan Y120 down to Y51
            for (int y = 120; y > 50; y--) {
                if (chunk.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.GRASS_BLOCK)) {
                    ScragglyTrees.scragglyTreeWithBranches(chunk, random, x, y, z);
                    break;
                }
            }
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);

        // Decoration (as opposed to buildSurface) runs after neighboring
        // chunks have also finished their terrain pass, so large
        // multi-chunk structures are safe to place here. Each style opts in
        // to its own decoration pass; failures are logged-and-ignored so a
        // single bad chunk never bricks worldgen.
        if (style == DimensionStyle.CRYSTAL) {
            try {
                RandomSource random = level.getRandom();
                CrystalStructures.generate(level, random,
                        chunk.getPos().getMinBlockX(), chunk.getPos().getMinBlockZ(),
                        this.crystalStructureCooldown);
            } catch (Exception e) {
                // Non-fatal: a failed structure in one chunk doesn't compromise
                // the world — but it must not be invisible either (BUG-021).
                org.slf4j.LoggerFactory.getLogger(OreSpawnChunkGenerator.class)
                        .warn("Crystal structure decoration failed for chunk {}", chunk.getPos(), e);
            }
        }

        // Audit Part 3 â€” Royal Altars no longer placed via this hook.
        // The previous RoyalAltars.tryPlaceRoyalAltar wrote ~12k blocks
        // through WorldGenLevel.setBlock during applyBiomeDecoration, which
        // (a) sheared at the 24-block WorldGenLevel write window for any
        // chunk past the centre and (b) silently dropped the entire portrait
        // wall pixel data (king[]/queen[]) the 1.7.10 source renders. Both
        // King's Altar and Queen's Altar are now first-class Structures
        // (LegacyDungeonStructure with dungeon_type=KING_ALTAR / QUEEN_ALTAR)
        // dispatched through the same multi-pass chunkBox.isInside stitching
        // pipeline as the rest of the audit-restored dungeons. See
        // src/main/resources/data/orespawn/worldgen/structure/{king,queen}_altar.json
        // and LegacyDungeonPiece#generateRoyalAltar for the byte-for-byte
        // port of GenericDungeon.makeKingAltar (line 4353) /
        // makeQueenAltar (line 5697).
    }

    /**
     * Replaces all vanilla terrain blocks with Crystal dimension equivalents.
     * The original 1.7.10 generated the entire chunk with CrystalStone as the base
     * block, with CrystalGrass on the surface. This post-processes the vanilla
     * noise terrain to achieve the same result.
     *
     * Also fills in shallow water (Y >= 56) with crystal stone to reduce the
     * oversized oceans that vanilla overworld noise creates. The original 1.7.10
     * dimension had custom noise that generated mostly land.
     */
    private void replaceTerrain(ChunkAccess chunk) {
        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                fillShallowWater(chunk, worldX, worldZ, crystalStone, crystalGrass, air);

                boolean hitSurface = false;
                for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
                    BlockPos pos = new BlockPos(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(pos);

                    if (state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                        continue;
                    }

                    if (state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    if (!hitSurface) {
                        hitSurface = true;
                        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SAND)
                                || state.is(Blocks.MYCELIUM) || state.is(Blocks.DIRT)) {
                            chunk.setBlockState(pos, crystalGrass, false);
                            continue;
                        }
                    }

                    if (isReplaceableStone(state)) {
                        chunk.setBlockState(pos, crystalStone, false);
                    }
                }
            }
        }
    }

    /**
     * Fills in shallow water columns with crystal stone + grass.
     * Scans downward; if water is found at Y >= 56, fills the entire water
     * column with crystal stone and places crystal grass on top.
     * Leaves deep water (seabed below Y 56) as actual water features.
     */
    private void fillShallowWater(ChunkAccess chunk, int worldX, int worldZ,
                                   BlockState crystalStone, BlockState crystalGrass, BlockState air) {
        int seabedY = -1;
        int waterTopY = -1;

        for (int y = 70; y >= chunk.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            BlockState state = chunk.getBlockState(pos);
            if (state.is(Blocks.WATER)) {
                if (waterTopY == -1) waterTopY = y;
                continue;
            }
            if (waterTopY != -1 && !state.isAir()) {
                seabedY = y;
                break;
            }
            if (!state.isAir()) break;
        }

        if (waterTopY == -1 || seabedY == -1) return;
        if (seabedY < 56) return;

        for (int y = seabedY + 1; y <= waterTopY; y++) {
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            chunk.setBlockState(pos, crystalStone, false);
        }
        BlockPos topPos = new BlockPos(worldX, waterTopY, worldZ);
        chunk.setBlockState(topPos, crystalGrass, false);
        BlockPos abovePos = new BlockPos(worldX, waterTopY + 1, worldZ);
        if (chunk.getBlockState(abovePos).is(Blocks.WATER)) {
            chunk.setBlockState(abovePos, air, false);
        }
    }

    private static boolean isReplaceableStone(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE) || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.SAND)
                || state.is(Blocks.SANDSTONE) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MYCELIUM);
    }

    /**
     * Pink Tourmaline columnar crystal formations.
     * 1/30 chance per chunk. Generates 1-10 crystal columns starting at Y=30-35,
     * growing upward at random angles. Each column is 1-2 blocks wide and 1-15 tall.
     */
    private void generatePinkTourmaline(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(30) != 1) return;

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockState crystal = ModBlocks.CRYSTAL_CRYSTAL.get().defaultBlockState();

        int startX = 3 + minX + random.nextInt(10);
        int startY = 30 + random.nextInt(5);
        int startZ = 3 + minZ + random.nextInt(10);
        int patches = 1 + random.nextInt(10);

        for (int i = 0; i < patches; i++) {
            float dx = random.nextFloat() - random.nextFloat();
            float dz = random.nextFloat() - random.nextFloat();
            float dy = 0.5f + random.nextFloat() / 2.0f;
            int width = random.nextInt(2);
            int length = 1 + width * 3 + random.nextInt(15);
            float rx = startX, ry = startY, rz = startZ;

            for (int iy = 0; iy <= length; iy++) {
                for (int ix = 0; ix <= width; ix++) {
                    for (int iz = 0; iz <= width; iz++) {
                        int bx = (int) (rx + ix);
                        int by = (int) ry;
                        int bz = (int) (rz + iz);
                        if (isInChunk(chunk, bx, bz)) {
                            setBlockInChunk(chunk, bx, by, bz, crystal);
                        }
                    }
                }
                ry += dy;
                rx += dx;
                rz += dz;
            }
        }
    }

    /**
     * Tiger's Eye columnar formations.
     * 1/30 chance per chunk. Generates 1-5 thin columns at Y=5-10.
     */
    private void generateTigersEye(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(30) != 1) return;

        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockState tigersEye = ModBlocks.TIGERS_EYE_ORE.get().defaultBlockState();

        int startX = 3 + minX + random.nextInt(10);
        int startY = 5 + random.nextInt(5);
        int startZ = 3 + minZ + random.nextInt(10);
        int patches = 1 + random.nextInt(5);

        for (int i = 0; i < patches; i++) {
            float dx = random.nextFloat() - random.nextFloat();
            float dz = random.nextFloat() - random.nextFloat();
            float dy = 0.5f + random.nextFloat() / 2.0f;
            int length = random.nextInt(6);
            float rx = startX, ry = startY, rz = startZ;

            for (int iy = 0; iy <= length; iy++) {
                int bx = (int) rx;
                int by = (int) ry;
                int bz = (int) rz;
                if (isInChunk(chunk, bx, bz)) {
                    setBlockInChunk(chunk, bx, by, bz, tigersEye);
                }
                ry += dy;
                rx += dx;
                rz += dz;
            }
        }
    }

    /**
     * Crystal ore veins: spawn blocks, crystal coal, crystal rat, crystal fairy.
     * Ported from ChunkProviderOreSpawn5.generateCrystalOres.
     */
    private void generateCrystalOres(ChunkAccess chunk, RandomSource random) {
        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();

        int spawnBlockPatches = 25 + random.nextInt(30);
        if (random.nextInt(20) == 0) spawnBlockPatches += 30;

        BlockState[] spawnBlocks = getSpawnBlockStates();

        for (int i = 0; i < spawnBlockPatches; i++) {
            int rx = 2 + chunk.getPos().getMinBlockX() + random.nextInt(12);
            int ry = random.nextInt(128);
            int rz = 2 + chunk.getPos().getMinBlockZ() + random.nextInt(12);
            if (ry <= 45) continue;
            BlockState block = spawnBlocks[random.nextInt(spawnBlocks.length)];
            generateOreVein(chunk, random, rx, ry, rz, block, 4, crystalStone);
        }

        int coalPatches = 3 + random.nextInt(8);
        BlockState crystalCoal = ModBlocks.CRYSTAL_COAL.get().defaultBlockState();
        for (int i = 0; i < coalPatches; i++) {
            int rx = 2 + chunk.getPos().getMinBlockX() + random.nextInt(12);
            int ry = random.nextInt(128);
            int rz = 2 + chunk.getPos().getMinBlockZ() + random.nextInt(12);
            generateOreVein(chunk, random, rx, ry, rz, crystalCoal, 6, crystalStone);
        }

        int ratPatches = 15 + random.nextInt(20);
        BlockState crystalRat = ModBlocks.CRYSTAL_RAT.get().defaultBlockState();
        for (int i = 0; i < ratPatches; i++) {
            int rx = 2 + chunk.getPos().getMinBlockX() + random.nextInt(12);
            int ry = random.nextInt(128);
            int rz = 2 + chunk.getPos().getMinBlockZ() + random.nextInt(12);
            if (ry >= 25) continue;
            generateOreVein(chunk, random, rx, ry, rz, crystalRat, 6, crystalStone);
        }

        int fairyPatches = 12 + random.nextInt(20);
        BlockState crystalFairy = ModBlocks.CRYSTAL_FAIRY.get().defaultBlockState();
        for (int i = 0; i < fairyPatches; i++) {
            int rx = 2 + chunk.getPos().getMinBlockX() + random.nextInt(12);
            int ry = random.nextInt(128);
            int rz = 2 + chunk.getPos().getMinBlockZ() + random.nextInt(12);
            if (ry >= 25) continue;
            generateOreVein(chunk, random, rx, ry, rz, crystalFairy, 6, crystalStone);
        }
    }

    /**
     * The 11 spawn-block egg ores that appear as veins above Y=45, in the exact
     * {@code nextInt(11)} switch order of orig ChunkProviderOreSpawn5.java:586-633:
     * Urchin, Flounder, Skate, Rotator, Peacock, Fairy, DungeonBeast, Vortex,
     * Rat, Whale, Irukandji. All are {@code OreGenericEgg} XP-drop blocks
     * (orig OreSpawnMain.java:6326-6336) — distinct from the CrystalRat /
     * CrystalFairy mob-spawning deep veins below Y25 (WGEN-023).
     */
    private BlockState[] getSpawnBlockStates() {
        return new BlockState[]{
                ModBlocks.ORE_URCHIN.get().defaultBlockState(),        // case 0
                ModBlocks.ORE_FLOUNDER.get().defaultBlockState(),      // case 1
                ModBlocks.ORE_SKATE.get().defaultBlockState(),         // case 2
                ModBlocks.ORE_ROTATOR.get().defaultBlockState(),       // case 3
                ModBlocks.ORE_PEACOCK.get().defaultBlockState(),       // case 4
                ModBlocks.ORE_FAIRY.get().defaultBlockState(),         // case 5
                ModBlocks.ORE_DUNGEON_BEAST.get().defaultBlockState(), // case 6
                ModBlocks.ORE_VORTEX.get().defaultBlockState(),        // case 7
                ModBlocks.ORE_RAT.get().defaultBlockState(),           // case 8
                ModBlocks.ORE_WHALE.get().defaultBlockState(),         // case 9
                ModBlocks.ORE_IRUKANDJI.get().defaultBlockState()      // case 10
        };
    }

    /**
     * Ellipsoid ore vein generator, ported from the original 1.7.10 generateOre method.
     * Creates a vein of `newBlock` replacing `targetBlock` in an ellipsoid shape.
     */
    private void generateOreVein(ChunkAccess chunk, RandomSource random,
                                  int posX, int posY, int posZ,
                                  BlockState newBlock, int veinSize, BlockState targetBlock) {
        float f = random.nextFloat() * (float) Math.PI;
        double d0 = posX + 8 + Mth.sin(f) * veinSize / 8.0f;
        double d1 = posX + 8 - Mth.sin(f) * veinSize / 8.0f;
        double d2 = posZ + 8 + Mth.cos(f) * veinSize / 8.0f;
        double d3 = posZ + 8 - Mth.cos(f) * veinSize / 8.0f;
        double d4 = posY + random.nextInt(3) - 2;
        double d5 = posY + random.nextInt(3) - 2;

        for (int l = 0; l <= veinSize; l++) {
            double cx = d0 + (d1 - d0) * l / veinSize;
            double cy = d4 + (d5 - d4) * l / veinSize;
            double cz = d2 + (d3 - d2) * l / veinSize;
            double radius = random.nextDouble() * veinSize / 16.0;
            double xr = (Mth.sin((float) (l * Math.PI / veinSize)) + 1.0f) * radius + 1.0;
            double yr = (Mth.sin((float) (l * Math.PI / veinSize)) + 1.0f) * radius + 1.0;

            int minBX = Mth.floor(cx - xr / 2.0);
            int minBY = Mth.floor(cy - yr / 2.0);
            int minBZ = Mth.floor(cz - xr / 2.0);
            int maxBX = Mth.floor(cx + xr / 2.0);
            int maxBY = Mth.floor(cy + yr / 2.0);
            int maxBZ = Mth.floor(cz + xr / 2.0);

            for (int bx = minBX; bx <= maxBX; bx++) {
                double dx2 = (bx + 0.5 - cx) / (xr / 2.0);
                if (dx2 * dx2 >= 1.0) continue;
                for (int by = minBY; by <= maxBY; by++) {
                    double dy2 = (by + 0.5 - cy) / (yr / 2.0);
                    if (dx2 * dx2 + dy2 * dy2 >= 1.0) continue;
                    for (int bz = minBZ; bz <= maxBZ; bz++) {
                        double dz2 = (bz + 0.5 - cz) / (xr / 2.0);
                        if (dx2 * dx2 + dy2 * dy2 + dz2 * dz2 >= 1.0) continue;
                        if (!isInChunk(chunk, bx, bz)) continue;
                        BlockPos p = new BlockPos(bx, by, bz);
                        if (chunk.getBlockState(p).equals(targetBlock)) {
                            chunk.setBlockState(p, newBlock, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Crystal flowers: 1/3 chance per chunk, 1-13 flowers of a single color.
     * Colors: red, green, blue, yellow (equal probability per cluster).
     */
    private void placeCrystalFlowers(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(3) != 0) return;

        int howmany = 1 + random.nextInt(13);
        int colorChoice = random.nextInt(4);
        BlockState flower = switch (colorChoice) {
            case 0 -> ModBlocks.CRYSTAL_FLOWER_RED.get().defaultBlockState();
            case 1 -> ModBlocks.CRYSTAL_FLOWER_GREEN.get().defaultBlockState();
            case 2 -> ModBlocks.CRYSTAL_FLOWER_BLUE.get().defaultBlockState();
            default -> ModBlocks.CRYSTAL_FLOWER_YELLOW.get().defaultBlockState();
        };
        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < howmany; i++) {
            int px = minX + random.nextInt(16);
            int pz = minZ + random.nextInt(16);
            for (int py = 128; py > 40; py--) {
                BlockPos pos = new BlockPos(px, py, pz);
                BlockPos below = new BlockPos(px, py - 1, pz);
                if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(below).equals(crystalGrass)) {
                    chunk.setBlockState(pos, flower, false);
                    break;
                }
            }
        }
    }

    /** Rice plants: 10% chance, up to 5 plants on CrystalGrass surface. */
    private void placeRice(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(10) != 0) return;

        BlockState rice = ModBlocks.RICE_PLANT.get().defaultBlockState();
        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < 5; i++) {
            int px = minX + random.nextInt(16);
            int pz = minZ + random.nextInt(16);
            for (int py = 128; py > 40; py--) {
                BlockPos pos = new BlockPos(px, py, pz);
                BlockPos below = new BlockPos(px, py - 1, pz);
                if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(below).equals(crystalGrass)) {
                    chunk.setBlockState(pos, rice, false);
                    break;
                }
            }
        }
    }

    /** Quinoa plants: 5% chance, up to 5 plants on CrystalGrass surface. */
    private void placeQuinoa(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(20) != 0) return;

        BlockState quinoa = ModBlocks.QUINOA_0.get().defaultBlockState();
        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < 5; i++) {
            int px = minX + random.nextInt(16);
            int pz = minZ + random.nextInt(16);
            for (int py = 128; py > 40; py--) {
                BlockPos pos = new BlockPos(px, py, pz);
                BlockPos below = new BlockPos(px, py - 1, pz);
                if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(below).equals(crystalGrass)) {
                    chunk.setBlockState(pos, quinoa, false);
                    break;
                }
            }
        }
    }

    /** Crystal Termite blocks: 1/40 chance, replaces CrystalGrass at 3 spots. */
    private void placeCrystalTermites(ChunkAccess chunk, RandomSource random) {
        if (random.nextInt(40) != 0) return;

        BlockState termiteBlock = ModBlocks.CRYSTAL_TERMITE_BLOCK.get().defaultBlockState();
        BlockState crystalGrass = ModBlocks.CRYSTAL_GRASS.get().defaultBlockState();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < 3; i++) {
            int px = minX + random.nextInt(16);
            int pz = minZ + random.nextInt(16);
            for (int py = 100; py > 50; py--) {
                BlockPos pos = new BlockPos(px, py, pz);
                BlockPos below = new BlockPos(px, py - 1, pz);
                if (chunk.getBlockState(pos).isAir() && chunk.getBlockState(below).equals(crystalGrass)) {
                    chunk.setBlockState(below, termiteBlock, false);
                    break;
                }
            }
        }
    }

    /**
     * Per-dimension dungeon dispatch, faithful to the 1.7.10
     * {@code OreSpawnWorld.generate} branches:
     *
     * <ul>
     *   <li><b>Utopia</b> (orig OreSpawnWorld.java:48-52): try the Ruby Bird
     *       dungeon first ({@code addRubyDungeon}, :1998-2012 — 1/15 gate, 8
     *       lava-seek attempts); only when it fails, roll the generic dungeon
     *       ({@code addGenericDungeon}, :2014-2029 — 1/16 gate).</li>
     *   <li><b>Mining</b> (orig OreSpawnWorld.java:79-104): the generic dungeon
     *       runs in the else-branch of the 1/95 large-structure roll, i.e. in
     *       ~664/665 of chunks — approximated here as an unconditional 1/16
     *       roll (the large structures are data-driven structure sets now).</li>
     *   <li><b>Village</b> (orig OreSpawnWorld.java:120): generic dungeon every
     *       chunk (1/16 gate).</li>
     *   <li><b>Islands</b> (orig OreSpawnWorld.java:134-139): the D4 generic
     *       dungeon takes 4/19 of the 1/100 structure roll → 1/475 chunks,
     *       surfaced at grass level ({@code addD4GenericDungeon}, :2438-2452).</li>
     *   <li><b>Crystal / Chaos / overworld:</b> no generic or ruby dungeons in
     *       the original — none here. (The pre-C7 port wrongly rolled a Ruby
     *       dungeon in Crystal and generic dungeons everywhere; WGEN-034/035.)</li>
     * </ul>
     *
     * <p>The original applies no {@code recently_placed} cooldown and no
     * {@code DisableOverworldDungeons} gate to these dungeons (the config flag
     * only guards the overworld surface set, orig OreSpawnWorld.java:284), so
     * neither is checked here.</p>
     */
    private void placeDungeons(WorldGenRegion region, ChunkAccess chunk) {
        RandomSource random = region.getRandom();
        int cx = chunk.getPos().getMinBlockX();
        int cz = chunk.getPos().getMinBlockZ();

        switch (style) {
            case UTOPIA -> {
                if (!GenericDungeon.tryPlaceRubyDungeon(region, random, cx, cz)) {
                    GenericDungeon.tryPlaceGenericDungeon(region, random, cx, cz);
                }
            }
            case MINING, VILLAGE -> GenericDungeon.tryPlaceGenericDungeon(region, random, cx, cz);
            case ISLANDS -> GenericDungeon.tryPlaceIslandsGenericDungeon(region, random, cx, cz);
            default -> {
                // Crystal, Chaos, DEFAULT: no dungeons (orig parity).
            }
        }
    }

    private static boolean isInChunk(ChunkAccess chunk, int worldX, int worldZ) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        return worldX >= minX && worldX < minX + 16 && worldZ >= minZ && worldZ < minZ + 16;
    }

    private static void setBlockInChunk(ChunkAccess chunk, int x, int y, int z, BlockState state) {
        if (isInChunk(chunk, x, z)) {
            chunk.setBlockState(new BlockPos(x, y, z), state, false);
        }
    }
}
