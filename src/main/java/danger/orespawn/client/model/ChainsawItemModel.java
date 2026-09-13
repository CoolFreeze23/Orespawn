package danger.orespawn.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
/**
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelChainsaw.java:
 * 8 stores (the eighth on the {@code tooth} part the port lacks - a geometry note, not a drop), all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 7 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */

public class ChainsawItemModel {
    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("engine", CubeListBuilder.create()
            .texOffs(0, 19)
            .addBox(-2.0F, -4.0F, -4.0F, 4, 7, 8),
            PartPose.ZERO);

        root.addOrReplaceChild("handle1", CubeListBuilder.create()
            .texOffs(49, 0)
            .addBox(0.0F, -3.0F, 3.0F, 1, 1, 5),
            PartPose.rotation(-0.1919862F, 0.0F, 0.0F));

        root.addOrReplaceChild("handle2", CubeListBuilder.create()
            .texOffs(50, 13)
            .addBox(0.0F, 2.0F, 4.0F, 1, 1, 4),
            PartPose.ZERO);

        root.addOrReplaceChild("handle3", CubeListBuilder.create()
            .texOffs(52, 7)
            .addBox(0.0F, -2.0F, 7.0F, 1, 4, 1),
            PartPose.rotation(-0.0872665F, 0.0F, 0.0F));

        root.addOrReplaceChild("muffler", CubeListBuilder.create()
            .texOffs(14, 0)
            .addBox(-3.0F, 0.0F, 1.0F, 1, 3, 3),
            PartPose.ZERO);

        root.addOrReplaceChild("blade1", CubeListBuilder.create()
            .texOffs(0, 35)
            .addBox(0.0F, -2.0F, -28.0F, 1, 4, 24),
            PartPose.ZERO);

        root.addOrReplaceChild("blade2", CubeListBuilder.create()
            .texOffs(0, 8)
            .addBox(-2.5F, -2.5F, -2.5F, 1, 5, 5),
            PartPose.offset(0.0F, 0.0F, -28.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
