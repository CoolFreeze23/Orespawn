package danger.orespawn.data;

import com.mojang.serialization.MapCodec;
import danger.orespawn.OreSpawnMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** The mod's data load conditions: {@code orespawn:modern} (MOD-040, {@link ModernCondition}). */
public class ModConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ModernCondition>> MODERN =
            CONDITION_CODECS.register("modern", () -> ModernCondition.CODEC);

    public static void register(IEventBus eventBus) {
        CONDITION_CODECS.register(eventBus);
    }
}
