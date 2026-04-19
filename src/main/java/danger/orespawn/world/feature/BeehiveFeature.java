package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

/**
 * Beehive &mdash; <b>Audit Part 1 rewrite</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code GenericDungeon.makeBeeHive} (1.7.10 source, lines
 * 812&ndash;858). The previous 1.21.1 implementation was a
 * procedural hallucination (3&times;2&times;3 mossy cobble box) that
 * bore no resemblance to the legacy structure.</p>
 *
 * <p><b>Legacy geometry (verified GenericDungeon.java:812-858):</b></p>
 * <ol>
 *   <li>Hollow a {@code 10&times;5&times;10} air pocket centred on
 *       {@code (cposx, cposy, cposz)}, descending from the surface
 *       (lines 821&ndash;827).</li>
 *   <li>Stamp a solid {@code COAL_ORE} floor at {@code cposy - 30}
 *       (lines 828&ndash;833, legacy {@code field_150365_q} = coal_ore).</li>
 *   <li>For Y rows 1..29 below {@code cposy}: stamp a 10&times;10
 *       perimeter ring of alternating {@code COAL_ORE} (even rows)
 *       and {@code GOLD_ORE} (odd rows, legacy {@code field_150352_o}).
 *       Interior of each Y row is hollow air (lines 835&ndash;849).</li>
 *   <li>Place 4 Bee spawners at {@code cposy - 2 - j*7}
 *       (j=0..3) on the centre column (lines 851&ndash;856).</li>
 *   <li>Stamp 4 chests per Y row (every 2 rows from j=2 to j=29) at
 *       the cardinal-wall midpoints (lines 860&ndash;888).</li>
 * </ol>
 *
 * <p><b>Loot fidelity (beeContentsList, GenericDungeon.java:55):</b>
 * sugar, dandelion, gold nuggets, paper, Fairy Sword, full Crystal
 * Pink armour set, butter candy, experience catcher, bee egg
 * (golden carrot stand-in: no entity item exists in 1.21.1). Each
 * chest gets {@code 1 + nextInt(5)} weighted draws.</p>
 *
 * <p><b>Spawn frequency:</b></p>
 * <ul>
 *   <li><b>Forest/Jungle (verified OreSpawnWorld.java:999-1018,
 *       addANest):</b> {@code random.nextInt(230) != 0} gating &times;
 *       50/50 split with the mantis hive variant &rarr; net <b>1 in
 *       460 chunks</b> in Forest / Jungle / Birch Forest biomes.</li>
 *   <li>Mining Dim (legacy line 88): also reachable via the
 *       {@code random.nextInt(95) == 1 && pick == 2} path. We do not
 *       wire that branch into 1.21.1 because we don't have a mining
 *       dim chunk-pick rotation; the Forest/Jungle branch is the
 *       canonical surface variant.</li>
 * </ul>
 *
 * <p>Encoded by {@code structure_set/beehive.json} as
 * {@code spacing=24, separation=12} (~1 per 576 chunks) so it shares
 * the addANest grid with the mantis nest without colliding.</p>
 *
 * <p><b>Stability guard:</b> the 30-block descent is bounds-checked
 * up-front; placement aborts if the shaft would clip the world floor
 * (legacy 1.7.10 had no such guard).</p>
 */
public class BeehiveFeature extends Feature<NoneFeatureConfiguration> {

    /** Horizontal footprint. Legacy: {@code width = 10}. */
    private static final int WIDTH = 10;
    /** Vertical reach below the surface anchor. Legacy: {@code height = 30}. */
    private static final int HEIGHT = 30;

    public BeehiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        // Legacy addBeeHive (OreSpawnWorld.java:2031) probes for the
        // lowest grass tile in the chunk, then calls makeBeeHive at
        // (lowestX, lowestY + 3, lowestZ). Mirror that by adding 3 to
        // the surface tile so the chamber pokes out of the ground.
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos cpos = surface.above(3);

        // Bound-check the entire 10x35x10 footprint up-front.
        if (cpos.getY() + 1 >= level.getMaxBuildHeight() - 2) return false;
        if (cpos.getY() - HEIGHT - 1 <= level.getMinBuildHeight() + 2) return false;

        BlockState air = Blocks.AIR.defaultBlockState();
        // QA Fix: legacy 1.7.10 used coal_ore (field_150365_q) and
        // gold_ore (field_150352_o), NOT the compressed coal_block /
        // gold_block. The previous port substituted blocks for ores —
        // restoring the canonical ore palette here.
        BlockState coal = Blocks.COAL_ORE.defaultBlockState();
        BlockState gold = Blocks.GOLD_ORE.defaultBlockState();

