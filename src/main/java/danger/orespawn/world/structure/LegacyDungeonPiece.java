package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Audit Part 2 &mdash; unified {@link StructurePiece} for the four legacy
 * Tech &amp; Danger dungeons (Shadow, Greenhouse, Robot Lab, White House).
 *
 * <p>All four use the canonical Mansion / Stronghold multi-pass
 * {@code chunkBox.isInside} stitching pattern (see {@link RoyalTreePiece}
 * for the reference implementation), so they can faithfully reproduce
 * the legacy structures' full footprints &mdash; up to ~50 blocks across
 * for the Robot Lab and White House &mdash; without being clipped at
 * chunk borders by the {@link WorldGenLevel} 24-block write window.</p>
 *
 * <p>The legacy generators in {@code GenericDungeon} are ported
 * byte-for-byte: same loops in the same nesting order, same block
 * selections by index, same chest fill counts. Where a legacy item has
 * no 1.21.1 counterpart (e.g. {@code BeeEgg}, {@code MantisEgg}), the
 * closest functional analog is substituted &mdash; documented inline in
 * each chest fill helper.</p>
 */
public class LegacyDungeonPiece extends StructurePiece {

    /** {@code Block.UPDATE_CLIENTS}: no neighbour cascade, no lighting recompute. */
    private static final int FLAG_CLIENTS_ONLY = 2;

    /**
     * Per-dungeon bounding box envelope. Each tuple is sized to the legacy
     * footprint plus a safety margin so {@code postProcess} fires for
     * every chunk the algorithm can touch &mdash; matching the
     * {@link RoyalTreePiece} telemetry-locked pattern (worst-observed
     * extent + ~12-block ceiling).
     *
     * <p>Footprint reference (verified 1.7.10
     * {@code GenericDungeon.java}):</p>
     * <ul>
     *   <li><b>Shadow</b> &mdash; line 1453: 19 wide, double pyramid going
     *       down 10 + up 10 along Y; ±20 horizontal × +20/-12 vertical.</li>
     *   <li><b>Greenhouse</b> &mdash; line 5030: 23 long × 15 wide × 7
     *       tall + 3 above for spawners; ±24 horizontal × +14/-2 vertical.</li>
     *   <li><b>Robot Lab</b> &mdash; line 4044: 10×20 entry, then 30×30
     *       hangar shifted south + 35-tall sniper tower; ±48 horizontal
     *       × +50/-2 vertical.</li>
     *   <li><b>White House</b> &mdash; line 5423: 25×25 base + walls (23×6
     *       tall) + 13-tier stepped roof + 12-tall flagpole + fountains
     *       extending z-15; ±48 horizontal × +25/-2 vertical.</li>
     * </ul>
     */
    public enum DungeonType {
        SHADOW(20, 12, 20),
        GREENHOUSE(24, 2, 14),
        ROBOT_LAB(48, 2, 50),
        WHITE_HOUSE(48, 2, 25),
        // Audit Part 3 — buried 5x5 lapis surface antenna + 17-block descending
        // shaft + 4 cardinal "Part" rooms (W=15 wide max). Down -25, up +6.
        ALIEN_WTF(20, 25, 6),
        // Audit Part 3 — hollow rad=10 sphere with surface decoration shell
        // (legacy line 4677). Down -10, up +5 (hollowed sky cap).
        LEONOPTERYX_NEST(12, 10, 5),
        // Audit Part 3 — 51x51x48 grand altar (legacy line 4353/5697). Origin
        // is the SW corner of the pad, so positive X/Z reach is +56 with the
        // 5-block clear margin; we centre by passing the centre as origin
        // and use ±32 extents to span the full footprint.
        KING_ALTAR(32, 4, 56),
        QUEEN_ALTAR(32, 4, 56),
        // Audit Part 4 — King's / Queen's Challenge Tower (legacy
        // GenericDungeon.makeEnormousCastle line 191 / makeEnormousCastleQ
        // line 6393). 28x28 base + 6 stacked floors stepping inward
        // (10/10/9/9/8/16 tall), 4-block stone foundation skirt, and a
        // ~38-block western spire arm + descending stair. Worst-case
        // footprint = origin -38..+31 horizontal, -2..+88 vertical. We use
        // a symmetric ±40 horizontal box so /locate keeps the player at
        // the body centre rather than at the spire tip, and an upExtent
        // of 95 to include the level-6 Nightmare cap (+5 above the
        // top floor at j=78).
        KING_TOWER(40, 4, 95),
        QUEEN_TOWER(40, 4, 95);

        public final int hExtent;
        public final int downExtent;
        public final int upExtent;

        DungeonType(int h, int d, int u) {
            this.hExtent = h;
            this.downExtent = d;
            this.upExtent = u;
        }
    }

    private final DungeonType dungeonType;
    private final BlockPos origin;

    // Per-postProcess hot-loop cache (canonical RoyalTreePiece pattern).
    private transient WorldGenLevel pLevel;
    private transient BlockPos.MutableBlockPos pMut;
    private transient int pCbMinX;
    private transient int pCbMaxX;
    private transient int pCbMinY;
    private transient int pCbMaxY;
    private transient int pCbMinZ;
    private transient int pCbMaxZ;

    public LegacyDungeonPiece(BlockPos origin, DungeonType dungeonType) {
        super(ModStructureTypes.LEGACY_DUNGEON_PIECE.get(), 0,
                new BoundingBox(
                        origin.getX() - dungeonType.hExtent,
                        origin.getY() - dungeonType.downExtent,
                        origin.getZ() - dungeonType.hExtent,
                        origin.getX() + dungeonType.hExtent,
                        origin.getY() + dungeonType.upExtent,
                        origin.getZ() + dungeonType.hExtent));
        this.origin = origin.immutable();
        this.dungeonType = dungeonType;
    }

