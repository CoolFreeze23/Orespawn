package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.GoldFish;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelGoldFish extends EntityModel<GoldFish> {
    private final ModelPart Body;
    private final ModelPart Head;
    private final ModelPart Dorsalfin;
    private final ModelPart Mouth;
    private final ModelPart Jaw;
    private final ModelPart Pectoralfin1;
    private final ModelPart Pectoralfin2;
    private final ModelPart Pectoralfin3;
    private final ModelPart Pectoralfin4;
    private final ModelPart Bottomfin;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Caudalfin1;
    private final ModelPart Caudalfin2;
    private final ModelPart Bottomfin1;
    private final ModelPart Bottomfin2;

    public ModelGoldFish(ModelPart root) {
        this.Body = root.getChild("Body");
        this.Head = root.getChild("Head");
        this.Dorsalfin = root.getChild("Dorsalfin");
        this.Mouth = root.getChild("Mouth");
        this.Jaw = root.getChild("Jaw");
        this.Pectoralfin1 = root.getChild("Pectoralfin1");
        this.Pectoralfin2 = root.getChild("Pectoralfin2");
        this.Pectoralfin3 = root.getChild("Pectoralfin3");
        this.Pectoralfin4 = root.getChild("Pectoralfin4");
        this.Bottomfin = root.getChild("Bottomfin");
        this.Tail1 = root.getChild("Tail1");
        this.Tail2 = root.getChild("Tail2");
        this.Caudalfin1 = root.getChild("Caudalfin1");
        this.Caudalfin2 = root.getChild("Caudalfin2");
        this.Bottomfin1 = root.getChild("Bottomfin1");
        this.Bottomfin2 = root.getChild("Bottomfin2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Body",
                CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-2.0F, -2.0F, 0.0F, 4, 4, 10),
                PartPose.offset(0.0F, 14.0F, -5.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-1.5F, -2.0F, -3.0F, 3, 4, 3),
                PartPose.offset(0.0F, 14.0F, -5.0F));

        partdefinition.addOrReplaceChild("Dorsalfin",
                CubeListBuilder.create().texOffs(29, 0).mirror().addBox(0.0F, -6.0F, 0.0F, 0, 4, 10),
                PartPose.offset(0.0F, 14.0F, -5.0F));

        partdefinition.addOrReplaceChild("Mouth",
                CubeListBuilder.create().texOffs(0, 38).mirror().addBox(-1.5F, 0.6F, -3.5F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, 14.0F, -5.0F, -0.7853982F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Jaw",
                CubeListBuilder.create().texOffs(13, 30).mirror().addBox(-1.0F, 0.0F, -3.0F, 3, 1, 3),
                PartPose.offsetAndRotation(-0.5F, 15.6F, -7.4F, -0.2284419F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Pectoralfin1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -1.5F, 0.0F, 0, 3, 5),
                PartPose.offsetAndRotation(-2.0F, 14.0F, -3.0F, -0.2974289F, -0.3346075F, 0.0F));

        partdefinition.addOrReplaceChild("Pectoralfin2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -1.5F, 0.0F, 0, 3, 5),
                PartPose.offsetAndRotation(2.0F, 14.0F, -3.0F, -0.2974216F, 0.3346145F, 0.0F));

        partdefinition.addOrReplaceChild("Pectoralfin3",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -1.5F, 0.0F, 0, 3, 5),
                PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, -0.2974289F, -0.3346075F, 0.0F));

        partdefinition.addOrReplaceChild("Pectoralfin4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -1.5F, 0.0F, 0, 3, 5),
                PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, -0.2974289F, 0.3346145F, 0.0F));

        partdefinition.addOrReplaceChild("Bottomfin",
                CubeListBuilder.create().texOffs(20, 8).mirror().addBox(0.0F, 2.0F, 6.0F, 0, 3, 4),
                PartPose.offset(0.0F, 14.0F, -5.0F));

        partdefinition.addOrReplaceChild("Tail1",
                CubeListBuilder.create().texOffs(29, 15).mirror().addBox(-1.5F, -2.0F, 0.0F, 3, 4, 6),
                PartPose.offset(0.0F, 14.0F, 5.0F));

        partdefinition.addOrReplaceChild("Tail2",
                CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.0F, -1.5F, 6.0F, 2, 3, 4),
                PartPose.offset(0.0F, 14.0F, 5.0F));

        partdefinition.addOrReplaceChild("Caudalfin1",
                CubeListBuilder.create().texOffs(13, 35).mirror().addBox(-0.5F, 5.5F, 6.0F, 1, 3, 4),
                PartPose.offsetAndRotation(0.0F, 14.0F, 5.0F, 0.8179294F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Caudalfin2",
                CubeListBuilder.create().texOffs(15, 35).mirror().addBox(-0.5F, 5.5F, 6.0F, 1, 4, 3),
                PartPose.offsetAndRotation(0.0F, 14.0F, 5.0F, 0.8179294F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Bottomfin1",
                CubeListBuilder.create().texOffs(20, 0).mirror().addBox(-1.0F, 2.0F, 1.0F, 0, 5, 2),
                PartPose.offsetAndRotation(0.0F, 14.0F, -5.0F, 0.2974289F, 0.0F, 0.3346145F));

        partdefinition.addOrReplaceChild("Bottomfin2",
                CubeListBuilder.create().texOffs(20, 0).mirror().addBox(1.0F, 2.0F, 1.0F, 0, 5, 2),
                PartPose.offsetAndRotation(0.0F, 14.0F, -5.0F, 0.2974289F, 0.0F, -0.3346075F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(GoldFish entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.Pectoralfin1.yRot = 0.4f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 1.2f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.Pectoralfin2.yRot = -0.4f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.Pectoralfin3.yRot = 0.4f + newangle;
        newangle = Mth.cos((float)(ageInTicks * 1.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        this.Pectoralfin4.yRot = -0.4f + newangle;
        this.Bottomfin1.yRot = newangle = Mth.cos((float)(ageInTicks * 1.7f * limbSwingAmount)) * (float)Math.PI * 0.25f;
        this.Bottomfin2.yRot = -newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.7f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.Jaw.xRot = -0.25f + newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Dorsalfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Pectoralfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Pectoralfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Pectoralfin3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Pectoralfin4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Bottomfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Caudalfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Caudalfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Bottomfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Bottomfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
