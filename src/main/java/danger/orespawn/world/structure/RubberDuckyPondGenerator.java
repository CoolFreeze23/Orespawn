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
 * The Rubber Ducky Pond (vanilla-overworld Plains), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeRubberDuckyPond}
 * (orig GenericDungeon.java:5383-5421) &mdash; a 12&times;11 one-block-thick
 * pond pad perched ON the grass (sand perimeter ring, 10&times;9 still-water
 * interior) with a floating two-column totem centered over it at
 * {@code x 0..1, z 0}: a 4-wide water line at y+3, glass blocks at y+4, a
 * chest pair at y+5, and two "Rubber Ducky" spawners at y+6. Water pours from
 * under the glass two blocks down onto the pond surface.
 *
 * <p>Mapping decisions (spec:
 * {@code phase_d_reports/d6_extraction/rubber_ducky_pond_spec.md}):</p>
 * <ul>
 * <li><b>Write order is behavior (spec S2):</b> tower first (spawners y+6
 *     &rarr; chests y+5 &rarr; glass y+4 &rarr; water y+3), pond pad LAST
 *     &mdash; the pond loop's +1/+2 air layers sweep under the already-built
 *     tower and clear the terrain below it (the tower's lowest course is +3,
 *     so nothing of it is erased). Source order is preserved verbatim; do not
 *     "optimize" into bottom-up placement.</li>
 * <li><b>Double-chest cosmetics (spec S3):</b> the original placed two
 *     adjacent chests (orig :5396-5397) that 1.7.10 auto-merged into a double
 *     chest; modern default-state chests stay singles. ONLY the
 *     {@code (+1, +5, 0)} half is TE-fetched and loot-bound (orig
 *     :5398-5401); the {@code (0, +5, 0)} half is deliberately empty &mdash;
 *     faithful oddity, do not "fix"/fill both. Accepted cosmetic deviation,
 *     the {@link PlayPoolGenerator}/{@link LeafMonsterDungeonGenerator}
 *     precedent (the filled half here is the SECOND-placed +1 chest, same as
 *     Play Pool's x+1).</li>
 * <li><b>Flowing water flattens to source (spec S4):</b> the two
 *     {@code field_150358_i} meta-0 caps at x&minus;1/x+2 (orig :5407-5408)
 *     map &mdash; like the meta-0 still-water writes &mdash; to the modern
 *     {@code minecraft:water} source state, so all four y+3 blocks are
 *     sources ({@link PlayPoolGenerator} shipped precedent). The pond cascade
 *     widens by one block each side exactly as in 1.7.10.</li>
 * <li><b>Flag-3 water writes (spec S5):</b> the four y+3 water writes were
 *     the method's only flag-3 (neighbor-notifying) writes (orig :5405,
 *     :5407-5408); {@code piece.place} writes flag 2 only, but modern
 *     {@code LiquidBlock} self-schedules a fluid tick on placement, so the
 *     cascade still starts &mdash; PARITY note, non-behavioral. Flip side:
 *     the POND water (flag 2 in the original, orig :5415) also fluid-ticks in
 *     the port, so edge cells overhanging a slope can drain where 1.7.10's
 *     stayed metastable until a block update &mdash; same PARITY
 *     classification.</li>
 * <li><b>Perched, not dug (spec S6):</b> the anchor is the AIR block above
 *     Plains grass (orig OreSpawnWorld.java:1228-1229, no Y offset) and the
 *     pond layer is written AT rel 0 &mdash; nothing at or below
 *     {@code cposy - 1}; the pre-existing terrain is the pond bottom. Do not
 *     excavate.</li>
 * <li><b>The totem floats (spec S7):</b> water/glass/chests/spawners hang
 *     +3..+6 over the pond with air at +1..+2 and no connecting column;
 *     flag-2 writes keep it (and the overhanging sand ring) suspended. Do not
 *     add supports.</li>
 * <li><b>Glass is the full block (spec S10):</b> {@code field_150359_w}
 *     (orig :5402-5403), NOT panes &mdash; no connection-state concerns.</li>
 * <li><b>RNG (spec &sect;11):</b> the original's only draw, the chest fill
 *     count {@code 8 + nextInt(5)} (orig :5400), moved into the loot JSON's
 *     rolls (uniform 8-12), so this generator consumes ZERO random draws and
 *     satisfies the per-chunk-replay stitching contract vacuously. Its only
 *     world reads were tile-entity fetches at just-written positions (orig
 *     :5392/:5398), absorbed by the {@code placeSpawner}/{@code
 *     placeLootChest} helpers &mdash; no terrain probes anywhere.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/rubber_ducky_pond} transcribes the
 * original 13-entry {@code RubberDuckyContentsList} (orig
 * GenericDungeon.java:27, every weight 35, total weight 455) with the
 * original 8-12 fill rolls &mdash; ten fish/bug items, the Rubber Ducky
 * spawn egg, the peacock feather (stacks 4-10 each) and vanilla feathers
 * (stacks 6-16). The random-slot collision quirk is dropped, the same
 * documented approximation as every chest-list conversion since Phase C.</p>
 */
