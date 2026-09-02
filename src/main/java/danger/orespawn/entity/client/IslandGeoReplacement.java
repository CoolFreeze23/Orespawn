package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Island;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/** GeckoLib Island: {@link ModelIsland#setupAnim} verbatim on the converted 1.7.10 rig (ENT-S-091 re-proof). */
public final class IslandGeoReplacement extends OreSpawnGeoReplacement<Island> {
    private static final GeoReplacementDescriptor<Island> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ISLAND.get(),
            Island.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/island.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/island.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/island.png"),
            0.25F) {
    };

    public IslandGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        poseIslandRig(processor, inputs.ageInTicks());
    }

    /** Shared with IslandToo: the same nine cosines on Shape1..Shape3. */
    static void poseIslandRig(AnimationProcessor<?> processor, float ageInTicks) {
        float w = ModelIsland.WINGSPEED;
        rotateX(processor, "Shape1", Mth.cos(ageInTicks * 0.05F * w) * (float) Math.PI);
        rotateY(processor, "Shape1", Mth.cos(ageInTicks * 0.051F * w) * (float) Math.PI);
        rotateZ(processor, "Shape1", Mth.cos(ageInTicks * 0.052F * w) * (float) Math.PI);
        rotateX(processor, "Shape2", Mth.cos(ageInTicks * 0.053F * w) * (float) Math.PI);
        rotateY(processor, "Shape2", Mth.cos(ageInTicks * 0.054F * w) * (float) Math.PI);
        rotateZ(processor, "Shape2", Mth.cos(ageInTicks * 0.055F * w) * (float) Math.PI);
        rotateX(processor, "Shape3", Mth.cos(ageInTicks * 0.056F * w) * (float) Math.PI);
        rotateY(processor, "Shape3", Mth.cos(ageInTicks * 0.057F * w) * (float) Math.PI);
        rotateZ(processor, "Shape3", Mth.cos(ageInTicks * 0.058F * w) * (float) Math.PI);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Island, IslandGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new IslandGeoReplacement());
        }
    }
}
