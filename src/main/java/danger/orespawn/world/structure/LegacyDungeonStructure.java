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
            Codec.STRING.fieldOf("dungeon_type").forGetter(s -> s.dungeonType.name())
    ).apply(inst, (settings, name) -> new LegacyDungeonStructure(settings, LegacyDungeonPiece.DungeonType.valueOf(name))));

    private final LegacyDungeonPiece.DungeonType dungeonType;

    public LegacyDungeonStructure(StructureSettings settings, LegacyDungeonPiece.DungeonType dungeonType) {
        super(settings);
        this.dungeonType = dungeonType;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        BlockPos origin = switch (dungeonType.placement) {
            case SURFACE_CENTER -> surfaceCenterOrigin(context);
            case LOWEST_SURFACE_36 -> lowestSurfaceOrigin(context);
            case ISLANDS_GRASS -> islandsGrassOrigin(context);
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
