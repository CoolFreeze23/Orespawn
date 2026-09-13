package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Irukandji;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Irukandji (the third Tier-2 slice, 2026-09-13): {@link ModelIrukandji#setupAnim} verbatim on the converted
 * rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate
 * stays closed until an artist delivers {@code idle} and {@code walk}). Four two-part tentacles (orig
 * ModelIrukandji.java:90-133): each root part pitches and rolls on its own cosines, and its tip's pivot FOLLOWS the
 * root - {@code z = root.z + sin(pitch) * 7}, {@code x = root.x - sin(roll) * cos(pitch) * 7},
 * {@code y = root.y + cos(roll) * cos(pitch) * 7} - a POSITION-write idiom carried through {@link #moveTo}; the tip
 * then pitches and rolls on two more cosines. The roots' pivots are never written (the bind), read through
 * {@link #classicPosition}.
 *
 * <p>Scale and shadow follow {@link IrukandjiRenderer}: 0.25 render scale and a 0.1 x 0.25 shadow (ENT-S-092).</p>
 */
public final class IrukandjiGeoReplacement extends OreSpawnGeoReplacement<Irukandji> {
    private static final GeoReplacementDescriptor<Irukandji> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.IRUKANDJI.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Irukandji.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/irukandji.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/irukandji.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/irukandji.png"),
            IrukandjiRenderer.SHADOW) {
        @Override
        public void applyScale(Irukandji entity, PoseStack poseStack, float partialTick) {
            // orig RenderIrukandji.preRenderScale: GL11.glScalef(scale, scale, scale) (IrukandjiRenderer.scale)
            poseStack.scale(IrukandjiRenderer.SCALE, IrukandjiRenderer.SCALE, IrukandjiRenderer.SCALE);
        }
    };

    public IrukandjiGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // ModelIrukandji.setupAnim verbatim, one tentacle after another (root pitch, tip z; root roll, tip x / y; tip
        // pitch and roll), the float chain left to right.
        tentacle(processor, ageInTicks, "t11", "t12", 0.55f, 0.35f, 0.45f, 0.25f);
        tentacle(processor, ageInTicks, "t21", "t22", 0.65f, 0.45f, 0.55f, 0.35f);
        tentacle(processor, ageInTicks, "t31", "t32", 0.5f, 0.3f, 0.4f, 0.2f);
        tentacle(processor, ageInTicks, "t41", "t42", 0.57f, 0.37f, 0.48f, 0.29f);
    }

    /** One tentacle of the classic's four identical blocks: the root's X / Z cosines, the tip's follow, the tip's X / Z cosines. */
    private static void tentacle(AnimationProcessor<?> processor, float ageInTicks, String root, String tip,
                                 float rootPitch, float rootRoll, float tipPitch, float tipRoll) {
        float newangle = Mth.cos(ageInTicks * rootPitch) * (float) Math.PI * 0.15f;
        rotateX(processor, root, newangle);
        float d1 = (float) (Math.sin(newangle) * 7.0);
        float d2 = (float) (Math.cos(newangle) * 7.0);
        float[] rootPivot = classicPosition(bone(processor, root));  // never written: the bind pivot
        float tipZ = rootPivot[2] + d1;
        newangle = Mth.cos(ageInTicks * rootRoll) * (float) Math.PI * 0.1f;
        rotateZ(processor, root, newangle);
        float d3 = (float) (Math.cos(newangle) * (double) d2);
        float d4 = (float) (Math.sin(newangle) * (double) d2);
        float tipX = rootPivot[0] - d4;
        float tipY = rootPivot[1] + d3;
        moveTo(processor, tip, tipX, tipY, tipZ);
        rotateX(processor, tip, Mth.cos(ageInTicks * tipPitch) * (float) Math.PI * 0.15f);
        rotateZ(processor, tip, Mth.cos(ageInTicks * tipRoll) * (float) Math.PI * 0.1f);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Irukandji, IrukandjiGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new IrukandjiGeoReplacement());
        }
    }
}
