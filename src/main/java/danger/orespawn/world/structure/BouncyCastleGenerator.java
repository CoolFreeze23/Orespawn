package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Bouncy Castle (vanilla-overworld Desert), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeBouncyCastle}
 * (orig GenericDungeon.java:3106-3205) &mdash; a 9&times;9&times;5 hollow
 * box whose floor, ceiling, and perimeter walls are ALL Lavafoam, with four
 * full-height red-terracotta corner pillars, a 1-wide 2-tall doorway centred
 * in the &minus;z (north) wall, nine spawners floating one block below the
 * ceiling (the same Silverfish/Rat/Scorpion trio on the south, east, and
 * west interior walls), and one north-facing loot chest floating in the
 * interior corner diagonal from the door.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/bouncy_castle_spec.md}):</p>
 * <ul>
 * <li>The whole shell is {@code OreSpawnMain.MyLavafoamBlock} (registered
 *     "lavafoam", orig OreSpawnMain.java:1610) &rarr;
 *     {@code ModBlocks.LAVAFOAM}, the mod's bouncy/damaging block (friction
 *     1.1, ITEM-009) &mdash; the building is literally a bouncy castle; do
 *     not substitute wool/clay &mdash; spec S3. The corner pillars are
 *     stained clay meta 14 = red terracotta, full height including floor
 *     and ceiling corners (their rule runs after the wall rules and
 *     overwrites them, orig :3143-3146) &mdash; spec S4.</li>
 * <li>The box write is UNCONDITIONAL over 9&times;9&times;5 (orig :3151
 *     runs for every cell): terrain inside becomes air and the top sand
 *     layer under the box becomes the Lavafoam floor, but nothing outside
 *     the box is touched &mdash; no foundation skirt, no yard clearing; on
 *     a dune slope the castle half-buries, over a hollow it overhangs on
 *     nothing. Do not add either &mdash; spec S7.</li>
 * <li>All 9 spawners AND the chest float at y+3, one block below the
 *     ceiling &mdash; flag-2 writes need no support and the piece helpers
 *     preserve that. The chest faces north (meta 2, orig :3200), toward
 *     the door wall &mdash; the facing {@code placeLootChest} overload
 *     &mdash; spec S5. The "Silverfish" spawner is the VANILLA mob &mdash;
 *     mapped to {@link EntityType#SILVERFISH}, not swapped for an OreSpawn
 *     mob &mdash; spec S6/&sect;4.</li>
 * <li>The original's only RNG draw, the chest fill count
 *     {@code 6 + nextInt(5)} (orig :3203), moved into the loot JSON's
 *     rolls (uniform 6-10), so this generator consumes ZERO random draws
 *     and satisfies the per-chunk-replay stitching contract vacuously
 *     &mdash; spec &sect;10. Its only world reads were tile-entity fetches
 *     at just-written positions (orig :3156/:3161/:3166/:3171/:3176/:3181/
 *     :3186/:3191/:3196/:3201), absorbed by the {@code placeSpawner}/
 *     {@code placeLootChest} helpers &mdash; spec &sect;9.</li>
 * <li>Dead locals dropped: {@code deltax/deltaz/dirx/dirz/stuffdir} and
 *     the unused copies {@code x/y/z} (orig :3107-3125); the client guard
 *     (orig :3126-3128) has no port equivalent &mdash; both port paths are
 *     server-only by construction &mdash; spec S8.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/bouncy_castle} transcribes the
 * original 7-entry {@code BouncyContentsList} (orig GenericDungeon.java:41,
 * total weight 180) with the original 6-10 fill rolls; the random-slot
 * collision quirk is dropped, the same documented approximation as every
 * other chest-list conversion since Phase C.</p>
 */
final class BouncyCastleGenerator {

    /** Loot for the floating corner chest (orig GenericDungeon.java:41 + :3203). */
    private static final ResourceKey<LootTable> CASTLE_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/bouncy_castle"));

    private BouncyCastleGenerator() {}

