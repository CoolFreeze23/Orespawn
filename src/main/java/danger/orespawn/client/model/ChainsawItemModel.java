package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ChainsawItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("engine", CubeListBuilder.create()
            .texOffs(0, 19).mirror()
            .addBox(-2.0F, -4.0F, -4.0F, 4, 7, 8),
            PartPose.ZERO);

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(49, 0).mirror()
            .addBox(0.0F, -3.0F, 3.0F, 1, 1, 5),
            PartPose.rotation(-0.1919862F, 0.0F, 0.0F));

        root.addOrReplaceChild("handle2", CubeListBuilder.create()
            .texOffs(50, 13).mirror()
            .addBox(0.0F, 2.0F, 4.0F, 1, 1, 4),
            PartPose.ZERO);

        root.addOrReplaceChild("handle3", CubeListBuilder.create()
            .texOffs(52, 7).mirror()
            .addBox(0.0F, -2.0F, 7.0F, 1, 4, 1),
            PartPose.rotation(-0.0872665F, 0.0F, 0.0F));

        root.addOrReplaceChild("muffler", CubeListBuilder.create()
            .texOffs(14, 0).mirror()
            .addBox(-3.0F, 0.0F, 1.0F, 1, 3, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("blade1", CubeListBuilder.create()
            .texOffs(0, 35).mirror()
            .addBox(0.0F, -2.0F, -28.0F, 1, 4, 24),
            PartPose.ZERO);

        root.addOrReplaceChild("blade2", CubeListBuilder.create()
            .texOffs(0, 8).mirror()
            .addBox(-2.5F, -2.5F, -2.5F, 1, 5, 5),
            PartPose.offset(0.0F, 0.0F, -28.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