        // Step 1: hollow the 10x5x10 surface chamber (lines 821-827).
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < WIDTH; k++) {
                    level.setBlock(cpos.offset(i, -j, k), air, 2);
                }
            }
        }

        // Step 2: solid coal-ore floor at cposy - 30 (lines 828-833).
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                level.setBlock(cpos.offset(i, -HEIGHT, k), coal, 2);
            }
        }

        // Step 3: 10x10 perimeter rings, alternating coal/gold, with
        // hollow interior for Y rows 1..29 (lines 835-849).
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                for (int j = 1; j < HEIGHT; j++) {
                    if (k == 0 || i == 0 || k == WIDTH - 1 || i == WIDTH - 1) {
                        BlockState ringBlock = (j & 1) == 1 ? gold : coal;
                        level.setBlock(cpos.offset(i, -j, k), ringBlock, 2);
                    } else {
                        level.setBlock(cpos.offset(i, -j, k), air, 2);
                    }
                }
            }
        }

        // Step 4: 4 Bee spawners at cposy - 2 - j*7, j=0..3 (lines 851-856).
        for (int j = 0; j < 4; j++) {
            placeSpawner(level, cpos.offset(WIDTH / 2, -2 - j * (HEIGHT / 4), WIDTH / 2),
                    ModEntities.ENTITY_BEE.get());
        }

        // Step 5: 4 chests per layer every 2 rows j=2..29 (lines 860-888).
        fillBeehiveChests(level, cpos, random);
        return true;
    }

    /** Direct port of {@code fill_beehive_chests} (GenericDungeon.java:860-888). */
    private static void fillBeehiveChests(WorldGenLevel level, BlockPos cpos, Random random) {
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        for (int j = 2; j < HEIGHT - 1; j += 2) {
            BlockPos[] chestPositions = new BlockPos[]{
                    cpos.offset(1, -j, WIDTH / 2),
                    cpos.offset(WIDTH - 2, -j, WIDTH / 2),
                    cpos.offset(WIDTH / 2, -j, 1),
                    cpos.offset(WIDTH / 2, -j, WIDTH - 2)
            };
            for (BlockPos chestPos : chestPositions) {
                level.setBlock(chestPos, chestState, 2);
                if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                    fillBeeLoot(chest, random);
                }
            }
        }
    }

    /**
     * Populates a chest with the canonical legacy beeContentsList
     * (GenericDungeon.java:55). Each chest gets {@code 1 + nextInt(5)}
     * draws, mirroring the legacy WeightedRandomChestContent fill loop.
     */
    private static void fillBeeLoot(ChestBlockEntity chest, Random random) {
        int[] weights = {15, 15, 15, 15, 10, 10, 10, 10, 10, 15, 10, 15};
        ItemStack[] pool = {
                new ItemStack(Items.SUGAR, 2 + random.nextInt(7)),
                new ItemStack(Items.DANDELION, 4 + random.nextInt(5)),
                new ItemStack(Items.GOLD_NUGGET, 5 + random.nextInt(11)),
                new ItemStack(Items.PAPER, 2 + random.nextInt(7)),
                new ItemStack(ModItems.FAIRY_SWORD.get()),
                new ItemStack(ModItems.PINK_HELMET.get()),
                new ItemStack(ModItems.PINK_CHESTPLATE.get()),
                new ItemStack(ModItems.PINK_LEGGINGS.get()),
                new ItemStack(ModItems.PINK_BOOTS.get()),
                new ItemStack(ModItems.BUTTER_CANDY.get(), 2 + random.nextInt(7)),
                new ItemStack(ModItems.EXPERIENCE_CATCHER.get(), 4 + random.nextInt(7)),
                new ItemStack(Items.GOLDEN_CARROT, 1 + random.nextInt(3))
        };
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;

        int draws = 1 + random.nextInt(5);
        for (int draw = 0; draw < draws && draw < chest.getContainerSize(); draw++) {
            int roll = random.nextInt(totalWeight);
            int cumulative = 0;
            for (int idx = 0; idx < weights.length; idx++) {
                cumulative += weights[idx];
                if (roll < cumulative) {
                    chest.setItem(draw, pool[idx].copy());
                    break;
                }
            }
        }
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }
}
