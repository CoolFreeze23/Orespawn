package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Coin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The 1.7.10 coin (orig ModelCoin.java, BUG-040): one 256x256x1 quad at
 * texture offset (0, 0) on the 512x512 sheet, rotation point (0, -109, 0),
 * drawn by RenderCoin at 0.125 scale. The original sets {@code mirror} AFTER
 * {@code addBox}; 1.7.10's ModelBox reads the flag in its constructor, so the
 * quad is NOT mirrored. Yaw is a slow cosine oscillation, not a spin:
 * {@code cos(ageInTicks * 0.05 * wingspeed) * PI} with wingspeed 0.22
 * (ClientProxyOreSpawn.java:491, {@code new ModelCoin(0.22f)}).
 */
public class ModelCoin extends EntityModel<Coin> {
    /** orig ClientProxyOreSpawn.java:491: new ModelCoin(0.22f). */
    private static final float WINGSPEED = 0.22F;

    private final ModelPart coin;

    public ModelCoin(ModelPart root) {
        this.coin = root.getChild("coin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // orig: Shape1 = new ModelRenderer(this, 0, 0); addBox(-128, -128, 0, 256, 256, 1);
        //       setRotationPoint(0, -109, 0); setTextureSize(512, 512); mirror = true (inert, see class doc)
        root.addOrReplaceChild("coin",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-128.0F, -128.0F, 0.0F, 256.0F, 256.0F, 1.0F),
                PartPose.offset(0.0F, -109.0F, 0.0F));
        return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(Coin entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // orig ModelCoin.render: Shape1.rotateAngleY = MathHelper.cos(f2 * 0.05f * wingspeed) * PI
        this.coin.yRot = Mth.cos(ageInTicks * 0.05F * WINGSPEED) * (float) Math.PI;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        this.coin.render(ps, vc, light, overlay, color);
    }
}
