package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityVortex;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimatableManager;

/**
 * GeckoLib Vortex: one static zero-thickness billboard quad; nothing animates (classic {@code VortexModel.setupAnim} is empty).
 * Shadow is {@link VortexRenderer#SHADOW} (0.1 x 1.0, ENT-S-092, from ClientProxyOreSpawn.java:480 / RenderVortex.java:23).
 */
public final class VortexGeoReplacement extends OreSpawnGeoReplacement<EntityVortex> {
    private static final GeoReplacementDescriptor<EntityVortex> DESCRIPTOR = new GeoReplacementDescriptor<>(
            ModEntities.ENTITY_VORTEX::get,
            EntityVortex.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/vortex.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/vortex.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/vortex.png"),
            VortexRenderer.SHADOW) {
    };

    public VortexGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityVortex, VortexGeoReplacement> {
        public Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
            super(context, new VortexGeoReplacement());
        }
    }
}
