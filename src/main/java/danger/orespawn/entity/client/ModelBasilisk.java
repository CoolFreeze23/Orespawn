package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Basilisk;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelBasilisk extends EntityModel<Basilisk> {
    private final float wingspeed = 1.0F;
    private final ModelPart body3;
    private final ModelPart body2;
    private final ModelPart body1;
    private final ModelPart body4;
    private final ModelPart body5;
    private final ModelPart body6;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart neck2;
    private final ModelPart neck1;
    private final ModelPart head;
    private final ModelPart rog1;
    private final ModelPart rog2;
    private final ModelPart rog3;
    private final ModelPart rog4;
    private final ModelPart rog5;
    private final ModelPart rog6;
    private final ModelPart snout;
    private final ModelPart jaw;

    public ModelBasilisk(ModelPart root) {
        this.body3 = root.getChild("body3");
        this.body2 = root.getChild("body2");
        this.body1 = root.getChild("body1");
        this.body4 = root.getChild("body4");
        this.body5 = root.getChild("body5");
        this.body6 = root.getChild("body6");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.neck2 = root.getChild("neck2");
        this.neck1 = root.getChild("neck1");
        this.head = root.getChild("head");
        this.rog1 = root.getChild("rog1");
        this.rog2 = root.getChild("rog2");
        this.rog3 = root.getChild("rog3");
        this.rog4 = root.getChild("rog4");
        this.rog5 = root.getChild("rog5");
        this.rog6 = root.getChild("rog6");
        this.snout = root.getChild("snout");
        this.jaw = root.getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body3", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offset(-8.0F, 8.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offsetAndRotation(-8.0F, 4.0F, -10.0F, -0.2974289F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body1", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offsetAndRotation(-8.0F, 2.0F, -25.0F, -0.1487144F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body4", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offsetAndRotation(-8.0F, 8.0F, 13.0F, 0.1487144F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body5", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offset(-8.0F, 5.8F, 28.8F));

        partdefinition.addOrReplaceChild("body6", CubeListBuilder.create()
            .texOffs(148, 4).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 15.0F, 15.0F, 17.0F),
            PartPose.offsetAndRotation(-7.5F, 6.166667F, 44.0F, -0.1115358F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1", CubeListBuilder.create()
            .texOffs(140, 36).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 13.0F, 13.0F, 15.0F),
            PartPose.offsetAndRotation(-6.5F, 9.0F, 58.0F, 0.1115358F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2", CubeListBuilder.create()
            .texOffs(64, 41).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 10.0F, 10.0F, 13.0F),
            PartPose.offsetAndRotation(-5.0F, 10.0F, 70.0F, 0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3", CubeListBuilder.create()
            .texOffs(64, 20).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 13.0F),
            PartPose.offsetAndRotation(-4.0F, 6.0F, 82.0F, 0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail4", CubeListBuilder.create()
            .texOffs(64, 1).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 13.0F),
            PartPose.offsetAndRotation(-3.0F, 4.0F, 95.0F, -0.0743572F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck2", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offsetAndRotation(-8.0F, -4.9F, -26.0F, -0.8464847F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck1", CubeListBuilder.create()
            .texOffs(0, 32).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offsetAndRotation(-8.0F, -15.0F, -29.0F, -1.181092F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 16.0F, 18.0F, 10.0F),
            PartPose.offsetAndRotation(-8.0F, -21.0F, -30.0F, -1.404164F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rog1", CubeListBuilder.create()
            .texOffs(110, 45).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 5.0F),
            PartPose.offsetAndRotation(3.0F, -21.0F, -32.0F, 0.6320364F, 0.2230717F, 0.0F));

        partdefinition.addOrReplaceChild("rog2", CubeListBuilder.create()
            .texOffs(110, 45).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 5.0F),
            PartPose.offsetAndRotation(-6.0F, -21.0F, -32.8F, 0.6320364F, -0.2230705F, 0.0F));

        partdefinition.addOrReplaceChild("rog3", CubeListBuilder.create()
            .texOffs(52, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(0.4666667F, -21.0F, -31.0F, 0.6320364F, 0.2230717F, 0.0F));

        partdefinition.addOrReplaceChild("rog4", CubeListBuilder.create()
            .texOffs(52, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(-2.466667F, -21.0F, -31.46667F, 0.6320364F, -0.2230705F, 0.0F));

        partdefinition.addOrReplaceChild("rog5", CubeListBuilder.create()
            .texOffs(52, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(-8.0F, -17.0F, -32.0F, 0.6320364F, -0.6692139F, 0.0F));

        partdefinition.addOrReplaceChild("rog6", CubeListBuilder.create()
            .texOffs(52, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(6.4F, -17.0F, -32.0F, 0.6320364F, 0.6692116F, 0.0F));

        partdefinition.addOrReplaceChild("snout", CubeListBuilder.create()
            .texOffs(102, 1).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 9.0F),
            PartPose.offsetAndRotation(-7.0F, -17.0F, -43.0F, -1.404164F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw", CubeListBuilder.create()
            .texOffs(106, 26).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 3.0F),
            PartPose.offsetAndRotation(-7.0F, -11.0F, -39.0F, -0.8836633F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 64);
    }

    @Override
    public void setupAnim(Basilisk entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pi4 = 0.7853975F;
        this.body1.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.body2.x = this.body1.x + (float) Math.cos(this.body1.yRot) * 12.0F;
        this.body2.z = this.body1.z + (float) Math.sin(this.body1.yRot) * 12.0F;
        this.body2.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.body3.x = this.body2.x + (float) Math.cos(this.body2.yRot) * 11.0F;
        this.body3.z = this.body2.z + (float) Math.sin(this.body2.yRot) * 11.0F;
        this.body3.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 2.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.body4.x = this.body3.x + (float) Math.cos(this.body3.yRot) * 12.0F;
        this.body4.z = this.body3.z + (float) Math.sin(this.body3.yRot) * 12.0F;
        this.body4.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 3.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.body5.x = this.body4.x + (float) Math.cos(this.body4.yRot) * 12.0F;
        this.body5.z = this.body4.z + (float) Math.sin(this.body4.yRot) * 12.0F;
        this.body5.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 4.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.body6.x = this.body5.x + (float) Math.cos(this.body5.yRot) * 12.0F;
        this.body6.z = this.body5.z + 0.5F + (float) Math.sin(this.body5.yRot) * 12.0F;
        this.body6.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 5.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.tail1.x = this.body6.x + (float) Math.cos(this.body6.yRot) * 12.0F;
        this.tail1.z = this.body6.z + 1.0F + (float) Math.sin(this.body6.yRot) * 12.0F;
        this.tail1.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 6.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.tail2.x = this.tail1.x + (float) Math.cos(this.tail1.yRot) * 10.0F;
        this.tail2.z = this.tail1.z + 1.5F + (float) Math.sin(this.tail1.yRot) * 10.0F;
        this.tail2.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 7.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.tail3.x = this.tail2.x + (float) Math.cos(this.tail2.yRot) * 10.0F;
        this.tail3.z = this.tail2.z + 1.0F + (float) Math.sin(this.tail2.yRot) * 10.0F;
        this.tail3.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 8.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.tail4.x = this.tail3.x + (float) Math.cos(this.tail3.yRot) * 10.0F;
        this.tail4.z = this.tail3.z + 1.0F + (float) Math.sin(this.tail3.yRot) * 10.0F;
        this.tail4.yRot = Mth.cos(ageInTicks * 1.3F * this.wingspeed - 9.0F * pi4) * (float) Math.PI * 0.1F * limbSwingAmount;
        this.jaw.xRot = entity.getAttacking() != 0 ? -1.0F + Mth.cos(ageInTicks * 0.45F) * (float) Math.PI * 0.18F : -1.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rog1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rog2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rog3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rog4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rog5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rog6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snout.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
