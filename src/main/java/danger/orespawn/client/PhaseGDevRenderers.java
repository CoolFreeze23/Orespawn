package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.Elevator;
import danger.orespawn.entity.EntityVortex;
import danger.orespawn.entity.client.BeaverGeoReplacedRenderer;
import danger.orespawn.entity.client.BeaverRenderer;
import danger.orespawn.entity.client.ElevatorGeoReplacement;
import danger.orespawn.entity.client.ElevatorRenderer;
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
