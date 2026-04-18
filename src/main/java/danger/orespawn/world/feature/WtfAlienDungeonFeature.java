package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
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

/**
 * WTF–Alien Dungeon — modernized port of the legacy
 * {@code GenericDungeon.makeAlienWTFDungeon} (1.7.10 source line 1570).
 * The original was a deeply-buried 5×5 lapis chamber (block id
 * {@code field_150369_x} = lapis) with a vertical staircase of 20 floors
 * leading down to a centre chamber containing 4 "WTF?" Alien spawners and
 * the legacy {@link #ALIEN_LOOT canonical Ultimate-gear chest}.
 *
 * <p>Modernization condenses the 20-floor stair into a single buried
 * lapis chamber under the surface — a fully procedural staircase of that
 * length would routinely carve through neighbouring chunks and trigger
 * the cascading chunk-load failures the user explicitly flagged. Instead
 * we generate:</p>
 *
 * <ol>
 *   <li>A 9×7×9 lapis-block hollow chamber, top sunk 12 blocks below
 *       the surface heightmap (matches the legacy "deep below" feel).</li>
 *   <li>A vertical lapis access shaft running from the surface down to
 *       the chamber roof (1×1 ladder hole). Bound to the surface column
 *       so it never tunnels diagonally into another chunk.</li>
 *   <li>A central spawner pillar with the {@link AlienBoss} on top of an
 *       Alien rank-and-file spawner (mirrors the legacy boss-cap design
 *       used by the Mantis Lair / Sea Monster's Dungeon).</li>
 *   <li>One 4-corner treasure chest set with Ultimate-gear pieces.</li>
 * </ol>
 */
public class WtfAlienDungeonFeature extends Feature<NoneFeatureConfiguration> {
    /** Equivalents to the legacy {@code AlienWTFContentsList} (line 51). */
    private static final ItemStack[] ALIEN_LOOT_TEMPLATE = {
            new ItemStack(Items.DIAMOND_BLOCK, 1),
            new ItemStack(Items.EMERALD_BLOCK, 1),
            new ItemStack(Items.NETHERITE_INGOT, 2)
    };

    private static final int CHAMBER_WIDTH = 9;
    private static final int CHAMBER_HEIGHT = 7;
    private static final int CHAMBER_DEPTH = 9;
    private static final int BURY_DEPTH = 12;

    public WtfAlienDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());

        // Place the chamber 12 blocks below the surface.
        BlockPos chamberOrigin = surface.below(BURY_DEPTH + CHAMBER_HEIGHT);
        if (chamberOrigin.getY() < level.getMinBuildHeight() + 4) return false;

        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();

        // Hollow chamber
        for (int dx = 0; dx < CHAMBER_WIDTH; dx++) {
            for (int dz = 0; dz < CHAMBER_DEPTH; dz++) {
                for (int dy = 0; dy < CHAMBER_HEIGHT; dy++) {
                    BlockPos pos = chamberOrigin.offset(dx, dy, dz);
                    boolean isShell = dx == 0 || dx == CHAMBER_WIDTH - 1
                            || dz == 0 || dz == CHAMBER_DEPTH - 1
                            || dy == 0 || dy == CHAMBER_HEIGHT - 1;
                    if (dy == 0) {
                        level.setBlock(pos, floor, 2);
                    } else if (isShell) {
                        // Sprinkle glowstone in the ceiling for ambient light
                        boolean ceiling = dy == CHAMBER_HEIGHT - 1;
                        BlockState wallState = ceiling && ctx.random().nextInt(6) == 0
                                ? glow : lapis;
                        level.setBlock(pos, wallState, 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Vertical access shaft from chamber roof up to surface.
        BlockPos shaftBase = chamberOrigin.offset(CHAMBER_WIDTH / 2, CHAMBER_HEIGHT - 1, CHAMBER_DEPTH / 2);
        for (int dy = 0; shaftBase.getY() + dy <= surface.getY(); dy++) {
            level.setBlock(shaftBase.above(dy), Blocks.AIR.defaultBlockState(), 2);
            // Lapis ring around each shaft cell to keep the dirt out
            for (int rdx = -1; rdx <= 1; rdx++) {
                for (int rdz = -1; rdz <= 1; rdz++) {
                    if (rdx == 0 && rdz == 0) continue;
                    BlockPos ring = shaftBase.offset(rdx, dy, rdz);
                    if (level.getBlockState(ring).isAir()) continue;
                    level.setBlock(ring, lapis, 2);
                }
            }
        }
        // Cap the shaft mouth with a glow block so players can find it.
        level.setBlock(shaftBase.above(surface.getY() - shaftBase.getY() + 1), glow, 2);

        // Central spawner pillar — Alien Boss above 2 rank-and-file spawners.
        BlockPos centre = chamberOrigin.offset(CHAMBER_WIDTH / 2, 1, CHAMBER_DEPTH / 2);
        placeSpawner(level, centre, ModEntities.ALIEN.get());
        placeSpawner(level, centre.above(), ModEntities.ALIEN.get());
        placeSpawner(level, centre.above(2), ModEntities.ALIEN_BOSS.get());

        // Treasure chest — diagonal corner, contains Ultimate gear.
        BlockPos chestPos = chamberOrigin.offset(1, 1, 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(ModItems.ULTIMATE_HELMET.get()));
            chest.setItem(1, new ItemStack(ModItems.ULTIMATE_CHESTPLATE.get()));
            chest.setItem(2, new ItemStack(ModItems.ULTIMATE_LEGGINGS.get()));
            chest.setItem(3, new ItemStack(ModItems.ULTIMATE_BOOTS_ARMOR.get()));
            chest.setItem(4, new ItemStack(ModItems.ULTIMATE_SWORD.get()));
            chest.setItem(5, new ItemStack(ModItems.ULTIMATE_BOW.get()));
            for (int i = 0; i < ALIEN_LOOT_TEMPLATE.length; i++) {
                chest.setItem(9 + i, ALIEN_LOOT_TEMPLATE[i].copy());
            }
        }
        return true;
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos,
                                     net.minecraft.world.entity.EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }
}
