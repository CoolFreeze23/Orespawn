package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;

/**
 * One Utopia tree as a structure piece (WGEN-072, WGEN-073, WGEN-074): the Sky tree and the Wind tree of
 * {@code Trees.java} and the big square, circular and round trees of {@code ItemMagicApple.java}, each transcribed
 * from the 1.7.10 source with the line cited at every step, written chunk by chunk.
 *
 * <p><b>Why a piece.</b> A feature may write only into the chunk being decorated and its eight neighbours; every one
 * of these trees reaches farther, and the feature ports shipped with the branches sheared off at that boundary. A
 * structure piece's {@link #postProcess} runs once for every chunk its bounding box touches, so the whole algorithm
 * runs on every pass and each write lands only when its cell lies in the pass's chunk ({@link #place}); the slices
 * stitch into one tree. That contract needs every pass to walk the same path: the tree's own draws come from a
 * {@link RandomSource} seeded from the piece ({@link #seed}, drawn once when the structure was placed), and a draw
 * never depends on a terrain read. Where the original read the world before drawing (the square tree's floor chest,
 * the branch leaves' vines and gem substitutions, the branch chests' item counts) the draw is made regardless and
 * only the write is gated on the read; the golem's yaw, which the original drew from the world's random, comes from
 * the cell instead. Terrain reads themselves are answered as "writable" outside the pass's chunk, where the write is
 * dropped anyway, and from the real block inside it, so a read only ever decides the cell it is about to write.
 *
 * <p><b>What the reads mean.</b> {@code isBoringBlock} (orig ItemMagicApple.java:70-103) is air, tall grass, cactus,
 * the flowers, leaves, a snow layer, the strawberry plant or apple leaves; {@code isBoringBaseBlock} (:105-117) is
 * anything but stone and bedrock (stone read as the STONES tag, so the 1.21.1 terrain's deepslate and the stone
 * variants stop a foundation walk as stone did). The original's {@code y - j <= 0} floor is the level's minimum
 * build height. Leaves are placed persistent (they never decay) as every feature port of these trees did: the
 * original's worldgen leaves decayed only after a neighbour change and a failed log search, and a canopy this far
 * from its logs would otherwise vanish on the first random tick.
 *
 * <p><b>Chests and critters.</b> The square and circular trees' chests (orig :161-167, :394-398, :608-613) carry
 * the original's forty-entry list as the loot tables {@code chests/utopia_huge_tree_branch} (1 + nextInt(8) items,
 * :166), {@code chests/utopia_huge_tree_floor} (the square tree's floors, t_radius - tier + nextInt(10) items at :398,
 * 0-13 as one range) and {@code chests/utopia_huge_tree_disc} (the circular tree's discs, t_radius - (int)rad +
 * nextInt(10) at :612 under rad > 3, 0-12 as one range); the
 * critters (orig :169-171, {@code spawnCreature(world, 99, ...)}: entity id 99 is the Iron Golem) stand on the
 * branches of the one tree in four that carries them.
 */
public class UtopiaTreePiece extends StructurePiece {

    public enum Kind { SKY, WIND, ROUND, SQUARE, CIRCULAR }

