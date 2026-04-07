package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SquidZookaItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("barrel", CubeListBuilder.create()
            .texOffs(29, 19).mirror()
            .addBox(-1.0F, -1.0F, -19.0F, 2, 2, 34),
            PartPose.ZERO);

        root.addOrReplaceChild("tail1", CubeListBuilder.create()
            .texOffs(0, 53).mirror()
            .addBox(-1.5F, -1.5F, 15.0F, 3, 3, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail2", CubeListBuilder.create()
            .texOffs(0, 58).mirror()
            .addBox(-2.0F, -2.0F, 16.0F, 4, 4, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail3", CubeListBuilder.create()
            .texOffs(0, 64).mirror()
            .addBox(-2.5F, -2.5F, 17.0F, 5, 5, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail4", CubeListBuilder.create()
            .texOffs(0, 71).mirror()
            .addBox(-3.0F, -3.0F, 18.0F, 6, 6, 6),
            PartPose.ZERO);

        root.addOrReplaceChild("tail5", CubeListBuilder.create()
            .texOffs(0, 84).mirror()
            .addBox(-2.5F, -2.5F, 24.0F, 5, 5, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail6", CubeListBuilder.create()
            .texOffs(0, 91).mirror()
            .addBox(-2.0F, -2.0F, 25.0F, 4, 4, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail7", CubeListBuilder.create()
            .texOffs(0, 97).mirror()
            .addBox(-1.5F, -1.5F, 26.0F, 3, 3, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("sight3", CubeListBuilder.create()
            .texOffs(25, 0).mirror()
            .addBox(1.0F, -2.0F, -10.0F, 1, 1, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("sight2", CubeListBuilder.create()
            .texOffs(32, 0).mirror()
            .addBox(0.5F, -4.0F, -12.0F, 2, 2, 6),
            PartPose.ZERO);

        root.addOrReplaceChild("sight1", CubeListBuilder.create()
            .texOffs(18, 0).mirror()
            .addBox(1.0F, -1.0F, -10.0F, 1, 1, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(0.0F, 1.0F, 0.0F, 1, 7, 1),
            PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }
}
