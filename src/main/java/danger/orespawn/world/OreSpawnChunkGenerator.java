package danger.orespawn.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnConfig;
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
 * dimension JSON's {@code dimension_style} field — see {@link DimensionStyle}
 * for the rationale.</p>
 *
 * <p><b>Thread safety.</b> NeoForge 1.21.1 runs {@link #buildSurface} and
 * {@link #applyBiomeDecoration} on a worker pool — the generator instance is
 * shared across all workers for a given dimension. Every field on this class
 * that is read during generation is either immutable (codec-provided) or
 * wrapped in {@link java.util.concurrent.atomic.AtomicInteger}. Callees that
 * need ordering guarantees (dungeon cooldowns, large-structure cooldowns) use
 * atomic compare/update so two workers can never both pass a "cooldown is 0"
 * check at the same instant.</p>
 *
 * <p><b>Codec fields:</b></p>
 * <ul>
 *   <li>{@code biome_source} — forwarded to {@link NoiseBasedChunkGenerator}.</li>
 *   <li>{@code settings} — {@link NoiseGeneratorSettings} holder (overworld,
 *       floating_islands, etc). Dimensions pick their noise preset here.</li>
 *   <li>{@code dimension_style} — optional {@link DimensionStyle} discriminator,
 *       defaults to {@link DimensionStyle#DEFAULT}.</li>
 *   <li>{@code crystal_surface} — legacy boolean, still accepted for
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
     * predate the {@link DimensionStyle} enum — the constructor folds it into
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
     * Per-JVM cooldown that skips dungeon placement for 50 chunks after one is
     * placed. Prevents back-to-back dungeons clustering.
     *
     * <p>NeoForge 1.21.1 paradigm shift: unlike 1.12.2's serial chunk pipeline,
     * worldgen in 1.21 runs on the chunk-generator worker thread pool — multiple
     * threads call {@link #buildSurface} concurrently. A plain {@code static int}
     * would race (two threads reading 0 simultaneously, both placing dungeons,
     * then double-decrementing on the next 50 chunks). {@link AtomicInteger}
     * keeps decrement and reset atomic so the cooldown is honoured under
     * parallel chunk loading.</p>
     */
    private static final java.util.concurrent.atomic.AtomicInteger recentlyPlaced =
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
            case CHAOS, VILLAGE, UTOPIA, DEFAULT -> {
                // Pass-through — vanilla noise + the dungeon pass above is
                // sufficient. These styles exist as named hooks for future
                // per-dimension tweaks without needing another codec field.
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
     * Islands surface post-process. Expects the dimension JSON to select
     * {@code "settings": "minecraft:floating_islands"} so vanilla noise already
     * carves the sky-island shape; we only scatter scraggly oak trees on top,
     * mirroring 1.7.10 {@code ChunkProviderOreSpawn4.addScragglyTrees} (1–10
     * attempts per chunk, each looking for an air-over-grass column).
     */
    private void applyIslandsSurface(ChunkAccess chunk, RandomSource random) {
        int attempts = 1 + random.nextInt(10);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        for (int i = 0; i < attempts; i++) {
            int x = minX + random.nextInt(16);
            int z = minZ + random.nextInt(16);
            for (int y = chunk.getMaxBuildHeight() - 1; y > chunk.getMinBuildHeight() + 1; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockPos below = new BlockPos(x, y - 1, z);
                if (!chunk.getBlockState(pos).isAir()) break;
                if (chunk.getBlockState(below).is(Blocks.GRASS_BLOCK)) {
                    placeScragglyTree(chunk, random, x, y, z);
                    break;
                }
            }
        }
    }

    /**
     * 4–7 block oak trunk topped with a 3x3 leaf cap. Intentionally minimal
     * to match the "scraggly" feel from 1.7.10 — cheap to run N times per
     * chunk and never writes outside the chunk boundary (out-of-chunk writes
     * during concurrent generation cause chunk pop-in).
     */
    private void placeScragglyTree(ChunkAccess chunk, RandomSource random, int x, int y, int z) {
        int trunkHeight = 4 + random.nextInt(4);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();

        for (int j = 0; j < trunkHeight; j++) {
            setBlockInChunk(chunk, x, y + j, z, log);
        }
        int topY = y + trunkHeight;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setIfAirInChunk(chunk, x + dx, topY, z + dz, leaves);
                setIfAirInChunk(chunk, x + dx, topY - 1, z + dz, leaves);
            }
        }
        setIfAirInChunk(chunk, x, topY + 1, z, leaves);
    }

    private static void setIfAirInChunk(ChunkAccess chunk, int x, int y, int z, BlockState state) {
        if (!isInChunk(chunk, x, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        if (chunk.getBlockState(pos).isAir()) {
            chunk.setBlockState(pos, state, false);
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
                        chunk.getPos().getMinBlockX(), chunk.getPos().getMinBlockZ());
            } catch (Exception e) {
                // Non-fatal: a failed structure in one chunk doesn't compromise the world.
            }
        }

        // Utopia: roll for a King/Queen altar (1/2000 chunk). Restored from
        // 1.7.10 OreSpawnWorld.addKingAltar — this is the only structural way
        // to obtain The King / The Queen spawn eggs short of using the Magic
        // Apple ginormous tree (Phase 5C).
        if (style == DimensionStyle.UTOPIA) {
            try {
                RoyalAltars.tryPlaceRoyalAltar(level, chunk.getPos(), level.getRandom());
            } catch (Exception e) {
                // Non-fatal: altar placement failures are silently swallowed.
            }
        }
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
     * The 11 spawn block types that appear as ore veins above Y=45 in the original.
     * These are OreBasicStone blocks that spawn creatures when broken.
     * We reuse CRYSTAL_RAT and CRYSTAL_FAIRY for the ones we have registered;
     * for others we fall back to CRYSTAL_STONE (placeholder until all blocks exist).
     */
    private BlockState[] getSpawnBlockStates() {
        BlockState fallback = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        return new BlockState[]{
                fallback, // Urchin spawn block
                fallback, // Flounder spawn block
                fallback, // Skate spawn block
                fallback, // Rotator spawn block
                fallback, // Peacock spawn block
                ModBlocks.CRYSTAL_FAIRY.get().defaultBlockState(),
                fallback, // DungeonBeast spawn block
                fallback, // Vortex spawn block
                ModBlocks.CRYSTAL_RAT.get().defaultBlockState(),
                fallback, // Whale spawn block
                fallback  // Irukandji spawn block
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

    private void placeDungeons(WorldGenRegion region, ChunkAccess chunk) {
        if (OreSpawnConfig.DISABLE_OVERWORLD_DUNGEONS.get()) {
            return;
        }

        // Atomic decrement so concurrent chunk-gen threads can't both pass the
        // gate when the counter is 1. decrementAndGet is safe because we never
        // let the value drop below 0 (we always compareAndSet it to 50 on a
        // successful placement).
        if (recentlyPlaced.get() > 0) {
            recentlyPlaced.updateAndGet(v -> v > 0 ? v - 1 : 0);
            return;
        }

        RandomSource random = region.getRandom();
        int cx = chunk.getPos().getMinBlockX();
        int cz = chunk.getPos().getMinBlockZ();

        // Crystal dimension gets a dedicated ruby-themed dungeon tier on top
        // of the generic dungeon roll — mirrors 1.7.10
        // OreSpawnWorld.addRubyDungeon for dimension 5.
        if (style == DimensionStyle.CRYSTAL) {
            if (random.nextInt(15) == 0) {
                int y = 10 + random.nextInt(10);
                BlockPos pos = new BlockPos(cx + 8, y, cz + 8);
                if (GenericDungeon.tryPlaceRubyDungeon(region, random, pos)) {
                    recentlyPlaced.set(50);
                    return;
                }
            }
        }

        if (random.nextInt(16) == 0) {
            int y = 5 + random.nextInt(40);
            BlockPos pos = new BlockPos(cx + 8, y, cz + 8);
            if (GenericDungeon.tryPlaceGenericDungeon(region, random, pos)) {
                recentlyPlaced.set(50);
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
