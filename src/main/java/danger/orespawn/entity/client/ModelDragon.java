package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Dragon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelDragon extends EntityModel<Dragon> {
    private static final float ANIM_SPEED = 1.0F;

    private final ModelPart horn1;
    private final ModelPart horn2;
    private final ModelPart tail6;
    private final ModelPart wing15;
    private final ModelPart spike1;
    private final ModelPart spike2;
    private final ModelPart spike3;
    private final ModelPart spike4;
    private final ModelPart wing14;
    private final ModelPart spike5;
    private final ModelPart spike6;
    private final ModelPart spike7;
    private final ModelPart spike8;
    private final ModelPart spike9;
    private final ModelPart spike10;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart body2;
    private final ModelPart neck1;
    private final ModelPart body3;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart leg7;
    private final ModelPart leg9;
    private final ModelPart foot1;
    private final ModelPart foot2;
    private final ModelPart leg10;
    private final ModelPart leg11;
    private final ModelPart foot3;
    private final ModelPart foot4;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart mouth1;
    private final ModelPart mouth2;
    private final ModelPart tail5;
    private final ModelPart wing1;
    private final ModelPart wing2;
    private final ModelPart wing3;
    private final ModelPart wing4;
    private final ModelPart wing5;
    private final ModelPart wing6;
    private final ModelPart wing7;
    private final ModelPart wing8;
    private final ModelPart wing9;
    private final ModelPart wing10;
    private final ModelPart wing11;
    private final ModelPart wing12;
    private final ModelPart tail4;

    public ModelDragon(ModelPart root) {
        this.horn1 = root.getChild("horn1");
        this.horn2 = root.getChild("horn2");
        this.tail6 = root.getChild("tail6");
        this.wing15 = root.getChild("wing15");
        this.spike1 = root.getChild("spike1");
        this.spike2 = root.getChild("spike2");
        this.spike3 = root.getChild("spike3");
        this.spike4 = root.getChild("spike4");
        this.wing14 = root.getChild("wing14");
        this.spike5 = root.getChild("spike5");
        this.spike6 = root.getChild("spike6");
        this.spike7 = root.getChild("spike7");
        this.spike8 = root.getChild("spike8");
        this.spike9 = root.getChild("spike9");
        this.spike10 = root.getChild("spike10");
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.body2 = root.getChild("body2");
        this.neck1 = root.getChild("neck1");
        this.body3 = root.getChild("body3");
        this.neck2 = root.getChild("neck2");
        this.neck3 = root.getChild("neck3");
        this.leg5 = root.getChild("leg5");
        this.leg6 = root.getChild("leg6");
        this.leg7 = root.getChild("leg7");
        this.leg9 = root.getChild("leg9");
        this.foot1 = root.getChild("foot1");
        this.foot2 = root.getChild("foot2");
        this.leg10 = root.getChild("leg10");
        this.leg11 = root.getChild("leg11");
        this.foot3 = root.getChild("foot3");
        this.foot4 = root.getChild("foot4");
        this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.mouth1 = root.getChild("mouth1");
        this.mouth2 = root.getChild("mouth2");
        this.tail5 = root.getChild("tail5");
        this.wing1 = root.getChild("wing1");
        this.wing2 = root.getChild("wing2");
        this.wing3 = root.getChild("wing3");
        this.wing4 = root.getChild("wing4");
        this.wing5 = root.getChild("wing5");
        this.wing6 = root.getChild("wing6");
        this.wing7 = root.getChild("wing7");
        this.wing8 = root.getChild("wing8");
        this.wing9 = root.getChild("wing9");
        this.wing10 = root.getChild("wing10");
        this.wing11 = root.getChild("wing11");
        this.wing12 = root.getChild("wing12");
        this.tail4 = root.getChild("tail4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("horn1",
                CubeListBuilder.create().texOffs(0, 39).mirror()
                        .addBox(2.0F, -4.0F, 1.0F, 2, 2, 6),
                PartPose.offsetAndRotation(0.0F, 6.0F, -23.0F, 0.4089647F, 0.2602503F, 0.0F));

        partdefinition.addOrReplaceChild("horn2",
                CubeListBuilder.create().texOffs(0, 39).mirror()
                        .addBox(-4.0F, -4.0F, 1.0F, 2, 2, 6),
                PartPose.offsetAndRotation(0.0F, 6.0F, -23.0F, 0.4089647F, -0.2602503F, 0.0F));

        partdefinition.addOrReplaceChild("tail6",
                CubeListBuilder.create().texOffs(0, 49).mirror()
                        .addBox(-1.0F, 0.0F, -2.0F, 2, 6, 4),
                PartPose.offsetAndRotation(0.0F, 7.0F, 43.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("wing15",
                CubeListBuilder.create().texOffs(0, 62).mirror()
                        .addBox(1.0F, -1.0F, 1.0F, 12, 1, 8),
                PartPose.offsetAndRotation(4.0F, 3.0F, -5.0F, -0.0743572F, -0.4089594F, 0.0F));

        partdefinition.addOrReplaceChild("spike1",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -3.0F, -5.0F, 2, 2, 4),
                PartPose.offset(0.0F, 6.0F, -17.0F));

        partdefinition.addOrReplaceChild("spike2",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -3.0F, -5.0F, 2, 2, 4),
                PartPose.offset(0.0F, 6.0F, -11.0F));

        partdefinition.addOrReplaceChild("spike3",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -4.0F, 1.0F, 2, 2, 4),
                PartPose.offset(0.0F, 7.0F, 25.0F));

        partdefinition.addOrReplaceChild("spike4",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -2.0F, 0.0F, 2, 2, 4),
                PartPose.offset(0.0F, 3.0F, -7.0F));

        partdefinition.addOrReplaceChild("wing14",
                CubeListBuilder.create().texOffs(0, 62).mirror()
                        .addBox(-13.0F, -1.0F, 0.0F, 12, 1, 8),
                PartPose.offsetAndRotation(-4.0F, 3.0F, -5.0F, -0.0698132F, 0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("spike5",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -2.0F, 0.0F, 2, 2, 4),
                PartPose.offset(0.0F, 3.0F, -1.0F));

        partdefinition.addOrReplaceChild("spike6",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -2.0F, 0.0F, 2, 2, 4),
                PartPose.offset(0.0F, 3.0F, 5.0F));

        partdefinition.addOrReplaceChild("spike7",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -4.0F, 1.0F, 2, 2, 4),
                PartPose.offset(0.0F, 7.0F, 13.0F));

        partdefinition.addOrReplaceChild("spike8",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -2.0F, 1.0F, 2, 2, 4),
                PartPose.offset(0.0F, 5.0F, 19.0F));

        partdefinition.addOrReplaceChild("spike9",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -4.0F, 1.0F, 2, 2, 4),
                PartPose.offset(0.0F, 7.0F, 31.0F));

        partdefinition.addOrReplaceChild("spike10",
                CubeListBuilder.create().texOffs(0, 73).mirror()
                        .addBox(-1.0F, -4.0F, 2.0F, 2, 2, 4),
                PartPose.offset(0.0F, 7.0F, 36.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 81).mirror()
                        .addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8),
                PartPose.offset(0.0F, 6.0F, -23.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(1, 99).mirror()
                        .addBox(-6.0F, -10.0F, -7.0F, 12, 18, 10),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(47, 112).mirror()
                        .addBox(-4.0F, -1.0F, -2.0F, 4, 9, 5),
                PartPose.offsetAndRotation(-4.0F, 11.0F, 8.0F, -0.6320364F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(47, 112).mirror()
                        .addBox(1.0F, -1.0F, -2.0F, 4, 9, 5),
                PartPose.offsetAndRotation(3.0F, 11.0F, 8.0F, -0.6320364F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(18, 47).mirror()
                        .addBox(-3.0F, -2.0F, -2.0F, 4, 9, 4),
                PartPose.offsetAndRotation(-4.0F, 11.0F, -5.0F, 0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg4",
                CubeListBuilder.create().texOffs(18, 47).mirror()
                        .addBox(0.0F, -2.0F, -2.0F, 4, 9, 4),
                PartPose.offsetAndRotation(3.0F, 11.0F, -5.0F, 0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create().texOffs(68, 94).mirror()
                        .addBox(-5.0F, 0.0F, 0.0F, 10, 22, 10),
                PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck1",
                CubeListBuilder.create().texOffs(43, 85).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 6),
                PartPose.offset(0.0F, 7.0F, 25.0F));

        partdefinition.addOrReplaceChild("body3",
                CubeListBuilder.create().texOffs(70, 59).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 24, 8),
                PartPose.offsetAndRotation(0.0F, 11.0F, -11.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(43, 85).mirror()
                        .addBox(-3.0F, -2.0F, -6.0F, 6, 6, 6),
                PartPose.offset(0.0F, 6.0F, -11.0F));

        partdefinition.addOrReplaceChild("neck3",
                CubeListBuilder.create().texOffs(43, 85).mirror()
                        .addBox(-3.0F, -2.0F, -6.0F, 6, 6, 6),
                PartPose.offset(0.0F, 6.0F, -17.0F));

        partdefinition.addOrReplaceChild("leg5",
                CubeListBuilder.create().texOffs(47, 99).mirror()
                        .addBox(0.0F, 3.0F, 3.0F, 4, 8, 4),
                PartPose.offsetAndRotation(3.0F, 11.0F, -5.0F, -0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg6",
                CubeListBuilder.create().texOffs(47, 99).mirror()
                        .addBox(-3.0F, 3.0F, 3.0F, 4, 8, 4),
                PartPose.offsetAndRotation(-4.0F, 11.0F, -5.0F, -0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg7",
                CubeListBuilder.create().texOffs(38, 73).mirror()
                        .addBox(1.0F, 2.0F, -8.0F, 4, 5, 4),
                PartPose.offsetAndRotation(3.0F, 11.0F, 8.0F, 0.8922867F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg9",
                CubeListBuilder.create().texOffs(38, 73).mirror()
                        .addBox(-4.0F, 2.0F, -8.0F, 4, 5, 4),
                PartPose.offsetAndRotation(-4.0F, 11.0F, 8.0F, 0.8922867F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("foot1",
                CubeListBuilder.create().texOffs(43, 63).mirror()
                        .addBox(-3.0F, 11.0F, -5.0F, 4, 2, 6),
                PartPose.offset(-4.0F, 11.0F, -5.0F));

        partdefinition.addOrReplaceChild("foot2",
                CubeListBuilder.create().texOffs(43, 63).mirror()
                        .addBox(0.0F, 11.0F, -5.0F, 4, 2, 6),
                PartPose.offset(3.0F, 11.0F, -5.0F));

        partdefinition.addOrReplaceChild("leg10",
                CubeListBuilder.create().texOffs(39, 52).mirror()
                        .addBox(1.0F, 6.0F, 2.0F, 4, 5, 4),
                PartPose.offsetAndRotation(3.0F, 11.0F, 8.0F, -0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg11",
                CubeListBuilder.create().texOffs(39, 52).mirror()
                        .addBox(-4.0F, 6.0F, 2.0F, 4, 5, 4),
                PartPose.offsetAndRotation(-4.0F, 11.0F, 8.0F, -0.5576792F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("foot3",
                CubeListBuilder.create().texOffs(43, 63).mirror()
                        .addBox(1.0F, 11.0F, -7.0F, 4, 2, 6),
                PartPose.offset(3.0F, 11.0F, 8.0F));

        partdefinition.addOrReplaceChild("foot4",
                CubeListBuilder.create().texOffs(43, 63).mirror()
                        .addBox(-4.0F, 11.0F, -7.0F, 4, 2, 6),
                PartPose.offset(-4.0F, 11.0F, 8.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(43, 85).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 6),
                PartPose.offset(0.0F, 7.0F, 13.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(43, 85).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 6),
                PartPose.offset(0.0F, 7.0F, 19.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(56, 45).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 4, 6, 6),
                PartPose.offset(1.0F, 7.0F, 31.0F));

        partdefinition.addOrReplaceChild("mouth1",
                CubeListBuilder.create().texOffs(90, 22).mirror()
                        .addBox(-3.0F, -1.0F, -15.0F, 6, 3, 8),
                PartPose.offset(0.0F, 6.0F, -23.0F));

        partdefinition.addOrReplaceChild("mouth2",
                CubeListBuilder.create().texOffs(90, 6).mirror()
                        .addBox(-2.0F, 1.0F, -5.0F, 4, 2, 8),
                PartPose.offsetAndRotation(0.0F, 7.0F, -32.0F, 0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(87, 36).mirror()
                        .addBox(0.0F, 0.0F, -5.0F, 0, 11, 10),
                PartPose.offsetAndRotation(0.0F, 7.0F, 49.0F, 1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("wing1",
                CubeListBuilder.create().texOffs(26, 40).mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 11, 2, 2),
                PartPose.offsetAndRotation(4.0F, 3.0F, -5.0F, 0.0F, -0.4833219F, 0.0F));

        partdefinition.addOrReplaceChild("wing2",
                CubeListBuilder.create().texOffs(110, 88).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 36),
                PartPose.offsetAndRotation(19.0F, 3.0F, -23.0F, 0.0F, 1.041001F, 0.0F));

        partdefinition.addOrReplaceChild("wing3",
                CubeListBuilder.create().texOffs(109, 60).mirror()
                        .addBox(-1.0F, -1.0F, -24.0F, 2, 2, 24),
                PartPose.offsetAndRotation(12.0F, 3.0F, -1.0F, -0.0090881F, -0.3497888F, 0.0F));

        partdefinition.addOrReplaceChild("wing4",
                CubeListBuilder.create().texOffs(26, 40).mirror()
                        .addBox(-11.0F, -1.0F, -1.0F, 11, 2, 2),
                PartPose.offsetAndRotation(-4.0F, 3.0F, -5.0F, 0.0F, 0.4833166F, 0.0F));

        partdefinition.addOrReplaceChild("wing5",
                CubeListBuilder.create().texOffs(109, 60).mirror()
                        .addBox(-1.0F, -1.0F, -24.0F, 2, 2, 24),
                PartPose.offsetAndRotation(-12.0F, 3.0F, -1.0F, -0.0090932F, 0.3323281F, 0.0F));

        partdefinition.addOrReplaceChild("wing6",
                CubeListBuilder.create().texOffs(110, 88).mirror()
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 36),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -23.0F, 0.0F, -1.041002F, 0.0F));

        partdefinition.addOrReplaceChild("wing7",
                CubeListBuilder.create().texOffs(124, 21).mirror()
                        .addBox(-8.0F, 0.0F, 1.0F, 8, 1, 36),
                PartPose.offsetAndRotation(19.0F, 2.0F, -23.0F, 0.0F, 1.041001F, 0.0F));

        partdefinition.addOrReplaceChild("wing8",
                CubeListBuilder.create().texOffs(122, 10).mirror()
                        .addBox(-11.0F, -1.0F, 0.0F, 28, 1, 8),
                PartPose.offsetAndRotation(12.0F, 3.0F, -1.0F, 0.002272F, 1.264073F, -0.0174533F));

        partdefinition.addOrReplaceChild("wing9",
                CubeListBuilder.create().texOffs(0, 10).mirror()
                        .addBox(-25.0F, -1.0F, 7.0F, 18, 1, 26),
                PartPose.offsetAndRotation(19.0F, 3.0F, -23.0F, 0.002272F, 1.264073F, 0.0F));

        partdefinition.addOrReplaceChild("wing10",
                CubeListBuilder.create().texOffs(122, 10).mirror()
                        .addBox(-23.0F, -1.0F, 0.0F, 33, 1, 8),
                PartPose.offsetAndRotation(-12.0F, 3.0F, -1.0F, -0.0022689F, -1.226894F, 0.0F));

        partdefinition.addOrReplaceChild("wing11",
                CubeListBuilder.create().texOffs(124, 21).mirror()
                        .addBox(0.0F, -1.0F, 1.0F, 8, 1, 36),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -23.0F, 0.0F, -1.041002F, 0.0F));

        partdefinition.addOrReplaceChild("wing12",
                CubeListBuilder.create().texOffs(0, 10).mirror()
                        .addBox(7.0F, -1.0F, 7.0F, 18, 1, 26),
                PartPose.offsetAndRotation(-20.0F, 3.0F, -23.0F, 0.002272F, -1.264072F, 0.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(56, 45).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 4, 6, 6),
                PartPose.offset(1.0F, 7.0F, 37.0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }

    @Override
    public void setupAnim(Dragon entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle;
        float lspeed = 0.0F;
        float tailspeed = 0.76F;
        float tailamp = 0.45F;

        float rf1 = 0.0f;

        if (limbSwingAmount > 0.001F) {
            lspeed = (float) ((entity.xOld - entity.getX()) * (entity.xOld - entity.getX())
                    + (entity.zOld - entity.getZ()) * (entity.zOld - entity.getZ()));
            lspeed = (float) Math.sqrt(lspeed);
            newangle = Mth.cos(ageInTicks * 1.25F * ANIM_SPEED) * (float) Math.PI * lspeed * 0.6F;
        } else {
            newangle = 0.0F;
        }

        // Leg animation
        if (entity.getActivity() != 0) {
            float a = 1.0F;
            this.leg4.xRot = 0.557F - a;
            this.leg5.xRot = -0.557F - a;
            this.foot2.xRot = -a;
            this.leg3.xRot = 0.557F - a;
            this.leg6.xRot = -0.557F - a;
            this.foot1.xRot = -a;
            this.leg2.xRot = -0.632F + a;
            this.leg7.xRot = 0.89F + a;
            this.leg10.xRot = -0.557F + a;
            this.foot3.xRot = a;
            this.leg1.xRot = -0.632F + a;
            this.leg9.xRot = 0.89F + a;
            this.leg11.xRot = -0.557F + a;
            this.foot4.xRot = a;
        } else {
            this.leg4.xRot = 0.557F + newangle;
            this.leg5.xRot = -0.557F + newangle;
            this.foot2.xRot = newangle;
            this.leg3.xRot = 0.557F - newangle;
            this.leg6.xRot = -0.557F - newangle;
            this.foot1.xRot = -newangle;
            this.leg2.xRot = -0.632F - newangle;
            this.leg7.xRot = 0.89F - newangle;
            this.leg10.xRot = -0.557F - newangle;
            this.foot3.xRot = -newangle;
            this.leg1.xRot = -0.632F + newangle;
            this.leg9.xRot = 0.89F + newangle;
            this.leg11.xRot = -0.557F + newangle;
            this.foot4.xRot = newangle;
        }

        // Wing animation
        if (entity.getAttacking() != 0) {
            newangle = entity.getActivity() != 0
                    ? Mth.cos(ageInTicks * 0.75F * ANIM_SPEED) * (float) Math.PI * 0.28F
                    : -0.45F + Mth.cos(ageInTicks * 0.85F * ANIM_SPEED) * (float) Math.PI * 0.2F;
        } else {
            newangle = entity.getActivity() != 0
                    ? Mth.cos(ageInTicks * 0.75F * ANIM_SPEED) * (float) Math.PI * 0.28F
                    : -0.85F + Mth.cos(ageInTicks * 0.2F * ANIM_SPEED) * (float) Math.PI * 0.028F;
        }

        this.wing1.zRot = newangle;
        this.wing15.zRot = newangle;
        this.wing3.zRot = newangle * 4.0F / 3.0F;
        this.wing3.y = this.wing1.y + (float) Math.sin(this.wing1.zRot) * 7.0F;
        this.wing3.x = this.wing1.x + (float) Math.cos(this.wing1.zRot) * 7.0F;
        this.wing8.zRot = newangle * 4.0F / 3.0F;
        this.wing8.y = this.wing3.y;
        this.wing8.x = this.wing3.x;
        this.wing2.zRot = newangle * 3.0F / 2.0F;
        this.wing2.y = this.wing3.y + (float) Math.sin(this.wing3.zRot) * 6.0F;
        this.wing2.x = this.wing3.x + (float) Math.cos(this.wing3.zRot) * 6.0F;
        this.wing7.zRot = newangle * 3.0F / 2.0F;
        this.wing7.y = this.wing2.y;
        this.wing7.x = this.wing2.x;
        this.wing9.zRot = newangle * 3.0F / 2.0F;
        this.wing9.y = this.wing2.y;
        this.wing9.x = this.wing2.x;

        this.wing4.zRot = -newangle;
        this.wing14.zRot = -newangle;
        this.wing5.zRot = -newangle * 4.0F / 3.0F;
        this.wing5.y = this.wing4.y - (float) Math.sin(this.wing4.zRot) * 7.0F;
        this.wing5.x = this.wing4.x - (float) Math.cos(this.wing4.zRot) * 7.0F;
        this.wing10.zRot = -newangle * 4.0F / 3.0F;
        this.wing10.y = this.wing5.y;
        this.wing10.x = this.wing5.x;
        this.wing6.zRot = -newangle * 3.0F / 2.0F;
        this.wing6.y = this.wing5.y - (float) Math.sin(this.wing5.zRot) * 6.0F;
        this.wing6.x = this.wing5.x - (float) Math.cos(this.wing5.zRot) * 6.0F;
        this.wing11.zRot = -newangle * 3.0F / 2.0F;
        this.wing11.y = this.wing6.y;
        this.wing11.x = this.wing6.x;
        this.wing12.zRot = -newangle * 3.0F / 2.0F;
        this.wing12.y = this.wing6.y;
        this.wing12.x = this.wing6.x;

        // Tail animation
        if (entity.getAttacking() != 0) {
            tailspeed = 0.96F;
            tailamp = 0.75F;
        }
        if (entity.getActivity() == 0 && entity.getAttacking() == 0) {
            tailspeed = 0.22F;
            tailamp = 0.22F;
        }
        if (entity.isPassenger()) {
            tailspeed = 0.0F;
            tailamp = 0.0F;
        }

        this.tail1.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * 0.04F;
        this.spike10.z = this.tail1.z;
        this.spike10.x = this.tail1.x;
        this.spike10.yRot = this.tail1.yRot;

        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 6.0F;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 6.0F;
        this.tail2.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.125F;
        this.spike7.z = this.tail2.z;
        this.spike7.x = this.tail2.x;
        this.spike7.yRot = this.tail2.yRot;

        this.neck1.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 6.0F;
        this.neck1.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 6.0F;
        this.neck1.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.25F;
        this.spike8.z = this.neck1.z;
        this.spike8.x = this.neck1.x;
        this.spike8.yRot = this.neck1.yRot;

        this.tail3.z = this.neck1.z + (float) Math.cos(this.neck1.yRot) * 6.0F;
        this.tail3.x = this.neck1.x + 1.0F + (float) Math.sin(this.neck1.yRot) * 6.0F;
        this.tail3.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.375F;
        this.spike3.z = this.tail3.z;
        this.spike3.x = this.tail3.x - 1.0F;
        this.spike3.yRot = this.tail3.yRot;

        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 6.0F;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 6.0F;
        this.tail4.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.5F;
        this.spike9.z = this.tail4.z;
        this.spike9.x = this.tail4.x - 1.0F;
        this.spike9.yRot = this.tail4.yRot;

        this.tail6.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 6.0F;
        this.tail6.x = this.tail4.x - 1.0F + (float) Math.sin(this.tail4.yRot) * 6.0F;
        this.tail6.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.625F;

        this.tail5.z = this.tail6.z + (float) Math.cos(this.tail6.yRot) * 6.0F;
        this.tail5.x = this.tail6.x - 0.5F + (float) Math.sin(this.tail6.yRot) * 6.0F;
        this.tail5.yRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.75F;

        // Head/neck tracking
        float headYaw = netHeadYaw;
        if (entity.getActivity() == 1) {
            headYaw = (entity.yHeadRotO - entity.yBodyRot) * 8.0F;
            headYaw = -headYaw;
            rf1 += (headYaw - rf1) / 60.0F;
            if (rf1 > 50.0F) rf1 = 50.0F;
            if (rf1 < -50.0F) rf1 = -50.0F;
            headYaw = rf1;
        } else {
            headYaw /= 2.0F;
        }

        this.spike2.yRot = (float) Math.toRadians(headYaw) * 0.25F;
        this.neck2.yRot = this.spike2.yRot;

        this.neck3.z = this.neck2.z - (float) Math.cos(this.neck2.yRot) * 6.0F;
        this.neck3.x = this.neck2.x - (float) Math.sin(this.neck2.yRot) * 6.0F;
        this.neck3.yRot = (float) Math.toRadians(headYaw) * 0.5F;
        this.spike1.z = this.neck3.z;
        this.spike1.x = this.neck3.x;
        this.spike1.yRot = this.neck3.yRot;

        this.head.z = this.neck3.z - (float) Math.cos(this.neck3.yRot) * 6.0F;
        this.head.x = this.neck3.x - (float) Math.sin(this.neck3.yRot) * 6.0F;
        this.head.yRot = (float) Math.toRadians(headYaw) * 0.75F;
        this.mouth1.z = this.head.z;
        this.mouth1.x = this.head.x;
        this.mouth1.yRot = this.head.yRot;
        this.horn1.z = this.head.z;
        this.horn1.x = this.head.x;
        this.horn1.yRot = this.head.yRot + 0.26F;
        this.horn2.z = this.head.z;
        this.horn2.x = this.head.x;
        this.horn2.yRot = this.head.yRot - 0.26F;
        this.mouth2.z = this.head.z - (float) Math.cos(this.head.yRot) * 9.0F;
        this.mouth2.x = this.head.x - (float) Math.sin(this.head.yRot) * 9.0F;
        this.mouth2.yRot = this.head.yRot;

        newangle = Mth.cos(ageInTicks * 1.5F * ANIM_SPEED) * (float) Math.PI * 0.14F;
        this.mouth2.xRot = entity.getAttacking() != 0 ? 0.4F + newangle : 0.07F;

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.horn1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.spike10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.foot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.foot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.foot3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.foot4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
