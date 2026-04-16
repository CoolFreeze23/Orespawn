/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.ContainerCrystalWorkbench;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@SideOnly(value=Side.CLIENT)
public class CrystalWorkbenchGUI
extends GuiContainer {
    private static final ResourceLocation craftingTableGuiTextures = new ResourceLocation("textures/gui/container/crafting_table.png");

    public CrystalWorkbenchGUI(InventoryPlayer par1InventoryPlayer, World par2World, int par3, int par4, int par5) {
        super((Container)new ContainerCrystalWorkbench(par1InventoryPlayer, par2World, par3, par4, par5));
    }

    protected void func_146979_b(int par1, int par2) {
        this.field_146289_q.func_78276_b(I18n.func_135052_a((String)"container.crafting", (Object[])new Object[0]), 28, 6, 0x404040);
        this.field_146289_q.func_78276_b(I18n.func_135052_a((String)"container.inventory", (Object[])new Object[0]), 8, this.field_147000_g - 96 + 2, 0x404040);
    }

    protected void func_146976_a(float par1, int par2, int par3) {
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.field_146297_k.func_110434_K().func_110577_a(craftingTableGuiTextures);
        int k = (this.field_146294_l - this.field_146999_f) / 2;
        int l = (this.field_146295_m - this.field_147000_g) / 2;
        this.func_73729_b(k, l, 0, 0, this.field_146999_f, this.field_147000_g);
    }
}

