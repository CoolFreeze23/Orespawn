package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Coin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelCoin extends EntityModel<Coin> {
    private final ModelPart coin;

    public ModelCoin(ModelPart root) {
        this.coin = root.getChild("coin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("coin",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -2.0F, 16, 16, 4),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(Coin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.coin.yRot = ageInTicks * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.coin.render(ps, vc, light, overlay, color);
    }
}
