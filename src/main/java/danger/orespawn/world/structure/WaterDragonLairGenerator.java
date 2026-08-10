package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
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
 * The Water Dragon Lair (overworld ocean), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeWaterDragonLair}
 * (orig GenericDungeon.java:1959-2057) &mdash; one of the WGEN-042
 * structure pool. An open-air ocean-surface arena: a radius-10 rim wall six
 * high (glowstone courses at y+1/+4, a two-course lapis band at y+2..+3
 * ~50% studded with harvestable Water Dragon spawn-egg blocks, bedrock
 * parapet at y+5..+6), roofed at y+7 by a bedrock disc bearing an
 * iron-block annulus (radius 5-6) and iron cross, with a central 1&times;1
 * air skylight ringed by glowstone. Inside, on the water: a 7&times;7
 * floating sand raft (stone underlayer) carrying a 3-log oak "palm" whose
 * 5&times;5 canopy holds four "Water Dragon" spawners and four diagonal
 * branch logs, plus a loot chest on the raft beside the trunk.
 *
 * <p><b>Mapping decisions</b> (full tables in
 * {@code phase_d_reports/d6_extraction/water_dragon_lair_spec.md}):</p>
 * <ul>
 *   <li><b>Float-polar rounding (spec S2):</b> the roof disc, spokes,
 *       center cross, and rim all address cells as
 *       {@code (int)((float)c + off + 0.5f)} (orig :1980, :1984-1993,
 *       :1998-2011) while the pad/canopy/tree/spawners/chest use plain
 *       ints (orig :2013-2052). At negative world coordinates Java's
 *       truncation-toward-zero shifts the whole roof/rim assembly +1 per
 *       negative axis relative to the raft &mdash; original,
 *       coordinate-sign-dependent behavior. Transcribed VERBATIM
 *       (precedent {@code CrystalStructures.buildCrystalBattleTower},
 *       CrystalStructures.java:868); the piece helpers take absolute
 *       world coordinates, so the quirk reproduces itself. {@code currad}/
 *       {@code curdeg} stay float accumulators ({@code += 0.33f} /
 *       {@code += 5.0f}), never recomputed multiples.</li>
 *   <li><b>RNG:</b> the original drew 144 {@code nextInt(2)} rim-block
 *       picks from {@code world.rand} (orig :2000, :2005 &mdash; two per
 *       angle, 72 angles); the port threads the deterministic per-piece
 *       {@link RandomSource} through the identical unconditional sequence
 *       and lets the gated {@code piece.place} drop out-of-chunk writes
 *       (stitching contract). Mirrored angles rounding to the same cell
 *       simply overwrite with a fresh draw &mdash; per-cell odds stay
 *       50/50; never deduplicated (spec S9). The chest fill count
 *       {@code 4 + nextInt(5)} (orig :2055) moved into the loot JSON's
 *       rolls, leaving exactly 144 draws per replay pass. Its only world
 *       reads were tile-entity fetches at just-written positions
 *       (orig :2033/:2038/:2043/:2048/:2053), absorbed by the
 *       {@code placeSpawner}/{@code placeLootChest} helpers &mdash; no
 *       in-memory model needed (spec &sect;9).</li>
 *   <li><b>Leaves:</b> meta-0 flag-2 leaves never decayed in 1.7.10;
 *       modern default oak leaves placed without neighbour updates decay
 *       on the first random tick, so the port sets {@code persistent=true}
 *       &mdash; established adaptation (spec S5, precedent
 *       MonsterIslandGenerator).</li>
 *   <li><b>Skylight (spec S3):</b> the roof center is deliberately set to
 *       AIR (orig :1989) after the disc and spokes &mdash; the arena's
 *       only through-roof opening. Not filled.</li>
 *   <li><b>Interior (spec S6):</b> everything between the raft edge and
 *       the rim at y &ge; 0 is never written &mdash; open ocean surface at
 *       worldgen, whatever was there via the Dungeon Spawner Block path.
 *       Not floored over.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/water_dragon_lair}
 *       transcribes the 7-entry {@code WaterDragonContentsList}
 *       (orig GenericDungeon.java:48, total weight 145; raw fish = cod)
 *       with the original 4-8 stack rolls ({@code 4 + nextInt(5)},
 *       orig :2055); the random-slot collision quirk is dropped, the same
 *       documented approximation as every chest-list conversion since
 *       Phase C.</li>
 * </ul>
 */
