package de.dertoaster.multihitboxlib.example.init;

import de.dertoaster.multihitboxlib.Constants;
import de.dertoaster.multihitboxlib.MHLibMod;
import de.dertoaster.multihitboxlib.example.entity.Anjanath;
import de.dertoaster.multihitboxlib.example.entity.AnjanathALib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MHLibExampleEntities {
	
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MODID);
	
	public static final Supplier<EntityType<Anjanath>> ANJANATH = registerSized(Anjanath::new, "anjanath", 6, 5, 1);
	public static final Supplier<EntityType<AnjanathALib>> ANJANATH_AL = registerSized(AnjanathALib::new, "anjanath_al", 6, 5, 1);

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

	/*
	 * NeoForge 1.21.1 port note: FMLJavaModLoadingContext was removed.
	 * Mod constructors now receive their IEventBus directly. The upstream
	 * NeoForge branch left this TODO unresolved because MHLibMod also
	 * commented out the call site (see MHLibMod#shouldRegisterExamples
	 * branch). To register the example entities downstream code must call
	 * registerEntityTypes(IEventBus) explicitly with the example mod's
	 * mod event bus. We keep the no-arg overload as a hard-fail so any
	 * accidental caller surfaces immediately rather than silently no-op.
	 */
	public static void registerEntityTypes() {
		throw new UnsupportedOperationException(
			"MHLibExampleEntities.registerEntityTypes() is unsupported on NeoForge 1.21.1. " +
			"Use registerEntityTypes(IEventBus) and pass the mod event bus from your @Mod constructor.");
	}

	public static void registerEntityTypes(net.neoforged.bus.api.IEventBus modEventBus) {
		ENTITY_TYPES.register(modEventBus);
	}

}
