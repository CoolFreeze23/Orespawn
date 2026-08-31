package danger.orespawn.g1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

/**
 * Deterministic, non-production conversion fixture.
 *
 * <p>Its compiled tree deliberately covers a rotated parent with a nested
 * child, ordinary (non-mirrored) box UVs, and uniform cube inflation. It is
 * compiled and baked by the same probe as production models, but is never
 * packaged into or referenced by the mod runtime.</p>
 */
public final class G1ConverterFixtureModel extends EntityModel<Entity> {
    private final ModelPart parent;

    public G1ConverterFixtureModel(ModelPart root) {
        this.parent = root.getChild("fixture_parent");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition parent = root.addOrReplaceChild(
                "fixture_parent",
                CubeListBuilder.create().texOffs(3, 5)
                        .addBox(-2.0F, -1.0F, -3.0F, 4.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(2.5F, 7.0F, -1.5F,
                        0.23F, -0.31F, 0.17F));
        parent.addOrReplaceChild(
                "fixture_child",
                CubeListBuilder.create().texOffs(24, 9)
                        .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-1.25F, 2.75F, 3.5F,
                        -0.19F, 0.27F, -0.11F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, int color) {
        this.parent.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
