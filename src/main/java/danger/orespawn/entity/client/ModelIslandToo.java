package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.IslandToo;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

/**
 * IslandToo drew the very same ModelIsland in 1.7.10 (ClientProxyOreSpawn.java:455,
 * `new RenderIslandToo(new ModelIsland(1.0f), 0.25f, 1.0f)`); the geometry and
 * animation are ModelIsland's (ENT-S-091), kept as its own class for the entity type.
 */
public class ModelIslandToo extends EntityModel<IslandToo> {
    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;

    public ModelIslandToo(ModelPart root) {
        this.shape1 = root.getChild("Shape1");
        this.shape2 = root.getChild("Shape2");
        this.shape3 = root.getChild("Shape3");
    }

    public static LayerDefinition createBodyLayer() {
        return ModelIsland.islandLayer();
    }

    @Override
    public void setupAnim(IslandToo entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.shape1.xRot = Mth.cos(ageInTicks * 0.05F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape1.yRot = Mth.cos(ageInTicks * 0.051F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape1.zRot = Mth.cos(ageInTicks * 0.052F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape2.xRot = Mth.cos(ageInTicks * 0.053F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape2.yRot = Mth.cos(ageInTicks * 0.054F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape2.zRot = Mth.cos(ageInTicks * 0.055F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape3.xRot = Mth.cos(ageInTicks * 0.056F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape3.yRot = Mth.cos(ageInTicks * 0.057F * ModelIsland.WINGSPEED) * (float) Math.PI;
        this.shape3.zRot = Mth.cos(ageInTicks * 0.058F * ModelIsland.WINGSPEED) * (float) Math.PI;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.shape3.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
