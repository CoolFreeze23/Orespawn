package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityWormSmall;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WormSmallModel<T extends EntityWormSmall> extends EntityModel<T> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;

    public WormSmallModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-0.5F, -5.0F, -0.5F, 1, 5, 1),
                PartPose.offset(0.0F, 14.0F, 0.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(6, 0).mirror()
                        .addBox(-0.5F, -5.0F, -0.5F, 1, 5, 1),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(12, 0).mirror()
                        .addBox(-0.5F, -5.0F, -0.5F, 1, 5, 1),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = Mth.cos(ageInTicks * 0.55F) * (float) Math.PI * 0.15F;
        this.tail.xRot = newangle;
        float d1 = (float) (Math.sin(newangle) * 5.0);
        float d2 = (float) (Math.cos(newangle) * 5.0);
        this.body.z = this.tail.z - d1;

        float newangle2 = Mth.cos(ageInTicks * 0.35F) * (float) Math.PI * 0.1F;
        this.tail.zRot = newangle2;
        float d3 = (float) (Math.cos(newangle2) * d2);
        float d4 = (float) (Math.sin(newangle2) * d2);
        this.body.x = this.tail.x + d4;
        this.body.y = (float) (this.tail.y - 5.0 + (5.0 - d3));

        newangle = Mth.cos(ageInTicks * 0.45F) * (float) Math.PI * 0.15F;
        this.body.xRot = newangle;
        d1 = (float) (Math.sin(newangle) * 5.0);
        d2 = (float) (Math.cos(newangle) * 5.0);
        this.head.z = this.body.z - d1;

        newangle2 = Mth.cos(ageInTicks * 0.25F) * (float) Math.PI * 0.1F;
        this.body.zRot = newangle2;
        d3 = (float) (Math.cos(newangle2) * d2);
        d4 = (float) (Math.sin(newangle2) * d2);
        this.head.x = this.body.x + d4;
        this.head.y = (float) (this.body.y - 5.0 + (5.0 - d3));

        this.head.xRot = 0.62F + Mth.cos(ageInTicks * 0.65F) * (float) Math.PI * 0.15F;
        this.head.zRot = Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
