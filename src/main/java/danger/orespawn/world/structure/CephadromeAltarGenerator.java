package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Cephadrome Altar (Islands dimension), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeCephadromeAltar}
 * (orig GenericDungeon.java:4731-4829) &mdash; one of the WGEN-042
 * structure pool. A three-tier cobblestone ziggurat: 9&times;9 base plate,
 * 7&times;7 and 5&times;5 tiers with stone-brick center-cross inlays and
 * corners, and a 3&times;3 cap whose corners are end stone and whose center
 * is the Eye-of-Ender block. At the 7&times;7 corners four pillars rise
 * clear of the upper tiers: stone brick, stone brick, end stone, Extreme
 * Torch on top.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/cephadrome_altar_spec.md}):</p>
 * <ul>
 * <li>The builder is 100% deterministic: zero RNG draws, zero world reads,
 *     no spawner, no chest, no entity spawn (verified over orig
 *     :4731-4829) &mdash; spec &sect;3/&sect;4/&sect;9/&sect;10. The
 *     per-chunk-replay stitching contract is satisfied vacuously; the
 *     {@code random} parameter is unused by design.</li>
 * <li>Write order is behavior and is preserved verbatim: the 5&times;5
 *     tier (loop 6) re-solidifies the middle of the {@code j=+2} air layer
 *     loop 3 just carved, and the 3&times;3 cap (loop 7) re-solidifies the
 *     middle of the {@code j=+3} air layer from loop 4 &mdash; spec S3.
 *     The surviving air is a 7&times;7&times;3 headspace over the upper
 *     tiers (including the one air cell directly above the eye); the
 *     9&times;9 base's outer ring gets NO clearing above it &mdash; spec
 *     S4, do not widen.</li>
 * <li>No loot and no spawner is faithful: the altar is a summoning pad.
 *     A player places an Extreme Torch on the central Eye-of-Ender block
 *     to summon a Cephadrome (ITEM-017,
 *     {@code danger.orespawn.block.BlockExtremeTorch#setPlacedBy} &mdash;
 *     the eye-below gate lives in the block, not here). The four pillar
 *     torches are decorative: flag-2 structure placement never fires
 *     {@code setPlacedBy}, and they sit on end stone, not the eye &mdash;
 *     spec S2/&sect;4.</li>
 * <li>Dead code dropped: {@code boolean meta = false} (orig :4735) is
 *     never read, and the loop-head {@code bid} pre-assignments (orig
 *     :4734/:4748/:4800/:4816) are overwritten per-cell. Orig :4739 is
 *     NOT dead &mdash; loop 1 has no per-cell assignment and stamps that
 *     cobblestone directly &mdash; spec &sect;2.</li>
 * <li>Every original write is meta-0 flag-2 {@code setBlockFast};
 *     {@code piece.place} preserves the no-neighbor-update semantics. The
 *     Extreme Torch is a standing no-collision torch in the port; its
 *     default state on end stone needs no support adaptation &mdash;
 *     spec S9.</li>
 * </ul>
 */
final class CephadromeAltarGenerator {

    private CephadromeAltarGenerator() {}

    /**
     * Direct port of {@code makeCephadromeAltar(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:4731-4829). {@code origin} is the scanned
     * grass block itself (worldgen, orig OreSpawnWorld.java:2193-2196 &mdash;
     * NO Y offset; loop 1's base plate overwrites the grass so the altar
     * sits flush IN the surface, spec S5) or the Dungeon Spawner Block
     * position (outcome 34, orig DungeonSpawnerBlock.java:155-157, no
     * ground gate &mdash; floating/embedded is faithful).
     *
     * <p>Each loop is {@code for (i = -width..width) for (k =
     * -length..length)} writing at {@code (cposx+i, cposy+j, cposz+k)}.
     * Shared cell predicates: <b>cross</b> = {@code k == 0 || i == 0};
     * <b>corner</b> = {@code !(k != -length && k != length || i != -width
     * && i != width)} &hArr; {@code |k| == length && |i| == width} (orig
     * :4755 idiom, De Morgan) &mdash; spec &sect;2.</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();        // field_150347_e
        BlockState stoneBricks = Blocks.STONE_BRICKS.defaultBlockState();       // field_150417_aV
        BlockState endStone = Blocks.END_STONE.defaultBlockState();             // field_150377_bs
        BlockState air = Blocks.AIR.defaultBlockState();                        // field_150350_a
        BlockState extremeTorch = ModBlocks.EXTREME_TORCH.get().defaultBlockState();       // OreSpawnMain.ExtremeTorch
        BlockState eyeOfEnderBlock = ModBlocks.BLOCK_EYE_OF_ENDER.get().defaultBlockState(); // OreSpawnMain.MyEyeOfEnderBlock

        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // Loop 1 — j=0: 9x9 solid cobblestone base plate (orig :4736-4744).
        // Overwrites the grass anchor cell (spec S5). No per-cell bid
        // assignment in the original — the whole plate is the orig :4739
        // cobblestone.
        for (int i = -4; i <= 4; i++) {
            for (int k = -4; k <= 4; k++) {
                piece.place(cx + i, cy, cz + k, cobblestone);                   // orig :4742
            }
        }

        // Loop 2 — j=+1: 7x7 tier, cobblestone with stone-brick center
        // cross (orig :4752-4754) and stone-brick corners (orig :4755-4757).
        for (int i = -3; i <= 3; i++) {
            for (int k = -3; k <= 3; k++) {
                BlockState state = cobblestone;                                 // orig :4751
                if (k == 0 || i == 0) {
                    state = stoneBricks;                                        // orig :4752-4754
                }
                if (isCorner(i, k, 3, 3)) {
                    state = stoneBricks;                                        // orig :4755-4757
                }
                piece.place(cx + i, cy + 1, cz + k, state);                     // orig :4758
            }
        }

        // Loop 3 — j=+2: 7x7 air layer with stone-brick corners (pillar
        // course 2) (orig :4761-4772). The 5x5 middle is re-solidified by
        // loop 6 below — write order preserved (spec S3, §2.1).
        for (int i = -3; i <= 3; i++) {
            for (int k = -3; k <= 3; k++) {
                BlockState state = isCorner(i, k, 3, 3) ? stoneBricks : air;    // orig :4766-4769
                piece.place(cx + i, cy + 2, cz + k, state);                     // orig :4770
            }
        }

        // Loop 4 — j=+3: 7x7 air layer with end-stone corners (pillar
        // course 3) (orig :4773-4784). The 3x3 middle is re-solidified by
        // loop 7 below (spec S3, §2.1).
        for (int i = -3; i <= 3; i++) {
            for (int k = -3; k <= 3; k++) {
                BlockState state = isCorner(i, k, 3, 3) ? endStone : air;       // orig :4778-4781
                piece.place(cx + i, cy + 3, cz + k, state);                     // orig :4782
            }
        }

        // Loop 5 — j=+4: 7x7 air layer with an Extreme Torch on each pillar
        // top (orig :4785-4796). Nothing overwrites this layer; the air cell
        // at (0,+4,0) above the eye is where the player's summoning torch
        // goes (spec §2.2/§4).
        for (int i = -3; i <= 3; i++) {
            for (int k = -3; k <= 3; k++) {
                BlockState state = isCorner(i, k, 3, 3) ? extremeTorch : air;   // orig :4790-4793
                piece.place(cx + i, cy + 4, cz + k, state);                     // orig :4794
            }
        }

        // Loop 6 — j=+2: 5x5 tier, cobblestone with stone-brick cross and
        // corners, overwriting the middle of loop 3's air (orig :4797-4812).
        for (int i = -2; i <= 2; i++) {
            for (int k = -2; k <= 2; k++) {
                BlockState state = cobblestone;                                 // orig :4803
                if (k == 0 || i == 0) {
                    state = stoneBricks;                                        // orig :4804-4806
                }
                if (isCorner(i, k, 2, 2)) {
                    state = stoneBricks;                                        // orig :4807-4809
                }
                piece.place(cx + i, cy + 2, cz + k, state);                     // orig :4810
            }
        }

        // Loop 7 — j=+3: 3x3 cap, cobblestone with the Eye-of-Ender block
        // dead center and end-stone corners, overwriting the middle of
        // loop 4's air (orig :4813-4828). The center and corner predicates
        // are disjoint on a 3x3, but the original's check order
        // (center, then corner) is kept anyway.
        for (int i = -1; i <= 1; i++) {
            for (int k = -1; k <= 1; k++) {
                BlockState state = cobblestone;                                 // orig :4819
                if (k == 0 && i == 0) {
                    state = eyeOfEnderBlock;                                    // orig :4820-4822
                }
                if (isCorner(i, k, 1, 1)) {
                    state = endStone;                                           // orig :4823-4825
                }
                piece.place(cx + i, cy + 3, cz + k, state);                     // orig :4826
            }
        }
    }

    /**
     * The original's corner predicate {@code !(k != -length && k != length
     * || i != -width && i != width)} (orig GenericDungeon.java:4755 et al.)
     * &hArr; {@code |k| == length && |i| == width} &mdash; true only at the
     * four corner cells of the loop's rectangle.
     */
    private static boolean isCorner(int i, int k, int width, int length) {
        return (k == -length || k == length) && (i == -width || i == width);
    }
}
