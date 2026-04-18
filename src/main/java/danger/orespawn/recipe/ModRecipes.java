package danger.orespawn.recipe;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * DeferredRegister wiring for the {@code orespawn:extracting} recipe type +
 * serializer. Registered on the mod event bus from {@link OreSpawnMod}.
 *
 * <p>1.21.1 quirk: RecipeType is registered against {@link Registries#RECIPE_TYPE}
 * with an opaque holder; vanilla resolves the recipe lookup by identity-comparing
 * this instance, so it must outlive the registration phase.</p>
 */
public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, OreSpawnMod.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ExtractingRecipe>> EXTRACTING_TYPE =
            RECIPE_TYPES.register("extracting", () -> new RecipeType<ExtractingRecipe>() {
                @Override
                public String toString() {
                    return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "extracting").toString();
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, ExtractingRecipe.Serializer> EXTRACTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("extracting", () -> ExtractingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
