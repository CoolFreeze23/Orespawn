package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Island;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/** GeckoLib Island: {@link ModelIsland#setupAnim} verbatim on the converted rig (Tier 3, code-driven per Amendment 1). */
public final class IslandGeoReplacement extends OreSpawnGeoReplacement<Island> {
    private static final GeoReplacementDescriptor<Island> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ISLAND.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
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
        poseIslandRig(processor, inputs.limbSwing(), inputs.limbSwingAmount(), inputs.netHeadYaw(), inputs.headPitch());
    }

    /** Shared with IslandToo: the two classic models have byte-identical setupAnim bodies. */
    static void poseIslandRig(AnimationProcessor<?> processor, float limbSwing, float limbSwingAmount,
                              float netHeadYaw, float headPitch) {
        float angle = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        rotateX(processor, "leg1", angle);
        rotateX(processor, "leg2", -angle);
        rotateX(processor, "leg3", -angle);
        rotateX(processor, "leg4", angle);
        rotateX(processor, "head", headPitch * ((float) Math.PI / 180F));
        rotateY(processor, "head", netHeadYaw * ((float) Math.PI / 180F));
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Island, IslandGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new IslandGeoReplacement());
        }
    }
}
