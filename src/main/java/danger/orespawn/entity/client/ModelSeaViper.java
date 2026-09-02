package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.SeaViper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelSeaViper extends EntityModel<SeaViper> {
    /** Animation frequency constant; orig ModelSeaViper.java:14,51 (wingspeed), value from orig ClientProxyOreSpawn.java:497. */
    private final float wingspeed = 0.5f;
    private final ModelPart TailTip;
    private final ModelPart Neck;
    private final ModelPart tBase;
    private final ModelPart t2;
    private final ModelPart t3;
    private final ModelPart t4;
    private final ModelPart t5;
    private final ModelPart t6;
    private final ModelPart t7;
    private final ModelPart t8;
    private final ModelPart t9;
    private final ModelPart t10;
    private final ModelPart t12;
    private final ModelPart t11;
    private final ModelPart t13;
    private final ModelPart t14;
    private final ModelPart t15;
    private final ModelPart t16;
    private final ModelPart t17;
    private final ModelPart t18;
    private final ModelPart t19;
    private final ModelPart t20;
    private final ModelPart t21;
    private final ModelPart MouthBottom;
    private final ModelPart ToungBase;
    private final ModelPart MiddleTounge;
    private final ModelPart EyeRight;
    private final ModelPart EyeLeft;
    private final ModelPart MouthTop;
    private final ModelPart Head;
    private final ModelPart FangRight;
    private final ModelPart FangLeft;
    private final ModelPart ForkRight;
    private final ModelPart ForkLeft;

    public ModelSeaViper(ModelPart root) {
        this.TailTip = root.getChild("TailTip");
        this.Neck = root.getChild("Neck");
        this.tBase = root.getChild("tBase");
        this.t2 = root.getChild("t2");
        this.t3 = root.getChild("t3");
        this.t4 = root.getChild("t4");
        this.t5 = root.getChild("t5");
        this.t6 = root.getChild("t6");
        this.t7 = root.getChild("t7");
        this.t8 = root.getChild("t8");
        this.t9 = root.getChild("t9");
        this.t10 = root.getChild("t10");
        this.t12 = root.getChild("t12");
        this.t11 = root.getChild("t11");
        this.t13 = root.getChild("t13");
        this.t14 = root.getChild("t14");
        this.t15 = root.getChild("t15");
        this.t16 = root.getChild("t16");
        this.t17 = root.getChild("t17");
        this.t18 = root.getChild("t18");
        this.t19 = root.getChild("t19");
        this.t20 = root.getChild("t20");
        this.t21 = root.getChild("t21");
        this.MouthBottom = root.getChild("MouthBottom");
        this.ToungBase = root.getChild("ToungBase");
        this.MiddleTounge = root.getChild("MiddleTounge");
        this.EyeRight = root.getChild("EyeRight");
        this.EyeLeft = root.getChild("EyeLeft");
        this.MouthTop = root.getChild("MouthTop");
        this.Head = root.getChild("Head");
        this.FangRight = root.getChild("FangRight");
        this.FangLeft = root.getChild("FangLeft");
        this.ForkRight = root.getChild("ForkRight");
        this.ForkLeft = root.getChild("ForkLeft");
    }

    public static LayerDefinition createBodyLayer() {
        // ENT-S-091 slice C: geometry regenerated from orig ModelSeaViper.java by
        // tools/reference_to_layer_definition.py, including the constructor's trailing +32 z shifts
        // (orig :258-291) the port had dropped; reference-leg proven.
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition part_TailTip = root.addOrReplaceChild("TailTip",
                CubeListBuilder.create()
                .texOffs(0, 90).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 5.0F, 10.0F),
                PartPose.offsetAndRotation(1.0F, 20.0F, 152.0F, 0.0F, -0.6981317F, 0.0F));
        PartDefinition part_Neck = root.addOrReplaceChild("Neck",
                CubeListBuilder.create()
                .texOffs(60, 60).addBox(-4.0F, -4.0F, -10.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 4.5F, -1.0F, -0.2617994F, 0.0F, 0.0F));
        PartDefinition part_tBase = root.addOrReplaceChild("tBase",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -2.0F, -0.5235988F, 0.0F, 0.0F));
        PartDefinition part_t2 = root.addOrReplaceChild("t2",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 5.0F, -1.047198F, 0.0F, 0.0F));
        PartDefinition part_t3 = root.addOrReplaceChild("t3",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 8.0F, -0.5235988F, 0.0F, 0.0F));
        PartDefinition part_t4 = root.addOrReplaceChild("t4",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 19.0F, 15.0F, -0.0872665F, 0.0F, 0.0F));
        PartDefinition part_t5 = root.addOrReplaceChild("t5",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offset(0.0F, 20.0F, 23.0F));
        PartDefinition part_t6 = root.addOrReplaceChild("t6",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, 20.0F, 31.0F, 0.0F, 0.3490659F, 0.0F));
        PartDefinition part_t7 = root.addOrReplaceChild("t7",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(2.0F, 20.0F, 38.0F, 0.0F, 0.6981317F, 0.0F));
        PartDefinition part_t8 = root.addOrReplaceChild("t8",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(7.0F, 20.0F, 44.0F, 0.0F, 0.3490659F, 0.0F));
        PartDefinition part_t9 = root.addOrReplaceChild("t9",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offset(10.0F, 20.0F, 52.0F));
        PartDefinition part_t10 = root.addOrReplaceChild("t10",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(10.0F, 20.0F, 60.0F, 0.0F, -0.3490659F, 0.0F));
        PartDefinition part_t12 = root.addOrReplaceChild("t12",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(2.0F, 20.0F, 74.0F, 0.0F, -0.6981317F, 0.0F));
        PartDefinition part_t11 = root.addOrReplaceChild("t11",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(8.0F, 20.0F, 67.0F, 0.0F, -0.6981317F, 0.0F));
        PartDefinition part_t13 = root.addOrReplaceChild("t13",
                CubeListBuilder.create()
                .texOffs(0, 31).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(-4.0F, 20.0F, 80.0F, 0.0F, -0.3490659F, 0.0F));
        PartDefinition part_t14 = root.addOrReplaceChild("t14",
                CubeListBuilder.create()
                .texOffs(0, 51).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 7.0F, 10.0F),
                PartPose.offset(-8.0F, 20.0F, 88.0F));
        PartDefinition part_t15 = root.addOrReplaceChild("t15",
                CubeListBuilder.create()
                .texOffs(0, 51).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 7.0F, 10.0F),
                PartPose.offsetAndRotation(-8.0F, 20.0F, 97.0F, 0.0F, 0.3490659F, 0.0F));
        PartDefinition part_t16 = root.addOrReplaceChild("t16",
                CubeListBuilder.create()
                .texOffs(0, 51).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 7.0F, 10.0F),
                PartPose.offsetAndRotation(-5.0F, 20.0F, 105.0F, 0.0F, 0.6981317F, 0.0F));
        PartDefinition part_t17 = root.addOrReplaceChild("t17",
                CubeListBuilder.create()
                .texOffs(0, 70).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 6.0F, 10.0F),
                PartPose.offsetAndRotation(1.0F, 20.0F, 112.0F, 0.0F, 0.6981317F, 0.0F));
        PartDefinition part_t18 = root.addOrReplaceChild("t18",
                CubeListBuilder.create()
                .texOffs(0, 70).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 6.0F, 10.0F),
                PartPose.offsetAndRotation(7.0F, 20.0F, 119.0F, 0.0F, 0.3490659F, 0.0F));
        PartDefinition part_t19 = root.addOrReplaceChild("t19",
                CubeListBuilder.create()
                .texOffs(0, 70).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 6.0F, 10.0F),
                PartPose.offset(10.0F, 20.0F, 127.0F));
        PartDefinition part_t20 = root.addOrReplaceChild("t20",
                CubeListBuilder.create()
                .texOffs(0, 90).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 5.0F, 10.0F),
                PartPose.offsetAndRotation(10.0F, 20.0F, 136.0F, 0.0F, -0.3490659F, 0.0F));
        PartDefinition part_t21 = root.addOrReplaceChild("t21",
                CubeListBuilder.create()
                .texOffs(0, 90).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 5.0F, 10.0F),
                PartPose.offsetAndRotation(7.0F, 20.0F, 145.0F, 0.0F, -0.6981317F, 0.0F));
        PartDefinition part_MouthBottom = root.addOrReplaceChild("MouthBottom",
                CubeListBuilder.create()
                .texOffs(58, 78).addBox(-4.0F, 0.0F, -12.0F, 8.0F, 2.0F, 12.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -10.0F, 0.5235988F, 0.0F, 0.0F));
        PartDefinition part_ToungBase = root.addOrReplaceChild("ToungBase",
                CubeListBuilder.create()
                .texOffs(70, 17).addBox(-1.0F, -2.0F, -11.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.2617994F, 0.0F, 0.0F));
        PartDefinition part_MiddleTounge = root.addOrReplaceChild("MiddleTounge",
                CubeListBuilder.create()
                .texOffs(70, 10).addBox(-1.0F, -1.0F, -17.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.1745329F, 0.0F, 0.0F));
        PartDefinition part_EyeRight = root.addOrReplaceChild("EyeRight",
                CubeListBuilder.create()
                .texOffs(96, 60).addBox(-7.0F, -7.0F, -3.0F, 1.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.3490659F, 0.0F, 0.0F));
        PartDefinition part_EyeLeft = root.addOrReplaceChild("EyeLeft",
                CubeListBuilder.create()
                .texOffs(50, 60).addBox(6.0F, -7.0F, -3.0F, 1.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.3490659F, 0.0F, 0.0F));
        PartDefinition part_MouthTop = root.addOrReplaceChild("MouthTop",
                CubeListBuilder.create()
                .texOffs(52, 24).addBox(-5.0F, -6.0F, -16.0F, 10.0F, 6.0F, 16.0F),
                PartPose.offset(0.0F, 6.0F, -8.0F));
        PartDefinition part_Head = root.addOrReplaceChild("Head",
                CubeListBuilder.create()
                .texOffs(60, 46).addBox(-6.0F, -8.0F, -6.0F, 12.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, 6.0F, -8.0F));
        PartDefinition part_FangRight = root.addOrReplaceChild("FangRight",
                CubeListBuilder.create()
                .texOffs(92, 18).addBox(-4.0F, -3.0F, -15.0F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.1745329F, 0.0F, 0.0F));
        PartDefinition part_FangLeft = root.addOrReplaceChild("FangLeft",
                CubeListBuilder.create()
                .texOffs(60, 18).addBox(3.0F, -3.0F, -15.0F, 1.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.1745329F, 0.0F, 0.0F));
        PartDefinition part_ForkRight = root.addOrReplaceChild("ForkRight",
                CubeListBuilder.create()
                .texOffs(60, 3).addBox(6.0F, 0.6F, -21.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.0872665F, 0.4363323F, 0.0F));
        PartDefinition part_ForkLeft = root.addOrReplaceChild("ForkLeft",
                CubeListBuilder.create()
                .texOffs(80, 3).addBox(-8.0F, 0.6F, -21.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -8.0F, 0.0872665F, -0.4363323F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    // ENT-S-091 slice C: animation transcribed line for line from orig ModelSeaViper.java:295-402
    // (two independent refutations, none upheld). The previous port wrote orig field_82907_q
    // (ModelRenderer.offsetZ, a block-unit pre-rotation translation, proven from the 1.7.10
    // bytecode) into zRot; it is folded into z as initialPose.z + offsetZ * 16 here.
    @Override
    public void setupAnim(SeaViper entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelSeaViper.java:295 — cast to SeaViper (typed parameter here).
        // orig :296 super.render is ModelBase's no-op; orig :297 / :410-412 setRotationAngles
        // is a pure super call (no-op). Nothing to transcribe for either.

        // orig :298-301 — clamp negative limb swing to 0.
        float newangle = 0.0f;
        if (limbSwingAmount < 0.0f) {
            limbSwingAmount = 0.0f;
        }

        // orig :302 — base segment yaw: cos(t * 1.3 * wingspeed) * PI * 0.1, scaled by limbSwingAmount.
        this.tBase.yRot = newangle = Mth.cos(ageInTicks * 1.3f * this.wingspeed) * (float) Math.PI * 0.1f * limbSwingAmount;

        // orig :303-323 — chain every segment off the previous one (doseg), segment index 2..21.
        this.doseg(this.tBase, this.t2, 2.0f, limbSwingAmount, ageInTicks);   // orig :303
        this.doseg(this.t2, this.t3, 2.0f, limbSwingAmount, ageInTicks);      // orig :304 (index 2 again, as in the original)
        this.doseg(this.t3, this.t4, 3.0f, limbSwingAmount, ageInTicks);      // orig :305
        this.doseg(this.t4, this.t5, 4.0f, limbSwingAmount, ageInTicks);      // orig :306
        this.doseg(this.t5, this.t6, 5.0f, limbSwingAmount, ageInTicks);      // orig :307
        this.doseg(this.t6, this.t7, 6.0f, limbSwingAmount, ageInTicks);      // orig :308
        this.doseg(this.t7, this.t8, 7.0f, limbSwingAmount, ageInTicks);      // orig :309
        this.doseg(this.t8, this.t9, 8.0f, limbSwingAmount, ageInTicks);      // orig :310
        this.doseg(this.t9, this.t10, 9.0f, limbSwingAmount, ageInTicks);     // orig :311
        this.doseg(this.t10, this.t11, 10.0f, limbSwingAmount, ageInTicks);   // orig :312
        this.doseg(this.t11, this.t12, 11.0f, limbSwingAmount, ageInTicks);   // orig :313
        this.doseg(this.t12, this.t13, 12.0f, limbSwingAmount, ageInTicks);   // orig :314
        this.doseg(this.t13, this.t14, 13.0f, limbSwingAmount, ageInTicks);   // orig :315
        this.doseg(this.t14, this.t15, 14.0f, limbSwingAmount, ageInTicks);   // orig :316
        this.doseg(this.t15, this.t16, 15.0f, limbSwingAmount, ageInTicks);   // orig :317
        this.doseg(this.t16, this.t17, 16.0f, limbSwingAmount, ageInTicks);   // orig :318
        this.doseg(this.t17, this.t18, 17.0f, limbSwingAmount, ageInTicks);   // orig :319
        this.doseg(this.t18, this.t19, 18.0f, limbSwingAmount, ageInTicks);   // orig :320
        this.doseg(this.t19, this.t20, 19.0f, limbSwingAmount, ageInTicks);   // orig :321
        this.doseg(this.t20, this.t21, 20.0f, limbSwingAmount, ageInTicks);   // orig :322
        this.doseg(this.t21, this.TailTip, 21.0f, limbSwingAmount, ageInTicks); // orig :323

        // orig :324-346 — jaw / tongue: attacking branch vs idle branch.
        // orig field_82907_q is ModelRenderer.offsetZ (MCP 1.7.10: 82906_o=offsetX, 82907_q=offsetZ,
        // 82908_p=offsetY), a pre-rotation translation in BLOCK units that orig ModelRenderer.render
        // applied before the rotation-point translate (which is scaled by f5 = 1/16). ModelPart has
        // no offset field, so it folds into z as initialPose.z + offsetZ * 16 (see tongueOffsetZ below).
        float tongueOffsetZ;
        if (entity.getAttacking() != 0) {                                     // orig :324
            // orig :325-326 — jaw chatters wide open.
            newangle = Mth.cos(ageInTicks * 1.7f * this.wingspeed) * (float) Math.PI * 0.17f;
            this.MouthBottom.xRot = 0.65f + newangle;
            // orig :327-331 — tongue flicks fast.
            newangle = Mth.cos(ageInTicks * 4.7f * this.wingspeed) * (float) Math.PI * 0.07f;
            this.ToungBase.xRot = 0.261f + newangle;
            this.MiddleTounge.xRot = 0.174f + newangle;
            this.ForkLeft.xRot = 0.087f + newangle;
            this.ForkRight.xRot = 0.087f + newangle;
            // orig :332-334 — ForkLeft.offsetZ = ForkRight.offsetZ = newangle; MiddleTounge / ToungBase copy it.
            tongueOffsetZ = newangle = Mth.cos(ageInTicks * 1.5f * this.wingspeed) * (float) Math.PI * 0.05f;
        } else {                                                              // orig :335
            // orig :336-337 — jaw nearly closed, slow breathing.
            newangle = Mth.cos(ageInTicks * 0.2f * this.wingspeed) * (float) Math.PI * 0.02f;
            this.MouthBottom.xRot = 0.45f + newangle;
            // orig :338-342 — tongue flicks slowly.
            newangle = Mth.cos(ageInTicks * 1.7f * this.wingspeed) * (float) Math.PI * 0.03f;
            this.ToungBase.xRot = 0.261f + newangle;
            this.MiddleTounge.xRot = 0.174f + newangle;
            this.ForkLeft.xRot = 0.087f + newangle;
            this.ForkRight.xRot = 0.087f + newangle;
            // orig :343-345 — same offsetZ fan-out at 0.5 Hz factor.
            tongueOffsetZ = newangle = Mth.cos(ageInTicks * 0.5f * this.wingspeed) * (float) Math.PI * 0.05f;
        }
        // orig :332-334 / :343-345 — offsetZ (blocks) folded into pivot z (pixels, f5 = 1/16 -> x16).
        this.ForkLeft.z = this.ForkLeft.getInitialPose().z + tongueOffsetZ * 16.0f;
        this.ForkRight.z = this.ForkRight.getInitialPose().z + tongueOffsetZ * 16.0f;
        this.MiddleTounge.z = this.MiddleTounge.getInitialPose().z + tongueOffsetZ * 16.0f;
        this.ToungBase.z = this.ToungBase.getInitialPose().z + tongueOffsetZ * 16.0f;

        // orig :347-351 — head group yaws at half the net head yaw.
        this.EyeLeft.yRot = this.EyeRight.yRot = (newangle = (float) Math.toRadians(netHeadYaw) * 0.5f);
        this.MouthTop.yRot = this.EyeRight.yRot;
        this.Head.yRot = this.EyeRight.yRot;
        this.FangLeft.yRot = this.FangRight.yRot = newangle;
        this.MouthBottom.yRot = newangle;
        // orig :352-353 — lower-jaw pivot trails the head pivot by 2 px along the head yaw.
        this.MouthBottom.z = this.Head.z - (float) Math.cos(this.Head.yRot) * 2.0f;
        this.MouthBottom.x = this.Head.x - (float) Math.sin(this.Head.yRot) * 2.0f;
        // orig :354-357 — tongue follows head yaw; forks splay +/-0.436 rad.
        this.ToungBase.yRot = newangle;
        this.MiddleTounge.yRot = newangle;
        this.ForkLeft.yRot = newangle - 0.436f;
        this.ForkRight.yRot = newangle + 0.436f;

        // orig :358-391 — the per-part render(f5) calls are renderToBuffer's job in 1.21
        // (same order, including t12 before t11 at orig :370-371).
    }

    // orig ModelSeaViper.java:394-402 — position and yaw one segment off its predecessor.
    private void doseg(ModelPart inn, ModelPart notinn, float f, float f1, float f2) {
        float pi4 = 0.7853982f;                                               // orig :395
        float newangle = 0.0f;                                                // orig :396
        // orig :397-398 — 9 px along the previous segment's yaw, foreshortened by |cos(pitch)|.
        // Math.cos / casts kept verbatim (double math) so the result is bit-identical to the original.
        notinn.z = (float) ((double) inn.z + (double) ((float) Math.cos(inn.yRot)) * (9.0 * Math.abs(Math.cos(inn.xRot))));
        notinn.x = (float) ((double) inn.x + (double) ((float) Math.sin(inn.yRot) * 9.0f) * Math.abs(Math.cos(inn.xRot)));
        // orig :399-401 — travelling wave lagged pi/4 per segment, scaled by limbSwingAmount;
        // blends toward the static S-curve cos(-pi4 * f) as the swing amount drops to 0.
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed - pi4 * f) * (float) Math.PI * 0.2f * f1;
        float a = Mth.cos(-(pi4 * f));
        notinn.yRot = newangle + a - a * f1;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.TailTip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t19.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t20.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MouthBottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ToungBase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MiddleTounge.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.EyeRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.EyeLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.MouthTop.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FangRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.FangLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ForkRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ForkLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