    public LegacyDungeonPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructureTypes.LEGACY_DUNGEON_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
        this.dungeonType = DungeonType.valueOf(tag.getString("dt"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("ox", origin.getX());
        tag.putInt("oy", origin.getY());
        tag.putInt("oz", origin.getZ());
        tag.putString("dt", dungeonType.name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        // RoyalTreePiece pattern: deterministic per-piece RNG seeded from the
        // bounding box corners so every chunk pass paints the same slice.
        RandomSource rng = RandomSource.create(
                (long) this.boundingBox.minX() * 341873128712L
                        + (long) this.boundingBox.minZ() * 132897987541L);

        this.pLevel = level;
        this.pMut = new BlockPos.MutableBlockPos();
        this.pCbMinX = chunkBox.minX();
        this.pCbMaxX = chunkBox.maxX();
        this.pCbMinY = chunkBox.minY();
        this.pCbMaxY = chunkBox.maxY();
        this.pCbMinZ = chunkBox.minZ();
        this.pCbMaxZ = chunkBox.maxZ();

        try {
            switch (dungeonType) {
                case SHADOW -> generateShadowDungeon(rng);
                case GREENHOUSE -> generateGreenhouse(rng);
                case ROBOT_LAB -> generateRobotLab(rng);
                case WHITE_HOUSE -> generateWhiteHouse(rng);
                case ALIEN_WTF -> generateAlienWtfDungeon(rng);
                case LEONOPTERYX_NEST -> generateLeonopteryxNest(rng);
                case KING_ALTAR -> generateRoyalAltar(rng, true);
                case QUEEN_ALTAR -> generateRoyalAltar(rng, false);
                case KING_TOWER -> generateChallengeTower(rng, true);
                case QUEEN_TOWER -> generateChallengeTower(rng, false);
            }
        } finally {
            this.pLevel = null;
            this.pMut = null;
        }
    }

    // ---- Per-cell helpers ----------------------------------------------

    /** Returns true iff the target cell is inside the per-chunk write window. */
    private boolean inChunk(int x, int y, int z) {
        return x >= pCbMinX && x <= pCbMaxX
                && y >= pCbMinY && y <= pCbMaxY
                && z >= pCbMinZ && z <= pCbMaxZ;
    }

    /** Gated {@code level.setBlock} ({@link #FLAG_CLIENTS_ONLY}). */
    private void place(int x, int y, int z, BlockState state) {
        if (!inChunk(x, y, z)) return;
        pMut.set(x, y, z);
        pLevel.setBlock(pMut, state, FLAG_CLIENTS_ONLY);
    }

    /** Gated chest placement + immediate fill (within the same postProcess pass). */
    private void placeChest(int x, int y, int z, ChestFiller filler, RandomSource random) {
        if (!inChunk(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        pLevel.setBlock(pos, Blocks.CHEST.defaultBlockState(), FLAG_CLIENTS_ONLY);
        if (pLevel.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            filler.fill(chest, random);
        }
    }

    /** Gated spawner placement + immediate {@link EntityType} bind. */
    private void placeSpawner(int x, int y, int z, EntityType<?> mob) {
        if (!inChunk(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        pLevel.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), FLAG_CLIENTS_ONLY);
        if (pLevel.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, pLevel.getRandom(), pos);
        }
    }

    @FunctionalInterface
    private interface ChestFiller {
        void fill(ChestBlockEntity chest, RandomSource random);
    }

    // ---- Shadow Dungeon ------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeShadowDungeon} (1.7.10
     * source line 1453&ndash;1537). Two stacked pyramids: the lower
     * 19&rarr;1 stepped pyramid descends 10 blocks below origin with
     * obsidian/bedrock walls (alternating by Y parity), soul-sand cross-
     * pattern at midpoint, four corner Nightmare/Ender Reaper spawners
     * on every odd-Y ring (width 9&ndash;15), and four chest fills filled
     * from {@link #shadowContentsList}. The upper pyramid is the same
     * 19&rarr;1 stepped layout going up 10 blocks (no spawners or
     * chests &mdash; pure visual cap, legacy line 1519).
     */
    private void generateShadowDungeon(RandomSource random) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int totalWidth = 19;
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();

        // Phase 1 — lower pyramid descending below origin (legacy line 1467-1515).
        int yoff = 0;
        int xoff = 0;
        int zoff = 0;
        for (int width = totalWidth; width > 0; width -= 2) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < width; k++) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        BlockState blk = obsidian;
                        if ((yoff & 1) != 0) blk = bedrock;
                        // Soul-sand cross at midpoint windows (line 1475).
                        if (k >= width / 2 - 1 && k <= width / 2 + 1
                                || i >= width / 2 - 1 && i <= width / 2 + 1) {
                            blk = soulSand;
                        }
                        place(cposx + i + xoff, cposy - yoff, cposz + k + zoff, blk);
                    } else {
                        place(cposx + i + xoff, cposy - yoff, cposz + k + zoff, air);
                    }
                }
            }
            // Spawner + chest tier (legacy line 1484-1511).
            if (width <= 15 && width >= 9) {
                EntityType<?> mob;
                if ((yoff & 1) != 0) {
                    fillShadowChests(cposx + xoff, cposy - yoff, cposz + zoff, width, 0, random);
                    mob = ModEntities.ENDER_REAPER.get();
                } else {
                    mob = ModEntities.PITCH_BLACK.get();
                }
                placeSpawner(cposx + xoff + 1, cposy - yoff, cposz + zoff + 1, mob);
                placeSpawner(cposx + xoff + width - 2, cposy - yoff, cposz + zoff + 1, mob);
                placeSpawner(cposx + xoff + 1, cposy - yoff, cposz + zoff + width - 2, mob);
                placeSpawner(cposx + xoff + width - 2, cposy - yoff, cposz + zoff + width - 2, mob);
            }
            xoff++;
            zoff++;
            yoff++;
        }

        // Phase 2 — upper pyramid rising above origin (legacy line 1519-1535).
        yoff = 0;
        xoff = 0;
        zoff = 0;
        for (int width = totalWidth; width > 0; width -= 2) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < width; k++) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        BlockState blk = obsidian;
                        if ((yoff & 1) != 0) blk = bedrock;
                        place(cposx + i + xoff, cposy + yoff, cposz + k + zoff, blk);
                    } else {
                        place(cposx + i + xoff, cposy + yoff, cposz + k + zoff, air);
                    }
                }
            }
            xoff++;
            zoff++;
            yoff++;
        }
    }

    /** Direct port of {@code fill_shadow_chests} (1.7.10 line 1539-1568). */
    private void fillShadowChests(int cposx, int cposy, int cposz, int width, int height, RandomSource random) {
        placeChest(cposx + 1, cposy + height, cposz + width / 2, this::fillShadowChest, random);
        placeChest(cposx + width - 2, cposy + height, cposz + width / 2, this::fillShadowChest, random);
        placeChest(cposx + width / 2, cposy + height, cposz + 1, this::fillShadowChest, random);
        placeChest(cposx + width / 2, cposy + height, cposz + width - 2, this::fillShadowChest, random);
    }

    /**
     * Authentic {@code shadowContentsList} (1.7.10 line 52). Each entry
     * preserves the legacy weight by frequency: 13 entries with weights
     * 20/20/15/15/15/25/25/15/15/15/15/15/10. The fill loop runs
     * {@code 3 + nextInt(7)} insertions per legacy {@code
     * WeightedRandomChestContent.func_76293_a} call.
     */
    private void fillShadowChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(Items.GLOWSTONE_DUST, 2 + random.nextInt(7)),     // weight 20
                new ItemStack(Items.GLOWSTONE_DUST, 2 + random.nextInt(7)),     //   (dup for 20 weight)
                new ItemStack(Items.BLAZE_ROD, 4 + random.nextInt(5)),          // weight 20
                new ItemStack(Items.BLAZE_ROD, 4 + random.nextInt(5)),          //   (dup)
                new ItemStack(Items.MAGMA_CREAM, 2 + random.nextInt(7)),        // weight 15
                new ItemStack(Items.BLAZE_POWDER, 2 + random.nextInt(7)),       // weight 15
                new ItemStack(Items.FIRE_CHARGE, 4 + random.nextInt(5)),        // weight 15
                new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)),      // weight 25
                new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)),      //   (dup)
                new ItemStack(Items.RED_DYE, 6 + random.nextInt(11)),           // weight 25
                new ItemStack(Items.RED_DYE, 6 + random.nextInt(11)),           //   (dup)
                new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(7)),      // weight 15
                new ItemStack(ModItems.EXPERIENCE_TREE_SEED.get(), 2 + random.nextInt(3)), // weight 15
                new ItemStack(ModItems.ELEVATOR.get(), 1),                      // weight 15
                new ItemStack(ModItems.NIGHTMARE_SWORD.get(), 1),               // weight 15
                new ItemStack(ModItems.POISON_SWORD.get(), 1),                  // weight 15
                new ItemStack(ModItems.RAT_SWORD.get(), 1),                     // weight 10
                new ItemStack(ModItems.RUBY_SWORD.get(), 1),                    // weight 10
                new ItemStack(ModItems.BIG_HAMMER.get(), 1),                    // weight 15
                new ItemStack(ModItems.INGOT_TITANIUM.get(), 1),                // weight 5
                new ItemStack(ModItems.INGOT_URANIUM.get(), 1),                 // weight 5
                new ItemStack(ModItems.ULTIMATE_SWORD.get(), 1),                // weight 10
                new ItemStack(ModItems.ULTIMATE_BOW.get(), 1),                  // weight 10
                new ItemStack(ModItems.ENDER_REAPER_SPAWN_EGG.get(), 2 + random.nextInt(7)), // weight 15
                new ItemStack(ModItems.PITCH_BLACK_SPAWN_EGG.get(), 2 + random.nextInt(7))   // weight 15
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(7);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- Greenhouse ----------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeGreenhouseDungeon} (1.7.10
     * source line 5030&ndash;5168). 23&times;15&times;7 glass-walled box
     * with iron-block ceiling stripes, glass-pane skylight strip every
     * 4 columns, glowstone every 4&times;4 grid intersection, grass
     * floor with water-irrigation strips every 3 rows, randomized crop
     * planting on farmland, two stacked Triffid spawners on the centre
     * of the roof, and a {@link #fillGreenhouseChest} chest beneath them.
     */
    private void generateGreenhouse(RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        int height = 7;
        int width = 15;
        int length = 23;

        // Centre the box on origin so /locate puts the player inside the
        // greenhouse, not at the SW corner.
        int ox = cposx - length / 2;
        int oz = cposz - width / 2;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7);

        for (int i = 0; i < length; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = 0; j < height; j++) {
                    BlockState blk = air;
                    if (i == 0 || k == 0 || i == length - 1 || k == width - 1) {
                        blk = glass;
                    }
                    if (j == height - 1) {
                        blk = iron;
                        if (i % 4 == 3 && k % 4 == 3) blk = glowstone;
                        if (k % 4 == 1) blk = glass;
                    }
                    if (j == 0) {
                        blk = grass;
                        if (i != 0 && k != 0 && i != length - 1 && k != width - 1 && i % 3 == 2) {
                            blk = water;
                        }
                    }
                    if (j == 1 && i != 0 && k != 0 && i != length - 1 && k != width - 1
                            && i % 3 != 2 && random.nextInt(3) != 1) {
                        // Lay farmland one block below + plant on top
                        // (legacy line 5064-5126).
                        place(ox + i, cposy + j - 1, oz + k, farmland);
                        BlockState plant = pickGreenhousePlant(random.nextInt(20));
                        place(ox + i, cposy + j, oz + k, plant);
                        continue;
                    }
                    place(ox + i, cposy + j, oz + k, blk);
                }
            }
        }
        // Hollow out the headroom above the greenhouse (legacy line 5131-5137).
        for (int i = 0; i < length; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = height; j <= height + 6; j++) {
                    place(ox + i, cposy + j, oz + k, air);
                }
            }
        }
        // Two double-iron-door entry (legacy line 5138-5147).
        place(ox + width / 2, cposy + 1, oz, air);
        place(ox + width / 2, cposy + 2, oz, air);
        place(ox + width / 2 - 1, cposy + 1, oz, air);
        place(ox + width / 2 - 1, cposy + 2, oz, air);
        place(ox + length / 2, cposy + 1, oz, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.DOOR_HINGE, net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT));
        place(ox + length / 2, cposy + 2, oz, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));

        // Roof Triffid spawners (legacy line 5148-5161).
        int i = length / 2;
        int k = width / 2;
        placeSpawner(ox + i, cposy + height + 1, oz + k, ModEntities.ENTITY_TRIFFID.get());
        placeSpawner(ox + i, cposy + height + 2, oz + k, ModEntities.ENTITY_TRIFFID.get());

        // Loot chest at roof base (legacy line 5162-5167).
        placeChest(ox + i, cposy + height, oz + k, this::fillGreenhouseChest, random);
    }

    /**
     * Authentic per-tile RNG (legacy line 5067-5125). Indices 8 and 19
     * are intentional gaps in the legacy table (no plant rolled), so the
     * call must return air on those rolls to match drop frequencies.
     */
    private BlockState pickGreenhousePlant(int t) {
        return switch (t) {
            case 0 -> Blocks.DANDELION.defaultBlockState();
            case 1 -> Blocks.POPPY.defaultBlockState();
            case 2 -> Blocks.BROWN_MUSHROOM.defaultBlockState();
            case 3 -> Blocks.RED_MUSHROOM.defaultBlockState();
            case 4 -> Blocks.WHEAT.defaultBlockState();
            case 5 -> Blocks.CARROTS.defaultBlockState();
            case 6 -> Blocks.POTATOES.defaultBlockState();
            case 7 -> Blocks.PUMPKIN.defaultBlockState();
            case 9 -> ModBlocks.CORN_3.get().defaultBlockState();
            case 10 -> ModBlocks.TOMATO_3.get().defaultBlockState();
            case 11 -> ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState();
            case 12 -> ModBlocks.BUTTERFLY_PLANT.get().defaultBlockState();
            case 13 -> ModBlocks.MOTH_PLANT.get().defaultBlockState();
            case 14 -> ModBlocks.RADISH_PLANT.get().defaultBlockState();
            case 15 -> ModBlocks.LETTUCE_3.get().defaultBlockState();
            case 16 -> ModBlocks.FLOWER_PINK.get().defaultBlockState();
            case 17 -> ModBlocks.FLOWER_BLUE.get().defaultBlockState();
            case 18 -> ModBlocks.QUINOA_3.get().defaultBlockState();
            default -> Blocks.AIR.defaultBlockState(); // t == 8 or t == 19 (legacy gaps)
        };
    }

    /**
     * Authentic {@code GreenhouseContentsList} (1.7.10 line 31). Weights
     * preserved by entry duplication: 35/35/35/35/25/25/25.
     */
    private void fillGreenhouseChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(ModItems.GREEN_GOO.get(), 4 + random.nextInt(7)),
                new ItemStack(ModBlocks.CREEPER_REPELLENT.get(), 4 + random.nextInt(7)),
                new ItemStack(Items.APPLE, 6 + random.nextInt(11)),
                new ItemStack(Items.OAK_SAPLING, 6 + random.nextInt(11)),
                new ItemStack(Items.OAK_LEAVES, 6 + random.nextInt(11)),
                new ItemStack(Items.DIRT, 6 + random.nextInt(11)),
                new ItemStack(Items.OAK_LOG, 6 + random.nextInt(11))
        };
        int slots = chest.getContainerSize();
        int count = 5 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- Robot Lab -----------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeRobotLab} (1.7.10 source
     * line 4044&ndash;4091) and its sub-methods. Builds:
     * <ul>
     *   <li>Entry hall: 10&times;20&times;5 quartz block with iron-block
     *       floor stripe, double iron door + button (line 4076-4083).</li>
     *   <li>Six Robo-Sniper pillars: three on each wall at z = length/3,
     *       2*length/3, length-1 (line 4085-4090).</li>
     *   <li>Main hangar 30&times;30&times;9 (south of entry, x-shifted -10):
     *       quartz walls, iron-block floor stripe, open south side
     *       (line 4127-4158).</li>
     *   <li>Robo-Pounder altar (8&times;8 iron + 6&times;6 quartz +
     *       redstone/torch corners + 2 spawners, line 4223-4258).</li>
     *   <li>Redstone railway (line 4260-4293).</li>
     *   <li>Assembly line with sticky-piston crushers (line 4295-4308).</li>
     *   <li>Robo-Warrior treasure room with iron bars + 2 chests
     *       (line 4310-4351).</li>
     *   <li>Sniper tower: 12&times;12 iron-bar foundation + 4 corner
     *       redstone-torch markers + 4 inner Robo-Sniper pillars + a
     *       30-tall central iron-bar shaft (line 4166-4221).</li>
     * </ul>
     */
    private void generateRobotLab(RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        // Centre on origin so /locate matches the visual centre of the lab.
        int ox = cposx - 5;
        int oz = cposz - 25;

        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // Entry hall (legacy line 4053-4075).
        int width = 10;
        int length = 20;
        int height = 5;
        for (int j = 0; j <= height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = quartz;
                    if (j == 0) {
                        bid = quartz;
                        if (i == width / 2 || i == width / 2 - 1) bid = iron;
                    }
                    if (j == height) {
                        bid = quartz;
                        if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = air;
                    }
                    place(ox + i, cposy + j, oz + k, bid);
                }
            }
        }
        // Carve the iron-door entry (legacy line 4076-4083).
        place(ox + width / 2, cposy + 1, oz, air);
        place(ox + width / 2, cposy + 2, oz, air);
        place(ox + width / 2 - 1, cposy + 1, oz, air);
        place(ox + width / 2 - 1, cposy + 2, oz, air);
        place(ox + width / 2, cposy + 1, oz, Blocks.IRON_DOOR.defaultBlockState());
        place(ox + width / 2 - 1, cposy + 1, oz, Blocks.IRON_DOOR.defaultBlockState());

        // 6 Robo-Sniper pillars (legacy line 4085-4090).
        makeRoboPillar(ox, cposy, oz + length / 3, 0);
        makeRoboPillar(ox, cposy, oz + length * 2 / 3, 0);
        makeRoboPillar(ox, cposy, oz + (length - 1), 0);
        makeRoboPillar(ox + width - 1, cposy, oz + length / 3, 1);
        makeRoboPillar(ox + width - 1, cposy, oz + length * 2 / 3, 1);
        makeRoboPillar(ox + width - 1, cposy, oz + (length - 1), 1);

        // Main hangar (legacy line 4127-4163, shifted south + x-shifted -10).
        makeRoboMain(ox, cposy, oz + length - 1, random);
    }

    /** Direct port of {@code makerobopillar} (1.7.10 line 4093-4125). */
    private void makeRoboPillar(int cposx, int cposy, int cposz, int dir) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        for (int j = 0; j < 5; j++) {
            for (int i = -1; i < 2; i++) {
                for (int k = -1; k < 2; k++) {
                    BlockState bid = quartz;
                    if (j == 2 || j == 3) {
                        if (k == 0 && (i == -1 || i == 1)) bid = redstone;
                        if (i == 0 && (k == -1 || k == 1)) bid = redstone;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        if (dir == 0) {
            placeSpawner(cposx + 1, cposy + 1, cposz, ModEntities.ROBOT_5.get());
        } else {
            placeSpawner(cposx - 1, cposy + 1, cposz, ModEntities.ROBOT_5.get());
        }
    }

    /** Direct port of {@code makerobomain} (1.7.10 line 4127-4164). */
    private void makeRoboMain(int cposx, int cposy, int cposz, RandomSource random) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int width = 30;
        int length = 30;
        int height = 9;
        cposx -= 10; // legacy line 4132 — x-shift
        for (int j = 0; j <= height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = quartz;
                    if (j == 0) {
                        bid = quartz;
                        if (i == width / 2 || i == width / 2 - 1) bid = iron;
                    }
                    if (j == height) {
                        bid = quartz;
                        if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = air;
                    }
                    // Open south face for the entry hall connection (line 4152).
                    if ((j == 1 || j == 2 || j == 3) && k == 0
                            && i >= width / 3 && i < width * 2 / 3) {
                        bid = air;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        makeRoboAltar(cposx + width / 2 - 4, cposy, cposz + 6);
        makeRoboRailway(cposx + 3, cposy, cposz + 10);
        makeRoboAssemblyLine(cposx + width - 4, cposy, cposz + 4);
        makeRoboTreasureRoom(cposx + 9, cposy, cposz + 18, random);
        makeRoboTower(cposx + width / 2 - 6, cposy + height, cposz + length / 2 - 6);
    }

    /** Direct port of {@code makeroboaltar} (1.7.10 line 4223-4258). */
    private void makeRoboAltar(int cposx, int cposy, int cposz) {
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                place(cposx + i, cposy, cposz + k, iron);
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int k = 0; k < 6; k++) {
                place(cposx + i + 1, cposy + 1, cposz + k + 1, quartz);
            }
        }
        place(cposx + 2, cposy + 1, cposz + 2, redstone);
        place(cposx + 2, cposy + 2, cposz + 2, torch);
        place(cposx + 5, cposy + 1, cposz + 5, redstone);
        place(cposx + 5, cposy + 2, cposz + 5, torch);
        place(cposx + 5, cposy + 1, cposz + 2, redstone);
        place(cposx + 5, cposy + 2, cposz + 2, torch);
        place(cposx + 2, cposy + 1, cposz + 5, redstone);
        place(cposx + 2, cposy + 2, cposz + 5, torch);
        placeSpawner(cposx + 3, cposy + 2, cposz + 3, ModEntities.ROBOT_4.get());
        placeSpawner(cposx + 4, cposy + 2, cposz + 4, ModEntities.ROBOT_4.get());
    }

    /** Direct port of {@code makeroborailway} (1.7.10 line 4260-4293). */
    private void makeRoboRailway(int cposx, int cposy, int cposz) {
        BlockState rail = Blocks.RAIL.defaultBlockState();
        BlockState detector = Blocks.DETECTOR_RAIL.defaultBlockState();
        BlockState lever = Blocks.LEVER.defaultBlockState();
        // 4-wide railway from z..z+12, with detector/lever pairs every 4 ties.
        for (int dz = 0; dz <= 12; dz++) {
            BlockState here = (dz == 2 || dz == 6 || dz == 10) ? detector : rail;
            place(cposx + 0, cposy + 1, cposz + dz, here);
            place(cposx + 3, cposy + 1, cposz + dz, here);
            if (dz == 2 || dz == 6 || dz == 10) {
                place(cposx + 1, cposy + 1, cposz + dz, lever);
                place(cposx + 2, cposy + 1, cposz + dz, lever);
            }
        }
    }

    /** Direct port of {@code makeroboassemblyline} (1.7.10 line 4295-4308). */
    private void makeRoboAssemblyLine(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        BlockState piston = Blocks.STICKY_PISTON.defaultBlockState();
        BlockState wool = Blocks.RED_WOOL.defaultBlockState();
        BlockState lever = Blocks.LEVER.defaultBlockState();
        for (int k = 0; k < 24; k++) {
            place(cposx, cposy + 1, cposz + k, quartz);
            place(cposx + 1, cposy + 1, cposz + k, quartz);
            if (k % 3 == 1) {
                place(cposx - 2, cposy + 1, cposz + k, carpet);
                place(cposx, cposy + 2, cposz + k, piston);
                place(cposx, cposy + 3, cposz + k, wool);
            }
            if (k % 3 == 0) {
                place(cposx, cposy + 2, cposz + k, lever);
            }
        }
    }

    /** Direct port of {@code makerobotreasureroom} (1.7.10 line 4310-4351). */
    private void makeRoboTreasureRoom(int cposx, int cposy, int cposz, RandomSource random) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int j = 1; j < 7; j++) {
            for (int i = 0; i < 12; i++) {
                for (int k = 0; k < 8; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == 11 || k == 7) bid = quartz;
                    if (j == 2 && i == 11) bid = bars;
                    if (j == 3 && bid != air) bid = bars;
                    if (!(j != 1 && j != 2 && j != 3 || k != 0 || i != 1 && i != 2)) bid = air;
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        placeSpawner(cposx + 10, cposy + 1, cposz + 1, ModEntities.ROBOT_2.get());
        placeChest(cposx + 8, cposy + 1, cposz + 1, this::fillRobotChest, random);
        placeChest(cposx + 6, cposy + 1, cposz + 1, this::fillRobotChest, random);
    }

    /** Direct port of {@code makerobotower} (1.7.10 line 4166-4221). */
    private void makeRoboTower(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        // Two-layer 12x12 base + corner redstone markers.
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 12; i++) {
                for (int k = 0; k < 12; k++) {
                    BlockState bid = air;
                    if (j == 1) {
                        if (i == 0 || k == 0 || i == 11 || k == 11) bid = bars;
                        if (i == 0 && (k == 0 || k == 11)) bid = redstone;
                        if (i == 11 && (k == 0 || k == 11)) bid = redstone;
                    }
                    if (j == 0) bid = quartz;
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // 4 corner Robo-Sniper pillars (legacy line 4193-4196).
        makeRoboPillar(cposx + 4, cposy + 1, cposz + 4, 1);
        makeRoboPillar(cposx + 7, cposy + 1, cposz + 7, 0);
        makeRoboPillar(cposx + 4, cposy + 1, cposz + 7, 1);
        makeRoboPillar(cposx + 7, cposy + 1, cposz + 4, 0);
        // 30-tall iron-bar central spire (legacy line 4197-4220).
        for (int j = 5; j < 35; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 3; k++) {
                    BlockState bid;
                    if (j < 15) {
                        bid = quartz;
                    } else if (j < 25) {
                        bid = (k == 2) ? bars : quartz;
                    } else {
                        if (k == 1) bid = bars;
                        else if (k == 2) bid = air;
                        else bid = quartz;
                    }
                    place(cposx + i + 5, cposy + j, cposz + k + 5, bid);
                }
            }
        }
    }

    /**
     * Authentic {@code RobotContentsList} (1.7.10 line 37). 11 entries, all
     * weight 35 (so equal-weight uniform random pick); the legacy fill
     * loop runs {@code 10 + nextInt(5)} per chest.
     */
    private void fillRobotChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(Items.REDSTONE, 1 + random.nextInt(10)),
                new ItemStack(Items.CLOCK, 1 + random.nextInt(10)),
                new ItemStack(Items.MINECART, 1),
                new ItemStack(Items.FIRE_CHARGE, 1 + random.nextInt(10)),
                new ItemStack(Items.COMPARATOR, 1),
                new ItemStack(Items.REDSTONE_BLOCK, 1 + random.nextInt(10)),
                new ItemStack(Items.RAIL, 1 + random.nextInt(10)),
                new ItemStack(Items.PISTON, 1 + random.nextInt(10)),
                new ItemStack(Items.STICKY_PISTON, 1 + random.nextInt(10)),
                new ItemStack(Items.DROPPER, 1 + random.nextInt(10)),
                new ItemStack(Items.DISPENSER, 1 + random.nextInt(10))
        };
        int slots = chest.getContainerSize();
        int count = 10 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- White House ---------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeWhiteHouse} (1.7.10 source
     * line 5423&ndash;5694). Composes 7 sub-builds:
     * <ul>
     *   <li>Two fountains (legacy {@code makefountain}, line 5436).</li>
     *   <li>Quartz walkway (legacy {@code makewalkway}, line 5466).</li>
     *   <li>25&times;25 quartz base with crystal-torch corners
     *       (legacy {@code makewhbase}, line 5487).</li>
     *   <li>23&times;6 quartz walls with checkerboard glass-pane windows
     *       and an iron-door entry (legacy {@code makewhwalls}, line 5507).</li>
     *   <li>13-tier stepped quartz roof + 12-tall cobblestone-wall
     *       flagpole + crystal-torch corners (legacy {@code makewhroof},
     *       line 5554).</li>
     *   <li>Carpet/bed interior (legacy {@code makewhinterior}, line 5599).</li>
     *   <li>4 Criminal spawners + 4 chests at the rear back row
     *       (legacy line 5646-5694).</li>
     * </ul>
     */
    private void generateWhiteHouse(RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        // Centre the structure on the origin so /locate matches the
        // visual centre (legacy code laid the structure between
        // [-5..+22] X and [-15..+19] Z relative to its passed cposx/cposz;
        // the natural pivot is +12,+9 from the SW corner).
        int ox = cposx - 12;
        int oz = cposz - 9;

        makeFountain(ox - 5, cposy, oz - 15);
        makeFountain(ox + 15, cposy, oz - 15);
        makeWalkway(ox + 7, cposy, oz - 15);
        makeWhBase(ox - 4, cposy, oz - 6);
        makeWhWalls(ox - 3, cposy + 2, oz - 5);
        makeWhRoof(ox - 4, cposy, oz - 6);
        makeWhInterior(ox - 1, cposy + 2, oz - 3);
        makeWhSpawnersAndChests(ox - 1, cposy + 2, oz - 3, random);
    }

    /** Direct port of {@code makefountain} (1.7.10 line 5436-5464). */
    private void makeFountain(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 7; i++) {
            for (int k = 0; k < 5; k++) {
                for (int j = 0; j < 15; j++) {
                    BlockState bid = water;
                    if (i == 0 || k == 0 || i == 6 || k == 4) bid = quartz;
                    if (j == 0) bid = quartz;
                    if (j == 1 && i == 3 && k == 2) bid = glowstone;
                    if (j > 1) {
                        bid = air;
                        if (j <= 4 && i == 3 && k == 2) bid = quartz;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // Centre water column at j=5 (legacy line 5461-5463).
        place(cposx + 3, cposy + 5, cposz + 2, Blocks.WATER.defaultBlockState());
        place(cposx + 2, cposy + 5, cposz + 2, Blocks.WATER.defaultBlockState());
        place(cposx + 4, cposy + 5, cposz + 2, Blocks.WATER.defaultBlockState());
    }

    /** Direct port of {@code makewalkway} (1.7.10 line 5466-5485). */
    private void makeWalkway(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 10; k++) {
                for (int j = 0; j < 15; j++) {
                    BlockState bid = quartz;
                    if (j == 1) {
                        bid = air;
                        if (k > 6) bid = quartz;
                    }
                    if (j > 1) bid = air;
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
    }

    /** Direct port of {@code makewhbase} (1.7.10 line 5487-5505). */
    private void makeWhBase(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();
        for (int i = 0; i < 25; i++) {
            for (int k = 0; k < 25; k++) {
                place(cposx + i, cposy + 1, cposz + k, quartz);
                if (i != 0 && i != 24 || k != 0 && k != 24) continue;
                place(cposx + i, cposy + 2, cposz + k, torch);
            }
        }
        for (int i = 1; i < 24; i++) {
            for (int k = 1; k < 24; k++) {
                place(cposx + i, cposy + 2, cposz + k, quartz);
            }
        }
    }

    /** Direct port of {@code makewhwalls} (1.7.10 line 5507-5552). */
    private void makeWhWalls(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 23; i++) {
            for (int k = 0; k < 23; k++) {
                for (int j = 0; j < 6; j++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == 22 || k == 22) bid = quartz;
                    if (j != 0 && bid != air) {
                        if (k == 22) {
                            if ((j & 1) == 1) {
                                if ((i & 1) == 0 || (k & 1) == 0) bid = pane;
                            } else if ((i & 1) == 1 || (k & 1) == 1) bid = pane;
                        } else if (k != 0) {
                            if ((j & 1) == 1) {
                                if (i == 2 || k == 2 || i == 20 || k == 20) bid = pane;
                            } else if (i == 1 || k == 1 || i == 21 || k == 21) bid = pane;
                            if (j > 0 && j < 5 && k > 7 && k < 15) bid = pane;
                        } else if ((j & 1) == 1) {
                            if (i == 2 || k == 2 || i == 20 || k == 20) bid = pane;
                        } else if (i == 1 || k == 1 || i == 21 || k == 21) bid = pane;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // Iron door entry (legacy line 5548-5551).
        place(cposx + 11, cposy, cposz, air);
        place(cposx + 11, cposy + 1, cposz, air);
        place(cposx + 11, cposy, cposz, Blocks.IRON_DOOR.defaultBlockState());
        place(cposx + 12, cposy + 1, cposz - 1, Blocks.STONE_BUTTON.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE,
                        net.minecraft.world.level.block.state.properties.AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
    }

    /** Direct port of {@code makewhroof} (1.7.10 line 5554-5597). */
    private void makeWhRoof(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState stairs = Blocks.QUARTZ_STAIRS.defaultBlockState();
        BlockState wall = Blocks.COBBLESTONE_WALL.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int j = 0; j < 13; j++) {
            for (int i = 0; i < 25 - 2 * j; i++) {
                for (int k = 0; k < 25 - 2 * j; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == 24 - 2 * j || k == 24 - 2 * j) bid = quartz;
                    if (j == 0 && bid != air && (i + k & 1) == 1) bid = stairs;
                    if (j == 12) bid = stairs;
                    place(cposx + i + j, cposy + 8 + j, cposz + k + j, bid);
                    if (i != 0 && i != 24 - 2 * j || k != 0 && k != 24 - 2 * j) continue;
                    place(cposx + i + j, cposy + 8 + j + 1, cposz + k + j, torch);
                }
            }
        }
        // 12-tall flagpole + base cross (legacy line 5575-5591).
        for (int dy = 0; dy <= 11; dy++) {
            place(cposx + 12, cposy + 8 + dy, cposz + 12, wall);
        }
        place(cposx + 11, cposy + 8 + 0, cposz + 12, wall);
        place(cposx + 13, cposy + 8 + 0, cposz + 12, wall);
        place(cposx + 12, cposy + 8 + 0, cposz + 11, wall);
        place(cposx + 12, cposy + 8 + 0, cposz + 13, wall);
        // 4 ground-tier torches (legacy line 5593-5596).
        place(cposx + 11, cposy + 8 + 1, cposz + 12, torch);
        place(cposx + 13, cposy + 8 + 1, cposz + 12, torch);
        place(cposx + 12, cposy + 8 + 1, cposz + 11, torch);
        place(cposx + 12, cposy + 8 + 1, cposz + 13, torch);
    }

    /**
     * Direct port of {@code makewhinterior} (1.7.10 line 5599-5645). The
     * legacy code placed alternating red carpet (legacy {@code field_150370_cb}
     * meta=3) and red bed strips (legacy {@code field_150326_M}) at four
     * carpet-bed pair locations: zoff = 1/7/13 with xoff = 0/11.
     */
    private void makeWhInterior(int cposx, int cposy, int cposz) {
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        BlockState bedFoot = Blocks.RED_BED.defaultBlockState()
                .setValue(BlockStateProperties.BED_PART,
                        net.minecraft.world.level.block.state.properties.BedPart.FOOT);
        BlockState bedHead = Blocks.RED_BED.defaultBlockState()
                .setValue(BlockStateProperties.BED_PART,
                        net.minecraft.world.level.block.state.properties.BedPart.HEAD);
        int[] zoffs = {1, 7, 13};
        int[] xoffs = {0, 11};
        for (int zoff : zoffs) {
            for (int xoff : xoffs) {
                for (int i = 0; i < 8; i++) {
                    place(cposx + xoff + i, cposy, cposz + zoff, carpet);
                    place(cposx + xoff + i, cposy, cposz + zoff + 1, bedFoot);
                    place(cposx + xoff + i, cposy, cposz + zoff + 2, bedHead);
                    place(cposx + xoff + i, cposy, cposz + zoff + 3, carpet);
                }
            }
        }
    }

    /** Direct port of {@code makewhinterior} spawner block (1.7.10 line 5646-5694). */
    private void makeWhSpawnersAndChests(int cposx, int cposy, int cposz, RandomSource random) {
        EntityType<?> criminal = ModEntities.BAND_P.get();
        int zoff = 18;
        for (int xoff : new int[]{2, 6, 12, 16}) {
            placeSpawner(cposx + xoff, cposy + 1, cposz + zoff, criminal);
            placeChest(cposx + xoff, cposy, cposz + zoff, this::fillWhiteHouseChest, random);
        }
    }

    /**
     * Authentic {@code WhiteHouseContentsList} (1.7.10 line 26). Weights:
     * 35/10/10/35/25/35/35/35/35/35/35; preserved by entry duplication.
     */
    private void fillWhiteHouseChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(ModItems.CORN_DOG.get(), 6 + random.nextInt(7)),  // 35
                new ItemStack(ModItems.URANIUM_NUGGET.get(), 2 + random.nextInt(5)), // 10
                new ItemStack(ModItems.TITANIUM_NUGGET.get(), 2 + random.nextInt(5)), // 10
                new ItemStack(ModItems.AMETHYST_GEM.get(), 2 + random.nextInt(5)),    // 35
                new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(5)),      // 25
                new ItemStack(ModItems.BAND_P_SPAWN_EGG.get(), 4 + random.nextInt(7)), // 35
                new ItemStack(Items.EMERALD, 6 + random.nextInt(11)),           // 35
                new ItemStack(Items.PORKCHOP, 6 + random.nextInt(11)),          // 35
                new ItemStack(Items.COOKED_PORKCHOP, 6 + random.nextInt(11)),   // 35
                new ItemStack(Items.DIAMOND, 6 + random.nextInt(11)),           // 35
                new ItemStack(Items.GOLD_INGOT, 6 + random.nextInt(11))         // 35
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- WTF-Alien Dungeon (Audit Part 3) -----------------------------

    /**
     * Direct port of {@code GenericDungeon.makeAlienWTFDungeon} (1.7.10
     * source line 1570&ndash;1691). Three composite phases:
     *
     * <ol>
     *   <li><b>Surface antenna</b> &mdash; 5&times;5&times;5 hollow lapis
     *       block on the surface (legacy line 1581-1591), shell built of
     *       {@code Blocks.LAPIS_BLOCK} ({@code field_150369_x}). The
     *       {@code cposy -= depth - 3} on line 1580 anchors the antenna's
     *       top three blocks below the legacy surface anchor.</li>
     *   <li><b>Descending shaft</b> &mdash; 4&times;4 lapis-walled access
     *       shaft from the antenna down 17 blocks (legacy line 1595-1624)
     *       with a single stone "step" inscribed in a rotating corner per
     *       Y (the legacy {@code switch (s)} on line 1605). The rotating
     *       step block is {@code Blocks.STONE} ({@code field_150348_b}).</li>
     *   <li><b>Four cardinal "Part" rooms</b> &mdash; 9/11/13/15 wide cube
     *       rooms (difficulty 1/2/3/4) opening N/E/W/S of the shaft floor
     *       via 1-tall connecting tubes (legacy {@code makePart}, line
     *       1693-1791). Each room has a quartz floor with central obsidian
     *       cross + obsidian walls/ceiling, {@code difficulty} central
     *       Alien/WTF spawners (50/50 random, line 1740-1762), and 1-4
     *       chests filled from {@link #fillAlienWtfChest}.</li>
     * </ol>
     */
    private void generateAlienWtfDungeon(RandomSource random) {
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();

        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();

        int width = 5;
        int height = 5;
        int depth = 20;
        // Legacy line 1580: cposy -= depth - 3 (antenna sinks below grass).
        int antennaY = cposy - (depth - 3);

        // Phase 1: 5x5x5 hollow lapis antenna (legacy line 1581-1591).
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    boolean shell = i == 0 || j == 0 || k == 0
                            || i == width - 1 || j == height - 1 || k == width - 1;
                    place(cposx + i - 2, antennaY + j, cposz + k - 2, shell ? lapis : air);
                }
            }
        }

        // Phase 2: descending 4x4 shaft (legacy line 1592-1624).
        int s = 0;
        int sx = cposx - 1;
        int sz = cposz - 1;
        for (int j = 3; j < depth; j++) {
            for (int i = 0; i < 4; i++) {
                for (int k = 0; k < 4; k++) {
                    boolean shell = i == 0 || k == 0 || i == 3 || k == 3;
                    place(sx + i, antennaY + j, sz + k, shell ? lapis : air);
                }
            }
            switch (s) {
                case 0 -> place(sx + 1, antennaY + j, sz + 1, stone);
                case 1 -> place(sx + 2, antennaY + j, sz + 1, stone);
                case 2 -> place(sx + 2, antennaY + j, sz + 2, stone);
                default -> place(sx + 1, antennaY + j, sz + 2, stone);
            }
            if (++s > 3) s = 0;
        }

        // Phase 3: four cardinal "Part" rooms (legacy line 1625-1690).
        // Each call centres on the shaft floor and steps further outward.
        sx++;
        sz++;
        // North part — width 9, difficulty 1, dx=+1, dz=+1.
        makeAlienPart(sx, antennaY, sz + 7, 9, 5, 1, 1, 1, random);
        carveAlienConnector(sx, antennaY, sz, 3, 6, 0, 1);
        // East part — width 11, difficulty 2, dx=+1, dz=-1.
        makeAlienPart(sx + 7, antennaY, sz, 11, 6, 1, -1, 2, random);
        carveAlienConnector(sx, antennaY, sz, 6, 3, 1, 0);
        // West part — width 13, difficulty 3, dx=-1, dz=+1.
        makeAlienPart(sx - 7, antennaY, sz, 13, 7, -1, 1, 3, random);
        carveAlienConnector(sx, antennaY, sz, 6, 3, -1, 0);
        // South part — width 15, difficulty 4, dx=-1, dz=-1.
        makeAlienPart(sx, antennaY, sz - 7, 15, 8, -1, -1, 4, random);
        carveAlienConnector(sx, antennaY, sz, 3, 6, 0, -1);
    }

    /** Direct port of {@code makePart} (1.7.10 line 1693-1791). */
    private void makeAlienPart(int cposx, int cposy, int cposz,
                               int width, int height, int dx, int dz,
                               int difficulty, RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();

        // Hollow box (legacy line 1699-1705).
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    place(cposx + i * dx, cposy + j, cposz + k * dz, air);
                }
            }
        }
        // Floor — quartz with central obsidian cross (legacy line 1706-1715).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                BlockState floor = quartz;
                if (i == width / 2 || k == width / 2) floor = obsidian;
                place(cposx + i * dx, cposy, cposz + k * dz, floor);
            }
        }
        // Ceiling — pure obsidian (legacy line 1716-1722).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i * dx, cposy + height, cposz + k * dz, obsidian);
            }
        }
        // X-walls (legacy line 1723-1731).
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                place(cposx + i * dx, cposy + j, cposz + 0 * dz, obsidian);
                place(cposx + i * dx, cposy + j, cposz + (width - 1) * dz, obsidian);
            }
        }
        // Z-walls (legacy line 1732-1739).
        for (int k = 0; k < width; k++) {
            for (int j = 0; j < height; j++) {
                place(cposx + 0 * dx, cposy + j, cposz + k * dz, obsidian);
                place(cposx + (width - 1) * dx, cposy + j, cposz + k * dz, obsidian);
            }
        }
        // Spawners (legacy line 1740-1762). Difficulty controls vertical
        // count; each placement randomly picks Alien (mob index 0) or
        // "WTF?" (mob index 1) — both resolve to ALIEN in 1.21.1 since
        // the legacy mod registered them under the same EntityType.
        EntityType<?> alien = ModEntities.ALIEN.get();
        for (int j = 0; j < difficulty; j++) {
            placeSpawner(cposx + dx * width / 2, cposy + j + 2,
                    cposz + dz * width / 2, alien);
            placeSpawner(cposx + dx * width / 2 + dx, cposy + j + 2,
                    cposz + dz * width / 2 + dz, alien);
        }
        // Tiered chests (legacy line 1763-1791).
        placeChest(cposx + width * dx / 2, cposy + 1, cposz + dz,
                this::fillAlienWtfChest, random);
        if (difficulty > 1) {
            placeChest(cposx + width * dx / 2, cposy + 1, cposz + (width - 2) * dz,
                    this::fillAlienWtfChest, random);
        }
        if (difficulty > 2) {
            placeChest(cposx + dx, cposy + 1, cposz + width / 2 * dz,
                    this::fillAlienWtfChest, random);
        }
        if (difficulty > 3) {
            placeChest(cposx + (width - 2) * dx, cposy + 1, cposz + width / 2 * dz,
                    this::fillAlienWtfChest, random);
        }
    }

    /** Carve the 1-tall connecting tube from shaft floor to a Part room. */
    private void carveAlienConnector(int sx, int sy, int sz, int dx, int dz, int rx, int rz) {
        BlockState air = Blocks.AIR.defaultBlockState();
        // 1-tall tube shaft-side -> Part-side, hugging the centerline.
        for (int n = 0; n < 8; n++) {
            int x = sx + (rx == 0 ? dx / 2 : rx * (1 + n));
            int z = sz + (rz == 0 ? dz / 2 : rz * (1 + n));
            place(x, sy + 1, z, air);
            place(x, sy + 2, z, air);
        }
    }

    /**
     * Authentic {@code AlienWTFContentsList} (1.7.10 line 51). 18 entries:
     * Diamond-Block(15), Ruby(20), Amethyst(20), Uranium-Ingot(5),
     * Titanium-Ingot(5), Ultimate-{Helmet,Body,Legs,Boots}(10 each),
     * Ultimate-Bow(15), Nightmare-Sword(15), Experience-Catcher(15),
     * Ray-Gun(10), Cage-Empty(20), Corn-Dog(20), Bacon(20),
     * Popcorn-Bag(20), Fire-Fish(15). Fill loop is
     * {@code 3 + nextInt(5)} per legacy {@code func_76293_a} call.
     */
    private void fillAlienWtfChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(Items.DIAMOND_BLOCK, 1 + random.nextInt(2)),                  // 15
                new ItemStack(ModItems.RUBY.get(), 1),                                      // 20
                new ItemStack(ModItems.RUBY.get(), 1),                                      //   (dup for 20 weight)
                new ItemStack(ModItems.AMETHYST_GEM.get(), 1),                              // 20
                new ItemStack(ModItems.AMETHYST_GEM.get(), 1),                              //   (dup)
                new ItemStack(ModItems.INGOT_URANIUM.get(), 1 + random.nextInt(2)),         // 5
                new ItemStack(ModItems.INGOT_TITANIUM.get(), 1 + random.nextInt(2)),        // 5
                new ItemStack(ModItems.ULTIMATE_HELMET.get(), 1),                           // 10
                new ItemStack(ModItems.ULTIMATE_CHESTPLATE.get(), 1),                       // 10
                new ItemStack(ModItems.ULTIMATE_LEGGINGS.get(), 1),                         // 10
                new ItemStack(ModItems.ULTIMATE_BOOTS_ARMOR.get(), 1),                      // 10
                new ItemStack(ModItems.ULTIMATE_BOW.get(), 1),                              // 15
                new ItemStack(ModItems.NIGHTMARE_SWORD.get(), 1),                           // 15
                new ItemStack(ModItems.EXPERIENCE_CATCHER.get(), 4 + random.nextInt(7)),    // 15
                new ItemStack(ModItems.RAY_GUN.get(), 1),                                   // 10
                new ItemStack(ModItems.CAGE_EMPTY.get(), 1 + random.nextInt(10)),           // 20
                new ItemStack(ModItems.CORN_DOG.get(), 1 + random.nextInt(10)),             // 20
                new ItemStack(ModItems.COOKED_BACON.get(), 1 + random.nextInt(5)),          // 20
                new ItemStack(ModItems.POPCORN_BAG.get(), 2 + random.nextInt(7)),           // 20
                new ItemStack(ModItems.FIRE_FISH.get(), 2 + random.nextInt(7))              // 15
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- Leonopteryx Nest (Audit Part 3) ------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeLeonNest} (1.7.10 source
     * line 4677&ndash;4729). Hollow rad=10 dome with a randomized
     * decoration shell on the outer 2 layers (oak-leaves / oak-log /
     * oak-planks / dirt / cobblestone / mossy-cobblestone — legacy
     * {@code which = nextInt(6)} on line 4692). Hollow interior carved
     * to rad-2. Five-block air pocket above the dome (legacy line
     * 4716-4723). Single Leonopteryx spawner placed at
     * {@code cposy - (rad - 4)} (line 4724-4728) so the boss surfaces
     * out of the centre of the dome when triggered.
     */
    private void generateLeonopteryxNest(RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int rad = 10;
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();

        // Phase 1: hollow dome (legacy line 4685-4715).
        for (int j = 0; j <= rad; j++) {
            for (int i = -rad; i <= rad; i++) {
                for (int k = -rad; k <= rad; k++) {
                    int dist = (int) Math.sqrt(j * j + i * i + k * k);
                    if (dist > rad) continue;
                    BlockState bid = air;
                    if (dist >= rad - 2) {
                        // Per-cell RNG palette pick: 6-way uniform random.
                        int which = random.nextInt(6);
                        bid = switch (which) {
                            case 0 -> Blocks.OAK_LEAVES.defaultBlockState();
                            case 1 -> Blocks.OAK_LOG.defaultBlockState();
                            case 2 -> Blocks.OAK_PLANKS.defaultBlockState();
                            case 3 -> Blocks.DIRT.defaultBlockState();
                            case 4 -> Blocks.COBBLESTONE.defaultBlockState();
                            default -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        };
                    }
                    place(cposx + i, cposy - j, cposz + k, bid);
                }
            }
        }

        // Phase 2: 5-block air pocket above the dome (legacy line 4716-4723).
        for (int j = 1; j <= 5; j++) {
            for (int i = -rad; i <= rad; i++) {
                for (int k = -rad; k <= rad; k++) {
                    place(cposx + i, cposy + j, cposz + k, air);
                }
            }
        }

        // Phase 3: central Leonopteryx spawner (legacy line 4724-4728).
        placeSpawner(cposx, cposy - (rad - 4), cposz, ModEntities.LEONOPTERYX.get());
    }

    // ---- Royal Altars (King + Queen, Audit Part 3) --------------------

    /**
     * Unified port of {@code GenericDungeon.makeKingAltar} (line 4353)
     * and {@code makeQueenAltar} (line 5697). Both share the same
     * 51&times;51&times;48 envelope, four corner columns, ceiling
     * plate, portrait wall, and tapered centre altar; only the palette
     * differs. King uses Quartz/Gold/Emerald (legacy {@code field_150371_ca}
     * / {@code field_150340_R} / {@code field_150475_bE}). Queen uses
     * Obsidian/Redstone/Amethyst.
     *
     * <p><b>King portrait wall &mdash;</b> the 33&times;33 pixel sprite
     * encoded in {@code GenericDungeon.king[]} (line 63). Each
     * non-negative entry runs {@code v} consecutive blocks of the
     * current color and toggles the palette; each {@code -1} fills the
     * remaining columns to width 33 with stone and advances the row.
     * The Queen wall uses the identical sprite data but swaps the
     * {@code field_150371_ca} (quartz) palette half for Block-Ruby.</p>
     */
    private void generateRoyalAltar(RandomSource random, boolean king) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        // The legacy generator anchored its SW corner at (cposx, cposz);
        // for a centred /locate hit we shift so the centre of the 51x51
        // pad lands on origin.
        int ox = cposx - 25;
        int oz = cposz - 25;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState slab = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();

        int width = 51;
        int length = 51;
        int height = 48;

        // Phase 1: clear the build envelope (legacy line 4364-4371).
        for (int j = 0; j <= height + 10; j++) {
            for (int i = -5; i < width + 5; i++) {
                for (int k = -5; k < length + 5; k++) {
                    place(ox + i, cposy + j, oz + k, air);
                }
            }
        }
        // Phase 2: 51x51 grass pad with up-to-10-block dirt skirt
        // (legacy line 4372-4384).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < length; k++) {
                place(ox + i, cposy, oz + k, grass);
                for (int v = 1; v < 10; v++) {
                    if (!inChunk(ox + i, cposy - v, oz + k)) continue;
                    BlockState here = pLevel.getBlockState(
                            new BlockPos(ox + i, cposy - v, oz + k));
                    if (here.isAir() || here.is(Blocks.SHORT_GRASS) || here.is(Blocks.WATER)) {
                        place(ox + i, cposy - v, oz + k, dirt);
                    }
                }
            }
        }
        // Phase 3: four 5x5x44 corner columns (legacy line 4385-4388 / 5729-5732).
        buildRoyalColumn(ox + 1, cposy + 1, oz + 1, king);
        buildRoyalColumn(ox + width - 8, cposy + 1, oz + length - 8, king);
        buildRoyalColumn(ox + 1, cposy + 1, oz + length - 8, king);
        buildRoyalColumn(ox + width - 8, cposy + 1, oz + 1, king);
        // Phase 4: ceiling plate (legacy line 4389-4402 / 5733-5746).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < length; k++) {
                place(ox + i, cposy + height - 1, oz + k, slab);
            }
        }
        for (int i = -1; i <= width; i++) {
            for (int k = -1; k <= length; k++) {
                place(ox + i, cposy + height, oz + k, slab);
            }
        }
        // Phase 5: 33x33 portrait wall (legacy line 4403 -> makekingbackground
        // at 4476 / 5747 -> makequeenbackground at 5817).
        buildRoyalPortraitWall(ox + 4, cposy + 10, oz + 9, king);
        // Phase 6: centre altar pyramid (legacy line 4404 / 5748).
        buildRoyalCenterAltar(ox + width / 2, cposy, oz + length / 2, king);
    }

    /** Direct port of {@code makekingcolumn} / {@code makequeencolumn}. */
    private void buildRoyalColumn(int cposx, int cposy, int cposz, boolean king) {
        BlockState slab = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState band1 = king ? Blocks.GOLD_BLOCK.defaultBlockState()
                : Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState band2 = king ? Blocks.EMERALD_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int width = 5;
        int length = 5;
        int height = 44;

        // 7x7 cap top + bottom (legacy line 4419-4425 / 5763-5769).
        for (int i = 0; i < width + 2; i++) {
            for (int k = 0; k < length + 2; k++) {
                place(cposx + i, cposy, cposz + k, slab);
                place(cposx + i, cposy + height + 1, cposz + k, slab);
            }
        }
        cposx++; cposz++; cposy++;

        // 5x5x44 hollow column with banded inlays
        // (legacy line 4429-4473 / 5773-5814).
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    BlockState bid = air;
                    boolean wall = i == 0 || k == 0 || i == width - 1 || k == length - 1;
                    if (wall) bid = slab;
                    if (wall && j % 4 == 0 && (i == 2 || k == 2)) bid = band1;
                    if (wall && j % 4 == 1) {
                        if (i == 1 || k == 1) bid = band1;
                        if (i == 3 || k == 3) bid = band1;
                    }
                    if (wall && j % 4 == 2) {
                        if (i == 1 || k == 1) bid = band1;
                        if (i == 3 || k == 3) bid = band1;
                        if (i == 2 || k == 2) bid = band2;
                    }
                    if (wall && j % 4 == 3) {
                        if (i == 1 || k == 1) bid = band1;
                        if (i == 3 || k == 3) bid = band1;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
    }

    /**
     * Direct port of {@code makekingbackground} / {@code makequeenbackground}.
     * 33&times;33 single-X-slab portrait sprite encoded in
     * {@link #ROYAL_PORTRAIT_DATA}.
     */
    private void buildRoyalPortraitWall(int cposx, int cposy, int cposz, boolean king) {
        BlockState band = king ? Blocks.GOLD_BLOCK.defaultBlockState()
                : Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState diamond = Blocks.DIAMOND_BLOCK.defaultBlockState();
        BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState foreground = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_RUBY.get().defaultBlockState();

        int height = 33;
        int width = 33;
        int curz = 0;
        int cury = 0;
        BlockState bid = stone;

        for (int v : ROYAL_PORTRAIT_DATA) {
            if (v < 0) {
                // Newline marker — fill remaining row with stone, advance Y.
                bid = stone;
                while (curz < width) {
                    place(cposx, cposy + cury, cposz + curz, bid);
                    curz++;
                }
                cury++;
                curz = 0;
                continue;
            }
            for (int n = 0; n < v; n++) {
                place(cposx, cposy + cury, cposz + curz, bid);
                curz++;
            }
            bid = (bid == stone) ? foreground : stone;
        }

        // Portrait frame: gold/redstone trim (legacy line 4503-4513).
        for (int i = 0; i < width; i++) {
            place(cposx, cposy - 1, cposz + i, band);
            place(cposx, cposy + height, cposz + i, band);
        }
        for (int i = -1; i <= height; i++) {
            place(cposx, cposy + i, cposz - 1, band);
            place(cposx, cposy + i, cposz + width, band);
        }
        // Portrait corner gems + crystal-torch sconces (legacy line 4515-4522).
        place(cposx, cposy - 2, cposz - 2, diamond);
        place(cposx, cposy + height + 1, cposz + width + 1, diamond);
        place(cposx, cposy - 2, cposz + width + 1, diamond);
        place(cposx, cposy + height + 1, cposz - 2, diamond);
        place(cposx, cposy - 1, cposz - 2, torch);
        place(cposx, cposy + height + 2, cposz + width + 1, torch);
        place(cposx, cposy - 1, cposz + width + 1, torch);
        place(cposx, cposy + height + 2, cposz - 2, torch);
    }

    /** Direct port of {@code makekingcenteraltar} / {@code makequeencenteraltar}. */
    private void buildRoyalCenterAltar(int cposx, int cposy, int cposz, boolean king) {
        BlockState slab = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState amethyst = ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
        BlockState corner = king ? lapis : amethyst;
        BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();

        // Layer 0: 21x21 centre + 13x41 cross arms (legacy line 4534-4556).
        layRoyalRect(cposx, cposy, cposz, 10, 10, slab);
        layRoyalRect(cposx, cposy, cposz, 6, 20, slab);
        layRoyalRect(cposx, cposy, cposz, 20, 6, slab);

        // Layer 1: 17x17 centre + 9x37 cross with corner gems (legacy line 4557-4595).
        layRoyalRect(cposx, cposy + 1, cposz, 8, 8, slab);
        layRoyalCrossWithCorners(cposx, cposy + 1, cposz, 4, 18, slab, corner);
        layRoyalCrossWithCorners(cposx, cposy + 1, cposz, 18, 4, slab, corner);

        // Layer 2: 15x15 + 7x35 cross with corner torches (legacy line 4596-4627).
        for (int i = -7; i <= 7; i++) {
            for (int k = -7; k <= 7; k++) {
                place(cposx + i, cposy + 2, cposz + k, slab);
                if (i == 7 && (k == -7 || k == 7)) {
                    place(cposx + i, cposy + 3, cposz + k, torch);
                }
                if (i == -7 && (k == -7 || k == 7)) {
                    place(cposx + i, cposy + 3, cposz + k, torch);
                }
            }
        }
        layRoyalRect(cposx, cposy + 2, cposz, 3, 17, slab);
        layRoyalRect(cposx, cposy + 2, cposz, 17, 3, slab);

        // Layer 3: 13x13 + 5x33 cross (legacy line 4628-4654).
        layRoyalRect(cposx, cposy + 3, cposz, 6, 6, slab);
        layRoyalRect(cposx, cposy + 3, cposz, 2, 16, slab);
        layRoyalRect(cposx, cposy + 3, cposz, 16, 2, slab);

        // Layer 4: 5x5 cap + corner torches (legacy line 4655-4668).
        for (int i = -2; i <= 2; i++) {
            for (int k = -2; k <= 2; k++) {
                place(cposx + i, cposy + 4, cposz + k, slab);
                if (i == 2 && (k == -2 || k == 2)) {
                    place(cposx + i, cposy + 5, cposz + k, torch);
                }
                if (i == -2 && (k == -2 || k == 2)) {
                    place(cposx + i, cposy + 5, cposz + k, torch);
                }
            }
        }

        // Apex chest with the boss spawn egg in slot 13 (legacy line 4669-4674).
        if (inChunk(cposx, cposy + 4, cposz)) {
            BlockPos chestPos = new BlockPos(cposx, cposy + 4, cposz);
            // Use full update flag so the chest tile entity initialises.
            pLevel.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
            if (pLevel.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                ItemStack egg = king
                        ? new ItemStack(ModItems.THE_KING_SPAWN_EGG.get())
                        : new ItemStack(ModItems.THE_QUEEN_SPAWN_EGG.get());
                chest.setItem(13, egg);
            }
        }
    }

    private void layRoyalRect(int cx, int cy, int cz,
                              int halfX, int halfZ, BlockState slab) {
        for (int i = -halfX; i <= halfX; i++) {
            for (int k = -halfZ; k <= halfZ; k++) {
                place(cx + i, cy, cz + k, slab);
            }
        }
    }

    private void layRoyalCrossWithCorners(int cx, int cy, int cz,
                                          int halfX, int halfZ,
                                          BlockState slab, BlockState corner) {
        for (int i = -halfX; i <= halfX; i++) {
            for (int k = -halfZ; k <= halfZ; k++) {
                boolean isCorner = (i == halfX || i == -halfX) && (k == -halfZ || k == halfZ);
                place(cx + i, cy, cz + k, isCorner ? corner : slab);
            }
        }
    }

    /**
     * 33&times;33 portrait sprite data shared by the King and Queen
     * altars, copied verbatim from {@code GenericDungeon.king[]}
     * (1.7.10 line 63). The Queen variant in the legacy source uses
     * the identical array (line 64), so a single literal serves both.
     * Each {@code -1} is a row terminator; non-negative entries
     * specify a run length and toggle the palette colour.
     */
    private static final int[] ROYAL_PORTRAIT_DATA = new int[]{
            -1, -1, 24, 3, -1, 24, 5, -1, 17, 12, -1, 16, 15, -1, 15, 14, -1,
            15, 6, 3, 5, -1, 14, 6, 4, 3, -1, 14, 5, -1, 14, 5, -1, 12, 9, -1,
            11, 11, -1, 8, 17, -1, 5, 23, -1, 3, 27, -1, 2, 29, -1, 1, 31, -1,
            0, 33, -1, 13, 6, -1, 12, 9, -1, 11, 3, 1, 2, 1, 4, -1, 10, 3, 2,
            2, 3, 2, -1, 10, 2, 4, 2, 3, 2, -1, 9, 2, 5, 2, 4, 6, -1, 9, 2, 5,
            2, 6, 4, -1, 8, 2, 6, 1, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 8, 2,
            5, 2, -1, 15, 2, -1, -1, -1
    };

    // ---- Audit Part 4 — King's / Queen's Challenge Tower ---------------

    /**
     * Direct port of {@code GenericDungeon.makeEnormousCastle} (King, line
     * 191&ndash;375) and {@code makeEnormousCastleQ} (Queen, line
     * 6393&ndash;6577). The two share the same scaffolding — 28&times;28
     * base, four corner exterior spawner stacks, central Emperor Scorpion
     * column, six stacked floors with shrinking footprint
     * (26&times;10 / 26&times;10 / 24&times;9 / 24&times;9 / 22&times;8 /
     * 22&times;16), western platform-spire arm with a long descending
     * stair, and a level-6 Large Worm scatter — but with three palette
     * differences:
     * <ul>
     *   <li>King floor: {@code stone}; Queen floor: {@code obsidian}.</li>
     *   <li>King exterior corner spawners: Terrible Terror; Queen: Lurking Terror.</li>
     *   <li>King floor mob ladder: Cloud Shark / Lurking Terror / Rotator /
     *       Bee / Mantis / Mothra. Queen ladder: Rotator / Bee / Mantis /
     *       Mothra / Brutalfly / Vortex.</li>
     *   <li>King spire: {@code quartz_block} (legacy {@code field_150371_ca});
     *       Queen spire: {@link ModBlocks#BLOCK_AMETHYST}.</li>
     *   <li>King foundation skirt: {@code stone}; Queen: {@code obsidian}.</li>
     * </ul>
     *
     * <p>The legacy code rolled {@code level = 1 + nextInt(6)} with a
     * weighted reroll favouring level 4&ndash;6, then suppressed any floor
     * whose index exceeded {@code level}. We reproduce the same roll using
     * the deterministic per-piece {@link RandomSource} so the visible tower
     * shape stays seed-stable (and so the prize-floor that places the
     * Royal Guardian Sword + Prince/Princess Egg is gated on the same
     * roll the legacy expected). The final-prize chest only appears when
     * the level=6 floor's {@code addLevelDecorations(decor=1, difficulty=6)}
     * resolves {@code reward=6}, exactly as the legacy.</p>
     */
    private void generateChallengeTower(RandomSource random, boolean king) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        int width = 28;
        int height = 16;
        int platformwidth = 11;

        // QA Fix (Endgame Loot Gate): legacy line 202-205 / 6404-6406 had
        //   level = 1 + nextInt(6); if (level<=3 && nextInt(3)!=1) level += 3;
        // which only landed on level=6 about 4/18 (~22%) of the time — so
        // most Challenge Towers rolled difficulty<6, and pickDecorReward(1,
        // difficulty) returned <6, which fell through to the generic
        // chestContents fill instead of the reward==6 Royal-Guardian-Sword
        // / Royal-Armor / Prince-Egg branch in fillChallengeChests. The
        // user reported "top floor doesn't contain Royal loot" — the cause
        // was that they were looting non-level-6 towers.
        //
        // Because the King and Queen Challenge Towers are the canonical
        // endgame mega-structures (already extreme-rarity-gated by their
        // structure_set spacing), every spawn must guarantee the prize.
        // Lock difficulty at 6 so pickDecorReward(decor=1, difficulty=6)
        // always returns 6 and the bottom-floor chests always carry the
        // Royal Guardian Sword + Royal/Queen armor + Prince/Princess egg.
        int level = 6;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();
        BlockState netherFence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
        BlockState floorBlk = king ? Blocks.STONE.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState skirtBlk = king ? Blocks.STONE.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState extremeTorch = ModBlocks.EXTREME_TORCH.get().defaultBlockState();
        EntityType<?> baseCornerMob = king ? ModEntities.ENTITY_TERRIBLE_TERROR.get()
                : ModEntities.ENTITY_LURKING_TERROR.get();

        // Phase 1 — clear interior + west spire envelope (legacy line 206-212 / 6408-6414).
        for (int i = -20; i < width + 4; i++) {
            for (int j = 1; j < height + 10; j++) {
                for (int k = -4; k < width + 4; k++) {
                    place(cposx + i, cposy + j, cposz + k, air);
                }
            }
        }
        // Phase 2 — base floor (line 213-218 / 6415-6420). King = stone, Queen = obsidian.
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy, cposz + k, floorBlk);
            }
        }
        // Phase 3 — base ceiling at j=height (line 219-224 / 6421-6426). Bedrock cap.
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy + height, cposz + k, bedrock);
            }
        }
        // Phase 4 — N/S iron-bar walls (line 225-232 / 6427-6434).
        for (int i = 0; i < width; i++) {
            for (int j = 1; j < height; j++) {
                place(cposx + i, cposy + j, cposz, ironBars);
                place(cposx + i, cposy + j, cposz + width - 1, ironBars);
            }
        }
        // Phase 5 — E/W iron-bar walls (line 233-240 / 6435-6442).
        for (int k = 0; k < width; k++) {
            for (int j = 1; j < height; j++) {
                place(cposx, cposy + j, cposz + k, ironBars);
                place(cposx + width - 1, cposy + j, cposz + k, ironBars);
            }
        }
        // Phase 6 — 4 corner Extreme Torches (line 241-244 / 6443-6446).
        place(cposx + 1, cposy + 1, cposz + 1, extremeTorch);
        place(cposx + 1, cposy + 1, cposz + width - 2, extremeTorch);
        place(cposx + width - 2, cposy + 1, cposz + 1, extremeTorch);
        place(cposx + width - 2, cposy + 1, cposz + width - 2, extremeTorch);
        // Phase 7 — foundation skirt + outer fence trim (line 245-253 / 6447-6455).
        for (int i = -4; i < width + 4; i++) {
            for (int k = -4; k < width + 4; k++) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    place(cposx + i, cposy, cposz + k, skirtBlk);
                }
                if (i == -4 || k == -4 || i == width + 3 || k == width + 3) {
                    place(cposx + i, cposy + 1, cposz + k, netherFence);
                }
            }
        }
        // Phase 8 — 4 corner exterior spawner stacks j=0..3 (line 254-275 / 6456-6477).
        // King = Terrible Terror; Queen = Lurking Terror.
        for (int j = 0; j < 4; j++) {
            placeSpawner(cposx - 3, cposy + 1 + j, cposz - 3, baseCornerMob);
            placeSpawner(cposx - 3, cposy + 1 + j, cposz + width + 2, baseCornerMob);
            placeSpawner(cposx + width + 2, cposy + 1 + j, cposz - 3, baseCornerMob);
            placeSpawner(cposx + width + 2, cposy + 1 + j, cposz + width + 2, baseCornerMob);
        }
        // Phase 9 — central Emperor Scorpion column j=2,3,4 (line 276-290 / 6478-6492).
        for (int j = 2; j <= 4; j++) {
            placeSpawner(cposx + width / 2, cposy + j, cposz + width / 2,
                    ModEntities.ENTITY_EMPEROR_SCORPION.get());
        }

        // Phase 10 — stacked floors. The (cposx, cposz) corner shifts inward as
        // the floor footprint shrinks; widths and heights match legacy exactly.
        EntityType<?>[] cornerMobs = king ? kingFloorCornerMobs() : queenFloorCornerMobs();
        int j = height;
        // Floor 1 — width-2, h=10, pw=4, decor=1.
        buildChallengeFloor(king, cposx + 1, cposy + j, cposz + 1,
                width - 2, 10, 4, cornerMobs[0], 1, -1, 5, 1, level, random);
        j += 10;
        if (level >= 2) {
            buildChallengeFloor(king, cposx + 1, cposy + j, cposz + 1,
                    width - 2, 10, 4, cornerMobs[1], 0, 0, 4, 2, level, random);
        }
        j += 10;
        if (level >= 3) {
            buildChallengeFloor(king, cposx + 2, cposy + j, cposz + 2,
                    width - 4, 9, 4, cornerMobs[2], 1, 1, 4, 3, level, random);
        }
        j += 9;
        if (level >= 4) {
            buildChallengeFloor(king, cposx + 2, cposy + j, cposz + 2,
                    width - 4, 9, 3, cornerMobs[3], 0, 0, 4, 4, level, random);
        }
        j += 9;
        if (level >= 5) {
            buildChallengeFloor(king, cposx + 3, cposy + j, cposz + 3,
                    width - 6, 8, 3, cornerMobs[4], 1, 1, 4, 5, level, random);
        }
        j += 8;
        if (level >= 6) {
            buildChallengeFloor(king, cposx + 3, cposy + j, cposz + 3,
                    width - 6, 16, 3, cornerMobs[5], 0, 0, 3, 6, level, random);
        }

        // Phase 11 — western platform (line 314-321 / 6516-6523).
        BlockState spireBlk = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
        for (int i = 0; i < platformwidth; i++) {
            int yj = height;
            for (int k = -(platformwidth / 2); k <= platformwidth / 2; k++) {
                place(cposx + i - 20, cposy + yj, cposz + k + width / 2, spireBlk);
                if ((i != 0 && i != platformwidth - 1 && k != -(platformwidth / 2) && k != platformwidth / 2)
                        || (i == 0 && k >= -1 && k <= 1)) continue;
                place(cposx + i - 20, cposy + yj + 1, cposz + k + width / 2, netherFence);
            }
        }
        // Phase 12 — connector arm (line 322-339 / 6524-6541).
        for (int i = -10; i <= -3; i++) {
            int yj = height;
            for (int k = -2; k < 3; k++) {
                if (i == -3 || i == -10) {
                    if (k != -2 && k != 2) {
                        place(cposx + i, cposy + yj + 1, cposz + k + width / 2, air);
                        continue;
                    }
                    place(cposx + i, cposy + yj + 1, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + i, cposy + yj + 2, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + i, cposy + yj + 3, cposz + k + width / 2, Blocks.FIRE.defaultBlockState());
                    continue;
                }
                place(cposx + i, cposy + yj, cposz + k + width / 2, spireBlk);
                if (k != -2 && k != 2) continue;
                place(cposx + i, cposy + yj + 1, cposz + k + width / 2, netherFence);
            }
        }
        // Phase 13 — descending stair (line 340-361 / 6542-6563).
        int xi = -21;
        for (int yj = height; yj >= 0; yj--) {
            for (int k = -2; k < 3; k++) {
                for (int t = 0; t < 6; t++) {
                    place(cposx + xi, cposy + yj + t + 1, cposz + k + width / 2, air);
                }
                if (yj == 0) {
                    if (k != -2 && k != 2) {
                        place(cposx + xi, cposy + yj + 1, cposz + k + width / 2, air);
                        continue;
                    }
                    place(cposx + xi, cposy + yj + 1, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + xi, cposy + yj + 2, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + xi, cposy + yj + 3, cposz + k + width / 2, Blocks.FIRE.defaultBlockState());
                    continue;
                }
                place(cposx + xi, cposy + yj, cposz + k + width / 2, spireBlk);
                if (k != -2 && k != 2) continue;
                place(cposx + xi, cposy + yj + 1, cposz + k + width / 2, netherFence);
            }
            xi--;
        }
        // Phase 14 — level=6 Large Worm scatter (line 362-374 / 6564-6576).
        if (level >= 6) {
            int span = width * 3;
            for (int tries = 0; tries < 100; tries++) {
                int yj2 = -1;
                int xi2 = random.nextInt(span);
                int zk2 = random.nextInt(span);
                if (xi2 >= span / 4 && xi2 <= span * 3 / 4
                        && zk2 >= span / 4 && zk2 <= span * 3 / 4) continue;
                xi2 -= span / 2;
                zk2 -= span / 2;
                placeSpawner(cposx + xi2 + width / 2, cposy + yj2,
                        cposz + zk2 + width / 2, ModEntities.ENTITY_WORM_LARGE.get());
            }
        }
    }

    /**
     * Direct port of {@code GenericDungeon.buildLevel} (King, line 377&ndash;478)
     * / {@code buildLevelQ} (Queen, line 6579&ndash;6680). Each floor is a
     * bedrock-walled cube with a vein-block accent on its NS faces (gold
     * for King, ruby for Queen), a bedrock floor + ceiling, an outer
     * fence skirt, a diagonal stair connector to the floor above, a
     * 4-corner spawner column j=1..4 for the floor's exterior mob, and a
     * centred decoration room dispatched via
     * {@link #addChallengeFloorDecor}.
     */
    private void buildChallengeFloor(boolean king, int cposx, int cposy, int cposz,
                                     int width, int height, int pw, EntityType<?> critter,
                                     int stepside, int stepoff, int holelen, int decor, int level,
                                     RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState veinBlk = king ? Blocks.GOLD_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_RUBY.get().defaultBlockState();
        BlockState skirtBlk = king ? Blocks.STONE.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState fence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();

        // Phase A — hollow the floor's footprint + 1-block fence margin (line 381-387 / 6583-6589).
        for (int i = -pw; i < width + pw; i++) {
            for (int yj = 1; yj < height; yj++) {
                for (int k = -pw; k < width + pw; k++) {
                    place(cposx + i, cposy + yj, cposz + k, air);
                }
            }
        }
        // Phase B — bedrock floor (line 388-393 / 6590-6595).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy, cposz + k, bedrock);
            }
        }
        // Phase C — bedrock ceiling at y=height (line 394-399 / 6596-6601).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy + height, cposz + k, bedrock);
            }
        }
        // Phase D — N/S bedrock walls (line 400-407 / 6602-6609).
        for (int i = 0; i < width; i++) {
            for (int yj = 1; yj < height; yj++) {
                place(cposx + i, cposy + yj, cposz, bedrock);
                place(cposx + i, cposy + yj, cposz + width - 1, bedrock);
            }
        }
        // Phase E — E/W walls with vein-block accent on the corner columns
        // (line 408-419 / 6610-6621). King vein = gold; Queen vein = ruby.
        for (int k = 0; k < width; k++) {
            for (int yj = 1; yj < height; yj++) {
                BlockState blk = (k == 0 || k == width - 1) ? veinBlk : bedrock;
                place(cposx, cposy + yj, cposz + k, blk);
                place(cposx + width - 1, cposy + yj, cposz + k, blk);
            }
        }
        // Phase F — outer foundation skirt + outer fence (line 420-428 / 6622-6630).
        for (int i = -pw; i < width + pw; i++) {
            for (int k = -pw; k < width + pw; k++) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    place(cposx + i, cposy, cposz + k, skirtBlk);
                }
                if (i != -pw && k != -pw && i != width + (pw - 1) && k != width + (pw - 1)) continue;
                place(cposx + i, cposy + 1, cposz + k, fence);
            }
        }
        // Phase G — diagonal stair to the next floor (line 429-440 / 6631-6642).
        int si = -(height / 2) + width / 2;
        for (int yj = 1; yj < height; yj++) {
            int sk;
            if (stepside != 0) {
                sk = -1;
            } else {
                sk = width;
            }
            place(cposx + si, cposy + yj, cposz + sk, skirtBlk);
            si++;
        }
        // Phase H — hole through the floor for the stair to land in (line 441-454 / 6643-6656).
        if (stepoff >= 0) {
            int sk;
            if (stepside == 0) {
                sk = -1 - stepoff;
            } else {
                sk = width + stepoff;
            }
            int hi = width / 2;
            for (int l = 0; l < holelen; l++) {
                place(cposx + hi + l, cposy, cposz + sk, air);
            }
        }
        // Phase I — 4 corner spawner stacks j=1..4 with the floor's "outside" mob
        // (line 455-476 / 6657-6678). This is the wiki "Outside" gauntlet ladder.
        for (int yj = 0; yj < 4; yj++) {
            placeSpawner(cposx - (pw - 1), cposy + yj + 1, cposz - (pw - 1), critter);
            placeSpawner(cposx - (pw - 1), cposy + yj + 1, cposz + width + (pw - 2), critter);
            placeSpawner(cposx + width + (pw - 2), cposy + yj + 1, cposz - (pw - 1), critter);
            placeSpawner(cposx + width + (pw - 2), cposy + yj + 1, cposz + width + (pw - 2), critter);
        }
        // Phase J — centred decoration room (the wiki "Inside" gauntlet ladder).
        addChallengeFloorDecor(king, cposx, cposy, cposz, width, height, decor, level, random);
    }

    /**
     * Direct port of {@code GenericDungeon.addLevelDecorations} (King, line
     * 480&ndash;725) / {@code addLevelDecorationsQ} (Queen, line
     * 6682&ndash;6927). Each "decor" tier picks a different "Inside" mob
     * for the centre 1&times;1 spawner column gated by an iron-bar shaft,
     * and sets {@code reward} that drives the chest-fill table. Decor=6
     * is the Nightmare cap (4 Nightmare spawners around a central Large
     * Worm column with a dirt-filled void). The bottom floor (decor=1)
     * with difficulty=6 sets {@code reward=6}, which is what triggers the
     * Royal Guardian Sword + Prince/Princess Egg chest layout in
     * {@link #fillChallengeChests}.
     */
    private void addChallengeFloorDecor(boolean king, int cposx, int cposy, int cposz,
                                        int width, int height, int decor, int difficulty,
                                        RandomSource random) {
        int reward = 1;
        EntityType<?> critter;

        if (decor == 6) {
            // Nightmare cap (line 485-541 / 6687-6743). Same for King and Queen.
            BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
            BlockState fire = Blocks.FIRE.defaultBlockState();
            BlockState dirt = Blocks.DIRT.defaultBlockState();
            place(cposx, cposy + height, cposz, netherrack);
            place(cposx, cposy + height + 1, cposz, fire);
            place(cposx, cposy + height, cposz + width - 1, netherrack);
            place(cposx, cposy + height + 1, cposz + width - 1, fire);
            place(cposx + width - 1, cposy + height, cposz, netherrack);
            place(cposx + width - 1, cposy + height + 1, cposz, fire);
            place(cposx + width - 1, cposy + height, cposz + width - 1, netherrack);
            place(cposx + width - 1, cposy + height + 1, cposz + width - 1, fire);
            place(cposx + width / 2, cposy + height, cposz + width / 2, Blocks.AIR.defaultBlockState());
            placeSpawner(cposx + width / 2 - 1, cposy + height + 2, cposz + width / 2,
                    ModEntities.PITCH_BLACK.get());
            placeSpawner(cposx + width / 2 + 1, cposy + height + 2, cposz + width / 2,
                    ModEntities.PITCH_BLACK.get());
            placeSpawner(cposx + width / 2, cposy + height + 2, cposz + width / 2 - 1,
                    ModEntities.PITCH_BLACK.get());
            placeSpawner(cposx + width / 2, cposy + height + 2, cposz + width / 2 + 1,
                    ModEntities.PITCH_BLACK.get());
            for (int i = 1; i < width - 1; i++) {
                for (int yj = 1; yj < 5; yj++) {
                    for (int k = 1; k < width - 1; k++) {
                        place(cposx + i, cposy + yj, cposz + k, dirt);
                    }
                }
            }
            placeSpawner(cposx + width / 2, cposy + 2, cposz + width / 2,
                    ModEntities.ENTITY_WORM_LARGE.get());
            placeSpawner(cposx + width / 2, cposy + 3, cposz + width / 2,
                    ModEntities.ENTITY_WORM_LARGE.get());
            placeSpawner(cposx + width / 2, cposy + 4, cposz + width / 2,
                    ModEntities.ENTITY_WORM_LARGE.get());
            for (int yj = 0; yj < 10; yj++) {
                place(cposx + 1, cposy + yj, cposz + 1, Blocks.AIR.defaultBlockState());
            }
            // QA Traversal Fix: legacy line 537-539 carved a 1x1 air column
            // through the dirt fill at (cposx+1, j=0..9, cposz+1) but never
            // placed a climbable block — the player just fell into a 9-deep
            // pit. Stack scaffolding from j=1..9 so they can also climb
            // back out (and so the column connects to decor=5's ceiling hole
            // at (cposx_5+1, cposz_5+1) = world (cposx_6+1, cposz_6+1) since
            // both floors share cposx+3, cposz+3 — the columns line up).
            BlockState scaffolding6 = Blocks.SCAFFOLDING.defaultBlockState();
            for (int yj = 1; yj < 10; yj++) {
                place(cposx + 1, cposy + yj, cposz + 1, scaffolding6);
            }
            fillChallengeChests(king, cposx, cposy + 4, cposz, width, decor, reward, random);
            return;
        }
        // Decor 5..1 — central spawner column with iron-bar shaft + chest fill.
        // Mob ladder differs between King and Queen per legacy.
        if (king) {
            critter = pickKingDecorMob(decor, difficulty);
            reward = pickDecorReward(decor, difficulty);
        } else {
            critter = pickQueenDecorMob(decor, difficulty);
            reward = pickDecorReward(decor, difficulty);
        }
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        // Two stacked spawners j=2,3 in the centre (line 551-560 etc / 6753-6762 etc).
        placeSpawner(cposx + width / 2, cposy + 2, cposz + width / 2, critter);
        placeSpawner(cposx + width / 2, cposy + 3, cposz + width / 2, critter);
        // 4-cell bedrock shaft around the spawner column at y=1..4 (line 561-566 etc / 6763-6768 etc).
        for (int yj = 1; yj < 5; yj++) {
            place(cposx + width / 2 - 1, cposy + yj, cposz + width / 2, bedrock);
            place(cposx + width / 2 + 1, cposy + yj, cposz + width / 2, bedrock);
            place(cposx + width / 2, cposy + yj, cposz + width / 2 - 1, bedrock);
            place(cposx + width / 2, cposy + yj, cposz + width / 2 + 1, bedrock);
        }
        // Up/down ladder access cut. Exact corners alternate per decor tier
        // — match legacy parity precisely.
        //   decor=5: floor (w-2, w-2), ceiling (1, 1)   [legacy line 567-568]
        //   decor=4: floor (1, 1),     ceiling (w-2, w-2) [line 600-601]
        //   decor=3: floor (w-2, w-2), ceiling (1, 1)   [line 637-638]
        //   decor=2: floor (1, 1),     ceiling (w-2, w-2) [line 678-679]
        //   decor=1: NO floor hole; ceiling (1, 1) only [line 722]
        // The previous port collapsed decor=1 into the generic else branch,
        // which carved a bogus extra ceiling hole at (w-2, w-2) and an
        // extraneous floor hole at (1, 1) exposing the bedrock base. Match
        // the legacy exactly per-decor below.
        BlockState air = Blocks.AIR.defaultBlockState();
        int floorHoleX, floorHoleZ, ceilHoleX, ceilHoleZ;
        if (decor == 5 || decor == 3) {
            floorHoleX = width - 2; floorHoleZ = width - 2;
            ceilHoleX = 1;          ceilHoleZ = 1;
        } else if (decor == 4 || decor == 2) {
            floorHoleX = 1;         floorHoleZ = 1;
            ceilHoleX = width - 2;  ceilHoleZ = width - 2;
        } else { // decor == 1: bottom floor, no floor hole; ceiling at (1, 1)
            floorHoleX = -1; floorHoleZ = -1; // sentinel: skip floor carve
            ceilHoleX = 1;   ceilHoleZ = 1;
        }
        if (floorHoleX >= 0) {
            place(cposx + floorHoleX, cposy, cposz + floorHoleZ, air);
        }
        place(cposx + ceilHoleX, cposy + height, cposz + ceilHoleZ, air);

        // QA Traversal Fix: legacy buildLevel placed NO ladders inside the
        // bedrock-walled rooms (verified: zero references to Blocks.ladder /
        // field_150468_ap in GenericDungeon's challenge-tower code). Pure
        // 1x1 holes in the bedrock ceiling were unclimbable in survival —
        // QA flagged "completely sealed with bedrock". Drop a SCAFFOLDING
        // column at the ceiling-hole position from j=1 to j=height-1 so
        // the player can climb out. Scaffolding (vs ladders) because two
        // of the six floor pairs (decor=2↔3 and decor=4↔5) have ceiling
        // holes 2 blocks away from any wall — ladders need wall support,
        // scaffolding supports itself off the bedrock floor below.
        BlockState scaffolding = Blocks.SCAFFOLDING.defaultBlockState();
        for (int yj = 1; yj < height; yj++) {
            place(cposx + ceilHoleX, cposy + yj, cposz + ceilHoleZ, scaffolding);
        }

        // Decor 1 also lays 4 RTP teleport blocks at the central spawner base
        // (line 718-721 / 6920-6923) so the player can warp out after looting.
        if (decor == 1) {
            BlockState rtp = ModBlocks.BLOCK_TELEPORT.get().defaultBlockState();
            place(cposx + width / 2 - 1, cposy + 1, cposz + width / 2 - 1, rtp);
            place(cposx + width / 2 + 1, cposy + 1, cposz + width / 2 + 1, rtp);
            place(cposx + width / 2 + 1, cposy + 1, cposz + width / 2 - 1, rtp);
            place(cposx + width / 2 - 1, cposy + 1, cposz + width / 2 + 1, rtp);
        }
        fillChallengeChests(king, cposx, cposy, cposz, width, decor, reward, random);
    }

    /**
     * King's "Inside" mob ladder. Direct port of {@code addLevelDecorations}
     * decor 1..5 critter switches (legacy lines 484-700). The wiki's
     * "Worm &rarr; T-Rex &rarr; Basilisk &rarr; Hercules Beetle &rarr;
     * Jumpy Bug &rarr; Hammerhead &rarr; Emperor Scorpion" combined ladder
     * collapses across all six floors; the per-(decor, difficulty) lookup
     * here reproduces the exact source mapping.
     */
    private static EntityType<?> pickKingDecorMob(int decor, int difficulty) {
        // Layout matches the legacy if/else cascade exactly.
        switch (decor) {
            case 1:
                if (difficulty >= 6) return ModEntities.HAMMERHEAD.get();
                if (difficulty == 5) return ModEntities.ENTITY_SPIT_BUG.get(); // Jumpy Bug → SpitBug
                if (difficulty == 4) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 3) return ModEntities.BASILISK.get();
                if (difficulty == 2) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 2:
                if (difficulty >= 6) return ModEntities.ENTITY_SPIT_BUG.get(); // Jumpy Bug
                if (difficulty == 5) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 4) return ModEntities.BASILISK.get();
                if (difficulty == 3) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 3:
                if (difficulty >= 6) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 5) return ModEntities.BASILISK.get();
                if (difficulty == 4) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 4:
                if (difficulty >= 6) return ModEntities.BASILISK.get();
                if (difficulty == 5) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 5:
            default:
                if (difficulty >= 6) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
        }
    }

    /**
     * Queen's "Inside" mob ladder. Direct port of
     * {@code addLevelDecorationsQ} decor 1..5 critter switches (legacy
     * lines 6686-6902). Replaces the King's Alosaurus/T.Rex bottom with
     * T.Rex/Nastysaurus and tops out at CaterKiller for decor=1
     * difficulty=6 (line 6900-6902).
     */
    private static EntityType<?> pickQueenDecorMob(int decor, int difficulty) {
        switch (decor) {
            case 1:
                if (difficulty >= 6) return ModEntities.ENTITY_CATER_KILLER.get();
                if (difficulty == 5) return ModEntities.ENTITY_SPIT_BUG.get(); // Jumpy Bug
                if (difficulty == 4) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 3) return ModEntities.BASILISK.get();
                if (difficulty == 2) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 2:
                if (difficulty >= 6) return ModEntities.ENTITY_SPIT_BUG.get(); // Jumpy Bug
                if (difficulty == 5) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 4) return ModEntities.BASILISK.get();
                if (difficulty == 3) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 3:
                if (difficulty >= 6) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 5) return ModEntities.BASILISK.get();
                if (difficulty == 4) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 4:
                if (difficulty >= 6) return ModEntities.BASILISK.get();
                if (difficulty == 5) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 5:
            default:
                if (difficulty >= 6) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
        }
    }

    /**
     * Direct port of the legacy decor-vs-difficulty reward table. Each
     * decor tier shifts the difficulty&rarr;reward mapping by one so a
     * level-6 player ends with reward=6 only on decor=1 (the bottom
     * floor), matching the legacy cascade exactly. Decor=6 sets
     * reward=1 unconditionally (the Nightmare cap drops level-1 loot).
     */
    private static int pickDecorReward(int decor, int difficulty) {
        switch (decor) {
            case 1: return difficulty;          // 1..6 -> 1..6 (line 701 / 6903)
            case 2: return Math.max(1, difficulty - 1); // 2..6 -> 1..5
            case 3: return Math.max(1, difficulty - 2); // 3..6 -> 1..4
            case 4: return Math.max(1, difficulty - 3); // 4..6 -> 1..3
            case 5: return Math.max(1, difficulty - 4); // 5..6 -> 1..2
            default: return 1;
        }
    }

    /**
     * Direct port of {@code GenericDungeon.fill_chests} (King, line
     * 727&ndash;785) / {@code fill_chestsQ} (Queen, line 6929&ndash;6987).
     * Four chests at the cardinal mid-edges of the floor; when
     * {@code reward == 6} they hold the Royal Guardian Sword + Royal /
     * Queen armor set + Prince / Princess egg in the exact slot order
     * the legacy used. Otherwise a level-N {@code chestContents} fill
     * runs {@code 5 + nextInt(7)} insertions (legacy
     * {@code WeightedRandomChestContent.func_76293_a}).
     */
    private void fillChallengeChests(boolean king, int cposx, int cposy, int cposz,
                                     int width, int decor, int reward, RandomSource random) {
        // West chest (line 743-752 / 6945-6954). reward=6 -> Prince/Princess Egg in slot 1.
        placeChest(cposx + 1, cposy + 1, cposz + width / 2, (chest, rng) -> {
            if (reward == 6) {
                chest.setItem(1, new ItemStack(king
                        ? ModItems.PRINCE_EGG.get() : ModItems.PRINCESS_EGG.get()));
            } else {
                fillChallengeContents(chest, reward, rng);
            }
        }, random);
        // East chest (line 753-763 / 6955-6965). reward=6 -> Royal Helmet + Chestplate.
        placeChest(cposx + width - 2, cposy + 1, cposz + width / 2, (chest, rng) -> {
            if (reward == 6) {
                chest.setItem(1, new ItemStack(king
                        ? ModItems.ROYAL_HELMET.get() : ModItems.QUEEN_HELMET.get()));
                chest.setItem(2, new ItemStack(king
                        ? ModItems.ROYAL_CHESTPLATE.get() : ModItems.QUEEN_CHESTPLATE.get()));
            } else {
                fillChallengeContents(chest, reward, rng);
            }
        }, random);
        // North chest (line 764-774 / 6966-6976). reward=6 -> Royal Leggings + Boots.
        placeChest(cposx + width / 2, cposy + 1, cposz + 1, (chest, rng) -> {
            if (reward == 6) {
                chest.setItem(1, new ItemStack(king
                        ? ModItems.ROYAL_LEGGINGS.get() : ModItems.QUEEN_LEGGINGS.get()));
                chest.setItem(2, new ItemStack(king
                        ? ModItems.ROYAL_BOOTS.get() : ModItems.QUEEN_BOOTS.get()));
            } else {
                fillChallengeContents(chest, reward, rng);
            }
        }, random);
        // South chest (line 775-784 / 6977-6986). reward=6 -> Royal Guardian Sword.
        placeChest(cposx + width / 2, cposy + 1, cposz + width - 2, (chest, rng) -> {
            if (reward == 6) {
                chest.setItem(1, new ItemStack(ModItems.ROYAL_GUARDIAN_SWORD.get()));
            } else {
                fillChallengeContents(chest, reward, rng);
            }
        }, random);
    }

    /**
     * Authentic per-tier weighted chest fill. The legacy
     * {@code level1ContentsList}&hellip;{@code level5ContentsList}
     * (legacy lines 57&ndash;61) reference dozens of OreSpawn items;
     * each tier here ports the items that have a 1.21.1 counterpart and
     * preserves the original entry weight by duplication. Entry counts
     * keep the same {@code 5 + nextInt(7)} insertion budget the legacy
     * {@code WeightedRandomChestContent.func_76293_a} used.
     */
    private static void fillChallengeContents(ChestBlockEntity chest, int reward, RandomSource random) {
        ItemStack[] palette = switch (reward) {
            case 5 -> new ItemStack[]{
                    new ItemStack(ModItems.NIGHTMARE_SWORD.get(), 1),
                    new ItemStack(ModItems.POISON_SWORD.get(), 1),
                    new ItemStack(Items.WITHER_SKELETON_SPAWN_EGG, 1 + random.nextInt(4)),
                    new ItemStack(Items.IRON_GOLEM_SPAWN_EGG, 1 + random.nextInt(4)),
                    new ItemStack(ModItems.MOTHRA_SPAWN_EGG.get(), 1 + random.nextInt(4)),
                    new ItemStack(Items.NETHERITE_INGOT, 1 + random.nextInt(2)),
                    new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1)
            };
            case 4 -> new ItemStack[]{
                    new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(7)),
                    new ItemStack(ModItems.MAGIC_APPLE.get(), 1),
                    new ItemStack(ModBlocks.CREEPER_REPELLENT.get(), 4 + random.nextInt(7)),
                    new ItemStack(ModBlocks.KRAKEN_REPELLENT.get(), 4 + random.nextInt(7)),
                    new ItemStack(ModItems.RUBY_PICKAXE.get(), 1),
                    new ItemStack(ModItems.RUBY_SWORD.get(), 1),
                    new ItemStack(ModItems.RUBY_HELMET.get(), 1),
                    new ItemStack(ModItems.RUBY_CHESTPLATE.get(), 1),
                    new ItemStack(ModItems.RUBY_LEGGINGS.get(), 1),
                    new ItemStack(ModItems.RUBY_BOOTS_ARMOR.get(), 1)
            };
            case 3 -> new ItemStack[]{
                    new ItemStack(ModItems.RAT_SWORD.get(), 1),
                    new ItemStack(Items.AMETHYST_SHARD, 2 + random.nextInt(7)),
                    new ItemStack(Items.LAPIS_LAZULI, 2 + random.nextInt(7)),
                    new ItemStack(ModItems.TIGERSEYE_HELMET.get(), 1),
                    new ItemStack(ModItems.TIGERSEYE_CHESTPLATE.get(), 1),
                    new ItemStack(ModItems.TIGERSEYE_LEGGINGS.get(), 1),
                    new ItemStack(ModItems.TIGERSEYE_BOOTS.get(), 1),
                    new ItemStack(ModItems.AMETHYST_SWORD.get(), 1),
                    new ItemStack(ModItems.AMETHYST_PICKAXE.get(), 1)
            };
            case 2 -> new ItemStack[]{
                    new ItemStack(Items.ENDER_PEARL, 2 + random.nextInt(7)),
                    new ItemStack(Items.ENDER_PEARL, 2 + random.nextInt(7)),
                    new ItemStack(ModItems.PINK_HELMET.get(), 1),
                    new ItemStack(ModItems.PINK_CHESTPLATE.get(), 1),
                    new ItemStack(ModItems.PINK_LEGGINGS.get(), 1),
                    new ItemStack(ModItems.PINK_BOOTS.get(), 1),
                    new ItemStack(ModItems.FAIRY_SWORD.get(), 1),
                    new ItemStack(ModItems.EMERALD_PICKAXE.get(), 1),
                    new ItemStack(ModItems.EMERALD_SWORD.get(), 1)
            };
            default -> new ItemStack[]{
                    new ItemStack(Items.EMERALD, 2 + random.nextInt(7)),
                    new ItemStack(ModItems.MINERS_DREAM.get(), 4 + random.nextInt(5)),
                    new ItemStack(ModItems.EMERALD_PICKAXE.get(), 1),
                    new ItemStack(ModItems.EMERALD_SWORD.get(), 1),
                    new ItemStack(ModItems.EMERALD_HELMET.get(), 1),
                    new ItemStack(ModItems.EMERALD_CHESTPLATE.get(), 1),
                    new ItemStack(ModItems.EMERALD_LEGGINGS.get(), 1),
                    new ItemStack(ModItems.EMERALD_BOOTS_ARMOR.get(), 1)
            };
        };
        int slots = chest.getContainerSize();
        int count = 5 + random.nextInt(7);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    /**
     * King's per-floor "Outside" corner-spawner ladder. Direct port of
     * the {@code makeEnormousCastle.buildLevel(critter=...)} string
     * arguments at legacy lines 292 / 295 / 299 / 303 / 307 / 311.
     *
     * <pre>
     *   Floor 1 (decor=1): "Cloud Shark"     -&gt; CLOUD_SHARK
     *   Floor 2 (decor=2): "Lurking Terror"  -&gt; ENTITY_LURKING_TERROR
     *   Floor 3 (decor=3): "Rotator"         -&gt; ENTITY_ROTATOR
     *   Floor 4 (decor=4): "Bee"             -&gt; ENTITY_BEE
     *   Floor 5 (decor=5): "Mantis"          -&gt; ENTITY_MANTIS
     *   Floor 6 (decor=6): "Mothra"          -&gt; MOTHRA
     * </pre>
     *
     * <p>Resolved at call-time (i.e. inside {@code postProcess}) so the
     * NeoForge {@code DeferredHolder} registry-attach has guaranteed to
     * have run; lazy-resolving statically would NPE if this class loaded
     * before {@code FMLClientSetupEvent} fired.</p>
     */
    private static EntityType<?>[] kingFloorCornerMobs() {
        return new EntityType<?>[]{
                ModEntities.CLOUD_SHARK.get(),
                ModEntities.ENTITY_LURKING_TERROR.get(),
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_BEE.get(),
                ModEntities.ENTITY_MANTIS.get(),
                ModEntities.MOTHRA.get()
        };
    }

    /**
     * Queen's per-floor "Outside" corner-spawner ladder. Direct port of
     * the {@code makeEnormousCastleQ.buildLevelQ(critter=...)} string
     * arguments at legacy lines 6494 / 6497 / 6501 / 6505 / 6509 / 6513.
     *
     * <pre>
     *   Floor 1: "Rotator"    -&gt; ENTITY_ROTATOR
     *   Floor 2: "Bee"        -&gt; ENTITY_BEE
     *   Floor 3: "Mantis"     -&gt; ENTITY_MANTIS
     *   Floor 4: "Mothra"     -&gt; MOTHRA
     *   Floor 5: "Brutalfly"  -&gt; ENTITY_BRUTALFLY
     *   Floor 6: "Vortex"     -&gt; ENTITY_VORTEX
     * </pre>
     */
    private static EntityType<?>[] queenFloorCornerMobs() {
        return new EntityType<?>[]{
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_BEE.get(),
                ModEntities.ENTITY_MANTIS.get(),
                ModEntities.MOTHRA.get(),
                ModEntities.ENTITY_BRUTALFLY.get(),
                ModEntities.ENTITY_VORTEX.get()
        };
    }
}
