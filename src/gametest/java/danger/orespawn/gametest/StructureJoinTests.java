package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.structure.LegacyDungeonPiece;
import danger.orespawn.world.structure.LegacyDungeonPiece.DungeonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * WGEN-076 (GitHub issue #5): the fences, glass panes, iron bars and walls the legacy structures place join their
 * neighbours, as 1.7.10 drew them. The structures are built with {@link LegacyDungeonPiece#buildNow}, the live path;
 * worldgen hands the same blocks to the chunk's post-processing, which settles them the same way when the chunk goes
 * live (vanilla's structure pieces rely on it for their fences and bars).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StructureJoinTests {

    /** Every joiner in the box is settled: its state is what its finished neighbours make it. Returns how many were checked. */
    private static int assertJoinersSettled(GameTestHelper helper, BlockPos from, BlockPos to, String what) {
        ServerLevel level = helper.getLevel();
        int checked = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            BlockState state = level.getBlockState(pos);
            if (!LegacyDungeonPiece.joinsNeighbours(state)) continue;
            checked++;
            BlockState joined = Block.updateFromNeighbourShapes(state, level, pos);
            helper.assertTrue(joined.equals(state), what + ": the " + BuiltInRegistries.BLOCK.getKey(state.getBlock())
                    + " at " + pos + " is " + state + ", its neighbours make it " + joined);
        }
        return checked;
    }

    /**
     * The Damsel in Distress jail (orig GenericDungeon.java:3706-3711): a 7-wide, 4-high wall of iron bars at z=+1
     * between the cobblestone shell's walls, the open cells either side. Each bar joins east and west (its neighbours,
     * the shell at the ends) and neither side; before the fix every one stood as a lone post.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void w076a_the_jail_bars_join_into_a_wall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = helper.absolutePos(new BlockPos(24, 1, 24));
        LegacyDungeonPiece.buildNow(level, o, DungeonType.DAMSEL_IN_DISTRESS);
        for (int i = -3; i <= 3; i++) {
            for (int k = 1; k <= 4; k++) {
                BlockPos pos = o.offset(i, k, 1);
                BlockState bar = level.getBlockState(pos);
                helper.assertTrue(bar.is(Blocks.IRON_BARS), "no jail bar at " + pos);
                helper.assertTrue(bar.getValue(CrossCollisionBlock.EAST) && bar.getValue(CrossCollisionBlock.WEST),
                        "the jail bar at " + pos + " must join east and west: " + bar);
                helper.assertTrue(!bar.getValue(CrossCollisionBlock.NORTH) && !bar.getValue(CrossCollisionBlock.SOUTH),
                        "the jail bar at " + pos + " must not join the open cells north and south: " + bar);
            }
        }
        int checked = assertJoinersSettled(helper, o.offset(-5, 0, -5), o.offset(5, 6, 5), "Damsel in Distress");
        helper.assertTrue(checked >= 28, "only " + checked + " joiners in the Damsel in Distress (28 jail bars expected)");
        helper.succeed();
    }

    /**
     * The Mini Dungeon (orig GenericDungeon.java:2243-2301): its iron-bar cage and its staircase railing fences, every
     * one settled against the finished structure (a railing fence joins the next step's planks).
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void w076b_the_mini_dungeon_cage_and_railings_join(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos o = helper.absolutePos(new BlockPos(24, 1, 24));
        LegacyDungeonPiece.buildNow(level, o, DungeonType.MINI_DUNGEON);
        int checked = assertJoinersSettled(helper, o.offset(-12, -1, -6), o.offset(14, 14, 14), "Mini Dungeon");
        helper.assertTrue(checked >= 12, "only " + checked + " joiners in the Mini Dungeon (12 railing fences at least)");
        BlockState railing = level.getBlockState(o.offset(-6, 2, 3));
        helper.assertTrue(railing.is(Blocks.OAK_FENCE) && railing.getValue(CrossCollisionBlock.EAST),
                "the first railing fence must join the next step's planks to the east: " + railing);
        helper.succeed();
    }

    /**
     * Only fences, panes, bars and walls are settled: the plants and every other block keep the suppressed shape
     * updates TF-021 relies on (a plant settled against its neighbours can erase itself).
     */
    @GameTest(template = "empty")
    public static void w076c_only_the_joining_blocks_are_settled(GameTestHelper helper) {
        for (Block joiner : new Block[] {Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.GLASS_PANE,
                Blocks.IRON_BARS, Blocks.COBBLESTONE_WALL}) {
            helper.assertTrue(LegacyDungeonPiece.joinsNeighbours(joiner.defaultBlockState()),
                    BuiltInRegistries.BLOCK.getKey(joiner) + " must be settled");
        }
        for (Block other : new Block[] {Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.LILY_PAD, Blocks.NETHER_WART,
                Blocks.WHEAT, Blocks.TORCH, Blocks.QUARTZ_STAIRS, Blocks.CHEST, Blocks.COBBLESTONE}) {
            helper.assertTrue(!LegacyDungeonPiece.joinsNeighbours(other.defaultBlockState()),
                    BuiltInRegistries.BLOCK.getKey(other) + " must keep the suppressed shape updates");
        }
        helper.succeed();
    }
}
