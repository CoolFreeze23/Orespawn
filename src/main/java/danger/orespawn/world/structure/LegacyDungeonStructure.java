package danger.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Audit Part 2 &mdash; dedicated {@link Structure} for the four legacy
 * Tech &amp; Danger dungeons (Shadow, Greenhouse, Robot Lab, White House)
 * that the audit flagged as procedural fabrications.
 *
 * <p>The previous {@link FeatureStructure} wrapper around their respective
 * {@code *Feature} classes ran the entire generator inside a single
 * {@code WorldGenLevel} write window (~24-block radius), which forced
 * every prior implementation to either (a) shrink the legacy footprint
 * to fit (procedural fabrication) or (b) silently get sheared at chunk
 * borders. The Phase 13C-fix Royal Trees solved the same problem with a
 * dedicated {@link RoyalTreePiece} that uses the canonical Mansion
 * multi-pass {@code chunkBox.isInside} stitching pattern; this class is
 * the same fix applied to the four big dungeons via a single shared
 * {@link LegacyDungeonPiece} dispatched on a {@link
 * LegacyDungeonPiece.DungeonType} enum.</p>
 *
 * <p>JSON usage: {@code "type": "orespawn:legacy_dungeon",
 * "dungeon_type": "SHADOW" | "GREENHOUSE" | "ROBOT_LAB" | "WHITE_HOUSE"}.</p>
 */
public class LegacyDungeonStructure extends Structure {

