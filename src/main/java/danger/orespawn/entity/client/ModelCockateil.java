package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Cockateil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelCockateil extends EntityModel<Cockateil> {
    private final ModelPart Body;
    private final ModelPart Head;
    private final ModelPart Beak;
    private final ModelPart LowerBeak;
    private final ModelPart feather2;
    private final ModelPart feather1;
    private final ModelPart feather3;
    private final ModelPart tailfeather1;
    private final ModelPart rwing1;
    private final ModelPart lwing1;
    private final ModelPart leg;
    private final ModelPart otherleg;
    private final ModelPart lwing2;
    private final ModelPart rwing2;
    private final ModelPart tailfeather2;
    private final ModelPart tailfeather3;

    public ModelCockateil(ModelPart root) {
        this.Body = root.getChild("Body");
        this.Head = root.getChild("Head");
        this.Beak = root.getChild("Beak");
        this.LowerBeak = root.getChild("LowerBeak");
        this.feather2 = root.getChild("feather2");
        this.feather1 = root.getChild("feather1");
        this.feather3 = root.getChild("feather3");
        this.tailfeather1 = root.getChild("tailfeather1");
        this.rwing1 = root.getChild("rwing1");
        this.lwing1 = root.getChild("lwing1");
        this.leg = root.getChild("leg");
        this.otherleg = root.getChild("otherleg");
        this.lwing2 = root.getChild("lwing2");
        this.rwing2 = root.getChild("rwing2");
        this.tailfeather2 = root.getChild("tailfeather2");
        this.tailfeather3 = root.getChild("tailfeather3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Body",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 5, 3, 6),
                PartPose.offset(-1.0F, 18.0F, 0.0F));

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(22, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 3, 4),
                PartPose.offset(0.0F, 16.0F, -3.0F));

        partdefinition.addOrReplaceChild("Beak",
                CubeListBuilder.create().texOffs(0, 21).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 3),
                PartPose.offset(1.0F, 17.0F, -6.0F));

        partdefinition.addOrReplaceChild("LowerBeak",
                CubeListBuilder.create().texOffs(1, 17).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 1),
                PartPose.offset(1.0F, 18.0F, -4.0F));

        partdefinition.addOrReplaceChild("feather2",
                CubeListBuilder.create().texOffs(15, 9).mirror().addBox(0.0F, -2.5F, -0.75F, 1, 3, 1),
                PartPose.offsetAndRotation(1.0F, 16.0F, 0.0F, -0.6426736F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("feather1",
                CubeListBuilder.create().texOffs(11, 9).mirror().addBox(0.0F, -2.5F, -0.5F, 1, 3, 1),
                PartPose.offsetAndRotation(1.0F, 16.0F, -2.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("feather3",
                CubeListBuilder.create().texOffs(19, 9).mirror().addBox(0.0F, -3.0F, 0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, -1.276259F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailfeather1",
                CubeListBuilder.create().texOffs(46, 15).mirror().addBox(0.0F, 0.0F, 0.0F, 3, 2, 3),
                PartPose.offset(0.0F, 18.0F, 6.0F));

        partdefinition.addOrReplaceChild("rwing1",
                CubeListBuilder.create().texOffs(23, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 4, 4),
                PartPose.offsetAndRotation(-1.0F, 18.0F, 1.0F, 0.0F, 0.0F, 1.595066F));

        partdefinition.addOrReplaceChild("lwing1",
                CubeListBuilder.create().texOffs(33, 9).mirror().addBox(-1.0F, 0.0F, 0.0F, 1, 4, 4),
                PartPose.offsetAndRotation(4.0F, 18.0F, 1.0F, 0.0F, 0.0F, -1.561488F));

        partdefinition.addOrReplaceChild("leg",
                CubeListBuilder.create().texOffs(4, 12).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(2.0F, 21.0F, 3.0F, 0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("otherleg",
                CubeListBuilder.create().texOffs(0, 12).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 3, 1),
                PartPose.offsetAndRotation(0.0F, 21.0F, 3.0F, 0.6108652F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lwing2",
                CubeListBuilder.create().texOffs(10, 14).mirror().addBox(4.0F, 0.0F, 0.0F, 3, 1, 3),
                PartPose.offset(4.0F, 18.0F, 1.0F));

        partdefinition.addOrReplaceChild("rwing2",
                CubeListBuilder.create().texOffs(10, 19).mirror().addBox(-7.0F, 0.0F, 0.0F, 3, 1, 3),
                PartPose.offset(-1.0F, 18.0F, 1.0F));

        partdefinition.addOrReplaceChild("tailfeather2",
                CubeListBuilder.create().texOffs(44, 20).mirror().addBox(-0.5F, 0.0F, 3.0F, 4, 1, 4),
                PartPose.offset(0.0F, 18.0F, 6.0F));

        partdefinition.addOrReplaceChild("tailfeather3",
                CubeListBuilder.create().texOffs(36, 26).mirror().addBox(-1.0F, 0.0F, 7.0F, 5, 1, 4),
                PartPose.offset(0.0F, 18.0F, 6.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Cockateil entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * 1.5f * limbSwingAmount)) * (float)Math.PI * 0.35f;
        this.lwing1.zRot = -1.5f + newangle;
        this.lwing2.zRot = newangle;
        this.rwing1.zRot = 1.5f - newangle;
        this.rwing2.zRot = -newangle;
        this.tailfeather1.xRot = newangle = Mth.cos((float)(ageInTicks * 0.3f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.tailfeather2.xRot = newangle;
        this.tailfeather3.xRot = newangle;
        this.feather1.zRot = newangle = Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.08f;
        this.feather2.zRot = newangle = Mth.cos((float)(ageInTicks * 1.2f * limbSwingAmount)) * (float)Math.PI * 0.08f;
        this.feather3.zRot = newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.08f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Beak.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.LowerBeak.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feather2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feather1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.feather3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfeather1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.otherleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfeather2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfeather3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