final class RubberDuckyPondGenerator {

    /** Loot for the (+1, +5, 0) chest half only (orig GenericDungeon.java:27 + :5398-5401). */
    private static final ResourceKey<LootTable> POND_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/rubber_ducky_pond"));

    private RubberDuckyPondGenerator() {}

    /**
     * Direct port of {@code makeRubberDuckyPond(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:5383-5421). {@code origin} is the first air
     * block directly ABOVE a Plains grass block (worldgen,
     * orig OreSpawnWorld.java:1228-1229 &mdash; NO Y offset, so the pond pad
     * replaces the anchor air block and sits one block above the terrain
     * grass) or the Dungeon Spawner Block position (type 40, orig
     * DungeonSpawnerBlock.java:173-175 &mdash; no validation there: a
     * floating or terrain-embedded pond is faithful behavior; on that path
     * the modern water fluid-ticks immediately and a floating pond drains
     * where 1.7.10's flag-2 water stayed metastable, spec &sect;9). Write
     * order is behavior and is preserved verbatim: spawners &rarr; chests
     * &rarr; glass &rarr; water line &rarr; pond pad + air sweep.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        BlockState air = Blocks.AIR.defaultBlockState();
        // Both field_150355_j meta 0 AND field_150358_i meta 0 flatten to the
        // modern source state — see the class Javadoc on the spec-S4 delta.
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState sand = Blocks.SAND.defaultBlockState();               // field_150354_m

        // Spawner pair at y+6 (orig :5390-5395): "Rubber Ducky"
        // (orig OreSpawnMain.java:4393) → orespawn:rubber_ducky (spec §4).
        for (int i = 0; i < 2; i++) {
            piece.placeSpawner(cx + i, cy + 6, cz, ModEntities.ENTITY_RUBBER_DUCKY.get()); // orig :5391-5394
        }

        // Chest pair at y+5 (orig :5396-5401), source order: the (0, +5, 0)
        // chest is placed first and NEVER fetched — deliberately empty; only
        // the (+1, +5, 0) chest gets the loot fill (orig fetches its tile
        // entity alone, :5398 — fill count 8 + nextInt(5) lives in the loot
        // table's 8-12 rolls). Both are default-state singles — see the class
        // Javadoc on the double-chest cosmetic deviation.
        piece.place(cx, cy + 5, cz, Blocks.CHEST.defaultBlockState());   // orig :5396
        piece.placeLootChest(cx + 1, cy + 5, cz, POND_LOOT);             // orig :5397 + :5398-5401

        // Glass pair at y+4 (orig :5402-5403) — full glass blocks, not panes
        // (spec S10).
        piece.place(cx, cy + 4, cz, Blocks.GLASS.defaultBlockState());       // orig :5402
        piece.place(cx + 1, cy + 4, cz, Blocks.GLASS.defaultBlockState());   // orig :5403

        // Water line at y+3 (orig :5404-5408) — the method's only flag-3
        // writes: two still-water sources under the glass, widened to 4 by
        // the two flowing-water caps at x-1 / x+2 (all four are sources in
        // the port, spec S4/S5).
        for (int i = 0; i < 2; i++) {
            piece.place(cx + i, cy + 3, cz, water);                      // orig :5404-5406
        }
        piece.place(cx - 1, cy + 3, cz, water);                          // orig :5407
        piece.place(cx + 2, cy + 3, cz, water);                          // orig :5408

        // Pond pad (orig :5409-5420): 12×11 layer at rel y 0 (rel X -5..+6,
        // rel Z -5..+5) — sand on the perimeter ring, still water inside —
        // plus the +1/+2 air layers clearing terrain over the whole
        // footprint AND under the floating totem. The totem columns
        // (0..1, 0) are interior cells (i = 5..6, k = 5), so the anchor
        // column itself ends up water at rel 0.
        for (int i = 0; i < 12; i++) {
            for (int k = 0; k < 11; k++) {
                BlockState bid = water;                                  // orig :5411
                if (i == 0 || k == 0 || i == 11 || k == 10) {
                    bid = sand;                                          // orig :5412-5414 — perimeter ring
                }
                piece.place(cx + i - 5, cy, cz + k - 5, bid);            // orig :5415
                piece.place(cx + i - 5, cy + 1, cz + k - 5, air);        // orig :5416-5417
                piece.place(cx + i - 5, cy + 2, cz + k - 5, air);        // orig :5418
            }
        }
    }
}
