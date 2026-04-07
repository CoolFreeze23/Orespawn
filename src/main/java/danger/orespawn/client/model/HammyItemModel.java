package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class HammyItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(0, 0).mirror()
            .addBox(-0.5F, -12.0F, -1.0F, 1, 36, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("handle2", CubeListBuilder.create()
            .texOffs(7, 0).mirror()
            .addBox(-0.5F, -12.0F, -1.0F, 1, 36, 2),
            PartPose.rotation(0.0F, 1.047198F, 0.0F));

        root.addOrReplaceChild("handle3", CubeListBuilder.create()
            .texOffs(14, 0).mirror()
            .addBox(-0.5F, -12.0F, -1.0F, 1, 36, 2),
            PartPose.rotation(0.0F, -1.047198F, 0.0F));

        root.addOrReplaceChild("head1", CubeListBuilder.create()
            .texOffs(0, 230).mirror()
            .addBox(-20.0F, -22.0F, -7.0F, 40, 6, 14),
            PartPose.ZERO);

        root.addOrReplaceChild("head2", CubeListBuilder.create()
            .texOffs(0, 184).mirror()
            .addBox(-20.0F, -26.0F, -3.0F, 40, 14, 6),
            PartPose.ZERO);

        root.addOrReplaceChild("head3", CubeListBuilder.create()
            .texOffs(0, 161).mirror()
            .addBox(-20.0F, -16.5F, 6.4F, 40, 6, 14),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("head4", CubeListBuilder.create()
            .texOffs(0, 207).mirror()
            .addBox(-20.0F, -16.5F, -20.4F, 40, 6, 14),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band1", CubeListBuilder.create()
            .texOffs(0, 88).mirror()
            .addBox(12.0F, -22.5F, -8.0F, 5, 7, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("band2", CubeListBuilder.create()
            .texOffs(0, 128).mirror()
            .addBox(12.0F, -22.5F, 7.0F, 5, 7, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("band3", CubeListBuilder.create()
            .texOffs(0, 98).mirror()
            .addBox(12.0F, -17.0F, 5.4F, 5, 7, 1),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band4", CubeListBuilder.create()
            .texOffs(0, 118).mirror()
            .addBox(12.0F, -16.9F, -6.4F, 5, 7, 1),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band5", CubeListBuilder.create()
            .texOffs(0, 108).mirror()
            .addBox(12.0F, -12.0F, -3.5F, 5, 1, 7),
            PartPose.ZERO);

        root.addOrReplaceChild("band6", CubeListBuilder.create()
            .texOffs(0, 79).mirror()
            .addBox(12.0F, -16.5F, -21.4F, 5, 6, 1),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band7", CubeListBuilder.create()
            .texOffs(0, 138).mirror()
            .addBox(12.0F, -17.0F, 20.4F, 5, 7, 1),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band8", CubeListBuilder.create()
            .texOffs(0, 148).mirror()
            .addBox(12.0F, -27.0F, -3.5F, 5, 1, 7),
            PartPose.ZERO);

        root.addOrReplaceChild("point1", CubeListBuilder.create()
            .texOffs(28, 130).mirror()
            .addBox(-2.5F, -29.5F, -0.5F, 5, 5, 1),
            PartPose.rotation(0.0F, 0.0F, 0.7853982F));

        root.addOrReplaceChild("spike1", CubeListBuilder.create()
            .texOffs(67, 0).mirror()
            .addBox(14.0F, -20.0F, -10.0F, 1, 1, 20),
            PartPose.ZERO);

        root.addOrReplaceChild("spike2", CubeListBuilder.create()
            .texOffs(49, 0).mirror()
            .addBox(14.0F, -29.0F, 0.0F, 1, 20, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("spike3", CubeListBuilder.create()
            .texOffs(55, 0).mirror()
            .addBox(14.0F, -23.5F, 13.0F, 1, 20, 1),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("spike4", CubeListBuilder.create()
            .texOffs(61, 0).mirror()
            .addBox(-15.0F, -23.5F, -14.0F, 1, 20, 1),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band1b", CubeListBuilder.create()
            .texOffs(0, 88).mirror()
            .addBox(-17.0F, -22.5F, -8.0F, 5, 7, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("band2b", CubeListBuilder.create()
            .texOffs(0, 128).mirror()
            .addBox(-17.0F, -22.5F, 7.0F, 5, 7, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("band3b", CubeListBuilder.create()
            .texOffs(0, 98).mirror()
            .addBox(-17.0F, -17.0F, 5.4F, 5, 7, 1),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band4b", CubeListBuilder.create()
            .texOffs(0, 118).mirror()
            .addBox(-17.0F, -16.9F, -6.4F, 5, 7, 1),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band5b", CubeListBuilder.create()
            .texOffs(0, 108).mirror()
            .addBox(-17.0F, -12.0F, -3.5F, 5, 1, 7),
            PartPose.ZERO);

        root.addOrReplaceChild("band6b", CubeListBuilder.create()
            .texOffs(0, 79).mirror()
            .addBox(-17.0F, -16.5F, -21.4F, 5, 6, 1),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band7b", CubeListBuilder.create()
            .texOffs(0, 138).mirror()
            .addBox(-17.0F, -17.0F, 20.4F, 5, 7, 1),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("band8b", CubeListBuilder.create()
            .texOffs(0, 148).mirror()
            .addBox(-17.0F, -27.0F, -3.5F, 5, 1, 7),
            PartPose.ZERO);

        root.addOrReplaceChild("point1b", CubeListBuilder.create()
            .texOffs(28, 130).mirror()
            .addBox(-29.5F, -2.5F, -0.5F, 5, 5, 1),
            PartPose.rotation(0.0F, 0.0F, 0.7853982F));

        root.addOrReplaceChild("spike1b", CubeListBuilder.create()
            .texOffs(67, 0).mirror()
            .addBox(-15.0F, -20.0F, -10.0F, 1, 1, 20),
            PartPose.ZERO);

        root.addOrReplaceChild("spike2b", CubeListBuilder.create()
            .texOffs(49, 0).mirror()
            .addBox(-15.0F, -29.0F, 0.0F, 1, 20, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("spike3b", CubeListBuilder.create()
            .texOffs(55, 0).mirror()
            .addBox(-15.0F, -23.5F, 13.0F, 1, 20, 1),
            PartPose.rotation(0.7853982F, 0.0F, 0.0F));

        root.addOrReplaceChild("spike4b", CubeListBuilder.create()
            .texOffs(61, 0).mirror()
            .addBox(14.0F, -23.5F, -14.0F, 1, 20, 1),
            PartPose.rotation(-0.7853982F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 256);
    }
}
