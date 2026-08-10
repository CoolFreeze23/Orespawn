package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import danger.orespawn.entity.Elevator;

public class ModelElevator extends EntityModel<Elevator> {
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;
    private final ModelPart shape4;
    private final ModelPart shape5;

    public ModelElevator(ModelPart root) {
        this.shape1 = root.getChild("shape1");
        this.shape2 = root.getChild("shape2");
        this.shape3 = root.getChild("shape3");
        this.shape4 = root.getChild("shape4");
        this.shape5 = root.getChild("shape5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // TF-029: every shape is baked 24 px (1.5 blocks) down. The original
        // ModelElevator (orig ModelElevator.java:22-51) put all five boxes at
        // model y 0..1 with rotation point (0,0,0), but rendered them through a
        // boat-style Render (orig RenderElevator.java:27-45) that translated
        // straight to the entity position with NO -1.5 living-model offset, so
        // the deck top sat at the board's posY. The modern MobRenderer/
        // EntityModel pipeline anchors the model root 1.501 blocks above the
        // entity origin (Y-down), so the verbatim coordinates rendered the deck
        // ~1.5 blocks too high; the +24 px offset re-anchors the deck top at
        // ~posY, reproducing the original boat-style anchor.
        partdefinition.addOrReplaceChild("shape1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-4.0F, 0.0F, -8.0F, 8, 1, 16),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape2",
                CubeListBuilder.create().texOffs(0, 18).mirror()
                        .addBox(-3.0F, 0.0F, -9.0F, 6, 1, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape3",
                CubeListBuilder.create().texOffs(0, 21).mirror()
                        .addBox(-1.0F, 0.0F, -10.0F, 2, 1, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape4",
                CubeListBuilder.create().texOffs(17, 18).mirror()
                        .addBox(-3.0F, 0.0F, 8.0F, 6, 1, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape5",
                CubeListBuilder.create().texOffs(17, 21).mirror()
                        .addBox(-1.0F, 0.0F, 9.0F, 2, 1, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Elevator entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        shape1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        shape5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
