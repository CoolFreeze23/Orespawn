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
 * The Haunted House (vanilla OVERWORLD &mdash; distinct from the
 * Crystal-dimension variant {@code CrystalStructures.buildCrystalHauntedHouse},
 * which shares the shape but nothing else), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeHauntedHouse} (orig GenericDungeon.java:891-1010)
 * &mdash; the InstantShelter starter cottage with its orientation HARDCODED
 * ({@code deltax = 1}, {@code stuffdir = 2}, orig :905-906: east door,
 * north-facing furniture) and overrun by ghosts: a 7&times;7&times;5 box with
 * a full cobblestone floor AT origin level, oak-plank walls at y+1..+2, a full
 * perimeter GLASS-BLOCK clerestory band at y+3 (corners included &mdash; not
 * punched windows, not panes; spec S8), a flat plank roof at y+4, one
 * 1&times;2 doorway centered in the +X (east) wall, a furnace / crafting
 * table / kit-chest row along the interior south side, and a three-high
 * Rat / Ghost / Ghost Pumpkin Skelly spawner STACK in the center column
 * (one vertical column, spec S4 &mdash; do not redistribute).
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/haunted_house_spec.md}):</p>
 * <ul>
 * <li><b>Flag-3 writes ported through flag-2</b>: the original shell +
 *     furniture use {@code func_147449_b}/{@code func_72921_c} (default
 *     flag 3, WITH neighbor updates), not the siblings'
 *     {@code setBlockFast} flag-2 (spec header trap / S5). The palette
 *     contains no attachable or gravity-affected block, so the delta has
 *     zero behavioral consequence; everything goes through the standard
 *     {@code piece.place}.</li>
 * <li><b>Furniture/chest facing is content</b> (meta 2 = north, orig
 *     :944/:949, spec S6): chest via the {@code placeLootChest} facing
 *     overload (WGEN-056), furnace FACING set explicitly in the BlockState
 *     (it coincides with the default state, but the crystal-variant port's
 *     defaultBlockState simplification dropped the facing &mdash;
 *     deliberately not copied here). Furnace unlit ({@code LIT=false}
 *     default).</li>
 * <li>The original's only RNG, 14 independent {@code nextInt(2)} chest-slot
 *     gates (orig :952-991), moved into the loot JSON as 14 pools with
 *     {@code random_chance 0.5} each, so this generator consumes ZERO random
 *     draws and satisfies the per-chunk-replay stitching contract vacuously
 *     (spec &sect;11). Terrain reads: NONE (spec &sect;10) &mdash; the only
 *     world reads were tile-entity fetches at just-written positions (chest
 *     orig :950, spawners :996/:1001/:1006), absorbed by the
 *     {@code placeSpawner}/{@code placeLootChest} helpers.</li>
 * <li><b>Unconditional 7&times;7&times;5 box write</b> (orig :913-939, spec
 *     S7): terrain inside becomes air, the surface air layer at y+0 becomes
 *     the cobble floor; nothing outside the box is touched &mdash; no yard,
 *     no foundation skirt. On a slope the house half-buries; over a hollow
 *     it overhangs. Faithful &mdash; do not add either.</li>
 * <li><b>Worldgen anchor is the AIR block above grass</b> (addHauntedHouse
 *     orig OreSpawnWorld.java:979-997 &mdash; the Y 100&rarr;41 air-descent
 *     scan hands the builder {@code posY} with no offset), which is exactly
 *     what {@code PlacementMode.SWAMP_GRASS_SURFACE} returns; the doorway
 *     bottom (y+1) therefore sits two above the outside grass &mdash; a
 *     1-block step up onto the floor (spec S9, faithful). The original's
 *     1/285 chunk gate maps to the structure set (spacing 17 / separation 8,
 *     salt 84372, the leaf-monster 1/275 pair; spec &sect;8), the
 *     exact-name "Plains"/"Taiga"/"Swampland" biome gate to the
 *     {@code has_structure/haunted_house} tag (plains/taiga/swamp, spec
 *     &sect;7). Documented absorbed deltas: 5 jitter attempts vs the mode's
 *     4, and the air-descent scan vs the siblings' full-window scan (spec
 *     S10).</li>
 * <li>The Dungeon Spawner Block path (type 5, orig
 *     DungeonSpawnerBlock.java:68-70) builds at the clicked position in any
 *     dimension, bypassing biome and scan &mdash; floating or embedded there
 *     is faithful behavior (spec &sect;9).</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/haunted_house} &mdash; the
 * InstantShelter kit INCLUDING its slot-12 Ore Salt block (which the igloo
 * omits), at 50% per slot instead of InstantShelter's guaranteed fill, with
 * COOKED porkchop where the shelter kit has raw (the one item where the two
 * lists differ, spec S3) and WITHOUT the igloo's three appended gold-nugget
 * slots; NOT a weighted list. Only the fixed slot positions are lost to the
 * loot system's sequential fill &mdash; documented approximation, no
 * collisions existed to begin with (spec &sect;3).</p>
 */
final class HauntedHouseGenerator {

