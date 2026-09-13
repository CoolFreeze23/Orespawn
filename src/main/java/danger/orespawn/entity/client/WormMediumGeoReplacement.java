package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormMedium;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Medium Worm (the third Tier-2 slice, 2026-09-13): {@link WormMediumModel#setupAnim} verbatim on the
 * converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). orig ModelWormMedium.java:79-125: a
 * three-segment chain of 12-unit links - the tail pitches (0.45f) and rolls (0.25f), the body's pivot FOLLOWS the tail
 * ({@code z = tail.z - sin(pitch) * 12}, {@code x = tail.x + sin(roll) * cos(pitch) * 12}, {@code y = tail.y - 12 +
 * (12 - cos(roll) * cos(pitch) * 12)}) and pitches (0.35f) / rolls (0.15f) itself, the head and its inner copy follow
 * the body the same way and pitch on {@code 0.62f + cos(age * 0.55f) * PI * 0.15f} / roll on a 0.25f cosine; the four
 * teeth follow the head one more link on, offset +-1 unit (z for the top pair, x for the side pair), and open on a
 * 0.55f cosine about 0.4 rad (X for the top pair, Z for the side pair). A POSITION-write idiom through
 * {@link #moveTo}; the tail's pivot is never written (the bind), read through {@link #classicPosition}; every value
 * the classic reads back from a part it just wrote is held in a local.
 *
 * <p>Shadow follows {@link WormMediumRenderer}: a 0.25 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0,
 * so no scale hook.</p>
 */
public final class WormMediumGeoReplacement extends OreSpawnGeoReplacement<EntityWormMedium> {
    private static final GeoReplacementDescriptor<EntityWormMedium> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_WORM_MEDIUM.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityWormMedium.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/wormmedium.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/wormmedium.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormmedium.png"),
            WormMediumRenderer.SHADOW) {
    };

    public WormMediumGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // WormMediumModel.setupAnim verbatim, the float / double chain exactly as the classic casts it.
        float newangle;
        newangle = Mth.cos(ageInTicks * 0.45f) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail", newangle);
        float d1 = (float) (Math.sin(newangle) * 12.0);
        float d2 = (float) (Math.cos(newangle) * 12.0);
        float[] tail = classicPosition(bone(processor, "tail"));  // never written: the bind pivot
        float bodyZ = tail[2] - d1;
        newangle = Mth.cos(ageInTicks * 0.25f) * (float) Math.PI * 0.08f;
        rotateZ(processor, "tail", newangle);
        float d3 = (float) (Math.cos(newangle) * (double) d2);
        float d4 = (float) (Math.sin(newangle) * (double) d2);
        float bodyX = tail[0] + d4;
        float bodyY = (float) ((double) tail[1] - 12.0 + (12.0 - (double) d3));
        moveTo(processor, "body", bodyX, bodyY, bodyZ);
        newangle = Mth.cos(ageInTicks * 0.35f) * (float) Math.PI * 0.1f;
        rotateX(processor, "body", newangle);
        d1 = (float) (Math.sin(newangle) * 12.0);
        d2 = (float) (Math.cos(newangle) * 12.0);
        float headZ = bodyZ - d1;
        newangle = Mth.cos(ageInTicks * 0.15f) * (float) Math.PI * 0.07f;
        rotateZ(processor, "body", newangle);
        d3 = (float) (Math.cos(newangle) * (double) d2);
        d4 = (float) (Math.sin(newangle) * (double) d2);
        float headX = bodyX + d4;
        float headY = (float) ((double) bodyY - 12.0 + (12.0 - (double) d3));
        moveTo(processor, "head", headX, headY, headZ);
        moveTo(processor, "head2", headX, headY, headZ);
        float headPitch = 0.62f + Mth.cos(ageInTicks * 0.55f) * (float) Math.PI * 0.15f;
        rotateX(processor, "head", headPitch);
        rotateX(processor, "head2", headPitch);
        float headRoll = Mth.cos(ageInTicks * 0.25f) * (float) Math.PI * 0.05f;
        rotateZ(processor, "head", headRoll);
        rotateZ(processor, "head2", headRoll);
        newangle = headPitch;
        float toothPitch = newangle;
        d1 = (float) (Math.sin(newangle) * 12.0);
        d2 = (float) (Math.cos(newangle) * 12.0);
        float toothZ = headZ - d1;
        newangle = headRoll;
        float toothRoll = newangle;
        d3 = (float) (Math.cos(newangle) * (double) d2);
        d4 = (float) (Math.sin(newangle) * (double) d2);
        float toothX = headX + d4;
        float toothY = (float) ((double) headY - 12.0 + (12.0 - (double) d3));
        float openAngle = Mth.cos(ageInTicks * 0.55f) * (float) Math.PI * 0.15f;
        moveTo(processor, "tooth1", toothX, toothY, toothZ + 1.0f);
        moveTo(processor, "tooth2", toothX, toothY, toothZ - 1.0f);
        moveTo(processor, "tooth3", toothX + 1.0f, toothY, toothZ);
        moveTo(processor, "tooth4", toothX - 1.0f, toothY, toothZ);
        rotateX(processor, "tooth1", toothPitch - 0.4f - openAngle);
        rotateX(processor, "tooth2", toothPitch + 0.4f + openAngle);
        rotateX(processor, "tooth3", toothPitch);
        rotateX(processor, "tooth4", toothPitch);
        rotateZ(processor, "tooth1", toothRoll);
        rotateZ(processor, "tooth2", toothRoll);
        rotateZ(processor, "tooth3", toothRoll + 0.4f + openAngle);
        rotateZ(processor, "tooth4", toothRoll - 0.4f - openAngle);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityWormMedium, WormMediumGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new WormMediumGeoReplacement());
        }
    }
}
