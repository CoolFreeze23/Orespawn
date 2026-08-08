package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
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

import java.util.HashSet;
import java.util.Set;

/**
 * The Inca Pyramid (Islands dimension), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makeIncaPyramid} (orig GenericDungeon.java:3735-3952)
 * plus its helpers {@code makepoolalter} (:3954-3961), {@code makeincagraves}
 * (:3963-3977) and {@code makeincagrave} (:3979-4042) &mdash; one of the
 * WGEN-042 structure pool (spec:
 * {@code phase_d_reports/d6_extraction/inca_pyramid_spec.md}).
 *
 * <p>Layout: a 41&times;31 stepped hollow pyramid base (10 one-block rings,
 * solid stone-brick floor plate at grass level, torch rows on layers 2/5/8),
 * four 5-lane staircase ramps climbing the N/S/E/W faces to the top, a
 * 21&times;11&times;9 inner temple (doorways on all four sides, lit
 * redstone-lamp checkerboard, five water altars, a CreeperRepellent-guarded
 * centre altar, a Molenoid spawner) with a floating checkered slab rim, a
 * trapdoor + ladder shaft descending from the temple floor into the hollow
 * base cavity, and a hidden graveyard of 24 flower-decked graves down there
 * &mdash; each with a loot chest and a 1/3-chance "Ghost" spawner.</p>
 *
 * <p><b>Mapping decisions</b> (full tables in the spec):</p>
 * <ul>
 *   <li><b>⚠ PARITY DEVIATION &mdash; ramp mid-build world reads (spec
 *       &sect;10).</b> The original gated every ramp rail/tread/support-pillar
 *       write on {@code world.getBlock(...) == air} (orig :3791/:3798/:3801
 *       and the three mirrored loops). Two behaviours hid behind those reads:
 *       <ol>
 *         <li><i>Self-reads</i> &mdash; ramps dock into the pyramid step
 *             rings the same call built moments earlier and must not
 *             overwrite them. Worldgen passes cannot read neighbouring
 *             chunks' pending writes, so the base build is mirrored into an
 *             in-memory write-set model ({@link #placeModeled}) and the ramp
 *             probes query the model &mdash; the probe sees exactly the state
 *             the original's read-after-write saw (the BasiliskMaze wall
 *             bitmap pattern).</li>
 *         <li><i>Pre-build terrain reads</i> &mdash; the original's support
 *             pillars stopped at the first pre-existing terrain block and its
 *             rails/treads skipped terrain-occupied cells. Pre-build state is
 *             unknowable under chunk stitching, so the spec's sanctioned
 *             deviation applies: cells the model has no record of are treated
 *             as air &mdash; support pillars always fill down to relative
 *             y&nbsp;0 and rails/treads place unconditionally (unless docking
 *             into the structure's own base per the model). This does NOT
 *             coincide with the original even on the flat Islands grass
 *             plane: the original's pillar loop stopped AT the surface grass
 *             (orig GenericDungeon.java:3801 reads the pre-build world and
 *             the worldgen site guarantees grass at relative y&nbsp;0,
 *             OreSpawnWorld.java:2351-2353), whereas this port writes stone
 *             into that grass cell under every outside-footprint ramp-lane
 *             column (~200 cells across the four ramps). Larger deltas are
 *             possible on the live Dungeon Spawner Block path over rough
 *             terrain. PARITY note PN-018.</li>
 *       </ol></li>
 *   <li><b>RNG:</b> the original drew everything from {@code world.rand};
 *       this port threads the deterministic per-piece {@link RandomSource}
 *       through the identical draw sequence (base wall rolls &rarr; temple
 *       wall rolls &rarr; per-grave Ghost gates). Draws are UNCONDITIONAL
 *       and only the writes are chunk-gated (stitching contract); the chest
 *       fill counts move into the loot JSON, shortening the stream
 *       identically in every pass. Layouts become seed-stable; the Dungeon
 *       Spawner Block path (outcome 29, orig DungeonSpawnerBlock.java:140-142)
 *       keeps live-RNG behaviour via {@code buildNow}.</li>
 *   <li><b>Chest loot:</b> {@code orespawn:chests/inca_pyramid} transcribes
 *       the 14-entry {@code IncaPyramidContentsList} (orig
 *       GenericDungeon.java:38, total weight 480) with the original
 *       {@code 10 + nextInt(5)} = 10-14 stack rolls (orig :4010/:4039 &mdash;
 *       all 24 grave chests use this one formula). The original's random-slot
 *       placement could overwrite earlier stacks; the data-driven table
 *       cannot &mdash; the same documented approximation as every chest-list
 *       conversion since Phase C.</li>
 *   <li><b>Metadata blocks:</b> torch meta 3/4 &rarr; wall torch facing
 *       SOUTH/NORTH; ramp-end torches were meta 0 (an unattached default,
 *       spec &sect;11.5) &rarr; standing torch; 1.7.10 stone slab meta 0
 *       &rarr; {@code smooth_stone_slab}; trapdoor meta 3 &rarr; oak trapdoor
 *       facing EAST (1.8 {@code BlockTrapDoor.getFacing} table); ladder
 *       meta 2 / chest meta 2 &rarr; facing NORTH; redstone lamp
 *       {@code field_150374_bv} was the LIT block &rarr; {@code lit=true}
 *       (survives &mdash; flag-2 writes send no neighbour updates, exactly
 *       like the original's setBlockFast).</li>
 * </ul>
 */
