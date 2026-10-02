package danger.orespawn.compat;

import com.mojang.logging.LogUtils;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.item.SpearTier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * MOD-043: OreSpawn's spears, registered only while Mounts of Mayhem is loaded. OreSpawnMod calls {@link #register}
 * then and never otherwise, so this class and OreSpawnSpear, the only two that name the mod's classes, load only with
 * it. A world keeps OreSpawn's spears only while the mod is installed; a server and its clients both need it. Built
 * and tested against {@link #TESTED}; a newer version still gets the spears, with a line in the log.
 */
public final class MayhemSpears {
    public static final String MOD_ID = "mounts_of_mayhem";
    /** The version of Mounts of Mayhem the spears are built and tested against. */
    public static final String TESTED = "1.9.8";

    private static final DeferredRegister.Items SPEARS = DeferredRegister.createItems(OreSpawnMod.MOD_ID);

    private MayhemSpears() {
    }

    public static void register(IEventBus bus) {
        for (SpearTier tier : SpearTier.values()) {
            DeferredItem<OreSpawnSpear> item = SPEARS.register(tier.id(), () -> new OreSpawnSpear(tier));
            SpearTier.registered(tier, item);
        }
        SPEARS.register(bus);
        ModList.get().getModContainerById(MOD_ID).ifPresent(container -> {
            ArtifactVersion loaded = container.getModInfo().getVersion();
            if (loaded.compareTo(new DefaultArtifactVersion(TESTED)) > 0) {
                LogUtils.getLogger().info("OreSpawn spears: Mounts of Mayhem {} is newer than {}, the version they are"
                        + " tested against; they are registered all the same", loaded, TESTED);
            }
        });
    }
}
