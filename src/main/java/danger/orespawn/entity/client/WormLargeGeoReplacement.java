package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityWormLarge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Large Worm (the third Tier-2 slice, 2026-09-13): {@link WormLargeModel#setupAnim} verbatim on the converted
 * rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate
 * stays closed until an artist delivers {@code idle} and {@code walk}). orig ModelWormLarge.java:185-274: the five neck
 * parts pitch on {@code cos(age * 0.25f) * PI * 0.08f - 0.698f} and yaw on {@code cos(age * 0.15f) * PI * 0.07f}; the
 * five head parts' pivot FOLLOWS the neck 32 units along (cos, sin) of those angles (a POSITION-write idiom, through
 * {@link #moveTo}) and they pitch / yaw on 0.35f / 0.45f cosines; the eight teeth sit 19 units on from the head with
 * fixed offsets, shifted by {@code sin} of the head's angles, and open on a 0.57f cosine (X for the top / bottom pair,
 * Y for the side pair, both for the four corners); the tail tip pitches on a 0.63f cosine around 0.35 rad and yaws on
 * the same cosine a quarter turn ahead ({@code + 1.57075}). The neck's pivot is never written (the bind), read through
 * {@link #classicPosition}; every value the classic reads back from a part it just wrote is held in a local.
 *
 * <p>Shadow follows {@link WormLargeRenderer}: a 0.9 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0, so
 * no scale hook.</p>
 */
public final class WormLargeGeoReplacement extends OreSpawnGeoReplacement<EntityWormLarge> {
    private static final GeoReplacementDescriptor<EntityWormLarge> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_WORM_LARGE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityWormLarge.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/wormlarge.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/wormlarge.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/wormlarge.png"),
            WormLargeRenderer.SHADOW) {
    };

    private static final String[] NECKS = {"neck1", "neck4", "neck5", "neck3", "neck2"};
    private static final String[] HEADS = {"head1", "head4", "head5", "head3", "head2"};

    public WormLargeGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // WormLargeModel.setupAnim verbatim, the float / double chain exactly as the classic casts it.
        float newangle2;
        double dist = 32.0;
        float newangle = Mth.cos(ageInTicks * 0.25f) * (float) Math.PI * 0.08f;
        newangle -= 0.698f;
        float neckX = newangle;
        newangle2 = Mth.cos(ageInTicks * 0.15f) * (float) Math.PI * 0.07f;
        float neckY = newangle2;
        for (String neck : NECKS) {
            rotateX(processor, neck, neckX);
            rotateY(processor, neck, neckY);
        }
        float[] neck1 = classicPosition(bone(processor, "neck1"));  // never written: the bind pivot
        double d1 = (float) (Math.cos(newangle) * dist);
        double d2 = (float) (Math.sin(newangle) * dist);
        float head1Z = (float) ((double) neck1[2] - d1);
        double d3 = (float) (Math.sin(newangle2) * d1);
        double d4 = (float) (Math.cos(newangle2) * d1);
        float head1X = (float) ((double) neck1[0] - d3);
        float head1Y = (float) ((double) neck1[1] + d2);
        newangle = Mth.cos(ageInTicks * 0.35f) * (float) Math.PI * 0.15f;
        float headPitch = newangle;
        newangle2 = Mth.cos(ageInTicks * 0.45f) * (float) Math.PI * 0.05f;
        float headYaw = newangle2;
        for (String head : HEADS) {
            moveTo(processor, head, head1X, head1Y, head1Z);
            rotateX(processor, head, headPitch);
            rotateY(processor, head, headYaw);
        }
        dist = 19.0;
        d1 = (float) (Math.cos(newangle) * dist);
        d2 = (float) (Math.sin(newangle) * dist);
        float tooth1Z = (float) ((double) head1Z - d1);
        d3 = (float) (Math.sin(newangle2) * d1);
        d4 = (float) (Math.cos(newangle2) * d1);
        float tooth1X = (float) ((double) head1X - d3);
        float tooth1Y = (float) ((double) head1Y + d2 - 9.0);
        float tooth2Z = tooth1Z;
        float tooth2X = tooth1X;
        float tooth2Y = tooth1Y + 18.0f;
        float tooth3Z = tooth1Z;
        float tooth3X = tooth1X + 9.0f;
        float tooth3Y = tooth1Y + 9.0f;
        float tooth4Z = tooth1Z;
        float tooth4X = tooth1X - 9.0f;
        float tooth4Y = tooth1Y + 9.0f;
        float tooth5Z = tooth1Z;
        float tooth5X = tooth1X - 6.0f;
        float tooth5Y = tooth1Y + 9.0f - 6.0f;
        float tooth6Z = tooth1Z;
        float tooth6X = tooth1X + 6.0f;
        float tooth6Y = tooth1Y + 9.0f + 6.0f;
        float tooth7Z = tooth1Z;
        float tooth7X = tooth1X + 6.0f;
        float tooth7Y = tooth1Y + 9.0f - 6.0f;
        float tooth8Z = tooth1Z;
        float tooth8X = tooth1X - 6.0f;
        float tooth8Y = tooth1Y + 9.0f + 6.0f;
        tooth1Z = (float) ((double) tooth1Z - Math.sin(headPitch) * 9.0);
        tooth2Z = (float) ((double) tooth2Z + Math.sin(headPitch) * 9.0);
        tooth3Z = (float) ((double) tooth3Z - Math.sin(headYaw) * 9.0);
        tooth4Z = (float) ((double) tooth4Z + Math.sin(headYaw) * 9.0);
        tooth7Z = (float) ((double) tooth7Z - Math.sin(headPitch) * 6.0);
        tooth7Z = (float) ((double) tooth7Z - Math.sin(headYaw) * 6.0);
        tooth6Z = (float) ((double) tooth6Z + Math.sin(headPitch) * 6.0);
        tooth6Z = (float) ((double) tooth6Z - Math.sin(headYaw) * 6.0);
        tooth5Z = (float) ((double) tooth5Z - Math.sin(headPitch) * 6.0);
        tooth5Z = (float) ((double) tooth5Z + Math.sin(headYaw) * 6.0);
        tooth8Z = (float) ((double) tooth8Z + Math.sin(headPitch) * 6.0);
        tooth8Z = (float) ((double) tooth8Z + Math.sin(headYaw) * 6.0);
        moveTo(processor, "tooth1", tooth1X, tooth1Y, tooth1Z);
        moveTo(processor, "tooth2", tooth2X, tooth2Y, tooth2Z);
        moveTo(processor, "tooth3", tooth3X, tooth3Y, tooth3Z);
        moveTo(processor, "tooth4", tooth4X, tooth4Y, tooth4Z);
        moveTo(processor, "tooth5", tooth5X, tooth5Y, tooth5Z);
        moveTo(processor, "tooth6", tooth6X, tooth6Y, tooth6Z);
        moveTo(processor, "tooth7", tooth7X, tooth7Y, tooth7Z);
        moveTo(processor, "tooth8", tooth8X, tooth8Y, tooth8Z);
        newangle = Mth.cos(ageInTicks * 0.57f) * (float) Math.PI * 0.35f;
        rotateX(processor, "tooth1", headPitch + newangle);
        rotateX(processor, "tooth2", headPitch - newangle);
        rotateY(processor, "tooth3", headYaw + newangle);
        rotateY(processor, "tooth4", headYaw - newangle);
        rotateX(processor, "tooth5", headPitch + newangle);
        rotateX(processor, "tooth7", headPitch + newangle);
        rotateX(processor, "tooth6", headPitch - newangle);
        rotateX(processor, "tooth8", headPitch - newangle);
        rotateY(processor, "tooth6", headYaw + newangle);
        rotateY(processor, "tooth7", headYaw + newangle);
        rotateY(processor, "tooth5", headYaw - newangle);
        rotateY(processor, "tooth8", headYaw - newangle);
        newangle = Mth.cos(ageInTicks * 0.63f) * (float) Math.PI * 0.15f;
        rotateX(processor, "tailtip", newangle + 0.35f);
        rotateY(processor, "tailtip", Mth.cos((float) ((float) ((double) (ageInTicks * 0.63f) + 1.57075))) * (float) Math.PI * 0.15f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityWormLarge, WormLargeGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new WormLargeGeoReplacement());
        }
    }
}
