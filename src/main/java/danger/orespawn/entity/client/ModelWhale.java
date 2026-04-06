package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ModelWhale extends EntityModel<Entity> {
    private final ModelPart belly;
    private final ModelPart body;
    private final ModelPart back;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tailfin1;
    private final ModelPart tailfin2;
    private final ModelPart backfin;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart lfin1;
    private final ModelPart lfin2;
    private final ModelPart rfin1;
    private final ModelPart rfin2;

    public ModelWhale(ModelPart root) {
        this.belly = root.getChild("belly");
        this.body = root.getChild("body");
        this.back = root.getChild("back");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tailfin1 = root.getChild("tailfin1");
        this.tailfin2 = root.getChild("tailfin2");
        this.backfin = root.getChild("backfin");
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
        this.lfin1 = root.getChild("lfin1");
        this.lfin2 = root.getChild("lfin2");
        this.rfin1 = root.getChild("rfin1");
        this.rfin2 = root.getChild("rfin2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("belly",
                CubeListBuilder.create().texOffs(0, 92).mirror()
                        .addBox(-6.0F, 0.0F, 0.0F, 12, 2, 32),
                PartPose.offset(0.0F, 22.0F, 6.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 188).mirror()
                        .addBox(-10.0F, 0.0F, 0.0F, 20, 12, 52),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        partdefinition.addOrReplaceChild("back",
                CubeListBuilder.create().texOffs(0, 45).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 2, 40),
                PartPose.offset(0.0F, 8.0F, 3.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(186, 0).mirror()
                        .addBox(-6.0F, 0.0F, 0.0F, 12, 7, 14),
                PartPose.offset(0.0F, 11.0F, 52.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(186, 24).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 5, 10),
                PartPose.offset(0.0F, 12.0F, 66.0F));

        partdefinition.addOrReplaceChild("tailfin1",
                CubeListBuilder.create().texOffs(186, 43).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 17, 2, 11),
                PartPose.offsetAndRotation(2.0F, 13.0F, 74.0F, 0.0872665F, -0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("tailfin2",
                CubeListBuilder.create().texOffs(186, 59).mirror()
                        .addBox(-17.0F, 0.0F, 0.0F, 17, 2, 11),
                PartPose.offsetAndRotation(-2.0F, 13.0F, 74.0F, 0.0872665F, 0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("backfin",
                CubeListBuilder.create().texOffs(0, 15).mirror()
                        .addBox(-0.5F, 0.0F, 0.0F, 1, 4, 8),
                PartPose.offsetAndRotation(0.0F, 8.0F, 11.0F, 0.3665191F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 155).mirror()
                        .addBox(-8.0F, 0.0F, -16.0F, 16, 8, 22),
                PartPose.offset(0.0F, 11.0F, -6.0F));

        partdefinition.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 130).mirror()
                        .addBox(-7.0F, -1.0F, -20.0F, 14, 2, 20),
                PartPose.offsetAndRotation(0.0F, 20.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfin1",
                CubeListBuilder.create().texOffs(96, 0).mirror()
                        .addBox(0.0F, -1.0F, -3.0F, 4, 3, 6),
                PartPose.offsetAndRotation(10.0F, 18.0F, 8.0F, 0.0F, -0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("lfin2",
                CubeListBuilder.create().texOffs(120, 0).mirror()
                        .addBox(2.0F, -0.5F, -3.0F, 22, 2, 8),
                PartPose.offsetAndRotation(10.0F, 18.0F, 8.0F, 0.0F, -0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("rfin1",
                CubeListBuilder.create().texOffs(96, 12).mirror()
                        .addBox(-4.0F, -1.0F, -3.0F, 4, 3, 6),
                PartPose.offsetAndRotation(-10.0F, 18.0F, 8.0F, 0.0F, 0.0872665F, 0.0F));

        partdefinition.addOrReplaceChild("rfin2",
                CubeListBuilder.create().texOffs(120, 13).mirror()
                        .addBox(-24.0F, -0.5F, -3.0F, 22, 2, 8),
                PartPose.offsetAndRotation(-10.0F, 18.0F, 8.0F, 0.0F, 0.0872665F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle;
        newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.2F * limbSwingAmount
                : Mth.cos(ageInTicks * 0.08F) * (float) Math.PI * 0.05F;

        this.lfin2.zRot = 0.436F + newangle;
        this.lfin1.zRot = this.lfin2.zRot / 2.0F;
        this.rfin2.zRot = -0.436F - newangle;
        this.rfin1.zRot = this.rfin2.zRot / 2.0F;

        newangle = Mth.cos(ageInTicks * 0.03F) * (float) Math.PI * 0.02F;
        this.jaw.xRot = 0.087F + newangle;

        newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 0.4F) * (float) Math.PI * 0.16F * limbSwingAmount
                : Mth.cos(ageInTicks * 0.05F) * (float) Math.PI * 0.03F;

        this.tail1.xRot = newangle * 0.5F;
        this.tail2.xRot = newangle * 1.25F;
        this.tailfin1.xRot = this.tailfin2.xRot = newangle * 2.25F;

        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.xRot) * 14.0F;
        this.tail2.y = this.tail1.y - (float) Math.sin(this.tail1.xRot) * 14.0F;

        this.tailfin1.z = this.tailfin2.z = this.tail2.z + (float) Math.cos(this.tail2.xRot) * 8.0F;
        this.tailfin1.y = this.tailfin2.y = this.tail2.y - (float) Math.sin(this.tail2.xRot) * 8.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.belly.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backfin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
