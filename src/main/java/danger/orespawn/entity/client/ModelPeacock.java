package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Peacock;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelPeacock extends EntityModel<Peacock> {
    private final ModelPart lleg;
    private final ModelPart rleg;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart hf1;
    private final ModelPart hf2;
    private final ModelPart hf3;
    private final ModelPart tailf1;
    private final ModelPart tailf2;
    private final ModelPart tailf3;
    private final ModelPart tailf4;
    private final ModelPart tailf5;
    private final ModelPart tailf6;
    private final ModelPart tailf7;

    public ModelPeacock(ModelPart root) {
        this.lleg = root.getChild("lleg");
        this.rleg = root.getChild("rleg");
        this.body = root.getChild("body");
        this.neck = root.getChild("neck");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.hf1 = root.getChild("hf1");
        this.hf2 = root.getChild("hf2");
        this.hf3 = root.getChild("hf3");
        this.tailf1 = root.getChild("tailf1");
        this.tailf2 = root.getChild("tailf2");
        this.tailf3 = root.getChild("tailf3");
        this.tailf4 = root.getChild("tailf4");
        this.tailf5 = root.getChild("tailf5");
        this.tailf6 = root.getChild("tailf6");
        this.tailf7 = root.getChild("tailf7");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("lleg",
                CubeListBuilder.create().texOffs(0, 20).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("rleg",
                CubeListBuilder.create().texOffs(5, 20).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(-1.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(88, 0).mirror().addBox(-2.0F, -2.0F, -5.0F, 5, 4, 11),
                PartPose.offsetAndRotation(0.0F, 15.0F, 1.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(70, 0).mirror().addBox(-0.5F, -1.0F, -6.0F, 2, 2, 6),
                PartPose.offsetAndRotation(0.0F, 14.0F, -3.0F, -0.5585054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-0.5F, -2.0F, -2.0F, 2, 2, 4),
                PartPose.offset(0.0F, 12.0F, -8.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(48, 0).mirror().addBox(0.0F, -1.0F, -4.0F, 1, 1, 2),
                PartPose.offset(0.0F, 12.0F, -8.0F));

        partdefinition.addOrReplaceChild("hf1",
                CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.5F, -9.0F, -1.5F, 0, 7, 3),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4014257F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hf2",
                CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.5F, -9.0F, -1.5F, 0, 7, 3),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hf3",
                CubeListBuilder.create().texOffs(8, 0).mirror().addBox(0.5F, -9.0F, -1.5F, 0, 7, 3),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailf1",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.5F, 14.0F, 7.0F));

        partdefinition.addOrReplaceChild("tailf2",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.5F, 14.0F, 7.0F));

        partdefinition.addOrReplaceChild("tailf3",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.5F, 14.0F, 7.0F));

        partdefinition.addOrReplaceChild("tailf4",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.5F, 14.0F, 7.0F));

        partdefinition.addOrReplaceChild("tailf5",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.5F, 14.0F, 7.0F));

        partdefinition.addOrReplaceChild("tailf6",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.5F, 14.0F, 7.0F));

        partdefinition.addOrReplaceChild("tailf7",
                CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-4.0F, 0.0F, 0.0F, 8, 0, 30),
                PartPose.offset(0.514F, 14.0F, 7.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Peacock entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.15f * limbSwingAmount : 0.0f;
        this.lleg.xRot = newangle;
        this.rleg.xRot = -newangle;
        if (entity.getBlink() > 0) {
        this.hf1.xRot = 0.401f;
        this.hf2.xRot = -0.174f;
        this.hf3.xRot = -0.698f;
        this.tailf1.xRot = 1.047f;
        this.tailf2.xRot = 1.047f;
        this.tailf3.xRot = 1.047f;
        this.tailf4.xRot = 1.047f;
        this.tailf5.xRot = 1.047f;
        this.tailf6.xRot = 1.047f;
        this.tailf7.xRot = 1.047f;
        this.tailf1.zRot = -0.4f;
        this.tailf2.zRot = -0.8f;
        this.tailf3.zRot = -1.2f;
        this.tailf4.zRot = 0.4f;
        this.tailf5.zRot = 0.8f;
        this.tailf6.zRot = 1.2f;
        } else {
        this.hf1.xRot = -1.06f;
        this.hf2.xRot = -1.06f;
        this.hf3.xRot = -1.06f;
        this.tailf1.xRot = 0.0f;
        this.tailf2.xRot = 0.0f;
        this.tailf3.xRot = 0.0f;
        this.tailf4.xRot = 0.0f;
        this.tailf5.xRot = 0.0f;
        this.tailf6.xRot = 0.0f;
        this.tailf7.xRot = 0.0f;
        this.tailf1.zRot = 0.0f;
        this.tailf2.zRot = 0.0f;
        this.tailf3.zRot = 0.0f;
        this.tailf4.zRot = 0.0f;
        this.tailf5.zRot = 0.0f;
        this.tailf6.zRot = 0.0f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.lleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailf7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
