package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityCaterKiller;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class CaterKillerModel extends EntityModel<EntityCaterKiller> {
    private static final float WINGSPEED = 0.22f;

    private final ModelPart Head;
    private final ModelPart falsehead;
    private final ModelPart ltusk1;
    private final ModelPart ltusk2;
    private final ModelPart rtusk1;
    private final ModelPart rtusk2;
    private final ModelPart ljaw;
    private final ModelPart rjaw;

    private final ModelPart[] seg1 = new ModelPart[3];
    private final ModelPart[] seg1lspike = new ModelPart[3];
    private final ModelPart[] seg1rspike = new ModelPart[3];
    private final ModelPart[] seg1ltopspike = new ModelPart[3];
    private final ModelPart[] seg1rtopspike = new ModelPart[3];
    private final ModelPart[] seg1lleg = new ModelPart[3];
    private final ModelPart[] seg1rleg = new ModelPart[3];

    private final ModelPart[] seg2 = new ModelPart[6];
    private final ModelPart[] seg2lfoot = new ModelPart[6];
    private final ModelPart[] seg2rfoot = new ModelPart[6];
    private final ModelPart[] seg2ltopspike = new ModelPart[6];
    private final ModelPart[] seg2rtopspike = new ModelPart[6];
    private final ModelPart[] seg2lspike = new ModelPart[6];
    private final ModelPart[] seg2rspike = new ModelPart[6];

    private final ModelPart seg3;
    private final ModelPart seg3lfoot;
    private final ModelPart seg3rfoot;
    private final ModelPart seg3lspike;
    private final ModelPart seg3rspike;
    private final ModelPart seg3ltopspike;
    private final ModelPart seg3rtopspike;
    private final ModelPart seg3lbackspike;
    private final ModelPart seg3rbackspike;

    public CaterKillerModel(ModelPart root) {
        this.Head = root.getChild("Head");
        this.falsehead = root.getChild("falsehead");
        this.ltusk1 = root.getChild("ltusk1");
        this.ltusk2 = root.getChild("ltusk2");
        this.rtusk1 = root.getChild("rtusk1");
        this.rtusk2 = root.getChild("rtusk2");
        this.ljaw = root.getChild("ljaw");
        this.rjaw = root.getChild("rjaw");

        for (int i = 0; i < 3; i++) {
            this.seg1[i] = root.getChild("seg1_" + i);
            this.seg1lspike[i] = root.getChild("seg1lspike_" + i);
            this.seg1rspike[i] = root.getChild("seg1rspike_" + i);
            this.seg1ltopspike[i] = root.getChild("seg1ltopspike_" + i);
            this.seg1rtopspike[i] = root.getChild("seg1rtopspike_" + i);
            this.seg1lleg[i] = root.getChild("seg1lleg_" + i);
            this.seg1rleg[i] = root.getChild("seg1rleg_" + i);
        }

        for (int i = 0; i < 6; i++) {
            this.seg2[i] = root.getChild("seg2_" + i);
            this.seg2lfoot[i] = root.getChild("seg2lfoot_" + i);
            this.seg2rfoot[i] = root.getChild("seg2rfoot_" + i);
            this.seg2ltopspike[i] = root.getChild("seg2ltopspike_" + i);
            this.seg2rtopspike[i] = root.getChild("seg2rtopspike_" + i);
            this.seg2lspike[i] = root.getChild("seg2lspike_" + i);
            this.seg2rspike[i] = root.getChild("seg2rspike_" + i);
        }

        this.seg3 = root.getChild("seg3");
        this.seg3lfoot = root.getChild("seg3lfoot");
        this.seg3rfoot = root.getChild("seg3rfoot");
        this.seg3lspike = root.getChild("seg3lspike");
        this.seg3rspike = root.getChild("seg3rspike");
        this.seg3ltopspike = root.getChild("seg3ltopspike");
        this.seg3rtopspike = root.getChild("seg3rtopspike");
        this.seg3lbackspike = root.getChild("seg3lbackspike");
        this.seg3rbackspike = root.getChild("seg3rbackspike");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 50).mirror()
                        .addBox(-8.0F, -8.0F, -8.0F, 16, 16, 8),
                PartPose.offset(0.0F, -8.0F, -12.0F));

        partdefinition.addOrReplaceChild("falsehead",
                CubeListBuilder.create().texOffs(0, 100).mirror()
                        .addBox(-10.0F, -27.0F, -11.0F, 20, 20, 10),
                PartPose.offsetAndRotation(0.0F, -8.0F, -12.0F, -0.1570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ltusk1",
                CubeListBuilder.create().texOffs(0, 140).mirror()
                        .addBox(-1.0F, -1.0F, -1.0F, 33, 3, 3),
                PartPose.offsetAndRotation(9.0F, -25.0F, -19.0F, 0.0F, 0.5585054F, 0.0F));

        partdefinition.addOrReplaceChild("ltusk2",
                CubeListBuilder.create().texOffs(0, 160).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 20, 1, 1),
                PartPose.offsetAndRotation(36.0F, -25.0F, -36.0F, 0.0F, 0.8028515F, 0.0F));

        partdefinition.addOrReplaceChild("rtusk1",
                CubeListBuilder.create().texOffs(0, 150).mirror()
                        .addBox(-33.0F, 0.0F, 0.0F, 33, 3, 3),
                PartPose.offsetAndRotation(-8.0F, -25.0F, -17.0F, 0.0F, -0.5585054F, 0.0F));

        partdefinition.addOrReplaceChild("rtusk2",
                CubeListBuilder.create().texOffs(0, 170).mirror()
                        .addBox(-20.0F, 0.0F, 0.0F, 20, 1, 1),
                PartPose.offsetAndRotation(-36.0F, -24.0F, -34.0F, 0.0F, -0.8028515F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw",
                CubeListBuilder.create().texOffs(100, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 4),
                PartPose.offsetAndRotation(4.0F, -1.0F, -18.0F, 0.0F, 0.0F, 0.1396263F));

        partdefinition.addOrReplaceChild("rjaw",
                CubeListBuilder.create().texOffs(125, 50).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 1, 7, 4),
                PartPose.offsetAndRotation(-5.0F, -1.0F, -18.0F, 0.0F, 0.0F, -0.1396263F));

        for (int i = 0; i < 3; i++) {
            partdefinition.addOrReplaceChild("seg1_" + i,
                    CubeListBuilder.create().texOffs(0, 200).mirror()
                            .addBox(-14.0F, -31.0F, 0.0F, 28, 32, 14),
                    PartPose.offset(0.0F, -8.0F, -12.0F));

            partdefinition.addOrReplaceChild("seg1lspike_" + i,
                    CubeListBuilder.create().texOffs(0, 260).mirror()
                            .addBox(-1.0F, -1.0F, -1.0F, 33, 2, 2),
                    PartPose.offsetAndRotation(14.0F, -32.0F, -6.0F, 0.0F, 0.3316126F, -0.122173F));

            partdefinition.addOrReplaceChild("seg1rspike_" + i,
                    CubeListBuilder.create().texOffs(0, 270).mirror()
                            .addBox(-33.0F, -1.0F, -1.0F, 33, 2, 2),
                    PartPose.offsetAndRotation(-13.0F, -32.0F, -6.0F, 0.0F, -0.3316126F, 0.122173F));

            partdefinition.addOrReplaceChild("seg1ltopspike_" + i,
                    CubeListBuilder.create().texOffs(125, 260).mirror()
                            .addBox(-2.0F, -8.0F, -2.0F, 4, 9, 4),
                    PartPose.offsetAndRotation(8.0F, -39.0F, -6.0F, 0.0F, 0.0F, 0.1396263F));

            partdefinition.addOrReplaceChild("seg1rtopspike_" + i,
                    CubeListBuilder.create().texOffs(150, 260).mirror()
                            .addBox(-2.0F, -8.0F, -2.0F, 4, 9, 4),
                    PartPose.offsetAndRotation(-10.0F, -39.0F, -6.0F, 0.0F, 0.0F, -0.1396263F));

            partdefinition.addOrReplaceChild("seg1lleg_" + i,
                    CubeListBuilder.create().texOffs(125, 200).mirror()
                            .addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2),
                    PartPose.offsetAndRotation(8.0F, -8.0F, -5.0F, 0.0F, 0.0F, 0.1570796F));

            partdefinition.addOrReplaceChild("seg1rleg_" + i,
                    CubeListBuilder.create().texOffs(150, 200).mirror()
                            .addBox(0.0F, 0.0F, 0.0F, 2, 16, 2),
                    PartPose.offsetAndRotation(-9.0F, -8.0F, -5.0F, 0.0F, 0.0F, -0.1570796F));
        }

        for (int i = 0; i < 6; i++) {
            partdefinition.addOrReplaceChild("seg2_" + i,
                    CubeListBuilder.create().texOffs(0, 300).mirror()
                            .addBox(-20.0F, -17.0F, -9.0F, 40, 34, 18),
                    PartPose.offset(0.0F, -2.0F, 32.0F));

            partdefinition.addOrReplaceChild("seg2lfoot_" + i,
                    CubeListBuilder.create().texOffs(125, 300).mirror()
                            .addBox(-5.0F, 0.0F, -5.0F, 10, 10, 10),
                    PartPose.offset(13.0F, 14.0F, 32.0F));

            partdefinition.addOrReplaceChild("seg2rfoot_" + i,
                    CubeListBuilder.create().texOffs(175, 300).mirror()
                            .addBox(-5.0F, 0.0F, -5.0F, 10, 10, 10),
                    PartPose.offset(-13.0F, 14.0F, 32.0F));

            partdefinition.addOrReplaceChild("seg2ltopspike_" + i,
                    CubeListBuilder.create().texOffs(100, 360).mirror()
                            .addBox(-2.0F, -9.0F, -2.0F, 4, 9, 4),
                    PartPose.offsetAndRotation(14.0F, -18.0F, 32.0F, 0.0F, 0.0F, 0.1396263F));

            partdefinition.addOrReplaceChild("seg2rtopspike_" + i,
                    CubeListBuilder.create().texOffs(125, 360).mirror()
                            .addBox(-2.0F, -9.0F, -2.0F, 4, 9, 4),
                    PartPose.offsetAndRotation(-14.0F, -18.0F, 32.0F, 0.0F, 0.0F, -0.1396263F));

            partdefinition.addOrReplaceChild("seg2lspike_" + i,
                    CubeListBuilder.create().texOffs(0, 360).mirror()
                            .addBox(0.0F, -1.0F, -1.0F, 20, 2, 2),
                    PartPose.offsetAndRotation(18.0F, -9.0F, 32.0F, 0.0F, 0.0F, -0.0698132F));

            partdefinition.addOrReplaceChild("seg2rspike_" + i,
                    CubeListBuilder.create().texOffs(0, 370).mirror()
                            .addBox(-20.0F, -1.0F, -1.0F, 20, 2, 2),
                    PartPose.offsetAndRotation(-18.0F, -9.0F, 32.0F, 0.0F, 0.0F, 0.0698132F));
        }

        partdefinition.addOrReplaceChild("seg3",
                CubeListBuilder.create().texOffs(0, 400).mirror()
                        .addBox(-15.0F, -14.0F, -7.0F, 30, 28, 14),
                PartPose.offset(0.0F, 3.0F, 48.0F));

        partdefinition.addOrReplaceChild("seg3lfoot",
                CubeListBuilder.create().texOffs(100, 400).mirror()
                        .addBox(-4.0F, 0.0F, -6.0F, 8, 8, 12),
                PartPose.offset(10.0F, 16.0F, 48.0F));

        partdefinition.addOrReplaceChild("seg3rfoot",
                CubeListBuilder.create().texOffs(150, 400).mirror()
                        .addBox(-4.0F, 0.0F, -6.0F, 8, 8, 12),
                PartPose.offset(-10.0F, 16.0F, 48.0F));

        partdefinition.addOrReplaceChild("seg3lspike",
                CubeListBuilder.create().texOffs(0, 450).mirror()
                        .addBox(0.0F, -1.0F, -1.0F, 14, 2, 2),
                PartPose.offsetAndRotation(14.0F, -4.0F, 48.0F, 0.0F, 0.0F, -0.0698132F));

        partdefinition.addOrReplaceChild("seg3rspike",
                CubeListBuilder.create().texOffs(0, 460).mirror()
                        .addBox(-14.0F, -1.0F, -1.0F, 14, 2, 2),
                PartPose.offsetAndRotation(-14.0F, -4.0F, 48.0F, 0.0F, 0.0F, 0.0698132F));

        partdefinition.addOrReplaceChild("seg3ltopspike",
                CubeListBuilder.create().texOffs(100, 450).mirror()
                        .addBox(-2.0F, -13.0F, -2.0F, 3, 13, 3),
                PartPose.offsetAndRotation(10.0F, -10.0F, 48.0F, 0.0F, 0.0F, 0.1396263F));

        partdefinition.addOrReplaceChild("seg3rtopspike",
                CubeListBuilder.create().texOffs(120, 450).mirror()
                        .addBox(-2.0F, -13.0F, -2.0F, 3, 13, 3),
                PartPose.offsetAndRotation(-10.0F, -10.0F, 48.0F, 0.0F, 0.0F, -0.1396263F));

        partdefinition.addOrReplaceChild("seg3lbackspike",
                CubeListBuilder.create().texOffs(50, 450).mirror()
                        .addBox(-2.0F, -20.0F, -2.0F, 4, 20, 4),
                PartPose.offsetAndRotation(13.0F, -8.0F, 54.0F, -0.9773844F, 0.2792527F, 0.1396263F));

        partdefinition.addOrReplaceChild("seg3rbackspike",
                CubeListBuilder.create().texOffs(75, 450).mirror()
                        .addBox(-2.0F, -20.0F, -2.0F, 4, 20, 4),
                PartPose.offsetAndRotation(-13.0F, -8.0F, 54.0F, -0.9773844F, -0.3490659F, 0.1396263F));

        return LayerDefinition.create(meshdefinition, 256, 512);
    }

    @Override
    public void setupAnim(EntityCaterKiller entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean attacking = entity.getAttacking() != 0;

        float jawAngle = attacking
                ? Mth.cos(ageInTicks * 1.7f * WINGSPEED) * (float) Math.PI * 0.07f
                : Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.025f;
        this.ljaw.zRot = 0.139f + jawAngle;
        this.rjaw.zRot = -0.139f - jawAngle;

        float headoff = attacking
                ? Mth.cos(ageInTicks * 1.7f * WINGSPEED) * 8.0f
                : Mth.cos(ageInTicks * 0.3f * WINGSPEED) * 2.0f;

        this.Head.y = -8.0f + headoff;
        this.falsehead.y = -8.0f + headoff;
        this.ltusk1.y = -25.0f + headoff;
        this.ltusk2.y = -25.0f + headoff;
        this.rtusk1.y = -25.0f + headoff;
        this.rtusk2.y = -25.0f + headoff;
        this.ljaw.y = -1.0f + headoff;
        this.rjaw.y = -1.0f + headoff;

        float tuskAngle = Mth.cos(ageInTicks * 2.11f * WINGSPEED) * (float) Math.PI * 0.08f;
        this.ltusk2.yRot = 0.802f + tuskAngle;
        tuskAngle = Mth.cos(ageInTicks * 2.3f * WINGSPEED) * (float) Math.PI * 0.08f;
        this.rtusk2.yRot = -0.802f + tuskAngle;

        for (int i = 0; i < 3; i++) {
            float yOff = headoff / (float) (i + 1) + 8.0f * i;

            this.seg1[i].y = -8.0f + yOff;
            this.seg1lspike[i].y = -32.0f + yOff;
            this.seg1rspike[i].y = -32.0f + yOff;
            this.seg1ltopspike[i].y = -39.0f + yOff;
            this.seg1rtopspike[i].y = -39.0f + yOff;
            this.seg1lleg[i].y = -8.0f + yOff;
            this.seg1rleg[i].y = -8.0f + yOff;

            this.seg1[i].z = -12.0f + 14.0f * i;
            this.seg1lspike[i].z = -6.0f + 14.0f * i;
            this.seg1rspike[i].z = -6.0f + 14.0f * i;
            this.seg1ltopspike[i].z = -6.0f + 14.0f * i;
            this.seg1rtopspike[i].z = -6.0f + 14.0f * i;
            this.seg1lleg[i].z = -5.0f + 14.0f * i;
            this.seg1rleg[i].z = -5.0f + 14.0f * i;

            float spikeAngle = Mth.cos((float) (ageInTicks * 0.91f * WINGSPEED + Math.PI / 8.0 * i)) * (float) Math.PI * 0.08f;
            this.seg1lspike[i].zRot = spikeAngle;
            this.seg1rspike[i].zRot = -spikeAngle;

            float legAngle = attacking
                    ? Mth.cos((float) (ageInTicks * 2.91f * WINGSPEED + Math.PI / 8.0 * i)) * (float) Math.PI * 0.15f
                    : Mth.cos((float) (ageInTicks * 0.35f * WINGSPEED + Math.PI / 8.0 * i)) * (float) Math.PI * 0.04f;
            this.seg1lleg[i].xRot = legAngle;
            this.seg1rleg[i].xRot = -legAngle;
        }

        for (int i = 0; i < 6; i++) {
            float zdist = Mth.cos(ageInTicks * 1.7f * WINGSPEED + (float) (Math.PI / 4.0) * i) * 1.5f * limbSwingAmount;
            float segZ = 39.0f + (16.0f + zdist) * i;

            this.seg2[i].z = segZ;
            this.seg2lfoot[i].z = segZ;
            this.seg2rfoot[i].z = segZ;
            this.seg2ltopspike[i].z = segZ;
            this.seg2rtopspike[i].z = segZ;
            this.seg2lspike[i].z = segZ;
            this.seg2rspike[i].z = segZ;

            float seg2SpikeAngle = Mth.cos((float) (ageInTicks * 0.4f * WINGSPEED - Math.PI / 8.0 * i)) * (float) Math.PI * 0.07f;
            this.seg2lspike[i].zRot = seg2SpikeAngle;
            this.seg2rspike[i].zRot = -seg2SpikeAngle;
        }

        float seg3Z = this.seg2rspike[5].z + 16.0f;
        this.seg3.z = seg3Z;
        this.seg3lfoot.z = seg3Z;
        this.seg3rfoot.z = seg3Z;
        this.seg3lspike.z = seg3Z;
        this.seg3rspike.z = seg3Z;
        this.seg3ltopspike.z = seg3Z;
        this.seg3rtopspike.z = seg3Z;
        this.seg3lbackspike.z = seg3Z + 6.0f;
        this.seg3rbackspike.z = seg3Z + 6.0f;

        float seg3SpikeAngle = Mth.cos((float) (ageInTicks * 0.4f * WINGSPEED - Math.PI / 8.0 * 6)) * (float) Math.PI * 0.07f;
        this.seg3lspike.zRot = seg3SpikeAngle;
        this.seg3rspike.zRot = -seg3SpikeAngle;

        float backAngle = Mth.cos(ageInTicks * 0.81f * WINGSPEED) * (float) Math.PI * 0.04f;
        this.seg3lbackspike.xRot = -0.977f + backAngle;
        backAngle = Mth.cos(ageInTicks * 0.87f * WINGSPEED) * (float) Math.PI * 0.04f;
        this.seg3rbackspike.xRot = -0.977f + backAngle;

        backAngle = Mth.cos(ageInTicks * 1.11f * WINGSPEED) * (float) Math.PI * 0.04f;
        this.seg3lbackspike.yRot = 0.28f + backAngle;
        backAngle = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.04f;
        this.seg3rbackspike.yRot = -0.28f + backAngle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.falsehead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltusk1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltusk2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtusk1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtusk2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ljaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rjaw.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        for (int i = 0; i < 3; i++) {
            this.seg1[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg1lspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg1rspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg1ltopspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg1rtopspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg1lleg[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg1rleg[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        for (int i = 0; i < 6; i++) {
            this.seg2[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg2lfoot[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg2rfoot[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg2ltopspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg2rtopspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg2lspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            this.seg2rspike[i].render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }

        this.seg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3lfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3rfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3lspike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3rspike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3ltopspike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3rtopspike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3lbackspike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.seg3rbackspike.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
