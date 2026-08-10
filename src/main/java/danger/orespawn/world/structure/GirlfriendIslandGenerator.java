package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Girlfriend Island (overworld ocean), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeGirlfriendIsland}
 * (orig GenericDungeon.java:4962-5028) &mdash; one of the WGEN-042
 * structure pool, and Monster Island's geometry TWIN (island/tree loops
 * and chest positions byte-identical to orig :5181-5209/:5230-5239). An
 * 11&times;7 lens-shaped sand island (stone underlayer) sitting in the
 * ocean surface, topped by a single oak "palm": 3-block trunk, 5&times;5
 * leaf canopy with four diagonal branch logs and a tip leaf. Four
 * spawners hide in the canopy &mdash; a Girlfriend, a Boyfriend, and two
 * Gold Fish, all FIXED name constants (no Monster-Island-style 50/50 mob
 * roll) &mdash; and two chests flank the trunk.
 *
 * <p><b>Mapping decisions</b> (full tables in
 * {@code phase_d_reports/d6_extraction/girlfriend_island_spec.md}):</p>
 * <ul>
 *   <li><b>RNG:</b> the original's only draws were the two chest fill
 *       counts ({@code 4 + nextInt(5)} on {@code world.rand}, orig
 *       :5021, :5026), both moved into the loot JSON's 4-8 rolls
 *       &mdash; this generator consumes ZERO random draws (spec S4/&sect;11:
 *       geometry, spawner positions, and mob names are all constants).
 *       Do NOT copy Monster Island's up-front mob pick; this method
 *       has no counterpart to it.</li>
 *   <li><b>Leaves:</b> the original placed leaves with meta 0 via
 *       flag-2 {@code FastSetBlock} (orig :4985-4990) &mdash; in 1.7.10
 *       the decay-check bit (8) is unset and no neighbour update ever
 *       fires, so the canopy never decayed. Modern default oak leaves
 *       (persistent=false, distance=7) placed without neighbour updates
 *       would decay on the first random tick, so the port sets
 *       {@code persistent=true} &mdash; the faithful modern equivalent
 *       of the original's never-checked leaves (spec S5).</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/girlfriend_island}
 *       transcribes the 9-entry {@code DamselContentsList}
 *       (orig GenericDungeon.java:39, total weight 315) at THIS
 *       structure's 4-8 rolls. The list is SHARED with
 *       {@code makeDamselInDistress} (bound at orig :3635), which fills
 *       10-14 (orig :3725) &mdash; per the D6b batch-3 ruling (pattern
 *       doc &sect;1 step 5, Kyuubi precedent: one table per (list, fill
 *       formula) pair) the two structures ship separate tables with
 *       identical entries, cross-referenced by a {@code _shared_list}
 *       comment key. The random-slot collision quirk is dropped, the
 *       same documented approximation as every chest-list conversion
 *       since Phase C.</li>
 * </ul>
 */
final class GirlfriendIslandGenerator {

    /** Loot for the two trunk-side chests (orig GenericDungeon.java:39 + :5021). */
    private static final ResourceKey<LootTable> GIRLFRIEND_ISLAND_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/girlfriend_island"));

    private GirlfriendIslandGenerator() {}

    /**
     * Direct port of {@code makeGirlfriendIsland(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:4962-5028). {@code origin} is the
     * water-surface block of the ocean anchor (worldgen passes the scan hit's
     * {@code posY - 1}, orig OreSpawnWorld.java:1390, so the sand layer
     * replaces surface water) or the Dungeon Spawner Block position
     * (outcome 35, orig DungeonSpawnerBlock.java:158-160, no offset).
     * {@code random} is deliberately never drawn from &mdash; the method's
     * RNG stream is empty (class Javadoc).
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

        // orig :4969-4984 — lens-shaped island body: columns i = -5..5, half-
        // width k = 3 by default, narrowed to 1 at the tips (i = ±5) and 2 at
        // i = ±4 and ±3; sand at island level, stone one below.
        for (int i = -5; i <= 5; i++) {
            int k = 3;                                                     // orig :4970
            if (i == -5 || i == 5) {                                       // orig :4971-4973
                k = 1;
            }
            if (i == -4 || i == 4) {                                       // orig :4974-4976
                k = 2;
            }
            if (i == -3 || i == 3) {                                       // orig :4977-4979
                k = 2;
            }
            for (int j = -k; j <= k; j++) {                                // orig :4980
                piece.place(x + i, y, z + j, sand);                        // orig :4981
                piece.place(x + i, y - 1, z + j, stone);                   // orig :4982
            }
        }
        // orig :4985-4989 — 5x5 leaf canopy slab at y+3.
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                piece.place(x + i, y + 3, z + j, leaves);
            }
        }
        piece.place(x, y + 4, z, leaves);                                  // orig :4990 — canopy tip
        // orig :4991-4993 — trunk, written top-down (y+3 overwrites the
        // canopy-centre leaf).
        piece.place(x, y + 3, z, log);
        piece.place(x, y + 2, z, log);
        piece.place(x, y + 1, z, log);
        // orig :4994-4997 — four diagonal branches at canopy level
        // (overwriting loop-2 leaves).
        piece.place(x + 1, y + 3, z + 1, log);
        piece.place(x - 1, y + 3, z - 1, log);
        piece.place(x + 1, y + 3, z - 1, log);
        piece.place(x - 1, y + 3, z + 1, log);
        // orig :4998-5017 — four spawners with FIXED mob names, in the canopy
        // orthogonally around the trunk (overwriting loop-2 leaves): a
        // Girlfriend, a Boyfriend, and two Gold Fish.
        piece.placeSpawner(x + 1, y + 3, z, ModEntities.GIRLFRIEND.get()); // orig :4998-5002
        piece.placeSpawner(x - 1, y + 3, z, ModEntities.BOYFRIEND.get());  // orig :5003-5007
        piece.placeSpawner(x, y + 3, z + 1, ModEntities.GOLD_FISH.get());  // orig :5008-5012
        piece.placeSpawner(x, y + 3, z - 1, ModEntities.GOLD_FISH.get());  // orig :5013-5017
        // orig :5018-5027 — two chests flanking the trunk at y+1, each filled
        // with 4-8 weighted DamselContentsList stacks (counts live in
        // chests/girlfriend_island.json). Placed with meta 0 (no facing) →
        // default facing overload.
        piece.placeLootChest(x, y + 1, z - 1, GIRLFRIEND_ISLAND_LOOT);     // orig :5018-5022
        piece.placeLootChest(x, y + 1, z + 1, GIRLFRIEND_ISLAND_LOOT);     // orig :5023-5027
    }
}
