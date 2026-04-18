package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.HoverboardEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Flat oblong board mesh for the {@link HoverboardEntity}. Modelled after the
 * 1.7.10 Hoverboard sprite — a single rounded plate with a slightly thicker
 * trailing fin so the rear is visually distinguishable from the front.
 */
public class HoverboardModel extends EntityModel<HoverboardEntity> {

    private final ModelPart deck;
    private final ModelPart fin;

    public HoverboardModel(ModelPart root) {
        this.deck = root.getChild("deck");
        this.fin = root.getChild("fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("deck",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-6.0F, 0.0F, -10.0F, 12, 1, 20),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("fin",
                CubeListBuilder.create().texOffs(0, 22).mirror()
                        .addBox(-2.0F, -1.0F, 8.0F, 4, 2, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(HoverboardEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Static board — no per-tick animation. Yaw/pitch are applied to the
        // whole entity transform by the renderer.
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer buf, int packedLight,
                                int packedOverlay, int color) {
        deck.render(stack, buf, packedLight, packedOverlay, color);
        fin.render(stack, buf, packedLight, packedOverlay, color);
    }
}
