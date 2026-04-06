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
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int maxCookTime = 200;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CrystalFurnaceBlockEntity.this.burnTime;
                case 1 -> CrystalFurnaceBlockEntity.this.maxBurnTime;
                case 2 -> CrystalFurnaceBlockEntity.this.cookTime;
                case 3 -> CrystalFurnaceBlockEntity.this.maxCookTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CrystalFurnaceBlockEntity.this.burnTime = value;
                case 1 -> CrystalFurnaceBlockEntity.this.maxBurnTime = value;
                case 2 -> CrystalFurnaceBlockEntity.this.cookTime = value;
                case 3 -> CrystalFurnaceBlockEntity.this.maxCookTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
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
        this.items = NonNullList.withSize(3, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.burnTime = tag.getInt("BurnTime");
        this.cookTime = tag.getInt("CookTime");
        this.maxBurnTime = tag.getInt("MaxBurnTime");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalFurnaceBlockEntity blockEntity) {
        // Crystal furnace smelts at 2x speed (100 ticks instead of 200)
        boolean wasBurning = blockEntity.burnTime > 0;
        boolean changed = false;

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--;
        }

        if (!level.isClientSide()) {
            ItemStack fuel = blockEntity.items.get(1);
            ItemStack input = blockEntity.items.get(0);

            if (blockEntity.burnTime == 0 && !fuel.isEmpty() && !input.isEmpty()) {
                blockEntity.burnTime = net.neoforged.neoforge.common.CommonHooks.getBurnTime(fuel, RecipeType.SMELTING);
                blockEntity.maxBurnTime = blockEntity.burnTime;
                if (blockEntity.burnTime > 0) {
                    fuel.shrink(1);
                    changed = true;
                }
            }

            if (blockEntity.burnTime > 0 && !input.isEmpty()) {
                blockEntity.cookTime++;
                if (blockEntity.cookTime >= 100) {
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
