package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityMosquito;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Mosquito (the first Tier-2 slice, 2026-09-13): {@link MosquitoModel#setupAnim} on the converted
 * rig - one frequency group, the two wing pairs flapping about Z at {@code cos(ageInTicks * 3.0F) * PI * 0.25F},
 * the right wings positive and the left wings the negative.
 *
 * <p>The SHIPPED pose is the classic hook below (the S4 doctrine); {@link #keyframeLayers()} declares the one
 * group's transcription under the bare name {@code walk} (a one-group species, contract section 2.1), so the
 * shipped {@code mosquito.animation.json} ({@code idle} keying no bone + {@code walk}) opens the gate on the
 * GeckoLib candidate. The harness's keyframe reference leg proves the layer against this hook at 2.5e-3 rad.</p>
 *
 * <p>Scale and shadow follow {@link MosquitoRenderer}: 0.5 render scale and a 0.3 x 0.5 shadow (ENT-S-092).</p>
 */
public final class MosquitoGeoReplacement extends OreSpawnGeoReplacement<EntityMosquito> {
    static final List<KeyframeLayer> KEYFRAME_LAYERS = List.of(
            new KeyframeLayer("wings", KeyframeLayer.WALK, 3.0F,
                    Set.of("rightwing1", "rightwing2", "leftwing1", "leftwing2"), false));
    private static final GeoReplacementDescriptor<EntityMosquito> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_MOSQUITO.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityMosquito.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/mosquito.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/mosquito.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/mosquito.png"),
            MosquitoRenderer.SHADOW) {
        @Override
        public void applyScale(EntityMosquito entity, PoseStack poseStack, float partialTick) {
            // orig RenderMosquito.preRenderScale: GL11.glScalef(scale, scale, scale) (MosquitoRenderer.scale)
            poseStack.scale(MosquitoRenderer.SCALE, MosquitoRenderer.SCALE, MosquitoRenderer.SCALE);
        }
    };

    public MosquitoGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public List<KeyframeLayer> keyframeLayers() {
        return KEYFRAME_LAYERS;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // MosquitoModel.setupAnim:65-69 verbatim (orig ModelMosquito.render).
        float flap = Mth.cos(ageInTicks * 3.0F) * (float) Math.PI * 0.25F;
        rotateZ(processor, "rightwing1", flap);
        rotateZ(processor, "rightwing2", flap);
        rotateZ(processor, "leftwing1", -flap);
        rotateZ(processor, "leftwing2", -flap);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityMosquito, MosquitoGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new MosquitoGeoReplacement());
        }
    }
}
