package danger.orespawn.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Beaver;
import danger.orespawn.entity.client.BeaverGeoReplacedRenderer;
import danger.orespawn.entity.client.BeaverRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Resolves the Phase G developer switch into renderer providers at registration time. */
public final class PhaseGDevRenderers {
    private PhaseGDevRenderers() {
    }

    public static EntityRendererProvider<Beaver> beaverRenderer() {
        if (DevRendererSwitch.beaver() == DevRendererSwitch.Variant.CANDIDATE) {
            OreSpawnMod.LOGGER.warn("Phase G dev switch: Beaver is using the GeckoLib candidate renderer "
                    + "(-D{}={}). This is a review build, not a production cutover.",
                    DevRendererSwitch.BEAVER_PROPERTY, DevRendererSwitch.CANDIDATE);
            return BeaverGeoReplacedRenderer::new;
        }
        return BeaverRenderer::new;
    }
}
