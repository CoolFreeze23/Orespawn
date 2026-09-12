package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityBrutalfly;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Brutalfly (the first Tier-2 slice, 2026-09-13): {@link BrutalflyModel#setupAnim} on the converted rig -
 * one frequency group over twelve wing parts about Z, {@code cos(ageInTicks * 1.3f * wingspeed) * PI * 0.25f} on the
 * six right wings and the negative on the six left (wingspeed 0.2f, orig ModelBrutalfly.java:27,32 /
 * ClientProxyOreSpawn.java:507). The phase is the chain's two float multiplies left to right,
 * {@code (age * 1.3f) * 0.2f}, never {@code age * 0.26f}.
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine); {@link #keyframeLayers()} declares the one
 * group's transcription under the bare name {@code walk} (a one-group species, contract section 2.1), so the
 * shipped {@code brutalfly.animation.json} ({@code idle} keying no bone + {@code walk}) opens the gate on the
 * GeckoLib candidate. The harness's keyframe reference leg proves the layer against this hook at 2.5e-3 rad.</p>
 *
 * <p>Scale and shadow follow {@link BrutalflyRenderer}: 9.0 render scale and a 0.75 x 9.0 shadow (ENT-S-092).</p>
 */
public final class BrutalflyGeoReplacement extends OreSpawnGeoReplacement<EntityBrutalfly> {
    /** orig ModelBrutalfly.java:27,32 {@code wingspeed} = 0.2f (ClientProxyOreSpawn.java:507): the chain's second multiply. */
    static final float WINGSPEED = 0.2F;
    static final Set<String> RIGHT_WINGS = Set.of("rightwing", "rightwing2", "rightwing3", "rightwing4", "rightwing5", "rightwing6");
    static final Set<String> LEFT_WINGS = Set.of("leftwing", "leftwing2", "leftwing3", "leftwing4", "leftwing5", "leftwing6");
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("wings", KeyframeLayer.WALK, 1.3F, WINGSPEED,
                    Set.of("rightwing", "rightwing2", "rightwing3", "rightwing4", "rightwing5", "rightwing6",
                            "leftwing", "leftwing2", "leftwing3", "leftwing4", "leftwing5", "leftwing6"), false));
    private static final GeoReplacementDescriptor<EntityBrutalfly> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_BRUTALFLY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityBrutalfly.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/brutalfly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/brutalfly.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/brutalfly.png"),
            BrutalflyRenderer.SHADOW) {
        @Override
        public void applyScale(EntityBrutalfly entity, PoseStack poseStack, float partialTick) {
            // orig RenderBrutalfly.preRenderScale: GL11.glScalef(scale, scale, scale) (BrutalflyRenderer.scale)
            poseStack.scale(BrutalflyRenderer.SCALE, BrutalflyRenderer.SCALE, BrutalflyRenderer.SCALE);
        }
    };

    public BrutalflyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // BrutalflyModel.setupAnim:126-136 verbatim: one angle, the right wings positive, the left the negative.
        float flap = Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f;
        for (String wing : RIGHT_WINGS) {
            rotateZ(processor, wing, flap);
        }
        for (String wing : LEFT_WINGS) {
            rotateZ(processor, wing, -flap);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityBrutalfly, BrutalflyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BrutalflyGeoReplacement());
        }
    }
}
