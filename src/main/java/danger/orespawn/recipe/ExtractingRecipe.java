package danger.orespawn.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Recipe representing a single-input → single-output transformation in the
 * Extractor block. Configurable per-recipe processing time so e.g. extracting
 * DNA from a fossil can be slower than extracting nuggets from an ore.
 */
public record ExtractingRecipe(Ingredient input, ItemStack result, int processTime)
        implements Recipe<SingleRecipeInput> {

    public static final int DEFAULT_PROCESS_TIME = 200;

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.EXTRACTING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ExtractingRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<ExtractingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtractingRecipe::input),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ExtractingRecipe::result),
                Codec.INT.optionalFieldOf("processtime", DEFAULT_PROCESS_TIME)
                        .forGetter(ExtractingRecipe::processTime)
        ).apply(inst, ExtractingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ExtractingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ExtractingRecipe::input,
                ItemStack.STREAM_CODEC, ExtractingRecipe::result,
                ByteBufCodecs.INT, ExtractingRecipe::processTime,
                ExtractingRecipe::new
        );

        @Override
        public MapCodec<ExtractingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ExtractingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    /**
     * Helper used by the {@link danger.orespawn.block.entity.ExtractorBlockEntity}
     * when the world isn't available during item-handler queries (e.g. for
     * insertion validity checks). Returns the recipe-resolved result for the
     * current input slot if any, otherwise empty.
     */
    public ItemStack copyResult() {
        return this.result.copy();
    }

    /** Cosmetic helper — guards against negative process times in JSON. */
    public int safeProcessTime() {
        return Math.max(1, this.processTime);
    }

    public static ExtractingRecipe of(Ingredient ingredient, ItemStack result, int processTime) {
        return new ExtractingRecipe(ingredient, result, Math.max(1, processTime));
    }

    /** Used by datagen / tests to construct without manual ResourceLocation games. */
    public static ExtractingRecipe of(Ingredient ingredient, ItemStack result) {
        return of(ingredient, result, DEFAULT_PROCESS_TIME);
    }

    /** Provided so future menu integrations can show a "default" tooltip. */
    public RegistryAccess registryAccessFallback() {
        return RegistryAccess.EMPTY;
    }
}
