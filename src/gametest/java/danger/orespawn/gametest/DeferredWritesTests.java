package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.DeferredWrites;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * BUG-021 (GitHub #6): the Crystal builders' writes beyond a decoration pass's write radius are kept for their chunk
 * ({@link DeferredWrites}) instead of dropped. These pin the keeping itself: a kept write lands on a loaded chunk on the
 * server tick, as a block, a chest with its loot table or a spawner with its mob; an air-only write leaves a solid
 * block alone; a write the level may take now is laid at once; and the kept writes survive the save.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DeferredWritesTests {

    @GameTest(template = "empty")
    public static void d021a_kept_writes_land_on_a_loaded_chunk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos block = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos onStone = helper.absolutePos(new BlockPos(2, 2, 1));
        BlockPos chest = helper.absolutePos(new BlockPos(1, 2, 2));
        BlockPos spawner = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(onStone, Blocks.STONE.defaultBlockState(), 3);
        int before = DeferredWrites.pending(level);
        DeferredWrites.keep(level, new DeferredWrites.Write(block, Blocks.GLASS.defaultBlockState(), true, null, null));
        DeferredWrites.keep(level, new DeferredWrites.Write(onStone, Blocks.GLASS.defaultBlockState(), true, null, null));
        DeferredWrites.keep(level, new DeferredWrites.Write(chest, Blocks.CHEST.defaultBlockState(), false,
                BuiltInLootTables.SIMPLE_DUNGEON, null));
        DeferredWrites.keep(level, new DeferredWrites.Write(spawner, Blocks.SPAWNER.defaultBlockState(), false, null,
                EntityType.ZOMBIE));
        helper.assertTrue(DeferredWrites.pending(level) == before + 4, "the four writes were not kept");
        helper.assertTrue(level.getBlockState(block).isAir(), "a kept write landed before the tick");

        DeferredWrites.tick(level);

        helper.assertTrue(level.getBlockState(block).is(Blocks.GLASS), "the kept block did not land on the loaded chunk");
        helper.assertTrue(level.getBlockState(onStone).is(Blocks.STONE), "an air-only write replaced a solid block");
        helper.assertTrue(level.getBlockState(chest).is(Blocks.CHEST)
                        && level.getBlockEntity(chest) instanceof RandomizableContainerBlockEntity box
                        && BuiltInLootTables.SIMPLE_DUNGEON.equals(box.getLootTable()),
                "the kept chest did not land with its loot table");
        helper.assertTrue(level.getBlockState(spawner).is(Blocks.SPAWNER)
                        && level.getBlockEntity(spawner) instanceof SpawnerBlockEntity cage
                        && cage.getSpawner().getOrCreateDisplayEntity(level, spawner) != null
                        && cage.getSpawner().getOrCreateDisplayEntity(level, spawner).getType() == EntityType.ZOMBIE,
                "the kept spawner did not land with its mob");
        helper.assertTrue(DeferredWrites.pending(level) == before, "the applied writes are still kept");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void d021b_a_write_the_level_takes_now_is_laid_at_once(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        int before = DeferredWrites.pending(level);
        DeferredWrites.setBlock(level, pos, Blocks.GLASS.defaultBlockState(), false);
        helper.assertTrue(level.getBlockState(pos).is(Blocks.GLASS), "a write the level may take was not laid");
        helper.assertTrue(DeferredWrites.pending(level) == before, "a write the level may take was kept");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void d021c_kept_writes_survive_the_save(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos chunk = new ChunkPos(40, -12);
        BlockPos base = chunk.getBlockAt(3, 90, 5);
        List<DeferredWrites.Write> writes = List.of(
                new DeferredWrites.Write(base, Blocks.OAK_LEAVES.defaultBlockState(), true, null, null),
                new DeferredWrites.Write(base.above(), Blocks.GLASS.defaultBlockState(), false, null, null),
                new DeferredWrites.Write(base.east(), Blocks.CHEST.defaultBlockState(), false,
                        BuiltInLootTables.SIMPLE_DUNGEON, null),
                new DeferredWrites.Write(base.west(), Blocks.SPAWNER.defaultBlockState(), false, null, EntityType.ZOMBIE));
        DeferredWrites.Data data = new DeferredWrites.Data();
        for (DeferredWrites.Write w : writes) data.add(chunk, w);
        CompoundTag tag = data.save(new CompoundTag(), level.registryAccess());
        DeferredWrites.Data back = DeferredWrites.Data.load(tag, level.registryAccess());
        helper.assertTrue(back.size() == writes.size(), "the save lost writes: " + back.size());
        List<DeferredWrites.Write> got = back.take(chunk);
        helper.assertTrue(got.equals(writes), "the writes changed across the save: " + got);
        helper.succeed();
    }
}
