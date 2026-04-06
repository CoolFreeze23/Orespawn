package danger.orespawn.gui;

import danger.orespawn.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrystalFurnaceMenu extends AbstractContainerMenu {
    private static final int BE_SLOT_COUNT = 3;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int HOTBAR_Y = 142;
    private static final int INVENTORY_ORIGIN_X = 8;
    private static final int INVENTORY_ORIGIN_Y = 84;
    private static final int SLOT_STRIDE = 18;
    private static final int CRYSTAL_SMELT_DURATION_TICKS = 100;
    private static final int BURN_PROGRESS_BAR_WIDTH_PX = 24;
    private static final int FLAME_PROGRESS_BAR_HEIGHT_PX = 13;
    private static final int VANILLA_FURNACE_MAX_BURN_FALLBACK = 200;

    private final ContainerData data;
    private final Container container;

    public CrystalFurnaceMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(BE_SLOT_COUNT), new SimpleContainerData(4));
    }

    public CrystalFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.CRYSTAL_FURNACE_MENU.get(), containerId);
        this.data = data;
        this.container = container;
        checkContainerSize(container, BE_SLOT_COUNT);
        checkContainerDataCount(data, 4);

        addDataSlots(data);

        this.addSlot(new Slot(container, 0, 56, 17));
        this.addSlot(new Slot(container, 1, 56, 53));
        this.addSlot(new FurnaceResultSlot(playerInventory.player, container, 2, 116, 35));

        for (int row = 0; row < PLAYER_INVENTORY_ROWS; ++row) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * PLAYER_INVENTORY_COLUMNS + PLAYER_INVENTORY_COLUMNS,
                        INVENTORY_ORIGIN_X + col * SLOT_STRIDE, INVENTORY_ORIGIN_Y + row * SLOT_STRIDE));
            }
        }
        for (int col = 0; col < PLAYER_INVENTORY_COLUMNS; ++col) {
            this.addSlot(new Slot(playerInventory, col, INVENTORY_ORIGIN_X + col * SLOT_STRIDE, HOTBAR_Y));
        }
    }

    public int getBurnProgress() {
        int cookTime = this.data.get(2);
        return cookTime != 0 ? cookTime * BURN_PROGRESS_BAR_WIDTH_PX / CRYSTAL_SMELT_DURATION_TICKS : 0;
    }

    public int getLitProgress() {
        int maxBurnTime = this.data.get(1);
        if (maxBurnTime == 0) maxBurnTime = VANILLA_FURNACE_MAX_BURN_FALLBACK;
        return this.data.get(0) * FLAME_PROGRESS_BAR_HEIGHT_PX / maxBurnTime;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index < BE_SLOT_COUNT) {
                if (!this.moveItemStackTo(slotStack, BE_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(slotStack, 0, BE_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }
}
