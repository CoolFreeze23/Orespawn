package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.CloudShark;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.Coin;
import danger.orespawn.entity.Elevator;
import danger.orespawn.entity.EntityAnt;
import danger.orespawn.entity.EntityBee;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityCannonFodder;
import danger.orespawn.entity.EntityCaterKiller;
import danger.orespawn.entity.EntityCliffRacer;
import danger.orespawn.entity.EntityCricket;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityGammaMetroid;
import danger.orespawn.entity.EntityHerculesBeetle;
import danger.orespawn.entity.EntityMosquito;
import danger.orespawn.entity.EntityRainbowAnt;
import danger.orespawn.entity.EntityRedAnt;
import danger.orespawn.entity.EntityRotator;
import danger.orespawn.entity.EntityRubberDucky;
import danger.orespawn.entity.EntityTermite;
import danger.orespawn.entity.EntityTerribleTerror;
import danger.orespawn.entity.EntityTshirt;
import danger.orespawn.entity.EntityUnstableAnt;
import danger.orespawn.entity.EntityVortex;
import danger.orespawn.entity.EntityWormLarge;
import danger.orespawn.entity.EntityWormMedium;
import danger.orespawn.entity.EntityWormSmall;
import danger.orespawn.entity.Fairy;
import danger.orespawn.entity.Firefly;
import danger.orespawn.entity.GoldFish;
import danger.orespawn.entity.Irukandji;
import danger.orespawn.entity.Island;
import danger.orespawn.entity.IslandToo;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.Robot1;
import danger.orespawn.entity.Robot2;
import danger.orespawn.entity.Robot3;
import danger.orespawn.entity.Robot4;
import danger.orespawn.entity.Robot5;
import danger.orespawn.entity.RockBase;
import danger.orespawn.entity.Skate;
import danger.orespawn.entity.client.AntGeoReplacement;
import danger.orespawn.entity.client.AntRenderer;
import danger.orespawn.entity.client.BeaverGeoReplacedRenderer;
import danger.orespawn.entity.client.BeaverRenderer;
import danger.orespawn.entity.client.BeeGeoReplacement;
import danger.orespawn.entity.client.BeeRenderer;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.BrutalflyRenderer;
import danger.orespawn.entity.client.CannonFodderGeoReplacement;
import danger.orespawn.entity.client.CannonFodderRenderer;
import danger.orespawn.entity.client.CaterKillerGeoReplacement;
import danger.orespawn.entity.client.CaterKillerRenderer;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CliffRacerRenderer;
import danger.orespawn.entity.client.CloudSharkGeoReplacement;
import danger.orespawn.entity.client.CloudSharkRenderer;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CockateilRenderer;
import danger.orespawn.entity.client.CoinGeoReplacement;
import danger.orespawn.entity.client.CoinRenderer;
import danger.orespawn.entity.client.CricketGeoReplacement;
import danger.orespawn.entity.client.CricketRenderer;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.DragonflyRenderer;
import danger.orespawn.entity.client.ElevatorGeoReplacement;
import danger.orespawn.entity.client.ElevatorRenderer;
import danger.orespawn.entity.client.FairyGeoReplacement;
import danger.orespawn.entity.client.FairyRenderer;
import danger.orespawn.entity.client.FireflyGeoReplacement;
import danger.orespawn.entity.client.FireflyRenderer;
import danger.orespawn.entity.client.GammaMetroidGeoReplacement;
import danger.orespawn.entity.client.GammaMetroidRenderer;
import danger.orespawn.entity.client.GoldFishGeoReplacement;
import danger.orespawn.entity.client.GoldFishRenderer;
import danger.orespawn.entity.client.HerculesBeetleGeoReplacement;
import danger.orespawn.entity.client.HerculesBeetleRenderer;
import danger.orespawn.entity.client.IrukandjiGeoReplacement;
import danger.orespawn.entity.client.IrukandjiRenderer;
import danger.orespawn.entity.client.IslandGeoReplacement;
import danger.orespawn.entity.client.IslandRenderer;
import danger.orespawn.entity.client.IslandTooGeoReplacement;
import danger.orespawn.entity.client.IslandTooRenderer;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MosquitoRenderer;
import danger.orespawn.entity.client.PurplePowerGeoReplacement;
import danger.orespawn.entity.client.PurplePowerRenderer;
import danger.orespawn.entity.client.RainbowAntGeoReplacement;
import danger.orespawn.entity.client.RainbowAntRenderer;
import danger.orespawn.entity.client.RedAntGeoReplacement;
import danger.orespawn.entity.client.RedAntRenderer;
import danger.orespawn.entity.client.Robot1GeoReplacement;
import danger.orespawn.entity.client.Robot1Renderer;
import danger.orespawn.entity.client.Robot2GeoReplacement;
import danger.orespawn.entity.client.Robot2Renderer;
import danger.orespawn.entity.client.Robot3GeoReplacement;
import danger.orespawn.entity.client.Robot3Renderer;
import danger.orespawn.entity.client.Robot4GeoReplacement;
import danger.orespawn.entity.client.Robot4Renderer;
import danger.orespawn.entity.client.Robot5GeoReplacement;
import danger.orespawn.entity.client.Robot5Renderer;
import danger.orespawn.entity.client.RockBaseGeoReplacement;
import danger.orespawn.entity.client.RockBaseRenderer;
import danger.orespawn.entity.client.RotatorGeoReplacement;
import danger.orespawn.entity.client.RotatorRenderer;
import danger.orespawn.entity.client.RubberDuckyGeoReplacement;
import danger.orespawn.entity.client.RubberDuckyRenderer;
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.SkateGeoReplacement;
import danger.orespawn.entity.client.SkateRenderer;
import danger.orespawn.entity.client.TermiteGeoReplacement;
import danger.orespawn.entity.client.TermiteRenderer;
import danger.orespawn.entity.client.TerribleTerrorGeoReplacement;
import danger.orespawn.entity.client.TerribleTerrorRenderer;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.TshirtRenderer;
import danger.orespawn.entity.client.UnstableAntGeoReplacement;
import danger.orespawn.entity.client.UnstableAntRenderer;
import danger.orespawn.entity.client.VortexGeoReplacement;
import danger.orespawn.entity.client.VortexRenderer;
import danger.orespawn.entity.client.WormLargeGeoReplacement;
import danger.orespawn.entity.client.WormLargeRenderer;
import danger.orespawn.entity.client.WormMediumGeoReplacement;
import danger.orespawn.entity.client.WormMediumRenderer;
import danger.orespawn.entity.client.WormSmallGeoReplacement;
import danger.orespawn.entity.client.WormSmallRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

