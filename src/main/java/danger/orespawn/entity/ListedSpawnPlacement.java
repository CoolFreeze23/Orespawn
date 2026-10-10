package danger.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.MobSpawnSettings;

/**
 * A species is placed by the spawn list the biome carries it in, as the 1.7.10 spawner placed it (ENT-S-170, the Frog;
 * WGEN-086, every species the port's lists carry).
 *
 * <p>1.7.10 placed a natural spawn by the CREATURE TYPE of the list it was drawn from
 * ({@code SpawnerAnimals.canCreatureTypeSpawnAtLocation}): a {@code waterCreature} entry needed liquid at the cell,
 * liquid below and no normal cube above; an {@code ambient}, {@code creature} or {@code monster} entry needed a solid
 * top surface below, no normal cube or liquid at the cell and no normal cube above, the flyers too. The original listed
 * a species in lists of another type than its class (the Frog a water creature in Utopia, the Village and the Crystal
 * plains and in the overworld's rivers and swamps, an ambient creature in rivers, jungles and swamps; the Crab, the
 * Lizard and the Whale water creatures; the flyers ambient), and the port's lists keep them there
 * ({@code orespawn:spawn_table}, {@code ModBiomeModifiers}). 1.21.1 keys the placement by ENTITY TYPE, one
 * {@link SpawnPlacementType} per type: with {@link SpawnPlacementTypes#ON_GROUND} for the Frog the water-creature
 * pass, which runs every tick, put Frogs on land all over Utopia (GitHub issue #2).
 *
 * <p>This placement type reads the biome spawner lists at the position: the water rule applies where the species is
 * listed as a water creature, the ground rule where an ambient, creature or monster list carries it, either where both
 * do; a biome that lists it in neither (a spawner block, a spawn egg, a datapack with its own lists) keeps the ground
 * rule. The entity's own rule runs after this ({@code OriginalSpawnRules}).
 */
public final class ListedSpawnPlacement implements SpawnPlacementType {
    public static final ListedSpawnPlacement INSTANCE = new ListedSpawnPlacement();

    private ListedSpawnPlacement() {
    }

    @Override
    public boolean isSpawnPositionOk(LevelReader level, BlockPos pos, @Nullable EntityType<?> entityType) {
        if (entityType == null) return false;
        MobSpawnSettings settings = level.getBiome(pos).value().getMobSettings();
        return isSpawnPositionOk(level, pos, entityType,
                listedAsWater(settings, entityType), listedOnGround(settings, entityType));
    }

    /**
     * The per-list rule, exposed for the gametests: {@code waterListed} means a water list of the biome carries the
     * type; {@code groundListed} means an ambient, creature or monster list does.
     */
    public static boolean isSpawnPositionOk(LevelReader level, BlockPos pos, EntityType<?> type,
                                            boolean waterListed, boolean groundListed) {
        if (!waterListed && !groundListed) {
            return SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type);
        }
        if (waterListed && isTwoDeepLiquid(level, pos)) return true;
        return groundListed && SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type);
    }

    /**
     * orig SpawnerAnimals.canCreatureTypeSpawnAtLocation, the water branch: liquid at the cell and below it, no normal
     * cube above ({@code isRedstoneConductor} is the 1.21.1 reading of 1.7.10 {@code Block.isNormalCube}).
     */
    public static boolean isTwoDeepLiquid(LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(pos).liquid()
                && level.getBlockState(pos.below()).liquid()
                && !level.getBlockState(above).isRedstoneConductor(level, above);
    }

    public static boolean listedAsWater(MobSpawnSettings settings, EntityType<?> type) {
        return listed(settings, MobCategory.WATER_CREATURE, type)
                || listed(settings, MobCategory.WATER_AMBIENT, type);
    }

    public static boolean listedOnGround(MobSpawnSettings settings, EntityType<?> type) {
        return listed(settings, MobCategory.CREATURE, type)
                || listed(settings, MobCategory.AMBIENT, type)
                || listed(settings, MobCategory.MONSTER, type);
    }

    private static boolean listed(MobSpawnSettings settings, MobCategory category, EntityType<?> type) {
        for (MobSpawnSettings.SpawnerData data : settings.getMobs(category).unwrap()) {
            if (data.type == type) return true;
        }
        return false;
    }
}
