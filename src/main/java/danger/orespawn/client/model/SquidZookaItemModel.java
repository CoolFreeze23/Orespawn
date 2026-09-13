package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelSquidZooka.java:
 * 12 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 12 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class SquidZookaItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("barrel", CubeListBuilder.create()
            .texOffs(29, 19)
            .addBox(-1.0F, -1.0F, -19.0F, 2, 2, 34),
            PartPose.ZERO);

        root.addOrReplaceChild("tail1", CubeListBuilder.create()
            .texOffs(0, 53)
            .addBox(-1.5F, -1.5F, 15.0F, 3, 3, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail2", CubeListBuilder.create()
            .texOffs(0, 58)
            .addBox(-2.0F, -2.0F, 16.0F, 4, 4, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail3", CubeListBuilder.create()
            .texOffs(0, 64)
            .addBox(-2.5F, -2.5F, 17.0F, 5, 5, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail4", CubeListBuilder.create()
            .texOffs(0, 71)
            .addBox(-3.0F, -3.0F, 18.0F, 6, 6, 6),
            PartPose.ZERO);

        root.addOrReplaceChild("tail5", CubeListBuilder.create()
            .texOffs(0, 84)
            .addBox(-2.5F, -2.5F, 24.0F, 5, 5, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail6", CubeListBuilder.create()
            .texOffs(0, 91)
            .addBox(-2.0F, -2.0F, 25.0F, 4, 4, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("tail7", CubeListBuilder.create()
            .texOffs(0, 97)
            .addBox(-1.5F, -1.5F, 26.0F, 3, 3, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("sight3", CubeListBuilder.create()
            .texOffs(25, 0)
            .addBox(1.0F, -2.0F, -10.0F, 1, 1, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("sight2", CubeListBuilder.create()
            .texOffs(32, 0)
            .addBox(0.5F, -4.0F, -12.0F, 2, 2, 6),
            PartPose.ZERO);

        root.addOrReplaceChild("sight1", CubeListBuilder.create()
            .texOffs(18, 0)
            .addBox(1.0F, -1.0F, -10.0F, 1, 1, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(0.0F, 1.0F, 0.0F, 1, 7, 1),
            PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }
}
