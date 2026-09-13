package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelSlice.java:
 * 14 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 14 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class SliceItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("grip", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(0.0F, -6.0F, 0.0F, 1, 12, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("blade1", CubeListBuilder.create()
            .texOffs(6, 49)
            .addBox(0.0F, -41.0F, 0.0F, 1, 34, 3),
            PartPose.offsetAndRotation(0.5F, 0.0F, -2.3F, 0.0F, 0.3490659F, 0.0F));

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
            .addBox(0.0F, -9.0F, -7.0F, 1, 3, 3),
            PartPose.offset(0.5F, 0.0F, 0.0F));

        root.addOrReplaceChild("hg4", CubeListBuilder.create()
            .texOffs(0, 22)
            .addBox(0.0F, -9.0F, 5.0F, 1, 3, 3),
            PartPose.offset(0.5F, 0.0F, 0.0F));

        root.addOrReplaceChild("hg3", CubeListBuilder.create()
            .texOffs(0, 29)
            .addBox(-4.0F, -9.0F, 0.0F, 3, 3, 1),
            PartPose.offset(-2.0F, 0.0F, 0.5F));

        root.addOrReplaceChild("hg1", CubeListBuilder.create()
            .texOffs(0, 34)
            .addBox(4.0F, -9.0F, 0.0F, 3, 3, 1),
            PartPose.offset(0.0F, 0.0F, 0.5F));

        root.addOrReplaceChild("base_grip", CubeListBuilder.create()
            .texOffs(0, 39)
            .addBox(-1.0F, 5.0F, -1.0F, 3, 1, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("bottom", CubeListBuilder.create()
            .texOffs(0, 45)
            .addBox(0.0F, 6.0F, 0.0F, 1, 1, 1),
            PartPose.ZERO);

        root.addOrReplaceChild("blade2", CubeListBuilder.create()
            .texOffs(24, 49)
            .addBox(-1.0F, -41.0F, 0.0F, 1, 34, 3),
            PartPose.offsetAndRotation(0.5F, 0.0F, -2.3F, 0.0F, -0.3490659F, 0.0F));

        root.addOrReplaceChild("blade3", CubeListBuilder.create()
            .texOffs(15, 49)
            .addBox(0.0F, -41.0F, 0.0F, 1, 34, 3),
            PartPose.offsetAndRotation(1.5F, 0.0F, 0.4F, 0.0F, -0.3490659F, 0.0F));

        root.addOrReplaceChild("blade4", CubeListBuilder.create()
            .texOffs(33, 49)
            .addBox(0.0F, -41.0F, 0.0F, 1, 34, 3),
            PartPose.offsetAndRotation(-1.5F, 0.0F, 0.7F, 0.0F, 0.3490659F, 0.0F));

        root.addOrReplaceChild("shape1", CubeListBuilder.create()
            .texOffs(6, 0)
            .addBox(0.0F, -6.0F, 0.0F, 1, 6, 3),
            PartPose.offset(0.5F, -40.0F, -1.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }
}
