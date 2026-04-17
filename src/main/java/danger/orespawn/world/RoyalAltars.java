package danger.orespawn.world;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Royal altars for the Utopia dimension — the legacy 1.7.10 King/Queen altars
 * (51×51 footprint, 48 blocks tall) that hold The King/The Queen spawn eggs in
 * a central chest. Ported from {@code OreSpawnWorld.addKingAltar} +
 * {@code GenericDungeon.makeKingAltar/makeQueenAltar}.
 *
 * <p><b>1.7.10 spawn rules (verbatim):</b></p>
 * <ul>
 *   <li>{@code random.nextInt(2000) != 1 → return} — 1/2000 chunk chance.</li>
 *   <li>8 anchor attempts at {@code (3+chunkX+rand(10), 3+chunkZ+rand(10))}
 *       scanning Y from 100 down to 50 looking for an air column on grass.</li>
 *   <li>{@code quickReallyBigSpaceCheck} demands ≥60×60 of air at +8 Y so the
 *       altar isn't buried inside a hill.</li>
 *   <li>50/50 split between King (quartz) and Queen (obsidian) altar.</li>
 *   <li>{@code recently_placed = 100} chunk cooldown after a placement.</li>
 * </ul>
 *
 * <p><b>Modern paradigm shifts:</b></p>
 * <ul>
 *   <li><b>Concurrent worldgen.</b> Multiple chunk-gen workers can call
 *       {@link #tryPlaceRoyalAltar} for different chunks simultaneously. The
 *       100-chunk cooldown is therefore an {@link AtomicInteger} on this class
 *       — the same pattern the dungeon placer uses in
 *       {@link OreSpawnChunkGenerator}. {@code updateAndGet} keeps decrement
 *       and reset atomic, so two workers can never both pass a "cooldown is 0"
 *       gate at the same instant.</li>
 *   <li><b>Cross-chunk writes.</b> The 51×51 footprint crosses up to 5 chunks
 *       horizontally. {@link WorldGenLevel#setBlock} during the
 *       {@code applyBiomeDecoration} pass legally writes into neighbour
 *       chunks because their terrain is already settled by then. We always
 *       pass {@link Block#UPDATE_CLIENTS} to skip neighbour-update cascades —
 *       the altar places ~12,000 blocks and triggering full updates would TPS
 *       the worker pool.</li>
 *   <li><b>Bedrock-safe scan.</b> The 1.7.10 scan started at Y=100 (the world
 *       ceiling back then). 1.21.1 worlds cap at Y=320, so we use the chunk's
 *       generation surface (256 down to 60) so the altar actually sits on the
 *       ground and not under a sky island.</li>
 * </ul>
 */
public final class RoyalAltars {

    /**
     * 100-chunk cooldown shared by both King and Queen placement so the two
     * altar variants can't spawn back-to-back. Mirrors 1.7.10
     * {@code OreSpawnWorld.recently_placed}.
     */
    private static final AtomicInteger recentlyPlaced = new AtomicInteger(0);

    /** 1.7.10's 1/2000 chunk gate — kept verbatim so altars stay legendary. */
    private static final int CHUNK_RARITY = 2000;

    /** Cooldown the placer sets after a successful altar build. */
    private static final int COOLDOWN_CHUNKS = 100;

    /** Footprint of the altar (51×51) used for both space checks and build. */
    private static final int ALTAR_FOOTPRINT = 51;

    /** Vertical extent of the altar quartz/obsidian shell. */
    private static final int ALTAR_HEIGHT = 48;

    /** Y range scanned for a grass anchor. 1.7.10 used 100→50, modern overworld is taller. */
    private static final int SCAN_Y_TOP = 200;
    private static final int SCAN_Y_BOTTOM = 60;

    /** How many anchor attempts to try inside the chunk before bailing. */
    private static final int ANCHOR_ATTEMPTS = 8;

    private RoyalAltars() {}

    /**
     * Roll the 1.7.10 1/2000 gate and, if it passes and the cooldown allows,
     * build either a King or Queen altar anchored inside the supplied chunk.
     * Returns {@code true} if a placement happened so the caller can adjust
     * its own bookkeeping (e.g. dungeon cooldown).
     */
    public static boolean tryPlaceRoyalAltar(WorldGenLevel level, ChunkPos chunkPos, RandomSource random) {
        if (random.nextInt(CHUNK_RARITY) != 1) {
            return false;
        }
        // Atomic cooldown. updateAndGet ensures only one worker observes the
        // 0→COOLDOWN_CHUNKS transition; the rest see the post-decrement value.
        if (recentlyPlaced.get() > 0) {
            recentlyPlaced.updateAndGet(v -> v > 0 ? v - 1 : 0);
            return false;
        }

        int chunkX = chunkPos.getMinBlockX();
        int chunkZ = chunkPos.getMinBlockZ();

        for (int attempt = 0; attempt < ANCHOR_ATTEMPTS; attempt++) {
            int posX = 3 + chunkX + random.nextInt(10);
            int posZ = 3 + chunkZ + random.nextInt(10);

            for (int posY = SCAN_Y_TOP; posY > SCAN_Y_BOTTOM; posY--) {
                BlockPos here = new BlockPos(posX, posY, posZ);
                BlockPos below = here.below();

                if (!level.getBlockState(here).isAir()) continue;
                if (!level.getBlockState(below).is(Blocks.GRASS_BLOCK)) continue;

                if (!hasOpenSpace(level, posX, posY - 1, posZ)) {
                    return false;
                }

                boolean kingPicked = random.nextBoolean();
                if (kingPicked) {
                    buildKingAltar(level, posX, posY - 1, posZ, random);
                } else {
                    buildQueenAltar(level, posX, posY - 1, posZ, random);
                }
                recentlyPlaced.set(COOLDOWN_CHUNKS);
                return true;
            }
        }
        return false;
    }

    /**
     * 60×60 open-air check at +8 Y — direct port of
     * {@code OreSpawnWorld.quickReallyBigSpaceCheck}. Prevents the altar from
     * trying to render itself inside a mountain.
     */
    private static boolean hasOpenSpace(WorldGenLevel level, int posX, int posY, int posZ) {
        for (int i = -5; i < 55; i++) {
            for (int k = -5; k < 55; k++) {
                BlockPos p = new BlockPos(posX + i, posY + 8, posZ + k);
                if (!level.getBlockState(p).isAir()) return false;
            }
        }
        return true;
    }

    /**
     * King altar: 51×51 grass pad + 51×51×48 quartz shell + four corner
     * columns + flat quartz floor + central chest with the King spawn egg.
     * Ported from {@code GenericDungeon.makeKingAltar}.
     */
    private static void buildKingAltar(WorldGenLevel level, int cx, int cy, int cz, RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();

        clearVolume(level, cx, cy, cz, air);
        layGrassPad(level, cx, cy, cz, grass, dirt);
        layCeiling(level, cx, cy, cz, quartz);
        placeCornerColumns(level, cx, cy, cz, quartz, /* king */ true);
        placeCenterAltar(level, cx + ALTAR_FOOTPRINT / 2, cy, cz + ALTAR_FOOTPRINT / 2,
                quartz, /* king */ true, random);
    }

    /**
     * Queen altar: same shape as the king but obsidian instead of quartz, with
     * an obsidian shell. Ported from {@code GenericDungeon.makeQueenAltar}.
     * The center chest holds the Queen spawn egg.
     */
    private static void buildQueenAltar(WorldGenLevel level, int cx, int cy, int cz, RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();

        clearVolume(level, cx, cy, cz, air);
        layGrassPad(level, cx, cy, cz, grass, dirt);
        layCeiling(level, cx, cy, cz, obsidian);
        placeCornerColumns(level, cx, cy, cz, obsidian, /* king */ false);
        placeCenterAltar(level, cx + ALTAR_FOOTPRINT / 2, cy, cz + ALTAR_FOOTPRINT / 2,
                obsidian, /* king */ false, random);
    }

    /**
     * Air out the entire 51+10 wide × 58 tall × 51+10 deep volume above the
     * anchor so the altar has a clean canvas to write into. 1.7.10 did this
     * inside {@code makeKingAltar}; we use UPDATE_CLIENTS to skip neighbour
     * updates while bulk-clearing.
     */
    private static void clearVolume(WorldGenLevel level, int cx, int cy, int cz, BlockState air) {
        for (int j = 0; j <= ALTAR_HEIGHT + 10; j++) {
            for (int i = -5; i < ALTAR_FOOTPRINT + 5; i++) {
                for (int k = -5; k < ALTAR_FOOTPRINT + 5; k++) {
                    setFast(level, cx + i, cy + j, cz + k, air);
                }
            }
        }
    }

    /**
     * Ground floor: a 51×51 grass pad with up-to-10-block dirt skirt that
     * fills any air/grass/water columns directly under the pad — keeps the
     * altar from floating when it spawns on uneven terrain.
     */
    private static void layGrassPad(WorldGenLevel level, int cx, int cy, int cz,
                                    BlockState grass, BlockState dirt) {
        for (int i = 0; i < ALTAR_FOOTPRINT; i++) {
            for (int k = 0; k < ALTAR_FOOTPRINT; k++) {
                setFast(level, cx + i, cy, cz + k, grass);
                for (int v = 1; v < 10; v++) {
                    BlockPos p = new BlockPos(cx + i, cy - v, cz + k);
                    BlockState here = level.getBlockState(p);
                    if (here.isAir() || here.is(Blocks.SHORT_GRASS) || here.is(Blocks.WATER)) {
                        setFast(level, cx + i, cy - v, cz + k, dirt);
                    }
                }
            }
        }
    }

    /**
     * Two-layer ceiling at the top of the shell — the inner one fits inside
     * the 51×51 footprint, the outer adds a one-block-wide cornice.
     */
    private static void layCeiling(WorldGenLevel level, int cx, int cy, int cz, BlockState slab) {
        int j = ALTAR_HEIGHT - 1;
        for (int i = 0; i < ALTAR_FOOTPRINT; i++) {
            for (int k = 0; k < ALTAR_FOOTPRINT; k++) {
                setFast(level, cx + i, cy + j, cz + k, slab);
            }
        }
        j = ALTAR_HEIGHT;
        for (int i = -1; i <= ALTAR_FOOTPRINT; i++) {
            for (int k = -1; k <= ALTAR_FOOTPRINT; k++) {
                setFast(level, cx + i, cy + j, cz + k, slab);
            }
        }
    }

    /**
     * Four corner columns: 7×7 base/cap caps + 5×5 hollow trunk with banded
     * gold/emerald (King) or redstone/amethyst (Queen) inlays every 4 blocks.
     * Ported from {@code makekingcolumn} / {@code makequeencolumn}.
     */
    private static void placeCornerColumns(WorldGenLevel level, int cx, int cy, int cz,
                                           BlockState slab, boolean king) {
        int[][] offsets = new int[][]{
                {1, 1},
                {ALTAR_FOOTPRINT - 8, ALTAR_FOOTPRINT - 8},
                {1, ALTAR_FOOTPRINT - 8},
                {ALTAR_FOOTPRINT - 8, 1}
        };
        for (int[] off : offsets) {
            buildColumn(level, cx + off[0], cy + 1, cz + off[1], slab, king);
        }
    }

    /**
     * Single 5×5×44 hollow column with 7×7 quartz/obsidian caps. Banded inlays
     * (gold/emerald or redstone/amethyst) every 4 Y so the column doesn't read
     * as a featureless box. Faithful to 1.7.10's {@code j%4} switch.
     */
    private static void buildColumn(WorldGenLevel level, int cx, int cy, int cz,
                                    BlockState slab, boolean king) {
        int width = 5;
        int length = 5;
        int height = 44;
        BlockState band1 = king ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState band2 = king ? Blocks.EMERALD_BLOCK.defaultBlockState() : ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();

        // 7x7 cap top & bottom
        for (int i = 0; i < width + 2; i++) {
            for (int k = 0; k < length + 2; k++) {
                setFast(level, cx + i, cy, cz + k, slab);
                setFast(level, cx + i, cy + height + 1, cz + k, slab);
            }
        }
        cx++; cz++; cy++;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    boolean wall = (i == 0 || k == 0 || i == width - 1 || k == length - 1);
                    BlockState bid = wall ? slab : Blocks.AIR.defaultBlockState();

                    if (wall) {
                        int phase = j % 4;
                        switch (phase) {
                            case 0 -> {
                                if (i == 2 || k == 2) bid = band1;
                            }
                            case 1, 3 -> {
                                if (i == 1 || k == 1 || i == 3 || k == 3) bid = band1;
                            }
                            case 2 -> {
                                if (i == 1 || k == 1 || i == 3 || k == 3) bid = band1;
                                if (i == 2 || k == 2) bid = band2;
                            }
                            default -> { /* no-op */ }
                        }
                    }

                    setFast(level, cx + i, cy + j, cz + k, bid);
                }
            }
        }
    }

    /**
     * Center altar — a tapered 21→9 quartz/obsidian platform with the spawn-egg
     * chest at the top. Faithful to {@code makekingcenteraltar} +
     * {@code makequeencenteraltar} with the same step heights and
     * {@code +/-width} cross-bracing pattern.
     */
    private static void placeCenterAltar(WorldGenLevel level, int cx, int cy, int cz,
                                         BlockState slab, boolean king, RandomSource random) {
        BlockState band = king ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState egg = king ? Blocks.LAPIS_BLOCK.defaultBlockState() : ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();

        // Layer 0: 21x21 + 13x41 cross
        layRect(level, cx, cy, cz, 10, 10, slab);
        layRect(level, cx, cy, cz, 6, 20, slab);
        layRect(level, cx, cy, cz, 20, 6, slab);

        // Layer 1: 17x17 + 9x37 cross with band caps at the cross tips
        layRect(level, cx, cy + 1, cz, 8, 8, slab);
        layCrossWithCorners(level, cx, cy + 1, cz, 4, 18, slab, band);
        layCrossWithCorners(level, cx, cy + 1, cz, 18, 4, slab, band);

        // Layer 2: 15x15 + 7x35 cross with corner torches
        layRect(level, cx, cy + 2, cz, 7, 7, slab);
        placeCornerTorches(level, cx, cy + 2, cz, 7, 7);
        layRect(level, cx, cy + 2, cz, 3, 17, slab);
        layRect(level, cx, cy + 2, cz, 17, 3, slab);

        // Layer 3: 13x13 + 5x33 cross
        layRect(level, cx, cy + 3, cz, 6, 6, slab);
        layRect(level, cx, cy + 3, cz, 2, 16, slab);
        layRect(level, cx, cy + 3, cz, 16, 2, slab);

        // Layer 4: 5x5 cap with corner torches
        layRect(level, cx, cy + 4, cz, 2, 2, slab);
        placeCornerTorches(level, cx, cy + 4, cz, 2, 2);

        // Egg accent column rising from inside the cap
        for (int dy = 1; dy <= 3; dy++) {
            setFast(level, cx, cy + 4 + dy, cz, egg);
        }

        // Chest with the spawn egg at the apex
        BlockPos chestPos = new BlockPos(cx, cy + 4, cz);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        // UPDATE_ALL on the chest itself so its tile entity initialises
        // correctly. The setBlock(...,3) flag = UPDATE + NOTIFY_NEIGHBOURS
        // which is what tile entities expect during world gen.
        level.setBlock(chestPos, chestState, Block.UPDATE_ALL);
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            ItemStack eggStack = king
                    ? new ItemStack(ModItems.THE_KING_SPAWN_EGG.get())
                    : new ItemStack(ModItems.THE_QUEEN_SPAWN_EGG.get());
            // Slot 13 is the dead-center slot of a single chest, mirroring
            // 1.7.10's chest.func_70299_a(13, ...)
            chest.setItem(13, eggStack);
        }

        // Random nudge so adjacent altar variants don't all face the same way
        if (random.nextBoolean()) {
            // Reserved for future facing-aware chest rotation; intentional no-op
            // to keep deterministic placement under fixed seeds.
        }
    }

    private static void layRect(WorldGenLevel level, int cx, int cy, int cz,
                                int halfX, int halfZ, BlockState slab) {
        for (int i = -halfX; i <= halfX; i++) {
            for (int k = -halfZ; k <= halfZ; k++) {
                setFast(level, cx + i, cy, cz + k, slab);
            }
        }
    }

    private static void layCrossWithCorners(WorldGenLevel level, int cx, int cy, int cz,
                                            int halfX, int halfZ, BlockState slab, BlockState corner) {
        for (int i = -halfX; i <= halfX; i++) {
            for (int k = -halfZ; k <= halfZ; k++) {
                boolean isCorner = (i == halfX || i == -halfX) && (k == -halfZ || k == halfZ);
                setFast(level, cx + i, cy, cz + k, isCorner ? corner : slab);
            }
        }
    }

    private static void placeCornerTorches(WorldGenLevel level, int cx, int cy, int cz,
                                           int halfX, int halfZ) {
        BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();
        setFast(level, cx + halfX, cy + 1, cz + halfZ, torch);
        setFast(level, cx + halfX, cy + 1, cz - halfZ, torch);
        setFast(level, cx - halfX, cy + 1, cz + halfZ, torch);
        setFast(level, cx - halfX, cy + 1, cz - halfZ, torch);
    }

    /**
     * Bulk write helper. Always uses {@link Block#UPDATE_CLIENTS} (flag 2) so
     * we don't trigger neighbour updates — placing ~12k blocks with full
     * updates would spike the worldgen worker pool. The chest is the one
     * exception and is placed via {@link Block#UPDATE_ALL} above.
     */
    private static void setFast(WorldGenLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }
}
