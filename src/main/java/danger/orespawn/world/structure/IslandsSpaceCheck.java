package danger.orespawn.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.HashMap;
import java.util.Map;

/**
 * WGEN-120 (GitHub #8): the Islands' space checks, which the one roll of WGEN-080 counted only as a share of its odds.
 *
 * <p>1.7.10 built an Islands structure only where {@code D4BigSpaceCheck} (orig OreSpawnWorld.java:2655-2664) found
 * Y11 clear (air, logs or apple and scary leaves) over x -25 to 39 and z -25 to 29 from the chunk's corner (the roll
 * passes posY 7, the check reads posY + 4, :134), and a tower only where {@code addD4Castle} (:2209-2218) found air 18
 * over its grass across x -20 to 32 and z -4 to 32 from its site. So a structure was never built into one already
 * standing: the later of two turned away. WGEN-080 put the 4.5% of builds those checks turned away into the weights
 * of {@code islands_structures} and refused none, so two of its spots a chunk apart built into each other, two
 * towers among them.
 *
 * <p>Here a spot of the set is refused where an earlier spot among its eight neighbours is built and that structure's
 * envelope ({@link LegacyDungeonPiece.DungeonType}'s offsets about its site) meets either check's box at its height.
 * "Earlier" stands in for the original's populate order, which followed the player: each cell's {@link #priority},
 * a mix of the seed and the cell, the lower first. A neighbour is built when its pick finds its site and no earlier
 * neighbour of its own refuses it; the chain runs only along spots whose boxes meet, so it stays short. Cells two
 * apart lie ten chunks or more apart, beyond every envelope's reach, so the eight neighbours are all there is. The
 * structure pass, /locate and the census all ask {@code findGenerationPoint}, so they agree. The weights carry the
 * original's odds before the checks again.
 */
public final class IslandsSpaceCheck {

    static final ResourceKey<StructureSet> SET = ResourceKey.create(Registries.STRUCTURE_SET,
            ResourceLocation.fromNamespaceAndPath("orespawn", "islands_structures"));
    /** orig :134, D4BigSpaceCheck(world, x, 7, z) reading posY + 4. */
    static final int D4_Y = 11;
    /** orig :2214, addD4Castle reading posY + 18 over its grass. */
    static final int TOWER_RISE = 18;

    private IslandsSpaceCheck() {
    }

