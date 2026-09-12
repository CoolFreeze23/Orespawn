package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.Cockateil;
import danger.orespawn.entity.Coin;
import danger.orespawn.entity.Elevator;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.EntityCliffRacer;
import danger.orespawn.entity.EntityDragonfly;
import danger.orespawn.entity.EntityMosquito;
import danger.orespawn.entity.EntityRotator;
import danger.orespawn.entity.EntityTshirt;
import danger.orespawn.entity.EntityVortex;
import danger.orespawn.entity.Island;
import danger.orespawn.entity.IslandToo;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.Robot1;
import danger.orespawn.entity.Robot2;
import danger.orespawn.entity.Robot3;
import danger.orespawn.entity.Robot4;
import danger.orespawn.entity.Robot5;
import danger.orespawn.entity.RockBase;
import danger.orespawn.entity.client.BeaverGeoReplacedRenderer;
import danger.orespawn.entity.client.BeaverRenderer;
import danger.orespawn.entity.client.BrutalflyGeoReplacement;
import danger.orespawn.entity.client.BrutalflyRenderer;
import danger.orespawn.entity.client.CliffRacerGeoReplacement;
import danger.orespawn.entity.client.CliffRacerRenderer;
import danger.orespawn.entity.client.CockateilGeoReplacement;
import danger.orespawn.entity.client.CockateilRenderer;
import danger.orespawn.entity.client.CoinGeoReplacement;
import danger.orespawn.entity.client.CoinRenderer;
import danger.orespawn.entity.client.DragonflyGeoReplacement;
import danger.orespawn.entity.client.DragonflyRenderer;
import danger.orespawn.entity.client.ElevatorGeoReplacement;
import danger.orespawn.entity.client.ElevatorRenderer;
import danger.orespawn.entity.client.IslandGeoReplacement;
import danger.orespawn.entity.client.IslandRenderer;
import danger.orespawn.entity.client.IslandTooGeoReplacement;
import danger.orespawn.entity.client.IslandTooRenderer;
import danger.orespawn.entity.client.MosquitoGeoReplacement;
import danger.orespawn.entity.client.MosquitoRenderer;
import danger.orespawn.entity.client.PurplePowerGeoReplacement;
import danger.orespawn.entity.client.PurplePowerRenderer;
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
import danger.orespawn.entity.client.RubyBirdGeoReplacement;
import danger.orespawn.entity.client.TshirtGeoReplacement;
import danger.orespawn.entity.client.TshirtRenderer;
import danger.orespawn.entity.client.VortexGeoReplacement;
import danger.orespawn.entity.client.VortexRenderer;
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
