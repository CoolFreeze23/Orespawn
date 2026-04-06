package danger.orespawn.gui;

import danger.orespawn.ModBlockEntities;
import danger.orespawn.block.CrystalFurnace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CrystalFurnaceBlockEntity extends BlockEntity implements MenuProvider, Container {
    private static final int INVENTORY_SIZE = 3;
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    private static final int SLOT_OUTPUT = 2;
    private static final int CRYSTAL_SMELT_DURATION_TICKS = 100;
    private static final int CONTAINER_DATA_SIZE = 4;
    private static final int DATA_INDEX_BURN_TIME = 0;
    private static final int DATA_INDEX_MAX_BURN_TIME = 1;
    private static final int DATA_INDEX_COOK_TIME = 2;
    private static final int DATA_INDEX_MAX_COOK_TIME = 3;

    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int maxCookTime = CRYSTAL_SMELT_DURATION_TICKS;
    private Component customName;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_INDEX_BURN_TIME -> CrystalFurnaceBlockEntity.this.burnTime;
                case DATA_INDEX_MAX_BURN_TIME -> CrystalFurnaceBlockEntity.this.maxBurnTime;
                case DATA_INDEX_COOK_TIME -> CrystalFurnaceBlockEntity.this.cookTime;
                case DATA_INDEX_MAX_COOK_TIME -> CrystalFurnaceBlockEntity.this.maxCookTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_INDEX_BURN_TIME -> CrystalFurnaceBlockEntity.this.burnTime = value;
                case DATA_INDEX_MAX_BURN_TIME -> CrystalFurnaceBlockEntity.this.maxBurnTime = value;
                case DATA_INDEX_COOK_TIME -> CrystalFurnaceBlockEntity.this.cookTime = value;
                case DATA_INDEX_MAX_COOK_TIME -> CrystalFurnaceBlockEntity.this.maxCookTime = value;
            }
        }

        @Override
        public int getCount() {
            return CONTAINER_DATA_SIZE;
        }
    };

    public CrystalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_FURNACE_BE.get(), pos, state);
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    @Override
    public Component getDisplayName() {
        return this.customName != null ? this.customName : Component.translatable("container.orespawn.crystal_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CrystalFurnaceMenu(containerId, playerInventory, this, this.data);
    }

    // ---- Container interface ----

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
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
        tag.putInt("BurnTime", this.burnTime);
        tag.putInt("CookTime", this.cookTime);
        tag.putInt("MaxBurnTime", this.maxBurnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.burnTime = tag.getInt("BurnTime");
        this.cookTime = tag.getInt("CookTime");
        this.maxBurnTime = tag.getInt("MaxBurnTime");
    }

    // ---- Tick logic ----

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalFurnaceBlockEntity be) {
        boolean wasBurning = be.isBurning();
        boolean changed = false;

        if (be.burnTime > 0) {
            be.burnTime--;
        }

        ItemStack fuel = be.items.get(SLOT_FUEL);
        ItemStack input = be.items.get(SLOT_INPUT);

        if (be.burnTime == 0 && !fuel.isEmpty() && canSmelt(level, input, be)) {
            be.burnTime = fuel.getBurnTime(RecipeType.SMELTING);
            be.maxBurnTime = be.burnTime;
            if (be.burnTime > 0) {
                fuel.shrink(1);
                changed = true;
            }
        }

        if (be.isBurning() && canSmelt(level, input, be)) {
            be.cookTime++;
            if (be.cookTime >= CRYSTAL_SMELT_DURATION_TICKS) {
                be.cookTime = 0;
                smeltItem(level, be);
                changed = true;
            }
        } else {
            be.cookTime = 0;
        }

        if (wasBurning != be.isBurning()) {
            changed = true;
            level.setBlock(pos, state.setValue(CrystalFurnace.LIT, be.isBurning()), 3);
        }

        if (changed) {
            be.setChanged();
        }
    }

    private static boolean canSmelt(Level level, ItemStack input, CrystalFurnaceBlockEntity be) {
        if (input.isEmpty()) return false;
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        if (recipe.isEmpty()) return false;
        ItemStack result = recipe.get().value().getResultItem(level.registryAccess());
        if (result.isEmpty()) return false;
        ItemStack output = be.items.get(SLOT_OUTPUT);
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private static void smeltItem(Level level, CrystalFurnaceBlockEntity be) {
        ItemStack input = be.items.get(SLOT_INPUT);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        if (recipe.isEmpty()) return;

        ItemStack result = recipe.get().value().getResultItem(level.registryAccess()).copy();
        ItemStack output = be.items.get(SLOT_OUTPUT);

        if (output.isEmpty()) {
            be.items.set(SLOT_OUTPUT, result);
        } else if (ItemStack.isSameItemSameComponents(output, result)) {
            output.grow(result.getCount());
        }

        input.shrink(1);
    }
}
