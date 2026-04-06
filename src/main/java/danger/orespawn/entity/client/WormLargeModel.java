package danger.orespawn.entity.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityWormLarge;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
public class WormLargeModel<T extends EntityWormLarge> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart body2;
    private final ModelPart head;
    private final ModelPart jaw;
    public WormLargeModel(ModelPart root) {
        this.body = root.getChild("body");
        this.body2 = root.getChild("body2");
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -4.0F, 8, 16, 8), PartPose.offset(0.0F, 8.0F, 0.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(33, 0).addBox(-3.0F, 0.0F, -3.0F, 6, 10, 6), PartPose.offset(0.0F, -2.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 25).addBox(-5.0F, -8.0F, -5.0F, 10, 8, 10), PartPose.offset(0.0F, -2.0F, 0.0F));
        root.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 44).addBox(-4.0F, 0.0F, -6.0F, 8, 3, 6), PartPose.offset(0.0F, -2.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.jaw.yRot = this.head.yRot;
        float jawAngle = Mth.cos(ageInTicks * 0.3f) * 0.15f;
        this.jaw.xRot = Math.abs(jawAngle);
    }
    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        body2.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        jaw.render(ps, vc, light, overlay, color);
    }
}
