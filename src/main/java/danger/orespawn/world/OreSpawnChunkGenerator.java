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
 * Custom chunk generator for OreSpawn dimensions. Extends vanilla's noise-based
 * generator so terrain shape (caves, height variation, biome blending) still
 * benefits from 1.21.1's noise router pipeline, but adds:
 *
 * <ul>
 *   <li>Post-terrain block replacement: vanilla stone/grass/dirt become
 *       CrystalStone and CrystalGrass to match the original 1.7.10 palette.</li>
 *   <li>Shallow-water fill: since this generator reuses the overworld noise
 *       settings (which carve huge oceans) we convert shallow seas to land so
 *       the Crystal dimension is mostly walkable, like the original.</li>
 *   <li>Custom per-chunk features: mazes, crystal trees, flora, ore veins.</li>
 *   <li>Cross-chunk structures (battle towers, haunted houses) placed in
 *       {@link #applyBiomeDecoration} so neighboring chunks are stable.</li>
 * </ul>
 *
 * <p>Codec: exposed via {@link #CODEC} using {@link RecordCodecBuilder}. The
 * {@code crystal_surface} flag is the only OreSpawn-specific field; when false
 * this generator just runs dungeon placement (used by the other five dimensions
 * which use the stock overworld look).</p>
 */
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
    /**
     * Per-JVM cooldown that skips dungeon placement for 50 chunks after one is
     * placed. Prevents back-to-back dungeons clustering. Static is acceptable
     * because chunk generation for a single dimension is effectively serialized
     * and this is a soft rate-limiter, not a correctness guard.
     */
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

        try {
            placeDungeons(region, chunk);
        } catch (Exception e) {
            // Dungeon placement can fail during early dimension generation (e.g.
            // when neighboring chunks referenced for solidity checks haven't been
            // generated yet). Failing silently here is correct: dungeons are
            // decorative and a missed chunk just means no dungeon in that chunk.
        }

        if (!crystalSurface) return;

        RandomSource random = region.getRandom();

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

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);

        if (!crystalSurface) return;

        try {
            // Called in decoration (not surface-build) so adjacent chunks are
            // generated; this lets large structures (Battle Tower radius=10,
            // Haunted House 7x7) safely straddle chunk borders.
            RandomSource random = level.getRandom();
            CrystalStructures.generate(level, random,
                    chunk.getPos().getMinBlockX(), chunk.getPos().getMinBlockZ());
        } catch (Exception e) {
            // Non-fatal — a failed structure in one chunk doesn't compromise the world.
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
