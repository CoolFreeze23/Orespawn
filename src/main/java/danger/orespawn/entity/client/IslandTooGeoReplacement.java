package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.IslandToo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/** GeckoLib IslandToo: {@link ModelIslandToo#setupAnim} verbatim on the converted rig (Tier 3, code-driven per Amendment 1). */
public final class IslandTooGeoReplacement extends OreSpawnGeoReplacement<IslandToo> {
    private static final GeoReplacementDescriptor<IslandToo> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ISLAND_TOO.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            IslandToo.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/islandtoo.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/islandtoo.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/islandtoo.png"),
            0.25F) {
    };

    public IslandTooGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        IslandGeoReplacement.poseIslandRig(processor, inputs.limbSwing(), inputs.limbSwingAmount(),
                inputs.netHeadYaw(), inputs.headPitch());
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<IslandToo, IslandTooGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new IslandTooGeoReplacement());
        }
    }
}
