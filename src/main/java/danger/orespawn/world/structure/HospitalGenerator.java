package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Ender Dragon Hospital (The End), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeEnderDragonHospital}
 * (orig GenericDungeon.java:2815-2991) &mdash; one of the WGEN-042
 * structure pool. A 10&times;10 iron-bar cage (end-stone floor and top
 * rim, obsidian corner posts) under a 3-ring eye-of-ender ziggurat dome
 * with a 2&times;2 opening at the top; obsidian corner towers rise to
 * four bedrock-capped perches at y+9, each holding a vanilla End
 * Crystal; a 4-wide end-stone stair ramp with iron-bar/glowstone
 * railings climbs from x&minus;6 to the west rim. Four "Ender Reaper"
 * spawners sit on the dome-ring corners and four "Nightmare"
 * (= {@code PitchBlack}) spawners inside the cage corners, with one
 * loot chest at the cage centre.
 *
 * <p><b>No Ender Dragon is spawned</b> despite the method name &mdash;
 * the only entity in the original method is {@code EntityEnderCrystal}
 * (&times;4, orig :2906-2921). The "hospital" heals visiting dragons
 * through the vanilla crystal-healing AI; do not invent a dragon spawn
 * (spec {@code d6_extraction/hospital_monster_island_spec.md} &sect;A2).</p>
 *
 * <p><b>Mapping decisions</b> (full tables in the spec):</p>
 * <ul>
 *   <li><b>RNG:</b> the original drew the four crystal yaws and the
 *       chest fill count from {@code world.rand} (orig :2907 etc.,
 *       :2989); the port threads the deterministic per-piece
 *       {@link RandomSource} through the same draw sequence
 *       (yaw&#8321;..yaw&#8324; after the geometry, before the
 *       spawners/chest). The yaws are drawn OUTSIDE the gated spawn
 *       helper so every chunk pass consumes an identical stream
 *       (stitching contract, structure_conversion_pattern.md &sect;1
 *       step 3). The fill count moved into the loot JSON.</li>
 *   <li><b>Crystal perch quirk kept:</b> each crystal is spawned at
 *       {@code y+9} and THEN a bedrock block is written into that same
 *       cell (orig :2906-2909 &mdash; entity first, block second), so
 *       the crystal sits embedded in its cap rather than one block
 *       above it like vanilla pillars. Coordinates and order are
 *       replicated faithfully (spec surprise S3).</li>
 *   <li><b>Spawner overwrite kept:</b> the four Ender Reaper spawners
 *       land on the j=9 dome-ring corners, overwriting four
 *       eye-of-ender blocks placed moments earlier (orig :2877-2878 vs
 *       :2925/:2933/:2941/:2949; spec surprise S4). Spawners are
 *       placed after the rings, same net result.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/hospital} transcribes
 *       the 6-entry {@code HospitalContentsList} (orig GenericDungeon
 *       .java:44, total weight 210) with the original 6-10 stack rolls
 *       ({@code 6 + nextInt(5)}, orig :2989); the random-slot collision
 *       quirk is dropped &mdash; the same documented approximation as
 *       every chest-list conversion since Phase C.</li>
 * </ul>
 */
final class HospitalGenerator {

    /** Loot for the cage-centre chest (orig GenericDungeon.java:44 + :2989). */
    private static final ResourceKey<LootTable> HOSPITAL_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/hospital"));

    private HospitalGenerator() {}

