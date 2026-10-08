package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;

/**
 * MOD-044: the Royal Altars fitted to their ground (modern.altarTerrain). The layout: an altar laid out fitted keeps
 * the flag in its save data and reaches over the ground round it; laid out classic, or any other structure, keeps the
 * box its type gives. The ground: the strip inside the cleared envelope level with the pad, and beyond it each column
 * brought within one block a block of the pad's level, a hillside cut back with grass on the cut, a drop filled with
 * dirt under a grass top, and a column holding anything but ground, plants, leaves and water left as it stands.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AltarGroundTests {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();

    /** A column from Y0 to Y255 of blocks in an array. */
    private static final class ArrayColumn implements LegacyDungeonPiece.Column {
        final BlockState[] blocks = new BlockState[256];

        ArrayColumn() {
            Arrays.fill(this.blocks, AIR);
        }

        @Override
        public BlockState get(int y) {
            return y < 0 || y > 255 ? AIR : this.blocks[y];
        }

        @Override
        public void set(int y, BlockState state) {
            if (y >= 0 && y <= 255) this.blocks[y] = state;
        }

        /** Stone, two dirt and a grass top at {@code top}. */
        ArrayColumn ground(int top) {
            for (int y = 0; y <= top; y++) this.blocks[y] = y == top ? GRASS : y >= top - 2 ? DIRT : STONE;
            return this;
        }

        int highestNonAir() {
            for (int y = 255; y >= 0; y--) if (!this.blocks[y].isAir()) return y;
            return -1;
        }
    }

    @GameTest(template = "empty")
    public static void mod044a_altars_are_laid_out_fitted_or_classic(GameTestHelper helper) {
        BlockPos origin = new BlockPos(1000, 70, -2000);
        LegacyDungeonPiece fitted = new LegacyDungeonPiece(origin, DungeonType.KING_ALTAR, true);
        LegacyDungeonPiece classic = new LegacyDungeonPiece(origin, DungeonType.QUEEN_ALTAR, false);
        LegacyDungeonPiece other = new LegacyDungeonPiece(origin, DungeonType.SHADOW, true);
        int reach = 30 + LegacyDungeonPiece.ALTAR_FIT_RING;
        helper.assertTrue(fitted.fitted(), "an altar laid out fitted is not fitted");
        helper.assertTrue(fitted.getBoundingBox().equals(new BoundingBox(1000 - reach, 60 - LegacyDungeonPiece.ALTAR_FIT_RING,
                -2000 - reach, 1000 + reach, 129, -2000 + reach)), "the fitted altar's box " + fitted.getBoundingBox()
                + " does not reach over the ring and down to its deepest fill");
        helper.assertTrue(!classic.fitted() && classic.getBoundingBox().equals(new BoundingBox(968, 60, -2032, 1032, 129, -1968)),
                "the classic altar's box " + classic.getBoundingBox() + " is not its type's");
        helper.assertTrue(!other.fitted(), "a structure other than an altar was fitted");
        StructurePieceSerializationContext context = StructurePieceSerializationContext.fromLevel(helper.getLevel());
        CompoundTag saved = fitted.createTag(context);
        LegacyDungeonPiece loaded = new LegacyDungeonPiece(context, saved);
        helper.assertTrue(loaded.fitted() && loaded.getBoundingBox().equals(fitted.getBoundingBox()),
                "a fitted altar loaded from its save data is " + (loaded.fitted() ? "" : "not ") + "fitted, box "
                        + loaded.getBoundingBox());
        LegacyDungeonPiece loadedClassic = new LegacyDungeonPiece(context, classic.createTag(context));
        helper.assertTrue(!loadedClassic.fitted(), "a classic altar loaded from its save data came back fitted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mod044b_the_ground_round_a_fitted_altar_slopes_one_block_a_block(GameTestHelper helper) {
        int pad = 70;
        for (int out = -4; out <= 20; out++) {
            int hill = LegacyDungeonPiece.fittedGround(78, pad, out);
            int drop = LegacyDungeonPiece.fittedGround(56, pad, out);
            int reach = Math.max(0, out);
            helper.assertTrue(hill == Math.min(78, pad + reach) && drop == Math.max(56, pad - reach),
                    out + " blocks out: a hillside at 78 goes to " + hill + ", a drop at 56 to " + drop);
        }

        // a hillside eight over the pad, three blocks out: cut to 73, grass on the cut, dirt under it
        ArrayColumn hill = new ArrayColumn().ground(78);
        hill.set(79, Blocks.SHORT_GRASS.defaultBlockState());
        LegacyDungeonPiece.fitColumn(hill, pad, 3, 120, 40);
        helper.assertTrue(hill.highestNonAir() == 73 && hill.get(73) == GRASS && hill.get(72) == DIRT && hill.get(71) == DIRT,
                "the hillside column's top is " + hill.highestNonAir() + " (" + hill.get(hill.highestNonAir()) + ")");

        // a drop fourteen under the pad, four blocks out: filled to 66 with dirt, the grass on top
        ArrayColumn drop = new ArrayColumn().ground(56);
        LegacyDungeonPiece.fitColumn(drop, pad, 4, 120, 40);
        boolean filled = drop.get(66) == GRASS && drop.get(56) == DIRT;
        for (int y = 57; y < 66; y++) filled &= drop.get(y) == DIRT;
        helper.assertTrue(filled && drop.highestNonAir() == 66, "the drop's column is not filled to 66 under grass: top "
                + drop.highestNonAir());

        // inside the envelope (two blocks off the pad): level with the pad
        ArrayColumn strip = new ArrayColumn().ground(64);
        LegacyDungeonPiece.fitColumn(strip, pad, -2, 120, 40);
        helper.assertTrue(strip.highestNonAir() == 70 && strip.get(70) == GRASS, "the strip round the pad is not level with "
                + "it: top " + strip.highestNonAir());

        // water over a drop, two blocks out: the water filled with the ground
        ArrayColumn pond = new ArrayColumn().ground(56);
        for (int y = 57; y <= 62; y++) pond.set(y, Blocks.WATER.defaultBlockState());
        LegacyDungeonPiece.fitColumn(pond, pad, 2, 120, 40);
        helper.assertTrue(pond.get(68) == GRASS && pond.get(62) == DIRT && pond.highestNonAir() == 68,
                "the pond's column is not filled to 68: top " + pond.highestNonAir());

        // a trunk on the hillside: the column stands as it was
        ArrayColumn trunk = new ArrayColumn().ground(78);
        for (int y = 79; y <= 84; y++) trunk.set(y, Blocks.OAK_LOG.defaultBlockState());
        LegacyDungeonPiece.fitColumn(trunk, pad, 3, 120, 40);
        helper.assertTrue(trunk.get(78) == GRASS && trunk.get(79).is(Blocks.OAK_LOG), "a column with a trunk was cut");

        // a canopy over the hillside: the ground cut back under it, the leaves kept
        ArrayColumn canopy = new ArrayColumn().ground(78);
        canopy.set(86, Blocks.OAK_LEAVES.defaultBlockState());
        LegacyDungeonPiece.fitColumn(canopy, pad, 3, 120, 40);
        helper.assertTrue(canopy.get(86).is(Blocks.OAK_LEAVES) && canopy.get(73) == GRASS && canopy.get(74).isAir(),
                "under a canopy the hillside was not cut back to 73, or the leaves went");

        // the column read from its surface, as a world being generated reads it: a flower on the hillside and a salt
        // vein in it go with the cut, a sunflower too
        ArrayColumn flowered = new ArrayColumn().ground(78);
        flowered.set(79, Blocks.POPPY.defaultBlockState());
        flowered.set(76, ModBlocks.ORE_SALT.get().defaultBlockState());
        LegacyDungeonPiece.fitColumn(flowered, pad, 3, 79, 40);
        helper.assertTrue(flowered.highestNonAir() == 73 && flowered.get(73) == GRASS,
                "a hillside under a flower with salt in it was not cut to 73: top " + flowered.highestNonAir() + " ("
                        + flowered.get(flowered.highestNonAir()) + ")");
        ArrayColumn sunflower = new ArrayColumn().ground(78);
        sunflower.set(79, Blocks.SUNFLOWER.defaultBlockState());
        sunflower.set(80, Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        LegacyDungeonPiece.fitColumn(sunflower, pad, 3, 80, 40);
        helper.assertTrue(sunflower.highestNonAir() == 73, "a sunflower on the hillside stood over the cut: top "
                + sunflower.highestNonAir());

        // a branch over the hillside (its trunk elsewhere): the ground cut under it, the branch and its leaves kept; a
        // bush resting on the hillside goes with the cut
        ArrayColumn branch = new ArrayColumn().ground(78);
        branch.set(88, Blocks.SPRUCE_LOG.defaultBlockState());
        branch.set(89, Blocks.SPRUCE_LEAVES.defaultBlockState());
        LegacyDungeonPiece.fitColumn(branch, pad, 3, 89, 40);
        helper.assertTrue(branch.get(73) == GRASS && branch.get(74).isAir() && branch.get(78).isAir()
                && branch.get(88).is(Blocks.SPRUCE_LOG) && branch.get(89).is(Blocks.SPRUCE_LEAVES),
                "under a branch the hillside was not cut to 73, or the branch went");
        ArrayColumn bush = new ArrayColumn().ground(78);
        bush.set(79, Blocks.OAK_LEAVES.defaultBlockState());
        bush.set(80, Blocks.OAK_LEAVES.defaultBlockState());
        LegacyDungeonPiece.fitColumn(bush, pad, 3, 80, 40);
        helper.assertTrue(bush.highestNonAir() == 73, "a bush on the hillside stood over the cut: top "
                + bush.highestNonAir());
        // a huge tree's crown resting on the hillside (five leaves high) stays over the cut, whole
        ArrayColumn crown = new ArrayColumn().ground(78);
        for (int y = 79; y <= 83; y++) crown.set(y, ModBlocks.APPLE_LEAVES.get().defaultBlockState());
        LegacyDungeonPiece.fitColumn(crown, pad, 3, 83, 40);
        boolean whole = crown.get(73) == GRASS && crown.get(78).isAir();
        for (int y = 79; y <= 83; y++) whole &= crown.get(y).is(ModBlocks.APPLE_LEAVES.get());
        helper.assertTrue(whole, "a crown resting on the hillside was not left whole over the cut to 73");
        // a spawn ore in the hillside goes with the cut
        ArrayColumn egg = new ArrayColumn().ground(78);
        egg.set(75, ModBlocks.SPIDER_SPAWN_BLOCK.get().defaultBlockState());
        LegacyDungeonPiece.fitColumn(egg, pad, 3, 78, 40);
        helper.assertTrue(egg.highestNonAir() == 73, "a spawn ore in the hillside stood over the cut: top "
                + egg.highestNonAir());

        // an ant hill set into the hillside's grass is ground, cut with it
        ArrayColumn anthill = new ArrayColumn().ground(78);
        anthill.set(78, ModBlocks.RED_ANT_BLOCK.get().defaultBlockState());
        LegacyDungeonPiece.fitColumn(anthill, pad, 3, 78, 40);
        helper.assertTrue(anthill.highestNonAir() == 73 && anthill.get(73) == GRASS, "an ant hill on the hillside was "
                + "not cut to 73: top " + anthill.highestNonAir());

        // a drop with a flower, leaves and tall grass on it, four blocks out: filled to 66 over all of them, the tall
        // grass's top half gone with its root
        ArrayColumn meadow = new ArrayColumn().ground(56);
        meadow.set(57, Blocks.DANDELION.defaultBlockState());
        meadow.set(60, Blocks.OAK_LEAVES.defaultBlockState());
        meadow.set(66, Blocks.TALL_GRASS.defaultBlockState());
        meadow.set(67, Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        LegacyDungeonPiece.fitColumn(meadow, pad, 4, 67, 40);
        boolean buried = meadow.get(66) == GRASS && meadow.highestNonAir() == 66;
        for (int y = 56; y < 66; y++) buried &= meadow.get(y) == DIRT;
        helper.assertTrue(buried, "a drop with a flower, leaves and tall grass was not filled to 66 under grass: top "
                + meadow.highestNonAir() + " (" + meadow.get(meadow.highestNonAir()) + ")");

        // a branch low over a drop: the fill goes round it
        ArrayColumn under = new ArrayColumn().ground(56);
        under.set(62, Blocks.OAK_LOG.defaultBlockState());
        LegacyDungeonPiece.fitColumn(under, pad, 4, 62, 40);
        helper.assertTrue(under.get(62).is(Blocks.OAK_LOG) && under.get(61) == DIRT && under.get(63) == DIRT
                && under.get(66) == GRASS, "the fill did not go round a branch low over the drop");
        // a trunk standing at the foot of the drop: the fill goes round it too
        ArrayColumn foot = new ArrayColumn().ground(56);
        for (int y = 57; y <= 60; y++) foot.set(y, Blocks.OAK_LOG.defaultBlockState());
        LegacyDungeonPiece.fitColumn(foot, pad, 4, 60, 40);
        helper.assertTrue(foot.get(60).is(Blocks.OAK_LOG) && foot.get(61) == DIRT && foot.get(66) == GRASS,
                "the fill did not go round a trunk at the foot of the drop");
        // a stump in the strip round the pad: the strip filled level with the pad round it
        ArrayColumn stump = new ArrayColumn().ground(64);
        stump.set(65, Blocks.OAK_LOG.defaultBlockState());
        stump.set(66, Blocks.OAK_LOG.defaultBlockState());
        LegacyDungeonPiece.fitColumn(stump, pad, -2, 69, 40);
        helper.assertTrue(stump.get(66).is(Blocks.OAK_LOG) && stump.get(67) == DIRT && stump.get(70) == GRASS
                && stump.highestNonAir() == 70, "the strip was not filled level with the pad round a stump");
        helper.succeed();
    }

    /** Stone up to the top {@code top} gives each column of a square, two dirt and a grass top under it; air above. */
    private static void ground(ServerLevel level, BlockPos centre, int half, int bottom, int up, IntBinaryOperator top) {
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                int t = top.applyAsInt(dx, dz);
                for (int y = bottom; y <= up; y++) {
                    BlockState state = y > t ? AIR : y == t ? GRASS : y >= t - 2 ? DIRT : STONE;
                    at.set(centre.getX() + dx, y, centre.getZ() + dz);
                    if (level.getBlockState(at) != state) level.setBlock(at, state, 2 | 16);
                }
            }
        }
    }

    /** The highest block that is not air in a column, from {@code from} down to {@code to}. */
    private static int top(ServerLevel level, int x, int z, int from, int to) {
        for (int y = from; y >= to; y--) {
            if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) return y;
        }
        return to - 1;
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void mod044c_an_altar_built_fitted_meets_its_ground(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corner = helper.absolutePos(BlockPos.ZERO);
        int pad = 100;
        // a hillside eight over the pad to the west, a drop fourteen under it to the east; the same ground twice, an
        // altar built fitted on one and classic on the other, 400 blocks apart
        BlockPos fittedAt = new BlockPos(corner.getX(), pad, corner.getZ() + 70_000);
        BlockPos classicAt = fittedAt.offset(400, 0, 0);
        IntBinaryOperator hillAndDrop = (dx, dz) -> dx < 0 ? pad + 8 : pad - 14;
        for (BlockPos at : new BlockPos[] {fittedAt, classicAt}) {
            ground(level, at, 50, pad - 30, pad + 62, hillAndDrop);
            // a poppy and a salt vein on the hillside, three blocks beyond the envelope's west edge
            level.setBlock(at.offset(-33, 9, 4), Blocks.POPPY.defaultBlockState(), 2);
            level.setBlock(at.offset(-33, 5, 4), ModBlocks.ORE_SALT.get().defaultBlockState(), 2);
        }
        LegacyDungeonPiece.buildNow(level, fittedAt, DungeonType.QUEEN_ALTAR, RandomSource.create(44L), true);
        LegacyDungeonPiece.buildNow(level, classicAt, DungeonType.QUEEN_ALTAR, RandomSource.create(44L), false);

        int x0 = fittedAt.getX(), z0 = fittedAt.getZ();
        // the strip round the pad level with it on both sides, under the ceiling's rim too (the pad spans -25..25)
        for (int dx : new int[] {-30, -27, -26, 26, 27, 30}) {
            int t = top(level, x0 + dx, z0, pad + 40, pad - 30);
            helper.assertTrue(t == pad && level.getBlockState(new BlockPos(x0 + dx, pad, z0)).is(Blocks.GRASS_BLOCK),
                    "the strip " + dx + " from the centre stands at " + t + ", not grass level with the pad at " + pad);
        }
        // the hillside cut back one block a block: three blocks beyond the envelope at pad + 3, eight and more out as
        // it was; the poppy and the salt gone with the cut
        int hill3 = top(level, x0 - 33, z0 + 4, pad + 40, pad - 30);
        int hill8 = top(level, x0 - 38, z0, pad + 40, pad - 30);
        helper.assertTrue(hill3 == pad + 3 && hill8 == pad + 8, "the hillside 3 and 8 beyond the envelope stands at "
                + hill3 + " and " + hill8 + ", not " + (pad + 3) + " and " + (pad + 8));
        helper.assertTrue(level.getBlockState(new BlockPos(x0 - 33, pad + 5, z0 + 4)).isAir()
                && level.getBlockState(new BlockPos(x0 - 33, pad + 9, z0 + 4)).isAir(), "the salt or the poppy on the "
                + "cut hillside stayed");
        // the drop filled: four beyond the envelope at pad - 4, sixteen out at pad - 14 as it was
        int drop4 = top(level, x0 + 34, z0, pad + 40, pad - 30);
        int drop16 = top(level, x0 + 46, z0, pad + 40, pad - 30);
        helper.assertTrue(drop4 == pad - 4 && drop16 == pad - 14, "the drop 4 and 16 beyond the envelope stands at "
                + drop4 + " and " + drop16 + ", not " + (pad - 4) + " and " + (pad - 14));
        // the pad's east edge on dirt down to the ground fourteen under it
        boolean skirt = true;
        for (int y = pad - 13; y < pad; y++) skirt &= level.getBlockState(new BlockPos(x0 + 25, y, z0)).is(Blocks.DIRT);
        helper.assertTrue(skirt, "the pad's east edge does not stand on dirt down to the ground");

        // the classic altar on the same ground keeps its cut wall, its bare strip and its nine-block skirt
        int cx = classicAt.getX(), cz = classicAt.getZ();
        int wall = top(level, cx - 31, cz, pad + 40, pad - 30);
        int strip = top(level, cx - 27, cz, pad + 40, pad - 30);
        int dropOut = top(level, cx + 34, cz, pad + 40, pad - 30);
        helper.assertTrue(wall == pad + 8 && strip == pad - 1 && dropOut == pad - 14, "the classic altar's wall, strip "
                + "and drop stand at " + wall + ", " + strip + " and " + dropOut);
        helper.assertTrue(level.getBlockState(new BlockPos(cx + 25, pad - 9, cz)).is(Blocks.DIRT)
                && level.getBlockState(new BlockPos(cx + 25, pad - 10, cz)).isAir(), "the classic skirt is not nine deep");
        helper.succeed();
    }
}
