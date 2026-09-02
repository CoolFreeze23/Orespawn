package danger.orespawn.entity.client;

import danger.orespawn.entity.Beaver;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Selected only through {@link danger.orespawn.client.PhaseGDevRenderers}; classic {@link BeaverRenderer} remains the default. */
public final class BeaverGeoReplacedRenderer extends OreSpawnGeoReplacedEntityRenderer<Beaver, BeaverGeoReplacement> {
    public BeaverGeoReplacedRenderer(EntityRendererProvider.Context context) {
        super(context, new BeaverGeoReplacement());
    }
}
