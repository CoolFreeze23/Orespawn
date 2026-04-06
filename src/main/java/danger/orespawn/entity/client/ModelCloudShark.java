package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.CloudShark;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCloudShark extends EntityModel<CloudShark> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart topfin;
    private final ModelPart bbody;
    private final ModelPart fins;
    private final ModelPart leftfin;
    private final ModelPart rightfin;

    public ModelCloudShark(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
        this.topfin = root.getChild("topfin");
        this.bbody = root.getChild("bbody");
        this.fins = root.getChild("fins");
        this.leftfin = root.getChild("leftfin");
        this.rightfin = root.getChild("rightfin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 6, 8, 15),
                PartPose.offset(-4.0F, 11.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.5F, 0.0F, -8.0F, 5, 5, 8),
                PartPose.offset(-1.0F, 11.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(42, 0).mirror().addBox(-2.5F, 0.0F, -6.0F, 5, 2, 6),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.5056291F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topfin",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 3, 6),
                PartPose.offsetAndRotation(-1.5F, 11.0F, 5.0F, 0.935765F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bbody",
                CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-2.0F, 0.0F, 0.0F, 4, 8, 6),
                PartPose.offset(-1.0F, 11.0F, 15.0F));

        partdefinition.addOrReplaceChild("fins",
                CubeListBuilder.create().texOffs(0, 24).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 10, 10),
                PartPose.offsetAndRotation(-1.0F, 16.0F, 16.0F, 0.9220296F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftfin",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 3, 7),
                PartPose.offsetAndRotation(2.0F, 16.0F, 6.0F, -0.6108652F, 1.134464F, -0.6108652F));

        partdefinition.addOrReplaceChild("rightfin",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 3, 7),
                PartPose.offsetAndRotation(-4.0F, 16.0F, 6.0F, -0.6283185F, -1.134464F, 0.6108652F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(CloudShark entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * 0.7f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.leftfin.yRot = 1.15f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 1.5f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.rightfin.yRot = -0.9f + newangle;
        this.fins.yRot = newangle = Mth.cos((float)(ageInTicks * 1.5f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        newangle = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.jaw.xRot = 0.5f + newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bbody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fins.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
