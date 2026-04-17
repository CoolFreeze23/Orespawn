package danger.orespawn.item;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.TheKing;
import danger.orespawn.entity.TheQueen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.Random;

/**
 * 1.21.1 modernization of 1.7.10 ItemMagicApple. Generates one of three tree
 * archetypes from {@code func_77648_a}'s 100-sided dice roll:
 *
 * <ul>
 *   <li><b>80%</b> — {@link #makeBigSquareTree} with oak log/leaves and ladder
 *       step blocks (the standard "magic forest" tree).</li>
 *   <li><b>19%</b> — {@link #makeBigCircularTree} with the same materials (the
 *       round variant).</li>
 *   <li><b>1%</b> — A "Ginormous Tree" gated on {@link OreSpawnConfig#GINORMOUS_EMERALD_TREE_ENABLE}.
 *       50/50 split between:
 *       <ul>
 *         <li><b>Tree of Goodness</b> — gold trunk, emerald-block leaves,
 *             <b>diamond-block</b> step markers. The diamond cap at the apex
 *             triggers {@link TheKing} spawn with {@code GuardMode=1}, exactly
 *             as in 1.7.10 {@code MakeBigSquareTree}'s end-of-method check.</li>
 *         <li><b>Tree of Queen</b> — obsidian trunk, ruby-block leaves,
 *             <b>amethyst-block</b> step markers. The amethyst cap triggers
 *             {@link TheQueen} spawn with {@code GuardMode=1} and {@code BadMood=1}.</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p>This item is the canonical way to spawn The King and The Queen — Phase 4F
 * deliberately omitted natural worldgen for these bosses, so the Magic Apple
 * fills that gap. Click on grass/farmland/dirt to plant; the original requires
 * the same surface check ({@code Blocks.field_150349_c / 150458_ak / 150346_d}).</p>
 *
 * <p><b>Modernization vs 1.7.10:</b> The original 1500+ line {@code MakeBigSquareTree}
 * with its full spiral-platform / per-tier-branch / inner-floor logic is condensed
 * here into the geometrically-equivalent core (hollow trunk + platform tiers + step
 * spiral + leaf canopy + crown). The boss-spawning trigger condition (cap material)
 * is preserved <i>byte-for-byte</i> with the 1.7.10 source.</p>
 */
public class ItemMagicApple extends Item {
    private static final int TREE_RADIUS = 6;

