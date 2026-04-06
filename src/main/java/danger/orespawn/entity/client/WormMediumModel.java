package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityWormMedium;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WormMediumModel extends EntityModel<EntityWormMedium> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;
    private final ModelPart tooth4;
    private final ModelPart head2;

    public WormMediumModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.tail = root.getChild("tail");
        this.tooth1 = root.getChild("tooth1");
        this.tooth2 = root.getChild("tooth2");
        this.tooth3 = root.getChild("tooth3");
        this.tooth4 = root.getChild("tooth4");
        this.head2 = root.getChild("head2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(24, 0).mirror()
                        .addBox(-1.5F, -12.0F, -1.5F, 3, 12, 3),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(37, 0).mirror()
                        .addBox(-1.5F, -12.0F, -1.5F, 3, 12, 3),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(50, 0).mirror()
                        .addBox(-1.5F, -12.0F, -1.5F, 3, 12, 3),
                PartPose.offset(0.0F, 25.0F, 0.0F));

        partdefinition.addOrReplaceChild("tooth1",
                CubeListBuilder.create().texOffs(15, 0).mirror()
                        .addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(1.0F, -11.0F, 0.0F));

        partdefinition.addOrReplaceChild("tooth2",
                CubeListBuilder.create().texOffs(5, 0).mirror()
                        .addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(-1.0F, -11.0F, 0.0F));

        partdefinition.addOrReplaceChild("tooth3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(0.0F, -11.0F, 1.0F));

        partdefinition.addOrReplaceChild("tooth4",
                CubeListBuilder.create().texOffs(10, 0).mirror()
                        .addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1),
                PartPose.offset(0.0F, -11.0F, -1.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(0, 6).mirror()
                        .addBox(-2.0F, -8.0F, -2.0F, 4, 8, 4),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(EntityWormMedium entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle;
        this.tail.xRot = newangle = Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.1f;
        float d1 = (float)(Math.sin(newangle) * 12.0);
        float d2 = (float)(Math.cos(newangle) * 12.0);
        this.body.z = this.tail.z - d1;
        this.tail.zRot = newangle = Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.08f;
        float d3 = (float)(Math.cos(newangle) * (double)d2);
        float d4 = (float)(Math.sin(newangle) * (double)d2);
        this.body.x = this.tail.x + d4;
        this.body.y = (float)((double)this.tail.y - 12.0 + (12.0 - (double)d3));
        this.body.xRot = newangle = Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.1f;
        d1 = (float)(Math.sin(newangle) * 12.0);
        d2 = (float)(Math.cos(newangle) * 12.0);
        this.head2.z = this.head.z = this.body.z - d1;
        this.body.zRot = newangle = Mth.cos((float)(ageInTicks * 0.15f)) * (float)Math.PI * 0.07f;
        d3 = (float)(Math.cos(newangle) * (double)d2);
        d4 = (float)(Math.sin(newangle) * (double)d2);
        this.head2.x = this.head.x = this.body.x + d4;
        this.head2.y = this.head.y = (float)((double)this.body.y - 12.0 + (12.0 - (double)d3));
        this.head2.xRot = this.head.xRot = 0.62f + Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
        this.head2.zRot = this.head.zRot = Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.05f;
        this.tooth3.xRot = this.tooth4.xRot = (newangle = this.head.xRot);
        this.tooth2.xRot = this.tooth4.xRot;
        this.tooth1.xRot = this.tooth4.xRot;
        d1 = (float)(Math.sin(newangle) * 12.0);
        d2 = (float)(Math.cos(newangle) * 12.0);
        this.tooth3.z = this.tooth4.z = this.head.z - d1;
        this.tooth2.z = this.tooth4.z;
        this.tooth1.z = this.tooth4.z;
        this.tooth3.zRot = this.tooth4.zRot = (newangle = this.head.zRot);
        this.tooth2.zRot = this.tooth4.zRot;
        this.tooth1.zRot = this.tooth4.zRot;
        d3 = (float)(Math.cos(newangle) * (double)d2);
        d4 = (float)(Math.sin(newangle) * (double)d2);
        this.tooth3.x = this.tooth4.x = this.head.x + d4;
        this.tooth2.x = this.tooth4.x;
        this.tooth1.x = this.tooth4.x;
        this.tooth3.y = this.tooth4.y = (float)((double)this.head.y - 12.0 + (12.0 - (double)d3));
        this.tooth2.y = this.tooth4.y;
        this.tooth1.y = this.tooth4.y;
        this.tooth1.z += 1.0f;
        this.tooth2.z -= 1.0f;
        this.tooth1.xRot = this.tooth1.xRot - 0.4f - Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
        this.tooth2.xRot = this.tooth2.xRot + 0.4f + Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
        this.tooth3.x += 1.0f;
        this.tooth4.x -= 1.0f;
        this.tooth3.zRot = this.tooth3.zRot + 0.4f + Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
        this.tooth4.zRot = this.tooth4.zRot - 0.4f - Mth.cos((float)(ageInTicks * 0.55f)) * (float)Math.PI * 0.15f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
