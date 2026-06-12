package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.List;

/**
 * The OreSpawn cobblestone box dungeons, ported from 1.7.10
 * {@code GenericDungeon.makeDungeon} (orig GenericDungeon.java:97-185) and
 * {@code RubyBirdDungeon.makeDungeon} (orig RubyBirdDungeon.java:30-82).
 *
 * <p>Geometry (both): hollow box carved as air, mossy-cobblestone floor, walls
 * and ceiling a random mix per block. Spawner dead centre at floor+1, chest at
 * {@code (corner + width/2, +1, corner + 1)}. Generic: 12x12 footprint, 6
 * tall. Ruby: 10x10, 5 tall, with a 1-in-20 ruby-ore roll mixed into the
 * wall/ceiling blocks and a fixed Ruby Bird spawner.</p>
 *
 * <p>Chest loot is data-driven: {@code orespawn:chests/generic_dungeon}
 * transcribes the original's 93-entry {@code chestContentsList}
 * (orig GenericDungeon.java:62, 5+nextInt(7) rolls) and
 * {@code orespawn:chests/ruby_dungeon} the 14-entry list
 * (orig RubyBirdDungeon.java:18, 4+nextInt(7) rolls).</p>
 */
public class GenericDungeon {

    /** Loot for the generic dungeon chest (orig GenericDungeon.java:62 + :183). */
    private static final ResourceKey<LootTable> GENERIC_DUNGEON_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/generic_dungeon"));

    /** Loot for the Ruby Bird dungeon chest (orig RubyBirdDungeon.java:18 + :80). */
    private static final ResourceKey<LootTable> RUBY_DUNGEON_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/ruby_dungeon"));

    /**
     * The 12-mob spawner pool, exactly the {@code nextInt(12)} ladder in
     * orig GenericDungeon.java:141-177: Scorpion, Alien, Cryolophosaurus,
     * "WTF?" (ported as alien_boss), Kyuubi, Bee, Cloud Shark, Lurking Terror,
     * Terrible Terror, Rotator, Rat, Dungeon Beast.
     */
    private static final List<java.util.function.Supplier<EntityType<?>>> SPAWNER_MOBS = List.of(
            ModEntities.ENTITY_SCORPION::get,        // t==0  "Scorpion"
            ModEntities.ALIEN::get,                  // t==1  "Alien"
            ModEntities.CRYOLOPHOSAURUS::get,        // t==2  "Cryolophosaurus"
            ModEntities.ALIEN_BOSS::get,             // t==3  "WTF?"
            ModEntities.ENTITY_KYUUBI::get,          // t==4  "Kyuubi"
            ModEntities.ENTITY_BEE::get,             // t==5  "Bee"
            ModEntities.CLOUD_SHARK::get,            // t==6  "Cloud Shark"
            ModEntities.ENTITY_LURKING_TERROR::get,  // t==7  "Lurking Terror"
            ModEntities.ENTITY_TERRIBLE_TERROR::get, // t==8  "Terrible Terror"
            ModEntities.ENTITY_ROTATOR::get,         // t==9  "Rotator"
            ModEntities.ENTITY_RAT::get,             // t==10 "Rat"
            ModEntities.DUNGEON_BEAST::get           // t==11 "Dungeon Beast"
    );

    /**
     * Generic dungeon roll + placement, faithful to
     * {@code OreSpawnWorld.addGenericDungeon} (orig OreSpawnWorld.java:2014-2029):
     * 1-in-16 gate, corner at {@code chunk + nextInt(4)}, floor at
     * {@code 5 + nextInt(40)}, carved unconditionally (no solidity pre-check
     * and no cooldown in the original).
     *
     * @param cx chunk min block X, {@code cz} chunk min block Z
     */
    public static boolean tryPlaceGenericDungeon(WorldGenLevel level, RandomSource random, int cx, int cz) {
        // orig OreSpawnWorld.java:2015 — 1/16 gate (LessLag=0 default)
        if (random.nextInt(16) != 0) return false;
        int x = cx + random.nextInt(4);
        int z = cz + random.nextInt(4);
        int y = 5 + random.nextInt(40);
        makeGenericDungeon(level, random, new BlockPos(x, y, z));
        return true;
    }

    /**
     * Ruby Bird dungeon for Utopia, faithful to
     * {@code OreSpawnWorld.addRubyDungeon} (orig OreSpawnWorld.java:1998-2012):
     * 1-in-15 gate, then 8 attempts each scanning {@code chunk + nextInt(8)}
     * from Y50 down to Y6 for a LAVA block; the dungeon is carved with its
     * corner AT the lava position.
     */
    public static boolean tryPlaceRubyDungeon(WorldGenLevel level, RandomSource random, int cx, int cz) {
        // orig OreSpawnWorld.java:1999 — 1/15 gate
        if (random.nextInt(15) != 0) return false;
        for (int i = 0; i < 8; i++) {
            int x = cx + random.nextInt(8);
            int z = cz + random.nextInt(8);
            for (int y = 50; y > 5; y--) {
                if (!level.getBlockState(new BlockPos(x, y, z)).is(Blocks.LAVA)) continue;
                makeRubyDungeon(level, random, new BlockPos(x, y, z));
                return true;
            }
        }
        return false;
    }