    public static final MapCodec<LegacyDungeonStructure> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            settingsCodec(inst),
            Codec.STRING.fieldOf("dungeon_type").forGetter(s -> s.dungeonType.name()),
            // Phase D6a — optional per-JSON anchor override for dungeon types
            // that generate in more than one dimension with different original
            // anchors (Ender Castle: END_SURFACE in the End, ISLANDS_GRASS on
            // the Islands D4 roll — orig OSW:1557-1570 vs :2322-2343). Omit to
            // use the DungeonType's default mode.
            Codec.STRING.optionalFieldOf("placement_mode").forGetter(s ->
                    s.placementOverride.map(Enum::name))
    ).apply(inst, (settings, name, mode) -> new LegacyDungeonStructure(settings,
            LegacyDungeonPiece.DungeonType.valueOf(name),
            mode.map(LegacyDungeonPiece.DungeonType.PlacementMode::valueOf))));

    private final LegacyDungeonPiece.DungeonType dungeonType;
    private final Optional<LegacyDungeonPiece.DungeonType.PlacementMode> placementOverride;

    public LegacyDungeonStructure(StructureSettings settings, LegacyDungeonPiece.DungeonType dungeonType) {
        this(settings, dungeonType, Optional.empty());
    }

    public LegacyDungeonStructure(StructureSettings settings, LegacyDungeonPiece.DungeonType dungeonType,
                                  Optional<LegacyDungeonPiece.DungeonType.PlacementMode> placementOverride) {
        super(settings);
        this.dungeonType = dungeonType;
        this.placementOverride = placementOverride;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        BlockPos origin = switch (placementOverride.orElse(dungeonType.placement)) {
            case SURFACE_CENTER -> surfaceCenterOrigin(context);
            case LOWEST_SURFACE_36 -> lowestSurfaceOrigin(context);
            case ISLANDS_GRASS -> islandsGrassOrigin(context);
            case END_SURFACE -> endSurfaceOrigin(context);
            case OCEAN_SURFACE -> oceanSurfaceOrigin(context);
            case OCEAN_SURFACE_AIR -> {
                // Play Pool (orig OreSpawnWorld.java:1146-1148): identical scan
                // to Monster Island's, but the anchor is the AIR block above
                // the water surface.
                BlockPos water = oceanSurfaceOrigin(context);
                yield water == null ? null : water.above();
            }
            case SKY_BAND_150 -> skyBand150Origin(context);
            case SWAMP_GRASS_SURFACE -> swampGrassSurfaceOrigin(context);
            case SAND_SURFACE_MINUS1 -> {
                // Bouncy Castle (orig OreSpawnWorld.java:1280-1299): identical
                // scan shape to the swamp-grass one, but the builder receives
                // posY−1 — the SAND block itself (:1292). Sand identity is
                // carried by the desert biome list the same way grass was by
                // the swamp tag.
                BlockPos air = swampGrassSurfaceOrigin(context);
                yield air == null ? null : air.below();
            }
            case VILLAGE_GRASS_SURFACE -> villageGrassSurfaceOrigin(context);
            case ISLANDS_GRASS_AIR -> {
                // Pumpkin (orig OreSpawnWorld.java:2416): the D4 i-roll hands
                // makePumpkin the AIR block above the grass anchor.
                BlockPos grass = islandsGrassOrigin(context);
                yield grass == null ? null : grass.above();
            }
            case SKY_BAND_70 -> skyBand70Origin(context);
        };
        if (origin == null) return Optional.empty();
        if (origin.getY() + dungeonType.upExtent + 4 >= context.heightAccessor().getMaxBuildHeight()) {
            return Optional.empty();
        }
        BlockPos finalOrigin = origin;
        return Optional.of(new GenerationStub(finalOrigin, builder ->
                builder.addPiece(new LegacyDungeonPiece(finalOrigin, dungeonType))));
    }

    /**
     * Original Audit Part 2-4 anchoring: chunk-centre heightmap probe with a
     * Y-bound rejection so the structure has room to build up (Greenhouse +12,
     * RobotLab +40, WhiteHouse +25, ShadowDungeon -10/+10).
     */
    private BlockPos surfaceCenterOrigin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        int y = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        if (y <= context.heightAccessor().getMinBuildHeight() + 12) return null;
        return new BlockPos(x, y, z);
    }

    /**
     * Port of {@code OreSpawnWorld.addBasiliskMaze}'s ground scan
     * (orig OreSpawnWorld.java:2573-2597): sample the chunk at column offsets
     * {@code i, j ∈ {0, 3, 6, 9, 12, 15}} (:2578-2581), find each column's
     * topmost solid block within the original's Y 128→31 scan window (:2582-2583,
     * here the noise-predicted {@code WORLD_SURFACE_WG} height − 1, since
     * structure starts resolve before terrain blocks exist), keep the column
     * whose surface is strictly lowest — first seen wins ties, matching
     * {@code if (posY >= lowestY) continue} (:2584-2588) against the
     * {@code lowestY = 128} initialiser (:2574) — and refuse unless
     * {@code lowestY > 40} (:2593). The origin sinks 2 blocks into the terrain
     * ({@code lowestY - 2}, :2594), which buries the pyramid's ground ring.
     */
    private BlockPos lowestSurfaceOrigin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int lowestSurfaceY = 128;
        int lowestX = chunk.getMinBlockX();
        int lowestZ = chunk.getMinBlockZ();
        boolean found = false;
        for (int xOff = 0; xOff <= 15; xOff += 3) {
            for (int zOff = 0; zOff <= 15; zOff += 3) {
                int x = chunk.getMinBlockX() + xOff;
                int z = chunk.getMinBlockZ() + zOff;
                int surfaceY = context.chunkGenerator().getBaseHeight(
                        x, z, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState()) - 1;
                // orig :2582 — the scan visits Y 128 down to 31. In the 128-tall
                // 1.7.10 world no surface sat above the window; modern terrain
                // can, and the original's block scan could then still find a
                // cave floor/overhang inside the window. Structure starts
                // resolve before blocks exist, so the noise surface is all we
                // have — columns whose predicted surface is above Y128 simply
                // don't qualify (documented delta, D5 report §2).
                if (surfaceY > 128 || surfaceY < 31) continue;
                if (surfaceY >= lowestSurfaceY) continue;
                lowestSurfaceY = surfaceY;
                lowestX = x;
                lowestZ = z;
                found = true;
            }
        }
        if (!found || lowestSurfaceY <= 40) return null;
        return new BlockPos(lowestX, lowestSurfaceY - 2, lowestZ);
    }

    /**
     * The End-dimension anchor shared by {@code addEnderCastle}
     * (orig OreSpawnWorld.java:1557-1570) and {@code addHospital} (:1542-1555):
     * up to 3 attempts (:1561/:1546) of {@code chunk + nextInt(16)} jitter
     * (:1562-1563/:1547-1548); accept a column whose surface is air directly
     * on end stone within the original's Y 90→11 downward scan (:1564-1565/
     * :1549-1550). Structure starts resolve before blocks exist, so the probes
     * map to the noise heightmap: first-free-Y = the air block sitting on the
     * end-stone surface (void columns predict the world floor and are
     * rejected). The originals' flat clearance planes (quickBigSpaceCheck
     * 30×30 at +8, OSW:2635-2643; quickSpaceCheck 12×12 at +4, :2625-2633)
     * are approximated by requiring the footprint's sampled corner/centre
     * surfaces not to rise more than 3 blocks above the anchor — conservative
     * for the castle's looser +8 plane; documented in the D6a report.
     * Frequency (the 1/4 dimension roll × 1/50 / 1/25 gates) maps to the
     * structure-set spacing per the C7 equivalence.
     */
    private BlockPos endSurfaceOrigin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        for (int attempt = 0; attempt < 3; attempt++) {
            int x = chunk.getMinBlockX() + context.random().nextInt(16);
            int z = chunk.getMinBlockZ() + context.random().nextInt(16);
            int firstFree = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            // Void column — no end-stone surface to sit on.
            if (firstFree <= context.heightAccessor().getMinBuildHeight() + 1) continue;
            // orig :1564/:1549 — the scan only visits Y 90 down to 11.
            if (firstFree > 90 || firstFree < 11) continue;
            if (!footprintClearAbove(context, x, z, firstFree)) continue;
            return new BlockPos(x, firstFree, z);
        }
        return null;
    }

    /**
     * Port of {@code OreSpawnWorld.addMonsterIsland}'s anchoring
     * (orig OreSpawnWorld.java:1398-1412): the corner-biome "Ocean" gate maps
     * to the structure's biome tag; up to 4 attempts (:1404) of in-chunk
     * jitter (:1405-1406); the original scans Y 100→41 for air directly above
     * STILL WATER (:1408-1409) and anchors at the water-surface block itself
     * ({@code posY - 1}, :1410). Modern mapping: water surface = the noise
     * first-free-Y minus 1; a water column exists iff the ocean-floor
     * heightmap sits below it.
     */
    private BlockPos oceanSurfaceOrigin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        for (int attempt = 0; attempt < 4; attempt++) {
            int x = chunk.getMinBlockX() + context.random().nextInt(16);
            int z = chunk.getMinBlockZ() + context.random().nextInt(16);
            int firstFree = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            int surfaceY = firstFree - 1;
            // orig :1408-1410 — the scan visits posY 100 down to 41 and anchors
            // at posY−1, so the legal water-surface anchor band is Y 40..99.
            if (surfaceY > 99 || surfaceY < 40) continue;
            int floorY = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            // Land column — the surface block is terrain, not water.
            if (floorY >= firstFree) continue;
            return new BlockPos(x, surfaceY, z);
        }
        return null;
    }

    /**
     * Port of {@code addD4CloudShark}'s anchoring
     * (orig OreSpawnWorld.java:2423-2428): no scan at all — X/Z at
     * {@code chunk + 4 + nextInt(8)}, Y in the 150..159 sky band,
     * unconditional success (the original had no cooldown or space check on
     * this path either; frequency lives in the structure set). The original's
     * mixed-RNG quirk (Y from the world RNG, X/Z from the passed one) is a
     * single-stream draw here — no cross-pass hazard, since structure starts
     * draw from one seeded random anyway.
     */
    private BlockPos skyBand150Origin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 4 + context.random().nextInt(8);
        int z = chunk.getMinBlockZ() + 4 + context.random().nextInt(8);
        int y = 150 + context.random().nextInt(10);
        return new BlockPos(x, y, z);
    }

    /**
     * Port of {@code addSpitBugLair}'s anchoring
     * (orig OreSpawnWorld.java:1236-1256): the "Swampland" corner-biome gate
     * maps to the structure's biome tag (minecraft:swamp only — mangrove has
     * no 1.7.10 analogue); up to 4 attempts (:1244) of chunk + nextInt(16)
     * jitter (:1245-1246); the original scans Y 100→41 for air directly above
     * GRASS (:1248-1249). Grass identity is not predictable before terrain
     * exists, so the accept test is the noise surface inside the window AND a
     * dry column (surface heightmap == ocean-floor heightmap — rejects the
     * swamp's water sheets exactly where the original's grass test did) —
     * documented mapping delta, END_SURFACE-style.
     */
    private BlockPos swampGrassSurfaceOrigin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        for (int attempt = 0; attempt < 4; attempt++) {
            int x = chunk.getMinBlockX() + context.random().nextInt(16);
            int z = chunk.getMinBlockZ() + context.random().nextInt(16);
            int firstFree = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            // orig :1248 — the scan only visits Y 100 down to 41.
            if (firstFree > 100 || firstFree < 41) continue;
            int floorY = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            // Wet column — the original required grass below, never water.
            if (floorY != firstFree) continue;
            return new BlockPos(x, firstFree, z);
        }
        return null;
    }

    /**
     * Port of {@code addDamselInDistress}'s anchoring
     * (orig OreSpawnWorld.java:1301-1317): the Village dimension's grass scan
     * — up to 4 attempts (:1305) of chunk + nextInt(16) jitter (:1306-1307),
     * air-over-GRASS scan Y 100→41 (:1309-1310) with the same dry-column
     * grass approximation as {@link #swampGrassSurfaceOrigin} — but the
     * builder receives {@code posY - 1}, the GRASS block itself (:1311), and
     * the original also requires {@code quickSpaceCheck(posX, posY - 1,
     * posZ)} — a 12×12 all-air plane at anchor+4, offsets −2..+9 on both
     * axes (:2625-2633) — which maps to the END_SURFACE-style
     * {@link #footprintClearAbove} approximation (the same pairing
     * {@code endSurfaceOrigin} uses for the hospital's identical probe).
     */
    private BlockPos villageGrassSurfaceOrigin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        for (int attempt = 0; attempt < 4; attempt++) {
            int x = chunk.getMinBlockX() + context.random().nextInt(16);
            int z = chunk.getMinBlockZ() + context.random().nextInt(16);
            int firstFree = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            // orig :1309 — the scan only visits Y 100 down to 41.
            if (firstFree > 100 || firstFree < 41) continue;
            int floorY = context.chunkGenerator().getBaseHeight(
                    x, z, Heightmap.Types.OCEAN_FLOOR_WG,
                    context.heightAccessor(), context.randomState());
            // Wet column — the original required grass below, never water.
            if (floorY != firstFree) continue;
            int anchorY = firstFree - 1;                     // orig :1311
            if (!footprintClearAbove(context, x, z, anchorY)) continue;
            return new BlockPos(x, anchorY, z);
        }
        return null;
    }

    /**
     * Port of {@code addD4Rainbow}'s anchoring
     * (orig OreSpawnWorld.java:2430-2436): a line-for-line clone of
     * {@link #skyBand150Origin} in the 70..89 band. The original drew Y from
     * the WORLD RNG while X/Z came from the chunk random (:2434); that
     * mixed-stream quirk collapses into the single seeded structure random,
     * the same documented delta as the Cloud Shark's.
     */
    private BlockPos skyBand70Origin(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 4 + context.random().nextInt(8);
        int z = chunk.getMinBlockZ() + 4 + context.random().nextInt(8);
        int y = 70 + context.random().nextInt(20);
        return new BlockPos(x, y, z);
    }

    /**
     * Noise-level approximation of the originals' flat air-plane clearance
     * probes (see {@link #endSurfaceOrigin}): the four footprint corners and
     * the centre must not predict terrain rising more than 3 blocks above the
     * anchor.
     */
    private boolean footprintClearAbove(GenerationContext context, int x, int z, int anchorY) {
        int[][] samples = {
                {x + dungeonType.minXOff, z + dungeonType.minZOff},
                {x + dungeonType.maxXOff, z + dungeonType.minZOff},
                {x + dungeonType.minXOff, z + dungeonType.maxZOff},
                {x + dungeonType.maxXOff, z + dungeonType.maxZOff},
                {x + (dungeonType.minXOff + dungeonType.maxXOff) / 2,
                 z + (dungeonType.minZOff + dungeonType.maxZOff) / 2},
        };
        for (int[] sample : samples) {
            int surface = context.chunkGenerator().getBaseHeight(
                    sample[0], sample[1], Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(), context.randomState());
            if (surface > anchorY + 3) return false;
        }
        return true;
    }

    /**
     * Port of {@code OreSpawnWorld.addD4NightmareRookery}'s anchoring
     * (orig OreSpawnWorld.java:2253-2274): with {@code LessLag != 0} skip half
     * of all attempts (:2254-2256); jitter the position by {@code nextInt(8)}
     * from the chunk corner (:2257-2258); anchor AT the grass block found by
     * the original's Y 20→5 downward scan (:2259-2261) — on the flat Islands
     * plane (grass Y7 via the {@code orespawn:islands} noise settings) the
     * noise-predicted heightmap − 1 is exactly that grass block. The original's
     * {@code D4BigSpaceCheck} air probe and shared {@code recently_placed}
     * cooldown belong to the structure-set spacing machinery, per the Phase C7
     * treatment of the other Islands structures.
     */
    private BlockPos islandsGrassOrigin(GenerationContext context) {
        if (danger.orespawn.OreSpawnConfig.LESS_LAG.get() != 0 && context.random().nextInt(2) != 0) {
            return null;
        }
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + context.random().nextInt(8);
        int z = chunk.getMinBlockZ() + context.random().nextInt(8);
        int grassY = context.chunkGenerator().getBaseHeight(
                x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState()) - 1;
        // orig :2259 — the grass scan only visits Y 20 down to 5.
        if (grassY > 20 || grassY < 5) return null;
        return new BlockPos(x, grassY, z);
    }

    @Override
    public StructureType<?> type() {
        return ModStructureTypes.LEGACY_DUNGEON.get();
    }
}
