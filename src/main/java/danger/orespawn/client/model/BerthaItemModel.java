package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BerthaItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("grip", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, -6.0F, 0.0F, 1, 12, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("blade", CubeListBuilder.create()
            .texOffs(6, 0).mirror()
            .addBox(0.0F, -41.0F, -1.0F, 1, 34, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("handguard2", CubeListBuilder.create()
            .texOffs(16, 0).mirror()
            .addBox(0.0F, -7.0F, -4.0F, 1, 1, 9),
            PartPose.ZERO);

        root.addOrReplaceChild("handguard1", CubeListBuilder.create()
            .texOffs(18, 12).mirror()
            .addBox(-3.0F, -7.0F, 0.0F, 7, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg2", CubeListBuilder.create()
            .texOffs(0, 15).mirror()
            .addBox(0.0F, -8.0F, -5.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg4", CubeListBuilder.create()
            .texOffs(0, 18).mirror()
            .addBox(0.0F, -8.0F, 5.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg3", CubeListBuilder.create()
            .texOffs(0, 21).mirror()
            .addBox(-4.0F, -8.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg1", CubeListBuilder.create()
            .texOffs(0, 24).mirror()
            .addBox(4.0F, -8.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("base_grip", CubeListBuilder.create()
            .texOffs(0, 39).mirror()
            .addBox(-1.0F, 5.0F, -1.0F, 3, 1, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("tip1", CubeListBuilder.create()
            .texOffs(21, 16).mirror()
            .addBox(0.0F, -42.0F, -0.5F, 1, 1, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("tip2", CubeListBuilder.create()
            .texOffs(22, 20).mirror()
            .addBox(0.0F, -43.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(0, 45).mirror()
            .addBox(0.0F, 6.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 128);
    }
}
