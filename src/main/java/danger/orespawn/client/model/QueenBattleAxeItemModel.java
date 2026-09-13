package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelQueenBattleAxe.java:
 * 9 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 9 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class QueenBattleAxeItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-7.0F, -0.5F, 0.0F, 31, 1, 1),
            PartPose.offsetAndRotation(-0.5F, -12.0F, 0.0F, 0.0F, 0.0F, 1.570796F));

        root.addOrReplaceChild("head1", CubeListBuilder.create()
            .texOffs(29, 18)
            .addBox(-2.0F, -4.5F, -0.5F, 3, 4, 2),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("grip", CubeListBuilder.create()
            .texOffs(0, 7)
            .addBox(-1.92F, 13.0F, -0.5F, 2, 11, 2),
            PartPose.offset(0.5F, -12.0F, 0.0F));

        root.addOrReplaceChild("pin", CubeListBuilder.create()
            .texOffs(38, 11)
            .addBox(-1.0F, -3.0F, -1.0F, 1, 1, 3),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        root.addOrReplaceChild("blade1", CubeListBuilder.create()
            .texOffs(70, 0)
            .addBox(-10.0F, -2.0F, 0.0F, 20, 4, 1),
            PartPose.offsetAndRotation(-0.5F, -14.5F, 0.0F, 0.0F, 0.0F, -0.5934119F));

        root.addOrReplaceChild("blade2", CubeListBuilder.create()
            .texOffs(70, 0)
            .addBox(-10.0F, -2.0F, 0.0F, 20, 4, 1),
            PartPose.offsetAndRotation(-0.5F, -14.5F, 0.0F, 0.0F, 0.0F, -0.1919862F));

        root.addOrReplaceChild("blade3", CubeListBuilder.create()
            .texOffs(70, 0)
            .addBox(-10.0F, -2.0F, 0.0F, 20, 4, 1),
            PartPose.offsetAndRotation(-0.5F, -14.5F, 0.0F, 0.0F, 0.0F, 0.2094395F));

        root.addOrReplaceChild("blade4", CubeListBuilder.create()
            .texOffs(70, 0)
            .addBox(-10.0F, -2.0F, 0.0F, 20, 4, 1),
            PartPose.offsetAndRotation(-0.5F, -14.5F, 0.0F, 0.0F, 0.0F, 0.5934119F));

        root.addOrReplaceChild("top", CubeListBuilder.create()
            .texOffs(13, 4)
            .addBox(0.0F, 0.0F, 0.0F, 2, 2, 2),
            PartPose.offset(-1.5F, -21.0F, -0.5F));

        return LayerDefinition.create(mesh, 128, 64);
    }
}
