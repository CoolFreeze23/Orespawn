package danger.orespawn.client.armour;

import danger.orespawn.OreSpawnMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * "OreSpawn Visuals": the armour style as two pictures, Classic and Modern, the current one marked. A click sets the
 * style and saves it, and the armour, the icons and every wearer change at once. Opened by the OreSpawn Visuals key
 * (O by default) and by the mod list's Config button.
 */
public class VisualsScreen extends Screen {
    private static final ResourceLocation CLASSIC_PICTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/gui/visuals/classic.png");
    private static final ResourceLocation MODERN_PICTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/gui/visuals/modern.png");
    /** The pictures' size in the texture files. */
    private static final int PICTURE = 144;
    private static final int GAP = 24;
    private static final int GOLD = 0xFFFFD24A;

    private final Screen parent;
    private int tile;
    private int tilesTop;

    public VisualsScreen(Screen parent) {
        super(Component.translatable("screen.orespawn.visuals.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        tile = Math.max(48, Math.min(PICTURE, Math.min((width - GAP - 40) / 2, height - 110)));
        tilesTop = Math.max(44, (height - tile - 70) / 2 + 10);
        int left = width / 2 - tile - GAP / 2;
        addRenderableWidget(new StyleTile(left, tilesTop, false));
        addRenderableWidget(new StyleTile(left + tile + GAP, tilesTop, true));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 - 100, Math.min(height - 28, tilesTop + tile + 34), 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.orespawn.visuals.armour"), width / 2,
                tilesTop - 18, 0xB0B0B0);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /** One style's picture and name; the current style framed in gold. */
    private final class StyleTile extends AbstractButton {
        private final boolean modern;

        StyleTile(int x, int y, boolean modern) {
            super(x, y, tile, tile + 16, Component.translatable(modern ? "screen.orespawn.visuals.modern"
                    : "screen.orespawn.visuals.classic"));
            this.modern = modern;
        }

        @Override
        public void onPress() {
            ArmourStyleConfig.setModern(modern);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean selected = ArmourStyleConfig.modern() == modern;
            int x = getX(), y = getY();
            int frame = selected ? GOLD : isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF4A4A54;
            graphics.fill(x - 2, y - 2, x + tile + 2, y + tile + 2, frame);
            graphics.blit(modern ? MODERN_PICTURE : CLASSIC_PICTURE, x, y, tile, tile, 0, 0, PICTURE, PICTURE,
                    PICTURE, PICTURE);
            Component label = selected ? Component.translatable("screen.orespawn.visuals.selected", getMessage())
                    : getMessage();
            graphics.drawCenteredString(font, label, x + tile / 2, y + tile + 6, selected ? 0xFFD24A : 0xE0E0E0);
        }

        /** The narrator says which style is the current one, as the gold frame shows it. */
        @Override
        protected MutableComponent createNarrationMessage() {
            return wrapDefaultNarrationMessage(ArmourStyleConfig.modern() == modern
                    ? Component.translatable("screen.orespawn.visuals.selected.narration", getMessage())
                    : getMessage());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
