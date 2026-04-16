/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.client.FMLClientHandler;
import danger.orespawn.ModelHammy;
import danger.orespawn.RenderHammy;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class RenderHammy
implements IItemRenderer {
    protected ModelHammy modelHammy = new ModelHammy();
    private static final ResourceLocation texture = new ResourceLocation("orespawn", "AttitudeAdjustertexture.png");

    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
        switch (1.$SwitchMap$net$minecraftforge$client$IItemRenderer$ItemRenderType[type.ordinal()]) {
            case 1: {
                return true;
            }
            case 2: {
                return true;
            }
        }
        return false;
    }

    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
        return true;
    }

    public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object ... data) {
        switch (1.$SwitchMap$net$minecraftforge$client$IItemRenderer$ItemRenderType[type.ordinal()]) {
            case 1: {
                this.renderSwordF5(6.0f, -20.0f, -4.0f, 0.15f);
                break;
            }
            case 2: {
                this.renderSword(-10.0f, -13.0f, -5.0f, 0.15f);
                break;
            }
        }
    }

    private void renderSword(float x, float y, float z, float scale) {
        GL11.glPushMatrix();
        GL11.glRotatef((float)70.0f, (float)0.0f, (float)1.0f, (float)0.0f);
        GL11.glRotatef((float)190.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glRotatef((float)25.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glScalef((float)scale, (float)scale, (float)scale);
        GL11.glTranslatef((float)x, (float)y, (float)z);
        FMLClientHandler.instance().getClient().field_71446_o.func_110577_a(texture);
        this.modelHammy.render();
        GL11.glPopMatrix();
    }

    private void renderSwordF5(float x, float y, float z, float scale) {
        GL11.glPushMatrix();
        GL11.glRotatef((float)180.0f, (float)1.0f, (float)0.0f, (float)0.0f);
        GL11.glScalef((float)scale, (float)scale, (float)scale);
        GL11.glTranslatef((float)x, (float)y, (float)z);
        FMLClientHandler.instance().getClient().field_71446_o.func_110577_a(texture);
        this.modelHammy.render();
        GL11.glPopMatrix();
    }
}

