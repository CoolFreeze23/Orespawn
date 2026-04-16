package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import java.util.List;

public class GenericDungeon {

    private static final List<EntityType<?>> SPAWNER_MOBS = List.of(
            ModEntities.ALIEN.get(),
            ModEntities.CAVE_FISHER.get(),
            ModEntities.DUNGEON_BEAST.get(),
            ModEntities.ENTITY_SCORPION.get(),
            ModEntities.ENTITY_EMPEROR_SCORPION.get(),
            ModEntities.ENTITY_TROOPER_BUG.get(),
            ModEntities.ENTITY_CATER_KILLER.get(),
            ModEntities.ENTITY_MOLENOID.get(),
            ModEntities.BASILISK.get(),
            ModEntities.ENTITY_STINK_BUG.get(),
            ModEntities.ENTITY_TRIFFID.get()
    );

    public static boolean tryPlaceGenericDungeon(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int width = 12, height = 6, depth = 12;
        BlockPos corner = origin.offset(-width / 2, 0, -depth / 2);

        if (!canPlaceAt(level, corner, width, height, depth)) return false;

        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockPos pos = corner.offset(x, y, z);
                    boolean isWall = x == 0 || x == width - 1 || y == 0 || y == height - 1 || z == 0 || z == depth - 1;
                    if (isWall) {
                        level.setBlock(pos, random.nextInt(3) == 0 ? mossy : cobble, 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        BlockPos spawnerPos = corner.offset(width / 2, 1, depth / 2);
        EntityType<?> mobType = SPAWNER_MOBS.get(random.nextInt(SPAWNER_MOBS.size()));
        placeSpawner(level, spawnerPos, mobType);

        BlockPos chestPos = corner.offset(width / 2 + 2, 1, depth / 2 + 2);
        placeChest(level, chestPos, random);

        return true;
    }

    public static boolean tryPlaceRubyDungeon(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int width = 10, height = 5, depth = 10;
        BlockPos corner = origin.offset(-width / 2, 0, -depth / 2);

        if (!canPlaceAt(level, corner, width, height, depth)) return false;

        BlockState rubyOre = ModBlocks.ORE_RUBY.get().defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    BlockPos pos = corner.offset(x, y, z);
                    boolean isWall = x == 0 || x == width - 1 || y == 0 || y == height - 1 || z == 0 || z == depth - 1;
                    if (isWall) {
                        level.setBlock(pos, random.nextInt(3) == 0 ? cobble : rubyOre, 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        BlockPos spawnerPos = corner.offset(width / 2, 1, depth / 2);
        placeSpawner(level, spawnerPos, ModEntities.RUBY_BIRD.get());

        BlockPos chestPos = corner.offset(width / 2 + 2, 1, depth / 2 + 2);
        placeChest(level, chestPos, random);

        return true;
    }

    private static boolean canPlaceAt(WorldGenLevel level, BlockPos corner, int w, int h, int d) {
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                BlockPos top = corner.offset(x, h - 1, z);
                BlockPos bottom = corner.offset(x, 0, z);
                if (!level.getBlockState(top).isSolid()) return false;
                if (!level.getBlockState(bottom).isSolid()) return false;
            }
        }
        return true;
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mobType) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            // Pass null for Level to avoid ServerLevel chunk-loading during worldgen
            spawner.getSpawner().setEntityId(mobType, null, level.getRandom(), pos);
        }
    }

    private static void placeChest(WorldGenLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
        }
    }
}
