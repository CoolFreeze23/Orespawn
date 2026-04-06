package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.BandP;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelBandP extends EntityModel<BandP> {
    private final ModelPart belly;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart lleg;
    private final ModelPart rleg;
    private final ModelPart larm;
    private final ModelPart rarm;

    public ModelBandP(ModelPart root) {
        this.belly = root.getChild("belly");
        this.chest = root.getChild("chest");
        this.head = root.getChild("head");
        this.lleg = root.getChild("lleg");
        this.rleg = root.getChild("rleg");
        this.larm = root.getChild("larm");
        this.rarm = root.getChild("rarm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("belly",
                CubeListBuilder.create().texOffs(0, 61).mirror()
                        .addBox(-8.0F, -5.0F, -7.0F, 16, 10, 16),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0698132F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("chest",
                CubeListBuilder.create().texOffs(0, 42).mirror()
                        .addBox(-5.0F, -3.0F, -5.0F, 10, 6, 10),
                PartPose.offset(0.0F, 5.0F, 2.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 11).mirror()
                        .addBox(-3.0F, -5.0F, -3.0F, 6, 6, 6),
                PartPose.offset(0.0F, 1.0F, 3.0F));

        partdefinition.addOrReplaceChild("lleg",
                CubeListBuilder.create().texOffs(25, 90).mirror()
                        .addBox(-2.0F, 0.0F, -3.0F, 6, 8, 6),
                PartPose.offset(2.0F, 16.0F, 2.0F));

        partdefinition.addOrReplaceChild("rleg",
                CubeListBuilder.create().texOffs(0, 90).mirror()
                        .addBox(-4.0F, 0.0F, -3.0F, 6, 8, 6),
                PartPose.offset(-2.0F, 16.0F, 2.0F));

        partdefinition.addOrReplaceChild("larm",
                CubeListBuilder.create().texOffs(0, 25).mirror()
                        .addBox(-1.0F, -1.0F, -2.0F, 4, 10, 4),
                PartPose.offsetAndRotation(6.0F, 4.0F, 3.0F, 0.0F, 0.0F, -0.4886922F));

        partdefinition.addOrReplaceChild("rarm",
                CubeListBuilder.create().texOffs(18, 25).mirror()
                        .addBox(-3.0F, -1.0F, -2.0F, 4, 10, 4),
                PartPose.offsetAndRotation(-6.0F, 4.0F, 3.0F, 0.0F, 0.0F, 0.4886922F));

        return LayerDefinition.create(meshdefinition, 64, 128);
    }

    @Override
    public void setupAnim(BandP entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        if ((double)limbSwingAmount > 0.1) {
        newangle = Mth.cos((float)(ageInTicks * 1.3f * limbSwingAmount)) * (float)Math.PI * 0.25f * limbSwingAmount;
        newangle2 = Mth.cos((float)(ageInTicks * 2.6f * limbSwingAmount)) * (float)Math.PI * 0.025f * limbSwingAmount;
        newangle3 = newangle;
        } else {
        newangle = 0.0f;
        newangle2 = Mth.cos((float)(ageInTicks * 0.6f * limbSwingAmount)) * (float)Math.PI * 0.005f;
        newangle3 = Mth.cos((float)(ageInTicks * 0.3f * limbSwingAmount)) * (float)Math.PI * 0.02f;
        }
        this.lleg.xRot = newangle;
        this.rleg.xRot = -newangle;
        this.belly.xRot = 0.07f + newangle2;
        this.larm.xRot = -newangle3;
        this.rarm.xRot = newangle3;
        this.belly.yRot = -newangle / 2.0f;
        this.head.yRot = (float)Math.toRadians(netHeadYaw);
        this.head.xRot = (float)Math.toRadians(headPitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.belly.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.larm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rarm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
