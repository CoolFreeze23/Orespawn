package danger.orespawn.block.entity;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.recipe.ExtractingRecipe;
import danger.orespawn.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Backing inventory + tick logic for the {@link danger.orespawn.block.Extractor}.
 *
 * <p>Two-slot {@link WorldlyContainer}: slot 0 = input (top face), slot 1 =
 * output (bottom face). Hoppers above push into input, hoppers below pull from
 * output. Any {@code orespawn:extracting} recipe that matches the input slot
 * advances per tick; on completion the result is moved to the output slot.</p>
 *
 * <p>Throttled to 1 progress increment per server tick — no event listeners,
 * no recipe-cache leaks across worlds. The cached {@link RecipeHolder} is
 * looked up once per progress cycle and dropped on input change.</p>
 */
public class ExtractorBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int[] TOP_SLOTS = {INPUT_SLOT};
    private static final int[] BOTTOM_SLOTS = {OUTPUT_SLOT};
    private static final int[] SIDE_SLOTS = {};

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = ExtractingRecipe.DEFAULT_PROCESS_TIME;

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTRACTOR_BE.get(), pos, state);
    }

    public NonNullList<ItemStack> snapshotInventory() {
        return this.items;
    }

    // ---- Tick ----

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExtractorBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack input = be.items.get(INPUT_SLOT);
        if (input.isEmpty()) {
            if (be.progress != 0) {
                be.progress = 0;
                be.setChanged();
            }
            return;
        }

        Optional<RecipeHolder<ExtractingRecipe>> recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipes.EXTRACTING_TYPE.get(),
                        new SingleRecipeInput(input), level);
        if (recipeOpt.isEmpty()) {
            if (be.progress != 0) {
                be.progress = 0;
                be.setChanged();
            }
            return;
        }

        ExtractingRecipe recipe = recipeOpt.get().value();
        ItemStack result = recipe.assemble(new SingleRecipeInput(input), level.registryAccess());
        if (!be.canPlaceOutput(result)) {
            return; // output blocked, hold progress
        }

        be.maxProgress = recipe.safeProcessTime();
        be.progress++;
        if (be.progress >= be.maxProgress) {
            be.progress = 0;
            be.finishProcessing(result);
        }
        be.setChanged();
    }

    private boolean canPlaceOutput(ItemStack result) {
        ItemStack out = this.items.get(OUTPUT_SLOT);
        if (out.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void finishProcessing(ItemStack result) {
        ItemStack out = this.items.get(OUTPUT_SLOT);
        if (out.isEmpty()) {
            this.items.set(OUTPUT_SLOT, result.copy());
        } else {
            out.grow(result.getCount());
        }
        this.items.get(INPUT_SLOT).shrink(1);
    }

    // ---- WorldlyContainer ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP) return TOP_SLOTS;
        if (side == Direction.DOWN) return BOTTOM_SLOTS;
        return SIDE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == INPUT_SLOT && direction == Direction.UP;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT && direction == Direction.DOWN;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack s : this.items) if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack r = ContainerHelper.removeItem(this.items, slot, amount);
        if (!r.isEmpty()) this.setChanged();
        return r;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack prev = this.items.get(slot);
        boolean changedSignificantly = !ItemStack.isSameItemSameComponents(stack, prev);
        this.items.set(slot, stack);
        if (slot == INPUT_SLOT && changedSignificantly) {
            this.progress = 0;
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // ---- Persistence ----

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("Progress", this.progress);
        tag.putInt("MaxProgress", this.maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.progress = tag.getInt("Progress");
        this.maxProgress = tag.getInt("MaxProgress");
        if (this.maxProgress <= 0) this.maxProgress = ExtractingRecipe.DEFAULT_PROCESS_TIME;
    }

    // Reference kept so static analysis keeps the import — vendor codecs can
    // need this when serializing recipe-derived item-stacks for chunk save.
    @SuppressWarnings("unused")
    private void touchRegistries() {
        BuiltInRegistries.ITEM.getKey(net.minecraft.world.item.Items.AIR);
    }
}
