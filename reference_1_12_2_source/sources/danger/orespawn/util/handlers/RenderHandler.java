/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.client.registry.RenderingRegistry
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package danger.orespawn.util.handlers;

import danger.orespawn.entity.Alien;
import danger.orespawn.entity.Alosaurus;
import danger.orespawn.entity.Baryonyx;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.Bird;
import danger.orespawn.entity.Brutalfly;
import danger.orespawn.entity.Butterfly;
import danger.orespawn.entity.Camarasaurus;
import danger.orespawn.entity.Cassowary;
import danger.orespawn.entity.CaveFisher;
import danger.orespawn.entity.Cryolophosaurus;
import danger.orespawn.entity.Dragonfly;
import danger.orespawn.entity.Firefly;
import danger.orespawn.entity.GammaMetroid;
import danger.orespawn.entity.Kyuubi;
import danger.orespawn.entity.Mantis;
import danger.orespawn.entity.Mosquito;
import danger.orespawn.entity.Moth;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.Pointysaurus;
import danger.orespawn.entity.RedAnt;
import danger.orespawn.entity.RedCow;
import danger.orespawn.entity.Spyro;
import danger.orespawn.entity.StinkBug;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.Termite;
import danger.orespawn.entity.VelocityRaptor;
import danger.orespawn.entity.WormDoom;
import danger.orespawn.entity.WormLarge;
import danger.orespawn.entity.WormMedium;
import danger.orespawn.entity.WormSmall;
import danger.orespawn.entity.render.RenderAlien;
import danger.orespawn.entity.render.RenderAlosaurus;
import danger.orespawn.entity.render.RenderBaryonyx;
import danger.orespawn.entity.render.RenderBeaver;
import danger.orespawn.entity.render.RenderBird;
import danger.orespawn.entity.render.RenderBrutalfly;
import danger.orespawn.entity.render.RenderButterfly;
import danger.orespawn.entity.render.RenderCamarasaurus;
import danger.orespawn.entity.render.RenderCassowary;
import danger.orespawn.entity.render.RenderCaveFisher;
import danger.orespawn.entity.render.RenderCryolophosaurus;
import danger.orespawn.entity.render.RenderDragonfly;
import danger.orespawn.entity.render.RenderEnchantedCow;
import danger.orespawn.entity.render.RenderFirefly;
import danger.orespawn.entity.render.RenderGammaMetroid;
import danger.orespawn.entity.render.RenderKyuubi;
import danger.orespawn.entity.render.RenderMantis;
import danger.orespawn.entity.render.RenderMosquito;
import danger.orespawn.entity.render.RenderMoth;
import danger.orespawn.entity.render.RenderMothra;
import danger.orespawn.entity.render.RenderNastysaurus;
import danger.orespawn.entity.render.RenderPointysaurus;
import danger.orespawn.entity.render.RenderRedAnt;
import danger.orespawn.entity.render.RenderSpyro;
import danger.orespawn.entity.render.RenderStinkBug;
import danger.orespawn.entity.render.RenderTRex;
import danger.orespawn.entity.render.RenderTermite;
import danger.orespawn.entity.render.RenderVelocityRaptor;
import danger.orespawn.entity.render.RenderWormDoom;
import danger.orespawn.entity.render.RenderWormLarge;
import danger.orespawn.entity.render.RenderWormMedium;
import danger.orespawn.entity.render.RenderWormSmall;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
public class RenderHandler {
    public static void registerEntityRenders() {
        RenderingRegistry.registerEntityRenderingHandler(Alosaurus.class, RenderAlosaurus::new);
        RenderingRegistry.registerEntityRenderingHandler(TRex.class, RenderTRex::new);
        RenderingRegistry.registerEntityRenderingHandler(Baryonyx.class, RenderBaryonyx::new);
        RenderingRegistry.registerEntityRenderingHandler(Camarasaurus.class, RenderCamarasaurus::new);
        RenderingRegistry.registerEntityRenderingHandler(RedAnt.class, RenderRedAnt::new);
        RenderingRegistry.registerEntityRenderingHandler(Cryolophosaurus.class, RenderCryolophosaurus::new);
        RenderingRegistry.registerEntityRenderingHandler(Pointysaurus.class, RenderPointysaurus::new);
        RenderingRegistry.registerEntityRenderingHandler(CaveFisher.class, RenderCaveFisher::new);
        RenderingRegistry.registerEntityRenderingHandler(Butterfly.class, RenderButterfly::new);
        RenderingRegistry.registerEntityRenderingHandler(Bird.class, RenderBird::new);
        RenderingRegistry.registerEntityRenderingHandler(GammaMetroid.class, RenderGammaMetroid::new);
        RenderingRegistry.registerEntityRenderingHandler(Spyro.class, RenderSpyro::new);
        RenderingRegistry.registerEntityRenderingHandler(Dragonfly.class, RenderDragonfly::new);
        RenderingRegistry.registerEntityRenderingHandler(Firefly.class, RenderFirefly::new);
        RenderingRegistry.registerEntityRenderingHandler(Mosquito.class, RenderMosquito::new);
        RenderingRegistry.registerEntityRenderingHandler(Nastysaurus.class, RenderNastysaurus::new);
        RenderingRegistry.registerEntityRenderingHandler(Alien.class, RenderAlien::new);
        RenderingRegistry.registerEntityRenderingHandler(VelocityRaptor.class, RenderVelocityRaptor::new);
        RenderingRegistry.registerEntityRenderingHandler(WormSmall.class, RenderWormSmall::new);
        RenderingRegistry.registerEntityRenderingHandler(WormMedium.class, RenderWormMedium::new);
        RenderingRegistry.registerEntityRenderingHandler(WormLarge.class, RenderWormLarge::new);
        RenderingRegistry.registerEntityRenderingHandler(WormDoom.class, RenderWormDoom::new);
        RenderingRegistry.registerEntityRenderingHandler(Moth.class, RenderMoth::new);
        RenderingRegistry.registerEntityRenderingHandler(Kyuubi.class, RenderKyuubi::new);
        RenderingRegistry.registerEntityRenderingHandler(Mantis.class, RenderMantis::new);
        RenderingRegistry.registerEntityRenderingHandler(Mothra.class, RenderMothra::new);
        RenderingRegistry.registerEntityRenderingHandler(Brutalfly.class, RenderBrutalfly::new);
        RenderingRegistry.registerEntityRenderingHandler(Beaver.class, RenderBeaver::new);
        RenderingRegistry.registerEntityRenderingHandler(Termite.class, RenderTermite::new);
        RenderingRegistry.registerEntityRenderingHandler(Cassowary.class, RenderCassowary::new);
        RenderingRegistry.registerEntityRenderingHandler(RedCow.class, RenderEnchantedCow::new);
        RenderingRegistry.registerEntityRenderingHandler(StinkBug.class, RenderStinkBug::new);
    }
}

