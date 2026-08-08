package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Monster Island (overworld ocean), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeMonsterIsland}
 * (orig GenericDungeon.java:5170-5240) &mdash; one of the WGEN-042
 * structure pool. An 11&times;7 lens-shaped sand island (stone
 * underlayer) sitting in the ocean surface, topped by a single oak
 * "palm": 3-block trunk, 5&times;5 leaf canopy with four diagonal
 * branch logs and a tip leaf. Four spawners hide in the canopy &mdash;
 * all four of ONE mob, a single 50/50 pick between "Sea Monster" and
 * "Sea Viper" &mdash; and two chests flank the trunk.
 *
 * <p><b>Mapping decisions</b> (full tables in
 * {@code phase_d_reports/d6_extraction/hospital_monster_island_spec.md}
 * &sect;B):</p>
 * <ul>
 *   <li><b>RNG:</b> the original drew the mob pick and both chest fill
 *       counts from {@code world.rand} (orig :5178, :5233, :5238); the
 *       port threads the deterministic per-piece {@link RandomSource}
 *       through the same sequence. The mob pick is the FIRST draw,
 *       before any block write (spec surprise S7) &mdash; kept first
 *       here for stitching parity. The fill counts moved into the loot
 *       JSON.</li>
 *   <li><b>Leaves:</b> the original placed leaves with meta 0 via
 *       flag-2 {@code FastSetBlock} (orig :5197-5202) &mdash; in 1.7.10
 *       the decay-check bit (8) is unset and no neighbour update ever
 *       fires, so the canopy never decayed. Modern default oak leaves
 *       (persistent=false, distance=7) placed without neighbour updates
 *       would decay on the first random tick, so the port sets
 *       {@code persistent=true} &mdash; the faithful modern equivalent
 *       of the original's never-checked leaves.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/monster_island}
 *       transcribes the 14-entry {@code MonsterIslandContentsList}
 *       (orig GenericDungeon.java:30, total weight 450; dye meta 0 =
 *       ink sac, raw fish = cod, log/leaves meta 0 = oak) with the
 *       original 4-8 stack rolls ({@code 4 + nextInt(5)}, orig :5233);
 *       the random-slot collision quirk is dropped, the same documented
 *       approximation as every chest-list conversion since Phase C.</li>
 * </ul>
 */
final class MonsterIslandGenerator {

    /** Loot for the two trunk-side chests (orig GenericDungeon.java:30 + :5233). */
    private static final ResourceKey<LootTable> MONSTER_ISLAND_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/monster_island"));

    private MonsterIslandGenerator() {}

    /**
     * Direct port of {@code makeMonsterIsland(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:5170-5240). {@code origin} is the
     * water-surface block of the ocean anchor (worldgen passes the scan hit's
     * {@code posY - 1}, orig OreSpawnWorld.java:1410, so the sand layer
     * replaces surface water) or the Dungeon Spawner Block position
     * (outcome 37, orig DungeonSpawnerBlock.java:164-166, no offset).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        BlockState sand = Blocks.SAND.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        // Persistent — see the class Javadoc on the 1.7.10 meta-0 decay delta.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(BlockStateProperties.PERSISTENT, true);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        // orig :5176-5180 — the mob pick: default "Sea Viper", 50% flip to
        // "Sea Monster". THE FIRST RNG DRAW, before any block write (spec
        // surprise S7); all four spawners share the one pick.
        EntityType<?> monster = ModEntities.SEA_VIPER.get();
        if (random.nextInt(2) == 0) {
            monster = ModEntities.SEA_MONSTER.get();
        }
        // orig :5181-5196 — lens-shaped island body: columns i = -5..5, half-
        // width k = 3 by default, narrowed to 1 at the tips (i = ±5) and 2 at
        // i = ±4 and ±3; sand at island level, stone one below.
        for (int i = -5; i <= 5; i++) {
            int k = 3;                                                     // orig :5182
            if (i == -5 || i == 5) {                                       // orig :5183-5185
                k = 1;
            }
            if (i == -4 || i == 4) {                                       // orig :5186-5188
                k = 2;
            }
            if (i == -3 || i == 3) {                                       // orig :5189-5191
                k = 2;
            }
            for (int j = -k; j <= k; j++) {                                // orig :5192
                piece.place(x + i, y, z + j, sand);                        // orig :5193
                piece.place(x + i, y - 1, z + j, stone);                   // orig :5194
            }
        }
        // orig :5197-5201 — 5x5 leaf canopy slab at y+3.
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                piece.place(x + i, y + 3, z + j, leaves);
            }
        }
        piece.place(x, y + 4, z, leaves);                                  // orig :5202 — canopy tip
        // orig :5203-5205 — trunk, written top-down (y+3 overwrites the
        // canopy-centre leaf).
        piece.place(x, y + 3, z, log);
        piece.place(x, y + 2, z, log);
        piece.place(x, y + 1, z, log);
        // orig :5206-5209 — four diagonal branches at canopy level
        // (overwriting loop-2 leaves).
        piece.place(x + 1, y + 3, z + 1, log);
        piece.place(x - 1, y + 3, z - 1, log);
        piece.place(x + 1, y + 3, z - 1, log);
        piece.place(x - 1, y + 3, z + 1, log);
        // orig :5210-5229 — four spawners of the picked mob, in the canopy
        // orthogonally around the trunk (overwriting loop-2 leaves).
        piece.placeSpawner(x + 1, y + 3, z, monster);                      // orig :5210-5214
        piece.placeSpawner(x - 1, y + 3, z, monster);                      // orig :5215-5219
        piece.placeSpawner(x, y + 3, z + 1, monster);                      // orig :5220-5224
        piece.placeSpawner(x, y + 3, z - 1, monster);                      // orig :5225-5229
        // orig :5230-5239 — two chests flanking the trunk at y+1, each filled
        // with 4-8 weighted MonsterIslandContentsList stacks (counts live in
        // chests/monster_island.json). Placed with meta 0 (no facing) →
        // default facing overload.
        piece.placeLootChest(x, y + 1, z - 1, MONSTER_ISLAND_LOOT);        // orig :5230-5234
        piece.placeLootChest(x, y + 1, z + 1, MONSTER_ISLAND_LOOT);       // orig :5235-5239
    }
}
