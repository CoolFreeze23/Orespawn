package danger.orespawn.gui;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModMenuTypes;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Crystal Workbench menu -- functions identically to a vanilla Crafting Table
 * but is tied to the Crystal Workbench block. Uses the same recipe system
 * so all vanilla + modded 3x3 recipes work inside it.
 */
public class CrystalWorkbenchMenu extends AbstractContainerMenu {

    private static final int GRID_WIDTH = 3;
    private static final int GRID_HEIGHT = 3;
    private static final int RESULT_SLOT_INDEX = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_END = 10;
    private static final int INV_SLOT_START = 10;
    private static final int INV_SLOT_END = 37;
    private static final int HOTBAR_SLOT_START = 37;
    private static final int HOTBAR_SLOT_END = 46;

    private final CraftingContainer craftSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    /** Client-side constructor (no block access). */
    public CrystalWorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    /** Server-side constructor with block-position awareness for stillValid check. */
    public CrystalWorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.CRYSTAL_WORKBENCH_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;
        this.craftSlots = new TransientCraftingContainer(this, GRID_WIDTH, GRID_HEIGHT);

        // Result slot at (124, 35) matching vanilla crafting table layout
        this.addSlot(new ResultSlot(playerInventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

        // 3x3 crafting grid starting at (30, 17)
        for (int row = 0; row < GRID_HEIGHT; ++row) {
            for (int col = 0; col < GRID_WIDTH; ++col) {
                this.addSlot(new Slot(this.craftSlots, col + row * GRID_WIDTH, 30 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    /**
     * Re-evaluates the crafting recipe whenever the grid changes.
     * Mirrors vanilla CraftingMenu behavior for full recipe compatibility.
     */
    @Override
    public void slotsChanged(Container container) {
        this.access.execute((level, pos) -> slotChangedCraftingGrid(level));
    }

    private void slotChangedCraftingGrid(Level level) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;

        ItemStack result = ItemStack.EMPTY;
        CraftingInput input = this.craftSlots.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> optional = level.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level);

        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> holder = optional.get();
            CraftingRecipe recipe = holder.value();
            if (this.resultSlots.setRecipeUsed(level, serverPlayer, holder)) {
                result = recipe.assemble(input, level.registryAccess());
            }
        }

        this.resultSlots.setItem(0, result);
        this.setRemoteSlot(RESULT_SLOT_INDEX, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                this.containerId, this.incrementStateId(), RESULT_SLOT_INDEX, result));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.CRYSTAL_WORKBENCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index == RESULT_SLOT_INDEX) {
                this.access.execute((level, pos) -> slotStack.getItem().onCraftedBy(slotStack, level, player));
                if (!this.moveItemStackTo(slotStack, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (index >= INV_SLOT_START && index < HOTBAR_SLOT_END) {
                if (!this.moveItemStackTo(slotStack, CRAFT_SLOT_START, CRAFT_SLOT_END, false)) {
                    if (index < INV_SLOT_END) {
                        if (!this.moveItemStackTo(slotStack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
            if (index == RESULT_SLOT_INDEX) {
                player.drop(slotStack, false);
            }
        }
        return result;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    /** Drop crafting grid contents when the menu is closed. */
    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
    }
}
