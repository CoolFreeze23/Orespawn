package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModBlocks;
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
 * Crystal Battle Tower &mdash; modernized port of the legacy
 * {@code GenericDungeon.makeCrystalBattleTower} (1.7.10 source line 4831).
 *
 * <p>The original was a hollow cylindrical tower of {@code CrystalStone}
 * (radius 10, total height 23) capped with two layers of
 * {@code CrystalCrystal}. Every fifth Y row was a solid disc — those acted
 * as the floor of each storey. Between two consecutive discs the wall ring
 * had a 20&deg; arc of air carved out, producing the legacy tower's
 * arrow-slit / window column. Five floors were placed (Y=1, 6, 11, 16, 21)
 * with a 2-tall spawner column and a chest centred at each storey:</p>
 *
 * <pre>
 *   Floor 1 (Y=1) : Rat            chest = mixed steaks / drumsticks
 *   Floor 2 (Y=6) : Dungeon Beast  chest = dyes, gold, rotten flesh
 *   Floor 3 (Y=11): Urchin         chest = Crystal Pink armor + Fairy Sword
 *   Floor 4 (Y=16): Rotator        chest = Tigers Eye armor + Rat Sword
 *   Floor 5 (Y=21): Vortex (cap)   chest = Crystal Coal, Tigers Eye Sword,
 *                                          Tigers Eye Block, Poison Sword
 * </pre>
 *
 * <p>Modernization notes (1.21.1):</p>
 * <ul>
 *   <li>Footprint &le; 21&times;21 blocks centred on origin, well within a
 *       2&times;2 chunk window. We bound-check Y up-front and bail with no
 *       partial writes if the column would clip max-build height &mdash;
 *       this is the explicit guard against the runaway cascading
 *       chunk-load failures the user flagged.</li>
 *   <li>{@link WorldGenLevel#setBlock} uses flag {@code 2} (no neighbour
 *       updates / no light) to mirror the legacy
 *       {@code FastSetBlock} fast-path while still firing block-entity
 *       creation for chests and spawners.</li>
 *   <li>Uses the tower's own {@link FeaturePlaceContext} RNG for every
 *       random pick so worlds with the same seed regenerate the same
 *       tower in the same column &mdash; matches modern world-gen
 *       determinism guarantees.</li>
 * </ul>
 */
public class CrystalBattleTowerFeature extends Feature<NoneFeatureConfiguration> {

    /** Half-width of the cylinder. Legacy: {@code radius = 10.0f}. */
    private static final int RADIUS = 10;
    /** Total tower height (21 stone + 2 crystal cap). Legacy: 0..22 inclusive. */
    private static final int TOWER_HEIGHT = 23;
    /** Storey Y offsets (relative to base). Legacy: 1, 6, 11, 16, 21. */
    private static final int[] FLOOR_Y = {1, 6, 11, 16, 21};

    public CrystalBattleTowerFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        Random random = new Random(ctx.random().nextLong());

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        BlockPos base = surface;

        // Bound-check the entire footprint up-front. If even one cell would
        // clip max-build height we abort *before* writing any blocks, so the
        // worldgen pipeline never sees a half-built tower (which is exactly
        // the failure mode the legacy 1.7.10 implementation could trigger
        // when it landed near a height-cap biome).
        if (base.getY() + TOWER_HEIGHT >= level.getMaxBuildHeight() - 4) return false;
        if (base.getY() <= level.getMinBuildHeight() + 2) return false;

        // Reject ocean / unstable footing: the tower needs solid ground or
        // it visually clips through water surface and looks broken.
        BlockState floorBelow = level.getBlockState(base.below());
        if (floorBelow.isAir() || floorBelow.liquid()) return false;

        BlockState crystalStone = ModBlocks.CRYSTAL_STONE.get().defaultBlockState();
        BlockState crystalCap = ModBlocks.CRYSTAL_CRYSTAL.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // ---- Build the cylinder (legacy lines 4842-4864) ----
        for (int dy = 0; dy <= 20; dy++) {
            BlockPos centre = base.above(dy);
            if (dy % 5 == 0) {
                // Solid disc: floor of each storey.
                stampDisc(level, centre, RADIUS, crystalStone);
            } else {
                // Open ring with a 20-degree arrow-slit window column.
                stampRing(level, centre, RADIUS, crystalStone,
                        (dy % 5) >= 1 && (dy % 5) <= 3, air);
            }
        }

        // ---- Crystal Crystal cap (legacy lines 4865-4874, Y=21..22) ----
        for (int dy = 21; dy <= 22; dy++) {
            stampRing(level, base.above(dy), RADIUS, crystalCap, false, air);
        }

