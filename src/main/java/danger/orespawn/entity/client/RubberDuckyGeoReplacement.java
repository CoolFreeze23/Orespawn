package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRubberDucky;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Rubber Ducky (the third Tier-2 slice, 2026-09-13): {@link RubberDuckyModel#setupAnim} verbatim on the
 * converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). The port's classic model as it is:
 * the HEAD-LOOK idiom (head and beak yaw by {@code netHeadYaw} in radians) and the wings' {@code |cos|} idiom
 * ({@code |cos(age * 1.0f) * PI * 0.15f|}, mirrored about Z; orig ModelRubberDucky.java:88, :91, :111-114 for the
 * lines the port keeps - the register line of this slice records where the port's pose departs from the 1.7.10 one).
 *
 * <p>Scale and shadow follow {@link RubberDuckyRenderer}: 0.75 render scale, halved for a baby, and a 0.15 x 0.75 shadow
 * (ENT-S-092).</p>
 */
public final class RubberDuckyGeoReplacement extends OreSpawnGeoReplacement<EntityRubberDucky> {
    private static final GeoReplacementDescriptor<EntityRubberDucky> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_RUBBER_DUCKY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityRubberDucky.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/rubberducky.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/rubberducky.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rubberducky.png"),
            RubberDuckyRenderer.SHADOW) {
        @Override
        public void applyScale(EntityRubberDucky entity, PoseStack poseStack, float partialTick) {
            // orig RenderRubberDucky.preRenderScale (:40-46): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            if (entity.isBaby()) {
                poseStack.scale(RubberDuckyRenderer.SCALE / 2.0F, RubberDuckyRenderer.SCALE / 2.0F, RubberDuckyRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(RubberDuckyRenderer.SCALE, RubberDuckyRenderer.SCALE, RubberDuckyRenderer.SCALE);
        }
    };

    public RubberDuckyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // RubberDuckyModel.setupAnim verbatim: the beak follows the head's yaw; the wings fold on |cos|.
        float headYaw = netHeadYaw * ((float) Math.PI / 180F);
        rotateY(processor, "head", headYaw);
        rotateY(processor, "beak", headYaw);
        float wingAngle = Mth.cos(ageInTicks * 1.0f) * (float) Math.PI * 0.15f;
        wingAngle = Math.abs(wingAngle);
        rotateZ(processor, "lwing", -wingAngle);
        rotateZ(processor, "rwing", wingAngle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityRubberDucky, RubberDuckyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RubberDuckyGeoReplacement());
        }
    }
}
