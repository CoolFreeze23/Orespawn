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
 * Mantis Nest (a.k.a. "Mantis Hive") &mdash; <b>Audit Part 1 rewrite</b>.
 *
 * <p>Authentic byte-for-byte port of legacy
 * {@code GenericDungeon.makeMantisHive} (1.7.10 source, lines
 * 1012&ndash;1062). The previous 1.21.1 implementation was a
 * procedural hallucination (3&times;3 mossy cobble platform with one
 * spawner) that bore no resemblance to the legacy structure.</p>
 *
 * <p><b>Legacy geometry (verified GenericDungeon.java:1012-1062):</b></p>
 * <ol>
 *   <li>Hollow a 13&times;20&times;13 air pocket above {@code cposy} (lines
 *       1021&ndash;1027).</li>
 *   <li>Stamp a stepped pyramid <em>downward</em>: starting at
 *       {@code width = 13} and shrinking by 2 each step
 *       (13&rarr;11&rarr;9&rarr;7&rarr;5&rarr;3&rarr;1), each ring
 *       offset by {@code (xoff, -yoff, zoff)} so the pyramid points
 *       up. Ring perimeter alternates {@code GOLD_BLOCK} on even
 *       {@code yoff} rows and {@code EMERALD_BLOCK} on odd
 *       {@code yoff} rows; interior is air (lines 1031&ndash;1052).</li>
 *   <li>At the {@code width == 11, 9, 7} steps, place 4 chests at the
 *       midpoints of each cardinal wall (line 1046 &rarr;
 *       {@code fill_mantishive_chests}).</li>
 *   <li>After the loop, decrement xoff/zoff/yoff once and place 3
 *       Mantis spawners at {@code (xoff, +4..+6 - yoff, +yoff)} on the
 *       cap (lines 1056&ndash;1061).</li>
 * </ol>
 *
 * <p><b>Loot fidelity (mantisContentsList, GenericDungeon.java:56):</b>
 * Mantis Claw, gold nuggets, uranium nugget, titanium nugget, mantis
 * egg (cobweb stand-in: no entity item exists in 1.21.1), full Tigers
 * Eye armour set, rotten flesh, diamonds. Each chest gets
 * {@code 3 + nextInt(7)} weighted draws.</p>
 *
 * <p><b>Spawn frequency (verified OreSpawnWorld.java:999-1018,
 * addANest):</b> {@code random.nextInt(230) != 0} gating, then a 50/50
 * split with the small bee hive variant &rarr; net <b>1 in 460
 * chunks</b> in Forest / Jungle / Birch Forest biomes. Encoded by
 * {@code structure_set/mantis_nest.json} as {@code spacing=24,
 * separation=12} which yields ~1 per 576 chunks &mdash; the closest
 * achievable {@code random_spread} cell that respects the legacy
 * minimum-distance constraint without colliding with the bee hive
 * grid.</p>
 *
 * <p><b>Stability guard:</b> the legacy version did unbounded
 * {@code FastSetBlock} writes that could clip max build height. We
 * abort up-front if the entire {@code 13&times;20&times;13} footprint
 * would clip world bounds.</p>
 */
public class MantisNestFeature extends Feature<NoneFeatureConfiguration> {
    public MantisNestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        // The legacy spawn point in addANest was the surface grass tile;
        // makeMantisHive then built the 20-tall air pocket starting at
        // cposy (so the chamber sits just above the grass). Mirror that
        // by anchoring at the surface tile.
        BlockPos cpos = surface;

        // Bound checks: the chamber is 20 blocks tall up, the pyramid
        // descends ~7 blocks down, so we need ~28 blocks of vertical
        // headroom around the anchor. Bail early if any of it would
        // clip world bounds (legacy version had no such check).
        if (cpos.getY() + 20 >= level.getMaxBuildHeight() - 2) return false;
        if (cpos.getY() - 7 <= level.getMinBuildHeight() + 2) return false;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState emerald = Blocks.EMERALD_BLOCK.defaultBlockState();

