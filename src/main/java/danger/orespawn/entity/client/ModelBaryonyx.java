package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Baryonyx;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelBaryonyx extends EntityModel<Baryonyx> {
    private float wingspeed = 1.0f;
    private final ModelPart backSail1;
    private final ModelPart backSail2;
    private final ModelPart backSail3;
    private final ModelPart backSail4;
    private final ModelPart backSail5;
    private final ModelPart backSail6;
    private final ModelPart backSail7;
    private final ModelPart backSail8;
    private final ModelPart backSail9;
    private final ModelPart backSail10;
    private final ModelPart backSail11;
    private final ModelPart backSail12;
    private final ModelPart backSail13;
    private final ModelPart backSail14;
    private final ModelPart backSail15;
    private final ModelPart backSail16;
    private final ModelPart backSail17;
    private final ModelPart backSail18;
    private final ModelPart backSail19;
    private final ModelPart crestSegment1;
    private final ModelPart crestSegment2;
    private final ModelPart crestSegment3;
    private final ModelPart crestSegment4;
    private final ModelPart crestSegment5;
    private final ModelPart crestSegment6;
    private final ModelPart torso;
    private final ModelPart neckBase;
    private final ModelPart headTop;
    private final ModelPart upperSnout;
    private final ModelPart elongatedSnout;
    private final ModelPart rump;
    private final ModelPart tail;
    private final ModelPart leftFrontThigh;
    private final ModelPart leftFrontKnee;
    private final ModelPart leftFrontShin;
    private final ModelPart leftFrontToe1;
    private final ModelPart leftFrontToe2;
    private final ModelPart leftBackLeg;
    private final ModelPart snoutTip;
    private final ModelPart leftBackCalf;
    private final ModelPart leftFrontClaw;
    private final ModelPart leftBackFoot;
    private final ModelPart rightFrontThigh;
    private final ModelPart rightFrontKnee;
    private final ModelPart rightFrontShin;
    private final ModelPart rightFrontClaw;
    private final ModelPart rightFrontToe1;
    private final ModelPart rightFrontToe2;
    private final ModelPart rightBackLeg;
    private final ModelPart rightBackCalf;
    private final ModelPart rightBackFoot;
    private final ModelPart shape52; // snout ornament (zero-size cube)

    public ModelBaryonyx(ModelPart root) {
        this.backSail1 = root.getChild("shape27");
        this.backSail2 = root.getChild("shape28");
        this.backSail3 = root.getChild("shape29");
        this.backSail4 = root.getChild("shape30");
        this.backSail5 = root.getChild("shape31");
        this.backSail6 = root.getChild("shape32");
        this.backSail7 = root.getChild("shape33");
        this.backSail8 = root.getChild("shape34");
        this.backSail9 = root.getChild("shape35");
        this.backSail10 = root.getChild("shape36");
        this.backSail11 = root.getChild("shape37");
        this.backSail12 = root.getChild("shape38");
        this.backSail13 = root.getChild("shape39");
        this.backSail14 = root.getChild("shape40");
        this.backSail15 = root.getChild("shape41");
        this.backSail16 = root.getChild("shape42");
        this.backSail17 = root.getChild("shape43");
        this.backSail18 = root.getChild("shape44");
        this.backSail19 = root.getChild("shape45");
        this.crestSegment1 = root.getChild("shape46");
        this.crestSegment2 = root.getChild("shape47");
        this.crestSegment3 = root.getChild("shape48");
        this.crestSegment4 = root.getChild("shape49");
        this.crestSegment5 = root.getChild("shape50");
        this.crestSegment6 = root.getChild("shape51");
        this.torso = root.getChild("shape1");
        this.neckBase = root.getChild("shape2");
        this.headTop = root.getChild("shape3");
        this.upperSnout = root.getChild("shape4");
        this.elongatedSnout = root.getChild("shape5");
        this.rump = root.getChild("shape6");
        this.tail = root.getChild("shape7");
        this.leftFrontThigh = root.getChild("shape8");
        this.leftFrontKnee = root.getChild("shape9");
        this.leftFrontShin = root.getChild("shape10");
        this.leftFrontToe1 = root.getChild("shape11");
        this.leftFrontToe2 = root.getChild("shape12");
        this.leftBackLeg = root.getChild("shape13");
        this.snoutTip = root.getChild("shape14");
        this.leftBackCalf = root.getChild("shape15");
        this.leftFrontClaw = root.getChild("shape16");
        this.leftBackFoot = root.getChild("shape17");
        this.rightFrontThigh = root.getChild("shape18");
        this.rightFrontKnee = root.getChild("shape19");
        this.rightFrontShin = root.getChild("shape20");
        this.rightFrontClaw = root.getChild("shape21");
        this.rightFrontToe1 = root.getChild("shape22");
        this.rightFrontToe2 = root.getChild("shape23");
        this.rightBackLeg = root.getChild("shape24");
        this.rightBackCalf = root.getChild("shape25");
        this.rightBackFoot = root.getChild("shape26");
        this.shape52 = root.getChild("shape52");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("shape27", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -10.0F));

        partdefinition.addOrReplaceChild("shape28", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -7.0F));

        partdefinition.addOrReplaceChild("shape29", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -4.0F));

        partdefinition.addOrReplaceChild("shape30", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, -1.0F));

        partdefinition.addOrReplaceChild("shape31", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape32", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 5.0F));

        partdefinition.addOrReplaceChild("shape33", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 8.0F));

        partdefinition.addOrReplaceChild("shape34", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 11.0F));

        partdefinition.addOrReplaceChild("shape35", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 14.0F));

        partdefinition.addOrReplaceChild("shape36", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 17.0F));

        partdefinition.addOrReplaceChild("shape37", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 20.0F));

        partdefinition.addOrReplaceChild("shape38", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 23.0F));

        partdefinition.addOrReplaceChild("shape39", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 26.0F));

        partdefinition.addOrReplaceChild("shape40", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 29.0F));

        partdefinition.addOrReplaceChild("shape41", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 32.0F));

        partdefinition.addOrReplaceChild("shape42", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 35.0F));

        partdefinition.addOrReplaceChild("shape43", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 38.0F));

        partdefinition.addOrReplaceChild("shape44", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 41.0F));

        partdefinition.addOrReplaceChild("shape45", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -17.0F, 44.0F));

        partdefinition.addOrReplaceChild("shape46", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -12.0F, -11.0F));

        partdefinition.addOrReplaceChild("shape47", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -13.0F, -13.0F));

        partdefinition.addOrReplaceChild("shape48", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -15.0F, -15.0F));

        partdefinition.addOrReplaceChild("shape49", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, -16.0F, -16.0F));

        partdefinition.addOrReplaceChild("shape50", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -19.0F, -17.0F));

        partdefinition.addOrReplaceChild("shape51", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F),
            PartPose.offset(0.0F, -19.0F, -19.0F));

        partdefinition.addOrReplaceChild("shape1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 10.0F, 17.0F, 25.0F),
            PartPose.offset(-5.0F, -15.0F, -10.0F));

        partdefinition.addOrReplaceChild("shape2", CubeListBuilder.create()
            .texOffs(0, 93).mirror()
            .addBox(-3.0F, 0.0F, -11.0F, 6.0F, 10.0F, 11.0F),
            PartPose.offsetAndRotation(0.0F, -10.0F, -6.0F, -0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape3", CubeListBuilder.create()
            .texOffs(29, 110).mirror()
            .addBox(-2.0F, -9.0F, -8.0F, 4.0F, 9.0F, 8.0F),
            PartPose.offsetAndRotation(0.0F, -10.0F, -11.0F, 0.7504916F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape4", CubeListBuilder.create()
            .texOffs(54, 108).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 7.0F, 12.0F),
            PartPose.offset(-3.0F, -18.0F, -28.0F));

        partdefinition.addOrReplaceChild("shape5", CubeListBuilder.create()
            .texOffs(54, 86).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 15.0F),
            PartPose.offset(-1.5F, -17.5F, -43.0F));

        partdefinition.addOrReplaceChild("shape6", CubeListBuilder.create()
            .texOffs(0, 43).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 8.0F, 11.0F, 8.0F),
            PartPose.offset(-4.0F, -15.0F, 15.0F));

        partdefinition.addOrReplaceChild("shape7", CubeListBuilder.create()
            .texOffs(0, 63).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 23.0F),
            PartPose.offset(-3.0F, -15.0F, 23.0F));

        partdefinition.addOrReplaceChild("shape8", CubeListBuilder.create()
            .texOffs(47, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 3.0F),
            PartPose.offset(5.0F, 0.0F, -7.0F));

        partdefinition.addOrReplaceChild("shape9", CubeListBuilder.create()
            .texOffs(49, 10).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(5.1F, 3.0F, -6.0F, -0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape10", CubeListBuilder.create()
            .texOffs(13, 17).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 3.0F),
            PartPose.offset(5.0F, 7.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape11", CubeListBuilder.create()
            .texOffs(0, 17).mirror()
            .addBox(0.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(5.0F, 8.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape12", CubeListBuilder.create()
            .texOffs(0, 21).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(5.0F, 9.0F, -11.0F));

        partdefinition.addOrReplaceChild("shape13", CubeListBuilder.create()
            .texOffs(95, 36).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 3.0F, 21.0F, 13.0F),
            PartPose.offset(5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape14", CubeListBuilder.create()
            .texOffs(36, 94).mirror()
            .addBox(0.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F),
            PartPose.offset(-1.5F, -17.0F, -43.0F));

        partdefinition.addOrReplaceChild("shape15", CubeListBuilder.create()
            .texOffs(113, 71).mirror()
            .addBox(0.0F, 18.0F, 8.0F, 3.0F, 18.0F, 4.0F),
            PartPose.offsetAndRotation(5.0F, -15.0F, 2.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape16", CubeListBuilder.create()
            .texOffs(13, 11).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(5.0F, 10.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape17", CubeListBuilder.create()
            .texOffs(0, 74).mirror()
            .addBox(0.0F, 35.0F, -1.0F, 3.0F, 3.0F, 6.0F),
            PartPose.offset(5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape18", CubeListBuilder.create()
            .texOffs(58, 0).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 5.0F, 3.0F),
            PartPose.offset(-5.0F, 0.0F, -7.0F));

        partdefinition.addOrReplaceChild("shape19", CubeListBuilder.create()
            .texOffs(59, 10).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(-5.1F, 3.0F, -6.0F, -0.3839724F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape20", CubeListBuilder.create()
            .texOffs(71, 5).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 2.0F, 4.0F, 3.0F),
            PartPose.offset(-5.0F, 7.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape21", CubeListBuilder.create()
            .texOffs(71, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(-5.0F, 10.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape22", CubeListBuilder.create()
            .texOffs(0, 10).mirror()
            .addBox(-1.0F, 0.0F, -2.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(-5.0F, 8.0F, -8.0F));

        partdefinition.addOrReplaceChild("shape23", CubeListBuilder.create()
            .texOffs(0, 14).mirror()
            .addBox(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(-5.0F, 9.0F, -11.0F));

        partdefinition.addOrReplaceChild("shape24", CubeListBuilder.create()
            .texOffs(95, 0).mirror()
            .addBox(-3.0F, 0.0F, 0.0F, 3.0F, 22.0F, 13.0F),
            PartPose.offset(-5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape25", CubeListBuilder.create()
            .texOffs(96, 71).mirror()
            .addBox(-3.0F, 18.0F, 8.0F, 3.0F, 18.0F, 4.0F),
            PartPose.offsetAndRotation(-5.0F, -15.0F, 2.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("shape26", CubeListBuilder.create()
            .texOffs(0, 64).mirror()
            .addBox(-3.0F, 35.0F, -1.0F, 3.0F, 3.0F, 6.0F),
            PartPose.offset(-5.0F, -15.0F, 2.0F));

        partdefinition.addOrReplaceChild("shape52", CubeListBuilder.create()
            .texOffs(9, 0).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F),
            PartPose.offset(0.0F, -19.0F, -30.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(Baryonyx entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0F;
        newangle = limbSwingAmount > 0.1F ? Mth.cos(ageInTicks * 1.3F * this.wingspeed) * (float) Math.PI * 0.15F * limbSwingAmount : 0.0F;
        this.rightBackLeg.xRot = newangle;
        this.rightBackCalf.xRot = -0.17F + newangle;
        this.rightBackFoot.xRot = newangle;
        this.leftBackLeg.xRot = -newangle;
        this.leftBackCalf.xRot = -0.17F - newangle;
        this.leftBackFoot.xRot = -newangle;
        newangle = Mth.cos(ageInTicks * 0.7F * this.wingspeed) * (float) Math.PI * 0.25F;
        this.rightFrontClaw.zRot = newangle;
        this.leftFrontClaw.zRot = -newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.backSail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.backSail19.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crestSegment1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crestSegment2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crestSegment3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crestSegment4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crestSegment5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crestSegment6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neckBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.headTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperSnout.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.elongatedSnout.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rump.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftFrontThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftFrontKnee.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftFrontShin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftFrontToe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftFrontToe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftBackLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snoutTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftBackCalf.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftFrontClaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftBackFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightFrontThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightFrontKnee.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightFrontShin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightFrontClaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightFrontToe1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightFrontToe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightBackLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightBackCalf.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightBackFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.shape52.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