    /**
     * Islands-dimension generic dungeon, faithful to the D4 dispatch
     * (orig OreSpawnWorld.java:134-139 — 1/100 structure roll, share 4/19 →
     * 1/475 chunks) and {@code addD4GenericDungeon}
     * (orig OreSpawnWorld.java:2438-2452): corner at {@code chunk + nextInt(8)},
     * scanning Y20 down to Y5 for grass; carved at the grass level so it sits
     * embedded in the flat island plane. The original's shared
     * {@code recently_placed}/D4BigSpaceCheck gating belongs to the
     * structure-set machinery and is approximated by the flat 1/475 roll.
     */
    public static boolean tryPlaceIslandsGenericDungeon(WorldGenLevel level, RandomSource random, int cx, int cz) {
        if (random.nextInt(475) != 0) return false;
        int x = cx + random.nextInt(8);
        int z = cz + random.nextInt(8);
        for (int y = 20; y > 4; y--) {
            if (!level.getBlockState(new BlockPos(x, y, z)).is(Blocks.GRASS_BLOCK)) continue;
            makeGenericDungeon(level, random, new BlockPos(x, y, z));
            return true;
        }
        return false;
    }

    /**
     * Direct build at a fixed corner, no chance gate — used by the Dungeon
     * Spawner block's outcome 21 (orig DungeonSpawnerBlock.java:94-96 calls
     * {@code GenericDungeon.makeDungeon(world, random, x, y, z)} immediately).
     */
    public static boolean placeGenericDungeonAt(WorldGenLevel level, RandomSource random, BlockPos corner) {
        makeGenericDungeon(level, random, corner);
        return true;
    }

    /**
     * Direct build at a fixed corner, no chance gate — Dungeon Spawner block
     * outcome 22 (orig DungeonSpawnerBlock.java:97-99 calls
     * {@code RubyBirdDungeon.makeDungeon} immediately).
     */
    public static boolean placeRubyDungeonAt(WorldGenLevel level, RandomSource random, BlockPos corner) {
        makeRubyDungeon(level, random, corner);
        return true;
    }

    /** Box build per orig GenericDungeon.makeDungeon (orig GenericDungeon.java:97-185). */
    private static void makeGenericDungeon(WorldGenLevel level, RandomSource random, BlockPos corner) {
        int width = 12, height = 6;
        carveBox(level, random, corner, width, height, null);

        // orig GenericDungeon.java:138-178 — spawner at centre, 12-mob pool
        BlockPos spawnerPos = corner.offset(width / 2, 1, width / 2);
        placeSpawner(level, spawnerPos, SPAWNER_MOBS.get(random.nextInt(SPAWNER_MOBS.size())).get());

        // orig GenericDungeon.java:180-184 — chest at (width/2, 1, 1)
        placeChest(level, corner.offset(width / 2, 1, 1), GENERIC_DUNGEON_LOOT);
    }

    /** Box build per orig RubyBirdDungeon.makeDungeon (orig RubyBirdDungeon.java:30-82). */
    private static void makeRubyDungeon(WorldGenLevel level, RandomSource random, BlockPos corner) {
        int width = 10, height = 5;
        carveBox(level, random, corner, width, height, ModBlocks.ORE_RUBY.get().defaultBlockState());

        // orig RubyBirdDungeon.java:71-75 — fixed Ruby Bird spawner at centre
        BlockPos spawnerPos = corner.offset(width / 2, 1, width / 2);
        placeSpawner(level, spawnerPos, ModEntities.RUBY_BIRD.get());

        // orig RubyBirdDungeon.java:77-81 — chest at (width/2, 1, 1)
        placeChest(level, corner.offset(width / 2, 1, 1), RUBY_DUNGEON_LOOT);
    }

    /**
     * Shared box carve: air interior, mossy floor, mixed walls/ceiling.
     * {@code rubyOre} non-null enables the Ruby dungeon's 1-in-20 ruby-ore
     * roll in the wall mix (orig RubyBirdDungeon.java:20-28); otherwise the
     * mix is the generic 50/50 mossy/cobble (orig GenericDungeon.java:67-73).
     */
    private static void carveBox(WorldGenLevel level, RandomSource random, BlockPos corner,
                                 int width, int height, BlockState rubyOre) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();

        // orig GenericDungeon.java:103-109 — carve the full box to air first
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    level.setBlock(corner.offset(i, j, k), air, 2);
                }
            }
        }
        // orig GenericDungeon.java:110-115 — floor: always mossy cobblestone
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                level.setBlock(corner.offset(i, 0, k), mossy, 2);
            }
        }
        // orig GenericDungeon.java:116-121 — ceiling: random mix
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                level.setBlock(corner.offset(i, height - 1, k), wallMix(random, rubyOre), 2);
            }
        }
        // orig GenericDungeon.java:122-137 — four side walls: random mix
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                level.setBlock(corner.offset(i, j, 0), wallMix(random, rubyOre), 2);
                level.setBlock(corner.offset(i, j, width - 1), wallMix(random, rubyOre), 2);
                level.setBlock(corner.offset(0, j, i), wallMix(random, rubyOre), 2);
                level.setBlock(corner.offset(width - 1, j, i), wallMix(random, rubyOre), 2);
            }
        }
    }

    /**
     * Wall block picker. Generic (orig GenericDungeon.java:67-73): 50/50
     * mossy/cobble. Ruby (orig RubyBirdDungeon.java:20-28): 1/20 ruby ore
     * first, then the same 50/50.
     */
    private static BlockState wallMix(RandomSource random, BlockState rubyOre) {
        if (rubyOre != null && random.nextInt(20) == 1) {
            return rubyOre;
        }
        return random.nextInt(2) == 1
                ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                : Blocks.COBBLESTONE.defaultBlockState();
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mobType) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            // Pass null for Level to avoid ServerLevel chunk-loading during worldgen
            spawner.getSpawner().setEntityId(mobType, null, level.getRandom(), pos);
        }
    }

    private static void placeChest(WorldGenLevel level, BlockPos pos, ResourceKey<LootTable> lootTable) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(lootTable);
        }
    }
}
