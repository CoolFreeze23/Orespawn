package danger.orespawn.gui;

import danger.orespawn.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CrystalFurnaceMenu extends AbstractContainerMenu {
    private final ContainerData data;

    public CrystalFurnaceMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainerData(4));
    }

    public CrystalFurnaceMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.CRYSTAL_FURNACE_MENU.get(), containerId);
        this.data = data;

        addDataSlots(data);

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public int getBurnProgress() {
        int cookTime = this.data.get(2);
        return cookTime != 0 ? cookTime * 24 / 100 : 0;
    }

    public int getLitProgress() {
        int maxBurnTime = this.data.get(1);
        if (maxBurnTime == 0) maxBurnTime = 200;
        return this.data.get(0) * 13 / maxBurnTime;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
