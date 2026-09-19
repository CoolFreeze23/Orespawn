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
 * ENT-S-170 — the Frog is placed by the spawn list the biome carries it in, as the 1.7.10 spawner placed it.
 *
 * <p>1.7.10 placed a natural spawn by the CREATURE TYPE of the list it was drawn from
 * ({@code SpawnerAnimals.canCreatureTypeSpawnAtLocation}): a {@code waterCreature} entry needed liquid at the cell,
 * liquid below and no normal cube above; an {@code ambient} or {@code creature} entry needed a solid top surface below,
 * no normal cube or liquid at the cell and no normal cube above. The Frog is a water creature in Utopia and the Village
 * (orig BiomeGenUtopianPlains.java:135, w5 4-6), in the Crystal plains (w1 3-5) and in the overworld rivers and swamps
 * (orig OreSpawnMain.java:4963, :4966, w20), and an ambient creature in rivers, jungles and swamps (:4964, :4965,
 * :4967, w3 / w3 / w2); the port's companion modifiers carry the overworld entries in those very lists through
 * {@code orespawn:add_spawns_in_category} ({@code ModBiomeModifiers}), since NeoForge's own add_spawns files an entry
 * under the entity's category. 1.21.1 keys the placement by ENTITY TYPE, one {@link SpawnPlacementType} per type, and the port had
 * registered {@link SpawnPlacementTypes#ON_GROUND}: so the water-creature pass, which runs every tick, put Frogs on
 * land all over Utopia, and since a Frog counts as a CREATURE (its {@code MobCategory}; orig {@code EntityAnimal}
 * counted the same way) that pass never met its cap. That is the overspawn of GitHub issue #2.
 *
 * <p>This placement type reads the biome spawner lists at the position: the water rule applies where the Frog is
 * listed as a water creature, the ground rule where an ambient or creature list carries it, either where both do; a
 * biome that lists it in neither (a spawner block, a spawn egg, a datapack with its own lists) keeps the ground rule
 * the port had. The entity's own {@code checkSpawnRules} (orig Frog.java:240-251: Y at least 50, daytime, the Crystal
 * 1-in-20, at most five buddies within 20 x 8 x 20) runs after this as before; orig Frog.java has no grass-or-light
 * rule, so the registration carries none.
 */
public final class FrogSpawnPlacement implements SpawnPlacementType {
    public static final FrogSpawnPlacement INSTANCE = new FrogSpawnPlacement();

    private FrogSpawnPlacement() {
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
