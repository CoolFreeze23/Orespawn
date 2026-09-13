package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelBertha.java:
 * 12 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 12 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class BerthaItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("grip", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(0.0F, -6.0F, 0.0F, 1, 12, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("blade", CubeListBuilder.create()
            .texOffs(6, 0)
            .addBox(0.0F, -41.0F, -1.0F, 1, 34, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("handguard2", CubeListBuilder.create()
            .texOffs(16, 0)
            .addBox(0.0F, -7.0F, -4.0F, 1, 1, 9),
            PartPose.ZERO);

        root.addOrReplaceChild("handguard1", CubeListBuilder.create()
            .texOffs(18, 12)
            .addBox(-3.0F, -7.0F, 0.0F, 7, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg2", CubeListBuilder.create()
            .texOffs(0, 15)
            .addBox(0.0F, -8.0F, -5.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg4", CubeListBuilder.create()
            .texOffs(0, 18)
            .addBox(0.0F, -8.0F, 5.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg3", CubeListBuilder.create()
            .texOffs(0, 21)
            .addBox(-4.0F, -8.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("hg1", CubeListBuilder.create()
            .texOffs(0, 24)
            .addBox(4.0F, -8.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("base_grip", CubeListBuilder.create()
            .texOffs(0, 39)
            .addBox(-1.0F, 5.0F, -1.0F, 3, 1, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("tip1", CubeListBuilder.create()
            .texOffs(21, 16)
            .addBox(0.0F, -42.0F, -0.5F, 1, 1, 2),
            PartPose.ZERO);

        root.addOrReplaceChild("tip2", CubeListBuilder.create()
            .texOffs(22, 20)
            .addBox(0.0F, -43.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(0, 45)
            .addBox(0.0F, 6.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 128);
    }
}
