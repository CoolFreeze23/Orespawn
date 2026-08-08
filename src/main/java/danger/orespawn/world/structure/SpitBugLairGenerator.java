package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Spit Bug Lair (vanilla-overworld Swampland), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeSpitBugLair}
 * (orig GenericDungeon.java:2638-2696) &mdash; a lime-terracotta
 * taxicab-diamond platform (radius 8) with a 2-high green-terracotta +
 * chiseled-stone-brick rim wall, a 17-wide 1-thick A-frame fin across it at
 * z = 0, three "Spit Bug" spawners stacked in the fin's core (y+7..+9), a
 * 3-block emerald-ore antenna above them (y+10..+12), and one loot chest at
 * the center floor (y+1).
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/spit_bug_lair_spec.md}):</p>
 * <ul>
 * <li>The original's misleading local color constants {@code green = 5} /
 *     {@code dark_green = 13} (orig :2640-2641) are 1.7.10 stained-clay
 *     indices: 5 = LIME terracotta (platform floor), 13 = GREEN terracotta
 *     (fin courses + rim) &mdash; spec &sect;5/S7.</li>
 * <li>The antenna block {@code Blocks.field_150412_bA} is emerald ORE, not
 *     emerald block (identity confirmed against the mod's emerald-ore
 *     worldgen feed, orig OreSpawnWorld.java:919) &mdash; spec S2.</li>
 * <li>Write order is behavior and is preserved verbatim: fin &rarr; antenna
 *     &rarr; spawners &rarr; platform &rarr; chest. The antenna overwrites
 *     the fin's apex terracotta, spawner #1 overwrites the apex mossy
 *     cobblestone, and the platform pass erases/converts the fin's two
 *     lowest steps on each side (broken-ridge look) &mdash; spec S2/S3;
 *     do not "repair" any of it.</li>
 * <li>The original's only RNG draw, the chest fill count
 *     {@code 4 + nextInt(4)} (orig :2694), moved into the loot JSON's
 *     rolls (uniform 4-7), so this generator consumes ZERO random draws
 *     and satisfies the per-chunk-replay stitching contract vacuously
 *     &mdash; spec S4. Its only world reads were tile-entity fetches at
 *     just-written positions (orig :2659/:2664/:2669/:2692), absorbed by
 *     the {@code placeSpawner}/{@code placeLootChest} helpers.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/spit_bug_lair} transcribes the
 * original 15-entry {@code SpitBugContentsList} (orig GenericDungeon.java:42,
 * total weight 295) with the original 4-7 stack rolls; the random-slot
 * collision quirk is dropped, the same documented approximation as every
 * other chest-list conversion since Phase C.</p>
 */
final class SpitBugLairGenerator {

    /** Loot for the center-floor chest (orig GenericDungeon.java:42 + :2694). */
    private static final ResourceKey<LootTable> LAIR_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/spit_bug_lair"));

    private SpitBugLairGenerator() {}