    public static final ResourceKey<LootTable> BRANCH_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chests/utopia_huge_tree_branch"));
    public static final ResourceKey<LootTable> FLOOR_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chests/utopia_huge_tree_floor"));
    public static final ResourceKey<LootTable> DISC_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "chests/utopia_huge_tree_disc"));

    /** {@code Block.UPDATE_CLIENTS}: no neighbour cascade, no lighting recompute (as RoyalTreePiece). */
    private static final int FLAG_CLIENTS_ONLY = 2;
    /** The original's foundation walk goes down up to 20 blocks (orig :263, :548, :642). */
    private static final int FOUNDATION = 20;
    /** The square tree's permit: RoyalTreePiece's measured reach for the same algorithm at t_radius 6 (its ±144 / +172). */
    private static final int SQUARE_H = 144, SQUARE_UP = 172;
    /** The circular tree: rings to 6, branches 5 x 6 + 7 out of the ring, their width 7, the (int) rounding. */
    private static final int CIRCULAR_H = 56, CIRCULAR_UP = 172;
    /** The round tree: branch discs of half the branch length, the branch up to 6 x 5 + 7 out of a ring of 6. */
    private static final int ROUND_H = 48, ROUND_UP = 172;
    /**
     * The round and circular trees rise a ring per step while the radius survives {@code rad -= 0.01 * nextInt(15)};
     * about 86 steps for a radius of 6. A run of zero draws long enough to matter has no realistic probability; the
     * cap is a safety valve that the trees never reach. Not in the original; declared in WGEN-074.
     */
    private static final int RING_STEP_CAP = 512;

    private final BlockPos origin;
    private final Kind kind;
    private final long seed;
    /** Per kind: SKY top / width / drop; WIND height / dir; ROUND radius / wood; SQUARE and CIRCULAR radius / wood / apple leaves / no critters. */
    private final int a, b, c, d;

    /** orig Trees.java:96-119 SkyTree at a grass block: the trunk's top {@code top} (absolute), the canopy half-width, the lower ring's extra drop below {@code top - 5}. */
    public static UtopiaTreePiece sky(BlockPos origin, int top, int width, int drop) {
        return new UtopiaTreePiece(Kind.SKY, origin, 0L, top, width, drop, 0);
    }

    /** orig Trees.java:45-77 WindTree at a grass block: {@code height} rows, leaning in {@code dir} (0 +x, 1 -x, 2 +z, 3 -z). */
    public static UtopiaTreePiece wind(BlockPos origin, int height, int dir) {
        return new UtopiaTreePiece(Kind.WIND, origin, 0L, height, dir, 0, 0);
    }

    /** orig ItemMagicApple.java:624-694 MakeBigRoundTree at a grass block, {@code treeType} the wood (0 oak, 1 spruce, 2 birch, 3 jungle). */
    public static UtopiaTreePiece round(BlockPos origin, long seed, int radius, int treeType) {
        return new UtopiaTreePiece(Kind.ROUND, origin, seed, radius, treeType, 0, 0);
    }

    /** orig ItemMagicApple.java:248-469 MakeBigSquareTree at a grass block, oak-family wood and mossy cobblestone steps as addHugeTree passed them. */
    public static UtopiaTreePiece square(BlockPos origin, long seed, int radius, int treeType, boolean appleLeaves, boolean noCritters) {
        return new UtopiaTreePiece(Kind.SQUARE, origin, seed, radius, treeType, appleLeaves ? 1 : 0, noCritters ? 1 : 0);
    }

    /** orig ItemMagicApple.java:533-623 MakeBigCircularTree at a grass block. */
    public static UtopiaTreePiece circular(BlockPos origin, long seed, int radius, int treeType, boolean noCritters) {
        return new UtopiaTreePiece(Kind.CIRCULAR, origin, seed, radius, treeType, 0, noCritters ? 1 : 0);
    }

    private UtopiaTreePiece(Kind kind, BlockPos origin, long seed, int a, int b, int c, int d) {
        super(ModStructureTypes.UTOPIA_TREE_PIECE.get(), 0, permit(kind, origin, a, b));
        this.kind = kind;
        this.origin = origin.immutable();
        this.seed = seed;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public UtopiaTreePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructureTypes.UTOPIA_TREE_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
        this.kind = Kind.values()[tag.getInt("k")];
        this.seed = tag.getLong("s");
        this.a = tag.getInt("a");
        this.b = tag.getInt("b");
        this.c = tag.getInt("c");
        this.d = tag.getInt("d");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("ox", origin.getX());
        tag.putInt("oy", origin.getY());
        tag.putInt("oz", origin.getZ());
        tag.putInt("k", kind.ordinal());
        tag.putLong("s", seed);
        tag.putInt("a", a);
        tag.putInt("b", b);
        tag.putInt("c", c);
        tag.putInt("d", d);
    }

    public BlockPos origin() {
        return origin;
    }

    public Kind kind() {
        return kind;
    }

    /** The piece's permit: every chunk it touches gets a pass, so it must hold every cell the tree can reach. */
    private static BoundingBox permit(Kind kind, BlockPos o, int a, int b) {
        int ox = o.getX(), oy = o.getY(), oz = o.getZ();
        switch (kind) {
            case SKY: {
                int w = b + 1;
                return new BoundingBox(ox - w, oy, oz - w, ox + w, a + 2, oz + w);
            }
            case WIND: {
                int reach = a + 2;
                int minX = ox - 1, maxX = ox + 1, minZ = oz - 1, maxZ = oz + 1;
                if (b == 0) maxX = ox + reach;
                else if (b == 1) minX = ox - reach;
                else if (b == 2) maxZ = oz + reach;
                else minZ = oz - reach;
                return new BoundingBox(minX, oy, minZ, maxX, oy + a + 2, maxZ);
            }
            case ROUND:
                return new BoundingBox(ox - ROUND_H, oy - FOUNDATION, oz - ROUND_H, ox + ROUND_H, oy + ROUND_UP, oz + ROUND_H);
            case SQUARE:
                return new BoundingBox(ox - SQUARE_H, oy - FOUNDATION, oz - SQUARE_H, ox + SQUARE_H, oy + SQUARE_UP, oz + SQUARE_H);
            default:
                return new BoundingBox(ox - CIRCULAR_H, oy - FOUNDATION, oz - CIRCULAR_H, ox + CIRCULAR_H, oy + CIRCULAR_UP, oz + CIRCULAR_H);
        }
    }

    // ---- The per-pass context (BUG-033: passes of one piece run concurrently; each keeps its own) ----------------

    private record PassCtx(WorldGenLevel level, BlockPos.MutableBlockPos mut, int minY, int maxY,
                           int cbMinX, int cbMaxX, int cbMinY, int cbMaxY, int cbMinZ, int cbMaxZ) {}

    private final transient ThreadLocal<PassCtx> passCtx = new ThreadLocal<>();

    private PassCtx ctx() {
        return passCtx.get();
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        passCtx.set(new PassCtx(level, new BlockPos.MutableBlockPos(),
                level.getMinBuildHeight(), level.getMaxBuildHeight(),
                chunkBox.minX(), chunkBox.maxX(), chunkBox.minY(), chunkBox.maxY(), chunkBox.minZ(), chunkBox.maxZ()));
        try {
            switch (kind) {
                case SKY -> skyTree();
                case WIND -> windTree();
                case ROUND -> bigRoundTree(RandomSource.create(seed));
                case SQUARE -> bigSquareTree(RandomSource.create(seed));
                case CIRCULAR -> bigCircularTree(RandomSource.create(seed));
            }
        } finally {
            passCtx.remove();
        }
    }

    /** True iff the cell is inside the pass's chunk window. */
    private boolean inChunk(int x, int y, int z) {
        PassCtx c = ctx();
        return x >= c.cbMinX() && x <= c.cbMaxX()
                && y >= c.cbMinY() && y <= c.cbMaxY()
                && z >= c.cbMinZ() && z <= c.cbMaxZ();
    }

    /** The gated write: the algorithm runs whole on every pass; only the cells of this pass's chunk land. */
    private void place(int x, int y, int z, BlockState state) {
        PassCtx c = ctx();
        if (y < c.minY() || y >= c.maxY()) return;
        if (!inChunk(x, y, z)) return;
        c.mut().set(x, y, z);
        c.level().setBlock(c.mut(), state, FLAG_CLIENTS_ONLY);
    }

    /** The block at a cell of this pass's chunk; {@code null} outside it (where no write can land). */
    private BlockState read(int x, int y, int z) {
        PassCtx c = ctx();
        if (y < c.minY() || y >= c.maxY()) return null;
        if (!inChunk(x, y, z)) return null;
        c.mut().set(x, y, z);
        return c.level().getBlockState(c.mut());
    }

    /** orig {@code Blocks.air == world.getBlock(...)}. */
    private boolean isAirAt(int x, int y, int z) {
        BlockState s = read(x, y, z);
        return s == null || s.isAir();
    }

    /** orig ItemMagicApple.java:70-103 isBoringBlock. */
    private boolean isBoring(int x, int y, int z) {
        BlockState s = read(x, y, z);
        if (s == null || s.isAir()) return true;
        return s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.CACTUS)
                || s.is(BlockTags.SMALL_FLOWERS) || s.is(BlockTags.LEAVES) || s.is(Blocks.SNOW)
                || s.is(ModBlocks.STRAWBERRY_PLANT.get()) || s.is(ModBlocks.APPLE_LEAVES.get());
    }

    /** orig ItemMagicApple.java:105-117 isBoringBaseBlock: air, or anything but stone and bedrock. */
    private boolean isBoringBase(int x, int y, int z) {
        BlockState s = read(x, y, z);
        if (s == null || s.isAir()) return true;
        return !s.is(Tags.Blocks.STONES) && !s.is(Blocks.BEDROCK);
    }

    private static BlockState log(int treeType) {
        return switch (treeType) {
            case 1 -> Blocks.SPRUCE_LOG.defaultBlockState();
            case 2 -> Blocks.BIRCH_LOG.defaultBlockState();
            case 3 -> Blocks.JUNGLE_LOG.defaultBlockState();
            default -> Blocks.OAK_LOG.defaultBlockState();
        };
    }

    private static BlockState leaves(int treeType) {
        BlockState s = switch (treeType) {
            case 1 -> Blocks.SPRUCE_LEAVES.defaultBlockState();
            case 2 -> Blocks.BIRCH_LEAVES.defaultBlockState();
            case 3 -> Blocks.JUNGLE_LEAVES.defaultBlockState();
            default -> Blocks.OAK_LEAVES.defaultBlockState();
        };
        return persistent(s);
    }

    /** Leaves that never decay (see the class note). */
    static BlockState persistent(BlockState s) {
        if (s.hasProperty(LeavesBlock.PERSISTENT)) s = s.setValue(LeavesBlock.PERSISTENT, true);
        if (s.hasProperty(LeavesBlock.DISTANCE)) s = s.setValue(LeavesBlock.DISTANCE, 1);
        return s;
    }

    /** A chest with a loot table at the cell (orig: a chest block filled from chestContentsList), in this pass's chunk only. */
    private void lootChest(int x, int y, int z, ResourceKey<LootTable> table) {
        if (!inChunk(x, y, z)) return;
        PassCtx c = ctx();
        if (y < c.minY() || y >= c.maxY()) return;
        BlockPos pos = new BlockPos(x, y, z);
        c.level().setBlock(pos, Blocks.CHEST.defaultBlockState(), FLAG_CLIENTS_ONLY);
        if (c.level().getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(table);
        }
    }

    /**
     * orig ItemMagicApple.java:48-56 spawnCreature(world, 99, x, y, z): entity id 99 is the Iron Golem; placed with a
     * random yaw, then its living sound. The yaw comes from the cell so that every chunk pass draws alike.
     */
    private void ironGolem(double x, double y, double z) {
        int bx = Mth.floor(x), by = Mth.floor(y), bz = Mth.floor(z);
        if (!inChunk(bx, by, bz)) return;
        WorldGenLevel level = ctx().level();
        IronGolem golem = EntityType.IRON_GOLEM.create(level.getLevel());
        if (golem == null) return;
        float yaw = (float) Math.floorMod(bx * 31L + bz * 17L + by * 7L, 360L);
        golem.moveTo(x, y, z, yaw, 0.0f);
        level.addFreshEntityWithPassengers(golem);
        golem.playAmbientSound();
    }

    // ---- Trees.java: the Sky tree --------------------------------------------------------------------------------

    /** orig Trees.java:96-119 SkyTree(world, x, y, z) at the grass block (x, y, z). */
    private void skyTree() {
        int x = origin.getX(), y = origin.getY(), z = origin.getZ();
        int top = a, width = b, drop = c;
        BlockState skyLog = ModBlocks.SKY_TREE_LOG.get().defaultBlockState();
        BlockState leaves = persistent(Blocks.OAK_LEAVES.defaultBlockState());
        for (int j = y; j <= top; j++) place(x, j, z, skyLog);                       // :106-108
        place(x, top + 1, z, leaves);                                                  // :109
        skyTreeBranch(x, top, z, width, 1, 0, skyLog, leaves);                        // :110
        skyTreeBranch(x, top, z, width, -1, 0, skyLog, leaves);                       // :111
        skyTreeBranch(x, top, z, width, 0, 1, skyLog, leaves);                        // :112
        skyTreeBranch(x, top, z, width, 0, -1, skyLog, leaves);                       // :113
        int lower = top - 5 - drop;                                                    // :114-115
        int lowerWidth = width / 3;                                                    // :115
        skyTreeBranch(x, lower, z, lowerWidth, 1, 0, skyLog, leaves);                 // :115
        skyTreeBranch(x, lower, z, lowerWidth, -1, 0, skyLog, leaves);                // :116
        skyTreeBranch(x, lower, z, lowerWidth, 0, 1, skyLog, leaves);                 // :117
        skyTreeBranch(x, lower, z, lowerWidth, 0, -1, skyLog, leaves);                // :118
    }

    /** orig Trees.java:79-94 SkyTreeBranch: {@code length - 1} logs out along the cardinal, leaves above and to both sides where air, one leaf at the tip. */
    private void skyTreeBranch(int x, int y, int z, int length, int dirx, int dirz, BlockState skyLog, BlockState leaves) {
        for (int i = 1; i < length; i++) {                                             // :80
            int bx = x + i * dirx, bz = z + i * dirz;
            place(bx, y, bz, skyLog);                                                  // :81
            if (isAirAt(bx, y + 1, bz)) place(bx, y + 1, bz, leaves);                  // :82-84
            if (isAirAt(bx + dirz, y, bz + dirx)) place(bx + dirz, y, bz + dirx, leaves);   // :85-87
            if (isAirAt(bx - dirz, y, bz - dirx)) place(bx - dirz, y, bz - dirx, leaves);   // :88-89
        }
        int tx = x + length * dirx, tz = z + length * dirz;
        if (isAirAt(tx, y, tz)) place(tx, y, tz, leaves);                               // :91-93
    }

    // ---- Trees.java: the Wind tree -------------------------------------------------------------------------------

    /** orig Trees.java:45-77 WindTree(world, x, y, z, dir) at the grass block (x, y, z). */
    private void windTree() {
        int x = origin.getX(), y = origin.getY(), z = origin.getZ();
        int height = a, dir = b;
        int dirx = 1, dirz = 0;                                                        // :50-51
        if (dir == 1) { dirx = -1; dirz = 0; }                                         // :52-55
        if (dir == 2) { dirx = 0; dirz = 1; }                                          // :56-59
        if (dir == 3) { dirx = 0; dirz = -1; }                                         // :60-63
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = persistent(Blocks.OAK_LEAVES.defaultBlockState());
        for (int j = 0; j < height; j++) {                                             // :68
            place(x, j + y, z, log);                                                   // :69
            if (j <= height / 5) continue;                                             // :70
            place(x + dirx, j + y, z + dirz, leaves);                                  // :71
            if (j <= height / 4 || j % 4 != 0) continue;                               // :72
            windTreeBranch(x, j + y, z, height - j, dirx, dirz, log, leaves);          // :73
        }
        place(x, y + height, z, leaves);                                               // :76
    }

    /** orig Trees.java:21-43 WindTreeBranch: {@code length} logs out along the lean, leaves above (two high on the inner third) and, past the inner third, to both sides where air; two leaves at the tip. */
    private void windTreeBranch(int x, int y, int z, int length, int dirx, int dirz, BlockState log, BlockState leaves) {
        for (int i = 1; i <= length; i++) {                                            // :22
            int bx = x + i * dirx, bz = z + i * dirz;
            place(bx, y, bz, log);                                                     // :23
            if (isAirAt(bx, y + 1, bz)) place(bx, y + 1, bz, leaves);                  // :24-26
            if (i < length / 3 && isAirAt(bx, y + 2, bz)) place(bx, y + 2, bz, leaves);   // :27-29
            if (i <= length / 3) continue;                                             // :30
            if (isAirAt(bx + dirz, y, bz + dirx)) place(bx + dirz, y, bz + dirx, leaves);   // :31-33
            if (isAirAt(bx - dirz, y, bz - dirx)) place(bx - dirz, y, bz - dirx, leaves);   // :34-35
        }
        int t1x = x + (length + 1) * dirx, t1z = z + (length + 1) * dirz;
        if (isAirAt(t1x, y, t1z)) place(t1x, y, t1z, leaves);                            // :37-39
        int t2x = x + (length + 2) * dirx, t2z = z + (length + 2) * dirz;
        if (isAirAt(t2x, y, t2z)) place(t2x, y, t2z, leaves);                            // :40-42
    }

    // ---- ItemMagicApple.java: the big round tree -----------------------------------------------------------------

    /**
     * orig ItemMagicApple.java:624-694 MakeBigRoundTree. The height counter advances once per ring before the radius
     * draw (the class file's {@code iinc} at the loop's end; CFR renders it as {@code y + ++cury} inside the diamond
     * test), so the rings climb and the diamond caps the column when the radius runs out.
     */
    private void bigRoundTree(RandomSource rand) {
        int inx = origin.getX(), y = origin.getY(), inz = origin.getZ();
        int tRadius = a;
        BlockState id = log(b), leafId = leaves(b);
        BlockState diamond = Blocks.DIAMOND_BLOCK.defaultBlockState();
        double rad = tRadius;                                                          // :629
        int cury;                                                                      // :630
        int ibranch = 0;                                                               // :631
        float fx = inx;                                                                // :632
        fx += 0.5f;                                                                    // :633
        float fz = inz;                                                                // :634
        fz += 0.5f;                                                                    // :635
        cury = y;                                                                      // :636
        for (int i = 0; i < 360; i++) {                                                // :637
            double dt = rad * Math.sin(Math.toRadians(i));                             // :638
            float fcurx = (float) dt;                                                  // :639
            float fcurz = (float) (rad * Math.cos(Math.toRadians(i)));                 // :640
            int cx = (int) (fx + fcurx), cz = (int) (fz + fcurz);
            if (!isBoringBase(cx, cury, cz)) continue;                                 // :641
            for (int j = 0; j < FOUNDATION; j++) {                                     // :642
                if (cury - j <= ctx().minY()) continue;                                // :643
                if (!isBoringBase(cx, cury - j, cz)) break;                            // :644
                place(cx, cury - j, cz, id);                                           // :645-649
            }
        }
        cury = 1;                                                                      // :652
        int steps = 0;
        while (rad > 0.0) {                                                            // :653
            for (int i = 0; i < 360; i++) {                                            // :654
                double dt = rad * Math.sin(Math.toRadians(i));                         // :655
                float fcurx = (float) dt;                                              // :656
                float fcurz = (float) (rad * Math.cos(Math.toRadians(i)));             // :657
                int cx = (int) (fx + fcurx), cz = (int) (fz + fcurz);
                if (!isBoringBase(cx, y + cury, cz)) continue;                         // :658
                place(cx, y + cury, cz, id);                                           // :659-663
            }
            if (cury > (int) rad) {                                                    // :665
                ibranch += 80 + rand.nextInt(80);                                      // :666
                if (ibranch > 360) ibranch -= 360;                                     // :667
                int ibranchlen = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);      // :669
                float fcurx = (float) (rad * Math.sin(Math.toRadians(ibranch)));       // :670-671
                float fcurz = (float) (rad * Math.cos(Math.toRadians(ibranch)));       // :672-673
                roundBranch(ibranch, ibranchlen, fx + fcurx, y + cury, fz + fcurz, id, leafId);   // :674
            }
            if (cury % 6 == 0 && rad > 3.0) {                                          // :676
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {                   // :677
                    for (int i = 0; i < 360; i++) {                                    // :678
                        double dt = dr * Math.sin(Math.toRadians(i));                  // :679
                        float fcurx = (float) dt;                                      // :680
                        float fcurz = (float) (dr * Math.cos(Math.toRadians(i)));      // :681
                        int cx = (int) (fx + fcurx), cz = (int) (fz + fcurz);
                        if (!isBoringBase(cx, y + cury, cz)) continue;                 // :682
                        place(cx, y + cury, cz, id);                                   // :683-687
                    }
                }
            }
            ++cury;                                                                    // the class file's iinc
            rad -= 0.01 * rand.nextInt(15);                                            // :691
            if (rad <= 0.0 && isBoringBase((int) fx, y + cury, (int) fz)) {            // :691
                place((int) fx, y + cury, (int) fz, diamond);                          // :692
            }
            if (++steps > RING_STEP_CAP) break;
        }
    }

    /**
     * orig ItemMagicApple.java:696-722 MakeRoundBranch: a filled disc, its centre half the branch length out along
     * the branch angle, the outer two blocks of its radius leaves; a cell is written once per sweep step change.
     */
    private void roundBranch(int iangle, int branchlen, float startx, int starty, float startz, BlockState id, BlockState leafId) {
        double deltadir = 0.06283185200000001;                                         // :697
        double deltamag = 0.35f;                                                       // :698
        int ixlast = 0, izlast = 0;                                                    // :699-700
        int radius = branchlen / 2;                                                    // :703
        float centerx = (float) ((double) startx + (double) radius * Math.sin(Math.toRadians(iangle)));   // :704
        float centerz = (float) ((double) startz + (double) radius * Math.cos(Math.toRadians(iangle)));   // :705
        for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {     // :708
            for (double h = 0.75; h < (double) radius; h += deltamag) {                // :709
                int ix = (int) ((double) centerx + Math.cos(curdir) * h);              // :710
                int iz = (int) ((double) centerz + Math.sin(curdir) * h);              // :711
                if (ix == ixlast && iz == izlast) continue;                            // :712
                ixlast = ix;                                                           // :713
                izlast = iz;                                                           // :714
                BlockState cell = id;                                                  // :715
                if ((double) radius - h < 2.0) cell = leafId;                          // :716-718
                if (!isBoring(ix, starty, iz)) continue;                               // :719
                place(ix, starty, iz, cell);                                           // :720
            }
        }
    }

    // ---- ItemMagicApple.java: the big square tree ----------------------------------------------------------------

    /**
     * orig ItemMagicApple.java:248-469 MakeBigSquareTree(world, x, y, z, ID, leafID, stepID, tree_type, t_radius,
     * bad_critters, chunk), as addHugeTree called it (:1859): oak-family logs and leaves, mossy cobblestone steps.
     * The apex (:443-467) puts two emerald blocks on the trunk; its King and Queen belong to the royal trees.
     */
    private void bigSquareTree(RandomSource rand) {
        int x = origin.getX(), y = origin.getY(), z = origin.getZ();
        int tRadius = a;
        int treeType = b;
        boolean badCritters = d != 0;
        BlockState id = log(treeType);
        BlockState leafId = c != 0 ? persistent(ModBlocks.APPLE_LEAVES.get().defaultBlockState()) : leaves(treeType);
        BlockState stepId = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        int thisHeight = tRadius + rand.nextInt(tRadius);                              // :251
        int thisWidth = tRadius;                                                       // :252
        int baseHeight = tRadius * 3;                                                  // :253
        int spiral;                                                                    // :254
        int currentY;                                                                  // :255
        boolean doFloor;                                                               // :257
        int platformLooper;                                                            // :258
        int last = -1;                                                                 // :259
        int lastLast = -1;                                                             // :260
        for (int i = -tRadius; i <= tRadius; i++) {                                    // :261
            foundation(x + i, y, z - tRadius, id);                                     // :262-271
            foundation(x + i, y, z + tRadius, id);                                     // :273-282
            foundation(x - tRadius, y, z + i, id);                                     // :284-293
            foundation(x + tRadius, y, z + i, id);                                     // :295-305
        }
        currentY = y;                                                                  // :306
        spiral = -thisWidth;                                                           // :308
        while (thisWidth >= 0) {                                                       // :309
            if (thisWidth != tRadius) baseHeight = 0;                                  // :310-312
            for (int j = 0; j < thisHeight + baseHeight; j++) {                        // :313
                doFloor = false;                                                       // :314
                for (int i = -thisWidth; i <= thisWidth; i++) {                        // :315
                    if (isBoringBase(x + i, currentY, z - thisWidth)) place(x + i, currentY, z - thisWidth, id);   // :316-322
                    if (isBoringBase(x + i, currentY, z + thisWidth)) place(x + i, currentY, z + thisWidth, id);   // :323-329
                    if (isBoringBase(x - thisWidth, currentY, z + i)) place(x - thisWidth, currentY, z + i, id);   // :330-336
                    if (isBoringBase(x + thisWidth, currentY, z + i)) place(x + thisWidth, currentY, z + i, id);   // :337-342
                }
                if (thisWidth != 0 || j < thisHeight / 2) {                            // :344
                    platformLooper = 1;                                                // :345
                    if (spiral == 0 && thisWidth >= 2 || spiral == thisWidth
                            || spiral == thisWidth - 1 && j == thisHeight + baseHeight - 1) {   // :346
                        ++platformLooper;                                              // :347
                        if (spiral != 0 && thisWidth >= 3) ++platformLooper;           // :348-350
                        if (spiral == 0) doFloor = true;                               // :351-353
                    }
                    for (int k = 0; k < platformLooper; k++) {                         // :355
                        step(x - spiral, currentY, z - thisWidth - 1, stepId);         // :356-358
                        step(x + spiral, currentY, z + thisWidth + 1, stepId);         // :359-361
                        step(x - thisWidth - 1, currentY, z + spiral, stepId);         // :362-364
                        step(x + thisWidth + 1, currentY, z - spiral, stepId);         // :365-367
                        if (thisWidth >= 3) {                                          // :368
                            step(x - spiral, currentY, z - thisWidth - 2, stepId);     // :369-371
                            step(x + spiral, currentY, z + thisWidth + 2, stepId);     // :372-374
                            step(x - thisWidth - 2, currentY, z + spiral, stepId);     // :375-377
                            step(x + thisWidth + 2, currentY, z - spiral, stepId);     // :378-380
                        }
                        if (platformLooper == 1) continue;                             // :382
                        ++spiral;                                                      // :383
                    }
                    if (doFloor) {                                                     // :385
                        for (int m = -thisWidth; m <= thisWidth; m++) {                // :386
                            for (int n = -thisWidth; n <= thisWidth; n++) {            // :387
                                boolean boring = isBoring(x + m, currentY, z + n);     // :388, the write gate
                                if (boring) place(x + m, currentY, z + n, id);         // :389-393
                                if (m != 0 || n != 0) continue;                        // :394
                                boolean noChest = rand.nextInt(2) != 0;                // :394, drawn on every pass
                                if (!boring || noChest || badCritters || !isAirAt(x, currentY + 1, z)) continue;   // :394
                                lootChest(x, currentY + 1, z, FLOOR_LOOT);             // :395-398
                            }
                        }
                    }
                }
                if (thisWidth != tRadius) {                                            // :403
                    int next = rand.nextInt(4 + thisWidth);                            // :404
                    while (next == last || next == lastLast) {                         // :405
                        next = rand.nextInt(4 + thisWidth);                            // :406
                    }
                    if (next < 4) {                                                    // :408
                        lastLast = last;                                               // :409
                        last = next;                                                   // :410
                    }
                    switch (next) {                                                    // :412
                        case 0 -> makeBranch(rand, x + thisWidth, currentY, z, thisWidth, 1, 0, id, leafId, treeType, badCritters);    // :414
                        case 1 -> makeBranch(rand, x - thisWidth, currentY, z, thisWidth, -1, 0, id, leafId, treeType, badCritters);   // :418
                        case 2 -> makeBranch(rand, x, currentY, z + thisWidth, thisWidth, 0, 1, id, leafId, treeType, badCritters);    // :422
                        case 3 -> makeBranch(rand, x, currentY, z - thisWidth, thisWidth, 0, -1, id, leafId, treeType, badCritters);   // :426
                        default -> { }
                    }
                }
                ++currentY;                                                            // :431
                if (!doFloor) ++spiral;                                                // :432-434
                if (spiral <= thisWidth) continue;                                     // :435
                spiral = -thisWidth;                                                   // :436
            }
            if (Math.abs(spiral) > --thisWidth) spiral = -thisWidth;                   // :438-440
            thisHeight += rand.nextInt(tRadius);                                       // :441
        }
        if (isBoringBase(x, currentY, z)) {                                            // :443
            BlockState emerald = Blocks.EMERALD_BLOCK.defaultBlockState();
            place(x, currentY, z, emerald);                                            // :445
            place(x, currentY + 1, z, emerald);                                        // :446
        }
    }

    /** orig ItemMagicApple.java:262-271, one perimeter cell: where it is boring base, walk down up to 20 through boring base, laying the trunk. */
    private void foundation(int cx, int y, int cz, BlockState id) {
        if (!isBoringBase(cx, y, cz)) return;
        for (int j = 0; j < FOUNDATION; j++) {
            if (y - j <= ctx().minY()) continue;                                       // orig: y - j <= 0
            if (!isBoringBase(cx, y - j, cz)) break;
            place(cx, y - j, cz, id);
        }
    }

    /** orig ItemMagicApple.java:356-358, one step cell: written where boring. */
    private void step(int x, int y, int z, BlockState stepId) {
        if (isBoring(x, y, z)) place(x, y, z, stepId);
    }

    /**
     * orig ItemMagicApple.java:133-246 make_branch: a trunk run of the tier's width narrowing to a point, leaf
     * clusters once the run is thin, perpendicular sub-branches, and on the thick runs a chest (1 in 75) or a golem
     * (1 in 50) per cell of the centre line where the tree carries critters. Every draw of the original is made on
     * every pass; the terrain reads gate the writes alone (see the class note).
     */
    private void makeBranch(RandomSource rand, int x, int y, int z, int thisWidth, int dirx, int dirz,
                            BlockState id, BlockState leafId, int treeType, boolean badCritters) {
        int currentWidth = thisWidth;                                                  // :134
        int lastBranch = 0;                                                            // :135
        int branchSide = 1;                                                            // :136
        int xaccum = dirx;                                                             // :139
        int zaccum = dirz;                                                             // :140
        if (rand.nextInt(2) == 0) branchSide = -1;                                     // :141-143
        while (currentWidth >= 0) {                                                    // :144
            int length = thisWidth * 3 + rand.nextInt(thisWidth + 3);                  // :145
            for (int i = 0; i < length; i++) {                                         // :146
                for (int j = -currentWidth; j <= currentWidth; j++) {                  // :150
                    int realx = x + j * dirz + xaccum;                                 // :151
                    int realz = z + j * dirx + zaccum;                                 // :152
                    if (isBoring(realx, y, realz)) place(realx, y, realz, id);         // :153-159
                    if (i <= 0 || j != 0 || currentWidth < 3) continue;                // :160
                    if (treeType >= 0 && rand.nextInt(75) == 0 || treeType < 0 && rand.nextInt(50) == 0) {   // :161
                        if (badCritters || !isAirAt(realx, y + 1, realz)) continue;    // :162
                        lootChest(realx, y + 1, realz, BRANCH_LOOT);                   // :163-166
                        continue;                                                      // :167
                    }
                    if (rand.nextInt(50) != 0 || badCritters || !isAirAt(realx, y + 1, realz)
                            || !isAirAt(realx, y + 2, realz) || !isAirAt(realx, y + 3, realz)) continue;   // :169
                    ironGolem(realx + 0.5, y + 1.01, realz + 0.5);                     // :171
                }
                if (currentWidth < 3 || thisWidth <= 1) {                              // :173
                    int leafDepth = 2 + rand.nextInt(2);                               // :174
                    int leafWidth = 2 + rand.nextInt(3);                               // :175
                    for (int n = 0; n < leafDepth; n++) {                              // :176
                        int lw = currentWidth + leafWidth - n;                         // :177
                        if (currentWidth == 0 && length - i <= 2 && lw >= length - i) lw = length - i - 1;   // :178-180
                        if (lw < 0) lw = 0;                                            // :181-183
                        for (int j = -lw; j <= lw; j++) {                              // :184
                            int realx = x + j * Math.abs(dirz) + xaccum + dirx;        // :185
                            int realz = z + j * Math.abs(dirx) + zaccum + dirz;        // :186
                            boolean boring = isBoring(realx, y + n, realz);            // :186, the write gate
                            if (treeType >= 0) {                                       // :187
                                if (boring) place(realx, y + n, realz, leafId);        // :188
                                if (n != 0 || treeType != 3 || lw == 0 || j != lw && j != -lw || rand.nextInt(5) != 0) continue;   // :189
                                int vineLen = rand.nextInt(10);                        // :192 / :195 / :199 / :202
                                if (!boring) continue;
                                if (dirx == 0) {                                       // :190
                                    if (j == lw) growVines(realx + 1, y, realz, VineBlock.WEST, vineLen);    // :191-193, meta 2
                                    else growVines(realx - 1, y, realz, VineBlock.EAST, vineLen);          // :195, meta 8
                                    continue;
                                }
                                if (j == lw) growVines(realx, y, realz + 1, VineBlock.NORTH, vineLen);      // :198-200, meta 4
                                else growVines(realx, y, realz - 1, VineBlock.SOUTH, vineLen);              // :202, meta 1
                                continue;
                            }
                            BlockState local = leafId;                                 // :205
                            if (rand.nextInt(20) == 1) {                               // :206
                                if (rand.nextInt(3) != 0) {                            // :207
                                    local = Blocks.REDSTONE_BLOCK.defaultBlockState(); // :208
                                } else {
                                    int ilt = rand.nextInt(4);                         // :210
                                    local = switch (ilt) {
                                        case 0 -> ModBlocks.BLOCK_URANIUM.get().defaultBlockState();    // :211-213
                                        case 1 -> ModBlocks.BLOCK_TITANIUM.get().defaultBlockState();   // :214-216
                                        case 2 -> ModBlocks.BLOCK_RUBY.get().defaultBlockState();       // :217-219
                                        default -> ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();  // :220-222
                                    };
                                }
                            }
                            if (boring) place(realx, y + n, realz, local);             // :225
                        }
                    }
                }
                if (currentWidth > 0 && lastBranch > currentWidth && currentWidth != thisWidth
                        && rand.nextInt(currentWidth + 1) == 0) {                      // :229
                    int subdirx = branchSide;                                          // :230
                    int subdirz = 0;                                                   // :231
                    if (dirx != 0) {                                                   // :232
                        subdirx = 0;                                                   // :233
                        subdirz = branchSide;                                          // :234
                    }
                    makeBranch(rand, x + xaccum + currentWidth * subdirx, y, z + zaccum + currentWidth * subdirz,
                            currentWidth - 1, subdirx, subdirz, id, leafId, treeType, badCritters);   // :236
                    lastBranch = 0;                                                    // :237
                    branchSide = branchSide < 0 ? 1 : -1;                              // :238
                }
                xaccum += dirx;                                                        // :240
                zaccum += dirz;                                                        // :241
                ++lastBranch;                                                          // :242
            }
            --currentWidth;                                                            // :244
        }
    }

    /** orig ItemMagicApple.java growVines: a vine on the cell where air, then down the column while air, {@code len} more. */
    private void growVines(int x, int y, int z, BooleanProperty side, int len) {
        if (!isAirAt(x, y, z)) return;
        BlockState vine = Blocks.VINE.defaultBlockState().setValue(side, true);
        place(x, y, z, vine);
        while (len > 0) {
            if (!isAirAt(x, --y, z)) return;
            place(x, y, z, vine);
            --len;
        }
    }

    // ---- ItemMagicApple.java: the big circular tree --------------------------------------------------------------

    /**
     * orig ItemMagicApple.java:533-623 MakeBigCircularTree, as addHugeTree called it (:1868). The height counter
     * advances once per ring before the radius draw (the class file's {@code iinc}), as in the round tree.
     */
    private void bigCircularTree(RandomSource rand) {
        int x = origin.getX(), y = origin.getY(), z = origin.getZ();
        int tRadius = a;
        boolean badCritters = d != 0;
        BlockState id = log(b), leafId = leaves(b);
        BlockState stepId = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        BlockState diamond = Blocks.DIAMOND_BLOCK.defaultBlockState();
        double rad = tRadius;                                                          // :536
        int curx, cury, curz;                                                          // :537-539
        int stepindex = rand.nextInt(360);                                             // :540
        int ibranch = 0;                                                               // :541
        cury = y;                                                                      // :542
        for (int i = 0; i < 360; i++) {                                                // :543
            double dt = rad * Math.sin(Math.toRadians(i)) + 0.5;                       // :544
            curx = (int) dt;                                                           // :545
            curz = (int) (rad * Math.cos(Math.toRadians(i)) + 0.5);                    // :546
            if (!isBoringBase(x + curx, cury, z + curz)) continue;                     // :547
            for (int j = 0; j < FOUNDATION; j++) {                                     // :548
                if (cury - j <= ctx().minY()) continue;                                // :549
                if (!isBoringBase(x + curx, cury - j, z + curz)) break;                // :550
                place(x + curx, cury - j, z + curz, id);                               // :551-555
            }
        }
        cury = 1;                                                                      // :558
        int steps = 0;
        while (rad > 0.0) {                                                            // :559
            for (int i = 0; i < 360; i++) {                                            // :560
                double dt = rad * Math.sin(Math.toRadians(i)) + 0.5;                   // :561
                curx = (int) dt;                                                       // :562
                curz = (int) (rad * Math.cos(Math.toRadians(i)) + 0.5);                // :563
                if (isBoringBase(x + curx, y + cury, z + curz)) place(x + curx, y + cury, z + curz, id);   // :564-570
                if (i < stepindex - 1 || i > stepindex + 1 || !(rad > 1.0)) continue;  // :571
                curx = (int) ((rad + 1.9) * Math.sin(Math.toRadians(i)) + 0.5);        // :572-573
                curz = (int) ((rad + 1.9) * Math.cos(Math.toRadians(i)) + 0.5);        // :574-575
                for (int m = -1; m <= 1; m++) {                                        // :576
                    for (int n = -1; n <= 1; n++) {                                    // :577
                        if (!isBoringBase(x + curx + m, y + cury, z + curz + n)) continue;   // :578
                        place(x + curx + m, y + cury, z + curz + n, stepId);           // :579
                    }
                }
            }
            if (cury > (int) rad) {                                                    // :583
                ibranch += 80 + rand.nextInt(80);                                      // :584
                if (ibranch > 360) ibranch -= 360;                                     // :585-586
                int ibranchlen = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);      // :587
                curx = (int) (rad * Math.sin(Math.toRadians(ibranch)) + 0.5);          // :588-589
                curz = (int) (rad * Math.cos(Math.toRadians(ibranch)) + 0.5);          // :590-591
                int twist = rand.nextInt(2) * (rand.nextInt(2) == 0 ? -1 : 1);         // :592
                circularBranch(ibranch, ibranchlen, (int) rad + 1, x + curx, y + cury, z + curz, twist, id, leafId);   // :592
            }
            if (cury % 6 == 0 && rad > 3.0) {                                          // :594
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {                   // :595
                    for (int i = 0; i < 360; i++) {                                    // :596
                        double dt = dr * Math.sin(Math.toRadians(i)) + 0.5;            // :597
                        curx = (int) dt;                                               // :598
                        curz = (int) (dr * Math.cos(Math.toRadians(i)) + 0.5);         // :599
                        if (!isBoringBase(x + curx, y + cury, z + curz)) continue;     // :600
                        place(x + curx, y + cury, z + curz, id);                       // :601-605
                    }
                }
                boolean chest = rand.nextInt(2) == 0;                                  // :608
                if (chest && !badCritters && isAirAt(x, y + cury + 1, z)) {            // :608
                    lootChest(x, y + cury + 1, z, DISC_LOOT);                          // :609-613
                }
            }
            stepindex += 15 + (int) (((double) tRadius - rad) * 3.0);                  // :616
            if (stepindex > 360) stepindex -= 360;                                     // :616-618
            ++cury;                                                                    // the class file's iinc
            rad -= 0.01 * rand.nextInt(15);                                            // :619
            if (rad <= 0.0 && isBoringBase(x, y + cury, z)) {                          // :619
                place(x, y + cury, z, diamond);                                        // :620
            }
            if (++steps > RING_STEP_CAP) break;
        }
    }

    /** orig ItemMagicApple.java:471-531 MakeCirclularBranch: a tapering run out from the ring at the branch angle, its inner half trunk with leaves above, its outer half and its last stretch leaves, twisting by {@code twist} degrees a step. */
    private void circularBranch(int iangle, int branchlen, int width, int startx, int starty, int startz, int twist,
                                BlockState id, BlockState leafId) {
        int curangle = iangle;                                                         // :473
        double curx = startx;                                                          // :474
        double curz = startz;                                                          // :475
        for (double curlen = 0.0; curlen < (double) branchlen; curlen += 0.5) {        // :476
            curx += 0.5 * Math.sin(Math.toRadians(curangle));                          // :477
            curz += 0.5 * Math.cos(Math.toRadians(curangle));                          // :478
            double tw = (double) width - (double) width * curlen / (double) branchlen; // :479
            for (double wd = 0.0; wd <= tw; wd += 0.5) {                               // :480
                BlockState cell = leafId;                                              // :484
                if (wd < tw / 2.0) cell = id;                                          // :485-487
                if (tw < 0.9) cell = leafId;                                           // :488-490
                int ta = curangle + 90;                                                // :491
                if (ta > 360) ta -= 360;                                               // :491-493
                double wx = curx + wd * Math.sin(Math.toRadians(ta));                  // :494
                double wz = curz + wd * Math.cos(Math.toRadians(ta));                  // :494
                if (isBoring((int) wx, starty, (int) wz)) place((int) wx, starty, (int) wz, cell);   // :494-500
                if (cell == id && isBoring((int) wx, starty + 1, (int) wz)) {          // :501
                    place((int) wx, starty + 1, (int) wz, leafId);                     // :502-506
                }
                ta = curangle - 90;                                                    // :508
                if (ta < 0) ta += 360;                                                 // :508-510
                wx = curx + wd * Math.sin(Math.toRadians(ta));                         // :511
                wz = curz + wd * Math.cos(Math.toRadians(ta));                         // :511
                if (isBoring((int) wx, starty, (int) wz)) place((int) wx, starty, (int) wz, cell);   // :511-517
                if (cell != id || !isBoring((int) wx, starty + 1, (int) wz)) continue; // :518
                place((int) wx, starty + 1, (int) wz, leafId);                         // :519-523
            }
            curangle += twist;                                                         // :525
            if (curangle < 0) curangle += 360;                                         // :525-527
            if (curangle < 360) continue;                                              // :528
            curangle -= 360;                                                           // :529
        }
    }
}
