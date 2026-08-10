package danger.orespawn.entity.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Leonopteryx;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Leonopteryx renderer — draws the bespoke Leon geometry and texture.
 *
 * <p>Asset-audit follow-up (leonopteryx UV mismatch): 1.7.10 has a SINGLE
 * entity — class {@code Leon} registered as "Leonopteryx"
 * (orig OreSpawnMain.java:4377) and rendered through RenderLeon/ModelLeon with
 * the 256x256 {@code Leon.png} (orig RenderLeon.java:20,
 * ClientProxyOreSpawn.java:500). The port duplicated it as EntityLeon +
 * Leonopteryx, and this renderer previously pushed the Leon art through the
 * 64x32 {@link ButterflyModel} UVs, which rendered scrambled. Minimal visual
 * fix: bake the existing Leon layer ({@link LeonRenderer#MODEL_LAYER}, geometry
 * from {@link LeonModel#createBodyLayer}) and use
 * {@code textures/entity/leon.png}; the mis-copied
 * {@code textures/entity/leonopteryx.png} byte-duplicate was deleted.
 * The EntityLeon-vs-Leonopteryx duplicate consolidation is logged
 * separately and NOT performed here.</p>
 */
public class LeonopteryxRenderer extends MobRenderer<Leonopteryx, LeonopteryxRenderer.LeonPoseModel> {

    /** Same art as LeonRenderer — orig RenderLeon.java:20 ("orespawn:Leon.png"). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leon.png");

    public LeonopteryxRenderer(EntityRendererProvider.Context context) {
        // Shadow 3.0f mirrors the port's LeonRenderer for the twin entity
        // (orig shadow was 1.0f * 1.75f — ClientProxyOreSpawn.java:500). No scale
        // override: LeonRenderer renders the same geometry unscaled, and the
        // former 4x butterfly scale would be wrong for the full-size Leon mesh
        // (orig RenderLeon.java:39-41 scaled 1.75x — a LeonRenderer parity gap
        // owned by the consolidation item, mirrored here for port consistency).
        super(context, new LeonPoseModel(context.bakeLayer(LeonRenderer.MODEL_LAYER)), 3.0f);
    }

    @Override
    public boolean shouldRender(Leonopteryx entity, Frustum frustum, double x, double y, double z) {
        // The Leon geometry extends far beyond the registered hitbox; skip
        // frustum culling so the boss doesn't pop out at screen edges.
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Leonopteryx entity) {
        return TEXTURE;
    }

    /**
     * Static-pose stand-in over the baked Leon layer. Needed because
     * {@link LeonModel} is typed {@code EntityModel<EntityLeon>} and its
     * {@code setupAnim} reads EntityLeon-only accessors
     * ({@code getActivity()}/{@code getBeingRidden()}) that {@link Leonopteryx}
     * does not expose, so the class cannot be reused for this entity without
     * touching it. Renders the standing part set only: LeonModel's layer bakes a
     * second "f"-prefixed flying duplicate of every part at the same bind pose
     * (LeonModel.java:465-708), and drawing both sets un-animated would z-fight.
     * Full wing/leg animation returns when the duplicate entities are
     * consolidated onto LeonRenderer (logged separately).
     */
    public static final class LeonPoseModel extends EntityModel<Leonopteryx> {

        /** The standing (non-"f") part set of {@link LeonModel#createBodyLayer}. */
        private static final String[] STANDING_PARTS = {
                "chest", "neck_1", "neck_2", "neck_3", "abdomen", "head", "upper_jaw",
                "bottom_jaw", "chest_ridge", "upper_sail_1", "upper_sail2_", "upper_sail3",
                "lower_sail1", "lower_sail2", "lower_sail_3", "eye_ridge_L", "eye_ridge_R",
                "anntena_1_L", "anntena_1_R", "anntena_2_L", "anntena_2_R",
                "arm_1_L", "arm_2_L", "wing_1_L", "wing_2_L",
                "arm_1_R", "arm_2_R", "wing_1_R", "wing_2_R",
                "leg_1_L", "leg_1_R", "leg_2_L", "leg_2_R", "footL", "footR",
                "wing_3_L", "wing_3_R", "wing_4_L", "wing_4_R",
                "claw_L", "claw_R", "claw_L2", "claw_R_2",
                "wing_5_L", "wing_6_L", "wing_7_L", "wing_5_R", "wing_6_R", "wing_7_R",
        };

        private final List<ModelPart> parts;

        LeonPoseModel(ModelPart root) {
            List<ModelPart> baked = new ArrayList<>(STANDING_PARTS.length);
            for (String name : STANDING_PARTS) {
                baked.add(root.getChild(name));
            }
            this.parts = List.copyOf(baked);
        }

        @Override
        public void setupAnim(Leonopteryx entity, float limbSwing, float limbSwingAmount,
                              float ageInTicks, float netHeadYaw, float headPitch) {
            // Bind pose only — see class Javadoc.
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                                   int packedLight, int packedOverlay, int color) {
            for (ModelPart part : parts) {
                part.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            }
        }
    }
}
