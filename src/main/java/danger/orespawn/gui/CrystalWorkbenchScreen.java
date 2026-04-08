package danger.orespawn.gui;

import danger.orespawn.OreSpawnMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client-side screen for the Crystal Workbench.
 * Uses the vanilla crafting table texture layout since the grid dimensions
 * are identical (3x3 + result). A custom texture can be substituted later
 * by changing the TEXTURE path.
 */
public class CrystalWorkbenchScreen extends AbstractContainerScreen<CrystalWorkbenchMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/gui/crystal_workbench.png");

    /** Fallback to vanilla crafting table texture if custom one is missing. */
    private static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public CrystalWorkbenchScreen(CrystalWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Use vanilla crafting table texture as base -- works perfectly since
        // slot positions match. Replace FALLBACK_TEXTURE with TEXTURE once a
        // custom crystal workbench GUI texture is available.
        guiGraphics.blit(FALLBACK_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
