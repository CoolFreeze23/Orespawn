package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * DeferredRegister for OreSpawn's custom {@link Feature} types. Each entry
 * here corresponds to a {@code data/orespawn/worldgen/configured_feature/*.json}
 * referencing it via its registry id (e.g. {@code "type": "orespawn:mantis_nest"}).
 *
 * <p>Registered on the mod event bus from {@link OreSpawnMod}.</p>
 */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, MantisNestFeature> MANTIS_NEST =
            FEATURES.register("mantis_nest", () -> new MantisNestFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, BeehiveFeature> BEEHIVE =
            FEATURES.register("beehive", () -> new BeehiveFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
