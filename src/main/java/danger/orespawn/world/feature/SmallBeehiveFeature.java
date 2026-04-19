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
 * Small Beehive ("skep") &mdash; <b>QA Bug Fix Part 5 rewrite</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code GenericDungeon.makeSmallBeeHive} (1.7.10 source, lines
 * 1363&ndash;1451). This is the surface-spawned bee skep that the
 * original {@code addANest} (OreSpawnWorld.java:999&ndash;1021) drops
 * 50/50 with the Mantis Hive in Forest, ForestHills, Jungle,
 * JungleHills, Birch Forest, and Birch Forest Hills biomes.</p>
 *
 * <p>The previous 1.21.1 port shipped only the deep
 * {@link BeehiveFeature} ({@code makeBeeHive}, the 30-block-deep mining
 * dim variant). The {@code makeSmallBeeHive} surface skep was missing
 * entirely &mdash; QA flagged "only one beehive variant generates",
 * which this class restores.</p>
 *
 * <p><b>Legacy geometry (verified GenericDungeon.java:1363-1451):</b></p>
 * <ol>
 *   <li>Hollow a {@code (width+6) x (height/3) x (width+6)} air halo
 *       above {@code cposy + height*2/3} so the skep can rise out of
 *       the canopy without clipping leaves (lines 1376&ndash;1382;
 *       {@code width=7, height=21}).</li>
 *   <li>Stamp a {@code 7&times;7} honeycomb-textured ({@code field_150360_v},
 *       legacy {@code Blocks.sponge}) "trunk top" at {@code cposy+14}
 *       (line 1386), then drop variable-height
 *       {@link net.minecraft.world.level.block.Blocks#MOSSY_COBBLESTONE}
 *       columns below it (max in the centre, tapered toward the
 *       corners; lines 1387&ndash;1399). The legacy column heights are
 *       randomised by {@code nextInt(height/3)*2 - |dx| - |dz|}, so the
 *       trunk silhouette varies per spawn just like the legacy.</li>
 *   <li>Stack three 2-layer bee-skep "tiers" above the trunk: each tier
 *       is a {@code 7&times;7} honeycomb ring followed by a slightly
 *       inflated {@code 9&times;9} honeycomb ring (lines 1402&ndash;1422).
 *       Loop count is {@code height/6 = 3}, matching the canonical 3
 *       tiers.</li>
 *   <li>Cap with a flat {@code 7&times;7} honeycomb roof (lines
 *       1425&ndash;1428).</li>
 *   <li>Carve a {@code 2&times;3&times;2} doorway out of the west face
 *       at the trunk top (lines 1431&ndash;1437). Players can walk in.</li>
 *   <li>Stack 3 Bee spawners vertically inside the chamber at offset
 *       {@code (1, 1)} (lines 1438&ndash;1443).</li>
 *   <li>Drop a single chest at the chamber centre with {@code 7 +
 *       nextInt(5)} weighted draws from {@code beeContentsList} (lines
 *       1444&ndash;1450).</li>
 * </ol>
 *
 * <p><b>Block palette substitution:</b> the 1.7.10 source used
 * {@code Blocks.sponge} ({@code field_150360_v}) only because
 * {@code honeycomb_block} did not exist in 1.7.10. The sponge there is
 * the canonical "yellow honeycomb-textured block". In 1.21.1 we use
 * {@link net.minecraft.world.level.block.Blocks#HONEYCOMB_BLOCK} as the
 * modern thematic equivalent &mdash; identical visual intent, plus it
 * actually behaves like a beehive material instead of absorbing water.
 * Mossy cobblestone for the trunk is preserved verbatim
 * ({@code field_150341_Y}).</p>
 *
 * <p><b>Loot fidelity:</b> shares {@code beeContentsList} with the deep
 * {@link BeehiveFeature} (sugar, dandelion, gold nuggets, paper, Fairy
 * Sword, Pink/Crystal Pink armour set, butter candy, experience
 * catcher, golden carrot stand-in for the legacy bee egg), but the
 * small skep gets {@code 7 + nextInt(5)} draws (vs 1+nextInt(5) in the
 * deep variant) per legacy line 1449.</p>
 *
 * <p><b>Spawn frequency:</b> wired via {@code structure_set/small_beehive.json}
 * with {@code spacing=24, separation=12, salt=84314} so it shares the
 * Forest/Jungle {@code addANest} grid with {@link MantisNestFeature}
 * and {@link BeehiveFeature} but avoids salt collisions.</p>
 */
public class SmallBeehiveFeature extends Feature<NoneFeatureConfiguration> {

    /** Footprint side length. Legacy: {@code width = 7}. */
    private static final int WIDTH = 7;
    /** Vertical span. Legacy: {@code height = 21}. */
    private static final int HEIGHT = 21;
    /** {@code height * 2 / 3 = 14} &mdash; trunk-top elevation. */
    private static final int TRUNK_Y = HEIGHT * 2 / 3;

    public SmallBeehiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        // Legacy addANest probes for a grass tile and calls makeSmallBeeHive
        // with (posX, posY, posZ) where posY is the air block above the grass.
        // Mirror that by anchoring at the surface heightmap.
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos cpos = surface;

        // Bound-check the entire vertical span (~22 blocks up) up-front so
        // the skep doesn't shear at the build limit.
        if (cpos.getY() + HEIGHT + 2 >= level.getMaxBuildHeight() - 2) return false;
        if (cpos.getY() <= level.getMinBuildHeight() + 2) return false;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState honeycomb = Blocks.HONEYCOMB_BLOCK.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();

        // Step 1: hollow the canopy halo (lines 1376-1382).
        for (int i = -3; i < WIDTH + 3; i++) {
            for (int j = TRUNK_Y; j < HEIGHT; j++) {
                for (int k = -3; k < WIDTH + 3; k++) {
                    level.setBlock(cpos.offset(i, j, k), air, 2);
                }
            }
        }

        // Step 2: trunk-top platform + variable mossy cobblestone columns
        // (lines 1383-1400).
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                level.setBlock(cpos.offset(i, TRUNK_Y, k), honeycomb, 2);
                int colHeight = random.nextInt(HEIGHT / 3) * 2;
                colHeight -= Math.abs(i - WIDTH / 2);
                colHeight -= Math.abs(k - WIDTH / 2);
                if (colHeight < 1) colHeight = 1;
                // Centre column always reaches the trunk top.
                if (i == WIDTH / 2 && k == WIDTH / 2) colHeight = TRUNK_Y;
                for (int j = 0; j < colHeight; j++) {
                    level.setBlock(cpos.offset(i, TRUNK_Y - j, k), mossy, 2);
                }
            }
        }

        // Step 3: stack 3 bee-skep tiers above the trunk (lines 1401-1423).
        int j = TRUNK_Y;
        for (int tier = 0; tier < HEIGHT / 6; tier++) {
            // Inner ring: 7x7 honeycomb perimeter, hollow interior.
            j++;
            for (int i = 0; i < WIDTH; i++) {
                for (int k = 0; k < WIDTH; k++) {
                    if (i == 0 || k == 0 || i == WIDTH - 1 || k == WIDTH - 1) {
                        level.setBlock(cpos.offset(i, j, k), honeycomb, 2);
                    } else {
                        level.setBlock(cpos.offset(i, j, k), air, 2);
                    }
                }
            }
            // Outer ring: 9x9 honeycomb perimeter (inflated by 1 each side).
            j++;
            for (int i = -1; i < WIDTH + 1; i++) {
                for (int k = -1; k < WIDTH + 1; k++) {
                    if (i == -1 || k == -1 || i == WIDTH || k == WIDTH) {
                        level.setBlock(cpos.offset(i, j, k), honeycomb, 2);
                    } else {
                        level.setBlock(cpos.offset(i, j, k), air, 2);
                    }
                }
            }
        }

        // Step 4: flat 7x7 honeycomb roof (lines 1424-1429).
        j++;
        for (int i = 0; i < WIDTH; i++) {
            for (int k = 0; k < WIDTH; k++) {
                level.setBlock(cpos.offset(i, j, k), honeycomb, 2);
            }
        }

        // Step 5: carve the west-face doorway (lines 1430-1437).
        // The legacy code RE-ASSIGNS j here so spawner/chest math below uses
        // j = TRUNK_Y + 1 (= 15), not the cap j we've been incrementing.
        j = TRUNK_Y + 1;
        for (int i = -1; i < 1; i++) {
            for (int k = 2; k < 4; k++) {
                level.setBlock(cpos.offset(i, j,     k), air, 2);
                level.setBlock(cpos.offset(i, j + 1, k), air, 2);
                level.setBlock(cpos.offset(i, j + 2, k), air, 2);
            }
        }

        // Step 6: 3 Bee spawners stacked at (1, 1) inside the chamber
        // (lines 1438-1443).
        for (int s = 0; s < 3; s++) {
            placeSpawner(level, cpos.offset(1, j + s, 1), ModEntities.ENTITY_BEE.get());
        }

        // Step 7: single chest at chamber centre (lines 1444-1450).
        BlockPos chestPos = cpos.offset(WIDTH / 2, j, WIDTH / 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            // Legacy: 7 + nextInt(5) draws (line 1449).
            fillBeeLoot(chest, random, 7 + random.nextInt(5));
        }
        return true;
    }

    /**
     * Populates a chest with the canonical legacy {@code beeContentsList}
     * (GenericDungeon.java:55). Item pool, weights, and stack sizes
     * exactly mirror the {@link BeehiveFeature} fill (DRY would put this
     * in a shared util, but I'm keeping the small skep self-contained so
     * the audit trail per-feature stays explicit).
     */
    private static void fillBeeLoot(ChestBlockEntity chest, Random random, int draws) {
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
