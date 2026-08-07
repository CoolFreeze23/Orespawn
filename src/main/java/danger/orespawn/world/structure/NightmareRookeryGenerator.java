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
 * The Nightmare Rookery (Islands dimension), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeNightmareRookery}
 * (orig GenericDungeon.java:5242-5312) &mdash; one of the WGEN-042
 * structure pool. Two identical passes each build a wandering ridge of 26
 * random-height stone spires; every spire that rolls tall enough (h &ge;
 * 19) is truncated at its 19th block and capped with a loot chest and a
 * floating "Nightmare" spawner, producing a cluster of jagged spires
 * crowned with {@code PitchBlack} spawners.
 *
 * <p>Note this is the structure that actually generated in 1.7.10: the
 * standalone {@code NightmareDungeon} class the audit asked for
 * (WGEN-038) is proven dead code &mdash; never instantiated anywhere in
 * the original tree (exhaustive search in
 * {@code phase_d_reports/d5_extraction/nightmare_spec.md} &sect;1).</p>
 *
 * <p>Chest loot: {@code orespawn:chests/nightmare_rookery} transcribes
 * the original 10-entry {@code NightmareRookeryContentsList}
 * (orig GenericDungeon.java:29) with the original 4-8 stack rolls
 * ({@code 4 + nextInt(5)}, orig :5278/:5308); the random-slot collision
 * quirk is dropped, the same documented approximation as every other
 * chest-list conversion since Phase C.</p>
 */
final class NightmareRookeryGenerator {

    /** Loot for the spire-top chests (orig GenericDungeon.java:29 + :5278). */
    private static final ResourceKey<LootTable> ROOKERY_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/nightmare_rookery"));

    private NightmareRookeryGenerator() {}

    /**
     * Direct port of {@code makeNightmareRookery(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:5242-5312). {@code origin} is the grass-level
     * anchor (worldgen) or the Dungeon Spawner Block position (outcome 38,
     * orig DungeonSpawnerBlock.java:167-169).
     *
     * <p>The Z drift {@code k} is deliberately NOT reset between the two
     * passes (orig :5283 reuses pass 1's final value), so the second ridge
     * starts where the first ended. Both passes are bit-identical in the
     * original source; they run here as two calls of one helper.</p>
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int zDrift = 0;                                              // orig :5249
        zDrift = buildSpikeRidge(piece, random, origin, zDrift);     // orig :5252-5281
        buildSpikeRidge(piece, random, origin, zDrift);              // orig :5282-5311
    }

    /**
     * One 26-column spike-ridge pass (orig GenericDungeon.java:5252-5281;
     * the second pass :5282-5311 is byte-identical). Per column
     * {@code i = -5..20}: the Z drift takes a step of &minus;1/0/+1
     * (orig :5253), the pillar height rolls {@code h = 1 + nextInt(20)}
     * (orig :5254), and each pillar block gets four independent
     * {@code nextInt(j + 5) == 1} side-bulge rolls in +X, &minus;X, +Z,
     * &minus;Z order (orig :5257-5268) &mdash; bulges thin out with height.
     * At the first block with {@code j >= 18} (only pillars that rolled
     * h = 19 or 20; &asymp; 10% of columns) the pillar truncates and is
     * capped with a chest at j+1 and a "Nightmare" spawner
     * (= {@code PitchBlack}, orig OreSpawnMain.java:4023) at j+2 sitting
     * directly on the chest (orig :5269-5279).
     *
     * @return the Z drift after this pass, carried into the next
     */
    private static int buildSpikeRidge(LegacyDungeonPiece piece, RandomSource random,
                                       BlockPos origin, int zDrift) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        for (int column = -5; column <= 20; column++) {
            zDrift += random.nextInt(3) - 1;                          // orig :5253
            int pillarHeight = random.nextInt(20) + 1;                // orig :5254
            for (int j = 0; j < pillarHeight; j++) {
                int x = origin.getX() + column;
                int y = origin.getY() + j;
                int z = origin.getZ() + zDrift;
                piece.place(x, y, z, stone);                          // orig :5256
                // orig :5257-5268 — independent side bulges, +X/−X/+Z/−Z.
                // The rolls are unconditional (stitching contract): only the
                // writes are chunk-gated.
                if (random.nextInt(j + 5) == 1) {
                    piece.place(x + 1, y, z, stone);
                }
                if (random.nextInt(j + 5) == 1) {
                    piece.place(x - 1, y, z, stone);
                }
                if (random.nextInt(j + 5) == 1) {
                    piece.place(x, y, z + 1, stone);
                }
                if (random.nextInt(j + 5) == 1) {
                    piece.place(x, y, z - 1, stone);
                }
                if (j < 18) continue;
                // orig :5269-5279 — spawner cap + chest, then next column.
                piece.placeSpawner(x, y + 2, z, ModEntities.PITCH_BLACK.get());
                piece.placeLootChest(x, y + 1, z, ROOKERY_LOOT);
                break;
            }
        }
        return zDrift;
    }
}
