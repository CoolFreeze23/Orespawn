package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Mothra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Mothra (the hook survey, 2026-09-14; landed by the Butterfly rig's slice, 2026-09-19 - TEST-019,
 * its t_three_quarter tie pinned by the entry's cap): a consumer of the Butterfly rig. orig
 * ClientProxyOreSpawn.java:411 registers {@code new RenderButterfly(new ModelButterfly(0.2f), 0.75f, 10.0f)}
 * over the eye-moth sheet, so this descriptor shares {@link ButterflyGeoReplacement}'s geo, clip file and hook
 * under its own registry path ({@code mothra}; one profile per registry path even for a shared rig) at Mothra's own wingspeed 0.2f.
 * <p>Scale and shadow follow {@link MothraRenderer}: 10.0 render scale and a 0.75 x 10.0 shadow (ENT-S-092); the one
 * texture {@code eyemoth.png} ({@link MothraRenderer#getTextureLocation}). The classic renderer's unconditional
 * {@code shouldRender} (OPT-013) is carried onto {@link Renderer}, not a pose fact.</p>
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

        /** The classic {@link MothraRenderer#shouldRender} (OPT-013): unconditionally drawn, never frustum-culled by its hitbox (the Butterfly rig's slice, 2026-09-19). */
        @Override
        public boolean shouldRender(Mothra entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return true;
        }
    }

    /** The SPEC's {@code locomotion: flyer} (tools/artist_specs/mothra.json): {@code flying} is {@code !onGround()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.FLYER;
    }
}
