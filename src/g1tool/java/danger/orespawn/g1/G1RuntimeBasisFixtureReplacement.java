package danger.orespawn.g1;

import danger.orespawn.entity.client.GeoReplacementDescriptor;
import danger.orespawn.entity.client.OreSpawnGeoReplacement;
import danger.orespawn.entity.client.PoseInputs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * The production-helper side of {@link G1RuntimeBasisFixtureModel}: the same
 * pose written through {@code OreSpawnGeoReplacement}'s classic-vocabulary
 * helpers. Non-production; it has no entity type and is never registered.
 */
public final class G1RuntimeBasisFixtureReplacement extends OreSpawnGeoReplacement<Entity> {
    private static final ResourceLocation FIXTURE =
            ResourceLocation.fromNamespaceAndPath("orespawn", "g1/fixture_runtime_basis_yz");
    private static final GeoReplacementDescriptor<Entity> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> {
                throw new IllegalStateException("non-production fixture has no entity type");
            },
            Entity.class, FIXTURE, FIXTURE, FIXTURE, 0.0F) {
    };

    public G1RuntimeBasisFixtureReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwing = inputs.limbSwing();
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();

        rotateX(processor, "basis_parent", 0.23F + 0.05F * ageInTicks);
        rotateY(processor, "basis_parent", -0.31F + Mth.cos(ageInTicks * 0.7F) * 0.4F);
        rotateZ(processor, "basis_parent", 0.17F + Mth.sin(ageInTicks * 0.5F) * 0.3F);
        moveTo(processor, "basis_parent",
                2.5F + 0.1F * ageInTicks,
                7.0F + headPitch * 0.05F,
                -1.5F + 0.25F * ageInTicks);

        rotateX(processor, "basis_child", -0.19F + netHeadYaw * 0.01F);
        rotateY(processor, "basis_child", 0.27F + 0.02F * ageInTicks);
        rotateZ(processor, "basis_child", -0.11F - 0.03F * ageInTicks);
        moveTo(processor, "basis_child",
                -1.25F + 0.5F * limbSwing * limbSwingAmount,
                2.75F + 1.5F * Mth.cos(ageInTicks * 0.3F),
                3.5F - 2.0F * Mth.sin(ageInTicks * 0.3F));
    }
}
