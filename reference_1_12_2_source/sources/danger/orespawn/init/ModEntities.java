/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.ResourceLocation
 *  net.minecraftforge.fml.common.registry.EntityRegistry
 */
package danger.orespawn.init;

import danger.orespawn.OreSpawnMain;
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
import danger.orespawn.entity.EntityCage;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {
    public static void registerEntities() {
        ModEntities.myRegisterEntity("alosaurus", Alosaurus.class, 120);
        ModEntities.myRegisterEntity("trex", TRex.class, 121);
        ModEntities.myRegisterEntity("baryonyx", Baryonyx.class, 122);
        ModEntities.myRegisterEntity("camarasaurus", Camarasaurus.class, 123);
        ModEntities.myRegisterEntity("pointysaurus", Pointysaurus.class, 124);
        ModEntities.myRegisterEntity("cryolophosaurus", Cryolophosaurus.class, 126);
        ModEntities.myRegisterEntity("cavefisher", CaveFisher.class, 128);
        ModEntities.myRegisterEntity("red_ant", RedAnt.class, 125);
        ModEntities.myRegisterEntity("bird", Bird.class, 130);
        ModEntities.myRegisterEntity("butterfly", Butterfly.class, 129);
        ModEntities.myRegisterEntity("gammametroid", GammaMetroid.class, 131);
        ModEntities.myRegisterEntity("spyro", Spyro.class, 132);
        ModEntities.myRegisterEntity("dragonfly", Dragonfly.class, 133);
        ModEntities.myRegisterEntity("firefly", Firefly.class, 134);
        ModEntities.myRegisterEntity("mosquito", Mosquito.class, 135);
        ModEntities.myRegisterEntity("nastysaurus", Nastysaurus.class, 136);
        ModEntities.myRegisterEntity("alien", Alien.class, 137);
        ModEntities.myRegisterEntity("velocityraptor", VelocityRaptor.class, 138);
        ModEntities.myRegisterEntity("thrown_critter_cage", EntityCage.class, 150);
        ModEntities.myRegisterEntity("small_worm", WormSmall.class, 139);
        ModEntities.myRegisterEntity("medium_worm", WormMedium.class, 140);
        ModEntities.myRegisterEntity("large_worm", WormLarge.class, 141);
        EntityRegistry.registerModEntity((ResourceLocation)new ResourceLocation("orespawn:doom_worm"), WormDoom.class, (String)"doom_worm", (int)142, (Object)OreSpawnMain.instance, (int)325, (int)1, (boolean)true);
        ModEntities.myRegisterEntity("moth", Moth.class, 143);
        ModEntities.myRegisterEntity("kyuubi", Kyuubi.class, 144);
        ModEntities.myRegisterEntity("mantis", Mantis.class, 145);
        ModEntities.myRegisterEntity("mothra", Mothra.class, 146);
        ModEntities.myRegisterEntity("brutalfly", Brutalfly.class, 147);
        ModEntities.myRegisterEntity("beaver", Beaver.class, 148);
        ModEntities.myRegisterEntity("cassowary", Cassowary.class, 151);
        ModEntities.myRegisterEntity("termite", Termite.class, 149);
        ModEntities.myRegisterEntity("redcow", RedCow.class, 152);
        ModEntities.myRegisterEntity("stinkbug", StinkBug.class, 153);
    }

    public static void myRegisterEntity(String name, Class<? extends Entity> entity, int entity2, int range) {
        EntityRegistry.registerModEntity((ResourceLocation)new ResourceLocation("orespawn:" + name), entity, (String)name, (int)entity2, (Object)OreSpawnMain.instance, (int)range, (int)1, (boolean)true);
    }

    private static void myRegisterEntity(String name, Class<? extends Entity> cls, int id) {
        EntityRegistry.registerModEntity((ResourceLocation)new ResourceLocation("orespawn:" + name), cls, (String)name, (int)id, (Object)OreSpawnMain.instance, (int)50, (int)1, (boolean)true);
    }
}