    /** Loot for the kit chest at (0, +1, +2) (orig GenericDungeon.java:947-993). */
    private static final ResourceKey<LootTable> HAUNTED_HOUSE_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/haunted_house"));

    private HauntedHouseGenerator() {}

    /**
     * Direct port of {@code makeHauntedHouse(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:891-1010). {@code origin} is the AIR block
     * above grass found by the {@code addHauntedHouse} Y 100&rarr;41
     * air-descent scan (worldgen, orig OreSpawnWorld.java:979-997 &mdash; no
     * offset) or the Dungeon Spawner Block position (outcome 5, orig
     * DungeonSpawnerBlock.java:68-70).
     *
     * <p>Method-local constants folded in: {@code length = 3} (orig :902),
     * {@code width = 3} (:903), {@code height = 3} (:904), {@code deltax = 1}
     * (:905), {@code deltaz = 0} (:896), {@code stuffdir = 2} (:906). Dead
     * locals {@code bid/dirx/dirz} (:897-899) and the plain {@code x/z/y}
     * copies of {@code cposx/cposz/cposy} (:907-909) dropped; the client
     * guard (:910-912) is a server-only no-op with nothing to port.</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        BlockState oakPlanks = Blocks.OAK_PLANKS.defaultBlockState();   // field_150344_f (meta 0)
        BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState(); // field_150347_e
        BlockState glass = Blocks.GLASS.defaultBlockState();            // field_150359_w — the full BLOCK, not pane (spec S8)
        BlockState air = Blocks.AIR.defaultBlockState();

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Shell loop — the 7×7×5 house (orig :913-939). Per cell the rules
        // run in source order, first match wins (if/continue chain, no
        // overwrites). The write is unconditional over the whole box:
        // terrain inside becomes air, the surface air layer at y+0 becomes
        // the cobble floor (spec S7).
        for (int i = -3; i <= 3; i++) {                                  // orig :913 (-width..width, width=3 :903)
            for (int j = -3; j <= 3; j++) {                              // orig :914 (-length..length, length=3 :902)
                for (int k = 0; k <= 4; k++) {                           // orig :915 (0..height+1, height=3 :904)
                    if (k == 4) {                                        // orig :916 (k == height+1)
                        piece.place(cx + i, cy + k, cz + j, oakPlanks);  // orig :917 — full 7×7 flat roof, walls included
                        continue;                                        // orig :918
                    }
                    if (k == 0) {                                        // orig :920
                        piece.place(cx + i, cy + k, cz + j, cobblestone); // orig :921 — full 7×7 floor, walls included
                        continue;                                        // orig :922
                    }
                    if (i == 3 || j == 3 || i == -3 || j == -3) {        // orig :924 — perimeter
                        if (k == 3) {                                    // orig :925 (k == height)
                            piece.place(cx + i, cy + k, cz + j, glass);  // orig :926 — full clerestory band, corners included (spec S8)
                            continue;                                    // orig :927
                        }
                        if ((k == 1 || k == 2) && i == 3 && j == 0) {    // orig :929 (i == deltax*width, j == deltaz*length)
                            piece.place(cx + i, cy + k, cz + j, air);    // orig :930 — 1×2 doorway centered in the +X (east) wall
                            continue;                                    // orig :931
                        }
                        piece.place(cx + i, cy + k, cz + j, oakPlanks);  // orig :933 — the wall proper
                        continue;                                        // orig :934
                    }
                    piece.place(cx + i, cy + k, cz + j, air);            // orig :936 — interior clearing
                }
            }
        }

        // Furniture row along the interior south side, on the floor (orig
        // :940-949): setup i=2, k=1, j=length−1=2 (:940-942), positions
        // (x + i*deltax + j*deltaz, y + k, z + i*deltaz + j*deltax) — with
        // deltax=1, deltaz=0 this collapses to (cx+i, cy+1, cz+2). Both
        // facings meta 2 = north (stuffdir, :906) — fronts toward the room
        // interior (spec §2.2/S6).
        piece.place(cx + 2, cy + 1, cz + 2, Blocks.FURNACE.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)); // orig :943-944 — unlit furnace, meta 2 = north
        piece.place(cx + 1, cy + 1, cz + 2, Blocks.CRAFTING_TABLE.defaultBlockState()); // orig :945-946
        // Kit chest, meta 2 = facing north (orig :947-949; WGEN-056). The 14
        // independent 50% slot rolls (orig :950-993) live in the loot JSON.
        piece.placeLootChest(cx, cy + 1, cz + 2, Direction.NORTH, HAUNTED_HOUSE_LOOT);

        // Center spawner STACK, one per Y level in the column (cposx, cposz)
        // — floor to glass band, all in loop-interior air (orig :995-1009;
        // spec S4 — one vertical column, do not redistribute).
        piece.placeSpawner(cx, cy + 1, cz, ModEntities.ENTITY_RAT.get());    // "Rat", orig :995-999
        piece.placeSpawner(cx, cy + 2, cz, ModEntities.GHOST.get());         // "Ghost", orig :1000-1004
        piece.placeSpawner(cx, cy + 3, cz, ModEntities.GHOST_SKELLY.get());  // "Ghost Pumpkin Skelly", orig :1005-1009
    }
}
