package danger.orespawn.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceMenu> {
    /**
     * Uses the vanilla furnace texture since the slot layout is identical.
     * Replace with a custom crystal-themed texture when one is available.
     *
     * FIX i005 (audit ITEM-016, GUI_LEGACY_ATLAS_BLIT): the flame/arrow progress
     * indicators were previously blitted from this texture at U=176 (the 1.7.10-era
     * atlas layout). In 1.21.1 that region of furnace.png is empty - the sub-sprites
     * moved to individual GUI sprites (textures/gui/sprites/container/furnace/*.png),
     * so the progress bar rendered invisible while smelting still worked. Progress is
     * now drawn via GuiGraphics.blitSprite with the modern sprite ids, copying
     * vanilla's exact usage from the decompiled 1.21.1 sources
     * (net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen.renderBg and
     * FurnaceScreen's sprite constants, build/neoform/neoFormJoined1.21.1-20240808.144430
     * steps/patch/outputs.jar).
     */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    // Sprite ids exactly as declared in vanilla 1.21.1 FurnaceScreen.
    private static final ResourceLocation LIT_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation BURN_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

    // Geometry from vanilla AbstractFurnaceScreen.renderBg: the lit_progress sprite is
    // 14x14 px drawn at (left+56, top+36), the burn_progress sprite is 24x16 px drawn
    // at (left+79, top+34). Both are revealed bottom-up / left-to-right respectively.
    private static final int FLAME_BLIT_X = 56;
    private static final int FLAME_BLIT_Y = 36;
    private static final int FLAME_SPRITE_SIZE = 14;
    private static final int ARROW_BLIT_X = 79;
    private static final int ARROW_BLIT_Y = 34;
    private static final int ARROW_SPRITE_WIDTH = 24;
    private static final int ARROW_SPRITE_HEIGHT = 16;

    public CrystalFurnaceScreen(CrystalFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int panelLeft = (this.width - this.imageWidth) / 2;
        int panelTop = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, panelLeft, panelTop, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isLit()) {
            // Vanilla: l = Mth.ceil(litProgress * 13.0F) + 1; our menu already returns
            // the scaled 0..13 px value, so only the +1 (always-visible flame base) applies.
            int litHeight = this.menu.getLitProgress() + 1;
            // Vanilla: blitSprite(litProgressSprite, 14, 14, 0, 14 - l, i + 56, j + 36 + 14 - l, 14, l)
            guiGraphics.blitSprite(LIT_PROGRESS_SPRITE,
                    FLAME_SPRITE_SIZE, FLAME_SPRITE_SIZE,
                    0, FLAME_SPRITE_SIZE - litHeight,
                    panelLeft + FLAME_BLIT_X,
                    panelTop + FLAME_BLIT_Y + FLAME_SPRITE_SIZE - litHeight,
                    FLAME_SPRITE_SIZE, litHeight);
        }

        // Vanilla: j1 = Mth.ceil(burnProgress * 24.0F); our menu already returns 0..24 px.
        int burnProgressWidth = this.menu.getBurnProgress();
        // Vanilla: blitSprite(burnProgressSprite, 24, 16, 0, 0, i + 79, j + 34, j1, 16)
        guiGraphics.blitSprite(BURN_PROGRESS_SPRITE,
                ARROW_SPRITE_WIDTH, ARROW_SPRITE_HEIGHT,
                0, 0,
                panelLeft + ARROW_BLIT_X,
                panelTop + ARROW_BLIT_Y,
                burnProgressWidth, ARROW_SPRITE_HEIGHT);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
