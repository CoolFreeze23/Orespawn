package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import danger.orespawn.world.GenericDungeon;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Islands' generic dungeon and ruby dungeon as structure pieces (WGEN-080). The original's D4 roll gave them five
 * of its nineteen slots (orig OreSpawnWorld.java:134-176: {@code addD4GenericDungeon} four, :2438-2452;
 * {@code addD4RubyDungeon} one, :2171-2185), so they now stand in the islands_structures set beside the D4 structures,
 * one pick per spot, instead of a chunk generator roll of their own. The boxes are {@link GenericDungeon}'s (orig
 * GenericDungeon.java:97-185, RubyBirdDungeon.java:30-82): carved to air, a mossy cobblestone floor, the ceiling and
 * then the four walls drawn block by block from the cobblestone mix (the ruby dungeon's with its ruby ore), the
 * spawner in the middle one above the floor (the generic dungeon's mob drawn after the walls, the Ruby Bird fixed), the
 * chest at the middle of the first wall. Every chunk pass draws the whole box and writes only its own slice, as the
 * piece's gated writers require.
 */
final class IslandsDungeonGenerator {

    private IslandsDungeonGenerator() {
    }

    static void generate(LegacyDungeonPiece piece, BlockPos corner, RandomSource random, boolean ruby) {
        int width = ruby ? 10 : 12;
        int height = ruby ? 5 : 6;
        int x0 = corner.getX();
        int y0 = corner.getY();
        int z0 = corner.getZ();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    piece.place(x0 + i, y0 + j, z0 + k, air);
                }
            }
        }
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                piece.place(x0 + i, y0, z0 + k, mossy);
            }
        }
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                piece.place(x0 + i, y0 + height - 1, z0 + k, GenericDungeon.wallBlock(random, ruby));
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                piece.place(x0 + i, y0 + j, z0, GenericDungeon.wallBlock(random, ruby));
                piece.place(x0 + i, y0 + j, z0 + width - 1, GenericDungeon.wallBlock(random, ruby));
                piece.place(x0, y0 + j, z0 + i, GenericDungeon.wallBlock(random, ruby));
                piece.place(x0 + width - 1, y0 + j, z0 + i, GenericDungeon.wallBlock(random, ruby));
            }
        }
        piece.placeSpawner(x0 + width / 2, y0 + 1, z0 + width / 2,
                ruby ? ModEntities.RUBY_BIRD.get() : GenericDungeon.spawnerMob(random));
        piece.placeLootChest(x0 + width / 2, y0 + 1, z0 + 1,
                ruby ? GenericDungeon.RUBY_DUNGEON_LOOT : GenericDungeon.GENERIC_DUNGEON_LOOT);
    }
}
