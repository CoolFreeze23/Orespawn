package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.Elevator;
import danger.orespawn.entity.client.BeaverGeoReplacedRenderer;
import danger.orespawn.entity.client.BeaverRenderer;
import danger.orespawn.entity.client.ElevatorGeoReplacement;
import danger.orespawn.entity.client.ElevatorRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

/** Resolves the Phase G developer switch into renderer providers at registration time. */
public final class PhaseGDevRenderers {
    private PhaseGDevRenderers() {
    }

    public static EntityRendererProvider<Beaver> beaverRenderer() {
        return select("Beaver", BeaverRenderer::new, BeaverGeoReplacedRenderer::new);
    }

    public static EntityRendererProvider<Elevator> elevatorRenderer() {
        return select("Elevator", ElevatorRenderer::new, ElevatorGeoReplacement.Renderer::new);
    }

    private static <E extends Entity> EntityRendererProvider<E> select(String species,
                                                                       EntityRendererProvider<E> classic,
                                                                       EntityRendererProvider<E> candidate) {
        if (DevRendererSwitch.geckolib() == DevRendererSwitch.Variant.CANDIDATE) {
            OreSpawnMod.LOGGER.warn("Phase G dev switch: {} is using its GeckoLib candidate renderer "
                    + "(-D{}={}). This is a review build, not a production cutover.",
                    species, DevRendererSwitch.candidateSource(), DevRendererSwitch.CANDIDATE);
            return candidate;
        }
        return classic;
    }
}
