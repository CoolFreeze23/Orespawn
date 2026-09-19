package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.VampireButterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Vampire Butterfly (the hook survey, 2026-09-14; landed by the Butterfly rig's slice, 2026-09-19, TEST-019): a
 * consumer of the Butterfly rig. The port-only registration ({@link VampireButterflyRenderer}: no 1.7.10 {@code RenderButterfly}
 * line for it) draws the shared {@link ButterflyModel} at the default wingspeed 1.0f (orig
 * ModelButterfly.java:23) over {@code vampire_butterfly.png}, so this descriptor shares {@link ButterflyGeoReplacement}'s geo,
 * clip file and hook under its own registry path ({@code vampire_butterfly}; one profile per registry path even for a shared rig).
 *
 * <p>Shadow follows {@link VampireButterflyRenderer}'s constructor ({@code super(context, model, 0.2f)}: the literal it passes -
 * it declares no SHADOW constant and no scale override, and no reference renderer pins it).</p>
 */
public final class VampireButterflyGeoReplacement extends OreSpawnGeoReplacement<VampireButterfly> {
    /** orig ModelButterfly.java:23 {@code wingspeed = 1.0f}: the default the port-only renderer passes. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<VampireButterfly> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.VAMPIRE_BUTTERFLY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            VampireButterfly.class,
            // the Butterfly's shipped rig and clip file, one rig for four consumers
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/butterfly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/butterfly.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/vampire_butterfly.png"),
            // VampireButterflyRenderer's constructor passes the literal 0.2f (no SHADOW constant): the equal literal
            0.2F) {
    };

    public VampireButterflyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ButterflyGeoReplacement.pose(processor, inputs.ageInTicks(), WINGSPEED);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<VampireButterfly, VampireButterflyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new VampireButterflyGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: flyer} (tools/artist_specs/vampire_butterfly.json): {@code flying} is {@code !onGround()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.FLYER;
    }
}
