package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityButterfly;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Butterfly (the hook survey): {@link ButterflyModel#setupAnim} verbatim on the converted rig, ON THE HOOK
 * (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and {@code
 * walk}). The parameterised rig's one rhythm: the eight wing parts about Z at {@code cos(age * 1.3f *
 * wingspeed) * PI * 0.25f}, the four right wings positive and the four left wings the negative (orig
 * ModelButterfly.java:23,28 {@code wingspeed}; the port's ButterflyModel.setupAnim). The WINGSPEED is the
 * classic model's constructor argument, one per registry (orig ClientProxyOreSpawn.java:405-411: the Butterfly 1.0f,
 * the Luna Moth 0.75f, Mothra 0.2f; the Vampire Butterfly the default 1.0f, orig ModelButterfly.java:23).
 *
 * <p>One rig, four consumers (the Ant precedent): {@link LunaMothGeoReplacement}, {@link MothraGeoReplacement} and
 * {@link VampireButterflyGeoReplacement} share this class's geo, clip file and hook ({@link #pose}) under their own
 * descriptors - one profile per registry path - each with its own wingspeed, texture, shadow and scale.</p>
 *
 * <p>Scale and shadow follow {@link ButterflyRenderer}: 1.0 render scale (identity - the classic renderer has no scale
 * override, so no scale hook) and a 0.3 x 1.0 shadow (ENT-S-092); the texture per {@code getButterflyType()} exactly as
 * {@link ButterflyRenderer#getTextureLocation} switches it (its sheets are private there, so the switch is carried here).</p>
 */
public final class ButterflyGeoReplacement extends OreSpawnGeoReplacement<EntityButterfly> {
    /** orig ClientProxyOreSpawn.java:405 {@code new ModelButterfly(1.0f)}: the Butterfly's wingspeed. */
    static final float WINGSPEED = 1.0F;
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly.png");
    private static final GeoReplacementDescriptor<EntityButterfly> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_BUTTERFLY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityButterfly.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/butterfly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/butterfly.animation.json"),
            TEXTURE,
            ButterflyRenderer.SHADOW) {
        /** ButterflyRenderer.getTextureLocation: the type's sheet (1 / 2 / 3 -> butterfly2 / 3 / 4, otherwise butterfly). */
        @Override
        public ResourceLocation texture(EntityButterfly entity) {
            return textureFor(entity.getButterflyType());
        }
    };

    public ButterflyGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** The switch of {@link ButterflyRenderer#getTextureLocation}, sheet for sheet. */
    static ResourceLocation textureFor(int type) {
        return switch (type) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly2.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly3.png");
            case 3 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/butterfly4.png");
            default -> TEXTURE;
        };
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        pose(processor, inputs.ageInTicks(), WINGSPEED);
    }

    /** ButterflyModel.setupAnim verbatim, the float chain left to right; shared by the four consumers of the rig. */
    static void pose(AnimationProcessor<?> processor, float ageInTicks, float wingspeed) {
        // this.rightwing2.zRot = this.rightwing.zRot = cos(age * 1.3f * wingspeed) * PI * 0.25f; the six others copy it.
        float rightwingZRot = Mth.cos(ageInTicks * 1.3f * wingspeed) * (float) Math.PI * 0.25f;
        rotateZ(processor, "rightwing2", rightwingZRot);
        rotateZ(processor, "rightwing", rightwingZRot);
        rotateZ(processor, "rightwing3", rightwingZRot);
        rotateZ(processor, "rightwing4", rightwingZRot);
        rotateZ(processor, "leftwing", -rightwingZRot);
        rotateZ(processor, "leftwing2", -rightwingZRot);
        rotateZ(processor, "leftwing3", -rightwingZRot);
        rotateZ(processor, "leftwing4", -rightwingZRot);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityButterfly, ButterflyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new ButterflyGeoReplacement());
        }
    }

    /** The SPEC's {@code locomotion: flyer} (tools/artist_specs/butterfly.json): {@code flying} is {@code !onGround()} (contract section 3; the weights slice: every landed species carries its seed's word, the asset audit pins it). */
    @Override
    public danger.orespawn.entity.client.animation.LocomotionKind locomotion() {
        return danger.orespawn.entity.client.animation.LocomotionKind.FLYER;
    }
}
