package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntitySpyro;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class SpyroModel<T extends EntitySpyro> extends EntityModel<T> {
    private final ModelPart torso, headBottom, headTop, neck;
    private final ModelPart wingLeft, wingRight;
    private final ModelPart legRFTop, legRFBot, legLFTop, legLFBot;
    private final ModelPart legRBTop, legRBBot, legLBTop, legLBBot;
    private final ModelPart tailBack, tailFront;
    private final ModelPart jawPiece;

    public SpyroModel(ModelPart root) {
        this.torso = root.getChild("torso");
        this.headBottom = root.getChild("headbottom");
        this.headTop = root.getChild("headtop");
        this.neck = root.getChild("neck");
        this.wingLeft = root.getChild("wingleft");
        this.wingRight = root.getChild("wingright");
        this.legRFTop = root.getChild("legrftop");
        this.legRFBot = root.getChild("legrfbot");
        this.legLFTop = root.getChild("leglftop");
        this.legLFBot = root.getChild("leglfbot");
        this.legRBTop = root.getChild("legrbtop");
        this.legRBBot = root.getChild("legrbbot");
        this.legLBTop = root.getChild("leglbtop");
        this.legLBBot = root.getChild("leglbbot");
        this.tailBack = root.getChild("tailback");
        this.tailFront = root.getChild("tailfront");
        this.jawPiece = root.getChild("jawpiece");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, -2.0F, -5.0F, 5, 4, 10), PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("headbottom", CubeListBuilder.create().texOffs(30, 7).mirror().addBox(-3.0F, -2.0F, -5.0F, 5, 2, 6), PartPose.offset(1.0F, 16.0F, -3.0F));
        root.addOrReplaceChild("headtop", CubeListBuilder.create().texOffs(30, 0).mirror().addBox(-3.0F, -5.0F, -3.0F, 5, 3, 4), PartPose.offset(1.0F, 16.0F, -3.0F));
        root.addOrReplaceChild("jawpiece", CubeListBuilder.create().texOffs(52, 0).mirror().addBox(-2.0F, -1.0F, -4.0F, 3, 1, 3), PartPose.offsetAndRotation(1.0F, 16.0F, -3.0F, 0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(52, 7).mirror().addBox(-1.0F, -2.0F, -1.0F, 3, 3, 3), PartPose.offsetAndRotation(0.0F, 17.0F, -4.0F, 0.4537856F, 0.0F, 0.0F));
        root.addOrReplaceChild("wingleft", CubeListBuilder.create().texOffs(2, 51).mirror().addBox(-10.0F, -1.0F, -2.0F, 10, 0, 4), PartPose.offsetAndRotation(-1.0F, 16.0F, 0.0F, 0.1745329F, 0.0F, -0.1745329F));
        root.addOrReplaceChild("wingright", CubeListBuilder.create().texOffs(2, 45).mirror().addBox(0.0F, -1.0F, -2.0F, 10, 0, 4), PartPose.offsetAndRotation(2.0F, 16.0F, 0.0F, 0.1745329F, 0.0F, 0.1745329F));
        root.addOrReplaceChild("legrftop", CubeListBuilder.create().texOffs(20, 19).mirror().addBox(0.0F, 0.0F, -2.0F, 2, 3, 3), PartPose.offsetAndRotation(3.0F, 18.0F, -2.0F, -0.0872665F, 0.0F, 0.0F));
        root.addOrReplaceChild("legrfbot", CubeListBuilder.create().texOffs(0, 25).mirror().addBox(0.0F, 2.0F, -1.5F, 2, 4, 2), PartPose.offsetAndRotation(3.0F, 18.0F, -2.0F, -0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("leglftop", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-2.0F, 0.0F, -1.0F, 2, 3, 3), PartPose.offsetAndRotation(-2.0F, 18.0F, -3.0F, -0.0872665F, 0.0F, 0.0F));
        root.addOrReplaceChild("leglfbot", CubeListBuilder.create().texOffs(8, 25).mirror().addBox(-2.0F, 2.0F, -0.5F, 2, 4, 2), PartPose.offsetAndRotation(-2.0F, 18.0F, -3.0F, -0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("legrbtop", CubeListBuilder.create().texOffs(30, 19).mirror().addBox(0.0F, 0.0F, -2.0F, 2, 3, 3), PartPose.offsetAndRotation(3.0F, 18.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("legrbbot", CubeListBuilder.create().texOffs(16, 25).mirror().addBox(0.0F, 2.0F, -1.0F, 2, 4, 2), PartPose.offsetAndRotation(3.0F, 18.0F, 3.0F, -0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("leglbtop", CubeListBuilder.create().texOffs(10, 19).mirror().addBox(-2.0F, 0.0F, -2.0F, 2, 3, 3), PartPose.offsetAndRotation(-2.0F, 18.0F, 3.0F, 0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("leglbbot", CubeListBuilder.create().texOffs(24, 25).mirror().addBox(-2.0F, 2.0F, -1.0F, 2, 4, 2), PartPose.offsetAndRotation(-2.0F, 18.0F, 3.0F, -0.1745329F, 0.0F, 0.0F));
        root.addOrReplaceChild("tailback", CubeListBuilder.create().texOffs(0, 36).mirror().addBox(-1.0F, -1.0F, -1.0F, 2, 2, 4), PartPose.offsetAndRotation(0.5F, 17.5F, 5.0F, 0.4537856F, 0.0F, 0.0F));
        root.addOrReplaceChild("tailfront", CubeListBuilder.create().texOffs(12, 36).mirror().addBox(0.0F, 0.0F, -1.0F, 1, 1, 4), PartPose.offsetAndRotation(0.0F, 16.0F, 7.0F, 0.2617994F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wingAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 2.3f) * (float) Math.PI * 0.4f * limbSwingAmount : 0.0f;
        this.wingLeft.zRot = wingAngle - 0.1745329f;
        this.wingRight.zRot = -wingAngle + 0.1745329f;
        float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 2.0f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        this.legRFTop.xRot = legAngle - 0.087f;
        this.legLFTop.xRot = -legAngle - 0.087f;
        this.legRBTop.xRot = -legAngle + 0.139f;
        this.legLBTop.xRot = legAngle + 0.139f;
        this.headBottom.yRot = (float) Math.toRadians(netHeadYaw);
        this.headTop.yRot = this.headBottom.yRot;
        this.jawPiece.yRot = this.headBottom.yRot;
        float tailAngle = Mth.cos(ageInTicks * 1.2f) * (float) Math.PI * 0.25f;
        this.tailBack.yRot = tailAngle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        torso.render(ps, vc, light, overlay, color);
        headBottom.render(ps, vc, light, overlay, color);
        headTop.render(ps, vc, light, overlay, color);
        jawPiece.render(ps, vc, light, overlay, color);
        neck.render(ps, vc, light, overlay, color);
        wingLeft.render(ps, vc, light, overlay, color);
        wingRight.render(ps, vc, light, overlay, color);
        legRFTop.render(ps, vc, light, overlay, color);
        legRFBot.render(ps, vc, light, overlay, color);
        legLFTop.render(ps, vc, light, overlay, color);
        legLFBot.render(ps, vc, light, overlay, color);
        legRBTop.render(ps, vc, light, overlay, color);
        legRBBot.render(ps, vc, light, overlay, color);
        legLBTop.render(ps, vc, light, overlay, color);
        legLBBot.render(ps, vc, light, overlay, color);
        tailBack.render(ps, vc, light, overlay, color);
        tailFront.render(ps, vc, light, overlay, color);
    }
}
