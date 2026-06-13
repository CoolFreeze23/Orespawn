package danger.orespawn;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Dimension {@link ResourceKey}s for the six OreSpawn dimensions, shared by every
 * spawn gate and teleport check.
 *
 * <p>Maps the original 1.7.10 numeric dimension IDs (OreSpawnMain.java:1595-1600,
 * provider registrations :5379-5390) to the port's dimension JSONs:
 * <ul>
 *   <li>DimensionID  — WorldProviderOreSpawn  "Dimension-Utopia"        → {@code orespawn:utopia}</li>
 *   <li>DimensionID2 — WorldProviderOreSpawn2 "Dimension-Extreme"       → {@code orespawn:mining}</li>
 *   <li>DimensionID3 — WorldProviderOreSpawn3 "Dimension-VillageMania"  → {@code orespawn:village}</li>
 *   <li>DimensionID4 — WorldProviderOreSpawn4 "Dimension-Islands"       → {@code orespawn:islands}</li>
 *   <li>DimensionID5 — WorldProviderOreSpawn5 "Dimension-Crystal"       → {@code orespawn:crystal}</li>
 *   <li>DimensionID6 — WorldProviderOreSpawn6 "Dimension-Chaos"         → {@code orespawn:chaos}</li>
 * </ul>
 */
public final class ModDimensionKeys {
    public static final ResourceKey<Level> UTOPIA = create("utopia");
    public static final ResourceKey<Level> MINING = create("mining");
    public static final ResourceKey<Level> VILLAGE = create("village");
    public static final ResourceKey<Level> ISLANDS = create("islands");
    public static final ResourceKey<Level> CRYSTAL = create("crystal");
    public static final ResourceKey<Level> CHAOS = create("chaos");

    private ModDimensionKeys() {
    }

    private static ResourceKey<Level> create(String path) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path));
    }

    /**
     * True when the accessor belongs to the given dimension. Returns {@code false}
     * when the dimension cannot be determined (non-server accessor), matching the
     * original int-compare semantics where an unknown world never equals a mod
     * dimension ID. Server side only in practice (spawn checks run there).
     */
    public static boolean isIn(LevelAccessor level, ResourceKey<Level> dimension) {
        if (level instanceof ServerLevelAccessor server) {
            return server.getLevel().dimension() == dimension;
        }
        return false;
    }
}
