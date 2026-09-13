package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityRubberDucky;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelRubberDucky.java:
 * 8 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 8 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class RubberDuckyModel<T extends EntityRubberDucky> extends EntityModel<T> {
    private final ModelPart bottom;
    private final ModelPart body;
    private final ModelPart back;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart lWing;
    private final ModelPart rWing;

    public RubberDuckyModel(ModelPart root) {
        this.bottom = root.getChild("bottom");
        this.body = root.getChild("body");
        this.back = root.getChild("back");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.beak = root.getChild("beak");
        this.lWing = root.getChild("lwing");
        this.rWing = root.getChild("rwing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, 0.0F, -2.0F, 4, 1, 4), PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 45).addBox(-3.0F, 0.0F, -3.0F, 6, 2, 8), PartPose.offset(0.0F, 21.0F, 0.0F));
        root.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 33).addBox(-3.0F, 0.0F, -3.0F, 6, 1, 10), PartPose.offset(0.0F, 20.0F, 0.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(17, 27).addBox(-1.0F, 0.0F, -1.0F, 2, 1, 2), PartPose.offset(0.0F, 19.0F, -1.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(13, 18).addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4), PartPose.offset(0.0F, 19.0F, -1.0F));
        root.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(0, 21).addBox(-1.5F, -1.0F, -5.0F, 3, 1, 3), PartPose.offset(0.0F, 19.0F, -1.0F));
        root.addOrReplaceChild("lwing", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.5F, 0.0F, 2, 1, 5), PartPose.offset(3.0F, 21.0F, -2.0F));
        root.addOrReplaceChild("rwing", CubeListBuilder.create().texOffs(17, 0).addBox(-2.0F, -0.5F, 0.0F, 2, 1, 5), PartPose.offset(-3.0F, 21.0F, -2.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.beak.yRot = this.head.yRot;
        float wingAngle = Mth.cos(ageInTicks * 1.0f) * (float) Math.PI * 0.15f;
        wingAngle = Math.abs(wingAngle);
        this.lWing.zRot = -wingAngle;
        this.rWing.zRot = wingAngle;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        bottom.render(ps, vc, light, overlay, color);
        body.render(ps, vc, light, overlay, color);
        back.render(ps, vc, light, overlay, color);
        neck.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        beak.render(ps, vc, light, overlay, color);
        lWing.render(ps, vc, light, overlay, color);
        rWing.render(ps, vc, light, overlay, color);
    }
}
