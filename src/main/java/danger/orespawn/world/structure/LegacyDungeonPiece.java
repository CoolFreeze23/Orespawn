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
        WHITE_HOUSE(48, 2, 25);

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
}
