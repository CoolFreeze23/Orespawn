package danger.orespawn.gui;

import danger.orespawn.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrystalFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    private static final int INVENTORY_SIZE = 3;
    private static final int SLOT_INPUT = 0;
    private static final int SLOT_FUEL = 1;
    /** Smelt completes in half the time of a vanilla furnace (200 -> 100 ticks). */
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
    private int maxCookTime = 200;

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

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.orespawn.crystal_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CrystalFurnaceMenu(containerId, playerInventory, this, this.data);
    }

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

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalFurnaceBlockEntity blockEntity) {
        boolean changed = false;

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--;
        }

        if (!level.isClientSide()) {
            ItemStack fuel = blockEntity.items.get(SLOT_FUEL);
            ItemStack input = blockEntity.items.get(SLOT_INPUT);

            if (blockEntity.burnTime == 0 && !fuel.isEmpty() && !input.isEmpty()) {
                blockEntity.burnTime = fuel.getBurnTime(RecipeType.SMELTING);
                blockEntity.maxBurnTime = blockEntity.burnTime;
                if (blockEntity.burnTime > 0) {
                    fuel.shrink(1);
                    changed = true;
                }
            }

            if (blockEntity.burnTime > 0 && !input.isEmpty()) {
                blockEntity.cookTime++;
                if (blockEntity.cookTime >= CRYSTAL_SMELT_DURATION_TICKS) {
                    blockEntity.cookTime = 0;
                    changed = true;
                }
            } else {
                blockEntity.cookTime = 0;
            }

            if (changed) {
                blockEntity.setChanged();
            }
        }
    }
}
