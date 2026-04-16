/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import danger.orespawn.Alien;
import danger.orespawn.Alosaurus;
import danger.orespawn.AntRobot;
import danger.orespawn.BandP;
import danger.orespawn.Basilisk;
import danger.orespawn.Boyfriend;
import danger.orespawn.CaterKiller;
import danger.orespawn.Cephadrome;
import danger.orespawn.Crab;
import danger.orespawn.Dragon;
import danger.orespawn.EmperorScorpion;
import danger.orespawn.GiantRobot;
import danger.orespawn.Girlfriend;
import danger.orespawn.Godzilla;
import danger.orespawn.Hammerhead;
import danger.orespawn.HerculesBeetle;
import danger.orespawn.Irukandji;
import danger.orespawn.Kraken;
import danger.orespawn.Kyuubi;
import danger.orespawn.Leon;
import danger.orespawn.Mantis;
import danger.orespawn.Molenoid;
import danger.orespawn.Mothra;
import danger.orespawn.Nastysaurus;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.PitchBlack;
import danger.orespawn.Robot2;
import danger.orespawn.Robot4;
import danger.orespawn.SeaMonster;
import danger.orespawn.SeaViper;
import danger.orespawn.SpiderRobot;
import danger.orespawn.SpitBug;
import danger.orespawn.Spyro;
import danger.orespawn.TRex;
import danger.orespawn.TheKing;
import danger.orespawn.ThePrince;
import danger.orespawn.ThePrinceAdult;
import danger.orespawn.ThePrinceTeen;
import danger.orespawn.ThePrincess;
import danger.orespawn.TheQueen;
import danger.orespawn.Triffid;
import danger.orespawn.TrooperBug;
import danger.orespawn.Vortex;
import danger.orespawn.WaterDragon;
import danger.orespawn.WormLarge;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
        float myf;
        EmperorScorpion e;
        Girlfriend gf;
        if (event.isCancelable() || event.type != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        int u = 0;
        int v = 0;
        String outstring = null;
        int color = 0xFF3434;
        FontRenderer fr = this.mc.field_71466_p;
        int barWidth = 182;
        int barHeight = 5;
        float gfHealth = 0.0f;
        Entity entity = null;
        EntityClientPlayerMP player = null;
        if (this.mc.field_71474_y.field_74319_N || this.mc.field_71462_r != null) {
            return;
        }
        player = this.mc.field_71439_g;
        if (player == null) {
            return;
        }
        OreSpawnMain.current_dimension = player.field_70170_p.field_73011_w.field_76574_g;
        OreSpawnMain.FastGraphicsLeaves = this.mc.field_71474_y.field_74347_j ? 0 : 1;
        if (OreSpawnMain.GuiOverlayEnable == 0) {
            return;
        }
        entity = this.mc.field_147125_j;
        if (entity == null) {
            entity = OreSpawnMain.getPointedAtEntity((World)this.mc.field_71441_e, (EntityPlayer)player, (double)16.0);
            if (entity == null) {
                return;
            }
            if (!(entity instanceof EntityLivingBase)) {
                return;
            }
        }
        if (entity instanceof Girlfriend) {
            gf = null;
            gf = (Girlfriend)entity;
            if (!gf.func_152114_e((EntityLivingBase)player)) {
                return;
            }
            if (gf.passenger != 0) {
                return;
            }
            if (gf.func_94056_bM()) {
                outstring = gf.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Girlfriend";
            }
            gfHealth = (float)gf.getGirlfriendHealth() / gf.func_110138_aP();
        }
        if (entity instanceof Boyfriend) {
            gf = null;
            gf = (Boyfriend)entity;
            if (!gf.func_152114_e((EntityLivingBase)player)) {
                return;
            }
            if (gf.passenger != 0) {
                return;
            }
            if (gf.func_94056_bM()) {
                outstring = gf.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Boyfriend";
            }
            gfHealth = (float)gf.getBoyfriendHealth() / gf.func_110138_aP();
        }
        if (entity instanceof ThePrince) {
            gf = null;
            gf = (ThePrince)entity;
            if (!gf.func_152114_e((EntityLivingBase)player)) {
                return;
            }
            if (gf.func_94056_bM()) {
                outstring = gf.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Toddler Prince";
            }
            gfHealth = gf.func_110143_aJ() / gf.func_110138_aP();
        }
        if (entity instanceof ThePrincess) {
            gf = null;
            gf = (ThePrincess)entity;
            if (!gf.func_152114_e((EntityLivingBase)player)) {
                return;
            }
            if (gf.func_94056_bM()) {
                outstring = gf.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Toddler Princess";
            }
            gfHealth = gf.func_110143_aJ() / gf.func_110138_aP();
        }
        if (entity instanceof ThePrinceTeen) {
            gf = null;
            gf = (ThePrinceTeen)entity;
            if (!gf.func_152114_e((EntityLivingBase)player)) {
                return;
            }
            if (gf.func_94056_bM()) {
                outstring = gf.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Young Prince";
            }
            if (gf.getActivity() != 0) {
                return;
            }
            gfHealth = gf.func_110143_aJ() / gf.func_110138_aP();
        }
        if (entity instanceof ThePrinceAdult) {
            gf = null;
            gf = (ThePrinceAdult)entity;
            if (!gf.func_152114_e((EntityLivingBase)player)) {
                return;
            }
            if (gf.func_94056_bM()) {
                outstring = gf.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "The Young Adult Prince";
            }
            if (gf.getActivity() != 0) {
                return;
            }
            gfHealth = gf.func_110143_aJ() / gf.func_110138_aP();
        }
        if (entity instanceof Dragon) {
            Dragon df = null;
            df = (Dragon)entity;
            if (df.func_94056_bM()) {
                outstring = df.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Dragon";
            }
            if (df.getActivity() != 0) {
                return;
            }
            gfHealth = (float)df.getDragonHealth() / df.func_110138_aP();
        }
        if (entity instanceof EmperorScorpion) {
            e = (EmperorScorpion)entity;
            outstring = "Emperor Scorpion";
            gfHealth = (float)e.getEmperorScorpionHealth() / e.func_110138_aP();
        }
        if (entity instanceof Basilisk) {
            e = (Basilisk)entity;
            outstring = "Basilisk";
            gfHealth = (float)e.getBasiliskHealth() / e.func_110138_aP();
        }
        if (entity instanceof Mothra) {
            e = (Mothra)entity;
            outstring = "Mothra!";
            gfHealth = (float)e.getMothraHealth() / e.func_110138_aP();
        }
        if (entity instanceof Spyro) {
            e = (Spyro)entity;
            if (e.func_94056_bM()) {
                outstring = e.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Baby Dragon";
            }
            gfHealth = (float)e.getSpyroHealth() / e.func_110138_aP();
        }
        if (entity instanceof WormLarge) {
            e = (WormLarge)entity;
            if (!e.field_70145_X) {
                outstring = "Worm";
                gfHealth = e.func_110143_aJ() / e.func_110138_aP();
            }
        }
        if (entity instanceof Alien) {
            e = (Alien)entity;
            outstring = "Alien!";
            gfHealth = (float)e.getAlienHealth() / e.func_110138_aP();
        }
        if (entity instanceof WaterDragon) {
            e = (WaterDragon)entity;
            if (e.func_94056_bM()) {
                outstring = e.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "WaterDragon";
            }
            gfHealth = (float)e.getWaterDragonHealth() / e.func_110138_aP();
        }
        if (entity instanceof Kraken) {
            e = (Kraken)entity;
            outstring = "Kraken";
            gfHealth = (float)e.getKrakenHealth() / e.func_110138_aP();
        }
        if (entity instanceof Cephadrome) {
            e = (Cephadrome)entity;
            outstring = "Cephadrome";
            gfHealth = (float)e.getCephadromeHealth() / e.func_110138_aP();
            if (e.getActivity() != 0) {
                return;
            }
        }
        if (entity instanceof TrooperBug) {
            e = (TrooperBug)entity;
            outstring = "Jumpy Bug";
            gfHealth = (float)e.getTrooperBugHealth() / e.func_110138_aP();
        }
        if (entity instanceof SpitBug) {
            e = (SpitBug)entity;
            outstring = "Spit Bug";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof PitchBlack) {
            e = (PitchBlack)entity;
            outstring = "Nightmare";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
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
        if (entity instanceof Robot2) {
            e = (Robot2)entity;
            outstring = "Robo-Pounder";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Robot4) {
            e = (Robot4)entity;
            outstring = "Robo-Warrior";
            gfHealth = (float)e.getRobot4Health() / e.func_110138_aP();
        }
        if (entity instanceof Triffid) {
            e = (Triffid)entity;
            outstring = "Triffid";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Godzilla) {
            e = (Godzilla)entity;
            outstring = "Mobzilla";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Vortex) {
            e = (Vortex)entity;
            outstring = "Vortex";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Irukandji) {
            e = (Irukandji)entity;
            outstring = "Irukandji";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Mantis) {
            e = (Mantis)entity;
            outstring = "Mantis";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof HerculesBeetle) {
            e = (HerculesBeetle)entity;
            outstring = "Hercules Beetle";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof TheKing) {
            e = (TheKing)entity;
            outstring = "The King";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof TheQueen) {
            e = (TheQueen)entity;
            outstring = "The Queen";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof SeaViper) {
            e = (SeaViper)entity;
            outstring = "Sea Viper";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof SeaMonster) {
            e = (SeaMonster)entity;
            outstring = "Sea Monster";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Molenoid) {
            e = (Molenoid)entity;
            outstring = "Molenoid";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof CaterKiller) {
            e = (CaterKiller)entity;
            outstring = "CaterKiller";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Leon) {
            e = (Leon)entity;
            if (e.func_94056_bM()) {
                outstring = e.func_94057_bL();
            }
            if (outstring == null || outstring.equals("")) {
                outstring = "Leonopteryx";
            }
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Hammerhead) {
            e = (Hammerhead)entity;
            outstring = "Hammerhead";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof BandP) {
            e = (BandP)entity;
            outstring = e.getWhat() == 0 ? "Banker" : "Politician";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof SpiderRobot) {
            e = (SpiderRobot)entity;
            outstring = "Giant Robot Spider";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof GiantRobot) {
            e = (GiantRobot)entity;
            outstring = "Jeffery";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof AntRobot) {
            e = (AntRobot)entity;
            outstring = "Giant Robot Red Ant";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (entity instanceof Crab && (myf = (e = (Crab)entity).getCrabScale()) > 0.75f) {
            outstring = "Very Large Crab";
            gfHealth = e.func_110143_aJ() / e.func_110138_aP();
        }
        if (outstring == null) {
            return;
        }
        ScaledResolution res = new ScaledResolution(this.mc, this.mc.field_71443_c, this.mc.field_71440_d);
        int width = res.func_78326_a();
        int barWidthFilled = (int)(gfHealth * (float)(barWidth + 1));
        int x = width / 2 - barWidth / 2;
        int y = 25;
        if (player.func_70055_a(Material.field_151586_h) || player.func_70658_aO() > 0) {
            y -= 10;
        }
        fr.func_78261_a(outstring, width / 2 - fr.func_78256_a(outstring) / 2, y - 10, color);
        this.mc.field_71446_o.func_110577_a(texture);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        this.func_73729_b(x, y, u, v, barWidth, barHeight);
        if (barWidthFilled > 0) {
            this.func_73729_b(x, y, u, v + barHeight, barWidthFilled, barHeight);
        }
    }
}

