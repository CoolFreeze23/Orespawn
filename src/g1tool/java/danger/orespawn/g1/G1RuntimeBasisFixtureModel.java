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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Deterministic, non-production RUNTIME BASIS fixture (Slice 4b).
 *
 * <p>Its {@link #setupAnim} writes every channel a code-driven species can
 * write: X, Y and Z rotations and X, Y and Z pivot positions, on a rotated,
 * inflated parent and on a nested child, driven by all five animation inputs.
 * The paired {@link G1RuntimeBasisFixtureReplacement} writes the same pose
 * through the production {@code OreSpawnGeoReplacement} helpers, so the
 * surface-mapping leg proves the helpers' basis conversion geometrically,
 * and the animation leg proves the probe's bone-value conversion. Never
 * packaged into the mod.</p>
 */
public final class G1RuntimeBasisFixtureModel extends EntityModel<Entity> {
    private final ModelPart parent;
    private final ModelPart child;

    public G1RuntimeBasisFixtureModel(ModelPart root) {
        this.parent = root.getChild("basis_parent");
        this.child = this.parent.getChild("basis_child");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition parent = root.addOrReplaceChild(
                "basis_parent",
                CubeListBuilder.create().texOffs(3, 5)
                        .addBox(-2.0F, -1.0F, -3.0F, 4.0F, 3.0F, 5.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(2.5F, 7.0F, -1.5F,
                        0.23F, -0.31F, 0.17F));
        parent.addOrReplaceChild(
                "basis_child",
                CubeListBuilder.create().texOffs(24, 9)
                        .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(-1.25F, 2.75F, 3.5F,
                        -0.19F, 0.27F, -0.11F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.parent.xRot = 0.23F + 0.05F * ageInTicks;
        this.parent.yRot = -0.31F + Mth.cos(ageInTicks * 0.7F) * 0.4F;
        this.parent.zRot = 0.17F + Mth.sin(ageInTicks * 0.5F) * 0.3F;
        this.parent.x = 2.5F + 0.1F * ageInTicks;
        this.parent.y = 7.0F + headPitch * 0.05F;
        this.parent.z = -1.5F + 0.25F * ageInTicks;

        this.child.xRot = -0.19F + netHeadYaw * 0.01F;
        this.child.yRot = 0.27F + 0.02F * ageInTicks;
        this.child.zRot = -0.11F - 0.03F * ageInTicks;
        this.child.x = -1.25F + 0.5F * limbSwing * limbSwingAmount;
        this.child.y = 2.75F + 1.5F * Mth.cos(ageInTicks * 0.3F);
        this.child.z = 3.5F - 2.0F * Mth.sin(ageInTicks * 0.3F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, int color) {
        this.parent.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
