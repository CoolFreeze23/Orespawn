package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityTerribleTerror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class TerribleTerrorModel<T extends EntityTerribleTerror> extends EntityModel<T> {
    private final ModelPart horn1, horn2, snout, head, jaw, neck, body, wing1, wing2;
    private final ModelPart tail1, tail2, tail3, tail4;
    private final ModelPart fl11, fl12, fl21, fl22, bl21, bl22, bl11, bl12;

    public TerribleTerrorModel(ModelPart root) {
        this.horn1 = root.getChild("horn1"); this.horn2 = root.getChild("horn2");
        this.snout = root.getChild("snout"); this.head = root.getChild("head");
        this.jaw = root.getChild("jaw"); this.neck = root.getChild("neck");
        this.body = root.getChild("body"); this.wing1 = root.getChild("wing1");
        this.wing2 = root.getChild("wing2"); this.tail1 = root.getChild("tail1");
        this.tail2 = root.getChild("tail2"); this.tail3 = root.getChild("tail3");
        this.tail4 = root.getChild("tail4"); this.fl11 = root.getChild("fl11");
        this.fl12 = root.getChild("fl12"); this.fl21 = root.getChild("fl21");
        this.fl22 = root.getChild("fl22"); this.bl21 = root.getChild("bl21");
        this.bl22 = root.getChild("bl22"); this.bl11 = root.getChild("bl11");
        this.bl12 = root.getChild("bl12");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("horn1", CubeListBuilder.create().texOffs(90, 0).mirror().addBox(1.0F, -4.0F, 0.0F, 0, 2, 2), PartPose.offset(0.0F, 17.0F, -6.0F));
        root.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(102, 0).mirror().addBox(-1.0F, -4.0F, 0.0F, 0, 2, 2), PartPose.offset(0.0F, 17.0F, -6.0F));
        root.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-2.0F, -1.0F, -4.0F, 4, 1, 4), PartPose.offset(0.0F, 17.0F, -6.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(41, 0).mirror().addBox(-2.0F, -2.0F, -2.0F, 4, 1, 2), PartPose.offset(0.0F, 17.0F, -6.0F));
        root.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(42, 5).mirror().addBox(-2.0F, 0.0F, -4.0F, 4, 1, 4), PartPose.offset(0.0F, 17.0F, -6.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 5, 2), PartPose.offsetAndRotation(-1.0F, 18.0F, -2.0F, -2.082002F, 0.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(38, 16).mirror().addBox(0.0F, 0.0F, 0.0F, 2, 3, 10), PartPose.offset(-1.0F, 17.0F, -4.0F));
        root.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(36, 37).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 11, 15), PartPose.offsetAndRotation(0.0F, 18.0F, -1.0F, -0.3490659F, 0.0F, -2.356194F));
        root.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(0, 37).mirror().addBox(0.0F, 0.0F, 0.0F, 0, 11, 15), PartPose.offsetAndRotation(0.0F, 18.0F, -1.0F, -0.3490659F, 0.0F, 2.356194F));
        root.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(14, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 6), PartPose.offsetAndRotation(-0.5F, 17.0F, 6.0F, -0.5235988F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(14, 8).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 6), PartPose.offset(-0.5F, 20.0F, 11.0F));
        root.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(17, 16).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 1, 4), PartPose.offsetAndRotation(-0.5F, 20.0F, 17.0F, 0.0F, -0.6320364F, 0.0F));
        root.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(16, 23).mirror().addBox(-1.0F, 0.5F, 4.0F, 3, 0, 2), PartPose.offsetAndRotation(-0.5F, 20.0F, 17.0F, 0.0F, -0.6320364F, 0.0F));
        root.addOrReplaceChild("fl11", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offsetAndRotation(-2.0F, 19.0F, -4.0F, 0.3490659F, 0.0F, 0.1745329F));
        root.addOrReplaceChild("fl12", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-0.5F, 1.0F, 1.0F, 1, 2, 1), PartPose.offsetAndRotation(-2.0F, 19.0F, -4.0F, -0.2617994F, 0.0F, 0.0F));
        root.addOrReplaceChild("fl21", CubeListBuilder.create().texOffs(5, 9).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offsetAndRotation(1.0F, 19.0F, -4.0F, 0.3490659F, 0.0F, -0.1745329F));
        root.addOrReplaceChild("fl22", CubeListBuilder.create().texOffs(5, 13).mirror().addBox(0.5F, 1.0F, 1.0F, 1, 2, 1), PartPose.offsetAndRotation(1.0F, 19.0F, -4.0F, -0.2617994F, 0.0F, 0.0F));
        root.addOrReplaceChild("bl21", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offsetAndRotation(1.0F, 18.0F, 4.0F, -0.3490659F, 0.0F, -0.1745329F));
        root.addOrReplaceChild("bl22", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(0.5F, 2.0F, -1.0F, 1, 2, 1), PartPose.offsetAndRotation(1.0F, 18.0F, 4.0F, 0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("bl11", CubeListBuilder.create().texOffs(5, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 1, 2, 1), PartPose.offsetAndRotation(-2.0F, 18.0F, 4.0F, -0.3490659F, 0.0F, 0.1745329F));
        root.addOrReplaceChild("bl12", CubeListBuilder.create().texOffs(5, 22).mirror().addBox(-0.5F, 2.0F, -1.0F, 1, 2, 1), PartPose.offsetAndRotation(-2.0F, 18.0F, 4.0F, 0.1745329F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 119, 72);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
        this.wing1.zRot = -2.0f + wingAngle;
        this.wing2.zRot = 2.0f - wingAngle;
        float jawAngle = Mth.cos(ageInTicks * 0.3f) * (float) Math.PI * 0.1f;
        this.jaw.xRot = Math.abs(jawAngle);
        float legAngle = Mth.cos(ageInTicks * 1.25f) * (float) Math.PI * 0.35f;
        this.fl21.xRot = 0.349f + legAngle;
        this.fl11.xRot = 0.349f - legAngle;
        this.bl21.xRot = -0.349f - legAngle;
        this.bl11.xRot = -0.349f + legAngle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        horn1.render(ps, vc, light, overlay, color); horn2.render(ps, vc, light, overlay, color);
        snout.render(ps, vc, light, overlay, color); head.render(ps, vc, light, overlay, color);
        jaw.render(ps, vc, light, overlay, color); neck.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color); wing1.render(ps, vc, light, overlay, color);
        wing2.render(ps, vc, light, overlay, color); tail1.render(ps, vc, light, overlay, color);
        tail2.render(ps, vc, light, overlay, color); tail3.render(ps, vc, light, overlay, color);
        tail4.render(ps, vc, light, overlay, color); fl11.render(ps, vc, light, overlay, color);
        fl12.render(ps, vc, light, overlay, color); fl21.render(ps, vc, light, overlay, color);
        fl22.render(ps, vc, light, overlay, color); bl21.render(ps, vc, light, overlay, color);
        bl22.render(ps, vc, light, overlay, color); bl11.render(ps, vc, light, overlay, color);
        bl12.render(ps, vc, light, overlay, color);
    }
}
