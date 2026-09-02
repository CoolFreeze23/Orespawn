package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityMosquito;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The 1.7.10 Mosquito (orig ModelMosquito.java, ENT-S-091): a 1x1x8 body and
 * two wing pairs on a 32x32 sheet. The port had a hand-authored four-part rig.
 * Geometry generated from the parsed original; proven by the reference-geometry
 * leg. Reference render order: body, leftwing1, rightwing1, leftwing2, rightwing2.
 */
public class MosquitoModel<T extends EntityMosquito> extends EntityModel<T> {
    private final ModelPart body;
    private final ModelPart leftwing1;
    private final ModelPart rightwing1;
    private final ModelPart leftwing2;
    private final ModelPart rightwing2;

    public MosquitoModel(ModelPart root) {
        this.body = root.getChild("body");
        this.leftwing1 = root.getChild("leftwing1");
        this.rightwing1 = root.getChild("rightwing1");
        this.leftwing2 = root.getChild("leftwing2");
        this.rightwing2 = root.getChild("rightwing2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                .texOffs(8, 18).addBox(0.0F, 0.0F, -2.0F, 1.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("leftwing1",
                CubeListBuilder.create()
                .texOffs(16, 13).addBox(1.0F, 0.0F, -1.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(1.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("rightwing1",
                CubeListBuilder.create()
                .texOffs(2, 13).addBox(-4.0F, 0.0F, -1.0F, 3.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("leftwing2",
                CubeListBuilder.create()
                .texOffs(15, 8).addBox(0.0F, 0.0F, 0.0F, 5.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("rightwing2",
                CubeListBuilder.create()
                .texOffs(2, 8).addBox(-5.0F, 0.0F, 0.0F, 5.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 17.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelMosquito.render: rightwing2.zRot = rightwing1.zRot = cos(f2*3)*PI*0.25; left wings the negative
        float flap = Mth.cos(ageInTicks * 3.0F) * (float) Math.PI * 0.25F;
        this.rightwing1.zRot = flap;
        this.rightwing2.zRot = flap;
        this.leftwing1.zRot = -flap;
        this.leftwing2.zRot = -flap;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.body.render(ps, vc, light, overlay, color);
        this.leftwing1.render(ps, vc, light, overlay, color);
        this.rightwing1.render(ps, vc, light, overlay, color);
        this.leftwing2.render(ps, vc, light, overlay, color);
        this.rightwing2.render(ps, vc, light, overlay, color);
    }
}
