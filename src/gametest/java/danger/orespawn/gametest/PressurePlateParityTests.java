package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-090: the fifteen 1.7.10 entities whose {@code doesEntityNotTriggerPressurePlate}
 * returns true ({@code func_145773_az}) must ignore block triggers in the port. The
 * plate's own {@code entityInside} is invoked directly with the entity standing in the
 * plate's box, so hovering fliers cannot pass by never touching it; a zombie on an
 * identical plate is the control.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PressurePlateParityTests {
    /** Registry ids of the ports; the reference lines are cited on each entity's override. */
    static final List<String> IGNORING_PLATES = List.of(
            "brutalfly", "butterfly", "luna_moth", "mosquito", "fairy", "firefly", "ghost", "ghost_skelly",
            "mothra", "purple_power", "rotator", "vortex", "worm_large", "worm_medium", "worm_small");

    @GameTest(template = "empty_large")
    public static void ents090_pressure_plates_ignored(GameTestHelper helper) {
        int index = 0;
        for (String id : IGNORING_PLATES) {
            BlockPos plate = slot(index++);
            helper.setBlock(plate.below(), Blocks.STONE);
            helper.setBlock(plate, Blocks.STONE_PRESSURE_PLATE);
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, id)).orElse(null);
            helper.assertTrue(type != null, "unknown registry id orespawn:" + id);
            Entity entity = helper.spawn(type, plate);
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setPersistenceRequired();
            }
            helper.assertTrue(entity.isIgnoringBlockTriggers(),
                    "orespawn:" + id + " must ignore block triggers (orig func_145773_az -> true, ENT-S-090)");
            press(helper, plate, entity);
            helper.assertTrue(!powered(helper, plate),
                    "orespawn:" + id + " standing on a stone pressure plate must not power it (ENT-S-090)");
        }
        BlockPos controlPlate = slot(index);
        helper.setBlock(controlPlate.below(), Blocks.STONE);
        helper.setBlock(controlPlate, Blocks.STONE_PRESSURE_PLATE);
        Entity zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, controlPlate);
        press(helper, controlPlate, zombie);
        helper.assertTrue(powered(helper, controlPlate), "control: a zombie on the same plate powers it");
        helper.succeed();
    }

    /** A 6-block grid inside the 48x16x48 template. */
    static BlockPos slot(int index) {
        return new BlockPos(4 + (index % 7) * 6, 2, 4 + (index / 7) * 6);
    }

    /** What the plate does when an entity is inside its box: recompute its signal from the entities present. */
    static void press(GameTestHelper helper, BlockPos plate, Entity entity) {
        BlockPos absolute = helper.absolutePos(plate);
        BlockState state = helper.getLevel().getBlockState(absolute);
        state.entityInside(helper.getLevel(), absolute, entity);
    }

    static boolean powered(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        return state.hasProperty(PressurePlateBlock.POWERED) && state.getValue(PressurePlateBlock.POWERED);
    }
}
