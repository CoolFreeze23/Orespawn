package danger.orespawn.world.structure;

import danger.orespawn.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The Cloud Shark Dungeon (Islands dimension), ported line-by-line from
 * 1.7.10 {@code GenericDungeon.makeCloudSharkDungeon}
 * (orig GenericDungeon.java:2059-2091) &mdash; one of the WGEN-042
 * structure pool. A 7-block floating "cloud": a 2-block vertical
 * glowstone core, four "Cloud Shark" spawners in a cardinal plus around
 * the core top, and a loot chest capping it. No air clearing, no floor,
 * no shell &mdash; the original's flag-2 writes let the cluster float,
 * which {@code piece.place}'s UPDATE_CLIENTS writes reproduce.
 *
 * <p>Worldgen anchor: the original fired at 1/300 per Islands chunk with
 * no gating whatsoever (outside the 19-way dispatch, no
 * {@code recently_placed}, no space check, orig OreSpawnWorld.java:
 * 179-181) at {@code x/z = 4 + chunk + nextInt(8)},
 * {@code y = 150 + nextInt(10)} (orig OreSpawnWorld.java:2423-2428) &mdash;
 * mapped to structure set spacing 17/8 (&radic;300 &asymp; 17.3) and
 * {@code PlacementMode.SKY_BAND_150}. Play-time trigger: Random Dungeon
 * Spawner type 14 (orig DungeonSpawnerBlock.java:95-97, unmodified click
 * position &mdash; a terrain-embedded build there is faithful).</p>
 *
 * <p>Mapping decisions: geometry is fully deterministic (zero RNG draws
 * in orig :2064-2091 except the chest fill count :2089, which moves into
 * the loot JSON), so the generator is draw-free and the RNG stitching
 * contract is trivially satisfied. The chest is placed with meta 0 / no
 * facing call in the original (:2086, contrast the Kyuubi chests'
 * {@code func_72921_c}), so the facing-less {@code placeLootChest}
 * overload is used. The tile-entity reads (orig :2067, :2072, :2077,
 * :2082, :2087) are self-reads on just-placed blocks, subsumed by the
 * piece helpers &mdash; no world reads remain.</p>
 *
 * <p>Chest loot: {@code orespawn:chests/cloud_shark_dungeon} transcribes
 * the original 6-entry {@code CloudSharkContentsList}
 * (orig GenericDungeon.java:47, total weight 140) with the original 4-8
 * stack rolls ({@code 4 + nextInt(5)}, orig :2089); the random-slot
 * collision quirk is dropped, the same documented approximation as every
 * other chest-list conversion since Phase C.</p>
 */
final class CloudSharkDungeonGenerator {

    /** Loot for the cap chest (orig GenericDungeon.java:47 + :2089). */
    private static final ResourceKey<LootTable> CLOUD_SHARK_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("orespawn", "chests/cloud_shark_dungeon"));

    private CloudSharkDungeonGenerator() {}

    /**
     * Direct port of {@code makeCloudSharkDungeon(world, cposx, cposy, cposz)}
     * (orig GenericDungeon.java:2059-2091). {@code origin} is the sky-band
     * anchor (worldgen, Y 150-159) or the Dungeon Spawner Block position
     * (type 14, orig DungeonSpawnerBlock.java:95-97). Seven writes in the
     * original's order; every position is deterministic given the origin.
     */
    static void generate(LegacyDungeonPiece piece, BlockPos origin, RandomSource random) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();
        // Glowstone core: top then bottom (orig :2064-2065).
        piece.place(x, y, z, Blocks.GLOWSTONE.defaultBlockState());
        piece.place(x, y - 1, z, Blocks.GLOWSTONE.defaultBlockState());
        // Four "Cloud Shark" spawners, cardinal plus around the core top,
        // in the original's +X/-X/+Z/-Z order (orig :2066-2085).
        piece.placeSpawner(x + 1, y, z, ModEntities.CLOUD_SHARK.get());
        piece.placeSpawner(x - 1, y, z, ModEntities.CLOUD_SHARK.get());
        piece.placeSpawner(x, y, z + 1, ModEntities.CLOUD_SHARK.get());
        piece.placeSpawner(x, y, z - 1, ModEntities.CLOUD_SHARK.get());
        // Cap chest directly atop the core, default facing (orig :2086-2090;
        // 4-8 stack fill :2089 lives in the loot table's rolls).
        piece.placeLootChest(x, y + 1, z, CLOUD_SHARK_LOOT);
    }
}
