package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Hammerhead;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelHammerhead extends EntityModel<Hammerhead> {
    private final ModelPart chest;
    private final ModelPart abdomen;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart snout;
    private final ModelPart neck_armour;
    private final ModelPart horn_base;
    private final ModelPart horn_1;
    private final ModelPart horn_2;
    private final ModelPart horn_R;
    private final ModelPart horn_L;
    private final ModelPart back_armour1;
    private final ModelPart back_armour_2;
    private final ModelPart back_armour_3;
    private final ModelPart back_armour_3R;
    private final ModelPart back_armour_4;
    private final ModelPart back_armour_4R;
    private final ModelPart tail;
    private final ModelPart leg_1R;
    private final ModelPart leg_1;
    private final ModelPart leg_2;
    private final ModelPart leg_2R;
    private final ModelPart leg_3R;
    private final ModelPart leg_3;
    private final ModelPart leg_1Rb;
    private final ModelPart leg_1b;
    private final ModelPart leg_2b;
    private final ModelPart leg_2Rb;
    private final ModelPart leg_3Rb;
    private final ModelPart leg_3b;
    private final ModelPart fan1;
    private final ModelPart Lfan2;
    private final ModelPart Rfan2;
    private final ModelPart Lfan3;
    private final ModelPart Rfan3;
    private final ModelPart Lear;
    private final ModelPart Rear;

    public ModelHammerhead(ModelPart root) {
        this.chest = root.getChild("chest");
        this.abdomen = root.getChild("abdomen");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.snout = root.getChild("snout");
        this.neck_armour = root.getChild("neck_armour");
        this.horn_base = root.getChild("horn_base");
        this.horn_1 = root.getChild("horn_1");
        this.horn_2 = root.getChild("horn_2");
        this.horn_R = root.getChild("horn_R");
        this.horn_L = root.getChild("horn_L");
        this.back_armour1 = root.getChild("back_armour1");
        this.back_armour_2 = root.getChild("back_armour_2");
        this.back_armour_3 = root.getChild("back_armour_3");
        this.back_armour_3R = root.getChild("back_armour_3R");
        this.back_armour_4 = root.getChild("back_armour_4");
        this.back_armour_4R = root.getChild("back_armour_4R");
        this.tail = root.getChild("tail");
        this.leg_1R = root.getChild("leg_1R");
        this.leg_1 = root.getChild("leg_1");
        this.leg_2 = root.getChild("leg_2");
        this.leg_2R = root.getChild("leg_2R");
        this.leg_3R = root.getChild("leg_3R");
        this.leg_3 = root.getChild("leg_3");
        this.leg_1Rb = root.getChild("leg_1Rb");
        this.leg_1b = root.getChild("leg_1b");
        this.leg_2b = root.getChild("leg_2b");
        this.leg_2Rb = root.getChild("leg_2Rb");
        this.leg_3Rb = root.getChild("leg_3Rb");
        this.leg_3b = root.getChild("leg_3b");
        this.fan1 = root.getChild("fan1");
        this.Lfan2 = root.getChild("Lfan2");
        this.Rfan2 = root.getChild("Rfan2");
        this.Lfan3 = root.getChild("Lfan3");
        this.Rfan3 = root.getChild("Rfan3");
        this.Lear = root.getChild("Lear");
        this.Rear = root.getChild("Rear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-9.0F, -1.0F, 0.0F, 19, 16, 17),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.0349066F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(0, 34).mirror().addBox(-7.5F, 0.0F, 0.0F, 16, 14, 16),
                PartPose.offsetAndRotation(0.0F, -2.0F, 4.0F, -0.0349066F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(146, 59).mirror().addBox(-6.5F, -0.5F, -12.0F, 14, 13, 13),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(101, 59).mirror().addBox(-6.0F, -0.5F, -21.0F, 13, 11, 9),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(166, 86).mirror().addBox(-4.0F, -6.0F, -27.0F, 9, 8, 8),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.6108652F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck_armour",
                CubeListBuilder.create().texOffs(73, 0).mirror().addBox(-7.0F, -1.5F, -18.0F, 15, 4, 18),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("horn_base",
                CubeListBuilder.create().texOffs(49, 35).mirror().addBox(-7.0F, -1.5F, -27.0F, 15, 5, 9),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("horn_1",
                CubeListBuilder.create().texOffs(122, 23).mirror().addBox(-12.0F, -4.5F, -40.0F, 25, 6, 14),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("horn_2",
                CubeListBuilder.create().texOffs(106, 44).mirror().addBox(-18.0F, -3.5F, -37.0F, 37, 4, 10),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("horn_R",
                CubeListBuilder.create().texOffs(158, 0).mirror().addBox(-26.0F, -5.5F, -38.5F, 8, 7, 13),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1919862F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("horn_L",
                CubeListBuilder.create().texOffs(158, 0).mirror().addBox(19.0F, -5.5F, -38.5F, 8, 7, 13),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.1919862F, 0.0F, -0.0174533F));

        partdefinition.addOrReplaceChild("back_armour1",
                CubeListBuilder.create().texOffs(0, 98).mirror().addBox(-5.0F, -2.5F, -6.0F, 9, 3, 7),
                PartPose.offsetAndRotation(1.0F, -4.0F, -15.0F, -0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("back_armour_2",
                CubeListBuilder.create().texOffs(0, 65).mirror().addBox(-8.0F, -4.5F, -13.0F, 17, 4, 28),
                PartPose.offsetAndRotation(0.0F, -1.0F, -3.0F, -0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("back_armour_3",
                CubeListBuilder.create().texOffs(15, 104).mirror().addBox(0.5F, -3.5F, -13.0F, 4, 4, 20),
                PartPose.offsetAndRotation(8.0F, 1.0F, -2.0F, 0.0174533F, 0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("back_armour_3R",
                CubeListBuilder.create().texOffs(15, 104).mirror().addBox(-3.5F, -3.5F, -13.0F, 4, 4, 20),
                PartPose.offsetAndRotation(-8.0F, 1.0F, -2.0F, 0.0174533F, -0.1570796F, 0.0F));

        partdefinition.addOrReplaceChild("back_armour_4",
                CubeListBuilder.create().texOffs(0, 65).mirror().addBox(1.5F, -1.5F, -3.0F, 3, 4, 10),
                PartPose.offsetAndRotation(6.0F, 5.0F, -10.0F, -0.1396263F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("back_armour_4R",
                CubeListBuilder.create().texOffs(0, 65).mirror().addBox(-1.5F, -1.5F, -3.0F, 3, 4, 10),
                PartPose.offsetAndRotation(-8.0F, 5.0F, -11.0F, -0.1396263F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(66, 52).mirror().addBox(-2.0F, 0.0F, -3.0F, 5, 5, 3),
                PartPose.offsetAndRotation(0.0F, 0.0F, 20.0F, 0.5061455F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_1R",
                CubeListBuilder.create().texOffs(71, 102).mirror().addBox(-2.5F, -2.5F, -3.0F, 5, 10, 6),
                PartPose.offsetAndRotation(-9.0F, 11.0F, -10.0F, -0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_1",
                CubeListBuilder.create().texOffs(64, 76).mirror().addBox(-1.5F, -2.5F, -3.0F, 5, 10, 6),
                PartPose.offsetAndRotation(9.0F, 11.0F, -10.0F, -0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_2",
                CubeListBuilder.create().texOffs(98, 28).mirror().addBox(-1.5F, -2.5F, -3.0F, 5, 9, 6),
                PartPose.offsetAndRotation(9.0F, 12.0F, -2.0F, -0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_2R",
                CubeListBuilder.create().texOffs(98, 80).mirror().addBox(-1.5F, -2.5F, -3.0F, 5, 9, 6),
                PartPose.offsetAndRotation(-10.0F, 12.0F, -2.0F, -0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_3R",
                CubeListBuilder.create().texOffs(44, 129).mirror().addBox(-3.5F, -2.5F, -3.0F, 5, 11, 8),
                PartPose.offsetAndRotation(-7.0F, 9.0F, 14.0F, -0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_3",
                CubeListBuilder.create().texOffs(44, 99).mirror().addBox(-3.5F, -2.5F, -3.0F, 5, 11, 8),
                PartPose.offsetAndRotation(10.0F, 9.0F, 14.0F, -0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_1Rb",
                CubeListBuilder.create().texOffs(15, 129).mirror().addBox(-2.0F, 5.5F, -3.0F, 4, 8, 5),
                PartPose.offset(-9.0F, 11.0F, -10.0F));

        partdefinition.addOrReplaceChild("leg_1b",
                CubeListBuilder.create().texOffs(15, 110).mirror().addBox(-1.0F, 5.5F, -3.0F, 4, 8, 5),
                PartPose.offset(9.0F, 11.0F, -10.0F));

        partdefinition.addOrReplaceChild("leg_2b",
                CubeListBuilder.create().texOffs(57, 1).mirror().addBox(-1.0F, 5.5F, -3.0F, 4, 7, 5),
                PartPose.offsetAndRotation(9.0F, 12.0F, -2.0F, 0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_2Rb",
                CubeListBuilder.create().texOffs(94, 106).mirror().addBox(-2.0F, 5.5F, -3.0F, 4, 7, 5),
                PartPose.offsetAndRotation(-9.0F, 12.0F, -2.0F, 0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_3Rb",
                CubeListBuilder.create().texOffs(122, 81).mirror().addBox(-2.0F, 6.5F, -5.0F, 4, 9, 5),
                PartPose.offsetAndRotation(-8.0F, 9.0F, 14.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg_3b",
                CubeListBuilder.create().texOffs(122, 0).mirror().addBox(-3.0F, 6.5F, -5.0F, 4, 9, 5),
                PartPose.offsetAndRotation(10.0F, 9.0F, 14.0F, 0.122173F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fan1",
                CubeListBuilder.create().texOffs(0, 109).mirror().addBox(-1.0F, -7.0F, -34.0F, 4, 15, 1),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Lfan2",
                CubeListBuilder.create().texOffs(0, 109).mirror().addBox(-1.0F, -3.0F, -31.5F, 4, 12, 1),
                PartPose.offsetAndRotation(0.0F, -1.0F, -14.0F, -0.2094395F, -0.122173F, 0.0F));

        partdefinition.addOrReplaceChild("Rfan2",
                CubeListBuilder.create().texOffs(0, 109).mirror().addBox(-1.0F, -3.0F, -33.5F, 4, 12, 1),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, -0.2094395F, 0.122173F, 0.0F));

        partdefinition.addOrReplaceChild("Lfan3",
                CubeListBuilder.create().texOffs(0, 109).mirror().addBox(-1.0F, 4.0F, -32.0F, 4, 9, 1),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, -0.3316126F, -0.2268928F, 0.0F));

        partdefinition.addOrReplaceChild("Rfan3",
                CubeListBuilder.create().texOffs(0, 109).mirror().addBox(-1.0F, 4.0F, -32.0F, 4, 9, 1),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, -0.3316126F, 0.2443461F, 0.0F));

        partdefinition.addOrReplaceChild("Lear",
                CubeListBuilder.create().texOffs(0, 80).mirror().addBox(8.5F, 2.5F, -10.0F, 1, 1, 10),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.3665191F, 0.2268928F, 0.0F));

        partdefinition.addOrReplaceChild("Rear",
                CubeListBuilder.create().texOffs(0, 80).mirror().addBox(-8.5F, 2.5F, -11.0F, 1, 1, 10),
                PartPose.offsetAndRotation(0.0F, -1.0F, -12.0F, 0.3665191F, -0.2268928F, 0.0F));

        return LayerDefinition.create(meshdefinition, 222, 256);
    }

    @Override
    public void setupAnim(Hammerhead entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if ((double)limbSwingAmount > 0.1) {
        newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.1f * limbSwingAmount;
        newangle2 = Mth.cos((float)((float)((double)(ageInTicks * 1.3f * limbSwingAmount) + 0.7853981633974483))) * (float)Math.PI * 0.1f * limbSwingAmount;
        } else {
        newangle = 0.0f;
        }
        this.leg_1.xRot = -0.087f + newangle;
        this.leg_1b.xRot = newangle;
        this.leg_1R.xRot = -0.087f - newangle;
        this.leg_1Rb.xRot = -newangle;
        this.leg_2.xRot = -0.052f + newangle2;
        this.leg_2b.xRot = newangle2;
        this.leg_2R.xRot = -0.052f - newangle2;
        this.leg_2Rb.xRot = -newangle2;
        this.leg_3.xRot = -0.349f - newangle;
        this.leg_3b.xRot = -newangle;
        this.leg_3R.xRot = -0.349f + newangle;
        this.leg_3Rb.xRot = newangle;
        this.neck_armour.yRot = this.neck.yRot = (float)Math.toRadians(netHeadYaw) * 0.25f;
        this.horn_base.yRot = this.neck.yRot;
        this.horn_1.yRot = this.neck.yRot;
        this.horn_2.yRot = this.neck.yRot;
        this.horn_L.yRot = this.neck.yRot;
        this.horn_R.yRot = this.neck.yRot;
        this.head.yRot = this.neck.yRot;
        this.snout.yRot = this.neck.yRot;
        this.fan1.yRot = this.neck.yRot;
        this.Lfan2.yRot = this.neck.yRot - 0.122f;
        this.Lfan3.yRot = this.neck.yRot - 0.226f;
        this.Rfan2.yRot = this.neck.yRot + 0.122f;
        this.Rfan3.yRot = this.neck.yRot + 0.226f;
        this.Lear.yRot = this.neck.yRot + 0.227f;
        this.Rear.yRot = this.neck.yRot - 0.227f;
        newangle = Mth.cos((float)(ageInTicks * 0.3f * limbSwingAmount)) * (float)Math.PI * 0.03f;
        this.back_armour_4.yRot = 0.349f + newangle;
        this.back_armour_4R.yRot = -0.349f - newangle;
        newangle = entity.getAttacking() != 0 ? Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.13f : 0.0f;
        this.neck.xRot = newangle + 0.157f;
        this.neck_armour.xRot = newangle + 0.157f;
        this.horn_base.xRot = newangle + 0.087f;
        this.horn_1.xRot = newangle + 0.192f;
        this.horn_2.xRot = newangle + 0.192f;
        this.horn_L.xRot = newangle + 0.192f;
        this.horn_R.xRot = newangle + 0.192f;
        this.head.xRot = newangle + 0.209f;
        this.snout.xRot = newangle + 0.611f;
        this.fan1.xRot = newangle - 0.139f;
        this.Lfan2.xRot = newangle - 0.209f;
        this.Lfan3.xRot = newangle - 0.331f;
        this.Rfan2.xRot = newangle - 0.209f;
        this.Rfan3.xRot = newangle - 0.331f;
        this.Lear.xRot = newangle + 0.366f;
        this.Rear.xRot = newangle + 0.366f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.snout.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck_armour.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn_base.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn_R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn_L.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_armour1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_armour_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_armour_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_armour_3R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_armour_4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.back_armour_4R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_1R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_2R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_3R.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_1Rb.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_1b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_2b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_2Rb.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_3Rb.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg_3b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fan1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lfan2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rfan2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lfan3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rfan3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Lear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Rear.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
