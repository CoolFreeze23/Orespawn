package danger.orespawn.gametest;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.world.OldLandRepair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WGEN-096: land saved when Utopia, the Village, Crystal and Mining reached down to Y-64, loaded now that they start at
 * Y0. Such a chunk is known by its save holding blocks below the dimension's bottom (or over its top); a save made since
 * holds at most light there. The repair gives it the original's bedrock (a floor at Y0 under every column, a block with
 * a block entity or a point of interest left; the ragged layers up to Y4 in its stone), measures its heights again and
 * has its light worked out again.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class OldLandRepairTests {

    private static CompoundTag section(int y, String... blocks) {
        CompoundTag section = new CompoundTag();
        section.putByte("Y", (byte) y);
        ListTag palette = new ListTag();
        for (String block : blocks) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", block);
            palette.add(entry);
        }
        CompoundTag states = new CompoundTag();
        states.put("palette", palette);
        section.put("block_states", states);
        return section;
    }

    private static CompoundTag lightOnly(int y) {
        CompoundTag section = new CompoundTag();
        section.putByte("Y", (byte) y);
        section.putByteArray("SkyLight", new byte[2048]);
        return section;
    }

    private static CompoundTag saved(CompoundTag... sections) {
        ListTag list = new ListTag();
        for (CompoundTag section : sections) list.add(section);
        CompoundTag tag = new CompoundTag();
        tag.put("sections", list);
        return tag;
    }

    @GameTest(template = "empty")
    public static void wgen096a_old_land_is_known_by_its_blocks_below_the_bottom(GameTestHelper helper) {
        LevelHeightAccessor now = LevelHeightAccessor.create(0, 256);
        LevelHeightAccessor before = LevelHeightAccessor.create(-64, 384);
        CompoundTag old = saved(section(-4, "minecraft:deepslate"), section(-1, "minecraft:deepslate", "minecraft:air"),
                section(0, "minecraft:stone"));
        helper.assertTrue(OldLandRepair.holdsBlocksOutside(old, now), "a save with deepslate below Y0 was not known as old");
        helper.assertTrue(!OldLandRepair.holdsBlocksOutside(old, before), "a save inside its dimension's own height was "
                + "taken for old");
        helper.assertTrue(!OldLandRepair.holdsBlocksOutside(saved(lightOnly(-1), section(0, "minecraft:stone"),
                lightOnly(16)), now), "a save with only light round its height (as every save has) was taken for old");
        helper.assertTrue(!OldLandRepair.holdsBlocksOutside(saved(section(-2, "minecraft:air", "minecraft:cave_air")), now),
                "a save with only air below Y0 was taken for old");
        helper.assertTrue(OldLandRepair.holdsBlocksOutside(saved(section(17, "minecraft:stone")), now),
                "a save with stone over the top was not known as old");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wgen096b_old_land_gets_a_floor_and_its_true_heights(GameTestHelper helper) {
        ProtoChunk chunk = new ProtoChunk(new ChunkPos(3, -7), UpgradeData.EMPTY, LevelHeightAccessor.create(0, 256),
                helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME), null);
        chunk.setPersistedStatus(ChunkStatus.CARVERS);
        BlockState deepslate = Blocks.DEEPSLATE.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= 64; y++) {
                    BlockState state = y == 64 ? Blocks.GRASS_BLOCK.defaultBlockState() : y < 8 ? deepslate : stone;
                    chunk.setBlockState(at.set(minX + x, y, minZ + z), state, false);
                }
            }
        }
        // a cave through the old floor, water standing on it, a chest, an ore, a composter (a job site), a cave just
        // above the floor
        for (int y = 0; y <= 2; y++) chunk.setBlockState(at.set(minX, y, minZ), Blocks.AIR.defaultBlockState(), false);
        chunk.setBlockState(at.set(minX + 1, 0, minZ), Blocks.WATER.defaultBlockState(), false);
        chunk.setBlockState(at.set(minX + 2, 0, minZ), Blocks.CHEST.defaultBlockState(), false);
        chunk.setBlockState(at.set(minX + 4, 0, minZ), ModBlocks.ORE_TITANIUM.get().defaultBlockState(), false);
        chunk.setBlockState(at.set(minX + 5, 0, minZ), Blocks.COMPOSTER.defaultBlockState(), false);
        chunk.setBlockState(at.set(minX + 3, 2, minZ), Blocks.AIR.defaultBlockState(), false);
        // the heights as saved from Y-64: 64 too high in every column
        List<Heightmap.Types> types = new ArrayList<>();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) types.add(entry.getKey());
        for (Heightmap.Types type : types) {
            SimpleBitStorage stale = new SimpleBitStorage(Mth.ceillog2(chunk.getHeight() + 1), 256);
            for (int i = 0; i < 256; i++) stale.set(i, 65 + 64);
            chunk.setHeightmap(type, stale.getRaw());
        }
        helper.assertTrue(!types.isEmpty() && chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 5, 5) == 128,
                "the old heights were not set up: " + types + ", " + chunk.getHeight(Heightmap.Types.WORLD_SURFACE, 5, 5));
        chunk.setUnsaved(false);

        OldLandRepair.repair(chunk);

        int floor = 0, ragged = 0, high = 0, heights = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (chunk.getBlockState(at.set(minX + x, 0, minZ + z)).is(Blocks.BEDROCK)) floor++;
                if (chunk.getBlockState(at.set(minX + x, 1, minZ + z)).is(Blocks.BEDROCK)) ragged++;
                for (int y = 5; y <= 64; y++) if (chunk.getBlockState(at.set(minX + x, y, minZ + z)).is(Blocks.BEDROCK)) high++;
                boolean all = true;
                for (Heightmap.Types type : types) all &= chunk.getHeight(type, x, z) == 64;
                if (all) heights++;
            }
        }
        helper.assertTrue(floor == 254 && chunk.getBlockState(at.set(minX + 2, 0, minZ)).is(Blocks.CHEST)
                        && chunk.getBlockState(at.set(minX + 5, 0, minZ)).is(Blocks.COMPOSTER),
                "the floor at Y0 is bedrock in " + floor + " columns of 254 (the ore's among them), the chest "
                        + chunk.getBlockState(at.set(minX + 2, 0, minZ)));
        helper.assertTrue(ragged > 100 && ragged < 255 && high == 0, "bedrock at Y1 in " + ragged + " columns (the "
                + "original's four in five), above Y4 in " + high);
        helper.assertTrue(chunk.getBlockState(at.set(minX, 1, minZ)).isAir()
                        && chunk.getBlockState(at.set(minX + 3, 2, minZ)).isAir(),
                "a cave over the floor was filled with bedrock");
        helper.assertTrue(heights == 256, "the heights are true in " + heights + " columns of 256");
        helper.assertTrue(chunk.isUnsaved() && !chunk.isLightCorrect(), "the repaired chunk is not marked to be saved and "
                + "lit again");
        helper.succeed();
    }
}
