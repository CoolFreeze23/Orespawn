package de.dertoaster.multihitboxlib.example.init;

import de.dertoaster.multihitboxlib.Constants;
import de.dertoaster.multihitboxlib.MHLibMod;
import de.dertoaster.multihitboxlib.example.entity.Anjanath;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MHLibExampleEntities {

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MODID);

	// AnjanathALib (AzureLib variant) intentionally absent: see project-level
	// build.gradle / neoforge.mods.toml notes. Class-loading AnjanathALib at
	// runtime causes NoClassDefFoundError when AzureLib isn't on the
	// classpath (it implements mod.azure.azurelib.GeoEntity), so the
	// vendored MHLib in OreSpawn ships only the Geckolib example.
	public static final Supplier<EntityType<Anjanath>> ANJANATH = registerSized(Anjanath::new, "anjanath", 6, 5, 1);

	protected static <T extends Entity>  Supplier<EntityType<T>> registerSized(EntityFactory<T> factory, final String entityName, float width, float height, int updateInterval) {
		Supplier<EntityType<T>> result = ENTITY_TYPES.register(entityName, () -> EntityType.Builder
				.<T>of(factory, MobCategory.MISC)
				.sized(width, height)
				.setTrackingRange(128)
				.clientTrackingRange(64)
				.updateInterval(updateInterval)
				.setShouldReceiveVelocityUpdates(true)
				.build(MHLibMod.prefix(entityName).toString()));

		return result;
	}

	// NeoForge 1.21.1 dropped FMLJavaModLoadingContext.get().getModEventBus().
	// The mod constructor (now @Mod(...)-injected with IEventBus) must pass
	// its bus in explicitly. The no-arg overload is kept as a guard so any
	// stale upstream caller fails loudly instead of silently doing nothing.
	public static void registerEntityTypes() {
		throw new UnsupportedOperationException(
				"MHLibExampleEntities.registerEntityTypes() is unsupported on NeoForge 1.21.1. " +
				"Use registerEntityTypes(IEventBus) and pass the mod event bus from your @Mod constructor.");
	}

	public static void registerEntityTypes(IEventBus modEventBus) {
		ENTITY_TYPES.register(modEventBus);
	}

}