    /**
     * Direct port of {@code makeEnderDragonHospital(world, cposx, cposy,
     * cposz)} (orig GenericDungeon.java:2815-2991). {@code origin} is the
     * End-surface anchor (worldgen, orig OreSpawnWorld.java:1542-1555 &mdash;
     * air-on-end-stone scan hit, no offset) or the Dungeon Spawner Block
     * position (outcome 24, orig DungeonSpawnerBlock.java:125-127).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState endStone = Blocks.END_STONE.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState eyeOfEnder = ModBlocks.BLOCK_EYE_OF_ENDER.get().defaultBlockState();

        // orig :2824-2852 — 10x10x7 cage. The if-chain override ORDER is
        // load-bearing (later conditions win): air → perimeter iron bars →
        // corner obsidian → j==0 end-stone floor → j==6 perimeter end-stone
        // rim (the j==6 rule beats the corner obsidian, so the rim is
        // end stone all the way around).
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 10; k++) {
                for (int j = 0; j < 7; j++) {
                    BlockState blk = air;                                   // orig :2827
                    if (i == 0 || k == 0 || i == 9 || k == 9) {             // orig :2828-2830
                        blk = ironBars;
                    }
                    if ((i == 0 || i == 9) && (k == 0 || k == 9)) {         // orig :2831-2842 (4 corner ifs)
                        blk = obsidian;
                    }
                    if (j == 0) {                                           // orig :2843-2845
                        blk = endStone;
                    }
                    if (j == 6 && (i == 0 || k == 0 || i == 9 || k == 9)) { // orig :2846-2848
                        blk = endStone;
                    }
                    piece.place(x + i, y + j, z + k, blk);                  // orig :2849
                }
            }
        }
        // orig :2853-2862 — dome ring 1 at j=7: 8x8, edge {1,8} eye-of-ender,
        // interior air.
        for (int i = 1; i < 9; i++) {
            for (int k = 1; k < 9; k++) {
                BlockState blk = (i == 1 || i == 8 || k == 1 || k == 8) ? eyeOfEnder : air;
                piece.place(x + i, y + 7, z + k, blk);
            }
        }
        // orig :2863-2872 — dome ring 2 at j=8: 6x6, edge {2,7}.
        for (int i = 2; i < 8; i++) {
            for (int k = 2; k < 8; k++) {
                BlockState blk = (i == 2 || i == 7 || k == 2 || k == 7) ? eyeOfEnder : air;
                piece.place(x + i, y + 8, z + k, blk);
            }
        }
        // orig :2873-2882 — dome ring 3 at j=9: 4x4, edge {3,6}; the interior
        // 2x2 air at i,k = 4..5 is the ONLY way into the cage (spec surprise
        // S5 — the ramp reaches the rim but no doorway is carved).
        for (int i = 3; i < 7; i++) {
            for (int k = 3; k < 7; k++) {
                BlockState blk = (i == 3 || i == 6 || k == 3 || k == 6) ? eyeOfEnder : air;
                piece.place(x + i, y + 9, z + k, blk);
            }
        }
        // orig :2883-2897 — entrance ramp: 6 steps from (-6,1) up to (-1,6),
        // each a 4-wide end-stone tread at k=3..6 with iron-bar railings one
        // above and glowstone caps two above on the outer columns (k=3, k=6).
        {
            int i = -6;                                                     // orig :2883
            int j = 1;                                                      // orig :2884
            int k = 3;                                                      // orig :2885
            for (int m = 0; m < 6; m++) {                                   // orig :2886
                piece.place(x + i, y + j, z + k, endStone);                 // orig :2887
                piece.place(x + i, y + j, z + k + 1, endStone);             // orig :2888
                piece.place(x + i, y + j, z + k + 2, endStone);             // orig :2889
                piece.place(x + i, y + j, z + k + 3, endStone);             // orig :2890
                piece.place(x + i, y + j + 1, z + k, ironBars);             // orig :2891
                piece.place(x + i, y + j + 1, z + k + 3, ironBars);         // orig :2892
                piece.place(x + i, y + j + 2, z + k, glowstone);            // orig :2893
                piece.place(x + i, y + j + 2, z + k + 3, glowstone);        // orig :2894
                i++;                                                        // orig :2895
                j++;                                                        // orig :2896
            }
        }
        // orig :2898-2905 — four obsidian corner towers, 2 high (j=7, j=8),
        // continuing the cage corner posts up to the crystal perches.
        for (int j = 7; j <= 8; j++) {
            piece.place(x, y + j, z, obsidian);
            piece.place(x, y + j, z + 9, obsidian);
            piece.place(x + 9, y + j, z, obsidian);
            piece.place(x + 9, y + j, z + 9, obsidian);
        }
        // orig :2906-2921 — the four End Crystals. Per crystal: yaw drawn
        // (world.rand nextFloat*360, pitch 0), entity spawned at the cell
        // centre (+0.5) at y+9, THEN bedrock written into that same cell —
        // entity-then-block order kept (spec surprise S3). Yaws are drawn
        // unconditionally by this caller; only the spawn/write is chunk-gated
        // (stitching contract).
        float yaw = random.nextFloat() * 360.0f;                            // orig :2907
        piece.spawnEntity(EntityType.END_CRYSTAL, x + 0.5, y + 9, z + 0.5, yaw);
        piece.place(x, y + 9, z, bedrock);                                  // orig :2909
        yaw = random.nextFloat() * 360.0f;                                  // orig :2911
        piece.spawnEntity(EntityType.END_CRYSTAL, x + 0.5, y + 9, z + 9.5, yaw);
        piece.place(x, y + 9, z + 9, bedrock);                              // orig :2913
        yaw = random.nextFloat() * 360.0f;                                  // orig :2915
        piece.spawnEntity(EntityType.END_CRYSTAL, x + 9.5, y + 9, z + 0.5, yaw);
        piece.place(x + 9, y + 9, z, bedrock);                              // orig :2917
        yaw = random.nextFloat() * 360.0f;                                  // orig :2919
        piece.spawnEntity(EntityType.END_CRYSTAL, x + 9.5, y + 9, z + 9.5, yaw);
        piece.place(x + 9, y + 9, z + 9, bedrock);                          // orig :2921
        // orig :2922-2953 — four "Ender Reaper" spawners on the j=9 dome-ring
        // corners (overwriting the ring's corner eye-of-ender blocks, spec
        // surprise S4).
        piece.placeSpawner(x + 3, y + 9, z + 3, ModEntities.ENDER_REAPER.get()); // orig :2922-2929
        piece.placeSpawner(x + 3, y + 9, z + 6, ModEntities.ENDER_REAPER.get()); // orig :2930-2937
        piece.placeSpawner(x + 6, y + 9, z + 3, ModEntities.ENDER_REAPER.get()); // orig :2938-2945
        piece.placeSpawner(x + 6, y + 9, z + 6, ModEntities.ENDER_REAPER.get()); // orig :2946-2953
        // orig :2954-2985 — four "Nightmare" (= PitchBlack, orig
        // OreSpawnMain.java:4023-4027) spawners at cage floor level, just
        // inside each corner post.
        piece.placeSpawner(x + 1, y + 1, z + 1, ModEntities.PITCH_BLACK.get()); // orig :2954-2961
        piece.placeSpawner(x + 1, y + 1, z + 8, ModEntities.PITCH_BLACK.get()); // orig :2962-2969
        piece.placeSpawner(x + 8, y + 1, z + 1, ModEntities.PITCH_BLACK.get()); // orig :2970-2977
        piece.placeSpawner(x + 8, y + 1, z + 8, ModEntities.PITCH_BLACK.get()); // orig :2978-2985
        // orig :2986-2990 — one chest at the cage centre, filled with 6-10
        // weighted HospitalContentsList stacks (the counts live in
        // chests/hospital.json). Placed with meta 0 (no facing) → default
        // facing overload.
        piece.placeLootChest(x + 4, y + 1, z + 4, HOSPITAL_LOOT);
    }
}
