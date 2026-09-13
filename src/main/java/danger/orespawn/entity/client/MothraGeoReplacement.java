package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Mothra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Mothra (the hook survey, 2026-09-14): a consumer of the Butterfly rig. orig
 * ClientProxyOreSpawn.java:411 registers {@code new RenderButterfly(new ModelButterfly(0.2f), 0.75f, 10.0f)}
 * over the eye-moth sheet, so this descriptor shares {@link ButterflyGeoReplacement}'s geo, clip file and
 * hook under its own registry path ({@code mothra}; one profile per registry path even for a shared rig) at Mothra's own wingspeed 0.2f.
 *
 * <p>Scale and shadow follow {@link MothraRenderer}: 10.0 render scale and a 0.75 x 10.0 shadow (ENT-S-092); the one
 * texture {@code eyemoth.png} ({@link MothraRenderer#getTextureLocation}). The classic renderer's unconditional
 * {@code shouldRender} (OPT-013) is a renderer matter for the landing slice, not a pose fact.</p>
 */
public final class MothraGeoReplacement extends OreSpawnGeoReplacement<Mothra> {
    /** orig ClientProxyOreSpawn.java:411 {@code new ModelButterfly(0.2f)}: Mothra's wingspeed. */
    static final float WINGSPEED = 0.2F;
    private static final GeoReplacementDescriptor<Mothra> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.MOTHRA.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Mothra.class,
            // the Butterfly's shipped rig and clip file, one rig for four consumers
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/butterfly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/butterfly.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png"),
            MothraRenderer.SHADOW) {
        @Override
        public void applyScale(Mothra entity, PoseStack poseStack, float partialTick) {
            // orig RenderButterfly.java:26,42 preRenderScale: GL11.glScalef(scale, scale, scale) (MothraRenderer.scale)
            poseStack.scale(MothraRenderer.SCALE, MothraRenderer.SCALE, MothraRenderer.SCALE);
        }
    };

    public MothraGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ButterflyGeoReplacement.pose(processor, inputs.ageInTicks(), WINGSPEED);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Mothra, MothraGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new MothraGeoReplacement());
        }
    }
}