    /**
     * Direct port of {@code makeBouncyCastle(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:3106-3205). {@code origin} is the top SAND
     * block itself (worldgen, orig OreSpawnWorld.java:1292 passes
     * {@code posY - 1} &mdash; the &minus;1 anchor makes the floor REPLACE
     * the surface sand layer and sit flush with the desert floor, doorway
     * walk-in level at y+1..+2, spec S2) or the Dungeon Spawner Block
     * position (outcome 26, orig DungeonSpawnerBlock.java:131-133, NO
     * offset &mdash; the floor builds AT the clicked position; floating or
     * terrain-embedded is faithful, spec &sect;11).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        final int length = 4;                                             // orig :3118
        final int width = 4;                                              // orig :3119
        final int height = 5;                                             // orig :3120
        BlockState air = Blocks.AIR.defaultBlockState();                            // field_150350_a
        BlockState lavafoam = ModBlocks.LAVAFOAM.get().defaultBlockState();         // OreSpawnMain.MyLavafoamBlock
        BlockState redTerracotta = Blocks.RED_TERRACOTTA.defaultBlockState();       // stained clay meta 14

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // ONE triple loop builds the entire shell (orig :3129-3154):
        // i = X -width..+width, j = Z -length..+length, k = Y 0..height-1.
        // Per cell the rules run in source order, later assignments
        // overwriting earlier ones; the write is unconditional over the
        // whole 9x9x5 box (spec S7 — terrain inside cleared, nothing
        // outside touched).
        for (int i = -width; i <= width; i++) {                           // orig :3129
            for (int j = -length; j <= length; j++) {                     // orig :3130
                for (int k = 0; k < height; k++) {                        // orig :3131
                    BlockState state = air;                               // orig :3132-3133
                    if (k == height - 1 || k == 0) {
                        state = lavafoam;                                 // orig :3134-3136 — ceiling + floor slabs
                    }
                    if (i == -width || i == width) {
                        state = lavafoam;                                 // orig :3137-3139 — east/west walls
                    }
                    if (j == -length || j == length) {
                        state = lavafoam;                                 // orig :3140-3142 — north/south walls
                    }
                    // Corner predicate !(i != -width && i != width
                    // || j != -length && j != length) <=> |i| == width
                    // && |j| == length — the 4 corner columns, full height.
                    if ((i == -width || i == width) && (j == -length || j == length)) {
                        state = redTerracotta;                            // orig :3143-3146
                    }
                    if ((k == 1 || k == 2) && i == 0 && j == -length) {
                        state = air;                                      // orig :3147-3150 — doorway (0, +1..+2, -4)
                    }
                    piece.place(cx + i, cy + k, cz + j, state);           // orig :3151
                }
            }
        }

        // Nine spawners, all floating at y+3 one below the ceiling, one
        // block inside a wall (width-1 = length-1 = 3): the same
        // Silverfish/Rat/Scorpion trio on the south (z = +3), east
        // (x = +3), and west (x = -3) interior walls — the door (north)
        // wall has none (orig :3155-3199, spec §2.1/S6).
        piece.placeSpawner(cx - 1, cy + 3, cz + length - 1, EntityType.SILVERFISH);             // orig :3155-3159
        piece.placeSpawner(cx, cy + 3, cz + length - 1, ModEntities.ENTITY_RAT.get());          // orig :3160-3164
        piece.placeSpawner(cx + 1, cy + 3, cz + length - 1, ModEntities.ENTITY_SCORPION.get()); // orig :3165-3169
        piece.placeSpawner(cx + width - 1, cy + 3, cz - 1, EntityType.SILVERFISH);              // orig :3170-3174
        piece.placeSpawner(cx + width - 1, cy + 3, cz, ModEntities.ENTITY_RAT.get());           // orig :3175-3179
        piece.placeSpawner(cx + width - 1, cy + 3, cz + 1, ModEntities.ENTITY_SCORPION.get());  // orig :3180-3184
        piece.placeSpawner(cx - width + 1, cy + 3, cz - 1, EntityType.SILVERFISH);              // orig :3185-3189
        piece.placeSpawner(cx - width + 1, cy + 3, cz, ModEntities.ENTITY_RAT.get());           // orig :3190-3194
        piece.placeSpawner(cx - width + 1, cy + 3, cz + 1, ModEntities.ENTITY_SCORPION.get());  // orig :3195-3199

        // ONE chest at (+3, +3, +3) — the interior corner cell diagonal
        // from the door, floating like the spawners, meta 2 = facing north
        // toward the door wall (orig :3200-3204). Not adjacent to any
        // spawner (gaps at (+2,+3,+3) and (+3,+3,+2)), so it stays a
        // single chest. Fill count 6 + nextInt(5) (orig :3203) lives in
        // the loot table's rolls (uniform 6-10).
        piece.placeLootChest(cx + width - 1, cy + 3, cz + length - 1, Direction.NORTH, CASTLE_LOOT);
    }
}
