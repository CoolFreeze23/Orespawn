package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Leaf Monster Dungeon (vanilla-overworld Plains), ported line-by-line
 * from 1.7.10 {@code GenericDungeon.makeLeafMonsterDungeon}
 * (orig GenericDungeon.java:2093-2226) &mdash; a 4&times;4 oak-log lookout
 * tower 10 tall with a 2-wide north doorway, an interior ladder shaft up to a
 * 10&times;10 log platform at y+9, a 3-tall leaf-walled crown room holding
 * four "Leaf Monster" spawners and a chest pair, capped by a stepped log/leaf
 * roof (y+13..+16), with log foundation "roots" under the footprint
 * (y&minus;1..&minus;4).
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/leaf_monster_dungeon_spec.md}):</p>
 * <ul>
 * <li><b>Foundation-root terrain probe (orig :2113, spec &sect;9.1/S2)
 *     &mdash; faithful.</b> Each foundation log at {@code j = -1..-4} is
 *     gated on the PRE-BUILD terrain being air or tallgrass
 *     ({@code field_150329_H} &rarr; modern {@code short_grass}), leaving
 *     solid ground &mdash; including the doorway threshold and the
 *     deliberately floor-less interior (spec S3) &mdash; untouched.
 *     Ported with {@code LegacyDungeonPiece.terrainStateIfInChunk}, the
 *     sanctioned read-at-write-cell probe (stitch-safe: read cell ==
 *     write cell). An interim revision of this class filled the 64 cells
 *     unconditionally; the D6b batch-2 verification pass flagged it and
 *     the probe was restored.</li>
 * <li><b>Leaf decay adaptation (spec S5):</b> every oak-leaves write uses
 *     {@code PERSISTENT=true} &mdash; the crown walls sit on the plateau's
 *     leaf rim and exceed {@code DISTANCE=7} from any log, so default-state
 *     modern leaves would decay on random ticks. Same adaptation as
 *     {@link MonsterIslandGenerator}/{@link WaterDragonLairGenerator} and the
 *     pattern-doc &sect;4 tree rule; the original's flag-2 meta-0 leaves had
 *     no decay hazard.</li>
 * <li><b>Ladder flags (spec S4):</b> the 2&times;10 ladder wall was the
 *     method's only flag-3 write (orig :2139, meta 2 = facing north). The
 *     supporting {@code k = +3} south wall is placed by the shell loop BEFORE
 *     the ladder loop runs, so the neighbour check was a no-op and the port's
 *     flag-2 {@code piece.place} yields the identical final state.</li>
 * <li><b>Roof-ring corner notches (spec S6):</b> in the y+13 layer the
 *     leaf-ring {@code if} runs AFTER the log-ring {@code if} and wins on the
 *     8 cells where both apply, visibly notching the outer log square near
 *     its corners. Faithful; the check order is replicated, not "repaired".</li>
 * <li><b>Double-chest cosmetics (spec S7):</b> the original placed two
 *     adjacent chests (orig :2221-2222) that 1.7.10 auto-merged into a double
 *     chest; modern default-state chests stay singles (both face north, into
 *     the room &mdash; the default {@code HORIZONTAL_FACING}). Accepted as a
 *     documented cosmetic deviation, the {@link PlayPoolGenerator} precedent;
 *     ONLY the {@code (+1, +10, +5)} half is loot-bound (orig :2223-2226),
 *     the {@code (+2)} half is deliberately empty &mdash; do not "fix". A
 *     ChestType-aware {@code placeLootChest} overload on the piece would
 *     restore the visual merge (same follow-up boundary as the terrain
 *     probe).</li>
 * <li><b>RNG (spec &sect;10):</b> the original's only draw, the chest fill
 *     count {@code 12 + nextInt(5)} (orig :2225), moved into the loot JSON's
 *     rolls (uniform 12-16), so this generator consumes ZERO random draws and
 *     satisfies the per-chunk-replay stitching contract vacuously. Its other
 *     world reads were tile-entity fetches at just-written positions
 *     (orig :2202/:2207/:2212/:2217/:2223), absorbed by the
 *     {@code placeSpawner}/{@code placeLootChest} helpers.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/leaf_monster_dungeon} transcribes the
 * original 9-entry {@code LeafMonsterContentsList} (orig
 * GenericDungeon.java:46, total weight 255) with the original 12-16 stack
 * rolls &mdash; INCLUDING the two intentional duplicate pairs (flower pot
 * &times;2, oak sapling &times;2, spec S9) and the proven flower-pot identity
 * for {@code Items.field_151162_bE} (ItemSifter case 5, spec S8 &mdash; NOT
 * the greenhouse fill's apple mis-mapping). The random-slot collision quirk
 * is dropped, the same documented approximation as every chest-list
 * conversion since Phase C.</p>
 */
final class LeafMonsterDungeonGenerator {

    /** Loot for the (+1, +10, +5) chest half only (orig GenericDungeon.java:46 + :2223-2226). */
    private static final ResourceKey<LootTable> DUNGEON_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/leaf_monster_dungeon"));

    private LeafMonsterDungeonGenerator() {}

    /**
     * Direct port of {@code makeLeafMonsterDungeon(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:2093-2226). {@code origin} is the first air
     * block directly ABOVE a Plains grass block (worldgen,
     * orig OreSpawnWorld.java:1207-1208 &mdash; NO Y offset, so the tower's
     * {@code j = 0} course sits one block above the terrain surface and the
     * doorway threshold is the grass itself, spec S11) or the Dungeon Spawner
     * Block position (type 15, orig DungeonSpawnerBlock.java:98-100 &mdash; no
     * validation there: a floating or terrain-embedded tower is faithful
     * behavior). Write order is behavior and is preserved verbatim: clearing
     * &rarr; foundation &rarr; shell &rarr; ladders &rarr; awning &rarr;
     * plateau &rarr; crown walls &rarr; roof rings &rarr; spawners &rarr;
     * chests.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();          // field_150364_r meta 0 (axis=y)
        // PERSISTENT=true — see the class Javadoc on the leaf-decay delta.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(BlockStateProperties.PERSISTENT, true);
        // meta 2 = facing north, attached to the k = +3 south log wall.
        BlockState ladder = Blocks.LADDER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        // Loop 1 — yard/base clearing (orig :2102-2108): 8×5×4 air over the
        // door-side yard and the tower's own front columns. Terrain around
        // the tower past k = +1 is NOT cleared — a tower embedded in a
        // hillside is faithful (spec §2.1).
        for (int i = -2; i < 6; i++) {
            for (int k = -3; k < 2; k++) {
                for (int j = 0; j < 4; j++) {
                    piece.place(cx + i, cy + j, cz + k, air);          // orig :2105
                }
            }
        }

        // Loop 2 — foundation roots (orig :2109-2118): a log ONLY where the
        // pre-build terrain probe (orig :2113-2114) finds air or tallgrass
        // (field_150329_H → modern short_grass), leaving solid ground —
        // including the doorway threshold and interior floor — untouched.
        // Read-at-write-cell probe via the piece's sanctioned
        // terrainStateIfInChunk helper (added in the D6b batch-2 verification
        // pass, replacing this class's interim treat-as-air deviation).
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = -1; j > -5; j--) {
                    var terrain = piece.terrainStateIfInChunk(cx + i, cy + j, cz + k);
                    if (terrain == null) continue;
                    if (!terrain.isAir() && !terrain.is(Blocks.SHORT_GRASS)) continue; // orig :2113-2114
                    piece.place(cx + i, cy + j, cz + k, log);          // orig :2112/:2115
                }
            }
        }

        // Loop 3 — 4×4 tower shell, 10 tall (orig :2119-2133). Carve rules in
        // source order: 2×2-footprint doorway at j < 2 on the north face,
        // then the full-height 2×1 air shaft at k = 1 (its j = 9 opening is
        // the plateau exit hole), then `continue` on the k = 2 ladder cells —
        // this loop never writes them (spec §2.3).
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = 0; j < 10; j++) {
                    BlockState blk = log;                              // orig :2122
                    if (j < 2 && (k == 0 || k == 1) && (i == 1 || i == 2)) {
                        blk = air;                                     // orig :2123-2125 — doorway
                    }
                    if (k == 1 && (i == 1 || i == 2)) {
                        blk = air;                                     // orig :2126-2128 — shaft
                    }
                    if (k == 2 && (i == 1 || i == 2)) {
                        continue;                                      // orig :2129 — ladder cells
                    }
                    piece.place(cx + i, cy + j, cz + k, blk);          // orig :2130
                }
            }
        }

        // Loop 4 — 2×10 ladder wall (orig :2134-2142) on the k = 2 cells the
        // shell loop skipped, climbable from the k = 1 shaft. The supporting
        // south wall (k = 3) already exists — see the class Javadoc on the
        // original's flag-3 write here being a behavioral no-op.
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = 0; j < 10; j++) {
                    if (k != 2 || (i != 1 && i != 2)) {
                        continue;                                      // orig :2137
                    }
                    piece.place(cx + i, cy + j, cz + k, ladder);       // orig :2138-2139
                }
            }
        }

        // Door awning — two leaf blocks floating over the doorway in the
        // cleared yard (orig :2143-2144), each side-adjacent to the front wall.
        piece.place(cx + 1, cy + 2, cz - 1, leaves);                   // orig :2143
        piece.place(cx + 2, cy + 2, cz - 1, leaves);                   // orig :2144

        // Loop 5 — 10×10 plateau ring at j = 9 (orig :2145-2155): skips the
        // 4×4 tower core (whose j = 9 layer the shell loop already wrote —
        // log except the shaft hole and ladder cells); oak-leaves outermost
        // rim, oak log elsewhere.
        for (int i = -3; i < 7; i++) {
            for (int k = -3; k < 7; k++) {
                if (i >= 0 && i <= 3 && k >= 0 && k <= 3) {
                    continue;                                          // orig :2148 — tower core
                }
                BlockState blk = log;                                  // orig :2149
                if (i == -3 || i == 6 || k == -3 || k == 6) {
                    blk = leaves;                                      // orig :2150-2152 — rim
                }
                piece.place(cx + i, cy + 9, cz + k, blk);              // orig :2147/:2153
            }
        }

        // Loop 6 — crown-room walls, j = 10..12 (orig :2156-2166): 10×10
        // leaf-walled room, 3 tall, interior air (also clears any terrain
        // inside), floored by loop 5's platform.
        for (int i = -3; i < 7; i++) {
            for (int k = -3; k < 7; k++) {
                for (int j = 10; j < 13; j++) {
                    BlockState blk = air;                              // orig :2159
                    if (i == -3 || i == 6 || k == -3 || k == 6) {
                        blk = leaves;                                  // orig :2160-2162
                    }
                    piece.place(cx + i, cy + j, cz + k, blk);          // orig :2163
                }
            }
        }

        // Loop 7 — j = 13 roof ring layer (orig :2167-2179): 8×8, outer log
        // square then inner leaf square, 4×4 air centre. The leaf `if` runs
        // AFTER the log `if` and overwrites it on the 8 cells where the two
        // rings intersect near the corners (spec S6) — faithful, keep order.
        for (int i = -2; i < 6; i++) {
            for (int k = -2; k < 6; k++) {
                BlockState blk = air;                                  // orig :2170
                if (i == -2 || i == 5 || k == -2 || k == 5) {
                    blk = log;                                         // orig :2171-2173 — outer square
                }
                if (i == -1 || i == 4 || k == -1 || k == 4) {
                    blk = leaves;                                      // orig :2174-2176 — inner square, wins overlaps
                }
                piece.place(cx + i, cy + 13, cz + k, blk);             // orig :2169/:2177
            }
        }

        // Loop 8 — j = 14 roof cap course: 6×6 leaves (orig :2180-2186).
        for (int i = -1; i < 5; i++) {
            for (int k = -1; k < 5; k++) {
                piece.place(cx + i, cy + 14, cz + k, leaves);          // orig :2182-2184
            }
        }

        // Loop 9 — j = 15 roof cap course: 4×4 log (orig :2187-2193).
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                piece.place(cx + i, cy + 15, cz + k, log);             // orig :2189-2191
            }
        }

        // Loop 10 — j = 16 roof tip: 2×2 leaves (orig :2194-2200).
        for (int i = 1; i < 3; i++) {
            for (int k = 1; k < 3; k++) {
                piece.place(cx + i, cy + 16, cz + k, leaves);          // orig :2196-2198
            }
        }

        // Four "Leaf Monster" spawners at the crown room's inner corners,
        // standing on the plateau (each overwrites a loop-6 interior air
        // cell). Mob mapping: "Leaf Monster" (orig OreSpawnMain.java:4111) →
        // orespawn:leaf_monster (spec §4).
        piece.placeSpawner(cx - 2, cy + 10, cz - 2, ModEntities.ENTITY_LEAF_MONSTER.get()); // orig :2201-2205
        piece.placeSpawner(cx + 5, cy + 10, cz + 5, ModEntities.ENTITY_LEAF_MONSTER.get()); // orig :2206-2210
        piece.placeSpawner(cx - 2, cy + 10, cz + 5, ModEntities.ENTITY_LEAF_MONSTER.get()); // orig :2211-2215
        piece.placeSpawner(cx + 5, cy + 10, cz - 2, ModEntities.ENTITY_LEAF_MONSTER.get()); // orig :2216-2220

        // Chest pair against the south crown wall (orig :2221-2222). ONLY the
        // +1 half gets the loot fill (orig fetches its tile entity alone,
        // :2223-2226 — fill count 12 + nextInt(5) lives in the loot table's
        // 12-16 rolls); the +2 chest is deliberately empty. Both are
        // default-state singles facing north — see the class Javadoc on the
        // double-chest cosmetic deviation.
        piece.placeLootChest(cx + 1, cy + 10, cz + 5, DUNGEON_LOOT);   // orig :2221 + :2223-2226
        piece.place(cx + 2, cy + 10, cz + 5, Blocks.CHEST.defaultBlockState()); // orig :2222
    }
}
