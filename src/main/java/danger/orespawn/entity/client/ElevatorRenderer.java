package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Elevator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Hoverboard renderer (orig RenderElevator.java). Texture follows the
 * board's synced paint color 1..10 (orig Elevator.java:45-54, 73-107 —
 * cycled by Ultimate Sword clicks); a recent hit rocks the board around X
 * by {@code sin(t) * t * damage / 10} in the synced forward direction
 * (orig RenderElevator.java:31-38, the vanilla boat wobble).
 */
public class ElevatorRenderer extends MobRenderer<Elevator, ModelElevator> {
    /** Index 0 unused; 1..10 match the original's texture1..texture10. */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[11];

    static {
        for (int i = 1; i <= 10; ++i) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                    "textures/entity/elevator" + i + ".png");
        }
    }

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "elevator"), "main");

    public ElevatorRenderer(EntityRendererProvider.Context context) {
        // orig RenderElevator.java:23 — shadow 0.25.
        super(context, new ModelElevator(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(Elevator entity) {
        int color = entity.getColor();
        if (color < 1 || color > 10) color = 1;
        return TEXTURES[color];
    }

    @Override
    protected void scale(Elevator entity, PoseStack poseStack, float partialTick) {
        // LivingEntityRenderer.render: scale(-1,-1,1); this.scale(...); translate(0,-1.501,0).
        // orig RenderElevator.java:43-44 flips and renders with NO lift, so cancel vanilla's in the
        // already-flipped frame: local +1.501 == world -1.501 (ENT-S-091, exact cancellation).
        poseStack.translate(0.0F, 1.501F, 0.0F);
    }

    @Override
    protected void setupRotations(Elevator entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks, scale);
        // orig RenderElevator.java:31-38 — boat-style hit wobble.
        float hitTime = (float) entity.getTimeSinceHit() - partialTicks;
        float damage = entity.getDamageTaken() - partialTicks;
        if (damage < 0.0f) {
            damage = 0.0f;
        }
        if (hitTime > 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    Mth.sin(hitTime) * hitTime * damage / 10.0f * (float) entity.getForwardDirection()));
        }
    }
}
