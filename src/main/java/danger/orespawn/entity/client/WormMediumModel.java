package danger.orespawn.entity.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityWormMedium;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
public class WormMediumModel<T extends EntityWormMedium> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    public WormMediumModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4, 20, 4), PartPose.offset(0.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(17, 0).addBox(-3.0F, -5.0F, -3.0F, 6, 5, 6), PartPose.offset(0.0F, 4.0F, 0.0F));
        root.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(17, 12).addBox(-2.0F, -1.0F, -4.0F, 4, 2, 4), PartPose.offset(0.0F, 4.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.jaw.yRot = this.head.yRot;
    }
    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        body.render(ps, vc, light, overlay, color);
        head.render(ps, vc, light, overlay, color);
        jaw.render(ps, vc, light, overlay, color);
    }
}
