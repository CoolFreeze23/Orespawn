package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Irukandji;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelIrukandji extends EntityModel<Irukandji> {
    private final ModelPart body;
    private final ModelPart t11;
    private final ModelPart t12;
    private final ModelPart t21;
    private final ModelPart t22;
    private final ModelPart t31;
    private final ModelPart t32;
    private final ModelPart t41;
    private final ModelPart t42;

    public ModelIrukandji(ModelPart root) {
        this.body = root.getChild("body");
        this.t11 = root.getChild("t11");
        this.t12 = root.getChild("t12");
        this.t21 = root.getChild("t21");
        this.t22 = root.getChild("t22");
        this.t31 = root.getChild("t31");
        this.t32 = root.getChild("t32");
        this.t41 = root.getChild("t41");
        this.t42 = root.getChild("t42");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-2.0F, 0.0F, -2.0F, 4, 4, 4),
                PartPose.offset(0.0F, 6.0F, 0.0F));

        partdefinition.addOrReplaceChild("t11",
                CubeListBuilder.create().texOffs(25, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(1.0F, 10.0F, -2.0F));

        partdefinition.addOrReplaceChild("t12",
                CubeListBuilder.create().texOffs(5, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(1.0F, 17.0F, -2.0F));

        partdefinition.addOrReplaceChild("t21",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(-2.0F, 10.0F, -2.0F));

        partdefinition.addOrReplaceChild("t22",
                CubeListBuilder.create().texOffs(20, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(-2.0F, 17.0F, -2.0F));

        partdefinition.addOrReplaceChild("t31",
                CubeListBuilder.create().texOffs(30, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(1.0F, 10.0F, 1.0F));

        partdefinition.addOrReplaceChild("t32",
                CubeListBuilder.create().texOffs(10, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(1.0F, 17.0F, 1.0F));

        partdefinition.addOrReplaceChild("t41",
                CubeListBuilder.create().texOffs(35, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(-2.0F, 10.0F, 1.0F));

        partdefinition.addOrReplaceChild("t42",
                CubeListBuilder.create().texOffs(15, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 7, 1),
                PartPose.offset(-2.0F, 17.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Irukandji entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        this.t11.xRot = newangle = Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
        float d1 = (float)(Math.sin(newangle) * 7.0);
        float d2 = (float)(Math.cos(newangle) * 7.0);
        this.t12.z = this.t11.z + d1;
        this.t11.zRot = newangle = Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.1f;
        float d3 = (float)(Math.cos(newangle) * (double)d2);
        float d4 = (float)(Math.sin(newangle) * (double)d2);
        this.t12.x = this.t11.x - d4;
        this.t12.y = this.t11.y + d3;
        this.t12.xRot = newangle = Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.15f;
        this.t12.zRot = newangle = Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.1f;
        this.t21.xRot = newangle = Mth.cos((float)(ageInTicks * 0.65f)) * (float)Math.PI * 0.15f;
        d1 = (float)(Math.sin(newangle) * 7.0);
        d2 = (float)(Math.cos(newangle) * 7.0);
        this.t22.z = this.t21.z + d1;
        this.t21.zRot = newangle = Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.1f;
        d3 = (float)(Math.cos(newangle) * (double)d2);
        d4 = (float)(Math.sin(newangle) * (double)d2);
        this.t22.x = this.t21.x - d4;
        this.t22.y = this.t21.y + d3;
        this.t22.xRot = newangle = Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
        this.t22.zRot = newangle = Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.1f;
        this.t31.xRot = newangle = Mth.cos((float)(ageInTicks * 0.5f)) * (float)Math.PI * 0.15f;
        d1 = (float)(Math.sin(newangle) * 7.0);
        d2 = (float)(Math.cos(newangle) * 7.0);
        this.t32.z = this.t31.z + d1;
        this.t31.zRot = newangle = Mth.cos((float)(ageInTicks * 0.3f)) * (float)Math.PI * 0.1f;
        d3 = (float)(Math.cos(newangle) * (double)d2);
        d4 = (float)(Math.sin(newangle) * (double)d2);
        this.t32.x = this.t31.x - d4;
        this.t32.y = this.t31.y + d3;
        this.t32.xRot = newangle = Mth.cos((float)(ageInTicks * 0.4f)) * (float)Math.PI * 0.15f;
        this.t32.zRot = newangle = Mth.cos((float)(ageInTicks * 0.2f)) * (float)Math.PI * 0.1f;
        this.t41.xRot = newangle = Mth.cos((float)(ageInTicks * 0.57f)) * (float)Math.PI * 0.15f;
        d1 = (float)(Math.sin(newangle) * 7.0);
        d2 = (float)(Math.cos(newangle) * 7.0);
        this.t42.z = this.t41.z + d1;
        this.t41.zRot = newangle = Mth.cos((float)(ageInTicks * 0.37f)) * (float)Math.PI * 0.1f;
        d3 = (float)(Math.cos(newangle) * (double)d2);
        d4 = (float)(Math.sin(newangle) * (double)d2);
        this.t42.x = this.t41.x - d4;
        this.t42.y = this.t41.y + d3;
        this.t42.xRot = newangle = Mth.cos((float)(ageInTicks * 0.48f)) * (float)Math.PI * 0.15f;
        this.t42.zRot = newangle = Mth.cos((float)(ageInTicks * 0.29f)) * (float)Math.PI * 0.1f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t22.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t31.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t32.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t41.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t42.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