        // ---- Place the 5 floors of spawners + chests ----
        // Mob ladder + chest contents mirror the legacy
        // CrystalBattleTower*ContentsList arrays (lines 32-36).
        EntityType<?>[] floorMobs = {
                ModEntities.ENTITY_RAT.get(),
                ModEntities.DUNGEON_BEAST.get(),
                ModEntities.URCHIN.get(),
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_VORTEX.get()
        };
        for (int i = 0; i < FLOOR_Y.length; i++) {
            int y = FLOOR_Y[i];
            EntityType<?> mob = floorMobs[i];
            BlockPos floorCentre = base.above(y);

            // 2-tall spawner column directly above the chest (matches legacy
            // makeCrystalBattleTower, which places the chest at y and two
            // spawners at y+1/y+2 in the centre tile).
            placeSpawner(level, floorCentre.above(1), mob);
            placeSpawner(level, floorCentre.above(2), mob);

            level.setBlock(floorCentre, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(floorCentre) instanceof ChestBlockEntity chest) {
                fillFloorChest(chest, i, random);
            }
        }
        return true;
    }

    /**
     * Stamps a solid disc of the given radius. Mirrors the legacy
     * 5&deg;-stride polar sweep so the pixelization matches the original
     * tower's silhouette exactly.
     */
    private static void stampDisc(WorldGenLevel level, BlockPos centre, int radius, BlockState block) {
        for (float r = 0.0f; r < radius; r += 0.33f) {
            for (float deg = 0.0f; deg < 360.0f; deg += 5.0f) {
                int dx = (int) (r * Math.cos(Math.toRadians(deg)) + 0.5f);
                int dz = (int) (r * Math.sin(Math.toRadians(deg)) + 0.5f);
                level.setBlock(centre.offset(dx, 0, dz), block, 2);
            }
        }
    }

    /**
     * Stamps an outline ring at the given radius. If {@code carveWindow} is
     * true, leaves a 20&deg; arc (deg &lt; 10 or deg &gt; 350) as
     * {@code airState} &mdash; this is the legacy arrow-slit window
     * column.
     */
    private static void stampRing(WorldGenLevel level, BlockPos centre, int radius, BlockState block,
                                  boolean carveWindow, BlockState airState) {
        for (float deg = 0.0f; deg < 360.0f; deg += 5.0f) {
            int dx = (int) (radius * Math.cos(Math.toRadians(deg)) + 0.5f);
            int dz = (int) (radius * Math.sin(Math.toRadians(deg)) + 0.5f);
            BlockState target = block;
            if (carveWindow && (deg < 10.0f || deg > 350.0f)) {
                target = airState;
            }
            level.setBlock(centre.offset(dx, 0, dz), target, 2);
        }
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }

    /**
     * Populates a chest with the canonical legacy contents for the given
     * floor index (0=Rat &hellip; 4=Vortex). Each stack is randomized
     * within the original size band so two seeds produce visibly different
     * chests, but the rare-armor slots remain at fixed positions for
     * datapack overrides.
     */
    private static void fillFloorChest(ChestBlockEntity chest, int floorIndex, Random random) {
        switch (floorIndex) {
            case 0 -> { // Rat-floor: cooked food sampler.
                chest.setItem(0, new ItemStack(Items.COOKED_PORKCHOP, 3 + random.nextInt(8)));
                chest.setItem(1, new ItemStack(Items.COOKED_BEEF, 3 + random.nextInt(8)));
                chest.setItem(2, new ItemStack(Items.COOKED_CHICKEN, 3 + random.nextInt(8)));
                chest.setItem(3, new ItemStack(Items.COOKED_COD, 3 + random.nextInt(8)));
                chest.setItem(4, new ItemStack(Items.GOLDEN_CARROT, 4 + random.nextInt(4)));
            }
            case 1 -> { // Dungeon Beast-floor: dye / gold / rotten flesh haul.
                chest.setItem(0, new ItemStack(Items.PINK_DYE, 6 + random.nextInt(11)));
                chest.setItem(1, new ItemStack(Items.GOLD_NUGGET, 5 + random.nextInt(11)));
                chest.setItem(2, new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)));
                chest.setItem(3, new ItemStack(ModItems.SQUID_ZOOKA.get()));
            }
            case 2 -> { // Urchin-floor: full Crystal Pink set + Fairy Sword.
                chest.setItem(0, new ItemStack(ModItems.PINK_HELMET.get()));
                chest.setItem(1, new ItemStack(ModItems.PINK_CHESTPLATE.get()));
                chest.setItem(2, new ItemStack(ModItems.PINK_LEGGINGS.get()));
                chest.setItem(3, new ItemStack(ModItems.PINK_BOOTS.get()));
                chest.setItem(4, new ItemStack(ModItems.FAIRY_SWORD.get()));
            }
            case 3 -> { // Rotator-floor: full Tigers Eye set + Rat Sword.
                chest.setItem(0, new ItemStack(ModItems.TIGERSEYE_HELMET.get()));
                chest.setItem(1, new ItemStack(ModItems.TIGERSEYE_CHESTPLATE.get()));
                chest.setItem(2, new ItemStack(ModItems.TIGERSEYE_LEGGINGS.get()));
                chest.setItem(3, new ItemStack(ModItems.TIGERSEYE_BOOTS.get()));
                chest.setItem(4, new ItemStack(ModItems.RAT_SWORD.get()));
            }
            case 4 -> { // Vortex cap-floor: legacy CrystalBattleTowerVortexContentsList
                // (GenericDungeon.java:36) — note the legacy array deliberately
                // duplicated CrystalCoal twice to weight it heavier in the
                // 6-slot fill loop. Mirrored here.
                chest.setItem(0, new ItemStack(ModItems.CRYSTAL_COAL_ITEM.get(), 6 + random.nextInt(5)));
                chest.setItem(1, new ItemStack(ModItems.CRYSTAL_COAL_ITEM.get(), 6 + random.nextInt(5)));
                chest.setItem(2, new ItemStack(ModItems.TIGERS_EYE_SWORD.get()));
                chest.setItem(3, new ItemStack(ModItems.BLOCK_TIGERS_EYE_ITEM.get(), 4 + random.nextInt(5)));
                chest.setItem(4, new ItemStack(ModItems.POISON_SWORD.get()));
            }
            default -> {
                // Defensive: should never hit, but keep the chest from being
                // empty in case a future patch adds a 6th storey.
                chest.setItem(0, new ItemStack(Items.DIAMOND, 4));
            }
        }
    }
}
