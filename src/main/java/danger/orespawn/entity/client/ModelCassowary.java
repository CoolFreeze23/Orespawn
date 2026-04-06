package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Cassowary;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelCassowary extends EntityModel<Cassowary> {
    private float wingspeed = 1.0f;
    private final ModelPart tail;
    private final ModelPart body;
    private final ModelPart neck1;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart crest;
    private final ModelPart foot1;
    private final ModelPart foot2;
    private final ModelPart gobbler;

    public ModelCassowary(ModelPart root) {
        this.tail = root.getChild("tail");
        this.body = root.getChild("body");
        this.neck1 = root.getChild("neck1");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.beak = root.getChild("beak");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.crest = root.getChild("crest");
        this.foot1 = root.getChild("foot1");
        this.foot2 = root.getChild("foot2");
        this.gobbler = root.getChild("gobbler");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
            .texOffs(38, 16).mirror()
            .addBox(-3.0F, 0.0F, 0.0F, 6.0F, 9.0F, 7.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 1.0F, 0.8922867F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 13).mirror()
            .addBox(-4.0F, 0.0F, 0.0F, 8.0F, 10.0F, 9.0F),
            PartPose.offsetAndRotation(0.0F, 5.0F, -3.0F, 0.3346075F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck1", CubeListBuilder.create()
            .texOffs(48, 0).mirror()
            .addBox(-2.0F, 0.0F, 0.0F, 4.0F, 5.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, 4.0F, -1.0F, -1.189716F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck", CubeListBuilder.create()
            .texOffs(38, 0).mirror()
            .addBox(-1.0F, 0.0F, 0.0F, 2.0F, 7.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, -3.0F, -2.806985F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(24, 0).mirror()
            .addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 4.0F),
            PartPose.offsetAndRotation(0.0F, 2.0F, -6.0F, 0.0371786F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("beak", CubeListBuilder.create()
            .texOffs(28, 7).mirror()
            .addBox(-0.5F, 0.0F, 3.0F, 1.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, 2.0F, -6.0F, -3.104414F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-0.5F, 0.0F, -1.0F, 1.0F, 11.0F, 2.0F),
            PartPose.offset(3.0F, 12.0F, 3.0F));

        partdefinition.addOrReplaceChild("leg2", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-0.5F, 0.0F, -1.0F, 1.0F, 11.0F, 2.0F),
            PartPose.offset(-3.0F, 12.0F, 3.0F));

        partdefinition.addOrReplaceChild("crest", CubeListBuilder.create()
            .texOffs(10, 0).mirror()
            .addBox(-0.5F, -4.0F, 1.0F, 1.0F, 4.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 2.0F, -6.0F, 1.710216F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("foot1", CubeListBuilder.create()
            .texOffs(47, 10).mirror()
            .addBox(-1.033333F, 11.0F, -2.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(-3.0F, 12.0F, 3.0F));

        partdefinition.addOrReplaceChild("foot2", CubeListBuilder.create()
            .texOffs(47, 10).mirror()
            .addBox(-1.0F, 11.0F, -2.0F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(3.0F, 12.0F, 3.0F));

        partdefinition.addOrReplaceChild("gobbler", CubeListBuilder.create()
            .texOffs(38, 10).mirror()
            .addBox(-0.5F, -1.0F, -2.5F, 1.0F, 5.0F, 1.0F),
            PartPose.offset(0.0F, 8.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(Cassowary entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0F;
        float newangle2 = 0.0F;
        if (limbSwingAmount > 0.1F) {
            newangle = Mth.cos(ageInTicks * 1.3F * this.wingspeed) * (float) Math.PI * 0.15F * limbSwingAmount;
            newangle2 = Mth.cos(ageInTicks * 2.6F * this.wingspeed) * (float) Math.PI * 0.1F * limbSwingAmount;
        } else {
            newangle2 = 0.0F;
            newangle = 0.0F;
        }
        this.leg1.xRot = newangle;
        this.foot2.xRot = newangle;
        this.leg2.xRot = -newangle;
        this.foot1.xRot = -newangle;
        this.neck.xRot = -2.827F + newangle2;
        this.gobbler.xRot = newangle2;
        this.crest.z = this.neck.z + Mth.sin(this.neck.xRot) * 7.0F;
        this.beak.z = this.crest.z;
        this.head.z = this.beak.z;
        this.crest.y = this.neck.y + Mth.cos(this.neck.xRot) * 7.0F;
        this.beak.y = this.crest.y;
        this.head.y = this.beak.y;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.tail.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.beak.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.crest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.foot1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.foot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.gobbler.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
