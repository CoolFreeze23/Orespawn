package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormSmall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Small Worm (the third Tier-2 slice, 2026-09-13): {@link WormSmallModel#setupAnim} verbatim on the converted
 * rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate
 * stays closed until an artist delivers {@code idle} and {@code walk}). orig ModelWormSmall.java:44-63: a two-link
 * chain of 5-unit links - the tail pitches (0.55f) and rolls (0.35f), the body's pivot FOLLOWS the tail ({@code z =
 * tail.z - sin(pitch) * 5}, {@code x = tail.x + sin(roll) * cos(pitch) * 5}, {@code y = tail.y - 5 + (5 - cos(roll) *
 * cos(pitch) * 5)}) and pitches (0.45f) / rolls (0.25f) itself, the head follows the body the same way and pitches on
 * {@code 0.62f + cos(age * 0.65f) * PI * 0.15f} / rolls on a 0.3f cosine. A POSITION-write idiom through
 * {@link #moveTo}; the tail's pivot is never written (the bind), read through {@link #classicPosition}.
 *
 * <p>Shadow follows {@link WormSmallRenderer}: a 0.1 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0, so
 * no scale hook.</p>
 */
public final class WormSmallGeoReplacement extends OreSpawnGeoReplacement<EntityWormSmall> {
    private static final GeoReplacementDescriptor<EntityWormSmall> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_WORM_SMALL.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityWormSmall.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/wormsmall.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/wormsmall.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormsmall.png"),
            WormSmallRenderer.SHADOW) {
    };

    public WormSmallGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // WormSmallModel.setupAnim verbatim, the float / double chain exactly as the classic casts it.
        float newangle = Mth.cos(ageInTicks * 0.55F) * (float) Math.PI * 0.15F;
        rotateX(processor, "tail", newangle);
        float d1 = (float) (Math.sin(newangle) * 5.0);
        float d2 = (float) (Math.cos(newangle) * 5.0);
        float[] tail = classicPosition(bone(processor, "tail"));  // never written: the bind pivot
        float bodyZ = tail[2] - d1;

        float newangle2 = Mth.cos(ageInTicks * 0.35F) * (float) Math.PI * 0.1F;
        rotateZ(processor, "tail", newangle2);
        float d3 = (float) (Math.cos(newangle2) * d2);
        float d4 = (float) (Math.sin(newangle2) * d2);
        float bodyX = tail[0] + d4;
        float bodyY = (float) (tail[1] - 5.0 + (5.0 - d3));
        moveTo(processor, "body", bodyX, bodyY, bodyZ);

        newangle = Mth.cos(ageInTicks * 0.45F) * (float) Math.PI * 0.15F;
        rotateX(processor, "body", newangle);
        d1 = (float) (Math.sin(newangle) * 5.0);
        d2 = (float) (Math.cos(newangle) * 5.0);
        float headZ = bodyZ - d1;

        newangle2 = Mth.cos(ageInTicks * 0.25F) * (float) Math.PI * 0.1F;
        rotateZ(processor, "body", newangle2);
        d3 = (float) (Math.cos(newangle2) * d2);
        d4 = (float) (Math.sin(newangle2) * d2);
        float headX = bodyX + d4;
        float headY = (float) (bodyY - 5.0 + (5.0 - d3));
        moveTo(processor, "head", headX, headY, headZ);

        rotateX(processor, "head", 0.62F + Mth.cos(ageInTicks * 0.65F) * (float) Math.PI * 0.15F);
        rotateZ(processor, "head", Mth.cos(ageInTicks * 0.3F) * (float) Math.PI * 0.05F);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityWormSmall, WormSmallGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new WormSmallGeoReplacement());
        }
    }
}
