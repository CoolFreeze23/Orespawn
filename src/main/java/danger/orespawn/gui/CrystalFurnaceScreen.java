package danger.orespawn.gui;

import danger.orespawn.OreSpawnMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceMenu> {
    /**
     * Uses the vanilla furnace texture since the slot layout is identical.
     * Replace with a custom crystal-themed texture when one is available.
     */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");

    private static final int FLAME_BLIT_X = 56;
    private static final int FLAME_BLIT_BASE_Y = 36;
    private static final int FLAME_TEXTURE_MAX_HEIGHT = 12;
    private static final int FLAME_TEXTURE_U = 176;
    private static final int FLAME_WIDTH = 14;
    private static final int ARROW_BLIT_X = 79;
    private static final int ARROW_BLIT_Y = 34;
    private static final int ARROW_TEXTURE_U = 176;
    private static final int ARROW_TEXTURE_V = 14;
    private static final int ARROW_HEIGHT = 16;
    private static final int ARROW_EXTRA_WIDTH = 1;

    public CrystalFurnaceScreen(CrystalFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int panelLeft = (this.width - this.imageWidth) / 2;
        int panelTop = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, panelLeft, panelTop, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isLit()) {
            int litHeight = this.menu.getLitProgress();
            guiGraphics.blit(TEXTURE,
                    panelLeft + FLAME_BLIT_X,
                    panelTop + FLAME_BLIT_BASE_Y + FLAME_TEXTURE_MAX_HEIGHT - litHeight,
                    FLAME_TEXTURE_U,
                    FLAME_TEXTURE_MAX_HEIGHT - litHeight,
                    FLAME_WIDTH,
                    litHeight + ARROW_EXTRA_WIDTH);
        }

        int burnProgressWidth = this.menu.getBurnProgress();
        guiGraphics.blit(TEXTURE,
                panelLeft + ARROW_BLIT_X,
                panelTop + ARROW_BLIT_Y,
                ARROW_TEXTURE_U,
                ARROW_TEXTURE_V,
                burnProgressWidth + ARROW_EXTRA_WIDTH,
                ARROW_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
