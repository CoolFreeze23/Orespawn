package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BattleAxeItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-7.0F, -0.5F, 0.0F, 31, 2, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, 1.570796F));

        root.addOrReplaceChild("head1", CubeListBuilder.create()
            .texOffs(29, 18).mirror()
            .addBox(-2.0F, -4.5F, -0.5F, 3, 4, 2),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("grip", CubeListBuilder.create()
            .texOffs(0, 7).mirror()
            .addBox(-1.92F, 13.0F, -0.5F, 3, 11, 2),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("pin", CubeListBuilder.create()
            .texOffs(38, 11).mirror()
            .addBox(-1.0F, -3.0F, -1.0F, 1, 1, 3),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("top", CubeListBuilder.create()
            .texOffs(24, 11).mirror()
            .addBox(-2.0F, -8.0F, -0.5F, 3, 2, 2),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("blade1", CubeListBuilder.create()
            .texOffs(70, 0).mirror()
            .addBox(6.0F, -8.0F, 0.0F, 3, 10, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, 0.5061455F));

        root.addOrReplaceChild("blade2", CubeListBuilder.create()
            .texOffs(70, 0).mirror()
            .addBox(8.5F, -6.9F, 0.0F, 3, 10, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.5061455F));

        root.addOrReplaceChild("blade3", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-1.5F, -3.0F, 0.0F, 10, 1, 1),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("blade4", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-1.0F, -2.0F, 0.0F, 7, 1, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, 0.5061455F));

        root.addOrReplaceChild("blade5", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.5F, -3.5F, 0.0F, 8, 1, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.5061455F));

        root.addOrReplaceChild("blade6", CubeListBuilder.create()
            .texOffs(70, 0).mirror()
            .addBox(-12.2F, -5.2F, 0.0F, 3, 10, 1),
            PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.0F, 0.0F, 0.5061455F));

        root.addOrReplaceChild("blade7", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-9.9F, -3.0F, 0.0F, 8, 1, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, 0.5061455F));

        root.addOrReplaceChild("blade8", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-10.0F, -3.0F, 0.0F, 10, 1, 1),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("blade9", CubeListBuilder.create()
            .texOffs(70, 0).mirror()
            .addBox(-10.0F, -8.5F, 0.0F, 3, 10, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.5061455F));

        root.addOrReplaceChild("blade10", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-7.0F, -2.5F, 0.0F, 7, 1, 1),
            PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.5061455F));

        return LayerDefinition.create(mesh, 128, 64);
    }
}