    /**
     * Direct port of {@code makeSpitBugLair(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:2638-2696). {@code origin} is the first air
     * block above swamp grass (worldgen, orig OreSpawnWorld.java:1249-1250
     * &mdash; NO Y offset, so the platform floor sits one block above the
     * terrain surface, spec S6) or the Dungeon Spawner Block position
     * (outcome 19, orig DungeonSpawnerBlock.java:110-112).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        final int width = 9;                                              // orig :2642
        BlockState greenTerracotta = Blocks.GREEN_TERRACOTTA.defaultBlockState();   // dark_green = 13, orig :2641
        BlockState limeTerracotta = Blocks.LIME_TERRACOTTA.defaultBlockState();     // green = 5, orig :2640
        BlockState mossyCobble = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState chiseledStoneBricks = Blocks.CHISELED_STONE_BRICKS.defaultBlockState(); // stonebrick meta 3
        BlockState emeraldOre = Blocks.EMERALD_ORE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop 1 — triangular A-frame fin in the XY plane at z = 0
        // (orig :2647-2654). Per i = 0..8, at both +i and -i: two courses of
        // green terracotta over one mossy-cobblestone diagonal step. The
        // i = 0 pair writes the same three center cells twice — harmless,
        // kept for fidelity.
        for (int i = 0; i < width; i++) {
            piece.place(cx + i, cy + width - i + 2, cz, greenTerracotta); // orig :2648
            piece.place(cx + i, cy + width - i + 1, cz, greenTerracotta); // orig :2649
            piece.place(cx + i, cy + width - i, cz, mossyCobble);         // orig :2650
            piece.place(cx - i, cy + width - i + 2, cz, greenTerracotta); // orig :2651
            piece.place(cx - i, cy + width - i + 1, cz, greenTerracotta); // orig :2652
            piece.place(cx - i, cy + width - i, cz, mossyCobble);         // orig :2653
        }

        // Emerald-ore antenna, center column y+12/+11/+10 (orig :2655-2657).
        // The lower two overwrite the fin's apex terracotta (spec S2).
        piece.place(cx, cy + width + 3, cz, emeraldOre);                  // orig :2655
        piece.place(cx, cy + width + 2, cz, emeraldOre);                  // orig :2656
        piece.place(cx, cy + width + 1, cz, emeraldOre);                  // orig :2657

        // Three stacked "Spit Bug" spawners, y+9/+8/+7 (orig :2658-2672).
        // #1 overwrites the fin's apex mossy cobblestone (spec S2).
        piece.placeSpawner(cx, cy + width, cz, ModEntities.ENTITY_SPIT_BUG.get());     // orig :2658-2662
        piece.placeSpawner(cx, cy + width - 1, cz, ModEntities.ENTITY_SPIT_BUG.get()); // orig :2663-2667
        piece.placeSpawner(cx, cy + width - 2, cz, ModEntities.ENTITY_SPIT_BUG.get()); // orig :2668-2672

        // Loop 2 — taxicab-diamond platform |dx|+|dz| <= 8 (orig :2674-2690).
        // Two mirrored columns per (i, j): west x = cx-8+i, east x = cx+8-i
        // (collapsing to the same cells at i = 8). Floor of lime terracotta
        // everywhere; on the rim (j == +-i) a green-terracotta + chiseled-
        // stone-brick wall at y+1/y+2; in the interior, air at y+1/y+2.
        // Runs AFTER loop 1, partially erasing the fin's lowest steps at
        // z = 0 — original behavior, preserved (spec S3, §2.5).
        for (int i = 0; i < width; i++) {
            for (int j = -i; j <= i; j++) {
                int xWest = cx - width + i + 1;
                int xEast = cx + width - i - 1;
                piece.place(xWest, cy, cz + j, limeTerracotta);           // orig :2676
                piece.place(xEast, cy, cz + j, limeTerracotta);           // orig :2677
                if (j == -i || j == i) {
                    piece.place(xWest, cy + 1, cz + j, greenTerracotta);       // orig :2679
                    piece.place(xEast, cy + 1, cz + j, greenTerracotta);       // orig :2680
                    piece.place(xWest, cy + 2, cz + j, chiseledStoneBricks);   // orig :2681
                    piece.place(xEast, cy + 2, cz + j, chiseledStoneBricks);   // orig :2682
                    continue;
                }
                piece.place(xWest, cy + 1, cz + j, air);                  // orig :2685
                piece.place(xEast, cy + 1, cz + j, air);                  // orig :2686
                piece.place(xWest, cy + 2, cz + j, air);                  // orig :2687
                piece.place(xEast, cy + 2, cz + j, air);                  // orig :2688
            }
        }

        // Loot chest at the center floor, y+1 (orig :2691-2695); the interior
        // rule just cleared this cell. Fill count 4 + nextInt(4) lives in the
        // loot table's rolls (uniform 4-7).
        piece.placeLootChest(cx, cy + 1, cz, LAIR_LOOT);
    }
}
