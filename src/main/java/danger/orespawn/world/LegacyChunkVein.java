package danger.orespawn.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

/**
 * The vein OreSpawn's own ore pass lays in Utopia, the Village and Mining, as the original's
 * {@code ChunkOreGenerator.generateBlockOre} lays it: 1.7.10's WorldGenMinable shape (size + 1 spheres along a line
 * size / 4 blocks long), centred 8 blocks in from its origin on x and z, kept to the origin's chunk and laid in the
 * target only. The original wrote it into the chunk being built and read every position outside that chunk as air, so
 * the part of a vein past the chunk's edge was never laid; with the origin 3 to 12 blocks into the chunk on x and z,
 * most veins lose part of themselves, a vein centred past the edge most of it.
 */
public final class LegacyChunkVein {
    private LegacyChunkVein() {
    }

    /** A cell of the vein. */
    public interface Cell {
        void accept(int x, int y, int z);
    }

    /** Every cell of the vein's spheres, in the original's order, before the chunk and the target are looked at. */
    public static void cells(RandomSource random, BlockPos origin, int size, Cell cell) {
        float f = random.nextFloat() * (float) Math.PI;
        double x0 = (float) (origin.getX() + 8) + Mth.sin(f) * (float) size / 8.0F;
        double x1 = (float) (origin.getX() + 8) - Mth.sin(f) * (float) size / 8.0F;
        double z0 = (float) (origin.getZ() + 8) + Mth.cos(f) * (float) size / 8.0F;
        double z1 = (float) (origin.getZ() + 8) - Mth.cos(f) * (float) size / 8.0F;
        double y0 = origin.getY() + random.nextInt(3) - 2;
        double y1 = origin.getY() + random.nextInt(3) - 2;
        for (int l = 0; l <= size; l++) {
            double cx = x0 + (x1 - x0) * l / size;
            double cy = y0 + (y1 - y0) * l / size;
            double cz = z0 + (z1 - z0) * l / size;
            double spread = random.nextDouble() * size / 16.0;
            double width = (Mth.sin((float) l * (float) Math.PI / (float) size) + 1.0F) * spread + 1.0;
            int minX = Mth.floor(cx - width / 2.0), minY = Mth.floor(cy - width / 2.0), minZ = Mth.floor(cz - width / 2.0);
            int maxX = Mth.floor(cx + width / 2.0), maxY = Mth.floor(cy + width / 2.0), maxZ = Mth.floor(cz + width / 2.0);
            for (int x = minX; x <= maxX; x++) {
                double dx = (x + 0.5 - cx) / (width / 2.0);
                if (!(dx * dx < 1.0)) continue;
                for (int y = minY; y <= maxY; y++) {
                    double dy = (y + 0.5 - cy) / (width / 2.0);
                    if (!(dx * dx + dy * dy < 1.0)) continue;
                    for (int z = minZ; z <= maxZ; z++) {
                        double dz = (z + 0.5 - cz) / (width / 2.0);
                        if (dx * dx + dy * dy + dz * dz < 1.0) cell.accept(x, y, z);
                    }
                }
            }
        }
    }

    /** Whether a cell is laid at all: inside the origin's chunk and between bottom and top (the original read the rest as air). */
    public static boolean keeps(int chunkX, int chunkZ, int x, int y, int z, int bottom, int top) {
        return x >> 4 == chunkX && z >> 4 == chunkZ && y >= bottom && y < top;
    }

    /** Lays the vein in the origin's chunk between {@code bottom} and {@code top}, over the target only; the blocks laid. */
    public static int place(WorldGenLevel level, RandomSource random, BlockPos origin, int size, BlockState ore,
                            RuleTest target, int bottom, int top) {
        int chunkX = origin.getX() >> 4, chunkZ = origin.getZ() >> 4;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        int[] laid = {0};
        cells(random, origin, size, (x, y, z) -> {
            if (!keeps(chunkX, chunkZ, x, y, z, bottom, top)) return;
            at.set(x, y, z);
            if (!target.test(level.getBlockState(at), random)) return;
            level.setBlock(at, ore, 2);
            laid[0]++;
        });
        return laid[0];
    }
}