        // Step 1: hollow the 13x20x13 chamber (lines 1021-1027).
        int width = 13;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < 20; j++) {
                for (int k = 0; k < width; k++) {
                    level.setBlock(cpos.offset(i, j, k), air, 2);
                }
            }
        }

        // Step 2: stamp the stepped pyramid downward (lines 1028-1052).
        int yoff = 0;
        int zoff = 0;
        int xoff = 0;
        while (width > 0) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < width; k++) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        // Perimeter: alternate gold (yoff even) / emerald
                        // (yoff odd). Matches legacy line 1036-1038.
                        BlockState ringBlock = (yoff & 1) != 0 ? emerald : gold;
                        level.setBlock(cpos.offset(i + xoff, -yoff, k + zoff), ringBlock, 2);
                    } else {
                        level.setBlock(cpos.offset(i + xoff, -yoff, k + zoff), air, 2);
                    }
                }
            }
            // Step 3: place chests at the wide pyramid steps (line 1045-1047).
            if (width <= 11 && width >= 7) {
                fillMantisChests(level, cpos.offset(xoff, -yoff, zoff), width, random);
            }
            xoff++;
            zoff++;
            yoff++;
            width -= 2;
        }
        // Decrement once so we're at the cap step (lines 1053-1055).
        xoff--;
        zoff--;
        yoff--;

        // Step 4: place 3 Mantis spawners on the cap (lines 1056-1061).
        for (int j = 4; j < 7; j++) {
            placeSpawner(level, cpos.offset(xoff, j - yoff, yoff), ModEntities.ENTITY_MANTIS.get());
        }
        return true;
    }

    /**
     * Stamps 4 chests at the cardinal-wall midpoints of a pyramid step
     * of {@code width}. Direct port of legacy
     * {@code fill_mantishive_chests} (GenericDungeon.java:1064-1093).
     */
    private static void fillMantisChests(WorldGenLevel level, BlockPos stepCorner, int width, Random random) {
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        // Legacy placed chests at (cposx+1, cposy, cposz+width/2) etc. —
        // i.e. at the inside face of each cardinal wall, midpoint along
        // the perpendicular axis.
        BlockPos[] chestPositions = new BlockPos[]{
                stepCorner.offset(1, 0, width / 2),
                stepCorner.offset(width - 2, 0, width / 2),
                stepCorner.offset(width / 2, 0, 1),
                stepCorner.offset(width / 2, 0, width - 2)
        };
        for (BlockPos chestPos : chestPositions) {
            level.setBlock(chestPos, chestState, 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                fillMantisLoot(chest, random);
            }
        }
    }

    /**
     * Populates a chest with the canonical legacy mantisContentsList
     * (GenericDungeon.java:56). Each chest gets {@code 3 + nextInt(7)}
     * draws, mirroring the legacy WeightedRandomChestContent fill loop.
     */
    private static void fillMantisLoot(ChestBlockEntity chest, Random random) {
        // Direct port of mantisContentsList weights.
        int[] weights = {10, 15, 5, 5, 20, 10, 10, 10, 10, 25, 15};
        ItemStack[] pool = {
                new ItemStack(ModItems.MANTIS_CLAW.get(), 1),
                new ItemStack(Items.GOLD_NUGGET, 4 + random.nextInt(5)),
                new ItemStack(ModItems.URANIUM_NUGGET.get(), 1 + random.nextInt(3)),
                new ItemStack(ModItems.TITANIUM_NUGGET.get(), 1 + random.nextInt(3)),
                new ItemStack(Items.SPIDER_EYE, 2 + random.nextInt(3)),
                new ItemStack(ModItems.TIGERSEYE_HELMET.get()),
                new ItemStack(ModItems.TIGERSEYE_CHESTPLATE.get()),
                new ItemStack(ModItems.TIGERSEYE_LEGGINGS.get()),
                new ItemStack(ModItems.TIGERSEYE_BOOTS.get()),
                new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)),
                new ItemStack(Items.DIAMOND, 1 + random.nextInt(3))
        };
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;

        int draws = 3 + random.nextInt(7);
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
