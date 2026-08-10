package danger.orespawn.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the Crystal Workbench (3x3 grid + result, identical
 * slot layout to vanilla).
 *
 * <p>Faithful to 1.7.10: orig CrystalWorkbenchGUI.java:20 binds the VANILLA
 * crafting-table texture ({@code textures/gui/container/crafting_table.png});
 * bespoke crystal-workbench GUI art never existed. Asset-audit follow-up: the
 * former {@code orespawn:textures/gui/crystal_workbench.png} constant was dead
 * code and its shipped png was a byte-copy of the Mojang asset — both were
 * removed (avoids redistributing vanilla art); the vanilla texture is the one
 * path.</p>
 */
public class CrystalWorkbenchScreen extends AbstractContainerScreen<CrystalWorkbenchMenu> {

    /** orig CrystalWorkbenchGUI.java:20 — vanilla crafting-table GUI texture. */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public CrystalWorkbenchScreen(CrystalWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Vanilla crafting-table background, exactly like the original
        // (orig CrystalWorkbenchGUI.java:33 binds craftingTableGuiTextures).
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
