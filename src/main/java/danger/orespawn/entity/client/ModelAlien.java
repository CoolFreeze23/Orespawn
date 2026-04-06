package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Alien;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelAlien extends EntityModel<Alien> {
    private int ri1, ri2, ri3;
    private final ModelPart torso;
    private final ModelPart stomach;
    private final ModelPart rThigh;
    private final ModelPart lThigh;
    private final ModelPart lShin;
    private final ModelPart rShin;
    private final ModelPart lShin1;
    private final ModelPart rShin1;
    private final ModelPart lFoot;
    private final ModelPart rFoot;
    private final ModelPart neck;
    private final ModelPart fan;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail1;
    private final ModelPart fanl1;
    private final ModelPart fanr1;
    private final ModelPart fanl2;
    private final ModelPart fanr2;
    private final ModelPart fanl3;
    private final ModelPart fanr3;
    private final ModelPart fanl4;
    private final ModelPart fanr4;
    private final ModelPart fanl5;
    private final ModelPart fanr5;
    private final ModelPart fanl6;
    private final ModelPart fanr6;
    private final ModelPart spike4;
    private final ModelPart spike5;
    private final ModelPart spike3;
    private final ModelPart fanl7;
    private final ModelPart fanr7;
    private final ModelPart head;
    private final ModelPart head1;
    private final ModelPart jaw1;
    private final ModelPart head2;
    private final ModelPart jaw2;
    private final ModelPart fang1;
    private final ModelPart fang2;
    private final ModelPart fang3;
    private final ModelPart fang4;
    private final ModelPart spike2;
    private final ModelPart spike1;
    private final ModelPart arml1;
    private final ModelPart armr1;
    private final ModelPart arml2;
    private final ModelPart armr2;
    private final ModelPart clawr1;
    private final ModelPart clawr2;
    private final ModelPart clawr3;
    private final ModelPart clawl2;
    private final ModelPart clawl3;
    private final ModelPart clawl1;

    public ModelAlien(ModelPart root) {
        this.torso = root.getChild("torso");
        this.stomach = root.getChild("stomach");
        this.rThigh = root.getChild("rThigh");
        this.lThigh = root.getChild("lThigh");
        this.lShin = root.getChild("lShin");
        this.rShin = root.getChild("rShin");
        this.lShin1 = root.getChild("lShin1");
        this.rShin1 = root.getChild("rShin1");
        this.lFoot = root.getChild("lFoot");
        this.rFoot = root.getChild("rFoot");
        this.neck = root.getChild("neck");
        this.fan = root.getChild("fan");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.tail5 = root.getChild("tail5");
        this.tail1 = root.getChild("tail1");
        this.fanl1 = root.getChild("fanl1");
        this.fanr1 = root.getChild("fanr1");
        this.fanl2 = root.getChild("fanl2");
        this.fanr2 = root.getChild("fanr2");
        this.fanl3 = root.getChild("fanl3");
        this.fanr3 = root.getChild("fanr3");
        this.fanl4 = root.getChild("fanl4");
        this.fanr4 = root.getChild("fanr4");
        this.fanl5 = root.getChild("fanl5");
        this.fanr5 = root.getChild("fanr5");
        this.fanl6 = root.getChild("fanl6");
        this.fanr6 = root.getChild("fanr6");
        this.spike4 = root.getChild("spike4");
        this.spike5 = root.getChild("spike5");
        this.spike3 = root.getChild("spike3");
        this.fanl7 = root.getChild("fanl7");
        this.fanr7 = root.getChild("fanr7");
        this.head = root.getChild("head");
        this.head1 = root.getChild("head1");
        this.jaw1 = root.getChild("jaw1");
        this.head2 = root.getChild("head2");
        this.jaw2 = root.getChild("jaw2");
        this.fang1 = root.getChild("fang1");
        this.fang2 = root.getChild("fang2");
        this.fang3 = root.getChild("fang3");
        this.fang4 = root.getChild("fang4");
        this.spike2 = root.getChild("spike2");
        this.spike1 = root.getChild("spike1");
        this.arml1 = root.getChild("arml1");
        this.armr1 = root.getChild("armr1");
        this.arml2 = root.getChild("arml2");
        this.armr2 = root.getChild("armr2");
        this.clawr1 = root.getChild("clawr1");
        this.clawr2 = root.getChild("clawr2");
        this.clawr3 = root.getChild("clawr3");
        this.clawl2 = root.getChild("clawl2");
        this.clawl3 = root.getChild("clawl3");
        this.clawl1 = root.getChild("clawl1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("torso",
                CubeListBuilder.create().texOffs(0, 46).mirror().addBox(-4.5F, -2.0F, 0.0F, 9, 8, 10),
                PartPose.offsetAndRotation(0.0F, -2.5F, -8.0F, -0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("stomach",
                CubeListBuilder.create().texOffs(0, 27).mirror().addBox(-3.5F, -5.0F, 8.0F, 7, 6, 12),
                PartPose.offsetAndRotation(0.0F, -2.5F, -8.0F, -0.5585054F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rThigh",
                CubeListBuilder.create().texOffs(59, 45).mirror().addBox(-1.5F, -4.0F, -2.5F, 4, 14, 5),
                PartPose.offsetAndRotation(-4.5F, 7.0F, 8.0F, -0.8028515F, 0.2443461F, 0.418879F));

        partdefinition.addOrReplaceChild("lThigh",
                CubeListBuilder.create().texOffs(40, 45).mirror().addBox(-2.5F, -4.0F, -2.5F, 4, 14, 5),
                PartPose.offsetAndRotation(4.5F, 7.0F, 8.0F, -0.8028515F, -0.2443461F, -0.418879F));

        partdefinition.addOrReplaceChild("lShin",
                CubeListBuilder.create().texOffs(79, 49).mirror().addBox(-2.0F, 8.0F, -5.5F, 3, 3, 12),
                PartPose.offsetAndRotation(4.5F, 7.0F, 8.0F, -0.4014257F, -0.2443461F, -0.418879F));

        partdefinition.addOrReplaceChild("rShin",
                CubeListBuilder.create().texOffs(79, 33).mirror().addBox(-1.0F, 8.0F, -5.5F, 3, 3, 12),
                PartPose.offsetAndRotation(-4.5F, 7.0F, 8.0F, -0.4014257F, 0.2443461F, 0.418879F));

        partdefinition.addOrReplaceChild("lShin1",
                CubeListBuilder.create().texOffs(113, 40).mirror().addBox(-1.5F, 5.5F, 9.0F, 2, 9, 2),
                PartPose.offsetAndRotation(4.5F, 7.0F, 8.0F, -0.8028515F, -0.2443461F, -0.418879F));

        partdefinition.addOrReplaceChild("rShin1",
                CubeListBuilder.create().texOffs(113, 53).mirror().addBox(-0.5F, 5.5F, 9.0F, 2, 9, 2),
                PartPose.offsetAndRotation(-4.5F, 7.0F, 8.0F, -0.8028515F, 0.2443461F, 0.418879F));

        partdefinition.addOrReplaceChild("lFoot",
                CubeListBuilder.create().texOffs(110, 24).mirror().addBox(5.0F, 15.0F, -8.0F, 2, 2, 6),
                PartPose.offsetAndRotation(4.5F, 7.0F, 8.0F, 0.0F, -0.2443461F, 0.0F));

        partdefinition.addOrReplaceChild("rFoot",
                CubeListBuilder.create().texOffs(95, 24).mirror().addBox(-7.0F, 15.0F, -8.0F, 2, 2, 6),
                PartPose.offsetAndRotation(-4.5F, 7.0F, 8.0F, 0.0F, 0.2443461F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(23, 86).mirror().addBox(-2.0F, -2.0F, -4.0F, 4, 6, 5),
                PartPose.offsetAndRotation(0.0F, -2.5F, -8.0F, -0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fan",
                CubeListBuilder.create().texOffs(149, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offset(0.0F, -7.0F, -10.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(85, 66).mirror().addBox(-2.0F, -1.5F, 0.0F, 4, 4, 11),
                PartPose.offsetAndRotation(0.0F, 9.5F, 20.5F, -0.3141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(118, 66).mirror().addBox(-1.5F, -1.5F, 0.0F, 3, 3, 11),
                PartPose.offsetAndRotation(0.0F, 13.5F, 30.5F, -0.2094395F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(149, 66).mirror().addBox(-1.0F, -1.0F, 0.0F, 2, 2, 11),
                PartPose.offsetAndRotation(0.0F, 15.5F, 40.5F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(178, 66).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 11),
                PartPose.offsetAndRotation(0.0F, 17.5F, 50.5F, -0.0523599F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(50, 66).mirror().addBox(-2.0F, -2.5F, 0.0F, 4, 4, 11),
                PartPose.offsetAndRotation(0.0F, 6.5F, 10.5F, -0.4014257F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fanl1",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 0.2617994F));

        partdefinition.addOrReplaceChild("fanr1",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -0.2617994F));

        partdefinition.addOrReplaceChild("fanl2",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 0.5235988F));

        partdefinition.addOrReplaceChild("fanr2",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("fanl3",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 0.7853982F));

        partdefinition.addOrReplaceChild("fanr3",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -0.7853982F));

        partdefinition.addOrReplaceChild("fanl4",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 1.047198F));

        partdefinition.addOrReplaceChild("fanr4",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -1.047198F));

        partdefinition.addOrReplaceChild("fanl5",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 1.308997F));

        partdefinition.addOrReplaceChild("fanr5",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -1.308997F));

        partdefinition.addOrReplaceChild("fanl6",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 1.570796F));

        partdefinition.addOrReplaceChild("fanr6",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -1.570796F));

        partdefinition.addOrReplaceChild("spike4",
                CubeListBuilder.create().texOffs(178, 66).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 11),
                PartPose.offsetAndRotation(0.0F, 16.0F, 41.0F, -0.0523599F, 0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("spike5",
                CubeListBuilder.create().texOffs(178, 66).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 11),
                PartPose.offsetAndRotation(0.0F, 16.0F, 41.0F, -0.0523599F, -0.5759587F, 0.0F));

        partdefinition.addOrReplaceChild("spike3",
                CubeListBuilder.create().texOffs(178, 66).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 11),
                PartPose.offsetAndRotation(0.0F, 13.5F, 30.5F, 0.3141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("fanl7",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, 1.832596F));

        partdefinition.addOrReplaceChild("fanr7",
                CubeListBuilder.create().texOffs(130, 10).mirror().addBox(-3.0F, -24.0F, 0.0F, 6, 24, 1),
                PartPose.offsetAndRotation(0.0F, -7.0F, -10.0F, 0.0F, 0.0F, -1.832596F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(200, 0).mirror().addBox(-3.0F, -4.0F, -7.0F, 6, 7, 8),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(200, 18).mirror().addBox(-2.5F, -2.0F, -15.0F, 5, 2, 8),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("jaw1",
                CubeListBuilder.create().texOffs(200, 43).mirror().addBox(-2.0F, -1.0F, -7.0F, 4, 2, 8),
                PartPose.offset(0.0F, -2.0F, -19.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(200, 31).mirror().addBox(-2.0F, -2.0F, -22.0F, 4, 2, 7),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("jaw2",
                CubeListBuilder.create().texOffs(200, 56).mirror().addBox(-1.5F, -1.0F, -13.0F, 3, 2, 6),
                PartPose.offset(0.0F, -2.0F, -19.0F));

        partdefinition.addOrReplaceChild("fang1",
                CubeListBuilder.create().texOffs(42, 0).mirror().addBox(1.0F, 0.0F, -20.0F, 1, 5, 1),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("fang2",
                CubeListBuilder.create().texOffs(50, 0).mirror().addBox(-2.0F, 0.0F, -20.0F, 1, 5, 1),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("fang3",
                CubeListBuilder.create().texOffs(60, 0).mirror().addBox(1.0F, 0.0F, -14.0F, 1, 3, 1),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("fang4",
                CubeListBuilder.create().texOffs(69, 0).mirror().addBox(-2.0F, 0.0F, -14.0F, 1, 3, 1),
                PartPose.offset(0.0F, -3.0F, -11.0F));

        partdefinition.addOrReplaceChild("spike2",
                CubeListBuilder.create().texOffs(178, 66).mirror().addBox(-0.5F, -0.5F, 0.0F, 1, 1, 11),
                PartPose.offsetAndRotation(0.0F, 9.5F, 20.5F, 0.3141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("spike1",
                CubeListBuilder.create().texOffs(178, 66).mirror().addBox(-0.5F, -1.5F, 0.0F, 1, 1, 11),
                PartPose.offsetAndRotation(0.0F, 6.5F, 10.5F, 0.3141593F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arml1",
                CubeListBuilder.create().texOffs(50, 98).mirror().addBox(0.0F, 0.0F, -2.0F, 11, 3, 4),
                PartPose.offsetAndRotation(2.0F, -1.0F, -6.0F, 0.0F, -0.5235988F, 0.1745329F));

        partdefinition.addOrReplaceChild("armr1",
                CubeListBuilder.create().texOffs(49, 88).mirror().addBox(0.0F, 0.0F, -2.0F, 11, 3, 4),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -6.0F, 0.0F, -2.617994F, -0.1745329F));

        partdefinition.addOrReplaceChild("arml2",
                CubeListBuilder.create().texOffs(41, 107).mirror().addBox(0.0F, -1.0F, -1.0F, 15, 3, 3),
                PartPose.offsetAndRotation(11.0F, 2.0F, -1.0F, 0.0F, 0.8552113F, 0.0F));

        partdefinition.addOrReplaceChild("armr2",
                CubeListBuilder.create().texOffs(42, 115).mirror().addBox(0.0F, -1.0F, -2.0F, 15, 3, 3),
                PartPose.offsetAndRotation(-11.0F, 2.0F, -1.0F, 0.0F, 2.268928F, 0.0F));

        partdefinition.addOrReplaceChild("clawr1",
                CubeListBuilder.create().texOffs(100, 85).mirror().addBox(-0.5F, -1.0F, -6.0F, 1, 1, 6),
                PartPose.offsetAndRotation(-21.0F, 2.0F, -12.0F, -0.1745329F, 0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("clawr2",
                CubeListBuilder.create().texOffs(100, 94).mirror().addBox(0.0F, 0.0F, -10.0F, 1, 1, 10),
                PartPose.offsetAndRotation(-21.0F, 2.0F, -12.0F, 0.0F, 0.8726646F, 0.0F));

        partdefinition.addOrReplaceChild("clawr3",
                CubeListBuilder.create().texOffs(100, 107).mirror().addBox(0.0F, 1.0F, -6.0F, 1, 1, 6),
                PartPose.offsetAndRotation(-21.0F, 2.0F, -12.0F, 0.1745329F, 0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("clawl2",
                CubeListBuilder.create().texOffs(130, 94).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 10),
                PartPose.offsetAndRotation(21.0F, 2.0F, -12.0F, 0.0F, 2.268928F, 0.0F));

        partdefinition.addOrReplaceChild("clawl3",
                CubeListBuilder.create().texOffs(130, 109).mirror().addBox(0.0F, 1.0F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(21.0F, 2.0F, -12.0F, -0.1745329F, 2.70526F, 0.0F));

        partdefinition.addOrReplaceChild("clawl1",
                CubeListBuilder.create().texOffs(130, 83).mirror().addBox(0.0F, -1.0F, 0.0F, 1, 1, 6),
                PartPose.offsetAndRotation(21.0F, 2.0F, -12.0F, 0.1745329F, 2.70526F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }

    @Override
    public void setupAnim(Alien entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float nextangle = 0.0f;
        newangle = Mth.cos((float)(ageInTicks * 4.0f * limbSwingAmount)) * (float)Math.PI * 0.5f * limbSwingAmount;
        this.doLeftLeg(newangle);
        this.doRightLeg(-newangle);
        if (entity.getAttacking() == 0) {
        this.fan.zRot = 0.0f;
        this.fanl1.zRot = 0.0f;
        this.fanl2.zRot = 0.0f;
        this.fanl3.zRot = 0.0f;
        this.fanl4.zRot = 0.0f;
        this.fanl5.zRot = 0.0f;
        this.fanl6.zRot = 0.0f;
        this.fanl7.zRot = 0.0f;
        this.fanr1.zRot = 0.0f;
        this.fanr2.zRot = 0.0f;
        this.fanr3.zRot = 0.0f;
        this.fanr4.zRot = 0.0f;
        this.fanr5.zRot = 0.0f;
        this.fanr6.zRot = 0.0f;
        this.fanr7.zRot = 0.0f;
        this.fan.xRot = -1.85f;
        this.fanl1.xRot = -1.85f;
        this.fanl2.xRot = -1.85f;
        this.fanl3.xRot = -1.85f;
        this.fanl4.xRot = -1.85f;
        this.fanl5.xRot = -1.85f;
        this.fanl6.xRot = -1.85f;
        this.fanl7.xRot = -1.85f;
        this.fanr1.xRot = -1.85f;
        this.fanr2.xRot = -1.85f;
        this.fanr3.xRot = -1.85f;
        this.fanr4.xRot = -1.85f;
        this.fanr5.xRot = -1.85f;
        this.fanr6.xRot = -1.85f;
        this.fanr7.xRot = -1.85f;
        } else {
        float pi6 = 0.5235988f;
        float fanspeed = 1.22f;
        float fanamp = 0.1f;
        this.fan.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount)) * (float)Math.PI * fanamp;
        this.fanl1.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 1.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanl2.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 2.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanl3.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 3.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanl4.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 4.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanl5.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 5.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanl6.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 6.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanl7.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 7.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr1.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 1.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr2.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 2.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr3.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 3.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr4.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 4.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr5.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 5.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr6.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 6.0f * pi6)) * (float)Math.PI * fanamp;
        this.fanr7.xRot = Mth.cos((float)(ageInTicks * fanspeed * limbSwingAmount - 7.0f * pi6)) * (float)Math.PI * fanamp;
        this.fan.zRot = 0.0f;
        this.fanl1.zRot = 0.261f;
        this.fanl2.zRot = 0.523f;
        this.fanl3.zRot = 0.785f;
        this.fanl4.zRot = 1.047f;
        this.fanl5.zRot = 1.309f;
        this.fanl6.zRot = 1.571f;
        this.fanl7.zRot = 1.832f;
        this.fanr1.zRot = -0.261f;
        this.fanr2.zRot = -0.523f;
        this.fanr3.zRot = -0.785f;
        this.fanr4.zRot = -1.047f;
        this.fanr5.zRot = -1.309f;
        this.fanr6.zRot = -1.571f;
        this.fanr7.zRot = -1.832f;
        }
        this.neck.yRot = (float)Math.toRadians(netHeadYaw) * 0.35f;
        this.head.yRot = (float)Math.toRadians(netHeadYaw) * 0.75f;
        this.head.z = this.neck.z - (float)Math.cos(this.neck.yRot) * 3.0f;
        this.head.x = this.neck.x + (float)Math.sin(this.neck.yRot) * 3.0f;
        this.head1.yRot = this.head.yRot;
        this.head1.z = this.head.z;
        this.head1.x = this.head.x;
        this.head2.yRot = this.head.yRot;
        this.head2.z = this.head.z;
        this.head2.x = this.head.x;
        this.fang1.yRot = this.head.yRot;
        this.fang1.z = this.head.z;
        this.fang1.x = this.head.x;
        this.fang2.yRot = this.head.yRot;
        this.fang2.z = this.head.z;
        this.fang2.x = this.head.x;
        this.fang3.yRot = this.head.yRot;
        this.fang3.z = this.head.z;
        this.fang3.x = this.head.x;
        this.fang4.yRot = this.head.yRot;
        this.fang4.z = this.head.z;
        this.fang4.x = this.head.x;
        this.jaw1.yRot = this.head.yRot;
        this.jaw1.z = this.head.z - (float)Math.cos(this.head.yRot) * 8.0f;
        this.jaw1.x = this.head.x - (float)Math.sin(this.head.yRot) * 8.0f;
        this.jaw2.yRot = this.jaw1.yRot;
        this.jaw2.z = this.jaw1.z;
        this.jaw2.x = this.jaw1.x;
        newangle = Mth.cos((float)(ageInTicks * 3.5f * limbSwingAmount)) * (float)Math.PI * 0.5f;
        nextangle = Mth.cos((float)((ageInTicks + 0.2f) * 3.5f * limbSwingAmount)) * (float)Math.PI * 0.5f;
        if (nextangle > 0.0f && newangle < 0.0f) {
        if (entity.getAttacking() == 0) {
        ri1 = entity.getRandom().nextInt(15);
        ri2 = entity.getRandom().nextInt(15);
        ri3 = entity.getRandom().nextInt(15);
        } else {
        ri1 = entity.getRandom().nextInt(4);
        ri2 = entity.getRandom().nextInt(2);
        ri3 = 1;
        }
        }
        if (ri2 == 1) {
        this.doTail(newangle);
        } else {
        newangle = Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.05f;
        this.doTail(newangle);
        }
        if (ri3 == 1) {
        newangle = Mth.cos((float)(ageInTicks * 3.5f * limbSwingAmount)) * (float)Math.PI * 0.35f;
        this.doJaw(newangle);
        } else {
        newangle = Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.02f;
        this.doJaw(newangle);
        }
        newangle = Mth.cos((float)(ageInTicks * limbSwingAmount * 3.5f)) * (float)Math.PI * 0.2f;
        if (ri1 == 1 || ri1 == 3) {
        this.doLeftClaw(newangle);
        } else {
        newangle = Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.03f;
        this.doLeftClaw(newangle);
        }
        if (ri1 == 2 || ri1 == 3) {
        this.doRightClaw(-newangle);
        } else {
        newangle = Mth.cos((float)(ageInTicks * limbSwingAmount)) * (float)Math.PI * 0.03f;
        this.doRightClaw(-newangle);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.stomach.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lThigh.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lShin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rShin.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lShin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rShin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rFoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fang1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fang2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fang3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fang4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arml1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.armr1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arml2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.armr2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.clawr1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.clawr2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.clawr3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.clawl2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.clawl3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.clawl1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fan.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanl7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.fanr7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void doLeftLeg(float angle) {
        this.lFoot.xRot = angle;
        this.lShin.xRot = angle - 0.4f;
        this.lShin1.xRot = angle - 0.8f;
        this.lThigh.xRot = angle - 0.8f;
    }

    private void doRightLeg(float angle) {
        this.rFoot.xRot = angle;
        this.rShin.xRot = angle - 0.4f;
        this.rShin1.xRot = angle - 0.8f;
        this.rThigh.xRot = angle - 0.8f;
    }

    private void doJaw(float angle) {
        this.jaw2.xRot = this.jaw1.xRot = Math.abs(angle);
    }

    private void doTail(float angle) {
        this.spike1.yRot = this.tail1.yRot = angle * 0.25f;
        this.tail2.yRot = angle * 0.5f;
        this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 10.0f;
        this.tail2.x = this.tail1.x + (float)Math.sin(this.tail1.yRot) * 10.0f;
        this.spike2.yRot = this.tail2.yRot;
        this.spike2.z = this.tail2.z;
        this.spike2.x = this.tail2.x;
        this.tail3.yRot = angle * 0.8f;
        this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 10.0f;
        this.tail3.x = this.tail2.x + (float)Math.sin(this.tail2.yRot) * 10.0f;
        this.spike3.yRot = this.tail3.yRot;
        this.spike3.z = this.tail3.z;
        this.spike3.x = this.tail3.x;
        this.tail4.yRot = angle * 1.25f;
        this.tail4.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 10.0f;
        this.tail4.x = this.tail3.x + (float)Math.sin(this.tail3.yRot) * 10.0f;
        this.spike4.yRot = this.tail4.yRot + 0.52f;
        this.spike4.z = this.tail4.z;
        this.spike4.x = this.tail4.x;
        this.spike5.yRot = this.tail4.yRot - 0.52f;
        this.spike5.z = this.tail4.z;
        this.spike5.x = this.tail4.x;
        this.tail5.yRot = angle * 1.5f;
        this.tail5.z = this.tail4.z + (float)Math.cos(this.tail4.yRot) * 10.0f;
        this.tail5.x = this.tail4.x + (float)Math.sin(this.tail4.yRot) * 10.0f;
    }

    private void doLeftClaw(float angle) {
        this.arml1.yRot = -0.52f + Math.abs(angle * 2.0f);
        this.arml2.z = this.arml1.z - (float)Math.sin(this.arml1.yRot) * 9.0f;
        this.arml2.x = this.arml1.x + (float)Math.cos(this.arml1.yRot) * 9.0f;
        this.arml2.yRot = 0.855f + Math.abs(angle);
        this.clawl1.z = this.arml2.z - (float)Math.sin(this.arml2.yRot) * 14.0f;
        this.clawl1.x = this.arml2.x + (float)Math.cos(this.arml2.yRot) * 14.0f;
        this.clawl1.yRot = 2.7f + Math.abs(angle * 4.0f);
        this.clawl2.z = this.clawl1.z;
        this.clawl2.x = this.clawl1.x;
        this.clawl2.yRot = 2.27f + Math.abs(angle * 4.0f);
        this.clawl3.z = this.clawl1.z;
        this.clawl3.x = this.clawl1.x;
        this.clawl3.yRot = 2.7f + Math.abs(angle * 4.0f);
    }

    private void doRightClaw(float angle) {
        this.armr1.yRot = -2.61f - Math.abs(angle * 2.0f);
        this.armr2.z = this.armr1.z - (float)Math.sin(this.armr1.yRot) * 9.0f;
        this.armr2.x = this.armr1.x + (float)Math.cos(this.armr1.yRot) * 9.0f;
        this.armr2.yRot = 2.27f - Math.abs(angle);
        this.clawr1.z = this.armr2.z - (float)Math.sin(this.armr2.yRot) * 14.0f;
        this.clawr1.x = this.armr2.x + (float)Math.cos(this.armr2.yRot) * 14.0f;
        this.clawr1.yRot = 0.436f - Math.abs(angle * 4.0f);
        this.clawr2.z = this.clawr1.z;
        this.clawr2.x = this.clawr1.x;
        this.clawr2.yRot = 0.87f - Math.abs(angle * 4.0f);
        this.clawr3.z = this.clawr1.z;
        this.clawr3.x = this.clawr1.x;
        this.clawr3.yRot = 0.436f - Math.abs(angle * 4.0f);
    }
}
