package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import danger.orespawn.entity.PitchBlack;

public class ModelPitchBlack extends EntityModel<PitchBlack> {
    private final ModelPart body;
    private final ModelPart body2;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart head7;
    private final ModelPart jaw1;
    private final ModelPart jaw5;
    private final ModelPart leftleg1;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart rightleg1;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart wing1;
    private final ModelPart wing2;
    private final ModelPart wing3;
    private final ModelPart rwing1;
    private final ModelPart rwing2;
    private final ModelPart rwing3;
    private final ModelPart mem1;
    private final ModelPart mem2;
    private final ModelPart mem3;
    private final ModelPart rmem1;
    private final ModelPart rmem2;
    private final ModelPart rmem3;
    private final ModelPart lshoulder;
    private final ModelPart rshoulder;
    private final ModelPart lclaw2;
    private final ModelPart rclaw2;

    public ModelPitchBlack(ModelPart root) {
        this.body = root.getChild("body");
        this.body2 = root.getChild("body2");
        this.neck1 = root.getChild("neck1");
        this.neck2 = root.getChild("neck2");
        this.neck3 = root.getChild("neck3");
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.head3 = root.getChild("head3");
        this.head7 = root.getChild("head7");
        this.jaw1 = root.getChild("jaw1");
        this.jaw5 = root.getChild("jaw5");
        this.leftleg1 = root.getChild("leftleg1");
        this.leftleg2 = root.getChild("leftleg2");
        this.leftleg3 = root.getChild("leftleg3");
        this.rightleg1 = root.getChild("rightleg1");
        this.rightleg2 = root.getChild("rightleg2");
        this.rightleg3 = root.getChild("rightleg3");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.tail5 = root.getChild("tail5");
        this.wing1 = root.getChild("wing1");
        this.wing2 = root.getChild("wing2");
        this.wing3 = root.getChild("wing3");
        this.rwing1 = root.getChild("rwing1");
        this.rwing2 = root.getChild("rwing2");
        this.rwing3 = root.getChild("rwing3");
        this.mem1 = root.getChild("mem1");
        this.mem2 = root.getChild("mem2");
        this.mem3 = root.getChild("mem3");
        this.rmem1 = root.getChild("rmem1");
        this.rmem2 = root.getChild("rmem2");
        this.rmem3 = root.getChild("rmem3");
        this.lshoulder = root.getChild("lshoulder");
        this.rshoulder = root.getChild("rshoulder");
        this.lclaw2 = root.getChild("lclaw2");
        this.rclaw2 = root.getChild("rclaw2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(400, 26).mirror()
                        .addBox(-6.0F, -12.0F, -9.0F, 12, 12, 9),
                PartPose.offsetAndRotation(0.0F, 0.0F, 9.0F, 0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(400, 50).mirror()
                        .addBox(0.0F, -3.0F, -3.0F, 12, 14, 16),
                PartPose.offsetAndRotation(-6.0F, -9.0F, 10.0F, -0.1047198F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck1",
                CubeListBuilder.create().texOffs(400, 7).mirror()
                        .addBox(-4.0F, -4.0F, 0.0F, 8, 8, 8),
                PartPose.offsetAndRotation(0.0F, -4.0F, -5.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(375, 10).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 4),
                PartPose.offsetAndRotation(0.0F, -4.0F, -5.0F, -0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck3",
                CubeListBuilder.create().texOffs(375, 23).mirror()
                        .addBox(-2.0F, -2.0F, -6.0F, 4, 4, 8),
                PartPose.offsetAndRotation(0.0F, -4.0F, -9.0F, -0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(123, 3).mirror()
                        .addBox(-18.0F, -1.0F, -12.0F, 36, 1, 12),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, -0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(140, 18).mirror()
                        .addBox(-8.0F, -2.0F, -11.0F, 16, 1, 11),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, -0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head3",
                CubeListBuilder.create().texOffs(143, 32).mirror()
                        .addBox(-2.0F, -4.0F, -14.0F, 4, 4, 16),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, -0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head7",
                CubeListBuilder.create().texOffs(185, 34).mirror()
                        .addBox(-3.0F, -5.0F, -3.0F, 6, 5, 7),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, -0.2443461F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1",
                CubeListBuilder.create().texOffs(143, 114).mirror()
                        .addBox(-2.0F, 1.0F, -14.0F, 4, 4, 15),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw5",
                CubeListBuilder.create().texOffs(151, 135).mirror()
                        .addBox(-3.0F, 1.0F, -4.0F, 6, 5, 7),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg1",
                CubeListBuilder.create().texOffs(300, 10).mirror()
                        .addBox(-1.0F, -5.0F, -20.0F, 5, 10, 10),
                PartPose.offsetAndRotation(7.0F, 5.0F, 23.0F, -0.5759587F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg2",
                CubeListBuilder.create().texOffs(300, 31).mirror()
                        .addBox(-1.0F, -10.0F, -4.0F, 4, 12, 5),
                PartPose.offsetAndRotation(7.0F, 5.0F, 23.0F, 0.9773844F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg3",
                CubeListBuilder.create().texOffs(300, 51).mirror()
                        .addBox(-1.0F, -19.0F, 1.0F, 3, 18, 4),
                PartPose.offsetAndRotation(7.0F, 21.0F, 11.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg1",
                CubeListBuilder.create().texOffs(250, 10).mirror()
                        .addBox(-1.0F, -5.0F, -20.0F, 5, 10, 10),
                PartPose.offsetAndRotation(-10.0F, 5.0F, 23.0F, -0.5934119F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg2",
                CubeListBuilder.create().texOffs(250, 32).mirror()
                        .addBox(0.0F, -10.0F, -4.0F, 4, 12, 5),
                PartPose.offsetAndRotation(-10.0F, 5.0F, 23.0F, 0.9773844F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg3",
                CubeListBuilder.create().texOffs(250, 52).mirror()
                        .addBox(-1.0F, -19.0F, 1.0F, 3, 18, 4),
                PartPose.offsetAndRotation(-8.0F, 21.0F, 11.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(400, 82).mirror()
                        .addBox(-5.0F, -6.0F, 0.0F, 8, 10, 12),
                PartPose.offsetAndRotation(1.0F, -3.0F, 22.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(400, 106).mirror()
                        .addBox(-3.0F, -4.0F, 0.0F, 6, 8, 10),
                PartPose.offsetAndRotation(0.0F, -2.0F, 33.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(400, 126).mirror()
                        .addBox(-2.0F, -2.0F, 0.0F, 4, 5, 10),
                PartPose.offsetAndRotation(0.0F, -1.0F, 42.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(400, 143).mirror()
                        .addBox(-2.0F, -2.0F, 0.0F, 4, 4, 10),
                PartPose.offsetAndRotation(0.0F, 0.0F, 51.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(400, 159).mirror()
                        .addBox(-1.5F, -2.0F, 0.0F, 3, 3, 10),
                PartPose.offsetAndRotation(0.0F, 1.0F, 59.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("wing1",
                CubeListBuilder.create().texOffs(10, 30).mirror()
                        .addBox(-1.0F, -1.0F, -1.0F, 23, 3, 3),
                PartPose.offsetAndRotation(6.0F, -12.0F, 3.0F, 0.0F, 0.0872665F, -0.1396263F));

        partdefinition.addOrReplaceChild("wing2",
                CubeListBuilder.create().texOffs(10, 40).mirror()
                        .addBox(-1.0F, -1.0F, -1.0F, 44, 2, 2),
                PartPose.offset(27.0F, -15.0F, 1.0F));

        partdefinition.addOrReplaceChild("wing3",
                CubeListBuilder.create().texOffs(10, 50).mirror()
                        .addBox(-1.0F, -1.0F, -1.0F, 23, 2, 2),
                PartPose.offsetAndRotation(70.0F, -15.0F, 1.0F, 0.0F, -0.0872665F, 0.1745329F));

        partdefinition.addOrReplaceChild("rwing1",
                CubeListBuilder.create().texOffs(10, 140).mirror()
                        .addBox(-22.0F, -1.0F, -1.0F, 23, 3, 3),
                PartPose.offsetAndRotation(-6.0F, -12.0F, 3.0F, 0.0F, -0.0872665F, 0.1396263F));

        partdefinition.addOrReplaceChild("rwing2",
                CubeListBuilder.create().texOffs(10, 150).mirror()
                        .addBox(-43.0F, -1.0F, -1.0F, 44, 2, 2),
                PartPose.offset(-27.0F, -15.0F, 1.0F));

        partdefinition.addOrReplaceChild("rwing3",
                CubeListBuilder.create().texOffs(10, 160).mirror()
                        .addBox(-22.0F, -1.0F, -1.0F, 23, 2, 2),
                PartPose.offsetAndRotation(-70.0F, -15.0F, 1.0F, 0.0F, 0.0872665F, -0.1745329F));

        partdefinition.addOrReplaceChild("mem1",
                CubeListBuilder.create().texOffs(10, 60).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 24, 1, 21),
                PartPose.offsetAndRotation(6.0F, -12.0F, 3.0F, 0.0F, 0.0872665F, -0.1396263F));

        partdefinition.addOrReplaceChild("mem2",
                CubeListBuilder.create().texOffs(10, 85).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 43, 1, 21),
                PartPose.offset(27.0F, -15.0F, 1.0F));

        partdefinition.addOrReplaceChild("mem3",
                CubeListBuilder.create().texOffs(10, 110).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 23, 1, 21),
                PartPose.offsetAndRotation(70.0F, -15.0F, 1.0F, 0.0F, -0.0872665F, 0.1745329F));

        partdefinition.addOrReplaceChild("rmem1",
                CubeListBuilder.create().texOffs(10, 170).mirror()
                        .addBox(-22.0F, 0.0F, 0.0F, 24, 1, 21),
                PartPose.offsetAndRotation(-6.0F, -12.0F, 3.0F, 0.0F, -0.0872665F, 0.1396263F));

        partdefinition.addOrReplaceChild("rmem2",
                CubeListBuilder.create().texOffs(10, 195).mirror()
                        .addBox(-43.0F, 0.0F, 0.0F, 43, 1, 21),
                PartPose.offset(-27.0F, -15.0F, 1.0F));

        partdefinition.addOrReplaceChild("rmem3",
                CubeListBuilder.create().texOffs(10, 220).mirror()
                        .addBox(-23.0F, 0.0F, 0.0F, 23, 1, 21),
                PartPose.offsetAndRotation(-70.0F, -15.0F, 1.0F, 0.0F, 0.0872665F, -0.1745329F));

        partdefinition.addOrReplaceChild("lshoulder",
                CubeListBuilder.create().texOffs(370, 40).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 2, 6),
                PartPose.offsetAndRotation(3.0F, -13.0F, 1.0F, 0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rshoulder",
                CubeListBuilder.create().texOffs(370, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 3, 2, 6),
                PartPose.offsetAndRotation(-6.0F, -13.0F, 1.0F, 0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lclaw2",
                CubeListBuilder.create().texOffs(300, 76).mirror()
                        .addBox(-1.0F, -1.0F, -6.0F, 3, 4, 13),
                PartPose.offset(7.0F, 21.0F, 11.0F));

        partdefinition.addOrReplaceChild("rclaw2",
                CubeListBuilder.create().texOffs(250, 76).mirror()
                        .addBox(-1.0F, -1.0F, -6.0F, 3, 4, 13),
                PartPose.offset(-8.0F, 21.0F, 11.0F));

        return LayerDefinition.create(meshdefinition, 512, 256);
    }

    @Override
    public void setupAnim(PitchBlack entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = Mth.cos(ageInTicks * 0.45F) * (float) Math.PI * 0.24F;
        this.wing1.zRot = wingAngle;
        this.mem1.zRot = wingAngle;
        this.rwing1.zRot = -wingAngle;
        this.rmem1.zRot = -wingAngle;

        float tailSwing = Mth.cos(ageInTicks * 0.26F) * (float) Math.PI * 0.08F / 2.0F;
        this.tail1.yRot = tailSwing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        neck3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        head7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        jaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        jaw5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        leftleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rightleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rwing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rwing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rwing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        mem1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        mem2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        mem3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rmem1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rmem2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rmem3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lshoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rshoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rclaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