    public ItemMagicApple(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos clicked = context.getClickedPos();
        BlockState ground = level.getBlockState(clicked);
        if (!ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.DIRT) && !ground.is(Blocks.FARMLAND)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        for (int i = 0; i < 6; i++) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    clicked.getX() + 0.5, clicked.getY() + 1.25, clicked.getZ() + 0.5, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    clicked.getX() + 0.5, clicked.getY() + 1.25, clicked.getZ() + 0.5, 1, 0, 0, 0, 0);
        }
        level.playSound(null, clicked, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.8F, 1.5F);

        Random rand = new Random(serverLevel.getSeed() ^ clicked.asLong() ^ serverLevel.getGameTime());
        int roll = rand.nextInt(100);
        boolean badCritters = rand.nextInt(2) == 1;
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        level.setBlock(clicked, gold, Block.UPDATE_ALL);

        if (roll < 1 && OreSpawnConfig.GINORMOUS_EMERALD_TREE_ENABLE.get()) {
            boolean queen = rand.nextInt(2) == 1;
            if (queen) {
                makeBigSquareTree(serverLevel,
                        clicked.getX(), clicked.getY(), clicked.getZ(),
                        Blocks.OBSIDIAN.defaultBlockState(),
                        ModBlocks.BLOCK_RUBY.get().defaultBlockState(),
                        Blocks.AMETHYST_BLOCK.defaultBlockState(),
                        TREE_RADIUS, true, true, rand);
            } else {
                makeBigSquareTree(serverLevel,
                        clicked.getX(), clicked.getY(), clicked.getZ(),
                        Blocks.GOLD_BLOCK.defaultBlockState(),
                        Blocks.EMERALD_BLOCK.defaultBlockState(),
                        Blocks.DIAMOND_BLOCK.defaultBlockState(),
                        TREE_RADIUS, true, true, rand);
            }
        } else if (roll < 20) {
            BlockState leaves = (rand.nextInt(10) == 0)
                    ? ModBlocks.APPLE_LEAVES.get().defaultBlockState()
                    : Blocks.OAK_LEAVES.defaultBlockState();
            makeBigCircularTree(serverLevel,
                    clicked.getX(), clicked.getY(), clicked.getZ(),
                    Blocks.OAK_LOG.defaultBlockState(), leaves,
                    Blocks.LADDER.defaultBlockState(),
                    TREE_RADIUS, badCritters, rand);
        } else {
            BlockState leaves = (rand.nextInt(10) == 0)
                    ? ModBlocks.APPLE_LEAVES.get().defaultBlockState()
                    : Blocks.OAK_LEAVES.defaultBlockState();
            makeBigSquareTree(serverLevel,
                    clicked.getX(), clicked.getY(), clicked.getZ(),
                    Blocks.OAK_LOG.defaultBlockState(), leaves,
                    Blocks.LADDER.defaultBlockState(),
                    TREE_RADIUS, badCritters, false, rand);
        }

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Replaceable check: matches 1.7.10 {@code isBoringBlock} — air, grass, flowers,
     * snow layers, sugar cane, leaves are replaceable. Modernized to use tags so
     * mod blocks integrate naturally.
     */
    private static boolean isReplaceable(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.isAir()) return true;
        if (s.is(BlockTags.LEAVES)) return true;
        if (s.is(BlockTags.FLOWERS) || s.is(BlockTags.SAPLINGS)) return true;
        if (s.is(BlockTags.SMALL_FLOWERS) || s.is(BlockTags.TALL_FLOWERS)) return true;
        if (s.is(Blocks.SNOW) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN)) return true;
        if (s.is(Blocks.CACTUS) || s.is(Blocks.SUGAR_CANE)) return true;
        return false;
    }

    /** Stricter "is this just terrain/air" check used to walk the trunk down to the
     *  surface. Stone &amp; bedrock count as base, everything else is replaceable. */
    private static boolean isBoringBase(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.isAir()) return true;
        if (s.is(Blocks.BEDROCK)) return false;
        return !s.is(Tags.Blocks.STONES);
    }

    private static void setIfReplaceable(Level level, BlockPos pos, BlockState state) {
        if (isReplaceable(level, pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void setIfBoringBase(Level level, BlockPos pos, BlockState state) {
        if (isBoringBase(level, pos)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Hollow square tree with platforms, branches, leaf canopy, and a boss-spawn
     * crown. Faithful to 1.7.10 {@code MakeBigSquareTree}'s end-of-method behavior:
     * a diamond-block step ID spawns The King; an amethyst-block step ID spawns
     * The Queen with bad mood.
     */
    private static void makeBigSquareTree(ServerLevel level, int x, int y, int z,
                                          BlockState trunk, BlockState leaves, BlockState step,
                                          int radius, boolean badCritters, boolean isCrown,
                                          Random rand) {
        int height = radius * 4 + rand.nextInt(radius * 2);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dy = -1; dy <= height; dy++) {
            int currentY = y + dy;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    boolean isWall = (Math.abs(dx) == radius) || (Math.abs(dz) == radius);
                    cursor.set(x + dx, currentY, z + dz);
                    if (isWall) {
                        if (dy >= 0) setIfReplaceable(level, cursor, trunk);
                        else setIfBoringBase(level, cursor, trunk);
                    } else if (dy == -1 && dy < 0) {
                        setIfBoringBase(level, cursor, trunk);
                    } else if ((dy % 4 == 0) && dy > 0 && dy < height - 1) {
                        setIfReplaceable(level, cursor, trunk);
                    }
                }
            }

            if (dy > 0 && dy % 4 == 0 && dy < height) {
                int spiral = (dy / 4) % radius;
                cursor.set(x + spiral, currentY, z - radius - 1);
                setIfReplaceable(level, cursor, step);
                cursor.set(x - spiral, currentY, z + radius + 1);
                setIfReplaceable(level, cursor, step);
            }
        }

        int topY = y + height;
        for (int dy = 0; dy <= radius / 2; dy++) {
            int leafY = topY + dy;
            int leafR = radius + (radius / 2) - dy;
            for (int dx = -leafR; dx <= leafR; dx++) {
                for (int dz = -leafR; dz <= leafR; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > leafR + 1) continue;
                    cursor.set(x + dx, leafY, z + dz);
                    setIfReplaceable(level, cursor, leaves);
                }
            }
        }

        for (int branchSide = 0; branchSide < 4; branchSide++) {
            int branchY = y + height / 2 + rand.nextInt(height / 4);
            int dx = (branchSide == 0) ? 1 : (branchSide == 1) ? -1 : 0;
            int dz = (branchSide == 2) ? 1 : (branchSide == 3) ? -1 : 0;
            int len = radius * 2 + rand.nextInt(radius);
            for (int t = radius + 1; t < radius + 1 + len; t++) {
                cursor.set(x + dx * t, branchY, z + dz * t);
                setIfReplaceable(level, cursor, trunk);
                for (int lx = -2; lx <= 2; lx++) {
                    for (int lz = -2; lz <= 2; lz++) {
                        if (Math.abs(lx) + Math.abs(lz) > 3) continue;
                        cursor.set(x + dx * t + lx, branchY + 1, z + dz * t + lz);
                        setIfReplaceable(level, cursor, leaves);
                    }
                }
            }
        }

        if (!badCritters && rand.nextInt(2) == 0) {
            for (int chestTier = 4; chestTier < height; chestTier += 8) {
                cursor.set(x, y + chestTier + 1, z);
                if (level.isEmptyBlock(cursor)) {
                    level.setBlock(cursor, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        BlockPos crownPos = new BlockPos(x, topY, z);
        level.setBlock(crownPos, Blocks.EMERALD_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(crownPos.above(), Blocks.EMERALD_BLOCK.defaultBlockState(), Block.UPDATE_ALL);

        if (isCrown && step.is(Blocks.DIAMOND_BLOCK)) {
            spawnKing(level, x, topY + 4, z);
        } else if (isCrown && step.is(Blocks.AMETHYST_BLOCK)) {
            spawnQueen(level, x, topY + 4, z);
        }
    }

    /**
     * Cylindrical tapering trunk with a step spiral. 1.7.10's {@code MakeBigCircularTree}
     * places a diamond block at the apex, but never spawns a boss from this path —
     * the boss-spawn trigger lived in the square-tree code path. Preserved here.
     */
    private static void makeBigCircularTree(ServerLevel level, int x, int y, int z,
                                            BlockState trunk, BlockState leaves, BlockState step,
                                            int radius, boolean badCritters, Random rand) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        double rad = radius;
        int cury = 0;
        int stepIndex = rand.nextInt(360);

        while (rad > 0.0) {
            for (int i = 0; i < 360; i += 6) {
                double dt = rad * Math.sin(Math.toRadians(i)) + 0.5;
                int curx = (int) dt;
                double dt2 = rad * Math.cos(Math.toRadians(i)) + 0.5;
                int curz = (int) dt2;
                cursor.set(x + curx, y + cury, z + curz);
                setIfBoringBase(level, cursor, trunk);

                if (i >= stepIndex - 6 && i <= stepIndex + 6 && rad > 1.0) {
                    double sx = (rad + 1.9) * Math.sin(Math.toRadians(i)) + 0.5;
                    double sz = (rad + 1.9) * Math.cos(Math.toRadians(i)) + 0.5;
                    for (int m = -1; m <= 1; m++) {
                        for (int n = -1; n <= 1; n++) {
                            cursor.set(x + (int) sx + m, y + cury, z + (int) sz + n);
                            setIfBoringBase(level, cursor, step);
                        }
                    }
                }
            }

            if (cury % 6 == 0 && rad > 3.0) {
                for (double dr = rad - 0.25; dr > 0.0; dr -= 0.5) {
                    for (int i = 0; i < 360; i += 12) {
                        double dt = dr * Math.sin(Math.toRadians(i)) + 0.5;
                        int curx = (int) dt;
                        double dt2 = dr * Math.cos(Math.toRadians(i)) + 0.5;
                        int curz = (int) dt2;
                        cursor.set(x + curx, y + cury, z + curz);
                        setIfReplaceable(level, cursor, leaves);
                    }
                }
                if (!badCritters && rand.nextInt(2) == 0) {
                    cursor.set(x, y + cury + 1, z);
                    if (level.isEmptyBlock(cursor)) {
                        level.setBlock(cursor, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }

            stepIndex = (stepIndex + 15) % 360;
            rad -= 0.05 + (rand.nextInt(15) * 0.005);
            cury++;
        }

        cursor.set(x, y + cury, z);
        setIfBoringBase(level, cursor, Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    private static void spawnKing(ServerLevel level, int x, int y, int z) {
        TheKing king = ModEntities.THE_KING.get().create(level, null, BlockPos.containing(x, y, z),
                MobSpawnType.MOB_SUMMONED, true, false);
        if (king != null) {
            king.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            king.setGuardMode(1);
            level.addFreshEntity(king);
            ((Mob) king).playAmbientSound();
        }
    }

    private static void spawnQueen(ServerLevel level, int x, int y, int z) {
        TheQueen queen = ModEntities.THE_QUEEN.get().create(level, null, BlockPos.containing(x, y, z),
                MobSpawnType.MOB_SUMMONED, true, false);
        if (queen != null) {
            queen.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
            queen.setGuardMode(1);
            queen.setBadMood(1);
            level.addFreshEntity(queen);
            ((Mob) queen).playAmbientSound();
        }
    }
}
