package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Island;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The 1.7.10 Island (orig ModelIsland.java, ENT-S-091): three 8x8x8 cubes at
 * one pivot, two of them pre-rotated 45 degrees on every axis, each tumbling
 * on all three axes at its own slow cosine. The port had replaced it with a
 * body/head/legs rig that never existed in 1.7.10. Geometry generated from
 * the parsed original by tools/reference_to_layer_definition.py and proven
 * by the reference-geometry leg; wingspeed 1.0 (ClientProxyOreSpawn.java:454).
 */
public class ModelIsland extends EntityModel<Island> {
    /** orig ClientProxyOreSpawn.java:454: new ModelIsland(1.0f). */
    public static final float WINGSPEED = 1.0F;

    private final ModelPart shape1;
    private final ModelPart shape2;
    private final ModelPart shape3;

    public ModelIsland(ModelPart root) {
        this.shape1 = root.getChild("Shape1");
        this.shape2 = root.getChild("Shape2");
        this.shape3 = root.getChild("Shape3");
    }

    public static LayerDefinition createBodyLayer() {
        return islandLayer();
    }

    /** Shared with ModelIslandToo: 1.7.10 registered IslandToo with this same ModelIsland (ClientProxyOreSpawn.java:455). */
    static LayerDefinition islandLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("Shape1",
                CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("Shape2",
                CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.7853982F, 0.7853982F, 0.7853982F));
        root.addOrReplaceChild("Shape3",
                CubeListBuilder.create()
                .texOffs(32, 16).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, 0.7853982F, 0.7853982F, 0.7853982F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Island entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelIsland.render: nine cosines, frequencies 0.05..0.058 * wingspeed, amplitude PI
        this.shape1.xRot = Mth.cos(ageInTicks * 0.05F * WINGSPEED) * (float) Math.PI;
        this.shape1.yRot = Mth.cos(ageInTicks * 0.051F * WINGSPEED) * (float) Math.PI;
        this.shape1.zRot = Mth.cos(ageInTicks * 0.052F * WINGSPEED) * (float) Math.PI;
        this.shape2.xRot = Mth.cos(ageInTicks * 0.053F * WINGSPEED) * (float) Math.PI;
        this.shape2.yRot = Mth.cos(ageInTicks * 0.054F * WINGSPEED) * (float) Math.PI;
        this.shape2.zRot = Mth.cos(ageInTicks * 0.055F * WINGSPEED) * (float) Math.PI;
        this.shape3.xRot = Mth.cos(ageInTicks * 0.056F * WINGSPEED) * (float) Math.PI;
        this.shape3.yRot = Mth.cos(ageInTicks * 0.057F * WINGSPEED) * (float) Math.PI;
        this.shape3.zRot = Mth.cos(ageInTicks * 0.058F * WINGSPEED) * (float) Math.PI;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.shape3.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