final class WaterDragonLairGenerator {

    /** Loot for the raft-side chest (orig GenericDungeon.java:48 + :2055). */
    private static final ResourceKey<LootTable> LAIR_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/water_dragon_lair"));

    private WaterDragonLairGenerator() {}

    /**
     * Direct port of {@code makeWaterDragonLair(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:1959-2057). {@code origin} is the ocean's
     * water-surface block (worldgen passes the scan hit's {@code posY - 1},
     * orig OreSpawnWorld.java:1370, so the sand raft replaces surface water)
     * or the Dungeon Spawner Block position (outcome 13,
     * orig DungeonSpawnerBlock.java:92-94, no offset).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState ironBlock = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState lapisBlock = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState spawnEgg = ModBlocks.WATER_DRAGON_SPAWN_BLOCK.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState sand = Blocks.SAND.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        // Persistent — see the class Javadoc on the 1.7.10 meta-0 decay delta.
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(BlockStateProperties.PERSISTENT, true);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();

        float radius = 10.0f;                                             // orig :1971

        // Loop A — roof disc at y+7 (orig :1972-1982): 31 float radius
        // steps (0.00..9.90) x 72 angles; bedrock, with an iron-block
        // annulus on the three steps in (5, 6) exclusive (5.28/5.61/5.94).
        // At currad = 0 all 72 angles write the same center cell — kept.
        for (float currad = 0.0f; currad < radius; currad += 0.33f) {
            for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));  // orig :1974
                float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));  // orig :1975
                BlockState blk = bedrock;                                 // orig :1976
                if (currad > 5.0f && currad < 6.0f) {                     // orig :1977-1979
                    blk = ironBlock;
                }
                piece.place((int) (cx + curx + 0.5f), cy + 7, (int) (cz + curz + 0.5f), blk); // orig :1980
            }
        }
        // Loop B — iron cross spokes at y+7, rel 1..9 on both axes
        // (orig :1983-1988), overwriting disc bedrock. The (int)(v + 0.5f)
        // idiom on integer sums is kept verbatim (spec S2).
        for (int i = 1; i < 10; i++) {
            piece.place((int) ((float) (cx + i) + 0.5f), cy + 7, (int) ((float) cz + 0.5f), ironBlock); // orig :1984
            piece.place((int) ((float) (cx - i) + 0.5f), cy + 7, (int) ((float) cz + 0.5f), ironBlock); // orig :1985
            piece.place((int) ((float) cx + 0.5f), cy + 7, (int) ((float) (cz + i) + 0.5f), ironBlock); // orig :1986
            piece.place((int) ((float) cx + 0.5f), cy + 7, (int) ((float) (cz - i) + 0.5f), ironBlock); // orig :1987
        }
        // Roof center — 1x1 air skylight (spec S3) ringed by glowstone,
        // overwriting the disc center and the spokes' rel-1 iron
        // (orig :1989-1993).
        piece.place((int) ((float) cx + 0.5f), cy + 7, (int) ((float) cz + 0.5f), air);             // orig :1989
        piece.place((int) ((float) (cx + 1) + 0.5f), cy + 7, (int) ((float) cz + 0.5f), glowstone); // orig :1990
        piece.place((int) ((float) (cx - 1) + 0.5f), cy + 7, (int) ((float) cz + 0.5f), glowstone); // orig :1991
        piece.place((int) ((float) cx + 0.5f), cy + 7, (int) ((float) (cz + 1) + 0.5f), glowstone); // orig :1992
        piece.place((int) ((float) cx + 0.5f), cy + 7, (int) ((float) (cz - 1) + 0.5f), glowstone); // orig :1993
        // Loop C — rim cylinder at fixed radius 10, y+1..+6, 72 angles
        // (orig :1994-2012). Courses: glowstone / lapis-or-egg / lapis-or-
        // egg / glowstone / bedrock / bedrock. TWO unconditional nextInt(2)
        // draws per angle = 144 total (stitching contract; spec §10) —
        // ~50% of the y+2..+3 band is harvestable spawn-egg block (spec S4).
        {
            float currad = 10.0f;                                         // orig :1994
            for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));  // orig :1996
                float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));  // orig :1997
                int wx = (int) (cx + curx + 0.5f);
                int wz = (int) (cz + curz + 0.5f);
                piece.place(wx, cy + 1, wz, glowstone);                   // orig :1998
                BlockState blk = lapisBlock;                              // orig :1999
                if (random.nextInt(2) == 0) {                             // orig :2000-2002
                    blk = spawnEgg;
                }
                piece.place(wx, cy + 2, wz, blk);                         // orig :2003
                blk = lapisBlock;                                         // orig :2004
                if (random.nextInt(2) == 0) {                             // orig :2005-2007
                    blk = spawnEgg;
                }
                piece.place(wx, cy + 3, wz, blk);                         // orig :2008
                piece.place(wx, cy + 4, wz, glowstone);                   // orig :2009
                piece.place(wx, cy + 5, wz, bedrock);                     // orig :2010
                piece.place(wx, cy + 6, wz, bedrock);                     // orig :2011
            }
        }
        // Loop D — 7x7 floating sand raft, plain ints (orig :2013-2018):
        // sand replaces the water-surface layer, stone the layer below;
        // ocean water remains beneath (spec S6).
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                piece.place(cx + i, cy, cz + j, sand);                    // orig :2015
                piece.place(cx + i, cy - 1, cz + j, stone);               // orig :2016
            }
        }
        // Loop E — 5x5 leaf canopy slab at y+3 (orig :2019-2023).
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                piece.place(cx + i, cy + 3, cz + j, leaves);              // orig :2021
            }
        }
        // Tree — apex leaf, trunk written top-down (y+3 overwrites the
        // canopy-center leaf), four diagonal branches overwriting canopy
        // leaves (orig :2024-2031; spec S7).
        piece.place(cx, cy + 4, cz, leaves);                              // orig :2024
        piece.place(cx, cy + 3, cz, log);                                 // orig :2025
        piece.place(cx, cy + 2, cz, log);                                 // orig :2026
        piece.place(cx, cy + 1, cz, log);                                 // orig :2027
        piece.place(cx + 1, cy + 3, cz + 1, log);                         // orig :2028
        piece.place(cx - 1, cy + 3, cz - 1, log);                         // orig :2029
        piece.place(cx + 1, cy + 3, cz - 1, log);                         // orig :2030
        piece.place(cx - 1, cy + 3, cz + 1, log);                         // orig :2031
        // Four "Water Dragon" spawners at the trunk-top's cardinal
        // neighbours, overwriting canopy leaves (orig :2032-2051).
        piece.placeSpawner(cx + 1, cy + 3, cz, ModEntities.WATER_DRAGON.get());  // orig :2032-2036
        piece.placeSpawner(cx - 1, cy + 3, cz, ModEntities.WATER_DRAGON.get());  // orig :2037-2041
        piece.placeSpawner(cx, cy + 3, cz + 1, ModEntities.WATER_DRAGON.get());  // orig :2042-2046
        piece.placeSpawner(cx, cy + 3, cz - 1, ModEntities.WATER_DRAGON.get());  // orig :2047-2051
        // Loot chest on the raft beside the trunk, meta 0 (no facing) →
        // default-facing overload (orig :2052-2056); fill count
        // 4 + nextInt(5) lives in the loot table's rolls (uniform 4-8).
        piece.placeLootChest(cx, cy + 1, cz - 1, LAIR_LOOT);
    }
}
