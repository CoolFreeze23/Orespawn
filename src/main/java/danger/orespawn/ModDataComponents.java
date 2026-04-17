package danger.orespawn;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Modern 1.21.1 replacement for the 1.7.10 NBT-on-ItemStack pattern. All persistent
 * per-stack state for OreSpawn items lives here as typed {@link DataComponentType}s
 * rather than raw {@code CompoundTag} keys, which is the paradigm shift mandated by
 * Mojang's component overhaul.
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> CAGED_ENTITY =
            DATA_COMPONENTS.register("caged_entity", () ->
                    DataComponentType.<ResourceLocation>builder()
                            .persistent(ResourceLocation.CODEC)
                            .networkSynchronized(ResourceLocation.STREAM_CODEC)
                            .build());

    /** Full serialized {@link CompoundTag} for a captured mob — health, custom name,
     *  taming state, equipment, age, etc. Replaces the 1.7.10 pattern of throwing
     *  away all live state on capture and re-spawning a vanilla-default entity.
     *  Network-synced so the tooltip can render mob-specific info on the client. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CAGED_ENTITY_DATA =
            DATA_COMPONENTS.register("caged_entity_data", () ->
                    DataComponentType.<CompoundTag>builder()
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                            .build());

    /** Cumulative kill counter for the Ultimate Sword. Replaces the 1.7.10 stack-NBT
     *  "kills" key that we never had room for in the legacy item. Surfaced in tooltip. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ULTIMATE_SWORD_KILLS =
            DATA_COMPONENTS.register("ultimate_sword_kills", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    /** Cumulative kill counter for any Bertha-family weapon (Big Bertha, Royal Guardian
     *  Sword, Battle Axe, Queen Battle Axe, Chainsaw, Attitude Adjuster). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BERTHA_KILLS =
            DATA_COMPONENTS.register("bertha_kills", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