    /**
     * Whether the structure pass leaves {@code self}'s spot empty: the context's chunk is a spot of the Islands set,
     * {@code self} its first pick there, and an earlier, built neighbour stands in one of the original's checks.
     */
    static boolean refused(Structure.GenerationContext context, LegacyDungeonStructure self) {
        StructureSet set = context.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(SET);
        if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) return false;
        ChunkPos chunk = context.chunkPos();
        if (!placement.getPotentialStructureChunk(context.seed(), chunk.x, chunk.z).equals(chunk)) return false;
        if (StructurePicks.firstPick(set, context.seed(), chunk).structure().value() != self) return false;
        Decider decider = new Decider(context, set, placement);
        return decider.refused(Math.floorDiv(chunk.x, placement.spacing()), Math.floorDiv(chunk.z, placement.spacing()));
    }

    /** A cell's place in the stand-in populate order: the lower, the earlier (ties by z, then x). */
    public static long priority(long seed, int cellX, int cellZ) {
        long h = seed ^ (cellX * 0x9E3779B97F4A7C15L) ^ (cellZ * 0xC2B2AE3D27D4EB4FL);
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    /** Whether cell a comes before cell b in the stand-in populate order. */
    public static boolean before(long seed, int ax, int az, int bx, int bz) {
        long pa = priority(seed, ax, az), pb = priority(seed, bx, bz);
        if (pa != pb) return pa < pb;
        return az != bz ? az < bz : ax < bx;
    }

    /**
     * Whether a structure of {@code type} at {@code site} stands in the checks of a spot at {@code chunk} whose own
     * structure, of {@code type2}, has its site at {@code site2}: its envelope meets the D4 box at Y11, or, when the
     * spot's structure is a tower, the tower's box 18 over its grass.
     */
    public static boolean blocks(LegacyDungeonPiece.DungeonType type, BlockPos site, ChunkPos chunk,
                                 LegacyDungeonPiece.DungeonType type2, BlockPos site2) {
        int x0 = site.getX() + type.minXOff, x1 = site.getX() + type.maxXOff;
        int z0 = site.getZ() + type.minZOff, z1 = site.getZ() + type.maxZOff;
        int y0 = site.getY() - type.downExtent, y1 = site.getY() + type.upExtent;
        int cx = chunk.getMinBlockX(), cz = chunk.getMinBlockZ();
        if (y0 <= D4_Y && D4_Y <= y1 && x0 <= cx + 39 && cx - 25 <= x1 && z0 <= cz + 29 && cz - 25 <= z1) return true;
        if (type2 == LegacyDungeonPiece.DungeonType.KING_TOWER || type2 == LegacyDungeonPiece.DungeonType.QUEEN_TOWER) {
            int y = site2.getY() + TOWER_RISE;
            return y0 <= y && y <= y1 && x0 <= site2.getX() + 32 && site2.getX() - 20 <= x1
                    && z0 <= site2.getZ() + 32 && site2.getZ() - 4 <= z1;
        }
        return false;
    }

    /** One spot: its chunk, its structure's type and its site (no site: null type). */
    private record Spot(ChunkPos chunk, LegacyDungeonPiece.DungeonType type, BlockPos site) {
    }

    /** The decisions of one question, each spot and each refusal worked out once. */
    private static final class Decider {
        private final Structure.GenerationContext context;
        private final StructureSet set;
        private final RandomSpreadStructurePlacement placement;
        private final Map<Long, Spot> spots = new HashMap<>();
        private final Map<Long, Boolean> refusals = new HashMap<>();

        Decider(Structure.GenerationContext context, StructureSet set, RandomSpreadStructurePlacement placement) {
            this.context = context;
            this.set = set;
            this.placement = placement;
        }

        private static long key(int gx, int gz) {
            return ((long) gx << 32) ^ (gz & 0xFFFFFFFFL);
        }

        Spot spot(int gx, int gz) {
            return spots.computeIfAbsent(key(gx, gz), k -> {
                long seed = context.seed();
                ChunkPos chunk = placement.getPotentialStructureChunk(seed, gx * placement.spacing(), gz * placement.spacing());
                Structure structure = StructurePicks.firstPick(set, seed, chunk).structure().value();
                if (!(structure instanceof LegacyDungeonStructure legacy)) return new Spot(chunk, null, null);
                BlockPos site = legacy.siteOf(new Structure.GenerationContext(context.registryAccess(),
                        context.chunkGenerator(), context.biomeSource(), context.randomState(),
                        context.structureTemplateManager(), seed, chunk, context.heightAccessor(), structure.biomes()::contains));
                return site == null ? new Spot(chunk, null, null) : new Spot(chunk, legacy.dungeonType(), site);
            });
        }

        boolean refused(int gx, int gz) {
            Boolean known = refusals.get(key(gx, gz));
            if (known != null) return known;
            Spot me = spot(gx, gz);
            boolean refused = false;
            if (me.type() != null) {
                for (int dz = -1; dz <= 1 && !refused; dz++) {
                    for (int dx = -1; dx <= 1 && !refused; dx++) {
                        int nx = gx + dx, nz = gz + dz;
                        if ((dx == 0 && dz == 0) || !before(context.seed(), nx, nz, gx, gz)) continue;
                        Spot other = spot(nx, nz);
                        refused = other.type() != null && blocks(other.type(), other.site(), me.chunk(), me.type(), me.site())
                                && !refused(nx, nz);
                    }
                }
            }
            refusals.put(key(gx, gz), refused);
            return refused;
        }
    }
}
