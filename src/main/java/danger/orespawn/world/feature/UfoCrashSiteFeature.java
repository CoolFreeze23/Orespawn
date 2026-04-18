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
 * UFO Crash Site — Overworld surface companion to the
 * {@link WtfAlienDungeonFeature}. The legacy 1.7.10 source described UFO
 * crash sites in the changelog but never implemented them as a discrete
 * generator; this feature gives the Alien threat an above-ground entry
 * point so players can encounter Aliens without first finding the Mine
 * Dimension lapis dungeon.
 *
 * <p>The shape is a flying-saucer disc: an oval iron-block hull with a
 * glass dome on top, half-buried at an angle so it looks crashed.
 * Inside is a single Alien spawner and a partial-loot chest with the
 * lighter Alien scrap drops (matching the legacy alien.json loot table:
 * gunpowder + ender pearls + iron).</p>
 */
public class UfoCrashSiteFeature extends Feature<NoneFeatureConfiguration> {
    private static final int RADIUS = 6;
    private static final int HULL_HEIGHT = 2;

    public UfoCrashSiteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());

        if (surface.getY() + 5 >= level.getMaxBuildHeight() - 4) return false;
        BlockState floor = level.getBlockState(surface.below());
        if (floor.isAir() || floor.liquid()) return false;

        BlockState hull = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState rim = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState dome = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();

        // Tilt: south side sits 2 blocks lower than north (crashed nose-down).
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq > RADIUS * RADIUS) continue;
                int tilt = -dz / 3;
                BlockPos floorPos = surface.offset(dx, tilt, dz);
                boolean rimRing = distSq > (RADIUS - 1) * (RADIUS - 1);
                level.setBlock(floorPos, rimRing ? rim : hull, 2);

                if (distSq <= (RADIUS - 2) * (RADIUS - 2)) {
                    // Hollow interior
                    for (int dy = 1; dy <= HULL_HEIGHT; dy++) {
                        level.setBlock(floorPos.above(dy), Blocks.AIR.defaultBlockState(), 2);
                    }
                    // Dome cap
                    if (distSq <= (RADIUS - 3) * (RADIUS - 3)) {
                        level.setBlock(floorPos.above(HULL_HEIGHT + 1), dome, 2);
                    }
                }
            }
        }

        BlockPos centre = surface;
        // Centre console: lapis pillar + spawner on top
        level.setBlock(centre, lapis, 2);
        level.setBlock(centre.above(), lapis, 2);
        BlockPos spawnerPos = centre.above(2);
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(ModEntities.ALIEN.get(),
                    null, ctx.random(), spawnerPos);
        }

        // Loot chest in the rear of the hull
        BlockPos chestPos = surface.offset(0, 1, RADIUS - 2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.GUNPOWDER, 4 + ctx.random().nextInt(8)));
            chest.setItem(2, new ItemStack(Items.ENDER_PEARL, 1 + ctx.random().nextInt(3)));
            chest.setItem(4, new ItemStack(Items.IRON_INGOT, 4 + ctx.random().nextInt(6)));
            chest.setItem(6, new ItemStack(ModItems.URANIUM_NUGGET.get(), 1 + ctx.random().nextInt(3)));
            chest.setItem(8, new ItemStack(ModItems.ALIEN_SPAWN_EGG.get(), 1));
        }
        return true;
    }
}
