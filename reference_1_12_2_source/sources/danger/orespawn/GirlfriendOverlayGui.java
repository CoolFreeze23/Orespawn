/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.material.Material
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.ResourceLocation
 *  net.minecraftforge.client.event.RenderGameOverlayEvent
 *  net.minecraftforge.client.event.RenderGameOverlayEvent$ElementType
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  org.lwjgl.opengl.GL11
 */
package danger.orespawn;

import danger.orespawn.entity.Alien;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.Kyuubi;
import danger.orespawn.entity.Mantis;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.Spyro;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.WormLarge;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class GirlfriendOverlayGui
extends Gui {
    private Minecraft mc;
    private static final ResourceLocation texture = new ResourceLocation("orespawn", "girlfriendgui.png");

    public GirlfriendOverlayGui(Minecraft mc) {
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        Object e;
        System.out.println("RUNNING");
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        System.out.println("GOING THRU");
        int u = 0;
        int v = 0;
        String outstring = null;
        int color = 0xFF3434;
        FontRenderer fr = this.mc.field_71466_p;
        int barWidth = 182;
        int barHeight = 5;
        float gfHealth = 0.0f;
        Entity entity = null;
        EntityPlayerSP player = null;
        if (this.mc.field_71474_y.field_74319_N || this.mc.field_71462_r != null) {
            return;
        }
        player = this.mc.field_71439_g;
        if (player == null) {
            return;
        }
        entity = this.mc.field_147125_j;
        if (entity instanceof Mothra) {
            e = (Mothra)entity;
            outstring = "Mothra!";
            gfHealth = (float)e.getMothraHealth() / e.func_110138_aP();
        }
        if (entity instanceof Spyro) {
            e = (Spyro)entity;
            if (e.func_145818_k_()) {
                outstring = e.func_95999_t();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Baby Dragon";
            }
            gfHealth = (float)((Spyro)((Object)e)).getSpyroHealth() / e.func_110138_aP();
        }
        if (entity instanceof WormLarge) {
            e = (WormLarge)entity;
            if (!((WormLarge)((Object)e)).field_70145_X) {
                outstring = "Worm";
                gfHealth = e.func_110143_aJ() / e.func_110138_aP();
            }
        }
        if (entity instanceof Alien) {
            e = (Alien)entity;
            outstring = "Alien!";
            gfHealth = (float)((Alien)((Object)e)).getAlienHealth() / e.func_110138_aP();
        }
        if (entity instanceof Alosaurus) {
            e = (Alosaurus)entity;
            outstring = "Alosaurus";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Nastysaurus) {
            e = (Nastysaurus)entity;
            outstring = "Nastysaurus";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof TRex) {
            e = (TRex)entity;
            outstring = "T. Rex";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Kyuubi) {
            e = (Kyuubi)entity;
            outstring = "Kyuubi";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Mantis) {
            e = (Mantis)entity;
            outstring = "Mantis";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (outstring == null) {
            return;
        }
        ScaledResolution res = new ScaledResolution(this.mc);
        int width = res.func_78326_a();
        int barWidthFilled = (int)(gfHealth * (float)(barWidth + 1));
        int x = width / 2 - barWidth / 2;
        int y = 25;
        if (player.func_70055_a(Material.field_151586_h) || player.func_70658_aO() > 0) {
            y -= 10;
        }
        fr.func_175063_a(outstring, (float)(width / 2 - fr.func_78256_a(outstring) / 2), (float)(y - 10), color);
        this.mc.field_71446_o.func_110577_a(texture);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.func_73729_b(x, y, u, v, barWidth, barHeight);
        if (barWidthFilled > 0) {
            this.func_73729_b(x, y, u, v + barHeight, barWidthFilled, barHeight);
        }
    }
}

