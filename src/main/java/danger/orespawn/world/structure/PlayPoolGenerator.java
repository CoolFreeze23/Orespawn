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
 * The Play Pool (vanilla-overworld Ocean), ported line-by-line from 1.7.10
 * {@code GenericDungeon.makePlayPool} (orig GenericDungeon.java:1934-1957)
 * &mdash; one of the WGEN-042 structure pool. A floating 6-long water trough
 * at y+18 (X &minus;1..+4, single Z column) pours down around a row of four
 * "Attack Squid" spawners at y+16 and a chest pair at y+17; only the middle
 * two columns (x+1, x+2) are capped by the chests, so water cascades at
 * x&minus;1, x, x+3, x+4 into the open sea below.
 *
 * <p>Mapping decisions (spec: {@code phase_d_reports/d6_extraction/
 * play_pool_spec.md}):</p>
 * <ul>
 * <li><b>Anchor</b>: the origin is the AIR block directly above the ocean
 * surface &mdash; {@code addPlayPool} passes the scan hit {@code posY} with
 * NO {@code -1} offset (orig OreSpawnWorld.java:1148, unlike Monster
 * Island/FrogPond). Worldgen hands that anchor in via
 * {@code PlacementMode.OCEAN_SURFACE_AIR}; the Dungeon Spawner Block path
 * (type 12, orig DungeonSpawnerBlock.java:89-91) uses the clicked position
 * unmodified. All offsets here are therefore the original's, unchanged.</li>
 * <li><b>Loot on ONE chest only</b>: both chests are placed (orig :1946-1947)
 * but the tile-entity fetch + fill target x+1 only (orig :1948-1951); the
 * x+2 chest is placed empty. Preserved verbatim &mdash; do not "fix".</li>
 * <li><b>Flowing-water caps</b>: the original caps use
 * {@code Blocks.field_150358_i} (flowing_water, meta 0), which the 1.13
 * flattening maps &mdash; like the meta-0 still-water row &mdash; to the
 * {@code minecraft:water} source state (spec &sect;6). All six trough
 * blocks are therefore water source blocks here.</li>
 * <li><b>Flag-3 water writes</b> (orig :1953-1956, the method's only
 * non-flag-2 writes): the original notified neighbours to start the
 * cascade. {@code piece.place} writes flag 2 only, but modern
 * {@code LiquidBlock.onPlace} self-schedules a fluid tick, so the cascade
 * still starts &mdash; PARITY note, non-behavioral (spec S3).</li>
 * <li><b>Double-chest cosmetics</b> (spec S2): 1.7.10's adjacent pair
 * auto-merged into a double chest; modern default-state chests stay
 * singles. Accepted as a documented cosmetic deviation &mdash; loot
 * behavior is identical.</li>
 * </ul>
 *
 * <p>Chest loot: {@code orespawn:chests/play_pool} transcribes the original
 * 4-entry {@code SquidContentsList} (orig GenericDungeon.java:49, total
 * weight 80) with the original 3-7 stack rolls ({@code 3 + nextInt(5)},
 * orig :1950); the random-slot collision quirk is dropped, the same
 * documented approximation as every chest-list conversion since Phase C.
 * With the fill in JSON this generator consumes ZERO random draws &mdash;
 * trivially stitch-safe across the &le; 2 intersecting chunks.</p>
 */
final class PlayPoolGenerator {

    /** Loot for the x+1 chest only (orig GenericDungeon.java:49 + :1948-1951). */
    private static final ResourceKey<LootTable> PLAY_POOL_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/play_pool"));

    private PlayPoolGenerator() {}

    /**
     * Direct port of {@code makePlayPool(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:1934-1957). {@code origin} is the air block
     * above the ocean surface (worldgen, orig OreSpawnWorld.java:1148) or
     * the Dungeon Spawner Block position (type 12, orig
     * DungeonSpawnerBlock.java:89-91 &mdash; no validation there: a Play
     * Pool on land or underground is faithful behavior). Writes run in
     * source order: spawners &rarr; chests &rarr; water.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        // orig :1940-1945 — four "Attack Squid" spawners along +X at y+16.
        for (int i = 0; i < 4; i++) {
            piece.placeSpawner(x + i, y + 16, z, ModEntities.ATTACK_SQUID.get());
        }

        // orig :1946-1951 — chest pair on the middle two spawners at y+17.
        // ONLY the x+1 chest gets the loot fill (orig fetches its tile
        // entity alone, :1948); the x+2 chest is deliberately empty.
        piece.placeLootChest(x + 1, y + 17, z, PLAY_POOL_LOOT);
        piece.place(x + 2, y + 17, z, Blocks.CHEST.defaultBlockState());

        // orig :1952-1956 — the water trough at y+18: four still-water
        // blocks over the spawner row, then the two flowing-water caps at
        // x-1 / x+4 (both flatten to the modern water source state).
        BlockState water = Blocks.WATER.defaultBlockState();
        for (int i = 0; i < 4; i++) {
            piece.place(x + i, y + 18, z, water);
        }
        piece.place(x - 1, y + 18, z, water);
        piece.place(x + 4, y + 18, z, water);
    }
}