/** Resolves the Phase G developer switch into renderer providers at registration time. Species ids are registry names. */
public final class PhaseGDevRenderers {
    private PhaseGDevRenderers() {
    }

    public static EntityRendererProvider<Beaver> beaverRenderer() {
        return select("beaver", BeaverRenderer::new, BeaverGeoReplacedRenderer::new);
    }

    public static EntityRendererProvider<Elevator> elevatorRenderer() {
        return select("elevator", ElevatorRenderer::new, ElevatorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityVortex> vortexRenderer() {
        return select("vortex", VortexRenderer::new, VortexGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Coin> coinRenderer() {
        return select("coin", CoinRenderer::new, CoinGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Island> islandRenderer() {
        return select("island", IslandRenderer::new, IslandGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<IslandToo> islandTooRenderer() {
        return select("island_too", IslandTooRenderer::new, IslandTooGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot1> robot1Renderer() {
        return select("robot_1", Robot1Renderer::new, Robot1GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot2> robot2Renderer() {
        return select("robot_2", Robot2Renderer::new, Robot2GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot3> robot3Renderer() {
        return select("robot_3", Robot3Renderer::new, Robot3GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot4> robot4Renderer() {
        return select("robot_4", Robot4Renderer::new, Robot4GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Robot5> robot5Renderer() {
        return select("robot_5", Robot5Renderer::new, Robot5GeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<RockBase> rockBaseRenderer() {
        return select("rock_base", RockBaseRenderer::new, RockBaseGeoReplacement.Renderer::new);
    }

    /** Slice 4c: the render-instance-expanded rigs. */
    public static EntityRendererProvider<PurplePower> purplePowerRenderer() {
        return select("purple_power", PurplePowerRenderer::new, PurplePowerGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRotator> rotatorRenderer() {
        return select("rotator", RotatorRenderer::new, RotatorGeoReplacement.Renderer::new);
    }

    /** The first Tier-2 slice (2026-09-13): the simple-cyclic rigs, the classic renderers the default. */
    public static EntityRendererProvider<EntityTshirt> tshirtRenderer() {
        return select("tshirt", TshirtRenderer::new, TshirtGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityMosquito> mosquitoRenderer() {
        return select("mosquito", MosquitoRenderer::new, MosquitoGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCliffRacer> cliffRacerRenderer() {
        return select("cliff_racer", CliffRacerRenderer::new, CliffRacerGeoReplacement.Renderer::new);
    }



    public static EntityRendererProvider<EntityBrutalfly> brutalflyRenderer() {
        return select("brutalfly", BrutalflyRenderer::new, BrutalflyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityDragonfly> dragonflyRenderer() {
        return select("dragonfly", DragonflyRenderer::new, DragonflyGeoReplacement.Renderer::new);
    }

    /** One rig, two consumers (design Q9: one profile per registry path): the Cockateil and the Ruby Bird select separately. */
    public static EntityRendererProvider<Cockateil> cockateilRenderer() {
        return select("cockateil", CockateilRenderer::new, CockateilGeoReplacement.Renderer::new);
    }

    /** The classic path draws the Ruby Bird with the Cockateil's renderer (RubyBird extends Cockateil), so both providers are typed over Cockateil, as OreSpawnClient always registered it. */
    public static EntityRendererProvider<Cockateil> rubyBirdRenderer() {
        return select("ruby_bird", CockateilRenderer::new, RubyBirdGeoReplacement.Renderer::new);
    }

    /** The second Tier-2 slice (2026-09-13): the two rigs rejoined under ENT-S-161 (the Cloud Shark waits on the visual leg's tie rule), the classic renderers the default. */
    public static EntityRendererProvider<Firefly> fireflyRenderer() {
        return select("firefly", FireflyRenderer::new, FireflyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<GoldFish> goldFishRenderer() {
        return select("gold_fish", GoldFishRenderer::new, GoldFishGeoReplacement.Renderer::new);
    }

    /** One rig, five consumers (design Q9: one profile per registry path): the Ant and its four siblings select separately, each over its own entity class as the classic renderers are typed. */
    public static EntityRendererProvider<EntityAnt> antRenderer() {
        return select("ant", AntRenderer::new, AntGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRainbowAnt> rainbowAntRenderer() {
        return select("rainbow_ant", RainbowAntRenderer::new, RainbowAntGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRedAnt> redAntRenderer() {
        return select("red_ant", RedAntRenderer::new, RedAntGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityTermite> termiteRenderer() {
        return select("termite", TermiteRenderer::new, TermiteGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityUnstableAnt> unstableAntRenderer() {
        return select("unstable_ant", UnstableAntRenderer::new, UnstableAntGeoReplacement.Renderer::new);
    }


    /** The third Tier-2 slice (2026-09-13): the fifteen rigs ON THE HOOK (Amendment 2), the classic renderers the default. */
    public static EntityRendererProvider<CloudShark> cloudSharkRenderer() {
        return select("cloud_shark", CloudSharkRenderer::new, CloudSharkGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityBee> beeRenderer() {
        return select("bee", BeeRenderer::new, BeeGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Fairy> fairyRenderer() {
        return select("fairy", FairyRenderer::new, FairyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityGammaMetroid> gammaMetroidRenderer() {
        return select("gamma_metroid", GammaMetroidRenderer::new, GammaMetroidGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Irukandji> irukandjiRenderer() {
        return select("irukandji", IrukandjiRenderer::new, IrukandjiGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<Skate> skateRenderer() {
        return select("skate", SkateRenderer::new, SkateGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityRubberDucky> rubberDuckyRenderer() {
        return select("rubber_ducky", RubberDuckyRenderer::new, RubberDuckyGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityTerribleTerror> terribleTerrorRenderer() {
        return select("terrible_terror", TerribleTerrorRenderer::new, TerribleTerrorGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityWormLarge> wormLargeRenderer() {
        return select("worm_large", WormLargeRenderer::new, WormLargeGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityWormMedium> wormMediumRenderer() {
        return select("worm_medium", WormMediumRenderer::new, WormMediumGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityWormSmall> wormSmallRenderer() {
        return select("worm_small", WormSmallRenderer::new, WormSmallGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCannonFodder> cannonFodderRenderer() {
        return select("cannon_fodder", CannonFodderRenderer::new, CannonFodderGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCaterKiller> caterKillerRenderer() {
        return select("cater_killer", CaterKillerRenderer::new, CaterKillerGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityCricket> cricketRenderer() {
        return select("cricket", CricketRenderer::new, CricketGeoReplacement.Renderer::new);
    }

    public static EntityRendererProvider<EntityHerculesBeetle> herculesBeetleRenderer() {
        return select("hercules_beetle", HerculesBeetleRenderer::new, HerculesBeetleGeoReplacement.Renderer::new);
    }


    private static <E extends Entity> EntityRendererProvider<E> select(String species,
                                                                       EntityRendererProvider<E> classic,
                                                                       EntityRendererProvider<E> candidate) {
        if (DevRendererSwitch.geckolib(species) == DevRendererSwitch.Variant.CANDIDATE) {
            OreSpawnMod.LOGGER.warn("Phase G dev switch: {} is using its GeckoLib candidate renderer "
                    + "(selected by -D{}). This is a review build, not a production cutover.",
                    species, DevRendererSwitch.candidateSource(species));
            return candidate;
        }
        return classic;
    }
}
