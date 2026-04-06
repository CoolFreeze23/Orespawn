package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityWormLarge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WormLargeModel extends EntityModel<EntityWormLarge> {
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart head4;
    private final ModelPart head5;
    private final ModelPart neck1;
    private final ModelPart neck4;
    private final ModelPart neck5;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart tail1;
    private final ModelPart tailtip;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;
    private final ModelPart tooth4;
    private final ModelPart tooth5;
    private final ModelPart tooth6;
    private final ModelPart tooth7;
    private final ModelPart tooth8;

    public WormLargeModel(ModelPart root) {
        this.head1 = root.getChild("head1");
        this.head2 = root.getChild("head2");
        this.head3 = root.getChild("head3");
        this.head4 = root.getChild("head4");
        this.head5 = root.getChild("head5");
        this.neck1 = root.getChild("neck1");
        this.neck4 = root.getChild("neck4");
        this.neck5 = root.getChild("neck5");
        this.neck2 = root.getChild("neck2");
        this.neck3 = root.getChild("neck3");
        this.tail1 = root.getChild("tail1");
        this.tailtip = root.getChild("tailtip");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4");
        this.tooth1 = root.getChild("tooth1");
        this.tooth2 = root.getChild("tooth2");
        this.tooth3 = root.getChild("tooth3");
        this.tooth4 = root.getChild("tooth4");
        this.tooth5 = root.getChild("tooth5");
        this.tooth6 = root.getChild("tooth6");
        this.tooth7 = root.getChild("tooth7");
        this.tooth8 = root.getChild("tooth8");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-8.0F, -8.0F, -20.0F, 16, 16, 20),
                PartPose.offset(0.0F, 0.0F, 10.0F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(83, 27).mirror()
                        .addBox(8.0F, -3.0F, -20.0F, 3, 6, 19),
                PartPose.offset(0.0F, 0.0F, 10.0F));

        partdefinition.addOrReplaceChild("head3",
                CubeListBuilder.create().texOffs(9, 65).mirror()
                        .addBox(-11.0F, -3.0F, -20.0F, 3, 6, 19),
                PartPose.offset(0.0F, 0.0F, 10.0F));

        partdefinition.addOrReplaceChild("head4",
                CubeListBuilder.create().texOffs(77, 0).mirror()
                        .addBox(-3.0F, -11.0F, -20.0F, 6, 3, 20),
                PartPose.offset(0.0F, 0.0F, 10.0F));

        partdefinition.addOrReplaceChild("head5",
                CubeListBuilder.create().texOffs(10, 39).mirror()
                        .addBox(-3.0F, 8.0F, -20.0F, 6, 3, 20),
                PartPose.offset(0.0F, 0.0F, 10.0F));

        partdefinition.addOrReplaceChild("neck1",
                CubeListBuilder.create().texOffs(25, 94).mirror()
                        .addBox(-6.0F, -6.0F, -36.0F, 12, 12, 36),
                PartPose.offsetAndRotation(0.0F, 20.0F, 33.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck4",
                CubeListBuilder.create().texOffs(25, 146).mirror()
                        .addBox(-2.0F, -8.0F, -38.0F, 4, 2, 38),
                PartPose.offsetAndRotation(0.0F, 20.0F, 33.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck5",
                CubeListBuilder.create().texOffs(125, 189).mirror()
                        .addBox(-2.0F, 6.0F, -31.0F, 4, 2, 31),
                PartPose.offsetAndRotation(0.0F, 20.0F, 33.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck2",
                CubeListBuilder.create().texOffs(25, 189).mirror()
                        .addBox(6.0F, -2.0F, -34.0F, 2, 4, 34),
                PartPose.offsetAndRotation(0.0F, 20.0F, 33.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck3",
                CubeListBuilder.create().texOffs(125, 147).mirror()
                        .addBox(-8.0F, -2.0F, -34.0F, 2, 4, 34),
                PartPose.offsetAndRotation(0.0F, 20.0F, 33.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(145, 21).mirror()
                        .addBox(-4.0F, -4.0F, 0.0F, 8, 8, 24),
                PartPose.offset(0.0F, 20.0F, 29.0F));

        partdefinition.addOrReplaceChild("tailtip",
                CubeListBuilder.create().texOffs(180, 0).mirror()
                        .addBox(-1.5F, -1.5F, 0.0F, 3, 3, 12),
                PartPose.offsetAndRotation(0.0F, 19.5F, 52.0F, 0.3490659F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(145, 56).mirror()
                        .addBox(4.0F, -1.0F, 2.0F, 1, 2, 14),
                PartPose.offset(0.0F, 20.0F, 29.0F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(145, 90).mirror()
                        .addBox(-5.0F, -1.0F, 2.0F, 1, 2, 14),
                PartPose.offset(0.0F, 20.0F, 29.0F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(145, 76).mirror()
                        .addBox(-1.0F, -5.0F, 7.0F, 2, 1, 9),
                PartPose.offset(0.0F, 20.0F, 29.0F));

        partdefinition.addOrReplaceChild("tooth1",
                CubeListBuilder.create().texOffs(0, 220).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(0.0F, 9.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth2",
                CubeListBuilder.create().texOffs(0, 210).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(0.0F, -9.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth3",
                CubeListBuilder.create().texOffs(0, 200).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(9.0F, 0.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth4",
                CubeListBuilder.create().texOffs(0, 190).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(-9.0F, 0.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth5",
                CubeListBuilder.create().texOffs(0, 180).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(-6.0F, -6.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth6",
                CubeListBuilder.create().texOffs(0, 170).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(6.0F, 6.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth7",
                CubeListBuilder.create().texOffs(0, 160).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(6.0F, -6.0F, -10.0F));

        partdefinition.addOrReplaceChild("tooth8",
                CubeListBuilder.create().texOffs(0, 150).mirror()
                        .addBox(-0.5F, -0.5F, -7.0F, 1, 1, 7),
                PartPose.offset(-6.0F, 6.0F, -10.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(EntityWormLarge entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle2;
        double dist = 32.0;
        float newangle = Mth.cos((float)(ageInTicks * 0.25f)) * (float)Math.PI * 0.08f;
        this.neck1.xRot = newangle -= 0.698f;
        this.neck1.yRot = newangle2 = Mth.cos((float)(ageInTicks * 0.15f)) * (float)Math.PI * 0.07f;
        this.neck4.xRot = this.neck5.xRot = this.neck1.xRot;
        this.neck3.xRot = this.neck5.xRot;
        this.neck2.xRot = this.neck5.xRot;
        this.neck4.yRot = this.neck5.yRot = this.neck1.yRot;
        this.neck3.yRot = this.neck5.yRot;
        this.neck2.yRot = this.neck5.yRot;
        double d1 = (float)(Math.cos(newangle) * dist);
        double d2 = (float)(Math.sin(newangle) * dist);
        this.head1.z = (float)((double)this.neck1.z - d1);
        double d3 = (float)(Math.sin(newangle2) * d1);
        double d4 = (float)(Math.cos(newangle2) * d1);
        this.head1.x = (float)((double)this.neck1.x - d3);
        this.head1.y = (float)((double)this.neck1.y + d2);
        this.head1.xRot = newangle = Mth.cos((float)(ageInTicks * 0.35f)) * (float)Math.PI * 0.15f;
        this.head1.yRot = newangle2 = Mth.cos((float)(ageInTicks * 0.45f)) * (float)Math.PI * 0.05f;
        this.head4.x = this.head5.x = this.head1.x;
        this.head3.x = this.head5.x;
        this.head2.x = this.head5.x;
        this.head4.y = this.head5.y = this.head1.y;
        this.head3.y = this.head5.y;
        this.head2.y = this.head5.y;
        this.head4.z = this.head5.z = this.head1.z;
        this.head3.z = this.head5.z;
        this.head2.z = this.head5.z;
        this.head4.xRot = this.head5.xRot = this.head1.xRot;
        this.head3.xRot = this.head5.xRot;
        this.head2.xRot = this.head5.xRot;
        this.head4.yRot = this.head5.yRot = this.head1.yRot;
        this.head3.yRot = this.head5.yRot;
        this.head2.yRot = this.head5.yRot;
        dist = 19.0;
        d1 = (float)(Math.cos(newangle) * dist);
        d2 = (float)(Math.sin(newangle) * dist);
        this.tooth1.z = (float)((double)this.head1.z - d1);
        d3 = (float)(Math.sin(newangle2) * d1);
        d4 = (float)(Math.cos(newangle2) * d1);
        this.tooth1.x = (float)((double)this.head1.x - d3);
        this.tooth1.y = (float)((double)this.head1.y + d2 - 9.0);
        this.tooth2.z = this.tooth1.z;
        this.tooth2.x = this.tooth1.x;
        this.tooth2.y = this.tooth1.y + 18.0f;
        this.tooth3.z = this.tooth1.z;
        this.tooth3.x = this.tooth1.x + 9.0f;
        this.tooth3.y = this.tooth1.y + 9.0f;
        this.tooth4.z = this.tooth1.z;
        this.tooth4.x = this.tooth1.x - 9.0f;
        this.tooth4.y = this.tooth1.y + 9.0f;
        this.tooth5.z = this.tooth1.z;
        this.tooth5.x = this.tooth1.x - 6.0f;
        this.tooth5.y = this.tooth1.y + 9.0f - 6.0f;
        this.tooth6.z = this.tooth1.z;
        this.tooth6.x = this.tooth1.x + 6.0f;
        this.tooth6.y = this.tooth1.y + 9.0f + 6.0f;
        this.tooth7.z = this.tooth1.z;
        this.tooth7.x = this.tooth1.x + 6.0f;
        this.tooth7.y = this.tooth1.y + 9.0f - 6.0f;
        this.tooth8.z = this.tooth1.z;
        this.tooth8.x = this.tooth1.x - 6.0f;
        this.tooth8.y = this.tooth1.y + 9.0f + 6.0f;
        this.tooth1.z = (float)((double)this.tooth1.z - Math.sin(this.head1.xRot) * 9.0);
        this.tooth2.z = (float)((double)this.tooth2.z + Math.sin(this.head1.xRot) * 9.0);
        this.tooth3.z = (float)((double)this.tooth3.z - Math.sin(this.head1.yRot) * 9.0);
        this.tooth4.z = (float)((double)this.tooth4.z + Math.sin(this.head1.yRot) * 9.0);
        this.tooth7.z = (float)((double)this.tooth7.z - Math.sin(this.head1.xRot) * 6.0);
        this.tooth7.z = (float)((double)this.tooth7.z - Math.sin(this.head1.yRot) * 6.0);
        this.tooth6.z = (float)((double)this.tooth6.z + Math.sin(this.head1.xRot) * 6.0);
        this.tooth6.z = (float)((double)this.tooth6.z - Math.sin(this.head1.yRot) * 6.0);
        this.tooth5.z = (float)((double)this.tooth5.z - Math.sin(this.head1.xRot) * 6.0);
        this.tooth5.z = (float)((double)this.tooth5.z + Math.sin(this.head1.yRot) * 6.0);
        this.tooth8.z = (float)((double)this.tooth8.z + Math.sin(this.head1.xRot) * 6.0);
        this.tooth8.z = (float)((double)this.tooth8.z + Math.sin(this.head1.yRot) * 6.0);
        newangle = Mth.cos((float)(ageInTicks * 0.57f)) * (float)Math.PI * 0.35f;
        this.tooth1.xRot = this.head1.xRot + newangle;
        this.tooth2.xRot = this.head1.xRot - newangle;
        this.tooth3.yRot = this.head1.yRot + newangle;
        this.tooth4.yRot = this.head1.yRot - newangle;
        this.tooth5.xRot = this.head1.xRot + newangle;
        this.tooth7.xRot = this.head1.xRot + newangle;
        this.tooth6.xRot = this.head1.xRot - newangle;
        this.tooth8.xRot = this.head1.xRot - newangle;
        this.tooth6.yRot = this.head1.yRot + newangle;
        this.tooth7.yRot = this.head1.yRot + newangle;
        this.tooth5.yRot = this.head1.yRot - newangle;
        this.tooth8.yRot = this.head1.yRot - newangle;
        newangle = Mth.cos((float)(ageInTicks * 0.63f)) * (float)Math.PI * 0.15f;
        this.tailtip.xRot = newangle + 0.35f;
        this.tailtip.yRot = newangle = Mth.cos((float)((float)((double)(ageInTicks * 0.63f) + 1.57075))) * (float)Math.PI * 0.15f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailtip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