final class IncaPyramidGenerator {

    /** Loot for the 24 graveyard chests (orig GenericDungeon.java:38 + :4010/:4039). */
    private static final ResourceKey<LootTable> INCA_PYRAMID_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/inca_pyramid"));

    /** Pyramid base X size (orig :3749). */
    private static final int BASE_WIDTH = 41;
    /** Pyramid base Z size (orig :3750). */
    private static final int BASE_DEPTH = 31;
    /** Pyramid step count = inner temple offset on all three axes (orig :3751, :3879-3881). */
    private static final int BASE_HEIGHT = 10;
    /** Inner temple X size (orig :3746). */
    private static final int TEMPLE_WIDTH = 21;
    /** Inner temple Z size (orig :3747). */
    private static final int TEMPLE_DEPTH = 11;
    /** Inner temple Y size, floor..ceiling inclusive (orig :3748). */
    private static final int TEMPLE_HEIGHT = 9;

    private IncaPyramidGenerator() {}

    /**
     * Direct port of {@code makeIncaPyramid(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:3735-3952). {@code origin} is the grass-level
     * anchor (worldgen, orig OreSpawnWorld.java:2351-2361 &mdash; the 41&times;31
     * floor plate replaces the grass surface) or the Dungeon Spawner Block
     * position (outcome 29, orig DungeonSpawnerBlock.java:140-142).
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int bx = origin.getX();
        int by = origin.getY();
        int bz = origin.getZ();

        // In-memory write-set model for the ramp probes (class Javadoc):
        // packed positions currently non-air per the structure's own writes.
        Set<Long> solid = new HashSet<>();

        buildSteppedBase(piece, random, solid, bx, by, bz);   // orig :3755-3782

        // orig :3784-3878 — four byte-identical ramp loops (west/east/north/
        // south), collapsed into one helper; safe because the ramps draw no
        // randomness. Walk formulae: west i = -10+m (:3785), east
        // i = 41+10-m-1 = 50-m (:3809), north k = -10+m (:3833), south
        // k = 31+10-m-1 = 40-m (:3857).
        buildRamp(piece, solid, bx, by, bz, true, -BASE_HEIGHT, +1);                    // west, orig :3784-3806
        buildRamp(piece, solid, bx, by, bz, true, BASE_WIDTH + BASE_HEIGHT - 1, -1);    // east, orig :3808-3830
        buildRamp(piece, solid, bx, by, bz, false, -BASE_HEIGHT, +1);                   // north, orig :3832-3854
        buildRamp(piece, solid, bx, by, bz, false, BASE_DEPTH + BASE_HEIGHT - 1, -1);   // south, orig :3856-3878

        // orig :3879-3881 — the method shifts its own cpos by +baseheight on
        // all three axes before building the inner temple.
        int tx = bx + BASE_HEIGHT;
        int ty = by + BASE_HEIGHT;
        int tz = bz + BASE_HEIGHT;

        buildTempleShell(piece, random, tx, ty, tz);          // orig :3882-3919
        buildRoofRim(piece, tx, ty, tz);                      // orig :3920-3928

        // orig :3929-3933 — five 3x3 cobble water altars on the temple floor:
        // four corners (their pads overwrite the wall columns at j=1, spec
        // §11.6 — write order preserved) + room centre.
        makePoolAltar(piece, tx + 1, ty, tz + 1);                                // orig :3929
        makePoolAltar(piece, tx + TEMPLE_WIDTH - 2, ty, tz + TEMPLE_DEPTH - 2);  // orig :3930
        makePoolAltar(piece, tx + 1, ty, tz + TEMPLE_DEPTH - 2);                 // orig :3931
        makePoolAltar(piece, tx + TEMPLE_WIDTH - 2, ty, tz + 1);                 // orig :3932
        makePoolAltar(piece, tx + TEMPLE_WIDTH / 2, ty, tz + TEMPLE_DEPTH / 2);  // orig :3933

        // orig :3934-3937 — 4 CreeperRepellent blocks on the diagonal corners
        // one above the centre altar pad (width/2 = 10, depth/2 = 5).
        BlockState repellent = ModBlocks.CREEPER_REPELLENT.get().defaultBlockState();
        piece.place(tx + 9, ty + 2, tz + 4, repellent);
        piece.place(tx + 11, ty + 2, tz + 6, repellent);
        piece.place(tx + 9, ty + 2, tz + 6, repellent);
        piece.place(tx + 11, ty + 2, tz + 4, repellent);

        // orig :3938-3942 — Molenoid spawner at (width/2 - 2, +1, depth/2).
        piece.placeSpawner(tx + 8, ty + 1, tz + 5, ModEntities.ENTITY_MOLENOID.get());

        // orig :3943-3950 — trapdoor + ladder shaft from the temple floor down
        // into the hollow base cavity. Trapdoor meta 3 → facing east; ladder
        // meta 2 → facing north, mounted on the cobble backing column at z+1.
        piece.place(tx + 12, ty + 1, tz + 5, Blocks.OAK_TRAPDOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));      // orig :3943
        piece.place(tx + 12, ty, tz + 5, Blocks.AIR.defaultBlockState());                // orig :3944 — floor hole
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState ladder = Blocks.LADDER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        for (int j = 1; j < BASE_HEIGHT; j++) {                                          // orig :3947-3950
            piece.place(tx + 12, ty - j, tz + 6, cobble);
            piece.place(tx + 12, ty - j, tz + 5, ladder);
        }

        // orig :3951 — graveyard, called with the ORIGINAL base origin
        // (cpos - baseheight un-does the temple shift).
        makeIncaGraves(piece, random, bx, by, bz);
    }

    /**
     * The stepped hollow pyramid base (orig GenericDungeon.java:3755-3782):
     * layer j (0..9) is a 1-high hollow rectangular ring of
     * (41-2j)&times;(31-2j) inset one block per side per layer; layer 0 is a
     * solid stone-brick floor plate. Ring wall cells roll
     * stone&nbsp;3/8&nbsp;/ cobblestone&nbsp;3/8&nbsp;/ mossy&nbsp;1/4
     * (orig :3760-3768 — the rolls run for EVERY perimeter cell, including
     * layer 0's, before the j==0 stone-brick override at :3769-3771, so both
     * draws stay in the stream). Layers with j%3==2 (2/5/8) carry interior
     * torch rows on both the north ring wall's inner face (k==1, meta 3 =
     * facing south, orig :3772-3775) and the south wall's inner face (extra
     * write at z-1, meta 4 = facing north, orig :3777-3779).
     *
     * <p>All writes go through {@link #placeModeled} — the ramp probes read
     * this state back (class Javadoc).</p>
     */
    private static void buildSteppedBase(LegacyDungeonPiece piece, RandomSource random,
                                         Set<Long> solid, int bx, int by, int bz) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState torchSouth = Blocks.WALL_TORCH.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        BlockState torchNorth = Blocks.WALL_TORCH.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        for (int j = 0; j < BASE_HEIGHT; j++) {                       // orig :3755
            int maxI = BASE_WIDTH - j * 2 - 1;
            int maxK = BASE_DEPTH - j * 2 - 1;
            for (int i = 0; i <= maxI; i++) {                         // orig :3756
                for (int k = 0; k <= maxK; k++) {                     // orig :3757
                    BlockState state = air;                           // orig :3758-3759
                    if (i == 0 || k == 0 || i == maxI || k == maxK) { // orig :3760-3768 — ring perimeter
                        state = stone;
                        if (random.nextInt(2) == 0) {
                            state = cobble;
                        }
                        if (random.nextInt(4) == 0) {
                            state = mossy;
                        }
                    }
                    if (j == 0) {                                     // orig :3769-3771 — solid floor plate
                        state = stoneBricks;
                    }
                    if (k == 1 && j % 3 == 2 && i != 0 && i != maxI) { // orig :3772-3775 — north torch row
                        state = torchSouth;
                    }
                    placeModeled(piece, solid, bx + i + j, by + j, bz + k + j, state); // orig :3776
                    if (k == maxK && j % 3 == 2 && i != 0 && i != maxI) { // orig :3777-3779 — south torch row
                        placeModeled(piece, solid, bx + i + j, by + j, bz + k + j - 1, torchNorth);
                    }
                }
            }
        }
    }

    /**
     * One staircase ramp (orig GenericDungeon.java:3784-3806; the east/north/
     * south loops :3808-3830/:3832-3854/:3856-3878 are byte-identical up to
     * the walk formula — see the call sites). Per step {@code m = 0..18}
     * (rise {@code j = m/2}) across the 5 lanes {@code p = -2..2} centred on
     * the face midline (z = basedepth/2 = 15 for the X-axis ramps, x =
     * basewidth/2 = 20 for the Z-axis ramps):
     * <ul>
     *   <li>rails ({@code p == ±2}): stone bricks at y+j+1, plus a standing
     *       torch one higher at both ramp ends (m == 0 and m == 18);</li>
     *   <li>treads ({@code |p| <= 1}, odd m): smooth stone slab at y+j+1;</li>
     *   <li>support pillar (every lane): stone filling downward from y+j.</li>
     * </ul>
     * Every write is gated on the model probe {@link #isModelAir} — the
     * original's world-read gate, reproduced for self-reads and deviating
     * (treat-as-air) for pre-build terrain per the class Javadoc.
     *
     * @param alongX true for the west/east ramps (m walks X, lanes span Z)
     * @param start  the walk coordinate at m = 0 (relative to the base origin)
     * @param step   +1 for the ascending (west/north) loops, -1 for east/south
     */
    private static void buildRamp(LegacyDungeonPiece piece, Set<Long> solid,
                                  int bx, int by, int bz, boolean alongX, int start, int step) {
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();

        int lastM = BASE_HEIGHT * 2 - 2;                              // = 18
        for (int m = 0; m <= lastM; m++) {                            // orig :3784
            int walk = start + step * m;                              // orig :3785 (etc.)
            int j = m / 2;                                            // orig :3789
            for (int p = -2; p <= 2; p++) {                           // orig :3786
                // orig :3787-3788 — fixed lane on the face midline.
                int x = alongX ? bx + walk : bx + BASE_WIDTH / 2 + p;
                int z = alongX ? bz + BASE_DEPTH / 2 + p : bz + walk;
                if (p < -1 || p > 1) {
                    // orig :3790-3797 — rail, with end torches nested in the
                    // same air gate.
                    if (isModelAir(solid, x, by + j + 1, z)) {
                        placeModeled(piece, solid, x, by + j + 1, z, stoneBricks);
                        if (m == 0 || m == lastM) {
                            placeModeled(piece, solid, x, by + j + 2, z, torch);
                        }
                    }
                } else if (m % 2 == 1 && isModelAir(solid, x, by + j + 1, z)) {
                    // orig :3798-3800 — tread.
                    placeModeled(piece, solid, x, by + j + 1, z, slab);
                }
                // orig :3801-3804 — support pillar: fill stone downward until
                // the model shows a non-air block, never below relative y 0.
                int fill = j;
                while (fill >= 0 && isModelAir(solid, x, by + fill, z)) {
                    placeModeled(piece, solid, x, by + fill, z, stone);
                    --fill;
                }
            }
        }
    }

    /**
     * The inner temple shell (orig GenericDungeon.java:3882-3919), a
     * 21&times;11&times;9 box at the shifted origin. Override order per cell
     * (orig :3885-3915): air default; perimeter walls with the same
     * 3/8-3/8-1/4 stone/cobble/mossy rolls as the base; stone-brick floor and
     * ceiling (j==0 / j==8); 3-wide doorway openings mid-wall on all four
     * sides at j 1..3 (the j==3 row is an oak-fence lintel); and at
     * {@code (i+k)%2==1} a lit-redstone-lamp checkerboard punched INTO the
     * walls at j==6 (only where the cell is not already air) with matching
     * air holes straight through at j==7.
     */
    private static void buildTempleShell(LegacyDungeonPiece piece, RandomSource random,
                                         int tx, int ty, int tz) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        BlockState litLamp = Blocks.REDSTONE_LAMP.defaultBlockState()
                .setValue(BlockStateProperties.LIT, true);            // field_150374_bv = the LIT lamp block

        for (int j = 0; j < TEMPLE_HEIGHT; j++) {                     // orig :3882
            for (int i = 0; i < TEMPLE_WIDTH; i++) {                  // orig :3883
                for (int k = 0; k < TEMPLE_DEPTH; k++) {              // orig :3884
                    BlockState state = air;                           // orig :3885-3886
                    if (i == 0 || k == 0 || i == TEMPLE_WIDTH - 1 || k == TEMPLE_DEPTH - 1) {
                        state = stone;                                // orig :3887-3895
                        if (random.nextInt(2) == 0) {
                            state = cobble;
                        }
                        if (random.nextInt(4) == 0) {
                            state = mossy;
                        }
                    }
                    if (j == 0 || j == TEMPLE_HEIGHT - 1) {           // orig :3896-3898 — floor + ceiling
                        state = stoneBricks;
                    }
                    if (j == 1 || j == 2 || j == 3) {                 // orig :3899-3906 — doorways
                        if ((k == 0 || k == TEMPLE_DEPTH - 1)
                                && i >= TEMPLE_WIDTH / 2 - 1 && i <= TEMPLE_WIDTH / 2 + 1) {
                            state = j == 3 ? fence : air;
                        }
                        if ((i == 0 || i == TEMPLE_WIDTH - 1)
                                && k >= TEMPLE_DEPTH / 2 - 1 && k <= TEMPLE_DEPTH / 2 + 1) {
                            state = j == 3 ? fence : air;
                        }
                    }
                    // orig :3907-3915 — wall checkerboard: lit lamps at j==6
                    // (height-3, only in non-air cells), air holes at j==7.
                    if ((j == TEMPLE_HEIGHT - 3 || j == TEMPLE_HEIGHT - 2) && (i + k) % 2 == 1) {
                        if (j == TEMPLE_HEIGHT - 3) {
                            if (!state.isAir()) {
                                state = litLamp;
                            }
                        } else {
                            state = air;
                        }
                    }
                    piece.place(tx + i, ty + j, tz + k, state);       // orig :3916
                }
            }
        }
    }

    /**
     * The roof slab rim (orig GenericDungeon.java:3920-3928): at
     * {@code j = height = 9} (one above the ceiling), a checkered
     * ({@code (i+k)&1 == 1}) ring of smooth stone slabs on the EXTENDED
     * perimeter i/k = -1 or width/depth &mdash; one block outside the temple
     * walls, floating with nothing beneath (spec &sect;11.7).
     */
    private static void buildRoofRim(LegacyDungeonPiece piece, int tx, int ty, int tz) {
        BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        int j = TEMPLE_HEIGHT;                                        // orig :3922
        for (int i = -1; i <= TEMPLE_WIDTH; i++) {                    // orig :3923
            for (int k = -1; k <= TEMPLE_DEPTH; k++) {                // orig :3924
                // orig :3925 — skip unless on the extended rim AND checkered.
                if ((i != -1 && k != -1 && i != TEMPLE_WIDTH && k != TEMPLE_DEPTH)
                        || (i + k & 1) != 1) {
                    continue;
                }
                piece.place(tx + i, ty + j, tz + k, slab);            // orig :3926
            }
        }
    }

    /**
     * Direct port of {@code makepoolalter} (orig GenericDungeon.java:3954-3961):
     * a 3&times;3 cobblestone pad at cy+1 whose centre block is then replaced
     * with a still-water source.
     */
    private static void makePoolAltar(LegacyDungeonPiece piece, int cx, int cy, int cz) {
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        for (int i = -1; i <= 1; i++) {                               // orig :3955
            for (int k = -1; k <= 1; k++) {                           // orig :3956
                piece.place(cx + i, cy + 1, cz + k, cobble);          // orig :3957
            }
        }
        piece.place(cx, cy + 1, cz, Blocks.WATER.defaultBlockState()); // orig :3960
    }

    /**
     * Direct port of {@code makeincagraves} (orig GenericDungeon.java:3963-3977),
     * called with the base origin and width/depth 41/31: four rows of six
     * graves at x = 5, 11, 17, 23, 29, 35 ({@code i} from 5 step 6 while
     * i &lt; 36) &mdash; rows z+5 and z+10 face dir 1 (grave extends south,
     * chest north), rows z+20 and z+25 face dir 3 (the exact z-mirror).
     * 24 graves total, all inside the hollow base cavity at ground level.
     */
    private static void makeIncaGraves(LegacyDungeonPiece piece, RandomSource random,
                                       int bx, int by, int bz) {
        for (int i = 5; i < BASE_WIDTH - 5; i += 6) {                 // orig :3965-3967
            makeIncaGrave(piece, random, bx + i, by, bz + 5, false);
        }
        for (int i = 5; i < BASE_WIDTH - 5; i += 6) {                 // orig :3968-3970
            makeIncaGrave(piece, random, bx + i, by, bz + 10, false);
        }
        for (int i = 5; i < BASE_WIDTH - 5; i += 6) {                 // orig :3971-3973
            makeIncaGrave(piece, random, bx + i, by, bz + 20, true);
        }
        for (int i = 5; i < BASE_WIDTH - 5; i += 6) {                 // orig :3974-3976
            makeIncaGrave(piece, random, bx + i, by, bz + 25, true);
        }
    }

    /**
     * Direct port of {@code makeincagrave} (orig GenericDungeon.java:3979-4042).
     * The dir 1 layout (:3984-4011) extends toward +Z; dir 3 (:4013-4041) is
     * its exact z-mirror &mdash; collapsed here into one body with
     * {@code s = ±1} ({@code makeincagrave} has no code for dir 2/4, spec
     * &sect;11.4). Per grave: two flower beds flanking the plot (grass blocks
     * punched into the stone-brick floor plate with poppy/dandelion/poppy on
     * top), a stone headstone with two smooth-stone-slab grave slabs, a
     * "Ghost" spawner atop the headstone on a 1/3 roll (drawn UNCONDITIONALLY
     * &mdash; stitching contract &mdash; write gated), and a north-facing
     * (meta 2, orig :4007/:4036) loot chest behind the headstone.
     *
     * @param mirrored false = dir 1 (rows z+5/z+10), true = dir 3 (z+20/z+25)
     */
    private static void makeIncaGrave(LegacyDungeonPiece piece, RandomSource random,
                                      int gx, int gy, int gz, boolean mirrored) {
        int s = mirrored ? -1 : 1;
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState poppy = Blocks.POPPY.defaultBlockState();
        BlockState dandelion = Blocks.DANDELION.defaultBlockState();
        BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();

        // orig :3985-3996 / :4014-4025 — flower beds west (gx-1) then east
        // (gx+1) of the plot: poppy, dandelion, poppy marching away from the
        // headstone.
        for (int dx = -1; dx <= 1; dx += 2) {
            piece.place(gx + dx, gy, gz, grass);
            piece.place(gx + dx, gy + 1, gz, poppy);
            piece.place(gx + dx, gy, gz + s, grass);
            piece.place(gx + dx, gy + 1, gz + s, dandelion);
            piece.place(gx + dx, gy, gz + 2 * s, grass);
            piece.place(gx + dx, gy + 1, gz + 2 * s, poppy);
        }
        piece.place(gx, gy + 1, gz, Blocks.STONE.defaultBlockState()); // orig :3997/:4026 — headstone
        piece.place(gx, gy + 1, gz + s, slab);                         // orig :3998/:4027 — grave slab
        piece.place(gx, gy + 1, gz + 2 * s, slab);                     // orig :3999/:4028
        // orig :4000-4006 / :4029-4035 — Ghost spawner atop the headstone,
        // 1/3 per grave. The roll is drawn unconditionally; only the write is
        // chunk-gated (stitching contract).
        boolean ghost = random.nextInt(3) == 1;
        if (ghost) {
            piece.placeSpawner(gx, gy + 2, gz, ModEntities.GHOST.get());
        }
        // orig :4007-4011 / :4036-4040 — loot chest behind the headstone
        // (dir 1: z-1, dir 3: z+1), chest meta 2 = facing north in both
        // branches; fill 10-14 weighted stacks (counts live in the JSON).
        piece.placeLootChest(gx, gy + 1, gz - s, Direction.NORTH, INCA_PYRAMID_LOOT);
    }

    // ---- Write-set model (ramp self-reads, class Javadoc) ----------------

    /**
     * {@code piece.place} plus write-set bookkeeping: non-air states join the
     * model, air writes leave it. The model tracks the structure's FINAL
     * in-memory state (e.g. the south torch rows overwrite ring-interior
     * cells first written as air) so the ramp probes see exactly what the
     * original's mid-build world reads saw. Bookkeeping is NEVER chunk-gated
     * &mdash; every pass must model the whole build identically or the ramp
     * pillars would diverge at chunk seams.
     */
    private static void placeModeled(LegacyDungeonPiece piece, Set<Long> solid,
                                     int x, int y, int z, BlockState state) {
        piece.place(x, y, z, state);
        if (state.isAir()) {
            solid.remove(BlockPos.asLong(x, y, z));
        } else {
            solid.add(BlockPos.asLong(x, y, z));
        }
    }

    /**
     * The ramp air probe (replaces the original's {@code func_147439_a} world
     * reads, orig :3791 etc.): air unless the structure's own write set says
     * otherwise. Unrecorded cells &mdash; pre-build terrain &mdash; read as
     * air, the spec &sect;10 sanctioned deviation (class Javadoc).
     */
    private static boolean isModelAir(Set<Long> solid, int x, int y, int z) {
        return !solid.contains(BlockPos.asLong(x, y, z));
    }
}
