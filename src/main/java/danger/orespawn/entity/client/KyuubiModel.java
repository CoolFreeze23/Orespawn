package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityKyuubi;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class KyuubiModel extends EntityModel<EntityKyuubi> {
    private final ModelPart rtHorn5;
    private final ModelPart lfHorn5;
    private final ModelPart tail9;
    private final ModelPart tail8;
    private final ModelPart tail7;
    private final ModelPart tail6;
    private final ModelPart tail5;
    private final ModelPart tail2;
    private final ModelPart tail1;
    private final ModelPart tail0;
    private final ModelPart lfLegLower;
    private final ModelPart rtLegLower;
    private final ModelPart head;
    private final ModelPart chest;
    private final ModelPart lfArmUpper;
    private final ModelPart rtArmLower;
    private final ModelPart lfLegUpper;
    private final ModelPart rtLegUpper;
    private final ModelPart body;
    private final ModelPart rtArmUpper;
    private final ModelPart lfArmLower;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart lfHorn2;
    private final ModelPart rtHorn1;
    private final ModelPart rtHorn2;
    private final ModelPart lfHorn1;
    private final ModelPart lfHorn3;
    private final ModelPart rtHorn3;
    private final ModelPart lfHorn4;
    private final ModelPart rtHorn4;
    private final ModelPart headFire;
    private final ModelPart lfArmUpperFire;
    private final ModelPart chestFire;
    private final ModelPart bodyFire;
    private final ModelPart lfArmLowerFire;
    private final ModelPart rtArmUpperFire;
    private final ModelPart rtArmLowerFire;
    private final ModelPart lfLegUppperFire;
    private final ModelPart lfLegLowerFire;
    private final ModelPart rtLegUpperFire;
    private final ModelPart rtLegLowerFire;

    public KyuubiModel(ModelPart root) {
        this.rtHorn5 = root.getChild("rtHorn5");
        this.lfHorn5 = root.getChild("lfHorn5");
        this.tail9 = root.getChild("tail9");
        this.tail8 = root.getChild("tail8");
        this.tail7 = root.getChild("tail7");
        this.tail6 = root.getChild("tail6");
        this.tail5 = root.getChild("tail5");
        this.tail2 = root.getChild("tail2");
        this.tail1 = root.getChild("tail1");
        this.tail0 = root.getChild("tail0");
        this.lfLegLower = root.getChild("lfLegLower");
        this.rtLegLower = root.getChild("rtLegLower");
        this.head = root.getChild("head");
        this.chest = root.getChild("chest");
        this.lfArmUpper = root.getChild("lfArmUpper");
        this.rtArmLower = root.getChild("rtArmLower");
        this.lfLegUpper = root.getChild("lfLegUpper");
        this.rtLegUpper = root.getChild("rtLegUpper");
        this.body = root.getChild("body");
        this.rtArmUpper = root.getChild("rtArmUpper");
        this.lfArmLower = root.getChild("lfArmLower");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.lfHorn2 = root.getChild("lfHorn2");
        this.rtHorn1 = root.getChild("rtHorn1");
        this.rtHorn2 = root.getChild("rtHorn2");
        this.lfHorn1 = root.getChild("lfHorn1");
        this.lfHorn3 = root.getChild("lfHorn3");
        this.rtHorn3 = root.getChild("rtHorn3");
        this.lfHorn4 = root.getChild("lfHorn4");
        this.rtHorn4 = root.getChild("rtHorn4");
        this.headFire = root.getChild("headFire");
        this.lfArmUpperFire = root.getChild("lfArmUpperFire");
        this.chestFire = root.getChild("chestFire");
        this.bodyFire = root.getChild("bodyFire");
        this.lfArmLowerFire = root.getChild("lfArmLowerFire");
        this.rtArmUpperFire = root.getChild("rtArmUpperFire");
        this.rtArmLowerFire = root.getChild("rtArmLowerFire");
        this.lfLegUppperFire = root.getChild("lfLegUppperFire");
        this.lfLegLowerFire = root.getChild("lfLegLowerFire");
        this.rtLegUpperFire = root.getChild("rtLegUpperFire");
        this.rtLegLowerFire = root.getChild("rtLegLowerFire");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("rtHorn5",
                CubeListBuilder.create().texOffs(56, 8)
                        .addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, -11.5F, -7.0F, -0.4461433F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfHorn5",
                CubeListBuilder.create().texOffs(56, 24)
                        .addBox(-0.5F, -0.5F, -2.0F, 1, 1, 2),
                PartPose.offsetAndRotation(-3.0F, -11.5F, -7.0F, -0.4461433F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail9",
                CubeListBuilder.create().texOffs(145, 47)
                        .addBox(-0.5F, -0.5F, -1.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 9.0F, -26.0F, 2.007645F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail8",
                CubeListBuilder.create().texOffs(135, 45)
                        .addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2),
                PartPose.offsetAndRotation(0.0F, 7.0F, -25.75F, 1.524323F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail7",
                CubeListBuilder.create().texOffs(122, 44)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, 5.0F, -24.0F, 0.8922867F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail6",
                CubeListBuilder.create().texOffs(105, 43)
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 4),
                PartPose.offsetAndRotation(0.0F, 3.0F, -21.0F, 0.6320364F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(84, 42)
                        .addBox(-2.5F, -2.5F, -5.0F, 5, 5, 5),
                PartPose.offsetAndRotation(0.0F, 2.0F, -17.0F, 0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(20, 43)
                        .addBox(-2.0F, -2.0F, -5.0F, 4, 4, 5),
                PartPose.offsetAndRotation(0.0F, 10.0F, -7.0F, -0.7807508F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(9, 36)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, 10.0F, -4.333333F, -0.2602503F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail0",
                CubeListBuilder.create().texOffs(0, 46)
                        .addBox(-1.0F, -1.0F, -3.0F, 4, 4, 3),
                PartPose.offset(-1.0F, 9.0F, -2.0F));

        partdefinition.addOrReplaceChild("lfLegLower",
                CubeListBuilder.create().texOffs(205, 55)
                        .addBox(-2.0F, 0.0F, -3.0F, 4, 6, 4),
                PartPose.offsetAndRotation(-3.0F, 18.0F, 2.0F, -0.4461433F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtLegLower",
                CubeListBuilder.create().texOffs(149, 53)
                        .addBox(-2.0F, -1.0F, -3.0F, 4, 7, 4),
                PartPose.offsetAndRotation(3.0F, 18.0F, 4.0F, -0.1487144F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(168, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8),
                PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.2230705F, 3.141593F, 0.0F));

        partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(170, 17)
                        .addBox(-4.0F, 0.0F, -2.0F, 8, 7, 6),
                PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, -0.8551081F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfArmUpper",
                CubeListBuilder.create().texOffs(205, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4, 7, 4),
                PartPose.offsetAndRotation(-5.0F, 2.0F, 3.0F, 0.0F, 0.0F, 0.3020292F));

        partdefinition.addOrReplaceChild("rtArmLower",
                CubeListBuilder.create().texOffs(136, 29)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4),
                PartPose.offsetAndRotation(7.0F, 6.0F, 2.0F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfLegUpper",
                CubeListBuilder.create().texOffs(188, 46)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.260246F, 0.0F, 0.2602503F));

        partdefinition.addOrReplaceChild("rtLegUpper",
                CubeListBuilder.create().texOffs(168, 46)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.5948578F, 0.0F, -0.260246F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(170, 31)
                        .addBox(-4.0F, 0.0F, -3.0F, 8, 7, 6),
                PartPose.offsetAndRotation(0.0F, 5.0F, 1.0F, -0.2974289F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtArmUpper",
                CubeListBuilder.create().texOffs(142, 16)
                        .addBox(-1.0F, -2.0F, -2.0F, 4, 7, 4),
                PartPose.offsetAndRotation(5.0F, 2.0F, 2.0F, 0.0F, 0.0F, -0.302028F));

        partdefinition.addOrReplaceChild("lfArmLower",
                CubeListBuilder.create().texOffs(208, 31)
                        .addBox(-2.0F, 0.0F, -2.0F, 4, 7, 4),
                PartPose.offsetAndRotation(-7.0F, 6.0F, 2.0F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(38, 42)
                        .addBox(-2.5F, -2.0F, -5.0F, 5, 5, 5),
                PartPose.offsetAndRotation(0.0F, 6.5F, -10.0F, -0.96F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(59, 41)
                        .addBox(-3.0F, -3.0F, -6.0F, 6, 6, 6),
                PartPose.offsetAndRotation(0.0F, 3.0F, -12.0F, -0.22F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfHorn2",
                CubeListBuilder.create().texOffs(13, 5)
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 4),
                PartPose.offsetAndRotation(-3.0F, -10.0F, 2.0F, -0.2230705F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtHorn1",
                CubeListBuilder.create().texOffs(0, 22)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3),
                PartPose.offsetAndRotation(3.0F, -8.7F, 4.0F, -0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtHorn2",
                CubeListBuilder.create().texOffs(13, 21)
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 4),
                PartPose.offsetAndRotation(3.0F, -10.0F, 2.0F, -0.2230705F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfHorn1",
                CubeListBuilder.create().texOffs(0, 6)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3),
                PartPose.offsetAndRotation(-3.0F, -8.7F, 4.0F, -0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfHorn3",
                CubeListBuilder.create().texOffs(31, 6)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3),
                PartPose.offsetAndRotation(-3.0F, -11.0F, -2.0F, -0.0371786F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtHorn3",
                CubeListBuilder.create().texOffs(31, 22)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3),
                PartPose.offsetAndRotation(3.0F, -11.0F, -2.0F, -0.0371786F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfHorn4",
                CubeListBuilder.create().texOffs(45, 23)
                        .addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2),
                PartPose.offsetAndRotation(-3.0F, -11.0F, -5.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtHorn4",
                CubeListBuilder.create().texOffs(45, 7)
                        .addBox(-1.0F, -1.0F, -2.0F, 2, 2, 2),
                PartPose.offsetAndRotation(3.0F, -11.0F, -5.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("headFire",
                CubeListBuilder.create().texOffs(168, 84)
                        .addBox(-5.0F, -10.0F, -5.0F, 10, 10, 10),
                PartPose.offsetAndRotation(0.0F, 1.0F, 6.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfArmUpperFire",
                CubeListBuilder.create().texOffs(209, 108)
                        .addBox(-6.0F, -1.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(-3.0F, 1.0F, 3.0F, 0.0F, 0.0F, 0.3020292F));

        partdefinition.addOrReplaceChild("chestFire",
                CubeListBuilder.create().texOffs(170, 105)
                        .addBox(-5.0F, 0.0F, -3.0F, 10, 9, 8),
                PartPose.offsetAndRotation(0.0F, -1.0F, 6.0F, -0.8551081F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodyFire",
                CubeListBuilder.create().texOffs(170, 125)
                        .addBox(-5.0F, 0.0F, -4.0F, 10, 9, 8),
                PartPose.offsetAndRotation(0.0F, 4.0F, 1.0F, -0.2974289F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfArmLowerFire",
                CubeListBuilder.create().texOffs(208, 126)
                        .addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(-7.333333F, 5.0F, 1.5F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtArmUpperFire",
                CubeListBuilder.create().texOffs(142, 105)
                        .addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(5.0F, -1.0F, 2.0F, 0.0F, 0.0F, -0.302028F));

        partdefinition.addOrReplaceChild("rtArmLowerFire",
                CubeListBuilder.create().texOffs(136, 122)
                        .addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(7.0F, 5.0F, 1.0F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lfLegUppperFire",
                CubeListBuilder.create().texOffs(188, 146)
                        .addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, 0.260246F, 0.0F, 0.2602503F));

        partdefinition.addOrReplaceChild("lfLegLowerFire",
                CubeListBuilder.create().texOffs(205, 163)
                        .addBox(-3.0F, 0.0F, -3.0F, 6, 8, 6),
                PartPose.offsetAndRotation(-3.0F, 16.0F, 2.0F, -0.4461433F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtLegUpperFire",
                CubeListBuilder.create().texOffs(160, 146)
                        .addBox(-3.0F, 0.0F, -4.0F, 6, 9, 6),
                PartPose.offsetAndRotation(2.0F, 11.0F, 0.0F, 0.5948578F, 0.0F, -0.260246F));

        partdefinition.addOrReplaceChild("rtLegLowerFire",
                CubeListBuilder.create().texOffs(150, 167)
                        .addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6),
                PartPose.offsetAndRotation(3.0F, 15.0F, 4.0F, -0.1487144F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 256);
    }

    @Override
    public void setupAnim(EntityKyuubi entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        newangle = (double)limbSwingAmount > 0.1 ? Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.2f * limbSwingAmount : 0.0f;
        this.rtLegUpper.xRot = 0.59f + newangle;
        this.rtLegUpperFire.xRot = 0.59f + newangle;
        this.rtLegLower.xRot = -0.15f + newangle;
        this.rtLegLowerFire.xRot = -0.15f + newangle;
        this.rtLegLower.z = (float)(Math.sin(this.rtLegUpperFire.xRot) * 8.0);
        this.rtLegLowerFire.z = (float)(Math.sin(this.rtLegUpperFire.xRot) * 8.0);
        this.lfLegUpper.xRot = 0.26f - newangle;
        this.lfLegUppperFire.xRot = 0.26f - newangle;
        this.lfLegLower.xRot = -0.44f - newangle;
        this.lfLegLowerFire.xRot = -0.44f - newangle;
        this.lfLegLower.z = (float)(Math.sin(this.lfLegUppperFire.xRot) * 8.0);
        this.lfLegLowerFire.z = (float)(Math.sin(this.lfLegUppperFire.xRot) * 8.0);
        newangle = Mth.cos((float)(ageInTicks * 1.1f * limbSwingAmount)) * (float)Math.PI * 0.08f * limbSwingAmount;
        this.rtArmUpper.xRot = newangle += Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.01f;
        this.rtArmUpperFire.xRot = newangle;
        this.rtArmLower.xRot = 0.48f + newangle;
        this.rtArmLowerFire.xRot = 0.48f + newangle;
        this.rtArmLower.z = (float)(Math.sin(this.rtArmUpperFire.xRot) * 8.0);
        this.rtArmLowerFire.z = (float)(Math.sin(this.rtArmUpperFire.xRot) * 8.0);
        this.lfArmUpper.xRot = -newangle;
        this.lfArmUpperFire.xRot = -newangle;
        this.lfArmLower.xRot = 0.48f - newangle;
        this.lfArmLowerFire.xRot = 0.48f - newangle;
        this.lfArmLower.z = (float)(Math.sin(this.lfArmUpperFire.xRot) * 8.0);
        this.lfArmLowerFire.z = (float)(Math.sin(this.lfArmUpperFire.xRot) * 8.0);
        float pi4 = 0.7853975f;
        this.head.yRot = (float)Math.toRadians(netHeadYaw) + pi4 * 4.0f;
        this.headFire.yRot = (float)Math.toRadians(netHeadYaw);
        float fc = (float)Math.cos(this.headFire.yRot + pi4);
        float fs = (float)Math.sin(this.headFire.yRot + pi4);
        this.lfHorn1.z = this.headFire.z - fc * 3.6f;
        this.lfHorn1.x = this.headFire.x - fs * 3.6f;
        this.lfHorn1.yRot = this.headFire.yRot + 0.244f + Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.lfHorn2.z = this.lfHorn1.z - (float)Math.cos(this.lfHorn1.yRot) * 2.0f;
        this.lfHorn2.x = this.lfHorn1.x - (float)Math.sin(this.lfHorn1.yRot) * 2.0f;
        this.lfHorn2.yRot = this.headFire.yRot + 0.244f + Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - pi4)) * (float)Math.PI * 0.1f;
        this.lfHorn3.z = this.lfHorn2.z - (float)Math.cos(this.lfHorn2.yRot) * 4.0f;
        this.lfHorn3.x = this.lfHorn2.x - (float)Math.sin(this.lfHorn2.yRot) * 4.0f;
        this.lfHorn3.yRot = this.headFire.yRot + 0.244f + Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.1f;
        this.lfHorn4.z = this.lfHorn3.z - (float)Math.cos(this.lfHorn3.yRot) * 3.0f;
        this.lfHorn4.x = this.lfHorn3.x - (float)Math.sin(this.lfHorn3.yRot) * 3.0f;
        this.lfHorn4.yRot = this.headFire.yRot + 0.244f + Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.1f;
        this.lfHorn5.z = this.lfHorn4.z - (float)Math.cos(this.lfHorn4.yRot) * 2.0f;
        this.lfHorn5.x = this.lfHorn4.x - (float)Math.sin(this.lfHorn4.yRot) * 2.0f;
        this.lfHorn5.yRot = this.headFire.yRot + 0.244f + Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 4.0f * pi4)) * (float)Math.PI * 0.1f;
        fc = (float)Math.cos(this.headFire.yRot - pi4);
        fs = (float)Math.sin(this.headFire.yRot - pi4);
        this.rtHorn1.z = this.headFire.z - fc * 3.6f;
        this.rtHorn1.x = this.headFire.x - fs * 3.6f;
        this.rtHorn1.yRot = this.headFire.yRot + -0.244f - Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.rtHorn2.z = this.rtHorn1.z - (float)Math.cos(this.rtHorn1.yRot) * 2.0f;
        this.rtHorn2.x = this.rtHorn1.x - (float)Math.sin(this.rtHorn1.yRot) * 2.0f;
        this.rtHorn2.yRot = this.headFire.yRot + -0.244f - Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - pi4)) * (float)Math.PI * 0.1f;
        this.rtHorn3.z = this.rtHorn2.z - (float)Math.cos(this.rtHorn2.yRot) * 4.0f;
        this.rtHorn3.x = this.rtHorn2.x - (float)Math.sin(this.rtHorn2.yRot) * 4.0f;
        this.rtHorn3.yRot = this.headFire.yRot + -0.244f - Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.1f;
        this.rtHorn4.z = this.rtHorn3.z - (float)Math.cos(this.rtHorn3.yRot) * 3.0f;
        this.rtHorn4.x = this.rtHorn3.x - (float)Math.sin(this.rtHorn3.yRot) * 3.0f;
        this.rtHorn4.yRot = this.headFire.yRot + -0.244f - Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.1f;
        this.rtHorn5.z = this.rtHorn4.z - (float)Math.cos(this.rtHorn4.yRot) * 2.0f;
        this.rtHorn5.x = this.rtHorn4.x - (float)Math.sin(this.rtHorn4.yRot) * 2.0f;
        this.rtHorn5.yRot = this.headFire.yRot + -0.244f - Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount - 4.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail1.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount)) * (float)Math.PI * 0.2f;
        this.tail2.x = this.tail1.x - (float)Math.sin(this.tail1.yRot) * 3.0f;
        this.tail2.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - pi4)) * (float)Math.PI * 0.2f;
        this.tail3.x = this.tail2.x - (float)Math.sin(this.tail2.yRot) * 4.0f;
        this.tail3.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail4.x = this.tail3.x - (float)Math.sin(this.tail3.yRot) * 3.5f;
        this.tail4.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail5.x = this.tail4.x - (float)Math.sin(this.tail4.yRot) * 5.0f;
        this.tail5.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 4.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail6.x = this.tail5.x - (float)Math.sin(this.tail5.yRot) * 4.0f;
        this.tail6.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 5.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail7.x = this.tail6.x - (float)Math.sin(this.tail6.yRot) * 3.0f;
        this.tail7.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 6.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail8.x = this.tail7.x - (float)Math.sin(this.tail7.yRot) * 2.0f;
        this.tail8.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 7.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail9.x = this.tail8.x - (float)Math.sin(this.tail8.yRot) * 1.0f;
        this.tail9.yRot = Mth.cos((float)(ageInTicks * 0.9f * limbSwingAmount - 8.0f * pi4)) * (float)Math.PI * 0.2f;
        this.tail1.xRot = -0.26f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.tail2.y = this.tail1.y + (float)Math.sin(this.tail1.xRot) * 3.0f;
        this.tail2.z = this.tail1.z - (float)Math.cos(this.tail1.xRot) * 3.0f;
        this.tail2.xRot = -0.78f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - pi4)) * (float)Math.PI * 0.1f;
        this.tail3.y = this.tail2.y + (float)Math.sin(this.tail2.xRot) * 4.0f;
        this.tail3.z = this.tail2.z - (float)Math.cos(this.tail2.xRot) * 4.0f;
        this.tail3.xRot = -1.11f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 2.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail4.y = this.tail3.y + (float)Math.sin(this.tail3.xRot) * 3.5f;
        this.tail4.z = this.tail3.z - (float)Math.cos(this.tail3.xRot) * 3.5f;
        this.tail4.xRot = -0.18f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 3.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail5.y = this.tail4.y + (float)Math.sin(this.tail4.xRot) * 5.0f;
        this.tail5.z = this.tail4.z - (float)Math.cos(this.tail4.xRot) * 5.0f;
        this.tail5.xRot = 0.22f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 4.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail6.y = this.tail5.y + (float)Math.sin(this.tail5.xRot) * 4.0f;
        this.tail6.z = this.tail5.z - (float)Math.cos(this.tail5.xRot) * 4.0f;
        this.tail6.xRot = 0.63f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 5.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail7.y = this.tail6.y + (float)Math.sin(this.tail6.xRot) * 3.0f;
        this.tail7.z = this.tail6.z - (float)Math.cos(this.tail6.xRot) * 3.0f;
        this.tail7.xRot = 0.89f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 6.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail8.y = this.tail7.y + (float)Math.sin(this.tail7.xRot) * 2.0f;
        this.tail8.z = this.tail7.z - (float)Math.cos(this.tail7.xRot) * 2.0f;
        this.tail8.xRot = 1.52f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 7.0f * pi4)) * (float)Math.PI * 0.1f;
        this.tail9.y = this.tail8.y + (float)Math.sin(this.tail8.xRot) * 2.0f;
        this.tail9.z = this.tail8.z - (float)Math.cos(this.tail8.xRot) * 2.0f;
        this.tail9.xRot = 2.0f + Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount - 8.0f * pi4)) * (float)Math.PI * 0.1f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.lfLegLower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtLegLower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfArmUpper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtArmLower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfLegUpper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtLegUpper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtArmUpper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfArmLower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtHorn5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfHorn5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail0.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfHorn2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtHorn1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtHorn2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfHorn1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfHorn3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtHorn3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfHorn4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtHorn4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.headFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfArmUpperFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chestFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodyFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfArmLowerFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtArmUpperFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtArmLowerFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfLegUppperFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfLegLowerFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtLegUpperFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtLegLowerFire.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
